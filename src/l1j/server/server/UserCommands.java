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

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.ADVANCE_SPIRIT;
import static l1j.server.server.model.skill.L1SkillId.BLESS_WEAPON;
import static l1j.server.server.model.skill.L1SkillId.EARTH_BIND;
import static l1j.server.server.model.skill.L1SkillId.FREEZING_BLIZZARD;
import static l1j.server.server.model.skill.L1SkillId.FREEZING_BREATH;
import static l1j.server.server.model.skill.L1SkillId.ICE_LANCE;
import static l1j.server.server.model.skill.L1SkillId.IRON_SKIN;
import static l1j.server.server.model.skill.L1SkillId.MOB_BASILL;
import static l1j.server.server.model.skill.L1SkillId.MOB_COCA;
import static l1j.server.server.model.skill.L1SkillId.PHYSICAL_ENCHANT_DEX;
import static l1j.server.server.model.skill.L1SkillId.PHYSICAL_ENCHANT_STR;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import l1j.server.Base64;
import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.Warehouse.PackageWarehouse;
import l1j.server.server.datatables.AuctionBTable;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.LetterTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_LetterList;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_Serchdrop;
import l1j.server.server.serverpackets.S_Serchdrop2;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_UserCommands;
import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.SQLUtil;
import server.manager.eva;

//Referenced classes of package l1j.server.server:
//ClientThread, Shutdown, IpTable, MobTable,
//PolyTable, IdFactory

public class UserCommands {

	private static UserCommands _instance;

	private UserCommands() {}

	public static UserCommands getInstance() {
		if (_instance == null) {
			_instance = new UserCommands();
		}
		return _instance;
	}

