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

public class S_SystemMessage extends ServerBasePacket {
	private static final String S_SYSTEM_MESSAGE = "[S] S_SystemMessage";

	private byte[] _byte = null;

	/**
	 * Ŭ���̾�Ʈ�� �������� �������� �ʴ� ���������� �޼����� ǥ���Ѵ�.
	 * �޼����� nameid($xxx)�� ���ԵǾ� �ִ� ���� overload �� ����(����) ������ ����Ѵ�.
	 * 
	 * @param msg - ǥ���ϴ� ĳ���� ����
	 */
	public S_SystemMessage(String msg) {
		writeC(Opcodes.S_OPCODE_MSG);
		writeC(0x09);
		writeS(msg);
	}

	/**
	 * Ŭ���̾�Ʈ�� �������� �������� �ʴ� ���������� �޼����� ǥ���Ѵ�.
	 * 
	 * @param msg - ǥ���ϴ� ĳ���� ����
	 * @param nameid - ĳ���� ���ο� nameid($xxx)�� ���ԵǾ� �ִ� ��� true�� �Ѵ�.
	 */
	public S_SystemMessage(String msg, boolean nameid) {
		writeC(Opcodes.S_OPCODE_NPCSHOUT);
		writeC(2);
		writeD(0);
		writeS(msg);
		// NPC ä�� ��Ŷ�̸� nameid�� �ؼ��Ǳ� (����)������ �̰��� �̿��Ѵ�
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
		return S_SYSTEM_MESSAGE;
	}
}
