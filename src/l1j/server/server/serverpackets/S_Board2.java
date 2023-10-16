
package l1j.server.server.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.templates.L1Item;
import l1j.server.server.utils.SQLUtil;

public class S_Board2 extends ServerBasePacket {
	private static final String S_Board2 = "[S] S_Board2";
	private static Logger _log = Logger.getLogger(S_Board2.class.getName());
	private byte[] _byte = null;
	public S_Board2(L1NpcInstance board) {
		buildPacket(board, 0);
	}
	public S_Board2(L1NpcInstance board, int number) {
		buildPacket(board, number);
	}

	public S_Board2(int number) {
		buildPacket(number);
	}

	private void buildPacket(L1NpcInstance board, int number) {
		int count = 0;
		String[][] db = null;
		int[] id = null;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			db = new String[8][4];
			id = new int[8];
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM AuctionB order by id desc");
			rs = pstm.executeQuery();
			while (rs.next() && count < 8) {
				if (rs.getInt(1) <= number || number == 0) {
					id[count] = rs.getInt(1);
					db[count][0] = rs.getString(2);
					db[count][1] = rs.getString(8);
					db[count][2] = rs.getString(5);
					db[count][3] = rs.getString(4);
					count++;
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		writeC(Opcodes.S_OPCODE_BOARD);
		writeC(0);// type
		writeD(board.getId());
		writeC(0xFF); // ?
		writeC(0xFF); // ?
		writeC(0xFF); // ?
		writeC(0x7F); // ?
		writeH(count);
		writeH(300);
		for (int i = 0; i < count; ++i) {
			writeD(id[i]);
			writeS(db[i][0]);
			writeS(db[i][1]);
			writeS("+ "+db[i][2]+" "+db[i][3]);
		}
	}

	public void buildPacket(int number) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM AuctionB WHERE id=?");
			pstm.setInt(1, number);
			rs = pstm.executeQuery();
			while (rs.next()) {
				writeC(Opcodes.S_OPCODE_BOARDREAD);
				writeD(number);
				writeS(rs.getString(2));
				writeS("+ "+rs.getString(5) +" "+rs.getString(4));
				writeS(rs.getString(8));
				writeS("판매 금액 : "+rs.getString(6)+" 아데나"+"\n"+"속성 인첸트 : "+ UpClick(rs.getString(9)) +"\n"+acacac(rs.getString(3)));
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	private String UpClick(String z){
		int aaaaa = Integer.parseInt(z);
		String ggg = "";

		switch(aaaaa){
		case 1:
			ggg= "불 1단계";
			break; 
		case 2:
			ggg= "불 2단계";
			break; 
		case 3:
			ggg= "불 3단계";
			break; 
		case 4:
			ggg= "물 1단계";
			break; 
		case 5:
			ggg= "물 2단계";
			break; 
		case 6:
			ggg= "물 3단계";
			break; 
		case 7:
			ggg= "바람 1단계";
			break; 
		case 8:
			ggg= "바람 2단계";
			break; 
		case 9:
			ggg= "바람 3단계";
			break; 
		case 10:
			ggg= "땅 1단계";
			break; 
		case 11:
			ggg= "땅 2단계";
			break; 
		case 12:
			ggg= "땅 3단계";
			break;
		default: 
			ggg= "없음";
			break;
		}
		return ggg;
	}

	private String Bless(String g) {
		String ggg = "";
		int aaaaa = Integer.parseInt(g);
		L1Item temp = ItemTable.getInstance().getTemplate(aaaaa);
		if(temp.getBless()==1){
			ggg = "일반";
		} else if(temp.getBless()== 2){
			ggg = "저주";
		} else if(temp.getBless()== 0){
			ggg = "축복";
		}
		return ggg;
	}

	private String acacac(String g){
		int aaaaa = Integer.parseInt(g);
		L1Item temp = ItemTable.getInstance().getTemplate(aaaaa);
		int MinLevel = temp.getMinLevel();
		int MaxLevel = temp.getMaxLevel();
		String Min = "";
		String Max = "";
		String ggg = "";
		if(MinLevel <= 0) Min = "없음";
		else Min = ""+MinLevel+"";
		if(MaxLevel <= 0) Max = "없음";
		else Max = ""+MaxLevel+"";
		int safeenchant = temp.get_safeenchant();
		if (temp.getType2() == 2) {
			ggg = ("\n축복여부 : "+Bless(g)+
					"\n착용가능 최소 레벨 : "+Min+
					"\n착용가능 최대 레벨 : "+Max+
					"\n기본 AC : "+temp.get_ac()+
					"\n안전 인첸트 : "+safeenchant+
					"\n"+
					"\n"+adadad(g)+
					"\n"+
					"\n"+sdsdsd(g));
		} else if (temp.getType2() == 1) {
			ggg = ("\n축복여부 : "+Bless(g)+
					"\n착용가능 최소 레벨 : "+Min+
					"\n착용가능 최대 레벨 : "+Max+
					"\n작은몹 데미지 : "+temp.getDmgSmall()+
					"\n큰몹 데미지 : "+temp.getDmgLarge()+
					"\n안전 인첸트 : "+safeenchant+
					"\n"+
					"\n"+adadad(g)+
					"\n"+
					"\n"+sdsdsd(g));
		}
		return ggg;
	}

	private String sdsdsd(String g){
		int aaaaa = Integer.parseInt(g);
		L1Item temp = ItemTable.getInstance().getTemplate(aaaaa);
		String ggg = "";
		String Class= "";
		if(temp.get_addstr()!= 0){
			Class  += " STR:"+temp.get_addstr(); 
		}
		if(temp.get_adddex()!= 0){
			Class  += " DEX:"+temp.get_adddex(); 
		}
		if(temp.get_addcon()!= 0){
			Class  +=" CON:"+temp.get_addcon();
		} 
		if(temp.get_addint()!= 0){
			Class  +=" INT:"+temp.get_addint();
		} 
		if(temp.get_addwis()!= 0){
			Class  +=" WIZ:"+temp.get_addwis();
		}
		if(temp.get_addcha()!= 0){
			Class  +=" CHA:"+temp.get_addcha();
		} 
		if(temp.get_addhp()!= 0){
			Class  +=" HP:"+temp.get_addhp();
		}
		if(temp.get_addmp()!= 0){
			Class  +=" MP:"+temp.get_addmp();
		}
		if(temp.get_addhpr()!= 0){
			Class  +=" hpr:"+temp.get_addhpr();
		}
		if(temp.get_addmpr()!= 0){
			Class  +=" mpr:"+temp.get_addmpr();
		} 
		if(temp.get_addsp()!= 0){
			Class  +=" SP:"+temp.get_addsp();
		} 
		if(temp.get_mdef()!= 0){
			Class  +=" MR:"+temp.get_mdef();
		} 
		if(temp.getDmgModifier()!= 0){
			Class  +=" 추타:"+temp.getDmgModifier();
		}  
		if(temp.getHitModifier()!= 0){
			Class  +=" 명중:"+temp.getHitModifier();
		}
		if(temp.getDamageReduction()!= 0){
			Class  +=" 데미지 감소:"+temp.getDamageReduction();
		}  
		if(temp.getWeightReduction()!= 0){
			Class  +=" 무게 감소:"+temp.getWeightReduction()+"%";
		}  
		if(temp.getBowDmgup()!= 0){
			Class  +=" 원거리 추타:"+temp.getBowDmgup();
		}  
		if(temp.getBowHitup()!= 0){
			Class  +=" 원거리 명중:"+temp.getBowHitup();
		} 
		if(temp.get_defense_water()!= 0){
			Class  +=" 속성방어(물):"+temp.get_defense_water();
		}  
		if(temp.get_defense_fire()!= 0){
			Class  +=" 속성방어(불):"+temp.get_defense_fire();
		} 
		if(temp.get_defense_earth()!= 0){
			Class  +=" 속성방어(땅):"+temp.get_defense_earth();
		}  
		if(temp.get_defense_wind()!= 0){
			Class  +=" 속성방어(바람):"+temp.get_defense_wind();
		}
		if(temp.get_regist_stun()!= 0){
			Class  +=" 내성(스턴):"+temp.get_regist_stun();
		}  
		if(temp.get_regist_stone()!= 0){
			Class  +=" 내성(석화):"+temp.get_regist_stone();
		} 
		if(temp.get_regist_sleep()!= 0){
			Class  +=" 내성(수면):"+temp.get_regist_sleep();
		}  
		if(temp.get_regist_freeze()!= 0){
			Class  +=" 내성(동빙):"+temp.get_regist_freeze();
		}
		if(temp.get_regist_sustain()!= 0){
			Class  +=" 내성(암흑):"+temp.get_regist_sustain();
		}  
		if(temp.get_regist_blind()!= 0){
			Class  +=" 내성(홀드):"+temp.get_regist_blind();
		}
		ggg = "옵션:"+Class;
		return ggg;
	}

	private String adadad(String g){
		int aaaaa = Integer.parseInt(g);
		L1Item temp = ItemTable.getInstance().getTemplate(aaaaa);
		String ggg = "";
		String Class= "";
		if(temp.isUseRoyal()== true){
			Class  += "군주 "; 
		}
		if(temp.isUseKnight()== true){
			Class  += "기사 "; 
		}
		if(temp.isUseElf()== true){
			Class  +="요정 ";
		} 
		if(temp.isUseMage()== true){
			Class  +="마법사 "; 
		} 
		if(temp.isUseDarkelf()== true){
			Class  += "다크엘프 ";
		}
		if(temp.isUseDragonKnight()== true){
			Class  += "용기사 "; 
		} 
		if(temp.isUseBlackwizard()== true){
			Class  +="환술사";
		}
		ggg = "착용 클래스 : "+Class;
		return ggg;
	}
	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	@Override
	public String getType() {
		return S_Board2;
	}
}