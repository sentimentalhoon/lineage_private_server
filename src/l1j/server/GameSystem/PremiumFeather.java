package l1j.server.GameSystem;

import java.util.Calendar;

import server.system.autoshop.AutoShopManager;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.BaseTime;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.gametime.TimeListener;
import l1j.server.server.serverpackets.S_ServerMessage;

public class PremiumFeather implements TimeListener{
	private static PremiumFeather _instance;
	
	public static void start() {
		if (_instance == null) {
			_instance = new PremiumFeather();
		}
		_instance.some();
		RealTimeClock.getInstance().addListener(_instance);
	}

	private void some() {}

	@Override
	public void onDayChanged(BaseTime time) {}

	@Override
	public void onHourChanged(BaseTime time) {}

	@Override
	public void onMinuteChanged(BaseTime time) {
		int rm = time.get(Calendar.MINUTE);
		if(rm % 12 == 0)
			PremiumTime();
	}

	@Override
	public void onMonthChanged(BaseTime time) {}

	private void PremiumTime() {
		int premiumNumber = 6;	
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if(AutoShopManager.getInstance().isAutoShop()){
				if(!pc.isPrivateShop()) {
					pc.getInventory().storeItem(41159, premiumNumber); // Ω≈∫Ò«— ≥Ø∞≥±Í≈– ¡ˆ±ﬁ 
					pc.sendPackets(new S_ServerMessage(403, "$5116 ("+premiumNumber+")"));
				}
			}else{
				//pc.getInventory().storeItem(41159, premiumNumber); // Ω≈∫Ò«— ≥Ø∞≥±Í≈– ¡ˆ±ﬁ 
				//pc.sendPackets(new S_ServerMessage(403, "$5116 ("+premiumNumber+")"));
			}
		}
	}
}
