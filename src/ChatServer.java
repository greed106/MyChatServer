
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.*;

public class ChatServer{
    //服务器的端口号
    private static final int Port = 1234;
    //储存在线的客户端连接信息的映射
    private HashMap<String, ClientConnection> clientsMap;
    //用户信息数据库
    private DataBase dataBase;
    private final String ServerName = "服务器";
    //主函数
    public static void main(String[] args){
        ChatServer chatServer = new ChatServer();
        chatServer.startServer();
    }
    //服务器,启动！
    public void startServer(){
        //创建储存客户端连接的映射
        clientsMap = new HashMap<>();
        dataBase = new DataBase();
        try{
            //创建服务器使用的套接字
            //服务器端使用的套接字
            ServerSocket serverSock = new ServerSocket(Port);
            System.out.println("服务器serverSock已建立");

            //进入接受并处理消息的循环之中
            while(true){
                //接收客户端请求并创建客户端需要的套接字
                Socket clientSock = serverSock.accept();
                System.out.println("新的客户端建立连接");

                //向客户端发送消息的输出流
                PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                ObjectOutputStream objOut = new ObjectOutputStream(clientSock.getOutputStream());
                //接收客户端消息的输入流
                Scanner scanner = new Scanner(clientSock.getInputStream());
                ObjectInputStream objIn = new ObjectInputStream(clientSock.getInputStream());

                //创建客户端的原型
                ClientConnection client = new ClientConnection(clientSock,writer,scanner,objOut,objIn);

                Thread t = new Thread(new Handler(client));
                t.start();

            }

        } catch (IOException e) {
            //throw new RuntimeException(e);
            //打印未成功创建服务器使用套接字的异常
            System.out.println("发生了未成功创建服务器使用套接字的异常");
            e.printStackTrace();
        }
    }
    private void broadcastStatus(Message message){
        StatusUpdateMessage sMes = (StatusUpdateMessage) message;
        int uid = dataBase.getUidByUsername(message.getNameSender());
        List<Integer>FriendsUid = dataBase.getAcceptedFriendsUid(uid);
        for(int friendUid: FriendsUid){
            String friendName = dataBase.getUsernameByUid(friendUid);
            ClientConnection clientConnetion = clientsMap.get(friendName);
            if (clientConnetion != null){
                try {
                    clientConnetion.objOut.writeObject(new StatusUpdateMessage(
                            ServerName,clientConnetion.getUsername(),sMes.getNameSender(),
                            "StatusUpdateMessage",sMes.isOnline()
                    ));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //创建内部类Handler,实现Runnable接口
    //并完成客户端具体操作的业务逻辑
    class Handler implements Runnable{
        //被控制的对应的客户端信息
        ClientConnection client;
        public Handler(ClientConnection client) {
            this.client = client;
        }

        //具体的业务逻辑实现
        @Override
        public void run() {
            try {
                CommandExecutorServer executor = new CommandExecutorServer();
                while (!client.socket.isClosed()) {
                    //读入消息
                    Object obj = client.objIn.readObject();
                    Message mes = null;
                    //判断消息类型
                    if (obj instanceof CheckNameMessage)
                        mes = (CheckNameMessage) obj;
                    if (obj instanceof CreatClientMessage)
                        mes = (CreatClientMessage) obj;
                    if (obj instanceof SendChatMessage)
                        mes = (SendChatMessage) obj;
                    if(obj instanceof CheckUserMessage)
                        mes = (CheckUserMessage) obj;
                    if(obj instanceof CreatUserMessage)
                        mes = (CreatUserMessage) obj;
                    if(obj instanceof GetUserMessage)
                        mes = (GetUserMessage) obj;
                    if(obj instanceof GetFriendsMessage)
                        mes = (GetFriendsMessage) obj;
                    if(obj instanceof StatusUpdateMessage)
                        mes = (StatusUpdateMessage) obj;
                    if(obj instanceof UnreadUpdateMessage)
                        mes = (UnreadUpdateMessage) obj;
                    if(obj instanceof GetRequestsMessage)
                        mes = (GetRequestsMessage) obj;
                    if(obj instanceof UpdateRequestMessage)
                        mes = (UpdateRequestMessage) obj;
                    if(obj instanceof GetSearchFriendMessage)
                        mes = (GetSearchFriendMessage) obj;
                    if(obj instanceof AddRequestMessage)
                        mes = (AddRequestMessage) obj;
                    if(mes != null)
                        executor.executeCommand(mes, client);
                    else
                        System.out.println("接收到了无效的命令");
                }
            }catch (SocketException e) {
                String nameClient = client.getUsername();
                clientsMap.remove(nameClient).getUsername();
                System.out.println("username: "+nameClient+"用户下线，对应Socket连接已关闭");
                System.out.println("当前连接数："+clientsMap.size());
                broadcastStatus(new StatusUpdateMessage(nameClient,ServerName,nameClient,
                        "StatusUpdateMessage",false));
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }
    class CommandCheckNameServer implements Command{

        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                System.out.println("收到CheckName请求");
                //如果用户已存在的话，name不合法
                if(dataBase.isUsernameDuplicate(message.getMessage())){
                    //抛出异常
                    throw new RenameInMapException();
                }
                //用户名合法
                Message mes = new isErrorMessage(ChatServer.this.ServerName,"","用户名合法","isErrorMessage",false);
                client.objOut.writeObject(mes);
            }catch (IOException e) {
                throw new RuntimeException(e);
            } catch (RenameInMapException e) {
                try {
                    System.out.println("发生了用户重命名的异常,重命名的对应用户为："+message.getMessage());
                    Message mes = new isErrorMessage(ChatServer.this.ServerName,"","用户名已存在","isErrorMessage",true);
                    client.objOut.writeObject(mes);
                    e.printStackTrace();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                //throw new RuntimeException(e);
            }
        }
    }
    class CommandCheckUserServer implements Command{

        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                System.out.println("收到了CheckUser请求");
                String[] parts = message.getMessage().split("#", 2);
                String username = parts[0];
                String password = parts.length > 1 ? parts[1] : "";
                boolean isValid = dataBase.isUserValid(username, password);
                String errorMes = "您提供的user有效";
                if(!isValid){
                    System.out.println("用户提供的user无效");
                    errorMes = "您输入的username不存在或password有误";
                }
                client.objOut.writeObject(new isErrorMessage(ServerName,message.getNameSender(),
                       errorMes ,"isErrorMessage",!isValid));
            } catch (IOException e) {
                //throw new RuntimeException(e);
                e.printStackTrace();
            }
        }
    }
    class CommandCreatClientServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            client.setUsername(message.getMessage());
            clientsMap.put(client.getUsername(), client);
            System.out.println("用户username: " + client.getUsername() + " 连接到服务器");
            System.out.println("当前连接数：" + clientsMap.size());

        }
    }
    class CommandCreatUserServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            CreatUserMessage cMes = (CreatUserMessage) message;
            dataBase.addUser(cMes.getUser());
            System.out.println("新的用户已创建并加入数据库");
        }
    }
    class CommandGetUserServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                User user = dataBase.getUserByUsername(message.getMessage());

