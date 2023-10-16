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
			L1World.getInstance().broadcastServerMessage(" \\fY잠시 후 설문조사가 시작됩니다. (제한시간 30초)");
			L1World.getInstance().broadcastServerMessage(" YES = 찬성, NO = 반대, 그외 무효~!");
			Thread.sleep(3000L);
			L1World.getInstance().broadcastPacketToAll(new S_Message_YN(622, maintext));
			Thread.sleep(30000L);
			L1World.getInstance().broadcastServerMessage(" 잠시 후 설문조사 결과가 발표됩니다.");
			Thread.sleep(3000L);
			L1World.getInstance().broadcastServerMessage(" \\fW[결과] 찬성 : " + good + "표, 반대 : " + bad + "표");			
			
//			ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_GM, "\n [설문내용] : " + maintext, null);
//			ChatMonitorChannel.getInstance().sendMsg(ChatMonitorChannel.CHAT_MONITOR_GM, "\n [설문결과] : 찬성 = " + good + "표,  반대 = "+bad+"표", null);
			_instance = null;		
			mainstart = false;
			maintext = "";			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
