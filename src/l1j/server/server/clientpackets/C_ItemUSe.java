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
package l1j.server.server.clientpackets;

import static l1j.server.server.model.skill.L1SkillId.ERASE_MAGIC;
import static l1j.server.server.model.skill.L1SkillId.EXP_POTION;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CASHSCROLL;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CASHSCROLL2;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CASHSCROLL3;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_BARLOG;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_YAHEE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_DELAY;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_MITHRIL_POWDER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER_OF_EVA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.GameSystem.CrockSystem;
import l1j.server.server.ActionCodes;
import l1j.server.server.DevilController;
import l1j.server.server.SkyCastleController;
import l1j.server.server.UserCommands;
import l1j.server.server.datatables.AuctionBTable;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.MapFixKeyTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1EffectSpawn;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1ItemDelay;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1DoorInstance;
import l1j.server.server.model.Instance.L1EffectInstance;
import l1j.server.server.model.Instance.L1GuardianInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_AttackPacket;
import l1j.server.server.serverpackets.S_ChangeName;
import l1j.server.server.serverpackets.S_ChangeShape;
import l1j.server.server.serverpackets.S_DRAGONPERL;
import l1j.server.server.serverpackets.S_EffectLocation;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_Liquor;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.serverpackets.S_NoticBoard;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_Sound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_UserCommands2;
import l1j.server.server.templates.L1EtcItem;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Pet;
import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.L1SpawnUtil;
import server.LineageClient;

//Referenced classes of package l1j.server.server.clientpackets:
//ClientBasePacket

public class C_ItemUSe extends ClientBasePacket {

	private static final String C_ITEM_USE = "[C] C_ItemUSe";
	private static Logger _log = Logger.getLogger(C_ItemUSe.class.getName());

	//private static Random _random = new Random(System.nanoTime());

	Calendar currentDate = Calendar.getInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd h:mm:ss a"); 
	String time = dateFormat.format(currentDate.getTime());

