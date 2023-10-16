package server.threads.pc;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;

public class AutoSaveThread extends Thread{
	
	private static AutoSaveThread _instance;
	private static Logger _log = Logger.getLogger(AutoSaveThread.class.getName());
	private final int _saveCharTime;
	private final int _saveInvenTime;
	public static AutoSaveThread getInstance(){
		if (_instance == null){
			_instance = new AutoSaveThread();
			_instance.start();
		}
		return _instance;
	}	
	
	public AutoSaveThread(){
		_saveCharTime = Config.AUTOSAVE_INTERVAL;
		_saveInvenTime = Config.AUTOSAVE_INTERVAL_INVENTORY;
	}
	
	public void run(){
		System.out.println(AutoSaveThread.class.getName()  + " Start");
		while(true){
			try {
				for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
					if(pc == null || pc.getNetConnection() == null){
						continue;
					}
					else{
						// 캐릭터 정보
						if (_saveCharTime * 1000 < System.currentTimeMillis() - pc.getlastSavedTime()) {
							pc.save();
							pc.setlastSavedTime(System.currentTimeMillis());
						}

						// 소지 아이템 정보
						if (_saveInvenTime * 1000 < System.currentTimeMillis() - pc.getlastSavedTime_inventory()) {
							pc.saveInventory();
							pc.setlastSavedTime_inventory(System.currentTimeMillis());
						}
					}
				}
				Thread.sleep(60000);
			}
			catch(Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}

}
