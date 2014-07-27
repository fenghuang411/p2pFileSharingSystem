package pull;
import java.rmi.*;
import java.util.ArrayList;
public interface Query extends Remote{
	public ArrayList<IpAddr> query(MessID mi, int ttl,String filename) throws RemoteException;
	public boolean checkAlive() throws RemoteException;
}
