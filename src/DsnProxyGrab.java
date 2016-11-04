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

public class DsnProxyGrab {
	static CloseableHttpClient httpclient = null;
    static RequestConfig requestConfig = null;
    static HttpClientContext clientContext = null;
    
    //static Map<String,String> cookieMap = new HashMap<String, String>(64);
    static String strCookies = "";
    static String cookieuid = "";
    static String cookiedae = "";
    static String [] dataCQSSC = {"", "", ""};
    static String [] dataGXKL = {"", "", ""};
    static String [] dataTJSSC = {"", "", ""};
    static String [] dataXJSSC = {"", "", ""};
    static String [] dataGD115 = {"", "", ""};
    static String [] dataBJSC = {"", "", "", "", ""};
    static String [] dataXYNC = {"", "", "", "", "", "", "", "", "", "", ""};
    static String [] dataGDKL = {"", "", "", "", "", "", "", "", "", "", ""};
    static boolean isCQSSCdataOk = false;
    static boolean isBJSCdataOk = false;
    static boolean isXYNCdataOk = false;
    static boolean isGXKLdataOk = false;
    static boolean isGDKLdataOk = false;
    static boolean isTJSSCdataOk = false;
    static boolean isXJSSCdataOk = false;
    static boolean isGD115dataOk = false;
    static boolean isInReLogin = false;
    
    
    private static String ADDRESS = "";
    private static String ACCOUNT = "";
    private static String PASSWORD = "";
    private static long timeDValue = 0;  //��վʱ��͵���ʱ��Ĳ�ֵ  ��ҳʱ�� - ��ǰʱ��
    private static long closeTimeGXKL = 0;
    private static long closeTimeGDKL = 0;
    private static long closeTimeTJSSC = 0;
    private static long closeTimeXJSSC = 0;
    private static long closeTimeGD115 = 0;
    private static ReadWriteLock lockCQSSC = new ReentrantReadWriteLock();
    private static ReadWriteLock lockBJSC = new ReentrantReadWriteLock();
    private static ReadWriteLock lockXYNC = new ReentrantReadWriteLock();
    private static ReadWriteLock lockGXKL = new ReentrantReadWriteLock();
    private static ReadWriteLock lockGDKL = new ReentrantReadWriteLock();
    private static ReadWriteLock lockTJSSC = new ReentrantReadWriteLock();
    private static ReadWriteLock lockXJSSC = new ReentrantReadWriteLock();
    private static ReadWriteLock lockGD115 = new ReentrantReadWriteLock();
    
    static {
         requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
         requestConfig = RequestConfig.copy(requestConfig).setRedirectsEnabled(false).build();//��ֹ�ض��� �� �Ա��ȡcookiedae
         requestConfig = RequestConfig.copy(requestConfig).setConnectTimeout(15*1000).setConnectionRequestTimeout(15*1000).setSocketTimeout(15*1000).build();//���ó�ʱ
         httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
    }
    
    
    //�Ż���·ѡ��
    static Vector<Object[]> lines;
    
    static Vector<Long> lastTenRequestTime = new Vector<Long>();
    static long avgRequestTime = 0;    
    static boolean bcalcRequestTime = true;
    static boolean bneedChangeLine = false;
    
    
    
    

