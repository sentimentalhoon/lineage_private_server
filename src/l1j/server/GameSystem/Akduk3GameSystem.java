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

import l1j.server.server.ActionCodes;
import l1j.server.server.Opcodes;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1EffectSpawn;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_ServerMessage;

public class Akduk3GameSystem {
	private static final Logger _log = Logger.getLogger(Akduk3GameSystem.class
			.getName());

	private final static String mobName[] = new String[] { "Blank", "오크전사", "스파토이", "멧돼지", "슬라임", "해골", "늑대인간", "버그베어", "장로", "괴물눈",
		"난쟁이", "오크", "라이칸", "개구리", "늑대", "가스트", "좀비", "리자드맨", "도베르만"};

	public void Gambling(L1PcInstance player, int bettingmoney){
		try {
			NpcMSG(); //엔피씨 메시지를 설정
			String chat = player.getName()+"님 "+ bettingmoney + "아덴 배팅하셨어요~ 맞으면 2배 입니다~!";
			player.sendPackets(new S_NpcChatPacket(dealer, chat, 0));
			Broadcaster.broadcastPacket(player, new S_NpcChatPacket(dealer, chat, 0));
			Thread.sleep(2000);
			String chat2 = "배팅할 몹이름을 말해주세요~!(버그베어,장로,멧돼지,스파토이,슬라임,해골,늑대인간,괴물눈,오크전사)";
			player.sendPackets(new S_NpcChatPacket(dealer, chat2, 0));
			Broadcaster.broadcastPacket(player, new S_NpcChatPacket(dealer, chat2, 0));
			Thread.sleep(2000);
			String chat3 = "배팅할 몹이름을 말해주세요~!(난쟁이,오크,라이칸,개구리,늑대,가스트,좀비,리자드맨,도베르만,)";
			player.sendPackets(new S_NpcChatPacket(dealer, chat3, 0));
			Broadcaster.broadcastPacket(player, new S_NpcChatPacket(dealer, chat3, 0));
			player.setGamblingMoney1(bettingmoney);
			player.setGambling1(true);
		}catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void Gambling1(L1PcInstance pc, String chatText, int type){
		//ChatLogTable.getInstance().storeChat(pc, null, chatText, 0);
		S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
				Opcodes.S_OPCODE_NORMALCHAT, 0);
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
			NpcMSG(); //엔피씨 메시지를 설정

			int mobid1 = 81245 + random.nextInt(18);
			int mobid2 = 81245 + random.nextInt(18);
			int mobid3 = 81245 + random.nextInt(18);
			int mobid4 = 81245 + random.nextInt(18);
			int mobid5 = 81245 + random.nextInt(18);

			int x = dealer.getX();
			int y = dealer.getY() + 2;
			short mapid = dealer.getMapId();
			int spawntime = 3500 ;

			GamebleResult(pc, mobid1, mobid2, mobid3, mobid4, mobid5, type, spawntime, x, y, mapid);

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
	private void GamebleResult(L1PcInstance pc, int mobid1, int mobid2, int mobid3, int mobid4, int mobid5,
			int type, int spawntime, int x, int y, short mapid){
		String chat7 = mobName[type] + "에 배팅합니다~ 멀리가시면 게임이 취소됩니다!";
		String chat8 = "과연? ㅋㅋㅋ";
		String chat9 = "오!굿... 축하드려요... 배당금 지급 해 드렸습니다...";
		String chat11 = "아쉽군요ㅋㅋ 다음기회에 도전해주세요~!";
		int GMoney20 = (pc.getGamblingMoney1() * 2);
		try{
			Thread.sleep(1000);
			pc.sendPackets(new S_NpcChatPacket(dealer, chat7, 0));
			Broadcaster.broadcastPacket(pc,new S_NpcChatPacket(dealer, chat7, 0));
			Thread.sleep(1000);
			pc.sendPackets(new S_NpcChatPacket(dealer, chat8, 0));
			Broadcaster.broadcastPacket(pc,new S_NpcChatPacket(dealer, chat8, 0));
			pc.sendPackets(new S_DoActionGFX(dealer2.getId(), ActionCodes.ACTION_Wand));
			Broadcaster.broadcastPacket(pc,new S_DoActionGFX(dealer2.getId(), ActionCodes.ACTION_Wand));

			L1EffectSpawn.getInstance().spawnEffect(mobid1, spawntime, x, y, mapid);
			L1EffectSpawn.getInstance().spawnEffect(mobid2, spawntime, x - 1, y, mapid);
			L1EffectSpawn.getInstance().spawnEffect(mobid3, spawntime, x - 2, y, mapid);
			L1EffectSpawn.getInstance().spawnEffect(mobid4, spawntime, x - 3, y, mapid);
			L1EffectSpawn.getInstance().spawnEffect(mobid5, spawntime, x - 4, y, mapid);

			Thread.sleep(1500);
			if(GambleResultBoolean(type, mobid1, mobid2, mobid3, mobid4, mobid5)){
				pc.sendPackets(new S_NpcChatPacket(dealer, chat9, 0));
				Broadcaster.broadcastPacket(pc,new S_NpcChatPacket(dealer, chat9, 0));
				pc.getInventory().storeItem(L1ItemId.ADENA, GMoney20);
				String doubleMoney = String.valueOf(GMoney20);
				pc.sendPackets(new S_ServerMessage(143, dealer.getName(), "$4" + "(" +  doubleMoney + ")")); // \f1%0이%1를 주었습니다.
			}else{
				pc.sendPackets(new S_NpcChatPacket(dealer, chat11, 0));
				Broadcaster.broadcastPacket(pc,new S_NpcChatPacket(dealer, chat11, 0));
			}
		} catch (Exception e) {}
	}

	private boolean GambleResultBoolean(int type, int mobid1, int mobid2, int mobid3, int mobid4, int mobid5){
		switch(type){
		case 1: if(mobid1 == 81245 || mobid2 == 81245 || mobid3 == 81245 || mobid4 == 81245 || mobid5 == 81245) return true;  else return false;
		case 2: if(mobid1 == 81246 || mobid2 == 81246 || mobid3 == 81246 || mobid4 == 81246 || mobid5 == 81246) return true;  else return false;
		case 3: if(mobid1 == 81247 || mobid2 == 81247 || mobid3 == 81247 || mobid4 == 81247 || mobid5 == 81247) return true;  else return false;
		case 4: if(mobid1 == 81248 || mobid2 == 81248 || mobid3 == 81248 || mobid4 == 81248 || mobid5 == 81248) return true;  else return false;
		case 5: if(mobid1 == 81249 || mobid2 == 81249 || mobid3 == 81249 || mobid4 == 81249 || mobid5 == 81249) return true;  else return false;
		case 6: if(mobid1 == 81250 || mobid2 == 81250 || mobid3 == 81250 || mobid4 == 81250 || mobid5 == 81250) return true;  else return false;
		case 7: if(mobid1 == 81251 || mobid2 == 81251 || mobid3 == 81251 || mobid4 == 81251 || mobid5 == 81251) return true;  else return false;
		case 8: if(mobid1 == 81252 || mobid2 == 81252 || mobid3 == 81252 || mobid4 == 81252 || mobid5 == 81252) return true;  else return false;
		case 9: if(mobid1 == 81253 || mobid2 == 81253 || mobid3 == 81253 || mobid4 == 81253 || mobid5 == 81253) return true;  else return false;
		case 10: if(mobid1 == 81254 || mobid2 == 81254 || mobid3 == 81254 || mobid4 == 81254 || mobid5 == 81254) return true;  else return false;
		case 11: if(mobid1 == 81255 || mobid2 == 81255 || mobid3 == 81255 || mobid4 == 81255 || mobid5 == 81255) return true;  else return false;
		case 12: if(mobid1 == 81256 || mobid2 == 81256 || mobid3 == 81256 || mobid4 == 81256 || mobid5 == 81256) return true;  else return false;
		case 13: if(mobid1 == 81257 || mobid2 == 81257 || mobid3 == 81257 || mobid4 == 81257 || mobid5 == 81257) return true;  else return false;
		case 14: if(mobid1 == 81258 || mobid2 == 81258 || mobid3 == 81258 || mobid4 == 81258 || mobid5 == 81258) return true;  else return false;
		case 15: if(mobid1 == 81259 || mobid2 == 81259 || mobid3 == 81259 || mobid4 == 81259 || mobid5 == 81259) return true;  else return false;
		case 16: if(mobid1 == 81260 || mobid2 == 81260 || mobid3 == 81260 || mobid4 == 81260 || mobid5 == 81260) return true;  else return false;
		case 17: if(mobid1 == 81261 || mobid2 == 81261 || mobid3 == 81261 || mobid4 == 81261 || mobid5 == 81261) return true;  else return false;
		case 18: if(mobid1 == 81262 || mobid2 == 81262 || mobid3 == 81262 || mobid4 == 81262 || mobid5 == 81262) return true;  else return false;
		}
		return false;
	}
	L1NpcInstance dealer = null;
	L1NpcInstance dealer2 = null;

	private void NpcMSG(){
		L1NpcInstance npc = null;
		for (L1Object obj : L1World.getInstance().getObject()) {
			if(obj instanceof L1NpcInstance){
				npc = (L1NpcInstance)obj;
				if(npc.getNpcId() == 7000076){
					dealer = npc;
				}
				if(npc.getNpcId() == 7000077){
					dealer2 = npc;
				}
			}
		}
	}
}
