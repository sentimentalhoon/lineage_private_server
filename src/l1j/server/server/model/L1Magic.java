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

import java.util.Random;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.CalcStat;
import static l1j.server.server.model.skill.L1SkillId.*;

public class L1Magic {

	/*-----------------------------------------------------------------------------------------*/
	//										�� �� �� �� �� ��
	/*-----------------------------------------------------------------------------------------*/
	/** ��� �ʵ� **/
	private L1Character _attacker = null;	//Attacker
	private L1Character _target = null;		//Target
	private int _calcType = 0;				//Calculator Type
	private final int PC_PC = 1;			//PC->PC
	private final int PC_NPC = 2;			//PC->NPC
	private final int NPC_PC = 3;			//NPC->PC
	private final int NPC_NPC = 4;			//NPC->NPC
	private int _skillId = 0;				//Skill ID
	private int _probability = 0;			//Probability
	private double _damage = 0;				//Magic Damage
	private double _heal = 0;				//Healing Damage
	private double _attrResist = 0;			//Attribute Resist
	private int _leverage = 0;				//leverage
	private L1Skills _skillTemplate = null;	//Skill Template
	private Random _random = new Random(System.nanoTime());	//Random Function

	private int[] hiddenPetrifactionItem = {20229, 20230, 20117};	//��ȭ������ ���� �ɼ� ������
	/*-----------------------------------------------------------------------------------------*/


	/*-----------------------------------------------------------------------------------------*/
	//										��    ��    ��
	/*-----------------------------------------------------------------------------------------*/
	/** ������ **/
	public L1Magic(L1Character attacker, L1Character target){
		_attacker = attacker;
		_target = target;
		TypeCheck();
	}
	/*-----------------------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------------------*/
	//										�� �� �� �� �� ��
	/*-----------------------------------------------------------------------------------------*/
	private void TypeCheck(){
		if(_attacker instanceof L1PcInstance){
			if(_target instanceof L1PcInstance){
				_calcType = PC_PC;
			} else {
				_calcType = PC_NPC;
			}
		} else {
			if(_target instanceof L1PcInstance){
				_calcType = NPC_PC;
			} else {
				_calcType = NPC_NPC;
			}
		}
	}
	/**
	 * @param cha
	 * @return Spell Power Value
	 */
	private int getSP(L1Character cha){
		return cha.getAbility().getSp();
	}

	/**
	 * @param L1Character character
	 * @return Magic Level Value
	 * */
	private int getMagicLevel(L1Character cha){
		return cha.getAbility().getMagicLevel();
	}

	/**
	 * @param L1Character character
	 * @return Magic Bonus Value
	 * */
	private int getMagicBonus(L1Character cha){
		return cha.getAbility().getMagicBonus();
	}

	/**
	 * @param L1Character character
	 * @return Lawful Value
	 * */
	private int getLawful(L1Character cha){
		return cha.getLawful();
	}

	/**
	 * @param L1Character character
	 * @return Magic Resist Value
	 * */
	private int getMR(L1Character cha){
		return cha.getResistance().getMr();
	}

	/**
	 * @param L1PcInstance PC
	 * @return Magic Hit-Up By Armor Value
	 * */
	/*private int getMagicHitByArmor(L1PcInstance pc){
		return pc.getMagicHitupByArmor();
	}*/

	/**
	 * @param Nothing
	 * @return Leverage Value
	 * */
	public int getLeverage(){
		return _leverage;
	}

	/**
	 * @param Leverage Value
	 * @return Nothing
	 * */
	public void setLeverage(int leverage){
		_leverage = leverage;
	}
	/*-----------------------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------------------*/
	//										Ȯ �� �� �� �� ��
	/*-----------------------------------------------------------------------------------------*/
	/**
	 * @param Skill Id at skills Table
	 * @return Success : true / Not Success : false
	 * */
	public boolean calcProbabilityMagic(int skillId){
		_skillId = skillId;													//��ų ID ����
		_skillTemplate = SkillsTable.getInstance().getTemplate(skillId);	//��ų ���ø� ����

		//����ó�� ����
		if(!FailTypeCheck()) { return false; }

		//100% Ȯ���� ������ ��ų
		if(AbsoluteSuccess()) { return true; }

		return calcProbability();
	}

	/**
	 * @param Nothing
	 * @return Fail Check [ nothing : true / is checked : false ]
	 * */
	private boolean FailTypeCheck(){

		//Ÿ���� �κ��������̸� ���� �Ұ���.
		if(_target.isInvisble()) { return false; }

		//Ư�����ǿ��� ��ƾ��ϴ� ���� üũ
		if(!CheckforMonster()){ return false; }

		//�ű� Ŭ�����Դ� ������ �ȵȴ�.
		if(!FailNewClan()){ return false; }

		//�ߺ������� �ȵǴ� ��ų üũ
		if(!OverlapCheck()){ return false; }

		// pvp �� safety zone ���� ���Ұ� ����
		if(!isSaftyZoneCheck()) { return false; }

		//���� ������  �������� �Ұ����� ����
		if(!isCantResurrectType()){return false;}

		return true;
	}

