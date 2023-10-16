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
	//��Ʋ�� ����Ʈ
	private final List<L1PcInstance> _BattleList = new ArrayList<L1PcInstance>();
	///��Ʋ�� ����Ʈ�� �ο��� �߰��Ѵ�.
	public void addBattleList(L1PcInstance pc){
		if (pc == null || _BattleList.contains(pc)){
			return;
		}
		_BattleList.add(pc);
	}

	//����Ʈ���� �ο����� �����Ѵ�
	public void removeBattleList(L1PcInstance pc){
		if (pc == null || !_BattleList.contains(pc)){
			return;
		}
		_BattleList.remove(pc);
	}
	//��Ʋ�� �ο��� üũ
	public int getBattleCount(){
		return _BattleList.size();
	}
	public L1PcInstance[] getBattleMembers() {
		return _BattleList.toArray(new L1PcInstance[_BattleList.size()]);
	}
	//����Ʈ ���� ��� ��ü�� �����.
	public void BattleListClear(){
		_BattleList.clear();
	}
	//���� ���θ� �Ǵ��ϱ� ����.
	private boolean _BattleStart;
	public boolean getBattleStart() {
		return _BattleStart;
	}
	public void setBattleStart(boolean Battle) {
		_BattleStart = Battle;
	}
	//���� ���۽ð�����.
	private boolean _BattleOne;
	public boolean getBattleOne(){
		return _BattleOne;
	}
	public void setBattleOne(boolean BattleOne){
		_BattleOne = BattleOne;
	}
	//��Ʋ�������� �Ǵ�
	private boolean _Battling;
	public boolean getBattling(){
		return _Battling;
	}
	public void setBattling(boolean Battling){
		_Battling = Battling;
	}
	//��Ʋ���� ���� ����
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
	//�ð� ���� �κ�.
	private long _endTime;
	public long getendTime(){
		return _endTime;
	}
	// �⺻������ 1�ð��� �����մϴ�.
	public void setendTime(long endTime){
		_endTime = endTime;
	}
	//���½ð�����
	private long _openTime;
	public long getopenTime(){
		return _openTime;
	}
	public void setopenTime(long openTime){
		_openTime = openTime;
	}
	//����ð�
	private long _InBattleTime;
	public long getInBattleTime(){
		return _InBattleTime;
	}
	public void setInBattleTime(long InBattleTime){
		_InBattleTime = InBattleTime;
	}
	private int ADeadCount;
	private int BDeadCount;
	//��Ʋ�� ���սð� ����
	public void BattleTime(){
		if(getBattleOne()){//���� �����̶��
			setendTime(System.currentTimeMillis() + 10000);//���� ���۽� ������ 10��.
		}else{
			setendTime(System.currentTimeMillis() + 14400000);//�� ��Ʋ�� ��Ÿ�� �⺻ 1�ð� 3600000
		}
		setopenTime(getendTime() + 10000); //������ 10��
		setInBattleTime(getopenTime() + 120000);//���� �ð� 2��
	}
	private class TimeUpdater implements Runnable {
		public void run() {
			try {
				while (true) {
					if(getBattleOpen()){//���忩�θ� �Ǵ��Ѵ�.
						if(getopenTime() < System.currentTimeMillis()){//���� �ð��̶��
							L1World.getInstance().broadcastServerMessage("\\fW��Ʋ���� ���Ƚ��ϴ�. 2�� ���� ���� �����մϴ�.");
							setBattleOpen(false);
							setBattleStart(true);
						}
					}else if(getBattleStart()){
						if(getInBattleTime() < System.currentTimeMillis()){//����ð��� �ٵǾ��ٸ�
							L1World.getInstance().broadcastServerMessage("\\fW��Ʋ�� ����ð��� ����Ǿ����ϴ�.");
							setBattleStart(false);
							setBattling(true);
							InBattle();
						}

					}else if(getBattling()){//��Ʋ���� ���۵Ǿ��ٸ�
						BattleStart();
					}
					Thread.sleep(1000);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	//��Ʋ������ �̵�.
	private void InBattle(){
		//����Ʈ�� ����ִ� ����� ����ŭ �����Ų��.
		for (int i = 0; i < _BattleList.size(); i++) {
			L1PcInstance pc = _BattleList.get(i);//����Ʈ���� ����� �����´�.
			if(pc != null){
				if (pc.getMapId() == 5083){//����忡 ����� �ִٸ�.
					if(pc.getBattleOk()){//������ ���� ����̶��.
						if(i % 2 == 0){//¦���� ���
							pc.set_BattleLine(2);
							L1Teleport.teleport(pc, 32709 ,32829, (short) 208, 0, true);
							pc.sendPackets(new S_SystemMessage("��Ʋ������ �����ϼ̽��ϴ�. B����"));
							Poly(pc, 2);
							createHp(pc);
						}else{//Ȧ���ϰ��
							pc.set_BattleLine(1);
							L1Teleport.teleport(pc, 32709 ,32829, (short) 208, 0, true);
							pc.sendPackets(new S_SystemMessage("��Ʋ������ �����ϼ̽��ϴ�. A����"));
							Poly(pc, 1);
							createHp(pc);
						}
					}else{
						pc.sendPackets(new S_SystemMessage("���������� �������� �̵����� �ʾҽ��ϴ�."));
						L1Teleport.teleport(pc, 33442, 32799, (short) 4, 0, true);
					}
				}
			}
		}
	}
	private void BattleStart(){
		//�ʱ�ȭ
		ADeadCount = 0;
		BDeadCount = 0;

		//�ǽð����� ���� �������� üũ�Ѵ�.
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

		//���� �ð��� �ƴ϶��
		if(getendTime() < System.currentTimeMillis()){
			if(ADeadCount >= 1 && BDeadCount ==0){//����ִ� A����� 1���� ũ��, B����� 0���̶�� ����
				BattleEnd(ADeadCount, BDeadCount);
			}else if(BDeadCount >= 1 && ADeadCount == 0){//����ִ� B����� 1����ũ��, A����� 0���̶�� ����
				BattleEnd(ADeadCount, BDeadCount);
			}
		}else{//���� �ð��� �����ٸ� �ٷ� ��������
			BattleEnd(ADeadCount, BDeadCount);
		}
		if(getBattleCount() == 0){ //�����ڰ� ���� ���
			BattleEnd(0, 0);
		}
	}
	private void BattleEnd(int countA, int countB){
		int winLine = 0;

		switch(getBattleCount()){
		case 0 ://�����ڰ� 0��
			winLine = 3;
			L1World.getInstance().broadcastServerMessage("\\fW�����ڰ� ���� ��Ʋ���� ����Ǿ����ϴ�.");
			break;
		case 1 ://1�� �϶�
			winLine = 3;
			L1World.getInstance().broadcastServerMessage("\\fWȥ�ڼ��� ��Ʋ���� ������ �� �����ϴ�.");
			L1World.getInstance().broadcastServerMessage("\\fW�����е��� ���� ���� ��Ź�帳�ϴ�.");
			break;
		default : //�ش������ ������ 1�� �̻��̴�
			if(countA > countB){
				winLine = 1;
				L1World.getInstance().broadcastServerMessage("\\fW��Ʋ�� A������ �¸��Դϴ�.");
			}else if(countA < countB){
				winLine = 2;
				L1World.getInstance().broadcastServerMessage("\\fW��Ʋ�� B������ �¸��Դϴ�.");
			}else if (countA == countB){
				winLine = 3;
				L1World.getInstance().broadcastServerMessage("\\fW��Ʋ�� ���º��Դϴ�.");
			}
			break;
		}

		//������ ���ް� ������ ��Ʋ ���¸� �ʱ�ȭ�Ѵ�.
		for (L1PcInstance pc : getBattleMembers()) {
			if(pc != null){
				if(pc.get_BattleLine() == winLine){
					//������ �߰�
					pc.getInventory().storeItem(41159, 500);
					pc.sendPackets(new S_SystemMessage("\\fW�ź��� ���� ���� (500)���� ������ϴ�."));
					pc.set_BattleLine(0);
					pc.setBattleOk(false);
					deleteMiniHp(pc);
					removeBattleList(pc);//��ü ����Ʈ ������ ���� �۵��ȵɶ��� ����ؼ� �ٽ� �־��
					if(!pc.isDead()){//���� ���� ����� ������
						L1Teleport.teleport(pc, 33442, 32799, (short) 4, 0, true);
					}else{ // ���� ������Դ� �޼���
						pc.sendPackets(new S_SystemMessage("��Ʋ���� �������ϴ�. ����ŸƮ�� ���ֽñ� �ٶ��ϴ�."));

					}
				}else{
					pc.set_BattleLine(0);
					pc.setBattleOk(false);
					deleteMiniHp(pc);
					removeBattleList(pc);//��ü ����Ʈ ������ ���� �۵��ȵɶ��� ����ؼ� �ٽ� �־��
					if(!pc.isDead()){//���� ���� ����� ������
						L1Teleport.teleport(pc, 33442, 32799, (short) 4, 0, true);
					}else{ // ���� ������Դ� �޼���
						pc.sendPackets(new S_SystemMessage("��Ʋ���� �������ϴ�. ����ŸƮ�� ���ֽñ� �ٶ��ϴ�."));

					}
				}
			}
		}
		L1World.getInstance().broadcastServerMessage("\\fW��Ʋ���� ����Ǿ����ϴ�. ���� ����ð��� 4�ð� �� �Դϴ�.");
		setBattleOpen(true);
		setBattleStart(false);
		setBattleOne(false);
		setBattling(false);
		BattleTime();//�ð��ʱ�ȭ
		BattleListClear();//����Ʈ Ŭ����
	}
	// �̴� HP�ٸ� �����Ѵ�.
	private void createHp(L1PcInstance pc) {
		for (L1PcInstance member : getBattleMembers()) {
			if(member != null){
				if(pc != member){
					if(pc.get_BattleLine() == member.get_BattleLine()){//���� ������ ������� HP�ٸ� ����
						member.sendPackets(new S_HPMeter(pc.getId(), 100 * pc.getCurrentHp() / pc.getMaxHp()));
						pc.sendPackets(new S_HPMeter(member.getId(), 100 * member.getCurrentHp() / member.getMaxHp()));
					}
				}
			}
		}
	}
	//HP�ٸ� �����Ѵ�.
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
			//Ÿ�Ժ� �з�
			switch(pc.getWeapon().getItem().getType()){
			//Ȱ
			case 4:
			case 13:
				if(BattleLine == 1){
					polyid = 6269;
				}else{
					polyid = 6272;
				}
				L1PolyMorph.doPoly(pc, polyid, time, 1);
				break;
				//ũ�ο� �̵���
			case 11:
			case 12:
				if(BattleLine == 1){
					polyid = 6279;
				}else{
					polyid = 6280;
				}
				L1PolyMorph.doPoly(pc, polyid, time, 1);
				break;
				//������
			case 7:
			case 16:
				if(BattleLine == 1){
					polyid = 6268;
				}else{
					polyid = 6271;
				}
				L1PolyMorph.doPoly(pc, polyid, time, 1);
				break;
				//�׿�..
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
