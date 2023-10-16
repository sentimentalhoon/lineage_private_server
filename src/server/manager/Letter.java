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
 * 편지 다이얼로그
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

        setTitle("편지보내기");
        setModal(true);
        setResizable(false);

        jLabel1.setText("받는캐릭명");

        jButton1.setText("검색");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton2.setText("보내기");
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

    // 검색
    private void jButton1ActionPerformed(ActionEvent evt) {
    	loadCharacters();
    	String name = dialogShow();
		if(name != null && name.length() > 0) jTextField1.setText(name);
    }

    // 보내기
    private void jButton2ActionPerformed(ActionEvent evt) {
    	String name = jTextField1.getText();
    	// 이름이 없다면..
    	if(name == null || name.length() < 1){
    		errorMessage("보낼 캐릭명을 입력하세요.");
    	}else{
    		String subject = jTextField2.getText();
    		String memo = jTextArea1.getText();
    		// 내용 길이 체크
    		if(subject == null || subject.length() < 1){
    			errorMessage("보낼 편지 제목을 입력하세요.");
    			return;
    		}
    		// 내용 길이 체크
    		if(memo == null || memo.length() < 1){
    			errorMessage("보낼 편지 내용을 입력하세요.");
    			return;
    		}
    		if(memo != null && memo.length() > 290){
    			errorMessage("편지가 너무 깁니다.");
    			return;
    		}
    	//	itemUse.letterWrite("운영자", name, subject, memo);
    		setVisible(false);
    	}
    }
    
    // 에러..
	public void errorMessage(String msg){
		JOptionPane.showMessageDialog(this, msg, "경고", JOptionPane.ERROR_MESSAGE);
	}
    
    // 다이얼로그 쇼..
	private String dialogShow(){
		String tmp = null;
		if(tmpArray.size() > 1){
			String[] s = tmpArray.toArray(new String[tmpArray.size()]);
			tmp = (String) JOptionPane.showInputDialog(this, "대상을 선택하세요.", "검색결과", JOptionPane.QUESTION_MESSAGE, null, s, s[0]);
		}
		tmpArray.clear();
		return tmp;
	}
    
	// 캐릭터 로드
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
