package l1j.server.server.model.skill.skills;

import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1PcInstance;

public class HorrorOfDeath {

	public static void runSkill(L1Character cha) {
		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			pc.getAbility().addAddedStr((byte) -10);
			pc.getAbility().addAddedInt((byte) -10);
		}
	}
}
