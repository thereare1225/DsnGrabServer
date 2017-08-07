package dsn;
class GrabCQSSCthread extends Thread{
	long almostTime = 35*1000;  //进入加速时间
	long sleepTime = 10*1000;	//平时睡眠时间
    
    boolean grabCQSSC = false;
    boolean isCQSSCclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inCQSSCgrabTime = true;
    GrabCQSSCwindow gwCQSSC;
    DsnProxyGrab dsnProxyGrab;
    public GrabCQSSCthread(GrabCQSSCwindow gwCQSSC, DsnProxyGrab dsnProxyGrab) {
		this.gwCQSSC = gwCQSSC;
		this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
    		String[] CQSSCTime = {"0", "0", "0"};
			while(true){
				if(!dsnProxyGrab.setDsnTime()) {
					isNeedLogin = true;
				}
				
				if(isNeedLogin) {
					
					if(dsnProxyGrab.getIsisNeedChangeLine() == true){
						dsnProxyGrab.setLinePriority();
					}
					
					int res = dsnProxyGrab.reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3000);
					} else if(res == 1) {
						//todo
						gwCQSSC.setOnlineStatus(false);
						dsnProxyGrab.disableCQSSCData();
						dsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwCQSSC.setOnlineStatus(true);
					}
					
					dsnProxyGrab.setisNeedChangeLine(false);
					dsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long CQSSCremainTime = 0;
				if(dsnProxyGrab.isInCQSSCgrabTime()) {
						inCQSSCgrabTime = true;
				} else {
					if(inCQSSCgrabTime) {
						CQSSCremainTime = -1;
						isCQSSCclose = true;
						gwCQSSC.resetData();
						gwCQSSC.setRemainTime(0);
						dsnProxyGrab.disableCQSSCData();
						inCQSSCgrabTime = false;
					}
				}
				
				if(!grabCQSSC) {
					dsnProxyGrab.disableCQSSCData();
				}
				
				if(grabCQSSC && inCQSSCgrabTime) {
					CQSSCremainTime = gwCQSSC.getRemainTime();
					if(requestTime) {
						CQSSCTime= dsnProxyGrab.getCQSSCTime();
						CQSSCremainTime = Long.parseLong(CQSSCTime[0]);
						if(CQSSCremainTime > 0) {
							System.out.println("[代理]距离重庆时时彩封盘:" + CQSSCremainTime/1000);
							gwCQSSC.setRemainTime(CQSSCremainTime);
						}
						else {
							System.out.println("[代理]距离重庆时时彩开盘:" + Long.parseLong(CQSSCTime[2])/1000);
							gwCQSSC.setRemainTime(Long.parseLong(CQSSCTime[2]));
						}
						
					}
					while(CQSSCremainTime > 10*60*1000) {//获取时间失败
						if(!dsnProxyGrab.isInCQSSCgrabTime()) {
							CQSSCremainTime = -1;
							isCQSSCclose = true;
							gwCQSSC.resetData();
							gwCQSSC.setRemainTime(0);
							dsnProxyGrab.disableCQSSCData();
							inCQSSCgrabTime = false;
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
							gwCQSSC.setOnlineStatus(false);
							dsnProxyGrab.disableCQSSCData();
							dsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							dsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwCQSSC.setOnlineStatus(true);
						}
						
						dsnProxyGrab.setisNeedChangeLine(false);
						dsnProxyGrab.clearAvgRequest();
						
						CQSSCTime = dsnProxyGrab.getCQSSCTime();
						CQSSCremainTime = Long.parseLong(CQSSCTime[0]);
					}
					
					if(CQSSCremainTime > 0) {
						if(isCQSSCclose) {
							gwCQSSC.setCloseText(false);
							gwCQSSC.resetData();
							isCQSSCclose = false;
						}
						if(!requestTime) {
							System.out.println("[代理][距离重庆时时彩封盘时间为]:" + CQSSCremainTime/1000);
						}
						if(CQSSCremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(CQSSCremainTime <= 0){
						if(!isCQSSCclose) {
							gwCQSSC.setCloseText(true);
							isCQSSCclose = true;
							requestTime = true;
							sleepTime = 8*1000;
							//dsnProxyGrab.disableCQSSCData();
							continue;
						}
					}
					
					gwCQSSC.setDrawNumber(CQSSCTime[1]);
				}
				
				
				if(grabCQSSC && inCQSSCgrabTime) {
					String data = dsnProxyGrab.grabCQSSCdata("LM", "XZ", "");
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(CQSSCremainTime > 2) {
						dsnProxyGrab.setCQSSCdata(CQSSCTime[1], data, Long.toString(gwCQSSC.getRemainTime()/1000));
					}
					else {
						dsnProxyGrab.setCQSSCdata(CQSSCTime[1], data, Long.toString(CQSSCremainTime/1000));
					}
					String [] datas = {data};
					gwCQSSC.setData(datas);
					
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabCQSSC() {
    	grabCQSSC = true;
    	gwCQSSC.setVisible(true);
    }
    
    public  void stopGrabCQSSC() {
    	grabCQSSC = false;
    	gwCQSSC.resetData();
    	gwCQSSC.setVisible(false);
    }
    
}
