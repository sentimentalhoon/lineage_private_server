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
package l1j.server.server.types;

/**
 * ��ǥ �»��� ��(left, top), �� ������ ��(right, bottom)�� ���� �ѷ����̴� ��ǥ�� area�� �����ϴ� Ŭ����.
 */
public class Rectangle {
	private int _left;
	private int _top;
	private int _right;
	private int _bottom;

	public Rectangle(Rectangle rect) {
		set(rect);
	}

	public Rectangle(int left, int top, int right, int bottom) {
		set(left, top, right, bottom);
	}

	public Rectangle() {
		this(0, 0, 0, 0);
	}

	public void set(Rectangle rect) {
		set(rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight());
	}

	public void set(int left, int top, int right, int bottom) {
		_left = left;
		_top = top;
		_right = right;
		_bottom = bottom;
	}

	public int getLeft() {	return _left;	}

	public int getTop() {	return _top;	}

	public int getRight() {	return _right;	}

	public int getBottom() {	return _bottom;	}

	public int getWidth() {	return _right - _left;	}

	public int getHeight() {	return _bottom - _top;	}

	/**
	 * ������ ��(x, y)��, �� Rectangle�� �������� �ִ����� �����Ѵ�.
	 * 
	 * @param x
	 *            �����ϴ� ���� X��ǥ
	 * @param y
	 *            �����ϴ� ���� Y��ǥ
	 * @return ��(x, y)�� �� Rectangle�� �������� �ִ� ���, true.
	 */
	public boolean contains(int x, int y) {
		return (_left <= x && x <= _right) && (_top <= y && y <= _bottom);
	}

	/**
	 * ������ Point��, �� Rectangle�� �������� �ִ����� �����Ѵ�.
	 * 
	 * @param pt
	 *            �����ϴ� Point
	 * @return pt�� �� Rectangle�� �������� �ִ� ���, true.
	 */
	public boolean contains(Point pt) {
		return contains(pt.getX(), pt.getY());
	}
}
