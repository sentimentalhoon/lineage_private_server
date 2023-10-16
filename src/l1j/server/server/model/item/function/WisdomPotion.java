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

import static l1j.server.server.model.skill.L1SkillId.STATUS_WISDOM_POTION;
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconWisdomPotion;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class WisdomPotion extends L1ItemInstance{
	
	public WisdomPotion(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			int itemId = this.getItemId();
			if (pc.isWizard()) {
				useWisdomPotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			}
			pc.getInventory().removeItem(useItem, 1);
		}
	}
	
	private void useWisdomPotion(L1PcInstance pc, int item_id) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // ���������� ����
			pc.sendPackets(new S_ServerMessage(698)); // \f1���¿� ���� �ƹ��͵� ���� ���� �����ϴ�.
			return;
		}

		// �ƺ�Ҹ�Ʈ�ٸ����� ����
		pc.cancelAbsoluteBarrier();
	

		int time = 0; // �ð��� 4�� ����� �ϴ� ��
		if (item_id == L1ItemId.POTION_OF_EMOTION_WISDOM) { // ���� �� �Ϻ�
			time = 300;
		} else if (item_id == L1ItemId.B_POTION_OF_EMOTION_WISDOM) { // �ູ�� ���� ��
			// �Ϻ�
			time = 360;
		} else if (item_id == 550003) { // ���� ����
			time = 1000;
		}

		if (!pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_WISDOM_POTION)) {
			pc.getAbility().addSp(2);
		}

		pc.sendPackets(new S_SkillIconWisdomPotion((int) (time / 4)));
		pc.sendPackets(new S_SkillSound(pc.getId(), 750));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 750));

		pc.getSkillEffectTimerSet().setSkillEffect(STATUS_WISDOM_POTION, time * 1000);

	}
}

