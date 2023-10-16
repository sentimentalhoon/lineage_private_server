package l1j.server.server.clientpackets;

import l1j.server.server.datatables.LetterTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.L1Clan.ClanMember;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_LetterList;
import l1j.server.server.serverpackets.S_ReadLetter;
import l1j.server.server.serverpackets.S_RenewLetter;
import l1j.server.server.serverpackets.S_SkillSound;
import java.text.SimpleDateFormat;
import java.util.*;

import server.LineageClient;

public class C_MailBox  extends ClientBasePacket {
	private static final int TYPE_PRIVATE_MAIL 			= 0;  // ���� ����   
	private static final int TYPE_BLOODPLEDGE_MAIL 		= 1;  // ���� ����
	private static final int TYPE_KEPT_MAIL				= 2;  // ���� ����

	private static final int READ_PRIVATE_MAIL 			= 16; // ���� �����б�
	private static final int READ_BLOODPLEDGE_MAIL 		= 17; // ���� �����б�
	private static final int READ_KEPT_MAIL_			= 18; // ������ �����б�

	private static final int WRITE_PRIVATE_MAIL 		= 32; // ���� ��������
	private static final int WRITE_BLOODPLEDGE_MAIL 	= 33; // ���� ��������

	private static final int DEL_PRIVATE_MAIL 			= 48; // ���� ��������
	private static final int DEL_BLOODPLEDGE_MAIL 		= 49; // ���� ��������
	private static final int DEL_KEPT_MAIL 				= 50; // ������ ��������

	private static final int TO_KEEP_MAIL				= 64; // ���� �����ϱ� 

	private static final int PRICE_PRIVATEMAIL			= 50; 	// ���� ���� ����
	private static final int PRICE_BLOODPLEDGEMAIL		= 1000; // ���� ���� ����

	private static final int SIZE_PRIVATE_MAILBOX		= 20; 	// ���� ������ ũ��
	private static final int SIZE_BLOODPLEDGE_MAILBOX	= 50; 	// ���� ������ ũ��
	private static final int SIZE_KEPTMAIL_MAILBOX		= 10; 	// ���������� ũ��

	private static final String C_MailBox = "[C] C_MailBox";

	public C_MailBox(byte abyte0[], LineageClient client) {
		super(abyte0);
		int type = readC(); 

		L1PcInstance pc = client.getActiveChar();		
		switch(type){
		case TYPE_PRIVATE_MAIL: 	LetterList(pc,TYPE_PRIVATE_MAIL,SIZE_PRIVATE_MAILBOX);			break;
		case TYPE_BLOODPLEDGE_MAIL: LetterList(pc,TYPE_BLOODPLEDGE_MAIL,SIZE_BLOODPLEDGE_MAILBOX); 	break;
		case TYPE_KEPT_MAIL: 		LetterList(pc,TYPE_KEPT_MAIL,SIZE_KEPTMAIL_MAILBOX); 			break;
		case READ_PRIVATE_MAIL: 	ReadLetter(pc,READ_PRIVATE_MAIL, TYPE_PRIVATE_MAIL); 			break;
		case READ_BLOODPLEDGE_MAIL: ReadLetter(pc,READ_BLOODPLEDGE_MAIL,TYPE_BLOODPLEDGE_MAIL); 	break;
		case READ_KEPT_MAIL_: 		ReadLetter(pc,READ_KEPT_MAIL_,TYPE_KEPT_MAIL); 					break;
		case WRITE_PRIVATE_MAIL: 	WritePrivateMail(pc); 											break;
		case WRITE_BLOODPLEDGE_MAIL:WriteBloodPledgeMail(pc); 										break;
		case DEL_PRIVATE_MAIL: 		DeleteLetter(pc,DEL_PRIVATE_MAIL,TYPE_PRIVATE_MAIL); 			break;
		case DEL_BLOODPLEDGE_MAIL: 	DeleteLetter(pc,DEL_BLOODPLEDGE_MAIL,TYPE_BLOODPLEDGE_MAIL); 	break;
		case DEL_KEPT_MAIL: 		DeleteLetter(pc,DEL_KEPT_MAIL,TYPE_KEPT_MAIL); 					break;
		case TO_KEEP_MAIL: 			SaveLetter(pc,TO_KEEP_MAIL,TYPE_KEPT_MAIL);						break; 
		default:	
		}
	}

	private boolean payMailCost(final L1PcInstance RECEIVER, final int PRICE) {
		int AdenaCnt = RECEIVER.getInventory().countItems(L1ItemId.ADENA);
		if(AdenaCnt < PRICE) { 
			RECEIVER.sendPackets(new S_ServerMessage(189,""));
			return false;
		}

		RECEIVER.getInventory().consumeItem(L1ItemId.ADENA, PRICE);
		return true;
	}

