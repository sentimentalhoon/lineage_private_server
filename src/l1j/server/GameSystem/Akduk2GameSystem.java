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
package l1j.server.GameSystem;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ChatLogTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1BuffNpcInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;

public class Akduk2GameSystem {
	private static final Logger _log = Logger.getLogger(Akduk2GameSystem.class.getName());

	public void Gambling(L1PcInstance player, int bettingmoney){
		try {
			NpcMSG();
			String chat = player.getName()+"님 "+ bettingmoney + "원 배팅하셨습니다.";
			player.sendPackets(new S_NpcChatPacket(dealer, chat, 0));
			Broadcaster.broadcastPacket(player, new S_NpcChatPacket(dealer, chat, 0));
			Thread.sleep(3000);
			String chat2 = "홀 or 짝 1.5배//// 1 ~ 6 숫자 2배///홀짝or숫자를 입력해주세요";
			player.sendPackets(new S_NpcChatPacket(dealer, chat2, 0));
			Broadcaster.broadcastPacket(player, new S_NpcChatPacket(dealer, chat2, 0));
			player.setGamblingMoney(bettingmoney);
			player.setGambling(true);
		}catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void Gambling2(L1PcInstance pc, String chatText, int type){
		ChatLogTable.getInstance().storeChat(pc, null, chatText, 0);
		S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,(Opcodes.S_OPCODE_NORMALCHAT), 0);
		if (!pc.getExcludingList(). contains(pc.getName())) {
			pc.sendPackets(s_chatpacket);
		}
		for (L1PcInstance listner : L1World
				.getInstance().getRecognizePlayer(pc)) {
			if (!listner.getExcludingList(). contains(pc.getName())) {
				listner.sendPackets(s_chatpacket);
			}
		}
		Random random = new Random();
		try {
			NpcMSG();
			int gfxid = 3204 + random.nextInt(6);
			GamebleResult(pc, gfxid, type);
			pc.setGamblingMoney(0);
			pc.setGamblingMoney1(0);
			pc.setGamblingMoney3(0);
			pc.setGamblingMoney4(0);
			pc.setGambling(false);
			pc.setGambling1(false);
			pc.setGambling3(false);
			pc.setGambling4(false);
		}catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}
	private final static String Name[] = new String[] { "Blank", "홀", "짝", "1", "2", "3", "4", "5", "6"};
	private void GamebleResult(L1PcInstance pc, int gfxid, int type){
		int GMoney15 = (int) (pc.getGamblingMoney() * 1.5);
		int GMoney20 = (pc.getGamblingMoney() * 2);
		String chat = pc.getName()+"님 [" + Name[type] + "]을 선택하셨습니다. 멀리가시면 게임이 취소됩니다.";
		String chat9 = pc.getName()+"님 맞추셧습니다. "+ GMoney15 +"원 입금했습니다.";
		String chat10 = pc.getName()+"님 맞추셧습니다." + GMoney20 + "원 입금했습니다.";
		String chat11 = pc.getName()+"님 틀리셧습니다.";

		try{
			Thread.sleep(2000);
			pc.sendPackets(new S_NpcChatPacket(dealer, chat, 0));
			Broadcaster.broadcastPacket(pc, new S_NpcChatPacket(dealer, chat, 0));
			Thread.sleep(1000);
			pc.sendPackets(new S_SkillSound(dealer.getId(), gfxid));
			Broadcaster.broadcastPacket(pc, new S_SkillSound(dealer.getId(), gfxid));
			Thread.sleep(3000);
			if(GambleResultBoolean(type, gfxid)){
				if (type == 1 || type == 2){
					pc.sendPackets(new S_NpcChatPacket(dealer, chat9, 0));
					Broadcaster.broadcastPacket(pc, new S_NpcChatPacket(dealer, chat9, 0));
					pc.getInventory().storeItem(L1ItemId.ADENA, GMoney15);
					String doubleMoney = String.valueOf(GMoney15);
					pc.sendPackets(new S_ServerMessage(143, dealer.getName(), "$4" + "(" +  doubleMoney + ")")); // \f1%0이%1를 주었습니다.
				} else {
					pc.sendPackets(new S_NpcChatPacket(dealer, chat10, 0));
					Broadcaster.broadcastPacket(pc, new S_NpcChatPacket(dealer, chat10, 0));
					pc.getInventory().storeItem(L1ItemId.ADENA, GMoney20);
					String doubleMoney = String.valueOf(GMoney20);
					pc.sendPackets(new S_ServerMessage(143, dealer.getName(), "$4" + "(" +  doubleMoney + ")")); // \f1%0이%1를 주었습니다.
				}
			}else{
				pc.sendPackets(new S_NpcChatPacket(dealer, chat11, 0));
				Broadcaster.broadcastPacket(pc, new S_NpcChatPacket(dealer, chat11, 0));
			}
		} catch (Exception e) {}
	}

	private boolean GambleResultBoolean(int type, int gfxid){
		switch(type){
		case 1: if(gfxid == 3204 ||gfxid == 3206 ||gfxid == 3208) return true; else return false;
		case 2: if(gfxid == 3205 ||gfxid == 3207 ||gfxid == 3209) return true; else return false; 
		case 3: if(gfxid == 3204) return true;  else return false;
		case 4: if(gfxid == 3205) return true;  else return false;
		case 5: if(gfxid == 3206) return true;  else return false;
		case 6: if(gfxid == 3207) return true;  else return false;
		case 7: if(gfxid == 3208) return true;  else return false;
		case 8: if(gfxid == 3209) return true;  else return false;
		}
		return false;
	}

	L1NpcInstance dealer = null;

	private void NpcMSG(){
		L1NpcInstance npc = null;
		for (L1Object obj : L1World.getInstance().getObject()) {
			if(obj instanceof L1NpcInstance){
				npc = (L1NpcInstance)obj;
				if(npc.getNpcId() == 7000073){
					dealer = npc;
				}
			}
		}
	}
}