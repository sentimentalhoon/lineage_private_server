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

	//듀얼 시작여부
	private boolean _OxStart = false;

	public boolean getOxStart() {
		return _OxStart;
	}

	public void setOxStart(boolean ox) {
		_OxStart = ox;
	}
	// GMOpen
	public boolean isGmOpen = false;
	//듀얼 입장여부
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

	/** 퀴즈방 선착순 20명을 담기 위한 리스트 */
	private static final ArrayList<L1PcInstance> QuizuserList = new ArrayList<L1PcInstance>();
	/**
	 * 선착순 20명 등록
	 */
	public synchronized void add(L1PcInstance pc){
		/** 등록되어 있지 않고 */
		if(!QuizuserList.contains(pc)){
			QuizuserList.add(pc);
		}
	}
	/**
	 * 선착순 리스트 사이즈 반납
	 * @return	(int)	sList 의 사이즈
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
				/** 오픈 **/
				if(!isOpen() && !isGmOpen)
					continue;
				if(L1World.getInstance().getAllPlayers().size() <= 0)
					continue;

				isGmOpen = false;

				L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("[******] 지금부터 O/X QUIZ 가 시작됩니다. 참가를 원하시는 분들은 지금 입장하여 주십시오"));

				setOxStart(true); // 퀴즈 시작 true

				setOxOpen(true); // 퀴즈방 true

				spawn(); // 기란 마을에 NPC를 리스폰 시킨다.

				startClock(); // 남은 시간을 중얼 중얼

				setOxOpen(false); // 입장 시간이 끝났으므로 입장을 거부한다.

				startQuiz(); // 방식 설명 후 퀴즈를 시작한다.

				checkOxTime(); // 듀얼 가능시간을 체크

				result(); // 결과를 통보한다.

				spawnEndNpc(); // 기란 마을로 텔레포터

				setOxStart(false);

				Close = false;

				end(); // 끝났다

				Thread.sleep(180000);

				endTeleport();
			}
		} catch (Exception e1) { System.out.println(e1); }
	}


	/**
	 *퀴즈가 열려있는지 확인
	 *
	 *@return (boolean) 열려있다면 true 닫혀있다면 false
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
		L1SpawnUtil.spawn2(33440, 32805, (short) 4, 460001, 0, 180*1000, 0); // 입장 npc
		L1SpawnUtil.spawn2(32766, 32830, (short) 5120, 70514, 0, 240*1000, 0); // 헤이스트사
		L1SpawnUtil.spawn2(32766, 32846, (short) 5120, 70514, 0, 240*1000, 0); // 헤이스트사
	}

	private void spawnEndNpc(){
		L1SpawnUtil.spawn2(32773, 32831, (short) 5120, 44992, 0, 300*1000, 0); // 기란 마을 텔레포터
		L1SpawnUtil.spawn2(32759, 32831, (short) 5120, 44992, 0, 300*1000, 0); // 기란 마을 텔레포터
		L1SpawnUtil.spawn2(32759, 32845, (short) 5120, 44992, 0, 300*1000, 0); // 기란 마을 텔레포터
		L1SpawnUtil.spawn2(32773, 32845, (short) 5120, 44992, 0, 300*1000, 0); // 기란 마을 텔레포터
	} 
	private void endTeleport(){
		for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
			if(pc.getMapId() == 5120){
				CommonUtil.tryCount(pc, 33429, 32799, 4, 0, 2, 0);
			}
		}
	}
	private void startClock(){
		NpcMSG(); //엔피씨 메시지를 설정
		try {	
			for (int i = 180; i >= 1; --i) { //55초부터 5초마다 알림 by-Kingdom
				if(i % 10 == 0){
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "O/X QUIZ 시작 "+ i +" 초 전 입니다.", 2));
				}
				Thread.sleep(1000);	
			}
		} catch ( InterruptedException ie ){
			System.out.println(ie); 
			System.out.println("error : Ox 존 시작 5초타이머 에러"); //종료 카운터 by 킹덤
		}
	}

	private void startQuiz(){
		NpcMSG(); //엔피씨 메시지를 설정
		try{
			int quizusersize = QuizuserList.size();
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("O/X QUIZ 의 입장이 종료되었습니다. 다음 기회를 이용해 주세요."));
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "O/X QUIZ 를 시작합니다.", 2));
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "O/X QUIZ 에 " + quizusersize + "명이 참가 하셨습니다.", 2));
			Thread.sleep(1000);
			
			for(L1PcInstance pc : QuizuserList){
				pc.set_QuizResult(0);
				pc.save();
			}
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "방식은 문제가 나오면 1시(X)와 7시 방향(O)으로 이동하시면 됩니다", 2));
			Thread.sleep(500);
			Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, "이 곳이 O 입니다.", 2));
			Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, "이 곳이 X 입니다.", 2));
			Thread.sleep(1000);
			Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "이제 O/X QUIZ 를 시작할께요.", 2));
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
					Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, pc.getName() + "님이 모든 문제의 정답을 맞추었습니다.", 2));
					L1ItemInstance item = pc.getInventory().storeItem(20251, 1); // 만점자는 우헤헤헤
					pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
					rightanswer = 10; 
				} else if (rightanswer <= 0){  
					Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, pc.getName() + "님.. 참.. 대단하십니다.. 어떻게.. 한개도.. 못 맞추냐..", 2));
					rightanswer = 0; 
				}
				pc.sendPackets(new S_SystemMessage("총 10개의 문제 중 [" +rightanswer +"] 개를 맞추었습니다."));
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
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage("O/X QUIZ 가 종료 되었습니다."));
	}

	private void checkOxTime() {
		NpcMSG(); //엔피씨 메시지를 설정
		try {
			int round = 1;
			for (int i = 0; i < 301; i++) { //55초부터 5초마다 알림 by-Kingdom
				int question = 0;
				question = CommonUtil.random(1, QuizBoardTable.getQuizList().size());
				QuizBoard quizlist = QuizBoardTable.getQuizList().get(question);

				switch(i){
				case 10: case 40: case 70: case 100: case 130:
				case 160: case 190: case 220: case 250: case 280:
				{
					String chat = "[   제 " +  round + " 라운드   ]";  // %0 라운드
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, chat, 2));
					Thread.sleep(1000);	
					String memo = quizlist.memo;
					Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, memo, 2)); // 퀴즈를 출제한다.
					Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, memo, 2)); // 퀴즈를 출제한다.
					try{
						for (int i10 = 20; i10 >= 1; --i10) { //55초부터 5초마다 알림 by-Kingdom
							if( i10 == 15 || i10 == 10){
								Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "남은 시간은  "+ i10 + " 초입니다.!", 2));
								Broadcaster.broadcastPacket(quiz1, new S_NpcChatPacket(quiz1, memo, 2)); // 퀴즈를 출제한다.
								Broadcaster.broadcastPacket(quiz2, new S_NpcChatPacket(quiz2, memo, 2)); // 퀴즈를 출제한다.
							}
							if(i10 <= 5){
								Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "남은 시간은  "+ i10 + " 초입니다.!", 2));
							}
							Thread.sleep(1000);	
						}
					} catch (Exception e){ }

					String answer_right = "정답은 O 입니다.";
					String answer_wring = "정답은 X 입니다.";
					String right = "정답입니다.";
					String wrong = "틀렸습니다.";
					if(!quizlist.answer){//홀수번 문제의 정답은 X
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
								}//홀수번 문제의 정답은 X
							} else {
								if (pc.getX() <= 32766){ 
									pc.set_QuizResult(pc.get_QuizResult() + 1);
								}//짝수번 문제의 정답은 O
							}
						}
					}
					for(L1PcInstance pc : QuizuserList){
						int rightanswer = pc.get_QuizResult();
						pc.sendPackets(new S_SystemMessage("현재까지 " + round + "개의 문제 중 [" + rightanswer +"] 개를 맞추었습니다."));
					}
					round++;
					break;
				}
				case 300:
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "퀴즈가 종료되었습니다.", 2));
					Thread.sleep(2000);	
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "잠시 후 맞춘 갯수에 따라 상품을 드리도록 하겠습니다. 상품을 받으신 후 나가시기 바랍니다.", 2));
					Thread.sleep(2000);	
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "오늘도 우리 " + Config.servername + "서버를 이용해 주셔서 감사합니다.", 2));
					Thread.sleep(2000);	
					Broadcaster.broadcastPacket(quiz, new S_NpcChatPacket(quiz, "좋은 하루 보내세요!!", 2));
					break;
				}
				Thread.sleep(1000);
			}
		} catch ( InterruptedException ie ){ System.out.println(ie); } //종료 카운터 by 킹덤
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
