
package l1j.server.server;

import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.*;
import static l1j.server.server.model.skill.L1SkillId.*;
import java.util.ArrayList;
import l1j.server.server.model.Instance.*;
import server.GameServer;
import server.LineageClient;
import server.manager.eva;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.server.datatables.*;
import l1j.server.server.model.*;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.templates.L1BookMark;
import l1j.server.L1DatabaseFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class AutoTraderThread extends Thread{

	private static Logger _log = Logger.getLogger(AutoTraderThread.class.getName());

	private ArrayList<String> traders;
	private static AutoTraderThread instance;

	public static AutoTraderThread getInstance(){
		if(instance == null){
			instance = new AutoTraderThread();
			instance.start();
		}
		return instance;
	}

	private AutoTraderThread(){
		traders = new ArrayList<String>();
	}

	public void add(String name){
		synchronized(traders){
			if(!traders.contains(name)){
				traders.add(name);
			}
		}
	}
	public void remove(String name){
		synchronized(traders){
			if(traders.contains(name)){
				traders.remove(name);
			}
		}
	}
	private String[] getTraders(){
		return traders.toArray(new String[traders.size()]);
	}
	private void traders(){
		String[] tarder = getTraders();
		for(int i = 0; i < tarder.length; i++){

			//LineageClient client = new LineageClient(null, 0);
			L1PcInstance pc = L1PcInstance.load(tarder[i]);

			eva.writeMessage(0, "접속 [" + pc.getName() + "] 자동저장 시장");

			int currentHpAtLoad = pc.getCurrentHp();
			int currentMpAtLoad = pc.getCurrentMp();

			pc.clearSkillMastery();
			pc.setOnlineStatus(1);
			CharacterTable.updateOnlineStatus(pc);
			L1World.getInstance().storeObject(pc);

			pc.noPlayerCK = true;

			//pc.setNetConnection(client);
			//client.setActiveChar(pc);

			CharacterTable.getInstance().restoreInventory(pc);
			bookmarks(pc);
			skills(pc);
			//buff(pc);
			
			L1World.getInstance().addVisibleObject(pc);
			pc.beginGameTimeCarrier();
			pc.sendVisualEffectAtLogin(); // 독, 수중 등의 시각 효과를 표시

			pc.getLight().turnOnOffLight();

			if (pc.getCurrentHp() > 0) {
				pc.setDead(false);
				pc.setActionStatus(0);
			} else {
				pc.setDead(true);
				pc.setActionStatus(ActionCodes.ACTION_Die);
			}
			if (currentHpAtLoad > pc.getCurrentHp()) {
				pc.setCurrentHp(currentHpAtLoad);
			}
			if (currentMpAtLoad > pc.getCurrentMp()) {
				pc.setCurrentMp(currentMpAtLoad);
			}
			pc.startObjectAutoUpdate();
			//client.CharReStart(false);
			pc.beginExpMonitor();

			if(pc.getMoveState().getHeading() < 0 || pc.getMoveState().getHeading() > 7) pc.getMoveState().setHeading(0);

			if (pc.getHellTime() > 0) pc.beginHell(false);

			// send shop items;
			AutoTraderTable.getInstance().startShop(pc);
		}
	}

	private void bookmarks(L1PcInstance pc) {

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_teleport WHERE char_id=? ORDER BY name ASC");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();

			L1BookMark bookmark = null;
			while (rs.next()) {
				bookmark = new L1BookMark();
				bookmark.setId(rs.getInt("id"));
				bookmark.setCharId(rs.getInt("char_id"));
				bookmark.setName(rs.getString("name"));
				bookmark.setLocX(rs.getInt("locx"));
				bookmark.setLocY(rs.getInt("locy"));
				bookmark.setMapId(rs.getShort("mapid"));
				bookmark.setRandomX(rs.getShort("randomX"));
				bookmark.setRandomY(rs.getShort("randomY"));
				pc.addBookMark(bookmark);

			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void skills(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_skills WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();

			int i = 0;
			int lv1 = 0;
			int lv2 = 0;
			int lv3 = 0;
			int lv4 = 0;
			int lv5 = 0;
			int lv6 = 0;
			int lv7 = 0;
			int lv8 = 0;
			int lv9 = 0;
			int lv10 = 0;
			int lv11 = 0;
			int lv12 = 0;
			int lv13 = 0;
			int lv14 = 0;
			int lv15 = 0;
			int lv16 = 0;
			int lv17 = 0;
			int lv18 = 0;
			int lv19 = 0;
			int lv20 = 0;
			int lv21 = 0;
			int lv22 = 0;
			int lv23 = 0;
			int lv24 = 0;
			int lv25 = 0;
			int lv26= 0;
			int lv27 = 0;
			int lv28 = 0;
			L1Skills l1skills = null;
			while (rs.next()) {
				int skillId = rs.getInt("skill_id");
				l1skills = SkillsTable.getInstance().getTemplate(skillId);
				if (l1skills.getSkillLevel() == 1) 	{lv1 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 2) 	{lv2 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 3) 	{lv3 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 4) 	{lv4 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 5) 	{lv5 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 6) 	{lv6 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 7) 	{lv7 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 8) 	{lv8 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 9) 	{lv9 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 10) {lv10 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 11) {lv11 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 12) {lv12 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 13) {lv13 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 14) {lv14 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 15) {lv15 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 16) {lv16 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 17) {lv17 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 18) {lv18 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 19) {lv19 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 20) {lv20 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 21) {lv21 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 22) {lv22 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 23) {lv23 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 24) {lv24 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 25) {lv25 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 26) {lv26 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 27) {lv27 |= l1skills.getId();}
				if (l1skills.getSkillLevel() == 28) {lv28 |= l1skills.getId();}

				i = lv1 + lv2 + lv3 + lv4 + lv5 + lv6 + lv7 + lv8 + lv9 + lv10
				+ lv11 + lv12 + lv13 + lv14 + lv15 + lv16 + lv17 + lv18
				+ lv19 + lv20 + lv21 + lv22 + lv23 + lv24 + lv25 + lv26 + lv27 + lv28;

				pc.setSkillMastery(skillId);
			}			
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void buff(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_buff WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			int icon[] = {	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0};

			while (rs.next()) {
				int skillid = rs.getInt("skill_id");
				int remaining_time = rs.getInt("remaining_time");

				if (skillid >= COOKING_1_0_N && skillid <= COOKING_1_6_N
						|| skillid >= COOKING_1_8_N && skillid <= COOKING_1_14_N
						|| skillid >= COOKING_1_16_N && skillid <= COOKING_1_22_N
						|| skillid >= COOKING_1_0_S && skillid <= COOKING_1_6_S
						|| skillid >= COOKING_1_8_S && skillid <= COOKING_1_14_S
						|| skillid >= COOKING_1_16_S && skillid <= COOKING_1_22_S) { // 요리(디저트는 제외하다)
					L1Cooking.eatCooking(pc, skillid, remaining_time);
					continue;
				}

				switch(skillid) {
				case DECREASE_WEIGHT:
					icon[0] = remaining_time/16;
					break;
				case WEAKNESS:// 위크니스 //
					icon[4] = remaining_time/4;
					pc.addDmgup(-5);
					pc.addHitup(-1);
					break;
				case BERSERKERS:// 버서커스 //
					icon[7] = remaining_time/4;
					pc.getAC().addAc(10);
					pc.addDmgup(5);
					pc.addHitup(2);
					break;
				case DISEASE:// 디지즈 //
					icon[5] = remaining_time/4;
					pc.addDmgup(-6);
					pc.getAC().addAc(12);
					break;
				case SILENCE:
					icon[2] = remaining_time/4;
					break;
				case SHAPE_CHANGE:
					int poly_id = rs.getInt("poly_id");
					L1PolyMorph.doPoly(pc, poly_id, remaining_time, L1PolyMorph.MORPH_BY_LOGIN);
					continue;
				case DECAY_POTION:
					icon[1] = remaining_time/4;
					break;
				case VENOM_RESIST:// 베놈 레지스트 //
					icon[3] = remaining_time/4;	
					break;
				case DRESS_EVASION:// 드레스 이베이젼 //
					icon[6] = remaining_time/4;
					break;
				case RESIST_MAGIC:// 레지스트 매직
					pc.getResistance().addMr(10);
					pc.sendPackets(new S_ElfIcon(remaining_time/16, 0, 0, 0));
					break;
				case ELEMENTAL_FALL_DOWN:
					icon[12] = remaining_time/4;
					int playerAttr = pc.getElfAttr();
					int i = -50;
					switch (playerAttr) {
					case 0: pc.sendPackets(new S_ServerMessage(79)); break;
					case 1: pc.getResistance().addEarth(i); pc.setAddAttrKind(1); break;
					case 2: pc.getResistance().addFire(i); pc.setAddAttrKind(2); break;
					case 4: pc.getResistance().addWater(i); pc.setAddAttrKind(4); break;
					case 8: pc.getResistance().addWind(i); pc.setAddAttrKind(8); break;
					default: break;
					}
					break;
				case CLEAR_MIND:// 클리어 마인드
					pc.getAbility().addAddedWis((byte) 3);
					pc.resetBaseMr();
					pc.sendPackets(new S_ElfIcon(0, remaining_time/16, 0, 0));
					break;
				case RESIST_ELEMENTAL:// 레지스트 엘리멘탈
					pc.getResistance().addAllNaturalResistance(10);
					pc.sendPackets(new S_ElfIcon(0, 0, remaining_time/16, 0));
					break;
				case ELEMENTAL_PROTECTION:// 프로텍션 프롬 엘리멘탈
					int attr = pc.getElfAttr();
					if (attr == 1) {
						pc.getResistance().addEarth(50);
					} else if (attr == 2) {
						pc.getResistance().addFire(50);
					} else if (attr == 4) {
						pc.getResistance().addWater(50);
					} else if (attr == 8) {
						pc.getResistance().addWind(50);
					}
					pc.sendPackets(new S_ElfIcon(0, 0, 0, remaining_time/16));
					break;
				case ERASE_MAGIC:
					icon[10] = remaining_time/4;
					break;
				case NATURES_TOUCH:// 네이쳐스 터치 //
					icon[8] = remaining_time/4;
					break;
				case WIND_SHACKLE:
					icon[9] = remaining_time/4;
					break;
				case ELEMENTAL_FIRE:
					icon[13] = remaining_time/4;
					break;
				case POLLUTE_WATER:// 폴루트 워터 //
					icon[16] = remaining_time/4;
					break;
				case STRIKER_GALE:// 스트라이커 게일 //
					icon[14] = remaining_time/4;
					break;
				case SOUL_OF_FLAME:// 소울 오브 프레임 //
					icon[15] = remaining_time/4;
					break;
				case ADDITIONAL_FIRE:
					icon[11] = remaining_time/16;
					break;
				case DRAGON_SKIN:// 드래곤 스킨 //
					icon[29] = remaining_time/16;
					break;
				case GUARD_BREAK:// 가드 브레이크 //
					icon[28] = remaining_time/4;
					pc.getAC().addAc(15);
					break;
				case FEAR:// 피어 //
					icon[26] = remaining_time/4;
					break;
				case MORTAL_BODY:// 모탈바디 //
					icon[24] = remaining_time/4;
					break;
				case HORROR_OF_DEATH:// 호러 오브 데스 //
					icon[25] = remaining_time/4;
					pc.getAbility().addAddedStr((byte) -10);
					pc.getAbility().addAddedInt((byte) -10);
					break;
				case CONCENTRATION:
					icon[21] = remaining_time/16;
					break;
				case PATIENCE:// 페이션스 //
					icon[27] = remaining_time/4;
					break;
				case INSIGHT:
					icon[22] = remaining_time/16;
					pc.getAbility().addAddedStr((byte) 1);
					pc.getAbility().addAddedDex((byte) 1);
					pc.getAbility().addAddedCon((byte) 1);
					pc.getAbility().addAddedInt((byte) 1);
					pc.getAbility().addAddedWis((byte) 1);
					pc.getAbility().addAddedCha((byte) 1);
					pc.resetBaseMr();
					break;
				case PANIC:
					icon[23] = remaining_time/16;
					pc.getAbility().addAddedStr((byte) -1);
					pc.getAbility().addAddedDex((byte) -1);
					pc.getAbility().addAddedCon((byte) -1);
					pc.getAbility().addAddedInt((byte) -1);
					pc.getAbility().addAddedWis((byte) -1);
					pc.getAbility().addAddedCha((byte) -1);
					pc.resetBaseMr();
					break;
				case STATUS_BRAVE:
					pc.getMoveState().setBraveSpeed(1);
					break;
				case STATUS_HASTE:
					pc.getMoveState().setMoveSpeed(1);
					break;
				case STATUS_ELFBRAVE:
					pc.getMoveState().setBraveSpeed(1);
					break;
				case STATUS_TIKAL_BOSSDIE:
					icon[20] = (remaining_time+8)/16;
					new L1SkillUse().handleCommands(pc,
							skillid, pc.getId(), pc.getX(), pc.getY(), null, remaining_time, L1SkillUse.TYPE_LOGIN);
					break;
				case STATUS_COMA_3:// 코마 3
					icon[31] = (remaining_time + 16) / 32;
					icon[32] = 40;
					new L1SkillUse().handleCommands(pc,
							skillid, pc.getId(), pc.getX(), pc.getY(), null, remaining_time, L1SkillUse.TYPE_LOGIN);
					break;
				case STATUS_COMA_5:// 코마 5
					icon[31] = (remaining_time + 16) / 32;
					icon[32] = 41;
					new L1SkillUse().handleCommands(pc,
							skillid, pc.getId(), pc.getX(), pc.getY(), null, remaining_time, L1SkillUse.TYPE_LOGIN);
					break;
				case SPECIAL_COOKING:
					if(pc.getSkillEffectTimerSet().hasSkillEffect(SPECIAL_COOKING)){
						if(pc.getSkillEffectTimerSet().getSkillEffectTimeSec(SPECIAL_COOKING) < remaining_time){
							pc.getSkillEffectTimerSet().setSkillEffect(SPECIAL_COOKING, remaining_time * 1000);
						}
					}
					continue;
				case STATUS_CASHSCROLL:// 체력증강주문서 //
					icon[18] = remaining_time/16;
					pc.addHpr(4);
					pc.addMaxHp(50);
					if (pc.isInParty()) { 
						pc.getParty().updateMiniHP(pc);
					}
					break;
				case STATUS_CASHSCROLL2:// 마력증강주문서 //
					icon[18] = remaining_time/16;
					icon[19] = 1;
					pc.addMpr(4);
					pc.addMaxMp(40);
					break;
				case STATUS_CASHSCROLL3:
					icon[18] = remaining_time/16;
					icon[19] = 2;
					pc.addDmgup(3);
					pc.addHitup(3);
					pc.getAbility().addSp(3);
					break;
				case STATUS_FRUIT:// 유그드라 //
					icon[30] = remaining_time/4;
					break;
				case EXP_POTION:
					icon[17] = remaining_time/16;
					break;
				case FEATHER_BUFF_A:// 운세에 따른 깃털 버프 // 매우좋은
					icon[33] = remaining_time/16;
					icon[34] = 70;
					pc.addDmgup(2);
					pc.addHitup(2);
					pc.getAbility().addSp(2);
					pc.addHpr(3);
					pc.addMaxHp(50);
					pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
					if (pc.isInParty()) { 
						pc.getParty().updateMiniHP(pc);
					}
					pc.addMpr(3);
					pc.addMaxMp(30);
					pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					break;
				case FEATHER_BUFF_B:// 운세에 따른 깃털 버프 // 좋은
					icon[33] = remaining_time/16;
					icon[34] = 71;
					pc.addHitup(2);
					pc.getAbility().addSp(1);
					pc.sendPackets(new S_SPMR(pc));
					pc.addMaxHp(50);
					pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
					if (pc.isInParty()) { 
						pc.getParty().updateMiniHP(pc);
					}
					pc.addMaxMp(30);
					pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					break;
				case FEATHER_BUFF_C:// 운세에 따른 깃털 버프 // 보통
					icon[33] = remaining_time/16;
					icon[34] = 72;
					pc.addMaxHp(50);
					pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
					if (pc.isInParty()) { 
						pc.getParty().updateMiniHP(pc);
					}
					pc.addMaxMp(30);
					pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					pc.getAC().addAc(-2);
					break;
				case FEATHER_BUFF_D:// 운세에 따른 깃털 버프 // 나쁜
					icon[33] = remaining_time/16;
					icon[34] = 73;
					pc.getAC().addAc(-1);
					break;
				default:
					new L1SkillUse().handleCommands(pc,
							skillid, pc.getId(), pc.getX(), pc.getY(), null, remaining_time, L1SkillUse.TYPE_LOGIN);
				continue;
				}
				pc.getSkillEffectTimerSet().setSkillEffect(skillid, remaining_time * 1000);
			}

			pc.sendPackets(new S_UnityIcon(
					icon[0], icon[1], icon[2], icon[3], icon[4], icon[5], icon[6], icon[7], icon[8], icon[9], icon[10],
					icon[11], icon[12], icon[13], icon[14], icon[15], icon[16], icon[17], icon[18], icon[19], icon[20],
					icon[21], icon[22], icon[23], icon[24], icon[25], icon[26], icon[27], icon[28], icon[29], icon[30],
					icon[31], icon[32], icon[33], icon[34]));
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

	}

	@Override public void run(){
		while(!GameServer._complated){
			try{ sleep(1000L); }catch(Exception e){}
		}
		traders();
	}
}
