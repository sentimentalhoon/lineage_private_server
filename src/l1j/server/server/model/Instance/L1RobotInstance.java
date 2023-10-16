// ��ġ Ȯ��.
//	: ���������� �ƴҰ�� �ϴ� ������ �̵�. ���� ����.
//	: ���� ���� Ȯ���ؼ� �����ϱ�.
//	: �������� Ȯ���ؼ� ���� �����ϱ�.
//	: hp ���� Ȯ���ؼ� 100% �ɶ����� ���� �Ա�.
// �غ�Ϸ�� ����.
//	: ���� ��Ÿ�Ͽ� ���� ó���ϱ�.
//	:	��������
//	:		���� �����ΰ� ã�Ƽ� �ش� �� �Ա������� �ڷ���Ʈ
//	:		�κ� ��ü�� ������ ��� ��ü�� �� ����ó��.
//	:	��ɿ�
//	:		��ϵ� �������ǥ�� �ϳ����� �����ؼ� �̵�.
//	:		��ó�� ���̴� ���� ��� ����.
// ������ ó��.
//	: hp �� 98% �̸��ϰ�� ��������Ʈ�� �Բ� ü�� ȸ����Ű��.
//	: hp �� 30% �̸��ϰ�� ������ ��ȯ.
// �ֺ��� ���� �κ� ������ �����Ұ��.
//	: hp 90% �̸��ϰ�� ü��ȸ�� ���� �������ֱ�
//	: �������µ� Ȯ���ؼ� ������ ���ֱ�.
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

	private long ai_time; // �ΰ����� ó���� ���� ���������� ��
	private long ai_start_time; // �ΰ����� ���۵� �ð���
	private int ai_status; // �ΰ����� ó���ؾ��� ����
	private boolean ai_showment; // ��Ʈ �߻�����.

	// �ڷ���Ʈ�� ������� ��ġ �ӽ� �����.
	private RobotLocation location;
	// ��� ���۵� ���� �����.
	private int homeX;
	private int homeY;
	private int homeMap;

	// Ÿ�� ��ã���
	private L1Astar aStar;
	private L1Node tail;
	private int iCurrentPath;
	private int[][] iPath;
	protected List<L1Character> attackList;
	protected List<L1Character> temp_list;

	// pvp������ Ȯ���� ����.
	public boolean pvp;
	// �������� ���� ���� Ȯ�ο�
	private int buff_step;
	// ���� ��Ÿ�� �ΰ����ɽ� �̵��� �� ���̵�.
	private int castle_id;

	// �ΰ����� ���� ����
	static private final int AI_STATUS_SETTING = 0; // �ʹ� ����ó��
	static private final int AI_STATUS_WALK = 1; // ������ŷ ����
	static private final int AI_STATUS_ATTACK = 2; // ���� ����
	static private final int AI_STATUS_DEAD = 3; // ���� ����
	static private final int AI_STATUS_CORPSE = 4; // ��ü ����
	static private final int AI_STATUS_SPAWN = 5; // ���� ����
	static private final int AI_STATUS_ESCAPE = 6; // ���� ����
	static private final int AI_STATUS_PICKUP = 7; // ������ �ݱ� ����
	static private final int AI_STATUS_SHOP = 8; // ���������̵�ó��

	public L1RobotInstance() {
		aStar = new L1Astar();
		iPath = new int[300][2];
		attackList = new ArrayList<L1Character>();
		temp_list = new ArrayList<L1Character>();
	}

	

	private void setAiStatus(int ai_status) {
		this.ai_status = ai_status;
		// ai ���� ����ɶ����� ��Ʈǥ�� ���� �ʱ�ȭ.
		ai_showment = false;
	}

	private int getAiStatus() {
		return ai_status;
	}

	/**
	 * �ΰ����� Ȱ��ȭ�� �ð��� �̴��� Ȯ�����ִ� �Լ�.
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

	

	// /���⼭ ���ټ������ָ��

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
	 * ������ ��Ͽ� ���ó�� �Լ�.
	 * 
	 * @param o
	 */
	public void addAttackList(L1Character o) {
		if (!isDead() && !o.isDead() && o.getId() != getId() && !attackList.contains(o)) {
			// ���ݸ�Ͽ� �߰�.
			attackList.add(o);
		}

	}

	/**
	 * ��ã�⿡ ���� aStar�̿��� ó�� �Լ�.
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
	 * �̵�ó�� �Լ�.
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
	 * ���ݸ�Ͽ� ��ϵ� ��ü�� ������ ��ü�� �켱�˻��ؼ� ����. : ���ϵ� ��ü�� Ÿ������ ������.
	 * 
	 * @return
	 */
	private L1Character findDangerousObject() {
		L1Character o = null;

		// ����� �켱 �˻�.
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

		// ���� �˻�.
		for (L1Character oo : attackList) {
			if (o == null)
				o = oo;
			else if (getDistance(getX(), getY(), oo.getX(), oo.getY()) < getDistance(getX(), getY(), o.getX(), o.getY()))
				o = oo;
		}
		return o;
	}

	/**
	 * ���� ó�� �Լ�.
	 * 
	 * @param o
	 * @param x
	 * @param y
	 * @param bow
	 * @param gfxMode
	 * @param alpha_dmg
	 */
	private void toAttack(L1Object o, int x, int y, boolean bow, int gfxMode, int alpha_dmg) {

		// ���� �׼��� ���� �� �ִ� ����� ó��
		if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.ABSOLUTE_BARRIER)) { // �ƺ�Ҹ�Ʈ�ٸ����� ����
			getSkillEffectTimerSet().killSkillEffectTimer(L1SkillId.ABSOLUTE_BARRIER);
			// pc.startHpRegeneration();
			// pc.startMpRegeneration();
			startHpRegenerationByDoll();
			startMpRegenerationByDoll();
		}
		/** 2011.04.16 ������ ������ */
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
	 * ��Ŭ������ ������ �����ϰ� �Ѵ�.
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
	 * ��ü�� ���ݰ����� �������� Ȯ�����ִ� �Լ�.
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
	 * ���ӵ� ���ູ�� ó�� �Լ�.
	 */
	private void toSpeedPostion() {
		if (!isWizard() && getMoveState().getBraveSpeed() == 0) {
			// ��� ����������� �����ϱ�.
			Broadcaster.broadcastPacket(this, new S_SkillBrave(getId(), 1, 0));
			getMoveState().setBraveSpeed(1);
			getSkillEffectTimerSet().setSkillEffect(STATUS_BRAVE, 300 * 1000);
			Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 751));
			return;
		}
		if (getMoveState().getMoveSpeed() == 0) {
			// �ͱⶰ��������� �����ϱ�.
			Broadcaster.broadcastPacket(this, new S_SkillHaste(getId(), 1, 0));
			getMoveState().setMoveSpeed(1);
			getSkillEffectTimerSet().setSkillEffect(STATUS_HASTE, 300 * 1000);
			return;
		}
		if (isWizard() && !getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_BLUE_POTION)) {
			// �ķ��̶���������� �����ϱ�.
			sendPackets(new S_SkillIconGFX(34, 600));
			sendPackets(new S_SkillSound(getId(), 190));
			getSkillEffectTimerSet().setSkillEffect(STATUS_BLUE_POTION, 600 * 1000);
			return;
		}
	}

	/**
	 * ���� ����ó�� �Լ�.
	 * 
	 * @param direct
	 */
	private void toHealingPostion(boolean direct) {
		int p = (int) (((double) getCurrentHp() / (double) getMaxHp()) * 100);
		if (direct || p <= 70) { // ���� ȸ�� 70%
			// if(direct || p <= 90){ // ���� ȸ�� 90%
			// ���������� ���´� ȸ�� ����.
			if (getSkillEffectTimerSet().hasSkillEffect(71))
				return;
			// �ۼַ�Ʈ�������� ����
			cancelAbsoluteBarrier();
			// ����Ʈ ó��
			Broadcaster.broadcastPacket(this, new S_SkillSound(getId(), 197));

			int healHp = 55 * CommonUtil.random(1, 5);
			// ����Ʈ��Ÿ���� ȸ����1/2��
			if (getSkillEffectTimerSet().hasSkillEffect(POLLUTE_WATER))
				healHp /= 2;
			setCurrentHp(getCurrentHp() + healHp);
		}

		// 50�ۼ�Ʈ �̸� �Ǹ� ������ �̵��ϱ����� ���� ����.
		if (p < 30) { // 50% �̻�� �����ϱ� // �⺻ 30
			setAiStatus(AI_STATUS_SETTING);
		}
	}

	/**
	 * ��Ʈ ó�� �Լ�.
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

		// ��Ʈ�� ǥ���� �� �ִ� �������ϰ��.
		if (list.size() > 0) {
			if (!ai_showment) {
				Broadcaster.broadcastPacket(this, new S_ChatPacket(this, list.get(CommonUtil.random(0, list.size() - 1)).ment, Opcodes.S_OPCODE_NORMALCHAT, 0));
				ai_showment = true;
			}
		}
	}

	/**
	 * �ֺ� �κ����� �������� ó�� �Լ�.
	 * 
	 * @return
	 */
	private boolean toBuff() {

		// �ֺ��� �Ǵ� ���� �κ��� ������� �����ֱ�.
		if (isWizard() && getCurrentMp() >= 30) {
			// �κ� ���� ����
			List<L1RobotInstance> list = new ArrayList<L1RobotInstance>();
			// pvp���� �κ� ã��.
			for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 17)) {
				if (obj instanceof L1RobotInstance) {
					L1RobotInstance robot = (L1RobotInstance) obj;
					if (robot.pvp && this.getClanid() != robot.getClanid())// pvp���̰� ���������̶��..
						list.add(robot);
				}
			}
			// �������.
			if (list.size() > 0) {
				for (L1RobotInstance robot : list) {
					if (robot.isDead()) {
						return false;
					}
					int p2 = (int) (((double) robot.getCurrentHp() / (double) robot.getMaxHp()) * 100);
					if (p2 <= 90) {// �ǰ� 90~81% �� �����ֱ�.
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

		// ���� ������ �༮�� �� ���ݸ�Ͽ� ���.
		addAttackList(attacker);
		// �ֺ������� ��û
		for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 17)) {
			if (obj instanceof L1RobotInstance)
				if (getClanid() == ((L1RobotInstance) obj).getClanid())// ���������̶��..
					((L1RobotInstance) obj).addAttackList(attacker);
		}
	}

	/**
	 * �ΰ����� ���ۺκ�. : �ΰ����� �ð��� ��츸 ó����.
	 * 
	 * @param time
	 */
	public void toAi(long time) {
		try {
			if (!isAi(time))
				return;
	
			/** 2011.04.18 ������ �䷲������ ���½� �ȿ����̰� */
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
			// ���ݸ���� �߻��ϸ� ���ݸ��� ����
			case AI_STATUS_WALK:
			case AI_STATUS_PICKUP:
				if (attackList.size() > 0)
					setAiStatus(AI_STATUS_ATTACK);
				break;
	
			// ���� ó���κ��� �׻� Ÿ�ϵ��� ���ݰ������� Ȯ���� �ʿ䰡 ����.
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
				// ��������� ������� ������ŷ���� ����.
				if (attackList.size() == 0) {
					curtime = System.currentTimeMillis() / 1000;
					setQuizTime(curtime);
					setAiStatus(AI_STATUS_WALK);				
				}
				break;
			}
	
			// �������·� �����ȵ������ �����ϱ�.
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
			//System.out.println("------------------ " + getName() + " : ���� ----------------------");
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

			// ���� ����.
			setAiStatus(AI_STATUS_WALK);
		} catch (Exception e) {

		}
	}

	private void TelePort1() {
		if (getMap().isSafetyZone(this.getLocation()) && (this.getMapId() == 4)) {
			for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 1)) { // 1������ npc�� ��
				if (obj instanceof L1NpcInstance) {
					Random random = new Random();
					int a = random.nextInt(3);
					location = RobotThread.getLocation();
					L1Teleport.teleport(this, location.x + a, location.y + a, (short) location.map, 5, true);
				}
			}
		}
	}

	/** ����� �ֺ� ������ �ڷ���Ʈ */
	private void TelePort2() {
		Random random = new Random();
		int a = random.nextInt(3);
		location = RobotThread.getLocation();
		L1Teleport.teleport(this, location.x + a, location.y + a, (short) location.map, 5, true);

		// �� ���� ����� �ֺ� ���� ���ø���Ʈ �߰�
		for (L1Object obj : L1World.getInstance().getVisibleObjects(this)) {
			if (obj instanceof L1MonsterInstance && !(obj == null)) {
				addAttackList((L1MonsterInstance) obj);
			}
		}
		// ���ø���Ʈ �߰� �� ��� ����
		if (attackList.size() > 0) {
			setAiStatus(AI_STATUS_ATTACK);
			ai_time = 0;
		}
	}

	/**
	 * �������� �⺻���� ����ó���Ҷ� ���.
	 * 
	 * @param time
	 */
	private void toAiSetting(long time) {
		
	}

	/**
	 * �̵�ó�� �Լ�. : ������ŷ �ϸ鼭 �ֺ� ��ü �˻���.
	 * 
	 * @param time
	 */
	private void toAiWalk(long time) {
		
	}

	/**
	 * ���� �����϶� ó���ϴ� �Լ�
	 * 
	 * @param time
	 */
	private void toAiAttack(long time) {
		
	}

	/**
	 * �׾��ִ� �����϶� ó���ϴ� �Լ�.
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
