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

import java.util.concurrent.atomic.AtomicInteger;
import l1j.server.server.Opcodes;
import l1j.server.server.model.L1Character;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_RangeSkill extends ServerBasePacket {

	private static final String S_RANGE_SKILL = "[S] S_RangeSkill";
	private static AtomicInteger _sequentialNumber = new AtomicInteger(0);

	private byte[] _byte = null;

	public static final int TYPE_NODIR = 0;

	public static final int TYPE_DIR = 8;

	public S_RangeSkill(L1Character cha, L1Character[] target, int spellgfx,
			int actionId, int type) {
		buildPacket(cha, target, spellgfx, actionId, type);
	}

	private void buildPacket(L1Character cha, L1Character[] target,
			int spellgfx, int actionId, int type) {
		writeC(Opcodes.S_OPCODE_RANGESKILLS);
		writeC(actionId);
		writeD(cha.getId());
		writeH(cha.getX());
		writeH(cha.getY());
		if (type == TYPE_NODIR) {
			writeC(cha.getMoveState().getHeading());
		} else if (type == TYPE_DIR) {
			int newHeading = calcheading(cha.getX(), cha.getY(),
					target[0].getX(), target[0].getY());
			cha.getMoveState().setHeading(newHeading);
			writeC(cha.getMoveState().getHeading());
		}
		writeD(_sequentialNumber.incrementAndGet()); // ��ȣ�� ��ġ�� �ʰ� ������.
		writeH(spellgfx);
		writeC(type); // 0:���� 6:���Ÿ� 8:����&���Ÿ�
		writeH(0);
		writeH(target.length);
		for (int i = 0; i < target.length; i++) {			
			writeD(target[i].getId());
			writeC(0x20); // 0:������ ��� ���� 0�̿�:����
		}	
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		} else {
			int seq = _sequentialNumber.incrementAndGet();
			_byte[11] = (byte) (seq & 0xff);
			_byte[12] = (byte) (seq >> 8 & 0xff);
			_byte[13] = (byte) (seq >> 16 & 0xff);
			_byte[14] = (byte) (seq >> 24 & 0xff);
		}
		return _byte;
	}

	private static int calcheading(int myx, int myy, int tx, int ty) {
		int newheading = 0;
		if (tx > myx && ty > myy) {
			newheading = 3;
		}
		if (tx < myx && ty < myy) {
			newheading = 7;
		}
		if (tx > myx && ty == myy) {
			newheading = 2;
		}
		if (tx < myx && ty == myy) {
			newheading = 6;
		}
		if (tx == myx && ty < myy) {
			newheading = 0;
		}
		if (tx == myx && ty > myy) {
			newheading = 4;
		}
		if (tx < myx && ty > myy) {
			newheading = 5;
		}
		if (tx > myx && ty < myy) {
			newheading = 1;
		}
		return newheading;
	}
	@Override
	public String getType() {
		return S_RANGE_SKILL;
	}

}