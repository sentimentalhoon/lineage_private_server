/**
 * Ÿ�̸� ���� �ʿ� ���� ��Ʈ�ѷ�
 * 2008. 12. 04
*/

package l1j.server.server;

import java.util.ArrayList;

import l1j.server.server.datatables.DoorSpawnTable;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1TimeMap;

public class TimeMapController extends Thread {

	private ArrayList<L1TimeMap> mapList;
	private static TimeMapController instance;	

	public static TimeMapController getInstance(){
		if(instance == null) instance = new TimeMapController();
		return instance;
	}
	
	private TimeMapController(){
		super("TimeMapController");
		mapList = new ArrayList<L1TimeMap>();
	}
	
	@Override
	public void run(){
		try{
			while(true){
				for(L1TimeMap timeMap : array()){
					if(timeMap.count()){
						for(L1PcInstance pc : L1World.getInstance().getAllPlayers()){
							switch(pc.getMapId()){
							case 73:
							case 74:
								L1Teleport.teleport(pc, 34056, 32279, (short) 4, 5, true);
								break;
							case 460:
							case 461:
							case 462:
							case 463:
							case 464:
							case 465:
							case 466:
								L1Teleport.teleport(pc, 32664, 32855, (short) 457, 5, true);
								break;
							case 470:
							case 471:
							case 472:
							case 473:
							case 474:
								L1Teleport.teleport(pc, 32663, 32853, (short) 467, 5, true);
								break;
							case 475:
							case 476:
							case 477:
							case 478:
								L1Teleport.teleport(pc, 32660, 32876, (short) 468, 5, true);
								break;
							default:
								break;
							}
						}
						DoorSpawnTable.getInstance().getDoor(timeMap.getDoor()).close();
						remove(timeMap);
					}
				}
				Thread.sleep(1000L);
			}
		}catch(Exception e){
			TimeMapController sTemp = new TimeMapController();
			copy(arrayList(), sTemp.arrayList());
			clear();
			sTemp.start();
			e.printStackTrace();
		}
	}
	
	/**
	 * Ÿ�� �̺�Ʈ�� �ִ� �� ���
	 * �ߺ� ����� ���� �ʵ��� �̹� ��ϵ� �� ���̵�� �� ���ٸ� ���
	 * ����� 0 �̶�� �� �ʱ��� �񱳴���� ���⶧���� ������ ���
	 * @param	(TimeMap)	����� �� ��ü
	*/
	public void add(L1TimeMap map){
		if(mapList.size() > 0){
			for(L1TimeMap m : array()){
				if(m.getId() != map.getId()){
					mapList.add(map);
					break;
				}
			}
		}else mapList.add(map);
	}
	/**
	 * Ÿ�� �̺�Ʈ�� �ִ� �� ����
	 * �ߺ� ���� �Ǵ� IndexOutOfBoundsException�� ���� �ʵ��� �̹� ��ϵ� �� ���̵�� �� �ִٸ� ����
	 * @param	(TimeMap)	������ �� ��ü
	*/
	private void remove(L1TimeMap map){
		for(L1TimeMap m : array()){
			if(m.getId() == map.getId()){
				mapList.remove(map);
				break;
			}
		}
		map = null;
	}
	/**
	 * ��Ʈ�ѷ� ����Ʈ �ʱ�ȭ
	 * ���Ӽ��� ����� ��û(���������� �������)
	*/
	private void clear(){
		mapList.clear();
	}
	/**
	 * ��ϵ� �̺�Ʈ �� �迭 ����
	 * @return	(TimeMap[])	�� ��ü �迭
	*/
	private L1TimeMap[] array(){
		return mapList.toArray(new L1TimeMap[mapList.size()]);
	}
	/**
	 * ��Ʈ�ѷ� ����Ʈ ��ü(Exception ������ �����)
	 * @return	(ArrayList<TimeMap>)	�� ���� ����Ʈ
	*/
	private ArrayList<L1TimeMap> arrayList(){
		return mapList;
	}
	/**
	 * ��Ʈ�ѷ� ���� ó���� ��ϵ� �� �̺�Ʈ�� ������Ű�� ���� ����Ʈ ��ü ����
	 * ���� for ���� �̿��ϵ� ���� �߻��� ���� for ���� �̿��Ͽ� ����
	 * @param	(ArrayList<TimeMap>)	src		���� ����Ʈ
	 * @param	(ArrayList<TimeMap>)	desc	����� ����Ʈ
	*/
	private void copy(ArrayList<L1TimeMap> src, ArrayList<L1TimeMap> desc){
		try{
			for(L1TimeMap map : src.toArray(new L1TimeMap[mapList.size()])){
				if(!desc.contains(map)) desc.add(map);
			}
		}catch(Exception e){
			L1TimeMap map = null;
			for(int i = 0; i < src.size(); i++){
				map = src.get(i);
				if(!desc.contains(map)) desc.add(map);
			}
		}
	}
}