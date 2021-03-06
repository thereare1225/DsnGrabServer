package dsn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.protocol.HttpClientContext;

import java.io.*; 

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;

import com.sun.jna.platform.win32.WinDef.LONG;

public class DsnProxyGrab {
	 CloseableHttpClient httpclient = null;
     RequestConfig requestConfig = null;
     HttpClientContext clientContext = null;
    
    // Map<String,String> cookieMap = new HashMap<String, String>(64);
     String strCookies = "";
     String cookieuid = "";
     String cookiedae = "";
     String [] dataCQSSC = {"", "", ""};
     String [] dataGXKL = {"", "", ""};
     String [] dataTJSSC = {"", "", ""};
     String [] dataXJSSC = {"", "", ""};
     String [] dataGD115 = {"", "", ""};
     String [] dataBJKL8 = {"", "", ""};
     String [] dataBJSC = {"", "", "", "", "", "", ""};
     String [] dataXYNC = {"", "", "", "", "", "", "", "", "", "", ""};
     String [] dataGDKL = {"", "", "", "", "", "", "", "", "", "", ""};
     boolean betBJSCopen = false;
     long BJSCdrawNumber = 0;

	 public long getBJSCdrawNumber() {
		return BJSCdrawNumber;
	}

	boolean isCQSSCdataOk = false;
     boolean isBJSCdataOk = false;
     boolean isXYNCdataOk = false;
     boolean isGXKLdataOk = false;
     boolean isGDKLdataOk = false;
     boolean isTJSSCdataOk = false;
     boolean isXJSSCdataOk = false;
     boolean isGD115dataOk = false;
     boolean isBJKL8dataOk = false;
     boolean isInReLogin = false;
    
    
    private  String ADDRESS = "";

	private  String ACCOUNT = "";
    private  String PASSWORD = "";
    private  long timeDValue = 0;  //网站时间和电脑时间的差值  网页时间 - 当前时间
    private  long closeTimeGXKL = 0;
    private  long closeTimeGDKL = 0;
    private  long closeTimeTJSSC = 0;
    private  long closeTimeXJSSC = 0;
    private  long closeTimeGD115 = 0;
    private  long closeTimeBJKL8 = 0;
    private  long closeTimeBJSC = 0;
    private  ReadWriteLock lockCQSSC = new ReentrantReadWriteLock();
    private  ReadWriteLock lockBJSC = new ReentrantReadWriteLock();
    private  ReadWriteLock lockXYNC = new ReentrantReadWriteLock();
    private  ReadWriteLock lockGXKL = new ReentrantReadWriteLock();
    private  ReadWriteLock lockGDKL = new ReentrantReadWriteLock();
    private  ReadWriteLock lockTJSSC = new ReentrantReadWriteLock();
    private  ReadWriteLock lockXJSSC = new ReentrantReadWriteLock();
    private  ReadWriteLock lockGD115 = new ReentrantReadWriteLock();
    private  ReadWriteLock lockBJKL8 = new ReentrantReadWriteLock();
    
     {
         requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
         requestConfig = RequestConfig.copy(requestConfig).setRedirectsEnabled(false).build();//禁止重定向 ， 以便获取cookiedae
         requestConfig = RequestConfig.copy(requestConfig).setConnectTimeout(15*1000).setConnectionRequestTimeout(15*1000).setSocketTimeout(15*1000).build();//设置超时
         httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
    }
    
    
    //优化线路选择
     Vector<Object[]> lines;
    
     Vector<Long> lastTenRequestTime = new Vector<Long>();
     long avgRequestTime = 0;    
     boolean bcalcRequestTime = true;
     boolean bneedChangeLine = false;
    
    
    
    

	public  String setCookie(CloseableHttpResponse httpResponse) {
		//System.out.println("----setCookieStore");
		Header headers[] = httpResponse.getHeaders("Set-Cookie");
		if (headers == null || headers.length==0)
		{
			//System.out.println("----there are no cookies");
			return "";
		}
		String cookie = "";
		for (int i = 0; i < headers.length; i++) {
			cookie += headers[i].getValue();
			if(i != headers.length-1)
			{
				cookie += ";";
			}
		}
		//System.out.println("----cookies:" + cookie);
		String cookies[] = cookie.split(";");
		
		for (String c : cookies)
		{
			if(c.indexOf("path=") != -1 || c.indexOf("expires=") != -1 || c.indexOf("domain=") != -1 ||
			   c.indexOf("Max-Age=") != -1 || c.indexOf("HttpOnly") != -1 || c.indexOf("Expires=") != -1)
				continue;
			strCookies += c;
			strCookies += ";";
		}
		
		int binPos = strCookies.indexOf("LOGINCHK=Y");
		if(binPos != -1)
		{
			strCookies = strCookies.substring(binPos);
		}
		//System.out.println("----cookies:" + strCookies);
		//System.out.println("----setCookieStore success");

		return strCookies;
	}
    
	private  void loginInit() {
		strCookies = "";
        cookieuid = "";
        cookiedae = "";
        clientContext = HttpClientContext.create();
        clientContext.setRequestConfig(requestConfig);
	}

    public  boolean doLogin() { 	
        loginInit();
        String loginPage = doGet(ADDRESS + "/login", ""); //get 登录页面
        
        if(loginPage != "" && loginPage != "timeout") {
        	cookieuid = strCookies;
        	int posStart = loginPage.indexOf("img src=") + 9;
        	if(posStart >= 9) {
        		int posEnd = loginPage.indexOf('"', posStart);
        		String rmNum = getPicNum(ADDRESS + "/" + loginPage.substring(posStart, posEnd));//get 验证码
        		System.out.println("yzm: " + rmNum); 
        		if(!Common.isNum(rmNum)) {
        			return false;
        		}
        	
        		//发送post
        		List<NameValuePair> params = new ArrayList<NameValuePair>();
        		params.add(new BasicNameValuePair("type", "2"));
        		params.add(new BasicNameValuePair("account", ACCOUNT));
        		params.add(new BasicNameValuePair("password", PASSWORD));
        		params.add(new BasicNameValuePair("code", rmNum));
        		String location = doPost(ADDRESS + "/login", params, strCookies, "");
        		
        		System.out.println("location: " + location); 

            
        		if(location.indexOf("index?") > 0) {
        			strCookies = "";
        			if(location.indexOf("http") >= 0) {
        				location = doGet(location, cookieuid);//get cookiedae和重定向url
        			}
        			else {
        				location = doGet(ADDRESS + location, cookieuid);
        			}
        			cookiedae = strCookies;
        			strCookies = "";
        			if(location.indexOf("http") >= 0) {
        				location = doGet(location, cookieuid + cookiedae);
        			}
        			else {
        				location = doGet(ADDRESS + location, cookieuid + cookiedae);
        			}
        			if(location != "" && location != "timeout"){
        				return true;
        			}
        		}
            
        	}
        }
         
        return false;
    }
    
