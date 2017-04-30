package dsn;
import java.awt.BorderLayout;
import java.awt.Container;  
import java.awt.GridLayout;
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;

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
    //代理登录界面
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
  	public JButton btnStartGrabGD115;
  	public JButton btnStopGrabGD115; 
  	public JButton btnStartGrabBJKL8;
  	public JButton btnStopGrabBJKL8;
  	
  	GrabBJSCthread grabBJSCthread;
  	GrabCQSSCthread grabCQSSCthread;
  	GrabXYNCthread grabXYNCthread;
  	GrabGXKLthread grabGXKLthread;
  	GrabGDKLthread grabGDKLthread;
  	GrabXJSSCthread grabXJSSCthread;
  	GrabTJSSCthread grabTJSSCthread;
  	GrabGD115thread grabGD115thread;
  	GrabBJKL8thread grabBJKL8thread;
  	
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
        JLabel labelAddress = new JLabel("网址 "); 
        JLabel labelAccount = new JLabel("账号 "); ;
        JLabel labelPassword = new JLabel("密码");
        
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
      	JButton btnLogin = new JButton("登录");
		btnLogin.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(loginToProxySuccess == true)
					return;
				
				String address = textFieldProxyAddress.getText();
				String account = textFieldProxyAccount.getText();
				String password = textFieldProxyPassword.getText();
				
				DsnProxyGrab.setLoginParams(address, account, password);
				
				if(!DsnProxyGrab.login()) {
					System.out.println("登录失败");
					return;
				}
				
				System.out.println("登录成功");
				
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
				btnStartGrabGD115.setEnabled(true);
				btnStopGrabGD115.setEnabled(true);
				btnStartGrabBJKL8.setEnabled(true);
				btnStopGrabBJKL8.setEnabled(true);
				
				
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
				
				grabGD115thread = new GrabGD115thread();
				grabGD115thread.start();
				
				grabBJKL8thread = new GrabBJKL8thread();
				grabBJKL8thread.start();
				
			  	btnStopGrabCQSSC.setBackground(Color.red);
			  	btnStopGrabBJSC.setBackground(Color.red);
			  	btnStopGrabXYNC.setBackground(Color.red);
			  	btnStopGrabGXKL.setBackground(Color.red);
			  	btnStopGrabGDKL.setBackground(Color.red);
			  	btnStopGrabXJSSC.setBackground(Color.red);
			  	btnStopGrabTJSSC.setBackground(Color.red); 
			  	btnStopGrabGD115.setBackground(Color.red); 
			  	btnStopGrabBJKL8.setBackground(Color.red);
				
				loginToProxySuccess = true;
				ConfigWriter.updateProxyAddress(address);
				ConfigWriter.updateProxyAccount(account);
				ConfigWriter.updateProxyPassword(password);
				
				ConfigWriter.saveTofile("common.config");
			}
		});
		
		btnLogin.setSize(75, 25);
		btnLogin.setLocation(10, 100);
      	
      	btnStartGrabCQSSC = new JButton("开抓重庆时彩");
      	btnStartGrabCQSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabCQSSCthread.startGrabCQSSC();
				btnStartGrabCQSSC.setBackground(Color.green);
				btnStopGrabCQSSC.setBackground(null);
			}
      	});
		btnStartGrabCQSSC.setSize(120, 25);
		btnStartGrabCQSSC.setLocation(10, 130);
		btnStopGrabCQSSC = new JButton("停抓重庆时彩");
		btnStopGrabCQSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabCQSSCthread.stopGrabCQSSC();
				btnStartGrabCQSSC.setBackground(null);
				btnStopGrabCQSSC.setBackground(Color.red);
			}
      	});
		btnStopGrabCQSSC.setSize(120, 25);
		btnStopGrabCQSSC.setLocation(130, 130);
		
		btnStartGrabBJSC = new JButton("开抓北京赛车");
		btnStartGrabBJSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabBJSCthread.startGrabBJSC();
				btnStartGrabBJSC.setBackground(Color.green);
				btnStopGrabBJSC.setBackground(null);
			}
      	});
		btnStartGrabBJSC.setSize(120, 25);
		btnStartGrabBJSC.setLocation(10, 160);
		btnStopGrabBJSC = new JButton("停抓北京赛车");
		btnStopGrabBJSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabBJSCthread.stopGrabBJSC();
				btnStartGrabBJSC.setBackground(null);
				btnStopGrabBJSC.setBackground(Color.red);
			}
      	});
		btnStopGrabBJSC.setSize(120, 25);
		btnStopGrabBJSC.setLocation(130, 160);
		
		btnStartGrabXYNC = new JButton("开抓幸运农场");
      	btnStartGrabXYNC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXYNCthread.startGrabXYNC();
				btnStartGrabXYNC.setBackground(Color.green);
				btnStopGrabXYNC.setBackground(null);
			}
      	});
		btnStartGrabXYNC.setSize(120, 25);
		btnStartGrabXYNC.setLocation(10, 190);
		
		btnStopGrabXYNC = new JButton("停抓幸运农场");
		btnStopGrabXYNC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXYNCthread.stopGrabXYNC();
				btnStartGrabXYNC.setBackground(null);
				btnStopGrabXYNC.setBackground(Color.red);
			}
      	});
		btnStopGrabXYNC.setSize(120, 25);
		btnStopGrabXYNC.setLocation(130, 190);
		
		btnStartGrabGXKL = new JButton("开抓广西快乐");
      	btnStartGrabGXKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGXKLthread.startGrabGXKL();
				btnStartGrabGXKL.setBackground(Color.green);
				btnStopGrabGXKL.setBackground(null);
			}
      	});
		btnStartGrabGXKL.setSize(120, 25);
		btnStartGrabGXKL.setLocation(10, 220);
		
		btnStopGrabGXKL = new JButton("停抓广西快乐");
		btnStopGrabGXKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGXKLthread.stopGrabGXKL();
				btnStartGrabGXKL.setBackground(null);
				btnStopGrabGXKL.setBackground(Color.red);
			}
      	});
		btnStopGrabGXKL.setSize(120, 25);
		btnStopGrabGXKL.setLocation(130, 220);
		
		btnStartGrabGDKL = new JButton("开抓广东快乐");
      	btnStartGrabGDKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGDKLthread.startGrabGDKL();
				btnStartGrabGDKL.setBackground(Color.green);
				btnStopGrabGDKL.setBackground(null);
			}
      	});
		btnStartGrabGDKL.setSize(120, 25);
		btnStartGrabGDKL.setLocation(10, 250);
		
		btnStopGrabGDKL = new JButton("停抓广东快乐");
		btnStopGrabGDKL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGDKLthread.stopGrabGDKL();
				btnStartGrabGDKL.setBackground(null);
				btnStopGrabGDKL.setBackground(Color.red);
			}
      	});
		btnStopGrabGDKL.setSize(120, 25);
		btnStopGrabGDKL.setLocation(130, 250);
		
		btnStartGrabXJSSC = new JButton("开抓新疆时彩");
      	btnStartGrabXJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXJSSCthread.startGrabXJSSC();
				btnStartGrabXJSSC.setBackground(Color.green);
				btnStopGrabXJSSC.setBackground(null);
			}
      	});
		btnStartGrabXJSSC.setSize(120, 25);
		btnStartGrabXJSSC.setLocation(10, 280);
		
		btnStopGrabXJSSC = new JButton("停抓新疆时彩");
		btnStopGrabXJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabXJSSCthread.stopGrabXJSSC();
				btnStartGrabXJSSC.setBackground(null);
				btnStopGrabXJSSC.setBackground(Color.red);
			}
      	});
		btnStopGrabXJSSC.setSize(120, 25);
		btnStopGrabXJSSC.setLocation(130, 280);
		
		btnStartGrabTJSSC = new JButton("开抓天津时彩");
      	btnStartGrabTJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabTJSSCthread.startGrabTJSSC();
				btnStartGrabTJSSC.setBackground(Color.green);
				btnStopGrabTJSSC.setBackground(null);
			}
      	});
		btnStartGrabTJSSC.setSize(120, 25);
		btnStartGrabTJSSC.setLocation(10, 310);
		
		btnStopGrabTJSSC = new JButton("停抓天津时彩");
		btnStopGrabTJSSC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabTJSSCthread.stopGrabTJSSC();
				btnStartGrabTJSSC.setBackground(null);
				btnStopGrabTJSSC.setBackground(Color.red);
			}
      	});
		btnStopGrabTJSSC.setSize(120, 25);
		btnStopGrabTJSSC.setLocation(130, 310);
		
		btnStartGrabGD115 = new JButton("开抓广东11选5");
      	btnStartGrabGD115.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGD115thread.startGrabGD115();
				btnStartGrabGD115.setBackground(Color.green);
				btnStopGrabGD115.setBackground(null);
			}
      	});
		btnStartGrabGD115.setSize(120, 25);
		btnStartGrabGD115.setLocation(10, 340);
		
		btnStopGrabGD115 = new JButton("停抓广东11选5");
		btnStopGrabGD115.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabGD115thread.stopGrabGD115();
				btnStartGrabGD115.setBackground(null);
				btnStopGrabGD115.setBackground(Color.red);
			}
      	});
		btnStopGrabGD115.setSize(120, 25);
		btnStopGrabGD115.setLocation(130, 340);
		
		btnStartGrabBJKL8 = new JButton("开抓北京快乐8");
      	btnStartGrabBJKL8.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabBJKL8thread.startGrabBJKL8();
				btnStartGrabBJKL8.setBackground(Color.green);
				btnStopGrabBJKL8.setBackground(null);
			}
      	});
		btnStartGrabBJKL8.setSize(120, 25);
		btnStartGrabBJKL8.setLocation(10, 370);
		
		btnStopGrabBJKL8 = new JButton("停抓北京快乐8");
		btnStopGrabBJKL8.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				grabBJKL8thread.stopGrabBJKL8();
				btnStartGrabBJKL8.setBackground(null);
				btnStopGrabBJKL8.setBackground(Color.red);
			}
      	});
		btnStopGrabBJKL8.setSize(120, 25);
		btnStopGrabBJKL8.setLocation(130, 370);
		
		
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
		btnStartGrabGD115.setEnabled(false);
		btnStopGrabGD115.setEnabled(false);
		btnStartGrabBJKL8.setEnabled(false);
		btnStopGrabBJKL8.setEnabled(false);

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
      	container.add(btnStartGrabGD115);
      	container.add(btnStopGrabGD115);
      	container.add(btnStartGrabBJKL8);
      	container.add(btnStopGrabBJKL8);
      	
      	container.add(btnLogin);
      	
      	//table   	
        
        Vector<String> tableColumnNames = new Vector<String>();  
        tableColumnNames.add("IP");  
        tableColumnNames.add("会员账号");
        tableColumnNames.add("网站");
        
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
        
        setTitle("迪斯尼");  
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
                    	int n = JOptionPane.showConfirmDialog(null, "确认退出吗?", "退出程序", JOptionPane.YES_NO_OPTION);  
                        if (n == JOptionPane.YES_OPTION) {
                        	System.exit(0);  
                        }
                        else {
                        	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                        }
                    }
                }
        );
  	}
  	
  	public void updateTable(Vector<Vector<String>> conns) {
  		 ((DefaultTableModel)tableConns.getModel()).getDataVector().clear();
  		 
  		 Vector<String> tableColumnNames = new Vector<String>();  
         tableColumnNames.add("IP");  
         tableColumnNames.add("会员账号");
         tableColumnNames.add("会员类型");
         
         DefaultTableModel defaultTableModel = new DefaultTableModel(conns, tableColumnNames);
         tableConns.setModel(defaultTableModel);
  	}
}