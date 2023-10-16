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

import l1j.server.server.ActionCodes;
import l1j.server.server.TimeController.FishingTimeController;
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Fishing;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class Fishing extends L1ItemInstance{

	private static Random _random = new Random(System.nanoTime());

	public Fishing(L1Item item){
		super(item);
	}

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			int itemId = this.getItemId();
			startFishing(pc, itemId, packet.readH(),  packet.readH());
		}
	}

	private void startFishing(L1PcInstance pc, int itemId, int fishX, int fishY) {
		if (pc.getMapId() != 5302) {
			// 여기에 낚싯대를 던질 수 없습니다.
			pc.sendPackets(new S_ServerMessage(1138));
			return;
		}
		int gab = 0;
		int heading = pc.getMoveState().getHeading(); //● 방향: (0.좌상)(1.상)( 2.우상)(3.오른쪽)(4.우하)(5.하)(6.좌하)(7.좌)
		switch(heading){
		case 0: //상좌
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX(), pc.getY()-5);
			break;
		case 1: //상
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()+5, pc.getY()-5);
			break;
		case 2: //우상
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()+5, pc.getY()-5);
			break;
		case 3: //오른쪽
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()+5, pc.getY()+5);
			break;
		case 4: //우하
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX(), pc.getY()+5);
			break;
		case 5: //하
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()-5, pc.getY()+5);
			break;
		case 6: //좌하
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()-5, pc.getY());
			break;
		case 7: //좌
			gab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(pc.getX()-5, pc.getY()-5);
			break;
		}
		int fishGab = L1WorldMap.getInstance().getMap((short)5302).getOriginalTile(fishX, fishY);
		if(gab == 28 && fishGab == 28){
			if (pc.getInventory().consumeItem(41295, 1)) { // 먹이
				pc.sendPackets(new S_Fishing(pc.getId(), ActionCodes.ACTION_Fishing, fishX, fishY));
				Broadcaster.broadcastPacket(pc, new S_Fishing(pc.getId(), ActionCodes.ACTION_Fishing, fishX, fishY));
				pc.setFishing(true);
				long time = System.currentTimeMillis() + 10000 + _random.nextInt(5) * 1000;
				pc.setFishingTime(time);
				FishingTimeController.getInstance().addMember(pc);
			} else {
				// 낚시를 하기 위해서는 먹이가 필요합니다.
				pc.sendPackets(new S_ServerMessage(1137));
			}
		} else {
			// 여기에 낚싯대를 던질 수 없습니다.
			pc.sendPackets(new S_ServerMessage(1138));
		}
	} 
}

