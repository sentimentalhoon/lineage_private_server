/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package l1j.server.server;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;
/**
 * TODO ������� ����� PC�� 1�븸 ���� ���� ������ �Ѵ�δ� ���� ������ �����ϴ�
 * XXX ���� ������ Ŭ���̾�Ʈ���� ���� ����Ÿ�δ� ������ ��� ���� �Ұ���.
 * @author 
 *
 */
public class LoginAuth {
	private static Logger _log = Logger.getLogger(LoginAuth.class.getName());

	/**
	 * ���ӱ� ��� ����üũ
	 * @param Ip
	 *    ������̶�� true
	 */
	public synchronized boolean ConnectCheck(String Ip) {
		//if(Ip.startsWith("192.168.0.")) return false;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "SELECT count(ip) FROM connected WHERE ip=?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setString(1, Ip);
			rs = pstm.executeQuery();
			if(rs.next()){
				if(rs.getInt(1) >= 1) return false;
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally{
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return true;
	}

	/**
	 * ���ӱ� ��� ��������
	 * @param IP
	 */
	public synchronized void ConnectDelete(String Ip) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM connected WHERE ip=?");
			pstm.setString(1, Ip);
			pstm.execute();
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	/**
	 * Connected DB �ʱ�ȭ
	 */
	public static void InitialAuthStatus() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("TRUNCATE connected");
			pstm.execute();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
