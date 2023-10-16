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
				pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
				return;
			}
			if(l1iteminstance1.getBless() >= 128){          //������
				pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
				return;
			}
			int safe_enchant = l1iteminstance1.getItem().get_safeenchant();
			if (safe_enchant < 0) { // ��ȭ �Ұ�
				pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
				return;
			}
			int weaponId = l1iteminstance1.getItem().getItemId();
			if (weaponId >= 246 && weaponId <= 249) { // ��ȭ �Ұ�
				if (itemId == L1ItemId.SCROLL_OF_ENCHANT_QUEST_WEAPON) {// �÷��� ��ũ��
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
					return;
				}
			}
			if (itemId == L1ItemId.SCROLL_OF_ENCHANT_QUEST_WEAPON) {
				// �÷��� ��ũ��
				if (weaponId >= 246 && weaponId <= 249) { // ��ȭ �Ұ�
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
					return;
				}
			}
			/** ȯ���� ���� ���� �ֹ���**/				
			if (weaponId >= 413000 && weaponId <= 413007) { // �̿ܿ� ��ȭ �Ұ�
				if (itemId == L1ItemId.SCROLL_OF_ENCHANT_FANTASY_WEAPON) {// ȯ���ǹ��⸶���ֹ���
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
					return;
				}
			}
			if (itemId == L1ItemId.SCROLL_OF_ENCHANT_FANTASY_WEAPON) {// ȯ���ǹ��⸶���ֹ���
				if (weaponId >= 413000 && weaponId <= 413007) { // �̿ܿ� ��ȭ �Ұ�
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
					return;
				}
			}
			/** ȯ���� ���� ���� �ֹ���**/

			/** âõ ���� ���� �ֹ���**/				
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
			/** âõ ���� ���� �ֹ���**/
			int enchant_level = l1iteminstance1.getEnchantLevel();

			if (Config.GAME_SERVER_TYPE == 1 && enchant_level >= safe_enchant+3
					&& (itemId != L1ItemId.WIND_ENCHANT_WEAPON_SCROLL || itemId != L1ItemId.EARTH_ENCHANT_WEAPON_SCROLL
					|| itemId != L1ItemId.WATER_ENCHANT_WEAPON_SCROLL || itemId != L1ItemId.FIRE_ENCHANT_WEAPON_SCROLL)){
				pc.sendPackets(new S_SystemMessage("�׽�Ʈ���������� ������æ+3 �̻��� ��æ�ϽǼ� �����ϴ�."));				
				return;
			}
			/*	if(enchant_level >= 8){ //���� 8��������
				pc.sendPackets(new S_SkillSound(pc.getId(), 2048));
			//	pc.broadcastPackets(new S_SkillSound(pc.getId(), 2048));

			}*/

			if (itemId == L1ItemId.C_SCROLL_OF_ENCHANT_WEAPON) { // c-dai
				pc.getInventory().removeItem(useItem, 1);
				if (enchant_level < -6) {
					// -7�̻��� �� �� ����.
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
						pc.sendPackets(new S_ServerMessage(79));  //\f1�ƹ��ϵ� �Ͼ�� �ʾҽ��ϴ�.
						return;
					}	
				}
				pc.getInventory().removeItem(useItem, 1);
				int rnd = _random.nextInt(100) + 1;
				int enchant_chance_wepon;		

				switch(weaponId){
				case 12: // �ٶ� Į���� �ܰ�
				case 61: // ����Ȳ�� �����
				case 86: // ���� �׸����� �̵���
				case 134: // ���� ����ü ������
				case 191: // ��õ�� Ȱ
				case 203: // �ٸ��� 8�ܰ�
				case 213: // �⸣Ÿ���� ������
				case 217: // �⸣Ÿ���� ��
				case 294: // ������ ���
					switch(enchant_level){
					case 0: case 1: case 2: case 3: case 4: case 5:
						enchant_chance_wepon = (Config.ENCHANT_CHANCE_WEAPON); break; 
					case 6:	case 7:	case 8:	case 9:	case 10: case 11: case 12: case 13: case 14: case 15:
						enchant_chance_wepon = 1; break;
					default: enchant_chance_wepon = 1; break;
					}
					break;
				case 450010: case 450011: case 450012: case 450013: case 450014: case 450015: // ������ ������~
				case 119: case 123: case 124: // ������������,��������������,������Ʈ��������
					switch(enchant_level){
					case 0: case 1: case 2: case 3: case 4: case 5:	
						enchant_chance_wepon = (Config.ENCHANT_CHANCE_WEAPON); break; 
					case 6: case 7: case 8:	case 9: case 10: case 11: case 12: case 13: case 14: case 15: 
						enchant_chance_wepon = (Config.ENCHANT_CHANCE_WEAPON) / 2; break; 
					default: enchant_chance_wepon = 1; break;
					}
					break;
				case 450022: case 450023: case 450024: case 450025: // �Ź���~
					switch(enchant_level){
					case 0: case 1: case 2: case 3: case 4: case 5:
						enchant_chance_wepon = 50 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 6: enchant_chance_wepon = 30 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 7:	enchant_chance_wepon = Config.ENCHANT_CHANCE_WEAPON; break;
					case 8: case 9: case 10: case 11: case 12: case 13: case 14: case 15: enchant_chance_wepon = 1 ; break;
					default: enchant_chance_wepon = 0; break;
					}
					break;
				case 54: // Ŀ���� ��
				case 58: // ��������Ʈ�� �Ұ�
				case 59: // ����Ʈ�ߵ��� ��հ�
				case 84: // ��յ�
				case 85: // �Ǹ����̵���
				case 164: // ��վ�
				case 165: // �Ǹ��� ũ�ο�
				case 189: // ��ձ�
				case 190: // ��������Ȱ
				case 205: // �������
				case 100084: // �� ��յ�
				case 100164: // �� ��վ�
				case 100189: // �� ��ձ�
				case 413101: case 413102: case 413103: case 413104: // �Ǹ�����~
				case 415010: case 415011: case 415012: case 415013: // �׺� ���ø�����~
				case 450031: // �ҷ��� ȣ�� ��հ� 2011
				case 450032: // �ҷ��� ȣ�� ���� 2011
				case 450033: // �ҷ��� ȣ�� ������ 2011
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
				case 121: // ���������� ������
				case 100009: // �� �����ϸ��ܴܰ�
				case 100049: // �� ������ ���
				case 100062: // �� ������ ��հ�
				case 412000: // ���Ű�
				case 412001: // �ĸ��Ǵ��
				case 412002: // ������ �ܰ�
				case 412003: // ��ũ��������������
				case 415016: // ����ĭ�� â
					switch(enchant_level){
					case 6: enchant_chance_wepon = 90 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 7:	enchant_chance_wepon = 70 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 8: enchant_chance_wepon = 5 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					case 9: case 10: case 11: case 12: case 13: case 14: case 15: enchant_chance_wepon = 1 ; break;
					default: enchant_chance_wepon = 40 / ((enchant_level - safe_enchant + 1) * 2) / (enchant_level / 9 != 0 ? 1 * 2 : 1) + Config.ENCHANT_CHANCE_WEAPON; break;
					}
					break;
				case 9: // �����Ϸ��� �ܰ�
				case 49: // ���������
				case 62: // ������ ��հ�
				case 76: // �е��� �̵���
				case 81: // ����� �̵���
				case 162: // ����� ũ�ο�
				case 188: // ��Ÿ�ٵ� ��� ũ�ν�����
				case 410000: // �Ҹ����� ü�μҵ�
				case 410001: // �ĸ����� ü�μҵ�
				case 410003: // �����̾� Ű��ũ
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
					// \f1%0��%2�� �����ϰ�%1 �������ϴٸ�, ������ �����ϰ� ��ҽ��ϴ�.
					pc.sendPackets(new S_ServerMessage(160, l1iteminstance1.getLogName(), "$245", "$248"));
				} else {
					FailureEnchant(pc, l1iteminstance1);
				}
			}
		}
	}
}

