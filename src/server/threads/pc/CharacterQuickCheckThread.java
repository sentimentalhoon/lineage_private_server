package server.threads.pc;

import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;

public class CharacterQuickCheckThread extends Thread{

	private static CharacterQuickCheckThread _instance;
	private static Logger _log = Logger.getLogger(CharacterQuickCheckThread.class.getName());

	public static CharacterQuickCheckThread getInstance(){
		if (_instance == null){
			_instance = new CharacterQuickCheckThread();
			_instance.start();
		}
		return _instance;
	}	

	public CharacterQuickCheckThread(){ 	}

	public void run(){
		System.out.println(CharacterQuickCheckThread.class.getName()  + " Start");
		while(true){
			try {
				for(L1PcInstance _client : L1World.getInstance().getAllPlayers()){
					if(_client.isPrivateShop() || _client.noPlayerCK){
						continue;
					}else{
						try {
							if(_client.getNetConnection().isClosed() ){
								_client.logout();
								_client.getNetConnection().close();
							}
						} catch (Exception e) {
							_log.warning("Quit Character failure.");
							_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
							throw e;
						}
					}
				}
				Thread.sleep(10000);
			}
			catch(Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
}
