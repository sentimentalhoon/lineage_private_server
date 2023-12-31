/*
 * 일반 상점(buy) 소스입니다. 
 * 이 소스는 상점에 물건을 살 경우..즉 buy창을 사용했을때 작동합니다.
 * 항목에 보이는 순서대로 저장을 하게 됩니다.
 * 엑셀에 호환되도록 소스를 추출하며, 엑셀에서 보시면 조금 편합니다,
 * 엑셀이 없는분은 구글 문서편집이나, 오픈오피스를 사용하시는걸 추천드립니다.
 * 만약 소스 자체를 개선해야 할 점이나, 추가해야할 의견이 있으시면,
 * wi연구실 - 가니 를 찾아주세요.
 * by 린프리덤 - 가니
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

public class LogShopBuy {
	private static Logger _log = Logger.getLogger(LogShopBuy.class.getName());

	public void storeLogShopBuy(L1PcInstance pc, L1ItemInstance item, int cnt, int adenabefore, int adenaafter, int itemprice) {
		File file = new File("LogDB/ShopBuy.txt");
		boolean fileex = file.exists();
		if(!fileex){
			File file2 = new File("LogDB/");
			file2.mkdirs();
			DataOutputStream out = null;
			String slog = null;

			Date time1 = new Date(); // 현재시간
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try{
				out = new DataOutputStream(new FileOutputStream("LogDB/ShopBuy.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                              일반상점(buy) 로그입니다.                                 #\r\n".getBytes());
				out.write("#                                                            by 가니                     #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#시간	아이피	계정	케릭터id	케릭터이름	아이템id	아이템이름	아이템인챈 아이템갯수 아데나갯수(전)	아데나갯수(후)	아데나갯수(차이)	산가격#\r\n".getBytes());
				//out.write("\r\n\r\n".getBytes());
				slog = fm + "	"; // 시간
				out.write(slog.getBytes());
				slog = pc.getNetConnection().getIp() + "	"; // 아이피
				out.write(slog.getBytes());
				slog = pc.getAccountName() + "	"; // 계정
				out.write(slog.getBytes());
				slog = pc.getId() + "	"; // pcid
				out.write(slog.getBytes());
				slog = pc.getName() + "	"; // 케릭이름
				out.write(slog.getBytes());
				slog = item.getId() + "	"; // 아이템id
				out.write(slog.getBytes());
				slog = item.getItem().getName() + "	"; // 아이템이름
				out.write(slog.getBytes());
				slog = item.getEnchantLevel() + "	"; // 아이템 인챈수
				out.write(slog.getBytes());
				slog = cnt + "	"; // 아이템 갯수
				out.write(slog.getBytes());
				slog = adenabefore + "	"; // 아데나 갯수(전)
				out.write(slog.getBytes());
				slog = adenaafter + "	"; // 아데나 갯수(후)
				out.write(slog.getBytes());
				int adenadiff = adenabefore - adenaafter;
				if(adenadiff < 0){
					adenadiff = -adenadiff;
				}
				slog = adenadiff + "	"; // 아데나 갯수 후-전
				out.write(slog.getBytes());
				slog = itemprice + "\r\n"; // 아이템 가격
				out.write(slog.getBytes());
			} catch (Exception e) {
				_log.warning("ShopBuy log oufutstream error:" + e);
				e.printStackTrace();
			} finally {
				try{
					out.close();
				}catch(Exception e1){}
			}
		}else{
			RandomAccessFile rfile = null;
			String slog = null;

			Date time1 = new Date(); // 현재시간
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try {
				rfile = new RandomAccessFile("LogDB/ShopBuy.txt", "rw");
				rfile.seek(rfile.length()); //마지막위치 지정

				slog = fm + "	"; // 시간
				rfile.writeBytes(slog);
				slog = pc.getNetConnection().getIp() + "	"; // 아이피
				rfile.writeBytes(slog);
				slog = pc.getAccountName() + "	"; // 계정
				rfile.writeBytes(slog);
				slog = pc.getId() + "	"; // pcid
				rfile.writeBytes(slog);
				slog = pc.getName() + "	"; // 케릭이름
				rfile.writeBytes(encode(slog));
				slog = item.getId() + "	"; // 아이템id
				rfile.writeBytes(slog);
				slog = item.getItem().getName() + "	"; // 아이템이름
				rfile.writeBytes(encode(slog));
				slog = item.getEnchantLevel() + "	"; // 아이템 인챈수
				rfile.writeBytes(slog);
				slog = cnt + "	"; // 아이템 갯수
				rfile.writeBytes(slog);
				slog = adenabefore + "	"; // 아데나 갯수(전)
				rfile.writeBytes(slog);
				slog = adenaafter + "	"; // 아데나 갯수(후)
				rfile.writeBytes(slog);
				int adenadiff = adenabefore - adenaafter;
				if(adenadiff < 0){
					adenadiff = -adenadiff;
				}
				slog = adenadiff + "	"; // 아데나 갯수 후-전
				rfile.writeBytes(slog);
				slog = itemprice + "\r\n"; // 아이템 가격
				rfile.writeBytes(slog);
			} catch (Exception e) {
				_log.warning("ShopBuy log randomacess error:" + e);
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
