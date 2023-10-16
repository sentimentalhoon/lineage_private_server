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

import java.util.logging.Logger;

import l1j.server.server.datatables.CastleTable;
import l1j.server.server.datatables.DropItemTable;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.MapFixKeyTable;
import l1j.server.server.datatables.MapsTable;
import l1j.server.server.datatables.MobSkillTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.ResolventTable;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.datatables.WeaponSkillTable;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1TreasureBox;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1Reload implements L1CommandExecutor {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1Reload.class.getName());

	private L1Reload() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Reload();
	}

	@Override
	public void execute(L1PcInstance gm, String cmdName, String arg) {		
		if (arg.equalsIgnoreCase("드랍")) {
			DropTable.reload();
			gm.sendPackets(new S_SystemMessage("DropTable Update Complete..."));
		} else if (arg.equalsIgnoreCase("드랍아이템")) {
			DropItemTable.reload();
			gm.sendPackets(new S_SystemMessage("DropItemTable Update Complete..."));		
		} else if (arg.equalsIgnoreCase("변신")) {
			PolyTable.reload();
			gm.sendPackets(new S_SystemMessage("PolyTable Update Complete..."));
		} else if (arg.equalsIgnoreCase("용해제")) {
			ResolventTable.reload();
			gm.sendPackets(new S_SystemMessage("ResolventTable Update Complete..."));
		} else if (arg.equalsIgnoreCase("트레져박스")) {
			L1TreasureBox.load();
			gm.sendPackets(new S_SystemMessage("TreasureBox Reload Complete..."));		
		} else if (arg.equalsIgnoreCase("스킬")) {
			SkillsTable.reload();
			gm.sendPackets(new S_SystemMessage("Skills Reload Complete..."));
		} else if (arg.equalsIgnoreCase("아이피")){
			IpTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("공성")){
			CastleTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("상점")){
			ShopTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("몹스킬")){
			MobSkillTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("맵키")){
			MapFixKeyTable.reload();	
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));	
		} else if (arg.equalsIgnoreCase("맵아이디")){
			MapsTable.reload();	
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));	
		} else if (arg.equalsIgnoreCase("아이템")){
			ItemTable.reload();
			gm.sendPackets(new S_SystemMessage("ItemTable Reload Complete..."));
		} else if (arg.equalsIgnoreCase("웨폰스킬")){
			WeaponSkillTable.reload();
			gm.sendPackets(new S_SystemMessage("WeaponSkillTable Reload Complete..."));
		}else { 
			gm.sendPackets(new S_SystemMessage(cmdName + " : [드랍, 드랍아이템, 변신, 용해제]"));
			gm.sendPackets(new S_SystemMessage(cmdName + " : [스킬, 몹스킬, 맵키, 아이템, 아이피, 상점]"));
			gm.sendPackets(new S_SystemMessage(cmdName + " : [맵아이디, 트레져박스, 공성, 균열,웨폰스킬]"));
		}		
	}
}
