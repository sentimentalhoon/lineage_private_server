package l1j.server.server.model;

public class MoveState {
	private int _heading; // �� ���� 0. �»� 1. �� 2. ��� 3. ������ 4. ���� 5. �� 6. ���� 7. ��
	private int _moveSpeed; // �� ���ǵ� 0. ��� 1. ���� �ľ� 2. ���ο�
	private int _braveSpeed; // �� ġ��ħ �̺� ���� 0. ��� 1. ġ��ħ �̺�
	
	public int getHeading() 		 { return _heading;		}
	public void setHeading(int i) 	 { _heading = i;		}

	public int getMoveSpeed() 		 { return _moveSpeed; 	}
	public void setMoveSpeed(int i)  { _moveSpeed = i;	 	}

	public int getBraveSpeed() 		 { return _braveSpeed;	}
	public void setBraveSpeed(int i) { _braveSpeed = i; 	}
}
