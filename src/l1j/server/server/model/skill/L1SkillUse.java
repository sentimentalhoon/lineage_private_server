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

package l1j.server.server.model.skill;

import static l1j.server.server.model.skill.L1SkillId.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Cube;
import l1j.server.server.model.L1CurseParalysis;
import l1j.server.server.model.L1EffectSpawn;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Magic;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PinkName;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1AuctionBoardInstance;
import l1j.server.server.model.Instance.L1BoardInstance;
import l1j.server.server.model.Instance.L1CrownInstance;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1DoorInstance;
import l1j.server.server.model.Instance.L1DwarfInstance;
import l1j.server.server.model.Instance.L1EffectInstance;
import l1j.server.server.model.Instance.L1FieldObjectInstance;
import l1j.server.server.model.Instance.L1FurnitureInstance;
import l1j.server.server.model.Instance.L1GuardInstance;
import l1j.server.server.model.Instance.L1HousekeeperInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1LittleBugInstance;
import l1j.server.server.model.Instance.L1MerchantInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.Instance.L1TeleporterInstance;
import l1j.server.server.model.Instance.L1TowerInstance;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.serverpackets.S_ChangeHeading;
import l1j.server.server.serverpackets.S_ChangeName;
import l1j.server.server.serverpackets.S_ChangeShape;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_CurseBlind;
import l1j.server.server.serverpackets.S_Dexup;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_DoActionShop;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_Poison;
import l1j.server.server.serverpackets.S_RangeSkill;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_ShowPolyList;
import l1j.server.server.serverpackets.S_ShowSummonList;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillIconAura;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillIconShield;
import l1j.server.server.serverpackets.S_SkillIconWindShackle;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_Sound;
import l1j.server.server.serverpackets.S_Strup;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_TrueTarget;
import l1j.server.server.serverpackets.S_UseAttackSkill;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.L1SpawnUtil;

public class L1SkillUse {
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_LOGIN = 1;
	public static final int TYPE_SPELLSC = 2;
	public static final int TYPE_NPCBUFF = 3;
	public static final int TYPE_GMBUFF = 4;

	private L1Skills _skill;
	private int _skillId;
	private int _getBuffDuration;
	private int _shockStunDuration;
	private int _getBuffIconDuration;
	private int _targetID;
	private int _mpConsume = 0;
	private int _hpConsume = 0;
	private int _targetX = 0;
	private int _targetY = 0;
	private String _message = null;
	private int _skillTime = 0;
	private int _type = 0;
	private boolean _isPK = false;
	private int _bookmarkId = 0;
	private int _itemobjid = 0;
	private boolean _checkedUseSkill = false;
	private int _leverage = 10;
	private boolean _isFreeze = false;
	/**** A111  주석 ****/
	//private boolean _isCounterMagic = true;

	private L1Character _user = null;
	private L1Character _target = null;

	private L1PcInstance _player = null;
	private L1NpcInstance _npc = null;
	private L1NpcInstance _targetNpc = null;

	private int _calcType;
	private static final int PC_PC = 1;
	private static final int PC_NPC = 2;
	private static final int NPC_PC = 3;
	private static final int NPC_NPC = 4;
	private Random random = new Random(System.nanoTime());
	private ArrayList<TargetStatus> _targetList;
	private boolean _isGlanceCheckFail = false;

	private static Logger _log = Logger.getLogger(L1SkillUse.class.getName());

	/**** A111  주석 ****/
	/*
	private static final int[] CAST_WITH_INVIS = { 1, 2, 3, 5, 8, 9, 12, 13,
		14, 19, 21, 26, 31, 32, 35, 37, 42, 43, 44, 48, 49, 52, 54, 55, 57,
		60, 61, 63, 67, 68, 69, 72, 73, 75, 78, 79, REDUCTION_ARMOR,
		BOUNCE_ATTACK, SOLID_CARRIAGE, COUNTER_BARRIER, 97, 98, 99, 100,
		101, 102, 104, 105, 106, 107, 109, 110, 111, 113, 114, 115, 116,
		117, 118, 129, 130, 131, 133, 134, 137, 138, 146, 147, 148, 149,
		150, 151, 155, 156, 158, 159, 163, 164, 165, 166, 168, 169, 170,
		171, 181, SOUL_OF_FLAME, ADDITIONAL_FIRE, 
		IllUSION_OGRE,PATIENCE, IllUSION_DIAMONDGOLEM,IllUSION_LICH,IllUSION_AVATAR};

	private static final int[] EXCEPT_COUNTER_MAGIC = { 1, 2, 3, 5, 8, 9, 12,
		13, 14, 19, 21, 26, 31, 32, 35, 37, 42, 43, 44, 48, 49, 52, 54, 55,
		57, 60, 61, 63, 67, 68, 69, 72, 73, 75, 78, 79, SHOCK_STUN,
		REDUCTION_ARMOR, BOUNCE_ATTACK, SOLID_CARRIAGE, COUNTER_BARRIER,
		97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 109, 110, 111, 113,
		114, 115, 116, 117, 118, 129, 130, 131, 132, 134, 137, 138, 146,
		147, 148, 149, 150, 151, 155, 156, 158, 159, 161, 163, 164, 165,
		166, 168, 169, 170, 171, 181, SOUL_OF_FLAME, ADDITIONAL_FIRE, DRAGON_SKIN,
		FOU_SLAYER,SCALES_EARTH_DRAGON,SCALES_FIRE_DRAGON,SCALES_WATER_DRAGON, MIRROR_IMAGE,
		IllUSION_OGRE,PATIENCE, IllUSION_DIAMONDGOLEM,IllUSION_LICH,IllUSION_AVATAR,
		INSIGHT,10026,
		10027, 10028, 10029, 30060, 30000, 30078, 30079, 30011, 30081, 30082, 30083, 
		30080, 30084, 30010, 30002, 30086 };
	 */
	public L1SkillUse() {
	}

	private static class TargetStatus {
		private L1Character _target = null;
		//private boolean _isAction = false; 
		//private boolean _isSendStatus = false; 
		private boolean _isCalc = true; 

		public TargetStatus(L1Character _cha) {	_target = _cha;	}
		public L1Character getTarget() {	return _target;	}

		public TargetStatus(L1Character _cha, boolean _flg) {	_isCalc = _flg;	}
		public boolean isCalc() {	return _isCalc;	}

		/*public void isAction(boolean _flg) {_isAction = _flg;	}
		public boolean isAction() {	return _isAction;	}

		public void isSendStatus(boolean _flg) {	_isSendStatus = _flg;	}
		public boolean isSendStatus() {	return _isSendStatus;	}*/
	}

	public void setLeverage(int i) {	_leverage = i;	}
	public int getLeverage() {	return _leverage;	}

	private boolean isCheckedUseSkill() {	return _checkedUseSkill;	}
	private void setCheckedUseSkill(boolean flg) {	_checkedUseSkill = flg;	}

	public boolean checkUseSkill(L1PcInstance player, int skillid,
			int target_id, int x, int y, String message, int time, int type,
			L1Character attacker) {

		//존재버그 관련 추가
		if (player instanceof L1PcInstance) {
			L1PcInstance jonje = L1World.getInstance().getPlayer(player.getName());
			if (jonje == null && player.getAccessLevel() != Config.GMCODE) {
				player.sendPackets(new S_SystemMessage("존재버그 강제종료! 재접속하세요"));
				player.sendPackets(new S_Disconnect()); 
				return false;
			}
		}

		setCheckedUseSkill(true);
		_targetList = new ArrayList<TargetStatus>();

		_skill = SkillsTable.getInstance().getTemplate(skillid);
		_skillId = skillid;
		_targetX = x;
		_targetY = y;
		_message = message;
		_skillTime = time;
		_type = type;
		boolean checkedResult = true;

		if (attacker == null) {
			// pc
			_player = player;
			_user = _player;
		} else {
			// npc
			_npc = (L1NpcInstance) attacker;
			_user = _npc;
		}

		if (_skill.getTarget().equals("none")) {
			_targetID = _user.getId();
			_targetX = _user.getX();
			_targetY = _user.getY();
		} else {
			_targetID = target_id;
		}

		if (type == TYPE_NORMAL) { 
			checkedResult = isNormalSkillUsable();
		} else if (type == TYPE_SPELLSC) { 
			checkedResult = isSpellScrollUsable();
		} else if (type == TYPE_NPCBUFF) {
			checkedResult = true;
		}
		if (!checkedResult) {
			return false;
		}

		if (_skillId == FIRE_WALL 
				|| _skillId == LIFE_STREAM
				|| _skillId == CUBE_IGNITION 
				|| _skillId == CUBE_QUAKE 
				|| _skillId == CUBE_SHOCK 
				|| _skillId == CUBE_BALANCE) {
			return true;
		}

		L1Object l1object = L1World.getInstance().findObject(_targetID);
		if (l1object instanceof L1LittleBugInstance) {
			return false;
		}
		if (l1object instanceof L1ItemInstance) {
			_log.fine("skill target item name: "
					+ ((L1ItemInstance) l1object).getViewName());
			return false;
		}
		if (_user instanceof L1PcInstance) {
			if (l1object instanceof L1PcInstance) {
				_calcType = PC_PC;
			} else {
				_calcType = PC_NPC;
				_targetNpc = (L1NpcInstance) l1object;
			}
		} else if (_user instanceof L1NpcInstance) {
			if (l1object instanceof L1PcInstance) {
				_calcType = NPC_PC;
			} else if (_skill.getTarget().equals("none")) {
				_calcType = NPC_PC;
			} else {
				_calcType = NPC_NPC;
				_targetNpc = (L1NpcInstance) l1object;
			}
		}

		if (_skillId == TELEPORT || _skillId == MASS_TELEPORT) {
			_bookmarkId = target_id;
		}

		if (_skillId == CREATE_MAGICAL_WEAPON 
				|| _skillId == BRING_STONE
				|| _skillId == BLESSED_ARMOR 
				|| _skillId == ENCHANT_WEAPON 
				|| _skillId == SHADOW_FANG) {
			_itemobjid = target_id;
		}
		_target = (L1Character) l1object;

		if (!(_target instanceof L1MonsterInstance)
				&& _skill.getTarget().equals("attack")
				&& _user.getId() != target_id) {
			_isPK = true; 
		}
		if (!(l1object instanceof L1Character)) { 
			checkedResult = false;
		}

		makeTargetList();

		if (_targetList.size() == 0 
				&& (_user instanceof L1NpcInstance)) {
			checkedResult = false;
		}
		return checkedResult;
	}

	/**
	 * 통상의 스킬 사용시에 사용자 상태로부터 스킬이 사용 가능한가 판단한다
	 * 
	 * @return false 스킬이 사용 불가능한 상태인 경우
	 */

	private boolean isNormalSkillUsable() {
		if (_user instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) _user;
			if (pc.isParalyzed()) return false;
			if ((pc.isInvisble() || pc.isInvisDelay()) && !_skill.canCastWithInvis()) { 
				return false;
			}
			if (pc.getInventory().getWeight240() >= 200) { // 중량 오버이면 스킬을 사용할 수 없다
				pc.sendPackets(new S_ServerMessage(316));
				return false;
			}
			int polyId = pc.getGfxId().getTempCharGfx();
			L1PolyMorph poly = PolyTable.getInstance().getTemplate(polyId);
			if (poly != null && !poly.canUseSkill()) {
				pc.sendPackets(new S_ServerMessage(285)); 
				return false;
			}
			int castle_id = L1CastleLocation.getCastleIdByArea(pc);////////////공성전 지역에서 마법 스킬 제한
			if (castle_id != 0){
				if(_skillId == 69 || _skillId == 157 || _skillId == 58 || 
						_skillId == 50 || _skillId == 59 || _skillId == 80 || _skillId == 78
						|| _skillId == 51 || _skillId ==154 || _skillId == 162 ){    // 서먼 계열 마법 차단 
					pc.sendPackets(new S_ServerMessage(563));  // 여기에서는 사용할 수 없습니다.
					return false;
				}
			}

			if (pc.getMapId() == 4 || pc.getMapId() == 350) {
				if (_skillId == 220 ) {
					pc.sendPackets(new S_ServerMessage(563));  // 여기에서는 사용할 수 없습니다.
					return false;
				}
			}
			if (pc.getMapId() == 5302){
				return false;
			}

			if (!isAttrAgrees()) { return false; }

			if (_skillId == ELEMENTAL_PROTECTION && pc.getElfAttr() == 0) {
				pc.sendPackets(new S_ServerMessage(280)); 
				return false;
			}

			if (pc.isSkillDelay()) {
				return false;
			}

			if (pc.getSkillEffectTimerSet().hasSkillEffect(SILENCE)
					|| pc.getSkillEffectTimerSet().hasSkillEffect(AREA_OF_SILENCE)
					|| pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_POISON_SILENCE)
					|| pc.getSkillEffectTimerSet().hasSkillEffect(CONFUSION)) {
				if (_skillId < SHOCK_STUN || _skillId > COUNTER_BARRIER){
					pc.sendPackets(new S_ServerMessage(285)); 
					return false;
				}
			}

			/** 솔리드 캐리지 방패 착용 */
			if (_skillId == SOLID_CARRIAGE) { // 솔리드 캐리지
				if (pc.getInventory().checkEquipped(420000) 
						|| pc.getInventory().checkEquipped(420001)
						|| pc.getInventory().checkEquipped(420002) 
						|| pc.getInventory().checkEquipped(420003)
						|| pc.getInventory().checkEquipped(500019)
						|| pc.getInventory().checkEquipped(426006)
						){
					pc.sendPackets(new S_ServerMessage(1008)); 
					return false;
				}
			} 

			if (_skillId == DISINTEGRATE && pc.getLawful() < 500) {
				pc.sendPackets(new S_ServerMessage(352, "$967")); 
				return false;
			}

