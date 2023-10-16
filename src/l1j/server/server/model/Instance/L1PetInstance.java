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

package l1j.server.server.model.Instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import l1j.server.server.ActionCodes;
import l1j.server.server.ObjectIdFactory;
import l1j.server.server.TimeController.WarTimeController;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.PetItemTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.model.Broadcaster;
import l1j.server.server.model.CharPosUtil;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1GroundInventory;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.item.function.HealingPotion;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_HPMeter;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_PetMenuPacket;
import l1j.server.server.serverpackets.S_PetPack;
import l1j.server.server.serverpackets.S_ReturnedStat;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Pet;
import l1j.server.server.templates.L1PetItem;
import l1j.server.server.templates.L1PetType;

public class L1PetInstance extends L1NpcInstance {

	private static final long serialVersionUID = 1L;
	private static Random _random = new Random(System.nanoTime());
	public synchronized void deleteMe()
	{ 

		Object aobj[] = _master.getPetList().values().toArray();
		for(int i = 0; i < aobj.length; i++)
			if(aobj[i] == this)
				_master.sendPackets(new S_ReturnedStat(12, i * 3, getId(), false));

		super.deleteMe();
	}


	@Override
	public boolean noTarget() {
		int castleid = L1CastleLocation.getCastleIdByArea(_petMaster);
		if (_currentPetStatus == 3) {
			return true;
		} else if (castleid != 0 && WarTimeController.getInstance().isNowWar(castleid)){ //������ �� ����
			_petMaster.getPetList().remove(getId());
			dropItem();
			deleteMe();
			return true;
		} else if( _master.getMapId() == 37 || _master.getMapId() == 65 || _master.getMapId() == 67  || _master.getMapId() == 1005  || _master.getMapId() == 1011 ) { //**�� ��濡�� ����**//        
			_petMaster.getPetList().remove(getId());
			dropItem();
			deleteMe();
			return true; 
		} else if (_currentPetStatus == 4) { 
			if (_petMaster != null
					&& _petMaster.getMapId() == getMapId()
					&& getLocation().getTileLineDistance(
							_petMaster.getLocation()) < 5) {
				int dir = targetReverseDirection(_petMaster.getX(), _petMaster.getY());
				dir = checkObject(getX(), getY(), getMapId(), dir);
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			} else {
				_currentPetStatus = 3;
				return true;
			}
		} else if (_currentPetStatus == 5) {
			if (Math.abs(getHomeX() - getX()) > 1
					|| Math.abs(getHomeY() - getY()) > 1) {
				int dir = moveDirection(getHomeX(), getHomeY());
				if (dir == -1) {
					setHomeX(getX());
					setHomeY(getY());
				} else {
					setDirectionMove(dir);
					setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
				}
			}
		} else if (_currentPetStatus == 7) { 
			if (_petMaster != null
					&& _petMaster.getMapId() == getMapId()
					&& getLocation().getTileLineDistance(
							_petMaster.getLocation()) <= 1) {
				_currentPetStatus = 3;
				return true;
			}
			int locx = _petMaster.getX() + _random.nextInt(1);
			int locy = _petMaster.getY() + _random.nextInt(1);
			int dir = moveDirection(locx, locy);
			if (dir == -1) { 
				_currentPetStatus = 3;
				return true;
			}
			setDirectionMove(dir);
			setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
		} else if (_petMaster != null && _petMaster.getMapId() == getMapId()) { 
			if (getLocation().getTileLineDistance(_petMaster.getLocation()) > 2) {
				int dir = moveDirection(_petMaster.getX(), _petMaster.getY());
				if (dir == -1) { 
					_currentPetStatus = 3;
					return true;
				}
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
				if (_currentPetStatus == 8) {
					collect();
				}
			}
		} else {
			_currentPetStatus = 3;
			return true;
		}
		return false;
	}

