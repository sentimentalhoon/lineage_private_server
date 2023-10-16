package l1j.server.server.serverpackets;

public class KeyPacket extends ServerBasePacket {
 private byte[] _byte = null;

 public KeyPacket() {
  byte[] _byte1 = {
    (byte) 0x37, // id
      (byte) 0x80, (byte) 0x64, (byte) 0x9c,
      (byte) 0x49, // key
      (byte) 0x89, (byte) 0x5d, (byte) 0x0c,
      (byte) 0x39, //
      (byte) 0x38, (byte) 0xed, (byte) 0xe7, (byte) 0x26,
      (byte) 0x22, (byte) 0xda, (byte) 0xc8 };
  /*  (byte) 0x2a, // id
    (byte) 0x59, (byte) 0xc1, (byte) 0xc4,
    (byte) 0x7b, // key
    (byte) 0x7d, (byte) 0x55, (byte) 0x37,
    (byte) 0x1d, //
    (byte) 0xf1, (byte) 0x7f, (byte) 0x0d, (byte) 0x00,
    (byte) 0x3d, (byte) 0x3d, (byte) 0x00 };*/
  for (int i = 0; i < _byte1.length; i++) {
   writeC(_byte1[i]);
  }

 }

 @Override
 public byte[] getContent() {
  if (_byte == null) {
   _byte = getBytes();
  }
  return _byte;
 }
}
