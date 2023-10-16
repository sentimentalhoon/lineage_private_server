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

import static l1j.server.server.model.skill.L1SkillId.HOLY_WALK;
import static l1j.server.server.model.skill.L1SkillId.MOVING_ACCELERATION;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_ELFBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_FRUIT;
import static l1j.server.server.model.skill.L1SkillId.WIND_WALK;
import static l1j.server.server.model.skill.L1SkillId.BLOOD_LUST;
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class BravePotion extends L1ItemInstance{

	public BravePotion(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){			
			L1PcInstance pc = (L1PcInstance)cha;						
			if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // ���������� ����
				pc.sendPackets(new S_ServerMessage(698));// \f1���¿� ���� �ƹ��͵� ���� ���� �����ϴ�.
				return;
			}
			pc.cancelAbsoluteBarrier();


			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			int itemId = this.getItemId();

			if ((itemId == L1ItemId.POTION_OF_EMOTION_BRAVERY // ġ��ħ �̺� �Ϻ�
					|| itemId == L1ItemId.B_POTION_OF_EMOTION_BRAVERY  // �ູ�� ġ��ħ �̺�
					|| itemId == 41415 || itemId == 550001)&& pc.isKnight()) {
				useBravePotion(pc, itemId);
			} else if ((itemId == 40068 || itemId == 140068 || itemId == 550002) && pc.isElf()) {				
				useBravePotion(pc, itemId);
			} else if (itemId == 40031 && pc.isCrown()) { // �Ǹ�����				
				useBravePotion(pc, itemId);		
			} else if (itemId == L1ItemId.UGDRA_FRUIT && pc.isIllusionist()) { // ���׵�󿭸�				
				useFruit(pc, itemId);				
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
				return;	
			}			
			pc.getInventory().removeItem(useItem, 1);
		}
	}

	private void useFruit(L1PcInstance pc, int item_id) {
		int time = 0;
		time = 480;

		if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_BRAVE)){
			pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_BRAVE);
			pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));          
			pc.getMoveState().setBraveSpeed(0);
		}

		pc.sendPackets(new S_SkillSound(pc.getId(), 7110));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 7110));
		pc.getSkillEffectTimerSet().setSkillEffect(STATUS_FRUIT, time * 1000);
	}

	private void useBravePotion(L1PcInstance pc, int item_id) {
		int time = 0;

		switch(item_id){
		case L1ItemId.POTION_OF_EMOTION_BRAVERY:
			time = 300;
			break;
		case 40031:
			time = 600;
			break;
		case 40068:
			time = 600;
			if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_BRAVE)) { // ���ʹ� �ߺ� ���� �ʴ´�.
				pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_BRAVE);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			if (pc.getSkillEffectTimerSet().hasSkillEffect(WIND_WALK)) { // �����ũ�ʹ� �ߺ� ���� �ʴ´�
				pc.getSkillEffectTimerSet().killSkillEffectTimer(WIND_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			break;
		case 550002: //���� ����
			time = 1920;
			if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_BRAVE)) { // ���ʹ� �ߺ� ���� �ʴ´�.
				pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_BRAVE);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			if (pc.getSkillEffectTimerSet().hasSkillEffect(WIND_WALK)) { // �����ũ�ʹ� �ߺ� ���� �ʴ´�
				pc.getSkillEffectTimerSet().killSkillEffectTimer(WIND_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			break;
		case 40733:
			time = 600;
			if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_ELFBRAVE)) {
				pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_ELFBRAVE);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			if (pc.getSkillEffectTimerSet().hasSkillEffect(HOLY_WALK)) { // ȣ-�� ��ũ�ʹ� �ߺ� ���� �ʴ´�
				pc.getSkillEffectTimerSet().killSkillEffectTimer(HOLY_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			if (pc.getSkillEffectTimerSet().hasSkillEffect(MOVING_ACCELERATION)) { // ���� �� �����̼ǰ��� �ߺ� ���� �ʴ´�
				pc.getSkillEffectTimerSet().killSkillEffectTimer(MOVING_ACCELERATION);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			if (pc.getSkillEffectTimerSet().hasSkillEffect(WIND_WALK)) { // �����ũ�ʹ� �ߺ� ���� �ʴ´�
				pc.getSkillEffectTimerSet().killSkillEffectTimer(WIND_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_FRUIT)) { // ���׵�󿭸ſʹ� �ߺ��ȵ�
				pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_FRUIT);
				//pc.sendPackets(new S_SkillFruit(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			break;
		case 41415:
			time = 1800;
			break;
		case 550001: // ���� ���
			time = 1200;
			break;
		case L1ItemId.B_POTION_OF_EMOTION_BRAVERY:
			time = 350;
			break;
		case 140068:
			time = 700;
			if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_BRAVE)) { // ��� ȿ���ʹ� �ߺ� ���� �ʴ´�.
				pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_BRAVE);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			if (pc.getSkillEffectTimerSet().hasSkillEffect(WIND_WALK)) { // �����ũ�ʹ� �ߺ� ���� �ʴ´�
				pc.getSkillEffectTimerSet().killSkillEffectTimer(WIND_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 0, 0));
				pc.getMoveState().setBraveSpeed(0);
			}
			break;
		}

		if (item_id == 40068 || item_id == 140068 || item_id ==550002) { // ���� ����
			pc.sendPackets(new S_SkillBrave(pc.getId(), 3, time));
			Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 3, 0));
			pc.getSkillEffectTimerSet().setSkillEffect(STATUS_ELFBRAVE, time * 1000);
		} else {
			pc.sendPackets(new S_SkillBrave(pc.getId(), 1, time));
			Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 1, 0));
			pc.getSkillEffectTimerSet().setSkillEffect(STATUS_BRAVE, time * 1000);
		}
		pc.sendPackets(new S_SkillSound(pc.getId(), 751));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 751));
		pc.getMoveState().setBraveSpeed(1);
	}
}

