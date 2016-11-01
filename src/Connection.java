import java.nio.channels.SocketChannel;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;  
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Connection {
	SocketChannel sc = null;
	long lastRecvTime = 0;
	String ip = "null";
	String account = "null";
	String website = "null";
	private ReadWriteLock lock;
	
	Connection(SocketChannel sc, long time, String ip) {
		this.sc = sc;
		this.ip = ip;
		lastRecvTime = time;
		lock = new ReentrantReadWriteLock();
	}
	
	public void setLastRecvTime(long time) {
		lock.writeLock().lock();
		lastRecvTime = time;
		lock.writeLock().unlock();
	}
	
	public long getLastRecvTime() {
		lock.readLock().lock();
		long re = lastRecvTime;
		lock.readLock().unlock();
		return re;

	}
	
	public Vector<String> getProperty() {
		Vector<String> property = new Vector<String>();
		property.add(ip);
		property.add(account);
		property.add(website);
		return property;
	} 
	
	public void setProperty(String account, String website) {
		this.account = account;
		this.website = website;
	} 


}
