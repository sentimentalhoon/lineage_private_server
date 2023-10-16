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
    new S_SystemMessage("\\fA월드 맵상의 아이템이 30초 후에 삭제됩니다.")); 
    try {
     Thread.sleep(30000);
    } catch (Exception exception) {
     _log.warning("L1DeleteItemOnGround error: " + exception);
     break;
    }
    deleteItem();
    //#################### 여기 부터 추가 #################
       현재시간();
       L1World.getInstance().broadcastPacketToAll(
         new S_SystemMessage("\\fA월드 맵상의 아이템이 삭제 되었습니다.")); 
       // %2
      try {    
       Thread.sleep(50000); // 월드맵 삭제 멘트후 시간 설정 50000이면 50초 후에 이메세지가 뜸
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
           private void 현재시간(){ 
           Calendar calender;     
           calender = Calendar.getInstance(); //달력 클래스의 인스턴스를 생성 
           int hour, minute, sec, ampm;//시간,분, 초, 오전오후의 변수 선언 및 초기화..
           hour = calender.get(Calendar.HOUR);  
           minute = calender.get(Calendar.MINUTE);
           sec = calender.get(Calendar.SECOND);
           ampm = calender.get(Calendar.AM_PM);
           String ampmm = ""; 

           if(0 == ampm){ //0일경우 오전
           ampmm = "오전";                    
         }else if(1 == ampm) {// 1일 경우 오후 
           ampmm = "오후";
          }
      // 실제 접속 중인 유저에게 시간 뿌려줘 벌장...
           //L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fTSERVER 알림 : 현재시간은 " +ampmm+" " + hour +"시 " + minute + "분 " + sec+ "초 입니다." ));
 }

 public void initialize() {
  if (! Config.ALT_ITEM_DELETION_TYPE.equalsIgnoreCase("auto")) {
   return;
  }

   _deleteTimer = new DeleteTimer();
   GeneralThreadPool.getInstance().execute(_deleteTimer); // 타이머 개시
  }

 private void deleteItem() {
  int numOfDeleted = 0;
  for (L1Object obj : L1World.getInstance(). getObject()) {
   if (! (obj instanceof L1ItemInstance)) {
    continue;
   }

   L1ItemInstance item = (L1ItemInstance) obj;
    if (item.getX() == 0 && item.getY() == 0) { // 지면상의 아이템은 아니고, 누군가의 소유물
     continue;
    }
    if (item.getItem().getItemId() == 40515) { // 정령의 돌
     continue;
    }
    if (L1HouseLocation.isInHouse(item.getX(), item.getY(), item
      .getMapId())) { // 아지트내
     continue;
    }
				//무한대전시 대전장안 아이템 안사라지게 by 아스라이
   if (item.getMapId() >= 88 && item.getMapId() <= 98){
    for (L1UltimateBattle ub : UBTable.getInstance().getAllUb()) {
     switch (ub.getUbId()) {
     case 1: // 기란
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 2: // 웰던
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 3: // 글말
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 4: // 말섬
      if (ub.isNowUb()){
       continue; 
      }
      break;
     case 5: // 은말
      if (ub.isNowUb()){
       continue; 
      }
      break;     
     }  
    }
   }

    List<L1PcInstance> players = L1World.getInstance()
     . getVisiblePlayer(item, Config.ALT_ITEM_DELETION_RANGE);
   if (players.isEmpty()) { // 지정 범위내에 플레이어가 없으면 삭제
    L1Inventory groundInventory = L1World
      . getInstance()
      . getInventory(item.getX(), item.getY(), item.getMapId());
    groundInventory.removeItem(item);
    numOfDeleted++;
   }
  }
  _log.fine("월드 맵상의 아이템을 자동 삭제. 삭제수: " + numOfDeleted);
 }
}
