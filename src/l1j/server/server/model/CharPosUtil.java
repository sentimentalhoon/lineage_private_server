package l1j.server.server.model;

import l1j.server.server.model.Instance.L1CastleGuardInstance;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.types.Point;

public class CharPosUtil {
	
	private static final byte HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };	
	private static final byte HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
	/**
	 * ĳ������ ������ ��ǥ�� �����ش�.
	 * 
	 * @return ������ ��ǥ
	 */
	public static int[] getFrontLoc(int x, int y, int heading) {
		int[] loc = new int[2];
		//int x = getX();
		//int y = getY();
		//int heading = getMoveState().getHeading();
		x += HEADING_TABLE_X[heading];
		y += HEADING_TABLE_Y[heading];
		loc[0] = x;
		loc[1] = y;
		return loc;
	}

	/**
	 * ������ ��ǥ�� ���� ������ �����ش�.
	 * 
	 * @param tx
	 *            ��ǥ�� Xġ
	 * @param ty
	 *            ��ǥ�� Yġ
	 * @return ������ ��ǥ�� ���� ����
	 */
	public static int targetDirection(L1Character cha, int tx, int ty) {
		float dis_x = Math.abs(cha.getX() - tx); // X������ Ÿ�ٱ����� �Ÿ�
		float dis_y = Math.abs(cha.getY() - ty); // Y������ Ÿ�ٱ����� �Ÿ�
		float dis = Math.max(dis_x, dis_y);  // Ÿ�ٱ����� �Ÿ�

		if (dis == 0) 	return cha.getMoveState().getHeading(); 

		int avg_x = (int) Math.floor((dis_x / dis) + 0.59f); // ���� �¿찡 ���� �켱�� �ձ�
		int avg_y = (int) Math.floor((dis_y / dis) + 0.59f); // ���� �¿찡 ���� �켱�� �ձ�

		int dir_x = 0;
		int dir_y = 0;

		if (cha.getX() < tx) 	dir_x =  1;
		if (cha.getX() > tx) 	dir_x = -1;

		if (cha.getY() < ty) 	dir_y =  1;
		if (cha.getY() > ty) 	dir_y = -1;

		if (avg_x == 0) 	dir_x =  0;
		if (avg_y == 0) 	dir_y =  0;

		if (dir_x ==  1 && dir_y == -1) 		return 1; // ��
		if (dir_x ==  1 && dir_y ==  0) 		return 2; // ���
		if (dir_x ==  1 && dir_y ==  1) 		return 3; // ������
		if (dir_x ==  0 && dir_y ==  1) 		return 4; // ����
		if (dir_x == -1 && dir_y ==  1) 		return 5; // ��
		if (dir_x == -1 && dir_y ==  0) 		return 6; // ����
		if (dir_x == -1 && dir_y == -1) 		return 7; // ����
		if (dir_x ==  0 && dir_y == -1) 		return 0; // �»�

		return cha.getMoveState().getHeading(); 
	}
	

	/**
	 * ������ ��ǥ������ ������, ��ֹ��� ����*���� �ʴ°�*�� �����ش�.
	 * 
	 * @param tx
	 *            ��ǥ�� Xġ
	 * @param ty
	 *            ��ǥ�� Yġ
	 * @return ��ֹ��� ������ true, ��� false�� �����ش�.
	 */
	public static boolean glanceCheck(L1Character cha, int tx, int ty) {
		L1Map map = cha.getMap();
		int chx = cha.getX();
		int chy = cha.getY();
		for (int i = 0; i < 15; i++) {
			if ((chx == tx && chy == ty) || (chx == tx && chy + 1 == ty)// 0  0  0  1
					|| (chx == tx && chy - 1 == ty) || (chx + 1 == tx && chy == ty)// 0 -1  1  0
					|| (chx + 1 == tx && chy + 1 == ty) || (chx + 1 == tx && chy - 1 == ty)// 1  1  1 -1
					|| (chx - 1 == tx && chy == ty) || (chx - 1 == tx && chy + 1 == ty)//-1  0 -1  1
					|| (chx - 1 == tx && chy - 1 == ty)) {									//-1 -1
				break; 
			}

			if (!map.isArrowPassable(chx, chy, targetDirection(cha, tx, ty))) return false;
			
			if (chx < tx && chy == ty) 		{chx++;} 
			else if (chx > tx && chy == ty) {chx--;} 
			else if (chx == tx && chy < ty) {chy++;} 
			else if (chx == tx && chy > ty) {chy--;} 
			else if (chx < tx && chy < ty)  {chx++;	chy++;} 
			else if (chx < tx && chy > ty)  {chx++;	chy--;} 
			else if (chx > tx && chy < ty)  {chx--;	chy++;}
			else if (chx > tx && chy > ty)  {chx--;	chy--;}
		}

		return true;
	}

	/**
	 * ������ ��ǥ�� ���� �����Ѱ��� �����ش�.
	 * 
	 * @param x
	 *            ��ǥ�� Xġ.
	 * @param y
	 *            ��ǥ�� Yġ.
	 * @param range
	 *            ���� ������ ����(Ÿ�ϼ�)
	 * @return ���� �����ϸ� true, �Ұ����ϸ� false
	 */
	public static boolean isAttackPosition(L1Character cha, int x, int y, int range) {
		if (range >= 7) {// ���� ����(7�̻��� ��� ���⸦ ����ϸ�(��) ȭ��ܿ� ���´�)
			if (cha.getLocation().getTileDistance(new Point(x, y)) > range) 
				return false;
		} else {
			if (cha.getLocation().getTileLineDistance(new Point(x, y)) > range) 
				return false;
		}
		if(cha instanceof L1CastleGuardInstance){
			L1CastleGuardInstance guard = (L1CastleGuardInstance)cha;
			if(guard.getNpcId() == 7000002 || guard.getNpcId() == 4707001){
				return true;
			}
		}
		return glanceCheck(cha, x, y);
	}
	

	/**
	 * ĳ���Ͱ� �����ϴ� ��ǥ��, ��� ���� ���ϰ� ����� �����ش�.
	 * 
	 * @return ��ǥ�� ���� ��Ÿ���� ��. ������Ƽ ���̸� 1, �Ĺ�Ʈ ���̸顪1, ��� ���̸� 0.
	 */
	public static int getZoneType(L1Character cha) {
		if (cha.getMap().isSafetyZone(cha.getLocation())) 		return 1;
		else if (cha.getMap().isCombatZone(cha.getLocation()))  return -1;
		else 													return 0;
	}
}
