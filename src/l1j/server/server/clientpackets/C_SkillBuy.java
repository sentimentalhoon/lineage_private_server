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
import l1j.server.server.serverpackets.S_SkillBuy;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_SkillBuy extends ClientBasePacket {

	private static final String C_SKILL_BUY = "[C] C_SkillBuy";

	public C_SkillBuy(byte abyte0[], LineageClient clientthread)	throws Exception {
		super(abyte0);

		int i = readD();

		L1PcInstance pc = clientthread.getActiveChar();
		if (pc.isGhost()) {	return;}
		
		pc.sendPackets(new S_SkillBuy(i, pc));
	}

	@Override
	public String getType() {
		return C_SKILL_BUY;
	}

}
