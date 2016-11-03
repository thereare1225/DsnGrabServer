import java.awt.BorderLayout;
import java.awt.Container;  
import java.awt.GridLayout;
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;  
import javax.swing.JLabel;  
import javax.swing.JOptionPane;
import javax.swing.JPanel;  
import javax.swing.JScrollPane;  
import javax.swing.JTable;  
import javax.swing.JTextField;  
import javax.swing.JPasswordField;
import javax.swing.JButton;  
import javax.swing.table.DefaultTableModel;  

import java.util.Date;      
import java.util.Vector;

import javax.swing.Timer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicLong;
   
public class DsnGrabUI extends JFrame {  
  

    private static final long serialVersionUID = -8581492063632813033L;
    //�����¼����
  	public JTextField textFieldProxyAddress;
  	public JTextField textFieldProxyAccount;
  	public JPasswordField textFieldProxyPassword;
  	
  	public JButton btnStartGrabCQSSC;
  	public JButton btnStopGrabCQSSC;	
  	public JButton btnStartGrabBJSC;
  	public JButton btnStopGrabBJSC;
  	public JButton btnStartGrabXYNC;
  	public JButton btnStopGrabXYNC;
  	public JButton btnStartGrabGXKL;
  	public JButton btnStopGrabGXKL;
  	public JButton btnStartGrabGDKL;
  	public JButton btnStopGrabGDKL; 
  	public JButton btnStartGrabXJSSC;
  	public JButton btnStopGrabXJSSC; 
  	public JButton btnStartGrabTJSSC;
  	public JButton btnStopGrabTJSSC; 
  	
  	GrabBJSCthread grabBJSCthread;
  	GrabCQSSCthread grabCQSSCthread;
  	GrabXYNCthread grabXYNCthread;
  	GrabGXKLthread grabGXKLthread;
  	GrabGDKLthread grabGDKLthread;
  	GrabXJSSCthread grabXJSSCthread;
  	GrabTJSSCthread grabTJSSCthread;
  	
  	public JTable tableConns;
  	boolean loginToProxySuccess = false;
  	
