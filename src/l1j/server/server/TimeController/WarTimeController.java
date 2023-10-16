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
package l1j.server.server.TimeController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import l1j.server.Config;
import l1j.server.server.datatables.CastleTable;
//import l1j.server.server.datatables.CharSoldierTable;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.CheckWarTime;
import l1j.server.server.datatables.DoorSpawnTable;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1WarSpawn;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1CrownInstance;
import l1j.server.server.model.Instance.L1DoorInstance;
import l1j.server.server.model.Instance.L1FieldObjectInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1TowerInstance;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Castle;

public class WarTimeController implements Runnable {
	private static WarTimeController _instance;
	private static L1Castle[] _l1castle = new L1Castle[8];
	private static Calendar[] _war_start_time = new Calendar[8];
	private static Calendar[] _war_end_time = new Calendar[8];
	private boolean[] _is_now_war = new boolean[8];

	private WarTimeController() {
		for (int i = 0; i < _l1castle.length; i++) {
			_l1castle[i] = CastleTable.getInstance().getCastleTable(i + 1);
			Calendar nowTime = getRealTime();
			Calendar oldTime = getRealTime();
			oldTime.setTime(_l1castle[i].getWarTime().getTime());
			_war_start_time[i] = _l1castle[i].getWarTime();
			/*
			_war_end_time[i] = (Calendar) _l1castle[i].getWarTime().clone();
			_war_end_time[i].add(Config.ALT_WAR_TIME_UNIT, Config.ALT_WAR_TIME);
			 */
			oldTime.add(Config.ALT_WAR_TIME_UNIT, Config.ALT_WAR_TIME);

			if(CheckWarTime.getInstance().isActive(_l1castle[i].getId()) 
					&& nowTime.after(oldTime)){
				nowTime.add(Config.ALT_WAR_INTERVAL_UNIT, Config.ALT_WAR_INTERVAL);
				_l1castle[i].setWarTime(nowTime);
				CastleTable.getInstance().updateCastle(_l1castle[i]);
			}

			_war_start_time[i] = _l1castle[i].getWarTime();
			_war_end_time[i] = oldTime;
		}
	}

