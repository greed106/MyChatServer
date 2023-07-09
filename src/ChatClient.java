import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient extends Connection {
    private Scanner scannerUser;
    private PrintWriter writerUser;

    public ChatClient(Socket socket, PrintWriter writer, Scanner scanner, String uid,
                      Scanner scannerUser, PrintWriter writerUser, ObjectOutputStream objOutServer,
                      ObjectInputStream objInServer) {
        super(socket, writer, scanner, uid,objOutServer, objInServer);
        this.scannerUser = scannerUser;
        this.writerUser = writerUser;
    }

    //客户端,启动！
    public static void main(String[] args){
        try {
            //确定IP地址和端口
            String IPAddress = "10.21.178.175";
            int Port = 1234;

            Socket socketUser = new Socket(IPAddress,Port);
            System.out.println("已连接到服务器");


            //进行用户的初始化
            Scanner scannerServer = new Scanner(socketUser.getInputStream());
            PrintWriter writerServer = new PrintWriter(socketUser.getOutputStream());
            ObjectOutputStream objOutServer = new ObjectOutputStream(socketUser.getOutputStream());
            ObjectInputStream objInServer = new ObjectInputStream(socketUser.getInputStream());

            System.out.println("请输入您的uid");
            Scanner scannerUser = new Scanner(System.in);
            PrintWriter writerUser = new PrintWriter(System.out);

            String uid = scannerUser.nextLine();
            writerServer.println(uid);
            writerServer.flush();


            //如果用户名不合法的话,则抛出异常
            if(!scannerServer.nextBoolean()){
                System.out.println(scannerServer.nextLine());
                throw new RenameInMapException();
            }


            ChatClient client = new ChatClient(socketUser,writerServer,scannerServer,uid,scannerUser,writerUser,objOutServer,objInServer);
            System.out.println("注册成功,欢迎进入聊天室");
            client.startClient();



        } catch (IOException e) {
            System.out.println("发生了建立clientSock异常");
            e.printStackTrace();
        } catch (RenameInMapException e) {
            System.out.println("发生了uid重命名异常");
            e.printStackTrace();
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
                while(socket.isConnected()) {
                    obj = objIn.readObject();
                    Message mes = new Message();
                    if (obj instanceof Message)
                        mes = (Message) obj;
                    if (obj instanceof ChatMessage)
                        mes = (ChatMessage) obj;
                    System.out.println(mes);
                }
            }catch (EOFException e){
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
            String message;
            while(!socket.isClosed()){
                message = scannerUser.nextLine();
                //判断是否为退出消息
                if(!isExit(message)){
                    try {
                        objOut.writeObject(readMessage("ChatMessage",message));
                        System.out.println("消息发送成功");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            System.out.println("客户端已退出,欢迎您下次使用");
        }
    }
    private boolean isExit(String message) {
        boolean isEXIT = false;
        if (message.equals("EXIT")) {
            isEXIT = true;
            try {
                objOut.writeObject(readMessage("Message","EXIT#"));
                Thread.sleep(1000);
                socket.close();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return isEXIT;
    }

}
