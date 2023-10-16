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
package l1j.server.server.utils;

import java.util.Random;

public class Dice {
	private static final Random _rnd = new Random(System.nanoTime());
	private final int _faces;

	public Dice(int faces) {
		_faces = faces;
	}

	public int getFaces() {
		return _faces;
	}

	public int roll() {
		return _rnd.nextInt(_faces) + 1;
	}

	public int roll(int count) {
		int n = 0;
		for(int i = 0; i < count; i++) {
			n += roll();
		}
		return n;
	}
}
