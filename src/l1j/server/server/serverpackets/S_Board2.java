
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
				writeS("�Ǹ� �ݾ� : "+rs.getString(6)+" �Ƶ���"+"\n"+"�Ӽ� ��þƮ : "+ UpClick(rs.getString(9)) +"\n"+acacac(rs.getString(3)));
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
			ggg= "�� 1�ܰ�";
			break; 
		case 2:
			ggg= "�� 2�ܰ�";
			break; 
		case 3:
			ggg= "�� 3�ܰ�";
			break; 
		case 4:
			ggg= "�� 1�ܰ�";
			break; 
		case 5:
			ggg= "�� 2�ܰ�";
			break; 
		case 6:
			ggg= "�� 3�ܰ�";
			break; 
		case 7:
			ggg= "�ٶ� 1�ܰ�";
			break; 
		case 8:
			ggg= "�ٶ� 2�ܰ�";
			break; 
		case 9:
			ggg= "�ٶ� 3�ܰ�";
			break; 
		case 10:
			ggg= "�� 1�ܰ�";
			break; 
		case 11:
			ggg= "�� 2�ܰ�";
			break; 
		case 12:
			ggg= "�� 3�ܰ�";
			break;
		default: 
			ggg= "����";
			break;
		}
		return ggg;
	}

	private String Bless(String g) {
		String ggg = "";
		int aaaaa = Integer.parseInt(g);
		L1Item temp = ItemTable.getInstance().getTemplate(aaaaa);
		if(temp.getBless()==1){
			ggg = "�Ϲ�";
		} else if(temp.getBless()== 2){
			ggg = "����";
		} else if(temp.getBless()== 0){
			ggg = "�ູ";
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
		if(MinLevel <= 0) Min = "����";
		else Min = ""+MinLevel+"";
		if(MaxLevel <= 0) Max = "����";
		else Max = ""+MaxLevel+"";
		int safeenchant = temp.get_safeenchant();
		if (temp.getType2() == 2) {
			ggg = ("\n�ູ���� : "+Bless(g)+
					"\n���밡�� �ּ� ���� : "+Min+
					"\n���밡�� �ִ� ���� : "+Max+
					"\n�⺻ AC : "+temp.get_ac()+
					"\n���� ��þƮ : "+safeenchant+
					"\n"+
					"\n"+adadad(g)+
					"\n"+
					"\n"+sdsdsd(g));
		} else if (temp.getType2() == 1) {
			ggg = ("\n�ູ���� : "+Bless(g)+
					"\n���밡�� �ּ� ���� : "+Min+
					"\n���밡�� �ִ� ���� : "+Max+
					"\n������ ������ : "+temp.getDmgSmall()+
					"\nū�� ������ : "+temp.getDmgLarge()+
					"\n���� ��þƮ : "+safeenchant+
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
			Class  +=" ��Ÿ:"+temp.getDmgModifier();
		}  
		if(temp.getHitModifier()!= 0){
			Class  +=" ����:"+temp.getHitModifier();
		}
		if(temp.getDamageReduction()!= 0){
			Class  +=" ������ ����:"+temp.getDamageReduction();
		}  
		if(temp.getWeightReduction()!= 0){
			Class  +=" ���� ����:"+temp.getWeightReduction()+"%";
		}  
		if(temp.getBowDmgup()!= 0){
			Class  +=" ���Ÿ� ��Ÿ:"+temp.getBowDmgup();
		}  
		if(temp.getBowHitup()!= 0){
			Class  +=" ���Ÿ� ����:"+temp.getBowHitup();
		} 
		if(temp.get_defense_water()!= 0){
			Class  +=" �Ӽ����(��):"+temp.get_defense_water();
		}  
		if(temp.get_defense_fire()!= 0){
			Class  +=" �Ӽ����(��):"+temp.get_defense_fire();
		} 
		if(temp.get_defense_earth()!= 0){
			Class  +=" �Ӽ����(��):"+temp.get_defense_earth();
		}  
		if(temp.get_defense_wind()!= 0){
			Class  +=" �Ӽ����(�ٶ�):"+temp.get_defense_wind();
		}
		if(temp.get_regist_stun()!= 0){
			Class  +=" ����(����):"+temp.get_regist_stun();
		}  
		if(temp.get_regist_stone()!= 0){
			Class  +=" ����(��ȭ):"+temp.get_regist_stone();
		} 
		if(temp.get_regist_sleep()!= 0){
			Class  +=" ����(����):"+temp.get_regist_sleep();
		}  
		if(temp.get_regist_freeze()!= 0){
			Class  +=" ����(����):"+temp.get_regist_freeze();
		}
		if(temp.get_regist_sustain()!= 0){
			Class  +=" ����(����):"+temp.get_regist_sustain();
		}  
		if(temp.get_regist_blind()!= 0){
			Class  +=" ����(Ȧ��):"+temp.get_regist_blind();
		}
		ggg = "�ɼ�:"+Class;
		return ggg;
	}

	private String adadad(String g){
		int aaaaa = Integer.parseInt(g);
		L1Item temp = ItemTable.getInstance().getTemplate(aaaaa);
		String ggg = "";
		String Class= "";
		if(temp.isUseRoyal()== true){
			Class  += "���� "; 
		}
		if(temp.isUseKnight()== true){
			Class  += "��� "; 
		}
		if(temp.isUseElf()== true){
			Class  +="���� ";
		} 
		if(temp.isUseMage()== true){
			Class  +="������ "; 
		} 
		if(temp.isUseDarkelf()== true){
			Class  += "��ũ���� ";
		}
		if(temp.isUseDragonKnight()== true){
			Class  += "���� "; 
		} 
		if(temp.isUseBlackwizard()== true){
			Class  +="ȯ����";
		}
		ggg = "���� Ŭ���� : "+Class;
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