/*
 * �ɸ��� ���� �ҽ��Դϴ�. 
 * �� �ҽ��� �ɸ��͸� ����������� �۵��մϴ�.
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

public class LogDeleteChar {
	private static Logger _log = Logger.getLogger(LogDeleteChar.class.getName());

	public void storeLogDeleteChar(L1PcInstance pc, String hostip){
		File file = new File("LogDB/DeleteChar.txt");
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
				out = new DataOutputStream(new FileOutputStream("LogDB/DeleteChar.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                              ĳ���� ���� �α��Դϴ�.                                   #\r\n".getBytes());
				out.write("#                                                            by ����                     #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#�ð�	������	����	�ɸ���id	�ɸ����̸�#\r\n".getBytes());
				//out.write("\r\n\r\n".getBytes());
				ditem = fm + "	"; // �ð�
				out.write(ditem.getBytes());
				ditem = hostip + "	"; // ������
				out.write(ditem.getBytes());
				ditem = pc.getAccountName() + "	"; // ����
				out.write(ditem.getBytes());
				ditem = pc.getId() + "	"; // pcid
				out.write(ditem.getBytes());
				ditem = pc.getName() + "\r\n"; // �ɸ��̸�
				out.write(ditem.getBytes());
			} catch (Exception e) {
				_log.warning("DeleteChar log oufutstream error:" + e);
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
				rfile = new RandomAccessFile("LogDB/DeleteChar.txt", "rw");
				rfile.seek(rfile.length()); //��������ġ ����

				ditem = fm + "	"; // �ð�
				rfile.writeBytes(ditem);
				ditem = hostip + "	"; // ������
				rfile.writeBytes(ditem);
				ditem = pc.getAccountName() + "	"; // ����
				rfile.writeBytes(ditem);
				ditem = pc.getId() + "	"; // pcid
				rfile.writeBytes(ditem);
				ditem = pc.getName() + "\r\n"; // �ɸ��̸�
				rfile.writeBytes(encode(ditem));
			} catch (Exception e) {
				_log.warning("DeleteChar log randomacess error:" + e);
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
