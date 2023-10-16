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

import server.LineageClient;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.utils.FaceToFace;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Fight extends ClientBasePacket {

	private static final String C_FIGHT = "[C] C_Fight";

	public C_Fight(byte abyte0[], LineageClient client)
			throws Exception {
		super(abyte0);

		L1PcInstance pc = client.getActiveChar();
		if (pc.isGhost()) {
			return;
		}
		L1PcInstance target = FaceToFace.faceToFace(pc);
		if (target != null) {
			if (!target.isParalyzed()) {
				if (pc.getFightId() != 0) {
					pc.sendPackets(new S_ServerMessage(633)); 
					return;
				} else if (target.getFightId() != 0) {
					target.sendPackets(new S_ServerMessage(634)); 
					return;
				}
				pc.setFightId(target.getId());
				target.setFightId(pc.getId());
				target.sendPackets(new S_Message_YN(630, pc.getName()));
			}
		}
	}

	@Override
	public String getType() {
		return C_FIGHT;
	}

}
