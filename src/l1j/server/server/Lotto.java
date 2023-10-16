/** 
 * 로또 by Chocco
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

	private static Lotto _instance;							// 단일 객체 용
	private static int LottoNumber;							// 회차
	private static int[] LottoNum;							// 1~6 번호
	private static int Bonus;								// 보너스
	private static long LottoTime;							// 추첨한 시간(ms단위)
	private static int One;									// 1등
	private static int Two;									// 2등
	private static int Three;								// 3등
	private static int Four;								// 4등
	private static int Five;								// 5등
	private static int Auto;								// 자동
	private static int None;								// 수동
	private static long[] Price;							// 당첨의 합산된 금액
	private static long SellPrice;							// 총 판매금액
	private static long GivePrice;							// 기부 금액
	private static long BeforeGive;							// 예전 기부 금액
	private static long BeforeOne;							// 예전 누적 금액(1등)
	private static final long AfterTime = 604800000L;		// 다음 로또 추첨일
	private static final long BIGADEN = 10000000000L;		// 초기화용

	/**
	 * 단일 객체 반환
	 * @return	(Lotto)	단일 객체
	 */
	public static Lotto getInstance(){
		if (_instance == null) _instance = new Lotto();
		return _instance;
	}
	/**
	 * 기본 생성자
	 */
	private Lotto(){
		LottoNum = new int[6];
		Price = new long[5];
	}
	/**
	 * extedns  Thread의 run Override
	 */
	public void run(){
		while(true){
			try{
				Open();
				sleep(60000L);					// 1분씩 검사
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 로또 추첨을 위해 디비 데이터를 가져온다.
	 */
	private void Open(){
		Connection c = null;
		PreparedStatement SelectLotto = null;
		PreparedStatement SelectBeforeLotto = null;
		ResultSet UseLotto = null;
		ResultSet UseBeforeLotto = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			SelectLotto = c.prepareStatement("select * from lotto where 발표날짜=? and 발표여부=0");
			String Now = LottoController.getInstance().getDate();
			SelectLotto.setString(1, Now);
			UseLotto = SelectLotto.executeQuery();
			// 당첨날이라면
			if(UseLotto.next()){
				LottoTime = System.currentTimeMillis();							// 다음 기록을 위해..
				LottoNumber = UseLotto.getInt(1);								// 회차
				SellPrice = UseLotto.getLong(19);								// 총 판매액
				LottoNum = LottoController.getInstance().getNumber();			// 6개 번호
				Bonus = LottoController.getInstance().getBonus();				// 보너스
				Arrays.sort(LottoNum);
				// 추첨 회차 바로 전 기부 , 이월 금액 합산
				SelectBeforeLotto = c.prepareStatement("select * from lotto where 회차=? and 발표여부=1");
				SelectBeforeLotto.setInt(1, LottoNumber-1);
				UseBeforeLotto = SelectBeforeLotto.executeQuery();
				if(UseBeforeLotto.next()){
					BeforeGive = UseBeforeLotto.getLong(22);					// 그 전 기부
					BeforeOne = UseBeforeLotto.getLong(23);						// 그 전 이월
				}
				L1World.getInstance().broadcastServerMessage("\\fV[" + LottoNumber + "] 회차 로또복권 추첨이 있겠습니다.");
				PcLotto();														// Pc 디비 검사 및 등수 저장
				Round();														// 금액 계산
				Update();														// 로또 당첨여부 및 번호 갱신
				Insert();														// 다음 회차 로또 등록
				SendMoney();													// Pc 당첨금 지급
				Thread.sleep(10000L);											// 처리 후
				L1World.getInstance().broadcastServerMessage("\\fV[" + LottoNumber + "] 회차 로또복권 추첨이 완료되었습니다.");
				L1World.getInstance().set_worldChatElabled(false);				// 채팅 못하게..
				Thread.sleep(6000L);												// 
				L1World.getInstance().broadcastServerMessage("\\fV당첨번호 : " + LottoNum[0] + " " + LottoNum[1] + " " + LottoNum[2] + " "
						+ LottoNum[3] + " " + LottoNum[4] + " " + LottoNum[5] + " 보너스 " + Bonus);
				Thread.sleep(6000L);												// 
				L1World.getInstance().broadcastServerMessage("\\fV보다 자세한 사항은 마을 로또 게시판 또는");
				L1World.getInstance().broadcastServerMessage("\\fV홈페이지 로또게시판을  이용해주시기 바랍니다.");
				Thread.sleep(6000L);												// 
				L1World.getInstance().broadcastServerMessage("\\fV감사합니다.");
				L1World.getInstance().set_worldChatElabled(true);				// 채팅 가능
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
	 * 로또 구입한 사람의 로또번호 등록
	 */
	private void PcLotto(){
		Connection c = null;
		PreparedStatement p = null;
		ResultSet r = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select * from lottoapply where 참여회차=?");
			p.setInt(1, LottoNumber);
			r = p.executeQuery();
			// 구입한 사람들대로 반복
			while(r.next()){
				int[] array = new int[6];
				array[0] = r.getInt(5);
				array[1] = r.getInt(6);
				array[2] = r.getInt(7);
				array[3] = r.getInt(8);
				array[4] = r.getInt(9);
				array[5] = r.getInt(10);
				if(r.getInt(11) == 0) None++;						// 자동 or 수동 판단
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
	 * 등수대로 캐릭디비 저장
	 * @param	(int)	number	당첨등수
	 * @param	(int)	id		오브젝트 아이디
	 */
	private void PcSave(int num, int number, int id){
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("update lottoapply set 당첨등수="+number+" where 아이디=" + id + " and 번호="+num);
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * 로또 추첨 결과 갱신
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
			NextPrice = NextPrice > BIGADEN ? 0 : NextPrice;									// 악용 소지가 있어서 100억 체크후 초기화
			p = c.prepareStatement("update lotto set 첫번째="+LottoNum[0]+", 두번째="+LottoNum[1]+", 세번째="+LottoNum[2]+", 네번째="+LottoNum[3]
					+", 다섯번째="+LottoNum[4]+", 여섯번째="+LottoNum[5]+", 보너스="+Bonus+", 일등당첨자수="+One+", 일등당첨금액="+Price[0]
							+", 이등당첨자수="+Two+", 이등당첨금액="+Price[1]+", 삼등당첨자수="+Three+", 삼등당첨금액="+Price[2]+", 사등당첨자수="+Four
							+", 사등당첨금액="+Price[3]+", 오등당첨자수="+Five+", 오등당첨금액="+Price[4]+", 자동구매="+Auto+", 수동구매="+None+", 기부금액="+GivePrice
							+", 이월금액="+NextPrice+", 발표여부=1 where 회차="+LottoNumber);
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * 로또 다음 회차 등록
	 */
	private void Insert(){
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("insert into lotto set 회차="+(LottoNumber+1) + ", 발표날짜='"+ LottoController.getInstance().getDate(LottoTime+AfterTime) +"', 발표여부=0");
			p.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	/**
	 * 당첨된 캐릭으로 당첨금 지급
	 */
	private void SendMoney(){
		Connection c = null;
		PreparedStatement p = null;
		ResultSet r = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select * from lottoapply where 참여회차="+ LottoNumber +" order by 당첨등수 asc");
			r = p.executeQuery();
			// 1등 순서대로
			while(r.next()){
				// 당첨등수가 0 이상이라면 -> 당청되었다면
				if(r.getInt(13) > 0){
					L1PcInstance pc = L1World.getInstance().getPlayer(r.getString(3));
					long count = Price[r.getInt(13)-1];
					long one_count = 0;
					// 이월 금액이 있고 일등이 있다면
					if(BeforeOne > 0 && One > 0) one_count = count + (BeforeOne / One);
					// pc 가 있고 접속중이라면...
					if(pc != null && pc.getOnlineStatus() != 0){
						if(r.getInt(13) == 1) pc.getInventory().storeItem(40308, (int) one_count);		// 1등
						else pc.getInventory().storeItem(40308, (int) count);							// 그 이하 등
						pc.sendPackets(new S_SystemMessage("\\fV축하합니다. "+r.getInt(13)+"등에 당첨되었습니다."));
						if(r.getInt(13) == 1) pc.sendPackets(new S_SystemMessage("\\fV당첨금 "+one_count+"원을 지급받았습니다."));
						else pc.sendPackets(new S_SystemMessage("\\fV당첨금 "+count+"원을 지급받았습니다."));
						// 접속중이지 않다면..
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
	 * 접속되지 않는 유저는 디비에 기록만 한다.
	 * @param	(int)	itemId	아이템 오브젝트 번호
	 * @param	(int)	charId	캐릭터 오브젝트 번호
	 * @param	(long)	count	갯수
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
		 * 1. 기존 아덴을 가지고 있는지 판단
		 * 2. 가지고 있다면 기존 아덴 갯수 파악
		 * 3. 기존+당첨금 업데이트
		 * 4. 아덴이 없다면 테이블 마지막 번호 파악
		 * 5. 아덴 데이터 추가
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
				InsertAden.setString(4, "아데나");
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
	 * 다음 추첨일을 리터널
	 * @return	(String)	다음 회차 발표일
	 */
	public String[] getLottoDate(){
		String[] date = new String[2];
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select * from lotto where 발표날짜 >= '" + LottoController.getInstance().getDate() + "' and 발표여부=0");
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
	 * 로또 복권은 구입 - 디비에 기록
	 * @param	(L1PcInstance)	c				유저객체
	 * @param	(String)		num				회차
	 * @param	(int)			type			자동/수동
	 * @param	(int...)		lotto_number	로또 번호
	 */
	public void BuyPc(L1PcInstance c, String num, int type, int itemId, int... lotto_number){
		ResultSet r = null;
		Connection con = null;
		PreparedStatement pp = null;
		PreparedStatement p = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			pp = con.prepareStatement("select max(번호)+1 as cnt from lottoapply order by 번호");
			r = pp.executeQuery();
			int Id = 0;
			if(r.next()) Id = r.getInt("cnt");
			p = con.prepareStatement("insert into lottoapply set 번호=?, 아이디=?, 캐릭명=?, 참여회차=?, 첫번째=?, 두번째=?, 세번째=?, 네번째=?, 다섯번째=?, 여섯번째=?, 자동여부=?, 구매날짜=?, 아이템아이디=?");
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
	 * 한 회차당 10개 이상 구매 불가
	 * @param	(L1PcInstance)	c				유저객체
	 * @param	(String)		num				회차
	 * @return	(boolean)		10개이상 true
	 */
	public boolean OverBuy(L1PcInstance c, String num){
		ResultSet r = null;
		Connection con = null;
		boolean isOver = false;
		PreparedStatement p = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			p = con.prepareStatement("select count(*) as cnt from lottoapply where 아이디=? and 참여회차=?");
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
	/**
	 * 로또 기록 삭제 (캐릭이 인벤에서 지우면 디비기록도 삭제)
	 * @param	(int)			price			가격
	 * @param	(int)			itemId			아이템 오브젝트 아이디
	 */
	public void removeLotto(L1PcInstance c, int itemId){
		Connection con = null;
		PreparedStatement p = null;
		try{
			con = L1DatabaseFactory.getInstance().getConnection();
			p = con.prepareStatement("delete from lottoapply where 아이디=? and 아이템아이디=?");
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
	 * 로또 당첨등수 체크
	 * 1등 : 6개 자리 동일						// 당첨금액의 60% 
	 * 2등 : 5개 자리 동일 + 보너스 동일		// 당첨금액의 10%
	 * 3등 : 5개 자리 동일						// 당첨금액의 10%
	 * 4등 : 4개 자리 동일						// 당첨금액의 20%
	 * 5등 : 3개 자리 동일						// 5000원
	 * @param	(int[])	lotto	캐릭별 로또 번호
	 * @return	(int)			당첨 등수
	 */
	private int Checking(int... lotto){
		int TempCount = 0;
		int Num = 0;
		// 1등 이라면 Arrays API 
		if(Arrays.equals(LottoNum, lotto)){
			Num = 1;
			One++;
			// 2~5등 처리
		}else{
			for(int number : LottoNum){
				// 6개중에 1개라도 맞으면 합격으로 총 5개가 맞는다면 2등
				if(number == lotto[0] || number == lotto[1] || number == lotto[2] || number == lotto[3] || number == lotto[4]
						|| number == lotto[5]){
					TempCount++;
				}
			}
			// 5개+보너스 -> 2등
			if(TempCount == 5 && Bonus == lotto[0] || Bonus == lotto[1] || Bonus == lotto[2] || Bonus == lotto[3] || Bonus == lotto[4]
					|| Bonus == lotto[5]){
				Num = 2;
				Two++;
				// 5개 -> 3등
			}else if(TempCount == 5){
				Num = 3;
				Three++;
				// 4개 -> 4등
			}else if(TempCount == 4){
				Num = 4;
				Four++;
				// 3개 -> 5등
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
	 * 각 등수별 당첨 금액을 판매액/당첨금/당첨자수 비율로 계산
	 */
	private void Round(){
		long sell = (SellPrice * 60) / 100;											// 60% 당첨금
		GivePrice = (SellPrice * 40) / 100;											// 40% 기부금
		// 1~4등의 당첨금 전체 판매금의 60% 중 5등 금액을 제외한 금액으로 계산
		sell = sell - (Five * 5000);
		Price[0] = One > 0 ? ((sell * 60) / 100) / One : ((sell * 60) / 100);		// 60% 1등 당첨금 / 당첨자 수
		Price[1] = Two > 0 ? ((sell * 10) / 100) / Two : ((sell * 10) / 100);		// 10% 2등 당첨금 / 당첨자 수
		Price[2] = Three > 0 ? ((sell * 10) / 100) / Three : ((sell * 10) / 100);	// 10% 3등 당첨금 / 당첨자 수
		Price[3] = Four > 0 ? ((sell * 20) / 100) / Four : ((sell * 20) / 100);		// 20% 4등 당첨금 / 당첨자 수
		Price[4] = Five > 0 ? 5000 : 0;												// 5등 당첨금 -> 5000원
		// 2등부터 당첨이 하나라도 없다면 모두 기부시킨다.
		if(Two == 0 && Three == 0 && Four == 0 && Five == 0) GivePrice += Price[1] + Price[2] + Price[3] + Price[4];
	}
}