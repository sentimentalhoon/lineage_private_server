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

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.ActionCodes;
import l1j.server.server.ObjectIdFactory;
import l1j.server.server.datatables.MobSkillTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_NPCPack;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1MobSkill;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.L1SpawnUtil;

public class L1MobSkillUse {
	private static Logger _log = Logger.getLogger(L1MobSkillUse.class.getName());

	private L1MobSkill _mobSkillTemplate = null;

	private L1NpcInstance _attacker = null;

	private L1Character _target = null;

	private Random _rnd = new Random(System.nanoTime());

	private int _sleepTime = 0;

	private int _skillUseCount[];
	private int ia;
	private int randcount = _rnd.nextInt(2)+1;
	private L1MonsterInstance mob = null;
	Timestamp dt = null;

	public L1MobSkillUse(L1NpcInstance npc) {
		_sleepTime = 0;

		_mobSkillTemplate = MobSkillTable.getInstance().getTemplate(npc.getNpcTemplate().get_npcId());
		if (_mobSkillTemplate == null) {
			return;
		}
		_attacker = npc;
		_skillUseCount = new int[getMobSkillTemplate().getSkillSize()];
	}

	private int getSkillUseCount(int idx) {
		return _skillUseCount[idx];
	}

	private void skillUseCountUp(int idx) {
		_skillUseCount[idx]++;
	}

	public void resetAllSkillUseCount() {
		if (getMobSkillTemplate() == null) {
			return;
		}

		for (int i = 0; i < getMobSkillTemplate().getSkillSize(); i++) {
			_skillUseCount[i] = 0;
		}
	}

	public int getSleepTime() {
		return _sleepTime;
	}

	public void setSleepTime(int i) {
		_sleepTime = i;
	}

	public L1MobSkill getMobSkillTemplate() {
		return _mobSkillTemplate;
	}

	public boolean isSkillTrigger(L1Character tg) {
		if (_mobSkillTemplate == null) {
			return false;
		}
		_target = tg;

		int type;
		type = getMobSkillTemplate().getType(0);

		if (type == L1MobSkill.TYPE_NONE) {
			return false;
		}

		int i = 0;
		for (i = 0; i < getMobSkillTemplate().getSkillSize() && getMobSkillTemplate().getType(i) != L1MobSkill.TYPE_NONE; i++) {

			int changeType = getMobSkillTemplate().getChangeTarget(i);
			if (changeType > 0) {
				_target = changeTarget(changeType, i);
			} else {
				_target = tg;
			}

			if (isSkillUseble(i, false)) {
				return true;
			}
		}
		return false;
	}


