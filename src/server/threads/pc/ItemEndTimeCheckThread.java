
package server.threads.pc;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
public class ItemEndTimeCheckThread extends Thread {
	private static ItemEndTimeCheckThread _instance = null;

	public static ItemEndTimeCheckThread getInstance() {
		if (_instance == null) {
			_instance = new ItemEndTimeCheckThread();
			_instance.start();
		}
		return _instance;
	}
	public ItemEndTimeCheckThread(){}

	public void run() {
		System.out.println("[Thread Start] 시간제 아이템 삭제");
		while (true) {
			long currentTimeMillis = System.currentTimeMillis();
			try {
				for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
					if (pc == null || pc.isDead() || pc.noPlayerCK) continue;
					L1Inventory pcInventory = pc.getInventory();
					for (L1ItemInstance item : pcInventory.getItems()) {
						if (item == null) continue;
						if (item.getEndTime() == null) continue;
						if (currentTimeMillis > item.getEndTime().getTime()) {
							pcInventory.removeItem(item);
							pc.sendPackets(new S_ServerMessage(1195, item.getName() + "의"));
						}
					}
				}
			} catch (Exception e) {e.printStackTrace();}
			try {
				Thread.sleep(60000);
			} catch (Exception e) {}
		}
	}
}