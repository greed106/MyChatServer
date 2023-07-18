import java.io.Serializable;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
            conn = DriverManager.getConnection("jdbc:mysql://10.21.229.183:3306/ChatServer","root","123456");
        } catch (SQLException | ClassNotFoundException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
        return conn;
    }


    //向数据库中添加一个新用户
    //新用户在添加时只关心uid,username,password三个字段，其他的信息在后续由用户自行设置
    public void addUser(User user) {
        String query = "INSERT INTO chatserver.UserInformation (uid, username, password) VALUES (?, ?, ?)";

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            user.setUid(getSizeofUserInformation() + 1);
            pStatement.setInt(1, user.getUid());
            pStatement.setString(2, user.getUsername());
            pStatement.setString(3, user.getPassword());

            pStatement.executeUpdate();
        } catch (SQLException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }



    //添加一条新的聊天消息
    public void addChatMessage(SendChatMessage sChatMessage) {
        String query = "INSERT INTO chatserver.chatmessage(uidSender, uidReceiver, sentTime, content) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setString(1, sChatMessage.getNameSender());
            pStatement.setString(2, sChatMessage.getNameReceiver());
            pStatement.setString(3, sChatMessage.getCurrentTime());
            pStatement.setString(4, sChatMessage.getMessage());
            pStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUnreadCount(int uidSender, int uidReceiver) {
        int unreadCount = 0;
        String query = "SELECT COUNT(*) FROM chatserver.chatmessage WHERE uidSender = ? AND uidReceiver = ? AND haveRead = 0";
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setInt(1, uidSender);
            pStatement.setInt(2, uidReceiver);
            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    unreadCount = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return unreadCount;
    }

    //返回当前数据库中的用户总数，用于为新用户分配uid
    public int getSizeofUserInformation() {
        int totalRows = 0;
        String query = "SELECT COUNT(*) AS totalRows FROM chatserver.userinformation";
        try (PreparedStatement pStatement = conn.prepareStatement(query);
             ResultSet rs = pStatement.executeQuery()) {
            if (rs.next()) {
                totalRows = rs.getInt("totalRows");
            }
        } catch (SQLException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
        return totalRows;
    }


    //判断对于给定的username是否出现重命名
    public boolean isUsernameDuplicate(String username) {
        boolean isDuplicate = false;
        String query = "SELECT COUNT(*) FROM chatserver.userinformation WHERE username = ?";
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setString(1, username);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    isDuplicate = count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isDuplicate;
    }



    /**
     * 获取两个用户之间分页的聊天消息列表。
     *
     * @param uid1       用户1的UID
     * @param uid2       用户2的UID
     * @param pageNumber 页面号码，从1开始
     * @param pageSize   每页的消息数量
     * @return 聊天消息列表
     */
    public List<HistoryChatMessage> getChatMessageBetweenTwoUsersWithPagination(int uid1, int uid2, int pageNumber, int pageSize) {
        List<HistoryChatMessage> chatMessages = new ArrayList<>();
        String query = "SELECT * FROM chatserver.chatmessage WHERE (uidSender = ? AND uidReceiver = ?) " +
                "OR (uidSender = ? AND uidReceiver = ?) ORDER BY sentTime DESC LIMIT ? OFFSET ?";
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setInt(1, uid1);
            pStatement.setInt(2, uid2);
            pStatement.setInt(3, uid2);
            pStatement.setInt(4, uid1);
            pStatement.setInt(5, pageSize);
            pStatement.setInt(6, pageSize * (pageNumber - 1));

            try (ResultSet rs = pStatement.executeQuery()) {
                while (rs.next()) {
                    String nameSender = getUsernameByUid(rs.getInt("uidSender"));
                    String nameReceiver = getUsernameByUid(rs.getInt("uidReceiver"));
                    chatMessages.add(new HistoryChatMessage(nameSender, nameReceiver, rs.getString("content"), "HistoryChatMessage", rs.getString("sentTime")));

                    // 更新 haveRead 字段为 1
                    int idMessage = rs.getInt("idMessage");
                    updateHaveReadStatus(idMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatMessages;
    }

    /**
     * 更新消息的 haveRead 字段为 1，表示已读状态。
     *
     * @param idMessage 消息ID
     */
    public void updateHaveReadStatus(int idMessage) {
        String updateQuery = "UPDATE chatserver.chatmessage SET haveRead = 1 WHERE idMessage = ?";
        try (PreparedStatement updateStatement = conn.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, idMessage);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAcceptedFriends(int uid) {
        List<Integer> acceptedFriends = new ArrayList<>();
        // 创建 SQL 查询语句
        String query = "SELECT friendId FROM chatserver.friends WHERE (userId = ? OR friendId = ?) AND status = 'ACCEPTED' ";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // 设置参数
            stmt.setInt(1, uid);
            stmt.setInt(2, uid);
            // 执行查询
            try (ResultSet rs = stmt.executeQuery()) {
                // 获取结果
                while (rs.next()) {
                    int friendId = rs.getInt("friendId");
                    acceptedFriends.add(friendId);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return acceptedFriends;
    }
    public int getUidByUsername(String username) {
        int uid = 0;
        String query = "SELECT uid FROM chatserver.userinformation WHERE username = ?";

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setString(1, username);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    uid = rs.getInt("uid");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return uid;
    }


    public String getUsernameByUid(int uid) {
        String username = "";
        String query = "SELECT username FROM chatserver.userinformation WHERE uid = ?";
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setInt(1, uid);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }

    /**
     * 根据指定的页面大小和两个用户的聊天信息数，计算应有的页面数。
     *
     * @param uid1     用户1的UID
     * @param uid2     用户2的UID
     * @param pageSize 页面大小
     * @return 页面数
     */
    public int getPageNumber(int uid1, int uid2, int pageSize) {
        String query = "SELECT COUNT(*) AS total FROM chatserver.chatmessage WHERE (uidSender = ? AND uidReceiver = ?) OR (uidSender = ? AND uidReceiver = ?)";
        int total = 0;

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setInt(1, uid1);
            pStatement.setInt(2, uid2);
            pStatement.setInt(3, uid2);
            pStatement.setInt(4, uid1);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int pageNumber = total / pageSize;
        if (total % pageSize != 0) {
            pageNumber++;
        }

        return pageNumber;
    }

    /**
     * 检查给定的用户名和密码是否有效。
     *
     * @param username 用户名
     * @param password 密码
     * @return 如果用户有效，则返回true；否则返回false。
     */
    public boolean isUserValid(String username, String password) {
        String query = "SELECT * FROM chatserver.userinformation WHERE username = ? AND password = ?";
        boolean isValid = false;

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setString(1, username);
            pStatement.setString(2, password);

            try (ResultSet rs = pStatement.executeQuery()) {
                isValid = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isValid;
    }
    /**
     * 根据用户名获取用户信息。
     *
     * @param username 用户名
     * @return 匹配的用户对象，如果找不到匹配则返回null。
     */
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM chatserver.userinformation WHERE username = ?";
        User user = null;

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setString(1, username);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setUid(rs.getInt("uid"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phonenumber"));
                    user.setSex(rs.getInt("sex"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }
}
class User implements Serializable {
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
