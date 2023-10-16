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

import java.util.logging.Logger;
import java.util.Timer;
import java.util.Random;
import java.util.TimerTask;

import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_HPUpdate;

public class L1ZombieMod {
	private static final Logger _log = Logger.getLogger(L1ZombieMod.class
			.getName());

	public static final int STATUS_NONE = 0;
	public static final int STATUS_READY = 1;
	public static final int STATUS_PLAYING = 2;
	private static final Random _random = new Random();

	private final ArrayList<L1PcInstance> _members = new ArrayList<L1PcInstance>();
	private int _ZombieModStatus = STATUS_NONE;
	private int _ZombieCount = 0;
	private int _BaseHp = 0;
	private int _Zombie = 0;
	private int _ZombieHp = 0;

	private static L1ZombieMod _instance;

	public static L1ZombieMod getInstance() {
		if (_instance == null) {
			_instance = new L1ZombieMod();
		}
		return _instance;
	}

	private void readyZombieMod() {
		setZombieModStatus(STATUS_READY);
		L1ZombieModReadyTimer zbTimer = new L1ZombieModReadyTimer();
		//L1ZombieModReadyTimer1 zbTimer1 = new L1ZombieModReadyTimer1();
		//zbTimer1.start();
		zbTimer.begin();
	}

	private void startZombieMod() {
		setZombieModStatus(STATUS_PLAYING);
		int membersCount = getMembersCount();
		if (membersCount < 0) {
			for (L1PcInstance pc : getMembersArray()) {
				pc.sendPackets(new S_SystemMessage("\\fY�����忡 ������ �ο��� : "
						+ membersCount + " �� (�ּ� 4�� �䱸)"));
				pc.sendPackets(new S_SystemMessage("\\fY�����ο��� �����Ͽ� �����尡 ����˴ϴ�."));
			}
			endZombieMod();
			return;
		}
		if (membersCount < 4) {
			setZombie(1);
			setZombieHp(1100);
		} else if (5 >= membersCount && membersCount < 10) {
			setZombie(2);
			setZombieHp(1400);
		} else if (10 >= membersCount && membersCount < 20) {
			setZombie(3);
			setZombieHp(1700);
		} else if (20 >= membersCount && membersCount < 30) {
			setZombie(4);
			setZombieHp(2100);
		} else if (30 >= membersCount && membersCount < 40) {
			setZombie(5);
			setZombieHp(2500);
		} else if (40 >= membersCount && membersCount < 50) {
			setZombie(6);
			setZombieHp(3400);
		} else if(50 >= membersCount){
			setZombie(8);
			setZombieHp(4000);
		}
		int zb = getZombie();
		for (int ss = 0; ss < zb; ss++) {
			polyZombie();
		}
		for (L1PcInstance pc : getMembersArray()) {
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(pc, L1SkillId.CANCELLATION, pc.getId(),
					pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_LOGIN);
			speedUp(pc);
		}

		for (L1PcInstance pc : getMembersArray()) {
			if (pc.getZombieMod() == 1) {
				L1PolyMorph.doPoly(pc, 6140, -1,L1PolyMorph.MORPH_BY_LOGIN);
				pc.getInventory().storeItem(311, 1);
				pc.getInventory().storeItem(40745, 1000);
			}
		}
		// ��������۽� ���� �˷��ְ�  by-Kingdom
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fY������ ���۵˴ϴ�. ���ִ� ��"+getZombie()+"�����Դϴ�"));
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fY30�а� ��Ƽ�ø� �ΰ��� �¸��մϴ�"));
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fY����� ����ΰ� ã�� ������ ���Ѿ��մϴ�"));
		// ��������۽� ���� �˷��ְ�  by-Kingdom
		L1ZombieModScore zbScore = new L1ZombieModScore();
		zbScore.begin();
	}

