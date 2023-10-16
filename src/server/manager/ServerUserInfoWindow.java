package server.manager;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.Account;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.SQLUtil;


@SuppressWarnings("serial")
public class ServerUserInfoWindow extends JInternalFrame {
	private ServerPresentWindow jServerPresentWindow = null;
	private ServerPolyWindow jServerPolyWindow = null;
	
	public  JTable jJTable0 = null;
	private JTable jJTable1 = null;
	private JTable jJTable2 = null;
	private JTable jJTable3 = null;
	private JTable jJTable4 = null;
	private JTable jJTable5 = null;
	
	private JScrollPane jJScrollPane1 = null;
	private JScrollPane jJScrollPane2 = null;
	private JScrollPane jJScrollPane3 = null;
	private JScrollPane jJScrollPane4 = null;
	private JScrollPane jJScrollPane5 = null;
	
	private DefaultTableModel model0 = null;
	private DefaultTableModel model1 = null;
	private DefaultTableModel model2 = null;
	private DefaultTableModel model3 = null;
	private DefaultTableModel model4 = null;
	private DefaultTableModel model5 = null;
	
	private JTabbedPane jJTabbedPane1 = null;
	private JTabbedPane jJTabbedPane2 = null;	
	
	private JLabel lbl_UserCount = null;
	private JLabel lbl_Helper = null;
	private JTextField txt_UserName = null;
	//public JList jJList = null;
	//private DefaultListModel listModel = null;
	private JScrollPane pScroll = null;
	private JButton btn_Search = null;
	private JButton btn_Refresh = null;
	private JButton btn_Ban = null;
	private JButton btn_NoChat = null;	
	private JButton btn_Present = null;
	private JButton btn_Poly = null;
	private JButton btn_AllPresent = null;		
	private JButton btn_AllPoly = null;
	
	private JCheckBox chk_Infomation = null;
	
	public ServerUserInfoWindow(String windowName, int x, int y, int width, int height, boolean resizable, boolean closable) {
		super();
		
		initialize(windowName, x, y, width, height, resizable, closable);
	}
	
	public void initialize(String windowName, int x, int y, int width, int height, boolean resizable, boolean closable) {
		this.title = windowName;
		this.closable = closable;      
		this.isMaximum = false;	   
		this.maximizable = false;
		this.resizable = resizable;
		this.iconable = true;   
		this.isIcon = false;			  
	    setSize(width, height);
		setBounds(x, y, width, height);
		setVisible(true);
		frameIcon = new ImageIcon("");
		setRootPaneCheckingEnabled(true);
		
		updateUI();
	    
		try {		
			jJTabbedPane1 = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
			jJTabbedPane2 = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
		    
			lbl_UserCount = new JLabel("�����ڼ� : 0");		
			lbl_UserCount.setForeground(Color.red);
			lbl_Helper = new JLabel("���������� Ŭ�� >");
			lbl_Helper.setForeground(Color.blue);
		    //listModel = new DefaultListModel(); 
		    //jJList = new JList(listModel);
		    //jJList.addListSelectionListener(new JListHandler());
			String[] model0ColName = { "" };
			
			model0 = new DefaultTableModel(model0ColName, 0);
			
			jJTable0 = new JTable(model0);	
			jJTable0.setEnabled(false);
			//jJTable0.addMouseListener(new MouseListenner());
			
		    pScroll = new JScrollPane(jJTable0);
		    txt_UserName = new JTextField();	
		    txt_UserName.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent evt) {
					chatKeyPressed(evt);
				}
			});
		    btn_Search = new JButton("Search");
		    btn_Search.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					if (eva.isServerStarted) {
						try {
							/*
							if (txt_UserName.getText().equalsIgnoreCase("")) {
								eva.errorMsg(eva.blankSetUser);
								return;
							}
							for (int i = 0; i < listModel.size(); i ++) {
								if (((String)listModel.get(i)).indexOf(txt_UserName.getText()) > -1) {
									jJList.setSelectedIndex(i);						
									jJList.ensureIndexIsVisible(i);		
									break;
								} 
							}
							*/
							if (txt_UserName.getText().equalsIgnoreCase("")) {
								eva.errorMsg(eva.blankSetUser);
								return;
							}
							/*
							for (int i = 0; i < jJTable0.getModel().getRowCount(); i++) {
								if (((String)jJTable0.getModel().getValueAt(i, 0)).indexOf(txt_UserName.getText()) > -1) {
									CharacterInfoSearch(i, 0);
									return;
								}
							}
							*/
							L1PcInstance player = L1World.getInstance().getPlayer(txt_UserName.getText());
							
							if (player != null) {
								txt_UserName.setText(player.getName());
								CharacterInfoSearch();
							} else {
								JOptionPane.showMessageDialog(null, txt_UserName.getText() + "���� ���� ����� �������� �ʽ��ϴ�.", "Server Message", JOptionPane.INFORMATION_MESSAGE);
							}
							
							
						} catch (Exception ex) {
							
						}
					} else {
						eva.errorMsg(eva.NoServerStartMSG);
					}
				}
			});
