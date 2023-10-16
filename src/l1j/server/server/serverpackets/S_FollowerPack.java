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
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1FollowerInstance;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket, S_NPCPack

public class S_FollowerPack extends ServerBasePacket {

	private static final String S_FOLLOWER_PACK = "[S] S_FollowerPack";
	
	private static final int STATUS_POISON = 1;

	private byte[] _byte = null;

	public S_FollowerPack(L1FollowerInstance follower, L1PcInstance pc) {
		writeC(Opcodes.S_OPCODE_SHOWOBJ);
		writeH(follower.getX());
		writeH(follower.getY());
		writeD(follower.getId());
		writeH(follower.getGfxId().getGfxId());
		writeC(follower.getActionStatus());
		writeC(follower.getMoveState().getHeading());
		writeC(follower.getLight().getChaLightSize());
		writeC(follower.getMoveState().getMoveSpeed());
		writeD(0);
		writeH(0);
		writeS(follower.getNameId());
		writeS(follower.getTitle());
		int status = 0;
		if (follower.getPoison() != null) {
			if (follower.getPoison().getEffectId() == 1) {
				status |= STATUS_POISON;
			}
		}
		writeC(status);
		writeD(0);
		writeS(null);
		writeS(null);
		writeC(0);
		writeC(0xFF);
		writeC(0);
		writeC(follower.getLevel());
		writeC(0);
		writeC(0xFF);
		writeC(0xFF);
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		}

		return _byte;
	}
	@Override
	public String getType() {
		return S_FOLLOWER_PACK;
	}

}
