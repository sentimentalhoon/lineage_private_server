/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1BoardInstance;
import l1j.server.server.utils.SQLUtil;

public class S_Lotto extends ServerBasePacket {

	private byte[] _byte = null;

	public S_Lotto(L1BoardInstance board){
		writeC(Opcodes.S_OPCODE_BOARD);
		writeD(board.getId());
		writeC(0xFF); // ?
		writeC(0xFF); // ?
		writeC(0xFF); // ?
		writeC(0x7F); // ?
		Connection c = null;
		PreparedStatement p = null;
		PreparedStatement pp = null;
		ResultSet r = null;
		ResultSet rr = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			pp = c.prepareStatement("select count(*) as cnt from lotto where 발표여부=1");
			rr = pp.executeQuery();
			int count = 0;
			if(rr.next()) count = rr.getInt("cnt");
			p = c.prepareStatement("select * from lotto where 발표여부=1 order by 회차 desc");
			r = p.executeQuery();
			writeH(count);
			writeH(300);
			while(r.next()){
				writeD(r.getInt(1));							// 아이디
				writeS("운영자");								// 글쓴이
				String sTemp = r.getString(24);
				String date = sTemp.substring(2, 4) + "/" + sTemp.substring(5, 7) + "/" + sTemp.substring(8, 10);
				writeS(date);									// 날짜
				writeS(r.getInt(1) + "회차 추첨결과");			// 제목
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(rr);
			SQLUtil.close(pp);
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	public S_Lotto(L1BoardInstance board, int number){
		writeC(Opcodes.S_OPCODE_BOARDREAD);
		writeD(number);
		Connection c = null;
		PreparedStatement p = null;
		ResultSet r = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select * from lotto where 발표여부=1 order by 회차 desc");
			r = p.executeQuery();
			if(r.next()){
				writeS(r.getString(2));								// 글쓴이
				writeS("제 "+r.getInt(1)+"회차 로또 추첨발표");		// 제목
				String sTemp = r.getString(24);
				String date = sTemp.substring(2, 4) + "/" + sTemp.substring(5, 7) + "/" + sTemp.substring(8, 10);
				writeS(date);										// 날짜
				StringBuffer sb = new StringBuffer();
				// 제 ?회차(2008-11-08 11:30)
				// 당첨번호 1 2 3 4 5 6 보너스 7
				// 등수    당첨금액    당첨수    
				sb.append("제 ").append(r.getInt(1)).append("회차(").append(r.getString(24)).append(")").append("\r\n\r\n");
				sb.append("당첨번호 ").append(r.getInt(2)).append(" ").append(r.getInt(3)).append(" ").append(r.getInt(4)).append(" ");
				sb.append(r.getInt(5)).append(" ").append(r.getInt(6)).append(" ").append(r.getInt(7)).append("\r\n");
				sb.append("보너스 ").append(r.getInt(8)+"\r\n\r\n");
				sb.append("1등\t\t").append(r.getInt(10)+"원\t\t").append(r.getInt(9)).append("명\r\n");
				sb.append("2등\t\t").append(r.getInt(12)+"원\t\t").append(r.getInt(11)).append("명\r\n");
				sb.append("3등\t\t").append(r.getInt(14)+"원\t\t").append(r.getInt(13)).append("명\r\n");
				sb.append("4등\t\t").append(r.getInt(16)+"원\t\t").append(r.getInt(15)).append("명\r\n");
				sb.append("5등\t\t").append(r.getInt(18)+"원\t\t").append(r.getInt(17)).append("명\r\n");
				sb.append("총판매액: ").append(r.getInt(19)).append("원\r\n");
				sb.append("기부금액: ").append(r.getInt(22)).append("원\r\n");
				sb.append("이월금액: ").append(r.getInt(23)).append("원\r\n");
				sb.append("자동: ").append(r.getInt(20)).append("명\r\n");
				sb.append("수동: ").append(r.getInt(21)).append("명\r\n\r\n");
				sb.append("여러분의 기부금은\r\n");
				sb.append("     불우이웃 돕기에 쓰입니다.   by 강남서버");
				writeS(sb.toString());
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	@Override
	public byte[] getContent(){
		if(_byte == null){
			_byte = getBytes();
		}
		return _byte;
	}
}