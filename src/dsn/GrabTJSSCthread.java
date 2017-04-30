package dsn;
class GrabTJSSCthread extends Thread{
	long almostTime = 35*1000;  //�������ʱ��
	long sleepTime = 10*1000;	//ƽʱ˯��ʱ��
    
    boolean grabTJSSC = false;
    boolean isTJSSCclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inTJSSCgrabTime = true;
    GrabTJSSCwindow gwTJSSC;
    public GrabTJSSCthread() {
    	gwTJSSC = new GrabTJSSCwindow();
	}
    
    @Override
    public void run() {
    	try {
    		String[] TJSSCTime = {"0", "0", "0"};
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
						gwTJSSC.setOnlineStatus(false);
						DsnProxyGrab.disableTJSSCData();
						DsnProxyGrab.disableBJSCData();
						System.out.println("�������������ʧ��,�������µ�¼....\n");
						DsnProxyGrab.connFailLogin();
						System.out.println("�������µ�¼�ɹ�\n");
						gwTJSSC.setOnlineStatus(true);
					}
					
					DsnProxyGrab.setisNeedChangeLine(false);
					DsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long TJSSCremainTime = 0;
				if(DsnProxyGrab.isInTJSSCgrabTime()) {
						inTJSSCgrabTime = true;
				} else {
					if(inTJSSCgrabTime) {
						TJSSCremainTime = -1;
						isTJSSCclose = true;
						gwTJSSC.resetData();
						gwTJSSC.setRemainTime(0);
						DsnProxyGrab.disableTJSSCData();
						inTJSSCgrabTime = false;
					}
				}
				
				if(!grabTJSSC) {
					DsnProxyGrab.disableTJSSCData();
				}
				
				if(grabTJSSC && inTJSSCgrabTime) {
					TJSSCremainTime = DsnProxyGrab.getTJSSClocalRemainTime();
					if(requestTime) {
						TJSSCTime= DsnProxyGrab.getTJSSCtime();
						TJSSCremainTime = Long.parseLong(TJSSCTime[0]);
						if(TJSSCremainTime > 0) {
							System.out.println("[����]�������ʱʱ�ʷ���:" + TJSSCremainTime/1000);
							gwTJSSC.setRemainTime(TJSSCremainTime);
						}
						else {
							System.out.println("[����]�������ʱʱ�ʿ���:" + Long.parseLong(TJSSCTime[2])/1000);
							gwTJSSC.setRemainTime(Long.parseLong(TJSSCTime[2]));
						}
						
					}
					while(TJSSCremainTime > 20*60*1000) {//��ȡʱ��ʧ��
						if(!DsnProxyGrab.isInTJSSCgrabTime()) {
							TJSSCremainTime = -1;
							isTJSSCclose = true;
							gwTJSSC.resetData();
							gwTJSSC.setRemainTime(0);
							DsnProxyGrab.disableTJSSCData();
							inTJSSCgrabTime = false;
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
							gwTJSSC.setOnlineStatus(false);
							DsnProxyGrab.disableTJSSCData();
							DsnProxyGrab.disableBJSCData();
							System.out.println("�������������ʧ��,�������µ�¼....\n");
							DsnProxyGrab.connFailLogin();
							System.out.println("�������µ�¼�ɹ�\n");
							gwTJSSC.setOnlineStatus(true);
						}
						
						DsnProxyGrab.setisNeedChangeLine(false);
						DsnProxyGrab.clearAvgRequest();
						
						TJSSCTime = DsnProxyGrab.getTJSSCtime();
						TJSSCremainTime = Long.parseLong(TJSSCTime[0]);
					}
					
					if(TJSSCremainTime > 0) {
						if(isTJSSCclose) {
							gwTJSSC.setCloseText(false);
							gwTJSSC.resetData();
							isTJSSCclose = false;
						}
						if(!requestTime) {
							System.out.println("[����][�������ʱʱ����ʱ��Ϊ]:" + TJSSCremainTime/1000);
						}
						if(TJSSCremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(TJSSCremainTime <= 0){
						if(!isTJSSCclose) {
							gwTJSSC.setCloseText(true);
							isTJSSCclose = true;
							requestTime = true;
							sleepTime = 8*1000;
							//DsnProxyGrab.disableTJSSCData();
							continue;
						}
					}
					
					gwTJSSC.setDrawNumber(TJSSCTime[1]);
				}
				
				
				if(grabTJSSC && inTJSSCgrabTime) {
					String data = DsnProxyGrab.grabTJSSCdata();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(TJSSCremainTime > 2) {
						DsnProxyGrab.setTJSSCdata(TJSSCTime[1], data, Long.toString(DsnProxyGrab.getTJSSClocalRemainTime()/1000));
					}
					else {
						DsnProxyGrab.setTJSSCdata(TJSSCTime[1], data, Long.toString(TJSSCremainTime/1000));
					}
					String [] datas = {data};
					gwTJSSC.setData(datas);
					//System.out.println("TJSSC data:" + data);
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabTJSSC() {
    	grabTJSSC = true;
    	gwTJSSC.setVisible(true);
    }
    
    public  void stopGrabTJSSC() {
    	grabTJSSC = false;
    	gwTJSSC.resetData();
    	gwTJSSC.setVisible(false);
    }
    
}
