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

import l1j.server.server.model.L1Object;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1TrapInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1ShowTrap implements L1CommandExecutor {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1ShowTrap.class.getName());

	private L1ShowTrap() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1ShowTrap();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		if (arg.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.GMSTATUS_SHOWTRAPS, 0);
		} else if (arg.equalsIgnoreCase("��")) {
			pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.GMSTATUS_SHOWTRAPS);

			for (L1Object obj : pc.getNearObjects().getKnownObjects()) {
				if (obj instanceof L1TrapInstance) {
					pc.getNearObjects().removeKnownObject(obj);
					pc.sendPackets(new S_RemoveObject(obj));
				}
			}
		} else {
			pc.sendPackets(new S_SystemMessage(cmdName + " [��,��] ��� �Է��� �ּ���. "));
		}
	}
}
