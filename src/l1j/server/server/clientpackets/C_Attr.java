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

package l1j.server.server.clientpackets;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.SpecialEventHandler;
import l1j.server.GameSystem.GhostHouse;
import l1j.server.GameSystem.PetRacing;
import l1j.server.GameSystem.MiniGame.DeathMatch;
import l1j.server.server.OxTimeController;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.datatables.BuddyTable;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.datatables.HouseTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Buddy;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1ChatParty;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Party;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.L1ZombieMod;
import l1j.server.server.model.Instance.L1BuffNpcInstance;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_ChangeName;
import l1j.server.server.serverpackets.S_CharTitle;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_OwnCharStatus2;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Resurrection;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_Trade;
import l1j.server.server.serverpackets.S_bonusstats;
import l1j.server.server.templates.L1Alliance;
import l1j.server.server.templates.L1House;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Pet;
import l1j.server.server.utils.CommonUtil;
import server.LineageClient;

public class C_Attr extends ClientBasePacket {

	private static final Logger _log = Logger.getLogger(C_Attr.class.getName());
	private static final String C_ATTR = "[C] C_Attr";

	private static final int HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
	private static final int HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };

	public C_Attr(byte abyte0[], LineageClient clientthread) throws Exception {
		super(abyte0);
		int i = readH();
		int c;
		String name;

		L1PcInstance pc = clientthread.getActiveChar();

		switch (i) {	
		/**by �ǵ��� ���̵���Ż**/
		case 2923: //���̵�
			c = readC();
			if(c == 0){
				pc.sendPackets(new S_SystemMessage("�巡�� ��Ż ������ ��ҵǾ����ϴ�."));
			} else if (c == 1) {
				if(pc.DragonPortalLoc[0] != 0){
					Collection<L1PcInstance> templist = L1World.getInstance().getAllPlayers();
					L1PcInstance[] list = templist.toArray(new L1PcInstance[templist.size()]);
					int count = 0;
					for (L1PcInstance player : list) {
						if (player == null)
							continue;
						if (player.getMapId() == pc.DragonPortalLoc[2]) {
							count += 1;
						}
					}
					if(count >= 32){
						pc.sendPackets(new S_ServerMessage(1536));// �ο��� �������� �� �̻� ������ �� �����ϴ�.
						return;
					}
					L1Teleport.teleport(pc, pc.DragonPortalLoc[0], pc.DragonPortalLoc[1], (short) pc.DragonPortalLoc[2], 5, true);
				}
			}
			pc.DragonPortalLoc[0] = 0;
			pc.DragonPortalLoc[1] = 0;
			pc.DragonPortalLoc[2] = 0;
			break;
			/**by �ǵ��� ���̵���Ż**/
		case 78: // ����� ���� ����
			c = readC();
			if (c== 0){
				pc.sendPackets(new S_ServerMessage(35)); // ���
			} else if (c== 1){
				CommonUtil.tryCount(pc, 32766, 32834, 5120, 0, 2, 0);
				OxTimeController.getInstance().add(pc);
				try{
					int PolyId = 6180 + CommonUtil.random(64);
					L1PolyMorph.doPoly(pc, PolyId, 900, L1PolyMorph.MORPH_BY_KEPLISHA);
					pc.set_QuizResult(0);
					pc.save();
				} catch (Exception e){ }
			}
			break;
		case 847:
			c = readC();
			if(c == 0){
				pc.sendPackets(new S_ServerMessage(256)); // �ŷ��� ��ҵǾ����ϴ�.
			}else if(c == 1){
				for (int i2 = 1; i2 < 8; i2++) {
					if (WarTimeController.getInstance().isNowWar(i2)) {
						pc.sendPackets(new S_ServerMessage(1512)); // �������� �������Դϴ�.
						return;
					}
				}
				if (pc.getInventory().checkItem(L1ItemId.ADENA, 10000000)){					
					pc.getInventory().consumeItem(L1ItemId.ADENA, 10000000);
					L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("[******] ["+ pc.getName() + "]���� ��ü������ �����Ͽ����ϴ�."));
					for (L1PcInstance _client : L1World.getInstance().getAllPlayers()) {
						if(_client.isPrivateShop() 
								|| _client.noPlayerCK
								|| _client.isDead()){
							continue;
						} else {
							_client.sendPackets(new S_Message_YN(403, "[��ü����]")); // %0%o ������ϴ�.
						}
					}
				} else {
					pc.sendPackets(new S_ServerMessage(189));
				}
			}
			break;
		case 403:
			c = readC();
			if(c == 0){
				pc.sendPackets(new S_ServerMessage(256)); // �ŷ��� ��ҵǾ����ϴ�.
			}else if(c == 1){
				if(pc.isPrivateShop() 
						|| pc.noPlayerCK
						|| pc.isDead()){
				} else {
					SpecialEventHandler.getInstance().doGoodAllBuff(pc, 1);
				}
			}
			break;
		case 456:
			c = readC();
			if(c == 0){
				pc.sendPackets(new S_ServerMessage(256)); // �ŷ��� ��ҵǾ����ϴ�.
			}else if(c == 1){
				if(pc.isPrivateShop() 
						|| pc.noPlayerCK
						|| pc.isDead()){
				} else {
					SpecialEventHandler.getInstance().doGoodAllBuff(pc, 0);
				}
			}
			break;
		case 422:
			c = readC();
			if(c == 0){
				pc.sendPackets(new S_ServerMessage(423, "[ O/X ���� ]"));
			}else if(c == 1){
				if (OxTimeController.getInstance().getOxStart()
						&& OxTimeController.getInstance().getOxOpen()) {
					OxTimeController.getInstance().add(pc);
					pc.sendPackets(new S_SystemMessage("[******] "+pc.getName()+" �� O/X QUIZ �� �����ϼ̽��ϴ�"));
					CommonUtil.tryCount(pc, 32799, 32872, 2006, 0, 2, 0);
					// ������ �ʾҴٸ�
				} else {
					pc.sendPackets(new S_SystemMessage("���� O/X QUIZ �� ������ �ʾҽ��ϴ�."));
				}
			}
			break;
			////������� by-kingdom
		case 391:
			c = readC();
			if(c == 0){
				pc.sendPackets(new S_SystemMessage("������ ������ �ź��մϴ�."));
			}else if(c == 1){
				if(pc.getZombieMod()!=0){
					pc.sendPackets(new S_SystemMessage("�̹��������Դϴ�."));
				}else{
					pc.setBackHp(pc.getMaxHp());
					L1ZombieMod.getInstance().addMember(pc);
					pc.setZombieMod(1);
					L1Teleport.teleport(pc, 32736 , 32799 , (short) 34, 2, true);
					pc.sendPackets(new S_SystemMessage("�����忡 �����մϴ�."));
				}
			}
			break;
			///////////////////������� by-kingdom
		case 97: // %0�� ���Ϳ� ������������ �ֽ��ϴ�. �³��մϱ�? (Y/N)
			c = readC();
			L1PcInstance joinPc = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			pc.setTempID(0);
			if (joinPc != null) {
				if (c == 0) { // No
					joinPc.sendPackets(new S_ServerMessage(96, pc.getName())); // \f1%0�� ����� ��û�� �����߽��ϴ�.
				} else if (c == 1) { // Yes
					int clan_id = pc.getClanid();
					String clanName = pc.getClanname();
					L1Clan clan = L1World.getInstance().getClan(clanName);
					if (clan != null) {
						int maxMember = 0;
						int charisma = pc.getAbility().getTotalCha();
						boolean lv45quest = false;
						if (pc.getQuest().isEnd(L1Quest.QUEST_LEVEL45)) {
							lv45quest = true;
						}
						if (pc.getLevel() >= 50) { // Lv50 �̻�
							if (lv45quest == true) { // Lv45 ����Ʈ Ŭ��� ���� ����
								maxMember = charisma * 9;
							} else {
								maxMember = charisma * 3;
							}
						} else { // Lv50 �̸�
							if (lv45quest == true) { // Lv45 ����Ʈ Ŭ��� ���� ����
								maxMember = charisma * 6;
							} else {
								maxMember = charisma * 2;
							}
						}
						if (Config.MAX_CLAN_MEMBER > 0) { // Clan �ο����� ������ ���� �־�
							maxMember = Config.MAX_CLAN_MEMBER;
						}

						if (joinPc.getClanid() == 0) { // ũ���̰���
							if (maxMember <= clan.getClanMemberList().size()) {//clanMembersName.length) { // �� ���� ����
								joinPc.sendPackets(new S_ServerMessage(188, pc.getName())); // %0�� ����� ���Ϳ����μ� �޾Ƶ��� ���� �����ϴ�.
								return;
							}
							for (L1PcInstance clanMembers : clan.getOnlineClanMember()) {
								clanMembers.sendPackets(new S_ServerMessage(94, joinPc.getName())); // \f1%0�� ������ �Ͽ����μ� �޾Ƶ鿩�����ϴ�.
							}

							joinPc.setClanid(clan_id);
							joinPc.setClanname(clanName);
							joinPc.setClanRank(L1Clan.CLAN_RANK_PUBLIC);
							joinPc.setTitle("");
							joinPc.sendPackets(new S_CharTitle(joinPc.getId(), ""));
							Broadcaster.broadcastPacket(joinPc, new S_CharTitle(joinPc.getId(), ""));
							joinPc.save(); // DB�� ĳ���� ������ �����Ѵ�
							clan.addClanMember(joinPc.getName(), joinPc.getClanRank());
							pc.sendPackets(new S_PacketBox(pc, S_PacketBox.PLEDGE_REFRESH_PLUS));
							joinPc.sendPackets(new S_ServerMessage(95, clanName)); // \f1%0 ���Ϳ� �����߽��ϴ�.
						} else { // ũ�� ������ ���� ����(ũ�� ����)
							if (Config.CLAN_ALLIANCE) {
								changeClan(clientthread, pc, joinPc, maxMember);
							} else {
								joinPc.sendPackets(new S_ServerMessage(89)); // \f1����� ���� ���Ϳ� �����ϰ� �ֽ��ϴ�.
							}
						}
					}
				}
			}
			break;

		case 217: // %0������%1�� ����� ���Ͱ��� ������ �ٶ�� �ֽ��ϴ�. ���￡ ���մϱ�? (Y/N)
		case 221: // %0������ �׺��� �ٶ�� �ֽ��ϴ�. �޾Ƶ��Դϱ�? (Y/N)
		case 222: // %0������ ������ ������ �ٶ�� �ֽ��ϴ�. �����մϱ�? (Y/N)
			c = readC();
			L1PcInstance enemyLeader = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			if (enemyLeader == null) {
				return;
			}
			pc.setTempID(0);
			String clanName = pc.getClanname();
			String enemyClanName = enemyLeader.getClanname();
			if (c == 0) { // No
				if (i == 217) {
					enemyLeader.sendPackets(new S_ServerMessage(236, clanName)); // %0������ ����� ���Ͱ��� ������ �����߽��ϴ�.
				} else if (i == 221 || i == 222) {
					enemyLeader.sendPackets(new S_ServerMessage(237, clanName)); // %0������ ����� ������ �����߽��ϴ�.
				}
			} else if (c == 1) { // Yes
				if (i == 217) {
					L1War war = new L1War();
					war.handleCommands(2, enemyClanName, clanName); // ������ ����
				} else if (i == 221 || i == 222) {
					for (L1War war : L1World.getInstance().getWarList()) { // ���� ����Ʈ�� ���
						if (war.CheckClanInWar(clanName)) { // ��ũ���� ���� �ִ� ������ �߰�
							if (i == 221) {
								war.SurrenderWar(enemyClanName, clanName); // �׺�
							} else if (i == 222) {
								war.CeaseWar(enemyClanName, clanName); // ����
							}
							break;
						}
					}
				}
			}
			break;

		case 223: // %0%s ������ ���մϴ�. �޾Ƶ��̽ðڽ��ϱ�? (Y/N)
			L1PcInstance AlliancePc = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			String PcClanName = pc.getClanname();
			String AllianceClanName = AlliancePc.getClanname();
			if(readC() == 1){
				if(pc.getClan().getAlliance() != null){
					L1Clan[] clans = pc.getClan().getAlliance().getClans();
					for(int i1 = 0; i1 < clans.length; i1++){
						AlliancePc.getClan().announcement_message(null, 1200, clans[i].getClanName(), null);
						clans[i].announcement_message(null, 1200, AlliancePc.getClan().getClanName(), null);
					}
					ClanTable.getInstance().ChangeAlliance(AlliancePc.getClan(), pc.getClan().getAlliance());
				} else {
					AlliancePc.getClan().announcement_message(null, 1200, pc.getClan().getClanName(), null);
					pc.getClan().announcement_message(null, 1200, AlliancePc.getClan().getClanName(), null);
					pc.sendPackets(new S_ServerMessage(224, AllianceClanName, PcClanName));// %0 ���Ͱ� %1 ������ ������ �ξ����ϴ�.
					AlliancePc.sendPackets(new S_ServerMessage(224, PcClanName, AllianceClanName));
					L1Alliance alliance = ClanTable.getInstance().NewAlliance();
					ClanTable.getInstance().ChangeAlliance(pc.getClan(), alliance);
					ClanTable.getInstance().ChangeAlliance(AlliancePc.getClan(), alliance);
				}
			} else {
				AlliancePc.sendPackets(new S_ServerMessage(1198));
			}
			break;

		case 1210: // ������ ������ Ż���Ͻðڽ��ϱ�? (Y/N)
			if(readC() == 1) {						
				pc.getClan().getAlliance().AllianceMessage(1204, pc.getClan().getClanName(), null);
				if (pc.getClan().getAlliance().get_size() > 2) {
					ClanTable.getInstance().ChangeAlliance(pc.getClan(), null);
				} else {
					pc.getClan().getAlliance().delete();
				}
			}
			break;		

		case 252: // %0%s�� ��Ű� �������� �ŷ��� �ٶ�� �ֽ��ϴ�. �ŷ��մϱ�? (Y/N)
			c = readC();
			L1Object trading_partner = L1World.getInstance().findObject(pc.getTradeID());
			if (trading_partner != null) {
				if(trading_partner instanceof L1PcInstance){
					L1PcInstance target = (L1PcInstance)trading_partner;
					if (c == 0) { // No
						target.sendPackets(new S_ServerMessage(253, pc.getName())); // %0%d�� ��Ű��� �ŷ��� ������ �ʾҽ��ϴ�.
						pc.setTradeID(0);
						target.setTradeID(0);
					} else if (c == 1) { // Yes
						if (pc.getLocation().getTileLineDistance(target.getLocation()) > 1) {
							pc.setMultiTrading(true);
							target.setMultiTrading(true);
						}

						pc.sendPackets(new S_Trade(target.getName()));
						target.sendPackets(new S_Trade(pc.getName()));
					}
				}else if(trading_partner instanceof L1BuffNpcInstance){
					L1BuffNpcInstance target = (L1BuffNpcInstance)trading_partner;
					if (c == 0) { // No
						pc.setTradeID(0);
						target.setTradeID(0);
					} else if (c == 1) { // Yes
						pc.sendPackets(new S_Trade(target.getName()));
						target.setTradeID(pc.getId()); // ����� ������Ʈ ID�� ������ �д�
					}
				}
			}
			break;

		case 321: // �� ��Ȱ�ϰ� �ͽ��ϱ�? (Y/N)
			c = readC();
			L1PcInstance resusepc1 = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			pc.setTempID(0);
			if (resusepc1 != null) { // ��Ȱ ��ũ��
				if (c == 0) { // No
					;
				} else if (c == 1) { // Yes
					pc.sendPackets(new S_SkillSound(pc.getId(), '\346'));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), '\346'));
					pc.resurrect(pc.getMaxHp() / 2);
					pc.setCurrentHp(pc.getMaxHp() / 2);
					//pc.startHpRegeneration();
					//pc.startMpRegeneration();
					pc.startMpRegenerationByDoll();
					pc.sendPackets(new S_Resurrection(pc, resusepc1, 0));
					Broadcaster.broadcastPacket(pc, new S_Resurrection(pc, resusepc1, 0));
					pc.sendPackets(new S_CharVisualUpdate(pc));
					Broadcaster.broadcastPacket(pc, new S_CharVisualUpdate(pc));
				}
			}
			break;

		case 322: // �� ��Ȱ�ϰ� �ͽ��ϱ�? (Y/N)
			c = readC();
			L1PcInstance resusepc2 = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			pc.setTempID(0);
			if (resusepc2 != null) { // �ູ�� ��Ȱ ��ũ��, ���ڷ�ũ��, �׷���Ÿ���ڷ�ũ��
				if (c == 0) { // No
					;
				} else if (c == 1) { // Yes
					pc.sendPackets(new S_SkillSound(pc.getId(), '\346'));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), '\346'));
					pc.resurrect(pc.getMaxHp());
					pc.setCurrentHp(pc.getMaxHp());
					//pc.startHpRegeneration();
					//pc.startMpRegeneration();
					pc.startHpRegenerationByDoll();
					pc.startMpRegenerationByDoll();
					pc.sendPackets(new S_Resurrection(pc, resusepc2, 0));
					Broadcaster.broadcastPacket(pc, new S_Resurrection(pc, resusepc2, 0));
					pc.sendPackets(new S_CharVisualUpdate(pc));
					Broadcaster.broadcastPacket(pc, new S_CharVisualUpdate(pc));
					// EXP �ν�Ʈ �ϰ� �ִ�, G-RES�� �� �� �ִ�, EXP �ν�Ʈ �� ���
					// ��θ� ä��� ��츸 EXP ����
					if (pc.getExpRes() == 1 && pc.isGres() && pc.isGresValid()) {
						pc.resExp();
						pc.setExpRes(0);
						pc.setGres(false);
					}
				}
			}
			break;

		case 325: // ������ �̸��� ������ �ּ��䣺
			c = readC(); // ?
			name = readS();
			L1PetInstance pet = (L1PetInstance) L1World.getInstance().findObject(pc.getTempID());
			pc.setTempID(0);
			renamePet(pet, name);
			break;

		case 512: // ���� �̸���?
			c = readC(); // ?
			name = readS();
			int houseId = pc.getTempID();
			pc.setTempID(0);
			if (name.length() <= 16) {
				L1House house = HouseTable.getInstance().getHouseTable(houseId);
				house.setHouseName(name);
				HouseTable.getInstance().updateHouse(house); // DB�� ������
			} else {
				pc.sendPackets(new S_ServerMessage(513)); // ���� �̸��� �ʹ� ��ϴ�.
			}
			break;

		case 622: 
			c = readC();   
			BuddyTable buddyTable = BuddyTable.getInstance();
			L1Buddy buddyList = buddyTable.getBuddyTable(pc.getId());
			L1PcInstance target2 = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			pc.setTempID(0);
			String name1 = pc.getName();
			String name2 = target2.getName();
			if (target2 != null) { // �ִٸ�
				if (c == 0) { // No
					target2.sendPackets(new S_ServerMessage(96, pc.getName())); // \f1%0%s ����� ��û�� �����Ͽ����ϴ�.
				} else if (c == 1) { // Yes
					buddyList.add(pc.getId(), name1);
					buddyTable.addBuddy(target2.getId(), pc.getId(), name1);
					target2.sendPackets(new S_SystemMessage(name1 + "���� ģ�� ��� �Ǿ����ϴ�."));
					pc.sendPackets(new S_SystemMessage(name2 + "�Կ��� ģ�� ����� �Ǿ����ϴ�."));
				}
			}
			break; 
		case 630: 
			c = readC();
			L1PcInstance fightPc = (L1PcInstance) L1World.getInstance().findObject(pc.getFightId());
			if (c == 0) {
				pc.setFightId(0);
				fightPc.setFightId(0);
				fightPc.sendPackets(new S_ServerMessage(631, pc.getName()));
			} else if (c == 1) {
				fightPc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL,	fightPc.getFightId(), fightPc.getId()));
				pc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, pc.getFightId(), pc.getId()));
			}
			break;

		case 653: // ��ȥ�� �ϸ�(��) ���� ����� �����ϴ�. ��ȥ�� �ٶ��ϱ�? (Y/N)
			c = readC();
			L1PcInstance target653 = (L1PcInstance) L1World.getInstance().findObject(pc.getPartnerId());
			if (c == 0) { // No
				return;
			} else if (c == 1) { // Yes
				if (target653 != null) {
					target653.setPartnerId(0);
					target653.save();
					target653.sendPackets(new S_ServerMessage(662));
				} else {
					CharacterTable.getInstance().updatePartnerId(pc.getPartnerId());
				}
			}
			pc.setPartnerId(0);
			pc.save();
			pc.sendPackets(new S_ServerMessage(662));		
			break;
		case 654: // %0%s��Ű� ��ȥ �ϰ� �;��ϰ� �ֽ��ϴ�. %0�� ��ȥ�մϱ�? (Y/N)
			c = readC();
			L1PcInstance partner = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
			pc.setTempID(0);
			if (partner != null) {
				if (c == 0) { // No
					partner.sendPackets(new S_ServerMessage(656, pc.getName())); // %0%s�� ��Ű��� ��ȥ�� �����߽��ϴ�.
				} else if (c == 1) { // Yes
					pc.setPartnerId(partner.getId());
					pc.save();
					//pc.sendPackets(new S_ServerMessage(790)); // ����� �ູ ��(��)����, �� ���� ��ȥ�� �߽��ϴ�.
					//pc.sendPackets(new S_ServerMessage(655, partner.getName())); // �����մϴ�! %0�� ��ȥ�߽��ϴ�.
					//pc.sendPackets(new S_SkillSound(pc.getId(), 2059));
					Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 2059));
					partner.setPartnerId(pc.getId());
					partner.save();
					//partner.sendPackets(new S_ServerMessage(790)); // ����� �ູ ��(��)����, �� ���� ��ȥ�� �߽��ϴ�.
					//partner.sendPackets(new S_ServerMessage(655, pc.getName())); // �����մϴ�! %0�� ��ȥ�߽��ϴ�.
					//partner.sendPackets(new S_SkillSound(partner.getId(), 2059));
					Broadcaster.broadcastPacket(partner, new S_SkillSound(partner.getId(), 2059));
					L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(790, pc.getName() + "�԰� " + partner.getName() + "��"));
				}
			}
			break;
			// �� ũ��
		case 729: // ���Ϳ��� ����� �ڷ���Ʈ ��Ű���� �ϰ� �ֽ��ϴ�. ���մϱ�? (Y/N)
			c = readC();
			if (c == 0) {}
			else if (c == 1) { // Yes
				callClan(pc);
			}
			break;

		case 738:// ����ġ�� ȸ���Ϸ���%0�� �Ƶ����� �ʿ��մϴ�. ����ġ�� ȸ���մϱ�?
			c = readC();
			if (c == 0) {} 
			else if (c == 1 && pc.getExpRes() == 1) { // Yes
				int cost = 0;
				int level = pc.getLevel();
				int lawful = pc.getLawful();
				if (level < 45) {
					cost = level * level * 100;
				} else {
					cost = level * level * 200;
				}
				if (lawful >= 0) {
					cost = (cost / 2);
				}
				cost *= 2;
				if (pc.getInventory().consumeItem(L1ItemId.ADENA, cost)) {
					pc.resExpToTemple();
					pc.setExpRes(0);
				} else {
					pc.sendPackets(new S_ServerMessage(189));// \f1�Ƶ����� �����մϴ�.
				}
			}
			break;

		case 951: // ä�� ��Ƽ �ʴ븦 �㰡�մϱ�? (Y/N)
			c = readC();
			L1PcInstance chatPc = (L1PcInstance) L1World.getInstance().findObject(pc.getPartyID());
			if (chatPc != null) {
				if (c == 0) { // No
					chatPc.sendPackets(new S_ServerMessage(423, pc.getName())); // %0�� �ʴ븦 �ź��߽��ϴ�.
					pc.setPartyID(0);
				} else if (c == 1) { // Yes
					if (chatPc.isInChatParty()) {
						if (chatPc.getChatParty().isVacancy() || chatPc.isGm()) {
							chatPc.getChatParty().addMember(pc);
						} else {
							chatPc.sendPackets(new S_ServerMessage(417)); // �� �̻� ��Ƽ ����� �޾Ƶ��� �� �����ϴ�.
						}
					} else {
						L1ChatParty chatParty = new L1ChatParty();
						chatParty.addMember(chatPc);
						chatParty.addMember(pc);
						chatPc.sendPackets(new S_ServerMessage(424, pc.getName())); // %0�� ��Ƽ�� �����ϴ�.
					}
				}
			}
			break;

		case 953: // ��Ƽ �ʴ븦 �㰡�մϱ�? (Y/N)
		case 954: // �й� ��Ƽ �ʴ� �Ѵ�~
			c = readC();
			L1PcInstance target = (L1PcInstance) L1World.getInstance().findObject(pc.getPartyID());
			if (target != null) {
				if (c == 0) { // No
					target.sendPackets(new S_ServerMessage(423, pc.getName())); // %0�� �ʴ븦 �ź��߽��ϴ�.
					pc.setPartyID(0);
				} else if (c == 1) { // Yes
					if (target.isInParty()) { // �ʴ��ְ� ��Ƽ��
						if (target.getParty().isVacancy() || target.isGm()) { // ��Ƽ�� �� ���� �ִ�
							target.getParty().addMember(pc);
						} else { // ��Ƽ�� �� ���� ����
							target.sendPackets(new S_ServerMessage(417)); // �� �̻� ��Ƽ ����� �޾Ƶ��� �� �����ϴ�.
						}
					} else {
						// �ʴ��ְ� ��Ƽ���� �ƴϴ�
						L1Party party = new L1Party();
						party.addMember(target);
						party.addMember(pc);
						target.sendPackets(new S_ServerMessage(424, pc.getName())); // %0�� ��Ƽ�� �����ϴ�.
					}
				}
			}
			break;

		case 1256:	// ����忡 �����Ͻðڽ��ϱ�? (Y/N)
			c = readC();		
			if(c == 0){
				miniGameRemoveEnterMember(pc);
			}else if(c == 1){
				if (pc.getInventory().checkItem(L1ItemId.ADENA, 1000)){					
					pc.getInventory().consumeItem(L1ItemId.ADENA, 1000);
					if (pc.isInParty())	pc.getParty().leaveMember(pc);
					L1SkillUse l1skilluse = new L1SkillUse();
					l1skilluse.handleCommands(pc, L1SkillId.CANCELLATION, pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_LOGIN);

					if (GhostHouse.getInstance().isEnterMember(pc)){
						if (GhostHouse.getInstance().isPlayingNow()){
							pc.sendPackets(new S_ServerMessage(1182));
							return;
						}											
						for (L1DollInstance doll : pc.getDollList().values()) {
							doll.deleteDoll();
						}
						GhostHouse.getInstance().addPlayMember(pc);
						L1Teleport.teleport(pc, 32722, 32830, (short) 5140, 2, true);
					}
					else if(PetRacing.getInstance().isEnterMember(pc)){
						if (PetRacing.getInstance().isPlay()){
							pc.sendPackets(new S_ServerMessage(1182));
							return;
						}
						for (L1DollInstance doll : pc.getDollList().values()) {
							doll.deleteDoll();
						}
						pc.setPetRacing(true);
						PetRacing.getInstance().removeEnterMember(pc);
						PetRacing.getInstance().addPlayMember(pc);
						L1Teleport.teleport(pc, 32768, 32848, (short) 5143, 5, true);
					}
				}else{
					pc.sendPackets(new S_ServerMessage(189));
					miniGameRemoveEnterMember(pc);
				}
			}
			break;

		case 1268:	// ������ġ�� �����Ͻðڽ��ϱ�? (Y/N)
			c = readC();
			if(c == 0){

			}
			else if(c == 1)	DeathMatch.getInstance().addPlayMember(pc);
			break;
		case 479: // ��� �ɷ�ġ�� ����ŵ�ϱ�? (str, dex, int, con, wis, cha)
			if (readC() == 1) {
				String s = readS();
				final int BONUS_ABILITY = pc.getAbility().getBonusAbility();

				if (!(pc.getLevel() - 50 > BONUS_ABILITY))	return;

				if (s.toLowerCase().equals("str".toLowerCase())) {
					if (pc.getAbility().getStr() < 35) {
						pc.getAbility().addStr((byte) 1); // ���� STRġ��+1
						pc.getAbility().setBonusAbility(BONUS_ABILITY + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));						
						pc.save(); // DB�� ĳ���� ������ �����Ѵ�
					} else {
						pc.sendPackets(new S_ServerMessage(481)); // �ϳ��� �ɷ�ġ�� �ִ�ġ�� 25�Դϴ�. �ٸ� �ɷ�ġ�� ������ �ּ���
					}
				} else if (s.toLowerCase().equals("dex".toLowerCase())) {
					if (pc.getAbility().getDex() < 35) {
						pc.getAbility().addDex((byte) 1);
						pc.resetBaseAc();
						pc.getAbility().setBonusAbility(BONUS_ABILITY + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save();
					} else {
						pc.sendPackets(new S_ServerMessage(481));
					}
				} else if (s.toLowerCase().equals("con".toLowerCase())) {
					if (pc.getAbility().getCon() < 35) {
						pc.getAbility().addCon((byte) 1);
						pc.getAbility().setBonusAbility(BONUS_ABILITY + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save();
					} else {
						pc.sendPackets(new S_ServerMessage(481));
					}
				} else if (s.toLowerCase().equals("int".toLowerCase())) {
					if (pc.getAbility().getInt() < 35) {
						pc.getAbility().addInt((byte) 1);
						pc.getAbility().setBonusAbility(BONUS_ABILITY + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save();
					} else {
						pc.sendPackets(new S_ServerMessage(481));
					}
				} else if (s.toLowerCase().equals("wis".toLowerCase())) {
					if (pc.getAbility().getWis() < 35) {
						pc.getAbility().addWis((byte) 1);
						pc.resetBaseMr();
						pc.getAbility().setBonusAbility(BONUS_ABILITY + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save();
					} else {
						pc.sendPackets(new S_ServerMessage(481));
					}
				} else if (s.toLowerCase().equals("cha".toLowerCase())) {
					if (pc.getAbility().getCha() < 35) {
						pc.getAbility().addCha((byte) 1);
						pc.getAbility().setBonusAbility(BONUS_ABILITY + 1);
						pc.sendPackets(new S_OwnCharStatus2(pc));
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.save();
					} else {
						pc.sendPackets(new S_ServerMessage(481));
					}
				}
				pc.CheckStatus();
				if (pc.getLevel() >= 51 && pc.getLevel() - 50 > pc.getAbility().getBonusAbility()) {
					if ((pc.getAbility().getStr() + pc.getAbility().getDex() + pc.getAbility().getCon()
							+ pc.getAbility().getInt() + pc.getAbility().getWis() + pc.getAbility().getCha()) < 150) {
						pc.sendPackets(new S_bonusstats(pc.getId(), 1));
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private void changeClan(LineageClient clientthread, L1PcInstance pc, L1PcInstance joinPc, int maxMember) {
		int clanId = pc.getClanid();
		String clanName = pc.getClanname();
		L1Clan clan = L1World.getInstance().getClan(clanName);
		int clanNum = clan.getClanMemberList().size();

		int oldClanId = joinPc.getClanid();
		String oldClanName = joinPc.getClanname();
		L1Clan oldClan = L1World.getInstance().getClan(oldClanName);
		int oldClanNum = oldClan.getClanMemberList().size();
		if (clan != null && oldClan != null && joinPc.isCrown() &&  joinPc.getId() == oldClan.getLeaderId()) {
			if (maxMember < clanNum + oldClanNum) { // �� ���� ����
				joinPc.sendPackets(new S_ServerMessage(188, pc.getName())); // %0�� ����� ���Ϳ����μ� �޾Ƶ��� ���� �����ϴ�.
				return;
			}
			L1PcInstance clanMember[] = clan.getOnlineClanMember();
			for (int cnt = 0; cnt < clanMember.length; cnt++) {
				clanMember[cnt].sendPackets(new S_ServerMessage(94, joinPc.getName())); // \f1%0�� ������ �Ͽ����μ� �޾Ƶ鿩�����ϴ�.
			}

			for (int i = 0; i < oldClan.getClanMemberList().size() ; i++) {
				L1PcInstance oldClanMember = L1World.getInstance().getPlayer(oldClan.getClanMemberList().get(i).name);
				if (oldClanMember != null) { // �¶������� ��ũ�� ���					
					oldClanMember.setClanid(clanId);
					oldClanMember.setClanname(clanName);
					// ���� ���տ� ������ ���ִ� �����
					// ���ְ� ���� �� ���Ϳ��� ���޾�
					if (oldClanMember.getId() == joinPc.getId()) {
						oldClanMember.setClanRank(L1Clan.CLAN_RANK_GUARDIAN);
					} else {
						oldClanMember.setClanRank(L1Clan.CLAN_RANK_PROBATION);
					}
					try {
						// DB�� ĳ���� ������ �����Ѵ�
						oldClanMember.save();
					} catch (Exception e) {
						_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
					clan.addClanMember(oldClanMember.getName(), oldClanMember.getClanRank());
					oldClanMember.sendPackets(new S_ServerMessage(95, clanName)); // \f1%0 ���Ϳ� �����߽��ϴ�.
				} else { // ���� �������� ��ũ�� ���
					try {
						L1PcInstance offClanMember = CharacterTable
								.getInstance().restoreCharacter(oldClan.getClanMemberList().get(i).name);
						offClanMember.setClanid(clanId);
						offClanMember.setClanname(clanName);
						offClanMember.setClanRank(L1Clan.CLAN_RANK_PROBATION);
						offClanMember.save();
						clan.addClanMember(offClanMember.getName(), offClanMember.getClanRank());
					} catch (Exception e) {
						_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
			}
			// �������� ����
			String emblem_file = String.valueOf(oldClanId);
			File file = new File("emblem/" + emblem_file);
			file.delete();
			ClanTable.getInstance().deleteClan(oldClanName);
		}
	}

	private static void renamePet(L1PetInstance pet, String name) {
		if (pet == null || name == null) {
			throw new NullPointerException();
		}

		int petItemObjId = pet.getItemObjId();
		L1Pet petTemplate = PetTable.getInstance().getTemplate(petItemObjId);
		if (petTemplate == null) {
			throw new NullPointerException();
		}

		L1PcInstance pc = (L1PcInstance) pet.getMaster();
		if (PetTable.isNameExists(name)) {
			pc.sendPackets(new S_ServerMessage(327)); // ���� �̸��� ���� �����ϰ� �ֽ��ϴ�.
			return;
		}
		L1Npc l1npc = NpcTable.getInstance().getTemplate(pet.getNpcId());
		if (!(pet.getName().equalsIgnoreCase(l1npc.get_name())) ) {
			pc.sendPackets(new S_ServerMessage(326));
			return;
		}
		pet.setName(name);
		petTemplate.set_name(name);
		PetTable.getInstance().storePet(petTemplate); // DB�� ������
		L1ItemInstance item = pc.getInventory().getItem(pet.getItemObjId());
		pc.getInventory().updateItem(item); 
		pc.sendPackets(new S_ChangeName(pet.getId(), name));
		Broadcaster.broadcastPacket(pc, new S_ChangeName(pet.getId(), name));
	}

	private void callClan(L1PcInstance pc) {
		L1PcInstance callClanPc = (L1PcInstance) L1World.getInstance().findObject(pc.getTempID());
		boolean isInWarArea = false;
		short mapId = callClanPc.getMapId();
		int castleId = L1CastleLocation.getCastleIdByArea(callClanPc);

		pc.setTempID(0);
		if (callClanPc == null || callClanPc.isPrivateShop()) { return; }

		if (!pc.getMap().isEscapable() && !pc.isGm()) {
			pc.sendPackets(new S_ServerMessage(647));
			L1Teleport.teleport(pc, pc.getLocation(), pc.getMoveState().getHeading(), false);
			return;
		}

		if (pc.getId() != callClanPc.getCallClanId()) { return; }

		if (castleId != 0) {
			isInWarArea = true;
			if (WarTimeController.getInstance().isNowWar(castleId)) {
				isInWarArea = false;
			}
		}

		if (mapId != 0 && mapId != 4 && mapId != 304 || isInWarArea) {			
			pc.sendPackets(new S_ServerMessage(547));
			return;
		}

		L1Map map = callClanPc.getMap();
		int locX = callClanPc.getX();
		int locY = callClanPc.getY();
		int heading = callClanPc.getCallClanHeading();
		locX += HEADING_TABLE_X[heading];
		locY += HEADING_TABLE_Y[heading];
		heading = (heading + 4) % 4;

		boolean isExsistCharacter = false;
		L1Character cha = null;
		for (L1Object object : L1World.getInstance().getVisibleObjects(callClanPc, 1)) {
			if (object instanceof L1Character) {
				cha = (L1Character) object;
				if (cha.getX() == locX && cha.getY() == locY && cha.getMapId() == mapId) {
					isExsistCharacter = true;
					break;
				}
			}
		}

		if (locX == 0 && locY == 0 || !map.isPassable(locX, locY) || isExsistCharacter) {
			pc.sendPackets(new S_ServerMessage(627));
			return;
		}
		L1Teleport.teleport(pc, locX, locY, mapId, heading, true, L1Teleport.CALL_CLAN, true);
	}
	private void miniGameRemoveEnterMember(L1PcInstance pc){
		if (GhostHouse.getInstance().isEnterMember(pc))
			GhostHouse.getInstance().removeEnterMember(pc);
		else if (PetRacing.getInstance().isEnterMember(pc))
			PetRacing.getInstance().removeEnterMember(pc);
	}
	@Override
	public String getType() { return C_ATTR; }
}