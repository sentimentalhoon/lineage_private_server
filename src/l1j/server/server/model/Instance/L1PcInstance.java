package l1j.server.server.model.Instance;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.SpecialEventHandler;
import l1j.server.Warehouse.ClanWarehouse;
import l1j.server.Warehouse.WarehouseManager;
import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.TimeController.BattleZoneController;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.command.executor.L1HpBar;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.datatables.UserSpeedControlTable;
import l1j.server.server.model.AHRegeneration;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.Beginner;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.HalloweenRegeneration;
import l1j.server.server.model.HpRegenerationByDoll;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1ChatParty;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1EquipmentSlot;
import l1j.server.server.model.L1ExcludingList;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Karma;
import l1j.server.server.model.L1Magic;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Party;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PinkName;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1TownLocation;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.MpDecreaseByScales;
import l1j.server.server.model.MpRegenerationByDoll;
import l1j.server.server.model.PapuBlessing;
import l1j.server.server.model.SHRegeneration;
import l1j.server.server.model.classes.L1ClassFeature;
import l1j.server.server.model.gametime.GameTimeCarrier;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.monitor.L1PcAutoUpdate;
import l1j.server.server.model.monitor.L1PcExpMonitor;
import l1j.server.server.model.monitor.L1PcGhostMonitor;
import l1j.server.server.model.monitor.L1PcHellMonitor;
import l1j.server.server.model.monitor.L1PcInvisDelay;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_BaphoIcon;
import l1j.server.server.serverpackets.S_BlueMessage;
import l1j.server.server.serverpackets.S_CastleMaster;
import l1j.server.server.serverpackets.S_ChangeShape;
import l1j.server.server.serverpackets.S_CharTitle;
import l1j.server.server.serverpackets.S_DelSkill;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_DoActionShop;
import l1j.server.server.serverpackets.S_Exp;
import l1j.server.server.serverpackets.S_HPMeter;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_Lawful;
import l1j.server.server.serverpackets.S_Liquor;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_PinkName;
import l1j.server.server.serverpackets.S_Poison;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_bonusstats;
import l1j.server.server.serverpackets.ServerBasePacket;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1PrivateShopBuyList;
import l1j.server.server.templates.L1PrivateShopSellList;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.CalcStat;
import l1j.server.server.utils.SQLUtil;
import server.LineageClient;
import server.manager.eva;


//Referenced classes of package l1j.server.server.model:
//L1Character, L1DropTable, L1Object, L1ItemInstance,
//L1World
public class L1PcInstance extends L1Character {
	/**by 판도라 레이드포탈**/
	public int[] DragonPortalLoc = new int[3];
	/**by 판도라 레이드포탈**/

	/**
	 *  로또 수동 입력때문에..
	 */
	private boolean isLotto;
	public boolean isLotto() {
		return isLotto;
	}
	public void setLotto(boolean isLotto) {
		this.isLotto = isLotto;
	}
	/**
	 * 코마 업데이트로 인한 메소드들
	 */
	private int _deathmatch;
	public int getDeathMatchPiece() {
		return _deathmatch;
	}
	public void setDeathMatchPiece(int i) {
		_deathmatch = i;
	}
	private int _petrace;
	public int getPetRacePiece() {
		return _petrace;
	}
	public void setPetRacePiece(int i) {
		_petrace = i;
	}
	private int _ultimatebattle;
	public int getUltimateBattlePiece() {
		return _ultimatebattle;
	}
	public void setUltimateBattlePiece(int i) {
		_ultimatebattle = i;
	}
	private int _petmatch;
	public int getPetMatchPiece() {
		return _petmatch;
	}
	public void setPetMatchPiece(int i) {
		_petmatch = i;
	}
	private int _ghosthouse;
	public int getGhostHousePiece() {
		return _ghosthouse;
	}
	public void setGhostHousePiece(int i) {
		_ghosthouse = i;
	}
	/** 소막 및 주사위 게임 */ //by 악덕 
	private boolean _isGambling = false;
	public boolean isGambling() { return _isGambling; }
	public void setGambling(boolean flag) { _isGambling = flag; }
	private int _gamblingmoney = 0;
	public int getGamblingMoney(){ return _gamblingmoney; }
	public void setGamblingMoney(int i){ _gamblingmoney = i; }   
	////##########겜블 소막 주사위 묵찌빠############
	private boolean _isGambling1 = false;
	public boolean isGambling1() { return _isGambling1; }
	public void setGambling1(boolean flag) { _isGambling1 = flag; }
	private int _gamblingmoney1 = 0;
	public int getGamblingMoney1(){ return _gamblingmoney1; }
	public void setGamblingMoney1(int i){ _gamblingmoney1 = i; }
	////##########겜블 소막 주사위 묵찌빠############
	private boolean _isGambling3 = false;
	public boolean isGambling3() { return _isGambling3; }
	public void setGambling3(boolean flag) { _isGambling3 = flag; }
	private int _gamblingmoney3 = 0;
	public int getGamblingMoney3(){ return _gamblingmoney3; }
	public void setGamblingMoney3(int i){ _gamblingmoney3 = i; }
	////##########겜블 소막 주사위 묵찌빠############
	private boolean _isGambling4 = false;
	public boolean isGambling4() { return _isGambling4; }
	public void setGambling4(boolean flag) { _isGambling4 = flag; }
	private int _gamblingmoney4 = 0;
	public int getGamblingMoney4(){ return _gamblingmoney4; }
	public void setGamblingMoney4(int i){ _gamblingmoney4 = i; }
	/** 소막 및 주사위 및 묵 찌 빠 게임 */   //by 악덕
	////////추가
	/**** 무인pc 관련 flag ******/
	public boolean noPlayerCK;
	private long _quiztime = 0;
	public long getQuizTime() { return _quiztime; }
	public void setQuizTime(long l) { _quiztime = l ; } 
	/** 2011.08.29 고정수 로봇 액션 딜레이 정보 */

	public boolean _multiTrading = false;
	public boolean MultiTrading() { return _multiTrading; }
	public void setMultiTrading(boolean i) { _multiTrading = i;}

	// 바포 아이콘 관련
	// 물리방어력
	private int physics_dmg_armor;
	// 마법방어력
	private int magic_dmg_armor;
	// 물리공격력
	private int physics_dmg_attack;
	// 주문력
	private int magic_dmg_attack;
	// 10레벨 높으면 보호되는..
	private boolean dismiss_level_10;

	public boolean isDismiss_level_10() {
		return dismiss_level_10;
	}public int getPhysics_dmg_armor() {
		return physics_dmg_armor;
	}public int getMagic_dmg_armor() {
		return magic_dmg_armor;
	}public int getPhysics_dmg_attack() {
		return physics_dmg_attack;
	}public int getMagic_dmg_attack() {
		return magic_dmg_attack;
	}

	// 바포 10레벨 차이 이상 무효
	L1Character Battacker;
	private boolean isDisMissLevel10(){
		if(Battacker == null) return false;
		if (Battacker instanceof L1PcInstance) {	// 죽이놈이 pc 라면
			int diffLv = getLevel() - Battacker.getLevel();
			// 초보아이콘이 떠있고 죽은놈이 레벨 20이하고이고 때린놈이랑 차이가 10이상일경우.
			if(dismiss_level_10 && getLevel() <= Config.MAX_LEVEL && diffLv > 10){ // 죽은놈이 초보보호 상태고, 죽인놈이랑 10레벨 차이란다면.
				return true;
			}				
		}
		return false;
	}	

	private int bLv = -1;
	private int bbLv = -1;
	// 
	// 라우풀에 따른 아이콘
	public void sendLawfulIcon(){
		try{
			int lawfulType = -1;
			// 라우풀 1단계 물리방어력이 2 마법방어력이 3 상승
			if(getLawful() >= 10000 && getLawful() <= 20000){
				lawfulType = 0;
				bLv = 0;
				dismiss_level_10 = false;
				// 라우풀 2단계 물리방어력이 4 마법방어력이 6 상승
			}else if(getLawful() >= 20001 && getLawful() <= 30000){
				lawfulType = 1;
				bLv = 1;
				dismiss_level_10 = false;
				// 라우풀 3단계 물리방어력이 6 마법방어력이 9 상승
			}else if(getLawful() >= 30001){
				lawfulType = 2;
				bLv = 2;
				dismiss_level_10 = false;
				// 카오풀 1단계 물리공격력 1 주문력 1 상승
			}else if(getLawful() >= -20000 && getLawful() <= -10000){
				lawfulType = 3;
				bLv = 3;
				dismiss_level_10 = false;
				// 카오풀 2단계 물리공격력 3 주문력 2
			}else if(getLawful() >= -30000 && getLawful() <= -20001){
				lawfulType = 4;
				bLv = 4;
				dismiss_level_10 = false;
				// 카오풀 3단계 물리공격력 5 주문력 3
			}else if(getLawful() <= -30001){
				lawfulType = 5;
				bLv = 5;
				dismiss_level_10 = false;				
			}else{
				lawfulType = -1;
				physics_dmg_armor = 0;
				magic_dmg_armor = 0;
				physics_dmg_attack = 0;
				magic_dmg_attack = 0;
				bLv = -1;
				dismiss_level_10 = false;
			}

			if(getLevel() <= 60){
				dismiss_level_10 = true;
				sendPackets(new S_BaphoIcon(6, true));	
			}else{
				dismiss_level_10 = false;
				sendPackets(new S_BaphoIcon(6, false));
			}

			if(bbLv != bLv){
				for(int i = 0; i < 6; i++) sendPackets(new S_BaphoIcon(i, false));
				// 이전 바포 레벨에 따라 효과 해제
				switch(bbLv){
				case 0:			// 라우풀 1 ac, mr
					getAC().addAc(2);
					getResistance().addMr(-3);
					break;
				case 1:			// 라우풀 2
					getAC().addAc(3);
					getResistance().addMr(-4);
					break;
				case 2:			// 3
					getAC().addAc(6);
					getResistance().addMr(-6);
					break;
				case 3:			// 카오틱 1 공력, sp
					getAbility().addSp(-1);
					addDmgupByArmor(-1);
					break;
				case 4:			// 2
					getAbility().addSp(-3);
					addDmgupByArmor(-3);
					break;
				case 5:			// 3
					getAbility().addSp(-5);
					addDmgupByArmor(-5);
					break;
				}
				// 셋팅 해주고..
				bbLv = bLv;
				// 현재 바포 수치에 따른 효과 적용
				switch(bbLv){
				case 0:			// 라우풀 1 ac, mr
					getAC().addAc(-2);
					getResistance().addMr(3);
					break;
				case 1:			// 라우풀 2
					getAC().addAc(-3);
					getResistance().addMr(4);
					break;
				case 2:			// 3
					getAC().addAc(-6);
					getResistance().addMr(6);
					break;
				case 3:			// 카오틱 1 공력, sp
					getAbility().addSp(1);
					addDmgupByArmor(1);
					break;
				case 4:			// 2
					getAbility().addSp(3);
					addDmgupByArmor(3);
					break;
				case 5:			// 3
					getAbility().addSp(5);
					addDmgupByArmor(5);
					break;
				}
				sendPackets(new S_SPMR(this));
				sendPackets(new S_OwnCharAttrDef(this));
			}
			if(lawfulType >= 0) sendPackets(new S_BaphoIcon(lawfulType, true));
		}catch(Exception e){}
	}
	//
	private static final long serialVersionUID = 1L;

	public static final int CLASSID_PRINCE = 0;
	public static final int CLASSID_PRINCESS = 1;
	public static final int CLASSID_KNIGHT_MALE = 61;
	public static final int CLASSID_KNIGHT_FEMALE = 48;
	public static final int CLASSID_ELF_MALE = 138;
	public static final int CLASSID_ELF_FEMALE = 37;
	public static final int CLASSID_WIZARD_MALE = 734;
	public static final int CLASSID_WIZARD_FEMALE = 1186;
	public static final int CLASSID_DARKELF_MALE = 2786;
	public static final int CLASSID_DARKELF_FEMALE = 2796;
	public static final int CLASSID_DRAGONKNIGHT_MALE = 6658;
	public static final int CLASSID_DRAGONKNIGHT_FEMALE = 6661;
	public static final int CLASSID_ILLUSIONIST_MALE = 6671;
	public static final int CLASSID_ILLUSIONIST_FEMALE = 6650;

	public static final int REGENSTATE_NONE = 4;
	public static final int REGENSTATE_MOVE = 2;
	public static final int REGENSTATE_ATTACK = 1;

	private L1ClassFeature _classFeature = null;
	private L1EquipmentSlot _equipSlot;
	private String _accountName;	
	private int _classId;
	private int _type;
	private int _exp;
	private short _accessLevel;
	private int _age;
	private String _username;
	private int birthday; 
	private short _baseMaxHp = 0; 
	private short _baseMaxMp = 0; 
	private int _baseAc = 0; 

	private int _baseBowDmgup = 0;
	private int _baseDmgup = 0; 
	private int _baseHitup = 0; 
	private int _baseBowHitup = 0; 

	private int _baseMagicHitup = 0;	//베이스 스탯에 의한 마법 명중
	private int _baseMagicCritical = 0; //베이스 스탯에 의한 마법 치명타(%)
	private int _baseMagicDmg = 0;		//베이스 스탯에 의한 마법 데미지
	private int _baseMagicDecreaseMp = 0;		//베이스 스탯에 의한 마법 데미지

	private int _HitupByArmor = 0; // 방어용 기구에 의한 근접무기 명중율
	private int _bowHitupByArmor = 0; // 방어용 기구에 의한 활의 명중율	
	private int _DmgupByArmor = 0; // 방어용 기구에 의한 근접무기 추타율	
	private int _bowDmgupByArmor = 0; // 방어용 기구에 의한 활의 추타율

	private int _bowHitupBydoll = 0;	// 인형에 의한 원거리 공격성공률
	private int _bowDmgupBydoll = 0;	// 인형에 의한 원거리 추타율

	private int _PKcount;
	private int _KillDeathInitialize; // 킬데스 초기화 주문서 사용 횟수
	private int _autoct; //오토인증추가

	private int _autogo; //오토인증추가

	private String  _autocode; //오토인증추가

	private int _autook;  //오토인증추가



	private int _clanid; 
	private String clanname; 
	private int _clanRank; 
	private byte _sex; 
	private int _returnstat; 
	private short _hpr = 0;
	private short _trueHpr = 0;
	private short _mpr = 0;
	private short _trueMpr = 0;

	private int _advenHp; 
	private int _advenMp;
	private int _highLevel; 

	private boolean _ghost = false; 
	private boolean _isReserveGhost = false;
	private boolean _isShowTradeChat = true;
	private boolean _isCanWhisper = true;
	private boolean _isFishing = false;
	private boolean _isFishingReady = false;

	private boolean _whisper = true;
	public boolean isWhisper() { return _whisper; }
	public void setWhisper(boolean i) { _whisper = i; }

	private boolean petRacing = false;		//펫레이싱
	private int petRacingLAB = 1;			// 현재 LAB
	private int petRacingCheckPoint = 162;	// 현재구간
	private boolean isHaunted = false; 
	private boolean isDeathMatch = false;
	private boolean _isShowWorldChat = true;
	private boolean _gm;
	private boolean _monitor;
	private boolean _gmInvis;
	private boolean _isTeleport = false;
	private boolean _isDrink = false;
	private boolean _isGres = false;
	private boolean _isPinkName = false;
	private boolean _banned; 
	private boolean _gresValid; 
	private boolean _tradeOk;
	private boolean _mpRegenActiveByDoll;
	private boolean _mpDecreaseActiveByScales;	
	private boolean _AHRegenActive;
	private boolean _SHRegenActive;
	private boolean _HalloweenRegenActive;
	private boolean _hpRegenActiveByDoll;



	private int invisDelayCounter = 0;
	private Object _invisTimerMonitor = new Object();

