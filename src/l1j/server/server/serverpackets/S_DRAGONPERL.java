
package l1j.server.server.serverpackets;

import java.util.logging.Logger;

import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1PcInstance;

public class S_DRAGONPERL extends ServerBasePacket {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(S_DRAGONPERL.class.getName());

	private static final String S_DRAGONPERL = "[S] S_DRAGONPERL";
	private byte[] _byte = null;
	
	public S_DRAGONPERL(int i, int type) {
		writeC(Opcodes.S_OPCODE_DRAGONPERL);
		writeD(i); //�ɸ��� ��ü ���̵�
		writeC(type); //1~7 ������ ȿ�� 8 ����
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		}

		return _byte;
	}
@Override
	public String getType() {
		return S_DRAGONPERL;
	}
}
