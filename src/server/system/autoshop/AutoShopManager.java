package server.system.autoshop;

import java.util.Vector;

import l1j.server.server.model.Instance.L1PcInstance;

public class AutoShopManager {
	private static AutoShopManager uniqueInstance;
	private Vector<AutoShop> autoshopList;
	private static boolean isAutoShop = true;
	
	private AutoShopManager() {
		autoshopList = new Vector<AutoShop>();
	}
	public static AutoShopManager getInstance() {
		if(uniqueInstance == null) {
			synchronized(AutoShopManager.class) {
				if(uniqueInstance == null) {
					uniqueInstance = new AutoShopManager(); 
				}
			}
		}
		
		return uniqueInstance;
	}
	public boolean isAutoShop(){
		return isAutoShop;
	}
	public void isAutoShop(boolean b){
		isAutoShop = b;
	}
	public void register(AutoShop shop) {
		if(autoshopList.contains(shop)) return;
		autoshopList.add(shop);
	}
	
	public void remove(AutoShop shop) {
		autoshopList.remove(shop);
	}

	public AutoShop getShopPlayer(String charName) {
		AutoShop autoShop = null;
		for(int i = 0 ; i < autoshopList.size() ; i++) {
			autoShop = autoshopList.get(i);			
			if(autoShop.getName().equalsIgnoreCase(charName))	break;
			else autoShop = null;
		}
		return autoShop;
	}
	public int getShopPlayerCount()
	{
		return this.autoshopList.size();
	}
	public AutoShop makeAutoShop(L1PcInstance pc) throws Exception {
		pc.save();
		pc.saveInventory();		
		pc.stopAHRegeneration();
		//pc.stopHpRegeneration();
		//pc.stopMpRegeneration();
		pc.stopHalloweenRegeneration();
		pc.stopAHRegeneration();
		pc.stopHpRegenerationByDoll();
		pc.stopMpRegenerationByDoll();
		pc.stopSHRegeneration();
		pc.stopMpDecreaseByScales();
		pc.stopEquipmentTimer();
		pc.setNetConnection(null);
		//pc.setPacketOutput(null);	
		return new AutoShopImpl(pc);
	}
}
