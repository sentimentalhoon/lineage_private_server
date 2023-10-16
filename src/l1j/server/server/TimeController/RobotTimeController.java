/*
 * 
 * 무인PC 타이머로 각종액션을 취하도록 옵션부여
 * 
 */
package l1j.server.server.TimeController;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.command.executor.L1Robot3;
import l1j.server.server.datatables.RobotTable;
import l1j.server.server.datatables.RobotTable.RobotTeleport;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Astar;
import l1j.server.server.model.L1Node;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1CastleGuardInstance;
import l1j.server.server.model.Instance.L1GuardInstance;
import l1j.server.server.model.Instance.L1MerchantInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1RobotInstance;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.serverpackets.S_ChangeHeading;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_Fishing;
import l1j.server.server.serverpackets.S_MoveCharPacket;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.types.Point;
import l1j.server.server.utils.CommonUtil;

public class RobotTimeController implements Runnable {

	private static RobotTimeController _instance;

	public static RobotTimeController getInstance() {
		if (_instance == null) {
			_instance = new RobotTimeController();
		}
		return _instance;
	}
	@Override
	public void run() {
		try {
			while (true) {
				int nowtime = Integer.valueOf(CommonUtil.dateFormat("mm"));
				if(L1Robot3.RobotUserList1.size() != 0){
					switch(nowtime){
					case 2: case 20: case 38: case 56:		robottime(1);	break;
					case 4: case 22: case 40: case 58:		robottime(2);	break;
					case 6: case 24: case 42:				robottime(3);	break;
					case 8: case 26: case 44:				robottime(4);	break;
					case 1: case 10: case 28: case 46:				robottime(5);	break;
					case 3: case 12: case 30: case 48:				robottime(6);	break;
					case 5: case 14: case 32: case 50:				robottime(7);	break;
					case 7 :case 16: case 34: case 52:				robottime(8);	break;
					case 9: case 18: case 36: case 54:				robottime(9);	break;
					}
				}
				Thread.sleep(60000);
			}
		} catch (Exception e1) {
			System.out.println(e1 + "RobotTimeController");
		}
	}

