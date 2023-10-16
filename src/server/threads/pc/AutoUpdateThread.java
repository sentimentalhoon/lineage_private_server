package server.threads.pc;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;

public class AutoUpdateThread extends Thread{
	
	private static AutoUpdateThread _instance;
	private static Logger _log = Logger.getLogger(AutoUpdateThread.class.getName());
	public static AutoUpdateThread getInstance(){
		if (_instance == null){
			_instance = new AutoUpdateThread();
			_instance.start();
		}
		return _instance;
	}	
	
	
	public AutoUpdateThread(){

	}
	

	
	public void run(){
		System.out.println(AutoUpdateThread.class.getName()  + " Start");
		while(true){
			try {
				for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
					if(pc == null || pc.getNetConnection() == null|| pc.isPrivateShop()){
						continue;
					}
					else{
						pc.updateObject();
					}
					
				}
				Thread.sleep(300);
			}
			catch(Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}
}
