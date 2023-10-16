/**SimpleFirewall ����Ŭ����**/
package l1j.server.server;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MiniClient extends Thread{
	/** ����ǻ���� ����ȣ��Ʈ**/
	private String serverIP = "118.38.92.240";
	/**���� ���̾�� ��Ʈ**/
	private final int serverPort = 2009;
	/** ��� ��Ʈ�� **/ 
	private PrintWriter printWriter = null;
	boolean listenFlag = true;

	/**MiniClient Ŭ������ü�� ��� �ν��Ͻ�**/
	private static MiniClient _instance;
	/**�Ŀ� ���������� �˻�**/

	public static MiniClient getInstance() {
		if (_instance == null) {
			_instance = new MiniClient();
		}
		return _instance;
	}

	public MiniClient() {
		//this.serverIP = serverIP;
		@SuppressWarnings("unused")
		boolean isInitEnv = this.initEnv();  
	}

	/**������ ��**/
	public void run() {  
		try{
			while(listenFlag == true) {
				MiniClient.sleep(2000);
			}
		}catch(Exception e) {   
			listenFlag = false;    
			e.printStackTrace();
		}   
	}

	/** ���Ͽ��� �� ��� ��Ʈ�� ���**/
	private boolean initEnv() {
		try {
			Socket clientSocket = new Socket(serverIP,serverPort);  
			printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
			System.out.println( "[MiniClient] SimpleFirewall ���� �Ϸ�!" );
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();   
			if(printWriter != null){
				try{
					printWriter.close();
				}catch(Exception printWriterException){
					printWriterException.printStackTrace();     
				}
			}   
			return false;
		}
	}

	/** ������ IP ���� **/
	public void MessageToServer(String IP){
		try {
			printWriter.println("L@"+IP);
			printWriter.flush();   
			System.out.println("[MiniClient] ("+ IP +")  SimpleFirewall ���ܿϷ�!" );
		} catch (Exception e){   
			e.printStackTrace();
		}finally{   
		}
	}

}