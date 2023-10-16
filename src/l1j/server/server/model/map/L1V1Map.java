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
package l1j.server.server.model.map;

import l1j.server.server.types.Point;
import l1j.server.server.ActionCodes;
import l1j.server.server.datatables.DoorSpawnTable;
import l1j.server.server.datatables.MapFixKeyTable;
import l1j.server.server.model.Instance.L1DoorInstance;

public class L1V1Map extends L1Map {
	private int _mapId;

	private int _worldTopLeftX;

	private int _worldTopLeftY;

	private int _worldBottomRightX;

	private int _worldBottomRightY;

	private byte _map[][];

	private boolean _isUnderwater;

	private boolean _isMarkable;

	private boolean _isTeleportable;

	private boolean _isEscapable;

	private boolean _isUseResurrection;

	private boolean _isUsePainwand;

	private boolean _isEnabledDeathPenalty;

	private boolean _isTakePets;

	private boolean _isRecallPets;

	private boolean _isUsableItem;

	private boolean _isUsableSkill;


	private static final byte BITFLAG_IS_IMPASSABLE = (byte) 128; // 1000 0000

	protected L1V1Map() {

	}

	public L1V1Map(int mapId, byte map[][], int worldTopLeftX,
			int worldTopLeftY, boolean underwater, boolean markable,
			boolean teleportable, boolean escapable, boolean useResurrection,
			boolean usePainwand, boolean enabledDeathPenalty, boolean takePets,
			boolean recallPets, boolean usableItem, boolean usableSkill) {
		_mapId = mapId;
		_map = map;
		_worldTopLeftX = worldTopLeftX;
		_worldTopLeftY = worldTopLeftY;

		_worldBottomRightX = worldTopLeftX + map.length - 1;
		_worldBottomRightY = worldTopLeftY + map[0].length - 1;

		_isUnderwater = underwater;
		_isMarkable = markable;
		_isTeleportable = teleportable;
		_isEscapable = escapable;
		_isUseResurrection = useResurrection;
		_isUsePainwand = usePainwand;
		_isEnabledDeathPenalty = enabledDeathPenalty;
		_isTakePets = takePets;
		_isRecallPets = recallPets;
		_isUsableItem = usableItem;
		_isUsableSkill = usableSkill;
	}

	public L1V1Map(L1V1Map map) {
		_mapId = map._mapId;

		_map = new byte[map._map.length][];
		for (int i = 0; i < map._map.length; i++) {
			_map[i] = map._map[i].clone();
		}

		_worldTopLeftX = map._worldTopLeftX;
		_worldTopLeftY = map._worldTopLeftY;
		_worldBottomRightX = map._worldBottomRightX;
		_worldBottomRightY = map._worldBottomRightY;

	}
	
	public L1V1Map clone(int id){
		L1V1Map map = new L1V1Map(this);
		map._mapId = id;
		return map;
	}

	private int accessTile(int x, int y) {
		if (!isInMap(x, y)) { 
			return 0;
		}

		return _map[x - _worldTopLeftX][y - _worldTopLeftY];
	}

	private int accessOriginalTile(int x, int y) {
		return accessTile(x, y) & (~BITFLAG_IS_IMPASSABLE);
	}

	private void setTile(int x, int y, int tile) {
		if (!isInMap(x, y)) { 
			return;
		}
		_map[x - _worldTopLeftX][y - _worldTopLeftY] = (byte) tile;
	}

	public byte[][] getRawTiles() {
		return _map;
	}

	@Override
	public L1V1Map copyMap(int newMapId){
		return clone(newMapId);
	}
	
	@Override
	public int getId() {
		return _mapId;
	}

	@Override
	public int getX() {
		return _worldTopLeftX;
	}

	@Override
	public int getY() {
		return _worldTopLeftY;
	}

	@Override
	public int getWidth() {
		return _worldBottomRightX - _worldTopLeftX + 1;
	}

	@Override
	public int getHeight() {
		return _worldBottomRightY - _worldTopLeftY + 1;
	}

	@Override
	public int getTile(int x, int y) {
		short tile = _map[x - _worldTopLeftX][y - _worldTopLeftY];
		if (0 != (tile & BITFLAG_IS_IMPASSABLE)) {
			return 300;
		}
		return accessOriginalTile(x, y);
	}

	@Override
	public int getOriginalTile(int x, int y) {
		return accessOriginalTile(x, y);
	}

	@Override
	public boolean isInMap(Point pt) {
		return isInMap(pt.getX(), pt.getY());
	}

	@Override
	public boolean isInMap(int x, int y) {
		if (_mapId == 4
				&& (x < 32520 || y < 32070 || (y < 32190 && x < 33950))) {
			return false;
		}
		return (_worldTopLeftX <= x && x <= _worldBottomRightX
				&& _worldTopLeftY <= y && y <= _worldBottomRightY);
	}

	@Override
	public boolean isPassable(Point pt) {
		return isPassable(pt.getX(), pt.getY());
	}

