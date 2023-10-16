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

import java.util.ArrayList;
import l1j.server.Config;
import l1j.server.Warehouse.ClanWarehouse;
import l1j.server.Warehouse.ElfWarehouse;
import l1j.server.Warehouse.PackageWarehouse;
import l1j.server.Warehouse.PrivateWarehouse;
import l1j.server.Warehouse.Warehouse;
import l1j.server.Warehouse.WarehouseManager;
import l1j.server.server.datatables.NpcShopTable;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.model.L1BugBearRace;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.shop.L1Shop;
import l1j.server.server.model.shop.L1ShopBuyOrderList;
import l1j.server.server.model.shop.L1ShopSellOrderList;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1PrivateShopBuyList;
import l1j.server.server.templates.L1PrivateShopSellList;

import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_SystemMessage;
//import l1j.server.server.BugKick;	//** �������� ó�� **//	By �����
import server.LineageClient;
import server.manager.eva;

import l1j.server.server.Log.LogDwarfIn;
import l1j.server.server.Log.LogDwarfOut;
import l1j.server.server.clientpackets.ClientBasePacket;


public class C_ShopAndWarehouse extends ClientBasePacket {
	private final int TYPE_BUY_SHP = 0;  // ���� or ���� ���� ���
	private final int TYPE_SEL_SHP = 1;  // ���� or ���� ���� �ȱ�
	private final int TYPE_PUT_PWH = 2;  // ���� â�� �ñ��
	private final int TYPE_GET_PWH = 3;  // ���� â�� ã��
	private final int TYPE_PUT_CWH = 4;  // ���� â�� �ñ��
	private final int TYPE_GET_CWH = 5;  // ���� â�� ã��
	private final int TYPE_PUT_EWH = 8;  // ���� â�� �ñ��
	private final int TYPE_GET_EWH = 9;  // ���� â�� ã��
	private final int TYPE_GET_MWH = 10; // ��Ű�� â�� ã��

