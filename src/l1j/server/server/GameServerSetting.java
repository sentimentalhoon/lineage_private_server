package l1j.server.server;

public class GameServerSetting extends Thread{
	private static GameServerSetting _instance;

	public static GameServerSetting getInstance(){
		if (_instance == null){
			_instance = new GameServerSetting();
		}
		return _instance;
	}

	/** Server Manager 1 °ü·Ã ºÎºÐ **/
	public static boolean ÀÏ¹Ý = false;
	public static boolean ±Ó¼Ó¸» = false;
	public static boolean ±Û·Î¹ú = false;
	public static boolean Ç÷¸Í = false;
	public static boolean ÆÄÆ¼ = false;
	public static boolean Àå»ç = false;	
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
