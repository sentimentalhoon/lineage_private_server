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
package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import static l1j.server.server.ActionCodes.*;
import l1j.server.server.utils.SQLUtil;

public class UserSpeedControlTable {

	private static final Logger _log = Logger.getLogger(UserSpeedControlTable.class.getName());

	private static class Spr {
		private final HashMap<Integer, Integer> moveSpeed = new HashMap<Integer, Integer>();

		private final HashMap<Integer, Integer> attackSpeed = new HashMap<Integer, Integer>();
	}

	private static final HashMap<Integer, Spr> _dataMap = new HashMap<Integer, Spr>();
	
	private static final UserSpeedControlTable _instance = new UserSpeedControlTable();

	private UserSpeedControlTable() {
		loadSprAction();
	}

	public static UserSpeedControlTable getInstance() {
		return _instance;
	}

	/**
	 * spr_action 테이블을 로드한다.
	 */
	public void loadSprAction() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Spr spr = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM user_speed_control");
			rs = pstm.executeQuery();
			while (rs.next()) {
				int key = rs.getInt("poly_id");
				if (!_dataMap.containsKey(key)) {
					spr = new Spr();
					_dataMap.put(key, spr);
				} else {
					spr = _dataMap.get(key);
				}

				int action_type = rs.getInt("action_type");
				int speed = rs.getInt("speed");

				switch (action_type) {
				case ACTION_Walk:
					spr.moveSpeed.put(action_type, speed);
					break;
				//case ACTION_SkillAttack:
				//	spr.dirSpellSpeed = speed;
				//	break;
				//case ACTION_SkillBuff:
				//	spr.nodirSpellSpeed = speed;
				//	break;
				case ACTION_Attack:
				case ACTION_SwordAttack://한손검
				case ACTION_AxeAttack://도끼
				case ACTION_BowAttack://활
				case ACTION_SpearAttack://창
				case ACTION_StaffAttack://지팡이
				case ACTION_DaggerAttack://단검
				case ACTION_TwoHandSwordAttack://양손검
				case ACTION_EdoryuAttack://이도류
				case ACTION_ClawAttack://크로우
				case ACTION_ThrowingKnifeAttack://건들렛
					spr.attackSpeed.put(action_type, speed);
					break;
				default:
					break;
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		_log.config("SPR 데이터 " + _dataMap.size() + "건 로드");
	}

	/**
	 * 지정된 spr의 공격 속도를 돌려준다. 만약 spr로 지정된 weapon_type의 데이터가 설정되어 있지 않은 경우는, 1. attack의 데이터를 돌려준다.
	 * 
	 * @param sprid -
	 *            조사하는 spr의 ID
	 * @param actid -
	 *            무기의 종류를 나타내는 값. L1Item.getType1()의 변환값 +1과 일치한다
	 * @return 지정된 spr의 공격 속도(ms)
	 */
	public int getAttackSpeed(int polyid, int acttype) {
		if (_dataMap.containsKey(polyid)) {
			if (_dataMap.get(polyid).attackSpeed.containsKey(acttype)) {
				return _dataMap.get(polyid).attackSpeed.get(acttype);
			}
		}
		return 0;
	}
	
	public void putAttackSpeed(int polyid, int acttype, int speed) {
		Spr spr = null;
		int key = polyid;
		if (!_dataMap.containsKey(key)) {
			spr = new Spr();
			_dataMap.put(key, spr);
		} else {
			spr = _dataMap.get(key);
		}
		spr.attackSpeed.put(acttype, speed);
	}

	public int getMoveSpeed(int sprid) {
		if (_dataMap.containsKey(sprid)) {
			return _dataMap.get(sprid).moveSpeed.get(0);
		}
		return 0;
	}
}
