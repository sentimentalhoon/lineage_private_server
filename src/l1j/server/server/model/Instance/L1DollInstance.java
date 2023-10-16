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

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.ObjectIdFactory;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1World;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_DollPack;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1Npc;

public class L1DollInstance extends L1NpcInstance {
	private static final long serialVersionUID = 1L;

	public static final int DOLLTYPE_BUGBEAR = 0;
	public static final int DOLLTYPE_SUCCUBUS = 1;
	public static final int DOLLTYPE_WAREWOLF = 2;
	public static final int DOLLTYPE_STONEGOLEM = 3;
	public static final int DOLLTYPE_ELDER = 4;
	public static final int DOLLTYPE_CRUSTACEA = 5;
	public static final int DOLLTYPE_SEADANCER = 6;
	public static final int DOLLTYPE_SNOWMAN = 7;
	public static final int DOLLTYPE_COCATRIS = 8;
	public static final int DOLLTYPE_DRAGON_M = 9;
	public static final int DOLLTYPE_DRAGON_W = 10;
	public static final int DOLLTYPE_HIGH_DRAGON_M = 11;
	public static final int DOLLTYPE_HIGH_DRAGON_W = 12;	
	public static final int DOLLTYPE_LAMIA = 13;
	public static final int DOLLTYPE_HELPER = 20;
	public static final int DOLLTYPE_SPATOI = 21;
	public static final int DOLLTYPE_HUSUABI = 22;
	public static final int DOLLTYPE_ETIN = 17;
	public static final int DOLLTYPE_MERMAID = 23; 
	public static final int DOLLTYPE_SNOWMAN_A = 24;  
	public static final int DOLLTYPE_SNOWMAN_B = 25;  
	public static final int DOLLTYPE_SNOWMAN_C = 26;  
	public static final int DOLLTYPE_CAT = 26; //by.함정 남마법사 인형
	private static Random _random = new Random(System.nanoTime());
	private int _dollType;
	private int _itemObjId;
	private ScheduledFuture<?> _future = null;	
	private static int Buff[] = {26, 42, 43, 79};	

	// 타겟이 없는 경우의 처리
	@Override
	public boolean noTarget() {
		if (_master.isDead()) {
			deleteDoll();
			return true;
		} else if (_master != null && _master.getMapId() == getMapId()) {
			if (getLocation().getTileLineDistance(_master.getLocation()) > 2) {
				int dir = moveDirection(_master.getX(), _master.getY());
				if (dir == -1) {
					teleport(_master.getX(), _master.getY(), getMoveState().getHeading());
					setDirectionMove(dir);
					setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
				} else {
					setDirectionMove(dir);
					setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
				}
			}
		} else {
			deleteDoll();
			return true;
		}
		return false;
	}

	// 시간 계측용
	class DollTimer implements Runnable {
		@Override
		public void run() {
			if (_destroyed) { // 이미 파기되어 있지 않은가 체크
				return;
			}
			deleteDoll();
		}
	}

	class HelpTimer implements Runnable {
		@Override
		public void run() {
			if (_destroyed) { // 이미 파기되어 있지 않은가 체크
				return;
			}
			getHelperAction();
		}
	}

	public L1DollInstance(L1Npc template, L1PcInstance master, int dollType, int itemObjId, int dollTime) {
		super(template);
		setId(ObjectIdFactory.getInstance().nextId());
		setDollType(dollType);
		setItemObjId(itemObjId);
		GeneralThreadPool.getInstance().schedule(new DollTimer(), dollTime);
		setMaster(master);
		setX(master.getX() + _random.nextInt(5) - 2);
		setY(master.getY() + _random.nextInt(5) - 2);
		setMap(master.getMapId());
		getMoveState().setHeading(5);
		setLightSize(template.getLightSize());

		L1World.getInstance().storeObject(this);
		L1World.getInstance().addVisibleObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}
		master.addDoll(this);
		if (!isAiRunning()) {
			startAI();
		}
		if (isMpRegeneration()) {
			master.startMpRegenerationByDoll();
		}
		if (isHpRegeneration()) {
			master.startHpRegenerationByDoll();
		}