  	DsnGrabUI() {
  		final Container container = getContentPane();; 
  		ConfigReader.read("common.config");		
		ConfigWriter.open("common.config");
		DsnProxyGrab.initLines();
        
		
		JPanel panel = new JPanel(new GridLayout(3, 1, 1, 1));
        panel.setSize(300, 80);
        panel.setLocation(0, 0);
        container.add(panel);  
        JLabel labelAddress = new JLabel("��ַ "); 
        JLabel labelAccount = new JLabel("�˺� "); ;
        JLabel labelPassword = new JLabel("����");
        
        textFieldProxyAddress = new JTextField();
        textFieldProxyAddress.setSize(150, 20);
       // textFieldProxyAddress.setLocation(60, 50);
      	textFieldProxyAccount = new JTextField();
      	textFieldProxyAccount.setSize(150, 20);
      	//textFieldProxyAccount.setLocation(60, 100);
      	textFieldProxyPassword = new JPasswordField();
      	textFieldProxyPassword.setSize(150, 20);
      	//textFieldProxyPassword.setLocation(60, 150);
 
        panel.add(labelAddress);  
        panel.add(textFieldProxyAddress);
        panel.add(labelAccount);  
        panel.add(textFieldProxyAccount);
        panel.add(labelPassword);
        panel.add(textFieldProxyPassword);
      
        
  	
      	textFieldProxyAddress.setText(ConfigReader.getProxyAddress());
      	textFieldProxyAccount.setText(ConfigReader.getProxyAccount());
      	textFieldProxyPassword.setText(ConfigReader.getProxyPassword());
      	JButton btnLogin = new JButton("��¼");
		btnLogin.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(loginToProxySuccess == true)
					return;
				
				String address = textFieldProxyAddress.getText();
				String account = textFieldProxyAccount.getText();
				String password = textFieldProxyPassword.getText();
				
				DsnProxyGrab.setLoginParams(address, account, password);
				
				if(!DsnProxyGrab.login()) {
					System.out.println("��¼ʧ��");
					return;
				}
				
				System.out.println("��¼�ɹ�");
				
				btnStartGrabCQSSC.setEnabled(true);
				btnStopGrabCQSSC.setEnabled(true);
				btnStartGrabBJSC.setEnabled(true);
				btnStopGrabBJSC.setEnabled(true);
				btnStartGrabXYNC.setEnabled(true);
				btnStopGrabXYNC.setEnabled(true);
				btnStartGrabGXKL.setEnabled(true);
				btnStopGrabGXKL.setEnabled(true);
				btnStartGrabGDKL.setEnabled(true);
				btnStopGrabGDKL.setEnabled(true);
				btnStartGrabXJSSC.setEnabled(true);
				btnStopGrabXJSSC.setEnabled(true);			
				btnStartGrabTJSSC.setEnabled(true);
				btnStopGrabTJSSC.setEnabled(true);
				
				
				grabBJSCthread = new GrabBJSCthread(new GrabBJSCwindow());
				grabBJSCthread.start();
				
				grabCQSSCthread = new GrabCQSSCthread(new GrabCQSSCwindow());
				grabCQSSCthread.start();
				
				grabXYNCthread = new GrabXYNCthread();
				grabXYNCthread.start();
				
				grabGXKLthread = new GrabGXKLthread();
				grabGXKLthread.start();
				
				grabGDKLthread = new GrabGDKLthread();
				grabGDKLthread.start();
				
				grabXJSSCthread = new GrabXJSSCthread();
				grabXJSSCthread.start();
				
				grabTJSSCthread = new GrabTJSSCthread();
				grabTJSSCthread.start();
				
				loginToProxySuccess = true;
				ConfigWriter.updateProxyAddress(address);
				ConfigWriter.updateProxyAccount(account);
				ConfigWriter.updateProxyPassword(password);
				
				ConfigWriter.saveTofile("common.config");
			}
		});
		
		btnLogin.setSize(75, 25);
		btnLogin.setLocation(10, 100);
      	
      	btnStartGrabCQSSC = new JButton("��ץ����ʱ��");
      	btnStartGrabCQSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabCQSSCthread.startGrabCQSSC();
			}
      	});
		btnStartGrabCQSSC.setSize(100, 25);
		btnStartGrabCQSSC.setLocation(10, 130);
		btnStopGrabCQSSC = new JButton("ͣץ����ʱ��");
		btnStopGrabCQSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabCQSSCthread.stopGrabCQSSC();
			}
      	});
		btnStopGrabCQSSC.setSize(100, 25);
		btnStopGrabCQSSC.setLocation(110, 130);
		
		btnStartGrabBJSC = new JButton("��ץ��������");
		btnStartGrabBJSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabBJSCthread.startGrabBJSC();
			}
      	});
		btnStartGrabBJSC.setSize(100, 25);
		btnStartGrabBJSC.setLocation(10, 160);
		btnStopGrabBJSC = new JButton("ͣץ��������");
		btnStopGrabBJSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabBJSCthread.stopGrabBJSC();
			}
      	});
		btnStopGrabBJSC.setSize(100, 25);
		btnStopGrabBJSC.setLocation(110, 160);
		
		btnStartGrabXYNC = new JButton("��ץ����ũ��");
      	btnStartGrabXYNC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXYNCthread.startGrabXYNC();
			}
      	});
		btnStartGrabXYNC.setSize(100, 25);
		btnStartGrabXYNC.setLocation(10, 190);
		
		btnStopGrabXYNC = new JButton("ͣץ����ũ��");
		btnStopGrabXYNC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXYNCthread.stopGrabXYNC();
			}
      	});
		btnStopGrabXYNC.setSize(100, 25);
		btnStopGrabXYNC.setLocation(110, 190);
		
		btnStartGrabGXKL = new JButton("��ץ��������");
      	btnStartGrabGXKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGXKLthread.startGrabGXKL();
			}
      	});
		btnStartGrabGXKL.setSize(100, 25);
		btnStartGrabGXKL.setLocation(10, 220);
		
		btnStopGrabGXKL = new JButton("ͣץ��������");
		btnStopGrabGXKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGXKLthread.stopGrabGXKL();
			}
      	});
		btnStopGrabGXKL.setSize(100, 25);
		btnStopGrabGXKL.setLocation(110, 220);
		
		btnStartGrabGDKL = new JButton("��ץ�㶫����");
      	btnStartGrabGDKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGDKLthread.startGrabGDKL();
			}
      	});
		btnStartGrabGDKL.setSize(100, 25);
		btnStartGrabGDKL.setLocation(10, 250);
		
		btnStopGrabGDKL = new JButton("ͣץ�㶫����");
		btnStopGrabGDKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGDKLthread.stopGrabGDKL();
			}
      	});
		btnStopGrabGDKL.setSize(100, 25);
		btnStopGrabGDKL.setLocation(110, 250);
		
		btnStartGrabXJSSC = new JButton("��ץ�½�ʱʱ��");
      	btnStartGrabXJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXJSSCthread.startGrabXJSSC();
			}
      	});
		btnStartGrabXJSSC.setSize(100, 25);
		btnStartGrabXJSSC.setLocation(10, 280);
		
		btnStopGrabXJSSC = new JButton("ͣץ�½�ʱʱ��");
		btnStopGrabXJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXJSSCthread.stopGrabXJSSC();
			}
      	});
		btnStopGrabXJSSC.setSize(100, 25);
		btnStopGrabXJSSC.setLocation(110, 280);
		
		btnStartGrabTJSSC = new JButton("��ץ���ʱʱ��");
      	btnStartGrabTJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabTJSSCthread.startGrabTJSSC();
			}
      	});
		btnStartGrabTJSSC.setSize(100, 25);
		btnStartGrabTJSSC.setLocation(10, 310);
		
		btnStopGrabTJSSC = new JButton("ͣץ���ʱʱ��");
		btnStopGrabTJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabTJSSCthread.stopGrabTJSSC();
			}
      	});
		btnStopGrabTJSSC.setSize(100, 25);
		btnStopGrabTJSSC.setLocation(110, 310);
		
		
		btnStartGrabCQSSC.setEnabled(false);
		btnStopGrabCQSSC.setEnabled(false);
		btnStartGrabBJSC.setEnabled(false);
		btnStopGrabBJSC.setEnabled(false);
		btnStartGrabXYNC.setEnabled(false);
		btnStopGrabXYNC.setEnabled(false);
		btnStartGrabGXKL.setEnabled(false);
		btnStopGrabGXKL.setEnabled(false);
		btnStartGrabGDKL.setEnabled(false);
		btnStopGrabGDKL.setEnabled(false);
		btnStartGrabTJSSC.setEnabled(false);
		btnStopGrabTJSSC.setEnabled(false);
		btnStartGrabXJSSC.setEnabled(false);
		btnStopGrabXJSSC.setEnabled(false);

