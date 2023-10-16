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
			pp = c.prepareStatement("select count(*) as cnt from lotto where ��ǥ����=1");
			rr = pp.executeQuery();
			int count = 0;
			if(rr.next()) count = rr.getInt("cnt");
			p = c.prepareStatement("select * from lotto where ��ǥ����=1 order by ȸ�� desc");
			r = p.executeQuery();
			writeH(count);
			writeH(300);
			while(r.next()){
				writeD(r.getInt(1));							// ���̵�
				writeS("���");								// �۾���
				String sTemp = r.getString(24);
				String date = sTemp.substring(2, 4) + "/" + sTemp.substring(5, 7) + "/" + sTemp.substring(8, 10);
				writeS(date);									// ��¥
				writeS(r.getInt(1) + "ȸ�� ��÷���");			// ����
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
			p = c.prepareStatement("select * from lotto where ��ǥ����=1 order by ȸ�� desc");
			r = p.executeQuery();
			if(r.next()){
				writeS(r.getString(2));								// �۾���
				writeS("�� "+r.getInt(1)+"ȸ�� �ζ� ��÷��ǥ");		// ����
				String sTemp = r.getString(24);
				String date = sTemp.substring(2, 4) + "/" + sTemp.substring(5, 7) + "/" + sTemp.substring(8, 10);
				writeS(date);										// ��¥
				StringBuffer sb = new StringBuffer();
				// �� ?ȸ��(2008-11-08 11:30)
				// ��÷��ȣ 1 2 3 4 5 6 ���ʽ� 7
				// ���    ��÷�ݾ�    ��÷��    
				sb.append("�� ").append(r.getInt(1)).append("ȸ��(").append(r.getString(24)).append(")").append("\r\n\r\n");
				sb.append("��÷��ȣ ").append(r.getInt(2)).append(" ").append(r.getInt(3)).append(" ").append(r.getInt(4)).append(" ");
				sb.append(r.getInt(5)).append(" ").append(r.getInt(6)).append(" ").append(r.getInt(7)).append("\r\n");
				sb.append("���ʽ� ").append(r.getInt(8)+"\r\n\r\n");
				sb.append("1��\t\t").append(r.getInt(10)+"��\t\t").append(r.getInt(9)).append("��\r\n");
				sb.append("2��\t\t").append(r.getInt(12)+"��\t\t").append(r.getInt(11)).append("��\r\n");
				sb.append("3��\t\t").append(r.getInt(14)+"��\t\t").append(r.getInt(13)).append("��\r\n");
				sb.append("4��\t\t").append(r.getInt(16)+"��\t\t").append(r.getInt(15)).append("��\r\n");
				sb.append("5��\t\t").append(r.getInt(18)+"��\t\t").append(r.getInt(17)).append("��\r\n");
				sb.append("���Ǹž�: ").append(r.getInt(19)).append("��\r\n");
				sb.append("��αݾ�: ").append(r.getInt(22)).append("��\r\n");
				sb.append("�̿��ݾ�: ").append(r.getInt(23)).append("��\r\n");
				sb.append("�ڵ�: ").append(r.getInt(20)).append("��\r\n");
				sb.append("����: ").append(r.getInt(21)).append("��\r\n\r\n");
				sb.append("�������� ��α���\r\n");
				sb.append("     �ҿ��̿� ���⿡ ���Դϴ�.   by ��������");
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