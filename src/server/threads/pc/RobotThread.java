package server.threads.pc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1RobotInstance;
import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.SQLUtil;
import server.threads.pc.bean.RobotLocation;
import server.threads.pc.bean.RobotMent;
import server.threads.pc.bean.RobotName;

public class RobotThread implements Runnable {
	
	// 인공지능 쓰레드를 처리해도되는지 확인용.
	static private boolean running;
	// 쓰레드가 동작중 잠시 휴식할 시간값.
	static private long sleep;
	// 관리될 로봇들객체
	static private ConcurrentHashMap<String, L1RobotInstance> list;
	// 텔레포트할 좌표목록
	static private List<RobotLocation> list_location;
	// 텔레포트할 좌표목록
	static private List<RobotMent> list_ment;
	// 생성될 케리명 목록
	static private List<RobotName> list_name;
	static public int list_name_idx;
	
	/**
	 * 초기화 처리 함수.
	 */
	static public void init(){
		// 변수 초기화.
		sleep = 20;
		running = true;
		list_name_idx = 0;
		list = new ConcurrentHashMap<String, L1RobotInstance>();
		list_location = new ArrayList<RobotLocation>();
		list_ment = new ArrayList<RobotMent>();
		list_name = new ArrayList<RobotName>();
		// 로봇 인공지능용 쓰레드 활성화.
		new Thread(new RobotThread()).start();
		// 디비로부터 정보 추출.
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();

			pstm = con.prepareStatement("SELECT * FROM robot_location where count = '1'");
			rs = pstm.executeQuery();
			while (rs.next()) {
				RobotLocation rl = new RobotLocation();
				rl.uid = rs.getInt("uid");
				rl.x = rs.getInt("x");
				rl.y = rs.getInt("y");
				rl.map = rs.getInt("map");
				rl.etc = rs.getString("etc");
				
				list_location.add(rl);
			}
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			
			pstm = con.prepareStatement("SELECT * FROM robot_message");
			rs = pstm.executeQuery();
			while (rs.next()) {
				RobotMent rm = new RobotMent();
				rm.uid = rs.getInt("uid");
				rm.type = rs.getString("type");
				rm.ment = rs.getString("ment");
				
				list_ment.add(rm);
			}
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			
			pstm = con.prepareStatement("SELECT * FROM robot_name");
			rs = pstm.executeQuery();
			while (rs.next()) {
				RobotName rn = new RobotName();
				rn.uid = rs.getInt("uid");
				rn.name = rs.getString("name");
				list_name.add(rn);
			}
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
		} catch (SQLException e) {
			
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	
	public static boolean doesCharNameExist(String name) {
		boolean result = true;
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT name FROM robot_name WHERE name=?");
			pstm.setString(1, name);
			rs = pstm.executeQuery();
			result = rs.next();
		} catch (SQLException e) {
			
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return result;
	}
	
	/**
	 * 종료 처리 함수.
	 */
	static public void close(){
		running = false;
	}
	
	/**
	 * 관리 목록에 추가요청 처리 함수.
	 * @param robot
	 */
	static public void append(L1RobotInstance robot){
		synchronized (list) {
			list.put(robot.getName(), robot);
		}
	}
	
	/**
	 * 관리목록에서 제거요청 처리 함수.
	 * @param robot
	 */
	static public void remove(L1RobotInstance robot){
		list.remove(robot.getName());
	}
	
	/**
	 * 소속 혈맹과 일치하는 로봇 찾아서 리턴함.
	 * @param clan_name
	 * @param r_list
	 */
	static public void find(String clan_name, ConcurrentHashMap<String, L1RobotInstance> r_list){		
		for(L1RobotInstance robot : list.values()){
			if(robot.getClanname().equalsIgnoreCase(clan_name))
				r_list.put(robot.getName(), robot);
		}
	}
	
	@Override
	public void run(){
		try {
			long time = System.currentTimeMillis();
			for( ; running ; ){
				// 휴식
				Thread.sleep(sleep);
				
				time = System.currentTimeMillis();
				// 로봇들 인공지능 활성화.				
				for(L1RobotInstance robot : list.values())
					robot.toAi(time);
			}
		} catch (Exception e) {
			try {
			long time = System.currentTimeMillis();
			for( ; running ; ){		
				Thread.sleep(sleep);
				time = System.currentTimeMillis();
				// 로봇들 인공지능 활성화.
				for(L1RobotInstance robot : list.values())
					robot.toAi(time);
			}
			}catch (Exception f) {
				System.out.println("로봇 쓰레드가 비정상적으로 종료 되어 AI 재시작중 중복오류 발생!");
				System.out.println(f);
			}
			System.out.println("로봇 쓰레드가 비정상적으로 종료 되어 AI재시작!");
			System.out.println(e);
		}
	}
	
	/**
	 * 사냥터 텔레포트 좌표리턴.
	 * @param type
	 * @return
	 */
	static public RobotLocation getLocation(){
		if(list_location.size() == 0)
			return null;
		
		return list_location.get( CommonUtil.random(0, list_location.size()-1) );
	}
	
	static public List<RobotMent> getRobotMent(){
		return list_ment;
	}
	
	static public List<RobotName> getRobotName(){
		return list_name;
	}
	
	static public String getName(){
		try {
			// 이름목록 순회.
			for( ; list_name_idx < list_name.size() ; ){
				String name = list_name.get(list_name_idx++).name;
				Connection con = null;
				PreparedStatement pstm = null;
				ResultSet rs = null;
				try {
					con = L1DatabaseFactory.getInstance().getConnection();
					pstm = con.prepareStatement("SELECT * FROM characters WHERE char_name=?");
					pstm.setString(1, name);
					rs = pstm.executeQuery();
					if(!rs.next())
						return name;
				} catch (SQLException e) {
				} finally {
					SQLUtil.close(rs);
					SQLUtil.close(pstm);
					SQLUtil.close(con);
				}
			}
		} catch (Exception e) { }
		// 디비에 동이한 이름 존재 확인.
		// 마지막이 이덱스까지 도달할경우 무시.
		return null;
	}
	
	static public void reload() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		list_location.clear();
		list_ment.clear();
		list_name.clear();
		
		try {
			con = L1DatabaseFactory.getInstance().getConnection();

			pstm = con.prepareStatement("SELECT * FROM robot_location where count = '1'");
			rs = pstm.executeQuery();
			while (rs.next()) {
				RobotLocation rl = new RobotLocation();
				rl.uid = rs.getInt("uid");
				rl.x = rs.getInt("x");
				rl.y = rs.getInt("y");
				rl.map = rs.getInt("map");
				rl.etc = rs.getString("etc");
				
				list_location.add(rl);
			}
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			
			pstm = con.prepareStatement("SELECT * FROM robot_message");
			rs = pstm.executeQuery();
			while (rs.next()) {
				RobotMent rm = new RobotMent();
				rm.uid = rs.getInt("uid");
				rm.type = rs.getString("type");
				rm.ment = rs.getString("ment");
				
				list_ment.add(rm);
			}
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			
			pstm = con.prepareStatement("SELECT * FROM robot_name");
			rs = pstm.executeQuery();
			while (rs.next()) {
				RobotName rn = new RobotName();
				rn.uid = rs.getInt("uid");
				rn.name = rs.getString("name");
				list_name.add(rn);
			}
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
		} catch (SQLException e) {
			
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
