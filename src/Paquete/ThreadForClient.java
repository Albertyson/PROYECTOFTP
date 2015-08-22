package Paquete;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadForClient extends Thread{
   
   private Socket s;
   
   public ThreadForClient(Socket s){
      this.s=s;
   }
   
   public void run(){
      BufferedReader in=null;
      PrintWriter out=null;
      
      try {
         in=new BufferedReader(new InputStreamReader(s.getInputStream()));
         out=new PrintWriter(s.getOutputStream(),true);
         String fromClient=null;
         out.println("ok\r\n");
         out.flush();
         out.println("220 localhost connected\r\n");
         out.println("331 Anonym no password needed\r\n");
         
         out.println("211-Features:");
         out.println("MDTM");
         out.println("REST STREAM");
         out.println("SIZE");
         out.println("MLST type*;size*;modify*;");
         out.println("MLSD");
         out.println("UTF8");
         out.println("CLNT");
         out.println("MFMT");
         out.println("211 End");
         out.println("230 OK. Current directory is /");
         
         boolean connected = true;
         
         while (connected) {
            fromClient = in.readLine();
            System.out.println("From client: "+fromClient);
            
            
            if(fromClient!=null ){
               if(fromClient.equals("END")){
                  connected = false;
               }
               else{
                  processCommand(fromClient, out);
               }
               
            }
         }
         
         
         
         
      } catch (IOException e) {
         String msg = e.getMessage();
         e.printStackTrace();
      }finally{
         try {
            if(in!=null)
               in.close();
            if(out!=null)
               out.close();
            if(s!=null)
               s.close();
         } catch (IOException e) {
            String msg = e.getMessage();
            e.printStackTrace();
         }
      }
   }
   
   private void processCommand(String command, PrintWriter out){
      
      String[] args = command.split(" ");
      
      switch(args[0]){
      
      case "CREATE":   createDirectory(args[1]);
                  break;
      default:
            break;
      }
      
   }
   
   private void createDirectory(String directoryName) {
      File theDir = new File(directoryName);

      // if the directory does not exist, create it
      if (!theDir.exists()) {
         System.out.println("creating directory: " + directoryName);
         boolean result = theDir.mkdir();

         if (result) {
            System.out.println("DIR created");
         }
      }
   }
}