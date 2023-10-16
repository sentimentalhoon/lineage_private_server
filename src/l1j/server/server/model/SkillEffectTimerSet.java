package l1j.server.server.model;

import java.util.HashMap;
import java.util.Map;

import l1j.server.server.model.skill.L1SkillTimer;
import l1j.server.server.model.skill.L1SkillTimerCreator;

public class SkillEffectTimerSet {
	private final Map<Integer, L1SkillTimer> _skillEffect = new HashMap<Integer, L1SkillTimer>();
	private L1Character cha;
	
	public SkillEffectTimerSet(L1Character cha) {
		this.cha = cha;
	}

	/**
	 * ĳ���Ϳ�, ���Ӱ� ��ų ȿ���� �߰��Ѵ�.
	 * 
	 * @param skillId
	 *            �߰��ϴ� ȿ���� ��ų ID.
	 * @param timeMillis
	 *            �߰��ϴ� ȿ���� ���� �ð�. ������ ���� 0.
	 */
	private void addSkillEffect(int skillId, int timeMillis) {
		L1SkillTimer timer = null;
		if (0 < timeMillis) {
			timer = L1SkillTimerCreator.create(cha, skillId, timeMillis);
			timer.begin();
		}
		_skillEffect.put(skillId, timer);
	}


	/**
	 * ĳ���Ϳ�, ��ų ȿ���� �����Ѵ�. <br>
	 * �ߺ� �ϴ� ��ų�� ���� ����, ���Ӱ� ��ų ȿ���� �߰��Ѵ�. <br>
	 * �ߺ� �ϴ� ��ų�� �ִ� ����, ������ ȿ�� �ð��� �Ķ������ ȿ�� �ð��� �� (��)���� �켱�� �����Ѵ�.
	 * 
	 * @param skillId
	 *            �����ϴ� ȿ���� ��ų ID.
	 * @param timeMillis
	 *            �����ϴ� ȿ���� ���� �ð�. ������ ���� 0.
	 */
	public void setSkillEffect(int skillId, int timeMillis) {
		if (hasSkillEffect(skillId)) {
			int remainingTimeMills = getSkillEffectTimeSec(skillId) * 1000;

			if (remainingTimeMills >= 0	&& (remainingTimeMills < timeMillis || timeMillis == 0)) {
				killSkillEffectTimer(skillId);
				addSkillEffect(skillId, timeMillis);
			}
		} else {
			addSkillEffect(skillId, timeMillis);
		}
	}


	/**
	 * ĳ���ͷκ���, ��ų ȿ���� �����Ѵ�.
	 * 
	 * @param skillId
	 *            �����ϴ� ȿ���� ��ų ID
	 */
	public void removeSkillEffect(int skillId) {
		L1SkillTimer timer = _skillEffect.remove(skillId);
		if (timer != null) {
			timer.end();
		}
	}

	/**
	 * ĳ���ͷκ���, ��ų ȿ���� Ÿ�̸Ӹ� �����Ѵ�.  ��ų ȿ���� �������� �ʴ´�.
	 * 
	 * @param skillId
	 *            �����ϴ� Ÿ�̸��� ��ų ID
	 */
	public void killSkillEffectTimer(int skillId) {
		L1SkillTimer timer = _skillEffect.remove(skillId);
		if (timer != null) {
			timer.kill();
		}
	}

	/**
	 * ĳ���ͷκ���, ��� ��ų ȿ�� Ÿ�̸Ӹ� �����Ѵ�. ��ų ȿ���� �������� �ʴ´�.
	 */
	public void clearSkillEffectTimer() {
		for (L1SkillTimer timer : _skillEffect.values()) {
			if (timer != null) {
				timer.kill();
			}
		}
		_skillEffect.clear();
	}

	/**
	 * ĳ���Ϳ�, �ش� ��ų ȿ���� �ɷ��ִ��� �˷���
	 * 
	 * @param skillId ��ų ID
	 * @return ���� ȿ���� ������ true, ������ false.
	 */
	public boolean hasSkillEffect(int skillId) {
		return _skillEffect.containsKey(skillId);
	}

	/**
	 * ĳ������ ��ų ȿ���� ���� �ð��� �����ش�.
	 * 
	 * @param skillId
	 *            �����ϴ� ȿ���� ��ų ID
	 * @return ��ų ȿ���� ���� �ð�(��). ��ų�� �ɸ��� ������ ȿ�� �ð��� ������ ���,-1.
	 */
	public int getSkillEffectTimeSec(int skillId) {
		L1SkillTimer timer = _skillEffect.get(skillId);
		if (timer == null) {
			return -1;
		}
		return timer.getRemainingTime();
	}

}