	public L1PetInstance(L1Npc template, L1PcInstance master, L1Pet l1pet) {
		super(template);

		_petMaster = master;
		_itemObjId = l1pet.get_itemobjid();
		_type = PetTypeTable.getInstance().get(template.get_npcId());

		setId(l1pet.get_objid());
		setName(l1pet.get_name());
		setLevel(l1pet.get_level());
		setMaxHp(l1pet.get_hp());
		setCurrentHp(l1pet.get_hp());
		setMaxMp(l1pet.get_mp());
		setCurrentMp(l1pet.get_mp());
		setExp(l1pet.get_exp());
		setExpPercent(ExpTable.getExpPercentage(l1pet.get_level(), l1pet
				.get_exp()));
		setLawful(l1pet.get_lawful());
		setTempLawful(l1pet.get_lawful());
		setFood(l1pet.get_food());
		setFoodTime(l1pet.get_foodtime());

		setMaster(master);
		setX(master.getX() + _random.nextInt(5) - 2);
		setY(master.getY() + _random.nextInt(5) - 2);
		setMap(master.getMapId());
		getMoveState().setHeading(5);
		setLightSize(template.getLightSize());

		_currentPetStatus = 3;

		L1World.getInstance().storeObject(this);
		L1World.getInstance().addVisibleObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}
		master.addPet(this);
		Object aobj[] = master.getPetList().values().toArray();
		master.sendPackets(new S_ReturnedStat(12, (aobj.length + 1) * 3, getId(), true));
	}

	public L1PetInstance(L1NpcInstance target, L1PcInstance master, int itemid) {
		super(null);

		_petMaster = master;
		_itemObjId = itemid;
		_type = PetTypeTable.getInstance().get(
				target.getNpcTemplate().get_npcId());

		setId(ObjectIdFactory.getInstance().nextId());
		setting_template(target.getNpcTemplate());
		setCurrentHp(target.getCurrentHp());
		setCurrentMp(target.getCurrentMp());
		setExp(750);
		setExpPercent(0);
		setLawful(0);
		setTempLawful(0);
		setFood(0);

		setMaster(master);
		setX(target.getX());
		setY(target.getY());
		setMap(target.getMapId());
		getMoveState().setHeading(target.getMoveState().getHeading());
		setLightSize(target.getLightSize());
		setPetcost(6);
		setInventory(target.getInventory());
		target.setInventory(null);

		_currentPetStatus = 3;

		target.deleteMe();
		L1World.getInstance().storeObject(this);
		L1World.getInstance().addVisibleObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}

		master.addPet(this);
		Object aobj[] = master.getPetList().values().toArray();
		master.sendPackets(new S_ReturnedStat(12, (aobj.length + 1) * 3, getId(), true));
		getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_PET_FOOD, 1200*1000);
		PetTable.getInstance().storeNewPet(target, getId(), itemid);
	}

	@Override
	public void receiveDamage(L1Character attacker, int damage) {
		if (getCurrentHp() > 0) {
			if (damage > 0) { 
				setHate(attacker, 0);
				if(getSkillEffectTimerSet().hasSkillEffect(L1SkillId.FOG_OF_SLEEPING)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.FOG_OF_SLEEPING);
				}else if (getSkillEffectTimerSet().hasSkillEffect(L1SkillId.PHANTASM)){
					getSkillEffectTimerSet().removeSkillEffect(L1SkillId.PHANTASM);
				}
			}

			if (attacker instanceof L1PcInstance && damage > 0) {
				L1PcInstance player = (L1PcInstance) attacker;
				player.setPetTarget(this);
			}

			int newHp = getCurrentHp() - damage;
			if (newHp <= 0) {
				death(attacker);
			} else {
				setCurrentHp(newHp);
			}
		} else if (!isDead()) {
			death(attacker);
		}
	}

	public synchronized void death(L1Character lastAttacker) {
		if (!isDead()) {
			setDead(true);
			setActionStatus(ActionCodes.ACTION_Die);
			setCurrentHp(0);

			getMap().setPassable(getLocation(), true);
			Broadcaster.broadcastPacket(this, new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
		}
	}

	public void evolvePet(int new_itemobjid) {

		L1Pet l1pet = PetTable.getInstance().getTemplate(_itemObjId);
		if (l1pet == null) {
			return;
		}

		String oldName = l1pet.get_name();
		String tempName = _type.getName();
		int newNpcId = _type.getNpcIdForEvolving();
		int tmpMaxHp = getMaxHp();
		int tmpMaxMp = getMaxMp();

		transform(newNpcId);
		_type = PetTypeTable.getInstance().get(newNpcId);

		setLevel(1);
		setMaxHp(tmpMaxHp / 2);
		setMaxMp(tmpMaxMp / 2);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setExp(0);
		setExpPercent(0);

		getInventory().clearItems();

		PetTable.getInstance().deletePet(_itemObjId);

		l1pet.set_itemobjid(new_itemobjid);
		l1pet.set_npcid(newNpcId);
		if (oldName == tempName){
			l1pet.set_name(getName());	
		} else {
			l1pet.set_name(oldName);	
		}
		l1pet.set_level(getLevel());
		l1pet.set_hp(getMaxHp());
		l1pet.set_mp(getMaxMp());
		l1pet.set_exp(getExp());
		getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_PET_FOOD, 1200*1000);
		PetTable.getInstance().storeNewPet(this, getId(), new_itemobjid);

		_itemObjId = new_itemobjid;
	}

	public void liberate() {
		L1MonsterInstance monster = new L1MonsterInstance(getNpcTemplate());
		monster.setId(ObjectIdFactory.getInstance().nextId());

		monster.setX(getX());
		monster.setY(getY());
		monster.setMap(getMapId());
		monster.getMoveState().setHeading(getMoveState().getHeading());
		monster.set_storeDroped(0);
		monster.setInventory(getInventory());
		setInventory(null);
		monster.setLevel(getLevel());
		monster.setMaxHp(getMaxHp());
		monster.setCurrentHp(getCurrentHp());
		monster.setMaxMp(getMaxMp());
		monster.setCurrentMp(getCurrentMp());

		_petMaster.getPetList().remove(getId());
		deleteMe();

		_petMaster.getInventory().removeItem(_itemObjId, 1);
		PetTable.getInstance().deletePet(_itemObjId);

		L1World.getInstance().storeObject(monster);
		L1World.getInstance().addVisibleObject(monster);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(monster)) {
			onPerceive(pc);
		}
	}

	public void collect() {
		L1Inventory targetInventory = _petMaster.getInventory();
		List<L1ItemInstance> items = _inventory.getItems();
		int size = _inventory.getSize();
		L1ItemInstance item = null;
		for (int i = 0; i < size; i++) {
			item = items.get(0);
			if (item.isEquipped()) {
				continue;
			}
			if (_petMaster.getInventory().checkAddItem(item, item.getCount()) == L1Inventory.OK) {
				_inventory.tradeItem(item, item.getCount(), targetInventory);
				_petMaster.sendPackets(new S_ServerMessage(143, getName(), item.getLogName())); 
			} else { 
				targetInventory = L1World.getInstance().getInventory(getX(), getY(), getMapId());
				_inventory.tradeItem(item, item.getCount(), targetInventory);
			}
		}
	}

	public void dropItem() {
		L1Inventory targetInventory = L1World.getInstance().getInventory(getX(), getY(), getMapId());
		List<L1ItemInstance> items = _inventory.getItems();
		int size = _inventory.getSize();
		L1ItemInstance item = null;
		for (int i = 0; i < size; i++) {
			item = items.get(0);
			item.setEquipped(false);
			_inventory.tradeItem(item, item.getCount(), targetInventory);
		}
	}

	public void call() {
		int id = _type.getMessageId(L1PetType.getMessageNumber(getLevel()));
		int id2 = _type.getDefyMessageId();
		if (getFood() < 5) {
			if (id != 0) {
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, "$" + id, 0));
			}
			setCurrentPetStatus(7);
		} else {
			if (id != 0) {
				Broadcaster.broadcastPacket(this, new S_NpcChatPacket(this, "$" + id2, 0));
			}
			setCurrentPetStatus(3);
		}
	}

	public void setTarget(L1Character target) {
		if (target != null
				&& (_currentPetStatus == 1 || _currentPetStatus == 2 || _currentPetStatus == 5)) {
			setHate(target, 0);
			if (!isAiRunning()) {
				startAI();
			}
		}
	}

	public void setMasterTarget(L1Character target) {
		if (target != null
				&& (_currentPetStatus == 1 || _currentPetStatus == 5)) {
			setHate(target, 0);
			if (!isAiRunning()) {
				startAI();
			}
		}
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		perceivedFrom.getNearObjects().addKnownObject(this);
		perceivedFrom.sendPackets(new S_PetPack(this, perceivedFrom)); 
		if (isDead()) {
			perceivedFrom.sendPackets(new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
		}
	}

	@Override
	public void onAction(L1PcInstance player) {
		L1Character cha = this.getMaster();
		L1PcInstance master = (L1PcInstance) cha;
		if (master.isTeleport()) { 
			return;
		}
		if (CharPosUtil.getZoneType(this) == 1) {
			L1Attack attack_mortion = new L1Attack(player, this); 
			attack_mortion.action();
			return;
		}

		if (player.checkNonPvP(player, this)) {
			return;
		}

		L1Attack attack = new L1Attack(player, this);
		if (attack.calcHit()) {
			attack.calcDamage();
		}
		attack.action();
		attack.commit();
	}

	@Override
	public void onTalkAction(L1PcInstance player) {
		if (isDead()) {
			return;
		}
		if (_petMaster.equals(player)) {
			player.sendPackets(new S_PetMenuPacket(this, getExpPercent()));
			L1Pet l1pet = PetTable.getInstance().getTemplate(_itemObjId);
			if (l1pet != null) {
				l1pet.set_exp(getExp());
				l1pet.set_level(getLevel());
				l1pet.set_hp(getMaxHp());
				l1pet.set_mp(getMaxMp());
				PetTable.getInstance().storePet(l1pet);
			}
		}
	}

	@Override
	public void onFinalAction(L1PcInstance player, String action) {
		int status = actionType(action);
		if (status == 0) {
			return;
		}
		if (status == 6) {
			liberate();
		} else {
			Object[] petList = _petMaster.getPetList().values().toArray();
			L1PetInstance pet = null;
			L1PetType type = null;
			for (Object petObject : petList) {
				if (petObject instanceof L1PetInstance) {
					pet = (L1PetInstance) petObject;
					if (_petMaster != null && _petMaster.getLevel() >= pet.getLevel() && pet.getFood() < 5) {
						pet.setCurrentPetStatus(status);
					} else {
						type = PetTypeTable.getInstance().get(pet.getNpcTemplate().get_npcId());
						int id = type.getDefyMessageId();
						if (id != 0) {
							Broadcaster.broadcastPacket(this, new S_NpcChatPacket(pet,"$" + id, 0));
						}
					}
				}
			}
			player.sendPackets(new S_PetMenuPacket(this, getExpPercent()));
		}
	}

	@Override
	public void onItemUse() {
		Arrays.sort(healPotions);
		Arrays.sort(haestPotions);
		if (!isActived()) {
			useItem(USEITEM_HASTE, 100);
		}
		if (getCurrentHp() * 100 / getMaxHp() < 40) {
			if(getInventory().checkItem(healPotions)) {
				L1ItemInstance item = getInventory().getItemOne(healPotions);
				if(item != null && (item instanceof HealingPotion)){
					HealingPotion potion = (HealingPotion) item;
					potion.consumeHealingPotion(this, potion.getItemId());
				}
			}
			//useItem(USEITEM_HEAL, 100);
		}
	}

	@Override
	public void onGetItem(L1ItemInstance item) {
		if (getNpcTemplate().get_digestitem() > 0) {
			setDigestItem(item);
		}
		Arrays.sort(healPotions);
		Arrays.sort(haestPotions);
		if (Arrays.binarySearch(healPotions, item.getItem().getItemId()) >= 0) {
			if (getCurrentHp() != getMaxHp()) {
				if(item instanceof HealingPotion){
					HealingPotion potion = (HealingPotion) item;
					potion.consumeHealingPotion(this, potion.getItemId());
				}
			}
		} else if (Arrays
				.binarySearch(haestPotions, item.getItem().getItemId()) >= 0) {
			useItem(USEITEM_HASTE, 100);
		}
	}

	private int actionType(String action) {
		int status = 0;
		if (action.equalsIgnoreCase("aggressive")) { 
			status = 1;
		} else if (action.equalsIgnoreCase("defensive")) { 
			status = 2;
		} else if (action.equalsIgnoreCase("stay")) {
			status = 3;
		} else if (action.equalsIgnoreCase("extend")) { 
			status = 4;
		} else if (action.equalsIgnoreCase("alert")) { 
			status = 5;
		} else if (action.equalsIgnoreCase("dismiss")) { 
			status = 6;
		} else if (action.equalsIgnoreCase("getitem")) {
			status = 8;
			collection();
		}
		return status;
	}

	private void collection() {
		ArrayList<L1GroundInventory> gInventorys = new ArrayList<L1GroundInventory>();

		for (L1Object obj : L1World.getInstance().getVisibleObjects(this, 10)) {
			if (obj != null && obj instanceof L1GroundInventory) {
				gInventorys.add((L1GroundInventory) obj);
			}
		}

		int groundinv = gInventorys.size();
		for(int i=0; i < groundinv; i++) {
			L1GroundInventory inventory = gInventorys.get(i);
			for (L1ItemInstance item : inventory.getItems()) { 
				if (getInventory().checkAddItem(item, item.getCount()) == L1Inventory.OK
						&& !item.getItem().isUseHighPet()) {
					_targetItem = item;
					_targetItemList.add(_targetItem);
				}
			}
		}
	}

	@Override
	public void setCurrentHp(int i) {
		super.setCurrentHp(i);

		if (getMaxHp() > getCurrentHp()) {
			startHpRegeneration();
		}

		if (_petMaster != null) {
			int HpRatio = 100 * getCurrentHp() / getMaxHp();
			L1PcInstance Master = _petMaster;
			Master.sendPackets(new S_HPMeter(getId(), HpRatio));
		}
	}

	@Override
	public void setCurrentMp(int i) {
		super.setCurrentMp(i);

		if (getMaxMp() > getCurrentMp()) {
			startMpRegeneration();
		}
	}

	public void setCurrentPetStatus(int i) {
		_currentPetStatus = i;
		if (_currentPetStatus == 5) {
			setHomeX(getX());
			setHomeY(getY());
		}
		if (_currentPetStatus == 7) {
			allTargetClear();
		}

		if (_currentPetStatus == 3) {
			allTargetClear();
		} else {
			if (!isAiRunning()) {
				startAI();
			}
		}
	}

	public void usePetWeapon(L1ItemInstance weapon) {
		if (getWeapon() == null) {
			setPetWeapon(weapon);
		} else { // �̹� �����ΰ��� ��� �ϰ� �ִ� ���, ���� ��� ����
			if (getWeapon().equals(weapon)) {
				removePetWeapon(getWeapon());
			} else {
				removePetWeapon(getWeapon());
				setPetWeapon(weapon);
			}
		}
	}

	public void usePetArmor(L1ItemInstance armor) {
		if (getArmor() == null) {
			setPetArmor(armor);
		} else { // �̹� �����ΰ��� ��� �ϰ� �ִ� ���, ���� ��� ����
			if (getArmor().equals(armor)) {
				removePetArmor(getArmor());
			} else {
				removePetArmor(getArmor());
				setPetArmor(armor);
			}
		}
	}

	private void setPetWeapon(L1ItemInstance weapon) {
		int itemId = weapon.getItem().getItemId();
		L1PetItem petItem = PetItemTable.getInstance().getTemplate(itemId);

		if (petItem == null) { return; }

		setHitByWeapon(petItem.getHitModifier());
		setDamageByWeapon(petItem.getDamageModifier());
		getAbility().addAddedStr(petItem.getAddStr());
		getAbility().addAddedCon(petItem.getAddCon());
		getAbility().addAddedDex(petItem.getAddDex());
		getAbility().addAddedInt(petItem.getAddInt());
		getAbility().addAddedWis(petItem.getAddWis());
		addMaxHp(petItem.getAddHp());
		addMaxMp(petItem.getAddMp());
		getAbility().addSp(petItem.getAddSp());
		getResistance().addMr(petItem.getAddMr());

		setWeapon(weapon);
		weapon.setEquipped(true);
	}

	public void removePetWeapon(L1ItemInstance weapon) {
		int itemId = weapon.getItem().getItemId();
		L1PetItem petItem = PetItemTable.getInstance().getTemplate(itemId);

		if (petItem == null) { return; }

		setHitByWeapon(0);
		setDamageByWeapon(0);
		getAbility().addAddedStr(-petItem.getAddStr());
		getAbility().addAddedCon(-petItem.getAddCon());
		getAbility().addAddedDex(-petItem.getAddDex());
		getAbility().addAddedInt(-petItem.getAddInt());
		getAbility().addAddedWis(-petItem.getAddWis());
		addMaxHp(-petItem.getAddHp());
		addMaxMp(-petItem.getAddMp());
		getAbility().addSp(-petItem.getAddSp());
		getResistance().addMr(-petItem.getAddMr());

		setWeapon(null);
		weapon.setEquipped(false);
	}

	public void setPetArmor(L1ItemInstance armor) {
		int itemId = armor.getItem().getItemId();
		L1PetItem petItem = PetItemTable.getInstance().getTemplate(itemId);

		if (petItem == null) { return; }

		getAC().addAc(petItem.getAddAc());
		getAbility().addAddedStr(petItem.getAddStr());
		getAbility().addAddedCon(petItem.getAddCon());
		getAbility().addAddedDex(petItem.getAddDex());
		getAbility().addAddedInt(petItem.getAddInt());
		getAbility().addAddedWis(petItem.getAddWis());
		addMaxHp(petItem.getAddHp());
		addMaxMp(petItem.getAddMp());
		getAbility().addSp(petItem.getAddSp());
		getResistance().addMr(petItem.getAddMr());

		setArmor(armor);
		armor.setEquipped(true);
	}

	public void removePetArmor(L1ItemInstance armor) {
		int itemId = armor.getItem().getItemId();
		L1PetItem petItem = PetItemTable.getInstance().getTemplate(itemId);

		if (petItem == null) { return; }

		getAC().addAc(-petItem.getAddAc());
		getAbility().addAddedStr(-petItem.getAddStr());
		getAbility().addAddedCon(-petItem.getAddCon());
		getAbility().addAddedDex(-petItem.getAddDex());
		getAbility().addAddedInt(-petItem.getAddInt());
		getAbility().addAddedWis(-petItem.getAddWis());
		addMaxHp(-petItem.getAddHp());
		addMaxMp(-petItem.getAddMp());
		getAbility().addSp(-petItem.getAddSp());
		getResistance().addMr(-petItem.getAddMr());

		setArmor(null);
		armor.setEquipped(false);
	}

	private int _damageByWeapon;
	private int _currentPetStatus;
	private L1PcInstance _petMaster;
	private int _itemObjId;
	private L1PetType _type;
	private int _expPercent;
	private L1ItemInstance _weapon;
	private L1ItemInstance _armor;
	private int _hitByWeapon;
	private int _Food = 0;
	private int _FoodTime = 0;

	public int getCurrentPetStatus() {
		return _currentPetStatus;
	}

	public int getItemObjId() {
		return _itemObjId;
	}

	public void setExpPercent(int expPercent) {
		_expPercent = expPercent;
	}

	public int getExpPercent() {
		return _expPercent;
	}

	public void setWeapon(L1ItemInstance weapon) {
		_weapon = weapon;
	}

	public L1ItemInstance getWeapon() {
		return _weapon;
	}

	public void setArmor(L1ItemInstance armor) {
		_armor = armor;
	}

	public L1ItemInstance getArmor() {
		return _armor;
	}

	public void setHitByWeapon(int i) {
		_hitByWeapon = i;
	}

	public int getHitByWeapon() {
		return _hitByWeapon;
	}

	public void setDamageByWeapon(int i) {
		_damageByWeapon = i;
	}

	public int getDamageByWeapon() {
		return _damageByWeapon;
	}

	public int getFood() {
		return _Food;
	}

	public void setFood(int i) {
		_Food = i;
	}

	public int getFoodTime() {
		return _FoodTime;
	}

	public void setFoodTime(int i) {
		_FoodTime = i;
	}

	public L1PetType getPetType() {
		return _type;
	}
}

