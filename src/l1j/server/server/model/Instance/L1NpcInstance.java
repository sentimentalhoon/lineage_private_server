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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.LineageClient;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.datatables.NpcChatTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1GroundInventory;
import l1j.server.server.model.L1HateList;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Magic;
import l1j.server.server.model.L1MobGroupInfo;
import l1j.server.server.model.L1MobSkillUse;
import l1j.server.server.model.L1NpcRegenerationTimer;
import l1j.server.server.model.L1NpcChatTimer;
import l1j.server.server.model.L1NpcTalkData;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Spawn;
import l1j.server.server.model.L1World;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_ChangeShape;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_MoveCharPacket;
import l1j.server.server.serverpackets.S_NPCPack;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.ServerBasePacket;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1NpcChat;
import l1j.server.server.types.Point;
import l1j.server.server.utils.TimerPool;
import static l1j.server.server.model.item.L1ItemId.*;

public class L1NpcInstance extends L1Character {
	private static final long serialVersionUID = 1L;

	public static final int MOVE_SPEED = 0;
	public static final int ATTACK_SPEED = 1;
	public static final int MAGIC_SPEED = 2;

	public static final int HIDDEN_STATUS_NONE = 0;
	public static final int HIDDEN_STATUS_SINK = 1;
	public static final int HIDDEN_STATUS_FLY = 2;	

	public static final int CHAT_TIMING_APPEARANCE = 0;
	public static final int CHAT_TIMING_DEAD = 1;
	public static final int CHAT_TIMING_HIDE = 2;
	public static final int CHAT_TIMING_GAME_TIME = 3;

	private static final long DELETE_TIME = 10000L;
	private LineageClient _netConnection;

	private L1Npc _npcTemplate;
	private L1Spawn _spawn;

	private int _spawnNumber;
	private int _petcost;

	protected L1Inventory _inventory = new L1Inventory();
	private L1MobSkillUse mobSkill;
	private static Random _random = new Random(System.nanoTime());

	private boolean firstFound = true;

	private static int courceRange = 25;
	private int _drainedMana = 0;

	private boolean _rest = false;
	private boolean _isResurrect;

	private int _randomMoveDistance = 0;
	private int _randomMoveDirection = 0;

	private boolean _aiRunning = false; 
	private boolean _actived = false; 
	private boolean _firstAttack = false; 
	private int _sleep_time; 

	public ArrayList marble = new ArrayList();
	public ArrayList marble2 = new ArrayList();
	public ArrayList tro = new ArrayList();
	private int _pahp;  //추가


	protected L1HateList _hateList = new L1HateList();
	protected L1HateList _dropHateList = new L1HateList();
	protected List<L1ItemInstance> _targetItemList = new ArrayList<L1ItemInstance>();
	protected L1Character _target = null; 
	protected L1ItemInstance _targetItem = null; 
	protected L1PcInstance _master = null; 
	private boolean _deathProcessing = false; 
	private int _paralysisTime = 0; // Paralysis RestTime
	private L1MobGroupInfo _mobGroupInfo = null;
	private int _mobGroupId = 0;
	private int num;	/*버경 관련*/

	private DeleteTimer _deleteTask;
	private ScheduledFuture<?> _future = null;

	private Map<Integer, Integer> _digestItems;
	public boolean _digestItemRunning = false;

