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

import l1j.server.Warehouse.PackageWarehouse;
import l1j.server.Warehouse.WarehouseManager;
import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.RealTime;
import l1j.server.server.model.gametime.RealTimeClock;

public class S_RetrievePackageList extends ServerBasePacket {
	public S_RetrievePackageList(int objid, L1PcInstance pc) {
		if (pc.getInventory().getSize() < 180) {
			PackageWarehouse w = WarehouseManager.getInstance().getPackageWarehouse(pc.getAccountName());
			if(w == null) return;
			int size = w.getSize();
			long addDay = 86400 * 1000;// 1day
			
			if (size > 0) {
				writeC(Opcodes.S_OPCODE_SHOWRETRIEVELIST);
				writeD(objid);
				writeH(size);
				writeC(10); // 6 : ������ 7 : �� 8 : ����â�� �ñ�� 9: ����ã�� 15:��Ű������
				L1ItemInstance item = null;
				for (Object itemObject : w.getItems()) {
					item = (L1ItemInstance) itemObject;
					if(item.getId() == 50021 && item.getBuyTime() != null) {
						RealTime time = RealTimeClock.getInstance().getRealTime();
						long showtime = item.getBuyTime().getTime() + addDay;
						if(time.getSeconds() > showtime){
							size -= 1;
							continue;
						}
					}
					writeD(item.getId());
					writeC(0);
					writeH(item.get_gfxid());
					writeC(item.getBless());
					writeD(item.getCount());
					writeC(item.isIdentified() ? 1 : 0);
					writeS(item.getViewName());
				}
			}
			// Ŭ���̾�Ʈ�� ������ �޼��� �߰ߵ��� �ʾұ� ������ ��ǥ��
			/*
			 * else { l1pcinstance.sendPackets(new
			 * S_SystemMessage("�ƹ��͵� �þ� �ϰ� ���� �ʽ��ϴ�.")); }
			 */
		} else {
			pc.sendPackets(new S_ServerMessage(263)); // \f1�ѻ���� ĳ���Ͱ� ������ ���� �� �ִ� �������� �ִ� 180�������Դϴ�.
		}
	}

	@Override
	public byte[] getContent() throws IOException {
		return getBytes();
	}

}
