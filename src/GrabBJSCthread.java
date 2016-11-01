class GrabBJSCthread extends Thread{
	long almostTime = 40*1000;  //进入加速时间
	long sleepTime = 8*1000;	//平时睡眠时间
    
    boolean grabBJSC = false;
    boolean isBJSCclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inBJSCgrabTime = true;

    GrabBJSCwindow gwBJSC;
    public GrabBJSCthread(GrabBJSCwindow gwBJSC) {
    	this.gwBJSC = gwBJSC;
	}
    
    @Override
    public void run() {
    	try {
			String[] BJSCTime = {"0", "0", "0"};
			while(true){
				if(isNeedLogin) {
					
					if(DsnProxyGrab.getIsisNeedChangeLine() == true){
						DsnProxyGrab.setLinePriority();
					}
					
					int res = DsnProxyGrab.reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3*1000);
					} else if(res == 1) {
						//todo
						gwBJSC.setOnlineStatus(false);
						DsnProxyGrab.disableCQSSCData();
						DsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						DsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwBJSC.setOnlineStatus(true);
					}
					
					DsnProxyGrab.setisNeedChangeLine(false);
					DsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long BJSCremainTime = 0;
				
				if(DsnProxyGrab.isInBJSCgrabTime()) {
					inBJSCgrabTime = true;
				} else {
					if(inBJSCgrabTime) {
						BJSCremainTime = -1;
						isBJSCclose = true;
						//gwBJSC.resetData();
						//gwBJSC.setRemainTime(0);
						DsnProxyGrab.disableBJSCData();
						inBJSCgrabTime = false;
					}
				}
				
				if(!grabBJSC) {
					DsnProxyGrab.disableBJSCData();
				}
				
				if(grabBJSC && inBJSCgrabTime) {
					BJSCremainTime = gwBJSC.getRemainTime();
					if(requestTime) {
						BJSCTime= DsnProxyGrab.getBJSCTime();
						BJSCremainTime = Long.parseLong(BJSCTime[0]);
						if(BJSCremainTime > 0) {
							System.out.println("[代理]距离北京赛车封盘:" + BJSCremainTime/1000);
							gwBJSC.setRemainTime(BJSCremainTime);
						}
						else {
							System.out.println("[代理]距离北京赛车开盘:" + Long.parseLong(BJSCTime[2])/1000);
							gwBJSC.setRemainTime(Long.parseLong(BJSCTime[2]));
						}
					}
					while(BJSCremainTime > 10*60*1000) {//获取时间失败
						if(!DsnProxyGrab.isInBJSCgrabTime()) {
							BJSCremainTime = -1;
							isBJSCclose = true;
							gwBJSC.resetData();
							gwBJSC.setRemainTime(0);
							DsnProxyGrab.disableBJSCData();
							inBJSCgrabTime = false;
							break;
						}
						
						if(DsnProxyGrab.getIsisNeedChangeLine() == true){
							DsnProxyGrab.setLinePriority();
						}
						
						int res = DsnProxyGrab.reLogin();
						if(res == -1) {
							Thread.currentThread().sleep(3000);
						} else if(res == 1) {
							//todo
							gwBJSC.setOnlineStatus(false);
							DsnProxyGrab.disableCQSSCData();
							DsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							DsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwBJSC.setOnlineStatus(true);
						}
						
						DsnProxyGrab.setisNeedChangeLine(false);
						DsnProxyGrab.clearAvgRequest();
						
						BJSCTime = DsnProxyGrab.getBJSCTime();
						BJSCremainTime = Long.parseLong(BJSCTime[0]);
					}
					
					if(BJSCremainTime > 0) {
						if(isBJSCclose) {
							gwBJSC.setCloseText(false);
							gwBJSC.resetData();
							isBJSCclose = false;
						}
						
						if(!requestTime) {
							System.out.println("[代理][距离北京赛车彩封盘时间为]:" + BJSCremainTime/1000);
						}
						
						if(BJSCremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
					}else if(BJSCremainTime <= 0){
						if(!isBJSCclose) {
							gwBJSC.setCloseText(true);
							isBJSCclose = true;
							requestTime = true;
							sleepTime = 8*1000;
							//DsnProxyGrab.disableBJSCData();
							continue;
						}
					}
					
					gwBJSC.setDrawNumber(BJSCTime[1]);
				}			
				
				if(grabBJSC && inBJSCgrabTime) {
					String dataGY = DsnProxyGrab.grabBJSCdata("GY", "XZ", "");
					if(dataGY == "timeout") {
						continue;
					}else if(dataGY == null) {
						isNeedLogin = true;
						continue;
					}
					
					String dataSSWL = DsnProxyGrab.grabBJSCdata("SSWL", "XZ", "");
					if(dataSSWL == "timeout") {
						continue;
					}else if(dataSSWL == null) {
						isNeedLogin = true;
						continue;
					}
					
					String dataQBJS = DsnProxyGrab.grabBJSCdata("QBJS", "XZ", "");
					if(dataQBJS == "timeout") {
						continue;
					}else if(dataQBJS == null) {
						isNeedLogin = true;
						continue;
					}
					
					String [] data = {dataGY, dataSSWL, dataQBJS};
					if(BJSCremainTime > 2) {
						DsnProxyGrab.setBJSCdata(BJSCTime[1], data, Long.toString(gwBJSC.getRemainTime()/1000));
					}
					else {
						DsnProxyGrab.setBJSCdata(BJSCTime[1], data, Long.toString(BJSCremainTime/1000));
					}
					gwBJSC.setData(data);
				}
					
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    
    public  void startGrabBJSC() {
    	grabBJSC = true;
    	gwBJSC.setVisible(true);
    }
    
    public  void stopGrabBJSC() {
    	grabBJSC = false;
    	gwBJSC.resetData();
    	gwBJSC.setVisible(false);
    }
    
}
