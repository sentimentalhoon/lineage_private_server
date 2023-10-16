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
				int time2 = Config.FEATHER_TIME / 2;  // ������ ȭ�չ��� �޴� �ð�, ������ ��
				int time3 = Config.FEATHER_TIME; // �ź��� ���� ������ �޴� �ð�, ������ ��
				int time4 = 200; // �Ⱘ�� ��� ��Ż ������ �ð�
				if (nowtime % time4 == 0) {
					LastavardTeleporter(); // ��Ÿ�ٵ� �ڷ����� �Ⱘ�� ����
					allBuff();  // �ù����� ��� ����
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
							int featherCount = pc.getFeatherCount(); // ������ ���� ȭ�չ� Ƚ��
							if(featherCount >= time2) givePotion(pc); // time2 ������ ũ�ٸ� ȭ�չ��� �����Ѵ�.
							else pc.setFeatherCount(featherCount + 1); // ������ feathercount �� +1�� �����ش�.

							int tc = pc.getTimeCount(); // ���� ���� Ƚ��
							if (tc >= time3) giveFeather(pc);// time3 ������ ũ�ٸ� ������ �����Ѵ�.
							else pc.setTimeCount(tc+1); // ������ tc ���� +1�� �����ش�.

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
	 * �������, ���ž����, ��Ÿ�ٵ� ������ �ð��� üũ�Ͽ� �����Ѵ�.
	 * @param pc
	 */
	private void MapTimeCheck(L1PcInstance pc){
		try{
			switch(pc.getMapId()){
			case 53: case 54: case 55: case 56: // ��� ���� �ð� ����
				GungeonTimeCheck(pc);
				break;
			case 78: case 79: case 80: case 81: case 82: case 83: // ���ž �ð� ����
				IvoryTowerTimeCheck(pc);
				break;
			case 451: case 452: case 453: case 454: case 455: case 456: // ��Ÿ�ٵ� �ð� ����
			case 460: case 461: case 462: case 463: case 464: case 465: case 466: // ��Ÿ�ٵ� �ð� ����
			case 470: case 471: case 472: case 473: case 474: case 475: case 476: case 477: case 478: // ��Ÿ�ٵ� �ð� ����
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
	 * �����ϻ���� �ູ�� ���� : safetyzone ������ �����ȴ�.
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
	 * ��� ������ ��Ÿ�ٵ� �̵� NPC�� 1�ð����� ��ȯ �Ѵ�.
	 */
	private void LastavardTeleporter(){
		try {
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("��� ����(1~4��)�� ��Ÿ�ٵ� �ڷ����Ͱ� �����Ǿ����ϴ�. 1�ð� �Ŀ� ������ϴ�."));
			L1SpawnUtil.spawn2(32797, 32728, (short) 53, 450001842, 0, 3600*1000, 0);
			L1SpawnUtil.spawn2(32806, 32777, (short) 54, 450001843, 0, 3600*1000, 0);
			L1SpawnUtil.spawn2(32736, 32869, (short) 55, 450001844, 0, 3600*1000, 0);
			L1SpawnUtil.spawn2(32792, 32730, (short) 56, 450001845, 0, 3600*1000, 0);
		} catch (Exception e) { }
	}
	/**
	 * ��� ������ 1000�� �����縦 10�а� ��ȯ�Ѵ�.
	 */
	private void allBuff(){
		try {
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("��ü �����簡 �����Ǿ����ϴ�. 10�� �Ŀ� ������ϴ�."));
			L1SpawnUtil.spawn2(33436, 32801, (short) 4, 450001876, 0, 600*1000, 0);
		} catch (Exception e) { }
	}
	/**
	 * �ź��� ���� ���� ������ �Ѵ�.
	 * @param pc
	 */
	private void giveFeather(L1PcInstance pc) {
		pc.setTimeCount(0); // �ʱ�ȭ �����ش�.
		if (pc.isDead() || pc.isPrivateShop() || pc.noPlayerCK){
		} else {
			int FN = Config.FEATHER_NUMBER; // �⺻ ���� ����
			int CLN = Config.CLAN_NUMBER; // ���� ���� ����
			int CAN = Config.CASTLE_NUMBER; // ���� ���� ����
			pc.getInventory().storeItem(41159, FN); // �ź��� �������� ���� 
			pc.sendPackets(new S_ServerMessage(403, "$5116"));

			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (clan.getCastleId() == 0 && pc.getClanid() != 0) { // ������ �ƴϰ� ���� ������
					pc.getInventory().storeItem(41159, CLN);
					pc.sendPackets(new S_ServerMessage(403, "$5116"));
				}
				if (clan.getCastleId() != 0) { // �����Ͻ�
					pc.getInventory().storeItem(41159, CAN);
					pc.sendPackets(new S_ServerMessage(403, "$5116"));
				}		  
			}
		}
	}
	/**
	 * ������ ������ ��� ���� ��� ������ ȭ�չ��� �����Ѵ�.
	 * @param pc
	 */
	private void givePotion(L1PcInstance pc) {
		pc.setFeatherCount(0); // �ʱ�ȭ �����ش�.
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
	 * ���� ���� ������ �ð��� üũ�Ͽ� �����ش�.
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
	 * ��Ÿ���� ����, ��Ǫ������ ���� ����
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
	 * ���ž �ð� üũ
	 * @param pc
	 */
	private void IvoryTowerTimeCheck(L1PcInstance pc) { 
		RealTime time = RealTimeClock.getInstance().getRealTime();
		int entertime = pc.getIvoryTowerTime() % 1000;
		int enterday = pc.getIvoryTowerTime() / 1000;
		int dayofyear = time.get(Calendar.DAY_OF_YEAR);

		if (dayofyear == 365)
			dayofyear += 1;

		if(enterday < dayofyear){ // ������ �ڽ�Ű�� �κк��� ��¥�� ����Ǿ������� ���� üũ�ؼ� �ʱ�ȭ�� ����� ��.
			pc.setLdungeonTime(time.get(Calendar.DAY_OF_YEAR) * 1000);
		} else if(entertime > 60){	// �޼����� �ְ�
			L1Teleport.teleport(pc, 33419, 32810, (short) 4, 5, true);
		} else {
			if (entertime > 49) {
				int a = 60 - entertime;
				pc.sendPackets(new S_ServerMessage(1527, "" + a + ""));// ü���ð��� %�г��Ҵ�.
			}
			pc.setIvoryTowerTime(pc.getIvoryTowerTime() + 1);
		}
	}
	/**
	 * ��Ÿ�ٵ� ���� �ð� üũ
	 * @param pc
	 */
	private void LdungeonTimeCheck(L1PcInstance pc) { // ��Ÿ�ٵ� ���� Ÿ�� üũ
		RealTime time = RealTimeClock.getInstance().getRealTime();
		int entertime = pc.getLdungeonTime() % 1000;
		int enterday = pc.getLdungeonTime() / 1000;
		int dayofyear = time.get(Calendar.DAY_OF_YEAR);

		if(dayofyear == 365)
			dayofyear += 1;

		if(enterday < dayofyear){
			pc.setLdungeonTime(time.get(Calendar.DAY_OF_YEAR) * 1000);
		} else if(entertime > 300){
			// �޼����� �ְ�
			L1Teleport.teleport(pc, 33419, 32810, (short) 4, 5, true);
		} else {
			if(entertime > 241){
				int a = 300 - entertime;
				pc.sendPackets(new S_ServerMessage(1527, ""+a+""));// ü���ð���  %�� ���Ҵ�.
			}
			pc.setLdungeonTime(pc.getLdungeonTime() + 1);
		}
	}
	/**
	 * ��� ���� �ð� üũ
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
			// �޼����� �ְ�
			L1Teleport.teleport(pc, 33419, 32810, (short) 4, 5, true);
		} else {
			if(entertime > 169){
				int a = 180 - entertime;
				pc.sendPackets(new S_ServerMessage(1527, ""+a+""));// ü���ð���  %�� ���Ҵ�.
			}
			pc.setGdungeonTime(pc.getGdungeonTime() + 1);
		}
	}
}
