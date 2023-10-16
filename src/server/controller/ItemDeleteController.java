package server.controller;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;

public class ItemDeleteController extends TimerTask{
	private static Logger _log = Logger.getLogger(ItemDeleteController.class.getName());
	private static Timer _itemDeleteController = new Timer();
	private final int _DeleteTime;
	
	public ItemDeleteController(int deleteTime) {
		_DeleteTime = deleteTime;
	}
	public void start() {
		_itemDeleteController.scheduleAtFixedRate(ItemDeleteController.this, 0, _DeleteTime);
	}
	@Override
	public void run(){
		try {
	
			int numOfDeleted = 0;
			L1Inventory groundInventory = null;
			Collection<L1Object> objs = L1World.getInstance().getObject();
			for (L1Object obj1 : objs) {
				if(obj1 instanceof L1ItemInstance){
					L1ItemInstance obj = (L1ItemInstance)obj1;
					if (obj.getX() == 0 && obj.getY() == 0) { // ������� �������� �ƴϰ�, �������� ������
						continue;
					}
					if (obj.getItem().getItemId() == 40515) { // ������ ��
						continue;
					}
					if (obj.getMapId() == 88 || obj.getMapId() == 98 || obj.getMapId() == 91 || obj.getMapId() == 92 || obj.getMapId() == 95) { // ���Ѵ���
						continue;
					}
					if (L1HouseLocation.isInHouse(obj.getX(), obj.getY(), obj
							. getMapId())) { // ����Ʈ��
						continue;
					}

					//players = L1World.getInstance()
					//		.getVisiblePlayer(obj, Config.ALT_ITEM_DELETION_RANGE);
					if(obj.get_DeleteItemTime() > 50){
						groundInventory = L1World
						. getInstance()
						. getInventory(obj.getX(), obj.getY(), obj.getMapId());
						groundInventory.removeItem(obj);
						numOfDeleted++;
					}
					else{
						obj.add_DeleteItemTime();
					}
					/*if (players.isEmpty()) { // ���� �������� �÷��̾ ������ ����
						groundInventory = L1World
								. getInstance()
								. getInventory(obj.getX(), obj.getY(), obj.getMapId());
						groundInventory.removeItem(obj);
						numOfDeleted++;
					}*/			
				}			
			}
			objs = null;
			_log.fine("���� �ʻ��� �������� �ڵ� ����. ������: " + numOfDeleted);
		}
		catch(Exception e){
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
	}
}
