package server.system.autoshop;

import java.sql.*;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.model.Instance.L1PcInstance;

public class AutoShopImpl implements AutoShop {
	private L1PcInstance shopCharacter;

	public AutoShopImpl(L1PcInstance pc) {
		shopCharacter = pc;
	}

	@Override
	public String getName() {
		return shopCharacter.getName();
	}

	@Override
	public void logout() {
		
		// 시장 저장
		// 무인상점이 켜져있었으나,주인이 드러와서 디비 삭제
		deleteAutoTrader();
		//
		shopCharacter.logout();
		shopCharacter = null;
		
	}
	
	// 시장 저장
	private void deleteAutoTrader(){
		Connection c = null;
		PreparedStatement p = null;
		PreparedStatement pp = null;
		 //shopCharacter
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("delete from character_traderitems where char_objectid=?");
			p.setInt(1, shopCharacter.getId());
			p.execute();
			pp = c.prepareStatement("update characters set trader=0, ment1=?, ment2=? where objid=?");
			pp.setString(1, null);
			pp.setString(2, null);
			pp.setInt(3, shopCharacter.getId());
			pp.execute();
		}catch(Exception e){
		}finally{
			SQLUtil.close(pp);
			SQLUtil.close(p);
			SQLUtil.close(c);			
		}
	}
	//
}