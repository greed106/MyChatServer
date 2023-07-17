import java.sql.*;
import java.time.LocalDateTime;
import java.util.Vector;

public class DataBase {
    //数据库和服务器的连接
    private Connection conn;
    public DataBase(){
        conn = connectToDataBase();
    }
    public static void main(String[] args){
        DataBase db =new DataBase();
        db.addChatMessage(new SendChatMessage("1","2","test_家人们谁懂啊", "SendChatMessage",LocalDateTime.now()));

    }




    //连接到本地服务器
    public Connection connectToDataBase(){
        Connection conn = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatServer","root","123456");
        } catch (SQLException | ClassNotFoundException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
        return conn;
    }


    //向数据库中添加一个新用户
    //新用户在添加时只关心uid,username,password三个字段，其他的信息在后续由用户自行设置
    public void addUser(User user){
        try {
            String query = "INSERT INTO chatserver.UserInformation (uid,username,password) VALUES (?,?,?)";
            PreparedStatement pStatement = conn.prepareStatement(query);
            user.setUid(getSizeofUserInformation()+1);
            pStatement.setInt(1,user.uid);
            pStatement.setString(2,user.Username);
            pStatement.setString(3,user.Password);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    //添加一条新的聊天消息
    public void addChatMessage(SendChatMessage sChatMessage){
        try {
            String query = "INSERT INTO chatserver.chatmessage(uidSender,uidReceiver,sentTime,content) VALUES (?,?,?,?)";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1,sChatMessage.getNameSender());
            pStatement.setString(2,sChatMessage.getNameReceiver());
            pStatement.setString(3,sChatMessage.getCurrentTime());
            pStatement.setString(4, sChatMessage.getMessage());
            pStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //返回当前数据库中的用户总数，用于为新用户分配uid
    public int getSizeofUserInformation(){
        int totalRows = 0;
        try {
            String query = "SELECT COUNT(*) AS totalRows FROM chatserver.userinformation";
            PreparedStatement pStatement = conn.prepareStatement(query);
            ResultSet rs = pStatement.executeQuery();

            if (rs.next()) {
                totalRows = rs.getInt("totalRows");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return totalRows;
    }

    //判断对于给定的username是否出现重命名
    public boolean isUsernameDuplicate(String username) {
        boolean isDuplicate = false;
        try {
            // 准备查询语句
            String query = "SELECT COUNT(*) FROM chatserver.userinformation WHERE username = ?";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1, username);
            // 执行查询
            ResultSet rs = pStatement.executeQuery();
            // 检查结果集中的计数值
            if (rs.next()) {
                int count = rs.getInt(1);
                isDuplicate = count > 0; // 如果计数值大于0，则表示存在重复的用户名
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isDuplicate; // 发生异常或查询结果为空时，默认为不重复
    }


    //分页查询两个用户间的聊天记录
    //pageSize表示每一页的大小
    //pageNumber表示当前要查询的页码
    public Vector<HistoryChatMessage> getChatMessageBetweenTwoUsersWithPagination(int uid1, int uid2, int pageNumber, int pageSize){
        Vector<HistoryChatMessage> vChatMessage = new Vector<>();
        try {
            String query = "SELECT * FROM chatserver.chatmessage WHERE (uidSender = ? AND uidReceiver = ?) OR (uidSender = ? AND uidReceiver = ?) ORDER BY sentTime DESC LIMIT ? OFFSET ?";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setInt(1, uid1);
            pStatement.setInt(2, uid2);
            pStatement.setInt(3, uid2);
            pStatement.setInt(4, uid1);
            pStatement.setInt(5, pageSize);
            pStatement.setInt(6, pageSize * (pageNumber - 1));
            ResultSet rs = pStatement.executeQuery();
            while (rs.next()) {
                String nameSender = getUsernameByUid(rs.getInt("uidSender"));
                String nameReceiver = getUsernameByUid(rs.getInt("uidReceiver"));
                vChatMessage.add(new HistoryChatMessage(nameSender,nameReceiver,rs.getString("content"),"HistoryChatMessage",rs.getString("sentTime")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vChatMessage;
    }
    public int getUidByUsername(String username) {
        int uid = 0;
        try{
            // 构建查询SQL
            String query = "SELECT uid FROM chatserver.userinformation WHERE username = ?";
            // 设置参数
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1, username);
            // 执行查询
            ResultSet rs = pStatement.executeQuery();
            // 获取结果
            if (rs.next()) {
                uid = rs.getInt("uid");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return uid;

    }

    public String getUsernameByUid(int uid) {
        String username = "";
        try{
            // 构建查询SQL
            String query = "SELECT username FROM chatserver.userinformation WHERE uid = ?";
            PreparedStatement pStatement = conn.prepareStatement(query);
            // 设置参数
            pStatement.setInt(1, uid);
            // 执行查询
            ResultSet rs = pStatement.executeQuery();
            // 获取结果
            if (rs.next()) {
                username = rs.getString("username");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return username;
    }

    //根据指定的页面大小pageSize和两人的聊天信息数返回应有的页面数
    public int getPageNumber(int uid1, int uid2, int pageSize) {
        // 构建统计总记录数的SQL
        String query = "SELECT COUNT(*) AS total FROM chatserver.chatmessage WHERE (uidSender = ? AND uidReceiver = ?) OR (uidSender = ? AND uidReceiver = ?)";
        int total = 0;
        try {
            PreparedStatement pStatement = conn.prepareStatement(query);
            // 设置参数
            pStatement.setInt(1, uid1);
            pStatement.setInt(2, uid2);
            pStatement.setInt(3, uid2);
            pStatement.setInt(4, uid1);
            // 执行查询
            ResultSet rs = pStatement.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 计算页数
        int pageNumber = total / pageSize;
        if (total % pageSize != 0) {
            pageNumber++;
        }
        return pageNumber;
    }
    public boolean isUserValid(String username, String password) {
        boolean isValid = false;
        try {
            // 准备查询语句
            String query = "SELECT * FROM chatserver.userinformation WHERE username = ? AND password = ?";
            PreparedStatement pStatement = conn.prepareStatement(query);
            // 设置参数
            pStatement.setString(1, username);
            pStatement.setString(2, password);
            // 执行查询
            ResultSet rs = pStatement.executeQuery();
            // 检查结果集是否含有记录
            isValid = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isValid;
    }
    public User getUserByUsername(String username) {
        User user = null;
        try{
            // 构建查询SQL
            String query = "SELECT * FROM chatserver.userinformation WHERE username = ?";
            // 设置参数
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1, username);
            // 执行查询
            ResultSet rs = pStatement.executeQuery();
            // 获取结果
            if (rs.next()) {
                user = new User();
                user.setUid(rs.getInt("uid"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phonenumber"));
                user.setSex(rs.getInt("sex"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return user;
    }


}
class User{
    //uid是指在数据库中存放的列数，具有唯一性
    protected int uid;
    //用户名是用户的标识，具有唯一性
    protected String Username;
    //用户的登陆密码
    protected String Password;
    //用户的邮箱地址
    protected String Email;
    //用户的手机号
    protected String PhoneNumber;
    //用户性别,0表示保密，1表示男，2表示女，3表示其他
    protected int Sex;

    public int getUid() {
        return uid;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public int getSex() {
        return Sex;
    }

    public void setSex(int sex) {
        Sex = sex;
    }

    public User(int uid, String username, String password) {
        this.uid = uid;
        this.Username = username;
        this.Password = password;
    }

    public User(String username, String password) {
        this.Username = username;
        this.Password = password;
    }

    public User() {}


    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        this.Password = password;
    }
}
