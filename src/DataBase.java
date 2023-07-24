import java.io.Serializable;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataBase {
    //数据库和服务器的连接
    private Connection conn;
    public DataBase(){
        conn = connectToDataBase();
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
    public void addUser(User user) {
        String query = "INSERT INTO chatserver.UserInformation (uid, username, password, email, phoneNumber, sex, age) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            user.setUid(getSizeofUserInformation() + 1);
            pStatement.setInt(1, user.getUid());
            pStatement.setString(2, user.getUsername());
            pStatement.setString(3, user.getPassword());
            pStatement.setString(4, user.getEmail());
            pStatement.setString(5, user.getPhoneNumber());
            pStatement.setString(6, user.getSex());
            pStatement.setInt(7, user.getAge());

            pStatement.executeUpdate();
        } catch (SQLException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    //添加一条新的聊天消息
    public void addChatMessage(SendChatMessage sChatMessage,boolean haveRead) {
        String query = "INSERT INTO chatserver.chatmessage(uidSender, uidReceiver, sentTime, content, haveRead) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            int uidSender = getUidByUsername(sChatMessage.getNameSender());
            int uidReceiver = getUidByUsername(sChatMessage.getNameReceiver());
            pStatement.setInt(1, uidSender);
            pStatement.setInt(2, uidReceiver);
            pStatement.setString(3, sChatMessage.getCurrentTime());
            pStatement.setString(4, sChatMessage.getMessage());
            pStatement.setInt(5, haveRead ? 1 : 0);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUnReadCount(int uidSender, int uidReceiver) {
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
     * 获取两个用户之间的聊天消息列表。
     *
     * @param uid1       用户1的UID
     * @param uid2       用户2的UID
     * @return 聊天消息列表
     */
    public List<HistoryChatMessage> getHistoryMessage(int uid1, int uid2) {
        List<HistoryChatMessage> chatMessages = new ArrayList<>();
        int pageNumber = getPageNumber(uid1,uid2,ChatClient.HistoryPageSize);
        String query = "SELECT * FROM chatserver.chatmessage WHERE (uidSender = ? AND uidReceiver = ?) " +
                "OR (uidSender = ? AND uidReceiver = ?) ";
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setInt(1, uid1);
            pStatement.setInt(2, uid2);
            pStatement.setInt(3, uid2);
            pStatement.setInt(4, uid1);

            try (ResultSet rs = pStatement.executeQuery()) {
                while (rs.next()) {
                    String nameSender = getUsernameByUid(rs.getInt("uidSender"));
                    String nameReceiver = getUsernameByUid(rs.getInt("uidReceiver"));
                    HistoryChatMessage hMes = new HistoryChatMessage(nameSender, nameReceiver,
                            rs.getString("content"), "HistoryChatMessage",
                            rs.getString("sentTime"));
                    hMes.setMessageId(rs.getInt("idMessage"));
                    hMes.setHaveRead((rs.getInt("haveRead")) != 0);
                    chatMessages.add(hMes);

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
    public void updateRequest(FriendRequest request){
        String updateQuery = "UPDATE chatserver.friends SET status = ? WHERE relationId = ?";
        try(PreparedStatement updateStatement = conn.prepareStatement(updateQuery)){
            updateStatement.setString(1,request.getStatus());
            updateStatement.setInt(2,request.getRelationId());
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deleteRequest(int relationId) {
        String deleteQuery = "DELETE FROM chatserver.friends WHERE relationId = ?";
        try (PreparedStatement deleteStatement = conn.prepareStatement(deleteQuery)) {
            deleteStatement.setInt(1, relationId);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addRequest(FriendRequest request) {
        String insertQuery = "INSERT INTO chatserver.friends (userId, friendId, status) VALUES (?, ?, ?)";
        try (PreparedStatement insertStatement = conn.prepareStatement(insertQuery)) {
            // 获取nameSender对应的userId
            int userId = getUidByUsername(request.getNameSender());
            // 获取nameReceiver对应的friendId
            int friendId = getUidByUsername(request.getNameReceiver());
            System.out.println("userId:"+userId);
            System.out.println("friendId:"+friendId);

            insertStatement.setInt(1, userId);
            insertStatement.setInt(2, friendId);
            insertStatement.setString(3, request.getStatus());

            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FriendRequest> getRequests(String messageSender){
        String query = "SELECT * FROM chatserver.friends WHERE friendId = ? AND status = ?";
        int friendId = getUidByUsername(messageSender);
        List<FriendRequest> requests = new ArrayList<>();
        try(PreparedStatement pStatement = conn.prepareStatement(query)){
            pStatement.setInt(1,friendId);
            pStatement.setString(2,"PENDING");
            try(ResultSet rs = pStatement.executeQuery()){
                while(rs.next()){
                    String nameSender = getUsernameByUid(rs.getInt("userId"));
                    int relationId = rs.getInt("relationId");
                    String status = rs.getString("status");
                    requests.add(new FriendRequest(nameSender,status,relationId));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    public List<Integer> getAcceptedFriendsUid(int uid) {
        List<Integer> acceptedFriends = new ArrayList<>();
        // 创建 SQL 查询语句
        String query1 = "SELECT friendId FROM chatserver.friends WHERE userId = ? AND status = 'ACCEPTED' ";
        String query2 = "SELECT userId FROM chatserver.friends WHERE friendId = ? AND status = 'ACCEPTED' ";

        try (PreparedStatement stmt1 = conn.prepareStatement(query1);
             PreparedStatement stmt2 = conn.prepareStatement(query2)) {
            // 设置参数
            stmt1.setInt(1, uid);
            stmt2.setInt(1, uid);
            // 执行查询
            try (ResultSet rs = stmt1.executeQuery()) {
                // 获取结果
                while (rs.next()) {
                    int friendId = rs.getInt("friendId");
                    acceptedFriends.add(friendId);
                }
            }
            try (ResultSet rs = stmt2.executeQuery()) {
                // 获取结果
                while (rs.next()) {
                    int friendId = rs.getInt("userId");
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
    public String getFriendStatus(String nameSender, String friendName) {
        int uidSender = getUidByUsername(nameSender);
        int uidFriend = getUidByUsername(friendName);


        String query = "SELECT status FROM chatserver.friends " +
                "WHERE (userId = ? AND friendId = ?) OR (userId = ? AND friendId = ?)";
        String status = "NONE";

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setInt(1, uidSender);
            pStatement.setInt(2, uidFriend);
            pStatement.setInt(3,uidFriend);
            pStatement.setInt(4,uidSender);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    status = rs.getString("status");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }

    public SearchFriend getSearchFriend(String friendName, String nameSender) {
        String query = "SELECT * FROM chatserver.userinformation WHERE username = ?";
        SearchFriend searchFriend = new SearchFriend();
        boolean haveFound = false;
        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setString(1, friendName);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    // Fill the searchFriend object with the user information
                    searchFriend.setUid(rs.getInt("uid"));
                    searchFriend.setUsername(rs.getString("username"));
                    searchFriend.setEmail(rs.getString("email"));
                    searchFriend.setPhoneNumber(rs.getString("phonenumber"));
                    searchFriend.setSex(rs.getString("sex"));
                    searchFriend.setAge(rs.getInt("age"));
                    haveFound = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(haveFound){
            // Now check the friend relationship
            String status = getFriendStatus(nameSender, friendName);
            searchFriend.setFriend("ACCEPTED".equals(status));
            searchFriend.setPending("PENDING".equals(status));
        }else{
            searchFriend = null;
        }
        return searchFriend;
    }


    /**
     * 根据指定的页面大小和两个用户的聊天信息数，计算应有的页面数。
     *
     * @param uid1     用户1的UID
     * @param uid2     用户2的UID
     * @param pageSize 页面大小
     * @return 页面数(从1开始)
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
                    user.setSex(rs.getString("sex"));
                    user.setAge(rs.getInt("age"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }
    public User getUserByUid(int uid) {
        String query = "SELECT * FROM chatserver.userinformation WHERE uid = ?";
        User user = null;

        try (PreparedStatement pStatement = conn.prepareStatement(query)) {
            pStatement.setInt(1, uid);

            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setUid(rs.getInt("uid"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phonenumber"));
                    user.setSex(rs.getString("sex"));
                    user.setAge(rs.getInt("age"));
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

    protected String Sex;
    protected int Age;

    public User( String username, String password, String email, String phoneNumber, String sex, int age) {
        Username = username;
        Password = password;
        Email = email;
        PhoneNumber = phoneNumber;
        Sex = sex;
        Age = age;
    }

    public int getAge() {
        return Age;
    }
    public void setAge(int age) {
        Age = age;
    }

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

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    public User(int uid, String username, String password) {
        this.uid = uid;
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
