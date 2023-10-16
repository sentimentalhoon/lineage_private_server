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
 * http://www.gnu.org/copyleft/gpl.html1
 */
package l1j.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.server.utils.IntRange;

public final class Config {
	private static final Logger _log = Logger.getLogger(Config.class.getName());

	// ���λ��� ���� �Ҳ���..
	public static boolean autoTrader = true;
	//
	public static boolean STANDBY_SERVER = false;//[0062]  // ���´��
	//# �ڵ� ��������..
	public static boolean Use_Show_Announcecycle;
	public static int Show_Announcecycle_Time;
	/** Debug/release mode */
	public static final boolean DEBUG = false;
	public static int systime;
	public static String sys1;
	public static String sys2;
	public static String sys3;
	public static String sys4;
	public static String sys5;
	public static String sys6;
	public static String sys7;

	public static final String servername = "ĭ";
	/** NewClan **/
	public static int NewClan1 = 268481136; // Heine
	public static int NewClan2 = 268481179; // Saiha
	public static int NewClan3 = 268481220; // Oren
	public static int NewClan4 = 268481302; // Jowoo
	public static final String NewClanName1 = "���̳�";
	public static final String NewClanName2 = "������";
	public static final String NewClanName3 = "����";
	public static final String NewClanName4 = "����";

	/** Thread pools size */
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int AI_MAX_THREAD;
	public static int THREAD_P_TYPE_GENERAL;
	public static int THREAD_P_SIZE_GENERAL;
	public static boolean AUTH_CONNECT; 
	/** Server control */
	public static int GAME_SERVER_TYPE;
	public static String GAME_SERVER_HOST_NAME;
	public static int GAME_SERVER_PORT;
	public static String DB_DRIVER;
	public static String DB_URL;
	public static String DB_LOGIN;
	public static String DB_PASSWORD;
	public static String TIME_ZONE;
	public static int CLIENT_LANGUAGE;
	public static boolean HOSTNAME_LOOKUPS;
	public static int AUTOMATIC_KICK;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static short MAX_ONLINE_USERS;
	public static boolean CACHE_MAP_FILES;
	public static boolean LOAD_V2_MAP_FILES;
	public static boolean CHECK_MOVE_INTERVAL;
	public static boolean CHECK_ATTACK_INTERVAL;
	public static boolean CHECK_SPELL_INTERVAL;
	public static short INJUSTICE_COUNT;
	public static int JUSTICE_COUNT;
	public static int CHECK_STRICTNESS;
	public static byte LOGGING_WEAPON_ENCHANT;
	public static byte LOGGING_ARMOR_ENCHANT;
	public static boolean LOGGING_CHAT_NORMAL;
	public static boolean LOGGING_CHAT_WHISPER;
	public static boolean LOGGING_CHAT_SHOUT;
	public static boolean LOGGING_CHAT_WORLD;
	public static boolean LOGGING_CHAT_CLAN;
	public static boolean LOGGING_CHAT_PARTY;
	public static boolean LOGGING_CHAT_COMBINED;
	public static boolean LOGGING_CHAT_CHAT_PARTY;

	public static int SPEEDUP_SPEEDMENT;
	public static int AUTOSAVE_INTERVAL;
	public static int AUTOSAVE_INTERVAL_INVENTORY;
	public static int SKILLTIMER_IMPLTYPE;
	public static int NPCAI_IMPLTYPE;
	public static boolean TELNET_SERVER;
	public static boolean isGmchat = true;
	public static int TELNET_SERVER_PORT;
	public static int PC_RECOGNIZE_RANGE;
	public static boolean CHARACTER_CONFIG_IN_SERVER_SIDE;
	public static boolean ALLOW_2PC;
	public static int LEVEL_DOWN_RANGE;
	public static boolean SEND_PACKET_BEFORE_TELEPORT;
	public static boolean DETECT_DB_RESOURCE_LEAKS;

	/** Rate control */
	public static double RATE_XP;
	public static double RATE_CASTLE_XP;
	public static double RATE_PET_XP;
	public static double RATE_LAWFUL;
	public static double RATE_KARMA;
	public static double RATE_DROP_ADENA;
	public static double RATE_DROP_ITEMS;
	public static int RATE_ROBOT_TIME; // ����PC(���)
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_ACCESSORY;
	public static int MAX_WEAPON;
	public static int MAX_ARMOR;
	public static int MAX_ACCESSORY; 
	public static double RATE_WEIGHT_LIMIT;
	public static double RATE_WEIGHT_LIMIT_PET;
	public static double RATE_SHOP_SELLING_PRICE;
	public static double RATE_SHOP_PURCHASING_PRICE;
	public static int FEATHER_TIME;
	public static int FEATHER_NUMBER;
	public static int CLAN_NUMBER;
	public static int CASTLE_NUMBER;
	public static int MAX_LEVEL;  //�űԺ�ȣ���� 
	public static int NEWUSERSAFETY_LEVEL;  // A105

	public static int CREATE_CHANCE_DIARY;
	public static int CREATE_CHANCE_RECOLLECTION;
	public static int CREATE_CHANCE_MYSTERIOUS;
	public static int CREATE_CHANCE_PROCESSING;
	public static int CREATE_CHANCE_PROCESSING_DIAMOND;
	public static int CREATE_CHANCE_DANTES;
	public static int CREATE_CHANCE_ANCIENT_AMULET;
	public static int CREATE_CHANCE_HISTORY_BOOK;

	/** AltSettings control */
	public static short GLOBAL_CHAT_LEVEL;
	public static short WHISPER_CHAT_LEVEL;
	public static byte AUTO_LOOT;
	public static int LOOTING_RANGE;
	public static boolean ALT_NONPVP;
	public static boolean ALT_ATKMSG;
	public static boolean CHANGE_TITLE_BY_ONESELF;
	public static int MAX_CLAN_MEMBER;
	public static boolean CLAN_ALLIANCE;
	public static int MAX_PT;
	public static int MAX_CHAT_PT;
	public static boolean SIM_WAR_PENALTY;
	public static boolean GET_BACK;
	public static String ALT_ITEM_DELETION_TYPE;
	public static int ALT_ITEM_DELETION_TIME;
	public static int ALT_ITEM_DELETION_RANGE;	

