import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.DirectoryNotEmptyException;
public class ServerClass{
    
    public String currentDirectory;
    
    public ServerClass(String currentDirectory){
        this.currentDirectory = currentDirectory;
    }
     public void cd(String rest,DataOutputStream dos){
        try{
        String cur_dir_temp=this.currentDirectory+ "/"+rest;
        if (Files.isDirectory(Paths.get(cur_dir_temp))) 
        {
            dos.writeUTF("Directory Changed to :" +cur_dir_temp );
            this.currentDirectory=cur_dir_temp;
            System.out.println("Directory Changed to :" +cur_dir_temp);
        }
        else
        {
            dos.writeUTF("Directory doesn't exist");
        }

        dos.flush();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public void get(String receiveMessage,String rest, DataOutputStream dos, DataInputStream dis){
        boolean rFile = true;
        boolean wFile = true;
        String mkdir_temp = this.currentDirectory+"/"+rest;
        File myFile = new File(this.currentDirectory+"/"+rest);  
        try{
            if(myFile.exists()){  
                if(MultiThreadServer.ReadMap.containsKey(mkdir_temp))
                {
                    rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                    wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                    while (rFile == false)
                    {
                        TimeUnit.SECONDS.sleep(1);
                        rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                        wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                        System.out.println("Write lock found Waiting the lock to release");
                    }
                }
                MultiThreadServer.ReadMap.put(this.currentDirectory+"/"+rest,true);
                MultiThreadServer.WriteMap.put(this.currentDirectory+"/"+rest,false);
                myFile = new File(this.currentDirectory+"/"+rest);
                byte[] mybytearray = new byte[(int) myFile.length()];  
                dos.writeUTF("true");
                FileInputStream fis = new FileInputStream(myFile);  
                dos.writeUTF(myFile.getName());     
                dos.writeLong(mybytearray.length);     
                dos.flush();  

                int read;

                byte[] buffer = new byte[4096];
                while ((read=fis.read(buffer)) > 0) 
                {
                    dos.write(buffer,0,read);
                }
                System.out.println("File byte array sent");
                MultiThreadServer.ReadMap.put(this.currentDirectory+"/"+rest,true);
                MultiThreadServer.WriteMap.put(this.currentDirectory+"/"+rest,true);
                dos.flush();
                fis.close();
                if((receiveMessage = dis.readUTF()) != null)
                {   
                    System.out.println(receiveMessage); 
                } 
            } else {   
                dos.writeUTF("false");
                System.out.println("File not Found");
            }
            dos.flush();
        }
        catch(Exception e)
        {   
            System.out.println(e);
        }
    }
    
    public void put(String receiveMessage, String rest, DataOutputStream dos, DataInputStream dis){
        String mkdir_temp = this.currentDirectory+"/"+rest;
        boolean rFile = true;
        boolean wFile = true;
        int bytesRead;  
        int current = 0; 
        try{
            String s=dis.readUTF();
            if(s.equals("true")) 
            {
                if(MultiThreadServer.ReadMap.containsKey(mkdir_temp))
                {
                    rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                    wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                    while (rFile == false || wFile == false)
                    {
                        TimeUnit.SECONDS.sleep(1);
                        rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                        wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                        System.out.println("Write Read/lock found Waiting the lock to release");
                    }
                }
                MultiThreadServer.ReadMap.put(this.currentDirectory+"/"+rest,false);
                MultiThreadServer.WriteMap.put(this.currentDirectory+"/"+rest,false);
                String fileName = dis.readUTF();
                File statText = new File(this.currentDirectory+"/"+fileName);    
                OutputStream output = new FileOutputStream(statText);     
                long size = dis.readLong();     
                byte[] buffer = new byte[1024];     
                while (size > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)     
                {     
                    output.write(buffer, 0, bytesRead);     
                    size -= bytesRead;     
                }
                output.flush();
                output.close();  
                dos.flush();
                System.out.println("File Transfered");
                dos.writeUTF("Transfer Complete"); 
                MultiThreadServer.ReadMap.put(this.currentDirectory+"/"+rest,true);
                MultiThreadServer.WriteMap.put(this.currentDirectory+"/"+rest,true);
            }
            else
            {
                System.out.println("File not Found");
            }
            dos.flush();
        }catch(Exception e){
            System.out.println(e);
        }

        
    }
    
    public void pwd(DataOutputStream dos){
        try{
        
        System.out.println("The current  directory is " + this.currentDirectory);
        dos.writeUTF("The current working directory is " + this.currentDirectory);
        dos.flush();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    
    public void ls(DataOutputStream dos){
        try{
        File dir = new File(this.currentDirectory);
        String[] files = dir.list();
        if (files.length == 0)
         {
            dos.writeUTF("The directory is empty");

        } else 
        {
            String str = String.join("    ", files);
            dos.writeUTF(str);
            }
        dos.flush();  
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

    }
    
   
    public void cddd(DataOutputStream dos){
        try{
        this.currentDirectory=this.currentDirectory.substring(0,this.currentDirectory.lastIndexOf('/'));
        dos.writeUTF("Directory Changed to" + this.currentDirectory);             
        dos.flush();
        System.out.println("Directory changed to" + this.currentDirectory);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    
    public void makedir(String rest,DataOutputStream dos){

        try{
        
        String mkdir_temp=this.currentDirectory+"/"+rest;
        File file = new File(mkdir_temp);
        if (!file.exists()) 
        {
        if (file.mkdir()) 
        {
            dos.writeUTF("Directory is created...");
            System.out.println("Created at "+ mkdir_temp);
        } 
        }
        else 
        {
            dos.writeUTF("Failed to create directory!!!");
        }

        mkdir_temp=null;
        dos.flush();
        }
    catch(Exception e)
        {
            System.out.println(e);
        }
    }
    
    public void delete(String rest,DataOutputStream dos){
        boolean rFile = true;
        boolean wFile = true;
        String mkdir_temp=this.currentDirectory+"/"+rest;
        File file = new File(mkdir_temp);
        try
        { 
            if(file.exists())
            {
                if(MultiThreadServer.ReadMap.containsKey(mkdir_temp))
                {
                    rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                    wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                    while (rFile == false || wFile == false)
                    {
                        rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                        wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                        System.out.println("Read/Write lock found Waiting the lock to release");
                    }
                }
                if(rFile == true && wFile == true)
                {
                    Files.deleteIfExists(Paths.get(mkdir_temp)); 
                    dos.writeUTF("Deletion successful."); 
                    System.out.println("Deleted at"+ mkdir_temp);
                    dos.flush();
                }
            }
            else
            {
                dos.writeUTF("No such file found"); 
                dos.flush();
            }

        } 
        catch(NoSuchFileException e) 
        {   try{
            dos.writeUTF("No such file/directory exists"); }
            catch(Exception e1)
        {
            System.out.println(e);
        }
        } 
        catch(DirectoryNotEmptyException e) 
        {   try{
            dos.writeUTF("Directory is not empty."); }
            catch(Exception e1)
        {
            System.out.println(e);
        }
        } 
        catch(IOException e) 
        {   try{
            dos.writeUTF("Invalid permissions."); }
            catch(Exception e1)
        {
            System.out.println(e);
        }
        }              
        mkdir_temp="null";
    }
    
    
    
    public void quit(DataOutputStream dos, DataInputStream dis){
        System.out.println("Client-Server Connection: Closing");
        try{
            dis.close();
            dos.close();
        }catch(Exception e){
        }
        System.out.println("Coonection closed!");

        

    }

    public void getand (String receiveMessage, String rest, DataOutputStream dos, DataInputStream dis)
    {   
        boolean rFile = true;
        boolean wFile = true;
        String mkdir_temp = this.currentDirectory+"/"+rest.substring(0,rest.length()-2);
        File myFile = new File(this.currentDirectory+"/"+rest.substring(0,rest.length()-2));  
        try{
            if(myFile.exists()){   
                if(MultiThreadServer.ReadMap.containsKey(mkdir_temp))
                {
                    rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                    wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                    while (rFile == false)
                    {
                        TimeUnit.SECONDS.sleep(1);
                        rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                        wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                        System.out.println("Write lock found Waiting the lock to release");
                    }
                }
                MultiThreadServer.ReadMap.put(mkdir_temp,true);
                MultiThreadServer.WriteMap.put(mkdir_temp,false);
                
                myFile = new File(this.currentDirectory+"/"+rest.substring(0,rest.length()-2));  
                byte[] mybytearray = new byte[(int) myFile.length()];  

                MultiThreadServer.commandId++;
                int current_temp_commandID=MultiThreadServer.commandId;
                System.out.println("Command ID for getand is-- "+ current_temp_commandID);
                MultiThreadServer.terminateMap.put(current_temp_commandID, true);
                dos.writeUTF("true");
                dos.writeUTF("Command ID is "+ current_temp_commandID);
                FileInputStream fis = new FileInputStream(myFile);  
                dos.writeUTF(myFile.getName());  
                System.out.println("File length == ---" + mybytearray.length);
                dos.writeLong(mybytearray.length);     
                
                dos.flush();  
                boolean flag=true;
                int read;
                byte[] buffer = new byte[1000];
                System.out.println(MultiThreadServer.terminateMap.get(current_temp_commandID));
                while ((read=fis.read(buffer)) > 0 && flag==true) 
                {  
                     boolean temp=MultiThreadServer.terminateMap.get(current_temp_commandID);
                    if(!temp)
                    {   
                        dos.flush();
                        flag=false;
                        dos.writeBoolean(flag);
                        System.out.println("Received Command in hasmap and flag value is "+ flag);
                        System.out.println("Returning from method get and");
                        MultiThreadServer.ReadMap.put(mkdir_temp,true);
                        MultiThreadServer.WriteMap.put(mkdir_temp,true);
                        dos.flush();
                        
                    
                    }
                    else{
                   
                    dos.flush();
                    dos.writeBoolean(flag);
                    dos.flush();
                    dos.write(buffer,0,read);
                    dos.flush();
                    
                    }
                }
                System.out.println("File byte array sent");
                MultiThreadServer.ReadMap.put(mkdir_temp,true);
                MultiThreadServer.WriteMap.put(mkdir_temp,true);
                dos.flush();
                fis.close();
                if((receiveMessage = dis.readUTF()) != null) //receive from server
                {   
                    System.out.println(receiveMessage); // displaying at DOS prompt
                } 
                MultiThreadServer.terminateMap.put(current_temp_commandID, false);
        } else {   
            dos.writeUTF("false");
            System.out.println("File not Found");
        }
        dos.flush();
        }
        catch(Exception e)
        {   
            System.out.println(e);
        }
        
    }

    public void putand(String receiveMessage,String rest, DataOutputStream dos, DataInputStream dis){
        String mkdir_temp = this.currentDirectory+"/"+rest;
        boolean rFile = true;
        boolean wFile = true;
        int bytesRead;  
        int current = 0; 
        try{
            String s=dis.readUTF();
            System.out.println("Value os S is-- "+s);
            if(s.equals("true")) 
            { 
                if(MultiThreadServer.ReadMap.containsKey(mkdir_temp))
                {
                    rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                    wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                    while (rFile == false || wFile == false)
                    {
                        TimeUnit.SECONDS.sleep(1);
                        rFile = MultiThreadServer.ReadMap.get(mkdir_temp);
                        wFile = MultiThreadServer.WriteMap.get(mkdir_temp);
                        System.out.println("Write Read/lock found Waiting the lock to release");
                    }
                }
                MultiThreadServer.ReadMap.put(this.currentDirectory+"/"+rest,false);
                MultiThreadServer.WriteMap.put(this.currentDirectory+"/"+rest,false);
                
                MultiThreadServer.commandId++;
                int current_temp_commandID=MultiThreadServer.commandId;
                System.out.println("Command ID for put and is-- "+ current_temp_commandID);
                MultiThreadServer.terminateMap.put(current_temp_commandID, true);
                dos.writeUTF("Command ID is "+ current_temp_commandID);              
                System.out.println("Reading File Name");
                String fileName = dis.readUTF();
                File statText = new File(this.currentDirectory+"/"+fileName); 
                OutputStream output = new FileOutputStream(statText);
                long size = dis.readLong();
                byte[] buffer = new byte[1000];     
                while (size > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)     
                {   
                    boolean temp=MultiThreadServer.terminateMap.get(current_temp_commandID);
                    output.write(buffer, 0, bytesRead);     
                    if(temp)
                    {
                   
                    dos.writeBoolean(true);
                    size -= bytesRead;
                    }
                    else if(!temp)
                    {   
                    dos.writeBoolean(false);
                    Files.deleteIfExists(Paths.get(this.currentDirectory+"/"+fileName)); 
                    MultiThreadServer.ReadMap.put(this.currentDirectory+"/"+rest,true);
                    MultiThreadServer.WriteMap.put(this.currentDirectory+"/"+rest,true);
                    System.out.println("File Deleted From Server");
                    break;
                    }   
                   TimeUnit.MILLISECONDS.sleep(15);  
                }
                output.flush();
                output.close();  
                dos.flush();
                MultiThreadServer.ReadMap.put(this.currentDirectory+"/"+rest,true);
                MultiThreadServer.WriteMap.put(this.currentDirectory+"/"+rest,true);
                System.out.println("Command Completed");
                dos.writeUTF("Command Completed"); 
            }
            else
            {
                System.out.println("File not Found");
            }
            dos.flush();
        }catch(Exception e){
            System.out.println(e);
        }

      
    }

}
