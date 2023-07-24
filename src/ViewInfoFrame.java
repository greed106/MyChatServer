import javax.swing.*;
import java.awt.*;

// 创建查看我的信息的窗口类
public class ViewInfoFrame extends JFrame {
    //...
    // 在这里添加查看我的信息窗口的相关代码
    public ViewInfoFrame(User user){
        super(user.getUsername()+"的详细信息");
        setInfoFrame(user,this);
        setFrameCenter(this);
    }
    private void setInfoFrame(User user,JFrame infoFrame){
        // 创建一个新的窗口来显示详细信息
        infoFrame.setLayout(new GridBagLayout()); // 设置布局为 GridBagLayout
        infoFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL; // 组件在水平方向上应占据其显示区域的所有空间
        constraints.anchor = GridBagConstraints.WEST; // 组件应该放在其显示区域的西边

        // 在新窗口中显示好友的详细信息
        constraints.weightx = 0.3; // 左侧标签的权重
        constraints.gridx = 0; // 组件应该开始的网格列
        constraints.gridy = 0; // 组件应该开始的网格行
        infoFrame.add(new JLabel("用户名："), constraints);

        constraints.weightx = 0.7; // 右侧标签的权重
        constraints.gridx = 1; // 组件应该开始的网格列
        infoFrame.add(new JLabel(user.getUsername()), constraints);

        constraints.weightx = 0.3;
        constraints.gridx = 0; // 组件应该开始的网格列
        constraints.gridy = 1; // 组件应该开始的网格行
        infoFrame.add(new JLabel("性别："), constraints);

        constraints.weightx = 0.7;
        constraints.gridx = 1; // 组件应该开始的网格列
        infoFrame.add(new JLabel(user.getSex()), constraints);

        constraints.weightx = 0.3;
        constraints.gridx = 0; // 组件应该开始的网格列
        constraints.gridy = 2; // 组件应该开始的网格行
        infoFrame.add(new JLabel("年龄："), constraints);

        constraints.weightx = 0.7;
        constraints.gridx = 1; // 组件应该开始的网格列
        infoFrame.add(new JLabel(String.valueOf(user.getAge())), constraints);

        constraints.weightx = 0.3;
        constraints.gridx = 0; // 组件应该开始的网格列
        constraints.gridy = 3; // 组件应该开始的网格行
        infoFrame.add(new JLabel("电话号码："), constraints);

        constraints.weightx = 0.7;
        constraints.gridx = 1; // 组件应该开始的网格列
        infoFrame.add(new JLabel(user.getPhoneNumber()), constraints);

        constraints.weightx = 0.3;
        constraints.gridx = 0; // 组件应该开始的网格列
        constraints.gridy = 4; // 组件应该开始的网格行
        infoFrame.add(new JLabel("电子邮箱："), constraints);

        constraints.weightx = 0.7;
        constraints.gridx = 1; // 组件应该开始的网格列
        infoFrame.add(new JLabel(user.getEmail()), constraints);

        infoFrame.setSize(300,230);
        infoFrame.setVisible(true); // 显示窗口
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
}