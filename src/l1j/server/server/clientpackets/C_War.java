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

		if (!player.isCrown()) { // 군주 이외
			player.sendPackets(new S_ServerMessage(478));// \f1프린스와 프린세스만 전쟁을 포고할 수 있습니다.
			return;
		}
		if (clanId == 0) { // 크란미소속
			player.sendPackets(new S_ServerMessage(272)); // \f1전쟁하기 위해서는 우선 혈맹을 창설하지 않으면 안됩니다.
			return;
		}
		L1Clan clan = L1World.getInstance().getClan(clanName);
		if (clan == null) { // 자크란이 발견되지 않는다
			return;
		}
		if (player.getId() != clan.getLeaderId()) { // 혈맹주
			player.sendPackets(new S_ServerMessage(478)); // \f1프린스와 프린세스만 전쟁을 포고할 수 있습니다.
			return;
		}

		if (clanName.toLowerCase().equals(s.toLowerCase())) { // 자크란을 지정
			return;
		}
		if (clan.getOnlineClanMember().length <= 4) {   // 5 라고 된 숫자를 바꾸시면됩니다.
            player.sendPackets( new S_SystemMessage("현재 접속한 혈맹 구성원이 [5]명 이상 되어야 공성전 선포가 가능합니다."));
            return; 
        }
		L1Clan enemyClan = null;
		String enemyClanName = null;
		for (L1Clan checkClan : L1World.getInstance().getAllClans()) { // 크란명을 체크
			if (checkClan.getClanName().toLowerCase().equals(s.toLowerCase())) {
				enemyClan = checkClan;
				enemyClanName = checkClan.getClanName();
				break;
			}
		}
		if (enemyClan == null) { // 상대 크란이 발견되지 않았다
			return;
		}
		/*
		if (clan.getAlliance() == enemyClan.getClanId()) {
			player.sendPackets(new S_ServerMessage(1205)); // 동맹혈맹과는 전쟁을 할 수 없습니다.
			return;
		}*/
		boolean inWar = false;
		List<L1War> warList = L1World.getInstance().getWarList(); // 전쟁 리스트를 취득
		for (L1War war : warList) {
			if (war.CheckClanInWar(clanName)) { // 자크란이 이미 전쟁중
				if (type == 0) { // 선전포고
					player.sendPackets(new S_ServerMessage(234)); // \f1당신의 혈맹은 벌써 전쟁중입니다.
					return;
				}
				inWar = true;
				break;
			}
		}
		if (!inWar && (type == 2 || type == 3)) { // 자크란이 전쟁중 이외로, 항복 또는 종결
			return;
		}

		if (clan.getCastleId() != 0) { // 자크란이 성주
			if (type == 0) { // 선전포고
				player.sendPackets(new S_ServerMessage(474)); // 당신은 벌써 성을 소유하고 있으므로, 다른 시로를 잡을 수 없습니다.
				return;
			} else if (type == 2 || type == 3) { // 항복, 종결
				return;
			}
		}

		if (enemyClan.getCastleId() == 0 && // 상대 크란이 성주는 아니고, 자캐릭터가 Lv15 이하
				player.getLevel() <= 25) {
			player.sendPackets(new S_ServerMessage(232)); // \f1레벨 15 이하의 군주는 선전포고할 수 없습니다.
			return;
		}

		if (enemyClan.getCastleId() != 0 && // 상대 크란이 성주로, 자캐릭터가 Lv25 미만
				player.getLevel() < Config.NEWUSERSAFETY_LEVEL) {
			//player.sendPackets(new S_ServerMessage(475)); // 공성전을 선언하려면  레벨 25에 이르지 않으면 안됩니다.
			player.sendPackets(new S_SystemMessage("공성전을 선언하려면  레벨 ["+ Config.NEWUSERSAFETY_LEVEL +"] 에 이르지 않으면 안됩니다.")); // 공성전을 선언하려면  레벨 25에 이르지 않으면 안됩니다.
			return;
		}

		/*if (clan.getHouseId() > 0){
			player.sendPackets(new S_SystemMessage("아지트가 있는 상태에서는 선전 포고를 할 수 없습니다."));
			return;
		}*/
		if (enemyClan.getCastleId() != 0) { // 상대 크란이 성주
			int castle_id = enemyClan.getCastleId();
			if (WarTimeController.getInstance().isNowWar(castle_id)) { // 전쟁 시간내
				L1PcInstance clanMember[] = clan.getOnlineClanMember();
				for (int k = 0; k < clanMember.length; k++) {
					if (L1CastleLocation.checkInWarArea(castle_id, clanMember[k])) {
						player.sendPackets(new S_ServerMessage(477)); // 당신을 포함한 모든 혈맹원이 성의 밖에 나오지 않으면 공성전은 선언할 수 없습니다.
						player.sendPackets(new S_SystemMessage("[" + clanMember[k].getName() + "]님이 공성존 안에 있습니다.")); // 당신을 포함한 모든 혈맹원이 성의 밖에 나오지 않으면 공성전은 선언할 수 없습니다.
						return;
					}
				}
				boolean enemyInWar = false;
				for (L1War war : warList) {
					if (war.CheckClanInWar(enemyClanName)) { // 상대 크란이 이미 전쟁중
						if (type == 0) { // 선전포고
							war.DeclareWar(clanName, enemyClanName);
							war.AddAttackClan(clanName);
						} else if (type == 2 || type == 3) {
							if (!war.CheckClanInSameWar(clanName, enemyClanName)) { // 자크란과 상대 크란이 다른 전쟁
								return;
							}
							if (type == 2) { // 항복
								war.SurrenderWar(clanName, enemyClanName);
							} else if (type == 3) { // 종결
								war.CeaseWar(clanName, enemyClanName);
							}
						}
						enemyInWar = true;
						break;
					}
				}
				if (!enemyInWar && type == 0) { // 상대 크란이 전쟁중 이외로, 선전포고
					L1War war = new L1War();
					war.handleCommands(1, clanName, enemyClanName); // 공성전 개시
				}
			} else { // 전쟁 시간외
				if (type == 0) { // 선전포고
					player.sendPackets(new S_ServerMessage(476)); // 아직 공성전의 시간이 아닙니다.
				}
			}
		} else { // 상대 크란이 성주는 아니다
			boolean enemyInWar = false;
			for (L1War war : warList) {
				if (war.CheckClanInWar(enemyClanName)) { // 상대 크란이 이미 전쟁중
					if (type == 0) { // 선전포고
						player.sendPackets(new S_ServerMessage(236, enemyClanName)); // %0혈맹이 당신의 혈맹과의 전쟁을 거절했습니다.
						return;
					} else if (type == 2 || type == 3) { // 항복 또는 종결
						if (!war.CheckClanInSameWar(clanName, enemyClanName)) { // 자크란과 상대 크란이 다른 전쟁
							return;
						}
					}
					enemyInWar = true;
					break;
				}
			}
			if (!enemyInWar && (type == 2 || type == 3)) { // 상대 크란이 전쟁중 이외로, 항복 또는 종결
				return;
			}

			// 공성전이 아닌 경우, 상대의 혈맹주의 승인이 필요
			L1PcInstance enemyLeader = L1World.getInstance().getPlayer(enemyClan.getLeaderName());

			if (enemyLeader == null) { // 상대의 혈맹주가 발견되지 않았다
				player.sendPackets(new S_ServerMessage(218, enemyClanName)); // \f1%0 혈맹의 군주는 현재 월드에 없습니다.
				return;
			}

			if (type == 0) { // 선전포고
				enemyLeader.setTempID(player.getId()); // 상대의 오브젝트 ID를 보존해 둔다
				enemyLeader.sendPackets(new S_Message_YN(217, clanName,
						playerName)); // %0혈맹의%1가 당신의 혈맹과의 전쟁을 바라고 있습니다. 전쟁에 응합니까? (Y/N)
			} else if (type == 2) { // 항복
				enemyLeader.setTempID(player.getId()); // 상대의 오브젝트 ID를 보존해 둔다
				enemyLeader.sendPackets(new S_Message_YN(221, clanName)); // %0혈맹이 항복을 바라고 있습니다. 받아들입니까? (Y/N)
			} else if (type == 3) { // 종결
				enemyLeader.setTempID(player.getId()); // 상대의 오브젝트 ID를 보존해 둔다
				enemyLeader.sendPackets(new S_Message_YN(222, clanName)); // %0혈맹이 전쟁의 종결을 바라고 있습니다. 종결합니까? (Y/N)
			}
		}
	}

	@Override
	public String getType() {
		return C_WAR;
	}

}
