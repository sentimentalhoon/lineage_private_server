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

package l1j.server.server.clientpackets;

import java.util.Random;

import server.LineageClient;

import l1j.server.Config;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1PetType;

public class C_GiveItem extends ClientBasePacket {
	private static final String C_GIVE_ITEM = "[C] C_GiveItem";

	private static Random _random = new Random(System.nanoTime());

	public C_GiveItem(byte decrypt[], LineageClient client) {
		super(decrypt);
		int targetId = readD();
		@SuppressWarnings("unused")
		int x = readH();
		@SuppressWarnings("unused")
		int y = readH();
		int itemId = readD();
		int count = readD();
		
		L1PcInstance pc = client.getActiveChar();
		L1Object object = L1World.getInstance().findObject(targetId);
		L1NpcInstance target = (L1NpcInstance) object;
		L1Inventory targetInv = target.getInventory();
		L1Inventory inv = pc.getInventory();
		L1ItemInstance item = inv.getItem(itemId);
		
		if (pc.isGhost()) {
			return;
		}
		if (object == null || item == null) {
			return;
		}
		if (!isNpcItemReceivable(target.getNpcTemplate())) {		
			if (!(item.getItem().getItemId() == 40499) || !(item.getItem().getItemId() == 40507)){		
				return;
			}
		}
		if (item.isEquipped()) {
			pc.sendPackets(new S_ServerMessage(141)); // \f1��� �ϰ� �ִ� ����, ������� �ǳ��� ���� �����ϴ�.
			return;
		}
		if (item.getBless() >= 128) {// ����
			pc.sendPackets(new S_ServerMessage(141));
			return;
		}		
		if (itemId != item.getId()) {
			pc.sendPackets(new S_Disconnect());
			return;
		}
		if (!item.isStackable() && count != 1) {
			pc.sendPackets(new S_Disconnect());
			return;
		}
		if (item.getCount() <= 0 || count <= 0) {
			pc.sendPackets(new S_Disconnect());
			return;
		}
		if (count >= item.getCount()) {
			count = item.getCount();
		}
		
		if (!item.getItem().isTradable()) {
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); // \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
			return;
		}
		L1PetInstance pet = null;
		for (Object petObject : pc.getPetList().values()) {
			if (petObject instanceof L1PetInstance) {
				pet = (L1PetInstance) petObject;
				if (item.getId() == pet.getItemObjId()) {
					// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
					pc.sendPackets(new S_ServerMessage(210, item.getItem()
							.getName()));
					return;
				}
			}
		}
		L1DollInstance doll = null;
		for (Object ���� : pc.getPetList().values()) {
			if (���� instanceof L1DollInstance) {
				doll = (L1DollInstance) ����;
				if (item.getId() == doll.getItemObjId()) {
					// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
					pc.sendPackets(new S_ServerMessage(210, item.getItem()
							.getName()));
					return;
				}
			}
		}
		if (targetInv.checkAddItem(item, count) != L1Inventory.OK) {
			pc.sendPackets(new S_ServerMessage(942)); // ����� �������� �ʹ� ���̱� (����)������, �� �̻� �� �� �����ϴ�.
			return;
		}
		
		L1PetType petType = PetTypeTable.getInstance().get(target.getNpcTemplate().get_npcId());
		if (petType != null){
			if((petType.getBaseNpcId() == 46046 || petType.getItemIdForTaming() != 0) && item.getItem().isUseHighPet()) {			
				return;
			}
		}

		item = inv.tradeItem(item, count, targetInv);
		target.onGetItem(item);
		target.getLight().turnOnOffLight();
		pc.getLight().turnOnOffLight();
	
		if (petType == null || target.isDead()) {
			return;
		}

		if (item.getItemId() == petType.getItemIdForTaming()) {
			tamePet(pc, target);
		}

		if (item.getItemId() == 40070 && petType.canEvolve() // ��ȭ�ǿ���(���)
				&& petType.getItemIdForTaming() == 40060) {
			evolvePet(pc, target);
		}

		if (item.getItemId() == 40070 && petType.canEvolve() // ��ȭ�ǿ���(�����)
				&& petType.getItemIdForTaming() == 40057) {
			evolvePet(pc, target);
		}

		if (item.getItemId() == 41310 && petType.canEvolve() // �¸��� ����
				&& petType.getItemIdForTaming() == 0) {
			evolvePet(pc, target);
		}

		if ((item.getItem().getMaterial() == 4 || item.getItemId() == 40060)
				&& item.getItem().getType() == 7) {// ������ ���ķ�
			petfoodgive(pc, target, item);
		}

