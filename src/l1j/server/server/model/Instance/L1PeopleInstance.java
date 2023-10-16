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
package l1j.server.server.model.Instance;

import java.util.Timer;
import java.util.TimerTask;

import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1NpcTalkData;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.serverpackets.S_ChangeHeading;

public class L1PeopleInstance extends L1NpcInstance {
	private static final long serialVersionUID = 1L;

	/**
	 * @param template
	 */
	public L1PeopleInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onAction(L1PcInstance pc) {
		if (getCurrentHp() > 0 && !isDead()) {
			L1Attack attack = new L1Attack(pc, this);
			if (attack.calcHit()) {
				if (getNpcId() == 4500191 || getNpcId() == 450001831){
					int dmg = attack.calcDamage();
					String chatId = "������ �������� " + dmg + " �Դϴ�!";
					Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chatId, 0));
					//����ϱ� ������ ������ ��� �Ʒ��� ���
					//player.sendPackets(new S_SystemMessage("������: " + dmg + "."));
				}
				attack.addPcPoisonAttack(pc, this);
			}
			attack.action();
			attack.commit();
		}
	}

	@Override
	public void onNpcAI() {
		if (isAiRunning()) {
			return;
		}
		setActived(false);
		startAI();
	}

	@Override
	public void onTalkAction(L1PcInstance pc) {
		int objid = getId();
		L1NpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(getNpcTemplate().get_npcId());
		int npcid = getNpcTemplate().get_npcId();
		String htmlid = null;
		String[] htmldata = null;

		int pcX = pc.getX();
		int pcY = pc.getY();
		int npcX = getX();
		int npcY = getY();

		if(getNpcTemplate().getChangeHead()) {
			int heading = 0;
			
			if (pcX == npcX && pcY < npcY) 			heading = 0;
			else if (pcX > npcX && pcY < npcY) 		heading = 1;
			else if (pcX > npcX && pcY == npcY) 	heading = 2;
			else if (pcX > npcX && pcY > npcY) 		heading = 3;
			else if (pcX == npcX && pcY > npcY) 	heading = 4;
			else if (pcX < npcX && pcY > npcY) 		heading = 5;
			else if (pcX < npcX && pcY == npcY) 	heading = 6;
			else if (pcX < npcX && pcY < npcY) 		heading = 7;
			
			getMoveState().setHeading(heading);
			Broadcaster.broadcastPacket(this, new S_ChangeHeading(this));

			synchronized (this) {
				if (_monitor != null) {
					_monitor.cancel();
				}
				setRest(true);
				_monitor = new RestMonitor();
				_restTimer.schedule(_monitor, REST_MILLISEC);
			}
		}

		if (talking != null) {	
			switch(npcid){
			case 70839: //����Ʈ
				if (pc.isCrown() || pc.isKnight() || pc.isWizard()){
					htmlid="doettM1";					
				} else if (pc.isDarkelf()){
					htmlid="doettM2";					
				} else if (pc.isDragonknight()){
					htmlid="doettM3";					
				} else if (pc.isIllusionist()){
					htmlid="doettM4";					
				}
				break;
			case 70854: //�ĸ��޷�
				if (pc.isCrown() || pc.isKnight() || pc.isWizard()){
					htmlid="hurinM1";					
				} else if (pc.isDarkelf()){
					htmlid="hurinE3";					
				} else if (pc.isDragonknight()){
					htmlid="hurinE4";					
				} else if (pc.isIllusionist()){
					htmlid="hurinE5";					
				}
				break;
			case 70843: //�𸮿�
				if (pc.isCrown() || pc.isKnight() || pc.isWizard()){
					htmlid="morienM1";					
				} else if (pc.isDarkelf()){
					htmlid="morienM2";					
				} else if (pc.isDragonknight()){
					htmlid="morienM3";					
				} else if (pc.isIllusionist()){
					htmlid="morienM4";					
				}
				break;
			case 70849: //�׿�����
				if (pc.isCrown() || pc.isKnight() || pc.isWizard()){
					htmlid="theodorM1";					
				} else if (pc.isDarkelf()){
					htmlid="theodorM2";					
				} else if (pc.isDragonknight()){
					htmlid="theodorM3";					
				} else if (pc.isIllusionist()){
					htmlid="theodorM4";					
				}
				break;
			default:
				break;
			}
			// html ǥ�� ��Ŷ �۽�
			if (htmlid != null) { // htmlid�� �����ǰ� �ִ� ���
				if (htmldata != null) { // html ������ �ִ� ���� ǥ��
					pc.sendPackets(new S_NPCTalkReturn(objid, htmlid, htmldata));
				} else {
					pc.sendPackets(new S_NPCTalkReturn(objid, htmlid));
				}
			} else {
				if (pc.getLawful() < -1000) { // �÷��̾ ī��ƽ
					pc.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
				} else {
					pc.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
				}
			}
		}
	}
	@Override
	public void onFinalAction(L1PcInstance player, String action) {}

	public void doFinalAction(L1PcInstance player) {}

	private static final long REST_MILLISEC = 10000;

	private static final Timer _restTimer = new Timer(true);

	private RestMonitor _monitor;

	public class RestMonitor extends TimerTask {
		@Override
		public void run() {
			setRest(false);
		}
	}

}
