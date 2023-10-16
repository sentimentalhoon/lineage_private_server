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


import l1j.server.server.OxTimeController;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.CommonUtil;

public class L1FieldObjectInstance extends L1NpcInstance {

	private static final long serialVersionUID = 1L;

	private int moveMapId;

	public L1FieldObjectInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onAction(L1PcInstance pc) {}

	@Override
	public void onTalkAction(L1PcInstance pc) {
		int npcid = getNpcTemplate().get_npcId();
		switch(npcid){
		case 4212015: // 드래곤 포탈
			if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_A)) {
				L1Teleport.teleport(pc, 33441, 32802, (short) 4, 5, false);
				pc.sendPackets(new S_ServerMessage(1626)); 
			}else{
				/**by 판도라 레이드포탈**/
				pc.sendPackets(new S_Message_YN(2923, ""));
				pc.DragonPortalLoc[0] = 32600;
				pc.DragonPortalLoc[1] = 32741;
				pc.DragonPortalLoc[2] = 1005;
				/**by 판도라 레이드포탈**/
			}
			break;
		case 4212016: // 파푸레이드
			if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_P)) {
				L1Teleport.teleport(pc, 33441, 32802, (short) 4, 5, false);
				pc.sendPackets(new S_ServerMessage(1626)); 
			}else{
				/**by 판도라 레이드포탈**/
				pc.sendPackets(new S_Message_YN(2923, ""));
				pc.DragonPortalLoc[0] = 32860;
				pc.DragonPortalLoc[1] = 32801;
				pc.DragonPortalLoc[2] = 1011;
				/**by 판도라 레이드포탈**/
			}
			break;
		case 4500101:   // 안타시작->동굴 
			L1Teleport.teleport(pc, 32935, 32610, (short) 1005, 5, true);
			break;
		case 4500102:	// 안타시작 -> 안타대기하는곳		
			L1Teleport.teleport(pc, 32671, 32668, (short) 1005, 5, true);
			break;
		case 4500103:	//안타대기->안타방 
			L1Teleport.teleport(pc, 32796, 32664, (short) 1005, 5, true);
		case 460001: // 퀴즈방 이동 NPC
			pc.sendPackets(new S_Message_YN(78, "")); // 무엇을 원합니까?
			break;
		case 450001876:
			String Chat = "모두에게 버프를 주는 것";
			pc.sendPackets(new S_Message_YN(847, Chat)); // 정말로 %s이(가) 맞습니까? (Y/N)
			break;
		default: break;
		}
	}

	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		L1World.getInstance().removeVisibleObject(this);
		L1World.getInstance().removeObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.getNearObjects().removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		getNearObjects().removeAllKnownObjects();
	}

	/**
	 * 이동할 맵을 설정한다.
	 * @param id
	 */
	public void setMoveMapId(int id){
		moveMapId = id;
	}






}
