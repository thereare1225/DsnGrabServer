package dsn;
class GrabGXKLthread extends Thread{
	long almostTime = 35*1000;  //进入加速时间
	long sleepTime = 10*1000;	//平时睡眠时间
    
    boolean grabGXKL = false;
    boolean isGXKLclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inGXKLgrabTime = true;
    GrabGXKLwindow gwGXKL;
    DsnProxyGrab dsnProxyGrab;
    public GrabGXKLthread(DsnProxyGrab dsnProxyGrab) {
    	gwGXKL = new GrabGXKLwindow();
    	this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
    		String[] GXKLTime = {"0", "0", "0"};
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
						gwGXKL.setOnlineStatus(false);
						dsnProxyGrab.disableGXKLData();
						dsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwGXKL.setOnlineStatus(true);
					}
					
					dsnProxyGrab.setisNeedChangeLine(false);
					dsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long GXKLremainTime = 0;
				if(dsnProxyGrab.isInGXKLgrabTime()) {
						inGXKLgrabTime = true;
				} else {
					if(inGXKLgrabTime) {
						GXKLremainTime = -1;
						isGXKLclose = true;
						gwGXKL.resetData();
						gwGXKL.setRemainTime(0);
						dsnProxyGrab.disableGXKLData();
						inGXKLgrabTime = false;
					}
				}
				
				if(!grabGXKL) {
					dsnProxyGrab.disableGXKLData();
				}
				
				if(grabGXKL && inGXKLgrabTime) {
					GXKLremainTime = dsnProxyGrab.getGXKLlocalRemainTime();
					if(requestTime) {
						GXKLTime= dsnProxyGrab.getGXKLtime();
						GXKLremainTime = Long.parseLong(GXKLTime[0]);
						if(GXKLremainTime > 0) {
							System.out.println("[代理]距离广西快乐十分封盘:" + GXKLremainTime/1000);
							gwGXKL.setRemainTime(GXKLremainTime);
						}
						else {
							System.out.println("[代理]距离广西快乐十分开盘:" + Long.parseLong(GXKLTime[2])/1000);
							gwGXKL.setRemainTime(Long.parseLong(GXKLTime[2]));
						}
						
					}
					while(GXKLremainTime > 20*60*1000) {//获取时间失败
						if(!dsnProxyGrab.isInGXKLgrabTime()) {
							GXKLremainTime = -1;
							isGXKLclose = true;
							gwGXKL.resetData();
							gwGXKL.setRemainTime(0);
							dsnProxyGrab.disableGXKLData();
							inGXKLgrabTime = false;
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
							gwGXKL.setOnlineStatus(false);
							dsnProxyGrab.disableGXKLData();
							dsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							dsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwGXKL.setOnlineStatus(true);
						}
						
						dsnProxyGrab.setisNeedChangeLine(false);
						dsnProxyGrab.clearAvgRequest();
						
						GXKLTime = dsnProxyGrab.getGXKLtime();
						GXKLremainTime = Long.parseLong(GXKLTime[0]);
					}
					
					if(GXKLremainTime > 0) {
						if(isGXKLclose) {
							gwGXKL.setCloseText(false);
							gwGXKL.resetData();
							isGXKLclose = false;
						}
						if(!requestTime) {
							System.out.println("[代理][距离广西快乐十分盘时间为]:" + GXKLremainTime/1000);
						}
						if(GXKLremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(GXKLremainTime <= 0){
						if(!isGXKLclose) {
							gwGXKL.setCloseText(true);
							isGXKLclose = true;
							requestTime = true;
							sleepTime = 8*1000;
							//dsnProxyGrab.disableGXKLData();
							continue;
						}
					}
					
					gwGXKL.setDrawNumber(GXKLTime[1]);
				}
				
				
				if(grabGXKL && inGXKLgrabTime) {
					String data = dsnProxyGrab.grabGXKLdata();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(GXKLremainTime > 2) {
						dsnProxyGrab.setGXKLdata(GXKLTime[1], data, Long.toString(dsnProxyGrab.getGXKLlocalRemainTime()/1000));
					}
					else {
						dsnProxyGrab.setGXKLdata(GXKLTime[1], data, Long.toString(GXKLremainTime/1000));
					}
					String [] datas = {data};
					gwGXKL.setData(datas);
					//System.out.println("GXKL data:" + data);
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabGXKL() {
    	grabGXKL = true;
    	gwGXKL.setVisible(true);
    }
    
    public  void stopGrabGXKL() {
    	grabGXKL = false;
    	gwGXKL.resetData();
    	gwGXKL.setVisible(false);
    }
    
}
