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

package l1j.server.server.clientpackets;

import java.util.StringTokenizer;

import l1j.server.Config;
import l1j.server.GameSystem.Akduk1GameSystem;
import l1j.server.GameSystem.Akduk2GameSystem;
import l1j.server.GameSystem.Akduk3GameSystem;
import l1j.server.GameSystem.Akduk4GameSystem;
import l1j.server.channel.ChatMonitorChannel;
import l1j.server.server.GM2Commands;
import l1j.server.server.GMCommands;
import l1j.server.server.Lotto;
import l1j.server.server.ObjectIdFactory;
import l1j.server.server.Opcodes;
import l1j.server.server.UserCommands;
import l1j.server.server.datatables.ChatLogTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1EtcItem;
import server.LineageClient;
import server.manager.eva;





//Referenced classes of package l1j.server.server.clientpackets:
//ClientBasePacket

//chat opecode type
//통상 0x44 0x00
//절규(! ) 0x44 0x00
//속삭임(") 0x56 charname
//전체(&) 0x72 0x03
//트레이드($) 0x44 0x00
//PT(#) 0x44 0x0b
//혈맹(@) 0x44 0x04
//연합(%) 0x44 0x0d
//CPT(*) 0x44 0x0e

public class C_Chat extends ClientBasePacket {

	private static final String C_CHAT = "[C] C_Chat";

