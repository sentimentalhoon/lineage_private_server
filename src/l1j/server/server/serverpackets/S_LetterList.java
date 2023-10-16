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

public class S_LetterList extends ServerBasePacket{
	private static Logger _log = Logger.getLogger(S_Letter.class.getName());
	private static final String S_LETTERLIST = "[S] S_LetterList";
	private byte[] _byte = null;

	public S_LetterList(L1PcInstance pc, int type, int count) {
		buildPacket(pc,type,count);
	}
	private void buildPacket(L1PcInstance pc, int type, int count) {
		Connection con = null;
		PreparedStatement pstm = null;
		PreparedStatement pstm1 = null;
		
		ResultSet rs = null;
		ResultSet rs1 = null;
		int cnt = 0;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM letter WHERE receiver=? AND template_id = ? order by date limit ?  ");
			
			pstm.setString(1, pc.getName());
			pstm.setInt(2, type);
			pstm.setInt(3, count);
			rs = pstm.executeQuery();
			
			pstm1 = con.prepareStatement(" SELECT count(*) as cnt FROM letter WHERE receiver=? AND template_id = ? order by date limit ?  ");
			pstm1.setString(1, pc.getName());
			pstm1.setInt(2, type);
			pstm1.setInt(3, count);
			rs1 = pstm1.executeQuery();
			if (rs1.next()){
				cnt = rs1.getInt(1);
			}
			writeC(Opcodes.S_OPCODE_LETTER);
		    writeC(type); // 0:메일함 1:혈맹메일함 2:보관함
		    
		    writeH(cnt);
		    //writeH(count); //표현할 글 게수 (보관함 10 일반 편지 20 혈편지 50개)
		    
		    String[] str;
			while (rs.next()) {
				   writeD(rs.getInt(1));
			       writeC(rs.getInt(9)); // 확인여부 0:미확인 1:확인
			       str = null;
			       str = rs.getString(5).split("/");
			       writeC(Integer.parseInt(str[0]));
			       writeC(Integer.parseInt(str[1]));
			       writeC(Integer.parseInt(str[2]));
			       writeS(rs.getString(3));
			       writeSS(rs.getString(7));
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(rs1);
			SQLUtil.close(pstm);
			SQLUtil.close(pstm1);
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
		return S_LETTERLIST;
	}
}
