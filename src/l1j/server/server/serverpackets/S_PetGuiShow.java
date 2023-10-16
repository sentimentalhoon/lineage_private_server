  package l1j.server.server.serverpackets;

  import l1j.server.server.Opcodes;


  public class S_PetGuiShow extends ServerBasePacket {

  	private static final String S_PetMenuSabu = "[S] S_PetMenuSabu";
  	private byte[] _byte = null;

  	public S_PetGuiShow(Boolean show) {
  		buildPacket(show);
  	}
  	private void buildPacket(Boolean show) {
  		writeC(Opcodes.S_OPCODE_RETURNEDSTAT);                 
  	  writeC(0x0c);    
  	  if (show == true){
  		writeC(03); // 00:OFF or Die  03:Pet                
  	  }else{
  		writeC(00); // 00:OFF or Die  03:Pet                
  	  }
  	  writeC(0x00);  
  	  writeC(0x00);//일단 보류
  	  writeC(0x00);
  	  writeC(0x00);
  	  writeC(0x00);               
  	  writeD(0x00000000);                  
  	  writeD(0x00000000); 

  
  	}

  	@Override
  	public byte[] getContent() {
  		if (_byte == null) {
  			_byte = getBytes();
  		}
  		return _byte;
  	}
  	@Override
  	public String getType() {
  		return S_PetMenuSabu;
  	}
  }