	/**
	 * �ű� ���� ���� �ȵȴ�.
	 * @return Fail Check [ nothing : true / is checked : false ]
	 * */
	public boolean FailNewClan(){
		/** �űԺ�ȣ�ҽ� : �űԷ��� Ȯ������ ������ ���� **/
		if(_calcType == PC_PC){
			L1PcInstance target = (L1PcInstance) _target;
			L1PcInstance attacker = (L1PcInstance) _attacker;
			/*
			if(_target.getLevel() <= Config.MAX_LEVEL || attacker.getLevel() <= Config.MAX_LEVEL){       
				if (_skillId != L1Skills.TYPE_CHANGE) { // ������
					attacker.sendPackets(new S_SystemMessage("\\fW�ű� ����"+Config.MAX_LEVEL+"���϶� ������ �����մϴ�."));
					target.sendPackets(new S_SystemMessage("\\fW�űԺ�ȣ�� �޽��ϴ�."));
					return false;
				}
			}*/
			/********* �ű� ������ ��� Ȯ�� 0% **********/
			if(target.getClanid() == Config.NewClan1 && !target.isPinkName() || target.getClanid() == Config.NewClan2 && !target.isPinkName()
					|| target.getClanid() == Config.NewClan3 && !target.isPinkName() || target.getClanid() == Config.NewClan4 && !target.isPinkName()
					|| attacker.getClanid() == Config.NewClan1 || attacker.getClanid() == Config.NewClan2 
					|| attacker.getClanid() == Config.NewClan3 || attacker.getClanid() == Config.NewClan4){
				if (_skillId != L1Skills.TYPE_HEAL // �� ��
						&& _skillId != L1Skills.TYPE_CHANGE) { // ������
					//attacker.sendPackets(new S_SystemMessage("�ű� ���Ϳ��� ������ �� �����ϴ�."));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @param Nothing
	 * @return Npc Check & Buff Check [ Available : true / Unavailable : false ]
	 * */
	private boolean CheckforMonster(){
		if(_target instanceof L1NpcInstance){
			L1NpcInstance npc = (L1NpcInstance) _target;	//����ȯ
			int npcId = npc.getNpcTemplate().get_npcId();	//npc id ���
			//int gfxId = npc.getNpcTemplate().get_gfxid();	//gfx id ���

			switch(npcId){
			case 45912:
			case 45913:
			case 45914: 
			case 45915: //���ѿ��� ���� �ø��� [����]
				return false;
			case 45916: //���ѿ��� �ϸ��屺 [�������� �̽��� ����]
				if(!_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_HOLY_MITHRIL_POWDER))
					return false;
			case 45941: //���ֹ��� ���� �翤 [�ż��� ������ ��]
				if(!_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_HOLY_WATER_OF_EVA))
					return false;
			case 45752: 
			case 45753: //�߷� ������, ������ [ �߷� ���� ]
				if(!_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_CURSE_BARLOG))
					return false;
			case 45675: 
			case 81082: 
			case 45625: 
			case 45674: 
			case 45685: //����(������, ������), ����, Ÿ��, ȥ�� [��������]
				if(!_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_CURSE_YAHEE))
					return false;
			case 7000007: case 7000008: case 7000009: case 7000010: case 7000011: case 7000012: case 7000013:
			case 7000014: case 7000015: case 7000016: case 7000017: case 7000018: case 7000019: case 7000020:
			case 7000021: case 7000022: case 7000023: case 7000024: case 7000025: case 7000026: case 7000027:
			case 7000028: case 7000029: case 7000030: case 7000031: case 7000032: case 7000033: case 7000034:
			case 7000035: case 7000036: case 7000037: case 7000038: case 7000039: case 7000040: case 7000041:
			case 7000042: // ���� ���� ĳ����
				return false;
			}

			//���̾�ֱ�, ���ù��� ���� ��, ������1~9 , ���ʺ�
			if(npcId >= 46068 && npcId <=46091 && _attacker.getGfxId().getTempCharGfx() == 6035)
				return false;

			//���ù��� ���� ��, ������1~9, ���ʺ�
			if(npcId >= 46092 && npcId < 46106 && _attacker.getGfxId().getTempCharGfx() == 4034)
				return false;

			//��������
			//if(gfxId == 7684 && !_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PAP_FIVEPEARLBUFF))
			//	return false;

			//�ź��� ��������
			//if(gfxId == 7805 && !_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PAP_MAGICALPEARLBUFF))
			//	return false;

			//�丣���� [������ ���ݸ���]
			//if(gfxId == 7720)
			//	return false;
		}
		return true;
	}

	/**
	 * @param Nothing
	 * @return Npc Check & Buff Check [ Available : true / Unavailable : false ]
	 * */
	private boolean OverlapCheck(){
		//Ÿ���� ���̽����� �����ϴ� 
		if(_target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ICE_LANCE)) {
			if(_skillId != L1SkillId.CANCELLATION){
				return false;
			}
		}

