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

		if (delay_id != 0) { // 지연 설정 있어
			if (cha.hasItemDelay(delay_id) == true) {
				return;
			}
		}
		/** 사용 부분 **/
		consumeHealingPotion(cha, itemId);
		cha.getInventory().removeItem(useItem, 1);
		L1ItemDelay.onItemUse(cha, useItem); // 아이템 지연 개시
	}
	/** 이팩트 번호 
	 * 빨갱이 : 189
	 * 주홍이 : 194
	 * 말갱이 : 197
	 **/
	public void consumeHealingPotion(L1Character cha, int item_id){
		int effect = 0;
		int heal = 0;
		switch (item_id) {
		case 40010: //체력 회복제 0
		case 40019: //농축 체력 회복제0
		case 40022://고대의 체력 회복제0
		case 40029: //상아탑의 체력 회복제0
			heal = calcHealing(cha, 9, 45, 0); effect = 189;
			break;
		case 140010: //축복받은 체력 회복제0
			heal = calcHealing(cha, 9, 45, 1); effect = 189;
			break;
		case 240010: //저주받은 체력 회복제0
			heal = calcHealing(cha, 9, 45, -1); effect = 189;
			break;
		case 40011://고급 체력 회복제0
		case 40020://고급 농축 체력 회복제0
		case 40023: //고대의 고급 체력 회복제0
			heal = calcHealing(cha, 33, 89, 0); effect = 194;
			break;
		case 140011: //축복받은 고급 체력 회복제0
			heal = calcHealing(cha, 33, 89, 1); effect = 194;
			break;
		case 40012://강력 체력 회복제0
		case 40021://농축 강력 체력 회복제0
		case 40024://고대의 강력 체력 회복제0
		case 435000://할로윈 호박 파이(2009)
		case 555580://응집된 화합물
			heal = calcHealing(cha, 55, 135, 0); effect = 197;
			break;
		case 5000169: // 할로윈 딸기 캔디0
		case 5000170: // 할로윈 밀크 캔디0
		case 5000171:
		case 5000172:
		case 5000173:
			heal = calcHealing(cha, 55, 135, 0); effect = 197;
		case 140012: //축복받은 강력 체력 회복제0
			heal = calcHealing(cha, 55, 135, 1); effect = 197;
			break;
		case 40026: //바나나 주스0
		case 40027: //오렌지 주스0
		case 40028: //사과 주스0
			heal = calcHealing(cha, 11, 65, 0); effect = 189;
			break;
		case 41141://신비한 힐링 포션0
			//[예정 : 신비한 농축 힐링포션 추가] 신비한힐링포션과 회복량은 같으나 무계 절반
			heal = calcHealing(cha, 23, 56, 0); effect = 189;
			break;
		case 40043: //토끼의 간0
			heal = calcHealing(cha, 141, 1384, 0); effect = 189;
			break;
		case 40058: //그을린 빵조각0
			heal = calcHealing(cha, 18, 58, 0); effect = 189;
			break;
		case 40071: //타다남은 빵조각0
			heal = calcHealing(cha, 46, 137, 0); effect = 197;
			break;
		case 40506: //엔트의 열매0
			heal = calcHealing(cha, 56, 136, 0); effect = 197;
			break;
		case 140506://축복받은 엔트의 열매0
			heal = calcHealing(cha, 56, 136, 1); effect = 197;
			break;
		case 40930: //바베큐0
			heal = calcHealing(cha, 79, 183, 0); effect = 189;
			break;
		case 41298: //어린 물고기0
			heal = calcHealing(cha, 8, 10, 0); effect = 189;
			break;
		case 41299: //재빠른 물고기0
			heal = calcHealing(cha, 7, 23, 0); effect = 194;
			break;
		case 41300: //강한 물고기0
			heal = calcHealing(cha, 11, 65, 0); effect = 197;
			break;
		case 41337: //축복받은 보리빵0
			heal = calcHealing(cha, 44, 107, 0); effect = 197;
			break;
		case 41403: //쿠작의 식량0
			heal = calcHealing(cha, 124, 600, 0); effect = 189;
			break;
			/** 본섭에 없는 아이템 **/
		case 41417: //감 얼음 딸기(본섭에 없는 아이템) 197 0
		case 41418: //감 얼음 레몬(본섭에 없는 아이템) 197 0
		case 41419: //감 얼음 망고(본섭에 없는 아이템) 197 0
		case 41420: //감 얼음 멜론(본섭에 없는 아이템) 197 0
		case 41421: //감 얼음 빨간콩(본섭에 없는 아이템) 197 0
			heal = calcHealing(cha, 50, 80, 0); effect = 197;
			break;
		case L1ItemId.MYSTERY_THICK_HEALING_POTION: // 신비한 농축 힐링포션
			heal = calcHealing(cha, 9, 45, 0); effect = 189;
			break;
		case 140024: // 시원한 체력의 물약 
			heal = calcHealing(cha, 40, 50, 0); effect = 197;
			break;
		case 40734: //신뢰의 코인 (본섭에 없는 아이템) 189 0
			heal = calcHealing(cha, 40, 50, 1); effect = 189;
			break;
		case 41411: //은쫑즈 (본섭에 없는 대만 명절음식) 
			heal = calcHealing(cha, 40, 50, 1); effect = 197;
			break;
		case 41412: //금쫑즈 (본섭에 없는 대만 명절음식)
			heal = calcHealing(cha, 50, 60, 1); effect = 194;
			break;
		}
		UseHeallingPotion(cha, heal, effect);
	}
	/** 물약의 회복량 연산 함수 **/
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
		//폴루트 워터일 경우 회복량 반감
		if (cha.getSkillEffectTimerSet().hasSkillEffect(POLLUTE_WATER)) {
			heal /= 2;
		}
		return heal;
	}

	private void UseHeallingPotion(L1Character cha, int healHp, int gfxid) {
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // 디케이포션 상태
				pc.sendPackets(new S_ServerMessage(698)); // 마력에 의해 아무것도 마실 수가 없습니다.
				return;
			}		
			// 앱솔루트베리어의 해제
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

