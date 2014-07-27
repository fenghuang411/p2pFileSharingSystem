package push;

public class NeighborInfo {
	int peerid;
	String rminame;
	public Query nq;
	public NeighborInfo(int peerid, String rminame) {
		super();
		this.peerid = peerid;
		this.rminame = rminame;
	}
	public int getPeerid() {
		return peerid;
	}
	public String getRminame() {
		return rminame;
	}
}
