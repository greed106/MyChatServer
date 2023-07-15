import java.sql.*;
import java.time.LocalDateTime;

public class DataBase {
    private Connection conn;
    public DataBase(){
        conn = connectToDataBase();
    }
    public static void main(String[] args){
        DataBase db =new DataBase();
        db.addChatMessage(new SendChatMessage("1","2","test", "SendChatMessage",LocalDateTime.now()));

    }
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
    public void addUser(User user){
        try {
            String query = "INSERT INTO UserInformation (uid,username,password) VALUES (?,?,?)";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setInt(1,user.uid);
            pStatement.setString(2,user.username);
            pStatement.setString(3,user.password);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void addChatMessage(SendChatMessage sChatMessage){
        try {
            String query = "INSERT INTO Message(uidSender,uidReceiver,sentTime,content) VALUES (?,?,?,?)";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1,sChatMessage.getUidSender());
            pStatement.setString(2,sChatMessage.getUidReceiver());
            pStatement.setString(3,sChatMessage.getCurrentTime());
            pStatement.setString(4, sChatMessage.getMessage());
            pStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getSizeofUserInformation(){
        int totalRows = 0;
        try {
            String query = "SELECT COUNT(*) AS totalRows FROM userinformation";
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


}
class User{
    protected int uid;
    protected String username;
    protected String password;

    public User(int uid, String username, String password) {
        this.uid = uid;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
