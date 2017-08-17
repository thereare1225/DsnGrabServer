package dsn;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class InitBetData {
	
	
	BufferedWriter fw = null;  //写	
	BufferedReader reader = null; //读
	
	int count = 0;
	double sum = 0;
	double allSum = 0;
	double sum1 = 0;
	int allCount = 0;
	long lastPeriod = 0;
	
	boolean positive = true;
	double percent = 1;
	double profit = 0;
	double profit1 = 0;
	int todayCount = 0;
	double todayProfit = 0;
	
	boolean open = true;
	
	GrabBJSCwindow wd = null;
	DsnProxyGrab dsnProxyGrab;
	
	InitBetData(GrabBJSCwindow wd, DsnProxyGrab dsnProxyGrab) {
		this.wd = wd;
		this.dsnProxyGrab = dsnProxyGrab;
	}
	
	
	public static String[] parseResult(String result) {
		int posStart = result.indexOf("两面");
		posStart = result.indexOf("a href=", posStart);
		posStart = result.indexOf("<td>", posStart);
		posStart = result.indexOf("<td>", posStart +3) + 4;
		int posEnd = result.indexOf("<", posStart);
		String [] data = {"", ""};
		data[0] = result.substring(posStart, posEnd);
		posStart = result.indexOf("<td>", posEnd);
		posStart =  result.indexOf("<td>", posStart + 3);
		posStart =  result.indexOf("<td>", posStart + 3) + 4;
	    posEnd = result.indexOf("<", posStart);
	    data[1] =result.substring(posStart, posEnd);
		
		//System.out.println(data);
		return data;
	}
	
	public  void initBetResDataFile(){
		
		
		try{
			
			
	    	long time = System.currentTimeMillis();
	    	Date date = new Date(time);			
			int day = date.getDate();
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
			
			String currentDrawNumber = null;
			
			File dir = new File("BJSCresData");
			if (dir.exists()) {
			} else {
				dir.mkdirs();
			}
			
			if(!dsnProxyGrab.isInBJSCgrabTime()){
				
				if(day > 1){
					day = day -1;
					
					long endNumber = dsnProxyGrab.getEndDrawNumber(df.format(date) + "-" + String.format("%02d",day));
					
					currentDrawNumber = Long.toString(endNumber);
					
				}else{
					

					
					// 设置日期格式
					File file = new File("BJSCresData/" + dsnProxyGrab.getACCOUNT() + df.format(date)
							+ ".txt");
					
					fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常
					
					return;
				}
				
				
				
			}else{
				currentDrawNumber = dsnProxyGrab.getBJSCTime()[1];
				
				if (currentDrawNumber.equals("0")){
					currentDrawNumber = dsnProxyGrab.getBJSCTime()[1];
				}
			}
			
			

			
			if(!currentDrawNumber.equals("0")){
				
				long drawNumber = 0;
				
				if(!dsnProxyGrab.isInBJSCgrabTime()){
					drawNumber = Long.parseLong(currentDrawNumber);
				}else{
					drawNumber = Long.parseLong(currentDrawNumber) - 1;
				}
				
				
				
				try {
					

					File file = new File("BJSCresData/" + dsnProxyGrab.getACCOUNT()  + df.format(new Date())
							+ ".txt");
					
					
					
					if(!file.exists())    
					{    
					    try {    
					        file.createNewFile();
					        
					        fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
					        
					        long startNumber = dsnProxyGrab.getStartDrawNumber(df.format(new Date()) + "-01");
					        
					        System.out.println(startNumber);
					        
					        
							long period = startNumber;
							for(long i = startNumber; i <= drawNumber; ) {
								try {
									Thread.sleep(200);
								}
								catch (InterruptedException e) {
						            // TODO: handle exception
						        }
								
								if(saveBetResToFile(i)){
									i++;
								}else{
									while(!dsnProxyGrab.doLogin()){
										
									}
								}
									

								
							}
					        
					        
					        
					    } catch (IOException e) {    

					        e.printStackTrace();    
					    }    
					}else{
						

						
						fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常

						
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); // 指定读取文件的编码格式，要和写入的格式一致，以免出现中文乱码,
						
						String str = null;
						
						String lastLine = null;
						
						while ((str = reader.readLine()) != null) {
							
							if(str.contains(",")){
								lastLine = str;
								double number = 0;
								str = str.substring(str.indexOf(",") + 1);
								
								number = Double.valueOf(str);
								
								putMethod1(number);
							}
					
						}  
					
						int posEnd = lastLine.indexOf(",");
						
						if(posEnd == -1){
							return;
						}
						
						long startNumber = Long.parseLong(lastLine.substring(0, posEnd));
						lastPeriod = startNumber;
						
						System.out.println(startNumber);
						
						
						
						for(long i = startNumber+1; i <= drawNumber; ) {
							try {
								Thread.sleep(200);
							}
							catch (InterruptedException e) {
					            // TODO: handle exception
					        }
							
							if(saveBetResToFile(i)){
								i++;
							}else{
								while(!dsnProxyGrab.doLogin()){
									
								}
							}
						
						}

					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		}catch(Exception e){
			
		}	
	}
	
	public void putMethod1(double number) {
		if(number > 0) {
			count++;
			allCount++;
		} else if(number < 0){
			count--;
			allCount--;
		}
		
		if(todayCount == 179) {
			todayCount = 1;
			todayProfit = 0;
		}
		else {
			todayCount++;
		}
			
		todayProfit += number;
		sum += number;
		allSum += number;
		sum1 += number;
		
		if(open || profit1 > -1000000) {
			if(positive)
				profit += number * percent;
			else
				profit -= number * percent;
		}
		
		if(positive)
			profit1 += number * percent;
		else
			profit1 -= number * percent;
		
		
		
		if(count >= 30 && sum > 800000) {
			if(!positive) {
				percent *= 2;
			} else{
				if (percent - 1 < 0.00001 && percent - 1 > - 0.00001) {
					positive = false;
				} else {
					percent = 1;
				}
			}
			sum = 0;
			count = 0;
			sum1 = 0;
		}
		
		if(count <= -30 && sum < -800000) {
			if(positive) {
				percent *= 2;
			} else{
				if (percent - 1 < 0.00001 && percent - 1 > - 0.00001) {
					positive = true;
				} else {
					percent = 1;
				}
			}
			sum = 0;
			count = 0;
			sum1 = 0;
		}
		
		open = true;
		
		if(positive && sum1 < -100000) {
			open = false;
		}
		
		if(!positive && sum1 > 100000) {
			open = false;
		}
		
		/*if(positive && (sum1 < -3500000 || count < -27)) {
			open = true;
			sum1 = 0;
		}
		
		if(!positive && (sum1 > 3500000 || count > 27)) {
			open = true;
			sum1 = 0;
		}*/
		
		if(profit1 > -1000000) {
			open = true;
		}
		
		dsnProxyGrab.setBetBJSCopen(open);
		
		wd.setTextFieldPercent(String.valueOf(percent));
		wd.setTextFieldPositive(String.valueOf(positive));
		wd.setTextFieldProfit(String.format("%.1f", allSum));
		wd.setTextFieldTimes(String.valueOf(allCount));
		wd.setTextFieldOurProfit(String.format("%.1f", profit1));
		wd.setTextFieldZhiSunProfit(String.format("%.1f", profit));
		wd.setTextFieldTodayProfit(String.format("%.1f", todayProfit));
	}
	
	public void putMethod(double number) {
		if(number > 0) {
			count++;
			allCount++;
		} else {
			count--;
			allCount--;
		}
		
		sum += number;
		allSum += number;
		if(positive)
			profit += number * percent;
		else
			profit -= number * percent;
		if(count >= 30 && sum > 800000) {
			if(!positive) {
				percent *= 2;
			} else{
				if (percent - 1 < 0.00001 && percent - 1 > - 0.00001) {
					positive = false;
				} else {
					percent = 1;
				}
			}
			sum = 0;
			count = 0;
		}
		
		if(count <= -30 && sum < -800000) {
			if(positive) {
				percent *= 2;
			} else{
				if (percent - 1 < 0.00001 && percent - 1 > - 0.00001) {
					positive = true;
				} else {
					percent = 1;
				}
			}
			sum = 0;
			count = 0;
		}
		
		wd.setTextFieldPercent(String.valueOf(percent));
		wd.setTextFieldPositive(String.valueOf(positive));
		wd.setTextFieldProfit(String.valueOf(allSum));
		wd.setTextFieldTimes(String.valueOf(allCount));
		wd.setTextFieldOurProfit(String.valueOf(profit1));
	}
	
	public String [] getMethod() {
		String[] method = {"", ""};

		method[0] = String.valueOf(percent);

		if(positive)
			method[1] = "true";
		else
			method[1] = "false";
		return method;
	}
	
	public boolean getOpen() {
		return open;
	}
	
	public  boolean saveBetResToFile(long period){
		
		boolean saveRes = false;
		if(period == lastPeriod) {
			return true;
		}
		
		if(lastPeriod == 0) {
			lastPeriod = period - 1;
		}
		
		System.out.println("save to file:" + period);
		
		try{
			
			for(long j = lastPeriod + 1; j <= period; j++) {
				boolean success = false;
				for(int i = 0; i < 4; i++){
					
					String result = dsnProxyGrab.getBJSCresult(j);
					
					//System.out.println(result);
					
					if(result.equals("") || result.equals("timeout")) {
						
						try{
							Thread.sleep(2000);
							
						}catch(Exception e){
	
						}
						
						continue;
					}
					
					if(result.contains("top.location")){
						return false;
					}
					
					//System.out.println(result);
					String [] data = parseResult(result);
					if(!Common.isNum(data[0]) || !Common.isNum(data[1])) {
						continue;
					}
					
					Double d1 = Double.parseDouble(data[0]);
					Double d2 = Double.parseDouble(data[1]);
					
					//System.out.println(period +" " + data[0] + " " + data[1]);
					
					String value = String.format("%.1f", d1 + d2);
					if(value.equals("0.0")) {
						continue;
					}
					putMethod1(Double.valueOf(value));
					fw.append(j +"," + String.format("%.1f", d1 + d2));
					fw.newLine();
					fw.flush();

					lastPeriod = j;
					success = true;
					
					break;
				}
				if(!success) {
					return false;
				}
			}
			if(lastPeriod == period) {
				saveRes = true;
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return saveRes;
	}
	
}
