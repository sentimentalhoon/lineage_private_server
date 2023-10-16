

package l1j.server.GameSystem;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.text.SimpleDateFormat;
import l1j.server.Config; // �̺�Ʈ �ⱸ
import server.LineageClient;
import l1j.server.server.datatables.DoorSpawnTable;
import l1j.server.server.ActionCodes;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1UbPattern;
import l1j.server.server.model.L1UbSpawn;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.Instance.L1DoorInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.L1GroundInventory;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.trap.L1OpenTrap;
import l1j.server.server.datatables.UBSpawnTable;
import l1j.server.server.datatables.UBTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.templates.L1Item;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_BlueMessage;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.utils.L1SpawnUtil;
import l1j.server.server.GeneralThreadPool;



public class InDunController extends Thread {
	//public class GiranController implements Runnable {
	//  public class HuntController implements Runnable{
	//private static Logger _log = Logger.getLogger(HuntController.class.getName());
	private static InDunController _instance;


	/** �̺�Ʈ ���۾˸� **/

	String[] chat = {"ù��°","�ι�° ","����°","�׹�°","�ټ���°","������°","�ϰ���°"
			,"������°","��ȩ��°","����°","���ѹ�°","������"};
	L1NpcInstance hadin = null;
	L1NpcInstance hadin1 = null;

	private boolean _InDunStart = false;// �δ� ���ۿ���
	public boolean getInDunStart() { return _InDunStart;}
	public void setInDunStart(boolean indun) {_InDunStart = indun; }

	private boolean _InDunOpen = false; // �δ� ���ۿ���
	public boolean getInDunOpen() { return _InDunStart; }
	public void setInDunOpen(boolean indun) { _InDunOpen = indun;}

	private boolean _StartOpenDoor = false; // ���Ѵ��� ��� ��ŸƮ ���� ���ȴ��� ����
	public boolean getStartOpenDoor(){return _StartOpenDoor;}
	public void setStartOpenDoor(boolean indun){_StartOpenDoor = indun;}

	private boolean _EndOpenDoor = false; // ������ ������ ������ ���� ���� ���ȴ��� üũ
	public boolean getEndOpenDoor(){return _EndOpenDoor;}
	public void setEndOpenDoor(boolean indun){_EndOpenDoor = indun;}

	private boolean _EndTrap = false; // ������ ������ ������ ���� ���� ���ȴ��� üũ
	public boolean getEndTrap(){return _EndTrap;}
	public void setEndTrap(boolean indun){_EndTrap = indun;}

	private boolean _TellTrap = false; // �ڸ���Ʈ Ʈ���� 
	public boolean getTellTrap(){return _TellTrap;}
	public void setTellTrap(boolean indun){_TellTrap = indun;}

	private boolean _TrapFour = false; // ù��° 4Ʈ�� ���ȴ��� ����
	public boolean getTrapFour(){return _TrapFour;}
	public void setTrapFour(boolean indun){_TrapFour = indun;}

	private boolean _TrapFour1 = false; // �ι�° 4Ʈ�� ���ȴ��� ����
	public boolean getTrapFour1(){return _TrapFour1;}
	public void setTrapFour1(boolean indun){_TrapFour1 = indun;}

	private boolean _TrapFour2 = false; // ����° 4Ʈ�� ���ȴ��� ����
	public boolean getTrapFour2(){return _TrapFour2;}
	public void setTrapFour2(boolean indun){_TrapFour2 = indun;}

	private boolean _LastTrapFour = false; // ������ ���� Ʈ��
	public boolean getLastTrapFour(){return _LastTrapFour;}
	public void setLastTrapFour(boolean indun){_LastTrapFour = indun;}

	private boolean _GiveItem = false; // ������ ���޵ƴ��� ����
	public boolean getGiveItem(){return _GiveItem;}
	public void setGiveItem(boolean indun){_GiveItem = indun;}

	private boolean _FirstDoor = false; // ��ŸƮ��
	public boolean getFirstDoor(){return _FirstDoor;}
	public void setFirstDoor(boolean indun){_FirstDoor = indun;}

	private boolean _CenterDoor = false; // �ذ��,������ ��
	public boolean getCenterDoor(){return _CenterDoor;}
	public void setCenterDoor(boolean indun){_CenterDoor = indun;}

