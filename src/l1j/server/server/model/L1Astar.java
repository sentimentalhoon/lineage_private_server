//******************************************************************************
// File Name	: L1AStar.java
// Description	: A* �˰����� ����� ��ã�� Ŭ����
// Create		: 2003/04/01 JongHa Woo
// Update		: 2008/03/17 SiraSoni
//******************************************************************************
package l1j.server.server.model;

import l1j.server.server.model.L1Character;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.Instance.L1NpcInstance;

public class L1Astar {
	// ���� ���, ���� ��� ����Ʈ
	private L1Node	OpenNode, ClosedNode;
	// �ִ� ���� ȸ��
	private static final int LIMIT_LOOP = 150;
	//*************************************************************************
	// Name : L1AStar()
	// Desc : ������
	//*************************************************************************
	public L1Astar() {
		OpenNode = null;
		ClosedNode = null;
	}
	//*************************************************************************
	// Name : ResetPath()
	// Desc : ������ ������ ��θ� ����
	//*************************************************************************
	public void ResetPath() {
		L1Node tmp;
		while( OpenNode != null ) {
			tmp = OpenNode.next;
			OpenNode = null;
			OpenNode = tmp;
		}
		while( ClosedNode != null ) {
			tmp = ClosedNode.next;
			ClosedNode = null;
			ClosedNode = tmp;
		}
	}
	//*************************************************************************
	// Name : FindPath()
	// Desc : ������ġ�� ��ǥ��ġ�� �Է� �޾� ��γ�� ����Ʈ�� ��ȯ
	//*************************************************************************
	public L1Node FindPath(L1Object npc, L1Character target) {
		return FindPath(npc, target.getX(), target.getY(), target.getMapId());
	}
	
