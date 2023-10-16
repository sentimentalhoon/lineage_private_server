package l1j.server.server.command.executor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import l1j.server.Config;
import l1j.server.server.BadNamesList;
import l1j.server.server.ObjectIdFactory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1RobotInstance;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.CommonUtil;
import server.threads.pc.RobotThread;

public class L1Robot3 implements L1CommandExecutor {

	private static Random _random =  new Random(System.nanoTime());

	private static final int[] MALE_LIST = new int[] { 0, 61, 138, 734, 2786, 6658, 6671 };
	private static final int[] FEMALE_LIST = new int[] { 1, 48, 37, 1186, 2796, 6661, 6650 };
	//private static final int[] WEAPON_LIST = new int[] { 41, 172, 125, 80, 52, 410003 };

	/** 로봇을 담기 위한 리스트 */
	public static final ArrayList<L1RobotInstance> RobotUserList1 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList2 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList3 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList4 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList5 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList6 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList7 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList8 = new ArrayList<L1RobotInstance>();
	public static final ArrayList<L1RobotInstance> RobotUserList9 = new ArrayList<L1RobotInstance>();
	/**
	 * 선착순 20명 등록
	 */
	public synchronized void add(L1RobotInstance pc, int type){
		switch(type){
		case 1:	RobotUserList1.add(pc); break;
		case 2:	RobotUserList2.add(pc); break;
		case 3:	RobotUserList3.add(pc); break;
		case 4:	RobotUserList4.add(pc); break;
		case 5:	RobotUserList5.add(pc); break;
		case 6:	RobotUserList6.add(pc); break;
		case 7:	RobotUserList7.add(pc); break;
		case 8:	RobotUserList8.add(pc); break;
		case 9:	RobotUserList9.add(pc); break;
		}
	}

