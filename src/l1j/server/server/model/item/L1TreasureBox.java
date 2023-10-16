package l1j.server.server.model.item;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.utils.PerformanceTimer;

@XmlAccessorType(XmlAccessType.FIELD)
public class L1TreasureBox {

	private static Logger _log = Logger.getLogger(L1TreasureBox.class.getName());

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "TreasureBoxList")
	private static class TreasureBoxList implements Iterable<L1TreasureBox> {
		@XmlElement(name = "TreasureBox")
		private List<L1TreasureBox> _list;

		public Iterator<L1TreasureBox> iterator() {
			return _list.iterator();
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	private static class Item {
		@XmlAttribute(name = "ItemId")
		private int _itemId;

		@XmlAttribute(name = "Count")
		private int _count;

		@XmlAttribute(name = "Enchant") // ����: ��æƮ��ġ
		private int _enchant;

		@XmlAttribute(name = "AttrEnchant") // ����: �Ӽ� ��æƮ��ġ
		private int _attrenchant;
		
		@XmlAttribute(name = "Is_Id") // ����: Ȯ�λ��� (1 = Ȯ�� , 0 = ��Ȯ��)
		private boolean _is_id;

		@XmlAttribute(name = "Bless") // ����: 
		/*
		 ���� = 0 ,���� = 1,���� = 2,��Ȯ�� = 3,128= ������� ����,129 = ��������� ����,130 = ���־����� ����,131 = ��Ȯ�ξ����� ����
		*/
		private int _bless;
		
		private int _chance;

		@SuppressWarnings("unused")
		@XmlAttribute(name = "Chance")
		private void setChance(double chance) {
			_chance = (int) (chance * 10000);
		}

		public int getItemId() {
			return _itemId;
		}

		public int getCount() {
			return _count;
		}

		public int getEnchant() { // ����: ��æƮ��ġ
			return _enchant;
		}
		
		public int getAttrEnchant() { // ����: �Ӽ� ��æ
			return _attrenchant;
		}
		
		public boolean getIs_ID() { // ����: Ȯ�λ���
			return _is_id;
		}
		
		public int getBless() { // ����: Ȯ�λ���
			return _bless;
		}

		public double getChance() {
			return _chance;
		}
	}

	private static enum TYPE {
		RANDOM, SPECIFIC
	}

	private static final String PATH = "./data/xml/Item/TreasureBox.xml";

	private static final HashMap<Integer, L1TreasureBox> _dataMap = new HashMap<Integer, L1TreasureBox>();

	public static L1TreasureBox get(int id) {
		return _dataMap.get(id);
	}

	@XmlAttribute(name = "ItemId")
	private int _boxId;

	@XmlAttribute(name = "Type")
	private TYPE _type;

	private int getBoxId() {
		return _boxId;
	}

	private TYPE getType() {
		return _type;
	}

	@XmlElement(name = "Item")
	private CopyOnWriteArrayList<Item> _items;

	private List<Item> getItems() {
		return _items;
	}

	private int _totalChance;

	private int getTotalChance() {
		return _totalChance;
	}

	private void init() {
		for (Item each : getItems()) {
			_totalChance += each.getChance();
			if (ItemTable.getInstance().getTemplate(each.getItemId()) == null) {
				getItems().remove(each);
				_log.warning("������ ID " + each.getItemId() + " �� ���ø��� �߰ߵ��� �ʾҽ��ϴ�.");
			}
		}
		if (getTotalChance() != 0 && getTotalChance() != 1000000) {
			_log.warning("ID " + getBoxId() + "�� Ȯ���� �հ谡 100%�� ���� �ʽ��ϴ�.");
		}
	}

	public static void load() {
		PerformanceTimer timer = new PerformanceTimer();
		System.out.print("[L1TreasureBox] loading TreasureBox...");
		try {
			JAXBContext context = JAXBContext.newInstance(L1TreasureBox.TreasureBoxList.class);

			Unmarshaller um = context.createUnmarshaller();

			File file = new File(PATH);
			TreasureBoxList list = (TreasureBoxList) um.unmarshal(file);

			for (L1TreasureBox each : list) {
				each.init();
				_dataMap.put(each.getBoxId(), each);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, PATH + "�� �ε忡 ����.", e);
			System.exit(0);
		}
		System.out.println("OK! " + timer.get() + "ms");
	}

	public boolean open(L1PcInstance pc) {
		L1ItemInstance item = null;
		Random random = null;
		if (getType().equals(TYPE.SPECIFIC)) {
			for (Item each : getItems()) {
				item = ItemTable.getInstance().createItem(each.getItemId());
				if (item != null) {
					item.setCount(each.getCount());
					item.setEnchantLevel(each.getEnchant()); // ����: ��æƮ��ġ
					item.setAttrEnchantLevel(each.getAttrEnchant()); // ����: �Ӽ���æƮ��ġ
					item.setIdentified(each.getIs_ID()); // ����: Ȯ�λ���
					item.setBless(each.getBless()); // ����: Ȯ�λ���
					storeItem(pc, item);
				}
			}
		} else if (getType().equals(TYPE.RANDOM)) {
			random = new Random(System.nanoTime());
			int chance = 0;

			int r = random.nextInt(getTotalChance());

			for (Item each : getItems()) {
				chance += each.getChance();

				if (r < chance) {
					item = ItemTable.getInstance().createItem(each.getItemId());
					if (item != null) {
						item.setCount(each.getCount());
						storeItem(pc, item);
					}
					break;
				}
			}
		}

		if (item == null) {
			return false;
		} else {
			int itemId = getBoxId();

			if (itemId == 40576 || itemId == 40577 || itemId == 40578
					|| itemId == 40411 || itemId == 49013) {
				L1Teleport.teleport(pc, 32808, 32830, (short)610, 5, true);
				pc.death(null); 
			}
			return true;
		}
	}

	private static void storeItem(L1PcInstance pc, L1ItemInstance item) {
		L1Inventory inventory;

		if (pc.getInventory().checkAddItem(item, item.getCount()) == L1Inventory.OK) {
			inventory = pc.getInventory();
		} else {
			//A102 Ʈ�����ڽ� ���� �κ��� �� ���ÿ� �ڽ� �� Ǯ����
			//inventory = L1World.getInstance().getInventory(pc.getLocation());
			return;
			//A102 Ʈ�����ڽ� ���� �κ��� �� ���ÿ� �ڽ� �� Ǯ����
		}
		inventory.storeItem(item);
		pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
	}
}
