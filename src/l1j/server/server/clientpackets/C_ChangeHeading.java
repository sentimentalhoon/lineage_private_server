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

import java.util.logging.Logger;

import server.LineageClient;

import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ChangeHeading;

public class C_ChangeHeading extends ClientBasePacket {
	private static final String C_CHANGE_HEADING = "[C] C_ChangeHeading";
	private static Logger _log = Logger.getLogger(C_ChangeHeading.class.getName());

	public C_ChangeHeading(byte[] decrypt, LineageClient client) {
		super(decrypt);
		int heading = readC();
		if(heading < 0 || heading >7)return;
		L1PcInstance pc = client.getActiveChar();
		pc.getMoveState().setHeading(heading);

		if (!pc.isGmInvis() && !pc.isGhost() && !pc.isInvisble()) {
			Broadcaster.broadcastPacket(pc, new S_ChangeHeading(pc));
		}
		
		_log.finest("Change Heading : " + pc.getMoveState().getHeading());
	}

	@Override
	public String getType() {
		return C_CHANGE_HEADING;
	}
}