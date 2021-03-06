package dsn;
class GrabBJSCthread extends Thread{
	long almostTime = 40*1000;  //进入加速时间
	long sleepTime = 8*1000;	//平时睡眠时间
    
    boolean grabBJSC = false;
    boolean isBJSCclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inBJSCgrabTime = true;
    boolean isInit = false;
    //InitBetData initBetData = null;
    DsnProxyGrab dsnProxyGrab;

    GrabBJSCwindow gwBJSC;
    public GrabBJSCthread(GrabBJSCwindow gwBJSC, DsnProxyGrab dsnProxyGrab) {
    	this.gwBJSC = gwBJSC;
    	//initBetData = new InitBetData(gwBJSC, dsnProxyGrab);
    	this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
			String[] BJSCTime = {"0", "0", "0"};
			while(true){
				if(isNeedLogin) {
					
//					if(dsnProxyGrab.getIsisNeedChangeLine() == true){
//						dsnProxyGrab.setLinePriority();
//					}
					
					int res = dsnProxyGrab.reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3*1000);
					} else if(res == 1) {
						//todo
						gwBJSC.setOnlineStatus(false);
						//dsnProxyGrab.disableCQSSCData();
						//dsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwBJSC.setOnlineStatus(true);
					}
					
					dsnProxyGrab.setisNeedChangeLine(false);
					dsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long BJSCremainTime = 0;
				
				if(dsnProxyGrab.isInBJSCgrabTime()) {
					inBJSCgrabTime = true;
				} else {
					if(inBJSCgrabTime) {
						BJSCremainTime = -1;
						isBJSCclose = true;
						//gwBJSC.resetData();
						//gwBJSC.setRemainTime(0);
						//dsnProxyGrab.disableBJSCData();
						inBJSCgrabTime = false;
					}
				}
				
				if(!grabBJSC) {
					//dsnProxyGrab.disableBJSCData();
				}		
				
				if(grabBJSC && inBJSCgrabTime) {
					BJSCremainTime = gwBJSC.getRemainTime();
					if(requestTime) {
						BJSCTime= dsnProxyGrab.getBJSCTime();
						BJSCremainTime = Long.parseLong(BJSCTime[0]);
						if(BJSCremainTime > 0) {
							System.out.println("[代理]距离北京赛车封盘:" + BJSCremainTime/1000);
							gwBJSC.setRemainTime(BJSCremainTime);
						}
						else {
							System.out.println("[代理]距离北京赛车开盘:" + Long.parseLong(BJSCTime[2])/1000);
							gwBJSC.setRemainTime(Long.parseLong(BJSCTime[2]));
						}
						
						if(BJSCremainTime < 10*60*1000 && BJSCremainTime > 40 * 1000) {
							//initBetData.saveBetResToFile(Long.parseLong(BJSCTime[1]) - 1);
						}
					}
					while(BJSCremainTime > 10*60*1000) {//获取时间失败
						if(!dsnProxyGrab.isInBJSCgrabTime()) {
							BJSCremainTime = -1;
							isBJSCclose = true;
							gwBJSC.resetData();
							gwBJSC.setRemainTime(0);
							//dsnProxyGrab.disableBJSCData();
							inBJSCgrabTime = false;
							break;
						}
						
//						if(dsnProxyGrab.getIsisNeedChangeLine() == true){
//							dsnProxyGrab.setLinePriority();
//						}
						
						int res = dsnProxyGrab.reLogin();
						if(res == -1) {
							Thread.currentThread().sleep(3000);
						} else if(res == 1) {
							//todo
							gwBJSC.setOnlineStatus(false);
							//dsnProxyGrab.disableCQSSCData();
							//dsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							dsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwBJSC.setOnlineStatus(true);
						}
						
						dsnProxyGrab.setisNeedChangeLine(false);
						dsnProxyGrab.clearAvgRequest();
						
						BJSCTime = dsnProxyGrab.getBJSCTime();
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
					String dataGY = dsnProxyGrab.grabBJSCdata("GY", "XZ", "");
					if(dataGY == "timeout") {
						continue;
					}else if(dataGY == null) {
						isNeedLogin = true;
						continue;
					}
					
					String dataSSWL = dsnProxyGrab.grabBJSCdata("SSWL", "XZ", "");
					if(dataSSWL == "timeout") {
						continue;
					}else if(dataSSWL == null) {
						isNeedLogin = true;
						continue;
					}
					
					String dataQBJS = dsnProxyGrab.grabBJSCdata("QBJS", "XZ", "");
					if(dataQBJS == "timeout") {
						continue;
					}else if(dataQBJS == null) {
						isNeedLogin = true;
						continue;
					}
					
					String [] data = {dataGY, dataSSWL, dataQBJS};
					String [] method = {"", ""};
					if(BJSCremainTime > 2 * 1000) {
						dsnProxyGrab.setBJSCdata(BJSCTime[1], data, Long.toString(gwBJSC.getRemainTime()/1000), method[0], method[1]);
					}
					else {
						dsnProxyGrab.setBJSCdata(BJSCTime[1], data, Long.toString(BJSCremainTime/1000), method[0], method[1]);
					}
					gwBJSC.setData(data);
					//String []data1 = Common.combineBJSCdata(data.clone(), data.clone());
					//Common.outputBetsDetails(Common.constructoutputData(data, 1, false));
					//Common.outputBetsDetails(Common.constructoutputData(data1, 1, false));
				}
					
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    
    public  void startGrabBJSC() {
    	if(!isInit) {
    		//initBetData.initBetResDataFile();
    		isInit = true;
    	}
    	
    	grabBJSC = true;
    	gwBJSC.setVisible(true);
    }
    
    public  void stopGrabBJSC() {
    	grabBJSC = false;
    	gwBJSC.resetData();
    	gwBJSC.setVisible(false);
    }
    
}
