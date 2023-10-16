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
package l1j.server.server.model;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1EtcItem;

// Referenced classes of package l1j.server.server.model:
// L1ItemDelay

public class L1ItemDelay {

	private L1ItemDelay() {
	}

	static class ItemDelayTimer implements Runnable {
		private int _delayId;
		private L1Character _cha;

		public ItemDelayTimer(L1Character cha, int id, int time) {
			_cha = cha;
			_delayId = id;
		}

		@Override
		public void run() {
			stopDelayTimer(_delayId);
		}

		public void stopDelayTimer(int delayId) {
			_cha.removeItemDelay(delayId);
		}
	}

	public static void onItemUse(L1Character cha, L1ItemInstance item) {
		int delayId = 0;
		int delayTime = 0;

		if (item.getItem().getType2() == 0) {
			delayId = ((L1EtcItem) item.getItem()).get_delayid();
			delayTime = ((L1EtcItem) item.getItem()).get_delaytime();
		} else if (item.getItem().getType2() == 1) {
			return;
		} else if (item.getItem().getType2() == 2) {
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				if (item.getItem().getItemId() == 20077
						|| item.getItem().getItemId() == 20062
						|| item.getItem().getItemId() == 120077) {
					if (item.isEquipped() && !pc.isInvisble()) {
						pc.beginInvisTimer();
					}
				}
			} else {
				return;
			}
		}

		ItemDelayTimer timer = new ItemDelayTimer(cha, delayId, delayTime);

		cha.addItemDelay(delayId, timer);
		GeneralThreadPool.getInstance().schedule(timer, delayTime);
	}
}
