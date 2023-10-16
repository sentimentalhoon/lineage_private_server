/** 
 * 
 * 시간의 균열 
 * 
 * */
package l1j.server.server;
import java.util.Calendar;
import java.util.ArrayList;

import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1EffectSpawn;
import l1j.server.server.model.Instance.L1FieldObjectInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.L1SpawnUtil;
public class TimeCrockController extends Thread{

	private static int dieCount = 0;
	private static int tikalboss1 = 0;
	private static int tikalboss2 = 0;
	private boolean boss = false;
	private boolean move = false;
	public boolean isGmOpen = false;
	private boolean killboss = false;
	private boolean istimecrock = false;
	private static int rnd = 0;
	private static int rnd2 = 0;
	private static TimeCrockController instance;
	
	private static final long period = 3 * 60 * 1000L; // 2시간 30분
	private static final long extendperiod = 1 * 60 * 1000L; // 연장 1시간
	private static final long bosstime = 1 * 60 * 1000L; // 보스 잡을 수 있는 시간 30분
	
	private static final int[] ID = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] ID2 = { 0, 1 };//0 == 테베, 1 == 티칼
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

	private static final ArrayList<L1PcInstance> sList = new ArrayList<L1PcInstance>();
	/**
	 * CrockController 객체 리턴
	 * @return (CrockController) 단일객체
	 */
	public static TimeCrockController getInstance(){
		if(instance == null) instance = new TimeCrockController();
		return instance;
	}
	/**
	 * 기본 생성자 - 싱글톤구현으로 private
	 */
	private TimeCrockController(){
		super("TimeCrockController");
	}
	/**
	 * Super class abstract method
	 */

	@Override
	public void run(){
		try{
			while(true){
				sleep(60000L);
				if(isOpen() || isGmOpen()){
					setGmOpen(false); //gm에 의한 시작이라면
					setTimeCrock(true); // 균열의 시작
					L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1469));// 시간의 균열이 열렸습니다. 이계의 침공이 시작 됩니다.
					rnd();    // 균열의 위치 선정
					setMove(true);  // 들어갈수있다
					int[] loc = loc();
					L1SpawnUtil.spawn2(loc[0], loc[1], (short) 4, 4500100, 0, 0, 0);// 위치에 스폰한다
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("" + loc[0] + " " + loc[1] + ""));
					sleep(period);      // 2시간 30분
					// 보스를 스폰하고 보스 타임을 잰다
					switch(crocktype()){
					case 1:// 테베
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1474));// 테베 오시리스 : 이럴수가...!! 우리가 졌다
						L1SpawnUtil.spawn2(32794, 32825, (short) 782, 400016, 0, 1920*1000, 0);
						L1SpawnUtil.spawn2(32794, 32836, (short) 782, 400017, 0, 1920*1000, 0);
						break;
					case 2:// 티칼
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1474));// 테베 오시리스 : 이럴수가...!! 우리가 졌다
						L1SpawnUtil.spawn2(32753, 32870, (short) 784, 4036016, 0, 1920*1000, 0);
						L1SpawnUtil.spawn2(32751, 32859, (short) 784, 4036017, 0, 1920*1000, 0);
						break;
						default: break;
					}
					setBoss(true);      // 보스 공략 시작
					sleep(bosstime);     // 30분
					if(getDieCount() != 2){
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1467));// 시간의 균열이 곧 닫힙니다.
					}
					sleep(30000L);
					setBoss(false);                    //보스공략시간은끝
					if(isTeleport()){
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1468));// 시간의 균열이 사라집니다.
						setKillBoss(false);
						TelePort();
					}else{
						setKillBoss(true);
						//L1EffectSpawn.getInstance().spawnEffect(204, 86400000, loc[0], loc[1], (short)4);
						L1SpawnUtil.spawn2(loc[0], loc[1], (short) 4, 4500100, 0, 0, 0);// 위치에 스폰한다
						if(crocktype() == 0){//테베
							L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1474));// 테베 오시리스 : 이럴수가...!! 우리가 졌다
						}else{//티칼
							L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1488));// 쿠쿨칸 : 이럴수가..!! 우리가 졌다.
						}
						sleep(extendperiod); //1시간
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1467));// 시간의 균열이 곧 닫힙니다.
						sleep(30000L);
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1468));// 시간의 균열이 사라집니다.
						TelePort();
					}
					crockDelete();
					sleep(1000L);
					clear();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 열쇠 소모 후 마을로 텔레포트
	 */
	private void TelePort(){
		for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
			if(crocktype() == 0){//테베
				if(pc.getInventory().checkItem(L1ItemId.TEBEOSIRIS_KEY, 1))
					pc.getInventory().consumeItem(L1ItemId.TEBEOSIRIS_KEY, 1);
				if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
					L1Teleport.teleport(pc, 33970, 33246, (short) 4, 4, true);
				}
			} else {//티칼 TIKAL_KEY
				if(pc.getInventory().checkItem(L1ItemId.TIKAL_KEY, 1))
					pc.getInventory().consumeItem(L1ItemId.TIKAL_KEY, 1);
				if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
					L1Teleport.teleport(pc, 33970, 33246, (short) 4, 4, true);
				}
			}
		}
	}

	/**
	 * 열리는 시간때인가를 체크한다 2일에 한번 7시 오픈한다
	 */
	private boolean isOpen(){
		Calendar calender = Calendar.getInstance();
		int hour, minute;
		hour = calender.get(Calendar.HOUR_OF_DAY);
		minute = calender.get(Calendar.MINUTE);

		if (hour == 3 && minute == 59){ 
			return true;
		} 
		if (hour == 9 && minute == 59){ 
			return true;
		} 
		if (hour == 15 && minute == 59){ 
			return true;
		} 
		if (hour == 21 && minute == 59){ 
			return true;
		}
		return false;
	}

	/**
	 * 테베가 오픈중인가는 확인한다
	 */
	public boolean isTimeCrock(){
		return istimecrock;
	}
	public void setTimeCrock(boolean istimecrock1){
		istimecrock = istimecrock1;
	}

	/**
	 * Gm에 의한 테베오픈
	 */
	public boolean isGmOpen(){
		return isGmOpen;
	}
	public void setGmOpen(boolean gmopen1){
		isGmOpen = gmopen1;
	}
	/**
	 * 들어갈 수 있는 때인가
	 */
	public boolean isMove(){
		return move;
	}
	public void setMove(boolean move1){
		move = move1;
	}
	/**
	 * 보스방에 입장 가능한 시간인가?
	 */
	public boolean isBoss(){
		return boss;
	}
	public void setBoss(boolean boss1){
		boss = boss1;
	}

	public boolean isKillBoss(){
		return killboss;
	}
	public void setKillBoss(boolean i){
		killboss = i;
	}
	/**
	 * 맴버 관리
	 */
	public synchronized void add(L1PcInstance c){
		/** 등록되어 있지 않고 */
		if(!sList.contains(c)){
			/** 선착순 20명 이하라면 */
			if(sList.size() < 20) sList.add(c);
		}
	}
	public int size(){
		return sList.size();
	}
	/**
	 * 시스템 종료 클리어
	 */
	private void clear(){
		sList.clear();
		dieCount = 0;
		tikalboss1 = 0;
		tikalboss2 = 0;
		setBoss(false);
		setMove(false);
		setTimeCrock(false); // 균열 종료
	}

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
	/**
	 * 선택지
	 */
	private void rnd(){
		rnd = (int)(Math.random() * ID.length);
		rnd2 = (int)(Math.random() * ID2.length);
	}
	public int[] loc(){
		return loc[rnd];
	}
	public int crocktype(){
		return rnd2;
	}

	/**
	 * 보스가 죽지않아 텔레포트 해야하는가?
	 */
	private boolean isTeleport(){
		boolean sTemp = true;
		switch(getDieCount()){
		case 2:
			sTemp = false;
			break;
		default:
			sTemp = true;
			break;
		}
		return sTemp;
	}
	/**
	 * 보스가 죽은 카운트
	 */
	public int getDieCount(){
		return dieCount;
	}
	public void setDieCount(int Count){
		dieCount = Count;
	}

	public int getTikalBoss1(){
		return tikalboss1;
	}
	public void setTikalBoss1(int Count){
		tikalboss1 = Count;
	}

	public int getTikalBoss2(){
		return tikalboss2;
	}
	public void setTikalBoss2(int Count){
		tikalboss2 = Count;
	}
}
