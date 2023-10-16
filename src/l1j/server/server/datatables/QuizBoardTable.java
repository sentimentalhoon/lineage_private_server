package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;

public class QuizBoardTable {
	public class QuizBoard {
		public int id;
		public String memo;
		public boolean answer;
	}
	
	private static Logger _log = Logger.getLogger(QuizBoardTable.class.getName());
	
	private static QuizBoardTable _instance;
	
	private static HashMap<Integer, QuizBoard> _QuizList = new HashMap<Integer, QuizBoard>();
	
	public static QuizBoardTable getInstance() {
		if (_instance == null) {
			_instance = new QuizBoardTable();
		}
		return _instance;
	}
	
	private QuizBoardTable() {
		System.out.print("[QuizBoardTable] loading QuizBoardTable...");	
		selectRobotTeleportList();
		System.out.println("OK! " + _QuizList.size() + " °Ç");	
	}
	
	public void selectRobotTeleportList() { 
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("select * from quizboard");
			rs = pstm.executeQuery();
			QuizBoard quizList = null;
			
			while (rs.next()) {
				quizList = new QuizBoard();	
				quizList.id = rs.getInt(1);
				quizList.memo = rs.getString(2);
				quizList.answer = rs.getBoolean(3);
				
				_QuizList.put(quizList.id, quizList);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	
	public static HashMap<Integer, QuizBoard> getQuizList() {
		return _QuizList;
	}
}
