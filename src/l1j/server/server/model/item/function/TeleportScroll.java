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

import java.util.Random;

import l1j.server.server.CrockController;
import l1j.server.server.DevilController;
import l1j.server.server.SkyCastleController;
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.L1ItemDelay;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1TownLocation;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1EtcItem;
import l1j.server.server.templates.L1Item;
import l1j.server.server.utils.CommonUtil;

@SuppressWarnings("serial")
public class TeleportScroll extends L1ItemInstance{

	public TeleportScroll(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			@SuppressWarnings("unused")
			int bmapid = 0; 
			bmapid = packet.readH(); 
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));
			int itemId = useItem.getItemId();
			int delay_id = 0;
			if (useItem.getItem().getType2() == 0) { // �������� ���� ������
				delay_id = ((L1EtcItem) useItem.getItem()).get_delayid();
			}
			if (delay_id != 0) { // ���� ���� �־�
				if (pc.hasItemDelay(delay_id) == true) {
					return;
				}
			}
			if(!escapeEnable(pc)){ 
				pc.sendPackets(new S_ServerMessage(538)); // \f1���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
				pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false)); 
				L1ItemDelay.onItemUse(pc, useItem); // ������ ���� ����
				return; 
			}

			switch(itemId){
			case 140100: // �ູ ���� ���� �̵� �ֹ���
			case 40100: // ���� �̵� �ֹ���
			case 40099: // ���ž�� ���� �̵� �ֹ���
			case 40086: // �Ž� �ڷ���Ʈ �ֹ���
			case 40863: // ���� �ֹ��� (�ڷ���Ʈ)
				if(TeleportEnable(pc)){ 
					TeleportBook(pc, packet, useItem, itemId);
				} else {
					pc.sendPackets(new S_ServerMessage(538)); // \f1���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
					pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false)); 
					L1ItemDelay.onItemUse(pc, useItem); // ������ ���� ����
					return; 
				}
				break;
			case 240100:  // �������� �ڷ���Ʈ ��ũ��(�������� ������)
				CurseTeleportScroll(pc, useItem);
				break;
			case 5000214: // �׺� �̵� �ֹ���
				ThebeTeleportScroll(pc, useItem);
				break;
			case 5000215: // ƼĮ �̵� �ֹ���
				TikalTeleportScroll(pc, useItem);
				break;
			case 40079: // ��ȯ �ֹ���
			case 40095: // ���ž�� ��ȯ �ֹ���
			case 40521: // ���� ����
			case 5000176: // ������ ����
				ReturnTeleportScroll(pc, useItem);
				break;
			case 40124: // ���� ��ȯ �ֹ���
				BloodReturnTeleportScroll(pc, useItem);
				break;
			case 40081: // ��� ��ȯ �ֹ���
				GiranTeleportScroll(pc, useItem);
				break;
			case 560030: // �Ǹ��� �̵� �ֹ���
				DevilTeleportScroll(pc, useItem);
				break;
			case 560031: // �ϴü� �̵� �ֹ���
				SkyCastleTeleportScroll(pc, useItem);
				break;
			}
			L1ItemDelay.onItemUse(pc, useItem); // ������ ���� ����
		}
	}
	/**
	 * @category Ż���� �������� ����
	 * */
	private boolean escapeEnable(L1PcInstance pc){
		if (pc.getMap().isEscapable() || pc.isGm()) {
			return true;
		}
		return false;
	}
	/**
	 * @category �ڷ���Ʈ�� �������� ����
	 * */
	private boolean TeleportEnable(L1PcInstance pc){
		if (pc.getMap().isTeleportable() || pc.isGm()) {
			return true;
		}
		return false;
	}
	/**
	 * @category �ڷ���Ʈ ����
	 */
	private void TeleportBook(L1PcInstance pc, ClientBasePacket packet, L1ItemInstance useItem, int itemId){
		L1BookMark bookm = pc.getBookMark(packet.readD());
		if (bookm != null) { // �ϸ�ũ�� ��� �� �� ������(��) �ڷ���Ʈ
			if (bookm.getRandomX() > 0 || bookm.getRandomY() > 0){						
				L1Teleport.randomBookmarkTeleport(pc, bookm, pc.getMoveState().getHeading(), true);
				pc.getInventory().removeItem(useItem, 1);
			} else {						
				int newX = bookm.getLocX();
				int newY = bookm.getLocY();
				short mapId = bookm.getMapId();
				if (itemId == 40086) { // �Ž� �ڷ���Ʈ �ֹ���
					for (L1PcInstance member : L1World.getInstance().getVisiblePlayer(pc)) {
						if (pc.getLocation().getTileLineDistance(member.getLocation()) <= 3
								&& member.getClanid() == pc.getClanid()
								&& pc.getClanid() != 0
								&& member.getId() != pc.getId()
								&& !member.isPrivateShop()) {
							L1Teleport.teleport(member, newX, newY, mapId, member.getMoveState().getHeading(), true);
						}
					}
				}
				L1Teleport.teleport(pc, newX, newY, mapId, 5, true);
				pc.getInventory().removeItem(useItem, 1);
			}
		} else {
			L1Location newLocation = pc.getLocation().randomLocation(200, true);
			int newX = newLocation.getX();
			int newY = newLocation.getY();
			short mapId = (short) newLocation.getMapId();

			if (itemId == 40086) { // �Ž��ڷ���Ʈ�ֹ���
				for (L1PcInstance member : L1World.getInstance().getVisiblePlayer(pc)) {
					if (pc.getLocation().getTileLineDistance(member.getLocation()) <= 3
							&& member.getClanid() == pc.getClanid()
							&& pc.getClanid() != 0
							&& member.getId() != pc.getId()
							&& !member.isPrivateShop()) {
						L1Teleport.teleport(member, newX, newY, mapId, member.getMoveState().getHeading(), true);
					}
				}
			}
			L1Teleport.teleport(pc, newX, newY, mapId, 5, true);
			pc.getInventory().removeItem(useItem, 1);
		}
		pc.cancelAbsoluteBarrier(); // �ƺ�Ҹ�Ʈ�ٸ����� ����
	}
	/**
	 * @category ���� �ڷ���Ʈ ����
	 */
	private void CurseTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		L1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getMoveState().getHeading(), true);
		pc.getInventory().removeItem(useItem, 1);
		pc.cancelAbsoluteBarrier(); // �ƺ�Ҹ�Ʈ�ٸ����� ����
	}
	/**
	 * @category �׺� �ڷ���Ʈ ����
	 */
	private void ThebeTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		if(CrockController.getInstance().getCrockStart() == true) { 
			int i13 = 32630 + CommonUtil.random(2); 
			int k19 = 32905 + CommonUtil.random(2); 
			L1Teleport.teleport(pc, i13, k19, (short)780, 6, true); 
			pc.getInventory().removeItem(useItem,1);
		} else {
			pc.sendPackets(new S_SystemMessage("�ð��� �տ��� �������� �ʽ��ϴ�.")); 
			pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "\\fR�׺�/ƼĮ�� �ð��� �տ��� ������ �ʾҽ��ϴ�."));
		}	
	}
	/**
	 * @category ƼĮ �ڷ���Ʈ ����
	 * @param pc
	 * @param useItem
	 */
	private void TikalTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		if(CrockController.getInstance().getCrockStart() == true) { 
			Random random = new Random(); 
			int i13 = 32793 + random.nextInt(2); 
			int k19 = 32753 + random.nextInt(2); 
			L1Teleport.teleport(pc, i13, k19, (short)783, 6, true); 

			pc.getInventory().removeItem(useItem,1);
		} else {
			pc.sendPackets(new S_SystemMessage("�ð��� �տ��� �������� �ʽ��ϴ�.")); 
			pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "\\fR�׺�/ƼĮ�� �ð��� �տ��� ������ �ʾҽ��ϴ�."));
		}
	}
	/**
	 * @category ��ȯ �ֹ��� ����
	 * @param pc
	 * @param useItem
	 */
	private void ReturnTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		int[] loc = Getback.GetBack_Location(pc, true);
		L1Teleport.teleport(pc, loc[0], loc[1], (short) loc[2], 5, true);
		pc.getInventory().removeItem(useItem, 1);
		pc.cancelAbsoluteBarrier();
	}
	/**
	 * @category ���� ��ȯ �ֹ���
	 * @param pc
	 * @param useItem
	 */
	private void BloodReturnTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		int castle_id = 0;
		int house_id = 0;
		if (pc.getClanid() != 0) { // ũ�� �Ҽ�
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				castle_id = clan.getCastleId();
				house_id = clan.getHouseId();
			}
		}
		if (castle_id != 0) { // ���� ũ����
			int[] loc = new int[3];
			loc = L1CastleLocation.getCastleLoc(castle_id);
			int locx = loc[0];
			int locy = loc[1];
			short mapid = (short) (loc[2]);
			L1Teleport.teleport(pc, locx, locy, mapid, 0, true);
			pc.getInventory().removeItem(useItem, 1);
		} else if (house_id != 0) { // ����Ʈ ���� ũ����
			int[] loc = new int[3];
			loc = L1HouseLocation.getHouseLoc(house_id);
			int locx = loc[0];
			int locy = loc[1];
			short mapid = (short) (loc[2]);
			L1Teleport.teleport(pc, locx, locy, mapid, 5, true);
			pc.getInventory().removeItem(useItem, 1);
		} else {
			if (pc.getHomeTownId() > 0) {
				int[] loc = L1TownLocation.getGetBackLoc(pc.getHomeTownId());
				int locx = loc[0];
				int locy = loc[1];
				short mapid = (short) (loc[2]);
				L1Teleport.teleport(pc, locx, locy, mapid, 5, true);
				pc.getInventory().removeItem(useItem, 1);
			} else {
				int[] loc = Getback.GetBack_Location(pc, true);
				L1Teleport.teleport(pc, loc[0], loc[1],	(short) loc[2], 5, true);
				pc.getInventory().removeItem(useItem, 1);
			}
		}
		pc.cancelAbsoluteBarrier(); // �ƺ�Ҹ�Ʈ�ٸ����� ����
	}

	/**
	 * @category �����ȯ �ֹ��� ����
	 * @param pc
	 * @param useItem
	 */
	private void GiranTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		int[] loc = null;
		int ckbb = CommonUtil.random(9); // 0~11
		switch (ckbb) {
		case 0: loc = new int[] { 33439, 32804, 4 }; break;
		case 1:	loc = new int[] { 33436, 32804, 4 }; break;
		case 2: loc = new int[] { 33437, 32802, 4 }; break;
		case 3: loc = new int[] { 33439, 32804, 4 }; break;
		case 4: loc = new int[] { 33441, 32806, 4 }; break;
		case 5:	loc = new int[] { 33442, 32813, 4 }; break;
		case 6: loc = new int[] { 33434, 32824, 4 }; break;
		case 7: loc = new int[] { 33437, 32802, 4 }; break;
		case 8: loc = new int[] { 33437, 32824, 4 }; break;
		case 9: loc = new int[] { 33419, 32816, 4 }; break;
		default: loc = new int[] { 33437, 32802, 4 }; break;
		}
		L1Teleport.teleport(pc, loc[0], loc[1], (short) loc[2],2, true);
		pc.getInventory().removeItem(useItem, 1); 
		pc.cancelAbsoluteBarrier();
	}

	/**
	 * @category �Ǹ��� �̵� �ֹ��� ����
	 * @param pc
	 * @param useItem
	 */
	private void DevilTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		if(DevilController.getInstance().getDevilStart() == true) { 
			pc.getInventory().removeItem(useItem , 1);
			int x = 32723 + CommonUtil.random(4); 
			int y = 32800 + CommonUtil.random(4); 
			L1Teleport.teleport(pc, x, y, (short)5167, 6, true); 
			//pc.sendPackets(new S_SystemMessage("\\fY�Ǹ����� ���信 ���� �ϼ̽��ϴ�."));
			pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "\\fR�Ǹ����� ���信 �����ϼ̽��ϴ�."));
		}else{ 
			//pc.sendPackets(new S_SystemMessage("\\fY�Ǹ����� ���䰡 ���� ������ �ʾҽ��ϴ�.")); 
			pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, 
					"\\fR�Ǹ����� ���䰡 ������ �ʾҽ��ϴ�."));
		} 
	}

	/**
	 * @category �ϴü� �̵� �ֹ��� ����
	 * @param pc
	 * @param useItem
	 */
	private void SkyCastleTeleportScroll(L1PcInstance pc, L1ItemInstance useItem){
		if(SkyCastleController.getInstance().getSkyStart() == true) { 
			pc.getInventory().removeItem(useItem, 1);
			int x = 32863 + CommonUtil.random(2); 
			int y = 32977 + CommonUtil.random(2); 
			L1Teleport.teleport(pc, x, y, (short)630, 6, true); 
			pc.sendPackets(new S_SystemMessage("\\fY�ϴ��� �� �������� 1�ð� ����  ����˴ϴ�.")); 
			pc.sendPackets(new S_SystemMessage("\\fY�ð��� ������ ��������� �ڵ���ȯ �˴ϴ�.")); 
			pc.sendPackets(new S_SystemMessage("\\fY������ �����Ͽ� �������� �����ϸ� ��������� ��ٸ��ϴ�.")); 
		}else{ 
			//pc.sendPackets(new S_SystemMessage("\\fY���� �ϴ��� �� �������� ���۵��� �ʾҽ��ϴ�.")); 
			pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, 
					"\\fR�ϴ��� �� �������� ���۵��� �ʾҽ��ϴ�."));
		} 
	}
}

