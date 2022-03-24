import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.DirectoryNotEmptyException;
  
 
public class MultiThreadServer 
{   
    public static Integer commandId=0;
    public Thread t2;
    public static Map<Integer, Boolean> terminateMap= new HashMap<Integer, Boolean>();
    public static Map<String, Boolean> ReadMap= new HashMap<String, Boolean>();
    public static Map<String, Boolean> WriteMap= new HashMap<String, Boolean>();
    
    
    public static void main(String[] args) throws IOException  
    { 
        
        ServerSocket serversocket1 = new ServerSocket(5000); 
        
        ServerSocket serversocket2 = new ServerSocket(6000);
          
         
        while (true)  
        { 
            Socket socket1 = null; 
            Socket socket2 = null;
              
            try 
            { 
                
                socket1 = serversocket1.accept(); 
                socket2 = serversocket2.accept();
                  
                System.out.println("A new client is connected to sockets: " + socket1+ " and " +socket2); 
                
                    DataInputStream dis= new DataInputStream(socket1.getInputStream());
                    DataOutputStream dos=new DataOutputStream(socket1.getOutputStream());
                    DataInputStream dis2 = new DataInputStream(socket2.getInputStream());
                    System.out.println("Assigning a new thread for this client at Nport"); 

                   
                    Thread t = new ClientHandler(dis,dos);
                    Thread t2 = new Client_Terminate(dis2);

                    t.start(); 
                    t2.start();
                }
                catch (Exception e){ 
                socket1.close(); 
                socket2.close();
                e.printStackTrace(); 
            } 
        } 
    } 
} 
  

class ClientHandler extends Thread  
{ 
   
    final DataInputStream dis;
    final DataOutputStream dos;
   
    ServerClass server = new ServerClass(System.getProperty("user.dir"));
      
  
 
    public ClientHandler(DataInputStream dis, DataOutputStream dos) 
    { 
        this.dis=dis;
        this.dos=dos;
    } 
  
    @Override
    public void run()  
    {   String rmsg, smsg,rest,word,cur_dir_temp, mkdir_temp,lastlast="";      
        String cur_dir=System.getProperty("user.dir");
        boolean input = true;
        
        while(input)
        {   
        try
        {  
            rmsg = dis.readUTF();
            while (rmsg!=null)
            {
            int index = rmsg.indexOf(' ');
                if (index > -1) 
                { 
                word=rmsg.substring(0, index);
                rest= rmsg.substring(index+1);
                lastlast=Character.toString(rest.charAt(rest.length() - 1));
                System.out.println("Last character is : "+lastlast);
                System.out.println("Rest is : " + rest);
                } else 
                {
                word=rmsg; 
                rest="";
                }
            if(rmsg != null && word.equals("pwd"))  
            {
                 server.pwd(this.dos); 
            }         
            else if(rmsg != null && word.equals("ls"))
            {
                server.ls(dos); 
            }
            else if (rmsg !=null && word.equals("cd"))
            {
                server.cd(rest,dos);
            }

            else if (rmsg !=null && word.equals("cd.."))
            {
                server.cddd(dos);
            }
            else if (rmsg !=null && word.equals("mkdir"))
            {
                server.makedir(rest,dos);
            }   
            else if (rmsg !=null && word.equals("delete"))
            {   
                server.delete(rest,dos);
            }   
            else if (rmsg !=null && word.equals("get")&& !lastlast.equals("&"))
            {  
                server.get(rmsg,rest,dos,dis);
                
            }   
            else if (rmsg!=null && word.equals("get") && lastlast.equals("&"))
            {  
                server.getand(rmsg,rest,dos,dis);
            }
            else if (rmsg !=null && word.equals("put") && !lastlast.equals("&"))
            {   
                server.put(rmsg,rest,dos,dis);
            }
            else if (rmsg !=null && word.equals("put")&& lastlast.equals("&"))
            {   
                System.out.println("Into ifelse of put and ");
                server.putand(rmsg,rest,dos,dis);
            }

            else if (rmsg !=null && word.equals("quit"))
            {
                
                server.quit(dos,dis);
                input = false;
                break;
            }
            rmsg=null;
            
          }       
        } 
         
            catch (Exception e)
            { 
                e.printStackTrace(); 
            } 
        }

    } 

}
class Client_Terminate extends Thread
{
    final DataInputStream dis2;

    public Client_Terminate(DataInputStream dis2)
    {
        this.dis2=dis2;
    }
    @Override
    public void run()
    { 
         String receive_terminate, word, rest;
         try
         {
            while(true)
            {
                receive_terminate=this.dis2.readUTF();
                if(receive_terminate!=null)
                {   
                    System.out.println("Terminate command received");
                    int index = receive_terminate.indexOf(' ');
                    word=receive_terminate.substring(0, index);
                    System.out.println("Command is : "+word);
                    rest= receive_terminate.substring(index+1);
                    System.out.println("ID is : "+rest);
                    MultiThreadServer.terminateMap.put(Integer.parseInt(rest), false);
                    System.out.println(MultiThreadServer.terminateMap.get((Integer.parseInt(rest))));
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