    public void setLoginParams(String address, String account, String password){
    	ADDRESS = address;
    	ACCOUNT = account;
    	PASSWORD = password;
    }
    
    public  void setLoginAddress(String address){
    	ADDRESS = address;
    }
    
    
    public  void initLines(){
    	String[] addressArray = ConfigReader.getProxyAddressArray();
    	

    	
    	lines = new Vector<Object[]>(addressArray.length);
    	
    	for(int k = 0; k < addressArray.length; k++){
    		lines.add(new Object[2]);
    		lines.elementAt(k)[0] = new String(addressArray[k]);
    		
    		lines.elementAt(k)[1] = new Long(10*1000);
    	}
    }
    
    
    
    @SuppressWarnings("unchecked")
	public  void setLinePriority(){
    	
    	boolean res = false;

    	long timeStart;
    	long timeEnd;
    	
    	long averageTime;
    	
    	
    	setIscalcRequestTime(false);


    	for(int k = 0; k < lines.size(); k++){
    		

    		
    		setLoginAddress((String)lines.elementAt(k)[0]);
    		
    		int i =0;
    		
    		long timePassing = 0;
    		
    		timeStart = System.currentTimeMillis();
    		
    		for( i = 0; i < 4; i++) {
    			
    			
    			
        		if(doLogin()) {
        			res = true;

        			break;
        		}
        		
        		long time1 = System.currentTimeMillis();
        		
        		timePassing += (time1 - timeStart);
        		
        		if(timePassing > 10){
        			break;
        		}
        	}
    		
    		timeEnd = System.currentTimeMillis();
    		
    		long usingTime = timeEnd - timeStart;
    		
    		
    		averageTime = usingTime/(i+1);
    		lines.elementAt(k)[1] = averageTime;
    		
    		
    		
    		res = false;
    		
    		

    	}

    	
    	Comparator ct = new MyCompare();
    	
    	Collections.sort(lines, ct);
    	
    	setIscalcRequestTime(true);
    	

    	System.out.println("【代理】线路快慢排序:");
    	
    	for(int j = 0; j < lines.size(); j++){
    		
    		
    		
    		System.out.println(lines.elementAt(j)[0]);
    		
    		System.out.println(lines.elementAt(j)[1]);
    		
    	}
    }
    
    
    public  void calcRequestAveTime(long requestTime){
        
    	if(bcalcRequestTime == true){
    		
        	//requestCount++;
        	
    		long totalReqeustTime = 0;
    		
        	lastTenRequestTime.add(requestTime);
        	
        	while(lastTenRequestTime.size() >10){
        		lastTenRequestTime.remove(0);
        	}
        	
        	
        	if(lastTenRequestTime.size() == 10){
            	for(int i = 0; i < lastTenRequestTime.size(); i++){
            		totalReqeustTime += lastTenRequestTime.elementAt(i);
            	}
            	avgRequestTime = totalReqeustTime/lastTenRequestTime.size();
            	//System.out.printf("[代理]平均请求时间:%d\n", avgRequestTime);
            	
            	if(avgRequestTime >= 400){
            		
            		setisNeedChangeLine(true);
            	}

        	}

    	}

    		
    	
    }
    
    public  void setIscalcRequestTime(boolean flag){
    	bcalcRequestTime = flag;
    }
    
    public  void setisNeedChangeLine(boolean flag){
    	bneedChangeLine = flag;
    }
    
    public  boolean getIsisNeedChangeLine(){
    	return bneedChangeLine;
    }
    
    public  void clearAvgRequest(){
    	
    	if(lastTenRequestTime.size() >0){
    		lastTenRequestTime.clear();
    	}
    	avgRequestTime = 0;

    }
    
    
    public  boolean login() {  
    	
    	
    	boolean res = false;

    	setIscalcRequestTime(false);
		
    	for(int i = 0; i < 8; i++) {
    		if(doLogin()) {
    			res = true;
    			break;
    		}
    	}
        	
    	String currentAddress = ADDRESS;

    	if(res == false){
    		
        	String[] addressArray = ConfigReader.getProxyAddressArray();
        	
        	
        	
        	for(int k = 0; k < addressArray.length; k++){
        		
        		
        		if(currentAddress.equals(addressArray[k]))
        				continue;
        		
        		setLoginAddress(addressArray[k]);
        		
        		for(int i = 0; i < 8; i++) {
            		if(doLogin()) {
            			res = true;
            			
            			ConfigWriter.updateProxyAddress(ADDRESS);//更新到现在登得上的网址
            			
            			ConfigWriter.saveTofile("common.config");
            			
            			break;
            		}
            	}
            	
            	if(res == true){
            		break;
            	}
        	}
    	}
    	
    	
    	setIscalcRequestTime(true);

    	    	
    	return res;
    	

    }
    
    public  int reLogin() {  
    	
    	if(isInReLogin) {
    		return -1;
    	}
    	
    	isInReLogin = true;
    	int res = 1;

    	setIscalcRequestTime(false);


        if(doLogin()) {
        	res = 0;
        }
    	
    	setIscalcRequestTime(true);
    	
    	isInReLogin = false;
        	
    	return res;
    	

    }
    
    
    
    
    
    public  void connFailLogin() {
    	
    	
    	boolean res = false;
    	
    	setIscalcRequestTime(false);

    	while(res == false){   	      	
        	
    		if(doLogin()) {
    			res = true;

    			break;
    		}
            	
            try{
            	Thread.currentThread().sleep(20*1000);
            }catch(Exception e){
            		
            }
        	
        	
    	}
    	
    	setIscalcRequestTime(true);
    	

    }


    /**以utf-8形式读取*/
    public  String doPost(String url, List<NameValuePair> formparams, String cookies, String referUrl) {
        return doPost(url, formparams, cookies, "UTF-8", referUrl);
    }

