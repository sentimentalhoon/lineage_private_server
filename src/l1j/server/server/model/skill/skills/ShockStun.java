package l1j.server.server.model.skill.skills;

import java.util.Random;

import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1EffectSpawn;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.serverpackets.S_Paralysis;

public class ShockStun {

	public static void runSkill(Random random, L1Character cha, int _shockStunDuration) {
		int[] stunTimeArray = { 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000 };

		int rnd = random.nextInt(stunTimeArray.length);
		_shockStunDuration = stunTimeArray[rnd];

		L1EffectSpawn.getInstance()
		.spawnEffect(81162, _shockStunDuration, cha.getX(), cha.getY(), cha.getMapId());
		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_STUN, true));
		} else if (cha instanceof L1MonsterInstance
				|| cha instanceof L1SummonInstance
				|| cha instanceof L1PetInstance) {
			L1NpcInstance npc = (L1NpcInstance) cha;
			npc.setParalyzed(true);
			npc.setParalysisTime(_shockStunDuration);
		}
	}
}
