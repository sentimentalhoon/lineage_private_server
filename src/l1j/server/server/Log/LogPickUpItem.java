/*
 * 아이템 픽업 소스입니다. 
 * 이 소스는 그라운드에서 아이템을 픽업으로 먹었을 경우 작동합니다.
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

public class LogPickUpItem {
	private static Logger _log = Logger.getLogger(LogPickUpItem.class.getName());

	public void storeLogPinkUpItem(L1PcInstance pc, L1ItemInstance item, int before_inven, int after_inven, int before_ground, int after_ground, int pickupCount){
		File file = new File("LogDB/PickUpItem.txt");
		boolean fileex = file.exists();
		if(!fileex){
			File file2 = new File("LogDB/");
			file2.mkdirs();
			DataOutputStream out = null;
			String ditem = null;

			Date time1 = new Date(); // 현재시간
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try{
				out = new DataOutputStream(new FileOutputStream("LogDB/PickUpItem.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                              아이템 픽업 로그입니다.                                   #\r\n".getBytes());
				out.write("#                                                            by 가니                     #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#시간	아이피	계정	케릭터id	케릭터이름	아이템id	아이템이름	아이템인챈	픽업전인벤속갯수	픽업후인벤속갯수	픽업전그라운드갯수	픽업후그라운드갯수	픽업갯수#\r\n".getBytes());
				//out.write("\r\n\r\n".getBytes());
				ditem = fm + "	"; // 시간
				out.write(ditem.getBytes());
				ditem = pc.getNetConnection().getIp() + "	"; // 아이피
				out.write(ditem.getBytes());
				ditem = pc.getAccountName() + "	"; // 계정
				out.write(ditem.getBytes());
				ditem = pc.getId() + "	"; // pcid
				out.write(ditem.getBytes());
				ditem = pc.getName() + "	"; // 케릭이름
				out.write(ditem.getBytes());
				ditem = item.getId() + "	"; // 아이템id
				out.write(ditem.getBytes());
				ditem = item.getItem().getName() + "	"; // 아이템이름
				out.write(ditem.getBytes());
				ditem = item.getEnchantLevel() + "	"; // 아이템 인챈수
				out.write(ditem.getBytes());
				ditem = before_inven + "	"; // 픽업전인벤속갯수
				out.write(ditem.getBytes());
				ditem = after_inven + "	"; // 픽업후인벤속갯수
				out.write(ditem.getBytes());
				ditem = before_ground + "	"; // 픽업전그라운드갯수
				out.write(ditem.getBytes());
				ditem = after_ground + "	"; // 픽업후그라운드갯수
				out.write(ditem.getBytes());
				ditem = pickupCount + "\r\n"; // 픽업갯수
				out.write(ditem.getBytes());
			} catch (Exception e) {
				_log.warning("PickUpItem log oufutstream error:" + e);
				e.printStackTrace();
			} finally {
				try{
					out.close();
				}catch(Exception e1){}
			}
		}else{
			RandomAccessFile rfile = null;
			String ditem = null;

			Date time1 = new Date(); // 현재시간
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try {
				rfile = new RandomAccessFile("LogDB/PickUpItem.txt", "rw");
				rfile.seek(rfile.length()); //마지막위치 지정

				ditem = fm + "	"; // 시간
				rfile.writeBytes(ditem);
				ditem = pc.getNetConnection().getIp() + "	"; // 아이피
				rfile.writeBytes(ditem);
				ditem = pc.getAccountName() + "	"; // 계정
				rfile.writeBytes(ditem);
				ditem = pc.getId() + "	"; // pcid
				rfile.writeBytes(ditem);
				ditem = pc.getName() + "	"; // 케릭이름
				rfile.writeBytes(encode(ditem));
				ditem = item.getId() + "	"; // 아이템id
				rfile.writeBytes(ditem);
				ditem = item.getItem().getName() + "	"; // 아이템이름
				rfile.writeBytes(encode(ditem));
				ditem = item.getEnchantLevel() + "	"; // 아이템 인챈수
				rfile.writeBytes(ditem);
				ditem = before_inven + "	"; // 픽업전인벤속갯수
				rfile.writeBytes(ditem);
				ditem = after_inven + "	"; // 픽업후인벤속갯수
				rfile.writeBytes(ditem);
				ditem = before_ground + "	"; // 픽업전그라운드갯수
				rfile.writeBytes(ditem);
				ditem = after_ground + "	"; // 픽업후그라운드갯수
				rfile.writeBytes(ditem);
				ditem = pickupCount + "\r\n"; // 픽업갯수
				rfile.writeBytes(ditem);
			} catch (Exception e) {
				_log.warning("PickUpItem log randomacess error:" + e);
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
