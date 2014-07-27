package push;
import java.io.Serializable;
public class FileInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public enum State{
		VALID,INVALID,TTR_EXPIRE
	};
	String name;
	int version;
	int origin;
	State state;
	public FileInfo(String name, int origin) {
		super();
		this.name = name;
		this.version = 0;
		this.origin = origin;
		this.state = State.VALID;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public String getStateName(){
		switch (this.state){
		case VALID: 
			return "VALID";
		case INVALID:
			return "INVALID";
		case TTR_EXPIRE:
			return "TTR_EXPIRE";
		default:
			System.out.println("File state error");
			return null;
		}
	}
	public String getName() {
		return name;
	}
	public int getOrigin() {
		return origin;
	}
	public String toString(){
		return " -> version:"+this.version+ " orgin-peer:"+this.origin+" state:"+this.getStateName()+"\n";
	}
}
