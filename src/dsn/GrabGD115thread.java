package dsn;
class GrabGD115thread extends Thread{
	long almostTime = 35*1000;  //�������ʱ��
	long sleepTime = 10*1000;	//ƽʱ˯��ʱ��
    
    boolean grabGD115 = false;
    boolean isGD115close = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inGD115grabTime = true;
    GrabGD115window gwGD115;
    DsnProxyGrab dsnProxyGrab;
    public GrabGD115thread(DsnProxyGrab dsnProxyGrab) {
    	gwGD115 = new GrabGD115window();
    	this.dsnProxyGrab = dsnProxyGrab;
	}
    
    @Override
    public void run() {
    	try {
    		String[] GD115Time = {"0", "0", "0"};
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
						gwGD115.setOnlineStatus(false);
						dsnProxyGrab.disableGD115Data();
						dsnProxyGrab.disableBJSCData();
						System.out.println("�������������ʧ��,�������µ�¼....\n");
						dsnProxyGrab.connFailLogin();
						System.out.println("�������µ�¼�ɹ�\n");
						gwGD115.setOnlineStatus(true);
					}
					
					dsnProxyGrab.setisNeedChangeLine(false);
					dsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long GD115remainTime = 0;
				if(dsnProxyGrab.isInGD115grabTime()) {
						inGD115grabTime = true;
				} else {
					if(inGD115grabTime) {
						GD115remainTime = -1;
						isGD115close = true;
						gwGD115.resetData();
						gwGD115.setRemainTime(0);
						dsnProxyGrab.disableGD115Data();
						inGD115grabTime = false;
					}
				}
				
				if(!grabGD115) {
					dsnProxyGrab.disableGD115Data();
				}
				
				if(grabGD115 && inGD115grabTime) {
					GD115remainTime = dsnProxyGrab.getGD115localRemainTime();
					if(requestTime) {
						GD115Time= dsnProxyGrab.getGD115time();
						GD115remainTime = Long.parseLong(GD115Time[0]);
						if(GD115remainTime > 0) {
							System.out.println("[����]����㶫11ѡ5����:" + GD115remainTime/1000);
							gwGD115.setRemainTime(GD115remainTime);
						}
						else {
							System.out.println("[����]����㶫11ѡ5����:" + Long.parseLong(GD115Time[2])/1000);
							gwGD115.setRemainTime(Long.parseLong(GD115Time[2]));
						}
						
					}
					while(GD115remainTime > 20*60*1000) {//��ȡʱ��ʧ��
						if(!dsnProxyGrab.isInGD115grabTime()) {
							GD115remainTime = -1;
							isGD115close = true;
							gwGD115.resetData();
							gwGD115.setRemainTime(0);
							dsnProxyGrab.disableGD115Data();
							inGD115grabTime = false;
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
							gwGD115.setOnlineStatus(false);
							dsnProxyGrab.disableGD115Data();
							dsnProxyGrab.disableBJSCData();
							System.out.println("�������������ʧ��,�������µ�¼....\n");
							dsnProxyGrab.connFailLogin();
							System.out.println("�������µ�¼�ɹ�\n");
							gwGD115.setOnlineStatus(true);
						}
						
						dsnProxyGrab.setisNeedChangeLine(false);
						dsnProxyGrab.clearAvgRequest();
						
						GD115Time = dsnProxyGrab.getGD115time();
						GD115remainTime = Long.parseLong(GD115Time[0]);
					}
					
					if(GD115remainTime > 0) {
						if(isGD115close) {
							gwGD115.setCloseText(false);
							gwGD115.resetData();
							isGD115close = false;
						}
						if(!requestTime) {
							System.out.println("[����][����㶫11ѡ5��ʱ��Ϊ]:" + GD115remainTime/1000);
						}
						if(GD115remainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(GD115remainTime <= 0){
						if(!isGD115close) {
							gwGD115.setCloseText(true);
							isGD115close = true;
							requestTime = true;
							sleepTime = 8*1000;
							//dsnProxyGrab.disableGD115Data();
							continue;
						}
					}
					
					gwGD115.setDrawNumber(GD115Time[1]);
				}
				
				
				if(grabGD115 && inGD115grabTime) {
					String data = dsnProxyGrab.grabGD115data();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(GD115remainTime > 2) {
						dsnProxyGrab.setGD115data(GD115Time[1], data, Long.toString(dsnProxyGrab.getGD115localRemainTime()/1000));
					}
					else {
						dsnProxyGrab.setGD115data(GD115Time[1], data, Long.toString(GD115remainTime/1000));
					}
					String [] datas = {data};
					gwGD115.setData(datas);
					System.out.println("GD115 data:" + data);
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabGD115() {
    	grabGD115 = true;
    	gwGD115.setVisible(true);
    }
    
    public  void stopGrabGD115() {
    	grabGD115 = false;
    	gwGD115.resetData();
    	gwGD115.setVisible(false);
    }
    
}
