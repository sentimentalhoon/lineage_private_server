

package l1j.server.server.clientpackets;

import server.LineageClient;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_HotelEnter extends ClientBasePacket {

	private static final String C_ENTER_PORTAL = "[C] C_EnterPortal";

	public C_HotelEnter(byte abyte0[], LineageClient client)
			throws Exception {
		super(abyte0);
/*		L1PcInstance pc = client.getActiveChar();

		L1WorldMap.getInstance().cloneMap(16384, (short)16385);

		pc.setX(32743);
		pc.setY(32803);
		pc.setMap((short)16385);
		pc.sendPackets(new S_CloseList(5345345));
		pc.sendPackets(new S_SkillSound(pc.getId(), 169));
		pc.sendPackets(new S_MapID(16384, false));
		pc.sendPackets(new S_OwnCharPack(pc));
		Broadcaster.broadcastPacket(pc, new S_OtherCharPacks(pc));
		pc.updateObject();*/
	}

	@Override
	public String getType() {
		return C_ENTER_PORTAL;
	}
}