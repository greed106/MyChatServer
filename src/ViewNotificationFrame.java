import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.List;

// 创建查看好友通知的窗口类
public class ViewNotificationFrame extends JFrame {
    private JPanel notificationPanel;
    private ChatClient client;
    public ViewNotificationFrame(ChatClient client) {
        this.client = client;

        setTitle("查看好友通知");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        notificationPanel = new JPanel();
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(notificationPanel);
        add(scrollPane, BorderLayout.CENTER);

        updateNotifications();
    }
    public void updateNotifications() {
        client.executeCommandClient(new GetRequestsMessage(client.getUsername(), client.getServerName(),
                "GetRequestsMessage","GetRequestsMessage"));
        notificationPanel.removeAll();
        synchronized (client.getLock()){
            while(!client.isReceiveFriendRequest()){
                try {
                    client.getLock().wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            createNotifications();
            client.setReceiveFriendRequest(false);
        }
    }
    private void createNotifications(){
        List<FriendRequest> friendRequests = client.getFriendRequests();
        for (FriendRequest request : friendRequests) {
            JPanel requestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel requestLabel = new JLabel(request.getNameSender()+" 希望添加您为好友");

            JButton acceptButton = createAcceptButton(request);
            JButton rejectButton = createRejectButton(request);

            requestPanel.add(requestLabel);
            buttonPanel.add(acceptButton);
            buttonPanel.add(rejectButton);

            JPanel p = new JPanel(new BorderLayout());
            p.add(requestPanel,BorderLayout.WEST);
            p.add(buttonPanel,BorderLayout.EAST);
            notificationPanel.add(p);

        }
        setFrameCenter(this);
        notificationPanel.revalidate();
        notificationPanel.repaint();
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
    private JButton createAcceptButton(FriendRequest request){
        JButton acceptButton = new JButton("同意");
        acceptButton.addActionListener(e -> {
            request.setStatus("ACCEPTED");
            client.executeCommandClient(new UpdateRequestMessage(client.getUsername(),
                    client.getServerName(), "ACCEPTED",
                    "UpdateRequestMessage", request));
            updateNotifications();
            JOptionPane.showMessageDialog(this, "已接受好友请求。请重启客户端以查看更新。", "信息", JOptionPane.INFORMATION_MESSAGE);
        });
        return acceptButton;
    }
    private JButton createRejectButton(FriendRequest request){
        JButton rejectButton = new JButton("拒绝");
        rejectButton.addActionListener(e -> {
            request.setStatus("REJECTED");
            client.executeCommandClient(new UpdateRequestMessage(client.getUsername(),
                    client.getServerName(), "REJECTED",
                    "UpdateRequestMessage", request));
            updateNotifications();
            JOptionPane.showMessageDialog(this, "已拒绝好友请求。", "信息", JOptionPane.INFORMATION_MESSAGE);
        });
        return rejectButton;
    }
}
class FriendRequest implements Serializable{
    private String nameSender;
    private String nameReceiver;
    private String status;
    private int relationId;

    public FriendRequest(String nameSender, String status, int relationId) {
        this.nameSender = nameSender;
        this.status = status;
        this.relationId = relationId;
    }

    public FriendRequest(String nameSender, String nameReceiver, String status) {
        this.nameSender = nameSender;
        this.nameReceiver = nameReceiver;
        this.status = status;
    }

    public String getNameSender() {
        return nameSender;
    }

    public String getNameReceiver() {
        return nameReceiver;
    }

    public void setNameReceiver(String nameReceiver) {
        this.nameReceiver = nameReceiver;
    }

    public void setNameSender(String nameSender) {
        this.nameSender = nameSender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRelationId() {
        return relationId;
    }
}