			if (isItemConsume() == false && !_player.isGm()) { 
				_player.sendPackets(new S_ServerMessage(299)); 
				return false;
			}
		}
		else if (_user instanceof L1NpcInstance) {

			if(_user.getSkillEffectTimerSet().hasSkillEffect(CONFUSION))
				return false;

			if (_user.getSkillEffectTimerSet().hasSkillEffect(SILENCE)) {
				_user.getSkillEffectTimerSet().killSkillEffectTimer(SILENCE);
				return false;
			}
		}

		if (!isHPMPConsume()) { 
			return false;
		}
		return true;
	}
	private boolean isSpellScrollUsable() {
		L1PcInstance pc = (L1PcInstance) _user;
		if (pc.isParalyzed()) return false;
		if ((pc.isInvisble() || pc.isInvisDelay()) && !_skill.canCastWithInvis()) {
			return false;
		}

		return true;
	}
	/**** A111  주석 ****/
	/*
	private boolean isInvisUsableSkill() {
		for (int skillId : CAST_WITH_INVIS) {
			if (skillId == _skillId) {
				return true;
			}
		}
		return false;
	}
	 */
	public void handleCommands(L1PcInstance player, int skillId, int targetId,
			int x, int y, String message, int timeSecs, int type) {
		try{
			L1Character attacker = null;
			handleCommands(player, skillId, targetId, x, y, message, timeSecs, type, attacker);
		} catch (Exception e) {
			System.out.println("skillId : "+skillId+" / attacker : "+ player.getName());
			_log.log(Level.SEVERE, "", e);
		}
	}

	public void handleCommands(L1PcInstance player, int skillId, int targetId,
			int x, int y, String message, int timeSecs, int type, L1Character attacker) {

		try {
			if (!isCheckedUseSkill()) {
				boolean isUseSkill = checkUseSkill(player, skillId, targetId, x, y, message, timeSecs, type, attacker);
				if (!isUseSkill) {
					failSkill();
					return;
				}
			}		

			switch(type){
			case TYPE_NORMAL: 
				if (!_isGlanceCheckFail || _skill.getArea() > 0 || _skill.getTarget().equals("none")) {
					runSkill();
					useConsume();
					sendGrfx(true);
					sendFailMessageHandle();
					setDelay();
					pinkname();
				}
				break;
			case TYPE_LOGIN: 
				runSkill();
				break;
			case TYPE_SPELLSC:
				runSkill();
				sendGrfx(true);
				break;
			case TYPE_GMBUFF:
				runSkill();
				sendGrfx(false);
				break;
			case TYPE_NPCBUFF:
				runSkill();
				sendGrfx(true);
				break;
			default: break;
			}
			setCheckedUseSkill(false);
		} catch (Exception e) {
			System.out.println("skillId : "+skillId+" / attacker : "+attacker.getName());
			_log.log(Level.SEVERE, "", e);
		}
	}

	private void pinkname() {
		if ((_skill.getTarget().equals("buff") && _calcType == PC_PC)
				&& CharPosUtil.getZoneType(_user) == 0 && CharPosUtil.getZoneType(_target) != 1){
			if(_skill.getType() == L1Skills.TYPE_HEAL
					|| _skill.getType() == L1Skills.TYPE_CHANGE 
					|| _skill.getType() == L1Skills.TYPE_PROBABILITY){
				if (_target instanceof L1PcInstance) {
					L1PcInstance target = (L1PcInstance)_target;
					if(target.isPinkName()){
						L1PinkName.onAction(target, _user);
					}
				}
			}
		}
	}

	private void failSkill() {
		setCheckedUseSkill(false);
		if (_skillId == TELEPORT 
				|| _skillId == MASS_TELEPORT
				|| _skillId == TELEPORT_TO_MOTHER) {
			_player.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));
		}
	}

	private boolean isTarget(L1Character cha) throws Exception {
		boolean _flg = false;

		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.isGhost() || pc.isGmInvis()) {
				return false;
			}
		}
		if (_calcType == NPC_PC
				&& (cha instanceof L1PcInstance 
						|| cha instanceof L1PetInstance 
						|| cha instanceof L1SummonInstance)) {
			_flg = true;
		}

		if (cha instanceof L1DoorInstance) {
			if (cha.getMaxHp() == 0 || cha.getMaxHp() == 1) {
				return false;
			}
		}

		if((_skill.getTarget().equals("attack") || _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _calcType == NPC_PC 
				&& cha instanceof L1PcInstance 
				&& _user instanceof L1SummonInstance) {
			L1SummonInstance summon = (L1SummonInstance) _user;

			if(cha.getId() == summon.getMaster().getId()){ return false; }
			if(CharPosUtil.getZoneType(cha) == 1){ return false; }
		}

		if((_skill.getTarget().equals("attack") || _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _calcType == NPC_PC 
				&& cha instanceof L1PcInstance 
				&& _user instanceof L1PetInstance) {
			L1PetInstance pet = (L1PetInstance) _user;
			if(cha.getId() == pet.getMaster().getId()){ return false; }
			if(CharPosUtil.getZoneType(cha) == 1){ return false; }
		}

		if (cha instanceof L1DollInstance && _skillId != HASTE) {
			return false;
		}

		if (_calcType == PC_NPC
				&& _target instanceof L1NpcInstance
				&& !(_target instanceof L1PetInstance)
				&& !(_target instanceof L1SummonInstance)
				&& (cha instanceof L1PetInstance
						|| cha instanceof L1SummonInstance 
						|| cha instanceof L1PcInstance)) {
			return false;
		}
		if (_calcType == PC_NPC
				&& _target instanceof L1NpcInstance
				&& !(_target instanceof L1GuardInstance)
				&& cha instanceof L1GuardInstance) {
			return false;
		}
		if ((_skill.getTarget().equals("attack") 
				|| _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _calcType == NPC_PC
				&& !(cha instanceof L1PetInstance)
				&& !(cha instanceof L1SummonInstance)
				&& !(cha instanceof L1PcInstance)) {
			return false;
		}

		if ((_skill.getTarget().equals("attack")
				|| _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _calcType == NPC_NPC
				&& _user instanceof L1MonsterInstance
				&& cha instanceof L1MonsterInstance) {
			return false;
		}

		if (_skill.getTarget().equals("none")
				&& _skill.getType() == L1Skills.TYPE_ATTACK
				&& (cha instanceof L1AuctionBoardInstance
						|| cha instanceof L1BoardInstance
						|| cha instanceof L1CrownInstance
						|| cha instanceof L1DwarfInstance
						|| cha instanceof L1EffectInstance
						|| cha instanceof L1FieldObjectInstance
						|| cha instanceof L1FurnitureInstance
						|| cha instanceof L1HousekeeperInstance
						|| cha instanceof L1MerchantInstance
						|| cha instanceof L1TeleporterInstance)) {
			return false;
		}

		if (_skill.getType() == L1Skills.TYPE_ATTACK && cha.getId() == _user.getId()) {
			return false;
		}

		if (cha.getId() == _user.getId() && _skillId == HEAL_ALL) {
			return false;
		}

		if (((_skill.getTargetTo() & L1Skills.TARGET_TO_PC) == L1Skills.TARGET_TO_PC
				|| (_skill.getTargetTo() & L1Skills.TARGET_TO_CLAN) == L1Skills.TARGET_TO_CLAN 
				|| (_skill.getTargetTo() & L1Skills.TARGET_TO_PARTY) == L1Skills.TARGET_TO_PARTY)
				&& cha.getId() == _user.getId() && _skillId != HEAL_ALL) {
			return true; 
		}

		if (_user instanceof L1PcInstance
				&& (_skill.getTarget().equals("attack") || _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _isPK == false) {
			if (cha instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (_player.getId() == summon.getMaster().getId()) {
					return false;
				}
			} else if (cha instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) cha;
				if (_player.getId() == pet.getMaster().getId()) {
					return false;
				}
			}
		}

		if ((_skill.getTarget().equals("attack")
				|| _skill.getType() == L1Skills.TYPE_ATTACK)
				&& !(cha instanceof L1MonsterInstance)
				&& _isPK == false
				&& _target instanceof L1PcInstance) {
			L1PcInstance enemy = (L1PcInstance) cha;
			if (_skillId == COUNTER_DETECTION && CharPosUtil.getZoneType(enemy) != 1
					&& (cha.getSkillEffectTimerSet().hasSkillEffect(INVISIBILITY)
							|| cha.getSkillEffectTimerSet().hasSkillEffect(BLIND_HIDING))) {
				return true; 
			}
			if (_player.getClanid() != 0 && enemy.getClanid() != 0) {
				for (L1War war : L1World.getInstance().getWarList()) {
					if (war.CheckClanInWar(_player.getClanname())) { 
						if (war.CheckClanInSameWar(_player.getClanname(), enemy.getClanname())) {
							if (L1CastleLocation.checkInAllWarArea(enemy.getX(), enemy.getY(), enemy.getMapId())) {
								return true;
							}
						}
					}
				}
			}
			return false; 
		}

		if (CharPosUtil.glanceCheck(_user, cha.getX(), cha.getY()) == false
				&& _skill.isThrough() == false) {
			if (!(_skill.getType() == L1Skills.TYPE_CHANGE || _skill.getType() == L1Skills.TYPE_RESTORE)) {
				_isGlanceCheckFail = true;
				return false; 
			}
		}

		if ((cha.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
				|| cha.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)
				|| cha.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH))
				&& (_skillId == ICE_LANCE 
				|| _skillId == FREEZING_BLIZZARD 
				|| _skillId == FREEZING_BREATH  
				|| _skillId == SHOCK_STUN)) {
			return false; 
		}

		if ((cha.getSkillEffectTimerSet().hasSkillEffect(MOB_BASILL) && _skillId == MOB_BASILL)
				|| (cha.getSkillEffectTimerSet().hasSkillEffect(MOB_COCA) && _skillId == MOB_COCA)) {
			return false; 
		}

		if (cha.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) {
			if (_skillId != WEAPON_BREAK && _skillId != CANCELLATION // 확률계
					&& _skill.getType() != L1Skills.TYPE_HEAL // 힐 계
					&& _skill.getType() != L1Skills.TYPE_CHANGE) { // 버프 계
				return false; 		
			}
		}

		if (!(cha instanceof L1MonsterInstance)
				&& (_skillId == TAMING_MONSTER || _skillId == CREATE_ZOMBIE)) {
			return false; 
		}
		if (cha.isDead()
				&& (_skillId != CREATE_ZOMBIE
				&& _skillId != RESURRECTION
				&& _skillId != GREATER_RESURRECTION
				&& _skillId != CALL_OF_NATURE)) {
			return false; 
		}

		if (!(cha instanceof L1TowerInstance || cha instanceof L1DoorInstance)
				&& cha.isDead() == false
				&& (_skillId == CREATE_ZOMBIE
				|| _skillId == RESURRECTION
				|| _skillId == GREATER_RESURRECTION
				|| _skillId == CALL_OF_NATURE)) {
			return false; 
		}

		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) { 
				if (_skillId == CURSE_BLIND 
						|| _skillId == WEAPON_BREAK
						|| _skillId == DARKNESS 
						|| _skillId == WEAKNESS
						|| _skillId == DISEASE 
						|| _skillId == FOG_OF_SLEEPING
						|| _skillId == MASS_SLOW 
						|| _skillId == SLOW
						|| _skillId == CANCELLATION 
						|| _skillId == SILENCE
						|| _skillId == DECAY_POTION 
						|| _skillId == MASS_TELEPORT
						|| _skillId == DETECTION 
						|| _skillId == HORROR_OF_DEATH 
						|| _skillId == COUNTER_DETECTION 
						|| _skillId == GUARD_BREAK
						//|| _skillId == ERASE_MAGIC 
						|| _skillId == ENTANGLE 
						|| _skillId == FEAR
						|| _skillId == PHYSICAL_ENCHANT_DEX
						|| _skillId == PHYSICAL_ENCHANT_STR
						|| _skillId == BLESS_WEAPON 
						|| _skillId == EARTH_SKIN
						//|| _skillId == IMMUNE_TO_HARM
						|| _skillId == REMOVE_CURSE 
						|| _skillId == CONFUSION
						|| _skillId == MOB_SLOW_1 
						|| _skillId == MOB_SLOW_18 
						|| _skillId == MOB_WEAKNESS_1 
						|| _skillId == MOB_DISEASE_1
						|| _skillId == MOB_BASILL 
						|| _skillId == MOB_SHOCKSTUN_30
						|| _skillId == MOB_RANGESTUN_19 
						|| _skillId == MOB_RANGESTUN_18
						|| _skillId == MOB_DISEASE_30 
						|| _skillId == MOB_WINDSHACKLE_1
						|| _skillId == MOB_COCA 
						|| _skillId == MOB_CURSEPARALYZ_19
						|| _skillId == MOB_CURSEPARALYZ_18) {
					return true;
				} else {
					return false;
				}
			}
		}

		if (cha instanceof L1NpcInstance) {
			int hiddenStatus = ((L1NpcInstance) cha).getHiddenStatus();
			if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_SINK) {
				if (_skillId == DETECTION || _skillId == COUNTER_DETECTION) {
					return true;
				} else {
					return false;
				}
			} else if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_FLY) {
				return false;
			}
		}

		if ((_skill.getTargetTo() & L1Skills.TARGET_TO_PC) == L1Skills.TARGET_TO_PC 
				&& cha instanceof L1PcInstance) {
			_flg = true;
		} else if ((_skill.getTargetTo() & L1Skills.TARGET_TO_NPC) == L1Skills.TARGET_TO_NPC
				&& (cha instanceof L1MonsterInstance || cha instanceof L1NpcInstance
						|| cha instanceof L1SummonInstance || cha instanceof L1PetInstance)) {
			_flg = true;
		} else if ((_skill.getTargetTo() & L1Skills.TARGET_TO_PET) == L1Skills
				.TARGET_TO_PET && _user instanceof L1PcInstance) {
			if (cha instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (summon.getMaster() != null) {
					if (_player.getId() == summon.getMaster().getId()) {
						_flg = true;
					}
				}
			}
			if (cha instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) cha;
				if (pet.getMaster() != null) {
					if (_player.getId() == pet.getMaster().getId()) {
						_flg = true;
					}
				}
			}
		}

		if (_calcType == PC_PC && cha instanceof L1PcInstance) {
			if ((_skill.getTargetTo() & L1Skills.TARGET_TO_CLAN) == L1Skills.TARGET_TO_CLAN
					&& ((_player.getClanid() != 0 && _player.getClanid() == ((L1PcInstance) cha).getClanid()) || _player
							.isGm())) {
				return true;
			}
			if ((_skill.getTargetTo() & L1Skills.TARGET_TO_PARTY) == L1Skills.TARGET_TO_PARTY
					&& (_player.getParty().isMember((L1PcInstance) cha) || _player.isGm())) {
				return true;
			}
		}

		return _flg;
	}

	private void makeTargetList() {
		try {
			if (_type == TYPE_LOGIN) {
				_targetList.add(new TargetStatus(_user));
				return;
			}
			if (_skill.getTargetTo() == L1Skills.TARGET_TO_ME
					&& (_skill.getType() & L1Skills.TYPE_ATTACK) != L1Skills.TYPE_ATTACK) {
				_targetList.add(new TargetStatus(_user)); 
				return;
			}

			if (_skill.getRanged() != -1) {
				if (_user.getLocation().getTileLineDistance(_target.getLocation()) > _skill.getRanged()) {
					return; 
				}
			} else {
				if (!_user.getLocation().isInScreen(_target.getLocation())) {
					return; 
				}
			}

			if (isTarget(_target) == false && !(_skill.getTarget().equals("none"))) {
				return;
			}

			if (_skillId == LIGHTNING || _skillId == FREEZING_BREATH) {
				for (L1Object tgobj : L1World.getInstance().getVisibleLineObjects(_user, _target)) {
					if (tgobj == null) {
						continue;
					}
					if (!(tgobj instanceof L1Character)) {
						continue;
					}
					L1Character cha = (L1Character) tgobj;
					if (isTarget(cha) == false) {
						continue;
					}
					_targetList.add(new TargetStatus(cha));
				}
				return;
			}

			if (_skill.getArea() == 0) {				
				if (!CharPosUtil.glanceCheck(_user, _target.getX(), _target.getY())) {
					if ((_skill.getType() & L1Skills.TYPE_ATTACK) == L1Skills.TYPE_ATTACK){ 
						_targetList.add(new TargetStatus(_target, false)); 
						return;
					}
				}
				_targetList.add(new TargetStatus(_target));
			} else { 
				if (!_skill.getTarget().equals("none")) {
					_targetList.add(new TargetStatus(_target));
				}

				if (_skillId != 49
						&& !(_skill.getTarget().equals("attack") || _skill
								.getType() == L1Skills.TYPE_ATTACK)) {
					_targetList.add(new TargetStatus(_user));
				}

				List<L1Object> objects;
				if (_skill.getArea() == -1) {
					objects = L1World.getInstance().getVisibleObjects(_user);
				} else {
					objects = L1World.getInstance().getVisibleObjects(_target, _skill.getArea());
				}
				for (L1Object tgobj : objects) {
					if (tgobj == null) {
						continue;
					}
					if (!(tgobj instanceof L1Character)) { 
						continue;
					}
					L1Character cha = (L1Character) tgobj;
					if (!isTarget(cha)) {
						continue;
					}
					_targetList.add(new TargetStatus(cha));
				}
				return;
			}

		} catch (Exception e) {
			_log.finest("exception in L1Skilluse makeTargetList" + e);
		}
	}

	private void sendHappenMessage(L1PcInstance pc) {
		int msgID = _skill.getSysmsgIdHappen();
		if (msgID > 0) {
			pc.sendPackets(new S_ServerMessage(msgID));
		}
	}

	private void sendFailMessageHandle() {
		if (_skill.getType() != L1Skills.TYPE_ATTACK
				&& !_skill.getTarget().equals("none")
				&& _targetList.size() == 0) {
			sendFailMessage();
		}
	}

	private void sendFailMessage() {
		int msgID = _skill.getSysmsgIdFail();
		if (msgID > 0 && (_user instanceof L1PcInstance)) {
			_player.sendPackets(new S_ServerMessage(msgID));
		}
	}

	private boolean isAttrAgrees() {
		int magicattr = _skill.getAttr();
		if (_user instanceof L1NpcInstance) { 
			return true;
		}

		if ((_skill.getSkillLevel() >= 17 && _skill.getSkillLevel() <= 22 && magicattr != 0) 
				&& (magicattr != _player.getElfAttr() && !_player.isGm())) {
			return false;
		}
		return true;
	}

	private boolean isHPMPConsume() {
		_mpConsume = _skill.getMpConsume();
		_hpConsume = _skill.getHpConsume();
		int currentMp = 0;
		int currentHp = 0;

		if (_user instanceof L1NpcInstance) {
			currentMp = _npc.getCurrentMp();
			currentHp = _npc.getCurrentHp();
		} else {
			currentMp = _player.getCurrentMp();
			currentHp = _player.getCurrentHp();

			if (_player.getAbility().getTotalInt() > 12
					&& (_skillId > HOLY_WEAPON && _skillId <= FREEZING_BLIZZARD)) { 
				_mpConsume--;
			}
			if (_player.getAbility().getTotalInt() > 13
					&& _skillId > STALAC && _skillId <= FREEZING_BLIZZARD) { 
				_mpConsume--;
			}
			if (_player.getAbility().getTotalInt() > 14
					&& _skillId > WEAK_ELEMENTAL && _skillId <= FREEZING_BLIZZARD) { 
				_mpConsume--;
			}
			if (_player.getAbility().getTotalInt() > 15
					&& _skillId > MEDITATION && _skillId <= FREEZING_BLIZZARD) { 
				_mpConsume--;
			}
			if (_player.getAbility().getTotalInt() > 16
					&& _skillId > DARKNESS && _skillId <= FREEZING_BLIZZARD) { 
				_mpConsume--;
			}
			if (_player.getAbility().getTotalInt() > 17
					&& _skillId > BLESS_WEAPON && _skillId <= FREEZING_BLIZZARD) { 
				_mpConsume--;
			}
			if (_player.getAbility().getTotalInt() > 18
					&& _skillId > DISEASE && _skillId <= FREEZING_BLIZZARD) { 
				_mpConsume--;
			}

			if (_player.getAbility().getTotalInt() > 12
					&& _skillId >= SHOCK_STUN && _skillId <= COUNTER_BARRIER) {
				_mpConsume -= (_player.getAbility().getTotalInt() - 12);
			}

			if ((_skillId == PHYSICAL_ENCHANT_DEX || _skillId == HASTE)
					&& _player.getInventory().checkEquipped(20013)) { 
				_mpConsume /= 2;
			}
			if ((_skillId == HEAL || _skillId == EXTRA_HEAL)
					&& _player.getInventory().checkEquipped(20014)) { 
				_mpConsume /= 2;
			}
			if ((_skillId == ENCHANT_WEAPON || _skillId == DETECTION || _skillId == PHYSICAL_ENCHANT_STR)
					&& _player.getInventory().checkEquipped(20015)) { 
				_mpConsume /= 2;
			}
			if (_skillId == HASTE && _player.getInventory().checkEquipped(20008)) {
				_mpConsume /= 2;
			}
			if (_skillId == GREATER_HASTE && _player.getInventory().checkEquipped(20023)) { 
				_mpConsume /= 2;
			}

			if (_player.getBaseMagicDecreaseMp() > 0){
				_mpConsume -= _player.getBaseMagicDecreaseMp();
			}


			if (0 < _skill.getMpConsume()) { 
				_mpConsume = Math.max(_mpConsume, 1); 
			}
		}

		if (currentHp < _hpConsume + 1) {
			if (_user instanceof L1PcInstance) {
				_player.sendPackets(new S_ServerMessage(279));
			}
			return false;
		} else if (currentMp < _mpConsume) {
			if (_user instanceof L1PcInstance) {
				_player.sendPackets(new S_ServerMessage(278));
			}
			return false;
		}

		return true;
	}

	private boolean isItemConsume() {
		int itemConsume = _skill.getItemConsumeId();
		int itemConsumeCount = _skill.getItemConsumeCount();

		if (itemConsume == 0) {	return true; }
		if (!_player.getInventory().checkItem(itemConsume, itemConsumeCount)) {
			return false; 
		}

		return true;
	}

	private void useConsume() {
		if (_user instanceof L1NpcInstance) {
			int current_hp = _npc.getCurrentHp() - _hpConsume;
			_npc.setCurrentHp(current_hp);

			int current_mp = _npc.getCurrentMp() - _mpConsume;
			_npc.setCurrentMp(current_mp);
			return;
		}

		if (isHPMPConsume()) {
			if (_skillId == FINAL_BURN ) { 
				_player.setCurrentHp(1);
				_player.setCurrentMp(0);
			} else {
				int current_hp = _player.getCurrentHp() - _hpConsume;
				_player.setCurrentHp(current_hp);

				int current_mp = _player.getCurrentMp() - _mpConsume;
				_player.setCurrentMp(current_mp);
			}
		}

		int lawful = _player.getLawful() + _skill.getLawful();
		if (lawful > 32767) {
			lawful = 32767;
		}
		if (lawful < -32767) {
			lawful = -32767;
		}
		_player.setLawful(lawful);

		int itemConsume = _skill.getItemConsumeId();
		int itemConsumeCount = _skill.getItemConsumeCount();

		if (itemConsume == 0) {
			return; 
		}

		_player.getInventory().consumeItem(itemConsume, itemConsumeCount);
	}

	private void addMagicList(L1Character cha, boolean repetition) {
		if (_skillTime == 0) {
			_getBuffDuration = _skill.getBuffDuration() * 1000; 
			if (_skill.getBuffDuration() == 0) {
				if (_skillId == INVISIBILITY) { 
					cha.getSkillEffectTimerSet().setSkillEffect(INVISIBILITY, 0);
				}
				return;
			}
		} else {
			_getBuffDuration = _skillTime * 1000;
		}

		if (_skillId == SHOCK_STUN ) {
			_getBuffDuration = _shockStunDuration;
		}

		if (_skillId == CURSE_POISON
				|| _skillId == CURSE_PARALYZE
				|| _skillId == CURSE_PARALYZE2
				|| _skillId == SHAPE_CHANGE
				|| _skillId == BLESSED_ARMOR
				|| _skillId == HOLY_WEAPON
				|| _skillId == ENCHANT_WEAPON
				|| _skillId == BLESS_WEAPON
				|| _skillId == SHADOW_FANG) {
			return;
		}

		if ((_skillId == ICE_LANCE || _skillId == FREEZING_BLIZZARD || _skillId == FREEZING_BREATH)
				&& !_isFreeze) { 
			return;
		}
		cha.getSkillEffectTimerSet().setSkillEffect(_skillId, _getBuffDuration);

		if (cha instanceof L1PcInstance && repetition) { 
			L1PcInstance pc = (L1PcInstance) cha;
			sendIcon(pc);
		}
	}

	private void sendIcon(L1PcInstance pc) {
		if (_skillTime == 0) {
			_getBuffIconDuration = _skill.getBuffDuration(); 
		} else {
			_getBuffIconDuration = _skillTime;
		}
		switch(_skillId){
		case SHIELD: pc.sendPackets(new S_SkillIconShield(5, _getBuffIconDuration)); break;
		case SHADOW_ARMOR: pc.sendPackets(new S_SkillIconShield(3, _getBuffIconDuration)); break;
		case DRESS_DEXTERITY: pc.sendPackets(new S_Dexup(pc, 2, _getBuffIconDuration)); break;
		case DRESS_MIGHTY: pc.sendPackets(new S_Strup(pc, 2, _getBuffIconDuration)); break;
		case GLOWING_AURA: pc.sendPackets(new S_SkillIconAura(113, _getBuffIconDuration)); break;
		case SHINING_AURA: pc.sendPackets(new S_SkillIconAura(114, _getBuffIconDuration)); break;
		case BRAVE_AURA: pc.sendPackets(new S_SkillIconAura(116, _getBuffIconDuration)); break;
		case FIRE_WEAPON: pc.sendPackets(new S_SkillIconAura(147, _getBuffIconDuration)); break;
		case WIND_SHOT: pc.sendPackets(new S_SkillIconAura(148, _getBuffIconDuration)); break;
		case FIRE_BLESS: pc.sendPackets(new S_SkillIconAura(154, _getBuffIconDuration)); break;
		case STORM_EYE: pc.sendPackets(new S_SkillIconAura(155, _getBuffIconDuration)); break;
		case EARTH_BLESS: pc.sendPackets(new S_SkillIconShield(7, _getBuffIconDuration)); break;
		case BURNING_WEAPON: pc.sendPackets(new S_SkillIconAura(162, _getBuffIconDuration)); break;
		case STORM_SHOT: pc.sendPackets(new S_SkillIconAura(165, _getBuffIconDuration)); break;
		case IRON_SKIN: pc.sendPackets(new S_SkillIconShield(10, _getBuffIconDuration)); break;
		case EARTH_SKIN: pc.sendPackets(new S_SkillIconShield(6, _getBuffIconDuration)); break;
		case PHYSICAL_ENCHANT_STR: pc.sendPackets(new S_Strup(pc, 5, _getBuffIconDuration)); break;
		case PHYSICAL_ENCHANT_DEX: pc.sendPackets(new S_Dexup(pc, 5, _getBuffIconDuration)); break;
		case IMMUNE_TO_HARM: pc.sendPackets(new S_SkillIconGFX(40, _getBuffIconDuration)); break;
		case HASTE: case GREATER_HASTE: pc.sendPackets(new S_SkillHaste(pc.getId(), 1, _getBuffIconDuration)); Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 1, 0)); break;
		case HOLY_WALK: case MOVING_ACCELERATION: case WIND_WALK: pc.sendPackets(new S_SkillBrave(pc.getId(), 4, _getBuffIconDuration)); Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 4, 0)); break;
		case BLOOD_LUST: pc.sendPackets(new S_SkillBrave(pc.getId(), 1, _getBuffIconDuration)); Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 1, 0)); break;
		case SLOW: case MOB_SLOW_1: case MOB_SLOW_18: case MASS_SLOW: case ENTANGLE: 
			pc.sendPackets(new S_SkillHaste(pc.getId(), 2, _getBuffIconDuration)); Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 2, 0)); break;
		default: break;
		}
		pc.sendPackets(new S_OwnCharStatus(pc));
	}

	private void sendGrfx(boolean isSkillAction) {
		int actionId = _skill.getActionId();
		int castgfx = _skill.getCastGfx();
		if (castgfx == 0) {
			return; 
		}

		if (_user instanceof L1PcInstance) {
			if (_skillId == FIRE_WALL 
					|| _skillId == LIFE_STREAM
					|| _skillId == CUBE_IGNITION 
					|| _skillId == CUBE_QUAKE
					|| _skillId == CUBE_SHOCK 
					|| _skillId == CUBE_BALANCE) {
				L1PcInstance pc = (L1PcInstance) _user;
				if (_skillId == FIRE_WALL) {
					pc.getMoveState().setHeading(CharPosUtil.targetDirection(pc, _targetX, _targetY));
					pc.sendPackets(new S_ChangeHeading(pc));
					Broadcaster.broadcastPacket(pc, new S_ChangeHeading(pc));
				}
				S_DoActionGFX gfx = new S_DoActionGFX(pc.getId(), actionId);
				pc.sendPackets(gfx);
				Broadcaster.broadcastPacket(pc, gfx);
				return;
			}

			int targetid = _target.getId();

			if (_skillId == SHOCK_STUN
					|| _skillId == MOB_SHOCKSTUN_30									
					|| _skillId == MOB_RANGESTUN_19 
					|| _skillId == MOB_RANGESTUN_18	){			
				if (_targetList.size() == 0) {
					if (_target instanceof L1PcInstance) { // Gn.89
						L1PcInstance pc = (L1PcInstance) _target;
						pc.sendPackets(new S_SkillSound(pc.getId(), 4434));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 4434));
						pc.sendPackets(new S_ServerMessage(280));
					} else if (_target instanceof L1NpcInstance) {
						Broadcaster.broadcastPacket(_target, new S_SkillSound(_target.getId(), 4434));
					}
					return;
				} else {
					if (_target instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) _target;
						pc.sendPackets(new S_SkillSound(pc.getId(), 4434));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 4434));
					} else if (_target instanceof L1NpcInstance) {
						Broadcaster.broadcastPacket(_target, new S_SkillSound(_target.getId(), 4434));
					}
					return;
				}
			}

			if (_skillId == AM_BREAK) {
				if (_targetList.size() == 0) {
					return;
				} else {
					if (_target instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) _target;
						pc.sendPackets(new S_SkillSound(pc.getId(), 6551));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6551));
					} else if (_target instanceof L1NpcInstance) {
						Broadcaster.broadcastPacket(_target, new S_SkillSound(_target.getId(), 6551));
					}
					return;
				}
			}

			if (_skillId == SMASH) {
				if (_targetList.size() == 0) {
					return;
				} else {
					if (_target instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) _target;
						pc.sendPackets(new S_SkillSound(pc.getId(), 6526));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 6526));
					} else if (_target instanceof L1NpcInstance) {
						Broadcaster.broadcastPacket(_target, new S_SkillSound(_target.getId(), 6526));
					}
					return;
				}
			}

			if (_skillId == LIGHT) {
				L1PcInstance pc = (L1PcInstance) _target;
				pc.sendPackets(new S_Sound(145));
			}

			if (_targetList.size() == 0 && !(_skill.getTarget().equals("none"))) {
				int tempchargfx = _player.getGfxId().getTempCharGfx();
				if (tempchargfx == 5727 || tempchargfx == 5730) { 
					actionId = ActionCodes.ACTION_SkillBuff;
				} else if (tempchargfx == 5733 || tempchargfx == 5736) {
					actionId = ActionCodes.ACTION_Attack;
				}
				if (isSkillAction && actionId > 0) {
					S_DoActionGFX gfx = new S_DoActionGFX(_player.getId(), actionId);
					_player.sendPackets(gfx);
					Broadcaster.broadcastPacket(_player, gfx);
				}
				return;
			}

			if (_skill.getTarget().equals("attack") && _skillId != 18) {
				if (isPcSummonPet(_target)) {
					if (CharPosUtil.getZoneType(_player) == 1
							|| CharPosUtil.getZoneType(_target) == 1 
							|| _player.checkNonPvP(_player, _target)) { 
						_player.sendPackets(new S_UseAttackSkill(_player, 0, castgfx, _targetX, _targetY, actionId)); 
						Broadcaster.broadcastPacket(_player, new S_UseAttackSkill(_player, 0, castgfx, _targetX, _targetY, actionId));
						return;
					}
				}

				if (_skill.getArea() == 0) { 
					_player.sendPackets(new S_UseAttackSkill(_player, targetid, castgfx, _targetX, _targetY, actionId));
					Broadcaster.broadcastPacket(_player, new S_UseAttackSkill(_player, targetid, castgfx, _targetX, _targetY, actionId));
					Broadcaster.broadcastPacketExceptTargetSight(_target, new S_DoActionGFX(targetid, ActionCodes.ACTION_Damage), _player);
				} else { 
					L1Character[] cha = new L1Character[_targetList.size()];
					int i = 0;
					for (TargetStatus ts : _targetList) {
						cha[i] = ts.getTarget();
						i++;
					}
					_player.sendPackets(new S_RangeSkill(_player, cha, castgfx, actionId, S_RangeSkill.TYPE_DIR));
					Broadcaster.broadcastPacket(_player, new S_RangeSkill(_player, cha, castgfx, actionId, S_RangeSkill.TYPE_DIR));
				}
			} else if (_skill.getTarget().equals("none") && _skill.getType() == L1Skills.TYPE_ATTACK) {
				L1Character[] cha = new L1Character[_targetList.size()];
				int i = 0;
				for (TargetStatus ts : _targetList) {
					cha[i] = ts.getTarget();
					Broadcaster.broadcastPacketExceptTargetSight(cha[i], new S_DoActionGFX(cha[i].getId(), ActionCodes.ACTION_Damage),_player);
					i++;
				}
				_player.sendPackets(new S_RangeSkill(_player, cha, castgfx, actionId, S_RangeSkill.TYPE_NODIR));
				Broadcaster.broadcastPacket(_player, new S_RangeSkill(_player, cha, castgfx, actionId, S_RangeSkill.TYPE_NODIR));
			} else { 
				if (_skillId != 5 && _skillId != 69 && _skillId != 131) {
					if (isSkillAction && actionId > 0) {
						S_DoActionGFX gfx = new S_DoActionGFX(_player.getId(), _skill.getActionId());
						_player.sendPackets(gfx);
						Broadcaster.broadcastPacket(_player, gfx);
					}
					if (_skillId == COUNTER_MAGIC || _skillId == COUNTER_MIRROR) {
						_player.sendPackets(new S_SkillSound(targetid, castgfx));
						Broadcaster.broadcastPacket(_player, new S_SkillSound(targetid, castgfx));
						/*
					} else if (_skillId == COUNTER_BARRIER) {
						_player.sendPackets(new S_SkillSound(targetid, castgfx));
						Broadcaster.broadcastPacket(_player, new S_SkillSound(targetid, castgfx));
						 */
					} else if (_skillId == TRUE_TARGET) {
						return;
					} else {
						_player.sendPackets(new S_SkillSound(targetid, castgfx));
						Broadcaster.broadcastPacket(_player, new S_SkillSound(targetid, castgfx));
					}
				}

				for (TargetStatus ts : _targetList) {
					L1Character cha = ts.getTarget();
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_OwnCharStatus(pc));
					}
				}
			}
		} else if (_user instanceof L1NpcInstance) { 
			int targetid = _target.getId();

			if (_user instanceof L1MerchantInstance) {
				Broadcaster.broadcastPacket(_user, new S_SkillSound(targetid, castgfx));
				return;
			}

			if (_targetList.size() == 0 && !(_skill.getTarget().equals("none"))) {
				S_DoActionGFX gfx = new S_DoActionGFX(_user.getId(), _skill.getActionId());
				Broadcaster.broadcastPacket(_user, gfx);
				return;
			}

			if (_skill.getTarget().equals("attack") && _skillId != 18) {
				if (_skill.getArea() == 0) { 
					Broadcaster.broadcastPacket(_user, new S_UseAttackSkill(_user, targetid,
							castgfx, _targetX, _targetY, actionId));
					Broadcaster.broadcastPacketExceptTargetSight(_target, new S_DoActionGFX(
							targetid, ActionCodes.ACTION_Damage), _user);
				} else { 
					L1Character[] cha = new L1Character[_targetList.size()];
					int i = 0;
					for (TargetStatus ts : _targetList) {
						cha[i] = ts.getTarget();
						Broadcaster.broadcastPacketExceptTargetSight(cha[i],
								new S_DoActionGFX(cha[i].getId(), ActionCodes.ACTION_Damage), _user);
						i++;
					}
					Broadcaster.broadcastPacket(_user, new S_RangeSkill(_user, cha,
							castgfx, actionId, S_RangeSkill.TYPE_DIR));
				}
			} else if (_skill.getTarget().equals("none") && _skill.getType() == L1Skills.TYPE_ATTACK) {
				L1Character[] cha = new L1Character[_targetList.size()];
				int i = 0;
				for (TargetStatus ts : _targetList) {
					cha[i] = ts.getTarget();
					i++;
				}
				Broadcaster.broadcastPacket(_user, new S_RangeSkill(_user, cha, castgfx,
						actionId, S_RangeSkill.TYPE_NODIR));
			} else { 
				if (_skillId != 5 && _skillId != 69 && _skillId != 131) {
					S_DoActionGFX gfx = new S_DoActionGFX(_user.getId(), _skill.getActionId());
					Broadcaster.broadcastPacket(_user, gfx);
					Broadcaster.broadcastPacket(_user, new S_SkillSound(targetid, castgfx));
				}
			}
		}
	}

	private void deleteRepeatedSkills(L1Character cha) {
		final int[][] repeatedSkills = {
				//				{ HOLY_WEAPON, ENCHANT_WEAPON, BLESS_WEAPON, SHADOW_FANG },
				{ FIRE_WEAPON, WIND_SHOT, FIRE_BLESS, STORM_EYE,BURNING_WEAPON, STORM_SHOT },
				{ SHIELD, SHADOW_ARMOR, EARTH_SKIN, EARTH_BLESS, IRON_SKIN },
				{ HOLY_WALK, BLOOD_LUST, MOVING_ACCELERATION, WIND_WALK, STATUS_BRAVE,STATUS_ELFBRAVE },
				{ HASTE, GREATER_HASTE, STATUS_HASTE },
				{ PHYSICAL_ENCHANT_DEX, DRESS_DEXTERITY },
				{ PHYSICAL_ENCHANT_STR, DRESS_MIGHTY },
				{ GLOWING_AURA, SHINING_AURA },
				{ AVATA, IMMUNE_TO_HARM },
				{ FAFU_MAAN, ANTA_MAAN, LIND_MAAN, VALA_MAAN, LIFE_MAAN, BIRTH_MAAN, SHAPE_MAAN },
				{FEATHER_BUFF_A, FEATHER_BUFF_B, FEATHER_BUFF_C, FEATHER_BUFF_D}
		};
		for (int[] skills : repeatedSkills) {
			for (int id : skills) {
				if (id == _skillId) {
					stopSkillList(cha, skills);
				}
			}
		}
	}

	private void stopSkillList(L1Character cha, int[] repeat_skill) {
		for (int skillId : repeat_skill) {
			if (skillId != _skillId) {
				cha.getSkillEffectTimerSet().removeSkillEffect(skillId);
			}
		}
	}

	private void setDelay() {
		if (_skill.getReuseDelay() > 0) {
			L1SkillDelay.onSkillUse(_user, _skill.getReuseDelay());
		}
	}

	private void runSkill() {
		if (_skillId == CUBE_IGNITION) {
			L1EffectInstance effect = L1EffectSpawn.getInstance().spawnEffect(4500501,
					_skill.getBuffDuration() * 1000, _user.getX(), _user.getY(), _user.getMapId());
			_player.getSkillEffectTimerSet().setSkillEffect(CUBE_IGNITION, _skill.getBuffDuration() * 1000);
			effect.setCubeTime(4);
			effect.setCubePc(_player);
			L1Cube.getInstance().add(0, effect);
			return;
		}

		if (_skillId == CUBE_QUAKE) {
			L1EffectInstance effect = L1EffectSpawn.getInstance().spawnEffect(4500502,
					_skill.getBuffDuration() * 1000, _user.getX(), _user.getY(), _user.getMapId());
			_player.getSkillEffectTimerSet().setSkillEffect(CUBE_QUAKE, _skill.getBuffDuration() * 1000);
			effect.setCubeTime(4);
			effect.setCubePc(_player);
			L1Cube.getInstance().add(1, effect);
			return;
		}

		if (_skillId == CUBE_SHOCK) {
			L1EffectInstance effect = L1EffectSpawn.getInstance().spawnEffect(4500503,
					_skill.getBuffDuration() * 1000, _user.getX(), _user.getY(), _user.getMapId());
			_player.getSkillEffectTimerSet().setSkillEffect(CUBE_SHOCK, _skill.getBuffDuration() * 1000);
			effect.setCubeTime(4);
			effect.setCubePc(_player);
			L1Cube.getInstance().add(2, effect);
			return;
		}

		if (_skillId == CUBE_BALANCE) {
			L1EffectInstance effect = L1EffectSpawn.getInstance().spawnEffect(4500504,
					_skill.getBuffDuration() * 1000, _user.getX(), _user.getY(), _user.getMapId());
			_player.getSkillEffectTimerSet().setSkillEffect(CUBE_BALANCE, _skill.getBuffDuration() * 1000);
			effect.setCubeTime(5);
			effect.setCubePc(_player);
			L1Cube.getInstance().add(3, effect);
			return;
		}


		if (_skillId == LIFE_STREAM) {
			L1EffectSpawn.getInstance().spawnEffect(81169,
					_skill.getBuffDuration() * 1000, _targetX, _targetY, _user.getMapId());
			return;
		}

		if (_skillId == FIRE_WALL) {
			L1EffectSpawn.getInstance().doSpawnFireWall(_user, _targetX, _targetY);
			return;
		}
		/**** A111  주석 ****/
		/*
		for (int skillId : EXCEPT_COUNTER_MAGIC) {
			if (_skillId == skillId) {				
				_isCounterMagic = false;
				break;
			}
		}*/		
		if (_skillId == SHOCK_STUN/* || _skillId == BONE_BREAK || _skillId == AM_BREAK || 
				_skillId == SMASH */&& _user instanceof L1PcInstance) {
			_target.onAction(_player);
		}		
		if (!isTargetCalc(_target)) {
			return;
		}

		try {
			TargetStatus ts = null;
			L1Character cha = null;
			int dmg = 0;
			int drainMana = 0;
			int heal = 0;
			boolean isSuccess = false;
			int undeadType = 0;

			for (Iterator<TargetStatus> iter = _targetList.iterator(); iter.hasNext();) {
				ts = null;
				cha = null;
				dmg = 0;
				heal = 0;
				isSuccess = false;
				undeadType = 0;

				ts = iter.next();
				cha = ts.getTarget();				
				if (!ts.isCalc() || !isTargetCalc(cha)) {
					continue; 
				}

				L1Magic _magic = new L1Magic(_user, cha);				
				_magic.setLeverage(getLeverage());

				if (cha instanceof L1MonsterInstance) { 
					undeadType = ((L1MonsterInstance) cha).getNpcTemplate().get_undead();
				}

				if ((_skill.getType() == L1Skills.TYPE_CURSE || _skill.getType() == L1Skills.TYPE_PROBABILITY)
						&& isTargetFailure(cha)) {
					iter.remove();
					continue;
				}			

				if (cha instanceof L1PcInstance) { 
					if (_skillTime == 0) {
						_getBuffIconDuration = _skill.getBuffDuration(); 
					} else {
						_getBuffIconDuration = _skillTime; 
					}
				}

				deleteRepeatedSkills(cha); 

				if (_skill.getType() == L1Skills.TYPE_ATTACK && _user.getId() != cha.getId()) {
					if (isUseCounterMagic(cha)) {
						iter.remove();
						continue;
					}
					dmg = _magic.calcMagicDamage(_skillId);
					//공격 스킬일때!! 이레이즈 여부 판멸후 제거
					if(cha.getSkillEffectTimerSet().hasSkillEffect(ERASE_MAGIC)){
						cha.getSkillEffectTimerSet().removeSkillEffect(ERASE_MAGIC);
					}
				} else if (_skill.getType() == L1Skills.TYPE_CURSE
						|| _skill.getType() == L1Skills.TYPE_PROBABILITY) { 
					isSuccess = _magic.calcProbabilityMagic(_skillId);
					//이레 마법이 아니고 현제 이레중이라면!!!
					if (_skillId != ERASE_MAGIC && _skillId != EARTH_BIND) {
						if(cha.getSkillEffectTimerSet().hasSkillEffect(ERASE_MAGIC)){
							cha.getSkillEffectTimerSet().removeSkillEffect(ERASE_MAGIC);
						}
					}
					if (_skillId != FOG_OF_SLEEPING) {
						cha.getSkillEffectTimerSet().removeSkillEffect(FOG_OF_SLEEPING);
					}
					if (_skillId != PHANTASM) {
						cha.getSkillEffectTimerSet().removeSkillEffect(PHANTASM);
					}
					if (isSuccess) {
						if (isUseCounterMagic(cha)) {
							iter.remove();
							continue;
						}
					} else {
						if (_skillId == FOG_OF_SLEEPING
								&& cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_ServerMessage(297)); 
						}
						iter.remove();
						continue;
					}
				} else if (_skill.getType() == L1Skills.TYPE_HEAL) {
					dmg = -1 * _magic.calcHealing(_skillId);
					if (cha.getSkillEffectTimerSet().hasSkillEffect(WATER_LIFE)) { 
						dmg *= 2;
					}
					if (cha.getSkillEffectTimerSet().hasSkillEffect(POLLUTE_WATER)) { 
						dmg /= 2;
					}
				}

				if (cha.getSkillEffectTimerSet().hasSkillEffect(_skillId) && _skillId != SHOCK_STUN && _skillId != THUNDER_GRAB) {
					addMagicList(cha, true); 
					if (_skillId != SHAPE_CHANGE) {
						continue;
					}
				}

				// ●●●● PC, NPC 양쪽 모두 효과가 있는 스킬 ●●●●
				// GFX Check (Made by HuntBoy)
				switch(_skillId){
				case HASTE:{
					if (cha.getMoveState().getMoveSpeed() != 2) { 
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							if (pc.getHasteItemEquipped() > 0) {
								continue;
							}
							pc.setDrink(false);
							pc.sendPackets(new S_SkillHaste(pc.getId(), 1, _getBuffIconDuration));
						}
						Broadcaster.broadcastPacket(cha, new S_SkillHaste(cha.getId(), 1, 0));
						cha.getMoveState().setMoveSpeed(1);
					} else { 
						int skillNum = 0;
						if (cha.getSkillEffectTimerSet().hasSkillEffect(SLOW)) {
							skillNum = SLOW;
						} else if (cha.getSkillEffectTimerSet().hasSkillEffect(MASS_SLOW)) {
							skillNum = MASS_SLOW;
						} else if (cha.getSkillEffectTimerSet().hasSkillEffect(ENTANGLE)) {
							skillNum = ENTANGLE;
						} else if (cha.getSkillEffectTimerSet().hasSkillEffect(MOB_SLOW_1)) {  
							skillNum = MOB_SLOW_1;
						} else if (cha.getSkillEffectTimerSet().hasSkillEffect(MOB_SLOW_18)) {  
							skillNum = MOB_SLOW_18;
						}
						if (skillNum != 0) {
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
							cha.getSkillEffectTimerSet().removeSkillEffect(HASTE);
							cha.getMoveState().setMoveSpeed(0);
							continue;
						}
					}
				}
				break;
				case CURE_POISON:{
					cha.curePoison();
				}
				break;
				case REMOVE_CURSE:{
					cha.curePoison();
					if (cha.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_PARALYZING)
							|| cha.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_PARALYZED)) {
						cha.cureParalaysis();
					}
					if (cha.getSkillEffectTimerSet().hasSkillEffect(CURSE_BLIND)
							|| cha.getSkillEffectTimerSet().hasSkillEffect(DARKNESS)) {
						if(cha.getSkillEffectTimerSet().hasSkillEffect(CURSE_BLIND)){
							cha.getSkillEffectTimerSet().removeSkillEffect(CURSE_BLIND);
						} else if (cha.getSkillEffectTimerSet().hasSkillEffect(DARKNESS)){
							cha.getSkillEffectTimerSet().removeSkillEffect(DARKNESS);
						}
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_CurseBlind(0));
						}
					}
				}
				break;
				case RESURRECTION:
				case GREATER_RESURRECTION:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (_player.getId() != pc.getId()) {
							if (L1World.getInstance().getVisiblePlayer(pc, 0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(pc, 0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (pc.getCurrentHp() == 0 && pc.isDead()) {
								if (pc.getMap().isUseResurrection()) {
									if (_skillId == RESURRECTION) {
										pc.setGres(false);
									} else if (_skillId == GREATER_RESURRECTION) {
										pc.setGres(true);
									}
									pc.setTempID(_player.getId());
									pc.sendPackets(new S_Message_YN(322, "")); 
								}
							}
						}
					}
					if (cha instanceof L1NpcInstance) {
						if (!(cha instanceof L1TowerInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							if (npc instanceof L1PetInstance && L1World.getInstance().getVisiblePlayer(npc, 0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(npc, 0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (npc.getCurrentHp() == 0 && npc.isDead()) {
								npc.resurrect(npc.getMaxHp() / 4);
								npc.setResurrect(true);
							}
						}
					}
				}
				break;
				case CALL_OF_NATURE:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (_player.getId() != pc.getId()) {
							if (L1World.getInstance().getVisiblePlayer(pc, 0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(pc, 0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (pc.getCurrentHp() == 0 && pc.isDead()) {
								pc.setTempID(_player.getId());
								pc.sendPackets(new S_Message_YN(322, "")); 
							}
						}
					}
					if (cha instanceof L1NpcInstance) {
						if (!(cha instanceof L1TowerInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							if (npc instanceof L1PetInstance && L1World.getInstance().getVisiblePlayer(npc, 0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(npc, 0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (npc.getCurrentHp() == 0 && npc.isDead()) {
								npc.resurrect(cha.getMaxHp());
								npc.resurrect(cha.getMaxMp() / 100);
								npc.setResurrect(true);
							}
						}
					}
				}
				break;
				case DETECTION:{
					if (cha instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						int hiddenStatus = npc.getHiddenStatus();
						if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_SINK) {
							if(npc.getNpcId() != 45682)
								npc.appearOnGround(_player);
						}
					}
				}
				break;
				case COUNTER_DETECTION:{
					if (cha instanceof L1PcInstance) {
						dmg = _magic.calcMagicDamage(_skillId);
					} else if (cha instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						int hiddenStatus = npc.getHiddenStatus();
						if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_SINK) {
							if(npc.getNpcId() != 45682)
								npc.appearOnGround(_player);
						} else {
							dmg = 0;
						}
					} else {
						dmg = 0;
					}
				}
				break;
				case JOY_OF_PAIN:{
					int selldmg = _player.getMaxHp() - _player.getCurrentHp();
					dmg = (selldmg/3)+70;
				}
				break;
				case MIND_BREAK:{
					if (_target.getCurrentMp() >= 5) {
						_target.setCurrentMp(_target.getCurrentMp() - 5);
						dmg = 25;
					}else{
						return;
					}
				}
				break;
				case TRUE_TARGET:{
					if (_user instanceof L1PcInstance) {
						L1PcInstance pri = (L1PcInstance) _user;
						pri.sendPackets(new S_TrueTarget(_targetID,
								pri.getId(), _message));
						for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(_target)) {
							if(pri.getClanid() == pc.getClanid()){
								pc.sendPackets(new S_TrueTarget(_targetID, pc.getId(), _message));
							}
						}
					}
				}
				break;
				case ELEMENTAL_FALL_DOWN:{
					if (_user instanceof L1PcInstance) {
						int playerAttr = _player.getElfAttr();
						int i = -50;
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							switch (playerAttr) {
							case 0: _player.sendPackets(new S_ServerMessage(79)); break;
							case 1: pc.getResistance().addEarth(i); pc.setAddAttrKind(1); break;
							case 2: pc.getResistance().addFire(i); pc.setAddAttrKind(2); break;
							case 4: pc.getResistance().addWater(i); pc.setAddAttrKind(4); break;
							case 8: pc.getResistance().addWind(i); pc.setAddAttrKind(8); break;
							default: break;
							}
						} else if (cha instanceof L1MonsterInstance) {
							L1MonsterInstance mob = (L1MonsterInstance) cha;
							switch (playerAttr) {
							case 0: _player.sendPackets(new S_ServerMessage(79)); break;
							case 1: mob.getResistance().addEarth(i); mob.setAddAttrKind(1); break;
							case 2: mob.getResistance().addFire(i); mob.setAddAttrKind(2); break;
							case 4: mob.getResistance().addWater(i); mob.setAddAttrKind(4); break;
							case 8: mob.getResistance().addWind(i); mob.setAddAttrKind(8); break;
							default: break;
							}
						}
					}
				}
				break;
				case HEAL:
				case EXTRA_HEAL:
				case GREATER_HEAL:
				case FULL_HEAL:
				case HEAL_ALL:
				case NATURES_TOUCH:
				case NATURES_BLESSING:{
					if(cha.getSkillEffectTimerSet().hasSkillEffect(WATER_LIFE)){
						cha.getSkillEffectTimerSet().killSkillEffectTimer(WATER_LIFE);
						if(cha instanceof L1PcInstance){
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_PacketBox(S_PacketBox.DEL_ICON));
						}
					}
				}
				break;
				case CHILL_TOUCH:
				case VAMPIRIC_TOUCH:{
					heal = dmg;
				}
				break;

				case TRIPLE_ARROW:{
					int weaponType =  _player.getWeapon().getItem().getType1(); 
					if (weaponType != 20) return;		
					for (int i = 3; i > 0; i--) {
						_target.onAction(_player);
					}
					if (_calcType == PC_PC) { // 공격타입이 PC vs PC일때
						dmg /= 0.7; // 데미지는 데미지를 100으로 나눈값에 90을 곱한다. 즉 90%의 데미지를 입힌다.
					} // 공격스킬 데미지조절 by 인연
					_player.sendPackets(new S_SkillSound(_player.getId(), 4394));
					Broadcaster.broadcastPacket(_player, new S_SkillSound(_player.getId(), 4394));
				}
				break;
				case FOU_SLAYER: {
					int weaponType =  _player.getWeapon().getItem().getType1(); 

					if (weaponType != 4 && weaponType != 11 && weaponType != 24 && weaponType != 50 && weaponType != 46)	return;

					for (int i = 3; i > 0; i--) {
						_target.onAction(_player);
					}	

					_player.sendPackets(new S_SkillSound(_player.getId(), 7020));
					_player.sendPackets(new S_SkillSound(_targetID, 6509));
					Broadcaster.broadcastPacket(_player, new S_SkillSound(_player.getId(), 7020));
					Broadcaster.broadcastPacket(_player, new S_SkillSound(_targetID, 6509));

					if (_player.getSkillEffectTimerSet().hasSkillEffect(STATUS_SPOT1)) {
						dmg += 20;
						_player.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_SPOT1);
						_player.sendPackets(new S_PacketBox(S_PacketBox.SPOT, 0)); //추가
					}
					if (_player.getSkillEffectTimerSet().hasSkillEffect(STATUS_SPOT2)) {
						dmg += 40;
						_player.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_SPOT2);
						_player.sendPackets(new S_PacketBox(S_PacketBox.SPOT, 0)); //추가 
					}
					if (_player.getSkillEffectTimerSet().hasSkillEffect(STATUS_SPOT3)) {
						dmg += 60;
						_player.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_SPOT3);
						_player.sendPackets(new S_PacketBox(S_PacketBox.SPOT, 0)); //추가 
					}
				}
				break;
				case 10026:
				case 10027:
				case 10028:
				case 10029:{
					if (_user instanceof L1NpcInstance) {
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "$3717", 0)); 
					} else {
						Broadcaster.broadcastPacket(_player, new S_ChatPacket(_player, "$3717", 0, 0)); 
					}
				}
				break;
				case 10057:{
					L1Teleport.teleportToTargetFront(cha, _user, 1);
				}
				break;
				case SLOW:
				case MASS_SLOW:
				case ENTANGLE:
				case MOB_SLOW_1:
				case MOB_SLOW_18:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getHasteItemEquipped() > 0) {
							continue;
						}
					}
					if (cha.getMoveState().getMoveSpeed() == 0) {
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_SkillHaste(pc.getId(), 2, _getBuffIconDuration));
						}
						Broadcaster.broadcastPacket(cha, new S_SkillHaste(cha.getId(), 2, _getBuffIconDuration));
						cha.getMoveState().setMoveSpeed(2);
					} else if (cha.getMoveState().getMoveSpeed() == 1) {
						int skillNum = 0;
						if (cha.getSkillEffectTimerSet().hasSkillEffect(HASTE)) {
							skillNum = HASTE;
						} else if (cha.getSkillEffectTimerSet().hasSkillEffect(GREATER_HASTE)) {
							skillNum = GREATER_HASTE;
						} else if (cha.getSkillEffectTimerSet().hasSkillEffect(STATUS_HASTE)) {
							skillNum = STATUS_HASTE;
						}
						if (skillNum != 0) {
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
							cha.getSkillEffectTimerSet().removeSkillEffect(_skillId);
							cha.getMoveState().setMoveSpeed(0);
							continue;
						}
					}
				}
				break;
				case CURSE_BLIND:
				case DARKNESS:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_FLOATING_EYE)) {
							pc.sendPackets(new S_CurseBlind(2));
						} else {
							pc.sendPackets(new S_CurseBlind(1));
						}
					}
				}
				break;
				case CURSE_POISON:{
					L1DamagePoison.doInfection(_user, cha, 3000, 5);
				}
				break;
				case CURSE_PARALYZE:
				case CURSE_PARALYZE2:
				case MOB_CURSEPARALYZ_18:
				case MOB_CURSEPARALYZ_19:{
					if (!cha.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND) 
							&& !cha.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
							&& !cha.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)
							&& !cha.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH)) {
						if (cha instanceof L1PcInstance) {
							L1CurseParalysis.curse(cha, 8000, 16000);
						} else if (cha instanceof L1MonsterInstance) {
							L1CurseParalysis.curse(cha, 0, 16000);
						}
					}
				}
				break;
				case WEAKNESS:
				case MOB_WEAKNESS_1:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(-5);
						pc.addHitup(-1);
					}
				}
				break;
				case DISEASE:
				case MOB_DISEASE_1:
				case MOB_DISEASE_30:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(-6);
						pc.getAC().addAc(12);
					}
				}
				break;
				case GUARD_BREAK:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(10);
					}
				}
				break;
				case HORROR_OF_DEATH:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedStr((byte) -3);
						pc.getAbility().addAddedInt((byte) -3);
					}
				}
				break;
				case PANIC:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedStr((byte) -1);
						pc.getAbility().addAddedDex((byte) -1);
						pc.getAbility().addAddedCon((byte) -1);
						pc.getAbility().addAddedInt((byte) -1);
						pc.getAbility().addAddedWis((byte) -1);
						pc.resetBaseMr();
					}
				}
				break;
				case ICE_LANCE:
				case FREEZING_BLIZZARD:
				case FREEZING_BREATH:{
					_isFreeze = _magic.calcProbabilityMagic(_skillId);
					if (_isFreeze) {
						int time = _skill.getBuffDuration() * 1000;
						L1EffectSpawn.getInstance()
						.spawnEffect(81168, time,
								cha.getX(), cha.getY(), cha.getMapId());
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_Poison(pc.getId(), 2));
							Broadcaster.broadcastPacket(pc, new S_Poison(pc.getId(), 2));
							pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_FREEZE, true));
						} else if (cha instanceof L1MonsterInstance
								|| cha instanceof L1SummonInstance
								|| cha instanceof L1PetInstance) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							Broadcaster.broadcastPacket(npc, new S_Poison(npc.getId(), 2));
							npc.setParalyzed(true);
							npc.setParalysisTime(time);
						}
					}
				}
				break;
				case EARTH_BIND:
				case MOB_BASILL:
				case MOB_COCA:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Poison(pc.getId(), 2));
						Broadcaster.broadcastPacket(pc, new S_Poison(pc.getId(), 2));
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_FREEZE, true));
					} else if (cha instanceof L1MonsterInstance
							|| cha instanceof L1SummonInstance
							|| cha instanceof L1PetInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						Broadcaster.broadcastPacket(npc, new S_Poison(npc.getId(), 2));
						npc.setParalyzed(true);
						npc.setParalysisTime(_skill.getBuffDuration() * 1000);
					}
				}
				break;
				case SHOCK_STUN:{
					int targetLevel = 0;
					int diffLevel = 0;
					// _user 가 pc 라면.
					if(_user instanceof L1PcInstance){
						L1PcInstance c = (L1PcInstance) _user;
						// 양손 여부 체크
						if(c == null) return;		// 시전자가 없다면 리턴
						if(c.getWeapon() == null) return;	// 검이 없다면 리턴
						if(!c.getWeapon().getItem().isTwohandedWeapon()) return; // 현재 검이 양손검이 아니라면 리턴.
					}

					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						targetLevel = pc.getLevel();
					} else if (cha instanceof L1MonsterInstance
							|| cha instanceof L1SummonInstance
							|| cha instanceof L1PetInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						targetLevel = npc.getLevel();
					}

					diffLevel = _user.getLevel() - targetLevel;

					if(diffLevel < -2){
						int[] stunTimeArray = { 1000,1050,1100,1150,1200,1250,1300,1350,1400,1450,1500,1550,1600,1650,1700,1750,1800,1850,1900,1950,2000,2500,3000 };
						_shockStunDuration = stunTimeArray[random.nextInt(stunTimeArray.length)];
					} else if(diffLevel >= -2 && diffLevel <= 3) {
						int[] stunTimeArray = { 1000,1050,1100,1150,1200,1250,1300,1350,1400,1450,1500,1550,1600,1650,1700,1750,1800,1850,1900,1950,2000,
								2200, 2500, 2700, 3000, 4000 };
						_shockStunDuration = stunTimeArray[random.nextInt(stunTimeArray.length)];
					} else if(diffLevel >= 4 && diffLevel <= 10) {
						int[] stunTimeArray = { 1000,1050,1100,1150,1200,1250,1300,1350,1400,1450,1500,1550,1600,1650,1700,1750,1800,1850,1900,1950,2000, 2500, 3000, 4000};
						_shockStunDuration = stunTimeArray[random.nextInt(stunTimeArray.length)];
					} else if(diffLevel > 10) {
						int[] stunTimeArray = { 1000,1050,1100,1150,1200,1250,1300,1350,1400,1450,1500,1550,1600,1650,1700,1750,1800,1850,1900,1950,2000};
						_shockStunDuration = stunTimeArray[random.nextInt(stunTimeArray.length)];
					}

					L1EffectSpawn.getInstance().spawnEffect(81162, _shockStunDuration,
							cha.getX(), cha.getY(), cha.getMapId());
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_STUN, true));
					} else if (cha instanceof L1MonsterInstance
							|| cha instanceof L1SummonInstance
							|| cha instanceof L1PetInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						npc.setParalyzed(true);
						npc.setParalysisTime(_shockStunDuration);
					}
				}
				break;
				case MOB_RANGESTUN_18:
				case MOB_RANGESTUN_19:
				case MOB_SHOCKSTUN_30:{					
					int[] stunTimeArray = { 500, 700, 1000, 1200, 1500, 1700, 2000, 2500, 3000, 3500, 4000 };

					int rnd = random.nextInt(stunTimeArray.length);
					_shockStunDuration = stunTimeArray[rnd];

					L1EffectSpawn.getInstance().spawnEffect(81162, _shockStunDuration,
							cha.getX(), cha.getY(), cha.getMapId());
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_STUN, true));
					} else if (cha instanceof L1MonsterInstance
							|| cha instanceof L1SummonInstance
							|| cha instanceof L1PetInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						npc.setParalyzed(true);
						npc.setParalysisTime(_shockStunDuration);
					}
				}
				break;
				case THUNDER_GRAB: {//용기사 리뉴얼
					_isFreeze = _magic.calcProbabilityMagic(_skillId);
					if (_isFreeze) {
						int[] grabTime = {1000, 2000, 3000, 4000 };
						int rnd = random.nextInt(grabTime.length);
						int time = grabTime[rnd];  // 시간 랜덤을 위해

						L1EffectSpawn.getInstance().spawnEffect(81182, time,
								cha.getX(), cha.getY(), cha.getMapId());
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.getSkillEffectTimerSet().setSkillEffect(
									L1SkillId.STATUS_FREEZE, time);
							pc.sendPackets(new S_SkillSound(pc.getId(), 4184));
							Broadcaster.broadcastPacket(pc, new S_SkillSound(pc
									.getId(), 4184));
							pc.sendPackets(new S_Paralysis(
									S_Paralysis.TYPE_BIND, true));
						} else if (cha instanceof L1MonsterInstance
								|| cha instanceof L1SummonInstance
								|| cha instanceof L1PetInstance) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							npc.getSkillEffectTimerSet().setSkillEffect(
									L1SkillId.STATUS_FREEZE, time);
							Broadcaster.broadcastPacket(npc, new S_SkillSound(
									npc.getId(), 4184));
							npc.setParalyzed(true);
						}
					}
				}
				break;
				case BONE_BREAK:{
					int bonetime = 1000;
					L1EffectSpawn.getInstance().spawnEffect(4500500, bonetime, cha.getX(), cha.getY(), cha.getMapId());
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_STUN, true));
					} else if (cha instanceof L1MonsterInstance 
							|| cha instanceof L1SummonInstance
							|| cha instanceof L1PetInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						npc.setParalyzed(true);
						npc.setParalysisTime(bonetime);
					}
				}
				break;
				case AM_BREAK:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(-2);
					}
				}
				break;
				case PHANTASM:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_SLEEP, true));
					}
					cha.setSleeped(true);
				}
				break;
				case WIND_SHACKLE:
				case MOB_WINDSHACKLE_1:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_SkillIconWindShackle(pc.getId(), _getBuffIconDuration));
					}
				}
				break;
				case CANCELLATION:{
					if (cha instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						int npcId = npc.getNpcTemplate().get_npcId();
						if (npcId == 71092) {
							if (npc.getGfxId().getGfxId() == npc.getGfxId().getTempCharGfx()) {
								npc.getGfxId().setTempCharGfx(1314);
								Broadcaster.broadcastPacket(npc, new S_ChangeShape(npc.getId(), 1314));
								return;
							} else {
								return;
							}
						} else if (npcId == 45640) {
							if (npc.getGfxId().getGfxId() == npc.getGfxId().getTempCharGfx()) {
								npc.setCurrentHp(npc.getMaxHp());
								npc.getGfxId().setTempCharGfx(2332);
								Broadcaster.broadcastPacket(npc, new S_ChangeShape(npc.getId(), 2332));
								npc.setName("$2103");
								npc.setNameId("$2103");
								Broadcaster.broadcastPacket(npc, new S_ChangeName(npc.getId(), "$2103"));
							} else if (npc.getGfxId().getTempCharGfx() == 2332) {
								npc.setCurrentHp(npc.getMaxHp());
								npc.getGfxId().setTempCharGfx(2755);
								Broadcaster.broadcastPacket(npc, new S_ChangeShape(npc.getId(), 2755));
								npc.setName("$2488");
								npc.setNameId("$2488");
								Broadcaster.broadcastPacket(npc, new S_ChangeName(npc.getId(), "$2488")); 
							}
						} else if (npcId == 81209) {
							if (npc.getGfxId().getGfxId() == npc.getGfxId().getTempCharGfx()) {
								npc.getGfxId().setTempCharGfx(4310);
								Broadcaster.broadcastPacket(npc, new S_ChangeShape(npc.getId(), 4310));
								return;
							} else {
								return;
							}
						}
					}

					if (_player != null && _player.isInvisble()) {
						_player.delInvis();
					}

					if (!(cha instanceof L1PcInstance)) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						npc.getMoveState().setMoveSpeed(0);
						npc.getMoveState().setBraveSpeed(0);
						Broadcaster.broadcastPacket(npc, new S_SkillHaste(cha.getId(), 0, 0));
						Broadcaster.broadcastPacket(npc, new S_SkillBrave(cha.getId(), 0, 0));
						npc.setWeaponBreaked(false);
						npc.setParalyzed(false);
						npc.setParalysisTime(0);
					}

					for (int skillNum = SKILLS_BEGIN; skillNum <= SKILLS_END; skillNum++) {
						if (isNotCancelable(skillNum) && !cha.isDead()) {
							continue;
						}
						cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
					}

					for (int skillNum = STATUS_BEGIN; skillNum <= STATUS_CANCLEEND; skillNum++) {
						cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
					}

					cha.curePoison();
					cha.cureParalaysis();

					if (cha instanceof L1PcInstance) {}

					for (int skillNum = COOKING_BEGIN; skillNum <= COOKING_END; skillNum++) {
						if (isNotCancelable(skillNum) && !cha.isDead()) {
							continue;
						}
						cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
					}

					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getSkillEffectTimerSet().getSkillEffectTimeSec(L1SkillId.SHAPE_CHANGE) > 0){
							L1PolyMorph.undoPoly(pc);
						}
						pc.sendPackets(new S_CharVisualUpdate(pc));
						Broadcaster.broadcastPacket(pc,new S_CharVisualUpdate(pc));

						if (pc.getHasteItemEquipped() > 0) {
							pc.getMoveState().setMoveSpeed(0);
							pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
							Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
						}
						if (pc != null && pc.isInvisble()) {
							if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) { 
								pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
								pc.sendPackets(new S_Invis(pc.getId(), 0));
								Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
								pc.sendPackets(new S_Sound(147));
							}
							if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLIND_HIDING)) { 
								pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
								pc.sendPackets(new S_Invis(pc.getId(), 0));
								Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
							}
						}
					}
					cha.getSkillEffectTimerSet().removeSkillEffect(STATUS_FREEZE); 
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_CharVisualUpdate(pc));
						Broadcaster.broadcastPacket(pc, new S_CharVisualUpdate(pc));
						if (pc.isPrivateShop()) {
							pc.sendPackets(new S_DoActionShop(pc.getId(), ActionCodes.ACTION_Shop, pc.get_ment1(), pc.get_ment2()));
							Broadcaster.broadcastPacket(pc, new S_DoActionShop(pc.getId(), ActionCodes.ACTION_Shop, pc.get_ment1(), pc.get_ment2()));
						}
						if (_user instanceof L1PcInstance) {
							L1PinkName.onAction(pc, _user);
						}
					}
				}
				break;
				case TURN_UNDEAD:{
					if(undeadType == 1 || undeadType == 3){
						dmg = cha.getCurrentHp();
					}
				}
				break;
				case MANA_DRAIN:{
					int chance = random.nextInt(10) + 5;
					drainMana = chance + (_user.getAbility().getTotalInt() / 2);
					if (cha.getCurrentMp() < drainMana) {
						drainMana = cha.getCurrentMp();
					}
					if (_user instanceof L1PcInstance){
						_player.sendPackets(new S_SkillSound(_player.getId(), 2171));
						Broadcaster.broadcastPacket(_player, new S_SkillSound(_player.getId(), 2171));
					}else{
						Broadcaster.broadcastPacket(_user, new S_SkillSound(_user.getId(), 2171));
					}
				}				
				break;
				case WEAPON_BREAK:{
					if (_calcType == PC_PC || _calcType == NPC_PC) {
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							L1ItemInstance weapon = pc.getWeapon();
							if (weapon != null) {
								int weaponDamage = random.nextInt(_user.getAbility().getTotalInt() / 3) + 1;
								pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
								pc.getInventory().receiveDamage(weapon, weaponDamage);
							}
						}
					} else {
						((L1NpcInstance) cha).setWeaponBreaked(true);
					}
				}
				break;
				case FOG_OF_SLEEPING:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_SLEEP, true));
					}
					cha.setSleeped(true);
				}
				break;
				case STATUS_FREEZE:{
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
					}
				}
				break;
				default:
					break;
				}

				if (_calcType == PC_PC || _calcType == NPC_PC) { //여기부터
					switch(_skillId){					
					case TELEPORT:
					case MASS_TELEPORT:{
						L1PcInstance pc = (L1PcInstance) cha;
						Random random = new Random();
						L1BookMark bookm = pc.getBookMark(_bookmarkId);
						if (bookm != null) { 
							if (pc.getMap().isEscapable() || pc.isGm()) {
								int newX = bookm.getLocX();
								int newY = bookm.getLocY();
								short mapId = bookm.getMapId();
								L1Map map = L1WorldMap.getInstance().getMap(mapId);

								if (_skillId == MASS_TELEPORT) { 
									for (L1PcInstance member : L1World.getInstance().getVisiblePlayer(pc, 3)) {
										if (pc.getClanid() != 0 && member.getClanid() == pc.getClanid()
												&& member.getId() != pc.getId() && !member.isPrivateShop()) {
											int newX2 = newX + random.nextInt(3)+1;
											int newY2 = newY + random.nextInt(3)+1;
											if (map.isInMap(newX2, newY2) && map.isPassable(newX2, newY2)) {
												L1Teleport.teleport(member, newX2, newY2, mapId, member.getMoveState().getHeading(), true, 0, false);
											}else{
												L1Teleport.teleport(member, newX, newY, mapId, member.getMoveState().getHeading(), true, 0, false);
											}
										}
									}
								}
								if(pc.getInventory().checkItem(20288)){
									L1Teleport.teleport(pc, newX, newY, mapId, pc.getMoveState().getHeading(), true);	
								}else{
									int newX2 = newX + random.nextInt(15);
									int newY2 = newY + random.nextInt(15);
									if (map.isInMap(newX2, newY2) && map.isPassable(newX2, newY2)) {
										L1Teleport.teleport(pc, newX2, newY2, mapId, pc.getMoveState().getHeading(), true);
									}else{
										L1Teleport.teleport(pc, newX, newY, mapId, pc.getMoveState().getHeading(), true);
									}
								}
							} else { 
								pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));
								pc.sendPackets(new S_ServerMessage(79));
							}
						} else {
							if (pc.getMap().isTeleportable() || pc.isGm()) {
								L1Location newLocation = pc.getLocation().randomLocation(200, true);
								int newX = newLocation.getX();
								int newY = newLocation.getY();
								short mapId = (short) newLocation.getMapId();
								L1Map map = L1WorldMap.getInstance().getMap(mapId);

								if (_skillId == MASS_TELEPORT) { 
									for (L1PcInstance member : L1World.getInstance().getVisiblePlayer(pc, 3)) {
										if (pc.getClanid() != 0 && member.getClanid() == pc.getClanid()											
												&& member.getId() != pc.getId() && !member.isPrivateShop()){
											int newX2 = newX + random.nextInt(3)+1;
											int newY2 = newY + random.nextInt(3)+1;
											if (map.isInMap(newX2, newY2) && map.isPassable(newX2, newY2)) {
												L1Teleport.teleport(member, newX2, newY2, mapId, member.getMoveState().getHeading(), true, 0, false);
											}else{
												L1Teleport.teleport(member, newX, newY, mapId, member.getMoveState().getHeading(), true, 0, false);
											}
										}
									}
								}
								L1Teleport.teleport(pc, newX, newY, mapId, pc.getMoveState().getHeading(), true);
							} else {
								pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));
								pc.sendPackets(new S_ServerMessage(276));
							}
						}
					}
					break;
					case TELEPORT_TO_MOTHER:{
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getMap().isEscapable() || pc.isGm()) {
							L1Teleport.teleport(pc, 33051, 32337, (short) 4, 5, true);
						} else {
							pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));
							pc.sendPackets(new S_ServerMessage(647));
						}
					}
					break;
					case CALL_CLAN:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						L1PcInstance clanPc = (L1PcInstance) L1World.getInstance().findObject(_targetID);
						if (clanPc != null) {
							clanPc.setTempID(pc.getId()); 
							clanPc.sendPackets(new S_Message_YN(729, ""));
						}
					}
					break;
					case RUN_CLAN:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						L1PcInstance clanPc = (L1PcInstance) L1World.getInstance().findObject(_targetID);
						if (clanPc != null) {
							if (pc.getMap().isEscapable() || pc.isGm()) {
								int castleid = L1CastleLocation.getCastleIdByArea(clanPc);
								boolean castle_area = L1CastleLocation.checkInAllWarArea( clanPc.getX(), clanPc.getY(), clanPc.getMapId());
								if ((clanPc.getMapId() >= 191 && clanPc.getMapId() <= 200) && castle_area == true || castleid != 0 
										|| clanPc.getMapId() == 99 || clanPc.getMapId() == 52 
										|| clanPc.getMapId() == 64 || clanPc.getMapId() == 15 || clanPc.getMapId() == 29  || clanPc.getMapId() == 29
										|| clanPc.getMapId() == 110 || clanPc.getMapId() == 120  // oman
										|| clanPc.getMapId() == 130 || clanPc.getMapId() == 140
										|| clanPc.getMapId() == 150 || clanPc.getMapId() == 160
										|| clanPc.getMapId() == 170 || clanPc.getMapId() == 180
										|| clanPc.getMapId() == 190
										|| clanPc.getMapId() == 530 || clanPc.getMapId() == 531  // lastabard 4F
										|| clanPc.getMapId() == 532 || clanPc.getMapId() == 533 
										|| clanPc.getMapId() == 534 || clanPc.getMapId() == 535
										|| clanPc.getMapId() == 603  // 발록방
										|| clanPc.getMapId() == 522 || clanPc.getMapId() == 523 || clanPc.getMapId() == 524  // 그림자신전
										|| clanPc.getMapId() == 5167 || clanPc.getMapId() == 5302 // 낚시터
										|| clanPc.getMapId() == 1002 || clanPc.getMapId() == 1005 || clanPc.getMapId() == 1011
										|| clanPc.getMapId() == 5125 || clanPc.getMapId() == 5140 || clanPc.getMapId() == 5143
										|| clanPc.getMapId() == 5153 || clanPc.getMapId() == 5166
										) {
									pc.sendPackets(new S_ServerMessage(547));
								} else {									
									L1Teleport.teleport(pc, clanPc.getX(), clanPc.getY(), clanPc.getMapId(), 5, true);
								}
							} else {
								pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));
								pc.sendPackets(new S_ServerMessage(647));
							}
						}
					}
					break;
					case CREATE_MAGICAL_WEAPON:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 1) {
							int item_type = item.getItem().getType2();
							int safe_enchant = item.getItem().get_safeenchant();
							int enchant_level = item.getEnchantLevel();
							String item_name = item.getName();
							if (safe_enchant < 0) { 
								pc.sendPackets(new S_ServerMessage(79));
							} else if (safe_enchant == 0) { 
								pc.sendPackets(new S_ServerMessage(79));
							} else if (item_type == 1 && enchant_level == 0) {
								if (!item.isIdentified()) {
									pc.sendPackets(new S_ServerMessage(161, item_name, "$245", "$247"));
								} else {
									item_name = "+0 " + item_name;
									pc.sendPackets(new S_ServerMessage(161, "+0 " + item_name, "$245", "$247"));
								}
								item.setEnchantLevel(1);
								pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);
							} else {
								pc.sendPackets(new S_ServerMessage(79));
							}
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					}
					break;
					case BRING_STONE:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						Random random = new Random();
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null) {
							int dark = (int) (10 + (pc.getLevel() * 0.8) + (pc.getAbility().getTotalWis() - 6) * 1.2);
							int brave = (int) (dark / 2.1);
							int wise = (int) (brave / 2.0);
							int kayser = (int) (wise / 1.9);
							int chance = random.nextInt(100) + 1;
							if (item.getItem().getItemId() == 40320) {
								pc.getInventory().removeItem(item, 1);
								if (dark >= chance) {
									pc.getInventory().storeItem(40321, 1);
									pc.sendPackets(new S_ServerMessage(403, "$2475")); 
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							} else if (item.getItem().getItemId() == 40321) {
								pc.getInventory().removeItem(item, 1);
								if (brave >= chance) {
									pc.getInventory().storeItem(40322, 1);
									pc.sendPackets(new S_ServerMessage(403, "$2476")); 
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							} else if (item.getItem().getItemId() == 40322) {
								pc.getInventory().removeItem(item, 1);
								if (wise >= chance) {
									pc.getInventory().storeItem(40323, 1);
									pc.sendPackets(new S_ServerMessage(403, "$2477")); 
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							} else if (item.getItem().getItemId() == 40323) {
								pc.getInventory().removeItem(item, 1);
								if (kayser >= chance) {
									pc.getInventory().storeItem(40324, 1);
									pc.sendPackets(new S_ServerMessage(403, "$2478")); 
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							}
						}
					}
					break;
					case SUMMON_MONSTER:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						int level = pc.getLevel();
						int[] summons;
						if (pc.getMap().isRecallPets() || pc.isGm()) {
							if (pc.getInventory().checkEquipped(20284)) {
								pc.sendPackets(new S_ShowSummonList(pc.getId()));
								if (!pc.isSummonMonster()) {
									pc.setSummonMonster(true);
								}
							} else {
								summons = new int[] { 81083, 81084, 81085, 81086, 81087, 81088, 81089 };
								int summonid = 0;
								int summoncost = 6;
								int levelRange = 32;
								for (int i = 0; i < summons.length; i++) { 
									if (level < levelRange || i == summons.length - 1) {
										summonid = summons[i];
										break;
									}
									levelRange += 4;
								}

								int petcost = 0;
								Object[] petlist = pc.getPetList().values().toArray();
								for (Object pet : petlist) {
									petcost += ((L1NpcInstance) pet).getPetcost();
								}
								int charisma = pc.getAbility().getTotalCha() + 6 - petcost;
								int summoncount = charisma / summoncost;
								L1Npc npcTemp = NpcTable.getInstance().getTemplate(summonid);
								for (int i = 0; i < summoncount; i++) {
									L1SummonInstance summon = new L1SummonInstance(npcTemp, pc);
									summon.setPetcost(summoncost);
								}
							}
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					}
					break;
					case LESSER_ELEMENTAL:
					case GREATER_ELEMENTAL:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						int attr = pc.getElfAttr();
						if (attr != 0) { 
							if (pc.getMap().isRecallPets() || pc.isGm()) {
								int petcost = 0;
								Object[] petlist = pc.getPetList().values().toArray();
								for (Object pet : petlist) {
									petcost += ((L1NpcInstance) pet).getPetcost();
								}

								if (petcost == 0) { 
									int summonid = 0;
									int summons[];
									if (_skillId == LESSER_ELEMENTAL) { 
										summons = new int[] { 45306, 45303, 45304, 45305 };
									} else {
										summons = new int[] { 81053, 81050, 81051, 81052 };
									}
									int npcattr = 1;
									for (int i = 0; i < summons.length; i++) {
										if (npcattr == attr) {
											summonid = summons[i];
											i = summons.length;
										}
										npcattr *= 2;
									}
									if (summonid == 0) {
										Random random = new Random();
										int k3 = random.nextInt(4);
										summonid = summons[k3];
									}

									L1Npc npcTemp = NpcTable.getInstance().getTemplate(summonid);
									L1SummonInstance summon = new L1SummonInstance(npcTemp, pc);
									summon.setPetcost(pc.getAbility().getTotalCha() + 7); 
								}
							} else {
								pc.sendPackets(new S_ServerMessage(79));
							}
						}
					}
					break;
					//이부분은 전체적으로 처리를 하기때문에 제외함 

					case ABSOLUTE_BARRIER:{
						L1PcInstance pc = (L1PcInstance) cha;
						//pc.stopHpRegeneration();
						//pc.stopMpRegeneration();
						pc.stopMpRegenerationByDoll();
						pc.stopHpRegenerationByDoll();
					}
					break;					

					case LIGHT:
						break;
					case GLOWING_AURA:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addHitup(5);
						pc.addBowHitup(5);
						pc.getResistance().addMr(20);
						pc.sendPackets(new S_SPMR(pc));
						pc.sendPackets(new S_SkillIconAura(113, _getBuffIconDuration));
					}
					break;
					case SHINING_AURA:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-8);
						pc.sendPackets(new S_SkillIconAura(114, _getBuffIconDuration));
					}
					break;
					case BRAVE_AURA:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(5);
						pc.sendPackets(new S_SkillIconAura(116, _getBuffIconDuration));
					}
					break;
					case SHIELD:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-2);
						pc.sendPackets(new S_SkillIconShield(5, _getBuffIconDuration));
					}
					break;
					case SHADOW_ARMOR:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-3);
						pc.sendPackets(new S_SkillIconShield(3, _getBuffIconDuration));
					} 
					break;
					case DRESS_DEXTERITY:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedDex((byte) 2);
						pc.sendPackets(new S_Dexup(pc, 2, _getBuffIconDuration));
					} 
					break;
					case DRESS_MIGHTY:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedStr((byte) 2);
						pc.sendPackets(new S_Strup(pc, 2, _getBuffIconDuration));
					}
					break;
					case SHADOW_FANG:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 1) {
							item.setSkillWeaponEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					} 
					break;

					case ENCHANT_WEAPON:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 1) {
							pc.sendPackets(new S_ServerMessage(161, String.valueOf(item.getLogName()).trim(), "$245", "$247"));
							item.setSkillWeaponEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					}
					break;
					case HOLY_WEAPON:
					case BLESS_WEAPON:{ 
						if (!(cha instanceof L1PcInstance)) {
							return;
						}
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getWeapon() == null) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
						for (L1ItemInstance item : pc.getInventory().getItems()) {
							if (pc.getWeapon().equals(item)) {
								pc.sendPackets(new S_ServerMessage(161, String.valueOf(item.getLogName()).trim(), "$245", "$247"));
								item.setSkillWeaponEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
								return;
							}
						}
					}
					break;
					case BLESSED_ARMOR:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 2 && item.getItem().getType() == 2) {
							pc.sendPackets(new S_ServerMessage(161, String.valueOf(item.getLogName()).trim(), "$245", "$247"));							
							item.setSkillArmorEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					}
					break;
					case EARTH_BLESS:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-7);
						pc.sendPackets(new S_SkillIconShield(7, _getBuffIconDuration));
					}
					break;
					case RESIST_MAGIC:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getResistance().addMr(10);
						pc.sendPackets(new S_SPMR(pc));
					}
					break;
					case CLEAR_MIND:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedWis((byte) 3);
						pc.resetBaseMr();
						pc.sendPackets(new S_SPMR(pc));
					}
					break;
					case RESIST_ELEMENTAL:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getResistance().addAllNaturalResistance(10);
						pc.sendPackets(new S_OwnCharAttrDef(pc));
					}
					break;
					case BODY_TO_MIND:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.setCurrentMp(pc.getCurrentMp() + 2);
					}
					break;
					case BLOODY_SOUL:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.setCurrentMp(pc.getCurrentMp() + 16);
					}
					break;
					case ELEMENTAL_PROTECTION:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						int attr = pc.getElfAttr();
						if (attr == 1) {
							pc.getResistance().addEarth(50);
						} else if (attr == 2) {
							pc.getResistance().addFire(50);
						} else if (attr == 4) {
							pc.getResistance().addWater(50);
						} else if (attr == 8) {
							pc.getResistance().addWind(50);
						}
					} 
					break;
					case INVISIBILITY:
					case BLIND_HIDING:{
						L1PcInstance pc = (L1PcInstance) cha;

						for (L1DollInstance doll : pc.getDollList().values()) {
							doll.deleteDoll();
							pc.sendPackets(new S_SkillIconGFX(56, 0));
							pc.sendPackets(new S_OwnCharStatus(pc));
						}
						pc.sendPackets(new S_Invis(pc.getId(), 1));
						Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 1));
						//pc.broadcastPacket(new S_RemoveObject(pc));
					}
					break;
					case IRON_SKIN:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-10);
						pc.sendPackets(new S_SkillIconShield(10, _getBuffIconDuration));
					}
					break;
					case EARTH_SKIN:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-6);
						pc.sendPackets(new S_SkillIconShield(6, _getBuffIconDuration));
					}
					break;
					case PHYSICAL_ENCHANT_STR:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedStr((byte) 5);
						pc.sendPackets(new S_Strup(pc, 5, _getBuffIconDuration));
					}
					break;
					case PHYSICAL_ENCHANT_DEX:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedDex((byte) 5);
						pc.sendPackets(new S_Dexup(pc, 5, _getBuffIconDuration));
					}
					break;
					case FIRE_WEAPON:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(4);
						pc.sendPackets(new S_SkillIconAura(147, _getBuffIconDuration));
					} 
					break;
					case FIRE_BLESS:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(4);
						pc.sendPackets(new S_SkillIconAura(154, _getBuffIconDuration));
					} 
					break;
					case BURNING_WEAPON:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(6);
						pc.addHitup(3);
						pc.sendPackets(new S_SkillIconAura(162, _getBuffIconDuration));
					}
					break;
					case WIND_SHOT:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addBowHitup(6);
						pc.sendPackets(new S_SkillIconAura(148, _getBuffIconDuration));
					} 
					break;
					case STORM_EYE:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addBowHitup(2);
						pc.addBowDmgup(3);
						pc.sendPackets(new S_SkillIconAura(155, _getBuffIconDuration));
					}
					break;
					case STORM_SHOT:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addBowDmgup(5);
						pc.addBowHitup(-1);
						pc.sendPackets(new S_SkillIconAura(165, _getBuffIconDuration));
					}
					break;
					case BERSERKERS:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(10);
						pc.addDmgup(5);
						pc.addHitup(2);
					} 
					break;

					////그래픽 번호 수정했으나 0519에서 테스트 결과 그래픽 똑같이보임..

					////팩뷰어로 볼때는 확실히 번호 맞는듯 싶은데.. 옵코 문제일려나?

					///그래픽 번호 바꾸면 이속 공속은 알아서 해결될듯

					case SCALES_EARTH_DRAGON: { //용기사 리뉴얼
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addMaxHp(35);
						pc.getAC().addAc(-8);
						pc.sendPackets(new S_OwnCharAttrDef(pc));
						pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc
								.getMaxHp()));
						if (pc.isInParty()) {
							pc.getParty().updateMiniHP(pc);
						}
						pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc
								.getMaxMp()));
						pc.getGfxId().setTempCharGfx(9362);
						pc.sendPackets(new S_ChangeShape(pc.getId(), 9362)); //그래픽 번호 수정

						if (!pc.isGmInvis() && !pc.isInvisble()) {
							Broadcaster.broadcastPacket(pc, new S_ChangeShape(
									pc.getId(), 9362));
						}
						pc.startMpDecreaseByScales();
					}
					break;
					case SCALES_WATER_DRAGON: {//용기사 리뉴얼
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getResistance().addMr(15);
						pc.getResistance().addAllNaturalResistance(15);
						pc.sendPackets(new S_SPMR(pc));
						pc.sendPackets(new S_OwnCharAttrDef(pc));
						pc.getGfxId().setTempCharGfx(9364);
						pc.sendPackets(new S_ChangeShape(pc.getId(), 9364)); //그래픽 번호 수정
						if (!pc.isGmInvis() && !pc.isInvisble()) {
							Broadcaster.broadcastPacket(pc, new S_ChangeShape(
									pc.getId(), 9364));
						}
						pc.startMpDecreaseByScales();
					}
					break;
					case SCALES_FIRE_DRAGON: {//용기사 리뉴얼
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedStr((byte) 3);
						pc.getAbility().addAddedDex((byte) 3);
						pc.getAbility().addAddedCon((byte) 3);
						pc.getAbility().addAddedInt((byte) 3);
						pc.getAbility().addAddedWis((byte) 3);
						pc.getGfxId().setTempCharGfx(9363);
						pc.sendPackets(new S_ChangeShape(pc.getId(), 9363)); //그래픽 번호 수정
						if (!pc.isGmInvis() && !pc.isInvisble()) {
							Broadcaster.broadcastPacket(pc, new S_ChangeShape(
									pc.getId(), 9363));
						}
						pc.startMpDecreaseByScales();
					}
					break;

					case IllUSION_OGRE:{  //일루젼 오거
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(4);
						pc.addHitup(4);
					}
					break;
					case IllUSION_LICH:{  //일루젼 리치
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addSp(2);
						pc.sendPackets(new S_SPMR(pc));
					}
					break;
					case IllUSION_DIAMONDGOLEM:{  //일루젼 다이아골렘
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-20);
						pc.sendPackets(new S_OwnCharAttrDef(pc));
					}
					break;
					case IllUSION_AVATAR:{  //일루젼 아바타
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(10);
						pc.getAbility().addSp(6);
						pc.sendPackets(new S_SPMR(pc));
					} 
					break;
					case INSIGHT:{  // 인사이트
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAbility().addAddedStr((byte) 1);
						pc.getAbility().addAddedDex((byte) 1);
						pc.getAbility().addAddedCon((byte) 1);
						pc.getAbility().addAddedInt((byte) 1);
						pc.getAbility().addAddedWis((byte) 1);
						pc.resetBaseMr();
					}
					break;
					case SHAPE_CHANGE:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_ShowPolyList(pc.getId()));
						if (!pc.isShapeChange()) {
							pc.setShapeChange(true);
						}
					}
					break;
					case ADVANCE_SPIRIT:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.setAdvenHp(pc.getBaseMaxHp() / 5);
						pc.setAdvenMp(pc.getBaseMaxMp() / 5);
						pc.addMaxHp(pc.getAdvenHp());
						pc.addMaxMp(pc.getAdvenMp());						
						pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));						
						pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
						if (pc.isInParty()) { pc.getParty().updateMiniHP(pc); }
					}
					break;
					case GREATER_HASTE:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getHasteItemEquipped() > 0) {
							continue;
						}
						if (pc.getMoveState().getMoveSpeed() != 2) { 
							pc.setDrink(false);
							pc.getMoveState().setMoveSpeed(1);
							pc.sendPackets(new S_SkillHaste(pc.getId(), 1, _getBuffIconDuration));
							Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 1, 0));
						} else { 
							int skillNum = 0;
							if (pc.getSkillEffectTimerSet().hasSkillEffect(SLOW)) {
								skillNum = SLOW;
							} else if (pc.getSkillEffectTimerSet().hasSkillEffect(MASS_SLOW)) {
								skillNum = MASS_SLOW;
							} else if (pc.getSkillEffectTimerSet().hasSkillEffect(ENTANGLE)) {
								skillNum = ENTANGLE;
							} else if (pc.getSkillEffectTimerSet().hasSkillEffect(MOB_SLOW_1)) {
								skillNum = MOB_SLOW_1;
							} else if (pc.getSkillEffectTimerSet().hasSkillEffect(MOB_SLOW_18)) {
								skillNum = MOB_SLOW_18;
							}
							if (skillNum != 0) {
								pc.getSkillEffectTimerSet().removeSkillEffect(skillNum);
								pc.getSkillEffectTimerSet().removeSkillEffect(GREATER_HASTE);
								pc.getMoveState().setMoveSpeed(0);
								continue;
							}
						}
					}
					break;
					case HOLY_WALK:
					case MOVING_ACCELERATION:
					case WIND_WALK: { 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getMoveState().setBraveSpeed(4);
						pc.sendPackets(new S_SkillBrave(pc.getId(), 4, _getBuffIconDuration));
						Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 4, 0));
					}
					break;
					case BLOOD_LUST:{ 
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getMoveState().setBraveSpeed(1);
						pc.sendPackets(new S_SkillBrave(pc.getId(), 1, _getBuffIconDuration));
						Broadcaster.broadcastPacket(pc, new S_SkillBrave(pc.getId(), 1, 0));
					}
					break;
					case STATUS_TIKAL_BOSSJOIN:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addHitup(5);
						pc.addDmgup(10);
						pc.addBowHitup(5);
						pc.addBowDmgup(10);
						pc.getAbility().addAddedStr((byte) 3);
						pc.getAbility().addAddedDex((byte) 3);
						pc.getAbility().addAddedCon((byte) 3);
						pc.getAbility().addAddedInt((byte) 3);
						pc.getAbility().addAddedWis((byte) 3);
						pc.getAbility().addSp(3);
					}
					break;
					case STATUS_TIKAL_BOSSDIE:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addHitup(5);
						pc.addDmgup(5);
						pc.addBowHitup(5);
						pc.addBowDmgup(5);
						pc.getAbility().addAddedStr((byte) 2);
						pc.getAbility().addAddedDex((byte) 2);
						pc.getAbility().addAddedCon((byte) 2);
						pc.getAbility().addAddedInt((byte) 2);
						pc.getAbility().addAddedWis((byte) 2);
						pc.getAbility().addSp(1);
					}
					break;

					case BUFF_SAEL: {
						if(cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.addHitup(5);
							pc.addDmgup(1);
							pc.addBowHitup(5);
							pc.addBowDmgup(1);
							pc.addExp(30);
							pc.addMaxHp(100);
							pc.addMaxMp(50);
							pc.addHpr(3);
							pc.addMpr(3);
							pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
							pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
							pc.sendPackets(new S_SPMR(pc));
						}
					}
					break; 





					/**
					 * 파푸리온 리뉴얼
					 */

					case PAPOO_SKILL: //리오타! 누스건 카푸

						if (_player != null && _player.isInvisble()) {
							_player.delInvis();
						}

						if (!(cha instanceof L1PcInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							npc.getMoveState().setMoveSpeed(0);
							npc.getMoveState().setBraveSpeed(0);
							Broadcaster.broadcastPacket(npc, new S_SkillHaste(cha.getId(), 0, 0));
							Broadcaster.broadcastPacket(npc, new S_SkillBrave(cha.getId(), 0, 0));
							npc.setWeaponBreaked(false);
							npc.setParalyzed(false);
							npc.setParalysisTime(0);
						}

						for (int skillNum = SKILLS_BEGIN; skillNum <= SKILLS_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						for (int skillNum = STATUS_BEGIN; skillNum <= STATUS_CANCLEEND; skillNum++) {
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						cha.curePoison();
						cha.cureParalaysis();

						if (cha instanceof L1PcInstance) {}

						for (int skillNum = COOKING_BEGIN; skillNum <= COOKING_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;

							L1PolyMorph.undoPoly(pc);
							pc.sendPackets(new S_CharVisualUpdate(pc));
							Broadcaster.broadcastPacket(pc,new S_CharVisualUpdate(pc));

							if (pc.getHasteItemEquipped() > 0) {
								pc.getMoveState().setMoveSpeed(0);
								pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
								Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
							}
							if (pc != null && pc.isInvisble()) {
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
									pc.sendPackets(new S_Sound(147));
								}
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLIND_HIDING)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
								}
							}
						}
						cha.getSkillEffectTimerSet().removeSkillEffect(STATUS_FREEZE);
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 누스건 카푸", 0));
						L1SpawnUtil.spawn3(_npc, 45943, 6, 0, false);
						break;
					case PAPOO_SKILL2:
						if (_player != null && _player.isInvisble()) {
							_player.delInvis();
						}
						if (!(cha instanceof L1PcInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							npc.getMoveState().setMoveSpeed(0);
							npc.getMoveState().setBraveSpeed(0);
							Broadcaster.broadcastPacket(npc, new S_SkillHaste(cha.getId(), 0, 0));
							Broadcaster.broadcastPacket(npc, new S_SkillBrave(cha.getId(), 0, 0));
							npc.setWeaponBreaked(false);
							npc.setParalyzed(false);
							npc.setParalysisTime(0);
						}

						for (int skillNum = SKILLS_BEGIN; skillNum <= SKILLS_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						for (int skillNum = STATUS_BEGIN; skillNum <= STATUS_CANCLEEND; skillNum++) {
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						cha.curePoison();
						cha.cureParalaysis();

						if (cha instanceof L1PcInstance) {}

						for (int skillNum = COOKING_BEGIN; skillNum <= COOKING_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;

							L1PolyMorph.undoPoly(pc);
							pc.sendPackets(new S_CharVisualUpdate(pc));
							Broadcaster.broadcastPacket(pc,new S_CharVisualUpdate(pc));

							if (pc.getHasteItemEquipped() > 0) {
								pc.getMoveState().setMoveSpeed(0);
								pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
								Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
							}
							if (pc != null && pc.isInvisble()) {
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
									pc.sendPackets(new S_Sound(147));
								}
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLIND_HIDING)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
								}
							}
						}
						cha.getSkillEffectTimerSet().removeSkillEffect(STATUS_FREEZE); 
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 나나 폰폰..", 0));
						S_DoActionGFX gfx = new S_DoActionGFX(_user.getId(), 5);
						Broadcaster.broadcastPacket(_user, gfx);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL3:
						if (_calcType == PC_PC || _calcType == NPC_PC) {
							if (cha instanceof L1PcInstance) {
								L1PcInstance pc = (L1PcInstance) cha;
								L1ItemInstance weapon = pc.getWeapon();
								if (weapon != null) {
									int weaponDamage = random.nextInt(_user.getAbility().getTotalInt() / 3) + 1;
									pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
									pc.getInventory().receiveDamage(weapon, weaponDamage);
								}
							}
						} else {
							((L1NpcInstance) cha).setWeaponBreaked(true);
						}
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 레포 폰폰..", 0));
						S_DoActionGFX leo = new S_DoActionGFX(_user.getId(), 12);
						Broadcaster.broadcastPacket(_user, leo);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL4:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 테나 론디르", 0));
						S_DoActionGFX tena = new S_DoActionGFX(_user.getId(), 25);
						S_DoActionGFX tena2 = new S_DoActionGFX(_user.getId(), 18);
						Broadcaster.broadcastPacket(_user, tena);
						Broadcaster.broadcastPacket(_user, tena2);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL5:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 네나 론디르", 0));
						S_DoActionGFX lena = new S_DoActionGFX(_user.getId(), 1);
						S_DoActionGFX lena2 = new S_DoActionGFX(_user.getId(), 18);
						Broadcaster.broadcastPacket(_user, lena);
						Broadcaster.broadcastPacket(_user, lena2);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL6:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 라나 오이므", 0));
						//if (cha instanceof L1PcInstance) {
						//	L1PcInstance pc = (L1PcInstance) cha;
						/*pc.getSkillEffectTimerSet().setSkillEffect(10513, 12);
		      pc.sendPackets(new S_SkillSound(pc.getId(), 7781));
		      Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 7781));*/

						//}

						S_DoActionGFX lana= new S_DoActionGFX(_user.getId(), 5);
						Broadcaster.broadcastPacket(_user, lana);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL7:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 레포 오이므", 0));
						//if (cha instanceof L1PcInstance) {
						//	L1PcInstance pc = (L1PcInstance) cha;
						/*pc.getSkillEffectTimerSet().setSkillEffect(10513, 12);
		      pc.sendPackets(new S_SkillSound(pc.getId(), 7781));
		      Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 7781));*/

						//}
						S_DoActionGFX repo= new S_DoActionGFX(_user.getId(), 19);
						Broadcaster.broadcastPacket(_user, repo);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL8:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 테나 웨인라크", 0));
						//if (cha instanceof L1PcInstance) {
						//	L1PcInstance pc = (L1PcInstance) cha;

						//}
						S_DoActionGFX tenas= new S_DoActionGFX(_user.getId(), 18);
						Broadcaster.broadcastPacket(_user, tenas);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL9:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 네나 우누스", 0));
						//if (cha instanceof L1PcInstance) {
						//	L1PcInstance pc = (L1PcInstance) cha;

						//}
						S_DoActionGFX nean= new S_DoActionGFX(_user.getId(), 18);
						Broadcaster.broadcastPacket(_user, nean);
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL10:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 오니즈 웨인라크", 0));
						if (_player != null && _player.isInvisble()) {
							_player.delInvis();
						}

						if (!(cha instanceof L1PcInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							npc.getMoveState().setMoveSpeed(0);
							npc.getMoveState().setBraveSpeed(0);
							Broadcaster.broadcastPacket(npc, new S_SkillHaste(cha.getId(), 0, 0));
							Broadcaster.broadcastPacket(npc, new S_SkillBrave(cha.getId(), 0, 0));
							npc.setWeaponBreaked(false);
							npc.setParalyzed(false);
							npc.setParalysisTime(0);
						}

						for (int skillNum = SKILLS_BEGIN; skillNum <= SKILLS_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						for (int skillNum = STATUS_BEGIN; skillNum <= STATUS_CANCLEEND; skillNum++) {
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						cha.curePoison();
						cha.cureParalaysis();

						if (cha instanceof L1PcInstance) {}

						for (int skillNum = COOKING_BEGIN; skillNum <= COOKING_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;

							L1PolyMorph.undoPoly(pc);
							pc.sendPackets(new S_CharVisualUpdate(pc));
							Broadcaster.broadcastPacket(pc,new S_CharVisualUpdate(pc));

							if (pc.getHasteItemEquipped() > 0) {
								pc.getMoveState().setMoveSpeed(0);
								pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
								Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
							}
							if (pc != null && pc.isInvisble()) {
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
									pc.sendPackets(new S_Sound(147));
								}
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLIND_HIDING)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
								}
							}
						}

						//if (cha instanceof L1PcInstance) {
						//	L1PcInstance pc = (L1PcInstance) cha;

						//}
						dmg = _magic.calcMagicDamage(_skillId);
						break;
					case PAPOO_SKILL11:
						Broadcaster.broadcastPacket(_user, new S_NpcChatPacket(_npc, "리오타! 오니즈 쿠스온 웨인라크", 0));
						if (_player != null && _player.isInvisble()) {
							_player.delInvis();
						}

						if (!(cha instanceof L1PcInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							npc.getMoveState().setMoveSpeed(0);
							npc.getMoveState().setBraveSpeed(0);
							Broadcaster.broadcastPacket(npc, new S_SkillHaste(cha.getId(), 0, 0));
							Broadcaster.broadcastPacket(npc, new S_SkillBrave(cha.getId(), 0, 0));
							npc.setWeaponBreaked(false);
							npc.setParalyzed(false);
							npc.setParalysisTime(0);
						}

						for (int skillNum = SKILLS_BEGIN; skillNum <= SKILLS_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						for (int skillNum = STATUS_BEGIN; skillNum <= STATUS_CANCLEEND; skillNum++) {
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						cha.curePoison();
						cha.cureParalaysis();

						if (cha instanceof L1PcInstance) {}

						for (int skillNum = COOKING_BEGIN; skillNum <= COOKING_END; skillNum++) {
							if (isNotCancelable(skillNum) && !cha.isDead()) {
								continue;
							}
							cha.getSkillEffectTimerSet().removeSkillEffect(skillNum);
						}

						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;

							L1PolyMorph.undoPoly(pc);
							pc.sendPackets(new S_CharVisualUpdate(pc));
							Broadcaster.broadcastPacket(pc,new S_CharVisualUpdate(pc));

							if (pc.getHasteItemEquipped() > 0) {
								pc.getMoveState().setMoveSpeed(0);
								pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
								Broadcaster.broadcastPacket(pc, new S_SkillHaste(pc.getId(), 0, 0));
							}
							if (pc != null && pc.isInvisble()) {
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.INVISIBILITY);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
									pc.sendPackets(new S_Sound(147));
								}
								if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLIND_HIDING)) { 
									pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
									pc.sendPackets(new S_Invis(pc.getId(), 0));
									Broadcaster.broadcastPacket(pc, new S_Invis(pc.getId(), 0));
								}
							}
						}

						//if (cha instanceof L1PcInstance) {
						//	L1PcInstance pc = (L1PcInstance) cha;

						//}
						S_DoActionGFX onian= new S_DoActionGFX(_user.getId(), 30);
						Broadcaster.broadcastPacket(_user, onian);
						dmg = _magic.calcMagicDamage(_skillId);
						break;

					case DRAGONBLOOD_A:{      
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_A)) 
							pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.DRAGONBLOOD_A);
						pc.getResistance().addWater(50);
						pc.getAC().addAc(-2);
						pc.sendPackets(new S_SPMR(pc));
						pc.sendPackets(new S_OwnCharAttrDef(pc));
						pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONBLOOD, 82, _getBuffIconDuration/60));
					}
					break;


					case DRAGONBLOOD_P:{   

						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_P)) 
							pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.DRAGONBLOOD_P);
						pc.getResistance().addWind(50);
						pc.addHpr(3);
						pc.addMpr(1);
						pc.sendPackets(new S_SPMR(pc));
						pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONBLOOD, 85, _getBuffIconDuration/60));
						pc.sendPackets(new S_OwnCharAttrDef(pc));
					}
					break;

					/////  마안 버프 ////
					case ANTA_MAAN://지룡마안
						break;
					case FAFU_MAAN: //수룡마안
						break;

					case VALA_MAAN://화룡마안
						break;

					case LIND_MAAN://풍룡마안
						break;

					case BIRTH_MAAN: //탄생마안
						break;

					case SHAPE_MAAN://형상마안
						break;

					case LIFE_MAAN: //생명마안

						break;

						/*case MAAN_TIMER://마안타이머  //추가
					break;*/
						/////  마안 버프 ////

					case STATUS_COMA_3:{
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_COMA_5)) 
							pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_COMA_5);
						pc.getAC().addAc(-2);
						pc.addHitup(3);
						pc.getAbility().addAddedStr((byte) 5);
						pc.getAbility().addAddedDex((byte) 5);
						pc.getAbility().addAddedCon((byte) 1);
					}
					break;
					case STATUS_COMA_5:{
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_COMA_3)) 
							pc.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_COMA_3);
						pc.getAC().addAc(-8);
						pc.addHitup(5);
						pc.getAbility().addAddedStr((byte) 5);
						pc.getAbility().addAddedDex((byte) 5);
						pc.getAbility().addAddedCon((byte) 1);
						pc.getAbility().addSp(1);
					}
					break;
					case FEATHER_BUFF_A:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(2);
						pc.addHitup(2);
						pc.getAbility().addSp(2);
						pc.sendPackets(new S_SPMR(pc));						
						pc.addMaxHp(50);
						pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
						if (pc.isInParty()) pc.getParty().updateMiniHP(pc);
						pc.addHpr(3);
						pc.addMaxMp(30);
						pc.addMpr(3);						
						pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					}break;
					case FEATHER_BUFF_B:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addHitup(2);
						pc.getAbility().addSp(1);
						pc.sendPackets(new S_SPMR(pc));
						pc.addMaxHp(50);
						pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
						if (pc.isInParty()) pc.getParty().updateMiniHP(pc);
						pc.addMaxMp(30);
						pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					}break;
					case FEATHER_BUFF_C:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addMaxHp(50);
						pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
						if (pc.isInParty()) pc.getParty().updateMiniHP(pc);
						pc.addMaxMp(30);
						pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
						pc.getAC().addAc(-2);
						pc.sendPackets(new S_OwnCharAttrDef(pc));
					}break;
					case FEATHER_BUFF_D:{
						L1PcInstance pc = (L1PcInstance) cha;
						pc.getAC().addAc(-1);
						pc.sendPackets(new S_OwnCharAttrDef(pc));
					}break;
					default:
						break;
					}
				}

				if (_calcType == PC_NPC || _calcType == NPC_NPC) {
					if (_skillId == TAMING_MONSTER && ((L1MonsterInstance) cha).getNpcTemplate().isTamable()) { 
						int petcost = 0;
						Object[] petlist = ((L1PcInstance)_user).getPetList().values().toArray();
						for (Object pet : petlist) {
							petcost += ((L1NpcInstance) pet).getPetcost();
						}
						int charisma = _user.getAbility().getTotalCha();
						if (_player.isElf()) { 
							charisma += 12;
						} else if (_player.isWizard()) { 
							charisma += 6;
						}
						charisma -= petcost;
						if (charisma >= 6) { 
							L1SummonInstance summon = new L1SummonInstance(_targetNpc, (L1PcInstance)_user, false);
							_target = summon; 
						} else {
							_player.sendPackets(new S_ServerMessage(319)); 
						}
					} else if (_skillId == CREATE_ZOMBIE) { 
						int petcost = 0;
						Object[] petlist = ((L1PcInstance)_user).getPetList().values().toArray();
						for (Object pet : petlist) {
							petcost += ((L1NpcInstance) pet).getPetcost();
						}
						int charisma = _user.getAbility().getTotalCha();
						if (_player.isElf()) { 
							charisma += 12;
						} else if (_player.isWizard()) { 
							charisma += 6;
						}
						charisma -= petcost;
						if (charisma >= 6) { 
							L1SummonInstance summon = new L1SummonInstance(_targetNpc, (L1PcInstance)_user, true);
							_target = summon; 
						} else {
							_player.sendPackets(new S_ServerMessage(319)); 
						}
					} else if (_skillId == WEAK_ELEMENTAL) { 
						if (cha instanceof L1MonsterInstance) {
							L1Npc npcTemp = ((L1MonsterInstance) cha).getNpcTemplate();
							int weakAttr = npcTemp.get_weakAttr();
							if ((weakAttr & 1) == 1) {
								Broadcaster.broadcastPacket(cha, new S_SkillSound(cha.getId(), 2169));
							}
							if ((weakAttr & 2) == 2) {
								Broadcaster.broadcastPacket(cha, new S_SkillSound(cha.getId(), 2167));
							}
							if ((weakAttr & 4) == 4) {
								Broadcaster.broadcastPacket(cha, new S_SkillSound(cha.getId(), 2166));
							}
							if ((weakAttr & 8) == 8) {
								Broadcaster.broadcastPacket(cha, new S_SkillSound(cha.getId(), 2168));
							}
						}
					} else if (_skillId == RETURN_TO_NATURE) { 
						if (Config.RETURN_TO_NATURE
								&& cha instanceof L1SummonInstance) {
							L1SummonInstance summon = (L1SummonInstance) cha;
							Broadcaster.broadcastPacket(summon, new S_SkillSound(summon.getId(), 2245));
							summon.returnToNature();
						} else {
							if (_user instanceof L1PcInstance) {
								_player.sendPackets(new S_ServerMessage(79));
							}
						}
					}
				}

				if (_skill.getType() == L1Skills.TYPE_HEAL && _calcType == PC_NPC && undeadType == 1) {
					dmg *= -1; 
				}

				if (_skill.getType() == L1Skills.TYPE_HEAL && _calcType == PC_NPC && undeadType == 3) {
					dmg = 0; 
				}

				if ((cha instanceof L1TowerInstance || cha instanceof L1DoorInstance) && dmg < 0) {
					dmg = 0;
				}

				if (dmg != 0 || drainMana != 0) {
					_magic.commit(dmg, drainMana); 
				}

				if (heal > 0) {
					if ((heal + _user.getCurrentHp()) > _user.getMaxHp()) {
						_user.setCurrentHp(_user.getMaxHp());
					} else {
						_user.setCurrentHp(heal + _user.getCurrentHp());
					}
				}

				if (cha instanceof L1PcInstance) { 
					L1PcInstance pc = (L1PcInstance) cha;
					pc.getLight().turnOnOffLight();
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.sendPackets(new S_OwnCharStatus(pc));
					sendHappenMessage(pc); 
				}

				addMagicList(cha, false); 
				if (cha instanceof L1PcInstance) {
					L1PcInstance pc = (L1PcInstance) cha;
					pc.getLight().turnOnOffLight();
				}
			}

			if (_skillId == DETECTION || _skillId == COUNTER_DETECTION) { 
				detection(_player);
			}

		} catch (Exception e) {
			//스킬 오류 발생 부분에 케릭터명, 몹명, 타켓명순으로 출력
			System.out.println("오류 발생 : " + _player.getAccountName() + " | " + _npc.getName() + " | " + _target.getName());
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private boolean isNotCancelable(int skillNum) {
		return skillNum == ENCHANT_WEAPON 
				|| skillNum == BLESSED_ARMOR
				|| skillNum == ABSOLUTE_BARRIER 
				|| skillNum == AVATA 
				|| skillNum == ADVANCE_SPIRIT
				|| skillNum == SHOCK_STUN 
				|| skillNum == SHADOW_FANG
				|| skillNum == REDUCTION_ARMOR 
				|| skillNum == SOLID_CARRIAGE
				|| skillNum == COUNTER_BARRIER;
	}

	private void detection(L1PcInstance pc) {
		if (!pc.isGmInvis() && pc.isInvisble()) { 
			pc.delInvis();
			pc.beginInvisTimer();
		}

		for (L1PcInstance tgt : L1World.getInstance().getVisiblePlayer(pc)) {
			if (!tgt.isGmInvis() && tgt.isInvisble()) {
				tgt.delInvis();
			}
		}
		L1WorldTraps.getInstance().onDetection(pc);
	}

	private boolean isTargetCalc(L1Character cha) {
		if (_skill.getTarget().equals("attack") && _skillId != 18) { 
			if (isPcSummonPet(cha)) {
				if (CharPosUtil.getZoneType(_player) == 1 
						|| CharPosUtil.getZoneType(cha) == 1
						|| _player.checkNonPvP(_player, cha)) { 
					return false;
				}
			}
		}

		if (_skillId == FOG_OF_SLEEPING && _user.getId() == cha.getId()) {
			return false;
		}

		if (_skillId == MASS_SLOW) {
			if (_user.getId() == cha.getId()) {
				return false;
			}
			if (cha instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (_user.getId() == summon.getMaster().getId()) {
					return false;
				}
			} else if (cha instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) cha;
				if (_user.getId() == pet.getMaster().getId()) {
					return false;
				}
			}
		}

		if (_skillId == MASS_TELEPORT) {
			if (_user.getId() != cha.getId()) {
				return false;
			}
		}

		return true;
	}

	private boolean isPcSummonPet(L1Character cha) {
		if (_calcType == PC_PC) {
			return true;
		}

		if (_calcType == PC_NPC) {
			if (cha instanceof L1SummonInstance) { 
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (summon.isExsistMaster()) { 
					return true;
				}
			}
			if (cha instanceof L1PetInstance) { 
				return true;
			}
		}
		return false;
	}

	private boolean isUseCounterMagic(L1Character cha) {
		if (!_skill.ignoresCounterMagic() && cha.getSkillEffectTimerSet().hasSkillEffect(COUNTER_MAGIC)) {
			cha.getSkillEffectTimerSet().removeSkillEffect(COUNTER_MAGIC);
			int castgfx = SkillsTable.getInstance().getTemplate(COUNTER_MAGIC).getCastGfx();
			Broadcaster.broadcastPacket(cha, new S_SkillSound(cha.getId(), castgfx));
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				pc.sendPackets(new S_SkillSound(pc.getId(), castgfx));
			}
			return true;
		}
		return false;
	}

	private boolean isTargetFailure(L1Character cha) {
		boolean isTU = false;
		boolean isErase = false;
		boolean isManaDrain = false;
		int undeadType = 0;

		if (cha instanceof L1TowerInstance || cha instanceof L1DoorInstance) {
			return true;
		}

		if (cha instanceof L1PcInstance) {
			if (_calcType == PC_PC && _player.checkNonPvP(_player, cha)) { 
				L1PcInstance pc = (L1PcInstance) cha;
				if (_player.getId() == pc.getId() 
						|| (pc.getClanid() != 0 && _player.getClanid() == pc.getClanid())) {
					return false;
				}
				return true;
			}
			return false;
		}

		if (cha instanceof L1MonsterInstance) {
			isTU = ((L1MonsterInstance) cha).getNpcTemplate().get_IsTU();
			isErase = ((L1MonsterInstance) cha).getNpcTemplate().get_IsErase();
			undeadType = ((L1MonsterInstance) cha).getNpcTemplate().get_undead();
			isManaDrain = true;
		}
		if ((_skillId == TURN_UNDEAD && (undeadType == 0 || undeadType == 2))
				|| (_skillId == TURN_UNDEAD && isTU == false)
				|| ((_skillId == ERASE_MAGIC 
				|| _skillId == SLOW
				|| _skillId == MOB_SLOW_1 
				|| _skillId == MOB_SLOW_18 
				|| _skillId == MANA_DRAIN 
				|| _skillId == MASS_SLOW
				|| _skillId == ENTANGLE 
				|| _skillId == WIND_SHACKLE)
				&& isErase == false)
				|| (_skillId == MANA_DRAIN && isManaDrain == false)) {
			return true;
		}
		return false;
	}
}
