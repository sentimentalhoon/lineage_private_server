
package l1j.server.server.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.Opcodes;
import l1j.server.server.utils.SQLUtil;

import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.ItemTable;

public class S_Serchdrop extends ServerBasePacket {

	private static final String S_Serchdrop = "[C] S_Serchdrop";

	private static Logger _log = Logger.getLogger(S_Serchdrop.class.getName());

	private byte[] _byte = null;

	int mobid[] = new int [30];
	String mobname[] = new String [30];

	public S_Serchdrop(int itemid) {
		buildPacket(itemid);
	}

	private void buildPacket(int itemid) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		Connection con1 = null;
		PreparedStatement pstm1 = null;
		ResultSet rs1 = null;

		int i = 0;
		try 
		{
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT `mobId` FROM `droplist` WHERE itemId=? ORDER BY `mobId` DESC LIMIT 30");
			pstm.setInt(1, itemid);
			rs = pstm.executeQuery();

			while (rs.next()) 
			{
				mobid[i] = rs.getInt(1);
				mobname[i] = NpcTable.getInstance().getTemplate(mobid[i]).get_name();
				i++;				
			}		

			writeC(Opcodes.S_OPCODE_BOARDREAD);
			writeD(0);//넘버
			writeS("운영자 #5");//글쓴이?
			writeS("Tomato 드랍리스트");
			writeS("");
			writeS("\r검색할 아이템 : " + ItemTable.getInstance().getTemplate(itemid).getName() + 
				   "\n\n\r******  드랍하는 몹  ******" +
				   "\n\n\r" + mobname[0] + " | " + mobname[1] + " | " + mobname[2] + " | " + mobname[3] + " | " + mobname[4] + " | " + mobname[5] + 
				   " | " + mobname[6] + " | " + mobname[7] + " | " + mobname[8] + " | " + mobname[9] + " | " + mobname[10] + " | " + mobname[11] + 
				   " | " + mobname[12] + " | " + mobname[13] + " | " + mobname[14] + " | " + mobname[15] + " | " + mobname[16] + " | " + mobname[17] +
				   " | " + mobname[18] + " | " + mobname[19] + " | " + mobname[20] + " | " + mobname[21] + " | " + mobname[22] + " | " + mobname[23] +
				   " | " + mobname[24] + " | " + mobname[25] + " | " + mobname[26] + " | " + mobname[27] + " | " + mobname[28] + " | " + mobname[29]
				  );

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
				SQLUtil.close(rs);
				SQLUtil.close(pstm);
				SQLUtil.close(con);
				SQLUtil.close(rs1);
				SQLUtil.close(pstm1);
				SQLUtil.close(con1);
		}
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	public String getType() {
		return S_Serchdrop;
	}
}

