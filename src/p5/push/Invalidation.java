package push;
import java.io.Serializable;
public class Invalidation extends MessID implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String fileName;
	int version_num;
	public Invalidation(int peerid, int seqnum,String filename, int version) {
		super(peerid, seqnum);
		this.fileName = filename;
		this.version_num = version;
		// TODO Auto-generated constructor stub
	}
	public String getFileName() {
		return fileName;
	}
	public int getVersion_num() {
		return version_num;
	}

	
}
