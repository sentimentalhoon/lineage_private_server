package l1j.server.GameSystem;


public class HotelSystem {

	private static HotelSystem _instance;
	
	public static HotelSystem getInstance() {
		if (_instance == null) {
			_instance = new HotelSystem();
		}
		return _instance;
	}
	
	
}
