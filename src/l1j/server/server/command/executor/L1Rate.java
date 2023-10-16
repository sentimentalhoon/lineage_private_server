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
package l1j.server.server.command.executor;

import java.util.StringTokenizer;
//import java.util.logging.Logger;
import l1j.server.Config;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1Rate implements L1CommandExecutor {
	//private static Logger _log = Logger.getLogger(L1Describe.class.getName());

	private L1Rate() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Rate();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			@SuppressWarnings("unused")
			StringTokenizer st = new StringTokenizer(arg);
			
			int exp = (int) Config.RATE_XP;
			int lawful = (int) Config.RATE_LAWFUL;
			int adena = (int) Config.RATE_DROP_ADENA;
			int items = (int) Config.RATE_DROP_ITEMS;
			int enchantWeapon = Config.ENCHANT_CHANCE_WEAPON;
			int enchantArmor = Config.ENCHANT_CHANCE_ARMOR;
			int enchantAccessory = Config.ENCHANT_CHANCE_ACCESSORY;
			int maxWeapon = Config.MAX_WEAPON;
			int maxArmor = Config.MAX_ARMOR;
			int maxAccessory = Config.MAX_ACCESSORY;
			int featherTime = Config.FEATHER_TIME;
			int featherNo = Config.FEATHER_NUMBER;
			int clanNo = Config.CLAN_NUMBER;
			int castleNo = Config.CASTLE_NUMBER;

			pc.sendPackets(new S_SystemMessage("¹èÀ²:"+exp+"/·Î¿ìÇ®:"+lawful+"/¾Æµ¥³ª:"+adena+"/¾ÆÀÌÅÛ:"+items));
			pc.sendPackets(new S_SystemMessage("¹«±âÈ®·ü:"+enchantWeapon+"/¹æ¾îÈ®·ü:"+enchantArmor+"/¾Ç¼¼È®·ü:"+enchantAccessory));
			pc.sendPackets(new S_SystemMessage("MaxWeapon:"+maxWeapon+"/MaxArmor:"+maxArmor+"/MaxAccessory:"+maxAccessory));
			pc.sendPackets(new S_SystemMessage("±êÅÐ½Ã°£:"+featherTime+"/±êÅÐ°¹¼ö:"+featherNo+"/Ç÷¸Í:"+clanNo+"/¼ºÇ÷:"+castleNo));

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("[.¹èÀ²] À¸·Î ÀÔ·ÂÇÏ¼¼¿ä."));
		}
	}
}
