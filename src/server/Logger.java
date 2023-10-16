/*
 * 2008. 8. 7 by psjump
 * - 서버 구동중 발생하는 다양한 에러들을 문자로 정리하여 이곳에 저장한다.
 * - 1분마다 저장된 정보를 파일에 쓴다.
 *  : 이미 파일이 존재할경우 파일끝 부분에 이어쓴다.
 */
package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import l1j.server.Config;

public class Logger extends TimerTask {
	public static Logger _instance;
	public static Logger getInstance(){
		if(_instance==null){
			_instance = new Logger();
			timer = new Timer(false); 
			timer.schedule(_instance, 0, 60 * 1000);
		}
		return _instance;
	}
	
	public Logger(){
		try {
			File f = new File("log");
			if(!f.isDirectory()) {
				f.mkdir();
			}
			
			f = new File("log/chat");
			if(!f.isDirectory()) {
				f.mkdir();
			}
			
			f = new File("log/error");
			if(!f.isDirectory()) {
				f.mkdir();
			}
			
			f = new File("log/system");
			if(!f.isDirectory()) {
				f.mkdir();
			}
			
			f = new File("log/badplayer");
			if(!f.isDirectory()) {
				f.mkdir();
			}
			
			f = new File("log/enchant");
			if(!f.isDirectory()) {
				f.mkdir();
			}
			
			f = new File("log/inventory");
			if(!f.isDirectory()){
				f.mkdir();
			}
			
			f = new File("log/system_time");
			if(!f.isDirectory()){
				f.mkdir();
			}
			
			_chating = new ArrayList<String>();
			_system = new ArrayList<String>();
			_error = new ArrayList<String>();
			_badplayer = new ArrayList<String>();
			_enchant = new ArrayList<String>();
			_inventory = new ArrayList<String>();
			_time = new ArrayList<String>();
		} catch (Exception e) {
			warn("server.log.Logger Logger()\r\n"+e, Config.LOG.system);
		}
		
	}
	
	private static Timer timer;
	private ArrayList<String> _chating;
	private ArrayList<String> _system;
	private ArrayList<String> _error;
	private ArrayList<String> _badplayer;
	private ArrayList<String> _enchant;
	private ArrayList<String> _inventory;
	private ArrayList<String> _time;
	
	@Override
	public void run(){
		String ymd = Config.YearMonthDate2();
		
		//--------------------- chat
		write(_chating, "log/chat/"+ymd+".log");
		
		//--------------------- error
		write(_error, "log/error/"+ymd+".log");
		
		//--------------------- system
		write(_system, "log/system/"+ymd+".log");
		
		//--------------------- badplayer
		write(_badplayer, "log/badplayer/"+ymd+".log");
		
		//--------------------- enchant
		write(_enchant, "log/enchant/"+ymd+".log");
		
		//--------------------- Inventory
		write(_inventory, "log/inventory/"+ymd+".log");
		
		//--------------------- System Time
		write(_time, "log/system_time/"+ymd+".log");
	}
	
	private void write(ArrayList<String> list, String file){
		try {	
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file)); 
			byte[] data = new byte[bis.available()]; 
			bis.read(data, 0, data.length);
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(data);
			for(String s : list) {
				bos.write(s.getBytes());
			}
			bos.flush();
			bos.close();
			bis.close();
			list.clear();
		} catch (Exception e) {
			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
				bos.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	
	public void info(String text, Config.LOG log){
		StringBuffer sb = new StringBuffer();
		sb.append("정보: ");
		sb.append(text);
		sb.append("\r\n");
		log(sb.toString(), log);
	}
	
	public void error(String text, Config.LOG log){
		StringBuffer sb = new StringBuffer();
		sb.append("심각: ");
		sb.append(text);
		sb.append("\r\n");
		log(sb.toString(), log);
		System.out.println(text);
	}
	
	public void warn(String text, Config.LOG log){
		StringBuffer sb = new StringBuffer();
		sb.append("경고: ");
		sb.append(text);
		sb.append("\r\n");
		log(sb.toString(), log);
	}
	
	public void badPalyer(String name, String text){
		StringBuffer sb = new StringBuffer();
		sb.append("--> ");
		sb.append(name);
		sb.append("\r\n");
		sb.append(text);
		sb.append("\r\n");
		info(sb.toString(), Config.LOG.badplayer);
	}
	
	public void en(String text){
		StringBuffer sb = new StringBuffer();
		sb.append(text);
		sb.append("\r\n");
		log(sb.toString(), Config.LOG.enchant);
	}
	
	public void addInventory(String text){
		StringBuffer sb = new StringBuffer();
		sb.append(text);
		sb.append("\r\n");
		log(sb.toString(), Config.LOG.inventory);
	}
	
	public void time(String text){
		StringBuffer sb = new StringBuffer();
		sb.append(text);
		sb.append("\r\n");
		log(sb.toString(), Config.LOG.time);
	}
	
	private void log(String text, Config.LOG log){
		switch(log){
			case system:
				_system.add(text);
				break;
			case chat:
				_chating.add(text);
				break;
			case error:
				_error.add(text);
				break;
			case badplayer:
				_badplayer.add(text);
				break;
			case enchant:
				_enchant.add(text);
				break;
			case inventory:
				_inventory.add(text);
				break;
			case time:
				_time.add(text);
				break;
		}
	}
}