	@Override
	public boolean isPassable(int x, int y) {
		return isPassable(x, y - 1, 4) || isPassable(x + 1, y, 6)
		|| isPassable(x, y + 1, 0) || isPassable(x - 1, y, 2);
	}

	@Override
	public boolean isPassable(Point pt, int heading) {
		return isPassable(pt.getX(), pt.getY(), heading);
	}
	@Override
	public boolean ismPassable(int x, int y, int heading) {
		switch(heading){
		case 0:
			return (accessTile(x, y)&8)>0;
		case 1:
			return ((accessTile(x, y)&8)>0 && (accessTile(x, y-1)&4)>0) || ((accessTile(x, y)&4)>0 && (accessTile(x+1, y)&8)>0);
		case 2:
			return (accessTile(x, y)&4)>0;
		case 3:
			return ((accessTile(x, y+1)&8)>0 && (accessTile(x, y+1)&4)>0) || ((accessTile(x, y)&4)>0 && (accessTile(x+1, y+1)&8)>0);
		case 4:
			return (accessTile(x, y+1)&8)>0;
		case 5:
			return ((accessTile(x, y+1)&8)>0 && (accessTile(x-1, y+1)&4)>0) || ((accessTile(x-1, y)&4)>0 && (accessTile(x-1, y+1)&8)>0);
		case 6:
			return (accessTile(x-1, y)&4) > 0;
		case 7:
			return ((accessTile(x, y)&8)>0 && (accessTile(x-1, y-1)&4)>0) || ((accessTile(x-1, y)&4)>0 && (accessTile(x-1, y)&8)>0);
		}
		return false;
	}
	@Override
	public boolean isPassable(int x, int y, int heading) {		
		int tile1 = accessTile(x, y);
		int tile2;

		switch(heading){
		case 0: tile2 = accessTile(x, y - 1); break;
		case 1: tile2 = accessTile(x + 1, y - 1); break;
		case 2: tile2 = accessTile(x + 1, y); break;
		case 3: tile2 = accessTile(x + 1, y + 1); break;
		case 4: tile2 = accessTile(x, y + 1); break;
		case 5: tile2 = accessTile(x - 1, y + 1); break;
		case 6: tile2 = accessTile(x - 1, y); break;
		case 7: tile2 = accessTile(x - 1, y - 1); 	break;
		default: return false;
		}

		if ((tile2 & BITFLAG_IS_IMPASSABLE) == BITFLAG_IS_IMPASSABLE) {
			return false;
		}

		switch(heading){
		case 0:{ return (tile1 & 0x02) == 0x02; }
		case 1:{ int tile3 = accessTile(x, y - 1); int tile4 = accessTile(x + 1, y); return (tile3 & 0x01) == 0x01 || (tile4 & 0x02) == 0x02; }
		case 2:{ return (tile1 & 0x01) == 0x01; }
		case 3:{ int tile3 = accessTile(x, y + 1); return (tile3 & 0x01) == 0x01; }
		case 4:{ return (tile2 & 0x02) == 0x02; }
		case 5:{ return (tile2 & 0x01) == 0x01 || (tile2 & 0x02) == 0x02; }
		case 6:{ return (tile2 & 0x01) == 0x01; }
		case 7:{ int tile3 = accessTile(x - 1, y); return (tile3 & 0x02) == 0x02; }
		default:break;
		}
		return false; 
	}

	@Override
	public void setPassable(Point pt, boolean isPassable) {
		setPassable(pt.getX(), pt.getY(), isPassable);
	}

	@Override
	public void setPassable(int x, int y, boolean isPassable) {
		if (isPassable) {
			setTile(x, y, (short) (accessTile(x, y) & (~BITFLAG_IS_IMPASSABLE)));
		} else {
			setTile(x, y, (short) (accessTile(x, y) | BITFLAG_IS_IMPASSABLE));
		}
	}

	@Override
	public boolean isSafetyZone(Point pt) {
		return isSafetyZone(pt.getX(), pt.getY());
	}

	@Override
	public boolean isSafetyZone(int x, int y) {
		int tile = accessOriginalTile(x, y);

		return (tile & 0x30) == 0x10;
	}

	@Override
	public boolean isCombatZone(Point pt) {
		return isCombatZone(pt.getX(), pt.getY());
	}

	@Override
	public boolean isCombatZone(int x, int y) {
		int tile = accessOriginalTile(x, y);

		return (tile & 0x30) == 0x20;
	}

	@Override
	public boolean isNormalZone(Point pt) {
		return isNormalZone(pt.getX(), pt.getY());
	}

	@Override
	public boolean isNormalZone(int x, int y) {
		int tile = accessOriginalTile(x, y);
		return (tile & 0x30) == 0x00;
	}

	@Override
	public boolean isArrowPassable(Point pt) {
		return isArrowPassable(pt.getX(), pt.getY());
	}

