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

import server.LineageClient;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_AddSkill;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1Skills;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_SkillBuyOK extends ClientBasePacket {

	private static final String C_SKILL_BUY_OK = "[C] C_SkillBuyOK";

	public C_SkillBuyOK(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);

		int count = readH();
		int sid[] = new int[count];
		int price = 0;
		int level1 = 0;
		int level2 = 0;
		int level3 = 0;
		int level1_cost = 0;
		int level2_cost = 0;
		int level3_cost = 0;
		String skill_name = null;
		int skill_id = 0;

		L1PcInstance pc = clientthread.getActiveChar();
		if (pc.isGhost()) { return;	}
		
		for (int i = 0; i < count; i++) {
			sid[i] = readD();
			if( sid[i] > 24){
				pc.sendPackets(new S_Disconnect());
				return;
			} 

			switch (sid[i]) {
			// Lv1 ����
			case 0:
				level1 += 1;
				level1_cost += 100;
				break;
			case 1:
				level1 += 2;
				level1_cost += 100;
				break;
			case 2:
				level1 += 4;
				level1_cost += 100;
				break;
			case 3:
				level1 += 8;
				level1_cost += 100;
				break;
			case 4:
				level1 += 16;
				level1_cost += 100;
				break;
			case 5:
				level1 += 32;
				level1_cost += 100;
				break;
			case 6:
				level1 += 64;
				level1_cost += 100;
				break;
			case 7:
				level1 += 128;
				level1_cost += 100;
				break;

			// Lv2 ����
			case 8:
				level2 += 1;
				level2_cost += 400;
				break;
			case 9:
				level2 += 2;
				level2_cost += 400;
				break;
			case 10:
				level2 += 4;
				level2_cost += 400;
				break;
			case 11:
				level2 += 8;
				level2_cost += 400;
				break;
			case 12:
				level2 += 16;
				level2_cost += 400;
				break;
			case 13:
				level2 += 32;
				level2_cost += 400;
				break;
			case 14:
				level2 += 64;
				level2_cost += 400;
				break;
			case 15:
				level2 += 128;
				level2_cost += 400;
				break;

			// Lv3 ����
			case 16:
				level3 += 1;
				level3_cost += 900;
				break;
			case 17:
				level3 += 2;
				level3_cost += 900;
				break;
			case 18:
				level3 += 4;
				level3_cost += 900;
				break;
			case 19:
				level3 += 8;
				level3_cost += 900;
				break;
			case 20:
				level3 += 16;
				level3_cost += 900;
				break;
			case 21:
				level3 += 32;
				level3_cost += 900;
				break;
			case 22:
				level3 += 64;
				level3_cost += 900;
				break;
			case 23:
				level3 += 128;
				level3_cost += 900;
				break;

			default:
				break;
			}
		}

		if (!pc.isGm()) {
			switch (pc.getType()) {
			case 0: //ROYAL?
				if (pc.getLevel() < 10) {
					level1 = 0;
					level1_cost = 0;
				}
				if (pc.getLevel() < 20) {
					level2 = 0;
					level2_cost = 0;
				}
				level3 = 0;
				level3_cost = 0;
				break;

			case 1: //KNIGHT
				if (pc.getLevel() < 50) {
					level1 = 0;
					level1_cost = 0;
				}
				level2 = 0;
				level2_cost = 0;
				level3 = 0;
				level3_cost = 0;
				break;

			case 2: //ELF
				if (pc.getLevel() < 8) {
					level1 = 0;
					level1_cost = 0;
				}
				if (pc.getLevel() < 16) {
					level2 = 0;
					level2_cost = 0;
				}
				if (pc.getLevel() < 24) {
					level3 = 0;
					level3_cost = 0;
				}
				break;

			case 3: // WIZ
				if (pc.getLevel() < 4) {
					level1 = 0;
					level1_cost = 0;
				}
				if (pc.getLevel() < 8) {
					level2 = 0;
					level2_cost = 0;
				}
				if (pc.getLevel() < 12) {
					level3 = 0;
					level3_cost = 0;
				}
				break;

			case 4: // DE
				if (pc.getLevel() < 12) {
					level1 = 0;
					level1_cost = 0;
				}
				if (pc.getLevel() < 24) {
					level2 = 0;
					level2_cost = 0;
				}
				level3 = 0;
				level3_cost = 0;
				break;

			default:
				break;
			}
		}

		if (level1 == 0 && level2 == 0 && level3 == 0) {
			return;
		}
		price = level1_cost + level2_cost + level3_cost;
		if (pc.getInventory().checkItem(L1ItemId.ADENA, price)) {
			pc.getInventory().consumeItem(L1ItemId.ADENA, price);
			S_SkillSound s_skillSound = new S_SkillSound(pc.getId(), 224);
			pc.sendPackets(s_skillSound);
			Broadcaster.broadcastPacket(pc, s_skillSound);
			pc.sendPackets(new S_AddSkill(level1, level2, level3, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

			if ((level1 & 1) == 1) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(1);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level1 & 2) == 2) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(2);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level1 & 4) == 4) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(3);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level1 & 8) == 8) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(4);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level1 & 16) == 16) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(5);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level1 & 32) == 32) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(6);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level1 & 64) == 64) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(7);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level1 & 128) == 128) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(8);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}

			if ((level2 & 1) == 1) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(9);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level2 & 2) == 2) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(10);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level2 & 4) == 4) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(11);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level2 & 8) == 8) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(12);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level2 & 16) == 16) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(13);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level2 & 32) == 32) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(14);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level2 & 64) == 64) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(15);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id,
						skill_name, 0, 0);
			}
			if ((level2 & 128) == 128) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(16);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}

			if ((level3 & 1) == 1) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(17);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level3 & 2) == 2) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(18);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level3 & 4) == 4) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(19);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level3 & 8) == 8) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(20);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level3 & 16) == 16) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(21);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level3 & 32) == 32) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(22);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
			if ((level3 & 64) == 64) {
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(23);
				skill_name = l1skills.getName();
				skill_id = l1skills.getSkillId();
				SkillsTable.getInstance().spellMastery(pc.getId(), skill_id, skill_name, 0, 0);
			}
		} else {
			pc.sendPackets(new S_ServerMessage(189)); // \f1�Ƶ����� �����մϴ�.
		}
	}

	@Override
	public String getType() {
		return C_SKILL_BUY_OK;
	}

}
