package push;
import java.io.*;   
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import javax.naming.*;

//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class Peer extends UnicastRemoteObject implements Query{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Map<String,FileInfo> myOriginFiles;
	Map<String,FileInfo> myMirrorFiles;
	ArrayList<NeighborInfo> myNeighbors;
	String originPath;
	String mirrorPath;
	NeighborInfo myInfo;//storing my information, just use the same structure
	int myPort;
	int myTtl;
	InetAddress myIp = InetAddress.getLocalHost();
	// in the ctor, local files gathered in list, and neighbors gathered in list
	public Peer() throws Exception {
		super();
		//read conf file and get neighbor info
		File conf = new File("./push/conf");
		BufferedReader bw = new BufferedReader(new FileReader(conf));
		myNeighbors = new ArrayList<NeighborInfo>();
		int nid;
		String nname;
		int neighborNum;
		//read my profile
		int tempid = Integer.parseInt(bw.readLine().substring(6));
		myInfo = new NeighborInfo(tempid,"peer"+tempid);
		myPort = Integer.parseInt(bw.readLine().substring(7));
		myTtl = Integer.parseInt(bw.readLine().substring(4));
		System.out.println("Peer(): from conf: myid="+myInfo.getPeerid()+" myname="+myInfo.getRminame()+" myport="+myPort+",ttl="+myTtl);
		//read neighbor profile
		neighborNum = Integer.parseInt(bw.readLine().substring(10));
		System.out.println("Peer(): Found "+neighborNum+" neighbors" );
		while(neighborNum>0){
			nid = Integer.parseInt(bw.readLine().substring(7));
			nname = "peer"+nid;
			myNeighbors.add(new NeighborInfo(nid, nname));
			System.out.println("Peer(): Found neighbor:"+nid+" = "+nname );
			neighborNum -= 1;
		}
		bw.close();
		//gather local files
		originPath = "./originfiles/";
		mirrorPath = "./mirrorfiles/";
		File file = new File(originPath);
		myOriginFiles = new HashMap<String, FileInfo>();
		myMirrorFiles = new HashMap<String, FileInfo>();
		for (File f : file.listFiles()){//new TestFilter() to filter file names
				myOriginFiles.put(f.getName(),new FileInfo(f.getName(), myInfo.getPeerid()));
		}
		System.out.println("Peer(): Found origin files: \n"+myOriginFiles);
	}
	
	public static void main() throws Exception  {
		// TODO Auto-generated method stub
		int messeq = 0;
		int ivseq = 0;
		Query p = new Peer();
		//config initial ttl quatum
		int ttl = ((Peer)p).myTtl;
		//start server thread
		PeerServer ps = new PeerServer(((Peer)p).myPort,((Peer)p).originPath,((Peer)p).mirrorPath);
		ps.start();
		//create local stub, register to rmi
		Context namingContext=new InitialContext();
	    namingContext.rebind( "rmi:"+((Peer)p).myInfo.getRminame(), p );
	    System.out.println( "main(): rmi "+((Peer)p).myInfo.getRminame()+" registed" );
	    //obtain all neighbor's remote object
	    //rendezvous
	    String url="rmi://localhost/";
		Context remoteContext=new InitialContext();
	    for (NeighborInfo n : ((Peer)p).myNeighbors){
	    	while(true){
	    		try {
	    			n.nq = (Query)remoteContext.lookup(url+n.rminame);
	    			n.nq.checkAlive();
	    			break;
	    		} catch (Exception e) {
	    			// TODO Auto-generated catch block
	    			System.out.println("main(): neighbor "+n.rminame+" can not be reached now, wait 5s and retry");
	    			Thread.sleep(5000);
	    		}
	    	}
	    }
	    System.out.println("main(): all neighbors are online");
	    
		// prompt commands
	    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	    while(true){
	    	System.out.print("MyMiniGnutella(peer"+((Peer)p).myInfo.getPeerid()+")->");
	    	String cmdinput = input.readLine();
	    	
	    	if (cmdinput.equals("exit")){
				break;
			}
	    	else if (cmdinput.startsWith("add")){
				String fn = cmdinput.substring(4);
				((Peer)p).myOriginFiles.put(fn,new FileInfo(fn,((Peer)p).myInfo.getPeerid()));
				continue;
			}
	    	else if (cmdinput.startsWith("del")){
				String fn = cmdinput.substring(4);
				if (((Peer)p).myMirrorFiles.containsKey(fn)){
					((Peer)p).myMirrorFiles.remove(fn);
					File f = new File(((Peer)p).mirrorPath,fn);
					f.delete();
				}
				//del can only be done in mirror copies, one can not delete its origin files
				else if (((Peer)p).myOriginFiles.containsKey(fn)){
					System.out.println("you can not delete your origin file");
				}
				else{
					System.out.println("filename not found to delete");
				}	
				continue;
			}
	    	else if (cmdinput.startsWith("list")){
	    		//checking my list
	    		File origFiles = new File(((Peer)p).originPath);
	    		System.out.println("in your origin folder:");
	    		for (File f : origFiles.listFiles()){
	    			System.out.println(f.getName()+((Peer)p).myOriginFiles.get(f.getName()));

	    		}
	    		File mirrFiles = new File(((Peer)p).mirrorPath);
	    		System.out.println("in your mirror folder:");
	    		for (File f : mirrFiles.listFiles()){
	    			System.out.println(f.getName()+((Peer)p).myMirrorFiles.get(f.getName()));
	    		}
				continue;
			}
	    	else if (cmdinput.equals("downloadall")){
	    		System.out.println("randomly download 10 files");
	    		Random randomGenerator = new Random();
	    		int loop = 0;
	    		while(loop++ <10){
	    		int fileint = randomGenerator.nextInt(119)+10;
	    		String fn = Integer.toString(fileint);
	    		if (((Peer)p).myOriginFiles.containsKey(fn) || (((Peer)p).myMirrorFiles.containsKey(fn)&&((Peer)p).myMirrorFiles.get(fn).getState()==FileInfo.State.VALID)){
	    			System.out.println("main(): queried target in local");
	    			continue;
	    		}
	    		else{
	    			ArrayList<IpAddr> ret = new ArrayList<IpAddr>();
	    			//query neighbors
	    			MessID m = new MessID(((Peer)p).myInfo.getPeerid(), messeq++);
	    			for (NeighborInfo n : ((Peer)p).myNeighbors){
	    				ret.addAll(n.nq.query(m, ttl, fn));
	    			}
	    			System.out.println("main(): query result: "+ ret);
	    			if (ret.isEmpty()){
	    				System.out.println("main(): Did not find a seed, try higher ttl");
	    			}
	    			else{
	    				//pick a random peer to connect
	    				//Random randomGenerator = new Random();
	    				int index = randomGenerator.nextInt(ret.size());
	    				IpAddr targetpeer = ret.get(index);
	    				System.out.println("main(): pick seed "+targetpeer.ia+" @ "+targetpeer.port+" to connect");
	    				//fork download thread
	    				PeerClient pc = new PeerClient(targetpeer.ia, targetpeer.port, fn,((Peer)p).mirrorPath,((Peer)p).myMirrorFiles,targetpeer.getFi());
	    				System.out.println("main(): creating download thread: "+fn);
	    				pc.start();
	    				Thread.sleep(200);// wait a little bit for download, print promt sign properly
	    			}
	    		}
	    		}
	    	}
	    	else if (cmdinput.equals("testdownload")){

	    		int cnt_max = 200;
	    		int invalid_count = 0;
	    		int all_count = 0;
	    		Random randomGenerator = new Random();
	    			while((cnt_max--)>0){//test 200 times
	    				//random file name from 10-129, each peer has 10 files, starting from 10
	    				
	    				int fileint = randomGenerator.nextInt(119)+10;
	    				String fn =  Integer.toString(fileint);
	    				if (((Peer)p).myOriginFiles.containsKey(fn)){
	    	    			System.out.println("main(): queried target in local");
	    	    			continue;
	    	    		}
	    				ArrayList<IpAddr> ret = new ArrayList<IpAddr>();
	    				//query neighbors
	    				for (NeighborInfo n : ((Peer)p).myNeighbors){
	    					MessID m  = new MessID(((Peer)p).myInfo.getPeerid(), messeq++);
	    					ret.addAll(n.nq.query(m, ttl, fn));
	    					all_count += ret.size();
	    					for (IpAddr i: ret){
	    						if (i.getState()== 0)
	    							invalid_count += 1;
	    					}
	    					
	    				}
	    				System.out.println("main(): query result: "+ ret);
	    				if (ret.isEmpty()){
	    					System.out.println("main(): Did not find a seed, try higher ttl");
	    				}
	    				else{
	    					//found seed
	    					System.out.println("main(): Found seed(s): "+ ret);
	    				}
	    			}
	    			
	    		    System.out.println("main(): test 200 times : all return="+all_count + "invalid="+invalid_count+ "% = "+ (float)invalid_count/(float)all_count);
	    	}
	    	else if (cmdinput.startsWith("download")){
	    		String fn = cmdinput.substring(9);
	    		if (((Peer)p).myOriginFiles.containsKey(fn) || (((Peer)p).myMirrorFiles.containsKey(fn)&&((Peer)p).myMirrorFiles.get(fn).getState()==FileInfo.State.VALID)){
	    			System.out.println("main(): queried target in local");
	    			continue;
	    		}
	    		else{
	    			ArrayList<IpAddr> ret = new ArrayList<IpAddr>();
	    			//query neighbors
	    			MessID m = new MessID(((Peer)p).myInfo.getPeerid(), messeq++);
	    			for (NeighborInfo n : ((Peer)p).myNeighbors){
	    				ret.addAll(n.nq.query(m, ttl, fn));
	    			}
	    			for (IpAddr ip : ret){
	    				if (ip.getState() == 0)
	    					ret.remove(ip);
	    			}
	    			System.out.println("main(): query result: "+ ret);
	    			if (ret.isEmpty()){
	    				System.out.println("main(): Did not find a seed, try higher ttl");
	    			}
	    			else{
	    				//pick a random peer to connect
	    				Random randomGenerator = new Random();
	    				int index = randomGenerator.nextInt(ret.size());
	    				IpAddr targetpeer = ret.get(index);
	    				System.out.println("main(): pick seed "+targetpeer.ia+" @ "+targetpeer.port+" to connect");
	    				//fork download thread
	    				PeerClient pc = new PeerClient(targetpeer.ia, targetpeer.port, fn,((Peer)p).mirrorPath,((Peer)p).myMirrorFiles,targetpeer.getFi());
	    				System.out.println("main(): creating download thread: "+fn);
	    				pc.start();
	    				Thread.sleep(200);// wait a little bit for download, print promt sign properly
	    			}
	    		}
	    	}
	    	else if (cmdinput.equals("modifyall")){
	    		for (String fn: ((Peer)p).myOriginFiles.keySet()){
	    			FileWriter fw = new FileWriter(((Peer)p).originPath+fn,true); //the true will append the new data
	    			fw.write("\nadd a line\n");//appends the string to the file
	    			fw.close();
	    			((Peer)p).myOriginFiles.get(fn).setVersion(((Peer)p).myOriginFiles.get(fn).getVersion()+1);
	    	    // do push invalidation
	    			Invalidation iv = new Invalidation(((Peer)p).myInfo.getPeerid(), ivseq++, fn,((Peer)p).myOriginFiles.get(fn).getVersion() );
	    			for (NeighborInfo n : ((Peer)p).myNeighbors){
	    				n.nq.invalidate(iv, ttl);
	    			}
	    		}
	    	}
	    	
	    	else if (cmdinput.startsWith("modify")){
	    		String fn = cmdinput.substring(7);
	    		if (((Peer)p).myOriginFiles.containsKey(fn)){
	    			FileWriter fw = new FileWriter(((Peer)p).originPath+fn,true); //the true will append the new data
	    			fw.write("\nadd a line\n");//appends the string to the file
	    			fw.close();
	    			((Peer)p).myOriginFiles.get(fn).setVersion(((Peer)p).myOriginFiles.get(fn).getVersion()+1);
	    	    // do push invalidation
	    			Invalidation iv = new Invalidation(((Peer)p).myInfo.getPeerid(), ivseq++, fn,((Peer)p).myOriginFiles.get(fn).getVersion() );
	    			for (NeighborInfo n : ((Peer)p).myNeighbors){
	    				n.nq.invalidate(iv, ttl);
	    			}
	    		}
	    		else{
	    			System.out.println("filename is not an origin file, can not modify");
	    		}
	    	}	
	    	else {
				System.out.println("Usage: \nadd filename\ndel filename\nmodify filename\ndownload filename\ntest filename\nlist\nexit\nothers=help\n");
			}
	    	
	    }
	}

	@Override
	public synchronized ArrayList<IpAddr> query(MessID mi, int ttl, String filename)
			throws RemoteException {
		// TODO Auto-generated method stub
		ArrayList<IpAddr> ret = new ArrayList<IpAddr>();
		// decision spread or not
		ttl -= 1;
		mi.addVisited(this.myInfo.getPeerid());
		if (ttl > 0){// if spread the query
			System.out.println("\nquery(): ttl = "+ttl+", spreading to neighbors");
			System.out.println("query(): "+mi+" visited "+ mi.getVisited());
			for (NeighborInfo n : myNeighbors){// Iterating neighbors
				if (mi.checkVisited(n.peerid)){// if mess didn't reach this neighbor
					
					System.out.println("query(): forward to peer"+n.peerid);
					ArrayList<IpAddr> temp = n.nq.query(mi, ttl, filename);
					ret.addAll(temp);//remote invoke query recursively
					System.out.println("query(): my neighbor "+n.getRminame()+" gives me "+temp);
				}
			}
		}
		else {
			System.out.println("\nquery(): ttl expires, no further spreading");
		}
		if (myOriginFiles.containsKey(filename)&& myOriginFiles.get(filename).getState()==FileInfo.State.VALID){
			ret.add(new IpAddr(myIp, myPort,myOriginFiles.get(filename),1));
			System.out.println("query(): I have origin file "+ filename);
		}
		else if (myMirrorFiles.containsKey(filename)){
			if (myMirrorFiles.get(filename).getState()==FileInfo.State.VALID){
				ret.add(new IpAddr(myIp, myPort,myMirrorFiles.get(filename),1));
				System.out.println("query(): I have VALID mirror file "+ filename);
			}
			else{
			//	ret.add(new IpAddr(myIp, myPort,myMirrorFiles.get(filename),0));
				System.out.println("query(): I have INVALID mirror file "+ filename);
			}
		}
		else{
			System.out.println("query(): I DON'T have the file "+ filename);
		}
		System.out.print("MyMiniGnutella(peer"+((Peer)this).myInfo.getPeerid()+")->");
		System.out.flush();
		return ret;
	}

	@Override
	public synchronized boolean checkAlive() throws RemoteException {
		// TODO Auto-generated method stub
		return true;
	}
	
	public synchronized void invalidate(Invalidation iv, int ttl) throws RemoteException{
		// TODO Auto-generated method stub
		// decision spread or not
		ttl -= 1;
		iv.addVisited(this.myInfo.getPeerid());
		if (ttl > 0){// if spread the invalidation
			System.out.println("\ninvalidate(): ttl = "+ttl+", spreading to neighbors");
			System.out.println("invalidate(): "+iv+" visited "+ iv.getVisited());
				for (NeighborInfo n : myNeighbors){// Iterating neighbors
					if (iv.checkVisited(n.peerid)){// if mess didn't reach this neighbor	
						System.out.println("invalidate(): forward to peer"+n.peerid);
						n.nq.invalidate(iv, ttl);
					}
				}
		}
		else {
				System.out.println("\ninvalidate(): ttl expires, no further spreading");
		}
		if (myMirrorFiles.containsKey(iv.getFileName())){
			if (myMirrorFiles.get(iv.getFileName()).getVersion() < iv.getVersion_num())
				//mark dirty(invalid)
				myMirrorFiles.get(iv.getFileName()).setState(FileInfo.State.INVALID);
				System.out.println("invalidate(): invalidate my file "+ iv.getFileName());
		}
		else {
			System.out.println("invalidate(): I DON'T have the file "+ iv.getFileName());
		}
		System.out.print("MyMiniGnutella(peer"+((Peer)this).myInfo.getPeerid()+")->");
		System.out.flush();
		
	}
}

