package server.manager;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.clientpackets.C_ItemUSe;
import l1j.server.server.model.Instance.L1PcInstance;

/**
 * ���� ���̾�α�
 * @author chocco
 */
public class Letter extends JDialog {
	
    private JButton jButton1;
    private JButton jButton2;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;
    private JTextField jTextField2;
    private JTextField jTextField1;
    private Frame father;
    private ArrayList<String> tmpArray;
  //  private ItemUse itemUse = new ItemUse();
    
    public Letter(Frame parent, boolean modal) {
        super(parent, modal);
        try{
			this.father = parent;
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){}
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    private void initComponents(){
    	tmpArray = new ArrayList<String>();
    	jLabel1 = new JLabel();
        jTextField1 = new JTextField();
        jButton1 = new JButton();
        jScrollPane1 = new JScrollPane();
        jTextArea1 = new JTextArea();
        jButton2 = new JButton();
        jTextField2 = new JTextField();

        setTitle("����������");
        setModal(true);
        setResizable(false);

        jLabel1.setText("�޴�ĳ����");

        jButton1.setText("�˻�");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton2.setText("������");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField2, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .addComponent(jButton2, GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    // �˻�
    private void jButton1ActionPerformed(ActionEvent evt) {
    	loadCharacters();
    	String name = dialogShow();
		if(name != null && name.length() > 0) jTextField1.setText(name);
    }

    // ������
    private void jButton2ActionPerformed(ActionEvent evt) {
    	String name = jTextField1.getText();
    	// �̸��� ���ٸ�..
    	if(name == null || name.length() < 1){
    		errorMessage("���� ĳ������ �Է��ϼ���.");
    	}else{
    		String subject = jTextField2.getText();
    		String memo = jTextArea1.getText();
    		// ���� ���� üũ
    		if(subject == null || subject.length() < 1){
    			errorMessage("���� ���� ������ �Է��ϼ���.");
    			return;
    		}
    		// ���� ���� üũ
    		if(memo == null || memo.length() < 1){
    			errorMessage("���� ���� ������ �Է��ϼ���.");
    			return;
    		}
    		if(memo != null && memo.length() > 290){
    			errorMessage("������ �ʹ� ��ϴ�.");
    			return;
    		}
    	//	itemUse.letterWrite("���", name, subject, memo);
    		setVisible(false);
    	}
    }
    
    // ����..
	public void errorMessage(String msg){
		JOptionPane.showMessageDialog(this, msg, "���", JOptionPane.ERROR_MESSAGE);
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
}
