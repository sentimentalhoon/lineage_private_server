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
	private Calendar OpenTime = eva.getEvaTime();// ���½ð�
	private Calendar CloseTime = (Calendar) eva.getEvaTime().clone();// �ݴ½ð�
	private Calendar BossTime = (Calendar) eva.getEvaTime().clone();// ���� ������ �ð�
	private Calendar ContinuationTime = (Calendar) eva.getEvaTime().clone();// ������ ����Ǿ�

	/**
	 * �տ� �ð� ����
	 */
	private static final int period = 240; // �� ���� default : 48�ð�
	private static final int extendperiod = 59; // �߰� 30��

	/** �տ��� ���ȴ��� �ƴ��� */
	private boolean isOpen = false;
	/** ���� �ð��� ���� �Ǿ����� */
	private boolean isBossTime = false;

	/** �ð��� �տ� �׺� ���� Ƚ�� */
	private static int dieCount = 0;

	/** �տ� ��ǥ */
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

	/** ������ ������ 20���� ��� ���� ����Ʈ */
	private static final ArrayList<L1PcInstance> sList = new ArrayList<L1PcInstance>();

	/** �ð� ������ ���� */
	private static final SimpleDateFormat ss = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

	private CrockSystem() {
		CloseTime.add(Calendar.MINUTE, 119);// 2�ð� 
		BossTime.add(Calendar.MINUTE, 99);//  1�ð� 40��
		if (eva.getOpenContinuation() == 1) {
			isOpen = true;
			ContinuationTime.add(Calendar.MINUTE, extendperiod);
			ready();
		}
	}

	/**
	 * �տ��� ���ȴ��� ����.
	 */
	private void checkCrock(BaseTime time) {
		if (eva.getOpenContinuation() == 1) {
			if (ContinuationTime.before(time.getCalendar())) {// ����ð��� �����ٸ�..
				clear();
			}
			return;
		}
		if (OpenTime.before(time.getCalendar()) && CloseTime.after(time.getCalendar())) {// ���½ð�
			if (!isOpen()) {
				setOpen(true);
				ready();
				L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1469));// ���ȴ�~
			} else {
				if (BossTime.before(time.getCalendar())) {// ����Ÿ�� ������ �����ٸ�
					if (!isBossTime()) {
						setBossTime(true);
						bossStart();
					}
				}
			}
		} else if (CloseTime.before(time.getCalendar())) {// ���� �ð� �Ķ��
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
		//System.out.println("������"+ eva.getOpenLocation());
		//System.out.println("�̵��ϴ°�" +eva.getMoveLocation());
		int OL = eva.getOpenLocation();
		L1SpawnUtil.spawn2(loc[OL][0], loc[OL][1], (short) loc[OL][2], 4500100, 0, 0, 0);// ��ġ�� �����Ѵ�
		EvaSystemTable.getInstance().updateSystem(eva);
	}

	private void bossStart() {
		// ������ �����ϰ� ���� Ÿ���� ���
		switch(eva.getMoveLocation()){
		case 1:// �׺�
			L1SpawnUtil.spawn2(32794, 32825, (short) 782, 400016, 0, 1920*1000, 0);
			L1SpawnUtil.spawn2(32794, 32836, (short) 782, 400017, 0, 1920*1000, 0);
			L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1471)); // ���ø��� : ����� �͵�..�̰��� �����!! �ƴ���! ȣ�罺! ���͵��� ���������!!
			break;
		case 2:// ƼĮ
			L1SpawnUtil.spawn2(32753, 32870, (short) 784, 4036016, 0, 1920*1000, 0);
			L1SpawnUtil.spawn2(32751, 32859, (short) 784, 4036017, 0, 1920*1000, 0);
			L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1485)); // ����ĭ : ���� �̰��� �����ٴ�!! ���� ����!! ����Ŷ�!!
			break;
		default: break;
		}
	}

	private void clear() {
		// ��� ���¸� �ʱ�ȭ �Ѵ� �׸��� ���� ������ �غ��Ѵ�
		CrockMSG msg = new CrockMSG(0);// ��
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
	 * ������ �Ѵ� ������ ���� �ְ� ������� �����Ѵ�
	 */
	public void CrockContinuation() {
		setBossTime(false);
		CrockMSG msg = new CrockMSG(1);// ����
		GeneralThreadPool.getInstance().execute(msg);
		if(eva.getMoveLocation() == 2)
		//BossDieBuff();// ������ �ְ�
		ContinuationTime.add(Calendar.MINUTE, extendperiod);// ������ �ð����� 60���� ���Ѵ�.
		eva.setOpenContinuation(1);// ���� ���¸� ����
		EvaSystemTable.getInstance().updateSystem(eva);
		msg = null;
	}

	/**
	 * �ð��� �տ� �������� Ȯ��
	 * @return	(boolean)	2������ �׾��ٸ� ture 1���� ���� �׿��ٸ� false
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
	 * �ð��� �տ� �׺� ���� ���� �ݳ�
	 * @return	(int)	dieCount	���� ���� Ƚ��
	 */
	public int dieCount(){	return dieCount;	}
	public void dieCount(int dieCount){	CrockSystem.dieCount = dieCount;	}

	/**
	 * �ð��� �տ� �̵� ����
	 * @return	(boolean)	move	�̵� ����
	 */
	public boolean isOpen(){	return isOpen;	}
	private void setOpen(boolean isOpen){	this.isOpen = isOpen;	}

	/**
	 * �׺��� ƼĮ�� ���� Ÿ������ ����
	 * @return
	 */
	public boolean isBossTime(){ return isBossTime; }
	private void setBossTime(boolean isBossTime) { this.isBossTime = isBossTime; }

	public boolean isContinuationTime() {
		if (eva.getOpenContinuation() == 0) return false;
		else return true;
	}

	/**
	 * ������ npcId �� ���� loc �� �ݳ�
	 * @return	(int[])	loc		��ǥ �迭
	 */
	public int[] loc(){
		return loc[eva.getOpenLocation()];
	}

	/**
	 * ������ 20�� ���
	 */
	public synchronized void add(L1PcInstance c){
	/** ��ϵǾ� ���� �ʰ� */
		if(!sList.contains(c)){
	/** ������ 20�� ���϶�� */
		if(sList.size() < 50) sList.add(c);
		}
	}

	/**
	 * ������ ����Ʈ ������ �ݳ�
	 * @return	(int)	sList �� ������
	 */
	public int size(){
		return sList.size();
	}

	/**
	 * ���� �ð��� �����´�
	 * @return	(String)	���� �ð�(MM-dd HH:mm)
	 */
	public String OpenTime(){
		return ss.format(OpenTime.getTime());
	}

	/**
	 * ƼĮ ������ �������� ���� �Ǿ� �������� ������ �ش�.
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
	 * ������ ������ ���� �������� �����ش�
	 * @return true : ����
	 */
	public boolean isCrockIng(){
		if(eva.getOpenContinuation() == 1) return true;
		else return false;
	}

	static class CrockMSG implements Runnable {
		private int _status;

		// �ð��� �տ� - �׺� ���� ������ ��ȣ
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
				case 0:// �� ��Ų��
					L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1467));// �ð��� �տ��� �� �����ϴ�.
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1476));//�ý��� �޽��� : 30�� �Ŀ� �ڷ���Ʈ �մϴ�.
						}
					}
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1477));//�ý��� �޽��� : 20�� �Ŀ� �ڷ���Ʈ �մϴ�.
						}
					}
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1478));//�ý��� �޽��� : 10�� �Ŀ� �ڷ���Ʈ �մϴ�.
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
					L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1468));// �ð��� �տ��� ������ϴ�
					break;
				case 1:
					//for(L1PcInstance pc : sList){
					//	int[] Item = Item();
					//	pc.getInventory().storeItem(Item[0], Item[1]);
					//}
					//for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
					switch(eva.getMoveLocation()){
					case 1:
						//pc.sendPackets(new S_ServerMessage(1474)); // �׺� ���ø��� : �̷�����..!!! �츮�� ����.
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1474)); // �׺� ���ø��� : �̷�����..!!! �츮�� ����.
						break;
					case 2:
						//pc.sendPackets(new S_ServerMessage(1488));	// ����ĭ : �̷�����..!!! �츮�� ����.
						L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(1488)); // ����ĭ : �̷�����..!!! �츮�� ����.
						break;
					}						
					//}
					
					/*
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1476));//�ý��� �޽��� : 30�� �Ŀ� �ڷ���Ʈ �մϴ�.
						}
					}
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						if(pc.getMap().getId() >= 780 && pc.getMap().getId() <= 784){
							pc.sendPackets(new S_ServerMessage(1477));//�ý��� �޽��� : 20�� �Ŀ� �ڷ���Ʈ �մϴ�.
						}
					}
					 */
					Thread.sleep(10000L);
					for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
						switch(pc.getMapId()){
						case 782:
							L1Teleport.teleport(pc, 32628, 32906, (short) 780, 5, true); break; // �׺� �縷���� �ڷ���Ʈ
						case 784:
							L1Teleport.teleport(pc, 32793, 32754, (short) 783, 2, false); break; // ���� ������ �ڷ���Ʈ
						default: break;
						}
					}
					sList.clear();
					break;
				}
			} catch (Exception exception){ }	
		}

		/**
		 * ������ ���� ���̵� ���� �ݳ� - �׺�
		 * @return	(int[]) Itemid	���޹��� �����۾��̵�, ����
		 */
		@SuppressWarnings("unused")
		private int[] Item(){
			return ItemId[(int)(Math.random() * ItemId.length)];
		}

		/**
		 * �տ��� �����Ѵ�.
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
