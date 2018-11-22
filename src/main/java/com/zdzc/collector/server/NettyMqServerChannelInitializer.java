package com.zdzc.collector.server;

import com.zdzc.collector.rabbitmq.MqInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyMqServerChannelInitializer extends
        ChannelInitializer<SocketChannel> {

    private MqInitializer mqInitializer;

    private MqSender mqSender;

    public NettyMqServerChannelInitializer(MqInitializer mqInitializer, MqSender mqSender) {
        this.mqSender = mqSender;
        this.mqInitializer = mqInitializer;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Reader ilde time 3 minutes
        ch.pipeline().addLast(new IdleStateHandler(5 * 60, 0, 0));
        ch.pipeline().addLast(new HeartBeatHandler());
        ch.pipeline().addLast(new ToMessageDecoder());
        ch.pipeline().addLast(new EchoServerHandler(mqInitializer, mqSender));
    }
}
