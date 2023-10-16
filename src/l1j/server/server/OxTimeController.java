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
package l1j.server.server;

import static l1j.server.server.model.skill.L1SkillId.FEATHER_BUFF_A;
import static l1j.server.server.model.skill.L1SkillId.STATUS_COMA_5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.command.executor.L1Robot3;
import l1j.server.server.datatables.QuizBoardTable;
import l1j.server.server.datatables.QuizBoardTable.QuizBoard;
import l1j.server.server.datatables.RobotTable;
import l1j.server.server.datatables.RobotTable.RobotTeleport;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1RobotInstance;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_QuizBoard;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.L1SpawnUtil;
import l1j.server.server.utils.SQLUtil;

public class OxTimeController implements Runnable {
	//private static Logger _log = Logger.getLogger(OxTimeController.class.getName());
	private static OxTimeController _instance;

	//��� ���ۿ���
	private boolean _OxStart = false;

	public boolean getOxStart() {
		return _OxStart;
	}

	public void setOxStart(boolean ox) {
		_OxStart = ox;
	}
	// GMOpen
	public boolean isGmOpen = false;
	//��� ���忩��
	@SuppressWarnings("unused")
	private boolean _OxOpen = false;

	public boolean getOxOpen() {
		return _OxStart;
	}

	public void setOxOpen(boolean ox) {
		_OxOpen = ox;
	}
	@SuppressWarnings("unused")
	private boolean Close = false;

	public static OxTimeController getInstance() {
		if (_instance == null) {
			_instance = new OxTimeController();
		}
		return _instance;
	}

	/** ����� ������ 20���� ��� ���� ����Ʈ */
	private static final ArrayList<L1PcInstance> QuizuserList = new ArrayList<L1PcInstance>();
	/**
	 * ������ 20�� ���
	 */
	public synchronized void add(L1PcInstance pc){
		/** ��ϵǾ� ���� �ʰ� */
		if(!QuizuserList.contains(pc)){
			QuizuserList.add(pc);
		}
	}
	/**
	 * ������ ����Ʈ ������ �ݳ�
	 * @return	(int)	sList �� ������
	 */
	public int size(){
		return QuizuserList.size();
	}

	static int[] loc = { -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8 };

