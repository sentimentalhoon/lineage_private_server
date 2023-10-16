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
		// ������ ��������� Ŀ���, �� ���Ĵ� ������ �ܶ����� �� �Ķ���ͷμ� ����Ѵ�
		String cmd = token.nextToken();
		String param = "";
		while (token.hasMoreTokens()) {
			param = new StringBuilder(param).append(token.nextToken()).append(
					' ').toString();
		}
		param = param.trim();
		try {
			if(pc.getAccessLevel() == 300){ 
				if (cmd.equalsIgnoreCase("����")) {
					showHelp(pc);   
				} else if (cmd.equalsIgnoreCase("����")) {
					level(pc, param);
				} else if (cmd.equalsIgnoreCase("��ȯ")) {
					recall(pc, param);
				} else if (cmd.equalsIgnoreCase("����")) {
					invisible(pc);
				} else if (cmd.equalsIgnoreCase("������")) {
					visible(pc);
				} else if (cmd.equalsIgnoreCase("�׾��")) {
					kill(pc, param);
				} else if (cmd.equalsIgnoreCase("��ȯ")) {
					gmRoom(pc, param);
				} else if (cmd.equalsIgnoreCase("����")) {
					prison(pc, param);
				} else if (cmd.equalsIgnoreCase("��������")){				
					GMCommands.getInstance().allGoodBuff();
				} else if (cmd.equalsIgnoreCase("����")) {
					GMCommands.getInstance().nolza(pc);
				} else if (cmd.equalsIgnoreCase("���")) {
					unprison(pc, param);
				} else if (cmd.equalsIgnoreCase("����")) {
					polymorph(pc, param);
				} else if (cmd.equalsIgnoreCase("���")) {
					moveToChar(pc, param);
				} else if (cmd.equalsIgnoreCase("�����з�")) {
					accbankick(pc, param);
				} else if (cmd.equalsIgnoreCase("�ӵ�")) {
					speed(pc);
					///////////////////////////////////////////////////////////////////////////////////
				} else if (cmd.equalsIgnoreCase("Į")) {
					pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.USERSTATUS_ATTACK, 0);
					UserCommands.tell(pc);//�ڷ�Ǯ�⵵ �ѹ� ���ָ� �ֺ������� �ٷ� �����̷� ���̰Ե�
				} else if (cmd.equalsIgnoreCase("Į��")) {
					pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.USERSTATUS_ATTACK);
					UserCommands.tell(pc);
					////////////////////////////////////////////////////////////////////////////////
				} else if (cmd.equalsIgnoreCase(".") || cmd.equalsIgnoreCase("�ڷ�Ǯ��")) {
					UserCommands.tell(pc);		
				} else if (cmd.startsWith("�з�����")) {
					accountdel(pc, param);
				} else if (cmd.equalsIgnoreCase("û��")) {
					deleteItem();
				} else if (cmd.equalsIgnoreCase("�ǹ�")) {
					HpBar(pc, param, param);
				} else if (cmd.equalsIgnoreCase("�˻�")) {
					CheckCha(pc, param, param);
				} else if (cmd.equalsIgnoreCase("ä��")) {
					chatng(pc, param);
				} 
			}
			else {
				String msg = new StringBuilder().append("Ŀ��壺").append(cmd)
						.append("�� �������� �ʴ�, �Ǵ� ��������� �����ϴ�.").toString();
				pc.sendPackets(new S_SystemMessage(msg));
			}
			_log.info("�ο�ڰ�." + cmdLine + "Ŀ�ǵ带 ����߽��ϴ�.");
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void showHelp(L1PcInstance pc) {
		pc.sendPackets(new S_SystemMessage("============[�ο�� ��ɾ�]=============="));
		pc.sendPackets(new S_SystemMessage(".����.����.��ȯ .�����з� .�з�����"));
		pc.sendPackets(new S_SystemMessage(".���� .������.�׾�� .��ȯ .��ȯ .�ǹ�"));
		pc.sendPackets(new S_SystemMessage(".���� .���.����.��� .û�� .�ӵ� ")); 
		pc.sendPackets(new S_SystemMessage(".ä��.�˻� "));
	}

	private void level(L1PcInstance pc, String param) {
		try {
			StringTokenizer tok = new StringTokenizer(param);
			int level = Integer.parseInt(tok.nextToken());
			if (level == pc.getLevel()) {
				return;
			}
			if (!IntRange.includes(level, 1, 81)) {
				pc.sendPackets(new S_SystemMessage("1~81�� �������� ������ �ּ���."));
				return;
			}
			pc.setExp(ExpTable.getExpByLevel(level));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~���� [1~81]�� �Է� ���ּ���."));
		}
	}

	private void recall(L1PcInstance pc, String pcName) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);

			if (target != null) {
				recallnow(pc, target);
			} else {
				pc.sendPackets(new S_SystemMessage("�׷��� �ɸ��ʹ� �����ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~��ȯ [�ɸ��͸�]���� �Է��� �ּ���."));
		}
	}

	private void recallnow(L1PcInstance pc, L1PcInstance target) {
		try {
			L1Teleport.teleportToTargetFront(target, pc, 2);
			pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(
					target.getName()).append("���� ��ȯ�߽��ϴ�.").toString()));
			target.sendPackets(new S_SystemMessage("���� �����Ϳ��� ��ȯ�Ǿ����ϴ�."));
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
		}
	}
	private void visible(L1PcInstance pc) {
		try {
			pc.setGmInvis(false);
			pc.sendPackets(new S_Invis(pc.getId(), 0));
			L1World.getInstance().broadcastPacketToAll(
					new S_Invis(pc.getId(), 0)); // �߰�
			pc.sendPackets(new S_SystemMessage("������¸� �����߽��ϴ�."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~������ Ŀ��� ����"));
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
				pc.sendPackets(new S_SystemMessage("������°� �Ǿ����ϴ�."));
			}else{
				pc.setGmInvis(false);
				pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
				pc.sendPackets(new S_Invis(pc.getId(), 0));
				Broadcaster.broadcastPacket(pc, new S_OtherCharPacks(pc));
				pc.sendPackets(new S_SystemMessage("������¸� �����߽��ϴ�. "));
			}

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(" Ŀ��� ����"));
		}
	}

	public void HpBar(L1PcInstance pc, String param, String arg) {
		if (arg.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.GMSTATUS_HPBAR, 0);
		} else if (arg.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.GMSTATUS_HPBAR);

			for (L1Object obj : pc.getNearObjects().getKnownObjects()) {
				if (isHpBarTarget(obj)) {
					pc.sendPackets(new S_HPMeter(obj.getId(), 0xFF));
				}
			}
		} else {
			pc.sendPackets(new S_SystemMessage("~�ǹ� [��,��] ��� �Է��� �ּ���. "));
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
				pc.sendPackets(new S_SystemMessage("��ڸ� ���ϼ������ϴ�."));
				return;
			}
			if (target != null) {
				target.setCurrentHp(0);
				target.death(null);
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~�׾�� ĳ���͸����� �Է��� �ּ���."));
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
				L1Teleport.teleport(gm, 32644, 32955, (short) 0, 5, false);  //�ǵ���
			} else if (i == 3) {
				L1Teleport.teleport(gm, 33429, 32814, (short) 4, 5, false);  //���
			} else if (i == 4) {
				L1Teleport.teleport(gm, 32535, 32955, (short) 777, 5, false);  // ���� �׽�
			} else if (i == 5) {
				L1Teleport.teleport(gm, 32736, 32787, (short) 15, 5, false);  //ĵƮ��
			} else if (i == 6) {
				L1Teleport.teleport(gm, 32735, 32788, (short) 29, 5, false);  //���ٿ�强
			} else if (i == 7) {
				L1Teleport.teleport(gm, 32572, 32826, (short) 64, 5, false);  //���̳׼�
			} else if (i == 8) {
				L1Teleport.teleport(gm, 32730, 32802, (short) 52, 5, false);  //�����
			} else if (i == 9) {
				L1Teleport.teleport(gm, 32895, 32533, (short) 300, 5, false);  //�Ƶ�
			} else if (i == 10) {
				L1Teleport.teleport(gm, 32736, 32799, (short) 39, 5, false);  //����
			} else if (i == 11) {
				L1Teleport.teleport(gm, 32861, 32806, (short) 66, 5, false);  //������
			} else if (i == 12) {
				L1Teleport.teleport(gm, 33384, 32347, (short) 4, 5, false);  //���
			} else if (i == 13) {
				L1Teleport.teleport(gm, 32738, 32797, (short) 509, 5, false);  //ī��������
			} else if (i == 14) {
				L1Teleport.teleport(gm, 32866, 32640, (short) 501, 5, false);  //��ź�� ��
			} else if (i == 15) {
				L1Teleport.teleport(gm, 32603, 32766, (short) 506, 5, false);  //�þ��ǳ�����
			} else if (i == 16) {
				L1Teleport.teleport(gm, 32769, 32827, (short) 610, 5, false);  //����;
			} else if (i == 17) {
				L1Teleport.teleport(gm, 34061, 32276, (short) 4, 5, false);  //����;
			} else {
				L1Location loc = GMCommandsConfig.ROOMS.get(room.toLowerCase());
				if (loc == null) {
					gm.sendPackets(new S_SystemMessage(".1��ڹ�   2�ǵ���   3���   4����(�׽�)  5��Ʈ��"));
					gm.sendPackets(new S_SystemMessage(".6���ٿ�强 7���̳׼� 8����� 9�Ƶ��� 10 ���� 11������"));
					gm.sendPackets(new S_SystemMessage(".12��� 13ī�������� 14��ź�Ǵ� 15�þ��ǳ�����   "));
					gm.sendPackets(new S_SystemMessage(".16����   "));
					return;
				}
				L1Teleport.teleport(gm, loc.getX(), loc.getY(), (short) loc
						.getMapId(), 5, false);
			}
		} catch (Exception exception) {
			gm.sendPackets(new S_SystemMessage("~��ȯ [1~16] �Ǵ� .��ȯ [��Ҹ�]�� �Է� ���ּ���.(��Ҹ��� GMCommands.xml�� ����)"));
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
				pc.sendPackets(new S_SystemMessage("�׷� �̸��� ĳ���ʹ� �����ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~���� ĳ���͸�"));
		}
	}

	private void prisonnow(L1PcInstance pc, L1PcInstance target) {
		try {
			int i = 32736;
			int j = 32799;
			int k = 39;
			L1Teleport.teleport(target, i, j, (short)k, 5, false);
			pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(
					target.getName()).append("���� �������� ���½��ϴ�.").toString()));
			target.sendPackets(new S_SystemMessage("���Ӹ����Ϳ� ���� ������ �����Ǿ����ϴ�."));
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
				pc.sendPackets(new S_SystemMessage("�׷� �̸��� ĳ���ʹ� �����ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~��� ĳ���͸�"));
		}
	}

	private void unprisonnow(L1PcInstance pc, L1PcInstance target) {
		try {
			int i = 33700;
			int j = 32502;
			int k = 4;
			L1Teleport.teleport(target, i, j, (short)k, 5, false);
			pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(
					target.getName()).append("���� �������� ��ҽ��׽��ϴ�.").toString()));
			target.sendPackets(new S_SystemMessage("���Ӹ����Ϳ� ���� �������� ��ҵǾ����ϴ�."));
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
				pc.sendPackets(new S_ServerMessage(73, name)); // \f1%0�� ������ �ϰ�
				// ���� �ʽ��ϴ�.
			} else {
				try {
					L1PolyMorph.doPoly(pc, polyid, 7200,
							L1PolyMorph.MORPH_BY_GM);
				} catch (Exception exception) {
					pc.sendPackets(new S_SystemMessage(
							"~���� ĳ���͸� �׷��ȹ�ȣ ��� �Է��� �ּ���."));
				}
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~���� ĳ���͸� �׷��ȹ�ȣ ��� �Է��� �ּ���."));
		}
	}


	private void moveToChar(L1PcInstance pc, String pcName) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);

			if (target != null) {
				L1Teleport.teleport(pc, target.getX(), target.getY(), target
						.getMapId(), 5, false);
				pc.sendPackets(new S_SystemMessage((new StringBuilder())
						.append(pcName).append("�Կ��� �̵��߽��ϴ�.").toString()));
			} else {
				pc.sendPackets(new S_SystemMessage((new StringBuilder())
						.append(pcName).append("���� ���������� �ʽ��ϴ�.").toString()));
			}
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage("~��� [ĳ���͸�]�� �Է� ���ּ���."));
		}
	}
	private void accbankick(L1PcInstance pc, String param) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(param);
			if(target.getAccessLevel() == Config.GMCODE){
				pc.sendPackets(new S_SystemMessage("��ڸ� �з��Ҽ������ϴ�."));
				return;
			}
			if (target != null) {
				// ��ī��Ʈ�� BAN �Ѵ�
				Account.ban(target.getAccountName());
				pc.sendPackets(new S_SystemMessage(target.getName()
						+ "���� �߹�, ������ �з� �߽��ϴ�."));
				L1World.getInstance().broadcastPacketToAll(
						new S_SystemMessage("���ӿ� �������� ���� �ൿ���� ���� "
								+ target.getName()
								+ " ��(��) ���Ӹ����Ϳ� ���� �����з��Ǿ����ϴ�"));
				target.sendPackets(new S_Disconnect());
			} else {
				pc.sendPackets(new S_SystemMessage(
						"�׷��� �̸��� ĳ���ʹ� ���峻���� �������� �ʽ��ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("+�����з� [ĳ���͸�]�� �Է� ���ּ���."));
		}
	}

	private void deleteItem() {
		for (L1Object obj : L1World.getInstance().getObject()) {
			if (!(obj instanceof L1ItemInstance)) {
				continue;
			}

			L1ItemInstance item = (L1ItemInstance) obj;
			if (item.getX() == 0 && item.getY() == 0) { // ������� �������� �ƴϰ�, ��������
				// ������
				continue;
			}
			if (item.getItem().getItemId() == 40515) { // ������ ��
				continue;
			}
			if (L1HouseLocation.isInHouse(item.getX(), item.getY(), item
					.getMapId())) { // ����Ʈ��
				continue;
			}
			// ���Ѵ����� ������� ������ �Ȼ������ by �ƽ�����
			/*if (item.getMapId() >= 88 && item.getMapId() <= 98) {
					L1UltimateBattle ub = (L1UltimateBattle) UBTable.getInstance()
					.getAllUb();
					if (ub.isNowUb()) {
						continue;
					}
				}*/

			List<L1PcInstance> players = L1World.getInstance()
					.getVisiblePlayer(item, Config.ALT_ITEM_DELETION_RANGE);
			if (players.isEmpty()) { // ���� �������� �÷��̾ ������ ����
				L1Inventory groundInventory = L1World
						.getInstance()
						.getInventory(item.getX(), item.getY(), item.getMapId());
				groundInventory.removeItem(item);
			}
		}
		L1World.getInstance().broadcastServerMessage(
				"����ʻ��� �������� ��ڿ� ���� �����Ǿ����ϴ�.");
	}

	private void speed(L1PcInstance pc) {
		try {
			L1BuffUtil.haste(pc, 3600 * 1000);
			L1BuffUtil.brave(pc, 3600 * 1000);
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("~�ӵ� Ŀ��� ����"));
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
				gm.sendPackets(new S_SystemMessage("DB�� " + pcName
						+ " �ɸ����� ���� ���� �ʽ��ϴ�"));

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
						+ " �� �����з��� ���� �Ͽ����ϴ�"));

				con.close();
				pstm.close();
				find.close();
				con2.close();
				pstm2.close();
			}

		} catch (Exception exception) {
			gm.sendPackets(new S_SystemMessage("~�з����� �ɸ������� �Է����ּ���."));
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
				pc.sendPackets(new S_ServerMessage(286, String.valueOf(time))); // \f3���ӿ�
				// ��������
				// �ʴ�
				// �ൿ�̱�
				// (����)������,
				// ����%0�а�
				// ä����
				// �����մϴ�.
				gm.sendPackets(new S_ServerMessage(287, name)); // %0�� ä����
				// �����߽��ϴ�.
				L1World.getInstance().broadcastServerMessage(
						"\\fY���Ӹ����Ͱ�" + pc.getName() + "\\fY�� ä���� �������׽��ϴ�.");// �߰�

			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage("~ä�� ĳ���͸� �ð�(��)�̶�� �Է��� �ּ���."));
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
			if (type.equalsIgnoreCase("�κ�")){	
				try {
					// ĳ�� ������Ʈ ID �˻� 1=objid 2=charname
					p = c.prepareStatement("SELECT objid, char_name FROM characters WHERE char_name = '" + charname + "'");
					r = p.executeQuery();
					while(r.next()){
						pc.sendPackets(new S_SystemMessage("\\fW** �˻�: "+type+" ĳ��: " + charname + " **"));
						L1PcInstance target = L1World.getInstance().getPlayer(charname);			
						if (target != null) target.saveInventory();						
						// ĳ�� ������ �˻� 1-itemid 2-��æ 3-���� 4-���� 5-�̸� 6-�ູ 7-�Ӽ�
						p1 = c.prepareStatement("SELECT item_id,enchantlvl,is_equipped,count,item_name,bless,attr_enchantlvl " +
								"FROM character_items WHERE char_id = '" + r.getInt(1) + "' ORDER BY 3 DESC,2 DESC, 1 ASC");
						r1 = p1.executeQuery();				
						while(r1.next()){
							itemname = getInvenItemMsg(r1.getInt(1),r1.getInt(2),r1.getInt(3),r1.getInt(4),r1.getString(5),r1.getInt(6),r1.getInt(7));
							pc.sendPackets(new S_SystemMessage("\\fU"+ ++searchCount +". " + itemname));
							itemname = "";
						}
						pc.sendPackets(new S_SystemMessage("\\fW** �� "+searchCount+"���� �������� �˻� �Ǿ����ϴ� **"));
					}					
				} catch (Exception e) {
					pc.sendPackets(new S_SystemMessage("\\fW** [" + charname + "] ĳ�� �˻� ���� **"));
				}
			} else if (type.equalsIgnoreCase("â��")){
				try {
					p = c.prepareStatement("SELECT account_name, char_name FROM characters WHERE char_name = '" + charname + "'");
					r = p.executeQuery();
					while (r.next()){
						pc.sendPackets(new S_SystemMessage("\\fW** �˻�: "+type+" ĳ��: " + charname + "(" + r.getString(1) + ") **"));
						//ĳ�� â�� �˻� 1-itemid 2-��æ 3-���� 4-�̸� 5-�ູ 6-�Ӽ�
						p1 = c.prepareStatement("SELECT item_id,enchantlvl,count,item_name,bless,attr_enchantlvl FROM character_warehouse " +
								"WHERE account_name = '" + r.getString(1) + "' ORDER BY 2 DESC, 1 ASC");
						r1 = p1.executeQuery();
						while (r1.next()){
							itemname = getInvenItemMsg(r1.getInt(1),r1.getInt(2),0,r1.getInt(3),r1.getString(4),r1.getInt(5),r1.getInt(6));
							pc.sendPackets(new S_SystemMessage("\\fU"+ ++searchCount +". " + itemname));
							itemname = "";
						}
						pc.sendPackets(new S_SystemMessage("\\fW** �� "+searchCount+"���� �������� �˻� �Ǿ����ϴ� **"));
					}
				} catch (Exception e) {
					pc.sendPackets(new S_SystemMessage("\\fW** [" + charname + "] ĳ�� �˻� ���� **"));
				}
			} else if (type.equalsIgnoreCase("����â��")){				
				try {
					p = c.prepareStatement("SELECT account_name, char_name FROM characters WHERE char_name = '" + charname + "'");
					r = p.executeQuery();
					while (r.next()){
						pc.sendPackets(new S_SystemMessage("\\fW** �˻�: "+type+" ĳ��: " + charname + "(" + r.getString(1) + ") **"));
						//ĳ�� ����â�� �˻� 1-itemid 2-��æ 3-���� 4-�̸� 5-�ູ 6-�Ӽ�
						p1 = c.prepareStatement("SELECT item_id,enchantlvl,count,item_name,bless,attr_enchantlvl FROM character_elf_warehouse " +
								"WHERE account_name = '" + r.getString(1) + "' ORDER BY 2 DESC, 1 ASC");
						r1 = p1.executeQuery();
						while (r1.next()){
							itemname = getInvenItemMsg(r1.getInt(1),r1.getInt(2),0,r1.getInt(3),r1.getString(4),r1.getInt(5),r1.getInt(6));
							pc.sendPackets(new S_SystemMessage("\\fU"+ ++searchCount +". " + itemname));
							itemname = "";
						}
						pc.sendPackets(new S_SystemMessage("\\fW** �� "+searchCount+"���� �������� �˻� �Ǿ����ϴ� **"));
					}
				} catch (Exception e) {
					pc.sendPackets(new S_SystemMessage("\\fW** [" + charname + "] ĳ�� �˻� ���� **"));
				}
			}			
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			pc.sendPackets(new S_SystemMessage("~�˻� [ĳ����] [�κ�,â��,����â��]"));
		} finally {
			SQLUtil.close(r1);SQLUtil.close(p1);
			SQLUtil.close(r);SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	private String getInvenItemMsg(int itemid, int enchant, int equip, int count, String itemname, int bless, int attr){
		StringBuilder name = new StringBuilder();
		// +9 �ູ���� ������ ��յ� (����)		
		// ��æ
		if (enchant > 0) {
			name.append("+" + enchant + " ");
		} else if (enchant == 0) {
			name.append("");
		} else if (enchant < 0) {
			name.append(String.valueOf(enchant) + " ");
		}
		// �ູ
		switch (bless) {
		case 0:name.append("�ູ���� ");break;
		case 1:name.append("");break;		
		case 2:name.append("���ֹ��� ");break;
		default: break;
		}
		// �Ӽ�
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
		// �̸�
		name.append(itemname + " ");
		// ���뿩��
		if (equip == 1){
			name.append("(����)");
		}
		// ī��Ʈ
		if (count > 1){
			name.append("(" + count + ")");
		}
		return name.toString();
	}

}

