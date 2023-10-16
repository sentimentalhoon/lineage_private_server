/**
 * ���� ���Ǿ� ���� ���� ��ɾ�
 * by - Eva Team.
 */
package l1j.server.server.command.executor;

import java.util.logging.Logger;

import l1j.server.GameSystem.NpcShopSystem;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1NpcShopSwitch implements L1CommandExecutor {
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L1NpcShopSwitch.class.getName());

	private L1NpcShopSwitch() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1NpcShopSwitch();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			boolean power = NpcShopSystem.getInstance().isPower();
			if (!pc.getName().equalsIgnoreCase("��Ƽ��")
					&& !pc.getName().equalsIgnoreCase("�̼��Ǿ�")) {
				pc.sendPackets(new S_SystemMessage("��� �̸��� �ƴϳ׿�."));
						return;
					}
			if(power)
				pc.sendPackets(new S_SystemMessage("�̹� �������Դϴ�."));
			else
				NpcShopSystem.getInstance().npcShopStart();
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("�κ����� Ŀ��� ����"));
		}
	}
}
