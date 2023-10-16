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
import l1j.server.server.model.Dungeon;
import l1j.server.server.model.Instance.L1PcInstance;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_EnterPortal extends ClientBasePacket {

	private static final String C_ENTER_PORTAL = "[C] C_EnterPortal";

	public C_EnterPortal(byte abyte0[], LineageClient client)
			throws Exception {
		super(abyte0);
		int locx = readH();
		int locy = readH();
		L1PcInstance pc = client.getActiveChar();
		if (pc.isTeleport()) { // �ڷ���Ʈ ó����
			return;
		}
		// ���� ������ �ڷ���Ʈ
		Dungeon.getInstance().dg(locx, locy, pc.getMap().getId(), pc);
	}

	@Override
	public String getType() {
		return C_ENTER_PORTAL;
	}
}
