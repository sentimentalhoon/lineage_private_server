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
import l1j.server.server.model.Instance.L1ItemInstance;

public class S_ItemColor extends ServerBasePacket {

	private static final String S_ITEM_COLOR = "[S] S_ItemColor";

	/**
	 * �������� ���� �����Ѵ�. �ູ������ ���°� ��ȭ���� �� � ������
	 */
	public S_ItemColor(L1ItemInstance item) {
		if (item == null) {
			return;
		}
		buildPacket(item);
	}
	
	public S_ItemColor(L1ItemInstance item, int color) {
		if (item == null) {
			return;
		}
		buildPacket(item, color);
	}

	private void buildPacket(L1ItemInstance item) {
		writeC(Opcodes.S_OPCODE_ITEMCOLOR);
		writeD(item.getId());
		writeC(item.getBless()); // 0:b 1:n 2:c -�� ��:�������� ���εǾ�?
	}
	
	private void buildPacket(L1ItemInstance item, int color) {
		writeC(Opcodes.S_OPCODE_ITEMCOLOR);
		writeD(item.getId());
		// 0 : �ູ 1: ���� 2: ���� 3: ��Ȯ�� 128: ����� 129: ���� 130: ���ֺ��� 131: ��Ȯ�κ���
		writeC(color);
	}

	@Override
	public byte[] getContent() {
		return getBytes();
	}
	@Override
	public String getType() {
		return S_ITEM_COLOR;
	}

}
