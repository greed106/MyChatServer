
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ChatClient client;
    private UserInterfaceFrame userInterfaceFrame;

    public LoginFrame() {

        //初始化客户端的连接
        initialClient();


        setTitle("登录"); // 设置窗口的标题为"登录"
        setSize(300, 150); // 设置窗口的大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭时的操作：程序终止
        setLayout(new BorderLayout()); // 设置布局为 BorderLayout

        JPanel panel = creatPanelNorth();
        // 将面板添加到窗口的中间区域
        add(panel, BorderLayout.CENTER);


        JPanel buttonPanel = creatPanelSouth();
        // 将按钮面板添加到窗口的下方区域
        add(buttonPanel, BorderLayout.SOUTH);

         // 让Frame显示在屏幕正中央
        setFrameCenter(this);
    }

    public static void main(String[] args) {
        LoginFrame loginFrame = new LoginFrame(); // 创建LoginFrame对象并设置为可见
        loginFrame.setVisible(true);
    }
    public void setFrameCenter(JFrame frame){
        // 获取屏幕的尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // 计算窗口的位置
        int windowWidth = frame.getWidth();
        int windowHeight = frame.getHeight();
        int x = (screenWidth - windowWidth) / 2;
        int y = (screenHeight - windowHeight) / 2;

        // 设置窗口的位置
        frame.setLocation(x, y);
    }
    public JButton creatRegisterButton(){
        JButton registerButton = new JButton("注册");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistrationFrame registerFrame = new RegistrationFrame(client);
                setFrameCenter(registerFrame);
                registerFrame.setVisible(true);
                // 这里添加注册账号的业务逻辑
                System.out.println("点击了注册按钮");
            }
        });
        return registerButton;
    }
    public JButton creatLoginButton(){
        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                //创建登陆消息
                Message mes = new LoginMessage(username,client.getServerName(),password,"LoginMessage");
                //执行登陆消息
                client.executeCommandClient(mes);
                //判断是否登陆成功，并输出提示信息
                if(CheckLogin()){
                    // System.out.println("执行了if");
                    OpenUserInterfaceFrame();
                }
                System.out.println("用户名: " + username);
                System.out.println("密码: " + password);
            }
        });
        return loginButton;
    }
    private JPanel creatPanelNorth(){
        // 创建面板并设置布局为GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 创建用户名标签并添加到面板
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        panel.add(usernameLabel, gbc);

        // 创建用户名输入框并添加到面板
        usernameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        panel.add(usernameField, gbc);

        // 创建密码标签并添加到面板
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panel.add(passwordLabel, gbc);

        // 创建密码输入框并添加到面板
        passwordField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        panel.add(passwordField, gbc);

        return panel;
    }
    private JPanel creatPanelSouth() {
        // 创建登录按钮
        JButton loginButton = creatLoginButton();
        // 创建注册按钮
        JButton registerButton = creatRegisterButton();

        // 创建一个面板用于放置按钮
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        return buttonPanel;
    }
    private void initialClient() {
        //确定IP地址和端口
        String IPAddress = "10.21.178.175";
        int Port = 1234;
        Socket socketUser = null;
        try {
            socketUser = new Socket(IPAddress,Port);
            client = new ChatClient(socketUser);
        } catch (IOException e) {
            System.out.println("发生了建立clientSock异常");
            e.printStackTrace();
        }

    }
    private boolean CheckLogin(){
        boolean haveLogin = true;
        if(client.isOnline()){
            JOptionPane.showMessageDialog(null, "登录成功",
                    "信息", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }else{
            JOptionPane.showMessageDialog(null, "登录失败\n请检查您的用户名或密码是否正确",
                    "信息", JOptionPane.INFORMATION_MESSAGE);
            haveLogin = false;
        }
        return haveLogin;
    }
    private void OpenUserInterfaceFrame(){
        Message mes = new GetFriendsMessage(client.getUsername(),client.getServerName(),
                client.getUsername(),"GetFriendsMessage");

        client.executeCommandClient(mes);
        System.out.println("执行了函数");
        userInterfaceFrame = new UserInterfaceFrame(client);
        userInterfaceFrame.setVisible(true);
        client.setUserInterfaceFrame(userInterfaceFrame);
        client.openServerReader();
    }


}


