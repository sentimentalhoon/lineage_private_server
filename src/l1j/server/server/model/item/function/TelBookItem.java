package l1j.server.server.model.item.function;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.EARTH_BIND;
import static l1j.server.server.model.skill.L1SkillId.FREEZING_BLIZZARD;
import static l1j.server.server.model.skill.L1SkillId.ICE_LANCE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_FREEZE;

import l1j.server.server.clientpackets.ClientBasePacket;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Item;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.skill.L1SkillId;

@SuppressWarnings("serial")
public class TelBookItem extends L1ItemInstance{
	public TelBookItem(L1Item item){ super(item); }

	@Override
	public void clickItem(L1Character cha, ClientBasePacket packet){ 
		if(cha instanceof L1PcInstance){
			L1PcInstance pc = (L1PcInstance)cha;
			L1ItemInstance useItem = pc.getInventory().getItem(this.getId());
			int itemId = useItem.getItemId();
			int BookTel = packet.readC();

			if (!pc.getMap().isEscapable()) {
				pc.sendPackets(new S_ServerMessage(626)); // �� ��ġ������ �� ������ �̵��� �� �����ϴ�.
				return;
			}
			if (pc.isPinkName()) {
				pc.sendPackets(new S_ServerMessage(215)); // ���� �߿� �����̵��� �� �����ϴ�.
				return;
			}
			if (isFreeze(pc)) {
				return;
			}
			int castle_id = L1CastleLocation.getCastleIdByArea(pc);
			if (castle_id != 0) { // ���������� ��� �Ұ�
				pc.sendPackets(new S_ServerMessage(538)); // ���⿡�� Ż���ϴ� ���� �Ұ����մϴ�.
				return;
			}
			switch(itemId){
			case 560025:
			case 560026:
				final int[][] TownAddBook = {
						{ 34060, 32281, 4 },   // ����
						{ 33079, 33390, 4 },   // �����
						{ 32750, 32439, 4 },   // ��ũ��
						{ 32612, 33188, 4 },   // ���ٿ��
						{ 33720, 32492, 4 }
						,   // ����
						{ 32872, 32912, 304 }, // ħ���� ����
						{ 32612, 32781, 4 },   // �۷���
						{ 33067, 32803, 4 },   // ��Ʈ
						{ 33933, 33358, 4 },   // �Ƶ�
						{ 33601, 33232, 4 },   // ���̳�
						{ 32574, 32942, 0 },   // ���ϴ� ��
						{ 33430, 32815, 4 },}; // ���
				int[] TownAddBookList = TownAddBook[BookTel];
				if(TownAddBookList != null){
					L1Teleport.teleport(pc, TownAddBookList[0], TownAddBookList[1], (short)TownAddBookList[2], 3, true);
					pc.getInventory().removeItem(useItem, 1);
				}
				break;
			case 560027:
				final int[][] DungeonAddBook = {
						{ 32791, 32800, 101 },  // ����1
						{ 32811, 32727, 7 },    // ����1
						{ 32743, 32833, 72 },   // ��������
						{ 32742, 32777, 30 },   // ���1
						{ 32894, 32771, 78 },   // ����4
						{ 32711, 32793, 59 },   // ����1
						{ 32788, 32794, 46 },   // ���̱�
						{ 32538, 32803, 400 },  // ����� ���ױ�
						{ 32920, 32800, 430 },  // ����
						{ 32927, 32995, 410 },  // ����
						{ 34267, 32189, 4 },    // �׽�
						{ 32760, 33461, 4 },};  // ���
				int[] DungeonAddBookList = DungeonAddBook[BookTel];
				if(DungeonAddBookList != null){ 
					L1Teleport.teleport(pc, DungeonAddBookList[0], DungeonAddBookList[1], (short)DungeonAddBookList[2], 3, true); 
					pc.getInventory().removeItem(useItem, 1);
				}
				break;
			case 560028:
				final int[][] OmanTowerAddBook = {
						{ 33766, 32863, 106 },    // ����6
						{ 32744, 32862, 116 },    // ����16
						{ 32741, 32854, 126 },    // ����26
						{ 32739, 32864, 136 },    // ����36
						{ 32735, 32869, 146 },    // ����46
						{ 32807, 32805, 156 },    // ����56
						{ 32736, 32802, 166 },    // ����66
						{ 32727, 32789, 176 },    // ����76
						{ 32724, 32797, 186 },    // ����86
						{ 32733, 32787, 196 },};  // ����96
				int[] OmanTowerAddBookList = OmanTowerAddBook[BookTel];
				if(OmanTowerAddBookList != null){ 
					L1Teleport.teleport(pc, OmanTowerAddBookList[0], OmanTowerAddBookList[1], (short)OmanTowerAddBookList[2], 3, true); 
					pc.getInventory().removeItem(useItem, 1);
				}
				break;
			}
		}
	}
	public boolean isFreeze(L1PcInstance pc) {
		if (pc.getSkillEffectTimerSet().hasSkillEffect(STATUS_FREEZE)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.SHOCK_STUN)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.CURSE_PARALYZE)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(ABSOLUTE_BARRIER)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(ICE_LANCE)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(FREEZING_BLIZZARD)
				|| pc.getSkillEffectTimerSet().hasSkillEffect(EARTH_BIND)) {
			return true;
		}
		return false;
	}
}

