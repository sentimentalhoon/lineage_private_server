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
	 * spr_action ���̺��� �ε��Ѵ�.
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
				case ACTION_SwordAttack://�Ѽհ�
				case ACTION_AxeAttack://����
				case ACTION_BowAttack://Ȱ
				case ACTION_SpearAttack://â
				case ACTION_StaffAttack://������
				case ACTION_DaggerAttack://�ܰ�
				case ACTION_TwoHandSwordAttack://��հ�
				case ACTION_EdoryuAttack://�̵���
				case ACTION_ClawAttack://ũ�ο�
				case ACTION_ThrowingKnifeAttack://�ǵ鷿
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
		_log.config("SPR ������ " + _dataMap.size() + "�� �ε�");
	}

	/**
	 * ������ spr�� ���� �ӵ��� �����ش�. ���� spr�� ������ weapon_type�� �����Ͱ� �����Ǿ� ���� ���� ����, 1. attack�� �����͸� �����ش�.
	 * 
	 * @param sprid -
	 *            �����ϴ� spr�� ID
	 * @param actid -
	 *            ������ ������ ��Ÿ���� ��. L1Item.getType1()�� ��ȯ�� +1�� ��ġ�Ѵ�
	 * @return ������ spr�� ���� �ӵ�(ms)
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
