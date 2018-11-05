package com.zdzc.collector.server;

import com.zdzc.collector.message.Header;
import com.zdzc.collector.message.Message;
import com.zdzc.collector.util.ProtocolType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.StringUtil;

import java.util.List;

public class ToMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
                          List<Object> out) throws Exception {

        // At least 5 bytes to decode
        if (in.readableBytes() < 5) {
            return;
        }

        String beginMark = StringUtil.byteToHexStringPadded(in.readByte());
        if(ProtocolType.JT808.getValue().equals(beginMark)){
            System.out.println("808协议");
        }else if(ProtocolType.WRT.getValue().toString().equals(beginMark)){
            System.out.println("沃瑞特协议");
        }else{
            System.out.println("未知协议内容");
            return;
        }

//        if (msgLength >= 5) {
//            ByteBuf bf = in.readBytes(msgLength - 5);
//            byte[] data = bf.array();
//            Header header = new Header();
//            header.setMsgLength(msgLength);
//            header.setMsgType(msgType);
//
//            Message message = new Message();
//            message.setHeader(header);
//            message.setData(data);
//
//            out.add(message); // Decode one Message successfully
//        }
    }
}