	public L1Node FindPath(L1Object npc, int tx, int ty, int mapId) {
		L1Node	src, best = null;
		int	count = 0;
		
		src = new L1Node();
		src.g = 0;
		src.h = (tx - npc.getX()) * (tx - npc.getX()) + (ty - npc.getY()) * (ty - npc.getY());
		src.f = src.h;
		src.x = npc.getX();
		src.y = npc.getY();
		OpenNode = src;
		
		while (count < LIMIT_LOOP) {
			if ( OpenNode == null ) {
				return best;
			}
			best = OpenNode;
			OpenNode = best.next;
			best.next = ClosedNode;
			ClosedNode = best;
			if (best == null) {
				return null;
			}
			if (Math.max(Math.abs(tx - best.x), Math.abs(ty - best.y)) == 1) {
				return best;
			}
			if( MakeChild(best, tx, ty, npc.getMapId()) == 0 && count == 0 ) {
				return null;
			}
			count++;
		}
		return best;
	}
	//*************************************************************************
	// Name : MakeChild()
	// Desc : �Է¹��� ����� ������ ����� Ȯ��
	//*************************************************************************
	public char MakeChild(L1Node node, int tx, int ty, short m) {
		int		x, y;
		char	flag = 0;
		char	cc[] = {0, 0, 0, 0, 0, 0, 0, 0};
		
		x = node.x;
		y = node.y;
		// ������ ���� �̵��������� �˻�
		cc[0] = IsMove(x  , y+1, m);
		cc[1] = IsMove(x-1, y+1, m);
		cc[2] = IsMove(x-1, y  , m);
		cc[3] = IsMove(x-1, y-1, m);
		cc[4] = IsMove(x  , y-1, m);
		cc[5] = IsMove(x+1, y-1, m);
		cc[6] = IsMove(x+1, y  , m);
		cc[7] = IsMove(x+1, y+1, m);
		// �̵������� �����̶�� ��带 �����ϰ� �򰡰� ���
		if ( cc[2] == 1 ) {
			MakeChildSub(node, x-1, y, tx, ty);
			flag = 1;
		}
		if ( cc[6] == 1 ) {
			MakeChildSub(node, x+1, y, tx, ty);
			flag = 1;
		}
		if ( cc[4] == 1 ) {
			MakeChildSub(node, x, y-1, tx, ty);
			flag = 1;
		}
		if ( cc[0] == 1 ) {
			MakeChildSub(node, x, y+1, tx, ty);
			flag = 1;
		}
		if ( cc[7] == 1 && cc[6] == 1 && cc[0] == 1 ) {
			MakeChildSub(node, x+1, y+1, tx, ty);
			flag = 1;
		}
		if ( cc[3] == 1 && cc[2] == 1 && cc[4] == 1 ) {
			MakeChildSub(node, x-1, y-1, tx, ty);
			flag = 1;
		}
		if ( cc[5] == 1 && cc[4] == 1 && cc[6] == 1 ) {
			MakeChildSub(node, x+1, y-1, tx, ty);
			flag = 1;
		}
		if ( cc[1] == 1 && cc[0] == 1 && cc[2] == 1 ){
			MakeChildSub(node, x-1, y+1, tx, ty);
			flag = 1;
		}

		return flag;
	}
	//*************************************************************************
	// Name : IsMove()
	// Desc : �̵������� ��ġ���� �˻�
	//*************************************************************************
	public char IsMove(int x, int y, short mapid) {
		L1Map map = L1WorldMap.getInstance().getMap(mapid);
		if ( map.isPassable(x, y) == false ) {
			return 0;
		}
		if ( map.isExistDoor(x, y) == true) {
			return 0;
		}
		
		return 1;
	}
	//*************************************************************************
	// Name : MakeChildSub()
	// Desc : ��带 ����. ������峪 ������忡 �̹� �ִ� ����� 
	//        �������� ���Ͽ� f�� �� ������ ���� ����
	//        ������忡 �ִٸ� �׿� ����� ��� ������ ������ ���� ����
	//*************************************************************************
	public void MakeChildSub(L1Node node, int x, int y, int tx, int ty) {
		L1Node	old = null, child = null;
		int		i;
		int		g = node.g + 1;
		// �����尡 ���� ��忡 �ְ� f�� �� ������ ���� ����
		if ( (old = IsOpen(x, y)) != null ) {
			for ( i = 0; i < 8; i++ ) {
				if ( node.direct[i] == null ) {
					node.direct[i] = old;
					break;
				}
			}
			if ( g < old.g ) {
				old.prev = node;
				old.g = g;
				old.f = old.h + old.g;
			}
		}
		// �����尡 ���� ��忡 �ְ� f�� �� ������ ���� ����
		else if ( (old = IsClosed(x, y)) != null ) {
			for ( i = 0; i < 8; i++ ) {
				if ( node.direct[i] == null ) {
					node.direct[i] = old;
					break;
				}
			}
			if ( g < old.g ) {
				old.prev = node;
				old.g = g;
				old.f = old.h + old.g;
				// �����尡 ������忡 �ִٸ� �׿� ����� ��� ������ ������ ����
				//MakeDown(old);
			}
		}
		// ���ο� ����� ������� �����ϰ� ������忡 �߰�
		else {
			// ���ο� ��� ����
			child = new L1Node();
			
			child.prev = node;
			child.g = g;
			child.h = (x-tx)*(x-tx) + (y-ty)*(y-ty);
			child.f = child.h + child.g;
			child.x = x;
			child.y = y;
			
			// ���ο� ��带 ������忡 �߰�
			InsertNode(child);

			for ( i = 0; i < 8; i++ ) {
				if ( node.direct[i] == null ) {
					node.direct[i] = child;
					break;
				}
			}
		}
	}
	//*************************************************************************
	// Name : IsOpen()
	// Desc : �Էµ� ��尡 ����������� �˻�
	//*************************************************************************
	public L1Node IsOpen(int x, int y) {
		L1Node tmp = OpenNode;
		while ( tmp != null ) {
			if ( tmp.x == x && tmp.y == y ) {
				return tmp;
			}
			tmp = tmp.next;
		}
		return null;
	}
	//*************************************************************************
	// Name : IsClosed()
	// Desc : �Էµ� ��尡 ����������� �˻�
	//*************************************************************************
	public L1Node IsClosed(int x, int y) {
		L1Node tmp = ClosedNode;
		while ( tmp != null ) {
			if ( tmp.x == x && tmp.y == y ) {
				return tmp;
			}
			tmp = tmp.next;
		}
		return null;
	}
	//*************************************************************************
	// Name : InsertNode()
	// Desc : �Էµ� ��带 ������忡 f���� ���� �����Ͽ� �߰�
	//        f���� �������� ���� ���� ������ -> ������ ���
	//*************************************************************************
	public void InsertNode(L1Node src) {
		L1Node old = null, tmp = null;
		if( OpenNode == null ) {
			OpenNode = src;
			return;
		}
		tmp = OpenNode;
		while ( tmp != null && (tmp.f < src.f) ) {
			old = tmp;
			tmp = tmp.next;
		}
		if ( old != null ) {
			src.next = tmp;
			old.next = src;
		} else {
			src.next = tmp;
			OpenNode = src;
		}
	}
}
