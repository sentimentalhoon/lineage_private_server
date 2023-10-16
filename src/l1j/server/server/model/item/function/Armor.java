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

import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1ItemDelay;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Armor;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class Armor extends L1ItemInstance{
	
	public Armor(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
		if (useItem.getItem().getType2() == 2) { // 종별：방어용 기구
			if (pc.isCrown() && useItem.getItem().isUseRoyal()
					|| pc.isKnight() && useItem.getItem().isUseKnight() 
					|| pc.isElf() && useItem.getItem().isUseElf() 
					|| pc.isWizard() && useItem.getItem().isUseMage()
					|| pc.isDarkelf() && useItem.getItem().isUseDarkelf()
					|| pc.isDragonknight() && useItem.getItem().isUseDragonKnight()
					|| pc.isIllusionist() && useItem.getItem().isUseBlackwizard()) {

				int min = ((L1Armor) useItem.getItem()).getMinLevel();
				int max = ((L1Armor) useItem.getItem()).getMaxLevel();
				if (min != 0 && min > pc.getLevel()) {
					// 이 아이템은%0레벨 이상이 되지 않으면 사용할 수 없습니다.
					pc.sendPackets(new S_ServerMessage(318, String.valueOf(min)));
				} else if (max != 0 && max < pc.getLevel()) {
					// 이 아이템은%d레벨 이하만 사용할 수 있습니다.
					// S_ServerMessage에서는 인수가 표시되지 않는다
					if (max < 50) { 
						pc.sendPackets(new S_PacketBox(S_PacketBox.MSG_LEVEL_OVER, max));
					} else {
						pc.sendPackets(new S_SystemMessage("이 아이템은" + max + "레벨 이하만 사용할 수 있습니다. "));
					}
				} else {
					UseArmor(pc, useItem);
				}
			} else {
				// \f1당신의 클래스에서는 이 아이템은 사용할 수 없습니다.
				pc.sendPackets(new S_ServerMessage(264));
			}
		}
		}
	}
	
	private void UseArmor(L1PcInstance activeChar, L1ItemInstance armor) {
		int type = armor.getItem().getType();		
		L1PcInventory pcInventory = activeChar.getInventory();
		boolean equipeSpace; // 장비 하는 개소가 비어 있을까
		if (type == 9) { // 링의 경우
			equipeSpace = pcInventory.getTypeEquipped(2, 9) <= 1;
		} else {
			equipeSpace = pcInventory.getTypeEquipped(2, type) <= 0;
		}

		if (equipeSpace && !armor.isEquipped()) { // 사용한 방어용 기구를 장비 하고 있지 않아, 그 장비 개소가 비어 있는 경우(장착을 시도한다)
			int polyid = activeChar.getGfxId().getTempCharGfx();

			if (!L1PolyMorph.isEquipableArmor(polyid, type)) { // 그 변신에서는 장비 불가
				return;
			}
			if (type == 7 && pcInventory.getTypeEquipped(2, 13) >= 1
					|| type == 13 && pcInventory.getTypeEquipped(2, 7) >= 1){
				activeChar.sendPackets(new S_ServerMessage(124)); // \f1 벌써 무엇인가를 장비 하고 있습니다.
				return;
			}

			if (type == 7 && activeChar.getWeapon() != null) { // 쉴드(shield)의 경우, 무기를 장비 하고 있으면(자) 양손 무기 체크
				if (activeChar.getWeapon().getItem().isTwohandedWeapon() && armor.getItem().getUseType() != 13) { // 양손 무기					
					activeChar.sendPackets(new S_ServerMessage(129)); // \f1양손의 무기를 무장한 채로 쉴드(shield)를 착용할 수 없습니다.
					return;				
				}
			}
			if (type == 3 && pcInventory.getTypeEquipped(2, 4) >= 1) { // 셔츠의 경우, 망토를 입지 않은가 확인
				activeChar.sendPackets(new S_ServerMessage(126, "$224", "$225")); // \f1%1상에%0를 입을 수 없습니다.
				return;
			} else if ((type == 3) && pcInventory.getTypeEquipped(2, 2) >= 1) { // 셔츠의 경우, 메일을 입지 않은가 확인
				activeChar.sendPackets(new S_ServerMessage(126, "$224", "$226")); // \f1%1상에%0를 입을 수 없습니다.
				return;
			} else if ((type == 2) && pcInventory.getTypeEquipped(2, 4) >= 1) { // 메일의 경우, 망토를 입지 않은가 확인
				activeChar.sendPackets(new S_ServerMessage(126, "$226", "$225")); // \f1%1상에%0를 입을 수 없습니다.
				return;
			}

			activeChar.cancelAbsoluteBarrier(); // 아브소르트바리아의 해제
			

			pcInventory.setEquipped(armor, true);
		} else if (armor.isEquipped()) { // 사용한 방어용 기구를 장비 하고 있었을 경우(탈착을 시도한다)
			if (armor.getItem().getBless() == 2) { // 저주해지고 있었을 경우
				activeChar.sendPackets(new S_ServerMessage(150)); // \f1 뗄 수가 없습니다. 저주를 걸칠 수 있고 있는 것 같습니다.
				return;
			}
			if (type == 3 && pcInventory.getTypeEquipped(2, 2) >= 1) { // 셔츠의 경우, 메일을 입지 않은가 확인
				activeChar.sendPackets(new S_ServerMessage(127)); // \f1그것은 벗을 수가 없습니다.
				return;
			} else if ((type == 2 || type == 3)
					&& pcInventory.getTypeEquipped(2, 4) >= 1) { // 셔츠와 메일의 경우, 망토를 입지 않은가 확인
				activeChar.sendPackets(new S_ServerMessage(127)); // \f1그것은 벗을 수가 없습니다.
				return;
			}
			if (type == 7) {
				if (activeChar.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SOLID_CARRIAGE)) {
					activeChar.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.SOLID_CARRIAGE);
				}
			}
			pcInventory.setEquipped(armor, false);
		} else {
			activeChar.sendPackets(new S_ServerMessage(124)); // \f1 벌써 무엇인가를 장비 하고 있습니다.
		}
		activeChar.setCurrentHp(activeChar.getCurrentHp());
		activeChar.setCurrentMp(activeChar.getCurrentMp());
		activeChar.sendPackets(new S_OwnCharAttrDef(activeChar));
		activeChar.sendPackets(new S_OwnCharStatus(activeChar));
		activeChar.sendPackets(new S_SPMR(activeChar));
		L1ItemDelay.onItemUse(activeChar, armor); // 아이템 지연 개시
	}
}