	/****** �̺�Ʈ ******/
	public static boolean ALT_HALLOWEENEVENT;	// �ҷ���
	public static boolean ALT_HALLOWEENEVENT2009;	// �ҷ���(2009��)
	public static boolean ALT_FANTASYEVENT;		// ȯ��
	public static boolean ALT_CHUSEOKEVENT;		// �߼�(09.09.24)
	public static boolean ALT_FEATURE;	

	public static boolean ALT_WHO_COMMAND;
	public static boolean ALT_REVIVAL_POTION;
	public static int ALT_WAR_TIME;
	public static int ALT_WAR_TIME_UNIT;
	public static int ALT_WAR_INTERVAL;
	public static int ALT_WAR_INTERVAL_UNIT;
	public static int ALT_RATE_OF_DUTY;
	public static boolean SPAWN_HOME_POINT;
	public static int SPAWN_HOME_POINT_RANGE;
	public static int SPAWN_HOME_POINT_COUNT;
	public static int SPAWN_HOME_POINT_DELAY;
	public static boolean INIT_BOSS_SPAWN;
	public static int ELEMENTAL_STONE_AMOUNT;
	public static int HOUSE_TAX_INTERVAL;
	public static int MAX_DOLL_COUNT;
	public static boolean RETURN_TO_NATURE;
	public static int MAX_NPC_ITEM;
	public static int MAX_PERSONAL_WAREHOUSE_ITEM;
	public static int MAX_CLAN_WAREHOUSE_ITEM;
	public static boolean DELETE_CHARACTER_AFTER_7DAYS;	
	public static int GMCODE;
	public static int DELETE_DB_DAYS;

	/** CharSettings control */
	public static int PRINCE_MAX_HP;
	public static int PRINCE_MAX_MP;
	public static int KNIGHT_MAX_HP;
	public static int KNIGHT_MAX_MP;
	public static int ELF_MAX_HP;
	public static int ELF_MAX_MP;
	public static int WIZARD_MAX_HP;
	public static int WIZARD_MAX_MP;
	public static int DARKELF_MAX_HP;
	public static int DARKELF_MAX_MP;
	public static int DRAGONKNIGHT_MAX_HP;
	public static int DRAGONKNIGHT_MAX_MP;
	public static int BLACKWIZARD_MAX_HP;
	public static int BLACKWIZARD_MAX_MP;

	public static int LIMITLEVEL;

	public static int LV50_EXP;
	public static int LV51_EXP;
	public static int LV52_EXP;
	public static int LV53_EXP;
	public static int LV54_EXP;
	public static int LV55_EXP;
	public static int LV56_EXP;
	public static int LV57_EXP;
	public static int LV58_EXP;
	public static int LV59_EXP;
	public static int LV60_EXP;
	public static int LV61_EXP;
	public static int LV62_EXP;
	public static int LV63_EXP;
	public static int LV64_EXP;
	public static int LV65_EXP;
	public static int LV66_EXP;
	public static int LV67_EXP;
	public static int LV68_EXP;
	public static int LV69_EXP;
	public static int LV70_EXP;
	public static int LV71_EXP;
	public static int LV72_EXP;
	public static int LV73_EXP;
	public static int LV74_EXP;
	public static int LV75_EXP;
	public static int LV76_EXP;
	public static int LV77_EXP;
	public static int LV78_EXP;
	public static int LV79_EXP;
	public static int LV80_EXP;
	public static int LV81_EXP;
	public static int LV82_EXP;
	public static int LV83_EXP;
	public static int LV84_EXP;
	public static int LV85_EXP;
	public static int LV86_EXP;
	public static int LV87_EXP;
	public static int LV88_EXP;
	public static int LV89_EXP;
	public static int LV90_EXP;
	public static int LV91_EXP;
	public static int LV92_EXP;
	public static int LV93_EXP;
	public static int LV94_EXP;
	public static int LV95_EXP;
	public static int LV96_EXP;
	public static int LV97_EXP;
	public static int LV98_EXP;
	public static int LV99_EXP;

	/** �����ͺ��̽� Ǯ ���� */
	public static int min;
	public static int max;
	public static boolean run;

	/** Configuration files */
	public static final String SERVER_CONFIG_FILE = "./config/server.properties";
	public static final String RATES_CONFIG_FILE = "./config/rates.properties";
	public static final String ALT_SETTINGS_FILE = "./config/altsettings.properties";
	public static final String CHAR_SETTINGS_CONFIG_FILE = "./config/charsettings.properties";
	//# �ڵ� ��������..
	public static final String OTHER_SETTINGS_CONFIG_FILE = "./config/othersettings.properties";

	public static boolean shutdown = false;
	// �α� ǥ���Ұ�����
	public static boolean LOGGER	= true;
	// ��Ŷ ǥ�� �Ұ�����
	public static boolean PACKET	= false;
	/** �� ���� ���� */

	// NPC�κ��� ���̸��� �� �ִ� MP�Ѱ�
	public static final int MANA_DRAIN_LIMIT_PER_NPC = 40;

	// 1ȸ�� �������� ���̸��� �� �ִ� MP�Ѱ�(SOM, ��ö SOM)
	public static final int MANA_DRAIN_LIMIT_PER_SOM_ATTACK = 9;