		//Ÿ���� ����ε��߿� ������ ����
		if(_target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.EARTH_BIND)){
			if(_skillId != L1SkillId.CANCELLATION || _skillId != L1SkillId.AREA_OF_SILENCE
					|| _skillId != L1SkillId.POLLUTE_WATER || _skillId != L1SkillId.WEAPON_BREAK
					|| _skillId != L1SkillId.DARKNESS || _skillId != L1SkillId.MASS_SLOW) {
				return false;
			}
		}

		//Ÿ���� �̷����� ���� �����϶� [ ��ø �Ұ� ]
		if(isOverlapSkill(L1SkillId.ERASE_MAGIC)) { return false;}

		//Ÿ���� ����Ƽ�׷���Ʈ �����϶� [ ��ø �Ұ� ]
		if(isOverlapSkill(L1SkillId.DISINTEGRATE)) { return false;}

		//Ÿ���� ��ũ���� �����϶� [ ��ø �Ұ� ]
		if(isOverlapSkill(L1SkillId.SHOCK_STUN)) { return false;}

		//Ÿ���� ���극��ũ �����϶� [ ��ø �Ұ� ]
		if(isOverlapSkill(L1SkillId.BONE_BREAK)) { return false;}

		return true;
	}

	/**
	 * @param Skill ID
	 * @return [ Overlapped : true / is not Overlapped : false ]
	 * */
	private boolean isOverlapSkill(int skillId){
		if(_target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DISINTEGRATE)){
			if(_skillId == L1SkillId.DISINTEGRATE) { return true; }
		}		
		return false;
	}


	/**
	 * @param Nothing
	 * @return [ Available : true / Unavailable : false ]
	 * */
	private boolean isSaftyZoneCheck(){
		if(_calcType == PC_PC){
			if(CharPosUtil.getZoneType(_attacker) == 1 || CharPosUtil.getZoneType(_target) == 1){
				switch(_skillId){
				case L1SkillId.WEAPON_BREAK:
				case L1SkillId.SLOW:
				case L1SkillId.CURSE_PARALYZE:
				case L1SkillId.MANA_DRAIN:
				case L1SkillId.DARKNESS:
				case L1SkillId.WEAKNESS:
				case L1SkillId.DISEASE:
				case L1SkillId.DECAY_POTION:
				case L1SkillId.MASS_SLOW:
				case L1SkillId.ENTANGLE:
				case L1SkillId.ERASE_MAGIC:
				case L1SkillId.EARTH_BIND:
				case L1SkillId.AREA_OF_SILENCE:
				case L1SkillId.WIND_SHACKLE:
				case L1SkillId.STRIKER_GALE:
				case L1SkillId.SHOCK_STUN:
				case L1SkillId.FOG_OF_SLEEPING:
				case L1SkillId.ICE_LANCE:
				case L1SkillId.FREEZING_BLIZZARD:
				case L1SkillId.HORROR_OF_DEATH:
				case L1SkillId.POLLUTE_WATER:
				case L1SkillId.FEAR:
				case L1SkillId.ELEMENTAL_FALL_DOWN:
				case L1SkillId.GUARD_BREAK:
				case L1SkillId.RETURN_TO_NATURE:
				case L1SkillId.FREEZING_BREATH:
				case L1SkillId.PHANTASM:
				case L1SkillId.JOY_OF_PAIN:
				case L1SkillId.CONFUSION:
				case L1SkillId.SILENCE:
				case L1SkillId.CANCELLATION:
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @param Nothing
	 * @return [ Available : true / Unavailable : false ] To Boss
	 * */
	private boolean isCantResurrectType(){
		if(_calcType == PC_NPC) {
			if(_target instanceof L1NpcInstance){
				L1NpcInstance npc = (L1NpcInstance) _target;
				if(npc.getNpcTemplate().isCantResurrect()){
					switch(_skillId){
					case L1SkillId.WEAPON_BREAK:
					case L1SkillId.SLOW:
					case L1SkillId.CURSE_PARALYZE:
					case L1SkillId.MANA_DRAIN:
					case L1SkillId.WEAKNESS:
					case L1SkillId.SILENCE:
					case L1SkillId.DISEASE:
					case L1SkillId.DECAY_POTION:
					case L1SkillId.MASS_SLOW:
					case L1SkillId.ENTANGLE:
					case L1SkillId.ERASE_MAGIC:
					case L1SkillId.AREA_OF_SILENCE:
					case L1SkillId.WIND_SHACKLE:
					case L1SkillId.STRIKER_GALE:
					case L1SkillId.FOG_OF_SLEEPING:
					case L1SkillId.ICE_LANCE:
					case L1SkillId.FREEZING_BLIZZARD:
					case L1SkillId.POLLUTE_WATER:
					case L1SkillId.RETURN_TO_NATURE:
						//case L1SkillId.PROMTION_BOW:
					case L1SkillId.THUNDER_GRAB:
					case L1SkillId.FREEZING_BREATH:
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * @param Nothing
	 * @return [ is Absolute Success : true / is Not Absolute Success : false ]
	 * */
	private boolean AbsoluteSuccess(){

		switch(_skillId){
		case L1SkillId.SMASH:
		case L1SkillId.MIND_BREAK:
		case L1SkillId.AM_BREAK:
		case L1SkillId.IllUSION_AVATAR:
		case L1SkillId.BONE_BREAK:
		case L1SkillId.JOY_OF_PAIN:
		case L1SkillId.FINAL_BURN:
			return true;
		}

		//ĵ�����̼� ���� ����
		if(_skillId == L1SkillId.CANCELLATION){
			//�ڽſ��� ������ ���
			if(_attacker == _target)
				return true;

			if(_attacker instanceof L1PcInstance && _target instanceof L1PcInstance){
				L1PcInstance attacker = (L1PcInstance)_attacker;
				L1PcInstance target = (L1PcInstance) _target;

				//���� �����ΰ��
				if(attacker.getClanid() == target.getClanid()){
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @param Nothing
	 * @return [ is Success : true / is Fail : false ]
	 * */
	private boolean calcProbability(){

		//�������� Ȯ���� ����
		calcMagicType();

		//�Ӽ������� ����
		calcProperties();

		//Ȯ�� �ֻ���
		if(_probability >= _random.nextInt(100)+1){
			return true;
		}

		return false;
	}

	/**
	 * @category Calculate for Magic Type
	 * */
	private void calcMagicType(){
		switch(_skillId){
		//Elfs Magic
		case L1SkillId.EARTH_BIND:			calcEarthBind();		break;
		case L1SkillId.ERASE_MAGIC:			calcEraseMagic();		break;
		case L1SkillId.ELEMENTAL_FALL_DOWN:	calcElementalFallDown();break;
		case L1SkillId.RETURN_TO_NATURE:	calcReturnToNature();	break;
		case L1SkillId.ENTANGLE:			calcEntangle();			break;
		case L1SkillId.AREA_OF_SILENCE:		calcAreaOfSilence();	break;
		case L1SkillId.WIND_SHACKLE:		calcWindShackle();		break;
		case L1SkillId.STRIKER_GALE:		calcStrikerGale();		break;
		case L1SkillId.POLLUTE_WATER:		calcPolluteWater();		break;

		//Knights Magic
		case L1SkillId.SHOCK_STUN:			calcShockStun();		break;
		case L1SkillId.COUNTER_BARRIER:		calcCounterBarrier();	break;

		//Dragon Knights Magic
		case L1SkillId.THUNDER_GRAB:		calcThunderGrab();		break;
		case L1SkillId.GUARD_BREAK:			calcGuardBreak();		break;
		case L1SkillId.FEAR:				calcFear();				break;
		case L1SkillId.HORROR_OF_DEATH:		calcHorrorOfDeath();	break;

		//Common Magic (Wizards)
		case L1SkillId.DECAY_POTION:		calcDecayPotion();		break;
		case L1SkillId.SILENCE:				calcSilence();			break;
		case L1SkillId.CURSE_PARALYZE:		calcCurseParalyze();	break;
		case L1SkillId.CANCELLATION:		calcCancellation();		break;
		case L1SkillId.SLOW:				calcSlow();				break;
		case L1SkillId.DARKNESS:			calcDarkness();			break;
		case L1SkillId.WEAKNESS:			calcWeakness();			break;
		case L1SkillId.CURSE_POISON:		calcCursePoison();		break;
		case L1SkillId.CURSE_BLIND:			calcCurseBlind();		break;
		case L1SkillId.WEAPON_BREAK:		calcWeaponBreak();		break;
		case L1SkillId.MANA_DRAIN:			calcManaDrain();		break;

		//DarkElfs Magic

		//Black Wizards Magic
		case L1SkillId.PANIC:				calcPanic();			break;
		case L1SkillId.CONFUSION:			calcConfusion();		break;
		case L1SkillId.PHANTASM:			calcPhantasm();			break;

		//Etc Magic
		//case L1SkillId.PUMPKIN_CURES:		calcPumpkinCurse();		break;
		//case L1SkillId.PROMTION_BOW:		calcPromtionBow();		break;

		//And Other Magic
		default:							calcOther();			break;
		}
	}

	/**
	 * @category Earth Bind
	 * */
	private void calcEarthBind(){
		_probability = (int) _skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel())* 2;
	}

	/**
	 * @category Erase Magic
	 * */
	private void calcEraseMagic(){
		_probability = (int) _skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel())* 2;
	}

	/**
	 * @category Elemental Fall Down
	 * */
	private void calcElementalFallDown(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Return To Nature
	 * */
	private void calcReturnToNature(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Entangle
	 * */
	private void calcEntangle(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Area Of Silence
	 * */
	private void calcAreaOfSilence(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Wind Shackle
	 * */
	private void calcWindShackle(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Striker Gale
	 * */
	private void calcStrikerGale(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Pollute Water
	 * */
	private void calcPolluteWater(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Shock Stun
	 * */
	private void calcShockStun(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2.2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Counter Barrier
	 * */
	private void calcCounterBarrier(){
		_probability = (int) _skillTemplate.getProbabilityValue();
	}

	/**
	 * @category Thunder Grab
	 * */
	private void calcThunderGrab(){
		_probability = (int) _skillTemplate.getProbabilityValue() + _random.nextInt(_skillTemplate.getProbabilityDice()+1);
	}

	/**
	 * @category Guard Break
	 * */
	private void calcGuardBreak(){
		_probability = (int) _skillTemplate.getProbabilityValue() + _random.nextInt(_skillTemplate.getProbabilityDice()+1);
	}

	/**
	 * @category Fear
	 * */
	private void calcFear(){
		_probability = (int) _skillTemplate.getProbabilityValue() + _random.nextInt(_skillTemplate.getProbabilityDice()+1);
	}

	/**
	 * @category Horror Of Death
	 * */
	private void calcHorrorOfDeath(){
		_probability = (int) _skillTemplate.getProbabilityValue() + _random.nextInt(_skillTemplate.getProbabilityDice()+1);
	}

	/**
	 * @category Decay Potion
	 * */
	private void calcDecayPotion(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Silence
	 * */
	private void calcSilence(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Curse Paralyze
	 * */
	private void calcCurseParalyze(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Cancellation
	 * */
	private void calcCancellation(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Slow
	 * */
	private void calcSlow(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Darkness
	 * */
	private void calcDarkness(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Weakness
	 * */
	private void calcWeakness(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Curse Poison
	 * */
	private void calcCursePoison(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Curse Blind
	 * */
	private void calcCurseBlind(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Weapon Break
	 * */
	private void calcWeaponBreak(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}
		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 5.95)) * _skillTemplate.getProbabilityValue());
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			if(pc.isElf())
				_probability/=2;
		}
	}

	/**
	 * @category Mana Drain
	 * */
	private void calcManaDrain(){
		int attacker_INT = 0;
		if(_attacker.getAbility().getInt() >= 25){
			attacker_INT = 25;
		} else {
			attacker_INT = _attacker.getAbility().getInt();
		}

		_probability = (int) ((attacker_INT - (_target.getResistance().getMr() / 6.7)) * _skillTemplate.getProbabilityValue());

		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability+=pc.getBaseMagicHitUp();
		}
	}

	/**
	 * @category Panic
	 * */
	private void calcPanic(){
		_probability = (int) (_skillTemplate.getProbabilityDice() / 10D) * (_attacker.getLevel() - _target.getLevel())
				+ _skillTemplate.getProbabilityValue();

		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability+=pc.getBaseMagicHitUp();
		}
	}

	/**
	 * @category Confusion
	 * */
	private void calcConfusion(){
		_probability = (int) _skillTemplate.getProbabilityValue();
	}

	/**
	 * @category Phantasm
	 * */
	private void calcPhantasm(){
		_probability = (int) _skillTemplate.getProbabilityValue();
	}

	/**
	 * @category Pumpkin Curse
	 * */
	private void calcPumpkinCurse(){
		_probability = (int) (_skillTemplate.getProbabilityValue() + (_attacker.getLevel() - _target.getLevel()) * 2);
		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;
			_probability += (pc.getBaseMagicHitUp()*2);
		}
	}

	/**
	 * @category Promtion Bow
	 * */
	private void calcPromtionBow(){
		_probability = (int) _skillTemplate.getProbabilityValue()+_random.nextInt(_skillTemplate.getProbabilityDice()+1);
	}

	/**
	 * @category Other
	 * */
	private void calcOther(){
		_probability = _skillTemplate.getProbabilityValue();
		int dice = _skillTemplate.getProbabilityDice();
		if(dice <= 0) dice = 1;
		int count = 1;

		if(_attacker instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance) _attacker;

			switch(pc.getClassId()){
			case L1PcInstance.CLASSID_WIZARD_MALE:
			case L1PcInstance.CLASSID_WIZARD_FEMALE:
				count = getMagicBonus(_attacker) + getMagicLevel(_attacker)+1;
				break;

			case L1PcInstance.CLASSID_DRAGONKNIGHT_MALE:
			case L1PcInstance.CLASSID_DRAGONKNIGHT_FEMALE:
				count = getMagicBonus(_attacker) + getMagicLevel(_attacker);
				break;

			default:
				count = getMagicBonus(_attacker) + getMagicLevel(_attacker)-1;
				break;
			}
		} else {
			count = getMagicBonus(_attacker) + getMagicLevel(_attacker);
		}

		if(count < 1){
			count = 1;
		}

		for(int i = 0; i < count; ++i){
			_probability += _random.nextInt(dice);
		}

		_probability -= _target.getResistance().getMr();

		if(_skillId == L1SkillId.TAMING_MONSTER){
			double revision = 1;
			if((_target.getMaxHp() * 1/4) > _target.getCurrentHp()) { revision = 1.3; }
			if((_target.getMaxHp() * 2/4) > _target.getCurrentHp()) { revision = 1.2; }
			if((_target.getMaxHp() * 3/4) > _target.getCurrentHp()) { revision = 1.1; }

			_probability *= revision;
		}
	}


	/**
	 * @category Calculate for Properties
	 * */
	private void calcProperties(){
		switch(_skillId){

		//Hold
		case L1SkillId.EARTH_BIND:
			_probability -= _target.getResistance().getHold();
			break;

			//Stun
		case L1SkillId.SHOCK_STUN:
			_probability -= _target.getResistance().getStun();
			break;

			//Petrifaction
		case L1SkillId.CURSE_PARALYZE:			//������ Ŀ���з�������
		case L1SkillId.CURSE_PARALYZE2:			//���ֹ��� ��Ȧ��, ���ֹ��� �޵λ�
		case L1SkillId.MOB_CURSEPARALYZ_18:		//�޵λ��, ��Ȧ����, ���Ƿ�
		case L1SkillId.MOB_CURSEPARALYZ_19:		//������
		case L1SkillId.MOB_COCA:				//��īƮ���� ��
			_probability -= _target.getResistance().getPetrifaction();
			HiddenPetrifaction();	//���� ��ȭ ���� Ȯ�� ����
			break;

			//Sleep
		case L1SkillId.FOG_OF_SLEEPING:
			_probability -= _target.getResistance().getSleep();
			break;

			//Freez
		case L1SkillId.ICE_LANCE:
		case L1SkillId.FREEZING_BLIZZARD:
			_probability -= _target.getResistance().getFreeze();
			break;

			//Blind
		case L1SkillId.CURSE_BLIND:
		case L1SkillId.DARKNESS:
		case L1SkillId.DARK_BLIND:
			_probability -= _target.getResistance().getBlind();
			break;
		}
	}

	private void HiddenPetrifaction(){
		for(int i = 0; i < hiddenPetrifactionItem.length; ++i){
			L1ItemInstance item = _target.getInventory().findItemId(hiddenPetrifactionItem[i]);
			if(item != null){
				if(item.isEquipped()){
					_probability -= (_probability*(item.getEnchantLevel()/5));
					if(_probability < 0) {
						_probability = 0;
						//System.out.println("�з������� ��ȿ : "+_probability);
					}
				}
			}
		}
	}

	/*-----------------------------------------------------------------------------------------*/
	//									�� �� �� �� �� �� �� �� ��
	/*-----------------------------------------------------------------------------------------*/

	/**
	 * @category Calculate Magic Damage
	 * */
	public int calcMagicDamage(int skillId) {
		_skillId = skillId;
		_skillTemplate = SkillsTable.getInstance().getTemplate(skillId);

		switch(_skillId){
		case L1SkillId.FINAL_BURN:
			_damage = _attacker.getCurrentMp();
			break;
		case L1SkillId.THEBAE_CHASER:
		case L1SkillId.EVEIL_REVERSE:
		case L1SkillId.KUKULCAN_CHASER:
		case L1SkillId.DEATHKNIGHT_SUNBURST:
			calcMagicDiceDamage();
			break;
		default:
			calcMagicDiceDamage();
			_damage = (_damage * getLeverage()) / 10;
			break;
		}

		//===================== �߰� ������ ó��

		//���� ������� ���ݷ�
		if(CheckDragonBoold()) { _damage*=1.5;}

		//===================== ������ ó��

		//������ ������
		calcReduction();
		//���������� ������ ����
		calcMrDefense();
		//���ȿ����� �������氨 [Ȯ�� 1/2 ������]
		calcReductionByMaan();
		//������ ����, ��� ������ ��ȣ�ۿ� ( 1/2 ������ )
		WarAreaReductionBySummons();
		//===================== 0 ������ ó�� 

		//������ �־�߸� ���ݰ����� ���ͷ�
		if(!CheckforMonster()) { _damage = 0; }

		//�ű� Ŭ��
		if(!FailNewClan()) { _damage = 0; }

		//ī���͹̷��� �ɸ� ���
		if(calcCounterMirror()) { _damage = 0; }

		//�������� �������� 0������ ó��
		InvalidateWideAreaDamage();

		//ü�º��� ���� �������� ���� ó��
		revisionOverDamage();
		/**
		 * ������ üũ �κ�
		 */
		/*
		if(_attacker instanceof L1PcInstance){
			L1PcInstance attacker = (L1PcInstance) _attacker;
			String skillname = _skillTemplate.getName();
			int dmg = (int) _damage;
			attacker.sendPackets(new S_SystemMessage(skillname + " : " + dmg));
		}*/
		return (int) _damage;
	}

	/**
	 * @category Calculate Magic Dice Damage
	 * */
	private void calcMagicDiceDamage(){
		int dice = _skillTemplate.getDamageDice();
		int diceCount = _skillTemplate.getDamageDiceCount();
		_damage = _skillTemplate.getDamageValue();

		int SpellPower = _attacker.getAbility().getSp();

		if(dice <= 0) { dice = 1; };

		//�ֻ��� ������
		for(int i = 0; i < diceCount; ++i){
			_damage += _random.nextInt(dice)+1;
		}

		if(_attacker instanceof L1PcInstance){
			//���⿡���� �߰�������
			L1PcInstance attacker = (L1PcInstance) _attacker;
			L1ItemInstance weapon = attacker.getEquipSlot().getWeapon();
			if(weapon != null){
				_damage += weapon.getItem().getMagicDmgModifier();
			}
			//���̽� ���� ���ʽ�
			_damage += attacker.getBaseMagicDmg();
		}

		//INT�� ���� �߰� ������ (1/2)
		_damage += (_attacker.getAbility().getInt()/2);

		//��� ����
		calcAttrResistance(_skillTemplate.getAttr());
		double coefficient = (1.0 - _attrResist + SpellPower * 3.2 / 32.0);
		if(coefficient < 0) { coefficient = 0; }
		_damage *= coefficient;

		//ũ��Ƽ�� ������ ���� 
		calcCriticalMagicDamage();

	}

	/**
	 * @category Calculate Magic Critical
	 * */
	private void calcCriticalMagicDamage(){
		double criticalCoefficient = 1.5;

		if(_attacker instanceof L1PcInstance){
			L1PcInstance attacker = (L1PcInstance) _attacker;
			int propCritical = CalcStat.calcBaseMagicCritical(attacker.getType(), attacker.getAbility().getBaseInt());
			if(occurCritical(propCritical)){
				_damage *= (1.5+calcMaanCritical());
			}
		} else {
			if(_random.nextInt(100)+1 <= 10){
				_damage *= criticalCoefficient;
			}
		}
	}

	/**
	 * @category Calculate Reduction By Maan
	 * */
	private void calcReductionByMaan(){
		if (_target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.FAFU_MAAN)
				|| _target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHAPE_MAAN)
				|| _target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.LIFE_MAAN)) {
			if (_random.nextInt(100) + 1 <= 10) {
				_damage /= 2;
			}
		}
	}

	/**
	 * @category Invalidate Wide Area Damage
	 * */
	private void InvalidateWideAreaDamage(){
		if(_attacker instanceof L1PcInstance && _target instanceof L1PcInstance){
			L1PcInstance attacker = (L1PcInstance) _attacker;
			L1PcInstance target = (L1PcInstance) _target;

			if(attacker.getClanid()>0 && (attacker.getClanid() == target.getClanid())){
				if(isWideAreaSkill(_skillId)) { _damage = 0;}
			}
			if(attacker.getPartyID()>0 && (attacker.getPartyID() == target.getPartyID())){
				if(isWideAreaSkill(_skillId)) { _damage = 0;}
			}
		}
	}

	/**
	 * @param Skill ID
	 * @return [ is Wide Area : true / is One Target : false ]
	 * */
	private boolean isWideAreaSkill(int skillId){
		switch(skillId){
		case L1SkillId.LIGHTNING:			//����Ʈ��
		case L1SkillId.FROZEN_CLOUD:		//������ Ŭ����
		case L1SkillId.FIREBALL:			//���̾� ��
		case L1SkillId.EARTH_JAIL:			//� ����
		case L1SkillId.TORNADO:				//����̵�
		case L1SkillId.FIRE_WALL:			//���̾� ��
		case L1SkillId.BLIZZARD:			//���ڵ�
		case L1SkillId.EARTHQUAKE:			//� ����ũ
		case L1SkillId.LIGHTNING_STORM:		//����Ʈ�� ����
		case L1SkillId.FIRE_STORM:			//���̾� ����
		case L1SkillId.METEOR_STRIKE:		//��Ƽ�� ��Ʈ����ũ
		case L1SkillId.FREEZING_BLIZZARD:	//����¡ ���ڵ�
		case L1SkillId.MAGMA_BREATH:		//���׸� �극��
		case L1SkillId.SHOCK_SKIN:			//��ũ��Ų
		case L1SkillId.FREEZING_BREATH:		//����¡ �극��
			return true;
		}
		return false;
	}

	/**
	 * @category Occur Critical
	 * */
	private boolean occurCritical(int prop) {
		int chance = _random.nextInt(100) + 1;
		if (prop <= 0) {return false;}
		if (chance <= prop) {return true;}
		return false;
	}

	/**
	 *@category Calculate Maan Critical 
	 * */
	private int calcMaanCritical(){
		if(_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.LIND_MAAN)
				|| _attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHAPE_MAAN)
				||_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.LIFE_MAAN)) {
			return 1;
		}
		return 0;
	}

	/**
	 * @category Calculate Attribute Resistance
	 * */
	public double calcAttrResistance(int attr){
		int resist = 0;
		switch(attr){
		case L1Skills.ATTR_EARTH:
			resist = _target.getResistance().getEarth();
			break;
		case L1Skills.ATTR_FIRE:
			resist = _target.getResistance().getFire();
			break;
		case L1Skills.ATTR_WATER:
			resist = _target.getResistance().getWater();
			break;
		case L1Skills.ATTR_WIND:
			resist = _target.getResistance().getWind();
			break;
		}

		int resistFloor = (int)(0.32 * Math.abs(resist));
		if(resist >= 0)
			resistFloor*=1;
		else
			resistFloor*=(-1);

		_attrResist = (resistFloor / 32.0);

		return _attrResist;
	}

	/**
	 * @category War Area Reduction By Summon & Pet
	 * */
	private void WarAreaReductionBySummons(){
		boolean isNowWar = false;
		int castleId = L1CastleLocation.getCastleIdByArea(_target);
		if(castleId > 0){
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}

		//�������϶�
		if(!isNowWar){
			//Ÿ���� PcInstance �ΰ��
			if(_target instanceof L1PcInstance){
				if(_attacker instanceof L1PetInstance){ //�����ڰ� ���ΰ��
					_damage /= 2;	
				}
				if(_attacker instanceof L1SummonInstance){ //�����ڰ� �����ΰ��
					L1SummonInstance summon = (L1SummonInstance) _target;
					if(summon.isExsistMaster()){
						_damage /= 2;
					}
				}
			}

			//Ÿ���� PetInstance�ΰ��
			if(_target instanceof L1PetInstance){
				if(_attacker instanceof L1PcInstance){ //�����ڰ� PcInstance�ΰ��
					_damage /= 2;	
				}

			}

			//Ÿ���� SummonInstance�ΰ��
			if(_target instanceof L1SummonInstance){
				if(_attacker instanceof L1PcInstance){ //�����ڰ� PcInstance�ΰ��
					L1SummonInstance summon = (L1SummonInstance) _target;
					if(summon.isExsistMaster()){
						_damage /= 2;
					}
				}
			}
		}
	}

	/**
	 * @param Nothing
	 * @return [ Available : true / Unavailable : false ]
	 * */
	private boolean CheckDragonBoold(){
		//��Ÿ���� ���� [��Ǫ���¿��� 1.5�� ������]
		if(_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_A)){
			if(_target instanceof L1NpcInstance){
				L1NpcInstance target = (L1NpcInstance) _target;
				int gfx_id = target.getNpcTemplate().get_gfxid();
				//[7869 : 2-1��] [7870 : 3-1~2��] [ 7864 : 1�� ] [1590 : ������ ��]
				if(gfx_id == 7864 || gfx_id == 7869 || gfx_id == 7870){
					return true;
				}
			}
		}

		if(_attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGONBLOOD_P)){
			if(_target instanceof L1NpcInstance){
				L1NpcInstance target = (L1NpcInstance) _target;
				int gfx_id = target.getNpcTemplate().get_gfxid();
				//��������
				if(gfx_id == 2544){ 
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @category Calculate Damage Reduction
	 * */
	private void calcReduction(){

		SkillEffectTimerSet timerSet = _target.getSkillEffectTimerSet();

		if(_target instanceof L1PcInstance){
			L1PcInstance target = (L1PcInstance) _target;

			//������ ���� ������
			_damage -= target.getDamageReductionByArmor();

			//������ ���� Ȯ�� ������
			for(L1DollInstance doll : target.getDollList().values()){
				_damage -= doll.getDamageReductionByDoll();
			}
		}

		//����ȿ丮������ �������氨
		if(timerSet.hasSkillEffect(L1SkillId.SPECIAL_COOKING)) { _damage -= 5; }
		//�����
		if(timerSet.hasSkillEffect(L1SkillId.FEATHER_BUFF_A)) { _damage -= 3; }
		if(timerSet.hasSkillEffect(L1SkillId.FEATHER_BUFF_B)) { _damage -= 2; }

		//���̼ǽ� [ȯ����]
		if(timerSet.hasSkillEffect(L1SkillId.PATIENCE)) { _damage -= 2; }

		//�巡�ｺŲ [����]
		if(timerSet.hasSkillEffect(L1SkillId.DRAGON_SKIN)) { _damage -= 3; }

		//������ �Ƹ� [���]
		if(timerSet.hasSkillEffect(L1SkillId.REDUCTION_ARMOR)) { _damage -= (_target.getLevel() - 50)/5 + 1 ; }
		
		// �ƹ�Ÿ [����]
		if(timerSet.hasSkillEffect(L1SkillId.AVATA)) { _damage -= (_target.getLevel() - 50)/5 + 5 ; }

		//��ܸ���
		if(timerSet.hasSkillEffect(L1SkillId.CALL_LIGHTNING)) { if(_damage > 30) { _damage = 15; } }

		//�Ϸ�� �ƹ�Ÿ [ȯ����]
		if(timerSet.hasSkillEffect(L1SkillId.IllUSION_AVATAR)) { _damage +=(_damage/5); }

		//�̹����� [������ �׻� �������� ����]
		if(timerSet.hasSkillEffect(L1SkillId.IMMUNE_TO_HARM)) { _damage /= 2; }

		//�ٽ��̳� ��ī���� ����ִٸ� ������ 0
		if(timerSet.hasSkillEffect(L1SkillId.MOB_BASILL)|| timerSet.hasSkillEffect(L1SkillId.MOB_COCA)) { _damage = 0; }
	}

	/**
	 * @category Calculate Counter Mirror
	 * */
	private boolean calcCounterMirror(){
		//����� ī���� �̷����̶��
		if(_target.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.COUNTER_MIRROR)){
			int MirrorLevel = (_target.getAbility().getInt()) - 12;
			if(MirrorLevel > 0){
				//ī���� �̷� �ߵ� Ȯ�� (INT 13������ 5%�� ����.)
				if(MirrorLevel*5 >= _random.nextInt(100)+1){
					//�����ڰ� PC�ΰ��
					if(_attacker instanceof L1PcInstance && _target instanceof L1PcInstance){
						L1PcInstance attacker = (L1PcInstance) _attacker;
						L1PcInstance target = (L1PcInstance) _target;

						attacker.sendPackets(new S_DoActionGFX(attacker.getId(),ActionCodes.ACTION_Damage));
						Broadcaster.broadcastPacket(attacker,new S_DoActionGFX(attacker.getId(),ActionCodes.ACTION_Damage));

						target.sendPackets(new S_SkillSound(target.getId(),4395));
						Broadcaster.broadcastPacket(target, new S_SkillSound(target.getId(), 4395));

						attacker.receiveDamage(target, _damage, false);
						target.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.COUNTER_MIRROR);
					}

					//�����ڰ� NPC�ΰ��
					if(_attacker instanceof L1NpcInstance && _target instanceof L1PcInstance){
						L1NpcInstance attacker = (L1NpcInstance) _attacker;
						L1PcInstance target = (L1PcInstance) _target;

						if(attacker.getNpcTemplate().get_IsErase()){
							Broadcaster.broadcastPacket(attacker, new S_DoActionGFX(attacker.getId(), ActionCodes.ACTION_Damage));

							target.sendPackets(new S_SkillSound(target.getId(), 4395));
							Broadcaster.broadcastPacket(target, new S_SkillSound(target.getId(), 4395));

							attacker.receiveDamage(target, (int)_damage);
							target.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.COUNTER_MIRROR);
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param int Damage
	 * @return int Calculated Damage
	 * */
	public void calcMrDefense() {

		int MagicResistance = 0; // ��������
		int RealMagicResistance = 0; // ����Ǵ� �������װ�
		double calMr = 0.00D; // ������
		double baseMr = 0.00D;

		MagicResistance = _target.getResistance().getEffectedMrBySkill();

		RealMagicResistance = MagicResistance - _random.nextInt(5) + 1;

		if (_calcType == PC_PC) {
			baseMr = (_random.nextInt(1000) + 98000) / 100000D;

			if (MagicResistance <= 100) {
				calMr = baseMr - (MagicResistance * 470) / 100000D;
			} 
			if (MagicResistance > 100) {
				calMr = baseMr - (MagicResistance * 470) / 100000D	+ ((MagicResistance - 100) * 0.004);
			}
		} else {
			calMr = (200 - RealMagicResistance) / 250.00D;
		}

		_damage *= calMr;
		if (_damage < 0) {
			_damage = 0;
		}
	}

	/**
	 * @category Revision Over Damage
	 * */
	private void revisionOverDamage(){
		if(_damage > _target.getCurrentHp())
			_damage = _target.getCurrentHp();
	}

	/**
	 * @return Calculate Fire wall Damage
	 * */
	public int calcFireWallDamage() {
		int dmg = 0;
		double attrDeffence = calcAttrResistance(L1Skills.ATTR_FIRE);
		L1Skills l1skills = SkillsTable.getInstance().getTemplate(L1SkillId.FIRE_WALL);
		dmg = (int) ((1.0 - attrDeffence) * l1skills.getDamageValue());

		//����� ��� �ִ��� üũ
		if(checkInvalidateBuff(_target)){ dmg = 0; }

		if (dmg < 0) {
			dmg = 0;
		}
		return dmg;
	}

	/**
	 * @param L1Character character
	 * @return [ is Invalidate : true / is Validate : false ]
	 * */
	private boolean checkInvalidateBuff(L1Character cha){
		SkillEffectTimerSet timerSet = cha.getSkillEffectTimerSet();
		if(timerSet.hasSkillEffect(L1SkillId.ABSOLUTE_BARRIER)
				|| timerSet.hasSkillEffect(L1SkillId.ICE_LANCE)
				|| timerSet.hasSkillEffect(L1SkillId.FREEZING_BLIZZARD)
				|| timerSet.hasSkillEffect(L1SkillId.FREEZING_BREATH)
				|| timerSet.hasSkillEffect(L1SkillId.EARTH_BIND)
				|| timerSet.hasSkillEffect(L1SkillId.MOB_BASILL)
				|| timerSet.hasSkillEffect(L1SkillId.MOB_COCA)){
			return true;
		}
		return false;
	}

	/**
	 * @return [ Available : true / Unavailable : false ]
	 * */
	/*
	private boolean checkPapuRadeBuff() {
		if(_target instanceof L1NpcInstance){
			L1NpcInstance target = (L1NpcInstance) _target;
			int gfx_id = target.getNpcTemplate().get_gfxid();

			if(gfx_id == 7684 && _attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PAP_FIVEPEARLBUFF)){
				return true;
			}
			if(gfx_id == 7805 && _attacker.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PAP_MAGICALPEARLBUFF)){
				return true;
			}
			if(gfx_id == 7720){
				return true;
			}
		}
		return false;
	}*/
	/*-----------------------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------------------*/
	//							�� ȸ �� �� (�𵥵忡�Դ� ������) �� ��
	/*-----------------------------------------------------------------------------------------*/
	/**
	 * @param Skill ID
	 * @return [int] Healing Point value
	 * */
	public int calcHealing(int skillId) {
		_skillId = skillId;
		_skillTemplate = SkillsTable.getInstance().getTemplate(skillId);
		int dice = _skillTemplate.getDamageDice();
		int value = _skillTemplate.getDamageValue();
		int magicBonus = this.getMagicBonus(_attacker);

		if(magicBonus > 10){ magicBonus = 10; }
		if(dice <= 0) { dice = 1; }
		int diceCount = value + magicBonus;

		for(int i = 0; i < diceCount; ++i){
			_heal += _random.nextInt(dice) + 1;
		}
		double alignmentRevision = 1.0;
		if (getLawful(_attacker) > 0) {
			alignmentRevision += (getLawful(_attacker) / 32768.0);
		}

		_heal = ((_heal * alignmentRevision) * getLeverage())/ 10;

		return (int)_heal;
	}
	/*-----------------------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------------------*/
	//									C O M M I T
	/*-----------------------------------------------------------------------------------------*/

	public void commit(int damage, int drainMana) {
		//if(checkPapuRadeBuff()) { damage = 1; }
		if(checkInvalidateBuff(_target)) { damage = 0; drainMana = 0; }
		if(drainMana > 0 && _target.getCurrentMp() > 0){
			if(drainMana > _target.getCurrentMp()){
				drainMana = _target.getCurrentMp();
			}
			_attacker.setCurrentMp(_attacker.getCurrentMp()+drainMana);
		}
		if(_target instanceof L1PcInstance){
			L1PcInstance target = (L1PcInstance) _target;
			target.receiveManaDamage(_attacker, drainMana);
			target.receiveDamage(_attacker, damage, true);
		}
		if(_target instanceof L1NpcInstance){
			L1NpcInstance target = (L1NpcInstance) _target;
			target.ReceiveManaDamage(_attacker, drainMana);
			target.receiveDamage(_attacker, damage);
		}
	}

	/*-----------------------------------------------------------------------------------------*/
}
