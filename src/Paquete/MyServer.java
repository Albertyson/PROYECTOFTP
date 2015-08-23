package Paquete;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyServer {
    public static final int PORT=20;
    public static final int MAX_CONCURRENT_CONN=100;
    public static final int DEFAULT_CONCURRENT_CONN=10;
    private ServerSocket srv;
    private ArrayList<User> usersList;


    /*public static void main(String[]args){
        MyServer srv=new MyServer();
        try {
            srv.init();
            srv.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    public void setUsersList(ArrayList<User> usersList){
        this.usersList=usersList;
    }
    public void init() throws IOException{
        srv=new ServerSocket(PORT,DEFAULT_CONCURRENT_CONN);
    }
    public void start(){
        while(true){
            try {
                System.out.println("waiting for a new connection...");
                Socket s=srv.accept();
                System.out.println("Incomming connection from " + s.getRemoteSocketAddress());
                ThreadForClient hiloCliente = new ThreadForClient(s,usersList);
                hiloCliente.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void stop(){
        try {
            srv.close();
        } catch (IOException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}