package server.threads.world;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import server.LineageClient;

public class LineageDecoderThread implements Runnable{
	
	public LineageDecoderThread(){
		_client = new CopyOnWriteArrayList<LineageClient>();
	}
	
	private List<LineageClient> _client;
	
	public void run(){
		while(true){
			try {
				for(LineageClient client : _client){
					if(client!=null){
						// ���� �����Ȱ� ����.
						if(!client.isConnected()){
							client.close();
							removeClient(client);
						}
						// ���ڴ�
						int length = PacketSize(client.PacketD);
						if(length!=0 && length<=client.PacketIdx){
							byte[] temp = new byte[length];
							System.arraycopy(client.PacketD, 0, temp, 0, length);
							System.arraycopy(client.PacketD, length, client.PacketD, 0, client.PacketIdx-length);
							client.PacketIdx -= length;
							client.encryptD(temp);
						}
				
					}else{
						removeClient(client);
					}
				}
				Thread.sleep(20);
			} catch (Exception e) {
				//Logger.getInstance().error(getClass().toString()+" run()\r\n"+e.toString(), Config.LOG.error);
			}
		}
	}
	
	// ��Ŷũ�� �� ����.
	private int PacketSize(byte[] data){
		int length = data[0] &0xff;
		length |= data[1] << 8 &0xff00;
		return length;
	}
	
	// Ŭ�� ���
	public void putClient(LineageClient c){
		try {
			if(!_client.contains(c)) {
				_client.add(c);
			}
		} catch (Exception e) {
			//Logger.getInstance().error(getClass().toString()+" putClient(LineageClient c)\r\n"+e.toString(), Config.LOG.error);
		}
	}
	
	// Ŭ�� ã��
	public LineageClient getClient(String id){
		if(id!=null){
			try {
				for(LineageClient c : _client){
					if(c!=null){
						if(c.getID()!=null && c.getID().equalsIgnoreCase(id)) {
							return c;
						}
					}else{
						removeClient(c);
					}
				}
			} catch (Exception e) {
				//Logger.getInstance().error(getClass().toString()+" getClient(String id)\r\n"+e.toString(), Config.LOG.error);
			}
		}
		return null;
	}
	
	// Ŭ�� ����
	public void removeClient(LineageClient c){
		_client.remove(c);
	}
	
	// Ŭ�� ����
	public int ClientCount(){
		return _client.size();
	}
	
	// Ŭ�� ��ϵǾ� �ִ��� üũ
	public boolean ContainsClient(LineageClient c){
		return _client.contains(c);
	}
	public List<LineageClient> getAllClient(){
		return this._client;
	}
}
