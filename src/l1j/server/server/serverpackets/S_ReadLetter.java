package l1j.server.server.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.utils.SQLUtil;

public class S_ReadLetter extends ServerBasePacket{
	private static Logger _log = Logger.getLogger(S_Letter.class.getName());
	private static final String S_READLETTER = "[S] S_ReadLetter";
	private byte[] _byte = null;
	
	public S_ReadLetter(L1PcInstance pc, int type, int letterType, int id) {
		
		buildPacket(pc,type,letterType,id);
	}
	private void buildPacket(L1PcInstance pc, int type, int letterType, int id) {
		Connection con = null;
		PreparedStatement pstm = null;
		
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM letter WHERE item_object_id = ? ");
			
			pstm.setInt(1, id);
			
			rs = pstm.executeQuery();
			
			
			writeC(Opcodes.S_OPCODE_LETTER);
		     writeC(type); // 16:������ 17:���͸���

		     if (rs.next())
		     {
		    	 writeD(rs.getInt(1));   // �Խñ� �ѹ�
		    	 writeSS(rs.getString(8)); // ����
		    	 writeC(id); // ��
		    	 writeS(rs.getString(3)); // �������
		    	 writeSS(rs.getString(7)); // ����
		     } 
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
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
		return S_READLETTER;
	}
}
