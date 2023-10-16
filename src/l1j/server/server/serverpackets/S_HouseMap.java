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

import l1j.server.server.Opcodes;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_HouseMap extends ServerBasePacket {

	private static final String S_HOUSEMAP = "[S] S_HouseMap";
	private byte[] _byte = null;

	public S_HouseMap(int objectId, String house_number) {
		buildPacket(objectId, house_number);
	}

	private void buildPacket(int objectId, String house_number) {
		int number = Integer.valueOf(house_number);

		writeC(Opcodes.S_OPCODE_HOUSEMAP);
		writeD(objectId);
		writeD(number);
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
		return S_HOUSEMAP;
	}
}
