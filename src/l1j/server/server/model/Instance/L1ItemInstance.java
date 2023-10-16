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

package l1j.server.server.model.Instance;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import l1j.server.GameSystem.CrockSystem;
import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1EquipmentTimer;
import l1j.server.server.model.L1ItemOwnerTimer;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Armor;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Pet;
import l1j.server.server.utils.BinaryOutputStream;

//Referenced classes of package l1j.server.server.model:
//L1Object, L1PcInstance

public class L1ItemInstance extends L1Object {

	private static final long serialVersionUID = 1L; 
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);

	private int _count;
	private int _itemId;
	private L1Item _item;
	private boolean _isEquipped = false;
	private int _enchantLevel;
	private int _attrenchantLevel;
	private int _protection;
	private boolean _isIdentified = false;
	private int _durability;
	private int _chargeCount;
	private int _remainingTime;
	private Timestamp _lastUsed = null;
	private int bless;
	private int _lastWeight;
	private final LastStatus _lastStatus = new LastStatus();
	private L1PcInstance _pc;
	private boolean _isRunning = false;
	private EnchantTimer _timer;
	private Timestamp _buyTime = null;
	private Timestamp _endTime = null;
	private int _registlevel;
	public int getRegistLevel() { return _registlevel; }
	public void setRegistLevel(int level) { _registlevel = level; }

	public L1ItemInstance() {
		_count = 1;
		_enchantLevel = 0;
	}
	public int getsp() {
		int sp = _item.get_addsp();   
		int itemid = getItemId();
		if (itemid == 420008){ // 테베 호루스의 반지
			switch(getEnchantLevel()){
			case 1: case 2: case 3: case 4: case 5:	break;
			default: sp += getEnchantLevel() - 5; break;
			}
		}
		if (sp < 0) sp = 0;
		return sp;
	}
	public L1ItemInstance(L1Item item, int count) {
		this();
		setItem(item);
		setCount(count);
	}

	public L1ItemInstance(L1Item item) {
		this(item, 1);
	}

	public void clickItem(L1Character cha, ClientBasePacket packet){
	}

	public boolean isIdentified() {	return _isIdentified;	}
	public void setIdentified(boolean identified) {	_isIdentified = identified;	}

	public String getName() {	return _item.getName();	}

	public int getCount() {	return _count;	}
	public void setCount(int count) {	_count = count;	}

	public boolean isEquipped() {	return _isEquipped;	}
	public void setEquipped(boolean equipped) {	_isEquipped = equipped;	}

	public L1Item getItem() {	return _item;	}
	public void setItem(L1Item item) {
		_item = item;
		_itemId = item.getItemId();
	}

	public int getItemId() {	return _itemId;	}
	public void setItemId(int itemId) {	_itemId = itemId;	}

	public boolean isStackable() {	return _item.isStackable();	}

	@Override
	public void onAction(L1PcInstance player) {	}

	public int getEnchantLevel() {	return _enchantLevel;	}
	public void setEnchantLevel(int enchantLevel) {	_enchantLevel = enchantLevel;}
	
	public int getAttrEnchantLevel() {	return _attrenchantLevel;	}
	public void setAttrEnchantLevel(int attrenchantLevel) {	_attrenchantLevel = attrenchantLevel;	}
	public int getProtection() { return _protection; }
	public void setProtection(int protection) { _protection = protection; }
	public int get_gfxid() {	return _item.getGfxId();	}

	public int get_durability() {	return _durability;	}

	public int getChargeCount() {	return _chargeCount;	}
	public void setChargeCount(int i) {	_chargeCount = i;	}

	public int getRemainingTime() {	return _remainingTime;	}
	public void setRemainingTime(int i) {	_remainingTime = i;	}

	public void setLastUsed(Timestamp t) {	_lastUsed = t;	}
	public Timestamp getLastUsed() {	return _lastUsed;	}

	public int getBless() {	return bless;	}
	public void setBless(int i) {	bless = i;	}

	public int getLastWeight() {	return _lastWeight;	}
	public void setLastWeight(int weight) {	_lastWeight = weight;	}

	public Timestamp getBuyTime() { return _buyTime; }
	public void setBuyTime(Timestamp t) { _buyTime = t; }

	public Timestamp getEndTime() { return _endTime; }
	public void setEndTime(Timestamp t) { _endTime = t; } 

	public int getMr() {
		int mr = _item.get_mdef();
		int itemid = getItemId();
		if (itemid == 20011 || itemid == 20110 || itemid == 120011
				|| itemid == 420108 || itemid == 420109 || itemid == 420110	|| itemid == 420111
				|| itemid == 425108 ||  itemid == 490008 || itemid == 490017|| itemid == 500014) {
			mr += getEnchantLevel();
		}
		if (itemid == 20056 || itemid == 120056 || itemid == 220056) {
			mr += getEnchantLevel() * 2;
		}
		if (itemid == 20078||itemid == 20079) {
			mr += getEnchantLevel() * 3;
		}
		if (mr <= 0) mr = 0; //<< -마방버그 픽스
		return mr;
	}

	public void set_durability(int i) {
		if (i < 0) {
			i = 0;
		}

		if (i > 127) {
			i = 127;
		}
		_durability = i;
	}

	public int getWeight() {
		if (getItem().getWeight() == 0) {
			return 0;
		} else {
			return Math.max(getCount() * getItem().getWeight() / 1000, 1);
		}
	}


	public class LastStatus {
		public int registLevel;
		public int count;
		public int itemId;
		public boolean isEquipped = false;
		public int enchantLevel;
		public boolean isIdentified = true;
		public int durability;
		public int chargeCount;
		public int remainingTime;
		public Timestamp lastUsed = null;
		public int bless;
		public int attrenchantLevel;
		public int protection;
		public void updateAll() {
			registLevel = getRegistLevel();
			count = getCount();
			itemId = getItemId();
			isEquipped = isEquipped();
			isIdentified = isIdentified();
			enchantLevel = getEnchantLevel();
			durability = get_durability();
			chargeCount = getChargeCount();
			remainingTime = getRemainingTime();
			lastUsed = getLastUsed();
			bless = getBless();
			attrenchantLevel = getAttrEnchantLevel();
			protection = getProtection(); 
		}

		public void updateCount() {	count = getCount();	}

		public void updateItemId() {	itemId = getItemId();	}

		public void updateEquipped() {	isEquipped = isEquipped();	}

		public void updateIdentified() {	isIdentified = isIdentified();	}

		public void updateEnchantLevel() {	enchantLevel = getEnchantLevel();	}

		public void updateDuraility() {	durability = get_durability();	}

		public void updateChargeCount() {	chargeCount = getChargeCount();	}

		public void updateRemainingTime() {	remainingTime = getRemainingTime();	}

		public void updateLastUsed() {	lastUsed = getLastUsed();	}

		public void updateBless() {	bless = getBless();	}

		public void updateAttrEnchantLevel() {	attrenchantLevel = getAttrEnchantLevel();	}
		public void updateRegistLevel() { registLevel = getRegistLevel(); }
		public void updateProtection() { protection = getProtection(); } 

	}

	public LastStatus getLastStatus() {
		return _lastStatus;
	}

	public int getRecordingColumns() {
		int column = 0;

		if (getCount() != _lastStatus.count) {
			column += L1PcInventory.COL_COUNT;
		}
		if (getItemId() != _lastStatus.itemId) {
			column += L1PcInventory.COL_ITEMID;
		}
		if (isEquipped() != _lastStatus.isEquipped) {
			column += L1PcInventory.COL_EQUIPPED;
		}
		if (getEnchantLevel() != _lastStatus.enchantLevel) {
			column += L1PcInventory.COL_ENCHANTLVL;
		}
		if (get_durability() != _lastStatus.durability) {
			column += L1PcInventory.COL_DURABILITY;
		}
		if (getChargeCount() != _lastStatus.chargeCount) {
			column += L1PcInventory.COL_CHARGE_COUNT;
		}
		if (getLastUsed() != _lastStatus.lastUsed) {
			column += L1PcInventory.COL_DELAY_EFFECT;
		}
		if (isIdentified() != _lastStatus.isIdentified) {
			column += L1PcInventory.COL_IS_ID;
		}
		if (getRemainingTime() != _lastStatus.remainingTime) {
			column += L1PcInventory.COL_REMAINING_TIME;
		}
		if (getBless() != _lastStatus.bless) {
			column += L1PcInventory.COL_BLESS;
		}
		if (getAttrEnchantLevel() != _lastStatus.attrenchantLevel) {
			column += L1PcInventory.COL_ATTRENCHANTLVL;
		}
		if (getProtection() != _lastStatus.protection) { 
			column += L1PcInventory.COL_ENCHANTLVL;
		}

		if (getRegistLevel() != _lastStatus.registLevel) {

			column += L1PcInventory.COL_ENCHANTLVL;
		}


		return column;
	}

	public String getNumberedViewName(int count) {
		StringBuilder name = new StringBuilder(getNumberedName(count));
		int itemType2 = getItem().getType2();
		int itemId = getItem().getItemId();

		if (itemId == 40314 || itemId == 40316) { 
			L1Pet pet = PetTable.getInstance().getTemplate(getId());
			if (pet != null) {
				L1Npc npc = NpcTable.getInstance().getTemplate(pet.get_npcid());
				//				name.append("[Lv." + pet.get_level() + " "
				//				+ npc.get_nameid() + "]");
				name.append("[Lv." + pet.get_level() + " " + pet.get_name() + "]HP" + pet.get_hp() + " " + npc.get_nameid());
			}
		}

		if (getItem().getType2() == 0 && getItem().getType() == 2) { // light
			if (isNowLighting()) {
				name.append(" ($10)");
			}
			if (itemId == 40001 || itemId == 40002) {
				if (getRemainingTime() <= 0) {
					name.append(" ($11)");
				}
			}
		}

		if (itemId == L1ItemId.TEBEOSIRIS_KEY || itemId == L1ItemId.TIKAL_KEY) {
			name.append(" [" + CrockSystem.getInstance().OpenTime() + "]");
		}

		if (itemId == L1ItemId.DRAGON_KEY) {// 드래곤 키 
			name.append(" [" + sdf.format(getEndTime().getTime()) + "]");
		}		
		if ((itemId >= 76767) && (itemId <= 76784)
				|| (itemId >= 426001) && (itemId <= 426012) // 시간제아이템(특 방어구)
				|| (itemId >= 301) && (itemId <= 309)){
			name.append(" [" + sdf.format(getEndTime().getTime()) + "]");
		}
		if (itemId == L1ItemId.HAPPY_BIRTHDAY_ELF) {
			name.append(" [" + sdf.format(getEndTime().getTime()) + "]");
		}
		if (itemId == 40309) {
			name.append(" " + getRoundId() + "-" + (getTicketId() + 1));
		}
		if (isEquipped()) {
			if (itemType2 == 1) {
				name.append(" ($9)"); 
			} else if (itemType2 == 2 && !getItem().isUseHighPet()) {
				name.append(" ($117)"); 
			}
		}
		return name.toString();
	}

	public String getViewName() {
		return getNumberedViewName(_count);
	}

	public String getLogName() {
		return getNumberedName(_count);
	}

	public String getNumberedName(int count) {
		StringBuilder name = new StringBuilder();

		if (isIdentified()) {
			if (getItem().getType2() == 1 || getItem().getType2() == 2) {
				switch(getAttrEnchantLevel()){
				case 1: name.append("$6115 "); break;
				case 2: name.append("$6116 "); break;
				case 3: name.append("$6117 "); break;
				case 4: name.append("$6118 "); break;
				case 5: name.append("$6119 "); break;
				case 6: name.append("$6120 "); break;
				case 7: name.append("$6121 "); break;
				case 8: name.append("$6122 "); break;
				case 9: name.append("$6123 "); break;
				case 10: name.append("$6124 "); break;
				case 11: name.append("$6125 "); break;
				case 12: name.append("$6126 "); break;				
				default: break;
				}
				if (getEnchantLevel() >= 0) {
					name.append("+" + getEnchantLevel() + " ");
				} else if (getEnchantLevel() < 0) {
					name.append(String.valueOf(getEnchantLevel()) + " ");
				}
			}
		}
		name.append(_item.getNameId());
		if (isIdentified()) {
			if (getItem().getMaxChargeCount() > 0) {
				name.append(" (" + getChargeCount() + ")");
			}
			if (getItem().getItemId() == 20383) {
				name.append(" (" + getChargeCount() + ")");
			}
			if (getItem().getMaxUseTime() > 0 && getItem().getType2() != 0) {
				name.append(" (" + getRemainingTime() + ")");
			}
			if (getProtection() == 1)
				name.append("(보호중)"); 
		}

		if (count > 1) {
			name.append(" (" + count + ")");
		}

		return name.toString();
	}

	/** 아이템 상태로부터 서버 패킷으로 이용하는 형식의 바이트열을 생성해, 돌려준다. 
	  1: 타격치 , 2: 인챈트 레벨, 3: 손상도, 4: 양손검, 5: 공격 성공, 6: 추가 타격
	  7: 왕자/공주 , 8: Str, 9: Dex, 10: Con, 11: Wiz, 12: Int, 13: Cha, 14: Hp,Mp
	  15: Mr, 16: 마나흡수, 17: 주술력, 18: 헤이스트효과, 19: Ac, 20: 행운, 21: 영양,
	  22: 밝기, 23:  재질, 24: 활 명중치, 25: 종류[writeH], 26: 레벨[writeH], 27: 불속성 29: 물속성,
	  29: 바람속성, 30: 땅속성, 31: 최대Hp, 32: 최대Mp, 33: 내성, 34: 생명흡수,
	  35: 활 타격치, 36: branch용dummy, 37: 체력회복률, 38: 마나회복률, 39: `,*/
	public byte[] getStatusBytes() {
		int itemType2 = getItem().getType2();
		int itemId = getItemId();
		BinaryOutputStream os = new BinaryOutputStream();

		if (itemType2 == 0) { // etcitem
			switch (getItem().getType()) {
			case 2: // light
				os.writeC(22); 
				os.writeH(getItem().getLightRange());
				break;
			case 7: // food
				os.writeC(21);
				os.writeH(getItem().getFoodVolume());
				break;
			case 0: // arrow
			case 15: // sting
				os.writeC(1);
				os.writeC(getItem().getDmgSmall());
				os.writeC(getItem().getDmgLarge());
				break;
			default:
				os.writeC(23);
				break;
			}
			os.writeC(getItem().getMaterial());
			os.writeD(getWeight());

		} else if (itemType2 == 1 || itemType2 == 2) { // weapon | armor
			if (itemType2 == 1) { // weapon
				os.writeC(1);
				os.writeC(getItem().getDmgSmall());
				os.writeC(getItem().getDmgLarge());
				os.writeC(getItem().getMaterial());
				os.writeD(getWeight());
			} else if (itemType2 == 2) { // armor
				// AC
				os.writeC(19);
				int ac = ((L1Armor) getItem()).get_ac();
				if (ac < 0) ac = ac - ac - ac;
				os.writeC(ac);
				os.writeC(getItem().getMaterial());
				os.writeC(getItem().getGrade());
				os.writeD(getWeight());			
			}
			if (getEnchantLevel() != 0 && !(itemType2 == 2 && getItem().getGrade() >= 0)) {
				os.writeC(2);
				os.writeC(getEnchantLevel());
			}
			if (get_durability() != 0) {
				os.writeC(3);
				os.writeC(get_durability());
			}
			if (getItem().isTwohandedWeapon()) {
				os.writeC(4);
			}
			// 공격 성공
			if (getItem().getHitModifier() != 0) {
				os.writeC(5);
				os.writeC(getItem().getHitModifier());
			}

			//추타표시
			if (getItem().getDmgModifier() != 0) {
				os.writeC(6);
				os.writeC(getItem().getDmgModifier());
			}
			if (getItem().getHitup() != 0) {
				os.writeC(5);
				os.writeC(getItem().getHitup());
			} else if (itemType2 == 2 && getItem().getGrade() == 0 && getEnchantLevel() >= 6 ){//상급악세 명중표시
				os.writeC(5);
				os.writeC(getItem().getHitup() + (getEnchantLevel()-5));
			}
			if (getItem().getDmgup() != 0) {
				os.writeC(6);
				os.writeC(getItem().getDmgup());
			} else if(itemType2 == 2 && (itemId >= 500000 && itemId <= 500004) && getEnchantLevel() >= 5){//순백추타표시
				os.writeC(6);
				os.writeC(getItem().getBowDmgup() + (getEnchantLevel() - 4));
			} 
			if (getItem().getBowHitup() != 0) {
				os.writeC(24);
				os.writeC(getItem().getBowHitup());				
			} else if (itemType2 == 2 && getItem().getGrade() == 1 && getEnchantLevel() >= 6 ){//중급악세 활명중표시
				os.writeC(24);
				os.writeC(getItem().getBowHitup() + (getEnchantLevel()-5));
			}
			//활추타표시
			if (getItem().getBowDmgup() != 0) {
				os.writeC(35);
				os.writeC(getItem().getBowDmgup());
			} else if(itemType2 == 2 && (itemId >= 500000 && itemId <= 500004) && getEnchantLevel() >= 5){//순백추타표시
				os.writeC(35);
				os.writeC(getItem().getBowDmgup() + (getEnchantLevel() - 4));
			}
			//마나흡수 옵션표시	
			if (itemId == 126 || itemId == 127|| itemId == 450013|| itemId == 412002
					|| itemId == 450011 || itemId == 450023 || itemId == 450025 
					|| itemId == 450015 || itemId == 413103|| itemId == 134) { 
				os.writeC(16);
			}
			//피흡수 옵션표시
			if (itemId == 412001 || itemId == 450014 ||itemId ==450022 //마족무기 신묘무기
					|| itemId == 450010 || itemId == 450012 ||itemId ==450024){
				os.writeC(34);
			}
			// STR~CHA
			if (getItem().get_addstr() != 0) {
				os.writeC(8);
				os.writeC(getItem().get_addstr());
			}
			if (getItem().get_adddex() != 0) {
				os.writeC(9);
				os.writeC(getItem().get_adddex());
			}
			if (getItem().get_addcon() != 0) {
				os.writeC(10);
				os.writeC(getItem().get_addcon());
			}
			if (getItem().get_addwis() != 0) {
				os.writeC(11);
				os.writeC(getItem().get_addwis());
			}
			if (getItem().get_addint() != 0) {
				os.writeC(12);
				os.writeC(getItem().get_addint());
			}
			if (getItem().get_addcha() != 0) {
				os.writeC(13);
				os.writeC(getItem().get_addcha());
			}
			/** 룸티스의 푸른빛 귀걸이 AC  **/
			if (itemType2 == 2 && itemId == 500008){
				switch(getEnchantLevel()){
				case 5: case 6: os.writeC(2);  os.writeC(getEnchantLevel() - 4);   break;
				case 7: case 8: os.writeC(2);  os.writeC(getEnchantLevel() - 5); break;
				}
				/** 룸티스의 푸른빛 귀걸이  **/
			} else if (itemType2 == 2 && (itemId >= 500000 && itemId <= 500004)){//순백의반지
				switch(getEnchantLevel()){
				case 2: case 3: case 4: os.writeC(2);  os.writeC(getEnchantLevel() - 1);   break;
				case 5: os.writeC(2);  os.writeC(getEnchantLevel() - 2);   break;
				case 6: os.writeC(2);  os.writeC(getEnchantLevel() - 3);   break;
				case 7: os.writeC(2);  os.writeC(getEnchantLevel() - 4);   break;
				case 8: os.writeC(2);  os.writeC(getEnchantLevel() - 5);   break;
				}
			} 

			// HP
			if (itemType2 == 2 && getItem().getGrade() == 1 && getEnchantLevel() != 0 ){ //중급악세 피통증가 옵션표시
				os.writeC(14);
				os.writeH(getItem().get_addhp() + (getEnchantLevel()*2));
			}else if (itemType2 == 2 && getItem().getGrade() == 0 && getEnchantLevel() != 0 ){ //상급악세 피통증가 옵션표시
				os.writeC(14);
				os.writeH(getItem().get_addhp() + (getEnchantLevel()*2));
			}else if (itemType2 == 2 &&(itemId >= 500000 && itemId <= 500004) && getEnchantLevel() == 1 ){//순백반지 피통증가표시
				os.writeC(14);
				os.writeH(getItem().get_addhp() + 15);
			}else if (itemType2 == 2 &&(itemId >= 500000 && itemId <= 500004) && getEnchantLevel() >= 2 ){//순백
				os.writeC(14);
				os.writeH(getItem().get_addhp() + 10 + (getEnchantLevel()*5));
			}else if (itemType2 == 2 && itemId == 500007  && getEnchantLevel() == 1 ){//룸티스 피통증가표시
				os.writeC(14);
				os.writeH(getItem().get_addhp() + 20);
			}else if (itemType2 == 2 && itemId == 500007  && getEnchantLevel() == 2 ){//룸티스 피통증가표시
				os.writeC(14);
				os.writeH(getItem().get_addhp() + 30);
			}else if (itemType2 == 2 && itemId == 500007  && getEnchantLevel() == 3 ){//룸티스 피통증가표시
				os.writeC(14);
				os.writeH(getItem().get_addhp() + 40);
			}else if (itemType2 == 2 && itemId == 500007  && getEnchantLevel() >= 4 ){//룸티스 피통증가표시
				os.writeC(14);
				os.writeH(getItem().get_addhp() + 25 + (getEnchantLevel()*5));
			}else if (getItem().get_addhp() != 0) { //일반무기방어구
				os.writeC(14);
				os.writeH(getItem().get_addhp());
			}
			//피틱옵션표시
			if (getItem().get_addhpr() != 0){
				os.writeC(37);
				os.writeC(getItem().get_addhpr());
			}
			//엠틱옵션표시
			if (getItem().get_addmpr() != 0){
				os.writeC(38);
				os.writeC(getItem().get_addmpr());
			}

			// MP	
			if (itemType2 == 2 && getItem().getGrade() == 2 && getEnchantLevel() != 0 
					|| itemType2 == 2 && itemId == 420008 && getEnchantLevel() != 0 ){ // 테베 호루스의 반지
				os.writeC(32);//하급악세
				os.writeC(getItem().get_addmp() + getEnchantLevel() * 2);
				//룸티스 엠통증가표시
			} else if (itemType2 == 2 && itemId == 500009  && getEnchantLevel() == 1 ) {
				switch(getEnchantLevel()){
				case 1:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+15);
					break;
				case 2:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+20);
					break;
				case 3:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+25);
					break;
				case 4:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+29);
					break;
				case 5:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+33);
					break;
				case 6:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+36);
					break;
				case 7:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+39);
					break;
				case 8:
					os.writeC(32);
					os.writeC(getItem().get_addmp()+41);
					break;
				}
			}else if (getItem().get_addmp() != 0) {//일반무기방어구
				os.writeC(32);
				os.writeC(getItem().get_addmp());
			}
			// MR
			if(itemType2 == 2 && getItem().getGrade() == 1 && getEnchantLevel() > 0){//중급악세마방표시
				os.writeC(15);
				os.writeH(getMr()+ getEnchantLevel() * 1);
			}else if(itemType2 == 2 && itemId == 500009 && getEnchantLevel() >= 1 ){//룸티스마방표시
				os.writeC(15);
				os.writeH(getMr()+ 2+ (getEnchantLevel() * 1));
			}else if (getMr() != 0) {
				os.writeC(15);
				os.writeH(getMr());
			}
			if(itemType2 == 2 && itemId == 500008 && getEnchantLevel() >= 0){//룸티스푸른귀걸이
				os.writeC(39); 
				os.writeS(RoomtisHealingPotion());
			}
			// SP
			if (getsp() != 0) {
				os.writeC(17);
				os.writeH(getsp());
			}
			//스펠파워 지팡이 
			// 장신구 업 포함 SP
			if (getItem().get_addsp() != 0) {
				os.writeC(17);
				os.writeC(getItem().get_addsp());
			} else if (itemType2 == 2 && getItem().getGrade() == 2 && getEnchantLevel() > 5 ){
				os.writeC(17);//하급악세
				os.writeC(getItem().get_addsp() + (getEnchantLevel() - 5));		
			} else if (itemType2 == 2 && itemId == 500009 && getEnchantLevel() == 5 ){ //룸티스 스펠표시
				os.writeC(17);
				os.writeC(getItem().get_addsp() + 1);	
			} else if (itemType2 == 2 && itemId == 500009 && getEnchantLevel() == 6 ){
				os.writeC(17);
				os.writeC(getItem().get_addsp() + 1);	
			} else if (itemType2 == 2 && itemId == 500009 && getEnchantLevel() == 7 ){
				os.writeC(17);
				os.writeC(getItem().get_addsp() + 2);	
			} else if (itemType2 == 2 && itemId == 500009 && getEnchantLevel() == 8 ){
				os.writeC(17);
				os.writeC(getItem().get_addsp() + 2);	
			}
			if (getItem().isHasteItem()) {
				os.writeC(18);
			}

			int bit = 0;
			bit |= getItem().isUseRoyal()   ? 1 : 0;
			bit |= getItem().isUseKnight()  ? 2 : 0;
			bit |= getItem().isUseElf()     ? 4 : 0;
			bit |= getItem().isUseMage()    ? 8 : 0;
			bit |= getItem().isUseDarkelf() ? 16 : 0;
			bit |= getItem().isUseDragonKnight() ? 32 : 0;
			bit |= getItem().isUseBlackwizard() ? 64 : 0;
			bit |= getItem().isUseHighPet() ? 128 : 0;
			os.writeC(7);
			os.writeC(bit);

			if (getItem().get_defense_fire() != 0) {
				os.writeC(27);
				os.writeC(getItem().get_defense_fire());
			} else if (itemType2 == 2 && getItem().getGrade() == 0 && getEnchantLevel() > 0){
				os.writeC(27);
				os.writeC(getItem().get_defense_fire() + getEnchantLevel()* 1);
			}

			if (getItem().get_defense_water() != 0) {
				os.writeC(28);
				os.writeC(getItem().get_defense_water());
			}else if (itemType2 == 2 && getItem().getGrade() == 0 && getEnchantLevel() > 0){
				os.writeC(28);
				os.writeC(getItem().get_defense_water() + getEnchantLevel()* 1);
			}
			if (getItem().get_defense_wind() != 0) {
				os.writeC(29);
				os.writeC(getItem().get_defense_wind());
			}else if (itemType2 == 2 && getItem().getGrade() == 0 && getEnchantLevel() > 0){
				os.writeC(29);
				os.writeC(getItem().get_defense_wind() + getEnchantLevel()* 1);
			}
			if (getItem().get_defense_earth() != 0) {
				os.writeC(30);
				os.writeC(getItem().get_defense_earth());
			}else if (itemType2 == 2 && getItem().getGrade() == 0 && getEnchantLevel() > 0){
				os.writeC(30);
				os.writeC(getItem().get_defense_earth() + getEnchantLevel()* 1);
			}

			if (getItem().get_regist_freeze() != 0) {
				os.writeC(15);
				os.writeH(getItem().get_regist_freeze());
				os.writeC(33);
				os.writeC(1);
			}
			if (getItem().get_regist_stone() != 0) {
				os.writeC(15);
				os.writeH(getItem().get_regist_stone());
				os.writeC(33);
				os.writeC(2);
			}
			if (getItem().get_regist_sleep() != 0) {
				os.writeC(15);
				os.writeH(getItem().get_regist_sleep());
				os.writeC(33);
				os.writeC(3);
			}
			if (getItem().get_regist_blind() != 0) {
				os.writeC(15);
				os.writeH(getItem().get_regist_blind());
				os.writeC(33);
				os.writeC(4);
			}
			if (getItem().get_regist_stun() != 0) {
				os.writeC(15);
				os.writeH(getItem().get_regist_stun());
				os.writeC(33);
				os.writeC(5);
			}
			if (getItem().get_regist_sustain() != 0) {
				os.writeC(15);
				os.writeH(getItem().get_regist_sustain());
				os.writeC(33);
				os.writeC(6);
			}

			if (getRegistLevel() != 0 && itemId >= 490000 && itemId <= 490017){

				os.writeC(39);
				os.writeS(spirit());
			}
			if(itemType2 == 2 && itemId == 500007  && getEnchantLevel() >= 3){//룸티스빨강 리덕표시

				os.writeC(39);
				os.writeS(reduction());
			}



			//			if (getItem.getLuck() != 0) {
			//			os.writeC(20);
			//			os.writeC(val);
			//			}
			//			if (getItem.getDesc() != 0) {
			//			os.writeC(25);
			//			os.writeH(val); // desc.tbl ID
			//			}
			//			if (getItem.getLevel() != 0) {
			//			os.writeC(26);
			//			os.writeH(val);
			//			}
		}
		return os.getBytes();
	}

	class EnchantTimer extends TimerTask {

		public EnchantTimer() {
		}

		@Override
		public void run() {
			try {
				int type = getItem().getType();
				int type2 = getItem().getType2();
				int itemId = getItem().getItemId();
				if (_pc != null && _pc.getInventory().checkItem(itemId)) {
					if (type == 2 && type2 == 2 && isEquipped()) {
						_pc.getAC().addAc(3);
						_pc.sendPackets(new S_OwnCharStatus(_pc));
					}
				}
				setAcByMagic(0);
				setDmgByMagic(0);
				setHolyDmgByMagic(0);
				setHitByMagic(0);
				_pc.sendPackets(new S_ServerMessage(308, getLogName()));
				_isRunning = false;
				_timer = null;
			} catch (Exception e) {
			}
		}
	}

	private int _acByMagic = 0;
	private int _hitByMagic = 0;
	private int _holyDmgByMagic = 0;
	private int _dmgByMagic = 0;

	public int getAcByMagic() {	return _acByMagic;	}
	public void setAcByMagic(int i) {	_acByMagic = i;	}

	public int getDmgByMagic() {	return _dmgByMagic;	}
	public void setDmgByMagic(int i) {	_dmgByMagic = i;	}

	public int getHolyDmgByMagic() {	return _holyDmgByMagic;	}
	public void setHolyDmgByMagic(int i) {	_holyDmgByMagic = i;	}

	public int getHitByMagic() {	return _hitByMagic;	}
	public void setHitByMagic(int i) {	_hitByMagic = i;	}

	public void setSkillArmorEnchant(L1PcInstance pc, int skillId, int skillTime) {
		int type = getItem().getType();
		int type2 = getItem().getType2();
		if (_isRunning) {
			_timer.cancel();
			int itemId = getItem().getItemId();
			if (pc != null && pc.getInventory().checkItem(itemId)) {
				if (type == 2 && type2 == 2 && isEquipped()) {
					pc.getAC().addAc(3);
					pc.sendPackets(new S_OwnCharStatus(pc));
				}
			}
			setAcByMagic(0);
			_isRunning = false;
			_timer = null;
		}

		if (type == 2 && type2 == 2 && isEquipped()) {
			pc.getAC().addAc(-3);
			pc.sendPackets(new S_OwnCharStatus(pc));
		}
		setAcByMagic(3);
		_pc = pc;
		_timer = new EnchantTimer();
		(new Timer()).schedule(_timer, skillTime);
		_isRunning = true;
	}

	public void setSkillWeaponEnchant(L1PcInstance pc, int skillId, int skillTime) {
		if (getItem().getType2() != 1) {
			return;
		}
		if (_isRunning) {
			_timer.cancel();
			setDmgByMagic(0);
			setHolyDmgByMagic(0);
			setHitByMagic(0);
			_isRunning = false;
			_timer = null;
		}
		switch(skillId) {
		case L1SkillId.HOLY_WEAPON:
			setHolyDmgByMagic(1);
			setHitByMagic(1);
			break;

		case L1SkillId.ENCHANT_WEAPON:
			setDmgByMagic(2);
			break;

		case L1SkillId.BLESS_WEAPON:
			setDmgByMagic(2);
			setHitByMagic(2);
			break;

		case L1SkillId.SHADOW_FANG:
			setDmgByMagic(5);
			break;
		default:
			break;
		}
		_pc = pc;
		_timer = new EnchantTimer();
		(new Timer()).schedule(_timer, skillTime);
		_isRunning = true;
	}
	
	private String reduction() {
		int lvl = getEnchantLevel();
		String in = "";
		switch(lvl){
		case 3:
			in = "데미지경감: 1";
			break;
		case 4:
			in = "데미지경감: 1";
			break;
		case 5:
			in = "데미지경감: 2";
			break;
		case 6:
			in = "데미지경감: 2";
			break;
		case 7:
			in = "데미지경감: 3";
			break;
		case 8:
			in = "데미지경감: 3";
			break;		  
		default:
			break;
		}
		return in;
	}

	private String spirit() {
		int lvl = getRegistLevel();
		String in = "";
		switch(lvl){
		case 1:
			in = "정령의 인(I)";
			break;
		case 2:
			in = "정령의 인(II)";
			break;
		case 3:
			in = "정령의 인(III)";
			break;
		case 4:
			in = "정령의 인(IV)";
			break;
		case 5:
			in = "정령의 인(V)";
			break;
		default:
			break;
		}
		return in;
	}

	private String RoomtisHealingPotion() {
		int lvl = getEnchantLevel();
		String in = "";
		switch(lvl){
		case 0:
			in = "물약효율:+2%+2";
			break;
		case 1:
			in = "물약효율:+4%+4";
			break;
		case 2:
			in = "물약효율:+6%+6";
			break;
		case 3:
			in = "물약효율:+8%+8";
			break;
		case 4:
			in = "물약효율:+10%+10";
			break;
		case 5:
			in = "물약효율:+12%+12";
			break;
		case 6:
			in = "물약효율:+14%+14";
			break;
		case 7:
			in = "물약효율:+16%+16";
			break;
		case 8:
			in = "물약효율:+18%+18";
			break;
		default:
			break;
		}
		return in;
	}

	public void startItemOwnerTimer(L1PcInstance pc) {
		setItemOwner(pc);
		L1ItemOwnerTimer timer = new L1ItemOwnerTimer(this, 10000);
		timer.begin();
	}
	private L1EquipmentTimer _equipmentTimer;

	public void startEquipmentTimer(L1PcInstance pc) {
		if (getRemainingTime() > 0) {
			_equipmentTimer = new L1EquipmentTimer(pc, this);
			Timer timer = new Timer(true);
			timer.scheduleAtFixedRate(_equipmentTimer, 1000, 1000);
		}
	}

	public void stopEquipmentTimer() {
		if (getRemainingTime() > 0) {
			_equipmentTimer.cancel();
			_equipmentTimer = null;
		}
	}
	private L1PcInstance _itemOwner;

	public L1PcInstance getItemOwner() {	return _itemOwner;	}
	public void setItemOwner(L1PcInstance pc) {	_itemOwner = pc;	}

	private boolean _isNowLighting = false;

	public boolean isNowLighting() {	return _isNowLighting;	}
	public void setNowLighting(boolean flag) {	_isNowLighting = flag;	}
	private int _secondId;

	public int getSecondId() {
		return _secondId;
	}

	public void setSecondId(int i) {
		_secondId = i;
	}

	private int _roundId;

	public int getRoundId() {
		return _roundId;
	}

	public void setRoundId(int i) {
		_roundId = i;
	}

	private int _ticketId = -1; // 티겟 번호

	public int getTicketId() {
		return _ticketId;
	}

	public void setTicketId(int i) {
		_ticketId = i;
	} 
	private int _DropMobId = 0;

	public int isDropMobId() {	return _DropMobId;	}
	public void setDropMobId(int i) {	_DropMobId = i;	}

	private boolean _isWorking = false;

	public boolean isWorking() {	return _isWorking;	}
	public void setWorking(boolean flag) {	_isWorking = flag;	}

	//아이템을 분당체크해서 삭제하기 위해서 추가!!
	private int _deleteItemTime = 0;
	public int get_DeleteItemTime(){
		return _deleteItemTime;
	}
	public void add_DeleteItemTime(){
		_deleteItemTime++;
	}
	public void init_DeleteItemTime(){
		_deleteItemTime = 0;
	}
}
