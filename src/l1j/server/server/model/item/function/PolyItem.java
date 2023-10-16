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

package l1j.server.server.model.item.function;

import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_ShowPolyList;
import l1j.server.server.templates.L1Item;

@SuppressWarnings("serial")
public class PolyItem extends L1ItemInstance{

	/*-------------------------------------------------------------------------------*/
	//          멤 버 필 드
	/*-------------------------------------------------------------------------------*/
	L1Character _user = null;
	int _polyId = 0;
	int _polyTime = 0;
	/*-------------------------------------------------------------------------------*/

	/*-------------------------------------------------------------------------------*/
	//         생  성  자
	/*-------------------------------------------------------------------------------*/
	public PolyItem(L1Item item){

	}
	/*-------------------------------------------------------------------------------*/

	/*-------------------------------------------------------------------------------*/
	//        Click Item 재 정 의
	/*-------------------------------------------------------------------------------*/
	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){
		_user = cha;
		if(cha instanceof L1PcInstance){
			L1PcInstance _pc = (L1PcInstance)cha;
			if(!ActionPolyState(_pc)) { return; }
		}
	}
	/*-------------------------------------------------------------------------------*/

	/*-------------------------------------------------------------------------------*/
	//        아이템별 처리 구간
	/*-------------------------------------------------------------------------------*/
	/** @category 변신에따른 분류 **/
	private boolean ActionPolyState(L1PcInstance _pc){
		if(!UnableUseMap(_pc)) { return false;}
		if(!UnableDragonScale(_pc)) { return false;}
		switch(getItemId()){
		case 41154: //어둠의 비늘
			_polyId = 3881; _polyTime = 600; calcPloyTime(_pc); break;
		case 41155: //열화의 비늘
			_polyId = 3126; _polyTime = 600; calcPloyTime(_pc); break;
		case 41156: //배덕의 비늘
			_polyId = 3888; _polyTime = 600; calcPloyTime(_pc); break;
		case 41157: //증오의 비늘
			_polyId = 3784; _polyTime = 600; calcPloyTime(_pc); break;
		case 5000103: //망각의 비늘
			_polyId = 3101; _polyTime = 600; calcPloyTime(_pc); break;
		case 41143: //라버본 헤드 변신물약
			_polyId = 6086; _polyTime = 900; break;
		case 41144: //라버본 솔저 변신물약
			_polyId = 6087; _polyTime = 900; break;
		case 41145: //라버본 나이트 변신물약
			_polyId = 6088; _polyTime = 900; break;
		case 5000099: //변신석[라미아스]
			_polyId = 5284; _polyTime = 1200; break;
		case 5000100: //변신석[엔디아스]
			_polyId = 5293; _polyTime = 1200; break;
		case 5000101: //변신석[이데아]
			_polyId = 5297; _polyTime = 1200; break;
		case 5000157: // 엄청난 소녀 케레니스
			_polyId = 7847; _polyTime = 1200; break;
		case 5000158: // 어여쁜 소녀 케레니스
			_polyId = 7845; _polyTime = 1200; break;
		case 5000159: // 화난 소녀 케레니스
			_polyId = 7847; _polyTime = 1200; break;
		case 5000160: // 소녀 케레니스
			_polyId = 7845; _polyTime = 1200; break;
		case L1ItemId.SHARNA_POLYSCROLL_LV30: checkSharnaState(_pc, 0); break;
		case L1ItemId.SHARNA_POLYSCROLL_LV40: checkSharnaState(_pc, 1); break;
		case L1ItemId.SHARNA_POLYSCROLL_LV52: checkSharnaState(_pc, 2); break;
		case L1ItemId.SHARNA_POLYSCROLL_LV55: checkSharnaState(_pc, 3); break;
		case L1ItemId.SHARNA_POLYSCROLL_LV60: checkSharnaState(_pc, 4); break;
		case L1ItemId.SHARNA_POLYSCROLL_LV65: checkSharnaState(_pc, 5); break;
		case L1ItemId.SHARNA_POLYSCROLL_LV70: checkSharnaState(_pc, 6); break;
		case L1ItemId.POLYSCROLL_ARC:
		case L1ItemId.POLYBOOK_ARC:    checkPolyArcState(_pc); return true;
		}
		_pc.getInventory().removeItem(this, 1);
		L1PolyMorph.doPoly(_pc, _polyId, _polyTime, L1PolyMorph.MORPH_BY_ITEMMAGIC);
		return true;
	}
	/*-------------------------------------------------------------------------------*/

	/** @category 아크변신 주문서, 아크변신 마법서 **/
	private void checkPolyArcState(L1PcInstance _pc){
		_pc.sendPackets(new S_ShowPolyList(_pc.getId(),"archmonlist"));
		if (!_pc.isArchShapeChange()) {
			_pc.setArchShapeChange(true);
			_pc.setArchPolyType(true);
		}

		if(getItemId() == L1ItemId.POLYSCROLL_ARC){
			_pc.getInventory().removeItem(this, 1);
		}
	}

	/** @category 샤르나 변신 주문서 30 40 52 55 60 65 70 **/
	private void checkSharnaState(L1PcInstance _pc, int state){
		switch(_pc.getClassId()){
		case L1PcInstance.CLASSID_PRINCE:    _polyId = 6822+(10*state); break;
		case L1PcInstance.CLASSID_PRINCESS:    _polyId = 6823+(10*state); break;

		case L1PcInstance.CLASSID_KNIGHT_MALE:   _polyId = 6824+(10*state); break;
		case L1PcInstance.CLASSID_KNIGHT_FEMALE:  _polyId = 6825+(10*state); break;

		case L1PcInstance.CLASSID_ELF_MALE:    _polyId = 6826+(10*state); break;
		case L1PcInstance.CLASSID_ELF_FEMALE:   _polyId = 6827+(10*state); break;

		case L1PcInstance.CLASSID_WIZARD_MALE:   _polyId = 6828+(10*state); break;
		case L1PcInstance.CLASSID_WIZARD_FEMALE:  _polyId = 6829+(10*state); break;

		case L1PcInstance.CLASSID_DARKELF_MALE:   _polyId = 6830+(10*state); break;
		case L1PcInstance.CLASSID_DARKELF_FEMALE:  _polyId = 6831+(10*state); break;

		case L1PcInstance.CLASSID_DRAGONKNIGHT_MALE: _polyId = 7139+(4*state); break;
		case L1PcInstance.CLASSID_DRAGONKNIGHT_FEMALE: _polyId = 7140+(4*state); break;

		case L1PcInstance.CLASSID_ILLUSIONIST_MALE:  _polyId = 7141+(4*state); break;
		case L1PcInstance.CLASSID_ILLUSIONIST_FEMALE: _polyId = 7142+(4*state); break;
		}
		_polyTime = 1800;
	}


	/** @category 성향치에따라 변신 시간 조절 [비늘변신, 변신석 변신]**/
	private void calcPloyTime(L1PcInstance _pc){
		if(_pc != null){
			int lawful = _pc.getLawful();
			double percentage = (lawful / 32768); 
			_polyTime += (percentage > 0 ? percentage*(_polyTime/2) : percentage*(_polyTime/2*-1));
		}
	}

	/** @category 변신못하는 멥 체크 **/
	private boolean UnableUseMap(L1PcInstance _pc){
		if(_pc != null){
			if (_pc.getMapId() == 9000 || _pc.getMapId() == 9100) {
				_pc.sendPackets(new S_ServerMessage(563));
				return false;

			}
		}
		return true;
	}

	/** @category 용기사 각성 상태에서 변신 불가 **/
	private boolean UnableDragonScale(L1PcInstance _pc){
		if(_pc != null){
			if (_pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SCALES_EARTH_DRAGON)
					|| _pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SCALES_FIRE_DRAGON)
					|| _pc.getSkillEffectTimerSet().hasSkillEffect( L1SkillId.SCALES_WATER_DRAGON)) {
				_pc.sendPackets(new S_ServerMessage(1384));
				return false;
			}
		}
		return true;
	}
}