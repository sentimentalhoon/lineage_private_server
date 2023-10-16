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

public class S_Serchdrop2 extends ServerBasePacket {

	private static final String S_Serchdrop2 = "[C] S_Serchdrop2";

	private static Logger _log = Logger.getLogger(S_Serchdrop2.class.getName());

	private byte[] _byte = null;

	int itemid[] = new int [30];
	String itemname[] = new String [30];

	public S_Serchdrop2(int itemid) {
		buildPacket(itemid);
	}

	private void buildPacket(int npcid) {
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
			pstm = con.prepareStatement("SELECT `itemId` FROM `droplist` WHERE mobId=? ORDER BY `itemId` DESC LIMIT 30");
			pstm.setInt(1, npcid);
			rs = pstm.executeQuery();

			while (rs.next()) 
			{
				itemid[i] = rs.getInt(1);
				itemname[i] = ItemTable.getInstance().getTemplate(itemid[i]).getName();
				i++;				
			}		

			writeC(Opcodes.S_OPCODE_BOARDREAD);
			writeD(0);//�ѹ�
			writeS("��� #5");//�۾���?
			writeS("Tomato �������Ʈ");
			writeS("");
			writeS("\r�˻��� �� : " + NpcTable.getInstance().getTemplate(npcid).get_name() + 
				   "\n\n\r******  ����ϴ� ������  ******" +
				   "\n\n\r" + itemname[0] + " | " + itemname[1] + " | " + itemname[2] + " | " + itemname[3] + " | " + itemname[4] + " | " + itemname[5] + 
				   " | " + itemname[6] + " | " + itemname[7] + " | " + itemname[8] + " | " + itemname[9] + " | " + itemname[10] + " | " + itemname[11] + 
				   " | " + itemname[12] + " | " + itemname[13] + " | " + itemname[14] + " | " + itemname[15] + " | " + itemname[16] + " | " + itemname[17] +
				   " | " + itemname[18] + " | " + itemname[19] + " | " + itemname[20] + " | " + itemname[21] + " | " + itemname[22] + " | " + itemname[23] +
				   " | " + itemname[24] + " | " + itemname[25] + " | " + itemname[26] + " | " + itemname[27] + " | " + itemname[28] + " | " + itemname[29]
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
		return S_Serchdrop2;
	}
}
