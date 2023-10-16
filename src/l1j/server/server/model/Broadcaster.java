package l1j.server.server.model;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.ServerBasePacket;

public class Broadcaster {
	/**
	 * 캐릭터의 가시 범위에 있는 플레이어에, 패킷을 송신한다.
	 * 
	 * @param packet
	 *            송신하는 패킷을 나타내는 ServerBasePacket 오브젝트.
	 */
	public static void broadcastPacket(L1Character cha, ServerBasePacket packet) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(cha)) {
			pc.sendPackets(packet);
		}
	}

	/**
	 * 캐릭터의 가시 범위에 있는 플레이어에, 패킷을 송신한다.  다만 타겟의 화면내에는 송신하지 않는다.
	 * 
	 * @param packet
	 *            송신하는 패킷을 나타내는 ServerBasePacket 오브젝트.
	 */
	public static void broadcastPacketExceptTargetSight(L1Character cha, ServerBasePacket packet, L1Character target) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayerExceptTargetSight(cha, target)) {
			pc.sendPackets(packet);
		}
	}

	/**
	 * 캐릭터의 50 매스 이내에 있는 플레이어에, 패킷을 송신한다.
	 * 
	 * @param packet
	 *            송신하는 패킷을 나타내는 ServerBasePacket 오브젝트.
	 */
	public static void wideBroadcastPacket(L1Character cha, ServerBasePacket packet) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(cha,	50)) {
			pc.sendPackets(packet);
		}
	}
}
