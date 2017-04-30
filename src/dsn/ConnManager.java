package dsn;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnManager extends Thread {
	private Map<String, Connection> connections = new Hashtable<String, Connection>();
	private ReadWriteLock lock;
	private DsnGrabUI ui;
	
	
	ConnManager(DsnGrabUI ui) {
		this.ui = ui;
		lock = new ReentrantReadWriteLock();
	}
	
	public void registerConn(String ip, String account, String website) {
		Connection conn = (Connection)connections.get(ip);
		conn.setProperty(account, website);
		updateConns();
	}
	
	public void addConn(String ip) {
		if(!connections.containsKey(ip)) {
			lock.writeLock().lock();
			connections.put(ip, new Connection(ip));
			lock.writeLock().unlock();
			updateConns();
		}
	}
	
	private void updateConns() {
		Vector<Vector<String>> datas = new Vector<Vector<String>>();
		
		Iterator<Map.Entry<String, Connection>> ite = connections.entrySet().iterator();
		
		lock.readLock().lock();
	    while(ite.hasNext()) {
	      Map.Entry<String, Connection> entry = ite.next();
	      Connection conn = entry.getValue();
	      datas.add(conn.getProperty());
	    }//!while
	
		lock.readLock().unlock();
		ui.updateTable(datas);
	}
	
	public void delConn(String ip) {		
		if(ip != null && connections.containsKey(ip)) {
			lock.writeLock().lock();
			connections.remove(ip);
			lock.writeLock().unlock();
			updateConns();
		}
	}	
}
