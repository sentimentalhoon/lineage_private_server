package l1j.server.server.model;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SkillSound;

public class MpRegenerationByDoll extends TimerTask {
	private static Logger _log = Logger.getLogger(MpRegenerationByDoll.class.getName());

	private final L1PcInstance _pc;

	public MpRegenerationByDoll(L1PcInstance pc) {
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead()) {
				return;
			}
			regenMp();
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void regenMp() {
		int regenMp = 0; 
		
		for (L1DollInstance doll : _pc.getDollList().values()) {
			regenMp = doll.getMpRegenerationValues();
		}
		
		int newMp = _pc.getCurrentMp() + regenMp;

		_pc.setCurrentMp(newMp);
		_pc.sendPackets(new S_SkillSound(_pc.getId(), 6321)); 
		Broadcaster.broadcastPacket(_pc, new S_SkillSound(_pc.getId(), 6321));

	}

}
