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

import server.LineageClient;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Trade;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_TradeCancel extends ClientBasePacket {

	private static final String C_TRADE_CANCEL = "[C] C_TradeCancel";

	public C_TradeCancel(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		L1PcInstance player = clientthread.getActiveChar();
		L1Trade trade = new L1Trade();
		trade.TradeCancel(player);
		
		player.setMultiTrading(false);
		  
		  L1Object trading_partner = L1World.getInstance().findObject(player.getTradeID());
		  if (trading_partner != null) {
		   if(trading_partner instanceof L1PcInstance){
		    L1PcInstance target = (L1PcInstance)trading_partner;
		    target.setMultiTrading(false);
		   }
		  }

	}

	@Override
	public String getType() {
		return C_TRADE_CANCEL;
	}

}
