import java.io.*;
public class Peer{
				public static void main(String[] args) throws Exception {
					File conf = new File("conf");	
					BufferedReader bw = new BufferedReader(new FileReader(conf));
					String opt = bw.readLine();
					if (opt.startsWith("pull")){
						System.out.println("Start AS PULL");	
						pull.Peer.main();
					}
					else if (opt.startsWith("push")){
						System.out.println("Start AS PUSH");	
						push.Peer.main();
					}
					else
						System.out.println("conf error");
					bw.close();
				}
}
