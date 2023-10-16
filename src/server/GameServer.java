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
package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.GameSystem.CrockSystem;
import l1j.server.GameSystem.HomeTownController;
import l1j.server.GameSystem.NoticeSystem;
import l1j.server.GameSystem.NpcShopSystem;
import l1j.server.GameSystem.Lastabard.LastabardController;
import l1j.server.server.Announcecycle;
import l1j.server.server.AutoTraderThread;
import l1j.server.server.BIRTHDAYController;
import l1j.server.server.ChatTimeController;
import l1j.server.server.CheckLevelController;
import l1j.server.server.DevilController;
import l1j.server.server.GMCommandsConfig;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.LoginAuth;
import l1j.server.server.Lotto;
import l1j.server.server.ObjectIdFactory;
import l1j.server.server.OxTimeController;
import l1j.server.server.Shutdown;
import l1j.server.server.SkyCastleController;
import l1j.server.server.Web;
import l1j.server.server.TimeController.AuctionTimeController;
import l1j.server.server.TimeController.FishingTimeController;
import l1j.server.server.TimeController.HouseTaxTimeController;
import l1j.server.server.TimeController.LightTimeController;
import l1j.server.server.TimeController.NpcChatTimeController;
import l1j.server.server.TimeController.RobotTimeController;
import l1j.server.server.TimeController.UbTimeController;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.datatables.AutoTraderTable;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.CheckWarTime;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.datatables.DoorSpawnTable;
import l1j.server.server.datatables.DropItemTable;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.EvaSystemTable;
import l1j.server.server.datatables.FurnitureSpawnTable;
import l1j.server.server.datatables.GetBackRestartTable;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.LightSpawnTable;
import l1j.server.server.datatables.MapsTable;
import l1j.server.server.datatables.MobGroupTable;
import l1j.server.server.datatables.ModelSpawnTable;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.datatables.NpcActionTable;
import l1j.server.server.datatables.NpcChatTable;
import l1j.server.server.datatables.NpcShopTable;
import l1j.server.server.datatables.NpcSpawnTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.QuizBoardTable;
import l1j.server.server.datatables.RaceTable;
import l1j.server.server.datatables.ResolventTable;
import l1j.server.server.datatables.RobotTable;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.datatables.SoldierTable;
import l1j.server.server.datatables.SpawnTable;
import l1j.server.server.datatables.SprTable;
import l1j.server.server.datatables.UBSpawnTable;
import l1j.server.server.datatables.UserSpeedControlTable;
import l1j.server.server.model.Dungeon;
import l1j.server.server.model.ElementalStoneGenerator;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1BugBearRace;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Cube;
import l1j.server.server.model.L1DeleteItemOnGround;
import l1j.server.server.model.L1NpcRegenerationTimer;
import l1j.server.server.model.L1Sys;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.GameTimeClock;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.item.L1TreasureBox;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.utils.SQLUtil;
import server.controller.ItemDeleteController;
import server.controller.LogDelController;
import server.manager.eva;
import server.threads.manager.DecoderManager;
import server.threads.pc.AutoSaveThread;
import server.threads.pc.CharacterQuickCheckThread;
import server.threads.pc.DollObserverThread;
import server.threads.pc.HpMpRegenThread;
import server.threads.pc.ItemEndTimeCheckThread;
import server.threads.pc.MiniMapThread;
import server.threads.pc.PremiumAinThread;
import server.threads.pc.RobotThread;


public class GameServer/* extends Thread*/ {
	private static Logger _log = Logger.getLogger(GameServer.class.getName());
	private static GameServer _instance;

	public static boolean _complated;

	private GameServer() {
		//super("GameServer");
	}

	public static GameServer getInstance() {
		if (_instance == null) {
			synchronized (GameServer.class) {
				if(_instance == null)
					_instance = new GameServer();
			}
		}
		return _instance;
	}

