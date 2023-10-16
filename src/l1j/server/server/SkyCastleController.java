package l1j.server.server;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class SkyCastleController extends Thread {

	private static SkyCastleController _instance;

	private boolean _SkyStart;
	public boolean getSkyStart() {
		return _SkyStart;
	}
	public void setSkyStart(boolean Sky) {
		_SkyStart = Sky;
	}
	private static long sTime = 0;

	private String NowTime = "";
	//시간 간격
	public int LOOP = 0;//하루 1번 공성

	public boolean isGmOpen = false;

	private static final SimpleDateFormat s = new SimpleDateFormat("HH", Locale.KOREA);

	private static final SimpleDateFormat ss = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

	public static SkyCastleController getInstance() {
		if(_instance == null) {
			_instance = new SkyCastleController();
		}
		return _instance;
	}
	public SkyCastleController(){
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
				L1World.getInstance().broadcastServerMessage("\\fW잠시후, 하늘의 성 NPC VS 유저 공성전이 시작됩니다!");

				/** 공성전 시작**/
				setSkyStart(true);

				/** 실행 60분 시작**/

				Thread.sleep(3800000L); //60분정도

				/** 60분 후 자동 텔레포트**/
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
	 *맵이 열려있는지 확인
	 *
	 *@return (boolean) 열려있다면 true 닫혀있다면 false
	 */
	private boolean isOpen() {
		NowTime = getTime();
		//if((Integer.parseInt(NowTime) % LOOP) == 0) return true;
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
			case 18:
			case 630:
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33442, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("하늘의 성 공성전이 끝났습니다."));
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
			case 18: //듀펠개넌던전
			case 630: //하늘의성
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33442, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("하늘의 성 공성전이 끝났습니다."));
				break;
			default:
				break;
			}
		}
	}

	/** 종료 **/
	private void End() {
		L1World.getInstance().broadcastServerMessage("\\fW하늘의 성 공성전이 끝났습니다. 수고 많으셨습니다.");
		setSkyStart(false);
	}
}

