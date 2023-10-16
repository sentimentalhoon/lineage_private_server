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
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Armor;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class EnchantArmor extends Enchant{

	private static Random _random = new Random(System.nanoTime());

	public EnchantArmor(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			L1ItemInstance l1iteminstance1 = pc.getInventory().getItem(packet.readD());
			int itemId = this.getItemId();
			int safe_enchant = ((L1Armor) l1iteminstance1.getItem()).get_safeenchant();
			int armorId = l1iteminstance1.getItem().getItemId();
			int armortype = l1iteminstance1.getItem().getType();
			int enchant_level = l1iteminstance1.getEnchantLevel();
			int randomEnchantLevel = RandomELevel(l1iteminstance1, itemId);

			if (pc.getLastEnchantItemid() == l1iteminstance1.getId()){
				pc.setLastEnchantItemid(l1iteminstance1.getId(), l1iteminstance1);
				return;
			}
			if (l1iteminstance1 == null || l1iteminstance1.getItem().getType2() != 2) {					
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
				return;
			}
			if(l1iteminstance1.getBless() >= 128){  //봉인템
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
				return;
			}

			if (safe_enchant < 0) { // 강화 불가
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
				return;
			}

			/**환상의 갑옷 마법 주문서**/				
			if (armorId >= 423000 && armorId <= 423008) {
				if (itemId == L1ItemId.SCROLL_OF_ENCHANT_FANTASY_ARMOR) {						
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			if (itemId == L1ItemId.SCROLL_OF_ENCHANT_FANTASY_ARMOR) {				
				if (armorId >= 423000 && armorId <= 423008) {
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			/**환상의 갑옷 마법 주문서**/
			if (itemId >= L1ItemId.Inadril_T_ScrollA && itemId <= L1ItemId.Inadril_T_ScrollD){
				if (!(armorId >= 490000 && armorId <= 490017)){
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			if (armorId >= 490000 && armorId <= 490017){
				if (!(itemId >= L1ItemId.Inadril_T_ScrollA && itemId <= L1ItemId.Inadril_T_ScrollD)){
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}

			/**창천의 갑옷 마법 주문서**/
			if (armorId >= 422000 && armorId <= 422020) {
				if (itemId == L1ItemId.CHANGCHUN_ENCHANT_ARMOR_SCROLL) {						
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			if (itemId == L1ItemId.CHANGCHUN_ENCHANT_ARMOR_SCROLL) {	
				if (armorId >= 422000 && armorId <= 422020) {
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			/**창천의 갑옷 마법 주문서**/

			/**장신구 강화 주문서 */
			if (itemId == L1ItemId.ACCESSORY_ENCHANT_SCROLL 
					|| itemId == L1ItemId.HALLOWEEN_2011_ACCESSORY_ENCHANT_SCROLL
					|| itemId == 430040|| itemId == 5000145){
				if (armortype >= 8 && armortype <= 12){						
				} else {	
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			if (armortype >= 8 && armortype <= 12){
				if (itemId == L1ItemId.ACCESSORY_ENCHANT_SCROLL
						|| itemId == L1ItemId.HALLOWEEN_2011_ACCESSORY_ENCHANT_SCROLL
						|| itemId == 430040|| itemId == 5000145){
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			/**장신구 강화 주문서 */

			/** 순백의 반지 강화 주문서 **/
			if (itemId == 430040){
				if (armorId >= 500000 && armorId <= 500004){
				} else {	
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			if (armorId >= 500000 && armorId <= 500004){
				if (itemId == 430040){
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			/** 순백의 반지 강화 주문서 **/

			/** 룸티스의 강화 주문서 **/
			if (itemId == 5000145){
				if (armorId >= 500007 && armorId <= 500009){
				} else {	
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			if (armorId >= 500007 && armorId <= 500009){
				if (itemId == 5000145){
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			/** 룸티스의 강화 주문서 **/

			if (enchant_level >= Config.MAX_ARMOR) {
				pc.sendPackets(new S_ServerMessage(79));  //\f1아무일도 일어나지 않았습니다.	
				return;
			}
			if (enchant_level == 8) {
				if (armorId >= 500007 && armorId <= 500009){ //룸티스
					pc.sendPackets(new S_ServerMessage(79));  //\f1아무일도 일어나지 않았습니다. 
					return;
				}
			}

			if (enchant_level >= Config.MAX_ACCESSORY) {
				if (armortype >= 8 && armortype <= 12 && getItem().getGrade() <= 2){
					pc.sendPackets(new S_ServerMessage(79));  //\f1아무일도 일어나지 않았습니다. 
					return;
				}
			}    

			if (itemId == L1ItemId.C_SCROLL_OF_ENCHANT_ARMOR|| itemId == L1ItemId.Inadril_T_ScrollC) { // c-zel
				pc.getInventory().removeItem(useItem, 1);
				int rnd = _random.nextInt(100) + 1;
				if (safe_enchant == 0 && rnd <= 30){
					FailureEnchant(pc, l1iteminstance1);
					return;
				}
				if (enchant_level < -6) {
					// -7이상은 할 수 없다.
					FailureEnchant(pc, l1iteminstance1);
				} else {
					SuccessEnchant(pc, l1iteminstance1, -1);
				}
			} else if ( itemId == L1ItemId.Inadril_T_ScrollD) {
				pc.getInventory().removeItem(useItem, 1);
				RegistEnchant(pc, l1iteminstance1, itemId);
			} else if (enchant_level < safe_enchant) {
				pc.getInventory().removeItem(useItem, 1);				
				SuccessEnchant(pc, l1iteminstance1,	RandomELevel(l1iteminstance1, itemId));				
			} else {
				pc.getInventory().removeItem(useItem, 1);
				int rnd = _random.nextInt(100) + 1;
				int enchant_chance_armor;
				int enchant_level_tmp;
				if (safe_enchant == 0) { // 뼈, 브락크미스릴용 보정
					enchant_level_tmp = 2;
				} else {
					enchant_level_tmp = 1;
				}

				if (armortype >= 8 && armortype <= 12){  // 악세사리류
					switch(enchant_level){
					case 0:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (2); break;
					case 1:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (enchant_level * 2); break;	
					case 2:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (enchant_level * 3); break;
					case 3:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (enchant_level * 4); break;
					case 4:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (enchant_level * 5); break;
					case 5:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (enchant_level * 6); break;
					case 6:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (enchant_level * 5); break;
					case 7:	enchant_chance_armor = Config.ENCHANT_CHANCE_ACCESSORY - (enchant_level * 4); break;
					case 8:	enchant_chance_armor = 1; break;
					case 9:	case 10: case 11: enchant_chance_armor = 0; break;
					default:
						enchant_chance_armor = 90/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ACCESSORY;
						break;
					}
					/*if (enchant_level >= 9) {
						enchant_chance_armor = 80/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ACCESSORY;
					} else {
						enchant_chance_armor = 90/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ACCESSORY;
					}*/

				}else{  // 방어구류
					switch(armorId){
					case 420100: case 420101: case 420102: case 420103: // 3차 용갑옷 인챈률
					case 420104: case 420105: case 420106: case 420107: // 3차 용갑옷 인챈률
					case 420108: case 420109: case 420110: case 420111: // 3차 용갑옷 인챈률
					case 420112: case 420113: case 420114: case 420115: // 3차 용갑옷 인챈률
						switch(enchant_level){
						case 0: 
						case 1: 
						case 2: 
						case 3: 
						case 4: enchant_chance_armor = 60/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ARMOR; break; 
						case 5: 
						case 6:  enchant_chance_armor = Config.ENCHANT_CHANCE_ARMOR; break; 
						case 7:
						case 8: 
						case 9: enchant_chance_armor = 1; break;
						default:
							enchant_chance_armor = 0;
							break;
						}
					default:
						switch(enchant_level){
						case 0: 
						case 1: 
						case 2: 
						case 3: 
						case 4: enchant_chance_armor = 80/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ARMOR; break; 
						case 5: enchant_chance_armor = 70/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ARMOR; break;
						case 6: enchant_chance_armor = 60/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ARMOR; break; 
						case 7: enchant_chance_armor = 10/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ARMOR; break; 
						case 8: enchant_chance_armor = 5/((enchant_level-safe_enchant+1)*2)/(enchant_level/7!=0?1*2:1)/(enchant_level_tmp)+Config.ENCHANT_CHANCE_ARMOR; break; 
						case 9: enchant_chance_armor = 1; break;
						default:
							enchant_chance_armor = 0;
							break;
						}
						break;
					}
				}
				if (enchant_chance_armor <= 0) enchant_chance_armor = 0;
				if (rnd < enchant_chance_armor) {
					SuccessEnchant(pc, l1iteminstance1, randomEnchantLevel);
					pc.sendPackets(new S_SkillSound(pc.getId(), 2059));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 2059));
				} else if (enchant_level >= 9 && rnd < (enchant_chance_armor * 2)) {
					String item_name_id = l1iteminstance1.getName();
					String pm = "";
					String msg = "";
					if (enchant_level > 0) {
						pm = "+";
					}
					msg = (new StringBuilder()).append(pm + enchant_level).append(" ").append(item_name_id).toString();
					// \f1%0이%2과 강렬하게%1 빛났습니다만, 다행히 무사하게 살았습니다.
					pc.sendPackets(new S_ServerMessage(160, msg, "$252", "$248"));
				} else {
					FailureEnchant(pc, l1iteminstance1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 2168));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 2168));
				}
			}
		}
	}
}

