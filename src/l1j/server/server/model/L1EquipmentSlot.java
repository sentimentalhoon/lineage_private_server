/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.datatables.UserSpeedControlTable;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Ability;
import l1j.server.server.serverpackets.S_AddSkill;
import l1j.server.server.serverpackets.S_DelSkill;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillIconBlessOfEva;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Item;
import static l1j.server.server.model.skill.L1SkillId.*;

public class L1EquipmentSlot {

	private L1PcInstance _owner;

	private ArrayList<L1ArmorSet> _currentArmorSet;

	private L1ItemInstance _weapon;

	private ArrayList<L1ItemInstance> _armors;

	public L1EquipmentSlot(L1PcInstance owner) {
		_owner = owner;

		_armors = new ArrayList<L1ItemInstance>();
		_currentArmorSet = new ArrayList<L1ArmorSet>();
	}

	private void setWeapon(L1ItemInstance weapon) {

		_owner.setWeapon(weapon);
		_owner.setCurrentWeapon(weapon.getItem().getType1());

		_owner.sendPackets(new S_SPMR(_owner));

		weapon.startEquipmentTimer(_owner);
		_weapon = weapon;
		int itemId = weapon.getItem().getItemId();
		if (itemId >= 11011 && itemId <= 11013){ // 신비한 눈뭉치 : 착용시 8768 로 변신
			L1PolyMorph.doPoly(_owner, 8768, 0, L1PolyMorph.MORPH_BY_ITEMMAGIC);
		}
		_owner.setAttackSpeed(UserSpeedControlTable.getInstance().getAttackSpeed(_owner.getGfxId().getTempCharGfx(), _owner.getCurrentWeapon() + 1));
	}

	public L1ItemInstance getWeapon() {
		return _weapon;
	}