	public C_Chat(byte abyte0[], LineageClient clientthread) {
		super(abyte0);
		L1PcInstance pc = clientthread.getActiveChar();
		int chatType = readC();
		String chatText = readS();
		if(pc.get_autogo()==1&&chatText.equals(pc.get_autocode())) //오토입력을 대기중&&입력한코드와 오토코드가동일할때  
		{           
			pc.sendPackets(new S_SystemMessage("오토 방지 코드가 인증되었습니다. "));
			pc.set_autook(0); 
			pc.set_autogo(0);

		}else if (pc.get_autogo()==1){ // 오토입력을 대기중 입력한코드와 오토코드가 불일치할때
			pc.sendPackets(new S_SystemMessage("오토 방지 코드 입력 실패! 코드:"+pc.get_autocode()+"를 다시입력해주세요. "));
			pc.set_autook(1); 
			pc.set_autoct(pc.get_autoct()+1);
			pc.set_autogo(1);
		} 
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SILENCE)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.AREA_OF_SILENCE)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_POISON_SILENCE)) {
			return;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_CHAT_PROHIBITED)) { // 채팅 금지중
			pc.sendPackets(new S_ServerMessage(242)); // 현재 채팅 금지중입니다.
			return;
		}

		if (pc.isDeathMatch() && !pc.isGhost()) {
			pc.sendPackets(new S_ServerMessage(912)); // 채팅을 할 수 없습니다.
			return;
		}

		switch(chatType){
		case 0 :{
			if (pc.isGhost() && !(pc.isGm() || pc.isMonitor())) {
				return;
			}
			// GM커멘드
			if (chatText.startsWith(".")){
				if (pc.getAccessLevel() == Config.GMCODE ){
					String cmd = chatText.substring(1);
					GMCommands.getInstance().handleCommands(pc, cmd);
					return;	
				} else if (pc.getAccessLevel() == 300 ){
					String cmd = chatText.substring(1);
					GM2Commands.getInstance().handleCommands(pc, cmd);					
					return;
				} else {			
					String cmd = chatText.substring(1);
					UserCommands.getInstance().handleCommands(pc, cmd);
					return;
				}
			}

			if (chatText.startsWith("$")) {
				String text = chatText.substring(1);
				chatWorld(pc, text, 12);
				if (!pc.isGm()) {
					pc.checkChatInterval();
				}
				return;
			}	


			/** 주사위 소막  */
			Akduk2GameSystem gam = new Akduk2GameSystem(); //주사위
			if(pc.isGambling()){
				if (chatText.startsWith("홀")) {
					gam.Gambling2(pc, chatText, 1);
					return;
				}else if (chatText.startsWith("짝")) {
					gam.Gambling2(pc, chatText, 2);
					return;
				}else if (chatText.startsWith("1")) {
					gam.Gambling2(pc, chatText, 3);
					return;
				}else if (chatText.startsWith("2")) {
					gam.Gambling2(pc, chatText, 4);
					return;
				}else if (chatText.startsWith("3")) {
					gam.Gambling2(pc, chatText, 5);
					return;
				}else if (chatText.startsWith("4")) {
					gam.Gambling2(pc, chatText, 6);
					return;
				}else if (chatText.startsWith("5")) {
					gam.Gambling2(pc, chatText, 7);
					return;
				}else if (chatText.startsWith("6")) {
					gam.Gambling2(pc, chatText, 8);
					return;
				}
			}

			if(pc.isGambling1()){ // 소막 큰버전
				Akduk3GameSystem gam1 = new Akduk3GameSystem();
				if (chatText.startsWith("오크전사")) {
					gam1.Gambling1(pc, chatText, 1);
					return;
				}else if (chatText.startsWith("스파토이")) {
					gam1.Gambling1(pc, chatText, 2);
					return;
				}else if (chatText.startsWith("멧돼지")) {
					gam1.Gambling1(pc, chatText, 3);
					return;
				}else if (chatText.startsWith("슬라임")) {
					gam1.Gambling1(pc, chatText, 4);
					return;
				}else if (chatText.startsWith("해골")) {
					gam1.Gambling1(pc, chatText, 5);
					return;
				}else if (chatText.startsWith("늑대인간")) {
					gam1.Gambling1(pc, chatText, 6);
					return;
				}else if (chatText.startsWith("버그베어")) {
					gam1.Gambling1(pc, chatText, 7);
					return;
				}else if (chatText.startsWith("장로")) {
					gam1.Gambling1(pc, chatText, 8);
					return;
				}else if (chatText.startsWith("괴물눈")) {
					gam1.Gambling1(pc, chatText, 9);
					return;
				} else if (chatText.startsWith("난쟁이")) {
					gam1.Gambling1(pc, chatText, 10);
					return;
				}else if (chatText.startsWith("오크")) {
					gam1.Gambling1(pc, chatText, 11);
					return;
				}else if (chatText.startsWith("라이칸")) {
					gam1.Gambling1(pc, chatText, 12);
					return;
				}else if (chatText.startsWith("개구리")) {
					gam1.Gambling1(pc, chatText, 13);
					return;
				}else if (chatText.startsWith("늑대")) {
					gam1.Gambling1(pc, chatText, 14);
					return;
				}else if (chatText.startsWith("가스트")) {
					gam1.Gambling1(pc, chatText, 15);
					return;
				}else if (chatText.startsWith("좀비")) {
					gam1.Gambling1(pc, chatText, 16);
					return;
				}else if (chatText.startsWith("리자드맨")) {
					gam1.Gambling1(pc, chatText, 17);
					return;
				}else if (chatText.startsWith("도베르만")) {
					gam1.Gambling1(pc, chatText, 18);
					return;
				}
			}

			if(pc.isGambling3()){ // 소막
				Akduk1GameSystem gam1 = new Akduk1GameSystem();
				if (chatText.startsWith("오크전사")) {
					gam1.Gambling3(pc, chatText, 1);
					return;
				}else if (chatText.startsWith("스파토이")) {
					gam1.Gambling3(pc, chatText, 2);
					return;
				}else if (chatText.startsWith("멧돼지")) {
					gam1.Gambling3(pc, chatText, 3);
					return;
				}else if (chatText.startsWith("슬라임")) {
					gam1.Gambling3(pc, chatText, 4);
					return;
				}else if (chatText.startsWith("해골")) {
					gam1.Gambling3(pc, chatText, 5);
					return;
				}else if (chatText.startsWith("늑대인간")) {
					gam1.Gambling3(pc, chatText, 6);
					return;
				}else if (chatText.startsWith("버그베어")) {
					gam1.Gambling3(pc, chatText, 7);
					return;
				}else if (chatText.startsWith("장로")) {
					gam1.Gambling3(pc, chatText, 8);
					return;
				}else if (chatText.startsWith("괴물눈")) {
					gam1.Gambling3(pc, chatText, 9);
					return;
				}
			}
			/** 주사위 소막 */
			if(pc.isGambling4()){
				Akduk4GameSystem gam2 = new Akduk4GameSystem();
				if (chatText.startsWith("찌")) {
					gam2.Gambling4(pc, chatText, 1);
					return;
				}else if (chatText.startsWith("묵")) {
					gam2.Gambling4(pc, chatText, 2);
					return;
				}else if (chatText.startsWith("빠")) {
					gam2.Gambling4(pc, chatText, 3);
					return;   
				}
			}
			/** 묵 찌 빠 */

			// 로또 입력 시간이라면
			if(pc.isLotto()){
				try{
					StringTokenizer s = new StringTokenizer(chatText, " ");
					int[] number = new int[6];
					String[] date = new String[2];
					number[0] = Integer.parseInt(s.nextToken());
					number[1] = Integer.parseInt(s.nextToken());
					number[2] = Integer.parseInt(s.nextToken());
					number[3] = Integer.parseInt(s.nextToken());
					number[4] = Integer.parseInt(s.nextToken());
					number[5] = Integer.parseInt(s.nextToken());
					// 입력한 숫자가 1~45까지 라면 입력
					if(isNumber(number[0]) && isNumber(number[1]) && isNumber(number[2]) && isNumber(number[3]) 
							&& isNumber(number[4]) && isNumber(number[5])){
						date = Lotto.getInstance().getLottoDate();;
						L1ItemInstance item = new L1ItemInstance();
						item.setId(ObjectIdFactory.getInstance().nextId());
						// 초과 돼지 않았다면
						if(!Lotto.getInstance().OverBuy(pc, date[0])){
							L1EtcItem lotto = new L1EtcItem();
							lotto.setType2(0);
							lotto.setItemId(500016);
							String name = "로또[수동] "+date[0]+"회차   추첨일 : "+date[1]+"    "+number[0]+" "+number[1]+" "+number[2]+" "+number[3]+" " +number[4]+" "+number[5];
							lotto.setName(name);
							lotto.setNameId(name);
							lotto.setType(8);
							lotto.setType1(8);
							lotto.setMaterial(5);
							lotto.setWeight(30);
							lotto.setGfxId(2275);
							lotto.setGroundGfxId(151);
							lotto.setMinLevel(0);
							lotto.setMaxLevel(0);
							lotto.setBless(1);
							lotto.setTradable(false);
							lotto.setDmgSmall(0);
							lotto.setDmgLarge(0);
							lotto.set_stackable(false);
							lotto.set_locx(0);
							lotto.set_locy(0);
							lotto.set_mapid((short)0);
							lotto.set_delayid(2);
							lotto.set_delaytime(10);
							lotto.set_delayEffect(0);
							Lotto.getInstance().BuyPc(pc, date[0], 0, item.getId(), number);
							item.setItem(lotto);
							item.set_durability(item.get_durability());
							L1World.getInstance().storeObject(item);
							item.setIdentified(true);
							pc.getInventory().storeItem(item);
							pc.sendPackets(new S_SystemMessage("로또 구매가 완료되었습니다."));
							pc.sendPackets(new S_SystemMessage("구매한 로또를 버리시거나 삭제하시면 무효처리됩니다."));
						}else{
							Lotto.getInstance().AddPrice(-10000, date[0]);
							pc.getInventory().storeItem(40308, 10000);
							pc.sendPackets(new S_SystemMessage("한 회차당 구입가능한 갯수는 20개입니다."));
							pc.sendPackets(new S_SystemMessage((Integer.parseInt(date[0])+1) + "회차를 이용해주세요."));
						}
						pc.setLotto(false);
					}else{
						pc.sendPackets(new S_SystemMessage("1~45 까지 번호만 입력해주세요.")); 
					}
				}catch(Exception e){
					pc.sendPackets(new S_SystemMessage("1 12 24 35 32 40처럼 한칸씩 띄어주세요.")); 
					return;
				}
			}
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_NORMALCHAT, 0);
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
						Broadcaster.broadcastPacket(mob, new S_NpcChatPacket(mob, chatText, 0));
					}
				}
			}
			eva.writeMessage(1, pc.getName() + " : "+ chatText);
		}
		break;
		case 2 :{
			if (pc.isGhost()) {
				return;
			}
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_NORMALCHAT, 2);
			if (!pc.getExcludingList().contains(pc.getName())) {
				pc.sendPackets(s_chatpacket);
			}
			for (L1PcInstance listner : L1World.getInstance().getVisiblePlayer(pc, 50)) {
				if (!listner.getExcludingList().contains(pc.getName())) {
					listner.sendPackets(s_chatpacket);
				}
			}
			eva.writeMessage(1, pc.getName() + " : "+ chatText);
			// 돕펠 처리
			L1MonsterInstance mob = null;
			for (L1Object obj : pc.getNearObjects().getKnownObjects()) {
				if (obj instanceof L1MonsterInstance) {
					mob = (L1MonsterInstance) obj;
					if (mob.getNpcTemplate().is_doppel() && mob.getName().equals(pc.getName())) {
						for (L1PcInstance listner : L1World.getInstance().getVisiblePlayer(mob, 50)) {
							listner.sendPackets(new S_NpcChatPacket(mob, chatText, 2));
						}
					}
				}
			}
		}
		break;

		case 3 :{ chatWorld(pc, chatText, chatType); }break;
		case 4 : {
			if (pc.getClanid() != 0) { // 크란 소속중
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if (clan != null && (rank == L1Clan.CLAN_RANK_PUBLIC
						|| rank == L1Clan.CLAN_RANK_GUARDIAN || rank == L1Clan.CLAN_RANK_PRINCE)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_MSG, 4);

					// monitoring
					ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_CLAN, chatText, pc);
					eva.writeMessage(3, pc.getName() + "["+ pc.getClanname() + "] : " + chatText);						
					for (L1PcInstance listner : clan.getOnlineClanMember()) {
						if (!listner.getExcludingList().contains(pc.getName())) {
							listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		}
		break;
		case 11 : {
			if (pc.isInParty()) { // 파티중
				ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_MSG, 11);
				for (L1PcInstance listner : pc.getParty().getMembers()) {
					if (!listner.getExcludingList().contains(pc.getName())) {
						listner.sendPackets(s_chatpacket);
					}
				}
				//					monitoring
				ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_PARTY, chatText, pc);
			}
			eva.writeMessage(4, pc.getName() + " : "+ chatText);	
		}
		break;
		case 12 : { chatWorld(pc, chatText, chatType); } break;
		case 13 : { // 연합 채팅
			if (pc.getClanid() != 0) { // 혈맹 소속중
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if (clan != null && (rank == L1Clan.CLAN_RANK_GUARDIAN || rank == L1Clan.CLAN_RANK_PRINCE)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_MSG, 4);
					for (L1PcInstance listner : clan.getOnlineClanMember()) {
						int listnerRank = listner.getClanRank();
						if (!listner.getExcludingList().contains(pc.getName()) 
								&& (listnerRank == L1Clan.CLAN_RANK_GUARDIAN || listnerRank == L1Clan.CLAN_RANK_PRINCE)) {
							listner.sendPackets(s_chatpacket);
						}
					}
				}
				// monitoring
				ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_CLAN, chatText, pc);
			}
			eva.writeMessage(4, pc.getName() + " : "+ chatText);
		}
		break;
		case 14 : { // 채팅 파티
			if (pc.isInChatParty()) { // 채팅 파티중
				ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_NORMALCHAT, 14);
				for (L1PcInstance listner : pc.getChatParty().getMembers()) {
					if (!listner.getExcludingList().contains(pc.getName())) {
						listner.sendPackets(s_chatpacket);
					}
				}
			}
			// monitoring
			ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_PARTY, chatText, pc);
			eva.writeMessage(5, pc.getName() + " : " + chatText);
		}
		break;
		case 15 :
			if (pc.getClan() != null && pc.getClan().getAlliance() != null) {
				pc.getClan().getAlliance().AllianceChat(pc, chatText);
			}
			break;
		}
		if (!pc.isGm()) {
			pc.checkChatInterval();
		}
	}



	private void chatWorld(L1PcInstance pc, String chatText, int chatType) {
		if (pc.isGm() || pc.getAccessLevel() == 300) {
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			L1World.getInstance().broadcastPacketToAll(new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_MSG, chatType));
			eva.writeMessage(2, pc.getName() +" : "+ chatText);
		} else if (pc.getLevel() >= Config.GLOBAL_CHAT_LEVEL) {
			if (L1World.getInstance().isWorldChatElabled()) {
				if (pc.get_food() >= 12) { //5%겟지?
					ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
					pc.sendPackets(new S_PacketBox(S_PacketBox.FOOD, pc.get_food()));					
					eva.writeMessage(2, pc.getName() +" : "+ chatText);
					for (L1PcInstance listner : L1World.getInstance().getAllPlayers()) {
						if (!listner.getExcludingList().contains(pc.getName())) {
							if (listner.isShowTradeChat() && chatType == 12) {
								listner.sendPackets(new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_MSG, chatType));
							} else if (listner.isShowWorldChat() && chatType == 3) {
								listner.sendPackets(new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_MSG, chatType));
							}
						}
					}
				} else {
					pc.sendPackets(new S_ServerMessage(462));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(510)); 
			}
		} else {
			pc.sendPackets(new S_ServerMessage(195, String.valueOf(Config.GLOBAL_CHAT_LEVEL))); 
		}
	}

	private boolean isNumber(int number){
		if (number >= 1 && 45 >= number){
			return true;
		}
		return false;
	}
	@Override
	public String getType() {
		return C_CHAT;
	}
}