	public static String setCookie(CloseableHttpResponse httpResponse) {
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
    
	private static void loginInit() {
		strCookies = "";
        cookieuid = "";
        cookiedae = "";
        clientContext = HttpClientContext.create();
        clientContext.setRequestConfig(requestConfig);
	}

    public static boolean doLogin() { 	
        loginInit();
        String loginPage = doGet(ADDRESS + "/login", ""); //get ��¼ҳ��
        
        if(loginPage != "" && loginPage != "timeout") {
        	cookieuid = strCookies;
        	int posStart = loginPage.indexOf("img src=") + 9;
        	if(posStart >= 9) {
        		int posEnd = loginPage.indexOf('"', posStart);
        		String rmNum = getPicNum(ADDRESS + "/" + loginPage.substring(posStart, posEnd));//get ��֤��
        		System.out.println("yzm: " + rmNum); 
        		if(!Common.isNum(rmNum)) {
        			return false;
        		}
        	
        		//����post
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
        				location = doGet(location, cookieuid);//get cookiedae���ض���url
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
    
    public static void setLoginParams(String address, String account, String password){
    	ADDRESS = address;
    	ACCOUNT = account;
    	PASSWORD = password;
    }
    
    public static void setLoginAddress(String address){
    	ADDRESS = address;
    }
    
    
    public static void initLines(){
    	String[] addressArray = ConfigReader.getProxyAddressArray();
    	

    	
    	lines = new Vector<Object[]>(addressArray.length);
    	
    	for(int k = 0; k < addressArray.length; k++){
    		lines.add(new Object[2]);
    		lines.elementAt(k)[0] = new String(addressArray[k]);
    		
    		lines.elementAt(k)[1] = new Long(10*1000);
    	}
    }
    
    
    
    @SuppressWarnings("unchecked")
	public static void setLinePriority(){
    	
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
    	

    	System.out.println("��������·��������:");
    	
    	for(int j = 0; j < lines.size(); j++){
    		
    		
    		
    		System.out.println(lines.elementAt(j)[0]);
    		
    		System.out.println(lines.elementAt(j)[1]);
    		
    	}
    }
    
    
    public static void calcRequestAveTime(long requestTime){
        
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
            	//System.out.printf("[����]ƽ������ʱ��:%d\n", avgRequestTime);
            	
            	if(avgRequestTime >= 400){
            		
            		setisNeedChangeLine(true);
            	}

        	}

    	}

    		
    	
    }
    
    public static void setIscalcRequestTime(boolean flag){
    	bcalcRequestTime = flag;
    }
    
    public static void setisNeedChangeLine(boolean flag){
    	bneedChangeLine = flag;
    }
    
    public static boolean getIsisNeedChangeLine(){
    	return bneedChangeLine;
    }
    
    public static void clearAvgRequest(){
    	
    	if(lastTenRequestTime.size() >0){
    		lastTenRequestTime.clear();
    	}
    	avgRequestTime = 0;

    }
    
    
    public static boolean login() {  
    	
    	
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
            			
            			ConfigWriter.updateProxyAddress(ADDRESS);//���µ����ڵǵ��ϵ���ַ
            			
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
    
    public static int reLogin() {  
    	
    	if(isInReLogin) {
    		return -1;
    	}
    	
    	isInReLogin = true;
    	int res = 1;

    	setIscalcRequestTime(false);

    	
    	
    	
    	
    	for(int k = 0; k < lines.size(); k++){

    		
    		setLoginAddress((String)lines.elementAt(k)[0]);
    		
    		for(int i = 0; i < 10; i++) {
        		if(doLogin()) {
        			res = 0;

        			
        			break;
        		}
        	}
        	
        	if(res == 0){
        		break;
        	}
    	}

    	
    	setIscalcRequestTime(true);
    	
    	isInReLogin = false;
        	
    	return res;
    	

    }
    
    
    
    
    
    public static void connFailLogin() {
    	
    	
    	boolean res = false;
    	
    	setIscalcRequestTime(false);

    	while(res == false){
    		
        	
        	
        	
        	
        	for(int k = 0; k < lines.size(); k++){
	
        		setLoginAddress((String)lines.elementAt(k)[0]);
        		
        		for(int i = 0; i < 10; i++) {
            		if(doLogin()) {
            			res = true;
		
            			break;
            		}
            	}
            	
            	if(res == true){
            		break;
            	}
        	}
        	
        	if(res == false){
            	try{
            		Thread.currentThread().sleep(20*1000);
            	}catch(Exception e){
            		
            	}
        	}
        	
        	
    	}
    	
    	setIscalcRequestTime(true);
    	

    }


    /**��utf-8��ʽ��ȡ*/
    public static String doPost(String url, List<NameValuePair> formparams, String cookies, String referUrl) {
        return doPost(url, formparams, cookies, "UTF-8", referUrl);
    }

    public static String doPost(String url,List<NameValuePair> formparams, String cookies, String charset, String referUrl) {  
        
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
                // ��ӡ��Ӧ״̬    
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
    
    
    public static CloseableHttpResponse  execute(HttpUriRequest request) throws IOException, ClientProtocolException{
    	
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
    
    
    public static String doGet(String url, String cookies) { 
        try {  
           // ����httpget.    
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
          
           // ִ��get����.    
           CloseableHttpResponse response = execute(httpget); 
           //CloseableHttpResponse response = httpclient.execute(httpget); 
           
           try {          	           	   
               // ��ȡ��Ӧʵ��    
        	   setCookie(response);
               HttpEntity entity = response.getEntity(); 
               //System.out.println("--------------------------------------"); 
               
               String statusLine = response.getStatusLine().toString();
               if(statusLine.indexOf("200 OK") == -1) {
            	   System.out.println(statusLine); 
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
    
    public static String getPicNum(String picUri) {
    	try {
    	 HttpGet httpget = new HttpGet(picUri);
         httpget.addHeader("Connection","keep-alive");
         if(strCookies != ""){
        	 httpget.addHeader("Cookie", strCookies);
         }
         httpget.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 "
         					+ "(KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36");           
         System.out.println("executing request " + httpget.getURI()); 
        
         // ִ��get����.    
        	 CloseableHttpResponse response = execute(httpget); 
        	 try {
        		 setCookie(response);
                 // ��ӡ��Ӧ״̬    
                 System.out.println(response.getStatusLine()); 
                 //System.out.println("------------------------------------");
                 if(response.getStatusLine().toString().indexOf("200 OK") < 0) {
                	 return "";
                 }
                 File storeFile = new File("yzm.png");   //ͼƬ���浽��ǰλ��
                 FileOutputStream output = new FileOutputStream(storeFile);  
                 //�õ�������Դ���ֽ�����,��д���ļ�  
                 byte [] a = EntityUtils.toByteArray(response.getEntity());
                 output.write(a);  
                 output.close();  
                 
          
                 InputStream ins = null;
         		 String[] cmd = new String[]{ConfigReader.getTessPath() + "\\tesseract", "yzm.png", "result", "-l", "eng"};

         		 Process process = Runtime.getRuntime().exec(cmd);
         		 // cmd ����Ϣ
         		 ins = process.getInputStream();
         		 BufferedReader reader = new BufferedReader(new InputStreamReader(ins));

         		 String line = null;
         	  	 while ((line = reader.readLine()) != null) {
         	  		 System.out.println(line);
         		 }
         			
         		 int exitValue = process.waitFor();
         		 System.out.println("����ֵ��" + exitValue);
         		 process.getOutputStream().close();
         		 File file = new File("result.txt");
         		 reader.close();
                 reader = new BufferedReader(new FileReader(file));
                  // һ�ζ���һ�У�ֱ������nullΪ�ļ�����
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
    
  //! @brief ץȡcqssc�µ�����
  //! @param game       ����:"LM", ����:"DH", ǰ�к���:"QZHS"
  //! @param all        ��ע:"XZ" ʵռ:"SZ" ����:"BH"
  //! @param range      ȫ��:"", A��:"A", B��:"B", C��:"C", D��:"D",
  //! @return           �ɹ�:String ����ʧ��:null ��ʱ:"timeout" 
    public static String grabCQSSCdata(String game, String all, String range){
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
    
    
    //! @brief ץȡcqssc�µ�����
    //! @param game       ����:"GY", ��������:"SSWL", �߰˾�ʮ:"QBJS"
    //! @param all        ��ע:"XZ" ʵռ:"SZ" ����:"BH"
    //! @param range      ȫ��:"", A��:"A", B��:"B", C��:"C", D��:"D",
    //! @return           �ɹ�:String ����ʧ��:null ��ʱ:"timeout" 
      public static String grabBJSCdata(String game, String all, String range){
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
      
      public static String grabGXKLdata() {
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
      
      public static String grabTJSSCdata() {
    	  long time =  System.currentTimeMillis();
  		String strTime = Long.toString(time);
  		String data = doGet(ADDRESS + "/agent/control/risk?lottery=TJSSC&games=DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%"
  				+ "2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CLH&all=XZ&range=&multiple=false&_=" + strTime, cookieuid + cookiedae);
  		if(data != "") {
  			return data;
  		}
  		return null;
      }
      
      public static String grabXJSSCdata() {
    	  long time =  System.currentTimeMillis();
    	  String strTime = Long.toString(time);
    	  String data = doGet(ADDRESS + "/agent/control/risk?lottery=XJSSC&games=DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%"
  				+ "2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CLH&all=XZ&range=&multiple=false&_" + strTime, cookieuid + cookiedae);
    	  if(data != "") {
    		  return data;
    	  }
    	  return null;
      }
      
      public static String grabGD115data() {
    	  long time =  System.currentTimeMillis();
    	  String strTime = Long.toString(time);
    	  String data = doGet(ADDRESS + "/agent/control/risk?lottery=GD11X5&games=DX1%2CDS1%2CDX2%2CDS2%2CDX3%2CDS3%2CDX4%"
    	  		+ "2CDS4%2CDX5%2CDS5%2CZDX%2CZDS%2CZWDX%2CLH&all=XZ&range=&multiple=false&_=" + strTime, cookieuid + cookiedae);
    	  if(data != "") {
    		  return data;
    	  }
    	  return null;
      }
      
      public static boolean grabXYNCdata() {
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
    	  System.out.println("����ũ��ץȡ��ʱ :"  + (time - timeStart));
    	  
//    	  for(int i = 1; i < 10; i++) {
//    		  System.out.println("ũ������:"  + dataXYNC[i]);
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
      
      public static String[] grabGDKLdata() {
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
    	  System.out.println("�㶫����ʮ��ץȡ��ʱ :"  + (time - timeStart));
    	  
//    	  for(int i = 1; i < 10; i++) {
//    		  System.out.println("ũ������:"  + dataGDKL[i]);
//    	  }  
    	  
    	  return data;
      }
      
      public static void setCQSSCdata(String drawNumber, String data, String remainTime) {
    	      lockCQSSC.writeLock().lock();
	    	  dataCQSSC[0] = drawNumber;
	    	  dataCQSSC[1] = data;
	    	  dataCQSSC[2] = remainTime;
	    	  isCQSSCdataOk = true;
	    	  lockCQSSC.writeLock().unlock();
	    	  System.out.println("set  CQSSCdata   ok");
      }
      
      public static void setBJSCdata(String drawNumber, String [] data, String remainTime) {
    	  	  lockBJSC.writeLock().lock();
	    	  dataBJSC[0] = drawNumber;
	    	  dataBJSC[1] = data[0];
	    	  dataBJSC[2] = data[1];
	    	  dataBJSC[3] = data[2];
	    	  dataBJSC[4] = remainTime;
	    	  isBJSCdataOk = true;
	    	  lockBJSC.writeLock().unlock();
	    	  System.out.println("set  BJSCdata   ok");
      }
      
      public static void setXYNCdata(String drawNumber, String remainTime) {
    	      lockXYNC.writeLock().lock();
	    	  dataXYNC[0] = drawNumber;
	    	  dataXYNC[10] = remainTime;
	    	  isXYNCdataOk = true;
	    	  lockXYNC.writeLock().unlock();
	    	  System.out.println("set  XYNCdata   ok");
      }
      
      public static void setGXKLdata(String drawNumber, String data, String remainTime) {
	  	  lockGXKL.writeLock().lock();
    	  dataGXKL[0] = drawNumber;
    	  dataGXKL[1] = data;
    	  dataGXKL[2] = remainTime;
    	  isGXKLdataOk = true;
    	  lockGXKL.writeLock().unlock();
    	  System.out.println("set  GXKLdata   ok");
      }
      
      public static void setGD115data(String drawNumber, String data, String remainTime) {
	  	  lockGD115.writeLock().lock();
    	  dataGD115[0] = drawNumber;
    	  dataGD115[1] = data;
    	  dataGD115[2] = remainTime;
    	  isGD115dataOk = true;
    	  lockGD115.writeLock().unlock();
    	  System.out.println("set  GD115data   ok");
      }
      
      public static void setTJSSCdata(String drawNumber, String data, String remainTime) {
    	  lockTJSSC.writeLock().lock();
    	  dataTJSSC[0] = drawNumber;
    	  dataTJSSC[1] = data;
    	  dataTJSSC[2] = remainTime;
    	  isTJSSCdataOk = true;
    	  lockTJSSC.writeLock().unlock();
    	  System.out.println("set  TJSSCdata   ok");
      }
      
      public static void setXJSSCdata(String drawNumber, String data, String remainTime) {
    	  lockXJSSC.writeLock().lock();
    	  dataXJSSC[0] = drawNumber;
    	  dataXJSSC[1] = data;
    	  dataXJSSC[2] = remainTime;
    	  isXJSSCdataOk = true;
    	  lockXJSSC.writeLock().unlock();
    	  System.out.println("set  XJSSCdata   ok");
      }
      
      public static void setGDKLdata(String drawNumber, String [] data, String remainTime) {
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
      
      
      //! @brief    ��ȡcqssc�µ�����
      //! @return   ���ݿ���(String[0]:������ String[1]:data, String[2]:����ʱ��);  ���ݲ�����(null)
      public static String[] getCQSSCdata() {
    	  String [] data = null;
    	  lockCQSSC.readLock().lock();
    	  if(isCQSSCdataOk) {
    		  data = (String [])dataCQSSC.clone();
    	  }
    	  lockCQSSC.readLock().unlock();

		  return data;
      }
      
      //! @brief     ��ȡȡBJSC�µ�����
      //! @return    ���ݿ���(String[0]:����, String[1]:����, String[2]:��������, String[3]:�߰˾�ʮ, String[4]:����ʱ��); ���ݲ�����(null)
      public static String[] getBJSCdata() {
    	  String [] data = null;
    	  lockBJSC.readLock().lock();
    	  if(isBJSCdataOk) {
    		  data = (String [])dataBJSC.clone();
    	  }
    	  lockBJSC.readLock().unlock();
    	  
    	  return data;
      }
      
      public static String[] getGXKLdata() {
    	  String [] data = null;
    	  lockGXKL.readLock().lock();
    	  if(isGXKLdataOk) {
    		  data = (String [])dataGXKL.clone();
    	  }
    	  lockGXKL.readLock().unlock();
    	  
    	  return data;
      }
      
      public static String[] getGDKLdata() {
    	  String [] data = null;
    	  lockGDKL.readLock().lock();
    	  if(isGDKLdataOk) {
    		  data = (String [])dataGDKL.clone();
    	  }
    	  lockGDKL.readLock().unlock();
    	  
    	  return data;
      }
      
      public static String[] getGD115data() {
    	  String [] data = null;
    	  lockGD115.readLock().lock();
    	  if(isGD115dataOk) {
    		  data = (String [])dataGD115.clone();
    	  }
    	  lockGD115.readLock().unlock();
    	  
    	  return data;
      }
      
      public static String[] getTJSSCdata() {
    	  String [] data = null;
    	  lockTJSSC.readLock().lock();
    	  if(isTJSSCdataOk) {
    		  data = (String [])dataTJSSC.clone();
    	  }
    	  lockTJSSC.readLock().unlock();
    	  
    	  return data;
      }
      
      public static String[] getXJSSCdata() {
    	  String [] data = null;
    	  lockXJSSC.readLock().lock();
    	  if(isXJSSCdataOk) {
    		  data = (String [])dataXJSSC.clone();
    	  }
    	  lockXJSSC.readLock().unlock();
    	  
    	  return data;
      }
      
      //! @brief     ��ȡȡXYNC�µ�����
      //! @return    ���ݿ���(String[0]:����, String[1~9]:�µ�����, String[10]:����ʱ��); ���ݲ�����(null)
      public static String[] getXYNCdata() {
    	  String [] data = null;
    	  lockXYNC.readLock().lock();
    	  if(isXYNCdataOk) {
    		  data = (String [])dataXYNC.clone();
    	  }
    	  lockXYNC.readLock().unlock();
    	  return data;
      }
      
      public static String [] getXYNCshowData() {
    	  lockXYNC.readLock().lock();
    	  String [] data = {dataXYNC[1], dataXYNC[2], dataXYNC[3], dataXYNC[4], dataXYNC[5], dataXYNC[6], dataXYNC[7], dataXYNC[8], dataXYNC[9]};
    	  lockXYNC.readLock().unlock();
    	  return data;
      }
      
      public static void disableCQSSCData() {
    	  lockCQSSC.writeLock().lock();
    	  isCQSSCdataOk = false;
    	  lockCQSSC.writeLock().unlock();
      }
      
      public static void disableBJSCData() {
    	  lockBJSC.writeLock().lock();
    	  isBJSCdataOk = false;
    	  lockBJSC.writeLock().unlock();
      }
      
      public static void disableXYNCData() {
    	  lockXYNC.writeLock().lock();
    	  isXYNCdataOk = false;
    	  lockXYNC.writeLock().unlock();
      }
      
      public static void disableGXKLData() {
    	  lockGXKL.writeLock().lock();
    	  isGXKLdataOk = false;
    	  lockGXKL.writeLock().unlock();
      }
      
      public static void disableGDKLData() {
    	  lockGDKL.writeLock().lock();
    	  isGDKLdataOk = false;
    	  lockGDKL.writeLock().unlock();
      }
      
      public static void disableGD115Data() {
    	  lockGD115.writeLock().lock();
    	  isGD115dataOk = false;
    	  lockGD115.writeLock().unlock();
      }
      
      public static void disableTJSSCData() {
    	  lockTJSSC.writeLock().lock();
    	  isTJSSCdataOk = false;
    	  lockTJSSC.writeLock().unlock();
      }
      
      public static void disableXJSSCData() {
    	  lockXJSSC.writeLock().lock();
    	  isXJSSCdataOk = false;
    	  lockXJSSC.writeLock().unlock();
      }
      
      public static boolean setDsnTime() {
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
      
      public static String [] getCQSSCTime(){
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
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTime - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public static String [] getBJSCTime(){
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
              drawTime = periodJson.getLong("drawTime");
          }
          catch(Exception e){
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
      		  return time;
          }
          
          time[0] = Long.toString(closeTime - (System.currentTimeMillis() + timeDValue));
    	  time[2] = Long.toString(drawTime - (System.currentTimeMillis() + timeDValue));
    	  return time;
      }    
      
      public static String [] getXYNCTime(){  
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
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTime - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public static String [] getGXKLtime() {
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
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeGXKL - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public static String [] getGD115time() {
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
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeGD115 - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public static String [] getGDKLtime() {
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
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeGDKL - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public static String [] getTJSSCtime() {
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
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeTJSSC - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public static String [] getXJSSCtime() {
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
        	  System.out.println("��ȡʱ���쳣");
        	  time[0] = Long.toString(System.currentTimeMillis());
    		  return time;
          }
				          
    	  time[0] = Long.toString(closeTimeXJSSC - (timeDValue + System.currentTimeMillis()));
    	  time[2] = Long.toString(drawTime - (timeDValue + System.currentTimeMillis()));
    	  return time;
      }
      
      public static long getGXKLlocalRemainTime() {
    	  return closeTimeGXKL - (timeDValue + System.currentTimeMillis());
      }
      
      public static long getGDKLlocalRemainTime() {
    	  return closeTimeGDKL - (timeDValue + System.currentTimeMillis());
      }
      
      public static long getTJSSClocalRemainTime() {
    	  return closeTimeTJSSC - (timeDValue + System.currentTimeMillis());
      }
      
      public static long getXJSSClocalRemainTime() {
    	  return closeTimeXJSSC - (timeDValue + System.currentTimeMillis());
      }
      
      public static long getGD115localRemainTime() {
    	  return closeTimeGD115 - (timeDValue + System.currentTimeMillis());
      }
      
      public static String grabCQSSCdataByCookie(String game, String all, String range, String uid, String dae){
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
      
      public static boolean isInCQSSCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour*60 + currentMinutes < 10*60 -5) && (currentHour * 60 + currentMinutes > 1 * 60 + 55))
              return false;
           
           return true;
      }
      
      public static boolean isInBJSCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour *60 + currentMinutes > 9*60 + 1) && (currentHour * 60 + currentMinutes <= 23 * 60 + 57)){
          		return true;
          }
           
           return false;
      }
      
      public static boolean isInXYNCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour*60 + currentMinutes < 10*60 -7) && (currentHour * 60 + currentMinutes > 2 * 60 + 3))
              return false;
           
           return true;
      }
      
      public static boolean isInTJSSCgrabTime() {
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
      
      public static boolean isInXJSSCgrabTime() {
    	  long time = System.currentTimeMillis() + timeDValue;
    	  Date date = new Date(time);
          int currentHour = date.getHours();
          int currentMinutes = date.getMinutes();
          int currentSeconds = date.getSeconds();
          
          if((currentHour*60 + currentMinutes < 10*60 + 1) && (currentHour * 60 + currentMinutes >= 2 * 60))
              return false;
           
           return true;
      }
      
      public static boolean isInGD115grabTime() {
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
      
      public static boolean isInGXKLgrabTime() {
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
      
      public static boolean isInGDKLgrabTime() {
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
      
      
    
}
