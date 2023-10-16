
package l1j.server.server.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.Opcodes;
import l1j.server.server.utils.SQLUtil;

public class S_UserCommands2 extends ServerBasePacket {

	private static final String S_UserCommands2 = "[C] S_UserCommands2";

	private static Logger _log = Logger.getLogger(S_UserCommands2.class.getName());

	private byte[] _byte = null;

	public S_UserCommands2(int number) {
		buildPacket(number);
	}

	private void buildPacket(int number) {
		writeC(Opcodes.S_OPCODE_BOARDREAD);
		writeD(number);//넘버
		writeS(" 운영자 ");//글쓴이?
		writeS(" 캐릭터교환 설명 ");//날짜?
		writeS("");//제목?
		writeS("\n === 케릭터교환 설명서 ===\n" +
				 "\n" +
				 " 캐릭터교환표는 캐릭을\n" +
				 " 판매하려는 유저가 표를\n" +
				 " 구입하셔야합니다.\n" +
				 " 자기렙이 70이상이라면.\n" +
				 " 상급캐릭교환표를 구입후\n" +
				 " 구입하려는 유저에게 교환창\n" +
				 " 으로 올리시면됩니다.\n" +
				 " 구입하려는 유저가 케릭을 \n" +
				 " 구입하기위해 올린아이템은\n" +
				 " 판매자의 창고로 저장됩니다.\n\n" +
				 " 거래가성립되면 서로팅기면서\n" +
				 " 증표를올린케릭은 구입자의\n" +
				 " 계정으로 넘어가게 됩니다.\n" +
				 " (서로 케릭 맞교환도 가능)");
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	public String getType() {
		return S_UserCommands2;
	}
}

