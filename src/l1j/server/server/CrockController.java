package l1j.server.server;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SystemMessage;

public class CrockController extends Thread {

	private static CrockController _instance;

	private boolean _CrockStart;
	public boolean getCrockStart() {
		return _CrockStart;
	}
	public void setCrockStart(boolean Crock) {
		_CrockStart = Crock;
	}
	private static long sTime = 0;

	private String NowTime = "";

	public int LOOP = 0;

	private static final SimpleDateFormat s = new SimpleDateFormat("HH", Locale.KOREA);

	private static final SimpleDateFormat ss = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

	public static CrockController getInstance() {
		if(_instance == null) {
			_instance = new CrockController();
		}
		return _instance;
	}

	public CrockController(){
		LOOP = 4;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(1000); 
				/** ���� **/
				if(!isOpen())
					continue;
				if(L1World.getInstance().getAllPlayers().size() <= 0)
					continue;

				/** ���� �޼��� **/
				L1World.getInstance().broadcastServerMessage("\\fW�׺� & ƼĮ�� �ð��� �տ��� ���Ƚ��ϴ�..");
				L1World.getInstance().broadcastServerMessage("\\fW�����ϽǺе��� �ֹ����� �̿����ּ���.");
				L1World.getInstance().broadcastPacketToAll(	new S_PacketBox(S_PacketBox.GREEN_MESSAGE,"�׺� & ƼĮ�� �ð��� �տ��� ���� �Ǿ����ϴ�."));

				/**����**/
				setCrockStart(true);

				/** ���� 2�ð� ����**/

				Thread.sleep(7300000L); //2�ð� 10������

				/** 2�ð� �� �ڵ� �ڷ���Ʈ**/
				TelePort();
				Thread.sleep(5000L);
				TelePort2();

				/** ���� **/
				End();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 *���� �ð��� �����´�
	 *
	 *@return (Strind) ���� �ð�(MM-dd HH:mm)
	 */
	public String OpenTime() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(sTime);
		return ss.format(c.getTime());
	}


	private boolean isOpen() {
		NowTime = getTime();
		if((Integer.parseInt(NowTime) % LOOP) == 0) return true;
		return false;
	}
	/**
	 *���� ����ð��� �����´�
	 *
	 *@return (String) ���� �ð�(HH:mm)
	 */
	private String getTime() {
		return s.format(Calendar.getInstance().getTime());
	}

	/**��������� �ñ��**/
	private void TelePort() {
		for(L1PcInstance c : L1World.getInstance().getAllPlayers()) {
			switch(c.getMap().getId()) {
			case 780: case 781: case 782: case 783:case 784:
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33430, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("�ð��� �տ��� �Ҹ�˴ϴ�."));
				break;
			default:
				break;
			}
		}
	}

	/**��������� �ñ��**/
	private void TelePort2() {
		for(L1PcInstance c : L1World.getInstance().getAllPlayers()) {
			switch(c.getMap().getId()) {
			case 780: case 781: case 782: case 783:case 784:
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33430, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("�ð��� �տ��� �Ҹ�˴ϴ�."));
				break;
			default:
				break;
			}
		}
	}

	/** ���� **/
	private void End() {
		L1World.getInstance().broadcastServerMessage("�ð��� �տ��� �Ҹ�Ǿ����ϴ�.");
		L1World.getInstance().broadcastPacketToAll(	new S_PacketBox(S_PacketBox.GREEN_MESSAGE,"�׺� & ƼĮ�� �ð��� �տ��� �������ϴ�."));
		setCrockStart(false);
	}
}