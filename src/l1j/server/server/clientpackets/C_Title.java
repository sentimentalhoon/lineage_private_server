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

import java.util.logging.Level;
import java.util.logging.Logger;

import server.LineageClient;

import l1j.server.Config;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CharTitle;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Title extends ClientBasePacket {

	private static final String C_TITLE = "[C] C_Title";
	private static Logger _log = Logger.getLogger(C_Title.class.getName());

	public C_Title(byte abyte0[], LineageClient clientthread) {
		super(abyte0);
		L1PcInstance pc = clientthread.getActiveChar();
		String charName = readS();
		String title = readS();		

		if (charName.isEmpty() || title.isEmpty()) {
			// \f1 ������ ���� �Է��� �ּ��䣺��/title \f0ĳ���͸� ȣĪ\f1��
			pc.sendPackets(new S_ServerMessage(196));
			return;
		}

		L1PcInstance target = L1World.getInstance().getPlayer(charName);
		if (target == null) { return; }

		if (pc.isGm()) {
			changeTitle(target, title);
			return;
		}

		if (isClanLeader(pc)) { // ������
			if (pc.getId() == target.getId()) { // �ڽ�
				if (pc.getLevel() < 10) {
					// \f1���Ϳ��� ���, ȣĪ�� ��������  ���� 10�̻��� �ƴϸ� �ȵ˴ϴ�.
					pc.sendPackets(new S_ServerMessage(197));
					return;
				}
				changeTitle(pc, title);
			} else { 
				if (pc.getClanid() != target.getClanid()) {
					// \f1���Ϳ��� �ƴϸ� Ÿ�ο��� ȣĪ�� �� �� �����ϴ�.
					pc.sendPackets(new S_ServerMessage(199));
					return;
				}
				if (target.getLevel() < 10) {
					// \f1%0�� ������ 10 �̸��̹Ƿ� ȣĪ�� �� �� �����ϴ�.
					pc.sendPackets(new S_ServerMessage(202, charName));
					return;
				}
				changeTitle(target, title);
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					for (L1PcInstance clanPc : clan.getOnlineClanMember()) {
						// \f1%0��%1�� ��%2��� �ϴ� ȣĪ�� �־����ϴ�.
						clanPc.sendPackets(new S_ServerMessage(203, pc.getName(), charName, title));
					}
				}
			}
		} else {
			if (pc.getId() == target.getId()) { // �ڽ�
				if (pc.getClanid() != 0 && !Config.CHANGE_TITLE_BY_ONESELF) {
					// \f1���Ϳ����� ȣĪ�� �־����� ���� �������� �������� ���Դϴ�.
					pc.sendPackets(new S_ServerMessage(198));
					return;
				}
				if (target.getLevel() < 40) {
					// \f1���Ϳ��� �ƴѵ� ȣĪ�� �������� , ���� 40�̻��� �ƴϸ� �ȵ˴ϴ�.
					pc.sendPackets(new S_ServerMessage(200));
					return;
				}
				changeTitle(pc, title);
			} else { // Ÿ��
				if (pc.isCrown()) { // ���տ� �Ҽ��� ����
					if (pc.getClanid() == target.getClanid()) {
						// \f1%0�� ����� ������ �ƴմϴ�.
						pc.sendPackets(new S_ServerMessage(201, pc.getClanname()));
						return;
					}
				}
			}
		}
	}

	private void changeTitle(L1PcInstance pc, String title) {
		int objectId = pc.getId();
		pc.setTitle(title);
		pc.sendPackets(new S_CharTitle(objectId, title));
		Broadcaster.broadcastPacket(pc, new S_CharTitle(objectId, title));
		try {
			pc.save(); // DB�� ĳ���� ������ �� ��
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private boolean isClanLeader(L1PcInstance pc) {
		boolean isClanLeader = false;
		if (pc.getClanid() != 0) { // ũ�� �Ҽ�
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (pc.isCrown() && pc.getId() == clan.getLeaderId()) { // ����, ����, ������
					isClanLeader = true;
				}
			}
		}
		return isClanLeader;
	}

	@Override
	public String getType() {
		return C_TITLE;
	}

}
