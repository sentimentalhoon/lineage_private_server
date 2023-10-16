/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l1j.server.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.StringTokenizer;

import l1j.server.server.datatables.ExpTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1BuffUtil;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_HPMeter;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.IntRange;
import l1j.server.server.utils.SQLUtil;


import l1j.server.Config;
import l1j.server.L1DatabaseFactory;


// Referenced classes of package l1j.server.server:
// ClientThread, Shutdown, IpTable, MobTable,
// PolyTable, IdFactory
//

public class GM2Commands {
	private static Logger _log = Logger.getLogger(GM2Commands.class.getName());

	boolean spawnTF = false;

	private static GM2Commands _instance;

	private GM2Commands() {
	}

	public static GM2Commands getInstance() {
		if (_instance == null) {
			_instance = new GM2Commands();
		}
		return _instance;
	}

	public void handleCommands(L1PcInstance pc, String cmdLine) {
		StringTokenizer token = new StringTokenizer(cmdLine);
		// 최초의 공백까지가 커멘드, 그 이후는 공백을 단락으로 한 파라미터로서 취급한다
		String cmd = token.nextToken();
		String param = "";
		while (token.hasMoreTokens()) {
			param = new StringBuilder(param).append(token.nextToken()).append(
					' ').toString();
		}
		param = param.trim();
		try {
			if(pc.getAccessLevel() == 300){ 
				if (cmd.equalsIgnoreCase("도움말")) {
					showHelp(pc);   
				} else if (cmd.equalsIgnoreCase("레벨")) {
					level(pc, param);
				} else if (cmd.equalsIgnoreCase("소환")) {
					recall(pc, param);
				} else if (cmd.equalsIgnoreCase("투명")) {
					invisible(pc);
				} else if (cmd.equalsIgnoreCase("불투명")) {
					visible(pc);
				} else if (cmd.equalsIgnoreCase("죽어라")) {
					kill(pc, param);
				} else if (cmd.equalsIgnoreCase("귀환")) {
					gmRoom(pc, param);
				} else if (cmd.equalsIgnoreCase("감옥")) {
					prison(pc, param);
				} else if (cmd.equalsIgnoreCase("좋은버프")){				
					GMCommands.getInstance().allGoodBuff();
				} else if (cmd.equalsIgnoreCase("놀자")) {
					GMCommands.getInstance().nolza(pc);
				} else if (cmd.equalsIgnoreCase("출소")) {
					unprison(pc, param);
				} else if (cmd.equalsIgnoreCase("변신")) {
					polymorph(pc, param);
				} else if (cmd.equalsIgnoreCase("출두")) {
					moveToChar(pc, param);
				} else if (cmd.equalsIgnoreCase("계정압류")) {
					accbankick(pc, param);
				} else if (cmd.equalsIgnoreCase("속도")) {
					speed(pc);
					///////////////////////////////////////////////////////////////////////////////////
				} else if (cmd.equalsIgnoreCase("칼")) {
					pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.USERSTATUS_ATTACK, 0);
					UserCommands.tell(pc);//텔렉풀기도 한번 해주면 주변유저가 바로 보라돌이로 보이게됨
				} else if (cmd.equalsIgnoreCase("칼끔")) {
					pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.USERSTATUS_ATTACK);
					UserCommands.tell(pc);
					////////////////////////////////////////////////////////////////////////////////
				} else if (cmd.equalsIgnoreCase(".") || cmd.equalsIgnoreCase("텔렉풀기")) {
					UserCommands.tell(pc);		
				} else if (cmd.startsWith("압류해제")) {
					accountdel(pc, param);
				} else if (cmd.equalsIgnoreCase("청소")) {
					deleteItem();
				} else if (cmd.equalsIgnoreCase("피바")) {
					HpBar(pc, param, param);
				} else if (cmd.equalsIgnoreCase("검사")) {
					CheckCha(pc, param, param);
				} else if (cmd.equalsIgnoreCase("채금")) {
					chatng(pc, param);
				} 
			}
			else {
				String msg = new StringBuilder().append("커멘드：").append(cmd)
						.append("가 존재하지 않는, 또는 실행권한이 없습니다.").toString();
				pc.sendPackets(new S_SystemMessage(msg));
			}
			_log.info("부운영자가." + cmdLine + "커맨드를 사용했습니다.");
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void showHelp(L1PcInstance pc) {
		pc.sendPackets(new S_SystemMessage("============[부운영자 명령어]=============="));
		pc.sendPackets(new S_SystemMessage(".도움말.레벨.소환 .계정압류 .압류해제"));
		pc.sendPackets(new S_SystemMessage(".투명 .불투명.죽어라 .귀환 .소환 .피바"));
		pc.sendPackets(new S_SystemMessage(".감옥 .출소.변신.출두 .청소 .속도 ")); 
		pc.sendPackets(new S_SystemMessage(".채금.검사 "));
	}

