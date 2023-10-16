package server;


import java.util.Date;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.Config;
import l1j.server.GameSystem.GhostHouse;
import l1j.server.GameSystem.PetRacing;
import l1j.server.GameSystem.MiniGame.DeathMatch;
import l1j.server.server.Account;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.LoginAuth;
import l1j.server.server.Opcodes;
import l1j.server.server.OxTimeController;
import l1j.server.server.PacketHandler;
import l1j.server.server.datatables.CharBuffTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Trade;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1FollowerInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SummonPack;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.ServerBasePacket;
import l1j.server.Warehouse.WarehouseManager; 
import l1j.server.Warehouse.ClanWarehouse;

import org.apache.mina.core.session.IoSession;

import server.manager.eva;
import server.mina.coder.LineageEncryption;
import server.threads.manager.DecoderManager;

public class LineageClient {

	private static Logger _log = Logger.getLogger(LineageClient.class.getName());
	// 세션 키값
	public static final String CLIENT_KEY = "CLIENT";
	// 클라 관리용 세션
	private IoSession session;
	// 암호화용
	private LineageEncryption le;
	// 로그인한 계정 아이디
	private String ID;
	// 접속중인 케릭터
	private L1PcInstance activeCharInstance;
	// 디코더 할 패킷 리스트
	public byte[] PacketD;
	// 디코더할 패킷 리스트에 위치값
	public int PacketIdx;
	// 클라 닫혓는지 체크
	private boolean close;
	private int i=0;
	private int 저장시간 = -1;
	private int 현시간 = -1;
	private int 시간당프레임 = 0;

	private int packetCount;
	private int packetSaveTime = -1;
	private int packetCurrentTime = -1;
	private int packetInjusticeCount;
	private int packetJusticeCount;

	private PacketHandler packetHandler;
	private static final int M_CAPACITY = 3; // 이동 요구를 한 변에 받아들이는 최대 용량
	private static final int H_CAPACITY = 2;// 행동 요구를 한 변에 받아들이는 최대 용량

	private static Timer observerTimer = new Timer();

	private int loginStatus = 0;

	private boolean charRestart = true;
	private int _kick = 0;
	private int _loginfaieldcount = 0;
	private Account account;
	private String hostname;
	private int threadIndex = 0;
	HcPacket movePacket = new HcPacket(M_CAPACITY);
	HcPacket hcPacket = new HcPacket(H_CAPACITY);
	//ClinetPacket cp = new ClinetPacket();

	ClientThreadObserver observer = new ClientThreadObserver(Config.AUTOMATIC_KICK * 60 * 1000);

	/**
	 * LineageClient 생성자 
	 * @param session
	 * @param key
	 */	
	public LineageClient(IoSession session, long key){
		this.session = session;
		le =new server.mina.coder.LineageEncryption();;
		le.initKeys(key);
		PacketD = new byte[1024*4];
		PacketIdx = 0;

		if (Config.AUTOMATIC_KICK > 0) {
			observer.start();
		}
		packetHandler = new PacketHandler(this);

		GeneralThreadPool.getInstance().execute(movePacket);
		GeneralThreadPool.getInstance().execute(hcPacket);
		//GeneralThreadPool.getInstance().execute(cp);

	}
	public void setthreadIndex(int ix){
		this.threadIndex = ix;
	}
	public int getthreadIndex(){
		return this.threadIndex;
	}
	/** 현재 상태를 끊는다 */
	public void kick() {
		sendPacket(new S_Disconnect());
		_kick = 1;
		if(Config.AUTH_CONNECT) {
			LoginAuth authIP = new LoginAuth();
			authIP.ConnectDelete(getIp());
		}
		session.close();
	}
	/** 케릭터의 리스타트 여부 */
	public void CharReStart(boolean flag) { this.charRestart = flag; }

	/** 로그인 상태값을 변경한다 */
	public void setloginStatus(int i){ loginStatus = i; }

	/** 
	 * 해당 패킷을 전송 한다.
	 * @param bp
	 */
	public synchronized void  sendPacket(ServerBasePacket bp){
		if(bp instanceof S_Disconnect){
			close();
		}
		session.write(bp);
	}

