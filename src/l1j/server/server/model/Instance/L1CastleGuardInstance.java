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

import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1NpcTalkData;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.types.Point;

public class L1CastleGuardInstance extends L1NpcInstance {
	private static final long serialVersionUID = 1L;
	
	private boolean isAttackClan = false;
	
	@Override
	public void searchTarget() {
		L1PcInstance targetPlayer = null;

		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(this)) {

			if (pc.getCurrentHp() <= 0 || pc.isDead() || pc.isGm() || pc.isGhost()) {
				continue;
			}
			if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) {
				for (L1War war : L1World.getInstance().getWarList()) {
					boolean isInWar = war.CheckClanInWar(pc.getClanname());
					isAttackClan = war.CheckAttackClan(pc.getClanname());
					if (isInWar && isAttackClan) {
						targetPlayer = pc;
						break;
					}
				}
			}
		}
		
		if (targetPlayer != null) {
			int castleId = 0;
			castleId = L1CastleLocation.getCastleIdByArea(targetPlayer);
			boolean isNowWar = WarTimeController.getInstance().isNowWar(castleId);
			if (castleId != 0 && isNowWar && !isAttackClan) {
				targetPlayer = null;
			}
		}
	
		if (targetPlayer != null) {
			setTarget(targetPlayer);
		}
	}

	public void setTarget(L1PcInstance targetPlayer) {
		if (targetPlayer != null) {
			_hateList.add(targetPlayer, 0);
			_target = targetPlayer;
		}
	}

	@Override
	public boolean noTarget() {
		if (getLocation()
				.getTileLineDistance(new Point(getHomeX(), getHomeY())) > 0) {
			int dir = moveDirection(getHomeX(), getHomeY());
			if (dir != -1) {
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			} else {
				teleport(getHomeX(), getHomeY(), 1);
			}
		} else {
			if (L1World.getInstance().getRecognizePlayer(this).size() == 0) {
				return true;
			}
		}
		return false;
	}

	public L1CastleGuardInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onNpcAI() {
		if (isAiRunning()) {
			return;
		}
		setActived(false);
		startAI();
	}

	@Override
	public void onAction(L1PcInstance pc) {
		if (!isDead()) {
			if (getCurrentHp() > 0) {
				L1Attack attack = new L1Attack(pc, this);
				if (attack.calcHit()) {
					attack.calcDamage();
				}
				attack.action();
				attack.commit();
			} else {
				L1Attack attack = new L1Attack(pc, this);
				attack.calcHit();
				attack.action();
			}
		}
	}

	@Override
	public void onTalkAction(L1PcInstance player) {
		int objid = getId();
		L1NpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());
		int npcid = getNpcTemplate().get_npcId();
		String htmlid = null;
		String[] htmldata = null;
		boolean hascastle = false;
		String clan_name = "";
		String pri_name = "";

		if (talking != null) {
			if (npcid == 70549 || npcid == 70985 || npcid == 70656) { 
				hascastle = checkHasCastle(player, L1CastleLocation.KENT_CASTLE_ID);
				if (hascastle && player.getClanRank() >= 3) {
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
			} else if (npcid == 70600 || 
					npcid == 70986) {
				hascastle = checkHasCastle(player, L1CastleLocation.OT_CASTLE_ID);
				if (hascastle && player.getClanRank() >= 3) {
					htmlid = "orckeeper";
				} else {
					htmlid = "orckeeperop";
				}
			} else if (npcid == 70687 || npcid == 70987 || npcid == 70778) {
				hascastle = checkHasCastle(player, L1CastleLocation.WW_CASTLE_ID);
				if (hascastle && player.getClanRank() >= 3) { 
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
			}else if (npcid == 70800 ||	npcid == 70988 || npcid == 70989 || npcid == 70990
					|| npcid == 70991 || npcid == 70817) {
				hascastle = checkHasCastle(player, L1CastleLocation.GIRAN_CASTLE_ID);
				if (hascastle && player.getClanRank() >= 3) { 
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
			} else if (npcid == 70862 
					|| npcid == 70992 || npcid == 70863) {
				hascastle = checkHasCastle(player, L1CastleLocation.HEINE_CASTLE_ID);
				if (hascastle && player.getClanRank() >= 3) { 
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
			} else if (npcid == 70993 || 
					npcid == 70994 || npcid == 70995) {
				hascastle = checkHasCastle(player, L1CastleLocation.DOWA_CASTLE_ID);
				if (hascastle && player.getClanRank() >= 3) { 
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
			} else if (npcid == 70996) { 
				hascastle = checkHasCastle(player, L1CastleLocation.ADEN_CASTLE_ID);
				if (hascastle && player.getClanRank() >= 3) { 
					htmlid = "gatekeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
			} else if (npcid == 60514) { 
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() == L1CastleLocation.KENT_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "ktguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			} else if (npcid == 60560) { 
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() == L1CastleLocation.OT_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "orcguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			} else if (npcid == 60552) {
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() == L1CastleLocation.WW_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "wdguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			} else if (npcid == 60524 || npcid == 60525 || npcid == 60529) {
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() == L1CastleLocation.GIRAN_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "grguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			} else if (npcid == 70857|| npcid == 4710001) { 
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() == L1CastleLocation.HEINE_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "heguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			} else if (npcid == 60530 || npcid == 60531) {
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() == L1CastleLocation.DOWA_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "dcguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			} else if (npcid == 60533 || npcid == 60534 || npcid == 60535 || npcid == 60536) {
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() == L1CastleLocation.ADEN_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "adguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			} else if (npcid == 81156) {
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId()== L1CastleLocation.DIAD_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "ktguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
			}
			
			if (htmlid != null) { 
				if (htmldata != null) {
					player.sendPackets(new S_NPCTalkReturn(objid, htmlid,
							htmldata));
				} else {
					player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
				}
			} else {
				if (player.getLawful() < -1000) {
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
				} else {
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
				}
			}
		}
	}

	public void onFinalAction() {

	}

	public void doFinalAction() {

	}

	@Override
	public void receiveDamage(L1Character attacker, int damage) {
		if (getCurrentHp() > 0 && !isDead()) {
			if (damage >= 0) {
				if (!(attacker instanceof L1EffectInstance)) { 
					setHate(attacker, damage);
				}
			}
			if (damage > 0) {
				if(getSkillEffectTimerSet().hasSkillEffect(L1SkillId.FOG_OF_SLEEPING)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.FOG_OF_SLEEPING);
				}else if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.PHANTASM);
				}
			}

			onNpcAI();

			if (attacker instanceof L1PcInstance && damage > 0) {
				L1PcInstance pc = (L1PcInstance) attacker;
				pc.setPetTarget(this);
			}

			int newHp = getCurrentHp() - damage;
			if (newHp <= 0 && !isDead()) {
				setCurrentHp(0);
				setDead(true);
				setActionStatus(ActionCodes.ACTION_Die);
				Death death = new Death(attacker);
				GeneralThreadPool.getInstance().execute(death);
			}
			if (newHp > 0) {
				setCurrentHp(newHp);
			}
		} else if (!isDead()) { 
			setDead(true);
			setActionStatus(ActionCodes.ACTION_Die);
			Death death = new Death(attacker);
			GeneralThreadPool.getInstance().execute(death);
		}
	}
	
	@Override
	public void checkTarget(){
		//System.out.println("�������̵� �ߴµ� ���� �����ұ�?");
		if (_target == null
				|| 	(Math.abs(this.getX() - this.getHomeX())) > 20 
				||  (Math.abs(this.getY() - this.getHomeY())) > 20
				|| _target.getMapId() != getMapId()
				|| _target.getCurrentHp() <= 0
				|| _target.isDead()
				|| (_target.isInvisble() && !getNpcTemplate().is_agrocoi() && !_hateList
						.containsKey(_target))) {
			if (_target != null) {
				tagertClear();
				teleport(getHomeX(), getHomeY(),getSpawn().getHeading());			
			}
			if (!_hateList.isEmpty()) {
				_target = _hateList.getMaxHateCharacter();
				checkTarget();
			}
		}
	}
	
	class Death implements Runnable {
		L1Character _lastAttacker;

		public Death(L1Character lastAttacker) {
			_lastAttacker = lastAttacker;
		}

		@Override
		public void run() {
			setDeathProcessing(true);
			setCurrentHp(0);
			setDead(true);
			setActionStatus(ActionCodes.ACTION_Die);

			getMap().setPassable(getLocation(), true);

			Broadcaster.broadcastPacket(L1CastleGuardInstance.this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
			startChat(CHAT_TIMING_DEAD);
			setDeathProcessing(false);
			allTargetClear();
			startDeleteTimer();
		}
	}

	private boolean checkHasCastle(L1PcInstance pc, int castleId) {
		boolean isExistDefenseClan = false;
		for (L1Clan clan : L1World.getInstance().getAllClans()) {
			if (castleId == clan.getCastleId()) {
				isExistDefenseClan = true;
				break;
			}
		}
		if (!isExistDefenseClan) {
			return true;
		}

		if (pc.getClanid() != 0) {
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (clan.getCastleId() == castleId) {
					return true;
				}
			}
		}
		return false;
	}

}