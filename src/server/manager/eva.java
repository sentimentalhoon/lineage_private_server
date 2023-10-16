package server.manager;

import static l1j.server.server.model.skill.L1SkillId.ADVANCE_SPIRIT;
import static l1j.server.server.model.skill.L1SkillId.BLESS_WEAPON;
import static l1j.server.server.model.skill.L1SkillId.BRAVE_AURA;
import static l1j.server.server.model.skill.L1SkillId.FEATHER_BUFF_A;
import static l1j.server.server.model.skill.L1SkillId.IRON_SKIN;
import static l1j.server.server.model.skill.L1SkillId.NATURES_TOUCH;
import static l1j.server.server.model.skill.L1SkillId.PHYSICAL_ENCHANT_DEX;
import static l1j.server.server.model.skill.L1SkillId.PHYSICAL_ENCHANT_STR;
import static l1j.server.server.model.skill.L1SkillId.SHINING_AURA;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.sun.management.OperatingSystemMXBean;

import l1j.server.server.utils.SQLUtil;
import l1j.server.server.utils.SystemUtil;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.GameServerSetting;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.Base64;
import l1j.server.Config;
import l1j.server.ServerChat;
import l1j.server.SpecialEventHandler;
import l1j.server.server.datatables.DropItemTable;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.MapFixKeyTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.ResolventTable;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1TreasureBox;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Skills;
import server.GameServer;
import server.Server;
import server.system.autoshop.AutoShopManager;

/**
 * 
 * @author code
 */

@SuppressWarnings("serial")
public class eva extends JFrame {

	private static final String[] event = {
		"�Ű���", "����������", "���ν���", "��������", "������̺�",
		"���Ǿ����ε�","�����ε�","�����۸��ε�","���Ÿ��ε�","���ظ��ε�","Ʈ�����ڽ�","��񸮷ε�",
		"server_text_clear","enchant_text_clear","system_text_clear","observe_text_clear","trade_text_clear","returnstatus_text_clear","gmcommands_text_clear"
		,"warehouse_text_clear","newaccount_text_clear","bug_text_clear","boss_text_clear","chat_text_clear","UserInfoWindow","LetterWindow","ServerSettingWindow"
	};

	public static ServerLogWindow jSystemLogWindow = null;      // �ý���
	public static JFrame jJFrame = null;
	public static int width = 0;
	public static int height = 0;
	public static boolean isServerStarted = true;
	public static final String NoServerStartMSG = "������ ������� �ʾҽ��ϴ�.";
	public static final String blankSetUser = "������ �������� �ʾҽ��ϴ�.";
	public static final String NoConnectUser = " ĳ���ʹ� ������ ���� �ʽ��ϴ�.";
	public static JDesktopPane jJDesktopPane = new JDesktopPane();
	// ��������
	public static ServerUserInfoWindow jServerUserInfoWindow = null;
	// ��������
	public static ServerSettingWindow jServerSettingWindow = null;
	public static ServerLogWindow jWareHouseLogWindow = null;   // â��
	public static ServerLogWindow jTradeLogWindow = null;       // �ŷ�
	public static ServerLogWindow jEnchantLogWindow = null;     // ��æ
	public static ServerLogWindow jObserveLogWindow = null;     // ����
	public static ServerLogWindow jBugLogWindow = null;         // ����
	public static ServerLogWindow jCommandLogWindow = null;     // ���
	public static ServerChatLogWindow jServerChatLogWindow = null;     // ����ä�� �����
	public static ServerLatterLogWindow jServerLatterLogWindow = null; // ���� �����
	public static ServerUserMoniterWindow jServerUserMoniterWindow = null; // ������ �����
	public static void errorMsg(String s) { // Message Window(ERROR)
		JOptionPane.showMessageDialog(null, s, "Server Message", JOptionPane.ERROR_MESSAGE);
	}

	//private static final Object lock = new Object();
	public static final Object lock = new Object();
	public static String date = "";
	public static String time = "";
	private int[] letter_idx = null;
	private evaSet evaset;
	private Letter letter;
	private ArrayList<String> tmpArray;
	private ArrayList<Integer> tmpArray2;
	private boolean s1;
	private boolean s2;
	private boolean s3;
	private boolean s4;
	private boolean s5;
	private boolean s6;
	private boolean s7;
	private boolean s8;
	private boolean s9;
	private boolean s10;
	private boolean s11;
	private boolean s12;
	private boolean s13;
	private boolean s14;
	private boolean s15;
	private int chatType;
	private JDialog C_Account_Dia;
	private JDialog Gift_Dia;
	private JDialog Move_Dia;
	private JDialog Poly_Dia;
	private JDialog Setting_Dia;
	private JButton all_buff_btn;
	private JButton etc_btn;
	private JPanel bottom;
	private JTextField c_ac_id;
	private JButton c_ac_ok;
	private JTextField c_ac_pass;
	private JToggleButton c_btn_g;
	private JButton char_ban_btn;
	private JButton char_no_chat_btn;
	private JTextField chat_msg;
	private JTextField chat_target;
	private static JTextPane chat_text;
	private JComboBox chat_type;
	private JButton chatmonitor_btn;
	private JInternalFrame chatmonitor_frame;
	private JToggleButton cp_btn_g;
	private JButton del_account_btn;
	public static JDesktopPane desktop;
	private JButton event_btn;
	private JInternalFrame event_frame;
	private JToggleButton g_btn_g;
	private JButton gift_btn;
	private JTextField gift_charName;
	private JTextField gift_itemCount;
	private JTextField gift_itemEnlv;
	private JTextField gift_itemId;
	private JComboBox gift_itemType;
	private JButton gift_ok;
	private JButton gift_search1;
	private JButton gift_search2;
	private JButton leteer_view_btn;
	private JLabel jLabel1;
	private JLabel jLabel10;
	private JLabel jLabel11;
	private JLabel jLabel12;
	private JLabel jLabel13;
	private JLabel jLabel14;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JLabel jLabel8;
	private JLabel jLabel9;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JScrollPane jScrollPane3;
	private JScrollPane jScrollPane4;
	private JScrollPane jScrollPane5;
	private JScrollPane jScrollPane6;
	private JScrollPane jScrollPane7;
	private JScrollPane jScrollPane8;
	private JScrollPane jScrollPane9;
	private JScrollPane jScrollPane10;
	private JScrollPane jScrollPane11;
	private JScrollPane jScrollPane12;
	private JScrollPane jScrollPane13;
	private static JProgressBar load_pro;
	public static JLabel logo;
	private JLabel memory;
	private static JLabel memory_label;
	private static JProgressBar memory_pro;
	private JButton move_btn;
	private JTextField move_charName;
	private JTextField move_map;
	private JButton move_ok;
	private JButton move_search;
	private JTextField move_x;
	private JTextField move_y;
	private JToggleButton n_btn_g;
	private JButton new_account_btn;
	private JButton no_chat_btn;
	private JToggleButton p_btn_g;
	private JButton poly_btn;
	private JTextField poly_charName;
	private JTextField poly_id;
	private JButton poly_ok;
	private JButton poly_search;
	private JToggleButton s_btn_g;
	private static JTextPane server_text;
	private static JTextPane enchant_text;
	private static JTextPane system_text;
	private static JTextPane observe_text;
	private static JTextPane trade_text;
	private static JTextPane returnstatus_text;
	private static JTextPane gmcommands_text;
	private static JTextPane warehouse_text;
	private static JTextPane newaccount_text;
	private static JTextPane bug_text;
	private static JTextPane boss_text;
	private JInternalFrame servermonitor_frame;
	private JInternalFrame enchantmonitor_frame;
	private JInternalFrame systemmonitor_frame;
	private JInternalFrame observemonitor_frame;
	private JInternalFrame trademonitor_frame;
	private JInternalFrame newaccountmonitor_frame;
	private JInternalFrame bugmonitor_frame;
	private JInternalFrame bossmonitor_frame;
	private JInternalFrame returnstatusmonitor_frame;
	private JInternalFrame gmcommandsmonitor_frame;
	private JInternalFrame warehousemonitor_frame;
	private JButton serverclose_btn;
	private JButton servermonitor_btn;
	private JButton enchantmonitor_btn;
	private JButton systemmonitor_btn;
	private JButton observemonitor_btn;
	private JButton trademonitor_btn;
	private JButton serversave_btn;
	private JButton serversetting_btn;
	private JButton serverstart_btn;
	private JButton newaccountmonitor_btn;
	private JButton bugmonitor_btn;
	private JButton bossmonitor_btn;
	private JButton returnstatusmonitor_btn;
	private JButton gmcommandsmonitor_btn;
	private JButton warehousemonitor_btn;
	private JPanel top_pane;
	private JLabel user;
	private static JLabel user_label;
	private static JList user_list;
	private JInternalFrame userlist_frame;
	private JButton usrlist_btn;
	private JToggleButton w_btn_g;
	private boolean isServer;
	private JMenuItem jMenuItem1;
	private JMenuItem jMenuItem2;
	private JMenuItem jMenuItem3;
	private JMenuItem jMenuItem4;
	private JMenuItem jMenuItem5;
	private JMenuItem jMenuItem6;
	private JPopupMenu jPopupMenu1;
	private JInternalFrame letterview_frame;
	private JTextField letter_charName;
	private JLabel letter_charName_label;
	private JButton letter_load;
	private JPanel letter_pane;
	private JScrollPane letter_scoll;
	private JButton letter_search;
	private JTable letter_table;
	private JRadioButton report_btn15d;
	private JRadioButton report_btn1m;
	private JRadioButton report_btn2m;
	private JRadioButton report_btn3m;
	private JRadioButton report_btn4m;
	private JRadioButton report_btnchar;
	private ButtonGroup report_btng;
	private JPanel report_pane1;
	private JPanel report_pane2;
	private JScrollPane report_scoll;
	private static JTable report_table;
	private static JInternalFrame report_frame;

