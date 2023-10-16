package server.monitor;

// TODO ������ �巡�� �˸���
public class MonitorManager {
	private MessageMonitor msgMonitor; 
	
	public MonitorManager() {
		msgMonitor = new MessageMonitor();
	}
	
	public void register(Monitorable m) {
		m.registerMonitor(msgMonitor);
	}
	
	public void remove(Monitorable m) {
		m.removeMonitor(msgMonitor);
	}
}