package l1j.server.server.TimeController;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_HPMeter;
import l1j.server.server.serverpackets.S_SystemMessage;
import java.util.ArrayList;
import java.util.List;
public class BattleZoneController{
	private static BattleZoneController _instance;
	public static BattleZoneController getInstance(){
		if(_instance == null){
			_instance = new BattleZoneController();
		}
		return _instance;
	}
	public void start(){
		GeneralThreadPool.getInstance().execute(new TimeUpdater());
	}
	//배틀존 리스트
	private final List<L1PcInstance> _BattleList = new ArrayList<L1PcInstance>();
	///배틀존 리스트에 인원을 추가한다.
	public void addBattleList(L1PcInstance pc){
		if (pc == null || _BattleList.contains(pc)){
			return;
		}
		_BattleList.add(pc);
	}

	//리스트에서 인원수를 삭제한다
	public void removeBattleList(L1PcInstance pc){
		if (pc == null || !_BattleList.contains(pc)){
			return;
		}
		_BattleList.remove(pc);
	}
	//배틀존 인원수 체크
	public int getBattleCount(){
		return _BattleList.size();
	}
	public L1PcInstance[] getBattleMembers() {
		return _BattleList.toArray(new L1PcInstance[_BattleList.size()]);
	}
	//리스트 안의 모든 객체를 지운다.
	public void BattleListClear(){
		_BattleList.clear();
	}
	//시작 여부를 판단하기 위해.
	private boolean _BattleStart;
	public boolean getBattleStart() {
		return _BattleStart;
	}
	public void setBattleStart(boolean Battle) {
		_BattleStart = Battle;
	}
	//최초 시작시간설정.
	private boolean _BattleOne;
	public boolean getBattleOne(){
		return _BattleOne;
	}
	public void setBattleOne(boolean BattleOne){
		_BattleOne = BattleOne;
	}
	//배틀중인지를 판단
	private boolean _Battling;
	public boolean getBattling(){
		return _Battling;
	}
	public void setBattling(boolean Battling){
		_Battling = Battling;
	}
	//배틀존의 오픈 여부
	private boolean _BattleOpen;
	public boolean getBattleOpen() {
		return _BattleOpen;
	}
	public void setBattleOpen(boolean Battle) {
		_BattleOpen = Battle;
	}
	//
	public int BattleCount;
	private boolean Close;
	//
	//시간 설정 부분.
	private long _endTime;
	public long getendTime(){
		return _endTime;
	}
	// 기본적으로 1시간을 설정합니다.
	public void setendTime(long endTime){
		_endTime = endTime;
	}
	//오픈시각설정
	private long _openTime;
	public long getopenTime(){
		return _openTime;
	}
	public void setopenTime(long openTime){
		_openTime = openTime;
	}
	//입장시각
	private long _InBattleTime;
	public long getInBattleTime(){
		return _InBattleTime;
	}
	public void setInBattleTime(long InBattleTime){
		_InBattleTime = InBattleTime;
	}
	private int ADeadCount;
	private int BDeadCount;
	//배틀존 통합시간 설정
	public void BattleTime(){
		if(getBattleOne()){//최초 시작이라면
			setendTime(System.currentTimeMillis() + 10000);//최초 시작시 딜레이 10초.
		}else{
			setendTime(System.currentTimeMillis() + 14400000);//후 배틀존 쿨타임 기본 1시간 3600000
		}
		setopenTime(getendTime() + 10000); //딜레이 10초
		setInBattleTime(getopenTime() + 120000);//입장 시간 2분
	}
	private class TimeUpdater implements Runnable {
		public void run() {
			try {
				while (true) {
					if(getBattleOpen()){//입장여부를 판단한다.
						if(getopenTime() < System.currentTimeMillis()){//오픈 시각이라면
							L1World.getInstance().broadcastServerMessage("\\fW배틀존이 열렸습니다. 2분 동안 입장 가능합니다.");
							setBattleOpen(false);
							setBattleStart(true);
						}
					}else if(getBattleStart()){
						if(getInBattleTime() < System.currentTimeMillis()){//입장시간이 다되었다면
							L1World.getInstance().broadcastServerMessage("\\fW배틀존 입장시간이 종료되었습니다.");
							setBattleStart(false);
							setBattling(true);
							InBattle();
						}

					}else if(getBattling()){//배틀존이 시작되었다면
						BattleStart();
					}
					Thread.sleep(1000);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	//배틀존으로 이동.
	private void InBattle(){
		//리스트에 담겨있는 사람의 수만큼 입장시킨다.
		for (int i = 0; i < _BattleList.size(); i++) {
			L1PcInstance pc = _BattleList.get(i);//리스트에서 사람을 가져온다.
			if(pc != null){
				if (pc.getMapId() == 5083){//대기장에 사람이 있다면.
					if(pc.getBattleOk()){//입장이 허용된 사람이라면.
						if(i % 2 == 0){//짝수일 경우
							pc.set_BattleLine(2);
							L1Teleport.teleport(pc, 32709 ,32829, (short) 208, 0, true);
							pc.sendPackets(new S_SystemMessage("배틀존으로 입장하셨습니다. B라인"));
							Poly(pc, 2);
							createHp(pc);
						}else{//홀수일경우
							pc.set_BattleLine(1);
							L1Teleport.teleport(pc, 32709 ,32829, (short) 208, 0, true);
							pc.sendPackets(new S_SystemMessage("배틀존으로 입장하셨습니다. A라인"));
							Poly(pc, 1);
							createHp(pc);
						}
					}else{
						pc.sendPackets(new S_SystemMessage("비정상적인 입장으로 이동되지 않았습니다."));
						L1Teleport.teleport(pc, 33442, 32799, (short) 4, 0, true);
					}
				}
			}
		}
	}
	private void BattleStart(){
		//초기화
		ADeadCount = 0;
		BDeadCount = 0;

		//실시간으로 죽은 유저수를 체크한다.
		for (L1PcInstance pc : getBattleMembers()) {
			if(pc != null){
				if(!pc.isDead()){
					if(pc.get_BattleLine() == 1){
						ADeadCount += 1;
					}else{
						BDeadCount += 1;
					}
				}

			}
		}

		//끝날 시간이 아니라면
		if(getendTime() < System.currentTimeMillis()){
			if(ADeadCount >= 1 && BDeadCount ==0){//살아있는 A사람이 1보다 크고, B사람이 0명이라면 종료
				BattleEnd(ADeadCount, BDeadCount);
			}else if(BDeadCount >= 1 && ADeadCount == 0){//살아있는 B사람이 1보다크고, A사람이 0명이라면 종료
				BattleEnd(ADeadCount, BDeadCount);
			}
		}else{//끝날 시간이 지났다면 바로 강제종료
			BattleEnd(ADeadCount, BDeadCount);
		}
		if(getBattleCount() == 0){ //참가자가 없을 경우
			BattleEnd(0, 0);
		}
	}
	private void BattleEnd(int countA, int countB){
		int winLine = 0;

		switch(getBattleCount()){
		case 0 ://참가자가 0명
			winLine = 3;
			L1World.getInstance().broadcastServerMessage("\\fW참가자가 없어 배틀존이 종료되었습니다.");
			break;
		case 1 ://1명 일때
			winLine = 3;
			L1World.getInstance().broadcastServerMessage("\\fW혼자서는 배틀존을 시작할 수 없습니다.");
			L1World.getInstance().broadcastServerMessage("\\fW유저분들의 많은 참여 부탁드립니다.");
			break;
		default : //해당사항이 없으니 1명 이상이다
			if(countA > countB){
				winLine = 1;
				L1World.getInstance().broadcastServerMessage("\\fW배틀존 A라인의 승리입니다.");
			}else if(countA < countB){
				winLine = 2;
				L1World.getInstance().broadcastServerMessage("\\fW배틀존 B라인의 승리입니다.");
			}else if (countA == countB){
				winLine = 3;
				L1World.getInstance().broadcastServerMessage("\\fW배틀존 무승부입니다.");
			}
			break;
		}

		//아이템 지급과 유저의 배틀 상태를 초기화한다.
		for (L1PcInstance pc : getBattleMembers()) {
			if(pc != null){
				if(pc.get_BattleLine() == winLine){
					//아이템 추가
					pc.getInventory().storeItem(41159, 500);
					pc.sendPackets(new S_SystemMessage("\\fW신비한 날개 깃털 (500)개를 얻었습니다."));
					pc.set_BattleLine(0);
					pc.setBattleOk(false);
					deleteMiniHp(pc);
					removeBattleList(pc);//전체 리스트 삭제가 재대로 작동안될때를 대비해서 다시 넣어둠
					if(!pc.isDead()){//죽지 않은 사람은 마을로
						L1Teleport.teleport(pc, 33442, 32799, (short) 4, 0, true);
					}else{ // 죽은 사람에게는 메세지
						pc.sendPackets(new S_SystemMessage("배틀존이 끝났습니다. 리스타트를 해주시기 바랍니다."));

					}
				}else{
					pc.set_BattleLine(0);
					pc.setBattleOk(false);
					deleteMiniHp(pc);
					removeBattleList(pc);//전체 리스트 삭제가 재대로 작동안될때를 대비해서 다시 넣어둠
					if(!pc.isDead()){//죽지 않은 사람은 마을로
						L1Teleport.teleport(pc, 33442, 32799, (short) 4, 0, true);
					}else{ // 죽은 사람에게는 메세지
						pc.sendPackets(new S_SystemMessage("배틀존이 끝났습니다. 리스타트를 해주시기 바랍니다."));

					}
				}
			}
		}
		L1World.getInstance().broadcastServerMessage("\\fW배틀존이 종료되었습니다. 다음 입장시간은 4시간 후 입니다.");
		setBattleOpen(true);
		setBattleStart(false);
		setBattleOne(false);
		setBattling(false);
		BattleTime();//시간초기화
		BattleListClear();//리스트 클리어
	}
	// 미니 HP바를 생성한다.
	private void createHp(L1PcInstance pc) {
		for (L1PcInstance member : getBattleMembers()) {
			if(member != null){
				if(pc != member){
					if(pc.get_BattleLine() == member.get_BattleLine()){//같은 라인의 멤버에게 HP바를 전송
						member.sendPackets(new S_HPMeter(pc.getId(), 100 * pc.getCurrentHp() / pc.getMaxHp()));
						pc.sendPackets(new S_HPMeter(member.getId(), 100 * member.getCurrentHp() / member.getMaxHp()));
					}
				}
			}
		}
	}
	//HP바를 삭제한다.
	private void deleteMiniHp(L1PcInstance pc) {
		for (L1PcInstance member : getBattleMembers()) {
			if(member != null){
				if(pc != member){
					if(pc.get_BattleLine() == member.get_BattleLine()){
						pc.sendPackets(new S_HPMeter(member.getId(), 0xff));
						member.sendPackets(new S_HPMeter(pc.getId(), 0xff));
					}
				}
			}
		}
	}
	/* <p>[weapon]<br>
	 * 1:sword, 2:dagger, 3:tohandsword, 4:bow, 5:spear, 6:blunt, 7:staff, 
	 * 8:throwingknife, 9:arrow, 10:gauntlet, 11:claw, 12:edoryu, 13:singlebow, 
	 * 14:singlespear, 15:tohandblunt, 16:tohandstaff</p>*/
	private void Poly(L1PcInstance pc, int BattleLine){
		int polyid = 0;
		int time = 1800;
		if (pc.getWeapon() != null){
			//타입별 분류
			switch(pc.getWeapon().getItem().getType()){
			//활
			case 4:
			case 13:
				if(BattleLine == 1){
					polyid = 6269;
				}else{
					polyid = 6272;
				}
				L1PolyMorph.doPoly(pc, polyid, time, 1);
				break;
				//크로우 이도류
			case 11:
			case 12:
				if(BattleLine == 1){
					polyid = 6279;
				}else{
					polyid = 6280;
				}
				L1PolyMorph.doPoly(pc, polyid, time, 1);
				break;
				//지팡이
			case 7:
			case 16:
				if(BattleLine == 1){
					polyid = 6268;
				}else{
					polyid = 6271;
				}
				L1PolyMorph.doPoly(pc, polyid, time, 1);
				break;
				//그외..
			default :
				if(BattleLine == 1){
					polyid = 6267;
				}else{
					polyid = 6270;
				}
				L1PolyMorph.doPoly(pc, polyid, time, 1);
				break;
			}
		}else{
			if(BattleLine == 1){
				polyid = 6267;
			}else{
				polyid = 6270;
			}
			L1PolyMorph.doPoly(pc, polyid, time, 1);
		}
	}
}
