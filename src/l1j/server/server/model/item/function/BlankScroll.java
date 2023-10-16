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
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1Skills;

@SuppressWarnings("serial")
public class BlankScroll extends L1ItemInstance{
	
	public BlankScroll(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			int blanksc_skillid = 0;
			blanksc_skillid = packet.readC();
			int itemId = useItem.getItemId();
			if (pc.isWizard()) { // ������
				if (itemId == 40090 && blanksc_skillid <= 7 || // ����
						// ��ũ��(Lv1)�� ���� 1 ������ ����
						itemId == 40091 && blanksc_skillid <= 15 || // ����
						// ��ũ��(Lv2)�� ���� 2 ������ ����
						itemId == 40092 && blanksc_skillid <= 22 || // ����
						// ��ũ��(Lv3)�� ���� 3 ������ ����
						itemId == 40093 && blanksc_skillid <= 31 || // ����
						// ��ũ��(Lv4)�� ���� 4 ������ ����
						itemId == 40094 && blanksc_skillid <= 39) { // ����
					// ��ũ��(Lv5)�� ���� 5 ������ ����
					L1ItemInstance spellsc = ItemTable.getInstance().createItem(40859 + blanksc_skillid);
					if (spellsc != null) {
						if (pc.getInventory().checkAddItem(spellsc, 1) == L1Inventory.OK) {
							// blanksc_skillid�� 0 ����
							L1Skills l1skills = SkillsTable.getInstance().getTemplate(blanksc_skillid + 1); 
							if (pc.getCurrentHp() + 1 < l1skills.getHpConsume() + 1) {
								pc.sendPackets(new S_ServerMessage(279)); 
								// \f1HP�� ������ ������ ����� �� ���� �ʽ��ϴ�.
								return;
							}
							if (pc.getCurrentMp() < l1skills.getMpConsume()) {
								pc.sendPackets(new S_ServerMessage(278)); 
								// \f1MP�� ������ ������ ����� �� ���� �ʽ��ϴ�.
								return;
							}
							if (l1skills.getItemConsumeId() != 0) { // ��ᰡ �ʿ�
								if (!pc.getInventory().checkItem(l1skills.getItemConsumeId(), l1skills.getItemConsumeCount())) {
									pc.sendPackets(new S_ServerMessage(299)); 
									// \f1������ ��â�ϱ� ���� ��ᰡ ������� �ʽ��ϴ�.
									return;
								}
							}
							pc.setCurrentHp(pc.getCurrentHp() - l1skills.getHpConsume());
							pc.setCurrentMp(pc.getCurrentMp() - l1skills.getMpConsume());
							int lawful = pc.getLawful() + l1skills.getLawful();
							if (lawful > 32767) { lawful = 32767; }
							if (lawful < -32767) { lawful = -32767; }
							pc.setLawful(lawful);
							if (l1skills.getItemConsumeId() != 0) { // ��ᰡ �ʿ�
								pc.getInventory().consumeItem(l1skills.getItemConsumeId(), l1skills.getItemConsumeCount());
							}
							pc.getInventory().removeItem(useItem, 1);
							pc.getInventory().storeItem(spellsc);
						}
					}
				} else {
					pc.sendPackets(new S_ServerMessage(591)); // \f1��ũ���� �׷��� ���� ������ ����Ϸ���  �ʹ��� ���մϴ�.
				}
			} else {
				pc.sendPackets(new S_ServerMessage(264)); // \f1����� Ŭ���������� �� �������� ����� �� �����ϴ�.
			}
		}
	}
}

