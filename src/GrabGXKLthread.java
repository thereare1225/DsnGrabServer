class GrabGXKLthread extends Thread{
	long almostTime = 35*1000;  //�������ʱ��
	long sleepTime = 10*1000;	//ƽʱ˯��ʱ��
    
    boolean grabGXKL = false;
    boolean isGXKLclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inGXKLgrabTime = true;
    //GrabGXKLwindow gwGXKL;
    public GrabGXKLthread() {
    	//gwGXKL = new GrabGXKLwindow();
	}
    
    @Override
    public void run() {
    	try {
    		String[] GXKLTime = {"0", "0", "0"};
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
						//gwGXKL.setOnlineStatus(false);
						DsnProxyGrab.disableGXKLData();
						DsnProxyGrab.disableBJSCData();
						System.out.println("�������������ʧ��,�������µ�¼....\n");
						DsnProxyGrab.connFailLogin();
						System.out.println("�������µ�¼�ɹ�\n");
						//gwGXKL.setOnlineStatus(true);
					}
					
					DsnProxyGrab.setisNeedChangeLine(false);
					DsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long GXKLremainTime = 0;
				if(DsnProxyGrab.isInGXKLgrabTime()) {
						inGXKLgrabTime = true;
				} else {
					if(inGXKLgrabTime) {
						GXKLremainTime = -1;
						isGXKLclose = true;
						//gwGXKL.resetData();
						//gwGXKL.setRemainTime(0);
						DsnProxyGrab.disableGXKLData();
						inGXKLgrabTime = false;
					}
				}
				
				if(!grabGXKL) {
					DsnProxyGrab.disableGXKLData();
				}
				
				if(grabGXKL && inGXKLgrabTime) {
					GXKLremainTime = DsnProxyGrab.getGXKLlocalRemainTime();
					if(requestTime) {
						GXKLTime= DsnProxyGrab.getGXKLtime();
						GXKLremainTime = Long.parseLong(GXKLTime[0]);
						if(GXKLremainTime > 0) {
							System.out.println("[����]�����������ʮ�ַ���:" + GXKLremainTime/1000);
							//gwGXKL.setRemainTime(GXKLremainTime);
						}
						else {
							System.out.println("[����]�����������ʮ�ֿ���:" + Long.parseLong(GXKLTime[2])/1000);
							//gwGXKL.setRemainTime(Long.parseLong(GXKLTime[2]));
						}
						
					}
					while(GXKLremainTime > 20*60*1000) {//��ȡʱ��ʧ��
						if(!DsnProxyGrab.isInGXKLgrabTime()) {
							GXKLremainTime = -1;
							isGXKLclose = true;
							//gwGXKL.resetData();
							//gwGXKL.setRemainTime(0);
							DsnProxyGrab.disableGXKLData();
							inGXKLgrabTime = false;
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
							//gwGXKL.setOnlineStatus(false);
							DsnProxyGrab.disableGXKLData();
							DsnProxyGrab.disableBJSCData();
							System.out.println("�������������ʧ��,�������µ�¼....\n");
							DsnProxyGrab.connFailLogin();
							System.out.println("�������µ�¼�ɹ�\n");
							//gwGXKL.setOnlineStatus(true);
						}
						
						DsnProxyGrab.setisNeedChangeLine(false);
						DsnProxyGrab.clearAvgRequest();
						
						GXKLTime = DsnProxyGrab.getGXKLtime();
						GXKLremainTime = Long.parseLong(GXKLTime[0]);
					}
					
					if(GXKLremainTime > 0) {
						if(isGXKLclose) {
							//gwGXKL.setCloseText(false);
							//gwGXKL.resetData();
							isGXKLclose = false;
						}
						if(!requestTime) {
							System.out.println("[����][�����������ʮ����ʱ��Ϊ]:" + GXKLremainTime/1000);
						}
						if(GXKLremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(GXKLremainTime <= 0){
						if(!isGXKLclose) {
							//gwGXKL.setCloseText(true);
							isGXKLclose = true;
							requestTime = true;
							sleepTime = 8*1000;
							//DsnProxyGrab.disableGXKLData();
							continue;
						}
					}
					
					//gwGXKL.setDrawNumber(GXKLTime[1]);
				}
				
				
				if(grabGXKL && inGXKLgrabTime) {
					String data = DsnProxyGrab.grabGXKLdata();
					if(data == "timeout") {
						continue;
					}else if(data == null) {
						isNeedLogin = true;
						continue;
					}
					
					if(GXKLremainTime > 2) {
						DsnProxyGrab.setGXKLdata(GXKLTime[1], data, Long.toString(DsnProxyGrab.getGXKLlocalRemainTime()/1000));
					}
					else {
						DsnProxyGrab.setGXKLdata(GXKLTime[1], data, Long.toString(GXKLremainTime/1000));
					}
					String [] datas = {data};
					//gwGXKL.setData(datas);
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
    	//gwGXKL.setVisible(true);
    }
    
    public  void stopGrabGXKL() {
    	grabGXKL = false;
    	//gwGXKL.resetData();
    	//gwGXKL.setVisible(false);
    }
    
}
