package netty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import dsn.ConnManager;


public class NettyServer {
    protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors()*2; //Ĭ��  
 
    protected static final int BIZTHREADSIZE = 4;

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);  
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE); 
    private ConnManager connManager = null;
    public NettyServer(ConnManager connManager) {
    	this.connManager = connManager;
    }

    public void run() throws Exception { 

        ServerBootstrap b = new ServerBootstrap(); 
        b.group(bossGroup, workerGroup);  
        b.channel(NioServerSocketChannel.class);  
        b.childHandler(new ChannelInitializer<SocketChannel>() {  
            @Override  
            public void initChannel(SocketChannel ch) throws Exception {  
                ChannelPipeline pipeline = ch.pipeline();  
                System.out.println("connect" + ch);
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));  
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("decoder", new StringDecoder());  
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast("idle", new IdleStateHandler(20, 0, 0,TimeUnit.SECONDS)); 
                pipeline.addLast(new NettyServerHandler(connManager));  
            }  
        });  

        ChannelFuture f = b.bind("0.0.0.0",12321).sync();  


        System.out.println("netty server start success...");

        /**
         * wait until the socket close
         */
        f.channel().closeFuture().sync();

        shutdown();

    }

    protected static void shutdown() {  
        workerGroup.shutdownGracefully();  
        bossGroup.shutdownGracefully();
    }

/*    public static void main(String[] args) throws Exception {
           new NettyServer().run();
    }   */ 
}
