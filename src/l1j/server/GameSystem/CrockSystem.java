package l1j.server.GameSystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.EvaSystemTable;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1FieldObjectInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.BaseTime;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.gametime.TimeListener;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1EvaSystem;
import l1j.server.server.utils.L1SpawnUtil;

public class CrockSystem implements TimeListener{

	private static CrockSystem _instance;

	public static CrockSystem getInstance() {
		if (_instance == null) {
			_instance = new CrockSystem();
			RealTimeClock.getInstance().addListener(_instance);
		}
		return _instance;
	}

	static L1EvaSystem eva = EvaSystemTable.getInstance().getSystem(1);
	private Calendar OpenTime = eva.getEvaTime();// 오픈시간
	private Calendar CloseTime = (Calendar) eva.getEvaTime().clone();// 닫는시간
	private Calendar BossTime = (Calendar) eva.getEvaTime().clone();// 보스 열리는 시간
	private Calendar ContinuationTime = (Calendar) eva.getEvaTime().clone();// 보스가 연장되어

	/**
	 * 균열 시간 설정
	 */
	private static final int period = 240; // 분 단위 default : 48시간
	private static final int extendperiod = 59; // 추가 30분

	/** 균열이 열렸는지 아닌지 */
	private boolean isOpen = false;
	/** 보스 시간이 시작 되었는지 */
	private boolean isBossTime = false;

	/** 시간의 균열 테베 보스 횟수 */
	private static int dieCount = 0;

	/** 균열 좌표 */
	private static final int[][] loc = {
		{ 32728, 32704, 4 }, //11 0
		{ 32827, 32658, 4 }, //12 1
		{ 32852, 32713, 4 }, //13 2
		{ 32914, 33427, 4 }, //21 3
		{ 32962, 33251, 4 }, //22 4
		{ 32908, 33169, 4 }, //23 5
		{ 34272, 33361, 4 }, //31 6
		{ 34258, 33202, 4 }, //32 7
		{ 34225, 33313, 4 }  //33 8
	};

	/** 보스방 선착순 20명을 담기 위한 리스트 */
	private static final ArrayList<L1PcInstance> sList = new ArrayList<L1PcInstance>();

	/** 시각 데이터 포맷 */
	private static final SimpleDateFormat ss = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

	private CrockSystem() {
		CloseTime.add(Calendar.MINUTE, 119);// 2시간 
		BossTime.add(Calendar.MINUTE, 99);//  1시간 40분
		if (eva.getOpenContinuation() == 1) {
			isOpen = true;
			ContinuationTime.add(Calendar.MINUTE, extendperiod);
			ready();
		}
	}

