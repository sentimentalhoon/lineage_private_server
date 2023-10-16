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
package l1j.server.server.model.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import l1j.server.Config;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.TownTable;
import l1j.server.server.model.L1BugBearRace;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1TaxCalculator;
import l1j.server.server.model.L1TownLocation;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Castle;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1ShopItem;
import l1j.server.server.utils.IntRange;

public class L1Shop {
	private final int _npcId;
	private final List<L1ShopItem> _sellingItems;
	private final List<L1ShopItem> _purchasingItems;

	public L1Shop(int npcId, List<L1ShopItem> sellingItems,	List<L1ShopItem> purchasingItems) {
		if (sellingItems == null || purchasingItems == null) { throw new NullPointerException();}		
		_npcId = npcId;
		_sellingItems = sellingItems;
		_purchasingItems = purchasingItems;
	}

	public void sellItems(L1PcInstance pc, L1ShopBuyOrderList orderList) {
		// ���� �ȵ��� NPC
		if (getNpcId() == 70068 || getNpcId() == 70020 || getNpcId() == 70056
				|| getNpcId() == 70051 || getNpcId() == 70055 /*|| getNpcId() == 4213002*/
				|| getNpcId() == 70017 || getNpcId() == 4200105) {
			if (!NoTaxEnsureSell(pc, orderList)){
				return;
			}
			NoTaxSellItems(pc.getInventory(), orderList);			
			return;
		}
		// ���� �ȵ��� NPC (�ζ� ������ ���)
		if (getNpcId() == 11111) { // �ζ� ����
			if (!NoTaxEnsureSell(pc, orderList)){
				return;
			}
			NoTaxSellItemsForLotto (pc.getInventory(), pc, orderList);
			return;
		}
		// �����̵��� ���Ǿ� ���� 
		if(getNpcId() >= 8000000 && getNpcId() <= 8000054){ //�ش� ��ȣ ������ ���� �ν�
			if(!NoTaxEnsureSell(pc, orderList)){
				return;
			}
			NpcShopSellItems(pc.getInventory(), orderList);
			return;
		}
		// ����� ��ȭ ���� (Ʈ��)
		if(getNpcId() == 4208001){
			if (!AGEnsureSell(pc, orderList)) { 
				return; 
			} 
			AGSellItems(pc.getInventory(), orderList);
			return;
		}
		//ųPoint ����
		if(getNpcId() == 450001846 
				|| getNpcId() == 4500175 || getNpcId() == 4500176 || getNpcId() == 4500177 
				|| getNpcId() == 4500178 || getNpcId() == 4500179 || getNpcId() == 4500180){
			if(!ensureSPSell(pc, orderList)){
				return;
			}
			sellSPItems(pc.getInventory(), orderList);
			return;
		}
		// �����̾� ����
		if( getNpcId() == 7000025 || getNpcId() == 7000026 || getNpcId() == 4220000 || getNpcId() == 4220001 || getNpcId() == 4220002 || getNpcId() == 4220003 
				|| getNpcId() == 4220700){
			if (!ensurePremiumSell(pc, orderList)) { 
				return; 
			} 
			sellPremiumItems(pc.getInventory(), orderList);
			return;
		}

		// ���ﹰ�� ����(¡ǥ)
		/*
		if(getNpcId() == 7100013){
			if (!ensureMarkSell(pc, orderList)) { 
				return; 
			} 
			sellMarkItems(pc.getInventory(), orderList);
			return;
		}
		 */
		// �� ��
		if (!ensureSell(pc, orderList)) {
			return;							
		}else{
			sellItems(pc.getInventory(), orderList);
			payTax(orderList);	
		}
	}	
	private void NpcShopSellItems(L1PcInventory inv, L1ShopBuyOrderList orderList) {
		if (!inv.consumeItem(L1ItemId.ADENA, orderList.getTotalPrice())){
			throw new IllegalStateException("���Կ� �ʿ��� �Ƶ����� �Һ� �� �� �����ϴ�.");
		}

		L1ItemInstance item  = null;
		boolean[] isRemoveFromList = new boolean[8];
		for (L1ShopBuyOrder order : orderList.getList()) {
			int orderid = order.getOrderNumber();
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			int enchant = order.getItem().getEnchant();
			int attrenchantLevel = order.getItem().getAttrEnchantLevel();
			int remaindcount = getSellingItems().get(orderid).getCount();

			if(remaindcount < amount)
				return;

			item = ItemTable.getInstance().createItem(itemId);
			if (getSellingItems().contains(item)) {
				return;
			}

			item.setCount(amount);
			item.setIdentified(true);
			item.setEnchantLevel(enchant);
			item.setAttrEnchantLevel(attrenchantLevel);
			if(remaindcount == amount)
				isRemoveFromList[orderid] = true; 
			else
				_sellingItems.get(orderid).setCount(remaindcount - amount);

			inv.storeItem(item);

			for (int i = 7; i >= 0; i--) {
				if (isRemoveFromList[i]) {
					_sellingItems.remove(i);
				}
			}
		}
	}


