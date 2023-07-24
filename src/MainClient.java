public class MainClient {
    //登录窗口
    LoginFrame loginFrame;
    public static void main(String[] args){
        MainClient client = new MainClient();
        client.start();
    }
    public MainClient(){
        loginFrame = new LoginFrame();
    }
    public void start(){
        loginFrame.setVisible(true);
    }
}
