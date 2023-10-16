package server.threads.pc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;

public class SpeedHackThread extends Thread{

	private static SpeedHackThread _instance;

	public static SpeedHackThread getInstance(){
		if (_instance == null){
			_instance = new SpeedHackThread();
			_instance.start();
		}
		return _instance;
	}	
	
	
	public SpeedHackThread(){

	}
	
	public void run(){
		System.out.println(SpeedHackThread.class.getName() + " Start");
		while(true){
			try {
				check_Hacktimer();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void check_Hacktimer(){
		try{
			for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
				if(pc == null || pc.getNetConnection() == null){
					continue;
				}
				else {
					//pc.increase_hackTimer();
				}
			}
		}catch (Exception e){}
	}
}
