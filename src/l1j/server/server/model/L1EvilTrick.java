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
package l1j.server.server.model;

import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_EffectLocation;

public class L1EvilTrick extends TimerTask {
	private static Logger _log = Logger.getLogger(L1EvilTrick.class.getName());
	private ScheduledFuture<?> _trickfuture = null;
	private int _timeCounter = 0;
	private final L1PcInstance _pc;
	private final L1Character _cha;
	private static final Random _random = new Random();
	public L1EvilTrick(L1PcInstance pc, L1Character cha) {
		_cha = cha;
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_cha == null || _cha.isDead()) {
				stop();
				return;
			}
			attack();
			_timeCounter++;
			if (_timeCounter >= 3) {
				stop();
				return;
			}
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void begin() {
		// 8bA4bXLl1
		// Jn0.9b
		_trickfuture = GeneralThreadPool.getInstance().scheduleAtFixedRate(this, 0, 1000);
	}

	public void stop() {
		if (_trickfuture != null) {
			_trickfuture.cancel(false);
		}
	}

	public void attack() {
		double _drainMana = getDamage(_pc, _cha);
		S_EffectLocation packet = new S_EffectLocation(_cha.getX(), _cha.getY(), 8152);
		_pc.sendPackets(packet);
		_pc.broadcastPacket(packet);
		if (_cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) _cha;
			pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Damage));
			pc.broadcastPacket(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Damage));
			if (_drainMana > 0 && pc.getCurrentMp() > 0) {
				if (_drainMana > pc.getCurrentMp()) {
					_drainMana = pc.getCurrentMp();
				}
				short newMp = (short) (pc.getCurrentMp() - _drainMana);
				pc.setCurrentMp(newMp);
				newMp = (short) (_pc.getCurrentMp() + _drainMana);
				_pc.setCurrentMp(newMp);
			}
		} else if(_cha instanceof L1NpcInstance) {
			L1NpcInstance npc = (L1NpcInstance) _cha;
			npc.broadcastPacket(new S_DoActionGFX(npc.getId(), ActionCodes.ACTION_Damage));
			if (_drainMana > 0) {
				int drainValue = npc.drainMana((int) _drainMana);
				int newMp = _pc.getCurrentMp() + drainValue;
				_pc.setCurrentMp(newMp);
				if (drainValue > 0) {
					int newMp2 = npc.getCurrentMp() - drainValue;
					npc.setCurrentMp(newMp2);
				}
			}
		}
	}

	public double getDamage(L1PcInstance pc, L1Character cha) {
		double dmg = 0;
		int dice = pc.getWeapon().getEnchantLevel() + 1;
		int EveilDamage;
		EveilDamage = (_random.nextInt(dice) + 5);
		dmg = EveilDamage; 
		return dmg;
	}
}