	public boolean skillUse(L1Character tg, boolean isTriRnd) {
		if (_mobSkillTemplate == null) {
			return false;
		}
		_target = tg;

		int type;
		type = getMobSkillTemplate().getType(0);

		if (type == L1MobSkill.TYPE_NONE) {
			return false;
		}
		int[] skills = null;
		int skillSizeCounter = 0;
		int skillSize = getMobSkillTemplate().getSkillSize();
		if (skillSize >= 0) {
			skills = new int[skillSize];
		}
		int i = 0;
		for (i = 0; i < getMobSkillTemplate().getSkillSize()
				&& getMobSkillTemplate().getType(i) != L1MobSkill.TYPE_NONE; i++) {

			int changeType = getMobSkillTemplate().getChangeTarget(i);
			if (changeType > 0) {
				_target = changeTarget(changeType, i);
			} else {

				_target = tg;
			}

			if (isSkillUseble(i, isTriRnd) == false) {
				continue;
			} else {
				skills[skillSizeCounter] = i;
				skillSizeCounter++;
			}			
		}
		if (skillSizeCounter != 0) {
			int num = _rnd.nextInt(skillSizeCounter);
			if (useSkill(skills[num])) {
				return true;
			}
		}
		return false;
	}
	private boolean useSkill(int i) {
		boolean isUseSkill = false;
		int type = getMobSkillTemplate().getType(i);
		switch(type){
		case L1MobSkill.TYPE_PHYSICAL_ATTACK:
			if (physicalAttack(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
			break;
		case L1MobSkill.TYPE_MAGIC_ATTACK:
			if (magicAttack(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
			break;
		case L1MobSkill.TYPE_SUMMON:
			if (summon(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
			break;
		case L1MobSkill.TYPE_POLY:
			if (poly(i) == true) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
			break;
			/* ��Ǫ ���� ��ų ����
		case L1MobSkill.TYPE_PAPOO:
			if(papoo() == true){
				skillUseCountUp(i);
				isUseSkill = true;
			}
			break;*/
		}  
		return isUseSkill;
	}
	/* ��Ǫ ���� ��ų ����
	private boolean papoo(){
		int chance = _rnd.nextInt(100)+1;

		SetAction();
		if(chance < 90){
			SummonMarble();
			SummonSael();
		}
		if(chance <70){
			SummonMarble2();
			SummonSael2();
		}
		if(chance < 60){
			SummonTorr();
		}
		if(_target.marble2.size()>0)
		{
			SetHaste();
		}
		return true;
	}
	private void SummonSael(){
		if(_target.sael.size() == 0){
			_target.sael.add("�翤");
			//mobspawn(4039004,1);
			L1SpawnUtil.spawn3(_attacker, 4039004, 6, 30*1000, false);
		}
	}
	private void SummonSael2(){
		if(_target.sael2.size() == 0){
			_target.sael2.add("�翤2");
			//mobspawn(4039004,1);
			L1SpawnUtil.spawn3(_attacker, 4039005, 6, 30*1000, false);
		}
	}
	private void SummonMarble(){
		if(_target.marble.size()<6){
			mobspawn(4039001,1);
			_target.sael.remove("�翤");
		}
	}

	private void SummonMarble2(){
		if(_target.marble2.size()<6){

			mobspawn(4039002,1);
			_target.sael2.remove("�翤2");
		}
	}


	private void SummonTorr(){
		dt = new Timestamp(System.currentTimeMillis() + 10000);// 10
		if(_target.tro.size()<6){
			//mobspawn(4039003,1);
			L1SpawnUtil.spawn3(_attacker, 4039003, 6, 10*1000, false);
		}
		setTorrTime(dt);
		System.out.println("mob : "+mob);
		System.out.println("attacker :"+_attacker);
	}

	public void setTorrTime(Timestamp t) { 
		dt = t; 
	} 
	private Timestamp getTorrTime(){
		return dt;
	}
	private void SetAction(){
		_attacker.sendPackets(new S_SkillSound(_attacker.getId(), 761));
		Broadcaster.broadcastPacket(_attacker, new S_SkillSound(_attacker.getId(), 761));
		Broadcaster.broadcastPacket(_attacker, new S_NpcChatPacket(_attacker, "����Ÿ! �Ƿ��̳�", 0));
	}
	private void SetHaste(){
		int time;
		time = 32766; 
		_attacker.sendPackets(new S_SkillHaste(_attacker.getId(), 1, time));
		Broadcaster.broadcastPacket(_attacker, new S_SkillHaste(_attacker.getId(), 1, 0));
		_attacker.sendPackets(new S_SkillSound(_attacker.getId(), 191));
		Broadcaster.broadcastPacket(_attacker, new S_SkillSound(_attacker.getId(), 191));
		_attacker.getMoveState().setMoveSpeed(1);
	}*/

	private boolean summon(int idx) {
		int summonId = getMobSkillTemplate().getSummon(idx);
		int min = getMobSkillTemplate().getSummonMin(idx);
		int max = getMobSkillTemplate().getSummonMax(idx);
		int count = 0;

		if (summonId == 0) {
			return false;
		}

		count = _rnd.nextInt(max) + min;
		mobspawn(summonId, count);

		Broadcaster.broadcastPacket(_attacker, new S_SkillSound(_attacker.getId(), 761));

		S_DoActionGFX gfx = new S_DoActionGFX(_attacker.getId(), ActionCodes.ACTION_SkillBuff);
		Broadcaster.broadcastPacket(_attacker, gfx);

		_sleepTime = _attacker.getNpcTemplate().getSubMagicSpeed();
		return true;
	}

	private boolean poly(int idx) {
		int polyId = getMobSkillTemplate().getPolyId(idx);
		boolean usePoly = false;

		if (polyId == 0) {
			return false;
		}

		for (L1PcInstance pc : L1World.getInstance()
				.getVisiblePlayer(_attacker)) {
			if (pc.isDead()) {
				continue;
			}
			if (pc.isGhost()) {
				continue;
			}
			if (pc.isGmInvis()) {
				continue;
			}
			if (CharPosUtil.glanceCheck(_attacker, pc.getX(), pc.getY()) == false) {
				continue;
			}

			int npcId = _attacker.getNpcTemplate().get_npcId();
			switch (npcId) {
			case 450001799: 
			case 81082: pc.getInventory().takeoffEquip(945); break;
			default: break;
			}
			L1PolyMorph.doPoly(pc, polyId, 1800, L1PolyMorph.MORPH_BY_NPC);

			usePoly = true;
		}
		if (usePoly) {
			for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(_attacker)) {
				pc.sendPackets(new S_SkillSound(pc.getId(), 230));
				Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 230));
				break;
			}
			S_DoActionGFX gfx = new S_DoActionGFX(_attacker.getId(),
					ActionCodes.ACTION_SkillBuff);
			Broadcaster.broadcastPacket(_attacker, gfx);

			_sleepTime = _attacker.getNpcTemplate().getSubMagicSpeed();
		}

		return usePoly;
	}

	private boolean magicAttack(int idx) {
		L1SkillUse skillUse = new L1SkillUse();
		int skillid = getMobSkillTemplate().getSkillId(idx);
		boolean canUseSkill = false;		
		if (_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SILENCE)){
			return false;
		}
		if(skillid == 43) {
			if (_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.HASTE)){
				return false;
			}
			_target = _attacker;
		}		
		if (skillid > 0) {
			canUseSkill = skillUse.checkUseSkill(null, skillid, _target.getId(), _target.getX(), 
					_target.getY(), null, 0, L1SkillUse.TYPE_NORMAL, _attacker);
		}		
		if (canUseSkill == true) {
			if (getMobSkillTemplate().getLeverage(idx) > 0) {
				skillUse.setLeverage(getMobSkillTemplate().getLeverage(idx));
			}
			skillUse.handleCommands(null, skillid, _target.getId(), _target.getX(), 
					_target.getY(), null, 0, L1SkillUse.TYPE_NORMAL, _attacker);
			L1Skills skill = SkillsTable.getInstance().getTemplate(skillid);
			if (skill.getTarget().equals("attack") && skillid != 18) { 
				_sleepTime = _attacker.getNpcTemplate().getAtkMagicSpeed();
			} else { 
				_sleepTime = _attacker.getNpcTemplate().getSubMagicSpeed();
			}

			return true;
		}
		return false;
	}

	private boolean physicalAttack(int idx) {
		Map<Integer, Integer> targetList = new ConcurrentHashMap<Integer, Integer>();
		int areaWidth = getMobSkillTemplate().getAreaWidth(idx);
		int areaHeight = getMobSkillTemplate().getAreaHeight(idx);
		int range = getMobSkillTemplate().getRange(idx);
		int actId = getMobSkillTemplate().getActid(idx);
		int gfxId = getMobSkillTemplate().getGfxid(idx);

		if (_attacker.getLocation().getTileLineDistance(_target.getLocation()) > range) {
			return false;
		}

		if (!CharPosUtil.glanceCheck(_attacker, _target.getX(), _target.getY())) {
			return false;
		}

		_attacker.getMoveState().setHeading(CharPosUtil.targetDirection(_attacker, _target.getX(), _target.getY()));

		if (areaHeight > 0) {
			L1Character cha  = null;
			for (L1Object obj : L1World.getInstance()
					.getVisibleBoxObjects(_attacker, _attacker.getMoveState().getHeading(),
							areaWidth, areaHeight)) {
				if (!(obj instanceof L1Character)) { 
					continue;
				}

				cha = (L1Character) obj;
				if (cha.isDead()) {
					continue;
				}

				if (cha instanceof L1PcInstance) {
					if (((L1PcInstance) cha).isGhost()) {
						continue;
					}
				}

				if(cha instanceof L1PcInstance &&
						_attacker instanceof L1SummonInstance
						|| _attacker instanceof L1PetInstance) {
					if(cha.getId() == _attacker.getMaster().getId()){
						continue;
					}
					if(CharPosUtil.getZoneType(cha) == 1){
						continue;
					}
				}

				if (!CharPosUtil.glanceCheck(_attacker, cha.getX(), cha.getY())) {
					continue;
				}

				if (_target instanceof L1PcInstance
						|| _target instanceof L1SummonInstance
						|| _target instanceof L1PetInstance) {
					if (obj instanceof L1PcInstance
							&& !((L1PcInstance) obj).isGhost()
							&& !((L1PcInstance) obj).isGmInvis()
							|| obj instanceof L1SummonInstance
							|| obj instanceof L1PetInstance) {
						targetList.put(obj.getId(), 0);
					}
				} else {
					if (obj instanceof L1MonsterInstance) {
						targetList.put(obj.getId(), 0);
					}
				}
			}
		} else {
			targetList.put(_target.getId(), 0);
		}

		if (targetList.size() == 0) {
			return false;
		}

		Iterator<Integer> ite = targetList.keySet().iterator();
		L1Attack attack  = null;
		while (ite.hasNext()) {
			int targetId = ite.next();
			attack = new L1Attack(_attacker, (L1Character) L1World
					.getInstance().findObject(targetId));
			if (attack.calcHit()) {
				if (getMobSkillTemplate().getLeverage(idx) > 0) {
					attack.setLeverage(getMobSkillTemplate().getLeverage(idx));
				}
				attack.calcDamage();
			}
			if (actId > 0) {
				attack.setActId(actId);
			}
			if (targetId == _target.getId()) {
				if (gfxId > 0) {
					Broadcaster.broadcastPacket(_attacker, new S_SkillSound(_attacker.getId(), gfxId));
				}
				attack.action();
			}
			attack.commit();
		}

		_sleepTime = _attacker.getAtkspeed();
		return true;
	}

	private boolean isSkillUseble(int skillIdx, boolean isTriRnd) {
		boolean useble = false;
		int type = getMobSkillTemplate().getType(skillIdx);

		if (isTriRnd || type == L1MobSkill.TYPE_SUMMON || type == L1MobSkill.TYPE_POLY) {
			if (getMobSkillTemplate().getTriggerRandom(skillIdx) > 0) {
				int chance = _rnd.nextInt(100) + 1;
				if (chance < getMobSkillTemplate().getTriggerRandom(skillIdx)) {					
					useble = true;					
				} else {
					return false;
				}
			}
		}

		if (getMobSkillTemplate().getTriggerHp(skillIdx) > 0) {
			int hpRatio = (_attacker.getCurrentHp() * 100) / _attacker.getMaxHp();
			if (hpRatio <= getMobSkillTemplate().getTriggerHp(skillIdx)) {
				useble = true;
			} else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerCompanionHp(skillIdx) > 0) {
			L1NpcInstance companionNpc = searchMinCompanionHp();
			if (companionNpc == null) {
				return false;
			}

			int hpRatio = (companionNpc.getCurrentHp() * 100) / companionNpc.getMaxHp();
			if (hpRatio <= getMobSkillTemplate().getTriggerCompanionHp(skillIdx)) {
				useble = true;
				_target = companionNpc;
			} else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerRange(skillIdx) != 0) {
			int distance = _attacker.getLocation().getTileLineDistance(_target.getLocation());

			if (getMobSkillTemplate().isTriggerDistance(skillIdx, distance)) {
				useble = true;
			} else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerCount(skillIdx) > 0) {
			if (getSkillUseCount(skillIdx) < getMobSkillTemplate().getTriggerCount(skillIdx)) {
				useble = true;
			} else {
				return false;
			}
		}
		return useble;
	}

	private L1NpcInstance searchMinCompanionHp() {
		L1NpcInstance npc;
		L1NpcInstance minHpNpc = null;
		int hpRatio = 100;
		int companionHpRatio;
		int family = _attacker.getNpcTemplate().get_family();

		for (L1Object object : L1World.getInstance().getVisibleObjects(_attacker)) {
			if (object instanceof L1NpcInstance) {
				npc = (L1NpcInstance) object;
				if (npc.getNpcTemplate().get_family() == family) {
					companionHpRatio = (npc.getCurrentHp() * 100)
							/ npc.getMaxHp();
					if (companionHpRatio < hpRatio) {
						hpRatio = companionHpRatio;
						minHpNpc = npc;
					}
				}
			}
		}
		return minHpNpc;
	}

	private void mobspawn(int summonId, int count) {
		int i;

		for (i = 0; i < count; i++) {
			mobspawn(summonId);
		}
	}

	private void mobspawn(int summonId) {
		try {
			L1Npc spawnmonster = NpcTable.getInstance().getTemplate(summonId);
			if (spawnmonster != null) {
				L1NpcInstance mob = null;
				try {
					String implementationName = spawnmonster.getImpl();
					Constructor<?> _constructor = Class.forName(
							(new StringBuilder()).append("l1j.server.server.model.Instance.")
							.append(implementationName).append("Instance").toString()).getConstructors()[0];
					mob = (L1NpcInstance) _constructor.newInstance(new Object[] { spawnmonster });
					mob.setId(ObjectIdFactory.getInstance().nextId());
					L1Location loc = _attacker.getLocation().randomLocation(8, false);
					int heading = _rnd.nextInt(8);
					mob.setX(loc.getX());
					mob.setY(loc.getY());
					mob.setHomeX(loc.getX());
					mob.setHomeY(loc.getY());
					short mapid = _attacker.getMapId();
					mob.setMap(mapid);
					mob.getMoveState().setHeading(heading);
					L1World.getInstance().storeObject(mob);
					L1World.getInstance().addVisibleObject(mob);
					L1Object object = L1World.getInstance().findObject(mob.getId());
					L1MonsterInstance newnpc = (L1MonsterInstance) object;
					newnpc.set_storeDroped(0); 
					if (summonId == 45061 
							|| summonId == 45161 
							|| summonId == 45181 
							|| summonId == 45455) {
						Broadcaster.broadcastPacket(newnpc, new S_DoActionGFX(newnpc.getId(), ActionCodes.ACTION_Hide));
						newnpc.setActionStatus(13);
						Broadcaster.broadcastPacket(newnpc, new S_NPCPack(newnpc));
						Broadcaster.broadcastPacket(newnpc, new S_DoActionGFX(newnpc.getId(), ActionCodes.ACTION_Appear));
						newnpc.setActionStatus(0);
						Broadcaster.broadcastPacket(newnpc, new S_NPCPack(newnpc));
					}
					newnpc.onNpcAI();
					newnpc.getLight().turnOnOffLight();
					newnpc.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE);
				} catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private L1Character changeTarget(int type, int idx) {
		L1Character target;

		switch (type) {
		case L1MobSkill.CHANGE_TARGET_ME:
			target = _attacker;
			break;
		case L1MobSkill.CHANGE_TARGET_RANDOM:
			List<L1Character> targetList = new ArrayList<L1Character>();
			L1Character cha = null;
			for (L1Object obj : L1World.getInstance().getVisibleObjects(_attacker)) {
				if (obj instanceof L1PcInstance || obj instanceof L1PetInstance
						|| obj instanceof L1SummonInstance) {
					cha = (L1Character) obj;

					int distance = _attacker.getLocation().getTileLineDistance(
							cha.getLocation());

					if (!getMobSkillTemplate().isTriggerDistance(idx, distance)) {
						continue;
					}

					if (!CharPosUtil.glanceCheck(_attacker, cha.getX(), cha.getY())) {
						continue;
					}

					if (!_attacker.getHateList().containsKey(cha)) {
						continue;
					}

					if (cha.isDead()) {
						continue;
					}

					if (cha instanceof L1PcInstance) {
						if (((L1PcInstance) cha).isGhost()) {
							continue;
						}
					}
					targetList.add((L1Character) obj);
				}
			}

			if (targetList.size() == 0) {
				target = _target;
			} else {
				int randomSize = targetList.size() * 100;
				int targetIndex = _rnd.nextInt(randomSize) / 100;
				target = targetList.get(targetIndex);
			}
			break;

		default:
			target = _target;
			break;
		}
		return target;
	}
}
