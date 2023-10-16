package l1j.server.server.model;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SkillSound; //## [A142] MP 회복시 이팩트 보이도록

public class HpRegenerationByDoll extends TimerTask {
	private static Logger _log = Logger.getLogger(HpRegenerationByDoll.class
			.getName());

	private final L1PcInstance _pc;

	public HpRegenerationByDoll(L1PcInstance pc) {
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead()) {
				return;
			}
			regenHp();
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void regenHp() {
		int regenHp = 0; 
		for (L1DollInstance doll : _pc.getDollList().values()) {
			regenHp = doll.getHpRegenerationValues();
		}


		int newHp = _pc.getCurrentHp() + 150 + regenHp;
		if (newHp < 0) {
			newHp = 0;
		}
		_pc.setCurrentHp(newHp);
		_pc.sendPackets(new S_SkillSound(_pc.getId(), 1608)); 
		Broadcaster.broadcastPacket(_pc, new S_SkillSound(_pc.getId(), 1608));
	}
}
