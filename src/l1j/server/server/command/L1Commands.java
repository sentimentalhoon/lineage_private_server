/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.   See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.templates.L1Command;
import l1j.server.server.utils.SQLUtil;

public class L1Commands {
	private static Logger _log = Logger.getLogger(L1Commands.class.getName());

	private static L1Command fromResultSet(ResultSet rs) throws SQLException {
		return new L1Command(rs.getString("name"), rs.getInt("access_level"),
				rs.getString("class_name"));
	}

	public static L1Command get(String name) {
		/*
		 * ����׳� �׽�Ʈ ���̼��� ���� ��ȸ DB�� ������ ���ϴ�.  ĳ���ϴ� �ͺ��� �̷л� �����ս��� �����ϴٸ�, ������ �� �ִ� �����Դϴ�.
		 */
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance(). getConnection();
			pstm = con.prepareStatement("SELECT * FROM commands WHERE name=? ");
			pstm.setString(1, name);
			rs = pstm.executeQuery();
			if (! rs.next()) {
				return null;
			}
			return fromResultSet(rs);
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "Ŀ��� ��� ����", e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return null;
	}

	public static List<L1Command> availableCommandList(int accessLevel) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		List<L1Command> result = new ArrayList<L1Command>();
		try {
			con = L1DatabaseFactory.getInstance(). getConnection();
			pstm = con
					. prepareStatement("SELECT * FROM commands WHERE access_level <= ? ");
			pstm.setInt(1, accessLevel);
			rs = pstm.executeQuery();
			while (rs.next()) {
				result.add(fromResultSet(rs));				
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "Ŀ��� ��� ����", e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return result;
	}
}
