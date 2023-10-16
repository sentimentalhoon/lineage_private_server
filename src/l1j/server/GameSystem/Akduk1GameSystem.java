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
import l1j.server.server.serverpackets.S_SkillSound;

public class Akduk1GameSystem {
	private static final Logger _log = Logger.getLogger(Akduk1GameSystem.class.getName());

	public void Gambling(L1PcInstance player, int bettingmoney){
		try {
			NpcMSG();
			String chat = player.getName()+"�� "+ bettingmoney + "�Ƶ� �����ϼ̾��~ ������ 2�� �Դϴ�~!";
			player.sendPackets(new S_NpcChatPacket(dealer, chat, 0));
			Broadcaster.broadcastPacket(player, new S_NpcChatPacket(dealer, chat, 0));
			Thread.sleep(2000);
			String chat2 = "������ ���̸��� �����ּ���~!(���׺���,���,�����,��������,������,�ذ�,�����ΰ�,������,��ũ����)";
			player.sendPackets(new S_NpcChatPacket(dealer, chat2, 0));
			Broadcaster.broadcastPacket(player, new S_NpcChatPacket(dealer, chat2, 0));
			player.setGamblingMoney3(bettingmoney);
			player.setGambling3(true);
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	private final static String mobName[] = new String[] { "Blank", "��ũ����", "��������", "�����", "������", "�ذ�", "�����ΰ�", "���׺���", "���", "������" };

	public void Gambling3(L1PcInstance pc, String chatText, int type){
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
			NpcMSG();

			int mobid1 = 81245 + random.nextInt(9);
			int mobid2 = 81245 + random.nextInt(9);
			int mobid3 = 81245 + random.nextInt(9);

			int x = dealer.getX();
			int y = dealer.getY() + 2;
			short mapid = dealer.getMapId();
			int spawntime = 3500 ;

			GamebleResult(pc, mobid1, mobid2, mobid3, type, spawntime, x, y, mapid);

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

	private void GamebleResult(L1PcInstance pc, int mobid1, int mobid2, int mobid3, int type, int spawntime, int x, int y, short mapid){
		String chat7 = mobName[type] + "�� �����մϴ�~ �ָ����ø� ������ ��ҵ˴ϴ�!";
		String chat8 = "����? ������";
		String chat9 = "��!��... ���ϵ����... ���� ���� �� ��Ƚ��ϴ�...";
		String chat11 = "�ƽ����䤻�� ������ȸ�� �������ּ���~!";
		int GMoney20 = (pc.getGamblingMoney3() * 2);
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

			Thread.sleep(1500);
			if(GambleResultBoolean(type, mobid1, mobid2, mobid3)){
				pc.sendPackets(new S_NpcChatPacket(dealer, chat9, 0));
				Broadcaster.broadcastPacket(pc,new S_NpcChatPacket(dealer, chat9, 0));
				pc.getInventory().storeItem(L1ItemId.ADENA, GMoney20);
				String doubleMoney = String.valueOf(GMoney20);
				pc.sendPackets(new S_ServerMessage(143, dealer.getName(), "$4" + "(" +  doubleMoney + ")")); // \f1%0��%1�� �־����ϴ�.
			}else{
				pc.sendPackets(new S_NpcChatPacket(dealer, chat11, 0));
				Broadcaster.broadcastPacket(pc,new S_NpcChatPacket(dealer, chat11, 0));
			}
		} catch (Exception e) {}
	}

	private boolean GambleResultBoolean(int type, int mobid1, int mobid2, int mobid3){
		switch(type){
		case 1: if(mobid1 == 81245 || mobid2 == 81245 || mobid3 == 81245) return true;  else return false;
		case 2: if(mobid1 == 81246 || mobid2 == 81246 || mobid3 == 81246) return true;  else return false;
		case 3: if(mobid1 == 81247 || mobid2 == 81247 || mobid3 == 81247) return true;  else return false;
		case 4: if(mobid1 == 81248 || mobid2 == 81248 || mobid3 == 81248) return true;  else return false;
		case 5: if(mobid1 == 81249 || mobid2 == 81249 || mobid3 == 81249) return true;  else return false;
		case 6: if(mobid1 == 81250 || mobid2 == 81250 || mobid3 == 81250) return true;  else return false;
		case 7: if(mobid1 == 81251 || mobid2 == 81251 || mobid3 == 81251) return true;  else return false;
		case 8: if(mobid1 == 81252 || mobid2 == 81252 || mobid3 == 81252) return true;  else return false;
		case 9: if(mobid1 == 81253 || mobid2 == 81253 || mobid3 == 81253) return true;  else return false;
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
				if(npc.getNpcId() == 7000074){
					dealer = npc;
				}
				if(npc.getNpcId() == 7000075){
					dealer2 = npc;
				}
			}
		}
	}
}
