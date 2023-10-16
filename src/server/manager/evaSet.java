package server.manager;

import java.awt.*;
import javax.swing.*;

import java.awt.event.*;

import javax.swing.event.*;

import server.manager.eva;
import l1j.server.Config;

/**
 * 서버셋팅
 * @author code
 */
@SuppressWarnings("serial")
public class evaSet extends JDialog {
	
	private int MAX;
	private ButtonGroup buttonGroup1;
	private JRadioButton jRadioButton1;
	private JRadioButton jRadioButton2;
	private JSlider jSlider1;
	private JLabel jl1;
	private JLabel jl10;
	private JLabel jl11;
	private JLabel jl12;
	private JLabel jl13;
	private JLabel jl14;
	private JLabel jl2;
	private JLabel jl3;
	private JLabel jl4;
	private JLabel jl5;
	private JLabel jl6;
	private JLabel jl7;
	private JLabel jl8;
	private JLabel jl9;
	private JTextField setting_aden;
	private JTextField setting_chat_lv;
	private JTextField setting_exp;
	private JTextField setting_weapon;
	private JTextField setting_gitol;
	private JTextField setting_item;
	private JTextField setting_max_text;
	private JTextField setting_lawful;
	private JTextField setting_accessory;
	private JTextField setting_clan;
	private JTextField setting_castle;
	private JButton setting_ok;
	private JTextField setting_weight;
	private JTextField setting_armor;
	private int createAccount;
	@SuppressWarnings("unused")
	private Frame father;
	


