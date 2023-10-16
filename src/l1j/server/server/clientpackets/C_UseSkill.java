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

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;

import static l1j.server.server.model.skill.L1SkillId.CALL_CLAN;
import static l1j.server.server.model.skill.L1SkillId.CUBE_BALANCE;
import static l1j.server.server.model.skill.L1SkillId.CUBE_IGNITION;
import static l1j.server.server.model.skill.L1SkillId.CUBE_QUAKE;
import static l1j.server.server.model.skill.L1SkillId.CUBE_SHOCK;
import static l1j.server.server.model.skill.L1SkillId.FIRE_WALL;
import static l1j.server.server.model.skill.L1SkillId.FOU_SLAYER;
import static l1j.server.server.model.skill.L1SkillId.LIFE_STREAM;
import static l1j.server.server.model.skill.L1SkillId.MASS_TELEPORT;
import static l1j.server.server.model.skill.L1SkillId.MEDITATION;
import static l1j.server.server.model.skill.L1SkillId.RUN_CLAN;
import static l1j.server.server.model.skill.L1SkillId.SCALES_EARTH_DRAGON;
import static l1j.server.server.model.skill.L1SkillId.SCALES_FIRE_DRAGON;
import static l1j.server.server.model.skill.L1SkillId.SCALES_WATER_DRAGON;
import static l1j.server.server.model.skill.L1SkillId.TELEPORT;
import static l1j.server.server.model.skill.L1SkillId.TRUE_TARGET;
import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Skills;
import server.LineageClient;

//Referenced classes of package l1j.server.server.clientpackets:
//ClientBasePacket

public class C_UseSkill extends ClientBasePacket {

