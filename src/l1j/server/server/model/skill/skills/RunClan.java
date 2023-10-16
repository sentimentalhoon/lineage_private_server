package l1j.server.server.model.skill.skills;

import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_ServerMessage;

public class RunClan {

	public static void runSkill(L1Character cha, int _targetID) {
		L1PcInstance pc = (L1PcInstance) cha;
		L1PcInstance clanPc = (L1PcInstance) L1World.getInstance().findObject(_targetID);
		if (clanPc != null) {
			if (pc.getMap().isEscapable() || pc.isGm()) {
				boolean castle_area = L1CastleLocation.checkInAllWarArea(clanPc.getX(), clanPc.getY(), clanPc.getMapId());
				if ((clanPc.getMapId() == 0 || clanPc.getMapId() == 4 || clanPc.getMapId() == 304)
						&& castle_area == false) {
					L1Teleport.teleport(pc, clanPc.getX(), clanPc.getY(), clanPc.getMapId(), 5, true);
				} else if (clanPc.getMapId() == 99 || clanPc.getMapId() == 200 || clanPc.getMapId() == 52 || clanPc.getMapId() == 64 
						|| clanPc.getMapId() == 15 || clanPc.getMapId() == 29  || clanPc.getMapId() == 29
						|| clanPc.getMapId() == 110 || clanPc.getMapId() == 120  // oman
						|| clanPc.getMapId() == 130 || clanPc.getMapId() == 140
						|| clanPc.getMapId() == 150 || clanPc.getMapId() == 160
						|| clanPc.getMapId() == 170 || clanPc.getMapId() == 180
						|| clanPc.getMapId() == 190 || clanPc.getMapId() == 200
						|| clanPc.getMapId() == 530 || clanPc.getMapId() == 531  // lastabard 4F
						|| clanPc.getMapId() == 532 || clanPc.getMapId() == 533 
						|| clanPc.getMapId() == 534 || clanPc.getMapId() == 535
						|| clanPc.getMapId() == 603  // 발록방
						|| clanPc.getMapId() == 522 || clanPc.getMapId() == 523 || clanPc.getMapId() == 524  // 그림자신전
						|| clanPc.getMapId() == 5167 ){ // 악마왕의 영토
					pc.sendPackets(new S_ServerMessage(547));
				} else {
					pc.sendPackets(new S_ServerMessage(547));
				}
			} else {
				pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));
				pc.sendPackets(new S_ServerMessage(647));
			}
		}
	}
}