	public void initialize() throws Exception {
		showGameServerSetting();

		ObjectIdFactory.createInstance();
		L1WorldMap.createInstance();  // FIXME 부실하다

		initTime();

		CharacterTable.getInstance().loadAllCharName(); // FIXME 굳이 메모리에 띄워둘 필요가 있나
		CharacterTable.clearOnlineStatus();

		// TODO change following code to be more effective  

		// UB타임 콘트롤러
		GeneralThreadPool.getInstance().execute(UbTimeController.getInstance());
		

		// 전쟁 타임 콘트롤러
		GeneralThreadPool.getInstance().execute(WarTimeController.getInstance());

		// 정령의 돌 타임 컨트롤러
		if (Config.ELEMENTAL_STONE_AMOUNT > 0) {
			GeneralThreadPool.getInstance().execute(ElementalStoneGenerator.getInstance());
		}

		// 홈 타운
		HomeTownController.getInstance();
		SkyCastleController.getInstance().start(); 
		//CrockController.getInstance().start(); 
		NpcShopTable.getInstance();
		NpcShopSystem.getInstance();		
		BIRTHDAYController.getInstance().start();		

		//GidunController.getInstance().start(); 

		/**로그 클리어*/
		LogDelController mldc = new LogDelController(3600000); //3600000 1시간
		mldc.start();
		/**로그 클리어*/

		// 아지트 경매 타임 콘트롤러
		GeneralThreadPool.getInstance().execute(AuctionTimeController.getInstance());

		if(Config.AUTH_CONNECT) {
			LoginAuth.InitialAuthStatus();
		}
		// A101 허상 로봇 추가
		RobotTable.getInstance();
		RobotThread.init(); // 허상 로봇
		// A101 허상 로봇 추가
		// 퀴즈 문제
		QuizBoardTable.getInstance();
		// 무인PC 컨트롤러
		RobotTimeController robotTimeController = RobotTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(robotTimeController);
		//레벨검사 써니
		CheckLevelController checkLevelController = CheckLevelController.getInstance();
		GeneralThreadPool.getInstance().execute(checkLevelController);

		//전랭1위 자동 멘트 써니
		ChatTimeController chatTimeController = ChatTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(chatTimeController);

		// 시간의 균열 컨트롤러
		//TimeCrockController timecrockcontroller = TimeCrockController.getInstance();
		//GeneralThreadPool.getInstance().execute(timecrockcontroller);

		// 아지트 세금 타임 콘트롤러
		GeneralThreadPool.getInstance().execute(HouseTaxTimeController.getInstance());

		// A108 Ox배틀존 타임 콘트롤러
		OxTimeController oxTimeController = OxTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(oxTimeController);

		// 낚시 타임 콘트롤러
		GeneralThreadPool.getInstance().execute(FishingTimeController.getInstance());

		GeneralThreadPool.getInstance().execute(NpcChatTimeController.getInstance());

		NpcTable.getInstance();
		L1DeleteItemOnGround deleteitem = new L1DeleteItemOnGround();
		deleteitem.initialize();

		if (!NpcTable.getInstance().isInitialized()) {
			throw new Exception("[GameServer] Could not initialize the npc table");
		}

		SpawnTable.getInstance();
		MobGroupTable.getInstance();
		SkillsTable.getInstance();
		PolyTable.getInstance();
		ItemTable.getInstance();
		DropTable.getInstance();
		DropItemTable.getInstance();
		ShopTable.getInstance();
		NPCTalkDataTable.getInstance();
		L1World.getInstance();
		L1WorldTraps.getInstance();
		Dungeon.getInstance();
		NpcSpawnTable.getInstance();
		IpTable.getInstance();
		MapsTable.getInstance();
		UBSpawnTable.getInstance();
		PetTable.getInstance();
		ClanTable.getInstance();
		ClanTable.getInstance().CleanAlliances();
		CastleTable.getInstance();
		L1CastleLocation.setCastleTaxRate(); // CastleTable 초기화 다음 아니면 안 된다
		CheckWarTime.getInstance();
		GetBackRestartTable.getInstance();
		DoorSpawnTable.getInstance();
		GeneralThreadPool.getInstance();
		L1NpcRegenerationTimer.getInstance();
		//		ChatLogTable.getInstance();
		//		WeaponSkillTable.getInstance();
		NpcActionTable.load();
		GMCommandsConfig.load();
		Getback.loadGetBack();
		PetTypeTable.load();
		L1TreasureBox.load();
		SprTable.getInstance();
		RaceTable.getInstance();
		ResolventTable.getInstance();
		FurnitureSpawnTable.getInstance();
		NpcChatTable.getInstance();
		L1Cube.getInstance();
		SoldierTable.getInstance();
		L1BugBearRace.getInstance();	
		/**
		 * 로또 시스템
		 */
		//Lotto.getInstance().start();
		//Web.getInstance();

		// 라스타바드 던전 
		LastabardController.start();

		// 시장저장
		AutoTraderTable.getInstance();
		AutoTraderThread.getInstance();
		//

		// 유령의집, 데스매치
		//GeneralThreadPool.getInstance().execute(DeathMatch.getInstance());
		//GeneralThreadPool.getInstance().execute(GhostHouse.getInstance());
		//GeneralThreadPool.getInstance().execute(PetRacing.getInstance());

		// 횃불
		LightSpawnTable.getInstance();
		LightTimeController.start();

		// 월드내에 모형 넣기(던전내 횟불 등등) 
		ModelSpawnTable.getInstance().ModelInsertWorld();

		// 공성 시간지정 타이머
		//		WarSetTime.start();

		//# 자동 공지사항..
		if (Config.Use_Show_Announcecycle) {
			Announcecycle.getInstance();
		}

		// 게임 공지 
		NoticeSystem.start();
		
		// 공지 시스템
		/*L1Sys.getInstance();  
		L1Sys l1Sys = L1Sys.getInstance();                         //추가
		GeneralThreadPool.getInstance().execute(l1Sys);     */                                 

		//DevilController.getInstance().start(); // 악마왕
		//HalloweenController.getInstance().start();  //할로윈사냥터

		// 시간의 균열
		EvaSystemTable.getInstance();
		CrockSystem.getInstance();

		//		if (Config.ALT_HALLOWEENEVENT != true) {
		//			Halloween();
		//		}

		// 버경표 삭제
		RaceTicket();
		//		MapFixKeyTable.getInstance();

		//		MiniClient Mini = MiniClient.getInstance();
		//		Mini.start();
		//케릭터 자동저장 스케줄러 해당 시간에 맞게 전체 유저를 읽어서 저장시킨다.
		//CharacterAutoSaveController chaSave = new CharacterAutoSaveController(Config.AUTOSAVE_INTERVAL * 1000);
		//chaSave.start();

		//CharacterQuitCheckController quick = new CharacterQuitCheckController(10000);
		//quick.start();
		//케릭터가 가진 인형의 Action을 전송한다.
		//DollobserverController dollAction = new DollobserverController(15000);
		//dollAction.start();

		//HpMpRegenController regen = new HpMpRegenController(1000);
		//regen.start();
		AutoSaveThread.getInstance();
		DollObserverThread.getInstance();
		HpMpRegenThread.getInstance();
		//SpeedHackThread.getInstance(); // * A110 사용 안함
		ItemEndTimeCheckThread.getInstance(); // 추가 해줍니다.
		MiniMapThread.getInstance();   
		PremiumAinThread.getInstance();
		CharacterQuickCheckThread.getInstance();
		//AutoUpdateThread.getInstance();
		//ExpMonitorThread.getInstance();

		ItemDeleteController idel = new ItemDeleteController(60000);
		idel.start();

		DecoderManager.getInstance();
		//GCThread.getInstance();

		UserSpeedControlTable.getInstance();

		// 가비지 컬렉터 실행 (Null) 객체의 해제
		System.out.println("[GameServer] 로딩 완료!");
		System.out.println("=================================================");
		//eva.refreshMemory();
		// 시장 저장
		_complated = true;
		//
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		//this.start();
	}

