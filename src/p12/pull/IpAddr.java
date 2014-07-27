package pull;
import java.io.Serializable;
import java.net.InetAddress;
public class IpAddr implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	InetAddress ia;
	int port;
	FileInfo fi;
	int state;//1 valid 0 invalid
	public IpAddr(InetAddress ia, int port, FileInfo fi, int state) {
		super();
		this.ia = ia;
		this.port = port;
		this.fi = fi;
		this.state = state;
	}
	public int getState() {
		return state;
	}
	public FileInfo getFi() {
		return fi;
	}
	public InetAddress getIa() {
		return ia;
	}
	public int getPort() {
		return port;
	}
	public  String toString() {
		return ia.toString()+" @ "+port;
	}
	
}
