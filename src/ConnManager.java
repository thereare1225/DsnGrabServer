import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReadWriteLock;  
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnManager extends Thread {
	private Map<SocketChannel, Connection> connections = new Hashtable<SocketChannel, Connection>();
	private List removeList = new ArrayList();
	private ReadWriteLock lock;
	private DsnGrabUI ui;
	
	
	ConnManager(DsnGrabUI ui) {
		this.ui = ui;
		lock = new ReentrantReadWriteLock();
	}
	
	@Override
    public void run() {
		while(true) {		
			long time = System.currentTimeMillis();
			boolean isNeedUpdate = false;
			lock.writeLock().lock();
			Iterator<Map.Entry<SocketChannel, Connection>> ite = connections.entrySet().iterator();
		
			//遍历检查心跳
		    while(ite.hasNext()){
		      Map.Entry<SocketChannel, Connection> entry = ite.next();
		      SocketChannel sc = entry.getKey();
		      Connection conn = entry.getValue();
		      
		      if(time - conn.getLastRecvTime() > 20000) {
		    	isNeedUpdate = true;
		        try {
		        	System.out.println("超时断开：" + sc);
		        	if(sc != null) {
		        		sc.close();
		        		sc = null;
		        	}
		        } catch(IOException io) {
		        	
		        }
		        ite.remove();
		      }
		    }//!while
		    lock.writeLock().unlock();
		    
		    if(isNeedUpdate) {
		    	updateConns();
		    }
		    
		    try {
		    	Thread.currentThread().sleep(2000);
		    } catch(InterruptedException e) {
		    	
		    }
		}
	}
	
	public boolean recvMsg(SocketChannel sc) {
		Connection conn = (Connection)connections.get(sc);
		if(conn != null) {
			conn.lastRecvTime = System.currentTimeMillis();
			return true;
		}
		
		return false;
	}
	
	public void registerConn(SocketChannel sc, String account, String website) {
		Connection conn = (Connection)connections.get(sc);
		conn.setProperty(account, website);
		updateConns();
	}
	
	public void addConn(SocketChannel sc, String ip) {
		lock.writeLock().lock();
		connections.put(sc, new Connection(sc, System.currentTimeMillis(), ip));
		lock.writeLock().unlock();
		updateConns();
	}
	
	private void updateConns() {
		Vector<Vector<String>> datas = new Vector<Vector<String>>();
		
		Iterator<Map.Entry<SocketChannel, Connection>> ite = connections.entrySet().iterator();
		
		lock.readLock().lock();
	    while(ite.hasNext()){
	      Map.Entry<SocketChannel, Connection> entry = ite.next();
	      Connection conn = entry.getValue();
	      datas.add(conn.getProperty());
	    }//!while
	
		lock.readLock().unlock();
		ui.updateTable(datas);
	}
	
	public void delConn(SocketChannel sc) {		
		try {
			if(sc != null) {
				lock.writeLock().lock();
				connections.remove(sc);
				lock.writeLock().unlock();
				sc.close();	
				sc = null;
				updateConns();
			}
        } catch(IOException io) {
        }
		//connections.remove(sc);
		
	}	
}
