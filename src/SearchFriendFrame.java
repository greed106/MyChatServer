import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 创建查找好友的窗口类
class SearchFriendFrame extends JFrame {
    private ChatClient client;
    private static boolean haveFound = false;

    private static SearchFriend friend = null;
    JTextField searchField;
    public SearchFriendFrame(ChatClient client) {
        super("查找添加好友");
        this.client = client;

        setSize(500, 200);  // 改变窗口大小以适应较大的文本框
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建上方的面板，包括一个文本输入框和一个查找按钮
        JPanel topPanel = createPanelNorth();

        // 创建下方的面板，包括一个用于程序输出信息的文本框
        // JPanel bottomPanel = createPanelSouth();

        add(topPanel, BorderLayout.NORTH);
        // add(bottomPanel, BorderLayout.CENTER);

        setFrameCenter(this);
    }
    private JPanel createPanelNorth(){
        JPanel topPanel = new JPanel();

        //文本框
        searchField = new JTextField(20);  // 增大文本框的长度
        //搜索按钮
        JButton searchButton = createSearchButton();

        topPanel.add(searchField);
        topPanel.add(searchButton);


        return topPanel;
    }
    private void setFrameCenter(JFrame frame){
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
    private JPanel createPanelSouth(){
        JPanel bottomPanel = new JPanel();
        JTextArea outputArea = new JTextArea(10, 30);  // 增大文本框的大小
        outputArea.setEditable(false);  // 设置文本框为不可编辑
        JScrollPane scrollPane = new JScrollPane(outputArea);
        bottomPanel.add(scrollPane);

        return bottomPanel;
    }
    private JButton createSearchButton(){
        JButton searchButton = new JButton("查找");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 在这里添加查找好友的逻辑
                String friendName = searchField.getText();
                client.executeCommandClient(new GetSearchFriendMessage(client.getUsername(),
                        client.getServerName(), friendName,"GetSearchFriendMessage"));
                synchronized (client.getLock()){
                    while(!haveFound){
                        try {
                            client.getLock().wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    haveFound = false;
                }
                //如果没有找到用户
                if(friend == null){
                    JOptionPane.showMessageDialog(
                            Frame.getFrames()[0], // Get the current active frame to center the dialog
                            "您查找的用户不存在，请检查您的输入", // Error message
                            "错误", // Dialog title
                            JOptionPane.ERROR_MESSAGE // Message type
                    );
                // 如果已经是好友了
                }else if(friend.isFriend()){
                    JOptionPane.showMessageDialog(
                            Frame.getFrames()[0], // Get the current active frame to center the dialog
                            "你们已经是好友啦", // Error message
                            "错误", // Dialog title
                            JOptionPane.ERROR_MESSAGE // Message type
                    );
                // 如果已经发送过请求了
                }else if(friend.isPending()){
                    JOptionPane.showMessageDialog(
                            Frame.getFrames()[0], // Get the current active frame to center the dialog
                            "您已经发送过申请了，请耐心等待", // Error message
                            "错误", // Dialog title
                            JOptionPane.ERROR_MESSAGE // Message type
                    );
                // 如果查找成功，弹出一个对话框询问用户是否要添加对方为好友
                }else{
                    new ViewInfoFrame(friend).setVisible(true);
                    int option = JOptionPane.showConfirmDialog(
                            SearchFriendFrame.this,
                            "用户已找到，是否添加为好友？",
                            "查找成功",
                            JOptionPane.YES_NO_OPTION
                    );
                    // 根据用户的选择执行相应的逻辑
                    if (option == JOptionPane.YES_OPTION) {
                        // 用户选择了“是”，在这里添加添加好友的逻辑

                    }
                }

            }
        });
        return searchButton;
    }

    public static boolean isHaveFound() {
        return haveFound;
    }

    public static void setHaveFound(boolean haveFound) {
        SearchFriendFrame.haveFound = haveFound;
    }

    public static void setFriend(SearchFriend friend) {
        SearchFriendFrame.friend = friend;
    }
}
class SearchFriend extends User{
    private boolean isFriend = false;
    private boolean isPending = false;

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }
}