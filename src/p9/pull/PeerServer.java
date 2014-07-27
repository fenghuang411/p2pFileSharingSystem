package pull;

import java.net.*;
import java.util.Map;
public class PeerServer extends Thread{
	private int myport;
	private String originPath;
	private String mirrorPath;
	private Map<String,FileInfo> myOriginFiles;
	private String prompt;
	public PeerServer(int myport,String originPath,String mirrorPath,Map<String,FileInfo> myOriginFiles,String prompt) {
		super();
		this.myport = myport;
		this.originPath = originPath;
		this.mirrorPath = mirrorPath;
		this.myOriginFiles = myOriginFiles;
		this.prompt = prompt;
	}

	public void run(){
		try{
			ServerSocket ss = new ServerSocket(this.myport);
			System.out.println("PeerServer: started and listening");
			while(true){
				new PeerServerThread(ss.accept(),this.originPath,this.mirrorPath, this.myOriginFiles,this.prompt).start();
			}
//			ss.close();
//			System.out.println("PeerServer: terminate");
		}
		catch(Exception e){
			System.out.println(e);
			
		}
	}
}

