
package l1j.server.server.model;

import java.util.ArrayList;

import l1j.server.server.model.Instance.L1EffectInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_SkillSound;

/**
 * ȯ���� ť�� Ŭ����
*/
@SuppressWarnings("unchecked")
public class L1Cube{

	/** ť�� ����Ʈ */
	private ArrayList<L1EffectInstance> CUBE[] = new ArrayList[4];

	/** ���� Ŭ���� */
	private static L1Cube instance;

	/** �ν��Ͻ� �ʱ�ȭ */
	{
		for(int i = 0; i < CUBE.length; i++) CUBE[i] = new ArrayList<L1EffectInstance>();
	}

	/**
	 * ť�� Ŭ���� ��ȯ
	 * @return	���� Ŭ���� ��ü
	*/
	public static L1Cube getInstance(){
		if(instance == null) instance = new L1Cube();
		return instance;
	}

	/**
	 * ť�� ����Ʈ �ݳ�
	 * @param	index	����Ʈ �ε���
	*/
	private L1EffectInstance[] toArray(int index){
		return CUBE[index].toArray(new L1EffectInstance[CUBE[index].size()]);
	}
	/**
	 * ť�� ����Ʈ ���
	 * @param	index	����Ʈ �ε���
	 * @param	npc		��ϵ� npc ��ü
	*/
	public void add(int index, L1EffectInstance npc){
		if(!CUBE[index].contains(npc)){
			CUBE[index].add(npc);
		}
	}
	/**
	 * ť�� ����Ʈ ����
	 * @param	index	����Ʈ �ε���
	 * @param	npc		������ npc ��ü
	*/
	private void remove(int index, L1NpcInstance npc){
		if(CUBE[index].contains(npc)){
			CUBE[index].remove(npc);
		}
	}

	/** ����� */
	private L1Cube(){
		new CUBE1().start();
		new CUBE2().start();
		new CUBE3().start();
		new CUBE4().start();
	}

