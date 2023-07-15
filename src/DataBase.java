import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBase {
    private Connection conn;
    public DataBase(){
        conn = connectToDataBase();
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
            String query = "INSERT INTO UserInformation (username,password) VALUES (?,?)";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1,user.username);
            pStatement.setString(2,user.password);
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


}
class User{
    protected String username;
    protected String password;

    public User(String username, String password) {
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