	private void initTime() {
		GameTimeClock.init(); // 게임 시간 시계
		RealTimeClock.init(); // 현재 시간 시계
	}

	private void showGameServerSetting() {
		System.out.println("[GameServer] Starting GameServer"); // (" + serverSocket.toString() + ")");

		double rateXp			= Config.RATE_XP;
		double rateLawful 		= Config.RATE_LAWFUL; 
		double rateKarma 		= Config.RATE_KARMA;
		double rateDropItems 	= Config.RATE_DROP_ITEMS;
		double rateDropAdena 	= Config.RATE_DROP_ADENA;

		System.out.println("[GameServer] Exp: x" + rateXp + " / Lawful: x" + rateLawful + " / Adena: x" + rateDropAdena);
		System.out.println("[GameServer] Karma: x" + rateKarma + " / Item: x" + rateDropItems);
		System.out.println("[GameServer] Chatting Level: " + Config.GLOBAL_CHAT_LEVEL);
		System.out.println("[GameServer] Maximum User: " + Config.MAX_ONLINE_USERS + "인");

		System.out.print("[GameServer] PvP mode: ");
		if (Config.ALT_NONPVP) 	System.out.println("On");
		else 					System.out.println("Off");
		System.out.println("=================================================");
	}

	/**
	 * 온라인중의 플레이어 모두에 대해서 kick, 캐릭터 정보의 보존을 한다.
	 */
	public void disconnectAllCharacters() {
		Collection<L1PcInstance> players = L1World.getInstance().getAllPlayers();
		// 모든 캐릭터 끊기
		for (L1PcInstance pc : players) {
			if(pc == null) continue;	// pc 가 업을때.
			//if (pc.getNetConnection() != null) {
			try {
				pc.save();
				pc.saveInventory();
				//	System.out.println(pc.getName());
				//	System.out.println(1);
				pc.getNetConnection().setActiveChar(null);
				//	System.out.println(2);
				pc.getNetConnection().kick();
				//		System.out.println(3);
				pc.getNetConnection().quitGame(pc);
				//		System.out.println(4);
				L1World.getInstance().removeObject(pc);
				//		System.out.println(5);
			} catch (Exception e) {
				//System.out.println("disconnectallcharacters error");
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}

			//}
		}
		//players.clear();
	}

