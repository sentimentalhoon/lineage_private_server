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

import l1j.server.server.Account;
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class SealScroll extends L1ItemInstance{
	
	public SealScroll(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			int itemId = this.getItemId();
			L1ItemInstance l1iteminstance1 = pc.getInventory().getItem(packet.readD());
			if (itemId == 50020){		// 봉인줌서
				if(l1iteminstance1.getBless() >=0 && l1iteminstance1.getBless() <=3){							
					int Bless = 0;
					switch(l1iteminstance1.getBless()){
					case 0: Bless = 128; break; //축
					case 1: Bless = 129; break; //보통
					case 2: Bless = 130; break; //저주
					case 3: Bless = 131; break; //미확인
					}
					l1iteminstance1.setBless(Bless);
					pc.getInventory().updateItem(l1iteminstance1, L1PcInventory.COL_BLESS);
					pc.getInventory().saveItem(l1iteminstance1, L1PcInventory.COL_BLESS);
					pc.getInventory().removeItem(useItem, 1);
				}else pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
			} else if (itemId == 50021){		// 봉인해제줌서
				Account account = Account.load(pc.getAccountName()); //추가 
				if(account.getquize() != null){
					pc.sendPackets(new S_SystemMessage("퀴즈를 인증[.퀴즈인증]하지 않으면 봉인해제를 하실 수 없습니다."));
					return;
				}
				if(l1iteminstance1.getBless() >=128 && l1iteminstance1.getBless() <=131){
					int Bless = 0;
					switch(l1iteminstance1.getBless()){
					case 128: Bless = 0; break;
					case 129: Bless = 1; break;
					case 130: Bless = 2; break;
					case 131: Bless = 3; break;
					}
					l1iteminstance1.setBless(Bless);
					pc.getInventory().updateItem(l1iteminstance1, L1PcInventory.COL_BLESS);
					pc.getInventory().saveItem(l1iteminstance1, L1PcInventory.COL_BLESS);
					pc.getInventory().removeItem(useItem, 1);
				}else pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.		
			}
		}
	}
}

