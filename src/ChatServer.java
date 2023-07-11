
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Scanner;

public class ChatServer{
    //服务器的端口号
    private static final int Port = 1234;
    //储存客户端信息的映射
    private HashMap<String,Connection> clientsMap;
    //服务器端使用的套接字
    private ServerSocket serverSock;
    private String ServerName = "服务器";
    //主函数
    public static void main(String[] args){
        ChatServer chatServer = new ChatServer();
        chatServer.startServer();
    }
    //服务器,启动！
    public void startServer(){
        //创建储存客户端连接的映射
        clientsMap = new HashMap<>();
        try{
            //创建服务器使用的套接字
            serverSock = new ServerSocket(Port);
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
                Connection client = new Connection(clientSock,writer,scanner,objOut,objIn);

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

    //创建内部类Handler,实现Runnable接口
    //并完成客户端具体操作的业务逻辑
    class Handler implements Runnable{
        //被控制的对应的客户端信息
        Connection client;
        public Handler(Connection client) {
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
                    if (obj instanceof CheckUIDMessage)
                        mes = (CheckUIDMessage) obj;
                    if (obj instanceof ExitMessage)
                        mes = (ExitMessage) obj;
                    if (obj instanceof CreatClientMessage)
                        mes = (CreatClientMessage) obj;
                    if (obj instanceof SendChatMessage)
                        mes = (SendChatMessage) obj;
                    if(mes != null)
                        executor.executeCommand(mes, client);
                }
            }catch (SocketException e) {
                clientsMap.remove(client.returnUID());
                System.out.println("uid: "+client.returnUID()+" 对应Socket连接已关闭");
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }
    class CommandExitAClientServer implements Command{
        @Override
        public void execute(Connection client,Message message) {
            try {
                System.out.println("用户uid: " + client.returnUID() + " 正常下线");
                clientsMap.remove(client.returnUID());
                System.out.println("当前连接数："+clientsMap.size());
                client.socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class CommandCheckUIDServer implements Command{

        @Override
        public void execute(Connection client, Message message) {
            try {
                System.out.println("收到CheckUID请求");
                //如果用户已存在的话，uid不合法
                if(clientsMap.get(message.getMessage()) != null){
                    //抛出异常
                    throw new RenameInMapException();
                }
                //用户名uid合法
                Message mes = new isErrorMessage(ChatServer.this.ServerName,"","您输入的uid合法","isErrorMessage",false);
                client.objOut.writeObject(mes);
            }catch (IOException e) {
                throw new RuntimeException(e);
            } catch (RenameInMapException e) {
                try {
                    System.out.println("发生了用户重命名的异常,重命名的对应用户为："+message.getMessage());
                    Message mes = new isErrorMessage(ChatServer.this.ServerName,"","您输入的uid已存在","isErrorMessage",true);
                    client.objOut.writeObject(mes);
                    e.printStackTrace();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                //throw new RuntimeException(e);
            }
        }
    }
    class CommandCreatClientServer implements Command{
        @Override
        public void execute(Connection client, Message message) {
            client.setUid(message.getMessage());
            clientsMap.put(client.returnUID(), client);
            System.out.println("用户uid: " + client.returnUID() + " 连接到服务器");
            System.out.println("当前连接数：" + clientsMap.size());
        }
    }
    class CommandChatServer implements Command{

        @Override
        public void execute(Connection client, Message message) {
            try {
                Connection recevier = ChatServer.this.clientsMap.get(message.getUidReceiver());
                if(recevier == null){
                    throw new NotFoundinMapException();
                }
                System.out.println("接收到来自 "+message.getUidSender()+" 的消息 "+message.getMessage()
                        +" 将发送给 "+message.uidReceiver);
                recevier.objOut.writeObject(new ReadChatMessage((SendChatMessage) message));

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NotFoundinMapException e) {
                try {
                    System.out.println("发生了未找到对应用户的异常，要寻找的用户为："+message.getUidReceiver());
                    client.objOut.writeObject(new ErrorMessage(ChatServer.this.ServerName,
                            client.returnUID(),"未找到您输入的用户uid","ErrorMessage"));
                    e.printStackTrace();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                //throw new RuntimeException(e);
            }
        }
    }
    class CommandExecutorServer{
        private HashMap<String,Command> commandMap = new HashMap<>();
        public CommandExecutorServer(){
            commandMap.put("ExitMessage",new CommandExitAClientServer());
            commandMap.put("CheckUIDMessage",new CommandCheckUIDServer());
            commandMap.put("CreatClientMessage",new CommandCreatClientServer());
            commandMap.put("SendChatMessage",new CommandChatServer());
        }
        public void executeCommand(Message message,Connection client){
            try{
                Command command = commandMap.get(message.getType());
                command.execute(client,message);
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}








class Connection{
    protected Socket socket;
    protected PrintWriter writer;
    protected Scanner scanner;
    protected String uid;
    protected ObjectOutput objOut;
    protected ObjectInput objIn;


    public Connection(Socket socket, PrintWriter writer, Scanner scanner, String uid, ObjectOutputStream objOut, ObjectInputStream objIn) {
        this.socket = socket;
        this.writer = writer;
        this.scanner = scanner;
        this.uid = uid;
        this.objOut = objOut;
        this.objIn = objIn;
    }

    public Connection(Socket socket, PrintWriter writer, Scanner scanner, ObjectOutput objOut, ObjectInput objIn) {
        this.socket = socket;
        this.writer = writer;
        this.scanner = scanner;
        this.objOut = objOut;
        this.objIn = objIn;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Message TransformInputToSendChatMessage(String type, String input){
        String[] parts = input.split("#", 2);
        String uidReceiver = parts[0];
        String messageRec = parts.length > 1 ? parts[1] : "";
        Message mes = null;
        if(type.equals("SendChatMessage"))
            mes = new SendChatMessage(this.uid,uidReceiver,messageRec,type,LocalDateTime.now());
        return mes;
    }

    public boolean isConnected(){
        return socket.isConnected();
    }

    public String returnUID(){
        return uid;
    }

}