	public static WarTimeController getInstance() {
		if (_instance == null) {
			_instance = new WarTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkWarTime(); // ���� �ð��� üũ
				Thread.sleep(1000);
			}
		} catch (Exception e1) {
		}
	}

	public static Calendar getRealTime() {
		TimeZone _tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(_tz);
		return cal;
	}

	public static L1Castle  getCastle(int id){
		return _l1castle[id];
	}

	public static void  setCastle(int id,L1Castle castle){
		_l1castle[id] = castle;
	}

	public static Calendar  getWarStartTime(int id){
		return _war_start_time[id];
	}

	public static void  setWarStartTime(int id,Calendar time){
		_war_start_time[id] = time;
	}

	public static Calendar  getWarEndTime(int id){
		return _war_end_time[id];
	}

	public static void  setWarEndTime(int id,Calendar time){
		_war_end_time[id] = time;
	}

	public boolean isNowWar(int castle_id) {
		return _is_now_war[castle_id - 1];
	}

	public void checkCastleWar(L1PcInstance player) {
		for (int i = 0; i < 8; i++) {
			if (_is_now_war[i]) {
				player.sendPackets(new S_PacketBox(S_PacketBox.MSG_WAR_GOING, i + 1)); // %s�� �������� �������Դϴ�.
			}
		}
	}

	private void checkWarTime() {
		L1WarSpawn warspawn = null;
		Calendar Rtime = RealTimeClock.getInstance().getRealTimeCalendar();
		/*		System.out.print(Rtime.get(Calendar.DAY_OF_MONTH));
		System.out.print("��"+Rtime.get(Calendar.HOUR_OF_DAY));
		System.out.print("��"+Rtime.get(Calendar.MINUTE));
		System.out.println("��"+Rtime.get(Calendar.SECOND)+"��");*/
		for (int i = 0; i < 8; i++) {
			if (_war_start_time[i].before(Rtime) // ���� ����
					&& _war_end_time[i].after(Rtime)) {
				if (_is_now_war[i] == false) {
					_is_now_war[i] = true;
					// �⸦ spawn �Ѵ�
					warspawn = new L1WarSpawn();
					warspawn.SpawnFlag(i + 1);
					// ������ ������ �ݴ´�
					for (L1DoorInstance door : DoorSpawnTable.getInstance().getDoorList()) {
						if (L1CastleLocation.checkInWarArea(i + 1, door)) {
							door.setAutoStatus(0);// �ڵ������� ����
							door.repairGate();
						}
					}
					if (_l1castle[i].getCastleSecurity() == 1)
						securityStart(_l1castle[i]);// ġ�Ȱ���
					L1World.getInstance().broadcastPacketToAll(new S_PacketBox(S_PacketBox.MSG_WAR_BEGIN, i + 1)); // %s�� �������� ���۵Ǿ����ϴ�.
					// ��� �������� �����ð� �˸���
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
					for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
						pc.sendPackets(new S_SystemMessage(String.format("�����ð�: %s ~ %s", 
								formatter.format(_war_start_time[i].getTime()), 
								formatter.format(_war_end_time[i].getTime()))));
					}
					for (int t = Config.ALT_WAR_TIME * 60; t > 0; --t) {
						try {
							Thread.sleep(1 * 1000);
							switch(t){
							case 3600:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 3000:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 2400:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 1800:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 1200:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 600:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 300:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 240:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 180:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            �������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 120:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            \\fS�������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 60:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            \\fT�������� ���� �ð��� "+ t/60 + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 30:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            \\fT�������� ���� �ð��� "+ t + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							case 10:
								L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("            \\fR�������� ���� �ð��� "+ t + "���Դϴ�.")); // %s�� �������� ���۵Ǿ����ϴ�.
								break;
							}
						} catch (Exception exception) {
							break;
						}
					}
					int[] loc = new int[3];
					L1Clan clan = null;
					for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
						int castleId = i + 1;
						if (L1CastleLocation.checkInWarArea(castleId, pc) && !pc.isGm()) {
							clan = L1World.getInstance().getClan(pc.getClanname());
							if (clan != null) {
								if (clan.getCastleId() == castleId) {
									continue;
								}
							}
							loc = L1CastleLocation.getGetBackLoc(castleId);
							L1Teleport.teleport(pc, loc[0], loc[1], (short) loc[2], 5, true);
						}
					}
				}
			} else if (_war_end_time[i].before(Rtime)) { // ���� ����
				if (_is_now_war[i] == true) {					
					_is_now_war[i] = false;
					L1World.getInstance().broadcastPacketToAll(
							new S_PacketBox(S_PacketBox.MSG_WAR_END, i + 1)); // %s�� �������� �����߽��ϴ�.
					_war_start_time[i].add(Config.ALT_WAR_INTERVAL_UNIT, Config.ALT_WAR_INTERVAL);
					_war_end_time[i].add(Config.ALT_WAR_INTERVAL_UNIT, Config.ALT_WAR_INTERVAL);
					_l1castle[i].setTaxRate(10); // ����10����
					_l1castle[i].setPublicMoney(0); // ����Ŭ����
					CastleTable.getInstance().updateCastle(_l1castle[i]);
					int castle_id = i + 1;
					/***************�߰�**************/
					int castle_id2 = 0;
					//���� ���忡 �ִ� �÷��̾ �����ϱ����ؼ� ..¯��
					for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
						L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
						if (clan != null) {  castle_id2 = clan.getCastleId();
						}
						if(castle_id2 == castle_id){//������ ����� ���� ��ȣ�� �Ǿ��� ������Ϲ�ȣ ���ٸ�...
							//������ ���� ������ �������� �����ؼ� �ֽõ���..
							//Ȥ�� ���� ���� ���� ���� ��� �������� �ִ°͵� ������..
							pc.getInventory().storeItem(5000218 , 1);//����������
						}
					}
					/***************�߰�***************/
					//					CharSoldierTable.getInstance().delCastleSoldier(castle_id);// �뺴 Ŭ���� 
					for (L1FieldObjectInstance flag : L1World.getInstance().getAllField()) {
						if (L1CastleLocation.checkInWarArea(castle_id, flag)) {
							flag.deleteMe();
						}
					}
					for (L1CrownInstance crown : L1World.getInstance().getAllCrown()) {
						if (L1CastleLocation.checkInWarArea(castle_id, crown)) {
							crown.deleteMe();
						}
					}
					for (L1TowerInstance tower : L1World.getInstance().getAllTower()) {
						if (L1CastleLocation.checkInWarArea(castle_id, tower)) {
							tower.deleteMe();
						}
					}

					warspawn = new L1WarSpawn();
					warspawn.SpawnTower(castle_id);
					for (L1DoorInstance door : DoorSpawnTable.getInstance().getDoorList()) {
						if (L1CastleLocation.checkInWarArea(castle_id, door)) {
							door.repairGate();
						}
					}					
				}
			}
		}
	}

	private void securityStart(L1Castle castle) {
		int castleId = castle.getId();
		int a = 0, b = 0, c = 0, d = 0, e = 0;
		int[] loc = new int[3];
		L1Clan clan = null;

		switch (castleId) {
		case 1: 
		case 2: 
		case 3: 
		case 4: a=52; b=248; c=249; d=250; e=251; break;
		case 5: 
		case 6: 
		case 7: 
		default : break;
		}

		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if ((pc.getMapId() == a || pc.getMapId() == b
					|| pc.getMapId() == c || pc.getMapId() == d
					|| pc.getMapId() == e) && !pc.isGm()) {
				clan = L1World.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					if (clan.getCastleId() == castleId) {
						continue;
					}
				}
				loc = L1CastleLocation.getGetBackLoc(castleId);
				L1Teleport.teleport(pc, loc[0], loc[1], (short) loc[2], 5, true);
			}
		}
		castle.setCastleSecurity(0);
		CastleTable.getInstance().updateCastle(castle);
		CharacterTable.getInstance().updateLoc(castleId, a, b, c, d, e);
	}
}
