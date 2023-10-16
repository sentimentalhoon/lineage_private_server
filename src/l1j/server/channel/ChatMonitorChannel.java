package l1j.server.channel;

import java.util.ArrayList;

import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ChatPacket;

public class ChatMonitorChannel {
	public static final int CHAT_MONITOR_CLAN = 75;
	public static final int CHAT_MONITOR_WHISPER = 76;
	public static final int CHAT_MONITOR_PARTY = 77;
	public static final int CHAT_MONITOR_GM = 78;
	
	private static ChatMonitorChannel instance;
	private ArrayList<L1PcInstance> listenerList;
	
	public static ChatMonitorChannel getInstance() {
		if(instance == null) {
			synchronized(ChatMonitorChannel.class) {
				if(instance == null) {
					instance = new ChatMonitorChannel();
				}
			}
		}
		
		return instance;
	}
	
	private ChatMonitorChannel() {
		listenerList = new ArrayList<L1PcInstance>();
	}
	
	public void join(L1PcInstance pc) {
		if(!pc.isGm())
			return;
		
		listenerList.add(pc);
	}

	public void out(L1PcInstance pc) {
		if(!pc.isGm())
			return;
		
		listenerList.remove(pc);
	}
	
	public void sendMsg(int type, String chatText, L1PcInstance pc) {
		S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_MSG, type);
		
		for(int i = 0 ; i < listenerList.size() ; i++) {
			listenerList.get(i).sendPackets(s_chatpacket);
		}
	}
}
