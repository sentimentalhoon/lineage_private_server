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
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class Weapon extends L1ItemInstance{
	
	public Weapon(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			 if (useItem.getItem().getType2() == 1) {
					int min = useItem.getItem().getMinLevel();
					int max = useItem.getItem().getMaxLevel();
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
						if (pc.isGm()){
							UseWeapon(pc, useItem);
						} else if (pc.isCrown() && useItem.getItem().isUseRoyal()
								|| pc.isKnight() && useItem.getItem().isUseKnight()
								|| pc.isElf() && useItem.getItem().isUseElf()
								|| pc.isWizard() && useItem.getItem().isUseMage()
								|| pc.isDarkelf() && useItem.getItem().isUseDarkelf()
								|| pc.isDragonknight() && useItem.getItem().isUseDragonKnight()
								|| pc.isIllusionist() && useItem.getItem().isUseBlackwizard()) {
							UseWeapon(pc, useItem);
						} else {
							// \f1����� Ŭ���������� �� �������� ����� �� �����ϴ�.
							pc.sendPackets(new S_ServerMessage(264));
						}
					}
			 }
		}
	}
	
	private void UseWeapon(L1PcInstance activeChar, L1ItemInstance weapon) {
		L1PcInventory pcInventory = activeChar.getInventory();		
		if (activeChar.getWeapon() == null
				|| !activeChar.getWeapon().equals(weapon)) { // ������ ���Ⱑ ��� �ϰ� �ִ� ����� �ٸ� ���, ��� �� �� ������ Ȯ��
			int weapon_type = weapon.getItem().getType();
			int polyid = activeChar.getGfxId().getTempCharGfx();

			if (!L1PolyMorph.isEquipableWeapon(polyid, weapon_type)) { // �� ���ſ����� ��� �Ұ�
				return;
			}		

			if (weapon.getItem().isTwohandedWeapon()
					&& pcInventory.getGarderEquipped(2, 7, 13) >= 1) { // ��� ������ ���, ����(shield) ����� Ȯ��
				activeChar.sendPackets(new S_ServerMessage(128)); // \f1����(shield)�� ��� �ϰ� ���� ���� ������� ������ ���⸦ ����� �� �����ϴ�.
				return;
			}
		}

		activeChar.cancelAbsoluteBarrier(); // �ƺ�Ҹ�Ʈ�ٸ����� ����

		if (activeChar.getWeapon() != null) { // �̹� �����ΰ��� ��� �ϰ� �ִ� ���, ���� ��� ����
			if (activeChar.getWeapon().getItem().getBless() == 2) { // ���������� �־��� ���
				activeChar.sendPackets(new S_ServerMessage(150)); // \f1 �� ���� �����ϴ�. ���ָ� ��ĥ �� �ְ� �ִ� �� �����ϴ�.
				return;
			}
			if (activeChar.getWeapon().equals(weapon)) {
				// ��� ��ȯ�� �ƴϰ� ������ ��
				pcInventory.setEquipped(activeChar.getWeapon(), false, false,
						false);
				return;
			} else {
				pcInventory.setEquipped(activeChar.getWeapon(), false, false,
						true);
			}
		}

		if (weapon.getItemId() == 200002) { // �������� ���̽��ٰ�
			activeChar.sendPackets(new S_ServerMessage(149, weapon
					.getLogName())); // \f1%0�� �տ� �鷯�پ����ϴ�.
		}
		pcInventory.setEquipped(weapon, true, false, false);
	}
}

