import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField ageField;
    private JTextField phoneNumberField;
    private JComboBox<String> genderComboBox;
    private ChatClient client;

    public RegistrationFrame(ChatClient client) {
        this.client = client;

        setTitle("注册新账号");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        panel.add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.7;
        panel.add(confirmPasswordField, gbc);

        JLabel emailLabel = new JLabel("电子邮件:");
        emailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panel.add(emailLabel, gbc);

        emailField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.7;
        panel.add(emailField, gbc);

        JLabel genderLabel = new JLabel("性别:");
        genderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        panel.add(genderLabel, gbc);

        String[] genderOptions = {"男", "女", "其他"};
        genderComboBox = new JComboBox<>(genderOptions);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 0.7;
        panel.add(genderComboBox, gbc);

        JLabel ageLabel = new JLabel("年龄:");
        ageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 5;  // 注意这里的y坐标要适当修改，使其不和其他的组件重叠
        gbc.weightx = 0.3;
        panel.add(ageLabel, gbc);

        ageField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 5;  // 注意这里的y坐标要适当修改，使其不和其他的组件重叠
        gbc.weightx = 0.7;
        panel.add(ageField, gbc);

        JLabel phoneNumberLabel = new JLabel("电话号码:");
        phoneNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 6;  // 注意这里的y坐标要适当修改，使其不和其他的组件重叠
        gbc.weightx = 0.3;
        panel.add(phoneNumberLabel, gbc);

        phoneNumberField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 6;  // 注意这里的y坐标要适当修改，使其不和其他的组件重叠
        gbc.weightx = 0.7;
        panel.add(phoneNumberField, gbc);



        add(panel, BorderLayout.CENTER);

        JButton registerButton = new JButton("创建账号");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                String email = emailField.getText();
                String gender = (String) genderComboBox.getSelectedItem();
                int age = Integer.parseInt(ageField.getText());
                String phoneNumber = phoneNumberField.getText();

                // 判断密码和确认密码是否相等
                if (!password.equals(confirmPassword)) {
                    // 弹出提示对话框
                    JOptionPane.showMessageDialog(Frame.getFrames()[0], "两次输入的密码不匹配",
                            "错误", JOptionPane.ERROR_MESSAGE);
                } else {
                    User user = new User(username, password, email, phoneNumber, gender, age);
                    CreatUserMessage cMes = new CreatUserMessage(username,client.getServerName(),password,"CreatUserMessage",user);
                    client.executeCommandClient(cMes);
                }
            }
        });


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

}
