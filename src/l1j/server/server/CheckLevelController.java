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


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import l1j.server.Config;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.SystemUtil;
import server.manager.eva;


public class CheckLevelController implements Runnable {

	private static CheckLevelController _instance;

	public static CheckLevelController getInstance() {
		if (_instance == null) {
			_instance = new CheckLevelController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(60000);
				Hp();
			}
		} catch (Exception e1) {
		}
	}
	private Calendar getRealTime() {
		TimeZone _tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(_tz);
		return cal;
	}

	private void Hp() {   //madeby-sunny
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		int nowtime = Integer.valueOf(sdf.format(getRealTime().getTime()));
		int time3 = 12;
		if (nowtime % time3 == 0) {
			try {
				for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) { 
					if(pc.isPrivateShop() 
							|| pc.noPlayerCK
							|| pc.isGm() 
							|| pc.isDead()){
						continue;
					} else {
						if (pc.getLevel() > pc.getHighLevel()){
							eva.writeMessage(-4, pc.getName() +"(" + pc.getLevel() + ")" + " / " + pc.getHighLevel());
							pc.sendPackets(new S_Disconnect());
							continue;
						} else if (pc.getLevel() > Config.LIMITLEVEL){
							eva.writeMessage(-4, pc.getName() +"(" + pc.getLevel() + ")" + " / " + pc.getHighLevel());
							pc.sendPackets(new S_Disconnect());
							continue;
						}
					}
				}	
				System.out.println(nowtime +" CheckLevelController 작업 완료");
				System.out.println("CPU 사용률 : " + SystemUtil.getCpuUsagePer() + " / 메모리 사용률 : " + SystemUtil.getUsedMemoryMB());
			} catch (Exception e){
			}
		}
	}
}//madeby-sunny
