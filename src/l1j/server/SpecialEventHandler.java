package l1j.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import server.GameServer;

import l1j.server.Warehouse.PackageWarehouse;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_DRAGONPERL;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharStatus2;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ReturnedStat;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.SQLUtil;
import static l1j.server.server.model.skill.L1SkillId.*;

enum SpecialEvent { BugRace, AllBuf, InfinityFight, DoNotChatEveryone, DoChatEveryone};

//게임 내, 전체 이벤트에 대한 처리를 담당
public class SpecialEventHandler {

	private static volatile SpecialEventHandler uniqueInstance = null;

	private boolean CheckBugrace = false; 

	private SpecialEventHandler() {}

	public static SpecialEventHandler getInstance() {
		if(uniqueInstance == null) {
			synchronized (SpecialEventHandler.class) {
				if(uniqueInstance == null) {
					uniqueInstance = new SpecialEventHandler();
				}
			}
		}

		return uniqueInstance;
	}

	public void giveFeather(){
		Connection c = null;
		PreparedStatement p = null;
		PreparedStatement p1 = null;
		ResultSet r = null;
		String accountName;
		int count;
		try {
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("SELECT * FROM hongbo");
			r = p.executeQuery();
			while(r.next()){
				accountName = r.getString("account");
				count = r.getInt("remaincount");
				if (count <= 0) continue;
				count *= 500;
				PackageWarehouse.insertItem(accountName, count);
				p1 = c.prepareStatement("UPDATE hongbo SET excutecount = excutecount + '"+r.getInt("remaincount")+"',remaincount = 0 WHERE account = '" + accountName +"'");
				p1.execute();
			}
		} catch (Exception e) {
		} finally {
			SQLUtil.close(r);
			SQLUtil.close(p1);
			SQLUtil.close(p);
			SQLUtil.close(c);
		}
	}
	public void doBugRace() {
		if(!CheckBugrace)
			CheckBugrace = true;
		else return;
		//BugRaceController.getInstance().BugRaceRestart = true;
	}

	public void doAllBuf() {
		int[] allBuffSkill = { DECREASE_WEIGHT, PHYSICAL_ENCHANT_DEX,
				PHYSICAL_ENCHANT_STR, BLESS_WEAPON, ADVANCE_SPIRIT,				
				IRON_SKIN, NATURES_TOUCH, 
				CONCENTRATION, PATIENCE, INSIGHT};
		L1SkillUse l1skilluse = null;
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if(pc.isPrivateShop() 
					|| pc.noPlayerCK
					|| pc.isGm() 
					|| pc.isDead()){
				continue;
			}
			l1skilluse = new L1SkillUse();
			for (int i = 0; i < allBuffSkill.length ; i++) {
				l1skilluse.handleCommands(pc, allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			}
			//pc.sendPackets(new S_SystemMessage("운영자에게 버프를 받았습니다. "));
		}
	}

