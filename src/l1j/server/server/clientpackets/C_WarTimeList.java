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

import java.util.Calendar;

import server.LineageClient;

import l1j.server.server.datatables.CastleTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_WarTime;
import l1j.server.server.templates.L1Castle;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_WarTimeList extends ClientBasePacket {

	private static final String C_WAR_TIME_LIST = "[C] C_WarTimeList";

	public C_WarTimeList(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		L1PcInstance pc = clientthread.getActiveChar();
		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
		
		if (clan != null) {
			int castle_id = clan.getCastleId();
			if (castle_id != 0) { // ���� Ŭ�� ���̵�
				L1Castle l1castle = CastleTable.getInstance().getCastleTable(castle_id);
				if (l1castle.getWarBaseTime() == 0) {
					pc.sendPackets(new S_ServerMessage(305));// \f1������ ���� �ð��� ������ �� �����ϴ�.
					return;
				}

				Calendar warTime = l1castle.getWarTime();
				int year = warTime.get(Calendar.YEAR);
				int month = warTime.get(Calendar.MONTH);
				int day = warTime.get(Calendar.DATE);
				//System.out.println(day);
				Calendar warBase = Calendar.getInstance();
				warBase.set(year, month, day, 12, 00);// 4 23

				Calendar base_cal = Calendar.getInstance();
				base_cal.set(1997, 0, 1, 17, 0);// 1997/01/01 17:00(��)�� �������� �ϰ� �ִ�
				long base_millis = base_cal.getTimeInMillis();
				long millis = warBase.getTimeInMillis();
				long diff = millis - base_millis;
				diff -= 1200 * 60 * 1000; // ��������
				diff = diff / 60000; // �� ���� �߶����
				// time�� 1�� ���ϸ� 3:02(182�� ) ����ȴ�
				int time = (int) (diff / 182);

				pc.sendPackets(new S_WarTime(time));
				pc.sendPackets(new S_ServerMessage(300));// ���� �������� ���� �ð��� ������ �ֽʽÿ�.
			}
		}
	}

	@Override
	public String getType() {
		return C_WAR_TIME_LIST;
	}

}
