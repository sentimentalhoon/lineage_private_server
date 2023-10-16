/* This program is free software; you can redistribute it and/or modify
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

import static l1j.server.server.model.Instance.L1PcInstance.REGENSTATE_MOVE;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import server.LineageClient;
import server.manager.eva;
import l1j.server.Config;

//import java.util.Random; // 시간의 균열 - 티칼용 주석

import l1j.server.GameSystem.CrockSystem;
import l1j.server.GameSystem.PetRacing;
import l1j.server.server.datatables.EvaSystemTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.Dungeon;
import l1j.server.server.model.DungeonRandom;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.serverpackets.S_MoveCharPacket;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.templates.L1EvaSystem;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.L1World;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_MoveChar extends ClientBasePacket {
	Calendar currentDate = Calendar.getInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd h:mm:ss a"); 
	String time = dateFormat.format(currentDate.getTime());

	private static final byte HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };	
	private static final byte HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };

	// 이동
	public C_MoveChar(byte decrypt[], LineageClient client) throws Exception {
		super(decrypt);
		int locx = readH();
		int locy = readH();
		int heading = readC();

		L1PcInstance pc = client.getActiveChar();

		if (pc == null || pc.isTeleport()) { return; }
		if (Config.CHECK_MOVE_INTERVAL) {
			int result;
			result = pc.getAcceleratorChecker().checkInterval(AcceleratorChecker.ACT_TYPE.MOVE);
			if (result == AcceleratorChecker.R_DISCONNECTED) {
				pc.bkteleport();
				return;
			}
		}
		
		/** 뚫어버그 */
		if (!pc.getMap().ismPassable(locx, locy, heading)){
			L1Teleport.teleport(pc, pc.getX(), pc.getY(),pc.getMapId(),pc.getMoveState().getHeading(), false);
			eva.writeMessage(-4, "뚫어" + "[" + pc.getName() + "] : " + "x,y,map:"+pc.getLocation().getX()+","+pc.getLocation().getY()+","+pc.getLocation().getMapId());
			return;
		}
		
		if (heading < 0 ||heading > 7)return;

		pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.MEDITATION);
		pc.setCallClanId(0);

		if (!pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ABSOLUTE_BARRIER)) { // 아브소르트바리아중은 아니다
			pc.setRegenState(REGENSTATE_MOVE);
		}
		pc.getMap().setPassable(pc.getLocation(), true);
		if (pc.checkMove() == 0) {
			pc.bkteleport();//효과부여 ~짱돌 2009 -08 -26
		}

		locx += HEADING_TABLE_X[heading];
		locy += HEADING_TABLE_Y[heading];

		if (Dungeon.getInstance().dg(locx, locy, pc.getMap().getId(), pc)) { // 지하 감옥에 텔레포트 했을 경우
			return;
		}		  

		if (DungeonRandom.getInstance().dg(locx, locy, pc.getMap().getId(),	pc)) { // 텔레포트처가 랜덤인 텔레포트 지점
			return;
		}

		pc.getLocation().set(locx, locy);
		pc.getMoveState().setHeading(heading);
		Broadcaster.broadcastPacket(pc, new S_MoveCharPacket(pc));

		if(CrockSystem.getInstance().isOpen()){
			L1EvaSystem eva = EvaSystemTable.getInstance().getSystem(1);
			int[] loc = CrockSystem.getInstance().loc();
			if(Math.abs(loc[0]-pc.getX())<=1 && Math.abs(loc[1] - pc.getY())<=1 && loc[2] == pc.getMap().getId()) {
				switch(eva.getMoveLocation()) {
				case 0: return;
				case 1: L1Teleport.teleport(pc, 32639, 32876, (short) 780, 2, false); break;// 테베
				case 2: L1Teleport.teleport(pc, 32793, 32754, (short) 783, 2, false); break;// 티칼
				}			
			}
		}
		if(pc.isPetRacing())	PetRacing.getInstance().RacingCheckPoint(pc);
		L1WorldTraps.getInstance().onPlayerMoved(pc);		
		pc.getMap().setPassable(pc.getLocation(), false);

	}
}