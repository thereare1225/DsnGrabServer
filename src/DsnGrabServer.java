import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.json.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;  
import java.util.Map;
import java.util.Set;


public class DsnGrabServer {
 
    // 用于检测所有的Channel状态的selector
    private Selector selector = null;
    static final int PORT = 12321;
    private Charset charse = Charset.forName("ASCII");
    private ConnManager connManager = new ConnManager(new DsnGrabUI());
 
    public void init() {
    	try {
	    	connManager.start();
	        selector = Selector.open();
	        ServerSocketChannel server = ServerSocketChannel.open();
	        //InetSocketAddress isa = new InetSocketAddress("0.0.0.0", PORT);
	        InetSocketAddress isa = new InetSocketAddress("127.0.0.1", PORT);
	        server.bind(isa);
	        
	        server.configureBlocking(false);
	        // 将server注册到指定的selector对象
	        server.register(selector, SelectionKey.OP_ACCEPT);
	        int a = 0;  
	        
	        while ((a = selector.select()) >= 0) {
	        	if(a == 0) {
	        		continue;
	        	}
	        	
	        	Set readyKeys=selector.selectedKeys();  
	            Iterator it = readyKeys.iterator();
	        	while(it.hasNext()) {
	        		SelectionKey sk = (SelectionKey)it.next(); 
	                it.remove();
	                
	                if (sk.isAcceptable()) {
	                	try {
		                    SocketChannel sc = server.accept();
		                    sc.configureBlocking(false);
		                    sc.register(selector, SelectionKey.OP_READ, SelectionKey.OP_WRITE); 
		                    String ip = sc.getRemoteAddress().toString();
		                    connManager.addConn(sc, ip); 
		                    System.out.println("新的连接：" + sc);    
	                	} catch(IOException io) {
	                		io.printStackTrace();
	                	}
	                }
	                // 如果sk对应的Channel有数据需要读取
	                if (sk.isReadable()) {
	                    SocketChannel sc = (SocketChannel) sk.channel();
	                    connManager.recvMsg(sc);
	                    ByteBuffer buff = ByteBuffer.allocate(1024);
	                    String content = "";
	                    try {
	                        while(true) {
	                        	int n = sc.read(buff);
	                        	if(n > 0) {
	                        		System.out.println("读取buff：");
		                            buff.flip();
		                            content += charse.decode(buff);
		                            buff.flip();
	                        	} else if(n == 0) {
	                        		if(!parseData(sc, content)) {
	                        			disconnect(sk);
	                        			break;
	                        		}
	                        		//sc.write(charse.encode(data));
	                        		break;
	                        	} else if(n == -1) {
	                        		System.out.println(sc.socket() + "连接断开");
	                        		disconnect(sk);
	                                break;
	                        	}
	                        }
	                    }
	                    catch (IOException io) {
	                        // 从Selector中删除指定的SelectionKey
	                    	io.printStackTrace();
	                    	System.out.println(sc.socket() + "对方强制断开");
	                    	disconnect(sk);
	                    }
	
	                }
	            }
	        }
        } catch (IOException io) {
        	io.printStackTrace();
        } 
    }
    
    public boolean parseData(SocketChannel sc, String data) {
    	try {
			JSONObject json = new JSONObject(data);
			String request = json.getString("request");
			if(request.equals("data")) {
				System.out.println("读取的数据：" + json.getString("lottery"));
				String lottery = json.getString("lottery").toString();
				switch(lottery) {
					case "CQSSC": {
						String [] datas = DsnProxyGrab.getCQSSCdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[2]);
			                map.put("data", datas[1]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;
					case "BJSC": {
						String [] datas = DsnProxyGrab.getBJSCdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[4]);
			                map.put("dataGY", datas[1]);
			                map.put("dataSSWL", datas[2]);
			                map.put("dataQBJS", datas[3]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;	
					case "XYNC": {
						String [] datas = DsnProxyGrab.getXYNCdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[10]);
			                map.put("data1", datas[1]);
			                map.put("data2", datas[2]);
			                map.put("data3", datas[3]);
			                map.put("data4", datas[4]);
			                map.put("data5", datas[5]);
			                map.put("data6", datas[6]);
			                map.put("data7", datas[7]);
			                map.put("data8", datas[8]);
			                map.put("data9", datas[9]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
						
					}
					break;
					case "GDKLSF": {
						String [] datas = DsnProxyGrab.getGDKLdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[10]);
			                map.put("data1", datas[1]);
			                map.put("data2", datas[2]);
			                map.put("data3", datas[3]);
			                map.put("data4", datas[4]);
			                map.put("data5", datas[5]);
			                map.put("data6", datas[6]);
			                map.put("data7", datas[7]);
			                map.put("data8", datas[8]);
			                map.put("data9", datas[9]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
						
					}
					break;
					case "GXKLSF": {
						String [] datas = DsnProxyGrab.getGXKLdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[2]);
			                map.put("data", datas[1]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;
					case "TJSSC": {
						String [] datas = DsnProxyGrab.getTJSSCdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[2]);
			                map.put("data", datas[1]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;
					case "XJSSC": {
						String [] datas = DsnProxyGrab.getXJSSCdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[2]);
			                map.put("data", datas[1]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;
					case "GD11X5": {
						String [] datas = DsnProxyGrab.getGD115data();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[2]);
			                map.put("data", datas[1]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;
					case "BJKL8": {
						String [] datas = DsnProxyGrab.getBJKL8data();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[2]);
			                map.put("data", datas[1]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
							ByteBuffer buff = charse.encode(strReturn);
							try {
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
									}
							} catch (IOException e) {
								return false;
							}
						} else {
							try {
								Map<String, String> map = new HashMap<String, String>();
								map.put("result", "false");
								JSONObject jsonReturn = new JSONObject(map);  
				                String strReturn = jsonReturn.toString();
							
								ByteBuffer buff = charse.encode(strReturn);
								System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;
				}
			} else if(request.equals("register")) {
				connManager.registerConn(sc, json.getString("account"), json.getString("website"));
				try {
					Map<String, String> map = new HashMap<String, String>();
					map.put("result", "true");
					JSONObject jsonReturn = new JSONObject(map);  
	                String strReturn = jsonReturn.toString();
				
					ByteBuffer buff = charse.encode(strReturn);
					System.out.println("包大小" + buff.limit());
					while(buff.position() < buff.limit()) {
						sc.write(buff);		
						System.out.println("位置" + buff.position());
					}
				} catch (IOException e) {
					return false;
				}
			}
			else{
				System.out.println("数据包内容错误");
				return false;
			}
			
		} catch (JSONException e) {
			System.out.println("json数据包错误");
			return false;
		}
    	return true;
    }
    
    public void disconnect(SelectionKey sk) {
        if (sk.channel() != null) {
        	connManager.delConn((SocketChannel)sk.channel());
        }
        sk.cancel();
    }
     
    public static void main(String [] args) throws IOException{
    	//重定向输出
    	try {  
	    	//生成路径  
	    	File dir = new File("log");  
	        if (dir.exists()) {   
	        } 
	        else {
	        	dir.mkdirs();
	        }
	         
	        //把输出重定向到文件
	    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
	    	PrintStream ps=new PrintStream("log/" + df.format(new Date()) + ".txt");  
	    	System.setOut(ps);
	    	System.setErr(ps);
	    } catch (FileNotFoundException e) {  
	    	e.printStackTrace();
		} 
    	new DsnGrabServer().init();
    }
 
}