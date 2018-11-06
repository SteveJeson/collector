package com.zdzc.collector.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyMqServerChannelInitializer extends
        ChannelInitializer<SocketChannel> {
    // private EventBus eventBus;
    private MqSender mqSender;

    public NettyMqServerChannelInitializer(MqSender mqSender) {
        this.mqSender = mqSender;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Reader ilde time 3 minutes
        ch.pipeline().addLast(new IdleStateHandler(3 * 60, 0, 0));
        ch.pipeline().addLast(new HeartBeatHandler());
        ch.pipeline().addLast(new ToMessageDecoder());
        ch.pipeline().addLast(new EchoServerHandler(mqSender));
    }
}
