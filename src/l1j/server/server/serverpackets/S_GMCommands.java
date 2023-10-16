
package l1j.server.server.serverpackets;

import l1j.server.server.Opcodes;

public class S_GMCommands extends ServerBasePacket {

	private static final String S_GMCommands = "[C] S_GMCommands";

	private byte[] _byte = null;

	public S_GMCommands(int number) {
		buildPacket(number);
	}

	private void buildPacket(int number) {
		writeC(Opcodes.S_OPCODE_BOARDREAD);
		writeD(number);//�ѹ�
		writeS(" �̼��Ǿ� ");//�۾���
		writeS("��ڸ�ɾ�");//��¥?
		writeS("");//����?
		writeS(".����.�����߰�.�������.�� \n" +
				"�˻�.���ε�.��������.��Ȱ\n" +
				"�����з�.�����߹�.�߹�.�׾�\n" +
				"�������.��ġ.����.���� \n" +
				"����.����.����.����.�ӵ�\n" +
				"����.�Ƶ���.������ \n" +
				"����.ä��.ä�� .ä��Ǯ�� \n" +
				"����.û��.����.�κ�����\n" +
				"��ȯ.��ȯ.���.��ų������\n" +
				"�̵�.��ġ.��ü��ȯ.��ü����\n" +
				"����.�ǹ�.����.����.������\n" +				
				"�����߹�.���� .����  \n" +
				"����ä��.�������� .�տ�\n" +
				"�Ǹ��� .�ϴü� .�ҷ��� \n" +
				"���ݰŷ�.�����̺�Ʈ.����\n" );

	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	public String getType() {
		return S_GMCommands;
	}
}

