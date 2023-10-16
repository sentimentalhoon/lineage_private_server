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

package l1j.server.server.utils;

import java.util.List;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1BuffNpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.utils:
// FaceToFace

public class FaceToFace {

	private FaceToFace() {
	}

	public static L1PcInstance faceToFace(L1PcInstance pc) {
		int pcX = pc.getX();
		int pcY = pc.getY();
		int pcHeading = pc.getMoveState().getHeading();
		List<L1PcInstance> players = L1World.getInstance().getVisiblePlayer(pc, 1);

		if (players.size() == 0) { // 1 �� �̳��� PC�� ���� ���
			pc.sendPackets(new S_ServerMessage(93)); // \f1 �ű⿡�� �ƹ��� �����ϴ�.
			return null;
		}
		for (L1PcInstance target : players) {
			int targetX = target.getX();
			int targetY = target.getY();
			int targetHeading = target.getMoveState().getHeading();
			if (pcHeading == 0 && pcX == targetX && pcY == (targetY + 1)) {
				if (targetHeading == 4) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			} else if (pcHeading == 1 && pcX == (targetX - 1) && pcY == (targetY + 1)) {
				if (targetHeading == 5) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			} else if (pcHeading == 2 && pcX == (targetX - 1) && pcY == targetY) {
				if (targetHeading == 6) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			} else if (pcHeading == 3 && pcX == (targetX - 1) && pcY == (targetY - 1)) {
				if (targetHeading == 7) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			} else if (pcHeading == 4 && pcX == targetX && pcY == (targetY - 1)) {
				if (targetHeading == 0) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			} else if (pcHeading == 5 && pcX == (targetX + 1) && pcY == (targetY - 1)) {
				if (targetHeading == 1) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			} else if (pcHeading == 6 && pcX == (targetX + 1) && pcY == targetY) {
				if (targetHeading == 2) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			} else if (pcHeading == 7 && pcX == (targetX + 1) && pcY == (targetY + 1)) {
				if (targetHeading == 3) {
					return target;
				} else {
					pc.sendPackets(new S_ServerMessage(91, target.getName())); // \f1%0�� ����� ���� ���� �ʽ��ϴ�.
					return null;
				}
			}
		}
		pc.sendPackets(new S_ServerMessage(93)); // \f1 �ű⿡�� �ƹ��� �����ϴ�.
		return null;
	}
	
	public static L1PcInstance faceToFaceForNpc(L1BuffNpcInstance npc) {
		int pcX = npc.getX();
		int pcY = npc.getY();
		int pcHeading = npc.getMoveState().getHeading();
		List<L1PcInstance> players = L1World.getInstance().getVisiblePlayer(npc, 1);

		if (players.size() == 0) { // 1 �� �̳��� PC�� ���� ���
			return null;
		}
		for (L1PcInstance target : players) {
			int targetX = target.getX();
			int targetY = target.getY();
			int targetHeading = target.getMoveState().getHeading();
			if (pcHeading == 0 && pcX == targetX && pcY == (targetY + 1)) {
				if (targetHeading == 4) {
					return target;
				} else {
					return null;
				}
			} else if (pcHeading == 1 && pcX == (targetX - 1) && pcY == (targetY + 1)) {
				if (targetHeading == 5) {
					return target;
				} else {
					return null;
				}
			} else if (pcHeading == 2 && pcX == (targetX - 1) && pcY == targetY) {
				if (targetHeading == 6) {
					return target;
				} else {
					return null;
				}
			} else if (pcHeading == 3 && pcX == (targetX - 1) && pcY == (targetY - 1)) {
				if (targetHeading == 7) {
					return target;
				} else {
					return null;
				}
			} else if (pcHeading == 4 && pcX == targetX && pcY == (targetY - 1)) {
				if (targetHeading == 0) {
					return target;
				} else {
					return null;
				}
			} else if (pcHeading == 5 && pcX == (targetX + 1) && pcY == (targetY - 1)) {
				if (targetHeading == 1) {
					return target;
				} else {
					return null;
				}
			} else if (pcHeading == 6 && pcX == (targetX + 1) && pcY == targetY) {
				if (targetHeading == 2) {
					return target;
				} else {
					return null;
				}
			} else if (pcHeading == 7 && pcX == (targetX + 1) && pcY == (targetY + 1)) {
				if (targetHeading == 3) {
					return target;
				} else {
					return null;
				}
			}
		}
		return null;
	}
}
