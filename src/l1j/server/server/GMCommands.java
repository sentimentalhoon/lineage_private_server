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
				pc.sendPackets(new S_ServerMessage(74, "[Command] 커멘드 " + name)); // \f1%0은 사용할 수 없습니다.
				return true;
			}

			Class<?> cls = Class.forName(complementClassName(command. getExecutorClassName()));
			L1CommandExecutor exe = (L1CommandExecutor)cls.getMethod("getInstance").invoke(null);
			exe.execute(pc, name, arg);
			eva.writeMessage(-8, '['+ pc.getName() + "] " + name + " " + arg);
			//	_log.info('('+ pc.getName() + ")가 " + name + " " + arg + "운영자 명령어를 사용했습니다. ");
			return true;
		} catch (Exception e) {
			//	_log.log(Level.SEVERE, "error gm command", e);
		}
		return false;
	}	
	public void handleCommands(L1PcInstance gm, String cmdLine) {
		StringTokenizer token = new StringTokenizer(cmdLine);
		// 최초의 공백까지가 커맨드, 그 이후는 공백을 단락으로 한 파라미터로서 취급한다
		String cmd = token.nextToken();
		String param = "";
		while (token.hasMoreTokens()) {
			param = new StringBuilder(param).append(token.nextToken()).append(
					' ').toString();
		}
		param = param.trim();

		// 데이타베이스화 된 커멘드
		if (executeDatabaseCommand(gm, cmd, param)) {
			if (!cmd.equalsIgnoreCase("재실행")) {
				_lastCommands.put(gm.getId(), cmdLine);
			}
			return;
		}

		if (gm.getAccessLevel() < 200) {
			gm.sendPackets(new S_ServerMessage(74, "[Command] 커맨드 " + cmd));
			return;
		}
		eva.writeMessage(-8, "["+ gm.getName() + "] " + cmd + " " + param);
		// GM에 개방하는 커맨드는 여기에 쓴다
		if (cmd.equalsIgnoreCase("도움말")) {
			showHelp(gm);	
			// 개인상점 저장
		} else if (cmd.equalsIgnoreCase("상점저장")) {
			if(Config.autoTrader){
				Config.autoTrader = false;
				// 초기화
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
			gm.sendPackets(new S_SystemMessage("상점저장 상태 : " + Config.autoTrader));
		} else if (cmd.equalsIgnoreCase("패킷박스")) {
			packetbox(gm, param);
		} else if (cmd.equalsIgnoreCase("공속기록")) {
			if(gm.getGmTest() == 0){
				gm.setGmTest(1);
				gm.sendPackets(new S_SystemMessage("공속기록 시작합니다."));
			}else{
				gm.setGmTest(0);
				gm.sendPackets(new S_SystemMessage("공속기록 종료합니다.."));
			}
		} else if (cmd.equalsIgnoreCase("레벨제한")) {
			startsWith(gm, param);		
		} else if (cmd.equalsIgnoreCase("영자채팅")) {
			if(Config.isGmchat){
				Config.isGmchat = false;
				gm.sendPackets(new S_SystemMessage("영자채팅 OFF"));
			}else{
				Config.isGmchat= true;
				gm.sendPackets(new S_SystemMessage("영자채팅 ON"));
			}
		} else if (cmd.equalsIgnoreCase("전체선물")) {
			allpresent(gm, param);
		} else if (cmd.equalsIgnoreCase("전체소환")) {
			allrecall(gm); 	
		} else if (cmd.equalsIgnoreCase("메세지")) {
			servermsg(gm, param);
		} else if (cmd.equalsIgnoreCase("네임")) { 
			invmsg(gm, param);
		} else if (cmd.equalsIgnoreCase("버경")){				
			SpecialEventHandler.getInstance().doBugRace();
		} else if (cmd.equalsIgnoreCase("전체버프")){				
			SpecialEventHandler.getInstance().doAllBuf();
		} else if (cmd.equalsIgnoreCase("좋은버프")){				
			allGoodBuff();
		} else if (cmd.equalsIgnoreCase("채금풀기")) {
			chatx(gm, param);
		} else if (cmd.equalsIgnoreCase("무인상점")){
			autoshop(gm, param);
		} else if (cmd.equalsIgnoreCase("감옥")) { 
			prison(gm, param);
		} else if (cmd.equalsIgnoreCase("비번변경")){
			changepassword(gm, param);
		} else if (cmd.equalsIgnoreCase("코드")) {
			CodeTest(gm, param);
		} else if (cmd.equalsIgnoreCase("정리")) {
			Clear(gm);
		} else if (cmd.equalsIgnoreCase("렙업")) {
			levelup2(gm, param);
		} else if (cmd.equalsIgnoreCase("불")) {
			spawnmodel(gm, param);
		} else if (cmd.equalsIgnoreCase("원격거래")) {
			UserCommands.MultiTrade(gm, param);
		} else if (cmd.equalsIgnoreCase("배틀존")) {
			BattleZone(gm, param); 
		} else if (cmd.equalsIgnoreCase("할로윈")){
			HalloweenController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("할로윈이벤트를 시작합니다."));
		} else if (cmd.equalsIgnoreCase("계정추가")) {
			addaccount(gm, param);
		} else if (cmd.equalsIgnoreCase("놀자")) {
			nolza(gm);
		} else if (cmd.equalsIgnoreCase("겜블")) {
			Gamble(gm, param);
			///////////////////////////////////////////////////////////////////////////////////
		} else if (cmd.equalsIgnoreCase("칼")) {
			gm.getSkillEffectTimerSet().setSkillEffect(L1SkillId.USERSTATUS_ATTACK, 0);
			UserCommands.tell(gm);//텔렉풀기도 한번 해주면 주변유저가 바로 보라돌이로 보이게됨
		} else if (cmd.equalsIgnoreCase("칼끔")) {
			gm.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.USERSTATUS_ATTACK);
			UserCommands.tell(gm);
			////////////////////////////////////////////////////////////////////////////////
		} else if (cmd.equalsIgnoreCase(".") || cmd.equalsIgnoreCase("텔렉풀기")) {
			UserCommands.tell(gm);		
		} else if (cmd.equalsIgnoreCase("악마왕")){
			DevilController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("악마왕의 영토를 오픈합니다."));
		} else if (cmd.equalsIgnoreCase("퀴즈")){
			OxTimeController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("퀴즈를 시작합니다."));
		} else if (cmd.equalsIgnoreCase("생일이벤트")){
			BIRTHDAYController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("생일이벤트를 오픈합니다."));
		} else if (cmd.equalsIgnoreCase("하늘성")){
			SkyCastleController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("하늘성공성전을 오픈합니다."));	
			/*
		} else if (cmd.equalsIgnoreCase("균열")){
			TimeCrockController.getInstance().isGmOpen = true;
			gm.sendPackets(new S_SystemMessage("시간의 균열을 열겠습니다.."));*/	
		} else if(cmd.equalsIgnoreCase("서버저장")){
			serversave(gm);
		} else if(cmd.equalsIgnoreCase("캐릭터조사")){
			account_Cha2(gm, param);
		} else if (cmd.equalsIgnoreCase("공지")) {        // 공지사항 
			GongZi(gm, param);
		} else if (cmd.equalsIgnoreCase("가라")) {
			nocall(gm, param);		
		} else if (cmd.equalsIgnoreCase("지옥")) {
			hellcall(gm, param);			
		} else if (cmd.equalsIgnoreCase("검색")) { 
			searchDatabase(gm, param);
		} else if (cmd.startsWith("복구")) {
			returnEXP(gm, param);
		} else if (cmd.startsWith("혈원확인")){
			SerchClanMember(gm, param);
		} else if(cmd.equalsIgnoreCase("혈맹소환")) {
			CallClan(gm, param);
		} else if (cmd.equalsIgnoreCase("혈전시작")) {
			StartWar(gm, param);
		} else if (cmd.equalsIgnoreCase("혈전종료")) {
			StopWar(gm, param);
		} else if (cmd.equalsIgnoreCase("상점킥")) {
			ShopKick(gm, param);
		} else if (cmd.equals("아지트")) { 
			GiveHouse(gm, param); // 포비: 아지트 지급
		} else if(cmd.equalsIgnoreCase("좀비모드선언")){ //좀비모드명령어인식 by-Kingdom
			ZombieReady(gm);
		} else if (cmd.equalsIgnoreCase("오픈대기")) {//(by 마트무사) [0062] 서버 오픈대기 스탠바이 상태 켬/끔
			standBy(gm, param);
		} else if (cmd.equalsIgnoreCase("재실행")) {
			if (!_lastCommands.containsKey(gm.getId())) {
				gm.sendPackets(new S_ServerMessage(74, "[Command] 커맨드 " + cmd)); // \f1%0은 사용할 수 없습니다.
				return;
			}
			redo(gm, param);			
			return;
		} else {
			gm.sendPackets(new S_SystemMessage("[Command] 커멘드 " + cmd + " 는 존재하지 않습니다. "));
		}
	}

	private void spawnmodel(L1PcInstance gm, String param) {
		StringTokenizer st = new StringTokenizer(param);
		int type = Integer.parseInt(st.nextToken(), 10);		
		ModelSpawnTable.getInstance().insertmodel(gm, type);
		gm.sendPackets(new S_SystemMessage("[Command] 불 넣었다"));
	}

	private static Map<Integer, String> _lastCommands = new HashMap<Integer, String>();

	private void redo(L1PcInstance pc, String arg) {
		try {
			String lastCmd = _lastCommands.get(pc.getId());
			if (arg.isEmpty()) {
				pc.sendPackets(new S_SystemMessage("[Command] 커맨드 " + lastCmd
						+ " 을(를) 재실행합니다."));
				handleCommands(pc, lastCmd);
			} else {				
				StringTokenizer token = new StringTokenizer(lastCmd);
				String cmd = token.nextToken() + " " + arg;
				pc.sendPackets(new S_SystemMessage("[Command] 커맨드 " + cmd + " 을(를) 재실행합니다."));
				handleCommands(pc, cmd);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			pc.sendPackets(new S_SystemMessage("[Command] .재실행 커맨드에러"));
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
			String s_type = null;//추가
			int count = 0;
			int count0 = 0;
			java.sql.Connection con0 = null; // 이름으로 objid를 검색하기 위해
			con0 = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement0 = null;
			statement0 = con0
					.prepareStatement("select account_name, Clanname  from characters where char_name = '"
							+ s_name + "'");
			ResultSet rs0 = statement0.executeQuery() ;
			while (rs0.next()) {
				s_account = rs0.getString(1);
				s_clan = rs0.getString(2);
				gm.sendPackets(new S_SystemMessage("캐릭명:" + s_name + "  계정:"
						+ s_account + "  클랜명:" + s_clan));//+"  클래스:" + s_type
				count0++;
			}
			java.sql.Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			statement = con.prepareStatement("select " + "char_name,"
					+ "level," + "Clanname," + "BonusStatus," + "OnlineStatus,"
					+ "MaxHp," + "MaxMp, " + "Type "+" from characters where account_name = '" + s_account
					+ "'");
			gm.sendPackets(new S_SystemMessage("***************** 계정 캐릭터 *****************"));
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
				gm.sendPackets(new S_SystemMessage("접속:[" + s_online + "] 랩:(" + s_level + ") [" + s_name+"]  클래스=[" + s_type+"]"));
				count++;
			}
			rs0.close();
			statement0.close();
			con0.close();
			rs.close();
			statement.close();
			con.close();
			gm.sendPackets(new S_SystemMessage("\\fY0(군주)1(기사)2(요정)3(법사)4(다엘)5(용기사)6(환술)"));
			gm.sendPackets(new S_SystemMessage("*** 계정캐릭터:(" + count + ")개  [1:게임중/0:오프라인] ***"));
		} catch (Exception e) { 
			gm.sendPackets(new S_SystemMessage(".캐릭터조사 캐릭명"));
		}
	}

	/**[0062] 서버 오픈 대기 스탠바이 켬 끔 메소드*/
	private void standBy(L1PcInstance gm, String param){
		try{
			StringTokenizer st = new StringTokenizer(param);
			String status = st.nextToken();
			if (status.equalsIgnoreCase("켬")) {
				Config.STANDBY_SERVER = true;
				gm.sendPackets(new S_SystemMessage("오픈대기 상태로 돌입합니다. 일부 패킷이 차단 됩니다."));
			} else if(status.equalsIgnoreCase("끔")) {
				Config.STANDBY_SERVER = false;
				gm.sendPackets(new S_SystemMessage("오픈대기 상태가 해지되고 정상적인 플레이가 가능합니다."));
			}
		}catch (Exception eee){
			gm.sendPackets(new S_SystemMessage(".오픈대기 [켬/끔] 으로 입력하세요."));
			gm.sendPackets(new S_SystemMessage("켬 - 오픈대기 상태로 전환 | 끔 - 일반모드로 게임시작"));
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
				gm.sendPackets(new S_SystemMessage(npcId[i] + " / " + x + " / " + y + " / " + "를 배치하였습니다."));
				Thread.sleep(100);
			}
			gm.sendPackets(new S_SystemMessage("놀자 NPC를 배치 완료하였습니다."));
		}catch (Exception e){
			System.out.println(e);
		}
	}

	public void Gamble (L1PcInstance gm, String param){
		try{
			int[] npcId = { 7000073, 7000078 }; // 주사위 딜러 , 묵찌빠 딜러
			int[] npcId2 = { 7000074, 7000075, 7000075 }; // 소막 1
			int[] npcId3 = { 7000076, 7000077, 7000077, 7000077, 7000077 }; // 소막 2
			int type =  Integer.parseInt(param);
			switch(type){
			case 1:
				for (int i = 0; i < npcId.length; i++) {
					int x = gm.getX() - (i * 1);
					int y = gm.getY() + 1;	
					int map = gm.getMapId();
					L1SpawnUtil.spawn4(x, y, (short) map, 4, npcId[i], 1, false, 0);
					gm.sendPackets(new S_SystemMessage(npcId[i] + " / " + x + " / " + y + " / " + "를 배치하였습니다."));
					Thread.sleep(100);
				}
				break;
			case 2:
				for (int i = 0; i < npcId2.length; i++) {
					int x = gm.getX() - (i * 1);
					int y = gm.getY() + 1;	
					int map = gm.getMapId();
					L1SpawnUtil.spawn4(x, y, (short) map, 4, npcId2[i], 1, false, 0);
					gm.sendPackets(new S_SystemMessage(npcId2[i] + " / " + x + " / " + y + " / " + "를 배치하였습니다."));
					Thread.sleep(100);
				}
				break;
			case 3:
				for (int i = 0; i < npcId3.length; i++) {
					int x = gm.getX() - (i * 1);
					int y = gm.getY() + 1;	
					int map = gm.getMapId();
					L1SpawnUtil.spawn4(x, y, (short) map, 4, npcId3[i], 1, false, 0);
					gm.sendPackets(new S_SystemMessage(npcId3[i] + " / " + x + " / " + y + " / " + "를 배치하였습니다."));
					Thread.sleep(100);
				}
				break;
			}
			gm.sendPackets(new S_SystemMessage("놀자 NPC를 배치 완료하였습니다."));
		} catch (Exception e){
			gm.sendPackets(new S_SystemMessage("1 : 주사위 & 묵찌빠 , 2 : 소막 1 , 3 : 소막 2"));
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
				gm.sendPackets(new S_SystemMessage("무인상점 가동 케릭이 아닙니다."));
			}
			stringtokenizer = null;
			s = null;
		} catch (Exception exception21) {
			gm.sendPackets(new S_SystemMessage(".상점킥 [상점캐릭터명]을 입력 해주세요."));
		}
	}
	/////////////////////////////////////////좀비모드 시작메소드 by-Kingdom
	private void ZombieReady(L1PcInstance gm){
		try{
			String chat = "좀비모드가 시작됩니다. 입장을 원하시는분은 Yes를 눌러주세요";
			L1World.getInstance().broadcastPacketToAll(new S_Message_YN(391, chat));
		} catch (Exception e){
			gm.sendPackets(new S_SystemMessage(".좀비모드선언 커맨드 에러"));
		}
	}
	/////////////////////////////////////////좀비모드 시작메소드 by-Kingdom	
	/** 포비: 아지트 */
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
					pc.sendPackets(new S_SystemMessage(target.getClanname()+" 혈맹에게 "+pobyhouse.getHouseName()+"번을 지급하였습니다."));
					for (L1PcInstance tc : TargetClan.getOnlineClanMember()) {
						tc.sendPackets(new S_SystemMessage("게임마스터로부터 "+pobyhouse.getHouseName()+"번을 지급 받았습니다."));
					}
				} else {
					pc.sendPackets(new S_SystemMessage(target.getName()+"님은 혈맹에 속해 있지 않습니다."));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(73, pobyname));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("\\fY.아지트 <지급할혈맹원> <아지트번호>"));
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
				gm.sendPackets(new S_SystemMessage(clan_name1 + "혈맹이 존재하지 않습니다."));
				return;
			}
			if (clan2 == null ) {
				gm.sendPackets(new S_SystemMessage(clan_name2 + "혈맹이 존재하지 않습니다."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					gm.sendPackets(new S_SystemMessage("[" + clan_name1 + "]혈맹과 [" + clan_name2 + "]혈맹은 현재 전쟁 중 입니다."));
					return;
				}
			}
			L1War war = new L1War();
			war.handleCommands(2, clan_name1, clan_name2); // 모의전 개시
			for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
				pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]혈맹과 [" + clan_name2 + "]혈맹의 전쟁이 시작 되었습니다."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".혈전시작 혈맹이름 혈맹이름"));
		}
	}

	public void allGoodBuff(){
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if(pc.isPrivateShop() // 개인 상점이라면
					|| pc.noPlayerCK // 허상이나 그외 잡다한 녀석들
					|| pc.isDead()){ // 죽어있다면
				continue;
			} else { // 그외에 정상적인 녀석들에게 운영자의 메세지를 보낸다.
				pc.sendPackets(new S_Message_YN(456, "")); // 당신의 마음이 따뜻하고 포근해짐을 느낍니다.
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
				gm.sendPackets(new S_SystemMessage(clan_name1 + "혈맹이 존재하지 않습니다."));
				return;
			}
			if (clan2 == null ) {
				gm.sendPackets(new S_SystemMessage(clan_name2 + "혈맹이 존재하지 않습니다."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					war.CeaseWar(clan_name1, clan_name2);
					for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
						pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]혈맹과 [" + clan_name2 + "]혈맹의 전쟁이 종료 되었습니다."));
					}
					return;
				}
			}
			gm.sendPackets(new S_SystemMessage("[" + clan_name1 + "]혈맹과 [" + clan_name2 + "]혈맹은 현재 전쟁중이지 않습니다."));
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".혈전종료 혈맹이름 혈맹이름"));
		}
	}
	/** 접속중인 혈맹원 모두 소환 **/
	private void CallClan(L1PcInstance pc, String param) { 
		try {
			StringTokenizer st = new StringTokenizer(param);
			String clanname = st.nextToken();
			L1Clan clan = L1World.getInstance().getClan(clanname);
			if (clan != null) {
				for (L1PcInstance player : clan.getOnlineClanMember()) {
					if (!player.isPrivateShop() && !player.isFishing()) { // 상점 중이거나 낚시 제외.
						L1Teleport.teleportToTargetFront(player, pc, 2); // 명령어 실행자 2칸 앞으로 소환
					}
				}
				pc.sendPackets(new S_SystemMessage("[ "+clanname+" ] 혈맹을 소환하였습니다."));
			} else {
				pc.sendPackets(new S_SystemMessage("[ "+clanname+" ] 혈맹은 존재하지 않습니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".혈맹소환 [혈맹이름] 순서로 입력"));
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
						"그러한 이름의 캐릭터는 월드내에는 존재하지 않습니다."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".겸치복구 [캐릭터명]을 입력 해주세요."));
		}
	}

	private void startsWith(L1PcInstance gm, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			int level = Integer.parseInt(st.nextToken());
			GameServerSetting.getInstance().set_maxLevel(level);
			L1World.getInstance().broadcastPacketToAll(
					new S_PacketBox(S_PacketBox.GREEN_MESSAGE, level
							+ "레벨까지로 렙업을 제한합니다."));
		} catch (Exception e) {
		}
	}
	private void SerchClanMember(L1PcInstance gm, String param){
		try{
			StringTokenizer tok = new StringTokenizer(param);
			String type = tok.nextToken();
			L1Clan clan = L1World.getInstance().getClan(type);
			if(clan == null){
				gm.sendPackets(new S_SystemMessage("혈맹 이름이 정확하지 않거나 존재하지 않습니다."));
				return;
			}
			gm.sendPackets(new S_SystemMessage("혈맹 이름 : "+type+" 총인원 : "+clan.getClanMemberList().size()+"명 접속인원 : "+clan.getOnlineMemberCount()+"명"));
		}catch(Exception e){
			gm.sendPackets(new S_SystemMessage(".혈원확인 [혈맹이름]"));
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
				gm.sendPackets(new S_SystemMessage("1-90의 범위에서 지정해 주세요"));
				return;
			}
			target.setExp(ExpTable.getExpByLevel(level));
			gm.sendPackets(new S_SystemMessage(target.getName()+"님의 레벨이 변경됨!"));
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".렙업 [케릭명] [레벨] 최대90 라고 입력"));
		}
	}

	private void BattleZone(L1PcInstance gm, String param) {
		boolean power = false;
		if(!power){
			BattleZoneController.getInstance().start();
			//상태 초기화
			BattleZoneController.getInstance().setBattleOpen(true);
			BattleZoneController.getInstance().setBattleOne(true);
			BattleZoneController.getInstance().setBattleStart(false);
			BattleZoneController.getInstance().setBattling(false);

			BattleZoneController.getInstance().BattleTime();
			gm.sendPackets(new S_SystemMessage("배틀존을 시작하였습니다."));
		}else {
			gm.sendPackets(new S_SystemMessage("이미 배틀존 실행하였습니다."));
		}
	}	

	private void GongZi(L1PcInstance gm, String param)       // 이부분 부터 
	{
		if ( param.equalsIgnoreCase("on") )
		{ 
			gm.sendPackets(new S_SystemMessage("fY운영자의 전체공지가 있겠습니다 쉿!! "));
			L1World.getInstance().set_worldChatElabled(false);   
		}
		else if( param.equalsIgnoreCase("off") )
		{
			gm.sendPackets(new S_SystemMessage("fY이제 말할 수 있어요 호호호^^"));
			L1World.getInstance().set_worldChatElabled(true);
		}
		else
		{   
			gm.sendPackets(new S_SystemMessage(".공지 on  off  라고 입력해 주세요."));
		}
	}  // 이 부분 까지가 공지사항 

	private void allrecall(L1PcInstance gm) {
		try {
			for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
				if (!pc.isGm() && !pc.isPrivateShop() && !pc.isFishing()) {
					recallnow(gm, pc);
				}
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".전체소환 커멘드 에러"));
		}
	}

	private void recallnow(L1PcInstance gm, L1PcInstance target) {
		try {
			L1Teleport.teleportToTargetFront(target, gm, 2);
			gm.sendPackets(new S_SystemMessage((new StringBuilder()).append(target.getName()).append("님을 소환했습니다.").toString()));
			target.sendPackets(new S_SystemMessage("게임 마스터에게 소환되었습니다."));
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
				gm.sendPackets(new S_SystemMessage("해당캐릭의 채금을 해제 했습니다."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(
					".채금풀기 캐릭터명 이라고 입력해 주세요."));
		}
	}

	private void packetbox(L1PcInstance pc, String param) {
		try {
			StringTokenizer st = new StringTokenizer(param);
			int id = Integer.parseInt(st.nextToken(), 10);			
			pc.sendPackets(new S_PacketBox(id));
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage("[Command] .패킷박스 [id] 입력"));
		}
	}
	private void showHelp(L1PcInstance gm) {
		gm.sendPackets(new S_GMCommands(1));
	}
	private void autoshop(L1PcInstance gm, String param){
		if(param.equalsIgnoreCase("켬")){
			AutoShopManager.getInstance().isAutoShop(true);
			gm.sendPackets(new S_SystemMessage("[Command] 무인상점 켬"));
		} else if(param.equalsIgnoreCase("끔")){
			AutoShopManager.getInstance().isAutoShop(false);
			gm.sendPackets(new S_SystemMessage("[Command] 무인상점 끔"));
		} else {
			gm.sendPackets(new S_SystemMessage("[Command] .무인상점 [켬 or 끔] 입력"));
		}
	}
	private void searchDatabase(L1PcInstance gm, String param) {
		try {
			StringTokenizer tok = new StringTokenizer(param);
			int type = Integer.parseInt(tok.nextToken());
			String name = tok.nextToken();
			searchObject(gm, type, name);
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".검색 [0~4] [name]을 입력 해주세요."));
			gm.sendPackets(new S_SystemMessage("0=etcitem, 1=weapon, 2=armor, 3=npc, 4=polymorphs"));   
			gm.sendPackets(new S_SystemMessage("name을 정확히 모르거나 띄워쓰기 되어있는 경우는"));
			gm.sendPackets(new S_SystemMessage("'%'를 앞이나 뒤에 붙여 쓰십시오."));
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
			gm.sendPackets(new S_SystemMessage("총 [" + count + "]개의 데이터가 검색되었습니다."));
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
			if (target != null) { //타겟
				L1Teleport.teleport(target, 33437, 32812, (short) 4, 5, true); /// 가게될 지점 (유저가떨어지는지점)
			} else {
				gm.sendPackets(new S_SystemMessage("접속중이지 않는 유저 ID 입니다."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".가라 (보낼케릭터명) 으로 입력해 주세요."));
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
				gm.sendPackets(new S_SystemMessage("\\fT(" + (ment + i) +")번의 멘트는 위와 같습니다"));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".메세지 [번호] [갯수(기본1로적을것)]을 입력 해주세요."));
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
			gm.sendPackets(new S_SystemMessage(".네임  [id] [출현시키는 수]로 입력해 주세요. "));
		}
	}

	private void hellcall(L1PcInstance gm, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();

			L1PcInstance target = null; // q
			target = L1World.getInstance().getPlayer(pcName);
			if (target != null) { //타겟
				L1Teleport.teleport(target, 32693, 32756, (short) 666, 5, true); /// 가게될 지점 (유저가떨어지는지점)
			} else {
				gm.sendPackets(new S_SystemMessage("접속중이지 않는 유저 ID 입니다."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".지옥 (보낼케릭터명) 으로 입력해 주세요."));
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
						pc.sendPackets(new S_SystemMessage("운영자님께서 전체유저에게 선물로["+ item.getViewName()	+"]를 주었습니다."));
					}
				}
			}
		} catch (Exception exception) {
			gm.sendPackets(new S_SystemMessage(
					".전체선물 아이템번호 인첸트수 갯수로 입력해 주세요."));
		}

	}



	private void serversave (L1PcInstance pc) {//검색후 추가 
		Saveserver();//서버세이브 메소드 선언
		pc.sendPackets(new S_SystemMessage("서버저장이 완료되었습니다."));//지엠에게 알려주고
	}    

	/**서버저장**/
	private void Saveserver() {
		/**전체플레이어를 호출**/
		for(L1PcInstance player : L1World.getInstance().getAllPlayers()){
			try {
				/**피씨저장해주고**/
				player.save();
				/**인벤도 저장하고**/
				player.saveInventory();
			} catch (Exception ex) {
				/**예외 인벤저장**/
				player.saveInventory();
			}
		}
	}
	/**서버저장**/

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
				gm.sendPackets(new S_SystemMessage("[Command] 이미 계정이 있습니다."));
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
				gm.sendPackets(new S_SystemMessage("[Command] 계정 추가가 완료되었습니다."));				
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
			if(!Character.isDigit(str.charAt(i))	// 숫자가 아니라면
					&& Character.isLetterOrDigit(str.charAt(i))	// 특수문자라면
					&& !Character.isUpperCase(str.charAt(i))	// 대문자가 아니라면
					&& !Character.isLowerCase(str.charAt(i))) {	// 소문자가 아니라면
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
				gm.sendPackets(new S_SystemMessage("[Command] 입력하신 계정명의 자릿수가 너무 짧습니다."));
				gm.sendPackets(new S_SystemMessage("[Command] 최소 4자 이상 입력해 주십시오."));
				return;
			}
			if (passwd.length() < 4) {
				gm.sendPackets(new S_SystemMessage("[Command] 입력하신 암호의 자릿수가 너무 짧습니다."));
				gm.sendPackets(new S_SystemMessage("[Command] 최소 4자 이상 입력해 주십시오."));
				return;
			}

			if (passwd.length() > 12) {
				gm.sendPackets(new S_SystemMessage("[Command] 입력하신 암호의 자릿수가 너무 깁니다."));
				gm.sendPackets(new S_SystemMessage("[Command] 최대 12자 이하로 입력해 주십시오."));
				return;
			}

			if (isDisitAlpha(passwd) == false) {
				gm.sendPackets(new S_SystemMessage("[Command] 암호에 허용되지 않는 문자가 포함 되어 있습니다."));
				return;
			}
			AddAccount(gm, user, passwd,"127.0.0.1","127.0.0.1");
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage("[Command] .계정추가 [계정명] [암호] 입력"));
		}
	}

	private void changepassword(L1PcInstance gm, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String user = tok.nextToken();
			String oldpasswd = tok.nextToken();
			String newpasswd = tok.nextToken();

			if (user.length() < 4) {
				gm.sendPackets(new S_SystemMessage("[Command] 입력하신 계정명의 자릿수가 너무 짧습니다."));
				gm.sendPackets(new S_SystemMessage("[Command] 최소 4자 이상 입력해 주십시오."));
				return;
			}
			if (newpasswd.length() < 4) {
				gm.sendPackets(new S_SystemMessage("[Command] 입력하신 암호의 자릿수가 너무 짧습니다."));
				gm.sendPackets(new S_SystemMessage("[Command] 최소 4자 이상 입력해 주십시오."));
				return;
			}
			if (newpasswd.length() > 12) {
				gm.sendPackets(new S_SystemMessage("[Command] 입력하신 암호의 자릿수가 너무 깁니다."));
				gm.sendPackets(new S_SystemMessage("[Command] 최대 12자 이하로 입력해 주십시오."));
				return;
			}

			if (isDisitAlpha(newpasswd) == false) {
				gm.sendPackets(new S_SystemMessage("[Command] 암호에 허용되지 않는 문자가 포함 되어 있습니다."));
				return;
			}
			chkpassword(gm, user, oldpasswd, newpasswd);
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage("[Command] .비번변경 [계정] [현재비번] [바꿀비번] 입력"));
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
				gm.sendPackets(new S_SystemMessage("[Command] 입력하신 계정은 서버내에 존재 하지 않습니다."));
				return;
			}

			if (!isPasswordTrue(password,oldpassword)){				
				//System.out.println("현재 비번 : " + oldpassword+" - 체크 비번 : "+password);
				gm.sendPackets(new S_SystemMessage("[Command] 기존 계정명의 비밀번호가 일치하지 않습니다. "));
				gm.sendPackets(new S_SystemMessage("[Command] 다시 확인하시고 실행해 주세요."));
				return;
			} else {
				String sqlstr = "UPDATE accounts SET password=password(?) WHERE login=?";
				pstm = con.prepareStatement(sqlstr);
				pstm.setString(1, newpassword);
				pstm.setString(2, account);
				pstm.execute();
				gm.sendPackets(new S_SystemMessage("[Command] 계정명 : " + account+" / 바뀐비밀번호 : " + newpassword));
				gm.sendPackets(new S_SystemMessage("[Command] 비밀번호 변경이 정상적으로 완료되었습니다."));				
			}
			rs.close();
			pstm.close();
			statement.close();
			con.close();
		} catch (Exception e) {
		}
	}
	//패스워드 맞는지 여부 리턴  
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
			if ( _rtnPwd.equals(Password)) { // 동일하다면
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
			// ↓ 테스트할 코드가 보여질 전달 패킷 부분 
			pc.sendPackets(new S_Test(pc,codetest));
			//pc.sendPackets(new S_PacketBox(S_PacketBox.ICON_AURA, codetest, time)); // 그신,욕망 버프 테스트
			//pc.sendPackets(new S_CastleMaster(codetest, pc.getId())); // 왕관 테스트
			//pc.sendPackets(new S_StatusReset(pc, codetest, 1)); // 스텟 초기화 테스트
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage("[Command] .코드 [숫자] 입력"));
		}
	}

	private void prison(L1PcInstance gm, String param) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(param);
			String pcName = tokenizer.nextToken();

			L1PcInstance target = null; // q
			target = L1World.getInstance().getPlayer(pcName);

			if (target != null) { // 타겟
				L1Teleport.teleport(target, 32736, 32799, (short) 34, 5, true);
			} else {
				gm.sendPackets(new S_SystemMessage("접속중이지 않는 유저 ID 입니다."));
			}
		} catch (Exception e) {
			gm.sendPackets(new S_SystemMessage(".가라 (보낼케릭터명) 으로 입력해 주세요."));
		}
	}
	private void Clear(L1PcInstance gm) {
		for (L1Object obj : L1World.getInstance().getVisibleObjects(gm, 10)) { // 10 범위 내에 오브젝트를 찾아서
			if (obj instanceof L1MonsterInstance){ // 몬스터라면
				L1NpcInstance npc = (L1NpcInstance) obj;
				npc.receiveDamage(gm, 50000); // 데미지
				if (npc.getCurrentHp() <= 0){
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815)); // 디스
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}else{
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815));
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}
			}/*else if (obj instanceof L1PcInstance){ // pc라면
				L1PcInstance player = (L1PcInstance) obj;
				player.receiveDamage(player, 0, false); // 데미지
				if (player.getCurrentHp() <= 0){
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815)); // 디스
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}else{
					gm.sendPackets(new S_SkillSound(obj.getId() , 1815));
					Broadcaster.broadcastPacket(gm, new S_SkillSound(obj.getId() , 1815));
				}
			}*/
		}
	}
}
