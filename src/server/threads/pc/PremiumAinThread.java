package server.threads.pc;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.RealTime;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.L1SpawnUtil;

public class PremiumAinThread extends Thread{

	private static PremiumAinThread _instance;
	private static Logger _log = Logger.getLogger(PremiumAinThread.class.getName());

	public static PremiumAinThread getInstance(){
		if (_instance == null){
			_instance = new PremiumAinThread();
			_instance.start();
		}
		return _instance;
	}
	public PremiumAinThread(){	}

	public void run(){
		System.out.println(PremiumAinThread.class.getName()  + " Start");
		while(true){
			try {
				int nowtime = Integer.valueOf(CommonUtil.dateFormat("HHmm"));
				int time2 = Config.FEATHER_TIME / 2;  // 응집된 화합물을 받는 시간, 단위는 분
				int time3 = Config.FEATHER_TIME; // 신비한 날개 깃털을 받는 시간, 단위는 분
				int time4 = 200; // 기감에 라던 포탈 열리는 시간
				if (nowtime % time4 == 0) {
					LastavardTeleporter(); // 라스타바드 텔레포터 기감에 출현
					allBuff();  // 올버프사 잠시 등장
				}
				for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
					if(pc == null 
							|| pc.getNetConnection() == null
							|| pc.isDead() 
							|| pc.isPrivateShop() 
							|| pc.noPlayerCK
							){
						continue;
					} else {
						try {				
							int featherCount = pc.getFeatherCount(); // 해츨링 인형 화합물 횟수
							if(featherCount >= time2) givePotion(pc); // time2 값보다 크다면 화합물을 지급한다.
							else pc.setFeatherCount(featherCount + 1); // 기존의 feathercount 에 +1을 시켜준다.

							int tc = pc.getTimeCount(); // 깃털 지급 횟수
							if (tc >= time3) giveFeather(pc);// time3 값보다 크다면 깃털을 지급한다.
							else pc.setTimeCount(tc+1); // 기존의 tc 값에 +1을 시켜준다.

							Ainhasad(pc);

							MapTimeCheck(pc);

							if(pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_A)  
									|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_P)) DragonBlood(pc);
							/*
							int BIRTHDAYcount = pc.getInventory().countItems(L1ItemId.HAPPY_BIRTHDAY_ELF);
							if(BIRTHDAYcount > 0){ HAPPYBIRTHDAYCheck(pc, BIRTHDAYcount); }
							 */
						} catch (Exception e) {
							_log.warning("Primeum give failure.");
							_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
							throw e;
						}
					}
				}
				Thread.sleep(60000);
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				//cancel();
			}
		}
	}
	/**
	 * 기란감옥, 상아탑던젼, 라스타바드 던젼의 시간을 체크하여 적용한다.
	 * @param pc
	 */
	private void MapTimeCheck(L1PcInstance pc){
		try{
			switch(pc.getMapId()){
			case 53: case 54: case 55: case 56: // 기란 감옥 시간 조정
				GungeonTimeCheck(pc);
				break;
			case 78: case 79: case 80: case 81: case 82: case 83: // 상아탑 시간 조정
				IvoryTowerTimeCheck(pc);
				break;
			case 451: case 452: case 453: case 454: case 455: case 456: // 라스타바드 시간 조정
			case 460: case 461: case 462: case 463: case 464: case 465: case 466: // 라스타바드 시간 조정
			case 470: case 471: case 472: case 473: case 474: case 475: case 476: case 477: case 478: // 라스타바드 시간 조정
			case 490: case 491: case 492: case 493: case 494: case 495: case 496:
			case 530: case 531: case 532: case 533: case 534:
				LdungeonTimeCheck(pc);
				break;
			}
		} catch (Exception e) {
			_log.warning("MapTimeCheck Error");
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	/**
	 * 아인하사드의 축복의 적립 : safetyzone 에서만 적립된다.
	 * @param pc
	 */
	private void Ainhasad(L1PcInstance pc){
		try{
			if(pc.getLevel() >= 49){
				int sc = pc.getSafeCount();
				if(CharPosUtil.getZoneType(pc) == 1/* && !pc.isPrivateShop()*/) {
					if(sc >= 14){
						if(pc.getAinHasad() <= 1999999)
							pc.calAinHasad(10000);
						pc.setSafeCount(0);
					} else {
						pc.setSafeCount(sc+1);
					}
				} else {
					if(sc > 0)
						pc.setSafeCount(0);
				}
			}
		} catch (Exception e) {
			_log.warning("PremiumAinThread Ainhasad Error");
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	/**
	 * 기란 감옥에 라스타바드 이동 NPC를 1시간동안 소환 한다.
	 */
	private void LastavardTeleporter(){
		try {
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("기란 감옥(1~4층)에 라스타바드 텔레포터가 생성되었습니다. 1시간 후에 사라집니다."));
			L1SpawnUtil.spawn2(32797, 32728, (short) 53, 450001842, 0, 3600*1000, 0);
			L1SpawnUtil.spawn2(32806, 32777, (short) 54, 450001843, 0, 3600*1000, 0);
			L1SpawnUtil.spawn2(32736, 32869, (short) 55, 450001844, 0, 3600*1000, 0);
			L1SpawnUtil.spawn2(32792, 32730, (short) 56, 450001845, 0, 3600*1000, 0);
		} catch (Exception e) { }
	}
	/**
	 * 기란 마을에 1000만 버프사를 10분감 소환한다.
	 */
	private void allBuff(){
		try {
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("전체 버프사가 생성되었습니다. 10분 후에 사라집니다."));
			L1SpawnUtil.spawn2(33436, 32801, (short) 4, 450001876, 0, 600*1000, 0);
		} catch (Exception e) { }
	}
	/**
	 * 신비한 날개 깃털 지급을 한다.
	 * @param pc
	 */
	private void giveFeather(L1PcInstance pc) {
		pc.setTimeCount(0); // 초기화 시켜준다.
		if (pc.isDead() || pc.isPrivateShop() || pc.noPlayerCK){
		} else {
			int FN = Config.FEATHER_NUMBER; // 기본 지급 갯수
			int CLN = Config.CLAN_NUMBER; // 혈맹 지급 갯수
			int CAN = Config.CASTLE_NUMBER; // 성형 지급 갯수
			pc.getInventory().storeItem(41159, FN); // 신비한 날개깃털 지급 
			pc.sendPackets(new S_ServerMessage(403, "$5116"));

			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (clan.getCastleId() == 0 && pc.getClanid() != 0) { // 성혈이 아니고 혈이 있을시
					pc.getInventory().storeItem(41159, CLN);
					pc.sendPackets(new S_ServerMessage(403, "$5116"));
				}
				if (clan.getCastleId() != 0) { // 성혈일시
					pc.getInventory().storeItem(41159, CAN);
					pc.sendPackets(new S_ServerMessage(403, "$5116"));
				}		  
			}
		}
	}
	/**
	 * 해츨링 인형을 들고 있을 경우 응집된 화합물을 지급한다.
	 * @param pc
	 */
	private void givePotion(L1PcInstance pc) {
		pc.setFeatherCount(0); // 초기화 시켜준다.
		if (pc.isDead() 
				|| pc.isPrivateShop() 
				|| pc.noPlayerCK 
				){
		} else {
			for (L1DollInstance doll : pc.getDollList().values()) {
				int type = doll.getDollType();
				int potion = 3;
				if (type == L1DollInstance.DOLLTYPE_DRAGON_M
						|| type == L1DollInstance.DOLLTYPE_DRAGON_W
						|| type == L1DollInstance.DOLLTYPE_HIGH_DRAGON_M
						|| type == L1DollInstance.DOLLTYPE_HIGH_DRAGON_W) {
					L1ItemInstance item = pc.getInventory().storeItem(555580, potion);
					pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
				}
			}
		}
	}

	/**
	 * 생일 축하 아이템 시간을 체크하여 없애준다.
	 * @param pc
	 * @param count
	 */
	/*
	private void HAPPYBIRTHDAYCheck(L1PcInstance pc, int count) {
		long nowtime = System.currentTimeMillis();
		if(count == 1){
			L1ItemInstance item = pc.getInventory().findItemId(L1ItemId.HAPPY_BIRTHDAY_ELF);
			if(nowtime > item.getEndTime().getTime())
				pc.getInventory().removeItem(item);
		}else{
			L1ItemInstance[] itemList = pc.getInventory().findItemsId(L1ItemId.HAPPY_BIRTHDAY_ELF);
			for (int i = 0; i < itemList.length; i++) {
				if(nowtime > itemList[i].getEndTime().getTime())
					pc.getInventory().removeItem(itemList[i]);		
			}
		}
	}*/
	/**
	 * 안타라스의 혈흔, 파푸리온의 혈흔 관련
	 * @param pc
	 */
	private void DragonBlood(L1PcInstance pc) {
		int Time = 0;
		if(pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_A)){
			Time = pc.getSkillEffectTimerSet().getSkillEffectTimeSec(L1SkillId.DRAGONBLOOD_A)/60;
			pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONBLOOD, 82, Time));
		}
		if(pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_P)){
			Time = pc.getSkillEffectTimerSet().getSkillEffectTimeSec(L1SkillId.DRAGONBLOOD_P)/60;
			pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONBLOOD, 85, Time));
		}
	}
	/**
	 * 상아탑 시간 체크
	 * @param pc
	 */
	private void IvoryTowerTimeCheck(L1PcInstance pc) { 
		RealTime time = RealTimeClock.getInstance().getRealTime();
		int entertime = pc.getIvoryTowerTime() % 1000;
		int enterday = pc.getIvoryTowerTime() / 1000;
		int dayofyear = time.get(Calendar.DAY_OF_YEAR);

		if (dayofyear == 365)
			dayofyear += 1;

		if(enterday < dayofyear){ // 마을로 텔시키는 부분보다 날짜가 변경되었는지를 먼저 체크해서 초기화를 해줘야 함.
			pc.setLdungeonTime(time.get(Calendar.DAY_OF_YEAR) * 1000);
		} else if(entertime > 60){	// 메세지를 주고
			L1Teleport.teleport(pc, 33419, 32810, (short) 4, 5, true);
		} else {
			if (entertime > 49) {
				int a = 60 - entertime;
				pc.sendPackets(new S_ServerMessage(1527, "" + a + ""));// 체류시간이 %분남았다.
			}
			pc.setIvoryTowerTime(pc.getIvoryTowerTime() + 1);
		}
	}
	/**
	 * 라스타바드 던젼 시간 체크
	 * @param pc
	 */
	private void LdungeonTimeCheck(L1PcInstance pc) { // 라스타바드 던전 타임 체크
		RealTime time = RealTimeClock.getInstance().getRealTime();
		int entertime = pc.getLdungeonTime() % 1000;
		int enterday = pc.getLdungeonTime() / 1000;
		int dayofyear = time.get(Calendar.DAY_OF_YEAR);

		if(dayofyear == 365)
			dayofyear += 1;

		if(enterday < dayofyear){
			pc.setLdungeonTime(time.get(Calendar.DAY_OF_YEAR) * 1000);
		} else if(entertime > 300){
			// 메세지를 주고
			L1Teleport.teleport(pc, 33419, 32810, (short) 4, 5, true);
		} else {
			if(entertime > 241){
				int a = 300 - entertime;
				pc.sendPackets(new S_ServerMessage(1527, ""+a+""));// 체류시간이  %분 남았다.
			}
			pc.setLdungeonTime(pc.getLdungeonTime() + 1);
		}
	}
	/**
	 * 기란 감옥 시간 체크
	 * @param pc
	 */
	private void GungeonTimeCheck(L1PcInstance pc) {
		RealTime time = RealTimeClock.getInstance().getRealTime();
		int entertime = pc.getGdungeonTime() % 1000;
		int enterday = pc.getGdungeonTime() / 1000;
		int dayofyear = time.get(Calendar.DAY_OF_YEAR);

		if(dayofyear == 365)
			dayofyear += 1;

		if(enterday < dayofyear){
			pc.setGdungeonTime(time.get(Calendar.DAY_OF_YEAR) * 1000);
		} else if(entertime > 180){
			// 메세지를 주고
			L1Teleport.teleport(pc, 33419, 32810, (short) 4, 5, true);
		} else {
			if(entertime > 169){
				int a = 180 - entertime;
				pc.sendPackets(new S_ServerMessage(1527, ""+a+""));// 체류시간이  %분 남았다.
			}
			pc.setGdungeonTime(pc.getGdungeonTime() + 1);
		}
	}
}