	public C_ShopAndWarehouse(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		int npcObjectId = readD();
		int resultType = readC();
		int size = readC();
		@SuppressWarnings("unused")
		int unknown = readC();

		L1PcInstance pc = clientthread.getActiveChar();
		int level = pc.getLevel();

		if(size < 0){
			pc.sendPackets(new S_Disconnect());
			return;
		}

		if (size > 150) {
			clientthread.kick();
			clientthread.close();
			return;
		}

		if(pc.getOnlineStatus() == 0 || isTwoLogin(pc)){
			clientthread.kick();
			clientthread.close();
			return;
		}

		int npcId = 0;
		String npcImpl = "";
		boolean isPrivateShop = false;
		@SuppressWarnings("unused")
		boolean tradable = true;
		L1Object findObject = L1World.getInstance().findObject(npcObjectId);

		if (findObject == null) {
			clientthread.kick();
			clientthread.close();
			return;
		}
		if (findObject != null) { // 3 �Ž� �̻� �������� ��� �׼� ��ȿ
			int diffLocX = Math.abs(pc.getX() - findObject.getX());
			int diffLocY = Math.abs(pc.getY() - findObject.getY());
			if (diffLocX > 3 || diffLocY > 3) {
				return;
			}

			if (findObject instanceof L1NpcInstance) {
				L1NpcInstance targetNpc = (L1NpcInstance) findObject;
				npcId = targetNpc.getNpcTemplate().get_npcId();
				npcImpl = targetNpc.getNpcTemplate().getImpl();
			}else if (findObject instanceof L1PcInstance) {
				isPrivateShop = true;
			}
		}

		if(resultType == 0||resultType == 2){
			if(size > 1000){
				eva.writeMessage(-4, "������ ����!! ["+pc.getName()+"] IP "+clientthread.getIp());
				System.out.println("������ ����!! ["+pc.getName()+"] IP "+clientthread.getIp());
				clientthread.kick();
			}
		} 

		////�ߺ� ���� ���׹��� 
		if(pc.getOnlineStatus() == 0){
			clientthread.kick();
			return;
		}
		////�ߺ� ���� ���׹��� 

		switch(resultType) {
		case TYPE_BUY_SHP : // ���� or ���� ���� ���
			/*
			if(level <= Config.NEWUSERSAFETY_LEVEL && pc.getClanid() <= 0){ 
				pc.sendPackets(new S_SystemMessage("\\fR�� [" + Config.NEWUSERSAFETY_LEVEL + "] ���ϴ� ���Ϳ� �����ϼž� �����̿��� �����մϴ�.")); 
				pc.sendPackets(new S_SystemMessage("\\fR�κ��� �����ϰ� �ִ� ���� ���� �ֹ����� ����Ͽ� �ֽñ� �ٶ��ϴ�.")); 
				return ;
			}
			*/
			/*	 int itemNumber; long itemcount;
	         L1Shop shop = ShopTable.getInstance().get(npcId);
		     L1ShopBuyOrderList orderList = shop.newBuyOrderList();
			////////���׹���////////////////////////////	
		     for (int i = 0; i < size; i++) {
				itemNumber = readD();
				itemcount = readD();
				if(itemcount <= 0) {
					return;
				}
				orderList.add(itemNumber, (int)itemcount , pc);	
			}
        	///////���׹���////////////////////////////
			int bugok = orderList.BugOk();
			if (bugok == 0){
				shop.sellItems(pc, orderList);
			}   */

			if (npcId == 70035 || npcId == 70041 || npcId == 70042) {
				int status = L1BugBearRace.getInstance().getBugRaceStatus();
				boolean chk = L1BugBearRace.getInstance().buyTickets;
				if (status != L1BugBearRace.STATUS_READY || chk == false) {
					return;
				}
			}
			if(size != 0 && npcImpl.equalsIgnoreCase("L1Merchant"))  
				buyItemFromShop(pc, npcId, size);
			if(size != 0 && npcImpl.equalsIgnoreCase("L1NpcShop")){
				buyItemFromNpcShop(pc, npcId, size);
				break;
			}
			if(size != 0 && isPrivateShop)						     
				buyItemFromPrivateShop(pc, findObject, size);
			break;
		case TYPE_SEL_SHP : // ���� or ���� ���� �ȱ�
			if(size != 0 && npcImpl.equalsIgnoreCase("L1Merchant")) 
				sellItemToShop(pc, npcId, size);
			if(size != 0 && isPrivateShop)							 
				sellItemToPrivateShop(pc, findObject, size);
			break;
			// ���� ��Ž� ���� ������ �����ּ� �Ʒ� ���� // by �����			
			/*		for (int i = 0; i < size; i++) {
				itemNumber = readD();
				itemcount = readD();
				if(itemcount <= 0){
					return;
				}
				orderList.add(itemNumber, (int)itemcount , pc);	
			}
			if (bugok == 0){
			shop.buyItems(orderList);

				}
			// ���� ��Ž� ���� ������ �����ּ� �Ʒ� ����// by �����	

			 */

		case TYPE_PUT_PWH : // ���� â�� �ñ��
			if(size != 0 && npcImpl.equalsIgnoreCase("L1Dwarf")&& level >= 5){// �ڽ��� â�� �ݳ�
				//		 pc.sendPackets(new S_SystemMessage("\\fUâ��� 5���� �̻� ��� ���� �մϴ�."));   
				putItemToPrivateWarehouse(pc, size);

				break;
			}
			// ������� //	By �����
			/*if(pc.getInventory().findItemId(40308).getCount() <30){
				pc.sendPackets(new S_SystemMessage("�Ƶ����� �����մϴ�."));
				break;
			}*/
			// ������� //	By �����
			int objectId; 
			long count;
			//���������//������

			for (int i = 0; i < size; i++) {
				tradable = true;
				objectId = readD();
				count = readD();
				if(count < 0){
					pc.sendPackets(new S_Disconnect());
					//  _log.info("â�� �ñ�� ���� �õ� : char=" + pc.getName());
					return;
				}
				//���������//������
				L1Object object = pc.getInventory().getItem(objectId);
				L1ItemInstance item = (L1ItemInstance) object;

				int itemType = item.getItem().getType2();

				/*���׹���*/
				if (objectId != item.getId()) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if (!item.isStackable() && count != 1) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if (count > item.getCount()) {
					count = item.getCount();
				}
				if(item.getCount() > 1000000000){
					return;
				}
				if(count > 1000000000){
					return;
				}

				/*	if (!pc.isGm() && item.getItem().getItemId() == 40005)  {
					pc.sendPackets(new S_SystemMessage("���ʴ� â�� �ñ� �� �����ϴ�."));
					return;
				}	*/
				/*���׹���*/

				if (item == null || item.getCount() < count) {
					pc.sendPackets(new S_Disconnect());
					return;
				}
				if ((itemType == 1 && item.getCount() != 1) || (itemType == 2 && item.getCount() != 1)){
					pc.sendPackets(new S_Disconnect());
					return;
				}
				/*	       if (count <= 0 || count < 1 || item.getCount() <= 0) {
		        BugKick.getInstance().KickPlayer(pc);
		           return;
		       }   */

				/*if (item.getItem().getItemId() == 41159 || item.getItem().getItemId() == 41246 ){ 
					pc.sendPackets(new S_SystemMessage("�ش� �������� â�� �̿��� �� �� �����ϴ�."));
					return;
				}*/

				/*	  if (item.getItem().getLockitem() > 100){
                    pc.sendPackets(new S_SystemMessage("���ε� �������� â�� �ñ� �� �����ϴ�."));
                    return;
                }
				 */       
				/*		if (!item.getItem().isTradable()) {
		     tradable = false;
		     pc.sendPackets(new S_ServerMessage(210, item.getItem()
		       .getName())); // \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
		    }
		    Object[] petlist = pc.getPetList().values().toArray();
		    for (Object petObject : petlist) {
		     if (petObject instanceof L1PetInstance) {
		      L1PetInstance pet = (L1PetInstance) petObject;
		      if (item.getId() == pet.getItemObjId()) {
		       tradable = false;
		       // \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
		       pc.sendPackets(new S_ServerMessage(210, item
		         .getItem().getName()));
		       break;
		      }
		     }
		    }
		      L1DollInstance doll = null;
		      Object[] dollList = pc.getDollList().values().toArray();
		      for (Object dollObject : dollList) {
		      doll = (L1DollInstance) dollObject;
		      if (item.getId() == doll.getItemObjId()) {
		      pc.sendPackets(new S_ServerMessage(1181)); // 
		       return;
		      }
		     }
		    if (pc.getMySqlCharactersItemStorage().checkAddItemToWarehouse(item, (int)count,
		      L1Inventory.WAREHOUSE_TYPE_PERSonAL) == L1Inventory
		        .SIZE_OVER) {
		     pc.sendPackets(new S_ServerMessage(75)); // \f1 �� �̻� ���� �δ� ��Ұ� �����ϴ�.
		     break;
		    }
		    if (tradable) {
		     pc.getInventory().tradeItem(objectId, (int)count,
		       pc.MySqlCharactersItemStorage());
		     pc.turnOnOffLight();
		    }
				 */
			}


		case TYPE_GET_PWH : // ���� â�� ã��
			if(size != 0 && npcImpl.equalsIgnoreCase("L1Dwarf")&& level >= 5){// �ڽ��� â�� ã��
				//		 pc.sendPackets(new S_SystemMessage("\\fUâ��� 5���� �̻� ��� ���� �մϴ�."));  
				getItemToPrivateWarehouse(pc, size);
				break;
			}
		case TYPE_PUT_CWH : // ���� â�� �ñ��
			if(npcImpl.equalsIgnoreCase("L1Dwarf")&& level >= 5){
				//			 pc.sendPackets(new S_SystemMessage("\\fU����â��� 5���� �̻� ��� ���� �մϴ�."));
				putItemToClanWarehouse(pc, size);
				break;
			}

		case TYPE_GET_CWH : // ���� â�� ã��
			if(npcImpl.equalsIgnoreCase("L1Dwarf")&& level >= 5){
				//		 pc.sendPackets(new S_SystemMessage("\\fU����â��� 5���� �̻� ��� ���� �մϴ�."));
				getItemToClanWarehouse(pc, size);
				break;
			}
		case TYPE_PUT_EWH : // ���� â�� �ñ��
			if(size != 0 && npcImpl.equalsIgnoreCase("L1Dwarf")&& level >= 5){
				//		 pc.sendPackets(new S_SystemMessage("\\fUâ��� 5���� �̻� ��� ���� �մϴ�."));
				putItemToElfWarehouse(pc, size);
				break;
			}
		case TYPE_GET_EWH : // ���� â�� ã��
			if(size != 0 && npcImpl.equalsIgnoreCase("L1Dwarf")&& level >= 5){
				//		 pc.sendPackets(new S_SystemMessage("\\fUâ��� 5���� �̻� ��� ���� �մϴ�."));
				getItemToElfWarehouse(pc, size);
				break;
			}
		case TYPE_GET_MWH : // ��Ű�� â�� ã��
			if(size != 0 && npcImpl.equalsIgnoreCase("L1Dwarf")) 
				getItemToPackageWarehouse(pc, size);
			break;
		default:		
		}
	}

