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

package l1j.server.server.model.item.function;

import java.util.Random;

import l1j.server.Config;
import l1j.server.GameSystem.CrockSystem;
import l1j.server.server.Log.LogEnchantFail;
import l1j.server.server.Log.LogEnchantSuccess;
import l1j.server.server.datatables.LogEnchantTable;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Item;
import server.manager.eva;

@SuppressWarnings("serial")
public class Enchant extends L1ItemInstance {

	private static Random _random = new Random(System.nanoTime());

	public Enchant(L1Item item) {
		super(item);
	}

	public void SuccessEnchant(L1PcInstance pc, L1ItemInstance item, int i) {
		String s = "";
		String sa = "";
		String sb = "";
		String s1 = item.getName();
		String pm = "";
		if (item.getEnchantLevel() > 0) {
			pm = "+";
		}
		if (item.getItem().getType2() == 1) {
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				switch (i) {
				case -1:
					s = s1;
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					s = s1;
					sa = "$245";
					sb = "$247";
					break;

				case 2: // '\002'
					s = s1;
					sa = "$245";
					sb = "$248";
					break;

				case 3: // '\003'
					s = s1;
					sa = "$245";
					sb = "$248";
					break;
				}
			} else {
				switch (i) {
				case -1:
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					sa = "$245";
					sb = "$247";
					break;

				case 2: // '\002'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					sa = "$245";
					sb = "$248";
					break;

				case 3: // '\003'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					sa = "$245";
					sb = "$248";
					break;
				}
			}
		} else if (item.getItem().getType2() == 2) {
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				switch (i) {
				case -1:
					s = s1;
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					if (item.getItem().getGrade() < 0) {
						s = s1;
						sa = "$252";
						sb = "$247 ";
					} else {
						s = s1;
						sa = "$245";
						sb = "$248 ";
					}
					break;
				case 2: // '\002'
					s = s1;
					sa = "$252";
					sb = "$248 ";
					break;

				case 3: // '\003'
					s = s1;
					sa = "$252";
					sb = "$248 ";
					break;
				}
			} else {
				switch (i) {
				case -1:
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					sa = "$246";
					sb = "$247";
					break;

				case 1: // '\001'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					if (item.getItem().getGrade() < 0) {
						sa = "$252";
						sb = "$247 ";
					} else {
						sa = "$245";
						sb = "$248 ";
					}
					break;

				case 2: // '\002'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					sa = "$252";
					sb = "$248 ";
					break;

				case 3: // '\003'
					s = (new StringBuilder()).append(
							pm + item.getEnchantLevel()).append(" ").append(s1)
							.toString(); // \f1%0이%2%1 빛납니다.
					sa = "$252";
					sb = "$248 ";
					break;
				}
			}
		}
		pc.getInventory().setEquipped(item, false);
		int oldEnchantLvl = item.getEnchantLevel();
		int newEnchantLvl = item.getEnchantLevel() + i;
		int safe_enchant = item.getItem().get_safeenchant();  
		int enchantnum = i; // 인챈트로그 추가

		if (item.getProtection() == 1){
			item.setProtection(0);
			pc.setLastEnchantItemid(0, null);
			pc.sendPackets(new S_ServerMessage(161, s, sa, sb));   
			item.setEnchantLevel(newEnchantLvl);
			pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);
			pc.getInventory().saveItem(item, L1PcInventory.COL_ENCHANTLVL);
			pc.saveInventory();
		} else {
			pc.setLastEnchantItemid(0, null);
			pc.sendPackets(new S_ServerMessage(161, s, sa, sb));
			item.setEnchantLevel(newEnchantLvl);
			pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL); 
			pc.saveInventory();
		} 
		if (newEnchantLvl > safe_enchant) {
			pc.getInventory().saveItem(item, L1PcInventory.COL_ENCHANTLVL);
		}

		if (item.getItem().getType2() == 1
				&& Config.LOGGING_WEAPON_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.LOGGING_WEAPON_ENCHANT) {
				LogEnchantTable logenchant = new LogEnchantTable();
				logenchant.storeLogEnchant(pc.getId(), item.getId(),
						oldEnchantLvl, newEnchantLvl);
			}
		}
		if (item.getItem().getType2() == 2 && Config.LOGGING_ARMOR_ENCHANT != 0) {
			if (safe_enchant == 0
					|| newEnchantLvl >= Config.LOGGING_ARMOR_ENCHANT) {
				LogEnchantTable logenchant = new LogEnchantTable();
				logenchant.storeLogEnchant(pc.getId(), item.getId(),
						oldEnchantLvl, newEnchantLvl);
			}
		}
		if (item.getItem().getType2() == 1) {
			if (newEnchantLvl > safe_enchant){
				eva.writeMessage(-2, "[성공:W]" + pc.getName() +":" + oldEnchantLvl + "->" + newEnchantLvl + " / " + item.getName());
				LogEnchantSuccess les = new LogEnchantSuccess();
				les.storeLogEnchantSuccess(pc, item, oldEnchantLvl, newEnchantLvl, enchantnum);
			}
		}
		if (item.getItem().getType2() == 2) {
			if (newEnchantLvl > safe_enchant){
				eva.writeMessage(-2, "[성공:A]" + pc.getName() + ":" + oldEnchantLvl + "->" + newEnchantLvl + " / " + item.getName());
				LogEnchantSuccess les = new LogEnchantSuccess();
				les.storeLogEnchantSuccess(pc, item, oldEnchantLvl, newEnchantLvl, enchantnum);
			}
		}

		if (item.getItem().getType2() == 2) {
			if (item.isEquipped()) {
				if (item.getItem().getType() >= 8
						&& item.getItem().getType() <= 12) {
				} else {
					pc.getAC().addAc(-i);
				}
				int i2 = item.getItem().getItemId();
				switch(i2){
				case 20011: case 20110: case 120011: case 420108:
				case 420109: case 420110: case 420111: case 490008: case 490017: case 425108: case 500014: // 매직 // 헤룸, 매직 // 체인 메일
					pc.getResistance().addMr(i);
					pc.sendPackets(new S_SPMR(pc));
					break;
				case 20056: case 120056: case 220056: // 매직 클로크
					pc.getResistance().addMr(i * 2);
					pc.sendPackets(new S_SPMR(pc));
					break;
				case 20078: case 20079:
					pc.getResistance().addMr(i * 3);
					pc.sendPackets(new S_SPMR(pc));
					break;
				}
			}
			pc.sendPackets(new S_OwnCharStatus(pc));
		}
		//	pc.sendPackets(new S_SkillSound(pc.getId(), 763));
		//	Broadcaster.broadcastPacket(pc,new S_SkillSound(pc.getId(),763));//우아꺼
	}

	public void FailureEnchant(L1PcInstance pc, L1ItemInstance item) {
		String s = "";
		String sa = "";
		int itemId = item.getItem().getItemId();
		int itemType = item.getItem().getType2();
		int safe_enchant = item.getItem().get_safeenchant();  
		String nameId = item.getName();
		String pm = "";
		if (itemType == 1) { // 무기
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				s = nameId; // \f1%0%s 강렬하게%1 빛나더니 증발되어 사라집니다.
				sa = "$245";
			} else {
				if (item.getEnchantLevel() > 8) {
					pm = "+";
				}
				s = (new StringBuilder()).append(pm + item.getEnchantLevel())
						.append(" ").append(nameId).toString(); // \f1%0%s
				// 강렬하게%1 빛나더니
				// 증발되어 사라집니다.
				sa = "$245";
			}
		} else if (itemType == 2) { // 방어용 기구
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				s = nameId; // \f1%0%s 강렬하게%1 빛나더니 증발되어 사라집니다.
				if (item.getItem().getGrade() < 0)
					sa = " $252";
				else
					sa = "$245";
			} else {
				if (item.getEnchantLevel() > 4) {
					pm = "+";
				}
				s = (new StringBuilder()).append(pm + item.getEnchantLevel())
						.append(" ").append(nameId).toString(); // \f1%0%s
				// 강렬하게%1 빛나더니
				// 증발되어 사라집니다.
				if (item.getItem().getGrade() < 0)
					sa = " $252";
				else
					sa = "$245";
			}
		}
		if ((itemId >= 427110 && itemId <= 427112) || (itemId >= 450022 && itemId <= 450025)) { // 신묘템
			if(CrockSystem.getInstance().isOpen() // 시간의 균열이 열려있거나
					|| CrockSystem.getInstance().isContinuationTime()) { // 연장되어있으면 날아가지 않는다. 
				pc.getInventory().setEquipped(item, false); // 실패시 방어력 중첩안되게 장비해제.
				item.setEnchantLevel(0);
				pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);
				pc.saveInventory();
				pc.sendPackets(new S_ServerMessage(1310)); // 강렬하게 빛났지만 장비가 증발 되지는 않았습니다.
				if (itemType == 1)
				{
					eva.writeMessage(-2, "[실패:W]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				} 
				else if (itemType == 2)
				{
					eva.writeMessage(-2, "[실패:A]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				}
			} else {
				pc.getInventory().setEquipped(item, false); // 실패시 방어력 중첩안되게 장비해제.
				pc.setLastEnchantItemid(item.getId(), item);
				pc.sendPackets(new S_ServerMessage(164, s, sa));
				pc.getInventory().removeItem(item, item.getCount());
				pc.saveInventory();

				if (itemType == 1) {
					if (safe_enchant == 0) {
						eva.writeMessage(-2, "[실패:W]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
						LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
						lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
					} else if (item.getEnchantLevel() >= 8)	{
						eva.writeMessage(-2, "[실패:W]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
						LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
						lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
					} else if (item.getEnchantLevel() > safe_enchant){
						LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
						lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
					}
				} else if (itemType == 2) {
					if (safe_enchant == 0) {
						eva.writeMessage(-2, "[실패:A]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
						LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
						lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
					} else if (item.getEnchantLevel() >= 7)	{
						eva.writeMessage(-2, "[실패:A]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
						LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
						lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
					} else if (item.getEnchantLevel() > safe_enchant){
						LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
						lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
					}
				}
			}
		} else {
			pc.getInventory().setEquipped(item, false);
			/*pc.setLastEnchantItemid(item.getId(), item);
		pc.sendPackets(new S_ServerMessage(164, s, sa));
		pc.getInventory().removeItem(item, item.getCount());
		pc.saveInventory();*/
			if (item.getProtection() == 1){
				item.setProtection(0);
				item.setEnchantLevel(0);
				pc.getInventory().setEquipped(item, false); 
				pc.setLastEnchantItemid(item.getId(), item);
				pc.sendPackets(new S_ServerMessage(1308, item.getLogName())); // %0이(가) 마력의 힘으로 증발에서 보호 됩니다.
				pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);
				pc.getInventory().saveItem(item, L1PcInventory.COL_ENCHANTLVL);
				pc.saveInventory();
			} else {
				pc.setLastEnchantItemid(item.getId(), item);
				pc.sendPackets(new S_ServerMessage(164, s, sa));
				pc.getInventory().removeItem(item, item.getCount());
				pc.saveInventory();
			} // 
			if (itemType == 1) {
				if (safe_enchant == 0) {
					eva.writeMessage(-2, "[실패:W]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				} else if (item.getEnchantLevel() >= 8)	{
					eva.writeMessage(-2, "[실패:W]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				} else if (item.getEnchantLevel() > safe_enchant){
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				}
			} else if (itemType == 2) {
				if (safe_enchant == 0) {
					eva.writeMessage(-2, "[실패:A]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				} else if (item.getEnchantLevel() >= 7)	{
					eva.writeMessage(-2, "[실패:A]" + pc.getName() + ":" + Integer.toString(item.getEnchantLevel()) + " " + item.getName());
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				} else if (item.getEnchantLevel() > safe_enchant){
					LogEnchantFail lef = new LogEnchantFail(); // 인챈트로그 추가
					lef.storeLogEnchantFail(pc, item); // 인챈트로그 추가
				}
			}
			/*pc.sendPackets(new S_SkillSound(pc.getId(), 746));
		Broadcaster.broadcastPacket(pc,new S_SkillSound(pc.getId(),746));//내꺼*/
		}
	}




	public int RandomELevel(L1ItemInstance item, int itemId) {
		int j = _random.nextInt(100) + 1;
		switch(itemId){
		case L1ItemId.B_SCROLL_OF_ENCHANT_ARMOR:
		case L1ItemId.B_SCROLL_OF_ENCHANT_WEAPON:
		case L1ItemId.Inadril_T_ScrollB:
			if (item.getEnchantLevel() < 0) {
				if (j < 30) {
					return 2;
				} else {
					return 1;
				}
			} else if (item.getEnchantLevel() <= 2) {
				if (j < 49) {
					return 1;
				} else if (j >= 49 && j <= 98) {
					return 2;
				} else if (j >= 99 && j <= 100) {
					return 3;
				}
			} else if (item.getEnchantLevel() >= 3
					&& item.getEnchantLevel() <= 5) {
				if (j < 50) {
					return 2;
				} else {
					return 1;
				}
			}
			return 1;
		case 140129: case 140130:
			if (item.getEnchantLevel() < 0) {
				if (j < 30) {
					return 2;
				} else {
					return 1;
				}
			} else if (item.getEnchantLevel() <= 2) {
				if (j < 49) {
					return 1;
				} else if (j >= 49 && j <= 98) {
					return 2;
				} else if (j >= 99 && j <= 100) {
					return 3;
				}
			} else if (item.getEnchantLevel() >= 3
					&& item.getEnchantLevel() <= 5) {
				if (j < 60) {
					return 2;
				} else {
					return 1;
				}
			}
			return 1;
		}
		return 1;
	}
	public void RegistEnchant(L1PcInstance pc, L1ItemInstance item, int item_id) {
		int level = item.getRegistLevel();
		int chance = _random.nextInt(20) + 1;
		if (item_id == L1ItemId.Inadril_T_ScrollD) {
			switch(level){
			case 0:
				if (chance <= 3) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName())); // \f1%0에 강력한 마법의 힘이 스며 듭니다.
					item.setRegistLevel(1);
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName())); // \f1%0에 마법이 스며들지 못했습니다.
				}
				break;
			case 1:
				if (chance <= 2) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName())); // \f1%0에 강력한 마법의 힘이 스며 듭니다.
					item.setRegistLevel(2);
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName())); // \f1%0에 마법이 스며들지 못했습니다.
				}
				break;
			case 2:
				if (chance <= 1) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName())); // \f1%0에 강력한 마법의 힘이 스며 듭니다.
					item.setRegistLevel(3);
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName())); // \f1%0에 마법이 스며들지 못했습니다.
				}
				break;
			case 3:
				if (chance <= 1) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName())); // \f1%0에 강력한 마법의 힘이 스며 듭니다.
					item.setRegistLevel(4);
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName())); // \f1%0에 마법이 스며들지 못했습니다.
				}
				break;
			case 4:
				if (chance <= 1) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName())); // \f1%0에 강력한 마법의 힘이 스며 듭니다.
					item.setRegistLevel(5);
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName())); // \f1%0에 마법이 스며들지 못했습니다.
				}
				break;
			default:
				pc.sendPackets(new S_ServerMessage(79));
				break;
			}
			pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);
			pc.getInventory().saveItem(item, L1PcInventory.COL_ENCHANTLVL);
			pc.saveInventory();
		}
	}


	public void AttrEnchant(L1PcInstance pc, L1ItemInstance item, int item_id) {
		int attr_level = item.getAttrEnchantLevel();
		int chance = _random.nextInt(100) + 1;
		switch(item_id){
		case L1ItemId.FIRE_ENCHANT_WEAPON_SCROLL:
			switch(attr_level){
			case 0: case 4: case 5: case 6: case 7: case 8: 
			case 9: case 10: case 11: case 12:
				if (chance < 20) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(1);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 1:
				if (chance < 8) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(2);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 2:
				if (chance < 4) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(3);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 3:
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지
				// 않았습니다.
				return;
			}
			break;
		case L1ItemId.WATER_ENCHANT_WEAPON_SCROLL:
			// 강화
			// 주문서
			switch(attr_level){
			case 0: case 1: case 2: case 3: case 7: case 8: case 9: case 10: case 11: case 12:
				if (chance < 20) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(4);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 4:
				if (chance < 8) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(5);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 5:
				if (chance < 4) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(6);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 6:
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
				return;
			}
			break;
		case L1ItemId.WIND_ENCHANT_WEAPON_SCROLL:
			switch(attr_level){
			case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 10: case 11: case 12:
				if (chance < 20) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(7);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 7:
				if (chance < 8) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(8);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 8:
				if (chance < 4) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(9);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 9:
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지
				// 않았습니다.
				return;
			}
			break;
		case L1ItemId.EARTH_ENCHANT_WEAPON_SCROLL:
			switch(attr_level){
			case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
				if (chance < 20) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(10);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 10:
				if (chance < 8) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(11);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 11:
				if (chance < 4) {
					pc.sendPackets(new S_ServerMessage(1410, item.getLogName()));
					item.setAttrEnchantLevel(12);					
				} else {
					pc.sendPackets(new S_ServerMessage(1411, item.getLogName()));
				}
				break;
			case 12:
				pc.sendPackets(new S_SystemMessage("더이상 강화가 불가능합니다."));				
				return;
			}
			break;
		}

		pc.getInventory().updateItem(item, L1PcInventory.COL_ATTRENCHANTLVL);
		pc.getInventory().saveItem(item, L1PcInventory.COL_ATTRENCHANTLVL);
		pc.saveInventory();
	}
}
