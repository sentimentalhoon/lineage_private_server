
package l1j.server.server.serverpackets;

import l1j.server.server.Opcodes; 

// ���� ������ �غ�.
public class S_BaphoIcon extends ServerBasePacket{

	private byte[] _byte = null;
	
	
	public S_BaphoIcon(int type, boolean show){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(0x72);
		writeD(type);
		writeD(show ? 0x01 : 0x00);
	}


	@Override
	public byte[] getContent() {
		return getBytes();
	}
}