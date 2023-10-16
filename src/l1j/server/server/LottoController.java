
/**
 현실의 로또를 게임상으로 프로그램 시키자
 많은 유저가 로또번호를 사기 위한 것이므로 싱글톤을 이용한다.
 by Chocco
 */

package l1j.server.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class LottoController{

	private static int Bonus;													// 보너스
	private static final Object lock;											// lock 용
	private static LottoController instance;									// 단일 객체 인스턴트
	private static final ArrayList<Integer> number;
	private static final ThreadLocal<LottoController> localThread;				// 멀티 Cpu 의 메모리 관계때문에...

	static{
		lock = new Object();
		number = new ArrayList<Integer>();
		localThread = new ThreadLocal<LottoController>();
		for(int i = 1; i < 46; i++) number.add(i);
	}

	/**
	 * 멀티코어지원을 위해 동기화 싱글톤을 구현
	 * return	(LottoController)	클래스 단일객체
	 */
	public static LottoController getInstance(){
		LottoController oTemp = (LottoController) (localThread.get());			// 로컬 쓰레드에 등록된 인스턴트를 리턴
		if(oTemp == null){														// 없다면
			synchronized(lock){
				oTemp = instance;												// 기존 인스턴트 리턴
				if(oTemp == null) oTemp = instance = new LottoController();		// 없다면 등록
			}
			localThread.set(oTemp);												// 로컬 쓰레드에 등록
		}
		return oTemp;
	}

	/**
	 * 기본 생성자(비공개)
	 */
	private LottoController(){}

	/**
	 * 현재 날짜정보 반환
	 * @return	(String)		현재의 날짜정보(yyyy-MM-dd HH:mm:ss)
	 */
	public String getDate(){

		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
		return s.format(Calendar.getInstance().getTime());
	}

	/**
	 * 특정 날짜정보 반환
	 * @param	(long)	time	특정 날짜정보의 밀리 세컨드의 시점(기준 1970 년 1 월 1 일 00:00:00 GMT)
	 * @return	(String)		현재의 날짜정보(yyyy-MM-dd HH:mm:ss)
	 */
	public String getDate(long time){
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return s.format(c.getTime());
	}

	/**
	 * 로또 번호 섞기
	 */
	private void shuffle(){
		Collections.shuffle(number);
	}

	/**
	 * 로또번호 반환
	 * return	(int[])		로또 6개 번호
	 */
	public int[] getNumber(){
		int[] Temp = new int[6];
		shuffle();
		for(int i = 0; i < 7; i++){
			if(i == 6){
				Bonus = number.get(i);
				break;
			}
			Temp[i] = number.get(i);
		}
		return Temp;
	}

	/**
	 * 로또번호 반환
	 * return	(int)		보너스 번호
	 */
	public int getBonus(){
		return Bonus;
	}
}