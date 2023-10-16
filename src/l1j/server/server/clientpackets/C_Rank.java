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
import java.util.Random;         // 생존
import java.util.logging.Level;
import java.util.logging.Logger;
import server.LineageClient;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1War;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;         // 생존
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SkillSound;               // 생존
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;       // 생존
import l1j.server.server.utils.FaceToFace;
// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket
public class C_Rank extends ClientBasePacket {
 private static final String C_RANK = "[C] C_Rank";
 private static Logger _log = Logger.getLogger(C_Rank.class.getName());
 public C_Rank(byte abyte0[], LineageClient clientthread) throws Exception {
  super(abyte0);
  int type = readC(); // ?
  int rank = readC();
  
  L1PcInstance pc = clientthread.getActiveChar();
  L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
  String clanname = pc.getClanname();
  
  if (pc == null) {return;} 
  
  if ((clan == null) && (type < 5)) {return;}
    
  int Enchantlvl = 0;  
		switch (type) {
		case 0: // 혈맹에 속한 인원수의 변동이 있었을 경우
			//pc.sendPackets(new S_PacketBox(pc, S_PacketBox.PLEDGE_ONE));
			pc.sendPackets(new S_PacketBox(pc, S_PacketBox.PLEDGE_TWO)); // 임시
			break;
		case 1://계급
			String name = readS();
			L1PcInstance targetPc = L1World.getInstance().getPlayer(name);
			if (rank < 1 && 3 < rank) {
				// 랭크를 변경하는 사람의 이름과 랭크를 입력해 주세요. [랭크=가디안, 일반, 견습]
				pc.sendPackets(new S_ServerMessage(781));
				return;
			}

			if (pc.isCrown()) { // 군주
				if (pc.getId() != clan.getLeaderId()) { // 혈맹주
					pc.sendPackets(new S_ServerMessage(785)); // 당신은 이제 군주가 아닙니다.
					return;
				}
			} else {
				pc.sendPackets(new S_ServerMessage(518)); // 이 명령은 혈맹의 군주만을 이용할 수 있습니다.
				return;
			}

			if (targetPc != null) { // 온라인중
				if (pc.getClanid() == targetPc.getClanid()) { // 같은 크란
					try {
						targetPc.setClanRank(rank);
						targetPc.save(); // DB에 캐릭터 정보를 기입한다
						String rankString = "$772";
						if (rank == L1Clan.CLAN_RANK_PROBATION) {
							rankString = "$774";
						} else if (rank == L1Clan.CLAN_RANK_PUBLIC) {
							rankString = "$773";
						} else if (rank == L1Clan.CLAN_RANK_GUARDIAN) {
							rankString = "$772";
						}
						targetPc.sendPackets(new S_ServerMessage(784, rankString)); // 당신의 랭크가%s로 변경되었습니다.
					} catch (Exception e) {
						_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				} else {
					pc.sendPackets(new S_ServerMessage(414)); // 같은 혈맹원이 아닙니다.
					return;
				}
			} else { // 오프 라인중
				L1PcInstance restorePc = CharacterTable.getInstance().restoreCharacter(name);
				if (restorePc != null
						&& restorePc.getClanid() == pc.getClanid()) { // 같은 크란
					try {
						restorePc.setClanRank(rank);
						restorePc.save(); // DB에 캐릭터 정보를 기입한다
					} catch (Exception e) {
						_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				} else {
					pc.sendPackets(new S_ServerMessage(109, name)); // %0라는 이름의 사람은 없습니다.
					return;
				}
			}
			break;
		
		case 2://목록
			if (pc.getClan().getAlliance() != null) {
				pc.sendPackets(new S_PacketBox(pc, S_PacketBox.ALLIANCE_LIST));
			} else {
				pc.sendPackets(new S_ServerMessage(1233));
			}
			break;
		case 3://가입
			L1PcInstance alliancePc = FaceToFace.faceToFace(pc);
			if (pc.getLevel() < 25 || !pc.isCrown()) {
				pc.sendPackets(new S_ServerMessage(1206));// 25레벨이상 혈맹 군주만 동맹신청을 할 수 있습니다. 또한 연합 군주는 동맹을 맺을 수 없습니다.
				return;
			}
			if (pc.getClan().getAlliance() != null) {
				pc.sendPackets(new S_ServerMessage(1202));// 이미 동맹에 가입된 상태입니다.
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInWar(clanname)) {
					pc.sendPackets(new S_ServerMessage(1234)); // 전쟁중에는 동맹에 가입할 수 없습니다.
					return;
				}
			}
			if(alliancePc == null) {
				pc.sendPackets(new S_ServerMessage(93)); // 거기에는 아무도 없습니다.
				return;
			}
			if(!alliancePc.isCrown()) {
				pc.sendPackets(new S_ServerMessage(92, alliancePc.getName())); // \f1%0은 프린스나 프린세스가 아닙니다.
				return;
			}
			if(alliancePc.getClan() == null || alliancePc.getClanid() == 0){
				pc.sendPackets(new S_ServerMessage(90, alliancePc.getName())); // \f1%0은 혈맹을 창설하고 있지 않는 상태입니다.
				return;
			}
			alliancePc.setTempID(pc.getId());
			alliancePc.sendPackets(new S_Message_YN(223, pc.getName()));
			
			break;
		case 4://탈퇴
			if(pc.getClan() == null || !pc.isCrown() 
					|| pc.getClan().getAlliance() == null) {
				return;
			}
			for (L1War war : L1World.getInstance().getWarList()) {
				if (war.CheckClanInWar(clanname)) {
					pc.sendPackets(new S_ServerMessage(1203)); // 전쟁중에는 동맹을 탈퇴할 수 없습니다.
					return;
				}
			}
			if (clan.getAlliance() != null) {
				pc.sendPackets(new S_Message_YN(1210, "")); //정말로 동맹을 탈퇴하시겠습니까? (Y/N)
			} else {
				pc.sendPackets(new S_ServerMessage(1233)); // 동맹이 없습니다.
			}
			break;
			  ///////////////////생존의 외침
		  case 5:
		          try {
		            int NewHp = 0;
		    
		            Random random = new Random();
		    
		            if (pc.get_food() >= 225) {
		              try {
		                Enchantlvl = pc.getEquipSlot().getWeapon().getEnchantLevel();
		              } catch (Exception e) {
		                //pc.sendPackets(new S_SystemMessage("\\fY무기를 착용해야 생존의 외침을 사용할 수 있습니다."));
		               pc.sendPackets(new S_ServerMessage(1973));
		               return;
		              }
		    
		              if (1800000L < System.currentTimeMillis() - pc.getSurvivalCry()) {
		                if (Enchantlvl <= 6) {
		                  int[] probability = { 20, 30, 40 };
		                  int percent = probability[random.nextInt(probability.length)];
		                  NewHp = pc.getCurrentHp() + pc.getMaxHp() / 100 * percent;
		    
		                  if (NewHp > pc.getMaxHp()) {
		                    NewHp = pc.getMaxHp();
		                  }
		    
		                  pc.setCurrentHp(NewHp);
		                  pc.sendPackets(new S_SystemMessage("\\fY생존의 외침을 사용하여 최대HP " + percent + "%를 회복하였습니다."));
		                  pc.sendPackets(new S_SkillSound(pc.getId(), 8684));
		                  Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8684));
		                  pc.set_food(0);
		                  pc.sendPackets(new S_PacketBox(11, pc.get_food()));
		                  pc.setSurvivalCry(System.currentTimeMillis());
		                } else if ((Enchantlvl >= 7) && (Enchantlvl <= 8)) {
		                  int[] probability = { 30, 40, 50 };
		                  int percent = probability[random.nextInt(probability.length)];
		    
		                  NewHp = pc.getCurrentHp() + pc.getMaxHp() / 100 * percent;
		    
		                  if (NewHp > pc.getMaxHp()) {
		                    NewHp = pc.getMaxHp();
		                 }
		    
		                  pc.setCurrentHp(NewHp);
		                  pc.sendPackets(new S_SystemMessage("\\fY생존의 외침을 사용하여 최대HP " + percent + "%를 회복하였습니다."));
		                  pc.sendPackets(new S_SkillSound(pc.getId(), 8685));
		                  Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8685));
		                  pc.set_food(0);
		                 pc.sendPackets(new S_PacketBox(11, pc.get_food()));
		                 pc.setSurvivalCry(System.currentTimeMillis());
		                } else if ((Enchantlvl >= 9) && (Enchantlvl <= 10)) {
		                  int[] probability = { 50, 60 };
		                  int percent = probability[random.nextInt(probability.length)];
		    
		                  NewHp = pc.getCurrentHp() + pc.getMaxHp() / 100 * percent;
		   
		                 if (NewHp > pc.getMaxHp()) {
		                    NewHp = pc.getMaxHp();
		                  }
		    
		                  pc.setCurrentHp(NewHp);
		                  pc.sendPackets(new S_SystemMessage("\\fY생존의 외침을 사용하여 최대HP " + percent + "%를 회복하였습니다."));
		                  pc.sendPackets(new S_SkillSound(pc.getId(), 8773));
		                  Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8773));
		                 pc.set_food(0);
		                  pc.sendPackets(new S_PacketBox(11, pc.get_food()));
		                  pc.setSurvivalCry(System.currentTimeMillis());
		                } else if (Enchantlvl >= 11) {
		                  NewHp = pc.getCurrentHp() + pc.getMaxHp() / 100 * 7;
		    
		                  if (NewHp > pc.getMaxHp()) {
		                    NewHp = pc.getMaxHp();
		                  }
		    
		                  pc.setCurrentHp(NewHp);
		                  pc.sendPackets(new S_SystemMessage("\\fY생존의 외침을 사용하여 최대HP 70%를 회복하였습니다."));
		                  pc.sendPackets(new S_SkillSound(pc.getId(), 8686));
		                  Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8686));
		                  pc.set_food(0);
		                  pc.sendPackets(new S_PacketBox(11, pc.get_food()));
		                  pc.setSurvivalCry(System.currentTimeMillis());
		                }
		              } else {
		                long time = 1800L - (System.currentTimeMillis() - pc.getSurvivalCry()) / 1000L;
		    
		                long minute = time / 60L;
		                long second = time % 60L;
		    
		                if (minute >= 29L) {
		                  pc.sendPackets(new S_SystemMessage("\\fY생존의 외침은 " + minute + "분 " + second + "초 후에 재사용 가능합니다."));
		                  return;
		                }
		    
		                NewHp = pc.getCurrentHp() + pc.getMaxHp() / 100 * (30 - (int)minute);
		    
		                if (NewHp > pc.getMaxHp()) {
		                  NewHp = pc.getMaxHp();
		                }
		    
		                pc.setCurrentHp(NewHp);
		                pc.sendPackets(new S_SystemMessage("\\fY생존의 외침을 사용하여 최대HP " + (30 - (int)minute) + "%를 회복하였습니다."));
		                pc.sendPackets(new S_SkillSound(pc.getId(), 8683));
		                Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8683));
		                pc.set_food(0);
		                pc.sendPackets(new S_PacketBox(11, pc.get_food()));
		               pc.setSurvivalCry(System.currentTimeMillis());
		              }
		            } else {
		              pc.sendPackets(new S_SystemMessage("\\fY생존의 외침은 배고픔게이지 100% 채운 시점부터,"));
		              //pc.sendPackets(new S_SystemMessage("\\fY30분뒤에 사용가능합니다."));
		              pc.sendPackets(new S_ServerMessage(1974));
		            }
		          } catch (Exception e) {
		          }
		        case 6:
		         try {
		            if (pc.get_food() >= 225) {
		              try {
		                Enchantlvl = pc.getEquipSlot().getWeapon().getEnchantLevel();
		             } catch (Exception e) {
		                pc.sendPackets(new S_SystemMessage("\\fY무기를 착용해야 생존의 외침을 사용할 수 있습니다."));
		                return;
		             }
		    
		              if (Enchantlvl <= 6) {
		                pc.sendPackets(new S_SkillSound(pc.getId(), 8684));
		                Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8684));
		              } else if ((Enchantlvl >= 7) && (Enchantlvl <= 8)) {
		                pc.sendPackets(new S_SkillSound(pc.getId(), 8685));
		                Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8685));
		             } else if ((Enchantlvl >= 9) && (Enchantlvl <= 10)) {
		                pc.sendPackets(new S_SkillSound(pc.getId(), 8773));
		                Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8773));
		              } else if (Enchantlvl >= 11) {
		                pc.sendPackets(new S_SkillSound(pc.getId(), 8686));
		                Broadcaster.broadcastPacket(pc, new S_SkillSound(pc.getId(), 8686));
		             }
		            }
		           else {
		              pc.sendPackets(new S_SystemMessage("\\fY생존의 외침은 배고픔게이지 100%를 채워야 충전됩니다,"));
		             
		            }
		          }
		          catch (Exception e) {
		          }
		        }
		     }
		 @Override
		 public String getType() {
		  return C_RANK;
		 }
		}
	