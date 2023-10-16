
package l1j.server.server.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.Opcodes;
import l1j.server.server.utils.SQLUtil;

public class S_UserCommands extends ServerBasePacket {

	private static final String S_UserCommands = "[C] S_UserCommands";

	private static Logger _log = Logger.getLogger(S_UserCommands.class.getName());

	private byte[] _byte = null;

	public S_UserCommands(int number) {
		buildPacket(number);
	}

	private void buildPacket(int number) {
		writeC(Opcodes.S_OPCODE_BOARDREAD);
		writeD(number);//�ѹ�
		writeS(" �̼��Ǿ� ");//�۾���?
		writeS(" ��ɾ� ���� ");//��¥?
		writeS("");//����?
		writeS("\n���λ����� �׳� ���忡�� \n" + 	
				"������ �Ѱ� �����ϸ� �˴ϴ�.\n" +	
				".. = ȭ�鷺Ǯ��\n" +	
				".���ݱ�ȯ.���ø�Ʈ [��/��]\n" +		
				".��ġ�� [�Ƶ�����������ĥ��]\n" +		
				".������(�ɸ�����).����ð�\n" +
				".���� (���� ���� ���) .�̸�\n" +
				".���� .��� .����� .���� .��ŷ\n" +						
				".��ȣ���� [�����ϽǾ�ȣ]\n" +				
				".�������� [�����Ͻ� �����Է�]\n" +
				".����� [���̵�] [�����Է�]\n" +
				".ȫ������ .ȫ������ [�˸�ȫ��]\n" +
				".�Ǹ� .���� .��� [��Ź�Ǳ���]\n" +
				".�̵� .��ȯ\n" +
				".ô�� .���� [ĳ����]\n\n");

	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	public String getType() {
		return S_UserCommands;
	}
}

