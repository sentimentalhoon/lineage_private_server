package l1j.server.server.templates;

import java.util.ArrayList;

import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_ServerMessage;

public class L1Alliance {
	private int allianceId = 0;
	private ArrayList<L1Clan> Clans = new ArrayList<L1Clan>();
	
	public int get_allianceId(){
		return allianceId;
	}
	
	public void set_allianceId(int allianceId){
		this.allianceId = allianceId;
	}
	
	public void add_clan(L1Clan clan){
		if (!Clans.contains(clan)) {
			Clans.add(clan);
		}
	}
	
	public void removeClan(L1Clan clan){
		if (Clans.contains(clan)) {
			Clans.remove(clan);
		}
	}
	
	public L1Clan[] getClans(){
		return (L1Clan[]) Clans.toArray(new L1Clan[Clans.size()]);
	}
	
	public boolean CheckAlliance(L1Clan clan){
		if (Clans.contains(clan)) {
			return true;
		} else {
			return false;
		}
	}
	
	public int get_size(){
		return Clans.size();
	}
	
	public void AllianceMessage(int messageNum, String msg1, String msg2){
		L1Clan[] clan = getClans();		
		for(int j = 0; j < clan.length; j++){
			L1PcInstance[] members = clan[j].getOnlineClanMember();
			for(int i = 0; i < members.length ; i++){
				if(msg2 == null) {
					members[i].sendPackets(new S_ServerMessage(messageNum, msg1));
				} else {
					members[i].sendPackets(new S_ServerMessage(messageNum, msg1, msg2));
				}
			}	
		}
	}
	
	public void AllianceChat(L1PcInstance pc, String chatText){
		L1Clan[] clan = getClans();		
		ArrayList<L1PcInstance> Listeners = new ArrayList<L1PcInstance>();
		S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_NORMALCHAT, 15);
		for(int j = 0; j < clan.length; j++){
			L1PcInstance[] members = clan[j].getOnlineClanMember();
			for(int i = 0; i < members.length ; i++){
				members[i].sendPackets(s_chatpacket);
			}	
			members = clan[j].getListeners();
			for(int i=0 ; i < members.length ; i++){ 
				if (members[i] == null || members[i].getNetConnection() == null) {
					clan[j].removeListener(members[i]);
					continue;
				}
				if (!Listeners.contains(members[i])) {
					members[i].sendPackets(s_chatpacket);
					Listeners.add(members[i]);
				}
			}
		}
	}
	
	public void delete(){
		try{
		L1Clan[] clan = getClans();	
		for(int j = 0; j < clan.length; j++){
			if(clan[j] != null){
				ClanTable.getInstance().ChangeAlliance(clan[j], null);
			}
		}
		ClanTable.getInstance().RemoveAlliance(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
}
