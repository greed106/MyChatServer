import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class HistoryMessageFrame extends JFrame {
    private JTextArea textArea;
    private JLabel pageLabel;
    private int currentPage;
    private int totalPages;
    private ChatClient client;
    String friendName;
    public HistoryMessageFrame(ChatClient client,String friendName){
        super("和"+friendName+"的聊天记录");
        this.client = client;
        this.friendName = friendName;

        setLayout(new BorderLayout());
        setSize(400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel pagePanel = new JPanel();
        JButton previousButton = createPreviousButton();
        pagePanel.add(previousButton);

        pageLabel = new JLabel();
        pagePanel.add(pageLabel);

        JButton nextButton = createNextButton();
        pagePanel.add(nextButton);

        add(pagePanel, BorderLayout.SOUTH);

        currentPage = 1;
        totalPages = client.getTotalHistoryPages(friendName); // 用于获取总页数
        updateTextArea();
        updatePageLabel();

        setFrameCenter(this);
    }

    private void updateTextArea(){
        List<HistoryChatMessage> messages = client.getChatHistory(currentPage,friendName);
        textArea.setText(""); // 清空文本区
        List<Integer> updateMessage = new ArrayList<>();
        for(HistoryChatMessage message: messages){
            textArea.append(message.toString()); // 假设ReadChatMessage类有一个合适的toString方法
            if(!message.haveRead() && message.getNameSender().equals(friendName)){
                updateMessage.add(message.getMessageId());
                message.setHaveRead(true);
            }
        }
        if(updateMessage.size()>0){
            UnreadUpdateMessage uMes = new UnreadUpdateMessage(client.getUsername(), friendName,
                    String.valueOf(updateMessage.size()),"UnreadUpdateMessage", updateMessage);
            client.executeCommandClient(uMes);
        }
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
    private void updatePageLabel(){
        pageLabel.setText("当前第 " + currentPage + " 页 / 共 " + totalPages + " 页");
    }
    private JButton createNextButton(){
        JButton nextButton = new JButton("下一页");
        nextButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(currentPage < totalPages){
                    currentPage++;
                    updateTextArea();
                    updatePageLabel();
                }
            }
        });
        return nextButton;
    }
    private JButton createPreviousButton(){
        JButton previousButton = new JButton("上一页");
        previousButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(currentPage > 1){
                    currentPage--;
                    updateTextArea();
                    updatePageLabel();
                }
            }
        });
        return previousButton;
    }
}