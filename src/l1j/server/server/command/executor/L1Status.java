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

import java.util.StringTokenizer;
//import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Lawful;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1Status implements L1CommandExecutor {
//	private static Logger _log = Logger.getLogger(L1Status.class.getName());

	private L1Status() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Status();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			String char_name = st.nextToken();
			String param = st.nextToken();
			int value = Integer.parseInt(st.nextToken());

			L1PcInstance target = null;
			target = L1World.getInstance().getPlayer(char_name);

			if (target == null) {
				pc.sendPackets(new S_ServerMessage(73, char_name)); // \f1%0�� ������ �ϰ� ���� �ʽ��ϴ�.
				return;
			}

			// -- not use DB --
			if (param.equalsIgnoreCase("���")) {
				target.getAC().addAc((byte) (value - target.getAC().getAc()));
			} else if (param.equalsIgnoreCase("����")) {
				target.getResistance().addMr((short) (value - target.getResistance().getMr()));
			} else if (param.equalsIgnoreCase("����")) {
				target.addHitup((short) (value - target.getHitup()));
			} else if (param.equalsIgnoreCase("������")) {
				target.addDmgup((short) (value - target.getDmgup()));
				// -- use DB --
			} else {
				if (param.equalsIgnoreCase("��")) {
					target.addBaseMaxHp((short) (value - target.getBaseMaxHp()));
					target.setCurrentHp(target.getMaxHp());
				} else if (param.equalsIgnoreCase("����")) {
					target.addBaseMaxMp((short)(value - target.getBaseMaxMp()));
					target.setCurrentMp(target.getMaxMp());
				} else if (param.equalsIgnoreCase("����")) {
					target.setLawful(value);
					S_Lawful s_lawful = new S_Lawful(target.getId(), target.getLawful());
					target.sendPackets(s_lawful);
					Broadcaster.broadcastPacket(target, s_lawful);
				} else if (param.equalsIgnoreCase("��ȣ��")) {
					target.setKarma(value);
				} else if (param.equalsIgnoreCase("����")) {
					if(value == Config.GMCODE || value == 1){
						target.setAccessLevel((short) value);
						target.sendPackets(new S_SystemMessage("RESTART �ϸ� GM������ ����ϴ�."));
					} else if(value == 0 && target.getAccessLevel() == 1) {
						target.setAccessLevel((short) value);
						target.sendPackets(new S_SystemMessage("RESTART �ϸ� ������ ������ϴ�."));
					} else {
						target.sendPackets(new S_SystemMessage("GM��ȣ�� ��ġ���� �ʽ��ϴ�."));
					}
				} else if (param.equalsIgnoreCase("��")) {
					target.getAbility().setStr((byte)value);
				} else if (param.equalsIgnoreCase("��")) {
					target.getAbility().setCon((byte)value);
				} else if (param.equalsIgnoreCase("����")) {
					target.getAbility().setDex((byte)value);
				} else if (param.equalsIgnoreCase("��Ʈ")) {
					target.getAbility().setInt((byte)value);
				} else if (param.equalsIgnoreCase("����")) {
					target.getAbility().setWis((byte)value);
				} else if (param.equalsIgnoreCase("ī��")) {
					target.getAbility().setCha((byte)value);
				} else {
					pc.sendPackets(new S_SystemMessage("�������ͽ� " + param
							+ " (��)�� �Ҹ��մϴ�. "));
					return;
				}
				target.save(); // DB�� ĳ���� ������ �����Ѵ�
				target.refresh();
			}
			target.sendPackets(new S_OwnCharStatus(target));
			pc.sendPackets(new S_SystemMessage(target.getName() + "�� " + param
					+ "(��)�� " + value + "�� �����߽��ϴ�. "));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + " [ĳ���͸�] [����] [����ġ]�� �Է��� �ּ���. "));
			pc.sendPackets(new S_SystemMessage("[��][����][����][��ȣ��][����][��][��][����][��Ʈ][����][ī��]"));
		}
	}
}
