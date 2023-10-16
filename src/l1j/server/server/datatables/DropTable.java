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

package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Drop;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.model.skill.L1SkillId;

//Referenced classes of package l1j.server.server.templates:
//L1Npc, L1Item, ItemTable

public class DropTable {

	private static Logger _log = Logger.getLogger(DropTable.class.getName());

	private static DropTable _instance;

	private final HashMap<Integer, ArrayList<L1Drop>> _droplists; // monster 마다의 드롭 리스트

	private static final byte HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

	private static final byte HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };

	public static DropTable getInstance() {
		if (_instance == null) {
			_instance = new DropTable();
		}
		return _instance;
	}



	private DropTable() {
		_droplists = allDropList();
	}

	public static void reload() {
		DropTable oldInstance = _instance;
		_instance = new DropTable();
		oldInstance._droplists.clear();
	}

	private HashMap<Integer, ArrayList<L1Drop>> allDropList() {
		HashMap<Integer, ArrayList<L1Drop>> droplistMap = new HashMap<Integer, ArrayList<L1Drop>>();

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("select * from droplist");
			rs = pstm.executeQuery();
			L1Drop drop = null;
			while (rs.next()) {
				int mobId = rs.getInt("mobId");
				int itemId = rs.getInt("itemId");
				int min = rs.getInt("min");
				int max = rs.getInt("max");
				int chance = rs.getInt("chance");

				drop = new L1Drop(mobId, itemId, min, max, chance);

				ArrayList<L1Drop> dropList = droplistMap.get(drop.getMobid());
				if (dropList == null) {
					dropList = new ArrayList<L1Drop>();
					droplistMap.put(new Integer(drop.getMobid()), dropList);
				}
				dropList.add(drop);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return droplistMap;
	}

	// 인벤트리에 드롭을 설정
	public void setDrop(L1NpcInstance npc, L1Inventory inventory) {
		// 드롭 리스트의 취득
		int mobId = npc.getNpcTemplate().get_npcId();
		ArrayList<L1Drop> dropList = _droplists.get(mobId);
		if (dropList == null) {
			return;
		}

		// 레이트 취득
		double droprate = Config.RATE_DROP_ITEMS;
		if (droprate <= 0) {
			droprate = 0;
		}
		double adenarate = Config.RATE_DROP_ADENA;
		if (adenarate <= 0) {
			adenarate = 0;
		}
		if (droprate <= 0 && adenarate <= 0) {
			return;
		}

		int itemId;
		int itemCount;
		int addCount;
		int randomChance;
		L1ItemInstance item;

		/** 환상 이벤트 **/
		L1ItemInstance Fitem;
		L1ItemInstance Citem;

		Random random = new Random(System.nanoTime());

		for (L1Drop drop : dropList) {
			// 드롭 아이템의 취득
			itemId = drop.getItemid();
			if (adenarate == 0 && itemId == L1ItemId.ADENA) {
				continue; // 아데나레이트 0으로 드롭이 아데나의 경우는 스르
			}

			// 드롭 찬스 판정
			randomChance = random.nextInt(0xf4240) + 1;
			double rateOfMapId = MapsTable.getInstance().getDropRate(npc.getMapId());
			double rateOfItem = DropItemTable.getInstance().getDropRate(itemId);
			double resultDroprate = drop.getChance() * droprate * rateOfMapId;

			resultDroprate = (int)(resultDroprate*rateOfItem);

			if (droprate == 0 || resultDroprate < randomChance) {
				continue;
			}

			// 드롭 개수를 설정
			double amount = DropItemTable.getInstance().getDropAmount(itemId);
			int min = drop.getMin();
			int max = drop.getMax();
			min = (int)(min * amount);
			max = (int)(max * amount);	


			itemCount = min;
			addCount = max - min + 1;

			if (addCount > 1) {
				itemCount += random.nextInt(addCount);
			}
			if (itemId == L1ItemId.ADENA) { // 드롭이 아데나의 경우는 아데나레이트를 건다
				if(npc.getMapId() == 410){ 
					itemCount = 0; 
				}else{
					itemCount *= adenarate;
				}				
			}
			if (itemCount < 0) {
				itemCount = 0;
			}
			if (itemCount > 2000000000) {
				itemCount = 2000000000;
			}

			// 아이템의 생성			
			item = ItemTable.getInstance().createItem(itemId);
			item.setCount(itemCount);		
			// 아이템 격납
			inventory.storeItem(item);
		}
		/** 환상 이벤트 **/
		if (Config.ALT_FANTASYEVENT == true) {		
			int itemRandom = random.nextInt(100)+1;
			int countRandom = random.nextInt(100)+1;
			int item1Random = random.nextInt(100+1);
			int Fcount = 0;
			int Itemnum = 0;
			if(item1Random <= 50){
				Itemnum = 40127;
			} else {
				Itemnum = 40128;
			}
			if(countRandom <=90 ){
				Fcount = 1;
			} else if(countRandom >=91){
				Fcount = 2;
			}				
			if(itemRandom <=40 ){					
			} else if(itemRandom >=46 || itemRandom <= 70){
				Fitem = ItemTable.getInstance().createItem(Itemnum);
				Fitem.setCount(Fcount);
				inventory.storeItem(Fitem);				
			} else if(itemRandom >=96){
				Fitem = ItemTable.getInstance().createItem(Itemnum);
				Fitem.setCount(Fcount);
				inventory.storeItem(Fitem);					
			}				
		}
		/** 추석 이벤트 **/
		if (Config.ALT_CHUSEOKEVENT == true){
			int itemRandom = random.nextInt(100)+1;
			if (itemRandom <= 3){
				Citem = ItemTable.getInstance().createItem(435014);
				inventory.storeItem(Citem);
			}
		}
		/** 깃털 이벤트 **/
		if (Config.ALT_FEATURE == true){
			short mapid = npc.getMapId();
			if ((mapid >= 450 && mapid <=478)
					||(mapid >= 490 && mapid <=496)
					||(mapid >= 530 && mapid <=536)){
				return;
			}
			int itemRandom = random.nextInt(300)+1;
			if (itemRandom <= 3){
				Citem = ItemTable.getInstance().createItem(41159);
				Citem.setCount(random.nextInt(5) + 1);
				inventory.storeItem(Citem);
			}
		}

		/** 테스트 서버 **/
		if (Config.GAME_SERVER_TYPE == 1){
			short mapid = npc.getMapId();
			if ((mapid >= 450 && mapid <=478)
					||(mapid >= 490 && mapid <=496)
					||(mapid >= 530 && mapid <=536)){
				return;
			}
			int lvl = npc.getLevel();
			int itemRandom = 0;
			if (lvl >= 20){
				itemRandom = random.nextInt(lvl * 5+1)+lvl;				
				Citem = ItemTable.getInstance().createItem(L1ItemId.TEST_MARK);
				Citem.setCount(itemRandom);
				inventory.storeItem(Citem);
			}
		}
	}

	// 드롭을 분배
	public void dropShare(L1NpcInstance npc, ArrayList<?> acquisitorList, ArrayList<?> hateList, L1PcInstance pc) {
		L1Inventory inventory = npc.getInventory();
		int mobId = npc.getNpcTemplate().get_npcId();
		if (inventory.getSize() == 0) {
			return;
		}
		if (acquisitorList.size() != hateList.size()) {
			return;
		}
		// 헤이트의 합계를 취득
		int totalHate = 0;
		L1Character acquisitor;
		for (int i = hateList.size() - 1; i >= 0; i--) {
			acquisitor = (L1Character) acquisitorList.get(i);
			if ((Config.AUTO_LOOT == 2)  // 오토 루팅 2의 경우는 사몬 및 애완동물은 생략한다
					&& (acquisitor instanceof L1SummonInstance || acquisitor instanceof L1PetInstance)) {
				acquisitorList.remove(i);
				hateList.remove(i);
			} else if (acquisitor != null && acquisitor.getMapId() == npc.getMapId()
					&& acquisitor.getLocation().getTileLineDistance(npc.getLocation()) <= Config.LOOTING_RANGE) {
				totalHate += (Integer) hateList.get(i);
			} else { // null였거나 죽기도 하고 멀었으면 배제
				acquisitorList.remove(i);
				hateList.remove(i);
			}
		}

		// 드롭의 분배
		L1ItemInstance item;
		L1Inventory targetInventory = null;
		L1PcInstance player;
		Random random = new Random();
		int randomInt;
		int chanceHate;
		int itemId;
		for (int i = inventory.getSize(); i > 0; i--) {
			item = inventory.getItems().get(0);
			itemId = item.getItem().getItemId();
			boolean isGround = false;
			if (item.getItem().getType2() == 0 && item.getItem().getType() == 2) { // light계 아이템
				item.setNowLighting(false);
			}

			if (((Config.AUTO_LOOT != 0) || (itemId == L1ItemId.ADENA)
					|| itemId == 40044 || itemId == 40045 || itemId == 40046
					|| itemId == 40047 || itemId == 40048 || itemId == 40049
					|| itemId == 40050 || itemId == 40051 || itemId == 40052
					|| itemId == 40053 || itemId == 40054 || itemId == 40055
					|| itemId == 40093 || itemId == 40094 || itemId == 140074
					|| itemId == 240074 || itemId == 40074 || itemId == 40087
					|| itemId == 140087 || itemId == 240087 || itemId == 140100
					|| itemId == 40913 || itemId == 40914 || itemId == 40915
					|| itemId == 40397 || itemId == 40398 || itemId == 3500046 || itemId == 40399
					|| itemId == 40400 || itemId == 3500045 || itemId == 40466 || itemId == 5000177 
					|| itemId == 40718 || itemId == 40678 || itemId == 430023 || itemId == 20281 || itemId == 20288
					|| itemId == 40916 || itemId == 40710 || (itemId >= 40131 && itemId <= 40135)
					|| itemId == 40618 || itemId == 40643 || itemId == 40645 || itemId == 40651 || itemId == 440676
					|| itemId == 40466 || itemId == 40491 || itemId == 41159
					|| itemId == 40468 || itemId == 40441 || itemId == 40489 || itemId == 40496
					|| itemId == 40033 || itemId == 40034 || itemId == 40035 || itemId == 40036
					|| itemId == 40037 || itemId == 40038
					|| (itemId >= 40043 && itemId <= 40055)
					|| (itemId >= 40090 && itemId <= 40094)
					|| (itemId >= 40104 && itemId <= 40113))// 토템
					&& totalHate > 0) { // 오토 루팅이나 아데나로 취득자가 있는 경우
				/*	if (Config.AUTO_LOOT != 0 && totalHate > 0) { */

				randomInt = random.nextInt(totalHate);
				chanceHate = 0;
				for (int j = hateList.size() - 1; j >= 0; j--) {
					chanceHate += (Integer) hateList.get(j);
					if (chanceHate > randomInt) {
						acquisitor = (L1Character) acquisitorList.get(j);
						if (itemId >= 40131 && itemId <= 40135) {
							if (!(acquisitor instanceof L1PcInstance) || hateList.size() > 1) {
								targetInventory = null;
								break;
							}
							player = (L1PcInstance) acquisitor;
							if (player.getQuest().get_step(L1Quest.QUEST_LYRA) != 1) {
								inventory.removeItem(item, item.getCount());
								break;
							}
						}
						if (acquisitor.getInventory().checkAddItem(item, item.getCount()) == L1Inventory.OK) {
							targetInventory = acquisitor.getInventory();
							if (acquisitor instanceof L1PcInstance) {
								player = (L1PcInstance) acquisitor;
								L1ItemInstance l1iteminstance = player
										.getInventory().findItemId(L1ItemId.ADENA); // 소지 아데나를 체크
								if (l1iteminstance != null && l1iteminstance.getCount() > 2000000000) {
									targetInventory = L1World.getInstance().getInventory(acquisitor.getX(),	acquisitor.getY(), acquisitor.getMapId()); // 가질 수  없기 때문에 발밑에 떨어뜨린다
									isGround = true;
									player.sendPackets(new S_ServerMessage(166,	"소지하고 있는 아데나", "20억을 초과하고 있습니다.")); 
								} else {
									if (player.isInParty() && !player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_MENT)) { // 파티의 경우
										for (L1PcInstance partymember : player.getParty().getMembers()) {
											partymember.sendPackets(new S_ServerMessage(813, npc.getName(), item.getLogName(), player.getName()));
										}
									} else if (!player.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_MENT)){ //by사부 멘트 
										// 솔로의 경우
										player.sendPackets(new S_ServerMessage(143, npc.getName(), item.getLogName())); // \f1%0이%1를 주었습니다.
									}
								}
							} //통으로 교체
						} else {
							targetInventory = L1World.getInstance().getInventory(acquisitor.getX(), acquisitor.getY(), acquisitor.getMapId()); // 가질 수  없기 때문에 발밑에 떨어뜨린다
							isGround = true;
						}
						break;
					}
				}
			} else { // Non 오토 루팅
				item.setDropMobId(mobId);
				item.startItemOwnerTimer(pc);
				List<Integer> dirList = new ArrayList<Integer>();
				for (int j = 0; j < 8; j++) {
					dirList.add(j);
				}
				int x = 0;
				int y = 0;
				int dir = 0;
				do {
					if (dirList.size() == 0) {
						x = 0;
						y = 0;
						break;
					}
					randomInt = random.nextInt(dirList.size());
					dir = dirList.get(randomInt);
					dirList.remove(randomInt);
					x = HEADING_TABLE_X[dir];
					y = HEADING_TABLE_Y[dir];
				} while (!npc.getMap().isPassable(npc.getX(), npc.getY(), dir));
				targetInventory = L1World.getInstance().getInventory(npc.getX() + x, npc.getY() + y, npc.getMapId());
				isGround = true;
			}
			if (itemId >= 40131 && itemId <= 40135) {
				if (isGround || targetInventory == null) {
					inventory.removeItem(item, item.getCount());
					continue;
				}
			}
			if (targetInventory == null){
				targetInventory = L1World.getInstance().getInventory(npc.getX(), npc.getY(), npc.getMapId()); // 가질 수  없기 때문에 발밑에 떨어뜨린다
				isGround = true; 
			}
			inventory.tradeItem(item, item.getCount(), targetInventory);
		}
		npc.getLight().turnOnOffLight();
	}

	public void setPainwandDrop(L1NpcInstance npc, L1Inventory inventory) {
		// 드롭 리스트의 취득
		int mobId = npc.getNpcTemplate().get_npcId();
		ArrayList<L1Drop> dropList = _droplists.get(mobId);
		if (dropList == null) {
			return;
		}

		// 레이트 취득
		double droprate = Config.RATE_DROP_ITEMS;
		if (droprate <= 0) {
			droprate = 0;
		}
		double adenarate = Config.RATE_DROP_ADENA;
		if (adenarate <= 0) {
			adenarate = 0;
		}
		if (droprate <= 0 && adenarate <= 0) {
			return;
		}

		int itemId;
		int itemCount;
		int addCount;
		int randomChance;
		L1ItemInstance item;	
		Random random = new Random(System.nanoTime());

		for (L1Drop drop : dropList) {
			// 드롭 아이템의 취득
			itemId = drop.getItemid();
			if (adenarate == 0 && itemId == L1ItemId.ADENA) {
				continue; // 아데나레이트 0으로 드롭이 아데나의 경우는 스르
			}
			if (itemId != L1ItemId.ADENA) {
				continue; 
			}

			// 드롭 찬스 판정
			randomChance = random.nextInt(0xf4240) + 1;
			double rateOfMapId = MapsTable.getInstance().getDropRate(npc.getMapId());
			double rateOfItem = DropItemTable.getInstance().getDropRate(itemId);
			if (droprate == 0 || drop.getChance() * droprate * rateOfMapId * rateOfItem < randomChance) {
				continue;
			}

			// 드롭 개수를 설정
			double amount = DropItemTable.getInstance().getDropAmount(itemId);
			int min = drop.getMin();
			int max = drop.getMax();
			if (amount < 0){
				min = (int)(min / amount);
				max = (int)(max / amount);
			}else{
				min = (int)(min * amount);
				max = (int)(max * amount);	
			}

			itemCount = min;
			addCount = max - min + 1;

			if (addCount > 1) {
				itemCount += random.nextInt(addCount);
			}
			if (itemId == L1ItemId.ADENA) { // 드롭이 아데나의 경우는 아데나레이트를 건다
				if(npc.getMapId() == 410){ 
					itemCount = 0; 
				}else{
					itemCount *= adenarate;
				}				
			}
			if (itemCount < 0) {
				itemCount = 0;
			}
			if (itemCount > 2000000000) {
				itemCount = 2000000000;
			}

			// 아이템의 생성			
			item = ItemTable.getInstance().createItem(itemId);
			item.setCount(itemCount);		
			// 아이템 격납
			inventory.storeItem(item);
		}
	}

}