	public static void load() {
		_log.info("loading gameserver config");
		//# �ڵ� ��������..
		//othersettings.properties
		try 
		{
			Properties otherSettings = new Properties();
			InputStream is = new FileInputStream(new File(OTHER_SETTINGS_CONFIG_FILE));
			otherSettings.load(is);
			is.close();
			Use_Show_Announcecycle = Boolean.parseBoolean(otherSettings.getProperty("UseShowAnnouncecycle", "false")); 
			Show_Announcecycle_Time = Integer.parseInt(otherSettings.getProperty("ShowAnnouncecycleTime", "30"));
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new Error("Failed to Load " + SERVER_CONFIG_FILE + " File.");
		}

		// server.properties
		try {
			Properties serverSettings = new Properties();
			InputStream is = new FileInputStream(new File(SERVER_CONFIG_FILE));
			serverSettings.load(is);
			is.close();

			/** ������ ���̽� Ǯ */
			min = Integer.parseInt(serverSettings.getProperty("min"));
			max = Integer.parseInt(serverSettings.getProperty("max"));
			run = Boolean.parseBoolean(serverSettings.getProperty("run"));

			GAME_SERVER_TYPE = Integer.parseInt(serverSettings.getProperty("ServerType", "0"));

			GAME_SERVER_HOST_NAME = serverSettings.getProperty("GameserverHostname", "*");

			GAME_SERVER_PORT = Integer.parseInt(serverSettings.getProperty("GameserverPort", "2000"));

			DB_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");

			DB_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l1jdb?useUnicode=true&characterEncoding=euckr");

			DB_LOGIN = serverSettings.getProperty("Login", "root");

			DB_PASSWORD = serverSettings.getProperty("Password", "");

			THREAD_P_TYPE_GENERAL = Integer.parseInt(serverSettings.getProperty("GeneralThreadPoolType", "0"), 10);

			THREAD_P_SIZE_GENERAL = Integer.parseInt(serverSettings.getProperty("GeneralThreadPoolSize", "0"), 10);

			CLIENT_LANGUAGE = Integer.parseInt(serverSettings.getProperty("ClientLanguage", "4"));

			TIME_ZONE = serverSettings.getProperty("TimeZone", "KST");

			HOSTNAME_LOOKUPS = Boolean.parseBoolean(serverSettings.getProperty("HostnameLookups", "false"));

			AUTOMATIC_KICK = Integer.parseInt(serverSettings.getProperty("AutomaticKick", "10"));

			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "true"));

			MAX_ONLINE_USERS = Short.parseShort(serverSettings.getProperty("MaximumOnlineUsers", "30"));

			CACHE_MAP_FILES = Boolean.parseBoolean(serverSettings.getProperty("CacheMapFiles", "false"));

			LOAD_V2_MAP_FILES = Boolean.parseBoolean(serverSettings.getProperty("LoadV2MapFiles", "false"));

			CHECK_MOVE_INTERVAL = Boolean.parseBoolean(serverSettings.getProperty("CheckMoveInterval", "false"));

			CHECK_ATTACK_INTERVAL = Boolean.parseBoolean(serverSettings.getProperty("CheckAttackInterval", "false"));

			CHECK_SPELL_INTERVAL = Boolean.parseBoolean(serverSettings.getProperty("CheckSpellInterval", "false"));

			INJUSTICE_COUNT = Short.parseShort(serverSettings.getProperty("InjusticeCount", "10"));

			JUSTICE_COUNT = Integer.parseInt(serverSettings.getProperty("JusticeCount", "4"));

			CHECK_STRICTNESS = Integer.parseInt(serverSettings.getProperty("CheckStrictness", "102"));

			LOGGING_WEAPON_ENCHANT = Byte.parseByte(serverSettings.getProperty("LoggingWeaponEnchant", "0"));

			LOGGING_ARMOR_ENCHANT = Byte.parseByte(serverSettings.getProperty("LoggingArmorEnchant", "0"));

			LOGGING_CHAT_NORMAL = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatNormal", "false"));

			LOGGING_CHAT_WHISPER = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatWhisper", "false"));

			LOGGING_CHAT_SHOUT = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatShout", "false"));

			LOGGING_CHAT_WORLD = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatWorld", "false"));

			LOGGING_CHAT_CLAN = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatClan", "false"));

			LOGGING_CHAT_PARTY = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatParty", "false"));

			LOGGING_CHAT_COMBINED = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatCombined", "false"));

			LOGGING_CHAT_CHAT_PARTY = Boolean.parseBoolean(serverSettings.getProperty("LoggingChatChatParty", "false"));

			SPEEDUP_SPEEDMENT = Integer.parseInt(serverSettings.getProperty("Speedment", "1"));

			AUTOSAVE_INTERVAL = Integer.parseInt(serverSettings.getProperty("AutosaveInterval", "1200"), 10);

			AUTOSAVE_INTERVAL_INVENTORY = Integer.parseInt(serverSettings.getProperty("AutosaveIntervalOfInventory", "300"), 10);

			SKILLTIMER_IMPLTYPE = Integer.parseInt(serverSettings.getProperty("SkillTimerImplType", "1"));

			NPCAI_IMPLTYPE = Integer.parseInt(serverSettings.getProperty("NpcAIImplType", "1"));

			TELNET_SERVER = Boolean.parseBoolean(serverSettings.getProperty("TelnetServer", "false"));

			TELNET_SERVER_PORT = Integer.parseInt(serverSettings.getProperty("TelnetServerPort", "23"));

			PC_RECOGNIZE_RANGE = Integer.parseInt(serverSettings.getProperty("PcRecognizeRange", "20"));

			CHARACTER_CONFIG_IN_SERVER_SIDE = Boolean.parseBoolean(serverSettings.getProperty("CharacterConfigInServerSide", "true"));

			ALLOW_2PC = Boolean.parseBoolean(serverSettings.getProperty("Allow2PC", "true"));

			LEVEL_DOWN_RANGE = Integer.parseInt(serverSettings.getProperty("LevelDownRange", "0"));

			SEND_PACKET_BEFORE_TELEPORT = Boolean.parseBoolean(serverSettings.getProperty("SendPacketBeforeTeleport", "false"));

			DETECT_DB_RESOURCE_LEAKS = Boolean.parseBoolean(serverSettings.getProperty("EnableDatabaseResourceLeaksDetection", "false"));

