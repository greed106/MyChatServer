import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInterfaceFrame extends JFrame {
    private JTextField signatureField;
    private ChatClient client;
    private Map<String, JPanel> friendStatusPanels = new HashMap<>();
    private Map<String, JLabel> unreadMessagesLabels = new HashMap<>();


    // 主窗口的构造函数
    public UserInterfaceFrame(ChatClient chatclient) {
        this.client = chatclient;

        setTitle("用户操作页面"); // 设置窗口的标题
        setSize(280, 500); // 设置窗口的大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭时的操作
        setLayout(new BorderLayout()); // 设置窗口的布局管理器

        // 创建顶部的面板，用于显示用户的昵称和个性签名
        JPanel topPanel = creatPanelNorth(client.getUsername());

        add(topPanel, BorderLayout.NORTH); // 将顶部面板添加到窗口的北部（顶部）

        // 创建中部的面板，用于显示好友列表
        JPanel centerPanel = creatPanelCenter(client.getFriends());

        // 创建滚动面板，并将中部面板添加到滚动面板
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        add(scrollPane, BorderLayout.CENTER); // 将滚动面板添加到窗口的中部

        // 在主窗口的构造函数的底部添加以下代码
        JPanel bottomPanel = creatPanelSouth();
        add(bottomPanel, BorderLayout.SOUTH); // 将底部面板添加到窗口的南部（底部）

        //让窗口生成在屏幕的右侧
        setFrameEast();
    }
    private JPanel creatPanelNorth(String nickname){
        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        JLabel nicknameLabel = new JLabel(nickname); // 创建昵称标签
        nicknameLabel.setHorizontalAlignment(SwingConstants.CENTER); // 设置昵称标签的对齐方式
        JLabel signatureLabel = new JLabel("个性签名:"); // 创建个性签名标签
        signatureLabel.setHorizontalAlignment(SwingConstants.CENTER); // 设置个性签名标签的对齐方式
        signatureField = new JTextField(); // 创建个性签名的输入框
        topPanel.add(nicknameLabel); // 将昵称标签添加到顶部面板
        topPanel.add(signatureLabel); // 将个性签名标签添加到顶部面板
        topPanel.add(signatureField); // 将个性签名的输入框添加到顶部面板

        return topPanel;
    }
    private JPanel creatPanelCenter(List<FriendUser> friendList){
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        for(FriendUser friend: friendList) {
            boolean isOnline = friend.isOnline(); // 好友的在线状态
            int unreadMessages = friend.getUnReadChatMessage(); // 未读消息的数量

            JPanel friendPanel = new JPanel(); // 创建每个好友的面板
            friendPanel.setLayout(new BoxLayout(friendPanel, BoxLayout.X_AXIS));

            // 创建状态面板，并设置其大小和颜色
            JPanel statusPanel = new JPanel();
            statusPanel.setPreferredSize(new Dimension(10, 10)); // 设置状态面板的预期大小
            statusPanel.setMaximumSize(new Dimension(10, 10)); // 设置状态面板的最大大小
            statusPanel.setBackground(isOnline ? Color.GREEN : Color.GRAY); // 根据好友的在线状态设置状态面板的背景颜色
            friendStatusPanels.put(friend.getUsername(), statusPanel); // 将好友的状态面板添加到 Map 中


            // 创建好友按钮，并为其添加动作监听器
            JButton friendButton = createFriendButton(friend);

            // 创建未读消息的面板
            JPanel unreadMessagesPanel = new JPanel();
            unreadMessagesPanel.setBackground(Color.WHITE); // 设置未读消息面板的背景颜色
            unreadMessagesPanel.setPreferredSize(new Dimension(30, 30)); // 设置未读消息面板的预期大小
            unreadMessagesPanel.setMaximumSize(new Dimension(30, 30)); // 设置未读消息面板的最大大小

            // 创建未读消息的标签，并将其添加到未读消息面板
            JLabel unreadMessagesLabel = new JLabel(String.valueOf(unreadMessages)); // 创建未读消息的标签
            unreadMessagesLabel.setHorizontalAlignment(SwingConstants.CENTER); // 设置未读消息标签的对齐方式
            unreadMessagesPanel.add(unreadMessagesLabel); // 将未读消息标签添加到未读消息面板
            unreadMessagesLabels.put(friend.getUsername(), unreadMessagesLabel); // 将未读消息标签添加到 Map 中

            friendPanel.add(statusPanel); // 将状态面板添加到好友面板
            friendPanel.add(Box.createRigidArea(new Dimension(5, 0))); // 添加一个固定大小的空间，以在状态面板和好友按钮之间创建间距
            friendPanel.add(friendButton); // 将好友按钮添加到好友面板
            friendPanel.add(unreadMessagesPanel); // 将未读消息面板添加到好友面板

            centerPanel.add(friendPanel); // 将好友面板添加到中部面板
        }
        return centerPanel;
    }
    public void handleFriendStatusUpdate(String friendUsername, boolean isOnline) {
        JPanel statusPanel = friendStatusPanels.get(friendUsername);
        if (statusPanel != null) {
            statusPanel.setBackground(isOnline ? Color.GREEN : Color.GRAY);
            statusPanel.repaint();
        }
    }
    public void updateUnreadMessagesCount(String username, int newCount) {
        JLabel label = unreadMessagesLabels.get(username);
        newCount = Integer.parseInt(label.getText()) - newCount;
        label.setText(String.valueOf(newCount));
        label.repaint();
    }


    private JPanel creatPanelSouth(){
        // 创建底部的面板，用于放置三个按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 3));

        // 创建“查找添加好友”的按钮，并为其添加动作监听器
        JButton addFriendButton = creatAddFriendButton();
        bottomPanel.add(addFriendButton); // 将“查找添加好友”的按钮添加到底部面板

        // 创建“查看好友通知”的按钮，并为其添加动作监听器
        JButton viewNotificationButton = creatViewNotificationButton();
        bottomPanel.add(viewNotificationButton); // 将“查看好友通知”的按钮添加到底部面板

        // 创建“查看我的信息”的按钮，并为其添加动作监听器
        JButton viewInfoButton = creatViewInfoButton();
        bottomPanel.add(viewInfoButton); // 将“查看我的信息”的按钮添加到底部面板

        return bottomPanel;
    }
    private JButton creatAddFriendButton(){
        JButton addFriendButton = new JButton("查找添加");
        addFriendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SearchFriendFrame(client).setVisible(true); // 当按钮被点击时，打开查找好友的窗口
            }
        });
        return addFriendButton;
    }
    private JButton creatViewNotificationButton(){
        JButton viewNotificationButton = new JButton("查看通知");
        viewNotificationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewNotificationFrame(client).setVisible(true); // 当按钮被点击时，打开查看好友通知的窗口
            }
        });
        return viewNotificationButton;
    }
    private JButton creatViewInfoButton(){
        JButton viewInfoButton = new JButton("我的信息");
        viewInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewInfoFrame(client.getUserClient()).setVisible(true); // 当按钮被点击时，打开查看我的信息的窗口
            }
        });
        return viewInfoButton;
    }
    private JButton createFriendButton(FriendUser friend){
        JButton friendButton = new JButton(friend.getUsername());
        friendButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, friendButton.getMinimumSize().height));
        friendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatFrame cFrame = new ChatFrame(friend.getUsername(),client); // 当好友按钮被点击时，打开聊天窗口
                cFrame.setVisible(true);
                client.addChatFrame(cFrame,friend.getUsername());
            }
        });
        return friendButton;
    }
    private void setFrameEast(){
        // 获取屏幕大小
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        // 计算窗口位置
        int frameWidth = getSize().width;
        int frameHeight = getSize().height;
        int x = width - frameWidth;
        int y = (height - frameHeight) / 2;

        // 设置窗口位置
        setLocation(x, y);
    }

}