	public C_ItemUSe(byte abyte0[], LineageClient client) throws Exception {
		super(abyte0);
		int itemObjid = readD();

		L1PcInstance pc = client.getActiveChar();
		if (pc.isGhost()) {
			return;
		}
		L1ItemInstance useItem = pc.getInventory().getItem(itemObjid);

		if (useItem.getItem().getUseType() == -1) { // none:사용할 수 없는 아이템
			pc.sendPackets(new S_ServerMessage(74, useItem.getLogName())); // \f1%0은 사용할 수 없습니다.	
			return;
		}
		if (pc.isTeleport()) { // 텔레포트 처리중
			return;
		}
		//존재버그 관련 추가
		L1PcInstance jonje = L1World.getInstance().getPlayer(pc.getName());
		if (jonje == null && pc.getAccessLevel() != 200) {
			client.kick();
			return;
		} 

		if (useItem == null && pc.isDead() == true) {
			return;
		}
		if (!pc.getMap().isUsableItem()) {
			pc.sendPackets(new S_ServerMessage(563)); // \f1 여기에서는 사용할 수 없습니다.
			return;
		}
		int itemId;
		try {
			itemId = useItem.getItem().getItemId();
		} catch (Exception e) {
			return;
		}
		if(useItem.isWorking()){
			if (pc.getCurrentHp() > 0) {
				useItem.clickItem(pc, this);
			}
			return;
		}
		int l = 0;
		int spellsc_objid = 0;
		int spellsc_x = 0;
		int spellsc_y = 0;

		int use_type = useItem.getItem().getUseType();
		if (itemId == 41029 // 소환공의 조각
				|| itemId == 40317 || itemId == 41036
				|| itemId == L1ItemId.LOWER_OSIRIS_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.HIGHER_OSIRIS_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.LOWER_TIKAL_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.HIGHER_TIKAL_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.TIMECRACK_CORE 
				|| itemId == L1ItemId.PROTECTION_SCROLL 
				|| itemId == L1ItemId.MAGIC_BREATH 
				|| itemId == 40964 || itemId == 41030
				|| itemId == 40925 || itemId == 40926 || itemId == 40927 // 정화·신비적인 일부
				|| itemId == 40928 || itemId == 40929 || itemId == 500231
				|| itemId == 4500162 || itemId == 447012 || itemId == 40076) { // 40076 고대의주문서
			l = readD();
		} else if (use_type == 30 || itemId == 40870 || itemId == 40879) { // spell_buff
			spellsc_objid = readD();
		} else if (use_type == 5 || use_type == 17) { // spell_long  spell_short
			spellsc_objid = readD();
			spellsc_x = readH();
			spellsc_y = readH();
		} else {
			l = readC();
		}

		if (pc.getCurrentHp() > 0) {
			int delay_id = 0;
			if (useItem.getItem().getType2() == 0) { // 종별：그 외의 아이템
				delay_id = ((L1EtcItem) useItem.getItem()).get_delayid();
			}
			if (delay_id != 0) { // 지연 설정 있어
				if (pc.hasItemDelay(delay_id) == true) {
					return;
				}
			}

			L1ItemInstance l1iteminstance1 = pc.getInventory().getItem(l);
			_log.finest("request item use (obj) = " + itemObjid + " action = " + l);
			if (useItem.getItem().getType2() == 0) { // 종별：그 외의 아이템
				int item_minlvl = ((L1EtcItem) useItem.getItem()).getMinLevel();
				int item_maxlvl = ((L1EtcItem) useItem.getItem()).getMaxLevel();

				boolean isDelayEffect = false;
				int delayEffect = ((L1EtcItem) useItem.getItem()).get_delayEffect();

				if (item_minlvl != 0 && item_minlvl > pc.getLevel() && !pc.isGm()) {
					pc.sendPackets(new S_ServerMessage(318, String.valueOf(item_minlvl))); 
					// 이 아이템은%0레벨 이상이 되지 않으면 사용할 수 없습니다.
					return;
				} else if (item_maxlvl != 0 && item_maxlvl < pc.getLevel() && !pc.isGm()) {
					pc.sendPackets(new S_ServerMessage(673, String.valueOf(item_maxlvl)));
					// 이 아이템은%d레벨 이상만 사용할 수 있습니다.
					return;
				}
				if ((itemId == 40576 && !pc.isElf()) 
						|| (itemId == 40577 && !pc.isWizard()) // 영혼의 결정의 파편(흑)
						|| (itemId == 40578 && !pc.isKnight())) { // 영혼의 결정의 파편(빨강)
					pc.sendPackets(new S_ServerMessage(264)); // \f1당신의 클래스에서는 이 아이템은 사용할 수 없습니다.
					return;
				}

				switch(itemId){
				case 7059: pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.GMSTATUS_HPBAR, 5 * 1000); removeItem(pc,useItem); break;// 사신의 눈?
				case 7064: pc.sendPackets(new S_NoticBoard(pc, 1)); break; // 위탁 판매 게시판	
				case 7065: pc.sendPackets(new S_NoticBoard(pc, 2)); break; // 공지 게시판
				case 7066: pc.sendPackets(new S_NoticBoard(pc, 3)); break; // 자유게시판
				case 7067: pc.sendPackets(new S_NoticBoard(pc, 4));  break; // 건의 게시판
				case 7068: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan1, Config.NewClanName1); break; // 신규 혈맹 가입 주문서
				case 7069: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan2, Config.NewClanName2); break; // 신규 혈맹 가입 주문서
				case 7070: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan3, Config.NewClanName3); break; // 신규 혈맹 가입 주문서
				case 7071: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan4, Config.NewClanName4); break; // 신규 혈맹 가입 주문서
				case 7072: Ress(pc, itemId); break;// 메티스의 가호, 부활
				case 40003: flashlight_Gas(pc,useItem); break;
				case L1ItemId.INCRESE_HP_SCROLL  : case L1ItemId.INCRESE_MP_SCROLL  : 
				case L1ItemId.INCRESE_ATTACK_SCROLL  : case L1ItemId.CHUNSANG_HP_SCROLL  : 
				case L1ItemId.CHUNSANG_MP_SCROLL  : case L1ItemId.CHUNSANG_ATTACK_SCROLL  : 
					useCashScroll(pc, itemId); pc.getInventory().removeItem(useItem, 1);break;
				case 40858: liquor(pc); removeItem(pc,useItem); break; //술
				case L1ItemId.EXP_POTION : 	
				case L1ItemId.EXP_POTION2 :  
					UseExpPotion(pc, itemId); 
					removeItem(pc,useItem);break;
				case 7062 : exp_Posion(pc);break;//경험치물약
				case 7063 : exp_PosionConsumable(pc);break;
				case 467009 : pc.sendPackets(new S_SystemMessage("\\fT[.캐릭명변경] [변경할아이디] 쳐주시면 됩니다. 캐릭명 변경시 전체창에 공지 됩니다.")); break; //케릭명변경주문서
				case 4500155: byBloodPledgeJoin(pc,useItem); break; //자기혈맹가입하기
				case 430104: case 430105:case 430106:
				case 430107:case 430108:case 430109:case 430110:
					if (delayEffect > 0) {
						isDelayEffect = true;
						Timestamp lastUsed = useItem.getLastUsed();
						if (lastUsed != null) {
							Calendar cal = Calendar.getInstance();
							long usedtime = (cal.getTimeInMillis() - lastUsed.getTime()) / 1000;
							if (usedtime <= delayEffect) {
								String used_time = Long.toString((delayEffect - usedtime)/60);
								pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 분 동안은 사용할 수 없습니다.
								return;
							}
						}
					}   
					pc.cancelAbsoluteBarrier(); // 아브소르트바리아의 해제
					int skillid = 0;
					if (itemId == 430106) { // 지룡의 마안
						skillid = 7671;
					} else if (itemId == 430104) { // 수룡의 마안	
						skillid = 7672;
					} else if (itemId == 430107) { // 화룡의 마안	
						skillid = 7673;
					} else if (itemId == 430105) { // 풍룡의 마안
						skillid = 7674;
					} else if (itemId == 430108) { // 탄생의 마안
						skillid = 7675;
					} else if (itemId == 430109) { // 형상의 마안
						skillid = 7676;
					} else if (itemId == 430110) { // 생명의 마안
						skillid = 7677;
					}	
					pc.setBuffnoch(1); // 스킬버그땜시 추가 올버프는 미작동
					L1SkillUse l1skilluse = new L1SkillUse();
					l1skilluse.handleCommands(client.getActiveChar(), skillid,
							spellsc_objid, spellsc_x, spellsc_y, null, 0, L1SkillUse.TYPE_SPELLSC);  
					pc.setBuffnoch(0); // 스킬버그땜시 추가 올버프는 미작동
					//// 마안 7종 추가 ////

					if (isDelayEffect) {
						Timestamp ts = new Timestamp(System.currentTimeMillis());
						useItem.setLastUsed(ts);
						pc.getInventory().updateItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
						pc.getInventory().saveItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
					}
					break;
				case 5000085 : eventT(pc , useItem, 490000, 490009 );break; // 힘의티상자 
				case 5000086 : eventT(pc , useItem, 490001, 490010 );break; // 민첩의티상자 
				case 5000087 : eventT(pc , useItem, 490002, 490011 );break; // 매력의티상자 
				case 5000088 : eventT(pc , useItem, 490003, 490012 );break; // 마력의티상자 
				case 5000089 : eventT(pc , useItem, 490004, 490013 );break; // 체력의티상자 
				case 5000090 : eventT(pc , useItem, 490005, 490014 );break; // 마나의티상자 
				case 5000091 : eventT(pc , useItem, 490006, 490015 );break; // 스턴내성의티상자 
				case 5000092 : eventT(pc , useItem, 490007, 490016 );break; // 홀드내성의티상자 
				case 5000093 : eventT(pc , useItem, 490008, 490017 );break; // 마법저항의티상자
				case 5000094 : item_change(pc, useItem,5000098);break; //정령의인장상자 
				case L1ItemId.POTION_OF_CURE_POISON : case 40507:cure_Posion(pc, itemId , useItem);	break;	//해독제
				case 40066 : case 41413: MpPosion(pc,useItem,7,6); break;//엠물약
				case 40067 : case 41414: case 140067 : MpPosion(pc,useItem,15,16); break;//엠물약
				case 40735 : MpPosion(pc,useItem,60,0); break;//엠물약
				case 40042 : MpPosion(pc,useItem,50,0); break;//엠물약
				case 41404 : MpPosion(pc,useItem,83,14); break;//엠물약
				case 41412 : MpPosion(pc,useItem,5,16); break;//엠물약
				case 500231 : dollchange(pc,useItem,l1iteminstance1); break;//인형체인지
				case 500034 : girandungeonTime(pc, useItem);	break;//기감충전
				case 500035 : girandungeonTime2(pc, useItem); break;//기감충전
				case 5000178 : ivoryTowerTime(pc, useItem); break;//상아탑충전
				case 5000179 : ldungeonTime(pc, useItem); break;//라던충전	
				case 5000148: portableStorage(pc,useItem); break;//휴대용창고
				case 5000161: cerenisCallStick(pc); break;//화난케레니스소환막대
				case 400074: CreateItemNoDelete(pc, 41159, 220, 400075, 1); break; // 깃털 200 통장
				case 400075: CreateItemNoDelete(pc, 400075, 1, 41159, 200); break;  // 깃털 200 수표
				case 400076 : adenaBankbook(pc); break;//아데나통장
				case 400077 : adenaCheck(pc); break;//아데나수표
				case 3500047:
					if (pc.getInventory(). checkItem(40308, 50000000)){  // 
						pc.getInventory().consumeItem(40308, 50000000); //
						pc.getInventory().storeItem(3500048, 1); //수표를 1개 생성
						pc.getInventory().consumeItem(3500047, 1); //수표 1개 소모
						pc.sendPackets(new S_SystemMessage("아덴 5천만이 수표로 변환되었습니다. ")); //성공시 멘트전송
					} else {
						pc.sendPackets(new S_SystemMessage("아데나 5천만을 소지하고  있어야합니다. ")); //실패시 멘트전송
					}
					break;
				case 3500048:
					pc.getInventory().storeItem(40308, 50000000); //5천만아덴 생성
					pc.getInventory().consumeItem(3500048, 1); //수표 1개 소모
					pc.sendPackets(new S_SystemMessage("아덴 50,000,000원으로 변환되었습니다. "));
					break;
				case 3500049:
					if (pc.getInventory(). checkItem(40308, 10000000)){  // 
						pc.getInventory().consumeItem(40308, 10000000); //
						pc.getInventory().storeItem(3500050, 1); //수표를 1개 생성
						pc.sendPackets(new S_SystemMessage("아덴 1천만이 수표로 변환되었습니다. ")); //성공시 멘트전송
					} else {
						pc.sendPackets(new S_SystemMessage("아데나 1천만을 소지하고  있어야합니다. ")); //실패시 멘트전송
					}
					break;
				case 3500050:
					pc.getInventory().storeItem(40308, 10000000); //1천만아덴 생성
					pc.getInventory().consumeItem(3500050, 1); //수표 1개 소모
					pc.sendPackets(new S_SystemMessage("아덴 10,000,000원으로 변환되었습니다. "));
					break;
				case 447011 : newUseSupportBox(pc); break; // 초보 지원상자
				case 447013 : newUseSupportBox2(pc); break; // 초보 지원상자
				case 5000149: FirstPkg(pc); break;// 1차 패키지 2차는 5000150
				case 5000156: SecondPkg(pc); break;// 2차패키지
				case 5000216: pc.sendPackets(new S_SystemMessage("[.살생부]를 입력하여주세요")); break;
				case 7060:
					if (pc.getKarma() <= 10000000) {
						pc.setKarma(pc.getKarma() + 100000);
						pc.sendPackets(new S_SystemMessage(pc.getName() + "님의 우호도가 향상되었습니다."));
						pc.getInventory().removeItem(useItem, 1);
					} else pc.sendPackets(new S_SystemMessage("8단계 이하에서만 사용할 수 있습니다."));     
					break;
				case 7061:
					if (pc.getKarma() >= -10000000) {
						pc.setKarma(pc.getKarma() - 100000);
						pc.sendPackets(new S_SystemMessage(pc.getName() + "님의 우호도가 향상되었습니다."));
						pc.getInventory().removeItem(useItem, 1);
					} else pc.sendPackets(new S_SystemMessage("8단계 이하에서만 사용할 수 있습니다.")); 
					break;
				case 500056 : item_Create(pc,useItem,500066,itemId,76776);break;//근거리룬
				case 500057 : item_Create(pc,useItem,500066,itemId,76775);break;//원거리룬
				case 500058 : item_Create(pc,useItem,500066,itemId,76774);break;//마법공격룬
				case 500059 : item_Create(pc,useItem,500066,itemId,76773);break;//근거리명중룬
				case 500060 : item_Create(pc,useItem,500066,itemId,76772);break;//원거리명중룬
				case 500061 : item_Create(pc,useItem,500066,itemId,76771);break;//마법명중륜 
				case 500062 : item_Create(pc,useItem,500066,itemId,76770);break;//땅의 방어룬
				case 500063 : item_Create(pc,useItem,500066,itemId,76769);break;//불의 방어룬
				case 500064 : item_Create(pc,useItem,500066,itemId,76768);break;//물의 방어룬
				case 500065 : item_Create(pc,useItem,500066,itemId,76767);break;//바람의 방어룬		
				case 500080 : item_Create(pc,useItem,500066,itemId,76777);break;//날빠 난쟁이족검 (대미지)
				case 500081 : item_Create(pc,useItem,500066,itemId,76778);break;//날빠 난쟁이족검 (명중)
				case 500082 : item_Create(pc,useItem,500066,itemId,76779);break;//손때묻은오크족활 (대미지)
				case 500083 : item_Create(pc,useItem,500066,itemId,76780);break;//손때묻은오크족활 (명중)
				case 500084 : item_Create(pc,useItem,500066,itemId,76781);break;//장로의 부저린 지팡이 (대미지)
				case 500085 : item_Create(pc,useItem,500066,itemId,76782);break;//장로의 부저린 지팡이 (명중)
				case 500086 : item_Create(pc,useItem,500066,itemId,76783);break;//꼬질꼬질한 갑옷 조각
				case 500087 : item_Create(pc,useItem,500066,itemId,76784);break;//서큐버스가 신다 버린 부츠
				case 4500170: doItPoly(pc, 8817, useItem); break; // 켄라우헬 변신
				case 4500171: doItPoly(pc, 9003, useItem); break; // 조우변신
				case 4500172: doItPoly(pc, 9206, useItem); break; // 80 데스 변신
				case 4500173: doItPoly(pc, 9226, useItem); break; // 80 다크엘프 변신
				case 42198 : KillAndDeathReset(pc,useItem);break;//킬데스초기화주문서
				case L1ItemId.PROTECTION_SCROLL:
					if (l1iteminstance1.getItem().getType2() != 0 && l1iteminstance1.getProtection() == 0){
						l1iteminstance1.setProtection(1);
						pc.getInventory().removeItem(useItem, 1);
						pc.sendPackets(new S_SystemMessage(l1iteminstance1.getLogName()+"에 마력의 기운이 스며들었습니다."));
					} else {
						pc.sendPackets(new S_ServerMessage(79));
					}
					pc.getInventory().updateItem(l1iteminstance1, L1PcInventory.COL_ENCHANTLVL);
					pc.getInventory().saveItem(l1iteminstance1, L1PcInventory.COL_ENCHANTLVL);
					break;
				case 560032: // 야히잡는 버프 주문서
					if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_BARLOG)) {
						pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, 
								"\\fR발록의 버프가 남아있습니다."));
					} else {
						pc.getSkillEffectTimerSet().setSkillEffect(STATUS_CURSE_YAHEE, 1020 * 1000);				
						pc.sendPackets(new S_PacketBox(S_PacketBox.ICON_AURA, 2, 1020));
						pc.sendPackets(new S_SkillSound(pc.getId(), 750));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 750));	
						pc.sendPackets(new S_ServerMessage(1127));
					}
					break;
				case 560033: // 발록 잡는 버프 주문서
					if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_YAHEE)) {
						pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, 
								"\\fR야히의 버프가 남아있습니다."));
					} else {
						pc.getSkillEffectTimerSet().setSkillEffect(STATUS_CURSE_BARLOG, 1020 * 1000);	//1020					
						pc.sendPackets(new S_PacketBox(S_PacketBox.ICON_AURA, 1, 1020));
						pc.sendPackets(new S_SkillSound(pc.getId(), 750));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 750));	
						pc.sendPackets(new S_ServerMessage(1127));
					}
					break;
				case 40317 : case 40508 : repairItem(pc, l1iteminstance1,useItem);break;// 숫돌  // 오리하루콘
				case L1ItemId.LOWER_OSIRIS_PRESENT_PIECE_DOWN : case L1ItemId.HIGHER_OSIRIS_PRESENT_PIECE_DOWN: 
					close_Lower_Oriris_present(pc,l1iteminstance1, useItem, itemId); break; // 오실리아보물 (상)(하)
				case L1ItemId.LOWER_TIKAL_PRESENT_PIECE_DOWN  : case L1ItemId.HIGHER_TIKAL_PRESENT_PIECE_DOWN :
					close_Lower_Tikal_Present(pc,l1iteminstance1, useItem,itemId); break;//쿠쿨칸보물상자(상)(하)
				case L1ItemId.ANCIENT_ROYALSEAL : ancient_RoyalSeal(pc,useItem,client);break; 	//태고의옥쇄
				case L1ItemId.TIMECRACK_CORE : timeCrack_Core(pc,l1iteminstance1,useItem);break;// 균열의 핵
				//case 40076: ago_Scroll(pc,l1iteminstance1,useItem);break;// 고대의주문서
				case 40097 : case  40119 : case 140119 : case 140329 : 
					curseClear(pc,useItem,itemId);break;	// 해주스크롤, 원주민의 토템
				case 41036 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,41038,41047,10);break; // 풀
				case 40964 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,41011,41018,8);break; //흑마법가루
				case 40925 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,40987,40989,186);break; //정화의물약
				case 40926 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41173,41175,12);break;//신비한물약1단계
				case 40927 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41176,41178,12);break;//신비한물약2단계
				case 40928 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41179,41181,12);break;//신비한물약3단계
				case 40929 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41182,41184,12);break;//신비한물약4단계
				case 41029 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,41030,41034,1);break;  // 소환구 조각
				case 40859 : case 40860 : case 40861 : case 40862 : case 40864 : case 40865 : case 40866 : 
				case 40867 : case 40868 : case 40869 : case 40870 : case 40871 : case 40872 : case 40873 : 
				case 40874 : case 40875 : case 40876 : case 40877 : case 40878 : case 40879 : case 40880 : 
				case 40881 : case 40882 : case 40883 : case 40884 : case 40885 : case 40886 : case 40887 :
				case 40888 : case 40889 : case 40890 : case 40891 : case 40892 : case 40893 : case 40894 :
				case 40895 : case 40896 : case 40897 : case 40898 :
					magicScroll(pc, useItem , client, itemId, spellsc_objid, spellsc_x, spellsc_y );break; //마법주문서 
				case 41921: case 41922: case 41923: case 41924: case 41925: case 41926: case 41927: case 41928:
				case 41929: case 41930: case 41931: case 41932: case 41933: case 41934: case 41935: case 41936:
				case 41937: case 41938: case 41939: case 41940: case 41941: case 41942: case 41943: case 41944:
				case 41945: case 41946: case 41947: case 41948: case 41949: case 41950: case 41951: case 41952:
				case 41953: case 41954: case 41955: case 41956: case 41957: case 41958: case 41959: case 41960:
					magicScroll2(pc, useItem , client, itemId, spellsc_objid, spellsc_x, spellsc_y );break; //7~10단계마법주문서 
				case 5000219: // 기사 전용 카운터 배리어 주문서
					if (pc.isKnight()) {
						int[] allBuffSkill = { 91 };
						L1SkillUse l1skilluse5000219 = new L1SkillUse();
						for (int i = 0; i < allBuffSkill.length; i++) {
							l1skilluse5000219.handleCommands(pc,allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_SPELLSC);
						}
						pc.getInventory().removeItem(useItem, 1);
					} else {
						pc.sendPackets(new S_ServerMessage(264)); // 당신의 클래스는 이 아이템을 사용할 수 없습니다.
					}
					break;
				case 5000212 : { int[] buffSkill  = {1025}; // 코마 버프 주문서
				allBuffPotion(pc,useItem,buffSkill);break; }
				case 3500043 : { int[] buffSkill  = {26, 42, 54, 48, 79, 88,148, 151, 158};
				allBuffPotion(pc,useItem,buffSkill);break; } // 일반 버프 물약
				case 3500044 : { int[] buffSkill  = {26, 42, 43, 48, 54, 79, 88, 168, 160, 206, 211, 216, 117, 166};
				allBuffPotion(pc,useItem,buffSkill);break; } // 일반 버프 물약
				case 437031: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_A}; // 깃털버프 대박
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 437032: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_B}; // 깃털버프 중박
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 437033: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_C}; // 깃털버프 소박
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 437034: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_D}; // 깃털버프 쪽박
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 40314 : case 40316 :  pet_Necklace(pc, itemObjid);break;	 // 펫의 아뮤렛트
				case 40315 : pet_Whistle(pc);break;	 // 펫 호루라기
				case 40493 : magicFlute(pc, useItem);break;	 // 매직 플룻
				case 40325 : magicDice(pc , 2);break;	//2단계주사위
				case 40326 : magicDice(pc , 3);break;	//3단계주사위
				case 40327 : magicDice(pc , 4);break;	//4단계주사위
				case 40328 : magicDice(pc , 6);break;	//6단계주사위
				case L1ItemId.CHARACTER_REPAIR_SCROLL : chatacter_Repair_Scroll(pc,useItem ,client);break;	//케릭터복구주문서
				case 40903 : case 40904 : case 40905 : case 40906 : case 40907 : case 40908 : weddingRing( pc, useItem);break;// 각종 약혼 반지
				case 40555 : secretRoom_Key(pc);break; // 비밀의 방의 키
				case 40417 : spirit_Crystal(pc);break;// 정령의결정
				case 40566 : misterioso_Shell(pc);break; // 신비적인 쉘
				case 40557 : hit_List( pc, 32620, 32641 , 45883);break; //살생부(다엘퀘스트아이템)
				case 40563 : hit_List( pc, 32730, 32426 , 45884);break; //살생부(다엘퀘스트아이템)
				case 40561 : hit_List( pc, 33046, 32806 , 45885);break; //살생부(다엘퀘스트아이템) 
				case 40560 : hit_List( pc, 32580, 33260 , 45886);break; //살생부(다엘퀘스트아이템)
				case 40562 : hit_List( pc, 33447, 33476 , 45887);break; //살생부(다엘퀘스트아이템)
				case 40559 : hit_List( pc, 34215, 33195 , 45888);break; //살생부(다엘퀘스트아이템)
				case 40558 : hit_List( pc, 33513, 32890 , 45889);break; //살생부(다엘퀘스트아이템)
				case 40572 : assassinToken(pc);break; //어쌔신의증표
				case 40009 : expulsionStick(pc, useItem , spellsc_objid, spellsc_x, spellsc_y );break;	// 추방막대
				case L1ItemId.ICECAVE_KEY :	iceCaveKey(pc,useItem ,spellsc_objid);break; //얼음성의열쇠
				case 40289 : case 40290 : case 40291 : case 40292 : case 40293 : case 40294 : case 40295 : 
				case 40296 : case 40297 : case 5000200 : case 5000201 : case 5000202 : case 5000203 : case 5000204 : 
				case 5000205 : case 5000206 : case 5000207 : case 5000208 : case 5000209 : case 5000210 : 
					useToiTeleportAmulet(pc, itemId, useItem); 	break;//오만의탑부적사용 
				case 5000101 :case 5000102 :case 5000103 :case 5000104 :case 5000105 :
				case 5000106 :case 5000107 :case 5000108 :case 5000109 :  
					arroganceTowerAmulet_change(pc, useItem,(itemId+20),(itemId-4959812) , 2); break; //변이된 오만의탑 11~91층부적
				case 5000111 :case 5000112 :case 5000113 :case 5000114 :case 5000115 :
				case 5000116 :case 5000117 :case 5000118 :case 5000119 :	
					arroganceTowerAmulet_change(pc, useItem,(itemId+91), (itemId-4959822) , 5); break;//혼돈의 오만의탑  이동 부적 (11~91)
				case 40280 : case 40281 : case 40282 : case 40283 : case 40284 : case 40285 : case 40286 : case 40287 : case 40288 : 
					sealArroganceTowerAmulet(pc,useItem,itemId);break; //봉인된오만의탑부적 
				case 40070 : 
					pc.sendPackets(new S_ServerMessage(76, useItem.getLogName()));
					pc.getInventory().removeItem(useItem, 1); 
					break; //진화의열매
				case 41301 : shinefish( pc,useItem, 40053, 40049, 40045, 40019)	;break;	// 샤이닝렛드핏슈
				case 41302 : shinefish( pc,useItem, 40055, 40051, 40047, 40018)	;break;	// 샤이닝그린핏슈
				case 41303 : shinefish( pc,useItem, 40054, 40050, 40046, 40015)	;break;	// 샤이닝브르핏슈
				case 41304 : shinefish( pc,useItem, 40052, 40048, 40044, 40021)	;break;	// 샤이닝화이트핏슈
				case 40615 : case 40616 : case 40782 : case 40783 : 
					shadowTempleKey(pc,useItem);break; // 그림자의 신전2층과3층열쇠 
				case 437011 : case 1437011 : case 435000 : 진주포션사용(pc,itemId);break;  // 드진주, 시원 통쾌한 물약
				case 40692 : finishedTreasureMap(pc,useItem);break; // 완성된 보물의 지도
				case 41146 	: pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei001"));break; //드로몬드의초대장
				case 560025 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook0"));break;	
				case 560027 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook2"));break;
				case 560026 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook3"));break;
				case 560028 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook4"));break;
				case 560029 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook5"));break;
				case 41209 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei002"));break;// 포피레아의 의뢰서
				case 41210 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei003"));break;// 연마재
				case 41211 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei004"));break;// 허브
				case 41212 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei005"));break;// 특제 캔디
				case 41213 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei006"));break;// 티미의 바스켓
				case 41214 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei0012"));break;// 운의 증거
				case 41215 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei0010"));break;//지의 증거
				case 41216 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei0011"));break;//력의 증거
				case 41222 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei008"));break;//마슈르
				case 41223 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei007"));break;//무기의 파편
				case 41224 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei009"));break;//배지 파편
				case 41225 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei013"));break;//케스킨의 발주서
				case 41226 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei014"));break;//파고의 약
				case 41227 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei033"));break;//알렉스의 소개장
				case 41228 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei034"));break;//율법박사의 부적
				case 41229 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei025"));break;//스켈리턴의 머리
				case 41230 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei020"));break;//지난에의 편지
				case 41231 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei021"));break;//맛티에의 편지
				case 41233 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei019"));break;//케이이에의 편지
				case 41234 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei023"));break;// 뼈가 들어온 봉투
				case 41235 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei024"));break;// 재료표
				case 41236 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei026"));break;//본아챠의 뼈
				case 41237 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei027"));break;//스켈리턴 스파이크의 뼈
				case 41239 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei018"));break;//브트에의 편지
				case 41240 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei022"));break;//페다에의 편지
				case 41060 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "nonames"));break;//노나메의 추천서
				case 41061 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "kames"));break;//조사단의 증서：에르프 지역 두다마라카메
				case 41062 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "bakumos"));break;// 조사단의 증서：인간 지역 네르가바크모
				case 41063 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "bukas"));break;// 조사단의 증서：정령 지역 두다마라브카
				case 41064 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "huwoomos"));break;// 조사단의 증서：오크 지역 네르가후우모
				case 41065 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "noas"));break;// 조사단의 증서：조사단장 아트바노아
				case 41356 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "rparum3"));break;// 파룸의 자원 리스트
				case 40701 : smallTreasureMap(pc);break;//작은보물지도
				case 40663 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "sonsletter"));break;//아들의 편지
				case 40630 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "diegodiary"));break;//디에고의 낡은 일기
				case 41340 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "tion"));break;//  용병단장 티온
				case 41317 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "rarson"));break;//  랄슨의 추천장
				case 41318 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "kuen"));break;//쿠엔의 메모
				case 41329 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "anirequest"));break;//박제의 제작 의뢰서
				case 41346 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "robinscroll"));break;// 로빈훗드의 메모 1
				case 41347 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "robinscrol2"));break;// 로빈훗드의 메모 2
				case 41348 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "robinhood"));break;//로빈훗드의 소개장
				case 41007 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "erisscroll"));break;//이리스의 명령서：영혼의 안식
				case 41009 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "erisscroll2"));break;//이리스의 명령서：동맹의 의지
				case 41019 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory1"));break;//라스타바드의역사서1권
				case 41020 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory2"));break;//라스타바드의역사서2권
				case 41021 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory3"));break;//라스타바드의역사서3권
				case 41022 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory4"));break;//라스타바드의역사서4권
				case 41023 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory5"));break;//라스타바드의역사서5권
				case 41024 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory6"));break;//라스타바드의역사서6권
				case 41025 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory7"));break;//라스타바드의역사서7권
				case 41026 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory8"));break;//라스타바드의역사서8권
				case 210087 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "first_p"));break;//프로켈의 첫 번째 지령서
				case 210093 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "silrein1lt"));break;//실레인의 첫 번째 편지
				case L1ItemId.TIKAL_CALENDAR : tcalendar_Open_And_Close(pc);break;//티칼달력(오픈돼잇는지확인)
				case 41208 : overSpirit(pc ,useItem);break;//져가는 영혼		
				case 40700 : silverPlute(pc);break;//실버플룻						
				case 41121 : cahellContract(pc);break;//카헬의계약서
				case 41130 : bloodstainContract(pc);break;//혈흔의계약서
				case 42501 : StormWalk(pc,spellsc_x,spellsc_y );break;//스톰워크
				case L1ItemId.CHANGING_PETNAME_SCROLL:changing_Petname_scroll(pc,useItem,l1iteminstance1);break; //펫이름변경주문서 
				case 41260 : firewood(pc,useItem);break;//장작
				case 41345 : L1DamagePoison.doInfection(pc, pc, 3000, 5);pc.getInventory().removeItem(useItem, 1);break;//산성의유액
				case 41315 :holyWater(pc,useItem,1141 ,STATUS_HOLY_WATER, STATUS_HOLY_WATER_OF_EVA,STATUS_HOLY_MITHRIL_POWDER);break;  // 성수 
				case 41316 :holyWater(pc,useItem,1142 ,STATUS_HOLY_MITHRIL_POWDER,STATUS_HOLY_WATER_OF_EVA,STATUS_HOLY_WATER );break;// 신성한 미스리르파우다
				case 41354 :holyWater(pc,useItem);break;// 신성한 에바의 물
				case L1ItemId.CHANGING_SEX_POTION : changing_Sex_potion(pc,useItem);break;
				case L1ItemId.DRAGON_EMERALD_BOX : EmeraldBox(pc,useItem,itemId,"$11518" );break;//드래곤다이몬드상자 
				case L1ItemId.DRAGON_JEWEL_BOX : dragonBox(pc,useItem,itemId,"$7969" );break;//드래곤 에메랄드상자 
				case 437010 : dragonJewel(pc,useItem,1000000);break; //드래곤 다이아몬드 
				case 437013 : dragonJewel(pc,useItem,1500000);break; //드래곤 사파이어
				case 437012 : dragonJewel(pc,useItem,1700000);break; //드래곤 루비  
				case 437036 : dragonEmerald(pc,useItem);break;//드래곤 에메랄드
				case 5000151 : 
					UserCommands.tell(pc);
					pc.getInventory().removeItem(useItem, 1);
					break;	
				case 100903: case 100904: pc.sendPackets(new S_UserCommands2(1)); break;
				case 5000121 :	birthdayCake(pc); break;	//생일축하케이크
				case L1ItemId.METIS_ONE : metisCakeBox(pc,useItem); break;	//메티스의 첫번째선물
				case 5000137: BossSpawnWand(pc,useItem); break; // 보스 소환
				case L1ItemId.DRAGON_KEY : dragon_Key(pc,useItem); break;//용키
				case L1ItemId.FORTUNE_COOKIE : fortune_Cookie(pc,useItem);break;
				case 430523:
					if (pc.getInventory(). checkItem(430523, 1) 
							&& pc.getInventory(). checkItem(430524, 1) 
							&& pc.getInventory(). checkItem(430525, 1) 
							&& pc.getInventory(). checkItem(430526, 1) 
							&& pc.getInventory(). checkItem(430527, 1) 
							&& pc.getInventory(). checkItem(430528, 1) 
							&& pc.getInventory(). checkItem(430529, 1)  ){ //수량체크부분
						pc.getInventory().consumeItem(430523, 1); 
						pc.getInventory().consumeItem(430524, 1);
						pc.getInventory().consumeItem(430525, 1);
						pc.getInventory().consumeItem(430525, 1); 
						pc.getInventory().consumeItem(430526, 1);
						pc.getInventory().consumeItem(430527, 1);
						pc.getInventory().consumeItem(430528, 1); 
						pc.getInventory().consumeItem(430529, 1); 
						pc.getInventory().storeItem(430530, 1);
						pc.sendPackets(new S_SkillSound(pc.getId() , 2568));
						//Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 2568)); // 클릭시 임팩부분 
						pc.sendPackets(new S_SkillSound(pc.getId(), 1249));   // 추가했습니다
						//Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 1249));    // 추가했습니다
						pc.sendPackets(new S_SystemMessage("7개의 조각이 하나로 합쳐져 보물상자가 완성되었습니다.")); //성공시 멘트전송
					} else {
						pc.sendPackets(new S_SystemMessage("보물상자 조각 7개가 다 모으신 후 1번을 더블 클릭해주십시요.")); //조건이 맞지 않을경우 메세지 전송
					}
					break;
				case 430531: // 랜덤 인챈트 무기 상자
					pc.getInventory().consumeItem(430531, 1);// 삭제를 해준다.
					Random random = new Random();
					int[] itemrnd = { 9, 49, 54, 57, 58, 62, 81, 84, 85, 119, 121, 123, 124, 162, 164, 177, 188, 189, 190, 205, 410001, 410003, 412000, 412001
							, 415010, 415011, 415012, 415013, 450031, 450032, 450033};
					int[] enchantrnd = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 
							7, 7, 7, 7, 7, 7, 7, 7, 7, 7}; //-10~+25 사이 랜덤하게 준다.
					int ran1 = random.nextInt(itemrnd.length);
					int ran2 = random.nextInt(enchantrnd.length);
					createNewItem2(pc, itemrnd[ran1], 1, enchantrnd[ran2]);
					break;
				case 430532: // 랜덤 인챈트 방어구 상자
					pc.getInventory().consumeItem(430532, 1);// 삭제를 해준다.
					Random random430532 = new Random();
					int[] itemrnd430532 = { 20017, 20018, 20025, 20042, 20049, 20050, 20057, 20074, 20079, 20108, 20119, 20130, 20153, 20178, 20187, 20186,
							20200, 20204, 20216, 20218, 20235, 120074, 500010, 500011, 500012, 500013, 500014, 500015 };
					int[] enchantrnd430532 = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 
							5, 5, 5, 5, 5, 5, 5, 5}; //-10~+25 사이 랜덤하게 준다.
					int ran11 = random430532.nextInt(itemrnd430532.length);
					int ran22 = random430532.nextInt(enchantrnd430532.length);
					createNewItem2(pc, itemrnd430532[ran11], 1, enchantrnd430532[ran22]);
					break;
				case 3500057:
					if (pc.getInventory(). checkItem(41246, 20000) && pc.getInventory(). checkItem(3500057, 1)){ //수량체크부분
						pc.getInventory().consumeItem(41246, 20000); 
						pc.getInventory().consumeItem(3500057, 1);
						pc.getInventory().storeItem(3500069, 1); //무기함 지급
						pc.sendPackets(new S_SystemMessage("숨겨진 마족의 무기함이 지급되었습니다.")); //성공시 멘트전송
					} else { //그렇지않고 모든 조건이 안맞는다면
						pc.sendPackets(new S_SystemMessage("하딘의일기장,결정체20000개 가 부족합니다.")); //조건이 맞지 않을경우 메세지 전송
					}
					break;
				case 3500058: case 3500059: case 3500060: case 3500061: case 3500062: case 3500063: case 3500064: case 3500065: case 3500066: case 3500067:
					if (pc.getInventory(). checkItem(3500058, 1) && pc.getInventory(). checkItem(3500059, 1) && pc.getInventory(). checkItem(3500060, 1) && pc.getInventory(). checkItem(3500061, 1) && pc.getInventory(). checkItem(3500062, 1) && pc.getInventory(). checkItem(3500063, 1) && pc.getInventory(). checkItem(3500064, 1) && pc.getInventory(). checkItem(3500065, 1) && pc.getInventory(). checkItem(3500066, 1) && pc.getInventory(). checkItem(3500067, 1)){ //수량체크부분
						pc.getInventory().consumeItem(3500058, 1); //
						pc.getInventory().consumeItem(3500059, 1);//
						pc.getInventory().consumeItem(3500060, 1); //
						pc.getInventory().consumeItem(3500061, 1);//
						pc.getInventory().consumeItem(3500062, 1); //
						pc.getInventory().consumeItem(3500063, 1);//.
						pc.getInventory().consumeItem(3500064, 1); //
						pc.getInventory().consumeItem(3500065, 1);//
						pc.getInventory().consumeItem(3500066, 1); //
						pc.getInventory().consumeItem(3500067, 1);//
						pc.getInventory().storeItem(3500057, 1); //일기장 지급
						pc.sendPackets(new S_SystemMessage("어두운 하딘의 일기장이 완성되었습니다.")); //성공시 멘트전송
					} else { //그렇지않고 모든 조건이 안맞는다면
						pc.sendPackets(new S_SystemMessage("하딘의일기(총10가지)가 부족합니다.")); //조건이 맞지 않을경우 메세지 전송
					}
					break;
				case 3500068:
					if (pc.getInventory(). checkItem(3500058, 1) && pc.getInventory(). checkItem(3500059, 1) && pc.getInventory(). checkItem(3500060, 1) && pc.getInventory(). checkItem(3500061, 1) && pc.getInventory(). checkItem(3500062, 1) && pc.getInventory(). checkItem(3500063, 1) && pc.getInventory(). checkItem(3500064, 1) && pc.getInventory(). checkItem(3500065, 1) && pc.getInventory(). checkItem(3500066, 1) && pc.getInventory(). checkItem(3500067, 1)){ //수량체크부분
						pc.getInventory().consumeItem(3500058, 1); //
						pc.getInventory().consumeItem(3500059, 1);//
						pc.getInventory().consumeItem(3500060, 1); //
						pc.getInventory().consumeItem(3500061, 1);//
						pc.getInventory().consumeItem(3500062, 1); //
						pc.getInventory().consumeItem(3500063, 1);//.
						pc.getInventory().consumeItem(3500064, 1); //
						pc.getInventory().consumeItem(3500065, 1);//
						pc.getInventory().consumeItem(3500066, 1); //
						pc.getInventory().consumeItem(3500067, 1);//
						pc.getInventory().storeItem(3500057, 1); //일기장 지급
						pc.sendPackets(new S_SystemMessage("어두운 하딘의 일기장이 완성되었습니다.")); //성공시 멘트전송
					} else { //그렇지않고 모든 조건이 안맞는다면
						pc.sendPackets(new S_SystemMessage("하딘의일기(총10가지)가 부족합니다.")); //조건이 맞지 않을경우 메세지 전송
					}
					break;
				case 4500153:
					if (pc.getInventory(). checkItem(4500152, 200) && pc.getInventory(). checkItem(555555, 1)){ //수량체크부분
						pc.getInventory().consumeItem(4500152, 200); //
						pc.getInventory().consumeItem(555555, 1);//
						pc.getInventory().consumeItem(4500153, 1);//
						pc.getInventory().storeItem(888888, 1); //
						pc.sendPackets(new S_SystemMessage("귀걸이(2단) 업그레이드 의 성공하였습니다.")); //성공시 멘트전송
					} else { //그렇지않고 모든 조건이 안맞는다면
						pc.sendPackets(new S_SystemMessage("귀걸이 파편(200개),귀걸이(1단)이 부족합니다.")); //조건이 맞지 않을경우 메세지 전송
					}
					break;
				case 4500154:
					if (pc.getInventory(). checkItem(4500152, 700) && pc.getInventory(). checkItem(888888, 1)){ //수량체크부분
						pc.getInventory().consumeItem(4500152, 700); //
						pc.getInventory().consumeItem(888888, 1);//
						pc.getInventory().consumeItem(4500154, 1);//
						pc.getInventory().storeItem(999999, 1); //
						pc.sendPackets(new S_SystemMessage("귀걸이(3단) 업그레이드 의 성공하였습니다.")); //성공시 멘트전송
					} else { //그렇지않고 모든 조건이 안맞는다면
						pc.sendPackets(new S_SystemMessage("귀걸이 파편(700개),귀걸이(2단)이 부족합니다.")); //조건이 맞지 않을경우 메세지 전송
					}
					break;
				case 447012:
					SimpleDateFormat formatter = new SimpleDateFormat ( "yy/MM/dd", Locale.KOREA );
					Date currentTime = new Date ( );
					String dTime = formatter.format ( currentTime );
					if(!l1iteminstance1.getItem().isTradable()){
						pc.sendPackets(new S_SystemMessage("교환불가능 아이템은 위탁 할수 없습니다."));
						return; 
					}
					if(l1iteminstance1.isEquipped()){
						pc.sendPackets(new S_SystemMessage("착용중인 아이템은 위탁 할수 없습니다."));
						return;
					}
					if (l1iteminstance1.getBless() > 3) {
						pc.sendPackets(new S_SystemMessage("봉인된 아이템은 위탁 할 수 없습니다."));
						return;
					}
					if (l1iteminstance1.getItem().getType2() == 1 || l1iteminstance1.getItem().getType2() == 2) {
						int count = pc.getInventory().countItems(447012); // 위탁판매 증서 아이템번호는 본인 팩에 맞게
						pc.getInventory().removeItem(useItem, count);
						pc.getInventory().removeItem(l1iteminstance1, 1);
						AuctionBTable.getInstance().writeTopic(pc, dTime ,l1iteminstance1, count);
					} else {
						pc.sendPackets(new S_SystemMessage("무기 방어구만 위탁가능합니다."));
					}
					break;
				default : unfitforuseItem(pc,useItem);break;
				}
			}
			L1ItemDelay.onItemUse(pc, useItem); // 아이템 지연 개시
		}
	}

	//삭제하는부분 
	private void removeItem(L1PcInstance pc, L1ItemInstance useItem){
		pc.getInventory().removeItem(useItem, 1);
	}

	private void item_change(L1PcInstance pc, L1ItemInstance useItem, int ChangeItemId ){
		pc.getInventory().removeItem(useItem, 1);
		pc.getInventory().storeItem(ChangeItemId, 5);
	}

	private void item_Create(L1PcInstance pc, L1ItemInstance useItem, int checkItemId , int deleteItemId, int storeItemId){
		if (pc.getInventory().checkItem(checkItemId, 1)) {
			pc.getInventory().consumeItem(checkItemId, 1);
			pc.getInventory().consumeItem(deleteItemId, 1);
			L1ItemInstance item = pc.getInventory().storeItem(storeItemId, 1);
			pc.sendPackets(new S_ServerMessage(403, item.getLogName())); //성공시 멘트전송 %O%o 얻었습니다.
		} else {
			pc.sendPackets(new S_ServerMessage(1110)); // 수량이 부족합니다.
		}
	}
	private void CreateItemNoDelete(L1PcInstance pc, int checkItem, int checkItemCount, int storeItem, int storeItemCount){
		if (pc.getInventory().checkItem(checkItem, checkItemCount)){  // 
			pc.getInventory().consumeItem(checkItem, checkItemCount); //
			L1ItemInstance item = pc.getInventory().storeItem(storeItem, storeItemCount); // 
			pc.sendPackets(new S_ServerMessage(403, item.getLogName())); //성공시 멘트전송 %O%o 얻었습니다.
		} else {
			pc.sendPackets(new S_ServerMessage(1110)); // 수량이 부족합니다.
		}
	}
	private String getItemName(L1PcInstance pc, int itemId){
		String getName = pc.getInventory().getItem(itemId).getName();
		return getName;
	}
	//드래곤의키 
	private void dragon_Key(L1PcInstance pc,L1ItemInstance useItem){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (castle_id != 0) { // 공성존에서 사용 불가
			pc.sendPackets(new S_ServerMessage(1892)); // 이 곳에서 드래곤 키를 사용할 수 없습니다.
			return;
		}
		if (CharPosUtil.getZoneType(pc) == 0) {
			pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONMENU, useItem));                       
		} else {
			pc.sendPackets(new S_ServerMessage(1892)); // 이 곳에서 드래곤 키를 사용할 수 없습니다.
		}  
	}

	//보스 소환 막대
	private void BossSpawnWand(L1PcInstance pc,L1ItemInstance useItem){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (castle_id != 0) { // 공성존에서 사용 불가
			pc.sendPackets(new S_ServerMessage(353)); // 이 근처에서는 몬스터를 소환할 수 없습니다.
			return;
		}
		if (CharPosUtil.getZoneType(pc) == 0) {
			useMobEventSpownWand(pc, useItem);
			pc.getInventory().removeItem(useItem, 1);
		} else {
			pc.sendPackets(new S_ServerMessage(353)); // 이 근처에서는 몬스터를 소환할 수 없습니다.
		} //여기까지 추가
	}
	private void pet_Necklace(L1PcInstance pc,int itemObjid){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);////////////공성전 지역에서 마법 스킬 제한
		if (castle_id != 0){
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
			return;
		} else {
			if (pc.getInventory().checkItem(41160)) { // 소환의 피리
				if (withdrawPet(pc, itemObjid)) {
					pc.getInventory().consumeItem(41160, 1);
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
			}
		}
	}

	private void pet_Whistle(L1PcInstance pc){
		pc.sendPackets(new S_Sound(437));
		Broadcaster.broadcastPacket(pc, new S_Sound(437));
		Object[] petList = pc.getPetList().values().toArray();
		for (Object petObject : petList) {
			if (petObject instanceof L1PetInstance) { // 펫
				L1PetInstance pet = (L1PetInstance) petObject;
				pet.call();
			}
		}
	}

	private void magicFlute(L1PcInstance pc,L1ItemInstance useItem){
		pc.sendPackets(new S_Sound(165));
		Broadcaster.broadcastPacket(pc, new S_Sound(165));
		L1GuardianInstance guardian = null;
		for (L1Object visible : pc.getNearObjects().getKnownObjects()) {
			if (visible instanceof L1GuardianInstance) {
				guardian = (L1GuardianInstance) visible;
				if (guardian.getNpcTemplate().get_npcId() == 70850) { // 빵
					if (createNewItem(pc, 88, 1)) {
						pc.getInventory().removeItem(useItem, 1);
					}
				}
			}
		}
	}

	private void magicDice(L1PcInstance pc ,int random){
		if (pc.getInventory().checkItem(40318, 1)) {
			int gfxid = 3237 + CommonUtil.random(random);
			pc.sendPackets(new S_SkillSound(pc.getId(), gfxid));
			Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), gfxid));
			pc.getInventory().consumeItem(40318, 1);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}

	private void chatacter_Repair_Scroll(L1PcInstance pc,L1ItemInstance useItem ,LineageClient client)throws Exception{
		Connection connection = null;
		connection = L1DatabaseFactory.getInstance().getConnection();
		PreparedStatement preparedstatement = connection.prepareStatement("UPDATE characters SET LocX=33087, LocY=33399, MapID=4 WHERE account_name=?");
		preparedstatement.setString(1, client.getAccountName());
		preparedstatement.execute();
		preparedstatement.close();
		connection.close();
		pc.getInventory().removeItem(useItem, 1);
		pc.sendPackets(new S_SystemMessage("모든 케릭터의 좌표가 정상적으로 복구 되었습니다."));
	}

	private void weddingRing(L1PcInstance pc,L1ItemInstance useItem){
		L1PcInstance partner = null;
		boolean partner_stat = false;
		if (pc.getPartnerId() != 0) { // 결혼중
			partner = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getPartnerId());
			if (partner != null && partner.getPartnerId() != 0&& pc.getPartnerId() == partner.getId()&& partner.getPartnerId() == pc.getId()) {
				partner_stat = true;
			}
		} else {
			pc.sendPackets(new S_ServerMessage(662)); // \f1당신은결혼하지않았습니다.
			return;
		}

		if (useItem.getChargeCount() <= 0) {
			return;
		}
		if (pc.getMapId() == 666) {
			return;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_DELAY)){
			int DelayTime = pc.getSkillEffectTimerSet().getSkillEffectTimeSec(L1SkillId.STATUS_DELAY);
			pc.sendPackets(new S_SystemMessage(DelayTime + "초 후에 사용하실 수 있습니다."));
			return;
		}
		if (partner_stat) {
			boolean castle_area = L1CastleLocation.checkInAllWarArea(partner.getX(), partner.getY(), partner.getMapId());

			if ((partner.getMapId() == 0 || partner.getMapId() == 4 || partner.getMapId() == 53 || partner.getMapId() == 54 
					|| partner.getMapId() == 55 || partner.getMapId() == 56 || partner.getMapId() == 304) && castle_area == false) {
				useItem.setChargeCount(useItem.getChargeCount() - 1);
				pc.getInventory().updateItem(useItem,L1PcInventory.COL_CHARGE_COUNT);
				L1Teleport.teleport(pc, partner.getX(), partner.getY(), partner.getMapId(), 5, true);
				pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_DELAY, 300 * 1000); // 딜레이적용
			} else {
				pc.sendPackets(new S_ServerMessage(547)); // \f1당신의
				// 파트너는
				// 지금
				// 당신이 갈
				// 수 없는
				// 곳에서
				// 플레이중입니다.
			}
		} else {
			pc.sendPackets(new S_ServerMessage(546)); // \f1당신의
			// 파트너는 지금
			// 플레이를 하고
			// 있지 않습니다.
		}
	}

	private void secretRoom_Key(L1PcInstance pc){
		//오림방
		if (pc.isKnight()
				&& (pc.getX() >= 32806 && pc.getX() <= 32814)
				&& (pc.getY() >= 32798 && pc.getY() <= 32807)
				&& pc.getMapId() == 13) {						
			L1Teleport.teleport(pc, 32815, 32810, (short) 13, 5, false);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}
	private void spirit_Crystal(L1PcInstance pc){
		if(pc.getMap().isEscapable()) { // 귀환가능지역인가를 검색한다			
			L1Teleport.teleport(pc, 32922, 32812, (short) 430, 5, true);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}

	private void misterioso_Shell(L1PcInstance pc){
		// 상아의 탑의 마을의 남쪽에 있는 매직 스퀘어의 좌표
		if (pc.isElf()
				&& (pc.getX() >= 33971 && pc.getX() <= 33975)
				&& (pc.getY() >= 32324 && pc.getY() <= 32328)
				&& pc.getMapId() == 4
				&& !pc.getInventory().checkItem(40548)) { // 망령의 봉투
			boolean found = false;
			L1MonsterInstance mob = null;
			for (L1Object obj : L1World.getInstance().getVisibleObjects(4).values()) {
				if (obj instanceof L1MonsterInstance) {
					mob = (L1MonsterInstance) obj;
					if (mob != null) {
						if (mob.getNpcTemplate().get_npcId() == 45300) {
							found = true;
							break;
						}
					}
				}
			}
			if (found) {
				pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
			} else {
				L1SpawnUtil.spawn(pc, 45300, 0, 0, false); // 고대인의 망령
			}
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}

	private void assassinToken(L1PcInstance pc){
		if (pc.getX() == 32778 && pc.getY() == 32738 && pc.getMapId() == 21) {
			L1Teleport.teleport(pc, 32781, 32728, (short)21, 5,	true);
		} else if (pc.getX() == 32781 && pc.getY() == 32728	&& pc.getMapId() == 21) {
			L1Teleport.teleport(pc, 32778, 32738, (short)21, 5, true);
		} else {
			pc.sendPackets(new S_ServerMessage(79));
		}	
	}

	private void expulsionStick(L1PcInstance pc,L1ItemInstance useItem ,int spellsc_objid,int spellsc_x, int spellsc_y ){
		int chargeCount = useItem.getChargeCount();
		if (chargeCount <= 0) {
			pc.sendPackets(new S_ServerMessage(79));// \f1 아무것도 일어나지 않았습니다.
			return;
		}

		L1Object target = L1World.getInstance().findObject(spellsc_objid);
		if (target != null) {
			int heding = CharPosUtil.targetDirection(pc, spellsc_x, spellsc_y);
			pc.getMoveState().setHeading(heding);
			pc.sendPackets(new S_AttackPacket(pc, 0, ActionCodes.ACTION_Wand));
			Broadcaster.broadcastPacket(pc, new S_AttackPacket(pc, 0, ActionCodes.ACTION_Wand));

			if(target instanceof L1PcInstance){
				L1PcInstance cha = (L1PcInstance) target;
				if (cha.getLevel() <= 60){
					if (!L1CastleLocation.checkInAllWarArea(cha.getX(), cha.getY(), cha.getMapId()) && 
							(CharPosUtil.getZoneType(cha) == 0 || CharPosUtil.getZoneType(cha) == -1)){
						L1Teleport.teleport(cha, pc.getLocation(), pc.getMoveState().getHeading(), false);
					}
				}
				if (cha.getSkillEffectTimerSet().hasSkillEffect(ERASE_MAGIC)){
					cha.getSkillEffectTimerSet().removeSkillEffect(ERASE_MAGIC);
				}
			}
		}

		useItem.setChargeCount(useItem.getChargeCount() - 1);
		pc.getInventory().updateItem(useItem, L1PcInventory.COL_CHARGE_COUNT);

	}


	private void iceCaveKey(L1PcInstance pc,L1ItemInstance useItem ,int spellsc_objid){
		L1Object t = L1World.getInstance().findObject(spellsc_objid);
		L1DoorInstance door = (L1DoorInstance) t;
		if (pc.getLocation().getTileLineDistance(door.getLocation()) > 3) {
			return;
		}
		if (door.getDoorId() >= 5000 && door.getDoorId() <= 5009){
			if (door != null && door.getOpenStatus() == ActionCodes.ACTION_Close){
				door.open();
				pc.getInventory().removeItem(useItem, 1);
			}
		}	
	}
	private void hit_List(L1PcInstance pc,int map_X,int map_Y , int napId){
		if (pc.getX() == map_X && pc.getY() == map_Y && pc.getMapId() == 4) {
			L1NpcInstance object = null;
			for (L1Object obj : L1World.getInstance().getObject()) {
				if(obj instanceof L1NpcInstance){
					object = (L1NpcInstance)obj;
					if (object.getNpcTemplate().get_npcId() == napId) {
						pc.sendPackets(new S_ServerMessage(79));
						return;
					}
				}

			}
			L1SpawnUtil.spawn(pc, 45883, 0, 300000, false);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); 
		}
	}
	private void  sealArroganceTowerAmulet(L1PcInstance pc,L1ItemInstance useItem,int itemId){
		// 봉인된 오만의 탑 11~91층 부적
		pc.getInventory().removeItem(useItem, 1);
		L1ItemInstance item = pc.getInventory().storeItem(itemId + 9, 1);
		pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
	}

	private void arroganceTowerAmulet_change(L1PcInstance pc,L1ItemInstance useItem,int storeItem1, int storeItem2 ,int chanceNum){
		pc.getInventory().removeItem(useItem, 1);
		int chance = CommonUtil.random(10);
		if (chance >= 0 && chance < chanceNum) {
			L1ItemInstance item = pc.getInventory().storeItem(storeItem1, 1);
			if (item != null) {
				pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
			}
		} else if (chance >= chanceNum && chance < 9) {
			L1ItemInstance item = pc.getInventory().storeItem(storeItem2, 1);
			if (item != null) {
				pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
			}
		}
	}
	private void shinefish(L1PcInstance pc,L1ItemInstance useItem,int item1, int item2,int item3, int item4){ //빛나는 물고기들
		int chance = CommonUtil.random(15);
		switch(chance){
		case 0 : createNewItem(pc, item1, 1);break;
		case 1 : case 2:  createNewItem(pc, item2, 1);break;
		case 3 : case 4:case 5 :  createNewItem(pc, item3, 1);break;
		case 6 : case 7:case 8 :case 9 :  createNewItem(pc, item4, 1);break;
		case 10 : case 11: case 12 : case 13 : case 14 :  createNewItem(pc, 41296, 1);break;
		}
		pc.getInventory().removeItem(useItem, 1);
	}
	private void girandungeonTime(L1PcInstance pc, L1ItemInstance useItem)throws Exception{
		boolean isDelayEffect = false;
		int delayEffect = ((L1EtcItem) useItem.getItem()).get_delayEffect();
		if (delayEffect > 0) {
			isDelayEffect = true;
			Timestamp lastUsed = useItem.getLastUsed();
			if (lastUsed != null) {
				Calendar cal = Calendar.getInstance();
				long usedtime = (cal.getTimeInMillis() - lastUsed.getTime()) / 1000;
				if (usedtime <= delayEffect) {
					String used_time = Long.toString((delayEffect - usedtime)/60);
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 분 동안은 사용할 수 없습니다.
					return;
				}
			}
		}  
		int entertime = pc.getGdungeonTime() % 1000;
		if(entertime > 61){
			pc.setGdungeonTime(pc.getGdungeonTime() - 60);
			pc.getInventory().consumeItem(41159, 500); 
			pc.sendPackets(new S_SystemMessage("기란감옥 체류시간이 1시간 충전되었습니다."));
			pc.save();
		} else {
			pc.sendPackets(new S_SystemMessage("기란감옥 체류시간이 남아 사용 불가합니다."));
			isDelayEffect = false;
		} 
		if (isDelayEffect) {
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			useItem.setLastUsed(ts);
			pc.getInventory().updateItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
			pc.getInventory().saveItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
		}
	}
	private void girandungeonTime2(L1PcInstance pc, L1ItemInstance useItem)throws Exception{
		boolean isDelayEffect = false;
		int delayEffect = ((L1EtcItem) useItem.getItem()).get_delayEffect();
		if (delayEffect > 0) {
			isDelayEffect = true;
			Timestamp lastUsed = useItem.getLastUsed();
			if (lastUsed != null) {
				Calendar cal = Calendar.getInstance();
				long usedtime = (cal.getTimeInMillis() - lastUsed.getTime()) / 1000;
				if (usedtime <= delayEffect) {
					String used_time = Long.toString((delayEffect - usedtime)/60);
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 분 동안은 사용할 수 없습니다.
					return;
				}
			}
		}  
		int entertime = pc.getGdungeonTime() % 1000;
		if(entertime > 121){
			pc.setGdungeonTime(pc.getGdungeonTime() - 120);
			pc.getInventory().consumeItem(41159, 2000); 
			pc.sendPackets(new S_SystemMessage("기란감옥 체류시간이 2시간 충전되었습니다."));
			//pc.getSkillEffectTimerSet().setSkillEffect(ITEMUSEDELAY_GIRAN, 14400 * 1000); // 4시간 스킬을 4시간으로 주고 살수잇게 ㅋ
			pc.save();
		} else {
			pc.sendPackets(new S_SystemMessage("기란감옥 체류시간이 남아 사용 불가합니다."));
			isDelayEffect = false;
		} 
		if (isDelayEffect) {
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			useItem.setLastUsed(ts);
			pc.getInventory().updateItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
			pc.getInventory().saveItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
		}
	}

	private void ivoryTowerTime(L1PcInstance pc, L1ItemInstance useItem)throws Exception{
		boolean isDelayEffect = false;
		int delayEffect = ((L1EtcItem) useItem.getItem()).get_delayEffect();
		if (delayEffect > 0) {
			isDelayEffect = true;
			Timestamp lastUsed = useItem.getLastUsed();
			if (lastUsed != null) {
				Calendar cal = Calendar.getInstance();
				long usedtime = (cal.getTimeInMillis() - lastUsed.getTime()) / 1000;
				if (usedtime <= delayEffect) {
					String used_time = Long.toString((delayEffect - usedtime)/60);
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 분 동안은 사용할 수 없습니다.
					return;
				}
			}
		}  
		int entertime = pc.getIvoryTowerTime() % 1000;
		if(entertime > 50) {
			pc.setIvoryTowerTime(pc.getIvoryTowerTime() - 50);
			pc.getInventory().consumeItem(41159, 500);
			pc.sendPackets(new S_SystemMessage("" + pc.getName() + "\\fY님의 상아탑 시간이 충전되었습니다."));
			//pc.getSkillEffectTimerSet().setSkillEffect(ITEMUSEDELAY_IVORY, 14400 * 1000); // 4시간 스킬을 4시간으로 주고 살수잇게 ㅋ
			pc.save(); 
		} else {
			pc.sendPackets(new S_SystemMessage("\\fY상아탑 체류시간이 남아 사용불가능합니다."));
			isDelayEffect = false;
		}
		if (isDelayEffect) {
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			useItem.setLastUsed(ts);
			pc.getInventory().updateItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
			pc.getInventory().saveItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
		}
	}

	private void ldungeonTime(L1PcInstance pc, L1ItemInstance useItem)throws Exception{
		boolean isDelayEffect = false;
		int delayEffect = ((L1EtcItem) useItem.getItem()).get_delayEffect();
		if (delayEffect > 0) {
			isDelayEffect = true;
			Timestamp lastUsed = useItem.getLastUsed();
			if (lastUsed != null) {
				Calendar cal = Calendar.getInstance();
				long usedtime = (cal.getTimeInMillis() - lastUsed.getTime()) / 1000;
				if (usedtime <= delayEffect) {
					String used_time = Long.toString((delayEffect - usedtime)/60);
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 분 동안은 사용할 수 없습니다.
					return;
				}
			}
		}  
		int entertime = pc.getLdungeonTime() % 1000;
		if (entertime > 61) {
			pc.setLdungeonTime(pc.getLdungeonTime() - 60);
			pc.getInventory().consumeItem(41159, 500);
			pc.sendPackets(new S_SystemMessage("" + pc.getName() + "\\fY님의 라스타바드 시간이 충전되었습니다."));
			pc.save(); 
		} else {
			pc.sendPackets(new S_SystemMessage("\\fY라스타바드 체류시간이남아 사용불가능합니다."));
			isDelayEffect = false;
		}
		if (isDelayEffect) {
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			useItem.setLastUsed(ts);
			pc.getInventory().updateItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
			pc.getInventory().saveItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
		}
	}
	private void cerenisCallStick(L1PcInstance pc){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc); 
		if (pc.getMap().isSafetyZone(pc.getLocation())||castle_id != 0) {
			//	S_AttackPacket s_attackStatus = new S_AttackPacket(pc,0, ActionCodes.ACTION_Wand);
			//	pc.sendPackets(s_attackStatus);
			//	Broadcaster.broadcastPacket(pc, s_attackStatus);
			pc.sendPackets(new S_ServerMessage(563)); // 여기에서는 사용할 수 없습니다.
		} else {
			L1SpawnUtil.spawn(pc, 450001854, 0, 300000, false);
			pc.getInventory().consumeItem(5000161, 1);
		}
	}
	private void portableStorage(L1PcInstance pc,L1ItemInstance useItem){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc); 
		if (castle_id != 0){
			pc.sendPackets(new S_ServerMessage(563)); // 여기에서는 사용할 수 없습니다.
			return;
		}
		L1SpawnUtil.spawn(pc, 60009, 0, 60*1000, false);//사우람
		pc.sendPackets(new S_SystemMessage("창고를 소환했습니다. 5분후에사라집니다."));
		pc.getInventory().removeItem(useItem,1);
	}
	private void shadowTempleKey(L1PcInstance pc,L1ItemInstance useItem){
		if(pc.getMap().isEscapable()) { // 귀환가능지역인가를 검색한다
			L1Teleport.teleport(pc, ((L1EtcItem) useItem
					.getItem()).get_locx(),
					((L1EtcItem) useItem.getItem()).get_locy(),
					((L1EtcItem) useItem.getItem()).get_mapid(), 5, true);
		} else {
			// \f1 아무것도 일어나지 않았습니다.
			pc.sendPackets(new S_ServerMessage(79));
		}
	}
	private void smallTreasureMap(L1PcInstance pc){
		//} else if (itemId == 40701) { // 작은 보물의 지도
		int check = pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1);
		switch(check){
		case 1 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "firsttmap"));break;
		case 2 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "secondtmapa"));break;
		case 3 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "secondtmapb"));break;
		case 4 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "secondtmapc"));break;
		case 5 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "thirdtmapd"));break;
		case 6 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "thirdtmape"));break;
		case 7 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "thirdtmapf"));break;
		case 8 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "thirdtmapg"));break;
		case 9 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "thirdtmaph"));break;
		case 10 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "thirdtmapi"));break;
		}
	}

	private void tcalendar_Open_And_Close(L1PcInstance pc){
		if (CrockSystem.getInstance().isOpen())
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "tcalendaro"));
		else 
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "tcalendarc"));
	}

	private void overSpirit(L1PcInstance pc,L1ItemInstance useItem){
		if (((pc.getX() >= 32844 && pc.getX() <= 32845)|| (pc.getY() >= 32693 && pc.getY() <= 32694)) && pc.getMapId() == 550) { // 배의 묘지:지상층
			L1Teleport.teleport(pc, ((L1EtcItem) useItem.getItem()).get_locx(),
					((L1EtcItem) useItem.getItem()).get_locy(),
					((L1EtcItem) useItem.getItem()).get_mapid(), 5, true);
		} else {
			// \f1 아무것도 일어나지 않았습니다.
			pc.sendPackets(new S_ServerMessage(79));
		}
	}

	private void silverPlute(L1PcInstance pc){
		pc.sendPackets(new S_Sound(10));
		Broadcaster.broadcastPacket(pc, new S_Sound(10));
		if (((pc.getX() >= 32619 && pc.getX() <= 32623)
				|| (pc.getY() >= 33120 && pc.getY() <= 33124))
				&& pc.getMapId() == 440){ // 해적 시마마에반매직 스퀘어 좌표
			boolean found = false;
			L1MonsterInstance mon = null;
			for (L1Object obj : L1World.getInstance().getObject()) {
				if(obj instanceof L1MonsterInstance){
					mon = (L1MonsterInstance)obj;
					if (mon != null) {
						if (mon.getNpcTemplate().get_npcId() == 45875) {
							found = true;
							break;
						}
					}
				}

			}
			if (found) {
			} else {
				L1SpawnUtil.spawn(pc, 45875, 0, 0, false);
			}
		}
	}

	private void cahellContract(L1PcInstance pc){
		if (pc.getQuest().get_step(L1Quest.QUEST_SHADOWS) == L1Quest.QUEST_END
				|| pc.getInventory().checkItem(41122, 1)) {
			pc.sendPackets(new S_ServerMessage(79));				
		} else {
			createNewItem(pc, 41122, 1);
		}

	}

	private void bloodstainContract(L1PcInstance pc){
		if (pc.getQuest().get_step(L1Quest.QUEST_DESIRE) == L1Quest.QUEST_END
				|| pc.getInventory().checkItem(41131, 1)) {
			pc.sendPackets(new S_ServerMessage(79));
		} else {
			createNewItem(pc, 41131, 1);
		}
	}

	private void StormWalk(L1PcInstance pc,int spellsc_x, int spellsc_y ){
		if (pc.getCurrentMp() < 10) {
			pc.sendPackets(new S_ServerMessage(278)); // \f1MP가 부족해 마법을 사용할 수 있지 않습니다.
			return;
		}
		pc.setCurrentMp(pc.getCurrentMp() - 10);
		L1Teleport.teleport(pc, spellsc_x, spellsc_y,
				pc.getMapId(), pc.getMoveState().getHeading(), true,
				L1Teleport.CHANGE_POSITION, true);
	}
	private void fortune_Cookie(L1PcInstance pc,L1ItemInstance useItem){
		pc.getInventory().removeItem(useItem, 1);
		pc.getInventory().storeItem(437022, 1);
		pc.sendPackets(new S_ServerMessage(403, "$8540"));
		int chance = CommonUtil.random(42);
		if (chance == 0){
			pc.getInventory().storeItem(437027, 1);
		} else if (chance >=1 && chance <= 10) {
			pc.getInventory().storeItem(437028, 1);
		} else if (chance >=11 && chance <= 40) {
			pc.getInventory().storeItem(437029, 1);
		} else if (chance ==41) {
			pc.getInventory().storeItem(437030, 1);						
		}
		pc.sendPackets(new S_ServerMessage(403, "$8539"));
	} 
	private void birthdayCake(L1PcInstance pc){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (pc.getInventory(). checkItem(5000121, 1)){
			pc.getInventory().consumeItem(5000121, 1); 
			L1SpawnUtil.spawn(pc, 450001798, 0, 300000, true);//생일케이크괴물    
		}else if(castle_id != 0) {
			pc.sendPackets(new S_ServerMessage(563)); // 여기에서는 사용할 수 없습니다.
			return;
		}
	}

	public void Ress (L1PcInstance pc, int item_id) {
		int objid = pc.getId();
		pc.sendPackets(new S_SkillSound(objid, 759));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(objid, 759));
		for (L1PcInstance tg : L1World.getInstance(). getVisiblePlayer(pc)) {
			if (tg.getCurrentHp() == 0 && tg.isDead()) {
				Broadcaster.broadcastPacket(tg, new S_SkillSound(tg.getId(), 3944));
				tg.sendPackets(new S_SkillSound(tg.getId(), 3944));
				// 축복된 부활 스크롤과 같은 효과
				tg.setTempID(objid);
				tg.sendPackets(new S_Message_YN(322, "")); // 또 부활하고 싶습니까? (Y/N)
			} else {
				Broadcaster.broadcastPacket(tg, new S_SkillSound(tg.getId(), 832));
				tg.sendPackets(new S_SkillSound(tg.getId(), 832));
				tg.setCurrentHp(tg.getMaxHp());
				tg.setCurrentMp(tg.getMaxMp());
			}
		}
	}

	private void metisCakeBox(L1PcInstance pc,L1ItemInstance useItem){//5000133
		int bonus = CommonUtil.random(80);
		pc.getInventory().storeItem(L1ItemId.METIS_THREE, 1);
		pc.sendPackets(new S_ServerMessage(403, "메티스의 케이크 상자"));
		pc.getInventory().removeItem(useItem, 1);
		L1ItemInstance bonusitem = null;
		switch(bonus/10){
		case 0:	bonusitem = pc.getInventory().storeItem(L1ItemId.weapon_0, 1);break;
		case 1:	bonusitem = pc.getInventory().storeItem(L1ItemId.weapon_1, 1);break;
		case 2:	bonusitem = pc.getInventory().storeItem(L1ItemId.armor_0, 1);break;
		case 3: bonusitem =	pc.getInventory().storeItem(L1ItemId.weapon_1, 1);break;
		case 4:	bonusitem = pc.getInventory().storeItem(L1ItemId.potion_0, 5);break;
		case 5:	bonusitem = pc.getInventory().storeItem(L1ItemId.potion_1, 5);break;
		case 6:	bonusitem = pc.getInventory().storeItem(L1ItemId.potion_2, 5);break;
		case 7:	bonusitem = pc.getInventory().storeItem(L1ItemId.potion_3, 5);break;
		}
		pc.sendPackets(new S_ServerMessage(403, bonusitem.getName()));
	}

	private void EmeraldBox(L1PcInstance pc,L1ItemInstance useItem,int itemId,String str ){
		int bonus = CommonUtil.random(100);
		L1ItemInstance bonusitem = null;

		pc.getInventory().storeItem(L1ItemId.DRAGON_EMERALD, 1);
		pc.sendPackets(new S_ServerMessage(403, "$11518"));
		pc.getInventory().removeItem(useItem, 1);

		switch(bonus/3){
		case 0: bonusitem = pc.getInventory().storeItem(40393, 1);break;
		case 1: bonusitem = pc.getInventory().storeItem(40394, 1);break;
		case 2: bonusitem = pc.getInventory().storeItem(40395, 1);break;
		case 3: bonusitem = pc.getInventory().storeItem(40396, 1);break;
		case 4: case 5: case 6 : case 7: bonusitem = pc.getInventory().storeItem(L1ItemId.DRAGON_PEARL, 1);break;
		case 8:  case 9:  case 10:bonusitem = pc.getInventory().storeItem(L1ItemId.DRAGON_SAPHIRE, 1);break;
		case 11:case 12: case 13: case 14: case 15: bonusitem = pc.getInventory().storeItem(L1ItemId.DRAGON_RUBY, 1);break;
		}
		pc.sendPackets(new S_ServerMessage(403, bonusitem.getItem().getNameId()));
	}

	private void dragonBox(L1PcInstance pc,L1ItemInstance useItem,int itemId,String str ){
		int bonus = CommonUtil.random(100);
		L1ItemInstance bonusitem = null;
		pc.getInventory().storeItem(L1ItemId.DRAGON_DIAMOND, 1);
		pc.sendPackets(new S_ServerMessage(403, "$7969"));
		pc.getInventory().removeItem(useItem, 1);
		switch(bonus/3){
		case 0: bonusitem = pc.getInventory().storeItem(40393, 1);break;
		case 1: bonusitem = pc.getInventory().storeItem(40394, 1);break;
		case 2: bonusitem = pc.getInventory().storeItem(40395, 1);break;
		case 3: bonusitem = pc.getInventory().storeItem(40396, 1);break;
		case 4: case 5: case 6 : case 7: bonusitem = pc.getInventory().storeItem(L1ItemId.DRAGON_PEARL, 1);break;
		case 8:  case 9:  case 10:bonusitem = pc.getInventory().storeItem(L1ItemId.DRAGON_SAPHIRE, 1);break;
		case 11:case 12: case 13: case 14: case 15: bonusitem = pc.getInventory().storeItem(L1ItemId.DRAGON_RUBY, 1);break;
		}
		pc.sendPackets(new S_ServerMessage(403, bonusitem.getItem().getNameId()));
	}

	private void dragonJewel(L1PcInstance pc,L1ItemInstance useItem,int ainhasad ){
		if (pc.getLevel() < 49) {
			pc.sendPackets(new S_ServerMessage(318, "49"));
			return;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGON_EMERALD_YES) == true) {
			pc.sendPackets(new S_ServerMessage(2146));
			return;
		}
		if (pc.getAinHasad() < ainhasad) {
			pc.calAinHasad(1000000);
			pc.sendPackets(new S_PacketBox(S_PacketBox.AINHASAD, pc.getAinHasad()));
			pc.getInventory().removeItem(useItem , 1);
			pc.sendPackets(new S_ServerMessage(2142));
		} else {  
			pc.sendPackets(new S_SystemMessage("축복지수 "+(ainhasad/10000)+"미만에서만 사용하실수 있습니다."));
		}
	}

	private void dragonEmerald(L1PcInstance pc,L1ItemInstance useItem){
		if (pc.getLevel() < 49) {
			pc.sendPackets(new S_ServerMessage(318, "49"));
			return;
		}		
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGON_EMERALD_NO) == true) {
			pc.sendPackets(new S_ServerMessage(2145));//사용할수없다          
			return;
		} else if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGON_EMERALD_YES) == true) {
			pc.sendPackets(new S_ServerMessage(2147));
			return;
		}
		pc.calAinHasad(1000000);
		pc.getSkillEffectTimerSet().setSkillEffect(7786, 10800 * 1000);
		pc.sendPackets(new S_PacketBox(S_PacketBox.EMERALD_EVA, 0x02, 10800));
		pc.sendPackets(new S_ServerMessage(2140));
		pc.getInventory().removeItem(useItem, 1);
	}

	private void changing_Sex_potion(L1PcInstance pc,L1ItemInstance useItem){
		int[] MALE_LIST = new int[] { 0, 61, 138, 734, 2786, 6658, 6671 };
		int[] FEMALE_LIST = new int[] { 1, 48, 37, 1186, 2796, 6661, 6650 };
		if (pc.get_sex() == 0) {
			pc.set_sex(1);
			pc.setClassId(FEMALE_LIST[pc.getType()]);
		} else {
			pc.set_sex(0);
			pc.setClassId(MALE_LIST[pc.getType()]);
		}
		pc.getGfxId().setTempCharGfx(pc.getClassId());
		pc.sendPackets(new S_ChangeShape(pc.getId(), pc.getClassId()));
		Broadcaster.broadcastPacket(pc, new S_ChangeShape(pc.getId(), pc.getClassId()));
		pc.getInventory().removeItem(useItem , 1);
	}
	private void allBuffPotion(L1PcInstance pc,L1ItemInstance useItem,int[] buffSkillId ){ 
		pc.setBuffnoch(1); 
		L1SkillUse l1skilluse = new L1SkillUse();
		for (int i = 0; i < buffSkillId.length ; i++) {
			l1skilluse.handleCommands(pc, buffSkillId[i], pc.getId(), pc.getX(), pc.getY(), null, 0,  L1SkillUse.TYPE_GMBUFF);
		}
		pc.getInventory().removeItem(useItem , 1);
		//1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getHeading(), false); 
		pc.sendPackets(new S_ServerMessage(822)); // 몸 속 깊숙한 곳에서 알 수 없는 힘이 생기는 것을 느낍니다.	
		pc.setBuffnoch(0);
	}
	private void changing_Petname_scroll(L1PcInstance pc,L1ItemInstance useItem,L1ItemInstance l1iteminstance1){
		if(l1iteminstance1.getItem().getItemId() == 40314 || l1iteminstance1.getItem().getItemId() == 40316){
			L1Pet petTemplate = PetTable.getInstance().getTemplate(l1iteminstance1.getId());
			L1Npc l1npc = NpcTable.getInstance().getTemplate(petTemplate.get_npcid());
			if (petTemplate == null) {
				throw new NullPointerException();
			}
			petTemplate.set_name(l1npc.get_name());
			PetTable.getInstance().storePet(petTemplate);
			L1ItemInstance item = pc.getInventory().getItem(l1iteminstance1.getId());
			pc.getInventory().updateItem(item);
			pc.getInventory().removeItem(useItem, 1);
			pc.sendPackets(new S_ServerMessage(1322, l1npc.get_name()));
			pc.sendPackets(new S_ChangeName(petTemplate.get_objid(), l1npc.get_name()));
			Broadcaster.broadcastPacket(pc, new S_ChangeName(petTemplate.get_objid(), l1npc.get_name()));
		}else{
			pc.sendPackets(new S_ServerMessage(1164));
		}
	}

	private void KillAndDeathReset(L1PcInstance pc,L1ItemInstance useItem){
		try{
			pc.setKills(0);
			pc.setDeaths(0);
			pc.set_KillDeathInitialize(pc.get_KillDeathInitialize() + 1);
			pc.sendPackets(new S_SystemMessage(pc.getName() + "님의 킬,데스 수치가 초기화되었습니다."));
			pc.getInventory().removeItem(useItem, 1);
			pc.save();
		} catch (Exception c) {
		}
	}

	private void firewood(L1PcInstance pc,L1ItemInstance useItem){
		for (L1Object object : L1World.getInstance().getVisibleObjects(pc, 3)) {
			if (object instanceof L1EffectInstance) {
				if (((L1NpcInstance) object).getNpcTemplate().get_npcId() == 81170) {
					pc.sendPackets(new S_ServerMessage(1162)); // 벌써 주위에 모닥불이 있습니다.
					return;
				}
			}
		}
		int[] loc = new int[2];
		loc = CharPosUtil.getFrontLoc(pc.getX(), pc.getY(), pc.getMoveState().getHeading());
		L1EffectSpawn.getInstance().spawnEffect(81170, 600000, loc[0], loc[1], pc.getMapId());
		pc.getInventory().removeItem(useItem, 1);
	}

	private void holyWater(L1PcInstance pc,L1ItemInstance useItem,int server_M ,int skillId,int checkSkillId, int removeSkillId ){
		if (pc.getSkillEffectTimerSet().hasSkillEffect(checkSkillId)) {//STATUS_HOLY_WATER_OF_EVA
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
			return;
		}

		if (pc.getSkillEffectTimerSet().hasSkillEffect(removeSkillId)) {//STATUS_HOLY_MITHRIL_POWDER
			pc.getSkillEffectTimerSet().removeSkillEffect(removeSkillId);//STATUS_HOLY_MITHRIL_POWDER
		}
		pc.getSkillEffectTimerSet().setSkillEffect(skillId, 900 * 1000);//STATUS_HOLY_WATER
		pc.sendPackets(new S_SkillSound(pc.getId(), 190));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 190));
		pc.sendPackets(new S_ServerMessage(server_M)); //1141 
		pc.getInventory().removeItem(useItem, 1);
	}

	private void holyWater(L1PcInstance pc,L1ItemInstance useItem){
		if(pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_HOLY_WATER)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_HOLY_MITHRIL_POWDER)) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
			return;
		}
		pc.getSkillEffectTimerSet().setSkillEffect(STATUS_HOLY_WATER_OF_EVA, 900 * 1000);
		pc.sendPackets(new S_SkillSound(pc.getId(), 190));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 190));
		pc.sendPackets(new S_ServerMessage(1140));
		pc.getInventory().removeItem(useItem, 1);
	}
	private void create_Chance_Item_Safe(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem ,int minItemId,int maxItemId,int num){
		//　신비적인 일부(1~4 단계)
		int earing2Id = l1iteminstance1.getItem().getItemId();
		//int potion1 = 0;
		//int potion2 = 0;
		// 귀 링류
		if (earing2Id >= minItemId && maxItemId >= earing2Id ) {
			if ((CommonUtil.random(99)+1) < Config.CREATE_CHANCE_MYSTERIOUS) {
				createNewItem(pc, (earing2Id - num), 1);//-12
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(useItem, 1);	
			} else {
				pc.sendPackets(new S_ServerMessage(160, l1iteminstance1.getName()));
				// \f1%0이%2 강렬하게%1 빛났습니다만, 다행히 무사하게 살았습니다.
				pc.getInventory().removeItem(useItem, 1);
			}
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}

	private void adenaBankbook(L1PcInstance pc){
		if (pc.getInventory().checkItem(40308, 100000000)) {
			pc.getInventory().consumeItem(40308, 100000000);
			pc.getInventory().storeItem(400077, 1);
			pc.sendPackets(new S_SystemMessage("1억 아데나가 1억 수표로 변환되었습니다."));
		} else {
			pc.sendPackets(new S_SystemMessage("1억 아데나가 있어야 가능합니다."));}	 
	}
	
	private void adenaCheck(L1PcInstance pc){
		if(pc.getInventory().getSize() <= 200000000){
			pc.getInventory().storeItem(40308, 100000000);
			pc.getInventory().consumeItem(400077, 1);
			pc.sendPackets(new S_SystemMessage("1억 아데나로 변환되었습니다."));
		} else {
			pc.sendPackets(new S_SystemMessage("20억 아데나가 넘었습니다. 변환할수없습니다.."));
		}
	}

	private void FirstPkg (L1PcInstance pc){
		if (pc.getInventory().checkItem(5000149, 1)) { // 체크 되는 아이템과 수량
			pc.getInventory().consumeItem(5000149, 1); // 삭제되는 아이템과 수량	
			createNewItem2(pc, 120056, 1, 7); //마망
			createNewItem2(pc, 20011, 1, 7); //마투
			createNewItem2(pc, 20187, 1, 7); //파글
			createNewItem2(pc, 490015, 1, 7); //스턴티
			createNewItem2(pc, 20200, 1, 7); //마부
			createNewItem2(pc, 20280, 1, 0); //멸마
			createNewItem2(pc, 20280, 1, 0); //멸마
			createNewItem2(pc, 21024, 1, 0); //우호도5단귀
			createNewItem2(pc, 20362, 1, 0); //우호도5단목
		}
	}

	private void SecondPkg (L1PcInstance pc){
		if (pc.getInventory().checkItem(5000156, 1)) { // 체크 되는 아이템과 수량
			pc.getInventory().consumeItem(5000156, 1); // 삭제되는 아이템과 수량	
			createNewItem2(pc, 120056, 1, 9); //마망
			createNewItem2(pc, 20011, 1, 9); //마투
			createNewItem2(pc, 20178, 1, 8); //암장
			createNewItem2(pc, 490015, 1, 8); //스턴티
			createNewItem2(pc, 20200, 1, 8); //마부						
			createNewItem2(pc, 21027, 1, 0); //우호도8단귀
			createNewItem2(pc, 20365, 1, 0); //우호도8단목
		}
	}
	private void finishedTreasureMap(L1PcInstance pc,L1ItemInstance useItem){
		if (pc.getInventory().checkItem(40621)) {
			// \f1 아무것도 일어나지 않았습니다.
			pc.sendPackets(new S_ServerMessage(79));
		} else if (((pc.getX() >= 32856 && pc.getX() <= 32858)|| (pc.getY() >= 32857 && pc.getY() <= 32858))&& pc.getMapId() == 443) { // 해적섬의 지하 감옥 3층
			L1Teleport.teleport(pc, ((L1EtcItem) useItem.getItem()).get_locx(), ((L1EtcItem) useItem.getItem()).get_locy(),
					((L1EtcItem) useItem.getItem()).get_mapid(), 5, true);
		} else {
			// \f1 아무것도 일어나지 않았습니다.
			pc.sendPackets(new S_ServerMessage(79));
		}
	}
	//마법주문서 
	private void magicScroll(L1PcInstance pc,L1ItemInstance useItem ,LineageClient client,int itemId,int spellsc_objid,int spellsc_x, int spellsc_y ){
		int chance = CommonUtil.random(10) ;
		if (pc.isSkillDelay()) {
			pc.sendPackets(new S_ServerMessage(281));	
			return;
		}
		if(chance != 0){
			if (spellsc_objid == pc.getId() && useItem.getItem().getUseType() != 30) { 
				// spell_buff
				pc.sendPackets(new S_ServerMessage(281)); // \f1마법이 무효가 되었습니다.
				return;
			}
			pc.getInventory().removeItem(useItem, 1);
			if (spellsc_objid == 0
					&& useItem.getItem().getUseType() != 0
					&& useItem.getItem().getUseType() != 26
					&& useItem.getItem().getUseType() != 27) {
				return;
				// 타겟이 없는 경우에 handleCommands전송 되기 (위해)때문에 여기서 return
				// handleCommands 쪽으로 판단＆처리해야 할 부분일지도 모른다
			}
			pc.cancelAbsoluteBarrier(); // 앱솔의해체
			int skillid = itemId - 40858;
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(client.getActiveChar(),
					skillid, spellsc_objid, spellsc_x, spellsc_y,
					null, 0, L1SkillUse.TYPE_SPELLSC);
		}else {
			pc.sendPackets(new S_ServerMessage(280)); // 마법이 실패했습니다.
		}
	}
	//마법주문서 7~10단계
	private void magicScroll2(L1PcInstance pc,L1ItemInstance useItem ,LineageClient client,int itemId,int spellsc_objid,int spellsc_x, int spellsc_y ){
		int chance = CommonUtil.random(10) ;
		if (pc.isSkillDelay()) {
			pc.sendPackets(new S_ServerMessage(281));	
			return;
		}
		if(chance != 0){
			if (spellsc_objid == pc.getId() && useItem.getItem().getUseType() != 30) { 
				// spell_buff
				pc.sendPackets(new S_ServerMessage(281)); // \f1마법이 무효가 되었습니다.
				return;
			}
			pc.getInventory().removeItem(useItem, 1);
			if (spellsc_objid == 0
					&& useItem.getItem().getUseType() != 0
					&& useItem.getItem().getUseType() != 26
					&& useItem.getItem().getUseType() != 27) {
				return;
				// 타겟이 없는 경우에 handleCommands전송 되기 (위해)때문에 여기서 return
				// handleCommands 쪽으로 판단＆처리해야 할 부분일지도 모른다
			}
			pc.cancelAbsoluteBarrier(); // 앱솔의해체
			int skillid = itemId - 41880;
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(client.getActiveChar(),
					skillid, spellsc_objid, spellsc_x, spellsc_y,
					null, 0, L1SkillUse.TYPE_SPELLSC);
		} else {
			pc.sendPackets(new S_ServerMessage(280)); // 마법이 실패했습니다.
		}
	}
	private void close_Lower_Oriris_present(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem, int itemId){
		int itemId2 = l1iteminstance1.getItem().getItemId();
		if (itemId == L1ItemId.LOWER_OSIRIS_PRESENT_PIECE_DOWN && itemId2 == L1ItemId.LOWER_OSIRIS_PRESENT_PIECE_UP){
			if(pc.getInventory().checkItem(L1ItemId.LOWER_OSIRIS_PRESENT_PIECE_UP)){
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(useItem, 1);
				pc.getInventory().storeItem(L1ItemId.CLOSE_LOWER_OSIRIS_PRESENT, 1);
			}
		}
		if (itemId == L1ItemId.HIGHER_OSIRIS_PRESENT_PIECE_DOWN  && itemId2 == L1ItemId.HIGHER_OSIRIS_PRESENT_PIECE_UP){
			if(pc.getInventory().checkItem(L1ItemId.HIGHER_OSIRIS_PRESENT_PIECE_UP)){
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(useItem, 1);
				pc.getInventory().storeItem(L1ItemId.CLOSE_HIGHER_OSIRIS_PRESENT, 1);
			}
		}else{
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}
	private void close_Lower_Tikal_Present(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem, int itemId){
		int itemId2 = l1iteminstance1.getItem().getItemId();
		if (itemId == L1ItemId.LOWER_TIKAL_PRESENT_PIECE_DOWN && itemId2 == L1ItemId.LOWER_TIKAL_PRESENT_PIECE_UP){
			if(pc.getInventory().checkItem(L1ItemId.LOWER_TIKAL_PRESENT_PIECE_UP)){
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(useItem, 1);
				pc.getInventory().storeItem(L1ItemId.CLOSE_LOWER_TIKAL_PRESENT, 1);
			}
		}else if (itemId == L1ItemId.HIGHER_TIKAL_PRESENT_PIECE_DOWN  && itemId2 == L1ItemId.HIGHER_TIKAL_PRESENT_PIECE_UP){
			if(pc.getInventory().checkItem(L1ItemId.HIGHER_TIKAL_PRESENT_PIECE_UP)){
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(useItem, 1);
				pc.getInventory().storeItem(L1ItemId.CLOSE_HIGHER_TIKAL_PRESENT, 1);
			}
		}else{
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}

	private void ancient_RoyalSeal(L1PcInstance pc,L1ItemInstance useItem,LineageClient client){
		if (client.getAccount().getCharSlot() < 8){
			client.getAccount().setCharSlot(client, client.getAccount().getCharSlot()+1);
			pc.getInventory().removeItem(useItem, 1);
		}else{
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}	
	}
	private void timeCrack_Core(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem){
		int itemId2 = l1iteminstance1.getItem().getItemId();
		switch(itemId2){
		case L1ItemId.CLOSE_LOWER_OSIRIS_PRESENT : 
			item_Create(pc,useItem,L1ItemId.CLOSE_LOWER_OSIRIS_PRESENT , itemId2,(L1ItemId.OPEN_LOWER_OSIRIS_PRESENT)); break;
		case L1ItemId.CLOSE_HIGHER_OSIRIS_PRESENT :
			item_Create(pc,useItem,L1ItemId.CLOSE_HIGHER_OSIRIS_PRESENT , itemId2,(L1ItemId.OPEN_HIGHER_OSIRIS_PRESENT)); break;
		case L1ItemId.CLOSE_LOWER_TIKAL_PRESENT : 
			item_Create(pc,useItem,L1ItemId.CLOSE_LOWER_TIKAL_PRESENT , itemId2,(L1ItemId.OPEN_LOWER_TIKAL_PRESENT)); break;
		case L1ItemId.CLOSE_HIGHER_TIKAL_PRESENT : 
			item_Create(pc,useItem,L1ItemId.CLOSE_HIGHER_TIKAL_PRESENT , itemId2,(L1ItemId.OPEN_HIGHER_TIKAL_PRESENT)); break;
		default : pc.sendPackets(new S_ServerMessage(79)); break;
		}
	}

	private void ago_Scroll(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem){
		int itemId2 = l1iteminstance1.getItem().getItemId();
		switch(itemId2){
		case 20140 : 
			item_Create(pc,useItem, 20140 , itemId2, 20092); break;
		case 20141 :
			item_Create(pc,useItem, 20141 , itemId2, 20093); break;
		case 20142 : 
			item_Create(pc,useItem, 20142 , itemId2, 20094); break;
		case 20143 : 
			item_Create(pc,useItem, 20143 , itemId2, 20095); break;
		case 17 : 
			item_Create(pc,useItem, 17 , itemId2, 67); break;
		case 18 : 
			item_Create(pc,useItem, 18, itemId2, 68); break;
		case 167 : 
			item_Create(pc,useItem, 167 , itemId2, 182); break;
		default : pc.sendPackets(new S_ServerMessage(79)); break;
		}
	}

	private void newclan (L1PcInstance pc, L1ItemInstance useItem, int safetylevel, int clanid, String ClanName ){
		try {
			if (pc.getLevel() < safetylevel){
				if (pc.getClanid() == 0 ) {
					pc.getInventory().removeItem(useItem, 1);
					L1Clan clan = L1World.getInstance().getClan(ClanName); 
					pc.setClanid(clanid); 
					pc.setClanRank(3);
					pc.setClanname(ClanName); 
					pc.setTitle("["+ Config.servername + "]" + ClanName); 
					pc.getInventory().removeItem(useItem, 1); 
					clan.addClanMember(pc.getName(), pc.getClanRank());          
					pc.save();
					UserCommands.tell(pc);
					pc.sendPackets(new S_SystemMessage(pc.getName() + "님은 " + ClanName + "혈맹 혈맹에 가입하였습니다."));
					pc.sendPackets(new S_SystemMessage("렙 "+ Config.NEWUSERSAFETY_LEVEL +"까지 PVP가 되지 않으며, 공격 받지도 않습니다. 렙" + safetylevel +"이 되는 순간 자동으로 혈맹에서 추방됩니다."));
				} else {
					pc.sendPackets(new S_ServerMessage(89)); // 이미 혈맹에 가입했습니다.
				}
			} else {
				pc.sendPackets(new S_SystemMessage("렙 "+ safetylevel + "이 넘으시면 신규 혈맹에 가입하실 수 없습니다"));
			}
		} catch (Exception e) {	}
	}
	private void curseClear(L1PcInstance pc,L1ItemInstance useItem, int itemId){
		L1Item template = null;
		for (L1ItemInstance eachItem : pc.getInventory().getItems()) {					
			if (eachItem.getItem().getBless() != 2) {
				continue;
			}
			if (!eachItem.isEquipped() && (itemId == 40119 || itemId == 40097)) {
				// n해주는 장비 하고 있는 것 밖에 해주 하지 않는다
				continue;
			}
			int id_normal = eachItem.getItemId() - 200000;
			template = ItemTable.getInstance().getTemplate(id_normal);
			if (template == null) {
				continue;
			}
			if (pc.getInventory().checkItem(id_normal) && template.isStackable()) {
				pc.getInventory().storeItem(id_normal, eachItem.getCount());
				pc.getInventory().removeItem(eachItem, eachItem.getCount());
			} else {
				eachItem.setItem(template);
				pc.getInventory().updateItem(eachItem, L1PcInventory.COL_ITEMID);
				pc.getInventory().saveItem(eachItem, L1PcInventory.COL_ITEMID);
				eachItem.setBless(eachItem.getBless() - 1);
				pc.getInventory().updateItem(eachItem, L1PcInventory.COL_BLESS);
				pc.getInventory().saveItem(eachItem, L1PcInventory.COL_BLESS);
			}
		}
		pc.getInventory().removeItem(useItem, 1);
		pc.sendPackets(new S_ServerMessage(155)); // \f1누군가가 도와 준 것 같습니다.
	}
	private void create_Chance_Item_Delete(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem ,int minItemId,int maxItemId,int num){
		int diaryId = l1iteminstance1.getItem().getItemId();
		if (diaryId >= minItemId && maxItemId >= diaryId) {
			if ((CommonUtil.random(99)+1) <= Config.CREATE_CHANCE_DIARY) {
				createNewItem(pc, diaryId + num, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(158, l1iteminstance1.getName())); // \f1%0이 증발하고 있지 않게 되었습니다.
			}
			pc.getInventory().removeItem(l1iteminstance1, 1);
			pc.getInventory().removeItem(useItem, 1);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
	}
	private void repairItem(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem){
		if (l1iteminstance1.getItem().getType2() != 0 && l1iteminstance1.get_durability() > 0) {
			String msg0;
			pc.getInventory().recoveryDamage(l1iteminstance1);
			msg0 = l1iteminstance1.getLogName();
			if (l1iteminstance1.get_durability() == 0) {
				pc.sendPackets(new S_ServerMessage(464, msg0)); // %0%s는 신품 같은 상태가 되었습니다.
			} else {
				pc.sendPackets(new S_ServerMessage(463, msg0)); // %0 상태가 좋아졌습니다.
			}
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 아무것도 일어나지 않았습니다.
		}
		pc.getInventory().removeItem(useItem, 1);
	}
	private void cure_Posion(L1PcInstance pc,int itemId,L1ItemInstance useItem){
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // 디케이포션 상태
			pc.sendPackets(new S_ServerMessage(698)); // 마력에 의해 아무것도 마실 수가 없습니다.
		} else {
			pc.cancelAbsoluteBarrier(); // 아브소르트바리아의 해제
			pc.sendPackets(new S_SkillSound(pc.getId(), 192));
			Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 192));
			if (itemId == L1ItemId.POTION_OF_CURE_POISON) {
				removeItem(pc,useItem);
			} else if (itemId == 40507) {
				removeItem(pc,useItem);
			}
			pc.curePoison();
		}
	}

	private void newUseSupportBox(L1PcInstance pc){
		int count = pc.getInventory().countItems(447011); 
		if (pc.getInventory().checkItem(447011, count)) { // 체크 되는 아이템과 수량
			pc.getInventory().consumeItem(447011, count); // 삭제되는 아이템과 수량	
			switch(pc.getClassId()){
			case L1PcInstance.CLASSID_PRINCE: 
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 426003, 1, 0); // 베테르랑 판금 갑옷
				createNewItem2(pc, 426013, 1, 0);// 샤르나의 망토
				createNewItem2(pc, 40031, 10, 0); //악마의 피
				break;
			case L1PcInstance.CLASSID_PRINCESS:  
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 426003, 1, 0); // 베테르랑 판금 갑옷
				createNewItem2(pc, 426014, 1, 0);// 샤르나의 망토
				createNewItem2(pc, 40031, 10, 0); //악마의 피
				break;
			case L1PcInstance.CLASSID_KNIGHT_MALE:  
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 426003, 1, 0); // 베테르랑 판금 갑옷
				createNewItem2(pc, 426015, 1, 0); // 샤르나의 망토 
				createNewItem2(pc, 40014, 10, 0); //용기의물약
				break;
			case L1PcInstance.CLASSID_KNIGHT_FEMALE:  
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 426003, 1, 0); // 베테르랑 판금 갑옷
				createNewItem2(pc, 426016, 1, 0); // 샤르나의 망토 
				createNewItem2(pc, 40014, 10, 0); //용기의물약
				break;

			case L1PcInstance.CLASSID_ELF_MALE: 
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 426017, 1, 0); // 샤르나의 망토 
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
				createNewItem2(pc, 40744, 1000, 0); //은화살
				createNewItem2(pc, 40068, 5, 0); //엘븐와퍼
				createNewItem2(pc, 40114, 10, 0);  //
				break;
			case L1PcInstance.CLASSID_ELF_FEMALE: 
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 426018, 1, 0); // 샤르나의 망토 
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
				createNewItem2(pc, 40744, 1000, 0); //은화살
				createNewItem2(pc, 40068, 5, 0); //엘븐와퍼
				createNewItem2(pc, 40114, 10, 0);  //
				break;

			case L1PcInstance.CLASSID_WIZARD_MALE:  
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 305, 1, 7); // 베테르랑 지팡이
				createNewItem2(pc, 426005, 1, 0); // 베테르랑 로브
				createNewItem2(pc, 426019, 1, 0); // 샤르나의 망토 
				break;
			case L1PcInstance.CLASSID_WIZARD_FEMALE: 
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 305, 1, 7); // 베테르랑 지팡이
				createNewItem2(pc, 426005, 1, 0); // 베테르랑 로브
				createNewItem2(pc, 426020, 1, 0); // 샤르나의 망토 
				break;

			case L1PcInstance.CLASSID_DARKELF_MALE: 
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 306, 1, 7); // 베테르랑 크로우
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 426021, 1, 0);// 샤르나의 망토 
				break;
			case L1PcInstance.CLASSID_DARKELF_FEMALE: 
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 306, 1, 7); // 베테르랑 크로우
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 426022, 1, 0);// 샤르나의 망토 
				break;

			case L1PcInstance.CLASSID_DRAGONKNIGHT_MALE:  
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 307, 1, 7); // 베테르랑 체인소드
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 426023, 1, 0);// 샤르나의 망토 
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
				break;
			case L1PcInstance.CLASSID_DRAGONKNIGHT_FEMALE: 
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 307, 1, 7); // 베테르랑 체인소드
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 426024, 1, 0);// 샤르나의 망토 
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
				break;

			case L1PcInstance.CLASSID_ILLUSIONIST_MALE:  
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 305, 1, 7); // 베테르랑 체인소드
				createNewItem2(pc, 426005, 1, 0); // 베테르랑 로브
				createNewItem2(pc, 426025, 1, 0); // 샤르나의 망토 
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
				break;
			case L1PcInstance.CLASSID_ILLUSIONIST_FEMALE: 
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 305, 1, 7); // 베테르랑 체인소드
				createNewItem2(pc, 426005, 1, 0); // 베테르랑 로브
				createNewItem2(pc, 426026, 1, 0); // 샤르나의 망토 
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
				break;
			}

			createNewItem2(pc, 426001, 1, 0); // 베테르랑 해골 투구
			createNewItem2(pc, 426002, 1, 0); // 베테르랑 티셔츠
			createNewItem2(pc, 426006, 1, 0); // 베테르랑 마법 망토
			createNewItem2(pc, 426007, 1, 0);  // 베테르랑 장화
			createNewItem2(pc, 426008, 1, 0); // 베테르랑 방패
			createNewItem2(pc, 426009, 1, 0); // 베테르랑 장갑
			createNewItem2(pc, 426010, 1, 0); // 베테르랑 가더
			createNewItem2(pc, 20282, 1, 0); //상아탑반지
			createNewItem2(pc, 20282, 1, 0); //상아탑반지
			createNewItem2(pc, 40308, 50000, 0); //아데나
			createNewItem2(pc, 41159, 20, 0); //깃털
			createNewItem2(pc, 437004, 1, 0); //전투 주문서
			createNewItem2(pc, 40081, 50, 0); //기란마을귀환주문서
			createNewItem2(pc, 40099, 100, 0); //순간이동주문서
			createNewItem2(pc, 140100, 10, 0); //축순간이동주문서
			createNewItem2(pc, 40088, 10, 0); //변신주문서
			createNewItem2(pc, 40126, 10, 0); //확인주문서
			createNewItem2(pc, 50020, 10, 0); //봉인 주문서
			createNewItem2(pc, 40021, 100, 0); //농축강력제
			createNewItem2(pc, 40018, 5, 0); //강촐
			createNewItem2(pc, 430005, 1, 0); //회상의 촛불 
			createNewItem2(pc, 41246, 1000, 0); //결정체
			createNewItem2(pc, 560025, 10, 0); //마을 기억책 
			createNewItem2(pc, 560027, 10, 0); //던젼 기억책 
			createNewItem2(pc, 435009, 1, 0); //코마세트
			createNewItem2(pc, 435010, 1, 0); //코마세트
			createNewItem2(pc, 435011, 1, 0); //코마세트
			createNewItem2(pc, 435012, 1, 0); //코마세트
			createNewItem2(pc, 435013, 1, 0); // 코마세트
			createNewItem2(pc, 435000, 5, 0); // 진주
			createNewItem2(pc, 437031, 5, 0); // 대박버프쪽지
		} 
	}

	private void newUseSupportBox2(L1PcInstance pc){
		if (pc.getInventory().checkItem(447013, 1)) { // 체크 되는 아이템과 수량
			pc.getInventory().consumeItem(447013, 1); // 삭제되는 아이템과 수량	
			if (pc.isKnight()) { // 기사 지급
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 426003, 1, 0); // 베테르랑 판금 갑옷
				createNewItem2(pc, 40014, 10, 0); //용기의물약
			} else if (pc.isDragonknight()) {	
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 307, 1, 7); // 베테르랑 체인소드
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
			} else if (pc.isCrown()) {
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 303, 1, 7); // 베테르랑 양손검
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 40031, 10, 0); //악마의 피
			} else if (pc.isWizard()) {
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 305, 1, 7); // 베테르랑 지팡이
				createNewItem2(pc, 426005, 1, 0); // 베테르랑 로브
			} else if (pc.isIllusionist()) {
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 305, 1, 7); // 베테르랑 체인소드
				createNewItem2(pc, 426005, 1, 0); // 베테르랑 로브
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
			} else if (pc.isElf()) {
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 302, 1, 7); // 베테르랑 한손검
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
				createNewItem2(pc, 430006, 10, 0); //유그드라열매
				createNewItem2(pc, 40744, 1000, 0); //은화살
				createNewItem2(pc, 40068, 5, 0); //엘븐와퍼
				createNewItem2(pc, 40114, 10, 0);  //
			} else if (pc.isDarkelf()) {
				createNewItem2(pc, 301, 1, 7); // 베테르랑 단검
				createNewItem2(pc, 304, 1, 7); // 베테르랑 보우건
				createNewItem2(pc, 306, 1, 7); // 베테르랑 크로우
				createNewItem2(pc, 426004, 1, 0); // 베테르랑 가죽갑옷
			}
			createNewItem2(pc, 426001, 1, 0); // 베테르랑 해골 투구
			createNewItem2(pc, 426002, 1, 0); // 베테르랑 티셔츠
			createNewItem2(pc, 426006, 1, 0); // 베테르랑 마법 망토
			createNewItem2(pc, 426007, 1, 0);  // 베테르랑 장화
			createNewItem2(pc, 426008, 1, 0); // 베테르랑 방패
			createNewItem2(pc, 426009, 1, 0); // 베테르랑 장갑
			createNewItem2(pc, 426010, 1, 0); // 베테르랑 가더
			createNewItem2(pc, 20282, 1, 0); //상아탑반지
			createNewItem2(pc, 20282, 1, 0); //상아탑반지
			//createNewItem2(pc, 4500172, 5, 0); //80변신주문서
			//createNewItem2(pc, 4500173, 5, 0); //80변신주문서
		} 
	}
	//즉시엠채워주는 아이템 
	private void MpPosion(L1PcInstance pc,L1ItemInstance useItem ,int currentMp , int randomCurrentMp){
		pc.sendPackets(new S_ServerMessage(338, "$1084")); // 당신의%0가 회복해 갈 것입니다.
		pc.sendPackets(new S_SkillSound(pc.getId(), 190));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 190));
		pc.setCurrentMp(pc.getCurrentMp() + (currentMp + CommonUtil.random(randomCurrentMp))); // 7~12
		pc.getInventory().removeItem(useItem, 1);
	}

	private void dollchange(L1PcInstance pc,L1ItemInstance useItem, L1ItemInstance l1iteminstance1){
		int itemId = useItem.getItem().getItemId();
		int i = l1iteminstance1.getItem().getItemId();
		boolean isAppear = true;
		L1DollInstance doll = null;
		Object[] dollList = pc.getDollList().values().toArray();
		for (Object dollObject : dollList) {
			doll = (L1DollInstance) dollObject;
			if (doll.getItemObjId() == itemId) { 
				isAppear = false;
				break;
			}
		}
		if (isAppear) {
			if (dollList.length >= 1) {       
				pc.sendPackets(new S_ServerMessage(1181)); // 해당 마법 인형은 현재 사용 중입니다.
				return;
			}
			if ((i == 41248) || (i == 41249) || (i == 41250) || (i == 430000) || (i == 430001) || (i == 430002) 
					|| (i == 430003) || (i == 430004) || (i == 430500) || (i == 430505) || (i == 430506) || (i == 5000034)) 
			{
				int i50 = CommonUtil.random(130);
				switch(i50/10){
				case 0 :	pc.sendPackets(new S_SystemMessage("인형이 증발되어 사라집니다.ㅠ_ㅠ")); break;
				case 1 :	pc.getInventory().storeItem(41249, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 서큐버스를 얻었습니다.")); break;
				case 2 :	pc.getInventory().storeItem(41250, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 늑대인간을 얻었습니다.")); break;
				case 3 :	pc.getInventory().storeItem(430000, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 돌골렘을 얻었습니다.")); break;
				case 4 :	pc.getInventory().storeItem(430001, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 장로를 얻었습니다.")); break;
				case 5 :	pc.getInventory().storeItem(430002, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 크러스트시안을 얻었습니다.")); break;
				case 6 :	pc.getInventory().storeItem(430003, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 시댄서를 얻었습니다.")); break;
				case 7 :	pc.getInventory().storeItem(430004, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 에티를 얻었습니다.")); break;
				case 8 :	pc.getInventory().storeItem(5000034, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 에틴을 얻었습니다.")); break;
				case 9 :	pc.getInventory().storeItem(430500, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 코카트리스를 얻었습니다.")); break;
				case 10 :pc.getInventory().storeItem(430505, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 라미아를 얻었습니다.")); break;
				case 11 :pc.getInventory().storeItem(41915, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 허수아비를 얻었습니다."));break;
				case 12 :pc.getInventory().storeItem(430506, 1);
				pc.sendPackets(new S_SystemMessage("마법인형 : 스파토이를 얻었습니다."));break;
				}
				pc.getInventory().removeItem(useItem, 1);
				pc.getInventory().removeItem(l1iteminstance1, 1);                           
			}
		}
	}
	private void byBloodPledgeJoin(L1PcInstance pc,L1ItemInstance useItem)throws Exception{
		if (pc.getInventory(). checkItem(4500155, 1)){  //인벤에 있나 체크
			pc.getInventory().consumeItem(4500155, 1); // 소모
			if (pc.isCrown()) { // 군주라면
				if(pc.get_sex() == 0){ // 왕자라면
					pc.sendPackets(new S_ServerMessage(87)); // 당신은 왕자입니다
				}else{ // 공주라면
					pc.sendPackets(new S_ServerMessage(88)); // 당신은 공주입니다
				}
				return;
			}
			if(pc.getClanid() != 0){ // 혈맹이 있다면
				pc.sendPackets(new S_ServerMessage(89)); // 이미 혈맹이 있습니다
				return;
			}
			Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			Statement pstm2 = con.createStatement(); 
			ResultSet rs2 = pstm2.executeQuery("SELECT `account_name`, `char_name`, `ClanID`, `Clanname` FROM `characters` WHERE Type = 0"); // 케릭터 테이블에서 군주만 골라와서
			while(rs2.next()){
				if(pc.getNetConnection().getAccountName().equalsIgnoreCase(rs2.getString("account_name"))){ // 현재 접속한 계정과 계정을 비교해서 동일하면
					if(rs2.getInt("ClanID") != 0){ // 군주의 혈맹이 있다면
						L1Clan clan = L1World.getInstance().getClan(rs2.getString("Clanname")); // 군주의 혈맹으로 가입
						L1PcInstance clanMember[] = clan.getOnlineClanMember();
						for (int cnt = 0; cnt < clanMember.length; cnt++) { // 접속한 혈맹원에게 메세지 뿌리고
							clanMember[cnt].sendPackets(new S_ServerMessage(94, pc.getName())); // \f1%0이 혈맹의 일원으로서 받아들여졌습니다.
						}
						pc.setClanid(rs2.getInt("ClanID"));
						pc.setClanRank(2);
						pc.setClanname(rs2.getString("Clanname"));
						pc.save(); // DB에 캐릭터 정보를 기입한다
						clan.addClanMember(pc.getName(), pc.getClanRank());
						pc.sendPackets(new S_ServerMessage(95, rs2.getString("Clanname"))); // \f1%0 혈맹에 가입했습니다. // 메세지 보내고
						pc.getInventory().removeItem(useItem, 1); 
						break;
					}
				}
			}
			rs2.first(); // 쿼리를 처음으로 되돌리고
			rs2.close();//여기부터 아래까지 리소스삭제부분 
			pstm2.close();
			con.close();
			if(pc.getClanid() == 0){ // 혈맹이 있다면
				pc.sendPackets(new S_SystemMessage("\\fY계정내에 군주가 없거나 혈맹이 창설되지 않았습니다.")); // 메세지 보내고
			}	
		}	
	}

	private void  flashlight_Gas(L1PcInstance pc,L1ItemInstance useItem){
		for (L1ItemInstance lightItem : pc.getInventory().getItems()) {
			if (lightItem.getItem().getItemId() == 40002) {
				lightItem.setRemainingTime(useItem.getItem().getLightFuel());
				pc.sendPackets(new S_ItemName(lightItem));
				pc.sendPackets(new S_ServerMessage(230));
				break;
			}
		}
		pc.getInventory().removeItem(useItem, 1);
	}
	//아이템유즈 스위치문으로 대체작업실시메서드부분! 
	//술 아이템

	private void unfitforuseItem(L1PcInstance pc, L1ItemInstance useItem){
		int locX = ((L1EtcItem) useItem.getItem()).get_locx();
		int locY = ((L1EtcItem) useItem.getItem()).get_locy();
		short mapId = ((L1EtcItem) useItem.getItem()).get_mapid();
		if (locX != 0 && locY != 0) {
			if (pc.getMap().isEscapable() || pc.isGm()) {
				L1Teleport.teleport(pc, locX, locY, mapId, pc.getMoveState().getHeading(), true);
				pc.getInventory().removeItem(useItem, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(647));
			}
			pc.cancelAbsoluteBarrier(); 
		} else {
			if (useItem.getCount() < 1) { 
				pc.sendPackets(new S_ServerMessage(329, useItem.getLogName())); 
			} else {
				pc.sendPackets(new S_ServerMessage(74, useItem.getLogName())); 
			}
		}
	}
	private void liquor(L1PcInstance pc){
		pc.setDrink(true);
		pc.sendPackets(new S_Liquor(pc.getId()));
	}

	private void exp_Posion(L1PcInstance pc){
		if (pc.getLevel() < 51){//51미만이면 한방에 51까지
			pc.addExp((ExpTable.getExpByLevel(51)-1) - pc.getExp()+((ExpTable.getExpByLevel(51)-1)/100));
			pc.getInventory().consumeItem(7062, 1);
		} else{  
			pc.addExp((ExpTable.getExpByLevel(pc.getLevel()+1)-1) - pc.getExp()+100);
			pc.getInventory().consumeItem(7062, 1);
		}
	}

	private void exp_PosionConsumable(L1PcInstance pc){
		if (pc.getLevel() < 51){//51미만이면 한방에 51까지
			pc.addExp((ExpTable.getExpByLevel(51)-1) - pc.getExp()+((ExpTable.getExpByLevel(51)-1)/100));
			pc.getInventory().consumeItem(7063, 1);
		} else{  
			pc.addExp((int)((ExpTable.getNeedExpNextLevel(pc.getLevel()+1)-1) * 0.01));
			pc.getInventory().consumeItem(7063, 1);       
		} 
	}

	private void eventT(L1PcInstance pc, L1ItemInstance useItem,int storeItem_N, int storeItem_S  ) {
		int special = CommonUtil.random(40);
		pc.getInventory().removeItem(useItem, 1);
		pc.getInventory().storeItem(L1ItemId.Inadril_T_ScrollA, 5);
		pc.getInventory().storeItem(L1ItemId.Inadril_T_ScrollB, 1);
		pc.getInventory().storeItem(L1ItemId.Inadril_T_ScrollC, 1);
		pc.getInventory().storeItem(L1ItemId.Inadril_T_ScrollD, 10);
		switch(special) {
		case 0:  pc.getInventory().storeItem(storeItem_S, 1); break;
		default:  pc.getInventory().storeItem(storeItem_N, 1); break;
		}
	}

	private void useMobEventSpownWand(L1PcInstance pc, L1ItemInstance item) {
		try {
			int[][] mobArray = {
					// 일반몹
					{ 45008, 45140, 45016, 45021, 45025, 45033, 45099, 45147,
						45123, 45130, 45046, 45092, 45138, 45098, 45127,
						45143, 45149, 45171, 45040, 45155, 45192, 45173,
						45213, 45079, 45144 },
						// 보스몹 10%
						{ 45488, 45456, 45473, 45497, 45464, 45545, 45529, 45516 },
						// 보스몹 7%
						{ 45601, 45573, 45583, 45609, 45955, 45956, 45957, 45958,
							45959, 45960, 45961, 45962, 45617, 45610, 45600,
							45614, 45618, 45649, 45680, 45654, 45674, 45625,
							45675, 45672 },
							// 보스몹 3%
							{ 45753, 45801, 45673 } };
			int category = 0;
			int rndcategory = CommonUtil.random(100) + 1;
			if (rndcategory <= 80)
				category = 0;
			else if (rndcategory <= 90)
				category = 1;
			else if (rndcategory <= 97)
				category = 2;
			else if (rndcategory <= 100)
				category = 3;
			int rnd = CommonUtil.random(mobArray[category].length);
			L1SpawnUtil.spawn(pc, mobArray[category][rnd], 0, 180000, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	} 


	private void 진주포션사용(L1PcInstance pc,int itemId) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // 디케이포션 상태
			pc.sendPackets(new S_ServerMessage(698)); 
			return;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_DRAGONPERL)) {
			pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.STATUS_DRAGONPERL);
			pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONPERL, 0 ,0)); 
			Broadcaster.broadcastPacket(pc, new S_DRAGONPERL(pc.getId(), 0)); 
			pc.sendPackets(new S_DRAGONPERL(pc.getId(), 0 ));
			pc.set진주속도(0);	 
		}
		pc.cancelAbsoluteBarrier();//앱솔해제(팩에 이 메소드없으면 무시)
		int time = 600 *1000;
		int stime = ((time/1000)/4)-2;
		pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_DRAGONPERL, time);
		pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONPERL, 8, stime)); 
		pc.sendPackets(new S_DRAGONPERL(pc.getId(), 8 ));
		Broadcaster.broadcastPacket(pc, new S_DRAGONPERL(pc.getId(), 8 ));
		pc.sendPackets(new S_SkillSound(pc.getId(),8031));//
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8031));
		pc.set진주속도(1);
		pc.sendPackets(new S_ServerMessage(1065));//새로추가
		pc.getInventory().consumeItem(itemId, 1);//새로추가
	}

	// 천상의 물약
	private void UseExpPotion(L1PcInstance pc , int item_id) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // 디케이포션 상태
			pc.sendPackets(new S_ServerMessage(698, "")); // 마력에 의해 아무것도 마실 수가 없습니다.
			return;
		}
		pc.cancelAbsoluteBarrier();
		int time = 0;
		if (item_id == L1ItemId.EXP_POTION
				|| item_id == L1ItemId.EXP_POTION2) { // 경험치 상승 물약
			time = 3600; // 20분
		}
		pc.getSkillEffectTimerSet().setSkillEffect(EXP_POTION, time * 1000);
		pc.sendPackets(new S_SkillSound(pc.getId(), 7013));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId() , 7013));
		pc.sendPackets(new S_ServerMessage(1313));
	}

	private void useCashScroll(L1PcInstance pc, int item_id) {
		int time = 3600;
		int scroll = 0;

		if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CASHSCROLL)){
			pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_CASHSCROLL);
			scroll = 6993;
			pc.addHpr(-4);
			pc.addMaxHp(-50);
			pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
			if (pc.isInParty()) { 
				pc.getParty().updateMiniHP(pc);
			}
			pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
		}

		if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CASHSCROLL2)){
			pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_CASHSCROLL2);
			pc.addMpr(-4);
			pc.addMaxMp(-40);
			pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CASHSCROLL3)){
			pc.getSkillEffectTimerSet().killSkillEffectTimer(STATUS_CASHSCROLL3);
			pc.addDmgup(-3);
			pc.addHitup(-3);
			pc.getAbility().addSp(-3);
			pc.sendPackets(new S_SPMR(pc));
		}


		if (item_id == L1ItemId.INCRESE_HP_SCROLL || item_id == L1ItemId.CHUNSANG_HP_SCROLL) {
			scroll = 6993;
			pc.addHpr(4);
			pc.addMaxHp(50);
			pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
			if (pc.isInParty()) { 
				pc.getParty().updateMiniHP(pc);
			}
			pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));

		} else if (item_id == L1ItemId.INCRESE_MP_SCROLL || item_id == L1ItemId.CHUNSANG_MP_SCROLL) {
			scroll = 6994;
			pc.addMpr(4);
			pc.addMaxMp(40);
			pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
		} else if (item_id == L1ItemId.INCRESE_ATTACK_SCROLL || item_id == L1ItemId.CHUNSANG_ATTACK_SCROLL) {
			scroll = 6995;
			pc.addDmgup(3);
			pc.addHitup(3);
			pc.getAbility().addSp(3);
			pc.sendPackets(new S_SPMR(pc));
		}
		pc.sendPackets(new S_SkillSound(pc.getId(), scroll));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), scroll));
		pc.getSkillEffectTimerSet().setSkillEffect(scroll, time * 1000);
	}


	private boolean createNewItem(L1PcInstance pc, int item_id, int count) {
		L1ItemInstance item = ItemTable.getInstance().createItem(item_id);
		item.setCount(count);
		if (item != null) {
			if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
				pc.getInventory().storeItem(item);
			} else { // 가질 수  없는 경우는 지면에 떨어뜨리는 처리의 캔슬은 하지 않는다(부정 방지)
				L1World.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(item);
			}
			//pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0를 손에 넣었습니다.
			return true;
		} else {
			return false;
		}
	}
	private boolean createNewItem2(L1PcInstance pc, int item_id, int count, int EnchantLevel) {
		L1ItemInstance item = ItemTable.getInstance().createItem(item_id);

		item.setCount(count);
		item.setEnchantLevel(EnchantLevel);
		item.setIdentified(true);
		if (item != null) {
			if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
				pc.getInventory().storeItem(item);
			} else { // 가질 수  없는 경우는 지면에 떨어뜨리는 처리의 캔슬은 하지 않는다(부정 방지)
				L1World.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(
						item);
			}
			//pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0를 손에 넣었습니다.
			return true;
		} else {
			return false;
		}
	}
	/** 오만의탑 11~91부적, 6~96부적 */
	private void useToiTeleportAmulet(L1PcInstance pc, int itemId, L1ItemInstance item) {
		boolean isTeleport = false;		
		/*
		if (itemId >= 5000200 && itemId <= 5000210 ||itemId >= 40289 && itemId <= 40297 ) { // 11,51Famulet
			if (pc.getX() >= 33923 && pc.getX() <= 33934 && pc.getY() >= 33340
					&& pc.getY() <= 33356 && pc.getMapId() == 4) { //아덴성당앞 동상앞
				isTeleport = true;
			}
		}*/
		// 세이프티존에서만 사용 가능
		if (UserCommands.StatusPc(pc)){ isTeleport = false; }
		if (pc.getMap().isSafetyZone(pc.getLocation()) && pc.getMapId() ==4) { isTeleport = true; }	
		if (isTeleport) {
			L1Teleport.teleport(pc, item.getItem().get_locx(), item.getItem()
					.get_locy(), item.getItem().get_mapid(), 5, true);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); 
		}
	}


	private boolean withdrawPet(L1PcInstance pc, int itemObjectId) {
		if (!pc.getMap().isTakePets()) {
			pc.sendPackets(new S_ServerMessage(563)); // \f1 여기에서는 사용할 수 없습니다.
			return false;
		}

		int petCost = 0;
		Object[] petList = pc.getPetList().values().toArray();
		for (Object pet : petList) {
			if (pet instanceof L1PetInstance) {
				if (((L1PetInstance) pet).getItemObjId() == itemObjectId) { // 이미 꺼내고 있는 애완동물
					return false;
				}
			}
			petCost += ((L1NpcInstance) pet).getPetcost();
		}
		int charisma = pc.getAbility().getTotalCha();
		if (pc.isCrown()) { // CROWN
			charisma += 6;
		} else if (pc.isElf()) { // ELF
			charisma += 12;
		} else if (pc.isWizard()) { // WIZ
			charisma += 6;
		} else if (pc.isDarkelf()) { // DE
			charisma += 6;
		} else if (pc.isDragonknight()) { // 용기사
			charisma += 6;
		} else if (pc.isIllusionist()) { // 환술사
			charisma += 6;
		}

		charisma -= petCost;
		int petCount = charisma / 6;
		if (petCount <= 0) {
			pc.sendPackets(new S_ServerMessage(489)); // 물러가려고 하는 애완동물이 너무 많습니다.
			return false;
		}

		L1Pet l1pet = PetTable.getInstance().getTemplate(itemObjectId);
		if (l1pet != null) {
			L1Npc npcTemp = NpcTable.getInstance().getTemplate(l1pet.get_npcid());
			L1PetInstance pet = new L1PetInstance(npcTemp, pc, l1pet);
			pet.setPetcost(6);
			pet.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_PET_FOOD, pet.getFoodTime()*1000);
		}
		return true;
	}

	private void IdentMapWand(L1PcInstance pc, int locX, int locY){
		pc.sendPackets(new S_SystemMessage("Gab :" + pc.getMap().getOriginalTile(locX, locY) + ",x :" + locX + ",y :"
				+ locY + ", mapId :" + pc.getMapId()));
		if (pc.getMap().isCloseZone(locX, locY)){
			pc.sendPackets(new S_EffectLocation(locX, locY, 10));
			Broadcaster.broadcastPacket(pc , new S_EffectLocation(locX, locY, 10));
			pc.sendPackets(new S_SystemMessage("벽으로 인식중"));
		}
	}

	private void MapFixKeyWand(L1PcInstance pc, int locX, int locY){
		String key = new StringBuilder().append(pc.getMapId()).append(locX).append(locY).toString();
		if (!pc.getMap().isCloseZone(locX, locY)){
			if (!MapFixKeyTable.getInstance().isLockey(key)){
				MapFixKeyTable.getInstance().storeLocFix(locX, locY, pc.getMapId());
				pc.sendPackets(new S_EffectLocation(locX, locY, 1815));
				Broadcaster.broadcastPacket(pc , new S_EffectLocation(locX, locY, 1815));
				pc.sendPackets(new S_SystemMessage("key추가 ,x :" + locX + ",y :" + locY + ", mapId :" + pc.getMapId()));
			}
		}else{
			pc.sendPackets(new S_SystemMessage("선택좌표는 벽이 아닙니다."));

			if (MapFixKeyTable.getInstance().isLockey(key)){
				MapFixKeyTable.getInstance().deleteLocFix(locX, locY, pc.getMapId());
				pc.sendPackets(new S_EffectLocation(locX, locY, 10));
				Broadcaster.broadcastPacket(pc , new S_EffectLocation(locX, locY, 10));
				pc.sendPackets(new S_SystemMessage("key삭제 ,x :" + locX + ",y :" + locY + ", mapId :" + pc.getMapId()));
			}
		}        
	}
	private void doItPoly(L1PcInstance pc, int polyid, L1ItemInstance useItem){
		pc.getInventory().removeItem(useItem , 1);			         
		L1PolyMorph.doPoly(pc, polyid, 1200, L1PolyMorph.MORPH_BY_ITEMMAGIC);
	}
	@Override
	public String getType() {
		return C_ITEM_USE;
	}
}