// 위치 확인.
//	: 세이프존이 아닐경우 일단 마을로 이동. 정비를 위해.
//	: 변신 상태 확인해서 변신하기.
//	: 버프상태 확인해서 버프 시전하기.
//	: hp 상태 확인해서 100% 될때까지 물약 먹기.
// 준비완료된 상태.
//	: 혈맹 스타일에 따라 처리하기.
//	:	공성전용
//	:		공성 상태인거 찾아서 해당 성 입구족으로 텔레포트
//	:		로봇 객체를 제외한 모든 객체들 다 공격처리.
//	:	사냥용
//	:		등록된 사냥터좌표중 하나랜덤 선택해서 이동.
//	:		근처에 보이는 몬스터 사냥 시작.
// 전투중 처리.
//	: hp 가 98% 미만일경우 물약이팩트와 함께 체력 회복시키기.
//	: hp 가 30% 미만일경우 마을로 귀환.
// 주변에 같은 로봇 혈맹이 존재할경우.
//	: hp 90% 미만일경우 체력회복 마법 시전해주기
//	: 버프상태도 확인해서 버프도 해주기.
//	: 
package l1j.server.server.model.Instance;

import static l1j.server.server.model.skill.L1SkillId.POLLUTE_WATER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BLUE_POTION;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HASTE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import l1j.server.server.Opcodes;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Astar;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Node;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_MoveCharPacket;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.utils.CommonUtil;
import server.threads.pc.RobotThread;
import server.threads.pc.bean.RobotLocation;
import server.threads.pc.bean.RobotMent;

public class L1RobotInstance extends L1PcInstance {
	private static final long serialVersionUID = 1L;

	private int x = 0;
	private int y = 0;
	private long curtime = 0;
	private L1Object targetObj = null;

	private long ai_time; // 인공지능 처리에 사용될 프레임참고 값
	private long ai_start_time; // 인공지능 시작된 시간값
	private int ai_status; // 인공지능 처리해야할 상태
	private boolean ai_showment; // 멘트 발생여부.

	// 텔레포트한 사냥터의 위치 임시 저장용.
	private RobotLocation location;
	// 사냥 시작된 지점 저장용.
	private int homeX;
	private int homeY;
	private int homeMap;

	// 타일 길찾기용
	private L1Astar aStar;
	private L1Node tail;
	private int iCurrentPath;
	private int[][] iPath;
	protected List<L1Character> attackList;
	protected List<L1Character> temp_list;

	// pvp중인지 확인할 변수.
	public boolean pvp;
	// 마을에서 버프 스탭 확인용
	private int buff_step;
	// 공성 스타일 인공지능시 이동된 성 아이디값.
	private int castle_id;

	// 인공지능 상태 변수
	static private final int AI_STATUS_SETTING = 0; // 초반 세팅처리
	static private final int AI_STATUS_WALK = 1; // 랜덤워킹 상태
	static private final int AI_STATUS_ATTACK = 2; // 공격 상태
	static private final int AI_STATUS_DEAD = 3; // 죽은 상태
	static private final int AI_STATUS_CORPSE = 4; // 시체 상태
	static private final int AI_STATUS_SPAWN = 5; // 스폰 상태
	static private final int AI_STATUS_ESCAPE = 6; // 도망 상태
	static private final int AI_STATUS_PICKUP = 7; // 아이템 줍기 상태
	static private final int AI_STATUS_SHOP = 8; // 상점으로이동처리

	public L1RobotInstance() {
		aStar = new L1Astar();
		iPath = new int[300][2];
		attackList = new ArrayList<L1Character>();
		temp_list = new ArrayList<L1Character>();
	}

	

	private void setAiStatus(int ai_status) {
		this.ai_status = ai_status;
		// ai 상태 변경될때마다 멘트표현 변수 초기화.
		ai_showment = false;
	}

	private int getAiStatus() {
		return ai_status;
	}

	/**
	 * 인공지능 활성화할 시간이 됫는지 확인해주는 함수.
	 * 
	 * @param time
	 * @return
	 */
	private boolean isAi(long time) {
		long speed = ai_time;
		long temp = time - ai_start_time;
		double gab = getMoveState().getMoveSpeed() == 1 && getMoveState().getBraveSpeed() == 1 ? 0.5 : getMoveState().getMoveSpeed() == 1 ? 0.2 : getMoveState().getMoveSpeed() == 2 ? -0.5 : 0;
		speed -= (long) (speed * gab);

		if (time == 0 || temp >= speed) {
			ai_start_time = time;
			return true;
		}

		return false;
	}

	