	private void doNothingClanWarehouse(L1PcInstance pc) {
		if(pc == null) return;

		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
		if(clan == null) return;


		ClanWarehouse clanWarehouse = WarehouseManager.getInstance().getClanWarehouse(clan.getClanName());
		if(clanWarehouse == null) 
			return;

		clanWarehouse.unlock(pc.getId());
	}

	private void getItemToPackageWarehouse(L1PcInstance pc, int size) {
		int objectId, count;
		L1ItemInstance item = null;
		PackageWarehouse w = WarehouseManager.getInstance().getPackageWarehouse(pc.getAccountName());
		if(w == null) return;

		for (int i = 0; i < size; i++) {
			objectId = readD();
			count = readD();		
			item = w.getItem(objectId);

			if(!isAvailableTrade(pc, objectId, item, count)) break;
			if(!hasAdena(pc)) break;   
			if(count > item.getCount()) count = item.getCount();
			if(!isAvailablePcWeight(pc, item, count)) break;

			w.tradeItem(item, count, pc.getInventory());
			if (item.getEnchantLevel() == 0){
				eva.writeMessage(-9, "��Ű��:" + pc.getName() + ":" + item.getName() +"/"+ count +"��");
			} else {
				eva.writeMessage(-9, "��Ű��:" + pc.getName() + ":" + item.getEnchantLevel() + " "+ item.getName() +"/"+ count +"��");
			}
		}
	}

	private void getItemToElfWarehouse(L1PcInstance pc, int size) {
		if(pc.getLevel() < 50 || !pc.isElf()) return;

		L1ItemInstance item;
		ElfWarehouse elfwarehouse = WarehouseManager.getInstance().getElfWarehouse(pc.getAccountName());
		if(elfwarehouse == null) return;

		for (int i = 0, objectId, count; i < size; i++) {
			objectId = readD();
			count = readD();
			item = elfwarehouse.getItem(objectId);

			/*���׹���*/
			if (objectId != item.getId()) {
				pc.sendPackets(new S_Disconnect());
				break;  }
			if (!item.isStackable() && count != 1) {
				pc.sendPackets(new S_Disconnect());
				break;  }
			/*���׹���*/

			if (item == null || item.getCount() < count  || count <= 0 || item.getCount() <= 0) {
				//				BugKick.getInstance().KickPlayer(pc);		       
				break;  }
			if (item.getCount() > 1000000000) {
				break;   }
			if (count > 1000000000) {
				break;    }
			if(!isAvailableTrade(pc, objectId, item, count)) break;		
			if(!hasAdena(pc)) break;
			if (count > item.getCount()) count = item.getCount();
			if(!isAvailablePcWeight(pc, item, count)) break;

			if (pc.getInventory().consumeItem(40494, 2)) {
				elfwarehouse.tradeItem(item, count, pc.getInventory());
				if (item.getEnchantLevel() == 0){
					eva.writeMessage(-9, "����[ã]:"+ pc.getName() +"/" + item.getName() +"/"+ count+"��");
				} else {
					eva.writeMessage(-9, "����[ã]:"+ pc.getName() +"/" + item.getEnchantLevel() + " " + item.getName() +"/"+ count+"��");
				}

			} else {
				pc.sendPackets(new S_ServerMessage(337,"$767"));
				break;
			}
		}
	}

