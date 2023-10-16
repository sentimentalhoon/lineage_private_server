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

import java.util.EnumMap;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.datatables.SprTable;
import l1j.server.server.model.Instance.L1PcInstance;

/**
 * 가속기의 사용을 체크하는 클래스.
 */
public class AcceleratorChecker {

	private static final Logger _log = Logger.getLogger(AcceleratorChecker.class.getName());

	private final L1PcInstance _pc;

	private int _injusticeCount;

	private int _justiceCount;

	private static final int INJUSTICE_COUNT_LIMIT = Config.INJUSTICE_COUNT;

	private static final int JUSTICE_COUNT_LIMIT = Config.JUSTICE_COUNT;

	// 실제로는 이동과 공격의 패킷 간격은 spr의 이론치보다5%만큼 늦다.
	// 그것을 고려해―5로 하고 있다.
	private static final double CHECK_STRICTNESS = (Config.CHECK_STRICTNESS - 5) / 100D;

	private static final double HASTE_RATE = 0.75;

	private static final double WAFFLE_RATE = 0.87;

	private static final double DOUBLE_HASTE_RATE = 0.375;

	private final EnumMap<ACT_TYPE, Long> _actTimers =	new EnumMap<ACT_TYPE, Long>(ACT_TYPE.class);

	private final EnumMap<ACT_TYPE, Long> _checkTimers = new EnumMap<ACT_TYPE, Long>(ACT_TYPE.class);

	// 체크의 결과
	public static enum ACT_TYPE {
		MOVE, ATTACK, SPELL_DIR, SPELL_NODIR
	}
	public static final int R_OK = 0;

	public static final int R_DETECTED = 1;

	public static final int R_DISCONNECTED = 2;

	public AcceleratorChecker(L1PcInstance pc) {
		_pc = pc;
		_injusticeCount = 0;
		_justiceCount = 0;
		long now = System.currentTimeMillis();
		for (ACT_TYPE each : ACT_TYPE.values()) {
			_actTimers.put(each, now);
			_checkTimers.put(each, now);
		}
	}

	/**
	 * 액션의 간격이 부정하지 않을까 체크해, 적당 처리를 실시한다.
	 * 
	 * @param type -
	 *            체크하는 액션의 타입
	 * @return 문제가 없었던 경우는 0, 부정할 경우는 1, 부정 동작이 일정 회수에 이르렀기 때문에 플레이어를 절단 했을 경우는 2를 돌려준다.
	 */
	public int checkInterval(ACT_TYPE type) {
		int result = R_OK;
		long now = System.currentTimeMillis();
		long interval = now - _actTimers.get(type);
		int rightInterval = getRightInterval(type);

		interval *= CHECK_STRICTNESS;
		if (_pc.isGm()){
			return R_OK;
		}

		if (_pc.getGfxId().getTempCharGfx() == 6284){	// 유령의집호박
			_injusticeCount = 0;
			_justiceCount = 0;
			return R_OK;
		}
		if (0 < interval && interval < rightInterval) {			
			_injusticeCount++;
			_justiceCount = 0;
			if (_injusticeCount >= INJUSTICE_COUNT_LIMIT) {
				return R_DISCONNECTED;
			}
			result = R_DETECTED;
		} else if (interval >= rightInterval) {
			_justiceCount++;
			if (_justiceCount >= JUSTICE_COUNT_LIMIT) {
				_injusticeCount = 0;
				_justiceCount = 0;
			}
		}
		// 검증용
		//		double rate = (double) interval / rightInterval;
		//		System.out.println(String.format("%s: %d / %d = %.2f (o-%d x-%d)",
		//		type.toString(), interval, rightInterval, rate,
		//		_justiceCount, _injusticeCount));

		_actTimers.put(type, now);
		return result;
	}

	/**
	 * PC 상태로부터 지정된 종류의 액션의 올바른 인터벌(ms)을 계산해, 돌려준다.
	 * 
	 * @param type -
	 *            액션의 종류
	 * @param _pc -
	 *            조사하는 PC
	 * @return 올바른 인터벌(ms)
	 */
	private int getRightInterval(ACT_TYPE type) {
		int interval;
		switch (type) {
		case ATTACK:
			interval = SprTable.getInstance().getAttackSpeed(
					_pc.getGfxId().getTempCharGfx(), _pc.getCurrentWeapon() + 1);
			break;
		case MOVE:
			interval = SprTable.getInstance().getMoveSpeed(
					_pc.getGfxId().getTempCharGfx(), _pc.getCurrentWeapon());
			break;
		case SPELL_DIR:
			interval = SprTable.getInstance().getDirSpellSpeed(
					_pc.getGfxId().getTempCharGfx());
			break;
		case SPELL_NODIR:
			interval = SprTable.getInstance().getNodirSpellSpeed(
					_pc.getGfxId().getTempCharGfx());
			break;
		default:
			return 0;
		}
		switch (_pc.getMoveState().getMoveSpeed()) {
		case 1: 
			interval *= HASTE_RATE;
			break;
		case 2: 
			interval /= HASTE_RATE;
			break;
		default:
			break;
		}

		switch (_pc.getMoveState().getBraveSpeed()) {
		case 1: 
			interval *= HASTE_RATE; // 1.33
			break;
		case 3: 
			interval *= WAFFLE_RATE; // 1.15
			break;
		case 4: 
			if (type.equals(ACT_TYPE.MOVE)) {
				interval *= HASTE_RATE; // 1.33
			}
			break;
		case 5: 
			interval *= DOUBLE_HASTE_RATE; // 2.66
			break;
		case 6:
			if (type.equals(ACT_TYPE.ATTACK)) {
				interval *= HASTE_RATE; // 1.33
			}
			break;
		default:
			break;
		}
		if (_pc.isBrave() && type.equals(ACT_TYPE.MOVE)) { 
			interval *= WAFFLE_RATE;
		}
		if (_pc.isHaste()) {
			interval *= HASTE_RATE;
		}
		if (_pc.isThirdSpeed()) {
			interval *= WAFFLE_RATE;
		}
		if (type.equals(ACT_TYPE.MOVE) && _pc.isFastMovable()
				|| type.equals(ACT_TYPE.MOVE) && _pc.isUgdraFruit()) {
			interval *= HASTE_RATE;
		}

		if (_pc.isBloodLust()){ // 블러드러스트			
			interval *= HASTE_RATE;
		}

		if (_pc.isElfBrave()) {
			interval *= WAFFLE_RATE;
		}

		if (_pc.isWindShackle() && !type.equals(ACT_TYPE.MOVE)) { 
			interval /= 2;
		}
		if (_pc.getMapId() == 5143) { 
			interval *= 0.1;
		}
		return interval;
	}
}
