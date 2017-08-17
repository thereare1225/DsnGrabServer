package dsn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Common {

	public static boolean isNum(String str) {
		try {
			new BigDecimal(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static String[] combineBJSCdata(String data1[], String[] data2) throws JSONException{
	
		for(int i = 0; i < 3; i++) {	
        	JSONArray cqsscLMGrabData1 = new JSONArray(data1[i]);        	
        	JSONArray gamesGrabData1 = cqsscLMGrabData1.getJSONArray(0);        	
        	JSONObject oddsGrabData1 = cqsscLMGrabData1.getJSONObject(1);
        	
        	JSONArray cqsscLMGrabData2 = new JSONArray(data2[i]);        	
        	JSONArray gamesGrabData2 = cqsscLMGrabData2.getJSONArray(0);        	
        	JSONObject oddsGrabData2 = cqsscLMGrabData2.getJSONObject(1);
  	
        	Map<String, Integer> betPairs = new HashMap<String, Integer>();
        	for(int j = 0; j < gamesGrabData1.length(); j++) {
        		JSONObject gameGrabData = gamesGrabData1.getJSONObject(j);
        		
    			String game = gameGrabData.getString("k");
    			String contents = gameGrabData.getString("i");
    			int amount = gameGrabData.getInt("a");
    			String oddsKey = game + "_" + contents;
    			double odds = oddsGrabData1.getDouble(oddsKey);
    			
    			betPairs.put(oddsKey, amount);
        	}
        	   	
        	for(int j = 0; j < gamesGrabData2.length(); j++) {
        		JSONObject gameGrabData = gamesGrabData2.getJSONObject(j);
        		
    			String game = gameGrabData.getString("k");
    			String contents = gameGrabData.getString("i");
    			int amount = gameGrabData.getInt("a");
    			String oddsKey = game + "_" + contents;
    			double odds = oddsGrabData2.getDouble(oddsKey);
    			
    			if(betPairs.containsKey(oddsKey)) {
    				int a = betPairs.get(oddsKey);
    				a += amount;
    				betPairs.put(oddsKey, a);
    			} else {
					betPairs.put(oddsKey, amount);
				}
        	}
        	
        	
        	JSONArray resArray = new JSONArray();
        	for (Map.Entry<String, Integer> entry : betPairs.entrySet()) { 
        		String [] k_i = entry.getKey().split("_");
        		int a = entry.getValue();
        		JSONObject resJsonObject = new JSONObject();
        		resJsonObject.put("k", k_i[0]);
        		resJsonObject.put("i", k_i[1]);
        		resJsonObject.put("a", a);
        		resArray.put(resJsonObject);
        	}
        	
        	JSONArray array = new JSONArray();
        	array.put(resArray);
        	array.put(oddsGrabData1);
        	data1[i] = array.toString();
		}

		return data1;
	}
	
	public static String constructoutputData(String[] data, double percent,  boolean opposite)
    {
    	
    	//data = "[[{\"k\":\"DX2\",\"i\":\"X\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0},{\"k\":\"DX3\",\"i\":\"D\",\"c\":3,\"a\":130,\"r\":258.294,\"cm\":0},{\"k\":\"DX4\",\"i\":\"D\",\"c\":4,\"a\":660,\"r\":1319.868,\"cm\":0},{\"k\":\"DS1\",\"i\":\"D\",\"c\":1,\"a\":10,\"r\":19.998,\"cm\":0},{\"k\":\"DX2\",\"i\":\"D\",\"c\":3,\"a\":20,\"r\":39.996,\"cm\":0},{\"k\":\"DS4\",\"i\":\"D\",\"c\":3,\"a\":20,\"r\":39.996,\"cm\":0},{\"k\":\"DX3\",\"i\":\"X\",\"c\":1,\"a\":5,\"r\":9.999,\"cm\":0},{\"k\":\"DX5\",\"i\":\"D\",\"c\":2,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"DX1\",\"i\":\"X\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0},{\"k\":\"DX4\",\"i\":\"X\",\"c\":1,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"ZDX\",\"i\":\"D\",\"c\":2,\"a\":15,\"r\":29.997,\"cm\":0},{\"k\":\"DS1\",\"i\":\"S\",\"c\":2,\"a\":15,\"r\":29.997,\"cm\":0},{\"k\":\"DS2\",\"i\":\"D\",\"c\":3,\"a\":100,\"r\":199.98,\"cm\":0},{\"k\":\"DS3\",\"i\":\"D\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0},{\"k\":\"DS3\",\"i\":\"S\",\"c\":3,\"a\":20,\"r\":39.996,\"cm\":0},{\"k\":\"DS4\",\"i\":\"S\",\"c\":2,\"a\":45,\"r\":89.991,\"cm\":0},{\"k\":\"DS5\",\"i\":\"D\",\"c\":3,\"a\":50,\"r\":99.99,\"cm\":0},{\"k\":\"DX1\",\"i\":\"D\",\"c\":3,\"a\":50,\"r\":99.99,\"cm\":0},{\"k\":\"DX5\",\"i\":\"X\",\"c\":3,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"ZDS\",\"i\":\"S\",\"c\":2,\"a\":15,\"r\":29.997,\"cm\":0},{\"k\":\"DS2\",\"i\":\"S\",\"c\":2,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"DS5\",\"i\":\"S\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0}],{\"DS1_S\":1.983,\"DS1_D\":1.983,\"DS2_S\":1.983,\"DS2_D\":1.983,\"DS3_S\":1.983,\"DS3_D\":1.983,\"DS4_S\":1.983,\"DS4_D\":1.983,\"DS5_S\":1.983,\"DS5_D\":1.983,\"DX1_X\":1.983,\"DX1_D\":1.983,\"DX2_X\":1.983,\"DX2_D\":1.983,\"DX3_X\":1.983,\"DX3_D\":1.983,\"DX4_X\":1.983,\"DX4_D\":1.983,\"DX5_X\":1.983,\"DX5_D\":1.983,\"LH_T\":9.28,\"LH_H\":1.983,\"LH_L\":1.983,\"ZDS_S\":1.983,\"ZDS_D\":1.983,\"ZDX_X\":1.983,\"ZDX_D\":1.983},{\"B1\":64,\"B4\":142,\"LM\":1535,\"B3\":64,\"B5\":334,\"B2\":64}]";
    	int totalAmount = 0;
    	
    	String res = "";

    	
    	try{
    		
    		List<String> parsedGames = new ArrayList<String>();;
    		
	    	JSONArray gamesArray = new JSONArray();
	    	JSONObject oddsGrabData = null;
	    	

	    	
	    	
	    	for(int i = 0; i < data.length; i++){
    		
    		
            	JSONArray cqsscLMGrabData = new JSONArray(data[i]);        	
            	JSONArray gamesGrabData = cqsscLMGrabData.getJSONArray(0);
            	
            	oddsGrabData = cqsscLMGrabData.getJSONObject(1);
        	
	        	for(int j = 0; j < gamesGrabData.length(); j++){
	        		JSONObject gameGrabData = gamesGrabData.getJSONObject(j);
	        		
	    			String game = gameGrabData.getString("k");
	    			String contents = gameGrabData.getString("i");
	    			int amount = gameGrabData.getInt("a");
	    			String oddsKey = game + "_" + contents;
	    			
	    			
	    			
	    			double odds = oddsGrabData.getDouble(oddsKey);
	    			
	    			//剔除北京赛车冠亚军 和 两面
	    			if(game.indexOf("GDX") != -1 || game.indexOf("GDS") != -1)
	    				continue;
	    			
	    			if(parsedGames.contains(game) == true)
	    				continue;
	    			
	    			//计算差值
	        		for(int k = j +1 ; k < gamesGrabData.length(); k++){
	        			JSONObject oppositeGameGrabData = gamesGrabData.getJSONObject(k);
	        			String oppositeGame = oppositeGameGrabData.getString("k");
	        			if(oppositeGame.equals(game)){
	        				int oppositeAmount = oppositeGameGrabData.getInt("a");
	        				if(oppositeAmount > amount){
	        					amount = oppositeAmount - amount;
	        					contents = oppositeGameGrabData.getString("i");
	        					oddsKey = oppositeGame + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	        				}
	        				else{
	        					amount = amount - oppositeAmount;
	        				}
	        				break;
	        			}
	        		}
	        		
	        		parsedGames.add(game);    			
	    			
	    			//只下赔率二以下的
	        		if(odds < 2.0 && amount >0){
	        			amount = (int)(amount*percent);  
	        			//amount = 10;
	        			if(amount < 5)
	        				continue;
	        			totalAmount += amount;
	        			
	        			JSONObject gameObj = new JSONObject();
	        			gameObj.put("game", game);
	        			
	        			//处理反投: 大变小，小变大，单变双，双变大，龙变虎，虎变隆
	        			if(opposite){
	        				if(game.indexOf("DX") != -1){//反大小
	        					if(contents.indexOf("D") != -1){
	        						contents = "X";        						
	        					}
	        					else{
	        						contents = "D";
	        					}
	        					oddsKey = game + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	        				}
	        				
	        				
	        				if(game.indexOf("DS") != -1){//反单双
	        					if(contents.indexOf("D") != -1){
	        						contents = "S";        						
	        					}
	        					else{
	        						contents = "D";
	        					}
	        					oddsKey = game + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	        				}
	        				
	        				if(game.indexOf("LH") != -1){//反龙虎
	        					if(contents.indexOf("L") != -1){
	        						contents = "H";        						
	        					}
	        					else{
	        						contents = "L";
	        					}
	        					oddsKey = game + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	
	        				}
	
	        				
	        			}
	        			//反投处理结束
	        				        			
	        				        			
	        			gameObj.put("contents", contents);
	        			gameObj.put("amount", amount);
	        			gameObj.put("odds", odds);      			
	        			gamesArray.put(gameObj);
	        		}
	        		
	        	}
	    	}
	    	

	    	
	    	if(gamesArray.length() == 0) {
	    		return "";
	    	}
	    	
	    	JSONObject betsObj = new JSONObject();
	    	
	    	boolean ignore = false;
	    	betsObj.put("ignore", ignore);
	    	betsObj.put("bets", gamesArray);
	    	
	    	
	        	
	        betsObj.put("lottery", "BJPK10");
	
	    	
	    	
	    	res = betsObj.toString();
    	
    	}catch(Exception e){
    		e.printStackTrace();
    		return "";
    	}
   	
    	return res;	
    }    
	
public static void outputBetsDetails(String jsonData){
    	
    	System.out.print("下注详情：\n");
    	try{
            	JSONObject betsData = new JSONObject(jsonData);
            	JSONArray gamesData = betsData.getJSONArray("bets");
            	int totalAmout = 0;
            	
        	
            	for(int i = 1; i <= 10 ; i++){
            		String gameDX = "DX" + Integer.toString(i);
            		String gameDS = "DS" + Integer.toString(i);
            		String gameLH = "LH" + Integer.toString(i);
            		JSONObject gameData;
            		int amountDX = 0;
            		String contentsDX = "";
            		int amountDS = 0;
            		String contentsDS = "";
            		int amountLH = 0;
            		String contentsLH = "";
            		
            		
            		for(int j = 0; j < gamesData.length(); j++){
            			
            			gameData = gamesData.getJSONObject(j);
            			
            			String game = gameData.getString("game");
            			if(game.equals(gameDX)){
            				amountDX = gameData.getInt("amount");
            				contentsDX = gameData.getString("contents");
            				contentsDX = contentsDX.equals("D")?"大":"小";
            			}
            			if(game.equals(gameDS)){
            				amountDS = gameData.getInt("amount");  
            				contentsDS = gameData.getString("contents");
            				contentsDS = contentsDS.equals("D")?"单":"双";
            			}
    					if(game.equals(gameLH)){
    						amountLH = gameData.getInt("amount");
    						contentsLH = gameData.getString("contents");
    						contentsLH = contentsLH.equals("L")?"龙":"虎";
    					}

    					

            		}
            		
    				String outputStr = "";
    				if(amountDX != 0 ){
    					if(i == 1){
    						outputStr  = String.format("冠军%s: %d,", contentsDX, amountDX);
    						System.out.println(outputStr);
    					}
    					else if(i == 2){
    						outputStr  = String.format("亚军%s: %d,", contentsDX, amountDX);
    						System.out.println(outputStr);
    					}
    					else{
    						outputStr  = String.format("第%s名%s: %d,", Integer.toString(i), contentsDX, amountDX);
    						System.out.println(outputStr);
    					}
    					
    					totalAmout += amountDX;
    				}
    				
    				if(amountDS != 0 ){
    					if(i == 1){
    						outputStr  = String.format("冠军%s: %d,", contentsDS, amountDS);
    						System.out.println(outputStr);
    					}
    					else if(i == 2){
    						outputStr  = String.format("亚军%s: %d,", contentsDS, amountDS);
    						System.out.println(outputStr);
    					}
    					else{
    						outputStr  = String.format("第%s名%s: %d,", Integer.toString(i), contentsDS, amountDS);
    						System.out.println(outputStr);
    					}
    					totalAmout += amountDS;
    				}
    				
    				if(amountLH != 0 ){
    					if(i == 1){
    						outputStr  = String.format("冠军%s: %d,", contentsLH, amountLH);
    						System.out.println(outputStr);
    					}
    					else if(i == 2){
    						outputStr  = String.format("亚军%s: %d,", contentsLH, amountLH);
    						System.out.println(outputStr);
    					}
    					else{
    						outputStr  = String.format("第%s名%s: %d,", Integer.toString(i), contentsLH, amountLH);
    						System.out.println(outputStr);
    					}
    					totalAmout += amountLH;
    					
    				}
            		
    				System.out.print("\n");
            	}
            	System.out.print("下单总金额:" + totalAmout + "\n");
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
  	
    }
}

class MyCompare implements Comparator // 实现Comparator，定义自己的比较方法
{
	public int compare(Object o1, Object o2) {
		Object[] e1 = (Object[]) o1;
		Object[] e2 = (Object[]) o2;

		if ((long) e1[1] > (long) e2[1])// 这样比较是降序,如果把-1改成1就是升序.
		{
			return 1;
		} else {
			return -1;
		}
	}
}

class CompareStr implements Comparator // 实现Comparator，定义自己的比较方法
{
	public int compare(Object o1, Object o2) {
		Object[] e1 = (Object[]) o1;
		Object[] e2 = (Object[]) o2;

		if (Long.parseLong((String) e1[1]) > Long.parseLong((String) e2[1]))// 这样比较是降序,如果把-1改成1就是升序.
		{
			return -1;
		} else {
			return 1;
		}
	}
}
