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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.poison.L1Poison;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Poison;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.ServerBasePacket;
import l1j.server.server.utils.IntRange;

//Referenced classes of package l1j.server.server.model:
//L1Object, Die, L1PcInstance, L1MonsterInstance,
//L1World, ActionFailed

public class L1Character extends L1Object {
	private static final long serialVersionUID = 1L;

	// 케릭터 기본
	//private BasicProperty basic;

	private String _name; 
	private String _title;

	private int _level; 
	private int _exp;

	private int _lawful; 
	private int _karma;

	private int _currentHp;
	private int _trueMaxHp;
	private short _maxHp;

	private int _currentMp;
	private int _trueMaxMp;
	private short _maxMp;

	private L1Poison _poison = null;
	private boolean _paralyzed;
	private boolean _sleeped;
	private L1Paralysis _paralysis;
	private boolean _isDead; 

	protected GfxId gfx;					// 케릭터 그래픽 ID
	private MoveState moveState;		// 이동속도, 바라보는 방향
	protected Light light;				// 케릭터 주위  빛
	protected Ability ability; 			// 능력치, SP, MagicBonus
	protected Resistance resistance;	// 저항 (마방, 불, 물, 바람, 땅, 스턴, 동빙, 슬립, 석화)
	protected AC ac;					// AC 방어

	private NearObjects nearObjects;	// 주위 객체 및 플레이어들
	private SkillEffectTimerSet skillEffectTimerSet;	// 스킬 타이머 

	// 모르는거
	private boolean _isSkillDelay;
	private int _addAttrKind; 
	private int actionStatus; 
	//파푸리뉴얼
	public ArrayList<String> marble = new ArrayList<String>();
	public ArrayList<String> marble2 = new ArrayList<String>();
	public ArrayList<String> tro = new ArrayList<String>();
	public ArrayList<String> sael = new ArrayList<String>();
	public ArrayList<String> sael2 = new ArrayList<String>();

	private boolean _isChaserHitting = false;

	public boolean isChaserHitting() {
		return _isChaserHitting;
	}

	public void setChaserHitting(boolean i) {
		_isChaserHitting = i;
	}
	/* Ox존 설정*/
	//private int _OxLine; //듀얼라인설정추가
	//public int get_OxLine() { return _OxLine; }
	//public void add_OxLine(int i) { set_OxLine(_OxLine +1); }
	//public void set_OxLine(int i) { _OxLine = i; }

	/**
	 * OX 퀴즈 관련
	 */
	private int _QuizResult;
	public int get_QuizResult(){ return _QuizResult; }
	public void set_QuizResult(int i){ _QuizResult = i; }	
	
	// 데미지
	private int _dmgup; 
	private int _trueDmgup; 
	private int _bowDmgup; 
	private int _trueBowDmgup; 
	private int _hitup; 
	private int _trueHitup; 
	private int _bowHitup;
	private int _trueBowHitup; 
	private int _BattleLine; 

	public int get_BattleLine(){
		return _BattleLine;
	} 

	public void set_BattleLine(int i){
		_BattleLine = i;
	} 

	private boolean _BattleOk; 

	public void setBattleOk(boolean BattleOk){
		_BattleOk = BattleOk;
	}
	public boolean getBattleOk(){
		return _BattleOk;
	}

	private final Map<Integer, L1ItemDelay.ItemDelayTimer> _itemdelay = new HashMap<Integer, L1ItemDelay.ItemDelayTimer>();

	public L1Character() {
		_level = 1;
		ability = new Ability(this); 
		resistance = new Resistance(this);
		ac = new AC();
		moveState = new MoveState();
		light = new Light(this);
		nearObjects = new NearObjects();
		gfx = new GfxId();
		skillEffectTimerSet = new SkillEffectTimerSet(this);
	}


