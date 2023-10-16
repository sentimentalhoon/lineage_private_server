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

import l1j.server.Config;
import l1j.server.server.Opcodes;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_ChatPacket extends ServerBasePacket {

	private static final String _S__1F_NORMALCHATPACK = "[S] S_ChatPacket";
	private byte[] _byte = null;

	public S_ChatPacket(String targetname, String chat, int opcode) {
		writeC(opcode);
		writeC(9);
		writeS("-> (" + targetname + ") " + chat);
	}

	// 매니저용 귓말
	public S_ChatPacket(String from , String chat) {
		writeC(Opcodes.S_OPCODE_WHISPERCHAT);
		writeS(from);
		writeS(chat);
	}

	public S_ChatPacket(L1PcInstance pc, String chat, int opcode, int type) {
		writeC(opcode);

		switch(type){
		case 0:
			writeC(type);
			writeD(pc.getId());
			writeS(pc.getName() + ": " + chat);
			break;
		case 2:
			writeC(type);
			if (pc.isInvisble()) {
				writeD(0);
			} else {
				writeD(pc.getId());
			}
			writeS("<" + pc.getName() + "> " + chat);
			writeH(pc.getX());
			writeH(pc.getY());
			break;
		case 3:
			if (pc.isGm() == true) {
				writeC(9);
				writeS("[******] " + chat);

				if(Config.isGmchat)
					L1World.getInstance().broadcastPacketToAll(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "[******] "  + chat));

			}
			else if (pc.getAccessLevel() == 1) {
				writeC(type);
				writeS("\\fY[홍보요원] " + chat);
			} else {
				writeC(type);
				writeS("[" + pc.getName() + "] " + chat);
			}
			break;
		case 4:
			writeC(type);
			//writeS("{" + pc.getName() +"} " + chat); //원본
			if (pc.getAge() == 0){ // 나이
				writeS("{" + pc.getName() + "} " + chat);
			}else{
				writeS("[" + pc.getUserName() + "]" + "{" + pc.getName() + "("+ pc.getAge()+")"+"} " + chat);
			}
			break;

		case 9:
			writeC(type);
			writeS("-> (" + pc.getName() + ") " + chat);
			break;
		case 11:
			writeC(type);
			writeS("(" + pc.getName() + ") " + chat);
			break;
		case 12:
			writeC(type);
			writeS("[" + pc.getName() + "] " + chat);
			break;
		case 13:
			writeC(type);
			writeS("{{" + pc.getName() + "}} " + chat);
			break;
		case 14:
			writeC(type);
			writeD(pc.getId());
			writeS("(" + pc.getName() + ") " + chat);
			break;
		case 15:
			writeC(type);
			writeD(pc.getId());
			writeS("["+pc.getClan().getClanName()+"][" + pc.getName() + "] " + chat);
			break;
		case 16:
			writeS(pc.getName());
			writeS(chat);
			break;
		case 99:
			writeC(3);
			writeS("\\fY" + pc.getName() + "님이 " + chat+"님을 눕혔습니다.");
			break;
		}
		/*
		// 모니터링을 위해 추가
		switch (type) {
		case ChatMonitorChannel.CHAT_MONITOR_CLAN: 
			writeC(opcode);
			writeC(type);
			writeS("[" + pc.getClanname() + "]{" + pc.getName() + "} " + chat);
			break;
		case ChatMonitorChannel.CHAT_MONITOR_PARTY: 
			writeC(opcode);
			writeC(type);
			writeS(pc.getName() + ": " + chat);
			break;
		case ChatMonitorChannel.CHAT_MONITOR_WHISPER: 
			writeC(opcode);
			writeC(type);
			writeS(pc.getName() + " " + chat);
			break;
		case ChatMonitorChannel.CHAT_MONITOR_GM:
			writeC(opcode);
			writeC(type);
			if(pc != null)
				writeS(pc.getName() + "에게 : " + chat);
			else
				writeS(chat);
			break;
		}
		 */
	}

	@Override
	public byte[] getContent() {
		if (null == _byte) {
			_byte = _bao.toByteArray();
		}
		return _byte;
	}
	@Override
	public String getType() {
		return _S__1F_NORMALCHATPACK;
	}

}