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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;

public final class MapsTable {
	private class MapData {
		public int startX = 0;
		public int endX = 0;
		public int startY = 0;
		public int endY = 0;
		public double monster_amount = 1;
		public double dropRate = 1;
		public boolean isUnderwater = false;
		public boolean markable = false;
		public boolean teleportable = false;
		public boolean escapable = false;
		public boolean isUseResurrection = false;
		public boolean isUsePainwand = false;
		public boolean isEnabledDeathPenalty = false;
		public boolean isTakePets = false;
		public boolean isRecallPets = false;
		public boolean isUsableItem = false;
		public boolean isUsableSkill = false;
	}

	private static Logger _log = Logger.getLogger(MapsTable.class.getName());

	private static MapsTable _instance;

	/**
	 * Key�� MAP ID, Value�� �ڷ���Ʈ ���� �÷��װ� �ݳ��Ǵ� HashMap
	 */
	private final Map<Integer, MapData> _maps = new HashMap<Integer, MapData>();

	/**
	 * ���Ӱ� MapsTable ������Ʈ�� ������, MAP�� �ڷ���Ʈ ���� �÷��׸� �о���δ�.
	 */
	private MapsTable() {
		loadMapsFromDatabase();
	}
	public static void reload() {
		MapsTable oldInstance = _instance;
		_instance = new MapsTable();
		oldInstance._maps.clear();
	}
	/**
	 * MAP�� �ڷ���Ʈ ���� �÷��׸� ����Ÿ���̽��κ��� �о�鿩, HashMap _maps�� �ݳ��Ѵ�.
	 */
	private void loadMapsFromDatabase() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM mapids");
			MapData data = null;
			for (rs = pstm.executeQuery(); rs.next();) {
				data = new MapData();
				int mapId = rs.getInt("mapid");
				// rs.getString("locationname");
				data.startX = rs.getInt("startX");
				data.endX = rs.getInt("endX");
				data.startY = rs.getInt("startY");
				data.endY = rs.getInt("endY");
				data.monster_amount = rs.getDouble("monster_amount");
				data.dropRate = rs.getDouble("drop_rate");
				data.isUnderwater = rs.getBoolean("underwater");
				data.markable = rs.getBoolean("markable");
				data.teleportable = rs.getBoolean("teleportable");
				data.escapable = rs.getBoolean("escapable");
				data.isUseResurrection = rs.getBoolean("resurrection");
				data.isUsePainwand = rs.getBoolean("painwand");
				data.isEnabledDeathPenalty = rs.getBoolean("penalty");
				data.isTakePets = rs.getBoolean("take_pets");
				data.isRecallPets = rs.getBoolean("recall_pets");
				data.isUsableItem = rs.getBoolean("usable_item");
				data.isUsableSkill = rs.getBoolean("usable_skill");

				_maps.put(new Integer(mapId), data);
			}

			_log.config("Maps " + _maps.size());
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	/**
	 * MapsTable�� �ν��Ͻ��� �����ش�.
	 * 
	 * @return MapsTable�� �ν��Ͻ�
	 */
	public static MapsTable getInstance() {
		if (_instance == null) {
			_instance = new MapsTable();
		}
		return _instance;
	}

	/**
	 * MAP���� X���� ��ǥ�� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * @return X���� ��ǥ
	 */
	public int getStartX(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return 0;
		}
		return _maps.get(mapId).startX;
	}

	/**
	 * MAP���� X���� ��ǥ�� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * @return X���� ��ǥ
	 */
	public int getEndX(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return 0;
		}
		return _maps.get(mapId).endX;
	}

	/**
	 * MAP���� Y���� ��ǥ�� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * @return Y���� ��ǥ
	 */
	public int getStartY(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return 0;
		}
		return _maps.get(mapId).startY;
	}

	/**
	 * MAP���� Y���� ��ǥ�� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * @return Y���� ��ǥ
	 */
	public int getEndY(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return 0;
		}
		return _maps.get(mapId).endY;
	}

	/**
	 * ���� monster�� ������ �����ش�
	 * 
	 * @param mapId
	 *            �����ϴ� ���� �� ID
	 * @return monster���� ����
	 */
	public double getMonsterAmount(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return 0;
		}
		return map.monster_amount;
	}

	/**
	 * ���� ��� ������ �����ش�
	 * 
	 * @param mapId
	 *            �����ϴ� ���� �� ID
	 * @return ��� ����
	 */
	public double getDropRate(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return 0;
		}
		return map.dropRate;
	}

	/**
	 * MAP��, �����ϱ �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * 
	 * @return �����̸� true
	 */
	public boolean isUnderwater(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isUnderwater;
	}

	/**
	 * MAP��, �ϸ�ũ �����Ѱ��� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * @return �ϸ�ũ �����ϸ� true
	 */
	public boolean isMarkable(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).markable;
	}

	/**
	 * MAP��, ���� �ڷ���Ʈ �����Ѱ��� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * @return �����ϸ� true
	 */
	public boolean isTeleportable(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).teleportable;
	}

	/**
	 * MAP��, MAP�� ���� �ڷ���Ʈ �����Ѱ��� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * @return �����ϸ� true
	 */
	public boolean isEscapable(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).escapable;
	}

	/**
	 * MAP��, restore �����Ѱ��� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * 
	 * @return restore �����ϸ� true
	 */
	public boolean isUseResurrection(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isUseResurrection;
	}

	/**
	 * MAP��, �����꽺 wand ��� �����Ѱ��� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * 
	 * @return �����꽺 wand ��� �����ϸ� true
	 */
	public boolean isUsePainwand(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isUsePainwand;
	}

	/**
	 * MAP��, �����䳪��Ƽ�� ����� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * 
	 * @return �����䳪��Ƽ�̸� true
	 */
	public boolean isEnabledDeathPenalty(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isEnabledDeathPenalty;
	}

	/**
	 * MAP��, �ֿϵ���������� ������ �� �� ����� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * 
	 * @return �ꡤ����� ������ �� �� �ִٸ� true
	 */
	public boolean isTakePets(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isTakePets;
	}

	/**
	 * MAP��, �ֿϵ���������� ȣ���� �� ����� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� MAP�� MAP ID
	 * 
	 * @return �ꡤ����� ȣ���� �� �ִٸ� true
	 */
	public boolean isRecallPets(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isRecallPets;
	}
	
	/**
	 * ����, �������� ����� �� ����� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� ���� �� ID
	 * 
	 * @return �������� ����� �� �ִٸ� true
	 */
	public boolean isUsableItem(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isUsableItem;
	}

	/**
	 * ����, ��ų�� ����� �� ����� �����ش�.
	 * 
	 * @param mapId
	 *            �����ϴ� ���� �� ID
	 * 
	 * @return ��ų�� ����� �� �ִٸ� true
	 */
	public boolean isUsableSkill(int mapId) {
		MapData map = _maps.get(mapId);
		if (map == null) {
			return false;
		}
		return _maps.get(mapId).isUsableSkill;
	}

}
