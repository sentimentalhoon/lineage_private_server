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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Base64;
import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.SpecialEventHandler;
import l1j.server.server.TimeController.BattleZoneController;
import l1j.server.server.command.L1Commands;
import l1j.server.server.command.executor.L1CommandExecutor;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.HouseTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.ModelSpawnTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_GMCommands;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_Test;
import l1j.server.server.templates.L1Command;
import l1j.server.server.templates.L1House;
import l1j.server.server.utils.IntRange;
import l1j.server.server.utils.L1SpawnUtil;
import l1j.server.server.utils.SQLUtil;
import server.manager.eva;
import server.system.autoshop.AutoShop;
import server.system.autoshop.AutoShopManager;



//Referenced classes of package l1j.server.server:
//ClientThread, Shutdown, IpTable, MobTable,
//PolyTable, IdFactory


public class GMCommands {

	private static Logger _log = Logger.getLogger(GMCommands.class.getName());	
	private static GMCommands _instance;

	private GMCommands() {
	}

	public static GMCommands getInstance() {
		if (_instance == null) {
			_instance = new GMCommands();
		}
		return _instance;
	}

	private String complementClassName(String className) {
		if (className.contains(".")) {
			return className;
		}
		return "l1j.server.server.command.executor." + className;
	}

	private boolean executeDatabaseCommand(L1PcInstance pc, String name,
			String arg) {
		try {
			L1Command command = L1Commands.get(name);
			if (command == null) {
				return false;
			}
			if (pc.getAccessLevel() < command.getLevel()) {
				pc.sendPackets(new S_ServerMessage(74, "[Command] Ŀ��� " + name)); // \f1%0�� ����� �� �����ϴ�.
				return true;
			}

			Class<?> cls = Class.forName(complementClassName(command. getExecutorClassName()));
			L1CommandExecutor exe = (L1CommandExecutor)cls.getMethod("getInstance").invoke(null);
			exe.execute(pc, name, arg);
			eva.writeMessage(-8, '['+ pc.getName() + "] " + name + " " + arg);
			//	_log.info('('+ pc.getName() + ")�� " + name + " " + arg + "��� ��ɾ ����߽��ϴ�. ");
			return true;
		} catch (Exception e) {
			//	_log.log(Level.SEVERE, "error gm command", e);
		}
		return false;
	}	
	public void handleCommands(L1PcInstance gm, String cmdLine) {
		StringTokenizer token = new StringTokenizer(cmdLine);
		// ������ ��������� Ŀ�ǵ�, �� ���Ĵ� ������ �ܶ����� �� �Ķ���ͷμ� ����Ѵ�
		String cmd = token.nextToken();
		String param = "";
		while (token.hasMoreTokens()) {
			param = new StringBuilder(param).append(token.nextToken()).append(
					' ').toString();
		}
		param = param.trim();

		// ����Ÿ���̽�ȭ �� Ŀ���
		if (executeDatabaseCommand(gm, cmd, param)) {
			if (!cmd.equalsIgnoreCase("�����")) {
				_lastCommands.put(gm.getId(), cmdLine);
			}
			return;
		}

		if (gm.getAccessLevel() < 200) {
			gm.sendPackets(new S_ServerMessage(74, "[Command] Ŀ�ǵ� " + cmd));
			return;
		}
		eva.writeMessage(-8, "["+ gm.getName() + "] " + cmd + " " + param);
		// GM�� �����ϴ� Ŀ�ǵ�� ���⿡ ����
		if (cmd.equalsIgnoreCase("����")) {
			showHelp(gm);	
			// ���λ��� ����
		} else if (cmd.equalsIgnoreCase("��������")) {
			if(Config.autoTrader){
				Config.autoTrader = false;
				// �ʱ�ȭ
				Connection c = null;
				PreparedStatement p = null;
				PreparedStatement pp = null;
				try{
					c = L1DatabaseFactory.getInstance().getConnection();
					p = c.prepareStatement("delete from character_traderitems");
					p.execute();
					pp = c.prepareStatement("update characters set trader=0, ment1=?, ment2=?");
					pp.setString(1, null);
					pp.setString(2, null);
					pp.execute();
				}catch(Exception e){
				}finally{
					SQLUtil.close(pp);
					SQLUtil.close(p);
					SQLUtil.close(c);					
				}
				//
			}else{
				Config.autoTrader = true;
			}
			gm.sendPackets(new S_SystemMessage("�������� ���� : " + Config.autoTrader));
		} else if (cmd.equalsIgnoreCase("��Ŷ�ڽ�")) {
			packetbox(gm, param);
		} else if (cmd.equalsIgnoreCase("���ӱ��")) {
			if(gm.getGmTest() == 0){
				gm.setGmTest(1);
				gm.sendPackets(new S_SystemMessage("���ӱ�� �����մϴ�."));
			}else{
				gm.setGmTest(0);
				gm.sendPackets(new S_SystemMessage("���ӱ�� �����մϴ�.."));
			}
		} else if (cmd.equalsIgnoreCase("��������")) {
			startsWith(gm, param);		
		} else if (cmd.equalsIgnoreCase("����ä��")) {
			if(Config.isGmchat){
				Config.isGmchat = false;
				gm.sendPackets(new S_SystemMessage("����ä�� OFF"));
			}else{
				Config.isGmchat= true;
				gm.sendPackets(new S_SystemMessage("����ä�� ON"));
			}
		} else if (cmd.equalsIgnoreCase("��ü����")) {
			allpresent(gm, param);
		} else if (cmd.equalsIgnoreCase("��ü��ȯ")) {
			allrecall(gm); 	
		} else if (cmd.equalsIgnoreCase("�޼���")) {
			servermsg(gm, param);
		} else if (cmd.equalsIgnoreCase("����")) { 
			invmsg(gm, param);
		} else if (cmd.equalsIgnoreCase("����")){				
			SpecialEventHandler.getInstance().doBugRace();
		} else if (cmd.equalsIgnoreCase("��ü����")){				
			SpecialEventHandler.getInstance().doAllBuf();
		} else if (cmd.equalsIgnoreCase("��������")){				
			allGoodBuff();
		} else if (cmd.equalsIgnoreCase("ä��Ǯ��")) {
			chatx(gm, param);
		} else if (cmd.equalsIgnoreCase("���λ���")){
			autoshop(gm, param);
		} else if (cmd.equalsIgnoreCase("����")) { 
			prison(gm, param);
		} else if (cmd.equalsIgnoreCase("�������")){
			changepassword(gm, param);
		} else if (cmd.equalsIgnoreCase("�ڵ�")) {
			CodeTest(gm, param);
		} else if (cmd.equalsIgnoreCase("����")) {
			Clear(gm);
		} else if (cmd.equalsIgnoreCase("����")) {
			levelup2(gm, param);
		} else if (cmd.equalsIgnoreCase("��")) {
			spawnmodel(gm, param);
		} else if (cmd.equalsIgnoreCase("���ݰŷ�")) {
			UserCommands.MultiTrade(gm, param);
		} else if (cmd.equalsIgnoreCase("��Ʋ��")) {
			BattleZone(gm, param); 
		} else if (cmd.equalsIgnoreCase("�ҷ���")){
			HalloweenController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("�ҷ����̺�Ʈ�� �����մϴ�."));
		} else if (cmd.equalsIgnoreCase("�����߰�")) {
			addaccount(gm, param);
		} else if (cmd.equalsIgnoreCase("����")) {
			nolza(gm);
		} else if (cmd.equalsIgnoreCase("�׺�")) {
			Gamble(gm, param);
			///////////////////////////////////////////////////////////////////////////////////
		} else if (cmd.equalsIgnoreCase("Į")) {
			gm.getSkillEffectTimerSet().setSkillEffect(L1SkillId.USERSTATUS_ATTACK, 0);
			UserCommands.tell(gm);//�ڷ�Ǯ�⵵ �ѹ� ���ָ� �ֺ������� �ٷ� �����̷� ���̰Ե�
		} else if (cmd.equalsIgnoreCase("Į��")) {
			gm.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.USERSTATUS_ATTACK);
			UserCommands.tell(gm);
			////////////////////////////////////////////////////////////////////////////////
		} else if (cmd.equalsIgnoreCase(".") || cmd.equalsIgnoreCase("�ڷ�Ǯ��")) {
			UserCommands.tell(gm);		
		} else if (cmd.equalsIgnoreCase("�Ǹ���")){
			DevilController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("�Ǹ����� ���並 �����մϴ�."));
		} else if (cmd.equalsIgnoreCase("����")){
			OxTimeController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("��� �����մϴ�."));
		} else if (cmd.equalsIgnoreCase("�����̺�Ʈ")){
			BIRTHDAYController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("�����̺�Ʈ�� �����մϴ�."));
		} else if (cmd.equalsIgnoreCase("�ϴü�")){
			SkyCastleController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("�ϴü��������� �����մϴ�."));	
			/*
		} else if (cmd.equalsIgnoreCase("�տ�")){
			TimeCrockController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("�ð��� �տ��� ���ڽ��ϴ�.."));*/	
		} else if(cmd.equalsIgnoreCase("��������")){
			serversave(gm);
		} else if(cmd.equalsIgnoreCase("ĳ��������")){
			account_Cha2(gm, param);
		} else if (cmd.equalsIgnoreCase("����")) {        // �������� 
			GongZi(gm, param);
		} else if (cmd.equalsIgnoreCase("����")) {
			nocall(gm, param);		
		} else if (cmd.equalsIgnoreCase("����")) {
			hellcall(gm, param);			
		} else if (cmd.equalsIgnoreCase("�˻�")) { 
			searchDatabase(gm, param);
		} else if (cmd.startsWith("����")) {
			returnEXP(gm, param);
		} else if (cmd.startsWith("����Ȯ��")){
			SerchClanMember(gm, param);
		} else if(cmd.equalsIgnoreCase("���ͼ�ȯ")) {
			CallClan(gm, param);
		} else if (cmd.equalsIgnoreCase("��������")) {
			StartWar(gm, param);
		} else if (cmd.equalsIgnoreCase("��������")) {
			StopWar(gm, param);
		} else if (cmd.equalsIgnoreCase("����ű")) {
			ShopKick(gm, param);
		} else if (cmd.equals("����Ʈ")) { 
			GiveHouse(gm, param); // ����: ����Ʈ ����
		} else if(cmd.equalsIgnoreCase("�����弱��")){ //�������ɾ��ν� by-Kingdom
			ZombieReady(gm);
		} else if (cmd.equalsIgnoreCase("���´��")) {//(by ��Ʈ����) [0062] ���� ���´�� ���Ĺ��� ���� ��/��
			standBy(gm, param);
		} else if (cmd.equalsIgnoreCase("�����")) {
			if (!_lastCommands.containsKey(gm.getId())) {
				gm.sendPackets(new S_ServerMessage(74, "[Command] Ŀ�ǵ� " + cmd)); // \f1%0�� ����� �� �����ϴ�.
				return;
			}
			redo(gm, param);			
			return;
		} else {
			gm.sendPackets(new S_SystemMessage("[Command] Ŀ��� " + cmd + " �� �������� �ʽ��ϴ�. "));
		}
	}

	private void spawnmodel(L1PcInstance gm, String param) {
		StringTokenizer st = new StringTokenizer(param);
		int type = Integer.parseInt(st.nextToken(), 10);		
		ModelSpawnTable.getInstance().insertmodel(gm, type);
		gm.sendPackets(new S_SystemMessage("[Command] �� �־���"));
	}

	private static Map<Integer, String> _lastCommands = new HashMap<Integer, String>();

	private void redo(L1PcInstance pc, String arg) {
		try {
			String lastCmd = _lastCommands.get(pc.getId());
			if (arg.isEmpty()) {
				pc.sendPackets(new S_SystemMessage("[Command] Ŀ�ǵ� " + lastCmd
						+ " ��(��) ������մϴ�."));
				handleCommands(pc, lastCmd);
			} else {				
				StringTokenizer token = new StringTokenizer(lastCmd);
				String cmd = token.nextToken() + " " + arg;
				pc.sendPackets(new S_SystemMessage("[Command] Ŀ�ǵ� " + cmd + " ��(��) ������մϴ�."));
				handleCommands(pc, cmd);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			pc.sendPackets(new S_SystemMessage("[Command] .����� Ŀ�ǵ忡��"));
		}
	}


	private void account_Cha2(L1PcInstance gm, String param) {
		try {
			String s_account = null;
			String s_name = param;
			String s_level = null;
			String s_clan = null;
			String s_bonus = null;
			String s_online = null;
			String s_hp = null;
			String s_mp = null;
			String s_type = null;//�߰�
			int count = 0;
			int count0 = 0;
			java.sql.Connection con0 = null; // �̸����� objid�� �˻��ϱ� ����
			con0 = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement0 = null;
			statement0 = con0
					.prepareStatement("select account_name, Clanname  from characters where char_name = '"
							+ s_name + "'");
			ResultSet rs0 = statement0.executeQuery() ;
			while (rs0.next()) {
				s_account = rs0.getString(1);
				s_clan = rs0.getString(2);
				gm.sendPackets(new S_SystemMessage("ĳ����:" + s_name + "  ����:"
						+ s_account + "  Ŭ����:" + s_clan));//+"  Ŭ����:" + s_type
				count0++;
			}
			java.sql.Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			statement = con.prepareStatement("select " + "char_name,"
					+ "level," + "Clanname," + "BonusStatus," + "OnlineStatus,"
					+ "MaxHp," + "MaxMp, " + "Type "+" from characters where account_name = '" + s_account
					+ "'");
			gm.sendPackets(new S_SystemMessage("***************** ���� ĳ���� *****************"));
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				s_name = rs.getString(1);
				s_level = rs.getString(2);
				s_clan = rs.getString(3);
				s_bonus = rs.getString(4);
				s_online = rs.getString(5);
				s_hp = rs.getString(6);
				s_mp = rs.getString(7);
				s_type = rs.getString(8);
				gm.sendPackets(new S_SystemMessage("����:[" + s_online + "] ��:(" + s_level + ") [" + s_name+"]  Ŭ����=[" + s_type+"]"));
				count++;
			}
			rs0.close();
			statement0.close();
			con0.close();
			rs.close();
			statement.close();
			con.close();
			gm.sendPackets(new S_SystemMessage("\\fY0(����)1(���)2(����)3(����)4(�ٿ�)5(����)6(ȯ��)"));
			gm.sendPackets(new S_SystemMessage("*** ����ĳ����:(" + count + ")��  [1:������/0:��������] ***"));
		} catch (Exception e) { 
			gm.sendPackets(new S_SystemMessage(".ĳ�������� ĳ����"));
		}
	}

	/**[0062] ���� ���� ��� ���Ĺ��� �� �� �޼ҵ�*/
	private void standBy(L1PcInstance gm, String param){
		try{
			StringTokenizer st = new StringTokenizer(param);
			String status = st.nextToken();
			if (status.equalsIgnoreCase("��")) {
				Config.STANDBY_SERVER = true;
				gm.sendPackets(new S_SystemMessage("���´�� ���·� �����մϴ�. �Ϻ� ��Ŷ�� ���� �˴ϴ�."));
			} else if(status.equalsIgnoreCase("��")) {
				Config.STANDBY_SERVER = false;
				gm.sendPackets(new S_SystemMessage("���´�� ���°� �����ǰ� �������� �÷��̰� �����մϴ�."));
			}
		}catch (Exception eee){
			gm.sendPackets(new S_SystemMessage(".���´�� [��/��] ���� �Է��ϼ���."));
			gm.sendPackets(new S_SystemMessage("�� - ���´�� ���·� ��ȯ | �� - �Ϲݸ��� ���ӽ���"));
		}
	}

	public void nolza (L1PcInstance gm){
		try{
			int[] npcId = { 4500175, 4500176, 4500177, 4500178, 4500179, 4500180, 4500181, 778812, 450001876 };
			for (int i = 0; i < npcId.length; i++) {
				int x = gm.getX() + 1;
				int y = gm.getY() + (i * 2);	
				int map = gm.getMapId();
				L1SpawnUtil.spawn4(x, y, (short) map, 6, npcId[i], 1, false, 0);
				gm.sendPackets(new S_SystemMessage(npcId[i] + " / " + x + " / " + y + " / " + "�� ��ġ�Ͽ����ϴ�."));
				Thread.sleep(100);
			}
			gm.sendPackets(new S_SystemMessage("���� NPC�� ��ġ �Ϸ��Ͽ����ϴ�."));
		}catch (Exception e){
			System.out.println(e);
		}
	}

	public void Gamble (L1PcInstance gm, String param){
		try{
			int[] npcId = { 7000073, 7000078 }; // �ֻ��� ���� , ����� ����
			int[] npcId2 = { 7000074, 7000075, 7000075 }; // �Ҹ� 1
			int[] npcId3 = { 7000076, 7000077, 7000077, 7000077, 7000077 }; // �Ҹ� 2
			int type =  Integer.parseInt(param);
			switch(type){
			case 1:
				for (int i = 0; i < npcId.length; i++) {
					int x = gm.getX() - (i * 1);
					int y = gm.getY() + 1;	
					int map = gm.getMapId();
					L1SpawnUtil.spawn4(x, y, (short) map, 4, npcId[i], 1, false, 0);
					gm.sendPackets(new S_SystemMessage(npcId[i] + " / " + x + " / " + y + " / " + "�� ��ġ�Ͽ����ϴ�."));
					Thread.sleep(100);
				}
				break;
			case 2:
				for (int i = 0; i < npcId2.length; i++) {
					int x = gm.getX() - (i * 1);
					int y = gm.getY() + 1;	
					int map = gm.getMapId();
					L1SpawnUtil.spawn4(x, y, (short) map, 4, npcId2[i], 1, false, 0);
					gm.sendPackets(new S_SystemMessage(npcId2[i] + " / " + x + " / " + y + " / " + "�� ��ġ�Ͽ����ϴ�."));
					Thread.sleep(100);
				}
				break;
			case 3:
				for (int i = 0; i < npcId3.length; i++) {
					int x = gm.getX() - (i * 1);
					int y = gm.getY() + 1;	
					int map = gm.getMapId();
					L1SpawnUtil.spawn4(x, y, (short) map, 4, npcId3[i], 1, false, 0);
					gm.sendPackets(new S_SystemMessage(npcId3[i] + " / " + x + " / " + y + " / " + "�� ��ġ�Ͽ����ϴ�."));
					Thread.sleep(100);
				}
				break;
			}
			gm.sendPackets(new S_SystemMessage("���� NPC�� ��ġ �Ϸ��Ͽ����ϴ�."));
		} catch (Exception e){
			gm.sendPackets(new S_SystemMessage("1 : �ֻ��� & ����� , 2 : �Ҹ� 1 , 3 : �Ҹ� 2"));
		}
	}

	private void ShopKick(L1PcInstance gm, String param){
		try {
			StringTokenizer stringtokenizer = new StringTokenizer(param);
			String s = stringtokenizer.nextToken();
			AutoShopManager shopManager = AutoShopManager.getInstance(); 
			AutoShop shopPlayer = shopManager.getShopPlayer(s);
			if (shopPlayer != null){
				shopPlayer.logout();
				shopManager.remove(shopPlayer);
				shopPlayer = null;
			} else {
				gm.sendPackets(new S_SystemMessage("���λ��� ���� �ɸ��� �ƴմϴ�."));
			}
			stringtokenizer = null;
			s = null;
		} catch (Exception exception21) {
			gm.sendPackets(new S_SystemMessage(".����ű [����ĳ���͸�]�� �Է� ���ּ���."));
		}
	}
	/////////////////////////////////////////������ ���۸޼ҵ� by-Kingdom
	private void ZombieReady(L1PcInstance gm){
		try{
			String chat = "�����尡 ���۵˴ϴ�. ������ ���Ͻôº��� Yes�� �����ּ���";
			L1World.getInstance().broadcastPacketToAll(new S_Message_YN(391, chat));
		} catch (Exception e){
			gm.sendPackets(new S_SystemMessage(".�����弱�� Ŀ�ǵ� ����"));
		}
	}
	/////////////////////////////////////////������ ���۸޼ҵ� by-Kingdom	
	/** ����: ����Ʈ */
	private void GiveHouse(L1PcInstance pc, String poby) {
		try {
			StringTokenizer st = new StringTokenizer(poby);
			String pobyname = st.nextToken();
			int pobyhouseid = Integer.parseInt(st.nextToken());
			L1PcInstance target = L1World.getInstance().getPlayer(pobyname);
			if (target != null) {
				if (target.getClanid() != 0) {
					L1Clan TargetClan = L1World.getInstance().getClan(target.getClanname());
					L1House pobyhouse = HouseTable.getInstance().getHouseTable(pobyhouseid);
					TargetClan.setHouseId(pobyhouseid);
					ClanTable.getInstance().updateClan(TargetClan);
					pc.sendPackets(new S_SystemMessage(target.getClanname()+" ���Ϳ��� "+pobyhouse.getHouseName()+"���� �����Ͽ����ϴ�."));
					for (L1PcInstance tc : TargetClan.getOnlineClanMember()) {
						tc.sendPackets(new S_SystemMessage("���Ӹ����ͷκ��� "+pobyhouse.getHouseName()+"���� ���� �޾ҽ��ϴ�."));
					}
				} else {
					pc.sendPackets(new S_SystemMessage(target.getName()+"���� ���Ϳ� ���� ���� �ʽ��ϴ�."));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(73, pobyname));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("\\fY.����Ʈ <���������Ϳ�> <����Ʈ��ȣ>"));
		}
	}

	private void StartWar(L1PcInstance gm, String param)
	{
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String clan_name1 = tok.nextToken();
			String clan_name2 = tok.nextToken();

			L1Clan clan1 = L1World.getInstance().getClan(clan_name1);
			L1Clan clan2 = L1World.getInstance().getClan(clan_name2);
			if (clan1 == null ) {
				gm.sendPackets(new S_SystemMessage(clan_name1 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			if (clan2 == null ) {
				gm.sendPackets(new S_SystemMessage(clan_name2 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					gm.sendPackets(new S_SystemMessage("[" + clan_name1 + "]���Ͱ� [" + clan_name2 + "]������ ���� ���� �� �Դϴ�."));
					return;
				}
			}
			L1War war = new L1War();
			war.handleCommands(2, clan_name1, clan_name2); // ������ ����
			for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
				pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]���Ͱ� [" + clan_name2 + "]������ ������ ���� �Ǿ����ϴ�."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".�������� �����̸� �����̸�"));
		}
	}

	public void allGoodBuff(){
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if(pc.isPrivateShop() // ���� �����̶��
					|| pc.noPlayerCK // ����̳� �׿� ����� �༮��
					|| pc.isDead()){ // �׾��ִٸ�
				continue;
			} else { // �׿ܿ� �������� �༮�鿡�� ����� �޼����� ������.
				pc.sendPackets(new S_Message_YN(456, "")); // ����� ������ �����ϰ� ���������� �����ϴ�.
			}
		}
	}
	
	private void StopWar(L1PcInstance gm, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String clan_name1 = tok.nextToken();
			String clan_name2 = tok.nextToken();

			L1Clan clan1 = L1World.getInstance().getClan(clan_name1);
			L1Clan clan2 = L1World.getInstance().getClan(clan_name2);
			if (clan1 == null ) {
				gm.sendPackets(new S_SystemMessage(clan_name1 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			if (clan2 == null ) {
				gm.sendPackets(new S_SystemMessage(clan_name2 + "������ �������� �ʽ��ϴ�."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					war.CeaseWar(clan_name1, clan_name2);
					for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
						pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]���Ͱ� [" + clan_name2 + "]������ ������ ���� �Ǿ����ϴ�."));
					}
					return;
				}
			}
			gm.sendPackets(new S_SystemMessage("[" + clan_name1 + "]���Ͱ� [" + clan_name2 + "]������ ���� ���������� �ʽ��ϴ�."));
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".�������� �����̸� �����̸�"));
		}
	}
	/** �������� ���Ϳ� ��� ��ȯ **/
	private void CallClan(L1PcInstance pc, String param) { 
		try {
			StringTokenizer st = new StringTokenizer(param);
			String clanname = st.nextToken();
			L1Clan clan = L1World.getInstance().getClan(clanname);
			if (clan != null) {
				for (L1PcInstance player : clan.getOnlineClanMember()) {
					if (!player.isPrivateShop() && !player.isFishing()) { // ���� ���̰ų� ���� ����.
						L1Teleport.teleportToTargetFront(player, pc, 2); // ��ɾ� ������ 2ĭ ������ ��ȯ
					}
				}
				pc.sendPackets(new S_SystemMessage("[ "+clanname+" ] ������ ��ȯ�Ͽ����ϴ�."));
			} else {
				pc.sendPackets(new S_SystemMessage("[ "+clanname+" ] ������ �������� �ʽ��ϴ�."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".���ͼ�ȯ [�����̸�] ������ �Է�"));
		}
	}

	private void returnEXP(L1PcInstance gm, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);
			if (target != null) {
				int oldLevel = target.getLevel();
				int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
				int exp = 0;
				if (oldLevel >= 1 && oldLevel < 11) {
					exp = 0;
				} else if (oldLevel >= 11 && oldLevel < 45) {
					exp = (int) (needExp * 0.1);
				} else if (oldLevel == 45) {
					exp = (int) (needExp * 0.09);
				} else if (oldLevel == 46) {
					exp = (int) (needExp * 0.08);
				} else if (oldLevel == 47) {
					exp = (int) (needExp * 0.07);
				} else if (oldLevel == 48) {
					exp = (int) (needExp * 0.06);
				} else if (oldLevel >= 49) {
					exp = (int) (needExp * 0.05);
				}
				target.addExp(+exp);
				target.save();
				target.saveInventory();
			} else {
				gm.sendPackets(new S_SystemMessage(
						"�׷��� �̸��� ĳ���ʹ� ���峻���� �������� �ʽ��ϴ�."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".��ġ���� [ĳ���͸�]�� �Է� ���ּ���."));
		}
	}

	private void startsWith(L1PcInstance gm, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			int level = Integer.parseInt(st.nextToken());
			GameServerSetting.getInstance().set_maxLevel(level);
			L1World.getInstance().broadcastPacketToAll(
					new S_PacketBox(S_PacketBox.GREEN_MESSAGE, level
							+ "���������� ������ �����մϴ�."));
		} catch (Exception e) {
		}
	}
	private void SerchClanMember(L1PcInstance gm, String param){
		try{
			StringTokenizer tok = new StringTokenizer(param);
			String type = tok.nextToken();
			L1Clan clan = L1World.getInstance().getClan(type);
			if(clan == null){
				gm.sendPackets(new S_SystemMessage("���� �̸��� ��Ȯ���� �ʰų� �������� �ʽ��ϴ�."));
				return;
			}
			gm.sendPackets(new S_SystemMessage("���� �̸� : "+type+" ���ο� : "+clan.getClanMemberList().size()+"�� �����ο� : "+clan.getOnlineMemberCount()+"��"));
		}catch(Exception e){
			gm.sendPackets(new S_SystemMessage(".����Ȯ�� [�����̸�]"));
		}
	}
	public void levelup2(L1PcInstance gm, String arg) {
		try {
			StringTokenizer tok = new StringTokenizer(arg);
			String user = tok.nextToken();
			L1PcInstance target = L1World.getInstance().getPlayer(user);
			int level = Integer.parseInt(tok.nextToken());
			if (level == target.getLevel()) {
				return;
			}
			if (! IntRange.includes(level, 1, 90)) {
				gm.sendPackets(new S_SystemMessage("1-90�� �������� ������ �ּ���"));
				return;
			}
			target.setExp(ExpTable.getExpByLevel(level));
			gm.sendPackets(new S_SystemMessage(target.getName()+"���� ������ �����!"));
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".���� [�ɸ���] [����] �ִ�90 ��� �Է�"));
		}
	}

	private void BattleZone(L1PcInstance gm, String param) {
		boolean power = false;
		if(!power){
			BattleZoneController.getInstance().start();
			//���� �ʱ�ȭ
			BattleZoneController.getInstance().setBattleOpen(true);
			BattleZoneController.getInstance().setBattleOne(true);
			BattleZoneController.getInstance().setBattleStart(false);
			BattleZoneController.getInstance().setBattling(false);

			BattleZoneController.getInstance().BattleTime();
			gm.sendPackets(new S_SystemMessage("��Ʋ���� �����Ͽ����ϴ�."));
		}else {
			gm.sendPackets(new S_SystemMessage("�̹� ��Ʋ�� �����Ͽ����ϴ�."));
		}
	}	

	private void GongZi(L1PcInstance gm, String param)       // �̺κ� ���� 
	{
		if ( param.equalsIgnoreCase("on") )
		{ 
			gm.sendPackets(new S_SystemMessage("fY����� ��ü������ �ְڽ��ϴ� ��!! "));
			L1World.getInstance().set_worldChatElabled(false);   
		}
		else if( param.equalsIgnoreCase("off") )
		{
			gm.sendPackets(new S_SystemMessage("fY���� ���� �� �־�� ȣȣȣ^^"));
			L1World.getInstance().set_worldChatElabled(true);
		}
		else
		{   
			gm.sendPackets(new S_SystemMessage(".���� on  off  ��� �Է��� �ּ���."));
		}
	}  // �� �κ� ������ �������� 

	private void allrecall(L1PcInstance gm) {
		try {
			for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
				if (!pc.isGm() && !pc.isPrivateShop() && !pc.isFishing()) {
					recallnow(gm, pc);
				}
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".��ü��ȯ Ŀ��� ����"));
		}
	}

	private void recallnow(L1PcInstance gm, L1PcInstance target) {
		try {
			L1Teleport.teleportToTargetFront(target, gm, 2);
			gm.sendPackets(new S_SystemMessage((new StringBuilder()).append(target.getName()).append("���� ��ȯ�߽��ϴ�.").toString()));
			target.sendPackets(new S_SystemMessage("���� �����Ϳ��� ��ȯ�Ǿ����ϴ�."));
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
		}
	} 	


	private void chatx(L1PcInstance gm, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();

			L1PcInstance target = null;
			target = L1World.getInstance().getPlayer(pcName);

			if (target!= null) {
				target.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.STATUS_CHAT_PROHIBITED);
				target.sendPackets(new S_SkillIconGFX(36, 0));
				target.sendPackets(new S_ServerMessage(288));
				gm.sendPackets(new S_SystemMessage("�ش�ĳ���� ä���� ���� �߽��ϴ�."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(
					".ä��Ǯ�� ĳ���͸� �̶�� �Է��� �ּ���."));
		}
	}

	private void packetbox(L1PcInstance pc, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			int id = Integer.parseInt(st.nextToken(), 10);			
			pc.sendPackets(new S_PacketBox(id));
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage("[Command] .��Ŷ�ڽ� [id] �Է�"));
		}
	}
	private void showHelp(L1PcInstance gm) {
		gm.sendPackets(new S_GMCommands(1));
	}
	private void autoshop(L1PcInstance gm, String param){
		if(param.equalsIgnoreCase("��")){
			AutoShopManager.getInstance().isAutoShop(true);
			gm.sendPackets(new S_SystemMessage("[Command] ���λ��� ��"));
		} else if(param.equalsIgnoreCase("��")){
			AutoShopManager.getInstance().isAutoShop(false);
			gm.sendPackets(new S_SystemMessage("[Command] ���λ��� ��"));
		} else {
			gm.sendPackets(new S_SystemMessage("[Command] .���λ��� [�� or ��] �Է�"));
		}
	}
	private void searchDatabase(L1PcInstance gm, String param) {
		try {
			StringTokenizer tok = new StringTokenizer(param);
			int type = Integer.parseInt(tok.nextToken());
			String name = tok.nextToken();
			searchObject(gm, type, name);
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".�˻� [0~4] [name]�� �Է� ���ּ���."));
			gm.sendPackets(new S_SystemMessage("0=etcitem, 1=weapon, 2=armor, 3=npc, 4=polymorphs"));   
			gm.sendPackets(new S_SystemMessage("name�� ��Ȯ�� �𸣰ų� ������� �Ǿ��ִ� ����"));
			gm.sendPackets(new S_SystemMessage("'%'�� ���̳� �ڿ� �ٿ� ���ʽÿ�."));
		}
	}

	private void searchObject(L1PcInstance gm, int type, String name) {
		try {
			String str1 = null;
			String str2 = null;
			int count = 0;
			java.sql.Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;

			switch (type) {
			case 0: // etcitem
				statement = con.prepareStatement("select item_id, name from etcitem where name Like '" + name + "'");
				break;
			case 1: // weapon
				statement = con.prepareStatement("select item_id, name from weapon where name Like '" + name + "'");
				break;
			case 2: // armor
				statement = con.prepareStatement("select item_id, name from armor where name Like '" + name + "'");
				break;
			case 3: // npc
				statement = con.prepareStatement("select npcid, name from npc where name Like '" + name + "'");
				break;
			case 4: // polymorphs
				statement = con.prepareStatement("select polyid, name from polymorphs where name Like '" + name + "'");
				break;
			default:
				break;
			}
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				str1 = rs.getString(1);
				str2 = rs.getString(2);
				gm.sendPackets(new S_SystemMessage("id : [" + str1 + "], name : [" + str2 + "]"));
				count++;
			}
			rs.close();
			statement.close();
			con.close();
			gm.sendPackets(new S_SystemMessage("�� [" + count + "]���� �����Ͱ� �˻��Ǿ����ϴ�."));
		}
		catch (Exception e) {
		}
	}		


	private void nocall(L1PcInstance gm, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();

			L1PcInstance target = null; // q
			target = L1World.getInstance().getPlayer(pcName);
			if (target != null) { //Ÿ��
				L1Teleport.teleport(target, 33437, 32812, (short) 4, 5, true); /// ���Ե� ���� (������������������)
			} else {
				gm.sendPackets(new S_SystemMessage("���������� �ʴ� ���� ID �Դϴ�."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".���� (�����ɸ��͸�) ���� �Է��� �ּ���."));
		}
	}
	///////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////////

	public void servermsg(L1PcInstance gm, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			int ment = Integer.parseInt(st.nextToken(), 10);
			int count = Integer.parseInt(st.nextToken(), 10);

			for (int i = 0; i <= count; i++ ) {
				gm.sendPackets(new S_ServerMessage(ment + i));
				gm.sendPackets(new S_SystemMessage("\\fT(" + (ment + i) +")���� ��Ʈ�� ���� �����ϴ�"));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".�޼��� [��ȣ] [����(�⺻1��������)]�� �Է� ���ּ���."));
		}
	}	 


	public void invmsg(L1PcInstance gm, String param) { 
		try {
			StringTokenizer st = new StringTokenizer(param);
			int invid = Integer.parseInt(st.nextToken(), 10);
			int count = Integer.parseInt(st.nextToken(), 10);
			L1ItemInstance item = null;

			for (int i = 0; i < count; i++) {
				item = ItemTable.getInstance().createItem(40308);
				item.getItem().setName(String.valueOf("$" + (invid + i)));
				gm.sendPackets(new S_SystemMessage((invid + i)+" : " + "$" + (invid + i)));
			}
		} catch (Exception exception) {
			gm.sendPackets(new S_SystemMessage(".����  [id] [������Ű�� ��]�� �Է��� �ּ���. "));
		}
	}

	private void hellcall(L1PcInstance gm, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();

			L1PcInstance target = null; // q
			target = L1World.getInstance().getPlayer(pcName);
			if (target != null) { //Ÿ��
				L1Teleport.teleport(target, 32693, 32756, (short) 666, 5, true); /// ���Ե� ���� (������������������)
			} else {
				gm.sendPackets(new S_SystemMessage("���������� �ʴ� ���� ID �Դϴ�."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".���� (�����ɸ��͸�) ���� �Է��� �ּ���."));
		}
	}
	private void allpresent(L1PcInstance gm, String param) {
		try {
			StringTokenizer kwang = new StringTokenizer(param);
			int itemid = Integer.parseInt(kwang.nextToken(), 10);
			int enchant = Integer.parseInt(kwang.nextToken(), 10);
			int count = Integer.parseInt(kwang.nextToken(), 10);
			for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
				if(!pc.isPrivateShop()){
					if (pc.isGhost() == false) {
						L1ItemInstance item = ItemTable.getInstance().createItem(itemid);
						item.setCount(count);
						item.setEnchantLevel(enchant);
						if (item != null) {
							if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
								pc.getInventory().storeItem(item);
							}
						}
						pc.sendPackets(new S_SystemMessage("��ڴԲ��� ��ü�������� ������["+ item.getViewName()	+"]�� �־����ϴ�."));
					}
				}
			}
		} catch (Exception exception) {
			gm.sendPackets(new S_SystemMessage(
					".��ü���� �����۹�ȣ ��þƮ�� ������ �Է��� �ּ���."));
		}

	}



	private void serversave (L1PcInstance pc) {//�˻��� �߰� 
		Saveserver();//�������̺� �޼ҵ� ����
		pc.sendPackets(new S_SystemMessage("���������� �Ϸ�Ǿ����ϴ�."));//�������� �˷��ְ�
	}    

	/**��������**/
	private void Saveserver() {
		/**��ü�÷��̾ ȣ��**/
		for(L1PcInstance player : L1World.getInstance().getAllPlayers()){
			try {
				/**�Ǿ��������ְ�**/
				player.save();
				/**�κ��� �����ϰ�**/
				player.saveInventory();
			} catch (Exception ex) {
				/**���� �κ�����**/
				player.saveInventory();
			}
		}
	}
	/**��������**/

	private static String encodePassword(String rawPassword)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte buf[] = rawPassword.getBytes("UTF-8");
		buf = MessageDigest.getInstance("SHA").digest(buf);

		return Base64.encodeBytes(buf);
	}

	private void AddAccount(L1PcInstance gm, String account, String passwd, String Ip, String Host) {
		try {
			String login = null;
			String password = null;
			java.sql.Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			PreparedStatement pstm = null;

			password = encodePassword(passwd);

			statement = con.prepareStatement("select * from accounts where login Like '" + account + "'");
			ResultSet rs = statement.executeQuery();

			if(rs.next())login = rs.getString(1);			
			if (login != null){
				gm.sendPackets(new S_SystemMessage("[Command] �̹� ������ �ֽ��ϴ�."));
				return;
			} else {
				String sqlstr = "INSERT INTO accounts SET login=?,password=?,lastactive=?,access_level=?,ip=?,host=?,banned=?,charslot=?,gamepassword=?,notice=?";
				pstm = con.prepareStatement(sqlstr);
				pstm.setString(1, account);
				pstm.setString(2, password);
				pstm.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				pstm.setInt(4, 0);
				pstm.setString(5, Ip);
				pstm.setString(6, Host);
				pstm.setInt(7, 0);
				pstm.setInt(8, 6);
				pstm.setInt(9, 0);
				pstm.setInt(10, 0);
				pstm.execute();
				gm.sendPackets(new S_SystemMessage("[Command] ���� �߰��� �Ϸ�Ǿ����ϴ�."));				
			}

			rs.close();
			pstm.close();
			statement.close();
			con.close();
		} catch (Exception e) {
		}
	}

	private static boolean isDisitAlpha(String str) {  
		boolean check = true;
		for(int i = 0; i < str.length(); i++) {
			if(!Character.isDigit(str.charAt(i))	// ���ڰ� �ƴ϶��
					&& Character.isLetterOrDigit(str.charAt(i))	// Ư�����ڶ��
					&& !Character.isUpperCase(str.charAt(i))	// �빮�ڰ� �ƴ϶��
					&& !Character.isLowerCase(str.charAt(i))) {	// �ҹ��ڰ� �ƴ϶��
				check = false;
				break;
			}
		}
		return check;
	}

	private void addaccount(L1PcInstance gm, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String user = tok.nextToken();
			String passwd = tok.nextToken();

			if (user.length() < 4) {
				gm.sendPackets(new S_SystemMessage("[Command] �Է��Ͻ� �������� �ڸ����� �ʹ� ª���ϴ�."));
				gm.sendPackets(new S_SystemMessage("[Command] �ּ� 4�� �̻� �Է��� �ֽʽÿ�."));
				return;
			}
			if (passwd.length() < 4) {
				gm.sendPackets(new S_SystemMessage("[Command] �Է��Ͻ� ��ȣ�� �ڸ����� �ʹ� ª���ϴ�."));
				gm.sendPackets(new S_SystemMessage("[Command] �ּ� 4�� �̻� �Է��� �ֽʽÿ�."));
				return;
			}

			if (passwd.length() > 12) {
				gm.sendPackets(new S_SystemMessage("[Command] �Է��Ͻ� ��ȣ�� �ڸ����� �ʹ� ��ϴ�."));
				gm.sendPackets(new S_SystemMessage("[Command] �ִ� 12�� ���Ϸ� �Է��� �ֽʽÿ�."));
				return;
			}

			if (isDisitAlpha(passwd) == false) {
				gm.sendPackets(new S_SystemMessage("[Command] ��ȣ�� ������ �ʴ� ���ڰ� ���� �Ǿ� �ֽ��ϴ�."));
				return;
			}
			AddAccount(gm, user, passwd,"127.0.0.1","127.0.0.1");
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage("[Command] .�����߰� [������] [��ȣ] �Է�"));
		}
	}

	private void changepassword(L1PcInstance gm, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String user = tok.nextToken();
			String oldpasswd = tok.nextToken();
			String newpasswd = tok.nextToken();

			if (user.length() < 4) {
				gm.sendPackets(new S_SystemMessage("[Command] �Է��Ͻ� �������� �ڸ����� �ʹ� ª���ϴ�."));
				gm.sendPackets(new S_SystemMessage("[Command] �ּ� 4�� �̻� �Է��� �ֽʽÿ�."));
				return;
			}
			if (newpasswd.length() < 4) {
				gm.sendPackets(new S_SystemMessage("[Command] �Է��Ͻ� ��ȣ�� �ڸ����� �ʹ� ª���ϴ�."));
				gm.sendPackets(new S_SystemMessage("[Command] �ּ� 4�� �̻� �Է��� �ֽʽÿ�."));
				return;
			}
			if (newpasswd.length() > 12) {
				gm.sendPackets(new S_SystemMessage("[Command] �Է��Ͻ� ��ȣ�� �ڸ����� �ʹ� ��ϴ�."));
				gm.sendPackets(new S_SystemMessage("[Command] �ִ� 12�� ���Ϸ� �Է��� �ֽʽÿ�."));
				return;
			}

			if (isDisitAlpha(newpasswd) == false) {
				gm.sendPackets(new S_SystemMessage("[Command] ��ȣ�� ������ �ʴ� ���ڰ� ���� �Ǿ� �ֽ��ϴ�."));
				return;
			}
			chkpassword(gm, user, oldpasswd, newpasswd);
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage("[Command] .������� [����] [������] [�ٲܺ��] �Է�"));
		}
	}
	private void chkpassword(L1PcInstance gm, String account, String oldpassword, String newpassword) {
		try {					
			String password = null;
			java.sql.Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;			
			PreparedStatement pstm = null;


			statement = con.prepareStatement("select password from accounts where login='" + account + "'");			
			ResultSet rs = statement.executeQuery();


			if(rs.next())password = rs.getString(1);	
			if (password == null){
				gm.sendPackets(new S_SystemMessage("[Command] �Է��Ͻ� ������ �������� ���� ���� �ʽ��ϴ�."));
				return;
			}

			if (!isPasswordTrue(password,oldpassword)){				
				//System.out.println("���� ��� : " + oldpassword+" - üũ ��� : "+password);
				gm.sendPackets(new S_SystemMessage("[Command] ���� �������� ��й�ȣ�� ��ġ���� �ʽ��ϴ�. "));
				gm.sendPackets(new S_SystemMessage("[Command] �ٽ� Ȯ���Ͻð� ������ �ּ���."));
				return;
			} else {
				String sqlstr = "UPDATE accounts SET password=password(?) WHERE login=?";
				pstm = con.prepareStatement(sqlstr);
				pstm.setString(1, newpassword);
				pstm.setString(2, account);
				pstm.execute();
				gm.sendPackets(new S_SystemMessage("[Command] ������ : " + account+" / �ٲ��й�ȣ : " + newpassword));
				gm.sendPackets(new S_SystemMessage("[Command] ��й�ȣ ������ ���������� �Ϸ�Ǿ����ϴ�."));				
			}
			rs.close();
			pstm.close();
			statement.close();
			con.close();
		} catch (Exception e) {
		}
	}
	//�н����� �´��� ���� ����  
	public static boolean isPasswordTrue(String Password,String oldPassword) { 
		String _rtnPwd = null;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT password(?) as pwd ");

			pstm.setString(1, oldPassword);
			rs = pstm.executeQuery();
			if (rs.next()){
				_rtnPwd = rs.getString("pwd");  
			}
			if ( _rtnPwd.equals(Password)) { // �����ϴٸ�
				return true;
			}else
				return false;
		}catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}return false;
	}

	private void CodeTest(L1PcInstance pc, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			int codetest = Integer.parseInt(st.nextToken(), 10);
			//pc.sendPackets(new S_ServerMessage(161,"$580","$245", "$247"));
			//int time = 1020;
			// �� �׽�Ʈ�� �ڵ尡 ������ ���� ��Ŷ �κ� 
			pc.sendPackets(new S_Test(pc,codetest));
			//pc.sendPackets(new S_PacketBox(S_PacketBox.ICON_AURA, codetest, time)); // �׽�,��� ���� �׽�Ʈ
			//pc.sendPackets(new S_CastleMaster(codetest, pc.getId())); // �հ� �׽�Ʈ
			//pc.sendPackets(new S_StatusReset(pc, codetest, 1)); // ���� �ʱ�ȭ �׽�Ʈ
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage("[Command] .�ڵ� [����] �Է�"));
		}
	}

	private void prison(L1PcInstance gm, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();

			L1PcInstance target = null; // q
			target = L1World.getInstance().getPlayer(pcName);

			if (target != null) { // Ÿ��
				L1Teleport.teleport(target, 32736, 32799, (short) 34, 5, true);
			} else {
				gm.sendPackets(new S_SystemMessage("���������� �ʴ� ���� ID �Դϴ�."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".���� (�����ɸ��͸�) ���� �Է��� �ּ���."));
		}
	}
	private void Clear(L1PcInstance gm) {
		for (L1Object obj : L1World.getInstance().getVisibleObjects(gm, 10)) { // 10 ���� ���� ������Ʈ�� ã�Ƽ�
			if (obj instanceof L1MonsterInstance){ // ���Ͷ��
				L1NpcInstance npc = (L1NpcInstance) obj;
				npc.receiveDamage(gm, 50000); // ������
				if (npc.getCurrentHp() <= 0){
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815)); // ��
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}else{
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815));
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}
			}/*else if (obj instanceof L1PcInstance){ // pc���
				L1PcInstance player = (L1PcInstance) obj;
				player.receiveDamage(player, 0, false); // ������
				if (player.getCurrentHp() <= 0){
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815)); // ��
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}else{
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815));
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}
			}*/
		}
	}
}
