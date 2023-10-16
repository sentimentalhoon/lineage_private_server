package l1j.server.Warehouse;

public class WarehouseManager {	
	private static WarehouseManager uniqueInstance = null;
	private PrivateWarehouseList plist = new PrivateWarehouseList();
	private ElfWarehouseList elist = new ElfWarehouseList();
	private ClanWarehouseList clist = new ClanWarehouseList();
	private PackageWarehouseList packlist = new PackageWarehouseList();
	
	public static WarehouseManager getInstance() {
		if(uniqueInstance == null) {
			synchronized(WarehouseManager.class){
				if(uniqueInstance == null)
					uniqueInstance = new WarehouseManager();
			}
		}
		return uniqueInstance;
	}
	
	public PrivateWarehouse getPrivateWarehouse(String name) {
		return (PrivateWarehouse) plist.findWarehouse(name);
	}
	
	public ElfWarehouse getElfWarehouse(String name) {
		return (ElfWarehouse) elist.findWarehouse(name);
	}
	
	public ClanWarehouse getClanWarehouse(String name) {
		return (ClanWarehouse) clist.findWarehouse(name);
	}
	
	public PackageWarehouse getPackageWarehouse(String name) {
		return (PackageWarehouse) packlist.findWarehouse(name);
	}
	
	public void delPrivateWarehouse(String name) {
		plist.delWarehouse(name);
	}
	
	public void delElfWarehouse(String name) {
		elist.delWarehouse(name);
	}
	
	public void delClanWarehouse(String name) {
		clist.delWarehouse(name);
	}
	
	public void delPackageWarehouse(String name) {
		packlist.delWarehouse(name);
	}
}
