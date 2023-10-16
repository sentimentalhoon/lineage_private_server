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
 * Author: ChrisLiu.2007.06.30
 */
package l1j.server.server.clientpackets;
import server.LineageClient;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Pledge;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Pledge extends ClientBasePacket {

	private static final String C_PLEDGE = "[C] C_Pledge";

	public C_Pledge(byte abyte0[], LineageClient clientthread) {
		super(abyte0);
		L1PcInstance pc = clientthread.getActiveChar();
			
		if (pc.getClanid() > 0) {
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			
			if (pc.isCrown() && pc.getId() == clan.getLeaderId()) {
				if (pc.getLevel() < 45) {
					pc.sendPackets(new S_Pledge("pledgeM", pc.getId(), clan.getClanName(), clan.getOnlineMembersFP(), clan.getAllMembersFP()));
				} else {
					pc.sendPackets(new S_PacketBox(pc, S_PacketBox.PLEDGE_TWO));
				} 
			} else {
				pc.sendPackets(new S_Pledge("pledge", pc.getId(), clan.getClanName(), clan.getOnlineMembersFP()));
			}
		} else {
			pc.sendPackets(new S_ServerMessage(1064));
			//pc.sendPackets(new S_Pledge("pledge", pc.getId()));
		}
	}

	@Override
	public String getType() {
		return C_PLEDGE;
	}

}
