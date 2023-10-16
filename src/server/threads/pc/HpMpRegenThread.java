package server.threads.pc;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1EffectInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.utils.CalcStat;

public class HpMpRegenThread extends Thread{
	
	private static HpMpRegenThread _instance;
	private static Logger _log = Logger.getLogger(HpMpRegenThread.class.getName());
	
	public static HpMpRegenThread getInstance(){
		if (_instance == null){
			_instance = new HpMpRegenThread();
			_instance.start();
		}
		return _instance;
	}	
	
	
	public HpMpRegenThread(){

	}
	
	public void run(){
		System.out.println(HpMpRegenThread.class.getName()  + " Start");
		while(true){
			try {
				for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
					if(pc == null || pc.getNetConnection() == null){
						continue;
					}
					else{
						if(pc.isDead() || pc.isPrivateShop() || pc.noPlayerCK){
							continue;
						}else{
							//HP 부문 먼저 쳐리
							pc.updateLevel();
							pc.addHpregenPoint(pc.getHpcurPoint());
							pc.setHpcurPoint(4);
							if (pc.getHpregenMax() <= pc.getHpregenPoint()) {
								pc.setHpregenPoint(0);
								regenHp(pc);
							}
							
							pc.addMpregenPoint(pc.getMpcurPoint());
							pc.setMpcurPoint(4);
							if (64 <= pc.getMpregenPoint()) {
								pc.setMpregenPoint(0);
								regenMp(pc);
							}
							
						}
					}
					
				}
				Thread.sleep(1000);
			}
			catch(Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}
	public void regenMp(L1PcInstance _pc) {
		int baseMpr = 1;
		int wis = _pc.getAbility().getTotalWis();
		if (wis == 15 || wis == 16) {
			baseMpr = 2;
		} else if (wis >= 17) {
			baseMpr = 3;
		}
		
		// 베이스 WIS 회복 보너스
		int baseStatMpr = CalcStat.calcBaseMpr(_pc.getType(), _pc.getAbility().getBaseWis());
		
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_BLUE_POTION) == true) { 
			if (wis < 11) { 
				wis = 11;
			}
			baseMpr += wis - 10;
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_BLUE_POTION2) == true) { 
			if (wis < 11) { 
				wis = 11;
			}
			baseMpr += wis - 8;
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_BLUE_POTION3) == true) { 			
			baseMpr += 3;
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MEDITATION) == true) { 
			baseMpr += 5;
		}
		if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.CONCENTRATION) == true) {
			baseMpr += 2;
		}
		if (L1HouseLocation.isInHouse(_pc.getX(), _pc.getY(), _pc.getMapId())) {
			baseMpr += 3;
		}
		if (isInn(_pc)){
			baseMpr += 3;
		}
		if (L1HouseLocation.isRegenLoc(_pc, _pc.getX(), _pc.getY(), _pc.getMapId())) {
			baseMpr += 3;
		}

		int itemMpr = _pc.getInventory().mpRegenPerTick();
		itemMpr += _pc.getMpr();	
		
		if (_pc.get_food() < 24 || isOverWeight(_pc)) {
			baseMpr = 0;
			baseStatMpr = 0;
			if (itemMpr > 0) {
				itemMpr = 0;
			}
		}
		int mpr = baseMpr + itemMpr + baseStatMpr;
		int newMp = _pc.getCurrentMp() + mpr;

		_pc.setCurrentMp(newMp);
	}

	
	public void regenHp(L1PcInstance _pc) {
		Random _random = new Random();
		if (_pc.isDead()) {
			return;
		}

		int maxBonus = 1;

		// CON 보너스
		if (11 < _pc.getLevel() && 14 <= _pc.getAbility().getTotalCon()) {
			maxBonus = _pc.getAbility().getTotalCon() - 12;
			if (25 < _pc.getAbility().getTotalCon()) {
				maxBonus = 14;
			}
		}
		// 베이스 CON 보너스
		int basebonus = CalcStat.calcBaseHpr(_pc.getType(), _pc.getAbility().getBaseCon());
		
		int equipHpr = _pc.getInventory().hpRegenPerTick();
		equipHpr += _pc.getHpr();
		int bonus = _random.nextInt(maxBonus) + 1;

		if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.NATURES_TOUCH)) {
			bonus += 15;
		}
		if (L1HouseLocation.isInHouse(_pc.getX(), _pc.getY(), _pc.getMapId())) {
			bonus += 5;
		}
		if (isInn(_pc)){
			bonus += 5;
		}
		if (L1HouseLocation.isRegenLoc(_pc, _pc.getX(), _pc.getY(), _pc.getMapId())) {
			bonus += 5;
		}

		boolean inLifeStream = false;
		if (isPlayerInLifeStream(_pc)) {
			inLifeStream = true;
			// 고대의 공간, 마족의 신전에서는 HPR+3은 없어져?
			bonus += 3;
		}

		// 공복과 중량의 체크
		if (_pc.get_food() < 24 || isOverWeight(_pc)
				|| _pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BERSERKERS)) {
			bonus = 0;
			basebonus = 0;
			// 장비에 의한 HPR 증가는 만복도, 중량에 의해 없어지지만, 감소인 경우는 만복도, 중량에 관계없이 효과가 남는다
			if (equipHpr > 0) {
				equipHpr = 0;
			}
		}
	
		int newHp = _pc.getCurrentHp();
		newHp += bonus + equipHpr + basebonus;

		if (newHp < 1) {
			newHp = 1; // HPR 감소 장비에 의해 사망은 하지 않는다
		}
		// 수중에서의 감소 처리
		// 라이프 시냇물로 감소를 없앨 수 있을까 불명
		if (isUnderwater(_pc)) {
			newHp -= 20;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // HP가 0이 되었을 경우는 사망한다.
				}
			}
		}
		// Lv50 퀘스트의 고대의 공간 1 F2F에서의 감소 처리
		if (isLv50Quest(_pc) && !inLifeStream) {
			newHp -= 10;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // HP가 0이 되었을 경우는 사망한다.
				}
			}
		}
		// 마족의 신전에서의 감소 처리
		if (_pc.getMapId() == 410 && !inLifeStream) {
			newHp -= 10;
			if (newHp < 1) {
				if (_pc.isGm()) {
					newHp = 1;
				} else {
					_pc.death(null); // HP가 0이 되었을 경우는 사망한다.
				}
			}
		}

		if (!_pc.isDead()) {
			_pc.setCurrentHp(Math.min(newHp, _pc.getMaxHp()));
		}
	}

	private boolean isUnderwater(L1PcInstance pc) {
		// 워터 부츠 장비시인가, 에바의 축복 상태이면, 수중은 아니면 간주한다.
		if (pc.getInventory().checkEquipped(20207)) {
			return false;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_UNDERWATER_BREATH)) {
			return false;
		}
		if (pc.getInventory().checkEquipped(21048)
				&& pc.getInventory().checkEquipped(21049)
				&& pc.getInventory().checkEquipped(21050)) {
			return false;
		}

		return pc.getMap().isUnderwater();
	}

	private boolean isOverWeight(L1PcInstance pc) {
		// 에키조틱크바이타라이즈 상태, 아디쇼나르파이아 상태인가
		// 골든 윙 장비시이면, 중량 오버이지 않으면 간주한다.
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.EXOTIC_VITALIZE)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ADDITIONAL_FIRE)) {
			return false;
		}
		if (pc.getInventory().checkEquipped(20049)) {
			return false;
		}
		if (isInn(pc)) {
			return false;
		}

		return (120 <= pc.getInventory().getWeight240()) ? true : false;
	}

	private boolean isLv50Quest(L1PcInstance pc) {
		int mapId = pc.getMapId();
		return (mapId == 2000 || mapId == 2001) ? true : false;
	}

	/**
	 * 지정한 PC가 라이프 시냇물의 범위내에 있는지 체크한다
	 * 
	 * @param pc
	 *            PC
	 * @return true PC가 라이프 시냇물의 범위내에 있는 경우
	 */
	private static boolean isPlayerInLifeStream(L1PcInstance pc) {
		L1EffectInstance effect = null;
		for (L1Object object : pc.getNearObjects().getKnownObjects()) {
			if (object instanceof L1EffectInstance == false) {
				continue;
			}
			effect = (L1EffectInstance) object;
			if (effect.getNpcId() == 81169 && effect.getLocation().getTileLineDistance(pc.getLocation()) < 4) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isInn(L1PcInstance pc) {
		int mapId = pc.getMapId();
		return (mapId == 16384 || mapId == 16896 || mapId == 17408 || mapId == 17492
				|| mapId == 17820 || mapId == 17920 || mapId == 18432 || mapId == 18944 
				|| mapId == 19456 || mapId == 19968 || mapId == 20480 || mapId == 20992 
				|| mapId == 21504 || mapId == 22016 || mapId == 22528 || mapId == 23040 
				|| mapId == 23552 || mapId == 24064 || mapId == 24576 || mapId == 25088 ) ? true : false;
	}
}