	private void putItemToElfWarehouse(L1PcInstance pc, int size) {
		if(pc.getLevel() < 50 || !pc.isElf()) return;

		L1Object object = null;
		L1ItemInstance item = null;
		ElfWarehouse elfwarehouse = WarehouseManager.getInstance().getElfWarehouse(pc.getAccountName());
		if(elfwarehouse == null) return;

		for (int i = 0, objectId, count ; i < size ; i++) {

			objectId = readD();
			count = readD();

			object = pc.getInventory(). getItem(objectId);
			item = (L1ItemInstance) object;

			/*	if (item.getItem().getItemId() == 41159 || item.getItem().getItemId() == 41246){ 
				pc.sendPackets(new S_SystemMessage("�ش� �������� â�� �̿��� �� �� �����ϴ�."));
				break;
			}*/
			/*			if (item.getItem().getItemId() == 40074 || item.getItem().getItemId() == 40087
		            || item.getItem().getItemId() == 140074 || item.getItem().getItemId() == 140087
		            || item.getItem().getItemId() == 240074 || item.getItem().getItemId() == 240087)  {
		                    pc.sendPackets(new S_SystemMessage("������ ��ȭ�ֹ����� â�� �̿��� �� �� �����ϴ�."));
		             return;}   */

			// ��� â�� ��� ����
			//	if (pc.getAccessLevel() == Config.GMCODE) break;			
			if(!isAvailableTrade(pc, objectId, item, count)) break;
			if(count > item.getCount()) count = item.getCount();
			/*���׹���*/
			if (objectId != item.getId()) {
				pc.sendPackets(new S_Disconnect());
				break;  }
			/*	if (!item.isStackable() && count != 1) {
				pc.sendPackets(new S_Disconnect());
				break;  }*/
			/*���׹���*/

			if (item == null || item.getCount() < count  || count <= 0 || item.getCount() <= 0) {
				break;  }
			if (item.getCount() > 1000000000) {
				break;   }
			if (count > 1000000000) {
				break;    }

			if (!item.getItem().isTradable()) {
				pc.sendPackets(new S_ServerMessage(210, item.getItem(). getName())); // \f1%0�� �����ų� �Ǵ� Ÿ�ο��� ������ �� �� �����ϴ�.
				break;
			}

			if(!checkPetList(pc, item)) break;
			if(!isAvailableWhCount(elfwarehouse, pc, item, count)) break;

			pc.getInventory().tradeItem(objectId, count, elfwarehouse);
			pc.getLight().turnOnOffLight();
			if (item.getEnchantLevel() == 0){
				eva.writeMessage(-9, "����[��]:" + pc.getName() + "/" + item.getName() + "/" + count + "��");
			} else {
				eva.writeMessage(-9, "����[��]:" + item.getEnchantLevel() + " " + pc.getName() + "/" + item.getName() + "/" + count + "��");
			}
		}
	}



	private void getItemToClanWarehouse(L1PcInstance pc, int size) {
		if(pc.getLevel() < 5) return;


		if(size == 0) {
			doNothingClanWarehouse(pc);
			return;
		}

		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());


		if(!isAvailableClan(pc, clan)) return;

