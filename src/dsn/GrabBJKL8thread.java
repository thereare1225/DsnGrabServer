package dsn;
class GrabBJKL8thread extends Thread{
	long almostTime = 35*1000;  //进入加速时间
	long sleepTime = 10*1000;	//平时睡眠时间
    
    boolean grabBJKL8 = false;
    boolean isBJKL8close = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inBJKL8grabTime = true;
   // GrabBJKL8window gwBJKL8;
    DsnProxyGrab dsnProxyGrab;
    public GrabBJKL8thread(DsnProxyGrab dsnProxyGrab) {
    	this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
    		String[] BJKL8Time = {"0", "0", "0"};
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
						//gwBJKL8.setOnlineStatus(false);
						dsnProxyGrab.disableBJKL8Data();
						dsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						//gwBJKL8.setOnlineStatus(true);
					}
					
					dsnProxyGrab.setisNeedChangeLine(false);
					dsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long BJKL8remainTime = 0;
				if(dsnProxyGrab.isInBJKL8grabTime()) {
						inBJKL8grabTime = true;
				} else {
					if(inBJKL8grabTime) {
						BJKL8remainTime = -1;
						isBJKL8close = true;
						//gwBJKL8.resetData();
						//gwBJKL8.setRemainTime(0);
						dsnProxyGrab.disableBJKL8Data();
						inBJKL8grabTime = false;
					}
				}
				
				if(!grabBJKL8) {
					dsnProxyGrab.disableBJKL8Data();
				}
				
				if(grabBJKL8 && inBJKL8grabTime) {
					BJKL8remainTime = dsnProxyGrab.getBJKL8localRemainTime();
					if(requestTime) {
						BJKL8Time= dsnProxyGrab.getBJKL8time();
						BJKL8remainTime = Long.parseLong(BJKL8Time[0]);
						if(BJKL8remainTime > 0) {
							System.out.println("[代理]距离北京快乐8封盘:" + BJKL8remainTime/1000);
							//gwBJKL8.setRemainTime(BJKL8remainTime);
						}
						else {
							System.out.println("[代理]距离北京快乐8开盘:" + Long.parseLong(BJKL8Time[2])/1000);
							//gwBJKL8.setRemainTime(Long.parseLong(BJKL8Time[2]));
						}
						
					}
					while(BJKL8remainTime > 20*60*1000) {//获取时间失败
						if(!dsnProxyGrab.isInBJKL8grabTime()) {
							BJKL8remainTime = -1;
							isBJKL8close = true;
							//gwBJKL8.resetData();
							//gwBJKL8.setRemainTime(0);
							dsnProxyGrab.disableBJKL8Data();
							inBJKL8grabTime = false;
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
							//gwBJKL8.setOnlineStatus(false);
							dsnProxyGrab.disableBJKL8Data();
							dsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							dsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							//gwBJKL8.setOnlineStatus(true);
						}
						
						dsnProxyGrab.setisNeedChangeLine(false);
						dsnProxyGrab.clearAvgRequest();
						
						BJKL8Time = dsnProxyGrab.getBJKL8time();
						BJKL8remainTime = Long.parseLong(BJKL8Time[0]);
					}
					
					if(BJKL8remainTime > 0) {
						if(isBJKL8close) {
							//gwBJKL8.setCloseText(false);
							//gwBJKL8.resetData();
							isBJKL8close = false;
						}
						if(!requestTime) {
							System.out.println("[代理][距离北京快乐8盘时间为]:" + BJKL8remainTime/1000);
						}
						if(BJKL8remainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(BJKL8remainTime <= 0){
						if(!isBJKL8close) {
							//gwBJKL8.setCloseText(true);
							isBJKL8close = true;
							requestTime = true;
							sleepTime = 8*1000;
							//dsnProxyGrab.disableBJKL8Data();
							continue;
						}
					}
					
					//gwBJKL8.setDrawNumber(BJKL8Time[1]);
				}
				
				
				if(grabBJKL8 && inBJKL8grabTime) {
					String data = dsnProxyGrab.grabBJKL8data();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(BJKL8remainTime > 2) {
						dsnProxyGrab.setBJKL8data(BJKL8Time[1], data, Long.toString(dsnProxyGrab.getBJKL8localRemainTime()/1000));
					}
					else {
						dsnProxyGrab.setBJKL8data(BJKL8Time[1], data, Long.toString(BJKL8remainTime/1000));
					}
					String [] datas = {data};
					//gwBJKL8.setData(datas);
					//System.out.println("BJKL8 data:" + data);
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabBJKL8() {
    	grabBJKL8 = true;
    	//gwBJKL8.setVisible(true);
    }
    
    public  void stopGrabBJKL8() {
    	grabBJKL8 = false;
    	//gwBJKL8.resetData();
    	//gwBJKL8.setVisible(false);
    }
    
}
