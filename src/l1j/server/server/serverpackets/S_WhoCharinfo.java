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

import java.util.Random;
import java.util.logging.Logger;

import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1NpcShopInstance;
import l1j.server.server.model.Instance.L1PcInstance;

public class S_WhoCharinfo extends ServerBasePacket {
	private static final String S_WHO_CHARINFO = "[S] S_WhoCharinfo";
	private static Logger _log = Logger.getLogger(S_WhoCharinfo.class.getName());
	private byte[] _byte = null;

	public S_WhoCharinfo(L1PcInstance pc) {
		_log.fine("Who charpack for : " + pc.getName());

		String lawfulness = "";
		//String Hunter = "";
		/* Kill & Death 시스템?  -by 천국- */

		float win = 0;
		float lose = 0;

		win = pc.getKills();
		lose = pc.getDeaths();

		/* Kill & Death 시스템?  -by 천국- */
		int lawful = pc.getLawful();		
		if (lawful < 0) {
			lawfulness = "(Chaotic)";
		} else if (lawful >= 0 && lawful < 500) {
			lawfulness = "(Neutral)";
		} else if (lawful >= 500) {
			lawfulness = "(Lawful)";
		}

		writeC(Opcodes.S_OPCODE_MSG);
		writeC(0x08);

		String title ="";
		String clan = "";

		if (pc.getTitle().equalsIgnoreCase("") == false) {
			title = pc.getTitle() + "";
		}

		if (pc.getClanid() > 0) {
			clan = "[" + pc.getClanname() + "]";
		}
		writeS(title +" "+ pc.getName() + " " + lawfulness + " " + clan+" "+"\n\r\\fR"+"( 킬 : "+pc.getKills()+" ) " +
				" ( 죽음 : "+pc.getDeaths()+" ) " + " 초기화 = " + pc.get_KillDeathInitialize() + "회");		 
		writeD(0);
	}
	public S_WhoCharinfo(L1NpcShopInstance npcshop) { // 추가

		_log.fine("Who charpack for : " + npcshop.getName());

		String lawfulness = "";
		Random ran = new Random();
		//int win = 0;
		//int lose = 0;

		int win = ran.nextInt(120);
		int lose = ran.nextInt(120);

		int lawful = npcshop.getLawful();
		if (lawful < 0) {
			lawfulness = "(Chaotic)";
		} else if (lawful >= 0 && lawful < 500) {
			lawfulness = "(Neutral)";
		} else if (lawful >= 500) {
			lawfulness = "(Lawful)";
		}

		writeC(Opcodes.S_OPCODE_MSG);
		writeC(0x08);

		String title = "";

		if (npcshop.getTitle().equalsIgnoreCase("") == false) {
			title = npcshop.getTitle() + " ";
		}
		writeS(title + " " + npcshop.getName() + " " + lawfulness + " "+"\n\r\\fR"+ "( 킬 : "+ win + " ) " +
				" ( 죽음 : "+ lose +" ) " + " 초기화 = " + npcshop.get_KillDeathInitialize() + "회");	
		writeD(0);
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
		return S_WHO_CHARINFO;
	}
}

