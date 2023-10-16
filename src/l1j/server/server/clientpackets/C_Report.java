package l1j.server.server.clientpackets;

import java.util.logging.Logger;

import server.LineageClient;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_MapSend;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.L1SpawnUtil;



// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Report extends ClientBasePacket {

	private static final String C_REPORT = "[C] C_Report";
	//private static Logger _log = Logger.getLogger(C_Report.class.getName()); 
	public static final int dragon_menu = 0x06;
	public static final int MapSend = 0x0B;

	public C_Report(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		int type = readC();  
		L1PcInstance pc = clientthread.getActiveChar();
		switch (type) {  
		case dragon_menu: 
			int itemid = readD(); 
			int dragonType = readC(); //안타0, 파푸1, 린드2, 발라3
			L1ItemInstance DragonKey = pc.getInventory().getItem(itemid);
			if(DragonKey == null) return;

			switch (dragonType) {
			case 0: // 안타
				L1SpawnUtil.spawn(pc,4212015,0,1800*1000,false);
				L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(2921)); 
				// 강철 길드 난쟁이: 으...드래곤의 울부짖음이 여기까지 들리오. 필시 누군가 드래곤 포탈을 연 것이 확실하오! 준비된 드래곤 슬레이어에게 영광과 축복을!
				pc.getInventory().consumeItem(430116, 1);
				break;
			case 1: // 파푸
				L1SpawnUtil.spawn(pc, 4212016, 0, 1800*1000, false);
				L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(2921));
				pc.getInventory().consumeItem(430116, 1);
				break;
			case 2: // 린드
				break;
			case 3: // 발라
				break;
			default:
				break;
			}
			break;
		case MapSend:
			break;
			/*
			String targetName = null;
			int mapid = 0, x = 0, y = 0, Mid = 0;
			String text = null;
			try{
				targetName = readS();
				mapid = readH();
				x = readH();
				y = readH();
				Mid = readH();
			}catch(Exception e){
				return;
			}
			L1PcInstance target = L1World.getInstance().getPlayer(targetName); 
			if (target == null)
				pc.sendPackets(new S_SystemMessage("해당 캐릭터를 찾을 수 없습니다."));
			else if(pc == target)
				pc.sendPackets(new S_SystemMessage("자신에게 보낼 수 없습니다."));
			else{
				target.sendPackets(new S_PacketBox(pc.getName(), mapid, x, y, Mid));
				pc.sendPackets(new S_SystemMessage(target.getName()+"님에게 전달 되었습니다."));
			}
			break;*/
		default:
			break;
		}

	}
	public String getType() {
		return C_REPORT;
	}
}


