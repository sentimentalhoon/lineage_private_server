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
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.templates.L1Pet;
import l1j.server.server.utils.SQLUtil;

// Referenced classes of package l1j.server.server:
// IdFactory

public class PetTable {

	private static Logger _log = Logger.getLogger(PetTable.class.getName());

	private static PetTable _instance;

	private final HashMap<Integer, L1Pet> _pets = new HashMap<Integer, L1Pet>();

	public static PetTable getInstance() {
		if (_instance == null) {
			_instance = new PetTable();
		}
		return _instance;
	}

	private PetTable() {
		load();
	}

	private void load() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM pets");

			rs = pstm.executeQuery();
			L1Pet pet  = null;
			while (rs.next()) {
				pet = new L1Pet();
				int itemobjid = rs.getInt(1);
				pet.set_itemobjid(itemobjid);
				pet.set_objid(rs.getInt(2));
				pet.set_npcid(rs.getInt(3));
				pet.set_name(rs.getString(4));
				pet.set_level(rs.getInt(5));
				pet.set_hp(rs.getInt(6));
				pet.set_mp(rs.getInt(7));
				pet.set_exp(rs.getInt(8));
				pet.set_lawful(rs.getInt(9));
				pet.set_food(rs.getInt(10));
				pet.set_foodtime(rs.getInt(11));

				_pets.put(new Integer(itemobjid), pet);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);

		}
	}

	public void storeNewPet(L1NpcInstance pet, int objid, int itemobjid) {
		L1Pet l1pet = new L1Pet();
		l1pet.set_itemobjid(itemobjid);
		l1pet.set_objid(objid);
		l1pet.set_npcid(pet.getNpcTemplate().get_npcId());
		l1pet.set_name(pet.getNpcTemplate().get_nameid());
		l1pet.set_level(pet.getNpcTemplate().get_level());
		l1pet.set_hp(pet.getMaxHp());
		l1pet.set_mp(pet.getMaxMp());
		l1pet.set_exp(750); 
		l1pet.set_lawful(0);
		l1pet.set_food(0);
		l1pet.set_foodtime(1200000);
		_pets.put(new Integer(itemobjid), l1pet);

		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO pets SET item_obj_id=?,objid=?,npcid=?,name=?,lvl=?,hp=?,mp=?,exp=?,lawful=?,food=?,foodtime=?");
			pstm.setInt(1, l1pet.get_itemobjid());
			pstm.setInt(2, l1pet.get_objid());
			pstm.setInt(3, l1pet.get_npcid());
			pstm.setString(4, l1pet.get_name());
			pstm.setInt(5, l1pet.get_level());
			pstm.setInt(6, l1pet.get_hp());
			pstm.setInt(7, l1pet.get_mp());
			pstm.setInt(8, l1pet.get_exp());
			pstm.setInt(9, l1pet.get_lawful());
			pstm.setInt(10, l1pet.get_food());
			pstm.setInt(11, l1pet.get_foodtime());

			pstm.execute();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);

		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);

		}
	}

	public void storePet(L1Pet pet) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE pets SET objid=?,npcid=?,name=?,lvl=?,hp=?,mp=?,exp=?,lawful=?,food=? WHERE item_obj_id=?");
			pstm.setInt(1, pet.get_objid());
			pstm.setInt(2, pet.get_npcid());
			pstm.setString(3, pet.get_name());
			pstm.setInt(4, pet.get_level());
			pstm.setInt(5, pet.get_hp());
			pstm.setInt(6, pet.get_mp());
			pstm.setInt(7, pet.get_exp());
			pstm.setInt(8, pet.get_lawful());
			pstm.setInt(9, pet.get_food());
			pstm.setInt(10, pet.get_itemobjid());
			pstm.execute();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public void storePetFoodTime(int id, int food, int foodtime) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE pets SET food=?,foodtime=? WHERE objid=?");
			pstm.setInt(1, food);
			pstm.setInt(2, foodtime);
			pstm.setInt(3, id);
			pstm.execute();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public void deletePet(int itemobjid) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			pstm.setInt(1, itemobjid);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);

		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		_pets.remove(itemobjid);
	}

	/**
	 * Pets ���̺� �̹� �̸��� �����ұ �����ش�.
	 * 
	 * @param nameCaseInsensitive
	 *            �����ϴ� �ֿϵ����� �̸�. �빮�� �ҹ����� ���̴� ���õȴ�.
	 * @return �̹� �̸��� �����ϸ� true
	 */
	public static boolean isNameExists(String nameCaseInsensitive) {
		String nameLower = nameCaseInsensitive.toLowerCase();
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			/*
			 * ���� �̸��� ã�´�. MySQL�� ����Ʈ�� case insensitive�� ����
			 * ���� LOWER�� �ʿ������, binary�� ����Ǿ��� ��쿡 �����.
			 */
			pstm = con.prepareStatement("SELECT item_obj_id FROM pets WHERE LOWER(name)=?");
			pstm.setString(1, nameLower);
			rs = pstm.executeQuery();
			if (!rs.next()) { // ���� �̸��� ������
				return false;
			}
			if (PetTypeTable.getInstance().isNameDefault(nameLower)) { // ����Ʈ�� �̸��̶�� �ߺ� �ϰ� ���� ������ �����Ѵ�
				return false;
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return true;
	}

	public L1Pet getTemplate(int itemobjid) {
		return _pets.get(new Integer(itemobjid));
	}

	public L1Pet[] getPetTableList() {
		return _pets.values().toArray(new L1Pet[_pets.size()]);
	}
}