	public eva() {
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){}
		initComponents();
		updataMemory();
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {

		evaset = new evaSet(this, true);
		letter = new Letter(this, true);
		tmpArray = new ArrayList<String>();
		tmpArray2 = new ArrayList<Integer>();

		jPopupMenu1 = new JPopupMenu();
		jMenuItem1 = new JMenuItem();
		jMenuItem2 = new JMenuItem();
		jMenuItem3 = new JMenuItem();
		jMenuItem4 = new JMenuItem();
		jMenuItem5 = new JMenuItem();
		jMenuItem6 = new JMenuItem();
		Gift_Dia = new JDialog();
		Setting_Dia = new JDialog();
		jPanel1 = new JPanel();
		jLabel1 = new JLabel();
		jLabel2 = new JLabel();
		jLabel3 = new JLabel();
		jLabel4 = new JLabel();
		jLabel5 = new JLabel();
		gift_charName = new JTextField();
		gift_itemType = new JComboBox();
		gift_itemId = new JTextField();
		gift_itemCount = new JTextField();
		gift_itemEnlv = new JTextField();
		gift_ok = new JButton();
		gift_search1 = new JButton();
		gift_search2 = new JButton();
		Poly_Dia = new JDialog();
		jLabel6 = new JLabel();
		poly_charName = new JTextField();
		jLabel7 = new JLabel();
		poly_id = new JTextField();
		poly_search = new JButton();
		poly_ok = new JButton();
		Move_Dia = new JDialog();
		move_search = new JButton();
		move_charName = new JTextField();
		jLabel8 = new JLabel();
		jLabel9 = new JLabel();
		move_x = new JTextField();
		jLabel10 = new JLabel();
		move_y = new JTextField();
		jLabel11 = new JLabel();
		move_map = new JTextField();
		move_ok = new JButton();
		C_Account_Dia = new JDialog();
		jLabel12 = new JLabel();
		c_ac_id = new JTextField();
		jLabel13 = new JLabel();
		c_ac_pass = new JTextField();
		c_ac_ok = new JButton();
		top_pane = new JPanel();
		chatmonitor_btn = new JButton();
		servermonitor_btn = new JButton();
		enchantmonitor_btn = new JButton();
		systemmonitor_btn = new JButton();
		observemonitor_btn = new JButton();
		trademonitor_btn = new JButton();
		serverstart_btn = new JButton();
		serverclose_btn = new JButton();
		serversave_btn = new JButton();
		event_btn = new JButton();
		usrlist_btn = new JButton();
		newaccountmonitor_btn = new JButton();
		bugmonitor_btn = new JButton();
		bossmonitor_btn = new JButton();
		returnstatusmonitor_btn = new JButton();
		gmcommandsmonitor_btn = new JButton();
		warehousemonitor_btn = new JButton();
		load_pro = new JProgressBar();
		memory_pro = new JProgressBar();
		leteer_view_btn = new JButton();
		desktop = new JDesktopPane();
		servermonitor_frame = new JInternalFrame();
		enchantmonitor_frame = new JInternalFrame();
		systemmonitor_frame = new JInternalFrame();
		observemonitor_frame = new JInternalFrame();
		trademonitor_frame = new JInternalFrame();
		letterview_frame = new JInternalFrame();
		report_frame = new JInternalFrame();
		newaccountmonitor_frame = new JInternalFrame();
		bugmonitor_frame = new JInternalFrame();
		bossmonitor_frame = new JInternalFrame();
		returnstatusmonitor_frame = new JInternalFrame();
		gmcommandsmonitor_frame = new JInternalFrame();
		warehousemonitor_frame = new JInternalFrame();
		jScrollPane2 = new JScrollPane();
		jScrollPane4 = new JScrollPane();
		jScrollPane5 = new JScrollPane();
		jScrollPane6 = new JScrollPane();
		jScrollPane7 = new JScrollPane();
		jScrollPane8 = new JScrollPane();
		jScrollPane9 = new JScrollPane();
		jScrollPane10 = new JScrollPane();
		jScrollPane11 = new JScrollPane();
		jScrollPane12 = new JScrollPane();
		jScrollPane13 = new JScrollPane();
		server_text = new JTextPane();
		enchant_text = new JTextPane();
		system_text = new JTextPane();
		observe_text = new JTextPane();
		trade_text = new JTextPane();
		newaccount_text = new JTextPane();
		bug_text = new JTextPane();
		boss_text = new JTextPane();
		returnstatus_text = new JTextPane();
		gmcommands_text = new JTextPane();
		warehouse_text = new JTextPane();
		chatmonitor_frame = new JInternalFrame();
		jScrollPane1 = new JScrollPane();
		chat_text = new JTextPane();
		n_btn_g = new JToggleButton();
		w_btn_g = new JToggleButton();
		g_btn_g = new JToggleButton();
		c_btn_g = new JToggleButton();
		p_btn_g = new JToggleButton();
		s_btn_g = new JToggleButton();
		cp_btn_g = new JToggleButton();
		jPanel2 = new JPanel();
		chat_target = new JTextField();
		chat_msg = new JTextField();
		chat_type = new JComboBox();
		event_frame = new JInternalFrame();
		all_buff_btn = new JButton();
		gift_btn = new JButton();
		poly_btn = new JButton();
		move_btn = new JButton();
		no_chat_btn = new JButton();
		new_account_btn = new JButton();
		del_account_btn = new JButton();
		etc_btn = new JButton();
		char_ban_btn = new JButton();
		char_no_chat_btn = new JButton();
		serversetting_btn = new JButton();
		userlist_frame = new JInternalFrame();
		jScrollPane3 = new JScrollPane();
		user_list = new JList();
		logo = new JLabel();
		jLabel14 = new JLabel();
		bottom = new JPanel();
		user = new JLabel();
		user_label = new JLabel();
		memory = new JLabel();
		memory_label = new JLabel();
		letter_charName_label = new JLabel();
		letter_charName = new JTextField();
		letter_load = new JButton();
		letter_search = new JButton();
		letter_pane = new JPanel();
		letter_scoll = new JScrollPane();
		letter_table = new JTable();
		report_btng = new ButtonGroup();
		report_pane1 = new JPanel();
		report_btn15d = new JRadioButton();
		report_btn1m = new JRadioButton();
		report_btn2m = new JRadioButton();
		report_btn3m = new JRadioButton();
		report_btn4m = new JRadioButton();
		report_btnchar = new JRadioButton();
		report_pane2 = new JPanel();
		report_scoll = new JScrollPane();
		report_table = new JTable();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		jPopupMenu1.setInvoker(this);

		jMenuItem1.setText("����");
		jMenuItem1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jMenuItem1ActionPerformed(evt);
			}
		});  
		jPopupMenu1.add(jMenuItem1);

		jMenuItem2.setText("�ù���");
		jMenuItem2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jMenuItem2ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem2);

		jMenuItem3.setText("ä��");
		jMenuItem3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jMenuItem3ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem3);

		jMenuItem4.setText("�߹�");
		jMenuItem4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jMenuItem4ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem4);

		jMenuItem5.setText("��������");
		jMenuItem5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jMenuItem5ActionPerformed(evt);
			}
		});

		jPopupMenu1.add(jMenuItem5);

		jMenuItem6.setText("�����ֱ�");
		jMenuItem6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jMenuItem6ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem6);

		jPopupMenu1.getAccessibleContext().setAccessibleParent(this);

		Gift_Dia.setTitle("�����ֱ�");
		Gift_Dia.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		Gift_Dia.setLocationByPlatform(true);
		Gift_Dia.setModal(true);
		Gift_Dia.setResizable(false);

		jLabel1.setLabelFor(gift_charName);
		jLabel1.setText("ĳ����");

		jLabel2.setLabelFor(gift_itemType);
		jLabel2.setText("������Ÿ��");

		jLabel3.setLabelFor(gift_itemId);
		jLabel3.setText("�����۹�ȣ");

		jLabel4.setLabelFor(gift_itemCount);
		jLabel4.setText("�����۰���");

		jLabel5.setLabelFor(gift_itemEnlv);
		jLabel5.setText("��æƮ����");

		gift_itemType.setModel(new DefaultComboBoxModel(new String[] { "����", "��", "��Ÿ" }));

		gift_ok.setText("�����ֱ�");
		gift_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				gift_okActionPerformed(evt);
			}
		});

		gift_search1.setText("�˻�");
		gift_search1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				gift_search1ActionPerformed(evt);
			}
		});

		gift_search2.setText("�˻�");
		gift_search2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				gift_search2ActionPerformed(evt);
			}
		});

		user_list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				jList1MouseClicked(evt);
			}
		});

		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(gift_ok)
								.addGroup(jPanel1Layout.createSequentialGroup()
										.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addGroup(jPanel1Layout.createSequentialGroup()
														.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
																.addComponent(jLabel2)
																.addComponent(jLabel3)
																.addComponent(jLabel4)
																.addComponent(jLabel5))
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
																		.addComponent(gift_itemEnlv, GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
																		.addComponent(gift_itemId, GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
																		.addComponent(gift_itemType, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(gift_itemCount, GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)))
																		.addGroup(jPanel1Layout.createSequentialGroup()
																				.addComponent(jLabel1)
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																				.addComponent(gift_charName, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)))
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																				.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
																						.addComponent(gift_search1)
																						.addComponent(gift_search2))))
																						.addContainerGap())
				);
		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(jPanel1Layout.createSequentialGroup()
										.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel1)
												.addComponent(gift_charName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel2)
														.addComponent(gift_itemType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
														.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																.addComponent(jLabel3)
																.addComponent(gift_itemId, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addComponent(gift_search2))
																.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel4)
																		.addComponent(gift_itemCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																		.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																				.addComponent(jLabel5)
																				.addComponent(gift_itemEnlv, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
																				.addComponent(gift_search1))
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																				.addComponent(gift_ok)
																				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		GroupLayout Gift_DiaLayout = new GroupLayout(Gift_Dia.getContentPane());
		Gift_Dia.getContentPane().setLayout(Gift_DiaLayout);
		Gift_DiaLayout.setHorizontalGroup(
				Gift_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				);
		Gift_DiaLayout.setVerticalGroup(
				Gift_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				);

		Gift_Dia.getAccessibleContext().setAccessibleParent(this);

		Poly_Dia.setTitle("ĳ������");
		Poly_Dia.setLocationByPlatform(true);
		Poly_Dia.setModal(true);
		Poly_Dia.setResizable(false);

		jLabel6.setText("ĳ����");

		jLabel7.setText("���Ź�ȣ");

		poly_search.setText("�˻�");
		poly_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				poly_searchActionPerformed(evt);
			}
		});

		poly_ok.setText("����");
		poly_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				poly_okActionPerformed(evt);
			}
		});

		GroupLayout Poly_DiaLayout = new GroupLayout(Poly_Dia.getContentPane());
		Poly_Dia.getContentPane().setLayout(Poly_DiaLayout);
		Poly_DiaLayout.setHorizontalGroup(
				Poly_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(Poly_DiaLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(Poly_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addGroup(Poly_DiaLayout.createSequentialGroup()
										.addComponent(jLabel7)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(poly_id))
										.addGroup(Poly_DiaLayout.createSequentialGroup()
												.addComponent(jLabel6)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(poly_charName, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(poly_search)
												.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addGroup(GroupLayout.Alignment.TRAILING, Poly_DiaLayout.createSequentialGroup()
														.addContainerGap(131, Short.MAX_VALUE)
														.addComponent(poly_ok)
														.addContainerGap())
				);
		Poly_DiaLayout.setVerticalGroup(
				Poly_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(Poly_DiaLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(Poly_DiaLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel6)
								.addComponent(poly_charName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(poly_search))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(Poly_DiaLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel7)
										.addComponent(poly_id, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(poly_ok)
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		Poly_Dia.getAccessibleContext().setAccessibleParent(this);

		Move_Dia.setTitle("ĳ���̵�");
		Move_Dia.setLocationByPlatform(true);
		Move_Dia.setModal(true);
		Move_Dia.setResizable(false);

		move_search.setText("�˻�");
		move_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				move_searchActionPerformed(evt);
			}
		});

		jLabel8.setText("ĳ����");

		jLabel9.setText("��ǥX");

		jLabel10.setText("��ǥY");

		jLabel11.setText("�ʾ��̵�");

		move_ok.setText("�̵�");
		move_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				move_okActionPerformed(evt);
			}
		});

		GroupLayout Move_DiaLayout = new GroupLayout(Move_Dia.getContentPane());
		Move_Dia.getContentPane().setLayout(Move_DiaLayout);
		Move_DiaLayout.setHorizontalGroup(
				Move_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(Move_DiaLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(Move_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(Move_DiaLayout.createSequentialGroup()
										.addComponent(jLabel8)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(move_charName, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(move_search))
										.addGroup(Move_DiaLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
												.addComponent(move_ok)
												.addGroup(Move_DiaLayout.createSequentialGroup()
														.addComponent(jLabel9)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(move_x, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jLabel10)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(move_y, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jLabel11)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(move_map, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE))))
														.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		Move_DiaLayout.setVerticalGroup(
				Move_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(Move_DiaLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(Move_DiaLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel8)
								.addComponent(move_charName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(move_search))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(Move_DiaLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel9)
										.addComponent(move_x, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel10)
										.addComponent(move_y, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel11)
										.addComponent(move_map, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(move_ok)
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		Move_Dia.getAccessibleContext().setAccessibleParent(this);

		C_Account_Dia.setTitle("��������");
		C_Account_Dia.setLocationByPlatform(true);
		C_Account_Dia.setModal(true);
		C_Account_Dia.setResizable(false);

		jLabel12.setText("������");

		jLabel13.setText("��й�ȣ");

		c_ac_ok.setText("����");
		c_ac_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				c_ac_okActionPerformed(evt);
			}
		});

		GroupLayout C_Account_DiaLayout = new GroupLayout(C_Account_Dia.getContentPane());
		C_Account_Dia.getContentPane().setLayout(C_Account_DiaLayout);
		C_Account_DiaLayout.setHorizontalGroup(
				C_Account_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(C_Account_DiaLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jLabel12)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(c_ac_id, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jLabel13)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(c_ac_pass, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(c_ac_ok)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		C_Account_DiaLayout.setVerticalGroup(
				C_Account_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(C_Account_DiaLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(C_Account_DiaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(C_Account_DiaLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel12)
										.addComponent(c_ac_id, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel13)
										.addComponent(c_ac_pass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addComponent(c_ac_ok))
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		C_Account_Dia.getAccessibleContext().setAccessibleParent(this);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle(Config.servername + "����");

		chatmonitor_btn.setText("Chat");
		chatmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				chatmonitor_btnActionPerformed(evt);
			}
		});

		servermonitor_btn.setText("Server");
		servermonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				servermonitor_btnActionPerformed(evt);
			}
		});

		enchantmonitor_btn.setText("Enchant");
		enchantmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				enchantmonitor_btnActionPerformed(evt);
			}
		});

		systemmonitor_btn.setText("System");
		systemmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				systemmonitor_btnActionPerformed(evt);
			}
		});

		observemonitor_btn.setText("Observe");
		observemonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				observemonitor_btnActionPerformed(evt);
			}
		});

		trademonitor_btn.setText("Trade");
		trademonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				trademonitor_btnActionPerformed(evt);
			}
		});

		serverstart_btn.setText("Start");
		serverstart_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				serverstart_btnActionPerformed(evt);
			}
		});

		serverclose_btn.setText("End");
		serverclose_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				serverclose_btnActionPerformed(evt);
			}
		});

		serversave_btn.setText("Save");
		serversave_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				serversave_btnActionPerformed(evt);
			}
		});

		event_btn.setText("Event");
		event_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				event_btnActionPerformed(evt);
			}
		});

		usrlist_btn.setText("UserList");
		usrlist_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				usrlist_btnActionPerformed(evt);
			}
		});

		load_pro.setStringPainted(true);

		memory_pro.setStringPainted(true);

		leteer_view_btn.setText("Letter");
		leteer_view_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				letter_view_btnActionPerformed(evt);
			}
		});

		newaccountmonitor_btn.setText("��������");
		newaccountmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				newaccountmonitor_btnActionPerformed(evt);
			}
		});

		bugmonitor_btn.setText("bug");
		bugmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				bugmonitor_btnActionPerformed(evt);
			}
		});

		bossmonitor_btn.setText("boss");
		bossmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				bossmonitor_btnActionPerformed(evt);
			}
		});
		returnstatusmonitor_btn.setText("����");
		returnstatusmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				returnstatusmonitor_btnActionPerformed(evt);
			}
		});
		gmcommandsmonitor_btn.setText("GM");
		gmcommandsmonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				gmcommandsmonitor_btnActionPerformed(evt);
			}
		});
		warehousemonitor_btn.setText("â��");
		warehousemonitor_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				warehousemonitor_btnActionPerformed(evt);
			}
		});
		GroupLayout top_paneLayout = new GroupLayout(top_pane);
		top_pane.setLayout(top_paneLayout);
		top_paneLayout.setHorizontalGroup(
				top_paneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(top_paneLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(serverstart_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(serverclose_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(serversave_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)

						.addComponent(servermonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)

						.addComponent(enchantmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)

						.addComponent(systemmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)

						.addComponent(observemonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)

						.addComponent(trademonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)

						.addComponent(chatmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(event_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(usrlist_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(newaccountmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(bugmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(bossmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(returnstatusmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(gmcommandsmonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(warehousemonitor_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(leteer_view_btn)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
						.addComponent(memory_pro, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(load_pro, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
				);
		top_paneLayout.setVerticalGroup(
				top_paneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(top_paneLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(top_paneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(leteer_view_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
								.addGroup(top_paneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(serverstart_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(serverclose_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(serversave_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(servermonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(enchantmonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(systemmonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(observemonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(trademonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(newaccountmonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(bugmonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(bossmonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(returnstatusmonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(gmcommandsmonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(warehousemonitor_btn, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
										.addGroup(top_paneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
												.addComponent(usrlist_btn, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(event_btn, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(chatmonitor_btn, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
												.addComponent(load_pro, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
												.addComponent(memory_pro, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
												.addContainerGap())
				);

		desktop.setSelectedFrame(chatmonitor_frame);

		servermonitor_frame.setClosable(true);
		servermonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		servermonitor_frame.setIconifiable(true);
		servermonitor_frame.setMaximizable(true);
		servermonitor_frame.setResizable(true);
		servermonitor_frame.setTitle("���������");
		servermonitor_frame.setToolTipText("��������� â");
		servermonitor_frame.setAutoscrolls(true);
		try {
			servermonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		server_text.setEditable(false);
		//server_text.setRows(5);
		server_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane2.setViewportView(server_text);

		GroupLayout servermonitor_frameLayout = new GroupLayout(servermonitor_frame.getContentPane());
		servermonitor_frame.getContentPane().setLayout(servermonitor_frameLayout);
		servermonitor_frameLayout.setHorizontalGroup(
				servermonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		servermonitor_frameLayout.setVerticalGroup(
				servermonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		servermonitor_frame.setBounds(300,0,300,150);
		desktop.add(servermonitor_frame, JLayeredPane.DEFAULT_LAYER);



		enchantmonitor_frame.setClosable(true);
		enchantmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		enchantmonitor_frame.setIconifiable(true);
		enchantmonitor_frame.setMaximizable(true);
		enchantmonitor_frame.setResizable(true);
		enchantmonitor_frame.setTitle("��æ�����");
		enchantmonitor_frame.setToolTipText("��æ����� â");
		enchantmonitor_frame.setAutoscrolls(true);
		try {
			enchantmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		enchant_text.setEditable(false);
		//server_text.setRows(5);
		enchant_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane4.setViewportView(enchant_text);

		GroupLayout enchantmonitor_frameLayout = new GroupLayout(enchantmonitor_frame.getContentPane());
		enchantmonitor_frame.getContentPane().setLayout(enchantmonitor_frameLayout);
		enchantmonitor_frameLayout.setHorizontalGroup(
				enchantmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		enchantmonitor_frameLayout.setVerticalGroup(
				enchantmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		enchantmonitor_frame.setBounds(0,600,600,320);
		desktop.add(enchantmonitor_frame, JLayeredPane.DEFAULT_LAYER);


		systemmonitor_frame.setClosable(true);
		systemmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		systemmonitor_frame.setIconifiable(true);
		systemmonitor_frame.setMaximizable(true);
		systemmonitor_frame.setResizable(true);
		systemmonitor_frame.setTitle("�ý��۸����");
		systemmonitor_frame.setToolTipText("�ý��۸���� â");
		systemmonitor_frame.setAutoscrolls(true);
		try {
			systemmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		system_text.setEditable(false);
		//server_text.setRows(5);
		system_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane5.setViewportView(system_text);

		GroupLayout systemmonitor_frameLayout = new GroupLayout(systemmonitor_frame.getContentPane());
		systemmonitor_frame.getContentPane().setLayout(systemmonitor_frameLayout);
		systemmonitor_frameLayout.setHorizontalGroup(
				systemmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		systemmonitor_frameLayout.setVerticalGroup(
				systemmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		systemmonitor_frame.setBounds(300,150,300,150);
		desktop.add(systemmonitor_frame, JLayeredPane.DEFAULT_LAYER);


		observemonitor_frame.setClosable(true);
		observemonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		observemonitor_frame.setIconifiable(true);
		observemonitor_frame.setMaximizable(true);
		observemonitor_frame.setResizable(true);
		observemonitor_frame.setTitle("���ø����");
		observemonitor_frame.setToolTipText("���ø���� â");
		observemonitor_frame.setAutoscrolls(true);
		try {
			observemonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		observe_text.setEditable(false);
		//server_text.setRows(5);
		observe_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane6.setViewportView(observe_text);

		GroupLayout observemonitor_frameLayout = new GroupLayout(observemonitor_frame.getContentPane());
		observemonitor_frame.getContentPane().setLayout(observemonitor_frameLayout);
		observemonitor_frameLayout.setHorizontalGroup(
				observemonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane6, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		observemonitor_frameLayout.setVerticalGroup(
				observemonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane6, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		observemonitor_frame.setBounds(300,300,300,300);
		desktop.add(observemonitor_frame, JLayeredPane.DEFAULT_LAYER);


		/******************trade*******************/
		trademonitor_frame.setClosable(true);
		trademonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		trademonitor_frame.setIconifiable(true);
		trademonitor_frame.setMaximizable(true);
		trademonitor_frame.setResizable(true);
		trademonitor_frame.setTitle("��ȯ");
		trademonitor_frame.setToolTipText("��ȯ �����");
		trademonitor_frame.setAutoscrolls(true);
		try {
			trademonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		trade_text.setEditable(false);
		//server_text.setRows(5);
		trade_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane7.setViewportView(trade_text);

		GroupLayout trademonitor_frameLayout = new GroupLayout(trademonitor_frame.getContentPane());
		trademonitor_frame.getContentPane().setLayout(trademonitor_frameLayout);
		trademonitor_frameLayout.setHorizontalGroup(
				trademonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane7, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		trademonitor_frameLayout.setVerticalGroup(
				trademonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane7, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		trademonitor_frame.setBounds(600,710,600,210);
		desktop.add(trademonitor_frame, JLayeredPane.DEFAULT_LAYER);
		/********************trade*******************/

		/******************newaccount*******************/
		newaccountmonitor_frame.setClosable(true);
		newaccountmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		newaccountmonitor_frame.setIconifiable(true);
		newaccountmonitor_frame.setMaximizable(true);
		newaccountmonitor_frame.setResizable(true);
		newaccountmonitor_frame.setTitle("��������");
		newaccountmonitor_frame.setToolTipText("�������� �����");
		newaccountmonitor_frame.setAutoscrolls(true);
		try {
			newaccountmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		newaccount_text.setEditable(false);
		//server_text.setRows(5);
		newaccount_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane8.setViewportView(newaccount_text);

		GroupLayout newaccountmonitor_frameLayout = new GroupLayout(newaccountmonitor_frame.getContentPane());
		newaccountmonitor_frame.getContentPane().setLayout(newaccountmonitor_frameLayout);
		newaccountmonitor_frameLayout.setHorizontalGroup(
				newaccountmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane8, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		newaccountmonitor_frameLayout.setVerticalGroup(
				newaccountmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane8, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		newaccountmonitor_frame.setBounds(140,0,160,300);
		desktop.add(newaccountmonitor_frame, JLayeredPane.DEFAULT_LAYER);

		/********************newaccount*******************/

		/******************bug*******************/
		bugmonitor_frame.setClosable(true);
		bugmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		bugmonitor_frame.setIconifiable(true);
		bugmonitor_frame.setMaximizable(true);
		bugmonitor_frame.setResizable(true);
		bugmonitor_frame.setTitle("���� �����");
		bugmonitor_frame.setToolTipText("���׸���� â");
		bugmonitor_frame.setAutoscrolls(true);
		try {
			bugmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		bug_text.setEditable(false);
		//server_text.setRows(5);
		bug_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane9.setViewportView(bug_text);

		GroupLayout bugmonitor_frameLayout = new GroupLayout(bugmonitor_frame.getContentPane());
		bugmonitor_frame.getContentPane().setLayout(bugmonitor_frameLayout);
		bugmonitor_frameLayout.setHorizontalGroup(
				bugmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane9, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		bugmonitor_frameLayout.setVerticalGroup(
				bugmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane9, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		bugmonitor_frame.setBounds(1200,250,600,250);
		desktop.add(bugmonitor_frame, JLayeredPane.DEFAULT_LAYER);
		/********************bug*******************/

		/******************boss*******************/
		bossmonitor_frame.setClosable(true);
		bossmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		bossmonitor_frame.setIconifiable(true);
		bossmonitor_frame.setMaximizable(true);
		bossmonitor_frame.setResizable(true);
		bossmonitor_frame.setTitle("������������");
		bossmonitor_frame.setToolTipText("�������");
		bossmonitor_frame.setAutoscrolls(true);
		try {
			bossmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		boss_text.setEditable(false);
		//server_text.setRows(5);
		boss_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane10.setViewportView(boss_text);

		GroupLayout bossmonitor_frameLayout = new GroupLayout(bossmonitor_frame.getContentPane());
		bossmonitor_frame.getContentPane().setLayout(bossmonitor_frameLayout);
		bossmonitor_frameLayout.setHorizontalGroup(
				bossmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane10, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		bossmonitor_frameLayout.setVerticalGroup(
				bossmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane10, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		bossmonitor_frame.setBounds(926,610,394,310);
		desktop.add(bossmonitor_frame, JLayeredPane.DEFAULT_LAYER);
		/********************boss*******************/


		/******************returnstatus*******************/
		returnstatusmonitor_frame.setClosable(true);
		returnstatusmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		returnstatusmonitor_frame.setIconifiable(true);
		returnstatusmonitor_frame.setMaximizable(true);
		returnstatusmonitor_frame.setResizable(true);
		returnstatusmonitor_frame.setTitle("���ʸ����");
		returnstatusmonitor_frame.setToolTipText("���ʸ���� â");
		returnstatusmonitor_frame.setAutoscrolls(true);
		try {
			returnstatusmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		returnstatus_text.setEditable(false);
		//server_text.setRows(5);
		returnstatus_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane11.setViewportView(returnstatus_text);

		GroupLayout returnstatusmonitor_frameLayout = new GroupLayout(returnstatusmonitor_frame.getContentPane());
		returnstatusmonitor_frame.getContentPane().setLayout(returnstatusmonitor_frameLayout);
		returnstatusmonitor_frameLayout.setHorizontalGroup(
				returnstatusmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane11, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		returnstatusmonitor_frameLayout.setVerticalGroup(
				returnstatusmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane11, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		returnstatusmonitor_frame.setBounds(1200,0,600,250);
		desktop.add(returnstatusmonitor_frame, JLayeredPane.DEFAULT_LAYER);

		/********************returnstatus*******************/


		/******************gmcommands*******************/
		gmcommandsmonitor_frame.setClosable(true);
		gmcommandsmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		gmcommandsmonitor_frame.setIconifiable(true);
		gmcommandsmonitor_frame.setMaximizable(true);
		gmcommandsmonitor_frame.setResizable(true);
		gmcommandsmonitor_frame.setTitle("GM�����");
		gmcommandsmonitor_frame.setToolTipText("GM����� â");
		gmcommandsmonitor_frame.setAutoscrolls(true);
		try {
			gmcommandsmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		gmcommands_text.setEditable(false);
		//server_text.setRows(5);
		gmcommands_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane12.setViewportView(gmcommands_text);

		GroupLayout gmcommandsmonitor_frameLayout = new GroupLayout(gmcommandsmonitor_frame.getContentPane());
		gmcommandsmonitor_frame.getContentPane().setLayout(gmcommandsmonitor_frameLayout);
		gmcommandsmonitor_frameLayout.setHorizontalGroup(
				gmcommandsmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane12, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		gmcommandsmonitor_frameLayout.setVerticalGroup(
				gmcommandsmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane12, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		gmcommandsmonitor_frame.setBounds(0,300,300,300);
		desktop.add(gmcommandsmonitor_frame, JLayeredPane.DEFAULT_LAYER);
		/********************gmcommands*******************/


		/******************warehouse*******************/
		warehousemonitor_frame.setClosable(true);
		warehousemonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		warehousemonitor_frame.setIconifiable(true);
		warehousemonitor_frame.setMaximizable(true);
		warehousemonitor_frame.setResizable(true);
		warehousemonitor_frame.setTitle("â������");
		warehousemonitor_frame.setToolTipText("â������ â");
		warehousemonitor_frame.setAutoscrolls(true);
		try {
			warehousemonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//server_text.setColumns(20);
		warehouse_text.setEditable(false);
		//server_text.setRows(5);
		warehouse_text.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jScrollPane13.setViewportView(warehouse_text);

		GroupLayout warehousemonitor_frameLayout = new GroupLayout(warehousemonitor_frame.getContentPane());
		warehousemonitor_frame.getContentPane().setLayout(warehousemonitor_frameLayout);
		warehousemonitor_frameLayout.setHorizontalGroup(
				warehousemonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane13, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
				);
		warehousemonitor_frameLayout.setVerticalGroup(
				warehousemonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane13, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
				);

		warehousemonitor_frame.setBounds(600,500,600,210);
		desktop.add(warehousemonitor_frame, JLayeredPane.DEFAULT_LAYER);
		/********************warehouse*******************/




		chatmonitor_frame.setClosable(true);
		chatmonitor_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		chatmonitor_frame.setIconifiable(true);
		chatmonitor_frame.setMaximizable(true);
		chatmonitor_frame.setResizable(true);
		chatmonitor_frame.setTitle("ä�ø����");
		chatmonitor_frame.setToolTipText("ä�ø���� â");
		chatmonitor_frame.setAutoscrolls(true);
		try {
			chatmonitor_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
			e1.printStackTrace();
		}

		//chat_text.setColumns(20);
		chat_text.setEditable(false);
		//chat_text.setRows(5);
		jScrollPane1.setViewportView(chat_text);

		n_btn_g.setText("�Ϲ�");
		n_btn_g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				n_btn_gActionPerformed(evt);
			}
		});

		w_btn_g.setText("�ӼӸ�");
		w_btn_g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				w_btn_gActionPerformed(evt);
			}
		});

		g_btn_g.setText("��ü");
		g_btn_g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				g_btn_gActionPerformed(evt);
			}
		});

		c_btn_g.setText("����");
		c_btn_g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				c_btn_gActionPerformed(evt);
			}
		});

		p_btn_g.setText("��Ƽ");
		p_btn_g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				p_btn_gActionPerformed(evt);
			}
		});

		s_btn_g.setText("�α�");
		s_btn_g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				s_btn_gActionPerformed(evt);
			}
		});

		cp_btn_g.setText("ä����Ƽ");
		cp_btn_g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cp_btn_gActionPerformed(evt);
			}
		});

		chat_target.setHorizontalAlignment(JTextField.CENTER);
		chat_target.setEnabled(false);

		chat_msg.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				chat_msgKeyPressed(evt);
			}
		});

		chat_type.setModel(new DefaultComboBoxModel(new String[] { "��ü", "����", "��Ƽ", "�ӼӸ�", "��Ƽä��" }));
		chat_type.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				chat_typeItemStateChanged(evt);
			}
		});

		GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(chat_type, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(10, 10, 10)
						.addComponent(chat_target, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(chat_msg, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
				);
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(chat_msg, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(chat_type, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(chat_target, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		GroupLayout chatmonitor_frameLayout = new GroupLayout(chatmonitor_frame.getContentPane());
		chatmonitor_frame.getContentPane().setLayout(chatmonitor_frameLayout);
		chatmonitor_frameLayout.setHorizontalGroup(
				chatmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(chatmonitor_frameLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(n_btn_g)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(g_btn_g)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(c_btn_g)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(p_btn_g)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(cp_btn_g)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(s_btn_g)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(w_btn_g)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
				);
		chatmonitor_frameLayout.setVerticalGroup(
				chatmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(chatmonitor_frameLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(chatmonitor_frameLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(n_btn_g)
								.addComponent(g_btn_g)
								.addComponent(c_btn_g)
								.addComponent(p_btn_g)
								.addComponent(cp_btn_g)
								.addComponent(s_btn_g)
								.addComponent(w_btn_g))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				);

		chatmonitor_frame.setBounds(600, 0, 600, 500);
		desktop.add(chatmonitor_frame, JLayeredPane.DEFAULT_LAYER);

		event_frame.setClosable(true);
		event_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		event_frame.setIconifiable(true);
		event_frame.setTitle("�̺�Ʈ");
		event_frame.setToolTipText("�̺�Ʈ ��ư ����");

		all_buff_btn.setText("��ü����");
		all_buff_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				all_buff_btnActionPerformed(evt);
			}
		});

		gift_btn.setText("�����ֱ�");
		gift_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				gift_btnActionPerformed(evt);
			}
		});

		poly_btn.setText("ĳ������");
		poly_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				poly_btnActionPerformed(evt);
			}
		});

		move_btn.setText("�����̵�");
		move_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				move_btnActionPerformed(evt);
			}
		});

		no_chat_btn.setText("��üä��");
		no_chat_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				no_chat_btnActionPerformed(evt);
			}
		});

		new_account_btn.setText("��������");
		new_account_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				new_account_btnActionPerformed(evt);
			}
		});

		del_account_btn.setText("��������");
		del_account_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				del_account_btnActionPerformed(evt);
			}
		});

		etc_btn.setText("�̺�Ʈ");
		etc_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				etc_btnActionPerformed(evt);
			}
		});

		char_ban_btn.setText("ĳ���߹�");
		char_ban_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				char_ban_btnActionPerformed(evt);
			}
		});

		char_no_chat_btn.setText("ä��Ǯ��");
		char_no_chat_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				char_no_chat_btnActionPerformed(evt);
			}
		});

		serversetting_btn.setText("��������");
		serversetting_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				serversetting_btnActionPerformed(evt);
			}
		});


		GroupLayout event_frameLayout = new GroupLayout(event_frame.getContentPane());
		event_frame.getContentPane().setLayout(event_frameLayout);
		event_frameLayout.setHorizontalGroup(
				event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(event_frameLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(event_frameLayout.createSequentialGroup()
										.addComponent(no_chat_btn)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(del_account_btn))
										.addGroup(event_frameLayout.createSequentialGroup()
												.addComponent(all_buff_btn)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(new_account_btn))
												.addGroup(event_frameLayout.createSequentialGroup()
														.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
																.addComponent(poly_btn)
																.addComponent(gift_btn))
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
																		.addComponent(char_no_chat_btn)
																		.addComponent(etc_btn)))
																		.addComponent(serversetting_btn)
																		.addComponent(etc_btn)
																		.addGroup(event_frameLayout.createSequentialGroup()
																				.addComponent(move_btn)
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																				.addComponent(char_ban_btn)))
																				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		event_frameLayout.setVerticalGroup(
				event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(event_frameLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
								.addComponent(new_account_btn, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(all_buff_btn, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
										.addComponent(del_account_btn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(no_chat_btn, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
												.addComponent(etc_btn, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
												.addComponent(gift_btn, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
														.addComponent(poly_btn, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
														.addComponent(char_no_chat_btn, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(event_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
																.addComponent(char_ban_btn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(move_btn, GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE))
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(serversetting_btn, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
																.addGap(33, 33, 33))
				);

		event_frame.setBounds(360, 0, 200, 260);
		desktop.add(event_frame, JLayeredPane.DEFAULT_LAYER);

		// �Ű���
		report_frame.setClosable(true);
		report_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		report_frame.setIconifiable(true);
		report_frame.setMaximizable(false);
		report_frame.setTitle("�Ű���");
		report_frame.setToolTipText("�Ű���");
		report_frame.setVisible(false);

		report_btng.add(report_btn15d);
		report_btng.add(report_btn1m);
		report_btng.add(report_btn2m);
		report_btng.add(report_btn3m);
		report_btng.add(report_btn4m);
		report_btng.add(report_btnchar);

		report_btn15d.setText("7��");
		report_btn15d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				report_btn7dActionPerformed(evt);
			}
		});
		report_btn15d.setSelected(true);

		report_btn1m.setText("15��");
		report_btn1m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				report_btn15dActionPerformed(evt);
			}
		});

		report_btn2m.setText("1����");
		report_btn2m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				report_btn1mActionPerformed(evt);
			}
		});

		report_btn3m.setText("2����");
		report_btn3m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				report_btn2mActionPerformed(evt);
			}
		});

		report_btn4m.setText("��ü");
		report_btn4m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				report_btnallActionPerformed(evt);
			}
		});

		report_btnchar.setText("ĳ����");
		report_btnchar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				report_btncharActionPerformed(evt);
			}
		});

		GroupLayout report_pane1Layout = new GroupLayout(report_pane1);
		report_pane1.setLayout(report_pane1Layout);
		report_pane1Layout.setHorizontalGroup(
				report_pane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(report_pane1Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(report_btn15d)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(report_btn1m)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(report_btn2m)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(report_btn3m)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(report_btn4m)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(report_btnchar)
						.addContainerGap(14, Short.MAX_VALUE))
				);
		report_pane1Layout.setVerticalGroup(
				report_pane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(report_pane1Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(report_pane1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(report_btn15d)
								.addComponent(report_btn1m)
								.addComponent(report_btn2m)
								.addComponent(report_btn3m)
								.addComponent(report_btn4m)
								.addComponent(report_btnchar))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		report_table.setModel(new DefaultTableModel(null, new String [] { "���� ĳ��", "�Ű� ĳ��", "�Ű� ��¥" }) {
			Class[] types = new Class [] {
					java.lang.String.class, java.lang.String.class, java.lang.String.class
			};
			boolean[] canEdit = new boolean [] {
					false, false, false
			};

			public Class getColumnClass(int columnIndex) {
				return types [columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit [columnIndex];
			}
		});
		report_scoll.setViewportView(report_table);

		GroupLayout report_pane2Layout = new GroupLayout(report_pane2);
		report_pane2.setLayout(report_pane2Layout);
		report_pane2Layout.setHorizontalGroup(
				report_pane2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(report_scoll, GroupLayout.PREFERRED_SIZE, 376, GroupLayout.PREFERRED_SIZE)
				);
		report_pane2Layout.setVerticalGroup(
				report_pane2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(report_scoll, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
				);

		GroupLayout report_layout = new GroupLayout(report_frame.getContentPane());
		report_frame.getContentPane().setLayout(report_layout);
		report_layout.setHorizontalGroup(
				report_layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(report_pane1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(report_pane2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				);
		report_layout.setVerticalGroup(
				report_layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(report_layout.createSequentialGroup()
						.addComponent(report_pane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(report_pane2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		report_frame.setBounds(376, 0, 400, 260);
		desktop.add(report_frame, JLayeredPane.DEFAULT_LAYER);

		// ��������
		letterview_frame.setClosable(true);
		letterview_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		letterview_frame.setIconifiable(true);
		letterview_frame.setMaximizable(true);
		letterview_frame.setResizable(true);
		letterview_frame.setTitle("��������");
		letterview_frame.setToolTipText("��������");
		letterview_frame.setVisible(false);

		letter_charName_label.setText("ĳ����");

		letter_charName.setHorizontalAlignment(JTextField.CENTER);

		letter_load.setText("����");
		letter_load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				letter_loadActionPerformed(evt);
			}
		});

		letter_search.setText("�˻�");
		letter_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				letter_searchActionPerformed(evt);
			}
		});
		letter_table.setRowSelectionAllowed(false);
		letter_table.setModel(new DefaultTableModel(null, new String [] { "������ ���", "����", "����", "������¥", "Ȯ�ο���" }) {
			Class[] types = new Class [] {
					java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
			};
			boolean[] canEdit = new boolean [] {
					false, false, false, false, true
			};

			public Class getColumnClass(int columnIndex) {
				return types [columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit [columnIndex];
			}
		});
		letter_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				jletter_tableMouseClicked(evt);
			}
		});
		letter_scoll.setViewportView(letter_table);

		GroupLayout letter_paneLayout = new GroupLayout(letter_pane);
		letter_pane.setLayout(letter_paneLayout);
		letter_paneLayout.setHorizontalGroup(
				letter_paneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(letter_scoll, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
				);
		letter_paneLayout.setVerticalGroup(
				letter_paneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(letter_scoll, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
				);

		GroupLayout letter_layout = new GroupLayout(letterview_frame.getContentPane());
		letterview_frame.getContentPane().setLayout(letter_layout);
		letter_layout.setHorizontalGroup(
				letter_layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(letter_layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(letter_charName_label)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(letter_charName, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(letter_search)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(letter_load)
						.addContainerGap(274, Short.MAX_VALUE))
						.addComponent(letter_pane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				);
		letter_layout.setVerticalGroup(
				letter_layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(letter_layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(letter_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(letter_charName_label)
								.addComponent(letter_charName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(letter_load)
								.addComponent(letter_search))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(letter_pane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		letterview_frame.setBounds(630, 0, 330, 260);
		desktop.add(letterview_frame, JLayeredPane.DEFAULT_LAYER);

		userlist_frame.setClosable(true);
		userlist_frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		userlist_frame.setIconifiable(true);
		userlist_frame.setMaximizable(true);
		userlist_frame.setResizable(true);
		userlist_frame.setTitle("��������Ʈ");
		userlist_frame.setToolTipText("��������Ʈ â");

		jScrollPane3.setViewportView(user_list);

		GroupLayout userlist_frameLayout = new GroupLayout(userlist_frame.getContentPane());
		userlist_frame.getContentPane().setLayout(userlist_frameLayout);
		userlist_frameLayout.setHorizontalGroup(
				userlist_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
				);
		userlist_frameLayout.setVerticalGroup(
				userlist_frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
				);

		userlist_frame.setBounds(0, 0, 140, 300);
		desktop.add(userlist_frame, JLayeredPane.DEFAULT_LAYER);

		logo.setFont(new java.awt.Font("����", 1, 36)); 
		logo.setForeground(UIManager.getDefaults().getColor("nbProgressBar.Foreground"));
		logo.setText(" Jowoo Server DeskTop");
		logo.setBounds(20, 20, 810, 50);
		desktop.add(logo, JLayeredPane.DEFAULT_LAYER);

		jLabel14.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel14.setText("by Shutup");
		jLabel14.setVerticalAlignment(SwingConstants.BOTTOM);
		jLabel14.setBounds(710, 70, 66, 15);
		desktop.add(jLabel14, JLayeredPane.DEFAULT_LAYER);

		user.setText("���Ӽ�");

		user_label.setFont(new java.awt.Font("����", 1, 12));
		user_label.setForeground(new java.awt.Color(0, 0, 255));
		user_label.setText(" ");

		memory.setText("���޸�");

		memory_label.setFont(new java.awt.Font("����", 1, 12));
		memory_label.setForeground(new java.awt.Color(0, 0, 255));
		memory_label.setText(" ");

		GroupLayout bottomLayout = new GroupLayout(bottom);
		bottom.setLayout(bottomLayout);
		bottomLayout.setHorizontalGroup(
				bottomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(bottomLayout.createSequentialGroup()
						.addComponent(user)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(user_label, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(memory)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(memory_label, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(591, Short.MAX_VALUE))
				);
		bottomLayout.setVerticalGroup(
				bottomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(bottomLayout.createSequentialGroup()
						.addGroup(bottomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(user)
								.addComponent(user_label)
								.addComponent(memory)
								.addComponent(memory_label))
								.addContainerGap(11, Short.MAX_VALUE))
				);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(top_pane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottom, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(desktop, GroupLayout.DEFAULT_SIZE, 906, Short.MAX_VALUE)
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(top_pane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(desktop, GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(bottom, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				);

		pack();
	}

	// ���� ����
	private void letter_view_btnActionPerformed(ActionEvent evt) {
		boolean bool = letterview_frame.isVisible();
		if(s5 && bool){
			s5 = false;
			letterview_frame.setVisible(false);
		}else{
			s5 = true;
			letterview_frame.setVisible(true);
		}
	}

	// ���� �����
	private void servermonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = servermonitor_frame.isVisible();
		if(s1 && bool){
			s1 = false;
			servermonitor_frame.setVisible(false);
		}else{
			s1 = true;
			servermonitor_frame.setVisible(true);
		}
	}

	// ��æƮ �����
	private void enchantmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = enchantmonitor_frame.isVisible();
		if(s6 && bool){
			s6 = false;
			enchantmonitor_frame.setVisible(false);
		}else{
			s6 = true;
			enchantmonitor_frame.setVisible(true);
		}
	}

	// �ý��� �����
	private void systemmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = systemmonitor_frame.isVisible();
		if(s7 && bool){
			s7 = false;
			systemmonitor_frame.setVisible(false);
		}else{
			s7 = true;
			systemmonitor_frame.setVisible(true);
		}
	}

	// ���� �����
	private void observemonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = observemonitor_frame.isVisible();
		if(s8 && bool){
			s8 = false;
			observemonitor_frame.setVisible(false);
		}else{
			s8 = true;
			observemonitor_frame.setVisible(true);
		}
	}

	// �ŷ� �����
	private void trademonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = trademonitor_frame.isVisible();
		if(s9 && bool){
			s9 = false;
			trademonitor_frame.setVisible(false);
		}else{
			s9 = true;
			trademonitor_frame.setVisible(true);
		}
	}
	//�������������
	private void newaccountmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = newaccountmonitor_frame.isVisible();
		if(s10 && bool){
			s10 = false;
			newaccountmonitor_frame.setVisible(false);
		}else{
			s10 = true;
			newaccountmonitor_frame.setVisible(true);
		}
	}
	// ���� �����
	private void bugmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = bugmonitor_frame.isVisible();
		if(s11 && bool){
			s11 = false;
			bugmonitor_frame.setVisible(false);
		}else{
			s11 = true;
			bugmonitor_frame.setVisible(true);
		}
	}
	// boss �����
	private void bossmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = bossmonitor_frame.isVisible();
		if(s12 && bool){
			s12 = false;
			bossmonitor_frame.setVisible(false);
		}else{
			s12 = true;
			bossmonitor_frame.setVisible(true);
		}
	}
	// ���� �����
	private void returnstatusmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = returnstatusmonitor_frame.isVisible();
		if(s13 && bool){
			s13 = false;
			returnstatusmonitor_frame.setVisible(false);
		}else{
			s13 = true;
			returnstatusmonitor_frame.setVisible(true);
		}
	}
	// gmcommands �����
	private void gmcommandsmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = gmcommandsmonitor_frame.isVisible();
		if(s14 && bool){
			s14 = false;
			gmcommandsmonitor_frame.setVisible(false);
		}else{
			s14 = true;
			gmcommandsmonitor_frame.setVisible(true);
		}
	}
	//  warehouse �����
	private void warehousemonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = warehousemonitor_frame.isVisible();
		if(s15 && bool){
			s15 = false;
			warehousemonitor_frame.setVisible(false);
		}else{
			s15 = true;
			warehousemonitor_frame.setVisible(true);
		}
	}
	// ä�� �����
	private void chatmonitor_btnActionPerformed(ActionEvent evt) {
		boolean bool = chatmonitor_frame.isVisible();
		if(s2 && bool){
			s2 = false;
			chatmonitor_frame.setVisible(false);
		}else{
			s2 = true;
			chatmonitor_frame.setVisible(true);
		}
	}

	// �̺�Ʈ â
	private void event_btnActionPerformed(ActionEvent evt) {
		boolean bool = event_frame.isVisible();
		if(s3 && bool){
			s3 = false;
			event_frame.setVisible(false);
		}else{
			s3 = true;
			event_frame.setVisible(true);
		}
	}
	// ��������Ʈ
	private void usrlist_btnActionPerformed(ActionEvent evt) {
		boolean bool = userlist_frame.isVisible();
		if(s4 && bool){
			s4 = false;
			userlist_frame.setVisible(false);
		}else{
			s4 = true;
			userlist_frame.setVisible(true);
		}
	}
	// ä��Ÿ�� ����
	private void chat_typeItemStateChanged(ItemEvent evt) {
		String s = (String) chat_type.getSelectedItem();
		if(s.startsWith("��ü")) chat_target.setEnabled(false);
		else chat_target.setEnabled(true);
	}

	// ä�� ����
	private void chat_msgKeyPressed(KeyEvent evt) {
		if(evt.getKeyCode() == 10){
			String target = chat_target.getText();
			if(target != null && target.length() > 0 && !target.equals("")){
				L1PcInstance c = L1World.getInstance().getPlayer(target);
				L1PcInstance cha[] = null;
				if(c != null){
					writeMessage(getChatType(), "[******]->[" + target + "] " + chat_msg.getText());
					//	int t = 0x04;
					switch(chat_type.getSelectedIndex()){
					// ����
					case 1:
						//	t = 0x04;
						cha = c.getClan().getOnlineClanMember();
						break;
						// ��Ƽ
					case 2:
						//	t = 0x0B;
						cha = c.getParty().getMembers();
						break;						
						// ä����Ƽ
					case 4:
						//	t = 0x0E;
						cha = c.getChatParty().getMembers();
						break;
					}
					// cha[] �迭�� �ִٴ°��� ��� ������ �ִٴ� ��..
					if(cha != null){
						for(int i = 0 ; i < cha.length; i++){							
							cha[i].sendPackets(new S_ChatPacket("******", chat_msg.getText()));									
						}
						// ���ٴ°��� �ӼӸ� �̱� ������..
					}else{
						ServerChat.getInstance().whisperToPlayer(chat_target.getText(), chat_msg.getText());						
					}
					cha = null;
				}
			}else{
				// ����־. ��üä�ø� �����ϵ���..
				if(chat_type.getSelectedIndex() == 0){
					writeMessage(getChatType(), "[******] : " + chat_msg.getText());
					ServerChat.getInstance().sendChatToAll("[******] "+chat_msg.getText());
				}
			}
			chat_msg.setText("");
		}
	}

	// �Ϲ�ä�� on
	private void n_btn_gActionPerformed(ActionEvent evt) {
		if(GameServerSetting.getInstance().�Ϲ�){
			GameServerSetting.getInstance().�Ϲ� = false;
		}else{
			GameServerSetting.getInstance().�Ϲ� = true;
		}
	}

	// ��üä�� on
	private void g_btn_gActionPerformed(ActionEvent evt) {
		if(GameServerSetting.getInstance().�۷ι�){
			GameServerSetting.getInstance().�۷ι� = false;
		}else{
			GameServerSetting.getInstance().�۷ι� = true;
		}
	}

	// ����ä�� on
	private void c_btn_gActionPerformed(ActionEvent evt) {
		if(GameServerSetting.getInstance().����){
			GameServerSetting.getInstance().���� = false;
		}else{
			GameServerSetting.getInstance().���� = true;
		}
	}

	// ��Ƽä�� on
	private void p_btn_gActionPerformed(ActionEvent evt) {
		if(GameServerSetting.getInstance().��Ƽ){
			GameServerSetting.getInstance().��Ƽ = false;
		}else{
			GameServerSetting.getInstance().��Ƽ = true;
		}
	}

	// ä����Ƽ on
	private void cp_btn_gActionPerformed(ActionEvent evt) {
		if(GameServerSetting.getInstance().��Ƽ){
			GameServerSetting.getInstance().��Ƽ = false;
		}else{
			GameServerSetting.getInstance().��Ƽ = true;
		}
	}

	// ����ä�� on
	private void s_btn_gActionPerformed(ActionEvent evt) {
		if(GameServerSetting.getInstance().���){
			GameServerSetting.getInstance().��� = false;
		}else{
			GameServerSetting.getInstance().��� = true;
		}
	}

	// �ӼӸ� on
	private void w_btn_gActionPerformed(ActionEvent evt) {
		if(GameServerSetting.getInstance().�ӼӸ�){
			GameServerSetting.getInstance().�ӼӸ� = false;
		}else{
			GameServerSetting.getInstance().�ӼӸ� = true;
		}
	}

	// ��ä����
	private void all_buff_btnActionPerformed(ActionEvent evt) {
		SpecialEventHandler.getInstance().doAllBuf();
	}

	// ��ää��
	private void no_chat_btnActionPerformed(ActionEvent evt) {
		SpecialEventHandler.getInstance().doNotChatEveryone();
		writeMessage(-1, "��üä�� ���� �Ϸ�");
	}

	// �����ֱ�
	private void gift_btnActionPerformed(ActionEvent evt) {
		Gift_Dia.setSize(215, 270);
		Gift_Dia.setVisible(true);
	}

	// �����ϱ�
	private void poly_btnActionPerformed(ActionEvent evt) {
		Poly_Dia.setSize(200, 140);
		Poly_Dia.setVisible(true);
	}

	// ĳ���̵�
	private void move_btnActionPerformed(ActionEvent evt) {
		Move_Dia.setSize(340, 150);
		Move_Dia.setVisible(true);
	}

	// ��������
	private void serversetting_btnActionPerformed(ActionEvent evt) {
		evaset.setVisible(true);
	}

	// ��������
	private void new_account_btnActionPerformed(ActionEvent evt) {
		C_Account_Dia.setSize(338, 70);
		C_Account_Dia.setVisible(true);
	}

	// ��������
	private void del_account_btnActionPerformed(ActionEvent evt) {
		if(isServer){
			int objId = 0;
			String uAccuntName = "";
			ResultSet r = null;
			ResultSet rr = null;
			Connection c = null;
			PreparedStatement p = null;
			PreparedStatement pp = null;
			PreparedStatement ppp = null;
			PreparedStatement pppp = null;
			PreparedStatement warehouse = null;
			PreparedStatement teleport = null;
			PreparedStatement skills = null;
			PreparedStatement quests = null;
			PreparedStatement items = null;
			PreparedStatement elf_warehouse = null;
			PreparedStatement config = null;
			PreparedStatement buff = null;
			PreparedStatement buddys = null;
			try{
				c = L1DatabaseFactory.getInstance().getConnection();
				p = c.prepareStatement("select login as uID from accounts where date_add(lastactive, interval 7 day) <= curdate()");
				r = p.executeQuery();
				// �ִٸ�
				while(r.next()){
					uAccuntName = r.getString(1);
					// ������Ʈ ���̵� �˻�
					pp = c.prepareStatement("select objid as oID from characters where account_name=?");
					pp.setString(1, uAccuntName);
					rr = pp.executeQuery();
					// ������ ĳ�� ��ŭ ������, â�� �� ����
					while(rr.next()){
						objId = rr.getInt(1);
						// â��
						warehouse = c.prepareStatement("delete from character_warehouse where account_name=?");
						warehouse.setString(1, uAccuntName);
						warehouse.execute();
						// ��
						teleport  = c.prepareStatement("delete from character_teleport where char_id=?");
						teleport.setInt(1, objId);
						teleport.execute();
						// ��ų
						skills  = c.prepareStatement("delete from character_skills where char_obj_id=?");
						skills.setInt(1, objId);
						skills.execute();
						// ����Ʈ
						quests  = c.prepareStatement("delete from character_quests where char_id=?");
						quests.setInt(1, objId);
						quests.execute();
						//������
						items  = c.prepareStatement("delete from character_items where char_id=?");
						items.setInt(1, objId);
						items.execute();
						//���� â��
						elf_warehouse  = c.prepareStatement("delete from character_elf_warehouse where account_name=?");
						elf_warehouse.setString(1, uAccuntName);
						elf_warehouse.execute();
						//�� -��-
						config  = c.prepareStatement("delete from character_config where object_id=?");
						config.setInt(1, objId);
						config.execute();
						//����
						buff  = c.prepareStatement("delete from character_buff where char_obj_id=?");
						buff.setInt(1, objId);
						buff.execute();    
						// ģ��
						buddys= c.prepareStatement("delete from character_buddys where char_id=?");
						buddys.setInt(1, objId);
						buddys.execute();

						// ����� Statement ����
						SQLUtil.close(warehouse);
						SQLUtil.close(teleport);
						SQLUtil.close(skills);
						SQLUtil.close(quests);
						SQLUtil.close(items);
						SQLUtil.close(elf_warehouse);
						SQLUtil.close(config);
						SQLUtil.close(buff);
						SQLUtil.close(buddys);
					}
					// ������Ʈ ���̵� �˻�
					ppp = c.prepareStatement("delete from characters where objid=?");
					ppp.setInt(1, objId);
					ppp.execute();
					SQLUtil.close(ppp);
					pppp = c.prepareStatement("delete from accounts where login=?");
					pppp.setString(1, uAccuntName);
					pppp.execute();
					SQLUtil.close(pppp);
				}
			}catch(Exception e){
				writeMessage(-1, "DB �ɸ��ͻ��� ����."); 
				e.printStackTrace();
			}finally{
				SQLUtil.close(rr);
				SQLUtil.close(pp);
				SQLUtil.close(r);
				SQLUtil.close(p);
				SQLUtil.close(c);
				writeMessage(-1, "DB �ɸ��ͻ��� �Ϸ�.");  
				javax.swing.JOptionPane.showMessageDialog(this, "DB �ɸ����� �Ϸ�", "Sohee Server", javax.swing.JOptionPane.INFORMATION_MESSAGE);
			}
		}else{
			JOptionPane.showMessageDialog(null, "������ �������� �ƴմϴ�", "The server did not start", JOptionPane.WARNING_MESSAGE);
		}   
	}	

	// ĳ���߹�
	private void char_ban_btnActionPerformed(ActionEvent evt) {
		loadCharacters();
		String name = dialogShow();
		if(name != null && name.length() > 0 && !name.equals("")){
			L1PcInstance c = L1World.getInstance().getPlayer(name);
			if(c != null){
				try{
					c.sendPackets(new S_SystemMessage((new StringBuilder())
							. append(c.getName()). append(" ��(��) �߹� �߽��ϴ�. ")
							. toString()));				
					c.sendPackets(new S_Disconnect());
					writeMessage(-1, c.getName() + "��(��) �����߹�.");
				}catch (Exception e){}
			}else{
				errorMessage("�׷� ĳ���� �����ϴ�.");
			}
		}
	}
	// ��Ÿ �̺�Ʈ ����.
	private void etc_btnActionPerformed(ActionEvent evt) {
		String s = (String) JOptionPane.showInputDialog(this, "�̺�Ʈ�� �����ϼ���.", "�̺�Ʈ", JOptionPane.QUESTION_MESSAGE, null, event, event[0]);
		if(s != null && s.length() > 0){
			int i = 0;
			for(Method m : this.getClass().getDeclaredMethods()){
				if(m.getName().equals(s)){
					try{
						i++;
						writeMessage(-1, s + "�̺�Ʈ�� �����Ͽ����ϴ�.");
						m.invoke(this);
						break;
					}catch(Exception e){}
				}
			}
			if(i < 1) errorMessage("�׷� ��ɾ�� �������� �ʽ��ϴ�.");
		}
	}	
	// ä��Ǯ��
	private void char_no_chat_btnActionPerformed(ActionEvent evt) {
		SpecialEventHandler.getInstance().doChatEveryone();
		writeMessage(-1, "��üä��Ǯ�� ���� �Ϸ�");
	}
	// ��������
	private void serverstart_btnActionPerformed(ActionEvent evt) {
		if(!isServer){
			isServer = true;
			new Thread(){
				@Override
				public void run(){
					try{
						Server.createServer().start();
					}catch(Exception e){}
				}
			}.start();
		}else{
			errorMessage("������ �̹� �������Դϴ�.");
		}
	}			
	// ��������
	private void serverclose_btnActionPerformed(ActionEvent evt) {			
		GameServer.getInstance().shutdownWithCountdown(30);	
	}
	// ��������
	private void serversave_btnActionPerformed(ActionEvent evt) {
		if(isServer){
			int count = GameServer.getInstance().saveAllCharInfo();
			if(count > -1){ // exception �߻��ϸ� -1 ���� 					
				L1World.getInstance().broadcastServerMessage("[Character Save Complete..]");
				writeMessage(-1, "ĳ������ ����Ϸ�"+" C:"+count+"");	
				infoMessage("ĳ���� ���� ���� ����");
			}else{
				errorMessage("ĳ���� ���� ���� ����");
			}
		}else{
			errorMessage("������ ���������� �ʽ��ϴ�.");
		}
	}

	// �����ֱ� ĳ�� �˻�
	private void gift_search1ActionPerformed(ActionEvent evt) {
		loadCharacters();
		String name = dialogShow();
		if(name != null && name.length() > 0) gift_charName.setText(name);
	}

	// �����ֱ� ������ �˻�
	private void gift_search2ActionPerformed(ActionEvent evt) {
		loadItem();
		int idx = dialogShow2();
		if(idx > -1) gift_itemId.setText(Integer.toString(idx));
	}

	// �����ֱ� ����
	private void gift_okActionPerformed(ActionEvent evt) {
		try{
			String name = gift_charName.getText();
			int id = Integer.parseInt(gift_itemId.getText());
			int cnt = Integer.parseInt(gift_itemCount.getText());
			int enlv = Integer.parseInt(gift_itemEnlv.getText());
			//	int type = 1;
			switch(gift_itemType.getSelectedIndex()){
			// ����
			case 0:
				//		type = 1;
				break;
				// ��
			case 1:
				//		type = 2;
				break;
				// ��Ÿ
			case 2:
				//		type = 0;
				break;				
			}
			L1PcInstance c = L1World.getInstance().getPlayer(name);
			if(c != null){
				L1ItemInstance item = ItemTable.getInstance().createItem(id);
				item.setEnchantLevel(enlv);
				item.setCount(cnt);			       
				if (c.getInventory().checkAddItem(item, cnt) == L1Inventory.OK) {
					c.getInventory().storeItem(item);
					writeMessage(-1, c.getName()+" : ��æ : "+enlv+" Id : "+id+" : "+cnt+"�� ����.");
				}
			}
		}catch(Exception e){
		}finally{
			Gift_Dia.setVisible(false);
		}
	}
	// ���� ����
	private void poly_okActionPerformed(ActionEvent evt) {
		int poly = -1;
		if(poly_id.getText() != null && poly_id.getText().length() > 0) poly = Integer.parseInt(poly_id.getText());
		if(poly > -1){
			L1PcInstance c = L1World.getInstance().getPlayer(poly_charName.getText());
			if(c != null){					
				L1PolyMorph.doPoly(c, poly, 3600, L1PolyMorph.MORPH_BY_GM);
				writeMessage(-1, c.getName()+" : " + poly +" ����.");
			}
		}
		Poly_Dia.setVisible(false);
	}
	// ���� ĳ�� �˻�
	private void poly_searchActionPerformed(ActionEvent evt) {
		loadCharacters();
		String name = dialogShow();
		if(name != null && name.length() > 0) poly_charName.setText(name);
	}

	// ĳ���̵� ĳ�� �˻�
	private void move_searchActionPerformed(ActionEvent evt) {
		loadCharacters();
		String name = dialogShow();
		if(name != null && name.length() > 0) move_charName.setText(name);
	}

	// ĳ���̵� ����
	private void move_okActionPerformed(ActionEvent evt) {
		int x = 0;
		int y = 0;
		int mapId = -1;
		if(move_x.getText() != null && move_x.getText().length() > 0) x = Integer.parseInt(move_x.getText());
		if(move_y.getText() != null && move_y.getText().length() > 0) y = Integer.parseInt(move_y.getText());
		if(move_map.getText() != null && move_map.getText().length() > 0) mapId = Integer.parseInt(move_map.getText());
		if(x > 0 && y > 0 && mapId > -1){
			L1PcInstance c = L1World.getInstance().getPlayer(move_charName.getText());
			if(c != null){
				L1Teleport.teleport(c, x, y, (short) mapId, 5, false);			
				writeMessage(-1, c.getName() + "�� �̵����׽��ϴ�.");
			}
		}
		Move_Dia.setVisible(false);
	}
	// �������� ����
	private void c_ac_okActionPerformed(ActionEvent evt) {	
		if(isServer){
			try {
				String login = null;	
				String password = null;
				java.sql.Connection con = null;
				con = L1DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = null;
				PreparedStatement pstm = null;			
				statement = con.prepareStatement("select * from accounts where login Like '" + c_ac_id.getText() + "'");
				ResultSet rs = statement.executeQuery();

				password = encodePassword(c_ac_pass.getText());

				if(rs.next())login = rs.getString(1);			
				if (login != null){
					JOptionPane.showMessageDialog(null, "���� �̸��� ������ �����մϴ�", "Sohee server", JOptionPane.WARNING_MESSAGE);
					return;
				} else {
					String sqlstr = "INSERT INTO accounts SET login=?,password=?,lastactive=?,access_level=?,ip=?,host=?,banned=?,charslot=?,gamepassword=?,notice=?,webAccount=?,point_time=?,point_time_ready=?,prcheck=?,prcount=?,quize=?";
					pstm = con.prepareStatement(sqlstr);
					pstm.setString(1, c_ac_id.getText());
					pstm.setString(2, password);
					pstm.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					pstm.setInt(4, 0);
					pstm.setString(5, "127.0.0.1");
					pstm.setString(6, "127.0.0.1");
					pstm.setInt(7, 0);
					pstm.setInt(8, 6);
					pstm.setInt(9, 0);
					pstm.setInt(10, 0);
					pstm.setString(11, "");
					pstm.setInt(12, 0);
					pstm.setInt(13, 0);
					pstm.setInt(14, 0);
					pstm.setInt(15, 0);
					pstm.setString(16, "");
					pstm.execute();
					writeMessage(-1, c_ac_id.getText()+" �������� �Ϸ�");  
					javax.swing.JOptionPane.showMessageDialog(this, c_ac_id.getText()+" �������� �Ϸ�", "Sohee Server", javax.swing.JOptionPane.INFORMATION_MESSAGE);				
				}
				rs.close();
				pstm.close();
				statement.close();
				con.close();
			} catch (Exception e) {

			}
		}else{
			JOptionPane.showMessageDialog(null, "������ �������� �ƴմϴ�", "The server did not start", JOptionPane.WARNING_MESSAGE);
		}
		C_Account_Dia.setVisible(false);
	}

	private static String encodePassword(final String rawPassword)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] buf = rawPassword.getBytes("UTF-8");
		buf = MessageDigest.getInstance("SHA").digest(buf);

		return Base64.encodeBytes(buf);
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new eva().setVisible(true);
			}
		});
	}	
	// ���̾�α� ��..
	private String dialogShow(){
		String tmp = null;
		if(tmpArray.size() > 1){
			String[] s = tmpArray.toArray(new String[tmpArray.size()]);
			tmp = (String) JOptionPane.showInputDialog(this, "����� �����ϼ���.", "�˻����", JOptionPane.QUESTION_MESSAGE, null, s, s[0]);
		}
		tmpArray.clear();
		return tmp;
	}
	// ���̾�α� ��2..
	private int dialogShow2(){
		int idx = -1;
		String tmp = null;
		if(tmpArray.size() > 1 && tmpArray2.size() > 1){
			String[] s = tmpArray.toArray(new String[tmpArray.size()]);
			tmp = (String) JOptionPane.showInputDialog(this, "����� �����ϼ���.", "�˻����", JOptionPane.QUESTION_MESSAGE, null, s, s[0]);
			if(tmp != null && tmp.length() > 0) idx = tmpArray2.get(tmpArray.indexOf(tmp));
		}
		tmpArray.clear();
		tmpArray2.clear();
		return idx;
	}

	// ä�� Ÿ��
	private int getChatType(){
		int s = 2;
		switch(chat_type.getSelectedIndex()){
		case 0:
			s = 2;
			break;
		case 1:
			s = 3;
			break;
		case 2:
			s = 4;
			break;
		case 3:
			s = 7;
			break;
		case 4:
			s = 5;
			break;
		}
		return s;
	}

	// ����..
	public void errorMessage(String msg){
		JOptionPane.showMessageDialog(this, msg, "���", JOptionPane.ERROR_MESSAGE);
	}
	// ����..
	public static void infoMessage(String msg){
		JOptionPane.showMessageDialog(null, msg, "�˸�", JOptionPane.INFORMATION_MESSAGE);
	}
	// ����..
	public static void yes_noMessage(String msg, int type){
		int confirm = JOptionPane.showConfirmDialog(null, msg, "�˸�", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if(confirm == 0){
			JInternalFrame jf = null;
			switch(type){
			case 0:
				updateReport(-1);
				jf = report_frame;
				break;
			}
			if(jf != null){
				jf.setVisible(true);
			}
		}
	}

	// �ؽ�Ʈ �۾���
	public static void writeMessage(int type, String msg){
		String c_s = "";
		String fstring = "";
		Color c = null;
		JTextPane jt = null;
		switch(type){
		/* 0 = servermonitor
		 * -1 = systemmonitor
		 * -2 = enchantmonitor : ��æƮ �����
		 * -3 = newaccountmonitor : ���� ���� �����
		 * -4 = bugmonitor : ���׸����
		 * -5 = trademonitor : �ŷ� �����
		 * -6 = bossmonitor : ���� ������ �����
		 * -7 = returnstatusmonitor : ���� �ʱ�ȭ �����
		 * -8 = gmcommandsmonitor : GM ��ɾ� �����
		 * -9 = warehousemonitor : â�� �����
		 * -10 = observemonitor : ������ ���� �����
		 */
		case -10:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = observe_text;
			c_s = "Black";
			break;
		case -9:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = warehouse_text;
			c_s = "Black";
			break;
		case -8:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = gmcommands_text;
			c_s = "Black";
			break;
		case -7:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = returnstatus_text;
			c_s = "Black";
			break;
		case -6:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = boss_text;
			c_s = "Black";
			break;
		case -5:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = trade_text;
			c_s = "Black";
			break;
		case -4:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = bug_text;
			c_s = "Black";
			break;
		case -3:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = newaccount_text;
			c_s = "Black";
			break;
		case -2:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = enchant_text;
			c_s = "Black";
			break;
		case -1:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = system_text;
			c_s = "Black";
			break;
		case 0:
			fstring = "["+getLogTime() +"] ";
			c = Color.black;
			jt = server_text;
			c_s = "Black";
			break;
		default:
			jt = chat_text;
			switch(type){
			// �Ϲ�ä��
			case 1:
				fstring = "["+getLogTime() +"] [�Ϲ�] ";
				c = Color.black;
				c_s = "Black";
				break;
				// ��üä��
			case 2:
				fstring = "["+getLogTime() +"] [��ü] ";
				c = Color.blue;
				c_s = "Blue";
				break;
				// ����ä��
			case 3:
				fstring = "["+getLogTime() +"] [����] ";
				c = Color.green;
				c_s = "Green";
				break;
				// ��Ƽä��
			case 4:
				fstring = "["+getLogTime() +"] [��Ƽ] ";
				c = Color.lightGray;
				c_s = "LightGray";
				break;
				// ä����Ƽä��
			case 5:
				fstring = "["+getLogTime() +"] [ä��] ";
				c = Color.orange;
				c_s = "Orange";
				break;
				// ����ä��
			case 6:
				fstring = "["+getLogTime() +"] [�α�] ";
				c = Color.red;
				c_s = "Red";
				break;
				// �ӼӸ�ä��
			case 7:
				fstring = "["+getLogTime() +"] [�ӼӸ�] ";
				c = Color.magenta;
				c_s = "Magenta";
				break;
				// ���α� ä��
			case 8:
				fstring = "["+getLogTime() +"] [���] ";
				c = Color.darkGray;
				c_s = "DarkGray";
				break;
			}
			break;
		}
		if(jt != null){
			// ��Ÿ�� ����
			StyledDocument doc = jt.getStyledDocument();
			// �� ��Ÿ�� ����
			Style style = jt.addStyle(c_s, null);
			// �÷�Ǯ �ϰ�..
			StyleConstants.setForeground(style, c);
			try {
				// �� ����, ������ ������ ��Ÿ�� ��������..
				doc.insertString(doc.getLength(), fstring + msg + "\n", style);
				jt.setCaretPosition(doc.getLength());
			}catch (BadLocationException e){
				e.printStackTrace();
			}
		}
	}

	public static void clear_text(){
		try{
			StyledDocument doc = server_text.getStyledDocument();
			doc.remove(0, doc.getLength());
			StyledDocument doc1 = chat_text.getStyledDocument();
			doc1.remove(0, doc1.getLength());
		}catch(Exception e){}
	}

	// ĳ���� �ε�
	private void loadCharacters(){
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select char_name from characters order by objid asc");
			r = p.executeQuery();
			while(r.next()) tmpArray.add(r.getString(1));
		}catch(Exception e){
		}finally{
			try{
				p.close();
				c.close();
			}catch(Exception e){}
		}
	}
	// ���� �ε�
	private void loadAccounts(){
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select login from accounts");
			r = p.executeQuery();
			while(r.next()) tmpArray.add(r.getString(1));
		}catch(Exception e){
		}finally{
			try{
				p.close();
				c.close();
			}catch(Exception e){}
		}
	}	
	// ������ �ε�
	private void loadItem(){
		String Query = "";
		ResultSet r = null;
		Connection c = null;
		PreparedStatement p = null;
		switch(gift_itemType.getSelectedIndex()){
		// ����
		case 0:
			Query = "select item_id, name from weapon order by name asc";
			break;
			// ��
		case 1:
			Query = "select item_id, name from armor order by name asc";
			break;
			// ��Ÿ
		case 2:
			Query = "select item_id, name from etcitem order by name asc";
			break;					
		}
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement(Query);
			r = p.executeQuery();
			while(r.next()){
				tmpArray2.add(r.getInt(1));
				tmpArray.add(r.getString(2));
			}

		}catch(Exception e){

		}finally{
			try{
				p.close();
				c.close();
			}catch(Exception e){}
		}
	}

	// �ε� �׷���
	public static void loadProgress(){
		load_pro.setValue(load_pro.getValue()+1);
	}
	// �ε� �׷��� ����
	public static void removeProgress(){
		load_pro.setVisible(false);
	}
	// �޸� ����
	public static void updataMemory(){
		memory_pro.setMaximum((int) SystemUtil.getUsedMemoryMB2());
		memory_pro.setValue((int) SystemUtil.getUsedMemoryMB());
		memory_label.setText(SystemUtil.getUsedMemoryMB() + " / " + SystemUtil.getUsedMemoryMB2());		
	}
	// ������

	public static void updataUserCount(){
		Collection<L1PcInstance> players = L1World.getInstance().getAllPlayers();
		String amount = String.valueOf(players.size() + 1);	
		int AutoShopUser = AutoShopManager.getInstance().getShopPlayerCount();
		user_label.setText(amount + " / " + AutoShopUser);
	}
	// ���� �߰�
	public static void updataUserList(){
		Vector<String> ulist = new Vector<String>();
		for(L1PcInstance c : L1World.getInstance().getAllPlayers()) ulist.add(c.getName());
		user_list.setModel(new DefaultComboBoxModel(ulist));
		//user_list.updateUI();
	}
	// ���� �߰�
	public static void updataUserList(String name){
		Vector<String> ulist = new Vector<String>();
		ulist.add(name);
		for(L1PcInstance c : L1World.getInstance().getAllPlayers()){
			if(!ulist.contains(c.getName())) ulist.add(c.getName());
		}
		user_list.setModel(new DefaultComboBoxModel(ulist));
		//user_list.updateUI();
	}
	/*
	private void �������(){
		SpecialEventHandler.getInstance().doBugRace();	
	}
	 */	
	private void ���ν���(){
		AutoShopManager.getInstance().isAutoShop(true);	
	}
	private void ��������(){
		AutoShopManager.getInstance().isAutoShop(false);	
	}
	private void ����������(){
		letter.setVisible(true);
	}
	private void �Ű���(){
		updateReport(15);
		report_frame.setVisible(true);
	}

	private void ������̺�(){
		DropTable.reload();
		DropItemTable.reload();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void ���Ǿ����ε�() {
		NpcTable.reload();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void �����ε�() {
		ShopTable.reload();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void �����۸��ε�() {
		ItemTable.reload();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void ���Ÿ��ε�() {
		PolyTable.reload();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void ���ظ��ε�() {
		ResolventTable.reload();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void Ʈ�����ڽ�() {
		L1TreasureBox.load();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void UserInfoWindow() {
		if (jServerUserInfoWindow != null && jServerUserInfoWindow.isClosed()) {
			jServerUserInfoWindow = null;
		}

		if (jServerUserInfoWindow == null) {
			jServerUserInfoWindow = new ServerUserInfoWindow("��������", 20, 20, width + 70, height + 150, true, true);				
			desktop.add(jServerUserInfoWindow, JLayeredPane.DEFAULT_LAYER);
		}
	}
	private void LetterWindow() {
		if (jServerLatterLogWindow != null && jServerLatterLogWindow.isClosed()) {
			jServerLatterLogWindow = null;
		}

		if (jServerLatterLogWindow == null) {
			jServerLatterLogWindow = new ServerLatterLogWindow("����", 20, 20, width + 70, height + 150, true, true);				
			desktop.add(jServerLatterLogWindow, JLayeredPane.DEFAULT_LAYER);
		}
	}
	private void ServerSettingWindow() {
		if (jServerSettingWindow != null && jServerSettingWindow.isClosed()) {
			jServerSettingWindow = null;
		}

		if (jServerSettingWindow == null) {
			jServerSettingWindow = new ServerSettingWindow();				
			desktop.add(jServerSettingWindow, JLayeredPane.DEFAULT_LAYER);
		}
	}
	private void ��񸮷ε�(){
		DropTable.reload();
		DropItemTable.reload();
		NpcTable.reload();
		ShopTable.reload();
		ItemTable.reload();
		PolyTable.reload();
		ResolventTable.reload();
		L1TreasureBox.load();
		MapFixKeyTable.reload();
		writeMessage(-1, "DB ���ε� �Ϸ�.");
	}
	private void server_text_clear(){
		server_text.setText("");
	}
	private void enchant_text_clear(){
		enchant_text.setText("");
	}
	private void system_text_clear(){
		system_text.setText("");
	}
	private void observe_text_clear(){
		observe_text.setText("");
	}
	private void trade_text_clear(){
		trade_text.setText("");
	}
	private void returnstatus_text_clear(){
		returnstatus_text.setText("");
	}
	private void gmcommands_text_clear(){
		gmcommands_text.setText("");
	}
	private void warehouse_text_clear(){
		warehouse_text.setText("");
	}
	private void newaccount_text_clear(){
		newaccount_text.setText("");
	}
	private void bug_text_clear(){
		bug_text.setText("");
	}
	private void boss_text_clear(){
		boss_text.setText("");
	}
	private void chat_text_clear(){
		chat_text.setText("");
	}

	private static void readReport(int num){
		Connection c = null;
		PreparedStatement p = null;
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("update report set see=1 where num=?");
			p.setInt(1, num);
			p.execute();
		}catch(Exception e){
		}finally{
			try{
				p.close();
				c.close();
			}catch(Exception e){}
		}
	}
	// �Ű��� ������Ʈ
	private static void updateReport(int day){
		Calendar cal = null;
		SimpleDateFormat sdf = null;
		Connection c = null;
		Vector<Object> row = null;
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		Vector<Object> type = new Vector<Object>();
		final Class[] types_tmp = { java.lang.String.class, java.lang.String.class, java.lang.String.class };
		try{
			// ����.. �� ó�� ����.. ��Ʈ�㶧.. �Ⱥô��͵� ����..
			if(day == -1){
				sdf = new SimpleDateFormat("yy/MM/dd", Locale.KOREA);
				cal = Calendar.getInstance();
				type.add("���� ĳ��");
				type.add("�Ű� ĳ��");
				type.add("�Ű� ��¥");
				long nextTime = System.currentTimeMillis() - 86400000L * day;
				ResultSet r = null;
				PreparedStatement p = null;
				c = L1DatabaseFactory.getInstance().getConnection();
				p = c.prepareStatement("select num, usr_name, target_name, date from report where see=0 order by date asc");
				r = p.executeQuery();
				while(r.next()){
					row = new Vector<Object>();
					row.add(r.getString(2));
					row.add(r.getString(3));
					cal.setTimeInMillis(r.getLong(4));
					row.add(sdf.format(cal.getTime()));
					data.addElement(row);
					readReport(r.getInt(1));
				}
				p.close();
				// �������� ..
			}else if(day == -2){
				type.add("�Ű� ĳ��");
				type.add("�Ű� ī��Ʈ");
				ResultSet r = null;
				ResultSet rr = null;
				PreparedStatement p = null;
				PreparedStatement pp = null;
				c = L1DatabaseFactory.getInstance().getConnection();
				p = c.prepareStatement("select target_name, target_id, num from report group by target_id order by target_id asc");
				r = p.executeQuery();
				while(r.next()){
					row = new Vector<Object>();
					row.add(r.getString(1));
					pp = c.prepareStatement("select count(num) from report where target_id=?");
					pp.setInt(1, r.getInt(2));
					rr = pp.executeQuery();
					row.add(rr.next() ? rr.getInt(1) : 0);
					data.addElement(row);
					readReport(r.getInt(3));
				}
				p.close();			
				// ��ü
			}else if(day == -3){
				sdf = new SimpleDateFormat("yy/MM/dd", Locale.KOREA);
				cal = Calendar.getInstance();
				type.add("���� ĳ��");
				type.add("�Ű� ĳ��");
				type.add("�Ű� ��¥");
				ResultSet r = null;
				PreparedStatement p = null;
				c = L1DatabaseFactory.getInstance().getConnection();
				p = c.prepareStatement("select num, usr_name, target_name, date from report order by date asc");
				r = p.executeQuery();
				while(r.next()){
					row = new Vector<Object>();
					row.add(r.getString(2));
					row.add(r.getString(3));
					cal.setTimeInMillis(r.getLong(4));
					row.add(sdf.format(cal.getTime()));
					data.addElement(row);
					readReport(r.getInt(1));
				}
				p.close();				
				// ��¥ �Ⱓ����..
			}else{
				sdf = new SimpleDateFormat("yy/MM/dd", Locale.KOREA);
				cal = Calendar.getInstance();
				type.add("���� ĳ��");
				type.add("�Ű� ĳ��");
				type.add("�Ű� ��¥");
				long nextTime = System.currentTimeMillis() - 86400000L * day;
				ResultSet r = null;
				PreparedStatement p = null;
				c = L1DatabaseFactory.getInstance().getConnection();
				p = c.prepareStatement("select num, usr_name, target_name, date from report where date > ? order by date asc");
				p.setLong(1, nextTime);
				r = p.executeQuery();
				while(r.next()){
					row = new Vector<Object>();
					row.add(r.getString(2));
					row.add(r.getString(3));
					cal.setTimeInMillis(r.getLong(4));
					row.add(sdf.format(cal.getTime()));
					data.addElement(row);
					readReport(r.getInt(1));
				}
				p.close();
			}
		}catch(Exception e){
		}finally{
			try{
				c.close();
				report_table.setModel(new DefaultTableModel(data, type){
					Class[] types = types_tmp;
					boolean[] canEdit = { false, false, false };
					public Class getColumnClass(int columnIndex) {
						return types [columnIndex];
					}public boolean isCellEditable(int rowIndex, int columnIndex) {
						return canEdit [columnIndex];
					}
				});
			}catch(Exception e){}
		}
	}
	// ����Ʈ���� ������ Ŭ��.
	private void jList1MouseClicked(MouseEvent evt) {
		if(user_list.getSelectedIndex() > -1){
			if(evt.getButton() == evt.BUTTON3){
				jPopupMenu1.setLocation(userlist_frame.getX() + evt.getX(), userlist_frame.getY() + evt.getY() - 15);
				jPopupMenu1.setVisible(true);
			}else{
				jPopupMenu1.setVisible(false);
			}
		}else{
			jPopupMenu1.setVisible(false);
		}
	}
	// ����
	private void jMenuItem1ActionPerformed(ActionEvent evt) {
		String name = (String) user_list.getSelectedValue();
		if(isName(name)){
			poly_charName.setText(name);
			Poly_Dia.setSize(200, 140);
			Poly_Dia.setVisible(true);
		}
	}
	// �ù���
	private void jMenuItem2ActionPerformed(ActionEvent evt) {
		String name = (String) user_list.getSelectedValue();
		if(isName(name)){
			int[] allBuffSkill = { PHYSICAL_ENCHANT_DEX,
					PHYSICAL_ENCHANT_STR, BLESS_WEAPON, ADVANCE_SPIRIT,				
					IRON_SKIN, BRAVE_AURA, SHINING_AURA, NATURES_TOUCH};

			L1PcInstance target = L1World.getInstance(). getPlayer(name);	
			L1Skills skill = null;
			for (int i = 0; i < allBuffSkill.length; i++) {
				skill = SkillsTable.getInstance(). getTemplate(allBuffSkill[i]);
				new L1SkillUse().handleCommands(target, allBuffSkill[i], target.getId(), 
						target.getX(), target.getY(), null, skill.getBuffDuration(), L1SkillUse.TYPE_GMBUFF);
			} 
			writeMessage(-1, name + "���� �ù���.");
		}
	}
	// ä��
	private void jMenuItem3ActionPerformed(ActionEvent evt) {
		String name = (String) user_list.getSelectedValue();
		if(isName(name)){
			L1PcInstance c = L1World.getInstance().getPlayer(name);
			if(c != null){
				c.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_CHAT_PROHIBITED, 10 * 60 * 1000);
				c.sendPackets(new S_SkillIconGFX(36, 10 * 60));
				c.sendPackets(new S_ServerMessage(286, String.valueOf(10)));	// �ʴ��� 10��
				writeMessage(-1, name + "���� 10�а� ä��.");
			}
		}
	}
	// �߹�
	private void jMenuItem4ActionPerformed(ActionEvent evt) {
		String name = (String) user_list.getSelectedValue();
		if(isName(name)){
			L1PcInstance c = L1World.getInstance().getPlayer(name);
			try{
				if(c != null){
					c.sendPackets(new S_SystemMessage((new StringBuilder())
							. append(c.getName()). append(" ��(��) �߹� �߽��ϴ�. ")
							. toString()));				
					c.sendPackets(new S_Disconnect());
					writeMessage(-1, c.getName() + "��(��) �����߹�.");
				}
			}catch (Exception e){}
		}
	}
	// ��������
	private void jMenuItem5ActionPerformed(ActionEvent evt) {
		jServerUserInfoWindow.setVisible(true);
		//errorMessage("���� �غ���..");
		if (jServerUserInfoWindow != null && jServerUserInfoWindow.isClosed()) {
			jServerUserInfoWindow = null;
		}

		if (jServerUserInfoWindow == null) {
			jServerUserInfoWindow = new ServerUserInfoWindow("���� ����", 20, 20, width + 70, height + 150, true, true);				
			jJDesktopPane.add(jServerUserInfoWindow, 0);
		}	
	}
	// �����ֱ�
	private void jMenuItem6ActionPerformed(ActionEvent evt) {
		String name = (String) user_list.getSelectedValue();
		if(isName(name)){
			gift_charName.setText(name);
			Gift_Dia.setSize(215, 270);
			Gift_Dia.setVisible(true);
		}
	}

	// �̸��� �ִ��� �Ǵ�.
	private boolean isName(String name){
		return name != null && name.length() > 0;
	}

	private static String getLogTime(){
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm"); 
		String time = dateFormat.format(currentDate.getTime());
		return time;
	}


	// ���� Ŭ��.
	private void jletter_tableMouseClicked(MouseEvent evt) {
		TableModel model = letter_table.getModel();
		// row idx
		int row = letter_table.getSelectedRow();
		if(row > - 1 && letter_idx != null && letter_idx.length > 0){
			// �ϴ� �б� �ݷ�����..
			if(letter_table.getSelectedColumn() == 4){
				model.setValueAt(true, row, 4);
				int num = letter_idx[row];
				Connection c = null;
				PreparedStatement p = null;
				try{
					c = L1DatabaseFactory.getInstance().getConnection();
					p = c.prepareStatement("update letter set isCheck=? where item_object_id=?");
					p.setInt(1, 1);
					p.setInt(2, num);
					p.execute();
				}catch(Exception e){
				}finally{
					try{
						p.close();
						c.close();
					}catch(Exception e){}
				}
			}
			String memo = (String) model.getValueAt(row, 2);
			letter_table.setToolTipText(memo);
		}
	}
	// ���� �����˻�
	private void letter_searchActionPerformed(ActionEvent evt) {
		loadCharacters();
		String name = dialogShow();
		if(name != null && name.length() > 0) letter_charName.setText(name); 
	}
	// ���� ������Ʈ
	private void letter_loadActionPerformed(ActionEvent evt) {
		String name = letter_charName.getText();
		if(name == null || name.length() < 1){
			errorMessage("�˻��� �̸��� �Է��ϼ���.");
			return;
		}
		int len = 0;
		int size = 0;
		ResultSet r = null;
		ResultSet rr = null;
		Connection c = null;
		PreparedStatement p = null; 
		PreparedStatement pp = null; 
		Object[][] obj = null;
		final Class[] types_tmp = { String.class, String.class, String.class, String.class, Boolean.class };
		String[] type = { "������ ���", "����", "����", "������¥", "Ȯ�ο���" };
		try{
			c = L1DatabaseFactory.getInstance().getConnection();
			p = c.prepareStatement("select count(item_object_id) from letter where receiver=?");
			p.setString(1, name);
			r = p.executeQuery();
			if(r.next()) size = r.getInt(1);
			r.close();
			p.close();
			letter_idx = new int[size];
			obj = new Object[size][5];
			pp = c.prepareStatement("select * from letter where receiver=?");
			pp.setString(1, name);
			rr = pp.executeQuery();
			while(rr.next()){
				letter_idx[len] = rr.getInt(1);
				obj[len][0] = rr.getString(3);
				obj[len][1] = rr.getString(7);
				obj[len][2] = rr.getString(8);
				obj[len][3] = rr.getString(5);
				obj[len][4] = rr.getInt(9) == 1 ? true : false;
				len++;
			}
			letter_table.setModel(new DefaultTableModel(obj, type){
				Class[] types = types_tmp;
				boolean[] canEdit = { false, false, false, false, true };
				public Class getColumnClass(int columnIndex) {
					return types [columnIndex];
				}public boolean isCellEditable(int rowIndex, int columnIndex) {
					return canEdit [columnIndex];
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				pp.close();
				c.close();        		
			}catch(Exception e){}
		}
	}
	// 7����
	private void report_btn7dActionPerformed(ActionEvent evt) {
		updateReport(7);
		report_frame.setVisible(true);
	}
	// 15����
	private void report_btn15dActionPerformed(ActionEvent evt) {
		updateReport(15);
		report_frame.setVisible(true);
	}
	// 1����
	private void report_btn1mActionPerformed(ActionEvent evt) {
		updateReport(30);
		report_frame.setVisible(true);
	}
	// 2����
	private void report_btn2mActionPerformed(ActionEvent evt) {
		updateReport(60);
		report_frame.setVisible(true);
	}
	// ��ü
	private void report_btnallActionPerformed(ActionEvent evt) {
		updateReport(-3);
		report_frame.setVisible(true);
	}
	// ĳ����
	private void report_btncharActionPerformed(ActionEvent evt) {
		updateReport(-2);
		report_frame.setVisible(true);
	}
	
	// ��¥����(yyyy-MM-dd) �ð�(hh-mm)
	public static String getDate(){
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd aa-hh-ss", Locale.KOREA);
		return s.format(Calendar.getInstance().getTime());
	}
	
	/***** �α� ���� *****/
	public static void savelog(){
		File f = null;
		String sTemp = "";
		synchronized(lock){
			sTemp = getDate();
			StringTokenizer s = new StringTokenizer(sTemp, " ");
			date = s.nextToken();
			time = s.nextToken();
			f = new File("ServerLog/"+date);
			if(!f.exists()) f.mkdir();
			flush(server_text, "[" + time + "] ����", date);
			flush(enchant_text, "[" + time + "] ��æƮ", date);
			flush(system_text, "[" + time + "] �ý���", date);
			flush(observe_text, "[" + time + "] ����", date);
			flush(trade_text, "[" + time + "] ��ȯ", date);
			flush(returnstatus_text, "[" + time + "] ����", date);
			flush(gmcommands_text, "[" + time + "] ����", date);			
			flush(warehouse_text, "[" + time + "] â��", date);
			flush(newaccount_text, "[" + time + "] ���ο����", date);
			flush(bug_text, "[" + time + "] ����", date);
			flush(boss_text, "[" + time + "] ����", date);
			flush(chat_text, "[" + time + "] ä��", date);
			sTemp = null;
			date = null;
			time = null;
		}
	}

	public static void flush(JTextPane text, String FileName, String date){
		try{
			RandomAccessFile rnd = new RandomAccessFile("ServerLog/"+ date + "/" + FileName + ".txt", "rw");
			rnd.write(text.getText().getBytes());
			rnd.close();
		}catch(Exception e){}
	}

	public static void LogDel(){
		savelog();
		server_text.setText("");
		enchant_text.setText("");
		system_text.setText("");
		observe_text.setText("");
		trade_text.setText("");
		//returnstatus_text.setText("");
		//gmcommands_text.setText("");
		warehouse_text.setText("");
		newaccount_text.setText("");
		//bug_text.setText("");
		//boss_text.setText("");
		chat_text.setText("");
	}
}
