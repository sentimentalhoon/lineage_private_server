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

import static l1j.server.server.model.skill.L1SkillId.COOKING_NOW;

import java.util.Random;

import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1EffectInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class MakeCooking extends L1ItemInstance{

	private static Random _random = new Random(System.nanoTime());

	public MakeCooking(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){		
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			int itemId = this.getItemId();
			if (packet.readC() == 0) {						
				pc.sendPackets(new S_PacketBox(S_PacketBox.COOK_WINDOW, (itemId - 41255)));
			} else {
				makeCooking(pc, packet.readC());
			}
		}
	}


	private void makeCooking(L1PcInstance pc, int cookNo) {
		boolean isNearFire =  false;
		L1EffectInstance effect = null;
		for (L1Object obj : L1World.getInstance().getVisibleObjects(pc, 3)) {
			if (obj instanceof L1EffectInstance) {
				effect = (L1EffectInstance) obj;
				if (effect.getGfxId().getGfxId() == 5943) {
					isNearFire = true;
					break;
				}
			}
		}
		if (!isNearFire) {
			pc.sendPackets(new S_ServerMessage(1101)); // �丮���� ��ں��� �ʿ��մϴ�.
			return;
		}
		if (pc.getMaxWeight() <= pc.getInventory().getWeight()) {
			pc.sendPackets(new S_ServerMessage(1103)); // �������� �ʹ� ���ſ�, �丮�� �� �����ϴ�.
			return;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(COOKING_NOW)) {
			return;
		}
		pc.getSkillEffectTimerSet().setSkillEffect(COOKING_NOW, 3 * 1000);		
		int chance = _random.nextInt(100) + 1;
		switch(cookNo){
		case 0: // ������ ������ũ
			if (pc.getInventory().checkItem(40057, 1)) {
				pc.getInventory().consumeItem(40057, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41277, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41285, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 1: // ����� ����
			if (pc.getInventory().checkItem(41275, 1)) {
				pc.getInventory().consumeItem(41275, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41278, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41286, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 2: // ��ȣ��
			if (pc.getInventory().checkItem(41263, 1)
					&& pc.getInventory().checkItem(41265, 1)) {
				pc.getInventory().consumeItem(41263, 1);
				pc.getInventory().consumeItem(41265, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41279, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41287, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 3: // ���̴ٸ� ġ���
			if (pc.getInventory().checkItem(41274, 1)
					&& pc.getInventory().checkItem(41267, 1)) {
				pc.getInventory().consumeItem(41274, 1);
				pc.getInventory().consumeItem(41267, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41280, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41288, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 4: // ���ϻ�����
			if (pc.getInventory().checkItem(40062, 1)
					&& pc.getInventory().checkItem(40069, 1)
					&& pc.getInventory().checkItem(40064, 1)) {
				pc.getInventory().consumeItem(40062, 1);
				pc.getInventory().consumeItem(40069, 1);
				pc.getInventory().consumeItem(40064, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41281, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41289, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 5: // ���� ������
			if (pc.getInventory().checkItem(40056, 1)
					&& pc.getInventory().checkItem(40060, 1)
					&& pc.getInventory().checkItem(40061, 1)) {
				pc.getInventory().consumeItem(40056, 1);
				pc.getInventory().consumeItem(40060, 1);
				pc.getInventory().consumeItem(40061, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41282, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41290, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 6: // ����� ��ġ ����
			if (pc.getInventory().checkItem(41276, 1)) {
				pc.getInventory().consumeItem(41276, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41283, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41291, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 7: // ���� ����
			if (pc.getInventory().checkItem(40499, 1)
					&& pc.getInventory().checkItem(40060, 1)) {
				pc.getInventory().consumeItem(40499, 1);
				pc.getInventory().consumeItem(40060, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41284, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41292, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 8: // ĳ��� ī����
			if (pc.getInventory().checkItem(49040, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49040, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49049, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49057, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 9: // �Ǿ� ������ũ
			if (pc.getInventory().checkItem(49041, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49041, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49050, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49058, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 10: // ��Ʋ�巡�� ����
			if (pc.getInventory().checkItem(49042, 1)
					&& pc.getInventory().checkItem(41265, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49042, 1);
				pc.getInventory().consumeItem(41265, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49051, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49059, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 11: // Ű�� �з� ����
			if (pc.getInventory().checkItem(49043, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49043, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49052, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49060, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 12: // �����ǿ� ����
			if (pc.getInventory().checkItem(49044, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49044, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49053, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49061, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 13: // �Ϸ�ī�� ��Ʃ
			if (pc.getInventory().checkItem(49045, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49045, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49054, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49062, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 14: // �Ź̴ٸ� ��ġ ����
			if (pc.getInventory().checkItem(49046, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49046, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49055, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49063, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 15: // ũ���� ����
			if (pc.getInventory().checkItem(49047, 1)
					&& pc.getInventory().checkItem(40499, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49047, 1);
				pc.getInventory().consumeItem(40499, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49056, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49064, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 16: // ũ����Ʈ�þ� ���Թ� ����
			if(pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)					
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_CRUSTCEA_CLAW, 1)) {							
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_CRUSTCEA_CLAW, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_CRUSTCEA_CLAW_CHARCOAL, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_CRUSTCEA_CLAW_CHARCOAL, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 17: // �׸��� ����
			if(pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_GRIFFON_FOOD, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_GRIFFON_FOOD, 1);				
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_GRIFFON_CHARCOAL, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_GRIFFON_CHARCOAL, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 18: // ��īƮ���� ������ũ
			if(pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_COCKATRICE_TAIL, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_COCKATRICE_TAIL, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_COCKATRICE_STEAK, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_COCKATRICE_STEAK, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 19: // ��հź� ����
			if(pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_TURTLEKING_FLESH, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_TURTLEKING_FLESH, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_TURTLEKING_CHARCOAL, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_TURTLEKING_CHARCOAL, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 20: // ���� �巡�� ������ġ
			if(pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_LESSERDRAGON_WING, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_LESSERDRAGON_WING, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_LESSERDRAGON_WING_SKEWER, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_LESSERDRAGON_WING_SKEWER, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 21: // �巹��ũ ����
			if(pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_DRAKE_FOOD, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_DRAKE_FOOD, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_DRAKE_CHARCOAL, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_DRAKE_CHARCOAL, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 22: // ���ؾ� ��Ʃ
			if(pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_DEEP_SEA_FISH_FLESH, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_DEEP_SEA_FISH_FLESH, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_DEEP_SEA_FISH_STEW, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_DEEP_SEA_FISH_STEW, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		case 23: // �ٽǸ���ũ �� ����
			if(pc.getInventory().checkItem(40499, 1) 
					&& pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOK_HUB, 1)
					&& pc.getInventory().checkItem(L1ItemId.COOKSTUFF_BASILISK_EGG, 1)) {
				pc.getInventory().consumeItem(40499, 1);
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(L1ItemId.COOK_HUB, 1);
				pc.getInventory().consumeItem(L1ItemId.COOKSTUFF_BASILISK_EGG, 1);				
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, L1ItemId.COOKFOOD_BASILIST_EGG_SOUP, 1);
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, L1ItemId.SCOOKFOOD_BASILIST_EGG_SOUP, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // �丮�� �����߽��ϴ�.
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // �丮�� ��ᰡ ������� �ʽ��ϴ�.
			}
			break;
		default:
			break;
		}
	}

	private boolean createNewItem(L1PcInstance pc, int item_id, int count) {
		L1ItemInstance item = ItemTable.getInstance().createItem(item_id);
		item.setCount(count);
		if (item != null) {
			if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
				pc.getInventory().storeItem(item);
			} else { // ���� ��  ���� ���� ���鿡 ����߸��� ó���� ĵ���� ���� �ʴ´�(���� ����)
				L1World.getInstance().getInventory(pc.getX(), pc.getY(),
						pc.getMapId()).storeItem(item);
			}
			pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0�� �տ� �־����ϴ�.
			return true;
		} else {
			return false;
		}
	}
}