	public evaSet(Frame parent, boolean modal) {
		super(parent, modal);
		try{
			this.father = parent;
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){}
		initComponents();
	}
	private void initComponents() {

		createAccount = Config.AUTO_CREATE_ACCOUNTS ? 1 : 0;
		buttonGroup1 = new ButtonGroup();
		jl1 = new JLabel();
		jl2 = new JLabel();
		jRadioButton1 = new JRadioButton();
		jRadioButton2 = new JRadioButton();
		jl3 = new JLabel();
		setting_max_text = new JTextField();  // 최대접속자
		jSlider1 = new JSlider();
		setting_chat_lv = new JTextField();  //채팅레벨
		jl4 = new JLabel();
		setting_exp = new JTextField(); // 경험치배율
		jl5 = new JLabel();
		setting_item = new JTextField(); //아이템배율
		jl6 = new JLabel();
		setting_aden = new JTextField(); // 아데나 비율
		jl7 = new JLabel();
		setting_weight = new JTextField(); // 무게비율
		jl8 = new JLabel();
		setting_gitol = new JTextField(); // 깃털비율
		jl9 = new JLabel();
		setting_weapon = new JTextField();  // 무기 인챈
		jl10 = new JLabel();
		setting_lawful = new JTextField(); // 라우풀수치
		jl11 = new JLabel();
		setting_accessory = new JTextField(); // 악세사리
		jl12 = new JLabel();
		setting_clan = new JTextField();  // 혈맹 깃털
		jl13 = new JLabel();
		setting_castle = new JTextField();  // 성혈 깃털
		jl14 = new javax.swing.JLabel();
        setting_armor = new javax.swing.JTextField(); // 방어 인챈
        setting_ok = new JButton();

		setTitle("서버셋팅");
		setResizable(false);
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent evt) {
				formMouseWheelMoved(evt);
			}
		});

		jl1.setText("자동생성타입");
		
		buttonGroup1.add(jRadioButton1);
		jRadioButton1.setText("자동");
		jRadioButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jRadioButton1ActionPerformed(evt);
			}
		});

		buttonGroup1.add(jRadioButton2);
		jRadioButton2.setText("수동");
		jRadioButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jRadioButton2ActionPerformed(evt);
			}
		});
		
		if(createAccount == 1){
			jRadioButton1.setSelected(true);
		}else{
			jRadioButton2.setSelected(true);			
		}

		jl3.setText("최대접속자");

		setting_max_text.setHorizontalAlignment(JTextField.CENTER);
		setting_max_text.setText(""+Config.MAX_ONLINE_USERS);
		setting_max_text.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_max_5000(evt);
			}
		});

		jSlider1.setMaximum(50000);
		jSlider1.setValue(0);
		jSlider1.setFocusable(false);
		jSlider1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				jSlider1StateChanged(evt);
			}
		});

		jl2.setText("채팅레벨");
		
		setting_chat_lv.setHorizontalAlignment(JTextField.CENTER);
		setting_chat_lv.setText(""+Config.GLOBAL_CHAT_LEVEL);
		setting_chat_lv.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_100(evt);
			}
		});

		jl4.setText("경험치");

		setting_exp.setHorizontalAlignment(JTextField.CENTER);
		setting_exp.setText(""+Config.RATE_XP);
		setting_exp.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_max_50000(evt);
			}
		});

		jl5.setText("아이템");

		setting_item.setHorizontalAlignment(JTextField.CENTER);
		setting_item.setText(""+Config.RATE_DROP_ITEMS);
		setting_item.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_500(evt);
			}
		});

		jl6.setText("아데나배율");

		setting_aden.setHorizontalAlignment(JTextField.CENTER);
		setting_aden.setText(""+Config.RATE_DROP_ADENA);
		setting_aden.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_500(evt);
			}
		});

		jl7.setText("무게배율");

		setting_weight.setHorizontalAlignment(JTextField.CENTER);
		setting_weight.setText(""+Config.RATE_WEIGHT_LIMIT);
		setting_weight.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_100(evt);
			}
		});

		jl8.setText("깃털갯수");

		setting_gitol.setHorizontalAlignment(JTextField.CENTER);
		setting_gitol.setText(""+Config.FEATHER_NUMBER);
		setting_gitol.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_500(evt);
			}
		});

		jl9.setText("무기인챈트");

		setting_weapon.setHorizontalAlignment(JTextField.CENTER);
		setting_weapon.setText(""+Config.ENCHANT_CHANCE_WEAPON);
		setting_weapon.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_100(evt);
			}
		});

		jl10.setText("라우풀");
		setting_lawful.setHorizontalAlignment(JTextField.CENTER);
		setting_lawful.setText(""+Config.RATE_LAWFUL);
		setting_lawful.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_100(evt);
			}
		});

		jl11.setText("악세사리");
		setting_accessory.setHorizontalAlignment(JTextField.CENTER);
		setting_accessory.setText(""+Config.ENCHANT_CHANCE_ACCESSORY);
		setting_accessory.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_100(evt);
			}
		});

		jl12.setText("혈맹깃털");

		setting_clan.setHorizontalAlignment(JTextField.CENTER);
		setting_clan.setText(""+Config.CLAN_NUMBER);
		setting_clan.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_500(evt);
			}
		});
		
		jl13.setText("성혈깃털");
		
		setting_castle.setHorizontalAlignment(JTextField.CENTER);
		setting_castle.setText(""+Config.CASTLE_NUMBER);
		setting_castle.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_500(evt);
			}
		});

		setting_ok.setText("저장");
		setting_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setting_okActionPerformed(evt);
			}
		});
		
		jl14.setText("갑옷인챈");

		setting_armor.setHorizontalAlignment(JTextField.CENTER);
		setting_armor.setText(""+Config.ENCHANT_CHANCE_ARMOR);
		setting_armor.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent evt) {
				setting_chat_100(evt);
			}
		});
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jl6)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_aden, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jl7)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_weight, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jl8)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_gitol, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(28, Short.MAX_VALUE))
                        
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jl9)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_weapon, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jl14)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_armor, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                         .addComponent(jl11)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_accessory, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                        
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jl10)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_lawful, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jl12)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_clan, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jl13)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setting_castle, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())    
                        
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                            .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jl1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton2)
                                .addGap(18, 18, 18)
                                .addComponent(jl3)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(setting_max_text, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE))
                            .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jl2, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(setting_chat_lv, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jl4)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(setting_exp, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jl5)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(setting_item, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(setting_ok)
                        .addContainerGap())))
            .addComponent(jSlider1, GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jl1)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jl3)
                    .addComponent(setting_max_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jl2)
                    .addComponent(setting_chat_lv, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl4)
                    .addComponent(setting_exp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl5)
                    .addComponent(setting_item, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jl6)
                    .addComponent(setting_aden, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl7)
                    .addComponent(setting_weight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl8)
                    .addComponent(setting_gitol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jl9)
                    .addComponent(setting_weapon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl14)
                    .addComponent(setting_armor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl11)
                    .addComponent(setting_accessory, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jl10)
                    .addComponent(setting_lawful, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl12)
                    .addComponent(setting_clan, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl13)
                    .addComponent(setting_castle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                
                .addGap(18, 18, 18)
                .addComponent(setting_ok)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

		pack();
	}

	// 폼에 휠로 카운트..
	private void formMouseWheelMoved(MouseWheelEvent evt) {
		if(getFocusOwner() instanceof JTextField){
			JTextField jf = null;
			try{
				jf = (JTextField) getFocusOwner();
				int b = Integer.parseInt(jf.getText());
				int c = 0;
				c = evt.getWheelRotation() == 1 ? b - 1 : b + 1;
				if(c < 0) c = 0;
				else if(c > MAX) c = MAX;
				jSlider1.setValue(c);
				jf.setText(c+"");
			}catch(Exception e){}
			jf = null;
		}
	}

	// 사이드바 이동.
	private void jSlider1StateChanged(ChangeEvent evt) {
		if(getFocusOwner() instanceof JTextField){
			JTextField jf = null;
			try{
				jf = (JTextField) getFocusOwner();
				jf.setText(jSlider1.getValue()+"");
				jSlider1.setToolTipText(Integer.toString(jSlider1.getValue()));
			}catch(Exception e){}
			jf = null;
		}
	}

	// 최대인원. 포커스일때. 
	private void setting_max_5000(FocusEvent evt) {
		if(getFocusOwner() instanceof JTextField){
			JTextField jf = null;
			try{
				jf = (JTextField) getFocusOwner();
				int tmp = Integer.parseInt(jf.getText());
				jSlider1.setMaximum(5000);
				MAX = 5000;
				jSlider1.setValue(tmp);
				jf.setText(jf.getText());
			}catch(Exception e){
			}
		}
	}
	private void setting_max_50000(FocusEvent evt) {
		if(getFocusOwner() instanceof JTextField){
			JTextField jf = null;
			try{
				jf = (JTextField) getFocusOwner();
				int tmp = Integer.parseInt(jf.getText());
				jSlider1.setMaximum(50000);
				MAX = 50000;
				jSlider1.setValue(tmp);
				jf.setText(jf.getText());
			}catch(Exception e){
			}
		}
	}

	// 채팅 레벨.
	private void setting_chat_100(FocusEvent evt) {
		if(getFocusOwner() instanceof JTextField){
			JTextField jf = null;
			try{
				jf = (JTextField) getFocusOwner();
				int tmp = Integer.parseInt(jf.getText());
				jSlider1.setMaximum(100);
				MAX = 100;
				jSlider1.setValue(tmp);
				jf.setText(jf.getText());
			}catch(Exception e){
			}
		}
	}
	
	// 채팅 레벨.
	private void setting_chat_500(FocusEvent evt) {
		if(getFocusOwner() instanceof JTextField){
			JTextField jf = null;
			try{
				jf = (JTextField) getFocusOwner();
				int tmp = Integer.parseInt(jf.getText());
				jSlider1.setMaximum(500);
				MAX = 500;
				jSlider1.setValue(tmp);
				jf.setText(jf.getText());
			}catch(Exception e){
			}
		}
	}

	private void setting_okActionPerformed(ActionEvent evt) {
		Config.GLOBAL_CHAT_LEVEL = Short.parseShort(setting_chat_lv.getText());		
		Config.MAX_ONLINE_USERS = Short.parseShort(setting_max_text.getText());
		Config.RATE_XP = Double.parseDouble(setting_exp.getText());
		Config.RATE_DROP_ADENA = Double.parseDouble(setting_aden.getText());
		Config.RATE_DROP_ITEMS = Double.parseDouble(setting_item.getText());
		Config.FEATHER_NUMBER = Integer.parseInt(setting_gitol.getText());
		Config.CLAN_NUMBER = Integer.parseInt(setting_clan.getText());
		Config.CASTLE_NUMBER = Integer.parseInt(setting_castle.getText());
		Config.RATE_LAWFUL = Double.parseDouble(setting_lawful.getText());
		Config.RATE_WEIGHT_LIMIT = Double.parseDouble(setting_weight.getText());
		Config.ENCHANT_CHANCE_WEAPON = Integer.parseInt(setting_weapon.getText());
		Config.ENCHANT_CHANCE_ARMOR = Integer.parseInt(setting_armor.getText());		
		Config.ENCHANT_CHANCE_ACCESSORY = Integer.parseInt(setting_accessory.getText());
		Config.AUTO_CREATE_ACCOUNTS = (createAccount == 1 ? true : false);
		eva.writeMessage(-1, "서버셋팅 저장완료");
		JOptionPane.showMessageDialog(this, "정상적으로 배율이 변경되었습니다.", " Server Message", javax.swing.JOptionPane.INFORMATION_MESSAGE);
		this.setVisible(false);
		
	}

	
	
	private void jRadioButton1ActionPerformed(ActionEvent evt) {
		createAccount = 1;
	}

	private void jRadioButton2ActionPerformed(ActionEvent evt) {
		createAccount = 0;
	}
}
