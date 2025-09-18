package com.example.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 基于Netty的WebSocket服务器
 * 支持高并发连接和消息处理
 */
@Component
public class NettyWebSocketServer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketServer.class);
    
    private static final int PORT = 9999;
    private static final String WEBSOCKET_PATH = "/ws";
    
    @Autowired
    private WebSocketChannelHandler webSocketChannelHandler;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    /**
     * 启动WebSocket服务器
     */
    @Override
    public void run(String... args) throws Exception {
        startServer();
    }

    public void startServer() {
        bossGroup = new NioEventLoopGroup(1); // 处理连接请求
        workerGroup = new NioEventLoopGroup(); // 处理IO操作
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // HTTP编解码器
                            pipeline.addLast(new HttpServerCodec());
                            
                            // HTTP对象聚合器，将多个HTTP消息合并为一个完整的HTTP消息
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            
                            // 支持大文件传输
                            pipeline.addLast(new ChunkedWriteHandler());
                            
                            // WebSocket协议处理器
                            pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
                            
                            // 自定义WebSocket消息处理器
                            pipeline.addLast(webSocketChannelHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(PORT).sync();
            serverChannel = future.channel();
            
            logger.info("🚀 Netty WebSocket服务器启动成功！");
            logger.info("📡 WebSocket地址: ws://localhost:{}{}", PORT, WEBSOCKET_PATH);
            logger.info("🌐 测试页面: http://localhost:8080/websocket-test");
            
            // 等待服务器关闭
            future.channel().closeFuture().sync();
            
        } catch (Exception e) {
            logger.error("WebSocket服务器启动失败", e);
        } finally {
            shutdown();
        }
    }

    /**
     * 优雅关闭服务器
     */
    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("🛑 Netty WebSocket服务器已关闭");
    }
}
