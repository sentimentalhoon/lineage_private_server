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
import l1j.server.server.model.Instance.L1PcInstance;

public class S_Party extends ServerBasePacket {

	private static final String _S_Party = "[S] S_Party";
	
	private byte[] _byte = null;
	
	public S_Party(int type, L1PcInstance pc){
		switch (type) {
		case 0x68:
			newMember(pc);
			break;
		case 0x69:
			oldMember(pc);
			break;
		case 0x6A:
			changeLeader(pc);
		case 0x6e:
			refreshParty(pc);
			break;
		default:
			break;
		}
	}
	
	public S_Party(String htmlid, int objid) {
		buildPacket(htmlid, objid, "", "", 0);
	}

	public S_Party(String htmlid, int objid, String partyname,
			String partymembers) {

		buildPacket(htmlid, objid, partyname, partymembers, 1);
	}

	private void buildPacket(String htmlid, int objid, String partyname,
			String partymembers, int type) {
		writeC(Opcodes.S_OPCODE_SHOWHTML);
		writeD(objid);
		writeS(htmlid);
		writeH(type);
		writeH(0x02);
		writeS(partyname);
		writeS(partymembers);
	}
	
	/* 68 : ó�� ������ ��Ƽ������ �����ش�
	================================================================================
	packetbox :119 ���� :48
	================================================================================
	77 68 01 (��/Ÿ��/�����-�ʴ���Ƽ�������ѻ����) 
	e4 eb 26 0f / 45 76 61 74 65 73 74 00 / 64 / 44 00 00 00 / 1a 80 / ef 7f (obj/name/������/map/x/y)
	b4 45 26 0f / 45 76 61 74 65 73 74 31 31 00 / 64 / 44 00 00 00 / 19 80 / ef 7f (obj/name/������/map/x/y)
	3c*/
	public void newMember(L1PcInstance pc){
		L1PcInstance leader = pc.getParty().getLeader();
		L1PcInstance member[] = pc.getParty().getMembers();
		double nowhp = 0.0d;
		double maxhp = 0.0d;		
		if(pc.getParty() == null){return;}
		else{
			writeC(Opcodes.S_OPCODE_PACKETBOX);		
			writeC(0x68);
			nowhp = leader.getCurrentHp();
			maxhp = leader.getMaxHp();
			writeC(member.length - 1);
			writeD(leader.getId());
			writeS(leader.getName());
			writeC((int)(nowhp/maxhp) * 100);
			writeD(leader.getMapId());
			writeH(leader.getX());
			writeH(leader.getY());
			for (int i = 0, a = member.length ; i < a ; i++){
				if(member[i].getId() == leader.getId() || member[i] == null) continue;
				nowhp = member[i].getCurrentHp();
				maxhp = member[i].getMaxHp();
				writeD(member[i].getId());
				writeS(member[i].getName());
				writeC((int)(nowhp/maxhp) * 100);
				writeD(member[i].getMapId());
				writeH(member[i].getX());
				writeH(member[i].getY());			
			}
			writeC(0x00);
		}		
	}
	/*	69 : ������ �ִ� ��Ƽ������ ���ο� ��Ƽ�������� ����(ù��Ƽ ��������)
	================================================================================
	packetbox :119 ���� :24
	================================================================================
	77 / 69 (��/Ÿ��)
	b4 45 26 0f / 45 76 61 74 65 73 74 31 31 00 / 44 00 00 00 / 19 80 / ef 7f (obj/name/map/x/y)*/
	public void oldMember(L1PcInstance pc){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(0x69);
		writeD(pc.getId());
		writeS(pc.getName());
		writeD(pc.getMapId());
		writeH(pc.getX());
		writeH(pc.getY());		
	}
	/*6a(��Ƽ������)		
	================================================================================
	packetbox :119 ���� :8
	================================================================================
	77 6a (��/Ÿ��)
	b4 45 26 0f (��Ƽ�� �޴� objid)
	dd 03*/	
	public void changeLeader(L1PcInstance pc){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(0x6A);
		writeD(pc.getId());
		writeH(0x0000);
	}
	/*6e(��Ƽ��������Ʈ) 25�ʸ��� �����Ѵ�
	================================================================================
	packetbox :119 ���� :40
	================================================================================
	77 6e 03 (��/Ÿ��/��Ƽ�������)
	8c d3 25 0f / 44 00 00 00 / 19 80 / f0 7f (obj/map/x/y)
	b4 45 26 0f / 44 00 00 00 / 19 80 / ef 7f (obj/map/x/y)
	e4 eb 26 0f / 44 00 00 00 / 1a 80 / ef 7f (obj/map/x/y)
	2e*/
	public void refreshParty(L1PcInstance pc){
		L1PcInstance member[] = pc.getParty().getMembers();		
		if (pc.getParty() == null){return;}		
		else{
			writeC(Opcodes.S_OPCODE_PACKETBOX);
			writeC(0x6E);
			writeC(member.length);
			for(int i = 0, a = member.length ; i < a ; i++){				
				writeD(member[i].getId());
				writeD(member[i].getMapId());
				writeH(member[i].getX());
				writeH(member[i].getY());				
			}
			writeC(0x00);
		}
	}	
	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		}

		return _byte;
	}
	@Override
	public String getType() {
		return _S_Party;
	}

}
