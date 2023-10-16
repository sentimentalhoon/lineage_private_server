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
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Trade;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1BuffNpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_TradeOK extends ClientBasePacket {

	private static final String C_TRADE_CANCEL = "[C] C_TradeOK";

	public C_TradeOK(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);

		L1PcInstance player = clientthread.getActiveChar();
		L1Object trading_partner = L1World.getInstance().findObject(player.getTradeID());

		if (trading_partner != null) {
			if(trading_partner instanceof L1PcInstance){
				L1PcInstance target = (L1PcInstance)trading_partner;

				player.setTradeOk(true);
				player.setMultiTrading(false);
				target.setMultiTrading(false);

				if (target.isChaTradeSlot()) {
					player.sendPackets(new S_SystemMessage("�ŷ� ��󿡰� �� ĳ���� ������ �����ϴ�."));
					target.sendPackets(new S_SystemMessage("�� ĳ���� ������ �����ϴ�. ĳ���� ������ Ȯ���ϰ� �ٽ� �õ����ֽñ� �ٶ��ϴ�."));

					L1Trade trade = new L1Trade();

					trade.TradeCancel(player);
					target.setChaTradeSlot(false);
					return;
				} else if (player.isChaTradeSlot()) {
					target.sendPackets(new S_SystemMessage("�ŷ� ��󿡰� �� ĳ���� ������ �����ϴ�."));
					player.sendPackets(new S_SystemMessage("�� ĳ���� ������ �����ϴ�. ĳ���� ������ Ȯ���ϰ� �ٽ� �õ����ֽñ� �ٶ��ϴ�."));

					L1Trade trade = new L1Trade();

					trade.TradeCancel(player);
					player.setChaTradeSlot(false);
					return;
				}
				if (player.getTradeOk() && target.getTradeOk()) // ��� OK�� ������

				{
					// (180 - 16) ���̸��̶�� Ʈ���̵� ����.
					// ������ ��ġ�� ������(�Ƶ����� )�� �̹� ������ �ִ� ��츦 ������� �ʴ� ���� �� �ȴ�.
					if (player.getInventory().getSize() < (180 - 16)
							&& target.getInventory().getSize() < (180 - 16)) {// ������ �������� ��뿡�� �ǳ��ش�				
						L1Trade trade = new L1Trade();
						trade.TradeOK(player);
					} else {// ������ �������� ���߿� �ǵ�����				
						player.sendPackets(new S_ServerMessage(263)); // \f1�ѻ���� ĳ���Ͱ� ������ ���� �� �ִ� �������� �ִ� 180�������Դϴ�.
						target.sendPackets(new S_ServerMessage(263)); // \f1�ѻ���� ĳ���Ͱ� ������ ���� �� �ִ� �������� �ִ� 180�������Դϴ�.
						L1Trade trade = new L1Trade();
						trade.TradeCancel(player);
					}
				}
			}else if(trading_partner instanceof L1BuffNpcInstance){
				L1BuffNpcInstance target = (L1BuffNpcInstance)trading_partner;
				player.setTradeOk(true);

				if (player.getTradeOk()){ // ��� OK�� ������
					if(player.getTradeWindowInventory().findItemId(40308).getCount() != 30000 && target.getNpcTemplate().get_npcId() == 4206004){ //����(��������)
						L1Trade trade = new L1Trade();
						trade.TradeCancel(player);
						Broadcaster.broadcastPacket(target, new S_NpcChatPacket(target, "3����������", 0));
						return;
					}

					if(player.getTradeWindowInventory().findItemId(40308).getCount() != 300000 && target.getNpcTemplate().get_npcId() == 7000070){ //����(����ȯ����)
						L1Trade trade = new L1Trade();
						trade.TradeCancel(player);
						Broadcaster.broadcastPacket(target, new S_NpcChatPacket(target, "30����������", 0));
						return;
					}
					
					if(player.getTradeWindowInventory().findItemId(40308).getCount() > 1000000 
							&& (target.getNpcTemplate().get_npcId() == 7000073
							|| target.getNpcTemplate().get_npcId() == 7000074
							|| target.getNpcTemplate().get_npcId() == 7000076
							|| target.getNpcTemplate().get_npcId() == 7000078)){ // �ֻ���, �Ҹ�, ����� ����
						L1Trade trade = new L1Trade();
						trade.TradeCancel(player);
						Broadcaster.broadcastPacket(target, new S_NpcChatPacket(target, "100���� ���ϸ� ����", 0));
						return;
					}
					// (180 - 16) ���̸��̶�� Ʈ���̵� ����.
					// ������ ��ġ�� ������(�Ƶ����� )�� �̹� ������ �ִ� ��츦 ������� �ʴ� ���� �� �ȴ�.
					if (player.getInventory().getSize() < (180 - 16)
							&& target.getInventory().getSize() < (180 - 16)) {// ������ �������� ��뿡�� �ǳ��ش�    
						L1Trade trade = new L1Trade();
						trade.TradeOK(player);
					} else {// ������ �������� ���߿� �ǵ�����    
						player.sendPackets(new S_ServerMessage(263)); // \f1�ѻ���� ĳ���Ͱ� ������ ���� �� �ִ� �������� �ִ� 180�������Դϴ�.
						L1Trade trade = new L1Trade();
						trade.TradeCancel(player);
					}
				}
			}
		}
	}

	@Override
	public String getType() {
		return C_TRADE_CANCEL;
	}

}
