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
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class EnchantWeapon extends Enchant{

	private static Random _random = new Random(System.nanoTime());

	public EnchantWeapon(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			int itemId = this.getItemId();
			L1ItemInstance l1iteminstance1 = pc.getInventory().getItem(packet.readD());
			if (pc.getLastEnchantItemid() == l1iteminstance1.getId()){
				pc.setLastEnchantItemid(l1iteminstance1.getId(), l1iteminstance1);
				return;
			}
			if (l1iteminstance1 == null	|| l1iteminstance1.getItem().getType2() != 1) {
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
				return;
			}
			if(l1iteminstance1.getBless() >= 128){          //봉인템
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
				return;
			}
			int safe_enchant = l1iteminstance1.getItem().get_safeenchant();
			if (safe_enchant < 0) { // 강화 불가
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
				return;
			}
			int weaponId = l1iteminstance1.getItem().getItemId();
			if (weaponId >= 246 && weaponId <= 249) { // 강화 불가
				if (itemId == L1ItemId.SCROLL_OF_ENCHANT_QUEST_WEAPON) {// 시련의 스크롤
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
					return;
				}
			}
			if (itemId == L1ItemId.SCROLL_OF_ENCHANT_QUEST_WEAPON) {
				// 시련의 스크롤
				if (weaponId >= 246 && weaponId <= 249) { // 강화 불가
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
					return;
				}
			}
			/** 환상의 무기 마법 주문서**/				
			if (weaponId >= 413000 && weaponId <= 413007) { // 이외에 강화 불가
				if (itemId == L1ItemId.SCROLL_OF_ENCHANT_FANTASY_WEAPON) {// 환상의무기마법주문서
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
					return;
				}
			}
			if (itemId == L1ItemId.SCROLL_OF_ENCHANT_FANTASY_WEAPON) {// 환상의무기마법주문서
				if (weaponId >= 413000 && weaponId <= 413007) { // 이외에 강화 불가
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
					return;
				}
			}
			/** 환상의 무기 마법 주문서**/

			/** 창천 무기 마법 주문서**/				
			if (weaponId >= 411000 && weaponId <= 411035) {
				if (itemId == L1ItemId.CHANGCHUN_ENCHANT_WEAPON_SCROLL) {
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			if (itemId == L1ItemId.CHANGCHUN_ENCHANT_WEAPON_SCROLL) {
				if (weaponId >= 411000 && weaponId <= 411035) {
				} else {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}
			/** 창천 무기 마법 주문서**/
			int enchant_level = l1iteminstance1.getEnchantLevel();

			if (Config.GAME_SERVER_TYPE == 1 && enchant_level >= safe_enchant+3
					&& (itemId != L1ItemId.WIND_ENCHANT_WEAPON_SCROLL || itemId != L1ItemId.EARTH_ENCHANT_WEAPON_SCROLL
					|| itemId != L1ItemId.WATER_ENCHANT_WEAPON_SCROLL || itemId != L1ItemId.FIRE_ENCHANT_WEAPON_SCROLL)){
				pc.sendPackets(new S_SystemMessage("테스트서버에서는 안전인챈+3 이상은 인챈하실수 없습니다."));				
				return;
			}
			/*	if(enchant_level >= 8){ //무기 8부터폭죽
				pc.sendPackets(new S_SkillSound(pc.getId(), 2048));
			//	pc.broadcastPackets(new S_SkillSound(pc.getId(), 2048));

			}*/

			if (itemId == L1ItemId.C_SCROLL_OF_ENCHANT_WEAPON) { // c-dai
				pc.getInventory().removeItem(useItem, 1);
				if (enchant_level < -6) {
					// -7이상은 할 수 없다.
					FailureEnchant(pc, l1iteminstance1);
				} else {
					SuccessEnchant(pc, l1iteminstance1, -1);
				}
			} else if (	itemId == L1ItemId.WIND_ENCHANT_WEAPON_SCROLL || itemId == L1ItemId.EARTH_ENCHANT_WEAPON_SCROLL
					|| itemId == L1ItemId.WATER_ENCHANT_WEAPON_SCROLL || itemId == L1ItemId.FIRE_ENCHANT_WEAPON_SCROLL
					){
				AttrEnchant(pc, l1iteminstance1, itemId);
				pc.getInventory().removeItem(useItem, 1);
			} else if (enchant_level < safe_enchant) {
				pc.getInventory().removeItem(useItem, 1);
				SuccessEnchant(pc, l1iteminstance1, RandomELevel(l1iteminstance1, itemId));
			} else {
				if (enchant_level >= Config.MAX_WEAPON){
					if (!(itemId == L1ItemId.WIND_ENCHANT_WEAPON_SCROLL && itemId == L1ItemId.EARTH_ENCHANT_WEAPON_SCROLL                 
							|| itemId == L1ItemId.WATER_ENCHANT_WEAPON_SCROLL && itemId == L1ItemId.FIRE_ENCHANT_WEAPON_SCROLL)){
						pc.sendPackets(new S_ServerMessage(79));  //\f1아무일도 일어나지 않았습니다.
						return;
					}	
				}
				pc.getInventory().removeItem(useItem, 1);
				int rnd = _random.nextInt(100) + 1;
				int enchant_chance_wepon;		

				switch(weaponId){
				case 12: // 바람 칼날의 단검
				case 61: // 진명황의 집행검
				case 86: // 붉은 그림자의 이도류
				case 134: // 수정 결정체 지팡이
				case 191: // 살천의 활
				case 203: // 앨리스 8단계
				case 213: // 기르타스의 지팡이
				case 217: // 기르타스의 검
				case 294: // 군주의 대검
					switch(enchant_level){
					case 0: case 1: case 2: case 3: case 4: case 5:
						enchant_chance_wepon = (Config.ENCHANT_CHANCE_WEAPON); break; 
					case 6:	case 7:	case 8:	case 9:	case 10: case 11: case 12: case 13: case 14: case 15:
						enchant_chance_wepon = 1; break;
					default: enchant_chance_wepon = 1; break;
					}
					break;
				case 450010: case 450011: case 450012: case 450013: case 450014: case 450015: // 숨겨진 마족의~
				case 119: case 123: case 124: // 데몬의지팡이,베레스의지팡이,바포메트의지팡이
					switch(enchant_level){
					case 0: case 1: case 2: case 3: case 4: case 5:	
						enchant_chance_wepon = (Config.ENCHANT_CHANCE_WEAPON); break; 
					case 6: case 7: case 8:	case 9: case 10: case 11: case 12: case 13: case 14: case 15: 
						enchant_chance_wepon = (Config.ENCHANT_CHANCE_WEAPON) / 2; break; 
					default: enchant_chance_wepon = 1; break;
					}
					break;
				case 450022: case 450023: case 450024: case 450025: // 신묘한~
					switch(enchant_level){
					case 0: case 1: case 2: case 3: case 4: case 5:
						enchant_chance_wepon = 50 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 6: enchant_chance_wepon = 30 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 7:	enchant_chance_wepon = Config.ENCHANT_CHANCE_WEAPON; break;
					case 8: case 9: case 10: case 11: case 12: case 13: case 14: case 15: enchant_chance_wepon = 1 ; break;
					default: enchant_chance_wepon = 0; break;
					}
					break;
				case 54: // 커츠의 검
				case 58: // 데스나이트의 불검
				case 59: // 나이트발드의 양손검
				case 84: // 흑왕도
				case 85: // 악마의이도류
				case 164: // 흑왕아
				case 165: // 악마의 크로우
				case 189: // 흑왕궁
				case 190: // 사이하의활
				case 205: // 달의장궁
				case 100084: // 축 흑왕도
				case 100164: // 축 흑왕아
				case 100189: // 측 흑왕궁
				case 413101: case 413102: case 413103: case 413104: // 악마왕의~
				case 415010: case 415011: case 415012: case 415013: // 테베 오시리스의~
				case 450031: // 할로윈 호박 양손검 2011
				case 450032: // 할로윈 호박 각궁 2011
				case 450033: // 할로윈 호박 지팡이 2011
					switch(enchant_level){
					case 0: 
					case 1: 
					case 2: 
					case 3:
					case 4:
					case 5:
					case 6: enchant_chance_wepon = 90 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 7:	enchant_chance_wepon = 30 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 8: enchant_chance_wepon = 10 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 9: case 10: case 11: case 12: case 13: case 14: case 15: enchant_chance_wepon = 1 ; break;
					default: enchant_chance_wepon = 40 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					}
					break;
				case 121: // 얼음여왕의 지팡이
				case 100009: // 축 오리하리콘단검
				case 100049: // 축 무관의 장검
				case 100062: // 축 무관의 양손검
				case 412000: // 뇌신검
				case 412001: // 파멸의대검
				case 412002: // 마력의 단검
				case 412003: // 아크메이지의지팡이
				case 415016: // 쿠쿨칸의 창
					switch(enchant_level){
					case 6: enchant_chance_wepon = 90 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 7:	enchant_chance_wepon = 70 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 8: enchant_chance_wepon = 5 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 9: case 10: case 11: case 12: case 13: case 14: case 15: enchant_chance_wepon = 1 ; break;
					default: enchant_chance_wepon = 40 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					}
					break;
				case 9: // 오리하루콘 단검
				case 49: // 무관의장검
				case 62: // 무관의 양손검
				case 76: // 론드의 이도류
				case 81: // 흑빛의 이도류
				case 162: // 흑빛의 크로우
				case 188: // 라스타바드 헤비 크로스보우
				case 410000: // 소멸자의 체인소드
				case 410001: // 파멸자의 체인소드
				case 410003: // 사파이어 키링크
					switch(enchant_level){
					case 6: enchant_chance_wepon = 90 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 7:	enchant_chance_wepon = 40 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 8: enchant_chance_wepon = 20 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 9: 
						enchant_chance_wepon = 1 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 10: case 11: case 12: case 13: case 14: case 15: 
						enchant_chance_wepon = 1; break;
					default: enchant_chance_wepon = 40 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					}
					break;
				default:
					switch(enchant_level){
					case 6: case 7:	case 8: case 9: 
						enchant_chance_wepon = 80 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 10: case 11: case 12: case 13: case 14: case 15: enchant_chance_wepon = 1; break;
					default: enchant_chance_wepon = 40 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					}
					break;
				}

				if (rnd < enchant_chance_wepon) {
					int randomEnchantLevel = RandomELevel(l1iteminstance1, itemId);
					SuccessEnchant(pc, l1iteminstance1, randomEnchantLevel);

				} else if (enchant_level >= 9 && rnd < (enchant_chance_wepon)) {
					// \f1%0이%2과 강렬하게%1 빛났습니다만, 다행히 무사하게 살았습니다.
					pc.sendPackets(new S_ServerMessage(160, l1iteminstance1.getLogName(), "$245", "$248"));
				} else {
					FailureEnchant(pc, l1iteminstance1);
				}
			}
		}
	}
}

