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

import server.LineageClient;
import server.manager.eva;
import l1j.server.server.Account;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_ReturnedStat;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.utils.CalcStat;

public class C_ReturnStaus extends ClientBasePacket {
	public C_ReturnStaus(byte[] decrypt, LineageClient client) {
		super(decrypt);
		int type = readC();
		L1PcInstance pc = client.getActiveChar();
		if(type == 1) {
			short init_hp = 0, init_mp = 0;

			int str = readC();
			int intel = readC();
			int wis = readC();
			int dex = readC();
			int con = readC();
			int cha = readC();
			int total = 0;
			total = str+dex+con+wis+cha+intel;

			if(!pc.getAbility().isNormalAbility(pc.getClassId(), pc.getLevel(), pc.getHighLevel(), total)) {
				pc.sendPackets(new S_Disconnect());
				return;
			}

			pc.getAbility().reset();

			pc.getAbility().setBaseStr((byte)str);
			pc.getAbility().setBaseInt((byte)intel);
			pc.getAbility().setBaseWis((byte)wis);
			pc.getAbility().setBaseDex((byte)dex);
			pc.getAbility().setBaseCon((byte)con);
			pc.getAbility().setBaseCha((byte)cha);

			pc.setLevel(1);

			if (pc.isCrown()) { // CROWN
				init_hp = 14;
				switch (pc.getAbility().getBaseWis()) {
				case 11:
					init_mp = 2;
					break;
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 3;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 4;
					break;
				default:
					init_mp = 2;
					break;
				}
			} else if (pc.isKnight()) { // KNIGHT
				init_hp = 16;
				switch (pc.getAbility().getBaseWis()) {
				case 9:
				case 10:
				case 11:
					init_mp = 1;
					break;
				case 12:
				case 13:
					init_mp = 2;
					break;
				default:
					init_mp = 1;
					break;
				}
			} else if (pc.isElf()) { // ELF
				init_hp = 15;
				switch (pc.getAbility().getBaseWis()) {
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 4;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 6;
					break;
				default:
					init_mp = 4;
					break;
				}
			} else if (pc.isWizard()) { // WIZ
				init_hp = 12;
				switch (pc.getAbility().getBaseWis()) {
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 6;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 8;
					break;
				default:
					init_mp = 6;
					break;
				}
			} else if (pc.isDarkelf()) { // DE
				init_hp = 12;
				switch (pc.getAbility().getBaseWis()) {
				case 10:
				case 11:
					init_mp = 3;
					break;
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 4;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 6;
					break;
				default:
					init_mp = 3;
					break;
				}
			} else if (pc.isDragonknight()) { // ����
				init_hp = 16;
				init_mp = 2;
			} else if (pc.isIllusionist()) { // ȯ����
				init_hp = 14;
				switch (pc.getAbility().getBaseWis()) {
				case 10:
				case 11:
				case 12:
				case 13:
				case 14:
				case 15:
					init_mp = 5;
					break;
				case 16:
				case 17:
				case 18:
					init_mp = 6;
					break;
				default:
					init_mp = 5;
					break;
				}
			}
			pc.addBaseMaxHp((short) (init_hp - pc.getBaseMaxHp()));
			pc.addBaseMaxMp((short) (init_mp - pc.getBaseMaxMp()));
			pc.getAC().setAc(10);
			pc.sendPackets(new S_SPMR(pc));
			pc.sendPackets(new S_OwnCharStatus(pc));
			pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP));
		} else if(type == 2) {
			int levelup = readC();
			if (CheckAbillity(pc)) {
				switch(levelup){
				case 0: statup(pc); pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP)); break;
				case 1: pc.getAbility().addStr((byte) 1); statup(pc); pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP)); break;
				case 2: pc.getAbility().addInt((byte) 1); statup(pc); pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP)); break;
				case 3: pc.getAbility().addWis((byte) 1); statup(pc); pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP)); break;
				case 4: pc.getAbility().addDex((byte) 1); statup(pc); pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP)); break;
				case 5: pc.getAbility().addCon((byte) 1); statup(pc); pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP)); break;
				case 6: pc.getAbility().addCha((byte) 1); statup(pc); pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP)); break;
				case 7: 
					if(pc.getLevel()+10 < pc.getHighLevel()){
						for(int m = 0; m < 10; m++)
							statup(pc);
						pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP));
					}
					break;
				case 8:
					int statusup = readC();
					switch(statusup){
					case 1: pc.getAbility().addStr((byte) 1); break;
					case 2: pc.getAbility().addInt((byte) 1); break;
					case 3: pc.getAbility().addWis((byte) 1); break;
					case 4: pc.getAbility().addDex((byte) 1); break;
					case 5: pc.getAbility().addCon((byte) 1); break;
					case 6: pc.getAbility().addCha((byte) 1); break;
					}
					if(pc.getAbility().getElixirCount() > 0) {
						pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.END));
					} else {
						try {
							if(pc.getLevel() >= 51)	pc.getAbility().setBonusAbility(pc.getLevel() - 50);
							else					pc.getAbility().setBonusAbility(0);
							if(pc.getLevel() >= 51) { // 2��:�����ɸ� ���ʰ��� �׹��׹���

								pc.setExp(pc.getReturnStat());
								pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.END));
								pc.sendPackets(new S_OwnCharStatus(pc));
								pc.sendPackets(new S_OwnCharAttrDef(pc));
								pc.sendPackets(new S_SPMR(pc));
								pc.setCurrentHp(pc.getMaxHp()/2);//���ݾ���
								pc.setCurrentMp(pc.getMaxMp()/2);
								pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
								pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
								L1Teleport.teleport(pc, 32612, 32734, (short)4, 5, true);
								pc.setReturnStat(0);
								pc.save();
								pc.CheckStatus();
								int Level = pc.getLevel();
								int Str = pc.getAbility().getStr();
								int Dex = pc.getAbility().getDex();
								int Con = pc.getAbility().getCon();
								int Int = pc.getAbility().getInt();
								int Wis = pc.getAbility().getWis();
								int Cha = pc.getAbility().getCha();
								int BaseMaxHp = pc.getBaseMaxHp();
								int BaseMaxMp = pc.getBaseMaxMp();
								eva.writeMessage(-7, pc.getName() + "[" + Level + "]�� " + "HP: " +BaseMaxHp + " / MP: " + BaseMaxMp + " / STR : " + Str + " / DEX : " + Dex
										+ " / CON : " + Con + " / INT : " + Int + "WIS : " + Wis + " / CHA : " + Cha);

								if (!pc.isGm() && hpbugFix(pc) == true){
									eva.writeMessage(-7, pc.getName() +" ["+ pc.getAccountName()+ "] ���� : " + pc.getLevel() + " HighLevel : " + pc.getHighLevel());
									Account.ban(pc.getAccountName());	// ������ BAN��Ų��.
									pc.sendPackets(new S_Disconnect());
								}     //�߰�
								if (pc.getLevel() > pc.getHighLevel()){
									eva.writeMessage(-7, pc.getName() +" ["+ pc.getAccountName()+ "] ���� : " + pc.getLevel() + " HighLevel : " + pc.getHighLevel());
									Account.ban(pc.getAccountName());	// ������ BAN��Ų��.
									pc.sendPackets(new S_Disconnect());
								}
							}
							else
							{
								return;
							}
						} catch (Exception exception) {}
					}
					break;
				}
			} else {
				pc.setReturnStat(1);
				pc.sendPackets(new S_Disconnect());
			}
		} else if(type == 3) { // ���� �ʱ�ȭ�� ������ ó��
			try{
				int str = readC();
				int intel = readC();
				int wis = readC();
				int dex = readC();
				int con = readC();
				int cha = readC();

				int Level = pc.getLevel();
				int BaseMaxHp = pc.getBaseMaxHp();
				int BaseMaxMp = pc.getBaseMaxMp();
				int Str = pc.getAbility().getStr();
				int Int = pc.getAbility().getInt();
				int Wis = pc.getAbility().getWis();
				int Dex = pc.getAbility().getDex();
				int Con = pc.getAbility().getCon();
				int Cha = pc.getAbility().getCha();
				if (con < Con || str < Str || intel < Int || wis < Wis || dex < Dex || cha < Cha) {
					pc.setReturnStat(1);
					pc.sendPackets(new S_Disconnect());
				} else {
					pc.getAbility().addStr((byte) (str - pc.getAbility().getStr()));
					pc.getAbility().addInt((byte) (intel - pc.getAbility().getInt()));
					pc.getAbility().addWis((byte) (wis - pc.getAbility().getWis()));
					pc.getAbility().addDex((byte) (dex - pc.getAbility().getDex()));
					pc.getAbility().addCon((byte) (con - pc.getAbility().getCon()));
					pc.getAbility().addCha((byte) (cha - pc.getAbility().getCha()));
					if (pc.getLevel() >= 51)	pc.getAbility().setBonusAbility(pc.getLevel() - 50);
					else					pc.getAbility().setBonusAbility(0);
					if (pc.getLevel() >= 51) { // 2��:�����ɸ� ���ʰ��� �׹��׹���
						pc.setExp(pc.getReturnStat());
						pc.sendPackets(new S_OwnCharStatus(pc));
						pc.sendPackets(new S_OwnCharAttrDef(pc));
						pc.setCurrentHp(pc.getMaxHp()/2);
						pc.setCurrentMp(pc.getMaxMp()/2);
						pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
						pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
						pc.sendPackets(new S_ReturnedStat(pc, 4));
						L1Teleport.teleport(pc, 32612, 32734, (short)4, 5, true);			
						pc.setReturnStat(0);
						pc.save();
						pc.CheckStatus();

						eva.writeMessage(-7, pc.getName() + "[" + Level + "]�� " + "HP: " +BaseMaxHp + " / MP: " + BaseMaxMp + " / STR : " + Str + " / DEX : " + Dex
								+ " / CON : " + Con + " / INT : " + Int + "WIS : " + Wis + " / CHA : " + Cha);

						if (!pc.isGm() && hpbugFix(pc) == true){
							eva.writeMessage(-7, pc.getName() +" ["+ pc.getAccountName()+ "] ���� : " + pc.getLevel() + " HighLevel : " + pc.getHighLevel());
							Account.ban(pc.getAccountName());	// ������ BAN��Ų��.
							pc.sendPackets(new S_Disconnect());
						}     //�߰�
						if (pc.getLevel() > pc.getHighLevel()){
							eva.writeMessage(-7, pc.getName() +" ["+ pc.getAccountName()+ "] ���� : " + pc.getLevel() + " HighLevel : " + pc.getHighLevel());
							Account.ban(pc.getAccountName());	// ������ BAN��Ų��.
							pc.sendPackets(new S_Disconnect());
						}
					} else {
						return;
					}
				}
			} catch (Exception exception) {}
		}
	}

	private boolean hpbugFix(L1PcInstance pc) {
		boolean hpbug = false;

		int charType = pc.getType();
		int baseMaxHp = pc.getBaseMaxHp();
		byte baseCon = pc.getAbility().getCon();

		if (charType == 0) {
			int calCon = 10; 
			int lvlUpHp = (baseMaxHp - 14) / (pc.getLevel() - 1);
			//���������� - ��1�� �⺻ HP / ���緹�� - 1
			// ���緹�� -1 �� �⺻�� 1�� HP(14)�� ���־��⶧���� �̷�����
			int defaul = (baseCon - calCon - 1) + (int)((baseCon - calCon)/5) + 11;
			// ���� 11�� ���� ������ ������ 0~1�� �ִ밪 1��, calCon+10�� ���ذ�
			switch(baseCon - calCon){
			case 0 : case 1 :
				if(lvlUpHp > 12) hpbug = true; break;
			case 2 : case 3 :
				if(lvlUpHp > 13) hpbug = true; break;
			case 4 : case 5 :
				if(lvlUpHp > 14) hpbug = true; break;
			case 6 :
				if(lvlUpHp > 16) hpbug = true; break;
			case 7 :
				if(lvlUpHp > 17) hpbug = true; break;
			case 8 :
				if(lvlUpHp > 18) hpbug = true; break;
			default :
				baseMaxHp -= 900; //���̽����� �ִ�� �Ͽ������ 50�������� �ִ���
				int defUpHp = (baseMaxHp - 14) / (pc.getLevel() - 51);
				if(defUpHp > defaul) hpbug = true; break;
			}
		} else if (charType == 1) {
			int calCon = 14; 
			int lvlUpHp = (baseMaxHp - 16) / (pc.getLevel() - 1);
			//���������� - ��1�� �⺻ HP / ���緹�� - 1
			// ���緹�� -1 �� �⺻�� 1�� HP(16)�� ���־��⶧���� �̷�����
			int defaul = 24 + (int)((baseCon - calCon)/3);
			// 24�� ���� �⺻ HP 9 + �����ִ�� 1 + calCon 14
			switch(baseCon - calCon){
			case 0 :
				if(lvlUpHp > 19) hpbug = true; break;
			case 1 :
				if(lvlUpHp > 20) hpbug = true; break;
			case 2 :
				if(lvlUpHp > 21) hpbug = true; break;
			case 3 :
				if(lvlUpHp > 22) hpbug = true; break;
			case 4 : 
				if(lvlUpHp > 24) hpbug = true; break;
			default :
				baseMaxHp -= 1200; //���̽����� �ִ�� �Ͽ������ 50�������� �ִ���
				int defUpHp = (baseMaxHp - 16) / (pc.getLevel() - 51);
				if(defUpHp > defaul) hpbug = true; break;
			}
		} else if (charType == 2) {
			int calCon = 12; 
			int lvlUpHp = (baseMaxHp - 15) / (pc.getLevel() - 1);
			//���������� - ��1�� �⺻ HP / ���緹�� - 1
			// ���緹�� -1 �� �⺻�� 1�� HP(15)�� ���־��⶧���� �̷�����
			int defaul = 17 + (int)((baseCon - calCon)/3);
			// 17�� ���� �⺻ HP 6 + �����ִ�� 1 + calCon 12 - 2
			switch(baseCon - calCon){
			case 0 :
				if(lvlUpHp > 13) hpbug = true; break;
			case 1 : case 2 : case 3 :
				if(lvlUpHp > 14) hpbug = true; break;
			case 4 :
				if(lvlUpHp > 15) hpbug = true; break;
			case 5 :
				if(lvlUpHp > 16) hpbug = true; break;
			case 6 :
				if(lvlUpHp > 17) hpbug = true; break;
			default :
				baseMaxHp -= 850; //���̽����� �ִ�� �Ͽ������ 50�������� �ִ���
				int defUpHp = (baseMaxHp - 15) / (pc.getLevel() - 51);
				if(defUpHp > defaul) hpbug = true; break;
			}
		} else if (charType == 3) {
			int calCon = 12; 
			int lvlUpHp = (baseMaxHp - 12) / (pc.getLevel() - 1);
			//���������� - ��1�� �⺻ HP / ���緹�� - 1
			// ���緹�� -1 �� �⺻�� 1�� HP(12)�� ���־��⶧���� �̷�����
			int defaul = 15 + (int)((baseCon - calCon)/3);
			// 15�� ���� �⺻ HP 6 + �����ִ�� 1 + calCon 12 - 2
			switch(baseCon - calCon){
			case 0 : case 1 :
				if(lvlUpHp > 11) hpbug = true; break;
			case 2 : case 3 :
				if(lvlUpHp > 12) hpbug = true; break;
			case 4 :
				if(lvlUpHp > 13) hpbug = true; break;
			case 5 :
				if(lvlUpHp > 14) hpbug = true; break;
			case 6 :
				if(lvlUpHp > 15) hpbug = true; break;
			default :
				baseMaxHp -= 750; //���̽����� �ִ�� �Ͽ������ 50�������� �ִ���
				int defUpHp = (baseMaxHp - 12) / (pc.getLevel() - 51);
				if(defUpHp > defaul) hpbug = true; break;
			}
		} else if (charType == 4) {
			int calCon = 8; 
			int lvlUpHp = (baseMaxHp - 12) / (pc.getLevel() - 1);
			//���������� - ��1�� �⺻ HP / ���緹�� - 1
			// ���緹�� -1 �� �⺻�� 1�� HP(12)�� ���־��⶧���� �̷�����
			int defaul = 17 + (int)((baseCon - calCon)/4);
			// 17�� ���� �⺻ HP 5 + �����ִ�� 1 + calCon 8 + 3
			switch(baseCon - calCon){
			case 0 : case 1 :
				if(lvlUpHp > 12) hpbug = true; break;
			case 2 : case 3 :
				if(lvlUpHp > 13) hpbug = true; break;
			case 4 : case 5 : case 6 : case 7 :
				if(lvlUpHp > 15) hpbug = true; break;
			case 8 : case 9 : case 10 :
				if(lvlUpHp > 17) hpbug = true; break;    
			default :
				baseMaxHp -= 850; //���̽����� �ִ�� �Ͽ������ 50�������� �ִ���
				int defUpHp = (baseMaxHp - 12) / (pc.getLevel() - 51);
				if(defUpHp > defaul) hpbug = true; break;
			}
		} else if (charType == 5) {
			int calCon = 14; 
			int lvlUpHp = (baseMaxHp - 16) / (pc.getLevel() - 1);
			//���������� - ��1�� �⺻ HP / ���緹�� - 1
			// ���緹�� -1 �� �⺻�� 1�� HP(16)�� ���־��⶧���� �̷�����
			int defaul = 17 + (int)((baseCon - calCon)/3);
			// 21�� ���� �⺻ HP 7 + �����ִ�� 1 + calCon 14 - 1
			switch(baseCon - calCon){
			case 0 :
				if(lvlUpHp > 17) hpbug = true; break;
			case 1 :
				if(lvlUpHp > 18) hpbug = true; break;
			case 2 :
				if(lvlUpHp > 19) hpbug = true; break;
			case 3 :
				if(lvlUpHp > 20) hpbug = true; break;
			case 4 :
				if(lvlUpHp > 21) hpbug = true; break; 
			default :
				baseMaxHp -= 1050; //���̽����� �ִ�� �Ͽ������ 50�������� �ִ���
				int defUpHp = (baseMaxHp - 16) / (pc.getLevel() - 51);
				if(defUpHp > defaul) hpbug = true; break;
			}
		} else if (charType == 6) {
			int calCon = 12; 
			int lvlUpHp = (baseMaxHp - 14) / (pc.getLevel() - 1);
			//���������� - ��1�� �⺻ HP / ���緹�� - 1
			// ���緹�� -1 �� �⺻�� 1�� HP(14)�� ���־��⶧���� �̷�����
			int defaul = 14 + (int)((baseCon - calCon)/3);
			// 14�� ���� �⺻ HP 5 + �����ִ�� 1 + calCon 12 - 4
			switch(baseCon - calCon){
			case 0 : case 1 :
				if(lvlUpHp > 12) hpbug = true; break;
			case 2 : case 3 :
				if(lvlUpHp > 13) hpbug = true; break;
			case 4 :
				if(lvlUpHp > 14) hpbug = true; break;
			default :
				baseMaxHp -= 700; //���̽����� �ִ�� �Ͽ������ 50�������� �ִ���
				int defUpHp = (baseMaxHp - 14) / (pc.getLevel() - 51);
				if(defUpHp > defaul) hpbug = true; break;
			}
		}
		return hpbug;
	}

	public boolean CheckAbillity(L1PcInstance pc) {
		boolean result = true;
		int MaxAbility = 35;
		int minStr, minDex, minCon, minWis, minCha, minInt;
		int str = pc.getAbility().getStr();
		int dex = pc.getAbility().getDex();
		int con = pc.getAbility().getCon();
		int wis = pc.getAbility().getWis();
		int cha = pc.getAbility().getCha();
		int intel = pc.getAbility().getInt();
		int StatusCount = str+dex+con+wis+cha+intel;
		switch(pc.getType()) {
		case 0:
			minStr = 13;
			minDex = 10;
			minCon = 10;
			minWis = 11;
			minCha = 13;
			minInt = 10;
			break;
		case 1:
			minStr = 16;
			minDex = 12;
			minCon = 14;
			minWis = 9;
			minCha = 12;
			minInt = 8;
			break;
		case 2:
			minStr = 11;
			minDex = 12;
			minCon = 12;
			minWis = 12;
			minCha = 9;
			minInt = 12;
			break;
		case 3:
			minStr = 8;
			minDex = 7;
			minCon = 12;
			minWis = 12;
			minCha = 8;
			minInt = 12;
			break;
		case 4:
			minStr = 12;
			minDex = 15;
			minCon = 8;
			minWis = 10;
			minCha = 9;
			minInt = 11;
			break;
		case 5:
			minStr = 13;
			minDex = 11;
			minCon = 14;
			minWis = 12;
			minCha = 8;
			minInt = 11;
			break;
		case 6:
			minStr = 11;
			minDex = 10;
			minCon = 12;
			minWis = 12;
			minCha = 8;
			minInt = 12;
			break;
		default:
			return false;
		}
		if(pc.getLevel() <= 50 && StatusCount > 75) { result = false; }
		if(pc.getAbility().getBaseStr() < minStr || pc.getAbility().getStr() > MaxAbility) result = false;
		if(pc.getAbility().getBaseDex() < minDex || pc.getAbility().getDex() > MaxAbility) result = false;
		if(pc.getAbility().getBaseCon() < minCon || pc.getAbility().getCon() > MaxAbility) result = false;
		if(pc.getAbility().getBaseWis() < minWis || pc.getAbility().getWis() > MaxAbility) result = false;
		if(pc.getAbility().getBaseCha() < minCha || pc.getAbility().getCha() > MaxAbility) result = false;
		if(pc.getAbility().getBaseInt() < minInt || pc.getAbility().getInt() > MaxAbility) result = false;
		return result;
	}

	/*
	 * Ŭ������ �ּ� �ִ밪 ���� �˻�
	 */
	public void Checknoend(L1PcInstance pc) {
		pc.setReturnStat(1);
		pc.sendPackets(new S_Disconnect()); // ĳ���͸� ���忡�� �߹�
	}

	public void Levelup(L1PcInstance pc) {
		if (pc.getLevel() <= pc.getHighLevel()) { // ���� �Ҷ� �ִ� �������� ������ ����..
			pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.LEVELUP));
		} else {
			Checknoend(pc);
		}
	}

	/*
	 * ���̽� ���� ���� 1���� �ְ��� �� 20 ������ 19 �ɼ� ���� Ŭ���� ���� ���� ���� �ߴٰ� ���� �ʿ� ��� ����
	 */

	public void Checkov(L1PcInstance pc) {

		int[] BaseStat2 = { pc.getAbility().getBaseStr(),
				pc.getAbility().getBaseDex(), pc.getAbility().getBaseCon(),
				pc.getAbility().getBaseWis(), pc.getAbility().getBaseCha(),
				pc.getAbility().getBaseInt() };

		int[] masBase2 = { 21, 19, 19, 19, 19, 19 };
		for (int i = 0; i < BaseStat2.length; i++) {
			if (BaseStat2[i] > masBase2[i]) {
				Checknoend(pc);
				System.out.println("�⺻ ����  ���� : " + pc.getName() + " )");

			}
		}
	}

	public void checkStatusBug2(L1PcInstance pc) {
		// pc.getAbility().setElixirCount(pc.getAbility().getElixirCount() + 1);
		int _Elixir = pc.getAbility().getElixirCount();
		int _status2 = 75 + pc.getAbility().getElixirCount();
		int _status = pc.getHighLevel() - 50;
		int status = pc.getAbility().getDex() + pc.getAbility().getCha()
				+ pc.getAbility().getCon() + pc.getAbility().getInt()
				+ pc.getAbility().getStr() + pc.getAbility().getWis();
		int _Allbase = pc.getAbility().getBaseStr()
				+ pc.getAbility().getBaseDex() + pc.getAbility().getBaseCon()
				+ pc.getAbility().getBaseWis() + pc.getAbility().getBaseCha()
				+ pc.getAbility().getBaseInt(); // ����

		if (pc.getHighLevel() <= 50) {// 50�����ϰ��
			if (_status2 > _Allbase + _Elixir) {
				Checknoend(pc);
				System.out.println("���� ���� / ���̵� : " + pc.getName() + " )");
			} else {
				// System.out.println("���� ����/ ���̵� : " + pc.getName() + " )");
			}
		} else if (pc.getHighLevel() >= 51) { // 51�̻��ϰ��
			if (status + _Elixir > _status2 + _status) {
				Checknoend(pc);
				System.out.println("���� ���� / ���̵� : " + pc.getName() + " )");
			} else {
				// System.out.println("���� ����/ ���̵� : " + pc.getName() + " )");
			}
		}

	}
	public void CheckStat(L1PcInstance pc) {

		int[] BaseStat = { pc.getAbility().getBaseStr(),
				pc.getAbility().getBaseDex(), pc.getAbility().getBaseCon(),
				pc.getAbility().getBaseWis(), pc.getAbility().getBaseCha(),
				pc.getAbility().getBaseInt() };
		int[] masBase = null;
		String[] BaseStat5 = { " ��  ", " ��  ", " ��  ", " ���� ", " ī��  ", " ��Ʈ " };
		int maxBase = 35;
		/* �� �� �� ���� ī�� ��Ʈ */
		switch (pc.getType()) {
		case 0:// ����
			masBase = new int[] { 13, 10, 10, 11, 13, 10 };// ����
			break;
		case 1:// ���
			masBase = new int[] { 16, 12, 14, 9, 12, 8 };// ���
			break;
		case 2:// ����
			masBase = new int[] { 11, 12, 12, 12, 9, 12 };// ����
			break;
		case 3:// ������
			masBase = new int[] { 8, 7, 12, 12, 8, 12 };// ������
			break;
		case 4:// �ٿ�
			masBase = new int[] { 12, 15, 8, 10, 9, 11 };// �ٿ�
			break;
		case 5:// ����
			masBase = new int[] { 13, 11, 14, 12, 8, 11 };// ����
			break;
		case 6:// ȯ����
			masBase = new int[] { 11, 10, 12, 12, 8, 12 };// ȯ����
			break;
		}

		for (int i = 0; i < masBase.length; i++) {
			if (BaseStat[i] < masBase[i] || BaseStat[i] > maxBase) {
				Checknoend(pc);
				System.out.println("( ���� �ʱ�ȭ / ������ ���̵� : "+ pc.getName() +" )");
				break;
			} else {
				//	System.out.println("( ���� �ʱ�ȭ / �������� ���̵� : "+ pc.getName() +" )");
			}
		}

	}

	public void statup(L1PcInstance pc){
		int Stathp = 0;
		int Statmp = 0;
		pc.setLevel(pc.getLevel() + 1);
		Stathp = CalcStat.calcStatHp(pc.getType(), pc.getBaseMaxHp(), pc.getAbility().getCon());
		Statmp = CalcStat.calcStatMp(pc.getType(), pc.getBaseMaxMp(), pc.getAbility().getWis());
		pc.resetBaseAc();
		pc.getAC().setAc(pc.getBaseAc());
		pc.addBaseMaxHp((short) Stathp);
		pc.addBaseMaxMp((short) Statmp);
	}
}