import java.io.IOException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.io.PrintStream;
import java.util.Date;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class InitBetData {
	
	
	BufferedWriter fw = null;  //写	
	BufferedReader reader = null; //读
	
	int count = 0;
	double sum = 0;
	long lastPeriod = 0;
	
	boolean positive = true;
	double percent = 1;
	
	double profit = 0;
	
	InitBetData() {
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
			
			if(!DsnProxyGrab.isInBJSCgrabTime()){
				
				if(day > 1){
					day = day -1;
					
					long endNumber = DsnProxyGrab.getEndDrawNumber(df.format(date) + "-" + String.format("%02d",day));
					
					currentDrawNumber = Long.toString(endNumber);
					
				}else{
					

					
					// 设置日期格式
					File file = new File("BJSCresData/" + df.format(date)
							+ ".txt");
					
					fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常
					
					return;
				}
				
				
				
			}else{
				currentDrawNumber = DsnProxyGrab.getBJSCTime()[1];
				
				if (currentDrawNumber.equals("0")){
					currentDrawNumber = DsnProxyGrab.getBJSCTime()[1];
				}
			}
			
			

			
			if(!currentDrawNumber.equals("0")){
				
				long drawNumber = 0;
				
				if(!DsnProxyGrab.isInBJSCgrabTime()){
					drawNumber = Long.parseLong(currentDrawNumber);
				}else{
					drawNumber = Long.parseLong(currentDrawNumber) - 1;
				}
				
				
				
				try {
					

					File file = new File("BJSCresData/" + df.format(new Date())
							+ ".txt");
					
					
					
					if(!file.exists())    
					{    
					    try {    
					        file.createNewFile();
					        
					        fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
					        
					        long startNumber = DsnProxyGrab.getStartDrawNumber(df.format(new Date()) + "-01");
					        
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
									while(!DsnProxyGrab.doLogin()){
										
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
							//System.out.println(str);
							
							if(str.contains(",")){
								lastLine = str;
								double number = 0;
								str = str.substring(str.indexOf(",") + 1);
								
								number = Double.valueOf(str);
								//System.out.println("number:" + number);
								
								putMethod(number);
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
								while(!DsnProxyGrab.doLogin()){
									
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
	
	public void putMethod(double number) {
		if(number > 0) {
			count++;
		} else {
			count--;
		}
		
		sum += number;
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
	
	public  boolean saveBetResToFile(long period){
		
		boolean saveRes = false;
		if(period == lastPeriod) {
			return true;
		}
		
		System.out.println("save to file:" + period);
		
		try{
			for(long j = lastPeriod + 1; j <= period; j++) {
			
				for(int i = 0; i < 3; i++){
					
					String result = DsnProxyGrab.getBJSCresult(j);
					
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
					putMethod(Double.valueOf(value));
					fw.append(j +"," + String.format("%.1f", d1 + d2));
					fw.newLine();
					fw.flush();

					lastPeriod = j;
					
					break;
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
