package dsn;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class BetBJSCdataFactory {
	
	
	BufferedWriter[] fw = null;  //写	
	BufferedReader[] reader = null; //读
	Map<String, Double> resultsToSave = null;
	Long [] lastPeriods = null;
	
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
	static String [] dataBJSC = {"", "", "", "", "", "", ""};
	
	boolean open = true;
	
	GrabBJSCwindow wd = null;
	DsnProxyGrab [] dsnProxyGrabs;
	int proxyNum = 0;
	
	BetBJSCdataFactory(GrabBJSCwindow wd, DsnProxyGrab [] dsnProxyGrabs) {
		this.wd = wd;
		this.dsnProxyGrabs = dsnProxyGrabs;
		proxyNum = dsnProxyGrabs.length;
		fw = new BufferedWriter[proxyNum];
		reader = new BufferedReader[proxyNum];
		lastPeriods = new Long[proxyNum];
		for(int i = 0; i < proxyNum; i++) {
			lastPeriods[i] = 0L;
		}
		resultsToSave = new HashMap<String, Double>();
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
	
	public boolean initBetResDataFile(){	
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
			
			if(!dsnProxyGrabs[0].isInBJSCgrabTime()){
				
				if(day > 1){
					day = day -1;
					
					long endNumber = dsnProxyGrabs[0].getEndDrawNumber(df.format(date) + "-" + String.format("%02d",day));
					
					currentDrawNumber = Long.toString(endNumber);
					
				}else{
					for(int i = 0; i < proxyNum; i++) {
						File file = new File("BJSCresData/" + dsnProxyGrabs[i].getACCOUNT() + df.format(date)
								+ ".txt");						
						fw[i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常					
					}
					
					return true;
				}		
			}else{
				currentDrawNumber = dsnProxyGrabs[0].getBJSCTime()[1];
				
				if (currentDrawNumber.equals("0")){
					currentDrawNumber = dsnProxyGrabs[0].getBJSCTime()[1];
				}
			}
			
			if(!currentDrawNumber.equals("0")){
				long drawNumber = 0;
				long startNumber = 0;
				if(!dsnProxyGrabs[0].isInBJSCgrabTime()){
					drawNumber = Long.parseLong(currentDrawNumber);
				}else{
					drawNumber = Long.parseLong(currentDrawNumber) - 1;
				}
			
				for(int i = 0; i < proxyNum; i++) {
					File file = new File("BJSCresData/" + dsnProxyGrabs[i].getACCOUNT() + df.format(new Date())
							+ ".txt");		
					
					if(!file.exists()) {  
						System.out.println("文件" +  dsnProxyGrabs[i].getACCOUNT() + df.format(new Date())+ ".txt" + "不存在");
						return false;
					}
				}
				
				for(int i = 0; i < proxyNum; i++) {
					File file = new File("BJSCresData/" + dsnProxyGrabs[i].getACCOUNT() + df.format(new Date())
							+ ".txt");
					fw[i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")); 
					reader[i] = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));	
					String str;
					if ((str = reader[i].readLine()) != null && str.contains(",")) {
						str = str.substring(0, str.indexOf(","));
						System.out.println(str);
						startNumber = Long.valueOf(str);
					} else {
						System.out.println("文件" +  dsnProxyGrabs[i].getACCOUNT() + df.format(new Date())+ ".txt" + "格式错误");
						return false;
					}
				}
				
				lastPeriod = startNumber;
				startNumber++;
				
				while(startNumber <= drawNumber) {
					double sum = 0;
					boolean success = true;
					for(int i = 0; i < proxyNum; i++) {
						String str = "";
						if(reader[i] != null) {	
							if ((str = reader[i].readLine()) != null) {
								if(startNumber != Long.valueOf(str.substring(0, str.indexOf(",")))) {
									System.out.println(dsnProxyGrabs[i].getACCOUNT() + ": " + startNumber + "缺少期数！！！");
									return false;
								}
								str = str.substring(str.indexOf(",") + 1);
								sum += Double.valueOf(str);
							}else {
								reader[i].close();
								reader[i] = null;
								while((str = getResult(dsnProxyGrabs[i], startNumber)) == null) {
								}
								fw[i].append(startNumber +"," + Double.valueOf(str));
								fw[i].newLine();
								fw[i].flush();
								System.out.println(dsnProxyGrabs[i].getACCOUNT() + startNumber +  "  save:" + str);
								sum += Double.valueOf(str);
							}
						} else {
							while((str = getResult(dsnProxyGrabs[i], startNumber)) == null) {
							} 
							fw[i].append(startNumber +"," + Double.valueOf(str));
							fw[i].newLine();
							fw[i].flush();
							System.out.println(dsnProxyGrabs[i].getACCOUNT() + startNumber +  "  save:" + str);
							sum += Double.valueOf(str);
						}
					}
					
					putMethod1(sum);
					lastPeriod = startNumber;
					startNumber++;
				}
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
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
		
		//dsnProxyGrab.setBetBJSCopen(open);
		
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
	
	public String getResult(DsnProxyGrab dsnProxyGrab, long period) {
		for(int i = 0; i < 4; i++){
			
			String result = dsnProxyGrab.getBJSCresult(period);
			
			//System.out.println(result);
			
			if(result.equals("") || result.equals("timeout")) {
				
				try{
					Thread.sleep(2000);		
				}catch(Exception e){
				}
				
				continue;
			}
			
			if(result.contains("top.location")){
				return null;
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
			return value;
		}
		return null;
	}
	
	public String[] CombineData() {
		String [] comData= null;
		//String [] dataBJSC = {"", "", "", "", "", "", ""};
//		  dataBJSC[0] = drawNumber;
//	  	  dataBJSC[1] = data[0];
//	  	  dataBJSC[2] = data[1];
//	  	  dataBJSC[3] = data[2];
//	  	  dataBJSC[4] = remainTime;
//	  	  dataBJSC[5] = percent;
//	  	  dataBJSC[6] = positive;
		Long tmp = 0L;
		for(int i = 0; i < dsnProxyGrabs.length; i++) {
			if(dsnProxyGrabs[i].getBJSCdrawNumber() > tmp) {
				tmp = dsnProxyGrabs[i].getBJSCdrawNumber();
			}
		}
		
		if(tmp == 0) {
			return null;
		}
		
		String drawNumber = String.valueOf(tmp);
		boolean init = false;
		for(int i = 0; i < dsnProxyGrabs.length; i++) {
			String [] data = dsnProxyGrabs[i].getBJSCdata();
			if(data != null && data[0].equals(drawNumber)) {
				if(!init) {
					comData = data;
					init = true;
				} else {
					String []data1 = {comData[1], comData[2], comData[3]};
					String []data2 = {data[1], data[2], data[3]};
					try {
						data1 = Common.combineBJSCdata(data1, data2);
					}catch (Exception e){
						e.printStackTrace();
						return null;
					}
					comData[1] = data1[0];
					comData[2] = data1[1];
					comData[3] = data1[2];
				}
			}
		}
		
		if(init) {
			comData[5] = String.valueOf(percent);
			comData[6] = positive? "true":"false";
			return comData.clone();
		}
		
		return null;
	}
	
	public  boolean saveBetResToFile(long period){
		
		boolean saveRes = false;
		if(period == 0) {
			return false;
		}
		if(period == lastPeriod) {
			return true;
		}
		
		if(lastPeriod == 0) {
			lastPeriod = period - 1;
		}
		
		System.out.println("save to file:" + period);
		
		try{
			
			for(long j = lastPeriod + 1; j <= period; j++) {
				Double sum = 0.0;
				Double [] results = new Double[proxyNum];
				for(int k = 0; k < proxyNum; k++) {
					String resultString = getResult(dsnProxyGrabs[k], j);
					if(resultString != null) {
						results[k] = Double.valueOf(resultString);
						sum += results[k];
					} else {
						return false;
					}
				}
				
				for(int k = 0; k < proxyNum; k++) {
					fw[k].append(j +"," + String.format("%.1f", results[k]));
					fw[k].newLine();
					fw[k].flush();
				}
				
				lastPeriod = j;
				putMethod1(sum);
			}
					
			if(lastPeriod == period) {
				saveRes = true;
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		
		return saveRes;
	}
	
}
