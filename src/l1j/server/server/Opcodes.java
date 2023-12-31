package l1j.server.server;
public class Opcodes {
	public Opcodes() {
	}
	public static final int C_OPCODE_PETMENU = 0;
	public static final int C_OPCODE_SOLDIERBUY = 1;
	public static final int C_OPCODE_NOTICECLICK = 2;
	public static final int C_OPCODE_WAREHOUSEPASSWORD = 3;
	public static final int C_OPCODE_WAR = 5;
	public static final int C_OPCODE_BOOKMARKDELETE = 6;
	public static final int C_OPCODE_FIGHT = 7;
	public static final int C_OPCODE_BOARDDELETE = 8;
	public static final int C_OPCODE_BOOKMARK = 9;
	public static final int C_OPCODE_TRADE = 11;
	public static final int C_OPCODE_BANCLAN = 12;
	public static final int C_OPCODE_NPCTALK = 13;
	public static final int C_OPCODE_MOVECHAR = 15;
	public static final int C_OPCODE_PARTY = 16;
	public static final int C_OPCODE_CHANGEHEADING = 17;
	public static final int C_OPCODE_CLIENTVERSION = 19;
	public static final int C_OPCODE_CHATGLOBAL = 21;
	public static final int C_OPCODE_BOARDWRITE = 22;
	public static final int C_OPCODE_PLEDGE = 23;
	public static final int C_OPCODE_EXCLUDE = 24;
	public static final int C_OPCODE_CALL = 25;
	public static final int C_OPCODE_BASERESET = 26;
	public static final int C_OPCODE_SKILLBUY = 27;
	public static final int C_OPCODE_DROPITEM = 30;
	public static final int C_OPCODE_BUDDYLIST = 31;
	public static final int C_OPCODE_LEAVECLANE = 33;
	public static final int C_OPCODE_TITLE = 34;
	public static final int C_OPCODE_TRADEADDOK = 35;
	public static final int C_OPCODE_RETURNTOLOGIN = 36;
	public static final int C_OPCODE_WHO = 37;
	public static final int C_OPCODE_PROPOSE = 38;
	public static final int C_OPCODE_WARTIMELIST = 39;
	public static final int C_OPCODE_SELECTLIST = 40;
	public static final int C_OPCODE_BOARD = 41;
	public static final int C_OPCODE_USEPETITEM = 42;
	public static final int C_OPCODE_CHARACTERCONFIG = 43;
	public static final int C_OPCODE_SHOP_N_WAREHOUSE = 44;
	public static final int C_OPCODE_AMOUNT = 45;
	public static final int C_OPCODE_HORUNOK = 46;
	public static final int C_OPCODE_ENTERPORTAL = 47;
	public static final int C_OPCODE_SOLDIERGIVE = 48;
	public static final int C_OPCODE_TRADEADDCANCEL = 49;
	public static final int C_OPCODE_CLAN = 50;
	public static final int C_OPCODE_SHIP = 52;
	public static final int C_OPCODE_TAXRATE = 53;
	public static final int C_OPCODE_DRAWAL = 58;
	public static final int C_OPCODE_USESKILL = 59;
	public static final int C_OPCODE_DOOR = 61;
	public static final int C_OPCODE_JOINCLAN = 64;
	public static final int C_OPCODE_SECURITYSTATUS = 66;
	public static final int C_OPCODE_QUITGAME = 67;
	public static final int C_OPCODE_FIX_WEAPON_LIST = 68;
	public static final int C_OPCODE_RANK = 69;
	public static final int C_OPCODE_USEITEM = 70;
	public static final int C_OPCODE_TRADEADDITEM = 72;
	public static final int C_OPCODE_RESTART_AFTER_DIE = 73;
	public static final int C_OPCODE_PICKUPITEM = 74;
	public static final int C_OPCODE_ATTR = 75;
	public static final int C_OPCODE_WARTIMESET = 76;
	public static final int C_OPCODE_SELECT_CHARACTER = 77;
	public static final int C_OPCODE_ARROWATTACK = 79;
	public static final int C_OPCODE_SECURITYSTATUSSET = 80;
	public static final int C_OPCODE_EXTCOMMAND = 81;
	public static final int C_OPCODE_ADDBUDDY = 83;
	public static final int C_OPCODE_SOLDIERGIVEOK = 84;
	public static final int C_OPCODE_RESTART = 85;
	public static final int C_OPCODE_ATTACK = 86;
	public static final int C_OPCODE_SHOP = 87;
	public static final int C_OPCODE_HORUN = 89;
	public static final int C_OPCODE_CHECKPK = 90;
	public static final int C_OPCODE_BOARDBACK = 93;
	public static final int C_OPCODE_SELECTTARGET = 94;
	public static final int C_OPCODE_CREATE_CHARACTER = 95;
	public static final int C_OPCODE_EMBLEM = 97;
	public static final int C_OPCODE_EXIT_GHOST = 98;
	public static final int C_OPCODE_CHATWHISPER = 100;
	public static final int C_OPCODE_KEEPALIVE = 101;
	public static final int C_OPCODE_REPORT = 102;
	public static final int C_OPCODE_PRIVATESHOPLIST = 103;
	public static final int C_OPCODE_BANPARTY = 104;
	public static final int C_OPCODE_TELEPORT = 106;
	public static final int C_OPCODE_CREATECLAN = 107;
	public static final int C_OPCODE_CHAT = 109;
	public static final int C_OPCODE_FISHCLICK = 110;
	public final static int C_OPCODE_FISHCANCEL   = 0x1f;
	public static final int C_OPCODE_LOGINTOSERVEROK = 111;
	public static final int C_OPCODE_DELETECHAR = 114;
	public static final int C_OPCODE_LEAVEPARTY = 115;
	public static final int C_OPCODE_GIVEITEM = 116;
	public static final int C_OPCODE_DEPOSIT = 118;
	public static final int C_OPCODE_BOARDREAD = 119;
	public static final int C_OPCODE_DELBUDDY = 120;
	public static final int C_OPCODE_LOGINPACKET = 121;
	public static final int C_OPCODE_MAIL = 122;
	public static final int C_OPCODE_DELETEINVENTORYITEM = 123;
	public static final int C_OPCODE_SKILLBUYOK = 125;
	public static final int C_OPCODE_NPCACTION = 127;
	public static final int C_OPCODE_CREATEPARTY = 130;
	public static final int C_OPCODE_CHATPARTY = 131;
	public static final int C_OPCODE_HOTEL_ENTER = 4097;
	public static final int S_OPCODE_EMBLEM = 0;
	public static final int S_OPCODE_PARALYSIS = 2;
	public static final int S_OPCODE_ADDITEM = 3;
	public static final int S_OPCODE_INVLIST = 4;
	public static final int S_OPCODE_INPUTAMOUNT = 6;
	public static final int S_OPCODE_TRUETARGET = 7;
	public static final int S_OPCODE_MPUPDATE = 9;
	public static final int S_OPCODE_INVIS = 12;
	public static final int S_OPCODE_DELETEINVENTORYITEM = 13;
	public static final int S_OPCODE_OWNCHARATTRDEF = 14;
	public static final int S_OPCODE_SERVERMSG = 15;
	public static final int S_OPCODE_YES_NO = 18;
	public static final int S_OPCODE_IDENTIFYDESC = 19;
	public static final int S_OPCODE_MSG = 20;
	public static final int S_OPCODE_DOACTIONGFX = 21;
	public static final int S_OPCODE_HORUN = 23;
	public static final int S_OPCODE_SOLDIERBUYLIST = 25;
	public static final int S_OPCODE_DEPOSIT = 26;
	public static final int S_OPCODE_NOTICE = 27;
	public static final int S_OPCODE_SOLDIERGIVE = 28;
	public static final int S_OPCODE_SHOWSHOPBUYLIST = 29;
	public static final int S_OPCODE_BOARD = 30;
	public static final int S_OPCODE_TRADESTATUS = 31;
	public static final int S_OPCODE_REMOVE_OBJECT = 32;
	public static final int S_OPCODE_SHOWHTML = 33;
	public static final int S_OPCODE_CHANGEHEADING = 34;
	public static final int S_OPCODE_SKILLICONSHIELD = 35;
	public static final int S_OPCODE_WAR = 37;
	public static final int S_OPCODE_LOGINRESULT = 38;
	public static final int S_OPCODE_LETTER = 39;
	public static final int S_OPCODE_HOUSEMAP = 40;
	public static final int S_OPCODE_ITEMCOLOR = 41;
	public static final int S_OPCODE_SHORTOFMATERIAL = 42;
	public static final int S_OPCODE_WHISPERCHAT = 43;
	public static final int S_OPCODE_BLUEMESSAGE = 44;
	public static final int S_OPCODE_STRUP = 46;
	public static final int S_OPCODE_SKILLBRAVE = 47;
	public static final int S_OPCODE_WEATHER = 49;
	public static final int S_OPCODE_RESURRECTION = 50;
	public static final int S_OPCODE_DELSKILL = 51;
	public static final int S_OPCODE_SHOWOBJ = 52;
	public static final int S_OPCODE_DISCONNECT = 53;
	public static final int S_OPCODE_SKILLSOUNDGFX = 54;
	public static final int S_OPCODE_SOUND = 56;
	public static final int S_OPCODE_SKILLHASTE = 57;
	public static final int S_OPCODE_ABILITY = 58;
	public static final int S_OPCODE_SERVERVERSION = 60;
	public static final int S_OPCODE_NEWCHARPACK = 61;
	public static final int S_OPCODE_NPCSHOUT = 62;
	public static final int S_OPCODE_TRADEADDITEM = 63;
	public static final int S_OPCODE_GAMETIME = 64;
	public static final int S_OPCODE_CHANGENAME = 65;
	public static final int S_OPCODE_USEMAP = 66;
	public static final int S_OPCODE_SELECTLIST = 68;
	public static final int S_OPCODE_ADDSKILL = 69;
	public static final int S_OPCODE_CURSEBLIND = 70;
	public static final int S_OPCODE_PRIVATESHOPLIST = 71;
	public static final int S_OPCODE_SKILLBUY = 72;
	public static final int S_OPCODE_PACKETBOX = 73;
	public static final int S_OPCODE_DRAWAL = 74;
	public static final int S_OPCODE_SOLDIERGIVELIST = 75;
	public static final int S_OPCODE_EXP = 76;
	public static final int S_OPCODE_CHARAMOUNT = 77;
	public static final int S_OPCODE_LIQUOR = 78;
	public static final int S_OPCODE_DRAGONPERL = 78;
	public static final int S_OPCODE_SHOWSHOPSELLLIST = 79;
	public static final int S_OPCODE_PETGUI = 80;
	public static final int S_OPCODE_RETURNEDSTAT = 80;
	public static final int S_OPCODE_CHARLIST = 81;
	public static final int S_OPCODE_HPMETER = 82;
	public static final int S_OPCODE_TAXRATE = 83;
	public static final int S_OPCODE_SPMR = 84;
	public static final int S_OPCODE_BOARDREAD = 85;
	public static final int S_OPCODE_POISON = 86;
	public static final int S_OPCODE_LIGHT = 87;
	public static final int S_OPCODE_OWNCHARSTATUS2 = 90;
	public static final int S_OPCODE_MAPID = 91;
	public static final int S_OPCODE_MOVEOBJECT = 92;
	public static final int S_OPCODE_ATTACKPACKET = 94;
	public static final int S_OPCODE_HOUSELIST = 96;
	public static final int S_OPCODE_ITEMNAME = 97;
	public static final int S_OPCODE_TRADE = 98;
	public static final int S_OPCODE_PINKNAME = 99;
	public static final int S_OPCODE_DETELECHAROK = 100;
	public static final int S_OPCODE_NEWCHARWRONG = 101;
	public static final int S_OPCODE_DEXUP = 102;
	public static final int S_OPCODE_CASTLEMASTER = 105;
	public static final int S_OPCODE_RANGESKILLS = 106;
	public static final int S_OPCODE_BOOKMARKS = 108;
	public static final int S_OPCODE_LAWFUL = 109;
	public static final int S_OPCODE_EFFECTLOCATION = 110;
	public static final int S_OPCODE_BLESSOFEVA = 111;
	public static final int S_OPCODE_CHARTITLE = 113;
	public static final int S_OPCODE_REFRESH_CLAN = 113;
	public static final int S_OPCODE_OWNCHARSTATUS = 114;
	public static final int S_OPCODE_ITEMSTATUS = 115;
	public static final int S_OPCODE_ITEMAMOUNT = 115;
	public static final int S_OPCODE_WARTIME = 116;
	public static final int S_OPCODE_UNKNOWN1 = 117; 
	public static final int S_OPCODE_POLY = 118;
	public static final int S_OPCODE_NORMALCHAT = 120;
	public static final int S_OPCODE_CHARVISUALUPDATE = 121;
	public static final int S_OPCODE_ATTRIBUTE = 122;
	public static final int S_OPCODE_SHOWRETRIEVELIST = 123;
	public static final int S_OPCODE_HPUPDATE = 125;
	public static final int S_OPCODE_SELECTTARGET = 126;
	public static final int S_OPCODE_ALLIANCECHAT = 4096;
	public static final int S_OPCODE_TELEPORT = 4097;
	public static final int S_OPCODE_HOTELENTER = 4098;
	public static final int S_OPCODE_EMERALD = 0x49;
	public final static int S_OPCODE_UNKNOWN2 = 0x76;
}