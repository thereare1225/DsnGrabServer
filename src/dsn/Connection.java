package dsn;
import java.util.Vector;

public class Connection {
	String ip = "null";
	String account = "null";
	String website = "null";
	
	Connection(String ip) {
		this.ip = ip;
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
