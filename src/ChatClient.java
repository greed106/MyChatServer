import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.List;

public class ChatClient extends ClientConnection {
    /**
     * 用户客户端
     */
    private UserClient userClient;

    /**
     * 命令执行客户端
     */
    private CommandExecutorClient executorClient;

    /**
     * 聊天界面映射
     */
    private HashMap<String, ChatFrame> chatFrames;

    /**
     * 服务器名称
     */
    private String ServerName = "服务器";

    /**
     * 历史记录页面大小
     */
    static final int HistoryPageSize = 6;

    /**
     * 好友请求列表
     */
    private List<FriendRequest> friendRequests;

    /**
     * 锁对象
     */
    private final Object lock;

    /**
     * 是否接收到好友请求标志
     */
    private boolean receiveFriendRequest = false;

    /**
     * 用户界面框架
     */
    UserInterfaceFrame userInterfaceFrame;

    /**
     * 获取用户客户端
     * @return 用户客户端
     */
    public UserClient getUserClient() {
        return userClient;
    }

    /**
     * 获取是否接收到好友请求标志
     * @return 是否接收到好友请求标志
     */
    public boolean isReceiveFriendRequest() {
        return receiveFriendRequest;
    }

    /**
     * 设置是否接收到好友请求标志
     * @param receiveFriendRequest 是否接收到好友请求标志
     */
    public void setReceiveFriendRequest(boolean receiveFriendRequest) {
        this.receiveFriendRequest = receiveFriendRequest;
    }

    /**
     * 设置用户界面框架
     * @param userInterfaceFrame 用户界面框架
     */
    public void setUserInterfaceFrame(UserInterfaceFrame userInterfaceFrame) {
        this.userInterfaceFrame = userInterfaceFrame;
    }

    /**
     * 构造函数,初始化客户端
     * @param socketUser 套接字
     * @throws IOException IO异常
     */
    public ChatClient (Socket socketUser) throws IOException {
        //进行客户端的初始化,建立输入输出流
        super(socketUser,new PrintWriter(System.out),new Scanner(System.in), new ObjectOutputStream(socketUser.getOutputStream()), new ObjectInputStream(socketUser.getInputStream()));

        userClient = new UserClient();
        executorClient = new CommandExecutorClient();
        chatFrames = new HashMap<>();
        friendRequests = new ArrayList<>();
        lock = new Object();
    }

    /**
     * 添加聊天界面
     * @param chatFrame 聊天界面
     * @param friendName 好友名称
     */
    public void addChatFrame(ChatFrame chatFrame,String friendName){
        chatFrames.put(friendName,chatFrame);
    }

    /**
     * 打开聊天界面
     * @param friendName 好友名称
     * @return 聊天界面
     */
    public ChatFrame openFrame(String friendName){
        ChatFrame chatFrame = chatFrames.get(friendName);
        if(chatFrame == null){
            System.out.println("friendname:"+friendName+"对应frame不存在");
            chatFrame = new ChatFrame(friendName,this);
            addChatFrame(chatFrame,friendName);
        }
        return chatFrame;
    }

    /**
     * 移除聊天界面
     * @param friendName 好友名称
     */
    public void removeFrame(String friendName){
        chatFrames.remove(friendName);
    }

    /**
     * 打开服务器读取线程
     */
    public void openServerReader(){
        Thread tServerReader = new Thread(new ServerReader());
        tServerReader.start();
    }

    /**
     * 获取某好友聊天历史总页数
     * @param friendName 好友名称
     * @return 总页数
     */
    public int getTotalHistoryPages(String friendName){
        return userClient.getTotalHistoryPages(friendName,HistoryPageSize);
    }

    /**
     * 获取某页聊天历史
     * @param currentPage 当前页码
     * @param friendName 好友名称
     * @return 聊天历史消息列表
     */
    public List<HistoryChatMessage> getChatHistory(int currentPage, String friendName){
        return userClient.getChatHistory(currentPage,HistoryPageSize,friendName);
    }

