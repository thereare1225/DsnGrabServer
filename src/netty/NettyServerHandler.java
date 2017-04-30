package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import dsn.ConnManager;
import dsn.DsnProxyGrab;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
	 // 心跳丢失计数器
    private int counter = 0;
    ConnManager connManager;
    
    NettyServerHandler(ConnManager connManager) {
    	this.connManager = connManager;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
    	String request = (String) msg;
    	if(!parseData(request, ctx)) {
    		ctx.channel().close();
    	}
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("server channelReadComplete..");
        ctx.flush();
    }
   
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("server exceptionCaught.." + cause.getMessage());
        ctx.close();
    }
      
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Client is active ---" + ctx.channel().remoteAddress().toString());
    	connManager.addConn(ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Client is inactive ---" + ctx.channel().remoteAddress().toString());
    	connManager.delConn(ctx.channel().remoteAddress().toString());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
        	IdleStateEvent event = (IdleStateEvent) evt;  
            if (event.state() == IdleState.READER_IDLE) {         
                if(counter >= 3){  
                    System.out.println("===服务端===(读超时，关闭chanel)");  
                    ctx.channel().close(); 
                }else{  
                	counter++;
                }  
            }
        }
    }

    public boolean parseData(String data, ChannelHandlerContext ctx) {
    	try {
			JSONObject json = new JSONObject(data);
			String request = json.getString("request");
			if(request.equals("data")) {
				String lottery = json.getString("lottery").toString();
				switch(lottery) {
					case "CQSSC": {
						String [] datas = DsnProxyGrab.getCQSSCdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("lottery", "CQSSC");
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[2]);
			                map.put("data", datas[1]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);
			                String strReturn = jsonReturn.toString();
			                ctx.channel().writeAndFlush(strReturn);
						} else {
							Map<String, String> map = new HashMap<String, String>();
							map.put("result", "false");
							map.put("lottery", "CQSSC");
							JSONObject jsonReturn = new JSONObject(map);
			                String strReturn = jsonReturn.toString();
			                ctx.channel().writeAndFlush(strReturn);
						}
					}
					break;
					case "BJSC": {
						String [] datas = DsnProxyGrab.getBJSCdata();
						if(datas != null) {
							Map<String, String> map = new HashMap<String, String>();  
			                map.put("result", "true");
			                map.put("lottery", "BJSC");
			                if(DsnProxyGrab.isBetBJSCopen()) {
			                	map.put("open", "y");
			                } else {
			                	map.put("open", "n");
			                }
			                map.put("drawNumber", datas[0]);
			                map.put("remainTime", datas[4]);
			                map.put("dataGY", datas[1]);
			                map.put("dataSSWL", datas[2]);
			                map.put("dataQBJS", datas[3]);
			                map.put("percent", datas[5]);
			                map.put("positive", datas[6]);
			                
			                //将json转化为String类型    
			                JSONObject jsonReturn = new JSONObject(map);  
			                String strReturn = jsonReturn.toString();
						
			                ctx.channel().writeAndFlush(strReturn);
						} else {
							Map<String, String> map = new HashMap<String, String>();
							map.put("result", "false");
							map.put("lottery", "BJSC");
							JSONObject jsonReturn = new JSONObject(map);
			                String strReturn = jsonReturn.toString();
			                ctx.channel().writeAndFlush(strReturn);
						}
					}
					break;	
					/*case "XYNC": {
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
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
								//System.out.println("包大小" + buff.limit());
								while(buff.position() < buff.limit()) {
									sc.write(buff);		
									//System.out.println("位置" + buff.position());
								}
							} catch (IOException e) {
								return false;
							}
						}
					}
					break;*/
				}
			} else if(request.equals("register")) {
				connManager.registerConn(ctx.channel().remoteAddress().toString(), json.getString("account"), json.getString("website"));
				Map<String, String> map = new HashMap<String, String>();
				map.put("result", "true");
				JSONObject jsonReturn = new JSONObject(map);  
	            String strReturn = jsonReturn.toString();
	            
	            ctx.channel().writeAndFlush(strReturn);
				
			} else if(request.equals("heartbeat")) {
				System.out.println("heartbeat!");
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
    



}
