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

import java.util.Random;

import l1j.server.server.command.executor.L1UserCalc;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcShopInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_WhoAmount;
import l1j.server.server.serverpackets.S_WhoCharinfo;
import server.LineageClient;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Who extends ClientBasePacket {

	private static final String C_WHO = "[C] C_Who";

	public C_Who(byte[] decrypt, LineageClient client) {
		super(decrypt);
		String s = readS();
		L1PcInstance find = L1World.getInstance().getPlayer(s);
		L1NpcShopInstance find1 = L1World.getInstance().getNpcShop(s); 
		L1PcInstance pc = client.getActiveChar();
		//Random ran = new Random();
		
		if (find != null && find.getAccessLevel() == 0 || find != null && find.getAccessLevel() == 300) {
			S_WhoCharinfo s_whocharinfo = new S_WhoCharinfo(find);
			pc.sendPackets(s_whocharinfo);
		}else if (find1 != null) { 
			   S_WhoCharinfo s_whocharinfo = new S_WhoCharinfo(find1);
			   pc.sendPackets(s_whocharinfo);
		} else {
			//int r = ran.nextInt(2);
			int AddUser = (int)(L1World.getInstance().getAllPlayersToArray().length * 3.5) + 1;
			//int ShopUser = AutoShopManager.getInstance().getShopPlayerCount();
			int CalcUser = L1UserCalc.getClacUser();
			AddUser += CalcUser; 
			String amount = String.valueOf(AddUser);
			S_WhoAmount s_whoamount = new S_WhoAmount(amount);
			pc.sendPackets(s_whoamount);
		}
	}

	@Override
	public String getType() {
		return C_WHO;
	}
}