	private void robottime(int type) {
		try {
			switch(type){
			case 1:	for (L1RobotInstance pc : L1Robot3.RobotUserList1) { robotAction(pc); } break;
			case 2:	for (L1RobotInstance pc : L1Robot3.RobotUserList2) { robotAction(pc); } break;
			case 3:	for (L1RobotInstance pc : L1Robot3.RobotUserList3) { robotAction(pc); } break;
			case 4:	for (L1RobotInstance pc : L1Robot3.RobotUserList4) { robotAction(pc); } break;
			case 5:	for (L1RobotInstance pc : L1Robot3.RobotUserList5) { robotAction(pc); } break;
			case 6:	for (L1RobotInstance pc : L1Robot3.RobotUserList6) { robotActionFish(pc); } break;
			case 7:	for (L1RobotInstance pc : L1Robot3.RobotUserList7) { robotActionFish(pc); } break;
			case 8:	for (L1RobotInstance pc : L1Robot3.RobotUserList8) { robotActionFish(pc); } break;
			case 9:	for (L1RobotInstance pc : L1Robot3.RobotUserList9) { robotActionFish(pc); } break;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void robotAction(L1RobotInstance _client){
		try {
			if(_client != null){
				// 텔레포트
				Teleport(_client);
				Thread.sleep(200L);
				if (!Config.STANDBY_SERVER){
					// 스킬사용
					Buff(_client);
					Thread.sleep(900L);
				}
				// 포션 먹자 먹자
				toSpeedPotion(_client);
				Thread.sleep(900L);
				if (!Config.STANDBY_SERVER){
				// 변신 우걱 우걱
				Poly(_client);
				Thread.sleep(500L);
				}
			}
		} catch (InterruptedException e) { e.printStackTrace(); }
	}

	private void robotActionFish(L1RobotInstance _client){
		try {
			if(_client != null){
				if (_client.getMapId() == 5302){ 
					startFishing(_client);
				} else {
					// 텔레포트
					fishTeleport(_client);
					Thread.sleep(500L);
					L1PolyMorph.undoPoly(_client);
					Thread.sleep(1000L);
					startFishing(_client);
				}
			}
		} catch (InterruptedException e) { e.printStackTrace(); }
	}
	// 변신 목록
	private static final int[] POLY_IDS = {	
		6157, 3881, 7332, 6698, 4003, 7129, 6267, 6268, 6269, 6279,	6270, 6271,	
		6272, 6280,	9206, 9205,	9225, 9226, 6137, 6138, 6139, 6140, 6143, 6144
	};

	private void Poly(L1RobotInstance pc) {
		try {
			int chance = CommonUtil.random(POLY_IDS.length);
			L1PolyMorph.doPoly(pc, POLY_IDS[chance], -1, L1PolyMorph.MORPH_BY_ITEMMAGIC);
		} catch (Exception e){
			System.out.println(e + "(poly)");
		}
	}

	private void Teleport(L1RobotInstance pc) {
		try{
			RobotTeleport robotTeleport = RobotTable.getRobotTeleportList().get(CommonUtil.random(RobotTable.getRobotTeleportList().size()));
			CommonUtil.tryCount(pc, robotTeleport.x, robotTeleport.y, (short)robotTeleport.mapid, robotTeleport.heading, 2, 1);
		} catch (Exception e){
			System.out.println(e + "(Teleport)");
		}
	}

	private void fishTeleport(L1RobotInstance pc) {
		try{
			RobotTeleport robotFishTeleport = RobotTable.getRobotFishTeleportList().get(CommonUtil.random(RobotTable.getRobotFishTeleportList().size()));
			CommonUtil.tryCount(pc, robotFishTeleport.x, robotFishTeleport.y, (short)robotFishTeleport.mapid, robotFishTeleport.heading, 2, 2);
		} catch (Exception e){
			System.out.println(e + "(fishTeleport)");
		} finally {
			RobotTeleport robotFishTeleport = RobotTable.getRobotFishTeleportList().get(CommonUtil.random(RobotTable.getRobotFishTeleportList().size()));
			CommonUtil.tryCount(pc, robotFishTeleport.x, robotFishTeleport.y, (short)robotFishTeleport.mapid, robotFishTeleport.heading, 2, 2);
		}
	}

	private void Buff(L1RobotInstance player) {
		L1SkillUse skilluse = new L1SkillUse();
		try {
			if (player != null) {
				if (player.isCrown()) {
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.GLOWING_AURA)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.GLOWING_AURA, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHINING_AURA) && player.getLevel() >= 55) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.SHINING_AURA, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
				} else if (player.isKnight()) {
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.REDUCTION_ARMOR)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.REDUCTION_ARMOR, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						player.getSkillEffectTimerSet().setSkillEffect(L1SkillId.REDUCTION_ARMOR, 100000);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BOUNCE_ATTACK)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.BOUNCE_ATTACK, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						player.getSkillEffectTimerSet().setSkillEffect(L1SkillId.BOUNCE_ATTACK, 60000);
						Thread.sleep(500);
					}
				} else if (player.isDarkelf()) {
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MOVING_ACCELERATION)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.MOVING_ACCELERATION, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.UNCANNY_DODGE)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.UNCANNY_DODGE, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BURNING_SPIRIT)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.BURNING_SPIRIT, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DOUBLE_BRAKE)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.DOUBLE_BRAKE, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHADOW_FANG)) {
						player.getSkillEffectTimerSet().setSkillEffect(L1SkillId.SHADOW_FANG, 300000);
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.SHADOW_FANG, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
				} else if (player.isElf()) {
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLOODY_SOUL)) {
						player.getSkillEffectTimerSet().setSkillEffect(L1SkillId.BLOODY_SOUL, 15000);
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.BLOODY_SOUL, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
						player.getSkillEffectTimerSet().setSkillEffect(L1SkillId.BLOODY_SOUL, 15000);
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.BLOODY_SOUL, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
						player.getSkillEffectTimerSet().setSkillEffect(L1SkillId.BLOODY_SOUL, 15000);
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.BLOODY_SOUL, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
				} else if (player.isDragonknight()) {
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLOOD_LUST)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.BLOOD_LUST, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGON_SKIN)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.DRAGON_SKIN, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MORTAL_BODY)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.MORTAL_BODY, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
				} else if (player.isIllusionist()) {
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.CONCENTRATION)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.CONCENTRATION, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PATIENCE)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.PATIENCE, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
					if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INSIGHT)) {
						Broadcaster.broadcastPacket(player, new S_DoActionGFX(player.getId(), ActionCodes.ACTION_SkillBuff));
						skilluse.handleCommands(player, L1SkillId.INSIGHT, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
						Thread.sleep(500);
					}
				}
			}
		} catch (Exception e) { }
	}
	/*
	private void Power() {
		try {
			for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
				if (pc != null) {
					if (pc.isDead()) {
						L1PcInstance resusepc1 = (L1PcInstance) L1World
								.getInstance().findObject(pc.getTempID());
						pc.setTempID(1);
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc
								.getId(), '\346'));
						pc.resurrect(pc.getMaxHp());
						pc.setCurrentHp(pc.getMaxHp());
						Broadcaster.broadcastPacket(pc, new S_Resurrection(pc,
								resusepc1, 1));
						Broadcaster.broadcastPacket(pc, new S_CharVisualUpdate(
								pc));
					}
				}
			}
		} catch (Exception c) {
			System.out.println(c + "(power)");
		}
	}
	 */

	/**
	 * 가속도 물약복용 처리 함수.
	 */
	private void toSpeedPotion(L1RobotInstance _client){
		// 용기 떠러졌을경우 복용하기.
		Broadcaster.broadcastPacket(_client, new S_SkillBrave(_client.getId(), 1, 0));
		_client.getMoveState().setBraveSpeed(1);
		_client.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_BRAVE, 300*1000);
		Broadcaster.broadcastPacket(_client, new S_SkillSound(_client.getId(), 751));
		// 촐기떠러졌을경우 복용하기.
		Broadcaster.broadcastPacket(_client, new S_SkillHaste(_client.getId(), 1, 0));
		_client.getMoveState().setMoveSpeed(1);
		_client.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_HASTE, 300*1000);
	}

	/**
	 * 로봇 낚시 제대로 될런지 모르겠다.. 2012. 04. 28.
	 * @param pc
	 */
	private void startFishing(L1RobotInstance pc) {
		int gab = 0;
		int fishX = 0;
		int fishY = 0;
		int heading = pc.getMoveState().getHeading(); //● 방향: (0.좌상)(1.상)( 2.우상)(3.오른쪽)(4.우하)(5.하)(6.좌하)(7.좌)
		switch(heading){
		case 0: //상좌
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX(), pc.getY()-5);
			fishX = pc.getX();
			fishY = pc.getY() -5;
			break;
		}
		case 1: //상
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()+5, pc.getY()-5);
			fishX = pc.getX() + 5;
			fishY = pc.getY() - 5;
			break;
		}
		case 2: //우상
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()+5, pc.getY()-5);
			fishX = pc.getX() + 5;
			fishY = pc.getY() -5;
			break;
		}
		case 3: //오른쪽
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()+5, pc.getY()+5);
			fishX = pc.getX() + 5;
			fishY = pc.getY() + 5;
			break;
		}
		case 4: //우하
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX(), pc.getY()+5);
			fishX = pc.getX();
			fishY = pc.getY() + 5;
			break;
		}
		case 5: //하
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()-5, pc.getY()+5);
			fishX = pc.getX() - 5;
			fishY = pc.getY() + 5;
			break;
		}
		case 6: //좌하
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()-5, pc.getY());
			fishX = pc.getX() - 5;
			fishY = pc.getY();
			break;
		}
		case 7: //좌
		{
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()-5, pc.getY()-5);
			fishX = pc.getX() - 5;
			fishY = pc.getY() - 5;
			break;
		}
		}
		int fishGab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(fishX, fishY);
		if(gab == 28 && fishGab == 28){
			pc.sendPackets(new S_Fishing(pc.getId(), ActionCodes.ACTION_Fishing, fishX, fishY));
			Broadcaster.broadcastPacket(pc, new S_Fishing(pc.getId(), ActionCodes.ACTION_Fishing, fishX, fishY));
		}
	}
}