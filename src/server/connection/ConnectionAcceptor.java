package server.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import server.LineageClient;
import server.LoginController;
//import server.monitor.Monitor;
//import server.monitor.Monitorable;

import l1j.server.Config;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.IpTable;

public class ConnectionAcceptor extends Thread /*implements Monitorable*/ {
//	private enum State {STARTUP, WAITING, CONNECTION, BAN};
	private ServerSocket serverSocket;
	private ConnectionCounter connectionCounter;
	//private Monitor monitor;
//	private State state;
	private String clientIp;
	
	public ConnectionAcceptor() {
		connectionCounter = new ConnectionCounter();
//		setState(State.STARTUP);
	}
	
	public void initialize() {
		LoginController.getInstance().setMaxAllowedOnlinePlayers(Config.MAX_ONLINE_USERS);
		
		String serverHostName 	= Config.GAME_SERVER_HOST_NAME;
		int gameServerPort 		= Config.GAME_SERVER_PORT;
		
		if (!"*".equals(serverHostName)) {
			InetAddress inetaddress = null;
			try {
				inetaddress = InetAddress.getByName(serverHostName);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			try {
				serverSocket = new ServerSocket(gameServerPort, 50, inetaddress);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				serverSocket = new ServerSocket(gameServerPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.start();
	}

	@Override
	public void run() {
		Socket connection = null;
		
//		setState(State.WAITING);
//		notifyMonitors();
		while (true) {
			try {
				connection = serverSocket.accept();
				clientIp = connection.getInetAddress().getHostAddress();
				
				if(IpTable.getInstance().isBannedIp(clientIp)) continue;
				if (connectionCounter.addIp(clientIp)){
//					setState(State.CONNECTION);
//					notifyMonitors();
					createClientThread(connection);
					connectionCounter.removeIp(clientIp);
				} else {
//					setState(State.BAN);
					IpTable.getInstance().banIp(clientIp);
//					notifyMonitors();
				}
			} catch (IOException ioexception) {}
		}
	}

	private void createClientThread(Socket connection) {
		/*LineageClient client = null;
		try {
			client = new LineageClient(connection);
			GeneralThreadPool.getInstance().execute(client);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public void shutdown() {
		Thread.interrupted();
	}
	
//	public void setState(State s) {
//		state = s;
//	}
/*
	@Override
	public void notifyMonitors() {
		String s = null;
		
		switch(state) {
		case STARTUP: 
			s = "[LoginServer] Started..";
			break;
		case WAITING: 
			//s = "[LoginServer : 메모리] " + SystemUtil.getUsedMemoryMB() + "MB 사용";
			s = "[LoginServer : 접속] 대기 中...";
			break;
		case CONNECTION: 
			s = "[LoginServer] Connected Client IP: " + clientIp;
			break;
		case BAN: 
			s =	"[LoginServer] Baned Client IP: " + clientIp + " (count : 7)";
			break;
		default:
			if(s == null)
				return;
		}
		
		monitor.update(s);
	}

	@Override
	public void registerMonitor(Monitor m) {
		monitor = m;
	}

	@Override
	public void removeMonitor(Monitor m) {
		if(monitor.equals(m))
			monitor = null;
	}
	*/
}
