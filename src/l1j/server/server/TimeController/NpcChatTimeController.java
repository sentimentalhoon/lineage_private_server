/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.   See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.TimeController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.datatables.NpcChatTable;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.templates.L1NpcChat;
import l1j.server.server.utils.CommonUtil;

public class NpcChatTimeController implements Runnable {
	private static Logger _log = Logger.getLogger(NpcChatTimeController.class
			. getName());

	private static NpcChatTimeController _instance;

	public static NpcChatTimeController getInstance() {
		if (_instance == null) {
			_instance = new NpcChatTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkNpcChatTime(); // ä�� ���� �ð��� üũ
				Thread.sleep(60000);
			}
		} catch (Exception e1) {
			_log.warning(e1.getMessage());
		}
	}

	private void checkNpcChatTime() {
		L1NpcInstance obj = null;
		for (L1NpcChat npcChat : NpcChatTable.getInstance(). getAllGameTime()) {
			if (isChatTime(npcChat.getGameTime())) {
				int npcId = npcChat.getNpcId();
				for (L1Object temp : L1World.getInstance().getObject()) {
					if(temp instanceof L1NpcInstance){
						obj = (L1NpcInstance)temp;
						if (obj.getNpcTemplate(). get_npcId() == npcId) {
							obj.startChat(L1NpcInstance.CHAT_TIMING_GAME_TIME);
						}
					}
					
				}
			}
		}
	}

	private boolean isChatTime(int chatTime) {
		int nowTime = Integer.valueOf(CommonUtil.dateFormat("HHmm"));
		return (nowTime == chatTime);
	}
}
