package pull;

import java.io.*;   
import java.net.*;
import java.util.Map;
public class PeerServerThread extends Thread{
	private Socket client = null;
	private String originPath;
	private String mirrorPath;
	private Map<String,FileInfo> myOriginFiles;
	private String prompt;
	public PeerServerThread(Socket client,String originPath,String mirrorPath,Map<String,FileInfo> myOriginFiles,String prompt) {
		super();
		this.client = client;
		this.originPath = originPath;
		this.mirrorPath = mirrorPath;
		this.myOriginFiles = myOriginFiles;
		this.prompt = prompt;
	}
	public void run(){
		try{
			System.out.println("\nPeerServerThread: new thread started");
			//handshake communication
			PrintWriter hsout = null;
		    BufferedReader hsin = null;
			hsout = new PrintWriter(this.client.getOutputStream(), true);
			hsin = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
			//protocol: client first report filename, wait server answer OK
			String fromclient = hsin.readLine();
			if (fromclient.startsWith("check")){
				hsout.println("OK"+myOriginFiles.get(fromclient.substring(5)).getVersion());
				System.out.println("ServerThread: report version number of "+fromclient.substring(5)+" to other: "+myOriginFiles.get(fromclient.substring(5)).getVersion());
			}
			else {
				System.out.println("ServerThread: a peer says: "+fromclient);
				File file = new File(this.originPath,fromclient);
				if (file.exists()){
					hsout.println("OK");
					System.out.println("ServerThread: I say: OK, requested file in origin path");
		
					//prepare file transfer stream
					FileInputStream fileIn = new FileInputStream(file);
					System.out.println("ServerThread: sending file begin");
					byte[] buf = new byte[1024];
					int num = fileIn.read(buf);
					while(num!=-1){
						this.client.getOutputStream().write(buf,0,num);
						num = fileIn.read(buf);
					}
					fileIn.close();
				}
				else{
					file = new File(this.mirrorPath,fromclient);
					if (file.exists()){
						hsout.println("OK");
						System.out.println("ServerThread: I say: OK, requested file in mirror path");
			
						//prepare file transfer stream
						FileInputStream fileIn = new FileInputStream(file);
						System.out.println("ServerThread: sending file begin");
						byte[] buf = new byte[1024];
						int num = fileIn.read(buf);
						while(num!=-1){
							this.client.getOutputStream().write(buf,0,num);
							num = fileIn.read(buf);
						}
						fileIn.close();
					}
					else
						hsout.println("ServerThread: file unavailable");
				}
			}
			hsin.close();
			hsout.close();
			System.out.println("ServerThread: thread terminate");
			System.out.print(this.prompt);
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}

