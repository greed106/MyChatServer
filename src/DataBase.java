import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
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
    public void addUser(Connection conn,User user){

    }
}
class User{
    protected String username;
    protected String password;


}
