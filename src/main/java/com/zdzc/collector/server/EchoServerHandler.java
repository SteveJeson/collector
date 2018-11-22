package com.zdzc.collector.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.zdzc.collector.message.Message;
import com.zdzc.collector.rabbitmq.MqInitializer;
import com.zdzc.collector.util.CommonUtil;
import com.zdzc.collector.util.DataType;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EchoServerHandler.class);

    static final ChannelGroup channels = new DefaultChannelGroup(
            GlobalEventExecutor.INSTANCE);

    private static final AtomicInteger gpsNum = new AtomicInteger(0);
    private static final AtomicInteger alarmNum = new AtomicInteger(0);
    private static final AtomicInteger heartbeatNum = new AtomicInteger(0);

    private MqSender mqSender;

    private MqInitializer mqInitializer;


    public EchoServerHandler(MqInitializer mqInitializer, MqSender mqSender) {
        this.mqInitializer = mqInitializer;
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
                System.out.println("gps num ==> " + gpsNum.intValue());
                toSendGpsMessage(message);
            }else if(msgType == DataType.ALARM.getValue()){
                //报警
                System.out.println("alarm num ==> " + alarmNum.intValue());
                toSendAlarmMessage(message);

            }else if(msgType == DataType.HEARTBEAT.getValue()){
                //心跳
                System.out.println("heartbeat num ==> " + heartbeatNum.intValue());
                toSendHeartBeatMessage(message);
            }

        }catch (Exception e){
            log.error(e.getMessage());
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void toSendGpsMessage(Message message){
        gpsNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = mqInitializer.gpsChannels;
        Channel channel = channels.get(gpsNum.intValue() % channels.size());
        String qname = mqInitializer.gpsQueuePrex + (gpsNum.intValue() % mqInitializer.gpsQueueCount + 1);
        byte[] sign = new byte[1];
        sign[0] = 01;
        byte[] newBody = CommonUtil.bytesMerge(sign, message.getBody());
        byte[] sendMsg = CommonUtil.bytesMerge(
                ByteArrayUtil.hexStringToByteArray(message.getHeader().getTerminalPhone()), newBody);
        String hex = ByteArrayUtil.toHexString(sendMsg);
        mqSender.send(channel, hex.getBytes(CharsetUtil.UTF_8), message, qname);
    }

    private void toSendAlarmMessage(Message message){
        alarmNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = mqInitializer.alarmChannels;
        Channel channel = channels.get(alarmNum.intValue() % channels.size());
        String qname = mqInitializer.alarmQueuePrex + (alarmNum.intValue() % mqInitializer.alarmQueueCount + 1);
        byte[] sendMsg = CommonUtil.bytesMerge(ByteArrayUtil
                .hexStringToByteArray(message.getHeader().getTerminalPhone()), message.getBody());
        String hex = ByteArrayUtil.toHexString(sendMsg);
        mqSender.send(channel, hex.getBytes(CharsetUtil.UTF_8), message, qname);

        //报警推送到电动车平台MQ一份
        CopyOnWriteArrayList<Channel> chs = mqInitializer.businessChannels;
        Channel ch = chs.get(alarmNum.intValue() % chs.size());
        String qn = mqInitializer.businessQueuePrex +
                (alarmNum.intValue() % mqInitializer.businessQueueCount + mqInitializer.businessQueueStart);
        mqSender.send(ch, hex.getBytes(CharsetUtil.UTF_8), message, qn);
    }

    private void toSendHeartBeatMessage(Message message){
        heartbeatNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = mqInitializer.heartbeatChannels;
        Channel channel = channels.get(heartbeatNum.intValue() % channels.size());
        String qname = mqInitializer.heartbeatQueuePrex + (heartbeatNum.intValue() % mqInitializer.heartbeatQueueCount + 1);
        byte[] sendMsg = CommonUtil.bytesMerge(ByteArrayUtil
                .hexStringToByteArray(message.getHeader().getTerminalPhone()), message.getBody());
        String hex = ByteArrayUtil.toHexString(sendMsg);
        mqSender.send(channel, hex.getBytes(CharsetUtil.UTF_8), message, qname);
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
