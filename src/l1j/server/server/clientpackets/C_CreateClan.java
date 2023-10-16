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
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_CreateClan extends ClientBasePacket {

	private static final String C_CREATE_CLAN = "[C] C_CreateClan";

	public C_CreateClan(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		String s = readS();
		int numOfNameBytes = 0;
		numOfNameBytes = s.getBytes("EUC-KR").length;
		
		L1PcInstance l1pcinstance = clientthread.getActiveChar();
		if (l1pcinstance.isCrown()) { // ������ �Ǵ� ��������
			if (l1pcinstance.getClanid() == 0) {				
				if (!l1pcinstance.getInventory().checkItem(L1ItemId.ADENA, 30000)) {
					l1pcinstance.sendPackets(new S_ServerMessage(337, "$4")); // \f1%0�� �����մϴ�.
					return;
				}
				for (int i = 0;i<s.length();i++) {  
					if (s.charAt(i) == ' ' || s.charAt(i) == '��'){
						l1pcinstance.sendPackets(new S_ServerMessage(53)); // �̸��� �߸��Ǿ����ϴ�. �ٸ� �̸��� �Է��Ͻʽÿ�.
						return; 
					}
				}
				if (8 < (numOfNameBytes - s.length()) || 16 < numOfNameBytes) {
					l1pcinstance.sendPackets(new S_ServerMessage(98)); // \f1�����̸��� �ʹ� ��ϴ�.
				}
				for (L1Clan clan : L1World.getInstance().getAllClans()) { // \f1 ���� �̸��� ������ �����մϴ�.
					if (clan.getClanName().toLowerCase().equals(s.toLowerCase())) {
						l1pcinstance.sendPackets(new S_ServerMessage(99)); // \f1 ���� �̸��� ������ �����մϴ�.
						return;
					}
				}
				L1Clan clan = ClanTable.getInstance().createClan(l1pcinstance, s); // ũ�� â��
				l1pcinstance.getInventory().consumeItem(L1ItemId.ADENA, 30000);
				if (clan != null) {
					l1pcinstance.sendPackets(new S_ServerMessage(84, s)); // \f1%0 ������ â���Ǿ����ϴ�.
				}
			} else {
				l1pcinstance.sendPackets(new S_ServerMessage(86)); // \f1 ���� ������ �Ἲ�ǰ� �����Ƿ� �ۼ��� �� �����ϴ�.
			}
		} else {
			l1pcinstance.sendPackets(new S_ServerMessage(85)); // \f1�������� ������������ ������ â���� �� �ֽ��ϴ�.
		}
	}

	@Override
	public String getType() {
		return C_CREATE_CLAN;
	}

}
