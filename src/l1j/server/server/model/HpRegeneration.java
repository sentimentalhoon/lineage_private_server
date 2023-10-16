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

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

import l1j.server.server.model.Instance.L1EffectInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.utils.CalcStat;

public class HpRegeneration extends TimerTask {

	private static Logger _log = Logger.getLogger(HpRegeneration.class.getName());

	private final L1PcInstance _pc;

	private int _regenMax = 0;

	private int _regenPoint = 0;

	private int _curPoint = 4;

	private static Random _random = new Random(System.nanoTime());

	public HpRegeneration(L1PcInstance pc) {
		_pc = pc;

		updateLevel();
	}

	public void setState(int state) {
		if (_curPoint < state) {
			return;
		}

		_curPoint = state;
	}

	@Override
	public void run() {
		try {
			int i = _pc.getMaxHp(); //hp,mp �����϶� ��ƽ����
			if (_pc.isDead() || _pc.noPlayerCK) {
				return;
			}
			if (_pc.getCurrentHp()==i){//hp,mp �����϶� ��ƽ����
				return; //hp,mp �����϶� ��ƽ����
			}

			_regenPoint += _curPoint;
			_curPoint = 4;

			synchronized (this) {
				if (_regenMax <= _regenPoint) {
					_regenPoint = 0;
					regenHp();
				}
			}
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void updateLevel() {
		final int lvlTable[] = new int[] { 30, 25, 20, 16, 14, 12, 11, 10, 9, 3, 2 };

		int regenLvl = Math.min(10, _pc.getLevel());
		if (30 <= _pc.getLevel() && _pc.isKnight()) {
			regenLvl = 11;
		}

		synchronized (this) {
			_regenMax = lvlTable[regenLvl - 1] * 4;
		}
	}

	public void regenHp() {
		if (_pc.isDead() || _pc.noPlayerCK) {
			return;
		}

		int maxBonus = 1;

		// CON ���ʽ�
		if (11 < _pc.getLevel() && 14 <= _pc.getAbility().getTotalCon()) {
			maxBonus = _pc.getAbility().getTotalCon() - 12;
			if (25 < _pc.getAbility().getTotalCon()) {
				maxBonus = 14;
			}
		}
		// ���̽� CON ���ʽ�
		int basebonus = CalcStat.calcBaseHpr(_pc.getType(), _pc.getAbility().getBaseCon());
		
		int equipHpr = _pc.getInventory().hpRegenPerTick();
		equipHpr += _pc.getHpr();
		int bonus = _random.nextInt(maxBonus) + 1;

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.NATURES_TOUCH)) {
			bonus += 15;
		}
		if (L1HouseLocation.isInHouse(_pc.getX(), _pc.getY(), _pc.getMapId())) {
			bonus += 5;
		}
		if (isInn(_pc)){
			bonus += 5;
		}
		if (L1HouseLocation.isRegenLoc(_pc, _pc.getX(), _pc.getY(), _pc.getMapId())) {
			bonus += 5;
		}

		boolean inLifeStream = false;
		if (isPlayerInLifeStream(_pc)) {
			inLifeStream = true;
			// ����� ����, ������ ���������� HPR+3�� ������?
			bonus += 3;
		}

		// ������ �߷��� üũ
		if (_pc.get_food() < 24 || isOverWeight(_pc)
				|| _pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BERSERKERS)) {
			bonus = 0;
			basebonus = 0;
			// ��� ���� HPR ������ ������, �߷��� ���� ����������, ������ ���� ������, �߷��� ������� ȿ���� ���´�
			if (equipHpr > 0) {
				equipHpr = 0;
			}
		}
	
		int newHp = _pc.getCurrentHp();
		newHp += bonus + equipHpr + basebonus;

		if (newHp < 1) {
			newHp = 1; // HPR ���� ��� ���� ����� ���� �ʴ´�
		}
		// ���߿����� ���� ó��
		// ������ �ó����� ���Ҹ� ���� �� ������ �Ҹ�
		if (isUnderwater(_pc)) {
			newHp -= 20;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // HP�� 0�� �Ǿ��� ���� ����Ѵ�.
				}
			}
		}
		// Lv50 ����Ʈ�� ����� ���� 1 F2F������ ���� ó��
		if (isLv50Quest(_pc) && !inLifeStream) {
			newHp -= 10;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // HP�� 0�� �Ǿ��� ���� ����Ѵ�.
				}
			}
		}
		// ������ ���������� ���� ó��
		if (_pc.getMapId() == 410 && !inLifeStream) {
			newHp -= 10;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // HP�� 0�� �Ǿ��� ���� ����Ѵ�.
				}
			}
		}

		if (!_pc.isDead()) {
			_pc.setCurrentHp(Math.min(newHp, _pc.getMaxHp()));
		}
	}

	private boolean isUnderwater(L1PcInstance pc) {
		// ���� ���� �����ΰ�, ������ �ູ �����̸�, ������ �ƴϸ� �����Ѵ�.
		if (pc.getInventory().checkEquipped(20207)) {
			return false;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_UNDERWATER_BREATH)) {
			return false;
		}
		if (pc.getInventory().checkEquipped(21048)
				&& pc.getInventory().checkEquipped(21049)
				&& pc.getInventory().checkEquipped(21050)) {
			return false;
		}

		return pc.getMap().isUnderwater();
	}

	private boolean isOverWeight(L1PcInstance pc) {
		// ��Ű��ƽũ����Ÿ������ ����, �Ƶ������̾� �����ΰ�
		// ��� �� �����̸�, �߷� �������� ������ �����Ѵ�.
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.EXOTIC_VITALIZE)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ADDITIONAL_FIRE)) {
			return false;
		}
		if (pc.getInventory().checkEquipped(20049)) {
			return false;
		}
		if (isInn(pc)) {
			return false;
		}

		return (120 <= pc.getInventory().getWeight240()) ? true : false;
	}

	private boolean isLv50Quest(L1PcInstance pc) {
		int mapId = pc.getMapId();
		return (mapId == 2000 || mapId == 2001) ? true : false;
	}

	/**
	 * ������ PC�� ������ �ó����� �������� �ִ��� üũ�Ѵ�
	 * 
	 * @param pc
	 *            PC
	 * @return true PC�� ������ �ó����� �������� �ִ� ���
	 */
	private static boolean isPlayerInLifeStream(L1PcInstance pc) {
		L1EffectInstance effect = null;
		for (L1Object object : pc.getNearObjects().getKnownObjects()) {
			if (object instanceof L1EffectInstance == false) {
				continue;
			}
			effect = (L1EffectInstance) object;
			if (effect.getNpcId() == 81169 && effect.getLocation().getTileLineDistance(pc.getLocation()) < 4) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isInn(L1PcInstance pc) {
		int mapId = pc.getMapId();
		return (mapId == 16384 || mapId == 16896 || mapId == 17408 || mapId == 17492
				|| mapId == 17820 || mapId == 17920 || mapId == 18432 || mapId == 18944 
				|| mapId == 19456 || mapId == 19968 || mapId == 20480 || mapId == 20992 || mapId == 621 
				|| mapId == 21504 || mapId == 22016 || mapId == 22528 || mapId == 23040 
				|| mapId == 23552 || mapId == 24064 || mapId == 24576 || mapId == 25088 ) ? true : false;
	}
}
