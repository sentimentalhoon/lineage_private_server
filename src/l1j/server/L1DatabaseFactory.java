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
package l1j.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.utils.LeakCheckedConnection;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * DB���� �׼����ϱ� ���� ���� �������̽��� ����
 */
public class L1DatabaseFactory {
	private static L1DatabaseFactory _instance = null;

	/** DB���� ������ ������ ��?  */
	private ComboPooledDataSource _source;

	/** �޼��� �α׿�.  */
	private static Logger _log = Logger.getLogger(L1DatabaseFactory.class.getName());

	/* DB �׼����� �ʿ��� ������ */
	/** DB���� ����̹�.  */
	private static String _driver;
	/** DB������ URL.  */
	private static String _url;     
	/** DB������ �����ϴ� ������.  */
	private static String _user;    
	/** DB������ �����ϴ� �н�����.  */
	private static String _password;

	/**
	 * @return L1DatabaseFactory
	 * @throws SQLException
	 */
	public static L1DatabaseFactory getInstance() throws SQLException {
		if(_instance == null) {
			synchronized (L1DatabaseFactory.class) {
				if(_instance  == null) {
					_instance = new L1DatabaseFactory();
				}
			}
		}
		
		return _instance ;
	}
	
	/**
	 * DB���� �׼����� �ʿ��� ���� ����
	 * 
	 * @param driver
	 *            DB���� ����̹�
	 * @param url
	 *            DB���� URL
	 * @param user
	 *            DB������ �����ϴ� ������
	 * @param password
	 *            DB������ �����ϴ� �н�����
	 */
	public static void setDatabaseSettings(final String driver,
			final String url, final String user, final String password) {
		_driver = driver;
		_url = url;
		_user = user;
		_password = password;
	}

	private L1DatabaseFactory() throws SQLException {
		try {
			_source = new ComboPooledDataSource();
			_source.setDriverClass(_driver);
			_source.setJdbcUrl(_url);
			_source.setUser(_user);
			_source.setPassword(_password);

			/* Test the connection */
			_source.getConnection(). close();
		} catch (SQLException x) {
			_log.fine("Database Connection FAILED");
			// rethrow the exception
			throw x;
		} catch (Exception e) {
			_log.fine("Database Connection FAILED");
			throw new SQLException("could not init DB connection:" + e);
		}
	}

	public void shutdown() {
		try {
			_source.close();
		} catch (Exception e) {
			_log.log(Level.INFO, "", e);
		}
		try {
			_source = null;
		} catch (Exception e) {
			_log.log(Level.INFO, "", e);
		}
	}

	/**
	 * DB������ ��, connection ������Ʈ�� �����ش�.
	 * 
	 * @return Connection connection ������Ʈ
	 * @throws SQLException
	 */
	public Connection getConnection() {
		Connection con = null;

		while (con == null) {
			try {
				con = _source.getConnection();
			} catch (SQLException e) {
				_log.warning("L1DatabaseFactory: getConnection() failed, trying again " + e);
			}
		}
		return Config.DETECT_DB_RESOURCE_LEAKS ?  LeakCheckedConnection
				. create(con) : con;
	}
}
