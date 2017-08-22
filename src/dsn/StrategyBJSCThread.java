package dsn;

import javax.swing.JOptionPane;

public class StrategyBJSCThread extends Thread{
	
	DsnProxyGrab [] dsnProxyGrabs;
	BetBJSCdataFactory betBJSCdataFactory;
	GrabBJSCwindow grabBJSCwindow;
	long remainTime = 0;
	boolean close = true;
	boolean grabBJSC = false;
	boolean init = false;
	
	public StrategyBJSCThread(DsnProxyGrab [] dsnProxyGrabs, BetBJSCdataFactory betBJSCdataFactory, GrabBJSCwindow grabBJSCwindow) {
		this.dsnProxyGrabs = dsnProxyGrabs;
		this.grabBJSCwindow = grabBJSCwindow;
		this.betBJSCdataFactory = betBJSCdataFactory;
		//betBJSCdataFactory = new BetBJSCdataFactory(grabBJSCwindow, dsnProxyGrabs);
	}
	@Override
    public void run() {
    	try {
			while(true){
				try {
					if(grabBJSC && dsnProxyGrabs[0].isInBJSCgrabTime()) {
						for(int i = 0; i < dsnProxyGrabs.length; i++) {
							if(!dsnProxyGrabs[i].isInReLogin) {
								remainTime = dsnProxyGrabs[i].getBJSClocalRemainTime();
								grabBJSCwindow.setDrawNumber(String.valueOf(dsnProxyGrabs[i].getBJSCdrawNumber()));
							}
						}
						
						betBJSCdataFactory.saveBetResToFile(dsnProxyGrabs[0].getBJSCdrawNumber() - 1);
						if(close) {
							if(remainTime > 0) {
								close = false;
								grabBJSCwindow.setCloseText(false);
								grabBJSCwindow.resetData();
							}
						} else {
							if(remainTime > 0) {
								String [] data = betBJSCdataFactory.CombineData();
								if(data == null) {
									Thread.currentThread().sleep(5000);
									continue;
								}
								String [] show = {data[1], data[2], data[3]};
								grabBJSCwindow.setData(show);	
								grabBJSCwindow.setRemainTime(remainTime);
							} else {
								close = true;
							}
						}
	
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
				Thread.currentThread().sleep(10000);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
	
	public  void startGrabBJSC() {
		if(!init) {
			if(betBJSCdataFactory.initBetResDataFile()) {
				grabBJSCwindow.setVisible(true);
				init = true;
			} else {
				System.out.println("betBJSCdataFactory.initBetResDataFile()  失败！！！！！！！！！！！！！");
				JOptionPane.showMessageDialog(null, "北京赛车文件初始化失败  请检查文件并重启程序！", "初始化失败", JOptionPane.ERROR_MESSAGE); 
			}
		}
		
		grabBJSCwindow.setVisible(true);
		grabBJSC = true;
    }
	
	public  void stopGrabBJSC() {
    	grabBJSC = false;
    	grabBJSCwindow.resetData();
    	grabBJSCwindow.setVisible(false);
    }
}