			AUTH_CONNECT = Boolean.parseBoolean(serverSettings.getProperty("AuthConnect", "true")); 

		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new Error("Failed to Load " + SERVER_CONFIG_FILE + " File.");
		}

		// rates.properties
		try {
			Properties rateSettings = new Properties();
			//InputStream is = new FileInputStream(new File(RATES_CONFIG_FILE));
			FileReader is = new FileReader(new File(RATES_CONFIG_FILE));
			rateSettings.load(is);
			is.close();

			systime = Integer.parseInt(rateSettings.getProperty("systime", "30"));          
			sys1 =rateSettings.getProperty("sys1",";;");
			sys2 =rateSettings.getProperty("sys2",";;");
			sys3 =rateSettings.getProperty("sys3",";;");
			sys4 =rateSettings.getProperty("sys4",";;");
			sys5 =rateSettings.getProperty("sys5",";;");
			sys6 =rateSettings.getProperty("sys6",";;");
			sys7 =rateSettings.getProperty("sys7",";;");  	

			RATE_XP = Double.parseDouble(rateSettings.getProperty("RateXp", "1.0"));

			RATE_PET_XP = Double.parseDouble(rateSettings.getProperty("RatePetXp", "1.0"));

			RATE_LAWFUL = Double.parseDouble(rateSettings.getProperty("RateLawful", "1.0"));

			RATE_KARMA = Double.parseDouble(rateSettings.getProperty("RateKarma", "1.0"));

			RATE_DROP_ADENA = Double.parseDouble(rateSettings.getProperty("RateDropAdena", "1.0"));

			RATE_DROP_ITEMS = Double.parseDouble(rateSettings.getProperty("RateDropItems", "1.0"));

			RATE_ROBOT_TIME = Integer.parseInt(rateSettings.getProperty("RateRobotTime", "20")); // ����PC(���)

			ENCHANT_CHANCE_WEAPON = Integer.parseInt(rateSettings.getProperty("EnchantChanceWeapon", "68"));

			ENCHANT_CHANCE_ARMOR = Integer.parseInt(rateSettings.getProperty("EnchantChanceArmor", "52"));

			MAX_WEAPON = Integer.parseInt(rateSettings.getProperty("MaxWeapon", "13"));

			MAX_ARMOR = Integer.parseInt(rateSettings.getProperty("MaxArmor", "10"));

			MAX_ACCESSORY = Integer.parseInt(rateSettings.getProperty("MaxAccessory", "9")); 

			ENCHANT_CHANCE_ACCESSORY = Integer.parseInt(rateSettings.getProperty("EnchantChanceAccessory", "50")); 

			RATE_WEIGHT_LIMIT = Double.parseDouble(rateSettings.getProperty("RateWeightLimit", "1"));

			RATE_WEIGHT_LIMIT_PET = Double.parseDouble(rateSettings.getProperty("RateWeightLimitforPet", "1"));

			RATE_SHOP_SELLING_PRICE = Double.parseDouble(rateSettings.getProperty("RateShopSellingPrice", "1.0"));

			RATE_SHOP_PURCHASING_PRICE = Double.parseDouble(rateSettings.getProperty("RateShopPurchasingPrice", "1.0"));

			CREATE_CHANCE_DIARY = Integer.parseInt(rateSettings.getProperty("CreateChanceDiary", "33"));

			CREATE_CHANCE_RECOLLECTION = Integer.parseInt(rateSettings.getProperty("CreateChanceRecollection", "90"));

			CREATE_CHANCE_MYSTERIOUS = Integer.parseInt(rateSettings.getProperty("CreateChanceMysterious", "90"));

			CREATE_CHANCE_PROCESSING = Integer.parseInt(rateSettings.getProperty("CreateChanceProcessing", "90"));

			CREATE_CHANCE_PROCESSING_DIAMOND = Integer.parseInt(rateSettings.getProperty("CreateChanceProcessingDiamond", "90"));		

			CREATE_CHANCE_DANTES= Integer.parseInt(rateSettings.getProperty("CreateChanceDantes", "90"));

			CREATE_CHANCE_ANCIENT_AMULET= Integer.parseInt(rateSettings.getProperty("CreateChanceAncientAmulet", "90"));

			CREATE_CHANCE_HISTORY_BOOK = Integer.parseInt(rateSettings.getProperty("CreateChanceHistoryBook", "50"));

			FEATHER_TIME = Integer.parseInt(rateSettings.getProperty("FeatherTime", "10"));

			FEATHER_NUMBER = Integer.parseInt(rateSettings.getProperty("FeatherNumber", "10"));

			CLAN_NUMBER = Integer.parseInt(rateSettings.getProperty("ClanNumber", "10"));

			CASTLE_NUMBER = Integer.parseInt(rateSettings.getProperty("CastleNumber", "50"));

			MAX_LEVEL = Integer.parseInt(rateSettings.getProperty("Maxlevel", "12"));//�űԺ�ȣ����

			NEWUSERSAFETY_LEVEL = Integer.parseInt(rateSettings.getProperty("NewUserSafetyLevel", "12"));// A105 �ű� ���� ��ȣ ���� ����

			RATE_CASTLE_XP = Double.parseDouble(rateSettings.getProperty("RateCastleXp", "1.02"));

		} catch (Exception e) {		
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new Error("Failed to Load " + RATES_CONFIG_FILE + " File.");
		}

		// altsettings.properties
		try {
			Properties altSettings = new Properties();
			InputStream is = new FileInputStream(new File(ALT_SETTINGS_FILE));
			altSettings.load(is);
			is.close();

			GLOBAL_CHAT_LEVEL = Short.parseShort(altSettings.getProperty("GlobalChatLevel", "30"));

			WHISPER_CHAT_LEVEL = Short.parseShort(altSettings.getProperty("WhisperChatLevel", "7"));

			AUTO_LOOT = Byte.parseByte(altSettings.getProperty("AutoLoot", "2"));

			LOOTING_RANGE = Integer.parseInt(altSettings.getProperty("LootingRange", "3"));

			ALT_NONPVP = Boolean.parseBoolean(altSettings.getProperty("NonPvP", "true"));

			ALT_ATKMSG = Boolean.parseBoolean(altSettings.getProperty("AttackMessageOn", "true"));

			CHANGE_TITLE_BY_ONESELF = Boolean.parseBoolean(altSettings.getProperty("ChangeTitleByOneself", "false"));

			MAX_CLAN_MEMBER = Integer.parseInt(altSettings.getProperty("MaxClanMember", "0"));

			CLAN_ALLIANCE = Boolean.parseBoolean(altSettings.getProperty("ClanAlliance", "true"));

			MAX_PT = Integer.parseInt(altSettings.getProperty("MaxPT", "8"));

			MAX_CHAT_PT = Integer.parseInt(altSettings.getProperty("MaxChatPT", "8"));

			SIM_WAR_PENALTY = Boolean.parseBoolean(altSettings.getProperty("SimWarPenalty", "true"));

			GET_BACK = Boolean.parseBoolean(altSettings.getProperty("GetBack", "false"));

			ALT_ITEM_DELETION_TYPE = altSettings.getProperty("ItemDeletionType", "auto");

			ALT_ITEM_DELETION_TIME = Integer.parseInt(altSettings.getProperty("ItemDeletionTime", "10"));

			ALT_ITEM_DELETION_RANGE = Integer.parseInt(altSettings.getProperty("ItemDeletionRange", "5"));

			ALT_HALLOWEENEVENT = Boolean.parseBoolean(altSettings.getProperty("HalloweenEvent", "true"));

			ALT_HALLOWEENEVENT2009 = Boolean.parseBoolean(altSettings.getProperty("HalloweenEvent2009", "true"));

			ALT_FANTASYEVENT = Boolean.parseBoolean(altSettings.getProperty("FantasyEvent", "true"));

			ALT_CHUSEOKEVENT = Boolean.parseBoolean(altSettings.getProperty("ChuSeokEvent", "true"));

			ALT_FEATURE = Boolean.parseBoolean(altSettings.getProperty("FeatureEvent", "true"));

			ALT_WHO_COMMAND = Boolean.parseBoolean(altSettings.getProperty("WhoCommand", "false"));

			ALT_REVIVAL_POTION = Boolean.parseBoolean(altSettings.getProperty("RevivalPotion", "false"));
			String strWar;
			strWar = altSettings.getProperty("WarTime", "1h");
			if (strWar.indexOf("d") >= 0) {
				ALT_WAR_TIME_UNIT = Calendar.DATE;
				strWar = strWar.replace("d", "");
			} else if (strWar.indexOf("h") >= 0) {
				ALT_WAR_TIME_UNIT = Calendar.HOUR_OF_DAY;
				strWar = strWar.replace("h", "");
			} else if (strWar.indexOf("m") >= 0) {
				ALT_WAR_TIME_UNIT = Calendar.MINUTE;
				strWar = strWar.replace("m", "");
			}
			ALT_WAR_TIME = Integer.parseInt(strWar);
			strWar = altSettings.getProperty("WarInterval", "2d");
			if (strWar.indexOf("d") >= 0) {
				ALT_WAR_INTERVAL_UNIT = Calendar.DATE;
				strWar = strWar.replace("d", "");
			} else if (strWar.indexOf("h") >= 0) {
				ALT_WAR_INTERVAL_UNIT = Calendar.HOUR_OF_DAY;
				strWar = strWar.replace("h", "");
			} else if (strWar.indexOf("m") >= 0) {
				ALT_WAR_INTERVAL_UNIT = Calendar.MINUTE;
				strWar = strWar.replace("m", "");
			}
			ALT_WAR_INTERVAL = Integer.parseInt(strWar);

			SPAWN_HOME_POINT = Boolean.parseBoolean(altSettings.getProperty("SpawnHomePoint", "true"));

			SPAWN_HOME_POINT_COUNT = Integer.parseInt(altSettings.getProperty("SpawnHomePointCount", "2"));

			SPAWN_HOME_POINT_DELAY = Integer.parseInt(altSettings.getProperty("SpawnHomePointDelay", "100"));

			SPAWN_HOME_POINT_RANGE = Integer.parseInt(altSettings.getProperty("SpawnHomePointRange", "8"));

			INIT_BOSS_SPAWN = Boolean.parseBoolean(altSettings.getProperty("InitBossSpawn", "true"));

			ELEMENTAL_STONE_AMOUNT = Integer.parseInt(altSettings.getProperty("ElementalStoneAmount", "300"));

			HOUSE_TAX_INTERVAL = Integer.parseInt(altSettings.getProperty("HouseTaxInterval", "10"));

			MAX_DOLL_COUNT = Integer.parseInt(altSettings.getProperty("MaxDollCount", "1"));

			RETURN_TO_NATURE = Boolean.parseBoolean(altSettings.getProperty("ReturnToNature", "false"));

			MAX_NPC_ITEM = Integer.parseInt(altSettings.getProperty("MaxNpcItem", "8"));

			MAX_PERSONAL_WAREHOUSE_ITEM = Integer.parseInt(altSettings.getProperty("MaxPersonalWarehouseItem", "100"));

			MAX_CLAN_WAREHOUSE_ITEM = Integer.parseInt(altSettings.getProperty("MaxClanWarehouseItem", "200"));

			DELETE_CHARACTER_AFTER_7DAYS = Boolean.parseBoolean(altSettings.getProperty("DeleteCharacterAfter7Days", "True"));

			GMCODE = Integer.parseInt(altSettings.getProperty("GMCODE", "9999"));

			DELETE_DB_DAYS = Integer.parseInt(altSettings.getProperty("DeleteDBDAY", "14"));
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new Error("Failed to Load " + ALT_SETTINGS_FILE + " File.");
		}

		// charsettings.properties
		try {
			Properties charSettings = new Properties();
			InputStream is = new FileInputStream(new File(
					CHAR_SETTINGS_CONFIG_FILE));
			charSettings.load(is);
			is.close();

			PRINCE_MAX_HP = Integer.parseInt(charSettings.getProperty("PrinceMaxHP", "1000"));

			PRINCE_MAX_MP = Integer.parseInt(charSettings.getProperty("PrinceMaxMP", "800"));

			KNIGHT_MAX_HP = Integer.parseInt(charSettings.getProperty("KnightMaxHP", "1400"));

			KNIGHT_MAX_MP = Integer.parseInt(charSettings.getProperty("KnightMaxMP", "600"));

			ELF_MAX_HP = Integer.parseInt(charSettings.getProperty("ElfMaxHP", "1000"));

			ELF_MAX_MP = Integer.parseInt(charSettings.getProperty("ElfMaxMP", "900"));

			WIZARD_MAX_HP = Integer.parseInt(charSettings.getProperty("WizardMaxHP", "800"));

			WIZARD_MAX_MP = Integer.parseInt(charSettings.getProperty("WizardMaxMP", "1200"));

			DARKELF_MAX_HP = Integer.parseInt(charSettings.getProperty("DarkelfMaxHP", "1000"));

			DARKELF_MAX_MP = Integer.parseInt(charSettings.getProperty("DarkelfMaxMP", "900"));

			DRAGONKNIGHT_MAX_HP = Integer.parseInt(charSettings.getProperty("DragonknightMaxHP", "1000"));

			DRAGONKNIGHT_MAX_MP = Integer.parseInt(charSettings.getProperty("DragonknightMaxMP", "900"));

			BLACKWIZARD_MAX_HP = Integer.parseInt(charSettings.getProperty("BlackwizardMaxHP", "900"));

			BLACKWIZARD_MAX_MP = Integer.parseInt(charSettings.getProperty("BlackwizardMaxMP", "1100"));

			LIMITLEVEL = Integer.parseInt(charSettings.getProperty("LimitLevel", "99"));			

			LV50_EXP = Integer.parseInt(charSettings.getProperty("Lv50Exp", "1"));
			LV51_EXP = Integer.parseInt(charSettings.getProperty("Lv51Exp", "1"));
			LV52_EXP = Integer.parseInt(charSettings.getProperty("Lv52Exp", "1"));
			LV53_EXP = Integer.parseInt(charSettings.getProperty("Lv53Exp", "1"));
			LV54_EXP = Integer.parseInt(charSettings.getProperty("Lv54Exp", "1"));
			LV55_EXP = Integer.parseInt(charSettings.getProperty("Lv55Exp", "1"));
			LV56_EXP = Integer.parseInt(charSettings.getProperty("Lv56Exp", "1"));
			LV57_EXP = Integer.parseInt(charSettings.getProperty("Lv57Exp", "1"));
			LV58_EXP = Integer.parseInt(charSettings.getProperty("Lv58Exp", "1"));
			LV59_EXP = Integer.parseInt(charSettings.getProperty("Lv59Exp", "1"));
			LV60_EXP = Integer.parseInt(charSettings.getProperty("Lv60Exp", "1"));
			LV61_EXP = Integer.parseInt(charSettings.getProperty("Lv61Exp", "1"));
			LV62_EXP = Integer.parseInt(charSettings.getProperty("Lv62Exp", "1"));
			LV63_EXP = Integer.parseInt(charSettings.getProperty("Lv63Exp", "1"));
			LV64_EXP = Integer.parseInt(charSettings.getProperty("Lv64Exp", "1"));
			LV65_EXP = Integer.parseInt(charSettings.getProperty("Lv65Exp", "2"));
			LV66_EXP = Integer.parseInt(charSettings.getProperty("Lv66Exp", "2"));
			LV67_EXP = Integer.parseInt(charSettings.getProperty("Lv67Exp", "2"));
			LV68_EXP = Integer.parseInt(charSettings.getProperty("Lv68Exp", "2"));
			LV69_EXP = Integer.parseInt(charSettings.getProperty("Lv69Exp", "2"));
			LV70_EXP = Integer.parseInt(charSettings.getProperty("Lv70Exp", "4"));
			LV71_EXP = Integer.parseInt(charSettings.getProperty("Lv71Exp", "4"));
			LV72_EXP = Integer.parseInt(charSettings.getProperty("Lv72Exp", "4"));
			LV73_EXP = Integer.parseInt(charSettings.getProperty("Lv73Exp", "4"));
			LV74_EXP = Integer.parseInt(charSettings.getProperty("Lv74Exp", "4"));
			LV75_EXP = Integer.parseInt(charSettings.getProperty("Lv75Exp", "8"));
			LV76_EXP = Integer.parseInt(charSettings.getProperty("Lv76Exp", "8"));
			LV77_EXP = Integer.parseInt(charSettings.getProperty("Lv77Exp", "8"));
			LV78_EXP = Integer.parseInt(charSettings.getProperty("Lv78Exp", "8"));
			LV79_EXP = Integer.parseInt(charSettings.getProperty("Lv79Exp", "16"));
			LV80_EXP = Integer.parseInt(charSettings.getProperty("Lv80Exp", "32"));
			LV81_EXP = Integer.parseInt(charSettings.getProperty("Lv81Exp", "64"));
			LV82_EXP = Integer.parseInt(charSettings.getProperty("Lv82Exp", "128"));
			LV83_EXP = Integer.parseInt(charSettings.getProperty("Lv83Exp", "256"));
			LV84_EXP = Integer.parseInt(charSettings.getProperty("Lv84Exp", "512"));
			LV85_EXP = Integer.parseInt(charSettings.getProperty("Lv85Exp", "1024"));
			LV86_EXP = Integer.parseInt(charSettings.getProperty("Lv86Exp", "2048"));
			LV87_EXP = Integer.parseInt(charSettings.getProperty("Lv87Exp", "4096"));
			LV88_EXP = Integer.parseInt(charSettings.getProperty("Lv88Exp", "8192"));
			LV89_EXP = Integer.parseInt(charSettings.getProperty("Lv89Exp", "16384"));
			LV90_EXP = Integer.parseInt(charSettings.getProperty("Lv90Exp", "32768"));
			LV91_EXP = Integer.parseInt(charSettings.getProperty("Lv91Exp", "65536"));
			LV92_EXP = Integer.parseInt(charSettings.getProperty("Lv92Exp", "131072"));
			LV93_EXP = Integer.parseInt(charSettings.getProperty("Lv93Exp", "262144"));
			LV94_EXP = Integer.parseInt(charSettings.getProperty("Lv94Exp", "524288"));
			LV95_EXP = Integer.parseInt(charSettings.getProperty("Lv95Exp", "1048576"));
			LV96_EXP = Integer.parseInt(charSettings.getProperty("Lv96Exp", "2097152"));
			LV97_EXP = Integer.parseInt(charSettings.getProperty("Lv97Exp", "4194304"));
			LV98_EXP = Integer.parseInt(charSettings.getProperty("Lv98Exp", "8388608"));
			LV99_EXP = Integer.parseInt(charSettings.getProperty("Lv99Exp", "16777216"));
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new Error("Failed to Load " + CHAR_SETTINGS_CONFIG_FILE + " File.");
		}
		validate();
	}

	private static void validate() {
		if (! IntRange.includes(Config.ALT_ITEM_DELETION_RANGE, 0, 5)) {
			throw new IllegalStateException("ItemDeletionRange�� ���� ���� ���� �������Դϴ�. ");
		}

		if (! IntRange.includes(Config.ALT_ITEM_DELETION_TIME, 1, 35791)) {
			throw new IllegalStateException("ItemDeletionTime�� ���� ���� ���� �������Դϴ�. ");
		}
	}

	public static boolean setParameterValue(String pName, String pValue) {
		// server.properties
		if (pName.equalsIgnoreCase("ServerType")) {
			GAME_SERVER_TYPE = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("GameserverHostname")) {
			GAME_SERVER_HOST_NAME = pValue;
		} else if (pName.equalsIgnoreCase("GameserverPort")) {
			GAME_SERVER_PORT = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Driver")) {
			DB_DRIVER = pValue;
		} else if (pName.equalsIgnoreCase("URL")) {
			DB_URL = pValue;
		} else if (pName.equalsIgnoreCase("Login")) {
			DB_LOGIN = pValue;
		} else if (pName.equalsIgnoreCase("Password")) {
			DB_PASSWORD = pValue;
		} else if (pName.equalsIgnoreCase("ClientLanguage")) {
			CLIENT_LANGUAGE = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("TimeZone")) {
			TIME_ZONE = pValue;
		} else if (pName.equalsIgnoreCase("AutomaticKick")) {
			AUTOMATIC_KICK = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("AutoCreateAccounts")) {
			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("MaximumOnlineUsers")) {
			MAX_ONLINE_USERS = Short.parseShort(pValue);
		} else if (pName.equalsIgnoreCase("LoggingWeaponEnchant")) {
			LOGGING_WEAPON_ENCHANT = Byte.parseByte(pValue);
		} else if (pName.equalsIgnoreCase("LoggingArmorEnchant")) {
			LOGGING_ARMOR_ENCHANT = Byte.parseByte(pValue);
		} else if (pName.equalsIgnoreCase("CharacterConfigInServerSide")) {
			CHARACTER_CONFIG_IN_SERVER_SIDE = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("Allow2PC")) {
			ALLOW_2PC = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("LevelDownRange")) {
			LEVEL_DOWN_RANGE = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("SendPacketBeforeTeleport")) {
			SEND_PACKET_BEFORE_TELEPORT = Boolean.parseBoolean(pValue);
		}else if (pName.equalsIgnoreCase("Speedment")) {
			SPEEDUP_SPEEDMENT = Integer.parseInt(pValue);
			// rates.properties
		} else if (pName.equalsIgnoreCase("RateXp")) {
			RATE_XP = Double.parseDouble(pValue);
		} else if (pName.equalsIgnoreCase("RatePetXp")) {
			RATE_PET_XP = Double.parseDouble(pValue);
		} else if (pName.equalsIgnoreCase("RateLawful")) {
			RATE_LAWFUL = Double.parseDouble(pValue);
		} else if (pName.equalsIgnoreCase("RateKarma")) {
			RATE_KARMA = Double.parseDouble(pValue);
		} else if (pName.equalsIgnoreCase("RateDropAdena")) {
			RATE_DROP_ADENA = Double.parseDouble(pValue);
		} else if (pName.equalsIgnoreCase("RateDropItems")) {
			RATE_DROP_ITEMS = Double.parseDouble(pValue);
		} else if (pName.equalsIgnoreCase("EnchantChanceWeapon")) {
			ENCHANT_CHANCE_WEAPON = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("EnchantChanceArmor")) {
			ENCHANT_CHANCE_ARMOR = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("EnchantChanceAccessory")) {
			ENCHANT_CHANCE_ACCESSORY = Integer.parseInt(pValue); 
		} else if (pName.equalsIgnoreCase("Weightrate")) {
			RATE_WEIGHT_LIMIT = Byte.parseByte(pValue);
		} else if (pName.equalsIgnoreCase("FeatherTime")) {
			FEATHER_TIME = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("FeatherNumber")) {
			FEATHER_NUMBER = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("ClanNumber")) {
			CLAN_NUMBER = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("CastleNumber")) {
			CASTLE_NUMBER = Integer.parseInt(pValue);

			// altsettings.properties

		} else if (pName.equalsIgnoreCase("GlobalChatLevel")) {
			GLOBAL_CHAT_LEVEL = Short.parseShort(pValue);
		} else if (pName.equalsIgnoreCase("WhisperChatLevel")) {
			WHISPER_CHAT_LEVEL = Short.parseShort(pValue);
		} else if (pName.equalsIgnoreCase("AutoLoot")) {
			AUTO_LOOT = Byte.parseByte(pValue);
		} else if (pName.equalsIgnoreCase("LOOTING_RANGE")) {
			LOOTING_RANGE = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("AltNonPvP")) {
			ALT_NONPVP = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("AttackMessageOn")) {
			ALT_ATKMSG = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("ChangeTitleByOneself")) {
			CHANGE_TITLE_BY_ONESELF = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("MaxClanMember")) {
			MAX_CLAN_MEMBER = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("ClanAlliance")) {
			CLAN_ALLIANCE = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("MaxPT")) {
			MAX_PT = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("MaxChatPT")) {
			MAX_CHAT_PT = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("SimWarPenalty")) {
			SIM_WAR_PENALTY = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("GetBack")) {
			GET_BACK = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("AutomaticItemDeletionTime")) {
			ALT_ITEM_DELETION_TIME = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("AutomaticItemDeletionRange")) {
			ALT_ITEM_DELETION_RANGE = Byte.parseByte(pValue);
		} else if (pName.equalsIgnoreCase("HalloweenEvent")) {
			ALT_HALLOWEENEVENT = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("HalloweenEvent2009")) {
			ALT_HALLOWEENEVENT2009 = Boolean.valueOf(pValue);	
		} else if (pName.equalsIgnoreCase("FantasyEvent")) {
			ALT_FANTASYEVENT = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("ChuSeokEvent")) {
			ALT_CHUSEOKEVENT = Boolean.valueOf(pValue);			
		} else if (pName.equalsIgnoreCase("HouseTaxInterval")) {
			HOUSE_TAX_INTERVAL = Integer.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("MaxDollCount")) {
			MAX_DOLL_COUNT = Integer.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("ReturnToNature")) {
			RETURN_TO_NATURE = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("MaxNpcItem")) {
			MAX_NPC_ITEM = Integer.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("MaxPersonalWarehouseItem")) {
			MAX_PERSONAL_WAREHOUSE_ITEM = Integer.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("MaxClanWarehouseItem")) {
			MAX_CLAN_WAREHOUSE_ITEM = Integer.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("DeleteCharacterAfter7Days")) {
			DELETE_CHARACTER_AFTER_7DAYS = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("GMCODE")) {
			GMCODE = Integer.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("DeleteDBDAY")) {
			DELETE_DB_DAYS = Integer.valueOf(pValue);
		}
		// charsettings.properties
		else if (pName.equalsIgnoreCase("PrinceMaxHP")) {
			PRINCE_MAX_HP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("PrinceMaxMP")) {
			PRINCE_MAX_MP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("KnightMaxHP")) {
			KNIGHT_MAX_HP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("KnightMaxMP")) {
			KNIGHT_MAX_MP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("ElfMaxHP")) {
			ELF_MAX_HP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("ElfMaxMP")) {
			ELF_MAX_MP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("WizardMaxHP")) {
			WIZARD_MAX_HP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("WizardMaxMP")) {
			WIZARD_MAX_MP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("DarkelfMaxHP")) {
			DARKELF_MAX_HP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("DarkelfMaxMP")) {
			DARKELF_MAX_MP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("DragonknightMaxHP")) {
			DRAGONKNIGHT_MAX_HP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("DragonknightMaxMP")) {
			DRAGONKNIGHT_MAX_MP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("BlackwizardMaxHP")) {
			BLACKWIZARD_MAX_HP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("BlackwizardMaxMP")) {
			BLACKWIZARD_MAX_MP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("LimitLevel")) {
			LIMITLEVEL = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv50Exp")) {
			LV50_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv51Exp")) {
			LV51_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv52Exp")) {
			LV52_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv53Exp")) {
			LV53_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv54Exp")) {
			LV54_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv55Exp")) {
			LV55_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv56Exp")) {
			LV56_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv57Exp")) {
			LV57_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv58Exp")) {
			LV58_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv59Exp")) {
			LV59_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv60Exp")) {
			LV60_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv61Exp")) {
			LV61_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv62Exp")) {
			LV62_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv63Exp")) {
			LV63_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv64Exp")) {
			LV64_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv65Exp")) {
			LV65_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv66Exp")) {
			LV66_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv67Exp")) {
			LV67_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv68Exp")) {
			LV68_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv69Exp")) {
			LV69_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv70Exp")) {
			LV70_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv71Exp")) {
			LV71_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv72Exp")) {
			LV72_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv73Exp")) {
			LV73_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv74Exp")) {
			LV74_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv75Exp")) {
			LV75_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv76Exp")) {
			LV76_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv77Exp")) {
			LV77_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv78Exp")) {
			LV78_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv79Exp")) {
			LV79_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv80Exp")) {
			LV80_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv81Exp")) {
			LV81_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv82Exp")) {
			LV82_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv83Exp")) {
			LV83_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv84Exp")) {
			LV84_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv85Exp")) {
			LV85_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv86Exp")) {
			LV86_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv87Exp")) {
			LV87_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv88Exp")) {
			LV88_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv89Exp")) {
			LV89_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv90Exp")) {
			LV90_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv91Exp")) {
			LV91_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv92Exp")) {
			LV92_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv93Exp")) {
			LV93_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv94Exp")) {
			LV94_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv95Exp")) {
			LV95_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv96Exp")) {
			LV96_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv97Exp")) {
			LV97_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv98Exp")) {
			LV98_EXP = Integer.parseInt(pValue);
		} else if (pName.equalsIgnoreCase("Lv99Exp")) {
			LV99_EXP = Integer.parseInt(pValue);
			//# �ڵ� ��������..
			//otherSettings.properties
		} else if (pName.equalsIgnoreCase("UseShowAnnouncecycle")) {
			Use_Show_Announcecycle = Boolean.valueOf(pValue);
		} else if (pName.equalsIgnoreCase("ShowAnnouncecycleTime")) {
			Show_Announcecycle_Time = Integer.parseInt(pValue);
		} else {
			return false;
		}
		return true;
	}

	private Config() {
	}


	public final static int etc_arrow = 0;
	public final static int etc_wand = 1;
	public final static int etc_light = 2;
	public final static int etc_gem = 3;
	public final static int etc_potion = 6;
	public final static int etc_firecracker = 5;
	public final static int etc_food = 7;
	public final static int etc_scroll = 8;
	public final static int etc_questitem = 9;
	public final static int etc_spellbook = 10;
	public final static int etc_other = 12;
	public final static int etc_material = 13;
	public final static int etc_sting = 15;
	public final static int etc_treasurebox = 16;

	public static enum LOG{
		chat,
		error,
		system,
		badplayer,
		enchant,
		inventory,
		time
	}
	public static synchronized String YearMonthDate2(){
		try{
			int �� = Year();
			String ��2;
			if(�� < 10){
				��2 = "0"+��;
			}else{
				��2 = Integer.toString(��);
			}
			int �� = Month();
			String ��2=null;
			if(�� < 10){
				��2 = "0"+��;
			}else{
				��2 = Integer.toString(��);
			}
			int �� = Date();
			String ��2 = null;
			if(�� < 10){
				��2 = "0"+��;
			}else{
				��2 = Integer.toString(��);
			}
			return ��2+��2+��2;
		}catch (Exception e){}

		return "000000";
	}
	public static synchronized int Year(){
		Calendar cal = Calendar.getInstance ( TimeZone.getTimeZone ("KST") );
		return cal.get ( Calendar.YEAR ) - 2000;
	}

	public static synchronized int Month(){
		Calendar cal = Calendar.getInstance ( TimeZone.getTimeZone ("KST") );
		return cal.get ( Calendar.MONTH ) + 1;
	}

	public static synchronized int Date(){
		Calendar cal = Calendar.getInstance ( TimeZone.getTimeZone ("KST") );
		return cal.get ( Calendar.DATE );
	}

	public static synchronized int Hour(){
		Calendar cal = Calendar.getInstance ( TimeZone.getTimeZone ("KST") );
		return cal.get ( Calendar.HOUR_OF_DAY );
	}

	public static synchronized int Minute(){
		Calendar cal = Calendar.getInstance ( TimeZone.getTimeZone ("KST") );
		return cal.get ( Calendar.MINUTE );
	}

	public static synchronized String Time(){
		Calendar cal = Calendar.getInstance ( TimeZone.getTimeZone ("KST") );
		int h = cal.get ( Calendar.HOUR );
		int m = cal.get ( Calendar.MINUTE );
		StringBuffer sb = new StringBuffer();
		if(h<10) {
			sb.append("0");
		}
		sb.append(h);
		sb.append(":");
		if(m<10) {
			sb.append("0");
		}
		sb.append(m);
		return sb.toString();
	}
}