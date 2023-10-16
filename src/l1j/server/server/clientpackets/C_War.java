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

import java.util.List;

import server.LineageClient;

import l1j.server.Config;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_War extends ClientBasePacket {

	private static final String C_WAR = "[C] C_War";

	public C_War(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		int type = readC();
		String s = readS();

		L1PcInstance player = clientthread.getActiveChar();
		String playerName = player.getName();
		String clanName = player.getClanname();
		int clanId = player.getClanid();

		if (!player.isCrown()) { // ���� �̿�
			player.sendPackets(new S_ServerMessage(478));// \f1�������� ���������� ������ ������ �� �ֽ��ϴ�.
			return;
		}
		if (clanId == 0) { // ũ���̼Ҽ�
			player.sendPackets(new S_ServerMessage(272)); // \f1�����ϱ� ���ؼ��� �켱 ������ â������ ������ �ȵ˴ϴ�.
			return;
		}
		L1Clan clan = L1World.getInstance().getClan(clanName);
		if (clan == null) { // ��ũ���� �߰ߵ��� �ʴ´�
			return;
		}
		if (player.getId() != clan.getLeaderId()) { // ������
			player.sendPackets(new S_ServerMessage(478)); // \f1�������� ���������� ������ ������ �� �ֽ��ϴ�.
			return;
		}

		if (clanName.toLowerCase().equals(s.toLowerCase())) { // ��ũ���� ����
			return;
		}
		if (clan.getOnlineClanMember().length <= 4) {   // 5 ��� �� ���ڸ� �ٲٽø�˴ϴ�.
            player.sendPackets( new S_SystemMessage("���� ������ ���� �������� [5]�� �̻� �Ǿ�� ������ ������ �����մϴ�."));
            return; 
        }
		L1Clan enemyClan = null;
		String enemyClanName = null;
		for (L1Clan checkClan : L1World.getInstance().getAllClans()) { // ũ������ üũ
			if (checkClan.getClanName().toLowerCase().equals(s.toLowerCase())) {
				enemyClan = checkClan;
				enemyClanName = checkClan.getClanName();
				break;
			}
		}
		if (enemyClan == null) { // ��� ũ���� �߰ߵ��� �ʾҴ�
			return;
		}
		/*
		if (clan.getAlliance() == enemyClan.getClanId()) {
			player.sendPackets(new S_ServerMessage(1205)); // �������Ͱ��� ������ �� �� �����ϴ�.
			return;
		}*/
		boolean inWar = false;
		List<L1War> warList = L1World.getInstance().getWarList(); // ���� ����Ʈ�� ���
		for (L1War war : warList) {
			if (war.CheckClanInWar(clanName)) { // ��ũ���� �̹� ������
				if (type == 0) { // ��������
					player.sendPackets(new S_ServerMessage(234)); // \f1����� ������ ���� �������Դϴ�.
					return;
				}
				inWar = true;
				break;
			}
		}
		if (!inWar && (type == 2 || type == 3)) { // ��ũ���� ������ �ܷ̿�, �׺� �Ǵ� ����
			return;
		}

		if (clan.getCastleId() != 0) { // ��ũ���� ����
			if (type == 0) { // ��������
				player.sendPackets(new S_ServerMessage(474)); // ����� ���� ���� �����ϰ� �����Ƿ�, �ٸ� �÷θ� ���� �� �����ϴ�.
				return;
			} else if (type == 2 || type == 3) { // �׺�, ����
				return;
			}
		}

		if (enemyClan.getCastleId() == 0 && // ��� ũ���� ���ִ� �ƴϰ�, ��ĳ���Ͱ� Lv15 ����
				player.getLevel() <= 25) {
			player.sendPackets(new S_ServerMessage(232)); // \f1���� 15 ������ ���ִ� ���������� �� �����ϴ�.
			return;
		}

		if (enemyClan.getCastleId() != 0 && // ��� ũ���� ���ַ�, ��ĳ���Ͱ� Lv25 �̸�
				player.getLevel() < Config.NEWUSERSAFETY_LEVEL) {
			//player.sendPackets(new S_ServerMessage(475)); // �������� �����Ϸ���  ���� 25�� �̸��� ������ �ȵ˴ϴ�.
			player.sendPackets(new S_SystemMessage("�������� �����Ϸ���  ���� ["+ Config.NEWUSERSAFETY_LEVEL +"] �� �̸��� ������ �ȵ˴ϴ�.")); // �������� �����Ϸ���  ���� 25�� �̸��� ������ �ȵ˴ϴ�.
			return;
		}

		/*if (clan.getHouseId() > 0){
			player.sendPackets(new S_SystemMessage("����Ʈ�� �ִ� ���¿����� ���� ���� �� �� �����ϴ�."));
			return;
		}*/
		if (enemyClan.getCastleId() != 0) { // ��� ũ���� ����
			int castle_id = enemyClan.getCastleId();
			if (WarTimeController.getInstance().isNowWar(castle_id)) { // ���� �ð���
				L1PcInstance clanMember[] = clan.getOnlineClanMember();
				for (int k = 0; k < clanMember.length; k++) {
					if (L1CastleLocation.checkInWarArea(castle_id, clanMember[k])) {
						player.sendPackets(new S_ServerMessage(477)); // ����� ������ ��� ���Ϳ��� ���� �ۿ� ������ ������ �������� ������ �� �����ϴ�.
						player.sendPackets(new S_SystemMessage("[" + clanMember[k].getName() + "]���� ������ �ȿ� �ֽ��ϴ�.")); // ����� ������ ��� ���Ϳ��� ���� �ۿ� ������ ������ �������� ������ �� �����ϴ�.
						return;
					}
				}
				boolean enemyInWar = false;
				for (L1War war : warList) {
					if (war.CheckClanInWar(enemyClanName)) { // ��� ũ���� �̹� ������
						if (type == 0) { // ��������
							war.DeclareWar(clanName, enemyClanName);
							war.AddAttackClan(clanName);
						} else if (type == 2 || type == 3) {
							if (!war.CheckClanInSameWar(clanName, enemyClanName)) { // ��ũ���� ��� ũ���� �ٸ� ����
								return;
							}
							if (type == 2) { // �׺�
								war.SurrenderWar(clanName, enemyClanName);
							} else if (type == 3) { // ����
								war.CeaseWar(clanName, enemyClanName);
							}
						}
						enemyInWar = true;
						break;
					}
				}
				if (!enemyInWar && type == 0) { // ��� ũ���� ������ �ܷ̿�, ��������
					L1War war = new L1War();
					war.handleCommands(1, clanName, enemyClanName); // ������ ����
				}
			} else { // ���� �ð���
				if (type == 0) { // ��������
					player.sendPackets(new S_ServerMessage(476)); // ���� �������� �ð��� �ƴմϴ�.
				}
			}
		} else { // ��� ũ���� ���ִ� �ƴϴ�
			boolean enemyInWar = false;
			for (L1War war : warList) {
				if (war.CheckClanInWar(enemyClanName)) { // ��� ũ���� �̹� ������
					if (type == 0) { // ��������
						player.sendPackets(new S_ServerMessage(236, enemyClanName)); // %0������ ����� ���Ͱ��� ������ �����߽��ϴ�.
						return;
					} else if (type == 2 || type == 3) { // �׺� �Ǵ� ����
						if (!war.CheckClanInSameWar(clanName, enemyClanName)) { // ��ũ���� ��� ũ���� �ٸ� ����
							return;
						}
					}
					enemyInWar = true;
					break;
				}
			}
			if (!enemyInWar && (type == 2 || type == 3)) { // ��� ũ���� ������ �ܷ̿�, �׺� �Ǵ� ����
				return;
			}

			// �������� �ƴ� ���, ����� �������� ������ �ʿ�
			L1PcInstance enemyLeader = L1World.getInstance().getPlayer(enemyClan.getLeaderName());

			if (enemyLeader == null) { // ����� �����ְ� �߰ߵ��� �ʾҴ�
				player.sendPackets(new S_ServerMessage(218, enemyClanName)); // \f1%0 ������ ���ִ� ���� ���忡 �����ϴ�.
				return;
			}

			if (type == 0) { // ��������
				enemyLeader.setTempID(player.getId()); // ����� ������Ʈ ID�� ������ �д�
				enemyLeader.sendPackets(new S_Message_YN(217, clanName,
						playerName)); // %0������%1�� ����� ���Ͱ��� ������ �ٶ�� �ֽ��ϴ�. ���￡ ���մϱ�? (Y/N)
			} else if (type == 2) { // �׺�
				enemyLeader.setTempID(player.getId()); // ����� ������Ʈ ID�� ������ �д�
				enemyLeader.sendPackets(new S_Message_YN(221, clanName)); // %0������ �׺��� �ٶ�� �ֽ��ϴ�. �޾Ƶ��Դϱ�? (Y/N)
			} else if (type == 3) { // ����
				enemyLeader.setTempID(player.getId()); // ����� ������Ʈ ID�� ������ �д�
				enemyLeader.sendPackets(new S_Message_YN(222, clanName)); // %0������ ������ ������ �ٶ�� �ֽ��ϴ�. �����մϱ�? (Y/N)
			}
		}
	}

	@Override
	public String getType() {
		return C_WAR;
	}

}
