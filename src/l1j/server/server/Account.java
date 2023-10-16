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
package l1j.server.server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.LineageClient;
import server.manager.eva;
import l1j.server.Base64;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;

public class Account {
	/** ������ */
	private String _name;
	/** ������ IP�ּ� */
	private String _ip;
	/** �н�����(��ȣȭ ��) */
	private String _password;
	/** �ֱ� ������ */
	private Timestamp _lastActive;
	/** ������ ���(GM�ΰ�?) */
	private int _accessLevel;
	/** ������ ȣ��Ʈ�� */
	private String _host;
	/** �� ����(True == ����) */
	private boolean _banned;
	/** ���� ��ȿ ����(True == ��ȿ) */
	private boolean _isValid = false;
	/** ĳ���� ����(�°��ǿ���) */
	private int _charslot;
	/** â�� ��й�ȣ */
	private int _GamePassword;
	/** ���� �ð� */
	private int _AccountTime;
	/** ���� �ð� ���� �� */
	private int _AccountTimeRead;
	/** �޼��� �α׿� */
	private static Logger _log = Logger.getLogger(Account.class.getName());

	public Account() {}

	/**
	 * �н����带 ��ȣȭ�Ѵ�.
	 *
	 * @param rawPassword �н�����
	 * @return String
	 * @throws NoSuchAlgorithmException
	 *             ��ȣȭ �˰����� ����� �� ���� ��
	 * @throws UnsupportedEncodingException
	 *             ���ڵ��� �������� ���� ��
	 */
	private static String encodePassword(final String rawPassword)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] buf = rawPassword.getBytes("UTF-8");
		buf = MessageDigest.getInstance("SHA").digest(buf);
		return Base64.encodeBytes(buf);
	}

	/**
	 * �ű� ���� ����
	 *
	 * @param name ������
	 * @param rawPassword �н�����
	 * @param ip ������ IP�ּ�
	 * @param host ������ ȣ��Ʈ��
	 * @return Account
	 */
	public static Account create(final String name, final String rawPassword,
			final String ip, final String host) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			Account account = new Account();
			account._name = name;
			account._password = rawPassword;			
			account._ip = ip;
			account._host = host;
			account._banned = false;
			account._lastActive = new Timestamp(System.currentTimeMillis());
			account._quize = "1234";

			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "INSERT INTO accounts SET login=?,password=password(?),lastactive=?,access_level=?,ip=?,host=?,banned=?,charslot=?,quize=?,gamepassword=?,point_time=?,Point_time_ready=?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setString(1, account._name);
			pstm.setString(2, account._password);
			pstm.setTimestamp(3, account._lastActive);
			pstm.setInt(4, 0);
			pstm.setString(5, account._ip);
			pstm.setString(6, account._host);
			pstm.setInt(7, account._banned ? 1 : 0);
			pstm.setInt(8, 2); // ĳ�� ����
			pstm.setString(9, account._quize);
			pstm.setInt(10, 0);
			pstm.setInt(11, 0);
			pstm.setInt(12, 0);
			pstm.execute();
			_log.info("created new account for " + name);
			eva.writeMessage(-3,"["+name+"]");
			return account;
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return null;
	}

	/**
	 * DB���� ���� ���� �ҷ����� 
	 *
	 * @param name ������
	 * @return Account
	 */

	public static Account load(final String name) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		Account account = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "SELECT * FROM accounts WHERE login=? LIMIT 1";
			pstm = con.prepareStatement(sqlstr);
			pstm.setString(1, name);
			rs = pstm.executeQuery();
			if (!rs.next()) {
				return null;
			}
			account = new Account();
			account._name = rs.getString("login");
			account._password = rs.getString("password");
			account._lastActive = rs.getTimestamp("lastactive");
			account._accessLevel = rs.getInt("access_level");
			account._ip = rs.getString("ip");
			account._host = rs.getString("host");
			account._banned = rs.getInt("banned") == 0 ? false : true;
			account._charslot = rs.getInt("charslot");
			account._quize = rs.getString("quize");
			account._GamePassword = (rs.getInt("gamepassword"));
			int pt = rs.getInt("point_time");
			int ptr = rs.getInt("point_time_ready");
			if(pt <= 0 && ptr > 0) {
				account._AccountTime = ptr;
				account._AccountTimeRead = 0;
				updatePointAccountReady(name, ptr, 0);
			} else {
				account._AccountTime = pt;
				account._AccountTimeRead = ptr;
			}

			_log.fine("account exists");

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

		return account;
	}

	/**
	 * DB�� �ֱ� ������ ������Ʈ
	 *
	 * @param account ������
	 */
	public static void updateLastActive(final Account account) {
		Connection con = null;
		PreparedStatement pstm = null;
		Timestamp ts = new Timestamp(System.currentTimeMillis());

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET lastactive=? WHERE login = ?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setTimestamp(1, ts);
			pstm.setString(2, account.getName());
			pstm.execute();
			account._lastActive = ts;
			_log.fine("update lastactive for " + account.getName());
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	/**
	 * ���н����������; 
	 *
	 * @param account ������
	 */
	public static void updateWebPwd(String AccountName, String pwd) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET password=password(?) WHERE login = ?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setString(1, pwd);
			pstm.setString(2, AccountName);
			pstm.execute();
			_log.fine("update lastactive for " + AccountName);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	/**
	 * �ش� ������ ĳ���ͼ��� ��
	 *
	 * @return result ĳ���ͼ�
	 */
	public int countCharacters() {
		int result = 0;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "SELECT count(*) as cnt FROM characters WHERE account_name=?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setString(1, _name);
			rs = pstm.executeQuery();
			if (rs.next()) {
				result = rs.getInt("cnt");
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return result;
	}

	public static void ban(final String account) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET banned=1 WHERE login=?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setString(1, account);
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public static void updateQuize(final Account account) {
		Connection con = null;
		PreparedStatement pstm = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET quize=? WHERE login=?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setString(1, account.getquize());
			pstm.setString(2, account.getName());
			pstm.execute();
			account._quize = account.getquize();
			_log.fine("update quize for " + account.getName());
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	/**
	 * �Էµ� ��й�ȣ�� DB�� ����� �н����带 ��
	 *
	 * @param rawPassword �н�����
	 * @return boolean
	 */
	public boolean validatePassword(String accountName, final String rawPassword) {
		// ���� ���� �Ŀ� ���� �����Ǿ��� ���� ���н�Ų��.
		//		if (_isValid) {
		//		return false;
		//		}
		try {
			_isValid = (_password.equals(encodePassword(rawPassword)) || checkPassword(accountName, _password, rawPassword));
			if (_isValid) {
				_password = null; // ������ �������� ���, �н����带 �ı��Ѵ�.
			}
			return _isValid;
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return false;
	}

	/**
	 * ��ȿ�� �����ΰ� 
	 *
	 * @return boolean
	 */
	public boolean isValid() {
		return _isValid;
	}

	/**
	 * GM �����ΰ�
	 *
	 * @return boolean
	 */
	public boolean isGameMaster() {
		return 0 < _accessLevel;
	}

	public String getName() {
		return _name;
	}

	public String getIp() {
		return _ip;
	}

	public Timestamp getLastActive() {
		return _lastActive;
	}

	public int getAccessLevel() {
		return _accessLevel;
	}

	public String getHost() {
		return _host;
	}


	public boolean isBanned() {
		return _banned;
	}
	/**
	 * ��� ����Ѵ�.
	 *
	 * @return String
	 */
	private String _quize; 

	public String getquize() {
		return _quize;
	}

	public void setquize(String s) {
		_quize = s;
	}

	public int getCharSlot() {
		return _charslot;
	}	

	/**
	 * �ɸ��� ���Լ� ���� 
	 *
	 * @return boolean
	 */
	public void setCharSlot(LineageClient client, int i) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET charslot=? WHERE login = ?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setInt(1, i);
			pstm.setString(2, client.getAccount().getName());
			pstm.execute();
			client.getAccount()._charslot = i;
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public static boolean checkLoginIP(String ip) {  
		int num = 0;  
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT count(ip) as cnt FROM accounts WHERE ip=? ");

			pstm.setString(1, ip);
			rs = pstm.executeQuery();

			if (rs.next()) num = rs.getInt("cnt");  

			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);

			// ���� IP�� ������ ������ 2�� �̸��� ���
			if (num < 4) return false;
			else 		 return true;
		}catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}return false;
	}

	//�� ������ ���� �޼ҵ� �߰� - By Sini
	public static boolean checkPassword(String accountName, String _pwd ,String rawPassword) { 
		String _inputPwd = null;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT password(?) as pwd ");

			pstm.setString(1, rawPassword);
			rs = pstm.executeQuery();
			if (rs.next()){
				_inputPwd = rs.getString("pwd");  
			}
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
			if ( _pwd.equals(_inputPwd)) { // �����ϴٸ�
				return true;
			}else
				return false;
		}catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}return false;
	}

	/**
	 * â�� ���
	 *
	 * @return boolean
	 */
	public static void setGamePassword(LineageClient client, int pass) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET gamepassword=? WHERE login =?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setInt(1, pass);
			pstm.setString(2, client.getAccount().getName());
			pstm.execute();
			client.getAccount()._GamePassword = pass;
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public int getGamePassword() {
		return _GamePassword;
	}

	/**
	 * �α׾ƿ��� ���� ����Ʈ ������ Ÿ���� �����Ų��; 
	 *
	 * @param account ������
	 */
	public static void updatePointAccount(String AccountName, long time) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET point_time=? WHERE login = ?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setLong(1, time);
			pstm.setString(2, AccountName);
			pstm.execute();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public static void updatePointAccountReady(String AccountName, int time, int ready) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			String sqlstr = "UPDATE accounts SET point_time=?, point_time_ready=? WHERE login = ?";
			pstm = con.prepareStatement(sqlstr);
			pstm.setInt(1, time);
			pstm.setInt(2, ready);
			pstm.setString(3, AccountName);
			pstm.execute();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public int getAccountTime() { return _AccountTime; }

	public int getAccountTimeReady() { return _AccountTimeRead; }
}
