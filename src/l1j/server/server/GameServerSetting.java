package l1j.server.server;

public class GameServerSetting extends Thread{
	private static GameServerSetting _instance;

	public static GameServerSetting getInstance(){
		if (_instance == null){
			_instance = new GameServerSetting();
		}
		return _instance;
	}

	/** Server Manager 1 ���� �κ� **/
	public static boolean �Ϲ� = false;
	public static boolean �ӼӸ� = false;
	public static boolean �۷ι� = false;
	public static boolean ���� = false;
	public static boolean ��Ƽ = false;
	public static boolean ��� = false;	
	public static boolean Att = false;
	public static boolean NYEvent = false;

	public static boolean ServerDown = false;

	private GameServerSetting(){
	}
	private int maxLevel = 100;

	public int get_maxLevel() {
		return maxLevel;
	}
	public void set_maxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}
	public void run(){
		while(true){
			try{
				sleep(1000L);
			}catch(Exception e){}
		}
	}



}