		L1PetInstance pets = (L1PetInstance) L1World.getInstance().findObject(targetId);
		if (item.getItem().isUseHighPet() && petType.getItemIdForTaming() == 0) {
			if (item.getItemId() >= 427100 && item.getItemId() <= 427109) {
				pets.usePetWeapon(item);
			} else if (item.getItemId() >= 427000 && item.getItemId() <= 427007) {
				pets.usePetArmor(item);
			}
		}
	}

	private final static String receivableImpls[] = new String[] { "L1Npc", // NPC
		"L1Monster", // monster
		"L1Guardian", // ���� ���� ��ȣ��
		"L1Teleporter", // �ڷ� ����
		"L1Guard" }; // ���̵�

	private boolean isNpcItemReceivable(L1Npc npc) {
		for (String impl : receivableImpls) {
			if (npc.getImpl().equals(impl)) {
				return true;
			}
		}
		return false;
	}

	private void tamePet(L1PcInstance pc, L1NpcInstance target) {
		if (target instanceof L1PetInstance
				|| target instanceof L1SummonInstance) {
			return;
		}

		int petcost = 0;
		Object[] petlist = pc.getPetList().values().toArray();
		for (Object pet : petlist) {
			petcost += ((L1NpcInstance) pet).getPetcost();
		}
		int charisma = pc.getAbility().getTotalCha();
		if (pc.isCrown()) { // ����
			charisma += 6;
		} else if (pc.isElf()) { // ����
			charisma += 12;
		} else if (pc.isWizard()) { // ������
			charisma += 6;
		} else if (pc.isDarkelf()) { // ��ũ����
			charisma += 6;
		} else if (pc.isDragonknight()) { // ����
			charisma += 6;
		} else if (pc.isIllusionist()) { // ȯ����
			charisma += 6;
		}
		charisma -= petcost;

		L1PcInventory inv = pc.getInventory();
		if (charisma >= 6 && inv.getSize() < 180) {
			if (isTamePet(pc, target)) {
				L1ItemInstance petamu = inv.storeItem(40314, 1); // ���� �ƹ·�Ʈ
				if (petamu != null) {
					new L1PetInstance(target, pc, petamu.getId());
					pc.sendPackets(new S_ItemName(petamu));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(324)); // ����̴µ� �����߽��ϴ�.
			}
		}
	}

	private void evolvePet(L1PcInstance pc, L1NpcInstance target) {
		if (!(target instanceof L1PetInstance)) {
			return;
		}
		L1PcInventory inv = pc.getInventory();
		L1PetInstance pet = (L1PetInstance) target;
		L1ItemInstance petamu = inv.getItem(pet.getItemObjId());
		if (pet.getLevel() >= 30 && // Lv30 �̻�
				pc == pet.getMaster() && // �ڽ��� �ֿϵ���
				petamu != null) {
			L1ItemInstance highpetamu = inv.storeItem(40316, 1);
			if (highpetamu != null) {
				pet.evolvePet(highpetamu.getId()); // ��ȭ��Ų��
				pc.sendPackets(new S_ItemName(highpetamu));
				inv.removeItem(petamu, 1);
			}
		}
	}

	private boolean isTamePet(L1PcInstance pc, L1NpcInstance npc) {
		boolean isSuccess = false;
		int npcId = npc.getNpcTemplate().get_npcId();
		if (npcId == 45313 || npcId == 45711) { // ȣ����, �Ʊ�������
			if (npc.getMaxHp() / 2 > npc.getCurrentHp() // HP��1/4�̸�����1/16�� Ȯ��
					&& _random.nextInt(16) + _random.nextInt(pc.getAbility().getTotalCha()) >= 20) {
				isSuccess = true;
			}
		} else {
			if (npc.getMaxHp() / 4 > npc.getCurrentHp() && _random.nextInt(3) == 2) {
				isSuccess = true;
			}
		}

		if (npcId == 45313 || npcId == 45044 || npcId == 45711) { // ȣ����, ����, �Ʊ� ������
			if (npc.isResurrect()) { // ��Ȱ �Ĵ� ����̱� �Ұ�
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	private void petfoodgive(L1PcInstance pc, L1NpcInstance target, L1ItemInstance item) {
		if (!(target instanceof L1PetInstance)) {
			return;
		}
		L1PetInstance pet = (L1PetInstance)target;
		L1Inventory inv = target.getInventory();

		if ((target.getNpcId() == 46042 || target.getNpcId() == 46043)
				&& item.getItemId() == 41423) {// Ļ�ŷ� ����
			pet.setFood(0);
			target.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_PET_FOOD, pet.getFoodTime()*1000);
		} else if ((target.getNpcId() == 46044 || target.getNpcId() == 46045)
				&& item.getItemId() == 41424) {// �Ǵٰ� ����
			inv.removeItem(item, 1);
			pet.setFood(0);
			target.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_PET_FOOD, pet.getFoodTime()*1000);
		} else if ((target.getNpcId() == 45049 || target.getNpcId() == 45695)
				&& item.getItemId() == 40060) {
			inv.removeItem(item, 1);
			pet.setFood(0);
			target.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_PET_FOOD, pet.getFoodTime()*1000);
		} else {
			inv.removeItem(item, 1);
			pet.setFood(0);
			target.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_PET_FOOD, pet.getFoodTime()*1000);
		}
	}


	@Override
	public String getType() {
		return C_GIVE_ITEM;
	}
}
