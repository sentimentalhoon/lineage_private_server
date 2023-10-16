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
package l1j.server.server.model;

import java.util.List;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.UBTable;
import l1j.server.server.model.L1UltimateBattle;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import java.util.Calendar;
// Referenced classes of package l1j.server.server.model:
// L1DeleteItemOnGround

public class L1DeleteItemOnGround {
 private DeleteTimer _deleteTimer;

 private static final Logger _log = Logger
   .getLogger(L1DeleteItemOnGround.class.getName());

 public L1DeleteItemOnGround() {
 }

 private class DeleteTimer implements Runnable {
  public DeleteTimer() {
  }

  @Override
  public void run() {
   int time = Config.ALT_ITEM_DELETION_TIME * 60 * 1000 - 10 * 1000;
   for (;;) {
    try {
     Thread.sleep(time);
    } catch (Exception exception) {
     _log.warning("L1DeleteItemOnGround error: " + exception);
     break;
    }
    L1World.getInstance().broadcastPacketToAll(
    new S_SystemMessage("\\fA���� �ʻ��� �������� 30�� �Ŀ� �����˴ϴ�.")); 
    try {
     Thread.sleep(30000);
    } catch (Exception exception) {
     _log.warning("L1DeleteItemOnGround error: " + exception);
     break;
    }
    deleteItem();
    //#################### ���� ���� �߰� #################
       ����ð�();
       L1World.getInstance().broadcastPacketToAll(
         new S_SystemMessage("\\fA���� �ʻ��� �������� ���� �Ǿ����ϴ�.")); 
       // %2
      try {    
       Thread.sleep(50000); // ����� ���� ��Ʈ�� �ð� ���� 50000�̸� 50�� �Ŀ� �̸޼����� ��
        } catch (Exception exception) {
             _log.warning("L1DeleteItemOnGround error: " + exception);
             break;
          }
        deleteItem();
       L1World.getInstance().broadcastPacketToAll(
         new S_SystemMessage(""));                                                                                                        
                   }
                       }
                       }
           private void ����ð�(){ 
           Calendar calender;     
           calender = Calendar.getInstance(); //�޷� Ŭ������ �ν��Ͻ��� ���� 
           int hour, minute, sec, ampm;//�ð�,��, ��, ���������� ���� ���� �� �ʱ�ȭ..
           hour = calender.get(Calendar.HOUR);  
           minute = calender.get(Calendar.MINUTE);
           sec = calender.get(Calendar.SECOND);
           ampm = calender.get(Calendar.AM_PM);
           String ampmm = ""; 

           if(0 == ampm){ //0�ϰ�� ����
           ampmm = "����";                    
         }else if(1 == ampm) {// 1�� ��� ���� 
           ampmm = "����";
          }
      // ���� ���� ���� �������� �ð� �ѷ��� ����...
           //L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fTSERVER �˸� : ����ð��� " +ampmm+" " + hour +"�� " + minute + "�� " + sec+ "�� �Դϴ�." ));
 }

 public void initialize() {
  if (! Config.ALT_ITEM_DELETION_TYPE.equalsIgnoreCase("auto")) {
   return;
  }

   _deleteTimer = new DeleteTimer();
   GeneralThreadPool.getInstance().execute(_deleteTimer); // Ÿ�̸� ����
  }

 private void deleteItem() {
  int numOfDeleted = 0;
  for (L1Object obj : L1World.getInstance(). getObject()) {
   if (! (obj instanceof L1ItemInstance)) {
    continue;
   }

   L1ItemInstance item = (L1ItemInstance) obj;
    if (item.getX() == 0 && item.getY() == 0) { // ������� �������� �ƴϰ�, �������� ������
     continue;
    }
    if (item.getItem().getItemId() == 40515) { // ������ ��
     continue;
    }
    if (L1HouseLocation.isInHouse(item.getX(), item.getY(), item
      .getMapId())) { // ����Ʈ��
     continue;
    }
				//���Ѵ����� ������� ������ �Ȼ������ by �ƽ�����
   if (item.getMapId() >= 88 && item.getMapId() <= 98){
    for (L1UltimateBattle ub : UBTable.getInstance().getAllUb()) {
     switch (ub.getUbId()) {
     case 1: // ���
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 2: // ����
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 3: // �۸�
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 4: // ����
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 5: // ����
      if (ub.isNowUb()){
       continue; 
      }
      break;     
     }  
    }
   }

    List<L1PcInstance> players = L1World.getInstance()
     . getVisiblePlayer(item, Config.ALT_ITEM_DELETION_RANGE);
   if (players.isEmpty()) { // ���� �������� �÷��̾ ������ ����
    L1Inventory groundInventory = L1World
      . getInstance()
      . getInventory(item.getX(), item.getY(), item.getMapId());
    groundInventory.removeItem(item);
    numOfDeleted++;
   }
  }
  _log.fine("���� �ʻ��� �������� �ڵ� ����. ������: " + numOfDeleted);
 }
}
