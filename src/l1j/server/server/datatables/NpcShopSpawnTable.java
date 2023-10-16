package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.templates.L1NpcShop;
import l1j.server.server.utils.SQLUtil;

public class NpcShopSpawnTable {
	private static Logger _log = Logger.getLogger(NpcShopSpawnTable.class.getName());

	private static NpcShopSpawnTable _instance;
	
	private ArrayList<L1NpcShop> npcShoplist = new ArrayList<L1NpcShop>();

	public static NpcShopSpawnTable getInstance() {
		if (_instance == null) {
			_instance = new NpcShopSpawnTable();
		}
		return _instance;
	}

	private NpcShopSpawnTable() {
		lode();
	}

	public void lode() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_npc_shop");
			rs = pstm.executeQuery();
			do {
				if (!rs.next()) {
					break;
				}
				
				L1NpcShop shop = new L1NpcShop();

				shop.setNpcId(rs.getInt("npc_id"));
				shop.setName(rs.getString("name"));
				shop.setX(rs.getInt("locx"));
				shop.setY(rs.getInt("locy"));
				shop.setMapId(rs.getShort("mapid"));
				shop.setHeading(rs.getInt("heading"));
				shop.setTitle(rs.getString("title"));
				shop.setShopName(rs.getString("shop_name"));
				
				npcShoplist.add(shop);
				shop = null;
				
			} while (true);
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (SecurityException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalArgumentException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public ArrayList<L1NpcShop> getList(){
		return npcShoplist;
	}

}
