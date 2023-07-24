import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;

// 聊天窗口的类
public class ChatFrame extends JFrame {
    private JTextArea messageArea; // 用于显示消息的文本区域
    private JTextField inputField; // 用于输入消息的文本框
    private ChatClient client;
    private String friendName;

    // 聊天窗口的构造函数
    public ChatFrame(String friendName,ChatClient client) {
        this.client = client;
        this.friendName = friendName;

        setTitle("与" + friendName + "的对话"); // 设置窗口的标题
        setSize(400, 400); // 设置窗口的大小
        setLayout(new BorderLayout()); // 设置窗口的布局管理器
        //设置窗口关闭后的操作
        setCloseOperation(friendName);

        createMessageArea();
        add(messageArea, BorderLayout.CENTER); // 将消息文本区域添加到窗口的中部

        createInputField(friendName);
        add(inputField, BorderLayout.SOUTH); // 将输入文本框添加到窗口的南部（底部）

        // 创建按钮面板
        JPanel buttonPanel = createPanelEast();
        add(buttonPanel, BorderLayout.EAST); // 将按钮面板添加到窗口的东部（右侧）
        setFrameCenter();
    }
    private JPanel createPanelEast(){
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // 创建查看聊天记录的按钮，并为其添加动作监听器
        JButton viewHistoryButton = createViewHistoryButton();
        buttonPanel.add(viewHistoryButton); // 将查看聊天记录的按钮添加到按钮面板

        // 创建查看详细信息的按钮，并为其添加动作监听器
        JButton viewInfoButton = createViewInfoButton(friendName);
        buttonPanel.add(viewInfoButton); // 将查看详细信息的按钮添加到按钮面板

        return buttonPanel;
    }
    private JButton createViewHistoryButton(){
        JButton viewHistoryButton = new JButton("查看聊天记录");
        viewHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 在这里添加查看聊天记录的逻辑
                new HistoryMessageFrame(client,friendName).setVisible(true);
            }
        });

        return viewHistoryButton;
    }
    private JButton createViewInfoButton(String friendName){
        JButton viewInfoButton = new JButton("查看详细信息");
        viewInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // getFriend 用来获取好友的详细信息
                FriendUser friend = client.getFriend(friendName);
                new ViewInfoFrame(friend).setVisible(true);
            }

        });
        return viewInfoButton;
    }

    public void addMessage(ReadChatMessage mes){
        messageArea.append(mes.toString());
    }
    private void createInputField(String friendName){
        inputField = new JTextField(); // 创建输入文本框
        inputField.setPreferredSize(new Dimension(inputField.getPreferredSize().width, 50)); // 设置输入文本框的预期高度
        // 回车键监听
        inputField.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // 获取输入内容
                    String input = inputField.getText();
                    // 清空输入框
                    inputField.setText("");

                    // 创建消息
                    SendChatMessage sMes = new SendChatMessage(client.getUsername(),friendName,
                            input,"SendChatMessage", LocalDateTime.now());
                    client.executeCommandClient(sMes);

                    //将自己发送的消息加入到聊天框中
                    addMessage(new ReadChatMessage(sMes));

                    //将自己发送的信息加入到历史信息中
                    client.addChatHistory(new HistoryChatMessage(sMes),friendName);
                }
            }
        });
    }
    public void createMessageArea(){
        messageArea = new JTextArea(); // 创建消息文本区域
        messageArea.setEditable(false); // 设置消息文本区域不可编辑
    }
    public void setFrameCenter(){
        // 获取屏幕的尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // 计算窗口的位置
        int frameWidth = getSize().width;
        int frameHeight = getSize().height;
        int x = (screenWidth - frameWidth) / 2;
        int y = (screenHeight - frameHeight) / 2;

        // 设置窗口的位置
        setLocation(x, y);
    }
    public void setCloseOperation(String friendName){
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 设置窗口关闭时的操作
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                // 移除打开的聊天窗口
                client.removeFrame(friendName);
            }
        });
    }

}