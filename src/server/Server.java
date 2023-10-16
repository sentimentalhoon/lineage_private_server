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
/*
 * $Header: /cvsroot/l2j/L2_Gameserver/java/net/sf/l2j/Server.java,v 1.5 2004/11/19 08:54:43 l2chef Exp $
 *
 * $Author: l2chef $
 * $Date: 2004/11/19 08:54:43 $
 * $Revision: 1.5 $
 * $Log: Server.java,v $
 * Revision 1.5  2004/11/19 08:54:43  l2chef
 * database is now used
 *
 * Revision 1.4  2004/07/08 22:42:28  l2chef
 * logfolder is created automatically
 *
 * Revision 1.3  2004/06/30 21:51:33  l2chef
 * using jdk logger instead of println
 *
 * Revision 1.2  2004/06/27 08:12:59  jeichhorn
 * Added copyright notice
 */
package server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.LogManager;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.PerformanceTimer;
import l1j.server.server.utils.SQLUtil;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import server.mina.LineageCodecFactory;

public class Server {
	private volatile static Server uniqueInstance;
	//private static Logger _log = Logger.getLogger(Server.class.getName());
	private static final String LOG_PROP = "./config/log.properties";//로그 설정 파일

	//private MonitorManager monitorServer;

	private Server() {
		//	monitorServer = new MonitorManager();
		//loginServer = new ConnectionAcceptor(); 
	}

	public static Server createServer() {
		if(uniqueInstance == null) {
			synchronized (Server.class) {
				if(uniqueInstance == null) {
					uniqueInstance = new Server();
				}
			}
		}
		return uniqueInstance;
	}

	public void start() {
		initLogManager();
		initDBFactory();
		EvaSystemTime(); // 시간의 균열 추가
		//RobotCharacterItemsDelete(); // 로봇 캐릭터 아이템 삭제
		try {
			PerformanceTimer timer = new PerformanceTimer();
			System.out.println("=================================================");
			System.out.print("[Delete DB] Delete DB Data - 1...");
			clearDB();
			System.out.println("OK! " + timer.get() + " ms");
			timer.reset();
			System.out.print("[Delete DB] Delete DB Data - 2...");
			clearDB();			
			System.out.println("OK! " + timer.get() + " ms");
			System.out.println("=================================================");
			timer = null;
		} catch (SQLException e) {}
		startGameServer();
		//	startTelnetServer();
		startLoginServer();
		//	monitorServer.register(loginServer);
	}
	private void addLogger(DefaultIoFilterChainBuilder chain) throws Exception {
		chain.addLast("logger", new LoggingFilter());
		System.out.println("Logging ON");
	}

	private void startLoginServer() {
		try {
			//loginServer.initialize();
			// TCP/IP 서버 클레스 활성화
			LoginController.getInstance().setMaxAllowedOnlinePlayers(Config.MAX_ONLINE_USERS);
			NioSocketAcceptor acceptor = new NioSocketAcceptor();
			DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

			// 암호화 클레스 등록
			chain.addLast("codec", new ProtocolCodecFilter( new LineageCodecFactory()));

			//로거로 기본 세팅
			if(Config.LOGGER) {
				addLogger(chain);
			}

			// Bind
			acceptor.setHandler(new LineageProtocolHandler());
			acceptor.bind(new InetSocketAddress(Config.GAME_SERVER_PORT));

			System.out.println("서버가 "+ Config.GAME_SERVER_PORT +"번 포트를 이용해서 가동 되었습니다.");
		} catch(Exception e) { /*e.printStackTrace();*/ };
		// FIXME StrackTrace하면 error  
	}

	public void shutdown() {
		//loginServer.shutdown();
		GameServer.getInstance().shutdown();		
		//System.exit(0);
	}

	private void initLogManager() {
		File logFolder = new File("log");
		logFolder.mkdir();

		try {
			InputStream is = new BufferedInputStream(new FileInputStream(LOG_PROP));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
		} catch (IOException e) {
			//_log.log(Level.SEVERE, "Failed to Load " + LOG_PROP + " File.", e);
			System.exit(0);
		}
		try {
			Config.load();
		} catch (Exception e) {
			//_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			System.exit(0);
		}
	}

	private void initDBFactory() {// L1DatabaseFactory 초기설정
		L1DatabaseFactory.setDatabaseSettings(Config.DB_DRIVER, Config.DB_URL,
				Config.DB_LOGIN, Config.DB_PASSWORD);
		try {
			L1DatabaseFactory.getInstance();
		} catch(Exception e) { /*e.printStackTrace();*/ };
		// FIXME StrackTrace하면 error 
	}

	private void startGameServer() {
		try {
			GameServer.getInstance().initialize();
		} catch(Exception e) { /*e.printStackTrace();*/ };
		// FIXME StrackTrace하면 error  
	}
	/*
	private void startTelnetServer() {
		if (Config.TELNET_SERVER) {
			TelnetServer.getInstance().start();
		}
	}
	 */
	private void clearDB() throws SQLException{
		Connection c = null;
		PreparedStatement p = null;
		try {
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("call deleteData(?)");
			p.setInt(1, Config.DELETE_DB_DAYS);
			p.execute();
		} catch (Exception e) {
		} finally {
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/*** 시간의 균열 시간 자동 업데이트 ***/
	public void EvaSystemTime() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE evasystem SET time=?, openLoc=1, moveLoc=1, extend=0 WHERE id=1");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, 1);
			Date date = cal.getTime();
			String sdf = (new SimpleDateFormat("yyyy/MM/dd HH").format(date));
			pstm.setString(1, sdf);
			pstm.execute();
			System.out.println("시간의 균열 시간이 [" + sdf + "]로 업데이트 되었습니다.");
		} catch (SQLException e) {
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	public void RobotCharacterItemsDelete() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM character_items WHERE char_id >= 268838769 and char_id <= 269027262");
			pstm.execute();
			System.out.println("로봇 캐릭터의 아이템이 삭제 완료 되었습니다.");
		} catch (SQLException e) {
			//_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);		
			SQLUtil.close(con);
		}
	}
}
