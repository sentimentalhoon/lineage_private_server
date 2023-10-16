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

import java.util.ArrayList;
import l1j.server.server.model.Instance.L1PcInstance;



// Referenced classes of package l1j.server.server.model:
// L1UltimateBattle

public class L1PetMember {

	private boolean _isNowPet;


	private final ArrayList<L1PcInstance> _members = new ArrayList<L1PcInstance>();

	/**
	 * constructor�� ��.
	 */
	public L1PetMember() {
	}

	class PetThread implements Runnable {

		/**
		 * thread ���ν���.
		 */
		@Override
		public void run() {
			try {
				Thread.sleep(10000);
				
			} catch (Exception e) {
			}
		}
	}

	/**
	 * �÷��̾ ���� ��� ����Ʈ�� �߰��Ѵ�.
	 * 
	 * @param pc
	 *            ���Ӱ� �����ϴ� �÷��̾�
	 */
	public void addMember(L1PcInstance pc) {
		if (!_members.contains(pc)) {
			_members.add(pc);
		}
	}
	

	/**
	 * �÷��̾ ���� ��� ����Ʈ�κ��� �����Ѵ�.
	 * 
	 * @param pc
	 *            �����ϴ� �÷��̾�
	 */
	public void removeMember(L1PcInstance pc) {
		_members.remove(pc);
	}


	/**
	 * ���� ��� ����Ʈ�� Ŭ���� �Ѵ�.
	 */
	public void clearMembers() {
		_members.clear();
	}

	/**
	 * �÷��̾, ���� ��������� �����ش�.
	 * 
	 * @param pc
	 *            �����ϴ� �÷��̾�
	 * @return ���� ����̸� true, �׷��� ������ false.
	 */
	public boolean isMember(L1PcInstance pc) {
		return _members.contains(pc);
	}


	/**
	 * ���� ����� �迭�� �ۼ���, �����ش�.
	 * 
	 * @return ���� ����� �迭
	 */
	public L1PcInstance[] getMembersArray() {
		return _members.toArray(new L1PcInstance[_members.size()]);
	}

	/**
	 * ���� ������� �����ش�.
	 * 
	 * @return ���� �����
	 */
	public int getMembersCount() {
		return _members.size();
	}



	/**
	 * UB�������� �����Ѵ�.
	 * 
	 * @param i
	 *            true/false
	 */

	/**
	 * ��� �������� �����ش�.
	 * 
	 * @return ��� ���̸� true, �׷��� ������ false.
	 */
	public boolean isNowPet() {
		return _isNowPet;
	}

}
