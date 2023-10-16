package l1j.server.server.utils;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;

public class CommonUtil {
	/**
	 * 2011.08.05 금액표시
	 * @param number
	 * @return
	 */
	public static String numberFormat(int number) {
		try {
			NumberFormat nf = NumberFormat.getInstance();
			return nf.format(number);
		} catch (Exception e) {
			return Integer.toString(number);
		}
	}

	/** 
	 * 2011.08.05 랜덤함수
	 * @param number
	 * @return
	 */
	public static int random(int number) {
		Random rnd = new Random();
		return rnd.nextInt(number);
	}

	/**
	 * 2011.08.05 랜덤함수
	 * @param lbound
	 * @param ubound
	 * @return
	 */
	public static int random(int lbound, int ubound) {
		return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
	}

	/**
	 * 2011.08.30 데이터포맷
	 * @param type
	 * @return
	 */
	public static String dateFormat(String type) {
		SimpleDateFormat sdf = new SimpleDateFormat(type, Locale.KOREA);
		return sdf.format(Calendar.getInstance().getTime());
	}

	/**
	 * 2011.08.30 데이터포맷
	 * @param type
	 * @return
	 */
	public static String dateFormat(String type, Timestamp date) {
		SimpleDateFormat sdf = new SimpleDateFormat(type, Locale.KOREA);
		return sdf.format(date.getTime());
	}

	/**
	 * 2011.08.31 아이템 종료 시간
	 * @param item
	 * @param minute
	 */
	public static void SetTodayDeleteTime(L1ItemInstance item, int minute) {
		Timestamp deleteTime = null;
		deleteTime = new Timestamp(System.currentTimeMillis() + (60000 * minute));
		item.setEndTime(deleteTime);
	}	

	/**
	 * 2011.08.31 아이템 종료 시간 지정(오늘 남은 시간 계산) - 로또 시스템
	 * @param item
	 */
	public static void SetTodayDeleteTime(L1ItemInstance item) {
		int hour = Integer.parseInt(dateFormat("HH"));
		int minute = Integer.parseInt(dateFormat("mm"));
		int time = 0;

		if (hour <= 22 && minute < 30) {
			time = (23 - hour) * 60 + (60 - minute) - 60;
		} else {
			time = (23 - hour) * 60 + (60 - minute) - 60 + (24 * 60);
		}

		Timestamp deleteTime = null;

		deleteTime = new Timestamp(System.currentTimeMillis() + (60000 * time));
		item.setEndTime(deleteTime);
	}	

	/**
	 * 2011.08.31 지정시간까지 남은 시간
	 * @param item
	 */
	public static int getRestTime(int hh) {		
		int hour = Integer.parseInt(dateFormat("HH"));
		int minute = Integer.parseInt(dateFormat("mm"));

		int time = 0;

		time = (hh - hour) * 60 - minute;	

		return time;

	}
	/**
	 * 2012.04.13. 무작위 텔레포트
	 * @param pc
	 * @param x
	 * @param y
	 * @param map
	 * @param randomRange
	 * @param type
	 */
	public static void tryCount(L1PcInstance pc, int x, int y, int map, int heading, int randomRange, int type){
		try{
			int tryCount = 0;
			int head = 0;
			int[] loc0 = { -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5 };
			int[] loc1 = { -5 , -4,-3, -2, -1, 0, 1, 2 , 3, 4, 5 }; // 추가
			switch(type){
			case 0:
				x += loc0[random(loc0.length)];
				y += loc0[random(loc0.length)];
				head = random(0, 7);
				break;
			case 1:
				x += loc1[random(loc1.length)];
				y += loc1[random(loc1.length)];
				head = random(0, 7);
				break;
			case 2:
				x += 0;
				y += 0;
				head = heading;
				break;
			default:
				x += loc0[random(loc0.length)];
				y += loc0[random(loc0.length)];
				head = random(0, 7);
				break;
			}
			do {
				tryCount++;
				L1Teleport.teleport( 
						pc, 
						x + (int) (Math.random() * randomRange) - (int) (Math.random() * randomRange), 
						y + (int) (Math.random() * randomRange) - (int) (Math.random() * randomRange), 
						(short) map, 
						head, 
						true);
				if (pc.getMap().isInMap(pc.getLocation()) && pc.getMap().isPassable(pc.getLocation())) {
					break;
				}
				Thread.sleep(1);
			} while (tryCount < 50);

			if (tryCount >= 50) {
				pc.getLocation().set(x,y,map);
				pc.getLocation().forward(random(0, 7));
			}
		} catch (Exception e){ }
	}
}
