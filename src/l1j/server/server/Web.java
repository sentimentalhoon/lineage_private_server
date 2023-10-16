
/**
 * PHP 서버단에서 넘어온 패킷 처리를 위한 웹 전용 서버
 * 제작 : 쪼꼬
 */

package l1j.server.server;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1EtcItem;
import l1j.server.server.utils.SQLUtil;

public class Web extends Thread {

	private ServerSocket _serverSocket;
	private int _port = 3001;
	private InputStream _in;
	private PrintWriter _out;

	private static Web instance;

	@Override
	public synchronized void run() {
		while(true){
			Socket connection = null;
			try{	
				connection = _serverSocket.accept();
				if(connection != null){
					_in = connection.getInputStream();
					_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())));
					byte[] data = new byte[256];
					_in.read(data);
					String sTemp = new String(data).trim();
					// T@DATA
					System.out.println(sTemp);
					if(sTemp.charAt(0) == 'L') readLotto(sTemp.substring(2));
					else if(sTemp.charAt(0) == 'P') readPoint(sTemp.substring(2));
				}
			}catch (Exception e){}
		}
	}

	public static Web getInstance(){
		if(instance == null) instance = new Web();
		return instance;
	}
	private Web(){
		super("Web");
		try{
			_serverSocket = new ServerSocket(_port);
			System.out.println("Web Server 완료");
		}catch(Exception e){
			System.out.println("Web Server 실패");
		}
		this.start();
	}

	private void readPoint(String data){
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		int objId = 0;
		try{
			//			System.out.println(data);		// no ^ char_name ^ type ^ number ^ count
			StringTokenizer s = new StringTokenizer(data, ":");
			String name = s.nextToken();
			int type = Integer.parseInt(s.nextToken());
			int number = Integer.parseInt(s.nextToken());
			int count = Integer.parseInt(s.nextToken());
			L1PcInstance cha = L1World.getInstance().getPlayer(name);
			if(cha != null && cha.getOnlineStatus() != 0){
				cha.getInventory().storeItem(number, count);				// 아이템 지급
			}else{
				c = L1DatabaseFactory.getInstance().getConnection();
				p = c.prepareStatement("select objid as objId from characters where char_name=?");
				p.setString(1, name);
				r = p.executeQuery();
				if(r.next()) objId = r.getInt("objId");
				if(type == 1 || type == 2 || type == 3){
					for(int i = 0; i < count; i++) DbSave(objId, number, "[포인트] 아이템", 1);
				}else DbSave(objId, number, "[포인트] 아이템", count);
				SQLUtil.close(r);
				SQLUtil.close(p);
				SQLUtil.close(c);
			}
		}catch(Exception e){
			System.out.println("포인트샵 저장 실패");
			e.printStackTrace();
		}
	}

	private void readLotto(String data){
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		int[] number = new int[6];
		String[] date = Lotto.getInstance().getLottoDate();
		int itemId = 0;
		int objId = 0;
		try{
			//			System.out.println(data);		// no ^ char_name ^ 1 ^ 2 ^ 3 ^ 4 ^ 5 ^ 6 ^ objId
			StringTokenizer s = new StringTokenizer(data, ":");
			int no = Integer.parseInt(s.nextToken());
			String name = s.nextToken();
			number[0] = Integer.parseInt(s.nextToken());
			number[1] = Integer.parseInt(s.nextToken());
			number[2] = Integer.parseInt(s.nextToken());
			number[3] = Integer.parseInt(s.nextToken());
			number[4] = Integer.parseInt(s.nextToken());
			number[5] = Integer.parseInt(s.nextToken());
			objId = Integer.parseInt(s.nextToken());
			L1PcInstance cha = L1World.getInstance().getPlayer(name);
			if(cha != null && cha.getOnlineStatus() != 0){
				itemId = addItem(cha, number);
			}else{
				String item_name = "로또[WEB] "+date[0]+"회차   추첨일 : "+date[1]+"    "+number[0]+" "+number[1]+" "+number[2]+" "+number[3]+" " +number[4]+" "+number[5];
				itemId = DbSave(objId, 500017, item_name, 1);
			}
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("update lottoapply set 아이템아이디=? where 번호=?");
			p.setInt(1, itemId);
			p.setInt(2, no);
			p.execute();
			SQLUtil.close(r);
			SQLUtil.close(p);
			SQLUtil.close(c);
			AddPrice(15000, date[0]);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendPacket(String msg){
		_out.println(msg);
		_out.flush();
	}

	public static void main(String... msg){
		Web.getInstance();
	}

	public int addItem(L1PcInstance c, int[] number){
		String[] date = Lotto.getInstance().getLottoDate();
		L1ItemInstance item = new L1ItemInstance();
		item.setId(ObjectIdFactory.getInstance().nextId());
		L1EtcItem lotto = new L1EtcItem();
		lotto.setType2(0);
		lotto.setItemId(500016);
		String name = "로또[WEB] "+date[0]+"회차   추첨일 : "+date[1]+"    "+number[0]+" "+number[1]+" "+number[2]+" "+number[3]+" " +number[4]+" "+number[5];
		lotto.setName(name);
		lotto.setNameId(name);
		lotto.setType(8);
		lotto.setType1(8);
		lotto.setMaterial(5);
		lotto.setWeight(30);
		lotto.setGfxId(2275);
		lotto.setGroundGfxId(151);
		lotto.setMinLevel(0);
		lotto.setMaxLevel(0);
		lotto.setBless(1);
		lotto.setTradable(false);
		lotto.setDmgSmall(0);
		lotto.setDmgLarge(0);
		lotto.set_stackable(false);
		lotto.set_locx(0);
		lotto.set_locy(0);
		lotto.set_mapid((short)0);
		lotto.set_delayid(2);
		lotto.set_delaytime(10);
		lotto.set_delayEffect(0);
		lotto.setFoodVolume(0);
		lotto.setToBeSavedAtOnce(true);
		lotto.setlogcheckitem(1);
		item.setItem(lotto);
		item.set_durability(item.get_durability());
		L1World.getInstance().storeObject(item);
		item.setIdentified(true);
		c.getInventory().storeItem(item);
		c.sendPackets(new S_SystemMessage("로또 구매가 완료되었습니다."));
		c.sendPackets(new S_SystemMessage("구매한 로또를 버리시거나 삭제하시면 무효처리됩니다."));
		return item.getId();
	}

	/**
	 * 판매가격 갱신
	 * @param	(int)			price			가격
	 * @param	(String)		num				회차
	 */
	public void AddPrice(int price, String num){
		ResultSet r = null;
		Connection con = null;
		PreparedStatement p = null;
		PreparedStatement pp = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			pp = con.prepareStatement("select 총판매금액 as price from lotto where 회차=?");
			pp.setInt(1, Integer.parseInt(num));
			r = pp.executeQuery();
			int B_price = 0;
			if(r.next()) B_price = r.getInt("price");
			p = con.prepareStatement("update lotto set 총판매금액=? where 회차=?");
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

	public int DbSave(int charId, int itemId, String name, int count){
		int Id = 0;
		Connection c = null;
		ResultSet NoneAden = null;
		PreparedStatement SelectId = null;
		PreparedStatement InsertAden = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			SelectId = c.prepareStatement("select max(id)+1 as cnt from character_items order by id");
			NoneAden = SelectId.executeQuery();
			Id = 0;
			if(NoneAden.next()) Id = NoneAden.getInt("cnt");
			InsertAden = c.prepareStatement("insert into character_items set id=?, item_id=?, char_id=?, item_name=?, count=?, is_equipped=?, enchantlvl=?, is_id=?, durability=?, bless=?");
			InsertAden.setInt(1, Id);
			InsertAden.setInt(2, itemId);
			InsertAden.setInt(3, charId);
			InsertAden.setString(4, name);
			InsertAden.setLong(5, count);
			InsertAden.setInt(6, 0);
			InsertAden.setInt(7, 0);
			InsertAden.setInt(8, 1);
			InsertAden.setInt(9, 0);
			InsertAden.setInt(10, 1);
			InsertAden.execute();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(NoneAden);
			SQLUtil.close(SelectId);
			SQLUtil.close(InsertAden);
			SQLUtil.close(c);
		}
		return Id;
	}
}