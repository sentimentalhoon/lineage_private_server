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

import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import server.LineageClient;

import l1j.server.Config;
import l1j.server.server.datatables.BoardTable;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1BoardInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_BoardWrite extends ClientBasePacket {

	private static final String C_BOARD_WRITE = "[C] C_BoardWrite";
	private static Logger _log = Logger.getLogger(C_BoardWrite.class.getName());

	public C_BoardWrite(byte decrypt[], LineageClient client) {
		super(decrypt);
		int id = readD();
		String date = currentTime();
		String title = readS();
		String content = readS();

		L1Object tg = L1World.getInstance().findObject(id);
		if(tg instanceof L1BoardInstance){
			L1BoardInstance board = (L1BoardInstance)tg;
			if (board != null) {
				L1PcInstance pc = client.getActiveChar();
				if(board.getNpcId() == 4500309){ 
					if (pc.isGm()){ 
					} else {
						pc.sendPackets(new S_SystemMessage("해당 게시판은 운영자만 사용가능합니다."));
						return;                                        
					}
				}
				if (pc.getLevel() >= 52) {  //게시판작성 레벨제한 
					if(board.getNpcId() == 4212014){
						if(checkdragonkey(pc)){
							L1ItemInstance dragonkey = pc.getInventory().findItemId(L1ItemId.DRAGON_KEY);
							BoardTable.getInstance().writeDragonKey(pc, dragonkey, date, board.getNpcId());
							pc.sendPackets(new S_ServerMessage(1567));// 등록되었다
						}else{
							return;
						}
					}else if(board.getNpcId() == 4500302){
						     BoardTable.getInstance().writeTopic2(pc, date, title, content, board.getNpcId());
					}else{
						if(board.getNpcId() == 4200020){
							pc.getInventory().consumeItem(L1ItemId.ADENA, 300);
							BoardTable.getInstance().writeTopicUser(pc, date, title, content);
						}
						if(board.getNpcId() == 4200018){
							pc.getInventory().consumeItem(L1ItemId.ADENA, 300);
							BoardTable.getInstance().writeTopicUser2(pc, date, title, content);
						}
						pc.getInventory().consumeItem(L1ItemId.ADENA, 300);
						BoardTable.getInstance().writeTopic(pc, date, title, content, board.getNpcId());
					}

				} else {
					pc.sendPackets(new S_SystemMessage("52레벨 이하는 게시판 작성을 할수 없습니다.")); 
				}
			} else {
				_log.warning("부정한 NPCID : " + id);
			}
		}
	}

	private static String currentTime() {
		TimeZone tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(tz);
		int year = cal.get(Calendar.YEAR) - 2000;
		String year2;
		if (year < 10) {
			year2 = "0" + year;
		} else {
			year2 = Integer.toString(year);
		}
		int Month = cal.get(Calendar.MONTH) + 1;
		String Month2 = null;
		if (Month < 10) {
			Month2 = "0" + Month;
		} else {
			Month2 = Integer.toString(Month);
		}
		int date = cal.get(Calendar.DATE);
		String date2 = null;
		if (date < 10) {
			date2 = "0" + date;
		} else {
			date2 = Integer.toString(date);
		}
		return year2 + "/" + Month2 + "/" + date2;
	}

	/**
	 * 드래곤키를 등록할 조건을 판단한다.
	 * @param pc
	 * @return
	 */
	private boolean checkdragonkey(L1PcInstance pc){
		if(pc.getInventory().checkItem(L1ItemId.DRAGON_KEY)){
			if(BoardTable.getInstance().checkExistName(pc.getName(), 4212014)){
				pc.sendPackets(new S_ServerMessage(1568));// 이미 등록되어 있어
				return false;
			}else{
				return true;
			}
		}else{
			pc.sendPackets(new S_ServerMessage(1566));// 드래곤 키 있어야 해
			return false;
		}
	}

	@Override
	public String getType() {
		return C_BOARD_WRITE;
	}

}
