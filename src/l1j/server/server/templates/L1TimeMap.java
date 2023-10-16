/**
 * Ÿ�̸� ���� �� ��ü
 * 2008. 12. 04
*/

package l1j.server.server.templates;

public class L1TimeMap{

	private int id;
	private int time;
	private int DoorId;

	/** 
	 * �⺻ ������
	 * @param	(int)	id		�� ���̵�
	 * @param	(int)	time	������ �ð�(s)
	*/
	public L1TimeMap(int id, int time){
		this.id = id;
		this.time = time;
	}
	/** 
	 * �⺻ ������
	 * @param	(int)	id		�� ���̵�
	 * @param	(int)	time	������ �ð�(s)
	 * @param	(int)	DoorId	������ �� ���̵�
	*/
	public L1TimeMap(int id, int time, int DoorId){
		this.id = id;
		this.time = time;
		this.DoorId = DoorId;
	}
	/** 
	 * �� ���̵� ����
	 * @return	(int)	�� ���̵�
	*/
	public int getId(){
		return id;
	}
	/** 
	 * ���� �ð� ����
	 * @return	(int)	�����ð�
	*/
	public int getTime(){
		return time;
	}
	/** 
	 * ������ �� ���̵� ����
	 * @return	(int)	�� ���̵�
	*/
	public int getDoor(){
		return DoorId;
	}
	/** 
	 * �����ð� ���
	 * @return	(boolean)	�ð��� ����Ǿ����� true, �����ִٸ� false
	*/
	public boolean count(){
		return time-- <= 0;
	}
}