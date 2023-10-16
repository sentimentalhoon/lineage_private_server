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
package l1j.server.server.model.monitor;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.Instance.L1PcInstance;

public class L1PcHellMonitor extends L1PcMonitor {

	public L1PcHellMonitor(int oId) {
		super(oId);
	}

	@Override
	public void execTask(L1PcInstance pc) {
		if (pc.isDead()) {
			return;
		}
		pc.setHellTime(pc.getHellTime() - 1);
		if (pc.getHellTime() <= 0) {
			Runnable r = new L1PcMonitor(pc.getId()) {
				@Override
				public void execTask(L1PcInstance pc) {
					pc.endHell();
				}
			};
			GeneralThreadPool.getInstance().execute(r);
		}
	}
}