	private void level(L1PcInstance pc, String param) {
		try {
			StringTokenizer tok = new StringTokenizer(param);
			int level = Integer.parseInt(tok.nextToken());
			if (level == pc.getLevel()) {
				return;
			}
			if (!IntRange.includes(level, 1, 81)) {
				pc.sendPackets(new S_SystemMessage("1~81의 범위에서 지정해 주세요."));
				return;
			}
			pc.setExp(ExpTable.getExpByLevel(level));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~레벨 [1~81]를 입력 해주세요."));
		}
	}

	private void recall(L1PcInstance pc, String pcName) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);

			if (target != null) {
				recallnow(pc, target);
			} else {
				pc.sendPackets(new S_SystemMessage("그러한 케릭터는 없습니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~소환 [케릭터명]으로 입력해 주세요."));
		}
	}

	private void recallnow(L1PcInstance pc, L1PcInstance target) {
		try {
			L1Teleport.teleportToTargetFront(target, pc, 2);
			pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(
					target.getName()).append("님을 소환했습니다.").toString()));
			target.sendPackets(new S_SystemMessage("게임 마스터에게 소환되었습니다."));
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
		}
	}
	private void visible(L1PcInstance pc) {
		try {
			pc.setGmInvis(false);
			pc.sendPackets(new S_Invis(pc.getId(), 0));
			L1World.getInstance().broadcastPacketToAll(
					new S_Invis(pc.getId(), 0)); // 추가
			pc.sendPackets(new S_SystemMessage("투명상태를 해제했습니다."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~불투명 커멘드 에러"));
		}
	}



	public void invisible(L1PcInstance pc) {
		try {
			if (!pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) {
				pc.setGmInvis(true);
				pc.sendPackets(new S_Invis(pc.getId(), 1));
				Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 1));
				Broadcaster.broadcastPacket(pc, new S_RemoveObject(pc));
				pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.INVISIBILITY, 0);
				pc.sendPackets(new S_SystemMessage("투명상태가 되었습니다."));
			}else{
				pc.setGmInvis(false);
				pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
				pc.sendPackets(new S_Invis(pc.getId(), 0));
				Broadcaster.broadcastPacket(pc, new S_OtherCharPacks(pc));
				pc.sendPackets(new S_SystemMessage("투명상태를 해제했습니다. "));
			}

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(" 커멘드 에러"));
		}
	}

	public void HpBar(L1PcInstance pc, String param, String arg) {
		if (arg.equalsIgnoreCase("켬")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.GMSTATUS_HPBAR, 0);
		} else if (arg.equalsIgnoreCase("끔")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.GMSTATUS_HPBAR);

			for (L1Object obj : pc.getNearObjects().getKnownObjects()) {
				if (isHpBarTarget(obj)) {
					pc.sendPackets(new S_HPMeter(obj.getId(), 0xFF));
				}
			}
		} else {
			pc.sendPackets(new S_SystemMessage("~피바 [켬,끔] 라고 입력해 주세요. "));
		}
	}

	public static boolean isHpBarTarget(L1Object obj) {
		if (obj instanceof L1MonsterInstance) {
			return true;
		}
		if (obj instanceof L1PcInstance) {
			return true;
		}
		if (obj instanceof L1SummonInstance) {
			return true;
		}
		if (obj instanceof L1PetInstance) {
			return true;
		}
		return false;
	}

	private void kill(L1PcInstance pc, String pcName) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);
			if(target.getAccessLevel() == Config.GMCODE){
				pc.sendPackets(new S_SystemMessage("운영자를 죽일순없습니다."));
				return;
			}
			if (target != null) {
				target.setCurrentHp(0);
				target.death(null);
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~죽어라 캐릭터명으로 입력해 주세요."));
		}
	}

	private void gmRoom(L1PcInstance gm, String room) {
		try {
			int i = 0;
			try {
				i = Integer.parseInt(room);
			} catch (NumberFormatException e) {
			}
			if (i == 1) {
				L1Teleport.teleport(gm, 32737, 32796, (short) 99, 5, false);
			} else if (i == 2) {
				L1Teleport.teleport(gm, 32644, 32955, (short) 0, 5, false);  //판도라
			} else if (i == 3) {
				L1Teleport.teleport(gm, 33429, 32814, (short) 4, 5, false);  //기란
			} else if (i == 4) {
				L1Teleport.teleport(gm, 32535, 32955, (short) 777, 5, false);  // 버땅 그신
			} else if (i == 5) {
				L1Teleport.teleport(gm, 32736, 32787, (short) 15, 5, false);  //캔트성
			} else if (i == 6) {
				L1Teleport.teleport(gm, 32735, 32788, (short) 29, 5, false);  //원다우드성
			} else if (i == 7) {
				L1Teleport.teleport(gm, 32572, 32826, (short) 64, 5, false);  //하이네성
			} else if (i == 8) {
				L1Teleport.teleport(gm, 32730, 32802, (short) 52, 5, false);  //기란성
			} else if (i == 9) {
				L1Teleport.teleport(gm, 32895, 32533, (short) 300, 5, false);  //아덴
			} else if (i == 10) {
				L1Teleport.teleport(gm, 32736, 32799, (short) 39, 5, false);  //감옥
			} else if (i == 11) {
				L1Teleport.teleport(gm, 32861, 32806, (short) 66, 5, false);  //지저성
			} else if (i == 12) {
				L1Teleport.teleport(gm, 33384, 32347, (short) 4, 5, false);  //용뼈
			} else if (i == 13) {
				L1Teleport.teleport(gm, 32738, 32797, (short) 509, 5, false);  //카오스대전
			} else if (i == 14) {
				L1Teleport.teleport(gm, 32866, 32640, (short) 501, 5, false);  //사탄의 늪
			} else if (i == 15) {
				L1Teleport.teleport(gm, 32603, 32766, (short) 506, 5, false);  //시야의놀이터
			} else if (i == 16) {
				L1Teleport.teleport(gm, 32769, 32827, (short) 610, 5, false);  //벗꽃;
			} else if (i == 17) {
				L1Teleport.teleport(gm, 34061, 32276, (short) 4, 5, false);  //벗꽃;
			} else {
				L1Location loc = GMCommandsConfig.ROOMS.get(room.toLowerCase());
				if (loc == null) {
					gm.sendPackets(new S_SystemMessage(".1운영자방   2판도라   3기란   4버땅(그신)  5켄트성"));
					gm.sendPackets(new S_SystemMessage(".6윈다우드성 7하이네성 8기란성 9아덴성 10 감옥 11지저성"));
					gm.sendPackets(new S_SystemMessage(".12용뼈 13카오스대전 14사탄의늪 15시야의놀이터   "));
					gm.sendPackets(new S_SystemMessage(".16벗꽃   "));
					return;
				}
				L1Teleport.teleport(gm, loc.getX(), loc.getY(), (short) loc
						.getMapId(), 5, false);
			}
		} catch (Exception exception) {
			gm.sendPackets(new S_SystemMessage("~귀환 [1~16] 또는 .귀환 [장소명]을 입력 해주세요.(장소명은 GMCommands.xml을 참조)"));
		}
	}


	private void prison(L1PcInstance pc, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);
			if (target != null) {
				prisonnow(pc, target);
			} else {
				pc.sendPackets(new S_SystemMessage("그런 이름의 캐릭터는 없습니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~감옥 캐릭터명"));
		}
	}

	private void prisonnow(L1PcInstance pc, L1PcInstance target) {
		try {
			int i = 32736;
			int j = 32799;
			int k = 39;
			L1Teleport.teleport(target, i, j, (short)k, 5, false);
			pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(
					target.getName()).append("님을 감옥으로 보냈습니다.").toString()));
			target.sendPackets(new S_SystemMessage("게임마스터에 의해 감옥에 수감되었습니다."));
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
		}
	}

	private void unprison(L1PcInstance pc, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);
			if (target != null) {
				unprisonnow(pc, target);
			} else {
				pc.sendPackets(new S_SystemMessage("그런 이름의 캐릭터는 없습니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~출소 캐릭터명"));
		}
	}

	private void unprisonnow(L1PcInstance pc, L1PcInstance target) {
		try {
			int i = 33700;
			int j = 32502;
			int k = 4;
			L1Teleport.teleport(target, i, j, (short)k, 5, false);
			pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(
					target.getName()).append("님을 감옥에서 출소시켰습니다.").toString()));
			target.sendPackets(new S_SystemMessage("게임마스터에 의해 감옥에서 출소되었습니다."));
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
		}
	}

	private void polymorph(L1PcInstance pc, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			String name = st.nextToken();
			int polyid = Integer.parseInt(st.nextToken());

			L1PcInstance pc1 = L1World.getInstance().getPlayer(name);

			if (pc1 == null) {
				pc.sendPackets(new S_ServerMessage(73, name)); // \f1%0은 게임을 하고
				// 있지 않습니다.
			} else {
				try {
					L1PolyMorph.doPoly(pc, polyid, 7200,
							L1PolyMorph.MORPH_BY_GM);
				} catch (Exception exception) {
					pc.sendPackets(new S_SystemMessage(
							"~변신 캐릭터명 그래픽번호 라고 입력해 주세요."));
				}
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~변신 캐릭터명 그래픽번호 라고 입력해 주세요."));
		}
	}


	private void moveToChar(L1PcInstance pc, String pcName) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);

			if (target != null) {
				L1Teleport.teleport(pc, target.getX(), target.getY(), target
						.getMapId(), 5, false);
				pc.sendPackets(new S_SystemMessage((new StringBuilder())
						.append(pcName).append("님에게 이동했습니다.").toString()));
			} else {
				pc.sendPackets(new S_SystemMessage((new StringBuilder())
						.append(pcName).append("님이 접속해있지 않습니다.").toString()));
			}
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage("~출두 [캐릭터명]을 입력 해주세요."));
		}
	}
	private void accbankick(L1PcInstance pc, String param) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(param);
			if(target.getAccessLevel() == Config.GMCODE){
				pc.sendPackets(new S_SystemMessage("운영자를 압류할순없습니다."));
				return;
			}
			if (target != null) {
				// 어카운트를 BAN 한다
				Account.ban(target.getAccountName());
				pc.sendPackets(new S_SystemMessage(target.getName()
						+ "님을 추방, 계정을 압류 했습니다."));
				L1World.getInstance().broadcastPacketToAll(
						new S_SystemMessage("게임에 적합하지 않은 행동으로 인해 "
								+ target.getName()
								+ " 은(는) 게임마스터에 의해 계정압류되었습니다"));
				target.sendPackets(new S_Disconnect());
			} else {
				pc.sendPackets(new S_SystemMessage(
						"그러한 이름의 캐릭터는 월드내에는 존재하지 않습니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("+계정압류 [캐릭터명]을 입력 해주세요."));
		}
	}

	private void deleteItem() {
		for (L1Object obj : L1World.getInstance().getObject()) {
			if (!(obj instanceof L1ItemInstance)) {
				continue;
			}

			L1ItemInstance item = (L1ItemInstance) obj;
			if (item.getX() == 0 && item.getY() == 0) { // 지면상의 아이템은 아니고, 누군가의
				// 소유물
				continue;
			}
			if (item.getItem().getItemId() == 40515) { // 정령의 돌
				continue;
			}
			if (L1HouseLocation.isInHouse(item.getX(), item.getY(), item
					.getMapId())) { // 아지트내
				continue;
			}
			// 무한대전시 대전장안 아이템 안사라지게 by 아스라이
			/*if (item.getMapId() >= 88 && item.getMapId() <= 98) {
					L1UltimateBattle ub = (L1UltimateBattle) UBTable.getInstance()
					.getAllUb();
					if (ub.isNowUb()) {
						continue;
					}
				}*/

			List<L1PcInstance> players = L1World.getInstance()
					.getVisiblePlayer(item, Config.ALT_ITEM_DELETION_RANGE);
			if (players.isEmpty()) { // 지정 범위내에 플레이어가 없으면 삭제
				L1Inventory groundInventory = L1World
						.getInstance()
						.getInventory(item.getX(), item.getY(), item.getMapId());
				groundInventory.removeItem(item);
			}
		}
		L1World.getInstance().broadcastServerMessage(
				"월드맵상의 아이템이 운영자에 의해 삭제되었습니다.");
	}

	private void speed(L1PcInstance pc) {
		try {
			L1BuffUtil.haste(pc, 3600 * 1000);
			L1BuffUtil.brave(pc, 3600 * 1000);
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~속도 커멘드 에러"));
		}
	}
	private void accountdel(L1PcInstance gm, String param) {

		try {

			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();

			Connection con = null;
			Connection con2 = null;
			PreparedStatement pstm = null;
			PreparedStatement pstm2 = null;
			ResultSet find = null;
			String findcha = null;

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM characters WHERE char_name=?");
			pstm.setString(1, pcName);
			find = pstm.executeQuery();

			while (find.next()) {
				findcha = find.getString(1);
			}

			if (findcha == null) {
				gm.sendPackets(new S_SystemMessage("DB에 " + pcName
						+ " 케릭명이 존재 하지 않습니다"));

				con.close();
				pstm.close();
				find.close();

			} else {
				con2 = L1DatabaseFactory.getInstance().getConnection();
				pstm2 = con
						.prepareStatement("UPDATE accounts SET banned = 0 WHERE login= ?");
				pstm2.setString(1, findcha);
				pstm2.execute();

				gm
				.sendPackets(new S_SystemMessage(pcName
						+ " 의 계정압류를 해제 하였습니다"));

				con.close();
				pstm.close();
				find.close();
				con2.close();
				pstm2.close();
			}

		} catch (Exception exception) {
			gm.sendPackets(new S_SystemMessage("~압류해제 케릭명으로 입력해주세요."));
		}
	}


	private void chatng(L1PcInstance gm, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			String name = st.nextToken();
			int time = Integer.parseInt(st.nextToken());

			L1PcInstance pc = L1World.getInstance().getPlayer(name);

			if (pc != null) {
				pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_CHAT_PROHIBITED,
						time * 60 * 1000);
				pc.sendPackets(new S_SkillIconGFX(36, time * 60));
				pc.sendPackets(new S_ServerMessage(286, String.valueOf(time))); // \f3게임에
				// 적합하지
				// 않는
				// 행동이기
				// (위해)때문에,
				// 향후%0분간
				// 채팅을
				// 금지합니다.
				gm.sendPackets(new S_ServerMessage(287, name)); // %0의 채팅을
				// 금지했습니다.
				L1World.getInstance().broadcastServerMessage(
						"\\fY게임마스터가" + pc.getName() + "\\fY의 채팅을 금지시켰습니다.");// 추가

			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage("~채금 캐릭터명 시간(분)이라고 입력해 주세요."));
		}
	}

	public void CheckCha(L1PcInstance pc, String cmdName, String arg) {
		Connection c = null;
		PreparedStatement p = null;
		PreparedStatement p1 = null;
		ResultSet r = null;
		ResultSet r1 = null;
		try {				
			StringTokenizer st = new StringTokenizer(arg);
			String charname = st.nextToken();
			String type = st.nextToken();

			c = L1DatabaseFactory.getInstance().getConnection();

			String itemname;
			int searchCount = 0;
			if (type.equalsIgnoreCase("인벤")){	
				try {
					// 캐릭 오브젝트 ID 검색 1=objid 2=charname
					p = c.prepareStatement("SELECT objid, char_name FROM characters WHERE char_name = '" + charname + "'");
					r = p.executeQuery();
					while(r.next()){
						pc.sendPackets(new S_SystemMessage("\\fW** 검사: "+type+" 캐릭: " + charname + " **"));
						L1PcInstance target = L1World.getInstance().getPlayer(charname);			
						if (target != null) target.saveInventory();						
						// 캐릭 아이템 검색 1-itemid 2-인챈 3-착용 4-수량 5-이름 6-축복 7-속성
						p1 = c.prepareStatement("SELECT item_id,enchantlvl,is_equipped,count,item_name,bless,attr_enchantlvl " +
								"FROM character_items WHERE char_id = '" + r.getInt(1) + "' ORDER BY 3 DESC,2 DESC, 1 ASC");
						r1 = p1.executeQuery();				
						while(r1.next()){
							itemname = getInvenItemMsg(r1.getInt(1),r1.getInt(2),r1.getInt(3),r1.getInt(4),r1.getString(5),r1.getInt(6),r1.getInt(7));
							pc.sendPackets(new S_SystemMessage("\\fU"+ ++searchCount +". " + itemname));
							itemname = "";
						}
						pc.sendPackets(new S_SystemMessage("\\fW** 총 "+searchCount+"건의 아이템이 검색 되었습니다 **"));
					}					
				} catch (Exception e) {
					pc.sendPackets(new S_SystemMessage("\\fW** [" + charname + "] 캐릭 검색 오류 **"));
				}
			} else if (type.equalsIgnoreCase("창고")){
				try {
					p = c.prepareStatement("SELECT account_name, char_name FROM characters WHERE char_name = '" + charname + "'");
					r = p.executeQuery();
					while (r.next()){
						pc.sendPackets(new S_SystemMessage("\\fW** 검사: "+type+" 캐릭: " + charname + "(" + r.getString(1) + ") **"));
						//캐릭 창고 검색 1-itemid 2-인챈 3-수량 4-이름 5-축복 6-속성
						p1 = c.prepareStatement("SELECT item_id,enchantlvl,count,item_name,bless,attr_enchantlvl FROM character_warehouse " +
								"WHERE account_name = '" + r.getString(1) + "' ORDER BY 2 DESC, 1 ASC");
						r1 = p1.executeQuery();
						while (r1.next()){
							itemname = getInvenItemMsg(r1.getInt(1),r1.getInt(2),0,r1.getInt(3),r1.getString(4),r1.getInt(5),r1.getInt(6));
							pc.sendPackets(new S_SystemMessage("\\fU"+ ++searchCount +". " + itemname));
							itemname = "";
						}
						pc.sendPackets(new S_SystemMessage("\\fW** 총 "+searchCount+"건의 아이템이 검색 되었습니다 **"));
					}
				} catch (Exception e) {
					pc.sendPackets(new S_SystemMessage("\\fW** [" + charname + "] 캐릭 검색 오류 **"));
				}
			} else if (type.equalsIgnoreCase("요정창고")){				
				try {
					p = c.prepareStatement("SELECT account_name, char_name FROM characters WHERE char_name = '" + charname + "'");
					r = p.executeQuery();
					while (r.next()){
						pc.sendPackets(new S_SystemMessage("\\fW** 검사: "+type+" 캐릭: " + charname + "(" + r.getString(1) + ") **"));
						//캐릭 요정창고 검색 1-itemid 2-인챈 3-수량 4-이름 5-축복 6-속성
						p1 = c.prepareStatement("SELECT item_id,enchantlvl,count,item_name,bless,attr_enchantlvl FROM character_elf_warehouse " +
								"WHERE account_name = '" + r.getString(1) + "' ORDER BY 2 DESC, 1 ASC");
						r1 = p1.executeQuery();
						while (r1.next()){
							itemname = getInvenItemMsg(r1.getInt(1),r1.getInt(2),0,r1.getInt(3),r1.getString(4),r1.getInt(5),r1.getInt(6));
							pc.sendPackets(new S_SystemMessage("\\fU"+ ++searchCount +". " + itemname));
							itemname = "";
						}
						pc.sendPackets(new S_SystemMessage("\\fW** 총 "+searchCount+"건의 아이템이 검색 되었습니다 **"));
					}
				} catch (Exception e) {
					pc.sendPackets(new S_SystemMessage("\\fW** [" + charname + "] 캐릭 검색 오류 **"));
				}
			}			
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			pc.sendPackets(new S_SystemMessage("~검사 [캐릭명] [인벤,창고,요정창고]"));
		} finally {
			SQLUtil.close(r1);SQLUtil.close(p1);
			SQLUtil.close(r);SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	private String getInvenItemMsg(int itemid, int enchant, int equip, int count, String itemname, int bless, int attr){
		StringBuilder name = new StringBuilder();
		// +9 축복받은 실프의 흑왕도 (착용)		
		// 인챈
		if (enchant > 0) {
			name.append("+" + enchant + " ");
		} else if (enchant == 0) {
			name.append("");
		} else if (enchant < 0) {
			name.append(String.valueOf(enchant) + " ");
		}
		// 축복
		switch (bless) {
		case 0:name.append("축복받은 ");break;
		case 1:name.append("");break;		
		case 2:name.append("저주받은 ");break;
		default: break;
		}
		// 속성
		switch(attr){
		case 1: name.append("$6115 "); break;
		case 2: name.append("$6116 "); break;
		case 3: name.append("$6117 "); break;
		case 4: name.append("$6118 "); break;
		case 5: name.append("$6119 "); break;
		case 6: name.append("$6120 "); break;
		case 7: name.append("$6121 "); break;
		case 8: name.append("$6122 "); break;
		case 9: name.append("$6123 "); break;
		case 10: name.append("$6124 "); break;
		case 11: name.append("$6125 "); break;
		case 12: name.append("$6126 "); break;
		default: break;
		}
		// 이름
		name.append(itemname + " ");
		// 착용여부
		if (equip == 1){
			name.append("(착용)");
		}
		// 카운트
		if (count > 1){
			name.append("(" + count + ")");
		}
		return name.toString();
	}

}

