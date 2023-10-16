/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.   See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.command.executor;

import java.util.logging.Logger;

import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.model.skill.L1SkillId;

public class L1Visible implements L1CommandExecutor {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1Visible.class.getName());

	private L1Visible() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Visible();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			pc.setGmInvis(false);
			pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
			pc.sendPackets(new S_Invis(pc.getId(), 0));
			Broadcaster.broadcastPacket(pc, new S_OtherCharPacks(pc));
			pc.sendPackets(new S_SystemMessage("������¸� �����߽��ϴ�. "));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + " Ŀ��� ����"));
		}
	}
}
