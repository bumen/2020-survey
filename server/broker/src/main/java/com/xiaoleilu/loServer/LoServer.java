package com.xiaoleilu.loServer;

import java.util.concurrent.TimeUnit;

import cn.wildfirechat.log.Logs;
import io.moquette.spi.IMessagesStore;
import io.moquette.spi.ISessionsStore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import win.liyufan.im.Utility;

import com.xiaoleilu.hutool.util.DateUtil;
import com.xiaoleilu.loServer.action.Action;
import com.xiaoleilu.loServer.action.ClassUtil;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.AdminActionHandler;
import com.xiaoleilu.loServer.handler.IMActionHandler;

/**
 * LoServer starter<br>
 * 用于启动服务器的主对象<br>
 * 使用LoServer.start()启动服务器<br>
 * 服务的Action类和端口等设置在ServerSetting中设置
 * @author Looly
 *
 */
public class LoServer {
    private static final org.slf4j.Logger Logger = Logs.SERVER;
	private int port;
    private int adminPort;
    private IMessagesStore messagesStore;
    private ISessionsStore sessionsStore;
    private Channel channel;
    private Channel adminChannel;

    public LoServer(int port, int adminPort, IMessagesStore messagesStore, ISessionsStore sessionsStore) {
        this.port = port;
        this.adminPort = adminPort;
        this.messagesStore = messagesStore;
        this.sessionsStore = sessionsStore;
    }

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    /**
	 * 启动服务
	 * @throws InterruptedException 
	 */
	public void start() throws InterruptedException {
		long start = System.currentTimeMillis();
		
		// Configure the server.
        bossGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("HTTP-Boss"));
        workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("HTTP-Worker"));

        registerAllAction();

		try {
			final ServerBootstrap b = new ServerBootstrap();
            final ServerBootstrap adminB = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 1024*64)
                .childOption(ChannelOption.SO_RCVBUF, 1024*64)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpRequestDecoder());
                        socketChannel.pipeline().addLast(new HttpResponseEncoder());
                        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                        socketChannel.pipeline().addLast(new HttpObjectAggregator(100 * 1024 * 1024));
                        socketChannel.pipeline().addLast(new IMActionHandler(messagesStore, sessionsStore));
					}
				});
			
			channel = b.bind(port).sync().channel();


            adminB.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 1024*64)
                .childOption(ChannelOption.SO_RCVBUF, 1024*64)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpRequestDecoder());
                        socketChannel.pipeline().addLast(new HttpResponseEncoder());
                        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                        socketChannel.pipeline().addLast(new HttpObjectAggregator(100 * 1024 * 1024));
                        socketChannel.pipeline().addLast(new AdminActionHandler(messagesStore, sessionsStore));
                    }
                });

            adminChannel = adminB.bind(adminPort).sync().channel();
			Logger.info("***** Welcome To LoServer on port [{},{}], startting spend {}ms *****", port, adminPort, DateUtil.spendMs(start));
		} finally {

		}
	}
    public void shutdown() {
        Logger.error("Lo server shutdown start...");

        if (bossGroup == null || workerGroup == null) {
            Logger.error("Lo server is not initialized");
            throw new IllegalStateException("Invoked close on an Acceptor that wasn't initialized");
        }

        Future<?> workerWaiter = bossGroup.shutdownGracefully();
        Future<?> bossWaiter = workerGroup.shutdownGracefully();

        Logger.info("Waiting for worker and boss event loop groups to terminate...");
        try {
            bossWaiter.await(2, TimeUnit.SECONDS);
            workerWaiter.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException iex) {
            Logger.warn("An InterruptedException was caught while waiting for event loops to terminate...");
        }

        if (!bossGroup.isTerminated()) {
            Logger.warn("Forcing shutdown of boss event loop...");
            bossGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }

        if (!workerGroup.isTerminated()) {
            Logger.warn("Forcing shutdown of worker event loop...");
            workerGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }

        Logger.error("Lo server shutdown finish");
    }

    private void registerAllAction() {
        try {
            for (Class cls:ClassUtil.getAllAssignedClass(Action.class)
                 ) {
                if(cls.getAnnotation(Route.class) != null) {
                    ServerSetting.setAction((Class<? extends Action>)cls);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utility.printExecption(Logger, e);
        }
    }
}
