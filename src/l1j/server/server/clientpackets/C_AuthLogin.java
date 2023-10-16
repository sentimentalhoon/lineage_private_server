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
package l1j.server.server.clientpackets;

import java.io.IOException;
import java.util.logging.Logger;

import server.Authorization;
import server.LineageClient;

public class C_AuthLogin extends ClientBasePacket {
	private static final String C_AUTH_LOGIN = "[C] C_AuthLogin";
	private static Logger _log = Logger.getLogger(C_AuthLogin.class.getName());

	public C_AuthLogin(byte[] decrypt, LineageClient client) throws IOException {
		super(decrypt);
		String accountName = readS().toLowerCase();
		String password = readS();
		String ip = client.getIp();
		String host = client.getHostname();
		//client.ipcountzero(ip); //## 아이피 카운트 제로선언

		_log.finest("Request AuthLogin from user : " + accountName);
		Authorization.getInstance().auth(client, accountName, password, ip, host);
	}

	@Override
	public String getType() {
		return C_AUTH_LOGIN;
	}
}