	L1NpcInstance quiz = null;
	L1NpcInstance quiz1 = null;
	L1NpcInstance quiz2 = null;
	L1NpcInstance npc = null;
	public void run() {
		try {
			while (true) {
				Thread.sleep(60000);
				/** ���� **/
				if(!isOpen() && !isGmOpen)
					continue;
				if(L1World.getInstance().getAllPlayers().size() <= 0)
					continue;

				isGmOpen = false;

				L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("[******] ���ݺ��� O/X QUIZ �� ���۵˴ϴ�. ������ ���Ͻô� �е��� ���� �����Ͽ� �ֽʽÿ�"));

				setOxStart(true); // ���� ���� true

				setOxOpen(true); // ����� true

				spawn(); // ��� ������ NPC�� ������ ��Ų��.

				startClock(); // ���� �ð��� �߾� �߾�

				setOxOpen(false); // ���� �ð��� �������Ƿ� ������ �ź��Ѵ�.

				startQuiz(); // ��� ���� �� ��� �����Ѵ�.

				checkOxTime(); // ��� ���ɽð��� üũ

				result(); // ����� �뺸�Ѵ�.

				spawnEndNpc(); // ��� ������ �ڷ�����

				setOxStart(false);

				Close = false;

				end(); // ������

				Thread.sleep(180000);

				endTeleport();
			}
		} catch (Exception e1) { System.out.println(e1); }
	}


	/**
	 *��� �����ִ��� Ȯ��
	 *
	 *@return (boolean) �����ִٸ� true �����ִٸ� false
	 */
	private boolean isOpen() {
		int nowtime = Integer.valueOf(CommonUtil.dateFormat("HHmm"));
		if(nowtime == 225){ return true; }
		if(nowtime == 625){ return true; }
		if(nowtime == 1225){ return true; }
		if(nowtime == 1825){ return true; }
		if(nowtime == 2025){ return true; }
		if(nowtime == 2225){ return true; }
		if(nowtime == 25){ return true; }
		return false;
	}

	private void spawn(){
		L1SpawnUtil.spawn2(33440, 32805, (short) 4, 460001, 0, 180*1000, 0); // ���� npc
		L1SpawnUtil.spawn2(32766, 32830, (short) 5120, 70514, 0, 240*1000, 0); // ���̽�Ʈ��
		L1SpawnUtil.spawn2(32766, 32846, (short) 5120, 70514, 0, 240*1000, 0); // ���̽�Ʈ��
	}

	private void spawnEndNpc(){
		L1SpawnUtil.spawn2(32773, 32831, (short) 5120, 44992, 0, 300*1000, 0); // ��� ���� �ڷ�����
		L1SpawnUtil.spawn2(32759, 32831, (short) 5120, 44992, 0, 300*1000, 0); // ��� ���� �ڷ�����
		L1SpawnUtil.spawn2(32759, 32845, (short) 5120, 44992, 0, 300*1000, 0); // ��� ���� �ڷ�����
		L1SpawnUtil.spawn2(32773, 32845, (short) 5120, 44992, 0, 300*1000, 0); // ��� ���� �ڷ�����
	} 
	private void endTeleport(){
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if(pc.getMapId() == 5120){
				CommonUtil.tryCount(pc, 33429, 32799, 4, 0, 2, 0);
			}
		}
	}
	private void startClock(){
		NpcMSG(); //���Ǿ� �޽����� ����
		try {	
			for (int i = 180; i >= 1; --i) { //55�ʺ��� 5�ʸ��� �˸� by-Kingdom
				if(i % 10 == 0){
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "O/X QUIZ ���� "+ i +" �� �� �Դϴ�.", 2));
				}
				Thread.sleep(1000);	
			}
		} catch ( InterruptedException ie ){
			System.out.println(ie); 
			System.out.println("error : Ox �� ���� 5��Ÿ�̸� ����"); //���� ī���� by ŷ��
		}
	}

	private void startQuiz(){
		NpcMSG(); //���Ǿ� �޽����� ����
		try{
			int quizusersize = QuizuserList.size();
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("O/X QUIZ �� ������ ����Ǿ����ϴ�. ���� ��ȸ�� �̿��� �ּ���."));
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "O/X QUIZ �� �����մϴ�.", 2));
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "O/X QUIZ �� " + quizusersize + "���� ���� �ϼ̽��ϴ�.", 2));
			Thread.sleep(1000);
			
			for(L1PcInstance pc : QuizuserList){
				pc.set_QuizResult(0);
				pc.save();
			}
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "����� ������ ������ 1��(X)�� 7�� ����(O)���� �̵��Ͻø� �˴ϴ�", 2));
			Thread.sleep(500);
			Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, "�� ���� O �Դϴ�.", 2));
			Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, "�� ���� X �Դϴ�.", 2));
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "���� O/X QUIZ �� �����Ҳ���.", 2));
		} catch ( Exception e){ System.out.println(e); }
	}

	private void result(){
		for(L1PcInstance pc : QuizuserList){
			if (pc.getMapId() == 5120){
				L1SkillUse l1skilluse = null;
				int rightanswer = 0;
				l1skilluse = new L1SkillUse();
				l1skilluse.handleCommands(pc, STATUS_COMA_5, pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				l1skilluse.handleCommands(pc, FEATHER_BUFF_A, pc.getId(), pc.getX(), pc.getY(), null, 0, L1SkillUse.TYPE_GMBUFF);
				rightanswer = pc.get_QuizResult();
				if (rightanswer >= 10){ 
					Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, pc.getName() + "���� ��� ������ ������ ���߾����ϴ�.", 2));
					L1ItemInstance item = pc.getInventory().storeItem(20251, 1); // �����ڴ� ��������
					pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
					rightanswer = 10; 
				} else if (rightanswer <= 0){  
					Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, pc.getName() + "��.. ��.. ����Ͻʴϴ�.. ���.. �Ѱ���.. �� ���߳�..", 2));
					rightanswer = 0; 
				}
				pc.sendPackets(new S_SystemMessage("�� 10���� ���� �� [" +rightanswer +"] ���� ���߾����ϴ�."));
				L1ItemInstance item = pc.getInventory().storeItem(400078, rightanswer);
				pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
				try{
					pc.set_QuizResult(0);
					pc.setAinHasad(2000000);
					pc.save();
				} catch (Exception e){ }
			}
		}
	}
	private void end(){
		QuizuserList.clear();
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("O/X QUIZ �� ���� �Ǿ����ϴ�."));
	}

	private void checkOxTime() {
		NpcMSG(); //���Ǿ� �޽����� ����
		try {
			int round = 1;
			for (int i = 0; i < 301; i++) { //55�ʺ��� 5�ʸ��� �˸� by-Kingdom
				int question = 0;
				question = CommonUtil.random(1, QuizBoardTable.getQuizList().size());
				QuizBoard quizlist = QuizBoardTable.getQuizList().get(question);

				switch(i){
				case 10: case 40: case 70: case 100: case 130:
				case 160: case 190: case 220: case 250: case 280:
				{
					String chat = "[   �� " +  round + " ����   ]";  // %0 ����
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, chat, 2));
					Thread.sleep(1000);	
					String memo = quizlist.memo;
					Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, memo, 2)); // ��� �����Ѵ�.
					Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, memo, 2)); // ��� �����Ѵ�.
					try{
						for (int i10 = 20; i10 >= 1; --i10) { //55�ʺ��� 5�ʸ��� �˸� by-Kingdom
							if( i10 == 15 || i10 == 10){
								Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "���� �ð���  "+ i10 + " ���Դϴ�.!", 2));
								Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, memo, 2)); // ��� �����Ѵ�.
								Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, memo, 2)); // ��� �����Ѵ�.
							}
							if(i10 <= 5){
								Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "���� �ð���  "+ i10 + " ���Դϴ�.!", 2));
							}
							Thread.sleep(1000);	
						}
					} catch (Exception e){ }

					String answer_right = "������ O �Դϴ�.";
					String answer_wring = "������ X �Դϴ�.";
					String right = "�����Դϴ�.";
					String wrong = "Ʋ�Ƚ��ϴ�.";
					if(!quizlist.answer){//Ȧ���� ������ ������ X
						Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, answer_wring, 2));
						Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, right, 2)); 
						Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, wrong, 2)); 
					} else {
						Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, answer_right, 2));
						Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, right, 2)); 
						Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, wrong, 2)); 
					}
					for(L1PcInstance pc : QuizuserList){
						if (pc.getMapId() == 5120){
							if(!quizlist.answer){ 
								if (pc.getX() >= 32766){ 
									pc.set_QuizResult(pc.get_QuizResult() + 1);
								}//Ȧ���� ������ ������ X
							} else {
								if (pc.getX() <= 32766){ 
									pc.set_QuizResult(pc.get_QuizResult() + 1);
								}//¦���� ������ ������ O
							}
						}
					}
					for(L1PcInstance pc : QuizuserList){
						int rightanswer = pc.get_QuizResult();
						pc.sendPackets(new S_SystemMessage("������� " + round + "���� ���� �� [" + rightanswer +"] ���� ���߾����ϴ�."));
					}
					round++;
					break;
				}
				case 300:
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "��� ����Ǿ����ϴ�.", 2));
					Thread.sleep(2000);	
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "��� �� ���� ������ ���� ��ǰ�� �帮���� �ϰڽ��ϴ�. ��ǰ�� ������ �� �����ñ� �ٶ��ϴ�.", 2));
					Thread.sleep(2000);	
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "���õ� �츮 " + Config.servername + "������ �̿��� �ּż� �����մϴ�.", 2));
					Thread.sleep(2000);	
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "���� �Ϸ� ��������!!", 2));
					break;
				}
				Thread.sleep(1000);
			}
		} catch ( InterruptedException ie ){ System.out.println(ie); } //���� ī���� by ŷ��
	}

	private void NpcMSG(){
		for (L1Object obj : L1World.getInstance().getObject()) {
			if(obj instanceof L1NpcInstance){
				npc = (L1NpcInstance)obj;
				if(npc.getNpcId() == 44989){
					quiz2 = npc;
				}
				if(npc.getNpcId() == 44990){
					quiz1 = npc;
				}
				if(npc.getNpcId() == 44991){
					quiz = npc;
				}
			}
		}
	}
}
