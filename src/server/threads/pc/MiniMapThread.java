package server.threads.pc;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Party;

public class MiniMapThread extends Thread{

	private static MiniMapThread _instance;
	public static MiniMapThread getInstance(){
		if (_instance == null){
			_instance = new MiniMapThread();
			_instance.start();
		}
		return _instance;
	} 

	public void run(){
		System.out.println("MiniMapThread... Strat");
		while(true){
			try {
				for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
					if (pc.getParty() == null 
							|| pc.isDead() 
							|| pc == null 
							|| pc.getNetConnection() == null 
							|| pc.isPrivateShop() 
							|| pc.noPlayerCK
							) {
						continue;
					}
					pc.sendPackets(new S_Party(0x6e, pc));
				}
				Thread.sleep(1000);   //  1초간격으로 미니맵을 갱신한다
			} catch(Exception e) { e.printStackTrace(); }
		}
	}
}

