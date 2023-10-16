package l1j.server.GameSystem.Lastabard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.DoorSpawnTable;
import l1j.server.server.model.Instance.L1DoorInstance;
import l1j.server.server.model.gametime.RealTime;

public class LastabardController {
	private static Logger _log = Logger.getLogger(LastabardController.class.getName());
	private ArrayList<LastabardTime> timeList = new ArrayList<LastabardTime>();
	private volatile RealTime currentTime = new RealTime();
	private static LastabardController _instance;
	private RealTime _previousTime = null;
	private int [][] fourthFloor = new int[2][3]; // 라던 4층 두번째 방, 네번째 방 
	
	public static LastabardController getInstance() {
		if (_instance == null) {
			synchronized(LastabardController.class) {
				if (_instance == null) {
					_instance = new LastabardController();
				}
			}
		}
		
		return _instance;
	}
	
	public static void start() {
		getInstance().init();
		LastabardSpawnTable.getInstance().Init();
		//LastabardSpawnTable.getInstance().spawnMobs(531);
		//LastabardSpawnTable.getInstance().spawnMobs(533);
	}

	private void init() {
		GeneralThreadPool.getInstance().execute(new TimeUpdater());
	}

	private class TimeUpdater implements Runnable {
		@Override
		public void run() {
			while (true) {
				_previousTime = null;
				_previousTime = currentTime;
				currentTime = new RealTime();
				
				checkTimes();

				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	private class LastabardTime {
		private int mapId;
		private int relatedDoor;
		private int deadline;
		
		public boolean isTimeOver(int currentTime) {
			if(deadline < currentTime)	return true;
			else						return false;
		}
		
		@SuppressWarnings("unused")
		private LastabardTime() {};
		
		public LastabardTime(int mapId, int startTime, int delayTime, int relatedDoor) {
			this.mapId = mapId;
			this.deadline = startTime + delayTime; 
			this.relatedDoor = relatedDoor;
		}

		public int  getMapId() 		{ return mapId;		  }
		public int  getDeadline() 	{ return deadline;	  }
		public int  getRelatedDoor(){ return relatedDoor; }
	}

	private boolean isFieldChanged(int field) {
		return _previousTime.get(field) != currentTime.get(field);
	}

	private void checkTimes() {
		if (isFieldChanged(Calendar.SECOND)) {
			for(LastabardTime time : timeList) {
				if(time.isTimeOver(getRealTime().getSeconds())) {
					// 해당 맵의 시간이 끝남, 해당 맵의 몹을 스폰하고 해당 맵으로 진입하기 위해 통과하는 문을 닫음
					reset(time.getMapId(), time.getRelatedDoor()); 
					timeList.remove(time);
				}
			}
		}
	}
	
	private void reset(int mapId, int relatedDoor) {
		if(mapId != 0){
			LastabardData.doTeleport(mapId);
			LastabardSpawnTable.getInstance().spawnMobs(mapId);
		}
		
		if(relatedDoor == 0) return;
		L1DoorInstance door = DoorSpawnTable.getInstance().getDoor(relatedDoor);
		if(door != null) {
			door.setDead(false); // 있으나 마나?
			door.close();
		}
	}

	private RealTime getRealTime() {
		return currentTime;
	}
	
	public void addDelayTime(int mapId, int doorId) {
		for(LastabardTime time : timeList) {
			if(time.getMapId() == mapId && mapId != 0) return;
		}
		
		int delayTime = LastabardData.getDelayTime(mapId);
		if(delayTime <= 0) return;
		
		timeList.add(new LastabardTime(mapId, getRealTime().getSeconds(), delayTime, doorId));
		
		additionalDelayTime(mapId);
	}

	private void additionalDelayTime(int mapId) {
		if(mapId == 0) return;
		int mapid = LastabardData.relatedTime(mapId);
		if(mapid != 0) addDelayTime(mapid, 0);
	}

	public int getMobCount(int mobMapId, int room) {
		if(room > 4 || room < 0) return 0;
		int pos = LastabardData.getPosInMapId(mobMapId);
		if(pos < 0 || pos > 1) return -1;
		return fourthFloor[pos][room];
	}

	public synchronized void die(int mobMapId, int room) {
		if(room > 4 || room < 0) return;
		int pos = LastabardData.getPosInMapId(mobMapId);
		if(pos < 0 || pos > 1) return;
		fourthFloor[pos][room]--;
	}

	public synchronized void alive(int mobMapId, int room) {
		if(room > 4 || room < 0) return;
		int pos = LastabardData.getPosInMapId(mobMapId);
		if(pos < 0 || pos > 1) return;
		fourthFloor[pos][room]++;
	}
}