	private boolean _LastDoor = false; //������ ��
	public boolean getLastDoor(){return _LastDoor;}
	public void setLastDoor(boolean indun){_LastDoor = indun;}

	private final List<L1PcInstance> _pushList = new ArrayList<L1PcInstance>();
	private final List<L1PcInstance> _pushList1 = new ArrayList<L1PcInstance>();
	private final List<L1PcInstance> _pushList2 = new ArrayList<L1PcInstance>();
	private final List<L1PcInstance> _pushList3 = new ArrayList<L1PcInstance>();
	private final ArrayList<L1PcInstance> playmember = new ArrayList<L1PcInstance>();

	public void addPlayMember(L1PcInstance pc)		{ playmember.add(pc);}
	public int getPlayMembersCount() 				{ return playmember.size(); 		}
	public void removePlayMember(L1PcInstance pc) 	{ playmember.remove(pc); 			}
	public void clearPlayMember() 					{ playmember.clear();				}
	public boolean isPlayMember(L1PcInstance pc) 	{ return playmember.contains(pc);	}
	public L1PcInstance[] getPlayMemberArray() {	
		return playmember.toArray(new L1PcInstance[getPlayMembersCount()]);	
	}

	private boolean Close;

	private static Random _random = new Random(System.nanoTime());



	public static InDunController getInstance() {
		if(_instance == null) {
			_instance = new InDunController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try    {
			/** �δ� ���� **/
			setInDunOpen(true);// ������ �ް�
			setInDunStart(true);// ������ �ް�
			NpcMSG(); //���Ǿ� �޽����� ����
			/** �δ� ���� ���θ� �Ǵ��Ѵ�.**/      
			try{ Thread.sleep(3000); }catch(Exception e){} //3���� �޼��� ����
			/** ������ �ź��Ѵ�. **/
			Hadin1MSG(1);//�ϵ� �޽��� ����
			StarZone(0); //���� �������ְ�
			setTellTrap(true); // ���ÿ� ���� ������ �޴´�.
			try{ Thread.sleep(15000); }catch(Exception e){}//15����
			MSG(1); // ����Ʈ �޼��� ����.
			setTellTrap(false); // �޼��� ������ ������  ���� ���� �ݴ´�.
			OpenDoor(9000);//���� �����ش�.
			setFirstDoor(true);
			int j = 0;
			while (j <= 1200){ // 20�е��� ���� ���������� �ݺ�. �ذ��
				if(getStartOpenDoor() == true){ break; }
				Thread.sleep(1000L);
				StartDoorOpen();
				PushTrap();
				++j;
			}
			if(getStartOpenDoor() == false){ // �ð����� ������ ���ϸ� ����
				End();

			}else{
				try { Thread.sleep(60000L); } catch(Exception e) { }
				MSG(5);
				OpenTheDoor(); // 1���� ���� ����.
				MSG(6);
				HadinMSG(1); // ���ÿ� �޼��� �����ϰ�.
				L1UbPattern pattern = null;
				ArrayList<L1UbSpawn> spawnList = null;
				for (int round = 1; round <= 12; round++) {
					if (round == 4){HadinMSG(2);}
					if (round == 8){HadinMSG(3);}
					if (round == 12){HadinMSG(4);}
					try { Thread.sleep(30000L); } catch(Exception e) { }
					pattern = UBSpawnTable.getInstance().getPattern(10, 1);
					spawnList = pattern.getSpawnList(round);
					Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, chat[round-1]	+" ������ ��ü �Ǿ����ϴ�.["+round+"/12]", 0));
					Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE,  chat[round-1] +" ������ ��ü �Ǿ����ϴ�.["+round+"/12]"));
					Effect();
					for (L1UbSpawn spawn : spawnList) {
						spawn.spawnAll();
						Thread.sleep(spawn.getSpawnDelay() * 1000);
					}
				}    				
				Boss();
				try { Thread.sleep(5000L); } catch(Exception e) { }
				Kereness();
				HadinMSG(5);
				LastDoorOpen();   					
				MSG(7); // ������ ���ް��� �޼��� ����.
				int k = 0;
				while (k <= 300){ // 5�е��� Ʈ���� ��� ������ ���� �ݺ�
					if(getLastTrapFour() == true){ 
						break; 
					}
					LastPushTrap();
					Thread.sleep(1000L);
					++k;
				}
				if(getLastTrapFour() == false){ // 5�г��� Ʈ���� �ȹ����� ����.
					End(); // ����

				}else{
					MSG(8);
					try { Thread.sleep(30000L); } catch(Exception e) { }
				}
				End();

			}
			_instance = null;          // ������ ���ῡ ����ؼ� �ν��Ͻ� �ʱ�ȭ
			_InDunStart = false;      // ������ ���ῡ ����ؼ� �ٽ� false�� �ʱ�ȭ
			_InDunOpen = false;     // ��������
		} catch(Exception e1) {}

	}

	private int PatternChoice() {
		int i = _random.nextInt(1);
		return i;
	}
	public void OpenDoor(int i){
		L1DoorInstance door = DoorSpawnTable.getInstance().getDoor(i);
		if (door != null){
			if(door.getOpenStatus() == ActionCodes.ACTION_Close){
				door.open();
			}
		}		
	}
	private void OpenTheDoor(){ // ������ �ذ�� ������.
		setCenterDoor(true);
	int []Door = {9017,9016};
	int j = Door.length;
	for(int i = 0;i < j;i++ ){
		OpenDoor(Door[i]);
	}
	}          
	private void LastDoorOpen(){
		setLastDoor(true);
		Effect();// ���� ����.
		int []Door = {9033,9034,9035,9036,9037,9038,9039,9040,9041,9042,9043,9045,9046,9047,9048};
		int j = Door.length;
		for(int i = 0;i < j;i++ ){
			OpenDoor(Door[i]);
			try { Thread.sleep(500L); } catch(Exception e) { }
		}
	}
	private void StartDoorOpen(){	// ���� ���ȴ��� üũ	
		L1DoorInstance door = DoorSpawnTable.getInstance().getDoor(9015);
		if (door != null){
			if(door.getOpenStatus() == ActionCodes.ACTION_Open){
				setStartOpenDoor(true);
				Effect();
			}
		}
	}
	private void DoorClose(){ // ��ü ���ݱ�
		L1DoorInstance door = null;
		for (L1Object object : L1World.getInstance().getVisibleObjects(9000).values()) {
			if (object instanceof L1DoorInstance) {
				door = (L1DoorInstance) object;
				if (door.getMapId() == 9000 && door.getOpenStatus() == ActionCodes.ACTION_Open) {
					setStartOpenDoor(false);
					setEndOpenDoor(false);
					door.close();
				}
			}
		}
	}                   
	/** �δ� ����� ���� Ŭ���� */
	private void InDunclear() {
		L1MonsterInstance mob = null;
		for (Object obj : L1World.getInstance().getVisibleObjects(9000).values()) {
			if (obj instanceof L1MonsterInstance) {
				mob = (L1MonsterInstance) obj;
				if (!mob.isDead()) {
					mob.setDead(true);
					mob.setActionStatus(ActionCodes.ACTION_Die);
					mob.setCurrentHp(0);
					mob.deleteMe();
				}
			}
		}
	}
	private void HadinMSG(int i){
		switch(i){
		case 1:
			try { Thread.sleep(15000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "��� ��� ���Գ� �ð��� ���� ����.", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ��� ��� ���Գ� �ð��� ���� ����."));
			sendMessage("�ϵ�: ��� ��� ���Գ� �ð��� ���� ����.");
			try { Thread.sleep(40000L); } catch(Exception e) { }
			setCenterDoor(false);// ���⼭ ���� ������ �ݾ�����.
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "������ ��谡 �Ҿ������� ƴ�� Ÿ. ���� �͵��� ���� �ð��̳�.", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ������ ��谡 �Ҿ������� ƴ�� Ÿ. ���� �͵��� ���� �ð��̳�."));
			sendMessage("�ϵ�: ������ ��谡 �Ҿ������� ƴ�� Ÿ. ���� �͵��� ���� �ð��̳�.");
			try { Thread.sleep(5000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "��� �غ��� �ְԳ�!.", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ��� �غ��� �ְԳ�!"));
			sendMessage("�ϵ�: ��� �غ��� �ְԳ�!.");
			try { Thread.sleep(5000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "���󺸴� ������ �ٰ����� �ֳ�! ���ɵ� �ϰ�!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ���󺸴� ������ �ٰ����� �ֳ�! ���ɵ� �ϰ�!"));
			sendMessage("�ϵ�: ���󺸴� ������ �ٰ����� �ֳ�! ���ɵ� �ϰ�!");
			break;
		case 2:
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "���󺸴� ������ �ٰ����� �ֳ�! ���ɵ� �ϰ�!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ���󺸴� ������ �ٰ����� �ֳ�! ���ɵ� �ϰ�!"));
			sendMessage("�ϵ�: ���󺸴� ������ �ٰ����� �ֳ�! ���ɵ� �ϰ�!");
			break;
		case 3:
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "���Ѱ͵��� �� �������� �ֳ�! �غ���ϰ�!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ���Ѱ͵��� �� �������� �ֳ�! �غ���ϰ�!"));
			sendMessage("�ϵ�: ���Ѱ͵��� �� �������� �ֳ�! �غ���ϰ�!");
			break;
		case 4:
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "��ε� �����ϰ�! �Ŵ��� ����� �ٰ�����!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ��ε� �����ϰ�! �Ŵ��� ����� �ٰ�����!"));
			sendMessage("�ϵ�: ��ε� �����ϰ�! �Ŵ��� ����� �ٰ�����!");
			break;
		case 5:
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "�ɷ��Ͻ�! �� ������ �̸��� �����ְ�!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: [�ɷ��Ͻ�]! �� ������ �̸��� �����ְ�!"));
			sendMessage("�ϵ�: �ɷ��Ͻ�! �� ������ �̸��� �����ְ�!");
			try { Thread.sleep(5000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "��ε� ���� ���ҳ�.", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ��ε� ���� ���ҳ�."));
			sendMessage("�ϵ�: ��ε� ���� ���ҳ�.");
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "�ɷ��Ͻ�! �ڴ� ���� �ðܵ� �ǳ�.", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: �ɷ��Ͻ�! �ڴ� ���� �ðܵ� �ǳ�."));
			sendMessage("�ϵ�: �ɷ��Ͻ�! �ڴ� ���� �ðܵ� �ǳ�.");
			try { Thread.sleep(5000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "�ɷ��Ͻ�! � ��������!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: �ɷ��Ͻ�! � ��������!"));
			sendMessage("�ϵ�: �ɷ��Ͻ�! � ��������!");
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "�ɷ��Ͻ��� �������Ѷ�!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: �ɷ��Ͻ��� �������Ѷ�!"));
			sendMessage("�ϵ�: �ɷ��Ͻ��� �������Ѷ�!");
			try { Thread.sleep(30000L); } catch(Exception e) { }
			Effect();
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "�̰� ���� ��������!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: �̰� ���� ��������!"));
			sendMessage("�ϵ�: �̰� ���� ��������!");
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "�̰��� �������� �ϴ°��ΰ�? ���� �� �����ߴµ�!!!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: �̰��� �������� �ϴ°��ΰ�? ���� �� �����ߴµ�!!!"));
			sendMessage("�ϵ�: �̰��� �������� �ϴ°��ΰ�? ���� �� �����ߴµ�!!!");
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "��ε� �����϶�! �̰��� �����ϰڴ�!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ��ε� �����϶�! �̰��� �����ϰڴ�!"));
			sendMessage("�ϵ�: ���� �ⱸ�� �� �����״� � ����������!");
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.wideBroadcastPacket(hadin, new S_NpcChatPacket(hadin, "���� �ⱸ�� �� �����״� � ����������!", 0));
			//Broadcaster.broadcastPacket(hadin, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ���� �ⱸ�� �� �����״� � ����������!"));
			sendMessage("�ϵ�: ���� �ⱸ�� �� �����״� � ����������!");

			break;
		}
	}
	private void Hadin1MSG(int i){
		switch(i){
		case 1:
			Broadcaster.broadcastPacket(hadin1, new S_NpcChatPacket(hadin1,"�� �Դ°�? ��� ��ٸ��Գ� �غ��� ���� �ֳ�.", 0));
			//Broadcaster.broadcastPacket(hadin1, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: �� �Դ°�? ��� ��ٸ��Գ� �غ��� ���� �ֳ�."));
			sendMessage("�ϵ�: �� �Դ°�? ��� ��ٸ��Գ� �غ��� ���� �ֳ�.");
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.broadcastPacket(hadin1, new S_NpcChatPacket(hadin1,"���� ������ �����غ���?.", 0));
			sendMessage("�ϵ�: ���� ������ �����غ���?.");
			//Broadcaster.broadcastPacket(hadin1, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ���� ������ �����غ���?"));         			
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.broadcastPacket(hadin1, new S_NpcChatPacket(hadin1,"��! �׷� �����غ���?", 0));
			sendMessage("�ϵ�: ��! �׷� �����غ���?");
			//Broadcaster.broadcastPacket(hadin1, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ��! �׷� �����غ���?"));         			
			try { Thread.sleep(10000L); } catch(Exception e) { }
			Broadcaster.broadcastPacket(hadin1, new S_NpcChatPacket(hadin1,"���θ���. �������� �ݹ� ���������.", 0));
			//Broadcaster.broadcastPacket(hadin1, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: ���θ���. �������� �ݹ� ���������."));
			sendMessage("�ϵ�: ���θ���. �������� �ݹ� ���������.");   		        	
			try { Thread.sleep(5000L); } catch(Exception e) { }
			Broadcaster.broadcastPacket(hadin1, new S_NpcChatPacket(hadin1,"�� �������� ���� ���ֵ��� �ϰڳ�.", 0));
			//Broadcaster.broadcastPacket(hadin1, new S_PacketBox(S_PacketBox.GREEN_MESSAGE, "�ϵ�: �� �������� ���� ���ֵ��� �ϰڳ�."));
			sendMessage("�ϵ�: �� �������� ���� ���ֵ��� �ϰڳ�.");
			break;
		}
	}
	public void NpcMSG(){
		L1NpcInstance npc = null;
		for (L1Object obj : L1World.getInstance().getObject()) {
			if(obj instanceof L1NpcInstance){
				npc = (L1NpcInstance)obj;
				if(npc.getNpcId() == 4100037){
					hadin = npc;
				}else if(npc.getNpcId() == 4100032){
					hadin1 = npc;
				}
			}
		}
	}
	private void Boss(){
		int i = PatternChoice();
		switch(i){
		case 0:
			L1SpawnUtil.spawn1(32707, 32846, (short)9000, 1, 420026, 1, false);
			break;
		case 1:
			L1SpawnUtil.spawn1(32707, 32846, (short)9000, 1, 420022, 1, false);
			break;       		
		}
	}
	private void Kereness(){
		L1SpawnUtil.spawn1(32707, 32846, (short)9000, 1, 4100033, 1, false);		
	}
	private void StarZone(int i){
		switch(i){
		case 0: // �����
		L1SpawnUtil.spawn4(32726, 32725, (short)9000, 1, 4100039, 1, false, 30000);
		break;
		case 1: // ù��° ����
			L1SpawnUtil.spawn4(32667, 32818, (short)9000, 1, 4100039, 1, false, 300000);
			break;
		case 2: // �ι�° ����
			L1SpawnUtil.spawn4(32808, 32838, (short)9000, 1, 4100039, 1, false, 300000);
			L1SpawnUtil.spawn4(32789, 32822, (short)9000, 1, 4100039, 1, false, 300000);
			break;
		case 3: // ������ ���� ����
			L1SpawnUtil.spawn4(32802, 32868, (short)9000, 1, 4100044, 1, false, 60000);
			break;
		}
	}

	private void sendMessage(String msg) {
		for (L1PcInstance pc : getPlayMemberArray()){
			//pc.sendPackets(new S_SystemMessage(msg));
			pc.sendPackets(new S_PacketBox(S_PacketBox.GREEN_MESSAGE, msg));
		}
	}
	private void MSG(int i) {
		switch(i){
		case 1:
			sendMessage("�ϵ�: �̰��� ������ ���̴� �ܺ����� �������� ���ϰ�");
			sendMessage("�ϵ�: ��ġ�� ���ֱ� �ٶ���.");
			try { Thread.sleep(5000L); } catch(Exception e) { }
			sendMessage("�ϵ�: ����鸸 �� �� �ִ� ��ġ�� �صξ����� �����ֱ� �ٶ��");
			try { Thread.sleep(5000L); } catch(Exception e) { }
			sendMessage("�ϵ�: �׷�  ���� �ִ°� ���� �ִ��� ���� ���ְԳ�.");
			Effect();
			break;
		case 2:
			sendMessage("�ϵ�: � ȭ�� Ʈ���� ��ġ�ϰ� �������� ���ֱ�");
			sendMessage("�ϵ�: �ٶ��.����鿡�� ������ �� �ϰų�.");
			Effect();
			break;
		case 3:
			sendMessage("�ϵ�: �����ߴ�! �� ���ѷ� �ֱ� �ٶ��� �̹����� �� �����ϳ�.");
			sendMessage("�ϵ�: �������� �Ѿ���� �������� �����.");
			Effect();
			break;  	 
		case 4:
			sendMessage("�ϵ�: ����! ���� �����⸦�� óġ�ϰ� �������� �Ѿ����");
			Effect();
			break; 
		case 5:
			sendMessage("�ϵ�: ���� �� �ǽ��� ���۵ǳ�.��� �������� ����.");
			Effect();
			break; 
		case 6:
			try { Thread.sleep(5000L); } catch(Exception e) { }
			sendMessage("�ϵ�: �ð��� ���ξ���. ���� ����� �������� ���� ������ ���ϰ� �ؾ��ϳ�");
			break; 
		case 7:
			try { Thread.sleep(30000L); } catch(Exception e) { }
			sendMessage("�������� �߾� ������ �����ø� ����������� ���޵˴ϴ�.");
			break; 
		case 8:
			sendMessage("1���� ���� ������ ����˴ϴ�.");
			break; 
		}
	}        
	private void EndTelePort() { // ���� ��Ų��. 
		for(L1PcInstance c : getPlayMemberArray()) {
			L1Teleport.teleport(c, 32596, 32916, (short) 0, 4, true);
		}
	} 
	private void Effect() { // ȭ�� ���� ����Ʈ.
		for(L1PcInstance c : getPlayMemberArray()) {
			c.sendPackets(new S_SkillSound(c.getId(), 1249));
		}
	}
	public void spawnGroundItem(int itemId, int stackCount, int count) {
		L1Item temp = ItemTable.getInstance().getTemplate(itemId);
		if (temp == null) {
			return;
		}
		L1Location loc = null;
		L1ItemInstance item = null;
		L1GroundInventory ground = null;
		short mapid = 9000;
		for (int i = 0; i < count; i++) {
			if (temp.isStackable()) {
				item = ItemTable.getInstance().createItem(itemId);
				item.setEnchantLevel(0);
				item.setCount(stackCount);
				int x = 32796 + _random.nextInt(8);
				int y =	32865 + _random.nextInt(11);
				ground = L1World.getInstance().getInventory(x, y, mapid);
				if (ground.checkAddItem(item, stackCount) == L1Inventory.OK) {
					ground.storeItem(item);
				}
			} else {
				item = null;
				for (int createCount = 0; createCount < stackCount; createCount++) {
					item = ItemTable.getInstance().createItem(itemId);
					item.setEnchantLevel(0);
					int x = 32796 + _random.nextInt(8);
					int y =	32865 + _random.nextInt(11);
					ground = L1World.getInstance().getInventory(x, y, mapid);
					if (ground.checkAddItem(item, stackCount) == L1Inventory.OK) {
						ground.storeItem(item);
					}
				}
			}
		}
	}
	public void addpc(L1PcInstance pc) {
		if (pc == null || _pushList.contains(pc)) {return;}
		_pushList.add(pc);}
	public void removepc(L1PcInstance pc) {
		if (pc == null || !_pushList.contains(pc)) {return;}
		_pushList.remove(pc);}   

	public void addpc1(L1PcInstance pc) {
		if (pc == null || _pushList1.contains(pc)) {return;}
		_pushList1.add(pc);}
	public void removepc1(L1PcInstance pc) {
		if (pc == null || !_pushList1.contains(pc)) {return;}
		_pushList1.remove(pc);}

	public void addpc2(L1PcInstance pc) {
		if (pc == null || _pushList2.contains(pc)) {return;}
		_pushList2.add(pc);}
	public void removepc2(L1PcInstance pc) {
		if (pc == null || !_pushList2.contains(pc)) {return;}
		_pushList2.remove(pc);}

	public void addpc3(L1PcInstance pc) {
		if (pc == null || _pushList3.contains(pc)) {return;}
		_pushList3.add(pc);}
	public void removepc3(L1PcInstance pc) {
		if (pc == null || !_pushList3.contains(pc)) {return;}
		_pushList3.remove(pc);}

	public void PushPc(L1PcInstance pc){
		if (_pushList.size() >= 1 && getTrapFour() == false){
			StarZone(1);
			OpenDoor(9002); // 4Ʈ�� ù��°
			MSG(2);
			setTrapFour(true);
		}
		if (_pushList1.size() >= 1 && getTrapFour1() == false){
			int []Door = {9022,9023};
			int j = Door.length;
			for(int i = 0;i < j;i++ ){
				OpenDoor(Door[i]);
			}
			MSG(3);
			setTrapFour1(true);
		}
		if (_pushList2.size() >= 1 && getTrapFour2() == false){
			StarZone(2);
			setTellTrap(true);
			MSG(4);
			setTrapFour2(true);
		}
	}

	private void PushTrap(){
		for(L1PcInstance pc : getPlayMemberArray()) {
			PushPc(pc);
			if((pc.getX() == 32666 && pc.getY() == 32819) ||
					(pc.getX() == 32666 && pc.getY() == 32817) ||
					(pc.getX() == 32668 && pc.getY() == 32817) ||
					(pc.getX() == 32668 && pc.getY() == 32819)){
				addpc(pc);
			}else{
				removepc(pc);
			}
			if((pc.getX() == 32703 && pc.getY() == 32800) ||
					(pc.getX() == 32703 && pc.getY() == 32791) ||
					(pc.getX() == 32712 && pc.getY() == 32793) ||
					(pc.getX() == 32710 && pc.getY() == 32803)){
				addpc1(pc);
			}else{
				removepc1(pc);
			}
			if((pc.getX() == 32809 && pc.getY() == 32839) ||
					(pc.getX() == 32807 && pc.getY() == 32839) ||
					(pc.getX() == 32807 && pc.getY() == 32837) ||
					(pc.getX() == 32809 && pc.getY() == 32837)){
				addpc2(pc);
			}else{
				removepc2(pc);
			}
		}       		
	}
	public void LastPushPc(L1PcInstance pc){
		if (_pushList3.size() >= 1 && getLastTrapFour() == false){
			StarZone(3);// ���� ����
			setLastTrapFour(true);
		}
	}
	private void LastPushTrap(){
		for(L1PcInstance pc : getPlayMemberArray()) {
			LastPushPc(pc);
			if((pc.getX() == 32798 && pc.getY() == 32872) ||
					(pc.getX() == 32800 && pc.getY() == 32873) ||
					(pc.getX() == 32806 && pc.getY() == 32872) ||
					(pc.getX() == 32807 && pc.getY() == 32870) ||
					(pc.getX() == 32799 && pc.getY() == 32866) ||
					(pc.getX() == 32800 && pc.getY() == 32864) ||
					(pc.getX() == 32808 && pc.getY() == 32864) ||
					(pc.getX() == 32806 && pc.getY() == 32863)){
				addpc3(pc);
			}else{
				removepc3(pc);
			}
		}       		
	}        	
	/** �δ� ���� **/
	private void End() {
		setInDunOpen(false);// �ݴ´�.
		setInDunStart(false); // �ݴ´�
		EndTelePort(); // ������ �ڸ���Ʈ
		InDunclear(); // ���� ó���Ѵ�.
		DoorClose(); // ��ü ���ݱ�
		setTellTrap(false);// �ڸ���Ʈ Ʈ�� ����
		setTrapFour(false);// 4�� Ʈ���� ����
		setTrapFour1(false);// 4�� Ʈ���� ����
		setTrapFour2(false);// 4�� Ʈ���� ����
		setLastTrapFour(false); // ������ Ʈ�� ����
		setFirstDoor(false);// ù��° �� ���¿���
		setLastDoor(false);//�������� ���¿���
		setCenterDoor(false);//������ �ذ��..
		clearPlayMember();// �÷��̾� ����Ʈ ���� 
		Close = false; //�ݴ´�
	} 
} 