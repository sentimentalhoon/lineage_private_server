package l1j.server.GameSystem;

import java.util.Calendar;

import l1j.server.server.datatables.CastleTable;
import l1j.server.server.model.gametime.BaseTime;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.gametime.TimeListener;
import l1j.server.server.templates.L1Castle;

public class WarSetTime implements TimeListener{
	private static WarSetTime _instance;
	
	public static void start() {
		if (_instance == null) {
			_instance = new WarSetTime();
		}
		_instance.some();
		RealTimeClock.getInstance().addListener(_instance);
	}

	private L1Castle[] _castle = new L1Castle[8];
	private int[] _warBaseTime = new int[8];

	private void some() {
		for (int i = 0; i < _castle.length; i++) {
			_castle[i] = CastleTable.getInstance().getCastleTable(i + 1);
			_warBaseTime[i] = _castle[i].getWarBaseTime();
		}
	}

	@Override
	public void onDayChanged(BaseTime time) {}

	@Override
	public void onHourChanged(BaseTime time) {}

	@Override
	public void onMinuteChanged(BaseTime time) {
		int rm = time.get(Calendar.MINUTE);
		int rt = time.getSeconds();
//		System.out.println("�и��� �� ���� �ϴ���.." + rm);
//		System.out.println("�ʴ� �� �о� ������.." + rt);
		if (rm%10 != 0) return;
		for (int i = 0; i < _castle.length; i++) {
			int wbt = _castle[i].getWarBaseTime() + 7200;
			if(_castle[i].getWarBaseTime() != 0) {
				if (wbt < rt) {
					_castle[i].setWarBaseTime(0);
					CastleTable.getInstance().updateCastle(_castle[i]);
				}
			}
		}
	}

	@Override
	public void onMonthChanged(BaseTime time) {}
}
