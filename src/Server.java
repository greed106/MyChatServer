import java.io.*;
import java.net.*;
import java.util.*;

// 封装客户端连接信息的内部类
class ClientConnection {
    Socket socket;              // 客户端套接字
    PrintWriter out;           // 用于向客户端发送数据的输出流
    BufferedReader in;         // 用于从客户端接收数据的输入流

    ClientConnection(Socket socket, PrintWriter out, BufferedReader in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }
}

public class Server {
    private List<ClientConnection> clients;   // 保存所有客户端连接的列表

    public static void main(String[] args) {
        new Server().startServer();
    }

    public void startServer() {
        clients = new ArrayList<>();    // 创建客户端连接列表
        try {
            ServerSocket serverSock = new ServerSocket(5000);   // 创建服务器套接字并绑定到指定端口
            while(true) {
                Socket clientSocket = serverSock.accept();     // 接受客户端连接请求并创建对应的套接字
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());   // 创建用于向客户端发送数据的输出流
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));    // 创建用于从客户端接收数据的输入流
                ClientConnection client = new ClientConnection(clientSocket, writer, reader);    // 创建封装客户端连接信息的对象
                clients.add(client);    // 将客户端连接信息添加到列表中

                Thread t = new Thread(new ClientHandler(client));   // 创建处理客户端请求的线程
                t.start();    // 启动线程
                System.out.println("Got a connection");   // 打印连接成功的消息
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    class ClientHandler implements Runnable {
        ClientConnection client;

        public ClientHandler(ClientConnection client) {
            this.client = client;
        }

        public void run() {
            String message;
            try {
                while ((message = client.in.readLine()) != null) {   // 从客户端读取数据，直到连接关闭
                    System.out.println("Read " + message);    // 打印接收到的消息
                    tellEveryone(message);   // 将消息发送给所有客户端
                }
            } catch(SocketException ex) {
                clients.remove(client);    // 客户端连接异常断开时，从列表中移除该客户端
                System.out.println("A client disconnected");   // 打印客户端断开连接的消息
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void tellEveryone(String message) {
        for(ClientConnection client : clients) {   // 遍历所有客户端连接
            client.out.println(message);   // 向客户端发送消息
            client.out.flush();   // 刷新输出流
        }
    }
}