	// /여기서 스핵설정해주면되

	private boolean isDistance(int x, int y, int m, int tx, int ty, int tm, int loc) {
		int distance = getDistance(x, y, tx, ty);
		if (loc < distance)
			return false;
		if (m != tm)
			return false;
		return true;
	}

	private int getDistance(int x, int y, int tx, int ty) {
		long dx = tx - x;
		long dy = ty - y;
		return (int) Math.sqrt(dx * dx + dy * dy);
	}

	private int getXY(final int h, final boolean type) {
		int loc = 0;
		switch (h) {
		case 0:
			if (!type)
				loc -= 1;
			break;
		case 1:
			if (type)
				loc += 1;
			else
				loc -= 1;
			break;
		case 2:
			if (type)
				loc += 1;
			break;
		case 3:
			loc += 1;
			break;
		case 4:
			if (!type)
				loc += 1;
			break;
		case 5:
			if (type)
				loc -= 1;
			else
				loc += 1;
			break;
		case 6:
			if (type)
				loc -= 1;
			break;
		case 7:
			loc -= 1;
			break;
		}
		return loc;
	}

	public static int calcheading(int myx, int myy, int tx, int ty) {
		if (tx > myx && ty > myy) {
			return 3;
		} else if (tx < myx && ty < myy) {
			return 7;
		} else if (tx > myx && ty == myy) {
			return 2;
		} else if (tx < myx && ty == myy) {
			return 6;
		} else if (tx == myx && ty < myy) {
			return 0;
		} else if (tx == myx && ty > myy) {
			return 4;
		} else if (tx < myx && ty > myy) {
			return 5;
		} else {
			return 1;
		}
	}

	public static int calcheading(L1Object o, int x, int y) {
		return calcheading(o.getX(), o.getY(), x, y);
	}

	/**
	 * 공격자 목록에 등록처리 함수.
	 * 
	 * @param o
	 */
	public void addAttackList(L1Character o) {
		if (!isDead() && !o.isDead() && o.getId() != getId() && !attackList.contains(o)) {
			// 공격목록에 추가.
			attackList.add(o);
		}

	}

	/**
	 * 길찾기에 사용될 aStar이용한 처리 함수.
	 * 
	 * @param o
	 * @param x
	 * @param y
	 * @param h
	 * @param astar
	 */
	private void toMoving(L1Object o, final int x, final int y, final int h, final boolean astar) {
		
	}

	/**
	 * 이동처리 함수.
	 * 
	 * @param x
	 * @param y
	 * @param h
	 */
	private void toMoving(final int x, final int y, final int h) {
		getMap().setPassable(getLocation(), true);
		getLocation().set(x, y);
		getMoveState().setHeading(h);
		Broadcaster.broadcastPacket(this, new S_MoveCharPacket(this));
		L1WorldTraps.getInstance().onPlayerMoved(this);
		getMap().setPassable(getLocation(), false);
	}

	/**
	 * 공격목록에 등록된 객체중 위험한 객체를 우선검색해서 리턴. : 리턴된 객체를 타켓으로 공격함.
	 * 
	 * @return
	 */
	private L1Character findDangerousObject() {
		L1Character o = null;

		// 사용자 우선 검색.
		for (L1Character oo : attackList) {
			if (oo instanceof L1PcInstance) {
				if (o == null)
					o = oo;
				else if (getDistance(getX(), getY(), oo.getX(), oo.getY()) < getDistance(getX(), getY(), o.getX(), o.getY()))
					o = oo;
			}
		}

		if (o != null)
			return o;

		// 몬스터 검색.
		for (L1Character oo : attackList) {
			if (o == null)
				o = oo;
			else if (getDistance(getX(), getY(), oo.getX(), oo.getY()) < getDistance(getX(), getY(), o.getX(), o.getY()))
				o = oo;
		}
		return o;
	}

