/*
	Ȥ�� ������ �߰��� �ý��ۿ� ���� �ð� ���̳� 
	��Ÿ ���� ������ �ִ� ��� ������ �ֵ���...
 */

package l1j.server.server.templates;

import java.util.Calendar;

public class L1EvaSystem {

	public L1EvaSystem(int id) {
		_typeId = id;
	}

	private int _typeId;
	public int getSystemTypeId() { return _typeId; }

	private Calendar _time;
	private int _openLocation;
	private int _moveLocation;
	private int _openContinuation;
	
	/** ���۵� Ķ���� ���� �����´� */
	public Calendar getEvaTime() { return _time; }
	public void setEvaTime(Calendar i) { _time = i; }

	/**
	 * �ð��� �տ� ���� ��Ұ��� ���� �´�
	 * @return 0~7
	 */
	public int getOpenLocation() { return _openLocation; }
	public void setOpenLocation(int i) { _openLocation = i; }
	
	/**
	 * �ð��� �տ� �̵� ��Ҹ� ���� �´�
	 * @return 0: default 1: �׺� 2: ƼĮ
	 */
	public int getMoveLocation() { return _moveLocation; }
	public void setMoveLocation(int i) { _moveLocation = i; }
	
	/**
	 * ������ �׾ �ð��� ����� ����
	 * @return 0: default 1: ����
	 */
	public int getOpenContinuation() { return _openContinuation; }
	public void setOpenContinuation(int i) { _openContinuation = i; }
}