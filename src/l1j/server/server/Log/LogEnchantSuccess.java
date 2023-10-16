/*
 * ��æƮ ���� �ҽ��Դϴ�. 
 * �� �ҽ��� ��æƮ�� ����������� �۵��մϴ�.
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

public class LogEnchantSuccess {
	private static Logger _log = Logger.getLogger(LogEnchantSuccess.class.getName());

	public void storeLogEnchantSuccess(L1PcInstance pc, L1ItemInstance item, int enchantbefore, int enchantafter, int enchantnum) {
		File file = new File("LogDB/EnchantSuccess.txt");
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
				out = new DataOutputStream(new FileOutputStream("LogDB/EnchantSuccess.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                              ��æƮ ���� �α��Դϴ�.                                   #\r\n".getBytes());
				out.write("#                                                            by ����                     #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#�ð�	������	����	�ɸ���id	�ɸ����̸�	������id	�������̸�	�����۰���	��������æ	��������æ(��)	��������æ(��)	��������æ(����)#\r\n".getBytes());
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
				ditem = item.getCount() + "	"; // ������ ����
				out.write(ditem.getBytes());
				ditem = enchantbefore + "	"; // ������ ��æ�� ��
				out.write(ditem.getBytes());
				ditem = enchantafter + "	"; // ������ ��æ�� ��
				out.write(ditem.getBytes());
				int enchantdiff = enchantafter > enchantbefore ? enchantafter - enchantbefore : enchantbefore - enchantafter;
				ditem = enchantdiff + "	"; // ������ ��æ�� ����
				out.write(ditem.getBytes());
				ditem = enchantnum + "\r\n"; // ������ ��æ��
				out.write(ditem.getBytes());
			} catch (Exception e) {
				_log.warning("enchantsuccess log oufutstream error:" + e);
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
				rfile = new RandomAccessFile("LogDB/EnchantSuccess.txt", "rw");
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
				ditem = item.getCount() + "	"; // ������ ����
				rfile.writeBytes(ditem);
				ditem = enchantbefore + "	"; // ������ ��æ�� ��
				rfile.writeBytes(ditem);
				ditem = enchantafter + "	"; // ������ ��æ�� ��
				rfile.writeBytes(ditem);
				int enchantdiff = enchantafter > enchantbefore ? enchantafter - enchantbefore : enchantbefore - enchantafter;
				ditem = enchantdiff + "	"; // ������ ��æ�� ����
				rfile.writeBytes(ditem);
				ditem = enchantnum + "\r\n"; // ������ ��æ��
				rfile.writeBytes(ditem);
			} catch (Exception e) {
				_log.warning("enchantsuccess log randomacess error:" + e);
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
