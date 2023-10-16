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

package l1j.server.server.model.item.function;

import java.util.Random;

import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.ResolventTable;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class Resolvent extends L1ItemInstance{

	private static Random _random = new Random(System.nanoTime());

	public Resolvent(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			L1ItemInstance l1iteminstance1 = pc.getInventory().getItem(packet.readD());
			useResolvent(pc, l1iteminstance1, useItem);
		}
	}

	private void useResolvent(L1PcInstance pc, L1ItemInstance item, L1ItemInstance resolvent) {
		if (item == null || resolvent == null) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			return;
		}
		if (item.getItem().getType2() == 1 || item.getItem().getType2() == 2) { // ���⡤���� �ⱸ
			if (item.getEnchantLevel() != 0) { // ��ȭ�� ���� ����
				pc.sendPackets(new S_ServerMessage(1161)); // ������ �� �����ϴ�.
				return;
			}
			if (item.isEquipped()) { // �����
				pc.sendPackets(new S_ServerMessage(1161)); // ������ �� �����ϴ�.
				return;
			}
			if (item.getBless() >= 128) { // ������
				pc.sendPackets(new S_ServerMessage(1161)); // ������ �� �����ϴ�.
				return;
			}
		}
		int crystalCount = ResolventTable.getInstance().getCrystalCount(item.getItem().getItemId());
		if (crystalCount == 0) {
			pc.sendPackets(new S_ServerMessage(1161)); // ������ �� �����ϴ�.
			return;
		}
		
		int rnd = _random.nextInt(100) + 1;
		if (rnd >= 1 && rnd <= 5) {
			crystalCount = 0;
			pc.sendPackets(new S_ServerMessage(158, item.getName())); // \f1%0�� �����ϰ� ���� �ʰ� �Ǿ����ϴ�.
		} else if (rnd >= 6 && rnd <= 100) {
			crystalCount *= 1;
		} 
		/*else if (rnd >= 96 && rnd <= 100) {
			crystalCount *= 1.5;
			pc.getInventory().storeItem(41246, (int)(crystalCount * 1.5));
		}*/
		 
		if (crystalCount != 0) {
			L1ItemInstance crystal = ItemTable.getInstance().createItem(41246);
			crystal.setCount(crystalCount);
			if (pc.getInventory().checkAddItem(crystal, 1) == L1Inventory.OK) {
				pc.getInventory().storeItem(crystal);
				pc.sendPackets(new S_ServerMessage(403, crystal.getLogName())); // %0�� �տ� �־����ϴ�.
			} else { // ���� ��  ���� ���� ���鿡 ����߸��� ó���� ĵ���� ���� �ʴ´�(���� ����)
				L1World.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(crystal);
			}
		} 
		pc.getInventory().removeItem(item, 1);
		pc.getInventory().removeItem(resolvent, 1);
	}
}

