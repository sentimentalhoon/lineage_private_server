
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
		writeD(number);//넘버
		writeS(" 미소피아 ");//글쓴이
		writeS("운영자명령어");//날짜?
		writeS("");//제목?
		writeS(".렙업.계정추가.비번변경.뻥 \n" +
				"검사.리로드.레벨제한.부활\n" +
				"계정압류.영구추방.추방.죽어\n" +
				"밴아이피.배치.가라.감옥 \n" +
				"지옥.버프.변신.레벨.속도\n" +
				"설문.아데나.아이템 \n" +
				"선물.채팅.채금 .채금풀기 \n" +
				"셋팅.청소.날씨.로봇상점\n" +
				"소환.귀환.출두.스킬마스터\n" +
				"이동.위치.전체소환.전체선물\n" +
				"누구.피바.감시.투명.불투명\n" +				
				"무인추방.서먼 .네임  \n" +
				"영자채팅.상점저장 .균열\n" +
				"악마왕 .하늘성 .할로윈 \n" +
				"원격거래.생일이벤트.정보\n" );

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

