package l1j.server.server;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SystemMessage;

public class CrockController extends Thread {

	private static CrockController _instance;

	private boolean _CrockStart;
	public boolean getCrockStart() {
		return _CrockStart;
	}
	public void setCrockStart(boolean Crock) {
		_CrockStart = Crock;
	}
	private static long sTime = 0;

	private String NowTime = "";

	public int LOOP = 0;

	private static final SimpleDateFormat s = new SimpleDateFormat("HH", Locale.KOREA);

	private static final SimpleDateFormat ss = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

	public static CrockController getInstance() {
		if(_instance == null) {
			_instance = new CrockController();
		}
		return _instance;
	}

	public CrockController(){
		LOOP = 4;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(1000); 
				/** 오픈 **/
				if(!isOpen())
					continue;
				if(L1World.getInstance().getAllPlayers().size() <= 0)
					continue;

				/** 오픈 메세지 **/
				L1World.getInstance().broadcastServerMessage("\\fW테베 & 티칼의 시간의 균열이 열렸습니다..");
				L1World.getInstance().broadcastServerMessage("\\fW입장하실분들은 주문서를 이용해주세요.");
				L1World.getInstance().broadcastPacketToAll(	new S_PacketBox(S_PacketBox.GREEN_MESSAGE,"테베 & 티칼의 시간의 균열이 개방 되었습니다."));

				/**시작**/
				setCrockStart(true);

				/** 실행 2시간 시작**/

				Thread.sleep(7300000L); //2시간 10분정도

				/** 2시간 후 자동 텔레포트**/
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


	private boolean isOpen() {
		NowTime = getTime();
		if((Integer.parseInt(NowTime) % LOOP) == 0) return true;
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
			case 780: case 781: case 782: case 783:case 784:
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33430, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("시간의 균열이 소멸됩니다."));
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
			case 780: case 781: case 782: case 783:case 784:
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33430, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("시간의 균열이 소멸됩니다."));
				break;
			default:
				break;
			}
		}
	}

	/** 종료 **/
	private void End() {
		L1World.getInstance().broadcastServerMessage("시간의 균열이 소멸되었습니다.");
		L1World.getInstance().broadcastPacketToAll(	new S_PacketBox(S_PacketBox.GREEN_MESSAGE,"테베 & 티칼의 시간의 균열이 닫혔습니다."));
		setCrockStart(false);
	}
}