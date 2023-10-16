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

import java.util.logging.Logger;

import server.LineageClient;
import server.system.autoshop.AutoShop;
import server.system.autoshop.AutoShopManager;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_PacketBox;

public class C_Restart extends ClientBasePacket {
	private static final String C_OPCODE_RESTART = "[C] C_Restart";
	private static Logger _log = Logger.getLogger(C_Restart.class.getName());	

	public C_Restart(byte[] decrypt, LineageClient client) throws Exception {
		super(decrypt);
		client.CharReStart(true);
		client.sendPacket(new S_PacketBox(S_PacketBox.LOGOUT));

		if (client.getActiveChar() != null) {
			L1PcInstance pc = client.getActiveChar();
			pc.setadFeature(1);
			pc.save();
			pc.saveInventory();


			//eva.LogServerAppend("종료", pc, client.getIp(), -1);
			if(pc.isPrivateShop() && AutoShopManager.getInstance().isAutoShop()){				
				synchronized (pc) {					
					AutoShopManager shopManager = AutoShopManager.getInstance(); 
					AutoShop autoshop = shopManager.makeAutoShop(pc);
					shopManager.register(autoshop);
					client.setActiveChar(null);
				}
				if(pc.getMapId() == 208){
					if(pc.getBattleOk()){
						return;
					}
				}
			}else{
				if (!(pc.getInventory().checkItem(400001, 1) || pc.getInventory().checkItem(400000, 1))) { // 무인PC 만들기(쿠우)
					_log.fine("Disconnect from: " + pc.getName());
					synchronized (pc) {
						client.quitGame(pc);
						pc.logout();
						client.setActiveChar(null);
						//client.close();
					}
				}else { // 무인PC 만들기(쿠우)
					synchronized (pc) {
						pc.noPlayerCK = true;	// 무인 제외 추가
						pc.setNetConnection(null);
						client.setActiveChar(null);
					}
				}
			}
		} else {
			_log.fine("Disconnect Request from Account : " + client.getAccountName());
		}
	}

	@Override
	public String getType() {
		return C_OPCODE_RESTART;
	}
}
