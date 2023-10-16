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

package l1j.server.server.clientpackets;

import server.LineageClient;
import server.manager.eva;
import l1j.server.Config;
//import l1j.server.channel.ChatMonitorChannel;
import l1j.server.server.Opcodes;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;

//Referenced classes of package l1j.server.server.clientpackets:
//ClientBasePacket

public class C_ChatWhisper extends ClientBasePacket {

	private static final String C_CHAT_WHISPER = "[C] C_ChatWhisper";

	public C_ChatWhisper(byte abyte0[], LineageClient client) throws Exception {
		super(abyte0);
		String targetName = readS();
		String text = readS();
		L1PcInstance whisperFrom = client.getActiveChar();
		// ä�� �������� ���
		if (whisperFrom.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_CHAT_PROHIBITED)) {
			whisperFrom.sendPackets(new S_ServerMessage(242)); // ���� ä�� �������Դϴ�.
			return;
		}
		if (whisperFrom.getLevel() < Config.WHISPER_CHAT_LEVEL) {
			whisperFrom.sendPackets(new S_ServerMessage(404, String.valueOf(Config.WHISPER_CHAT_LEVEL)));
			return;
		}

		if (targetName.equalsIgnoreCase("******")){	
			whisperFrom.sendPackets(new S_SystemMessage("-> (******) "+text));
			eva.writeMessage(7, whisperFrom.getName() + "******" + text + ">");		
			return;
		}
		L1PcInstance whisperTo = L1World.getInstance().getPlayer(targetName);	
				
		// ���忡 ���� ���
		if (whisperTo == null) {
			whisperFrom.sendPackets(new S_ServerMessage(73, targetName)); // \f1%0�� ������ �ϰ� ���� �ʽ��ϴ�.
			return;
		}
		// �ڱ� �ڽſ� ���� wis�� ���
		if (whisperTo.equals(whisperFrom)) {
			return;
		}
		
		// ���ܵǰ� �ִ� ���
		if (whisperTo.getExcludingList().contains(whisperFrom.getName())) {
			whisperFrom.sendPackets(new S_ServerMessage(117, whisperTo.getName())); // %0�� ����� �����߽��ϴ�.
			return;
		}

		if (!whisperTo.isCanWhisper()) {
			whisperFrom.sendPackets(new S_ServerMessage(205, whisperTo.getName()));
			return;
		}

		//ChatLogTable.getInstance().storeChat(whisperFrom, whisperTo, text, 1);
		whisperFrom.sendPackets(new S_ChatPacket(whisperTo, text, Opcodes.S_OPCODE_MSG, 9));
		whisperTo.sendPackets(new S_ChatPacket(whisperFrom, text, Opcodes.S_OPCODE_WHISPERCHAT, 16));
		// monitoring
		// ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_WHISPER, " -> "+whisperTo.getName()+" : "+ text, whisperFrom);
		eva.writeMessage(7, whisperFrom.getName() + " > " +whisperTo.getName() + " : "+ text);		
	}

	@Override
	public String getType() {
		return C_CHAT_WHISPER;
	}
}
