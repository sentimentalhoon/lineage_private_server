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
import java.util.logging.Level;
import java.util.logging.Logger;

import server.Server;


import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;

// Referenced classes of package l1j.server.server:
// IdFactory

public class LetterTable {
	private static Logger _log = Logger.getLogger(LetterTable.class.getName());
	private volatile static LetterTable uniqueInstance = null;
	
	public LetterTable() {
	}

	public static LetterTable  getInstance() {
		if(uniqueInstance == null) {
			synchronized (Server.class) {
				if(uniqueInstance == null) {
					uniqueInstance = new LetterTable();
				}
			}
		}
		return uniqueInstance;
	}

	// ���ø� ID�϶�
	// 16:ĳ���Ͱ� �������� �ʴ´�
	// 32:���� �ʹ� ����
	// 48:������ �������� �ʴ´�
	// 64:�س����� ǥ�õ��� �ʴ´�(�����)
	// 80:�س����� ǥ�õ��� �ʴ´�(����)
	// 96:�س����� ǥ�õ��� �ʴ´�(����)
	// 112:�����մϴ�. %n����� ������ ��Ŵ� ���� ����%0�Ƶ����� �������� �����Ǿ����ϴ�.
	// 128:����� ���õ� �ݾ׺��� �� �� ��� �ݾ��� ������ (��)���� ��Ÿ���� ������, ������������ ������ �����߽��ϴ�.
	// 144:����� ������ ��Ŵ� �����߽��ϴٸ�, �������� ������ �� �ִ� ���¿� �����ϴ�.
	// 160:����� �����ϰ� �ִ� ���� ���� ����%1�Ƶ����� �����Ǿ����ϴ�.
	// 176:����� ��û �Ͻ� ��Ŵ�, ��� �Ⱓ���� ������ �ݾ� �̻󿡼��� ������ ǥ���ϴ� ���� ��Ÿ���� �ʾұ� (����)������, �ᱹ �����Ǿ����ϴ�.
	// 192:����� ��û �Ͻ� ��Ŵ�, ��� �Ⱓ���� ������ �ݾ� �̻󿡼��� ������ ǥ���ϴ� ���� ��Ÿ���� �ʾұ� (����)������, �ᱹ �����Ǿ����ϴ�.
	// 208:����� ������ �����ϰ� �ִ� ����, �������� ������ �ͼ��ϰ� �ֱ� (����)������, ���� �̿��ϰ� �ʹٸ� �� �ʿ� ������ ���� ������ �ȵ˴ϴ�.
	// 224:�����, ����� ���� �ΰ��� ����%0�Ƶ����� ���� �����ϰ� ���� �ʽ��ϴ�.
	// 240:�����, �ᱹ ����� ���� �ΰ��� ����%0�� �������� �ʾұ� ������, ����� ����� ���� ���� �������� ��Ż�մϴ�.
	
	public int getLetterCount(String name, int type) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		int cnt = 0;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT count(*) as cnt FROM letter WHERE receiver=? AND template_id = ? order by date");
			pstm.setString(1, name);
			pstm.setInt(2, type);
			rs = pstm.executeQuery();
			if (rs.next()){
				cnt = rs.getInt(1);
			}
			
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return cnt;
	}
	
	public void writeLetter(int code, String dTime, String sender, String receiver, int templateId, String subject, String content) {	   
		Connection con = null;
		PreparedStatement pstm1 = null;
		ResultSet rs = null;
		PreparedStatement pstm2 = null;
		int itemObjectId = 0;
		
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			
			pstm1 = con.prepareStatement(" SELECT Max(item_object_id)+1 as cnt FROM letter ORDER BY item_object_id ");
			rs = pstm1.executeQuery();
			if (rs.next()) {
				itemObjectId = rs.getInt("cnt");
			}
			
			pstm2 = con.prepareStatement("INSERT INTO letter SET item_object_id=?, code=?, sender=?, receiver=?, date=?, template_id=?, subject=?, content=?, isCheck=? ");
			pstm2.setInt(1, itemObjectId);
			pstm2.setInt(2, code);
			pstm2.setString(3, sender);
			pstm2.setString(4, receiver);
			pstm2.setString(5, dTime);
			pstm2.setInt(6, templateId);
			pstm2.setString(7, subject);
			pstm2.setString(8, content);
			pstm2.setInt(9, 0);
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

	public void writeLetter(int itemObjectId, int code, String sender, String receiver, String date, int templateId, byte[] subject, byte[] content) {
		Connection con = null;
		PreparedStatement pstm1 = null;
		ResultSet rs = null;
		PreparedStatement pstm2 = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm1 = con.prepareStatement("SELECT * FROM letter ORDER BY item_object_id");
			rs = pstm1.executeQuery();
			pstm2 = con.prepareStatement("INSERT INTO letter SET item_object_id=?, code=?, sender=?, receiver=?, date=?, template_id=?, subject=?, content=?");
			pstm2.setInt(1, itemObjectId);
			pstm2.setInt(2, code);
			pstm2.setString(3, sender);
			pstm2.setString(4, receiver);
			pstm2.setString(5, date);
			pstm2.setInt(6, templateId);
			pstm2.setBytes(7, subject);
			pstm2.setBytes(8, content);
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

	public void deleteLetter(int itemObjectId) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM letter WHERE item_object_id=?");
			pstm.setInt(1, itemObjectId);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	
	public void SaveLetter(int id, int letterType) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE letter SET template_id = ? WHERE item_object_id=?");
			pstm.setInt(1, letterType);
			pstm.setInt(2, id);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	
	public void CheckLetter(int id) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE letter SET isCheck = 1 WHERE item_object_id=?");
			pstm.setInt(1, id);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
