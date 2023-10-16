
package l1j.server.server;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class BIRTHDAYController extends Thread {
 
  private static BIRTHDAYController _instance;

  private boolean _BIRTHDAYStart;
  public boolean getBIRTHDAYStart() {
   return _BIRTHDAYStart;
  }
  public void setBIRTHDAYStart(boolean BIRTHDAY) {
   _BIRTHDAYStart = BIRTHDAY;
  }
  private static long sTime = 0;
  
  private String NowTime = "";
  //시간 간격
  public int LOOP = 0;
  
  public boolean isGmOpen = false;

  private static final SimpleDateFormat s = new SimpleDateFormat("HH", Locale.KOREA);

  private static final SimpleDateFormat ss = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

  public static BIRTHDAYController getInstance() {
   if(_instance == null) {
    _instance = new BIRTHDAYController();
   }
   return _instance;
  }

  public BIRTHDAYController(){
		LOOP = 24;
	}

  @Override
   public void run() {
   try {
     while (true) {
      Thread.sleep(1000); 
      /** 오픈 **/
      if(!isOpen() && !isGmOpen)
       continue;
      if(L1World.getInstance().getAllPlayers().size() <= 0)
       continue;
      
      isGmOpen = false;

      /** 오픈 메세지 **/
      L1World.getInstance().broadcastServerMessage("메티스의 놀이터가 개방 되었습니다.");

      /** 놀이터 시작**/
      setBIRTHDAYStart(true);

      /** 실행 1시간 시작**/

      Thread.sleep(3800000L); //3800000L 1시간 10분정도

      /** 1시간 후 자동 텔레포트**/
      TelePort();
      Thread.sleep(5000L);
      TelePort2();
      
      /** 종료 **/
      End();
     }

    } catch(Exception e){
     e.printStackTrace();
    }
   }

   /**
    *오픈 시각을 가져온다
    *
    *@return (Strind) 오픈 시각(MM-dd HH:mm)
    */
    public String OpenTime() {
     Calendar c = Calendar.getInstance();
     c.setTimeInMillis(sTime);
     return ss.format(c.getTime());
    }

    /**
    *놀이터가 열려있는지 확인
    *
    *@return (boolean) 열려있다면 true 닫혀있다면 false
    */
    private boolean isOpen() {
     NowTime = getTime();
   //  if((Integer.parseInt(NowTime) % LOOP) == 0) return true;
     return false;
    }
    /**
    *실제 현재시각을 가져온다
    *
    *@return (String) 현재 시각(HH:mm)
    */
    private String getTime() {
     return s.format(Calendar.getInstance().getTime());
    }

    /**기란마을로 팅기게**/
    private void TelePort() {
     for(L1PcInstance c : L1World.getInstance().getAllPlayers()) {
      switch(c.getMap().getId()) {
       case 2006:
       c.stopHpRegenerationByDoll();
       c.stopMpRegenerationByDoll();
       L1Teleport.teleport(c, 33437, 32798, (short) 4, 4, true);
       c.sendPackets(new S_SystemMessage("즐거우셨나요~? 다음에 또 뵈요~~!"));
       break;
       default:
       break;
      }
     }
    }
    
    /**기란마을로 팅기게**/
    private void TelePort2() {
     for(L1PcInstance c : L1World.getInstance().getAllPlayers()) {
      switch(c.getMap().getId()) {
       case 2006:
       c.stopHpRegenerationByDoll();
       c.stopMpRegenerationByDoll();
       L1Teleport.teleport(c, 33437, 32798, (short) 4, 4, true);
       c.sendPackets(new S_SystemMessage("즐거우셨나요~? 다음에 또 뵈요~~!"));
       break;
       default:
       break;
      }
     }
    }
    
    /** 종료 **/
    private void End() {
     setBIRTHDAYStart(false);
    }
    
}