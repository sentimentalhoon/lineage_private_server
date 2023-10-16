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
		if (useItem.getItem().getType2() == 2) { // ���������� �ⱸ
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
					// �� ��������%0���� �̻��� ���� ������ ����� �� �����ϴ�.
					pc.sendPackets(new S_ServerMessage(318, String.valueOf(min)));
				} else if (max != 0 && max < pc.getLevel()) {
					// �� ��������%d���� ���ϸ� ����� �� �ֽ��ϴ�.
					// S_ServerMessage������ �μ��� ǥ�õ��� �ʴ´�
					if (max < 50) { 
						pc.sendPackets(new S_PacketBox(S_PacketBox.MSG_LEVEL_OVER, max));
					} else {
						pc.sendPackets(new S_SystemMessage("�� ��������" + max + "���� ���ϸ� ����� �� �ֽ��ϴ�. "));
					}
				} else {
					UseArmor(pc, useItem);
				}
			} else {
				// \f1����� Ŭ���������� �� �������� ����� �� �����ϴ�.
				pc.sendPackets(new S_ServerMessage(264));
			}
		}
		}
	}
	
	private void UseArmor(L1PcInstance activeChar, L1ItemInstance armor) {
		int type = armor.getItem().getType();		
		L1PcInventory pcInventory = activeChar.getInventory();
		boolean equipeSpace; // ��� �ϴ� ���Ұ� ��� ������
		if (type == 9) { // ���� ���
			equipeSpace = pcInventory.getTypeEquipped(2, 9) <= 1;
		} else {
			equipeSpace = pcInventory.getTypeEquipped(2, type) <= 0;
		}

		if (equipeSpace && !armor.isEquipped()) { // ����� ���� �ⱸ�� ��� �ϰ� ���� �ʾ�, �� ��� ���Ұ� ��� �ִ� ���(������ �õ��Ѵ�)
			int polyid = activeChar.getGfxId().getTempCharGfx();

			if (!L1PolyMorph.isEquipableArmor(polyid, type)) { // �� ���ſ����� ��� �Ұ�
				return;
			}
			if (type == 7 && pcInventory.getTypeEquipped(2, 13) >= 1
					|| type == 13 && pcInventory.getTypeEquipped(2, 7) >= 1){
				activeChar.sendPackets(new S_ServerMessage(124)); // \f1 ���� �����ΰ��� ��� �ϰ� �ֽ��ϴ�.
				return;
			}

			if (type == 7 && activeChar.getWeapon() != null) { // ����(shield)�� ���, ���⸦ ��� �ϰ� ������(��) ��� ���� üũ
				if (activeChar.getWeapon().getItem().isTwohandedWeapon() && armor.getItem().getUseType() != 13) { // ��� ����					
					activeChar.sendPackets(new S_ServerMessage(129)); // \f1����� ���⸦ ������ ä�� ����(shield)�� ������ �� �����ϴ�.
					return;				
				}
			}
			if (type == 3 && pcInventory.getTypeEquipped(2, 4) >= 1) { // ������ ���, ���並 ���� ������ Ȯ��
				activeChar.sendPackets(new S_ServerMessage(126, "$224", "$225")); // \f1%1��%0�� ���� �� �����ϴ�.
				return;
			} else if ((type == 3) && pcInventory.getTypeEquipped(2, 2) >= 1) { // ������ ���, ������ ���� ������ Ȯ��
				activeChar.sendPackets(new S_ServerMessage(126, "$224", "$226")); // \f1%1��%0�� ���� �� �����ϴ�.
				return;
			} else if ((type == 2) && pcInventory.getTypeEquipped(2, 4) >= 1) { // ������ ���, ���並 ���� ������ Ȯ��
				activeChar.sendPackets(new S_ServerMessage(126, "$226", "$225")); // \f1%1��%0�� ���� �� �����ϴ�.
				return;
			}

			activeChar.cancelAbsoluteBarrier(); // �ƺ�Ҹ�Ʈ�ٸ����� ����
			

			pcInventory.setEquipped(armor, true);
		} else if (armor.isEquipped()) { // ����� ���� �ⱸ�� ��� �ϰ� �־��� ���(Ż���� �õ��Ѵ�)
			if (armor.getItem().getBless() == 2) { // ���������� �־��� ���
				activeChar.sendPackets(new S_ServerMessage(150)); // \f1 �� ���� �����ϴ�. ���ָ� ��ĥ �� �ְ� �ִ� �� �����ϴ�.
				return;
			}
			if (type == 3 && pcInventory.getTypeEquipped(2, 2) >= 1) { // ������ ���, ������ ���� ������ Ȯ��
				activeChar.sendPackets(new S_ServerMessage(127)); // \f1�װ��� ���� ���� �����ϴ�.
				return;
			} else if ((type == 2 || type == 3)
					&& pcInventory.getTypeEquipped(2, 4) >= 1) { // ������ ������ ���, ���並 ���� ������ Ȯ��
				activeChar.sendPackets(new S_ServerMessage(127)); // \f1�װ��� ���� ���� �����ϴ�.
				return;
			}
			if (type == 7) {
				if (activeChar.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SOLID_CARRIAGE)) {
					activeChar.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.SOLID_CARRIAGE);
				}
			}
			pcInventory.setEquipped(armor, false);
		} else {
			activeChar.sendPackets(new S_ServerMessage(124)); // \f1 ���� �����ΰ��� ��� �ϰ� �ֽ��ϴ�.
		}
		activeChar.setCurrentHp(activeChar.getCurrentHp());
		activeChar.setCurrentMp(activeChar.getCurrentMp());
		activeChar.sendPackets(new S_OwnCharAttrDef(activeChar));
		activeChar.sendPackets(new S_OwnCharStatus(activeChar));
		activeChar.sendPackets(new S_SPMR(activeChar));
		L1ItemDelay.onItemUse(activeChar, armor); // ������ ���� ����
	}
}

