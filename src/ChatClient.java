import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.List;

public class ChatClient extends ClientConnection {
    private UserClient userClient;
    private CommandExecutorClient executorClient;
    private HashMap<String, ChatFrame> chatFrames;
    private String ServerName = "服务器";
    static final int HistoryPageSize = 6;
    private List<FriendRequest> friendRequests;
    private final Object lock;
    private boolean receiveFriendRequest = false;
    UserInterfaceFrame userInterfaceFrame;
    public UserClient getUserClient() {
        return userClient;
    }
    public boolean isReceiveFriendRequest() {
        return receiveFriendRequest;
    }
    public void setReceiveFriendRequest(boolean receiveFriendRequest) {
        this.receiveFriendRequest = receiveFriendRequest;
    }
    public void setUserInterfaceFrame(UserInterfaceFrame userInterfaceFrame) {
        this.userInterfaceFrame = userInterfaceFrame;
    }
    public ChatClient (Socket socketUser) throws IOException {
        //进行客户端的初始化,建立输入输出流
        super(socketUser,new PrintWriter(System.out),new Scanner(System.in),
                new ObjectOutputStream(socketUser.getOutputStream()),
                new ObjectInputStream(socketUser.getInputStream()));
        userClient = new UserClient();
        executorClient = new CommandExecutorClient();
        chatFrames = new HashMap<>();
        friendRequests = new ArrayList<>();
        lock = new Object();
    }
    public void addChatFrame(ChatFrame chatFrame,String friendName){
        chatFrames.put(friendName,chatFrame);
    }
    public ChatFrame openFrame(String friendName){
        ChatFrame chatFrame = chatFrames.get(friendName);
        if(chatFrame == null){
            System.out.println("friendname:"+friendName+"对应frame不存在");
            chatFrame = new ChatFrame(friendName,this);
            addChatFrame(chatFrame,friendName);
        }
        return chatFrame;
    }
    public void removeFrame(String friendName){
        chatFrames.remove(friendName);
    }
    public void openServerReader(){
        Thread tServerReader = new Thread(new ServerReader());
        tServerReader.start();
    }
    public int getTotalHistoryPages(String friendName){
        return userClient.getTotalHistoryPages(friendName,HistoryPageSize);
    }
    public List<HistoryChatMessage> getChatHistory(int currentPage, String friendName){
        return userClient.getChatHistory(currentPage,HistoryPageSize,friendName);
    }
    public void addChatHistory(HistoryChatMessage hMes,String friendName){
        userClient.addChatHistory(hMes,friendName);
    }
    public String getServerName() {
        return ServerName;
    }
    public boolean isOnline(){
        return userClient.isOnline();
    }
    public void setFriends(List<FriendUser> friends){
        userClient.setFriends(friends);
    }
    public List<FriendUser> getFriends(){
        return userClient.getFriends();
    }
    public FriendUser getFriend(String friendName){
        return userClient.getFriend(friendName);
    }
    public List<FriendRequest> getFriendRequests(){
        return friendRequests;
    }
    public void executeCommandClient(Message mes){
        executorClient.executeCommand(mes,this);
    }
    public Object getLock() {
        return lock;
    }

    //用于处理服务器传入消息的类
    class ServerReader implements Runnable{

        //读入服务器传入消息的业务逻辑
        @Override
        public void run() {
            Object obj;
            try {
                CommandExecutorClient executor = new CommandExecutorClient();
                while(!socket.isClosed()) {
                    obj = objIn.readObject();
                    Message mes = null;

                    //判断消息的类型
                    if (obj instanceof isErrorMessage)
                        mes = (isErrorMessage) obj;
                    if(obj instanceof ReadChatMessage)
                        mes = (ReadChatMessage) obj;
                    if(obj instanceof StatusUpdateMessage)
                        mes = (StatusUpdateMessage) obj;
                    if(obj instanceof UnreadUpdateMessage)
                        mes = (UnreadUpdateMessage) obj;
                    if(obj instanceof ReturnRequestsMessage)
                        mes = (ReturnRequestsMessage) obj;
                    if(obj instanceof ReturnUserMessage)
                        mes = (ReturnUserMessage) obj;
                    if(mes != null)
                        executor.executeCommand(mes,ChatClient.this);
                    else
                        System.out.println("无效的命令");
                }
            }catch (EOFException | SocketException e){
                System.out.println("与服务器的连接已断开");
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }




        }
    }