	private L1Robot3() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Robot3();
	}

	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer tok = new StringTokenizer(arg);			
			int robot = Integer.parseInt(tok.nextToken());
			int count = Integer.parseInt(tok.nextToken());
			//int team = Integer.parseInt(tok.nextToken());
			
			int SearchCount = 0;

			L1Map map = pc.getMap();

			int x = 0;
			int y = 0;

			int[] loc = { -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8 };

			pc.sendPackets(new S_SystemMessage("----------------------------------------------------"));
			while(count-- > 0){
				String name = RobotThread.getName();
				if(name == null){
					pc.sendPackets(new S_SystemMessage( "더이상 생성할 이름이 존재하지않습니다." ));
					RobotThread.list_name_idx = 0;
					return;
				}

				L1PcInstance player = L1World.getInstance().getPlayer(name);

				if (player != null) { continue;	}

				L1RobotInstance robotPc = new L1RobotInstance();
				robotPc.setAccountName("");
				robotPc.setId(ObjectIdFactory.getInstance().nextId());
				robotPc.setName(name);
				robotPc.setHighLevel(1);
				robotPc.setExp(0);
				if (robot == 0) { // 텔레포트 안하는 신규 로보트 생성(뉴트럴, 호칭 있슴)
					robotPc.setLawful(0);
				} else if (robot == 1){ // 텔레포트 하는 신규 로보트 생성(라우풀, 호칭 없슴)
					robotPc.setLawful(32767);
				} else if (robot == 2){ // 텔레포트 하는 신규 로보트 생성(카오, 호칭 없슴)
					robotPc.setLawful(-20000);
				}
				
				int classId = 0;
				int classSex = CommonUtil.random(0, 1);
				int classType = CommonUtil.random(MALE_LIST.length);
				switch (classSex) {
				case 0:
					classId = MALE_LIST[classType];
					break;
				case 1:
					classId = FEMALE_LIST[classType];
					break;
				}
				robotPc.setClassId(classId);
				robotPc.setType(classType);	// 0~6
				robotPc.set_sex(classSex);	// 0~1
				robotPc.getGfxId().setTempCharGfx(classId);
				robotPc.getGfxId().setGfxId(classId);
				
				robotPc.addBaseMaxHp((short)1000);
				robotPc.setCurrentHp(1000);
				robotPc.setDead(false);
				robotPc.addBaseMaxMp((short)2);
				robotPc.setCurrentMp(2);
				robotPc.getResistance().addMr(120);
				robotPc.getAbility().setBaseStr(16);
				robotPc.getAbility().setStr(16);
				robotPc.getAbility().setBaseCon(16);
				robotPc.getAbility().setCon(16);
				robotPc.getAbility().setBaseDex(11);
				robotPc.getAbility().setDex(11);
				robotPc.getAbility().setBaseCha(13);
				robotPc.getAbility().setCha(13);
				robotPc.getAbility().setBaseInt(12);
				robotPc.getAbility().setInt(12);
				robotPc.getAbility().setBaseWis(11);
				robotPc.getAbility().setWis(11);
				// robotPc.setCurrentWeapon(0);
				
				while (true) {
					x = loc[_random.nextInt(17)];
					y = loc[_random.nextInt(17)];
					robotPc.setX(pc.getX() + x);
					robotPc.setY(pc.getY() + y);
					robotPc.setMap(pc.getMapId());
					if (map.isPassable(robotPc.getX(), robotPc.getY())) {
						break;
					}
				}	

				robotPc.getMoveState().setHeading(CommonUtil.random(0, 7));
				robotPc.set_food(39);
				int clanrandom = CommonUtil.random(3);
				switch(clanrandom){
				case 0: 
					robotPc.setClanid(Config.NewClan1); 
					robotPc.setClanname(Config.NewClanName1);
					robotPc.setTitle("["+ Config.servername + "]" + Config.NewClanName1);
					break;
				case 1: 
					robotPc.setClanid(Config.NewClan2); 
					robotPc.setClanname(Config.NewClanName2);
					robotPc.setTitle("["+ Config.servername + "]" + Config.NewClanName2);
					break;
				case 2: 
					robotPc.setClanid(Config.NewClan3); 
					robotPc.setClanname(Config.NewClanName3);
					robotPc.setTitle("["+ Config.servername + "]" + Config.NewClanName3);
					break;
				case 3: 
					robotPc.setClanid(Config.NewClan4); 
					robotPc.setClanname(Config.NewClanName4);
					robotPc.setTitle("["+ Config.servername + "]" + Config.NewClanName4);
					break;
				default:
					robotPc.setClanid(Config.NewClan1); 
					robotPc.setClanname(Config.NewClanName1);
					robotPc.setTitle("["+ Config.servername + "]" + Config.NewClanName1);
					break;
				}
				robotPc.setClanRank(0);
				robotPc.setElfAttr(0);
				robotPc.set_PKcount(0);
				robotPc.setExpRes(0);
				robotPc.setPartnerId(0);
				robotPc.setAccessLevel((short)0);
				robotPc.setGm(false);
				robotPc.setMonitor(true);
				//robotPc.setOnlineStatus(1);
				robotPc.setHomeTownId(0);
				robotPc.setContribution(0);
				robotPc.setHellTime(0);
				robotPc.setBanned(false);
				robotPc.setKarma(0);
				robotPc.setReturnStat(0);				
				//robotPc.refresh();
				robotPc.setGmInvis(false);
				//				L1ItemInstance item = ItemTable.getInstance().createItem(WEAPON_LIST[type]);
				//				robotPc.getInventory().storeItem(item);
				//				robotPc.getInventory().setEquipped(item, true);

				if (robotPc.isKnight()) {
					robotPc.setCurrentWeapon(50);
				} else if (robotPc.isCrown()) {
					robotPc.setCurrentWeapon(4);
				} else if (robotPc.isElf()) {
					robotPc.setCurrentWeapon(20);
				} else if (robotPc.isWizard()) {
					robotPc.setCurrentWeapon(40);
				} else if (robotPc.isDarkelf()) {
					robotPc.setCurrentWeapon(54);
				} else if (robotPc.isIllusionist()) {
					robotPc.setCurrentWeapon(40);
				} else if (robotPc.isDragonknight()) {
					robotPc.setCurrentWeapon(50);
				}	
				
				int noplayerck = CommonUtil.random(9);
				switch(noplayerck){
				case 1: add(robotPc, 1); break;
				case 2: add(robotPc, 2); break;
				case 3: add(robotPc, 3); break;
				case 4: add(robotPc, 4); break;
				case 5: add(robotPc, 5); break;
				case 6: add(robotPc, 6); break;
				case 7: add(robotPc, 7); break;
				case 8:	add(robotPc, 8); break;
				case 9: add(robotPc, 9); break;
				default: add(robotPc, 1);	break;
				}
				RobotThread.append(robotPc);
				robotPc.noPlayerCK = true;
				robotPc.setActionStatus(0);				
				L1World.getInstance().storeObject(robotPc);
				L1World.getInstance().addVisibleObject(robotPc);
				robotPc.setNetConnection(null);
				SearchCount++;
			}
			pc.sendPackets(new S_SystemMessage(SearchCount + "명의 허상 캐릭터가 배치 되었습니다."));
			pc.sendPackets(new S_SystemMessage("----------------------------------------------------"));
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("----------------------------------------------------"));
			pc.sendPackets(new S_SystemMessage(" type - 0:Neutral, 1:Lawful, 2:Chaotic"));
			pc.sendPackets(new S_SystemMessage(" 팀 - 1~5: 일반팀 6~9 : 낚시팀"));
			pc.sendPackets(new S_SystemMessage(".허상 (type) (수) (팀)"));
			pc.sendPackets(new S_SystemMessage("----------------------------------------------------"));
		}
	}

	private static boolean isAlphaNumeric(String s) {
		boolean flag = true;
		char ac[] = s.toCharArray();
		int i = 0;
		do {
			if (i >= ac.length) {
				break;
			}
			if (!Character.isLetterOrDigit(ac[i])) {
				flag = false;
				break;
			}
			i++;
		} while (true);
		return flag;
	}

	private static boolean isInvalidName(String name) {
		int numOfNameBytes = 0;
		try {
			numOfNameBytes = name.getBytes("EUC-KR").length;
		} catch (UnsupportedEncodingException e) {
			return false;
		}

		if (isAlphaNumeric(name)) {
			return false;
		}

		if (5 < (numOfNameBytes - name.length()) || 12 < numOfNameBytes) {
			return false;
		}

		if (BadNamesList.getInstance().isBadName(name)) {
			return false;
		}
		return true;
	}
}