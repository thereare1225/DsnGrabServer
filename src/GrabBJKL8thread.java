class GrabBJKL8thread extends Thread{
	long almostTime = 35*1000;  //�������ʱ��
	long sleepTime = 10*1000;	//ƽʱ˯��ʱ��
    
    boolean grabBJKL8 = false;
    boolean isBJKL8close = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inBJKL8grabTime = true;
   // GrabBJKL8window gwBJKL8;
    public GrabBJKL8thread() {
	}
    
    @Override
    public void run() {
    	try {
    		String[] BJKL8Time = {"0", "0", "0"};
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
						//gwBJKL8.setOnlineStatus(false);
						DsnProxyGrab.disableBJKL8Data();
						DsnProxyGrab.disableBJSCData();
						System.out.println("�������������ʧ��,�������µ�¼....\n");
						DsnProxyGrab.connFailLogin();
						System.out.println("�������µ�¼�ɹ�\n");
						//gwBJKL8.setOnlineStatus(true);
					}
					
					DsnProxyGrab.setisNeedChangeLine(false);
					DsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long BJKL8remainTime = 0;
				if(DsnProxyGrab.isInBJKL8grabTime()) {
						inBJKL8grabTime = true;
				} else {
					if(inBJKL8grabTime) {
						BJKL8remainTime = -1;
						isBJKL8close = true;
						//gwBJKL8.resetData();
						//gwBJKL8.setRemainTime(0);
						DsnProxyGrab.disableBJKL8Data();
						inBJKL8grabTime = false;
					}
				}
				
				if(!grabBJKL8) {
					DsnProxyGrab.disableBJKL8Data();
				}
				
				if(grabBJKL8 && inBJKL8grabTime) {
					BJKL8remainTime = DsnProxyGrab.getBJKL8localRemainTime();
					if(requestTime) {
						BJKL8Time= DsnProxyGrab.getBJKL8time();
						BJKL8remainTime = Long.parseLong(BJKL8Time[0]);
						if(BJKL8remainTime > 0) {
							System.out.println("[����]���뱱������8����:" + BJKL8remainTime/1000);
							//gwBJKL8.setRemainTime(BJKL8remainTime);
						}
						else {
							System.out.println("[����]���뱱������8����:" + Long.parseLong(BJKL8Time[2])/1000);
							//gwBJKL8.setRemainTime(Long.parseLong(BJKL8Time[2]));
						}
						
					}
					while(BJKL8remainTime > 20*60*1000) {//��ȡʱ��ʧ��
						if(!DsnProxyGrab.isInBJKL8grabTime()) {
							BJKL8remainTime = -1;
							isBJKL8close = true;
							//gwBJKL8.resetData();
							//gwBJKL8.setRemainTime(0);
							DsnProxyGrab.disableBJKL8Data();
							inBJKL8grabTime = false;
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
							//gwBJKL8.setOnlineStatus(false);
							DsnProxyGrab.disableBJKL8Data();
							DsnProxyGrab.disableBJSCData();
							System.out.println("�������������ʧ��,�������µ�¼....\n");
							DsnProxyGrab.connFailLogin();
							System.out.println("�������µ�¼�ɹ�\n");
							//gwBJKL8.setOnlineStatus(true);
						}
						
						DsnProxyGrab.setisNeedChangeLine(false);
						DsnProxyGrab.clearAvgRequest();
						
						BJKL8Time = DsnProxyGrab.getBJKL8time();
						BJKL8remainTime = Long.parseLong(BJKL8Time[0]);
					}
					
					if(BJKL8remainTime > 0) {
						if(isBJKL8close) {
							//gwBJKL8.setCloseText(false);
							//gwBJKL8.resetData();
							isBJKL8close = false;
						}
						if(!requestTime) {
							System.out.println("[����][���뱱������8��ʱ��Ϊ]:" + BJKL8remainTime/1000);
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
							//DsnProxyGrab.disableBJKL8Data();
							continue;
						}
					}
					
					//gwBJKL8.setDrawNumber(BJKL8Time[1]);
				}
				
				
				if(grabBJKL8 && inBJKL8grabTime) {
					String data = DsnProxyGrab.grabBJKL8data();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(BJKL8remainTime > 2) {
						DsnProxyGrab.setBJKL8data(BJKL8Time[1], data, Long.toString(DsnProxyGrab.getBJKL8localRemainTime()/1000));
					}
					else {
						DsnProxyGrab.setBJKL8data(BJKL8Time[1], data, Long.toString(BJKL8remainTime/1000));
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
