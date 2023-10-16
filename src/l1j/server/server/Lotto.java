/** 
 * �ζ� by Chocco
 */

package l1j.server.server;

import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class Lotto extends Thread{

	private static Lotto _instance;							// ���� ��ü ��
	private static int LottoNumber;							// ȸ��
	private static int[] LottoNum;							// 1~6 ��ȣ
	private static int Bonus;								// ���ʽ�
	private static long LottoTime;							// ��÷�� �ð�(ms����)
	private static int One;									// 1��
	private static int Two;									// 2��
	private static int Three;								// 3��
	private static int Four;								// 4��
	private static int Five;								// 5��
	private static int Auto;								// �ڵ�
	private static int None;								// ����
	private static long[] Price;							// ��÷�� �ջ�� �ݾ�
	private static long SellPrice;							// �� �Ǹűݾ�
	private static long GivePrice;							// ��� �ݾ�
	private static long BeforeGive;							// ���� ��� �ݾ�
	private static long BeforeOne;							// ���� ���� �ݾ�(1��)
	private static final long AfterTime = 604800000L;		// ���� �ζ� ��÷��
	private static final long BIGADEN = 10000000000L;		// �ʱ�ȭ��

	/**
	 * ���� ��ü ��ȯ
	 * @return	(Lotto)	���� ��ü
	 */
	public static Lotto getInstance(){
		if (_instance == null) _instance = new Lotto();
		return _instance;
	}
	/**
	 * �⺻ ������
	 */
	private Lotto(){
		LottoNum = new int[6];
		Price = new long[5];
	}
	/**
	 * extedns  Thread�� run Override
	 */
	public void run(){
		while(true){
			try{
				Open();
				sleep(60000L);					// 1�о� �˻�
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * �ζ� ��÷�� ���� ��� �����͸� �����´�.
	 */
	private void Open(){
		Connection c = null;
		PreparedStatement SelectLotto = null;
		PreparedStatement SelectBeforeLotto = null;
		ResultSet UseLotto = null;
		ResultSet UseBeforeLotto = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			SelectLotto = c.prepareStatement("select * from lotto where ��ǥ��¥=? and ��ǥ����=0");
			String Now = LottoController.getInstance().getDate();
			SelectLotto.setString(1, Now);
			UseLotto = SelectLotto.executeQuery();
			// ��÷���̶��
			if(UseLotto.next()){
				LottoTime = System.currentTimeMillis();							// ���� ����� ����..
				LottoNumber = UseLotto.getInt(1);								// ȸ��
				SellPrice = UseLotto.getLong(19);								// �� �Ǹž�
				LottoNum = LottoController.getInstance().getNumber();			// 6�� ��ȣ
				Bonus = LottoController.getInstance().getBonus();				// ���ʽ�
				Arrays.sort(LottoNum);
				// ��÷ ȸ�� �ٷ� �� ��� , �̿� �ݾ� �ջ�
				SelectBeforeLotto = c.prepareStatement("select * from lotto where ȸ��=? and ��ǥ����=1");
				SelectBeforeLotto.setInt(1, LottoNumber-1);
				UseBeforeLotto = SelectBeforeLotto.executeQuery();
				if(UseBeforeLotto.next()){
					BeforeGive = UseBeforeLotto.getLong(22);					// �� �� ���
					BeforeOne = UseBeforeLotto.getLong(23);						// �� �� �̿�
				}
				L1World.getInstance().broadcastServerMessage("\\fV[" + LottoNumber + "] ȸ�� �ζǺ��� ��÷�� �ְڽ��ϴ�.");
				PcLotto();														// Pc ��� �˻� �� ��� ����
				Round();														// �ݾ� ���
				Update();														// �ζ� ��÷���� �� ��ȣ ����
				Insert();														// ���� ȸ�� �ζ� ���
				SendMoney();													// Pc ��÷�� ����
				Thread.sleep(10000L);											// ó�� ��
				L1World.getInstance().broadcastServerMessage("\\fV[" + LottoNumber + "] ȸ�� �ζǺ��� ��÷�� �Ϸ�Ǿ����ϴ�.");
				L1World.getInstance().set_worldChatElabled(false);				// ä�� ���ϰ�..
				Thread.sleep(6000L);												// 
				L1World.getInstance().broadcastServerMessage("\\fV��÷��ȣ : " + LottoNum[0] + " " + LottoNum[1] + " " + LottoNum[2] + " "
						+ LottoNum[3] + " " + LottoNum[4] + " " + LottoNum[5] + " ���ʽ� " + Bonus);
				Thread.sleep(6000L);												// 
				L1World.getInstance().broadcastServerMessage("\\fV���� �ڼ��� ������ ���� �ζ� �Խ��� �Ǵ�");
				L1World.getInstance().broadcastServerMessage("\\fVȨ������ �ζǰԽ�����  �̿����ֽñ� �ٶ��ϴ�.");
				Thread.sleep(6000L);												// 
				L1World.getInstance().broadcastServerMessage("\\fV�����մϴ�.");
				L1World.getInstance().set_worldChatElabled(true);				// ä�� ����
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(UseBeforeLotto);
			SQLUtil.close(SelectBeforeLotto);
			SQLUtil.close(UseLotto);
			SQLUtil.close(SelectLotto);
			SQLUtil.close(c);
		}
	}
	/**
	 * �ζ� ������ ����� �ζǹ�ȣ ���
	 */
	private void PcLotto(){
		Connection c = null;
		PreparedStatement p = null;
		ResultSet r = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select * from lottoapply where ����ȸ��=?");
			p.setInt(1, LottoNumber);
			r = p.executeQuery();
			// ������ ������� �ݺ�
			while(r.next()){
				int[] array = new int[6];
				array[0] = r.getInt(5);
				array[1] = r.getInt(6);
				array[2] = r.getInt(7);
				array[3] = r.getInt(8);
				array[4] = r.getInt(9);
				array[5] = r.getInt(10);
				if(r.getInt(11) == 0) None++;						// �ڵ� or ���� �Ǵ�
				else Auto++;
				Arrays.sort(array);
				PcSave(r.getInt(1), Checking(array), r.getInt(2));
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * ������ ĳ����� ����
	 * @param	(int)	number	��÷���
	 * @param	(int)	id		������Ʈ ���̵�
	 */
	private void PcSave(int num, int number, int id){
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("update lottoapply set ��÷���="+number+" where ���̵�=" + id + " and ��ȣ="+num);
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * �ζ� ��÷ ��� ����
	 */
	private void Update(){
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			long NextPrice = One > 0 ? 0 : Price[0];
			BeforeOne = One > 0 ? 0 : BeforeOne;
			NextPrice += BeforeOne;
			GivePrice += BeforeGive;
			NextPrice = NextPrice > BIGADEN ? 0 : NextPrice;									// �ǿ� ������ �־ 100�� üũ�� �ʱ�ȭ
			p = c.prepareStatement("update lotto set ù��°="+LottoNum[0]+", �ι�°="+LottoNum[1]+", ����°="+LottoNum[2]+", �׹�°="+LottoNum[3]
					+", �ټ���°="+LottoNum[4]+", ������°="+LottoNum[5]+", ���ʽ�="+Bonus+", �ϵ��÷�ڼ�="+One+", �ϵ��÷�ݾ�="+Price[0]
							+", �̵��÷�ڼ�="+Two+", �̵��÷�ݾ�="+Price[1]+", ����÷�ڼ�="+Three+", ����÷�ݾ�="+Price[2]+", ����÷�ڼ�="+Four
							+", ����÷�ݾ�="+Price[3]+", �����÷�ڼ�="+Five+", �����÷�ݾ�="+Price[4]+", �ڵ�����="+Auto+", ��������="+None+", ��αݾ�="+GivePrice
							+", �̿��ݾ�="+NextPrice+", ��ǥ����=1 where ȸ��="+LottoNumber);
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * �ζ� ���� ȸ�� ���
	 */
	private void Insert(){
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("insert into lotto set ȸ��="+(LottoNumber+1) + ", ��ǥ��¥='"+ LottoController.getInstance().getDate(LottoTime+AfterTime) +"', ��ǥ����=0");
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * ��÷�� ĳ������ ��÷�� ����
	 */
	private void SendMoney(){
		Connection c = null;
		PreparedStatement p = null;
		ResultSet r = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select * from lottoapply where ����ȸ��="+ LottoNumber +" order by ��÷��� asc");
			r = p.executeQuery();
			// 1�� �������
			while(r.next()){
				// ��÷����� 0 �̻��̶�� -> ��û�Ǿ��ٸ�
				if(r.getInt(13) > 0){
					L1PcInstance pc = L1World.getInstance().getPlayer(r.getString(3));
					long count = Price[r.getInt(13)-1];
					long one_count = 0;
					// �̿� �ݾ��� �ְ� �ϵ��� �ִٸ�
					if(BeforeOne > 0 && One > 0) one_count = count + (BeforeOne / One);
					// pc �� �ְ� �������̶��...
					if(pc != null && pc.getOnlineStatus() != 0){
						if(r.getInt(13) == 1) pc.getInventory().storeItem(40308, (int) one_count);		// 1��
						else pc.getInventory().storeItem(40308, (int) count);							// �� ���� ��
						pc.sendPackets(new S_SystemMessage("\\fV�����մϴ�. "+r.getInt(13)+"� ��÷�Ǿ����ϴ�."));
						if(r.getInt(13) == 1) pc.sendPackets(new S_SystemMessage("\\fV��÷�� "+one_count+"���� ���޹޾ҽ��ϴ�."));
						else pc.sendPackets(new S_SystemMessage("\\fV��÷�� "+count+"���� ���޹޾ҽ��ϴ�."));
						// ���������� �ʴٸ�..
					}else{
						PcDbSave(40308, r.getInt(2), count);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * ���ӵ��� �ʴ� ������ ��� ��ϸ� �Ѵ�.
	 * @param	(int)	itemId	������ ������Ʈ ��ȣ
	 * @param	(int)	charId	ĳ���� ������Ʈ ��ȣ
	 * @param	(long)	count	����
	 */
	public void PcDbSave(int itemId, int charId, long count){
		Connection c = null;
		ResultSet NoneAden = null;
		ResultSet UseAden = null;
		PreparedStatement SelectId = null;
		PreparedStatement InsertAden = null;
		PreparedStatement isAden = null;
		PreparedStatement AdenUpdate = null;
		/**
		 * 1. ���� �Ƶ��� ������ �ִ��� �Ǵ�
		 * 2. ������ �ִٸ� ���� �Ƶ� ���� �ľ�
		 * 3. ����+��÷�� ������Ʈ
		 * 4. �Ƶ��� ���ٸ� ���̺� ������ ��ȣ �ľ�
		 * 5. �Ƶ� ������ �߰�
		 */
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			isAden = c.prepareStatement("select * from character_items where char_id=? and item_id=?");
			isAden.setInt(1, charId);
			isAden.setInt(2, itemId);
			UseAden = isAden.executeQuery();
			if(UseAden.next()){
				long Aden = UseAden.getLong(5);
				AdenUpdate = c.prepareStatement("update character_items set count=count+" + Aden + " where char_id="+charId+" and item_id="+itemId);
				AdenUpdate.execute();
			}else{
				SelectId = c.prepareStatement("select max(id)+1 as cnt from character_items order by id");
				NoneAden = SelectId.executeQuery();
				int Id = 0;
				if(NoneAden.next()) Id = NoneAden.getInt("cnt");
				InsertAden = c.prepareStatement("insert into character_items set id=?, item_id=?, char_id=?, item_name=?, count=?, is_equipped=?, enchantlvl=?, is_id=?, durability=?");
				InsertAden.setInt(1, Id);
				InsertAden.setInt(2, itemId);
				InsertAden.setInt(3, charId);
				InsertAden.setString(4, "�Ƶ���");
				InsertAden.setLong(5, count);
				InsertAden.setInt(6, 0);
				InsertAden.setInt(7, 0);
				InsertAden.setInt(8, 0);
				InsertAden.setInt(9, 0);
				InsertAden.execute();
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(NoneAden);
			SQLUtil.close(UseAden);
			SQLUtil.close(SelectId);
			SQLUtil.close(InsertAden);
			SQLUtil.close(isAden);
			SQLUtil.close(AdenUpdate);
			SQLUtil.close(c);
		}
	}
	/**
	 * ���� ��÷���� ���ͳ�
	 * @return	(String)	���� ȸ�� ��ǥ��
	 */
	public String[] getLottoDate(){
		String[] date = new String[2];
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select * from lotto where ��ǥ��¥ >= '" + LottoController.getInstance().getDate() + "' and ��ǥ����=0");
			r = p.executeQuery();
			if(r.next()){
				date[0] = Integer.toString(r.getInt(1));
				date[1] = r.getString(24);
			}
		}catch(Exception e){
		}finally{
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
		return date;
	}
	/**
	 * �ζ� ������ ���� - ��� ���
	 * @param	(L1PcInstance)	c				������ü
	 * @param	(String)		num				ȸ��
	 * @param	(int)			type			�ڵ�/����
	 * @param	(int...)		lotto_number	�ζ� ��ȣ
	 */
	public void BuyPc(L1PcInstance c, String num, int type, int itemId, int... lotto_number){
		ResultSet r = null;
		Connection con = null;
		PreparedStatement pp = null;
		PreparedStatement p = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			pp = con.prepareStatement("select max(��ȣ)+1 as cnt from lottoapply order by ��ȣ");
			r = pp.executeQuery();
			int Id = 0;
			if(r.next()) Id = r.getInt("cnt");
			p = con.prepareStatement("insert into lottoapply set ��ȣ=?, ���̵�=?, ĳ����=?, ����ȸ��=?, ù��°=?, �ι�°=?, ����°=?, �׹�°=?, �ټ���°=?, ������°=?, �ڵ�����=?, ���ų�¥=?, �����۾��̵�=?");
			p.setInt(1, Id);
			p.setInt(2, c.getId());
			p.setString(3, c.getName());
			p.setInt(4, Integer.parseInt(num));
			p.setInt(5, lotto_number[0]);
			p.setInt(6, lotto_number[1]);
			p.setInt(7, lotto_number[2]);
			p.setInt(8, lotto_number[3]);
			p.setInt(9, lotto_number[4]);
			p.setInt(10, lotto_number[5]);
			p.setInt(11, type);
			p.setString(12, LottoController.getInstance().getDate());
			p.setInt(13, itemId);
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(pp);
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(con);
		}
	}
	/**
	 * �� ȸ���� 10�� �̻� ���� �Ұ�
	 * @param	(L1PcInstance)	c				������ü
	 * @param	(String)		num				ȸ��
	 * @return	(boolean)		10���̻� true
	 */
	public boolean OverBuy(L1PcInstance c, String num){
		ResultSet r = null;
		Connection con = null;
		boolean isOver = false;
		PreparedStatement p = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			p = con.prepareStatement("select count(*) as cnt from lottoapply where ���̵�=? and ����ȸ��=?");
			p.setInt(1, c.getId());
			p.setInt(2, Integer.parseInt(num));
			r = p.executeQuery();
			int cnt = 0;
			if(r.next()) cnt = r.getInt("cnt");
			if(cnt >= 20 ){
				isOver = true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(con);
		}
		return isOver;
	}
	/**
	 * �ǸŰ��� ����
	 * @param	(int)			price			����
	 * @param	(String)		num				ȸ��
	 */
	public void AddPrice(int price, String num){
		ResultSet r = null;
		Connection con = null;
		PreparedStatement p = null;
		PreparedStatement pp = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			pp = con.prepareStatement("select ���Ǹűݾ� as price from lotto where ȸ��=?");
			pp.setInt(1, Integer.parseInt(num));
			r = pp.executeQuery();
			int B_price = 0;
			if(r.next()) B_price = r.getInt("price");
			p = con.prepareStatement("update lotto set ���Ǹűݾ�=? where ȸ��=?");
			p.setInt(1, price+B_price);
			p.setInt(2, Integer.parseInt(num));
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(r);
			SQLUtil.close(pp);
			SQLUtil.close(p);
			SQLUtil.close(con);
		}
	}
	/**
	 * �ζ� ��� ���� (ĳ���� �κ����� ����� ����ϵ� ����)
	 * @param	(int)			price			����
	 * @param	(int)			itemId			������ ������Ʈ ���̵�
	 */
	public void removeLotto(L1PcInstance c, int itemId){
		Connection con = null;
		PreparedStatement p = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			p = con.prepareStatement("delete from lottoapply where ���̵�=? and �����۾��̵�=?");
			p.setInt(1, c.getId());
			p.setInt(2, itemId);
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(p);
			SQLUtil.close(con);
		}
	}
	/**
	 * �ζ� ��÷��� üũ
	 * 1�� : 6�� �ڸ� ����						// ��÷�ݾ��� 60% 
	 * 2�� : 5�� �ڸ� ���� + ���ʽ� ����		// ��÷�ݾ��� 10%
	 * 3�� : 5�� �ڸ� ����						// ��÷�ݾ��� 10%
	 * 4�� : 4�� �ڸ� ����						// ��÷�ݾ��� 20%
	 * 5�� : 3�� �ڸ� ����						// 5000��
	 * @param	(int[])	lotto	ĳ���� �ζ� ��ȣ
	 * @return	(int)			��÷ ���
	 */
	private int Checking(int... lotto){
		int TempCount = 0;
		int Num = 0;
		// 1�� �̶�� Arrays API 
		if(Arrays.equals(LottoNum, lotto)){
			Num = 1;
			One++;
			// 2~5�� ó��
		}else{
			for(int number : LottoNum){
				// 6���߿� 1���� ������ �հ����� �� 5���� �´´ٸ� 2��
				if(number == lotto[0] || number == lotto[1] || number == lotto[2] || number == lotto[3] || number == lotto[4]
						|| number == lotto[5]){
					TempCount++;
				}
			}
			// 5��+���ʽ� -> 2��
			if(TempCount == 5 && Bonus == lotto[0] || Bonus == lotto[1] || Bonus == lotto[2] || Bonus == lotto[3] || Bonus == lotto[4]
					|| Bonus == lotto[5]){
				Num = 2;
				Two++;
				// 5�� -> 3��
			}else if(TempCount == 5){
				Num = 3;
				Three++;
				// 4�� -> 4��
			}else if(TempCount == 4){
				Num = 4;
				Four++;
				// 3�� -> 5��
			}else if(TempCount == 3){
				Num = 5;
				Five++;
			}else{
				Num = 0;
			}
		}
		return Num;
	}
	/**
	 * �� ����� ��÷ �ݾ��� �Ǹž�/��÷��/��÷�ڼ� ������ ���
	 */
	private void Round(){
		long sell = (SellPrice * 60) / 100;											// 60% ��÷��
		GivePrice = (SellPrice * 40) / 100;											// 40% ��α�
		// 1~4���� ��÷�� ��ü �Ǹű��� 60% �� 5�� �ݾ��� ������ �ݾ����� ���
		sell = sell - (Five * 5000);
		Price[0] = One > 0 ? ((sell * 60) / 100) / One : ((sell * 60) / 100);		// 60% 1�� ��÷�� / ��÷�� ��
		Price[1] = Two > 0 ? ((sell * 10) / 100) / Two : ((sell * 10) / 100);		// 10% 2�� ��÷�� / ��÷�� ��
		Price[2] = Three > 0 ? ((sell * 10) / 100) / Three : ((sell * 10) / 100);	// 10% 3�� ��÷�� / ��÷�� ��
		Price[3] = Four > 0 ? ((sell * 20) / 100) / Four : ((sell * 20) / 100);		// 20% 4�� ��÷�� / ��÷�� ��
		Price[4] = Five > 0 ? 5000 : 0;												// 5�� ��÷�� -> 5000��
		// 2����� ��÷�� �ϳ��� ���ٸ� ��� ��ν�Ų��.
		if(Two == 0 && Three == 0 && Four == 0 && Five == 0) GivePrice += Price[1] + Price[2] + Price[3] + Price[4];
	}
}