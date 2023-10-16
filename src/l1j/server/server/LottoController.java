
/**
 ������ �ζǸ� ���ӻ����� ���α׷� ��Ű��
 ���� ������ �ζǹ�ȣ�� ��� ���� ���̹Ƿ� �̱����� �̿��Ѵ�.
 by Chocco
 */

package l1j.server.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class LottoController{

	private static int Bonus;													// ���ʽ�
	private static final Object lock;											// lock ��
	private static LottoController instance;									// ���� ��ü �ν���Ʈ
	private static final ArrayList<Integer> number;
	private static final ThreadLocal<LottoController> localThread;				// ��Ƽ Cpu �� �޸� ���趧����...

	static{
		lock = new Object();
		number = new ArrayList<Integer>();
		localThread = new ThreadLocal<LottoController>();
		for(int i = 1; i < 46; i++) number.add(i);
	}

	/**
	 * ��Ƽ�ھ������� ���� ����ȭ �̱����� ����
	 * return	(LottoController)	Ŭ���� ���ϰ�ü
	 */
	public static LottoController getInstance(){
		LottoController oTemp = (LottoController) (localThread.get());			// ���� �����忡 ��ϵ� �ν���Ʈ�� ����
		if(oTemp == null){														// ���ٸ�
			synchronized(lock){
				oTemp = instance;												// ���� �ν���Ʈ ����
				if(oTemp == null) oTemp = instance = new LottoController();		// ���ٸ� ���
			}
			localThread.set(oTemp);												// ���� �����忡 ���
		}
		return oTemp;
	}

	/**
	 * �⺻ ������(�����)
	 */
	private LottoController(){}

	/**
	 * ���� ��¥���� ��ȯ
	 * @return	(String)		������ ��¥����(yyyy-MM-dd HH:mm:ss)
	 */
	public String getDate(){

		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
		return s.format(Calendar.getInstance().getTime());
	}

	/**
	 * Ư�� ��¥���� ��ȯ
	 * @param	(long)	time	Ư�� ��¥������ �и� �������� ����(���� 1970 �� 1 �� 1 �� 00:00:00 GMT)
	 * @return	(String)		������ ��¥����(yyyy-MM-dd HH:mm:ss)
	 */
	public String getDate(long time){
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return s.format(c.getTime());
	}

	/**
	 * �ζ� ��ȣ ����
	 */
	private void shuffle(){
		Collections.shuffle(number);
	}

	/**
	 * �ζǹ�ȣ ��ȯ
	 * return	(int[])		�ζ� 6�� ��ȣ
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
	 * �ζǹ�ȣ ��ȯ
	 * return	(int)		���ʽ� ��ȣ
	 */
	public int getBonus(){
		return Bonus;
	}
}