//      	container.add(textFieldProxyAddress);  
//      	container.add(textFieldProxyAccount);  
//      	container.add(textFieldProxyPassword); 
      	container.add(btnStartGrabCQSSC);
      	container.add(btnStopGrabCQSSC);
      	container.add(btnStartGrabBJSC);
      	container.add(btnStopGrabBJSC);
      	container.add(btnStartGrabBJSC);
      	container.add(btnStopGrabBJSC);
      	container.add(btnStartGrabXYNC);
      	container.add(btnStopGrabXYNC);
      	container.add(btnStartGrabGXKL);
      	container.add(btnStopGrabGXKL);
      	container.add(btnStartGrabGDKL);
      	container.add(btnStopGrabGDKL);
      	container.add(btnStartGrabXJSSC);
      	container.add(btnStopGrabXJSSC);
      	container.add(btnStartGrabTJSSC);
      	container.add(btnStopGrabTJSSC);
      	
      	container.add(btnLogin);
      	
      	//table   	
        
        Vector<String> tableColumnNames = new Vector<String>();  
        tableColumnNames.add("IP");  
        tableColumnNames.add("��Ա�˺�");
        tableColumnNames.add("��վ");
        
        final DefaultTableModel defaultTableModel = new DefaultTableModel(  
                null, tableColumnNames);  
        tableConns = new JTable(defaultTableModel);  
        JScrollPane scrollPane = new JScrollPane();  
        //scrollPane.setViewportView(tableConns);  
        //scrollPane2.setLocation(600, 75);
        scrollPane.setSize(400, 500);
        scrollPane.setLocation(350, 0);
        scrollPane.setViewportView(tableConns);
        container.add(scrollPane);  
      	//!table
        
        setTitle("��˹��");  
       //pack(); //Realize the components.  
        setBounds(100, 100, 800, 580);  
//      textFieldA.requestFocus();  
        setLayout(null);  
        setVisible(true); //Display the window. 
        //table1.setEnabled(false);              

        this.addWindowListener
        (
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                    	int n = JOptionPane.showConfirmDialog(null, "ȷ���˳���?", "�˳�����", JOptionPane.YES_NO_OPTION);  
                        if (n == JOptionPane.YES_OPTION) {
                        	System.exit(0);  
                        }  
                    }
                }
        );
  	}
  	
  	public void updateTable(Vector<Vector<String>> conns) {
  		 ((DefaultTableModel)tableConns.getModel()).getDataVector().clear();
  		 
  		 Vector<String> tableColumnNames = new Vector<String>();  
         tableColumnNames.add("IP");  
         tableColumnNames.add("��Ա�˺�");
         tableColumnNames.add("��Ա����");
         
         DefaultTableModel defaultTableModel = new DefaultTableModel(conns, tableColumnNames);
         tableConns.setModel(defaultTableModel);
  	}
   
}