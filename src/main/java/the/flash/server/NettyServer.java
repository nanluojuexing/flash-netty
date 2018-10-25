package the.flash.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;

public class NettyServer {

    private static final int PORT = 8000;

    public static void main(String[] args) {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        /**
         * netty服务端启动的参数
         *      线程模型
         *      io模型
         *      连接读写处理逻辑
         *
         *      childHandler() 用于处理 新来你的读写处理逻辑
         *      attr()  给每个连接增加自定义属性
         *      childAttr()  给每个连接增加自定义属性
         *      childOption() 给每条连接设置 TCP 相关的属性
         *          ChannelOption.SO_KEEPALIVE  表示是否开启TCP底层心跳机制，true为开启
         *          ChannelOption.TCP_NODELAY   表示是否开启Nagle算法，true表示关闭，false表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启...
         *      option()  给每条连接设置属性以外还可以设置服务端 channel 的属性 如 sc_backlog 表示系统临时存放完成 三次握手的请求的队列的最大长度
         *
         */
        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new ServerHandler());
                    }
                });


        bind(serverBootstrap, PORT);

    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(new Date() + ": 端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
            }
        });
    }
}
