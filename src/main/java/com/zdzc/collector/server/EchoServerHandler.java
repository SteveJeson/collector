package com.zdzc.collector.server;

import com.zdzc.collector.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EchoServerHandler.class);

    static final ChannelGroup channels = new DefaultChannelGroup(
            GlobalEventExecutor.INSTANCE);

    private MqSender mqSender;

    public EchoServerHandler(MqSender mqSender) {
        this.mqSender = mqSender;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
//             ByteBuf in = (ByteBuf) msg;
//             String data = in.toString(io.netty.util.CharsetUtil.UTF_8);
//            String str = CommonUtil.bytebufToHexstr(in);
            Message message = (Message) (msg);
            System.out.println(message);
            // Receive Message from client
            // Send Message to rabbit MQ who wants to subscribe
//            String dataString = new String(Message.getData(), CharsetUtil.UTF_8);
//            mqSender.send(dataString);

            // Echo server: send back the msg to client (just for test)
//            log.debug(String.format("Receive Message: %s", dataString));
//            ctx.writeAndFlush(Unpooled.copiedBuffer(Message.getData()));
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // A closed channel will be removed from ChannelGroup automatically
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Disconnected client -> " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
//        ctx.close();
        log.warn(cause.getMessage());
    }

}