//                System.out.println("ServerUsername:"+user.getUsername());
//                System.out.println("ServerAge:"+user.getAge());

                client.objOut.writeObject(new ReturnUserMessage(ServerName,message.getNameSender(),"user",
                        "ReturnUserMessage",user));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class CommandGetFriendsServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            //System.out.println("服务器收到了消息");
            int uid = dataBase.getUidByUsername(message.getNameSender());
            List<Integer> uidFrineds = dataBase.getAcceptedFriendsUid(uid);
            List<FriendUser> friends = new ArrayList<>();
            for(int uidFriend: uidFrineds){
                User user = dataBase.getUserByUid(uidFriend);
                boolean isOnline = clientsMap.get(user.getUsername()) != null;
                int unReadMessage = dataBase.getUnReadCount(user.getUid(),uid);

                List<HistoryChatMessage> history = dataBase.getHistoryMessage(uid,uidFriend);
                FriendUser friend = new FriendUser(user,isOnline,unReadMessage,history);
                friends.add(friend);
            }
            try {
                client.objOut.writeObject(new ReturnFriendsMessage(ServerName,message.getNameReceiver()
                        ,"ReturnFriendsMessage", "ReturnFriendsMessage",friends));
            } catch (IOException e) {
                //throw new RuntimeException(e);
                e.printStackTrace();
            }
        }
    }
    class CommandChatServer implements Command{

        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                ClientConnection receiver = clientsMap.get(message.getNameReceiver());
                boolean haveRead = receiver != null;
                //在数据库里添加信息
                dataBase.addChatMessage((SendChatMessage) message,haveRead);
                //表明用户不在线
                if(!haveRead){
                    throw new NotFoundinMapException();
                }
                //表明用户在线
                System.out.println("接收到来自 "+message.getNameSender()+" 的消息 "+message.getMessage()
                        +" 将发送给 "+message.getNameReceiver());
                receiver.objOut.writeObject(new ReadChatMessage((SendChatMessage) message));


            } catch (NotFoundinMapException e) {
                System.out.println("发生了未找到对应用户的异常，要寻找的用户为："+message.getNameReceiver());
                System.out.println("该用户可能未上线");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class CommandExecutorServer{
        private HashMap<String,Command> commandMap = new HashMap<>();
        public CommandExecutorServer(){
            commandMap.put("CheckNameMessage",new CommandCheckNameServer());
            commandMap.put("CreatClientMessage",new CommandCreatClientServer());
            commandMap.put("SendChatMessage",new CommandChatServer());
            commandMap.put("CheckUserMessage",new CommandCheckUserServer());
            commandMap.put("CreatUserMessage",new CommandCreatUserServer());
            commandMap.put("GetUserMessage",new CommandGetUserServer());
            commandMap.put("GetFriendsMessage", new CommandGetFriendsServer());
            commandMap.put("StatusUpdateMessage",new CommandStatusUpdateServer());
            commandMap.put("UnreadUpdateMessage", new CommandUnreadUpdateServer());
            commandMap.put("GetRequestsMessage",new CommandGetRequestsServer());
            commandMap.put("UpdateRequestMessage", new CommandUpdateRequestServer());
            commandMap.put("GetSearchFriendMessage", new CommandGetSearchFriendServer());
            commandMap.put("AddRequestMessage", new CommandAddRequestServer());
        }
        public void executeCommand(Message message, ClientConnection client){
            try{
                Command command = commandMap.get(message.getType());
                command.execute(client,message);
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }
    class CommandStatusUpdateServer implements Command {
        @Override
        public void execute(ClientConnection client, Message message) {
            broadcastStatus(message);
        }
    }
    class CommandUnreadUpdateServer implements Command{

        @Override
        public void execute(ClientConnection client, Message message) {
            UnreadUpdateMessage uMes = (UnreadUpdateMessage) message;
            for(int messageId : uMes.getUpdateMessageId()){
                dataBase.updateHaveReadStatus(messageId);
                System.out.println("更新了消息的未读状态，messageID:"+messageId);
            }
        }
    }
    class CommandGetRequestsServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            List<FriendRequest> requests = dataBase.getRequests(message.getNameSender());
            //System.out.println("得到的request:"+requests);
            try {
                client.objOut.writeObject(new ReturnRequestsMessage(ServerName,message.getNameSender(),
                        "ReturnRequestsMessage","ReturnRequestsMessage",
                        requests));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    class CommandUpdateRequestServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            UpdateRequestMessage uMes = (UpdateRequestMessage) message;
            if(uMes.getMessage().equals("ACCEPTED")){
                dataBase.updateRequest(uMes.getRequest());
            }else{
                dataBase.deleteRequest(uMes.getRequest().getRelationId());
            }
        }
    }
    class CommandGetSearchFriendServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            System.out.println("接收到了查询消息");
            User friend = dataBase.getSearchFriend(message.getMessage(), message.getNameSender());
            try {
                client.objOut.writeObject(new ReturnUserMessage(ServerName, message.getNameSender(),
                        "ReaturnSearchFriend", "ReturnUserMessage",friend));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    class CommandAddRequestServer implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            String nameSender = message.getNameSender();
            String nameFriend = message.getMessage();
            dataBase.addRequest(new FriendRequest(nameSender,nameFriend,"PENDING"));
        }
    }
}








