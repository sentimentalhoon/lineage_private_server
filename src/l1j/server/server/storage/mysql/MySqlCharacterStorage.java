/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.storage.CharacterStorage;
import l1j.server.server.templates.L1PrivateShopBuyList;
import l1j.server.server.templates.L1PrivateShopSellList;
import l1j.server.server.utils.SQLUtil;

public class MySqlCharacterStorage implements CharacterStorage {
	private static Logger _log = Logger.getLogger(MySqlCharacterStorage.class.getName());

	@Override
	public L1PcInstance loadCharacter(String charName) {
		L1PcInstance pc = null;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM characters WHERE char_name=?");
			pstm.setString(1, charName);

			rs = pstm.executeQuery();

			if (!rs.next()) {
				// SELECT가 결과를 돌려주지 않았다.
				return null;
			}

			pc = new L1PcInstance();
			pc.setAccountName(rs.getString("account_name"));
			pc.setId(rs.getInt("objid"));
			pc.setName(rs.getString("char_name"));
			pc.setHighLevel(rs.getInt("HighLevel"));
			pc.setExp(rs.getInt("Exp"));
			pc.addBaseMaxHp(rs.getShort("MaxHp"));
			short currentHp = rs.getShort("CurHp");
			if (currentHp < 1) {
				currentHp = 1;
			}
			pc.setCurrentHp(currentHp);
			pc.setDead(false);
			pc.setActionStatus(0);
			pc.addBaseMaxMp(rs.getShort("MaxMp"));
			pc.setCurrentMp(rs.getShort("CurMp"));

			pc.getAbility().setBaseStr(rs.getByte("BaseStr"));
			pc.getAbility().setStr(rs.getByte("Str"));
			pc.getAbility().setBaseCon(rs.getByte("BaseCon"));
			pc.getAbility().setCon(rs.getByte("Con"));
			pc.getAbility().setBaseDex(rs.getByte("BaseDex"));
			pc.getAbility().setDex(rs.getByte("Dex"));
			pc.getAbility().setBaseCha(rs.getByte("BaseCha"));
			pc.getAbility().setCha(rs.getByte("Cha"));
			pc.getAbility().setBaseInt(rs.getByte("BaseIntel"));
			pc.getAbility().setInt(rs.getByte("Intel"));
			pc.getAbility().setBaseWis(rs.getByte("BaseWis"));
			pc.getAbility().setWis(rs.getByte("Wis"));

			int status = rs.getInt("Status");
			pc.setCurrentWeapon(status);
			int classId = rs.getInt("Class");
			pc.setClassId(classId);
			pc.getGfxId().setTempCharGfx(classId);
			pc.getGfxId().setGfxId(classId);
			pc.set_sex(rs.getInt("Sex"));
			pc.setType(rs.getInt("Type"));
			int head = rs.getInt("Heading");
			if (head > 7) {
				head = 0;
			}
			pc.getMoveState().setHeading(head);
			/*
			 * int locX = resultset.getInt("locX"); int locY =
			 * resultset.getInt("locY"); short map =
			 * resultset.getShort("MapID"); if (locX < 30000 || locX > 40000 ||
			 * locY < 30000 || locY > 40000) { locX = 32564; locY = 32955; } if
			 * (map == 70) { locX = 32828; locY = 32848; } // 강제 이동 short
			 * moveflag = Config.RANGE_RACE_RECOGNIT; if (moveflag != 1) {
			 * Random random = new Random(); //  int rndmap = 1 +
			 * random.nextInt(5); switch (rndmap) { case 1: // skt locX = 33080;
			 * locY = 33392; map = 4; break;
			 * 
			 * case 2: // ti locX = 32580; locY = 32931; map = 0; break;
			 * 
			 * case 3: // wb locX = 32621; locY = 33169; map = 4; break;
			 * 
			 * case 4: // kent locX = 33050; locY = 32780; map = 4; break;
			 * 
			 * case 5: // h locX = 33612; locY = 33268; map = 4; break;
			 * 
			 * default: // skt locX = 33080; locY = 33392; map = 4; break; } }
			 * pc.set_x(locX); pc.set_y(locY); pc.set_map(map);
			 */
			pc.setX(rs.getInt("locX"));
			pc.setY(rs.getInt("locY"));
			pc.setMap(rs.getShort("MapID"));
			pc.set_food(rs.getInt("Food"));
			pc.setLawful(rs.getInt("Lawful"));
			pc.setTitle(rs.getString("Title"));
			pc.setClanid(rs.getInt("ClanID"));
			pc.setClanname(rs.getString("Clanname"));
			pc.setClanRank(rs.getInt("ClanRank"));
			pc.getAbility().setBonusAbility(rs.getInt("BonusStatus"));
			pc.getAbility().setElixirCount(rs.getInt("ElixirStatus"));
			pc.setElfAttr(rs.getInt("ElfAttr"));
			pc.set_PKcount(rs.getInt("PKcount"));
			pc.setExpRes(rs.getInt("ExpRes"));
			pc.setPartnerId(rs.getInt("PartnerID"));
			pc.setAccessLevel(rs.getShort("AccessLevel"));
			if (pc.getAccessLevel() == Config.GMCODE) {
				pc.setGm(true);
				pc.setMonitor(false);
			} else if (pc.getAccessLevel() == 100) {
				pc.setGm(false);
				pc.setMonitor(true);
			} else {
				pc.setGm(false);
				pc.setMonitor(false);
			}
			pc.setOnlineStatus(rs.getInt("OnlineStatus"));
			pc.setHomeTownId(rs.getInt("HomeTownID"));
			pc.setContribution(rs.getInt("Contribution"));
			pc.setHellTime(rs.getInt("HellTime"));
			pc.setBanned(rs.getBoolean("Banned"));
			pc.setKarma(rs.getInt("Karma"));
			pc.setAge(rs.getInt("Age")); 
			pc.setUserName(rs.getString("UserName"));
			pc.setLastPk(rs.getTimestamp("LastPk"));
			pc.setDeleteTime(rs.getTimestamp("DeleteTime"));
			pc.setReturnStat(rs.getInt("ReturnStat"));
			pc.setGdungeonTime(rs.getInt("GdungeonTime"));
			pc.setIvoryTowerTime(rs.getInt("IvoryTowerTime"));
			pc.setLdungeonTime(rs.getInt("LdungeonTime"));
			pc.setAinHasad(rs.getInt("Ainhasad_Exp"));
			pc.setLogOutTime(rs.getTimestamp("Logout_time"));
			pc.setHuntCount(rs.getInt("HuntCount"));
			pc.setHuntPrice(rs.getInt("HuntPrice"));
			pc.setReasonToHunt(rs.getString("HuntText"));
			/* Kill & Death 시스템?  -by 천국- */
			pc.setKills(rs.getInt("PC_Kill")); //추가
			pc.setDeaths(rs.getInt("PC_Death"));
			/* Kill & Death 시스템?  -by 천국- */
			/* 킬데스 초기화 - 병훈 */
			pc.set_KillDeathInitialize(rs.getInt("KillDeathInitialize"));
			// 시장저장
			pc.set_ment1(rs.getString("ment1"));
			pc.set_ment2(rs.getString("ment2"));
			//좀비모드용 쿼리문추가
			pc.setZombieMod(rs.getInt("ZombieMod"));
			pc.setZombieHp(rs.getInt("ZombieHp"));
			pc.setBackHp(rs.getInt("BackHp"));
			pc.setKillPoint(rs.getInt("KillPoint"));
			//좀비모드용 쿼리문추가	
			pc.set_QuizResult(rs.getInt("QuizResult")); // 퀴즈
			pc.refresh();
			pc.getMoveState().setMoveSpeed(0);
			pc.getMoveState().setBraveSpeed(0);
			pc.setGmInvis(false);

			_log.finest("restored char data: ");
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return pc;
	}

