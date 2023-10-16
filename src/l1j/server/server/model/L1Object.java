/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l1j.server.server.model;

import java.io.Serializable;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;

// Referenced classes of package l1j.server.server.model:
// L1PcInstance, L1Character

/**
 * ����� �����ϴ� ��� ������Ʈ�� ���̽� Ŭ����
 */
public class L1Object implements Serializable {
	private static final long serialVersionUID = 1L;
	private L1Location _loc = new L1Location();
	private int _id = 0;

	/**
	 * ������Ʈ�� �����ϴ� MAP�� MAP ID�� �����ش�
	 * 
	 * @return MAP ID
	 */
	public short getMapId() {	return (short) _loc.getMap().getId();	}

	/**
	 * ������Ʈ�� �����ϴ� MAP�� MAP ID�� �����Ѵ�
	 * 
	 * @param mapId
	 *            MAP ID
	 */
	public void setMap(short mapId) {	_loc.setMap(L1WorldMap.getInstance().getMap(mapId));	}

	/**
	 * ������Ʈ�� �����ϴ� MAP�� ���� �����ϴ� L1Map ������Ʈ�� �����ش�
	 * 
	 */
	public L1Map getMap() {	return _loc.getMap();	}

	/**
	 * ������Ʈ�� �����ϴ� MAP�� �����Ѵ�
	 * 
	 * @param map
	 *            ������Ʈ�� �����ϴ� MAP�� ���� �����ϴ� L1Map ������Ʈ
	 */
	public void setMap(L1Map map) {
		if (map == null) {
			throw new NullPointerException();
		}
		_loc.setMap(map);
	}

	/**
	 * ������Ʈ�� �ĺ��ϴ� ID�� �����ش�
	 * 
	 * @return ������Ʈ ID
	 */
	public int getId() {	return _id;	}

	/**
	 * ������Ʈ�� �ĺ��ϴ� ID�� �����Ѵ�
	 * 
	 * @param id
	 *            ������Ʈ ID
	 */
	public void setId(int id) {	_id = id;	}

	/**
	 * ������Ʈ�� �����ϴ� ��ǥ�� Xġ�� �����ش�
	 * 
	 * @return ��ǥ�� Xġ
	 */
	public int getX() {	return _loc.getX();	}

	/**
	 * ������Ʈ�� �����ϴ� ��ǥ�� Xġ�� �����Ѵ�
	 * 
	 * @param x
	 *            ��ǥ�� Xġ
	 */
	public void setX(int x) {	_loc.setX(x);	}

	/**
	 * ������Ʈ�� �����ϴ� ��ǥ�� Yġ�� �����ش�
	 * 
	 * @return ��ǥ�� Yġ
	 */
	public int getY() {	return _loc.getY();	}

	/**
	 * ������Ʈ�� �����ϴ� ��ǥ�� Yġ�� �����Ѵ�
	 * 
	 * @param y
	 *            ��ǥ�� Yġ
	 */
	public void setY(int y) {	_loc.setY(y);	}

	/**
	 * ������Ʈ�� �����ϴ� ��ġ�� ���� �����ϴ�, L1Location ������Ʈ���� ������ �����ش�.
	 * 
	 * @return ��ǥ�� ���� �����ϴ�, L1Location ������Ʈ���� ����
	 */
	public L1Location getLocation() {	return _loc;	}

	public void setLocation(L1Location loc) {
		_loc.setX(loc.getX());
		_loc.setY(loc.getY());
		_loc.setMap(loc.getMapId());
	}

	public void setLocation(int x, int y, int mapid) {
		_loc.setX(x);
		_loc.setY(y);
		_loc.setMap(mapid);
	}

	/**
	 * ������ ������Ʈ������ ���� �Ÿ��� �����ش�.
	 */
	public double getLineDistance(L1Object obj) {	return this.getLocation().getLineDistance(obj.getLocation());	}

	/**
	 * ������ ������Ʈ������ ���� Ÿ�ϼ��� �����ش�.
	 */
	public int getTileLineDistance(L1Object obj) {	return this.getLocation().getTileLineDistance(obj.getLocation());	}

	/**
	 * ������ ������Ʈ������ Ÿ�ϼ��� �����ش�.
	 */
	public int getTileDistance(L1Object obj) {	return this.getLocation().getTileDistance(obj.getLocation());	}

	/**
	 * ������Ʈ�� �÷��̾��� ȭ�鳻�� �����(�νĵ�) ���� �ҷ� ����.
	 * 
	 * @param perceivedFrom
	 *            �� ������Ʈ�� �ν��� PC
	 */
	public void onPerceive(L1PcInstance perceivedFrom) {
	}

	/**
	 * ������Ʈ�� �׼��� �߻��� �� ȣ��
	 * 
	 * @param actionFrom
	 *            �׼��� ����Ų PC
	 */
	public void onAction(L1PcInstance actionFrom) {
	}

	/**
	 * ������Ʈ�� ��ȭ�� �� ȣ��
	 * 
	 * @param talkFrom
	 *            ���� �ǳ� PC
	 */
	public void onTalkAction(L1PcInstance talkFrom) {
	}
}
