package pull;

import java.io.Serializable;
import java.util.ArrayList;

public class MessID implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int peerid;
	int seqnum;//avoid re-enter same job
	ArrayList<Integer> visited;
	public MessID(int peerid, int seqnum) {
		super();
		this.peerid = peerid;
		this.seqnum = seqnum;
		this.visited = new ArrayList<Integer>();
		this.visited.add(this.peerid);
	}
	public boolean checkVisited(int pid) {
		if (visited.contains((Integer)pid))
			return false; //visted this peer, pass
		else return true; //you can do a query here
	}
	public void addVisited(int pid) {
		visited.add((Integer)pid);
	}
	public int getPeerid() {
		return peerid;
	}
	public int getSeqnum() {
		return seqnum;
	}
	public String toString(){
		return "Meg[peer"+peerid+"'s #"+seqnum+"]";
		
	}
	public ArrayList<Integer> getVisited() {
		return visited;
	}
}
