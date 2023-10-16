//******************************************************************************
// File Name	: L1Node.java
// Description	: ��� Ŭ����
// Create		: 2003/04/01 JongHa Woo
// Update		: 2008/03/17 SiraSoni
//******************************************************************************
package l1j.server.server.model;

public class L1Node {
	public int f;				// f = g+h
	public int h;				// �޸���ƽ ��
	public int g;				// ��������� �Ÿ�
	public int x, y;			// ����� ��ġ
	public L1Node prev;			// ���� ���
	public L1Node	direct[];	// ������ ���
	public L1Node	next;		// ���� ���
	
	//*************************************************************************
	// Name : L1Node()
	// Desc : ������
	//*************************************************************************
	public L1Node() {
		direct = new L1Node[8];
		
		for ( int i = 0; i < 8; i++) {
			direct[i] = null;
		}
	}
}

