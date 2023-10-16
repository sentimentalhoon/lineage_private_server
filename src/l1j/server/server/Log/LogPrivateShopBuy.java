/*
 * ���� ����(buy) �ҽ��Դϴ�. 
 * �� �ҽ��� ���λ������� ������ �� ���..�� ���λ��� buyâ�� ��������� �۵��մϴ�.
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

public class LogPrivateShopBuy {
	private static Logger _log = Logger.getLogger(LogPrivateShopBuy.class.getName());

	public void storeLogPrivateShopBuy(L1PcInstance pc, L1PcInstance target, L1ItemInstance item, int itembefore, int itemafter, int buycount) {
		File file = new File("LogDB/PriavteShopBuy.txt");
		boolean fileex = file.exists();
		if(!fileex){
			File file2 = new File("LogDB/");
			file2.mkdirs();
			DataOutputStream out = null;
			String slog = null;

			Date time1 = new Date(); // ����ð�
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try{
				out = new DataOutputStream(new FileOutputStream("LogDB/PriavteShopBuy.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                              ���λ���(buy) �α��Դϴ�.                                 #\r\n".getBytes());
				out.write("#                                                            by ����                     #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#�ð�	������	����	�ɸ���id	�ɸ����̸�	��������ip	�������ΰ���	��������id	���������ɸ��̸�	������id	�������̸�	��������æ �����۰���(����)	���������(����)	���İ���(����)	��������(��-��)	�갹��#\r\n".getBytes());
				//out.write("\r\n\r\n".getBytes());
				slog = fm + "	"; // �ð�
				out.write(slog.getBytes());
				slog = pc.getNetConnection().getIp() + "	"; // ������
				out.write(slog.getBytes());
				slog = pc.getAccountName() + "	"; // ����
				out.write(slog.getBytes());
				slog = pc.getId() + "	"; // pcid
				out.write(slog.getBytes());
				slog = pc.getName() + "	"; // �ɸ��̸�
				out.write(slog.getBytes());
				slog = target.getNetConnection().getIp() + "	"; // ������
				out.write(slog.getBytes());
				slog = target.getAccountName() + "	"; // ����
				out.write(slog.getBytes());
				slog = target.getId() + "	"; // pcid
				out.write(slog.getBytes());
				slog = target.getName() + "	"; // �ɸ��̸�
				out.write(slog.getBytes());
				slog = item.getId() + "	"; // ������id
				out.write(slog.getBytes());
				slog = item.getItem().getName() + "	"; // �������̸�
				out.write(slog.getBytes());
				slog = item.getEnchantLevel() + "	"; // ������ ��æ��
				out.write(slog.getBytes());
				slog = item.getCount() + "	"; // ������ ����
				out.write(slog.getBytes());
				slog = itembefore + "	"; // ������ ����(��)
				out.write(slog.getBytes());
				slog = itemafter + "	"; // ������ ����(��)
				out.write(slog.getBytes());
				int itemdiff = itembefore - itemafter;
				if(itemdiff < 0){
					itemdiff = -itemdiff;
				}
				slog = itemdiff + "	"; // ������ ���� ��-��
				out.write(slog.getBytes());
				slog = buycount + "\r\n"; // ������ ����
				out.write(slog.getBytes());
			} catch (Exception e) {
				_log.warning("PrivateShopBuy log oufutstream error:" + e);
				e.printStackTrace();
			} finally {
				try{
					out.close();
				}catch(Exception e1){}
			}
		}else{
			RandomAccessFile rfile = null;
			String slog = null;

			Date time1 = new Date(); // ����ð�
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try {
				rfile = new RandomAccessFile("LogDB/PriavteShopBuy.txt", "rw");
				rfile.seek(rfile.length()); //��������ġ ����

				slog = fm + "	"; // �ð�
				rfile.writeBytes(slog);
				slog = pc.getNetConnection().getIp() + "	"; // ������
				rfile.writeBytes(slog);
				slog = pc.getAccountName() + "	"; // ����
				rfile.writeBytes(slog);
				slog = pc.getId() + "	"; // pcid
				rfile.writeBytes(slog);
				slog = pc.getName() + "	"; // �ɸ��̸�
				rfile.writeBytes(encode(slog));
				slog = target.getNetConnection().getIp() + "	"; // ������
				rfile.writeBytes(slog);
				slog = target.getAccountName() + "	"; // ����
				rfile.writeBytes(slog);
				slog = target.getId() + "	"; // pcid
				rfile.writeBytes(slog);
				slog = target.getName() + "	"; // �ɸ��̸�
				rfile.writeBytes(encode(slog));
				slog = item.getId() + "	"; // ������id
				rfile.writeBytes(slog);
				slog = item.getItem().getName() + "	"; // �������̸�
				rfile.writeBytes(encode(slog));
				slog = item.getEnchantLevel() + "	"; // ������ ��æ��
				rfile.writeBytes(slog);
				slog = item.getCount() + "	"; // ������ ����
				rfile.writeBytes(slog);
				slog = itembefore + "	"; // ������ ����(��)
				rfile.writeBytes(slog);
				slog = itemafter + "	"; // ������ ����(��)
				rfile.writeBytes(slog);
				int itemdiff = itembefore - itemafter;
				if(itemdiff < 0){
					itemdiff = -itemdiff;
				}
				slog = itemdiff + "	"; // ������ ���� ��-��
				rfile.writeBytes(slog);
				slog = buycount + "\r\n"; // ������ ����
				rfile.writeBytes(slog);
			} catch (Exception e) {
				_log.warning("PrivateShopBuy log randomacess error:" + e);
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