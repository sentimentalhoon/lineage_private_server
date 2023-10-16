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

		if (useItem.getItem().getUseType() == -1) { // none:����� �� ���� ������
			pc.sendPackets(new S_ServerMessage(74, useItem.getLogName())); // \f1%0�� ����� �� �����ϴ�.	
			return;
		}
		if (pc.isTeleport()) { // �ڷ���Ʈ ó����
			return;
		}
		//������� ���� �߰�
		L1PcInstance jonje = L1World.getInstance().getPlayer(pc.getName());
		if (jonje == null && pc.getAccessLevel() != 200) {
			client.kick();
			return;
		} 

		if (useItem == null && pc.isDead() == true) {
			return;
		}
		if (!pc.getMap().isUsableItem()) {
			pc.sendPackets(new S_ServerMessage(563)); // \f1 ���⿡���� ����� �� �����ϴ�.
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
		if (itemId == 41029 // ��ȯ���� ����
				|| itemId == 40317 || itemId == 41036
				|| itemId == L1ItemId.LOWER_OSIRIS_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.HIGHER_OSIRIS_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.LOWER_TIKAL_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.HIGHER_TIKAL_PRESENT_PIECE_DOWN
				|| itemId == L1ItemId.TIMECRACK_CORE 
				|| itemId == L1ItemId.PROTECTION_SCROLL 
				|| itemId == L1ItemId.MAGIC_BREATH 
				|| itemId == 40964 || itemId == 41030
				|| itemId == 40925 || itemId == 40926 || itemId == 40927 // ��ȭ���ź����� �Ϻ�
				|| itemId == 40928 || itemId == 40929 || itemId == 500231
				|| itemId == 4500162 || itemId == 447012 || itemId == 40076) { // 40076 ������ֹ���
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
			if (useItem.getItem().getType2() == 0) { // �������� ���� ������
				delay_id = ((L1EtcItem) useItem.getItem()).get_delayid();
			}
			if (delay_id != 0) { // ���� ���� �־�
				if (pc.hasItemDelay(delay_id) == true) {
					return;
				}
			}

			L1ItemInstance l1iteminstance1 = pc.getInventory().getItem(l);
			_log.finest("request item use (obj) = " + itemObjid + " action = " + l);
			if (useItem.getItem().getType2() == 0) { // �������� ���� ������
				int item_minlvl = ((L1EtcItem) useItem.getItem()).getMinLevel();
				int item_maxlvl = ((L1EtcItem) useItem.getItem()).getMaxLevel();

				boolean isDelayEffect = false;
				int delayEffect = ((L1EtcItem) useItem.getItem()).get_delayEffect();

				if (item_minlvl != 0 && item_minlvl > pc.getLevel() && !pc.isGm()) {
					pc.sendPackets(new S_ServerMessage(318, String.valueOf(item_minlvl))); 
					// �� ��������%0���� �̻��� ���� ������ ����� �� �����ϴ�.
					return;
				} else if (item_maxlvl != 0 && item_maxlvl < pc.getLevel() && !pc.isGm()) {
					pc.sendPackets(new S_ServerMessage(673, String.valueOf(item_maxlvl)));
					// �� ��������%d���� �̻� ����� �� �ֽ��ϴ�.
					return;
				}
				if ((itemId == 40576 && !pc.isElf()) 
						|| (itemId == 40577 && !pc.isWizard()) // ��ȥ�� ������ ����(��)
						|| (itemId == 40578 && !pc.isKnight())) { // ��ȥ�� ������ ����(����)
					pc.sendPackets(new S_ServerMessage(264)); // \f1����� Ŭ���������� �� �������� ����� �� �����ϴ�.
					return;
				}

				switch(itemId){
				case 7059: pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.GMSTATUS_HPBAR, 5 * 1000); removeItem(pc,useItem); break;// ����� ��?
				case 7064: pc.sendPackets(new S_NoticBoard(pc, 1)); break; // ��Ź �Ǹ� �Խ���	
				case 7065: pc.sendPackets(new S_NoticBoard(pc, 2)); break; // ���� �Խ���
				case 7066: pc.sendPackets(new S_NoticBoard(pc, 3)); break; // �����Խ���
				case 7067: pc.sendPackets(new S_NoticBoard(pc, 4));  break; // ���� �Խ���
				case 7068: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan1, Config.NewClanName1); break; // �ű� ���� ���� �ֹ���
				case 7069: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan2, Config.NewClanName2); break; // �ű� ���� ���� �ֹ���
				case 7070: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan3, Config.NewClanName3); break; // �ű� ���� ���� �ֹ���
				case 7071: newclan(pc, useItem, Config.NEWUSERSAFETY_LEVEL, Config.NewClan4, Config.NewClanName4); break; // �ű� ���� ���� �ֹ���
				case 7072: Ress(pc, itemId); break;// ��Ƽ���� ��ȣ, ��Ȱ
				case 40003: flashlight_Gas(pc,useItem); break;
				case L1ItemId.INCRESE_HP_SCROLL  : case L1ItemId.INCRESE_MP_SCROLL  : 
				case L1ItemId.INCRESE_ATTACK_SCROLL  : case L1ItemId.CHUNSANG_HP_SCROLL  : 
				case L1ItemId.CHUNSANG_MP_SCROLL  : case L1ItemId.CHUNSANG_ATTACK_SCROLL  : 
					useCashScroll(pc, itemId); pc.getInventory().removeItem(useItem, 1);break;
				case 40858: liquor(pc); removeItem(pc,useItem); break; //��
				case L1ItemId.EXP_POTION : 	
				case L1ItemId.EXP_POTION2 :  
					UseExpPotion(pc, itemId); 
					removeItem(pc,useItem);break;
				case 7062 : exp_Posion(pc);break;//����ġ����
				case 7063 : exp_PosionConsumable(pc);break;
				case 467009 : pc.sendPackets(new S_SystemMessage("\\fT[.ĳ������] [�����Ҿ��̵�] ���ֽø� �˴ϴ�. ĳ���� ����� ��üâ�� ���� �˴ϴ�.")); break; //�ɸ������ֹ���
				case 4500155: byBloodPledgeJoin(pc,useItem); break; //�ڱ����Ͱ����ϱ�
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
								pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 �� ������ ����� �� �����ϴ�.
								return;
							}
						}
					}   
					pc.cancelAbsoluteBarrier(); // �ƺ�Ҹ�Ʈ�ٸ����� ����
					int skillid = 0;
					if (itemId == 430106) { // ������ ����
						skillid = 7671;
					} else if (itemId == 430104) { // ������ ����	
						skillid = 7672;
					} else if (itemId == 430107) { // ȭ���� ����	
						skillid = 7673;
					} else if (itemId == 430105) { // ǳ���� ����
						skillid = 7674;
					} else if (itemId == 430108) { // ź���� ����
						skillid = 7675;
					} else if (itemId == 430109) { // ������ ����
						skillid = 7676;
					} else if (itemId == 430110) { // ������ ����
						skillid = 7677;
					}	
					pc.setBuffnoch(1); // ��ų���׶��� �߰� �ù����� ���۵�
					L1SkillUse l1skilluse = new L1SkillUse();
					l1skilluse.handleCommands(client.getActiveChar(), skillid,
							spellsc_objid, spellsc_x, spellsc_y, null, 0, L1SkillUse.TYPE_SPELLSC);  
					pc.setBuffnoch(0); // ��ų���׶��� �߰� �ù����� ���۵�
					//// ���� 7�� �߰� ////

					if (isDelayEffect) {
						Timestamp ts = new Timestamp(System.currentTimeMillis());
						useItem.setLastUsed(ts);
						pc.getInventory().updateItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
						pc.getInventory().saveItem(useItem, L1PcInventory.COL_DELAY_EFFECT);
					}
					break;
				case 5000085 : eventT(pc , useItem, 490000, 490009 );break; // ����Ƽ���� 
				case 5000086 : eventT(pc , useItem, 490001, 490010 );break; // ��ø��Ƽ���� 
				case 5000087 : eventT(pc , useItem, 490002, 490011 );break; // �ŷ���Ƽ���� 
				case 5000088 : eventT(pc , useItem, 490003, 490012 );break; // ������Ƽ���� 
				case 5000089 : eventT(pc , useItem, 490004, 490013 );break; // ü����Ƽ���� 
				case 5000090 : eventT(pc , useItem, 490005, 490014 );break; // ������Ƽ���� 
				case 5000091 : eventT(pc , useItem, 490006, 490015 );break; // ���ϳ�����Ƽ���� 
				case 5000092 : eventT(pc , useItem, 490007, 490016 );break; // Ȧ�峻����Ƽ���� 
				case 5000093 : eventT(pc , useItem, 490008, 490017 );break; // ����������Ƽ����
				case 5000094 : item_change(pc, useItem,5000098);break; //������������� 
				case L1ItemId.POTION_OF_CURE_POISON : case 40507:cure_Posion(pc, itemId , useItem);	break;	//�ص���
				case 40066 : case 41413: MpPosion(pc,useItem,7,6); break;//������
				case 40067 : case 41414: case 140067 : MpPosion(pc,useItem,15,16); break;//������
				case 40735 : MpPosion(pc,useItem,60,0); break;//������
				case 40042 : MpPosion(pc,useItem,50,0); break;//������
				case 41404 : MpPosion(pc,useItem,83,14); break;//������
				case 41412 : MpPosion(pc,useItem,5,16); break;//������
				case 500231 : dollchange(pc,useItem,l1iteminstance1); break;//����ü����
				case 500034 : girandungeonTime(pc, useItem);	break;//�Ⱘ����
				case 500035 : girandungeonTime2(pc, useItem); break;//�Ⱘ����
				case 5000178 : ivoryTowerTime(pc, useItem); break;//���ž����
				case 5000179 : ldungeonTime(pc, useItem); break;//�������	
				case 5000148: portableStorage(pc,useItem); break;//�޴��â��
				case 5000161: cerenisCallStick(pc); break;//ȭ���ɷ��Ͻ���ȯ����
				case 400074: CreateItemNoDelete(pc, 41159, 220, 400075, 1); break; // ���� 200 ����
				case 400075: CreateItemNoDelete(pc, 400075, 1, 41159, 200); break;  // ���� 200 ��ǥ
				case 400076 : adenaBankbook(pc); break;//�Ƶ�������
				case 400077 : adenaCheck(pc); break;//�Ƶ�����ǥ
				case 3500047:
					if (pc.getInventory(). checkItem(40308, 50000000)){  // 
						pc.getInventory().consumeItem(40308, 50000000); //
						pc.getInventory().storeItem(3500048, 1); //��ǥ�� 1�� ����
						pc.getInventory().consumeItem(3500047, 1); //��ǥ 1�� �Ҹ�
						pc.sendPackets(new S_SystemMessage("�Ƶ� 5õ���� ��ǥ�� ��ȯ�Ǿ����ϴ�. ")); //������ ��Ʈ����
					} else {
						pc.sendPackets(new S_SystemMessage("�Ƶ��� 5õ���� �����ϰ�  �־���մϴ�. ")); //���н� ��Ʈ����
					}
					break;
				case 3500048:
					pc.getInventory().storeItem(40308, 50000000); //5õ���Ƶ� ����
					pc.getInventory().consumeItem(3500048, 1); //��ǥ 1�� �Ҹ�
					pc.sendPackets(new S_SystemMessage("�Ƶ� 50,000,000������ ��ȯ�Ǿ����ϴ�. "));
					break;
				case 3500049:
					if (pc.getInventory(). checkItem(40308, 10000000)){  // 
						pc.getInventory().consumeItem(40308, 10000000); //
						pc.getInventory().storeItem(3500050, 1); //��ǥ�� 1�� ����
						pc.sendPackets(new S_SystemMessage("�Ƶ� 1õ���� ��ǥ�� ��ȯ�Ǿ����ϴ�. ")); //������ ��Ʈ����
					} else {
						pc.sendPackets(new S_SystemMessage("�Ƶ��� 1õ���� �����ϰ�  �־���մϴ�. ")); //���н� ��Ʈ����
					}
					break;
				case 3500050:
					pc.getInventory().storeItem(40308, 10000000); //1õ���Ƶ� ����
					pc.getInventory().consumeItem(3500050, 1); //��ǥ 1�� �Ҹ�
					pc.sendPackets(new S_SystemMessage("�Ƶ� 10,000,000������ ��ȯ�Ǿ����ϴ�. "));
					break;
				case 447011 : newUseSupportBox(pc); break; // �ʺ� ��������
				case 447013 : newUseSupportBox2(pc); break; // �ʺ� ��������
				case 5000149: FirstPkg(pc); break;// 1�� ��Ű�� 2���� 5000150
				case 5000156: SecondPkg(pc); break;// 2����Ű��
				case 5000216: pc.sendPackets(new S_SystemMessage("[.�����]�� �Է��Ͽ��ּ���")); break;
				case 7060:
					if (pc.getKarma() <= 10000000) {
						pc.setKarma(pc.getKarma() + 100000);
						pc.sendPackets(new S_SystemMessage(pc.getName() + "���� ��ȣ���� ���Ǿ����ϴ�."));
						pc.getInventory().removeItem(useItem, 1);
					} else pc.sendPackets(new S_SystemMessage("8�ܰ� ���Ͽ����� ����� �� �ֽ��ϴ�."));     
					break;
				case 7061:
					if (pc.getKarma() >= -10000000) {
						pc.setKarma(pc.getKarma() - 100000);
						pc.sendPackets(new S_SystemMessage(pc.getName() + "���� ��ȣ���� ���Ǿ����ϴ�."));
						pc.getInventory().removeItem(useItem, 1);
					} else pc.sendPackets(new S_SystemMessage("8�ܰ� ���Ͽ����� ����� �� �ֽ��ϴ�.")); 
					break;
				case 500056 : item_Create(pc,useItem,500066,itemId,76776);break;//�ٰŸ���
				case 500057 : item_Create(pc,useItem,500066,itemId,76775);break;//���Ÿ���
				case 500058 : item_Create(pc,useItem,500066,itemId,76774);break;//�������ݷ�
				case 500059 : item_Create(pc,useItem,500066,itemId,76773);break;//�ٰŸ����߷�
				case 500060 : item_Create(pc,useItem,500066,itemId,76772);break;//���Ÿ����߷�
				case 500061 : item_Create(pc,useItem,500066,itemId,76771);break;//�������߷� 
				case 500062 : item_Create(pc,useItem,500066,itemId,76770);break;//���� ����
				case 500063 : item_Create(pc,useItem,500066,itemId,76769);break;//���� ����
				case 500064 : item_Create(pc,useItem,500066,itemId,76768);break;//���� ����
				case 500065 : item_Create(pc,useItem,500066,itemId,76767);break;//�ٶ��� ����		
				case 500080 : item_Create(pc,useItem,500066,itemId,76777);break;//���� ���������� (�����)
				case 500081 : item_Create(pc,useItem,500066,itemId,76778);break;//���� ���������� (����)
				case 500082 : item_Create(pc,useItem,500066,itemId,76779);break;//�ն�������ũ��Ȱ (�����)
				case 500083 : item_Create(pc,useItem,500066,itemId,76780);break;//�ն�������ũ��Ȱ (����)
				case 500084 : item_Create(pc,useItem,500066,itemId,76781);break;//����� ������ ������ (�����)
				case 500085 : item_Create(pc,useItem,500066,itemId,76782);break;//����� ������ ������ (����)
				case 500086 : item_Create(pc,useItem,500066,itemId,76783);break;//���������� ���� ����
				case 500087 : item_Create(pc,useItem,500066,itemId,76784);break;//��ť������ �Ŵ� ���� ����
				case 4500170: doItPoly(pc, 8817, useItem); break; // �˶���� ����
				case 4500171: doItPoly(pc, 9003, useItem); break; // ���캯��
				case 4500172: doItPoly(pc, 9206, useItem); break; // 80 ���� ����
				case 4500173: doItPoly(pc, 9226, useItem); break; // 80 ��ũ���� ����
				case 42198 : KillAndDeathReset(pc,useItem);break;//ų�����ʱ�ȭ�ֹ���
				case L1ItemId.PROTECTION_SCROLL:
					if (l1iteminstance1.getItem().getType2() != 0 && l1iteminstance1.getProtection() == 0){
						l1iteminstance1.setProtection(1);
						pc.getInventory().removeItem(useItem, 1);
						pc.sendPackets(new S_SystemMessage(l1iteminstance1.getLogName()+"�� ������ ����� ���������ϴ�."));
					} else {
						pc.sendPackets(new S_ServerMessage(79));
					}
					pc.getInventory().updateItem(l1iteminstance1, L1PcInventory.COL_ENCHANTLVL);
					pc.getInventory().saveItem(l1iteminstance1, L1PcInventory.COL_ENCHANTLVL);
					break;
				case 560032: // ������� ���� �ֹ���
					if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_BARLOG)) {
						pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, 
								"\\fR�߷��� ������ �����ֽ��ϴ�."));
					} else {
						pc.getSkillEffectTimerSet().setSkillEffect(STATUS_CURSE_YAHEE, 1020 * 1000);				
						pc.sendPackets(new S_PacketBox(S_PacketBox.ICON_AURA, 2, 1020));
						pc.sendPackets(new S_SkillSound(pc.getId(), 750));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 750));	
						pc.sendPackets(new S_ServerMessage(1127));
					}
					break;
				case 560033: // �߷� ��� ���� �ֹ���
					if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_CURSE_YAHEE)) {
						pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, 
								"\\fR������ ������ �����ֽ��ϴ�."));
					} else {
						pc.getSkillEffectTimerSet().setSkillEffect(STATUS_CURSE_BARLOG, 1020 * 1000);	//1020					
						pc.sendPackets(new S_PacketBox(S_PacketBox.ICON_AURA, 1, 1020));
						pc.sendPackets(new S_SkillSound(pc.getId(), 750));
						Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 750));	
						pc.sendPackets(new S_ServerMessage(1127));
					}
					break;
				case 40317 : case 40508 : repairItem(pc, l1iteminstance1,useItem);break;// ����  // �����Ϸ���
				case L1ItemId.LOWER_OSIRIS_PRESENT_PIECE_DOWN : case L1ItemId.HIGHER_OSIRIS_PRESENT_PIECE_DOWN: 
					close_Lower_Oriris_present(pc,l1iteminstance1, useItem, itemId); break; // ���Ǹ��ƺ��� (��)(��)
				case L1ItemId.LOWER_TIKAL_PRESENT_PIECE_DOWN  : case L1ItemId.HIGHER_TIKAL_PRESENT_PIECE_DOWN :
					close_Lower_Tikal_Present(pc,l1iteminstance1, useItem,itemId); break;//����ĭ��������(��)(��)
				case L1ItemId.ANCIENT_ROYALSEAL : ancient_RoyalSeal(pc,useItem,client);break; 	//�°��ǿ���
				case L1ItemId.TIMECRACK_CORE : timeCrack_Core(pc,l1iteminstance1,useItem);break;// �տ��� ��
				//case 40076: ago_Scroll(pc,l1iteminstance1,useItem);break;// ������ֹ���
				case 40097 : case  40119 : case 140119 : case 140329 : 
					curseClear(pc,useItem,itemId);break;	// ���ֽ�ũ��, ���ֹ��� ����
				case 41036 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,41038,41047,10);break; // Ǯ
				case 40964 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,41011,41018,8);break; //�渶������
				case 40925 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,40987,40989,186);break; //��ȭ�ǹ���
				case 40926 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41173,41175,12);break;//�ź��ѹ���1�ܰ�
				case 40927 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41176,41178,12);break;//�ź��ѹ���2�ܰ�
				case 40928 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41179,41181,12);break;//�ź��ѹ���3�ܰ�
				case 40929 : create_Chance_Item_Safe(pc,l1iteminstance1,useItem ,41182,41184,12);break;//�ź��ѹ���4�ܰ�
				case 41029 : create_Chance_Item_Delete(pc,l1iteminstance1,useItem,41030,41034,1);break;  // ��ȯ�� ����
				case 40859 : case 40860 : case 40861 : case 40862 : case 40864 : case 40865 : case 40866 : 
				case 40867 : case 40868 : case 40869 : case 40870 : case 40871 : case 40872 : case 40873 : 
				case 40874 : case 40875 : case 40876 : case 40877 : case 40878 : case 40879 : case 40880 : 
				case 40881 : case 40882 : case 40883 : case 40884 : case 40885 : case 40886 : case 40887 :
				case 40888 : case 40889 : case 40890 : case 40891 : case 40892 : case 40893 : case 40894 :
				case 40895 : case 40896 : case 40897 : case 40898 :
					magicScroll(pc, useItem , client, itemId, spellsc_objid, spellsc_x, spellsc_y );break; //�����ֹ��� 
				case 41921: case 41922: case 41923: case 41924: case 41925: case 41926: case 41927: case 41928:
				case 41929: case 41930: case 41931: case 41932: case 41933: case 41934: case 41935: case 41936:
				case 41937: case 41938: case 41939: case 41940: case 41941: case 41942: case 41943: case 41944:
				case 41945: case 41946: case 41947: case 41948: case 41949: case 41950: case 41951: case 41952:
				case 41953: case 41954: case 41955: case 41956: case 41957: case 41958: case 41959: case 41960:
					magicScroll2(pc, useItem , client, itemId, spellsc_objid, spellsc_x, spellsc_y );break; //7~10�ܰ踶���ֹ��� 
				case 5000219: // ��� ���� ī���� �踮�� �ֹ���
					if (pc.isKnight()) {
						int[] allBuffSkill = { 91 };
						L1SkillUse l1skilluse5000219 = new L1SkillUse();
						for (int i = 0; i < allBuffSkill.length; i++) {
							l1skilluse5000219.handleCommands(pc,allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_SPELLSC);
						}
						pc.getInventory().removeItem(useItem, 1);
					} else {
						pc.sendPackets(new S_ServerMessage(264)); // ����� Ŭ������ �� �������� ����� �� �����ϴ�.
					}
					break;
				case 5000212 : { int[] buffSkill  = {1025}; // �ڸ� ���� �ֹ���
				allBuffPotion(pc,useItem,buffSkill);break; }
				case 3500043 : { int[] buffSkill  = {26, 42, 54, 48, 79, 88,148, 151, 158};
				allBuffPotion(pc,useItem,buffSkill);break; } // �Ϲ� ���� ����
				case 3500044 : { int[] buffSkill  = {26, 42, 43, 48, 54, 79, 88, 168, 160, 206, 211, 216, 117, 166};
				allBuffPotion(pc,useItem,buffSkill);break; } // �Ϲ� ���� ����
				case 437031: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_A}; // ���й��� ���
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 437032: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_B}; // ���й��� �߹�
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 437033: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_C}; // ���й��� �ҹ�
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 437034: {
					int[] buffSkill  = {L1SkillId.FEATHER_BUFF_D}; // ���й��� �ʹ�
					allBuffPotion(pc,useItem,buffSkill);break; }
				case 40314 : case 40316 :  pet_Necklace(pc, itemObjid);break;	 // ���� �ƹ·�Ʈ
				case 40315 : pet_Whistle(pc);break;	 // �� ȣ����
				case 40493 : magicFlute(pc, useItem);break;	 // ���� �÷�
				case 40325 : magicDice(pc , 2);break;	//2�ܰ��ֻ���
				case 40326 : magicDice(pc , 3);break;	//3�ܰ��ֻ���
				case 40327 : magicDice(pc , 4);break;	//4�ܰ��ֻ���
				case 40328 : magicDice(pc , 6);break;	//6�ܰ��ֻ���
				case L1ItemId.CHARACTER_REPAIR_SCROLL : chatacter_Repair_Scroll(pc,useItem ,client);break;	//�ɸ��ͺ����ֹ���
				case 40903 : case 40904 : case 40905 : case 40906 : case 40907 : case 40908 : weddingRing( pc, useItem);break;// ���� ��ȥ ����
				case 40555 : secretRoom_Key(pc);break; // ����� ���� Ű
				case 40417 : spirit_Crystal(pc);break;// �����ǰ���
				case 40566 : misterioso_Shell(pc);break; // �ź����� ��
				case 40557 : hit_List( pc, 32620, 32641 , 45883);break; //�����(�ٿ�����Ʈ������)
				case 40563 : hit_List( pc, 32730, 32426 , 45884);break; //�����(�ٿ�����Ʈ������)
				case 40561 : hit_List( pc, 33046, 32806 , 45885);break; //�����(�ٿ�����Ʈ������) 
				case 40560 : hit_List( pc, 32580, 33260 , 45886);break; //�����(�ٿ�����Ʈ������)
				case 40562 : hit_List( pc, 33447, 33476 , 45887);break; //�����(�ٿ�����Ʈ������)
				case 40559 : hit_List( pc, 34215, 33195 , 45888);break; //�����(�ٿ�����Ʈ������)
				case 40558 : hit_List( pc, 33513, 32890 , 45889);break; //�����(�ٿ�����Ʈ������)
				case 40572 : assassinToken(pc);break; //��ؽ�����ǥ
				case 40009 : expulsionStick(pc, useItem , spellsc_objid, spellsc_x, spellsc_y );break;	// �߹渷��
				case L1ItemId.ICECAVE_KEY :	iceCaveKey(pc,useItem ,spellsc_objid);break; //�������ǿ���
				case 40289 : case 40290 : case 40291 : case 40292 : case 40293 : case 40294 : case 40295 : 
				case 40296 : case 40297 : case 5000200 : case 5000201 : case 5000202 : case 5000203 : case 5000204 : 
				case 5000205 : case 5000206 : case 5000207 : case 5000208 : case 5000209 : case 5000210 : 
					useToiTeleportAmulet(pc, itemId, useItem); 	break;//������ž������� 
				case 5000101 :case 5000102 :case 5000103 :case 5000104 :case 5000105 :
				case 5000106 :case 5000107 :case 5000108 :case 5000109 :  
					arroganceTowerAmulet_change(pc, useItem,(itemId+20),(itemId-4959812) , 2); break; //���̵� ������ž 11~91������
				case 5000111 :case 5000112 :case 5000113 :case 5000114 :case 5000115 :
				case 5000116 :case 5000117 :case 5000118 :case 5000119 :	
					arroganceTowerAmulet_change(pc, useItem,(itemId+91), (itemId-4959822) , 5); break;//ȥ���� ������ž  �̵� ���� (11~91)
				case 40280 : case 40281 : case 40282 : case 40283 : case 40284 : case 40285 : case 40286 : case 40287 : case 40288 : 
					sealArroganceTowerAmulet(pc,useItem,itemId);break; //���εȿ�����ž���� 
				case 40070 : 
					pc.sendPackets(new S_ServerMessage(76, useItem.getLogName()));
					pc.getInventory().removeItem(useItem, 1); 
					break; //��ȭ�ǿ���
				case 41301 : shinefish( pc,useItem, 40053, 40049, 40045, 40019)	;break;	// ���̴׷����ͽ�
				case 41302 : shinefish( pc,useItem, 40055, 40051, 40047, 40018)	;break;	// ���̴ױ׸��ͽ�
				case 41303 : shinefish( pc,useItem, 40054, 40050, 40046, 40015)	;break;	// ���̴׺긣�ͽ�
				case 41304 : shinefish( pc,useItem, 40052, 40048, 40044, 40021)	;break;	// ���̴�ȭ��Ʈ�ͽ�
				case 40615 : case 40616 : case 40782 : case 40783 : 
					shadowTempleKey(pc,useItem);break; // �׸����� ����2����3������ 
				case 437011 : case 1437011 : case 435000 : �������ǻ��(pc,itemId);break;  // ������, �ÿ� ������ ����
				case 40692 : finishedTreasureMap(pc,useItem);break; // �ϼ��� ������ ����
				case 41146 	: pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei001"));break; //��θ�����ʴ���
				case 560025 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook0"));break;	
				case 560027 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook2"));break;
				case 560026 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook3"));break;
				case 560028 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook4"));break;
				case 560029 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "telBook5"));break;
				case 41209 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei002"));break;// ���Ƿ����� �Ƿڼ�
				case 41210 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei003"));break;// ������
				case 41211 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei004"));break;// ���
				case 41212 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei005"));break;// Ư�� ĵ��
				case 41213 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei006"));break;// Ƽ���� �ٽ���
				case 41214 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei0012"));break;// ���� ����
				case 41215 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei0010"));break;//���� ����
				case 41216 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei0011"));break;//���� ����
				case 41222 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei008"));break;//������
				case 41223 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei007"));break;//������ ����
				case 41224 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei009"));break;//���� ����
				case 41225 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei013"));break;//�ɽ�Ų�� ���ּ�
				case 41226 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei014"));break;//�İ��� ��
				case 41227 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei033"));break;//�˷����� �Ұ���
				case 41228 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei034"));break;//�����ڻ��� ����
				case 41229 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei025"));break;//���̸����� �Ӹ�
				case 41230 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei020"));break;//�������� ����
				case 41231 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei021"));break;//��Ƽ���� ����
				case 41233 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei019"));break;//�����̿��� ����
				case 41234 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei023"));break;// ���� ���� ����
				case 41235 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei024"));break;// ���ǥ
				case 41236 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei026"));break;//����í�� ��
				case 41237 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei027"));break;//���̸��� ������ũ�� ��
				case 41239 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei018"));break;//��Ʈ���� ����
				case 41240 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei022"));break;//��ٿ��� ����
				case 41060 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "nonames"));break;//�볪���� ��õ��
				case 41061 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "kames"));break;//������� ������������ ���� �δٸ���ī��
				case 41062 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "bakumos"));break;// ������� �������ΰ� ���� �׸�����ũ��
				case 41063 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "bukas"));break;// ������� ���������� ���� �δٸ����ī
				case 41064 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "huwoomos"));break;// ������� ��������ũ ���� �׸����Ŀ��
				case 41065 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "noas"));break;// ������� ������������� ��Ʈ�ٳ��
				case 41356 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "rparum3"));break;// �ķ��� �ڿ� ����Ʈ
				case 40701 : smallTreasureMap(pc);break;//������������
				case 40663 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "sonsletter"));break;//�Ƶ��� ����
				case 40630 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "diegodiary"));break;//�𿡰��� ���� �ϱ�
				case 41340 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "tion"));break;//  �뺴���� Ƽ��
				case 41317 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "rarson"));break;//  ������ ��õ��
				case 41318 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "kuen"));break;//���� �޸�
				case 41329 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "anirequest"));break;//������ ���� �Ƿڼ�
				case 41346 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "robinscroll"));break;// �κ��ʵ��� �޸� 1
				case 41347 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "robinscrol2"));break;// �κ��ʵ��� �޸� 2
				case 41348 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "robinhood"));break;//�κ��ʵ��� �Ұ���
				case 41007 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "erisscroll"));break;//�̸����� ��ɼ�����ȥ�� �Ƚ�
				case 41009 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "erisscroll2"));break;//�̸����� ��ɼ��������� ����
				case 41019 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory1"));break;//��Ÿ�ٵ��ǿ��缭1��
				case 41020 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory2"));break;//��Ÿ�ٵ��ǿ��缭2��
				case 41021 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory3"));break;//��Ÿ�ٵ��ǿ��缭3��
				case 41022 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory4"));break;//��Ÿ�ٵ��ǿ��缭4��
				case 41023 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory5"));break;//��Ÿ�ٵ��ǿ��缭5��
				case 41024 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory6"));break;//��Ÿ�ٵ��ǿ��缭6��
				case 41025 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory7"));break;//��Ÿ�ٵ��ǿ��缭7��
				case 41026 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "lashistory8"));break;//��Ÿ�ٵ��ǿ��缭8��
				case 210087 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "first_p"));break;//�������� ù ��° ���ɼ�
				case 210093 : pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "silrein1lt"));break;//�Ƿ����� ù ��° ����
				case L1ItemId.TIKAL_CALENDAR : tcalendar_Open_And_Close(pc);break;//ƼĮ�޷�(���µ��մ���Ȯ��)
				case 41208 : overSpirit(pc ,useItem);break;//������ ��ȥ		
				case 40700 : silverPlute(pc);break;//�ǹ��÷�						
				case 41121 : cahellContract(pc);break;//ī���ǰ�༭
				case 41130 : bloodstainContract(pc);break;//�����ǰ�༭
				case 42501 : StormWalk(pc,spellsc_x,spellsc_y );break;//�����ũ
				case L1ItemId.CHANGING_PETNAME_SCROLL:changing_Petname_scroll(pc,useItem,l1iteminstance1);break; //���̸������ֹ��� 
				case 41260 : firewood(pc,useItem);break;//����
				case 41345 : L1DamagePoison.doInfection(pc, pc, 3000, 5);pc.getInventory().removeItem(useItem, 1);break;//�꼺������
				case 41315 :holyWater(pc,useItem,1141 ,STATUS_HOLY_WATER, STATUS_HOLY_WATER_OF_EVA,STATUS_HOLY_MITHRIL_POWDER);break;  // ���� 
				case 41316 :holyWater(pc,useItem,1142 ,STATUS_HOLY_MITHRIL_POWDER,STATUS_HOLY_WATER_OF_EVA,STATUS_HOLY_WATER );break;// �ż��� �̽������Ŀ��
				case 41354 :holyWater(pc,useItem);break;// �ż��� ������ ��
				case L1ItemId.CHANGING_SEX_POTION : changing_Sex_potion(pc,useItem);break;
				case L1ItemId.DRAGON_EMERALD_BOX : EmeraldBox(pc,useItem,itemId,"$11518" );break;//�巡����̸����� 
				case L1ItemId.DRAGON_JEWEL_BOX : dragonBox(pc,useItem,itemId,"$7969" );break;//�巡�� ���޶������ 
				case 437010 : dragonJewel(pc,useItem,1000000);break; //�巡�� ���̾Ƹ�� 
				case 437013 : dragonJewel(pc,useItem,1500000);break; //�巡�� �����̾�
				case 437012 : dragonJewel(pc,useItem,1700000);break; //�巡�� ���  
				case 437036 : dragonEmerald(pc,useItem);break;//�巡�� ���޶���
				case 5000151 : 
					UserCommands.tell(pc);
					pc.getInventory().removeItem(useItem, 1);
					break;	
				case 100903: case 100904: pc.sendPackets(new S_UserCommands2(1)); break;
				case 5000121 :	birthdayCake(pc); break;	//������������ũ
				case L1ItemId.METIS_ONE : metisCakeBox(pc,useItem); break;	//��Ƽ���� ù��°����
				case 5000137: BossSpawnWand(pc,useItem); break; // ���� ��ȯ
				case L1ItemId.DRAGON_KEY : dragon_Key(pc,useItem); break;//��Ű
				case L1ItemId.FORTUNE_COOKIE : fortune_Cookie(pc,useItem);break;
				case 430523:
					if (pc.getInventory(). checkItem(430523, 1) 
							&& pc.getInventory(). checkItem(430524, 1) 
							&& pc.getInventory(). checkItem(430525, 1) 
							&& pc.getInventory(). checkItem(430526, 1) 
							&& pc.getInventory(). checkItem(430527, 1) 
							&& pc.getInventory(). checkItem(430528, 1) 
							&& pc.getInventory(). checkItem(430529, 1)  ){ //����üũ�κ�
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
						//Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 2568)); // Ŭ���� ���Ѻκ� 
						pc.sendPackets(new S_SkillSound(pc.getId(), 1249));   // �߰��߽��ϴ�
						//Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 1249));    // �߰��߽��ϴ�
						pc.sendPackets(new S_SystemMessage("7���� ������ �ϳ��� ������ �������ڰ� �ϼ��Ǿ����ϴ�.")); //������ ��Ʈ����
					} else {
						pc.sendPackets(new S_SystemMessage("�������� ���� 7���� �� ������ �� 1���� ���� Ŭ�����ֽʽÿ�.")); //������ ���� ������� �޼��� ����
					}
					break;
				case 430531: // ���� ��æƮ ���� ����
					pc.getInventory().consumeItem(430531, 1);// ������ ���ش�.
					Random random = new Random();
					int[] itemrnd = { 9, 49, 54, 57, 58, 62, 81, 84, 85, 119, 121, 123, 124, 162, 164, 177, 188, 189, 190, 205, 410001, 410003, 412000, 412001
							, 415010, 415011, 415012, 415013, 450031, 450032, 450033};
					int[] enchantrnd = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 
							7, 7, 7, 7, 7, 7, 7, 7, 7, 7}; //-10~+25 ���� �����ϰ� �ش�.
					int ran1 = random.nextInt(itemrnd.length);
					int ran2 = random.nextInt(enchantrnd.length);
					createNewItem2(pc, itemrnd[ran1], 1, enchantrnd[ran2]);
					break;
				case 430532: // ���� ��æƮ �� ����
					pc.getInventory().consumeItem(430532, 1);// ������ ���ش�.
					Random random430532 = new Random();
					int[] itemrnd430532 = { 20017, 20018, 20025, 20042, 20049, 20050, 20057, 20074, 20079, 20108, 20119, 20130, 20153, 20178, 20187, 20186,
							20200, 20204, 20216, 20218, 20235, 120074, 500010, 500011, 500012, 500013, 500014, 500015 };
					int[] enchantrnd430532 = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 
							5, 5, 5, 5, 5, 5, 5, 5}; //-10~+25 ���� �����ϰ� �ش�.
					int ran11 = random430532.nextInt(itemrnd430532.length);
					int ran22 = random430532.nextInt(enchantrnd430532.length);
					createNewItem2(pc, itemrnd430532[ran11], 1, enchantrnd430532[ran22]);
					break;
				case 3500057:
					if (pc.getInventory(). checkItem(41246, 20000) && pc.getInventory(). checkItem(3500057, 1)){ //����üũ�κ�
						pc.getInventory().consumeItem(41246, 20000); 
						pc.getInventory().consumeItem(3500057, 1);
						pc.getInventory().storeItem(3500069, 1); //������ ����
						pc.sendPackets(new S_SystemMessage("������ ������ �������� ���޵Ǿ����ϴ�.")); //������ ��Ʈ����
					} else { //�׷����ʰ� ��� ������ �ȸ´´ٸ�
						pc.sendPackets(new S_SystemMessage("�ϵ����ϱ���,����ü20000�� �� �����մϴ�.")); //������ ���� ������� �޼��� ����
					}
					break;
				case 3500058: case 3500059: case 3500060: case 3500061: case 3500062: case 3500063: case 3500064: case 3500065: case 3500066: case 3500067:
					if (pc.getInventory(). checkItem(3500058, 1) && pc.getInventory(). checkItem(3500059, 1) && pc.getInventory(). checkItem(3500060, 1) && pc.getInventory(). checkItem(3500061, 1) && pc.getInventory(). checkItem(3500062, 1) && pc.getInventory(). checkItem(3500063, 1) && pc.getInventory(). checkItem(3500064, 1) && pc.getInventory(). checkItem(3500065, 1) && pc.getInventory(). checkItem(3500066, 1) && pc.getInventory(). checkItem(3500067, 1)){ //����üũ�κ�
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
						pc.getInventory().storeItem(3500057, 1); //�ϱ��� ����
						pc.sendPackets(new S_SystemMessage("��ο� �ϵ��� �ϱ����� �ϼ��Ǿ����ϴ�.")); //������ ��Ʈ����
					} else { //�׷����ʰ� ��� ������ �ȸ´´ٸ�
						pc.sendPackets(new S_SystemMessage("�ϵ����ϱ�(��10����)�� �����մϴ�.")); //������ ���� ������� �޼��� ����
					}
					break;
				case 3500068:
					if (pc.getInventory(). checkItem(3500058, 1) && pc.getInventory(). checkItem(3500059, 1) && pc.getInventory(). checkItem(3500060, 1) && pc.getInventory(). checkItem(3500061, 1) && pc.getInventory(). checkItem(3500062, 1) && pc.getInventory(). checkItem(3500063, 1) && pc.getInventory(). checkItem(3500064, 1) && pc.getInventory(). checkItem(3500065, 1) && pc.getInventory(). checkItem(3500066, 1) && pc.getInventory(). checkItem(3500067, 1)){ //����üũ�κ�
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
						pc.getInventory().storeItem(3500057, 1); //�ϱ��� ����
						pc.sendPackets(new S_SystemMessage("��ο� �ϵ��� �ϱ����� �ϼ��Ǿ����ϴ�.")); //������ ��Ʈ����
					} else { //�׷����ʰ� ��� ������ �ȸ´´ٸ�
						pc.sendPackets(new S_SystemMessage("�ϵ����ϱ�(��10����)�� �����մϴ�.")); //������ ���� ������� �޼��� ����
					}
					break;
				case 4500153:
					if (pc.getInventory(). checkItem(4500152, 200) && pc.getInventory(). checkItem(555555, 1)){ //����üũ�κ�
						pc.getInventory().consumeItem(4500152, 200); //
						pc.getInventory().consumeItem(555555, 1);//
						pc.getInventory().consumeItem(4500153, 1);//
						pc.getInventory().storeItem(888888, 1); //
						pc.sendPackets(new S_SystemMessage("�Ͱ���(2��) ���׷��̵� �� �����Ͽ����ϴ�.")); //������ ��Ʈ����
					} else { //�׷����ʰ� ��� ������ �ȸ´´ٸ�
						pc.sendPackets(new S_SystemMessage("�Ͱ��� ����(200��),�Ͱ���(1��)�� �����մϴ�.")); //������ ���� ������� �޼��� ����
					}
					break;
				case 4500154:
					if (pc.getInventory(). checkItem(4500152, 700) && pc.getInventory(). checkItem(888888, 1)){ //����üũ�κ�
						pc.getInventory().consumeItem(4500152, 700); //
						pc.getInventory().consumeItem(888888, 1);//
						pc.getInventory().consumeItem(4500154, 1);//
						pc.getInventory().storeItem(999999, 1); //
						pc.sendPackets(new S_SystemMessage("�Ͱ���(3��) ���׷��̵� �� �����Ͽ����ϴ�.")); //������ ��Ʈ����
					} else { //�׷����ʰ� ��� ������ �ȸ´´ٸ�
						pc.sendPackets(new S_SystemMessage("�Ͱ��� ����(700��),�Ͱ���(2��)�� �����մϴ�.")); //������ ���� ������� �޼��� ����
					}
					break;
				case 447012:
					SimpleDateFormat formatter = new SimpleDateFormat ( "yy/MM/dd", Locale.KOREA );
					Date currentTime = new Date ( );
					String dTime = formatter.format ( currentTime );
					if(!l1iteminstance1.getItem().isTradable()){
						pc.sendPackets(new S_SystemMessage("��ȯ�Ұ��� �������� ��Ź �Ҽ� �����ϴ�."));
						return; 
					}
					if(l1iteminstance1.isEquipped()){
						pc.sendPackets(new S_SystemMessage("�������� �������� ��Ź �Ҽ� �����ϴ�."));
						return;
					}
					if (l1iteminstance1.getBless() > 3) {
						pc.sendPackets(new S_SystemMessage("���ε� �������� ��Ź �� �� �����ϴ�."));
						return;
					}
					if (l1iteminstance1.getItem().getType2() == 1 || l1iteminstance1.getItem().getType2() == 2) {
						int count = pc.getInventory().countItems(447012); // ��Ź�Ǹ� ���� �����۹�ȣ�� ���� �ѿ� �°�
						pc.getInventory().removeItem(useItem, count);
						pc.getInventory().removeItem(l1iteminstance1, 1);
						AuctionBTable.getInstance().writeTopic(pc, dTime ,l1iteminstance1, count);
					} else {
						pc.sendPackets(new S_SystemMessage("���� ���� ��Ź�����մϴ�."));
					}
					break;
				default : unfitforuseItem(pc,useItem);break;
				}
			}
			L1ItemDelay.onItemUse(pc, useItem); // ������ ���� ����
		}
	}

	//�����ϴºκ� 
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
			pc.sendPackets(new S_ServerMessage(403, item.getLogName())); //������ ��Ʈ���� %O%o ������ϴ�.
		} else {
			pc.sendPackets(new S_ServerMessage(1110)); // ������ �����մϴ�.
		}
	}
	private void CreateItemNoDelete(L1PcInstance pc, int checkItem, int checkItemCount, int storeItem, int storeItemCount){
		if (pc.getInventory().checkItem(checkItem, checkItemCount)){  // 
			pc.getInventory().consumeItem(checkItem, checkItemCount); //
			L1ItemInstance item = pc.getInventory().storeItem(storeItem, storeItemCount); // 
			pc.sendPackets(new S_ServerMessage(403, item.getLogName())); //������ ��Ʈ���� %O%o ������ϴ�.
		} else {
			pc.sendPackets(new S_ServerMessage(1110)); // ������ �����մϴ�.
		}
	}
	private String getItemName(L1PcInstance pc, int itemId){
		String getName = pc.getInventory().getItem(itemId).getName();
		return getName;
	}
	//�巡����Ű 
	private void dragon_Key(L1PcInstance pc,L1ItemInstance useItem){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (castle_id != 0) { // ���������� ��� �Ұ�
			pc.sendPackets(new S_ServerMessage(1892)); // �� ������ �巡�� Ű�� ����� �� �����ϴ�.
			return;
		}
		if (CharPosUtil.getZoneType(pc) == 0) {
			pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONMENU, useItem));                       
		} else {
			pc.sendPackets(new S_ServerMessage(1892)); // �� ������ �巡�� Ű�� ����� �� �����ϴ�.
		}  
	}

	//���� ��ȯ ����
	private void BossSpawnWand(L1PcInstance pc,L1ItemInstance useItem){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (castle_id != 0) { // ���������� ��� �Ұ�
			pc.sendPackets(new S_ServerMessage(353)); // �� ��ó������ ���͸� ��ȯ�� �� �����ϴ�.
			return;
		}
		if (CharPosUtil.getZoneType(pc) == 0) {
			useMobEventSpownWand(pc, useItem);
			pc.getInventory().removeItem(useItem, 1);
		} else {
			pc.sendPackets(new S_ServerMessage(353)); // �� ��ó������ ���͸� ��ȯ�� �� �����ϴ�.
		} //������� �߰�
	}
	private void pet_Necklace(L1PcInstance pc,int itemObjid){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);////////////������ �������� ���� ��ų ����
		if (castle_id != 0){
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			return;
		} else {
			if (pc.getInventory().checkItem(41160)) { // ��ȯ�� �Ǹ�
				if (withdrawPet(pc, itemObjid)) {
					pc.getInventory().consumeItem(41160, 1);
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			}
		}
	}

	private void pet_Whistle(L1PcInstance pc){
		pc.sendPackets(new S_Sound(437));
		Broadcaster.broadcastPacket(pc, new S_Sound(437));
		Object[] petList = pc.getPetList().values().toArray();
		for (Object petObject : petList) {
			if (petObject instanceof L1PetInstance) { // ��
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
				if (guardian.getNpcTemplate().get_npcId() == 70850) { // ��
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
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
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
		pc.sendPackets(new S_SystemMessage("��� �ɸ����� ��ǥ�� ���������� ���� �Ǿ����ϴ�."));
	}

	private void weddingRing(L1PcInstance pc,L1ItemInstance useItem){
		L1PcInstance partner = null;
		boolean partner_stat = false;
		if (pc.getPartnerId() != 0) { // ��ȥ��
			partner = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getPartnerId());
			if (partner != null && partner.getPartnerId() != 0&& pc.getPartnerId() == partner.getId()&& partner.getPartnerId() == pc.getId()) {
				partner_stat = true;
			}
		} else {
			pc.sendPackets(new S_ServerMessage(662)); // \f1�������ȥ�����ʾҽ��ϴ�.
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
			pc.sendPackets(new S_SystemMessage(DelayTime + "�� �Ŀ� ����Ͻ� �� �ֽ��ϴ�."));
			return;
		}
		if (partner_stat) {
			boolean castle_area = L1CastleLocation.checkInAllWarArea(partner.getX(), partner.getY(), partner.getMapId());

			if ((partner.getMapId() == 0 || partner.getMapId() == 4 || partner.getMapId() == 53 || partner.getMapId() == 54 
					|| partner.getMapId() == 55 || partner.getMapId() == 56 || partner.getMapId() == 304) && castle_area == false) {
				useItem.setChargeCount(useItem.getChargeCount() - 1);
				pc.getInventory().updateItem(useItem,L1PcInventory.COL_CHARGE_COUNT);
				L1Teleport.teleport(pc, partner.getX(), partner.getY(), partner.getMapId(), 5, true);
				pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_DELAY, 300 * 1000); // ����������
			} else {
				pc.sendPackets(new S_ServerMessage(547)); // \f1�����
				// ��Ʈ�ʴ�
				// ����
				// ����� ��
				// �� ����
				// ������
				// �÷������Դϴ�.
			}
		} else {
			pc.sendPackets(new S_ServerMessage(546)); // \f1�����
			// ��Ʈ�ʴ� ����
			// �÷��̸� �ϰ�
			// ���� �ʽ��ϴ�.
		}
	}

	private void secretRoom_Key(L1PcInstance pc){
		//������
		if (pc.isKnight()
				&& (pc.getX() >= 32806 && pc.getX() <= 32814)
				&& (pc.getY() >= 32798 && pc.getY() <= 32807)
				&& pc.getMapId() == 13) {						
			L1Teleport.teleport(pc, 32815, 32810, (short) 13, 5, false);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
		}
	}
	private void spirit_Crystal(L1PcInstance pc){
		if(pc.getMap().isEscapable()) { // ��ȯ���������ΰ��� �˻��Ѵ�			
			L1Teleport.teleport(pc, 32922, 32812, (short) 430, 5, true);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
		}
	}

	private void misterioso_Shell(L1PcInstance pc){
		// ����� ž�� ������ ���ʿ� �ִ� ���� �������� ��ǥ
		if (pc.isElf()
				&& (pc.getX() >= 33971 && pc.getX() <= 33975)
				&& (pc.getY() >= 32324 && pc.getY() <= 32328)
				&& pc.getMapId() == 4
				&& !pc.getInventory().checkItem(40548)) { // ������ ����
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
				pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			} else {
				L1SpawnUtil.spawn(pc, 45300, 0, 0, false); // ������� ����
			}
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
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
			pc.sendPackets(new S_ServerMessage(79));// \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
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
		// ���ε� ������ ž 11~91�� ����
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
	private void shinefish(L1PcInstance pc,L1ItemInstance useItem,int item1, int item2,int item3, int item4){ //������ ������
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
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 �� ������ ����� �� �����ϴ�.
					return;
				}
			}
		}  
		int entertime = pc.getGdungeonTime() % 1000;
		if(entertime > 61){
			pc.setGdungeonTime(pc.getGdungeonTime() - 60);
			pc.getInventory().consumeItem(41159, 500); 
			pc.sendPackets(new S_SystemMessage("������� ü���ð��� 1�ð� �����Ǿ����ϴ�."));
			pc.save();
		} else {
			pc.sendPackets(new S_SystemMessage("������� ü���ð��� ���� ��� �Ұ��մϴ�."));
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
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 �� ������ ����� �� �����ϴ�.
					return;
				}
			}
		}  
		int entertime = pc.getGdungeonTime() % 1000;
		if(entertime > 121){
			pc.setGdungeonTime(pc.getGdungeonTime() - 120);
			pc.getInventory().consumeItem(41159, 2000); 
			pc.sendPackets(new S_SystemMessage("������� ü���ð��� 2�ð� �����Ǿ����ϴ�."));
			//pc.getSkillEffectTimerSet().setSkillEffect(ITEMUSEDELAY_GIRAN, 14400 * 1000); // 4�ð� ��ų�� 4�ð����� �ְ� ����հ� ��
			pc.save();
		} else {
			pc.sendPackets(new S_SystemMessage("������� ü���ð��� ���� ��� �Ұ��մϴ�."));
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
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 �� ������ ����� �� �����ϴ�.
					return;
				}
			}
		}  
		int entertime = pc.getIvoryTowerTime() % 1000;
		if(entertime > 50) {
			pc.setIvoryTowerTime(pc.getIvoryTowerTime() - 50);
			pc.getInventory().consumeItem(41159, 500);
			pc.sendPackets(new S_SystemMessage("" + pc.getName() + "\\fY���� ���ž �ð��� �����Ǿ����ϴ�."));
			//pc.getSkillEffectTimerSet().setSkillEffect(ITEMUSEDELAY_IVORY, 14400 * 1000); // 4�ð� ��ų�� 4�ð����� �ְ� ����հ� ��
			pc.save(); 
		} else {
			pc.sendPackets(new S_SystemMessage("\\fY���ž ü���ð��� ���� ���Ұ����մϴ�."));
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
					pc.sendPackets(new S_ServerMessage(1139, used_time)); //%0 �� ������ ����� �� �����ϴ�.
					return;
				}
			}
		}  
		int entertime = pc.getLdungeonTime() % 1000;
		if (entertime > 61) {
			pc.setLdungeonTime(pc.getLdungeonTime() - 60);
			pc.getInventory().consumeItem(41159, 500);
			pc.sendPackets(new S_SystemMessage("" + pc.getName() + "\\fY���� ��Ÿ�ٵ� �ð��� �����Ǿ����ϴ�."));
			pc.save(); 
		} else {
			pc.sendPackets(new S_SystemMessage("\\fY��Ÿ�ٵ� ü���ð��̳��� ���Ұ����մϴ�."));
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
			pc.sendPackets(new S_ServerMessage(563)); // ���⿡���� ����� �� �����ϴ�.
		} else {
			L1SpawnUtil.spawn(pc, 450001854, 0, 300000, false);
			pc.getInventory().consumeItem(5000161, 1);
		}
	}
	private void portableStorage(L1PcInstance pc,L1ItemInstance useItem){
		int castle_id = L1CastleLocation.getCastleIdByArea(pc); 
		if (castle_id != 0){
			pc.sendPackets(new S_ServerMessage(563)); // ���⿡���� ����� �� �����ϴ�.
			return;
		}
		L1SpawnUtil.spawn(pc, 60009, 0, 60*1000, false);//����
		pc.sendPackets(new S_SystemMessage("â�� ��ȯ�߽��ϴ�. 5���Ŀ�������ϴ�."));
		pc.getInventory().removeItem(useItem,1);
	}
	private void shadowTempleKey(L1PcInstance pc,L1ItemInstance useItem){
		if(pc.getMap().isEscapable()) { // ��ȯ���������ΰ��� �˻��Ѵ�
			L1Teleport.teleport(pc, ((L1EtcItem) useItem
					.getItem()).get_locx(),
					((L1EtcItem) useItem.getItem()).get_locy(),
					((L1EtcItem) useItem.getItem()).get_mapid(), 5, true);
		} else {
			// \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			pc.sendPackets(new S_ServerMessage(79));
		}
	}
	private void smallTreasureMap(L1PcInstance pc){
		//} else if (itemId == 40701) { // ���� ������ ����
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
		if (((pc.getX() >= 32844 && pc.getX() <= 32845)|| (pc.getY() >= 32693 && pc.getY() <= 32694)) && pc.getMapId() == 550) { // ���� ����:������
			L1Teleport.teleport(pc, ((L1EtcItem) useItem.getItem()).get_locx(),
					((L1EtcItem) useItem.getItem()).get_locy(),
					((L1EtcItem) useItem.getItem()).get_mapid(), 5, true);
		} else {
			// \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			pc.sendPackets(new S_ServerMessage(79));
		}
	}

	private void silverPlute(L1PcInstance pc){
		pc.sendPackets(new S_Sound(10));
		Broadcaster.broadcastPacket(pc, new S_Sound(10));
		if (((pc.getX() >= 32619 && pc.getX() <= 32623)
				|| (pc.getY() >= 33120 && pc.getY() <= 33124))
				&& pc.getMapId() == 440){ // ���� �ø������ݸ��� ������ ��ǥ
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
			pc.sendPackets(new S_ServerMessage(278)); // \f1MP�� ������ ������ ����� �� ���� �ʽ��ϴ�.
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
			L1SpawnUtil.spawn(pc, 450001798, 0, 300000, true);//��������ũ����    
		}else if(castle_id != 0) {
			pc.sendPackets(new S_ServerMessage(563)); // ���⿡���� ����� �� �����ϴ�.
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
				// �ູ�� ��Ȱ ��ũ�Ѱ� ���� ȿ��
				tg.setTempID(objid);
				tg.sendPackets(new S_Message_YN(322, "")); // �� ��Ȱ�ϰ� �ͽ��ϱ�? (Y/N)
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
		pc.sendPackets(new S_ServerMessage(403, "��Ƽ���� ����ũ ����"));
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
			pc.sendPackets(new S_SystemMessage("�ູ���� "+(ainhasad/10000)+"�̸������� ����ϽǼ� �ֽ��ϴ�."));
		}
	}

	private void dragonEmerald(L1PcInstance pc,L1ItemInstance useItem){
		if (pc.getLevel() < 49) {
			pc.sendPackets(new S_ServerMessage(318, "49"));
			return;
		}		
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DRAGON_EMERALD_NO) == true) {
			pc.sendPackets(new S_ServerMessage(2145));//����Ҽ�����          
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
		pc.sendPackets(new S_ServerMessage(822)); // �� �� ����� ������ �� �� ���� ���� ����� ���� �����ϴ�.	
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
			pc.sendPackets(new S_SystemMessage(pc.getName() + "���� ų,���� ��ġ�� �ʱ�ȭ�Ǿ����ϴ�."));
			pc.getInventory().removeItem(useItem, 1);
			pc.save();
		} catch (Exception c) {
		}
	}

	private void firewood(L1PcInstance pc,L1ItemInstance useItem){
		for (L1Object object : L1World.getInstance().getVisibleObjects(pc, 3)) {
			if (object instanceof L1EffectInstance) {
				if (((L1NpcInstance) object).getNpcTemplate().get_npcId() == 81170) {
					pc.sendPackets(new S_ServerMessage(1162)); // ���� ������ ��ں��� �ֽ��ϴ�.
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
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
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
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			return;
		}
		pc.getSkillEffectTimerSet().setSkillEffect(STATUS_HOLY_WATER_OF_EVA, 900 * 1000);
		pc.sendPackets(new S_SkillSound(pc.getId(), 190));
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 190));
		pc.sendPackets(new S_ServerMessage(1140));
		pc.getInventory().removeItem(useItem, 1);
	}
	private void create_Chance_Item_Safe(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem ,int minItemId,int maxItemId,int num){
		//���ź����� �Ϻ�(1~4 �ܰ�)
		int earing2Id = l1iteminstance1.getItem().getItemId();
		//int potion1 = 0;
		//int potion2 = 0;
		// �� ����
		if (earing2Id >= minItemId && maxItemId >= earing2Id ) {
			if ((CommonUtil.random(99)+1) < Config.CREATE_CHANCE_MYSTERIOUS) {
				createNewItem(pc, (earing2Id - num), 1);//-12
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(useItem, 1);	
			} else {
				pc.sendPackets(new S_ServerMessage(160, l1iteminstance1.getName()));
				// \f1%0��%2 �����ϰ�%1 �������ϴٸ�, ������ �����ϰ� ��ҽ��ϴ�.
				pc.getInventory().removeItem(useItem, 1);
			}
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
		}
	}

	private void adenaBankbook(L1PcInstance pc){
		if (pc.getInventory().checkItem(40308, 100000000)) {
			pc.getInventory().consumeItem(40308, 100000000);
			pc.getInventory().storeItem(400077, 1);
			pc.sendPackets(new S_SystemMessage("1�� �Ƶ����� 1�� ��ǥ�� ��ȯ�Ǿ����ϴ�."));
		} else {
			pc.sendPackets(new S_SystemMessage("1�� �Ƶ����� �־�� �����մϴ�."));}	 
	}
	
	private void adenaCheck(L1PcInstance pc){
		if(pc.getInventory().getSize() <= 200000000){
			pc.getInventory().storeItem(40308, 100000000);
			pc.getInventory().consumeItem(400077, 1);
			pc.sendPackets(new S_SystemMessage("1�� �Ƶ����� ��ȯ�Ǿ����ϴ�."));
		} else {
			pc.sendPackets(new S_SystemMessage("20�� �Ƶ����� �Ѿ����ϴ�. ��ȯ�Ҽ������ϴ�.."));
		}
	}

	private void FirstPkg (L1PcInstance pc){
		if (pc.getInventory().checkItem(5000149, 1)) { // üũ �Ǵ� �����۰� ����
			pc.getInventory().consumeItem(5000149, 1); // �����Ǵ� �����۰� ����	
			createNewItem2(pc, 120056, 1, 7); //����
			createNewItem2(pc, 20011, 1, 7); //����
			createNewItem2(pc, 20187, 1, 7); //�ı�
			createNewItem2(pc, 490015, 1, 7); //����Ƽ
			createNewItem2(pc, 20200, 1, 7); //����
			createNewItem2(pc, 20280, 1, 0); //�긶
			createNewItem2(pc, 20280, 1, 0); //�긶
			createNewItem2(pc, 21024, 1, 0); //��ȣ��5�ܱ�
			createNewItem2(pc, 20362, 1, 0); //��ȣ��5�ܸ�
		}
	}

	private void SecondPkg (L1PcInstance pc){
		if (pc.getInventory().checkItem(5000156, 1)) { // üũ �Ǵ� �����۰� ����
			pc.getInventory().consumeItem(5000156, 1); // �����Ǵ� �����۰� ����	
			createNewItem2(pc, 120056, 1, 9); //����
			createNewItem2(pc, 20011, 1, 9); //����
			createNewItem2(pc, 20178, 1, 8); //����
			createNewItem2(pc, 490015, 1, 8); //����Ƽ
			createNewItem2(pc, 20200, 1, 8); //����						
			createNewItem2(pc, 21027, 1, 0); //��ȣ��8�ܱ�
			createNewItem2(pc, 20365, 1, 0); //��ȣ��8�ܸ�
		}
	}
	private void finishedTreasureMap(L1PcInstance pc,L1ItemInstance useItem){
		if (pc.getInventory().checkItem(40621)) {
			// \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			pc.sendPackets(new S_ServerMessage(79));
		} else if (((pc.getX() >= 32856 && pc.getX() <= 32858)|| (pc.getY() >= 32857 && pc.getY() <= 32858))&& pc.getMapId() == 443) { // �������� ���� ���� 3��
			L1Teleport.teleport(pc, ((L1EtcItem) useItem.getItem()).get_locx(), ((L1EtcItem) useItem.getItem()).get_locy(),
					((L1EtcItem) useItem.getItem()).get_mapid(), 5, true);
		} else {
			// \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
			pc.sendPackets(new S_ServerMessage(79));
		}
	}
	//�����ֹ��� 
	private void magicScroll(L1PcInstance pc,L1ItemInstance useItem ,LineageClient client,int itemId,int spellsc_objid,int spellsc_x, int spellsc_y ){
		int chance = CommonUtil.random(10) ;
		if (pc.isSkillDelay()) {
			pc.sendPackets(new S_ServerMessage(281));	
			return;
		}
		if(chance != 0){
			if (spellsc_objid == pc.getId() && useItem.getItem().getUseType() != 30) { 
				// spell_buff
				pc.sendPackets(new S_ServerMessage(281)); // \f1������ ��ȿ�� �Ǿ����ϴ�.
				return;
			}
			pc.getInventory().removeItem(useItem, 1);
			if (spellsc_objid == 0
					&& useItem.getItem().getUseType() != 0
					&& useItem.getItem().getUseType() != 26
					&& useItem.getItem().getUseType() != 27) {
				return;
				// Ÿ���� ���� ��쿡 handleCommands���� �Ǳ� (����)������ ���⼭ return
				// handleCommands ������ �Ǵܣ�ó���ؾ� �� �κ������� �𸥴�
			}
			pc.cancelAbsoluteBarrier(); // �ۼ�����ü
			int skillid = itemId - 40858;
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(client.getActiveChar(),
					skillid, spellsc_objid, spellsc_x, spellsc_y,
					null, 0, L1SkillUse.TYPE_SPELLSC);
		}else {
			pc.sendPackets(new S_ServerMessage(280)); // ������ �����߽��ϴ�.
		}
	}
	//�����ֹ��� 7~10�ܰ�
	private void magicScroll2(L1PcInstance pc,L1ItemInstance useItem ,LineageClient client,int itemId,int spellsc_objid,int spellsc_x, int spellsc_y ){
		int chance = CommonUtil.random(10) ;
		if (pc.isSkillDelay()) {
			pc.sendPackets(new S_ServerMessage(281));	
			return;
		}
		if(chance != 0){
			if (spellsc_objid == pc.getId() && useItem.getItem().getUseType() != 30) { 
				// spell_buff
				pc.sendPackets(new S_ServerMessage(281)); // \f1������ ��ȿ�� �Ǿ����ϴ�.
				return;
			}
			pc.getInventory().removeItem(useItem, 1);
			if (spellsc_objid == 0
					&& useItem.getItem().getUseType() != 0
					&& useItem.getItem().getUseType() != 26
					&& useItem.getItem().getUseType() != 27) {
				return;
				// Ÿ���� ���� ��쿡 handleCommands���� �Ǳ� (����)������ ���⼭ return
				// handleCommands ������ �Ǵܣ�ó���ؾ� �� �κ������� �𸥴�
			}
			pc.cancelAbsoluteBarrier(); // �ۼ�����ü
			int skillid = itemId - 41880;
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(client.getActiveChar(),
					skillid, spellsc_objid, spellsc_x, spellsc_y,
					null, 0, L1SkillUse.TYPE_SPELLSC);
		} else {
			pc.sendPackets(new S_ServerMessage(280)); // ������ �����߽��ϴ�.
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
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
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
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
		}
	}

	private void ancient_RoyalSeal(L1PcInstance pc,L1ItemInstance useItem,LineageClient client){
		if (client.getAccount().getCharSlot() < 8){
			client.getAccount().setCharSlot(client, client.getAccount().getCharSlot()+1);
			pc.getInventory().removeItem(useItem, 1);
		}else{
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
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
					pc.sendPackets(new S_SystemMessage(pc.getName() + "���� " + ClanName + "���� ���Ϳ� �����Ͽ����ϴ�."));
					pc.sendPackets(new S_SystemMessage("�� "+ Config.NEWUSERSAFETY_LEVEL +"���� PVP�� ���� ������, ���� ������ �ʽ��ϴ�. ��" + safetylevel +"�� �Ǵ� ���� �ڵ����� ���Ϳ��� �߹�˴ϴ�."));
				} else {
					pc.sendPackets(new S_ServerMessage(89)); // �̹� ���Ϳ� �����߽��ϴ�.
				}
			} else {
				pc.sendPackets(new S_SystemMessage("�� "+ safetylevel + "�� �����ø� �ű� ���Ϳ� �����Ͻ� �� �����ϴ�"));
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
				// n���ִ� ��� �ϰ� �ִ� �� �ۿ� ���� ���� �ʴ´�
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
		pc.sendPackets(new S_ServerMessage(155)); // \f1�������� ���� �� �� �����ϴ�.
	}
	private void create_Chance_Item_Delete(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem ,int minItemId,int maxItemId,int num){
		int diaryId = l1iteminstance1.getItem().getItemId();
		if (diaryId >= minItemId && maxItemId >= diaryId) {
			if ((CommonUtil.random(99)+1) <= Config.CREATE_CHANCE_DIARY) {
				createNewItem(pc, diaryId + num, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(158, l1iteminstance1.getName())); // \f1%0�� �����ϰ� ���� �ʰ� �Ǿ����ϴ�.
			}
			pc.getInventory().removeItem(l1iteminstance1, 1);
			pc.getInventory().removeItem(useItem, 1);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
		}
	}
	private void repairItem(L1PcInstance pc,L1ItemInstance l1iteminstance1,L1ItemInstance useItem){
		if (l1iteminstance1.getItem().getType2() != 0 && l1iteminstance1.get_durability() > 0) {
			String msg0;
			pc.getInventory().recoveryDamage(l1iteminstance1);
			msg0 = l1iteminstance1.getLogName();
			if (l1iteminstance1.get_durability() == 0) {
				pc.sendPackets(new S_ServerMessage(464, msg0)); // %0%s�� ��ǰ ���� ���°� �Ǿ����ϴ�.
			} else {
				pc.sendPackets(new S_ServerMessage(463, msg0)); // %0 ���°� ���������ϴ�.
			}
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1 �ƹ��͵� �Ͼ�� �ʾҽ��ϴ�.
		}
		pc.getInventory().removeItem(useItem, 1);
	}
	private void cure_Posion(L1PcInstance pc,int itemId,L1ItemInstance useItem){
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // ���������� ����
			pc.sendPackets(new S_ServerMessage(698)); // ���¿� ���� �ƹ��͵� ���� ���� �����ϴ�.
		} else {
			pc.cancelAbsoluteBarrier(); // �ƺ�Ҹ�Ʈ�ٸ����� ����
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
		if (pc.getInventory().checkItem(447011, count)) { // üũ �Ǵ� �����۰� ����
			pc.getInventory().consumeItem(447011, count); // �����Ǵ� �����۰� ����	
			switch(pc.getClassId()){
			case L1PcInstance.CLASSID_PRINCE: 
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 426003, 1, 0); // ���׸��� �Ǳ� ����
				createNewItem2(pc, 426013, 1, 0);// �������� ����
				createNewItem2(pc, 40031, 10, 0); //�Ǹ��� ��
				break;
			case L1PcInstance.CLASSID_PRINCESS:  
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 426003, 1, 0); // ���׸��� �Ǳ� ����
				createNewItem2(pc, 426014, 1, 0);// �������� ����
				createNewItem2(pc, 40031, 10, 0); //�Ǹ��� ��
				break;
			case L1PcInstance.CLASSID_KNIGHT_MALE:  
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 426003, 1, 0); // ���׸��� �Ǳ� ����
				createNewItem2(pc, 426015, 1, 0); // �������� ���� 
				createNewItem2(pc, 40014, 10, 0); //����ǹ���
				break;
			case L1PcInstance.CLASSID_KNIGHT_FEMALE:  
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 426003, 1, 0); // ���׸��� �Ǳ� ����
				createNewItem2(pc, 426016, 1, 0); // �������� ���� 
				createNewItem2(pc, 40014, 10, 0); //����ǹ���
				break;

			case L1PcInstance.CLASSID_ELF_MALE: 
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 426017, 1, 0); // �������� ���� 
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
				createNewItem2(pc, 40744, 1000, 0); //��ȭ��
				createNewItem2(pc, 40068, 5, 0); //�������
				createNewItem2(pc, 40114, 10, 0);  //
				break;
			case L1PcInstance.CLASSID_ELF_FEMALE: 
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 426018, 1, 0); // �������� ���� 
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
				createNewItem2(pc, 40744, 1000, 0); //��ȭ��
				createNewItem2(pc, 40068, 5, 0); //�������
				createNewItem2(pc, 40114, 10, 0);  //
				break;

			case L1PcInstance.CLASSID_WIZARD_MALE:  
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 305, 1, 7); // ���׸��� ������
				createNewItem2(pc, 426005, 1, 0); // ���׸��� �κ�
				createNewItem2(pc, 426019, 1, 0); // �������� ���� 
				break;
			case L1PcInstance.CLASSID_WIZARD_FEMALE: 
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 305, 1, 7); // ���׸��� ������
				createNewItem2(pc, 426005, 1, 0); // ���׸��� �κ�
				createNewItem2(pc, 426020, 1, 0); // �������� ���� 
				break;

			case L1PcInstance.CLASSID_DARKELF_MALE: 
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 306, 1, 7); // ���׸��� ũ�ο�
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 426021, 1, 0);// �������� ���� 
				break;
			case L1PcInstance.CLASSID_DARKELF_FEMALE: 
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 306, 1, 7); // ���׸��� ũ�ο�
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 426022, 1, 0);// �������� ���� 
				break;

			case L1PcInstance.CLASSID_DRAGONKNIGHT_MALE:  
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 307, 1, 7); // ���׸��� ü�μҵ�
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 426023, 1, 0);// �������� ���� 
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
				break;
			case L1PcInstance.CLASSID_DRAGONKNIGHT_FEMALE: 
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 307, 1, 7); // ���׸��� ü�μҵ�
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 426024, 1, 0);// �������� ���� 
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
				break;

			case L1PcInstance.CLASSID_ILLUSIONIST_MALE:  
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 305, 1, 7); // ���׸��� ü�μҵ�
				createNewItem2(pc, 426005, 1, 0); // ���׸��� �κ�
				createNewItem2(pc, 426025, 1, 0); // �������� ���� 
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
				break;
			case L1PcInstance.CLASSID_ILLUSIONIST_FEMALE: 
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 305, 1, 7); // ���׸��� ü�μҵ�
				createNewItem2(pc, 426005, 1, 0); // ���׸��� �κ�
				createNewItem2(pc, 426026, 1, 0); // �������� ���� 
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
				break;
			}

			createNewItem2(pc, 426001, 1, 0); // ���׸��� �ذ� ����
			createNewItem2(pc, 426002, 1, 0); // ���׸��� Ƽ����
			createNewItem2(pc, 426006, 1, 0); // ���׸��� ���� ����
			createNewItem2(pc, 426007, 1, 0);  // ���׸��� ��ȭ
			createNewItem2(pc, 426008, 1, 0); // ���׸��� ����
			createNewItem2(pc, 426009, 1, 0); // ���׸��� �尩
			createNewItem2(pc, 426010, 1, 0); // ���׸��� ����
			createNewItem2(pc, 20282, 1, 0); //���ž����
			createNewItem2(pc, 20282, 1, 0); //���ž����
			createNewItem2(pc, 40308, 50000, 0); //�Ƶ���
			createNewItem2(pc, 41159, 20, 0); //����
			createNewItem2(pc, 437004, 1, 0); //���� �ֹ���
			createNewItem2(pc, 40081, 50, 0); //���������ȯ�ֹ���
			createNewItem2(pc, 40099, 100, 0); //�����̵��ֹ���
			createNewItem2(pc, 140100, 10, 0); //������̵��ֹ���
			createNewItem2(pc, 40088, 10, 0); //�����ֹ���
			createNewItem2(pc, 40126, 10, 0); //Ȯ���ֹ���
			createNewItem2(pc, 50020, 10, 0); //���� �ֹ���
			createNewItem2(pc, 40021, 100, 0); //���భ����
			createNewItem2(pc, 40018, 5, 0); //����
			createNewItem2(pc, 430005, 1, 0); //ȸ���� �к� 
			createNewItem2(pc, 41246, 1000, 0); //����ü
			createNewItem2(pc, 560025, 10, 0); //���� ���å 
			createNewItem2(pc, 560027, 10, 0); //���� ���å 
			createNewItem2(pc, 435009, 1, 0); //�ڸ���Ʈ
			createNewItem2(pc, 435010, 1, 0); //�ڸ���Ʈ
			createNewItem2(pc, 435011, 1, 0); //�ڸ���Ʈ
			createNewItem2(pc, 435012, 1, 0); //�ڸ���Ʈ
			createNewItem2(pc, 435013, 1, 0); // �ڸ���Ʈ
			createNewItem2(pc, 435000, 5, 0); // ����
			createNewItem2(pc, 437031, 5, 0); // ��ڹ�������
		} 
	}

	private void newUseSupportBox2(L1PcInstance pc){
		if (pc.getInventory().checkItem(447013, 1)) { // üũ �Ǵ� �����۰� ����
			pc.getInventory().consumeItem(447013, 1); // �����Ǵ� �����۰� ����	
			if (pc.isKnight()) { // ��� ����
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 426003, 1, 0); // ���׸��� �Ǳ� ����
				createNewItem2(pc, 40014, 10, 0); //����ǹ���
			} else if (pc.isDragonknight()) {	
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 307, 1, 7); // ���׸��� ü�μҵ�
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
			} else if (pc.isCrown()) {
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 303, 1, 7); // ���׸��� ��հ�
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 40031, 10, 0); //�Ǹ��� ��
			} else if (pc.isWizard()) {
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 305, 1, 7); // ���׸��� ������
				createNewItem2(pc, 426005, 1, 0); // ���׸��� �κ�
			} else if (pc.isIllusionist()) {
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 305, 1, 7); // ���׸��� ü�μҵ�
				createNewItem2(pc, 426005, 1, 0); // ���׸��� �κ�
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
			} else if (pc.isElf()) {
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 302, 1, 7); // ���׸��� �Ѽհ�
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
				createNewItem2(pc, 430006, 10, 0); //���׵�󿭸�
				createNewItem2(pc, 40744, 1000, 0); //��ȭ��
				createNewItem2(pc, 40068, 5, 0); //�������
				createNewItem2(pc, 40114, 10, 0);  //
			} else if (pc.isDarkelf()) {
				createNewItem2(pc, 301, 1, 7); // ���׸��� �ܰ�
				createNewItem2(pc, 304, 1, 7); // ���׸��� �����
				createNewItem2(pc, 306, 1, 7); // ���׸��� ũ�ο�
				createNewItem2(pc, 426004, 1, 0); // ���׸��� ���װ���
			}
			createNewItem2(pc, 426001, 1, 0); // ���׸��� �ذ� ����
			createNewItem2(pc, 426002, 1, 0); // ���׸��� Ƽ����
			createNewItem2(pc, 426006, 1, 0); // ���׸��� ���� ����
			createNewItem2(pc, 426007, 1, 0);  // ���׸��� ��ȭ
			createNewItem2(pc, 426008, 1, 0); // ���׸��� ����
			createNewItem2(pc, 426009, 1, 0); // ���׸��� �尩
			createNewItem2(pc, 426010, 1, 0); // ���׸��� ����
			createNewItem2(pc, 20282, 1, 0); //���ž����
			createNewItem2(pc, 20282, 1, 0); //���ž����
			//createNewItem2(pc, 4500172, 5, 0); //80�����ֹ���
			//createNewItem2(pc, 4500173, 5, 0); //80�����ֹ���
		} 
	}
	//��ÿ�ä���ִ� ������ 
	private void MpPosion(L1PcInstance pc,L1ItemInstance useItem ,int currentMp , int randomCurrentMp){
		pc.sendPackets(new S_ServerMessage(338, "$1084")); // �����%0�� ȸ���� �� ���Դϴ�.
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
				pc.sendPackets(new S_ServerMessage(1181)); // �ش� ���� ������ ���� ��� ���Դϴ�.
				return;
			}
			if ((i == 41248) || (i == 41249) || (i == 41250) || (i == 430000) || (i == 430001) || (i == 430002) 
					|| (i == 430003) || (i == 430004) || (i == 430500) || (i == 430505) || (i == 430506) || (i == 5000034)) 
			{
				int i50 = CommonUtil.random(130);
				switch(i50/10){
				case 0 :	pc.sendPackets(new S_SystemMessage("������ ���ߵǾ� ������ϴ�.��_��")); break;
				case 1 :	pc.getInventory().storeItem(41249, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ��ť������ ������ϴ�.")); break;
				case 2 :	pc.getInventory().storeItem(41250, 1);
				pc.sendPackets(new S_SystemMessage("�������� : �����ΰ��� ������ϴ�.")); break;
				case 3 :	pc.getInventory().storeItem(430000, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ������ ������ϴ�.")); break;
				case 4 :	pc.getInventory().storeItem(430001, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ��θ� ������ϴ�.")); break;
				case 5 :	pc.getInventory().storeItem(430002, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ũ����Ʈ�þ��� ������ϴ�.")); break;
				case 6 :	pc.getInventory().storeItem(430003, 1);
				pc.sendPackets(new S_SystemMessage("�������� : �ô��� ������ϴ�.")); break;
				case 7 :	pc.getInventory().storeItem(430004, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ��Ƽ�� ������ϴ�.")); break;
				case 8 :	pc.getInventory().storeItem(5000034, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ��ƾ�� ������ϴ�.")); break;
				case 9 :	pc.getInventory().storeItem(430500, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ��īƮ������ ������ϴ�.")); break;
				case 10 :pc.getInventory().storeItem(430505, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ��̾Ƹ� ������ϴ�.")); break;
				case 11 :pc.getInventory().storeItem(41915, 1);
				pc.sendPackets(new S_SystemMessage("�������� : ����ƺ� ������ϴ�."));break;
				case 12 :pc.getInventory().storeItem(430506, 1);
				pc.sendPackets(new S_SystemMessage("�������� : �������̸� ������ϴ�."));break;
				}
				pc.getInventory().removeItem(useItem, 1);
				pc.getInventory().removeItem(l1iteminstance1, 1);                           
			}
		}
	}
	private void byBloodPledgeJoin(L1PcInstance pc,L1ItemInstance useItem)throws Exception{
		if (pc.getInventory(). checkItem(4500155, 1)){  //�κ��� �ֳ� üũ
			pc.getInventory().consumeItem(4500155, 1); // �Ҹ�
			if (pc.isCrown()) { // ���ֶ��
				if(pc.get_sex() == 0){ // ���ڶ��
					pc.sendPackets(new S_ServerMessage(87)); // ����� �����Դϴ�
				}else{ // ���ֶ��
					pc.sendPackets(new S_ServerMessage(88)); // ����� �����Դϴ�
				}
				return;
			}
			if(pc.getClanid() != 0){ // ������ �ִٸ�
				pc.sendPackets(new S_ServerMessage(89)); // �̹� ������ �ֽ��ϴ�
				return;
			}
			Connection con = null;
			con = L1DatabaseFactory.getInstance().getConnection();
			Statement pstm2 = con.createStatement(); 
			ResultSet rs2 = pstm2.executeQuery("SELECT `account_name`, `char_name`, `ClanID`, `Clanname` FROM `characters` WHERE Type = 0"); // �ɸ��� ���̺��� ���ָ� ���ͼ�
			while(rs2.next()){
				if(pc.getNetConnection().getAccountName().equalsIgnoreCase(rs2.getString("account_name"))){ // ���� ������ ������ ������ ���ؼ� �����ϸ�
					if(rs2.getInt("ClanID") != 0){ // ������ ������ �ִٸ�
						L1Clan clan = L1World.getInstance().getClan(rs2.getString("Clanname")); // ������ �������� ����
						L1PcInstance clanMember[] = clan.getOnlineClanMember();
						for (int cnt = 0; cnt < clanMember.length; cnt++) { // ������ ���Ϳ����� �޼��� �Ѹ���
							clanMember[cnt].sendPackets(new S_ServerMessage(94, pc.getName())); // \f1%0�� ������ �Ͽ����μ� �޾Ƶ鿩�����ϴ�.
						}
						pc.setClanid(rs2.getInt("ClanID"));
						pc.setClanRank(2);
						pc.setClanname(rs2.getString("Clanname"));
						pc.save(); // DB�� ĳ���� ������ �����Ѵ�
						clan.addClanMember(pc.getName(), pc.getClanRank());
						pc.sendPackets(new S_ServerMessage(95, rs2.getString("Clanname"))); // \f1%0 ���Ϳ� �����߽��ϴ�. // �޼��� ������
						pc.getInventory().removeItem(useItem, 1); 
						break;
					}
				}
			}
			rs2.first(); // ������ ó������ �ǵ�����
			rs2.close();//������� �Ʒ����� ���ҽ������κ� 
			pstm2.close();
			con.close();
			if(pc.getClanid() == 0){ // ������ �ִٸ�
				pc.sendPackets(new S_SystemMessage("\\fY�������� ���ְ� ���ų� ������ â������ �ʾҽ��ϴ�.")); // �޼��� ������
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
	//���������� ����ġ������ ��ü�۾��ǽø޼���κ�! 
	//�� ������

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
		if (pc.getLevel() < 51){//51�̸��̸� �ѹ濡 51����
			pc.addExp((ExpTable.getExpByLevel(51)-1) - pc.getExp()+((ExpTable.getExpByLevel(51)-1)/100));
			pc.getInventory().consumeItem(7062, 1);
		} else{  
			pc.addExp((ExpTable.getExpByLevel(pc.getLevel()+1)-1) - pc.getExp()+100);
			pc.getInventory().consumeItem(7062, 1);
		}
	}

	private void exp_PosionConsumable(L1PcInstance pc){
		if (pc.getLevel() < 51){//51�̸��̸� �ѹ濡 51����
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
					// �Ϲݸ�
					{ 45008, 45140, 45016, 45021, 45025, 45033, 45099, 45147,
						45123, 45130, 45046, 45092, 45138, 45098, 45127,
						45143, 45149, 45171, 45040, 45155, 45192, 45173,
						45213, 45079, 45144 },
						// ������ 10%
						{ 45488, 45456, 45473, 45497, 45464, 45545, 45529, 45516 },
						// ������ 7%
						{ 45601, 45573, 45583, 45609, 45955, 45956, 45957, 45958,
							45959, 45960, 45961, 45962, 45617, 45610, 45600,
							45614, 45618, 45649, 45680, 45654, 45674, 45625,
							45675, 45672 },
							// ������ 3%
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


	private void �������ǻ��(L1PcInstance pc,int itemId) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // ���������� ����
			pc.sendPackets(new S_ServerMessage(698)); 
			return;
		}
		if (pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_DRAGONPERL)) {
			pc.getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.STATUS_DRAGONPERL);
			pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONPERL, 0 ,0)); 
			Broadcaster.broadcastPacket(pc, new S_DRAGONPERL(pc.getId(), 0)); 
			pc.sendPackets(new S_DRAGONPERL(pc.getId(), 0 ));
			pc.set���ּӵ�(0);	 
		}
		pc.cancelAbsoluteBarrier();//�ۼ�����(�ѿ� �� �޼ҵ������ ����)
		int time = 600 *1000;
		int stime = ((time/1000)/4)-2;
		pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_DRAGONPERL, time);
		pc.sendPackets(new S_PacketBox(S_PacketBox.DRAGONPERL, 8, stime)); 
		pc.sendPackets(new S_DRAGONPERL(pc.getId(), 8 ));
		Broadcaster.broadcastPacket(pc, new S_DRAGONPERL(pc.getId(), 8 ));
		pc.sendPackets(new S_SkillSound(pc.getId(),8031));//
		Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8031));
		pc.set���ּӵ�(1);
		pc.sendPackets(new S_ServerMessage(1065));//�����߰�
		pc.getInventory().consumeItem(itemId, 1);//�����߰�
	}

	// õ���� ����
	private void UseExpPotion(L1PcInstance pc , int item_id) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(71) == true) { // ���������� ����
			pc.sendPackets(new S_ServerMessage(698, "")); // ���¿� ���� �ƹ��͵� ���� ���� �����ϴ�.
			return;
		}
		pc.cancelAbsoluteBarrier();
		int time = 0;
		if (item_id == L1ItemId.EXP_POTION
				|| item_id == L1ItemId.EXP_POTION2) { // ����ġ ��� ����
			time = 3600; // 20��
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
			} else { // ���� ��  ���� ���� ���鿡 ����߸��� ó���� ĵ���� ���� �ʴ´�(���� ����)
				L1World.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(item);
			}
			//pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0�� �տ� �־����ϴ�.
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
			} else { // ���� ��  ���� ���� ���鿡 ����߸��� ó���� ĵ���� ���� �ʴ´�(���� ����)
				L1World.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(
						item);
			}
			//pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0�� �տ� �־����ϴ�.
			return true;
		} else {
			return false;
		}
	}
	/** ������ž 11~91����, 6~96���� */
	private void useToiTeleportAmulet(L1PcInstance pc, int itemId, L1ItemInstance item) {
		boolean isTeleport = false;		
		/*
		if (itemId >= 5000200 && itemId <= 5000210 ||itemId >= 40289 && itemId <= 40297 ) { // 11,51Famulet
			if (pc.getX() >= 33923 && pc.getX() <= 33934 && pc.getY() >= 33340
					&& pc.getY() <= 33356 && pc.getMapId() == 4) { //�Ƶ������ �����
				isTeleport = true;
			}
		}*/
		// ������Ƽ�������� ��� ����
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
			pc.sendPackets(new S_ServerMessage(563)); // \f1 ���⿡���� ����� �� �����ϴ�.
			return false;
		}

		int petCost = 0;
		Object[] petList = pc.getPetList().values().toArray();
		for (Object pet : petList) {
			if (pet instanceof L1PetInstance) {
				if (((L1PetInstance) pet).getItemObjId() == itemObjectId) { // �̹� ������ �ִ� �ֿϵ���
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
		} else if (pc.isDragonknight()) { // ����
			charisma += 6;
		} else if (pc.isIllusionist()) { // ȯ����
			charisma += 6;
		}

		charisma -= petCost;
		int petCount = charisma / 6;
		if (petCount <= 0) {
			pc.sendPackets(new S_ServerMessage(489)); // ���������� �ϴ� �ֿϵ����� �ʹ� �����ϴ�.
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
			pc.sendPackets(new S_SystemMessage("������ �ν���"));
		}
	}

	private void MapFixKeyWand(L1PcInstance pc, int locX, int locY){
		String key = new StringBuilder().append(pc.getMapId()).append(locX).append(locY).toString();
		if (!pc.getMap().isCloseZone(locX, locY)){
			if (!MapFixKeyTable.getInstance().isLockey(key)){
				MapFixKeyTable.getInstance().storeLocFix(locX, locY, pc.getMapId());
				pc.sendPackets(new S_EffectLocation(locX, locY, 1815));
				Broadcaster.broadcastPacket(pc , new S_EffectLocation(locX, locY, 1815));
				pc.sendPackets(new S_SystemMessage("key�߰� ,x :" + locX + ",y :" + locY + ", mapId :" + pc.getMapId()));
			}
		}else{
			pc.sendPackets(new S_SystemMessage("������ǥ�� ���� �ƴմϴ�."));

			if (MapFixKeyTable.getInstance().isLockey(key)){
				MapFixKeyTable.getInstance().deleteLocFix(locX, locY, pc.getMapId());
				pc.sendPackets(new S_EffectLocation(locX, locY, 10));
				Broadcaster.broadcastPacket(pc , new S_EffectLocation(locX, locY, 10));
				pc.sendPackets(new S_SystemMessage("key���� ,x :" + locX + ",y :" + locY + ", mapId :" + pc.getMapId()));
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