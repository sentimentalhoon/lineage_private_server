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
package l1j.server.server.serverpackets;

import java.io.IOException;
import java.util.List;

import l1j.server.Config;
import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.L1BugBearRace;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1TaxCalculator;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.shop.L1Shop;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1ShopItem;

public class S_ShopSellList extends ServerBasePacket {


	/**
	 * 가게의 물건 리스트를 표시한다. 캐릭터가 BUY 버튼을 눌렀을 때에 보낸다.
	 */
	public S_ShopSellList(int objId) {
		writeC(Opcodes.S_OPCODE_SHOWSHOPBUYLIST);
		writeD(objId);

		L1Object npcObj = L1World.getInstance().findObject(objId);
		if (!(npcObj instanceof L1NpcInstance)) {
			writeH(0);
			return;
		}
		int npcId = ((L1NpcInstance) npcObj).getNpcTemplate().get_npcId();

		L1TaxCalculator calc = new L1TaxCalculator(npcId);
		L1Shop shop = ShopTable.getInstance().get(npcId);
		List<L1ShopItem> shopItems = shop.getSellingItems();

		writeH(shopItems.size());
		
		// L1ItemInstance의 getStatusBytes를 이용하기 위해(때문에)
		L1ItemInstance dummy = new L1ItemInstance();
		L1ShopItem shopItem = null;
		L1Item item = null;
		L1Item template = null;
		for (int i = 0; i < shopItems.size(); i++) {
			shopItem = shopItems.get(i);
			item = shopItem.getItem();

			int price = calc.layTax((int) (shopItem.getPrice() * Config.RATE_SHOP_SELLING_PRICE));
			writeD(i);
			writeH(shopItem.getItem().getGfxId());
			writeD(price);
			
			/** 포비: 상점 아이템에 축, 저주 표시하기 S*/
			String PackCount = "";
			String Enchant = "";
			String MaxUseTime = "";
			String PobyBless = "";
			String attrenchantLevel = "";
			if (npcId == 70035 || npcId == 70041 || npcId == 70042) {
				writeS(L1BugBearRace.getInstance().getTicketName(i));
			} else {
				if (shopItem.getItem().getBless() != 1) { // 포비: 축, 저주 표시
					if (shopItem.getItem().getBless() == 0) PobyBless = "$227 ";
					else if (shopItem.getItem().getBless() == 2) PobyBless = "$228 ";
				}
				if (shopItem.getEnchant() > 0) {
					Enchant = "+"+shopItem.getEnchant()+" ";
				}
				if (shopItem.getAttrEnchantLevel() > 0) {
					switch(shopItem.getAttrEnchantLevel()){
					case 1:  attrenchantLevel = ("$6115 "); break;
					case 2:  attrenchantLevel = ("$6116 "); break;
					case 3:  attrenchantLevel = ("$6117 "); break;
					case 4:  attrenchantLevel = ("$6118 "); break;
					case 5:  attrenchantLevel = ("$6119 "); break;
					case 6:  attrenchantLevel = ("$6120 "); break;
					case 7:  attrenchantLevel = ("$6121 "); break;
					case 8:  attrenchantLevel = ("$6122 "); break;
					case 9:  attrenchantLevel = ("$6123 "); break;
					case 10: attrenchantLevel = ("$6124 "); break;
					case 11: attrenchantLevel = ("$6125 "); break;
					case 12: attrenchantLevel = ("$6126 "); break;				
					default: break;
					}
				}
				if (shopItem.getPackCount() > 1) {
					PackCount = " ("+shopItem.getPackCount()+")";
				}
				if (shopItem.getItem().getMaxUseTime() > 0) {
					MaxUseTime = " ["+item.getMaxUseTime()+"]";
				}
				writeS(PobyBless + attrenchantLevel + Enchant + item.getNameId() + PackCount + MaxUseTime);
			} /** 포비: 상점 아이템에 축, 저주 표시하기 E*/
			template = ItemTable.getInstance().getTemplate(item.getItemId());
			if (template == null) {
				writeC(0);
			} else {
				dummy.setItem(template);
				byte[] status = dummy.getStatusBytes();
				writeC(status.length);
				for (byte b : status) {
					writeC(b);
				}
			}
		}
		writeH(0x07);// 0x00:kaimo 0x01:pearl 0x07:adena
	}

	@Override
	public byte[] getContent() throws IOException {
		return _bao.toByteArray();
	}
}