	@Override
	public void createCharacter(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			int i = 0;
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO characters SET account_name=?,objid=?,char_name=?,level=?,HighLevel=?,Exp=?,MaxHp=?,CurHp=?,MaxMp=?,CurMp=?,Ac=?,Str=?,BaseStr=?,Con=?,BaseCon=?,Dex=?,BaseDex=?,Cha=?,BaseCha=?,Intel=?,BaseIntel=?,Wis=?,BaseWis=?,Status=?,Class=?,Sex=?,Type=?,Heading=?,LocX=?,LocY=?,MapID=?,Food=?,Lawful=?,Title=?,ClanID=?,Clanname=?,ClanRank=?,BonusStatus=?,ElixirStatus=?,ElfAttr=?,PKcount=?,ExpRes=?,PartnerID=?,AccessLevel=?,OnlineStatus=?,HomeTownID=?,Contribution=?,Pay=?,HellTime=?,Banned=?,Karma=?,Age=?, UserName=?, LastPk=?,DeleteTime=?,ReturnStat=?,GdungeonTime=?,IvoryTowerTime=?,LdungeonTime=?,Ainhasad_Exp=?,Logout_time=?, BirthDay=?, ZombieMod=?, ZombieHp=?,BackHp=?,KillPoint=?");
			pstm.setString(++i, pc.getAccountName());
			pstm.setInt(++i, pc.getId());
			pstm.setString(++i, pc.getName());
			pstm.setInt(++i, pc.getLevel());
			pstm.setInt(++i, pc.getHighLevel());
			pstm.setInt(++i, pc.getExp());
			pstm.setInt(++i, pc.getBaseMaxHp());
			int hp = pc.getCurrentHp();
			if (hp < 1) {
				hp = 1;
			}
			pstm.setInt(++i, hp);
			pstm.setInt(++i, pc.getBaseMaxMp());
			pstm.setInt(++i, pc.getCurrentMp());
			pstm.setInt(++i, pc.getAC().getAc());
			pstm.setInt(++i, pc.getAbility().getStr());
			pstm.setInt(++i, pc.getAbility().getBaseStr());
			pstm.setInt(++i, pc.getAbility().getCon());
			pstm.setInt(++i, pc.getAbility().getBaseCon());
			pstm.setInt(++i, pc.getAbility().getDex());
			pstm.setInt(++i, pc.getAbility().getBaseDex());
			pstm.setInt(++i, pc.getAbility().getCha());
			pstm.setInt(++i, pc.getAbility().getBaseCha());
			pstm.setInt(++i, pc.getAbility().getInt());
			pstm.setInt(++i, pc.getAbility().getBaseInt());
			pstm.setInt(++i, pc.getAbility().getWis());
			pstm.setInt(++i, pc.getAbility().getBaseWis());
			pstm.setInt(++i, pc.getCurrentWeapon());
			pstm.setInt(++i, pc.getClassId());
			pstm.setInt(++i, pc.get_sex());
			pstm.setInt(++i, pc.getType());
			pstm.setInt(++i, pc.getMoveState().getHeading());
			pstm.setInt(++i, pc.getX());
			pstm.setInt(++i, pc.getY());
			pstm.setInt(++i, pc.getMapId());
			pstm.setInt(++i, pc.get_food());
			pstm.setInt(++i, pc.getLawful());
			pstm.setString(++i, pc.getTitle());
			pstm.setInt(++i, pc.getClanid());
			pstm.setString(++i, pc.getClanname());
			pstm.setInt(++i, pc.getClanRank());
			pstm.setInt(++i, pc.getAbility().getBonusAbility());
			pstm.setInt(++i, pc.getAbility().getElixirCount());
			pstm.setInt(++i, pc.getElfAttr());
			pstm.setInt(++i, pc.get_PKcount());
			pstm.setInt(++i, pc.getExpRes());
			pstm.setInt(++i, pc.getPartnerId());
			pstm.setShort(++i, pc.getAccessLevel());
			pstm.setInt(++i, pc.getOnlineStatus());
			pstm.setInt(++i, pc.getHomeTownId());
			pstm.setInt(++i, pc.getContribution());
			pstm.setInt(++i, 0);
			pstm.setInt(++i, pc.getHellTime());
			pstm.setBoolean(++i, pc.isBanned());
			pstm.setInt(++i, pc.getKarma());
			pstm.setInt(++i, pc.getAge());
			pstm.setString(++i, pc.getUserName());
			pstm.setTimestamp(++i, pc.getLastPk());
			pstm.setTimestamp(++i, pc.getDeleteTime());
			pstm.setInt(++i, pc.getReturnStat());
			pstm.setInt(++i, pc.getGdungeonTime());
			pstm.setInt(++i, pc.getIvoryTowerTime());
			pstm.setInt(++i, pc.getLdungeonTime());
			pstm.setInt(++i, pc.getAinHasad());
			pstm.setTimestamp(++i, pc.getLogOutTime());
			pstm.setInt(++i, pc.getBirthDay());
			//좀비모드용 쿼리문추가
			pstm.setInt(++i, pc.getZombieMod());
			pstm.setInt(++i, pc.getZombieHp());
			pstm.setInt(++i, pc.getBackHp());
			pstm.setInt(++i, pc.getKillPoint());
			//좀비모드용 쿼리문추가
			pstm.execute();

			_log.finest("stored char data: " + pc.getName());
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	@Override
	public void deleteCharacter(String accountName, String charName) throws Exception {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM characters WHERE account_name=? AND char_name=?");
			pstm.setString(1, accountName);
			pstm.setString(2, charName);
			rs = pstm.executeQuery();
			if (!rs.next()) {
				/*
				 * SELECT가 값을 돌려주지 않았다
				 * 존재하지 않는지, 혹은 다른 어카운트가 소유하고 있는 캐릭터명이 지정되었다고 하는 것이 된다.
				 */
				_log.warning("invalid delete char request: account=" + accountName + " char=" + charName);
				throw new RuntimeException("could not delete character");
			}

			pstm = con.prepareStatement("DELETE FROM character_buddys WHERE char_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM character_buff WHERE char_obj_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM character_config WHERE object_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM character_items WHERE char_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM character_quests WHERE char_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM character_teleport WHERE char_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM character_soldier WHERE char_id IN (SELECT objid FROM characters WHERE char_name = ?)");
			pstm.setString(1, charName);
			pstm.execute();
			pstm = con.prepareStatement("DELETE FROM characters WHERE char_name=?");
			pstm.setString(1, charName);
			pstm.execute();

		} catch (SQLException e) {
			throw e;
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	@Override
	public void storeCharacter(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			int i = 0;
			con = L1DatabaseFactory.getInstance().getConnection();

			// 상점 저장
			if(Config.autoTrader){
				pstm = con.prepareStatement("UPDATE characters SET level=?,HighLevel=?,Exp=?,MaxHp=?,CurHp=?,MaxMp=?,CurMp=?,Ac=?,Str=?,BaseStr=?,Con=?,BaseCon=?,Dex=?,BaseDex=?,Cha=?,BaseCha=?,Intel=?,BaseIntel=?,Wis=?,BaseWis=?,Status=?,Class=?,Sex=?,Type=?,Heading=?,LocX=?,LocY=?,MapID=?,Food=?,Lawful=?,Title=?,ClanID=?,Clanname=?,ClanRank=?,BonusStatus=?,ElixirStatus=?,ElfAttr=?,PKcount=?,ExpRes=?,PartnerID=?,AccessLevel=?,OnlineStatus=?,HomeTownID=?,Contribution=?,HellTime=?,Banned=?,Karma=?,Age=?,UserName=?,LastPk=?,DeleteTime=?,ReturnStat=?,GdungeonTime=?,IvoryTowerTime=?,LdungeonTime=?,Ainhasad_Exp=?,Logout_time=?,HuntPrice=?, HuntCount=?, HuntText=?, PC_Kill=?, PC_Death=?, KillDeathInitialize=?,trader=?, ment1=?, ment2=?, ZombieMod=?, ZombieHp=?,BackHp=?,KillPoint=?, QuizResult=? WHERE objid=?");				
			}else{
				pstm = con.prepareStatement("UPDATE characters SET level=?,HighLevel=?,Exp=?,MaxHp=?,CurHp=?,MaxMp=?,CurMp=?,Ac=?,Str=?,BaseStr=?,Con=?,BaseCon=?,Dex=?,BaseDex=?,Cha=?,BaseCha=?,Intel=?,BaseIntel=?,Wis=?,BaseWis=?,Status=?,Class=?,Sex=?,Type=?,Heading=?,LocX=?,LocY=?,MapID=?,Food=?,Lawful=?,Title=?,ClanID=?,Clanname=?,ClanRank=?,BonusStatus=?,ElixirStatus=?,ElfAttr=?,PKcount=?,ExpRes=?,PartnerID=?,AccessLevel=?,OnlineStatus=?,HomeTownID=?,Contribution=?,HellTime=?,Banned=?,Karma=?,Age=?,UserName=?,LastPk=?,DeleteTime=?,ReturnStat=?,GdungeonTime=?,IvoryTowerTime=?,LdungeonTime=?,Ainhasad_Exp=?,Logout_time=?,HuntPrice=?, HuntCount=?, HuntText=?, PC_Kill=?, PC_Death=?, KillDeathInitialize=? ,ZombieMod=?, ZombieHp=?,BackHp=?,KillPoint=?, QuizResult=? WHERE objid=?");
			}
			pstm.setInt(++i, pc.getLevel());
			pstm.setInt(++i, pc.getHighLevel());
			pstm.setInt(++i, pc.getExp());
			pstm.setInt(++i, pc.getBaseMaxHp());
			int hp = pc.getCurrentHp();
			if (hp < 1) {
				hp = 1;
			}
			pstm.setInt(++i, hp);
			pstm.setInt(++i, pc.getBaseMaxMp());
			pstm.setInt(++i, pc.getCurrentMp());
			pstm.setInt(++i, pc.getAC().getAc());
			pstm.setInt(++i, pc.getAbility().getStr());
			pstm.setInt(++i, pc.getAbility().getBaseStr());
			pstm.setInt(++i, pc.getAbility().getCon());
			pstm.setInt(++i, pc.getAbility().getBaseCon());
			pstm.setInt(++i, pc.getAbility().getDex());
			pstm.setInt(++i, pc.getAbility().getBaseDex());
			pstm.setInt(++i, pc.getAbility().getCha());
			pstm.setInt(++i, pc.getAbility().getBaseCha());
			pstm.setInt(++i, pc.getAbility().getInt());
			pstm.setInt(++i, pc.getAbility().getBaseInt());
			pstm.setInt(++i, pc.getAbility().getWis());
			pstm.setInt(++i, pc.getAbility().getBaseWis());
			pstm.setInt(++i, pc.getCurrentWeapon());
			pstm.setInt(++i, pc.getClassId());
			pstm.setInt(++i, pc.get_sex());
			pstm.setInt(++i, pc.getType());
			pstm.setInt(++i, pc.getMoveState().getHeading());
			pstm.setInt(++i, pc.getX());
			pstm.setInt(++i, pc.getY());
			pstm.setInt(++i, pc.getMapId());
			pstm.setInt(++i, pc.get_food());
			pstm.setInt(++i, pc.getLawful());
			pstm.setString(++i, pc.getTitle());
			pstm.setInt(++i, pc.getClanid());
			pstm.setString(++i, pc.getClanname());
			pstm.setInt(++i, pc.getClanRank());
			pstm.setInt(++i, pc.getAbility().getBonusAbility());
			pstm.setInt(++i, pc.getAbility().getElixirCount());
			pstm.setInt(++i, pc.getElfAttr());
			pstm.setInt(++i, pc.get_PKcount());
			pstm.setInt(++i, pc.getExpRes());
			pstm.setInt(++i, pc.getPartnerId());
			pstm.setShort(++i, pc.getAccessLevel());
			pstm.setInt(++i, pc.getOnlineStatus());
			pstm.setInt(++i, pc.getHomeTownId());
			pstm.setInt(++i, pc.getContribution());
			pstm.setInt(++i, pc.getHellTime());
			pstm.setBoolean(++i, pc.isBanned());
			pstm.setInt(++i, pc.getKarma());
			pstm.setInt(++i, pc.getAge());  
			pstm.setString(++i, pc.getUserName());
			pstm.setTimestamp(++i, pc.getLastPk());
			pstm.setTimestamp(++i, pc.getDeleteTime());
			pstm.setInt(++i, pc.getReturnStat());
			pstm.setInt(++i, pc.getGdungeonTime());
			pstm.setInt(++i, pc.getIvoryTowerTime());
			pstm.setInt(++i, pc.getLdungeonTime());
			pstm.setInt(++i, pc.getAinHasad());
			pstm.setTimestamp(++i, pc.getLogOutTime());
			pstm.setInt(++i, pc.getHuntPrice());
			pstm.setInt(++i, pc.getHuntCount());
			pstm.setString(++i, pc.getReasonToHunt());			
			pstm.setInt(++i, pc.getKills());
			pstm.setInt(++i, pc.getDeaths());	
			pstm.setInt(++i, pc.get_KillDeathInitialize());  // 킬데스초기화 적용
			// 상점 저장
			if(Config.autoTrader){
				if(pc.isPrivateShop()){
					pstm.setInt(++i, 1);
					pstm.setString(++i, pc.get_ment1());
					pstm.setString(++i, pc.get_ment2());
				}else{
					pstm.setInt(++i, 0);
					pstm.setString(++i, null);
					pstm.setString(++i, null);
				}
			}
			//좀비모드용 쿼리문추가
			pstm.setInt(++i, pc.getZombieMod());
			pstm.setInt(++i, pc.getZombieHp());
			pstm.setInt(++i, pc.getBackHp());
			pstm.setInt(++i, pc.getKillPoint());
			//좀비모드용 쿼리문추가
			pstm.setInt(++i, pc.get_QuizResult());
			pstm.setInt(++i, pc.getId());
			pstm.execute();

			// 상점 저장 - 아이템 
			if(Config.autoTrader && pc.isPrivateShop()) saveTraderItems(pc);
			//
			_log.finest("stored char data:" + pc.getName());
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	// 상점 저장
	private void saveTraderItems(L1PcInstance cha){
		Connection c = null;
		PreparedStatement p = null;
		PreparedStatement pp = null;
		int idx = 0;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();

			try{
				// 기존꺼 삭제.
				pp = c.prepareStatement("delete from character_traderitems where char_objectid=?");
				pp.setInt(1, cha.getId());
				pp.execute();
				pp.close();
			}catch(Exception ee){}
			// sell 목록
			ArrayList<L1PrivateShopSellList> sellList = cha.getSellList();
			L1PrivateShopSellList sellItem = null;
			for(int i = 0; i < sellList.size(); i++){
				idx = 1;
				try{
					sellItem = sellList.get(i);
					if(sellItem == null) continue;

					if(sellItem.getSellTotalCount() - sellItem.getSellCount() <= 0) continue;

					p = c.prepareStatement("insert into character_traderitems set char_objectid=?, type=0, item_objectid=?, price=?, count=?");
					p.setInt(idx++, cha.getId());
					p.setInt(idx++, sellItem.getItemObjectId());
					p.setInt(idx++, sellItem.getSellPrice());
					p.setInt(idx++, sellItem.getSellTotalCount() - sellItem.getSellCount());					
					p.execute();
					SQLUtil.close(p);

				}catch(Exception ee){}
			}
			// buy 목록
			ArrayList<L1PrivateShopBuyList> buyList = cha.getBuyList();
			L1PrivateShopBuyList buyItem = null;
			for(int i = 0; i < buyList.size(); i++){
				idx = 1;
				try{
					buyItem = buyList.get(i);
					if(buyItem == null) continue;

					if(buyItem.getBuyTotalCount() - buyItem.getBuyCount() <= 0) continue;

					p = c.prepareStatement("insert into character_traderitems set char_objectid=?, type=1, item_objectid=?, price=?, count=?");
					p.setInt(idx++, cha.getId());
					p.setInt(idx++, buyItem.getItemObjectId());
					p.setInt(idx++, buyItem.getBuyPrice());
					p.setInt(idx++, buyItem.getBuyTotalCount() - buyItem.getBuyCount());					
					p.execute();
					SQLUtil.close(p);

				}catch(Exception ee){}
			}			
		}catch(Exception e){
		}finally{
			try{
				SQLUtil.close(c);
			}catch(Exception e){}
		}
	}
	//
}