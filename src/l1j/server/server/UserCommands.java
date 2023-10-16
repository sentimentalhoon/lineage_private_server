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
		// 최초의 공백까지가 커맨드, 그 이후는 공백을 단락으로 한 파라미터로서 취급한다
		String cmd = token.nextToken();
		String param = "";
		while (token.hasMoreTokens()) {
			param = new StringBuilder(param).append(token.nextToken()).append(' ').toString();
		}
		param = param.trim();

		if (cmd.equalsIgnoreCase("도움말")) {
			showHelp(pc);
		} else if (cmd.equalsIgnoreCase("조사")){
			infoitem(pc, param);
			/*
		} else if (cmd.equalsIgnoreCase("원격교환")) {
			MultiTrade(pc, param);
			 */
		} else if (cmd.equalsIgnoreCase("합치기")) {
			adenaAdd(pc);
		} else if (cmd.equalsIgnoreCase("랭킹")){
			infoRanking(pc);
		} else if (cmd.equalsIgnoreCase("버프")){
			buff(pc);
		} else if (cmd.equalsIgnoreCase(".") || cmd.equalsIgnoreCase("텔렉풀기")) {
			tell(pc);		
		} else if(cmd.equalsIgnoreCase("혈맹소환")) {
			CallClan(pc);
		} else if(cmd.equalsIgnoreCase("내정보")){
			check(pc);
		} else if (cmd.equalsIgnoreCase("암호변경")){
			changePassword(pc, param);
		} else if(cmd.equalsIgnoreCase("케릭명변경") || cmd.equalsIgnoreCase("캐릭명변경")) {
			charname(pc, param);
		} else if (cmd.equalsIgnoreCase("드랍")) { 
			serchdroplist(pc, param);
		} else if (cmd.equalsIgnoreCase("몹드랍")) {
			serchdroplist2(pc, param) ;
		} else if (cmd.equalsIgnoreCase("척살") || cmd.equalsIgnoreCase("수배")) {
			Hunt(pc, param);
		} else if (cmd.equalsIgnoreCase("나이") || cmd.equalsIgnoreCase("나이등록")) { 
			age(pc, param);
		} else if (cmd.equalsIgnoreCase("이름") || cmd.equalsIgnoreCase("이름등록")) { 
			username(pc, param);
		} else if (cmd.equalsIgnoreCase("입장시간")) { 
			entertime(pc);
		} else if(cmd.equalsIgnoreCase("루팅멘트")) {
			ment(pc, cmd, param); 
		} else if(cmd.equalsIgnoreCase("버그")) {
			bugracement(pc, cmd, param); 
		} else if(cmd.equalsIgnoreCase("이동") || cmd.equalsIgnoreCase("귀환")) {
			movement(pc, cmd, param); 
		} else if (cmd.equalsIgnoreCase("퀴즈설정")) {
			quize(pc, param);
		} else if (cmd.equalsIgnoreCase("퀴즈인증")) {
			quize1(pc, param);
		} else if (cmd.equalsIgnoreCase("살생부")) {
			moveToChar(pc, param);
		} else if(cmd.equalsIgnoreCase("혈맹파티")){
			ClanParty(pc);
		} else if (cmd.equalsIgnoreCase("원격파티")){
			Party(pc, param); //by.함정 //param<<<<<이거또추가해주세요 제가 배먹어습니다죄송 ㅠㅠ
		} else if (cmd.equalsIgnoreCase("칼")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.USERSTATUS_ATTACK, 0);
			tell(pc);//텔렉풀기도 한번 해주면 주변유저가 바로 보라돌이로 보이게됨
		} else if (cmd.equalsIgnoreCase("칼끔")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.USERSTATUS_ATTACK);
			tell(pc);
		} else if (cmd.equalsIgnoreCase("혈전시작")) {
			StartWar(pc, param);
		} else if (cmd.equalsIgnoreCase("혈전종료")) {
			StopWar(pc, param);
		} else if (cmd.equalsIgnoreCase("판매")) { 
			countR1(pc, param);
		} else if (cmd.equalsIgnoreCase("구입")) { 
			countR2(pc, param);
		} else if (cmd.equalsIgnoreCase("취소")) { 
			countR3(pc, param);
		} else if (cmd.equalsIgnoreCase("인형") || cmd.equalsIgnoreCase("인형정보")) { 
			pc.sendPackets(new S_SystemMessage("인형의 옵션은 본섭과 동일합니다."));
		} else if (cmd.equalsIgnoreCase("정보") || cmd.equalsIgnoreCase("서버정보")) { 
			pc.sendPackets(new S_SystemMessage("서버 정보는 기란 마을 공지사항 게시판에 있습니다."));
		} /*else if (cmd.equalsIgnoreCase("봉인해제신청") || cmd.equalsIgnoreCase("봉인해제주문서")) {
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
        	// 돕펠 처리
        	L1MonsterInstance mob = null;
        	for (L1Object obj : pc.getNearObjects().getKnownObjects()) {
        		if (obj instanceof L1MonsterInstance) {
        			mob = (L1MonsterInstance) obj;
        			if (mob.getNpcTemplate().is_doppel() && mob.getName().equals(pc.getName())) {
        				Broadcaster.broadcastPacket(mob, new S_NpcChatPacket(mob, cmdLine, 0));
        			}
        		}
        	}
        	eva.writeMessage(6, "ⓝ [" + pc.getName() + "] " + cmdLine);
        }
	}

	/** 위탁 판매 게시판 - 유저 명령어 관련 메서드 시작 **/
	private static void LetterList(L1PcInstance pc, int type, int count) {
		pc.sendPackets(new S_LetterList(pc,type,count));
	}

	public static void WriteLetter22(L1PcInstance pc , String in4, int in2 ,String in5) {
		int nu1 = 949; 
		SimpleDateFormat formatter = new SimpleDateFormat ( "yy/MM/dd", Locale.KOREA );
		Date currentTime = new Date ( );
		String dTime = formatter.format ( currentTime );
		String subject = "위탁물품판매완료";
		String content = "+ " + in2 +" " + in5 +"\n물품이 판매되었습니다. \n\n반드시 리스타트 후에 \n패키지 창고(기란)에서\n아데나를 회수하십시요.\n\n수수료 5%가 차감된\n아데나로 지급되었습니다.";
		String name = "위탁관리인";
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
				pc.sendPackets(new S_SystemMessage(".위탁판매  (판매금액)을 적어주세요."));
				return;
			}
			if (i > 1000000000) {
				pc.sendPackets(new S_SystemMessage("판매금액은 10억이상 판매하실수없습니다."));
				return;
			}
			if (i >= 1 && i < 100000) {
				pc.sendPackets(new S_SystemMessage("최소위탁금액은 10만이상 입니다."));
				return;
			}
			if (pc.getInventory().checkItem(447012)) { // 위탁판매 증서 아이템번호는 본인 팩에 맞게
				pc.sendPackets(new S_SystemMessage("위탁판매 증서를 소지 하고 있으면 불가능 합니다."));
			} else {
				pc.sendPackets(new S_SystemMessage("위탁판매 증서를 얻었습니다."));
				pc.getInventory().storeItem(447012 , i); // 위탁판매 증서 아이템번호는 본인 팩에 맞게
			}
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(".위탁판매 (판매할 금액) 이라고 적어주십시요."));
			pc.sendPackets(new S_SystemMessage("예)  .위탁판매 5000000"));
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
				pc.sendPackets(new S_SystemMessage("이미팔린물품이거나 구입번호가 틀리셨습니다."));
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
				pc.sendPackets(new S_SystemMessage("정상구입 되었습니다"));
			} else {
				pc.sendPackets(new S_SystemMessage("소지 금액이 부족합니다"));
			}
		} catch (SQLException e) { 
			pc.sendPackets(new S_SystemMessage(".위탁구입 (게시글번호) 라고 적어주십시요."));
			pc.sendPackets(new S_SystemMessage("예)  .위탁구입 0017"));
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
				pc.sendPackets(new S_SystemMessage("정상취소 되었습니다"));
			} else {
				pc.sendPackets(new S_SystemMessage("등록하신 물품이 아닙니다"));
			}
		} catch (SQLException e) {
			pc.sendPackets(new S_SystemMessage(".위탁취소 (게시글번호) 라고 적어주십시요."));
			pc.sendPackets(new S_SystemMessage("예)  .위탁취소 0035"));
		} finally { SQLUtil.close(rs, pstm, con); }
	}
	/** 위탁 판매 게시판 - 유저 명령어 관련 메서드 끝 **/

	/**  유저 명령어 이동 관련 **/
	public void movement(L1PcInstance pc, String cmdName, String arg) {
		try {
			int i = 0;
			try { 
				i = Integer.parseInt(arg);  
			} catch ( NumberFormatException e) {
				pc.sendPackets(new S_SystemMessage(".이동 [숫자]을 입력 해주세요."));
				pc.sendPackets(new S_SystemMessage("[0~8.시간의 균열],[9.류미엘],[10.기란5시],[11.기란7시],[12.아덴 바뮤트],[13.요리상],[14.드워프성],[15.기란무한대전]"));
				return;
			}
			if (StatusPc(pc)){ return; }
			pc.getInventory().consumeItem(40308, 5000);
			int[] loc = null;
			switch(i){
			case 0: loc = new int[] { 32728, 32704, 4 }; break;//시간의 균열 0 번 위치
			case 1: loc = new int[] { 32827, 32658, 4 }; break;//시간의 균열 1 번 위치
			case 2: loc = new int[] { 32852, 32713, 4 }; break;//시간의 균열 2 번 위치
			case 3: loc = new int[] { 32914, 33427, 4 }; break;//시간의 균열 3 번 위치
			case 4: loc = new int[] { 32962, 33251, 4 }; break;//시간의 균열 4 번 위치
			case 5: loc = new int[] { 32908, 33169, 4 }; break;//시간의 균열 5 번 위치
			case 6: loc = new int[] { 34272, 33361, 4 }; break;//시간의 균열6 번 위치
			case 7: loc = new int[] { 34258, 33202, 4 }; break;//시간의 균열 7 번 위치
			case 8: loc = new int[] { 34225, 33313, 4 }; break; //시간의 균열 8 번 위치
			case 9: loc = new int[] { 34123, 33148, 4 }; break;// 류미엘
			case 10: loc = new int[] { 33629, 32760, 4 }; break;//기란성 5시
			case 11: loc = new int[] { 33552, 32682, 4 }; break;//기란성7시
			case 12: loc = new int[] { 33975, 33168, 4 }; break;// 아덴성 바뮤트
			case 13: loc = new int[] { 32607, 32714, 4 }; break;// 글루디오 요리 상인
			case 14: loc = new int[] { 32765, 32862, 66 }; break;// 드워프성 입구
			case 15: loc = new int[] { 33499, 32766, 4 }; break;// 무한대전 입구
			case 16: loc = new int[] { 32611, 32771, 4 }; break;// 낚시터
			default:
				pc.sendPackets(new S_SystemMessage(".이동 [숫자]을 입력 해주세요."));
				pc.sendPackets(new S_SystemMessage("[0~8.시간의 균열],[9.류미엘],[10.기란5시],[11.기란7시],[12.아덴 바뮤트],[13.요리상],[14.드워프성],[15.기란무한대전],[16.낚시터]"));					
				break;
			}
			L1Teleport.teleport(pc, loc[0], loc[1], (short) loc[2], 2, true);
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(".이동 [숫자]을 입력 해주세요."));
			pc.sendPackets(new S_SystemMessage("[0~8.시간의 균열],[9.류미엘],[10.기란5시],[11.기란7시],[12.아덴 바뮤트],[13.요리상],[14.드워프성],[15.기란무한대전]"));	
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
				pc.sendPackets(new S_SystemMessage(clan_name1 + "혈맹이 존재하지 않습니다."));
				return;
			}
			if (clan2 == null ) {
				pc.sendPackets(new S_SystemMessage(clan_name2 + "혈맹이 존재하지 않습니다."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]혈맹과 [" + clan_name2 + "]혈맹은 현재 전쟁 중 입니다."));
					return;
				}
			}
			L1War war = new L1War();
			war.handleCommands(2, clan_name1, clan_name2); // 모의전 개시
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".혈전시작 혈맹이름 혈맹이름"));
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
				pc.sendPackets(new S_SystemMessage(clan_name1 + "혈맹이 존재하지 않습니다."));
				return;
			}
			if (clan2 == null ) {
				pc.sendPackets(new S_SystemMessage(clan_name2 + "혈맹이 존재하지 않습니다."));
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInSameWar(clan_name1, clan_name2) == true) {
					war.CeaseWar(clan_name1, clan_name2);
					return;
				}
			}
			pc.sendPackets(new S_SystemMessage("[" + clan_name1 + "]혈맹과 [" + clan_name2 + "]혈맹은 현재 전쟁중이지 않습니다."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".혈전종료 혈맹이름 혈맹이름"));
		}
	}
	/** 접속중인 혈맹원 모두 소환 -- By. 우니 */
	private void CallClan(L1PcInstance pc) { 
		try {
			String clanname = pc.getClanname();
			L1Clan clan = L1World.getInstance().getClan(clanname);
			if (pc.getClanRank() != 4 && !pc.isCrown()){
				pc.sendPackets(new S_ServerMessage(518)); // 이 명령은 혈맹 군주만이 이용할 수 있습니다.
				return;
			}
			int castle_id = L1CastleLocation.getCastleIdByArea(pc);
			if (castle_id != 0) { // 공성존에서 사용 불가
				pc.sendPackets(new S_ServerMessage(1008)); // 그 상태로는 시전할 수 없습니다.
				return;
			}
			int nowtime = Integer.valueOf(CommonUtil.dateFormat("hhmm"));
			if (nowtime >= 2250 && nowtime < 2300){
				pc.sendPackets(new S_ServerMessage(476));  // 아직 공성전 시간이 되지 않았습니다.
				return; 
			}

			if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_DELAY)) {
				pc.sendPackets(new S_ServerMessage(939)); // 아직은 사용할 수 없습니다
				return;
			}
			if (clan != null) {
				for (L1PcInstance player : clan.getOnlineClanMember()) {
					if (StatusPc(player) || player.isPrivateShop() || player.isFishing() || player == pc || player == null){ 
						continue; 
					} else { // 상점 중이거나 낚시 제외.
						player.sendPackets(new S_Message_YN(729, ""));
						//L1Teleport.teleportToTargetFront(player, pc, 2); // 명령어 실행자 2칸 앞으로 소환
					}
				}
				pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_DELAY, 600 * 1000); // by포비 딜레이적용
				pc.sendPackets(new S_SystemMessage("[ "+clanname+" ] 혈맹을 소환하였습니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".혈맹소환로 입력"));
		}
	}

	/////////**************** 원격파티 ***********////////////
	public static void Party(L1PcInstance pc, String arg) {
		L1PcInstance target = L1World.getInstance(). getPlayer(arg);
		try {
			if (target != null) {
				if (!(pc.getInventory().checkItem(40308, 10000))) {
					pc.sendPackets(new S_SystemMessage("원격파티는 1만 아데나가 필요합니다"));
					return;
				}
				if (arg.equals(pc.getName())) {
					pc.sendPackets(new S_SystemMessage("자신에게는 파티를 신청할 수 없습니다."));
					return;
				}
				long curtime = System.currentTimeMillis() / 1000;
				if (pc.getQuizTime() + 20 > curtime) {
					pc.sendPackets(new S_SystemMessage("20초간의 지연시간이 필요합니다."));
					return;
				}
				if (CheckPc(pc, arg)) return;
				if (!target.isParalyzed()) {
					pc.setPartyID(target.getId());
					target.setPartyID(pc.getId());
					target.sendPackets(new S_Message_YN(953, pc.getName()));
					pc.sendPackets(new S_SystemMessage(""+ target.getName() +" 님에게 파티을 요청하였습니다."));
				}
			} else {
				pc.sendPackets(new S_SystemMessage("\\fY[사용방법] .원격파티 [캐릭터명] 을 입력해주세요"));
			}
		} catch (Exception e) {
		}
	}
	/////////////***********원격파티 by.함정**************///////// <<<이건 원격교환 응용 `ㅡ`;;
	/** 혈맹 파티 신청 명령어 **/ 
	public void ClanParty (L1PcInstance pc){
		int ClanId = pc.getClanid();
		if (ClanId != 0 && pc.getClanRank() >= 3){ //Clan[O] [군주,수호기사]
			for (L1PcInstance SearchBlood : L1World.getInstance().getAllPlayers()) {
				if(SearchBlood.getClanid()!= ClanId || SearchBlood.isPrivateShop()
						|| SearchBlood.isInParty()){ // 클랜이 같지않다면[X], 이미파티중이면[X], 상점중[X]
					continue; // 포문탈출
				} else if(SearchBlood.getName() != pc.getName()){
					pc.setPartyType(1); // 파티타입 설정
					SearchBlood.setPartyID(pc.getId()); // 파티아이디 설정
					SearchBlood.sendPackets(new S_Message_YN(954, pc.getName())); // 분패파티 신청
					pc.sendPackets(new S_SystemMessage("당신은 ["+SearchBlood.getName()+"]에게 파티를 신청했습니다."));
				}
			}
		} else { // 클랜이 없거나 군주 또는 수호기사 [X]
			pc.sendPackets(new S_SystemMessage("혈맹의 군주, 수호기사만 사용할수 있습니다."));
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
			System.out.println("랭킹 조회 실패");
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(rs1);
			SQLUtil.close(pstm1);
			SQLUtil.close(con);
		}
		pc.sendPackets(new S_SystemMessage("\\fY[** "+ pc.getName() +"님의 랭킹 내용 **]"));
		pc.sendPackets(new S_SystemMessage(
				"\\fY전체 : " + allRank +
				"위 // 클래스 : " + classRank +"위"));
	}
	private void showHelp(L1PcInstance pc) {
		pc.sendPackets(new S_UserCommands(1));
	}


	private void moveToChar(L1PcInstance pc, String pcName) {
		try {
			L1PcInstance target = L1World.getInstance().getPlayer(pcName);
			// 세이프티존에서만 사용 가능
			if (!pc.getMap().isSafetyZone(pc.getLocation())) {   
				pc.sendPackets(new S_ServerMessage(276));  // 이 곳에서는 무작위 텔레포트를 사용할 수 없습니다.
				return;
			}		   
			//전투중에는 사용 불가
			if (pc.isPinkName()){
				pc.sendPackets(new S_ServerMessage(215));  // 전투 중에 순간 이동 할 수 없습니다.
				return;
			}
			//같은 혈맹일 경우
			if (pc.getClanid() == target.getClanid()){
				pc.sendPackets(new S_SystemMessage("\\fV같은 혈맹원끼리는 사용할 수 없습니다."));
				return;
			}
			// 운영자한테는 못 간다
			if(target.isGm()){
				pc.sendPackets(new S_SystemMessage("\\fU운영자님에게는 갈 수 없습니다."));
				return;
			}
			// 내성에 있거나 못 가는 곳에 있을 경우
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
					|| target.getMapId() == 603  // 발록방
					|| target.getMapId() == 522 || target.getMapId() == 523 || target.getMapId() == 524  // 그림자신전
					|| target.getMapId() == 5167 // 악마왕의 영토
			){
				pc.sendPackets(new S_SystemMessage("\\fU상대방이 살생부로 갈 수 없는 장소에 있습니다."));
				return; 
			}
			 */
			// 세이프티존에 있으면 못 간다.
			if (target.getMap().isSafetyZone(target.getLocation())) {   
				pc.sendPackets(new S_SystemMessage("\\fU상대방이 세이프티 존에 있기 때문에 사용할 수 없습니다."));
				return;
			}

			int castle_id = L1CastleLocation.getCastleIdByArea(pc);
			if (castle_id != 0) { // 공성존에서 사용 불가
				pc.sendPackets(new S_SystemMessage("\\fY공성지역에서는 사용 할 수 없습니다."));
				return;
			}
			/*
			if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_POBYTELL)) {
				pc.sendPackets(new S_SystemMessage("\\fY딜레이 중입니다. 잠시 후에 사용하실 수 있습니다."));
				return;
			}
			 */
			if (pc.getLevel() >= 60
					& pc.getInventory().checkItem(41159, 100)
					& pc.getInventory().checkItem(5000216, 1)) {
				pc.getInventory().consumeItem(41159, 100);
				pc.getInventory().consumeItem(5000216, 1);
				//pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_POBYTELL, 300 * 1000); // by포비 딜레이적용
				L1Teleport.teleport(pc, target.getX(), target.getY(), target.getMapId(), 5, false);
			} else {
				pc.sendPackets(new S_SystemMessage("사용)살생부[1],Lv60이상,깃털[100] 필요합니다"));
			}
			pc.sendPackets(new S_SystemMessage((new StringBuilder())
					.append(pcName)
					.append("님에게 사용하였습니다.")
					.toString()));
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(
					"\\fY .살생부 [가고싶은상대 캐릭터명]을 입력 해주세요."));
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
					pc.sendPackets(new S_SystemMessage(""+ target.getName() +" 님에게 원격교환을 요청하였습니다."));
				}
			} else {
				pc.sendPackets(new S_SystemMessage("상대방이 접속중이 아닙니다. 다시 확인 바랍니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".원격교환 [캐릭터명] 으로 입력해 주세요."));
		}
	}

	public static boolean StatusPc(L1PcInstance pc) {
		if (!pc.getMap().isEscapable()) {
			pc.sendPackets(new S_ServerMessage(626)); // 이 위치에서는 그 곳으로 이동할 수 없습니다.
			return true;
		}
		if (!pc.getInventory().checkItem(40308, 5000)) {
			pc.sendPackets(new S_ServerMessage(189)); // 아데나가 충분치 않습니다.
			return true;
		}
		if (pc.isPinkName()) {
			pc.sendPackets(new S_ServerMessage(215)); // 전투 중에 순간이동할 수 없습니다.
			return true;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)) {
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) {
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true; 
		}			
		if (pc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)) {
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true; 
		}			
		if (pc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH)) {
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true; 
		}	
		if (pc.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) {
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true;
		}	
		if (pc.getSkillEffectTimerSet().hasSkillEffect(MOB_BASILL)) {
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true;
		}			
		if (pc.getSkillEffectTimerSet().hasSkillEffect(MOB_COCA)) {
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true;
		}
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (castle_id != 0) { // 공성존에서 사용 불가
			pc.sendPackets(new S_ServerMessage(538)); // 여기에서 탈출하는 것을 불가능합니다.
			return true;
		}
		return false;
	}
	private static boolean CheckPc(L1PcInstance pc, String arg) {
		L1PcInstance target = L1World.getInstance(). getPlayer(arg);
		if (pc.isGhost()) return true;
		int castle_id = L1CastleLocation.getCastleIdByArea(pc); 
		if (castle_id != 0)return true;//무한몸빵방지,공성존에선 원격교환불가 

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

			pc.sendPackets(new S_ServerMessage(2535, "기란 감옥", time1)); // 2535 %0 : 남은 시간 %1 분 
			pc.sendPackets(new S_ServerMessage(2535, "상아탑", time3));
			pc.sendPackets(new S_ServerMessage(2535, "라스타바드 던전", time2));
		} catch (Exception e) {
		}
	}
	private void check(L1PcInstance pc) {
		try {
			long curtime = System.currentTimeMillis() / 1000;
			if (pc.getQuizTime() + 20 > curtime ) {
				pc.sendPackets(new S_SystemMessage("20초간의 지연시간이 필요합니다."));
				return;
			}
			int entertime = pc.getGdungeonTime() % 1000;
			int a = 180 - entertime;
			int hpr = pc.getHpr() + pc.getInventory(). hpRegenPerTick();
			int mpr = pc.getMpr() + pc.getInventory(). mpRegenPerTick();	

			pc.sendPackets(new S_SystemMessage("=================( 나의 케릭터 정보 )==================="));				
			pc.sendPackets(new S_SystemMessage("\\fT(HPr:" + hpr + ')' + "(MPr:" + mpr + ')' +"(기감:"+a+"분)(PK횟수:" + pc.get_PKcount() + ")(엘릭:"+ pc.getAbility().getElixirCount()+ "개)"));
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
			pc.sendPackets(new S_SystemMessage("총 " + count + "아데나를 합쳤습니다."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".아덴합치기 로 입력해주세요."));
		}
	}

	private void age(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String AGE = tok.nextToken();
			int AGEint = Integer.parseInt(AGE);

			if (AGEint > 99) {
				pc.sendPackets(new S_SystemMessage("입력하신 나이는 올바른 값이 아닙니다."));
				return;
			}

			pc.setAge(AGEint);
			pc.save();
			pc.sendPackets(new S_SystemMessage(pc.getName()+" 님의 나이 (" + AGEint
					+ ")가 설정되었습니다."));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("  사용 예) .나이 28"));
		}
	}

	private void username(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String USERNAME = tok.nextToken();

			for (int i = 0;i<USERNAME.length();i++) {  
				if (USERNAME.charAt(i) == 'ㄱ' || USERNAME.charAt(i) == 'ㄲ' || USERNAME.charAt(i) == 'ㄴ' || USERNAME.charAt(i) == 'ㄷ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == 'ㄸ' || USERNAME.charAt(i) == 'ㄹ' || USERNAME.charAt(i) == 'ㅁ' || USERNAME.charAt(i) == 'ㅂ' ||    //한문자(char)단위로 비교 
						USERNAME.charAt(i) == 'ㅃ' || USERNAME.charAt(i) == 'ㅅ' || USERNAME.charAt(i) == 'ㅆ' || USERNAME.charAt(i) == 'ㅇ' ||    //한문자(char)단위로 비교
						USERNAME.charAt(i) == 'ㅈ' || USERNAME.charAt(i) == 'ㅉ' || USERNAME.charAt(i) == 'ㅊ' || USERNAME.charAt(i) == 'ㅋ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == 'ㅌ' || USERNAME.charAt(i) == 'ㅍ' || USERNAME.charAt(i) == 'ㅎ' || USERNAME.charAt(i) == 'ㅛ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == 'ㅕ' || USERNAME.charAt(i) == 'ㅑ' || USERNAME.charAt(i) == 'ㅐ' || USERNAME.charAt(i) == 'ㅔ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == 'ㅗ' || USERNAME.charAt(i) == 'ㅓ' || USERNAME.charAt(i) == 'ㅏ' || USERNAME.charAt(i) == 'ㅣ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == 'ㅠ' || USERNAME.charAt(i) == 'ㅜ' || USERNAME.charAt(i) == 'ㅡ' || USERNAME.charAt(i) == 'ㅒ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == 'ㅖ' || USERNAME.charAt(i) == 'ㅢ' || USERNAME.charAt(i) == 'ㅟ' || USERNAME.charAt(i) == 'ㅝ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == 'ㅞ' || USERNAME.charAt(i) == 'ㅙ' || USERNAME.charAt(i) == 'ㅚ' || USERNAME.charAt(i) == 'ㅘ' ||    //한문자(char)단위로 비교.
						USERNAME.charAt(i) == '씹' || USERNAME.charAt(i) == '좃' || USERNAME.charAt(i) == '좆' || USERNAME.charAt(i) == 'ㅤ'){
					pc.sendPackets(new S_SystemMessage("사용할수없는 이름입니다."));
					return; 
				}
			}
			if (USERNAME.length() == 0) {
				pc.sendPackets(new S_SystemMessage("변경할 이름을 입력하세요."));
				return;
			}
			if (USERNAME.length() <= 1 || USERNAME.length() >= 5) {
				pc.sendPackets(new S_SystemMessage("이름은 2글자 이상 5글자 미만로 입력하셔야합니다."));
				return;
			}
			if (isInvalidName(USERNAME)) {
				pc.sendPackets(new S_SystemMessage("사용할 수 없는 이름입니다."));
				return;
			}
			pc.setUserName(USERNAME);
			pc.save();

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("  사용 예) .이름 이름명"));
		}
	}

	private void charname(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String chaName = tok.nextToken();
			if (pc.getClanid() > 0){
				pc.sendPackets(new S_SystemMessage("\\fU혈맹탈퇴후 캐릭명을 변경할수 있습니다."));
				return;
			}
			if (!pc.getInventory().checkItem(467009, 1)) { // 있나 체크
				pc.sendPackets(new S_SystemMessage("\\fU케릭명 변경 증표가 없습니다."));
				return; 
			}
			for (int i = 0;i<chaName.length();i++) {  
				if (chaName.charAt(i) == 'ㄱ' || chaName.charAt(i) == 'ㄲ' || chaName.charAt(i) == 'ㄴ' || chaName.charAt(i) == 'ㄷ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == 'ㄸ' || chaName.charAt(i) == 'ㄹ' || chaName.charAt(i) == 'ㅁ' || chaName.charAt(i) == 'ㅂ' ||    //한문자(char)단위로 비교 
						chaName.charAt(i) == 'ㅃ' || chaName.charAt(i) == 'ㅅ' || chaName.charAt(i) == 'ㅆ' || chaName.charAt(i) == 'ㅇ' ||    //한문자(char)단위로 비교
						chaName.charAt(i) == 'ㅈ' || chaName.charAt(i) == 'ㅉ' || chaName.charAt(i) == 'ㅊ' || chaName.charAt(i) == 'ㅋ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == 'ㅌ' || chaName.charAt(i) == 'ㅍ' || chaName.charAt(i) == 'ㅎ' || chaName.charAt(i) == 'ㅛ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == 'ㅕ' || chaName.charAt(i) == 'ㅑ' || chaName.charAt(i) == 'ㅐ' || chaName.charAt(i) == 'ㅔ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == 'ㅗ' || chaName.charAt(i) == 'ㅓ' || chaName.charAt(i) == 'ㅏ' || chaName.charAt(i) == 'ㅣ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == 'ㅠ' || chaName.charAt(i) == 'ㅜ' || chaName.charAt(i) == 'ㅡ' || chaName.charAt(i) == 'ㅒ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == 'ㅖ' || chaName.charAt(i) == 'ㅢ' || chaName.charAt(i) == 'ㅟ' || chaName.charAt(i) == 'ㅝ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == 'ㅞ' || chaName.charAt(i) == 'ㅙ' || chaName.charAt(i) == 'ㅚ' || chaName.charAt(i) == 'ㅘ' ||    //한문자(char)단위로 비교.
						chaName.charAt(i) == '씹' || chaName.charAt(i) == '좃' || chaName.charAt(i) == '좆' || chaName.charAt(i) == 'ㅤ'){
					pc.sendPackets(new S_SystemMessage("사용할수없는 케릭명입니다."));
					return; 
				}
			}
			if (chaName.length() == 0) {
				pc.sendPackets(new S_SystemMessage("변경할 케릭명을 입력하세요."));
				return;
			}
			if (BadNamesList.getInstance().isBadName(chaName)) {
				pc.sendPackets(new S_SystemMessage("사용할 수 없는 케릭명입니다."));
				return;
			}
			if (isInvalidName(chaName)) {
				pc.sendPackets(new S_SystemMessage("사용할 수 없는 케릭명입니다."));
				return;
			}
			if (CharacterTable.doesCharNameExist(chaName)) {
				pc.sendPackets(new S_SystemMessage("동일한 케릭명이 존재합니다."));
				return;
			}
			pc.getInventory().consumeItem(467009, 1); // 소모됩니다.
			String oldname = pc.getName();
			chaname(chaName,oldname);
			chanameok(pc);
			L1World.getInstance().broadcastServerMessage("\\fY"+ oldname +"님이 "+ chaName + "으로 닉네임을 변경하였습니다.");
		} catch (Exception e){
			pc.sendPackets(new S_SystemMessage("[.케릭명변경] [바꾸실아이디] 입력해주세요."));
		}
	}
	/** 변경 가능한지 검사한다 시작**/
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
	/** 변경 가능한지 검사한다 끝**/
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
				pc.sendPackets(new S_SystemMessage("전투중이라 사용할 수 없습니다."));
				return;
			}
			long curtime = System.currentTimeMillis() / 1000;
			if (pc.getQuizTime() + 15 > curtime ) {
				pc.sendPackets(new S_SystemMessage("15초후 다시 사용할 수 있습니다."));
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
					gm.sendPackets(new S_SystemMessage("해당 아이템이 발견되지 않았습니다."));
					return;
				}
			}
			gm.sendPackets(new S_Serchdrop(itemid));
		} catch (Exception e) {
			//   _log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			gm.sendPackets(new S_SystemMessage(".드랍리스트 [아이템이름]을 입력해 주세요."));
			gm.sendPackets(new S_SystemMessage("아이템이름을 공백없이 정확히 입력해야 합니다."));
			gm.sendPackets(new S_SystemMessage("ex) .드랍 마법서(디스인티그레이트) -- > 검색 O"));
			gm.sendPackets(new S_SystemMessage("ex) .드랍 디스 -- > 검색 X"));
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
					gm.sendPackets(new S_SystemMessage("해당 몬스터가 발견되지 않았습니다."));
					return;
				}
			}
			gm.sendPackets(new S_Serchdrop2(npcid));
		} catch (Exception e) {
			//   _log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			gm.sendPackets(new S_SystemMessage(".몹드랍 [몹이름]을 입력해 주세요."));
			gm.sendPackets(new S_SystemMessage("몬스터 [몹이름]을 공백없이 정확히 입력해야 합니다."));
			gm.sendPackets(new S_SystemMessage("ex) .몹드랍 사이클롭스 -- > 검색 O"));
			gm.sendPackets(new S_SystemMessage("ex) .몹드랍 사이 클롭스 -- > 검색 X"));
		}
	}

	private static boolean isDisitAlpha(String str) {  
		boolean check = true;
		for(int i = 0; i < str.length(); i++) {
			if(!Character.isDigit(str.charAt(i)) // 숫자가 아니라면
					&& Character.isLetterOrDigit(str.charAt(i)) // 특수문자라면
					//&& !Character.isUpperCase(str.charAt(i)) // 대문자가 아니라면
					&& Character.isWhitespace(str.charAt(i)) // 공백이라면
					&& !Character.isLowerCase(str.charAt(i))) { // 소문자가 아니라면
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

			Account account = Account.load(pc.getAccountName()); //추가 
			if(account.getquize() != null){
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage(".퀴즈인증 [설정한퀴즈] \\fY인증후 다시입력하세요."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			} // 암호변경시 퀴즈가 설정되어 있지 않다면 바꿀 수 없도록.

			if (passwd.length() < 4) {
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage("최소 4자 이상 입력해 주십시오."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			}

			if (passwd.length() > 12) {
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage("최대 10자 이하로 입력해 주십시오."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			}

			if (isDisitAlpha(passwd) == false) {
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				pc.sendPackets(new S_SystemMessage("암호에 허용되지 않는 문자가 포함 되어 있습니다."));
				pc.sendPackets(new S_SystemMessage(
						"\\fY-------------------------------------------------------"));
				return;
			}

			to_Change_Passwd(pc, passwd);
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".암호변경 [변경하실 암호]를 입력하세요."));
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
				pc.sendPackets(new S_SystemMessage("\\fY-변경정보- 계정명:[" + login + "] 비밀번호:[" + passwd + "]"));
				pc.sendPackets(new S_SystemMessage("[" + pc.getName() + "]\\fY님의 암호변경이 성공적으로 완료되었습니다."));
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
				pc.sendPackets(new S_SystemMessage("입력하신 퀴즈의 자릿수가 너무 짧습니다."));
				pc.sendPackets(new S_SystemMessage("최소 4자 이상 입력해 주십시오."));
				return;
			}

			if (quize.length() > 12) {
				pc.sendPackets(new S_SystemMessage("입력하신 퀴즈의 자릿수가 너무 깁니다."));
				pc.sendPackets(new S_SystemMessage("최대 12자 이하로 입력해 주십시오."));
				return;
			}
			if (isDisitAlpha(quize) == false) {
				pc.sendPackets(new S_SystemMessage("퀴즈에 허용되지 않는 문자가 포함되었습니다."));
				return;
			}

			if(account.getquize() != null){
				pc.sendPackets(new S_SystemMessage("이미 퀴즈가 설정되어 있습니다."));
				return;
			}
			account.setquize(quize);
			Account.updateQuize(account);
			pc.sendPackets(new S_SystemMessage("\\fY퀴즈 (" + quize + ") 가 설정되었습니다."));
			pc.sendPackets(new S_SystemMessage("\\fY설정하신 퀴즈를 인증후 새로 설정하실수있습니다."));			 
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".퀴즈설정 [설정하실퀴즈]를 입력해주세요."));
		}
	}
	private void quize1(L1PcInstance pc, String cmd) {
		try {
			StringTokenizer tok = new StringTokenizer(cmd);
			String quize2 = tok.nextToken();
			Account account = Account.load(pc.getAccountName());

			if (quize2.length() < 4) {
				pc.sendPackets(new S_SystemMessage("입력하신 퀴즈의 자릿수가 너무 짧습니다."));
				pc.sendPackets(new S_SystemMessage("최소 4자 이상 입력해 주십시오."));
				return;
			}

			if (quize2.length() > 12) {
				pc.sendPackets(new S_SystemMessage("입력하신 퀴즈의 자릿수가 너무 깁니다."));
				pc.sendPackets(new S_SystemMessage("최대 12자 이하로 입력해 주십시오."));
				return;
			}

			if(account.getquize() == null || account.getquize() == ""){
				pc.sendPackets(new S_SystemMessage("퀴즈가 설정되어 있지 않습니다."));
				pc.sendPackets(new S_SystemMessage(".퀴즈설정 [캐릭터명] [설정하실퀴즈]를 입력하세요."));
				return;
			}
			if (!quize2.equals(account.getquize())) {
				pc.sendPackets(new S_SystemMessage("설정된 퀴즈와 일치하지 않습니다."));
				pc.sendPackets(new S_SystemMessage("만약 퀴즈를 잃어버리셨다면 건의사항에 올려주세요."));
				return;
			}
			if (isDisitAlpha(quize2) == false ) {
				pc.sendPackets(new S_SystemMessage("퀴즈에 허용되지 않는 문자가 포함되었습니다."));
				return;
			}
			account.setquize(null);
			Account.updateQuize(account);
			pc.sendPackets(new S_SystemMessage("\\fY퀴즈인증완료 되었습니다."));
			pc.sendPackets(new S_SystemMessage("\\fY퀴즈인증후 다시 새로운 퀴즈를 설정하셔야합니다."));

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".퀴즈인증 [설정하신퀴즈]를 입력하세요."));
		}
	}

	private void Hunt(L1PcInstance pc, String cmd) {
		try { 
			StringTokenizer st = new StringTokenizer(cmd);
			String char_name = st.nextToken();
			//int price = Integer.parseInt(st.nextToken());
			//String story = st.nextToken();
			int huntprice = 500000;  // 척상 기본 금액
			L1PcInstance target = null;
			target = L1World.getInstance().getPlayer(char_name);
			if (target != null) {
				if (target.isGm()){ return;}
				/*
				if (char_name.equals(pc.getName())) {
					pc.sendPackets(new S_SystemMessage("자신에게 척살령을 내릴수 없습니다."));
					return;}
				 */
				//if (target.getHuntCount() == 1) {
				//	pc.sendPackets(new S_SystemMessage("해당 유저는 이미 척살령이 떨어졌습니다."));
				//	return;
				//}
				/*
				if (price < huntprice) {
					pc.sendPackets(new S_SystemMessage("최소 금액은 50만 아덴입니다"));
					return;
				}
				 */
				if (!(pc.getInventory().checkItem(40308, huntprice))) {
					pc.sendPackets(new S_SystemMessage("척살령을 내릴려면 50만 아데나가 필요합니다."));
					return;
				}
				/*
				if (story.length() > 30) {
					pc.sendPackets(new S_SystemMessage("이유는 짧게 입력하세요"));
					return;
				}
				 */
				target.setHuntCount(target.getHuntCount() + 1);
				target.setHuntPrice(target.getHuntPrice() + huntprice);
				//target.setReasonToHunt(story);
				target.save();
				//  L1World.getInstance().broadcastPacketToAll(new S_SystemMessage(""+pc.getName()+ ": " + story + "이유로"));
				L1World.getInstance().broadcastServerMessage("\\fY" + pc.getName() +"님이 " + target.getName()+ "님에게 " + target.getHuntCount() +"번째 척살을 선포!");
				L1World.getInstance().broadcastServerMessage(target.getName()+ "님에게 걸린 현상금은 현재 총 " + target.getHuntPrice()+ "아데나입니다.");
				pc.getInventory().consumeItem(40308, huntprice);
			} else {
				pc.sendPackets(new S_SystemMessage("접속중이지 않은 캐릭터입니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".척살 [캐릭터명]"));
			//pc.sendPackets(new S_SystemMessage("척살령이 떨어진 케릭은 타격치가 증가합니다."));
		}
	}


	public void ment(L1PcInstance pc, String cmd, String param) { 
		if (param.equalsIgnoreCase("끔")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_MENT, 0);
			pc.sendPackets(new S_SystemMessage("\\fY오토루팅 멘트를 끕니다."));
		} else if (param.equalsIgnoreCase("켬")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_MENT);
			pc.sendPackets(new S_SystemMessage("\\fY오토루팅 멘트를 켭니다."));

		} else {
			pc.sendPackets(new S_SystemMessage(cmd + " [켬,끔] 라고 입력해 주세요. "));
		}
	}

	public void bugracement(L1PcInstance pc, String cmd, String param) { 
		if (param.equalsIgnoreCase("끔")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.BUGRACEMENT, 0);
			pc.sendPackets(new S_SystemMessage("\\fY버그레이스 멘트를 끕니다."));
		} else if (param.equalsIgnoreCase("켬")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.BUGRACEMENT);
			pc.sendPackets(new S_SystemMessage("\\fY버그레이스 멘트를 켭니다."));
		} else {
			pc.sendPackets(new S_SystemMessage(cmd + " [켬,끔] 라고 입력해 주세요. "));
		}
	}

	private void buff(L1PcInstance pc){
		if (pc.getLevel() > Config.NEWUSERSAFETY_LEVEL){
			pc.sendPackets(new S_SystemMessage("초보버프는 " + Config.NEWUSERSAFETY_LEVEL +"레벨까지 사용 가능합니다."));
			return;
		}
		int[] allBuffSkill = { PHYSICAL_ENCHANT_DEX, PHYSICAL_ENCHANT_STR, BLESS_WEAPON, ADVANCE_SPIRIT, IRON_SKIN, ADVANCE_SPIRIT};
		L1SkillUse l1skilluse = null;

		l1skilluse = new L1SkillUse();
		for (int i = 0; i < allBuffSkill.length ; i++) {
			l1skilluse.handleCommands(pc, allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
		}
		pc.sendPackets(new S_SystemMessage("초보버프는 "+ Config.NEWUSERSAFETY_LEVEL + "레벨까지 사용 가능합니다."));		
	}

	private void infoitem(L1PcInstance pc, String param){
		try {
			StringTokenizer tok = new StringTokenizer(param);
			String charname = tok.nextToken();
			L1PcInstance target = L1World.getInstance().getPlayer(charname);
			if(!pc.isGm() && (charname.compareTo("메티스")==0)){
				//pc.sendPackets(new S_SystemMessage("운영자님을 조사를 할수 없습니다."));
				return;
			}
			if (target != null){
				pc.sendPackets(new S_SystemMessage("\\fY[** "+ target.getName() +"님의 조사 내용 **]"));
				pc.sendPackets(new S_SystemMessage(
						"\\fT+9무기 이상 : " + target.getInventory().getItemEnchantCount(1, 9) +
						"개 // +7방어구 이상 : " + target.getInventory().getArmorEnchantCount(7)+"개"));
			} else {
				pc.sendPackets(new S_SystemMessage("현재 플레이 유저 중 조사하신 ["+charname+"] 유저는 없습니다."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".조사 [캐릭터명] 으로 입력해 주시기 바랍니다."));
		}	
	}
}
