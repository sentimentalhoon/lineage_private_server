package l1j.server.server.serverpackets;

import java.util.List;

import l1j.server.server.Opcodes;
import l1j.server.server.datatables.NpcShopTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.shop.L1Shop;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1ShopItem;

public class S_PrivateShopforNpc extends ServerBasePacket{

	public S_PrivateShopforNpc(L1PcInstance pc, int objId, int type){
		L1NpcInstance npc = (L1NpcInstance) L1World.getInstance().findObject(objId);

		if (npc == null) {
			return;
		}

		writeC(Opcodes.S_OPCODE_PRIVATESHOPLIST);
		writeC(type);
		writeD(objId);

		int npcId = npc.getNpcTemplate().get_npcId();
		L1Shop shop = NpcShopTable.getInstance().get(npcId);
		List<L1ShopItem> shopItems = shop.getSellingItems();

		int size = shopItems.size();
		pc.setPartnersPrivateShopItemCount(size);
		writeH(size);
		L1ItemInstance dummy = new L1ItemInstance();
		for (int i = 0; i < size; i++) {
			L1ShopItem shopItem = shopItems.get(i);
			L1Item item = shopItem.getItem();
			dummy.setItem(item);
			if (dummy!= null) {
				writeC(i);
				writeC(dummy.getItem().getBless());
				writeH(dummy.getItem().getGfxId());
				writeD(shopItem.getCount());
				writeD(shopItem.getPrice());
				if(shopItem.getEnchant() > 0){
					if(shopItem.getCount() > 1) writeS("+"+shopItem.getEnchant()+" "+ dummy.getName() + " (" + shopItem.getCount() + ")");
					else writeS("+"+shopItem.getEnchant()+" "+ dummy.getName());
				}else{
					if(shopItem.getCount() > 1) writeS(dummy.getName() + " (" + shopItem.getCount() + ")");
					else writeS(dummy.getName());
				}
				writeC(0);
			}
		}
	}
	@Override
	public byte[] getContent() {
		return getBytes();
	}
}
