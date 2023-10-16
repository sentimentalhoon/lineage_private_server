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
package l1j.server.server.model;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.AVATA;
import static l1j.server.server.model.skill.L1SkillId.BOUNCE_ATTACK;
import static l1j.server.server.model.skill.L1SkillId.BURNING_SLASH;
import static l1j.server.server.model.skill.L1SkillId.BURNING_SPIRIT;
import static l1j.server.server.model.skill.L1SkillId.BURNING_WEAPON;
import static l1j.server.server.model.skill.L1SkillId.DOUBLE_BRAKE;
import static l1j.server.server.model.skill.L1SkillId.DRAGON_SKIN;
import static l1j.server.server.model.skill.L1SkillId.EARTH_BIND;
import static l1j.server.server.model.skill.L1SkillId.ELEMENTAL_FIRE;
import static l1j.server.server.model.skill.L1SkillId.ENCHANT_VENOM;
import static l1j.server.server.model.skill.L1SkillId.FEAR;
import static l1j.server.server.model.skill.L1SkillId.FEATHER_BUFF_A;
import static l1j.server.server.model.skill.L1SkillId.FEATHER_BUFF_B;
import static l1j.server.server.model.skill.L1SkillId.FIRE_BLESS;
import static l1j.server.server.model.skill.L1SkillId.FIRE_WEAPON;
import static l1j.server.server.model.skill.L1SkillId.FREEZING_BLIZZARD;
import static l1j.server.server.model.skill.L1SkillId.FREEZING_BREATH;
import static l1j.server.server.model.skill.L1SkillId.ICE_LANCE;
import static l1j.server.server.model.skill.L1SkillId.IMMUNE_TO_HARM;
import static l1j.server.server.model.skill.L1SkillId.IllUSION_AVATAR;
import static l1j.server.server.model.skill.L1SkillId.LIFE_MAAN;
import static l1j.server.server.model.skill.L1SkillId.MIRROR_IMAGE;
import static l1j.server.server.model.skill.L1SkillId.MOB_BASILL;
import static l1j.server.server.model.skill.L1SkillId.MOB_COCA;
import static l1j.server.server.model.skill.L1SkillId.PATIENCE;
import static l1j.server.server.model.skill.L1SkillId.REDUCTION_ARMOR;
import static l1j.server.server.model.skill.L1SkillId.SOUL_OF_FLAME;
import static l1j.server.server.model.skill.L1SkillId.SPECIAL_COOKING;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_BARLOG;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_YAHEE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_MITHRIL_POWDER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER_OF_EVA;
import static l1j.server.server.model.skill.L1SkillId.STORM_EYE;
import static l1j.server.server.model.skill.L1SkillId.STORM_SHOT;
import static l1j.server.server.model.skill.L1SkillId.UNCANNY_DODGE;
import static l1j.server.server.model.skill.L1SkillId.VALA_MAAN;

import java.util.Random;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.gametime.GameTimeClock;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.model.poison.L1ParalysisPoison;
import l1j.server.server.model.poison.L1SilencePoison;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_AttackMissPacket;
import l1j.server.server.serverpackets.S_AttackPacket;
import l1j.server.server.serverpackets.S_AttackPacketForNpc;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_UseArrowSkill;
import l1j.server.server.serverpackets.S_UseAttackSkill;
import l1j.server.server.types.Point;
import l1j.server.server.utils.CommonUtil;

public class L1Attack {

	private L1PcInstance _pc = null;

	private L1Character _target = null;

	private L1PcInstance _targetPc = null;

	private L1NpcInstance _npc = null;

	private L1NpcInstance _targetNpc = null;

	private final int _targetId;

	private int _targetX;

	private int _targetY;

	private int _statusDamage = 0;

	private static final Random _random = new Random(System.nanoTime());

	private int _hitRate = 0;

	private int _calcType;

	private static final int PC_PC = 1;

	private static final int PC_NPC = 2;

	private static final int NPC_PC = 3;

	private static final int NPC_NPC = 4;

	private boolean _isHit = false;

	private int _damage = 0;

	private int _drainMana = 0;

	/** 조우의 돌골렘 * */

	private int _drainHp = 0;

	/** 조우의 돌골렘 * */

	private int _attckGrfxId = 0;

	private int _attckActId = 0;

	// 공격자가 플레이어의 경우의 무기 정보
	private L1ItemInstance weapon = null;

	private int _weaponId = 0;

	private int _weaponType = 0;

	private int _weaponType1 = 0;

	private int _weaponAddHit = 0;

	private int _weaponAddDmg = 0;

	private int _weaponSmall = 0;

	private int _weaponLarge = 0;

	private int _weaponRange = 1;

	private int _weaponBless = 1;

	private int _weaponEnchant = 0;

	private int _weaponMaterial = 0;

	private int _weaponAttrEnchantLevel = 0;

	private int _weaponDoubleDmgChance = 0;

	private int _attackType = 0;

	private L1ItemInstance _arrow = null;

	private L1ItemInstance _sting = null;

	private int _leverage = 10; // 1/10배로 표현한다.

	public void setLeverage(int i) {
		_leverage = i;
	}

	private int getLeverage() {
		return _leverage;
	}

	// 공격자가 플레이어의 경우의 스테이터스에 의한 보정
	private static final int[] strHit = { -2, -2, -2, -2, -2, -2, -2, // 1`7
		-2, -1, -1, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6, // 8`26
		7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, // 27`44
		13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 17}; // 45`59

	private static final int[] dexHit = { -2, -2, -2, -2, -2, -2, -1, -1, 0, 0, // 1`10
		1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, // 11`30
		17, 18, 19, 19, 19, 20, 20, 20, 21, 21, 21, 22, 22, 22, 23, // 31`45
		23, 23, 24, 24, 24, 25, 25, 25, 26, 26, 26, 27, 27, 27, 28 }; // 46`60