	public void endZombieMod() {
		setZombieModStatus(STATUS_NONE);
		for (L1PcInstance pc : getMembersArray()) {
			// �����峡���� ȭ��� ���񹫱� �����ϰԲ� by-Kingdom
			int ȭ�찹�� = pc.getInventory().countItems(40745);
			int Ȱ���� = pc.getInventory().countItems(311);
			int ���񹫱ⰹ�� = pc.getInventory().countItems(310);
			// �����峡���� ȭ��� ���񹫱� �����ϰԲ� by-Kingdom
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(pc, L1SkillId.CANCELLATION, pc.getId(),
					pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			L1PolyMorph.undoPoly(pc);
			pc.setZombieMod(0);
			speedCancel(pc);
			pc.setMaxHp(pc.getBackHp());
			pc.setCurrentHp(pc.getBackHp());
			pc.setBackHp(0);
			pc.setWeapon(null);
			// �����峡���� ȭ��� ���񹫱� �����ϰԲ� by-Kingdom
			pc.getInventory().consumeItem(311, Ȱ����);
			pc.getInventory().consumeItem(310, ���񹫱ⰹ��);
			pc.getInventory().consumeItem(40745, ȭ�찹��);
			// �����峡���� ȭ��� ���񹫱� �����ϰԲ� by-Kingdom
			// �����峡���� Hp������Ʈ by-Kingdom
			pc.sendPackets(new S_HPUpdate(pc));
			// �����峡���� Hp������Ʈ by-Kingdom
			L1Teleport.teleport(pc, 33443, 32800, (short) 4, 5, true);
		}
		clearMembers();
	}

	public void removeRetiredMembers() {
		L1PcInstance[] temp = getMembersArray();
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].getMapId() != 202) {
				removeMember(temp[i]);
			}
		}
	}

	public void addMember(L1PcInstance pc) {
		if (!_members.contains(pc)) {
			_members.add(pc);
		}
		if (getMembersCount() > 0 && getZombieModStatus() == STATUS_NONE) {
			readyZombieMod();
			// ����������� ������ ���� (����by-Season)by-Kingdom
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fY������尡 �����Ͽ� 1�е� ������ְ� �����մϴ� "));
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fY�ٵ� ���ζ������� �ڸ�������ּ���."));
			// ����������� ������ ���� (����by-Season)by-Kingdom
		}
	}

	public void removeMember(L1PcInstance pc) {
		_members.remove(pc);
	}

	public void clearMembers() {
		_members.clear();
	}

	public boolean isMember(L1PcInstance pc) {
		return _members.contains(pc);
	}

	public L1PcInstance[] getMembersArray() {
		return _members.toArray(new L1PcInstance[_members.size()]);
	}

	public int getMembersCount() {
		return _members.size();
	}

	private void setZombieModStatus(int i) {
		_ZombieModStatus = i;
	}

	public int getZombieModStatus() {
		return _ZombieModStatus;
	}

	//
	private void setZombie(int i) {
		_Zombie = i;
	}

	public int getZombie() {
		return _Zombie;
	}

	private void setZombieHp(int i) {
		_ZombieHp = i;
	}

	public int getZombieHp() {
		return _ZombieHp;
	}

	public void polyZombie() {
		L1PcInstance zombiepc[] = getMembersArray();
		int rnd = _random.nextInt(zombiepc.length);
		// zombiepc[rnd].setBaseHp(zombiepc[rnd].getMaxHp());
		if (zombiepc[rnd].getZombieMod() != 2) {
			zombiepc[rnd].setZombieMod(2);
			zombiepc[rnd].setMaxHp(getZombieHp());
			zombiepc[rnd].setCurrentHp(getZombieHp());
			zombiepc[rnd].getInventory().storeItem(310, 1);
			L1PolyMorph.doPoly(zombiepc[rnd], 5110, -1,L1PolyMorph.MORPH_BY_LOGIN);
		}
	}

	public void polyHuman() {
		int[] polylist = { 4145, 2529, 2429, 800 };
		int rndpoly = _random.nextInt(polylist.length);
		int polyId = polylist[rndpoly];
		L1PcInstance humanpc[] = getMembersArray();
		int rnd = _random.nextInt(humanpc.length);
		if (humanpc[rnd].getZombieMod() == 1) {
			L1PolyMorph.doPoly(humanpc[rnd], polyId, -1, L1PolyMorph.MORPH_BY_LOGIN);
		}
	}

	public void speedUp(L1PcInstance pc) {
		int time = 200 * 1000;
		int objectId = pc.getId();
		pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_BRAVE, time);
		pc.sendPackets(new S_SkillBrave(objectId, 1, 9999));
		pc.getMoveState().setBraveSpeed(1);

		pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_HASTE, time);
		pc.sendPackets(new S_SkillHaste(objectId, 1, 9999));
		pc.getMoveState().setMoveSpeed(1);
	}

	public void speedCancel(L1PcInstance pc) {
		int objectId = pc.getId();
		pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_BRAVE, 0);
		pc.sendPackets(new S_SkillBrave(objectId, 1, 0));
		pc.getMoveState().setBraveSpeed(0);

		pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_HASTE, 0);
		pc.sendPackets(new S_SkillHaste(objectId, 1, 0));
		pc.getMoveState().setMoveSpeed(0);
	}

	public class L1ZombieModReadyTimer extends TimerTask {

		public L1ZombieModReadyTimer() {
		}

		@Override
		public void run() {
			this.cancel();
			startZombieMod();
			L1ZombieModTimer zbTimer = new L1ZombieModTimer();
			zbTimer.begin();
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 60000);

		}

	}
	/////////////////////////////////////////////// ���������ī���� �߰� by-Kingdom
	/*
	public class L1ZombieModReadyTimer1 extends Thread {

		public void run(){
			try{
				int A = 60;
				for (int i = 0; i < 60; i++) { //55�ʺ��� 5�ʸ��� �˸� by-Kingdom
					if(i == 55 || i == 50 || i == 45 || i == 40 || i == 35|| i == 30|| i == 25|| i == 20|| i == 15 || i <= 10 ){
						L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fU���� ����"+A+"����"));
					}
					A--;
					Thread.sleep(1000);	
				}
			}catch(InterruptedException ie){
				System.out.println("error : ����5��Ÿ�̸� ����");
			}
		}
	}
	*/
	/////////////////////////////////////////////// ���������ī���� �߰� by-Kingdom
	public class L1ZombieModTimer extends TimerTask {

		public L1ZombieModTimer() {
		}

		@Override
		public void run() {
			this.cancel();
			if (getZombieModStatus() == STATUS_PLAYING) {
				endZombieMod();
			}
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 300000);
		}
	}

	// 5�ʰ������� ���� ��Ȳ�� �����Ѵ�. ���������� ���� ���������� ���ᰡ�ȴ�.
	public class L1ZombieModScore extends TimerTask {

		public L1ZombieModScore() {
		}

		@Override
		public void run() {
			this.cancel();
			if (getZombieModStatus() == STATUS_PLAYING) {
				int human = 0;
				int zombie = 0;
				L1PcInstance[] temp = getMembersArray();
				for (int i = 0; i < temp.length; i++) {
					if (temp[i].getZombieMod() == 1) {
						human++;
					} else if (temp[i].getZombieMod() == 2) {
						zombie++;
					}
				}
				if (human == 0) {
					endZombieMod();
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\f4������ �¸�"));
				} else if (zombie == 0) {
					endZombieMod();
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\f4�ΰ��� �¸�"));
				} else {
					L1World.getInstance().broadcastPacketToAll(
							new S_SystemMessage("���� ���ھ� ���� : " + zombie
									+ "�� / �޸� : " + human + "��"));
					L1ZombieModScore zbScore = new L1ZombieModScore();
					zbScore.begin();
				}
			}
			return;
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 7000);
		}
	}

}