	public void handleCommands(L1PcInstance pc, String cmdLine) {		
		StringTokenizer token = new StringTokenizer(cmdLine);
		// ������ ��������� Ŀ�ǵ�, �� ���Ĵ� ������ �ܶ����� �� �Ķ���ͷμ� ����Ѵ�
		String cmd = token.nextToken();
		String param = "";
		while (token.hasMoreTokens()) {
			param = new StringBuilder(param).append(token.nextToken()).append(' ').toString();
		}
		param = param.trim();

		if (cmd.equalsIgnoreCase("����")) {
			showHelp(pc);
		} else if (cmd.equalsIgnoreCase("����")){
			infoitem(pc, param);
			/*
		} else if (cmd.equalsIgnoreCase("���ݱ�ȯ")) {
			MultiTrade(pc, param);
			 */
		} else if (cmd.equalsIgnoreCase("��ġ��")) {
			adenaAdd(pc);
		} else if (cmd.equalsIgnoreCase("��ŷ")){
			infoRanking(pc);
		} else if (cmd.equalsIgnoreCase("����")){
			buff(pc);
		} else if (cmd.equalsIgnoreCase(".") || cmd.equalsIgnoreCase("�ڷ�Ǯ��")) {
			tell(pc);		
		} else if(cmd.equalsIgnoreCase("���ͼ�ȯ")) {
			CallClan(pc);
		} else if(cmd.equalsIgnoreCase("������")){
			check(pc);
		} else if (cmd.equalsIgnoreCase("��ȣ����")){
			changePassword(pc, param);
		} else if(cmd.equalsIgnoreCase("�ɸ�����") || cmd.equalsIgnoreCase("ĳ������")) {
			charname(pc, param);
		} else if (cmd.equalsIgnoreCase("���")) { 
			serchdroplist(pc, param);
		} else if (cmd.equalsIgnoreCase("�����")) {
			serchdroplist2(pc, param) ;
		} else if (cmd.equalsIgnoreCase("ô��") || cmd.equalsIgnoreCase("����")) {
			Hunt(pc, param);
		} else if (cmd.equalsIgnoreCase("����") || cmd.equalsIgnoreCase("���̵��")) { 
			age(pc, param);
		} else if (cmd.equalsIgnoreCase("�̸�") || cmd.equalsIgnoreCase("�̸����")) { 
			username(pc, param);
		} else if (cmd.equalsIgnoreCase("����ð�")) { 
			entertime(pc);
		} else if(cmd.equalsIgnoreCase("���ø�Ʈ")) {
			ment(pc, cmd, param); 
		} else if(cmd.equalsIgnoreCase("����")) {
			bugracement(pc, cmd, param); 
		} else if(cmd.equalsIgnoreCase("�̵�") || cmd.equalsIgnoreCase("��ȯ")) {
			movement(pc, cmd, param); 
		} else if (cmd.equalsIgnoreCase("�����")) {
			quize(pc, param);
		} else if (cmd.equalsIgnoreCase("��������")) {
			quize1(pc, param);
		} else if (cmd.equalsIgnoreCase("�����")) {
			moveToChar(pc, param);
		} else if(cmd.equalsIgnoreCase("������Ƽ")){
			ClanParty(pc);
		} else if (cmd.equalsIgnoreCase("������Ƽ")){
			Party(pc, param); //by.���� //param<<<<<�̰Ŷ��߰����ּ��� ���� ��Ծ���ϴ��˼� �Ф�
		} else if (cmd.equalsIgnoreCase("Į")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.USERSTATUS_ATTACK, 0);
			tell(pc);//�ڷ�Ǯ�⵵ �ѹ� ���ָ� �ֺ������� �ٷ� �����̷� ���̰Ե�
		} else if (cmd.equalsIgnoreCase("Į��")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.USERSTATUS_ATTACK);
			tell(pc);
		} else if (cmd.equalsIgnoreCase("��������")) {
			StartWar(pc, param);
		} else if (cmd.equalsIgnoreCase("��������")) {
			StopWar(pc, param);
		} else if (cmd.equalsIgnoreCase("�Ǹ�")) { 
			countR1(pc, param);
		} else if (cmd.equalsIgnoreCase("����")) { 
			countR2(pc, param);
		} else if (cmd.equalsIgnoreCase("���")) { 
			countR3(pc, param);
		} else if (cmd.equalsIgnoreCase("����") || cmd.equalsIgnoreCase("��������")) { 
			pc.sendPackets(new S_SystemMessage("������ �ɼ��� ������ �����մϴ�."));
		} else if (cmd.equalsIgnoreCase("����") || cmd.equalsIgnoreCase("��������")) { 
			pc.sendPackets(new S_SystemMessage("���� ������ ��� ���� �������� �Խ��ǿ� �ֽ��ϴ�."));
		} /*else if (cmd.equalsIgnoreCase("����������û") || cmd.equalsIgnoreCase("���������ֹ���")) {
           Sealedoff(pc, param);
        }*/ else {
        	S_ChatPacket s_chatpacket = new S_ChatPacket(pc, cmdLine, Opcodes.S_OPCODE_NORMALCHAT, 0);
        	if (!pc.getExcludingList().contains(pc.getName())) {
        		pc.sendPackets(s_chatpacket);
        	}
        	for (L1PcInstance listner : L1World.getInstance().getRecognizePlayer(pc)) {
        		if (!listner.getExcludingList().contains(pc.getName())) {
        			listner.sendPackets(s_chatpacket);
        		}
        	}
        	// ���� ó��
        	L1MonsterInstance mob = null;
        	for (L1Object obj : pc.getNearObjects().getKnownObjects()) {
        		if (obj instanceof L1MonsterInstance) {
        			mob = (L1MonsterInstance) obj;
        			if (mob.getNpcTemplate().is_doppel() && mob.getName().equals(pc.getName())) {
        				Broadcaster.broadcastPacket(mob, new S_NpcChatPacket(mob, cmdLine, 0));
        			}
        		}
        	}
        	eva.writeMessage(6, "�� [" + pc.getName() + "] " + cmdLine);
        }
	}

	/** ��Ź �Ǹ� �Խ��� - ���� ��ɾ� ���� �޼��� ���� **/
	private static void LetterList(L1PcInstance pc, int type, int count) {
		pc.sendPackets(new S_LetterList(pc,type,count));
	}

	public static void WriteLetter22(L1PcInstance pc , String in4, int in2 ,String in5) {
		int nu1 = 949; 
		SimpleDateFormat formatter = new SimpleDateFormat ( "yy/MM/dd", Locale.KOREA );
		Date currentTime = new Date ( );
		String dTime = formatter.format ( currentTime );
		String subject = "��Ź��ǰ�ǸſϷ�";
		String content = "+ " + in2 +" " + in5 +"\n��ǰ�� �ǸŵǾ����ϴ�. \n\n�ݵ�� ����ŸƮ �Ŀ� \n��Ű�� â��(���)����\n�Ƶ����� ȸ���Ͻʽÿ�.\n\n������ 5%�� ������\n�Ƶ����� ���޵Ǿ����ϴ�.";
		String name = "��Ź������";
		L1PcInstance target = L1World.getInstance().getPlayer(in4);
		LetterTable.getInstance().writeLetter(nu1,dTime, name ,  in4 , 0, subject, content);
		if(target !=null && target.getOnlineStatus() !=0){
			LetterList(target,0,20);
			target.sendPackets(new S_SkillSound(target.getId(), 1091));
			target.sendPackets(new S_ServerMessage(428));
			pc.sendPackets(new S_LetterList(pc,0,20));
		}
	}
	private static void countR1(L1PcInstance pc, String count) {
		try {
			int i = 0; 
			try {
				i = Integer.parseInt(count);
			} catch (NumberFormatException e) { }
			if (i <= 0) {
				pc.sendPackets(new S_SystemMessage(".��Ź�Ǹ�  (�Ǹűݾ�)�� �����ּ���."));
				return;
			}
			if (i > 1000000000) {
				pc.sendPackets(new S_SystemMessage("�Ǹűݾ��� 10���̻� �Ǹ��ϽǼ������ϴ�."));
				return;
			}
			if (i >= 1 && i < 100000) {
				pc.sendPackets(new S_SystemMessage("�ּ���Ź�ݾ��� 10���̻� �Դϴ�."));
				return;
			}
			if (pc.getInventory().checkItem(447012)) { // ��Ź�Ǹ� ���� �����۹�ȣ�� ���� �ѿ� �°�
				pc.sendPackets(new S_SystemMessage("��Ź�Ǹ� ������ ���� �ϰ� ������ �Ұ��� �մϴ�."));
			} else {
				pc.sendPackets(new S_SystemMessage("��Ź�Ǹ� ������ ������ϴ�."));
				pc.getInventory().storeItem(447012 , i); // ��Ź�Ǹ� ���� �����۹�ȣ�� ���� �ѿ� �°�
			}
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(".��Ź�Ǹ� (�Ǹ��� �ݾ�) �̶�� �����ֽʽÿ�."));
			pc.sendPackets(new S_SystemMessage("��)  .��Ź�Ǹ� 5000000"));
		}
	}

	private static void countR2(L1PcInstance pc, String count) {
		int i = 0; 
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			i = Integer.parseInt(count);
		} catch (NumberFormatException e) { }
		int in1 =0;
		int in2 =0;
		int in3 =0;
		int in6 =0;
		String in = null;
		String in4 = null;
		String in5 = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT  name,item_id,item_name, item_enchantlvl ,count ,AccountName , UpClick from AuctionB where id Like '" + i + "'");
			rs = pstm.executeQuery();
			while (rs.next()) {
				in = rs.getString("AccountName");
				in1 = rs.getInt("item_id");
				in2 = rs.getInt("item_enchantlvl");
				in3 = rs.getInt("count");
				in4 = rs.getString("name");
				in5 = rs.getString("item_name");
				in6 = rs.getInt("UpClick");
			}
			if (in1 == 0) {
				pc.sendPackets(new S_SystemMessage("�̹��ȸ���ǰ�̰ų� ���Թ�ȣ�� Ʋ���̽��ϴ�."));
				return;
			}
			if(pc.getInventory().checkItem(40308, in3 )){
				pc.getInventory().consumeItem(40308, in3);
				L1ItemInstance item = ItemTable.getInstance().createItem(in1); 
				item.setEnchantLevel(in2);
				item.setAttrEnchantLevel(in6);
				item.setIdentified(true);
				pc.getInventory().storeItem(item);
				AuctionBTable.getInstance().deleteTopic(i);
				int L1D = (int) ((int)in3*0.95);
				try {
					WriteLetter22(pc , in4 ,in2 ,in5);
					PackageWarehouse.present(in, 40308, 0, L1D);
				} catch (Exception e) { e.printStackTrace(); }   
				pc.sendPackets(new S_SystemMessage("������ �Ǿ����ϴ�"));
			} else {
				pc.sendPackets(new S_SystemMessage("���� �ݾ��� �����մϴ�"));
			}
		} catch (SQLException e) { 
			pc.sendPackets(new S_SystemMessage(".��Ź���� (�Խñ۹�ȣ) ��� �����ֽʽÿ�."));
			pc.sendPackets(new S_SystemMessage("��)  .��Ź���� 0017"));
		} finally { SQLUtil.close(rs, pstm, con); }
	}

	private static void countR3(L1PcInstance pc, String count) {
		int i = 0; 
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			i = Integer.parseInt(count);
		} catch (NumberFormatException e) { }
		int in1 =0;
		int in2 =0;
		int in3 =0;
		String in = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT  item_id, item_enchantlvl  ,count ,AccountName ,UpClick from AuctionB where id Like '" + i + "'");
			rs = pstm.executeQuery();
			while (rs.next()) {
				in1 = rs.getInt("item_id");
				in2 = rs.getInt("item_enchantlvl");
				in = rs.getString("AccountName");
				in3 = rs.getInt("UpClick");
			}
			if (pc.getAccountName().equalsIgnoreCase(in)) {
				L1ItemInstance item = ItemTable.getInstance().createItem(in1); 
				item.setEnchantLevel(in2);
				item.setAttrEnchantLevel(in3);
				pc.getInventory().storeItem(item);
				AuctionBTable.getInstance().deleteTopic(i);
				pc.sendPackets(new S_SystemMessage("������� �Ǿ����ϴ�"));
			} else {
				pc.sendPackets(new S_SystemMessage("����Ͻ� ��ǰ�� �ƴմϴ�"));
			}
		} catch (SQLException e) {
			pc.sendPackets(new S_SystemMessage(".��Ź��� (�Խñ۹�ȣ) ��� �����ֽʽÿ�."));
			pc.sendPackets(new S_SystemMessage("��)  .��Ź��� 0035"));
		} finally { SQLUtil.close(rs, pstm, con); }
	}
	/** ��Ź �Ǹ� �Խ��� - ���� ��ɾ� ���� �޼��� �� **/

	/**  ���� ��ɾ� �̵� ���� **/
	public void movement(L1PcInstance pc, String cmdName, String arg) {
		try {
			int i = 0;
			try { 
				i = Integer.parseInt(arg);  
			} catch ( NumberFormatException e) {
				pc.sendPackets(new S_SystemMessage(".�̵� [����]�� �Է� ���ּ���."));
				pc.sendPackets(new S_SystemMessage("[0~8.�ð��� �տ�],[9.���̿�],[10.���5��],[11.���7��],[12.�Ƶ� �ٹ�Ʈ],[13.�丮��],[14.�������],[15.������Ѵ���]"));
				return;
			}
			if (StatusPc(pc)){ return; }
			pc.getInventory().consumeItem(40308, 5000);
			int[] loc = null;
			switch(i){
			case 0: loc = new int[] { 32728, 32704, 4 }; break;//�ð��� �տ� 0 �� ��ġ
			case 1: loc = new int[] { 32827, 32658, 4 }; break;//�ð��� �տ� 1 �� ��ġ
			case 2: loc = new int[] { 32852, 32713, 4 }; break;//�ð��� �տ� 2 �� ��ġ
			case 3: loc = new int[] { 32914, 33427, 4 }; break;//�ð��� �տ� 3 �� ��ġ
			case 4: loc = new int[] { 32962, 33251, 4 }; break;//�ð��� �տ� 4 �� ��ġ
			case 5: loc = new int[] { 32908, 33169, 4 }; break;//�ð��� �տ� 5 �� ��ġ
			case 6: loc = new int[] { 34272, 33361, 4 }; break;//�ð��� �տ�6 �� ��ġ
			case 7: loc = new int[] { 34258, 33202, 4 }; break;//�ð��� �տ� 7 �� ��ġ
			case 8: loc = new int[] { 34225, 33313, 4 }; break; //�ð��� �տ� 8 �� ��ġ
			case 9: loc = new int[] { 34123, 33148, 4 }; break;// ���̿�
			case 10: loc = new int[] { 33629, 32760, 4 }; break;//����� 5��
			case 11: loc = new int[] { 33552, 32682, 4 }; break;//�����7��
			case 12: loc = new int[] { 33975, 33168, 4 }; break;// �Ƶ��� �ٹ�Ʈ
			case 13: loc = new int[] { 32607, 32714, 4 }; break;// �۷��� �丮 ����
			case 14: loc = new int[] { 32765, 32862, 66 }; break;// ������� �Ա�
			case 15: loc = new int[] { 33499, 32766, 4 }; break;// ���Ѵ��� �Ա�
			case 16: loc = new int[] { 32611, 32771, 4 }; break;// ������
			default:
				pc.sendPackets(new S_SystemMessage(".�̵� [����]�� �Է� ���ּ���."));
				pc.sendPackets(new S_SystemMessage("[0~8.�ð��� �տ�],[9.���̿�],[10.���5��],[11.���7��],[12.�Ƶ� �ٹ�Ʈ],[13.�丮��],[14.�������],[15.������Ѵ���],[16.������]"));					
				break;
			}
			L1Teleport.teleport(pc, loc[0], loc[1], (short) loc[2], 2, true);
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(".�̵� [����]�� �Է� ���ּ���."));
			pc.sendPackets(new S_SystemMessage("[0~8.�ð��� �տ�],[9.���̿�],[10.���5��],[11.���7��],[12.�Ƶ� �ٹ�Ʈ],[13.�丮��],[14.�������],[15.������Ѵ���]"));	
		}
	}


	private void StartWar(L1PcInstance pc, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String clan_name1 = tok.nextToken();
			String clan_name2 = tok.nextToken();

			L1Clan clan1 = L1World.getInstance().getClan(clan_name1);
			L1Clan clan2 = L1World.getInstance().getClan(clan_name2);
			if (clan1 == null ) {
				pc.sendPackets(new S_SystemMessage(clan_name1 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			if (clan2 == null ) {
				pc.sendPackets(new S_SystemMessage(clan_name2 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]���Ͱ� [" + clan_name2 + "]������ ���� ���� �� �Դϴ�."));
					return;
				}
			}
			L1War war = new L1War();
			war.handleCommands(2, clan_name1, clan_name2); // ������ ����
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".�������� �����̸� �����̸�"));
		}
	}
	private void StopWar(L1PcInstance pc, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String clan_name1 = tok.nextToken();
			String clan_name2 = tok.nextToken();

			L1Clan clan1 = L1World.getInstance().getClan(clan_name1);
			L1Clan clan2 = L1World.getInstance().getClan(clan_name2);
			if (clan1 == null ) {
				pc.sendPackets(new S_SystemMessage(clan_name1 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			if (clan2 == null ) {
				pc.sendPackets(new S_SystemMessage(clan_name2 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					war.CeaseWar(clan_name1, clan_name2);
					return;
				}
			}
			pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]���Ͱ� [" + clan_name2 + "]������ ���� ���������� �ʽ��ϴ�."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".�������� �����̸� �����̸�"));
		}
	}
	/** �������� ���Ϳ� ��� ��ȯ -- By. ��� */
	private void CallClan(L1PcInstance pc) { 
		try {
			String clanname = pc.getClanname();
			L1Clan clan = L1World.getInstance().getClan(clanname);
			if (pc.getClanRank() != 4 && !pc.isCrown()){
				pc.sendPackets(new S_ServerMessage(518)); // �� ����� ���� ���ָ��� �̿��� �� �ֽ��ϴ�.
				return;
			}
			int castle_id = L1CastleLocation.getCastleIdByArea(pc);
			if (castle_id != 0) { // ���������� ��� �Ұ�
				pc.sendPackets(new S_ServerMessage(1008)); // �� ���·δ� ������ �� �����ϴ�.
				return;
			}
			int nowtime = Integer.valueOf(CommonUtil.dateFormat("hhmm"));
			if (nowtime >= 2250 && nowtime < 2300){
				pc.sendPackets(new S_ServerMessage(476));  // ���� ������ �ð��� ���� �ʾҽ��ϴ�.
				return; 
			}

			if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_DELAY)) {
				pc.sendPackets(new S_ServerMessage(939)); // ������ ����� �� �����ϴ�
				return;
			}
			if (clan != null) {
				for (L1PcInstance player : clan.getOnlineClanMember()) {
					if (StatusPc(player) || player.isPrivateShop() || player.isFishing() || player == pc || player == null){ 
						continue; 
					} else { // ���� ���̰ų� ���� ����.
						player.sendPackets(new S_Message_YN(729, ""));
						//L1Teleport.teleportToTargetFront(player, pc, 2); // ��ɾ� ������ 2ĭ ������ ��ȯ
					}
				}
				pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_DELAY, 600 * 1000); // by���� ����������
				pc.sendPackets(new S_SystemMessage("[ "+clanname+" ] ������ ��ȯ�Ͽ����ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".���ͼ�ȯ�� �Է�"));
		}
	}

	/////////**************** ������Ƽ ***********////////////
	public static void Party(L1PcInstance pc, String arg) {
		L1PcInstance target = L1World.getInstance(). getPlayer(arg);
		try {
			if (target != null) {
				if (!(pc.getInventory().checkItem(40308, 10000))) {
					pc.sendPackets(new S_SystemMessage("������Ƽ�� 1�� �Ƶ����� �ʿ��մϴ�"));
					return;
				}
				if (arg.equals(pc.getName())) {
					pc.sendPackets(new S_SystemMessage("�ڽſ��Դ� ��Ƽ�� ��û�� �� �����ϴ�."));
					return;
				}
				long curtime = System.currentTimeMillis() / 1000;
				if (pc.getQuizTime() + 20 > curtime) {
					pc.sendPackets(new S_SystemMessage("20�ʰ��� �����ð��� �ʿ��մϴ�."));
					return;
				}
				if (CheckPc(pc, arg)) return;
				if (!target.isParalyzed()) {
					pc.setPartyID(target.getId());
					target.setPartyID(pc.getId());
					target.sendPackets(new S_Message_YN(953, pc.getName()));
					pc.sendPackets(new S_SystemMessage(""+ target.getName() +" �Կ��� ��Ƽ�� ��û�Ͽ����ϴ�."));
				}
			} else {
				pc.sendPackets(new S_SystemMessage("\\fY[�����] .������Ƽ [ĳ���͸�] �� �Է����ּ���"));
			}
		} catch (Exception e) {
		}
	}
	/////////////***********������Ƽ by.����**************///////// <<<�̰� ���ݱ�ȯ ���� `��`;;
	/** ���� ��Ƽ ��û ��ɾ� **/ 
	public void ClanParty (L1PcInstance pc){
		int ClanId = pc.getClanid();
		if (ClanId != 0 && pc.getClanRank() >= 3){ //Clan[O] [����,��ȣ���]
			for (L1PcInstance SearchBlood : L1World.getInstance().getAllPlayers()) {
				if(SearchBlood.getClanid()!= ClanId || SearchBlood.isPrivateShop()
						|| SearchBlood.isInParty()){ // Ŭ���� �����ʴٸ�[X], �̹���Ƽ���̸�[X], ������[X]
					continue; // ����Ż��
				} else if(SearchBlood.getName() != pc.getName()){
					pc.setPartyType(1); // ��ƼŸ�� ����
					SearchBlood.setPartyID(pc.getId()); // ��Ƽ���̵� ����
					SearchBlood.sendPackets(new S_Message_YN(954, pc.getName())); // ������Ƽ ��û
					pc.sendPackets(new S_SystemMessage("����� ["+SearchBlood.getName()+"]���� ��Ƽ�� ��û�߽��ϴ�."));
				}
			}
		} else { // Ŭ���� ���ų� ���� �Ǵ� ��ȣ��� [X]
			pc.sendPackets(new S_SystemMessage("������ ����, ��ȣ��縸 ����Ҽ� �ֽ��ϴ�."));
		}
	}  

	private void infoRanking(L1PcInstance pc){
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		PreparedStatement pstm1 = null;
		ResultSet rs1 = null;
		int allRank = 0;
		int classRank = 0;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM ( SELECT @RNUM:=@RNUM+1 AS ROWNUM , C.Exp,C.char_name,c.objid,c.type  FROM (SELECT @RNUM:=0) R, characters c  WHERE C.AccessLevel = 0  ORDER BY C.Exp DESC ) A  WHERE objid = ?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			if (rs.next()){
				allRank = rs.getInt(1);
			}

			pstm1 = con.prepareStatement("SELECT * FROM ( SELECT @RNUM:=@RNUM+1 AS ROWNUM , C.Exp,C.char_name,c.objid,c.type  FROM (SELECT @RNUM:=0) R, characters c  WHERE C.AccessLevel = 0  and c.type =? ORDER BY C.Exp DESC ) A  WHERE objid = ?");
			pstm1.setInt(1, pc.getType());
			pstm1.setInt(2, pc.getId());
			rs1 = pstm1.executeQuery();

			if (rs1.next()){
				classRank = rs1.getInt(1);
			}


		} catch (SQLException e) {
			System.out.println("��ŷ ��ȸ ����");
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(rs1);
			SQLUtil.close(pstm1);
			SQLUtil.close(con);
		}
		pc.sendPackets(new S_SystemMessage("\\fY[** "+ pc.getName() +"���� ��ŷ ���� **]"));
		pc.sendPackets(new S_SystemMessage(
				"\\fY��ü : " + allRank +
				"�� // Ŭ���� : " + classRank +"��"));
	}
	private void showHelp(L1PcInstance pc) {
		pc.sendPackets(new S_UserCommands(1));
	}


	private void moveToChar(L1PcInstance pc, String pcName) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);
			// ������Ƽ�������� ��� ����
			if (!pc.getMap().isSafetyZone(pc.getLocation())) {   
				pc.sendPackets(new S_ServerMessage(276));  // �� �������� ������ �ڷ���Ʈ�� ����� �� �����ϴ�.
				return;
			}		   
			//�����߿��� ��� �Ұ�
			if (pc.isPinkName()){
				pc.sendPackets(new S_ServerMessage(215));  // ���� �߿� ���� �̵� �� �� �����ϴ�.
				return;
			}
			//���� ������ ���
			if (pc.getClanid() == target.getClanid()){
				pc.sendPackets(new S_SystemMessage("\\fV���� ���Ϳ������� ����� �� �����ϴ�."));
				return;
			}
			// ������״� �� ����
			if(target.isGm()){
				pc.sendPackets(new S_SystemMessage("\\fU��ڴԿ��Դ� �� �� �����ϴ�."));
				return;
			}
			// ������ �ְų� �� ���� ���� ���� ���
			/*
			if(target.getMapId() == 99 
					|| target.getMapId() == 200 
					|| target.getMapId() == 52 
					|| target.getMapId() == 64 
					|| target.getMapId() == 15 
					|| target.getMapId() == 29  
					|| target.getMapId() == 29
					|| target.getMapId() == 110 || target.getMapId() == 120  // oman
					|| target.getMapId() == 130 || target.getMapId() == 140
					|| target.getMapId() == 150 || target.getMapId() == 160
					|| target.getMapId() == 170 || target.getMapId() == 180
					|| target.getMapId() == 190 || target.getMapId() == 200
					|| target.getMapId() == 530 || target.getMapId() == 531  // lastabard 4F
					|| target.getMapId() == 532 || target.getMapId() == 533 
					|| target.getMapId() == 534 || target.getMapId() == 535
					|| target.getMapId() == 603  // �߷Ϲ�
					|| target.getMapId() == 522 || target.getMapId() == 523 || target.getMapId() == 524  // �׸��ڽ���
					|| target.getMapId() == 5167 // �Ǹ����� ����
			){
				pc.sendPackets(new S_SystemMessage("\\fU������ ����η� �� �� ���� ��ҿ� �ֽ��ϴ�."));
				return; 
			}
			 */
			// ������Ƽ���� ������ �� ����.
			if (target.getMap().isSafetyZone(target.getLocation())) {   
				pc.sendPackets(new S_SystemMessage("\\fU������ ������Ƽ ���� �ֱ� ������ ����� �� �����ϴ�."));
				return;
			}

			int castle_id = L1CastleLocation.getCastleIdByArea(pc);
			if (castle_id != 0) { // ���������� ��� �Ұ�
				pc.sendPackets(new S_SystemMessage("\\fY�������������� ��� �� �� �����ϴ�."));
				return;
			}
			/*
			if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_POBYTELL)) {
				pc.sendPackets(new S_SystemMessage("\\fY������ ���Դϴ�. ��� �Ŀ� ����Ͻ� �� �ֽ��ϴ�."));
				return;
			}
			 */
			if (pc.getLevel() >= 60
					& pc.getInventory().checkItem(41159, 100)
					& pc.getInventory().checkItem(5000216, 1)) {
				pc.getInventory().consumeItem(41159, 100);
				pc.getInventory().consumeItem(5000216, 1);
				//pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_POBYTELL, 300 * 1000); // by���� ����������
				L1Teleport.teleport(pc, target.getX(), target.getY(), target.getMapId(), 5, false);
			} else {
				pc.sendPackets(new S_SystemMessage("���)�����[1],Lv60�̻�,����[100] �ʿ��մϴ�"));
			}
			pc.sendPackets(new S_SystemMessage((new StringBuilder())
					.append(pcName)
					.append("�Կ��� ����Ͽ����ϴ�.")
					.toString()));
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(
					"\\fY .����� [���������� ĳ���͸�]�� �Է� ���ּ���."));
		}
	}
	public static void MultiTrade(L1PcInstance pc, String arg) {
		L1PcInstance target = L1World.getInstance(). getPlayer(arg);
		try {
			if (CheckPc(pc, arg)) return;
			if (target != null) {
				if (!target.isParalyzed()) {
					pc.setTradeID(target.getId());
					target.setTradeID(pc.getId());
					target.sendPackets(new S_Message_YN(252, pc.getName()));
					pc.sendPackets(new S_SystemMessage(""+ target.getName() +" �Կ��� ���ݱ�ȯ�� ��û�Ͽ����ϴ�."));
				}
			} else {
				pc.sendPackets(new S_SystemMessage("������ �������� �ƴմϴ�. �ٽ� Ȯ�� �ٶ��ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".���ݱ�ȯ [ĳ���͸�] ���� �Է��� �ּ���."));
		}
	}

	public static boolean StatusPc(L1PcInstance pc) {
		if (!pc.getMap().isEscapable()) {
			pc.sendPackets(new S_ServerMessage(626)); // �� ��ġ������ �� ������ �̵��� �� �����ϴ�.
			return true;
		}
		if (!pc.getInventory().checkItem(40308, 5000)) {
			pc.sendPackets(new S_ServerMessage(189)); // �Ƶ����� ���ġ �ʽ��ϴ�.
			return true;
		}
		if (pc.isPinkName()) {
			pc.sendPackets(new S_ServerMessage(215)); // ���� �߿� �����̵��� �� �����ϴ�.
			return true;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)) {
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) {
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true; 
		}			
		if (pc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)) {
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true; 
		}			
		if (pc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH)) {
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true; 
		}	
		if (pc.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) {
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true;
		}	
		if (pc.getSkillEffectTimerSet().hasSkillEffect(MOB_BASILL)) {
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true;
		}			
		if (pc.getSkillEffectTimerSet().hasSkillEffect(MOB_COCA)) {
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true;
		}
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (castle_id != 0) { // ���������� ��� �Ұ�
			pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
			return true;
		}
		return false;
	}
	private static boolean CheckPc(L1PcInstance pc, String arg) {
		L1PcInstance target = L1World.getInstance(). getPlayer(arg);
		if (pc.isGhost()) return true;
		int castle_id = L1CastleLocation.getCastleIdByArea(pc); 
		if (castle_id != 0)return true;//���Ѹ�������,���������� ���ݱ�ȯ�Ұ� 

		if(pc.getOnlineStatus() == 0) return true;
		if(pc.getOnlineStatus() != 1) return true;
		if (!pc.isGm() && pc.isInvisble()) {
			pc.sendPackets(new S_ServerMessage(334)); 
			return true;
		}
		if(pc.getAccountName().equalsIgnoreCase(target.getAccountName())){
			pc.sendPackets(new S_Disconnect());
			target.sendPackets(new S_Disconnect());
			return true;
		}
		if(pc.getId() == target.getId()) {
			pc.getNetConnection().kick();
			pc.getNetConnection().close();
			target.getNetConnection().kick();
			target.getNetConnection().close();
			return true;
		} else if(pc.getId() != target.getId()) {
			if(pc.getAccountName().equalsIgnoreCase(target.getAccountName())) {
				if(!target.isPrivateShop()) {
					pc.getNetConnection().kick();
					pc.getNetConnection().close();
					target.getNetConnection().kick();
					target.getNetConnection().close();
					return true;
				} 
			}
		}
		return false;
	} 

	private void entertime(L1PcInstance pc) {
		try {
			int entertime1 = 180 - pc.getGdungeonTime() % 1000;
			int entertime2 = 300 - pc.getLdungeonTime() % 1000;
			int entertime3 = 60 -  pc.getIvoryTowerTime() % 1000;

			String time1 = Integer.toString(entertime1);
			String time2 = Integer.toString(entertime2);
			String time3 = Integer.toString(entertime3);   

			pc.sendPackets(new S_ServerMessage(2535, "��� ����", time1)); // 2535 %0 : ���� �ð� %1 �� 
			pc.sendPackets(new S_ServerMessage(2535, "���ž", time3));
			pc.sendPackets(new S_ServerMessage(2535, "��Ÿ�ٵ� ����", time2));
		} catch (Exception e) {
		}
	}
	private void check(L1PcInstance pc) {
		try {
			long curtime = System.currentTimeMillis() / 1000;
			if (pc.getQuizTime() + 20 > curtime ) {
				pc.sendPackets(new S_SystemMessage("20�ʰ��� �����ð��� �ʿ��մϴ�."));
				return;
			}
			int entertime = pc.getGdungeonTime() % 1000;
			int a = 180 - entertime;
			int hpr = pc.getHpr() + pc.getInventory(). hpRegenPerTick();
			int mpr = pc.getMpr() + pc.getInventory(). mpRegenPerTick();	

			pc.sendPackets(new S_SystemMessage("=================( ���� �ɸ��� ���� )==================="));				
			pc.sendPackets(new S_SystemMessage("\\fT(HPr:" + hpr + ')' + "(MPr:" + mpr + ')' +"(�Ⱘ:"+a+"��)(PKȽ��:" + pc.get_PKcount() + ")(����:"+ pc.getAbility().getElixirCount()+ "��)"));
			pc.sendPackets(new S_SystemMessage("===================================================="));
			pc.setQuizTime(curtime);
		} catch (Exception e) {
		}
	}
	private void adenaAdd(L1PcInstance pc) {
		try {
			L1ItemInstance[] items = pc.getInventory().findItemsId(40308);
			int count = 0;

			for (L1ItemInstance item : items) {
				count += pc.getInventory().removeItem(item, item.getCount());
			}
			pc.getInventory().storeItem(40308, count);
			pc.sendPackets(new S_SystemMessage("�� " + count + "�Ƶ����� ���ƽ��ϴ�."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".�Ƶ���ġ�� �� �Է����ּ���."));
		}
	}

	private void age(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String AGE = tok.nextToken();
			int AGEint = Integer.parseInt(AGE);

			if (AGEint > 99) {
				pc.sendPackets(new S_SystemMessage("�Է��Ͻ� ���̴� �ùٸ� ���� �ƴմϴ�."));
				return;
			}

			pc.setAge(AGEint);
			pc.save();
			pc.sendPackets(new S_SystemMessage(pc.getName()+" ���� ���� (" + AGEint
					+ ")�� �����Ǿ����ϴ�."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("  ��� ��) .���� 28"));
		}
	}

	private void username(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String USERNAME = tok.nextToken();

			for (int i = 0;i<USERNAME.length();i++) {  
				if (USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ �� 
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��' || USERNAME.charAt(i) == '��'){
					pc.sendPackets(new S_SystemMessage("����Ҽ����� �̸��Դϴ�."));
					return; 
				}
			}
			if (USERNAME.length() == 0) {
				pc.sendPackets(new S_SystemMessage("������ �̸��� �Է��ϼ���."));
				return;
			}
			if (USERNAME.length() <= 1 || USERNAME.length() >= 5) {
				pc.sendPackets(new S_SystemMessage("�̸��� 2���� �̻� 5���� �̸��� �Է��ϼž��մϴ�."));
				return;
			}
			if (isInvalidName(USERNAME)) {
				pc.sendPackets(new S_SystemMessage("����� �� ���� �̸��Դϴ�."));
				return;
			}
			pc.setUserName(USERNAME);
			pc.save();

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("  ��� ��) .�̸� �̸���"));
		}
	}

	private void charname(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String chaName = tok.nextToken();
			if (pc.getClanid() > 0){
				pc.sendPackets(new S_SystemMessage("\\fU����Ż���� ĳ������ �����Ҽ� �ֽ��ϴ�."));
				return;
			}
			if (!pc.getInventory().checkItem(467009, 1)) { // �ֳ� üũ
				pc.sendPackets(new S_SystemMessage("\\fU�ɸ��� ���� ��ǥ�� �����ϴ�."));
				return; 
			}
			for (int i = 0;i<chaName.length();i++) {  
				if (chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ �� 
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' ||    //�ѹ���(char)������ ��.
						chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��' || chaName.charAt(i) == '��'){
					pc.sendPackets(new S_SystemMessage("����Ҽ����� �ɸ����Դϴ�."));
					return; 
				}
			}
			if (chaName.length() == 0) {
				pc.sendPackets(new S_SystemMessage("������ �ɸ����� �Է��ϼ���."));
				return;
			}
			if (BadNamesList.getInstance().isBadName(chaName)) {
				pc.sendPackets(new S_SystemMessage("����� �� ���� �ɸ����Դϴ�."));
				return;
			}
			if (isInvalidName(chaName)) {
				pc.sendPackets(new S_SystemMessage("����� �� ���� �ɸ����Դϴ�."));
				return;
			}
			if (CharacterTable.doesCharNameExist(chaName)) {
				pc.sendPackets(new S_SystemMessage("������ �ɸ����� �����մϴ�."));
				return;
			}
			pc.getInventory().consumeItem(467009, 1); // �Ҹ�˴ϴ�.
			String oldname = pc.getName();
			chaname(chaName,oldname);
			chanameok(pc);
			L1World.getInstance().broadcastServerMessage("\\fY"+ oldname +"���� "+ chaName + "���� �г����� �����Ͽ����ϴ�.");
		} catch (Exception e){
			pc.sendPackets(new S_SystemMessage("[.�ɸ�����] [�ٲٽǾ��̵�] �Է����ּ���."));
		}
	}
	/** ���� �������� �˻��Ѵ� ����**/
	private static boolean isAlphaNumeric(String s) {
		boolean flag = true;
		char ac[] = s.toCharArray();
		int i = 0;
		do {
			if (i >= ac.length) {
				break;
			}
			if (!Character.isLetterOrDigit(ac[i])) {
				flag = false;
				break;
			}
			i++;
		} while (true);
		return flag;
	}

	private static boolean isInvalidName(String name) {
		int numOfNameBytes = 0;
		try {
			numOfNameBytes = name.getBytes("EUC-KR").length;
		} catch (UnsupportedEncodingException e) {
			//	_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return false;
		}

		if (isAlphaNumeric(name)) {
			return false;
		}
		if (5 < (numOfNameBytes - name.length()) || 12 < numOfNameBytes) {
			return false;
		}

		if (BadNamesList.getInstance().isBadName(name)) {
			return false;
		}
		return true;
	}
	/** ���� �������� �˻��Ѵ� ��**/
	private void chanameok(L1PcInstance pc){
		pc.sendPackets(new S_Disconnect());
	}

	private void chaname(String chaName,String oldname) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE characters SET char_name=? WHERE char_name=?");
			pstm.setString(1, chaName);
			pstm.setString(2, oldname);
			pstm.execute();
		} catch (Exception e) {

		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////
	public static void tell(L1PcInstance pc) { 
		try {
			if (pc.isPinkName())
			{
				pc.sendPackets(new S_SystemMessage("�������̶� ����� �� �����ϴ�."));
				return;
			}
			long curtime = System.currentTimeMillis() / 1000;
			if (pc.getQuizTime() + 15 > curtime ) {
				pc.sendPackets(new S_SystemMessage("15���� �ٽ� ����� �� �ֽ��ϴ�."));
				return;
			}
			L1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getMoveState().getHeading(), false);
			pc.setQuizTime(curtime);
		} catch (Exception exception35) {}
	}



	private void serchdroplist(L1PcInstance gm, String param) {
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String nameid = tok.nextToken();

			int itemid = 0;
			try {
				itemid = Integer.parseInt(nameid);
			} catch (NumberFormatException e) {
				itemid = ItemTable.getInstance().findItemIdByNameWithoutSpace(nameid);
				if (itemid == 0) {
					gm.sendPackets(new S_SystemMessage("�ش� �������� �߰ߵ��� �ʾҽ��ϴ�."));
					return;
				}
			}
			gm.sendPackets(new S_Serchdrop(itemid));
		} catch (Exception e) {
			//   _log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			gm.sendPackets(new S_SystemMessage(".�������Ʈ [�������̸�]�� �Է��� �ּ���."));
			gm.sendPackets(new S_SystemMessage("�������̸��� ������� ��Ȯ�� �Է��ؾ� �մϴ�."));
			gm.sendPackets(new S_SystemMessage("ex) .��� ������(����Ƽ�׷���Ʈ) -- > �˻� O"));
			gm.sendPackets(new S_SystemMessage("ex) .��� �� -- > �˻� X"));
		}
	}

	private void serchdroplist2(L1PcInstance gm, String param) {
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String nameid = tok.nextToken();

			int npcid = 0;
			try {
				npcid = Integer.parseInt(nameid);
			} catch (NumberFormatException e) {
				npcid = NpcTable.getInstance().findNpcIdByName(nameid);
				if (npcid == 0) {
					gm.sendPackets(new S_SystemMessage("�ش� ���Ͱ� �߰ߵ��� �ʾҽ��ϴ�."));
					return;
				}
			}
			gm.sendPackets(new S_Serchdrop2(npcid));
		} catch (Exception e) {
			//   _log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			gm.sendPackets(new S_SystemMessage(".����� [���̸�]�� �Է��� �ּ���."));
			gm.sendPackets(new S_SystemMessage("���� [���̸�]�� ������� ��Ȯ�� �Է��ؾ� �մϴ�."));
			gm.sendPackets(new S_SystemMessage("ex) .����� ����Ŭ�ӽ� -- > �˻� O"));
			gm.sendPackets(new S_SystemMessage("ex) .����� ���� Ŭ�ӽ� -- > �˻� X"));
		}
	}

	private static boolean isDisitAlpha(String str) {  
		boolean check = true;
		for(int i = 0; i < str.length(); i++) {
			if(!Character.isDigit(str.charAt(i)) // ���ڰ� �ƴ϶��
					&& Character.isLetterOrDigit(str.charAt(i)) // Ư�����ڶ��
					//&& !Character.isUpperCase(str.charAt(i)) // �빮�ڰ� �ƴ϶��
					&& Character.isWhitespace(str.charAt(i)) // �����̶��
					&& !Character.isLowerCase(str.charAt(i))) { // �ҹ��ڰ� �ƴ϶��
				check = false;
				break;
			}
		}
		return check;
	}

	private void changePassword(L1PcInstance pc, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String passwd = tok.nextToken();

			Account account = Account.load(pc.getAccountName()); //�߰� 
			if(account.getquize() != null){
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage(".�������� [����������] \\fY������ �ٽ��Է��ϼ���."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			} // ��ȣ����� ��� �����Ǿ� ���� �ʴٸ� �ٲ� �� ������.

			if (passwd.length() < 4) {
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage("�ּ� 4�� �̻� �Է��� �ֽʽÿ�."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			}

			if (passwd.length() > 12) {
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage("�ִ� 10�� ���Ϸ� �Է��� �ֽʽÿ�."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			}

			if (isDisitAlpha(passwd) == false) {
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage("��ȣ�� ������ �ʴ� ���ڰ� ���� �Ǿ� �ֽ��ϴ�."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			}

			to_Change_Passwd(pc, passwd);
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".��ȣ���� [�����Ͻ� ��ȣ]�� �Է��ϼ���."));
		}
	}

	private static String encodePassword(String passwd)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte buf[] = passwd.getBytes("UTF-8");
		buf = MessageDigest.getInstance("SHA").digest(buf);
		return Base64.encodeBytes(buf);
	}

	private void to_Change_Passwd(L1PcInstance pc, String passwd) {
		try {
			String login = null;
			String password = null;
			java.sql.Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			PreparedStatement pstm = null;

			password = encodePassword(passwd);

			statement = con.prepareStatement("select account_name from characters where char_name Like '" + pc.getName() + "'");
			ResultSet rs = statement.executeQuery();

			while (rs.next()){
				login = rs.getString(1);
				pstm = con.prepareStatement("UPDATE accounts SET password=? WHERE login Like '" + login + "'");
				pstm.setString(1, password);
				pstm.execute();
				pc.sendPackets(new S_SystemMessage("\\fY-��������- ������:[" + login + "] ��й�ȣ:[" + passwd + "]"));
				pc.sendPackets(new S_SystemMessage("[" + pc.getName() + "]\\fY���� ��ȣ������ ���������� �Ϸ�Ǿ����ϴ�."));
			}
			rs.close();
			pstm.close();
			statement.close();
			con.close();
		} catch (Exception e) { 	}
	}

	private void quize(L1PcInstance pc, String param) {
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String quize = tok.nextToken();
			Account account = Account.load(pc.getAccountName());

			if (quize.length() < 4) {
				pc.sendPackets(new S_SystemMessage("�Է��Ͻ� ������ �ڸ����� �ʹ� ª���ϴ�."));
				pc.sendPackets(new S_SystemMessage("�ּ� 4�� �̻� �Է��� �ֽʽÿ�."));
				return;
			}

			if (quize.length() > 12) {
				pc.sendPackets(new S_SystemMessage("�Է��Ͻ� ������ �ڸ����� �ʹ� ��ϴ�."));
				pc.sendPackets(new S_SystemMessage("�ִ� 12�� ���Ϸ� �Է��� �ֽʽÿ�."));
				return;
			}
			if (isDisitAlpha(quize) == false) {
				pc.sendPackets(new S_SystemMessage("��� ������ �ʴ� ���ڰ� ���ԵǾ����ϴ�."));
				return;
			}

			if(account.getquize() != null){
				pc.sendPackets(new S_SystemMessage("�̹� ��� �����Ǿ� �ֽ��ϴ�."));
				return;
			}
			account.setquize(quize);
			Account.updateQuize(account);
			pc.sendPackets(new S_SystemMessage("\\fY���� (" + quize + ") �� �����Ǿ����ϴ�."));
			pc.sendPackets(new S_SystemMessage("\\fY�����Ͻ� ��� ������ ���� �����ϽǼ��ֽ��ϴ�."));			 
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".����� [�����Ͻ�����]�� �Է����ּ���."));
		}
	}
	private void quize1(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String quize2 = tok.nextToken();
			Account account = Account.load(pc.getAccountName());

			if (quize2.length() < 4) {
				pc.sendPackets(new S_SystemMessage("�Է��Ͻ� ������ �ڸ����� �ʹ� ª���ϴ�."));
				pc.sendPackets(new S_SystemMessage("�ּ� 4�� �̻� �Է��� �ֽʽÿ�."));
				return;
			}

			if (quize2.length() > 12) {
				pc.sendPackets(new S_SystemMessage("�Է��Ͻ� ������ �ڸ����� �ʹ� ��ϴ�."));
				pc.sendPackets(new S_SystemMessage("�ִ� 12�� ���Ϸ� �Է��� �ֽʽÿ�."));
				return;
			}

			if(account.getquize() == null || account.getquize() == ""){
				pc.sendPackets(new S_SystemMessage("��� �����Ǿ� ���� �ʽ��ϴ�."));
				pc.sendPackets(new S_SystemMessage(".����� [ĳ���͸�] [�����Ͻ�����]�� �Է��ϼ���."));
				return;
			}
			if (!quize2.equals(account.getquize())) {
				pc.sendPackets(new S_SystemMessage("������ ����� ��ġ���� �ʽ��ϴ�."));
				pc.sendPackets(new S_SystemMessage("���� ��� �Ҿ�����̴ٸ� ���ǻ��׿� �÷��ּ���."));
				return;
			}
			if (isDisitAlpha(quize2) == false ) {
				pc.sendPackets(new S_SystemMessage("��� ������ �ʴ� ���ڰ� ���ԵǾ����ϴ�."));
				return;
			}
			account.setquize(null);
			Account.updateQuize(account);
			pc.sendPackets(new S_SystemMessage("\\fY���������Ϸ� �Ǿ����ϴ�."));
			pc.sendPackets(new S_SystemMessage("\\fY���������� �ٽ� ���ο� ��� �����ϼž��մϴ�."));

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".�������� [�����Ͻ�����]�� �Է��ϼ���."));
		}
	}

	private void Hunt(L1PcInstance pc, String cmd) {
		try { 
			StringTokenizer st = new StringTokenizer(cmd);
			String char_name = st.nextToken();
			//int price = Integer.parseInt(st.nextToken());
			//String story = st.nextToken();
			int huntprice = 500000;  // ô�� �⺻ �ݾ�
			L1PcInstance target = null;
			target = L1World.getInstance().getPlayer(char_name);
			if (target != null) {
				if (target.isGm()){ return;}
				/*
				if (char_name.equals(pc.getName())) {
					pc.sendPackets(new S_SystemMessage("�ڽſ��� ô����� ������ �����ϴ�."));
					return;}
				 */
				//if (target.getHuntCount() == 1) {
				//	pc.sendPackets(new S_SystemMessage("�ش� ������ �̹� ô����� ���������ϴ�."));
				//	return;
				//}
				/*
				if (price < huntprice) {
					pc.sendPackets(new S_SystemMessage("�ּ� �ݾ��� 50�� �Ƶ��Դϴ�"));
					return;
				}
				 */
				if (!(pc.getInventory().checkItem(40308, huntprice))) {
					pc.sendPackets(new S_SystemMessage("ô����� �������� 50�� �Ƶ����� �ʿ��մϴ�."));
					return;
				}
				/*
				if (story.length() > 30) {
					pc.sendPackets(new S_SystemMessage("������ ª�� �Է��ϼ���"));
					return;
				}
				 */
				target.setHuntCount(target.getHuntCount() + 1);
				target.setHuntPrice(target.getHuntPrice() + huntprice);
				//target.setReasonToHunt(story);
				target.save();
				//  L1World.getInstance().broadcastPacketToAll(new S_SystemMessage(""+pc.getName()+ ": " + story + "������"));
				L1World.getInstance().broadcastServerMessage("\\fY" + pc.getName() +"���� " + target.getName()+ "�Կ��� " + target.getHuntCount() +"��° ô���� ����!");
				L1World.getInstance().broadcastServerMessage(target.getName()+ "�Կ��� �ɸ� ������� ���� �� " + target.getHuntPrice()+ "�Ƶ����Դϴ�.");
				pc.getInventory().consumeItem(40308, huntprice);
			} else {
				pc.sendPackets(new S_SystemMessage("���������� ���� ĳ�����Դϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".ô�� [ĳ���͸�]"));
			//pc.sendPackets(new S_SystemMessage("ô����� ������ �ɸ��� Ÿ��ġ�� �����մϴ�."));
		}
	}


	public void ment(L1PcInstance pc, String cmd, String param) { 
		if (param.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_MENT, 0);
			pc.sendPackets(new S_SystemMessage("\\fY������� ��Ʈ�� ���ϴ�."));
		} else if (param.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_MENT);
			pc.sendPackets(new S_SystemMessage("\\fY������� ��Ʈ�� �մϴ�."));

		} else {
			pc.sendPackets(new S_SystemMessage(cmd + " [��,��] ��� �Է��� �ּ���. "));
		}
	}

	public void bugracement(L1PcInstance pc, String cmd, String param) { 
		if (param.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.BUGRACEMENT, 0);
			pc.sendPackets(new S_SystemMessage("\\fY���׷��̽� ��Ʈ�� ���ϴ�."));
		} else if (param.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.BUGRACEMENT);
			pc.sendPackets(new S_SystemMessage("\\fY���׷��̽� ��Ʈ�� �մϴ�."));
		} else {
			pc.sendPackets(new S_SystemMessage(cmd + " [��,��] ��� �Է��� �ּ���. "));
		}
	}

	private void buff(L1PcInstance pc){
		if (pc.getLevel() > Config.NEWUSERSAFETY_LEVEL){
			pc.sendPackets(new S_SystemMessage("�ʺ������� " + Config.NEWUSERSAFETY_LEVEL +"�������� ��� �����մϴ�."));
			return;
		}
		int[] allBuffSkill = { PHYSICAL_ENCHANT_DEX, PHYSICAL_ENCHANT_STR, BLESS_WEAPON, ADVANCE_SPIRIT, IRON_SKIN, ADVANCE_SPIRIT};
		L1SkillUse l1skilluse = null;

		l1skilluse = new L1SkillUse();
		for (int i = 0; i < allBuffSkill.length ; i++) {
			l1skilluse.handleCommands(pc, allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
		}
		pc.sendPackets(new S_SystemMessage("�ʺ������� "+ Config.NEWUSERSAFETY_LEVEL + "�������� ��� �����մϴ�."));		
	}

	private void infoitem(L1PcInstance pc, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String charname = tok.nextToken();
			L1PcInstance target = L1World.getInstance().getPlayer(charname);
			if(!pc.isGm() && (charname.compareTo("��Ƽ��")==0)){
				//pc.sendPackets(new S_SystemMessage("��ڴ��� ���縦 �Ҽ� �����ϴ�."));
				return;
			}
			if (target != null){
				pc.sendPackets(new S_SystemMessage("\\fY[** "+ target.getName() +"���� ���� ���� **]"));
				pc.sendPackets(new S_SystemMessage(
						"\\fT+9���� �̻� : " + target.getInventory().getItemEnchantCount(1, 9) +
						"�� // +7�� �̻� : " + target.getInventory().getArmorEnchantCount(7)+"��"));
			} else {
				pc.sendPackets(new S_SystemMessage("���� �÷��� ���� �� �����Ͻ� ["+charname+"] ������ �����ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".���� [ĳ���͸�] ���� �Է��� �ֽñ� �ٶ��ϴ�."));
		}	
	}
}
