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

import static l1j.server.server.model.skill.L1SkillId.POLLUTE_WATER;

import java.util.Random;

import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1ItemDelay;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1EtcItem;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class HealingPotion extends L1ItemInstance{
	private static Random _random = new Random(System.nanoTime());

	public HealingPotion(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		L1ItemInstance useItem = cha.getInventory().getItem(this.getId());
		int itemId = useItem.getItemId();
		int delay_id = 0;
		delay_id = ((L1EtcItem) useItem.getItem()).get_delayid();

		if (delay_id != 0) { // ���� ���� �־�
			if (cha.hasItemDelay(delay_id) == true) {
				return;
			}
		}
		/** ��� �κ� **/
		consumeHealingPotion(cha, itemId);
		cha.getInventory().removeItem(useItem, 1);
		L1ItemDelay.onItemUse(cha, useItem); // ������ ���� ����
	}
	/** ����Ʈ ��ȣ 
	 * ������ : 189
	 * ��ȫ�� : 194
	 * ������ : 197
	 **/
	public void consumeHealingPotion(L1Character cha, int item_id){
		int effect = 0;
		int heal = 0;
		switch (item_id) {
		case 40010: //ü�� ȸ���� 0
		case 40019: //���� ü�� ȸ����0
		case 40022://����� ü�� ȸ����0
		case 40029: //���ž�� ü�� ȸ����0
			heal = calcHealing(cha, 9, 45, 0); effect = 189;
			break;
		case 140010: //�ູ���� ü�� ȸ����0
			heal = calcHealing(cha, 9, 45, 1); effect = 189;
			break;
		case 240010: //���ֹ��� ü�� ȸ����0
			heal = calcHealing(cha, 9, 45, -1); effect = 189;
			break;
		case 40011://��� ü�� ȸ����0
		case 40020://��� ���� ü�� ȸ����0
		case 40023: //����� ��� ü�� ȸ����0
			heal = calcHealing(cha, 33, 89, 0); effect = 194;
			break;
		case 140011: //�ູ���� ��� ü�� ȸ����0
			heal = calcHealing(cha, 33, 89, 1); effect = 194;
			break;
		case 40012://���� ü�� ȸ����0
		case 40021://���� ���� ü�� ȸ����0
		case 40024://����� ���� ü�� ȸ����0
		case 435000://�ҷ��� ȣ�� ����(2009)
		case 555580://������ ȭ�չ�
			heal = calcHealing(cha, 55, 135, 0); effect = 197;
			break;
		case 5000169: // �ҷ��� ���� ĵ��0
		case 5000170: // �ҷ��� ��ũ ĵ��0
		case 5000171:
		case 5000172:
		case 5000173:
			heal = calcHealing(cha, 55, 135, 0); effect = 197;
		case 140012: //�ູ���� ���� ü�� ȸ����0
			heal = calcHealing(cha, 55, 135, 1); effect = 197;
			break;
		case 40026: //�ٳ��� �ֽ�0
		case 40027: //������ �ֽ�0
		case 40028: //��� �ֽ�0
			heal = calcHealing(cha, 11, 65, 0); effect = 189;
			break;
		case 41141://�ź��� ���� ����0
			//[���� : �ź��� ���� �������� �߰�] �ź����������ǰ� ȸ������ ������ ���� ����
			heal = calcHealing(cha, 23, 56, 0); effect = 189;
			break;
		case 40043: //�䳢�� ��0
			heal = calcHealing(cha, 141, 1384, 0); effect = 189;
			break;
		case 40058: //������ ������0
			heal = calcHealing(cha, 18, 58, 0); effect = 189;
			break;
		case 40071: //Ÿ�ٳ��� ������0
			heal = calcHealing(cha, 46, 137, 0); effect = 197;
			break;
		case 40506: //��Ʈ�� ����0
			heal = calcHealing(cha, 56, 136, 0); effect = 197;
			break;
		case 140506://�ູ���� ��Ʈ�� ����0
			heal = calcHealing(cha, 56, 136, 1); effect = 197;
			break;
		case 40930: //�ٺ�ť0
			heal = calcHealing(cha, 79, 183, 0); effect = 189;
			break;
		case 41298: //� �����0
			heal = calcHealing(cha, 8, 10, 0); effect = 189;
			break;
		case 41299: //����� �����0
			heal = calcHealing(cha, 7, 23, 0); effect = 194;
			break;
		case 41300: //���� �����0
			heal = calcHealing(cha, 11, 65, 0); effect = 197;
			break;
		case 41337: //�ູ���� ������0
			heal = calcHealing(cha, 44, 107, 0); effect = 197;
			break;
		case 41403: //������ �ķ�0
			heal = calcHealing(cha, 124, 600, 0); effect = 189;
			break;
			/** ������ ���� ������ **/
		case 41417: //�� ���� ����(������ ���� ������) 197 0
		case 41418: //�� ���� ����(������ ���� ������) 197 0
		case 41419: //�� ���� ����(������ ���� ������) 197 0
		case 41420: //�� ���� ���(������ ���� ������) 197 0
		case 41421: //�� ���� ������(������ ���� ������) 197 0
			heal = calcHealing(cha, 50, 80, 0); effect = 197;
			break;
		case L1ItemId.MYSTERY_THICK_HEALING_POTION: // �ź��� ���� ��������
			heal = calcHealing(cha, 9, 45, 0); effect = 189;
			break;
		case 140024: // �ÿ��� ü���� ���� 
			heal = calcHealing(cha, 40, 50, 0); effect = 197;
			break;
		case 40734: //�ŷ��� ���� (������ ���� ������) 189 0
			heal = calcHealing(cha, 40, 50, 1); effect = 189;
			break;
		case 41411: //������ (������ ���� �븸 ��������) 
			heal = calcHealing(cha, 40, 50, 1); effect = 197;
			break;
		case 41412: //������ (������ ���� �븸 ��������)
			heal = calcHealing(cha, 50, 60, 1); effect = 194;
			break;
		}
		UseHeallingPotion(cha, heal, effect);
	}
	/** ������ ȸ���� ���� �Լ� **/
	private int calcHealing(L1Character cha, int minheal, int maxheal, int blessed){
		int heal = 0;
		int variable = 0;
		if(maxheal > 0){
			heal = minheal;
			variable = ( maxheal - minheal ) / 3;
			heal += _random.nextInt(variable)+1;
		}
		if(blessed == 1){
			heal += _random.nextInt(minheal/2);
		}
		if(blessed == -1){
			heal -= _random.nextInt(minheal/2);
		}
		//����Ʈ ������ ��� ȸ���� �ݰ�
		if (cha.getSkillEffectTimerSet().hasSkillEffect(POLLUTE_WATER)) {
			heal /= 2;
		}
		return heal;
	}

	private void UseHeallingPotion(L1Character cha, int healHp, int gfxid) {
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // ���������� ����
				pc.sendPackets(new S_ServerMessage(698)); // ���¿� ���� �ƹ��͵� ���� ���� �����ϴ�.
				return;
			}		
			// �ۼַ�Ʈ�������� ����
			pc.cancelAbsoluteBarrier();
			int upHp = 0;
			if(pc.getInventory().checkEquipped(500008))	{		
				int cnt_enchant = pc.getInventory().getEnchantCount(500008);
				upHp = 2*(cnt_enchant+1);
				healHp = healHp * (upHp + 100) / 100 + upHp;   
			}
			pc.sendPackets(new S_SkillSound(pc.getId(), gfxid));
		}
		Broadcaster.broadcastPacket(cha, new S_SkillSound(cha.getId(), gfxid));
		cha.setCurrentHp(cha.getCurrentHp() + healHp);
	}
}