	public void doGoodAllBuff(L1PcInstance pc, int type) {
		L1SkillUse l1skilluse = null;
		l1skilluse = new L1SkillUse();
		if (type == 0){
			int[] allBuffSkill = {DECREASE_WEIGHT, // 디크리즈 웨이트 : 무게를 줄여준다.
					PHYSICAL_ENCHANT_DEX, // 인챈트 덱스 : 덱스를 +5 증가시켜준다.
					IMMUNE_TO_HARM, // 이뮨 투 함 : 데미지 50% 감소
					PHYSICAL_ENCHANT_STR, // 인챈트 스트랭스 : 힘을 +5 증가 시켜준다.
					BLESS_WEAPON,  // 블레이 웨폰 : 무기 데미지를 +2 증가 시켜준다.
					ADVANCE_SPIRIT, // 어드밴스 스피릿 : 순수 HP를 20% 증가 시켜준다.
					VENOM_RESIST, // 베놈 레지스트 : 독에 걸리지 않는다.
					SHINING_AURA, // 샤이닝 오라 : Ac-8
					BRAVE_AURA, // 브레이브 오라 : 추타 5
					RESIST_MAGIC, // 레지스트 매직 : MR + 10
					IRON_SKIN, // 아이언 스킨 : AC -10
					EXOTIC_VITALIZE, // 엑조틱 바이탈 라이즈 : 무게가 50%를 넘어도 피,엠을 차게 한다
					CONCENTRATION,  // 컨센트레이션 : MPR +2
					PATIENCE, // 페이션스 : 데미지 감소 2
					INSIGHT, // 모든 스탯 +1
					STATUS_COMA_5, // 코마 버프 5 : AC-8, 공성+5, 힘+5, 덱스+5, 콘+1, SP+1, 경험치 보너스 20%
					FEATHER_BUFF_A // 깃털 버프 : 추가 타격+2, 공격 성공+2, 주술력+2, 최대 HP+50, HP 회복+3, 최대 MP+30, MP 회복+3, 대미지 리덕션+3
			};
			for (int i = 0; i < allBuffSkill.length ; i++) {
				l1skilluse.handleCommands(pc, allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			}
		} else if (type == 1){
			int[] allBuffSkill = {DECREASE_WEIGHT, // 디크리즈 웨이트 : 무게를 줄여준다.
					PHYSICAL_ENCHANT_DEX, // 인챈트 덱스 : 덱스를 +5 증가시켜준다.
					IMMUNE_TO_HARM, // 이뮨 투 함 : 데미지 50% 감소
					PHYSICAL_ENCHANT_STR, // 인챈트 스트랭스 : 힘을 +5 증가 시켜준다.
					BLESS_WEAPON,  // 블레이 웨폰 : 무기 데미지를 +2 증가 시켜준다.
					ADVANCE_SPIRIT, // 어드밴스 스피릿 : 순수 HP를 20% 증가 시켜준다.
					VENOM_RESIST, // 베놈 레지스트 : 독에 걸리지 않는다.
					SHINING_AURA, // 샤이닝 오라 : Ac-8
					BRAVE_AURA, // 브레이브 오라 : 추타 5
					RESIST_MAGIC, // 레지스트 매직 : MR + 10
					IRON_SKIN, // 아이언 스킨 : AC -10
					EXOTIC_VITALIZE, // 엑조틱 바이탈 라이즈 : 무게가 50%를 넘어도 피,엠을 차게 한다
					CONCENTRATION,  // 컨센트레이션 : MPR +2
					PATIENCE, // 페이션스 : 데미지 감소 2
					INSIGHT, // 모든 스탯 +1
					//STATUS_COMA_5, // 코마 버프 5 : AC-8, 공성+5, 힘+5, 덱스+5, 콘+1, SP+1, 경험치 보너스 20%
					//FEATHER_BUFF_A // 깃털 버프 : 추가 타격+2, 공격 성공+2, 주술력+2, 최대 HP+50, HP 회복+3, 최대 MP+30, MP 회복+3, 대미지 리덕션+3
			};
			for (int i = 0; i < allBuffSkill.length ; i++) {
				l1skilluse.handleCommands(pc, allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			}
		}
	}

	public void doGoodAllBuff(L1PcInstance pc) {
		L1SkillUse l1skilluse = null;
		l1skilluse = new L1SkillUse();
		int[] allBuffSkill = {DECREASE_WEIGHT, // 디크리즈 웨이트 : 무게를 줄여준다.
				PHYSICAL_ENCHANT_DEX, // 인챈트 덱스 : 덱스를 +5 증가시켜준다.
				IMMUNE_TO_HARM, // 이뮨 투 함 : 데미지 50% 감소
				PHYSICAL_ENCHANT_STR, // 인챈트 스트랭스 : 힘을 +5 증가 시켜준다.
				BLESS_WEAPON,  // 블레이 웨폰 : 무기 데미지를 +2 증가 시켜준다.
				ADVANCE_SPIRIT, // 어드밴스 스피릿 : 순수 HP를 20% 증가 시켜준다.
				VENOM_RESIST, // 베놈 레지스트 : 독에 걸리지 않는다.
				SHINING_AURA, // 샤이닝 오라 : Ac-8
				BRAVE_AURA, // 브레이브 오라 : 추타 5
				RESIST_MAGIC, // 레지스트 매직 : MR + 10
				IRON_SKIN, // 아이언 스킨 : AC -10
				EXOTIC_VITALIZE, // 엑조틱 바이탈 라이즈 : 무게가 50%를 넘어도 피,엠을 차게 한다
				CONCENTRATION,  // 컨센트레이션 : MPR +2
				PATIENCE, // 페이션스 : 데미지 감소 2
				INSIGHT, // 모든 스탯 +1
				STATUS_COMA_5, // 코마 버프 5 : AC-8, 공성+5, 힘+5, 덱스+5, 콘+1, SP+1, 경험치 보너스 20%
				FEATHER_BUFF_A // 깃털 버프 : 추가 타격+2, 공격 성공+2, 주술력+2, 최대 HP+50, HP 회복+3, 최대 MP+30, MP 회복+3, 대미지 리덕션+3
		};
		for (int i = 0; i < allBuffSkill.length ; i++) {
			l1skilluse.handleCommands(pc, allBuffSkill[i], pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
		}
	}


	public void doNotChatEveryone() {
		L1World.getInstance().set_worldChatElabled(false);
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("운영자에 의해 월드채팅이 금지되었습니다."));
	}

	public void doChatEveryone() {
		L1World.getInstance().set_worldChatElabled(true);
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("운영자에 의해 월드채팅이 실행되었습니다."));
	}

	public void ReturnStats(L1PcInstance pc) {
		pc.getAbility().initStat(pc.getClassId());
		L1SkillUse l1skilluse = new L1SkillUse();
		l1skilluse.handleCommands(pc, L1SkillId.CANCELLATION, pc.getId(), pc.getX(), pc.getY(),
				null, 0, L1SkillUse.TYPE_LOGIN);

		if(pc.getWeapon() != null) {
			pc.getInventory().setEquipped(pc.getWeapon(), false, false, false);
		}
		pc.sendPackets(new S_SPMR(pc));
		pc.sendPackets(new S_CharVisualUpdate(pc));
		pc.sendPackets(new S_OwnCharStatus2(pc));

		for (L1ItemInstance armor : pc.getInventory().getItems()) {
			for (int type = 0; type <= 12; type++) {
				if (armor != null) {
					pc.getInventory().setEquipped(armor, false, false, false);
				}
			}
		}
		pc.setReturnStat(pc.getExp());
		pc.sendPackets(new S_SPMR(pc));
		pc.sendPackets(new S_OwnCharAttrDef(pc));
		pc.sendPackets(new S_OwnCharStatus2(pc));
		pc.sendPackets(new S_ReturnedStat(pc, S_ReturnedStat.START));
		try {
			pc.save();
		} catch (Exception e) {}
	}

}
