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
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Party;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_CreateParty extends ClientBasePacket {

	private static final String C_CREATE_PARTY = "[C] C_CreateParty";

	public C_CreateParty(byte decrypt[], LineageClient client) throws Exception {
		super(decrypt);

		L1PcInstance pc = client.getActiveChar();

		int type = readC();
		if (type == 0 || type == 1) {// 0.�Ϲ� 1.�й�
			int targetId = readD();
			L1Object temp = L1World.getInstance().findObject(targetId);
			if (temp instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) temp;
				
				if (pc.getId() == targetPc.getId()) return;
				
				if (targetPc.isInParty()) {
					// ���� �ٸ� ��Ƽ�� �Ҽ��� �ֱ� (����)������ �ʴ��� �� �����ϴ�
					pc.sendPackets(new S_ServerMessage(415));
					return;
				}

				if (pc.isInParty()) {
					if (pc.getParty().isLeader(pc)) {
						targetPc.setPartyID(pc.getId());
						// \f2%0\f>%s�κ��� \fU��Ƽ \f> �� �ʴ�Ǿ����ϴ�. ���մϱ�? (Y/N)
						targetPc.sendPackets(new S_Message_YN(953, pc.getName()));
					} else {
						// ��Ƽ�� �������� �ʴ��� �� �ֽ��ϴ�.
						pc.sendPackets(new S_ServerMessage(416));
					}
				} else {
					pc.setPartyType(type);
					targetPc.setPartyID(pc.getId());
					switch (type) {
					case 0:
						// \f2%0\f>%s�κ��� \fU��Ƽ \f> �� �ʴ�Ǿ����ϴ�. ���մϱ�? (Y/N)
						targetPc.sendPackets(new S_Message_YN(953, pc.getName()));
						break;
					case 1:
						// \f2%0\f>%s \fU�ڵ��й���Ƽ\f> �ʴ��Ͽ����ϴ�. ����Ͻðڽ��ϱ�? (Y/N)
						targetPc.sendPackets(new S_Message_YN(954, pc.getName()));
						break;
					}
				}
			}
		} else if (type == 2) { // ä�� ��Ƽ
			String name = readS();
			L1PcInstance targetPc = L1World.getInstance().getPlayer(name);
			if (targetPc == null) {
				// %0��� �̸��� ����� �����ϴ�.
				pc.sendPackets(new S_ServerMessage(109));
				return;
			}
			if (pc.getId() == targetPc.getId()) return;
			
			if (targetPc.isInChatParty()) {
				// ���� �ٸ� ��Ƽ�� �Ҽ��� �ֱ� (����)������ �ʴ��� �� �����ϴ�
				pc.sendPackets(new S_ServerMessage(415));
				return;
			}

			if (pc.isInChatParty()) {
				if (pc.getChatParty().isLeader(pc)) {
					targetPc.setPartyID(pc.getId());
					// \f2%0\f>%s�κ���\fUä�� ��Ƽ \f>�� �ʴ�Ǿ����ϴ�. ���մϱ�? (Y/N)
					targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
				} else {
					// ��Ƽ�� �������� �ʴ��� �� �ֽ��ϴ�.
					pc.sendPackets(new S_ServerMessage(416));
				}
			} else {
				targetPc.setPartyID(pc.getId());
				// \f2%0\f>%s�κ���\fUä�� ��Ƽ \f>�� �ʴ�Ǿ����ϴ�. ���մϱ�? (Y/N)
				targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
			}
		} else if (type == 3){
			int target = readD();
			L1PcInstance nl = (L1PcInstance) L1World.getInstance().findObject(target);
			if (pc.getId() != pc.getParty().getLeader().getId()) return;
			pc.getParty().passLeader(nl);
		}

	}

	@Override
	public String getType() {
		return C_CREATE_PARTY;
	}

}
