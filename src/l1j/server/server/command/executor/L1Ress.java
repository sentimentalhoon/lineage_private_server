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
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1Ress implements L1CommandExecutor {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1Ress.class.getName());

	private L1Ress() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Ress();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			int objid = pc.getId();
			pc.sendPackets(new S_SkillSound(objid, 759));
			Broadcaster.broadcastPacket(pc, new S_SkillSound(objid, 759));
			for (L1PcInstance tg : L1World.getInstance(). getVisiblePlayer(pc)) {
				if (tg.getCurrentHp() == 0 && tg.isDead()) {
					tg.sendPackets(new S_SystemMessage("GM�� ��Ȱ�� ���־����ϴ�. "));
					Broadcaster.broadcastPacket(tg, new S_SkillSound(tg.getId(), 3944));
					tg.sendPackets(new S_SkillSound(tg.getId(), 3944));
					// �ູ�� ��Ȱ ��ũ�Ѱ� ���� ȿ��
					tg.setTempID(objid);
					tg.sendPackets(new S_Message_YN(322, "")); // �� ��Ȱ�ϰ� �ͽ��ϱ�? (Y/N)
				} else {
					tg.sendPackets(new S_SystemMessage("GM�� HP,MP�� ȸ�����־����ϴ�."));
					Broadcaster.broadcastPacket(tg, new S_SkillSound(tg.getId(), 832));
					tg.sendPackets(new S_SkillSound(tg.getId(), 832));
					tg.setCurrentHp(tg.getMaxHp());
					tg.setCurrentMp(tg.getMaxMp());
				}
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + " Ŀ��� ����"));
		}
	}
}