    /**
     * 添加一条聊天历史
     * @param hMes 聊天历史消息
     * @param friendName 好友名称
     */
    public void addChatHistory(HistoryChatMessage hMes,String friendName){
        userClient.addChatHistory(hMes,friendName);
    }

    /**
     * 获取服务器名称
     * @return 服务器名称
     */
    public String getServerName() {
        return ServerName;
    }

    /**
     * 判断是否在线
     * @return 是否在线
     */
    public boolean isOnline(){
        return userClient.isOnline();
    }

    /**
     * 设置好友列表
     * @param friends 好友列表
     */
    public void setFriends(List<FriendUser> friends){
        userClient.setFriends(friends);
    }

    /**
     * 获取好友列表
     * @return 好友列表
     */
    public List<FriendUser> getFriends(){
        return userClient.getFriends();
    }

    /**
     * 获取某个好友
     * @param friendName 好友名称
     * @return 好友
     */
    public FriendUser getFriend(String friendName){
        return userClient.getFriend(friendName);
    }

    /**
     * 获取好友请求列表
     * @return 好友请求列表
     */
    public List<FriendRequest> getFriendRequests(){
        return friendRequests;
    }

    /**
     * 执行命令客户端
     * @param mes 消息
     */
    public void executeCommandClient(Message mes){
        executorClient.executeCommand(mes,this);
    }

    /**
     * 获取锁对象
     * @return 锁对象
     */
    public Object getLock() {
        return lock;
    }

    /**
     * 用于处理服务器传入消息的类
     */
    class ServerReader implements Runnable{

        /**
         * 读入服务器传入消息的业务逻辑
         */
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

    /**
     * 发送聊天消息命令
     */
    class CommandSendChatClient implements Command{

        /**
         * 发送聊天消息
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 读取聊天消息命令
     */
    class CommandReadChatClient implements Command{

        /**
         * 读取聊天消息
         * @param client 客户端连接
         * @param message 消息
         */
        @Override
        public void execute(ClientConnection client, Message message) {
            System.out.println(message);
            ChatFrame chatFrame = openFrame(message.getNameSender());
            chatFrame.addMessage((ReadChatMessage) message);
            chatFrame.setVisible(true);
        }
    }

    /**
     * 未读消息更新命令
     */
    class CommandUnreadUpdateClient implements Command{

        /**
         * 未读消息更新
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 错误消息命令
     */
    class CommandisErrorClient implements Command{

        /**
         * 显示错误对话框
         * @param client 客户端连接
         * @param message 消息
         */
        @Override
        public void execute(ClientConnection client, Message message) {
            showErrorDialog(message.getMessage());
        }

        /**
         * 显示错误对话框
         * @param errorMessage 错误信息
         */
        public void showErrorDialog(String errorMessage) {
            JOptionPane.showMessageDialog(
                    Frame.getFrames()[0], // 获取当前活动框架以中心化对话框
                    errorMessage, // 错误信息
                    "错误", // 对话框标题
                    JOptionPane.ERROR_MESSAGE // 消息类型
            );
        }
    }

    /**
     * 在线状态更新命令
     */
    class CommandStatusUpdateClient implements Command{

        /**
         * 在线状态更新
         * @param client 客户端连接
         * @param message 消息
         */
        @Override
        public void execute(ClientConnection client, Message message) {
            StatusUpdateMessage uMes = (StatusUpdateMessage) message;
            userInterfaceFrame.handleFriendStatusUpdate(uMes.getMessage(),uMes.isOnline() );
        }
    }

    /**
     * 登录命令
     */
    class CommandLoginClient implements Command{

        /**
         * 登录
         * @param client 客户端连接
         * @param message 消息
         */
        @Override
        public void execute(ClientConnection client, Message message) {
            try {
                uploadUser(checkUser(message));
                objOut.writeObject(new StatusUpdateMessage(username,ServerName,username,
                        "StatusUpdateMessage",true));
                System.out.println("登陆成功!");
            } catch (IOException | ClassNotFoundException | RenameInMapException e) {
                e.printStackTrace();
            }
        }