//		    btn_Refresh = new JButton("Refresh");
//		    btn_Refresh.addActionListener(new ActionListener() { 
//				public void actionPerformed(ActionEvent e) {
//					if (eva.isServerStarted) {
//						try {
//							jJList.clearSelection();
//							listModel.clear();
//							
//							for (L1PcInstance player : L1World.getInstance().getAllPlayers()) {
//								if (!player.isNoPlayer()) {									
//									listModel.addElement(player.getName());
//								}
//							}
//												
//							lbl_UserCount.setText("�����ڼ� : " + (listModel.size()));
//						} catch (Exception ex) {
//							jJList.clearSelection();
//						}
//					} else {
//						eva.errorMsg(eva.NoServerStartMSG);
//					}
//				}
//			});
		    btn_Ban = new JButton("��");
		    btn_Ban.setToolTipText("������ ������ ���ŵ�ϴ�.");
		    btn_Ban.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					if (eva.isServerStarted) {
						if (!txt_UserName.getText().equalsIgnoreCase("")) {
							L1PcInstance pc = L1World.getInstance().getPlayer(txt_UserName.getText());
							IpTable iptable = IpTable.getInstance();
							if (pc != null) {
								Account.ban(pc.getAccountName()); // ������ BAN��Ų��.
								iptable.banIp(pc.getNetConnection().getIp()); // BAN ����Ʈ�� IP�� �߰��Ѵ�.
								pc.sendPackets(new S_Disconnect());
								//eva.LogCommandAppend("[***]", "�����߹�", pc.getName());
								txt_UserName.setText("");
							} else {
								eva.errorMsg(txt_UserName.getText() + eva.NoConnectUser);
							}
						} else {
							eva.errorMsg(eva.blankSetUser);
						}
					} else {
						eva.errorMsg(eva.NoServerStartMSG);
					}
				}
			});
		    btn_NoChat = new JButton("ä��");
		    btn_NoChat.setToolTipText("������ ������ 10�а� ä�ݽ�ŵ�ϴ�.");
		    btn_NoChat.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					if (eva.isServerStarted) {
						if (!txt_UserName.getText().equalsIgnoreCase("")) {
							L1PcInstance pc = L1World.getInstance().getPlayer(txt_UserName.getText());
							if (pc != null) {
								pc.getSkillEffectTimerSet().setSkillEffect(L1SkillId.STATUS_CHAT_PROHIBITED, 10 * 60 * 1000);
								pc.sendPackets(new S_SkillIconGFX(36, 10 * 60));
								pc.sendPackets(new S_ServerMessage(286, String.valueOf(10))); // \f3���ӿ� �������� �ʴ� �ൿ�̱� (����)������, ����%0�а� ä���� �����մϴ�.
								//eva.LogCommandAppend("[***]", "ä��", "10");
								txt_UserName.setText("");
							} else {
								eva.errorMsg(txt_UserName.getText() + eva.NoConnectUser);
							}
						} else {
							eva.errorMsg(eva.blankSetUser);
						}
					} else {
						eva.errorMsg(eva.NoServerStartMSG);
					}
				}
			});
		    btn_Present = new JButton("����");
		    btn_Present.setToolTipText("������ �������� ������ �����մϴ�.");
		    btn_Present.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					if (eva.isServerStarted) {
						if (!txt_UserName.getText().equalsIgnoreCase("")) {
							jServerPresentWindow = new ServerPresentWindow("����", 0, 0, 400, 400, false, true);		
							eva.jJDesktopPane.add(jServerPresentWindow, 0);	
							
							jServerPresentWindow.setLocation((eva.jJFrame.getContentPane().getSize().width / 2) - (jServerPresentWindow.getContentPane().getSize().width / 2), (eva.jJFrame.getContentPane().getSize().height / 2) - (jServerPresentWindow.getContentPane().getSize().height / 2));
							jServerPresentWindow.txt_UserName.setText(txt_UserName.getText());
						} else {
							eva.errorMsg(eva.blankSetUser);
						}
					} else {
						eva.errorMsg(eva.NoServerStartMSG);
					}
				}
			});
		    btn_Poly = new JButton("����");
		    btn_Poly.setToolTipText("������ ������ ���Ž�ŵ�ϴ�.");
		    btn_Poly.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					if (eva.isServerStarted) {
						if (!txt_UserName.getText().equalsIgnoreCase("")) {
							if (!txt_UserName.getText().equalsIgnoreCase("")) {
								jServerPolyWindow = new ServerPolyWindow("����", 0, 0, 400, 400, false, true);		
								eva.jJDesktopPane.add(jServerPolyWindow, 0);	
								
								jServerPolyWindow.setLocation((eva.jJFrame.getContentPane().getSize().width / 2) - (jServerPolyWindow.getContentPane().getSize().width / 2), (eva.jJFrame.getContentPane().getSize().height / 2) - (jServerPolyWindow.getContentPane().getSize().height / 2));
								jServerPolyWindow.txt_UserName.setText(txt_UserName.getText());
							} else {
								eva.errorMsg(eva.blankSetUser);
							}
						} else {
							eva.errorMsg(eva.blankSetUser);
						}
					} else {
						eva.errorMsg(eva.NoServerStartMSG);
					}
				}
			});
		    btn_AllPresent = new JButton("��ü����");
		    btn_AllPresent.setToolTipText("����� ��� �������� ������ �����մϴ�.");
		    btn_AllPresent.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					if (eva.isServerStarted) {						
						jServerPresentWindow = new ServerPresentWindow("��ü����", 0, 0, 400, 400, false, true);		
						eva.jJDesktopPane.add(jServerPresentWindow, 0);	
						
						jServerPresentWindow.setLocation((eva.jJFrame.getContentPane().getSize().width / 2) - (jServerPresentWindow.getContentPane().getSize().width / 2), (eva.jJFrame.getContentPane().getSize().height / 2) - (jServerPresentWindow.getContentPane().getSize().height / 2));
						jServerPresentWindow.txt_UserName.setText("��ü����");						
					} else {
						eva.errorMsg(eva.NoServerStartMSG);
					}
				}
			});
		    btn_AllPoly = new JButton("��ü����");
		    btn_AllPoly.setToolTipText("����� ��� ������ ���Ž�ŵ�ϴ�.");
		    btn_AllPoly.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					if (eva.isServerStarted) {
						if (!txt_UserName.getText().equalsIgnoreCase("")) {
							jServerPolyWindow = new ServerPolyWindow("��ü����", 0, 0, 400, 400, false, true);		
							eva.jJDesktopPane.add(jServerPolyWindow, 0);	
							
							jServerPolyWindow.setLocation((eva.jJFrame.getContentPane().getSize().width / 2) - (jServerPolyWindow.getContentPane().getSize().width / 2), (eva.jJFrame.getContentPane().getSize().height / 2) - (jServerPolyWindow.getContentPane().getSize().height / 2));
							jServerPolyWindow.txt_UserName.setText("��ü����");
						} else {
							eva.errorMsg(eva.blankSetUser);
						}
					} else {
						eva.errorMsg(eva.NoServerStartMSG);
					}
				}
			});
		    
		    chk_Infomation = new JCheckBox("����");
		    chk_Infomation.setSelected(true);
		    chk_Infomation.setToolTipText("üũ�Ͻø� �κ�, â��, ���, ���� ������ �ٷ� Ȯ�� ����!");
		    
			pScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			pScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			
			jJTabbedPane1.addTab("��������Ʈ", pScroll);
			
			String[] model1ColName = { "", "", "", "" };
			String[] model2ColName = { "��ȣ", "��Ī", "����", "��æ", "�Ӽ�", "�ܰ�" };
			String[] model5ColName = { "����", "������", "ĳ���͸�", "����", "����", "����" };
			
			model1 = new DefaultTableModel(model1ColName, 0);
			model2 = new DefaultTableModel(model2ColName, 0);
			model3 = new DefaultTableModel(model2ColName, 0);
			model4 = new DefaultTableModel(model2ColName, 0);
			model5 = new DefaultTableModel(model5ColName, 0);
			
			jJTable1 = new JTable(model1);
			
			jJTable2 = new JTable(model2);
			jJTable2.setAutoCreateRowSorter(true);
			TableRowSorter sorter2 = new TableRowSorter(jJTable2.getModel());
			jJTable2.setRowSorter(sorter2);
			jJTable2.getColumnModel().getColumn(0).setPreferredWidth(70);
			jJTable2.getColumnModel().getColumn(1).setPreferredWidth(180);
			jJTable2.getColumnModel().getColumn(2).setPreferredWidth(40);
			jJTable2.getColumnModel().getColumn(3).setPreferredWidth(40);
			jJTable2.getColumnModel().getColumn(4).setPreferredWidth(50);
			jJTable2.getColumnModel().getColumn(5).setPreferredWidth(100);
			
			jJTable3 = new JTable(model3);
			jJTable3.setAutoCreateRowSorter(true);
			TableRowSorter sorter3 = new TableRowSorter(jJTable3.getModel());
			jJTable3.setRowSorter(sorter3);
			jJTable3.getColumnModel().getColumn(0).setPreferredWidth(70);
			jJTable3.getColumnModel().getColumn(1).setPreferredWidth(180);
			jJTable3.getColumnModel().getColumn(2).setPreferredWidth(40);
			jJTable3.getColumnModel().getColumn(3).setPreferredWidth(40);
			jJTable3.getColumnModel().getColumn(4).setPreferredWidth(50);
			jJTable3.getColumnModel().getColumn(5).setPreferredWidth(100);
			
			jJTable4 = new JTable(model4);
			jJTable4.setAutoCreateRowSorter(true);
			TableRowSorter sorter4 = new TableRowSorter(jJTable4.getModel());
			jJTable4.setRowSorter(sorter4);
			jJTable4.getColumnModel().getColumn(0).setPreferredWidth(70);
			jJTable4.getColumnModel().getColumn(1).setPreferredWidth(180);
			jJTable4.getColumnModel().getColumn(2).setPreferredWidth(40);
			jJTable4.getColumnModel().getColumn(3).setPreferredWidth(40);
			jJTable4.getColumnModel().getColumn(4).setPreferredWidth(50);
			jJTable4.getColumnModel().getColumn(5).setPreferredWidth(100);
			
			jJTable5 = new JTable(model5);
			jJTable5.setAutoCreateRowSorter(true);
			TableRowSorter sorter5 = new TableRowSorter(jJTable5.getModel());
			jJTable5.setRowSorter(sorter5);
			jJTable5.getColumnModel().getColumn(0).setPreferredWidth(100);
			jJTable5.getColumnModel().getColumn(1).setPreferredWidth(100);
			jJTable5.getColumnModel().getColumn(2).setPreferredWidth(90);
			jJTable5.getColumnModel().getColumn(3).setPreferredWidth(80);
			jJTable5.getColumnModel().getColumn(4).setPreferredWidth(70);
			jJTable5.getColumnModel().getColumn(5).setPreferredWidth(40);
	
			jJTable1.setEnabled(false);
			jJTable2.setEnabled(false);
			jJTable3.setEnabled(false);
			jJTable4.setEnabled(false);
			jJTable5.setEnabled(false);
			
			jJScrollPane1 = new JScrollPane(jJTable1);
			jJScrollPane2 = new JScrollPane(jJTable2);
			jJScrollPane3 = new JScrollPane(jJTable3);
			jJScrollPane4 = new JScrollPane(jJTable4);
			jJScrollPane5 = new JScrollPane(jJTable5);
			
			jJScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jJScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		    
			jJScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jJScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		    
			jJScrollPane3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jJScrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		    
			jJScrollPane4.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jJScrollPane4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			
			jJScrollPane5.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jJScrollPane5.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			
			jJTabbedPane2.addTab("����", jJScrollPane1);
			jJTabbedPane2.addTab("�κ�", jJScrollPane2);
			jJTabbedPane2.addTab("â��", jJScrollPane3);
			jJTabbedPane2.addTab("���", jJScrollPane4);
			jJTabbedPane2.addTab("����", jJScrollPane5);
						    
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			
			GroupLayout.SequentialGroup main_horizontal_grp = layout.createSequentialGroup();
			
			GroupLayout.SequentialGroup horizontal_grp = layout.createSequentialGroup();
			GroupLayout.SequentialGroup vertical_grp   = layout.createSequentialGroup();
			
			GroupLayout.ParallelGroup main = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col1 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col3 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col4 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col5 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col6 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col7 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			GroupLayout.ParallelGroup col8 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
				
			
			main.addGroup(horizontal_grp);
			main_horizontal_grp.addGroup(main);	
			
			layout.setHorizontalGroup(main_horizontal_grp);
			layout.setVerticalGroup(vertical_grp);
			
			col1.addComponent(txt_UserName, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_Helper, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
			    .addComponent(lbl_UserCount, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE);
			
			col2.addComponent(btn_Search, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Ban, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);
			
//			col3.addComponent(btn_Refresh, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)			
//			    .addComponent(btn_NoChat, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);
//			col4.addComponent(chk_Infomation, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
//		        .addComponent(btn_Present, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);
			
			col3.addComponent(chk_Infomation, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
			    .addComponent(btn_NoChat, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);
			
			col4.addComponent(btn_Present, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);
			col5.addComponent(btn_Poly, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);
			col6.addComponent(btn_AllPresent, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);
			col7.addComponent(btn_AllPoly, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE);		
			
			
			
			horizontal_grp.addGap(5).addGroup(col1).addGap(5);
			horizontal_grp.addGap(5).addGroup(col2).addGap(5);
			horizontal_grp.addGap(5).addGroup(col3).addGap(5);
			horizontal_grp.addGap(5).addGroup(col4).addGap(5);
			horizontal_grp.addGap(5).addGroup(col5).addGap(5);
			horizontal_grp.addGap(5).addGroup(col6).addGap(5);
			horizontal_grp.addGap(5).addGroup(col7).addGap(5);
			horizontal_grp.addGap(5).addGroup(col8).addGap(5);
			
			
			main.addGroup(layout.createSequentialGroup().addComponent(jJTabbedPane1, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
					                                    .addComponent(jJTabbedPane2, GroupLayout.PREFERRED_SIZE, 440, GroupLayout.PREFERRED_SIZE));
//			vertical_grp.addGap(3).addContainerGap().addGroup(layout.createBaselineGroup(false, false).addComponent(txt_UserName)
//					                                                                                  .addComponent(btn_Search)
//					                                                                                  .addComponent(btn_Refresh)
//					                                                                                  .addComponent(chk_Infomation));
			
			vertical_grp.addGap(3).addContainerGap().addGroup(layout.createBaselineGroup(false, false).addComponent(txt_UserName)
																				                      .addComponent(btn_Search)
																				                      .addComponent(chk_Infomation));
			
			vertical_grp.addGap(3).addContainerGap().addGroup(layout.createBaselineGroup(true, true).addComponent(jJTabbedPane1)
					                                                                                .addComponent(jJTabbedPane2));
			
			
			vertical_grp.addGap(3).addContainerGap().addGroup(layout.createBaselineGroup(false, false).addComponent(lbl_Helper)
																									  .addComponent(btn_Ban)
																					                  .addComponent(btn_NoChat)
																					                  .addComponent(btn_Present)
																					                  .addComponent(btn_Poly)
																					                  .addComponent(btn_AllPresent)
																					                  .addComponent(btn_AllPoly));
			
			vertical_grp.addGap(3).addContainerGap().addGroup(layout.createBaselineGroup(false, false).addComponent(lbl_UserCount));
			
		} catch (Exception e) {}
			
	}
	
	private void chatKeyPressed(KeyEvent evt) {
		// ���� ä��
		if (eva.isServerStarted) {
			
			try {				
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					if (txt_UserName.getText().equalsIgnoreCase("")) {
						eva.errorMsg(eva.blankSetUser);
						return;
					}
					L1PcInstance player = L1World.getInstance().getPlayer(txt_UserName.getText());
					
					if (player != null) {
						txt_UserName.setText(player.getName());
						CharacterInfoSearch();
					} else {
						JOptionPane.showMessageDialog(null, txt_UserName.getText() + "���� ���� ����� �������� �ʽ��ϴ�.", "Server Message", JOptionPane.INFORMATION_MESSAGE);
					}
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		} else {
			eva.errorMsg(eva.NoServerStartMSG);
		}
	}
	
	private class MouseListenner extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == 1) {
				int column = ((JTable)e.getSource()).getSelectedColumn();
				int row = ((JTable)e.getSource()).getSelectedRow();
				
				CharacterInfoSearch();
			}
		}
	}
	
	private void CharacterInfoSearch() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		try {
			//txt_UserName.setText((String)model0.getValueAt(row, column));	
			
			if (chk_Infomation.isSelected()) {
				L1PcInstance player = L1World.getInstance().getPlayer(txt_UserName.getText());
				DefaultTableModel tModel1 = (DefaultTableModel)jJTable1.getModel();
				DefaultTableModel tModel2 = (DefaultTableModel)jJTable2.getModel();
				DefaultTableModel tModel3 = (DefaultTableModel)jJTable3.getModel();
				DefaultTableModel tModel4 = (DefaultTableModel)jJTable4.getModel();
				DefaultTableModel tModel5 = (DefaultTableModel)jJTable5.getModel();
				
				for (int i = tModel1.getRowCount() - 1; i >= 0; i--) {
					tModel1.removeRow(i);					
				}
				for (int i = tModel2.getRowCount() - 1; i >= 0; i--) {
					tModel2.removeRow(i);					
				}
				for (int i = tModel3.getRowCount() - 1; i >= 0; i--) {
					tModel3.removeRow(i);					
				}
				for (int i = tModel4.getRowCount() - 1; i >= 0; i--) {
					tModel4.removeRow(i);					
				}
				for (int i = tModel5.getRowCount() - 1; i >= 0; i--) {
					tModel5.removeRow(i);					
				}
				
				if (player != null) {
					int lv = player.getLevel();
					int currentLvExp = ExpTable.getExpByLevel(lv);
					int nextLvExp = ExpTable.getExpByLevel(lv + 1);
					double neededExp = nextLvExp - currentLvExp;
					double currentExp = player.getExp() - currentLvExp;
					int per = (int) ((currentExp / neededExp) * 100.0);
					String ClassName = "";
					
					if (player.isCrown()) {
						ClassName = "����";
					} else if (player.isKnight()) {
						ClassName = "���";
					} else if (player.isElf()) {
						ClassName = "����";
					} else if (player.isDarkelf()) {
						ClassName = "��ũ����";
					} else if (player.isWizard()) {
						ClassName = "������";
					} else if (player.isDragonknight()) {
						ClassName = "����";
					} else if (player.isIllusionist()) {
						ClassName = "ȯ����";
					}
					
					// ���� ���� ��ġ
					double armorWeight = player.getWeightReduction();
					// ������ ���� ��ġ
					int dollWeight = 0;
					for (L1DollInstance doll : player.getDollList().values()) {
						dollWeight = doll.getWeightReductionByDoll();
					}
					// �߰� ���԰��� ��ġ
					double plusWeight = armorWeight + dollWeight;
					
					int hpr = player.getHpr() + player.getInventory().hpRegenPerTick();
					int mpr = player.getMpr() + player.getInventory().mpRegenPerTick();
					
					String[] lbls = { "Class", "Level", 
							          "HP", "MP", 
							          "Weight", "Lawful",
							          "Str(Total/Base)", "Dex(Total/Base)", 
							          "Con(Total/Base)", "Int(Total/Base)",
							          "Wis(Total/Base)","Cha(Total/Base)",
							          "Hpr", "Mpr",
							          "[�Ϲ�]���ݼ���", "[�Ϲ�]�߰�Ÿ��",
							          "[�Ϲ�]Ȱ����ġ", "[�Ϲ�]ȰŸ��ġ",
							          "[����]���ݼ���", "[����]�߰�Ÿ��",
							          "[����]Ȱ����ġ", "[����]ȰŸ��ġ",
							          "[����]Ȱ����ġ", "[����]ȰŸ��ġ",
							          "[�Ϲ�]����������", ""
							         };
					String[] charInfo = new String[4];	
					
					int index = 0;
					for (int i = 0; i < lbls.length; i++) {
						charInfo[0] = lbls[i];
						charInfo[2] = lbls[++i];
						switch (index) {
							case 0:
								charInfo[1] = ClassName;
								charInfo[3] = String.valueOf(player.getLevel()) + " ( " + per + "% )";
								break;
							case 1:
								charInfo[1] = String.valueOf(player.getMaxHp());
								charInfo[3] = String.valueOf(player.getMaxMp());
								break;
							case 2:
								charInfo[1] = String.valueOf(plusWeight);
								charInfo[3] = String.valueOf(player.getLawful());
								break;
							case 3:
								charInfo[1] = String.valueOf(player.getAbility().getTotalStr()) + " / " + player.getAbility().getBaseStr();
								charInfo[3] = String.valueOf(player.getAbility().getTotalDex()) + " / " + player.getAbility().getBaseDex();
								break;
							case 4:
								charInfo[1] = String.valueOf(player.getAbility().getTotalCon()) + " / " + player.getAbility().getBaseCon();
								charInfo[3] = String.valueOf(player.getAbility().getTotalInt()) + " / " + player.getAbility().getBaseInt();
								break;
							case 5:
								charInfo[1] = String.valueOf(player.getAbility().getTotalWis()) + " / " + player.getAbility().getBaseWis();
								charInfo[3] = String.valueOf(player.getAbility().getTotalCha()) + " / " + player.getAbility().getBaseCha();							
								break;
							case 6:
								charInfo[1] = String.valueOf(hpr);
								charInfo[3] = String.valueOf(mpr);
								break;
							case 7:
								charInfo[1] = String.valueOf(player.getHitup());
								charInfo[3] = String.valueOf(player.getDmgup());
								break;
							case 8:
								charInfo[1] = String.valueOf(player.getBowHitup());
								charInfo[3] = String.valueOf(player.getBowDmgup());
								break;
							case 9:
								charInfo[1] = String.valueOf(player.getHitupByArmor());
								charInfo[3] = String.valueOf(player.getDmgupByArmor());
								break;
							case 10:
								charInfo[1] = String.valueOf(player.getBowHitupByArmor());
								charInfo[3] = String.valueOf(player.getBowDmgupByArmor());
								break;
							case 11:
								charInfo[1] = String.valueOf(player.getBowHitupByDoll());
								charInfo[3] = String.valueOf(player.getBowDmgupByDoll());
								break;
							case 12:
								charInfo[1] = String.valueOf(player.getDamageReductionByArmor());
								charInfo[3] = "";
								break;
							default:
								break;
						}
						
						tModel1.addRow(charInfo);
						index++;
					}
					
					
					try {
						con = L1DatabaseFactory.getInstance().getConnection();
						pstm = con.prepareStatement("select * from character_warehouse where account_name = ? ORDER BY 2 DESC, 1 ASC");			
						pstm.setString(1, player.getAccountName());
						rs1 = pstm.executeQuery();
						while (rs1.next()) {
							String[] items = new String[6];						
							items[0] = String.valueOf(rs1.getInt("item_id"));
							items[1] = rs1.getString("item_name");
							items[2] = CommonUtil.numberFormat(rs1.getInt("count"));
							items[3] = String.valueOf(rs1.getInt("enchantlvl"));
							L1ItemInstance temp = ItemTable.getInstance().createItem(rs1.getInt("item_id"));
							temp.setAttrEnchantLevel(rs1.getInt("attr_enchantlvl"));
							items[4] = getAttrName(temp);
							items[5] = getStepName(rs1.getInt("step_enchantlvl"));						
							tModel3.addRow(items);								
						}
					} catch (Exception ex) {	} 
					
					
					try {
						StringBuilder sb = new StringBuilder();
						sb.append("SELECT login, ip, host, phone FROM accounts WHERE ip = ");
						sb.append("(SELECT ip FROM accounts WHERE login = ");
						sb.append("(SELECT account_name FROM characters WHERE char_name = ?))");

						con = L1DatabaseFactory.getInstance().getConnection();
						pstm = con.prepareStatement(sb.toString());
						pstm.setString(1, player.getName());
						rs1 = pstm.executeQuery();
						
						while (rs1.next()) {
							pstm = con.prepareStatement("SELECT char_name, level, highlevel, clanname, onlinestatus FROM characters WHERE account_name = ?");
							pstm.setString(1, rs1.getString("login"));
							rs2 = pstm.executeQuery();
						
							String[] account = new String[6];
							
							account[0] = rs1.getString("login");
							account[1] = rs1.getString("host");
							while (rs2.next()) {
								account[2] = rs2.getString("char_name");
								account[3] = rs2.getInt("level") + " / " + rs2.getInt("highlevel");
								account[4] = rs2.getString("clanname");
								account[5] = rs2.getInt("onlinestatus") == 0 ? "X" : "O";
								
								tModel5.addRow(account);
							}
						}
					} catch (Exception ex) {	} 
					
					
					for (L1ItemInstance item : player.getInventory().getItems()) {					
						String[] items = new String[6];						
						items[0] = String.valueOf(item.getItemId());
						items[1] = item.getName();
						items[2] = String.valueOf(item.getCount());
						items[3] = String.valueOf(item.getEnchantLevel());
						items[4] = getAttrName(item);
						//items[5] = getStepName(item.getStepEnchantLevel());						
						tModel2.addRow(items);
						
						if (item.getItem().getType2() != 0) {
							tModel4.addRow(items);
						}
					}
				}
			}
		} catch (Exception ex) {
			
		} finally {
			SQLUtil.close(rs2);
			SQLUtil.close(rs1);			
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	
	/*
	private class JListHandler implements ListSelectionListener
	{
		// ����Ʈ�� �׸��� ������ �Ǹ�	
		public void valueChanged(ListSelectionEvent event)	
		{	
			Connection con = null;
			PreparedStatement pstm = null;
			ResultSet rs1 = null;
			ResultSet rs2 = null;
			try {
				//txt_UserName.setText((String)listModel.elementAt(jJList.getSelectedIndex()));	
				
				if (chk_Infomation.isSelected()) {
					L1PcInstance player = L1World.getInstance().getPlayer(txt_UserName.getText());
					DefaultTableModel tModel1 = (DefaultTableModel)jJTable1.getModel();
					DefaultTableModel tModel2 = (DefaultTableModel)jJTable2.getModel();
					DefaultTableModel tModel3 = (DefaultTableModel)jJTable3.getModel();
					DefaultTableModel tModel4 = (DefaultTableModel)jJTable4.getModel();
					DefaultTableModel tModel5 = (DefaultTableModel)jJTable5.getModel();
					
					for (int i = tModel1.getRowCount() - 1; i >= 0; i--) {
						tModel1.removeRow(i);					
					}
					for (int i = tModel2.getRowCount() - 1; i >= 0; i--) {
						tModel2.removeRow(i);					
					}
					for (int i = tModel3.getRowCount() - 1; i >= 0; i--) {
						tModel3.removeRow(i);					
					}
					for (int i = tModel4.getRowCount() - 1; i >= 0; i--) {
						tModel4.removeRow(i);					
					}
					for (int i = tModel5.getRowCount() - 1; i >= 0; i--) {
						tModel5.removeRow(i);					
					}
					
					if (player != null) {
						int lv = player.getLevel();
						int currentLvExp = ExpTable.getExpByLevel(lv);
						int nextLvExp = ExpTable.getExpByLevel(lv + 1);
						double neededExp = nextLvExp - currentLvExp;
						double currentExp = player.getExp() - currentLvExp;
						int per = (int) ((currentExp / neededExp) * 100.0);
						String ClassName = "";
						
						if (player.isCrown()) {
							ClassName = "����";
						} else if (player.isKnight()) {
							ClassName = "���";
						} else if (player.isElf()) {
							ClassName = "����";
						} else if (player.isDarkelf()) {
							ClassName = "��ũ����";
						} else if (player.isWizard()) {
							ClassName = "������";
						} else if (player.isDragonknight()) {
							ClassName = "����";
						} else if (player.isIllusionist()) {
							ClassName = "ȯ����";
						}
						
						// ���� ���� ��ġ
						double armorWeight = player.getWeightReduction();
						// ������ ���� ��ġ
						int dollWeight = 0;
						for (L1DollInstance doll : player.getDollList().values()) {
							dollWeight = doll.getWeightReductionByDoll();
						}
						// �߰� ���԰��� ��ġ
						double plusWeight = armorWeight + dollWeight;
						
						int hpr = player.getHpr() + player.getInventory().hpRegenPerTick();
						int mpr = player.getMpr() + player.getInventory().mpRegenPerTick();
						
						String[] lbls = { "Class", "Level", 
								          "HP", "MP", 
								          "Weight", "Lawful",
								          "Str(Total/Base)", "Dex(Total/Base)", 
								          "Con(Total/Base)", "Int(Total/Base)",
								          "Wis(Total/Base)","Cha(Total/Base)",
								          "Hpr", "Mpr",
								          "[�Ϲ�]���ݼ���", "[�Ϲ�]�߰�Ÿ��",
								          "[�Ϲ�]Ȱ����ġ", "[�Ϲ�]ȰŸ��ġ",
								          "[����]���ݼ���", "[����]�߰�Ÿ��",
								          "[����]Ȱ����ġ", "[����]ȰŸ��ġ",
								          "[����]���ݼ���", "[����]�߰�Ÿ��",
								          "[����]Ȱ����ġ", "[����]ȰŸ��ġ",
								          "[�Ϲ�]����������", "",
								          "[�߰�]�߰�Ÿ��", "[�߰�]�߰�Ÿ��Ȯ��",
								          "[�߰�]����������", "[�߰�]����������Ȯ��"
								         };
						String[] charInfo = new String[4];	
						
						int index = 0;
						for (int i = 0; i < lbls.length; i++) {
							charInfo[0] = lbls[i];
							charInfo[2] = lbls[++i];
							switch (index) {
								case 0:
									charInfo[1] = ClassName;
									charInfo[3] = String.valueOf(player.getLevel()) + " ( " + per + "% )";
									break;
								case 1:
									charInfo[1] = String.valueOf(player.getMaxHp());
									charInfo[3] = String.valueOf(player.getMaxMp());
									break;
								case 2:
									charInfo[1] = String.valueOf(plusWeight);
									charInfo[3] = String.valueOf(player.getLawful());
									break;
								case 3:
									charInfo[1] = String.valueOf(player.getAbility().getTotalStr()) + " / " + player.getAbility().getBaseStr();
									charInfo[3] = String.valueOf(player.getAbility().getTotalDex()) + " / " + player.getAbility().getBaseDex();
									break;
								case 4:
									charInfo[1] = String.valueOf(player.getAbility().getTotalCon()) + " / " + player.getAbility().getBaseCon();
									charInfo[3] = String.valueOf(player.getAbility().getTotalInt()) + " / " + player.getAbility().getBaseInt();
									break;
								case 5:
									charInfo[1] = String.valueOf(player.getAbility().getTotalWis()) + " / " + player.getAbility().getBaseWis();
									charInfo[3] = String.valueOf(player.getAbility().getTotalCha()) + " / " + player.getAbility().getBaseCha();							
									break;
								case 6:
									charInfo[1] = String.valueOf(hpr);
									charInfo[3] = String.valueOf(mpr);
									break;
								case 7:
									charInfo[1] = String.valueOf(player.getHitup());
									charInfo[3] = String.valueOf(player.getDmgup());
									break;
								case 8:
									charInfo[1] = String.valueOf(player.getBowHitup());
									charInfo[3] = String.valueOf(player.getBowDmgup());
									break;
								case 9:
									charInfo[1] = String.valueOf(player.getHitupByArmor());
									charInfo[3] = String.valueOf(player.getDmgupByArmor());
									break;
								case 10:
									charInfo[1] = String.valueOf(player.getBowHitupByArmor());
									charInfo[3] = String.valueOf(player.getBowDmgupByArmor());
									break;
								case 11:
									charInfo[1] = String.valueOf(player.getHitupByDoll());
									charInfo[3] = String.valueOf(player.getDmgupByDoll());
									break;
								case 12:
									charInfo[1] = String.valueOf(player.getBowHitupByDoll());
									charInfo[3] = String.valueOf(player.getBowDmgupByDoll());
									break;
								case 13:
									charInfo[1] = String.valueOf(player.getDamageReductionByArmor());
									charInfo[3] = "";
									break;
								case 14:
									charInfo[1] = String.valueOf(player.getAddDamage());
									charInfo[3] = String.valueOf(player.getAddDamageRate());
									break;
								case 15:
									charInfo[1] = String.valueOf(player.getAddReduction());
									charInfo[3] = String.valueOf(player.getAddReductionRate());
									break;
								default:
									break;
							}
							
							tModel1.addRow(charInfo);
							index++;
						}
						
						
						try {
							con = L1DatabaseFactory.getInstance().getConnection();
							pstm = con.prepareStatement("select * from character_warehouse where account_name = ? ORDER BY 2 DESC, 1 ASC");			
							pstm.setString(1, player.getAccountName());
							rs1 = pstm.executeQuery();
							while (rs1.next()) {
								String[] items = new String[6];						
								items[0] = String.valueOf(rs1.getInt("item_id"));
								items[1] = rs1.getString("item_name");
								items[2] = String.valueOf(rs1.getInt("count"));
								items[3] = String.valueOf(rs1.getInt("enchantlvl"));
								L1ItemInstance temp = ItemTable.getInstance().createItem(rs1.getInt("item_id"));
								temp.setAttrEnchantLevel(rs1.getInt("attr_enchantlvl"));
								items[4] = getAttrName(temp);
								items[5] = getStepName(rs1.getInt("step_enchantlvl"));						
								tModel3.addRow(items);								
							}
						} catch (Exception e) {	} 
						
						
						try {
							StringBuilder sb = new StringBuilder();
							sb.append("SELECT login, ip, host, phone FROM accounts WHERE ip = ");
							sb.append("(SELECT ip FROM accounts WHERE login = ");
							sb.append("(SELECT account_name FROM characters WHERE char_name = ?))");

							con = L1DatabaseFactory.getInstance().getConnection();
							pstm = con.prepareStatement(sb.toString());
							pstm.setString(1, player.getName());
							rs1 = pstm.executeQuery();
							
							while (rs1.next()) {
								pstm = con.prepareStatement("SELECT char_name, level, highlevel, clanname, onlinestatus FROM characters WHERE account_name = ?");
								pstm.setString(1, rs1.getString("login"));
								rs2 = pstm.executeQuery();
							
								String[] account = new String[6];
								
								account[0] = rs1.getString("login");
								account[1] = rs1.getString("host");
								while (rs2.next()) {
									account[2] = rs2.getString("char_name");
									account[3] = rs2.getInt("level") + " / " + rs2.getInt("highlevel");
									account[4] = rs2.getString("clanname");
									account[5] = rs2.getInt("onlinestatus") == 0 ? "X" : "O";
									
									tModel5.addRow(account);
								}
							}
						} catch (Exception e) {	} 
						
						
						for (L1ItemInstance item : player.getInventory().getItems()) {					
							String[] items = new String[6];						
							items[0] = String.valueOf(item.getItemId());
							items[1] = item.getName();
							items[2] = String.valueOf(item.getCount());
							items[3] = String.valueOf(item.getEnchantLevel());
							items[4] = getAttrName(item);
							items[5] = getStepName(item.getStepEnchantLevel());						
							tModel2.addRow(items);
							
							if (item.getItem().getType2() != 0) {
								tModel4.addRow(items);
							}
						}
					}
				}
			} catch (Exception e) {
				
			} finally {
				SQLUtil.close(rs2);
				SQLUtil.close(rs1);			
				SQLUtil.close(pstm);
				SQLUtil.close(con);
			}
		}
	}
	*/
	
	private String getAttrName(L1ItemInstance item) {
		int attrenchantlvl = item.getAttrEnchantLevel();
		String attrname = "";
		
		if (attrenchantlvl > 0 && item.getItem().getType2() == 1) {
			switch (attrenchantlvl) {
				case 1:
					attrname = "��";
					break;
				case 2:
					attrname = "����";
					break;
				case 3:
					attrname = "�̱״Ͻ�";
					break;
				case 4:
					attrname = "��";
					break;
				case 5:
					attrname = "����";
					break;
				case 6:
					attrname = "����";
					break;
				case 7:
					attrname = "�ٶ�";
					break;
				case 8:
					attrname = "��ǳ";
					break;
				case 9:
					attrname = "����";
					break;
				case 10:
					attrname = "��";
					break;
				case 11:
					attrname = "�ı�";
					break;
				case 12:
					attrname = "Ŭ����";
					break;
				default:
					attrname = "����";
					break;
			}
		}
		
		if (attrenchantlvl > 0 && item.getItem().getType2() == 2) {
			switch (attrenchantlvl) {
				case 1:
					attrname = "������ �� I";
					break;
				case 2:
					attrname = "������ �� II";
					break;
				case 3:
					attrname = "������ �� III";
					break;
				case 4:
					attrname = "������ �� IV";
					break;
				case 5:
					attrname = "������ �� V";
					break;
				default:
					attrname = "����";
					break;
			}
		}
		return attrname;
	}
	
	private String getStepName(int stepenchantlvl) {
		String stepname = "";
		
		if (stepenchantlvl > 0) {
			switch (stepenchantlvl) {
				case 1:
				case 2:
				case 3:
					stepname = "��" + stepenchantlvl + "�ܰ�";
					break;
				case 4:
				case 5:
				case 6:
					stepname = "��ø" + (stepenchantlvl - 3) + "�ܰ�";
					break;
				case 7:
				case 8:
				case 9:
					stepname = "ü��" + (stepenchantlvl - 6) + "�ܰ�";
					break;
				case 10:
				case 11:
				case 12:
					stepname = "����" + (stepenchantlvl - 9) + "�ܰ�";
					break;
				case 13:
				case 14:
				case 15:
					stepname = "����" + (stepenchantlvl - 12) + "�ܰ�";
					break;
				case 16:
				case 17:
				case 18:
					stepname = "�ŷ�" + (stepenchantlvl - 15) + "�ܰ�";
					break;
				case 19:
				case 20:
				case 21:
					stepname = "����������" + (stepenchantlvl - 18) + "�ܰ�";
					break;
				case 22:
				case 23:
				case 24:
					stepname = "���԰���" + (stepenchantlvl - 21) + "�ܰ�";
					break;
				case 25:
				case 26:
				case 27:
					stepname = "���ݼ���" + (stepenchantlvl - 24) + "�ܰ�";
					break;
				case 28:
				case 29:
				case 30:
					stepname = "�߰�Ÿ��" + (stepenchantlvl - 27) + "�ܰ�";
					break;
				case 31:
				case 32:
				case 33:
					stepname = "Ȱ����ġ" + (stepenchantlvl - 30) + "�ܰ�";
					break;
				case 34:
				case 35:
				case 36:
					stepname = "ȰŸ��ġ" + (stepenchantlvl - 33) + "�ܰ�";
					break;
				default:
					stepname = "����";
					break;
			}
		}
		return stepname;
	}

	/*
	public synchronized DefaultListModel getListModel() {
		return listModel;
	}

	public void setListModel(DefaultListModel listModel) {
		this.listModel = listModel;
	}
	*/
	
	public synchronized DefaultTableModel getTableModel() {
		return model0;
	}

	public void setListModel(DefaultTableModel tableModel) {
		this.model0 = tableModel;
	}

	public JLabel getLbl_UserCount() {
		return lbl_UserCount;
	}

	public void setLbl_UserCount(JLabel lbl_UserCount) {
		this.lbl_UserCount = lbl_UserCount;
	}
}
