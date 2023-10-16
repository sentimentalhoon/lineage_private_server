package l1j.server.server.model;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.types.Point;

public class ElementalStoneGenerator implements Runnable {

	private static Logger _log = Logger.getLogger(ElementalStoneGenerator.class.getName());

	private static final int ELVEN_FOREST_MAPID = 4;
	private static final int MAX_COUNT = Config.ELEMENTAL_STONE_AMOUNT; // ��ġ ����
	private static final int INTERVAL = 3; // ��ġ ������
	private static final int SLEEP_TIME = 300; // ��ġ ������, �缳ġ������ sleeve �ð���
	private static final int FIRST_X = 32911;
	private static final int FIRST_Y = 32210;
	private static final int LAST_X = 33141;
	private static final int LAST_Y = 32500;

	private ArrayList<L1GroundInventory> _itemList = new ArrayList<L1GroundInventory>(MAX_COUNT);
	
	private Random _random = new Random(System.nanoTime());

	private static ElementalStoneGenerator _instance = null;

	private ElementalStoneGenerator() {}

	public static ElementalStoneGenerator getInstance() {
		if (_instance == null) {
			_instance = new ElementalStoneGenerator();
		}
		return _instance;
	}

	private final L1Object _dummy = new L1Object();

	/**
	 * ������ ��ġ�� ���� �� �� ����� �����ش�.
	 */
	private boolean canPut(L1Location loc) {
		_dummy.setMap(loc.getMap());
		_dummy.setX(loc.getX());
		_dummy.setY(loc.getY());

		// ���� ������ �÷��̾� üũ
		if (L1World.getInstance().getVisiblePlayer(_dummy).size() > 0) {
			return false;
		}
		return true;
	}


	/**
	 * ������ ��ġ ����Ʈ�� �����Ѵ�.
	 */
	private Point nextPoint() {
		int newX = _random.nextInt(LAST_X - FIRST_X) + FIRST_X;
		int newY = _random.nextInt(LAST_Y - FIRST_Y) + FIRST_Y;

		return new Point(newX, newY);
	}


	/**
	 * �ֿ��� ���� ����Ʈ�κ��� �����Ѵ�.
	 */
	private void removeItemsPickedUp() {
		L1GroundInventory gInventory  = null;
		for (int i = 0; i < _itemList.size(); i++) {
			gInventory = _itemList.get(i);
			if (!gInventory.checkItem(L1ItemId.ELEMENTAL_STONE)) {
				_itemList.remove(i);
				i--;
			}
		}
	}

	/**
	 * ������ ��ġ�� ���� �д�.
	 */
	private void putElementalStone(L1Location loc) {
		L1GroundInventory gInventory = L1World.getInstance().getInventory(loc);
		L1ItemInstance item = ItemTable.getInstance().createItem(L1ItemId.ELEMENTAL_STONE);		
		item.setEnchantLevel(0);
		item.setCount(1);
		gInventory.storeItem(item);
		_itemList.add(gInventory);
	}

	@Override
	public void run() {
		try {
			L1Map map = L1WorldMap.getInstance().getMap((short) ELVEN_FOREST_MAPID);
			L1Location loc = null;
			while (true) {
				removeItemsPickedUp();

				while (_itemList.size() < MAX_COUNT) { // �پ��� �ִ� ��� ��Ʈ
					loc = new L1Location(nextPoint(), map);

					if (!canPut(loc)) {
						// XXX ��ġ ������ ��ο� PC�� �־��� ��� ���鷹�� ������ ��������
						continue;
					}

					putElementalStone(loc);

					Thread.sleep(INTERVAL * 1000); // �����ð����� ��ġ
				}
				Thread.sleep(SLEEP_TIME * 1000); // max���� ��ġ ������ �����ð��� �缳ġ���� �ʴ´�
			}
		} catch (Throwable e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
