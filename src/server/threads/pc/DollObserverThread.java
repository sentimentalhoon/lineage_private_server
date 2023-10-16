package server.threads.pc;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_DoActionGFX;

public class DollObserverThread extends Thread{
	
	private static DollObserverThread _instance;
	private static Logger _log = Logger.getLogger(DollObserverThread.class.getName());
	Random rnd = new Random();
	
	public static DollObserverThread getInstance(){
		if (_instance == null){
			_instance = new DollObserverThread();
			_instance.start();
		}
		return _instance;
	}	
	
	
	public DollObserverThread(){
	}

	public void run(){
		System.out.println(DollObserverThread.class.getName()  + " Start");
		while(true){
			try {
				for(L1PcInstance _client : L1World.getInstance().getAllPlayers()){
					if(_client == null || _client.getNetConnection() == null){
						continue;
					}
					else{
						for (L1DollInstance doll : _client.getDollList().values()) {
							_client.sendPackets(new S_DoActionGFX(doll.getId(), 66 + rnd.nextInt(2)));
							Broadcaster.broadcastPacket(_client, new S_DoActionGFX(doll.getId(), 66 + rnd.nextInt(2)));
						}
					}
				}
				Thread.sleep(15000);
			}
			catch(Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}
}