	@Override
	public boolean isArrowPassable(int x, int y) {
		return (accessOriginalTile(x, y) & 0x0e) != 0;
	}

	@Override
	public boolean isArrowPassable(Point pt, int heading) {
		return isArrowPassable(pt.getX(), pt.getY(), heading);
	}

	@Override
	public boolean isArrowPassable(int x, int y, int heading) {
		int tile1 = accessTile(x, y);
		int tile2;
		@SuppressWarnings("unused")
		int newX;
		@SuppressWarnings("unused")
		int newY;

		switch(heading){
		case 0: tile2 = accessTile(x, y - 1); newX = x; newY = y - 1; break;
		case 1: tile2 = accessTile(x + 1, y - 1); newX = x + 1; newY = y - 1; break;
		case 2: tile2 = accessTile(x + 1, y); newX = x + 1; newY = y; break;
		case 3: tile2 = accessTile(x + 1, y + 1); newX = x + 1; newY = y + 1; break;
		case 4: tile2 = accessTile(x, y + 1); newX = x; newY = y + 1; break;
		case 5: tile2 = accessTile(x - 1, y + 1); newX = x - 1; newY = y + 1; break;
		case 6: tile2 = accessTile(x - 1, y); newX = x - 1; newY = y; break;
		case 7: tile2 = accessTile(x - 1, y - 1); newX = x - 1; newY = y - 1; break;
		default: return false;
		}
		switch(heading){
		case 0:{ return (tile1 & 0x08) == 0x08; }
		case 1:{ int tile3 = accessTile(x, y - 1); int tile4 = accessTile(x + 1, y); return (tile3 & 0x04) == 0x04 || (tile4 & 0x08) == 0x08; }
		case 2:{ return (tile1 & 0x04) == 0x04; }
		case 3:{ int tile3 = accessTile(x, y + 1); return (tile3 & 0x04) == 0x04; }
		case 4:{ return (tile2 & 0x08) == 0x08; }
		case 5:{ return (tile2 & 0x04) == 0x04 || (tile2 & 0x08) == 0x08; }
		case 6:{ return (tile2 & 0x04) == 0x04; }
		case 7:{ int tile3 = accessTile(x - 1, y); return (tile3 & 0x08) == 0x08; }
		default: break;
		}

		return false;
	}

	@Override
	public boolean isUnderwater() {
		return _isUnderwater;
	}

	@Override
	public boolean isMarkable() {
		return _isMarkable;
	}

	@Override
	public boolean isTeleportable() {
		return _isTeleportable;
	}

	@Override
	public boolean isEscapable() {
		return _isEscapable;
	}

	@Override
	public boolean isUseResurrection() {
		return _isUseResurrection;
	}

	@Override
	public boolean isUsePainwand() {
		return _isUsePainwand;
	}

	@Override
	public boolean isEnabledDeathPenalty() {
		return _isEnabledDeathPenalty;
	}

	@Override
	public boolean isTakePets() {
		return _isTakePets;
	}

	@Override
	public boolean isRecallPets() {
		return _isRecallPets;
	}

	@Override
	public boolean isUsableItem() {
		return _isUsableItem;
	}

	@Override
	public boolean isUsableSkill() {
		return _isUsableSkill;
	}

	@Override
	public boolean isFishingZone(int x, int y) {
		return accessOriginalTile(x, y) == 16;
	}

	@Override
	public boolean isExistDoor(int x, int y) {
		for (L1DoorInstance door : DoorSpawnTable.getInstance().getDoorList()) {
			if (_mapId != door.getMapId()) {
				continue;
			}
			if (door.getOpenStatus() == ActionCodes.ACTION_Open) {
				continue;
			}
			if (door.isDead()) {
				continue;
			}
			int leftEdgeLocation = door.getLeftEdgeLocation();
			int rightEdgeLocation = door.getRightEdgeLocation();
			int size = rightEdgeLocation - leftEdgeLocation;
			if (size == 0) {
				if (x == door.getX() && y == door.getY()) {
					return true;
				}
			} else {
				if (door.getDirection() == 0) { 
					for (int doorX = leftEdgeLocation;
					doorX <= rightEdgeLocation; doorX++) {
						if (x == doorX && y == door.getY()) {
							return true;
						}
					}
				} else {
					for (int doorY = leftEdgeLocation;
					doorY <= rightEdgeLocation; doorY++) {
						if (x == door.getX() && y == doorY) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean isCloseZone(int locX, int locY)
	{
		int tile = accessOriginalTile(locX, locY);
		// System.out.println("tile : " + tile);
		// ���ν��̶�� üũ �ɶ�
		if (tile == 12 || (tile & 0x30) == tile)
		{
			String key = new StringBuilder().append(_mapId).append(locX).append(locY).toString();
			return !MapFixKeyTable.getInstance().isLockey(key); // key�� ������ true;
		}
		return false;
	}
	@Override
	public String toString(Point pt) {
		return "" + getOriginalTile(pt.getX(), pt.getY());
	}
}