	private void setArmor(L1ItemInstance armor) {
		int RegistLevel = armor.getRegistLevel();
		L1Item item = armor.getItem();
		int itemlvl = armor.getEnchantLevel();
		int itemtype = armor.getItem().getType();
		int itemId = armor.getItem().getItemId();
		int itemgrade = armor.getItem().getGrade();

		if (itemtype >= 8 && itemtype <=12){
			_owner.getAC().addAc(item.get_ac()  - armor.getAcByMagic());
		} else {
			_owner.getAC().addAc(item.get_ac() - armor.getEnchantLevel() - armor.getAcByMagic());
		}
		switch (itemId){
		case 420104: // 파푸리온의 완력
		case 420105: // 파푸리온의 예지력
		case 420106: // 파푸리온의 인내력
		case 420107: // 파푸리온의 마력; 가호 시작
			_owner.startPapuBlessing();
			break;
		case 490000: // 힘의 티셔츠
		case 490001: // 민첩의 티셔츠
		case 490002: // 매력의 티셔츠
		case 490003: // 마력의 티셔츠
		case 490004: // 체력의 티셔츠
		case 490005: // 마나의 티셔츠
		case 490006: // 스턴 내성의 티셔츠
		case 490007: // 홀드 내성의 티셔츠
		case 490008: // 마법 저항의 티셔츠
		case 490009: // 힘의 특제 티셔츠
		case 490010: // 민첩의 특제 티셔츠
		case 490011: // 매력의 특제 티셔츠
		case 490012: // 마력의 특제 티셔츠
		case 490013: // 체력의 특제 티셔츠
		case 490014: // 마나의 특제 티셔츠
		case 490015: // 스턴 내성의 특제 티셔츠
		case 490016: // 홀드 내성의 특제 티셔츠
		case 490017: // 마법 저항의 특제 티셔츠 : 인장 효과 ; 1인챈당 각각 속성이 2씩 증가 최대 인챈은 5, 속성은 10
			_owner.getResistance().addFire(RegistLevel * 2);
			_owner.getResistance().addWind(RegistLevel * 2);
			_owner.getResistance().addEarth(RegistLevel * 2);
			_owner.getResistance().addWater(RegistLevel * 2); 
			break;
		case 423014: _owner.startAHRegeneration(); break; // 견고한 등껍질 투구 : 착용시 영엄한 파편 지급
		case 423015: _owner.startSHRegeneration(); break; // 단단한 나뭇가지 면류관 : 착용시 빛나는 나뭇잎 지급
		case 20380: _owner.startHalloweenRegeneration(); break; // 할로윈 축복 모자 : 착용시 할로윈 호박 파이 지급
		case 20077: case 20062: case 120077: // 투명망토 종류
			if (!_owner.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.INVISIBILITY)) {
				for (L1DollInstance doll : _owner.getDollList().values()) {
					doll.deleteDoll();					
					_owner.sendPackets(new S_SkillIconGFX(56, 0));
					_owner.sendPackets(new S_OwnCharStatus(_owner));
				}
				_owner.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.BLIND_HIDING);
				_owner.getSkillEffectTimerSet().setSkillEffect(L1SkillId.INVISIBILITY, 0);
				_owner.sendPackets(new S_Invis(_owner.getId(), 1));
				Broadcaster.broadcastPacket(_owner, new S_Invis(_owner.getId(), 1));
			}
			break;
		case 20288: _owner.sendPackets(new S_Ability(1, true)); break; // 순간 이동 조정 반지 : 1 ROTC ; 순간 이동을 누르면 기억창이 뜨는 능력
		case 20036: _owner.sendPackets(new S_Ability(3, true)); break; // 인프라비젼 투구 : 3 ; 밤이 낮이 된다.
		case 20207: _owner.sendPackets(new S_SkillIconBlessOfEva(_owner.getId(), -1)); break; // 수중 부츠 ; 물속에서 숨을 쉴 수 있게 한다.
		case 20383:  // 기마용 투구 ; 군주 클래스만 사용 가능, 사용시 말탄 군주로 변신
			if (armor.getChargeCount() != 0) {
				armor.setChargeCount(armor.getChargeCount() - 1);
				_owner.getInventory().updateItem(armor, L1PcInventory.COL_CHARGE_COUNT);
			}
			break;
		}

		_owner.addDamageReductionByArmor(item.getDamageReduction());
		_owner.addWeightReduction(item.getWeightReduction());
		_owner.addHitupByArmor(item.getHitup());
		_owner.addDmgupByArmor(item.getDmgup());
		_owner.addBowHitupByArmor(item.getBowHitup());
		_owner.addBowDmgupByArmor(item.getBowDmgup());
		_owner.getResistance().addEarth(item.get_defense_earth());
		_owner.getResistance().addWind(item.get_defense_wind());
		_owner.getResistance().addWater(item.get_defense_water());
		_owner.getResistance().addFire(item.get_defense_fire());
		_owner.getResistance().addStun(item.get_regist_stun());
		_owner.getResistance().addPetrifaction(item.get_regist_stone());
		_owner.getResistance().addSleep(item.get_regist_sleep());
		_owner.getResistance().addFreeze(item.get_regist_freeze());
		_owner.getResistance().addHold(item.get_regist_sustain());
		_owner.getResistance().addBlind(item.get_regist_blind());
		_armors.add(armor);

		for (L1ArmorSet armorSet : L1ArmorSet.getAllSet()) {
			if (armorSet.isPartOfSet(itemId) && armorSet.isValid(_owner)) {
				if (armor.getItem().getType2() == 2 && armor.getItem().getType() == 9) {
					if (!armorSet.isEquippedRingOfArmorSet(_owner)) {
						armorSet.giveEffect(_owner);
						_currentArmorSet.add(armorSet);
					}
				} else {
					armorSet.giveEffect(_owner);
					_currentArmorSet.add(armorSet);
				}
			}
		}
		if (itemtype >= 8 && itemtype <=12){
			if(itemlvl > 0){
				if(itemgrade == 0){//상급
					_owner.getResistance().addAllNaturalResistance(itemlvl);	
					_owner.addMaxHp(itemlvl * 2);	
					if(itemlvl > 5) {
						_owner.addHitup(itemlvl-5);						
					}
				} else if(itemgrade == 1){//중급
					_owner.addMaxHp(itemlvl * 2);	
					_owner.getResistance().addMr(itemlvl * 1);
					if(itemlvl > 5){
						_owner.addBowHitup(itemlvl-5);		
					}
				} else if(itemgrade == 2){ //하급
					_owner.addMaxMp(itemlvl*2);	
					if(itemlvl > 5){
						_owner.getAbility().addSp(itemlvl-5);					
					}					
				} else if (itemId == 420008){ // 테베 호루스의 반지
					switch(itemlvl){
					case 1: case 2: case 3: case 4: case 5:	_owner.addMaxMp(itemlvl * 2); break;
					default:
						_owner.addMaxMp(itemlvl*2);	
						_owner.getAbility().addSp(itemlvl - 5);
						break;
					}	
				}else if(itemId == 500007){ //룸티스의 붉은빛귀걸이
					switch(itemlvl){				      
					case 1:	_owner.addMaxHp(20); break;
					case 2:	_owner.addMaxHp(30); break;
					case 3:	_owner.addMaxHp(40); break;
					case 4:	_owner.addMaxHp(45); break;
					case 5:	_owner.addMaxHp(50); break;
					case 6:	_owner.addMaxHp(55); break;
					case 7: _owner.addMaxHp(60); break;
					case 8:	_owner.addMaxHp(65); break;					      		       
					default: break;
					}
				}else if(itemId == 500008){ //룸티스의 푸른빛귀걸이
					switch(itemlvl){
					case 5: _owner.getAC().addAc(-1); break;
					case 6:	_owner.getAC().addAc(-2); break;        
					case 7:	_owner.getAC().addAc(-2); break;			       
					case 8:	_owner.getAC().addAc(-3); break;			       
					default: break;
					}
				}else if(itemId == 500009){ //룸티스의 보랏빛귀걸이
					switch(itemlvl){
					case 1:
						_owner.addMaxMp(10);
						_owner.getResistance().addMr(3);
						break;
					case 2:
						_owner.addMaxMp(15);
						_owner.getResistance().addMr(4);				       
						break;
					case 3:
						_owner.addMaxMp(20);
						_owner.getResistance().addMr(5);
						break;
					case 4:
						_owner.addMaxMp(24);
						_owner.getResistance().addMr(6);
						break;
					case 5:
						_owner.addMaxMp(28);
						_owner.getResistance().addMr(7);
						_owner.getAbility().addSp(1);
						break;
					case 6:
						_owner.addMaxMp(31);
						_owner.getResistance().addMr(8);
						_owner.getAbility().addSp(1);
						break;
					case 7:
						_owner.addMaxMp(34);
						_owner.getResistance().addMr(9);
						_owner.getAbility().addSp(2);
						break;
					case 8:
						_owner.addMaxMp(36);
						_owner.getResistance().addMr(10);
						_owner.getAbility().addSp(2);
						break;					       
					default: break;
					}
					///////여기서부터 인첸당 3등급 악세 변화주기////
				}else if(itemgrade == 3&&(itemId >= 500000 && itemId <= 500004)){ //순백
					switch(itemlvl){
					case 1:
						_owner.addMaxHp(15);
						break;
					case 2:
						_owner.addMaxHp(20);
						_owner.getAC().addAc(-1);
						break;
					case 3:
						_owner.addMaxHp(25);
						_owner.getAC().addAc(-2);
						break;
					case 4:
						_owner.addMaxHp(30);
						_owner.getAC().addAc(-3);
						break;

					case 5:
						_owner.addMaxHp(35);
						_owner.getAC().addAc(-3);
						_owner.addDmgup(1);
						_owner.addBowDmgup(1);
						break;

					case 6:
						_owner.addMaxHp(40);
						_owner.getAC().addAc(-3);
						_owner.addDmgup(2);
						_owner.addBowDmgup(2);
						break;

					case 7:
						_owner.addMaxHp(45);
						_owner.getAC().addAc(-3);
						_owner.addDmgup(3);
						_owner.addBowDmgup(3);
						break;

					case 8:
						_owner.addMaxHp(50);
						_owner.getAC().addAc(-3);
						_owner.addDmgup(4);
						_owner.addBowDmgup(4);
						break;	

					case 9:
						_owner.addMaxHp(50);
						_owner.getAC().addAc(-3);
						_owner.addDmgup(5);
						_owner.addBowDmgup(5);
						break; 

					case 10:
						_owner.addMaxHp(50);
						_owner.getAC().addAc(-3);
						_owner.addDmgup(6);
						_owner.addBowDmgup(6);
						break;
					default: break;
					}

				}			
			}
		}
		armor.startEquipmentTimer(_owner);
	}

	public ArrayList<L1ItemInstance> getArmors() {
		return _armors;
	}

	private void removeWeapon(L1ItemInstance weapon) {
		_owner.setWeapon(null);
		_owner.setCurrentWeapon(0);
		weapon.stopEquipmentTimer();
		_weapon = null;
		_owner.sendPackets(new S_SPMR(_owner));
		int itemId = weapon.getItem().getItemId();
		if (itemId >= 11011 && itemId <= 11013){
			L1PolyMorph.undoPoly(_owner); 
		}
		if (_owner.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.COUNTER_BARRIER)) {
			_owner.getSkillEffectTimerSet().removeSkillEffect(L1SkillId.COUNTER_BARRIER);
		}
		_owner.setAttackSpeed(UserSpeedControlTable.getInstance().getAttackSpeed(_owner.getGfxId().getTempCharGfx(), _owner.getCurrentWeapon() + 1));
	}

	private void removeArmor(L1ItemInstance armor) {
		int RegistLevel = armor.getRegistLevel();
		L1Item item = armor.getItem();
		int itemId = armor.getItem().getItemId();
		int itemlvl = armor.getEnchantLevel();
		int itemtype = armor.getItem().getType();
		int itemgrade = armor.getItem().getGrade();

		if (itemtype >= 8 && itemtype <=12){
			_owner.getAC().addAc(-(item.get_ac() - armor.getAcByMagic()));
		}else{
			_owner.getAC().addAc(-(item.get_ac() - armor.getEnchantLevel() - armor.getAcByMagic()));
		}
		if (itemId == 420104 || itemId == 420105 || itemId == 420106 || itemId == 420107) {
			_owner.stopPapuBlessing();
		}
		_owner.addDamageReductionByArmor(-item.getDamageReduction());
		_owner.addWeightReduction(-item.getWeightReduction());
		_owner.addHitupByArmor(-item.getHitup());
		_owner.addDmgupByArmor(-item.getDmgup());
		_owner.addBowHitupByArmor(-item.getBowHitup());
		_owner.addBowDmgupByArmor(-item.getBowDmgup());
		_owner.getResistance().addEarth(-item.get_defense_earth());
		_owner.getResistance().addWind(-item.get_defense_wind());
		_owner.getResistance().addWater(-item.get_defense_water());
		_owner.getResistance().addFire(-item.get_defense_fire());
		_owner.getResistance().addStun(-item.get_regist_stun());
		_owner.getResistance().addPetrifaction(-item.get_regist_stone());
		_owner.getResistance().addSleep(-item.get_regist_sleep());
		_owner.getResistance().addFreeze(-item.get_regist_freeze());
		_owner.getResistance().addHold(-item.get_regist_sustain());
		_owner.getResistance().addBlind(-item.get_regist_blind());

		for (L1ArmorSet armorSet : L1ArmorSet.getAllSet()) {
			if (armorSet.isPartOfSet(itemId)
					&& _currentArmorSet.contains(armorSet)
					&& !armorSet.isValid(_owner)) {
				armorSet.cancelEffect(_owner);
				_currentArmorSet.remove(armorSet);
			}
		}
		if (itemId >= 490000 && itemId <= 490017) { 
			_owner.getResistance().addFire(-RegistLevel * 2);
			_owner.getResistance().addWind(-RegistLevel * 2);
			_owner.getResistance().addEarth(-RegistLevel * 2);
			_owner.getResistance().addWater(-RegistLevel * 2); 
		} 

		if(armor.getAcByMagic() > 0){
			_owner.sendPackets(new S_SystemMessage("아직 장비에 마법효과가 남아있습니다."));
			return;
		}
		if(armor.getDmgByMagic() > 0){
			_owner.sendPackets(new S_SystemMessage("아직 장비에 마법효과가 남아있습니다."));
			return;
		}
		if(armor.getHolyDmgByMagic() > 0){
			_owner.sendPackets(new S_SystemMessage("아직 장비에 마법효과가 남아있습니다."));
			return;
		}
		if(armor.getHitByMagic() > 0){
			_owner.sendPackets(new S_SystemMessage("아직 장비에 마법효과가 남아있습니다."));
			return;
		}

		if (itemId == 423014){
			_owner.stopAHRegeneration();
		}
		if (itemId == 423015){
			_owner.stopSHRegeneration();
		}
		if (itemId == 20380){
			_owner.stopHalloweenRegeneration();
		}
		if (itemId == 20077 || itemId == 20062 || itemId == 120077) {
			_owner.delInvis();
		}
		if (itemId == 20288) {
			_owner.sendPackets(new S_Ability(1, false));
		}
		if (itemId == 20036) { 
			_owner.sendPackets(new S_Ability(3, false));
		}
		if (itemId == 20207) { 
			_owner.sendPackets(new S_SkillIconBlessOfEva(_owner.getId(), 0));
		}

		if (itemtype >= 8 && itemtype <=12){
			if(itemlvl > 0){ 
				if(itemgrade == 2){ //하급
					_owner.addMaxMp(-(itemlvl*2));	
					if(itemlvl > 5){
						_owner.getAbility().addSp(-(itemlvl-5));					
					}					
				}else if(itemgrade == 1){//중급
					_owner.addMaxHp(-(itemlvl * 2));	
					_owner.getResistance().addMr(-(itemlvl * 1));
					if(itemlvl > 5){
						_owner.addBowHitup(-(itemlvl-5));		
					}
				}else if(itemgrade == 0){//상급
					_owner.getResistance().addAllNaturalResistance(-itemlvl);	
					_owner.addMaxHp(-(itemlvl * 2));	
					if(itemlvl > 5) {
						_owner.addHitup(-(itemlvl-5));						
					}

				} else if (itemId == 420008){ // 테베 호루스의 반지
					switch(itemlvl){
					case 1: case 2: case 3: case 4: case 5:
						_owner.addMaxMp(itemlvl * -2);	
						break;
					default:
						_owner.addMaxMp(itemlvl * -2);	
						_owner.getAbility().addSp((itemlvl - 5) * -1);
						break;
					}	
				}else if(itemId == 500007){ //룸티스의 붉은빛귀걸이
					switch(itemlvl){				      
					case 1:
						_owner.addMaxHp(-20);
						break;
					case 2:
						_owner.addMaxHp(-30);				      
						break;
					case 3:
						_owner.addMaxHp(-40);
						//    _owner.addDamageReductionByArmor(-1);
						break;
					case 4:
						_owner.addMaxHp(-45);
						//    _owner.addDamageReductionByArmor(-1);
						break;
					case 5:
						_owner.addMaxHp(-50);
						//    _owner.addDamageReductionByArmor(-2);
						break;
					case 6:
						_owner.addMaxHp(-55);
						//    _owner.addDamageReductionByArmor(-2);
						break;
					case 7:
						_owner.addMaxHp(-60);
						//   _owner.addDamageReductionByArmor(-3);
						break;
					case 8:
						_owner.addMaxHp(-65);
						//   _owner.addDamageReductionByArmor(-3);
						break;					      			       
					default:

					}
				}else if(itemId == 500008){ //룸티스의 푸른빛귀걸이
					switch(itemlvl){


					case 5:				       
						_owner.getAC().addAc(1);				        
						break;

					case 6:				       
						_owner.getAC().addAc(2);				        
						break;

					case 7:				       
						_owner.getAC().addAc(2);				       
						break;

					case 8:				        
						_owner.getAC().addAc(3);
						break;					       			       
					default:

					}
				}else if(itemId == 500009){ //룸티스의 보랏빛귀걸이
					switch(itemlvl){
					case 1:
						_owner.addMaxMp(-10);
						_owner.getResistance().addMr(-3);
						break;
					case 2:
						_owner.addMaxMp(-15);
						_owner.getResistance().addMr(-4);				       
						break;
					case 3:
						_owner.addMaxMp(-20);
						_owner.getResistance().addMr(-5);
						break;
					case 4:
						_owner.addMaxMp(-24);
						_owner.getResistance().addMr(-6);
						break;
					case 5:
						_owner.addMaxMp(-28);
						_owner.getResistance().addMr(-7);
						_owner.getAbility().addSp(-1);
						break;
					case 6:
						_owner.addMaxMp(-31);
						_owner.getResistance().addMr(-8);
						_owner.getAbility().addSp(-1);
						break;
					case 7:
						_owner.addMaxMp(-34);
						_owner.getResistance().addMr(-9);
						_owner.getAbility().addSp(-2);
						break;

					case 8:
						_owner.addMaxMp(-36);
						_owner.getResistance().addMr(-10);
						_owner.getAbility().addSp(-2);
						break;					       				       
					default:

					}
				}else if(itemgrade == 3&&(itemId >= 500000 && itemId <= 500004)){
					switch(itemlvl){
					case 1:
						_owner.addMaxHp(-15);
						break;
					case 2:
						_owner.addMaxHp(-20);
						_owner.getAC().addAc(1);
						break;

					case 3:
						_owner.addMaxHp(-25);
						_owner.getAC().addAc(2);
						break;

					case 4:
						_owner.addMaxHp(-30);
						_owner.getAC().addAc(3);
						break;

					case 5:
						_owner.addMaxHp(-35);
						_owner.getAC().addAc(3);
						_owner.addDmgup(-1);
						_owner.addBowDmgup(-1);
						break;

					case 6:
						_owner.addMaxHp(-40);
						_owner.getAC().addAc(3);
						_owner.addDmgup(-2);
						_owner.addBowDmgup(-2);
						break;

					case 7:
						_owner.addMaxHp(-45);
						_owner.getAC().addAc(3);
						_owner.addDmgup(-3);
						_owner.addBowDmgup(-3);
						break;

					case 8:
						_owner.addMaxHp(-50);
						_owner.getAC().addAc(3);
						_owner.addDmgup(-4);
						_owner.addBowDmgup(-4);
						break;	
					case 9:
						_owner.addMaxHp(-50);
						_owner.getAC().addAc(3);
						_owner.addDmgup(-5);
						_owner.addBowDmgup(-5);
						break;	
					case 10:
						_owner.addMaxHp(-50);
						_owner.getAC().addAc(3);
						_owner.addDmgup(-6);
						_owner.addBowDmgup(-6);
						break;	
					default:
					}
				}			
			}
		}
		armor.stopEquipmentTimer();
		_armors.remove(armor);
	}

	public void set(L1ItemInstance equipment) {
		L1Item item = equipment.getItem();

		if (item.getType2() == 0) {
			return;
		}

		if (item.get_addhp() != 0) {
			_owner.addMaxHp(item.get_addhp());
		}
		if (item.get_addmp() != 0) {
			_owner.addMaxMp(item.get_addmp());
		}			
		_owner.getAbility().addAddedStr(item.get_addstr());
		_owner.getAbility().addAddedCon(item.get_addcon());
		_owner.getAbility().addAddedDex(item.get_adddex());
		_owner.getAbility().addAddedInt(item.get_addint());
		_owner.getAbility().addAddedWis(item.get_addwis());
		if (item.get_addwis() != 0) {
			_owner.resetBaseMr();
		}
		_owner.getAbility().addAddedCha(item.get_addcha());

		int addMr = 0;
		addMr += equipment.getMr();
		if (item.getItemId() == 20236 && _owner.isElf()) {
			addMr += 5;
		}
		if (addMr != 0) {
			_owner.getResistance().addMr(addMr);
			_owner.sendPackets(new S_SPMR(_owner));
		}
		if (item.get_addsp() != 0) {
			_owner.getAbility().addSp(item.get_addsp());
			_owner.sendPackets(new S_SPMR(_owner));
		}
		if (item.isHasteItem()) {
			_owner.addHasteItemEquipped(1);
			_owner.removeHasteSkillEffect();
			if (_owner.getMoveState().getMoveSpeed() != 1) {
				_owner.getMoveState().setMoveSpeed(1);
				_owner.sendPackets(new S_SkillHaste(_owner.getId(), 1, -1));
				Broadcaster.broadcastPacket(_owner, new S_SkillHaste(_owner.getId(), 1, 0));
			}
		}
		if (item.getItemId() == 20383) {
			if (_owner.getSkillEffectTimerSet().hasSkillEffect(STATUS_BRAVE)) {
				_owner.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_BRAVE);
				_owner.sendPackets(new S_SkillBrave(_owner.getId(), 0, 0));
				Broadcaster.broadcastPacket(_owner, new S_SkillBrave(_owner.getId(), 0, 0));
				_owner.getMoveState().setBraveSpeed(0);
			}
		}
		_owner.getEquipSlot().setMagicHelm(equipment);

		if (item.getType2() == 1) {
			setWeapon(equipment);
		} else if (item.getType2() == 2) {
			setArmor(equipment);
			_owner.sendPackets(new S_SPMR(_owner));
		}
	}

	public void remove(L1ItemInstance equipment) {
		L1Item item = equipment.getItem();
		if (item.getType2() == 0) {
			return;
		}

		if (item.get_addhp() != 0) {
			_owner.addMaxHp(-item.get_addhp());
		}
		if (item.get_addmp() != 0) {
			_owner.addMaxMp(-item.get_addmp());
		}
		_owner.getAbility().addAddedStr((byte)-item.get_addstr());
		_owner.getAbility().addAddedCon((byte)-item.get_addcon());
		_owner.getAbility().addAddedDex((byte)-item.get_adddex());
		_owner.getAbility().addAddedInt((byte)-item.get_addint());
		_owner.getAbility().addAddedWis((byte)-item.get_addwis());
		if (item.get_addwis() != 0) {
			_owner.resetBaseMr();
		}
		_owner.getAbility().addAddedCha((byte) -item.get_addcha());

		int addMr = 0;
		addMr -= equipment.getMr();
		if (item.getItemId() == 20236 && _owner.isElf()) {
			addMr -= 5;
		}
		if (addMr != 0) {
			_owner.getResistance().addMr(addMr);
			_owner.sendPackets(new S_SPMR(_owner));
		}
		if (item.get_addsp() != 0) {
			_owner.getAbility().addSp(-item.get_addsp());
			_owner.sendPackets(new S_SPMR(_owner));
		}
		if (item.isHasteItem()) {
			_owner.addHasteItemEquipped(-1);
			if (_owner.getHasteItemEquipped() == 0) {
				_owner.getMoveState().setMoveSpeed(0);
				_owner.sendPackets(new S_SkillHaste(_owner.getId(), 0, 0));
				Broadcaster.broadcastPacket(_owner, new S_SkillHaste(_owner.getId(), 0, 0));
			}
		}
		_owner.getEquipSlot().removeMagicHelm(_owner.getId(), equipment);

		if (item.getType2() == 1) {
			removeWeapon(equipment);
		} else if (item.getType2() == 2) {
			removeArmor(equipment);
		}
	}

	public void setMagicHelm(L1ItemInstance item) {
		switch (item.getItemId()) {
		case 20008:
			_owner.setSkillMastery(HASTE);
			_owner.sendPackets(new S_AddSkill(0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20013:
			_owner.setSkillMastery(PHYSICAL_ENCHANT_DEX);
			_owner.setSkillMastery(HASTE);
			_owner.sendPackets(new S_AddSkill(0, 0, 0, 2, 0, 4, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20014:
			_owner.setSkillMastery(HEAL);
			_owner.setSkillMastery(EXTRA_HEAL);
			_owner.sendPackets(new S_AddSkill(1, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20015:
			_owner.setSkillMastery(ENCHANT_WEAPON);
			_owner.setSkillMastery(DETECTION);
			_owner.setSkillMastery(PHYSICAL_ENCHANT_STR);
			_owner.sendPackets(new S_AddSkill(0, 24, 0, 0, 0, 2, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		case 20023:
			_owner.setSkillMastery(GREATER_HASTE);
			_owner.sendPackets(new S_AddSkill(0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			break;
		}
	}

	public void removeMagicHelm(int objectId, L1ItemInstance item) {
		switch (item.getItemId()) {
		case 20008: 
			if (!SkillsTable.getInstance().spellCheck(objectId, HASTE)) {
				_owner.removeSkillMastery(HASTE);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 4, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20013: 
			if (!SkillsTable.getInstance().spellCheck(objectId,
					PHYSICAL_ENCHANT_DEX)) {
				_owner.removeSkillMastery(PHYSICAL_ENCHANT_DEX);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 2, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			if (!SkillsTable.getInstance().spellCheck(objectId, HASTE)) {
				_owner.removeSkillMastery(HASTE);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 4, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20014: 
			if (!SkillsTable.getInstance().spellCheck(objectId, HEAL)) {
				_owner.removeSkillMastery(HEAL);
				_owner.sendPackets(new S_DelSkill(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			if (!SkillsTable.getInstance().spellCheck(objectId, EXTRA_HEAL)) {
				_owner.removeSkillMastery(EXTRA_HEAL);
				_owner.sendPackets(new S_DelSkill(0, 0, 4, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20015: 
			if (!SkillsTable.getInstance().spellCheck(objectId,
					ENCHANT_WEAPON)) {
				_owner.removeSkillMastery(ENCHANT_WEAPON);
				_owner.sendPackets(new S_DelSkill(0, 8, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			if (!SkillsTable.getInstance().spellCheck(objectId, DETECTION)) {
				_owner.removeSkillMastery(DETECTION);
				_owner.sendPackets(new S_DelSkill(0, 16, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			if (!SkillsTable.getInstance().spellCheck(objectId,
					PHYSICAL_ENCHANT_STR)) {
				_owner.removeSkillMastery(PHYSICAL_ENCHANT_STR);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 2, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		case 20023:
			if (!SkillsTable.getInstance().spellCheck(objectId,
					GREATER_HASTE)) {
				_owner.removeSkillMastery(GREATER_HASTE);
				_owner.sendPackets(new S_DelSkill(0, 0, 0, 0, 0, 0, 32, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			break;
		}
	}

}