	/**
	 * 캐릭터를 부활시킨다.
	 * 
	 * @param hp
	 *            부활 후의 HP
	 */
	public void resurrect(int hp) {
		if (!isDead()) return;
		if (hp <= 0)   hp = 1;

		setCurrentHp(hp);
		setDead(false);
		setActionStatus(0);
		L1PolyMorph.undoPoly(this);

		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.sendPackets(new S_RemoveObject(this));
			pc.getNearObjects().removeKnownObject(this);
			pc.updateObject();
		}
	}

	/**
	 * 캐릭터의 현재의 HP를 돌려준다.
	 * 
	 * @return 현재의 HP
	 */
	public int getCurrentHp() {	return _currentHp; }

	/**
	 * 캐릭터의 HP를 설정한다.
	 * 
	 * @param i 캐릭터의 새로운 HP
	 */
	public void setCurrentHp(int i) {
		if (i >= getMaxHp()) {
			i = getMaxHp();
		}
		if (i < 0) i = 0;

		_currentHp = i;
	}

	/**
	 * 캐릭터의 현재의 MP를 돌려준다.
	 * 
	 * @return 현재의 MP
	 */
	public int getCurrentMp() {
		return _currentMp;
	}

	/**
	 * 캐릭터의 MP를 설정한다.
	 * 
	 * @param i 캐릭터의 새로운 MP
	 */
	public void setCurrentMp(int i) {
		if (i >= getMaxMp()) {
			i = getMaxMp();
		}
		if (i < 0) i = 0;

		_currentMp = i;
	}

	/**
	 * 캐릭터의 잠상태를 돌려준다.
	 * 
	 * @return 잠상태를 나타내는 값. 잠상태이면 true.
	 */
	public boolean isSleeped() {
		return _sleeped;
	}

	/**
	 * 캐릭터의 잠상태를 설정한다.
	 * 
	 * @param sleeped
	 *            잠상태를 나타내는 값. 잠상태이면 true.
	 */
	public void setSleeped(boolean sleeped) {
		_sleeped = sleeped;
	}

	/**
	 * 캐릭터의 마비 상태를 돌려준다.
	 * 
	 * @return 마비 상태를 나타내는 값. 마비 상태이면 true.
	 */
	public boolean isParalyzed() {
		return _paralyzed;
	}

	/**
	 * 캐릭터의 마비 상태를 돌려준다.
	 * 
	 * @return 마비 상태를 나타내는 값. 마비 상태이면 true.
	 */
	public void setParalyzed(boolean paralyzed) {
		_paralyzed = paralyzed;
	}

	public L1Paralysis getParalysis() {
		return _paralysis;
	}

	public void setParalaysis(L1Paralysis p) {
		_paralysis = p;
	}

	public void cureParalaysis() {
		if (_paralysis != null) {
			_paralysis.cure();
		}
	}
	public void broadcastPacket(ServerBasePacket packet) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(this)) {
			pc.sendPackets(packet);
		}
	}
	/**
	 * 캐릭터의 목록을 돌려준다.
	 * 
	 * @return 캐릭터의 목록을 나타내는, L1Inventory 오브젝트.
	 */
	public L1Inventory getInventory() {
		return null;
	}

	/**
	 * 캐릭터에, skill delay 추가
	 * 
	 * @param flag
	 */
	public void setSkillDelay(boolean flag) {
		_isSkillDelay = flag;
	}

	/**
	 * 캐릭터의 독 상태를 돌려준다.
	 * 
	 * @return 스킬 지연중인가.
	 */
	public boolean isSkillDelay() {
		return _isSkillDelay;
	}

	/**
	 * 캐릭터에, Item delay 추가
	 * 
	 * @param delayId
	 *            아이템 지연 ID.  통상의 아이템이면 0, 인비지비리티크로크, 바르로그브랏디크로크이면 1.
	 * @param timer
	 *            지연 시간을 나타내는, L1ItemDelay.ItemDelayTimer 오브젝트.
	 */
	public void addItemDelay(int delayId, L1ItemDelay.ItemDelayTimer timer) {
		_itemdelay.put(delayId, timer);
	}

	/**
	 * 캐릭터로부터, Item delay 삭제
	 * 
	 * @param delayId
	 *            아이템 지연 ID.  통상의 아이템이면 0, 인비지비리티크로크, 바르로그브랏디크로크이면 1.
	 */
	public void removeItemDelay(int delayId) {
		_itemdelay.remove(delayId);
	}

	/**
	 * 캐릭터에, Item delay 이 있을까
	 * 
	 * @param delayId
	 *            조사하는 아이템 지연 ID.  통상의 아이템이면 0, 인비지비리티크로크, 바르로그브랏디
	 *            클로크이면 1.
	 * @return 아이템 지연이 있으면 true, 없으면 false.
	 */
	public boolean hasItemDelay(int delayId) {
		return _itemdelay.containsKey(delayId);
	}

	/**
	 * 캐릭터의 item delay 시간을 나타내는, L1ItemDelay.ItemDelayTimer를 돌려준다.
	 * 
	 * @param delayId
	 *            조사하는 아이템 지연 ID.  통상의 아이템이면 0, 인비지비리티크로크, 바르로그브랏디
	 *            클로크이면 1.
	 * @return 아이템 지연 시간을 나타내는, L1ItemDelay.ItemDelayTimer.
	 */
	public L1ItemDelay.ItemDelayTimer getItemDelayTimer(int delayId) {
		return _itemdelay.get(delayId);
	}

	/**
	 * 캐릭터에, 독을 추가한다.
	 * 
	 * @param poison
	 *            독을 나타내는, L1Poison 오브젝트.
	 */
	public void setPoison(L1Poison poison) {
		_poison = poison;
	}

	/**
	 * 캐릭터의 독을 치료한다.
	 */
	public void curePoison() {
		if (_poison == null) {
			return;
		}
		_poison.cure();
	}

	/**
	 * 캐릭터의 독상태를 돌려준다.
	 * 
	 * @return 캐릭터의 독을 나타내는, L1Poison 오브젝트.
	 */
	public L1Poison getPoison() {
		return _poison;
	}

	/**
	 * 캐릭터에 독의 효과를 부가한다
	 * 
	 * @param effectId
	 * @see S_Poison#S_Poison(int, int)
	 */
	public void setPoisonEffect(int effectId) {
		Broadcaster.broadcastPacket(this, new S_Poison(getId(), effectId));
	}
	private int _tempCharGfx; // ● 베이스 그래픽 ID

	public int getTempCharGfx() {
		return _tempCharGfx;
	}

	public void setTempCharGfx(int i) {
		_tempCharGfx = i;
	}

	public int getExp() 		{ return _exp; }
	public void setExp(int exp) { _exp = exp;  }

	public String getName() 		{ return _name; }
	public void setName(String s) 	{ _name = s; 	}

	public String getTitle() { return _title; }
	public void setTitle(String s) { _title = s; }

	public synchronized int getLevel() 				{ return _level; 		}
	public synchronized void setLevel(long level) 	{ _level = (int) level; }

	public short getMaxHp() 	 { return _maxHp; 			 }
	public void addMaxHp(int i)  { setMaxHp(_trueMaxHp + i); }
	public void setMaxHp(int hp) {
		_trueMaxHp = hp;
		_maxHp = (short) IntRange.ensure(_trueMaxHp, 1, 32767);
		_currentHp = Math.min(_currentHp, _maxHp);
	}

	public short getMaxMp() 	 { return _maxMp; }
	public void setMaxMp(int mp) {
		_trueMaxMp = mp;
		_maxMp = (short) IntRange.ensure(_trueMaxMp, 0, 32767);
		_currentMp = Math.min(_currentMp, _maxMp);
	}

	public void addMaxMp(int i) { setMaxMp(_trueMaxMp + i); 		 }
	public void healHp(int pt)  { setCurrentHp(getCurrentHp() + pt); }

	public int getAddAttrKind() 	  { return _addAttrKind; }
	public void setAddAttrKind(int i) { _addAttrKind = i; 	 }

	public int getDmgup() {	return _dmgup; } 
	public void addDmgup(int i) {
		_trueDmgup += i;
		if      (_trueDmgup >=  127) { _dmgup = 127;		} 
		else if (_trueDmgup <= -128) { _dmgup = -128; 		} 
		else 						 { _dmgup = _trueDmgup; }
	}

	public int getBowDmgup() { return _bowDmgup; } 
	public void addBowDmgup(int i) {
		_trueBowDmgup += i;
		if 		(_trueBowDmgup >=  127) { _bowDmgup = 127;			 }	 
		else if (_trueBowDmgup <= -128) { _bowDmgup = -128;			 } 
		else 							{ _bowDmgup = _trueBowDmgup; }
	}

	public int getHitup() {	return _hitup; } 
	public void addHitup(int i) {
		_trueHitup += i;
		if 		(_trueHitup >=  127) { _hitup = 127;  		}	 
		else if (_trueHitup <= -128) { _hitup = -128; 		} 
		else 						 { _hitup = _trueHitup;	}
	}

	public int getBowHitup() { return _bowHitup; } 
	public void addBowHitup(int i) {
		_trueBowHitup += i;
		if 		(_trueBowHitup >=  127) { _bowHitup = 127;  		 } 
		else if (_trueBowHitup <= -128) { _bowHitup = -128;			 } 
		else 							{ _bowHitup = _trueBowHitup; }
	}

	public boolean isDead() { return _isDead; }
	public void setDead(boolean flag) { _isDead = flag;	}

	public int getActionStatus() { return actionStatus; }
	public void setActionStatus(int i) { actionStatus = i;	}


	public int getLawful() { return _lawful; }
	public void setLawful(int i) { _lawful = i; }

	public synchronized void addLawful(int i) {
		_lawful += i;
		if 		(_lawful >  32767) { _lawful = 32767;  } 
		else if (_lawful < -32768) { _lawful = -32768; }
	}

	private int _moveSpeed; 

	public int getMoveSpeed() {
		return _moveSpeed;
	}

	public void setMoveSpeed(int i) {
		_moveSpeed = i;
	}

	private int _braveSpeed;

	public int getBraveSpeed() {
		return _braveSpeed;
	}

	public void setBraveSpeed(int i) {
		_braveSpeed = i;
	}

	public int checkMove() {
		if (getMap().isPassable(getLocation())) {
			return 1;
		} else {
			return 0;
		}
	}

	/* Kill & Death 시스템?  -by 천국- */
	private int _Kills;

	public int getKills() {
		return _Kills;
	}
	public void setKills(int Kills) {
		_Kills = Kills;
	} 
	private int _Deaths;

	public int getDeaths() {
		return _Deaths;
	}
	public void setDeaths(int Deaths) {
		_Deaths = Deaths;
	}
	private int _KillDeathInitialize;

	public int get_KillDeathInitialize() {
		return _KillDeathInitialize;
	}
	public void set_KillDeathInitialize(int KillDeathInitialize) {
		_KillDeathInitialize = KillDeathInitialize;
	}
	/* Kill & Death 시스템?  -by 천국- */

	/** 캐릭터의 우호도을 돌려준다.	 */
	public int getKarma() {	return _karma; }
	/** 캐릭터의 우호도을 설정한다.	 */
	public void setKarma(int karma) { _karma = karma; }

	public GfxId getGfxId()				{ return gfx;			}
	public NearObjects getNearObjects()	{ return nearObjects;	}
	public Light getLight() 			{ return light; 		} 
	public Ability getAbility() 		{ return ability; 		}
	public Resistance getResistance() 	{ return resistance; 	}
	public AC getAC()					{ return ac;			}
	public MoveState getMoveState()		{ return moveState;		}
	public SkillEffectTimerSet getSkillEffectTimerSet() { return skillEffectTimerSet; }

	public boolean isInvisble() {
		return (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BLIND_HIDING));
	}
	//**지엠 버프 따로 저장 **//  by 도우너 
	private int _buffnoch;  
	public int getBuffnoch(){
		return _buffnoch;
	} 

	public void setBuffnoch(int buffnoch){
		_buffnoch = buffnoch;
		//**지엠 버프 따로 저장 **// 
	}


	// 좀비모드#####(시즌) by-Season
	private int _zombiemod;

	public int getZombieMod() {
		return _zombiemod;
	}

	public void setZombieMod(int season) {
		_zombiemod = season;
	}

	private int _zombiehp;

	public int getZombieHp() {
		return _zombiehp;
	}

	public void setZombieHp(int season) {
		_zombiemod = season;
	}

	private int _backhp;

	public int getBackHp(){
		return _backhp;
	}
	public void setBackHp(int season){
		_backhp = season;
	}
	private int _killpoint;

	public int getKillPoint(){
		return _killpoint;
	}
	public void setKillPoint(int season){
		_killpoint = season;
	}
	////////////////////////좀비 by-Season
}
