
package l1j.server.server.datatables;

import l1j.server.server.serverpackets.S_DoActionShop;
import l1j.server.server.ActionCodes;
import l1j.server.server.model.Broadcaster;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l1j.server.server.AutoTraderThread;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1PrivateShopBuyList;
import l1j.server.server.templates.L1PrivateShopSellList;
import server.system.autoshop.AutoShop;
import server.system.autoshop.AutoShopManager;

public class AutoTraderTable {

	private static AutoTraderTable _instance;
	
	public static AutoTraderTable getInstance() {
		if (_instance == null) {
			_instance = new AutoTraderTable();
		}
		return _instance;
	}
	
	private AutoTraderTable(){
		load();
	}
	
	private void load(){
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select char_name from characters where trader=1 order by objid");
			r = p.executeQuery();
			while(r.next()){
				AutoTraderThread.getInstance().add(r.getString(1));
			}
		}catch(Exception e){
		}finally{
			SQLUtil.close(r);
            SQLUtil.close(p);
            SQLUtil.close(c);
		}
	}
	
	public void startShop(L1PcInstance cha){
		if(cha == null) return;
		ResultSet r = null;
		ResultSet rr = null;
		Connection c = null;
		PreparedStatement p = null;
		PreparedStatement pp = null;
		
		ArrayList<L1PrivateShopSellList> sellList = cha.getSellList();
		ArrayList<L1PrivateShopBuyList> buyList = cha.getBuyList();
		
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select item_objectid, count, price from character_traderitems where char_objectid=? and type=0 order by no");
			p.setInt(1, cha.getId());
			r = p.executeQuery();
			while(r.next()){
				L1PrivateShopSellList pssl = new L1PrivateShopSellList();
				pssl.setItemObjectId(r.getInt(1));
				pssl.setSellTotalCount(r.getInt(2));
				pssl.setSellPrice(r.getInt(3));
				sellList.add(pssl);
			}
			pp = c.prepareStatement("select item_objectid, count, price from character_traderitems where char_objectid=? and type=1 order by no");
			pp.setInt(1, cha.getId());
			rr = pp.executeQuery();
			while(rr.next()){
				L1PrivateShopBuyList psbl = new L1PrivateShopBuyList();
				psbl.setItemObjectId(rr.getInt(1));
				psbl.setBuyTotalCount(rr.getInt(2));
				psbl.setBuyPrice(rr.getInt(3));
				buyList.add(psbl);
			}
		}catch(Exception e){
		}finally{
			SQLUtil.close(r);
			SQLUtil.close(rr);
			SQLUtil.close(p);
            SQLUtil.close(pp);
            SQLUtil.close(c);
		}
		cha.setPrivateShop(true);
		Broadcaster.broadcastPacket(cha, new S_DoActionShop(cha.getId(),ActionCodes.ACTION_Shop, cha.get_ment1(), cha.get_ment2()));
		try{
			AutoShop autoshop = AutoShopManager.getInstance().makeAutoShop(cha);
			AutoShopManager.getInstance().register(autoshop);
		}catch(Exception e){}
	}
}