
package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.utils.SQLUtil;

public class AuctionBTable {
	private static Logger _log = Logger.getLogger(AuctionBTable.class
			.getName());
	private volatile static AuctionBTable _instance;
	private AuctionBTable() {
	}
	public static synchronized AuctionBTable getInstance() {

		if (_instance == null) {
			_instance = new AuctionBTable();
		}
		return _instance;
	}

	public void writeTopic(L1PcInstance player, String date, L1ItemInstance item ,int count) {
		int counts = 0;
		Connection con = null;
		PreparedStatement pstm1 = null;
		ResultSet rs = null;
		PreparedStatement pstm2 = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm1 = con
					.prepareStatement("SELECT * FROM AuctionB ORDER BY id DESC");
			rs = pstm1.executeQuery();
			if (rs.next()) {
				counts = rs.getInt("id");
			}
			pstm2 = con.prepareStatement("INSERT INTO AuctionB SET id=?, name=?, item_id=?, item_name=?, item_enchantlvl=?, count=?, AccountName=?, date=?, UpClick=?");
			pstm2.setInt(1, (counts + 1));
			pstm2.setString(2, player.getName());
			pstm2.setInt(3, item.getItemId());
			pstm2.setString(4, item.getName());
			pstm2.setInt(5, item.getEnchantLevel());
			pstm2.setInt(6, count);
			pstm2.setString(7, player.getAccountName());
			pstm2.setString(8, date);
			pstm2.setInt(9, item.getAttrEnchantLevel());
			pstm2.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm1);
			SQLUtil.close(pstm2);
			SQLUtil.close(con);
		}
	}
	/** 운영자 명령어로 사용할 메서드 **/
	public void boardclear() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM AuctionB");
			pstm.execute();
		} catch (SQLException e) {
			_log.info("게시판  리셋 에러4!");
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	public void deleteTopic(int number) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM AuctionB WHERE id=?");
			pstm.setInt(1, number);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
