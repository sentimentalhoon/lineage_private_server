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

package l1j.server.server.serverpackets;

import l1j.server.server.Opcodes;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_UnityIcon extends ServerBasePacket {

	public S_UnityIcon(int DECREASE, int DECAY_POTION, int SILENCE, int VENOM_RESIST, int WEAKNESS, int DISEASE,
			int DRESS_EVASION, int BERSERKERS, int NATURES_TOUCH, int WIND_SHACKLE, 
			int ERASE_MAGIC, int ADDITIONAL_FIRE, int ELEMENTAL_FALL_DOWN, int ELEMENTAL_FIRE,
			int STRIKER_GALE, int SOUL_OF_FLAME, int POLLUTE_WATER,
			int EXP_POTION, int SCROLL, int SCROLLTPYE,
			int TIKALBOSSDIE,
			int CONCENTRATION, int INSIGHT, int PANIC,
			int MORTAL_BODY, int HORROR_OF_DEATH, int FEAR,
			int PATIENCE, int GUARD_BREAK, int DRAGON_SKIN, int STATUS_FRUIT,
			int COMA, int COMA_TYPE, int FEATHER_BUFF, int FEATHER_TYPE) {
		//00
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(0x14);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(DECREASE); // ��ũ���� ����Ʈ DECREASE
		writeC(DECAY_POTION); // ������ ����
		writeC(0x00);
		writeC(SILENCE); // ���Ϸ���
		writeC(VENOM_RESIST); // ���� ������Ʈ
		//10
		writeC(WEAKNESS); // ��ũ�Ͻ�
		writeC(DISEASE); // ������
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(DRESS_EVASION);                      //�巹���̺����� !
		//20
		writeC(BERSERKERS);                        //����Ŀ�� !
		writeC(NATURES_TOUCH);                       //�����Ľ���ġ
		writeC(WIND_SHACKLE);                         //�����Ŭ
		writeC(ERASE_MAGIC);                         //�̷��������
		writeC(0x00);                              //������������ε� ������ ī���͹̷�ȿ����� ������
		writeC(ADDITIONAL_FIRE);                               //���ų� ���̾�
		writeC(ELEMENTAL_FALL_DOWN);                //������Ż���ٿ�   
		writeC(0x00);
		writeC(ELEMENTAL_FIRE);                     //������Ż ���̾�
		writeC(0x00);
		//30
		writeC(0x00);              //��ô������ �������� ��ġä�����ϰ��մϴ�???�����ܵ��̻���
		writeC(0x00);
		writeC(STRIKER_GALE);                        // ��Ʈ����Ŀ����
		writeC(SOUL_OF_FLAME);                     //�ҿ���� ������
		writeC(POLLUTE_WATER);                          //�÷�������
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);                //�Ӽ����׷� 10? 
		writeC(0x00);
		//40
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);             //sp
		writeC(EXP_POTION);            //exp
		writeC(SCROLL);      //������ȭ�ֹ��� 123 ������?
		writeC(SCROLLTPYE);             //0-hp50hpr4, 1-mp40mpr4, 2-��Ÿ3����3sp3
		//50
		writeC(0x00);
		writeC(0x00);
		writeC(TIKALBOSSDIE);//		writeC(0xa2);  ���ž�� �ູ
		writeC(0x22);//		writeC(0x22);  ���ž�� �ູ
		writeC(CONCENTRATION);                         //����Ʈ���̼�
		writeC(INSIGHT);                        //�λ���Ʈ
		writeC(PANIC);                       //�д�
		writeC(MORTAL_BODY);                       //��Ż�ٵ�                 
		writeC(HORROR_OF_DEATH);                       //ȣ����굥��
		writeC(FEAR);                     //�Ǿ�
		//60
		writeC(PATIENCE);                      //���̼ǽ�
		writeC(GUARD_BREAK);                      //����극��ũ
		writeC(DRAGON_SKIN);                   //�巡�ｺŲ
		writeC(STATUS_FRUIT);             //���׵��
		writeC(0x14);
		writeC(0x00);
		writeC(COMA);//�ð�
		writeC(COMA_TYPE);//Ÿ��
		writeC(0x00);
		writeC(0x00);
		//70
		writeC(0x1a);
		writeC(0x35);
		writeC(0x0d);
		writeC(0x00);
		writeC(0xf4);
		writeC(0xa5);
		writeC(0xdc);
		writeC(0x4a);
		writeC(0x00);
		writeC(0x00);
		//80
		writeC(0x00);
		writeC(0x00);
		writeC(0xa1);
		writeC(0x09);
		writeC(0x35);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		//90
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(FEATHER_BUFF);//��� ���� ���� ����
		writeC(FEATHER_TYPE); // 0x46 �ſ����� 0x47 ���� 0x48 ���� 0x49 ����
		writeC(0x00);
		writeC(0x00);
		//100
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
		writeC(0x00);
	}
	
	@Override
	public byte[] getContent() {
		return getBytes();
	}
}
