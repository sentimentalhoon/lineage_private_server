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
				// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
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
			 if (pc.getLevel() < 99) { // ������ ��� ���� ���� ����
	                pc.sendPackets(new S_SystemMessage("\\fY�������� ���ٴڿ� ���� �� �����ϴ�."));
					return;
				 }
			 /*if (item.getEnchantLevel() >= 1){  // ��æƮ�� �������� �ٴڿ� ��Ӹ���Ű��
				 pc.sendPackets(new S_SystemMessage("��þƮ�� �������� �����������ϴ�.."));
				return;
				 }*/
			/* if (!pc.isGm() && item.getItem().getItemId() == 40308 )  {
		            pc.sendPackets(new S_SystemMessage("�Ƶ����� ���� �� �����ϴ�."));
		            return;
		         }  */

			
			if(item.getBless() >= 128){
				pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); // ������.
				return;
			}

			Object[] petlist = pc.getPetList().values().toArray();
			L1PetInstance pet = null;
			for (Object petObject : petlist) {
				if (petObject instanceof L1PetInstance) {
					pet = (L1PetInstance) petObject;
					if (item.getId() == pet.getItemObjId()) {
						// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
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
				// \f1������ �� ���� �������̳� ��� �ϰ� �ִ� �������� ���� �� �����ϴ�.
				pc.sendPackets(new S_ServerMessage(125));
				return;
			}
			eva.writeMessage(-4, "[���]" + pc.getName() + ":" + item.getName() + "/" + count);
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
}
