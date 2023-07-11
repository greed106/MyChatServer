import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

public class ChatClient extends Connection {


    public ChatClient(Socket socket, PrintWriter writer, Scanner scanner, String uid,
                      Scanner scannerUser, PrintWriter writerUser, ObjectOutputStream objOutServer,
                      ObjectInputStream objInServer) {
        super(socket, writer, scanner, uid,objOutServer, objInServer);
    }

    //客户端,启动！
    public static void main(String[] args){
        try {
            //确定IP地址和端口
            String IPAddress = "10.21.178.175";
            int Port = 1234;
            String ServerName = "服务器";
            Socket socketUser = new Socket(IPAddress,Port);
            System.out.println("已连接到服务器");


            //进行用户的初始化
            Scanner scannerUser = new Scanner(System.in);
            PrintWriter writerUser = new PrintWriter(System.out);
            ObjectOutputStream objOutServer = new ObjectOutputStream(socketUser.getOutputStream());
            ObjectInputStream objInServer = new ObjectInputStream(socketUser.getInputStream());


            String uid;
            while(true){
                System.out.println("请输入您的uid");
                uid = scannerUser.nextLine();
                objOutServer.writeObject(new CheckUIDMessage(uid,ServerName,uid,"CheckUIDMessage"));

                Object obj = objInServer.readObject();
                isErrorMessage isErrorMes = (isErrorMessage)obj;
                if(isErrorMes.isError()){
                    System.out.println(isErrorMes);
                    continue;
                }
                break;
            }
            objOutServer.writeObject(new CreatClientMessage(uid,ServerName,uid,"CreatClientMessage"));
            ChatClient client = new ChatClient(socketUser,writerUser,scannerUser,uid,scannerUser,writerUser,objOutServer,objInServer);
            System.out.println("注册成功,欢迎进入聊天室");
            client.startClient();



        } catch (IOException e) {
            System.out.println("发生了建立clientSock异常");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
    public void startClient(){
        //处理服务器传入的信息
        Thread tServerReader = new Thread(new ServerReader());
        tServerReader.start();

        //处理用户输入
        Thread tUserInputReader = new Thread(new UserInputReader());
        tUserInputReader.start();
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
                    if (obj instanceof ErrorMessage) {
                        mes = (ErrorMessage) obj;
                    }else if (obj instanceof SendChatMessage) {
                        mes = (SendChatMessage) obj;
                    }else if(obj instanceof ReadChatMessage){
                        mes = (ReadChatMessage) obj;
                    }

                    if(mes != null){
                        executor.executeCommand(mes,ChatClient.this);
                    }
                }
            }catch (EOFException | SocketException e){
                System.out.println("与服务器的连接已断开");
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }




        }
    }

    //用于处理用户输入的类
    class UserInputReader implements Runnable{
        //读入用户输入后的业务逻辑
        @Override
        public void run() {
            CommandExecutorClient executor = new CommandExecutorClient();
            while(!socket.isClosed()){
                String message = scanner.nextLine();
                Message mes = null;



                //判断消息的类型
                if(message.equals("EXIT")){
                    mes = new ExitMessage(uid,uid,"EXIT","ExitMessage");
                }else if(message.contains("#")){
                    mes = TransformInputToSendChatMessage("SendChatMessage",message);
                }

                if (mes != null) {
                    executor.executeCommand(mes,ChatClient.this);
                }else{
                    System.out.println("无效的命令");
                }
            }
            System.out.println("客户端已退出,欢迎您下次使用");
        }

    }
    class CommandExitClient implements Command{
        @Override
        public void execute(Connection client,Message message) {
            try {
                client.objOut.writeObject(message);
                Thread.sleep(1000);
                client.socket.close();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    class CommandSendChatClient implements Command{
        @Override
        public void execute(Connection client,Message message) {
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
        public void execute(Connection client, Message message) {
            System.out.println(message);
        }
    }
    class CommandPrintErrorClient implements Command{
        @Override
        public void execute(Connection client, Message message) {
            System.out.println(message);
        }
    }
    class CommandExecutorClient{
        private HashMap<String,Command> commandMap = new HashMap<>();
        public CommandExecutorClient(){
            commandMap.put("ExitMessage",new CommandExitClient());
            commandMap.put("SendChatMessage",new CommandSendChatClient());
            commandMap.put("ReadChatMessage",new CommandReadChatClient());
            commandMap.put("ErrorMessage",new CommandPrintErrorClient());
        }
        public void executeCommand(Message mes, Connection client){
            Command command = commandMap.get(mes.getType());
            command.execute(client,mes);
        }
    }


}
