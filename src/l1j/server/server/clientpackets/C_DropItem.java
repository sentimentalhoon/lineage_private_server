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

import l1j.server.Config;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ServerMessage; 
import l1j.server.server.serverpackets.S_SystemMessage;
import server.LineageClient;
import server.manager.eva;

public class C_DropItem extends ClientBasePacket {

	private static final String C_DROP_ITEM = "[C] C_DropItem";

	public C_DropItem(byte[] decrypt, LineageClient client) throws Exception {
		super(decrypt);
		int x = readH();
		int y = readH();
		int objectId = readD();
		int count = readD();

		L1PcInstance pc = client.getActiveChar();

		if (pc == null) {return;}
		if (pc.getOnlineStatus() != 1) {
			pc.sendPackets(new S_Disconnect());
			return;
		}
		if (pc.isGhost()) return;
		if (isTwoLogin(pc)) return;

		L1ItemInstance item = pc.getInventory().getItem(objectId);
		if (item != null) {
			if (!item.getItem().isTradable()) {
				// \f1%0은 버리거나 또는 타인에게 양일을 할 수 없습니다.
				pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
				return;
			}
			
			if (!item.isStackable() && count != 1) {	
				pc.sendPackets(new S_Disconnect());
				return;
			}
			if (item.getCount() <= 0) {
				pc.sendPackets(new S_Disconnect());
				return;
			}
			if (count <= 0 || count > 2000000000) {
				pc.sendPackets(new S_Disconnect());
				return;
			}
			if (count > item.getCount()) {
				count = item.getCount();
				pc.sendPackets(new S_Disconnect());
				return;
			}
			/*if (pc.getAccessLevel() == Config.GMCODE){
				return;
			}*/
			if (pc.getMapId() == 340 || pc.getMapId() == 350 || pc.getMapId() == 360 || pc.getMapId() == 370){				
				return;
			}
			 if (pc.getLevel() < 99) { // 아이템 드롭 가능 레벨 설정
	                pc.sendPackets(new S_SystemMessage("\\fY아이템은 땅바닥에 버릴 수 없습니다."));
					return;
				 }
			 /*if (item.getEnchantLevel() >= 1){  // 인챈트된 아이템은 바닥에 드롭못시키게
				 pc.sendPackets(new S_SystemMessage("인첸트된 아이템은 버릴수없습니다.."));
				return;
				 }*/
			/* if (!pc.isGm() && item.getItem().getItemId() == 40308 )  {
		            pc.sendPackets(new S_SystemMessage("아데나를 버릴 수 없습니다."));
		            return;
		         }  */

			
			if(item.getBless() >= 128){
				pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); // 봉인템.
				return;
			}

			Object[] petlist = pc.getPetList().values().toArray();
			L1PetInstance pet = null;
			for (Object petObject : petlist) {
				if (petObject instanceof L1PetInstance) {
					pet = (L1PetInstance) petObject;
					if (item.getId() == pet.getItemObjId()) {
						// \f1%0은 버리거나 또는 타인에게 양일을 할 수 없습니다.
						pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
						return;
					}
				}
			}
			
			Object[] dollList = pc.getDollList().values().toArray();
			for (Object dollObject : dollList) {
				L1DollInstance doll = (L1DollInstance) dollObject;
				if (doll.getItemObjId() == item.getId()) {
					return;
				}
			}

			if (item.isEquipped()) {
				// \f1삭제할 수 없는 아이템이나 장비 하고 있는 아이템은 버릴 수 없습니다.
				pc.sendPackets(new S_ServerMessage(125));
				return;
			}
			eva.writeMessage(-4, "[드랍]" + pc.getName() + ":" + item.getName() + "/" + count);
			pc.getInventory().tradeItem(item, count, L1World.getInstance().getInventory(x, y, pc.getMapId()));
			pc.getLight().turnOnOffLight();			
		}
	}

	@Override
	public String getType() {
		return C_DROP_ITEM;
	}

	private boolean isTwoLogin(L1PcInstance c) {
		boolean bool = false;
		for(L1PcInstance target : L1World.getInstance().getAllPlayersToArray()){
			// 무인PC 는 제외
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
}
