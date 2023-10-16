package l1j.server.server.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l1j.server.server.model.Instance.L1PcInstance;

public class NearObjects {
	public List<L1Object> knownObjects = new CopyOnWriteArrayList<L1Object>();
	public List<L1PcInstance> knownPlayer = new CopyOnWriteArrayList<L1PcInstance>();

	/**
	 * ������ ������Ʈ��, ĳ���Ͱ� �ν��ϰ� ����� �����ش�.
	 * 
	 * @param obj
	 *            �����ϴ� ������Ʈ.
	 * @return ������Ʈ�� ĳ���Ͱ� �ν��ϰ� ������ true, �ϰ� ���� ������ false.  �ڱ� �ڽſ� ���ؼ��� false�� �����ش�.
	 */
	public boolean knownsObject(L1Object obj) {
		return knownObjects.contains(obj);
	}


	/**
	 * ĳ���Ͱ� �ν��ϰ� �ִ� ��� ������Ʈ�� �����ش�.
	 * 
	 * @return ĳ���Ͱ� �ν��ϰ� �ִ� ������Ʈ�� ��Ÿ���� List<L1Object>.
	 */
	public List<L1Object> getKnownObjects() {
		return knownObjects;
	}

	/**
	 * ĳ���Ͱ� �ν��ϰ� �ִ� ��� �÷��̾ �����ش�.
	 * 
	 * @return ĳ���Ͱ� �ν��ϰ� �ִ� ������Ʈ�� ��Ÿ���� List<L1PcInstance>
	 */
	public List<L1PcInstance> getKnownPlayers() {
		return knownPlayer;
	}

	/**
	 * ĳ���Ϳ�, ���Ӱ� �ν��ϴ� ������Ʈ�� �߰��Ѵ�.
	 * 
	 * @param obj
	 *            ���Ӱ� �ν��ϴ� ������Ʈ.
	 */
	public void addKnownObject(L1Object obj) {
		if (!knownObjects.contains(obj)) {
			knownObjects.add(obj);
			if (obj instanceof L1PcInstance) {
				knownPlayer.add((L1PcInstance) obj);
			}
		}
	}

	/**
	 * ĳ���ͷκ���, �ν��ϰ� �ִ� ������Ʈ�� �����Ѵ�.
	 * 
	 * @param obj
	 *            �����ϴ� ������Ʈ.
	 */
	public void removeKnownObject(L1Object obj) {
		knownObjects.remove(obj);
		if (obj instanceof L1PcInstance) {
			knownPlayer.remove(obj);
		}
	}

	/**
	 * ĳ���ͷκ���, ��� �ν��ϰ� �ִ� ������Ʈ�� �����Ѵ�.
	 */
	public void removeAllKnownObjects() {
		knownObjects.clear();
		knownPlayer.clear();
	}
}