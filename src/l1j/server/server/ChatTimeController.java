/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful ,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not , write to the Free Software
 * Foundation , Inc., 59 Temple Place - Suite 330, Boston , MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ChangeName;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.utils.CommonUtil;

public class ChatTimeController implements Runnable {

	private static ChatTimeController _instance;

	public static ChatTimeController getInstance() {
		if (_instance == null) {
			_instance = new ChatTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(360000);//1시간  00
				StartChat();    				  
			}
		} catch (Exception e1) {
		}
	}

	private void StartChat() {
		int nowtime = Integer.valueOf(CommonUtil.dateFormat("HHmm"));
		int chat = 10;		  
		if (nowtime % chat == 0) {
			for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
				for (L1Object obj : L1World.getInstance().getVisibleObjects(pc, 20)){
					if (obj instanceof L1NpcInstance){					  
						L1NpcInstance npc = (L1NpcInstance) obj;			           
						if(npc.getNpcTemplate().get_npcId() == 777097) {//1위동상
							Connection con = null;
							int i = 0;
							try {
								con = L1DatabaseFactory.getInstance().getConnection();
								Statement pstm = con.createStatement();
								ResultSet rs = pstm.executeQuery("SELECT `Exp`,`char_name` FROM `characters` WHERE AccessLevel = 0 ORDER BY `Exp` DESC");
								Statement pstm2 = con.createStatement();
								ResultSet rs2 = pstm2.executeQuery("SELECT `Exp`,`char_name` FROM `characters` WHERE AccessLevel = 0 ORDER BY `Exp` DESC limit 1");
								while (rs2.next()) {
									i++;
									if (npc.getGfxId().getGfxId() == npc.getGfxId().getTempCharGfx()) {
										npc.setName("["+ Config.servername + "] 서버 1위 ["+rs2.getString("char_name")+"]");
										npc.setNameId("["+ Config.servername + "] 서버 1위 ["+rs2.getString("char_name")+"]");
										Broadcaster.broadcastPacket(npc, new S_ChangeName(npc.getId(), "["+ Config.servername + "] 서버 1위 ["+rs2.getString("char_name")+"]"));
									}
									Broadcaster.broadcastPacket(npc, new S_NpcChatPacket(npc, "현재 ["+ Config.servername + "] 서버 전체랭킹 1위는 "+rs2.getString("char_name")+" 님이다!!", 2));
									rs.close(); 
									pstm.close();
									rs2.close(); 
									pstm2.close();
									con.close();
									return;
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}
		}
	}
}