	private void WritePrivateMail(L1PcInstance sender) {
		if(!payMailCost(sender, PRICE_PRIVATEMAIL))	return;

		int paper = readH(); //������
		SimpleDateFormat formatter = new SimpleDateFormat ( "yy/MM/dd", Locale.KOREA );
		String dTime = formatter.format(new Date());
		String receiverName = readS();
		String subject = readSS();
		String content = readSS();

		if(!checkCountMail(sender, receiverName, TYPE_PRIVATE_MAIL, SIZE_PRIVATE_MAILBOX)) return;

		L1PcInstance target = L1World.getInstance().getPlayer(receiverName);
		LetterTable.getInstance().writeLetter(paper, dTime, sender.getName(), receiverName, TYPE_PRIVATE_MAIL, subject, content);

		sendMessageToReceiver(target, sender, TYPE_PRIVATE_MAIL, SIZE_PRIVATE_MAILBOX);
	}

	private void WriteBloodPledgeMail(L1PcInstance sender) {

		if(!payMailCost(sender, PRICE_BLOODPLEDGEMAIL))	return;

		int paper = readH(); //������
		SimpleDateFormat formatter = new SimpleDateFormat ( "yy/MM/dd", Locale.KOREA );
		String dTime = formatter.format(new Date());
		String receiverName = readS();
		String subject = readSS();
		String content = readSS();

		L1Clan targetClan = null;
		for (L1Clan clan : L1World.getInstance().getAllClans()) {
			if (clan.getClanName().toLowerCase().equals(receiverName.toLowerCase())) {
				targetClan = clan;
				break;
			}
		}		
		String name;
		L1PcInstance target = null;
		ArrayList<ClanMember> clanMemberList = targetClan.getClanMemberList();		
		for (int i = 0,a = clanMemberList.size(); i < a ; i++) {			
			name = clanMemberList.get(i).name;			
			target = L1World.getInstance().getPlayer(name);			
			if(!checkCountMail(sender, name, TYPE_BLOODPLEDGE_MAIL, SIZE_BLOODPLEDGE_MAILBOX)) continue;			
			LetterTable.getInstance().writeLetter(paper, dTime, sender.getName(), name, TYPE_BLOODPLEDGE_MAIL, subject, content);			
			sendMessageToReceiver(target, sender, TYPE_BLOODPLEDGE_MAIL, SIZE_BLOODPLEDGE_MAILBOX);			
		}		
	}

	//������ �����ϱ����� �޼ҵ�
	private void DeleteLetter(L1PcInstance pc , int type, int letterType){
		int id = readD(); 
		LetterTable.getInstance().deleteLetter(id);
		pc.sendPackets(new S_RenewLetter(pc,type,id));   
	}
	//������ �б����� �޼ҵ�
	private void ReadLetter(L1PcInstance pc, int type, int letterType){
		int id = readD();
		LetterTable.getInstance().CheckLetter(id);
		pc.sendPackets(new S_ReadLetter(pc,type,letterType,id));
	}
	//��������Ʈ ��������� �޼ҵ�
	private void LetterList(L1PcInstance pc, int type, int count){
		pc.sendPackets(new S_LetterList(pc,type,count));
	}
	//������ �����ϱ� ���� �޼ҵ�
	private void SaveLetter(L1PcInstance pc, int type, int letterType){
		int id = readD(); 
		LetterTable.getInstance().SaveLetter(id,letterType);
		pc.sendPackets(new S_RenewLetter(pc,type,id));  
	}

	private boolean checkCountMail(L1PcInstance from, String to, int type, int max) {
		int cntMailInMailBox = LetterTable.getInstance().getLetterCount(to, type);		
		if(cntMailInMailBox >= max) { // ������ ����
			from.sendPackets(new S_ServerMessage(1261));
			return false;
		}		
		return true;
	}

	private void sendMessageToReceiver(L1PcInstance receiver, L1PcInstance sender, final int type, final int MAILBOX_SIZE) {
		if(receiver != null && receiver.getOnlineStatus() != 0){
			LetterList(receiver,type,MAILBOX_SIZE);
			receiver.sendPackets(new S_SkillSound(receiver.getId(), 1091));
			receiver.sendPackets(new S_ServerMessage(428)); // ������ �����߽��ϴ�.
			sender.sendPackets(new S_ServerMessage(1239)); //�����ڿ��� ������ ���½��ϴ�.
			sender.sendPackets(new S_LetterList(sender,type,MAILBOX_SIZE));
		}
	}

	@Override
	public String getType() {
		return C_MailBox;
	}
}
