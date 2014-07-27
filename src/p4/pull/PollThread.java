package pull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PollThread extends Thread{
	private int ttr;
	FileInfo fi;
	public PollThread(int ttr, FileInfo fi) {
		super();
		this.ttr = ttr;
		this.fi = fi;
	}
	public void run(){
		boolean check = true;
		while(check){
			try {
				Thread.sleep(1000*ttr);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("\nPoll(): check file "+this.fi.getName());
			try {
				Socket s = new Socket(this.fi.getOrigin_ip(),this.fi.getOrigin_port());
				//handshake communication
				PrintWriter hsout = null;
				BufferedReader hsin = null;
				hsout = new PrintWriter(s.getOutputStream(), true);
				hsin = new BufferedReader(new InputStreamReader(s.getInputStream()));
				hsout.println("check"+this.fi.getName());
				String fromserver = hsin.readLine();
				if (fromserver.startsWith("OK")){
					if (this.fi.getVersion() < Integer.parseInt(fromserver.substring(2))){
						this.fi.setState(FileInfo.State.INVALID);
						System.out.println("Poll(): invalidate my file: "+fi.getName());
						check = false;
					}
				}
				else{
					System.out.println("Poll(): origin server did not reply correctly");
				}
				hsin.close();
				hsout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
