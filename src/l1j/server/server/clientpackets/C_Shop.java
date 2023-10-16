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

import java.util.ArrayList;

import server.LineageClient;
import server.message.ServerMessage;
//import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_DoActionShop;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1PrivateShopBuyList;
import l1j.server.server.templates.L1PrivateShopSellList;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Shop extends ClientBasePacket {

	private static final String C_SHOP = "[C] C_Shop";

	public C_Shop(byte abyte0[], LineageClient clientthread) {
		super(abyte0);

		L1PcInstance pc = clientthread.getActiveChar();
		if (pc.getAccessLevel() == Config.GMCODE){return;}
		if (pc.isGhost()) {	
			pc.sendPackets(new S_ServerMessage(755)); /// ���� ���¿����� ������ ��ȯ�� �Ͻ� �� �����ϴ�.
			return; }
		if (pc.isInvisble()) {
			pc.sendPackets(new S_ServerMessage(755)); // ���� ���¿����� ������ ��ȯ�� �Ͻ� �� �����ϴ�.
			return;
		}
		int mapId = pc.getMapId();
		if (mapId != 340 && mapId != 350 && mapId != 360 && mapId != 370) {
			pc.sendPackets(new S_ServerMessage(876)); // �� �������� ���� ������ �� �� �����ϴ�.
			return;
		}
		if (pc.getGfxId().getTempCharGfx() != pc.getClassId() &&
				pc.getSkillEffectTimerSet().getSkillEffectTimeSec(L1SkillId.SHAPE_CHANGE) <=0){
			pc.sendPackets(new S_SystemMessage("���� �������� ���� �� �ֽñ� �ٶ��ϴ�."));
			return;
		}
		if (pc.getLevel() > 75) {
			pc.sendPackets(new S_SystemMessage("75�������� �� �����մϴ�."));
		    return;
		}
		ArrayList<L1PrivateShopSellList> sellList = pc.getSellList();
		ArrayList<L1PrivateShopBuyList> buyList = pc.getBuyList();
		L1ItemInstance checkItem;
		boolean tradable = true;

		int type = readC();
		if (type == 0) { // ����
			int sellTotalCount = readH();
			int sellObjectId;
			int sellPrice;
			int sellCount;
			Object[] petlist = null;
			for (int i = 0; i < sellTotalCount; i++) {
				sellObjectId = readD();
				sellPrice = readD();
				sellCount = readD();

				/** ���λ��� ���� ���� */
				if(sellTotalCount > 7){ 
					pc.sendPackets(new S_SystemMessage("��ǰ����� 7�������� �����մϴ�.")); 
					return;
				}

				// �ŷ� ������ �������̳� üũ
				checkItem = pc.getInventory().getItem(sellObjectId);
				if (sellObjectId != checkItem.getId()) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if (!checkItem.isStackable() && sellCount != 1) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if (sellCount > checkItem.getCount()) {
					sellCount = checkItem.getCount();
				}
				if (checkItem.getCount() < sellCount || checkItem.getCount() <= 0 || sellCount <= 0) {
					sellList.clear();  
					buyList.clear();				     
					return;
				}
				
				if(checkItem.getBless() >= 128){
					pc.sendPackets(new S_ServerMessage(ServerMessage.CANNOT_DROP_OR_TRADE, checkItem.getItem().getName())); 
					return;
				}
				if (!checkItem.getItem().isTradable()) {
					tradable = false;
					pc.sendPackets(new S_ServerMessage(166, checkItem.getItem().getName(), "�ŷ� �Ұ����մϴ�. "));
				}
		/*		
				if ((checkItem.getItem().getType2() == 1 || checkItem.getItem().getType2() == 2) 
						&& pc.getMapId() != 350 && pc.getMapId() != 360) {
					sellList.clear();
					buyList.clear();
					pc.setPrivateShop(false);
					pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
					Broadcaster.broadcastPacket(pc, new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
					pc.sendPackets(new S_SystemMessage("��� ���忡���� �����մϴ�."));
					return;
				}
				if (checkItem.getItem().getType2() == 0 && pc.getMapId() != 360 && pc.getMapId() != 350) {
					sellList.clear();
					buyList.clear();
					pc.setPrivateShop(false);
					pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
					Broadcaster.broadcastPacket(pc, new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
					pc.sendPackets(new S_SystemMessage("���۷��� ���� ���忡���� �����մϴ�."));
					return;
				}*/

				petlist = pc.getPetList().values().toArray();
				for (Object petObject : petlist) {
					if (petObject instanceof L1PetInstance) {
						L1PetInstance pet = (L1PetInstance) petObject;
						if (checkItem.getId() == pet.getItemObjId()) {
							tradable = false;
							pc.sendPackets(new S_ServerMessage(166, checkItem.getItem().getName(), "�ŷ� �Ұ����մϴ�. "));
							break;
						}
					}
				}
				L1PrivateShopSellList pssl = new L1PrivateShopSellList();
				pssl.setItemObjectId(sellObjectId);
				pssl.setSellPrice(sellPrice);
				pssl.setSellTotalCount(sellCount);
				sellList.add(pssl);
			}
			int buyTotalCount = readH();
			int buyObjectId;
			int buyPrice;
			int buyCount;
			for (int i = 0; i < buyTotalCount; i++) {
				buyObjectId = readD();
				buyPrice = readD();
				buyCount = readD();
				
				if(buyTotalCount > 7){ 
					pc.sendPackets(new S_SystemMessage("��ǰ����� 7�������� �����մϴ�.")); 
					return;
				}

				checkItem = pc.getInventory().getItem(buyObjectId);

				if (buyObjectId != checkItem.getId()) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if (!checkItem.isStackable() && buyCount != 1) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if (buyCount <= 0 || checkItem.getCount() <= 0) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if (buyCount > checkItem.getCount()) {
					buyCount = checkItem.getCount();
				}

				if (!checkItem.getItem().isTradable()) {
					tradable = false;
					pc.sendPackets(new S_ServerMessage(166, checkItem.getItem().getName(), "�ŷ� �Ұ����մϴ�. "));
				}
				petlist = pc.getPetList().values().toArray();
				for (Object petObject : petlist) {
					if (petObject instanceof L1PetInstance) {
						L1PetInstance pet = (L1PetInstance) petObject;
						if (checkItem.getId() == pet.getItemObjId()) {
							tradable = false;
							pc.sendPackets(new S_ServerMessage(166, checkItem.getItem().getName(), "�ŷ� �Ұ����մϴ�. "));
							break;
						}
					}
				}
				L1PrivateShopBuyList psbl = new L1PrivateShopBuyList();
				psbl.setItemObjectId(buyObjectId);
				psbl.setBuyPrice(buyPrice);
				psbl.setBuyTotalCount(buyCount);
				buyList.add(psbl);
			}
			if (sellTotalCount == 0 && buyTotalCount == 0){
				pc.sendPackets(new S_ServerMessage(908));
				pc.setPrivateShop(false);
				pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
				Broadcaster.broadcastPacket(pc, new S_DoActionGFX(pc.getId(),ActionCodes.ACTION_Idle));
				return;
			}
			if (!tradable) { // �ŷ� �Ұ����� �������� ���ԵǾ� �ִ� ���, ���� ���� ����
				sellList.clear();
				buyList.clear();
				pc.setPrivateShop(false);
				pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
				Broadcaster.broadcastPacket(pc, new S_DoActionGFX(pc.getId(),ActionCodes.ACTION_Idle));
				return;
			}
			
			// ���� ���嶧����..
			//byte[] chat = readByte();
			//pc.setShopChat(chat);
			
			String ment1 = readS2();
			String ment2 = readS();
			pc.setPrivateShop(true);
			pc.set_ment1(ment1);
			pc.set_ment2(ment2);
			
			//pc.sendPackets(new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, chat));
			//Broadcaster.broadcastPacket(pc, new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, chat));
			
			pc.sendPackets(new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, pc.get_ment1(), pc.get_ment2()));
			Broadcaster.broadcastPacket(pc, new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, pc.get_ment1(), pc.get_ment2()));
		} else if (type == 1) { // ����
			if(isTwoLogin(pc)){
				pc.sendPackets(new S_Disconnect());			
			}
			sellList.clear();
			buyList.clear();
			pc.setPrivateShop(false);
			pc.set_ment1(null);
			pc.set_ment2(null);
			pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
			Broadcaster.broadcastPacket(pc, new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
		}
	}
	
	private boolean isTwoLogin(L1PcInstance c) {
		boolean bool = false;
		for(L1PcInstance target : L1World.getInstance().getAllPlayersToArray()){
			// ����PC �� ����
			if(target.noPlayerCK) continue;
			//
			if(c.getId() != target.getId() && !target.isPrivateShop()){
				if(c.getNetConnection().getAccountName().equalsIgnoreCase(target.getNetConnection().getAccountName())) {
					bool = true;
					break;
				}
			}
		}
		return bool;
	}
	
	@Override
	public String getType() {
		return C_SHOP;
	}
}