class ClientConnection {
    protected Socket socket;
    protected PrintWriter writer;
    protected Scanner scanner;
    protected String username;
    protected ObjectOutput objOut;
    protected ObjectInput objIn;


    public ClientConnection(Socket socket, PrintWriter writer, Scanner scanner, String username, ObjectOutputStream objOut, ObjectInputStream objIn) {
        this.socket = socket;
        this.writer = writer;
        this.scanner = scanner;
        this.username = username;
        this.objOut = objOut;
        this.objIn = objIn;
    }

    public ClientConnection(Socket socket, PrintWriter writer, Scanner scanner, ObjectOutput objOut, ObjectInput objIn) {
        this.socket = socket;
        this.writer = writer;
        this.scanner = scanner;
        this.objOut = objOut;
        this.objIn = objIn;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public String getUsername() {
        return username;
    }

    public ObjectOutput getObjOut() {
        return objOut;
    }

    public ObjectInput getObjIn() {
        return objIn;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Message TransformInputToSendChatMessage(String type, String input){
        String[] parts = input.split("#", 2);
        String uidReceiver = parts[0];
        String messageRec = parts.length > 1 ? parts[1] : "";
        Message mes = null;
        if(type.equals("SendChatMessage"))
            mes = new SendChatMessage(this.username,uidReceiver,messageRec,type,LocalDateTime.now());
        return mes;
    }

    public boolean isConnected(){
        return socket.isConnected();
    }

}

class FriendUser extends User{
    private boolean isOnline;
    private int unReadChatMessage;
    private List<HistoryChatMessage> historyMessage;
    public FriendUser(User user,boolean isOnline, int unReadChatMessage,
                      List<HistoryChatMessage> historyMessage){
        this.uid = user.getUid();
        this.Sex = user.getSex();
        this.Username = user.getUsername();
        this.Password = user.getPassword();
        this.Email = user.getEmail();
        this.PhoneNumber = user.getPhoneNumber();
        this.Age = user.getAge();
        this.isOnline = isOnline;
        this.unReadChatMessage = unReadChatMessage;
        this.historyMessage = historyMessage;
    }
    public void addChatHistory(HistoryChatMessage hMes){
        historyMessage.add(hMes);
    }
    public boolean isOnline() {
        return isOnline;
    }
    public int getTotalHistoryPages(int pageSize){
        int total = historyMessage.size();
        int pageNumber = total / pageSize;
        if (total % pageSize != 0) {
            pageNumber++;
        }
        return pageNumber;
    }
    public int getUnReadChatMessage() {
        return unReadChatMessage;
    }
    public void sortChatHistory(){
        historyMessage.sort(new Comparator<HistoryChatMessage>() {
            @Override
            public int compare(HistoryChatMessage o1, HistoryChatMessage o2) {
                return o2.getCurrentTime().compareTo(o1.getCurrentTime()) ;
            }
        });
    }
    public List<HistoryChatMessage> getChatHistory(int currentPage,int pageSize) {
        List<HistoryChatMessage> history = new ArrayList<>();
        int total = historyMessage.size();
        int indexStart = (currentPage-1)*pageSize;
        int indexEnd = currentPage*pageSize-1;
        if(indexEnd > total - 1){
            indexEnd = total - 1;
        }
        for(int i = indexStart;i <= indexEnd;i++){
            history.add(historyMessage.get(i));
        }
        return history;
    }
}