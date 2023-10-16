/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.model.L1TaxCalculator;
import l1j.server.server.templates.L1ShopItem;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Disconnect;

class L1ShopBuyOrder {
	private final L1ShopItem _item;
	private final int _count;
	private final int _orderNumber;
	public L1ShopBuyOrder(L1ShopItem item, int count, int orderNumber) {
		_item = item;
		_count = count;
		_orderNumber = orderNumber;
	}

	public L1ShopItem getItem() { return _item; }
	public int getCount() {	return _count; }
	public int getOrderNumber() { return _orderNumber; }
}

public class L1ShopBuyOrderList {

	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1ShopBuyOrder.class.getName());
	private final L1Shop _shop;
	private final List<L1ShopBuyOrder> _list = new ArrayList<L1ShopBuyOrder>();
	private final L1TaxCalculator _taxCalc;

	private int _totalWeight = 0;
	private int _totalPrice = 0;
	private int _totalPriceTaxIncluded = 0;
	private int bugok  = 0;	
	private int _orderNumber = 0;
	L1ShopBuyOrderList(L1Shop shop) {
		_shop = shop;
		_taxCalc = new L1TaxCalculator(shop.getNpcId());
	}

	public void add(int orderNumber, int count, L1PcInstance pc) {

		if (_shop.getSellingItems().size() < orderNumber) return; 

		L1ShopItem shopItem = _shop.getSellingItems().get(orderNumber);
		int price = (int) (shopItem.getPrice() * Config.RATE_SHOP_SELLING_PRICE);			

		for (int j = 0; j < count; j++) {
			if (price * j < 0) {
				return;
			}
		}
		if (_totalPrice < 0 ) return;
//		long totalPrice = _totalPrice;
		_totalPrice += price * count;
		_totalPriceTaxIncluded += _taxCalc.layTax(price) * count;
		_totalWeight += shopItem.getItem().getWeight() * count * shopItem.getPackCount();
		_orderNumber = orderNumber;
		if (_totalPrice < 0 || _totalPrice >= 2000000000) {			
			pc.sendPackets(new S_Disconnect());
			bugok =1;   
			return;
		}
		if (count <= 0 || count >= 10000){			
			bugok = 1;
			return;				
		}else if (price < 1000 && count >= 10000){
			bugok = 1;
			return;
		}else if ((price >= 1000 && price < 10000) && count >= 10000){
			bugok = 1;
			return;
		}else if (price >= 10000 && count > 200000){
			bugok = 1;
			return;
		}		

		if (shopItem.getItem().isStackable()) {
			_list.add(new L1ShopBuyOrder(shopItem, count * shopItem.getPackCount(), orderNumber));
			return;
		}

		for (int i = 0; i < (count * shopItem.getPackCount()); i++) {
			_list.add(new L1ShopBuyOrder(shopItem, 1, orderNumber));
		}
	}

	List<L1ShopBuyOrder> getList() { return _list; }	
	L1TaxCalculator getTaxCalculator() { return _taxCalc; }
	public int BugOk() { return bugok; }	
	public int getTotalWeight() { return _totalWeight; }
	public int getTotalPrice() { return _totalPrice; }
	public int getTotalPriceTaxIncluded() { return _totalPriceTaxIncluded; }
}
