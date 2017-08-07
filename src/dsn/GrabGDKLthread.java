package dsn;
public class GrabGDKLthread extends Thread{
	
	long almostTime = 35*1000;  //进入加速时间
	long sleepTime = 10*1000;	//平时睡眠时间
    
    boolean grabGDKL = false;
    boolean isGDKLclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inGDKLgrabTime = true;
    GrabGDKLwindow gwGDKL= null;
    DsnProxyGrab dsnProxyGrab;
    public GrabGDKLthread(DsnProxyGrab dsnProxyGrab) {
    	gwGDKL = new GrabGDKLwindow();
    	this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
    		String[] GDKLTime = {"0", "0", "0"};
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
						gwGDKL.setOnlineStatus(false);
						dsnProxyGrab.disableGDKLData();
						dsnProxyGrab.disableBJSCData();
						System.out.println("代理端网络连接失败,正在重新登录....\n");
						dsnProxyGrab.connFailLogin();
						System.out.println("代理重新登录成功\n");
						gwGDKL.setOnlineStatus(true);
					}
					
					dsnProxyGrab.setisNeedChangeLine(false);
					dsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long GDKLremainTime = 0;
				if(dsnProxyGrab.isInGDKLgrabTime()) {
						inGDKLgrabTime = true;
				} else {
					if(inGDKLgrabTime) {
						GDKLremainTime = -1;
						isGDKLclose = true;
						gwGDKL.resetData();
						gwGDKL.setRemainTime(0);
						dsnProxyGrab.disableGDKLData();
						inGDKLgrabTime = false;
					}
				}
				
				if(!grabGDKL) {
					dsnProxyGrab.disableGDKLData();
				}
				
				if(grabGDKL && inGDKLgrabTime) {
					GDKLremainTime = dsnProxyGrab.getGDKLlocalRemainTime();
					if(requestTime) {
						GDKLTime= dsnProxyGrab.getGDKLtime();
						GDKLremainTime = Long.parseLong(GDKLTime[0]);
						if(GDKLremainTime > 0) {
							System.out.println("[代理]距离幸运农场封盘:" + GDKLremainTime/1000);
							gwGDKL.setRemainTime(GDKLremainTime);
						}
						else {
							System.out.println("[代理]距离幸运农场彩开盘:" + Long.parseLong(GDKLTime[2])/1000);
							gwGDKL.setRemainTime(Long.parseLong(GDKLTime[2]));
						}
						
					}
					
					while(GDKLremainTime > 10*60*1000) {//获取时间失败
						if(!dsnProxyGrab.isInGDKLgrabTime()) {
							GDKLremainTime = -1;
							isGDKLclose = true;
							gwGDKL.resetData();
							gwGDKL.setRemainTime(0);
							dsnProxyGrab.disableGDKLData();
							inGDKLgrabTime = false;
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
							gwGDKL.setOnlineStatus(false);
							dsnProxyGrab.disableGDKLData();
							dsnProxyGrab.disableBJSCData();
							System.out.println("代理端网络连接失败,正在重新登录....\n");
							dsnProxyGrab.connFailLogin();
							System.out.println("代理重新登录成功\n");
							gwGDKL.setOnlineStatus(true);
						}
						
						dsnProxyGrab.setisNeedChangeLine(false);
						dsnProxyGrab.clearAvgRequest();
						
						GDKLTime = dsnProxyGrab.getGDKLtime();
						GDKLremainTime = Long.parseLong(GDKLTime[0]);
					}
					
					if(GDKLremainTime > 0) {
						if(isGDKLclose) {
							gwGDKL.setCloseText(false);
							gwGDKL.resetData();
							isGDKLclose = false;
						}
						if(!requestTime) {
							System.out.println("[代理][距离幸运农场封盘时间为]:" + GDKLremainTime/1000);
						}
						if(GDKLremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(GDKLremainTime <= 0){
						if(!isGDKLclose) {
							gwGDKL.setCloseText(true);
							isGDKLclose = true;
							requestTime = true;
							sleepTime = 10*1000;
							//dsnProxyGrab.disableGDKLData();
							continue;
						}
					}
					
					gwGDKL.setDrawNumber(GDKLTime[1]);
				}
				
				
				if(grabGDKL && inGDKLgrabTime) {
					String [] data = dsnProxyGrab.grabGDKLdata();
					if(data == null) {
						data = dsnProxyGrab.grabGDKLdata();
					}
					
					if(data == null) {
						data = dsnProxyGrab.grabGDKLdata();
					}
					
					if(data != null) {
						if(GDKLremainTime > 2) {
							dsnProxyGrab.setGDKLdata(GDKLTime[1], data, Long.toString(dsnProxyGrab.getGDKLlocalRemainTime()/1000));
						}
						else {
							dsnProxyGrab.setGDKLdata(GDKLTime[1], data, Long.toString(GDKLremainTime/1000));
						}
						
						gwGDKL.setData(data);
//						for(int i = 0; i < 9; i++) {
//							System.out.println(data[i]);
//						}
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
    
    public  void startGrabGDKL() {
    	grabGDKL = true;
    	gwGDKL.setVisible(true);
    }
    
    public  void stopGrabGDKL() {
    	grabGDKL = false;
    	gwGDKL.resetData();
    	gwGDKL.setVisible(false);
    }
	
}
