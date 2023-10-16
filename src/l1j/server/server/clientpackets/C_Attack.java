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

package l1j.server.server.clientpackets;

import static l1j.server.server.model.Instance.L1PcInstance.REGENSTATE_ATTACK;

import server.LineageClient;
import server.manager.eva;
import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1LittleBugInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_AttackPacket;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_UseArrowSkill;
import l1j.server.server.datatables.UserSpeedControlTable;

//Referenced classes of package l1j.server.server.clientpackets:
//ClientBasePacket

public class C_Attack extends ClientBasePacket {

	private int _targetX = 0;
	private int _targetY = 0;
	public C_Attack(byte[] decrypt, LineageClient client) {
		super(decrypt);
		int targetId = readD();
		int x = readH();
		int y = readH();
		_targetX = x;
		_targetY = y;

		L1PcInstance pc = client.getActiveChar();
		L1Object target = L1World.getInstance().findObject(targetId);

		if (pc.isGhost() || pc.isDead() || pc.isTeleport()) { return; }
		if (pc.isInvisble()) { return; }
		if (pc.isInvisDelay()) { return; }

		if (pc.getInventory().getWeight240() >= 200) {
			pc.sendPackets(new S_ServerMessage(110)); // \f1아이템이 너무 무거워 전투할 수가 없습니다.
			return;
		}

		// 공격 액션을 취할 수 있는 경우의 처리
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ABSOLUTE_BARRIER)) { // 아브소르트바리아의 해제
			pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.ABSOLUTE_BARRIER);
			//pc.startHpRegeneration();
			//pc.startMpRegeneration();
			pc.startHpRegenerationByDoll();
			pc.startMpRegenerationByDoll();
		}

		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MEDITATION)){
			pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.MEDITATION);
		}

		pc.delInvis();
		pc.setRegenState(REGENSTATE_ATTACK);

		pc.setAttackSpeed(UserSpeedControlTable.getInstance().getAttackSpeed(pc.getGfxId().getTempCharGfx(), pc.getCurrentWeapon() + 1));
		// 공격 요구 간격을 체크한다
		if(pc.isGm()){
			if(pc.getGmTest() == 1){
				pc.speed_save();
			}
			attack(pc, target, x, y);
		}else{
			if(pc.getAttackSpeed() == 0){
				//pc.sendPackets(new S_SystemMessage("현제 변신에 대한 공격 속도 데이터가 없습니다. 운영자에게 제보 해주세요! 현제 변신코드("+pc.getGfxId().getTempCharGfx()+") 무기코드 ("+(pc.getCurrentWeapon() + 1)+")입니다."));
				return;
			}else{
				long oldtime = System.currentTimeMillis() - pc.getOldAttackTime();
				if(oldtime > (pc.getAttackSpeed() - 40) && pc.getTaskCount() == 0){// -20는 약간의 오차와 여유를 두기위한
					//pc.sendPackets(new S_SystemMessage("일반어택"));
					attack(pc, target, x, y);
				}else{
					long TaskTime = pc.getAttackSpeed() - oldtime;
					if((pc.getAttackSpeed() * 2) < pc.getNextAttackTime()){
						//pc.sendPackets(new S_SystemMessage("리턴"));
						return;
					}
					if(pc.getTaskCount() != 0){
						TaskTime = pc.getAttackSpeed();
					}
					pc.setNextAttackTime(pc.getNextAttackTime() + TaskTime);
					pc.TaskCountUp();
					//pc.sendPackets(new S_SystemMessage("타임테스크 : "+TaskTime));
					AttackTask task = new AttackTask(pc, target, x, y, TaskTime);
					GeneralThreadPool.getInstance().schedule(task, pc.getNextAttackTime());
				}
			}
		}
	}

	private class AttackTask implements Runnable {
		private L1PcInstance pc = null;
		private L1Object target = null;
		private int x = 0;
		private int y = 0;
		private long time = 0;

		private AttackTask(L1PcInstance p, L1Object t, int xx, int yy, long tm) {
			pc = p;
			target = t;
			x = xx;
			y = yy;
			time = tm;
		}

		@Override
		public void run(){
			attack(pc, target, x, y);
			pc.setNextAttackTime(pc.getNextAttackTime() - time);
			pc.TaskCountDown();
		}
	}

	private void attack(L1PcInstance pc, L1Object target, int x, int y){
		pc.setOldAttackTime(System.currentTimeMillis());
		if (target != null && !((L1Character) target).isDead()) {
			if (target instanceof L1LittleBugInstance){
				return;
			}
			if (target instanceof L1NpcInstance) {
				int hiddenStatus = ((L1NpcInstance)target).getHiddenStatus();
				if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_SINK || hiddenStatus == L1NpcInstance.HIDDEN_STATUS_FLY) {
					return;
				}
			}
			if (Config.CHECK_ATTACK_INTERVAL) {
				int result;
				result = pc.getAcceleratorChecker().checkInterval(AcceleratorChecker.ACT_TYPE.ATTACK);
				if (result == AcceleratorChecker.R_DISCONNECTED) {
					pc.addSpeedHackCount(1);
					return;
				} /*else{
					pc.addSpeedHackCount(-1);
				}*/
			}
			target.onAction(pc);
		} else { // 하늘 공격
			// TODO 활로 지면에 하늘 공격했을 경우는 화살이 날지 않으면 안 된다
			int weaponId = 0;
			int weaponType = 0;
			L1ItemInstance weapon = pc.getWeapon();
			L1ItemInstance arrow = null;
			L1ItemInstance sting = null;						
			if (weapon != null) {
				weaponId = weapon.getItem().getItemId();
				weaponType = weapon.getItem().getType1();
				if (weaponType == 20) {
					arrow = pc.getInventory().getArrow();
				}
				if (weaponType == 62) {
					sting = pc.getInventory().getSting();
				}
			}
			pc.getMoveState().setHeading(CharPosUtil.targetDirection(pc, x, y)); // 방향세트
			if (weaponType == 20 && (weaponId == 190|| (weaponId >= 11011 && weaponId <= 11013 )  || arrow != null)) {
				calcOrbit(pc.getX(), pc.getY(), pc.getMoveState().getHeading());
				if (arrow != null) { 
					if (pc.getGfxId().getTempCharGfx() == 7968){
						pc.sendPackets(new S_UseArrowSkill(pc, 0, 7972, _targetX, _targetY, true));
						Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 7972, _targetX, _targetY, true));
					}else if (pc.getGfxId().getTempCharGfx() == 8900){
						pc.sendPackets(new S_UseArrowSkill(pc, 0, 8904, _targetX, _targetY, true));
						Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 8904, _targetX, _targetY, true));
					}else if (pc.getGfxId().getTempCharGfx() == 8913){
						pc.sendPackets(new S_UseArrowSkill(pc, 0, 8916, _targetX, _targetY, true));
						Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 8916, _targetX, _targetY, true)); //추가
					}else{
						pc.sendPackets(new S_UseArrowSkill(pc, 0, 66, _targetX, _targetY, true));
						Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 66, _targetX, _targetY, true));
					}
					pc.getInventory().removeItem(arrow, 1);
				} else if (weaponId == 190) { 
					pc.sendPackets(new S_UseArrowSkill(pc, 0, 2349, _targetX, _targetY, true));
					Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 2349, _targetX, _targetY, true));
					//} else if (weaponId >= 11011 && weaponId <= 11013) { 
					//    pc.sendPackets(new S_UseArrowSkill(pc, 0, 8771, _targetX, _targetY, true));
					//   Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 8771, _targetX, _targetY, true));
					// }

				} else if (weaponId >= 11011 && weaponId <= 11013) { 
					pc.sendPackets(new S_UseArrowSkill(pc, 0, 8771, _targetX, _targetY, true));
					Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 8771, _targetX, _targetY, true));
				}
			} else if (weaponType == 62 && sting != null) {//건들렛
				calcOrbit(pc.getX(), pc.getY(), pc.getMoveState().getHeading());
				if (pc.getGfxId().getTempCharGfx() == 7968){
					pc.sendPackets(new S_UseArrowSkill(pc, 0, 7972, _targetX, _targetY, true));
					Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 7972, _targetX, _targetY, true));
				}else if (pc.getGfxId().getTempCharGfx() == 8900){
					pc.sendPackets(new S_UseArrowSkill(pc, 0, 8904, _targetX, _targetY, true));
					Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 8904, _targetX, _targetY, true));
				}else if (pc.getGfxId().getTempCharGfx() == 8913){
					pc.sendPackets(new S_UseArrowSkill(pc, 0, 8916, _targetX, _targetY, true));
					Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 8916, _targetX, _targetY, true)); //추가
				}else{
					pc.sendPackets(new S_UseArrowSkill(pc, 0, 2989, _targetX, _targetY, true));
					Broadcaster.broadcastPacket(pc, new S_UseArrowSkill(pc, 0, 2989, _targetX, _targetY, true));
				}
				pc.getInventory().removeItem(sting, 1);
			} else {
				pc.sendPackets(new S_AttackPacket(pc, 0, ActionCodes.ACTION_Attack));
				Broadcaster.broadcastPacket(pc, new S_AttackPacket(pc, 0, ActionCodes.ACTION_Attack));
			}
		}
		if (pc.getWeapon().getItem().getType() == 17) {//이건 무슨타입이지;키링크;
			if (pc.getWeapon().getItemId() == 410003) {
				pc.sendPackets(new S_SkillSound(pc.getId(), 6983));
				Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6983));
			} else {
				pc.sendPackets(new S_SkillSound(pc.getId(), 7049));
				Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 7049));
			}
		}
	}

	private void calcOrbit(int cX, int cY, int head) {
		final byte HEADING_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };	
		final byte HEADING_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
		float disX = Math.abs(cX - _targetX);
		float disY = Math.abs(cY - _targetY);
		float dis = Math.max(disX, disY);
		float avgX = 0;
		float avgY = 0;

		if (dis == 0) {
			avgX = HEADING_X[head];
			avgY = HEADING_Y[head];
		} else {
			avgX = disX / dis;
			avgY = disY / dis;
		}

		int addX = (int) Math.floor((avgX * 15) + 0.59f);
		int addY = (int) Math.floor((avgY * 15) + 0.59f);

		if (cX > _targetX) { addX *= -1; }
		if (cY > _targetY) { addY *= -1; }

		_targetX = _targetX + addX;
		_targetY = _targetY + addY;
	}
}
