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
    DsnProxyGrab [] dsnProxyGrab;
    public GrabCQSSCthread(GrabCQSSCwindow gwCQSSC, DsnProxyGrab [] dsnProxyGrab) {
		this.gwCQSSC = gwCQSSC;
		this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
    		String[] CQSSCTime = {"0", "0", "0"};
			while(true){
				if(!dsnProxyGrab[0].setDsnTime()) {
					if(dsnProxyGrab[0].getIsisNeedChangeLine() == true){
						dsnProxyGrab[0].setLinePriority();
					}
					
					int res = dsnProxyGrab[0].reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3000);
					} else if(res == 1) {
						//todo
						gwCQSSC.setOnlineStatus(false);
						dsnProxyGrab[0].disableCQSSCData();
						dsnProxyGrab[0].disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab[0].connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwCQSSC.setOnlineStatus(true);
					}
					
					dsnProxyGrab[0].setisNeedChangeLine(false);
					dsnProxyGrab[0].clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				if(!dsnProxyGrab[1].setDsnTime()) {
					if(dsnProxyGrab[1].getIsisNeedChangeLine() == true){
						dsnProxyGrab[1].setLinePriority();
					}
					
					int res = dsnProxyGrab[1].reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3000);
					} else if(res == 1) {
						//todo
						gwCQSSC.setOnlineStatus(false);
						dsnProxyGrab[1].disableCQSSCData();
						dsnProxyGrab[1].disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab[1].connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwCQSSC.setOnlineStatus(true);
					}
					
					dsnProxyGrab[1].setisNeedChangeLine(false);
					dsnProxyGrab[1].clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				if(isNeedLogin) {
					
					if(dsnProxyGrab[0].getIsisNeedChangeLine() == true){
						dsnProxyGrab[0].setLinePriority();
					}
					
					int res = dsnProxyGrab[0].reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3000);
					} else if(res == 1) {
						//todo
						gwCQSSC.setOnlineStatus(false);
						dsnProxyGrab[0].disableCQSSCData();
						dsnProxyGrab[0].disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab[0].connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwCQSSC.setOnlineStatus(true);
					}
					
					dsnProxyGrab[0].setisNeedChangeLine(false);
					dsnProxyGrab[0].clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long CQSSCremainTime = 0;
				if(dsnProxyGrab[0].isInCQSSCgrabTime()) {
						inCQSSCgrabTime = true;
				} else {
					if(inCQSSCgrabTime) {
						CQSSCremainTime = -1;
						isCQSSCclose = true;
						gwCQSSC.resetData();
						gwCQSSC.setRemainTime(0);
						dsnProxyGrab[0].disableCQSSCData();
						inCQSSCgrabTime = false;
					}
				}
				
				if(!grabCQSSC) {
					dsnProxyGrab[0].disableCQSSCData();
				}
				
				if(grabCQSSC && inCQSSCgrabTime) {
					CQSSCremainTime = gwCQSSC.getRemainTime();
					if(requestTime) {
						CQSSCTime= dsnProxyGrab[0].getCQSSCTime();
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
						if(!dsnProxyGrab[0].isInCQSSCgrabTime()) {
							CQSSCremainTime = -1;
							isCQSSCclose = true;
							gwCQSSC.resetData();
							gwCQSSC.setRemainTime(0);
							dsnProxyGrab[0].disableCQSSCData();
							inCQSSCgrabTime = false;
							break;
						}
						
						if(dsnProxyGrab[0].getIsisNeedChangeLine() == true){
							dsnProxyGrab[0].setLinePriority();
						}
						
						int res = dsnProxyGrab[0].reLogin();
						if(res == -1) {
							Thread.currentThread().sleep(3000);
						} else if(res == 1) {
							//todo
							gwCQSSC.setOnlineStatus(false);
							dsnProxyGrab[0].disableCQSSCData();
							dsnProxyGrab[0].disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							dsnProxyGrab[0].connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwCQSSC.setOnlineStatus(true);
						}
						
						dsnProxyGrab[0].setisNeedChangeLine(false);
						dsnProxyGrab[0].clearAvgRequest();
						
						CQSSCTime = dsnProxyGrab[0].getCQSSCTime();
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
							//dsnProxyGrab[0].disableCQSSCData();
							continue;
						}
					}
					
					gwCQSSC.setDrawNumber(CQSSCTime[1]);
				}
				
				
				if(grabCQSSC && inCQSSCgrabTime) {
					String data = dsnProxyGrab[0].grabCQSSCdata("LM", "XZ", "");
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(CQSSCremainTime > 2) {
						dsnProxyGrab[0].setCQSSCdata(CQSSCTime[1], data, Long.toString(gwCQSSC.getRemainTime()/1000));
					}
					else {
						dsnProxyGrab[0].setCQSSCdata(CQSSCTime[1], data, Long.toString(CQSSCremainTime/1000));
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
