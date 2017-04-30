package dsn;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import netty.NettyServer;


public class DsnGrabServer {
 
    private ConnManager connManager = null;
    private NettyServer nettyServer = null;
    
    DsnGrabServer() {
    	connManager = new ConnManager(new DsnGrabUI());
    	nettyServer = new NettyServer(connManager);
    	try {
    		nettyServer.run();
    	} catch (Exception e) {
    		
    	}
    }
  
    public static void main(String [] args) throws IOException{
    	//�ض������
    	try {  
	    	//����·��  
	    	File dir = new File("log");
	        if (dir.exists()) {   
	        } 
	        else {
	        	dir.mkdirs();
	        }
	         
	        //������ض����ļ�
	    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//�������ڸ�ʽ
	    	PrintStream ps=new PrintStream("log/" + df.format(new Date()) + ".txt");  
	    	System.setOut(ps);
	    	System.setErr(ps);
	    } catch (FileNotFoundException e) {  
	    	e.printStackTrace();
		} 
    	
    	new DsnGrabServer();
    }
 
}