        /**
         * 检查用户信息
         * @param mes 消息
         * @return 用户名
         * @throws RenameInMapException 异常
         * @throws IOException IO异常
         * @throws ClassNotFoundException 类找不到异常
         */
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

        /**
         * 上传用户信息
         * @param username 用户名
         * @throws IOException IO异常
         * @throws ClassNotFoundException 类找不到异常
         */
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

    /**
     * 获取好友列表命令
     */
    class CommandGetFriendsClient implements Command{

        /**
         * 获取好友列表
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 创建用户命令
     */
    class CommandCreatUserClient implements Command{

        /**
         * 创建用户
         * @param client 客户端连接
         * @param message 消息
         */
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

        /**
         * 显示错误对话框
         * @param errorMessage 错误信息
         */
        public void showErrorDialog(String errorMessage) {
            JOptionPane.showMessageDialog(
                    Frame.getFrames()[0], // 获取当前活动框架以中心化对话框
                    errorMessage, // 错误信息
                    "错误", // 对话框标题
                    JOptionPane.ERROR_MESSAGE // 消息类型
            );
        }
    }

    /**
     * 获取好友请求列表命令
     */
    class CommandGetRequestsClient implements Command{

        /**
         * 获取好友请求列表
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 返回好友请求列表命令
     */
    class CommandReturnRequestsClient implements Command{

        /**
         * 返回好友请求列表
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 更新好友请求命令
     */
    class CommandUpdateRequestClient implements Command{

        /**
         * 更新好友请求
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 返回搜索好友命令
     */
    class CommandReturnSearchFriendClient implements Command{

        /**
         * 返回搜索好友结果
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 添加好友请求命令
     */
    class CommandAddRequestClient implements Command{

        /**
         * 添加好友请求
         * @param client 客户端连接
         * @param message 消息
         */
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

    /**
     * 搜索好友命令
     */
    class CommandGetSearchFriendClient implements Command{

        /**
         * 搜索好友
         * @param client 客户端连接
         * @param message 消息
         */
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
    /**
     * 命令执行客户端类，用于执行接收到的不同类型的消息。
     */
    class CommandExecutorClient {
        private HashMap<String, Command> commandMap = new HashMap<>();

        /**
         * 创建CommandExecutorClient实例，并设置不同类型消息对应的命令。
         */
        public CommandExecutorClient() {
            commandMap.put("SendChatMessage", new CommandSendChatClient());
            commandMap.put("ReadChatMessage", new CommandReadChatClient());
            commandMap.put("CreatUserMessage", new CommandCreatUserClient());
            commandMap.put("LoginMessage", new CommandLoginClient());
            commandMap.put("GetFriendsMessage", new CommandGetFriendsClient());
            commandMap.put("StatusUpdateMessage", new CommandStatusUpdateClient());
            commandMap.put("UnreadUpdateMessage", new CommandUnreadUpdateClient());
            commandMap.put("isErrorMessage", new CommandisErrorClient());
            commandMap.put("GetRequestsMessage", new CommandGetRequestsClient());
            commandMap.put("ReturnRequestsMessage", new CommandReturnRequestsClient());
            commandMap.put("UpdateRequestMessage", new CommandUpdateRequestClient());
            commandMap.put("ReturnUserMessage", new CommandReturnSearchFriendClient());
            commandMap.put("AddRequestMessage", new CommandAddRequestClient());
            commandMap.put("GetSearchFriendMessage", new CommandGetSearchFriendClient());
        }

        /**
         * 执行相应类型的命令。
         *
         * @param mes    消息对象。
         * @param client 客户端连接对象。
         */
        public void executeCommand(Message mes, ClientConnection client) {
            Command command = commandMap.get(mes.getType());
            System.out.println(mes.getType());
            command.execute(client, mes);
        }
    }
}