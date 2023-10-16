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

import java.util.List;
import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PetInstance;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_PetInventory extends ServerBasePacket {

	private static final String S_PET_INVENTORY = "[S] S_PetInventory";
	private byte[] _byte = null;

	public S_PetInventory(L1PetInstance pet) {
		List<L1ItemInstance> itemList = pet.getInventory().getItems();

		writeC(Opcodes.S_OPCODE_SHOWRETRIEVELIST);
		writeD(pet.getId());
		writeH(itemList.size());
		writeC(0x0b);
		L1ItemInstance item = null;
		for (Object itemObject : itemList) {
			item = (L1ItemInstance) itemObject;
			if (item != null) {
				writeD(item.getId());
				writeC(0x16);//3
				writeH(item.get_gfxid());
				writeC(item.getBless());
				writeD(item.getCount());
				switch (item.isEquipped() ? 1 : 0){
				case 0:
					writeC(item.isIdentified() ? 1 : 0);
					break;
				case 1:
					writeC(3);
					break;
				}
				writeS(item.getViewName());
			}
		}
		writeC(pet.getAC().getAc());
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}
	@Override
	public String getType() {
		return S_PET_INVENTORY;
	}
}