    public  String doPost(String url,List<NameValuePair> formparams, String cookies, String charset, String referUrl) {  
        
    	try {
    	HttpPost httppost = new HttpPost(url); 
        httppost.addHeader("Accept-Encoding","Accept-Encoding: gzip, deflate");
        httppost.addHeader("Origin","http://a1.dio168.net");
        httppost.addHeader("Connection","keep-alive");
        httppost.addHeader("Cache-Control","no-cache");
        httppost.addHeader("Referer", referUrl);
        httppost.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36"
        					+ " (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36");
        if(cookies != ""){
        	httppost.addHeader("Cookie", cookies);
        }
        
        System.out.println("executing post request " + httppost.getURI()); 

       
        UrlEncodedFormEntity uefEntity;
        
            uefEntity = new UrlEncodedFormEntity(formparams, charset);
            httppost.setEntity(uefEntity);
            CloseableHttpResponse response = execute(httppost);
            try {
                // 打印响应状态    
            	setCookie(response);
            	System.out.println(response.getStatusLine());
            	if(response.getStatusLine().toString().indexOf("302 Found") > 0) {
            		String location = response.getFirstHeader("Location").getValue();
            		//System.out.println(response.getStatusLine());
            		if(location != null) {
            			return location;
            		}
            	}
            } finally {  
            	httppost.releaseConnection();
                response.close(); 
            }  
        } catch (ClientProtocolException e) {  
            e.printStackTrace(); 
        } catch (UnsupportedEncodingException e1) {  
            e1.printStackTrace(); 
        } catch (IOException e) {  
            e.printStackTrace();
            if(e.getMessage().indexOf("timed out") > 0) {
            	return "timeout";
            }
        } 
    	catch (Exception e) {  
            e.printStackTrace();
        } 
    	
