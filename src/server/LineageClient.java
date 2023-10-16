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
	// ���� Ű��
	public static final String CLIENT_KEY = "CLIENT";
	// Ŭ�� ������ ����
	private IoSession session;
	// ��ȣȭ��
	private LineageEncryption le;
	// �α����� ���� ���̵�
	private String ID;
	// �������� �ɸ���
	private L1PcInstance activeCharInstance;
	// ���ڴ� �� ��Ŷ ����Ʈ
	public byte[] PacketD;
	// ���ڴ��� ��Ŷ ����Ʈ�� ��ġ��
	public int PacketIdx;
	// Ŭ�� �������� üũ
	private boolean close;
	private int i=0;
	private int ����ð� = -1;
	private int ���ð� = -1;
	private int �ð��������� = 0;

	private int packetCount;
	private int packetSaveTime = -1;
	private int packetCurrentTime = -1;
	private int packetInjusticeCount;
	private int packetJusticeCount;

	private PacketHandler packetHandler;
	private static final int M_CAPACITY = 3; // �̵� �䱸�� �� ���� �޾Ƶ��̴� �ִ� �뷮
	private static final int H_CAPACITY = 2;// �ൿ �䱸�� �� ���� �޾Ƶ��̴� �ִ� �뷮

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
	 * LineageClient ������ 
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
	/** ���� ���¸� ���´� */
	public void kick() {
		sendPacket(new S_Disconnect());
		_kick = 1;
		if(Config.AUTH_CONNECT) {
			LoginAuth authIP = new LoginAuth();
			authIP.ConnectDelete(getIp());
		}
		session.close();
	}
	/** �ɸ����� ����ŸƮ ���� */
	public void CharReStart(boolean flag) { this.charRestart = flag; }

	/** �α��� ���°��� �����Ѵ� */
	public void setloginStatus(int i){ loginStatus = i; }

	/** 
	 * �ش� ��Ŷ�� ���� �Ѵ�.
	 * @param bp
	 */
	public synchronized void  sendPacket(ServerBasePacket bp){
		if(bp instanceof S_Disconnect){
			close();
		}
		session.write(bp);
	}

	/**
	 * ����� ȣ��
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
									|| activeCharInstance.getInventory().checkItem(400001, 1))) {  // ����pc(���)
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
	 * ���� Ŭ���̾�Ʈ�� ����� PC ��ü�� �����Ѵ�.
	 * @param pc
	 */
	public void setActiveChar(L1PcInstance pc) {	activeCharInstance = pc;	}

	/**
	 * ���� Ŭ���̾�Ʈ ����ϰ� �ִ� PC ��ü�� ��ȯ�Ѵ�.
	 * @return activeCharInstance;
	 */
	public L1PcInstance getActiveChar() {	return activeCharInstance;	}

	/**
	 * ���� ����ϴ� ������ �����Ѵ�.
	 * @param account
	 */
	public void setAccount(Account account) {	this.account = account;	}

	/**
	 * ���� ������� ������ ��ȯ�Ѵ�.
	 * @return account
	 */
	public Account getAccount() {	return account;	}

	/**
	 * ���� ������� �������� ��ȯ�Ѵ�.
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
	 * �ش� LineageClient�� �����Ҷ� ȣ�� 
	 * @param pc
	 */
	public static void quitGame(L1PcInstance pc) {
		//_log.info("ĳ���� ����: char=" + pc.getName() + " account=" + pc.getAccountName()	+ " host=" + L.getHostname());
		eva.writeMessage(0, "Out" + pc.getName() + " account=" + pc.getAccountName() + " " + pc.getNetConnection().getIp());
		pc.setadFeature(1);
		pc.setDeathMatch(false);
		pc.setHaunted(false);
		pc.setPetRacing(false);

		// ����ϰ� ������(��) �Ÿ��� �ǵ���, ���� ���·� �Ѵ�
		if (pc.isDead()) {
			int[] loc = Getback.GetBack_Location(pc, true);
			pc.setX(loc[0]);
			pc.setY(loc[1]);
			pc.setMap((short) loc[2]);
			pc.setCurrentHp(pc.getLevel());
			pc.set_food(39); // 10%
			loc = null;
		}
		// ����â�� ��뵵�� �ñ�ų� �����Ұ�� ����â�� ����� ����(���)
		ClanWarehouse clanWarehouse = null;
		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
		if(clan != null) 
			clanWarehouse = WarehouseManager.getInstance().getClanWarehouse(clan.getClanName());
		if(clanWarehouse != null) 
			clanWarehouse.unlock(pc.getId());
		// Ʈ���̵带 �����Ѵ�
		if (pc.getTradeID() != 0) { // Ʈ���̵���
			L1Trade trade = new L1Trade();
			trade.TradeCancel(pc);
		}

		//������
		if (pc.getFightId() != 0) {
			pc.setFightId(0);
			L1PcInstance fightPc = (L1PcInstance) L1World.getInstance().findObject(pc.getFightId());
			if (fightPc != null) {
				fightPc.setFightId(0);
				fightPc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, 0, 0));
			}
		}

		//		 ���� �ʱ�ȭ(����) by-Season
		if (pc.getZombieMod() >= 1) {
			pc.setZombieMod(0);
		}
		//		 ���� �ʱ�ȭ(����) by-Season

		// ��Ƽ�� ������
		if (pc.isInParty()) { // ��Ƽ��
			pc.getParty().leaveMember(pc);
		}

		// ä����Ƽ�� ������
		if (pc.isInChatParty()) { // ä����Ƽ��
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
		// �ֿϵ����� ���� MAP�����κ��� �����
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

		// ���� ������ ���� �ʻ����κ��� �����
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

		// ��îƮ�� DB�� character_buff�� �����Ѵ�
		CharBuffTable.DeleteBuff(pc);
		CharBuffTable.SaveBuff(pc);
		pc.getSkillEffectTimerSet().clearSkillEffectTimer();		

		for (L1ItemInstance item : pc.getInventory().getItems()) {
			if (item.getCount() <= 0) {
				pc.getInventory().deleteItem(item);
			}
		}
		// �α׾ƿ� �ð��� �����
		pc.setLogOutTime();
		// pc�� ����͸� stop �Ѵ�.
		//pc.stopEtcMonitor();
		// �¶��� ���¸� OFF�� ��, DB�� ĳ���� ������ �����Ѵ�
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
	 * ���� ����� ȣ��Ʈ���� ��ȯ�Ѵ�.
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
	 * ���� �α��� ������ ī��Ʈ ���� ��ȯ�Ѵ�.
	 * @return
	 */
	public int getLoginFailedCount() { return _loginfaieldcount; }

	/**
	 * ���� �α��� ������ ī��Ʈ ���� �����Ѵ�.
	 * @param i
	 */
	public void setLoginFailedCount(int i) { _loginfaieldcount = i; }

	/**
	 * ��Ŷ�� ��ȣȭ �ϰ� ��Ŷ�ڵ鷯�� ��Ŷ�� �����Ѵ�. 
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
	 * ��Ŷ�� ��ȣȭ�Ѵ�.
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
	 * ��Ŷ ����� ��ȯ�Ѵ�.
	 * @param data
	 * @return
	 */
	private int PacketSize(byte[] data){
		int length = data[0] &0xff;
		length |= data[1] << 8 &0xff00;
		return length;
	}

	/**
	 * ID�� ��ȯ�Ѵ�.
	 * @return
	 */
	public String getID(){
		return ID;
	}

	/** 
	 * ID�� �����Ѵ�.
	 * @param id
	 */
	public void setID(String id){
		ID = id;
	}

	/**
	 * LineageClient�� ���� ���θ� ��ȯ�Ѵ�.
	 * @return
	 */
	public boolean isConnected(){
		return session.isConnected();
	}

	/**
	 * ���� �������� LineageClient�� IP�� ��ȯ�Ѵ�.
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
	 * ���� �������� Ŭ���̾�Ʈ ���ø� �ߴ��Ѵ�.
	 */
	public void stopObsever(){
		observer.cancel();
	}

	/**
	 * ���� ���� ������¸� ��ȯ�Ѵ�.
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
	 * ��Ŷ �����Ͽ� ó��.
	 * @param data
	 * @throws Exception 
	 */
	public void PacketHandler(byte[] data) throws Exception{

		int opcode = data[0] & 0xFF;

		if (Config.STANDBY_SERVER){//���Ĺ��� ����[0062] T�϶� ��Ŷ �ɷ���
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
			//activeCharInstance.sendPackets(new S_SystemMessage("������Ŷ ��� "+ packetInjusticeCount +"ȸ. (3ȸ �߻��� ��������)"));
		}

		switch (packetInjusticeCount) {
		case 0: case 1: case 2: break;
		default: kick(); System.out.println("��Ŷ���� ���� ���� ĳ���� : "+activeCharInstance.getName()+""); break;
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
		 * A110 ����
		 **/
		/*
		if (opcode == Opcodes.C_OPCODE_SELECT_CHARACTER) {
			if (loginStatus != 1) return;
			�ð��������� = 3;
		}
		*/
		if (opcode != Opcodes.C_OPCODE_KEEPALIVE) {
			// C_OPCODE_KEEPALIVE �̿��� ������ ��Ŷ�� ������(��) Observer�� ����
			observer.packetReceived();
		}

		// null�� ���� ĳ���� �������̹Ƿ� Opcode�� ��� ������ ���� �ʰ� ��� ����
		if (activeCharInstance == null) {
			packetHandler.handlePacket(data, activeCharInstance);
			return;
		}

		// ����, PacketHandler�� ó�� ��Ȳ�� ClientThread�� ������ ���� �ʰ� �ϱ� ������(����)�� ó��
		// ������ Opcode�� ��� ���ð� ClientThread�� PacketHandler�� �и�
		/*
		if (opcode == Opcodes.C_OPCODE_USESKILL){
			switch(activeCharInstance.getTempCharGfx())
			{
			case 2177: // ���Ϸ���
			case 870: // ĵ�����̼�
			case 227: // ������ Ŀ��
			case 2175: // ��ũ�Ͻ�
			case 226: // ũ������Ʈ ����
			case 751: // ��æƮ ����Ƽ
			case 755: // ���̽�Ʈ 
			case 2176: // ���� ����
			case 3936: // Ȧ�� ��ũ
			case 3104: // �׷����� ���̽�Ʈ
			case 3943: // ����Ŀ��
			case 2230: // ������
			case 3944: // ��������
			case 231: // ������ ü����
			case 2236: // �Ž� �ڷ���Ʈ
			case 3934: // ī���� ���ؼ�
			case 763: // ũ������Ʈ ������ ����
			case 3935: // ���꽺 ���Ǹ�
			case 1819: // ���̾� ����
			case 4155: // �������� �ٶ� �극��
			case 1783: // ȭ���� ȥ �̺��ν� �� �ձ�

				�ð��������� = 2;
				break;

			case 2244: // ����
			case 744: // ��
			case 2510: // ����Ʈ
			case 221: // �ǵ�
			case 167: // ������ ��Ʈ
			case 169: // �ڷ���Ʈ
			case 1797: // ���̽� ���
			case 1799: // ���� Ŀ��
			case 236: // �����̾ ��ġ
			case 830: // �׷����� ��
			case 1804: // ������ Ŭ����
			case 1805: // � ����
			case 1809: // �� ���� �ݵ�
			case 2171: // ���� �巹��
			case 129: // �̷���
			case 749: // ���ؼ�
			case 746: // Ŀ��: ����ε�
			case 1811: // �� ����Ʈ
			case 2228: // ��ũ�Ͻ�
			case 759: // �� ��
			case 832: // Ǯ ��
			case 1812: // � ����ũ
			case 3924: // ����Ʈ�� ����
			case 757: // ���ڵ�
			case 2232: // ������ ����

				�ð��������� = 5;
				break; 

			case 760: // ���� ���� ������
			case 228: // �̹� �� ��
			case 762: // ��Ƽ�� ��Ʈ����ũ
			case 4434: // ��ũ ����
				�ð��������� = 16;
				break;
			case 1815: // ����Ƽ�׷���Ʈ
			case 4648: // �ٿ
			case 5831: // �ָ��� ĳ����
				�ð��������� = 21;
				break;
			}
			if(now.getSeconds() == ����ð�)
			{
				���ð� = ����ð�;
				i++;
			}
			if(now.getSeconds() != ���ð�)
			{
				i = 0;
				���ð� = now.getSeconds();
			}
			if(i >= �ð���������) //�������� ���� �ִ� ������ ���� �Ǵ�
			{
				i = 0;
				activeCharInstance.sendPackets(new S_Disconnect()); // ĳ���͸� ���忡�� �߹�
			}
			����ð� = now.getSeconds();
		}
		*/
		// �ı��ؼ� �� �Ǵ� Opecode�� restart, ������ ���, ������ ����
		if (opcode == Opcodes.C_OPCODE_RESTART || opcode == Opcodes.C_OPCODE_DROPITEM
				|| opcode == Opcodes.C_OPCODE_DELETEINVENTORYITEM) {
			packetHandler.handlePacket(data, activeCharInstance);
		} else if (opcode == Opcodes.C_OPCODE_MOVECHAR) {
			// �̵��� ������ �� Ȯ���� �ǽ��ϱ� ������(����), �̵� ���� thread�� �ְ� �޾�	
			movePacket.requestWork(data);					
		} else {
			// ��Ŷ ó�� thread�� �ְ� �޾�
			hcPacket.requestWork(data);
		}
		//		_log.warning((new StringBuilder()).append("�۵��ڵ�").append(i).toString());
		//		 System.out.println(DataToPacket(data, data.length)); // ��� ó��
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

				if (activeCharInstance == null // ĳ���� ������
						|| activeCharInstance != null && !activeCharInstance.isPrivateShop()) { // ���� ������
					kick();
					_log.warning("�����ð� ������ ���� �� ������ ������(" + hostname + ")��(��)�� ������ ���� ���� �߽��ϴ�.");
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
	// �ɸ����� ��Ŷ ó�� thread
	class ClinetPacket implements Runnable{
		public ClinetPacket(){

		}

		@Override
		public void run() {
			while(!session.isClosing()){
				try {
					// ���ڴ�
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
	// ĳ������ �ൿ ó�� thread
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
			//System.out.println("������ �����");
			return;
		}
	}
}
