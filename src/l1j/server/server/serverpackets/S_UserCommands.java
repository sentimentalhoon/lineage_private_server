
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
		writeD(number);//넘버
		writeS(" 미소피아 ");//글쓴이?
		writeS(" 명령어 설명 ");//날짜?
		writeS("");//제목?
		writeS("\n무인상점은 그냥 시장에서 \n" + 	
				"상점을 켜고 리스하면 됩니다.\n" +	
				".. = 화면렉풀기\n" +	
				".원격교환.루팅멘트 [켬/끔]\n" +		
				".합치기 [아덴나눠진거합칠때]\n" +		
				".내정보(케릭정보).입장시간\n" +
				".나이 (본인 나이 등록) .이름\n" +
				".조사 .드랍 .몹드랍 .버프 .랭킹\n" +						
				".암호변경 [변경하실암호]\n" +				
				".퀴즈인증 [설정하신 퀴즈입력]\n" +
				".퀴즈설정 [아이디] [퀴즈입력]\n" +
				".홍보내역 .홍보정산 [팝린홍보]\n" +
				".판매 .구입 .취소 [위탁판구매]\n" +
				".이동 .귀환\n" +
				".척살 .수배 [캐릭명]\n\n");

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

