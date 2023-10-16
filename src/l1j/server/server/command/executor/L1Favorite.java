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

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.GMCommands;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1Favorite implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1Favorite.class.getName());
	private static final Map<Integer, String> _faviCom = new HashMap<Integer, String>();

	private L1Favorite() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Favorite();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			if (! _faviCom.containsKey(pc.getId())) {
				_faviCom.put(pc.getId(), "");
			}
			String faviCom = _faviCom.get(pc.getId());
			if (arg.startsWith("����")) {
				// Ŀ����� ���
				StringTokenizer st = new StringTokenizer(arg);
				st.nextToken();
				if (! st.hasMoreTokens()) {
					pc.sendPackets(new S_SystemMessage("Ŀ��尡 �����ϴ�. "));
					return;
				}
				StringBuilder cmd = new StringBuilder();
				String temp = st.nextToken(); // Ŀ��� Ÿ��
				if (temp.equalsIgnoreCase(cmdName)) {
					pc
							. sendPackets(new S_SystemMessage(cmdName
									+ " �ڽ��� ����� �� �����ϴ�. "));
					return;
				}
				cmd.append(temp + " ");
				while (st.hasMoreTokens()) {
					cmd.append(st.nextToken() + " ");
				}
				faviCom = cmd.toString(). trim();
				_faviCom.put(pc.getId(), faviCom);
				pc.sendPackets(new S_SystemMessage(faviCom + " �� ����߽��ϴ�. "));
			} else if (arg.startsWith("����")) {
				pc.sendPackets(new S_SystemMessage("������ ��� Ŀ���: " + faviCom));
			} else if (faviCom.isEmpty()) {
				pc.sendPackets(new S_SystemMessage("����ϰ� �ִ� Ŀ��尡 �����ϴ�. "));
			} else {
				StringBuilder cmd = new StringBuilder();
				StringTokenizer st = new StringTokenizer(arg);
				StringTokenizer st2 = new StringTokenizer(faviCom);
				while (st2.hasMoreTokens()) {
					String temp = st2.nextToken();
					if (temp.startsWith("%")) {
						cmd.append(st.nextToken() + " ");
					} else {
						cmd.append(temp + " ");
					}
				}
				while (st.hasMoreTokens()) {
					cmd.append(st.nextToken() + " ");
				}
				pc.sendPackets(new S_SystemMessage(cmd + " �� �����մϴ�. "));
				GMCommands.getInstance(). handleCommands(pc, cmd.toString());
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + " ���� [Ŀ����] " + "| "
					+ cmdName + " ���� | " + cmdName + " [�μ�] ��� �Է��� �ּ���. "));
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
