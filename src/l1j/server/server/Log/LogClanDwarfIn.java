/*
 * Ŭ�� â��(in) �ҽ��Դϴ�. 
 * �� �ҽ��� Ŭ��â���� ������ �ñ��� �۵��մϴ�.
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

public class LogClanDwarfIn {
	private static Logger _log = Logger.getLogger(LogClanDwarfIn.class.getName());

	public void storeLogClanDwarfIn(L1PcInstance pc, L1ItemInstance item, int item_count_before, int item_count_after, int item_in_count) {
		File file = new File("LogDB/ClanDwarfIn.txt");
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
				out = new DataOutputStream(new FileOutputStream("LogDB/ClanDwarfIn.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                              Ŭ��â��(in) �α��Դϴ�.                                  #\r\n".getBytes());
				out.write("#                                                            by ����                     #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#�ð�	������	����	�ɸ���id	�ɸ����̸�	Ŭ��id	Ŭ���̸�	������id	�������̸�	��������æ	�ֱ�������(�κ�)	�����İ���(�κ�)	��������(��-��)	�����۰���(����)	�����۰���(������)	��������(����-������)#\r\n".getBytes());
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
				slog = pc.getClanid() + "	"; // Ŭ��id
				out.write(slog.getBytes());
				slog = pc.getClanname() + "	"; // Ŭ���̸�
				out.write(slog.getBytes());
				slog = item.getId() + "	"; // ������id
				out.write(slog.getBytes());
				slog = item.getItem().getName() + "	"; // �������̸�
				out.write(slog.getBytes());
				slog = item.getEnchantLevel() + "	"; // ������ ��æ��
				out.write(slog.getBytes());
				slog = item_count_before + "	"; // ������ ����(��)
				out.write(slog.getBytes());
				slog = item_count_after + "	"; // ������ ����(��)
				out.write(slog.getBytes());
				int item_count_diff = item_count_before - item_count_after;
				if(item_count_diff < 0){
					item_count_diff = -item_count_diff;
				}
				slog = item_count_diff + "	"; // ������ ���� ��-��
				out.write(slog.getBytes());
				slog = item.getCount() + "	"; // ������ ����
				out.write(slog.getBytes());
				slog = item_in_count + "	"; // ������ ���� ������
				out.write(slog.getBytes());
				int count_diff = item_in_count - item_count_diff;
				slog = count_diff + "\r\n"; // ������ ���� ����
				out.write(slog.getBytes());
			} catch (Exception e) {
				_log.warning("ClanDwarfIn log oufutstream error:" + e);
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
				rfile = new RandomAccessFile("LogDB/ClanDwarfIn.txt", "rw");
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
				slog = pc.getClanid() + "	"; // Ŭ��id
				rfile.writeBytes(slog);
				slog = pc.getClanname() + "	"; // Ŭ���̸�
				rfile.writeBytes(encode(slog));
				slog = item.getId() + "	"; // ������id
				rfile.writeBytes(slog);
				slog = item.getItem().getName() + "	"; // �������̸�
				rfile.writeBytes(encode(slog));
				slog = item.getEnchantLevel() + "	"; // ������ ��æ��
				rfile.writeBytes(slog);
				slog = item_count_before + "	"; // ������ ����(��)
				rfile.writeBytes(slog);
				slog = item_count_after + "	"; // ������ ����(��)
				rfile.writeBytes(slog);
				int item_count_diff = item_count_before - item_count_after;
				if(item_count_diff < 0){
					item_count_diff = -item_count_diff;
				}
				slog = item_count_diff + "	"; // ������ ���� ��-��
				rfile.writeBytes(slog);
				slog = item.getCount() + "	"; // ������ ����
				rfile.writeBytes(slog);
				slog = item_in_count + "	"; // ������ ���� ������
				rfile.writeBytes(slog);
				int count_diff = item_in_count - item_count_diff;
				slog = count_diff + "\r\n"; // ������ ���� ����
				rfile.writeBytes(slog);
			} catch (Exception e) {
				_log.warning("ClanDwarfIn log randomacess error:" + e);
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
