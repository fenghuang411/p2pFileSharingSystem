package pull;

import java.io.*;   
import java.net.*;
import java.util.*;


public class PeerClient extends Thread{
	private InetAddress targetAddr;
	private int targetPort;
	private String fileName;
	private String mypath;
	private Map<String,FileInfo> myFiles;
	private FileInfo fi;
	private int ttr;
	private String prompt;
	
	public PeerClient(InetAddress targetAddr, int targetPort, String fileName,String mypath,Map<String,FileInfo> myfiles,FileInfo fi,int ttr,String prompt) {
		super();
		this.targetAddr = targetAddr;
		this.targetPort = targetPort;
		this.fileName = fileName;
		this.mypath = mypath;
		this.myFiles = myfiles;
		this.fi = fi;
		this.ttr = ttr;
		this.prompt = prompt;
	}
	public void run(){
		try{
			Socket s = new Socket(this.targetAddr,this.targetPort);
			//handshake communication
			PrintWriter hsout = null;
		    BufferedReader hsin = null;
			hsout = new PrintWriter(s.getOutputStream(), true);
			hsin = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//protocol: client first report filename, wait server answer OK
			hsout.println(this.fileName);
			System.out.println("PeerClient: I am requesting "+ this.fileName);
			String fromserver = hsin.readLine();
			
			
			if(fromserver.equals("OK")){
				System.out.println("PeerClient: my download request granted");
				
				//prepare output to filestream
				File file = new File(this.mypath,this.fileName);

				if(file.exists())
					file.delete();
				file.createNewFile();
				RandomAccessFile distFile = new RandomAccessFile(file, "rw");
				//data transmission
				byte[] buf = new byte[1024];
				int num = s.getInputStream().read(buf);
				while(num!=-1){
					distFile.write(buf,0,num);
					num =  s.getInputStream().read(buf);
				}
				//complete task
				//in.close();
				distFile.close();
				s.close();
				}
			else{
				System.out.println("PeerClient: my download denied, because remote "+fromserver);
			}
			hsin.close();
			hsout.close();
			myFiles.put(fileName,this.fi);
			new PollThread(this.ttr,this.myFiles.get(fileName)).start();
			System.out.print(this.prompt);
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
}
