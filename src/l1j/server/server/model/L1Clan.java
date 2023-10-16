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
package l1j.server.server.model;

import java.util.ArrayList;
import java.util.logging.Logger;

import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Alliance;

public class L1Clan {
	static public class ClanMember {
		public String name;
		public int rank;

		public ClanMember(String name, int rank) {
			this.name = name;
			this.rank = rank;
		}
	}

	public static final int CLAN_RANK_PROBATION = 1;
	public static final int CLAN_RANK_PUBLIC = 2;
	public static final int CLAN_RANK_GUARDIAN = 3;
	public static final int CLAN_RANK_PRINCE = 4;

	@SuppressWarnings("unused")
	private static final Logger _log = Logger.getLogger(L1Clan.class.getName());

	private int _clanId;
	private String _clanName;
	private int _leaderId;
	private String _leaderName;
	private int _castleId;
	private int _houseId;

	private ArrayList<ClanMember> clanMemberList = new ArrayList<ClanMember>();


	public ArrayList<ClanMember> getClanMemberList() {
		return clanMemberList;
	}

	public void addClanMember(String name, int rank) {
		clanMemberList.add(new ClanMember(name, rank));
	}

	public void removeClanMember(String name) {
		for(int i = 0 ; i < clanMemberList.size() ; i++) {
			if(clanMemberList.get(i).name.equals(name)) {
				clanMemberList.remove(i);
				break;
			}
		}
	}

	public int getClanId() {	return _clanId;	}
	public void setClanId(int clan_id) {	_clanId = clan_id;	}

	public String getClanName() {	return _clanName;	}
	public void setClanName(String clan_name) {	_clanName = clan_name;	}

	public int getLeaderId() {	return _leaderId;	}
	public void setLeaderId(int leader_id) {	_leaderId = leader_id;	}

	public String getLeaderName() {	return _leaderName;	}
	public void setLeaderName(String leader_name) {	_leaderName = leader_name;	}

	public int getCastleId() {	return _castleId;	}
	public void setCastleId(int hasCastle) {	_castleId = hasCastle;	}

	public int getHouseId() {	return _houseId;	}
	public void setHouseId(int hasHideout) {	_houseId = hasHideout;	}

	private L1Alliance alliance = null;
	public L1Alliance getAlliance(){	return alliance;	}
	public void setAlliance(L1Alliance alliance){	this.alliance = alliance;	}

	public boolean AllianceCK(L1Clan clan){
		if(this == clan || alliance == null) return false;
		if(alliance.CheckAlliance(clan)) return true;
		else return false;
	}

	private ArrayList<L1PcInstance> ClanListener = new ArrayList<L1PcInstance>();
	public void addListener(L1PcInstance cha){
		if (!ClanListener.contains(cha)) {
			ClanListener.add(cha);
		}
	}
	public void removeListener(L1PcInstance cha){
		if (ClanListener.contains(cha)) {
			ClanListener.remove(cha);
		}
	}
	public boolean CheckListener(L1PcInstance cha){
		if(ClanListener.contains(cha)) {
			return true;
		} else {
			return false;
		}
	}
	public L1PcInstance[] getListeners(){
		return (L1PcInstance[]) ClanListener.toArray(new L1PcInstance[ClanListener.size()]);
	}

	// 온라인중의 혈원수
	public int getOnlineMemberCount() {
		int count = 0;
		for(int i = 0 ; i < clanMemberList.size() ; i++) {
			if (L1World.getInstance().getPlayer(clanMemberList.get(i).name) != null) {
				count++;
			}
		}
		return count;
	}

	//	 온라인중 혈원 인스턴스 리스트
	public L1PcInstance[] getOnlineClanMember() {
		ArrayList<L1PcInstance> onlineMembers = new ArrayList<L1PcInstance>();
		L1PcInstance pc = null;
		for(int i = 0 ; i < clanMemberList.size() ; i++) {
			pc = L1World.getInstance().getPlayer(clanMemberList.get(i).name);
			if (pc != null && !onlineMembers.contains(pc)) {
				onlineMembers.add(pc);
			}
		}
		return onlineMembers.toArray(new L1PcInstance[onlineMembers.size()]);
	}