	private int _ghostSaveLocX = 0;
	private int _ghostSaveLocY = 0;
	private short _ghostSaveMapId = 0;
	private int _ghostSaveHeading = 0;

	private ScheduledFuture<?> _ghostFuture;
	private ScheduledFuture<?> _hellFuture;
	private ScheduledFuture<?> _autoUpdateFuture;
	private ScheduledFuture<?> _expMonitorFuture;

	private Timestamp _lastPk;
	private Timestamp _deleteTime;

	private int _weightReduction = 0;
	private int _hasteItemEquipped = 0;
	private int _damageReductionByArmor = 0;

	private final L1ExcludingList _excludingList = new L1ExcludingList();
	private final AcceleratorChecker _acceleratorChecker = new AcceleratorChecker(this);
	private ArrayList<Integer> skillList = new ArrayList<Integer>();

	private int _teleportY = 0;
	private int _teleportX = 0;
	private short _teleportMapId = 0;
	private int _teleportHeading = 0;

	private int _speedhackCount = 0;
	private int _speedhackX = 0;
	private int _speedhackY = 0;
	private short _speedhackMapid = 0;
	private int _speedhackHeading = 0;
	private int _speedright = 0;
	private int _speedinterval = 0;

	private int _tempCharGfxAtDead;
	private int _fightId;
	private byte _chatCount = 0;
	private long _oldChatTimeInMillis = 0L;

	private int _elfAttr; 
	private int _expRes;

	private int _onlineStatus;
	private int _homeTownId; 
	private int _contribution; 
	private int _food; 
	private int _hellTime;
	private int _partnerId;
	private long _fishingTime = 0;
	private int _dessertId = 0;
	private int _callClanId;
	private int _callClanHeading;

	private int _currentWeapon;
	private final L1Karma _karma = new L1Karma();
	private final L1PcInventory _inventory;
	private final L1Inventory _tradewindow;

	private L1ItemInstance _weapon;
	private L1ItemInstance _armor;
	private L1Party _party;
	private L1ChatParty _chatParty;

	private int _cookingId = 0;
	private int _partyID;
	private int _partyType;
	private int _tradeID;
	private int _tempID;
	private int _ubscore;


	private L1Quest _quest;
	private long _SurvivalCry; 

	public long getSurvivalCry() {  return _SurvivalCry;   }
	public void setSurvivalCry(long SurvivalCry) { _SurvivalCry = SurvivalCry; }

	//케릭교환
	private boolean _isChaTradeSlot = false;

	public boolean isChaTradeSlot() { return _isChaTradeSlot; }

	public void setChaTradeSlot(boolean is) { _isChaTradeSlot = is; }
	private int _elixirStats;

	private String _sealingPW; // ● 클랜명

	public String getSealingPW() {
		return _sealingPW;
	}

	public void setSealingPW(String s) {
		_sealingPW = s;
	}


	//캐릭터 교환

	//private HpRegeneration _hpRegen;	
	//private MpRegeneration _mpRegen;
	private HpRegenerationByDoll _hpRegenByDoll;
	private MpRegenerationByDoll _mpRegenByDoll;
	private MpDecreaseByScales _mpDecreaseByScales;
	private AHRegeneration _AHRegen;
	private SHRegeneration _SHRegen;
	private HalloweenRegeneration _HalloweenRegen;	
	private static Timer _regenTimer = new Timer(true);

	private boolean _isTradingInPrivateShop = false;
	private boolean _isPrivateShop = false;
	private int _partnersPrivateShopItemCount = 0; 

	private final ArrayList<L1BookMark> _bookmarks;
	private ArrayList<L1PrivateShopSellList> _sellList = new ArrayList<L1PrivateShopSellList>();
	private ArrayList<L1PrivateShopBuyList> _buyList = new ArrayList<L1PrivateShopBuyList>();

	private final Map<Integer, L1NpcInstance> _petlist = new HashMap<Integer, L1NpcInstance>();
	private final Map<Integer, L1DollInstance> _dolllist = new HashMap<Integer, L1DollInstance>();
	private final Map<Integer, L1FollowerInstance> _followerlist = new HashMap<Integer, L1FollowerInstance>();

	private byte[] _shopChat;
	private LineageClient _netConnection;
	private static Logger _log = Logger.getLogger(L1PcInstance.class.getName());
	private long lastSavedTime = System.currentTimeMillis();
	private long lastSavedTime_inventory = System.currentTimeMillis();

	private int adFeature = 1;
	public L1PcInstance() {
		super();
		_accessLevel = 0;
		_currentWeapon = 0;
		_inventory = new L1PcInventory(this);
		_tradewindow = new L1Inventory();
		_bookmarks = new ArrayList<L1BookMark>();
		_quest = new L1Quest(this);
		_equipSlot = new L1EquipmentSlot(this); 
	}

	public int getadFeature(){
		return adFeature;
	}
	public void setadFeature(int count){
		this.adFeature = count;
	}

	public long getlastSavedTime(){
		return lastSavedTime;		
	}

	public long getlastSavedTime_inventory(){
		return lastSavedTime_inventory;		
	}

	public void setlastSavedTime(long stime){
		this.lastSavedTime = stime;
	}

	public void setlastSavedTime_inventory(long stime){
		this.lastSavedTime_inventory = stime;
	}


	public void setSkillMastery(int skillid) {
		if (!skillList.contains(skillid)) {
			skillList.add(skillid);
		}
	}

	public void removeSkillMastery(int skillid) {
		if (skillList.contains((Object)skillid)) {
			skillList.remove((Object)skillid);
		}
	}

	public boolean isSkillMastery(int skillid) {
		return skillList.contains(skillid);
	}

	public void clearSkillMastery() {
		skillList.clear();
	}
	public int getAge() {
		return _age;
	}

	public void setAge(int i) {
		_age = i;
	}	

	public String getUserName() {
		return _username;
	}

	public void setUserName(String i) {
		_username = i;
	}

	public short getHpr() {
		return _hpr;
	}

	public void addHpr(int i) {
		_trueHpr += i;
		_hpr = (short) Math.max(0, _trueHpr);
	}

	public short getMpr() {
		return _mpr;
	}

	public void addMpr(int i) {
		_trueMpr += i;
		_mpr = (short) Math.max(0, _trueMpr);
	}

	/*public void startHpRegeneration() {
		final int INTERVAL = 1000;

		if (!_hpRegenActive) {
			_hpRegen = new HpRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_hpRegen, INTERVAL, INTERVAL);
			_hpRegenActive = true;
		}
	}*/

