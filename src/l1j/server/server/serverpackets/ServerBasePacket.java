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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServerBasePacket {

	private int OpKey;	// opcode Key

	private boolean isKey = true;
	private static Logger _log = Logger.getLogger(ServerBasePacket.class
			.getName());

	ByteArrayOutputStream _bao = new ByteArrayOutputStream();

	protected ServerBasePacket() {
	}

	// Key
	private void setKey(int i ){
		OpKey = i;
	}private int getKey(){
		return OpKey;
	}

	protected void writeD(int value) {
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
		_bao.write(value >> 16 & 0xff);
		_bao.write(value >> 24 & 0xff);
	}

	protected void writeH(int value) {
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
	}

	protected void writeC(int value) {
		_bao.write(value & 0xff);
		// ���ڵ� wirteC ù��° ȣ�⸸ ����...
		if(isKey){
			setKey(value);
			isKey = false;
		}
	}

	protected void writeP(int value) {
		_bao.write(value);
	}

	protected void writeL(long value) {
		_bao.write((int) (value & 0xff));
	}

	protected void writeF(double org) {
		long value = Double.doubleToRawLongBits(org);
		_bao.write((int) (value & 0xff));
		_bao.write((int) (value >> 8 & 0xff));
		_bao.write((int) (value >> 16 & 0xff));
		_bao.write((int) (value >> 24 & 0xff));
		_bao.write((int) (value >> 32 & 0xff));
		_bao.write((int) (value >> 40 & 0xff));
		_bao.write((int) (value >> 48 & 0xff));
		_bao.write((int) (value >> 56 & 0xff));
	}

	protected void writeS(String text) {
		try {
			if (text != null) {
				_bao.write(text.getBytes("EUC-KR"));
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		_bao.write(0);
	}
	
	protected int writeS2(String text){
		try{
			if(text != null){	
				_bao.write(text.getBytes("EUC-KR"));
				_bao.write(0xff);
			}			
		}catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	protected void writeSS(String text){ 
		try{ 
			if(text != null){ 
				byte[] test = text.getBytes("EUC-KR"); 
				for(int i=0 ; i<test.length ; ){ 
					if((test[i]&0xff)>=0x7F){ 
						/** �ѱ� **/ 
						_bao.write(test[i+1]); 
						_bao.write(test[i]); 
					i += 2; 
					}else{ 
						/** ����&���� **/ 
						_bao.write(test[i]); 
						_bao.write(0); 
						i += 1; 
					} 
				} 
			} 
		}catch (Exception e){ 
					e.printStackTrace(); 
				} 
				_bao.write(0); 
				_bao.write(0); 
	}

	protected void writeByte(byte[] text) {
		try {
			if (text != null) {
				_bao.write(text);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	protected void writeB(byte[] data){
		if(data != null){
			_bao.write(data, 0, data.length);
		}
	}

	public int getLength() {
		return _bao.size() + 2;
	}

	public byte[] getBytes() {
		int padding = _bao.size() % 8;

		if (padding != 0) {
			for (int i = padding; i < 8; i++) {
				writeC(0x00);
			}
		}

		return _bao.toByteArray();
	}

	public abstract byte[] getContent() throws IOException;
	
	/**
	 * ���� ��Ŷ�� ������ ��Ÿ���� ĳ���� ������ �����ش�. ("[S] S_WhoAmount" �� )
	 */
	public String getType(){
		return "";
	}

	public String toString(){
		// getType() �� ������ "" �̶�� �� �ƴϸ� ��Ŷ�̸� + �ڵ尪 ���
		// [���ڵ�] ��Ŷ��
		String sTemp = getType().equals("") ? "" : "["+getKey()+"] " + getType();
		return sTemp;
	}
}
