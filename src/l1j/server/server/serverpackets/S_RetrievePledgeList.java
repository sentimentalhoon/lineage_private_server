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
package l1j.server.server.serverpackets;

import java.io.IOException;

import l1j.server.Warehouse.ClanWarehouse;
import l1j.server.Warehouse.WarehouseManager;
import l1j.server.server.Opcodes;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;

public class S_RetrievePledgeList extends ServerBasePacket {
	public S_RetrievePledgeList(int objid, L1PcInstance pc) {
		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
		if (clan == null) {
			return;
		}

		ClanWarehouse clanWarehouse = WarehouseManager.getInstance().getClanWarehouse(clan.getClanName());

		if(!clanWarehouse.lock(pc.getId())) {
			// \f1 ���Ϳ��� â�� ������Դϴ�.��а� ������ ���� �̿��� �ּ���.
			pc.sendPackets(new S_ServerMessage(209));
			return;
		}

		if (pc.getInventory().getSize() < 180) {
			int size = clanWarehouse.getSize();
			if (size > 0) {
				//clanWarehouse.setWarehouseUsingChar(pc.getId()); 
				writeC(Opcodes.S_OPCODE_SHOWRETRIEVELIST);
				writeD(objid);
				writeH(size);
				writeC(5); // ���� â��
				L1ItemInstance item = null;
				for (Object itemObject : clanWarehouse.getItems()) {
					item = (L1ItemInstance) itemObject;
					writeD(item.getId());
					writeC(0);
					writeH(item.get_gfxid());
					writeC(item.getBless());
					writeD(item.getCount());
					writeC(item.isIdentified() ? 1 : 0);
					writeS(item.getViewName());
				}
			}
		} else {
			pc.sendPackets(new S_ServerMessage(263)); // \f1�ѻ���� ĳ���Ͱ� ������ ���� �� �ִ� �������� �ִ� 180�������Դϴ�.
		}
	}

	@Override
	public byte[] getContent() throws IOException {
		return getBytes();
	}
}
