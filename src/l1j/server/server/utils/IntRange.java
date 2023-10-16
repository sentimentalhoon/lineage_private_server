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

/**
 * <p>
 * ����ġ low�� �ִ�ġ high�� ���� �ѷ�����, ��ġ�� ������ �����ϴ� Ŭ����.
 * </p>
 * <p>
 * <b>�� Ŭ������ ����ȭ ���� �ʴ´�.</b> ������ thread�� ���ÿ� �� Ŭ������ �ν��Ͻ��� �׼��� ��,
 * 1���̻��� thread�� ������ �����ϴ� ���, �ܺ����� ����ȭ�� �ʿ��ϴ�.
 * </p>
 */
public class IntRange {
	private static final Random _rnd = new Random(System.nanoTime());
	private int _low;
	private int _high;

	public IntRange(int low, int high) {
		_low = low;
		_high = high;
	}

	public IntRange(IntRange range) {
		this(range._low, range._high);
	}

	/**
	 * ��ġ i��, �������� ����� �����ش�.
	 * 
	 * @param i
	 *            ��ġ
	 * @return �������̸� true
	 */
	public boolean includes(int i) {
		return (_low <= i) && (i <= _high);
	}

	public static boolean includes(int i, int low, int high) {
		return (low <= i) && (i <= high);
	}

	/**
	 * ��ġ i��, �� �������� ����.
	 * 
	 * @param i
	 *            ��ġ
	 * @return �� �� �־��� ��
	 */
	public int ensure(int i) {
		int r = i;
		r = (_low <= r) ? r : _low;
		r = (r <= _high) ? r : _high;
		return r;
	}

	public static int ensure(int n, int low, int high) {
		int r = n;
		r = (low <= r) ? r : low;
		r = (r <= high) ? r : high;
		return r;
	}

	/**
	 * �� �������κ��� ������ ���� �����Ѵ�.
	 * 
	 * @return �������� ������ ��
	 */
	public int randomValue() {
		return _rnd.nextInt(getWidth() + 1) + _low;
	}

	public int getLow() {
		return _low;
	}

	public int getHigh() {
		return _high;
	}

	public int getWidth() {
		return _high - _low;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntRange)) {
			return false;
		}
		IntRange range = (IntRange) obj;
		return (this._low == range._low) && (this._high == range._high);
	}

	@Override
	public String toString() {
		return "low=" + _low + ", high=" + _high;
	}
}
