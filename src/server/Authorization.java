package server;

import java.io.IOException;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.Account;
import l1j.server.server.AccountAlreadyLoginException;
import l1j.server.server.GameServerFullException;
import l1j.server.server.LoginAuth;
import l1j.server.server.clientpackets.C_AuthLogin;
import l1j.server.server.clientpackets.C_NoticeClick;
import l1j.server.server.serverpackets.S_LoginResult;
import l1j.server.server.serverpackets.S_Notice;

public class Authorization {
	private static Authorization uniqueInstance = null;
	private static Logger _log = Logger.getLogger(C_AuthLogin.class.getName());

	public static Authorization getInstance() {
		if(uniqueInstance == null) {
			synchronized(Authorization.class) {
				if(uniqueInstance == null)
					uniqueInstance = new Authorization();
			}
		}
		return uniqueInstance;
	}

	private Authorization() {}

	public void auth(LineageClient client, String accountName, String password, String ip, String host) throws IOException {

		/*if(IPchek.getInstance().IPchek(ip) != null || accountName.equals("kurio88") ||  accountName.equals("kurio77")) {
		   IPchek.getInstance().liftIp(ip);
		  }else {
		   disconnect(client);
		   return;
		  }*/
		
		if (isDisitAlaha(accountName) == false) {  // ���̵� ������ �ִٸ� ����
			int lfc = client.getLoginFailedCount();
			client.setLoginFailedCount(lfc + 1);
			if (lfc > 3) disconnect(client);
			else client.sendPacket(new S_LoginResult(S_LoginResult.REASON_ACCESS_FAILED));
			return;
		} 
		if (isDisitAlaha(password) == false) { // ��ȣ�� ������ �ִٸ� ����
			int lfc = client.getLoginFailedCount();
			client.setLoginFailedCount(lfc + 1);
			if (lfc > 3) disconnect(client);
			else client.sendPacket(new S_LoginResult(S_LoginResult.REASON_ACCESS_FAILED));
			return;
		} 
		if (accountName.length() < 4 || accountName.length() > 12) { // ���̵� 4���ں��� �۰ų� 12���ں��� ũ�ٸ� ����
			int lfc = client.getLoginFailedCount();
			client.setLoginFailedCount(lfc + 1);
			if (lfc > 3) disconnect(client);
			else client.sendPacket(new S_LoginResult(S_LoginResult.REASON_ACCESS_FAILED));
			return;
		}
		
		if(checkDuplicatedIPConnection(ip)) {
			_log.info("������ IP�� �ߺ� �α����� �ź��߽��ϴ�. account=" + accountName + " host=" + host);
			client.sendPacket(new S_Notice("������ IP�� �ߺ� �α����� �ź��߽��ϴ�.")); 
			disconnect(client);
			return;
		}

		Account account = Account.load(accountName);
		if (account == null) {			
			if (Config.AUTO_CREATE_ACCOUNTS) {
				if(Account.checkLoginIP(ip)) {
					client.sendPacket(new S_Notice("���� IP�� ������ ������ 4�� �ֽ��ϴ�"));
					try {
						//Thread.sleep(1500);
						disconnect(client);
					} catch (Exception e1) {}
				}else{
					account = Account.create(accountName, password, ip, host);
					account = Account.load(accountName);
				}
			} else {
				_log.warning("account missing for user " + accountName);
			}
		}

		if (account == null || !account.validatePassword(accountName, password)) {
			int lfc = client.getLoginFailedCount();
			client.setLoginFailedCount(lfc + 1);
			if (lfc > 3) disconnect(client);
			else client.sendPacket(new S_LoginResult(S_LoginResult.REASON_USER_OR_PASS_WRONG));
			return;
		}

		if(Config.AUTH_CONNECT) {
			LoginAuth authIP = new LoginAuth();
			if (authIP.ConnectCheck(ip)) {
				client.sendPacket(new S_LoginResult(0x01)); // 0x01    
				return;
			}
		}

		if (account.isBanned()) {
			_log.info("BAN IP�� �α����� �ź��߽��ϴ�. account=" + accountName + " host=" + host);
			client.sendPacket(new S_LoginResult(S_LoginResult.REASON_BUG_WRONG));
			disconnect(client);
			return;
		}

		try {
			LoginController.getInstance().login(client, account);
			Account.updateLastActive(account); // ���� �α������� �����Ѵ�
			client.setAccount(account);
			sendNotice(client);
		} catch (GameServerFullException e) {
			_log.info("���� �ο����� �ʰ��Ͽ����ϴ�. (" + client.getHostname() + ")�� ���� �õ��� ���� �����߽��ϴ�.");
			client.sendPacket(new S_LoginResult(S_LoginResult.REASON_USER_OR_PASS_WRONG));
			disconnect(client);
			return;
		} catch (AccountAlreadyLoginException e) {
			_log.info("������ ������ �ߺ� �α����� �ź��߽��ϴ�. account=" + accountName + " host=" + host);
			client.sendPacket(new S_LoginResult(S_LoginResult.REASON_ACCOUNT_ALREADY_EXISTS));
			disconnect(client);
			return;
		}
	}

	// ########## A105 ������ ���� ���� �� �������� ���� ���ϰ�  
	private static boolean isDisitAlaha(String str) {
		boolean check = true;
		for (int i = 0; i < str.length(); i++) {
			if (Character.isWhitespace(str.charAt(i))){ // �����̶��
				check = false;
				break;
			}
		}
		return check;
	}
	// ########## A105 ������ ���� ���� �� �������� ���� ���ϰ� 
	private void sendNotice(LineageClient client) {
		String accountName = client.getAccountName();

		// �о���� ������ �ִ��� üũ
		if(S_Notice.NoticeCount(accountName) > 0){
			client.sendPacket(new S_Notice(accountName, client));
		} else {
			client.setloginStatus(1);
			new C_NoticeClick(client);
		}
	}

	private void disconnect(LineageClient client) throws IOException {
		client.kick();
		client.close();
	}

	@SuppressWarnings("unused")
	private Account loadAccountInfoFromDB(String accountName) {
		return Account.load(accountName);
	}

	private boolean checkDuplicatedIPConnection(String ip) {
		if (!Config.ALLOW_2PC) {
			return LoginController.getInstance().checkDuplicatedIP(ip);
		}
		return false;
	}

}
