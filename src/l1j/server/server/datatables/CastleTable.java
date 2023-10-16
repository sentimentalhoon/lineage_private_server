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
import java.sql.Timestamp;
import java.text.SimpleDateFormat; //## A1 war_time ���� ���� ���� ����Ʈ �߰� 
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.templates.L1Castle;
import l1j.server.server.utils.SQLUtil;


// Referenced classes of package l1j.server.server:
// IdFactory

public class CastleTable {

	private static Logger _log = Logger.getLogger(CastleTable.class.getName());

	private static CastleTable _instance;

	private final Map<Integer, L1Castle> _castles = new ConcurrentHashMap<Integer, L1Castle>();

	public static CastleTable getInstance() {
		if (_instance == null) {
			_instance = new CastleTable();
		}
		return _instance;
	}
	public static void reload() {
		CastleTable oldInstance = _instance;
		_instance = new CastleTable();
		oldInstance._castles.clear();
	}

	private CastleTable() {
		load();
	}

	private Calendar timestampToCalendar(Timestamp ts) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts.getTime());
		return cal;
	}

	private void load() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM castle");

			rs = pstm.executeQuery();
			L1Castle castle = null;
			while (rs.next()) {
				castle = new L1Castle(rs.getInt(1), rs.getString(2));
				castle.setWarTime(timestampToCalendar((Timestamp) rs.getObject(3)));
				castle.setTaxRate(rs.getInt(4));
				castle.setPublicMoney(rs.getInt(5));
				castle.setPublicReadyMoney(rs.getInt(6));
				castle.setShowMoney(rs.getInt(7));
				castle.setWarBaseTime(rs.getInt(8));
				castle.setCastleSecurity(rs.getInt(9));
//				castle.setTimeBase(timestampToCalendar((Timestamp) rs
//						.getObject(9)));

				_castles.put(castle.getId(), castle);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public L1Castle[] getCastleTableList() {
		return _castles.values().toArray(new L1Castle[_castles.size()]);
	}

	public L1Castle getCastleTable(int id) {
		return _castles.get(id);
	}

	public void updateCastle(L1Castle castle) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			int money;
			if (castle.getPublicMoney() > 500000000){
				money = 500000000;
			}else{
				money = castle.getPublicMoney();
			}

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE castle SET name=?, war_time=?, tax_rate=?, public_money=?, public_ready_money=?, show_money=?, war_basetime=?, security=? WHERE castle_id=?");
			pstm.setString(1, castle.getName());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String fm = sdf.format(castle.getWarTime().getTime());
			//			String fm = DateFormat.getDateTimeInstance().format( //## A1 ���� 
			//					castle.getWarTime().getTime()); //#
			pstm.setString(2, fm);
			pstm.setInt(3, castle.getTaxRate());
			//	pstm.setInt(4, castle.getPublicMoney());
			pstm.setInt(4, money);

			pstm.setInt(5, castle.getPublicReadyMoney());
			pstm.setInt(6, castle.getShowMoney());
			pstm.setInt(7, castle.getWarBaseTime());
			//			String ft = sdf.format(castle.getTimeBase().getTime());
			//			pstm.setString(8, ft);
			pstm.setInt(8, castle.getCastleSecurity());
			pstm.setInt(9, castle.getId());
			pstm.execute();

			_castles.put(castle.getId(), castle);
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