        return "";
    }
    
    
    public  CloseableHttpResponse  execute(HttpUriRequest request) throws IOException, ClientProtocolException{
    	
    	long time1 = System.currentTimeMillis();
    	long time2 = System.currentTimeMillis();
    	
    	CloseableHttpResponse response;
    	
    	try{
    		response = httpclient.execute(request);    		
    		time2 = System.currentTimeMillis();    		
    		calcRequestAveTime(time2 - time1);
    		
    	}catch(Exception e){
    		
    		time2 = System.currentTimeMillis();
    		calcRequestAveTime(time2 - time1);
    		
    		throw e;
    	}
    	

    	
    	return response;
    	
    }
    
    
    public  String doGet(String url, String cookies) { 
        try {  
           // 创建httpget.    
           //System.out.println(cookies); 
           HttpGet httpget = new HttpGet(url);
           
           httpget.addHeader("Accept-Encoding","Accept-Encoding: gzip, deflate, sdch");
           httpget.addHeader("Accept-Language","Accept-Language: zh-CN,zh;q=0.8");
           httpget.addHeader("Cache-Control","no-cache");
           httpget.addHeader("Connection","keep-alive");
           //httpget.addHeader("Content-Type","application/json; charset=UTF-8");
           //httpget.addHeader("Referer","http://www.lashou.com/");
           httpget.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36"
           					+ " (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36");  
           if(cookies != ""){
        	   httpget.addHeader("Cookie", cookies);
           }
           
           System.out.println("executing request " + url); 
          
           // 执行get请求.    
           CloseableHttpResponse response = execute(httpget); 
           //CloseableHttpResponse response = httpclient.execute(httpget); 
           
           try {          	           	   
               // 获取响应实体    
        	   setCookie(response);
               HttpEntity entity = response.getEntity(); 
               //System.out.println("--------------------------------------"); 
               
               String statusLine = response.getStatusLine().toString();
               System.out.println(statusLine);
               if(statusLine.indexOf("200 OK") == -1) {
            	   //System.out.println(statusLine); 
               }
               if(statusLine.indexOf("302 Found") > 0) {
            	   return response.getFirstHeader("Location").getValue();
               }
               //System.out.println("------------------------------------"); 
               if (entity != null) {
	            	String entityStr = EntityUtils.toString(entity);
	            	//System.out.println("entityStr: " + entityStr);
	            	if(entityStr.length() == 0) {
	            		return "";
	            	}
	            	return entityStr;
               }
           } 
           finally {
        	   httpget.releaseConnection();
               response.close(); 
           }  
       } catch (ClientProtocolException e) {  
           e.printStackTrace(); 
       } catch (ParseException e) {  
           e.printStackTrace(); 
       } catch (IOException e) {  
           e.printStackTrace(); 
           if(e.getMessage().indexOf("timed out") > 0) {
        	   return "timeout";
           }
       }
        catch (Exception e) {  
            e.printStackTrace();
        } 
        return "";
    }
    
    public  String getPicNum(String picUri) {
    	try {
    	 HttpGet httpget = new HttpGet(picUri);
         httpget.addHeader("Connection","keep-alive");
         if(strCookies != ""){
        	 httpget.addHeader("Cookie", strCookies);
         }
         httpget.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 "
         					+ "(KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36");           
         System.out.println("executing request " + httpget.getURI()); 
        
         // 执行get请求.    
        	 CloseableHttpResponse response = execute(httpget); 
        	 try {
        		 setCookie(response);
                 // 打印响应状态    
                 System.out.println(response.getStatusLine()); 
                 //System.out.println("------------------------------------");
                 if(response.getStatusLine().toString().indexOf("200 OK") < 0) {
                	 return "";
                 }
                 File storeFile = new File("yzm.png");   //图片保存到当前位置
                 FileOutputStream output = new FileOutputStream(storeFile);  
                 //得到网络资源的字节数组,并写入文件  
                 byte [] a = EntityUtils.toByteArray(response.getEntity());
                 output.write(a);  
                 output.close();  
                 
          
                 InputStream ins = null;
         		 String[] cmd = new String[]{ConfigReader.getTessPath() + "\\tesseract", "yzm.png", "result", "-l", "eng"};

         		 Process process = Runtime.getRuntime().exec(cmd);
         		 // cmd 的信息
         		 ins = process.getInputStream();
         		 BufferedReader reader = new BufferedReader(new InputStreamReader(ins));

         		 String line = null;
         	  	 while ((line = reader.readLine()) != null) {
         	  		 System.out.println(line);
         		 }
         			
         		 int exitValue = process.waitFor();
         		 System.out.println("返回值：" + exitValue);
         		 process.getOutputStream().close();
         		 File file = new File("result.txt");
         		 reader.close();
                 reader = new BufferedReader(new FileReader(file));
                  // 一次读入一行，直到读入null为文件结束
                 String rmNum;
                 rmNum = reader.readLine();
                 reader.close();
                 return rmNum;
        	 }
        	 finally{
        		 httpget.releaseConnection();
        		 response.close(); 
        	 }
         } catch (ClientProtocolException e) {  
             e.printStackTrace(); 
         } catch (UnsupportedEncodingException e1) {  
             e1.printStackTrace(); 
         } catch (IOException e) {  
             e.printStackTrace(); 
         } catch (Exception e) {
				e.printStackTrace();
		 }
    	
         
    	return "";
    }
    
  //! @brief 抓取cqssc下单数据
  //! @param game       两面:"LM", 单号:"DH", 前中后三:"QZHS"
  //! @param all        虚注:"XZ" 实占:"SZ" 补货:"BH"
  //! @param range      全部:"", A盘:"A", B盘:"B", C盘:"C", D盘:"D",
  //! @return           成功:String 连接失败:null 超时:"timeout" 
    public  String grabCQSSCdata(String game, String all, String range){
    	if((game == "LM" || game == "DH" || game == "QZHS") && (range == "" || range == "A" ||
    			range == "B" || range == "C" || range == "D") && (all == "XZ" || all == "SZ" || all == "BH")) {
    		switch (game) {
    		    case "LM":
    		      game = "DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CLH";
    		      break;
    		    case "DH":
      		      game = "B1%2CB2%2CB3%2CB4%2CB5";
      		      break;
    		    case "QZHS":
        		  game = "TS1%2CTS2%2CTS3";
        		  break;
    		   default :
    		}
    		long time =  System.currentTimeMillis();
    		String strTime = Long.toString(time);
    		String data = doGet(ADDRESS + "/agent/control/risk?lottery=CQSSC&games=" + game +"&all=" 
    								+ all + "&range=" + range + "&multiple=false&_=" + strTime, cookieuid + cookiedae);
    		if(data != "") {
    			return data;
    		}
    	}
    	return null;
    }
    
    
    //! @brief 抓取cqssc下单数据
    //! @param game       冠亚:"GY", 三四五六:"SSWL", 七八九十:"QBJS"
    //! @param all        虚注:"XZ" 实占:"SZ" 补货:"BH"
    //! @param range      全部:"", A盘:"A", B盘:"B", C盘:"C", D盘:"D",
    //! @return           成功:String 连接失败:null 超时:"timeout" 
      public  String grabBJSCdata(String game, String all, String range){
      	if((game == "GY" || game == "SSWL" || game == "QBJS") && (range == "" || range == "A" ||
      			range == "B" || range == "C" || range == "D") && (all == "XZ" || all == "SZ" || all == "BH")) {
      		switch (game) {
      		    case "GY":
      		      game = "GYH%2CGDX%2CGDS%2CB1%2CDX1%2CDS1%2CLH1%2CB2%2CDX2%2CDS2%2CLH2";
      		      break;
      		    case "SSWL":
        		      game = "B3%2CDX3%2CDS3%2CLH3%2CB4%2CDX4%2CDS4%2CLH4%2CB5%2CDX5%2CDS5%2CLH5%2CB6%2CDX6%2CDS6%2CLH6";
        		      break;
      		    case "QBJS":
          		  game = "B7%2CDX7%2CDS7%2CB8%2CDX8%2CDS8%2CB9%2CDX9%2CDS9%2CB10%2CDX10%2CDS10%2C";
          		  break;
      		   default :
      		}
      		long time =  System.currentTimeMillis();
      		String strTime = Long.toString(time);
      		String data = doGet(ADDRESS + "/agent/control/risk?lottery=BJPK10&games=" + game +"&all=" 
      								+ all + "&range=" + range + "&multiple=false&_=" + strTime, cookieuid + cookiedae);
      		if(data != "") {
      			return data;
      		}
      	}
      	return null;
      }
      
      public  String grabGXKLdata() {
    	  long time =  System.currentTimeMillis();
    		String strTime = Long.toString(time);
    		String data = doGet(ADDRESS + "/agent/control/risk?lottery=GXKLSF&games=YDX1%2CYDS1%2CYWDX1%2CYHDS1%2CYDX2%2"
    				+ "CYDS2%2CYWDX2%2CYHDS2%2CYDX3%2CYDS3%2CYWDX3%2CYHDS3%2CYDX4%2CYDS4%2CYWDX4%2CYHDS4%2CYDX5%2CYDS5"
    				+ "%2CYWDX5%2CYHDS5%2CZDX%2CZDS%2CZWDX%2CLH15&all=XZ&range=&multiple=false&_=" + strTime, cookieuid + cookiedae);
    		if(data != "") {
    			return data;
    		}
    	return null;
      }
      
      public  String grabTJSSCdata() {
    	  long time =  System.currentTimeMillis();
  		String strTime = Long.toString(time);
  		String data = doGet(ADDRESS + "/agent/control/risk?lottery=TJSSC&games=DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%"
  				+ "2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CLH&all=XZ&range=&multiple=false&_=" + strTime, cookieuid + cookiedae);
  		if(data != "") {
  			return data;
  		}
  		return null;
      }
      
      public  String grabXJSSCdata() {
    	  long time =  System.currentTimeMillis();
    	  String strTime = Long.toString(time);
    	  String data = doGet(ADDRESS + "/agent/control/risk?lottery=XJSSC&games=DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%"
  				+ "2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CLH&all=XZ&range=&multiple=false&_" + strTime, cookieuid + cookiedae);
    	  if(data != "") {
    		  return data;
    	  }
    	  return null;
      }
      
      public  String grabGD115data() {
    	  long time =  System.currentTimeMillis();
    	  String strTime = Long.toString(time);
    	  String data = doGet(ADDRESS + "/agent/control/risk?lottery=GD11X5&games=DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%"
    	  		+ "2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CZWDX%2CLH&all=XZ&range=&multiple=false&_=" + strTime, cookieuid + cookiedae);
    	  if(data != "") {
    		  return data;
    	  }
    	  return null;
      }
      
      public  String grabBJKL8data() {
    	  long time =  System.currentTimeMillis();
    	  String strTime = Long.toString(time);
    	  String data = doGet(ADDRESS + "/agent/control/risk?lottery=KL8&games=ZDX%2CZDS%2CDXDS%2CQHH%2CDSH%2CWX%2CZHT&all=XZ&"
    	  		+ "range=&multiple=false&_=" + strTime, cookieuid + cookiedae);
    	  if(data != "") {
    		  return data;
    	  }
    	  return null;
      }
      
      public  boolean grabXYNCdata() {
    	  String data1 = "";
    	  String data2 = "";
    	  String data3 = "";
    	  String data4 = "";
    	  String data5 = "";
    	  String data6 = "";
    	  String data7 = "";
    	  String data8 = "";
    	  String data9 = "";
    	  
    	  long time =  System.currentTimeMillis();
    	  long timeStart = time;
    	  String strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data1 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B1%2CDX1%2CDS1%2CWDX1%2CHDS1%2CFW1%2CZFB1%2CLH1"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data1 != "" && data1 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data1 == "" || data1 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data2 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B2%2CDX2%2CDS2%2CWDX2%2CHDS2%2CFW2%2CZFB2%2CLH2"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data2 != "" && data2 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data2 == "" || data2 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data3 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B3%2CDX3%2CDS3%2CWDX3%2CHDS3%2CFW3%2CZFB3%2CLH3"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data3 != "" && data3 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data3 == "" || data3 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data4 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B4%2CDX4%2CDS4%2CWDX4%2CHDS4%2CFW4%2CZFB4%2CLH4"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data4 != "" && data4 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data4 == "" || data4 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data5 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B5%2CDX5%2CDS5%2CWDX5%2CHDS5%2CFW5%2CZFB5%2CLH5"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data5 != "" && data5 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data5 == "" || data5 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data6 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B6%2CDX6%2CDS6%2CWDX6%2CHDS6%2CFW6%2CZFB6%2CLH6"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data6 != "" && data6 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data6 == "" || data6 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data7 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B7%2CDX7%2CDS7%2CWDX7%2CHDS7%2CFW7%2CZFB7%2CLH7"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data7 != "" && data7 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data7 == "" || data7 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data8 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=B8%2CDX8%2CDS8%2CWDX8%2CHDS8%2CFW8%2CZFB8%2CLH8"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data8 != "" && data8 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data8 == "" || data8 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data9 = doGet(ADDRESS + "/agent/control/risk?lottery=XYNC&games=ZM%2CZDX%2CZDS%2CZWDX"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data9 != "" && data9 != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data9 == "" || data9 == "timeout") {
    		  return false;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  System.out.println("幸运农场抓取用时 :"  + (time - timeStart));
    	  
//    	  for(int i = 1; i < 10; i++) {
//    		  System.out.println("农场数据:"  + dataXYNC[i]);
//    	  }
    	  
    	  lockXYNC.writeLock().lock();
    	  dataXYNC[1] = data1;
    	  dataXYNC[2] = data2;
    	  dataXYNC[3] = data3;
    	  dataXYNC[4] = data4;
    	  dataXYNC[5] = data5;
    	  dataXYNC[6] = data6;
    	  dataXYNC[7] = data7;
    	  dataXYNC[8] = data8;
    	  dataXYNC[9] = data9;
    	  lockXYNC.writeLock().unlock();
    	  
    	  return true;
      }
      
      public  String[] grabGDKLdata() {
    	  String [] data = {"", "", "", "", "", "", "", "", ""};
    	  
    	  long time =  System.currentTimeMillis();
    	  long timeStart = time;
    	  String strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[0] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B1%2CDX1%2CDS1%2CWDX1%2CHDS1%2CFW1%2CZFB1%2CLH1"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[0] != "" && data[0] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[0] == "" || data[0] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[1] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B2%2CDX2%2CDS2%2CWDX2%2CHDS2%2CFW2%2CZFB2%2CLH2"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[1] != "" && data[1] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[1] == "" || data[1] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[2] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B3%2CDX3%2CDS3%2CWDX3%2CHDS3%2CFW3%2CZFB3%2CLH3"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[2] != "" && data[2] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[2] == "" || data[2] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[3] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B4%2CDX4%2CDS4%2CWDX4%2CHDS4%2CFW4%2CZFB4%2CLH4"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[3] != "" && data[3] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[3] == "" || data[3] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[4] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B5%2CDX5%2CDS5%2CWDX5%2CHDS5%2CFW5%2CZFB5%2CLH5"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[4] != "" && data[4] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[4] == "" || data[4] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[5] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B6%2CDX6%2CDS6%2CWDX6%2CHDS6%2CFW6%2CZFB6%2CLH6"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[5] != "" && data[5] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[5] == "" || data[5] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[6] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B7%2CDX7%2CDS7%2CWDX7%2CHDS7%2CFW7%2CZFB7%2CLH7"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[6] != "" && data[6] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[6] == "" || data[6] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[7] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=B8%2CDX8%2CDS8%2CWDX8%2CHDS8%2CFW8%2CZFB8%2CLH8"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[7] != "" && data[7] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[7] == "" || data[7] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  strTime = Long.toString(time);
    	  for(int i = 0; i < 3; i++) {
    		  data[8] = doGet(ADDRESS + "/agent/control/risk?lottery=GDKLSF&games=ZM%2CZDX%2CZDS%2CZWDX"
    	    	  		+ "&all=XZ&range=&multiple=false&_"  + strTime, cookieuid + cookiedae);
    		  if(data[8] != "" && data[8] != "timeout") {
    			  break;
    		  }  
    	  }
    	  
    	  if(data[8] == "" || data[8] == "timeout") {
    		  return null;
    	  }
    	  
    	  time =  System.currentTimeMillis();
    	  System.out.println("广东快乐十分抓取用时 :"  + (time - timeStart));
    	  
//    	  for(int i = 1; i < 10; i++) {
//    		  System.out.println("农场数据:"  + dataGDKL[i]);
//    	  }  
    	  
    	  return data;
      }
      
      public  void setCQSSCdata(String drawNumber, String data, String remainTime) {
    	      lockCQSSC.writeLock().lock();
	    	  dataCQSSC[0] = drawNumber;
	    	  dataCQSSC[1] = data;
	    	  dataCQSSC[2] = remainTime;
	    	  isCQSSCdataOk = true;
	    	  lockCQSSC.writeLock().unlock();
	    	  System.out.println("set  CQSSCdata   ok");
      }
      
      public  void setBJSCdata(String drawNumber, String [] data, String remainTime, String percent, String positive) {
    	  	  lockBJSC.writeLock().lock();
	    	  dataBJSC[0] = drawNumber;
	    	  dataBJSC[1] = data[0];
	    	  dataBJSC[2] = data[1];
	    	  dataBJSC[3] = data[2];
	    	  dataBJSC[4] = remainTime;
	    	  dataBJSC[5] = percent;
	    	  dataBJSC[6] = positive;
	    	  isBJSCdataOk = true;
	    	  lockBJSC.writeLock().unlock();
	    	  System.out.println("set  BJSCdata   ok");
      }
      
      public  void setXYNCdata(String drawNumber, String remainTime) {
    	      lockXYNC.writeLock().lock();
	    	  dataXYNC[0] = drawNumber;
	    	  dataXYNC[10] = remainTime;
	    	  isXYNCdataOk = true;
	    	  lockXYNC.writeLock().unlock();
	    	  System.out.println("set  XYNCdata   ok");
      }
      
      public  void setGXKLdata(String drawNumber, String data, String remainTime) {
	  	  lockGXKL.writeLock().lock();
    	  dataGXKL[0] = drawNumber;
    	  dataGXKL[1] = data;
    	  dataGXKL[2] = remainTime;
    	  isGXKLdataOk = true;
    	  lockGXKL.writeLock().unlock();
    	  System.out.println("set  GXKLdata   ok");
      }
      
      public  void setGD115data(String drawNumber, String data, String remainTime) {
	  	  lockGD115.writeLock().lock();
    	  dataGD115[0] = drawNumber;
    	  dataGD115[1] = data;
    	  dataGD115[2] = remainTime;
    	  isGD115dataOk = true;
    	  lockGD115.writeLock().unlock();
    	  System.out.println("set  GD115data   ok");
      }
      
      public  void setTJSSCdata(String drawNumber, String data, String remainTime) {
    	  lockTJSSC.writeLock().lock();
    	  dataTJSSC[0] = drawNumber;
    	  dataTJSSC[1] = data;
    	  dataTJSSC[2] = remainTime;
    	  isTJSSCdataOk = true;
    	  lockTJSSC.writeLock().unlock();
    	  System.out.println("set  TJSSCdata   ok");
      }
      
      public  void setXJSSCdata(String drawNumber, String data, String remainTime) {
    	  lockXJSSC.writeLock().lock();
    	  dataXJSSC[0] = drawNumber;
    	  dataXJSSC[1] = data;
    	  dataXJSSC[2] = remainTime;
    	  isXJSSCdataOk = true;
    	  lockXJSSC.writeLock().unlock();
    	  System.out.println("set  XJSSCdata   ok");
      }
      
      public  void setBJKL8data(String drawNumber, String data, String remainTime) {
    	  lockBJKL8.writeLock().lock();
    	  dataBJKL8[0] = drawNumber;
    	  dataBJKL8[1] = data;
    	  dataBJKL8[2] = remainTime;
    	  isBJKL8dataOk = true;
    	  lockBJKL8.writeLock().unlock();
    	  System.out.println("set  BJKL8data   ok");
      }
      
      public  void setGDKLdata(String drawNumber, String [] data, String remainTime) {
	  	  lockGDKL.writeLock().lock();
    	  dataGDKL[0] = drawNumber;
    	  dataGDKL[1] = data[0];
    	  dataGDKL[2] = data[1];
    	  dataGDKL[3] = data[2];
    	  dataGDKL[4] = data[3];
    	  dataGDKL[5] = data[4];
    	  dataGDKL[6] = data[5];
    	  dataGDKL[7] = data[6];
    	  dataGDKL[8] = data[7];
    	  dataGDKL[9] = data[8];
    	  dataGDKL[10] = remainTime;
    	  isGDKLdataOk = true;
    	  lockGDKL.writeLock().unlock();
    	  System.out.println("set  GDKLdata   ok");
      }
      
      
      //! @brief    读取cqssc下单数据
      //! @return   数据可用(String[0]:期数， String[1]:data, String[2]:数据时间);  数据不可用(null)
      public  String[] getCQSSCdata() {
    	  String [] data = null;
    	  lockCQSSC.readLock().lock();
    	  if(isCQSSCdataOk) {
    		  data = (String [])dataCQSSC.clone();
    	  }
    	  lockCQSSC.readLock().unlock();

		  return data;
      }
      
      //! @brief     读取取BJSC下单数据
      //! @return    数据可用(String[0]:期数, String[1]:冠亚, String[2]:三四五六, String[3]:七八九十, String[4]:数据时间, String[5] percent, String[6]正反); 数据不可用(null)
      public  String[] getBJSCdata() {
    	  String [] data = null;
    	  lockBJSC.readLock().lock();
    	  if(isBJSCdataOk) {
    		  data = (String [])dataBJSC.clone();
    	  }
    	  lockBJSC.readLock().unlock();
    	  
    	  return data;
      }
      
      public  String[] getGXKLdata() {
    	  String [] data = null;
    	  lockGXKL.readLock().lock();
    	  if(isGXKLdataOk) {
    		  data = (String [])dataGXKL.clone();
    	  }
    	  lockGXKL.readLock().unlock();
    	  
    	  return data;
      }
      
      public  String[] getGDKLdata() {
    	  String [] data = null;
    	  lockGDKL.readLock().lock();
    	  if(isGDKLdataOk) {
    		  data = (String [])dataGDKL.clone();
    	  }
    	  lockGDKL.readLock().unlock();
    	  
    	  return data;
      }
      
      public  String[] getGD115data() {
    	  String [] data = null;
    	  lockGD115.readLock().lock();
    	  if(isGD115dataOk) {
    		  data = (String [])dataGD115.clone();
    	  }
    	  lockGD115.readLock().unlock();
    	  
    	  return data;
      }
      
      public  String[] getTJSSCdata() {
    	  String [] data = null;
    	  lockTJSSC.readLock().lock();
    	  if(isTJSSCdataOk) {
    		  data = (String [])dataTJSSC.clone();
    	  }
    	  lockTJSSC.readLock().unlock();
    	  
    	  return data;
      }
      
      public  String[] getXJSSCdata() {
    	  String [] data = null;
    	  lockXJSSC.readLock().lock();
    	  if(isXJSSCdataOk) {
    		  data = (String [])dataXJSSC.clone();
    	  }
    	  lockXJSSC.readLock().unlock();
    	  
    	  return data;
      }
      
      public  String[] getBJKL8data() {
    	  String [] data = null;
    	  lockBJKL8.readLock().lock();
    	  if(isBJKL8dataOk) {
    		  data = (String [])dataBJKL8.clone();
    	  }
    	  lockBJKL8.readLock().unlock();
    	  
    	  return data;
      }
      
      //! @brief     读取取XYNC下单数据
      //! @return    数据可用(String[0]:期数, String[1~9]:下单数据, String[10]:数据时间); 数据不可用(null)
      public  String[] getXYNCdata() {
    	  String [] data = null;
    	  lockXYNC.readLock().lock();
    	  if(isXYNCdataOk) {
    		  data = (String [])dataXYNC.clone();
    	  }
    	  lockXYNC.readLock().unlock();
    	  return data;
      }
      
      public  String [] getXYNCshowData() {
    	  lockXYNC.readLock().lock();
    	  String [] data = {dataXYNC[1], dataXYNC[2], dataXYNC[3], dataXYNC[4], dataXYNC[5], dataXYNC[6], dataXYNC[7], dataXYNC[8], dataXYNC[9]};
    	  lockXYNC.readLock().unlock();
    	  return data;
      }
      
      public  void disableCQSSCData() {
    	  lockCQSSC.writeLock().lock();
    	  isCQSSCdataOk = false;
    	  lockCQSSC.writeLock().unlock();
      }
      
      public  void disableBJSCData() {
    	  lockBJSC.writeLock().lock();
    	  isBJSCdataOk = false;
    	  lockBJSC.writeLock().unlock();
      }
      
      public  void disableXYNCData() {
    	  lockXYNC.writeLock().lock();
    	  isXYNCdataOk = false;
    	  lockXYNC.writeLock().unlock();
      }
      
      public  void disableGXKLData() {
    	  lockGXKL.writeLock().lock();
    	  isGXKLdataOk = false;
    	  lockGXKL.writeLock().unlock();
      }
      
      public  void disableGDKLData() {
    	  lockGDKL.writeLock().lock();
    	  isGDKLdataOk = false;
    	  lockGDKL.writeLock().unlock();
      }
      
      public  void disableGD115Data() {
    	  lockGD115.writeLock().lock();
    	  isGD115dataOk = false;
    	  lockGD115.writeLock().unlock();
      }
      
      public  void disableTJSSCData() {
    	  lockTJSSC.writeLock().lock();
    	  isTJSSCdataOk = false;
    	  lockTJSSC.writeLock().unlock();
      }
      
      public  void disableXJSSCData() {
    	  lockXJSSC.writeLock().lock();
    	  isXJSSCdataOk = false;
    	  lockXJSSC.writeLock().unlock();
      }
      
      public  void disableBJKL8Data() {
    	  lockBJKL8.writeLock().lock();
    	  isBJKL8dataOk = false;
    	  lockBJKL8.writeLock().unlock();
      }
      
      public  boolean setDsnTime() {
    	  String host = ADDRESS;
    	  String getTimeUrl = host + "/time?&_=";
    	  getTimeUrl += Long.toString(System.currentTimeMillis());
				          
    	  String response = doGet(getTimeUrl, "");				        
   	  
    	  if(response == "timeout") {
        	  response = doGet(getTimeUrl, "");
          }
				          
    	  if(Common.isNum(response)) {
    		  timeDValue = Long.parseLong(response) - System.currentTimeMillis();
    		  return true;
    	  }
    	  
    	  return false;
      }
      
      public  String [] getCQSSCTime(){
          //get period
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=CQSSC&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long closeTime = 0;
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTime = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTime - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  String [] getBJSCTime(){
          //get period
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;

          String getPeriodUrl = host + "/agent/period?lottery=BJPK10&_=";
          getPeriodUrl += Long.toString(System.currentTimeMillis());

          
          response = doGet(getPeriodUrl, "");
          
          if(response == "") {
          	System.out.println("get period failed");
          	time[0] = Long.toString(System.currentTimeMillis());
          	return time;
          }
          
          if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
        	  System.out.println("get period failed");
        	  time[0] = Long.toString(System.currentTimeMillis());
      		  return time;
           }
          
          System.out.println("preiod:");
          System.out.println(response);
          
          long closeTime = 0;
    	  long drawTime = 0;
          try{
              JSONObject periodJson = new JSONObject(response);
              closeTime = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              BJSCdrawNumber = Long.valueOf(time[1]);
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
      		  return time;
          }
          
          closeTimeBJSC = closeTime;
          time[0] = Long.toString(closeTime - (System.currentTimeMillis() + timeDValue));
    	  time[2] = Long.toString(drawTime - (System.currentTimeMillis() + timeDValue));
    	  return time;
      }    
      
      public  String [] getXYNCTime(){  
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;	 
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=XYNC&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long closeTime = 0;
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTime = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTime - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  String [] getGXKLtime() {
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;	 
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=GXKLSF&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTimeGXKL = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeGXKL - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  String [] getGD115time() {
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;	 
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=GD11X5&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTimeGD115 = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeGD115 - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  String [] getGDKLtime() {
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;	 
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=GDKLSF&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTimeGDKL = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeGDKL - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  String [] getTJSSCtime() {
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;	 
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=TJSSC&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTimeTJSSC = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeTJSSC - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  String [] getXJSSCtime() {
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;	 
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=XJSSC&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTimeXJSSC = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeXJSSC - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  String [] getBJKL8time() {
    	  String [] time = {"0", "0", "0"};
    	  String response = "";
    	  String host = ADDRESS;	 
    	  
    	  String getPeriodUrl = host + "/agent/period?lottery=KL8&_=";
    	  getPeriodUrl += Long.toString(System.currentTimeMillis());

          
    	  response = doGet(getPeriodUrl, "");
		          
    	  if(response == "") {	
    		  System.out.println("get period failed");
    		  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
	      }
    	  
    	  if(response == "timeout") {
        	  response = doGet(getPeriodUrl, "");
          }
          
          if(response == "" || response == "timeout") {
            	System.out.println("get period failed");
            	time[0] = Long.toString(System.currentTimeMillis());
      		  	return time;
           }
	          
    	  System.out.println("preiod:");
    	  System.out.println(response);
				          
    	  long drawTime = 0;
    	  try{
              JSONObject periodJson = new JSONObject(response);
              closeTimeBJKL8 = periodJson.getLong("closeTime");
              time[1] = periodJson.getString("drawNumber");
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("获取时间异常");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeBJKL8 - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public  long getGXKLlocalRemainTime() {
    	  return closeTimeGXKL - (timeDValue + System.currentTimeMillis());
      }
      
      public  long getBJSClocalRemainTime() {
    	  return closeTimeBJSC - (timeDValue + System.currentTimeMillis());
      }
      
      public  long getGDKLlocalRemainTime() {
    	  return closeTimeGDKL - (timeDValue + System.currentTimeMillis());
      }
      
      public  long getTJSSClocalRemainTime() {
    	  return closeTimeTJSSC - (timeDValue + System.currentTimeMillis());
      }
      
      public  long getXJSSClocalRemainTime() {
    	  return closeTimeXJSSC - (timeDValue + System.currentTimeMillis());
      }
      
      public  long getGD115localRemainTime() {
    	  return closeTimeGD115 - (timeDValue + System.currentTimeMillis());
      }
      
      public  long getBJKL8localRemainTime() {
    	  return closeTimeBJKL8 - (timeDValue + System.currentTimeMillis());
      }
      
      public  String grabCQSSCdataByCookie(String game, String all, String range, String uid, String dae){
      	if((game == "LM" || game == "DH" || game == "QZHS") && (range == "" || range == "A" ||
      			range == "B" || range == "C" || range == "D") && (all == "XZ" || all == "SZ" || all == "BH")) {
      		switch (game) {
      		    case "LM":
      		      game = "DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CLH";
      		      break;
      		    case "DH":
        		      game = "B1%2CB2%2CB3%2CB4%2CB5";
        		      break;
      		    case "QZHS":
          		  game = "TS1%2CTS2%2CTS3";
          		  break;
      		   default :
      		}
      		long time =  System.currentTimeMillis();
      		String strTime = Long.toString(time);
      		String data = doGet("http://3f071b45.dsn.ww311.com/agent/control/risk?lottery=CQSSC&games=" + game +"&all=" 
      								+ all + "&range=" + range + "&multiple=false&_=" + strTime, uid + dae);
      		if(data != "") {
      			return data;
      		}
      	}
      	return null;
      }
      
      public  boolean isInCQSSCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour*60 + currentMinutes < 10*60 -5) && (currentHour * 60 + currentMinutes > 1 * 60 + 55))
              return false;
           
           return true;
      }
      
      public  boolean isInBJSCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour *60 + currentMinutes > 9*60 + 3) && (currentHour * 60 + currentMinutes <= 23 * 60 + 56)){
          		return true;
          }
           
           return false;
      }
      
      public  boolean isInXYNCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour*60 + currentMinutes < 10*60 -7) && (currentHour * 60 + currentMinutes > 2 * 60 + 3))
              return false;
           
           return true;
      }
      
      public  boolean isInTJSSCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour *60 + currentMinutes > 9*60 + 1) && (currentHour * 60 + currentMinutes < 23 * 60)){
          		return true;
          }
           
          return false;
      }
      
      public  boolean isInXJSSCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour*60 + currentMinutes < 10*60 + 1) && (currentHour * 60 + currentMinutes >= 2 * 60))
              return false;
           
           return true;
      }
      
      public  boolean isInGD115grabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour *60 + currentMinutes > 9*60 + 1) && (currentHour * 60 + currentMinutes < 23 * 60)){
          		return true;
          }
           
          return false;
      }
      
      public  boolean isInGXKLgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour *60 + currentMinutes > 9*60 + 1) && (currentHour * 60 + currentMinutes <= 21 * 60 + 26)){
          		return true;
          }
           
          return false;
      }
      
      public  boolean isInGDKLgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour *60 + currentMinutes > 9*60 + 3) && (currentHour * 60 + currentMinutes <= 23 * 60 + 1)){
          		return true;
          }
           
          return false;
      }
      
      public  boolean isInBJKL8grabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour *60 + currentMinutes > 9*60) && (currentHour * 60 + currentMinutes < 23 * 60 + 55)){
          		return true;
          }
           
          return false;
      }
      
      public  long getEndDrawNumber(String date){
     	 // http://94500151.365.qq168.ws/agent/report/period?lottery=BJPK10&beginTime=2016-12-02&endTime=2016-12-01
     	
     	  long endDrawNumber = 0;
     	  String result = null;
     	  result = doGet(ADDRESS + "/agent/report/period?lottery=BJPK10&beginTime=" + date + "&&endTime=" + date
     	  ,"");
     	  
     	  if(result != null){
     		  try{
     			  JSONArray drawNumbers = new JSONArray(result);

     			  
     			  JSONObject drawNumber = drawNumbers.getJSONObject(0); 
     			  
     			  endDrawNumber = Long.parseLong(drawNumber.getString("drawNumber"));
     			  
     		  }catch(Exception e){
     			  e.printStackTrace();
     		  }
     	  }
     	  
     	  return endDrawNumber;	  
     		  
       }
      
      public  long getStartDrawNumber(String date){
     	 // http://94500151.365.qq168.ws/agent/report/period?lottery=BJPK10&beginTime=2016-12-02&endTime=2016-12-01
     	
     	  long startDrawNumber = 0;
     	  String result = null;
     	  result = doGet(ADDRESS + "/agent/report/period?lottery=BJPK10&beginTime=" + date + "&&endTime=" + date
     	  ,"");
     	  
     	  if(result != null){
     		  try{
     			  JSONArray drawNumbers = new JSONArray(result);
     			  
     			  int len = drawNumbers.length();
     			  
     			  JSONObject drawNumber = drawNumbers.getJSONObject(len - 1); 
     			  
     			  startDrawNumber = Long.parseLong(drawNumber.getString("drawNumber"));
     			  
     		  }catch(Exception e){
     			  e.printStackTrace();
     		  }
     	  }
     	  
     	  return startDrawNumber;	  
     		  
       }
      
      public  String getBJSCresult(long period) {
    	  String result = null;
    	  result = doGet(ADDRESS + "/agent/report/list_game?lottery=BJPK10&begin=&end=&settle=true&period=" 
    	  + period + "&amount=&dividend=", "");
    	  return result;
      }
      
      public  String getCQSSCresult(String period) {
    	  String result = null;
    	  result = doGet(ADDRESS + "/agent/report/list_game?lottery=CQSSC&begin=&end=&settle=true&period=" 
    	  + period + "&amount=&dividend=", "");
    	  return result;
      }
      
      public  boolean isBetBJSCopen() {
  		return betBJSCopen;
  	  }

  	  public  void setBetBJSCopen(boolean betBJSCopen) {
  		this.betBJSCopen = betBJSCopen;
  	  }
  	  
  	 public String getADDRESS() {
 		return ADDRESS;
  	 }
  	 
  	 public String getACCOUNT() {
 		return ACCOUNT;
  	 }
      
 
}
