package l1j.server.server;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class SkyCastleController extends Thread {

	private static SkyCastleController _instance;

	private boolean _SkyStart;
	public boolean getSkyStart() {
		return _SkyStart;
	}
	public void setSkyStart(boolean Sky) {
		_SkyStart = Sky;
	}
	private static long sTime = 0;

	private String NowTime = "";
	//�ð� ����
	public int LOOP = 0;//�Ϸ� 1�� ����

	public boolean isGmOpen = false;

	private static final SimpleDateFormat s = new SimpleDateFormat("HH", Locale.KOREA);

	private static final SimpleDateFormat ss = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

	public static SkyCastleController getInstance() {
		if(_instance == null) {
			_instance = new SkyCastleController();
		}
		return _instance;
	}
	public SkyCastleController(){
		LOOP = 24;
	}


	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(1000); 
				/** ���� **/
				if(!isOpen() && !isGmOpen)
					continue;
				if(L1World.getInstance().getAllPlayers().size() <= 0)
					continue;

				isGmOpen = false;

				/** ���� �޼��� **/
				L1World.getInstance().broadcastServerMessage("\\fW�����, �ϴ��� �� NPC VS ���� �������� ���۵˴ϴ�!");

				/** ������ ����**/
				setSkyStart(true);

				/** ���� 60�� ����**/

				Thread.sleep(3800000L); //60������

				/** 60�� �� �ڵ� �ڷ���Ʈ**/
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

	/**
	 *���� �����ִ��� Ȯ��
	 *
	 *@return (boolean) �����ִٸ� true �����ִٸ� false
	 */
	private boolean isOpen() {
		NowTime = getTime();
		//if((Integer.parseInt(NowTime) % LOOP) == 0) return true;
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
			case 18:
			case 630:
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33442, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("�ϴ��� �� �������� �������ϴ�."));
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
			case 18: //���簳�ʹ���
			case 630: //�ϴ��Ǽ�
				c.stopHpRegenerationByDoll();
				c.stopMpRegenerationByDoll();
				L1Teleport.teleport(c, 33442, 32797, (short) 4, 4, true);
				c.sendPackets(new S_SystemMessage("�ϴ��� �� �������� �������ϴ�."));
				break;
			default:
				break;
			}
		}
	}

	/** ���� **/
	private void End() {
		L1World.getInstance().broadcastServerMessage("\\fW�ϴ��� �� �������� �������ϴ�. ���� �����̽��ϴ�.");
		setSkyStart(false);
	}
}

