package com.zdzc.collector.server;

import com.zdzc.collector.message.Message;
import com.zdzc.collector.util.DataType;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EchoServerHandler.class);

    static final ChannelGroup channels = new DefaultChannelGroup(
            GlobalEventExecutor.INSTANCE);

    private static final AtomicInteger gpsNum = new AtomicInteger(0);
    private static final AtomicInteger alarmNum = new AtomicInteger(0);
    private static final AtomicInteger heartbeatNum = new AtomicInteger(0);

    private MqSender mqSender;

    public EchoServerHandler(MqSender mqSender) {
        this.mqSender = mqSender;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Message message = (Message) (msg);
            //给客户端发送应答消息
            if(message.getReplyBody() != null){
                ctx.writeAndFlush(Unpooled.copiedBuffer(message.getReplyBody()));
            }
            if(message.getExtReplyBody() != null){
                ctx.writeAndFlush(Unpooled.copiedBuffer(message.getExtReplyBody()));
            }
            //将收到的定位、报警、心跳消息推送至Rabbitmq
            int msgType = message.getHeader().getMsgType();
            if(msgType == DataType.GPS.getValue()){
                //定位
                gpsNum.incrementAndGet();
                System.out.println("gps num ==> " + gpsNum.intValue());
                mqSender.send(gpsNum.intValue());
            }else if(msgType == DataType.ALARM.getValue()){
                //报警
                alarmNum.incrementAndGet();
                System.out.println("alarm num ==> " + alarmNum.intValue());
            }else if(msgType == DataType.HEARTBEAT.getValue()){
                //心跳
                heartbeatNum.incrementAndGet();
                System.out.println("heartbeat num ==> " + heartbeatNum.intValue());
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // A closed channel will be removed from ChannelGroup automatically
        channels.add(ctx.channel());
        log.info("clients num ==> "+ channels.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("Disconnected client -> " + ctx.channel().remoteAddress());
        log.info("clients num ==> " + channels.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
        log.warn(cause.getMessage());
    }

}
