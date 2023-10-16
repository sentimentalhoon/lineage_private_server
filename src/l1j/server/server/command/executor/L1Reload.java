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
		if (arg.equalsIgnoreCase("���")) {
			DropTable.reload();
			gm.sendPackets(new S_SystemMessage("DropTable Update Complete..."));
		} else if (arg.equalsIgnoreCase("���������")) {
			DropItemTable.reload();
			gm.sendPackets(new S_SystemMessage("DropItemTable Update Complete..."));		
		} else if (arg.equalsIgnoreCase("����")) {
			PolyTable.reload();
			gm.sendPackets(new S_SystemMessage("PolyTable Update Complete..."));
		} else if (arg.equalsIgnoreCase("������")) {
			ResolventTable.reload();
			gm.sendPackets(new S_SystemMessage("ResolventTable Update Complete..."));
		} else if (arg.equalsIgnoreCase("Ʈ�����ڽ�")) {
			L1TreasureBox.load();
			gm.sendPackets(new S_SystemMessage("TreasureBox Reload Complete..."));		
		} else if (arg.equalsIgnoreCase("��ų")) {
			SkillsTable.reload();
			gm.sendPackets(new S_SystemMessage("Skills Reload Complete..."));
		} else if (arg.equalsIgnoreCase("������")){
			IpTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("����")){
			CastleTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("����")){
			ShopTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("����ų")){
			MobSkillTable.reload();
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));
		} else if (arg.equalsIgnoreCase("��Ű")){
			MapFixKeyTable.reload();	
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));	
		} else if (arg.equalsIgnoreCase("�ʾ��̵�")){
			MapsTable.reload();	
			gm.sendPackets(new S_SystemMessage("Reload Complete..."));	
		} else if (arg.equalsIgnoreCase("������")){
			ItemTable.reload();
			gm.sendPackets(new S_SystemMessage("ItemTable Reload Complete..."));
		} else if (arg.equalsIgnoreCase("������ų")){
			WeaponSkillTable.reload();
			gm.sendPackets(new S_SystemMessage("WeaponSkillTable Reload Complete..."));
		}else { 
			gm.sendPackets(new S_SystemMessage(cmdName + " : [���, ���������, ����, ������]"));
			gm.sendPackets(new S_SystemMessage(cmdName + " : [��ų, ����ų, ��Ű, ������, ������, ����]"));
			gm.sendPackets(new S_SystemMessage(cmdName + " : [�ʾ��̵�, Ʈ�����ڽ�, ����, �տ�,������ų]"));
		}		
	}
}
