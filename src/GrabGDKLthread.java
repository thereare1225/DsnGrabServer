public class GrabGDKLthread extends Thread{
	
	long almostTime = 35*1000;  //�������ʱ��
	long sleepTime = 10*1000;	//ƽʱ˯��ʱ��
    
    boolean grabGDKL = false;
    boolean isGDKLclose = false;
    boolean isNeedLogin = false;
    boolean requestTime = true;
    boolean inGDKLgrabTime = true;
    public GrabGDKLthread() {
	}
    
    @Override
    public void run() {
    	try {
    		String[] GDKLTime = {"0", "0", "0"};
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
						//gwGDKL.setOnlineStatus(false);
						DsnProxyGrab.disableGDKLData();
						DsnProxyGrab.disableBJSCData();
						System.out.println("�������������ʧ��,�������µ�¼....\n");
						DsnProxyGrab.connFailLogin();
						System.out.println("�������µ�¼�ɹ�\n");
						//gwGDKL.setOnlineStatus(true);
					}
					
					DsnProxyGrab.setisNeedChangeLine(false);
					DsnProxyGrab.clearAvgRequest();
					
					isNeedLogin = false;
				}
				
				long GDKLremainTime = 0;
				if(DsnProxyGrab.isInGDKLgrabTime()) {
						inGDKLgrabTime = true;
				} else {
					if(inGDKLgrabTime) {
						GDKLremainTime = -1;
						isGDKLclose = true;
						//gwGDKL.resetData();
						//gwGDKL.setRemainTime(0);
						DsnProxyGrab.disableGDKLData();
						inGDKLgrabTime = false;
					}
				}
				
				if(!grabGDKL) {
					DsnProxyGrab.disableGDKLData();
				}
				
				if(grabGDKL && inGDKLgrabTime) {
					GDKLremainTime = DsnProxyGrab.getGDKLlocalRemainTime();
					if(requestTime) {
						GDKLTime= DsnProxyGrab.getGDKLtime();
						GDKLremainTime = Long.parseLong(GDKLTime[0]);
						if(GDKLremainTime > 0) {
							System.out.println("[����]��������ũ������:" + GDKLremainTime/1000);
							//gwGDKL.setRemainTime(GDKLremainTime);
						}
						else {
							System.out.println("[����]��������ũ���ʿ���:" + Long.parseLong(GDKLTime[2])/1000);
							//gwGDKL.setRemainTime(Long.parseLong(GDKLTime[2]));
						}
						
					}
					
					while(GDKLremainTime > 10*60*1000) {//��ȡʱ��ʧ��
						if(!DsnProxyGrab.isInGDKLgrabTime()) {
							GDKLremainTime = -1;
							isGDKLclose = true;
							//gwGDKL.resetData();
							//gwGDKL.setRemainTime(0);
							DsnProxyGrab.disableGDKLData();
							inGDKLgrabTime = false;
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
							//gwGDKL.setOnlineStatus(false);
							DsnProxyGrab.disableGDKLData();
							DsnProxyGrab.disableBJSCData();
							System.out.println("�������������ʧ��,�������µ�¼....\n");
							DsnProxyGrab.connFailLogin();
							System.out.println("�������µ�¼�ɹ�\n");
							//gwGDKL.setOnlineStatus(true);
						}
						
						DsnProxyGrab.setisNeedChangeLine(false);
						DsnProxyGrab.clearAvgRequest();
						
						GDKLTime = DsnProxyGrab.getGDKLtime();
						GDKLremainTime = Long.parseLong(GDKLTime[0]);
					}
					
					if(GDKLremainTime > 0) {
						if(isGDKLclose) {
							//gwGDKL.setCloseText(false);
							//gwGDKL.resetData();
							isGDKLclose = false;
						}
						if(!requestTime) {
							System.out.println("[����][��������ũ������ʱ��Ϊ]:" + GDKLremainTime/1000);
						}
						if(GDKLremainTime < almostTime) {
							sleepTime = 2*1000;
							requestTime = false;
						}
						
					} else if(GDKLremainTime <= 0){
						if(!isGDKLclose) {
							//gwGDKL.setCloseText(true);
							isGDKLclose = true;
							requestTime = true;
							sleepTime = 10*1000;
							//DsnProxyGrab.disableGDKLData();
							continue;
						}
					}
					
					//gwGDKL.setDrawNumber(GDKLTime[1]);
				}
				
				
				if(grabGDKL && inGDKLgrabTime) {
					String [] data = DsnProxyGrab.grabGDKLdata();
					if(data == null) {
						data = DsnProxyGrab.grabGDKLdata();
					}
					
					if(data == null) {
						data = DsnProxyGrab.grabGDKLdata();
					}
					
					if(data != null) {
						if(GDKLremainTime > 2) {
							DsnProxyGrab.setGDKLdata(GDKLTime[1], data, Long.toString(DsnProxyGrab.getGDKLlocalRemainTime()/1000));
						}
						else {
							DsnProxyGrab.setGDKLdata(GDKLTime[1], data, Long.toString(GDKLremainTime/1000));
						}
						
						//gwGDKL.setData(DsnProxyGrab.getGDKLshowData());
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
    	//gwGDKL.setVisible(true);
    }
    
    public  void stopGrabGDKL() {
    	grabGDKL = false;
    	//gwGDKL.resetData();
    	//gwGDKL.setVisible(false);
    }
	
}