	// 전체 혈원 네임 리스트
	public String getAllMembersFP() {
		String result = "";
		for(int i = 0 ; i < clanMemberList.size() ; i++) {
			result = result + clanMemberList.get(i).name + " ";
		}
		return result;
	}

	// 온라인중의 혈원 네임 리스트
	public String getOnlineMembersFP() {
		String result = "";
		for(int i = 0 ; i < clanMemberList.size() ; i++) {
			if (L1World.getInstance().getPlayer(clanMemberList.get(i).name) != null) {
				result = result + clanMemberList.get(i).name + " ";
			}
		}
		return result;
	}

	public void announcement_message(L1PcInstance listener, int type, String msg1, String msg2){
		L1PcInstance[] members = getOnlineClanMember();
		for(int i = 0; i < members.length ; i++){
			if (members[i] == listener) {
				continue;
			}
			if (msg2 == null) {
				members[i].sendPackets(new S_ServerMessage(type, msg1));
			} else {
				members[i].sendPackets(new S_ServerMessage(type, msg1, msg2));
			}
		}	
	}

	public void announcement_message(L1PcInstance listener, int type, String msg1, String msg2, String msg3){
		L1PcInstance[] members = getOnlineClanMember();
		for(int i = 0; i < members.length ; i++){
			if (members[i] == listener) {
				continue;
			}
			members[i].sendPackets(new S_ServerMessage(type, msg1, msg2, msg3));
		}	
	}

	/*
	// 오프라인중의 혈원 네임 리스트
	public String getOfflineMembersFP() {
		String result = "";
		for (String name : membersNameList) {
			if (L1World.getInstance().getPlayer(name) == null) {
				result = result + name + " ";
			}
		}
		return result;
	}

	// 오프라인중의 혈원 네임 리스트
	public String getOfflineMemberName(int c) {
		int count = 0;
		for (String name : membersNameList) {
			if (L1World.getInstance().getPlayer(name) == null) {
				if(c == count)
					return name;
				count++;
			}
		}
		return null;
	}

	// 온라인중의 혈원 랭크
	public int getOnlineMemberRank(String member) {
		String result = "";
		L1PcInstance pc = null;
		for (String name : membersNameList) {
			pc = L1World.getInstance().getPlayer(name);
			if (pc != null) {
				result = result + getRankString(pc) + " ";
			}
		}
		return -1;
	}

	// 오프라인중의 혈원 랭크
	public int getOfflineMemberRank(String member) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT ClanRank FROM characters WHERE char_name=?");
			pstm.setString(1, member);
			rs = pstm.executeQuery();
			if(!rs.next()) return -1;

			return rs.getInt("ClanRank");

		} catch (SQLException e) {
			_log.warning("could not check existing charname:" + e.getMessage());
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return -1;
	}

	public String getOnlineMembersFPWithRank() {
		String result = "";
		L1PcInstance pc = null;
		for (String name : membersNameList) {
			pc = L1World.getInstance().getPlayer(name);
			if (pc != null) {
				result = result + name + getRankString(pc) + " ";
			}
		}
		return result;
	}

	public String getAllMembersFPWithRank() {
		String result = "";
		try {
			L1PcInstance pc = null;
			for (String name : membersNameList) {
				pc = CharacterTable.getInstance()
						.restoreCharacter(name);
				if (pc != null) {
					result = result + name + getRankString(pc) + " ";
				}
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return result;
	}

	private String getRankString(L1PcInstance pc) {
		String rank = "";
		if (pc != null) {
			if (pc.getClanRank() == CLAN_RANK_PROBATION) {
				rank = "[견습기사]";
			} else if (pc.getClanRank() == CLAN_RANK_PUBLIC) {
				rank = "[일반기사]";
			} else if (pc.getClanRank() == CLAN_RANK_GUARDIAN) {
				rank = "[수호기사]";
			} else if (pc.getClanRank() == CLAN_RANK_PRINCE) {
				rank = "[혈맹군주]";
			} else {
				rank = "";
			}
		}
		return rank;
	}*/

}
