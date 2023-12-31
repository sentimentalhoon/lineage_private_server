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

import server.LineageClient;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1SKick implements L1CommandExecutor {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1SKick.class.getName());

	private L1SKick() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1SKick();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			L1PcInstance target = L1World.getInstance(). getPlayer(arg);
			if (target != null) {
				pc.sendPackets(new S_SystemMessage((new StringBuilder())
						. append(target.getName()). append(" 를 강력추방 했습니다. ")
						. toString()));				
				L1World.getInstance().removeObject(target);
				target.getNetConnection().quitGame(target);
				target.logout();
//				target.getNetConnection().kick();
				
			} else {
				pc.sendPackets(new S_SystemMessage(
						"그러한 이름의 캐릭터는 월드내에는 존재하지 않습니다. "));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + " [캐릭터명]으로 입력해 주세요. "));
		}
	}
}