	private boolean ensureSell(L1PcInstance pc, L1ShopBuyOrderList orderList) {
		int price = orderList.getTotalPriceTaxIncluded();
		if (!IntRange.includes(price, 0, 2000000000)) {
			pc.sendPackets(new S_ServerMessage(904, "2000000000"));
			return false;
		}
		if (!pc.getInventory().checkItem(L1ItemId.ADENA, price)) {			
			pc.sendPackets(new S_ServerMessage(189));
			return false;
		}
		int currentWeight = pc.getInventory().getWeight() * 1000;
		if (currentWeight + orderList.getTotalWeight() > pc.getMaxWeight() * 1000) {
			pc.sendPackets(new S_ServerMessage(82));
			return false;
		}
		int totalCount = pc.getInventory().getSize();
		L1Item temp = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			temp = order.getItem().getItem();
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(temp.getItemId())) {
					totalCount += 1;
				}
			} else {
				totalCount += 1;
			}
		}
		if (totalCount > 180) {
			pc.sendPackets(new S_ServerMessage(263));
			return false;
		}
		if (price <= 0 || price > 2000000000) {
			pc.sendPackets(new S_Disconnect());
			return false;
		}

		return true;
	}

	private void sellItems(L1PcInventory inv, L1ShopBuyOrderList orderList) {
		if (!inv.consumeItem(L1ItemId.ADENA, orderList.getTotalPriceTaxIncluded())) {
			throw new IllegalStateException("���Կ� �ʿ��� �Ƶ����� �Һ� �� �� �����ϴ�.");
		}
		L1ItemInstance item  = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			int enchant = order.getItem().getEnchant();
			int attrenchantLevel = order.getItem().getAttrEnchantLevel();
			item = ItemTable.getInstance().createItem(itemId);
			if (getSellingItems().contains(item)) {
				return;
			}
			item.setCount(amount);
			item.setIdentified(true);	
			item.setEnchantLevel(enchant);
			item.setAttrEnchantLevel(attrenchantLevel);
			//����� �����ϱ� ���� �߰� �κ�
			if (getNpcId() == 70035 || getNpcId() == 70041 || getNpcId() == 70042) {
				int[] ticket = L1BugBearRace.getInstance().getTicketInfo(order.getOrderNumber());
				item.setSecondId(ticket[0]);
				item.setRoundId(ticket[1]);
				item.setTicketId(ticket[2]);
				L1BugBearRace.getInstance().addBetting(order.getOrderNumber(), amount);
			}  
			inv.storeItem(item);

		}
	}

	private boolean NoTaxEnsureSell(L1PcInstance pc, L1ShopBuyOrderList orderList) {
		int price = orderList.getTotalPrice();
		if (!IntRange.includes(price, 0, 2000000000)) {
			pc.sendPackets(new S_ServerMessage(904, "2000000000"));
			return false;
		}
		if (!pc.getInventory().checkItem(L1ItemId.ADENA, price)) {			
			pc.sendPackets(new S_ServerMessage(189));
			return false;
		}
		int currentWeight = pc.getInventory().getWeight() * 1000;
		if (currentWeight + orderList.getTotalWeight() > pc.getMaxWeight() * 1000) {
			pc.sendPackets(new S_ServerMessage(82));
			return false;
		}
		int totalCount = pc.getInventory().getSize();
		L1Item temp = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			temp = order.getItem().getItem();
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(temp.getItemId())) {
					totalCount += 1;
				}
			} else {
				totalCount += 1;
			}
		}
		if (totalCount > 180) {
			pc.sendPackets(new S_ServerMessage(263));
			return false;
		}
		if (price <= 0 || price > 2000000000) {
			pc.sendPackets(new S_Disconnect());
			return false;
		}
		return true;
	}
	/**
	 * ������ ���� NPC
	 * @param inv
	 * @param orderList
	 */
	private void NoTaxSellItems(L1PcInventory inv, L1ShopBuyOrderList orderList) { 
		if (!inv.consumeItem(L1ItemId.ADENA, orderList.getTotalPrice())){
			throw new IllegalStateException("���Կ� �ʿ��� �Ƶ����� �Һ� �� �� �����ϴ�.");
		}
		L1ItemInstance item  = null;
		Random random = new Random(System.nanoTime());
		for (L1ShopBuyOrder order : orderList.getList()) {
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			int enchant = order.getItem().getEnchant();/////////<�߰�
			int attrenchantLevel = order.getItem().getAttrEnchantLevel();/////////<�߰�
			item = ItemTable.getInstance().createItem(itemId);
			if (getSellingItems().contains(item)) {
				return;
			}
			item.setCount(amount);
			if (_npcId == 70068 || _npcId == 70020 || _npcId == 70056) {
				item.setIdentified(false);
				int chance = random.nextInt(150) + 1;
				if (chance <= 15) {
					item.setEnchantLevel(-2);
				} else if (chance >= 16 && chance <= 30) {
					item.setEnchantLevel(-1);
				} else if (chance >= 31 && chance <= 89) {
					item.setEnchantLevel(0);
				} else if (chance >= 90 && chance <= 141) {
					item.setEnchantLevel(random.nextInt(2)+1);
				} else if (chance >= 142 && chance <= 147) {
					item.setEnchantLevel(random.nextInt(3)+3);
				} else if (chance >= 148 && chance <= 149) {
					item.setEnchantLevel(6);
				} else if (chance == 150) {
					item.setEnchantLevel(7);
				}
			} else if (_npcId == 4200105){
				item.setIdentified(true);
				int type = item.getItem().getType2();
				if(type == 1){
					item.setEnchantLevel(6);
				}else if (type == 2){
					item.setEnchantLevel(4);
				}
			} else {
				item.setIdentified(true);
				item.setEnchantLevel(enchant);/////////////<�߰�
				item.setAttrEnchantLevel(attrenchantLevel);
			}
			inv.storeItem(item);
		}
	}
	

	/**
	 * ������ ���� NPC (�ζ� ����)
	 * @param inv
	 * @param orderList
	 */
	private void NoTaxSellItemsForLotto(L1PcInventory inv, L1PcInstance _use, L1ShopBuyOrderList orderList) { 
		if (!inv.consumeItem(L1ItemId.ADENA, orderList.getTotalPrice())){
			throw new IllegalStateException("���Կ� �ʿ��� �Ƶ����� �Һ� �� �� �����ϴ�.");
		}
		L1ItemInstance item  = null;
		Random random = new Random(System.nanoTime());
		for (L1ShopBuyOrder order : orderList.getList()) {
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			int enchant = order.getItem().getEnchant();/////////<�߰�
			int attrenchantLevel = order.getItem().getAttrEnchantLevel();/////////<�߰�
			item = ItemTable.getInstance().createItem(itemId, _use, orderList.getTotalPriceTaxIncluded());
			if (getSellingItems().contains(item)) {
				return;
			}
			item.setCount(amount);
			item.setIdentified(true);

			if (_npcId == 70068 || _npcId == 70020 || _npcId == 70056) {
				item.setIdentified(false);
				int chance = random.nextInt(150) + 1;
				if (chance <= 15) {
					item.setEnchantLevel(-2);
				} else if (chance >= 16 && chance <= 30) {
					item.setEnchantLevel(-1);
				} else if (chance >= 31 && chance <= 89) {
					item.setEnchantLevel(0);
				} else if (chance >= 90 && chance <= 141) {
					item.setEnchantLevel(random.nextInt(2)+1);
				} else if (chance >= 142 && chance <= 147) {
					item.setEnchantLevel(random.nextInt(3)+3);
				} else if (chance >= 148 && chance <= 149) {
					item.setEnchantLevel(6);
				} else if (chance == 150) {
					item.setEnchantLevel(7);
				}
			} else if (_npcId == 4200105){
				int type = item.getItem().getType2();
				if(type == 1){
					item.setEnchantLevel(6);
				}else if (type == 2){
					item.setEnchantLevel(4);
				}
			} else {
				item.setEnchantLevel(enchant);/////////////<�߰�
				item.setAttrEnchantLevel(attrenchantLevel);
			}
			inv.storeItem(item);
		}
	}
	private void sellSPItems(L1PcInventory inv, L1ShopBuyOrderList orderList) {
		if (!inv.consumeItem(5000153, orderList.getTotalPrice())){
			throw new IllegalStateException("���Կ� �ʿ��� ����Ʈ�� �Һ��� �� �����ϴ�.");
		}
		L1ItemInstance item  = null;		
		for (L1ShopBuyOrder order : orderList.getList()) {
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			int enchant = order.getItem().getEnchant();/////////<�߰�
			int attrenchantLevel = order.getItem().getAttrEnchantLevel();/////////<�߰�
			item = ItemTable.getInstance().createItem(itemId);
			if (getSellingItems().contains(item)) {
				return;
			}
			item.setCount(amount);
			item.setEnchantLevel(enchant);/////////////<�߰�
			item.setAttrEnchantLevel(attrenchantLevel);
			item.setIdentified(true);			
			inv.storeItem(item);
		}
	}
	private void AGSellItems(L1PcInventory inv, L1ShopBuyOrderList orderList) {
		if (!inv.consumeItem(49026, orderList.getTotalPrice())){
			throw new IllegalStateException("���Կ� �ʿ��� �Ƶ����� �Һ� �� �� �����ϴ�.");
		}
		L1ItemInstance item  = null;		
		for (L1ShopBuyOrder order : orderList.getList()) {
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			int enchant = order.getItem().getEnchant();/////////<�߰�
			int attrenchantLevel = order.getItem().getAttrEnchantLevel();/////////<�߰�
			item = ItemTable.getInstance().createItem(itemId);
			if (getSellingItems().contains(item)) {
				return;
			}
			item.setCount(amount);
			item.setEnchantLevel(enchant);/////////////<�߰�
			item.setAttrEnchantLevel(attrenchantLevel);
			item.setIdentified(true);			
			inv.storeItem(item);
		}
	}
	
	private boolean ensureSPSell(L1PcInstance pc, L1ShopBuyOrderList orderList) {
		int price = orderList.getTotalPrice();
		if (!IntRange.includes(price, 0, 100000)) {
			pc.sendPackets(new S_ServerMessage(904, "100000"));
			return false;
		}
		if (!pc.getInventory().checkItem(5000153, price)) {			
			pc.sendPackets(new S_SystemMessage("����Ʈ�� �����մϴ�."));
			return false;
		}
		int currentWeight = pc.getInventory().getWeight() * 1000;
		if (currentWeight + orderList.getTotalWeight() > pc.getMaxWeight() * 1000) {
			pc.sendPackets(new S_ServerMessage(82));
			return false;
		}
		int totalCount = pc.getInventory().getSize();
		L1Item temp = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			temp = order.getItem().getItem();
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(temp.getItemId())) {
					totalCount += 1;
				}
			} else {
				totalCount += 1;
			}
		}
		if (totalCount > 180) {
			pc.sendPackets(new S_ServerMessage(263));
			return false;
		}
		if (price <= 0 || price > 200000) {
			pc.sendPackets(new S_Disconnect());
			return false;
		}
		return true;
	}
	private boolean AGEnsureSell(L1PcInstance pc, L1ShopBuyOrderList orderList) {
		int price = orderList.getTotalPrice();
		if (!IntRange.includes(price, 0, 2000000000)) {
			pc.sendPackets(new S_ServerMessage(904, "2000000000"));
			return false;
		}
		if (!pc.getInventory().checkItem(49026, price)) {			
			pc.sendPackets(new S_ServerMessage(189));
			return false;
		}
		int currentWeight = pc.getInventory().getWeight() * 1000;
		if (currentWeight + orderList.getTotalWeight() > pc.getMaxWeight() * 1000) {
			pc.sendPackets(new S_ServerMessage(82));
			return false;
		}
		int totalCount = pc.getInventory().getSize();
		L1Item temp = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			temp = order.getItem().getItem();
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(temp.getItemId())) {
					totalCount += 1;
				}
			} else {
				totalCount += 1;
			}
		}
		if (totalCount > 180) {
			pc.sendPackets(new S_ServerMessage(263));
			return false;
		}
		if (price <= 0 || price > 2000000000) {
			pc.sendPackets(new S_Disconnect());
			return false;
		}
		return true;
	}
	private void sellMarkItems(L1PcInventory inv, L1ShopBuyOrderList orderList) {
		if (!inv.consumeItem(L1ItemId.TEST_MARK, orderList.getTotalPrice())) {
			throw new IllegalStateException("���Կ� �ʿ��� ¡ǥ�� �Һ��� �� �����ϴ�.");
		}
		L1ItemInstance item = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			item = ItemTable.getInstance().createItem(itemId);
			item.setCount(amount);
			item.setIdentified(true);
			inv.storeItem(item);
		}
	}
	private boolean ensureMarkSell(L1PcInstance pc, L1ShopBuyOrderList orderList) {

		int price = orderList.getTotalPrice();

		if (!pc.getInventory().checkItem(L1ItemId.TEST_MARK, price)) {
			pc.sendPackets(new S_SystemMessage("������ ���Ⱑ �����մϴ�."));
			return false;
		}
		int currentWeight = pc.getInventory().getWeight() * 1000;
		if (currentWeight + orderList.getTotalWeight() > pc.getMaxWeight() * 1000) {
			pc.sendPackets(new S_ServerMessage(82));
			return false;
		}
		int totalCount = pc.getInventory().getSize();
		L1Item temp  = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			temp = order.getItem().getItem();
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(temp.getItemId())) {
					totalCount += 1;
				}
			} else {
				totalCount += 1;
			}
		}
		if (totalCount > 180) {
			pc.sendPackets(new S_ServerMessage(263));
			return false;
		}
		if (price <= 0 || price > 2000000000) {
			pc.sendPackets(new S_Disconnect());
			return false;
		}
		return true;
	}


	private boolean AGEnsureSell2(L1PcInstance pc, L1ShopBuyOrderList orderList) {
		int price = orderList.getTotalPrice();
		if (!IntRange.includes(price, 0, 20000)) {
			pc.sendPackets(new S_SystemMessage("��ǥ�� �ѹ��� 20000�� �̻� ����Ҽ� �����ϴ�."));
			return false;
		}
		if (!pc.getInventory().checkItem(3500045, price)) {// ������ �� ������ üũ
			pc.sendPackets(new S_SystemMessage("��ǥ�� �����մϴ�."));
			return false;
		}
		int currentWeight = pc.getInventory().getWeight() * 1000;// �߷� üũ
		if (currentWeight + orderList.getTotalWeight() > pc.getMaxWeight() * 1000) {
			pc.sendPackets(new S_ServerMessage(82));
			return false;
		}
		int totalCount = pc.getInventory().getSize();// ���� üũ
		L1Item temp  = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			temp = order.getItem().getItem();
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(temp.getItemId())) {
					totalCount += 1;
				}
			} else {
				totalCount += 1;
			}
		}
		if (totalCount > 180) {
			//�ѻ���� ĳ���Ͱ� ������ ���� �� �ִ� �������� �ִ� 180�������Դϴ�.
			pc.sendPackets(new S_ServerMessage(263));
			return false;
		}
		if (price <= 0 || price > 2000000000) {
			pc.sendPackets(new S_Disconnect());
			return false;
		}
		return true;
	}

	private void sellPremiumItems(L1PcInventory inv, L1ShopBuyOrderList orderList) {
		if (!inv.consumeItem(41159, orderList.getTotalPrice())) {
			throw new IllegalStateException("���Կ� �ʿ��� �ź��� ������ �Һ��� �� �������ϴ�.");
		}
		L1ItemInstance item = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			int itemId = order.getItem().getItemId();
			int amount = order.getCount();
			int enchant = order.getItem().getEnchant();/////////<�߰�
			int attrenchantLevel = order.getItem().getAttrEnchantLevel();/////////<�߰�
			item = ItemTable.getInstance().createItem(itemId);
			item.setCount(amount);
			item.setEnchantLevel(enchant);/////////////<�߰�
			item.setAttrEnchantLevel(attrenchantLevel);
			item.setIdentified(true);
			inv.storeItem(item);
		}
	}

	private boolean ensurePremiumSell(L1PcInstance pc, L1ShopBuyOrderList orderList) {
		int price = orderList.getTotalPrice();
		if (!IntRange.includes(price, 0, 300000)) {
			pc.sendPackets(new S_SystemMessage("�ź��� ���������� �ѹ��� 300,000�� �̻� ����Ҽ� �����ϴ�."));
			return false;
		}
		if (!pc.getInventory().checkItem(41159, price)) {
			pc.sendPackets(new S_SystemMessage("�ź��� ���������� �����մϴ�."));
			return false;
		}
		int currentWeight = pc.getInventory().getWeight() * 1000;
		if (currentWeight + orderList.getTotalWeight() > pc.getMaxWeight() * 1000) {
			pc.sendPackets(new S_ServerMessage(82));
			return false;
		}
		int totalCount = pc.getInventory().getSize();
		L1Item temp  = null;
		for (L1ShopBuyOrder order : orderList.getList()) {
			temp = order.getItem().getItem();
			if (temp.isStackable()) {
				if (!pc.getInventory().checkItem(temp.getItemId())) {
					totalCount += 1;
				}
			} else {
				totalCount += 1;
			}
		}
		if (totalCount > 180) {
			pc.sendPackets(new S_ServerMessage(263));
			return false;
		}
		if (price <= 0 || price > 2000000000) {
			pc.sendPackets(new S_Disconnect());
			return false;
		}
		return true;
	}

	private void payTax(L1ShopBuyOrderList orderList) {
		payCastleTax(orderList);
		payTownTax(orderList);
		payDiadTax(orderList);
	}

	private void payCastleTax(L1ShopBuyOrderList orderList) {
		L1TaxCalculator calc = orderList.getTaxCalculator();
		int price = orderList.getTotalPrice();
		int castleId = L1CastleLocation.getCastleIdByNpcid(_npcId);
		int castleTax = calc.calcCastleTaxPrice(price);
		int nationalTax = calc.calcNationalTaxPrice(price);

		if (castleId == L1CastleLocation.ADEN_CASTLE_ID || castleId == L1CastleLocation.DIAD_CASTLE_ID) {
			castleTax += nationalTax;
			nationalTax = 0;
		}

		if (castleId != 0 && castleTax > 0) {
			L1Castle castle = CastleTable.getInstance().getCastleTable(castleId);
			synchronized (castle) {
				int money = castle.getPublicReadyMoney();
				if (2000000000 > money) {
					money += castleTax;
					castle.setPublicReadyMoney(money);
					CastleTable.getInstance().updateCastle(castle);
				}
			}
			if (nationalTax > 0) {
				L1Castle aden = CastleTable.getInstance().getCastleTable(L1CastleLocation.ADEN_CASTLE_ID);
				synchronized (aden) {
					int money = aden.getPublicReadyMoney();
					if (2000000000 > money) {
						money += nationalTax;
						aden.setPublicReadyMoney(money);
						CastleTable.getInstance().updateCastle(aden);
					}
				}
			}
		}
	}

	private void payDiadTax(L1ShopBuyOrderList orderList) {
		L1Castle castle = CastleTable.getInstance().getCastleTable(L1CastleLocation.DIAD_CASTLE_ID);
		L1TaxCalculator calc = orderList.getTaxCalculator();
		int price = orderList.getTotalPrice();
		int diadTax = calc.calcDiadTaxPrice(price);

		if (diadTax <= 0) { return; }
		synchronized (castle) {
			int money = castle.getPublicReadyMoney();
			if (2000000000 > money) {
				money = money + diadTax;
				castle.setPublicReadyMoney(money);
				CastleTable.getInstance().updateCastle(castle);
			}
		}
	}

	private void payTownTax(L1ShopBuyOrderList orderList) {
		int price = orderList.getTotalPrice();
		if (!L1World.getInstance().isProcessingContributionTotal()) {
			int town_id = L1TownLocation.getTownIdByNpcid(_npcId);
			if (town_id >= 1 && town_id <= 10) {
				TownTable.getInstance().addSalesMoney(town_id, price);
			}
		}
	}

	public void buyItems(L1ShopSellOrderList orderList) {
		L1PcInventory inv = orderList.getPc().getInventory();
		int totalPrice = 0;
		L1Object object  = null;
		L1ItemInstance item = null;
		for (L1ShopSellOrder order : orderList.getList()) {
			object = inv.getItem(order.getItem().getTargetId());
			item = (L1ItemInstance) object;
			if(item.getItem().getBless() < 128){
				int count = inv.removeItem(order.getItem().getTargetId(), order.getCount());
				totalPrice += order.getItem().getAssessedPrice() * order.getDividend() * count;
			}
		}
		totalPrice = IntRange.ensure(totalPrice, 0, 2000000000);
		if (0 < totalPrice) {
			inv.storeItem(L1ItemId.ADENA, totalPrice);
		}
	}

	private L1ShopItem getPurchasingItem(int itemId) {
		for (L1ShopItem shopItem : _purchasingItems) {
			if (shopItem.getItemId() == itemId) { 
				return shopItem;
			}
		}
		return null;
	}

	private boolean isPurchaseableItem(L1ItemInstance item) {
		if (item == null || item.isEquipped() || item.getEnchantLevel() != 0 || item.getBless() >= 128)
		{ return false; } 
		return true;
	}

	public L1AssessedItem assessItem(L1ItemInstance item) {
		L1ShopItem shopItem = getPurchasingItem(item.getItemId());
		if (shopItem == null) {	return null; }
		return new L1AssessedItem(item.getId(), getAssessedPrice(shopItem));
	}

	private int getAssessedPrice(L1ShopItem item) {
		return (int)(item.getPrice() * Config.RATE_SHOP_PURCHASING_PRICE / item.getPackCount());
	}

	public List<L1AssessedItem> assessItems(L1PcInventory inv) {
		List<L1AssessedItem> result = new ArrayList<L1AssessedItem>();
		for (L1ShopItem item : _purchasingItems) {
			for (L1ItemInstance targetItem : inv.findItemsId(item.getItemId())) {
				if (!isPurchaseableItem(targetItem)) { continue; }
				result.add(new L1AssessedItem(targetItem.getId(), getAssessedPrice(item)));
			}
		}
		return result;
	}

	public List<L1AssessedItem> assessTickets(L1PcInventory inv) {
		List<L1AssessedItem> result = new ArrayList<L1AssessedItem>();
		for (L1ShopItem item : _purchasingItems) {
			for (L1ItemInstance targetItem : inv.findItemsId(item.getItemId())) {
				if (!isPurchaseableItem(targetItem)) {
					continue;
				}
				float dividend = L1BugBearRace.getInstance().getTicketPrice(targetItem);

				result.add(new L1AssessedItem(targetItem.getId(),
						(int) (getAssessedPrice(item) * dividend)));
			}
		}
		return result;
	}

	public int getNpcId() { return _npcId; }
	public List<L1ShopItem> getSellingItems() {	return _sellingItems; }	
	public L1ShopBuyOrderList newBuyOrderList() { return new L1ShopBuyOrderList(this); }
	public L1ShopSellOrderList newSellOrderList(L1PcInstance pc) { return new L1ShopSellOrderList(this, pc); }

}