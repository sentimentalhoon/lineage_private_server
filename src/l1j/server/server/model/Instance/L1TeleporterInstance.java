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

import java.util.Calendar;
import java.util.Random;
import java.util.logging.Logger;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1NpcTalkData;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.gametime.RealTime;
import l1j.server.server.model.gametime.RealTimeClock;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.npc.L1NpcHtml;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.serverpackets.S_SystemMessage;

//Referenced classes of package l1j.server.server.model:
//L1NpcInstance, L1Teleport, L1NpcTalkData, L1PcInstance,
//L1TeleporterPrices, L1TeleportLocations

public class L1TeleporterInstance extends L1NpcInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public L1TeleporterInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onAction(L1PcInstance player) {
		L1Attack attack = new L1Attack(player, this);
		attack.calcHit();
		attack.action();
	}

	@Override
	public void onTalkAction(L1PcInstance player) {
		int objid = getId();
		L1NpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(getNpcTemplate().get_npcId());
		int npcid = getNpcTemplate().get_npcId();
		L1Quest quest = player.getQuest();
		String htmlid = null;

		if (talking != null) {
			switch(npcid){
			case 50001:
				if (player.isElf()) {
					htmlid = "barnia3";
				} else if (player.isKnight() || player.isCrown()) {
					htmlid = "barnia2";
				} else if (player.isWizard() || player.isDarkelf()) {
					htmlid = "barnia1";
				}
				break;
			case 50014:
				if (player.isWizard()) { // ������
					if (quest.get_step(L1Quest.QUEST_LEVEL30) == 1
							&& !player.getInventory().checkItem(40579)) { // �� ������ ��
						htmlid = "dilong1";
					} else {
						htmlid = "dilong3";
					}
				}
				break;
			case 50016:
				if (player.getLevel() >= 13) htmlid = "zeno2";
				break;
			case 50031:
				if (player.isElf()) { // ������
					if (quest.get_step(L1Quest.QUEST_LEVEL45) == 2) {
						if (!player.getInventory().checkItem(40602)) { // ��� �÷�
							htmlid = "sepia1";
						}
					}
				}
				break;
			case 50043:
				if (quest.get_step(L1Quest.QUEST_LEVEL50) == L1Quest.QUEST_END) {
					htmlid = "ramuda2";
				} else if (quest.get_step(L1Quest.QUEST_LEVEL50) == 1) { // �𰡸��� ���ǰ� ���� ����
					if (player.isCrown()) { // ����
						if (_isNowDely) { // �ڷ���Ʈ ������
							htmlid = "ramuda4";
						} else {
							htmlid = "ramudap1";
						}
					} else { // ���� �̿�
						htmlid = "ramuda1";
					}
				} else {
					htmlid = "ramuda3";
				}
				break;
			case 50055:
				if (player.getLevel() >=13) htmlid = "drist1";	
				break;
			case 50069:
				if (!player.isDarkelf()) 			htmlid = "enya2";
				else if (player.getLevel() >= 13 ) 	htmlid = "enya4";
				break;
			case 70779:
				if (player.getGfxId().getTempCharGfx() == 1037) { // ���̾�Ʈ��Ʈ ����
					htmlid = "ants3";
				} else if (player.getGfxId().getTempCharGfx() == 1039) {// ���̾�Ʈ��Ʈ�Ҹ��� ����
					if (player.isCrown()) { // ����
						if (quest.get_step(L1Quest.QUEST_LEVEL30) == 1) {
							if (player.getInventory().checkItem(40547)) { // �ֹε��� ��ǰ
								htmlid = "antsn";
							} else {
								htmlid = "ants1";
							}
						} else { // Step1 �̿�
							htmlid = "antsn";
						}
					} else { // ���� �̿�
						htmlid = "antsn";
					}
				}
				break;
			case 70853:
				if (player.isElf()) { // ������
					if (quest.get_step(L1Quest.QUEST_LEVEL30) == 1) {
						if (!player.getInventory().checkItem(40592)) { // �������� ���ɼ�
							Random random = new Random(System.nanoTime());
							if (random.nextInt(100) < 50) { // 50%�� ��ũ��������
								htmlid = "fairyp2";
							} else { // ��ũ ������ ���� ����
								htmlid = "fairyp1";
							}
						}
					}
				}
				break;
			}
			// html ǥ��
			if (htmlid != null) { // htmlid�� �����ǰ� �ִ� ���
				player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
			} else {
				if (player.getLawful() < -1000) { // �÷��̾ ī��ƽ
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
				} else {
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
				}
			}
		} else {
			_log.finest((new StringBuilder()).append("No actions for npc id : ").append(objid).toString());
		}
	}

	@Override
	public void onFinalAction(L1PcInstance player, String action) {
		int objid = getId();
		L1NpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(getNpcTemplate().get_npcId());
		if (action.equalsIgnoreCase("teleportURL")) {
			L1NpcHtml html = new L1NpcHtml(talking.getTeleportURL());
			String[] price = null;
			int npcid = getNpcTemplate().get_npcId();
			switch(npcid){
			case 50015: // ���ϴ¼� ��ī
				price = new String[]{"1500"};
				break;
			case 50017: // ���ϴ� �� ���̽�
				price = new String[]{"50"};
				break;
			case 50020: // ��Ʈ ���ĸ�
				price = new String[]{ "50","50","50","120","120","120","120","180","180","200","200","600","7100"};
				break;
			case 50024: // �۷��� �ƽ���
				price = new String[]{ "75","75","75","180","180","270","270","270","360","360","360","300","300","750","10200" };
				break;
			case 50026: // �׸��� ���墡��� ����, ���� ����, �ǹ� ����Ʈ Ÿ�� ����
				price = new String[]{ "550","700","810"};
				break;
			case 50033: // ��� ���墡�׸��� ����, ���� ����, �ǹ� ����Ʈ Ÿ�� ����
				price = new String[]{ "560","720","560"};
				break;
			case 50035: // ����� ����Ʈ ����
				price = new String[]{ "210", "210", "420", "210" };
				break;
			case 450001868: // ����^�ڷ�����
				price = new String[] { "3500", "3500", "3500", "7000", "7000" };
				break;
			case 50036: // ��� ����
				price = new String[]{ "75","75","75","180","180","180","180","270","270","450","450","1050","11100" };
				break;
			case 50039:  // ���� ������
				price = new String[]{ "72","72","174","174","261","261","261","348","348","580","580","1160","11165" };
				break;
			case 50040:	// ���� ����Ʈ Ű��
				price = new String[]{ "210","420","210"};
				break;
			case 50044:  // �Ƶ� �ø��콺
			case 50046:  // �Ƶ� ��������
				price = new String[]{ "70","168","168","252","252","252","336","336","420","700","700","1260","10360" };
				break;
			case 50049:  // ���� ���墡�׸��� ����, ��� ����, �ǹ� ����Ʈ Ÿ�� ����
				price = new String[]{ "1150","980","590"};
				break;
			case 50051: // ����Ű���콺
				price = new String[]{ "75","180","270","270","360","360","360","450","450","750","750","1350","12000" };
				break;
			case 50054: // ���ٿ��Ʈ����
				price = new String[]{ "75","75","180","180","180","270","270","360","450","300","300","750","9750" };
				break;
			case 50056:  // ����縶�� ��Ʈ
				price = new String[]{"75","75","75","180","180","180","270","270","270","360","360","450","450","1050","10200"};
				break;
			case 50059:  // �ǹ� ����Ʈ Ÿ�� ���墡�׸��� ����, ��� ����, ���� ����
				price = new String[]{ "580","680","680"};
				break;
			case 50063:	// ��ũ ��� ����Ʈ Ű��
				price = new String[]{ "210","420","210"};
				break;
			case 50066: // ���̳׸���
				price = new String[]{ "990","450","400","550","400","710","350","680","1000","180","180","3200","6900" };
				break;
			case 50068:  // ��Ƴ뽺
				price = new String[]{ "1500","800","600","1800","1800","1000" };
				break;
			case 50072: // �����̵��� ��Ʒ���
				price = new String[]{ "2200","1800","1000","1600","2200","1200","1300","2000","2000" };			    
				break;
			case 50073: // �����̵��� ��ƺ���
				price = new String[]{ "380","850","290","290","290","180","480","150","150","380","480","380","850","1000" };	    
				break;
			case 50079: // ������ �ٴϿ�
				price = new String[]{ "550","550","550","600","600","600","650","700","750","750","500","500","700"};
				break;
			case 4208002: // ������ָ�
				break;
			case 4918000: // ��ī��� ������
				price = new String[]{ "50","50","50","50","120","120","180","180","180","240","240","400","400","800","7700" };  
				break;
			case 4919000: // �Ǻ����� ������
				price = new String[]{ "50","50","50","120","180","180","240","240","240","300","300","500","500","900","8000" };		    
				break;
			case 6000014:  // ������ ����
				price = new String[]{"14000"};
				break;
			case 6000016:  // �ų� �÷ζ�
				price = new String[]{"1000"};	
				break;
			default:
				price = new String[]{""};
			break;
			}
			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		}
		else if (action.equalsIgnoreCase("teleportURLA")) {

			String html = "";
			String[] price = null;
			int npcid = getNpcTemplate().get_npcId();
			if(npcid == 50079){
				html = "telediad3";
				price = new String[]{ "700","800","800","1000","10000" };
			}else if(npcid == 4918000){
				html = "dekabia3";
				price = new String[]{ "100","220","220","220","330","330","330","330","440","440" };
			}else if(npcid == 4919000){
				html = "sharial3";
				price = new String[]{ "220","330","330","330","440","440","550","550","550","550" };
			}else{
				price = new String[]{""};
			}
			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLB")) {
			String html = "guide_1_1";
			String[] price = null;

			price = new String[]{ "450","450","450","450" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLC")) {
			String html = "guide_1_2";
			String[] price = null;

			price = new String[]{ "465","465","465","465","1065","1065" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLD")) {
			String html = "guide_1_3";
			String[] price = null;

			price = new String[]{ "480","480","480","480","630","1080","630" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLE")) {
			String html = "guide_2_1";
			String[] price = null;

			price = new String[]{ "600","600","750","750" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLF")) {
			String html = "guide_2_2";
			String[] price = null;

			price = new String[]{ "615","615","915","765" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLG")) {
			String html = "guide_2_3";
			String[] price = null;

			price = new String[]{ "630","780","630","1080","930" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLH")) {
			String html = "guide_3_1";
			String[] price = null;

			price = new String[]{ "750","750","750","1200","1050" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLI")) {
			String html = "guide_3_2";
			String[] price = null;

			price = new String[]{ "765","765","765","765","1515","1215","915" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLJ")) {
			String html = "guide_3_3";
			String[] price = null;

			price = new String[]{ "780","780","780","780","780","1230","1080" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		} else if (action.equalsIgnoreCase("teleportURLK")) {
			String html = "guide_4";
			String[] price = null;

			price = new String[]{ "780","780","780","780","780","1230","1080" };			

			player.sendPackets(new S_NPCTalkReturn(objid, html, price));
		}
		if (action.startsWith("teleport")) {
			_log.finest((new StringBuilder()).append("Setting action to : ")
					.append(action).toString());
			doFinalAction(player, action);
		}
	}

	private void doFinalAction(L1PcInstance player, String action) {
		int objid = getId();

		int npcid = getNpcTemplate().get_npcId();
		String htmlid = null;
		boolean isTeleport = true;

		if (npcid == 50014) { // �� ��
			if (!player.getInventory().checkItem(40581)) { // �� ������ Ű
				isTeleport = false;
				htmlid = "dilongn";
			}
		} else if (npcid == 50043) { // Lambda
			if (_isNowDely) { // �ڷ���Ʈ ������
				isTeleport = false;
			}
		} else if (npcid == 50625) { // �����(Lv50 ����Ʈ ����� ���� 2 F)
			if (_isNowDely) { // �ڷ���Ʈ ������
				isTeleport = false;
			}
		}

		if (isTeleport) { // �ڷ���Ʈ ����
			try {
				//  ��źƮ��Ʈ����(���� Lv30 ����Ʈ)
				if (action.equalsIgnoreCase("teleport mutant-dungen_la")) {
					// 3 �Ž� �̳��� Pc
					for (L1PcInstance otherPc : L1World.getInstance().getVisiblePlayer(player, 3)) {						
						if (otherPc.getClanid() != 0 && otherPc.getClanid() == player.getClanid() && otherPc.getId() != player.getId()) {
							L1Teleport.teleport(otherPc, 32740, 32800, (short) 217, 5, true);
						}
					}
					L1Teleport.teleport(player, 32740, 32800, (short) 217, 5, true);
				}
				// �÷��� ���� ����(������ Lv30 ����Ʈ)
				else if (action.equalsIgnoreCase("teleport mage-quest-dungen_la")) {
					L1Teleport.teleport(player, 32791, 32788, (short) 201, 5, true);
				} else if (action.equalsIgnoreCase("teleport 29_la")) { // Lambda
					L1PcInstance kni = null;
					L1PcInstance elf = null;
					L1PcInstance wiz = null;
					// 3 �Ž� �̳��� Pc
					L1Quest quest = null;
					for (L1PcInstance otherPc : L1World.getInstance()
							.getVisiblePlayer(player, 3)) {
						quest = otherPc.getQuest();
						if (otherPc.isKnight() // ����Ʈ
								&& quest.get_step(L1Quest.QUEST_LEVEL50) == 1) { // �𰡸��� ���ǰ� ���� ����
							if (kni == null) {
								kni = otherPc;
							}
						} else if (otherPc.isElf() // ����
								&& quest.get_step(L1Quest.QUEST_LEVEL50) == 1) { // �𰡸��� ���ǰ� ���� ����
							if (elf == null) {
								elf = otherPc;
							}
						} else if (otherPc.isWizard() // ������
								&& quest.get_step(L1Quest.QUEST_LEVEL50) == 1) { // �𰡸��� ���ǰ� ���� ����
							if (wiz == null) {
								wiz = otherPc;
							}
						}
					}
					if (kni != null && elf != null && wiz != null) { // ��Ŭ���� ���߾��� �ִ�
						L1Teleport.teleport(player, 32723, 32850, (short) 2000, 2, true);
						L1Teleport.teleport(kni, 32750, 32851, (short) 2000, 6, true);
						L1Teleport.teleport(elf, 32878, 32980, (short) 2000, 6, true);
						L1Teleport.teleport(wiz, 32876, 33003, (short) 2000, 0, true);
						TeleportDelyTimer timer = new TeleportDelyTimer();
						GeneralThreadPool.getInstance().execute(timer);
					}
				} else if (action.equalsIgnoreCase("teleport barlog_la")) { // �����(Lv50 ����Ʈ ����� ���� 2 F)
					L1Teleport.teleport(player, 32755, 32844, (short) 2002, 5, true);
					TeleportDelyTimer timer = new TeleportDelyTimer();
					GeneralThreadPool.getInstance().execute(timer);
					
				} else if (action.equalsIgnoreCase("teleport ivoryTower")) { // �Ƶ��Ͻ�4����
					RealTime time = RealTimeClock.getInstance().getRealTime();
					int entertime = player.getIvoryTowerTime() % 1000;
					int enterday = player.getIvoryTowerTime() / 1000;
					int dayofyear = time.get(Calendar.DAY_OF_YEAR);

					if (entertime > 60 && enterday == dayofyear) {
						player.sendPackets(new S_ServerMessage(1522, "1"));// 1�ð� ��λ���ߴ�.
					htmlid = "";
					return;
					} else {
					if (enterday < dayofyear)
						player.setIvoryTowerTime(dayofyear * 1000);					
					int x = 32901 ;
					int y = 32765 ;
					L1Teleport.teleport(player, x, y, (short) 78, 5, true); // ���ž4�� �ڷ���Ʈ,��ġ����										
					if ((60 - entertime) < 60){
						int c = 60 - entertime;
					player.sendPackets(new S_ServerMessage(1527, "" + c + ""));// �г��Ҵ�.				
					}			
					}	
				} else if (action.equalsIgnoreCase("teleport ivorytower4")) { // ���� ���ž4�� 
					 if (player.getInventory().checkItem(L1ItemId.ADENA, 7000)) {  // �Ƶ���
				      player.getInventory().consumeItem(L1ItemId.ADENA, 7000); 
					RealTime time = RealTimeClock.getInstance().getRealTime();
					int entertime = player.getIvoryTowerTime() % 1000;
					int enterday = player.getIvoryTowerTime() / 1000;
					int dayofyear = time.get(Calendar.DAY_OF_YEAR);

					if (entertime > 60 && enterday == dayofyear) {
						player.sendPackets(new S_ServerMessage(1522, "1"));// 1�ð� ��λ���ߴ�.
					htmlid = "";
					return;
					} else {
					if (enterday < dayofyear)
						player.setIvoryTowerTime(dayofyear * 1000);					
					int x = 32901;
					int y = 32765;
					L1Teleport.teleport(player, x, y, (short) 78, 5, true); // ���ž4�� �ڷ���Ʈ										
					}
					if ((60 - entertime) < 60){
						int c = 60 - entertime;
					player.sendPackets(new S_ServerMessage(1527, "" + c + ""));// �г��Ҵ�.				
					}		
					}
				} else if (action.equalsIgnoreCase("teleport ivorytower7")) { // ���ž7�� �����ּ���
					if (player.getInventory().checkItem(L1ItemId.ADENA, 7000)) {  //�Ƶ���
					      player.getInventory().consumeItem(L1ItemId.ADENA, 7000);
					RealTime time = RealTimeClock.getInstance().getRealTime();
					int entertime = player.getIvoryTowerTime() % 1000;
					int enterday = player.getIvoryTowerTime() / 1000;
					int dayofyear = time.get(Calendar.DAY_OF_YEAR);
					if (entertime > 60 && enterday == dayofyear) {
						player.sendPackets(new S_ServerMessage(1522, "1"));// 1�ð� ��λ���ߴ�.
					htmlid = "";
					return;
					} else {
					if (enterday < dayofyear)
						player.setIvoryTowerTime(dayofyear * 1000);							
					L1Teleport.teleport(player, 32809, 32868, (short) 81, 5, true); // ���ž7�� �ڷ���Ʈ
					}
					if ((60 - entertime) < 60){
						int c = 60 - entertime;
					player.sendPackets(new S_ServerMessage(1527, "" + c + ""));// �г��Ҵ�.				
					}		
					}
				
				} else if (action.equalsIgnoreCase("teleport giranD")) { // �������
					RealTime time = RealTimeClock.getInstance().getRealTime();
					int entertime = player.getGdungeonTime() % 1000;
					int enterday = player.getGdungeonTime() / 1000;
					int dayofyear = time.get(Calendar.DAY_OF_YEAR);

					if(entertime > 180 && enterday == dayofyear){
						player.sendPackets(new S_ServerMessage(1522, "3"));// 3�ð� ��� ����ߴ�.
						htmlid ="";
						return;
					} else {
						if(enterday < dayofyear)
							player.setGdungeonTime(dayofyear * 1000);
						L1Teleport.teleport(player, 32806, 32735, (short) 53, 5, true);
						
						int a = entertime % 60;
						if(a == 0){
							int b = (180 - entertime) / 60;
							player.sendPackets(new S_ServerMessage(1526, ""+b+""));// b �ð� ���Ҵ�.
						} else if ((180 - entertime) < 60){
							int c = 180 - entertime;
							player.sendPackets(new S_ServerMessage(1527, ""+c+""));// �� ���Ҵ�.
							
						 

						}
					}
				}
			} catch (Exception e) {
			}
		}
		if (htmlid != null) { // ǥ���ϴ� html�� �ִ� ���
			player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
		}
	}

	class TeleportDelyTimer implements Runnable {

		public TeleportDelyTimer() {
		}

		public void run() {
			try {
				_isNowDely = true;
				Thread.sleep(900000); // 15��
			} catch (Exception e) {
				_isNowDely = false;
			}
			_isNowDely = false;
		}
	}

	private boolean _isNowDely = false;
	private static Logger _log = Logger
	.getLogger(l1j.server.server.model.Instance.L1TeleporterInstance.class
			.getName());

}