		int type = getDollType();
		if (type == DOLLTYPE_SNOWMAN) {
			master.getAC().addAc(-3);
			_master.getResistance().addFreeze(7);
		}
		/**by 판도라 에틴인형**/
		if (type == DOLLTYPE_ETIN) {
			_master.getResistance().addHold(10);
			master.getAC().addAc(-2);
			master.removeHasteSkillEffect();
			if (master.getMoveSpeed() != 1) {
				master.setMoveSpeed(1);
				master.sendPackets(new S_SkillHaste(master.getId(), 1, -1));
				master.broadcastPacket(new S_SkillHaste(master.getId(), 1, 0));
			}
			master.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_HASTE, dollTime * 1000);
		}    
		/**by 판도라 에틴인형**/
		if (type == DOLLTYPE_HUSUABI) {
			_master.addBowHitupByDoll(2);
			_master.addHitup(2);
			_master.addMaxHp(50);
			_master.addMaxMp(30);
		}
		if (type == DOLLTYPE_MERMAID) { // 인어 마법인형
			_master.getLight().setmermaidlight(true);
			_master.getLight().turnOnOffLight();
		}


		if (type == DOLLTYPE_SNOWMAN_A) {
			_master.addBowHitupByDoll(5);
		} 

		if (type == DOLLTYPE_COCATRIS){
			_master.addBowHitupByDoll(1);
			_master.addBowDmgupByDoll(1);			

		}
		if (type == DOLLTYPE_DRAGON_M
				|| type == DOLLTYPE_DRAGON_W
				|| type == DOLLTYPE_HIGH_DRAGON_M
				|| type == DOLLTYPE_HIGH_DRAGON_W){
			_master.addMpr(5);
		}
		if (type == DOLLTYPE_LAMIA){
			_master.addMpr(4);			
		}
		if (type == DOLLTYPE_SPATOI){
			_master.addDmgup(2);
			_master.getResistance().addStun(10);
		}
		if (type == DOLLTYPE_CAT) { // by.함정 남마법사 착용시
			_master.addBowHitupByDoll(2); //옵션은 잘 몰라서 ㅠㅠ 제멋대로 
			_master.addMaxMp(50);
			_master.getResistance().addMr(5);
			_master.getResistance().addSleep(5);
		}
		startHelpTimer();
	}

	public void deleteDoll() {
		if (isMpRegeneration()) {
			((L1PcInstance) _master).stopMpRegenerationByDoll();			
		}else if (isHpRegeneration()) {
			((L1PcInstance) _master).stopHpRegenerationByDoll();
		}
		int type = getDollType();
		if (type == DOLLTYPE_SNOWMAN){ 
			_master.getAC().addAc(3);
			_master.getResistance().addFreeze(-7);
		}
		/**by판도라 에틴인형**/
		if (type == DOLLTYPE_ETIN) {
			_master.getResistance().addHold(-10);
			_master.getAC().addAc(2);
			_master.setMoveSpeed(0);
			((L1PcInstance) _master).sendPackets(new S_SkillHaste(_master.getId(), 0, 0));
			_master.broadcastPacket(new S_SkillHaste(_master.getId(), 0, 0));
			_master.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.STATUS_HASTE);
		} 
		/**by판도라 에틴인형**/

		if (type == DOLLTYPE_SNOWMAN_A) {
			_master.addBowHitupByDoll(-5);
		} 
		if (type == DOLLTYPE_HUSUABI) {
			_master.addBowHitupByDoll(-2);
			_master.addHitup(-2);
			_master.addMaxHp(-50);
			_master.addMaxMp(-30);
		}
		if (type == DOLLTYPE_COCATRIS){
			_master.addBowHitupByDoll(-1);
			_master.addBowDmgupByDoll(-1);		
		}
		if (type == DOLLTYPE_DRAGON_M
				|| type == DOLLTYPE_DRAGON_W
				|| type == DOLLTYPE_HIGH_DRAGON_M
				|| type == DOLLTYPE_HIGH_DRAGON_W){
			_master.addMpr(-5);
		}
		if (type == DOLLTYPE_LAMIA){
			_master.addMpr(-4);			
		}
		if (type == DOLLTYPE_SPATOI){
			_master.addDmgup(-2);
			_master.getResistance().addStun(-10);
		}
		if (type == DOLLTYPE_MERMAID) { 
			_master.getLight().setmermaidlight(false);
			_master.getLight().turnOnOffLight();
		}
		if (type == DOLLTYPE_CAT) { // by.함정 남법사 인형 해체시
			_master.addBowHitupByDoll(-2);
			_master.addMaxHp(-50);
			_master.getResistance().addMr(-5);
			_master.getResistance().addSleep(-5);
		}
		stopHelpTimer();
		_master.sendPackets(new S_SkillSound(getId(), 5936));
		Broadcaster.broadcastPacket(_master, new S_SkillSound(getId(), 5936));
		_master.getDollList().remove(getId());
		deleteMe();
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		perceivedFrom.getNearObjects().addKnownObject(this);
		perceivedFrom.sendPackets(new S_DollPack(this, perceivedFrom));
	}

	@Override
	public void onItemUse() {
		if (!isActived()) {			
			useItem(USEITEM_HASTE, 100);
		}
	}

	@Override
	public void onGetItem(L1ItemInstance item) {
		if (getNpcTemplate().get_digestitem() > 0) {
			setDigestItem(item);
		}
		if (Arrays.binarySearch(haestPotions, item.getItem().getItemId()) >= 0) {
			useItem(USEITEM_HASTE, 100);
		}
	}

	public int getDollType() {	return _dollType;	}
	public void setDollType(int i) {	_dollType = i;	}

	public int getItemObjId() {	return _itemObjId;	}
	public void setItemObjId(int i) {	_itemObjId = i;	}

	public int getDamageByDoll() {
		int damage = 0;
		int type = getDollType();
		if (type == DOLLTYPE_WAREWOLF || type == DOLLTYPE_CRUSTACEA) {
			int chance = _random.nextInt(100) + 1;
			if (chance <= 5) {
				damage = 15;
				if (_master instanceof L1PcInstance) {
					L1PcInstance pc = (L1PcInstance) _master;
					pc.sendPackets(new S_SkillSound(_master.getId(), 6319));
				}
				Broadcaster.broadcastPacket(_master, new S_SkillSound(_master.getId(), 6319));
			}
		}
		return damage;
	}

	public void attackPoisonDamage(L1PcInstance pc, L1Character cha){
		int type = getDollType();
		if (type == DOLLTYPE_LAMIA){
			int chance = _random.nextInt(100) + 1;
			if (5 >= chance) {
				L1DamagePoison.doInfection(pc, cha, 3000, 10);
			}
		}
	}
	public int getDamageReductionByDoll() {
		int DamageReduction = 0;
		if (getDollType() == DOLLTYPE_STONEGOLEM) {
			int chance = _random.nextInt(100) + 1;
			if (chance <= 5) {
				DamageReduction = 15;
				if (_master instanceof L1PcInstance) {
					L1PcInstance pc = (L1PcInstance) _master;
					pc.sendPackets(new S_SkillSound(_master.getId(), 6320));
				}
				Broadcaster.broadcastPacket(_master, new S_SkillSound(_master.getId(), 6320));
			}
		}
		return DamageReduction;
	}

	public boolean isMpRegeneration() {
		boolean isMpRegeneration = false;
		int type = getDollType();
		switch(type){
		case DOLLTYPE_SUCCUBUS:
		case DOLLTYPE_ELDER:
		case DOLLTYPE_SNOWMAN_B: 
			isMpRegeneration = true;
			break;
		}		
		return isMpRegeneration;
	}

	public boolean isHpRegeneration() {
		boolean isHpRegeneration = false;
		int type = getDollType();
		switch(type){
		case DOLLTYPE_SEADANCER:
		case DOLLTYPE_SNOWMAN_C:
		case DOLLTYPE_MERMAID:
			isHpRegeneration = true;
			break;
		} 
		return isHpRegeneration;
	}

	public int getWeightReductionByDoll() {
		int weightReduction = 0;
		int type = getDollType();
		switch(type){		
		case DOLLTYPE_BUGBEAR:		
		case DOLLTYPE_HIGH_DRAGON_M:
		case DOLLTYPE_HIGH_DRAGON_W:
		case DOLLTYPE_DRAGON_M:
		case DOLLTYPE_DRAGON_W:
			weightReduction = 10;
			break;
		}
		return weightReduction;
	}
	public int getMpRegenerationValues(){
		int regenMp = 0;
		int type = getDollType();
		switch(type){
		case DOLLTYPE_SUCCUBUS:
		case DOLLTYPE_ELDER:
			regenMp = 15;
			break;		
		case DOLLTYPE_SNOWMAN_B:
			regenMp = 18;
			break; 
		}
		return regenMp;
	}
	public int getHpRegenerationValues(){  
		int regenHp = 0;                              
		int type = getDollType();
		switch(type){
		case DOLLTYPE_SEADANCER:
			regenHp = 25;
			break;  
		case DOLLTYPE_SNOWMAN_C:
			regenHp = 60;
			break;  
		case DOLLTYPE_MERMAID:
			regenHp = 5;
			break;  
		}
		return regenHp;
	}
	private void getHelperAction(){		
		for(int i = 0; i < Buff.length; i++){
			if(!_master.getSkillEffectTimerSet().hasSkillEffect(Buff[i])){
				new L1SkillUse().handleCommands(null, Buff[i], _master.getId(), _master.getX(), 
						_master.getY(), null, 0, L1SkillUse.TYPE_NORMAL, this);
				break;
			}
		}
	}

	public void startHelpTimer() {
		if(getDollType() != DOLLTYPE_HELPER)
			return;
		_future = GeneralThreadPool.getInstance().scheduleAtFixedRate(new HelpTimer(), 4000, 4000);
	}

	public void stopHelpTimer(){
		if(getDollType() != DOLLTYPE_HELPER)
			return;
		if (_future != null) {
			_future.cancel(false);
		}
	}
}
