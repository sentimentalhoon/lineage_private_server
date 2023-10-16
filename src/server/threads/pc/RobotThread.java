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
	
	// �ΰ����� �����带 ó���ص��Ǵ��� Ȯ�ο�.
	static private boolean running;
	// �����尡 ������ ��� �޽��� �ð���.
	static private long sleep;
	// ������ �κ��鰴ü
	static private ConcurrentHashMap<String, L1RobotInstance> list;
	// �ڷ���Ʈ�� ��ǥ���
	static private List<RobotLocation> list_location;
	// �ڷ���Ʈ�� ��ǥ���
	static private List<RobotMent> list_ment;
	// ������ �ɸ��� ���
	static private List<RobotName> list_name;
	static public int list_name_idx;
	
	/**
	 * �ʱ�ȭ ó�� �Լ�.
	 */
	static public void init(){
		// ���� �ʱ�ȭ.
		sleep = 20;
		running = true;
		list_name_idx = 0;
		list = new ConcurrentHashMap<String, L1RobotInstance>();
		list_location = new ArrayList<RobotLocation>();
		list_ment = new ArrayList<RobotMent>();
		list_name = new ArrayList<RobotName>();
		// �κ� �ΰ����ɿ� ������ Ȱ��ȭ.
		new Thread(new RobotThread()).start();
		// ���κ��� ���� ����.
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
	 * ���� ó�� �Լ�.
	 */
	static public void close(){
		running = false;
	}
	
	/**
	 * ���� ��Ͽ� �߰���û ó�� �Լ�.
	 * @param robot
	 */
	static public void append(L1RobotInstance robot){
		synchronized (list) {
			list.put(robot.getName(), robot);
		}
	}
	
	/**
	 * ������Ͽ��� ���ſ�û ó�� �Լ�.
	 * @param robot
	 */
	static public void remove(L1RobotInstance robot){
		list.remove(robot.getName());
	}
	
	/**
	 * �Ҽ� ���Ͱ� ��ġ�ϴ� �κ� ã�Ƽ� ������.
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
				// �޽�
				Thread.sleep(sleep);
				
				time = System.currentTimeMillis();
				// �κ��� �ΰ����� Ȱ��ȭ.				
				for(L1RobotInstance robot : list.values())
					robot.toAi(time);
			}
		} catch (Exception e) {
			try {
			long time = System.currentTimeMillis();
			for( ; running ; ){		
				Thread.sleep(sleep);
				time = System.currentTimeMillis();
				// �κ��� �ΰ����� Ȱ��ȭ.
				for(L1RobotInstance robot : list.values())
					robot.toAi(time);
			}
			}catch (Exception f) {
				System.out.println("�κ� �����尡 ������������ ���� �Ǿ� AI ������� �ߺ����� �߻�!");
				System.out.println(f);
			}
			System.out.println("�κ� �����尡 ������������ ���� �Ǿ� AI�����!");
			System.out.println(e);
		}
	}
	
	/**
	 * ����� �ڷ���Ʈ ��ǥ����.
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
			// �̸���� ��ȸ.
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
		// ��� ������ �̸� ���� Ȯ��.
		// �������� �̵������� �����Ұ�� ����.
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