		L1ItemInstance item;
		ClanWarehouse clanWarehouse = WarehouseManager.getInstance().getClanWarehouse(clan.getClanName());
		if(clanWarehouse == null) return;
		for (int i = 0, objectId, count; i < size; i++) {
			objectId = readD();
			count = readD();
			item = clanWarehouse.getItem(objectId);   

			if(!isAvailableTrade(pc, objectId, item, count)) break;
			if(!hasAdena(pc)) break;			
			if(count >= item.getCount()) count = item.getCount();			
			if(!isAvailablePcWeight(pc, item, count)) break;

			/*���׹���*/
			if (objectId != item.getId()) {
				pc.sendPackets(new S_Disconnect());
				break; }
			if (!item.isStackable() && count != 1) {
				pc.sendPackets(new S_Disconnect());
				break;    }
			if (count > item.getCount()) {
				count = item.getCount();
			}
			/*���׹���*/
			if (item == null || item.getCount() < count) {
				pc.sendPackets(new S_Disconnect());
				break;  }
			if (count <= 0 || count < 1 || item.getCount() <= 0) {
				pc.sendPackets(new S_Disconnect());
				return;		        
			}   
			clanWarehouse.tradeItem(item, count, pc.getInventory());
			if (item.getEnchantLevel() == 0){
				eva.writeMessage(-9, "����[ã]:" + pc.getName() + "[" +pc.getClanname() + "]" + item.getName() + "/" + count + "��");
			} else { 
				eva.writeMessage(-9, "����[ã]:" + pc.getName() + "[" +pc.getClanname() + "]" + item.getEnchantLevel() + " "+item.getName() + "/" + count + "��");
			}
		}
		clanWarehouse.unlock(pc.getId());
	}


	private void putItemToClanWarehouse(L1PcInstance pc, int size) {
		if(size == 0) {
			doNothingClanWarehouse(pc);
			return;
		}


		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());

		if(!isAvailableClan(pc, clan)) return;

		L1Object object = null;
		L1ItemInstance item = null;
		ClanWarehouse clanWarehouse = WarehouseManager.getInstance().getClanWarehouse(clan.getClanName());
		if(clanWarehouse == null) return;

		for (int i = 0, objectId, count; i < size; i++) {
			objectId = readD();
			count = readD();

			object = pc.getInventory().getItem(objectId);
			item = (L1ItemInstance) object;

			if(item == null) break;
			if(count > item.getCount())	count = item.getCount();
			/*	if(pc.getAccessLevel() == Config.GMCODE){
				pc.sendPackets(new S_SystemMessage("��ڴ� â�� �̿� �� �� �����ϴ�."));	
				break; } */// ��� â�� ��� ����
			if(!isAvailableTrade(pc, objectId, item, count)) break;
			/*		if (item.getItem().getItemId() == 40074 || item.getItem().getItemId() == 40087
		            || item.getItem().getItemId() == 140074 || item.getItem().getItemId() == 140087
		            || item.getItem().getItemId() == 240074 || item.getItem().getItemId() == 240087)  {
		                    pc.sendPackets(new S_SystemMessage("������ ��ȭ�ֹ����� â�� �̿��� �� �� �����ϴ�."));
		             return;}   */
			/*	if (item.getItem().getItemId() == 41159 || item.getItem().getItemId() == 41246 ){ 
				pc.sendPackets(new S_SystemMessage("�ش� �������� â�� �̿��� �� �� �����ϴ�."));
				return;}  */ 


			if(item.getBless() >= 128 || !item.getItem().isTradable()){ 
				// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� �絵 �� �� �����ϴ�.
				pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); 
				break;
			}

			if(!checkPetList(pc, item)) break;
			if(!isAvailableWhCount(clanWarehouse, pc, item, count)) break;

			pc.getInventory().tradeItem(objectId, count, clanWarehouse);
			pc.getLight().turnOnOffLight();
			if (item.getEnchantLevel() == 0){
				eva.writeMessage(-9, "����[��]:" + pc.getName() + "[" + pc.getClanname() + "]" + item.getName() + "/" + count +"��");
			} else {
				eva.writeMessage(-9, "����[��]:" + pc.getName() + "[" + pc.getClanname() + "]" + item.getEnchantLevel() + " " + item.getName() + "/" + count +"��");
			}
		}
		clanWarehouse.unlock(pc.getId());
	}

	private void getItemToPrivateWarehouse(L1PcInstance pc, int size) {
		L1ItemInstance item = null;
		PrivateWarehouse warehouse = WarehouseManager.getInstance().getPrivateWarehouse(pc.getAccountName());

		if(warehouse == null) return;

		for (int i = 0, objectId, count ; i < size; i++) {
			objectId = readD();
			count = readD();			
			item = warehouse.getItem(objectId);  
			int item_count_before = item.getCount(); // �Ϲ�â�� �߰�
			int item_count_after = 0; // �Ϲ�â�� �߰�

			if(!isAvailableTrade(pc, objectId, item, count)) break;			
			if (count > item.getCount()) count = item.getCount();
			if(!isAvailablePcWeight(pc, item, count)) break;
			if(!hasAdena(pc)) break;

			// �Ϲ�â�� �߰� ����
			L1ItemInstance pcitem = pc.getInventory().getItem(objectId);
			if(pcitem != null){
				item_count_after = pcitem.getCount();
			}
			LogDwarfIn ldi = new LogDwarfIn();
			ldi.storeLogDwarfIn(pc, item, item_count_before, item_count_after, count);
			// �Ϲ�â�� �߰� ��

			warehouse.tradeItem(item, count, pc.getInventory());
			if (item.getEnchantLevel() == 0){
			eva.writeMessage(-9, "�Ϲ�[ã]:" + pc.getName() + "/"+ item.getName() + "/"+ count + "��");
			} else {
				eva.writeMessage(-9, "�Ϲ�[ã]:" + pc.getName() + "/"+ item.getEnchantLevel() + " "+item.getName() + "/"+ count + "��");
			}
		}
	}

	private void putItemToPrivateWarehouse(L1PcInstance pc, int size) {
		L1Object object = null;
		L1ItemInstance item = null;
		PrivateWarehouse warehouse = WarehouseManager.getInstance().getPrivateWarehouse(pc.getAccountName());
		if(warehouse == null) return;

		for(int i = 0, objectId, count; i < size; i++) {
			objectId = readD();
			count = readD();

			object = pc.getInventory().getItem(objectId);
			item = (L1ItemInstance) object;

			int item_count_before = item.getCount(); // �Ϲ�â�� �߰�
			int item_count_after = 0; // �Ϲ�â�� �߰�

			if (!item.getItem().isTradable()) {
				// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� �絵 �� �� �����ϴ�.
				pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
				break;
			}
			/*		if (item.getItem().getItemId() == 40074 || item.getItem().getItemId() == 40087
		            || item.getItem().getItemId() == 140074 || item.getItem().getItemId() == 140087
		            || item.getItem().getItemId() == 240074 || item.getItem().getItemId() == 240087)  {
		                    pc.sendPackets(new S_SystemMessage("������ ��ȭ�ֹ����� â�� �̿��� �� �� �����ϴ�."));
		             return;} */  
			/*	if (item.getItem().getItemId() == 41159 || item.getItem().getItemId() == 41246 ){ 
				pc.sendPackets(new S_SystemMessage("�ش� �������� â�� �̿��� �� �� �����ϴ�."));
				return;}  */
			/*���׹���*/  

			//			** ������� **//	By �����
			if (pc.getInventory().findItemId(40308).getCount() < 30) {
				pc.sendPackets(new S_SystemMessage("�Ƶ����� �����մϴ�."));
				return;
			}   
			//			����ʻ� �� ������ ���� ���� �� ������ ���� ĳ���� �������̶��
			if(isTwoLogin(pc)) return;			 
			if (objectId != item.getId()) {
				pc.sendPackets(new S_Disconnect());
				break;  }
			if (!item.isStackable() && count != 1) {
				pc.sendPackets(new S_Disconnect());
				break;  }
			/*���׹���*/

			if (item == null || item.getCount() < count  || count <= 0 || item.getCount() <= 0) {
				break;  }
			if (item.getCount() > 1000000000) {
				break;   }
			if (count > 1000000000) {
				break;    }
			if(item == null) break;
			/*	if(pc.getAccessLevel() == Config.GMCODE){
				pc.sendPackets(new S_SystemMessage("��ڴ� â�� �̿� �� �� �����ϴ�."));	
				break; }*/// ��� â�� ��� ����
			if(!isAvailableTrade(pc, objectId, item, count)) break;			
			if(!checkPetList(pc, item)) break;
			if(!isAvailableWhCount(warehouse, pc, item, count)) break;			
			if(count > item.getCount()) count = item.getCount();

			// �Ϲ�â�� �߰� ����
			L1ItemInstance dwitem = pc.getInventory().getItem(objectId);
			if(dwitem != null){
				item_count_after = dwitem.getCount();
			}
			LogDwarfOut ldo = new LogDwarfOut();
			ldo.storeLogDwarfOut(pc, item, item_count_before, item_count_after, count);
			// �Ϲ�â�� �߰� ��

			pc.getInventory().tradeItem(objectId, count, warehouse);					
			pc.getLight().turnOnOffLight();
			if (item.getEnchantLevel() == 0){
			eva.writeMessage(-9,"�Ϲ�[��]:" + pc.getName() + "/"+ item.getName() + "/" + count + "��");
			} else {
				eva.writeMessage(-9,"�Ϲ�[��]:" + pc.getName() + "/"+ item.getEnchantLevel() + " " +item.getName() + "/" + count + "��");
			}
		}
	} 

	private void sellItemToPrivateShop(L1PcInstance pc, L1Object findObject, int size) {
		L1PcInstance targetPc = null;

		if (findObject instanceof L1PcInstance) {
			targetPc = (L1PcInstance) findObject;
			if (targetPc == null) 	
				return;
		}
		if (targetPc.isTradingInPrivateShop()) return;

		targetPc.setTradingInPrivateShop(true);

		L1PrivateShopBuyList psbl;
		L1ItemInstance item = null;
		boolean[] isRemoveFromList = new boolean[8];
		ArrayList<L1PrivateShopBuyList> buyList = targetPc.getBuyList();

		synchronized (buyList) {
			int order, itemObjectId, count, buyPrice, buyTotalCount, buyCount;
			for (int i = 0; i < size; i++) {
				itemObjectId = readD();				
				count = readCH();
				order = readC();

				item = pc.getInventory().getItem(itemObjectId);

				if(!isAvailableTrade(pc, itemObjectId, item, count)) break;				
				if(item.getBless() >= 128){
					// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� �絵 �� �� �����ϴ�.
					pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); 
					break;
				}

				psbl = (L1PrivateShopBuyList) buyList.get(order);
				buyPrice = psbl.getBuyPrice();
				buyTotalCount = psbl.getBuyTotalCount(); // �� ������ ����
				buyCount = psbl.getBuyCount(); // �� ����

				if (count > buyTotalCount - buyCount) 
					count = buyTotalCount - buyCount;

				if (item.isEquipped()) {
					pc.sendPackets(new S_ServerMessage(905)); // ��� �ϰ� �ִ� �������� �Ǹ��� �� �����ϴ�.
					break;
				}

				if(!isAvailablePcWeight(pc, item, count)) break;
				if(isOverMaxAdena(targetPc, buyPrice, count)) return;

				//				���λ��� �κ� ��Ž� ��� //				  
				int itemType = item.getItem().getType2(); 
				/*���׹���*/
				if (itemObjectId != item.getId()) {
					pc.sendPackets(new S_Disconnect());
					targetPc.sendPackets(new S_Disconnect());
					return;
				}
				if (!item.isStackable() && count != 1) {
					pc.sendPackets(new S_Disconnect());
					targetPc.sendPackets(new S_Disconnect());
					return;
				}
				if (count >= item.getCount()) {
					count = item.getCount();
				}
				/*���׹���*/
				if ((itemType == 1 && count != 1) || (itemType == 2 && count != 1)) {
					return;
				}  
				if (item.getCount() <= 0 || count <= 0 || item.getCount() < count) { 
					pc.sendPackets(new S_Disconnect());					
					targetPc.sendPackets(new S_Disconnect());
					return;
				}
				if (buyPrice * count <= 0 || buyPrice * count > 2000000000) {
					return;
				}
				/*	if (item.getItem().getItemId() == 41159) { // �ź���� ����
					return;
				}		*/			
				//** ���λ��� �κ� ��Ž� ��� **//	by �����			
				if (count >= item.getCount()) count = item.getCount();

				if (!targetPc.getInventory().checkItem(L1ItemId.ADENA, count * buyPrice)) {
					targetPc.sendPackets(new S_ServerMessage(189)); // \f1�Ƶ����� �����մϴ�.
					break;
				}

				L1ItemInstance adena = targetPc.getInventory().findItemId(L1ItemId.ADENA);
				if (adena == null) break;

				targetPc.getInventory().tradeItem(adena, count * buyPrice, pc.getInventory());
				pc.getInventory().tradeItem(item, count, targetPc.getInventory());
				psbl.setBuyCount(count + buyCount);
				buyList.set(order, psbl);

				if (psbl.getBuyCount() == psbl.getBuyTotalCount()) { // �� ������ ������ ���
					isRemoveFromList[order] = true;
				}

				try {
					// ���� ����
					pc.save();
					targetPc.save();
					//
					pc.saveInventory();
					targetPc.saveInventory();
				} catch (Exception e) {

				}
			}
			// ������ �������� ����Ʈ�� ���̷κ��� ����
			for (int i = 7; i >= 0; i--) {
				if (isRemoveFromList[i]) {
					buyList.remove(i);
				}
			}  
			targetPc.setTradingInPrivateShop(false);
		}
	}

	private void sellItemToShop(L1PcInstance pc, int npcId, int size) {
		L1Shop shop = ShopTable.getInstance().get(npcId);
		L1ShopSellOrderList orderList = shop.newSellOrderList(pc);
		int itemNumber; long itemcount;

		for (int i = 0; i < size; i++) {
			itemNumber = readD();
			itemcount = readD();
			if(itemcount <= 0){
				return;
			}
			orderList.add(itemNumber, (int)itemcount , pc);	
			if (orderList.BugOk() != 0) {
				pc.sendPackets(new S_Disconnect());
				pc.getNetConnection().kick();
				pc.getNetConnection().close();
			}
		}
		int bugok = orderList.BugOk();
		if (bugok == 0){
			shop.buyItems(orderList);
		}		
	}

	// ���� ���� ���Ǿ� ������ ���� 
	private void buyItemFromNpcShop(L1PcInstance pc, int npcId, int size){
		L1Shop shop = NpcShopTable.getInstance().get(npcId);
		L1ShopBuyOrderList orderList = shop.newBuyOrderList();
		int itemNumber; long itemcount;

		for (int i = 0; i < size; i++) {
			itemNumber = readD();
			itemcount = readD();
			if(itemcount <= 0) {
				return;
			}
			if(size >= 2){ //���ÿ� �ٸ������� ������� 2���� ���õȴٸ�,
				pc.sendPackets(new S_SystemMessage("\\fY�ѹ��� ���� �ٸ��������� �����Ҽ������ϴ�."));
				return;
			}
			if(pc.getMapId() == 360){//���������� ���۸� �����ϰ��߱⶧����, �������忡�� 15���� ���� �̻� �Ȼ�����			
				if(itemcount > 15) {
					pc.sendPackets(new S_SystemMessage("\\fY�ִ뱸�ż��� : ���۷�(15����) / ����(1����)"));
					return;
				}
			}	
			orderList.add(itemNumber, (int)itemcount , pc);	
		}
		int bugok = orderList.BugOk();
		if (bugok == 0){
			shop.sellItems(pc, orderList);
			//�鼷���� ���� ���������׹���
			pc.saveInventory();
			//�鼷���� ���� ���������׹���
		}
	}
	private void buyItemFromPrivateShop(L1PcInstance pc, L1Object findObject, int size) {
		L1PcInstance targetPc = null;
		if (findObject instanceof L1PcInstance) {
			targetPc = (L1PcInstance) findObject;			
			if (targetPc == null) 
				return;
		}
		if (targetPc.isTradingInPrivateShop()) return;

		ArrayList<L1PrivateShopSellList> sellList = targetPc.getSellList();

		synchronized (sellList) {
			// ǰ���� �߻���, �������� �����ۼ��� ����Ʈ���� �ٸ���
			if (pc.getPartnersPrivateShopItemCount() != sellList.size()) return;
			if (pc.getPartnersPrivateShopItemCount() < sellList.size())  return;

			targetPc.setTradingInPrivateShop(true);

			L1ItemInstance item;
			L1PrivateShopSellList pssl;
			boolean[] isRemoveFromList = new boolean [8];
			int order, count, price, sellCount, sellPrice, itemObjectId, sellTotalCount;
			for (int i = 0 ; i < size ; i++) { // ���� ������ ��ǰ
				order = readD();
				count = readD();

				pssl = (L1PrivateShopSellList) sellList.get(order);
				itemObjectId = pssl.getItemObjectId();
				sellPrice = pssl.getSellPrice();
				sellTotalCount = pssl.getSellTotalCount(); // �� ������ ����
				sellCount = pssl.getSellCount(); // �� ����
				item = targetPc.getInventory().getItem(itemObjectId);

				if (item == null) break;
				if (item.isEquipped()) {
					pc.sendPackets(new S_ServerMessage(905, "")); // ��� �ϰ� �ִ� ������ ���� ���ϰ�.
					break;
				}

				if (count > sellTotalCount - sellCount) count = sellTotalCount - sellCount;
				if (count == 0) break;

				if(!isAvailablePcWeight(pc, item, count)) break;
				if(isOverMaxAdena(pc, sellPrice, count)) break; 

				price = count * sellPrice;	 
				if (price <= 0 ||price > 2000000000) break;

				if(!isAvailableTrade(pc, targetPc, itemObjectId, item, count)) break;

				if (count >= item.getCount()) count = item.getCount();	
				if (item.getCount() > 9999) break;

				if (!pc.getInventory().checkItem(L1ItemId.ADENA, price)) {
					pc.sendPackets(new S_ServerMessage(189)); // \f1�Ƶ����� �����մϴ�.
					break;
				}

				L1ItemInstance adena = pc.getInventory().findItemId(L1ItemId.ADENA);

				if (targetPc == null || adena == null) break;
				if (targetPc.getInventory().tradeItem(item, count, pc.getInventory()) == null) break;

				pc.getInventory().tradeItem(adena, price, targetPc.getInventory());

				// %1%o	%0�� �Ǹ��߽��ϴ�.
				String message = item.getItem().getName() + " (" + String.valueOf(count) + ")";
				targetPc.sendPackets(new S_ServerMessage(877, pc.getName(), message));

				pssl.setSellCount(count + sellCount);
				sellList.set(order, pssl);

				writeLogbuyPrivateShop(pc, targetPc, item, count, price);

				if (pssl.getSellCount() == pssl.getSellTotalCount()) // �ش� ���� �� �ȾҴ�
					isRemoveFromList[order] = true;				
				try {

					// ���� ����
					pc.save();
					targetPc.save();
					//
					pc.saveInventory();
					targetPc.saveInventory();
				} catch (Exception e) {}
			}

			// ǰ���� �������� ����Ʈ�� ���̷κ��� ����
			for (int i = 7; i >= 0; i--) {
				if (isRemoveFromList[i]) {
					sellList.remove(i);
				}
			}
			targetPc.setTradingInPrivateShop(false);
		}
	}

	private void buyItemFromShop(L1PcInstance pc, int npcId, int size) {
		L1Shop shop = ShopTable.getInstance().get(npcId);
		L1ShopBuyOrderList orderList = shop.newBuyOrderList();
		int itemNumber; long itemcount;
		if (shop.getSellingItems().size() < size) {
			System.out.println("������ �Ǹ��ϴ� ������ ��(" + shop.getSellingItems().size() + ")���� �� ���� ����� ��.("+size+")��");
			pc.getNetConnection().kick();
			pc.getNetConnection().close();
			return;
		}
		if(size > 1000){
			return;
		}
		for (int i = 0; i < size; i++) {
			itemNumber = readD();
			itemcount = readD();
			if(itemcount <= 0 || itemcount >= 10000) {
				return;
			}
			orderList.add(itemNumber, (int)itemcount , pc);	
			if (orderList.BugOk() != 0) {
				pc.sendPackets(new S_Disconnect());
				pc.getNetConnection().kick();
				pc.getNetConnection().close();
				return;
			}
		}
		int bugok = orderList.BugOk();
		if (bugok == 0){
			shop.sellItems(pc, orderList);
		}
	}

	/**
	 * ����� �ִ� ��� ĳ���� ������ ���� ���� ������ �ִٸ� true ���ٸ� false
	 * @param c L1PcInstance
	 * @return �ִٸ� true
	 */
	private boolean isTwoLogin(L1PcInstance c) {
		boolean bool = false;
		for(L1PcInstance target : L1World.getInstance().getAllPlayersToArray()){
			// ����PC �� ����
			if(target.noPlayerCK) continue;
			//
			if(c.getId() != target.getId() && !target.isPrivateShop()){
				if(c.getNetConnection().getAccountName().equalsIgnoreCase(target.getNetConnection().getAccountName())) {
					bool = true;
					break;
				}
			}
		}
		return bool;
	}

	private void writeLogbuyPrivateShop(L1PcInstance pc, L1PcInstance targetPc, L1ItemInstance item, int count, int price) {
		String itemadena = item.getName()+"("+price+")";
		eva.writeMessage(-9, "[����]" + pc.getName() +"/" + targetPc.getName() + "/" + item.getEnchantLevel() 
				+"/" + itemadena + "/" + item.getBless() + "/" + count + "/" + item.getId());
	}

	private boolean isOverMaxAdena(L1PcInstance pc, int sellPrice, int count) {
		if (sellPrice * count > 2000000000
				||sellPrice * count < 0) {
			pc.sendPackets(new S_ServerMessage(904, "2000000000"));
			return true;
		}
		if(count < 0){
			return true;
		}
		if(sellPrice < 0){
			return true;
		}
		return false;
	}
	private boolean checkPetList(L1PcInstance pc, L1ItemInstance item) {
		L1DollInstance doll = null;
		Object[] dollList = pc.getDollList().values().toArray();
		for (Object dollObject : dollList) {
			doll = (L1DollInstance) dollObject;
			if (item.getId() == doll.getItemObjId()) {
				pc.sendPackets(new S_ServerMessage(1181)); // 
				return false;
			}
		}
		Object[] petlist = pc.getPetList().values().toArray();
		for (Object petObject : petlist) {
			if (petObject instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) petObject;
				if (item.getId() == pet.getItemObjId()) {
					// \f1%0�� �����ų� �Ǵ� Ÿ�ο��� �絵 �� �� �����ϴ�.
					pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
					return false;
				}
			}
		}
		return true;
	}

	private boolean isAvailableWhCount(Warehouse warehouse, L1PcInstance pc, L1ItemInstance item, int count) {
		if (warehouse.checkAddItemToWarehouse(item, count) == L1Inventory.SIZE_OVER) {
			// \f1��밡 ������ �ʹ� ������ �־� �ŷ��� �� �����ϴ�.
			pc.sendPackets(new S_ServerMessage(75));
			return false;
		}
		return true;
	}

	private boolean isAvailableClan(L1PcInstance pc, L1Clan clan) {
		if (pc.getClanid() == 0 || clan == null) {
			// \f1���� â�� ����Ϸ���  ���Ϳ� �������� ������ �ȵ˴ϴ�.
			pc.sendPackets(new S_ServerMessage(208));
			return false;
		}
		return true;
	}

	private boolean isAvailablePcWeight(L1PcInstance pc, L1ItemInstance item, int count) {
		if (pc.getInventory().checkAddItem(item, count) != L1Inventory.OK) {
			// \f1 ������ �ִ� ���� ���ſ��� �ŷ��� �� �����ϴ�.
			pc.sendPackets(new S_ServerMessage(270));
			return false;
		}
		return true;
	}

	private boolean hasAdena(L1PcInstance pc) {
		if (!pc.getInventory().consumeItem(L1ItemId.ADENA, 30)) {
			// \f1�Ƶ����� �����մϴ�.
			pc.sendPackets(new S_ServerMessage(189)); 
			return false;
		}
		return true;
	}

	private boolean isAvailableTrade(L1PcInstance pc, int objectId, L1ItemInstance item, int count) {
		boolean result = true;

		if(item == null)											result = false; 
		if(objectId != item.getId())								result = false;
		if(!item.isStackable() && count != 1)						result = false;
		if(item.getCount() <= 0 || item.getCount() > 2000000000)	result = false;
		if(count <= 0 ||count > 2000000000)							result = false;

		if(!result) {
			pc.sendPackets(new S_Disconnect());
		}

		return result;
	}

	private boolean isAvailableTrade(L1PcInstance pc, L1PcInstance targetPc, int itemObjectId, L1ItemInstance item, int count) {
		boolean result = true;

		if (itemObjectId != item.getId() || !item.isStackable() && count != 1 ) 	result = false;
		if (count <= 0 || item.getCount() <= 0 || item.getCount() < count) 			result = false;

		if(!result) {
			pc.sendPackets(new S_Disconnect());
			targetPc.sendPackets(new S_Disconnect());
		}

		return result;
	}

	@Override
	public String getType() {
		return "[C] C_Result";
	}
}