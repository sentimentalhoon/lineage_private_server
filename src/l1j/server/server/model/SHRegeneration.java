package l1j.server.server.model;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;

public class SHRegeneration extends TimerTask {
	private static Logger _log = Logger.getLogger(SHRegeneration.class
			.getName());

	private final L1PcInstance _pc;

	public SHRegeneration(L1PcInstance pc) {
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead()) {
				return;
			}
			regenItem();
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void regenItem() {		
		_pc.getInventory().storeItem(L1ItemId.SHINY_LEAF,1);
		_pc.sendPackets(new S_ServerMessage(403, "$6379"));
	}	
}