	public void startHpRegenerationByDoll() {
		final int INTERVAL_BY_DOLL = 64000;
		boolean isExistHprDoll = false;

		for (L1DollInstance doll : getDollList().values()) {
			if (doll.isHpRegeneration()) {
				isExistHprDoll = true;
			}
		}
		if (!_hpRegenActiveByDoll && isExistHprDoll) {
			_hpRegenByDoll = new HpRegenerationByDoll(this);
			_regenTimer.scheduleAtFixedRate(_hpRegenByDoll, INTERVAL_BY_DOLL, INTERVAL_BY_DOLL);
			_hpRegenActiveByDoll = true;
		}
	}
	/**
	 * 영엄한 파편 관련 : 대량의 HP 회복 물약
	 */
	public void startAHRegeneration() {
		final int INTERVAL = 600000;
		if (!_AHRegenActive) {
			_AHRegen = new AHRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_AHRegen, INTERVAL, INTERVAL);
			_AHRegenActive = true;
		}
	}
	/**
	 * 빛나는 나뭇잎 관련 : MP 회복
	 */
	public void startSHRegeneration() {
		final int INTERVAL = 1800000;
		if (!_SHRegenActive) {
			_SHRegen = new SHRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_SHRegen, INTERVAL, INTERVAL);
			_SHRegenActive = true;
		}
	}
	/**
	 * 할로윈 축복 모자를 착용시 지급 : 할로윈 호박파이
	 */
	public void startHalloweenRegeneration() {
		final int INTERVAL = 900000;
		if (!_HalloweenRegenActive) {
			_HalloweenRegen = new HalloweenRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_HalloweenRegen, INTERVAL, INTERVAL);
			_HalloweenRegenActive = true;
		}
	}

	/*public void stopHpRegeneration() {
		if (_hpRegenActive) {
			_hpRegen.cancel();
			_hpRegen = null;
			_hpRegenActive = false;
		}
	}*/

	public void stopHpRegenerationByDoll() {
		if (_hpRegenActiveByDoll) {
			_hpRegenByDoll.cancel();
			_hpRegenByDoll = null;
			_hpRegenActiveByDoll = false;
		}
	}

	/*public void startMpRegeneration() {
		final int INTERVAL = 1000;

		if (!_mpRegenActive) {
			_mpRegen = new MpRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_mpRegen, INTERVAL, INTERVAL);
			_mpRegenActive = true;
		}
	}*/

	public void startMpRegenerationByDoll() {
		final int INTERVAL_BY_DOLL = 64000;
		boolean isExistMprDoll = false;

		for (L1DollInstance doll : getDollList().values()) {

			if (doll.isMpRegeneration()) {
				isExistMprDoll = true;
			}
		}
		if (!_mpRegenActiveByDoll && isExistMprDoll) {
			_mpRegenByDoll = new MpRegenerationByDoll(this);
			_regenTimer.scheduleAtFixedRate(_mpRegenByDoll, INTERVAL_BY_DOLL, INTERVAL_BY_DOLL);
			_mpRegenActiveByDoll = true;
		}
	}

	public void startMpDecreaseByScales() {
		final int INTERVAL_BY_SCALES = 4000;
		_mpDecreaseByScales = new MpDecreaseByScales(this);
		_regenTimer.scheduleAtFixedRate(_mpDecreaseByScales, INTERVAL_BY_SCALES, INTERVAL_BY_SCALES);
		_mpDecreaseActiveByScales = true;
	}

	/*public void stopMpRegeneration() {
		if (_mpRegenActive) {
			_mpRegen.cancel();
			_mpRegen = null;
			_mpRegenActive = false;
		}
	}*/

	public void stopMpRegenerationByDoll() {
		if (_mpRegenActiveByDoll) {
			_mpRegenByDoll.cancel();
			_mpRegenByDoll = null;
			_mpRegenActiveByDoll = false;
		}
	}

	public void stopMpDecreaseByScales() {
		if (_mpDecreaseActiveByScales) {
			_mpDecreaseByScales.cancel();
			_mpDecreaseByScales = null;
			_mpDecreaseActiveByScales = false;
		}
	}

	public void stopAHRegeneration() {
		if (_AHRegenActive) {
			_AHRegen.cancel();
			_AHRegen = null;
			_AHRegenActive = false;
		}
	}

	public void stopSHRegeneration() {
		if (_SHRegenActive) {
			_SHRegen.cancel();
			_SHRegen = null;
			_SHRegenActive = false;
		}
	}

	public void stopHalloweenRegeneration() {
		if (_HalloweenRegenActive) {
			_HalloweenRegen.cancel();
			_HalloweenRegen = null;
			_HalloweenRegenActive = false;
		}
	}

	public void startObjectAutoUpdate() {
		final long INTERVAL_AUTO_UPDATE = 300;
		getNearObjects().removeAllKnownObjects();
		_autoUpdateFuture = GeneralThreadPool.getInstance()
				.pcScheduleAtFixedRate(new L1PcAutoUpdate(getId()), 0L, INTERVAL_AUTO_UPDATE);
	}

	public void stopEtcMonitor() {
		if (_autoUpdateFuture != null) {
			_autoUpdateFuture.cancel(true);
			_autoUpdateFuture = null;
		}
		if (_expMonitorFuture != null) {
			_expMonitorFuture.cancel(true);
			_expMonitorFuture = null;
		}

		if (_ghostFuture != null) {
			_ghostFuture.cancel(true);
			_ghostFuture = null;
		}

		if (_hellFuture != null) {
			_hellFuture.cancel(true);
			_hellFuture = null;
		}

	}

	public void stopEquipmentTimer() {
		List<L1ItemInstance> allItems = this.getInventory().getItems();
		for(L1ItemInstance item : allItems){
			if(item.isEquipped() && item.getRemainingTime() >0) {
				item.stopEquipmentTimer();
			}
		}
	}

	public void onChangeExp() {
		int level = ExpTable.getLevelByExp(getExp());
		int char_level = getLevel();
		int gap = level - char_level;
		if (gap == 0) {
			// sendPackets(new S_OwnCharStatus(this));
			sendPackets(new S_Exp(this));
			return;
		}

		if (gap > 0) {
			levelUp(gap);
		} else if (gap < 0) {
			levelDown(gap);
		}
	}

	@Override
	public void onPerceive(L1PcInstance pc) {
		if (isGhost()) {
			return;
		}

		pc.getNearObjects().addKnownObject(this);
		pc.sendPackets(new S_OtherCharPacks(this));

		if (isPinkName()
				|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.USERSTATUS_ATTACK) 
				&& pc.getClanid() != getClanid()){
			pc.sendPackets(new S_PinkName(getId(), getSkillEffectTimerSet().getSkillEffectTimeSec(L1SkillId.STATUS_PINK_NAME)));
		}

		if (isInParty() && getParty().isMember(pc)) {
			pc.sendPackets(new S_HPMeter(this));
		}

		if (isPrivateShop()) {
			pc.sendPackets(new S_DoActionShop(getId(), ActionCodes.ACTION_Shop, get_ment1(), get_ment2()));
		}

		if (isCrown()) {
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			if (clan != null) {
				if (getId() == clan.getLeaderId()
						&& clan.getCastleId() != 0) {
					pc.sendPackets(new S_CastleMaster(clan.getCastleId(), getId()));
				}
			}
		}

	}

	public void updateObject() {
		for (L1Object known : getNearObjects().getKnownObjects()) {
			if (known == null) {
				continue;
			}
			if (Config.PC_RECOGNIZE_RANGE == -1) {
				if (!getLocation().isInScreen(known.getLocation())) { 
					getNearObjects().removeKnownObject(known);
					sendPackets(new S_RemoveObject(known));
				}
			}else {
				if (getLocation().getTileLineDistance(known.getLocation()) > Config.PC_RECOGNIZE_RANGE) {
					getNearObjects().removeKnownObject(known);
					sendPackets(new S_RemoveObject(known));
				}
			}
		}

		for (L1Object visible : L1World.getInstance().getVisibleObjects(this, Config.PC_RECOGNIZE_RANGE)) {
			if (!getNearObjects().knownsObject(visible)) {
				visible.onPerceive(this);
			} else {
				if (visible instanceof L1NpcInstance) {
					L1NpcInstance npc = (L1NpcInstance) visible;
					if (getLocation().isInScreen(npc.getLocation()) && npc.getHiddenStatus() != 0) {
						npc.approachPlayer(this);
					}
				}
			}
			if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.GMSTATUS_HPBAR) && L1HpBar.isHpBarTarget(visible)) {
				sendPackets(new S_HPMeter((L1Character) visible));
			}
			//////좀비모드시에 피바보이도록 by-Kingdom
			if (getZombieMod() > 0 && L1HpBar.isHpBarTarget(visible)) {
				sendPackets(new S_HPMeter((L1Character) visible));
			}
			//////좀비모드시에 피바보이도록 by-Kingdom
		}
	}

	private void sendVisualEffect() {
		int poisonId = 0;
		if (getPoison() != null) {
			poisonId = getPoison().getEffectId();
		}
		if (getParalysis() != null) { 
			poisonId = getParalysis().getEffectId();
		}
		if (poisonId != 0) { 
			sendPackets(new S_Poison(getId(), poisonId));
			Broadcaster.broadcastPacket(this, new S_Poison(getId(), poisonId));
		}
	}

	public void sendVisualEffectAtLogin() {
		sendVisualEffect();
	}

	public void sendCastleMaster(){
		if (getClanid() != 0) {
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			if (clan != null) {
				if (isCrown() && getId() == clan.getLeaderId() && clan.getCastleId() != 0) {
					sendPackets(new S_CastleMaster(clan.getCastleId(), getId()));
				}
			}
		}
	}

	public void sendVisualEffectAtTeleport() {
		if (isDrink()) { 
			sendPackets(new S_Liquor(getId()));
		}
		sendVisualEffect();
	}

	@Override
	public void setCurrentHp(int i) {
		if (getCurrentHp() == i) return;
		if(isGm()) i = getMaxHp();
		super.setCurrentHp(i);
		sendPackets(new S_HPUpdate(getCurrentHp(), getMaxHp()));
		if (isInParty()) getParty().updateMiniHP(this);
		//배틀존 추가
		if(this.getBattleOk()){
			if(this.getMapId() != 5083){
				updateMiniHP(this);
			}
		}
	}

	@Override
	public void setCurrentMp(int i) {
		if (getCurrentMp() == i) return;
		if(isGm()) i = getMaxMp();
		super.setCurrentMp(i);	
		sendPackets(new S_MPUpdate(getCurrentMp(), getMaxMp()));
	}

	@Override
	public L1PcInventory getInventory() {
		return _inventory;
	}

	public L1Inventory getTradeWindowInventory() {
		return _tradewindow;
	}

	public boolean isGmInvis() {	return _gmInvis;	}
	public void setGmInvis(boolean flag) {	_gmInvis = flag;	}

	public int getCurrentWeapon() {	return _currentWeapon;	}
	public void setCurrentWeapon(int i) {	_currentWeapon = i;	}

	public int getType() {	return _type;	}
	public void setType(int i) {		_type = i;	}

	public short getAccessLevel() {	return _accessLevel;	}
	public void setAccessLevel(short i) {		_accessLevel = i;	}


	/////////////////////////////// 좀비
	private int _Zombie;

	private void setZombie(int i) { _Zombie = i; }

	public int getZombie() { return _Zombie; }

	//////////////////////  좀비

	public int getClassId() {
		return _classId;
	}

	public void setClassId(int i) {
		_classId = i;
		_classFeature = L1ClassFeature.newClassFeature(i);
	}

	public L1ClassFeature getClassFeature() {
		return _classFeature;
	}

	@Override
	public synchronized int getExp() {
		return _exp;
	}

	@Override
	public synchronized void setExp(int i) {
		_exp = i;
	}
	public synchronized int getReturnStat() {	return _returnstat;	}
	public synchronized void setReturnStat(int i) {	_returnstat = i;	}


	private L1PcInstance getStat() {
		return null;
	}

	public void reduceCurrentHp(double d, L1Character l1character) {
		getStat().reduceCurrentHp(d, l1character);
	}

	private void notifyPlayersLogout(List<L1PcInstance> playersArray) {
		for (L1PcInstance player : playersArray) {
			if (player.getNearObjects().knownsObject(this)) {
				player.getNearObjects().removeKnownObject(this);
				player.sendPackets(new S_RemoveObject(this));
			}
		}
	}
	private void updateMiniHP(L1PcInstance pc){
		for (L1PcInstance member : BattleZoneController.getInstance().getBattleMembers()){
			if(member != null){
				if(pc != member){
					if(pc.get_BattleLine() == member.get_BattleLine()){
						member.sendPackets(new S_HPMeter(pc.getId(), 100 * pc.getCurrentHp() / pc.getMaxHp()));
					}
				}
			}
		}
	}
	private PapuBlessing _PapuRegen;
	private boolean _PapuBlessingActive;	 

	public void startPapuBlessing() { 
		final int RegenTime = 150000;
		if (!_PapuBlessingActive) {
			_PapuRegen = new PapuBlessing(this);
			_regenTimer.scheduleAtFixedRate(_PapuRegen, RegenTime, RegenTime);
			_PapuBlessingActive = true;
		}
	}	 

	public void stopPapuBlessing() {
		if (_PapuBlessingActive) {
			_PapuRegen.cancel();
			_PapuRegen = null;
			_PapuBlessingActive = false;
		}
	}

	public void logout() {
		L1World world = L1World.getInstance();
		notifyPlayersLogout(getNearObjects().getKnownPlayers());
		world.removeVisibleObject(this);
		world.removeObject(this);
		notifyPlayersLogout(world.getRecognizePlayer(this));
		stopEquipmentTimer();
		_inventory.clearItems();
		WarehouseManager w = WarehouseManager.getInstance(); 
		w.delPrivateWarehouse(this.getAccountName());
		w.delElfWarehouse(this.getAccountName());
		w.delPackageWarehouse(this.getAccountName());

		getNearObjects().removeAllKnownObjects();
		//stopHpRegeneration();
		//stopMpRegeneration();

		stopHalloweenRegeneration();
		stopAHRegeneration();
		stopPapuBlessing();
		stopHpRegenerationByDoll();
		stopMpRegenerationByDoll();
		stopSHRegeneration();
		stopMpDecreaseByScales();

		stopEtcMonitor();

		setDead(true); 
	}

	public LineageClient getNetConnection() {	return _netConnection;	}
	public void setNetConnection(LineageClient clientthread) {	_netConnection = clientthread;	}

	public boolean isInParty() {	return getParty() != null;	}
	public L1Party getParty() {	return _party;	}
	public void setParty(L1Party p) {	_party = p;	}

	public boolean isInChatParty() {	return getChatParty() != null;	}
	public L1ChatParty getChatParty() {	return _chatParty;	}
	public void setChatParty(L1ChatParty cp) {	_chatParty = cp;	}

	public int getPartyID() {	return _partyID;	}
	public void setPartyID(int partyID) {	_partyID = partyID;	}

	public int getPartyType(){	return _partyType;	}
	public void setPartyType(int partyType){	_partyType = partyType;	}

	public int getTradeID() {	return _tradeID;	}
	public void setTradeID(int tradeID) {	_tradeID = tradeID;	}

	public void setTradeOk(boolean tradeOk) {	_tradeOk = tradeOk;	}
	public boolean getTradeOk() {	return _tradeOk;	}

	public int getTempID() {	return _tempID;	}
	public void setTempID(int tempID) {	_tempID = tempID;	}

	public boolean isTeleport() {	return _isTeleport;	}
	public void setTeleport(boolean flag) {	_isTeleport = flag;	}

	public boolean isDrink() {	return _isDrink;	}
	public void setDrink(boolean flag) {	_isDrink = flag;	}

	public boolean isGres() {	return _isGres;	}
	public void setGres(boolean flag) {	_isGres = flag;	}

	public boolean isPinkName() {	return _isPinkName;	}
	public void setPinkName(boolean flag) {	_isPinkName = flag;	}

	public ArrayList<L1PrivateShopSellList> getSellList() {	return _sellList;	}

	public ArrayList<L1PrivateShopBuyList> getBuyList() {	return _buyList;	}

	public void setShopChat(byte[] chat) {	_shopChat = chat;	}
	public byte[] getShopChat() {	return _shopChat;	}

	// 시장 저장
	private String ment1;
	private String ment2;
	public void set_ment1(String s){
		ment1 = s;
	}public String get_ment1(){
		return ment1;
	}
	public void set_ment2(String s){
		ment2 = s;
	}public String get_ment2(){
		return ment2;
	}
	//

	public boolean isPrivateShop() {	return _isPrivateShop;	}
	public void setPrivateShop(boolean flag) {	_isPrivateShop = flag;	}

	public boolean isTradingInPrivateShop() {	return _isTradingInPrivateShop;	}
	public void setTradingInPrivateShop(boolean flag) {	_isTradingInPrivateShop = flag;	}

	public int getPartnersPrivateShopItemCount() {	return _partnersPrivateShopItemCount;	}
	public void setPartnersPrivateShopItemCount(int i) {	_partnersPrivateShopItemCount = i;	}

	public void sendPackets(ServerBasePacket serverbasepacket) {
		if (getNetConnection() == null) {
			return;
		}

		try {
			getNetConnection().sendPacket(serverbasepacket);
		} catch (Exception e) {
		}
	}

	@Override
	public void onAction(L1PcInstance attacker) {
		if (attacker == null) {
			return;
		}
		if (isTeleport()) {
			return;
		}
		if (CharPosUtil.getZoneType(this) == 1 || CharPosUtil.getZoneType(attacker) == 1) {
			L1Attack attack_mortion = new L1Attack(attacker, this);
			attack_mortion.action();
			return;
		}

		if (checkNonPvP(this, attacker) == true) {
			L1Attack attack_mortion = new L1Attack(attacker, this);
			attack_mortion.action();
			return;
		}

		if (getCurrentHp() > 0 && !isDead()) {
			attacker.delInvis();

			boolean isCounterBarrier = false;
			boolean isMortalBody = false;
			L1Attack attack = new L1Attack(attacker, this);
			L1Magic magic = null;

			if (attack.calcHit()) {
				if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.COUNTER_BARRIER)) {
					magic = new L1Magic(this, attacker);
					boolean isProbability = magic.calcProbabilityMagic(L1SkillId.COUNTER_BARRIER);
					boolean isShortDistance = attack.isShortDistance();
					if (isProbability && isShortDistance) {
						isCounterBarrier = true;
					}
				}else if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MORTAL_BODY)) {
					Random random = new Random();
					int MortalbodyChance = random.nextInt(100) + 1;  // 추가
					boolean isShortDistance = attack.isShortDistance();
					if (10 >= MortalbodyChance && isShortDistance) { 
						isMortalBody = true;
					}
				}
				if (!isCounterBarrier && !isMortalBody) {
					attacker.setPetTarget(this);
					attack.calcDamage();
					attack.calcDrainOfHp();
					attack.addPcPoisonAttack(attacker, this);
				}
			}
			if (isCounterBarrier) {
				attack.actionCounterBarrier();
				attack.commitCounterBarrier();
			}else if (isMortalBody){
				attack.actionMortalBody();
				attack.commitMortalBody();
			} else {
				attack.action();
				attack.commit();
			}
		}
	}

	public boolean checkNonPvP(L1PcInstance pc, L1Character target) {
		L1PcInstance targetpc = null;

		if(target instanceof L1PcInstance)			targetpc = (L1PcInstance) target;
		else if(target instanceof L1PetInstance) 	targetpc = (L1PcInstance) ((L1PetInstance) target).getMaster();
		else if(target instanceof L1SummonInstance) targetpc = (L1PcInstance) ((L1SummonInstance) target).getMaster();

		if(targetpc == null) return false; 

		if(!Config.ALT_NONPVP) { 
			if (getMap().isCombatZone(getLocation())) return false;

			for (L1War war : L1World.getInstance().getWarList()) {
				if (pc.getClanid() != 0 && targetpc.getClanid() != 0) {
					boolean same_war = war.CheckClanInSameWar(pc.getClanname(),	targetpc.getClanname());

					if (same_war == true) return false;
				}
			}

			if (target instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) target;
				if (isInWarAreaAndWarTime(pc, targetPc)) return false;
			}
			return true;
		} 

		return false;
	}

	private boolean isInWarAreaAndWarTime(L1PcInstance pc, L1PcInstance target) {
		int castleId = L1CastleLocation.getCastleIdByArea(pc);
		int targetCastleId = L1CastleLocation.getCastleIdByArea(target);
		if (castleId != 0 && targetCastleId != 0 && castleId == targetCastleId) {
			if (WarTimeController.getInstance().isNowWar(castleId)) {
				return true;
			}
		}
		return false;
	}

	public void setPetTarget(L1Character target) {
		Object[] petList = getPetList().values().toArray();
		L1PetInstance pets = null;
		L1SummonInstance summon = null;
		for (Object pet : petList) {
			if (pet instanceof L1PetInstance) {
				pets = (L1PetInstance) pet;
				pets.setMasterTarget(target);
			} else if (pet instanceof L1SummonInstance) {
				summon = (L1SummonInstance) pet;
				summon.setMasterTarget(target);
			}
		}
	}

	public void delInvis() {
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) { 
			getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
			sendPackets(new S_Invis(getId(), 0));
			Broadcaster.broadcastPacket(this, new S_Invis(getId(), 0));
			//broadcastPacket(new S_OtherCharPacks(this));
		}
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLIND_HIDING)) { 
			getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
			sendPackets(new S_Invis(getId(), 0));
			Broadcaster.broadcastPacket(this, new S_Invis(getId(), 0));
			//broadcastPacket(new S_OtherCharPacks(this));
		}
	}

	public void delBlindHiding() {
		getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
		sendPackets(new S_Invis(getId(), 0));
		Broadcaster.broadcastPacket(this, new S_Invis(getId(), 0));
		//broadcastPacket(new S_OtherCharPacks(this));
	}

	public void receiveDamage(L1Character attacker, int damage, int attr) {
		Random random = new Random(System.nanoTime());
		int player_mr = getResistance().getEffectedMrBySkill();
		int rnd = random.nextInt(100) + 1;
		if (player_mr >= rnd) {
			damage /= 2;
		}
		receiveDamage(attacker, damage, false);
	}

	public void receiveManaDamage(L1Character attacker, int mpDamage) {
		if (mpDamage > 0 && !isDead()) {
			delInvis();
			if (attacker instanceof L1PcInstance) {
				L1PinkName.onAction(this, attacker);
			}
			if (attacker instanceof L1PcInstance
					&& ((L1PcInstance) attacker).isPinkName()) {
				L1GuardInstance guard = null;
				for (L1Object object : L1World.getInstance().getVisibleObjects(attacker)) {
					if (object instanceof L1GuardInstance) {
						guard = (L1GuardInstance) object;
						guard.setTarget(((L1PcInstance) attacker));
					}
				}
			}
			int newMp = getCurrentMp() - mpDamage;
			this.setCurrentMp(newMp);
		}
	}

	public long _oldTime = 0;

	public void receiveDamage(L1Character attacker, double damage, boolean isMagicDamage) { 
		if (getCurrentHp() > 0 && !isDead() && !isGhost()) {
			if (attacker != this && !getNearObjects().knownsObject(attacker)
					&& attacker.getMapId() == this.getMapId()) {
				attacker.onPerceive(this);
			}

			Random random = new Random();  //파푸가호
			int chance = random.nextInt(100) + 1;
			int plus_hp = 70 + random.nextInt(20) + 1;
			if (getInventory().checkEquipped(420104) || getInventory().checkEquipped(420105) 
					|| getInventory().checkEquipped(420106) || getInventory().checkEquipped(420107)) { 
				if (chance <= 5 && getCurrentHp() != getMaxHp()) {
					if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.WATER_LIFE)) { //요건없어서 임시로넣었어요.
						plus_hp *= 2;
					}
					if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.POLLUTE_WATER)) { //요건없어서 임시로넣었어요.
						plus_hp /= 2;
					}
					setCurrentHp(getCurrentHp() + plus_hp);
					sendPackets(new S_SkillSound(getId(), 2187));
					Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 2187));
				}
			}
			if (isMagicDamage == true) {
				long nowTime = System.currentTimeMillis();
				long interval = nowTime - _oldTime;
				/*
				if (interval >= 1400) {
					damage = (damage * 1);                    
				} else if (1400 > interval && interval >= 1300) {
					damage = (damage * (100 - 3 * (10 / 3))) / 100;
				} else if (1300 > interval && interval >= 1200) {
					damage = (damage * (100 - 4 * (10 / 3))) / 100;
				} else if (1200 > interval && interval >= 1100) {
					damage = (damage * (100 - 4 * (10 / 3))) / 100;
				} else if (1100 > interval && interval >= 1000) {
					damage = (damage * (100 - 5 * (10 / 3))) / 100;
				} else if (1000 > interval && interval >= 900) {
					damage = (damage * (100 - 6 * (10 / 3))) / 100;
				} else 
				 */
				if (900 > interval && interval >= 800) {
					damage = (damage * (100 - 7 * (10 / 3))) / 100;
				} else if (800 > interval && interval >= 700) {
					damage = (damage * (100 - 8 * (10 / 3))) / 100;
				} else if (700 > interval && interval >= 600) {
					damage = (damage * (100 - 9 * (10 / 3))) / 100;
				} else if (600 > interval && interval >= 500) {
					damage = (damage * (100 - 10 * (10 / 3))) / 100;
				} else if (500 > interval && interval >= 400) {
					damage = (damage * (100 - 11 * (10 / 3))) / 100;
				} else if (400 > interval && interval >= 300) {
					damage = (damage * (100 - 12 * (10 / 3))) / 100;
				} else if (300 > interval) {
					damage = (damage * 0.8); 
				} else {
					damage = (1 + damage * 1);                   
				}

				/*if (damage < 1) {
						damage = 0;
					}*/
				_oldTime = nowTime; 

			}
			if (damage > 0) {
				delInvis();
				if (attacker instanceof L1PcInstance) {
					L1PinkName.onAction(this, attacker);
					/** 플레이어가 마법 공격시 펫이 피시에게 발동**/
					L1PcInstance _Attacker = (L1PcInstance) attacker;
					_Attacker.setPetTarget(this);
				}
				/** 플레이어가 마법 공격시 펫이  피시에게 발동**/ 
				/*
				if (attacker instanceof L1PcInstance
						&& ((L1PcInstance) attacker).isPinkName()) {
					for (L1Object object : L1World.getInstance().getVisibleObjects(attacker)) {
						if (object instanceof L1GuardInstance) {
							L1GuardInstance guard = (L1GuardInstance) object;
							guard.setTarget(((L1PcInstance) attacker));
						}
					}
				}
				 */
				if(getSkillEffectTimerSet().hasSkillEffect(L1SkillId.FOG_OF_SLEEPING)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.FOG_OF_SLEEPING);
				}else if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.PHANTASM);
				}
			}

			int newHp = getCurrentHp() - (int) (damage);
			/*if (newHp > getMaxHp()) {
				newHp = getMaxHp();
			}*/
			if (newHp <= 0) {
				if (isGm()) {
					this.setCurrentHp(getMaxHp());
				} else {
					if(isDeathMatch()){
						if(getMapId() == 5153){
							try {
								this.setCurrentHp(getMaxHp());
								save();
								beginGhost(getX(), getY(), (short) getMapId(), true);
								sendPackets(new S_ServerMessage(1271));
							} catch (Exception e) {
								_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
							}
							return;
						}
					} else {
						if (attacker instanceof L1PcInstance){
							if (damage > 0) {  //배틀존추가
								if(get_BattleLine() != 0 && attacker.get_BattleLine() != 0){         
									if(get_BattleLine() == attacker.get_BattleLine()){
										return;
									}
								}
							}
							if (CharPosUtil.getZoneType(L1PcInstance.this) == 0 && CharPosUtil.getZoneType(attacker) == 0){
								//if (getLevel() >= 1) { 
								attacker.setKills(attacker.getKills()+1); 
								setDeaths(getDeaths()+1); 
								//}							
								int price = getHuntPrice();							
								if (getHuntCount() > 0){
									if (CharPosUtil.getZoneType(L1PcInstance.this) == 0)//	
										attacker.getInventory().storeItem(40308, price);
									setHuntCount(0);
									setHuntPrice(0);
									//setReasonToHunt(null);
									L1World.getInstance().broadcastPacketToAll(
											new S_SystemMessage("\\fR"+ attacker.getName() + ": 님이  " + getName() + "님을 잡아 척살이 풀렸습니다."));
									try {
										save();
									} catch (Exception e) {
										_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
									}
								} else {						
									L1World.getInstance().broadcastPacketToAll(new S_SystemMessage(attacker.getName() + "님이 " + this.getName()+"님과의 전투에서 승리하였습니다."));
								}
							}
						}
						death(attacker);
						saveInventory();
					}
				}
			}else{ 
				if (newHp > getMaxHp()) {  
					newHp = getMaxHp();
				}
				this.setCurrentHp(newHp);
			}
		} else if (!isDead()) { 
			System.out.println("[L1PcInstance] 경고：플레이어의 HP감소 처리가 올바르게 행해지지 않은 개소가 있습니다.※혹은 최초부터 HP0");
			death(attacker);
		}
	}

	public void death(L1Character lastAttacker) {
		synchronized (this) {
			if (isDead()) {
				return;
			}
			setDead(true);
			saveInventory();

			setActionStatus(ActionCodes.ACTION_Die);
		}
		// 바포
		Battacker = lastAttacker;
		//
		GeneralThreadPool.getInstance().execute(new Death(lastAttacker));

	}

	private class Death implements Runnable {
		L1Character _lastAttacker;

		Death(L1Character cha) {
			_lastAttacker = cha;
		}

		public void run() {
			L1Character lastAttacker = _lastAttacker;
			_lastAttacker = null;
			setCurrentHp(0);
			setGresValid(false); 

			while (isTeleport()) { 
				try {
					Thread.sleep(300);
				} catch (Exception e) {}
			}

			//stopHpRegeneration();
			//stopMpRegeneration();

			int targetobjid = getId();
			getMap().setPassable(getLocation(), true);

			int tempchargfx = 0;
			if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHAPE_CHANGE)) {
				tempchargfx = getGfxId().getTempCharGfx();
				setTempCharGfxAtDead(tempchargfx);
			} else {
				setTempCharGfxAtDead(getClassId());
			}

			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(L1PcInstance.this,
					L1SkillId.CANCELLATION, getId(), getX(), getY(), null, 0, L1SkillUse.TYPE_LOGIN);

			if (tempchargfx == 5727 || tempchargfx == 5730 || tempchargfx == 5733 || tempchargfx == 5736) {
				tempchargfx = 0;
			}
			if (tempchargfx != 0) {
				sendPackets(new S_ChangeShape(getId(), tempchargfx));
				Broadcaster.broadcastPacket(L1PcInstance.this, new S_ChangeShape(getId(), tempchargfx));
			} else {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {}
			}

			sendPackets(new S_DoActionGFX(targetobjid, ActionCodes.ACTION_Die));
			Broadcaster.broadcastPacket(L1PcInstance.this, new S_DoActionGFX(targetobjid, ActionCodes.ACTION_Die));

			if (lastAttacker != L1PcInstance.this) {
				if (CharPosUtil.getZoneType(L1PcInstance.this) != 0) {
					L1PcInstance player = null;
					if (lastAttacker instanceof L1PcInstance) {
						player = (L1PcInstance) lastAttacker;
					} else if (lastAttacker instanceof L1PetInstance) {
						player = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
					} else if (lastAttacker instanceof L1SummonInstance) {
						player = (L1PcInstance) ((L1SummonInstance) lastAttacker).getMaster();
					}
					if (player != null) {
						if (!isInWarAreaAndWarTime(L1PcInstance.this, player)) {
							return;
						}
					}
				}

				boolean sim_ret = simWarResult(lastAttacker); 
				if (sim_ret == true) { 
					return;
				}
			}

			if (!getMap().isEnabledDeathPenalty()) {
				return;
			}

			L1PcInstance fightPc = null;
			if (lastAttacker instanceof L1PcInstance) {
				fightPc = (L1PcInstance) lastAttacker;
			}
			if (fightPc != null) {
				if (getFightId() == fightPc.getId()
						&& fightPc.getFightId() == getId()) {
					setFightId(0);
					sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, 0, 0));
					fightPc.setFightId(0);
					fightPc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, 0, 0));
					return;
				}
			}
			deathPenalty(); 

			setGresValid(true); 

			if (getExpRes() == 0) {
				setExpRes(1);
			}

			if (lastAttacker instanceof L1GuardInstance) {
				if (get_PKcount() > 0) {
					set_PKcount(get_PKcount() - 1);
				}
				setLastPk(null);
			}

			int lostRate = (int) (((getLawful() + 32768D) / 1000D - 65D) * 5D);
			if (lostRate < 0) {
				lostRate *= -1;
				Random random = new Random();
				int rnd = random.nextInt(65) + 1;
				if (rnd <= lostRate) {
					int count = 0;
					if (getLawful() <= -32000) {
						count = random.nextInt(8) + 1;
					} else if (getLawful() <= -30000) {
						count = random.nextInt(7) + 1;
					} else if (getLawful() <= -20000) {
						count = random.nextInt(6) + 1;
					} else if (getLawful() <= -10000) {
						count = random.nextInt(5) + 1;
					} else if (getLawful() < 0) {
						count = random.nextInt(4) + 1;
					} else if (getLawful() < 10000){
						count = random.nextInt(3) + 1;
					} else if (getLawful() < 30000){
						count = random.nextInt(2) + 1;
					} else if (getLawful() < 32767){
						count = random.nextInt(1) + 1;
					}
					caoPenaltyResult(count); // 일반 아이템 드랍
					//caoPenaltySkill(count); // 스킬 드랍
				}
			}
			boolean castle_ret = castleWarResult(); 
			if (castle_ret == true) { 
				return;
			}

			L1PcInstance player = null;
			if (lastAttacker instanceof L1PcInstance) {
				player = (L1PcInstance) lastAttacker;
			}
			if (player != null) {
				if (getLawful() >= 0 && isPinkName() == false) {
					boolean isChangePkCount = false;
					if (player.getLawful() < 30000) {
						player.set_PKcount(player.get_PKcount() + 1);
						isChangePkCount = true;
						player.setLastPk();
					}

					int lawful;

					if (player.getLevel() < 50) {
						lawful = -1	* (int) ((Math.pow(player.getLevel(), 2) * 4));
					} else {
						lawful = -1	* (int) ((Math.pow(player.getLevel(), 3) * 0.08));
					}
					if ((player.getLawful() - 1000) < lawful) {
						lawful = player.getLawful() - 1000;
					}

					if (lawful <= -32768) {
						lawful = -32768;
					}
					player.setLawful(lawful);

					S_Lawful s_lawful = new S_Lawful(player.getId(), player.getLawful());
					player.sendPackets(s_lawful);
					Broadcaster.broadcastPacket(player, s_lawful);

					if (isChangePkCount && (player.get_PKcount() >= 95 && player.get_PKcount() < 100)) {
						player.sendPackets(new S_BlueMessage(551, String.valueOf(player.get_PKcount()), "100"));
					} else if (isChangePkCount && (player.get_PKcount() == 100 || player.get_PKcount() == 200)) {
						player.beginHell(true);
					}
				} else {
					if (isPinkName()){
						getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_PINK_NAME);
						setPinkName(false);
					}
				}
			}
		}
	}

	private void caoPenaltyResult(int count) {
		// 바포
		if(isDisMissLevel10()){ return;}
		//A102 신규혈맹은 죽어도 떨구지 않는다.
		if(getClanid() == Config.NewClan1){ return; } 
		if(getClanid() == Config.NewClan2){ return; } 
		if(getClanid() == Config.NewClan3){ return; } 
		if(getClanid() == Config.NewClan4){ return; } 
		//A102 신규혈맹은 죽어도 떨구지 않는다.
		if(isGm()){	return; }
		for (int i = 0; i < count; i++) {
			L1ItemInstance item = getInventory().CaoPenalty();
			if (item != null) {
				if(item.getBless() > 3){
					getInventory().removeItem(item, item.isStackable() ? item.getCount() : 1);
					sendPackets(new S_ServerMessage(638,item.getLogName()));
					if(item.getItem().getType2() >= 1){
						if(item.getEnchantLevel() > 0){
							eva.writeMessage(-6, getName() + "님이 [" + item.getEnchantLevel() + " " +item.getName() + " / "+ item.getCount() + " ] 증발");
						} else {
							eva.writeMessage(-6, getName() + "님이 [" + " " +item.getName() + " / "+ item.getCount() + " ] 증발");
						}
					}
				}else{
					getInventory().tradeItem(item, item.isStackable() ? item.getCount() : 1, L1World.getInstance().getInventory(getX(), getY(), getMapId()));
					sendPackets(new S_ServerMessage(638,item.getLogName()));
					if(item.getItem().getType2() >= 1) {
						if(item.getEnchantLevel() > 0){
							eva.writeMessage(-6, getName() + "님이 [" + item.getEnchantLevel() + " "+ item.getName() + " / "+ item.getCount() + " ] 드랍");
						} else {
							eva.writeMessage(-6, getName() + "님이 [" + " "+ item.getName() + " / "+ item.getCount() + " ] 드랍");
						}
					}
				}
			}else{				
			}
		}
	}

	private void caoPenaltySkill(int count) {
		int l = 0;
		int lv1 = 0;
		int lv2 = 0;
		int lv3 = 0;
		int lv4 = 0;
		int lv5 = 0;
		int lv6 = 0;
		int lv7 = 0;
		int lv8 = 0;
		int lv9 = 0;
		int lv10 = 0;
		Random random = new Random();
		int lostskilll = 0;
		for (int i = 0; i < count; i++) {
			if (isCrown()) {	
				lostskilll = random.nextInt(16) + 1;
			} else if (isKnight()) {	
				lostskilll = random.nextInt(8) + 1;
			} else if (isElf()) {	
				lostskilll = random.nextInt(48) + 1;
			} else if (isDarkelf()) {	
				lostskilll = random.nextInt(23) + 1;
			} else if (isWizard()) {	
				lostskilll = random.nextInt(80) + 1;
			}

			if (!SkillsTable.getInstance().spellCheck(getId(), lostskilll)) {
				return;
			}

			L1Skills l1skills = null;
			l1skills = SkillsTable.getInstance().getTemplate(lostskilll);
			if (l1skills.getSkillLevel() == 1) 	{lv1 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 2) 	{lv2 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 3) 	{lv3 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 4) 	{lv4 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 5) 	{lv5 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 6) 	{lv6 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 7) 	{lv7 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 8) 	{lv8 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 9) 	{lv9 |= l1skills.getId();}
			if (l1skills.getSkillLevel() == 10) {lv10 |= l1skills.getId();}

			SkillsTable.getInstance().spellLost(getId(), lostskilll);
			l = lv1 + lv2 + lv3 + lv4 + lv5 + lv6 + lv7 + lv8 + lv9 + lv10;
		}
		if (l > 0) {
			sendPackets(new S_DelSkill(lv1, lv2, lv3, lv4, lv5, lv6, lv7, lv8, lv9, lv10,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
		}
	}

	public boolean castleWarResult() {
		if (getClanid() != 0 && isCrown()) { 
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			for (L1War war : L1World.getInstance().getWarList()) {
				int warType = war.GetWarType();
				boolean isInWar = war.CheckClanInWar(getClanname());
				boolean isAttackClan = war.CheckAttackClan(getClanname());
				if (getId() == clan.getLeaderId() && warType == 1 && isInWar && isAttackClan) {
					String enemyClanName = war.GetEnemyClanName(getClanname());
					if (enemyClanName != null) {
						if(war.GetWarType() == 1){//공성전일경우
							L1PcInstance clan_member[] = clan.getOnlineClanMember();//
							int castle_id = war.GetCastleId();
							int[] loc = new int[3];
							loc = L1CastleLocation.getGetBackLoc(castle_id);
							int locx = loc[0];
							int locy = loc[1];
							short mapid = (short) loc[2];
							for (int k = 0; k < clan_member.length; k++) {
								if (L1CastleLocation.checkInWarArea(castle_id, clan_member[k])) {// 기내에 있는 혈원 강제 텔레포트
									L1Teleport.teleport(clan_member[k], locx, locy, mapid, 5, true);
								}
							}
						}
						war.CeaseWar(getClanname(), enemyClanName); // 종결
					}  
					break;
				}
			}
		}

		int castleId = 0;
		boolean isNowWar = false;
		castleId = L1CastleLocation.getCastleIdByArea(this);
		if (castleId != 0) { 
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		return isNowWar;
	}

	public boolean simWarResult(L1Character lastAttacker) {
		if (getClanid() == 0) { 
			return false;
		}
		if (Config.SIM_WAR_PENALTY) { 
			return false;
		}
		L1PcInstance attacker = null;
		String enemyClanName = null;
		boolean sameWar = false;

		if (lastAttacker instanceof L1PcInstance) {
			attacker = (L1PcInstance) lastAttacker;
		} else if (lastAttacker instanceof L1PetInstance) {
			attacker = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
		} else if (lastAttacker instanceof L1SummonInstance) {
			attacker = (L1PcInstance) ((L1SummonInstance) lastAttacker).getMaster();
		} else {
			return false;
		}
		L1Clan clan = null;
		for (L1War war : L1World.getInstance().getWarList()) {
			clan = L1World.getInstance().getClan(getClanname());

			int warType = war.GetWarType();
			boolean isInWar = war.CheckClanInWar(getClanname());
			if (attacker != null && attacker.getClanid() != 0) { 
				sameWar = war.CheckClanInSameWar(getClanname(), attacker.getClanname());
			}

			if (getId() == clan.getLeaderId() && warType == 2 && isInWar == true) {
				enemyClanName = war.GetEnemyClanName(getClanname());
				if (enemyClanName != null) {
					war.CeaseWar(getClanname(), enemyClanName); 
				}
			}

			if (warType == 2 && sameWar) {
				return true;
			}
		}
		return false;
	}

	public void resExp() {
		int oldLevel = getLevel();
		int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
		int exp = 0;
		double ratio;

		if (oldLevel < 45) 			ratio = 0.05;
		else if (oldLevel >= 49) 	ratio = 0.025;
		else						ratio = 0.05 - (oldLevel - 44) * 0.005;

		exp = (int) (needExp * ratio);

		if (exp == 0) return;

		addExp(exp);
	}

	public void resExpToTemple() {
		int oldLevel = getLevel();
		int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
		int exp = 0;
		double ratio;

		if (oldLevel < 45) 							ratio = 0.05;
		else if (oldLevel >= 45 && oldLevel < 49) 	ratio = 0.05  - (oldLevel - 44) * 0.005;
		else if (oldLevel >= 49 && oldLevel < 52) 	ratio = 0.025;  
		else if (oldLevel == 52)    				ratio = 0.026;  
		else if (oldLevel >  52 && oldLevel < 74)   ratio = 0.0450;	/*ratio = 0.026 + (oldLevel - 52) * 0.001; */
		else if (oldLevel >= 74 && oldLevel < 79)   ratio = 0.0450;
		else /*if (oldLevel >= 79)*/    			ratio = 0.0450; // 79렙부터 4.5%복구

		exp = (int) (needExp * ratio);
		if (exp == 0) return;

		addExp(exp);
	}


	public void deathPenalty() {

		// 바포
		if(isDisMissLevel10()) return;
		//

		int oldLevel = getLevel();
		int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
		int exp = 0;

		if (oldLevel >= 1 && oldLevel < 11) 		exp = 0;
		else if (oldLevel >= 11 && oldLevel < 45) 	exp = (int) (needExp * 0.1);
		else if (oldLevel == 45) 					exp = (int) (needExp * 0.09);
		else if (oldLevel == 46) 					exp = (int) (needExp * 0.08);
		else if (oldLevel == 47) 					exp = (int) (needExp * 0.07);
		else if (oldLevel == 48) 					exp = (int) (needExp * 0.06);
		else if (oldLevel >= 49) 					exp = (int) (needExp * 0.05);

		if (exp == 0) return;

		addExp(-exp);
	}

	public int getEr() {
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STRIKER_GALE)) {
			return 0;
		}

		int er = 0;
		if (isKnight()) {						
			er = getLevel() / 4; 
		} else if (isCrown() || isElf()) {		
			er = getLevel() / 8; 
		} else if (isDragonknight()) {			
			er = getLevel() / 7; 
		} else if (isDarkelf()) {				
			er = getLevel() / 6; 
		} else if (isIllusionist()) {			
			er = getLevel() / 9; 
		} else if (isWizard()) {				
			er = getLevel() / 10; 
		}

		er += (getAbility().getTotalDex() - 8) / 2;

		int BaseEr = CalcStat.calcBaseEr(getType(), getAbility().getBaseDex());

		er += BaseEr;

		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRESS_EVASION)) {	er += 12;}
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SOLID_CARRIAGE)) {	er += 15;}
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MIRROR_IMAGE)) {	er += 5;}

		return er;
	}

	public L1BookMark getBookMark(String name) {
		L1BookMark element = null;
		for (int i = 0; i < _bookmarks.size(); i++) {
			element = _bookmarks.get(i);
			if (element.getName().equalsIgnoreCase(name)) {
				return element;
			}
		}
		return null;
	}

	public L1BookMark getBookMark(int id) {
		L1BookMark element = null;
		for (int i = 0; i < _bookmarks.size(); i++) {
			element = _bookmarks.get(i);
			if (element.getId() == id) {
				return element;
			}
		}
		return null;
	}

	public int getBookMarkSize() {	return _bookmarks.size();	}
	public void addBookMark(L1BookMark book) {	_bookmarks.add(book);	}
	public void removeBookMark(L1BookMark book) {	_bookmarks.remove(book);	}

	public L1ItemInstance getWeapon() {	return _weapon;	}
	public void setWeapon(L1ItemInstance weapon) {	_weapon = weapon;	}

	public L1ItemInstance getArmor() {	return _armor;	}
	public void setArmor(L1ItemInstance armor) {	_armor = armor;	}

	public L1Quest getQuest() {	return _quest;	}

	public boolean isCrown() {
		return (getClassId() == CLASSID_PRINCE || getClassId() == CLASSID_PRINCESS);
	}

	public boolean isKnight() {
		return (getClassId() == CLASSID_KNIGHT_MALE || getClassId() == CLASSID_KNIGHT_FEMALE);
	}

	public boolean isElf() {
		return (getClassId() == CLASSID_ELF_MALE || getClassId() == CLASSID_ELF_FEMALE);
	}

	public boolean isWizard() {
		return (getClassId() == CLASSID_WIZARD_MALE || getClassId() == CLASSID_WIZARD_FEMALE);
	}

	public boolean isDarkelf() {
		return (getClassId() == CLASSID_DARKELF_MALE || getClassId() == CLASSID_DARKELF_FEMALE);
	}

	public boolean isDragonknight() {
		return (getClassId() == CLASSID_DRAGONKNIGHT_MALE || getClassId() == CLASSID_DRAGONKNIGHT_FEMALE);
	}

	public boolean isIllusionist() {
		return (getClassId() == CLASSID_ILLUSIONIST_MALE || getClassId() == CLASSID_ILLUSIONIST_FEMALE);
	}

	public String getAccountName() {	return _accountName;	}
	public void setAccountName(String s) {	_accountName = s;	}
	public int getBirthDay(){
		return birthday;
	}
	public void setBirthDay(int t){
		birthday = t;
	}  

	public short getBaseMaxHp() {
		return _baseMaxHp;
	}

	public void addBaseMaxHp(short i) {
		i += _baseMaxHp;
		if (i >= 32767) {
			i = 32767;
		} else if (i < 1) {
			i = 1;
		}
		addMaxHp(i - _baseMaxHp);
		_baseMaxHp = i;
	}

	public short getBaseMaxMp() {
		return _baseMaxMp;
	}

	public void addBaseMaxMp(short i) {
		i += _baseMaxMp;
		if (i >= 32767) {
			i = 32767;
		} else if (i < 0) {
			i = 0;
		}
		addMaxMp(i - _baseMaxMp);
		_baseMaxMp = i;
	}

	public int getBaseAc() {	return _baseAc;	}
	public int getBaseDmgup() {	return _baseDmgup;	}
	public int getBaseBowDmgup() {	return _baseBowDmgup;	}
	public int getBaseHitup() {	return _baseHitup;	}
	public int getBaseBowHitup() {	return _baseBowHitup;	}

	public void setBaseMagicHitUp(int i) { _baseMagicHitup = i;}	
	public int getBaseMagicHitUp() { return _baseMagicHitup;}
	public void setBaseMagicCritical(int i) { _baseMagicCritical = i;}
	public int getBaseMagicCritical() { return _baseMagicCritical;}
	public void setBaseMagicDmg(int i) { _baseMagicDmg = i;}
	public int getBaseMagicDmg() { return _baseMagicDmg;}
	public void setBaseMagicDecreaseMp(int i) { _baseMagicDecreaseMp = i;}
	public int getBaseMagicDecreaseMp() { return _baseMagicDecreaseMp;}

	public int getAdvenHp() {	return _advenHp;	}
	public void setAdvenHp(int i) {	_advenHp = i;	}

	public int getAdvenMp() {	return _advenMp;	}
	public void setAdvenMp(int i) {	_advenMp = i;	}

	public int getHighLevel() {	return _highLevel;	}
	public void setHighLevel(int i) {	_highLevel = i;	}

	public int getElfAttr() {	return _elfAttr;	}
	public void setElfAttr(int i) {	_elfAttr = i;	}

	public int getExpRes() {	return _expRes;	}
	public void setExpRes(int i) {	_expRes = i;	}

	public int getPartnerId() {	return _partnerId;	}
	public void setPartnerId(int i) {	_partnerId = i;	}

	public int getOnlineStatus() {	return _onlineStatus;	}
	public void setOnlineStatus(int i) {	_onlineStatus = i;	}

	public int getHomeTownId() {	return _homeTownId;	}
	public void setHomeTownId(int i) {	_homeTownId = i;	}

	public int getContribution() {	return _contribution;	}
	public void setContribution(int i) {	_contribution = i;	}

	public int getHellTime() {	return _hellTime;	}
	public void setHellTime(int i) {	_hellTime = i;	}

	public boolean isBanned() {	return _banned;	}
	public void setBanned(boolean flag) {	_banned = flag;	}

	public int get_food() {	return _food;	}
	public void set_food(int i) {	_food = i;	}

	public L1EquipmentSlot getEquipSlot() {	return _equipSlot;	}

	public static L1PcInstance load(String charName) {
		L1PcInstance result = null;
		try {
			result = CharacterTable.getInstance().loadCharacter(charName);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return result;
	}
	public int teleportMove = 0;

	public void save() throws Exception {
		if (isGhost()) {
			return;
		}
		CharacterTable.getInstance().storeCharacter(this);
	}

	public void saveInventory() {
		for (L1ItemInstance item : getInventory().getItems()) {
			getInventory().saveItem(item, item.getRecordingColumns());
		}
	}
	public void setRegenState(int state){
		setHpRegenState(state);
		setMpRegenState(state);
	}
	public void setHpRegenState(int state) {
		if(_HpcurPoint < state) return;

		this._HpcurPoint = state;
		//_mpRegen.setState(state);
		//_hpRegen.setState(state);
	}
	public void setMpRegenState(int state) {
		if(_MpcurPoint < state) return;

		this._MpcurPoint = state;
		//_mpRegen.setState(state);
		//_hpRegen.setState(state);
	}


	public double getMaxWeight() {
		int str = getAbility().getTotalStr();
		int con = getAbility().getTotalCon();
		int basestr = ability.getBaseStr();
		int basecon = ability.getBaseCon();
		int maxWeight = CalcStat.getMaxWeight(str, con);
		double plusWeight = 0;
		// 베이스스탯에 의한 수치(최대 무게수지 + 1 당 0.04)
		double baseWeight = CalcStat.calcBaseWeight(getType(), basestr, basecon);		
		// 방어구에 의한 수치
		double armorWeight = getWeightReduction();		
		// 인형에 의한 수치
		int dollWeight = 0; 
		for (L1DollInstance doll : getDollList().values()) {
			dollWeight = doll.getWeightReductionByDoll();
		}
		// 마법에 의한 수치
		int magicWeight = 0;
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DECREASE_WEIGHT)) { 
			magicWeight = 3;
		}
		baseWeight = Math.ceil(maxWeight * ( 1 + (baseWeight * 0.04))) - maxWeight;
		plusWeight = Math.ceil(maxWeight * ( 1 + ((armorWeight + magicWeight + dollWeight) * 0.02)))-maxWeight;
		maxWeight += plusWeight + baseWeight;
		maxWeight *= Config.RATE_WEIGHT_LIMIT; 

		return maxWeight;
	}
	public boolean isUgdraFruit() {
		return getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_FRUIT);
	}
	public boolean isFastMovable() {
		return (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.HOLY_WALK)
				|| getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MOVING_ACCELERATION)
				|| getSkillEffectTimerSet().hasSkillEffect(L1SkillId.WIND_WALK));
	}

	public boolean isBloodLust() {
		return getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLOOD_LUST);
	}

	public boolean isBrave() {
		return (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_BRAVE));
	}

	public boolean isWindShackle() { 
		return (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.WIND_SHACKLE));
	}

	public boolean isElfBrave() {
		return getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_ELFBRAVE);
	}

	public boolean isHaste() {
		return (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_HASTE)
				|| getSkillEffectTimerSet().hasSkillEffect(L1SkillId.HASTE)
				|| getSkillEffectTimerSet().hasSkillEffect(L1SkillId.GREATER_HASTE)
				|| getMoveState().getMoveSpeed() == 1);
	}

	public boolean isInvisDelay() {
		return (invisDelayCounter > 0);
	}

	public void addInvisDelayCounter(int counter) {
		synchronized (_invisTimerMonitor) {
			invisDelayCounter += counter;
		}
	}

	public void beginInvisTimer() {
		final long DELAY_INVIS = 3000L;
		addInvisDelayCounter(1);
		GeneralThreadPool.getInstance().pcSchedule(new L1PcInvisDelay(getId()), DELAY_INVIS);
	}

	public synchronized void addExp(int exp) {
		_exp += exp;
		if (_exp > ExpTable.MAX_EXP) {
			_exp = ExpTable.MAX_EXP;
		}
	}

	public synchronized void addContribution(int contribution) {
		_contribution += contribution;
	}


	public void beginExpMonitor() {
		final long INTERVAL_EXP_MONITOR = 500;
		_expMonitorFuture = GeneralThreadPool.getInstance()
				.pcScheduleAtFixedRate(new L1PcExpMonitor(getId()), 0L,	INTERVAL_EXP_MONITOR);
	}

	private void levelUp(int gap) {
		resetLevel();
		// A106 신규 혈맹 보호 시스템
		if (getLevel() >= Config.NEWUSERSAFETY_LEVEL  && 
				(getClanid() == Config.NewClan1	|| getClanid() == Config.NewClan2 || getClanid() == Config.NewClan3	|| getClanid() == Config.NewClan4)) {   // 레벨 70 이 되는순간 신규혈맹일경우
			/*
			setTitle("["+ Config.servername + "]신규탈출");
			setClanid(0);
			setClanname("");
			setClanRank(0);
			 */
			try {
				L1Clan clan = L1World.getInstance().getClan(getClanname());
				this.ClearPlayerClanData(clan);
				clan.removeClanMember(this.getName());
				this.sendPackets(new S_PacketBox(this, S_PacketBox.PLEDGE_REFRESH_MINUS));
				this.sendPackets(new S_ServerMessage(238, clan.getClanName())); // 당신은%0혈맹으로부터 추방되었습니다.
				setTitle("["+ Config.servername + "]신규탈출");
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		// A106 End
		/*
		if (getLevel() == 99 && Config.ALT_REVIVAL_POTION) {
			try {
				L1Item l1item = ItemTable.getInstance().getTemplate(43000);
				if (l1item != null) {
					getInventory().storeItem(43000, 1);
					sendPackets(new S_ServerMessage(403, l1item.getName()));
				} else {
					sendPackets(new S_SystemMessage("환생의 물약 입수에 실패했습니다."));
				}
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				sendPackets(new S_SystemMessage("환생의 물약 입수에 실패했습니다."));
			}
		}
		 */
		for (int i = 0; i < gap; i++) {
			short randomHp = CalcStat.calcStatHp(getType(), getBaseMaxHp(),	getAbility().getCon());
			short randomMp = CalcStat.calcStatMp(getType(), getBaseMaxMp(),	getAbility().getWis());
			addBaseMaxHp(randomHp);
			addBaseMaxMp(randomMp);
		}

		this.setCurrentHp(getBaseMaxHp());
		this.setCurrentMp(getBaseMaxMp());
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseAc();
		resetBaseMr();
		if (getLevel() > getHighLevel() && getReturnStat() == 0) {
			setHighLevel(getLevel());
		}
		try {
			save();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		L1Quest quest = getQuest();
		L1ItemInstance item = null;
		L1ItemInstance item1 = null;
		int lv15_step = quest.get_step(L1Quest.QUEST_LEVEL15);
		if (getLevel() >= 15 && lv15_step != L1Quest.QUEST_END) {
			switch (getType()){  // <--케릭 클래스 구분
			case 0://군주라면
				item = getInventory().storeItem(40226, 1);//트루타겟
				item1 = getInventory().storeItem(20065, 1);//붉은망토
				if ((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(15)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL15);
				break;
			case 1:
				item = getInventory().storeItem(20027, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(15)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL15); 
				break;
			case 2:
				item = getInventory().storeItem(20021, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(15)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL15); 
				break;
			case 3:
				item = getInventory().storeItem(20226, 1);
				item1 = getInventory().storeItem(126, 1);
				if ((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(15)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL15); 
				break;

			case 4:
				item = getInventory().storeItem(40598, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(15)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL15); 
				break;  

			case 5:
				item = getInventory().storeItem(L1ItemId.DRAGONKNIGHTTABLET_DRAGONSKIN, 1);
				item1 = getInventory().storeItem(410002, 1);
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(15)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL15); 
				break;  

			case 6:
				item = getInventory().storeItem(L1ItemId.MEMORIALCRYSTAL_CUBE_IGNITION, 1);
				item1 = getInventory().storeItem(410005, 1);
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(15)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL15); 
				break;  
			}
		}
		int lv30_step = quest.get_step(L1Quest.QUEST_LEVEL30);
		if (getLevel() >= 30 && lv30_step != L1Quest.QUEST_END) {
			switch (getType()){  // <--케릭 클래스 구분
			case 0:
				item = getInventory().storeItem(40570, 1); //콜클랜+군주의위엄
				item1 = getInventory().storeItem(40227, 1); //글로잉오라
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(30)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL30); 
				break;

			case 1:
				item = getInventory().storeItem(20230, 1);
				item1 = getInventory().storeItem(30, 1);
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(30)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL30); 
				break;

			case 2:
				item = getInventory().storeItem(40588, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(30)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL30); 
				break;
			case 3:
				item = getInventory().storeItem(115, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(30)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL30); 
				break;

			case 4:
				item = getInventory().storeItem(40545, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(30)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL30); 
				break;       

			case 5:
				item = getInventory().storeItem(L1ItemId.DRAGONKNIGHTTABLET_BLOODLUST, 1);
				item1 = getInventory().storeItem(420001, 1);
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(30)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL30); 
				break; 

			case 6:
				item = getInventory().storeItem(L1ItemId.MEMORIALCRYSTAL_CUBE_SHOCK, 1);
				item1 = getInventory().storeItem(420006, 1);
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(30)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL30); 
				break; 
			}
		}
		int lv45_step = quest.get_step(L1Quest.QUEST_LEVEL45);
		if (getLevel() >= 45 && lv45_step != L1Quest.QUEST_END) {
			switch (getType()){  // <--케릭 클래스 구분
			case 0:
				item = getInventory().storeItem(40229, 1);//샤이닝오라
				item1 = getInventory().storeItem(40231, 1);//런클렌

				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(45)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL45); 
				break;

			case 1:
				item = getInventory().storeItem(20318, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(45)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL45);
				break;

			case 2:
				item = getInventory().storeItem(40546, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(45)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL45);
				break;

			case 3:
				item = getInventory().storeItem(40599, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(45)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL45);
				break;

			case 4:
				item = getInventory().storeItem(40553, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(45)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL45);
				break; 

			case 5:
				item = getInventory().storeItem(420004, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(45)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL45);
				break; 

			case 6:
				item = getInventory().storeItem(420005, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(45)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL45);
				break; 
			}
		}
		int lv50_step = quest.get_step(L1Quest.QUEST_LEVEL50);
		if (getLevel() >= 50 && lv50_step != L1Quest.QUEST_END) {
			switch (getType()){  // <--케릭 클래스 구분
			case 0:
				item = getInventory().storeItem(51, 1);
				item1 = getInventory().storeItem(40230, 1);//브레이브오라
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(50)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL50);
				break;

			case 1:
				item = getInventory().storeItem(56, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(50)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL50);
				break;

			case 2:
				item = getInventory().storeItem(184, 1);
				item1 = getInventory().storeItem(50, 1);
				if((item != null) && (item1 != null))
					//	sendPackets(new S_SystemMessage("Level(50)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL50);
				break;

			case 3:
				item = getInventory().storeItem(20225, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(50)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL50);
				break;

			case 4:
				item = getInventory().storeItem(13, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(50)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL50);
				break; 

			case 5:
				item = getInventory().storeItem(410000, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(50)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL50);
				break;

			case 6:
				item = getInventory().storeItem(410003, 1);
				if(item != null)
					//	sendPackets(new S_SystemMessage("Level(50)퀘스트를 완료 하였습니다.")); 
					getQuest().set_end(L1Quest.QUEST_LEVEL50);
				break;
			}
		}
		if (Config.GAME_SERVER_TYPE == 1){
			int lv70_step = quest.get_step(L1Quest.QUEST_LEVEL70);
			if (getLevel() >= 70 && lv70_step != L1Quest.QUEST_END) {
				Beginner.getInstance().GiveTestItem(getType(), this);
				sendPackets(new S_SystemMessage("테스트 아이템 지급을 완료 하였습니다.")); 
				getQuest().set_end(L1Quest.QUEST_LEVEL70);
			}
		}
		if (getLevel() >= Config.LIMITLEVEL){
			sendPackets(new S_SystemMessage("서버 제한 레벨에 도달하여 이후 사냥하셔도 경험치를 획득이 불가능 합니다."));
		}
		if (getLevel() >= 51 && getLevel() - 50 > getAbility().getBonusAbility() && getAbility().getAmount() < 210) {
			sendPackets(new S_bonusstats(getId(), 1));
		}
		if (getLevel() >= 13) {
			if (getMapId() == 69) {
				L1Teleport.teleport(this, 33082, 33389, (short) 4, 5, true);
			} else if (getMapId() == 68) {
				L1Teleport.teleport(this, 32574, 32941, (short) 0, 5, true);
			}
		}
		if (getLevel() >= 72) { // 지정 레벨 
			if (getMapId() == 777) { // 버림받은 사람들의 땅(그림자의 신전)
				L1Teleport.teleport(this, 34043, 32184, (short) 4, 5, true); // 상아탑
			} else if (getMapId() == 778 || getMapId() == 779) { // 버림받은 사람들의 땅(욕망의 동굴)
				L1Teleport.teleport(this, 32608, 33178, (short) 4, 5, true); // WB
			}
		}
		if (getLevel() >= 49 && getAinHasad() == 0) {
			setAinHasad(2000000);
			sendPackets(new S_PacketBox(S_PacketBox.AINHASAD, getAinHasad()));
		}

		CheckStatus();
		sendPackets(new S_OwnCharStatus(this));
		sendPackets(new S_SPMR(this));
	}

	private void levelDown(int gap) {
		resetLevel();

		for (int i = 0; i > gap; i--) {
			short randomHp = CalcStat.calcStatHp(getType(), 0, getAbility().getCon());
			short randomMp = CalcStat.calcStatMp(getType(), 0, getAbility().getWis());
			addBaseMaxHp((short) -randomHp);
			addBaseMaxMp((short) -randomMp);
		}
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseAc();
		resetBaseMr();
		if (!isGm() && Config.LEVEL_DOWN_RANGE != 0) {
			if (getHighLevel() - getLevel() >= Config.LEVEL_DOWN_RANGE) {
				//		Account.ban(getAccountName());
				sendPackets(new S_ServerMessage(64));
				sendPackets(new S_Disconnect());
				_log.info(String.format("[%s]: 렙따 범위를 넘었기 때문에 강제 절단 했습니다.", getName()));
			}
		}
		try {
			save();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		sendPackets(new S_OwnCharStatus(this));
	}

	public void beginGameTimeCarrier() {
		new GameTimeCarrier(this).start();
	}

	public boolean isGhost() {	return _ghost;	}
	private void setGhost(boolean flag) {	_ghost = flag;	}

	public boolean isReserveGhost() {	return _isReserveGhost;	}
	private void setReserveGhost(boolean flag) {	_isReserveGhost = flag;	}

	public void beginGhost(int locx, int locy, short mapid, boolean canTalk) {
		beginGhost(locx, locy, mapid, canTalk, 0);
	}

	public void beginGhost(int locx, int locy, short mapid, boolean canTalk, int sec) {
		if (isGhost()) {
			return;
		}
		setGhost(true);
		_ghostSaveLocX = getX();
		_ghostSaveLocY = getY();
		_ghostSaveMapId = getMapId();
		_ghostSaveHeading = getMoveState().getHeading();
		L1Teleport.teleport(this, locx, locy, mapid, 5, true);
		if (sec > 0) {
			_ghostFuture = GeneralThreadPool.getInstance().pcSchedule(new L1PcGhostMonitor(getId()), sec * 1000);
		}
	}

	public void makeReadyEndGhost() {
		setReserveGhost(true);
		L1Teleport.teleport(this, _ghostSaveLocX, _ghostSaveLocY, _ghostSaveMapId, _ghostSaveHeading, true);
	}

	public void DeathMatchEndGhost() {
		endGhost();
		L1Teleport.teleport(this, 32614, 32735, (short) 4, 5, true);
	}


	public void endGhost() {
		setGhost(false);
		setReserveGhost(false);
	}

	public void beginHell(boolean isFirst) {
		if (getMapId() != 666) {
			int locx = 32701;
			int locy = 32777;
			short mapid = 666;
			L1Teleport.teleport(this, locx, locy, mapid, 5, false);
		}

		if (isFirst) {
			if (get_PKcount() <= 10) {
				setHellTime(180);
			} else {
				setHellTime(300 * (get_PKcount() - 100) + 300);
			}
			sendPackets(new S_BlueMessage(552, String.valueOf(get_PKcount()),
					String.valueOf(getHellTime() / 60)));
		} else {
			sendPackets(new S_BlueMessage(637, String.valueOf(getHellTime())));
		}
		if (_hellFuture == null) {
			_hellFuture = GeneralThreadPool.getInstance()
					.pcScheduleAtFixedRate(new L1PcHellMonitor(getId()), 0L,
							1000L);
		}
	}

	public void endHell() {
		if (_hellFuture != null) {
			_hellFuture.cancel(false);
			_hellFuture = null;
		}
		int[] loc = L1TownLocation.getGetBackLoc(L1TownLocation.TOWNID_ORCISH_FOREST);
		L1Teleport.teleport(this, loc[0], loc[1], (short) loc[2], 5, true);
		try {
			save();
		} catch (Exception ignore) {}
	}

	@Override
	public void setPoisonEffect(int effectId) {
		sendPackets(new S_Poison(getId(), effectId));
		if (!isGmInvis() && !isGhost() && !isInvisble()) {
			Broadcaster.broadcastPacket(this, new S_Poison(getId(), effectId));
		}
	}

	@Override
	public void healHp(int pt) {
		super.healHp(pt);
		sendPackets(new S_HPUpdate(this));
	}

	@Override
	public int getKarma() {
		return _karma.get();
	}

	@Override
	public void setKarma(int i) {
		_karma.set(i);
	}

	public void addKarma(int i) {
		synchronized (_karma) {
			_karma.add(i);
			sendPackets(new S_PacketBox(this, S_PacketBox.KARMA));
		}
	}

	public int getKarmaLevel() { return _karma.getLevel(); }
	public int getKarmaPercent() { return _karma.getPercent(); }

	public Timestamp getLastPk() { return _lastPk; }
	public void setLastPk(Timestamp time) {	_lastPk = time;	}
	public void setLastPk() {
		_lastPk = new Timestamp(System.currentTimeMillis());
	}

	public boolean isWanted() {
		if (_lastPk == null) {
			return false;
			//} else if (System.currentTimeMillis() - _lastPk.getTime() > 24 * 3600 * 1000) {
		} else if (System.currentTimeMillis() - _lastPk.getTime() > 100) {
			setLastPk(null);
			return false;
		}
		return true;
	}

	public Timestamp getDeleteTime() { return _deleteTime; }
	public void setDeleteTime(Timestamp time) {	_deleteTime = time;	}

	public int getWeightReduction() { return _weightReduction; }
	public void addWeightReduction(int i) {	_weightReduction += i;	}

	public int getHasteItemEquipped() { return _hasteItemEquipped; }
	public void addHasteItemEquipped(int i) { _hasteItemEquipped += i; }

	public void removeHasteSkillEffect() {
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SLOW)) 			getSkillEffectTimerSet().removeSkillEffect(L1SkillId.SLOW);
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MASS_SLOW)) 		getSkillEffectTimerSet().removeSkillEffect(L1SkillId.MASS_SLOW);
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ENTANGLE)) 		getSkillEffectTimerSet().removeSkillEffect(L1SkillId.ENTANGLE);
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.HASTE)) 			getSkillEffectTimerSet().removeSkillEffect(L1SkillId.HASTE);
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.GREATER_HASTE))	getSkillEffectTimerSet().removeSkillEffect(L1SkillId.GREATER_HASTE);
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_HASTE)) 	getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_HASTE);
	}

	public void resetBaseDmgup() {
		int newBaseDmgup = 0;
		int newBaseBowDmgup = 0;
		int newBaseStatDmgup = CalcStat.calcBaseDmgup(getType(), getAbility().getBaseStr());
		int newBaseStatBowDmgup = CalcStat.calcBaseBowDmgup(getType(), getAbility().getBaseDex());
		if (isKnight() || isDarkelf() || isDragonknight()) {
			newBaseDmgup = getLevel() / 10;
			newBaseBowDmgup = 0;
		} else if (isElf()) { 
			newBaseDmgup = 0;
			newBaseBowDmgup = getLevel() / 10;
		}
		addDmgup((newBaseDmgup + newBaseStatDmgup) - _baseDmgup);
		addBowDmgup((newBaseBowDmgup + newBaseStatBowDmgup) - _baseBowDmgup);
		_baseDmgup = newBaseDmgup + newBaseStatDmgup;
		_baseBowDmgup = newBaseBowDmgup + newBaseStatBowDmgup;
	}

	public void resetBaseHitup() {
		int newBaseHitup = 0;
		int newBaseBowHitup = 0;
		int newBaseStatHitup = CalcStat.calcBaseHitup(getType(), getAbility().getBaseStr());
		int newBaseStatBowHitup = CalcStat.calcBaseBowHitup(getType(), getAbility().getBaseDex());

		if (isCrown()) { 
			newBaseHitup = getLevel() / 4;
			//newBaseBowHitup = getLevel() / 5;
		} else if (isKnight()) { 
			newBaseHitup = getLevel() / 3;
			//newBaseBowHitup = getLevel() / 3;
		} else if (isElf()) { 
			newBaseHitup = getLevel() / 5;
			newBaseBowHitup = getLevel() / 5;
		} else if (isDarkelf()) { 
			newBaseHitup = getLevel() / 3;
			//newBaseBowHitup = getLevel() / 3;
		} else if (isDragonknight()) { 
			newBaseHitup = getLevel() / 4;
			//newBaseBowHitup = getLevel() / 3;
		} else if (isIllusionist()) { 
			newBaseHitup = getLevel() / 5;
			//newBaseBowHitup = getLevel() / 5;
		}
		addHitup((newBaseHitup + newBaseStatHitup) - _baseHitup);
		addBowHitup((newBaseBowHitup + newBaseStatBowHitup) - _baseBowHitup);
		_baseHitup = newBaseHitup + newBaseStatHitup;
		_baseBowHitup = newBaseBowHitup + newBaseStatBowHitup;		
	}

	public void resetBaseAc() {
		int newAc = CalcStat.calcAc(getLevel(), getAbility().getDex());
		int newbaseAc = CalcStat.calcBaseAc(getType() ,getAbility().getBaseDex());
		this.ac.addAc((newAc + newbaseAc) - _baseAc);
		_baseAc = newAc + newbaseAc;
	}

	public void resetBaseMr() {
		//int newBaseMr = CalcStat.calcBaseMr(getType(), getAbility().getBaseWis());
		int newMr = 0;

		if (isCrown())  			newMr = 10;
		else if (isElf()) 			newMr = 25;
		else if (isWizard()) 		newMr = 15;
		else if (isDarkelf()) 		newMr = 10;
		else if (isDragonknight()) 	newMr = 18;
		else if (isIllusionist())	newMr = 20;
		newMr += CalcStat.calcStatMr(getAbility().getTotalWis()); 
		newMr += getLevel() / 2;
		resistance.setBaseMr(newMr);
	}

	public void resetLevel() {
		setLevel(ExpTable.getLevelByExp(_exp));
		updateLevel();
	}
	public void updateLevel() {
		final int lvlTable[] = new int[] { 30, 25, 20, 16, 14, 12, 11, 10, 9, 3, 2 };

		int regenLvl = Math.min(10, getLevel());
		if (30 <= getLevel() && isKnight()) {
			regenLvl = 11;
		}

		synchronized (this) {
			setHpregenMax(lvlTable[regenLvl - 1] * 4);
		}
	}
	public void refresh() {
		CheckChangeExp();
		resetLevel();
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseMr();
		resetBaseAc();
		setBaseMagicDecreaseMp(CalcStat.calcBaseDecreaseMp(getType(), getAbility().getBaseInt()));
		setBaseMagicHitUp(CalcStat.calcBaseMagicHitUp(getType(), getAbility().getBaseInt()));
		setBaseMagicCritical(CalcStat.calcBaseMagicCritical(getType(), getAbility().getBaseInt()));
		setBaseMagicDmg(CalcStat.calcBaseMagicDmg(getType(), getAbility().getBaseInt()));
	}

	public void checkChatInterval() {
		long nowChatTimeInMillis = System.currentTimeMillis();
		if (_chatCount == 0) {
			_chatCount++;
			_oldChatTimeInMillis = nowChatTimeInMillis;
			return;
		}

		long chatInterval = nowChatTimeInMillis - _oldChatTimeInMillis;
		if (chatInterval > 2000) {
			_chatCount = 0;
			_oldChatTimeInMillis = 0;
		} else {
			if (_chatCount >= 3) {
				getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_CHAT_PROHIBITED, 120 * 1000);
				sendPackets(new S_SkillIconGFX(36, 120));
				sendPackets(new S_ServerMessage(153));
				_chatCount = 0;
				_oldChatTimeInMillis = 0;
			}
			_chatCount++;
		}
	}

	// 범위외가 된 인식이 끝난 오브젝트를 제거(버경)
	private void removeOutOfRangeObjects(int distance) {
		try{
			List<L1Object> known = getNearObjects().getKnownObjects();
			for (int i = 0; i < known.size(); i++) {
				if (known.get(i) == null) {
					continue;
				}

				L1Object obj = known.get(i);
				if (! getLocation().isInScreen(obj.getLocation())) { // 범위외가 되는 거리
					getNearObjects().removeKnownObject(obj);
					sendPackets(new S_RemoveObject(obj));
				}
			}
		}catch(Exception e){
			System.out.println("removeOutOfRangeObjects 에러 : "+e);
		}
	}

	// 오브젝트 인식 처리(버경)
	public void UpdateObject() {
		try{
			if(this == null)
				return;
			try{
				removeOutOfRangeObjects(17);
			}catch(Exception e){
				System.out.println("removeOutOfRangeObjects(17) 에러 : "+e);
			}

			// 화면내의 오브젝트 리스트를 작성 
			ArrayList<L1Object> visible2 = L1World.getInstance().getVisibleObjects(this);
			L1NpcInstance npc = null;
			for (L1Object visible : visible2){
				if(this == null){
					break;
				}
				if(visible == null){
					continue;
				}
				if (! getNearObjects().knownsObject(visible)) {
					visible.onPerceive(this);
				} else {
					if (visible instanceof L1NpcInstance) {
						npc = (L1NpcInstance) visible;
						if (npc.getHiddenStatus() != 0) {
							npc.approachPlayer(this);
						}
					}

				}
			}
		}catch(Exception e){
			System.out.println("UpdateObject() 에러 : "+e);
		}
	}

	public void CheckChangeExp() {
		int level = ExpTable.getLevelByExp(getExp());
		int char_level = CharacterTable.getInstance().PcLevelInDB(getId());
		if(char_level == 0){ // 0이라면..에러겟지?
			return; // 그럼 그냥 리턴
		}
		int gap = level - char_level;
		if (gap == 0) {
			// sendPackets(new S_OwnCharStatus(this));
			sendPackets(new S_Exp(this));
			return;
		}

		// 레벨이 변화했을 경우
		if (gap > 0) {
			levelUp(gap);
		} else if (gap < 0) {
			levelDown(gap);
		}
	}

	public void CheckStatus(){
		if(!getAbility().isNormalAbility(getClassId(), getLevel(), getHighLevel(),getAbility().getBaseAmount()) && !isGm()) {
			SpecialEventHandler.getInstance().ReturnStats(this);
		}
	}

	public void cancelAbsoluteBarrier() { // 아브소르트바리아의 해제
		if (this.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) {
			this.getSkillEffectTimerSet().killSkillEffectTimer(ABSOLUTE_BARRIER);
			//this.startHpRegeneration();
			//this.startMpRegeneration();
			this.startHpRegenerationByDoll();
			this.startMpRegenerationByDoll();
		}
	}


	public int get_PKcount() {		return _PKcount;	}
	public void set_PKcount(int i) {		_PKcount = i;	}
	// 킬데스 초기화 횟수
	public int get_KillDeathInitialize() {		return _KillDeathInitialize;	}
	public void set_KillDeathInitialize(int i) {		_KillDeathInitialize = i;	}

	/////////////////////////////검색////////////////////////////////////////


	public int get_autoct() {  return _autoct; } // 오토인증추가
	public void set_autoct(int i) {  _autoct = i; }

	public int get_autogo() {  return _autogo; } // 오토인증추가
	public void set_autogo(int i) {  _autogo = i; }

	public String get_autocode() {  return _autocode; } // 오토인증추가
	public void set_autocode(String s) {  _autocode = s; }

	public int get_autook() {  return _autook; } // 오토인증추가
	public void set_autook(int i) {  _autook = i; }

	////////////////////////여기까지 추가///////////////

	public int getClanid() {		return _clanid;	}
	public void setClanid(int i) {		_clanid = i;	}
	public String getClanname() {		return clanname;	}
	public void setClanname(String s) {		clanname = s;	}
	public L1Clan getClan() {		return L1World.getInstance().getClan(getClanname());	}
	public int getClanRank() {		return _clanRank;	}
	public void setClanRank(int i) {		_clanRank = i;	}
	public byte get_sex() {		return _sex;	}
	public void set_sex(int i) {		_sex = (byte) i;	}


	private int huntCount;
	private int huntPrice;
	private String _reasontohunt;
	public String getReasonToHunt() {		return _reasontohunt;	}
	public void setReasonToHunt(String s) {		_reasontohunt = s;	}

	public int getHuntCount() {		  return huntCount;		 }
	public void setHuntCount(int i) {		  huntCount = i;		 }

	public int getHuntPrice() {		  return huntPrice;		 }
	public void setHuntPrice(int i) {		  huntPrice = i;		 }


	public boolean isGm() {		return _gm;	}
	public void setGm(boolean flag) {		_gm = flag;	}
	/**여기부터 드래곤진주*/
	public boolean isThirdSpeed() {
		return (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_DRAGONPERL)
				|| get진주속도() == 1);//;;;;
	}
	private int _진주속도; // ● 진주 상태 0.통상 1.치우침 이브

	public int get진주속도() {
		return _진주속도;
	}

	public void set진주속도(int i) {
		_진주속도= i;
	}
	public boolean isMonitor() {		return _monitor;	}
	public void setMonitor(boolean flag) {		_monitor = flag;	}

	public int getDamageReductionByArmor() { return _damageReductionByArmor; }
	public void addDamageReductionByArmor(int i) { _damageReductionByArmor += i; }

	public int getBowHitupByArmor() { return _bowHitupByArmor; }
	public void addBowHitupByArmor(int i) { _bowHitupByArmor += i; }

	public int getBowDmgupByArmor() { return _bowDmgupByArmor; }
	public void addBowDmgupByArmor(int i) { _bowDmgupByArmor += i; }

	public int getHitupByArmor() { return _HitupByArmor; }
	public void addHitupByArmor(int i) {	_HitupByArmor += i; }

	public int getDmgupByArmor() { return _DmgupByArmor; }
	public void addDmgupByArmor(int i) {	_DmgupByArmor += i; }

	public int getBowHitupByDoll() { return _bowHitupBydoll; }
	public void addBowHitupByDoll(int i) { _bowHitupBydoll += i; }

	public int getBowDmgupByDoll() { return _bowDmgupBydoll; }
	public void addBowDmgupByDoll(int i) { _bowDmgupBydoll += i; }

	private void setGresValid(boolean valid) { _gresValid = valid; }
	public boolean isGresValid() { return _gresValid; }

	public long getFishingTime() { return _fishingTime;	}
	public void setFishingTime(long i) { _fishingTime = i; }
	public boolean isFishing() { return _isFishing; }
	public boolean isFishingReady() { return _isFishingReady; }
	public void setFishing(boolean flag) { _isFishing = flag; }
	public void setFishingReady(boolean flag) {	_isFishingReady = flag;	}

	public int getCookingId() {	return _cookingId;	}
	public void setCookingId(int i) { _cookingId = i; }

	public int getDessertId() {	return _dessertId;	}
	public void setDessertId(int i) { _dessertId = i; }

	public L1ExcludingList getExcludingList() {	return _excludingList;	}
	public AcceleratorChecker getAcceleratorChecker() {	return _acceleratorChecker;	}

	public int getTeleportX() {	return _teleportX; }
	public void setTeleportX(int i) { _teleportX = i; }

	public int getTeleportY() {	return _teleportY;	}
	public void setTeleportY(int i) { _teleportY = i; }

	public short getTeleportMapId() { return _teleportMapId; }
	public void setTeleportMapId(short i) {	_teleportMapId = i; }

	public int getTeleportHeading() { return _teleportHeading; }
	public void setTeleportHeading(int i) {	_teleportHeading = i; }

	public int getTempCharGfxAtDead() { return _tempCharGfxAtDead; }
	public void setTempCharGfxAtDead(int i) { _tempCharGfxAtDead = i; }

	public boolean isCanWhisper() {	return _isCanWhisper; }
	public void setCanWhisper(boolean flag) { _isCanWhisper = flag; }

	public boolean isShowTradeChat() { return _isShowTradeChat;	}
	public void setShowTradeChat(boolean flag) { _isShowTradeChat = flag; }

	public boolean isShowWorldChat() { return _isShowWorldChat; }
	public void setShowWorldChat(boolean flag) { _isShowWorldChat = flag; }

	public int getFightId() { return _fightId; }
	public void setFightId(int i) { _fightId = i; }

	public boolean isPetRacing() { return petRacing; }
	public void setPetRacing(boolean Petrace) { this.petRacing = Petrace; }
	public int getPetRacingLAB() { return petRacingLAB; }
	public void setPetRacingLAB(int lab) { this.petRacingLAB = lab; }
	public int getPetRacingCheckPoint() { return petRacingCheckPoint; }
	public void setPetRacingCheckPoint(int p) { this.petRacingCheckPoint = p; }

	public void setHaunted(boolean i){ this.isHaunted = i; }
	public boolean isHaunted(){ return isHaunted; }

	public void setDeathMatch(boolean i){ this.isDeathMatch = i; }
	public boolean isDeathMatch(){ return isDeathMatch; }

	public int getCallClanId() { return _callClanId; }
	public void setCallClanId(int i) { _callClanId = i; }

	public int getCallClanHeading() { return _callClanHeading; 	}
	public void setCallClanHeading(int i) {	_callClanHeading = i;	}
	private boolean _isSummonMonster = false;

	public void setSummonMonster(boolean SummonMonster) {_isSummonMonster = SummonMonster;}
	public boolean isSummonMonster() {return _isSummonMonster;}

	private boolean _isShapeChange = false;
	public void setShapeChange(boolean isShapeChange) {_isShapeChange = isShapeChange;}
	public boolean isShapeChange() {return _isShapeChange;}

	private boolean _isArchShapeChange = false;
	public void setArchShapeChange(boolean isArchShapeChange) {_isArchShapeChange = isArchShapeChange;}
	public boolean isArchShapeChange() {return _isArchShapeChange;}

	private boolean _isArchPolyType = true;		// t 1200 f -1 
	public void setArchPolyType(boolean isArchPolyType) {_isArchPolyType = isArchPolyType;}
	public boolean isArchPolyType() {return _isArchPolyType;}

	public int getUbScore() { return _ubscore; }
	public void setUbScore(int i) { _ubscore = i; }

	private int _girandungeon;
	/** 기란던전 입장되어 있던 시간값을 가져 온다. 단위 : 1분*/
	public int getGdungeonTime() { return _girandungeon; }
	public void setGdungeonTime(int i) { _girandungeon = i; }

	private int _lasdungeon;
	/** 라스타바드 던전 입장되어 있던 시간값을 가져 온다. 단위 : 1분*/
	public int getLdungeonTime() { return _lasdungeon; }
	public void setLdungeonTime(int i) { _lasdungeon = i; }

	private int _ivorytower;
	/** 상아탑 입장되어 있던 시간값을 가져 온다. 단위 : 1분*/
	public int getIvoryTowerTime() { return _ivorytower; }
	public void setIvoryTowerTime(int i) { _ivorytower = i; }
	/** 깃털 지급 시간을 센다 **/
	private int _timeCount = 0;
	public int getTimeCount() { return _timeCount; }
	public void setTimeCount(int i) { _timeCount = i; }

	/** 해츨링 인형 (응집된 화합물) 지급 시간을 센다 **/
	private int _featherCount = 0;
	public int getFeatherCount() { return _featherCount; }
	public void setFeatherCount(int i) { _featherCount = i; }
	
	private boolean _isPointUser;
	/** 계정 시간이 남은 Pc 인지 판단한다 */
	public boolean isPointUser() { return _isPointUser; }
	public void setPointUser(boolean i) { _isPointUser = i; }

	private long _limitPointTime;
	public long getLimitPointTime() { return _limitPointTime; }
	public void setLimitPointTime(long i) { _limitPointTime = i; }

	private int _safecount = 0;
	public int getSafeCount() { return _safecount; }
	public void setSafeCount(int i) { _safecount = i; }

	private Timestamp _logoutTime;
	public Timestamp getLogOutTime() { return _logoutTime; }
	public void setLogOutTime(Timestamp t) { _logoutTime = t; }
	public void setLogOutTime() { _logoutTime = new Timestamp(System.currentTimeMillis()); }

	private int _ainhasad;
	public int getAinHasad() { return _ainhasad; }
	public void calAinHasad(int i) {
		int calc = _ainhasad + i;	
		if (calc >= 2000000) calc = 2000000;			
		_ainhasad = calc;
	}
	public void setAinHasad(int i) { _ainhasad = i; }

	public int getSpeedHackCount() {
		return _speedhackCount;
	}

	public void setSpeedHackCount(int x) {
		_speedhackCount = x;
	}

	public int getSpeedHackX() {
		return _speedhackX;
	}

	public void setSpeedHackX(int x) {
		_speedhackX = x;
	}

	public int getSpeedHackY() {
		return _speedhackY;
	}

	public void setSpeedHackY(int y) {
		_speedhackY = y;
	}

	public short getSpeedHackMapid() {
		return _speedhackMapid;
	}

	public void setSpeedHackMapid(short mapid) {
		_speedhackMapid = mapid;
	}

	public int getSpeedHackHeading() {
		return _speedhackHeading;
	}

	public void setSpeedHackHeading(int Heading) {
		_speedhackHeading = Heading;
	}

	public int getSpeedRightInterval() {
		return _speedright;
	}

	public void setSpeedRightInterval(int r) {
		_speedright = r;
	}

	public int getSpeedInterval() {
		return _speedinterval;
	}

	public void setSpeedInterval(int i) {
		_speedinterval = i;
	}

	public void speedHackClear() {
		_speedhackHeading = 0;
		_speedhackMapid = 0;
		_speedhackX = 0;
		_speedhackY = 0;
		_speedright = 0;
		_speedinterval = 0;
	}

	/** 인챈트 버그 예외 처리 */
	private int _enchantitemid = 0;
	public int getLastEnchantItemid(){ return _enchantitemid;}	
	public void setLastEnchantItemid(int i, L1ItemInstance item){
		if (getLastEnchantItemid() == i && i != 0){
			sendPackets(new S_Disconnect());
			getInventory().removeItem(item,item.getCount());			
			return;
		}
		_enchantitemid = i;
	}

	/** 캐릭터에, pet, summon monster, tame monster, created zombie 를 추가한다. */
	public void addPet(L1NpcInstance npc) { _petlist.put(npc.getId(), npc); 	}

	/** 캐릭터로부터, pet, summon monster, tame monster, created zombie 를 삭제한다. */
	public void removePet(L1NpcInstance npc) { _petlist.remove(npc.getId()); 	}
	/** 캐릭터의 애완동물 리스트를 돌려준다. */
	public Map<Integer, L1NpcInstance> getPetList() { return _petlist; 					}
	/** 캐릭터에 doll을 추가한다. */
	public void addDoll(L1DollInstance doll) { _dolllist.put(doll.getId(), doll); 	}
	/** 캐릭터로부터 dool을 삭제한다. */
	public void removeDoll(L1DollInstance doll) { _dolllist.remove(doll.getId()); 		}	
	/** 캐릭터의 doll 리스트를 돌려준다. */
	public Map<Integer, L1DollInstance> getDollList() { return _dolllist; 					}
	/** 캐릭터에 이벤트 NPC(케릭터를 따라다니는)를 추가한다. */
	public void addFollower(L1FollowerInstance follower) { _followerlist.put(follower.getId(), follower); 	}
	/** 캐릭터로부터 이벤트 NPC(케릭터를 따라다니는)를 삭제한다. */
	public void removeFollower(L1FollowerInstance follower) { _followerlist.remove(follower.getId()); }
	/** 캐릭터의 이벤트 NPC(케릭터를 따라다니는) 리스트를 돌려준다. */
	public Map<Integer, L1FollowerInstance> getFollowerList() { return _followerlist; }

	public void ClearPlayerClanData(L1Clan clan) throws Exception {
		ClanWarehouse clanWarehouse = WarehouseManager.getInstance().getClanWarehouse(clan.getClanName());
		if(clanWarehouse != null) clanWarehouse.unlock(getId());
		setClanid(0);
		setClanname("");
		setTitle("");
		if(this != null){
			sendPackets(new S_CharTitle(getId(), ""));
			Broadcaster.broadcastPacket(this, new S_CharTitle(getId(), ""));
		}
		setClanRank(0);
		save();
	}
	private int _HpregenMax = 0;
	public int getHpregenMax(){ 
		return _HpregenMax;
	}
	public void setHpregenMax(int num){
		this._HpregenMax = num;
	}

	private int _HpregenPoint = 0;
	public int getHpregenPoint(){
		return _HpregenPoint;
	}
	public void setHpregenPoint(int num){
		this._HpregenPoint = num;
	}
	public void addHpregenPoint(int num){
		this._HpregenPoint += num;
	}

	private int _HpcurPoint = 4;
	public int getHpcurPoint(){
		return _HpcurPoint;
	}
	public void setHpcurPoint(int num){
		this._HpcurPoint = num;
	}

	private int _MpregenMax = 0;
	public int getMpregenMax(){ 
		return _MpregenMax;
	}
	public void setMpregenMax(int num){
		this._MpregenMax = num;
	}

	private int _MpregenPoint = 0;
	public int getMpregenPoint(){
		return _MpregenPoint;
	}
	public void setMpregenPoint(int num){
		this._MpregenPoint = num;
	}
	public void addMpregenPoint(int num){
		this._MpregenPoint += num;
	}

	private int _MpcurPoint = 4;
	public int getMpcurPoint(){
		return _MpcurPoint;
	}
	public void setMpcurPoint(int num){
		this._MpcurPoint = num;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////// 스피드핵 방지 //////////////////////////////////////////////////////////
	/**
	 * A110 삭제
	 **/
	/*
	private int hackTimer = -1;
	private int hackCKtime = -1;
	private int hackCKcount = 0;

	public int get_hackTimer() {
		return hackTimer;
	}

	public void increase_hackTimer() {
		if (hackTimer < 0)
			return;
		hackTimer++;
	}

	public void init_hackTimer() {
		hackTimer = 0;
		// SpeedHackController.getInstance().pc_add(this);
	}

	public void calc_hackTimer() {
		if (hackTimer < 0)
			return;
		else if (hackTimer <= 30) {
			_netConnection.close();
			this.logout();
		} else if (hackTimer <= 57) {
			if (hackCKtime < 0 || hackCKtime < hackTimer - 1
					|| hackCKtime > hackTimer + 1) {
				hackCKtime = hackTimer;
				hackCKcount = 1;
			} else {
				hackCKcount++;
				System.out.println("hackCKcount : " + hackCKcount);
				if (hackCKcount == 3) {
					_netConnection.close();
					this.logout();
				}
			}
		} else {
			hackCKtime = -1;
			hackCKcount = 0;
		}
		hackTimer = 0;
	}
	 */
	private int _old_lawful;
	private int _old_exp;

	public int getold_lawful(){
		return this._old_lawful;
	}

	public void setold_lawful(int value){
		this._old_lawful = value;
	}

	public int getold_exp(){
		return this._old_exp;
	}

	public void setold_exp(int value){
		this._old_exp = value;
	}
	public void bkteleport() {
		int nx = getX();
		int ny = getY();
		int aaa = getMoveState().getHeading();
		switch (aaa) {
		case 1:
			nx += -1;
			ny += 1;
			break;
		case 2:
			nx += -1;
			ny += 0;
			break;
		case 3:
			nx += -1;
			ny += -1;
			break;
		case 4:
			nx += 0;
			ny += -1;
			break;
		case 5:
			nx += 1;
			ny += -1;
			break;
		case 6:
			nx += 1;
			ny += 0;
			break;
		case 7:
			nx += 1;
			ny += 1;
			break;
		case 0:
			nx += 0;
			ny += 1;
			break;
		default:
			break;
		}
		L1Teleport.teleport(this, nx, ny, getMapId(), aaa, false);
	}

	/** 공속관리*/
	private double HASTE_RATE = 0.75;
	private double WAFFLE_RATE = 0.87;

	private int _taskcount = 0;

	public void TaskCountUp(){
		_taskcount++;
	}
	public void TaskCountDown(){
		_taskcount--;
	}
	public int getTaskCount(){
		return _taskcount;
	}

	private long _nextattacktime = 0;

	public void setNextAttackTime(long i) {
		_nextattacktime = i;
	}
	public long getNextAttackTime() {
		return _nextattacktime;
	}

	private long _oldattacktime = 0;

	public void setOldAttackTime(long i) {
		_oldattacktime = i;
	}
	public long getOldAttackTime() {
		return _oldattacktime;
	}

	private int _attackspeed = 0;

	public void setAttackSpeed(int i) {
		_attackspeed = i;
	}
	public int getAttackSpeed() {
		if(_attackspeed == 0)return 0;
		int interval = _attackspeed;
		if (isHaste()) {//헤이스트
			interval *= HASTE_RATE;
		}
		if(isBrave() || getSkillEffectTimerSet().hasSkillEffect(186)) {//186블러드 러스트
			if (isElf()) {
				interval *= WAFFLE_RATE;
			} else {
				interval *= HASTE_RATE;
			}
		}
		if(isElfBrave()){
			interval *= WAFFLE_RATE;
		}
		return interval;
	}
	/** 공속관리*/

	private int _gmtest;

	public int getGmTest() {
		return _gmtest;
	}

	public void setGmTest(int i) {
		_gmtest = i;
	}


	private long test1 = 0;//이전 변신
	private long test2 = 0;//이전 무기상태
	private long test3 = 0;//이전 시간
	private long test_step = 0;//테스트 스탭

	private final Map<Integer, Integer> _speedtest = new HashMap<Integer, Integer>();

	private long test_speed = 0;
	public void speed_save(){
		try {
			if((System.currentTimeMillis() - test3) > 2000){
				test_reset();
			}
			if(getGfxId().getTempCharGfx() != test1 || (getCurrentWeapon() + 1) != test2){
				test1 = getGfxId().getTempCharGfx();
				test2 = (getCurrentWeapon() + 1);
				test_reset();
			}
			test_step++;
			if(test_step > 3){
				long speed = System.currentTimeMillis() - test3;
				if(_speedtest.containsKey(speed)){
					_speedtest.put((int)speed, _speedtest.get(speed)+1);
				}else{
					_speedtest.put((int)speed, 1);
				}
				//sendPackets(new S_SystemMessage("현제 : " +(test_speed / (test_step - 3)) + "입니다."));
			}

			if(test_step == 20){
				int speed = 0;
				int mapsize = 0;
				//Object[] hashkeys = _speedtest.keySet().toArray();
				for(Integer key : _speedtest.keySet()){
					if(_speedtest.get(key) > mapsize){
						speed = key;
						mapsize = _speedtest.get(key);
					}
				}
				/*for(int i=0; i<hashkeys.length; i++) {
				String keys = (String)hashkeys[i];
				int key = Integer.valueOf(keys);
				if(_speedtest.get(key) > mapsize){
					speed = key;
					mapsize = _speedtest.get(key);
				}
			}*/
				if(UserSpeedControlTable.getInstance().getAttackSpeed(getGfxId().getTempCharGfx(), getCurrentWeapon() + 1) != 0){
					if(UserSpeedControlTable.getInstance().getAttackSpeed(getGfxId().getTempCharGfx(), getCurrentWeapon() + 1) > speed){
						sendPackets(new S_SystemMessage("속도 측정 재기록 : " +speed + "입니다.(polyid="+getGfxId().getTempCharGfx()+", type"+(getCurrentWeapon() + 1)+")"));
						Connection con = null;
						PreparedStatement pstm = null;
						try {
							con = L1DatabaseFactory.getInstance().getConnection();
							pstm = con.prepareStatement("UPDATE user_speed_control SET speed=? WHERE poly_id=? and action_type=?");
							pstm.setInt(1, (int)speed);
							pstm.setInt(2, getGfxId().getTempCharGfx());
							pstm.setInt(3, (getCurrentWeapon() + 1));
							pstm.execute();

						} catch (SQLException e) {
						} finally {
							SQLUtil.close(pstm);
							SQLUtil.close(con);
						}
					}else{
						sendPackets(new S_SystemMessage("속도 재측정 결과 : " +speed + "입니다. 이전 보다 느리거나 같아 기록되지 않았습니다.(polyid="+getGfxId().getTempCharGfx()+", type"+(getCurrentWeapon() + 1)+")"));
						test_reset();
						return;
					}
				}else{
					sendPackets(new S_SystemMessage("속도측정기록 : " +speed + "입니다.(polyid="+getGfxId().getTempCharGfx()+", type"+(getCurrentWeapon() + 1)+")"));

					Connection con = null;
					PreparedStatement pstm = null;
					try {
						con = L1DatabaseFactory.getInstance().getConnection();
						pstm = con
								.prepareStatement("INSERT INTO user_speed_control SET poly_id = ?, action_type = ?, speed = ?");
						pstm.setInt(1, getGfxId().getTempCharGfx());
						pstm.setInt(2, (getCurrentWeapon() + 1));
						pstm.setInt(3, (int)speed);
						pstm.execute();

					} catch (SQLException e) {
					} finally {
						SQLUtil.close(pstm);
						SQLUtil.close(con);
					}
				}
				UserSpeedControlTable.getInstance().putAttackSpeed(getGfxId().getTempCharGfx(), getCurrentWeapon() + 1, (int)speed);

				test_reset();
			}

			test3 =	System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void test_reset(){
		test_step = 0;
		test_speed = 0;
		_speedtest.clear();
	}

	public void addSpeedHackCount(int x) {
		_speedhackCount += x;
		eva.writeMessage(-4, "스핵(C_Attack)" + "[" + getName() + "]" + " / "+ _speedhackCount + "회" + " / " + (getCurrentWeapon() + 1) );
		L1SkillUse l1skilluse = new L1SkillUse();
		if(_speedhackCount < 0) _speedhackCount = 0;
		switch(_speedhackCount){
		case 1:	
		case 2:
			break;
		case 3:
			L1Teleport.teleport(this, getX(), getY(), getMapId(), getMoveState().getHeading(), false);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 4: 
		case 5: 
			break;
		case 6: 
			L1Teleport.teleport(this, getX(), getY(), getMapId(), getMoveState().getHeading(), false);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 7: 
		case 8: 
			break;
		case 9: 
			L1Teleport.teleport(this, getX(), getY(), getMapId(), getMoveState().getHeading(), false);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 10: 
			setCurrentHp(100);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 11:
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 12:
			setCurrentHp(1);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 13: 
		case 14:
		case 15:
			l1skilluse.handleCommands(this, L1SkillId.CANCELLATION, getId(), getX(), getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 16: case 17:
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 18: case 19: case 20:
			l1skilluse.handleCommands(this, L1SkillId.EARTH_BIND, getId(), getX(), getY(), null, 18 * 1000, L1SkillUse.TYPE_GMBUFF);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 25:
			l1skilluse.handleCommands(this, L1SkillId.EARTH_BIND, getId(), getX(), getY(), null, 36 * 1000, L1SkillUse.TYPE_GMBUFF);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 30:
			l1skilluse.handleCommands(this, L1SkillId.EARTH_BIND, getId(), getX(), getY(), null, 180 * 1000, L1SkillUse.TYPE_GMBUFF);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 35:
			l1skilluse.handleCommands(this, L1SkillId.EARTH_BIND, getId(), getX(), getY(), null, 360 * 1000, L1SkillUse.TYPE_GMBUFF);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 40:
			l1skilluse.handleCommands(this, L1SkillId.EARTH_BIND, getId(), getX(), getY(), null, 720 * 1000, L1SkillUse.TYPE_GMBUFF);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 45:
			l1skilluse.handleCommands(this, L1SkillId.EARTH_BIND, getId(), getX(), getY(), null, 1800 * 1000, L1SkillUse.TYPE_GMBUFF);
			sendPackets(new S_ServerMessage(1452)); // 불법 프로그램을 사용시 서비스 이용에 제한을 받을 수 있습니다.
			break;
		case 50: case 51: case 52: case 53: case 54: case 55: case 56: case 57: case 58: case 59: case 60:
			sendPackets(new S_ServerMessage(945));
			sendPackets(new S_ServerMessage(64));
			sendPackets(new S_Disconnect());
			break;
		default: break;
		}
	}
}
