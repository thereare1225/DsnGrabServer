class GrabGD115thread extends Thread{
	long almostTime = 35*1000;  //�������ʱ��
	long sleepTime = 10*1000;	//ƽʱ˯��ʱ��
    
    boolean grabGD115 = false;
    boolean isGD115close = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inGD115grabTime = true;
   // GrabGD115window gwGD115;
    public GrabGD115thread() {
	}
    
    @Override
    public void run() {
    	try {
    		String[] GD115Time = {"0", "0", "0"};
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
						//gwGD115.setOnlineStatus(false);
						DsnProxyGrab.disableGD115Data();
						DsnProxyGrab.disableBJSCData();
						System.out.println("�������������ʧ��,�������µ�¼....\n");
						DsnProxyGrab.connFailLogin();
						System.out.println("�������µ�¼�ɹ�\n");
						//gwGD115.setOnlineStatus(true);
					}
					
					DsnProxyGrab.setisNeedChangeLine(false);
					DsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long GD115remainTime = 0;
				if(DsnProxyGrab.isInGD115grabTime()) {
						inGD115grabTime = true;
				} else {
					if(inGD115grabTime) {
						GD115remainTime = -1;
						isGD115close = true;
						//gwGD115.resetData();
						//gwGD115.setRemainTime(0);
						DsnProxyGrab.disableGD115Data();
						inGD115grabTime = false;
					}
				}
				
				if(!grabGD115) {
					DsnProxyGrab.disableGD115Data();
				}
				
				if(grabGD115 && inGD115grabTime) {
					GD115remainTime = DsnProxyGrab.getGD115localRemainTime();
					if(requestTime) {
						GD115Time= DsnProxyGrab.getGD115time();
						GD115remainTime = Long.parseLong(GD115Time[0]);
						if(GD115remainTime > 0) {
							System.out.println("[����]����㶫11ѡ5����:" + GD115remainTime/1000);
							//gwGD115.setRemainTime(GD115remainTime);
						}
						else {
							System.out.println("[����]����㶫11ѡ5����:" + Long.parseLong(GD115Time[2])/1000);
							//gwGD115.setRemainTime(Long.parseLong(GD115Time[2]));
						}
						
					}
					while(GD115remainTime > 20*60*1000) {//��ȡʱ��ʧ��
						if(!DsnProxyGrab.isInGD115grabTime()) {
							GD115remainTime = -1;
							isGD115close = true;
							//gwGD115.resetData();
							//gwGD115.setRemainTime(0);
							DsnProxyGrab.disableGD115Data();
							inGD115grabTime = false;
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
							//gwGD115.setOnlineStatus(false);
							DsnProxyGrab.disableGD115Data();
							DsnProxyGrab.disableBJSCData();
							System.out.println("�������������ʧ��,�������µ�¼....\n");
							DsnProxyGrab.connFailLogin();
							System.out.println("�������µ�¼�ɹ�\n");
							//gwGD115.setOnlineStatus(true);
						}
						
						DsnProxyGrab.setisNeedChangeLine(false);
						DsnProxyGrab.clearAvgRequest();
						
						GD115Time = DsnProxyGrab.getGD115time();
						GD115remainTime = Long.parseLong(GD115Time[0]);
					}
					
					if(GD115remainTime > 0) {
						if(isGD115close) {
							//gwGD115.setCloseText(false);
							//gwGD115.resetData();
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
							//gwGD115.setCloseText(true);
							isGD115close = true;
							requestTime = true;
							sleepTime = 8*1000;
							//DsnProxyGrab.disableGD115Data();
							continue;
						}
					}
					
					//gwGD115.setDrawNumber(GD115Time[1]);
				}
				
				
				if(grabGD115 && inGD115grabTime) {
					String data = DsnProxyGrab.grabGD115data();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(GD115remainTime > 2) {
						DsnProxyGrab.setGD115data(GD115Time[1], data, Long.toString(DsnProxyGrab.getGD115localRemainTime()/1000));
					}
					else {
						DsnProxyGrab.setGD115data(GD115Time[1], data, Long.toString(GD115remainTime/1000));
					}
					String [] datas = {data};
					//gwGD115.setData(datas);
					//System.out.println("GD115 data:" + data);
				}
			    	
				Thread.currentThread().sleep(sleepTime);

			}//while
		
    	}catch (InterruptedException e) {
            // TODO: handle exception
        }
	}//run
    
    public  void startGrabGD115() {
    	grabGD115 = true;
    	//gwGD115.setVisible(true);
    }
    
    public  void stopGrabGD115() {
    	grabGD115 = false;
    	//gwGD115.resetData();
    	//gwGD115.setVisible(false);
    }
    
}