	/**
	 * 종료시 호출
	 */
	public void close(){
		if(!close){
			close = true;

			try {
				if(activeCharInstance!=null) {
					quitGame(activeCharInstance);
					synchronized (activeCharInstance) {
						if (!activeCharInstance.isPrivateShop()) {
							if (!(activeCharInstance.getInventory().checkItem(400000, 1)
									|| activeCharInstance.getInventory().checkItem(400001, 1))) {  // 무인pc(쿠우)
								activeCharInstance.logout();
							}
							setActiveChar(null);
						}
					}
				}
			} catch (Exception e) {
			}
			try {
				LoginController.getInstance().logout(this);
				stopObsever();
				DecoderManager.getInstance().removeClient(this, threadIndex);

			} catch (Exception e) {
			}
			try {
				session.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 현재 클라이언트에 사용할 PC 객체를 설정한다.
	 * @param pc
	 */
	public void setActiveChar(L1PcInstance pc) {	activeCharInstance = pc;	}

	/**
	 * 현재 클라이언트 사용하고 있는 PC 객체를 반환한다.
	 * @return activeCharInstance;
	 */
	public L1PcInstance getActiveChar() {	return activeCharInstance;	}

	/**
	 * 현재 사용하는 계정을 설정한다.
	 * @param account
	 */
	public void setAccount(Account account) {	this.account = account;	}

	/**
	 * 현재 사용중인 계정은 반환한다.
	 * @return account
	 */
	public Account getAccount() {	return account;	}

	/**
	 * 현재 사용중인 계정명을 반환한다.
	 * @return account.getName();
	 */
	public String getAccountName() {
		if (account == null) {
			return null;
		}
		String name = account.getName();

		return name;
	}

	/**
	 * 해당 LineageClient가 종료할때 호출 
	 * @param pc
	 */
	public static void quitGame(L1PcInstance pc) {
		//_log.info("캐릭터 종료: char=" + pc.getName() + " account=" + pc.getAccountName()	+ " host=" + L.getHostname());
		eva.writeMessage(0, "Out" + pc.getName() + " account=" + pc.getAccountName() + " " + pc.getNetConnection().getIp());
		pc.setadFeature(1);
		pc.setDeathMatch(false);
		pc.setHaunted(false);
		pc.setPetRacing(false);

		// 사망하고 있으면(자) 거리에 되돌려, 공복 상태로 한다
		if (pc.isDead()) {
			int[] loc = Getback.GetBack_Location(pc, true);
			pc.setX(loc[0]);
			pc.setY(loc[1]);
			pc.setMap((short) loc[2]);
			pc.setCurrentHp(pc.getLevel());
			pc.set_food(39); // 10%
			loc = null;
		}
		// 혈맹창고 사용도중 팅기거나 종료할경우 혈맹창고 사용중 해제(쿠우)
		ClanWarehouse clanWarehouse = null;
		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
		if(clan != null) 
			clanWarehouse = WarehouseManager.getInstance().getClanWarehouse(clan.getClanName());
		if(clanWarehouse != null) 
			clanWarehouse.unlock(pc.getId());
		// 트레이드를 중지한다
		if (pc.getTradeID() != 0) { // 트레이드중
			L1Trade trade = new L1Trade();
			trade.TradeCancel(pc);
		}

		//결투중
		if (pc.getFightId() != 0) {
			pc.setFightId(0);
			L1PcInstance fightPc = (L1PcInstance) L1World.getInstance().findObject(pc.getFightId());
			if (fightPc != null) {
				fightPc.setFightId(0);
				fightPc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, 0, 0));
			}
		}

		//		 좀비 초기화(시즌) by-Season
		if (pc.getZombieMod() >= 1) {
			pc.setZombieMod(0);
		}
		//		 좀비 초기화(시즌) by-Season

		// 파티를 빠진다
		if (pc.isInParty()) { // 파티중
			pc.getParty().leaveMember(pc);
		}

		// 채팅파티를 빠진다
		if (pc.isInChatParty()) { // 채팅파티중
			pc.getChatParty().leaveMember(pc);
		}

		if (DeathMatch.getInstance().isEnterMember(pc)) {
			DeathMatch.getInstance().removeEnterMember(pc);
		}
		if (GhostHouse.getInstance().isEnterMember(pc)) {
			GhostHouse.getInstance().removeEnterMember(pc);
		}
		if (PetRacing.getInstance().isEnterMember(pc)){
			PetRacing.getInstance().removeEnterMember(pc);
		}
		// 애완동물을 월드 MAP상으로부터 지운다
		for (Object petObject : pc.getPetList().values().toArray()) {
			if (petObject instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) petObject;
				pet.dropItem();
				int time = pet.getSkillEffectTimerSet().getSkillEffectTimeSec(L1SkillId.STATUS_PET_FOOD);
				PetTable.getInstance().storePetFoodTime(pet.getId(),pet.getFood(),time);
				pet.getSkillEffectTimerSet().clearSkillEffectTimer();
				pc.getPetList().remove(pet.getId());
				pet.deleteMe();
			} else if (petObject instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) petObject;
				for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(summon)) {
					visiblePc.sendPackets(new S_SummonPack(summon, visiblePc, false));
				}
			}
		}

		// 마법 인형을 월드 맵상으로부터 지운다
		for (L1DollInstance doll : pc.getDollList().values()) {
			doll.deleteDoll();
		}

		Object[] followerList = pc.getFollowerList().values().toArray();
		L1FollowerInstance follower = null;
		for (Object followerObject : followerList) {
			follower = (L1FollowerInstance) followerObject;
			follower.setParalyzed(true);
			follower.spawn(follower.getNpcTemplate().get_npcId(),
					follower.getX(), follower.getY(), follower.getMoveState().getHeading(),
					follower.getMapId());
			follower.deleteMe();
		}

		// 엔챤트를 DB의 character_buff에 보존한다
		CharBuffTable.DeleteBuff(pc);
		CharBuffTable.SaveBuff(pc);
		pc.getSkillEffectTimerSet().clearSkillEffectTimer();		

		for (L1ItemInstance item : pc.getInventory().getItems()) {
			if (item.getCount() <= 0) {
				pc.getInventory().deleteItem(item);
			}
		}
		// 로그아웃 시간을 기록한
		pc.setLogOutTime();
		// pc의 모니터를 stop 한다.
		//pc.stopEtcMonitor();
		// 온라인 상태를 OFF로 해, DB에 캐릭터 정보를 기입한다
		pc.setOnlineStatus(0);

		try {
			pc.save();
			pc.saveInventory();
			pc = null;
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		//eva.userUpdate(false);
	}

	/**
	 * 현재 연결된 호스트명을 반환한다.
	 * @return 
	 */
	public String getHostname() {	
		String HostName = null;
		StringTokenizer st = new StringTokenizer(session.getRemoteAddress().toString().substring(1), ":");
		HostName = st.nextToken();
		st = null;
		return HostName;	
	}

	/**
	 * 현재 로그인 실패한 카운트 수를 반환한다.
	 * @return
	 */
	public int getLoginFailedCount() { return _loginfaieldcount; }

	/**
	 * 현재 로그인 실패한 카운트 수를 설정한다.
	 * @param i
	 */
	public void setLoginFailedCount(int i) { _loginfaieldcount = i; }

	/**
	 * 패킷을 복호화 하고 패킷핸들러에 패킷을 전달한다. 
	 * @param data
	 */
	public void encryptD(byte[] data){
		try {
			int length = PacketSize(data)-2;
			byte[] temp = new byte[length];
			char[] incoming = new char[length];
			System.arraycopy(data, 2, temp, 0, length);
			incoming = le.getUChar8().fromArray(temp, incoming, length);
			incoming = le.decrypt(incoming, length);
			data = le.getUByte8().fromArray(incoming, temp);

			PacketHandler(data);
		} catch (Exception e) {
			//Logger.getInstance().error(getClass().toString()+" encryptD(byte... data)\r\n"+e.toString(), Config.LOG.error);
		}
	}

	/**
	 * 패킷을 암호화한다.
	 * @param data
	 * @return
	 */
	public byte[] encryptE(byte[] data){
		try {
			char[] data1 = le.getUChar8().fromArray(data);
			data1 = le.encrypt(data1);
			return le.getUByte8().fromArray(data1);
		} catch (Exception e) {
			//Logger.getInstance().error(getClass().toString()+" encryptE(byte... data)\r\n"+e.toString(), Config.LOG.error);
		}
		return null;
	}

	/**
	 * 패킷 사이즈를 반환한다.
	 * @param data
	 * @return
	 */
	private int PacketSize(byte[] data){
		int length = data[0] &0xff;
		length |= data[1] << 8 &0xff00;
		return length;
	}

	/**
	 * ID를 반환한다.
	 * @return
	 */
	public String getID(){
		return ID;
	}

	/** 
	 * ID를 설정한다.
	 * @param id
	 */
	public void setID(String id){
		ID = id;
	}

	/**
	 * LineageClient의 접속 여부를 반환한다.
	 * @return
	 */
	public boolean isConnected(){
		return session.isConnected();
	}

	/**
	 * 현재 접속중인 LineageClient에 IP를 반환한다.
	 * @return
	 */
	public String getIp(){
		String _Ip = null;
		StringTokenizer st = new StringTokenizer(session.getRemoteAddress().toString().substring(1), ":");
		_Ip = st.nextToken();
		st = null;
		return _Ip;
	}

	/**
	 * 현재 실행중인 클라이언트 감시를 중단한다.
	 */
	public void stopObsever(){
		observer.cancel();
	}

	/**
	 * 현재 새션 종료상태를 반환한다.
	 * @return
	 */
	public boolean isClosed() {
		if(session.isClosing())
			return true;
		else{
			return false;	
		}

	}

	/**
	 * 패킷 구분하여 처리.
	 * @param data
	 * @throws Exception 
	 */
	public void PacketHandler(byte[] data) throws Exception{

		int opcode = data[0] & 0xFF;

		if (Config.STANDBY_SERVER){//스탠바이 서버[0062] T일때 패킷 걸러냄
			if(opcode == Opcodes.C_OPCODE_ATTACK  || opcode == Opcodes.C_OPCODE_TRADE || opcode == Opcodes.C_OPCODE_CREATECLAN
					|| opcode == Opcodes.C_OPCODE_DROPITEM || opcode == Opcodes.C_OPCODE_PICKUPITEM || opcode == Opcodes.C_OPCODE_ARROWATTACK
					|| opcode == Opcodes.C_OPCODE_GIVEITEM || opcode == Opcodes.C_OPCODE_USESKILL){
				return;
			}
		}

		Date now = new Date();

		if (CheckTime(now) == packetSaveTime) { packetCurrentTime = packetSaveTime; packetCount++; }
		if (CheckTime(now) != packetCurrentTime) { packetCount = 0; packetJusticeCount++; packetCurrentTime = CheckTime(now); }
		packetSaveTime = CheckTime(now);

		if (packetCount >= 30) {
			packetInjusticeCount++;
			packetCount = 0;
			packetJusticeCount = 0;
			//activeCharInstance.sendPackets(new S_SystemMessage("부정패킷 경고 "+ packetInjusticeCount +"회. (3회 발생시 강제종료)"));
		}

		switch (packetInjusticeCount) {
		case 0: case 1: case 2: break;
		default: kick(); System.out.println("패킷섭폭 강제 종료 캐릭터 : "+activeCharInstance.getName()+""); break;
		}
		switch (packetJusticeCount) {
		case 600: packetJusticeCount = 0; packetInjusticeCount = 0; break;
		default: break;
		}
		if (opcode == Opcodes.C_OPCODE_NOTICECLICK || opcode == Opcodes.C_OPCODE_RESTART) {
			loginStatus = 1;
		}

		if (opcode == Opcodes.C_OPCODE_LOGINTOSERVEROK || opcode == Opcodes.C_OPCODE_RETURNTOLOGIN) {
			loginStatus = 0;
		}
		/**
		 * A110 삭제
		 **/
		/*
		if (opcode == Opcodes.C_OPCODE_SELECT_CHARACTER) {
			if (loginStatus != 1) return;
			시간당프레임 = 3;
		}
		*/
		if (opcode != Opcodes.C_OPCODE_KEEPALIVE) {
			// C_OPCODE_KEEPALIVE 이외의 뭔가의 패킷을 받으면(자) Observer에 통지
			observer.packetReceived();
		}

		// null의 경우는 캐릭터 선택전이므로 Opcode의 취사 선택은 하지 않고 모두 실행
		if (activeCharInstance == null) {
			packetHandler.handlePacket(data, activeCharInstance);
			return;
		}

		// 이후, PacketHandler의 처리 상황이 ClientThread에 영향을 주지 않게 하기 때문에(위해)의 처리
		// 목적은 Opcode의 취사 선택과 ClientThread와 PacketHandler의 분리
		/*
		if (opcode == Opcodes.C_OPCODE_USESKILL){
			switch(activeCharInstance.getTempCharGfx())
			{
			case 2177: // 사일런스
			case 870: // 캔슬레이션
			case 227: // 리무브 커스
			case 2175: // 다크니스
			case 226: // 크리에이트 좀비
			case 751: // 인챈트 마이티
			case 755: // 헤이스트 
			case 2176: // 블레스 웨폰
			case 3936: // 홀리 워크
			case 3104: // 그레이터 헤이스트
			case 3943: // 버서커스
			case 2230: // 디지즈
			case 3944: // 리절렉션
			case 231: // 셰이프 체인지
			case 2236: // 매스 텔레포트
			case 3934: // 카운터 디텍션
			case 763: // 크리에이트 매지컬 웨폰
			case 3935: // 어드밴스 스피릿
			case 1819: // 파이어 스톰
			case 4155: // 린드비오르 바람 브레스
			case 1783: // 화염의 혼 켈베로스 불 뿜기

				시간당프레임 = 2;
				break;

			case 2244: // 블레싱
			case 744: // 힐
			case 2510: // 라이트
			case 221: // 실드
			case 167: // 에너지 볼트
			case 169: // 텔레포트
			case 1797: // 아이스 대거
			case 1799: // 윈드 커터
			case 236: // 뱀파이어릭 터치
			case 830: // 그레이터 힐
			case 1804: // 프로즌 클라우드
			case 1805: // 어스 재일
			case 1809: // 콘 오브 콜드
			case 2171: // 마나 드레인
			case 129: // 이럽션
			case 749: // 디텍션
			case 746: // 커스: 블라인드
			case 1811: // 선 버스트
			case 2228: // 위크니스
			case 759: // 힐 올
			case 832: // 풀 힐
			case 1812: // 어스 퀘이크
			case 3924: // 라이트닝 스톰
			case 757: // 블리자드
			case 2232: // 디케이 포션

				시간당프레임 = 5;
				break; 

			case 760: // 포그 오브 슬리핑
			case 228: // 이뮨 투 함
			case 762: // 미티어 스트라이크
			case 4434: // 쇼크 스턴
				시간당프레임 = 16;
				break;
			case 1815: // 디스인티그레이트
			case 4648: // 바운스
			case 5831: // 솔리드 캐리지
				시간당프레임 = 21;
				break;
			}
			if(now.getSeconds() == 저장시간)
			{
				현시간 = 저장시간;
				i++;
			}
			if(now.getSeconds() != 현시간)
			{
				i = 0;
				현시간 = now.getSeconds();
			}
			if(i >= 시간당프레임) //변신종류 별로 최대 프레임 조건 판단
			{
				i = 0;
				activeCharInstance.sendPackets(new S_Disconnect()); // 캐릭터를 월드에서 추방
			}
			저장시간 = now.getSeconds();
		}
		*/
		// 파기해선 안 되는 Opecode군 restart, 아이템 드롭, 아이템 삭제
		if (opcode == Opcodes.C_OPCODE_RESTART || opcode == Opcodes.C_OPCODE_DROPITEM
				|| opcode == Opcodes.C_OPCODE_DELETEINVENTORYITEM) {
			packetHandler.handlePacket(data, activeCharInstance);
		} else if (opcode == Opcodes.C_OPCODE_MOVECHAR) {
			// 이동은 가능한 한 확실히 실시하기 때문에(위해), 이동 전용 thread에 주고 받아	
			movePacket.requestWork(data);					
		} else {
			// 패킷 처리 thread에 주고 받아
			hcPacket.requestWork(data);
		}
		//		_log.warning((new StringBuilder()).append("작동코드").append(i).toString());
		//		 System.out.println(DataToPacket(data, data.length)); // 사용 처리
	}

	private int CheckTime(Date now) {
		return RealTimeClock.getInstance().getRealTime().getSeconds();
	}

	public String printData(byte[] data, int len){ 
		StringBuffer result = new StringBuffer();
		int counter = 0;
		for (int i=0;i< len;i++){
			if (counter % 16 == 0){
				result.append(fillHex(i,4)+": ");
			}
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16){
				result.append("   ");
				int charpoint = i-15;
				for (int a=0; a<16;a++){
					int t1 = data[charpoint++];
					if (t1 > 0x1f && t1 < 0x80){
						result.append((char)t1);
					}else{
						result.append('.');
					}
				}
				result.append("\n");
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if (rest > 0 ){
			for (int i=0; i<17-rest;i++ ){
				result.append("   ");
			}

			int charpoint = data.length-rest;
			for (int a=0; a<rest;a++){
				int t1 = data[charpoint++];
				if (t1 > 0x1f && t1 < 0x80){
					result.append((char)t1);
				}else{
					result.append('.');
				}
			}

			result.append("\n");
		}
		return result.toString();
	}

	private String fillHex(int data, int digits){
		String number = Integer.toHexString(data);

		for (int i=number.length(); i< digits; i++){
			number = "0" + number;
		}
		return number;
	}

	/**
	 * 
	 * @author Developer
	 *
	 */
	class ClientThreadObserver extends TimerTask {
		private int _checkct = 1;

		private final int _disconnectTimeMillis;

		public ClientThreadObserver(int disconnectTimeMillis) {
			_disconnectTimeMillis = disconnectTimeMillis;
		}

		public void start() {
			observerTimer.scheduleAtFixedRate(ClientThreadObserver.this, 0, _disconnectTimeMillis);
		}

		@Override
		public void run() {
			try {
				if (session.isClosing()) {
					cancel();
					return;
				}

				if (_checkct > 0) {
					_checkct = 0;
					return;
				}

				if (activeCharInstance == null // 캐릭터 선택전
						|| activeCharInstance != null && !activeCharInstance.isPrivateShop()) { // 개인 상점중
					kick();
					_log.warning("일정시간 응답을 얻을 수 없었기 때문에(" + hostname + ")과(와)의 접속을 강제 절단 했습니다.");
					cancel();
					return;
				}
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				cancel();
			}
		}

		public void packetReceived() {
			_checkct++;
		}
	}
	/*
	// 케릭터의 패킷 처리 thread
	class ClinetPacket implements Runnable{
		public ClinetPacket(){

		}

		@Override
		public void run() {
			while(!session.isClosing()){
				try {
					// 디코더
					synchronized(PacketD){
						int length = PacketSize(PacketD);
						if(length!=0 && length<=PacketIdx){
							byte[] temp = new byte[length];
							System.arraycopy(PacketD, 0, temp, 0, length);
							System.arraycopy(PacketD, length, PacketD, 0, PacketIdx-length);
							PacketIdx -= length;
							encryptD(temp);
						}
					}
					Thread.sleep(10);
				} catch (Exception e) {
					//Logger.getInstance().error(getClass().toString()+" run()\r\n"+e.toString(), Config.LOG.error);
				}
			}
		}
	}
	 */
	// 캐릭터의 행동 처리 thread
	class HcPacket implements Runnable {
		private final Queue<byte[]> _queue;

		private PacketHandler _handler;

		public HcPacket() {
			_queue = new ConcurrentLinkedQueue<byte[]>();
			_handler = new PacketHandler(LineageClient.this);
		}

		public HcPacket(int capacity) {
			_queue = new LinkedBlockingQueue<byte[]>(capacity);
			_handler = new PacketHandler(LineageClient.this);
		}

		public void requestWork(byte data[]) {
			_queue.offer(data);
		}

		@Override
		public void run() {
			byte[] data;
			while (!session.isClosing()) {
				data = _queue.poll();
				if (data != null) {
					try {
						_handler.handlePacket(data, activeCharInstance);
					} catch (Exception e) {}
				} else {
					try {
						Thread.sleep(10);
					} catch (Exception e) {}
				}
			}
			Thread.currentThread().interrupt();
			//System.out.println("세션이 종료됨");
			return;
		}
	}
}
