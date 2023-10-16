package l1j.server.GameSystem.Boss;

import java.util.Calendar;
import java.util.Random;

import l1j.server.server.datatables.BossSpawnTable;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.BaseTime;
import l1j.server.server.model.gametime.RealTime;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.gametime.TimeListener;
//import l1j.server.swing.Manager;
import l1j.server.server.serverpackets.S_SystemMessage;

public class BossSpawnTimeController implements TimeListener{
	private static BossSpawnTimeController _instance;
	
	public static void start() {
		if (_instance == null) {
			_instance = new BossSpawnTimeController();
		}
		_instance.init();
		RealTimeClock.getInstance().addListener(_instance);
	}
		
	private void init() {
		RealTime time = RealTimeClock.getInstance().getRealTime();
		for(L1BossCycle b : L1BossCycle.getBossCycleList()) {
			if(b.getBaseDay() == 0)
				b.setBaseDay(time.get(Calendar.DAY_OF_MONTH));

			setBaseTime(b, time);
			setNextBossSpawnTime(b);
		}
	}
	
	private void setBaseTime(L1BossCycle b, RealTime time) {
		final int H = time.get(Calendar.HOUR_OF_DAY);
		final int M = time.get(Calendar.MINUTE);
		boolean isOverDay = false; 
		
		for(;b.getBaseHour() < H && !isOverDay;) {
			int nM = (b.getBaseMinute() + b.getPeriodMinute());
			int nH = (b.getBaseHour() + b.getPeriodHour() + (nM / 60));
			
			if((nH / 24) >= 1) isOverDay = true;
			
			b.setBaseHour(nH % 24);
			b.setBaseMinute(nM % 60);
		}
		
		for(; b.getBaseHour() == H && b.getBaseMinute() <= M && b.getPeriodMinute() > 0;) {
			int plusM = b.getBaseMinute() + b.getPeriodMinute();
			if(plusM < 60)		b.setBaseMinute(plusM);
			else				b.setBaseHour(b.getBaseHour() + 1);
		}
		
		setNextBossSpawnTime(b);
	}

	private void setNextBossSpawnTime(L1BossCycle b) {	
		Random rnd = new Random(System.nanoTime());
		int newH = 0, newM = 0;
		newH = b.getBaseHour() + b.getStartHour();
		newM = b.getBaseMinute() + b.getStartMinute();
		//System.out.println("Name : " +b.getName());
		//System.out.println("bh/sh/eh = "+b.getBaseHour()+"/"+b.getStartHour()+"/"+b.getEndHour());
		//System.out.println("bm/sm/em = "+b.getBaseMinute()+"/"+b.getStartMinute()+"/"+b.getEndMinute());
		int eH = b.getEndHour();
		int eM = b.getEndMinute();
		int rndr = (eH*60+eM)-(b.getStartHour()*60+b.getStartMinute());
		rndr = rndr-(rndr/4);
		int rndM = 0;
		// 뜨는 시간 랜덤 설정
		//if(eH > 0)	rndM = rnd.nextInt((eH*60));
		//if(eM > 0)	rndM += rnd.nextInt(eM);
		if(rndr>0) rndM = rnd.nextInt(rndr);
		else rndM = rnd.nextInt(10);
		//System.out.println("eH/eM/rndM = "+eH+"/"+eM+"/"+rndM);
		newH += rndM / 60;
		newM += rndM % 60;
		
		newH += newM / 60;
		newM %= 60;
		newH %= 24;
		//System.out.println("newH/newM = "+newH+"/"+newM);
		b.setKillHour(b.getNewKillHour());
		b.setKillMinute(b.getNewKillMinute());
		
		int kM = b.getBaseMinute() + b.getEndMinute();
		int kH = b.getBaseHour() + b.getEndHour() + (kM / 60);
		kM %= 60;
		kH %= 24;
		
		b.setNewKillHour(kH);
		b.setNewKillMinute(kM);
		
		int plusH = (b.getBaseMinute() + b.getPeriodMinute()) / 60;
		b.setBaseMinute((b.getBaseMinute() + b.getPeriodMinute()) % 60);
		b.setBaseHour((b.getBaseHour() + b.getPeriodHour() + plusH) % 24); 
		//System.out.println(newH+"/"+newM);
		// 새로운 스폰 시간 설정
		b.setNextSpawnHour(newH);
		b.setNextSpawnMinute(newM);
		
	}

	@Override
	public void onDayChanged(BaseTime time) {}

	@Override
	public void onHourChanged(BaseTime time) {}

	@Override
	public void onMinuteChanged(BaseTime time) {
	//	System.out.println(time.get(Calendar.MINUTE)+ "분, 처리 시작");
		final int H = time.get(Calendar.HOUR_OF_DAY);
		final int M = time.get(Calendar.MINUTE);
		int sH, sM;
		for(L1BossCycle b : L1BossCycle.getBossCycleList()) {
			if(H == b.getKillHour() && M == b.getKillMinute()) {
				//Manager.textAreaServer.append("\n["+time.get(Calendar.HOUR)+"시"+time.get(Calendar.MINUTE)+"분] "+b.getName() + "- 보스 타임 종료");
				BossSpawnTable.killBoss(b.getName());
			}
			
			sH = b.getNextSpawnHour();
			sM = b.getNextSpawnMinute();
			
			if(sH == H && sM == M) {
				//Manager.textAreaServer.append("\n["+time.get(Calendar.HOUR)+"시"+time.get(Calendar.MINUTE)+"분] "+b.getName() + "- 보스 타임 시작");
				BossSpawnTable.spawnBoss(b.getName()); // 스폰				
				setNextBossSpawnTime(b); // 스폰 타임 재설정
			}
		}
	//	System.out.println(time.get(Calendar.MINUTE)+ "분, 처리 끝");
	}

	@Override
	public void onMonthChanged(BaseTime time) {}
	
}
