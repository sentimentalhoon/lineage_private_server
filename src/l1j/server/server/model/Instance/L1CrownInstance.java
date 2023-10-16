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
package l1j.server.server.model.Instance;

import java.util.List;

import l1j.server.server.datatables.ClanTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1WarSpawn;
import l1j.server.server.model.L1World;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_CastleMaster;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Npc;

public class L1CrownInstance extends L1NpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public L1CrownInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onAction(L1PcInstance player) {		
		boolean in_war = false;
		if (player.getClanid() == 0) { // ũ���̼Ҽ�
			return;
		}
		String playerClanName = player.getClanname();
		L1Clan clan = L1World.getInstance().getClan(playerClanName);
		if (clan == null) {
			return;
		}
		if (!player.isCrown()) { // ���� �̿�
			return;
		}
		if (player.getGfxId().getTempCharGfx() != 0 && // ������
				player.getGfxId().getTempCharGfx() != 1) {
			return;
		}
		if (player.getId() != clan.getLeaderId()) // ������ �̿�
			return;

		if (!checkRange(player)) // ũ����� 1 �� �̳�
			return;

		if (clan.getCastleId() != 0) {// ���� ũ��
			player.sendPackets(new S_ServerMessage(474));// ����� ���� ���� �����ϰ� �����Ƿ�, �ٸ� �÷θ� ���� �� �����ϴ�.
			return;
		}

		// ũ����� ��ǥ�κ��� castle_id�� ���
		int castle_id = L1CastleLocation.getCastleId(getX(), getY(), getMapId());

		// �����ϰ� ������ üũ.��, ���ְ� ���� ���� ���� �ҿ�
		boolean existDefenseClan = false;
		L1Clan defence_clan = null;
		for (L1Clan defClan : L1World.getInstance().getAllClans()) {
			if (castle_id == defClan.getCastleId()) {
				// ���� ���� ũ��
				defence_clan = L1World.getInstance().getClan(defClan.getClanName());
				existDefenseClan = true;
				break;
			}
		}
		List<L1War> wars = L1World.getInstance().getWarList(); // ������ ����Ʈ�� ���
		for (L1War war : wars) {
			if (castle_id == war.GetCastleId()) { // �̸��̼��� ����
				in_war = war.CheckClanInWar(playerClanName);
				break;
			}
		}
		if (existDefenseClan && in_war == false) { // ���ְ� �־�, �����ϰ� ���� �ʴ� ���
			return;
		}

		if(player.isDead()){ return; }
		new L1SkillUse().handleCommands(player, 78, player.getId(), player.getX(), player.getY(), null, 0, L1SkillUse.TYPE_GMBUFF); //�߰�
		// clan_data�� hascastle�� ������, ĳ���Ϳ� ũ����� ���δ�
		if (existDefenseClan && defence_clan != null) { // ���� ���� ũ���� �ִ�
			defence_clan.setCastleId(0);
			ClanTable.getInstance().updateClan(defence_clan);
			L1PcInstance defence_clan_member[] = defence_clan.getOnlineClanMember();
			for (int m = 0; m < defence_clan_member.length; m++) {
				if (defence_clan_member[m].getId() == defence_clan.getLeaderId()) { // ���� ���� ũ���� ����
					defence_clan_member[m].sendPackets(new S_CastleMaster(0, defence_clan_member[m].getId()));
					//					Broadcaster.broadcastPacket(defence_clan_member[m], new S_CastleMaster(0, defence_clan_member[m].getId()));
					L1World.getInstance().broadcastPacketToAll(new S_CastleMaster(0, defence_clan_member[m].getId()));
					break;
				}
			}
		}
		clan.setCastleId(castle_id);
		ClanTable.getInstance().updateClan(clan);
		player.sendPackets(new S_CastleMaster(castle_id, player.getId()));
		//Broadcaster.broadcastPacket(player, new S_CastleMaster(castle_id, player.getId()));
		L1World.getInstance().broadcastPacketToAll(new S_CastleMaster(castle_id, player.getId()));

		// ũ���� �ܸ̿� �Ÿ��� ���� �ڷ���Ʈ
		int[] loc = new int[3];
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if (pc.getClanid() != player.getClanid() && !pc.isGm()) {
				if (L1CastleLocation.checkInWarArea(castle_id, pc)) {
					// �⳻�� �ִ�
					loc = L1CastleLocation.getGetBackLoc(castle_id);
					int locx = loc[0];
					int locy = loc[1];
					short mapid = (short) loc[2];
					L1Teleport.teleport(pc, locx, locy, mapid, pc.getMoveState().getHeading(), true, 0, false);
				}
			}
		}

		// �޼��� ǥ��
		for (L1War war : wars) {
			if (war.CheckClanInWar(playerClanName) && existDefenseClan) {
				// ��ũ���� �����߿���, ���ְ� ����
				war.WinCastleWar(playerClanName);
				break;
			}
		}

		if (clan.getOnlineClanMember().length > 0) {
			// ���� �����߽��ϴ�.
			S_ServerMessage s_serverMessage = new S_ServerMessage(643);
			for (L1PcInstance pc : clan.getOnlineClanMember()) {
				pc.sendPackets(s_serverMessage);
			}
		}

		deleteMe();

		for (L1TowerInstance lt : L1World.getInstance().getAllTower()) {
			if (L1CastleLocation.checkInWarArea(castle_id, lt)) {
				lt.deleteMe();
			}
		}
		// Ÿ���� spawn �Ѵ�
		L1WarSpawn warspawn = new L1WarSpawn();
		warspawn.SpawnTower(castle_id);

		/*		for (L1DoorInstance door : DoorSpawnTable.getInstance().getDoorList()) {
			if (L1CastleLocation.checkInWarArea(castle_id, door)) {
				door.repairGate();
			}
		}*/		
	}

	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		allTargetClear();
		_master = null;
		L1World.getInstance().removeVisibleObject(this);
		L1World.getInstance().removeObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.getNearObjects().removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		getNearObjects().removeAllKnownObjects();
	}

	private boolean checkRange(L1PcInstance pc) {
		return (getX() - 1 <= pc.getX() && pc.getX() <= getX() + 1
				&& getY() - 1 <= pc.getY() && pc.getY() <= getY() + 1);
	}
}
