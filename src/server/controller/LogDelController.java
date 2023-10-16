package server.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import server.manager.eva;
import l1j.server.server.GeneralThreadPool;


public class LogDelController implements Runnable{
	private static Logger _log = Logger.getLogger(LogDelController.class.getName());
	private final int _runTime;
	
	public LogDelController(int runTime) {
		_runTime = runTime;
	}
	public void start() {
		GeneralThreadPool.getInstance().scheduleAtFixedRate(LogDelController.this, 0, _runTime);
	}

	@Override
	public void run(){
		try {
			eva.savelog();
			eva.LogDel();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}