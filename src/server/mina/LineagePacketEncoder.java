package server.mina;

import l1j.server.Config;
import server.LineageClient;
import l1j.server.server.serverpackets.ServerBasePacket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class LineagePacketEncoder implements ProtocolEncoder {
	
	public synchronized void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		try {
			// 동기화 해야함.
			if(message!=null){
				LineageClient client = (LineageClient)session.getAttribute(LineageClient.CLIENT_KEY);
				IoBuffer buffer = null;
				ServerBasePacket bp = (ServerBasePacket)message;
				if(bp!=null){
					if(client!=null){
						buffer = buffer(client.encryptE(bp.getBytes()), bp.getLength());
					}else{
						buffer = buffer(bp.getBytes(), bp.getLength());
					}
					out.write(buffer);
				}
			}
		} catch (Exception e) {
			//Logger.getInstance().error(getClass().toString()+" putClient(LineageClient c)\r\n"+e.toString(), Config.LOG.error);
		}
	}
	
	public void dispose(IoSession client) throws Exception {
		
	}
	
	private IoBuffer buffer(byte[] data, int length){
		byte[] size = new byte[2];
		size[0] |= length & 0xff;
		size[1] |= length >> 8 &0xff;
		IoBuffer buffer = IoBuffer.allocate(length, false);
		buffer.put(size);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
}
