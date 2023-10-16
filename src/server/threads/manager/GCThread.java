package server.threads.manager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GCThread extends Thread{
	private static GCThread _instance;
	private static Logger _log = Logger.getLogger(GCThread.class.getName());
	public static GCThread getInstance(){
		if (_instance == null){
			_instance = new GCThread();
			_instance.start();
		}
		return _instance;
	}	
	
	public void run(){
		System.out.println(GCThread.class.getName()  + " Ω√¿€");
		while(true){
			try {
					System.gc();
					Thread.sleep(7200000);
			}
			catch(Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}

}
