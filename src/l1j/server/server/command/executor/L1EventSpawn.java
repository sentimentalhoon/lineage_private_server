package l1j.server.server.command.executor;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.server.ObjectIdFactory;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.model.L1NpcDeleteTimer;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
public class L1EventSpawn implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1EventSpawn.class.getName());
	private L1EventSpawn() {
	}
	public static L1CommandExecutor getInstance() {
		return new L1EventSpawn();
	}
	private void sendErrorMessage(L1PcInstance pc, String cmdName) {
		String errorMsg = cmdName+  "[NPCID] [시간(분)] ";
		pc.sendPackets(new S_SystemMessage(errorMsg));
	}
	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) { 
		try {   
			StringTokenizer tok = new StringTokenizer(arg);
			String nameid = tok.nextToken();
			String time1 = tok.nextToken();
			int npcId =0;
			try {
				npcId = Integer.parseInt(nameid);
			} catch (NumberFormatException e) {
				npcId = NpcTable.getInstance(). findNpcIdByNameWithoutSpace(nameid);
				if (npcId == 0) {
					pc.sendPackets(new S_SystemMessage("해당 NPC가 발견되지 않습니다. "));
					return;
				}
			}
			int time = Integer.parseInt(time1);

			nameid = NpcTable.getInstance(). getTemplate(npcId).get_name();
			Eventspawn(pc, npcId, 60000*time);

			pc.sendPackets(new S_SystemMessage("("+nameid+") (ID:" + npcId + ") ("+ time + ")분 소환 "));
			L1World.getInstance().broadcastServerMessage("("+nameid+")  ("+time+")분 동안 소환됩니다. ");
			tok = null;
			nameid = null;   
		} catch (NoSuchElementException e) {
			sendErrorMessage(pc, cmdName);
		} catch (NumberFormatException e) {
			sendErrorMessage(pc, cmdName);
		} catch (Exception e) {
			//_log.log(Level.SEVERE, "", e); 
			sendErrorMessage(pc, cmdName);
		}
	}

	private void Eventspawn(L1PcInstance pc, int npcId, int timeMinToDelete) {
		try {
			L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(npcId);
			npc.setId(ObjectIdFactory.getInstance().nextId());
			npc.setMap(pc.getMapId());

			npc.getLocation().set(pc.getLocation());
			npc.getLocation().forward(pc.getMoveState().getHeading());

			npc.setHomeX(npc.getX());
			npc.setHomeY(npc.getY());
			npc.getMoveState().setHeading(pc.getMoveState().getHeading());

			if(npc instanceof L1MonsterInstance) {
				L1MonsterInstance mon = (L1MonsterInstance)npc;
				mon.set_storeDroped(2);
			}

			L1World.getInstance().storeObject(npc);
			L1World.getInstance().addVisibleObject(npc);
			npc.getLight().turnOnOffLight();
			npc.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE); // 채팅 개시
			if (0 < timeMinToDelete) {
				L1NpcDeleteTimer timer = new L1NpcDeleteTimer(npc, timeMinToDelete);
				timer.begin();
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}