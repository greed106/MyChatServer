import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

public class ChatClient extends ClientConnection {
    UserClient userClient;
    String ServerName = "服务器";
    public void clearScreen(){
        System.out.print("\033[H\033[2J");
    }
    public ChatClient (Socket socketUser) throws IOException {
        //进行客户端的初始化,建立输入输出流
        super(socketUser,new PrintWriter(System.out),new Scanner(System.in),
                new ObjectOutputStream(socketUser.getOutputStream()),
                new ObjectInputStream(socketUser.getInputStream()));
        userClient = new UserClient();
    }
    //客户端,启动！
    public static void main(String[] args){
        try {
            //确定IP地址和端口
            String IPAddress = "192.168.193.128";
            int Port = 1234;

            Socket socketUser = new Socket(IPAddress,Port);
            System.out.println("已连接到服务器");
            //建立客户端和服务器的连接和输入输出流
            ChatClient client = new ChatClient(socketUser);
            client.startClient();



        } catch (IOException e) {
            System.out.println("发生了建立clientSock异常");
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
                if(message.equals("SIGNUP") && !userClient.isOnline()){
                    mes = new SignupMessage(username,username,"SIGNUP","SignupMessage");
                }else if(message.equals("LOGIN") && !userClient.isOnline()){
                    mes = new LoginMessage(username,username,"LOGIN","LoginMessage");
                } else if(message.equals("EXIT") && userClient.isOnline()){
                    mes = new ExitMessage(username, username,"EXIT","ExitMessage");
                }else if(message.contains("#") && userClient.isOnline()){
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
        public void execute(ClientConnection client, Message message) {
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
        }
    }
    class CommandPrintErrorClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            System.out.println(message);
        }
    }
    class CommandSignupClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            while(true) {
                try {
                    creatUsername();
                    creatPassword();
                    objOut.writeObject(new CreatUserMessage(username,ServerName,userClient.Password,"CreatUserMessage"));
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (RenameInMapException e) {
                    System.out.println(e.isErrorMes);
                }
            }
        }
        public void creatPassword(){
            String password;
            while(true){
                System.out.println("请输入您的password");
                password = scanner.nextLine();
                System.out.println("请重复输入确认密码");
                if(password.equals(scanner.nextLine())){
                    break;
                }else{
                    System.out.println("两次输入的密码不一致，请重试");
                }
            }
            userClient.setPassword(password);
        }
        public void creatUsername() throws IOException, ClassNotFoundException, RenameInMapException {
            System.out.println("请输入您的username");
            username = scanner.nextLine();
            //检查username的合法性
            objOut.writeObject(new CheckNameMessage(username, ServerName, username, "CheckNameMessage"));
            //读取服务器返回的检查信息
            Object obj = objIn.readObject();
            isErrorMessage isErrorMes = (isErrorMessage) obj;
            //如果不合法则抛出异常
            if (isErrorMes.isError()) {
                throw new RenameInMapException(isErrorMes);
            }
            //向服务器发送创建客户端的请求
            objOut.writeObject(new CreatClientMessage(username, ServerName, username, "CreatClientMessage"));
            //为user添加username
            userClient.setUsername(username);
            //为本地客户端添加username
            setUsername(username);
        }
    }
    class CommandLoginClient implements Command{
        @Override
        public void execute(ClientConnection client, Message message) {
            while(true){
                try {
                    uploadUser(checkUser());
                    System.out.println("登陆成功！");
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    //throw new RuntimeException(e);
                    e.printStackTrace();
                } catch (RenameInMapException e) {
                    System.out.println(e.isErrorMes);
                }
            }
        }
        public String checkUser() throws RenameInMapException, IOException, ClassNotFoundException {
            System.out.println("请输入您的username");
            String username = scanner.nextLine();
            System.out.println("请输入您的password");
            String password = scanner.nextLine();
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
            //将本地用户的各项信息与服务器数据库同步
            userClient.setUserClient(mes.getUser());
            //为本地客户端添加username
            setUsername(username);
            //设置本地用户为在线状态
            userClient.setOnline(true);
        }
    }
    class CommandExecutorClient{
        private HashMap<String,Command> commandMap = new HashMap<>();
        public CommandExecutorClient(){
            commandMap.put("ExitMessage",new CommandExitClient());
            commandMap.put("SendChatMessage",new CommandSendChatClient());
            commandMap.put("ReadChatMessage",new CommandReadChatClient());
            commandMap.put("ErrorMessage",new CommandPrintErrorClient());
            commandMap.put("SignupMessage",new CommandSignupClient());
            commandMap.put("LoginMessage", new CommandLoginClient());
        }
        public void executeCommand(Message mes, ClientConnection client){
            Command command = commandMap.get(mes.getType());
            command.execute(client,mes);
        }
    }
    class UserClient extends User{
        boolean isOnline;
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
            this.Username = getUsername();
        }
    }


}
