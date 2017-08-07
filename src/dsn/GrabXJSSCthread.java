package dsn;
class GrabXJSSCthread extends Thread{
	long almostTime = 35*1000;  //进入加速时间
	long sleepTime = 10*1000;	//平时睡眠时间
    
    boolean grabXJSSC = false;
    boolean isXJSSCclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inXJSSCgrabTime = true;
    GrabXJSSCwindow gwXJSSC;
    DsnProxyGrab dsnProxyGrab;
    public GrabXJSSCthread(DsnProxyGrab dsnProxyGrab) {
    	gwXJSSC = new GrabXJSSCwindow();
    	this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
    		String[] XJSSCTime = {"0", "0", "0"};
			while(true){
				
				if(isNeedLogin) {
					
					if(dsnProxyGrab.getIsisNeedChangeLine() == true){
						dsnProxyGrab.setLinePriority();
					}
					
					int res = dsnProxyGrab.reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3000);
					} else if(res == 1) {
						//todo
						gwXJSSC.setOnlineStatus(false);
						dsnProxyGrab.disableXJSSCData();
						dsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwXJSSC.setOnlineStatus(true);
					}
					
					dsnProxyGrab.setisNeedChangeLine(false);
					dsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long XJSSCremainTime = 0;
				if(dsnProxyGrab.isInXJSSCgrabTime()) {
						inXJSSCgrabTime = true;
				} else {
					if(inXJSSCgrabTime) {
						XJSSCremainTime = -1;
						isXJSSCclose = true;
						gwXJSSC.resetData();
						gwXJSSC.setRemainTime(0);
						dsnProxyGrab.disableXJSSCData();
						inXJSSCgrabTime = false;
					}
				}
				
				if(!grabXJSSC) {
					dsnProxyGrab.disableXJSSCData();
				}
				
				if(grabXJSSC && inXJSSCgrabTime) {
					XJSSCremainTime = dsnProxyGrab.getXJSSClocalRemainTime();
					if(requestTime) {
						XJSSCTime= dsnProxyGrab.getXJSSCtime();
						XJSSCremainTime = Long.parseLong(XJSSCTime[0]);
						if(XJSSCremainTime > 0) {
							System.out.println("[代理]距离新疆时时彩封盘:" + XJSSCremainTime/1000);
							gwXJSSC.setRemainTime(XJSSCremainTime);
						}
						else {
							System.out.println("[代理]距离新疆时时彩开盘:" + Long.parseLong(XJSSCTime[2])/1000);
							gwXJSSC.setRemainTime(Long.parseLong(XJSSCTime[2]));
						}
						
					}
					while(XJSSCremainTime > 20*60*1000) {//获取时间失败
						if(!dsnProxyGrab.isInXJSSCgrabTime()) {
							XJSSCremainTime = -1;
							isXJSSCclose = true;
							gwXJSSC.resetData();
							gwXJSSC.setRemainTime(0);
							dsnProxyGrab.disableXJSSCData();
							inXJSSCgrabTime = false;
							break;
						}
						
						if(dsnProxyGrab.getIsisNeedChangeLine() == true){
							dsnProxyGrab.setLinePriority();
						}
						
						int res = dsnProxyGrab.reLogin();
						if(res == -1) {
							Thread.currentThread().sleep(3000);
						} else if(res == 1) {
							//todo
							gwXJSSC.setOnlineStatus(false);
							dsnProxyGrab.disableXJSSCData();
							dsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							dsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwXJSSC.setOnlineStatus(true);
						}
						
						dsnProxyGrab.setisNeedChangeLine(false);
						dsnProxyGrab.clearAvgRequest();
						
						XJSSCTime = dsnProxyGrab.getXJSSCtime();
						XJSSCremainTime = Long.parseLong(XJSSCTime[0]);
					}
					
					if(XJSSCremainTime > 0) {
						if(isXJSSCclose) {
							gwXJSSC.setCloseText(false);
							gwXJSSC.resetData();
							isXJSSCclose = false;
						}
						if(!requestTime) {
							System.out.println("[代理][距离新疆时时彩盘时间为]:" + XJSSCremainTime/1000);
						}
						if(XJSSCremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(XJSSCremainTime <= 0){
						if(!isXJSSCclose) {
							gwXJSSC.setCloseText(true);
							isXJSSCclose = true;
							requestTime = true;
							sleepTime = 8*1000;
							//dsnProxyGrab.disableXJSSCData();
							continue;
						}
					}
					
					gwXJSSC.setDrawNumber(XJSSCTime[1]);
				}
				
				
				if(grabXJSSC && inXJSSCgrabTime) {
					String data = dsnProxyGrab.grabXJSSCdata();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(XJSSCremainTime > 2) {
						dsnProxyGrab.setXJSSCdata(XJSSCTime[1], data, Long.toString(dsnProxyGrab.getXJSSClocalRemainTime()/1000));
					}
					else {
						dsnProxyGrab.setXJSSCdata(XJSSCTime[1], data, Long.toString(XJSSCremainTime/1000));
					}
					String [] datas = {data};
					gwXJSSC.setData(datas);
					//System.out.println("XJSSC data:" + data);
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabXJSSC() {
    	grabXJSSC = true;
    	gwXJSSC.setVisible(true);
    }
    
    public  void stopGrabXJSSC() {
    	grabXJSSC = false;
    	gwXJSSC.resetData();
    	gwXJSSC.setVisible(false);
    }
    
}
