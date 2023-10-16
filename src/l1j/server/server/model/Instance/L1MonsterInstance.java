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
import java.util.Random;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.datatables.UBTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.Dead;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1NpcTalkData;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1UltimateBattle;
import l1j.server.server.model.L1World;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_NPCPack;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.CalcExp;
import l1j.server.server.utils.L1SpawnUtil;

public class L1MonsterInstance extends L1NpcInstance {
	private static final long serialVersionUID = 1L;
	private static Logger _log = Logger.getLogger(L1MonsterInstance.class.getName());
	public static int[][] _classGfxId = { { 0, 1 }, { 48, 61 }, { 37, 138 }, { 734, 1186 }, { 2786, 2796 }, { 6658, 6661 }, { 6671, 6650 } };
	private static Random _random = new Random(System.nanoTime());
	private int _storeDroped; 
	private Dead dead = new Dead(this, null);
	private int hprsize;
	private L1NpcInstance _attacker = null;  


	@Override
	public void onItemUse() {
		if (!isActived() && _target != null) {
			if (getLevel() <= 45) {
				useItem(USEITEM_HASTE, 40); 	
			}
			if (getNpcTemplate().get_npcId() == 4038000) { // 지룡 안타라스 1차
				String chat = "감히 여기가 어디라고! 어리석은 인간들이란...";
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
			}
			if (getNpcTemplate().get_npcId() == 4200010) { // 지룡 안타라스 2차
				String chat = "어리석은 자여! 나의 분노를 자극하는 구나.!";
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
			}
			if (getNpcTemplate().get_npcId() == 4200011) { // 지룡 안타라스 3차
				String chat = "감히 나를 상대하려 하다니..그러고도 너희가 살길 바라느냐?";
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
			}
			///////////////////파푸
			if (getNpcTemplate().get_npcId() == 4039000) { 
				String chat = "감히 나의 영역에 들어오다니...용기가 가상하구나..";
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
			}
			if (getNpcTemplate().get_npcId() == 4039006) { 
				String chat = "봉인을 풀 때 네가 큰 도움이 되었지만..나에게 두 번의 자비는 없다..";
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
			}
			if (getNpcTemplate().get_npcId() == 4039007) { 
				String chat = "가소롭구나! 저들이 너와 함께 이승을 떠돌게 될 나의 제물들인 것이냐!";
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
			}

			if (getNpcTemplate().is_doppel() && _target instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) _target;
				setName(_target.getName());
				setNameId(_target.getName());
				setTitle(_target.getTitle());
				setTempLawful(_target.getLawful());
				getGfxId().setTempCharGfx(targetPc.getClassId());
				getGfxId().setGfxId(targetPc.getClassId());
				setPassispeed(640);
				setAtkspeed(900); 
				for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
					pc.sendPackets(new S_RemoveObject(this));
					pc.getNearObjects().removeKnownObject(this);
					pc.updateObject();
				}
			}
		}
		if (getCurrentHp() * 100 / getMaxHp() < 40) { 
			useItem(USEITEM_HEAL, 50);
		}
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		perceivedFrom.getNearObjects().addKnownObject(this);
		if (0 < getCurrentHp()) {
			if (getHiddenStatus() == HIDDEN_STATUS_SINK) {
				perceivedFrom.sendPackets(new S_DoActionGFX(getId(), ActionCodes.ACTION_Hide));
			} else if (getHiddenStatus() == HIDDEN_STATUS_FLY) {
				perceivedFrom.sendPackets(new S_DoActionGFX(getId(), ActionCodes.ACTION_Moveup));
			}
			onNpcAI();
			/*			if (getMoveState().getBraveSpeed() == 1) { 
				perceivedFrom.sendPackets(new S_SkillBrave(getId(), 1, 600000));
			}*/
		}
		perceivedFrom.sendPackets(new S_NPCPack(this));
	}

	@Override
	public void searchTarget() {
		L1PcInstance targetPlayer = null;
		L1MonsterInstance targetMonster = null; 
		L1NpcInstance targetNpc = null; 
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(this)) {
			if (pc.getCurrentHp() <= 0 || pc.isDead() || pc.isGm() || pc.isMonitor() || pc.isGhost()) {
				continue;
			}

			int mapId = getMapId();
			if (mapId == 88 || mapId == 98 || mapId == 92 || mapId == 91 || mapId == 95) {
				if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) {
					targetPlayer = pc;
					break;
				}
			}

			if (getNpcId() == 45600){ 
				if (pc.isCrown() || pc.isDarkelf() || pc.getGfxId().getTempCharGfx() != pc.getClassId()) { 
					targetPlayer = pc; 
					break;   
				}   
			} 

			if ((getNpcTemplate().getKarma() < 0 && pc.getKarmaLevel() >= 1)
					|| (getNpcTemplate().getKarma() > 0 && pc.getKarmaLevel() <= -1)) {
				continue;
			}

			// 버땅 퀘스트의 변신, 각 진영의 monster로부터 선제 공격받지 않는다
			if (pc.getGfxId().getTempCharGfx() == 6034 && getNpcTemplate(). getKarma() < 0 
					|| pc.getGfxId().getTempCharGfx() == 6035 && getNpcTemplate(). getKarma() > 0
					|| pc.getGfxId().getTempCharGfx() == 6035 && getNpcTemplate(). get_npcId() == 46070
					|| pc.getGfxId().getTempCharGfx() == 6035 && getNpcTemplate(). get_npcId() == 46072) {
				continue;
			}

			if (!getNpcTemplate().is_agro() && !getNpcTemplate().is_agrososc()
					&& getNpcTemplate().is_agrogfxid1() < 0
					&& getNpcTemplate().is_agrogfxid2() < 0) {
				if (pc.getLawful() < -1000) { 
					targetPlayer = pc;
					break;
				}
				continue;
			}

			if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) { 
				if (pc.getSkillEffectTimerSet().hasSkillEffect(67)) { 
					if (getNpcTemplate().is_agrososc()) { 
						targetPlayer = pc;
						break;
					}
				} else if (getNpcTemplate().is_agro()) { 
					targetPlayer = pc;
					break;
				}

				if (getNpcTemplate().is_agrogfxid1() >= 0
						&& getNpcTemplate().is_agrogfxid1() <= 4) {
					if (_classGfxId[getNpcTemplate().is_agrogfxid1()][0] == pc.getGfxId().getTempCharGfx()
							|| _classGfxId[getNpcTemplate().is_agrogfxid1()][1] == pc.getGfxId().getTempCharGfx()) {
						targetPlayer = pc;
						break;
					}
				} else if (pc.getGfxId().getTempCharGfx() == getNpcTemplate().is_agrogfxid1()) { 
					targetPlayer = pc;
					break;
				}

				if (getNpcTemplate().is_agrogfxid2() >= 0 && getNpcTemplate().is_agrogfxid2() <= 4) { 
					if (_classGfxId[getNpcTemplate().is_agrogfxid2()][0] == pc.getGfxId().getTempCharGfx()
							|| _classGfxId[getNpcTemplate().is_agrogfxid2()][1] == pc.getGfxId().getTempCharGfx()) {
						targetPlayer = pc;
						break;
					}
				} else if (pc.getGfxId().getTempCharGfx() == getNpcTemplate().is_agrogfxid2()) { 
					targetPlayer = pc;
					break;
				}
			}
		}
		/**
		 * 좀비 캐릭이 허수아비를 팬다.
		 */
		for (L1Object obj : L1World.getInstance().getVisibleObjects(this)) {
			if (obj instanceof L1ScarecrowInstance) {
				L1ScarecrowInstance mon = (L1ScarecrowInstance) obj;
				if(mon.getHiddenStatus() != 0 || mon.isDead()){
					continue;
				} 
				if(this.getNpcTemplate().get_npcId()>=7000007 && getNpcTemplate().get_npcId()<=7000011 ){ //적을 인식할 몬스터 
					if(mon.getNpcTemplate().get_npcId() == 44998){//로봇이 팰 허수아비
						targetNpc = mon;
						break;
					}
				} 
				if(this.getNpcTemplate().get_npcId()>=7000012 && getNpcTemplate().get_npcId()<=7000016 ){ //적을 인식할 몬스터 
					if(mon.getNpcTemplate().get_npcId() == 44999){//로봇이 팰 허수아비
						targetNpc = mon;
						break;
					}
				}
				if(this.getNpcTemplate().get_npcId() >= 7000017 && getNpcTemplate().get_npcId() <= 7000021 ){ //적을 인식할 몬스터 
					if(mon.getNpcTemplate().get_npcId() == 44997){//로봇이 팰 허수아비
						targetNpc = mon;
						break;
					}
				}
				if(this.getNpcTemplate().get_npcId()>=7000022 && getNpcTemplate().get_npcId()<=7000026 ){ //적을 인식할 몬스터 
					if(mon.getNpcTemplate().get_npcId() == 45001){//로봇이 팰 허수아비
						targetNpc = mon;
						break;
					}
				}
				if(this.getNpcTemplate().get_npcId()>=7000027 && getNpcTemplate().get_npcId()<=7000031 ){ //적을 인식할 몬스터 
					if(mon.getNpcTemplate().get_npcId() == 45002){//로봇이 팰 허수아비
						targetNpc = mon;
						break;
					}
				}
				if(this.getNpcTemplate().get_npcId()>=7000032 && getNpcTemplate().get_npcId()<=7000036 ){ //적을 인식할 몬스터 
					if(mon.getNpcTemplate().get_npcId() == 45003){//로봇이 팰 허수아비
						targetNpc = mon;
						break;
					}
				}
				if(this.getNpcTemplate().get_npcId()>=7000037 && getNpcTemplate().get_npcId()<=7000043 ){ //적을 인식할 몬스터 
					if(mon.getNpcTemplate().get_npcId() == 45004){//로봇이 팰 허수아비
						targetNpc = mon;
						break;
					}
				}
			}
		}
		if (targetPlayer != null) {
			_hateList.add(targetPlayer, 0);
			_target = targetPlayer;
		}
		if(targetNpc != null){ 
			_hateList.add(targetNpc, 0);
			_target = targetNpc;
		} //<<허수아비 패기
	}

	@Override
	public void setLink(L1Character cha) {
		if (cha != null && _hateList.isEmpty()) { 
			_hateList.add(cha, 0);
			checkTarget();
		}
	}

	public L1MonsterInstance(L1Npc template) {
		super(template);
		_storeDroped = 1;
	}

	@Override
	public void onNpcAI() {
		if (isAiRunning()) {
			return;
		}
		if (_storeDroped == 1) {
			DropTable.getInstance().setDrop(this, getInventory());
			getInventory().shuffle();
			_storeDroped = 0;
		}else if(_storeDroped == 2){
			DropTable.getInstance().setPainwandDrop(this, getInventory());
			getInventory().shuffle();
			_storeDroped = 0;
		}
		setActived(false);
		startAI();
	}

	@Override
	public void onTalkAction(L1PcInstance pc) {
		int objid = getId();
		L1NpcTalkData talking = NPCTalkDataTable.getInstance(). getTemplate(getNpcTemplate(). get_npcId());
		String htmlid = null;
		String[] htmldata = null;

		// html 표시 패킷 송신
		if (htmlid != null) { // htmlid가 지정되고 있는 경우
			if (htmldata != null) { // html 지정이 있는 경우는 표시
				pc.sendPackets(new S_NPCTalkReturn(objid, htmlid, htmldata));
			} else {
				pc.sendPackets(new S_NPCTalkReturn(objid, htmlid));
			}
		} else {
			if (pc.getLawful() < -1000) { // 플레이어가 카오틱
				pc.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
			} else {
				pc.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
			}
		}
	}

	@Override
	public void onAction(L1PcInstance pc) {
		if (getCurrentHp() > 0 && !isDead()) {
			L1Attack attack = new L1Attack(pc, this);
			if (attack.calcHit()) {
				attack.calcDamage();
				attack.calcDrainOfHp();
				attack.addPcPoisonAttack(pc, this);
			}
			attack.action();
			attack.commit();
		}
	}

	@Override
	public void ReceiveManaDamage(L1Character attacker, int mpDamage) {
		if (mpDamage > 0 && !isDead()) {
			// int Hate = mpDamage / 10 + 10; 
			// setHate(attacker, Hate);
			setHate(attacker, mpDamage);

			onNpcAI();

			if (attacker instanceof L1PcInstance) { 
				serchLink((L1PcInstance) attacker, getNpcTemplate().get_family());
			}

			int newMp = getCurrentMp() - mpDamage;
			if (newMp < 0) {
				newMp = 0;
			}
			setCurrentMp(newMp);
		}
	}

	@Override
	public void receiveDamage(L1Character attacker, int damage) { 
		if (getCurrentHp() > 0 && !isDead()) {
			if (getHiddenStatus() != HIDDEN_STATUS_NONE || getHiddenStatus() == HIDDEN_STATUS_FLY) {
				return;
			}
			if (damage >= 0) {
				if (!(attacker instanceof L1EffectInstance)) { 
					setHate(attacker, damage);
				}
			}
			if (damage > 0) {
				if(getSkillEffectTimerSet().hasSkillEffect(L1SkillId.FOG_OF_SLEEPING)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.FOG_OF_SLEEPING);
				}else if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.PHANTASM);
				}
			}

			onNpcAI();

			if (attacker instanceof L1PcInstance) { 
				serchLink((L1PcInstance) attacker, getNpcTemplate().get_family());
			}

			if (attacker instanceof L1PcInstance && damage > 0) {
				L1PcInstance player = (L1PcInstance) attacker;
				player.setPetTarget(this);

				if (getNpcTemplate().get_npcId() == 45681 
						|| getNpcTemplate().get_npcId() == 45682 
						|| getNpcTemplate().get_npcId() == 45683 
						|| getNpcTemplate().get_npcId() == 45684) // 피닉스 추가
				{
					recall(player);
				}
			}		

			int newHp = getCurrentHp() - damage;
			if (newHp <= 0 && !isDead()) {
				/**보스몹 멘트 **/
				// 안타라스
				int npcid1 = getNpcTemplate().get_npcId();  
				if (npcid1 == 4038000) { // 엔피씨 번호
					String chat = "이제 맛있는 식사를 해볼까? 너희 피냄새가 나를 미치게 하는구나.";
					Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
				}
				if (npcid1 == 4200010) { // 엔피씨 번호
					String chat = "나의 분노가 하늘에 닿았다. 이제 곧 나의 아버지가 나설 것이다.";
					Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
				}
				if (npcid1 == 4200011) { // 엔피씨 번호
					String chat = "황혼의 저주가 그대들에게 있을 지어다! 실렌이여. 나의 어머니여. 나의 숨을.. 거두소서...";
					Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
					if (attacker instanceof L1PcInstance) {

						for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
							if(pc.getMapId() == 1005){                               
								pc.setBuffnoch(1);			             
								pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONBLOOD, 82, 3600/60));
								pc.setBuffnoch(0);
							}
						}
					}
				}
				// 파푸리온 
				if (npcid1 == 4039000) { // 엔피씨 번호
					String chat = "놀잇감으로는 충분하구나! 흐흐흐...";
					Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
				}
				if (npcid1 == 4039006) { // 엔피씨 번호
					String chat = "뼈 속까지 파고드는 두려움이 무엇인지 이 몸이 알게 해주마!";
					Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
				}
				if (npcid1 == 4039007) { // 엔피씨 번호
					String chat = "사엘..네 녀석이..어떻게...나의 어머니..실렌이시여 나의 숨을..거두소서...";
					Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, chat, 0));
					if (attacker instanceof L1PcInstance) {
						L1SkillUse l1skilluse = null;
						for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
							if(pc.getMapId() == 1011){                               
								l1skilluse = new L1SkillUse();
								l1skilluse.handleCommands(pc, L1SkillId.DRAGONBLOOD_P, pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
								pc.setBuffnoch(1);			             
								pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONBLOOD, 85, 3600/60));
								pc.setBuffnoch(0);
							}
						}
					}
				}

				int transformId = getNpcTemplate().getTransformId();
				if (transformId == -1) {
					setCurrentHp(0);
					setDead(true);
					setActionStatus(ActionCodes.ACTION_Die);

					dead.setAttacker(attacker);
					GeneralThreadPool.getInstance().execute(dead);
				} else {
					if(isAntharas()) {
						setCurrentHp(0);
						setDead(true);
						dieAntharas(attacker);
						//////리뉴얼안타//////
					}if(isPapoo()){
						setCurrentHp(0);
						setDead(true);
						diePaPoo(attacker); 
					} else {
						//distributeExpDropKarma(attacker);
						transform(transformId);
					}
				}
			}
			if (newHp > 0) {
				setCurrentHp(newHp);
				hide();
			}
		} else if (!isDead()) {
			setDead(true);
			setActionStatus(ActionCodes.ACTION_Die);
			dead.setAttacker(attacker);
			GeneralThreadPool.getInstance().execute(dead);
			/*
			if(ismarble()){			    
				setCurrentHp(0);
				setDead(true);
				attacker.marble.remove("오색구슬");
				hprsize = attacker.marble.size();
				setActionStatus(ActionCodes.ACTION_Die);
				dead.setAttacker(attacker);
				GeneralThreadPool.getInstance().execute(dead);
			}
			if(ismarble2()){
				setCurrentHp(0);
				setDead(true);
				attacker.marble2.remove("신비한오색구슬");			      
				setActionStatus(ActionCodes.ACTION_Die);
				dead.setAttacker(attacker);
				GeneralThreadPool.getInstance().execute(dead);
			}
			*/
		}
	}

	private void diePaPoo(L1Character lastAttacker) {
		setDeathProcessing(true);
		setCurrentHp(0);
		setDead(true);
		getMap().setPassable(getLocation(), true);
		startChat(CHAT_TIMING_DEAD);		
		setDeathProcessing(false);
		setExp(0);
		setKarma(0);
		allTargetClear();
		int transformGfxId = getNpcTemplate().getTransformGfxId();
		if (transformGfxId > 0)
			Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), transformGfxId));
		setActionStatus(ActionCodes.ACTION_Die);
		Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
		deleteMe();
		GeneralThreadPool.getInstance().schedule(new PaPooTransTimer(this), 20 * 1000);

	}

	private static class PaPooTransTimer extends TimerTask {
		L1NpcInstance _npc;
		private PaPooTransTimer(L1NpcInstance some) {
			_npc = some;
		}
		@Override
		public void run() {
			L1SpawnUtil.spawn2(_npc.getX(), _npc.getY(), (short) _npc.getMap().getId(),
					_npc.getNpcTemplate().getTransformId(), 0, 0, 0);
		}
	}

	////////리뉴얼파푸///////
	private boolean isPapoo() {
		int id = getNpcTemplate().get_npcId();
		if (id == 4039000 || id == 4039006) return true;
		return false;
	}
	/* 오색진주를 소환한다.
	private boolean ismarble(){
		int id = getNpcTemplate().get_npcId();
		if( id == 4039001) return true;
		return false;
	}
	private boolean ismarble2(){
		int id = getNpcTemplate().get_npcId();
		if( id == 4039002) return true;
		return false;
	}
	public void getMarble(L1NpcInstance npc){
		_attacker = npc;
		_attacker.marble.remove("오색구슬");
		_attacker.marble2.remove("신비한오색구슬");			
	}*/

	public void setDeath(Dead d) {
		dead = d;
	}

	private void recall(L1PcInstance pc) {
		if (getMapId() != pc.getMapId()) {
			return;
		}
		if (getLocation().getTileLineDistance(pc.getLocation()) > 4) {
			L1Location newLoc = null;
			for (int count = 0; count < 10; count++) {
				newLoc = getLocation().randomLocation(3, 4, false);
				if (CharPosUtil.glanceCheck(this, newLoc.getX(), newLoc.getY())) {
					L1Teleport.teleport(pc, newLoc.getX(), newLoc.getY(), getMapId(), 5, true);
					break;
				}
			}
		}
	}

	@Override
	public void setCurrentHp(int i) {
		super.setCurrentHp(i);

		if (getMaxHp() > getCurrentHp()) {
			startHpRegeneration();
		}
	}

	@Override
	public void setCurrentMp(int i) {
		super.setCurrentMp(i);

		if (getMaxMp() > getCurrentMp()) {
			startMpRegeneration();
		}
	}

	public void die(L1Character lastAttacker) {
		setDeathProcessing(true);
		setCurrentHp(0);
		setDead(true);
		setActionStatus(ActionCodes.ACTION_Die);
		getMap().setPassable(getLocation(), true);
		Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
		startChat(CHAT_TIMING_DEAD);
		distributeExpDropKarma(lastAttacker);
		giveUbSeal();
		setDeathProcessing(false);
		setExp(0);
		setKarma(0);
		setLawful(0);
		allTargetClear();
		startDeleteTimer();
	}

	//////////////리뉴얼안타///////
	private void dieAntharas(L1Character lastAttacker) {
		setDeathProcessing(true);
		setCurrentHp(0);
		setDead(true);
		getMap().setPassable(getLocation(), true);
		startChat(CHAT_TIMING_DEAD);
		setDeathProcessing(false);
		setExp(0);
		setKarma(0);
		allTargetClear();
		int transformGfxId = getNpcTemplate().getTransformGfxId();
		if (transformGfxId > 0)
			Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), transformGfxId));
		setActionStatus(ActionCodes.ACTION_Die);
		Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
		deleteMe();
		GeneralThreadPool.getInstance().schedule(new AntharasTransTimer(this), 20 * 1000);
	}
	private static class AntharasTransTimer extends TimerTask {
		L1NpcInstance _npc;
		private AntharasTransTimer(L1NpcInstance some) {
			_npc = some;
		}
		@Override
		public void run() {
			L1SpawnUtil.spawn2(_npc.getX(), _npc.getY(), (short) _npc.getMap().getId(),
					_npc.getNpcTemplate().getTransformId(), 0, 0, 0);
		}
	}

	private boolean isAntharas() {
		int id = getNpcTemplate().get_npcId();
		if (id == 4038000 || id == 4200010) return true;
		return false;
	}



	private void distributeExpDropKarma(L1Character lastAttacker) {
		if (lastAttacker == null) {
			return;
		}
		L1PcInstance pc = null;
		if (lastAttacker instanceof L1PcInstance) {
			pc = (L1PcInstance) lastAttacker;
		} else if (lastAttacker instanceof L1PetInstance) {
			pc = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
		} else if (lastAttacker instanceof L1SummonInstance) {
			pc = (L1PcInstance) ((L1SummonInstance) lastAttacker).getMaster();
		}

		if (pc != null) {
			ArrayList<L1Character> targetList = _hateList.toTargetArrayList();
			ArrayList<Integer> hateList = _hateList.toHateArrayList();
			if (pc != null) {
				int exp = getExp();
				CalcExp.calcExp(pc, getId(), targetList, hateList, exp);
				if (isDead()) {
					distributeDrop(pc);
					giveKarma(pc);
				}
			}
		} else if (lastAttacker instanceof L1EffectInstance) {
			ArrayList<L1Character> targetList = _hateList.toTargetArrayList();
			ArrayList<Integer> hateList = _hateList.toHateArrayList();
			if (hateList.size() != 0) {
				int maxHate = 0;
				for (int i = hateList.size() - 1; i >= 0; i--) {
					if (maxHate < ((Integer) hateList.get(i))) {
						maxHate = (hateList.get(i));
						lastAttacker = targetList.get(i);
					}
				}
				if (lastAttacker instanceof L1PcInstance) {
					pc = (L1PcInstance) lastAttacker;
				} else if (lastAttacker instanceof L1PetInstance) {
					pc = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
				} else if (lastAttacker instanceof L1SummonInstance) {
					pc = (L1PcInstance) ((L1SummonInstance) lastAttacker).getMaster();
				}
				int exp = getExp();
				CalcExp.calcExp(pc, getId(), targetList, hateList, exp);
				if (isDead()) {
					distributeDrop(pc);
					giveKarma(pc);
				}
			}
		}
	}

	private void distributeDrop(L1PcInstance pc) {
		ArrayList<L1Character> dropTargetList = _dropHateList
				.toTargetArrayList();
		ArrayList<Integer> dropHateList = _dropHateList.toHateArrayList();
		try {
			int npcId = getNpcTemplate().get_npcId();
			if (npcId != 45640 || (npcId == 45640 && getGfxId().getTempCharGfx() == 2332)) { 
				DropTable.getInstance().dropShare(L1MonsterInstance.this,
						dropTargetList, dropHateList, pc);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void giveKarma(L1PcInstance pc) {
		int karma = getKarma();
		if (karma != 0) {
			int karmaSign = Integer.signum(karma);
			int pcKarmaLevel = pc.getKarmaLevel();
			int pcKarmaLevelSign = Integer.signum(pcKarmaLevel);
			if (pcKarmaLevelSign != 0 && karmaSign != pcKarmaLevelSign) {
				karma *= 5;
			}
			pc.addKarma((int) (karma * Config.RATE_KARMA));
		}
	}

	private void giveUbSeal() {
		if (getUbSealCount() != 0) {
			L1UltimateBattle ub = UBTable.getInstance().getUb(getUbId());
			if (ub != null) {
				for (L1PcInstance pc : ub.getMembersArray()) {
					if (pc != null && !pc.isDead() && !pc.isGhost()) {
						if (_random.nextInt(10) <= 2){
							pc.getInventory().storeItem(L1ItemId.UB_WINNER_PIECE, 1);
						}
						pc.getInventory().storeItem(41402, getUbSealCount());
						pc.sendPackets(new S_ServerMessage(403, "$5448"));
					}
				}
			}
		}
	}

	public int get_storeDroped() {
		return _storeDroped;
	}

	public void set_storeDroped(int i) {
		_storeDroped = i;
	}

	private int _ubSealCount = 0; 

	public int getUbSealCount() {
		return _ubSealCount;
	}

	public void setUbSealCount(int i) {
		_ubSealCount = i;
	}

	private int _ubId = 0; // UBID

	public int getUbId() {
		return _ubId;
	}

	public void setUbId(int i) {
		_ubId = i;
	}

	private void hide() {
		int npcid = getNpcTemplate().get_npcId();
		if (npcid == 45061 
				|| npcid == 45161 
				|| npcid == 45181 
				|| npcid == 45455) { 
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = _random.nextInt(10);
				if (1 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_SINK);
					Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Hide));
					setActionStatus(13);
					Broadcaster.broadcastPacket(this, new S_NPCPack(this));
				}
			}
		} else if (npcid == 45682) { 
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = _random.nextInt(50);
				if (1 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_SINK);
					Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_AntharasHide));
					setActionStatus(20);
					Broadcaster.broadcastPacket(this, new S_NPCPack(this));
				}
			}
		} else if (npcid == 45067 
				//|| npcid == 45264 
				//|| npcid == 45452 
				|| npcid == 45090 
				//|| npcid == 45321 
				//|| npcid == 45445
				) {
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = _random.nextInt(10);
				if (1 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_FLY);
					Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Moveup));
					setActionStatus(4);
					Broadcaster.broadcastPacket(this, new S_NPCPack(this));
				}
			}
		} else if (npcid == 45681) { 
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = _random.nextInt(50);
				if (1 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_FLY);
					Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Moveup));
					setActionStatus(11);
					Broadcaster.broadcastPacket(this, new S_NPCPack(this));
				}
			}
		}
	}

	public void initHide() {
		int npcid = getNpcTemplate().get_npcId();
		if (npcid == 45061 
				|| npcid == 45161 
				|| npcid == 45181 
				|| npcid == 45455
				|| npcid == 400000
				|| npcid == 400001) { 
			int rnd = _random.nextInt(3);
			if (1 > rnd) {
				setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_SINK);
				setActionStatus(13);
			}
		} else if (npcid == 45045 
				|| npcid == 45126 
				|| npcid == 45134 
				|| npcid == 45281) { 
			int rnd = _random.nextInt(3);
			if (1 > rnd) {
				setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_SINK);
				setActionStatus(4);
			}
		} else if (npcid == 45067 
				|| npcid == 45264 
				|| npcid == 45452 
				|| npcid == 45090 
				|| npcid == 45321 
				|| npcid == 45445) { 
			setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_FLY);
			setActionStatus(4);
		} else if (npcid == 45681) {
			setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_FLY);
			setActionStatus(11);
		}
	}

	public void initHideForMinion(L1NpcInstance leader) {
		int npcid = getNpcTemplate().get_npcId();
		if (leader.getHiddenStatus() == L1NpcInstance.HIDDEN_STATUS_SINK) {
			if (npcid == 45061 
					|| npcid == 45161 
					|| npcid == 45181 
					|| npcid == 45455
					|| npcid == 400000
					|| npcid == 400001) { 
				setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_SINK);
				setActionStatus(13);
			} else if (npcid == 45045 
					|| npcid == 45126
					|| npcid == 45134 
					|| npcid == 45281) { 
				setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_SINK);
				setActionStatus(4);
			}
		} else if (leader.getHiddenStatus() == L1NpcInstance.HIDDEN_STATUS_FLY) {
			if (npcid == 45067 
					|| npcid == 45264 
					|| npcid == 45452 
					|| npcid == 45090
					|| npcid == 45321
					|| npcid == 45445) { 
				setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_FLY);
				setActionStatus(4);
			} else if (npcid == 45681) {
				setHiddenStatus(L1NpcInstance.HIDDEN_STATUS_FLY);
				setActionStatus(11);
			}
		}
	}

	@Override
	protected void transform(int transformId) {
		super.transform(transformId);
		getInventory().clearItems();
		DropTable.getInstance().setDrop(this, getInventory());
		getInventory().shuffle();
	}
}
