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
package l1j.server.server.serverpackets;

import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.model.Instance.L1PcInstance;

public class S_ReturnedStat extends ServerBasePacket {
	
	private static final String S_ReturnedStat = "[S] S_ReturnedStat";
	private byte[] _byte = null;

	
	public static final int START = 1;
	
	public static final int LEVELUP = 2;
	
	public static final int END = 3;
	
	public static final int LOGIN = 4;
	
	public static final int PET_PARTY = 12;


	
	public S_ReturnedStat(L1PcInstance pc, int type) {
		buildPacket(pc, type);
	}	
	/** �� â **/
	public S_ReturnedStat(int type, int j, int k, boolean flag) {
		_byte = null;
		writeC(Opcodes.S_OPCODE_RETURNEDSTAT);
		writeC(type);
		switch (type) {
		case 12: // '\f'
			if (flag) {
				writeC(j);
				writeC(0);
				writeD(0);
			} else {
				writeC(j);
				writeC(0);
				writeC(1);
				writeC(0);
				writeC(0);
				writeC(0);
			}
			writeD(k);
			break;
		}
	}


	
	private void buildPacket(L1PcInstance pc, int type) {
		writeC(Opcodes.S_OPCODE_RETURNEDSTAT);
		writeC(type);
		switch (type) {
		case START:
			short init_hp = 0;
			short init_mp = 0;
			if (pc.isCrown()) { // CROWN
				init_hp = 14;
				switch (pc.getAbility().getBaseWis()) {
				case 11:
					init_mp = 2;
					break;
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 3;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 4;
					break;
				default:
					init_mp = 2;
					break;
				}
			} else if (pc.isKnight()) { // KNIGHT
				init_hp = 16;
				switch (pc.getAbility().getBaseWis()) {
				case 9:
				case 10:
				case 11:
					init_mp = 1;
					break;
				case 12:
				case 13:
					init_mp = 2;
					break;
				default:
					init_mp = 1;
					break;
				}
			} else if (pc.isElf()) { // ELF
				init_hp = 15;
				switch (pc.getAbility().getBaseWis()) {
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 4;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 6;
					break;
				default:
					init_mp = 4;
					break;
				}
			} else if (pc.isWizard()) { // WIZ
				init_hp = 12;
				switch (pc.getAbility().getBaseWis()) {
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 6;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 8;
					break;
				default:
					init_mp = 6;
					break;
				}
			} else if (pc.isDarkelf()) { // DE
				init_hp = 12;
				switch (pc.getAbility().getBaseWis()) {
				case 10:
				case 11:
					init_mp = 3;
					break;
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 4;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 6;
					break;
				default:
					init_mp = 3;
					break;
				}
			} else if (pc.isDragonknight()) { // ����
				init_hp = 16;
				init_mp = 2;
			} else if (pc.isIllusionist()) { // ȯ����
				init_hp = 14;
				switch (pc.getAbility().getBaseWis()) {
				case 10:
				case 11:
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 5;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 6;
					break;
				default:
					init_mp = 5;
					break;
				}
			}
			writeH(init_hp);
			writeH(init_mp);
			writeC(0x0a);
			writeC(ExpTable.getLevelByExp(pc.getReturnStat()));
			break;
		case LEVELUP:
			writeC(pc.getLevel());
			writeC(ExpTable.getLevelByExp(pc.getReturnStat()));
			writeH(pc.getBaseMaxHp());
			writeH(pc.getBaseMaxMp());
			writeH(pc.getBaseAc());
			writeC(pc.getAbility().getStr());
			writeC(pc.getAbility().getInt());
			writeC(pc.getAbility().getWis());
			writeC(pc.getAbility().getDex());
			writeC(pc.getAbility().getCon());
			writeC(pc.getAbility().getCha());
			break;
		case END:
			writeC(pc.getAbility().getElixirCount());
			break;
		case LOGIN:
			/*
			 * pc.getAblilyty���� ��ȯ�Ǵ� �ּ� ���Ȱ� �迭 ���� 
			 * 0:��/1:��/2:��/3:����/4:ī��/5:��Ʈ
			 */
			int minStat[] = new int[6]; 
			minStat = pc.getAbility().getMinStat(pc.getClassId());
			int first = minStat[0] + minStat[5] * 16;
			int second = minStat[3] + minStat[1] * 16;
			int third = minStat[2] + minStat[4] * 16;
			//System.out.println(first + "--" + second + "--" + third );
			writeC(first);  //int,str
			writeC(second);  //dex,wis
			writeC(third);  //cha,con
			writeC(0x00);
			break;
		}
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}
	
	public String getType() {
		return S_ReturnedStat;
	}
}