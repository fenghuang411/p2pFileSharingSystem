package push;

import java.net.*;
public class PeerServer extends Thread{
	private int myport;
	private String originPath;
	private String mirrorPath;
	public PeerServer(int myport,String originPath,String mirrorPath) {
		super();
		this.myport = myport;
		this.originPath = originPath;
		this.mirrorPath = mirrorPath;
	}

	public void run(){
		try{
			ServerSocket ss = new ServerSocket(this.myport);
			System.out.println("PeerServer: started and listening");
			while(true){
				new PeerServerThread(ss.accept(),this.originPath,this.mirrorPath).start();
			}
//			ss.close();
//			System.out.println("PeerServer: terminate");
		}
		catch(Exception e){
			System.out.println(e);
			
		}
	}
}

