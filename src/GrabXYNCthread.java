public class GrabXYNCthread extends Thread{
	
	long almostTime = 35*1000;  //进入加速时间
	long sleepTime = 10*1000;	//平时睡眠时间
    
    boolean grabXYNC = false;
    boolean isXYNCclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inXYNCgrabTime = true;
    GrabXYNCwindow gwXYNC;
    public GrabXYNCthread() {
		gwXYNC = new GrabXYNCwindow();
	}
    
    @Override
    public void run() {
    	try {
    		String[] XYNCTime = {"0", "0", "0"};
			while(true){
				if(isNeedLogin) {		
					if(DsnProxyGrab.getIsisNeedChangeLine() == true){
						DsnProxyGrab.setLinePriority();
					}
					
					int res = DsnProxyGrab.reLogin();
					if(res == -1) {
						Thread.currentThread().sleep(3000);
					} else if(res == 1) {
						//todo
						gwXYNC.setOnlineStatus(false);
						DsnProxyGrab.disableXYNCData();
						DsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						DsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwXYNC.setOnlineStatus(true);
					}
					
					DsnProxyGrab.setisNeedChangeLine(false);
					DsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long XYNCremainTime = 0;
				if(DsnProxyGrab.isInXYNCgrabTime()) {
						inXYNCgrabTime = true;
				} else {
					if(inXYNCgrabTime) {
						XYNCremainTime = -1;
						isXYNCclose = true;
						gwXYNC.resetData();
						gwXYNC.setRemainTime(0);
						DsnProxyGrab.disableXYNCData();
						inXYNCgrabTime = false;
					}
				}
				
				if(!grabXYNC) {
					DsnProxyGrab.disableXYNCData();
				}
				
				if(grabXYNC && inXYNCgrabTime) {
					XYNCremainTime = gwXYNC.getRemainTime();
					if(requestTime) {
						XYNCTime= DsnProxyGrab.getXYNCTime();
						XYNCremainTime = Long.parseLong(XYNCTime[0]);
						if(XYNCremainTime > 0) {
							System.out.println("[代理]距离幸运农场封盘:" + XYNCremainTime/1000);
							gwXYNC.setRemainTime(XYNCremainTime);
						}
						else {
							System.out.println("[代理]距离幸运农场彩开盘:" + Long.parseLong(XYNCTime[2])/1000);
							gwXYNC.setRemainTime(Long.parseLong(XYNCTime[2]));
						}
						
					}
					
					while(XYNCremainTime > 10*60*1000) {//获取时间失败
						if(!DsnProxyGrab.isInXYNCgrabTime()) {
							XYNCremainTime = -1;
							isXYNCclose = true;
							gwXYNC.resetData();
							gwXYNC.setRemainTime(0);
							DsnProxyGrab.disableXYNCData();
							inXYNCgrabTime = false;
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
							gwXYNC.setOnlineStatus(false);
							DsnProxyGrab.disableXYNCData();
							DsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							DsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwXYNC.setOnlineStatus(true);
						}
						
						DsnProxyGrab.setisNeedChangeLine(false);
						DsnProxyGrab.clearAvgRequest();
						
						XYNCTime = DsnProxyGrab.getXYNCTime();
						XYNCremainTime = Long.parseLong(XYNCTime[0]);
					}
					
					if(XYNCremainTime > 0) {
						if(isXYNCclose) {
							gwXYNC.setCloseText(false);
							gwXYNC.resetData();
							isXYNCclose = false;
						}
						if(!requestTime) {
							System.out.println("[代理][距离幸运农场封盘时间为]:" + XYNCremainTime/1000);
						}
						if(XYNCremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(XYNCremainTime <= 0){
						if(!isXYNCclose) {
							gwXYNC.setCloseText(true);
							isXYNCclose = true;
							requestTime = true;
							sleepTime = 10*1000;
							//DsnProxyGrab.disableXYNCData();
							continue;
						}
					}
					
					gwXYNC.setDrawNumber(XYNCTime[1]);
				}
				
				
				if(grabXYNC && inXYNCgrabTime) {
					boolean res = DsnProxyGrab.grabXYNCdata();
					if(!res) {
						res = DsnProxyGrab.grabXYNCdata();
					}
					
					if(!res) {
						res = DsnProxyGrab.grabXYNCdata();
					}
					
					if(res) {
						if(XYNCremainTime > 2) {
							DsnProxyGrab.setXYNCdata(XYNCTime[1], Long.toString(gwXYNC.getRemainTime()/1000));
						}
						else {
							DsnProxyGrab.setXYNCdata(XYNCTime[1], Long.toString(XYNCremainTime/1000));
						}
						
						gwXYNC.setData(DsnProxyGrab.getXYNCshowData());
					} else {
						isNeedLogin = true;
					}
					
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabXYNC() {
    	grabXYNC = true;
    	gwXYNC.setVisible(true);
    }
    
    public  void stopGrabXYNC() {
    	grabXYNC = false;
    	gwXYNC.resetData();
    	gwXYNC.setVisible(false);
    }
	
}
