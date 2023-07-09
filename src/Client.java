// 导入所需的库
import java.io.*;
import java.net.*;

// 定义一个名为Client的公共类
public class Client {
    // 为每个客户端创建BufferedReader、PrintWriter和Socket对象
    BufferedReader reader;
    PrintWriter writer;
    Socket sock;

    // 主函数
    public static void main(String[] args) {
        // 创建Client类的一个对象并启动客户端
        new Client().startClient();
    }

    // startClient方法，设置客户端的主要功能
    public void startClient() {


        // 尝试创建一个Socket对象以连接到服务器，并初始化PrintWriter对象
        try {
            sock = new Socket("localhost", 5000);
            writer = new PrintWriter(sock.getOutputStream());
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        // 创建一个线程，用于接收来自服务器的消息，并启动它
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

        // 在主线程中，读取用户的输入并发送给服务器
        try {
            BufferedReader userReader = new BufferedReader(new InputStreamReader(System.in));
            String userMessage;
            while((userMessage = userReader.readLine()) != null) {
                writer.println(userMessage);
                writer.flush();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    // IncomingReader是一个内部类，它实现Runnable接口以创建一个新线程
    class IncomingReader implements Runnable {
        // 重写run方法，用于接收来自服务器的信息
        public void run() {
            String message;
            try {
                reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                while((message = reader.readLine()) != null) {
                    System.out.println("Read " + message);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
