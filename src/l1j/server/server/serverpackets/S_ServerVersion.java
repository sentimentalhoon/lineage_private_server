/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.serverpackets;
import l1j.server.server.Opcodes;
import l1j.server.server.model.gametime.GameTimeClock;
public class S_ServerVersion extends ServerBasePacket {
	private static final String S_SERVER_VERSION = "[S] ServerVersion";
	public S_ServerVersion() {
		int time = GameTimeClock.getInstance().getGameTime().getSeconds();
		time = time - (time % 300);

		writeC(Opcodes.S_OPCODE_SERVERVERSION);
		writeC(0x00); // must be
		writeC(0xc8); // low version
		writeD(0x00a8a319); // serverver 19 a3 a8 00
		writeD(0x00a8a067); // cache version 67 a0 a8 00
		writeD(0x77de1136); // auth ver 36 11 de 77
		writeD(0x00a8794d); // npc ver 4d 79 a8 00
		writeD(0x69f40b4c); // 로그인시의 시간 설정 4c 0b f4 69
		writeC(0x00); // unk 1
		writeC(0x00); // unk 2
		writeC(0x00); // language korean
		writeD(time);  
	}
	@Override
	public byte[] getContent() {
		return getBytes();
	}
	@Override
	public String getType() {
		return S_SERVER_VERSION;
	}
}