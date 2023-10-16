package l1j.server.server.command.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;
//import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.L1DatabaseFactory;
//import l1j.server.server.datatables.ItemTable;
//import l1j.server.server.model.L1Inventory;
//import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
//import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
//import l1j.server.server.templates.L1Item;

public class L1Search implements L1CommandExecutor {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1Search.class.getName());

	private L1Search() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Search();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
			try {
				StringTokenizer st = new StringTokenizer(arg);
				String type = "";
				String name = "";
				String add = "";
				boolean simpleS = true;
				int itCount = 0;
				while(st.hasMoreTokens()) {
					if (itCount == 1) {
						add = "%";
					}
					String tempVar = st.nextToken();
					if (itCount == 0 && (tempVar.equals("갑옷")|| tempVar.equals("무기")||
							tempVar.equals("etc")||  tempVar.equals("변신")|| tempVar.equals("NPC"))) {
						simpleS = false;
						type = tempVar;
					} else {
						name = name + add + tempVar;
					}
					itCount++;
				}
				if (simpleS == false) {
					find_object(pc, type, name);
				} else {
					find_object(pc, name);
				}
			} catch(Exception e) {
				pc.sendPackets(new S_SystemMessage(".find [갑옷,무기,etc,변신,NPC] 를 입력 해주세요"));
			}
		}

	private void find_object(L1PcInstance pc, String type, String name) {
		try {
			String str1 = null;
			String str2 = null;
			int bless = 0;
			int count = 0;
			Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			boolean error = false;

			pc.sendPackets(new S_SystemMessage(" "));

			if (type.equals("갑옷")) {
				statement = con.prepareStatement("SELECT item_id,name,bless FROM armor WHERE name Like '%" + name + "%'");
			} else if (type.equals("무기")) {
				statement = con.prepareStatement("SELECT item_id,name,bless FROM weapon WHERE name Like '%" + name + "%'");
			} else if (type.equals("etc")) {
				statement = con.prepareStatement("SELECT item_id,name,bless FROM etcitem WHERE name Like '%" + name + "%'");
			} else if (type.equals("변신")) {
				statement = con.prepareStatement("SELECT polyid,name FROM polymorphs WHERE name Like '%" + name + "%'");
			} else if (type.equals("NPC")) {
				statement = con.prepareStatement("SELECT npcid,name FROM npc WHERE name Like '%" + name + "%'");
			} else {
				error = true;
				pc.sendPackets(new S_SystemMessage(".find [갑옷,무기,etc,변신,NPC] 를 입력 해주세요."));
			}
			String blessed = null;
			if (error == false) {
				ResultSet rs = statement.executeQuery();
				pc.sendPackets(new S_SystemMessage("라인에 검색 하는 '" + name.replace("%"," ") + " ' 는" + type + "이름..."));
				while(rs.next()) {
					str1 = rs.getString(1);
					str2 = rs.getString(2);
					if (type.equals("갑옷") || type.equals("무기") || type.equals("etc")) {
						bless = rs.getInt(3);
						if (bless == 1) {
							blessed = "";
						} else if (bless == 0) {
							blessed = "\\fR";
						} else {
							blessed = "\\fY";
						}
						pc.sendPackets(new S_SystemMessage(blessed + "번호: " + str1 + ", " + str2));
					} else {
						pc.sendPackets(new S_SystemMessage("번호: " + str1 + ", " + str2));
					}
					count++;
				}
				rs.close();
				statement.close();
				con.close();
				pc.sendPackets(new S_SystemMessage("찾기 " + count + " 라인에 항목" + type + "타입。"));
			}
		} catch(Exception e) {
		}
	}

	private void find_object(L1PcInstance pc, String name) {
		try {
			String str1 = null;
			String str2 = null;
			int bless = 0;
			String blessed = null;

			Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;

			pc.sendPackets(new S_SystemMessage(" "));

			pc.sendPackets(new S_SystemMessage("라인에서 검색 한 개체 이름: '" + name.replace("%"," ") + "'"));

			statement = con.prepareStatement("SELECT item_id,name,bless FROM armor WHERE name Like '%" + name + "%'");
			int count1 = 0;
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				if (count1 == 0) {
					pc.sendPackets(new S_SystemMessage("갑옷:"));
				}
				str1 = rs.getString(1);
				str2 = rs.getString(2);
				bless = rs.getInt(3);
				if (bless == 1) {
					blessed = "";
				} else if (bless == 0) {
					blessed = "\\fR";
				} else {
					blessed = "\\fY";
				}
				pc.sendPackets(new S_SystemMessage(blessed + "번호: " + str1 + ", " + str2));
				count1++;
			}
			rs.close();
			statement.close();

			statement = con.prepareStatement("SELECT item_id,name,bless FROM weapon WHERE name Like '%" + name + "%'");
			int count2 = 0;
			rs = statement.executeQuery();
			while(rs.next()) {
				if (count2 == 0) {
					pc.sendPackets(new S_SystemMessage("무기:"));
				}
				str1 = rs.getString(1);
				str2 = rs.getString(2);
				bless = rs.getInt(3);
				if (bless == 1) {
					blessed = "";
				} else if (bless == 0) {
					blessed = "\\fR";
				} else {
					blessed = "\\fY";
				}
				pc.sendPackets(new S_SystemMessage(blessed + "번호: " + str1 + ", " + str2));
				count2++;
			}
			rs.close();
			statement.close();

			statement = con.prepareStatement("SELECT item_id,name,bless FROM etcitem WHERE name Like '%" + name + "%'");
			int count3 = 0;
			rs = statement.executeQuery();
			while(rs.next()) {
				if (count3 == 0) {
					pc.sendPackets(new S_SystemMessage("etc:"));
				}
				str1 = rs.getString(1);
				str2 = rs.getString(2);
				bless = rs.getInt(3);
				if (bless == 1) {
					blessed = "";
				} else if (bless == 0) {
					blessed = "\\fR";
				} else {
					blessed = "\\fY";
				}
				pc.sendPackets(new S_SystemMessage(blessed + "번호: " + str1 + ", " + str2));
				count3++;
			}
			rs.close();
			statement.close();

			statement = con.prepareStatement("SELECT polyid,name FROM polymorphs WHERE name Like '%" + name + "%'");
			int count4 = 0;
			rs = statement.executeQuery();
			while(rs.next()) {
				if (count4 == 0) {
					pc.sendPackets(new S_SystemMessage("변신:"));
				}
				str1 = rs.getString(1);
				str2 = rs.getString(2);
				pc.sendPackets(new S_SystemMessage("번호: " + str1 + ", " + str2));
				count4++;
			}
			rs.close();
			statement.close();

			statement = con.prepareStatement("SELECT npcid,name FROM npc WHERE name Like '%" + name + "%'");
			int count5 = 0;
			rs = statement.executeQuery();
			while(rs.next()) {
				if (count5 == 0) {
					pc.sendPackets(new S_SystemMessage("NPC:"));
				}
				str1 = rs.getString(1);
				str2 = rs.getString(2);
				pc.sendPackets(new S_SystemMessage("번호: " + str1 + ", " + str2));
				count5++;
			}
			rs.close();
			statement.close();
			con.close();

			pc.sendPackets(new S_SystemMessage("검색 결과:"));
			String found = "";
			if (count1 > 0) {
				found += "갑옷: " + count1 + "、";
			}
			if (count2 > 0) {
				found += "무기: " + count2 + "、";
			}
			if (count3 > 0) {
				found += "etc: " + count3 + "、";
			}
			if (count4 > 0) {
				found += "변신: " + count4 + "、";
			}
			if (count5 > 0) {
				found += "NPC: " + count5 + "。";
			}
			if (found.length() > 0) {
				found = found.substring(0, found.length() - 1) + "。";
			} else {
				found = " 0 개의 항목을 발견";
			}
			pc.sendPackets(new S_SystemMessage(found));
		} catch(Exception e) {
		}
	}
}
