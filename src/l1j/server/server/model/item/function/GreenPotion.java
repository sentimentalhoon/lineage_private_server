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

import static l1j.server.server.model.skill.L1SkillId.ENTANGLE;
import static l1j.server.server.model.skill.L1SkillId.GREATER_HASTE;
import static l1j.server.server.model.skill.L1SkillId.HASTE;
import static l1j.server.server.model.skill.L1SkillId.MASS_SLOW;
import static l1j.server.server.model.skill.L1SkillId.SLOW;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HASTE;
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class GreenPotion extends L1ItemInstance{
	
	public GreenPotion(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			int itemId = useItem.getItemId();
			useGreenPotion(pc, itemId);
			pc.getInventory().removeItem(useItem, 1);
		}
	}
	
	private void useGreenPotion(L1PcInstance pc, int itemId) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // ���������� ����
			pc.sendPackets(new S_ServerMessage(698)); // \f1���¿� ���� �ƹ��͵� ���� ���� �����ϴ�.
			return;
		}

		// �ƺ�Ҹ�Ʈ�ٸ����� ����
		pc.cancelAbsoluteBarrier();
		
		int time = 0;
		switch(itemId){
		case L1ItemId.POTION_OF_HASTE_SELF:
		case 40030:
			time = 300;
			break;
		case 40018:
		case 41342:
			time = 1800;
			break;
		case 40039:
		case L1ItemId.MYSTERY_QUICK_POTION:
			time = 600;
			break;
		case 40040:
			time = 900;
			break;
		case 41261:
		case 41262:
		case 41268:
		case 41269:
		case 41271:
		case 41272:
		case 41273:
			time = 30;
			break;
		case 41338:
		case 140018:
			time = 2250;
			break;
		case L1ItemId.B_POTION_OF_HASTE_SELF:
			time = 350;
			break;
		case 550000:// ���� �ӵ�
			time = 1200;
			break;
		}

		pc.sendPackets(new S_SkillSound(pc.getId(), 191));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 191));
		// XXX:���̽�Ʈ������ ����, ���� ���°� �����Ǵ��� �Ҹ�
		if (pc.getHasteItemEquipped() > 0) {
			return;
		}
		// ���� ���¸� ����
		pc.setDrink(false);

		// ���� �ľ�, �׷����� ���� �ľ����� �ߺ� ���� �ʴ´�
		if (pc.getSkillEffectTimerSet().hasSkillEffect(HASTE)) {
			pc.getSkillEffectTimerSet().killSkillEffectTimer(HASTE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
			pc.getMoveState().setMoveSpeed(0);
		} else if (pc.getSkillEffectTimerSet().hasSkillEffect(GREATER_HASTE)) {
			pc.getSkillEffectTimerSet().killSkillEffectTimer(GREATER_HASTE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
			pc.getMoveState().setMoveSpeed(0);
		} else if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_HASTE)) {
			pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_HASTE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
			pc.getMoveState().setMoveSpeed(0);
		}

		// ���ο�, �Ž� ���ο�, ���������� ���ο� ���¸� ������ ��
		if (pc.getSkillEffectTimerSet().hasSkillEffect(SLOW)) { // ���ο�
			pc.getSkillEffectTimerSet().killSkillEffectTimer(SLOW);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
		} else if (pc.getSkillEffectTimerSet().hasSkillEffect(MASS_SLOW)) { // �Ž� ���ο�
			pc.getSkillEffectTimerSet().killSkillEffectTimer(MASS_SLOW);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
		} else if (pc.getSkillEffectTimerSet().hasSkillEffect(ENTANGLE)) { // ������
			pc.getSkillEffectTimerSet().killSkillEffectTimer(ENTANGLE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
		} else {
			pc.sendPackets(new S_SkillHaste(pc.getId(), 1, time));
			Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 1, 0));
			pc.getMoveState().setMoveSpeed(1);
			pc.getSkillEffectTimerSet().setSkillEffect(STATUS_HASTE, time * 1000);
		}
	}
}

