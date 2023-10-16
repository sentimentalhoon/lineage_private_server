/*
 * ������ ��� �ҽ��Դϴ�. 
 * �� �ҽ��� �������� ���� ���������� �۵��մϴ�.
 * �׸� ���̴� ������� ������ �ϰ� �˴ϴ�.
 * ������ ȣȯ�ǵ��� �ҽ��� �����ϸ�, �������� ���ø� ���� ���մϴ�,
 * ������ ���º��� ���� ���������̳�, ���¿��ǽ��� ����Ͻô°� ��õ�帳�ϴ�.
 * ���� �ҽ� ��ü�� �����ؾ� �� ���̳�, �߰��ؾ��� �ǰ��� �����ø�,
 * wi������ - ���� �� ã���ּ���.
 * by �������� - ����
 */
package l1j.server.server.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1ItemInstance;

public class LogDropItem {
	private static Logger _log = Logger.getLogger(LogDropItem.class.getName());

	public void storeLogDropItem(L1PcInstance pc, L1ItemInstance item, int before_inven, int after_inven, int before_ground, int after_ground, int dropcount){
		File file = new File("LogDB/DropItem.txt");
		boolean fileex = file.exists();
		if(!fileex){
			File file2 = new File("LogDB/");
			file2.mkdirs();
			DataOutputStream out = null;
			String ditem = null;

			Date time1 = new Date(); // ����ð�
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try{
				out = new DataOutputStream(new FileOutputStream("LogDB/DropItem.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                              ������ ��� �α��Դϴ�.                                   #\r\n".getBytes());
				out.write("#                                                            by ����                     #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#�ð�	������	����	�ɸ���id	�ɸ����̸�	������id	�������̸�	��������æ	������κ��Ӱ���	������κ��Ӱ���	������׶��尹��	����ı׶��尹��	�������#\r\n".getBytes());
				//out.write("\r\n\r\n".getBytes());
				ditem = fm + "	"; // �ð�
				out.write(ditem.getBytes());
				ditem = pc.getNetConnection().getIp() + "	"; // ������
				out.write(ditem.getBytes());
				ditem = pc.getAccountName() + "	"; // ����
				out.write(ditem.getBytes());
				ditem = pc.getId() + "	"; // pcid
				out.write(ditem.getBytes());
				ditem = pc.getName() + "	"; // �ɸ��̸�
				out.write(ditem.getBytes());
				ditem = item.getId() + "	"; // ������id
				out.write(ditem.getBytes());
				ditem = item.getItem().getName() + "	"; // �������̸�
				out.write(ditem.getBytes());
				ditem = item.getEnchantLevel() + "	"; // ������ ��æ��
				out.write(ditem.getBytes());
				ditem = before_inven + "	"; // ������κ��Ӱ���
				out.write(ditem.getBytes());
				ditem = after_inven + "	"; // ������κ��Ӱ���
				out.write(ditem.getBytes());
				ditem = before_ground + "	"; // ������׶��尹��
				out.write(ditem.getBytes());
				ditem = after_ground + "	"; // ����ı׶��尹��
				out.write(ditem.getBytes());
				ditem = dropcount + "\r\n"; // �������
				out.write(ditem.getBytes());
			} catch (Exception e) {
				_log.warning("DropItem log oufutstream error:" + e);
				e.printStackTrace();
			} finally {
				try{
					out.close();
				}catch(Exception e1){}
			}
		}else{
			RandomAccessFile rfile = null;
			String ditem = null;

			Date time1 = new Date(); // ����ð�
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try {
				rfile = new RandomAccessFile("LogDB/DropItem.txt", "rw");
				rfile.seek(rfile.length()); //��������ġ ����

				ditem = fm + "	"; // �ð�
				rfile.writeBytes(ditem);
				ditem = pc.getNetConnection().getIp() + "	"; // ������
				rfile.writeBytes(ditem);
				ditem = pc.getAccountName() + "	"; // ����
				rfile.writeBytes(ditem);
				ditem = pc.getId() + "	"; // pcid
				rfile.writeBytes(ditem);
				ditem = pc.getName() + "	"; // �ɸ��̸�
				rfile.writeBytes(encode(ditem));
				ditem = item.getId() + "	"; // ������id
				rfile.writeBytes(ditem);
				ditem = item.getItem().getName() + "	"; // �������̸�
				rfile.writeBytes(encode(ditem));
				ditem = item.getEnchantLevel() + "	"; // ������ ��æ��
				rfile.writeBytes(ditem);
				ditem = before_inven + "	"; // ������κ��Ӱ���
				rfile.writeBytes(ditem);
				ditem = after_inven + "	"; // ������κ��Ӱ���
				rfile.writeBytes(ditem);
				ditem = before_ground + "	"; // ������׶��尹��
				rfile.writeBytes(ditem);
				ditem = after_ground + "	"; // ����ı׶��尹��
				rfile.writeBytes(ditem);
				ditem = dropcount + "\r\n"; // �������
				rfile.writeBytes(ditem);
			} catch (Exception e) {
				_log.warning("DropItem log randomacess error:" + e);
				e.printStackTrace();
			} finally {
				try{
					rfile.close();
				}catch(Exception e1){}
			}
		}
	}

	public static String encode(String str){
        String result = "";
        try{
            if(str==null) return result;
            result=new String(str.getBytes("KSC5601"), "8859_1");
        }catch(java.io.UnsupportedEncodingException e){}
        return result;
	}

}
