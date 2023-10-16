package l1j.server.GameSystem;

import java.util.Calendar;

import l1j.server.Config;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.L1World;
import l1j.server.server.model.gametime.BaseTime;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.gametime.TimeListener;
import l1j.server.server.serverpackets.S_SystemMessage;

public class NoticeSystem implements TimeListener{
	private static NoticeSystem _instance;
	
	public static void start() {
		if (_instance == null) {
			_instance = new NoticeSystem();
		}
		_instance.some();
		RealTimeClock.getInstance().addListener(_instance);
	}

	private void some() {}

	private final int ubMsg = 1;
	
	static class NoticeTimer implements Runnable {
		private int _type = 0;
		private String _msg = null;
		public NoticeTimer(int type, String MSG) {
			_type = type;
			_msg = MSG;
		}

		@Override
		public void run() {
			try {
				switch (_type) {
				case 1:
					L1World.getInstance().set_worldChatElabled(false);
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fS잠시후 기란 콜롯세움에서 무한대전이 "));
					Thread.sleep(1000);
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fS진행되오니 많은 참여 바랍니다."));
					Thread.sleep(1000);
					L1World.getInstance().set_worldChatElabled(true);
					break;
				case 2:
					L1World.getInstance().set_worldChatElabled(false);
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("이곳은 본섭지향 명문 " + Config.servername + "서버입니다."));
					Thread.sleep(1000);
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("문의/상담은 [메티스] 편지로만 받습니다."));
					Thread.sleep(1000);
					L1World.getInstance().set_worldChatElabled(true);
					break;
					default : break;
				}
			} catch (Exception exception) {}
		}
	}


	@Override
	public void onDayChanged(BaseTime time) {}
	@Override
	public void onHourChanged(BaseTime time) {}
	
	@Override	
	public void onMinuteChanged(BaseTime time) {
		int rm = time.get(Calendar.MINUTE);
		int rh = time.get(Calendar.HOUR_OF_DAY);
		
		if (rm == 55) ubStartMSG(rh);
		
		
	}

	@Override
	public void onMonthChanged(BaseTime time) {}
		
	private void ubStartMSG(int hour) {
		String MSG = null;

		switch (hour) {
		case 1:
		case 5:
		case 9:
		case 13:
		case 17:
		case 21: MSG = "$1242"; break;
		default : return;
		}
		NoticeTimer nt = new NoticeTimer(ubMsg, MSG);
		GeneralThreadPool.getInstance().execute(nt);
	}

}