	/**
	 * 균열이 열렸는지 상태.
	 */
	private void checkCrock(BaseTime time) {
		if (eva.getOpenContinuation() == 1) {
			if (ContinuationTime.before(time.getCalendar())) {// 연장시간이 지났다면..
				clear();
			}
			return;
		}
		if (OpenTime.before(time.getCalendar()) && CloseTime.after(time.getCalendar())) {// 오픈시간
			if (!isOpen()) {
				setOpen(true);
				ready();
				L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1469));// 열렸다~
			} else {
				if (BossTime.before(time.getCalendar())) {// 보스타임 시작이 지났다면
					if (!isBossTime()) {
						setBossTime(true);
						bossStart();
					}
				}
			}
		} else if (CloseTime.before(time.getCalendar())) {// 종료 시간 후라면
			if (isOpen()) {
				if(isBossDie()){
					CrockContinuation();
				} else {
					setOpen(false);
					setBossTime(false);
					clear();
				}
			}
		}
	}

	private void ready() {
		if (eva.getMoveLocation() == 0) {
			eva.setOpenLocation((int)(Math.random() * 8));
			eva.setMoveLocation((int) (Math.random() * 2 + 1));
		}
		//System.out.println("열린곳"+ eva.getOpenLocation());
		//System.out.println("이동하는곳" +eva.getMoveLocation());
		int OL = eva.getOpenLocation();
		L1SpawnUtil.spawn2(loc[OL][0], loc[OL][1], (short) loc[OL][2], 4500100, 0, 0, 0);// 위치에 스폰한다
		EvaSystemTable.getInstance().updateSystem(eva);
	}

	private void bossStart() {
		// 보스를 스폰하고 보스 타임을 잰다
		switch(eva.getMoveLocation()){
		case 1:// 테베
			L1SpawnUtil.spawn2(32794, 32825, (short) 782, 400016, 0, 1920*1000, 0);
			L1SpawnUtil.spawn2(32794, 32836, (short) 782, 400017, 0, 1920*1000, 0);
			L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1471)); // 오시리스 : 어리석은 것들..이곳이 어디라고!! 아누비스! 호루스! 저것들을 쓸어버려라!!
			break;
		case 2:// 티칼
			L1SpawnUtil.spawn2(32753, 32870, (short) 784, 4036016, 0, 1920*1000, 0);
			L1SpawnUtil.spawn2(32751, 32859, (short) 784, 4036017, 0, 1920*1000, 0);
			L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1485)); // 쿠쿨칸 : 감히 이곳에 들어오다니!! 제브 레퀴!! 깨어나거라!!
			break;
		default: break;
		}
	}

	private void clear() {
		// 모든 상태를 초기화 한다 그리고 다음 오픈을 준비한다
		CrockMSG msg = new CrockMSG(0);// 텔
		GeneralThreadPool.getInstance().execute(msg);
		dieCount = 0;
		sList.clear();
		OpenTime.add(Calendar.MINUTE, period);
		CloseTime.add(Calendar.MINUTE, period);
		BossTime.add(Calendar.MINUTE, period);
		//ContinuationTime.add(Calendar.MINUTE, period);
		eva.setEvaTime(OpenTime);
		eva.setOpenLocation(0);
		eva.setMoveLocation(0);
		eva.setOpenContinuation(0);
		EvaSystemTable.getInstance().updateSystem(eva);
		msg = null;
	}

	/**
	 * 보스가 둘다 잡혀서 선물 주고 연장까지 설정한다
	 */
	public void CrockContinuation() {
		setBossTime(false);
		CrockMSG msg = new CrockMSG(1);// 선물
		GeneralThreadPool.getInstance().execute(msg);
		if(eva.getMoveLocation() == 2)
		//BossDieBuff();// 버프를 주고
		ContinuationTime.add(Calendar.MINUTE, extendperiod);// 닫히는 시간에서 60분을 더한다.
		eva.setOpenContinuation(1);// 연장 상태를 변경
		EvaSystemTable.getInstance().updateSystem(eva);
		msg = null;
	}

	/**
	 * 시간의 균열 보스공략 확인
	 * @return	(boolean)	2보스다 죽었다면 ture 1보스 이하 죽였다면 false
	 */
	private boolean isBossDie() {
		boolean sTemp = false;
		switch(dieCount()){
		case 2: sTemp = true; break;
		default: sTemp = false; break;
		}
		return sTemp;
	}

	/**
	 * 시간의 균열 테베 보스 다이 반납
	 * @return	(int)	dieCount	보스 다이 횟수
	 */
	public int dieCount(){	return dieCount;	}
	public void dieCount(int dieCount){	CrockSystem.dieCount = dieCount;	}

	/**
	 * 시간의 균열 이동 상태
	 * @return	(boolean)	move	이동 여부
	 */
	public boolean isOpen(){	return isOpen;	}
	private void setOpen(boolean isOpen){	this.isOpen = isOpen;	}

	/**
	 * 테베나 티칼이 보스 타임인지 여부
	 * @return
	 */
	public boolean isBossTime(){ return isBossTime; }
	private void setBossTime(boolean isBossTime) { this.isBossTime = isBossTime; }

	public boolean isContinuationTime() {
		if (eva.getOpenContinuation() == 0) return false;
		else return true;
	}

	/**
	 * 지정된 npcId 에 대한 loc 을 반납
	 * @return	(int[])	loc		좌표 배열
	 */
	public int[] loc(){
		return loc[eva.getOpenLocation()];
	}

	/**
	 * 선착순 20명 등록
	 */
	public synchronized void add(L1PcInstance c){
	/** 등록되어 있지 않고 */
		if(!sList.contains(c)){
	/** 선착순 20명 이하라면 */
		if(sList.size() < 50) sList.add(c);
		}
	}

	/**
	 * 선착순 리스트 사이즈 반납
	 * @return	(int)	sList 의 사이즈
	 */
	public int size(){
		return sList.size();
	}

	/**
	 * 오픈 시각을 가져온다
	 * @return	(String)	오픈 시각(MM-dd HH:mm)
	 */
	public String OpenTime(){
		return ss.format(OpenTime.getTime());
	}

	/**
	 * 티칼 보스가 잡혔으니 월드 피씨 전원에게 버프를 준다.
	 */
	public void BossDieBuff() {
		for(L1PcInstance pc : sList){
			if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_TIKAL_BOSSJOIN))
				pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_TIKAL_BOSSJOIN);
		}

		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if(pc.isPrivateShop() 
					|| pc.noPlayerCK
					|| pc.isDead() 
					|| pc.isGm()){
				continue;
			}
			new L1SkillUse().handleCommands(pc, L1SkillId.STATUS_TIKAL_BOSSDIE, pc.getId(),	pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_SPELLSC);
		}
	}

	/**
	 * 보스가 잡혀서 연장 상태인지 돌려준다
	 * @return true : 연장
	 */
	public boolean isCrockIng(){
		if(eva.getOpenContinuation() == 1) return true;
		else return false;
	}

	static class CrockMSG implements Runnable {
		private int _status;

		// 시간의 균열 - 테베 선물 아이템 번호
		private int[][] ItemId = {
				{ 410010, 1}, { 410011, 1}, { 410012, 1}, { 410013, 1}, 
				{ 420007, 1}, { 40074, 1}, { 40087, 1}, { 40076, 1}, 
				{ 140074, 1}, { 140087, 1}, { 240074, 1}, { 240087, 1}, 
				{ 40024, 10}, { 40023, 50}, { 40022, 100}, { 40015, 10}, 
				{ 40524, 10 }, { 430001, 1 }, { 40052, 2 }, { 40053, 2 }, 
				{ 40054, 2 }, { 40055, 2 }
		};

		public CrockMSG(int status) {
			_status = status;
		}

		@Override
		public void run() {
			try{
				switch(_status) {
				case 0:// 텔 시킨다
					L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1467));// 시간의 균열이 곧 닫힙니다.
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1476));//시스템 메시지 : 30초 후에 텔레포트 합니다.
						}
					}
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1477));//시스템 메시지 : 20초 후에 텔레포트 합니다.
						}
					}
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1478));//시스템 메시지 : 10초 후에 텔레포트 합니다.
						}
					}
					Thread.sleep(5000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1480));
						}
					}
					Thread.sleep(1000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1481));
						}
					}
					Thread.sleep(1000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1482));
						}
					}
					Thread.sleep(1000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1483));
						}
					}
					Thread.sleep(1000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1484));
						}
					}
					Thread.sleep(1000L);
					/*
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getInventory().checkItem(L1ItemId.TEBEOSIRIS_KEY, 1)){
							pc.getInventory().consumeItem(L1ItemId.TEBEOSIRIS_KEY, 1);
						}
					}
					Thread.sleep(1000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getInventory().checkItem(L1ItemId.TIKAL_KEY, 1)){
							pc.getInventory().consumeItem(L1ItemId.TIKAL_KEY, 1);
						}
					}
					Thread.sleep(1000L);
					*/
					crockDelete();
					Thread.sleep(1000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							L1Teleport.teleport(pc, 33970, 33246, (short) 4, 4, true);
						}
					}
					Thread.sleep(1000L);
					L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1468));// 시간의 균열이 사라집니다
					break;
				case 1:
					//for(L1PcInstance pc : sList){
					//	int[] Item = Item();
					//	pc.getInventory().storeItem(Item[0], Item[1]);
					//}
					//for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
					switch(eva.getMoveLocation()){
					case 1:
						//pc.sendPackets(new S_ServerMessage(1474)); // 테베 오시리스 : 이럴수가..!!! 우리가 졌다.
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1474)); // 테베 오시리스 : 이럴수가..!!! 우리가 졌다.
						break;
					case 2:
						//pc.sendPackets(new S_ServerMessage(1488));	// 쿠쿨칸 : 이럴수가..!!! 우리가 졌다.
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1488)); // 쿠쿨칸 : 이럴수가..!!! 우리가 졌다.
						break;
					}						
					//}
					
					/*
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1476));//시스템 메시지 : 30초 후에 텔레포트 합니다.
						}
					}
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1477));//시스템 메시지 : 20초 후에 텔레포트 합니다.
						}
					}
					 */
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						switch(pc.getMapId()){
						case 782:
							L1Teleport.teleport(pc, 32628, 32906, (short) 780, 5, true); break; // 테베 사막으로 텔레포트
						case 784:
							L1Teleport.teleport(pc, 32793, 32754, (short) 783, 2, false); break; // 폐허 마을로 텔레포트
						default: break;
						}
					}
					sList.clear();
					break;
				}
			} catch (Exception exception){ }	
		}

		/**
		 * 아이템 지급 아이디 랜덤 반납 - 테베
		 * @return	(int[]) Itemid	지급받을 아이템아이디, 갯수
		 */
		@SuppressWarnings("unused")
		private int[] Item(){
			return ItemId[(int)(Math.random() * ItemId.length)];
		}

		/**
		 * 균열을 삭제한다.
		 */
		private void crockDelete() {
			L1FieldObjectInstance f = null;
			for (L1Object l1object : L1World.getInstance().getObject()) {
				if(l1object instanceof L1FieldObjectInstance){
					f = (L1FieldObjectInstance)l1object;
					if(f.getNpcTemplate().get_npcId() == 4500100 && l1object !=null) {
						f.deleteMe();
					}	
				}

			}
		}
	}

	@Override
	public void onDayChanged(BaseTime time) {}

	@Override
	public void onMonthChanged(BaseTime time) {}

	@Override
	public void onHourChanged(BaseTime time) {}

	@Override
	public void onMinuteChanged(BaseTime time) {
		int realDay = time.get(Calendar.DAY_OF_MONTH);
		int openDay = OpenTime.get(Calendar.DAY_OF_MONTH);
		if (realDay != openDay && !isContinuationTime()) return;
		checkCrock(time);
	}
}
