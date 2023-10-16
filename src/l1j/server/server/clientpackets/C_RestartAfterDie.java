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
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_MapID;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_OwnCharPack;
import l1j.server.server.serverpackets.S_Weather;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_RestartAfterDie extends ClientBasePacket {

	private static final String C_RESTART = "[C] C_Restart";

	public C_RestartAfterDie(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		L1PcInstance pc = clientthread.getActiveChar();

		int[] loc;
		
		if (pc.getHellTime() > 0) {
			loc = new int[3];
			loc[0] = 32701;
			loc[1] = 32777;
			loc[2] = 666;
		} else {
			loc = Getback.GetBack_Location(pc, true);
		}

		pc.getNearObjects().removeAllKnownObjects();
		Broadcaster.broadcastPacket(pc, new S_RemoveObject(pc));

		pc.setCurrentHp(pc.getLevel());
		pc.set_food(39); // �׾����� ����? 10%
		pc.setDead(false);
		pc.setActionStatus(0);
		L1World.getInstance().moveVisibleObject(pc, loc[2]);
		pc.setX(loc[0]);
		pc.setY(loc[1]);
		pc.setMap((short) loc[2]);
		pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));
		Broadcaster.broadcastPacket(pc, new S_OtherCharPacks(pc));
		pc.sendPackets(new S_OwnCharPack(pc));
		pc.sendPackets(new S_CharVisualUpdate(pc));
		//pc.startHpRegeneration();
		//pc.startMpRegeneration();
		pc.sendPackets(new S_Weather(L1World.getInstance().getWeather()));
		if (pc.getHellTime() > 0) {
			pc.beginHell(false);
		}
	}

	@Override
	public String getType() {
		return C_RESTART;
	}
}