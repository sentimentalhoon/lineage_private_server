package server.mina;

import l1j.server.Config;
import server.LineageClient;
//import net.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class LineagePacketDecoder implements ProtocolDecoder {
	
	@Override
	public synchronized void decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out){
		try {
			// 동기화 해야함
			LineageClient client = (LineageClient)session.getAttribute(LineageClient.CLIENT_KEY);
			//if(client!=null) client.encryptD(buffer);
			if(client!=null){
				int size = buffer.limit();
				if(size<1024){
					byte[] data = buffer.array();
					System.arraycopy(data, 0, client.PacketD, client.PacketIdx, size);
					client.PacketIdx += size;
				}else{
					// 사이즈 오바로 종료해버림.
					client.close();
				}
			}
		} catch (Exception e) {
			//Logger.getInstance().error(getClass().toString()+" decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out)\r\n"+e.toString(), Config.LOG.error);
		}
	}

	@Override
	public void dispose(IoSession client) throws Exception {
		
	}

	@Override
	public void finishDecode(IoSession client, ProtocolDecoderOutput output) throws Exception {
		
	}
}