	/** 1�ܰ� */
	class CUBE1 extends Thread{
		@Override
		public void run(){
			try{
				while(true){
					sleep(1000L);
					for(L1EffectInstance npc : toArray(0)){
						// ���ӽð��� �����ٸ�
						if(npc.Cube()){
							npc.setCubePc(null);
							remove(0, npc);
							continue;
						}
						if(npc.isCube()){
							// ���� 3�� Pc �˻�
							// ť�긦 ���� ����� �� �츮��
							// �ϴ� �ٸ����� ����
							for(L1PcInstance pc : L1World.getInstance().getVisiblePlayer(npc, 3)){
								// ť�꿡 �ִ� ����� �������̰ų� ���� �����̶��
								if(npc.CubePc().getId() == pc.getId() || npc.CubePc().getClanid() == pc.getClanid() || (npc.CubePc().isInParty() && npc.CubePc().getParty().isMember(pc))){
									if(!pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_IGNITION)){
										pc.getResistance().addFire(30);
										pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_IGNITION, 8 * 1000);
										pc.sendPackets(new S_OwnCharAttrDef(pc));
									}else{
										pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_IGNITION, 8 * 1000);
										pc.sendPackets(new S_OwnCharAttrDef(pc));
									}
									pc.sendPackets(new S_SkillSound(pc.getId(), 6708));
								}else {
									if(CharPosUtil.getZoneType(pc) == 1){
										continue;
									}
									pc.receiveDamage(npc.CubePc(), 15, false);
									pc.sendPackets(new S_SkillSound(pc.getId(), 6709));
								}
							}
							npc.setCubeTime(4);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/** 2�ܰ� */
	class CUBE2 extends Thread{
		@Override
		public void run(){
			try{
				while(true){
					sleep(1000L);
					for(L1EffectInstance npc : toArray(1)){
						// ���ӽð��� �����ٸ�
						if(npc.Cube()){
							npc.setCubePc(null);
							remove(1, npc);
							continue;
						}
						if(npc.isCube()){
							// ���� 3�� Pc �˻�
							// ť�긦 ���� ����� �� �츮��
							// �ϴ� �ٸ����� ����
							for(L1PcInstance pc : L1World.getInstance().getVisiblePlayer(npc, 3)){
								// ť�꿡 �ִ� ����� �������̰ų� ���� �����̶��
								if(npc.CubePc().getId() == pc.getId() || npc.CubePc().getClanid() == pc.getClanid() || (npc.CubePc().isInParty() && npc.CubePc().getParty().isMember(pc))){
									if(!pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_QUAKE)){
										pc.getResistance().addEarth(40);
										pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_QUAKE, 8 * 1000);
										pc.sendPackets(new S_OwnCharAttrDef(pc));
									}else{
										pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_QUAKE, 8 * 1000);
										pc.sendPackets(new S_OwnCharAttrDef(pc));
									}
									pc.sendPackets(new S_SkillSound(pc.getId(), 6714));
								}else{
									if(CharPosUtil.getZoneType(pc) == 1){
										continue;
									}
									pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
									pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_FREEZE, 1 * 1000);
									pc.sendPackets(new S_SkillSound(pc.getId(), 6715));
								}
							}
							npc.setCubeTime(4);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/** 3�ܰ� */
	class CUBE3 extends Thread{
		@Override
		public void run(){
			try{
				while(true){
					sleep(1000L);
					for(L1EffectInstance npc : toArray(2)){
						// ���ӽð��� �����ٸ�
						if(npc.Cube()){
							npc.setCubePc(null);
							remove(2, npc);
							npc.deleteMe();
							continue;
						}
						if(npc.isCube()){
							// ���� 3�� Pc �˻�
							// ť�긦 ���� ����� �� �츮��
							// �ϴ� �ٸ����� ����
							for(L1PcInstance pc : L1World.getInstance().getVisiblePlayer(npc, 3)){
								// ť�꿡 �ִ� ����� �������̰ų� ���� �����̶��
								if(npc.CubePc().getId() == pc.getId() || npc.CubePc().getClanid() == pc.getClanid() || (npc.CubePc().isInParty() && npc.CubePc().getParty().isMember(pc))){
									if(!pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.STATUS_SHOCK)){
										pc.getResistance().addWind(30);
										pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_SHOCK, 8 * 1000);
										pc.sendPackets(new S_OwnCharAttrDef(pc));
									}else{
										pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_SHOCK, 8 * 1000);
										pc.sendPackets(new S_OwnCharAttrDef(pc));
									}
									pc.sendPackets(new S_SkillSound(pc.getId(), 6720));
								}else{
									if(CharPosUtil.getZoneType(pc) == 1){
										continue;
									}
									pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_DESHOCK, 8 * 1000);
									pc.sendPackets(new S_SkillSound(pc.getId(), 6721));
								}
							}
							npc.setCubeTime(4);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/** 4�ܰ� */
	class CUBE4 extends Thread{
		@Override
		public void run(){
			try{
				while(true){
					sleep(1000L);
					for(L1EffectInstance npc : toArray(3)){
						// ���ӽð��� �����ٸ�
						if(npc.Cube()){
							npc.setCubePc(null);
							remove(3, npc);
							continue;
						}
						if(npc.isCube()){
							// ���� 3�� Pc �˻�
							// ť�긦 ���� ����� �� �츮��
							// �ϴ� �ٸ����� ����
							for(L1PcInstance pc : L1World.getInstance().getVisiblePlayer(npc, 3)){
								if(pc != null){
									if(pc.getCurrentHp() > 0){
										pc.receiveDamage(npc.CubePc(), 25, false);
										pc.setCurrentMp(pc.getCurrentMp() + 5);
										pc.sendPackets(new S_SkillSound(pc.getId(), 6727));
									}
								}
							}
							npc.setCubeTime(5);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}