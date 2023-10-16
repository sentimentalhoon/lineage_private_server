
package l1j.server.server.TimeController;

import java.util.List;
import java.util.Random;
import l1j.server.server.datatables.ItemTable;
import java.util.ArrayList;

import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.model.Instance.L1ItemInstance;

public class FishingTimeController implements Runnable {
	private static FishingTimeController _instance;
	private final List<L1PcInstance> _fishingList =	new ArrayList<L1PcInstance>();
	private static Random _random = new Random(System.nanoTime());

	public static FishingTimeController getInstance() {
		if (_instance == null) {
			_instance = new FishingTimeController();
		}
		return _instance;
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(300);
				fishing();
			}
		} catch (Exception e1) {
		}
	}

	public void addMember(L1PcInstance pc) {
		if (pc == null || _fishingList.contains(pc)) {
			return;
		}
		_fishingList.add(pc);
	}

	public void removeMember(L1PcInstance pc) {
		if (pc == null || !_fishingList.contains(pc)) {
			return;
		}
		_fishingList.remove(pc);
	}

	private void fishing() {
		if (_fishingList.size() > 0) {
			long currentTime = System.currentTimeMillis();
			L1PcInstance pc = null;
			for (int i = 0; i < _fishingList.size(); i++) {
				pc = _fishingList.get(i);
				if (pc.isFishing() && pc.getMapId() == 5302) {
					long time = pc.getFishingTime();
					if (currentTime <= (time + 1000) && currentTime >= (time - 1000) ) {
						pc.setFishingReady(true);
						pc.sendPackets(new S_PacketBox(S_PacketBox.FISHING));
					} else if ( currentTime > (time + 100)){
						long time2 = System.currentTimeMillis() + 40000 + _random.nextInt(6) * 1000;
						int chance = _random.nextInt(1000);
						if (chance < 1) {
							successFishing(pc, 435009, "$7453"); // �ڸ�����
							pc.setFishingTime(time2);
						} else if (chance < 2){
							successFishing(pc, 435010, "$7457"); // �ڸ�����
							pc.setFishingTime(time2);
						} else if (chance < 3){
							successFishing(pc, 435011, "$7454"); // �ڸ�����
							pc.setFishingTime(time2);
						} else if (chance < 4){
							successFishing(pc, 435012, "$7455"); // �ڸ�����
							pc.setFishingTime(time2);
						} else if (chance < 5){
							successFishing(pc, 435013, "$7456"); // �ڸ�����
							pc.setFishingTime(time2);
						} else if (chance < 10){
							successFishing(pc, 41159, "$5116"); // �ź��� ���� ����
							pc.setFishingTime(time2);
						} else if (chance < 50) {
							successFishing(pc, 4500163, "$8811"); // ��¦�̴� ���
							pc.setFishingTime(time2);
						} else if (chance < 250) {
							successFishing(pc, 41297, "$5250"); // �׾�
							pc.setFishingTime(time2);
						} else if (chance < 400) {
							successFishing(pc, 41296, "$5249"); // �ؾ�
							pc.setFishingTime(time2);
						} else if (chance < 520) {
							successFishing(pc, 41298, "$5256"); // � �����
							pc.setFishingTime(time2);
						} else if (chance < 620) {
							successFishing(pc, 41299, "$5257"); // ����� �����
							pc.setFishingTime(time2);
						} else if (chance < 720) {
							successFishing(pc, 41300, "$5258"); //  ���� �����
							pc.setFishingTime(time2);
						} else if (chance < 820) {
							successFishing(pc, 41301, "$5259"); // ���� �� ���� �����
							pc.setFishingTime(time2);
						} else if (chance < 850) {
							successFishing(pc, 41302, "$5260"); // �ʷ� �� ���� �����
							pc.setFishingTime(time2);
						} else if (chance < 880) {
							successFishing(pc, 41303, "$5261"); // �Ķ� �� ���� �����
							pc.setFishingTime(time2);
						} else if (chance < 910) {
							successFishing(pc, 41304, "$5262"); // �� �� ���� �����
							pc.setFishingTime(time2);
						} else if (chance < 940) {
							successFishing(pc, 41425, "$7509"); // ���� ����� 
							pc.setFishingTime(time2);
						} else if (chance < 950) {
							successFishing(pc, 41252, "$5248"); // ������ �ź���  
							pc.setFishingTime(time2);
						} else if (chance < 960) {
							successFishing(pc, 41251, "$5235"); // �ذ񼺹�
							pc.setFishingTime(time2);
						} else if (chance < 961) {
							//successFishing(pc, 40396, "$150"); // ������
							pc.setFishingTime(time2);
						} else if (chance < 962) {
							//successFishing(pc, 40395, "$149"); // ������
							pc.setFishingTime(time2);
						} else if (chance < 963) {
							//successFishing(pc, 40394, "$146"); // ǳ����
							pc.setFishingTime(time2);
						} else if (chance < 964) {
							//successFishing(pc, 40393, "$145"); // ȭ����
							pc.setFishingTime(time2);
						} else if (chance < 968) {
							successFishing(pc, 40066, "$2578"); // ����
							pc.setFishingTime(time2);
						} else if (chance < 973) {
							successFishing(pc, 40087, "$244"); // ����
							pc.setFishingTime(time2);
						} else if (chance  < 980) {
							successFishing(pc, 40074, "$249"); // ��
							pc.setFishingTime(time2);
						} else if (chance  < 985) {
							successFishing(pc, 41305, "$5264"); // ���� �Ͱ���
							pc.setFishingTime(time2);
						} else if (chance  < 990) {
							successFishing(pc, 41306, "$5263"); // ���� ����
							pc.setFishingTime(time2);
						} else if (chance  < 995) {
							successFishing(pc, 41307, "$5264"); // ���� �����
							pc.setFishingTime(time2);							
						} else if (chance < 998) { 
							successFishing(pc, 400075, "����200����ǥ"); // ���� 200�� ��ǥ
							pc.setFishingTime(0);
							pc.setFishingReady(false);
							pc.setFishing(false);
							pc.sendPackets(new S_CharVisualUpdate(pc));
							Broadcaster.broadcastPacket(pc, new S_CharVisualUpdate(pc));
							pc.sendPackets(new S_ServerMessage(1163, ""));// ���ð� �����߽��ϴ�.
							removeMember(pc);
						} else {
							pc.sendPackets(new S_ServerMessage(1136, "")); // ���ý���
							pc.setFishingTime(time2);
							pc.getInventory().consumeItem(41295, 1);
						}
					}
				}
			}
		} 
	}

	private void successFishing(L1PcInstance pc, int itemId, String message) {
		if (pc.getInventory().getSize() > (180 - 16)) {//�κ� 180���̻� üũ
			pc.setFishingTime(0);
			pc.setFishingReady(false);
			pc.setFishing(false);
			pc.sendPackets(new S_CharVisualUpdate(pc));
			Broadcaster.broadcastPacket(pc, new S_CharVisualUpdate(pc));
			pc.sendPackets(new S_ServerMessage(263));
			removeMember(pc);
			return;
		}
		if (pc.getInventory().checkItem(41295, 1) && pc.getInventory().checkItem(41293, 1)){
			L1ItemInstance item = ItemTable.getInstance().createItem(itemId);
			item.startItemOwnerTimer(pc);
			pc.getInventory().storeItem(item);
			pc.getInventory().consumeItem(41295, 1);   //����
			pc.sendPackets(new S_ServerMessage(1185, message));//���ÿ� ������ �� �����߽��ϴ�.
		} else {  //���˴�� ����
			pc.setFishingTime(0);
			pc.setFishingReady(false);
			pc.setFishing(false);
			pc.sendPackets(new S_CharVisualUpdate(pc));
			Broadcaster.broadcastPacket(pc, new S_CharVisualUpdate(pc));
			pc.sendPackets(new S_ServerMessage(1137)); //���ø��ϱ� ���ؼ� ���̰� �ʿ��մϴ�.
			removeMember(pc);
			return;
		}
	}
}