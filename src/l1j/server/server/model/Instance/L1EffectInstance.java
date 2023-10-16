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
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Magic;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.templates.L1Npc;

public class L1EffectInstance extends L1NpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int FW_DAMAGE_INTERVAL = 1000;
	private int CubeTime; //ť��ð�
	private L1PcInstance CubePc; //ť������
	private int Cube = 20;

	public L1EffectInstance(L1Npc template) {
		super(template);

		if (getNpcTemplate().get_npcId() == 81157) { // FW
			GeneralThreadPool.getInstance().schedule(new FwDamageTimer(this), 0);
		}
	}
	
	/** ť��� */
	public void setCubeTime(int CubeTime){	this.CubeTime = CubeTime;	}
	public boolean isCube(){	return CubeTime-- <= 0;	}

	public void setCubePc(L1PcInstance CubePc){	this.CubePc = CubePc;	}
	public L1PcInstance CubePc(){	return CubePc;	}

	public boolean Cube(){	return Cube-- <= 0;	}

	@Override
	public void onAction(L1PcInstance pc) {
	}

	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		allTargetClear();
		_master = null;
		this.setCubePc(null);
		L1World.getInstance().removeVisibleObject(this);
		L1World.getInstance().removeObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.getNearObjects().removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		getNearObjects().removeAllKnownObjects();
	}

	class FwDamageTimer implements Runnable {
		private L1EffectInstance _effect;

		public FwDamageTimer(L1EffectInstance effect) {
			_effect = effect;
		}

		@Override
		public void run() {
			L1PcInstance pc = null;
			L1Magic magic = null;
			L1MonsterInstance mob = null;
			while (!_destroyed) {
				try {
					for (L1Object objects : L1World.getInstance().getVisibleObjects(_effect, 0)) {
						if (objects instanceof L1PcInstance) {
							pc = (L1PcInstance) objects;
							if (pc.isDead()) {
								continue;
							}
							if (pc.getId() == _effect.CubePc().getId()){
								continue;
							}
							if (CharPosUtil.getZoneType(pc) == 1) {
								boolean isNowWar = false;
								int castleId = L1CastleLocation.getCastleIdByArea(pc);
								if (castleId > 0) {
									isNowWar = WarTimeController.getInstance().isNowWar(castleId);
								}
								if (!isNowWar) {
									continue;
								}
							}
							magic = new L1Magic(_effect, pc);
							int damage = magic.calcFireWallDamage();
							if (damage == 0) {
								continue;
							}
							pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Damage));
							Broadcaster.broadcastPacket(pc, new S_DoActionGFX(pc.getId(),ActionCodes.ACTION_Damage));							
							pc.receiveDamage(_effect, damage, false);
						} else if (objects instanceof L1MonsterInstance) {
							mob = (L1MonsterInstance) objects;
							if (mob.isDead()) {
								continue;
							}
							magic = new L1Magic(_effect, mob);
							int damage = magic.calcFireWallDamage();
							if (damage == 0) {
								continue;
							}
							Broadcaster.broadcastPacket(mob, new S_DoActionGFX(mob.getId(), ActionCodes.ACTION_Damage));
							mob.receiveDamage(_effect, damage);
						}
					}
					Thread.sleep(FW_DAMAGE_INTERVAL);
				} catch (InterruptedException ignore) {
					// ignore
				}
			}
		}
	}
	
}
