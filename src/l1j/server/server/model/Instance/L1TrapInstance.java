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

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.trap.L1Trap;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_Trap;
import l1j.server.server.types.Point;

public class L1TrapInstance extends L1Object {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final L1Trap _trap;
	private final Point _baseLoc = new Point();
	private final Point _rndPt = new Point();
	private int _trapDoorId;
	private final int _span;
	private boolean _isEnable = true;
	private final String _nameForView;

	private List<L1PcInstance> _knownPlayers = new CopyOnWriteArrayList<L1PcInstance>();

	private static final Random _random = new Random(System.nanoTime());

	public L1TrapInstance(int id, L1Trap trap, L1Location loc, Point rndPt,
			int span, int trapDoorId) {
		setId(id);
		_trap = trap;
		_trapDoorId = trapDoorId;
		getLocation().set(loc);
		_baseLoc.set(loc);
		_rndPt.set(rndPt);
		_span = span;
		_nameForView = "trap";

		resetLocation();
	}

	public L1TrapInstance(int id, L1Location loc) {
		setId(id);
		_trap = L1Trap.newNull();
		getLocation().set(loc);
		_span = 0;
		_nameForView = "trap base";		
	}

	public void resetLocation() {
		if (_rndPt.getX() == 0 && _rndPt.getY() == 0) {
			return;
		}
		L1Map map = null;
		for (int i = 0; i < 50; i++) {
			int rndX = _random.nextInt(_rndPt.getX() + 1) * (_random.nextInt(2) == 1 ? 1 : -1);
			int rndY = _random.nextInt(_rndPt.getY() + 1) * (_random.nextInt(2) == 1 ? 1 : -1);

			rndX += _baseLoc.getX();
			rndY += _baseLoc.getY();

			map = getLocation().getMap();
			if (map.isInMap(rndX, rndY) && map.isPassable(rndX, rndY)) {
				getLocation().set(rndX, rndY);
				break;
			}
		}
	}

	public void enableTrap() {
		_isEnable = true;
	}

	public void disableTrap() {
		_isEnable = false;

		for (L1PcInstance pc : _knownPlayers) {
			pc.getNearObjects().removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		_knownPlayers.clear();
	}

	public boolean isEnable() {
		return _isEnable;
	}

	public int getSpan() {
		return _span;
	}
	
	public int getTrapDoorId(){
		return _trapDoorId;
	}

	public void onTrod(L1PcInstance trodFrom) {
		_trap.onTrod(trodFrom, this);
	}

	public void onDetection(L1PcInstance caster) {
		_trap.onDetection(caster, this);
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		if (perceivedFrom.getSkillEffectTimerSet().hasSkillEffect(L1SkillId.GMSTATUS_SHOWTRAPS)) {
			perceivedFrom.getNearObjects().addKnownObject(this);
			perceivedFrom.sendPackets(new S_Trap(this, _nameForView));
			_knownPlayers.add(perceivedFrom);
		}
	}
}
