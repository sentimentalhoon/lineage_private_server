package l1j.server.server.model;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.ServerBasePacket;

public class Broadcaster {
	/**
	 * ĳ������ ���� ������ �ִ� �÷��̾, ��Ŷ�� �۽��Ѵ�.
	 * 
	 * @param packet
	 *            �۽��ϴ� ��Ŷ�� ��Ÿ���� ServerBasePacket ������Ʈ.
	 */
	public static void broadcastPacket(L1Character cha, ServerBasePacket packet) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(cha)) {
			pc.sendPackets(packet);
		}
	}

	/**
	 * ĳ������ ���� ������ �ִ� �÷��̾, ��Ŷ�� �۽��Ѵ�.  �ٸ� Ÿ���� ȭ�鳻���� �۽����� �ʴ´�.
	 * 
	 * @param packet
	 *            �۽��ϴ� ��Ŷ�� ��Ÿ���� ServerBasePacket ������Ʈ.
	 */
	public static void broadcastPacketExceptTargetSight(L1Character cha, ServerBasePacket packet, L1Character target) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayerExceptTargetSight(cha, target)) {
			pc.sendPackets(packet);
		}
	}

	/**
	 * ĳ������ 50 �Ž� �̳��� �ִ� �÷��̾, ��Ŷ�� �۽��Ѵ�.
	 * 
	 * @param packet
	 *            �۽��ϴ� ��Ŷ�� ��Ÿ���� ServerBasePacket ������Ʈ.
	 */
	public static void wideBroadcastPacket(L1Character cha, ServerBasePacket packet) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(cha,	50)) {
			pc.sendPackets(packet);
		}
	}
}