	private static final byte HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };	
	private static final byte HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };

	private static Logger _log = Logger.getLogger(L1NpcInstance.class.getName());

	public L1NpcInstance(L1Npc template) {
		super();
		setActionStatus(0); 
		getMoveState().setMoveSpeed(0);
		setDead(false);
		setRespawn(false);

		if (template != null) {
			setting_template(template);
		}
	}
	private int calcRandomVal(int random, int val) {
		int ran = 0;
		if (random > 0){
			if (val > 0){
				ran = _random.nextInt(random - val) + 1;
			}else{
				ran = _random.nextInt(random) + 1;
			}
			val += ran;	
		} else {
			ran = _random.nextInt(random * (-1)) + 1;
			val -= ran;
		}			
		return val;
	}
	private double calcRandomVal(int seed, int ranval, double rate) {
		return rate * ( ranval - seed );
	}

	protected void setting_template(L1Npc template) {
		_npcTemplate = template;
		double rate = 0;
		int diff = 0;

		setName(template.get_name());
		setNameId(template.get_nameid());
		setTitle(template.get_title());//엔피씨 호칭
		int level = template.get_level();
		int randomlevel = template.get_randomlevel();
		if(randomlevel != 0) {			
			level = calcRandomVal(randomlevel, level);
			diff = randomlevel - level;
			if (level <= 0) level = 1;
		}
		setLevel(level);

		int hp = template.get_hp();
		int randomhp = template.get_randomhp();
		if(randomhp != 0) {
			hp = calcRandomVal(randomhp, hp);
			if (hp <= 0) hp = 1;
		}
		setMaxHp(hp);
		setCurrentHp(hp);

		int mp = template.get_mp();
		int randommp = template.get_randommp();
		if (randommp != 0) {
			mp = calcRandomVal(randommp, mp);
			if (mp <= 0) mp = 0;			
		}
		setMaxMp(mp);
		setCurrentMp(mp);

		int ac = template.get_ac();
		int randomac = template.get_randomac();
		if (randomac != 0) {
			ac = calcRandomVal(randomac, ac); 
		} 
		this.ac.setAc(ac);

		if (template.get_randomlevel() == 0) {
			ability.setStr(template.get_str());
			ability.setCon(template.get_con());
			ability.setDex(template.get_dex());
			ability.setInt(template.get_int());
			ability.setWis(template.get_wis());
			resistance.setBaseMr(template.get_mr());
		} else {
			ability.setStr((byte) Math.min(template.get_str() + diff, 127));
			ability.setCon((byte) Math.min(template.get_con() + diff, 127));
			ability.setDex((byte) Math.min(template.get_dex() + diff, 127));
			ability.setInt((byte) Math.min(template.get_int() + diff, 127));
			ability.setWis((byte) Math.min(template.get_wis() + diff, 127));
			resistance.setBaseMr((byte) Math.min(template.get_mr() + diff, 127));

			addHitup((template.get_randomlevel() - level) * 2);
			addDmgup((template.get_randomlevel() - level) * 2);
		}

		setPassispeed(template.get_passispeed());
		setAtkspeed(template.get_atkspeed());
		setAgro(template.is_agro());
		setAgrocoi(template.is_agrocoi());
		setAgrososc(template.is_agrososc());

		gfx.setTempCharGfx(template.get_gfxid());
		gfx.setGfxId(template.get_gfxid());

		if (template.get_randomexp() == 0) {
			setExp(template.get_exp());
		} else {	
			int ran = _random.nextInt(template.get_randomexp())+template.get_exp();
			if (ran >= template.get_randomexp()){
				ran = template.get_randomexp();
			}
			setExp(ran);
		}

		int lawful = template.get_lawful();
		int randomlawful = template.get_randomlawful();
		if (randomlawful != 0) {
			lawful = calcRandomVal(randomlawful, lawful);
		}
		setLawful(lawful);
		setTempLawful(lawful);

		setPickupItem(template.is_picupitem());
		if (template.is_bravespeed()) {
			getMoveState().setBraveSpeed(1);
		} else {
			getMoveState().setBraveSpeed(0);
		}
		if (template.get_digestitem() > 0) {
			_digestItems = new HashMap<Integer, Integer>();
		}
		setKarma(template.getKarma());
		setLightSize(template.getLightSize());

		mobSkill = new L1MobSkillUse(this);
	}

	interface NpcAI {
		public void start();
	}

	protected void startAI() {
		if (Config.NPCAI_IMPLTYPE == 1) {
			new NpcAITimerImpl().start();
		} else if (Config.NPCAI_IMPLTYPE == 2) {
			new NpcAIThreadImpl().start();
		} else {
			new NpcAITimerImpl().start();
		}
	}

	private static final TimerPool _timerPool = new TimerPool(4);

	class NpcAITimerImpl extends TimerTask implements NpcAI {
		private class DeathSyncTimer extends TimerTask {
			private void schedule(int delay) {
				_timerPool.getTimer().schedule(new DeathSyncTimer(), delay);
			}

			@Override
			public void run() {
				if (isDeathProcessing()) {
					schedule(getSleepTime());
					return;
				}
				allTargetClear();
				setAiRunning(false);
			}
		}

		@Override
		public void start() {
			setAiRunning(true);
			_timerPool.getTimer().schedule(NpcAITimerImpl.this, 0);
		}

		private void stop() {
			mobSkill.resetAllSkillUseCount();
			_timerPool.getTimer().schedule(new DeathSyncTimer(), 0);
		}

		private void schedule(int delay) {
			_timerPool.getTimer().schedule(new NpcAITimerImpl(), delay);
		}

		@Override
		public void run() {
			try {
				if (notContinued()) {
					stop();
					return;
				}

				if (0 < _paralysisTime) {
					schedule(_paralysisTime);
					_paralysisTime = 0;
					setParalyzed(false);
					return;
				} else if (isParalyzed() || isSleeped()) {
					schedule(200);
					return;
				}

				if (!AIProcess()) {
					schedule(getSleepTime());
					return;
				}
				stop();
			} catch (Exception e) {
				System.out.println("NPC ID : "+ getNpcTemplate().get_npcId());
				_log.log(Level.WARNING, "NpcAI에 예외가 발생했습니다.", e);
			}
		}

		private boolean notContinued() {
			return _destroyed || isDead() || getCurrentHp() <= 0
					|| getHiddenStatus() != HIDDEN_STATUS_NONE;
		}
	}
	// type: 2
	class NpcAIThreadImpl implements Runnable, NpcAI {
		@Override
		public void start() {
			GeneralThreadPool.getInstance().execute(NpcAIThreadImpl.this);
		}

		@Override
		public void run() {
			try {
				setAiRunning(true);
				while (!_destroyed && !isDead() && getCurrentHp() > 0
						&& getHiddenStatus() == HIDDEN_STATUS_NONE) {
					while (isParalyzed() || isSleeped()) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							setParalyzed(false);
						}
					}

					if (AIProcess()) {
						break;
					}
					try {
						Thread.sleep(getSleepTime());
					} catch (Exception e) {
						break;
					}
				}
				mobSkill.resetAllSkillUseCount();
				do {
					try {
						Thread.sleep(getSleepTime());
					} catch (Exception e) {
						break;
					}
				} while (isDeathProcessing());
				allTargetClear();
				setAiRunning(false);
			} catch (Exception e) {
				System.out.println("NPC ID : "+ getNpcTemplate().get_npcId());
				_log.log(Level.WARNING, "NpcAI에 예외가 발생했습니다.", e);
			}
		}
	}

	private boolean AIProcess() {
		setSleepTime(300);

		checkTarget();
		if (_target == null && _master == null) {
			searchTarget();
		}

		onItemUse();

		if (_target == null) {
			checkTargetItem();
			if (isPickupItem() && _targetItem == null) {
				searchTargetItem();
			}

			if (_targetItem == null) {
				if (noTarget()) {
					return true;
				}
			} else {
				// onTargetItem();
				L1Inventory groundInventory = L1World.getInstance().getInventory(_targetItem.getX(), _targetItem.getY(), _targetItem.getMapId());
				if (groundInventory.checkItem(_targetItem.getItemId())) {
					onTargetItem();
				} else {
					_targetItemList.remove(_targetItem);
					_targetItem = null;
					setSleepTime(1000);
					return false;
				}
			}
		} else {
			if (getHiddenStatus() == HIDDEN_STATUS_NONE) {
				onTarget();
			} else {
				return true;
			}
		}
		return false; 
	}

	public void onItemUse() {}

	public void searchTarget() {}

	public void checkTarget() {
		if (_target == null	|| _target.getMapId() != getMapId() || _target.isDead()
				|| _target.getCurrentHp() <= 0		
				|| (_target.isInvisble() && !getNpcTemplate().is_agrocoi() && !_hateList.containsKey(_target))) {

			if (_target != null) {
				tagertClear();
			}

			if (!_hateList.isEmpty()) {
				_target = _hateList.getMaxHateCharacter();
				checkTarget();
			}
		}
	}

	public void checkTargetItem() {
		if (_targetItem == null	|| _targetItem.getMapId() != getMapId()
				|| getLocation().getTileDistance(_targetItem.getLocation()) > 15) {
			if (!_targetItemList.isEmpty()) {
				_targetItem = _targetItemList.get(0);
				_targetItemList.remove(0);
				checkTargetItem();
			} else {
				_targetItem = null;
			}
		}
	}

	public void onTarget() {
		setActived(true);
		_targetItemList.clear();
		_targetItem = null;
		L1Character target = _target;
		int escapeDistance = 15;
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DARKNESS)
				|| getSkillEffectTimerSet().hasSkillEffect(L1SkillId.CURSE_BLIND))
			escapeDistance = 1;
		int calcx = (int) getLocation().getX() - _target.getLocation().getX();
		int calcy = (int) getLocation().getY() - _target.getLocation().getY();
		if (Math.abs(calcx) > escapeDistance
				|| Math.abs(calcy) > escapeDistance) {
			tagertClear();
			return;
		}
		if (getAtkspeed() == 0 && getPassispeed() > 0) {
			int dir = targetReverseDirection(target.getX(), target.getY());
			dir = checkObject(getX(), getY(), getMapId(), dir);
			setDirectionMove(dir);
			setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
		} else {
			if (CharPosUtil.isAttackPosition(this, target.getX(), target.getY(), getNpcTemplate().get_ranged())) {// 기본 공격범위
				if (mobSkill.isSkillTrigger(target)) {
					if (_random.nextInt(2) >= 1) {
						getMoveState().setHeading(CharPosUtil.targetDirection(this, target.getX(), target.getY()));
						attackTarget(target);
					} else {
						if (mobSkill.skillUse(target, true)) {
							setSleepTime(calcSleepTime(mobSkill.getSleepTime(), MAGIC_SPEED));
						} else { 
							getMoveState().setHeading(CharPosUtil.targetDirection(this, target.getX(), target.getY()));
							attackTarget(target);
						}
					}
				} else {
					getMoveState().setHeading(CharPosUtil.targetDirection(this, target.getX(), target.getY()));
					attackTarget(target);
				}
			} else {
				if (mobSkill.skillUse(target, true)) {// 확률적용
					setSleepTime(calcSleepTime(mobSkill.getSleepTime(), MAGIC_SPEED));
					return;
				}
				if (getPassispeed() > 0) {
					int distance = getLocation().getTileDistance(target.getLocation());
					if (firstFound == true && getNpcTemplate().is_teleport()
							&& distance > 3 && distance < 15) {
						if (nearTeleport(target.getX(), target.getY()) == true) {
							firstFound = false;
							return;
						}
					}

					if (getNpcTemplate().is_teleport()
							&& 20 > _random.nextInt(100)
							&& getCurrentMp() >= 10 && distance > 6
							&& distance < 15) {
						if (nearTeleport(target.getX(), target.getY()) == true) {
							return;
						}
					}
					int dir = moveDirection(target.getX(), target.getY());
					if (dir == -1) {
						tagertClear();
					} else {
						setDirectionMove(dir);
						setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
					}
				} else {
					tagertClear();
				}
			}
		}
	}

	public void setHate(L1Character cha, int hate) {
		if (cha != null && cha.getId() != getId()) {
			if (!isFirstAttack() && hate != 0) {
				// hate += 20; 
				hate += getMaxHp() / 10; 
				setFirstAttack(true);
			}

			_hateList.add(cha, hate);
			_dropHateList.add(cha, hate);
			_target = _hateList.getMaxHateCharacter();
			checkTarget();
		}
	}

	public void setLink(L1Character cha) {
	}

	public void serchLink(L1PcInstance targetPlayer, int family) {
		List<L1Object> targetKnownObjects = targetPlayer.getNearObjects().getKnownObjects();
		L1NpcInstance npc = null;
		L1MobGroupInfo mobGroupInfo = null;
		for (Object knownObject : targetKnownObjects) {
			if (knownObject instanceof L1NpcInstance) {
				npc = (L1NpcInstance) knownObject;
				if (npc.getNpcTemplate().get_agrofamily() > 0) {
					if (npc.getNpcTemplate().get_agrofamily() == 1) {
						if (npc.getNpcTemplate().get_family() == family) {
							npc.setLink(targetPlayer);
						}
					} else {
						npc.setLink(targetPlayer);
					}
				}
				mobGroupInfo = getMobGroupInfo();
				if (mobGroupInfo != null) {
					if (getMobGroupId() != 0 && getMobGroupId() == npc.getMobGroupId()) { 
						npc.setLink(targetPlayer);
					}
				}
			}
		}
	}

	public void attackTarget(L1Character target) {
		if (target instanceof L1PcInstance) {
			L1PcInstance player = (L1PcInstance) target;
			if (player.isTeleport()) { 
				return;
			}
		} else if (target instanceof L1PetInstance) {
			L1PetInstance pet = (L1PetInstance) target;
			L1Character cha = pet.getMaster();
			if (cha instanceof L1PcInstance) {
				L1PcInstance player = (L1PcInstance) cha;
				if (player.isTeleport()) { 
					return;
				}
			}
		} else if (target instanceof L1SummonInstance) {
			L1SummonInstance summon = (L1SummonInstance) target;
			L1Character cha = summon.getMaster();
			if (cha instanceof L1PcInstance) {
				L1PcInstance player = (L1PcInstance) cha;
				if (player.isTeleport()) {
					return;
				}
			}
		}
		if (this instanceof L1PetInstance) {
			L1PetInstance pet = (L1PetInstance) this;
			L1Character cha = pet.getMaster();
			if (cha instanceof L1PcInstance) {
				L1PcInstance player = (L1PcInstance) cha;
				if (player.isTeleport()) { 
					return;
				}
			}
		} else if (this instanceof L1SummonInstance) {
			L1SummonInstance summon = (L1SummonInstance) this;
			L1Character cha = summon.getMaster();
			if (cha instanceof L1PcInstance) {
				L1PcInstance player = (L1PcInstance) cha;
				if (player.isTeleport()) { 
					return;
				}
			}
		}

		if (target instanceof L1NpcInstance) {
			L1NpcInstance npc = (L1NpcInstance) target;
			if (npc.getHiddenStatus() != HIDDEN_STATUS_NONE) { 
				allTargetClear();
				return;
			}
		}

		boolean isCounterBarrier = false;
		boolean isMortalBody = false;
		L1Attack attack = new L1Attack(this, target);
		if (attack.calcHit()) {
			if (target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.COUNTER_BARRIER)) {
				L1Magic magic = new L1Magic(target, this);
				boolean isProbability = magic.calcProbabilityMagic(L1SkillId.COUNTER_BARRIER);
				boolean isShortDistance = attack.isShortDistance();
				if (isProbability && isShortDistance) {
					isCounterBarrier = true;
				}
			}else if (target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MORTAL_BODY)) {
				Random random = new Random();
				int MortalbodyChance = random.nextInt(100) + 1;  // 추가
				//L1Magic magic = new L1Magic(target, this);
				//boolean isProbability = magic.calcProbabilityMagic(L1SkillId.MORTAL_BODY);
				boolean isShortDistance = attack.isShortDistance();
				//if (isProbability && isShortDistance) {
				if (10 >= MortalbodyChance && isShortDistance) { 
					isMortalBody = true;
				}
			}
			if (!isCounterBarrier && !isMortalBody) {
				attack.calcDamage();
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
		setSleepTime(calcSleepTime(getAtkspeed(), ATTACK_SPEED));
	}


	public void searchTargetItem() {
		ArrayList<L1GroundInventory> gInventorys = new ArrayList<L1GroundInventory>();

		for (L1Object obj : L1World.getInstance().getVisibleObjects(this)) {
			if (obj != null && obj instanceof L1GroundInventory) {
				gInventorys.add((L1GroundInventory) obj);
			}
		}
		if (gInventorys.size() == 0) {
			return;
		}

		int pickupIndex = (int) (Math.random() * gInventorys.size());
		L1GroundInventory inventory = gInventorys.get(pickupIndex);
		for (L1ItemInstance item : inventory.getItems()) {
			if (getInventory().checkAddItem(item, item.getCount()) == L1Inventory.OK) { 
				_targetItem = item;
				_targetItemList.add(_targetItem);
			}
		}
	}

	public void searchItemFromAir() {
		ArrayList<L1GroundInventory> gInventorys = new ArrayList<L1GroundInventory>();

		for (L1Object obj : L1World.getInstance().getVisibleObjects(this)) {
			if (obj != null && obj instanceof L1GroundInventory) {
				gInventorys.add((L1GroundInventory) obj);
			}
		}
		if (gInventorys.size() == 0) {
			return;
		}

		int pickupIndex = (int) (Math.random() * gInventorys.size());
		L1GroundInventory inventory = gInventorys.get(pickupIndex);
		for (L1ItemInstance item : inventory.getItems()) {
			if (item.getItem().getType() == 6 // potion
					|| item.getItem().getType() == 7) { // food
				if (getInventory().checkAddItem(item, item.getCount()) == L1Inventory.OK) {
					if (getHiddenStatus() == HIDDEN_STATUS_FLY) {
						setHiddenStatus(HIDDEN_STATUS_NONE);
						Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Movedown));
						setActionStatus(0);
						Broadcaster.broadcastPacket(this, new S_NPCPack(this));
						onNpcAI();
						startChat(CHAT_TIMING_HIDE);
						_targetItem = item;
						_targetItemList.add(_targetItem);
					}
				}
			}
		}
	}

	public static void shuffle(L1Object[] arr) {
		for (int i = arr.length - 1; i > 0; i--) {
			int t = (int) (Math.random() * i);

			L1Object tmp = arr[i];
			arr[i] = arr[t];
			arr[t] = tmp;
		}
	}

	public void onTargetItem() {
		if (getLocation().getTileLineDistance(_targetItem.getLocation()) == 0) { 
			pickupTargetItem(_targetItem);
		} else { 
			int dir = moveDirection(_targetItem.getX(), _targetItem.getY());
			if (dir == -1) { 
				_targetItemList.remove(_targetItem);
				_targetItem = null;
			} else { 
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			}
		}
	}

	public void pickupTargetItem(L1ItemInstance targetItem) {
		L1Inventory groundInventory = L1World.getInstance().getInventory(
				targetItem.getX(), targetItem.getY(), targetItem.getMapId());
		L1ItemInstance item = groundInventory.tradeItem(targetItem, targetItem
				.getCount(), getInventory());
		light.turnOnOffLight();
		onGetItem(item);
		_targetItemList.remove(_targetItem);
		_targetItem = null;
		setSleepTime(1000);
	}

	public boolean noTarget() {
		if (_master != null && _master.getMapId() == getMapId()
				&& getLocation().getTileLineDistance(_master.getLocation()) > 2) { 
			int dir = moveDirection(_master.getX(), _master.getY());
			if (dir != -1) {
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			} else {
				return true;
			}
		} else {
			if (L1World.getInstance().getRecognizePlayer(this).size() == 0) {
				return true; 
			}

			if (_master == null && getPassispeed() > 0 && !isRest()) {

				//randomWalk();
				L1MobGroupInfo mobGroupInfo = getMobGroupInfo();
				if (mobGroupInfo == null || mobGroupInfo != null && mobGroupInfo.isLeader(this)) {
					if (_randomMoveDistance == 0) {
						_randomMoveDistance = _random.nextInt(5) + 1;
						_randomMoveDirection = _random.nextInt(20);
						if (getHomeX() != 0 && getHomeY() != 0
								&& _randomMoveDirection < 8
								&& _random.nextInt(3) == 0) {
							_randomMoveDirection = moveDirection(getHomeX(),
									getHomeY());
						}
					} else {
						_randomMoveDistance--;
					}
					int dir = checkObject(getX(), getY(), getMapId(), _randomMoveDirection);
					if (dir != -1) {
						setDirectionMove(dir);
						setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
					}
				} else {
					L1NpcInstance leader = mobGroupInfo.getLeader();
					if (getLocation().getTileLineDistance(leader.getLocation()) > 2) {
						int dir = moveDirection(leader.getX(), leader.getY());
						if (dir == -1) {
							return true;
						} else {
							setDirectionMove(dir);
							setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
						}
					}
				}
			}
		}
		return false;
	}

	public void onFinalAction(L1PcInstance pc, String s) {
	}


	public void tagertClear() {
		if (_target == null) {
			return;
		}
		_hateList.remove(_target);
		_target = null;
	}

	public void targetRemove(L1Character target) {
		_hateList.remove(target);
		if (_target != null && _target.equals(target)) {
			_target = null;
		}
	}

	public void allTargetClear() {
		_hateList.clear();
		_dropHateList.clear();
		_target = null;
		_targetItemList.clear();
		_targetItem = null;
	}

	public void setMaster(L1PcInstance cha) {	_master = cha;	}
	public L1Character getMaster() {	return _master;	}

	public void onNpcAI() {
	}

	// NPC타입 HTML 출력
	@Override
	public void onTalkAction(L1PcInstance player) {

		int objid = getId();
		L1NpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(getNpcTemplate().get_npcId());

		String htmlid = null;
		String[] htmldata = null;

		// html 표시 패킷 송신
		if (htmlid != null) { // htmlid가 지정되고 있는 경우
			if (htmldata != null) { // html 지정이 있는 경우는 표시
				player.sendPackets(new S_NPCTalkReturn(objid, htmlid,
						htmldata));
			} else {
				player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
			}
		} else {
			if (player.getLawful() < -1000) { // 플레이어가 카오틱
				player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
			} else {
				player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
			}
		}
	}

	public void refineItem() {
		int[] materials = null;
		int[] counts = null;
		int[] createitem = null;
		int[] createcount = null;

		if (_npcTemplate.get_npcId() == 45032) {
			if (getExp() != 0 && !_inventory.checkItem(20)) {
				materials = new int[] { 40508, 40521, 40045 };
				counts = new int[] { 150, 3, 3 };
				createitem = new int[] { 20 };
				createcount = new int[] { 1 };
				if (_inventory.checkItem(materials, counts)) {
					for (int i = 0; i < materials.length; i++) {
						_inventory.consumeItem(materials[i], counts[i]);
					}
					for (int j = 0; j < createitem.length; j++) {
						_inventory.storeItem(createitem[j], createcount[j]);
					}
				}
			}
			if (getExp() != 0 && !_inventory.checkItem(19)) {
				materials = new int[] { 40494, 40521 };
				counts = new int[] { 150, 3 };
				createitem = new int[] { 19 };
				createcount = new int[] { 1 };
				if (_inventory.checkItem(materials, counts)) {
					for (int i = 0; i < materials.length; i++) {
						_inventory.consumeItem(materials[i], counts[i]);
					}
					for (int j = 0; j < createitem.length; j++) {
						_inventory.storeItem(createitem[j], createcount[j]);
					}
				}
			}
			if (getExp() != 0 && !_inventory.checkItem(3)) {
				materials = new int[] { 40494, 40521 };
				counts = new int[] { 50, 1 };
				createitem = new int[] { 3 };
				createcount = new int[] { 1 };
				if (_inventory.checkItem(materials, counts)) {
					for (int i = 0; i < materials.length; i++) {
						_inventory.consumeItem(materials[i], counts[i]);
					}
					for (int j = 0; j < createitem.length; j++) {
						_inventory.storeItem(createitem[j], createcount[j]);
					}
				}
			}
			if (getExp() != 0 && !_inventory.checkItem(100)) {
				materials = new int[] { 88, 40508, 40045 };
				counts = new int[] { 4, 80, 3 };
				createitem = new int[] { 100 };
				createcount = new int[] { 1 };
				if (_inventory.checkItem(materials, counts)) {
					for (int i = 0; i < materials.length; i++) {
						_inventory.consumeItem(materials[i], counts[i]);
					}
					for (int j = 0; j < createitem.length; j++) {
						_inventory.storeItem(createitem[j], createcount[j]);
					}
				}
			}
			if (getExp() != 0 && !_inventory.checkItem(89)) {
				materials = new int[] { 88, 40494 };
				counts = new int[] { 2, 80 };
				createitem = new int[] { 89 };
				createcount = new int[] { 1 };
				if (_inventory.checkItem(materials, counts)) {
					for (int i = 0; i < materials.length; i++) {
						_inventory.consumeItem(materials[i], counts[i]);
					}
					L1ItemInstance item = null;
					for (int j = 0; j < createitem.length; j++) {
						item = _inventory.storeItem(createitem[j], createcount[j]);
						if (getNpcTemplate().get_digestitem() > 0) {
							setDigestItem(item);
						}
					}
				}
			}
		} else if (_npcTemplate.get_npcId() == 81069) { 
			if (getExp() != 0 && !_inventory.checkItem(40542)) {
				materials = new int[] { 40032 };
				counts = new int[] { 1 };
				createitem = new int[] { 40542 };
				createcount = new int[] { 1 };
				if (_inventory.checkItem(materials, counts)) {
					for (int i = 0; i < materials.length; i++) {
						_inventory.consumeItem(materials[i], counts[i]);
					}
					for (int j = 0; j < createitem.length; j++) {
						_inventory.storeItem(createitem[j], createcount[j]);
					}
				}
			}
		}
	}

	public void setParalysisTime(int ptime) {
		_paralysisTime = ptime;
	}

	public L1HateList getHateList() {
		return _hateList;
	}

	public int getParalysisTime() {
		return _paralysisTime;
	}

	public final void startHpRegeneration() {
		int hprInterval = getNpcTemplate().get_hprinterval();
		int hpr = getNpcTemplate().get_hpr();
		if (!_hprRunning && hprInterval > 0 && hpr > 0) {
			_hprTimer = new HprTimer(hpr);
			L1NpcRegenerationTimer.getInstance().scheduleAtFixedRate(_hprTimer, hprInterval, hprInterval);
			_hprRunning = true;
		}
	}

	public final void stopHpRegeneration() {
		if (_hprRunning) {
			_hprTimer.cancel();
			_hprRunning = false;
		}
	}

	public final void startMpRegeneration() {
		int mprInterval = getNpcTemplate().get_mprinterval();
		int mpr = getNpcTemplate().get_mpr();
		if (!_mprRunning && mprInterval > 0 && mpr > 0) {
			_mprTimer = new MprTimer(mpr);
			L1NpcRegenerationTimer.getInstance().scheduleAtFixedRate(_mprTimer, mprInterval, mprInterval);
			_mprRunning = true;
		}
	}

	public final void stopMpRegeneration() {
		if (_mprRunning) {
			_mprTimer.cancel();
			_mprRunning = false;
		}
	}

	private boolean _hprRunning = false;

	private HprTimer _hprTimer;

	class HprTimer extends TimerTask {
		@Override
		public void run() {
			try {
				if ((!_destroyed && !isDead()) && (getCurrentHp() > 0 && getCurrentHp() < getMaxHp())) {
					setCurrentHp(getCurrentHp() + _point);
				} else {
					cancel();
					_hprRunning = false;
				}
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}

		public HprTimer(int point) {
			if (point < 1) {
				point = 1;
			}
			_point = point;
		}

		private final int _point;
	}

	private boolean _mprRunning = false;

	private MprTimer _mprTimer;

	class MprTimer extends TimerTask {
		@Override
		public void run() {
			try {
				if ((!_destroyed && !isDead())
						&& (getCurrentHp() > 0 && getCurrentMp() < getMaxMp())) {
					setCurrentMp(getCurrentMp() + _point);
				} else {
					cancel();
					_mprRunning = false;
				}
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}

		public MprTimer(int point) {
			if (point < 1) {
				point = 1;
			}
			_point = point;
		}

		private final int _point;
	}

	class DigestItemTimer implements Runnable {
		@Override
		public void run() {
			_digestItemRunning = true;
			Object[] keys = null;
			L1ItemInstance digestItem = null;
			while (!_destroyed && _digestItems.size() > 0) {
				try {
					Thread.sleep(1000);
				} catch (Exception exception) {
					break;
				}

				keys = _digestItems.keySet().toArray();
				Integer key = null;
				Integer digestCounter = null;		
				for (int i = 0; i < keys.length; i++) {
					key = (Integer) keys[i];
					digestCounter = _digestItems.get(key);
					digestCounter -= 1;
					if (digestCounter <= 0) {
						_digestItems.remove(key);
						digestItem = getInventory().getItem(key);
						if (digestItem != null) {
							getInventory().removeItem(digestItem, digestItem.getCount());
						}
					} else {
						_digestItems.put(key, digestCounter);
					}
				}
			}
			_digestItemRunning = false;
		}
	}

	private int _passispeed;
	private int _atkspeed;
	private boolean _pickupItem;

	public int getPassispeed() {	return _passispeed;	}
	public void setPassispeed(int i) {	_passispeed = i;	}

	public int getAtkspeed() {	return _atkspeed;	}
	public void setAtkspeed(int i) {	_atkspeed = i;	}

	public boolean isPickupItem() {	return _pickupItem;	}
	public void setPickupItem(boolean flag) {	_pickupItem = flag;	}

	@Override
	public L1Inventory getInventory() {	return _inventory;	}

	public void setInventory(L1Inventory inventory) {	_inventory = inventory;	}

	public L1Npc getNpcTemplate() {	return _npcTemplate;	}

	public int getNpcId() {	return _npcTemplate.get_npcId();	}

	public void setPetcost(int i) {	_petcost = i;	}
	public int getPetcost() {	return _petcost;	}

	public void setSpawn(L1Spawn spawn) {	_spawn = spawn;	}
	public L1Spawn getSpawn() {	return _spawn;	}

	public void setSpawnNumber(int number) {	_spawnNumber = number;	}
	public int getSpawnNumber() {	return _spawnNumber;	}

	public void onDecay(boolean isReuseId) {
		int id = 0;
		if (isReuseId) {
			id = getId();
		} else {
			id = 0;
		}
		_spawn.executeSpawnTask(_spawnNumber, id);
	}

	/**by 판도라 드키본섭화**/
	private boolean portalActionCK = false;
	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		if (this == null || perceivedFrom == null)
			return;
		perceivedFrom.getNearObjects().addKnownObject(this);
		perceivedFrom.sendPackets(new S_NPCPack(this));
		/**by 판도라 드키본섭화**/
		if (getNpcTemplate().get_npcId() == 4212015 || getNpcTemplate().get_npcId() == 4212016) {
			if(!portalActionCK){
				perceivedFrom.sendPackets(new S_DoActionGFX(getId(), ActionCodes.ACTION_AxeWalk));
				for(L1PcInstance pc : perceivedFrom.getNearObjects().getKnownPlayers()){
					pc.sendPackets(new S_DoActionGFX(getId(), ActionCodes.ACTION_AxeWalk));
				}
				portalActionCK = true;
			}
		}
		/**by 판도라 드키본섭화**/
		onNpcAI();
	}

	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		allTargetClear();
		_master = null;
		L1World.getInstance().removeVisibleObject(this);
		L1World.getInstance().removeObject(this);
		List<L1PcInstance> players = L1World.getInstance().getRecognizePlayer(this);
		if (players.size() > 0) {
			S_RemoveObject s_deleteNewObject = new S_RemoveObject(this);
			for (L1PcInstance pc : players) {
				if (pc != null) {
					pc.getNearObjects().removeKnownObject(this);
					// if(!L1Character.distancepc(user, this))
					pc.sendPackets(s_deleteNewObject);
				}
			}
		}
		getNearObjects().removeAllKnownObjects();

		L1MobGroupInfo mobGroupInfo = getMobGroupInfo();
		if (mobGroupInfo == null) {
			if (isReSpawn()) {
				onDecay(true);
			}
		} else {
			if (mobGroupInfo.removeMember(this) == 0) {
				setMobGroupInfo(null);
				if (isReSpawn()) {
					onDecay(false);
				}
			}
		}
	}

	public void groupDeleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		allTargetClear();
		_master = null;
		L1World.getInstance().removeVisibleObject(this);
		L1World.getInstance().removeObject(this);
		List<L1PcInstance> players = L1World.getInstance().getRecognizePlayer(this);
		if (players.size() > 0) {
			S_RemoveObject s_deleteNewObject = new S_RemoveObject(this);
			for (L1PcInstance pc : players) {
				if (pc != null) {
					pc.getNearObjects().removeKnownObject(this);
					// if(!L1Character.distancepc(user, this))
					pc.sendPackets(s_deleteNewObject);
				}
			}
		}
		getNearObjects().removeAllKnownObjects();		
	}
	public void ReceiveManaDamage(L1Character attacker, int damageMp) {
	}

	public void receiveDamage(L1Character attacker, int damage) {
	}

	public void setDigestItem(L1ItemInstance item) {
		_digestItems.put(new Integer(item.getId()), new Integer(getNpcTemplate().get_digestitem()));
		if (!_digestItemRunning) {
			DigestItemTimer digestItemTimer = new DigestItemTimer();
			GeneralThreadPool.getInstance().execute(digestItemTimer);
		}
	}

	public void onGetItem(L1ItemInstance item) {
		refineItem();
		getInventory().shuffle();
		if (getNpcTemplate().get_digestitem() > 0) {
			setDigestItem(item);
		}
	}

	public void approachPlayer(L1PcInstance pc) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(60) || pc.getSkillEffectTimerSet().hasSkillEffect(97)) {
			return;
		}
		if (getHiddenStatus() == HIDDEN_STATUS_SINK) {
			if (getCurrentHp() == getMaxHp()) {
				if (pc.getLocation().getTileLineDistance(this.getLocation()) <= 2) {
					appearOnGround(pc);
				}
			}
		} else if (getHiddenStatus() == HIDDEN_STATUS_FLY) {
			if (getCurrentHp() == getMaxHp()) {
				if (pc.getLocation().getTileLineDistance(this.getLocation()) <= 1) {
					appearOnGround(pc);
				}
			} else {
				//if (getNpcTemplate().get_npcId() != 45681) {
				searchItemFromAir();
				//}
			}
		}

	}

	public void appearOnGround(L1PcInstance pc) {
		if (getHiddenStatus() == HIDDEN_STATUS_SINK) {
			setHiddenStatus(HIDDEN_STATUS_NONE);
			Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Appear));
			setActionStatus(0);
			Broadcaster.broadcastPacket(this, new S_NPCPack(this));
			if (!pc.getSkillEffectTimerSet().hasSkillEffect(60) && !pc.getSkillEffectTimerSet().hasSkillEffect(97) 
					&& !pc.isGm()) {
				_hateList.add(pc, 0);
				_target = pc;
			}
			onNpcAI(); 
		} else if (getHiddenStatus() == HIDDEN_STATUS_FLY) {
			setHiddenStatus(HIDDEN_STATUS_NONE);
			Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Movedown));
			setActionStatus(0);
			Broadcaster.broadcastPacket(this, new S_NPCPack(this));
			if (!pc.getSkillEffectTimerSet().hasSkillEffect(60) && !pc.getSkillEffectTimerSet().hasSkillEffect(97) 
					&& !pc.isGm()) {
				_hateList.add(pc, 0);
				_target = pc;
			}
			onNpcAI();
			startChat(CHAT_TIMING_HIDE);
		}
	}

	public void setDirectionMove(int dir) {
		if (dir >= 0) {
			int nx = 0;
			int ny = 0;

			int heading = 0;
			nx = HEADING_TABLE_X[dir];
			ny = HEADING_TABLE_Y[dir];
			heading = dir;

			getMoveState().setHeading(heading);
			getMap().setPassable(getLocation(), true);

			int nnx = getX() + nx;
			int nny = getY() + ny;
			setX(nnx);
			setY(nny);

			getMap().setPassable(getLocation(), false);

			Broadcaster.broadcastPacket(this, new S_MoveCharPacket(this));

			if (getMovementDistance() > 0) {
				if (this instanceof L1GuardInstance
						|| this instanceof L1CastleGuardInstance
						|| this instanceof L1MerchantInstance
						|| this instanceof L1MonsterInstance) {
					if (getLocation().getLineDistance(
							new Point(getHomeX(), getHomeY())) > getMovementDistance()) {
						teleport(getHomeX(), getHomeY(), getMoveState().getHeading());
					}
				}
			}
			if (getNpcTemplate().get_npcId() >= 45912
					&& getNpcTemplate().get_npcId() <= 45916) {
				if (getX() >= 32591 && getX() <= 32644
						&& getY() >= 32643 && getY() <= 32688
						&& getMapId() == 4) {
					teleport(getHomeX(), getHomeY(), getMoveState().getHeading());
				}
			}
		}
	}

	public int moveDirection(int x, int y) {
		return moveDirection(x, y, getLocation().getLineDistance(new Point(x, y)));
	}

	public int moveDirection(int x, int y, double d) { 
		int dir = 0;
		if (getSkillEffectTimerSet().hasSkillEffect(40) == true && d >= 2D) { 
			return -1;
		} else if (d > 30D) { 
			return -1;
		} else if (d > courceRange) {
			dir = CharPosUtil.targetDirection(this, x, y);
			dir = checkObject(getX(), getY(), getMapId(), dir);
		} else { 
			dir = _serchCource(x, y);
			if (dir == -1) { 
				dir = CharPosUtil.targetDirection(this, x, y);
				if (!isExsistCharacterBetweenTarget(dir)) {
					dir = checkObject(getX(), getY(), getMapId(), dir);
				}
			}
		}
		return dir;
	}

	private boolean isExsistCharacterBetweenTarget(int dir) {
		if (!(this instanceof L1MonsterInstance)) { 
			return false;
		}
		if (_target == null) { 
			return false;
		}

		int locX = getX();
		int locY = getY();
		int targetX = locX;
		int targetY = locY;

		switch(dir){
		case 1: targetX = locX + 1; targetY = locY - 1; break;
		case 2: targetX = locX + 1; break;
		case 3: targetX = locX + 1; targetY = locY + 1; break;
		case 4: targetY = locY + 1; break;
		case 5: targetX = locX - 1; targetY = locY + 1; break;
		case 6: targetX = locX - 1; break;
		case 7: targetX = locX - 1; targetY = locY - 1; break;
		case 0: targetY = locY - 1; break;
		default: break;
		}
		L1Character cha = null;
		L1PcInstance pc = null;

		for (L1Object object : L1World.getInstance().getVisibleObjects(this, 1)) {
			if (object instanceof L1PcInstance
					|| object instanceof L1SummonInstance
					|| object instanceof L1PetInstance) {
				cha = (L1Character) object;
				if (cha.getX() == targetX && cha.getY() == targetY
						&& cha.getMapId() == getMapId()) {
					if (object instanceof L1PcInstance) {
						pc = (L1PcInstance) object;
						if (pc.isGhost()) {
							continue;
						}
					}
					_hateList.add(cha, 0);
					_target = cha;
					return true;
				}
			}
		}
		return false;
	}

	public int targetReverseDirection(int tx, int ty) { 
		int dir = CharPosUtil.targetDirection(this, tx, ty);
		dir += 4;
		if (dir > 7) {
			dir -= 8;
		}
		return dir;
	}

	public static int checkObject(int x, int y, short m, int d) { 
		L1Map map = L1WorldMap.getInstance().getMap(m);
		switch(d){
		case 1:
			if (map.isPassable(x, y, 1)) {
				return 1;
			} else if (map.isPassable(x, y, 0)) {
				return 0;
			} else if (map.isPassable(x, y, 2)) {
				return 2;
			}
			break;
		case 2:
			if (map.isPassable(x, y, 2)) {
				return 2;
			} else if (map.isPassable(x, y, 1)) {
				return 1;
			} else if (map.isPassable(x, y, 3)) {
				return 3;
			}
			break;
		case 3:
			if (map.isPassable(x, y, 3)) {
				return 3;
			} else if (map.isPassable(x, y, 2)) {
				return 2;
			} else if (map.isPassable(x, y, 4)) {
				return 4;
			}
			break;
		case 4:
			if (map.isPassable(x, y, 4)) {
				return 4;
			} else if (map.isPassable(x, y, 3)) {
				return 3;
			} else if (map.isPassable(x, y, 5)) {
				return 5;
			}
			break;
		case 5:
			if (map.isPassable(x, y, 5)) {
				return 5;
			} else if (map.isPassable(x, y, 4)) {
				return 4;
			} else if (map.isPassable(x, y, 6)) {
				return 6;
			}
			break;
		case 6:
			if (map.isPassable(x, y, 6)) {
				return 6;
			} else if (map.isPassable(x, y, 5)) {
				return 5;
			} else if (map.isPassable(x, y, 7)) {
				return 7;
			}
			break;
		case 7:
			if (map.isPassable(x, y, 7)) {
				return 7;
			} else if (map.isPassable(x, y, 6)) {
				return 6;
			} else if (map.isPassable(x, y, 0)) {
				return 0;
			}
			break;
		case 0:
			if (map.isPassable(x, y, 0)) {
				return 0;
			} else if (map.isPassable(x, y, 7)) {
				return 7;
			} else if (map.isPassable(x, y, 1)) {
				return 1;
			}
			break;
		default:
			break;
		}
		return -1;
	}

	private int _serchCource(int x, int y) 
	{
		int i;
		int locCenter = courceRange + 1;
		int diff_x = x - locCenter; 
		int diff_y = y - locCenter; 
		int[] locBace = { getX() - diff_x, getY() - diff_y, 0, 0 };
		int[] locNext = new int[4];
		int[] locCopy;
		int[] dirFront = new int[5];
		boolean serchMap[][] = new boolean[locCenter * 2 + 1][locCenter * 2 + 1];
		LinkedList<int[]> queueSerch = new LinkedList<int[]>();

		for (int j = courceRange * 2 + 1; j > 0; j--) {
			for (i = courceRange - Math.abs(locCenter - j); i >= 0; i--) {
				serchMap[j][locCenter + i] = true;
				serchMap[j][locCenter - i] = true;
			}
		}

		int[] firstCource = { 2, 4, 6, 0, 1, 3, 5, 7 };
		for (i = 0; i < 8; i++) {
			System.arraycopy(locBace, 0, locNext, 0, 4);
			_moveLocation(locNext, firstCource[i]);
			if (locNext[0] - locCenter == 0 && locNext[1] - locCenter == 0) {
				return firstCource[i];
			}
			if (serchMap[locNext[0]][locNext[1]]) {
				int tmpX = locNext[0] + diff_x;
				int tmpY = locNext[1] + diff_y;
				boolean found = false;
				switch(i){
				case 0: found = getMap().isPassable(tmpX, tmpY + 1, i); break;
				case 1: found = getMap().isPassable(tmpX - 1, tmpY + 1, i); break;
				case 2: found = getMap().isPassable(tmpX - 1, tmpY, i); break;
				case 3: found = getMap().isPassable(tmpX - 1, tmpY - 1, i); break;
				case 4: found = getMap().isPassable(tmpX, tmpY - 1, i); break;
				case 5: found = getMap().isPassable(tmpX + 1, tmpY - 1, i); break;
				case 6: found = getMap().isPassable(tmpX + 1, tmpY, i); break;
				case 7: found = getMap().isPassable(tmpX + 1, tmpY + 1, i); break;
				default: break;
				}
				if (found)
				{
					locCopy = new int[4];
					System.arraycopy(locNext, 0, locCopy, 0, 4);
					locCopy[2] = firstCource[i];
					locCopy[3] = firstCource[i];
					queueSerch.add(locCopy);
				}
				serchMap[locNext[0]][locNext[1]] = false;
			}
		}
		locBace = null;

		while (queueSerch.size() > 0) {
			locBace = queueSerch.removeFirst();
			_getFront(dirFront, locBace[2]);
			for (i = 4; i >= 0; i--) {
				System.arraycopy(locBace, 0, locNext, 0, 4);
				_moveLocation(locNext, dirFront[i]);
				if (locNext[0] - locCenter == 0 && locNext[1] - locCenter == 0) {
					return locNext[3];
				}
				if (serchMap[locNext[0]][locNext[1]]) {
					int tmpX = locNext[0] + diff_x;
					int tmpY = locNext[1] + diff_y;
					boolean found = false;
					switch(i){
					case 0: found = getMap().isPassable(tmpX, tmpY + 1, i); break;
					case 1: found = getMap().isPassable(tmpX - 1, tmpY + 1, i); break;
					case 2: found = getMap().isPassable(tmpX - 1, tmpY, i); break;
					case 3: found = getMap().isPassable(tmpX - 1, tmpY - 1, i); break;
					case 4: found = getMap().isPassable(tmpX, tmpY - 1, i); break;
					default: break;
					}
					if (found)
					{
						locCopy = new int[4];
						System.arraycopy(locNext, 0, locCopy, 0, 4);
						locCopy[2] = dirFront[i];
						queueSerch.add(locCopy);
					}
					serchMap[locNext[0]][locNext[1]] = false;
				}
			}
			locBace = null;
		}
		return -1; 
	}

	private void _moveLocation(int[] ary, int d) {
		ary[0] = ary[0] + HEADING_TABLE_X[d];
		ary[1] = ary[1] + HEADING_TABLE_Y[d];
		ary[2] = d;
	}

	private void _getFront(int[] ary, int d) {
		switch(d){
		case 1:
			ary[4] = 2;
			ary[3] = 0;
			ary[2] = 1;
			ary[1] = 3;
			ary[0] = 7;
			break;
		case 2:
			ary[4] = 2;
			ary[3] = 4;
			ary[2] = 0;
			ary[1] = 1;
			ary[0] = 3;
			break;
		case 3:
			ary[4] = 2;
			ary[3] = 4;
			ary[2] = 1;
			ary[1] = 3;
			ary[0] = 5;
			break;
		case 4:
			ary[4] = 2;
			ary[3] = 4;
			ary[2] = 6;
			ary[1] = 3;
			ary[0] = 5;
			break;
		case 5:
			ary[4] = 4;
			ary[3] = 6;
			ary[2] = 3;
			ary[1] = 5;
			ary[0] = 7;
			break;
		case 6:
			ary[4] = 4;
			ary[3] = 6;
			ary[2] = 0;
			ary[1] = 5;
			ary[0] = 7;
			break;
		case 7:
			ary[4] = 6;
			ary[3] = 0;
			ary[2] = 1;
			ary[1] = 5;
			ary[0] = 7;
			break;
		case 0:
			ary[4] = 2;
			ary[3] = 6;
			ary[2] = 0;
			ary[1] = 1;
			ary[0] = 7;
			break;
		default:
			break;
		}
	}


	private void useHealPotion(int healHp, int effectId) {
		Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), effectId));
		if (this.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.POLLUTE_WATER)) { 
			healHp /= 2;
		}
		if (this instanceof L1PetInstance) {
			((L1PetInstance) this).setCurrentHp(getCurrentHp() + healHp);
		} else if (this instanceof L1SummonInstance) {
			((L1SummonInstance) this).setCurrentHp(getCurrentHp() + healHp);
		} else {
			setCurrentHp(getCurrentHp() + healHp);
		}
	}

	public void useHastePotion(int time) {
		Broadcaster.broadcastPacket(this, new S_SkillHaste(getId(), 1, time));
		Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 191));
		getMoveState().setMoveSpeed(1);
		getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_HASTE, time * 1000);
	}

	public static final int USEITEM_HEAL = 0;
	public static final int USEITEM_HASTE = 1;
	public static int[] healPotions = { POTION_OF_GREATER_HEALING,
		POTION_OF_EXTRA_HEALING, POTION_OF_HEALING };
	public static int[] haestPotions = { B_POTION_OF_GREATER_HASTE_SELF,
		POTION_OF_GREATER_HASTE_SELF, B_POTION_OF_HASTE_SELF,
		POTION_OF_HASTE_SELF };

	public void useItem(int type, int chance) { 
		if (getSkillEffectTimerSet().hasSkillEffect(71)) {
			return; 
		}

		Random random = new Random();
		if (random.nextInt(100) > chance) {
			return; 
		}

		if (type == USEITEM_HEAL) { 
			if (getInventory().consumeItem(POTION_OF_GREATER_HEALING, 1)) {
				useHealPotion(75, 197);
			} else if (getInventory().consumeItem(POTION_OF_EXTRA_HEALING, 1)) {
				useHealPotion(45, 194);
			} else if (getInventory().consumeItem(POTION_OF_HEALING, 1)) {
				useHealPotion(15, 189);
			}
		} else if (type == USEITEM_HASTE) { 
			if (getSkillEffectTimerSet().hasSkillEffect(1001)) {
				return; 
			}

			if (getInventory().consumeItem(B_POTION_OF_GREATER_HASTE_SELF, 1)) {
				useHastePotion(2100);
			} else if (getInventory().consumeItem(POTION_OF_GREATER_HASTE_SELF,
					1)) {
				useHastePotion(1800);
			} else if (getInventory().consumeItem(B_POTION_OF_HASTE_SELF, 1)) {
				useHastePotion(350);
			} else if (getInventory().consumeItem(POTION_OF_HASTE_SELF, 1)) {
				useHastePotion(300);
			}
		}
	}
	public void sendPackets(ServerBasePacket serverbasepacket) {
		if (getNetConnection() == null) {
			return;
		}

		try {
			getNetConnection().sendPacket(serverbasepacket);
		} catch (Exception e) {
		}
	}  //추가


	public boolean nearTeleport(int nx, int ny) {
		int rdir = _random.nextInt(8);
		int dir;
		for (int i = 0; i < 8; i++) {
			dir = rdir + i;
			if (dir > 7) {
				dir -= 8;
			}
			nx += HEADING_TABLE_X[dir];
			ny += HEADING_TABLE_Y[dir];

			if (getMap().isPassable(nx, ny)) {
				dir += 4;
				if (dir > 7) {
					dir -= 8;
				}
				teleport(nx, ny, dir);
				setCurrentMp(getCurrentMp() - 10);
				return true;
			}
		}
		return false;
	}

	public void teleport(int nx, int ny, int dir) {
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.sendPackets(new S_SkillSound(getId(), 169));
			pc.sendPackets(new S_RemoveObject(this));
			pc.getNearObjects().removeKnownObject(this);
		}
		setX(nx);
		setY(ny);
		getMoveState().setHeading(dir);
	}

	// ----------From L1Character-------------
	private String _nameId; 
	private boolean _Agro; 
	private boolean _Agrocoi; 
	private boolean _Agrososc; 
	private int _homeX; 
	private int _homeY; 
	private boolean _reSpawn; 
	private int _lightSize; 
	private boolean _weaponBreaked; 
	private int _hiddenStatus; 
	private int _movementDistance = 0;
	private int _tempLawful = 0;

	public String getNameId() {	return _nameId;	}
	public void setNameId(String s) {	_nameId = s;	}

	public boolean isAgro() {	return _Agro;	}
	public void setAgro(boolean flag) {	_Agro = flag;	}

	public boolean isAgrocoi() {	return _Agrocoi;	}
	public void setAgrocoi(boolean flag) {	_Agrocoi = flag;	}

	public boolean isAgrososc() {	return _Agrososc;	}
	public void setAgrososc(boolean flag) {	_Agrososc = flag;	}

	public int getHomeX() {	return _homeX;	}
	public void setHomeX(int i) {	_homeX = i;	}

	public int getHomeY() {	return _homeY;	}
	public void setHomeY(int i) {	_homeY = i;	}

	public boolean isReSpawn() {	return _reSpawn;	}
	public void setRespawn(boolean flag) {	_reSpawn = flag;	}

	public int getLightSize() {	return _lightSize;	}
	public void setLightSize(int i) {	_lightSize = i;	}

	public boolean isWeaponBreaked() {	return _weaponBreaked;	}
	public void setWeaponBreaked(boolean flag) {	_weaponBreaked = flag;	}

	public int getHiddenStatus() {	return _hiddenStatus;	}
	public void setHiddenStatus(int i) {	_hiddenStatus = i;	}

	public int getMovementDistance() {	return _movementDistance;	}
	public void setMovementDistance(int i) {	_movementDistance = i;	}

	public int getTempLawful() {	return _tempLawful;	}
	public void setTempLawful(int i) {	_tempLawful = i;	}

	protected int calcSleepTime(int sleepTime, int type) {
		switch (getMoveState().getMoveSpeed()) {
		case 0:  break;
		case 1:  sleepTime -= (sleepTime * 0.25); break;
		case 2:  sleepTime *= 2; break;
		}
		if (getMoveState().getBraveSpeed() == 1) {
			sleepTime -= (sleepTime * 0.25);
		}
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.WIND_SHACKLE)) {
			if (type == ATTACK_SPEED || type == MAGIC_SPEED) {
				sleepTime += (sleepTime * 0.25);
			}
		}
		return sleepTime;
	}

	protected void setAiRunning(boolean aiRunning) {	_aiRunning = aiRunning;	}
	protected boolean isAiRunning() {	return _aiRunning;	}

	protected void setActived(boolean actived) {	_actived = actived;	}
	protected boolean isActived() {	return _actived;	}

	protected void setFirstAttack(boolean firstAttack) {	_firstAttack = firstAttack;	}
	protected boolean isFirstAttack() {	return _firstAttack;	}

	protected void setSleepTime(int sleep_time) {	_sleep_time = sleep_time;	}
	protected int getSleepTime() {	return _sleep_time;	}

	protected void setDeathProcessing(boolean deathProcessing) {	_deathProcessing = deathProcessing;	}
	protected boolean isDeathProcessing() {	return _deathProcessing;	}

	public int drainMana(int drain) {
		if (_drainedMana >= Config.MANA_DRAIN_LIMIT_PER_NPC) {
			return 0;
		}
		int result = Math.min(drain, getCurrentMp());
		if (_drainedMana + result > Config.MANA_DRAIN_LIMIT_PER_NPC) {
			result = Config.MANA_DRAIN_LIMIT_PER_NPC - _drainedMana;
		}
		_drainedMana += result;
		return result;
	}

	public boolean _destroyed = false;

	protected void transform(int transformId) {
		stopHpRegeneration();
		stopMpRegeneration();
		int transformGfxId = getNpcTemplate().getTransformGfxId();
		if (transformGfxId != 0) {
			Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), transformGfxId));
		}
		L1Npc npcTemplate = NpcTable.getInstance().getTemplate(transformId);
		setting_template(npcTemplate);

		Broadcaster.broadcastPacket(this, new S_ChangeShape(getId(), getGfxId().getTempCharGfx()));
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}
	}

	public void setRest(boolean _rest) {	this._rest = _rest;	}
	public boolean isRest() {	return _rest;	}

	public boolean isResurrect() {	return _isResurrect;	}
	public void setResurrect(boolean flag) {	_isResurrect = flag;	}

	@Override
	public synchronized void resurrect(int hp) {
		if (_destroyed) {
			return;
		}
		if (_deleteTask != null) {
			if (!_future.cancel(false)) { 
				return;
			}
			_deleteTask = null;
			_future = null;
		}
		super.resurrect(hp);
		startHpRegeneration();
		startMpRegeneration();
		L1SkillUse skill = new L1SkillUse();
		skill.handleCommands(null, L1SkillId.CANCELLATION, getId(), getX(),
				getY(), null, 0, L1SkillUse.TYPE_LOGIN, this);
	}

	protected synchronized void startDeleteTimer() {
		if (_deleteTask != null) {
			return;
		}
		_deleteTask = new DeleteTimer(getId());
		_future = GeneralThreadPool.getInstance().schedule(_deleteTask, DELETE_TIME);
	}

	protected static class DeleteTimer extends TimerTask {
		private int _id;

		protected DeleteTimer(int oId) {
			_id = oId;
			if (!(L1World.getInstance().findObject(_id) instanceof L1NpcInstance)) {
				throw new IllegalArgumentException("allowed only L1NpcInstance");
			}
		}

		@Override
		public void run() {
			L1NpcInstance npc = (L1NpcInstance) L1World.getInstance().findObject(_id);
			if (npc == null || !npc.isDead() || npc._destroyed) {
				return; 
			}
			try {
				npc.deleteMe();
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}

	public boolean isInMobGroup() {
		return getMobGroupInfo() != null;
	}
	public L1MobGroupInfo getMobGroupInfo() {	return _mobGroupInfo;	}
	public void setMobGroupInfo(L1MobGroupInfo m) {	_mobGroupInfo = m;	}

	public int getMobGroupId() {	return _mobGroupId;	}
	public void setMobGroupId(int i) {	_mobGroupId = i;	}

	public void startChat(int chatTiming) {
		if (chatTiming == CHAT_TIMING_APPEARANCE && this.isDead()) {
			return;
		}
		if (chatTiming == CHAT_TIMING_DEAD && !this.isDead()) {
			return;
		}
		if (chatTiming == CHAT_TIMING_HIDE && this.isDead()) {
			return;
		}
		if (chatTiming == CHAT_TIMING_GAME_TIME && this.isDead()) {
			return;
		}		

		int npcId = this.getNpcTemplate().get_npcId();
		L1NpcChat npcChat = null;
		switch(chatTiming){
		case CHAT_TIMING_APPEARANCE: npcChat = NpcChatTable.getInstance().getTemplateAppearance(npcId); break;
		case CHAT_TIMING_DEAD: npcChat = NpcChatTable.getInstance().getTemplateDead(npcId); break;
		case CHAT_TIMING_HIDE: npcChat = NpcChatTable.getInstance().getTemplateHide(npcId); break;
		case CHAT_TIMING_GAME_TIME: npcChat = NpcChatTable.getInstance().getTemplateGameTime(npcId); break;
		default: break;
		}
		if (npcChat == null) {
			return;
		}

		Timer timer = new Timer(true);
		L1NpcChatTimer npcChatTimer = new L1NpcChatTimer(this, npcChat);
		if (!npcChat.isRepeat()) {
			timer.schedule(npcChatTimer, npcChat.getStartDelayTime());
		} else {
			timer.scheduleAtFixedRate(npcChatTimer, npcChat.getStartDelayTime(),
					npcChat.getRepeatInterval());
		}
	}

	public void set_num(int num) {	this.num = num;	}
	public int get_num() {	return num;	}

	@SuppressWarnings("unused")
	private void randomWalk() {
		tagertClear();
		int dir = checkObject(getX(), getY(), getMapId(), _random.nextInt(20));
		if (dir != -1) {
			setDirectionMove(dir);
			setSleepTime(calcSleepTime(getPassispeed()));
		}
	}
	public int calcSleepTime(int i) {
		int sleepTime = i;
		switch (getMoveState().getMoveSpeed()) {
		case 0: break;
		case 1: sleepTime -= (sleepTime * 0.25); break;
		case 2: sleepTime *= 2; break;
		}
		if (getMoveState().getBraveSpeed() == 1) {
			sleepTime -= (sleepTime * 0.25);
		}
		return sleepTime;
	}
	public LineageClient getNetConnection() { return _netConnection; }
	public void setNetConnection(LineageClient clientthread) { _netConnection = clientthread; }





}