	private static final int[] strDmg = new int[128];
	static {
		// STR_[W
		int dmg = -6;
		for (int str = 0; str <= 22; str++) { // 0`222+1
			if (str % 2 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 23; str <= 28; str++) { // 23`283+1
			if (str % 3 == 2) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 29; str <= 32; str++) { // 29`322+1
			if (str % 2 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
		for (int str = 33; str <= 34; str++) { // 33`341+1
			dmg++;
			strDmg[str] = dmg;
		}
		for (int str = 35; str <= 127; str++) { // 35`1274+1
			if (str % 4 == 1) {
				dmg++;
			}
			strDmg[str] = dmg;
		}
	}

	private static final int[] dexDmg = new int[128];

	static {
		// DEX_[W
		for (int dex = 0; dex <= 14; dex++) {
			// 0`140
			dexDmg[dex] = 0;
		}
		dexDmg[15] = 1;
		dexDmg[16] = 2;
		dexDmg[17] = 3;
		dexDmg[18] = 4;
		dexDmg[19] = 4;
		dexDmg[20] = 4;
		dexDmg[21] = 5;
		dexDmg[22] = 5;
		dexDmg[23] = 5;
		int dmg = 5;
		for (int dex = 24; dex <= 35; dex++) { // 24`353+1
			if (dex % 3 == 1) {
				dmg++;
			}
			dexDmg[dex] = dmg;
		}
		for (int dex = 36; dex <= 127; dex++) { // 36`12741
			if (dex % 4 == 1) {
				dmg++;
			}
			dexDmg[dex] = dmg;
		}
	}

	private static final int[] IntDmg = new int[128]; // 키링크 인트

	static {
		// Int 데미지 보정
		/*
		 * int dmg = 0; for (int Int = 0; Int <= 8; Int++) { IntDmg[Int] = 0; }
		 * for (int Int = 12; Int <= 127; Int++) { dmg+= 1; IntDmg[Int] = dmg;
		 */
		int dmg = 0;
		for (int Int = 0; Int <= 32; Int++) { // 0~32는 3마다＋1
			if (Int % 3 == 1) {
				dmg += 1;
			}
			IntDmg[Int] = dmg;
		}					
		for (int Int = 33; Int <= 127; Int++) { // 33~39는 2마다＋1
			if (Int % 3 == 1) {
				dmg += 1;
			}
			IntDmg[Int] = dmg;
		}
	}

	// ////////// 키링크 인트 데미지 보정 //////////

	public void setActId(int actId) {
		_attckActId = actId;
	}

	public void setGfxId(int gfxId) {
		_attckGrfxId = gfxId;
	}

	public int getActId() {
		return _attckActId;
	}

	public int getGfxId() {
		return _attckGrfxId;
	}

	public L1Attack(L1Character attacker, L1Character target) {
		if (attacker instanceof L1PcInstance) {
			_pc = (L1PcInstance) attacker;
			if (target instanceof L1PcInstance) {
				_targetPc = (L1PcInstance) target;
				_calcType = PC_PC;
			} else if (target instanceof L1NpcInstance) {
				_targetNpc = (L1NpcInstance) target;
				_calcType = PC_NPC;
			}
			// 무기 정보의 취득
			weapon = _pc.getWeapon();
			if (weapon != null) {
				_weaponId = weapon.getItem().getItemId();
				_weaponType = weapon.getItem().getType1();
				_weaponType1 = weapon.getItem().getType();
				_weaponAddHit = weapon.getItem().getHitModifier() + weapon.getHitByMagic();
				_weaponAddDmg = weapon.getItem().getDmgModifier() + weapon.getDmgByMagic();
				// 바포 - 공격자가 pc 대상자가 pc 라면
				if (_calcType == PC_PC) {
					// 검 데미지에 + 해주기
					// 검 대미지 + 공격자 대미지 - 맞는놈 방어 대미
					_weaponAddDmg = _weaponAddDmg + _pc.getPhysics_dmg_attack()	- _targetPc.getPhysics_dmg_armor();
				}
				//
				_weaponSmall = weapon.getItem().getDmgSmall();
				_weaponLarge = weapon.getItem().getDmgLarge();
				_weaponRange = weapon.getItem().getRange();
				_weaponBless = weapon.getItem().getBless();
				if (_weaponType != 20 && _weaponType != 62) {
					_weaponEnchant = weapon.getEnchantLevel() - weapon.get_durability(); // 손상분 마이너스
				} else {
					_weaponEnchant = weapon.getEnchantLevel();
				}
				_weaponMaterial = weapon.getItem().getMaterial();
				if (_weaponType == 20) { // 화살의 취득
					_arrow = _pc.getInventory().getArrow();
					if (_arrow != null) {
						_weaponBless = _arrow.getItem().getBless();
						_weaponMaterial = _arrow.getItem().getMaterial();
					}
				}
				if (_weaponType == 62) { // 스팅의 취득
					_sting = _pc.getInventory().getSting();
					if (_sting != null) {
						_weaponBless = _sting.getItem().getBless();
						_weaponMaterial = _sting.getItem().getMaterial();
					}
				}
				_weaponDoubleDmgChance = weapon.getItem().getDoubleDmgChance();
				_weaponAttrEnchantLevel = weapon.getAttrEnchantLevel();
			}
			// 스테이터스에 의한 추가 데미지 보정
			if (_weaponType == 20) { // 활의 경우는 DEX치 참조
				_statusDamage = dexDmg[_pc.getAbility().getTotalDex()];
			} else if (_weaponType1 == 17) { // 키링크의 경우 INT치 참조
				_statusDamage = IntDmg[_pc.getAbility().getTotalInt()];
			} else {
				_statusDamage = strDmg[_pc.getAbility().getTotalStr()];
			}
		} else if (attacker instanceof L1NpcInstance) {
			_npc = (L1NpcInstance) attacker;
			if (target instanceof L1PcInstance) {
				_targetPc = (L1PcInstance) target;
				_calcType = NPC_PC;
			} else if (target instanceof L1NpcInstance) {
				_targetNpc = (L1NpcInstance) target;
				_calcType = NPC_NPC;
			}
		}
		_target = target;
		_targetId = target.getId();
		_targetX = target.getX();
		_targetY = target.getY();
	}

	/* ■■■■■■■■■■■■■■■■ 명중 판정 ■■■■■■■■■■■■■■■■ */

	public boolean calcHit() {
		if (_calcType == PC_PC || _calcType == PC_NPC) {
			if (_weaponRange != -1) {
				if (_pc.getLocation().getTileLineDistance(_target.getLocation()) > _weaponRange + 1) {
					_isHit = false;
					return _isHit;
				}
			} else {
				if (!_pc.getLocation().isInScreen(_target.getLocation())) {
					_isHit = false;
					return _isHit;
				}
			}
			if (_weaponType == 20 && _weaponId != 190 && _weaponId != 11011 
					&& _weaponId != 11012 && _weaponId != 11013 && _arrow == null) {
				return _isHit = false; // 화살이 없는 경우는 미스
			} else if (_weaponType == 62 && _sting == null) {
				return _isHit = false; // 스팅이 없는 경우는 미스
			} else if (!CharPosUtil.glanceCheck(_pc, _targetX, _targetY)) {
				return _isHit = false; // 공격자가 플레이어의 경우는 장애물 판정
			} else if (_weaponId == 247 || _weaponId == 248 || _weaponId == 249) {
				return _isHit = false; // 시련의 검B~C 공격 무효
			} else if (_calcType == PC_PC) {
				if (CharPosUtil.getZoneType(_pc) == 1
						|| CharPosUtil.getZoneType(_targetPc) == 1) {
					return _isHit = false;
				}
				return _isHit = calcPcPcHit();
			} else if (_calcType == PC_NPC) {
				return _isHit = calcPcNpcHit();
			}
		} else if (_calcType == NPC_PC) {
			return _isHit = calcNpcPcHit();
		} else if (_calcType == NPC_NPC) {
			return _isHit = calcNpcNpcHit();
		}
		return _isHit;
	}

	// ●●●● 플레이어로부터 플레이어에의 명중 판정 ●●●●
	/*
	 * PC에의 명중률 근거리 = (PC클래스Lv보정＋STR 보정＋무기 보정＋아이템＋마법 보정+베이스보너스)
	 * PC에의 명중률 원거리 = (PC클래스Lv보정＋DEX 보정＋무기 보정＋아이템＋마법 보정+베이스보너스)
	 * 이것으로 산출된 수치는 자신이 최대 명중(95%)을 주는 일을 할 수 있는 상대측 PC의 AC에의한 회피율과 1:1로 대응한다.
	 * 최소 명중율5% 최대 명중율95%
	 */
	private boolean calcPcPcHit() {
		_hitRate = 0;
		if(_weaponType1 == 17){
			_hitRate += 128;
		} else if (_weaponType != 20 && _weaponType != 62) {
			_hitRate += 100 + _weaponAddHit + _pc.getHitup() + strHit[_pc.getAbility().getTotalStr()] 
					+ _pc.getHitupByArmor() + (_weaponEnchant / 2 + 1);
			if (_targetPc.getAC().getAc() >= 0) {
				_hitRate -= (int) ((10 - _targetPc.getAC().getAc()) * 0.66); //0~10계산
			} else if (_targetPc.getAC().getAc() < 0) {
				_hitRate += (int) ((-10 + _targetPc.getAC().getAc()) * 0.66); //-1~-255계산 AC가 마이너스여서 -10을 더해서 계산
			}
		} else {
			_hitRate += 100 +  _weaponAddHit + _pc.getBowHitup() + dexHit[_pc.getAbility().getTotalDex()-1] 
					+ _pc.getBowHitupByArmor() + _pc.getBowHitupByDoll() + (_weaponEnchant / 2 + 1) - _targetPc.getEr(); //ER을 바로 빼줌.
			if (_targetPc.getAC().getAc() >= 0) {
				_hitRate -= (int) ((10 - _targetPc.getAC().getAc()) * 0.33); //근거리는 AC가 1당 0.66 원거리는 AC가 1당 0.33임. ER때문에 이런차이 발생.
			} else if (_targetPc.getAC().getAc() < 0) {
				_hitRate += (int) ((-10 + _targetPc.getAC().getAc()) * 0.33);
			}
		}

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(MIRROR_IMAGE)) {
			_hitRate -= 25;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(UNCANNY_DODGE)) {
			_hitRate -= 25;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FEAR)) {
			_hitRate += 15;
		}

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(
				L1SkillId.ANTA_MAAN) // 지룡의 마안 - 물리 일정확률 회피
				|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
						L1SkillId.BIRTH_MAAN) // 탄생의 마안 - 물리 일정확률 회피
						|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
								L1SkillId.SHAPE_MAAN) // 형상의 마안 - 물리 일정확률 회피
								|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
										L1SkillId.LIFE_MAAN)) { // 생명의 마안 - 물리 일정확률 회피
			_hitRate -= 10;
		}
		
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)) {	
			_hitRate = 0; 
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) { 
			_hitRate = 0; 
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)) {
			_hitRate = 0; 
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH)) { 
			_hitRate = 0; 
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) { 
			_hitRate = 0; 
		}	

		int castle_id = L1CastleLocation.getCastleIdByArea(_targetPc);
		if(_calcType == PC_PC){
			if (castle_id != 0){
			} else {
				if(_targetPc.getClanid() == Config.NewClan1 && !_targetPc.isPinkName()){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_targetPc.getClanid() == Config.NewClan2 && !_targetPc.isPinkName()){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_targetPc.getClanid() == Config.NewClan3 && !_targetPc.isPinkName()){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_targetPc.getClanid() == Config.NewClan4 && !_targetPc.isPinkName()){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_pc.getClanid() == Config.NewClan1){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_pc.getClanid() == Config.NewClan2){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_pc.getClanid() == Config.NewClan3){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_pc.getClanid() == Config.NewClan4){
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_targetPc.getLevel() < Config.MAX_LEVEL){ //신규보호
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					_targetPc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				} else if(_pc.getLevel() < Config.MAX_LEVEL){ //신규보호
					_pc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					_targetPc.sendPackets(new S_ServerMessage(988));  // 988 공격력과 명중률이 떨어집니다.
					return _isHit = false;
				}
			}
		}
		/** 공성 화살 뚫어 방지 **/
		if (_weaponType == 20 || _weaponType == 62){
			if(_pc.getX() == 33636 && _pc.getY() == 32679 && _pc.getMapId() == 4){
				return _isHit = false;
			}
			if(_pc.getX() == 33635 && _pc.getY() == 32680 && _pc.getMapId() == 4){
				return _isHit = false;
			}
			if(_pc.getX() == 33634 && _pc.getY() == 32681 && _pc.getMapId() == 4){
				return _isHit = false;
			}
			if(_pc.getX() == 33633 && _pc.getY() == 32682 && _pc.getMapId() == 4){
				return _isHit = false;
			}
			if(_pc.getX() == 33627 && _pc.getY() == 32676 && _pc.getMapId() == 4){
				return _isHit = false;
			}
			if(_pc.getX() == 33628 && _pc.getY() == 32675 && _pc.getMapId() == 4){
				return _isHit = false;
			}
			if(_pc.getX() == 33629 && _pc.getY() == 32674 && _pc.getMapId() == 4){
				return _isHit = false;
			}		
			if(_pc.getX() == 33630 && _pc.getY() == 32673 && _pc.getMapId() == 4){
				return _isHit = false;
			}
		}
		/** 공성 화살 뚫어 방지 **/

		/** A107 원거리버그 방어 */
		int _jX = _pc.getX() - _targetPc.getX();
		int _jY = _pc.getY() - _targetPc.getY();
		if (_weaponType == 24) { // 창일때
			if ((_jX > 3 || _jX < -3) && (_jY > 3 || _jY < -3)) {
				return _isHit = false;
			}
		} else if (_weaponType == 20 || _weaponType == 62) {// 활일때
			if ((_jX > 15 || _jX < -15) && (_jY > 15 || _jY < -15)) {
				return _isHit = false;
			}
		} else {
			if ((_jX > 2 || _jX < -2) && (_jY > 2 || _jY < -2)) {
				return _isHit = false;
			}
		}
		/** A107 원거리버그 방어 */
		if (_hitRate < 5) { //최소값5% 최대값 95%
			_hitRate = 5;
		} else if (_hitRate > 95) {
			_hitRate = 95;
		}

		int rnd = _random.nextInt(110) + 1;
		return _hitRate >= rnd;
	}

	// ●●●● 플레이어로부터 NPC 에의 명중 판정 ●●●●
	private boolean calcPcNpcHit() {
		_hitRate = 0;
		// NPC에의 명중율
		// =(PC의 Lv＋클래스 보정＋STR 보정＋DEX 보정＋무기 보정＋DAI의 매수/2＋마법 보정)×5－{NPC의 AC×(-5)}
		if(_weaponType1 == 17){
			_hitRate += 128;
		} else if (_weaponType != 20 && _weaponType != 62) {
			_hitRate += 100 + _weaponAddHit + _pc.getHitup() + strHit[_pc.getAbility().getTotalStr()-1] 
					+ _pc.getHitupByArmor() + (_weaponEnchant / 2 + 1);
		} else {
			_hitRate += 100 +  _weaponAddHit + _pc.getBowHitup() + dexHit[_pc.getAbility().getTotalDex()-1] 
					+ _pc.getBowHitupByArmor() + _pc.getBowHitupByDoll() + (_weaponEnchant / 2 + 1); //npc는 ER없음.
		}
		/*
		if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(MIRROR_IMAGE)) { //로봇이 스킬쓰면 필요할거 같아서 넣음. 뺄사람 빼세요. 3가지모두.
			_hitRate -= 25;
		}
		if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(UNCANNY_DODGE)) { 
			_hitRate -= 25;
		}
		 */
		if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(FEAR)) {
			_hitRate += 15;
		}

		if (_targetNpc.getAC().getAc() >= 0) { //npc는 ER이 없기에 AC가 근거리 원거리 동일하게 0.66임
			_hitRate -= (int) ((10 - _targetNpc.getAC().getAc()) * 0.66);
		} else if (_targetNpc.getAC().getAc() < 0) {
			_hitRate += (int) ((-10 + _targetNpc.getAC().getAc()) * 0.66);
		}
		int npcId = _targetNpc.getNpcTemplate().get_npcId();
		if (npcId >= 45912 && npcId <= 45915 && !_pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_HOLY_WATER)) {
			_hitRate = 0;
		}
		if (npcId == 45916 && !_pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_HOLY_MITHRIL_POWDER)) {
			_hitRate = 0;
		}
		if (npcId == 45941 && !_pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_HOLY_WATER_OF_EVA)) {
			_hitRate = 0;
		}
		if (!_pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_BARLOG)//허수아비 때릴 몹
				&& (npcId == 45752 || npcId == 45753)) {
			_hitRate = 0;
		}
		if (npcId == 7000007 || npcId == 7000008
				|| npcId == 7000009 || npcId == 7000010 || npcId == 7000011
				|| npcId == 7000012 || npcId == 7000013 || npcId == 7000014
				|| npcId == 7000015 || npcId == 7000016 || npcId == 7000017
				|| npcId == 7000018 || npcId == 7000019 || npcId == 7000020
				|| npcId == 7000021 || npcId == 7000023
				|| npcId == 7000024 || npcId == 7000025 || npcId == 7000026
				|| npcId == 7000028 || npcId == 7000029 || npcId == 7000030
				|| npcId == 7000031 || npcId == 7000032 || npcId == 7000033
				|| npcId == 7000034 || npcId == 7000035
				|| npcId == 7000036 || npcId == 7000037 || npcId == 7000038
				|| npcId == 7000039 || npcId == 7000040 || npcId == 7000041
				|| npcId == 7000042 || npcId == 7000043 ){
			_hitRate = 0 ;
		}
		if (!_pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_YAHEE)
				&& (npcId == 45675 || npcId == 81082 || npcId == 45625
				|| npcId == 45674 || npcId == 45685	)) {
			_hitRate = 0;
		}
		if (npcId >= 46068 && npcId <= 46091 && _pc.getGfxId().getTempCharGfx() == 6035) {
			_hitRate = 0;
		}
		if (npcId >= 46092 && npcId <= 46106 && _pc.getGfxId().getTempCharGfx() == 6034) {
			_hitRate = 0;
		}

		/** A107 원거리버그 방어 */
		int _jX = _pc.getX() - _targetNpc.getX();
		int _jY = _pc.getY() - _targetNpc.getY();
		if (_weaponType == 24) { // 창일때
			if ((_jX > 3 || _jX < -3) && (_jY > 3 || _jY < -3)) {
				_hitRate = 0;
			}
		} else if (_weaponType == 20 || _weaponType == 62) { // 활일때
			if ((_jX > 15 || _jX < -15) && (_jY > 15 || _jY < -15)) {
				_hitRate = 0;
			}
		} else {
			if ((_jX > 2 || _jX < -2) && (_jY > 2 || _jY < -2)) {
				_hitRate = 0;
			}
		}
		/** A107 원거리버그 방어 */
		int rnd = _random.nextInt(100) + 1;
		return _hitRate >= rnd;
	}

	// ●●●● NPC 로부터 플레이어에의 명중 판정 ●●●●
	private boolean calcNpcPcHit() {
		_hitRate = 0;
		_hitRate += 100 + (_npc.getLevel() / 2) + _npc.getHitup(); //npc 명중률 모름. 펫은 2렙당1 50렙에 25       PC는 기사 70렙 25힘이 30정도임.
		//
		if (_npc instanceof L1PetInstance) {
			_hitRate += -50 + _npc.getLevel(); //펫의 렙에 따른 명중률을 크게하고싶으시면 이거 넣으세요. 펫의 5렙과 50렙의 명중률 차이가 45%만큼 차이남(본섭화와 관계없음)
			_hitRate += ((L1PetInstance) _npc).getHitByWeapon();
		}
		//_hitRate += _npc.getAbility().getTotalStr() / 2; //밸런스 조절용

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(
				L1SkillId.ANTA_MAAN) // 지룡의 마안 - 물리 일정확률 회피
				|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
						L1SkillId.BIRTH_MAAN) // 탄생의 마안 - 물리 일정확률 회피
						|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
								L1SkillId.SHAPE_MAAN) // 형상의 마안 - 물리 일정확률 회피
								|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
										L1SkillId.LIFE_MAAN)) { // 생명의 마안 - 물리 일정확률 회피
			_hitRate -= 10;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(MIRROR_IMAGE)) { //미러없어 넣음.
			_hitRate -= 25;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(UNCANNY_DODGE)) {
			_hitRate -= 25;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FEAR)) {
			_hitRate += 15;
		}

		if (_targetPc.getAC().getAc() >= 0) {
			_hitRate -= (int) ((10 - _targetPc.getAC().getAc()) * 0.66);
		} else if (_targetPc.getAC().getAc() < 0) {
			_hitRate += (int) ((-10 + _targetPc.getAC().getAc()) * 0.66);
		}

		// NPC의 공격 레인지가 10이상의 경우로, 2이상 떨어져 있는 경우활공격으로 간주한다
		if (_npc.getNpcTemplate().get_ranged() >= 10 && _npc.getLocation().getTileLineDistance(new Point(_targetX, _targetY)) >= 3) {
			_hitRate -= _targetPc.getEr();
			if (_targetPc.getAC().getAc() >= 0) { //ER만큼 빼주고 0.66으로 계산했던 값을 0.33만큼 돌려준다.
				_hitRate += (int) ((10 - _targetPc.getAC().getAc()) * 0.33);
			} else if (_targetPc.getAC().getAc() < 0) {
				_hitRate -= (int) ((-10 + _targetPc.getAC().getAc()) * 0.33);
			}
		}   
		if (_hitRate < 5) {
			_hitRate = 5;
		} else if (_hitRate > 95) {
			_hitRate = 95;
		}

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) {
			_hitRate = 0;
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)) {
			_hitRate = 0;
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)) {
			_hitRate = 0;
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH)) {
			_hitRate = 0;
		} else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) {
			_hitRate = 0;
		}
		int rnd = _random.nextInt(100) + 1;
		return _hitRate >= rnd;
	}

	// ●●●● NPC 로부터 NPC 에의 명중 판정 ●●●●
	private boolean calcNpcNpcHit() {
		_hitRate = 0;
		_hitRate += 100 + (_npc.getLevel() / 2) + _npc.getHitup();

		if (_npc instanceof L1PetInstance) {
			_hitRate += -50 + _npc.getLevel(); //펫의 렙에 따른 명중률을 크게하고싶으시면 이거 넣으세요. 펫의 5렙과 50렙의 명중률 차이가 45%만큼 차이남(본섭화는 아님)
			_hitRate += ((L1PetInstance) _npc).getHitByWeapon();
		}

		if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(MIRROR_IMAGE)) { //원래없었지만 넣음.
			_hitRate -= 25;
		}
		if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(UNCANNY_DODGE)) { 
			_hitRate -= 25;
		}
		if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(FEAR)) {
			_hitRate += 15;
		}

		if (_targetNpc.getAC().getAc() >= 0) { //근거리 원거리 같이 적용.
			_hitRate -= (int) ((10 - _targetNpc.getAC().getAc()) * 0.66);
		} else if (_targetNpc.getAC().getAc() < 0) {
			_hitRate += (int) ((-10 + _targetNpc.getAC().getAc()) * 0.66);
		}

		_hitRate -= _targetNpc.getLevel() / 3; //50렙 npc의 AC50일때 50렙npc가 75% 명중 다른 변수가 없다면.

		if (_hitRate < 5) {
			_hitRate = 5;
		} else if (_hitRate > 95) {
			_hitRate = 95;
		}

		int rnd = _random.nextInt(100) + 1;
		return _hitRate >= rnd;
	}

	/* ■■■■■■■■■■■■■■■ 데미지 산출 ■■■■■■■■■■■■■■■ */

	public int calcDamage() {
		switch (_calcType) {
		case PC_PC:
			_damage = calcPcPcDamage();
			break;
		case PC_NPC:
			_damage = calcPcNpcDamage();
			break;
		case NPC_PC:
			_damage = calcNpcPcDamage();
			break;
		case NPC_NPC:
			_damage = calcNpcNpcDamage();
			break;
		default:
			break;
		}
		return _damage;
	}

	// ●●●● 플레이어로부터 플레이어에의 데미지 산출 ●●●●
	public int calcPcPcDamage() {
		int weaponMaxDamage = _weaponSmall;
		int weaponDamage = 0;
		if (_weaponType == 0 || _weaponType == 20 || _weaponType == 62) {
			weaponDamage = 0;
		} else {
			if (_weaponType == 58 && (_random.nextInt(100) + 1) <= _weaponDoubleDmgChance) { // 크로우를 들고 있으면, 크로우의 최대 타격치가 적용된다.
				weaponDamage = weaponMaxDamage;
				_attackType = 2;
			} else if (_pc.getSkillEffectTimerSet().hasSkillEffect(SOUL_OF_FLAME)) { // 소울 어브 프레임을 가지고 있을 경우
				if ((_random.nextInt(100) + 1) <= 20) {
					weaponDamage = (int) ((weaponMaxDamage * 1.2));
					_attackType = 2;
				} else {
					weaponDamage = weaponMaxDamage;
				}
			} else if (_pc.getSkillEffectTimerSet().hasSkillEffect(AVATA)) { // 군주의 아바타 스킬을 가지고 있을 경우
				if ((_random.nextInt(100) + 1) <= 20) {
					weaponDamage = (int) ((weaponMaxDamage * 2));
					_attackType = 4; 
				} else {
					weaponDamage = weaponMaxDamage;
				}
			} else { // 그외 상황
				weaponDamage = _random.nextInt(weaponMaxDamage);
			}
		}

		int weaponTotalDamage = weaponDamage + _weaponAddDmg + _weaponEnchant; // 추타와 인챈트를 더해준다.

		if (_weaponType == 54 &&  _random.nextInt(100) + 1 <= _weaponDoubleDmgChance) { // 이도류
			weaponTotalDamage *= 2;
			_attackType = 4; 
		}

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(DOUBLE_BRAKE) && (_weaponType == 54 || _weaponType == 58)) {
			if (_weaponEnchant >= 10 && _weaponId == 84){
				if ((_random.nextInt(100) + 1) <= 38) {	
					weaponTotalDamage *= 2.2; 
				} // 10 흑왕도 이상 일 경우
			} else if (_weaponId == 86){
				if ((_random.nextInt(100) + 1) <= 38) {	
					weaponTotalDamage *= 2.2;
				} // 붉은 이도류
			} else { 
				if ((_random.nextInt(100) + 1) <= 33) {	
					weaponTotalDamage *= 2;
				} // 그외의 경우
			}
		}
		weaponTotalDamage += calcAttrPcDmg() + calcAttrEnchantPcDmg();
		double dmg = weaponTotalDamage + _statusDamage;

		if (_weaponType == 20 || _weaponType == 62) {
			dmg += _pc.getBowDmgup() + _pc.getBowDmgupByArmor() + _pc.getBowDmgupByDoll();
			if (_weaponType == 20) { // 활
				if (_arrow != null) {
					int add_dmg = _arrow.getItem().getDmgSmall();
					if (add_dmg <= 1) {	add_dmg = 1; }
					dmg += _random.nextInt(add_dmg) + 1;
				} else if (_weaponId == 190) { // 사이하의 활,눈덩이
					dmg += _random.nextInt(8) + 3;
				} else if (_weaponId >= 11011) { // 사이하의 활,눈덩이
					dmg += _random.nextInt(8) + 3;
				} else if (_weaponId >= 11012) { // 사이하의 활,눈덩이
					dmg += _random.nextInt(8) + 3;
				} else if (_weaponId <= 11013) { // 사이하의 활,눈덩이
					dmg += _random.nextInt(8) + 3;
				}
			} else if (_weaponType == 62) { // 암 토토 렛
				int add_dmg = _sting.getItem().getDmgSmall();
				if (add_dmg <= 1) { add_dmg = 1; }
				dmg = dmg + _random.nextInt(add_dmg) + 1;
			}
		} else {
			dmg += _pc.getDmgup() + _pc.getDmgupByArmor();
			if (_weaponType1 == 17) { dmg = L1WeaponSkill.getKiringkuDamage(_pc, _target); } 
			else if (_weaponType1 == 18) { dmg += L1WeaponSkill.getChainSwordDamage(_pc, _target); }
			else if (_weaponType == 0) { dmg = _statusDamage + (_random.nextInt(5) + 4) / 4; }
		}

		for (L1DollInstance doll : _pc.getDollList().values()) {
			if (_weaponType != 20 && _weaponType != 62) {
				dmg += doll.getDamageByDoll();
			}
			doll.attackPoisonDamage(_pc, _targetNpc);
		}
		Object[] targetDollList = _targetPc.getDollList().values().toArray();
		for (Object dollObject : targetDollList) {
			L1DollInstance doll = (L1DollInstance) dollObject;
			dmg -= doll.getDamageReductionByDoll();
		}

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_SPIRIT)) {
			if (_random.nextInt(100) + 1 < 35){
				dmg *= 1.5;
			}
		} else if (_pc.getSkillEffectTimerSet().hasSkillEffect(ELEMENTAL_FIRE) && (_weaponType != 20 || _weaponType != 62)) {
			if (_random.nextInt(100) + 1 < 35){
				dmg *= 1.5;
			}			
		}

		switch (_weaponId) {
		case 2:
		case 200002: // 악운의 검
			dmg = L1WeaponSkill.getDiceDaggerDamage(_pc, _targetPc, weapon);
			break;
		case 13:
		case 44: // 핑거 오브 데스 , 고대 다크엘프의 검
			L1WeaponSkill.getPoisonSword(_pc, _targetNpc);
			break;	
		case 124:
			dmg += L1WeaponSkill.getBaphometStaffDamage(_pc, _target);
			break;
		case 204:
		case 100204:
			L1WeaponSkill.giveFettersEffect(_pc, _target);
			break;
		case 58: // 데스나이트의불검
			dmg += L1WeaponSkill.getDeathKnightSwordDamage(_pc, _target);
			break;
		case 412000: // 뇌신검
			dmg += L1WeaponSkill.getLightningEdgeDamage(_pc, _target);
			break;
		case 412005: // 광품의도끼
		case 412004: // 혹한의 창
			dmg += L1WeaponSkill.getAreaSkillWeaponDamage(_pc, _target, 412005);
			break;
		case 126:
		case 127:
		case 308:
		case 134:
			calcStaffOfMana();
			break;
		case 412002: // 마력의단검
			calcDrainOfMana();
			break;
		case 412003:
		case 413101:
		case 413102:
		case 413104:
		case 413105: // 악마왕 무기류
			L1WeaponSkill.giveArkMageDiseaseEffect(_pc, _target);
			break;
		case 413103:  // 악마왕의 지팡이
			calcStaffOfMana();
			L1WeaponSkill.giveArkMageDiseaseEffect(_pc, _target);
			break;
		case 450013:
		case 450025:
			addEvilTrickAttack();
			break; // 활
		case 450015:
		case 450011:
		case 450023:
			DrainofEvil1();
			break; // 지팡이 키링크
		case 413224: // 새로운 3단 이블리버스 적용
			addEvilTrickAttack();
			break;
		case 415010: case 415011: case 415012: case 415013: // 테베 체이서
			//case 450010: 
		case 450012: case 450014: case 450022: case 450024: // 마족무기 및 신묘무기 이블리버스
		case 413202: case 413205: // 리토템 이블리버스
		case 415015: case 415016: // 쿠쿨칸 체이서
			addChaserAttack();
			break;
		case 450010: // 마족검의 경우 인챈트에 따른 추타가 1씩 더 붙는다.
			addChaserAttack();
			dmg += _weaponEnchant;
			break;
			/********** 마족 무기 및 집행류 무기 인챈 추타 *************/
		case 12: // 바람칼날의 단검
		case 61: // 진명황의 집행검
		case 86: // 붉은 그림자의 이도류
		case 203: // 앨리스 8
		case 294: // 군주의 대검
			dmg += _weaponEnchant;
			break;
			/*************************************************/
		default:
			dmg += L1WeaponSkill.getWeaponSkillDamage(_pc, _target, _weaponId);
			break;
		}

		dmg -= _targetPc.getDamageReductionByArmor();

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_SLASH)) {
			dmg += 7;
			_pc.sendPackets(new S_SkillSound(_targetPc.getId(), 6591));
			Broadcaster.broadcastPacket(_pc, new S_SkillSound(
					_targetPc.getId(), 6591));
			_pc.getSkillEffectTimerSet().killSkillEffectTimer(BURNING_SLASH);
		}

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(SPECIAL_COOKING)) { dmg -= 5; }// 스페셜요리에 의한 데미지 감소
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(REDUCTION_ARMOR)) { // 기사 마버바 리덕션 아머의 의한 데미지 감소
			int targetPcLvl = _targetPc.getLevel();
			if (targetPcLvl < 50) {
				targetPcLvl = 50;
			}
			dmg -= (targetPcLvl - 50) / 5 + 1;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(AVATA)) {  // 군주 특화 마법 [아바타]의 의한 데미지 감소, 리덕션 아머보다 높다
			int targetPcLvl = _targetPc.getLevel();
			if (targetPcLvl < 50) {
				targetPcLvl = 50;
			}
			dmg -= (targetPcLvl - 50) / 5 + 5;
		}

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(DRAGON_SKIN)) { dmg -= 3;	} // 드래곤 스킨의 의한 데미지 감소
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(PATIENCE)) { dmg -= 2; } // 페이션스의 의한 데미지 감소
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FEATHER_BUFF_A)) { dmg -= 3; } // 쪽지 버프 대박의 의한 데미지 감소
		else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FEATHER_BUFF_B)) { dmg -= 2; } // 쪽지 버프 중박의 의한 데미지 감소

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(VALA_MAAN)) { // 화룡의 마안 - 물리 추가 타격 + 2
			dmg += 2; 
		} else if ( _pc.getSkillEffectTimerSet().hasSkillEffect(LIFE_MAAN)) { // 생명의 마안 - 물리 추가 타격 + 2
			dmg += 2; 
		}
		/*
		if (_targetPc.getInventory().checkEquipped(420100)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420101)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420102)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420103)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420104)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420105)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420106)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420107)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420108)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420109)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420110)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420111)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420112)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420103)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420104)) { dmg -= dmg * 0.05; }//데미지 
		else if (_targetPc.getInventory().checkEquipped(420105)) { dmg -= dmg * 0.05; }//데미지
		 */ 
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(IllUSION_AVATAR)) { dmg += dmg / 5; }  // 일루션 아바타의 의한 데미지 증가
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(IMMUNE_TO_HARM)) { dmg /= 1.4; }  // 이뮨 투함의 의한 데미지 감소
		if (dmg <= 0) {
			_isHit = false;
			_drainHp = 0;
		}

		/////////////////////////////////////////////////////////		 #####좀비모드(시즌)PC_PC by-Season
		/*
		if (_pc.getMapId() == 34 ) {
			if (_pc.getZombieMod() == _targetPc.getZombieMod()) {
				dmg = 0;
			} else if (_pc.getZombieMod() == 2 && _targetPc.getZombieMod() == 1 && _weaponId == 310) {
				int polylist[] = { 5484, 5412 };
				int rndpoly = _random.nextInt(polylist.length);
				int polyId = polylist[rndpoly];
				L1PolyMorph.doPoly(_targetPc, polyId, -1, L1PolyMorph.MORPH_BY_ITEMMAGIC); // 시즌
				_targetPc.setZombieMod(2);
				// _targetPc.setBaseHp(_targetPc.getMaxHp());
				////////////////////////////////////////좀비가 3킬할시 피통2배되며 숙주로진화 by-Kingdom
				if(_pc.getKillPoint() >= 3){
					_pc.setKillPoint(0);
					_pc.setMaxHp(_pc.getMaxHp()*2);
					_pc.setCurrentHp(_pc.getCurrentHp()*2);
					_pc.sendPackets(new S_SystemMessage("\\fY3킬을 하셨으므로 숙주로 진화합니다."));
				}
				////////////////////////////////////////좀비가 3킬할시 피통2배되며 숙주로진화 by-Kingdom
				////////////////////////////////////////좀비의 모습에따라 피통이 다르게 by-Kingdom
				if(polyId ==5484){
					_targetPc.setMaxHp(1500);
					_targetPc.setCurrentHp(1500);
				}else if(polyId == 5412){
					_targetPc.setMaxHp(600);
					_targetPc.setCurrentHp(600);
				}else{
					_targetPc.setMaxHp(1000);
					_targetPc.setCurrentHp(1000);
				}
				////////////////////////////////////////좀비의 모습에따라 피통이 다르게 by-Kingdom
				dmg = 0;
				////////////////////////////////////////좀비로 변하면 인간용 활과 화살이 삭제되며 좀비무기지급 by-Kingdom
				int 화살갯수 = _pc.getInventory().countItems(40745);
				_targetPc.getInventory().consumeItem(311, 1);
				_targetPc.getInventory().consumeItem(40745, 화살갯수);
				_targetPc.getInventory().storeItem(310, 1);
				////////////////////////////////////////좀비로 변하면 인간용 활과 화살이 삭제되며 좀비무기지급 by-Kingdom
				L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("\\fY" + _targetPc.getName()+ "님이 좀비가 되셨습니다.")); 
				if (_weaponId == 311) {
					if(_targetPc.getZombieMod() == 2){
						if (_pc.getInventory().checkItem(40745)) {
							_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 66,	_targetX, _targetY, _isHit));
							_pc.broadcastPacket(new S_UseArrowSkill(_pc, _targetId,	66, _targetX, _targetY, _isHit));
							_pc.getInventory().removeItem(40745, 1);
							int ss = _random.nextInt(15) + 1;
							if (ss < _random.nextInt(100) + 1) {
								Broadcaster.broadcastPacketExceptTargetSight(_target, new S_DoActionGFX(_targetId,ActionCodes.ACTION_Damage), _pc);
								dmg = 50;
							}else{
								dmg = 15;
							}
						}
					}
				}
			}
		}
		 */
		/////////////////////////////////////////////////////////좀비모드 by-Season
		return (int) dmg;
	}

	// ●●●● 플레이어로부터 NPC 에의 데미지 산출 ●●●●
	private int calcPcNpcDamage() {
		int weaponMaxDamage = 0;
		int weaponDamage = 0;
		int doubleChance = _random.nextInt(100) + 1;
		if (_targetNpc.getNpcTemplate().get_size().equalsIgnoreCase("small") && _weaponSmall > 0) {
			weaponMaxDamage = _weaponSmall;
		} else if (_targetNpc.getNpcTemplate().get_size().equalsIgnoreCase("large") && _weaponLarge > 0) {
			weaponMaxDamage = _weaponLarge;
		}
		/**
		 * [weapon]
		 * sword:4, dagger:46, tohandsword:50, bow:20, blunt:11, spear:24, staff:40,
		 * throwingknife:2922, arrow:66, gauntlet:62, claw:58, edoryu:54,
		 * singlebow:20, singlespear:24, tohandblunt:11, tohandstaff:40, kiringku:58, chainsword:24
		 */
		switch(_weaponType){  // weapon
		case 0: weaponDamage = 0; break; // 맨손
		case 4: // 한손검
		case 46: // 단검
			if (_pc.getSkillEffectTimerSet().hasSkillEffect(SOUL_OF_FLAME)) {  // 요정 소울 어브 프레임
				if (doubleChance <= 20) {
					weaponDamage = (int) ((weaponMaxDamage * 1.2));
					_attackType = 4; 
				} else {
					weaponDamage = weaponMaxDamage;
				}
			} else if (_pc.getSkillEffectTimerSet().hasSkillEffect(AVATA)) {  // 군주 아바타가 있다면
				if (doubleChance <= 20) {
					weaponDamage = (int) ((weaponMaxDamage * 2));
					_attackType = 4; 
				} else {
					weaponDamage = weaponMaxDamage;
				}
			} else {
				weaponDamage = CommonUtil.random(weaponMaxDamage);
			}
			break;
		case 58: // 키링크와 크로우
			if (_weaponType1 == 11){ // 크로우
				if ((_random.nextInt(100) + 1) <= _weaponDoubleDmgChance) { // 크로우
					weaponDamage = weaponMaxDamage + _weaponAddDmg + _weaponEnchant;
					_attackType = 2;
				} else {
					weaponDamage = CommonUtil.random(weaponMaxDamage) + _weaponAddDmg + _weaponEnchant;
				}
				if (_pc.getSkillEffectTimerSet().hasSkillEffect(DOUBLE_BRAKE) && (_random.nextInt(100) + 1) <= 30) {
					weaponDamage *= 2;	
				}
			} else if (_weaponType1 == 17){ // 키링크
				weaponDamage = CommonUtil.random(weaponMaxDamage) + _weaponAddDmg + _weaponEnchant;
				weaponDamage += L1WeaponSkill.getKiringkuDamage(_pc, _target);
			}  
			break;
		case 54: // 이도류
			weaponDamage = _random.nextInt(weaponMaxDamage) + _weaponAddDmg + _weaponEnchant;
			if (doubleChance <= _weaponDoubleDmgChance) { // 이도류
				weaponDamage *= 2;
				_attackType = 4; 
			}
			if (_pc.getSkillEffectTimerSet().hasSkillEffect(DOUBLE_BRAKE) && (_random.nextInt(100) + 1) <= 30) {
				weaponDamage *= 2;	
			}
			break;
		case 24: // 체인소드
			weaponDamage = CommonUtil.random(weaponMaxDamage);
			weaponDamage += L1WeaponSkill.getChainSwordDamage(_pc, _target);
			break;
		case 20: // 활
			if (_arrow != null) {
				int add_dmg = 0;
				if (_targetNpc.getNpcTemplate().get_size().equalsIgnoreCase("large")) {
					add_dmg = _arrow.getItem().getDmgLarge();
				} else {
					add_dmg = _arrow.getItem().getDmgSmall();
				}
				if (add_dmg == 0) {
					add_dmg = 1;
				}
				if (_targetNpc.getNpcTemplate().is_hard()) {
					add_dmg /= 2;
				}
				weaponDamage += _random.nextInt(add_dmg);
			} else if (_weaponId == 190 || (_weaponId >= 11011 && _weaponId <= 11013)) {// 사이하의 활
				weaponDamage += _random.nextInt(15);
			}
			break;
		case 62: // 건틀렛
			int add_dmg = 0;
			if (_targetNpc.getNpcTemplate().get_size().equalsIgnoreCase("large")) {
				add_dmg = _sting.getItem().getDmgLarge();
			} else {
				add_dmg = _sting.getItem().getDmgSmall();
			}
			if (add_dmg == 0) {
				add_dmg = 1;
			}
			weaponDamage += _random.nextInt(add_dmg);
			break;
		default:
			weaponDamage = CommonUtil.random(weaponMaxDamage);
			break;
		}

		if (_weaponType != 0 ||_weaponType != 54 || _weaponType != 58){
			weaponDamage += _weaponAddDmg + _weaponEnchant;
		}

		int weaponTotalDamage = 0;

		weaponTotalDamage += weaponDamage;
		weaponTotalDamage += calcMaterialBlessDmg();
		weaponTotalDamage += calcAttrNpcDmg(); // 속성인챈
		weaponTotalDamage += calcAttrEnchantNpcDmg(); //속성버프

		double dmg = weaponTotalDamage + _statusDamage;
		switch (_weaponType) {
		case 0:	dmg += (_random.nextInt(5) + 4) / 4; return (int) dmg; //break; // 맨손
		case 20:
		case 62:
			dmg += _pc.getBowDmgup();
			dmg += _pc.getBowDmgupByArmor();
			dmg += _pc.getBowDmgupByDoll();
			break;
		default:
			dmg += _pc.getDmgup();
			dmg += _pc.getDmgupByArmor();
			break;
		}

		for (L1DollInstance doll : _pc.getDollList().values()) {
			if (_weaponType != 20 && _weaponType != 62) {
				dmg += doll.getDamageByDoll();
			}
			doll.attackPoisonDamage(_pc, _targetNpc);
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(VALA_MAAN) // 화룡의 마안 -
				|| _pc.getSkillEffectTimerSet().hasSkillEffect(LIFE_MAAN)) { // 생명의
			dmg += 2;
		}

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_SLASH)) {
			dmg += 7;
			_pc.sendPackets(new S_SkillSound(_targetNpc.getId(), 6591));
			Broadcaster.broadcastPacket(_pc, new S_SkillSound(_targetNpc.getId(), 6591));
			_pc.getSkillEffectTimerSet().killSkillEffectTimer(BURNING_SLASH);
		}

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_SPIRIT)) {
			if (_random.nextInt(100) + 1 < 30){
				dmg *= 1.5;
			}
		} else if (_pc.getSkillEffectTimerSet().hasSkillEffect(ELEMENTAL_FIRE) && (_weaponType != 20 || _weaponType != 62)) {
			if (_random.nextInt(100) + 1 < 30){
				dmg *= 1.5;
			}			
		}

		// 마법검류에 의한 데미지 산출
		dmg += calcMagicalWeaponDmg();

		// 플레이어로부터 애완동물, 사몬에 공격
		boolean isNowWar = false;
		int castleId = L1CastleLocation.getCastleIdByArea(_targetNpc);
		if (castleId > 0) {
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		if (!isNowWar) {
			if (_targetNpc instanceof L1PetInstance) {
				dmg /= 10;
			}
			if (_targetNpc instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) _targetNpc;
				if (summon.isExsistMaster()) {
					dmg /= 10;
				}
			}
		}

		//AC에 의한 데미지 감소
		if (_targetNpc.getAC().getAc() >= 0 ) {
			if (_targetNpc instanceof L1PetInstance) {
				dmg -= dmg * (10 - _targetNpc.getAC().getAc()) / 264;
			} else {
				dmg -= dmg * (10 - _targetNpc.getAC().getAc()) / 530; 
			}
		} else if (_targetNpc.getAC().getAc() < 0 ){
			if (_targetNpc instanceof L1PetInstance) {
				dmg += dmg * (-10 + _targetNpc.getAC().getAc()) / 264;
			} else {
				dmg += dmg * (-10 + _targetNpc.getAC().getAc()) / 530;
			} 
		}
		if (dmg <= 0) {
			_isHit = false;
			_drainHp = 0;
		}
		return (int) dmg;
	}

	// ●●●● NPC 로부터 플레이어에의 데미지 산출 ●●●●
	private int calcNpcPcDamage() {
		int lvl = _npc.getLevel();
		double dmg = 0D;
		int str = _npc.getAbility().getTotalStr();

		if (_npc instanceof L1PetInstance) { //마법펫 물리펫 str 10을 기준으로 구분함.
			if (_npc.getAbility().getStr() < 10) { //마법펫은 8렙당 1추타임.
				if(_npc.getAbility().getCon() <= 10) {
					dmg = (lvl / 8)  * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				} else if (_npc.getAbility().getCon() > 10) { //하이 마법펫은 4렙당 2추타임.
					dmg = (lvl / 8) * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				}
			} else if (_npc.getAbility().getStr() >= 10) { //물리펫은 4렙당 1추타임.
				if(_npc.getAbility().getCon() <= 10) {
					dmg = (lvl / 4)  * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				} else if (_npc.getAbility().getCon() > 10) { //하이 물리펫은 4렙당 2추타임.
					dmg = (lvl / 4) * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				}
			}
		} else {
			dmg = _random.nextInt(lvl) + _npc.getAbility().getTotalStr() / 2 + 1;
		}

		dmg += _npc.getDmgup();

		if (isUndeadDamage()) {	dmg *= 1.1;	}

		dmg = dmg * getLeverage() / 10;

		dmg -= (_targetPc.getDamageReductionByArmor()); // 방어용 기구에 의한 데미지 경감

		Object[] targetDollList = _targetPc.getDollList().values().toArray();
		for (Object dollObject : targetDollList) {
			L1DollInstance doll = (L1DollInstance) dollObject;
			dmg -= doll.getDamageReductionByDoll();
		}

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(SPECIAL_COOKING)) { // 스페셜요리에
			dmg -= 5;
		}

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(REDUCTION_ARMOR)) {
			int targetPcLvl = _targetPc.getLevel();
			if (targetPcLvl < 50) {
				targetPcLvl = 50;
			}
			dmg -= (targetPcLvl - 50) / 5 + 1;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(AVATA)) {
			int targetPcLvl = _targetPc.getLevel();
			if (targetPcLvl < 50) {
				targetPcLvl = 50;
			}
			dmg -= (targetPcLvl - 50) / 5 + 5;
		}
		// 애완동물, 사몬으로부터 플레이어에 공격
		boolean isNowWar = false;
		int castleId = L1CastleLocation.getCastleIdByArea(_targetPc);
		if (castleId > 0) {
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		if (!isNowWar) {
			if (_npc instanceof L1PetInstance) {
				dmg /= 10;
			}
			if (_npc instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) _npc;
				if (summon.isExsistMaster()) {
					dmg /= 10;
				}
			}
		}

		//AC에 의한 데미지 감소
		if (_targetPc.getAC().getAc() >= 0 ) {
			dmg -= dmg * (10 - _targetPc.getAC().getAc()) / 400;
		} else if (_targetPc.getAC().getAc() < 0 ) {
			dmg += dmg * (-10 + _targetPc.getAC().getAc()) / 400;
		}  

		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(PATIENCE)) {
			dmg -= 2;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(DRAGON_SKIN)) {
			dmg -= 3;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FEATHER_BUFF_A)) {
			dmg -= 3;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FEATHER_BUFF_B)) {
			dmg -= 2;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(IllUSION_AVATAR)) {
			dmg += dmg / 5;
		}
		if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(IMMUNE_TO_HARM)) {
			dmg /= 1.6;
		}
		if (_npc.isWeaponBreaked()) { // NPC가 웨폰브레이크중.
			dmg /= 2;
		}
		addNpcPoisonAttack(_npc, _targetPc);

		if (_npc instanceof L1PetInstance || _npc instanceof L1SummonInstance) {
			if (CharPosUtil.getZoneType(_targetPc) == 1) {
				_isHit = false;
			}
		}

		if (dmg <= 0) {
			_isHit = false;
		}

		return (int) dmg;
	}

	// ●●●● NPC 로부터 NPC 에의 데미지 산출 ●●●●
	private int calcNpcNpcDamage() {
		int lvl = _npc.getLevel();
		double dmg = 0;
		int str = _npc.getAbility().getTotalStr();
		if (_npc instanceof L1PetInstance) { //마법펫 물리펫 str 10을 기준으로 구분함.
			if (_npc.getAbility().getStr() < 10) { //마법펫은 8렙당 1추타임.
				if(_npc.getAbility().getCon() <= 10) {
					dmg = (lvl / 8)  * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				} else if (_npc.getAbility().getCon() > 10) { //하이 마법펫은 8렙당 2추타임
					dmg = (lvl / 8) * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				}
			} else if (_npc.getAbility().getStr() >= 10) { //물리펫은 4렙당 1추타임.
				if(_npc.getAbility().getCon() <= 10) {
					dmg = (lvl / 4)  * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				} else if (_npc.getAbility().getCon() > 10) { //하이 물리펫은 4렙당 2추타임.
					dmg = (lvl / 4) * 2 + _random.nextInt(str) * 2;
					dmg += ((L1PetInstance) _npc).getDamageByWeapon();
				}
			}
		} else {
			dmg = _random.nextInt(lvl) + _npc.getAbility().getTotalStr() + (lvl * lvl / 100);
		}

		if (isUndeadDamage()) {
			dmg *= 1.1;
		}

		dmg = dmg * getLeverage() / 10;

		//dmg -= calcNpcDamageReduction();

		if (_npc.isWeaponBreaked()) { // NPC가 웨폰브레이크중.
			dmg /= 2;
		}
		if (_targetNpc.getNpcId() == 45640) {
			dmg /= 2;
		}
		addNpcPoisonAttack(_npc, _targetNpc);

		//AC에 의한 데미지 감소 본섭화
		if (_targetNpc.getAC().getAc() >= 0 ) {
			if (_targetNpc instanceof L1PetInstance) {
				dmg -= dmg * (10 - _targetNpc.getAC().getAc()) / 132;
			} else {
				dmg -= dmg * (10 - _targetNpc.getAC().getAc()) / 265; 
			}
		} else if (_targetNpc.getAC().getAc() < 0 ){
			if (_targetNpc instanceof L1PetInstance) {
				dmg += dmg * (-10 + _targetNpc.getAC().getAc()) / 132;
			} else {
				dmg += dmg * (-10 + _targetNpc.getAC().getAc()) / 265;
			} 
		} 

		if (dmg <= 0) {
			_isHit = false;
		}
		return (int) dmg;
	}

	private double calcMagicalWeaponDmg() {
		double dmg = 0;
		try {
			switch (_weaponId) {
			case 13:
			case 44: // 핑거 오브 데스 , 고대 다크엘프의 검
				L1WeaponSkill.getPoisonSword(_pc, _targetNpc);
				break;	
			case 124:
				dmg += L1WeaponSkill.getBaphometStaffDamage(_pc, _target);
				break;
			case 204:
			case 100204:
				L1WeaponSkill.giveFettersEffect(_pc, _target);
				break;
			case 58: // 데스나이트의불검
				dmg += L1WeaponSkill.getDeathKnightSwordDamage(_pc, _target);
				break;
			case 412000: // 뇌신검
				dmg += L1WeaponSkill.getLightningEdgeDamage(_pc, _target);
				break;
			case 412005: // 광품의도끼
			case 412004: // 혹한의 창
				dmg += L1WeaponSkill.getAreaSkillWeaponDamage(_pc, _target, 412005);
				break;
			case 126:
			case 127:
			case 308:
			case 134:
				calcStaffOfMana();
				break;
			case 412002: // 마력의단검
				calcDrainOfMana();
				break;
			case 412003:
			case 413101:
			case 413102:
			case 413104:
			case 413105: // 악마왕 무기류
				L1WeaponSkill.giveArkMageDiseaseEffect(_pc, _target);
				break;
			case 413103:  // 악마왕의 지팡이
				calcStaffOfMana();
				L1WeaponSkill.giveArkMageDiseaseEffect(_pc, _target);
				break;
			case 450013:
			case 450025:
				addEvilTrickAttack();
				break; // 활
			case 450015:
			case 450011:
			case 450023:
				DrainofEvil1();
				break; // 지팡이 키링크
			case 413224: // 새로운 3단 이블리버스 적용
				addEvilTrickAttack();
				break;
			case 415010: case 415011: case 415012: case 415013: // 테베 체이서
				//case 450010: 
			case 450012: case 450014: case 450022: case 450024: // 마족무기 및 신묘무기 이블리버스
			case 413202: case 413205: // 리토템 이블리버스
			case 415015: case 415016: // 쿠쿨칸 체이서
				addChaserAttack();
				break;
			case 450010: // 마족검
				addChaserAttack();
				break;
			default:
				dmg += L1WeaponSkill.getWeaponSkillDamage(_pc, _target, _weaponId);
				break;
			}
		} catch (Exception e) { System.out.println (e);	}
		return dmg;
	}

	//속성버프와 속성인챈의 본섭화 더 정확하겐 npc속성 더 세분화해야지만 나비켓 노가다라서 이정도까지만 할께요.
	// ●●●● PC에의 속성버프 데미지 산출 ●●●●
	private int calcAttrPcDmg() {
		int dmg = 0;
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(FIRE_WEAPON)) { //속성저항에 따라 데미지 변동 -100일때 2배 100일때 0
			if (_targetPc.getResistance().getFire() != 0) {
				dmg -= (int) (_targetPc.getResistance().getFire() / 25);
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(FIRE_BLESS)) {
			if (_targetPc.getResistance().getFire() != 0) {
				dmg -= (int) (_targetPc.getResistance().getFire() / 25);
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_WEAPON)) {
			if (_targetPc.getResistance().getFire() != 0) {
				dmg -= (int) (_targetPc.getResistance().getFire() / 16);
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(STORM_SHOT)) {
			if (_targetPc.getResistance().getWind() != 0) {
				dmg -= (int) (_targetPc.getResistance().getWind() / 20);
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(STORM_EYE)) {
			if (_targetPc.getResistance().getWind() != 0) {
				dmg -= (int) (_targetPc.getResistance().getWind() / 33);
			}
		}
		return dmg;
	}
	// ●●●● NPC에의 속성버프 데미지 산출 ●●●●
	private int calcAttrNpcDmg() {
		int dmg = 0;
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(FIRE_WEAPON)) { //취약속성이 불이면 데미지 배로준다.
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 4) {
				dmg += 4;
			}
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 2) { //취약속성이 물이면 몹의속성은 불이므로 불속성 데미지 0
				dmg -= 4;
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(FIRE_BLESS)) {
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 4) {
				dmg += 4;
			}
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 2) {
				dmg -= 4;
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_WEAPON)) {
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 4) {
				dmg += 6;
			}
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 2) {
				dmg -= 6;
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(STORM_SHOT)) { //취약속성이 바람이면 2배
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 8) {
				dmg += 5;
			}
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 4) { //취약속성이 불이면 몹은 바람속성이므로 바람속성 데미지 0
				dmg -= 5;
			}
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(STORM_EYE)) {
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 8) {
				dmg += 3;
			}
			if (_targetNpc.getNpcTemplate().get_weakAttr() == 4) {
				dmg -= 3;
			}
		}
		return dmg;
	} 
	// ●●●● PC에의 무기 속성 데미지 산출 ●●●●
	private int calcAttrEnchantPcDmg() {
		int dmg = 0;
		switch(_weaponAttrEnchantLevel){
		case 1: case 4: case 7: case 10: dmg = 1; break;
		case 2: case 5: case 8: case 11: dmg = 3; break;
		case 3: case 6: case 9: case 12: dmg = 5; break;
		default: dmg = 0; break;
		}
		if (_targetPc.getResistance().getEarth() != 0) { //땅속성
			if (_weaponAttrEnchantLevel == 10 || _weaponAttrEnchantLevel == 11 || _weaponAttrEnchantLevel == 12) {
				dmg -= (int) (_targetPc.getResistance().getEarth() * dmg / 100);
			}
		} 
		if (_targetPc.getResistance().getWater() != 0) { //물속성
			if (_weaponAttrEnchantLevel == 4 || _weaponAttrEnchantLevel == 5 || _weaponAttrEnchantLevel == 6) {
				dmg -= (int) (_targetPc.getResistance().getWater() * dmg / 100);
			}
		} 
		if (_targetPc.getResistance().getFire() != 0) { //불속성
			if (_weaponAttrEnchantLevel == 1 || _weaponAttrEnchantLevel == 2 || _weaponAttrEnchantLevel == 3) {
				dmg -= (int) (_targetPc.getResistance().getFire() * dmg / 100);
			}
		} 
		if (_targetPc.getResistance().getWind() != 0) { //바람속성
			if (_weaponAttrEnchantLevel == 7 || _weaponAttrEnchantLevel == 8 || _weaponAttrEnchantLevel == 9) {
				dmg -= (int) (_targetPc.getResistance().getWind() * dmg / 100);
			}
		}
		return dmg;
	}
	// ●●●● NPC에의 무기 속성 데미지 산출 ●●●●
	private int calcAttrEnchantNpcDmg() {
		int dmg = 0;
		switch(_weaponAttrEnchantLevel){
		case 1: case 4: case 7: case 10: dmg = 1; break;
		case 2: case 5: case 8: case 11: dmg = 3; break;
		case 3: case 6: case 9: case 12: dmg = 5; break;
		case 13: dmg = 5; break; //특화속성은 모든 속성에 상관없이 일정 데미지
		case 14: dmg = 6; break;
		case 15: dmg = 7; break;
		default: dmg = 0; break;
		}
		//지수화풍의 상성은 (물은 땅에취약) (불은 물에취약) (바람은 불에취약) (땅은 바람에취약) 같은속성엔 데미지 0 
		if (_targetNpc.getNpcTemplate().get_weakAttr() == 1) { //취약속성 땅 일때 땅엔 2배 물속성엔 0
			if (_weaponAttrEnchantLevel == 10 || _weaponAttrEnchantLevel == 11 || _weaponAttrEnchantLevel == 12) {
				dmg *= 2;
			} else if (_weaponAttrEnchantLevel == 4 || _weaponAttrEnchantLevel == 5 || _weaponAttrEnchantLevel == 6) {
				dmg = 0;
			}
		} 
		if (_targetNpc.getNpcTemplate().get_weakAttr() == 2) { //취약속성 물 일때 물속성은 2배 불속성엔 0
			if (_weaponAttrEnchantLevel == 4 || _weaponAttrEnchantLevel == 5 || _weaponAttrEnchantLevel == 6) {
				dmg *= 2;
			} else if (_weaponAttrEnchantLevel == 1 || _weaponAttrEnchantLevel == 2 || _weaponAttrEnchantLevel == 3) {
				dmg = 0;
			}
		} 
		if (_targetNpc.getNpcTemplate().get_weakAttr() == 4) { //취약속성 불 일땐 불엔 2배 바람속성엔 0
			if (_weaponAttrEnchantLevel == 1 || _weaponAttrEnchantLevel == 2 || _weaponAttrEnchantLevel == 3) {
				dmg *= 2;
			} else if (_weaponAttrEnchantLevel == 7 || _weaponAttrEnchantLevel == 8 || _weaponAttrEnchantLevel == 9) {
				dmg = 0;
			}
		} 
		if (_targetNpc.getNpcTemplate().get_weakAttr() == 8) { //취약속성 바람 일땐 바람엔 2배 땅속성엔 0
			if (_weaponAttrEnchantLevel == 7 || _weaponAttrEnchantLevel == 8 || _weaponAttrEnchantLevel == 9) {
				dmg *= 2;
			} else if (_weaponAttrEnchantLevel == 10 || _weaponAttrEnchantLevel == 11 || _weaponAttrEnchantLevel == 12) {
				dmg = 0;
			}
		}
		return dmg;
	}

	// ●●●● 플레이어의 데미지 강화 마법 ●●●●
	/*
	private double calcBuffDamage(double dmg) {
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_SPIRIT)
				|| (_pc.getSkillEffectTimerSet().hasSkillEffect(ELEMENTAL_FIRE)
						&& _weaponType != 20 && _weaponType != 62 && _weaponType1 != 17)) {
			if ((_random.nextInt(100) + 1) <= 33) {
				double tempDmg = dmg;
				if (_pc.getSkillEffectTimerSet().hasSkillEffect(FIRE_WEAPON)) {
					tempDmg -= 4;
				}
				if (_pc.getSkillEffectTimerSet().hasSkillEffect(FIRE_BLESS)) {
					tempDmg -= 4;
				}
				if (_pc.getSkillEffectTimerSet().hasSkillEffect(BURNING_WEAPON)) {
					tempDmg -= 6;
				}
				if (_pc.getSkillEffectTimerSet().hasSkillEffect(BERSERKERS)) {
					tempDmg -= 5;
				}
				double diffDmg = dmg - tempDmg;
				dmg = tempDmg * 1.5 + diffDmg;
			}
		}

		return dmg;
	}
	 */

	// ●●●● 플레이어의 AC에 의한 데미지 경감 ●●●●
	/*
	private int calcPcDefense() {
		int ac = Math.max(0, 10 - _targetPc.getAC().getAc());
		int acDefMax = _targetPc.getClassFeature().getAcDefenseMax(ac);
		return _random.nextInt(acDefMax + 1);
	}
	 */

	///////////////////////마족무기////////////////////////
	public int getEbHP(L1PcInstance pc, L1Character target, int effect, int enchant) {  // 마족무기 
		int dmg = 0; 
		int in = (enchant); 
		int chance = _random.nextInt(100)+1; 
		dmg += in; 
		if (chance <= (in) + 7) { 
			int pcInt = pc.getAbility().getTotalInt(); 
			_drainHp = _random.nextInt(9) + 10 + (pcInt/2) + enchant ; //피빨 
			pc.sendPackets(new S_SkillSound(target.getId(), effect)); 
			Broadcaster.broadcastPacket(pc, new S_SkillSound(target.getId(), effect)); 
		} 
		return dmg; 
	}

	public void addEvilTrickAttack() {
		int mr = 0;
		double probability = 0;
		int weaponEnchantProbability = 0;
		if (_calcType == PC_PC) {
			mr = _targetPc.getResistance().getMr() - 2 * _pc.getBaseMagicHitUp();
		} else if (_calcType == PC_NPC) {
			mr = _targetNpc.getResistance().getMr() - 2 * _pc.getBaseMagicHitUp();
		}
		if (_weaponEnchant >= 7){
			weaponEnchantProbability = _weaponEnchant - 6;
		}
		probability = 3.0 + _pc.getAbility().getDex() * 0.10 + weaponEnchantProbability;
		probability -= (mr / 10) * 0.1;
		if (probability < 3.0) {
			probability = 3.0;
		}
		int rnd = _random.nextInt(100) + 1;
		if (_pc.isGm() || _pc.getAccessLevel() == 300){
			_pc.sendPackets(new S_SystemMessage("이블트릭의 확률은 [" + probability + "% / Random 값은 " + rnd + "] 입니다"));
		}
		if (probability > rnd) {
			L1EvilTrick eviltrick = new L1EvilTrick(_pc, _target);
			eviltrick.begin();
		}
	}

	// ■■■■ 테베 체이서 공격  ■■■■
	public void addChaserAttack() {
		int mr = 0;
		double probability = 0;
		int weaponEnchantProbability = 0;
		if (_calcType == PC_PC) {
			mr = _targetPc.getResistance().getMr() - 2 * _pc.getBaseMagicHitUp();
		} else if (_calcType == PC_NPC) {
			mr = _targetNpc.getResistance().getMr() - 2 * _pc.getBaseMagicHitUp();
		}
		switch(_weaponId){
		case 415010: case 415011: case 415012: case 415013: // 테베 체이서
			if (_weaponEnchant >= 9){
				weaponEnchantProbability = _weaponEnchant - 8;
			}
			probability = 3.0 + _pc.getAbility().getTrueSp() * 0.18 + weaponEnchantProbability;
			break;
		case 450010: case 450012: case 450014:
		case 450022: case 450024:
		case 413202: case 413205: // 이블리버스
			if (_weaponEnchant >= 7){
				weaponEnchantProbability = _weaponEnchant - 6;
			}
			probability = 3.0 + _pc.getAbility().getSp() * 0.14 + weaponEnchantProbability;
			break;
		case 415015: case 415016:
			probability = 3.0 + _pc.getAbility().getTrueSp() * 0.18;
			break;
		}

		probability -= (mr / 10) * 0.1;
		if (probability < 3.0) {
			probability = 3.0;
		}

		int rnd = CommonUtil.random(100) + 1;
		if (probability > rnd) {
			if (_calcType == PC_PC){
				if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)) {	return; }
				else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)) { return; }			
				else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)) { return; }			
				else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH)) { return; }	
				else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) { return; }	
				else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(MOB_BASILL)) { return; }			
				else if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(MOB_COCA)) { return; }

				switch(_weaponId){
				case 415010: case 415011: case 415012: case 415013: // 테베 체이서
				{
					L1Chaser chaser = new L1Chaser(_pc, _target, 6985);
					chaser.begin();
					break;
				}
				case 450010: case 450012: case 450014:
				case 450022: case 450024:
				case 413202: case 413205: // 이블리버스
				{
					L1Chaser chaser = new L1Chaser(_pc, _target, 8150);
					chaser.begin();
					break;
				}
				case 415015: case 415016:
				{
					L1Chaser chaser = new L1Chaser(_pc, _target, 7179);
					chaser.begin();
					break;
				}
				}
			} else if (_calcType == PC_NPC) {
				if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
						|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)
						|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BREATH)
						|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) { // 코카얼리기데미지0
					return;
				} 

				switch(_weaponId){
				case 415010: case 415011: case 415012: case 415013: // 테베 체이서
				{
					L1Chaser chaser = new L1Chaser(_pc, _target, 6985);
					chaser.begin();
					break;
				}
				case 450010: case 450012: case 450014:
				case 450022: case 450024:
				case 413202: case 413205: // 이블리버스
				{
					L1Chaser chaser = new L1Chaser(_pc, _target, 8150);
					chaser.begin();
					break;
				}
				case 415015: case 415016:
				{
					L1Chaser chaser = new L1Chaser(_pc, _target, 7179);
					chaser.begin();
					break;
				}
				}
			}
		}
	}

	// ●●●● 무기의 재질과 축복에 의한 추가 데미지 산출 ●●●●
	private int calcMaterialBlessDmg() {
		int damage = 0;
		int undead = _targetNpc.getNpcTemplate().get_undead();
		if (_pc.getWeapon() != null) { //<--요거 추가하세요
			if ((_weaponMaterial == 14 || _weaponMaterial == 17 || _weaponMaterial == 22 || weapon.getHolyDmgByMagic() != 0)
					&& (undead == 1 || undead == 3 || undead == 5)) { // 은·미스릴·오리하르콘,
				// 한편, 안
				// 데드계·안 데드계
				// 보스
				damage += _random.nextInt(20) + 1;
			}
			if ((_weaponMaterial == 17 || _weaponMaterial == 22 || weapon.getHolyDmgByMagic() != 0) && undead == 2) {
				damage += _random.nextInt(3) + 1;
			}
			if (_weaponBless == 0 && (undead == 1 || undead == 2 || undead == 3)) { // 축복
				// 무기,
				// 한편,
				// 안
				// 데드계·악마계·안
				// 데드계
				// 보스
				damage += _random.nextInt(4) + 1;
			}
			if (_pc.getWeapon() != null && _weaponType != 20 && _weaponType != 62
					&& weapon.getHolyDmgByMagic() != 0
					&& (undead == 1 || undead == 3)) {
				damage += weapon.getHolyDmgByMagic();
			}
		}
		return damage;
	}

	// ●●●● NPC의 안 데드의 야간 공격력의 변화 ●●●●
	private boolean isUndeadDamage() {
		boolean flag = false;
		int undead = _npc.getNpcTemplate().get_undead();
		boolean isNight = GameTimeClock.getInstance().getGameTime().isNight();
		if (isNight && (undead == 1 || undead == 3 || undead == 4)) { // 18~6시,
			// 한편, 안
			// 데드계·안
			// 데드계
			// 보스
			flag = true;
		}
		return flag;
	}

	// ●●●● PC의 독공격을 부가 ●●●●
	public void addPcPoisonAttack(L1Character attacker, L1Character target) {
		int chance = _random.nextInt(100) + 1;
		if ((_weaponId == 13 || _weaponId == 44 || (_weaponId != 0 && _pc
				.getSkillEffectTimerSet().hasSkillEffect(ENCHANT_VENOM)))
				&& chance <= 10) {
			L1DamagePoison.doInfection(attacker, target, 3000, 5);
		}
	}

	// ●●●● NPC의 독공격을 부가 ●●●●
	private void addNpcPoisonAttack(L1Character attacker, L1Character target) {
		if (_npc.getNpcTemplate().get_poisonatk() != 0) { // 독공격 있어
			if (15 >= _random.nextInt(100) + 1) { // 15%의 확률로 독공격
				if (_npc.getNpcTemplate().get_poisonatk() == 1) { // 통상독
					// 3초 주기에 데미지 5
					L1DamagePoison.doInfection(attacker, target, 3000, 5);
				} else if (_npc.getNpcTemplate().get_poisonatk() == 2) { // 침묵독
					L1SilencePoison.doInfection(target);
				} else if (_npc.getNpcTemplate().get_poisonatk() == 4) { // 마비독
					// 20초 후에 45초간 마비
					L1ParalysisPoison.doInfection(target, 20000, 45000);
				}
			}
		} else if (_npc.getNpcTemplate().get_paralysisatk() != 0) { // / 마비 공격
			// 있어
		}
	}

	// ■■■■ 마나스탓후와 강철의 마나스탓후의 MP흡수량 산출 ■■■■
	public void calcStaffOfMana() {
		int som_lvl = _weaponEnchant + 3; // 최대 MP흡수량을 설정
		if (som_lvl < 0) {
			som_lvl = 0;
		}
		// MP흡수량을 랜덤 취득
		_drainMana = _random.nextInt(som_lvl) + 1;
		// 최대 MP흡수량을 9에 제한
		if (_drainMana > Config.MANA_DRAIN_LIMIT_PER_SOM_ATTACK) {
			_drainMana = Config.MANA_DRAIN_LIMIT_PER_SOM_ATTACK;
		}
	}

	// ■■■■ 파멸의 대검 HP흡수량 산출 ■■■■
	public void calcDrainOfHp() {
		if (_weaponId == 412001) {
			int HpWeapon_lvl = _weaponEnchant; // 최대 HP흡수량을 설정
			if (HpWeapon_lvl < 1) {
				HpWeapon_lvl = 1;
			}
			// HP흡수량을 랜덤 취득
			_drainHp = _random.nextInt(HpWeapon_lvl);
			// 최대 HP흡수량을 9에 제한
			if (_drainHp > 5) {
				_drainHp = 5;
			}
		}
	}

	/** 조우의 돌골렘 - 마력의 단검 * */
	public void calcDrainOfMana() { // 마나 흡수를 위한 추가
		_drainMana = 1;
	}

	/* ■■■■■■■■■■■■■■ 공격 모션 송신 ■■■■■■■■■■■■■■ */

	public void action() {
		if (_calcType == PC_PC || _calcType == PC_NPC) {
			actionPc();
		} else if (_calcType == NPC_PC || _calcType == NPC_NPC) {
			actionNpc();
		}
	}

	// ●●●● 플레이어의 공격 모션 송신 ●●●●
	private void actionPc() {
		_pc.getMoveState().setHeading(CharPosUtil.targetDirection(_pc, _targetX, _targetY)); // 방향세트
		if (_weaponType == 20) {
			if (_arrow != null) {
				_pc.getInventory().removeItem(_arrow, 1);
				if (_pc.getGfxId().getTempCharGfx() == 7968) {
					_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 7972, _targetX, _targetY, _isHit));
					Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc,
							_targetId, 7972, _targetX, _targetY, _isHit));
				}else if (_pc.getGfxId().getTempCharGfx() == 8900){
					_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 8904, _targetX, _targetY, _isHit));
					Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc, _targetId, 8904, _targetX, _targetY, _isHit));
				}else if (_pc.getGfxId().getTempCharGfx() == 8913){
					_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 8916, _targetX, _targetY, _isHit));
					Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc, _targetId, 8916, _targetX, _targetY, _isHit)); 

				} else {
					_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 66,
							_targetX, _targetY, _isHit));
					Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc,
							_targetId, 66, _targetX, _targetY, _isHit));
				}
				if (_isHit) {
					Broadcaster.broadcastPacketExceptTargetSight(_target,
							new S_DoActionGFX(_targetId,
									ActionCodes.ACTION_Damage), _pc);
				}
			} else if (_weaponId == 190) {
				_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 2349,
						_targetX, _targetY, _isHit));
				Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc,
						_targetId, 2349, _targetX, _targetY, _isHit));
				if (_isHit) {
					Broadcaster.broadcastPacketExceptTargetSight(_target,
							new S_DoActionGFX(_targetId,
									ActionCodes.ACTION_Damage), _pc);
				}
			} else if (_weaponId >= 11011 && _weaponId <= 11013) {
				_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 8771, _targetX, _targetY, _isHit));
				Broadcaster.broadcastPacket(_pc,new S_UseArrowSkill(_pc, _targetId, 8771, _targetX, _targetY, _isHit));
				if (_isHit) {
					Broadcaster.broadcastPacketExceptTargetSight(_target, new S_DoActionGFX(_targetId, ActionCodes.ACTION_Damage), _pc);
				} //추가
			}
		} else if (_weaponType == 62 && _sting != null) {
			_pc.getInventory().removeItem(_sting, 1);
			if (_pc.getGfxId().getTempCharGfx() == 7968) {
				_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 7972,
						_targetX, _targetY, _isHit));
				Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc,
						_targetId, 7972, _targetX, _targetY, _isHit));
			}else if (_pc.getGfxId().getTempCharGfx() == 8900){
				_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 8904, _targetX, _targetY, _isHit));
				Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc, _targetId, 8904, _targetX, _targetY, _isHit));
			}else if (_pc.getGfxId().getTempCharGfx() == 8913){
				_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 8916, _targetX, _targetY, _isHit));
				Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc, _targetId, 8916, _targetX, _targetY, _isHit));
			} else {
				_pc.sendPackets(new S_UseArrowSkill(_pc, _targetId, 2989,
						_targetX, _targetY, _isHit));
				Broadcaster.broadcastPacket(_pc, new S_UseArrowSkill(_pc,
						_targetId, 2989, _targetX, _targetY, _isHit));
			}
			if (_isHit) {
				Broadcaster
				.broadcastPacketExceptTargetSight(_target,
						new S_DoActionGFX(_targetId,
								ActionCodes.ACTION_Damage), _pc);
			}
		} else {
			if (_isHit) {
				_pc.sendPackets(new S_AttackPacket(_pc, _targetId,
						ActionCodes.ACTION_Attack, _attackType));
				Broadcaster.broadcastPacket(_pc, new S_AttackPacket(_pc,
						_targetId, ActionCodes.ACTION_Attack, _attackType));
				Broadcaster
				.broadcastPacketExceptTargetSight(_target,
						new S_DoActionGFX(_targetId,
								ActionCodes.ACTION_Damage), _pc);
			} else {
				if (_targetId > 0) {
					_pc.sendPackets(new S_AttackMissPacket(_pc, _targetId));
					Broadcaster.broadcastPacket(_pc, new S_AttackMissPacket(
							_pc, _targetId));
				} else {
					_pc.sendPackets(new S_AttackPacket(_pc, 0,
							ActionCodes.ACTION_Attack));
					Broadcaster.broadcastPacket(_pc, new S_AttackPacket(_pc, 0,
							ActionCodes.ACTION_Attack));
				}
			}
		}
	}

	// ●●●● NPC의 공격 모션 송신 ●●●●
	private void actionNpc() {
		int _npcObjectId = _npc.getId();
		int bowActId = 0;
		int actId = 0;

		_npc.getMoveState().setHeading(
				CharPosUtil.targetDirection(_npc, _targetX, _targetY)); // 방향세트

		// 타겟과의 거리가 2이상 있으면 원거리 공격
		boolean isLongRange = (_npc.getLocation().getTileLineDistance(
				new Point(_targetX, _targetY)) > 1);
		bowActId = _npc.getNpcTemplate().getBowActId();

		if (getActId() > 0) {
			actId = getActId();
		} else {
			actId = ActionCodes.ACTION_Attack;
		}

		if (isLongRange && bowActId > 0) {
			Broadcaster.broadcastPacket(_npc, new S_UseArrowSkill(_npc,
					_targetId, bowActId, _targetX, _targetY, _isHit));
		} else {
			if (_isHit) {
				if (getGfxId() > 0) {
					Broadcaster.broadcastPacket(_npc, new S_UseAttackSkill(
							_target, _npcObjectId, getGfxId(), _targetX,
							_targetY, actId));
					Broadcaster.broadcastPacketExceptTargetSight(_target,
							new S_DoActionGFX(_targetId,
									ActionCodes.ACTION_Damage), _npc);
				} else {
					Broadcaster.broadcastPacket(_npc, new S_AttackPacketForNpc(
							_target, _npcObjectId, actId));
					Broadcaster.broadcastPacketExceptTargetSight(_target,
							new S_DoActionGFX(_targetId,
									ActionCodes.ACTION_Damage), _npc);
				}
			} else {
				if (getGfxId() > 0) {
					Broadcaster.broadcastPacket(_npc, new S_UseAttackSkill(
							_target, _npcObjectId, getGfxId(), _targetX,
							_targetY, actId, 0));
				} else {
					Broadcaster.broadcastPacket(_npc, new S_AttackMissPacket(
							_npc, _targetId, actId));
				}
			}
		}
	}

	/* ■■■■■■■■■■■■■■■ 계산 결과 반영 ■■■■■■■■■■■■■■■ */
	/** 물리 공격 멘트 **/
	public void commit() { //검색 하세요
		if (_isHit) {
			if (_calcType == PC_PC || _calcType == NPC_PC) {
				commitPc();
			} else if (_calcType == PC_NPC || _calcType == NPC_NPC) {
				commitNpc();
			}
		}
		if (!Config.ALT_ATKMSG) {
			return;
		}
		if (Config.ALT_ATKMSG) {
			if ((_calcType == PC_PC || _calcType == PC_NPC) && !_pc.isGm()) {
				return;
			}
			if ((_calcType == PC_PC || _calcType == NPC_PC)	&& !_targetPc.isGm()) {
				return;
			}
		}
		StringBuilder msg = new StringBuilder();
		if (_calcType == PC_PC || _calcType == PC_NPC) {
			msg.append(_pc.getName());
		} else if (_calcType == NPC_PC) {
			msg.append(_npc.getName());
		}
		msg.append(" => ");
		if (_calcType == NPC_PC || _calcType == PC_PC) {
			msg.append(_targetPc.getName()).append(_isHit ? " == 물리 데미지 " + _damage : " == 미스")
			.append(" => HP " + _targetPc.getCurrentHp());
		} else if (_calcType == PC_NPC) {
			msg.append(_targetNpc.getName()).append(_isHit ? " == 물리 데미지 " + _damage : " == 미스")
			.append(" => HP " + _targetNpc.getCurrentHp());
		}
		if (_calcType == PC_PC || _calcType == PC_NPC) { 
			_pc.sendPackets(new S_SystemMessage(msg.toString()));
		}
		if (_calcType == NPC_PC || _calcType == PC_PC) {
			_targetPc.sendPackets(new S_SystemMessage(msg.toString()));
		}
		msg = null;
	}


	// ●●●● 플레이어에 계산 결과를 반영 ●●●●
	private void commitPc() {
		if (_calcType == PC_PC) {
			if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
					|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
							ABSOLUTE_BARRIER)					
							|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
									FREEZING_BLIZZARD)
									|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
											FREEZING_BREATH)
											|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
													EARTH_BIND)
													|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
															MOB_BASILL) // 바실얼리기데미지0
															|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
																	MOB_COCA)) { // 코카얼리기데미지0
				_damage = 0;
				_drainMana = 0;
				_drainHp = 0;
			}
			if (_drainMana > 0 && _targetPc.getCurrentMp() > 0) {
				if (_drainMana > _targetPc.getCurrentMp()) {
					_drainMana = _targetPc.getCurrentMp();
				}
				short newMp = (short) (_targetPc.getCurrentMp() - _drainMana);
				_targetPc.setCurrentMp(newMp);
				newMp = (short) (_pc.getCurrentMp() + _drainMana);
				_pc.setCurrentMp(newMp);
			}

			/** 조우의 돌골렘 * */

			if (_drainHp > 0 && _targetPc.getCurrentHp() > 0) {
				if (_drainHp > _targetPc.getCurrentHp()) {
					_drainHp = _targetPc.getCurrentHp();
				}
				short newHp = (short) (_targetPc.getCurrentHp() - _drainHp);
				_targetPc.setCurrentHp(newHp);
				newHp = (short) (_pc.getCurrentHp() + _drainHp);
				_pc.setCurrentHp(newHp);
			}
			/** 조우의 돌골렘 * */

			damagePcWeaponDurability(); // 무기를 손상시킨다.

			_targetPc.receiveDamage(_pc, _damage, false);
		} else if (_calcType == NPC_PC) {
			if (_targetPc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
					|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
							ABSOLUTE_BARRIER)					
							|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
									FREEZING_BLIZZARD)
									|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
											FREEZING_BREATH)
											|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
													EARTH_BIND)
													|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
															MOB_BASILL) // 바실얼리기데미지0
															|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
																	MOB_COCA)) { // 코카얼리기데미지0
				_damage = 0;
			}
			_targetPc.receiveDamage(_npc, _damage, false);
		}
	}

	// ●●●● NPC에 계산 결과를 반영 ●●●●
	private void commitNpc() {
		if (_calcType == PC_NPC) {
			if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
					|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
							FREEZING_BLIZZARD)
							|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
									FREEZING_BREATH)
									|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
											EARTH_BIND)
											|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
													MOB_BASILL) // 바실얼리기데미지0
													|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
															MOB_COCA)) { // 코카얼리기데미지0
				_damage = 0;
				_drainMana = 0;
				_drainHp = 0;
			}
			if (_drainMana > 0) {
				int drainValue = _targetNpc.drainMana(_drainMana);
				int newMp = _pc.getCurrentMp() + drainValue;
				_pc.setCurrentMp(newMp);

				if (drainValue > 0) {
					int newMp2 = _targetNpc.getCurrentMp() - drainValue;
					_targetNpc.setCurrentMp(newMp2);
				}
			}

			/** 조우의 돌골렘 * */

			if (_drainHp > 0) {
				int newHp = _pc.getCurrentHp() + _drainHp;
				_pc.setCurrentHp(newHp);
			}
			/** 조우의 돌골렘 * */

			damageNpcWeaponDurability(); // 무기를 손상시킨다.

			_targetNpc.receiveDamage(_pc, _damage);
		} else if (_calcType == NPC_NPC) {
			if (_targetNpc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
					|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
							FREEZING_BLIZZARD)
							|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
									FREEZING_BREATH)
									|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
											EARTH_BIND)
											|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
													MOB_BASILL) // 바실얼리기데미지0
													|| _targetNpc.getSkillEffectTimerSet().hasSkillEffect(
															MOB_COCA)) { // //코카얼리기데미지0
				_damage = 0;
			}
			_targetNpc.receiveDamage(_npc, _damage);
		}
	}

	public void DrainofEvil1() { //여긴 지팡이 키링크
		int chance = _random.nextInt(100) + 1;
		if ((_weaponEnchant + 4) >= chance) { 
			int dice = 7;
			int diceCount = 3;
			int value = 5;
			int EveilDamage = 0;
			for (int i = 0; i < diceCount; i++) {
				EveilDamage += (_random.nextInt(dice) + 1);
			}
			EveilDamage += value;
			_drainMana = EveilDamage; 
			_pc.sendPackets(new S_SkillSound(_target.getId(), 8152));
			Broadcaster.broadcastPacket(_pc, new S_SkillSound(_target.getId(), 8152)); 
		}
	}

	/* ■■■■■■■■■■■■■■■ 카운터 바리어 ■■■■■■■■■■■■■■■ */

	// ■■■■ 카운터 바리어시의 공격 모션 송신 ■■■■
	public void actionCounterBarrier() {
		if (_calcType == PC_PC) {
			_pc.getMoveState().setHeading(
					CharPosUtil.targetDirection(_pc, _targetX, _targetY)); // 방향세트
			_pc.sendPackets(new S_AttackMissPacket(_pc, _targetId));
			Broadcaster.broadcastPacket(_pc, new S_AttackMissPacket(_pc,
					_targetId));
			_pc.sendPackets(new S_DoActionGFX(_pc.getId(),
					ActionCodes.ACTION_Damage));
			Broadcaster.broadcastPacket(_pc, new S_DoActionGFX(_pc.getId(),
					ActionCodes.ACTION_Damage));
			_pc.sendPackets(new S_SkillSound(_targetId, 4395));//티나는이펙 4395
			Broadcaster.broadcastPacket(_pc, new S_SkillSound(_targetId, 4395));

		} else if (_calcType == NPC_PC) {
			int actId = 0;
			_npc.getMoveState().setHeading(
					CharPosUtil.targetDirection(_npc, _targetX, _targetY)); // 방향세트
			if (getActId() > 0) {
				actId = getActId();
			} else {
				actId = ActionCodes.ACTION_Attack;
			}
			if (getGfxId() > 0) {
				Broadcaster
				.broadcastPacket(_npc, new S_UseAttackSkill(_target,
						_npc.getId(), getGfxId(), _targetX, _targetY,
						actId, 0));
			} else {
				Broadcaster.broadcastPacket(_npc, new S_AttackMissPacket(_npc,
						_targetId, actId));
			}
			Broadcaster.broadcastPacket(_npc, new S_DoActionGFX(_npc.getId(),
					ActionCodes.ACTION_Damage));
			Broadcaster
			.broadcastPacket(_npc, new S_SkillSound(_targetId, 4395));
		}
	}

	// ■■■■ 모탈바디 발동시의 공격 모션 송신 ■■■■
	public void actionMortalBody() {
		if (_calcType == PC_PC) {
			_pc.getMoveState().setHeading(CharPosUtil.targetDirection(_pc, _targetX, _targetY)); // 방향세트
			S_UseAttackSkill packet = new S_UseAttackSkill(_pc,	_target.getId(), 6513, _targetX, _targetY,
					ActionCodes.ACTION_Attack, false);
			_pc.sendPackets(packet);
			Broadcaster.broadcastPacket(_pc, packet);
			_pc.sendPackets(new S_DoActionGFX(_pc.getId(),
					ActionCodes.ACTION_Damage));
			Broadcaster.broadcastPacket(_pc, new S_DoActionGFX(_pc.getId(),
					ActionCodes.ACTION_Damage));
		} else if (_calcType == NPC_PC) {
			_npc.getMoveState().setHeading(
					CharPosUtil.targetDirection(_npc, _targetX, _targetY)); // 방향세트
			Broadcaster.broadcastPacket(_npc, new S_SkillSound(_target.getId(),
					6513));
			Broadcaster.broadcastPacket(_npc, new S_DoActionGFX(_npc.getId(),
					ActionCodes.ACTION_Damage));
		}
	}

	// ■■■■ 상대의 공격에 대해서 카운터 바리어가 유효한가를 판별 ■■■■
	public boolean isShortDistance() {
		boolean isShortDistance = true;
		if (_calcType == PC_PC) {
			if (_weaponType == 20 || _weaponType == 62|| _weaponType1 == 17) { // 활이나 간트렛트키링크추가
				isShortDistance = false;
			}
		} else if (_calcType == NPC_PC) {
			boolean isLongRange = (_npc.getLocation().getTileLineDistance(
					new Point(_targetX, _targetY)) > 1);
			int bowActId = _npc.getNpcTemplate().getBowActId();
			// 거리가 2이상, 공격자의 활의 액션 ID가 있는 경우는 원공격
			if (isLongRange && bowActId > 0) {
				isShortDistance = false;
			}
		}
		return isShortDistance;
	}

	// ■■■■ 카운터 바리어의 데미지를 반영 ■■■■
	public void commitCounterBarrier() {
		int damage = calcCounterBarrierDamage();
		if (damage == 0) {
			return;
		}
		if (_calcType == PC_PC) {
			_pc.receiveDamage(_targetPc, damage, false);
		} else if (_calcType == NPC_PC) {
			_npc.receiveDamage(_targetPc, damage);
		}
	}

	// ■■■■ 모탈바디의 데미지를 반영 ■■■■
	public void commitMortalBody() {
		int damage = 10;
		if (damage == 0) {
			return;
		}
		if (_calcType == PC_PC) {
			_pc.receiveDamage(_targetPc, damage, false);
		} else if (_calcType == NPC_PC) {
			_npc.receiveDamage(_targetPc, damage);
		}
	}

	// ●●●● 카운터 바리어의 데미지를 산출 ●●●●
	private int calcCounterBarrierDamage() {
		int damage = 0;
		L1ItemInstance weapon = null;
		weapon = _targetPc.getWeapon();
		if (weapon != null) {
			if (weapon.getItem().getType() == 3) {// Two-handed sword
				// (BIG strengthen the maximum number + + Additional Damage Damage) * 2
				damage = (weapon.getItem().getDmgLarge() + weapon.getEnchantLevel() + weapon.getItem().getDmgModifier()) * 2 ;
			}
		}
		return damage;
	}

	/*
	 * 무기를 손상시킨다. 대NPC의 경우, 손상 확률은10%로 한다. 축복 무기는3%로 한다.
	 */
	private void damageNpcWeaponDurability() {
		int chance = 3;
		int bchance = 1;

		/*
		 * 손상하지 않는 NPC, 맨손, 손상하지 않는 무기 사용, SOF중의 경우 아무것도 하지 않는다.
		 */
		if (_calcType != PC_NPC
				|| _targetNpc.getNpcTemplate().is_hard() == false
				|| _weaponType == 0 || weapon.getItem().get_canbedmg() == 0
				|| _pc.getSkillEffectTimerSet().hasSkillEffect(SOUL_OF_FLAME)) {
			return;
		}
		// 통상의 무기·저주해진 무기
		if ((_weaponBless == 1 || _weaponBless == 2)
				&& ((_random.nextInt(100) + 1) < chance)) {
			// \f1당신의%0가 손상했습니다.
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon);
		}
		// 축복된 무기
		if (_weaponBless == 0 && ((_random.nextInt(100) + 1) < bchance)) {
			// \f1당신의%0가 손상했습니다.
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon);
		}
	}

	/*
	 * 바운스아탁크에 의해 무기를 손상시킨다. 바운스아탁크의 손상 확률은10%
	 */
	private void damagePcWeaponDurability() {
		// PvP 이외, 맨손, 활, 암 토토 렛, 상대가 바운스아탁크미사용, SOF중의 경우 아무것도 하지 않는다
		if (_calcType != PC_PC
				|| _weaponType == 0
				|| _weaponType == 20
				|| _weaponType == 62
				|| _weaponType1 == 17 //키링크 추가
				|| _targetPc.getSkillEffectTimerSet().hasSkillEffect(
						BOUNCE_ATTACK) == false
						|| _pc.getSkillEffectTimerSet().hasSkillEffect(SOUL_OF_FLAME)
						|| _targetPc.isParalyzed()) {
			return;
		}

		if (_random.nextInt(100) + 1 <= 2) {
			// \f1당신의%0가 손상했습니다.
			_pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
			_pc.getInventory().receiveDamage(weapon);
		}
	}
}