	public int saveAllCharInfo() {
		// exception 발생하면 -1 리턴, 아니면 저장한 인원 수 리턴
		int cnt = 0;
		try {
			for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
				cnt++;
				pc.save();
				pc.saveInventory();							
			}	
		}
		catch(Exception e) { return -1; }

		return cnt;
	}

	/**
	 * 온라인중의 플레이어에 대해서 kick , 캐릭터 정보의 보존을 한다.
	 */
	public void disconnectChar(String name) {
		L1PcInstance pc = L1World.getInstance().getPlayer(name);
		L1PcInstance Player = pc;
		synchronized (pc) {
			pc.getNetConnection().kick();
			Player.logout();
			pc.getNetConnection().quitGame(Player);
		}
	}

	private class ServerShutdownThread extends Thread {
		private final int _secondsCount;

		public ServerShutdownThread(int secondsCount) {
			_secondsCount = secondsCount;
		}

		@Override
		public void run() {
			L1World world = L1World.getInstance();
			try {
				int secondsCount = _secondsCount;
				System.out.println("[GameServer] 잠시 후, 서버를 종료 합니다.");
				System.out.println("[GameServer] 안전한 장소에서 로그아웃 해 주세요.");
				world.broadcastServerMessage("잠시 후, 서버를 종료 합니다.");
				world.broadcastServerMessage("안전한 장소에서 로그아웃 해 주세요.");
				while (0 < secondsCount) {
					if (secondsCount <= 30) {
						System.out.println("[GameServer] 게임이 " + secondsCount + "초 후에 종료 됩니다. 게임을 중단해 주세요.");
						world.broadcastServerMessage("게임이 " + secondsCount + "초 후에 종료 됩니다. 게임을 중단해 주세요.");
					} else {
						if (secondsCount % 60 == 0) {
							System.out.println("[GameServer] 게임이 " + secondsCount / 60 + "분 후에 종료 됩니다.");
							world.broadcastServerMessage("게임이 " + secondsCount / 60 + "분 후에 종료 됩니다.");
						}
					}
					Thread.sleep(1000);
					secondsCount--;
				}
				shutdown();
			} catch (InterruptedException e) {
				System.out.println("[GameServer] 서버 종료가 중단되었습니다. 서버는 정상 가동중입니다.");
				world.broadcastServerMessage("서버 종료가 중단되었습니다. 서버는 정상 가동중입니다.");
				return;
			}
		}
	}

	private ServerShutdownThread _shutdownThread = null;

	public synchronized void shutdownWithCountdown(int secondsCount) {
		if (_shutdownThread != null) {
			// 이미 슛다운 요구를 하고 있다
			// TODO 에러 통지가 필요할지도 모른다
			return;
		}
		_shutdownThread = new ServerShutdownThread(secondsCount);
		GeneralThreadPool.getInstance().execute(_shutdownThread);
	}

	/*public void shutdown() {
		disconnectAllCharacters();
		eva.savelog();
		System.exit(0);
            }*/
	public void shutdown() {
		if(_shutdownThread != null){
			disconnectAllCharacters();
			eva.savelog();
			System.exit(0);
		}
	}



	public synchronized void abortShutdown() {
		if (_shutdownThread == null) {
			// 슛다운 요구를 하지 않았다
			// TODO 에러 통지가 필요할지도 모른다
			return;
		}

		_shutdownThread.interrupt();
		_shutdownThread = null;
	}

	public void Halloween() {
		Connection con = null;
		PreparedStatement pstm = null;
		PreparedStatement pstm1 = null;
		PreparedStatement pstm2 = null;
		PreparedStatement pstm3 = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM character_items WHERE item_id IN (20380, 21060, 256) AND enchantlvl < 8");
			pstm1 = con.prepareStatement("DELETE FROM character_elf_warehouse WHERE item_id IN (20380, 21060, 256) AND enchantlvl < 8");
			pstm2 = con.prepareStatement("DELETE FROM clan_warehouse WHERE item_id IN (20380, 21060, 256) AND enchantlvl < 8");
			pstm3 = con.prepareStatement("DELETE FROM character_warehouse WHERE item_id IN (20380, 21060, 256) AND enchantlvl < 8");		
			pstm3.execute();
			pstm2.execute();
			pstm1.execute();
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(pstm1);
			SQLUtil.close(pstm2);
			SQLUtil.close(pstm3);			
			SQLUtil.close(con);
		}
	}

	public void RaceTicket() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();			
			pstm = con.prepareStatement("DELETE FROM character_items WHERE item_id = 40309");
			pstm.execute();			
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
