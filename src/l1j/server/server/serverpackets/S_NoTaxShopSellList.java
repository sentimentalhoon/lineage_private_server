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
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1TaxCalculator;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.shop.L1Shop;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1ShopItem;
import l1j.server.server.model.L1BugBearRace;

public class S_NoTaxShopSellList extends ServerBasePacket {


	/**
	 * ������ ���� ����Ʈ�� ǥ���Ѵ�. ĳ���Ͱ� BUY ��ư�� ������ ���� ������.
	 */
	public S_NoTaxShopSellList(int objId) {
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
		
		// L1ItemInstance�� getStatusBytes�� �̿��ϱ� ����(������)
		L1ItemInstance dummy = new L1ItemInstance();
		L1ShopItem shopItem = null;
		L1Item item = null;
		L1Item template = null;
		for (int i = 0; i < shopItems.size(); i++) {
			shopItem = shopItems.get(i);
			item = shopItem.getItem();	
			//////////////////////////////////////////////////////////////// ���� �ӽ� �ּ� ���� �Ʒ�
			int price = (int)(shopItem.getPrice()* Config.RATE_SHOP_SELLING_PRICE);
			if (!(npcId == 70068 || npcId == 70020 || npcId == 70056)){			
				price = calc.NoTaxPrice(price);
			}
			///////////////////////////////////////////////////
			/*int price = (int)(shopItem.getPrice());
			if (!(npcId == 70068 || npcId == 70020 || npcId == 70056
					|| npcId == 70035 || npcId == 70041|| npcId == 70042)){			
				price = calc.NoTaxPrice(price);
			}*/
			
			//////////////////////////////////////////////////
			writeD(i);
			writeH(shopItem.getItem().getGfxId());			
			writeD(price);
			if (shopItem.getPackCount() > 1) {
				writeS(item.getName() + " (" + shopItem.getPackCount() + ")");
			}else if (shopItem.getItem().getMaxUseTime() > 0){
				writeS(item.getName() + " [" + item.getMaxUseTime() + "]");
			} else {
				writeS(item.getName());
			}
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
		writeH(0x07);
	}

	@Override
	public byte[] getContent() throws IOException {
		return _bao.toByteArray();
	}
}
