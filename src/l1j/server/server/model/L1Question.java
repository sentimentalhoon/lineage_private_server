package l1j.server.server.model;

//import l1j.server.channel.ChatMonitorChannel;
import l1j.server.server.serverpackets.S_Message_YN;

public class L1Question extends Thread{

	private static L1Question _instance;
	public static String maintext;
	public static int good;
	public static int bad;
	public static boolean mainstart;

	public static L1Question getInstance(String text) {
		if (_instance == null) {
			_instance = new L1Question(text);			
		}
		return _instance;
	}

	private L1Question(String text){
		good = 0;
		bad = 0;
		maintext = text;
		start();
	}

	@Override
	public void run(){
		try{
			mainstart = true;
			L1World.getInstance().broadcastServerMessage(" \\fY��� �� �������簡 ���۵˴ϴ�. (���ѽð� 30��)");
			L1World.getInstance().broadcastServerMessage(" YES = ����, NO = �ݴ�, �׿� ��ȿ~!");
			Thread.sleep(3000L);
			L1World.getInstance().broadcastPacketToAll(new S_Message_YN(622, maintext));
			Thread.sleep(30000L);
			L1World.getInstance().broadcastServerMessage(" ��� �� �������� ����� ��ǥ�˴ϴ�.");
			Thread.sleep(3000L);
			L1World.getInstance().broadcastServerMessage(" \\fW[���] ���� : " + good + "ǥ, �ݴ� : " + bad + "ǥ");			
			
//			ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_GM, "\n [��������] : " + maintext, null);
//			ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_GM, "\n [�������] : ���� = " + good + "ǥ,  �ݴ� = "+bad+"ǥ", null);
			_instance = null;		
			mainstart = false;
			maintext = "";			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