    class CommandSendChatClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                client.objOut.writeObject(message);
                System.out.println("消息成功发送至服务器");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class CommandReadChatClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            System.out.println(message);
            ChatFrame chatFrame = openFrame(message.getNameSender());
            chatFrame.addMessage((ReadChatMessage) message);
            chatFrame.setVisible(true);
        }
    }
    class CommandUnreadUpdateClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                objOut.writeObject(message);
                userInterfaceFrame.updateUnreadMessagesCount(message.getNameReceiver(),
                        Integer.parseInt(message.getMessage()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class CommandisErrorClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            showErrorDialog(message.getMessage());
        }
        public void showErrorDialog(String errorMessage) {
            JOptionPane.showMessageDialog(
                    Frame.getFrames()[0], // Get the current active frame to center the dialog
                    errorMessage, // Error message
                    "错误", // Dialog title
                    JOptionPane.ERROR_MESSAGE // Message type
            );
        }
    }
    class CommandStatusUpdateClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            StatusUpdateMessage uMes = (StatusUpdateMessage) message;
            userInterfaceFrame.handleFriendStatusUpdate(uMes.getMessage(),uMes.isOnline() );
        }
    }
    class CommandLoginClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                uploadUser(checkUser(message));
                objOut.writeObject(new StatusUpdateMessage(username,ServerName,username,
                        "StatusUpdateMessage",true));
                System.out.println("登陆成功！");
            } catch (IOException | ClassNotFoundException | RenameInMapException e) {
                e.printStackTrace();
            }
        }
        public String checkUser(Message mes) throws RenameInMapException, IOException, ClassNotFoundException {
            String username = mes.getNameSender();
            String password = mes.getMessage();
            //向服务器发送检测user的请求
            objOut.writeObject(new CheckUserMessage(username,ServerName,username+"#"+password,"CheckUserMessage"));
            isErrorMessage isErrorMes = (isErrorMessage) objIn.readObject();
            if(isErrorMes.isError()){
                throw new RenameInMapException(isErrorMes);
            }
            //向服务器发送创建客户端的请求
            objOut.writeObject(new CreatClientMessage(username, ServerName, username, "CreatClientMessage"));
            return username;
        }
        public void uploadUser(String username) throws IOException, ClassNotFoundException {
            objOut.writeObject(new GetUserMessage(username,ServerName,username,"GetUserMessage"));
            //获取服务器端的用户信息
            ReturnUserMessage mes = (ReturnUserMessage) objIn.readObject();
//            System.out.println("age:"+mes.getUser().getAge());
//            System.out.println("username:"+mes.getUser().getUsername());
            //将本地用户的各项信息与服务器数据库同步
            userClient.setUserClient(mes.getUser());
            //为本地客户端添加username
            setUsername(username);
            //设置本地用户为在线状态
            userClient.setOnline(true);
        }
    }
    class CommandGetFriendsClient implements Command{

        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                objOut.writeObject(message);

                ReturnFriendsMessage rMes = (ReturnFriendsMessage) objIn.readObject();
                System.out.println("已接收到服务器的message");
                setFriends(rMes.getFriends());
            } catch (IOException | ClassNotFoundException e) {
                // throw new RuntimeException(e);
                e.printStackTrace();
            }
        }
    }
    class CommandCreatUserClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                CheckNameMessage cMes = new CheckNameMessage(message.getNameSender(),ServerName,message.getNameSender(),"CheckNameMessage");
                objOut.writeObject(cMes);
                isErrorMessage eMes = (isErrorMessage) objIn.readObject();
                if(eMes.isError()){
                    throw new RenameInMapException(eMes);
                }else{
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                            "注册成功","成功",JOptionPane.INFORMATION_MESSAGE);
                }
                objOut.writeObject(message);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (RenameInMapException e) {
                showErrorDialog(e.isErrorMes.getMessage());
            }

        }
        public void showErrorDialog(String errorMessage) {
            JOptionPane.showMessageDialog(
                    Frame.getFrames()[0], // Get the current active frame to center the dialog
                    errorMessage, // Error message
                    "错误", // Dialog title
                    JOptionPane.ERROR_MESSAGE // Message type
            );
        }
    }
    class CommandGetRequestsClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                objOut.writeObject(message);
                System.out.println("向服务器发送获取通知列表的请求");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class CommandReturnRequestsClient implements Command{

        @Override
        public void execute(ClientConnection client, Message message) {
            ReturnRequestsMessage rMes = (ReturnRequestsMessage) message;
            System.out.println("接收到服务器传入的请求列表:");
            System.out.println(rMes.getFriendRequests());
            synchronized (lock){
                friendRequests = rMes.getFriendRequests();
                receiveFriendRequest = true;
                lock.notify();
            }
        }
    }
    class CommandUpdateRequestClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                objOut.writeObject(message);
                System.out.println("已向服务器发送更新请求的信息");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class CommandReturnSearchFriendClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            ReturnUserMessage rMes = (ReturnUserMessage) message;
            synchronized (lock){
                SearchFriendFrame.setFriend((SearchFriend) (rMes.getUser()));
                SearchFriendFrame.setHaveFound(true);
                lock.notify();
            }
        }
    }
    class CommandAddRequestClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                objOut.writeObject(message);
                System.out.println("已向服务器发送添加好友请求");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class CommandGetSearchFriendClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                System.out.println("已向服务器发送搜索请求");
                objOut.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class CommandExecutorClient{
        private HashMap<String,Command> commandMap = new HashMap<>();
        public CommandExecutorClient(){
            commandMap.put("SendChatMessage",new CommandSendChatClient());
            commandMap.put("ReadChatMessage",new CommandReadChatClient());
            commandMap.put("CreatUserMessage",new CommandCreatUserClient());
            commandMap.put("LoginMessage", new CommandLoginClient());
            commandMap.put("GetFriendsMessage",new CommandGetFriendsClient());
            commandMap.put("StatusUpdateMessage",new CommandStatusUpdateClient());
            commandMap.put("UnreadUpdateMessage",new CommandUnreadUpdateClient() );
            commandMap.put("isErrorMessage",new CommandisErrorClient());
            commandMap.put("GetRequestsMessage",new CommandGetRequestsClient());
            commandMap.put("ReturnRequestsMessage", new CommandReturnRequestsClient());
            commandMap.put("UpdateRequestMessage", new CommandUpdateRequestClient());
            commandMap.put("ReturnUserMessage", new CommandReturnSearchFriendClient());
            commandMap.put("AddRequestMessage", new CommandAddRequestClient());
            commandMap.put("GetSearchFriendMessage", new CommandGetSearchFriendClient());
        }
        public void executeCommand(Message mes, ClientConnection client){
            Command command = commandMap.get(mes.getType());
            System.out.println(mes.getType());
            command.execute(client,mes);
        }
    }
}
class UserClient extends User{
    private boolean isOnline;
    private List<FriendUser> friends;
    public UserClient(){
        super();
        isOnline = false;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setUserClient(User user) {
        this.uid = user.getUid();
        this.Sex = user.getSex();
        this.Email = user.getEmail();
        this.Password = user.getPassword();
        this.PhoneNumber = user.getPhoneNumber();
        this.Username = user.getUsername();
//            System.out.println("getUsername:"+Username);
        this.Age = user.getAge();
    }

    public void setFriends(List<FriendUser> friends) {
        this.friends = friends;
    }
    public int getTotalHistoryPages(String friendName,int pageSize){
        FriendUser friend = getFriend(friendName);
        int Pages = 0;
        if (friend != null) {
            Pages = friend.getTotalHistoryPages(pageSize);
        }
        return Pages;
    }
    public List<HistoryChatMessage> getChatHistory(int currentPage, int pageSize ,String friendName){
        sortChatHistory(friendName);
        return getFriend(friendName).getChatHistory(currentPage,pageSize);
    }
    public void sortChatHistory(String friendName){
        getFriend(friendName).sortChatHistory();
    }
    public void addChatHistory(HistoryChatMessage hMes,String friendName){
        FriendUser friend = getFriend(friendName);
        friend.addChatHistory(hMes);
        friend.sortChatHistory();
    }
    public List<FriendUser> getFriends() {
        return friends;
    }
    public FriendUser getFriend(String friendName){
        FriendUser friend = null;
        for(FriendUser f: friends){
            if(f.getUsername().equals(friendName)){
                friend = f;
                break;
            }
        }
        return friend;
    }
}