	public C_UseSkill(byte abyte0[], LineageClient client) throws Exception {
		super(abyte0);
		int row = readC();
		int column = readC();
		int skillId = (row * 8) + column + 1;
		String charName = null;
		String message = null;
		int targetId = 0;
		int targetX = 0;
		int targetY = 0;
		L1PcInstance pc = client.getActiveChar();	

		if (pc.isTeleport() || pc.isDead()) { return; }
		if (!pc.isSkillMastery(skillId)) { return; }
		if (!pc.getMap().isUsableSkill()) {
			pc.sendPackets(new S_ServerMessage(563)); // \f1 ���⿡���� ����� �� �����ϴ�.
			return;
		}
		// �䱸 ������ üũ�Ѵ�
		if (Config.CHECK_SPELL_INTERVAL && !(skillId == FOU_SLAYER)) {
			int result;
			// FIXME ��� ��ų�� dir/no dir�ϱ��� �Ǵ��� ����
			if (SkillsTable.getInstance(). getTemplate(skillId). getActionId() ==
				ActionCodes.ACTION_SkillAttack) {
				result = pc.getAcceleratorChecker(). checkInterval(
						AcceleratorChecker.ACT_TYPE.SPELL_DIR);
			} else {
				result = pc.getAcceleratorChecker(). checkInterval(
						AcceleratorChecker.ACT_TYPE.SPELL_NODIR);
			}
			if (result == AcceleratorChecker.R_DISCONNECTED) {
				return;
			}
		}

		if(skillId == SCALES_EARTH_DRAGON  || skillId == SCALES_WATER_DRAGON ||skillId == SCALES_FIRE_DRAGON){
			if(pc.getSkillEffectTimerSet().hasSkillEffect(SCALES_EARTH_DRAGON)){
				pc.getSkillEffectTimerSet().removeSkillEffect(SCALES_EARTH_DRAGON);
				return;
			}else if(pc.getSkillEffectTimerSet().hasSkillEffect(SCALES_WATER_DRAGON)){
				pc.getSkillEffectTimerSet().removeSkillEffect(SCALES_WATER_DRAGON);
				return;
			}else if(pc.getSkillEffectTimerSet().hasSkillEffect(SCALES_FIRE_DRAGON)){
				pc.getSkillEffectTimerSet().removeSkillEffect(SCALES_FIRE_DRAGON);
				return;
			}
		}

		if(skillId == CUBE_IGNITION) {
			if(pc.getSkillEffectTimerSet().hasSkillEffect(CUBE_IGNITION)){
				pc.sendPackets(new S_ServerMessage(1412)); 
				return;
			}
		}
		if(skillId == CUBE_QUAKE) {
			if(pc.getSkillEffectTimerSet().hasSkillEffect(CUBE_QUAKE)){
				pc.sendPackets(new S_ServerMessage(1412)); 
				return;
			}
		}
		if(skillId == CUBE_SHOCK) {
			if(pc.getSkillEffectTimerSet().hasSkillEffect(CUBE_SHOCK)){
				pc.sendPackets(new S_ServerMessage(1412)); 
				return;
			}
		}
		if(skillId == CUBE_BALANCE) {
			if(pc.getSkillEffectTimerSet().hasSkillEffect(CUBE_BALANCE)){
				pc.sendPackets(new S_ServerMessage(1412)); 
				return;
			}
		}

		if (abyte0.length > 4) {
			try {
				switch(skillId){
				case CALL_CLAN : 
				case RUN_CLAN : 
					charName = readS(); 
					break;
				case TRUE_TARGET : 
					targetId = readD();
					targetX = readH();
					targetY = readH();
					message = readS();
					break;
				case TELEPORT :
				case MASS_TELEPORT :
					readH(); // MapID
					targetId = readD(); // Bookmark ID
					break;
				case FIRE_WALL :
				case LIFE_STREAM :
					targetX = readH();
					targetY = readH();
					break;
				default :{
					targetId = readD();
					targetX = readH();
					targetY = readH();
				}
				break;				
				}
			} catch (Exception e) {
				// _log.log(Level.SEVERE, "", e);
			}
		}

		if (pc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) { // �ƺ�Ҹ�Ʈ�ٸ����� ����
			pc.getSkillEffectTimerSet().killSkillEffectTimer(ABSOLUTE_BARRIER);
			pc.startHpRegenerationByDoll();
			pc.startMpRegenerationByDoll();
		}
		pc.getSkillEffectTimerSet().killSkillEffectTimer(MEDITATION);

		try {
			if (skillId == CALL_CLAN || skillId == RUN_CLAN) { 
				if (charName.isEmpty()) { return; }

				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < charName.length(); i++){
					if(charName.charAt(i) == '['){
						break;
					}
					sb.append(charName.charAt(i));
				}

				L1PcInstance target = L1World.getInstance().getPlayer(sb.toString());

				if (target == null) {
					pc.sendPackets(new S_ServerMessage(73, charName)); 
					return;
				}
				if (pc.getClanid() != target.getClanid()) {
					pc.sendPackets(new S_ServerMessage(414)); 
					return;
				}
				targetId = target.getId();
				if (skillId == CALL_CLAN) {
					int callClanId = pc.getCallClanId();
					if (callClanId == 0 || callClanId != targetId) {
						pc.setCallClanId(targetId);
						pc.setCallClanHeading(pc.getMoveState().getHeading());
					}
				}
			}
			/*
			switch(skillId){
			case 4:	//������ ��Ʈ
			case 6:	//���̽� ���
			case 7:	//���� Ŀ��
			case 10:	//ĥ ��ġ
			case 15:	//���̾� �ַο�
			case 16:	//��Ż��
			case 28:	//�����̾ ��ġ
			case 30:	//� ����
			case 34:	//�� ����Ʈ��
			case 38:	//�� ���� �ݵ�
			case 45:	//�̷���
			case 46:	//�� ����Ʈ
			case 77:	//����Ƽ�׷���Ʈ
			case 108:	//���̳� ��
			case 132:	//Ʈ���� �ַο�
			case 187:	//���� �����̾�
			case 203:	//���Ž�
				new AttackSkill(pc, skillId, targetId, targetX, targetY);	return;
			}
			 */  

			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(pc, skillId, targetId, targetX, targetY,
			message, 0, L1SkillUse.TYPE_NORMAL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