	/**
	 * 공격 처리 함수.
	 * 
	 * @param o
	 * @param x
	 * @param y
	 * @param bow
	 * @param gfxMode
	 * @param alpha_dmg
	 */
	private void toAttack(L1Object o, int x, int y, boolean bow, int gfxMode, int alpha_dmg) {

		// 공격 액션을 취할 수 있는 경우의 처리
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ABSOLUTE_BARRIER)) { // 아브소르트바리아의 해제
			getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.ABSOLUTE_BARRIER);
			// pc.startHpRegeneration();
			// pc.startMpRegeneration();
			startHpRegenerationByDoll();
			startMpRegenerationByDoll();
		}
		/** 2011.04.16 고정수 디텍터 */
		if (this.isElf()) {
			for (L1Object obj : L1World.getInstance().getVisibleObjects(this)) {
				if (obj instanceof L1MonsterInstance) {
					if (((L1MonsterInstance) obj).getHiddenStatus() >= 1) {
						Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), 19));
						Broadcaster.broadcastPacket((L1Character) o, new S_SkillSound(o.getId(), 749));
						((L1MonsterInstance) obj).setHiddenStatus(0);
						setCurrentMp(getCurrentMp() - 10);
					}
				}
			}
		} else {
			for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 2)) {
				if (obj instanceof L1MonsterInstance) {
					if (((L1MonsterInstance) obj).getHiddenStatus() >= 1) {
						Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), 19));
						Broadcaster.broadcastPacket((L1Character) o, new S_SkillSound(o.getId(), 749));
						((L1MonsterInstance) obj).setHiddenStatus(0);
						setCurrentMp(getCurrentMp() - 10);
					}
				}
			}
		}

		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MEDITATION)) {
			getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.MEDITATION);
		}

		delInvis();
		setRegenState(REGENSTATE_ATTACK);
		o.onAction(this);
	}

	/**
	 * 각클레스가 마법을 시전하게 한다.
	 * 
	 * @param o
	 */
	private void toWizardMagic(L1Object o) {
		if (isWizard()) {
			Random random = new Random();
			int a = random.nextInt(5);
			switch (a) {
			case 1:
				new L1SkillUse().handleCommands(this, L1SkillId.SUNBURST, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 2:
				new L1SkillUse().handleCommands(this, L1SkillId.SILENCE, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 3:
				new L1SkillUse().handleCommands(this, L1SkillId.ERUPTION, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 4:
				new L1SkillUse().handleCommands(this, L1SkillId.DECAY_POTION, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 5:
				new L1SkillUse().handleCommands(this, L1SkillId.WEAPON_BREAK, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;

			default:
			}
		}
	}

	private void toKnightMagic(L1Object o) {
		if (isKnight()) {
			new L1SkillUse().handleCommands(this, L1SkillId.SHOCK_STUN, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			setCurrentMp(1);
		}
	}

	private void toDragonknightMagic(L1Object o) {
		Random random = new Random();
		int a = random.nextInt(6);
		switch (a) {
		case 1:
			new L1SkillUse().handleCommands(this, L1SkillId.FOU_SLAYER, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			setCurrentMp(1);
			break;
		case 2:
			new L1SkillUse().handleCommands(this, L1SkillId.THUNDER_GRAB, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			setCurrentMp(1);
			break;
		case 3:
			new L1SkillUse().handleCommands(this, L1SkillId.FOU_SLAYER, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
			setCurrentMp(1);
			break;
		default:
		}
	}

	private void toIllusionistMagic(L1Object o) {
		if (isIllusionist()) {
			Random random = new Random();
			int a = random.nextInt(4);
			switch (a) {
			case 1:
				new L1SkillUse().handleCommands(this, L1SkillId.MIND_BREAK, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 2:
				new L1SkillUse().handleCommands(this, L1SkillId.MIND_BREAK, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 3:
				new L1SkillUse().handleCommands(this, L1SkillId.PANIC, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 4:
				new L1SkillUse().handleCommands(this, L1SkillId.IllUSION_AVATAR, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;

			default:
			}
		}
	}

	private void toElfMagic(L1Object o) {
		if (isElf()) {
			Random random = new Random();
			int a = random.nextInt(6);
			switch (a) {
			case 1:
				Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 4394));
				for (int i = 0; i < 3; ++i)
					toAttack(o, 0, 0, isElf(), 1, 0);
				setCurrentMp(1);
				break;
			case 2:
				Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 4394));
				for (int i = 0; i < 3; ++i)
					toAttack(o, 0, 0, isElf(), 1, 0);
				setCurrentMp(1);
				break;
			case 3:
				new L1SkillUse().handleCommands(this, L1SkillId.STRIKER_GALE, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 4:
				new L1SkillUse().handleCommands(this, L1SkillId.POLLUTE_WATER, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;
			case 5:
				Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 2178));
				setCurrentMp(1);
				break;
			case 6:
				new L1SkillUse().handleCommands(this, L1SkillId.EARTH_BIND, o.getId(), o.getX(), o.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				setCurrentMp(1);
				break;

			default:
			}
		}
	}

	/**
	 * 객체가 공격가능한 상태인지 확인해주는 함수.
	 * 
	 * @param o
	 * @param walk
	 * @return
	 */
	public boolean isAttack(L1Character o, boolean walk) {
		if (o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SILENCE) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.DECAY_POTION) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHOCK_STUN) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.THUNDER_GRAB) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.MIND_BREAK) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PANIC) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.IllUSION_AVATAR)
				|| o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STRIKER_GALE) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.POLLUTE_WATER) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.EARTH_BIND) || o.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ICE_LANCE)) {
			return false;
		}
		if (o.getMap().isSafetyZone(o.getLocation()))
			return false;
		if (o == null)
			return false;
		if (o.isDead())
			return false;
		// if(o.isGm())
		// return false;
		// if(o.isLockHigh())
		// return false;
		if (o.isInvisble())
			return false;
		if (!isDistance(getX(), getY(), getMapId(), o.getX(), o.getY(), o.getMapId(), 12))
			return false;

		return true;
	}

	/**
	 * 가속도 물약복용 처리 함수.
	 */
	private void toSpeedPostion() {
		if (!isWizard() && getMoveState().getBraveSpeed() == 0) {
			// 용기 떠러졌을경우 복용하기.
			Broadcaster.broadcastPacket(this, new S_SkillBrave(getId(), 1, 0));
			getMoveState().setBraveSpeed(1);
			getSkillEffectTimerSet().setSkillEffect(STATUS_BRAVE, 300 * 1000);
			Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 751));
			return;
		}
		if (getMoveState().getMoveSpeed() == 0) {
			// 촐기떠러졌을경우 복용하기.
			Broadcaster.broadcastPacket(this, new S_SkillHaste(getId(), 1, 0));
			getMoveState().setMoveSpeed(1);
			getSkillEffectTimerSet().setSkillEffect(STATUS_HASTE, 300 * 1000);
			return;
		}
		if (isWizard() && !getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_BLUE_POTION)) {
			// 파랭이떠러졌을경우 복용하기.
			sendPackets(new S_SkillIconGFX(34, 600));
			sendPackets(new S_SkillSound(getId(), 190));
			getSkillEffectTimerSet().setSkillEffect(STATUS_BLUE_POTION, 600 * 1000);
			return;
		}
	}

	/**
	 * 물약 복용처리 함수.
	 * 
	 * @param direct
	 */
	private void toHealingPostion(boolean direct) {
		int p = (int) (((double) getCurrentHp() / (double) getMaxHp()) * 100);
		if (direct || p <= 70) { // 물약 회복 70%
			// if(direct || p <= 90){ // 물약 회복 90%
			// 디케이포션 상태는 회복 저지.
			if (getSkillEffectTimerSet().hasSkillEffect(71))
				return;
			// 앱솔루트베리어의 해제
			cancelAbsoluteBarrier();
			// 이팩트 처리
			Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 197));

			int healHp = 55 * CommonUtil.random(1, 5);
			// 포르트워타중은 회복량1/2배
			if (getSkillEffectTimerSet().hasSkillEffect(POLLUTE_WATER))
				healHp /= 2;
			setCurrentHp(getCurrentHp() + healHp);
		}

		// 50퍼센트 미만 되면 마을로 이동하기위해 상태 변경.
		if (p < 30) { // 50% 이상시 베르하기 // 기본 30
			setAiStatus(AI_STATUS_SETTING);
		}
	}

	/**
	 * 멘트 처리 함수.
	 * 
	 * @param time
	 * @param pvp
	 */
	private void toMent(long time, boolean pvp) {
		List<RobotMent> list = new ArrayList<RobotMent>();

		if (pvp) {
			for (RobotMent m : RobotThread.getRobotMent()) {
				if (m.type.equalsIgnoreCase("pvp"))
					list.add(m);
			}
		} else {
			switch (ai_status) {
			case AI_STATUS_DEAD:
				for (RobotMent m : RobotThread.getRobotMent()) {
					if (m.type.equalsIgnoreCase("die"))
						list.add(m);
				}
				break;
			}
		}

		// 멘트를 표현할 수 있는 디비상태일경우.
		if (list.size() > 0) {
			if (!ai_showment) {
				Broadcaster.broadcastPacket(this, new S_ChatPacket(this, list.get(CommonUtil.random(0, list.size() - 1)).ment, Opcodes.S_OPCODE_NORMALCHAT, 0));
				ai_showment = true;
			}
		}
	}

	/**
	 * 주변 로봇에게 버프시전 처리 함수.
	 * 
	 * @return
	 */
	private boolean toBuff() {

		// 주변에 피단 같은 로봇이 잇을경우 버프주기.
		if (isWizard() && getCurrentMp() >= 30) {
			// 로봇 담을 공간
			List<L1RobotInstance> list = new ArrayList<L1RobotInstance>();
			// pvp중인 로봇 찾기.
			for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 17)) {
				if (obj instanceof L1RobotInstance) {
					L1RobotInstance robot = (L1RobotInstance) obj;
					if (robot.pvp && this.getClanid() != robot.getClanid())// pvp중이고 같은혈맹이라면..
						list.add(robot);
				}
			}
			// 잇을경우.
			if (list.size() > 0) {
				for (L1RobotInstance robot : list) {
					if (robot.isDead()) {
						return false;
					}
					int p2 = (int) (((double) robot.getCurrentHp() / (double) robot.getMaxHp()) * 100);
					if (p2 <= 90) {// 피가 90~81% 면 버프주기.
						Random random = new Random();
						int a = random.nextInt(8);
						switch (a) {
						case 1:
							new L1SkillUse().handleCommands(this, L1SkillId.HEAL_ALL, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;
						case 2:
							new L1SkillUse().handleCommands(this, L1SkillId.FULL_HEAL, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;
						case 3:
							new L1SkillUse().handleCommands(this, L1SkillId.EXTRA_HEAL, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;
						case 4:
							new L1SkillUse().handleCommands(this, L1SkillId.GREATER_HEAL, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;
						case 5:
							new L1SkillUse().handleCommands(this, L1SkillId.ADVANCE_SPIRIT, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;
						case 6:
							new L1SkillUse().handleCommands(this, L1SkillId.BLESS_WEAPON, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;
						case 7:
							new L1SkillUse().handleCommands(this, L1SkillId.IMMUNE_TO_HARM, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;
						case 8:
							new L1SkillUse().handleCommands(this, L1SkillId.PHYSICAL_ENCHANT_STR, robot.getId(), robot.getX(), robot.getY(), null, 7000, L1SkillUse.TYPE_GMBUFF);
							setCurrentMp(getCurrentMp() - 15);
							break;

						default:
						}
						return true;
					}
				}
			}
		}

		return false;
	}

	public void onAction(L1PcInstance attacker) {
		super.onAction(attacker);

		// 나를 공격한 녀석은 다 공격목록에 등록.
		addAttackList(attacker);
		// 주변봇에게 요청
		for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 17)) {
			if (obj instanceof L1RobotInstance)
				if (getClanid() == ((L1RobotInstance) obj).getClanid())// 같은혈맹이라면..
					((L1RobotInstance) obj).addAttackList(attacker);
		}
	}

	/**
	 * 인공지능 시작부분. : 인공지능 시간일 경우만 처리함.
	 * 
	 * @param time
	 */
	public void toAi(long time) {
		try {
			if (!isAi(time))
				return;
	
			/** 2011.04.18 고정수 페럴라이즈 상태시 안움직이게 */
			if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.EARTH_BIND) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHOCK_STUN) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ICE_LANCE)  || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.FREEZING_BLIZZARD) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.CURSE_PARALYZE) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.THUNDER_GRAB) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.BONE_BREAK)   || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM) || 
				getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_FREEZE)) {
				return;
			}
	
			switch (ai_status) {
			// 공격목록이 발생하면 공격모드로 변경
			case AI_STATUS_WALK:
			case AI_STATUS_PICKUP:
				if (attackList.size() > 0)
					setAiStatus(AI_STATUS_ATTACK);
				break;
	
			// 전투 처리부분은 항상 타켓들이 공격가능한지 확인할 필요가 있음.
			case AI_STATUS_ATTACK:
			case AI_STATUS_ESCAPE:
				temp_list.clear();
				for (L1Character o : attackList) {
					if (!isAttack(o, false))
						temp_list.add(o);
				}
				for (L1Character o : temp_list) {
					attackList.remove(o);
				}
				// 전투목록이 없을경우 랜덤워킹으로 변경.
				if (attackList.size() == 0) {
					curtime = System.currentTimeMillis() / 1000;
					setQuizTime(curtime);
					setAiStatus(AI_STATUS_WALK);				
				}
				break;
			}
	
			// 죽은상태로 설정안됫을경우 변경하기.
			if (getAiStatus() != AI_STATUS_DEAD && isDead())
				setAiStatus(AI_STATUS_DEAD);
	
			ai_start_time = time;
			switch (getAiStatus()) {
			case AI_STATUS_SETTING:
				toAiSetting(time);
				break;
			case AI_STATUS_WALK:
				toAiWalk(time);
				break;
			case AI_STATUS_ATTACK:
				toAiAttack(time);
				break;
			case AI_STATUS_DEAD:
				toAiDead(time);
				break;
			case AI_STATUS_CORPSE:
				toAiCorpse(time);
				break;
			case AI_STATUS_SPAWN:
				toAiSpawn(time);
				break;
			case AI_STATUS_ESCAPE:
				toAiEscape(time);
				break;
			case AI_STATUS_PICKUP:
				toAiPickup(time);
				break;
			case AI_STATUS_SHOP:
				toShopMove(time);
				break;
			default:
				ai_time = 1000;
				break;
			}
		} catch (Exception e) {
			//System.out.println("-----------------------------------------------------------------");
			//System.out.println("------------------ " + getName() + " : 오류 ----------------------");
			//System.out.println("-----------------------------------------------------------------");
		}
	}

	private void toShopMove(long time) {
		try {
			if (targetObj == null) {
				for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 20)) {
					if (obj instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) obj;
						if (npc.getNpcId() == 5000152 || npc.getNpcId() == 4220019) {							
							x = npc.getX() - CommonUtil.random(1, 5);
							y = npc.getY() - CommonUtil.random(1, 5);
							targetObj = obj;
							break;
						}
					}
				}
			}

			if (targetObj == null) {				
				setAiStatus(AI_STATUS_WALK);
				return;
			}

			if (x != 0 && y != 0) {
				curtime = System.currentTimeMillis() / 1000;
				if (getQuizTime() + 10 > curtime) {
					if (!isDistance(getX(), getY(), getMapId(), x, y, targetObj.getMapId(), 1)) {
						ai_time = 500;
						toMoving(targetObj, x, y, getMoveState().getHeading(), true);
						return;
					}
				} else {
					targetObj = null;
					x = 0;
					y = 0;
				}
			}

			// 상태 변경.
			setAiStatus(AI_STATUS_WALK);
		} catch (Exception e) {

		}
	}

	private void TelePort1() {
		if (getMap().isSafetyZone(this.getLocation()) && (this.getMapId() == 4)) {
			for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 1)) { // 1셀앞이 npc면 텔
				if (obj instanceof L1NpcInstance) {
					Random random = new Random();
					int a = random.nextInt(3);
					location = RobotThread.getLocation();
					L1Teleport.teleport(this, location.x + a, location.y + a, (short) location.map, 5, true);
				}
			}
		}
	}

	/** 사냥터 주변 무작위 텔레포트 */
	private void TelePort2() {
		Random random = new Random();
		int a = random.nextInt(3);
		location = RobotThread.getLocation();
		L1Teleport.teleport(this, location.x + a, location.y + a, (short) location.map, 5, true);

		// 텔 이후 사냥터 주변 몬스터 어택리스트 추가
		for (L1Object obj : L1World.getInstance().getVisibleObjects(this)) {
			if (obj instanceof L1MonsterInstance && !(obj == null)) {
				addAttackList((L1MonsterInstance) obj);
			}
		}
		// 어택리스트 추가 후 모드 변경
		if (attackList.size() > 0) {
			setAiStatus(AI_STATUS_ATTACK);
			ai_time = 0;
		}
	}

	/**
	 * 마을에서 기본적인 세팅처리할때 사용.
	 * 
	 * @param time
	 */
	private void toAiSetting(long time) {
		
	}

	/**
	 * 이동처리 함수. : 랜덤워킹 하면서 주변 객체 검색함.
	 * 
	 * @param time
	 */
	private void toAiWalk(long time) {
		
	}

	/**
	 * 공격 상태일때 처리하는 함수
	 * 
	 * @param time
	 */
	private void toAiAttack(long time) {
		
	}

	/**
	 * 죽어있는 상태일때 처리하는 함수.
	 * 
	 * @param time
	 */
	private void toAiDead(long time) {
		
	}

	private void toAiCorpse(long time) {
		
	}

	private void toAiSpawn(long time) {
		
	}

	private void toAiEscape(long time) {
		
	}

	private void toAiPickup(long time) {
		
	}
}
