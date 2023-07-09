
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
                try {
                    Connection client = creatClient(clientSock);
                    Thread t = new Thread(new Handler(client));
                    t.start();
                    System.out.println("用户uid: " + client.returnUID() + " 连接到服务器");
                    System.out.println("当前连接数：" + clientsMap.size());
                }catch (RenameInMapException e) {
                    System.out.println("发生了客户端重命名的异常");
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            //throw new RuntimeException(e);
            //打印未成功创建服务器使用套接字的异常
            System.out.println("发生了未成功创建服务器使用套接字的异常");
            e.printStackTrace();
        }
    }



    //向uidSender发送消息的方法
    public void sendMessage(Message message)
        throws NotFoundinMapException
    {
        String uidReceiver = message.getUidReceiver();
        Connection Receiver = clientsMap.get(uidReceiver);
        if(Receiver == null){
            throw new NotFoundinMapException();
        }
        Receiver.sendMessage(message);
    }

    public Connection creatClient(Socket clientSock)
            throws RenameInMapException, IOException {

        //为客户端创建所需要的scanner和writer

        //向客户端发送消息的输出流
        PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
        ObjectOutputStream objOut = new ObjectOutputStream(clientSock.getOutputStream());
        //接收客户端消息的输入流
        Scanner scanner = new Scanner(clientSock.getInputStream());
        ObjectInputStream objIn = new ObjectInputStream(clientSock.getInputStream());



//        //提示客户端输入uid
//        writer.println("服务器：请输入您的uid(用户名)");
//        writer.flush();




        //读取客户端传入的uid
        String uid = scanner.nextLine();
        Connection tempClient =clientsMap.get(uid);
        boolean flag = true;
        //若出现uid重复,则抛出异常
        if(tempClient != null){
            String ErrorMessage = "您输入的uid已存在";
            flag = false;
            //Pair<Boolean,String> pair = new Pair<>(flag,ErrorMessage);
            writer.println(flag);
            writer.println(ErrorMessage);
            writer.flush();
            throw new RenameInMapException();
        }
        writer.println(flag);
        writer.flush();

        //System.out.println("提示消息已发送");

        //创建客户端连接信息类
        Connection client = new Connection(clientSock,writer,scanner,uid,objOut,objIn);
        //将client加入map中备用
        clientsMap.put(uid,client);
        return client;
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
            while(client.isConnected()){
                try {
                    Message mesRec = client.readMessage();
                    String uidReceiver = mesRec.getUidReceiver();
                    String messageStr = mesRec.getMessage();
                    //如果得到了EXIT退出指令
                    if (uidReceiver.equals("EXIT")) {
                        System.out.println("用户uid: " + client.returnUID() + " 下线");
                        clientsMap.remove(client.returnUID());
                        client.socket.close();
                        break;
                    } else {
                        //向对应用户发送消息
                        System.out.println("接收到消息: " + messageStr + " 将发送给用户: " + uidReceiver);
                        sendMessage(mesRec);
                    }
                } catch (NotFoundinMapException e) {

                    System.out.println("发生了未找到对应用户的异常");
                    try {
                        //向用户发送错误信息
                        client.objOut.writeObject(new Message(ServerName, client.returnUID(), "未找到您输入的uid"));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    e.printStackTrace();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }



}



//定义在map中未找到对应用户的异常
class NotFoundinMapException extends Exception{
    public NotFoundinMapException(){
        super();
    }
}
//定义在map中已经有重复的uid的异常
class RenameInMapException extends Exception{
    public RenameInMapException(){
        super();
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



    //
    public Message readMessage() throws IOException, ClassNotFoundException {
        Message message = (Message) objIn.readObject();
        return message;
    }

    public Message readMessage(String type,String input){
        String[] parts = input.split("#", 2);
        String uidReceiver = parts[0];
        String messageRec = parts.length > 1 ? parts[1] : "";
        if(type.equals("ChatMessage"))
            return new ChatMessage(this.uid,uidReceiver,messageRec,LocalDateTime.now());
        if(type.equals("Message"))
            return new Message(this.uid,uidReceiver,messageRec);
        return null;
    }



    public boolean isConnected(){
        return socket.isConnected();
    }
    public void sendMessage(Message message) {
        //System.out.println("Sending message: " + message);
        try {
            objOut.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String returnUID(){
        return uid;
    }

}