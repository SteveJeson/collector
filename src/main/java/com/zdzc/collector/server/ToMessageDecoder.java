package com.zdzc.collector.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.message.Header;
import com.zdzc.collector.message.Message;
import com.zdzc.collector.util.Command;
import com.zdzc.collector.util.CommonUtil;
import com.zdzc.collector.util.DataType;
import com.zdzc.collector.util.ProtocolSign;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ToMessageDecoder extends MessageToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ToMessageDecoder.class);

    /**
     * 解析数据
     * @param channelHandlerContext
     * @param o
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
        ByteBuf in = (ByteBuf)o;
        byte[] arr = new byte[in.readableBytes()];
        if(!in.hasArray()){
            in.getBytes(in.readerIndex(), arr);
            String src = ByteArrayUtil.toHexString(arr);
            logger.info("source data -> "+src);
        }

        String beginMark = StringUtil.byteToHexStringPadded(arr[0]);

        List<String> markList = Arrays.asList(ProtocolSign.JT808.getValue(), ProtocolSign.WRT.getValue());
        if(!markList.contains(beginMark.toUpperCase())){
            System.out.println("未知协议内容！");
        }else{
            if(markList.get(0).equals(beginMark.toUpperCase())){
                if (!in.hasArray()) {//false表示为这是直接缓冲
                    int length = in.readableBytes();//得到可读字节数
                    byte[] array = new byte[length];    //分配一个具有length大小的数组
                    in.getBytes(in.readerIndex(), array); //将缓冲区中的数据拷贝到这个数组中
                    Message msg = toJt808Decoder(array);
                    if(msg != null){
                        list.add(msg);
                    }
                }
            }else if(markList.get(1).equals(beginMark.toUpperCase())){
                System.out.println("沃瑞特协议");
            }
        }
    }

    /**
     * JT808数据解析
     * @param data
     */
    private Message toJt808Decoder(byte[] data){
        String hexstr = StringUtil.toHexStringPadded(data);
        logger.info("source data -> "+hexstr);
        byte[] bs = doReceiveEscape(data);
        String hex = StringUtil.toHexStringPadded(bs);
        Boolean isValid = validateChecksum(bs);
        if(!isValid){
            logger.error("校验码验证错误：" + hex + " src -> " + hexstr);
            return null;
        }
       return decodeMessage(data);

    }

    /**
     * 转义还原(JT808协议)
     * 0x7d 0x01 -> 0x7d
     * 0x7d 0x02 -> 0x7e
     * @param data
     * @return
     */
    private byte[] doReceiveEscape(byte[] data){
        ByteBuffer bb = ByteBuffer.allocate(data.length);

        for (int i = 0; i < data.length; i++)
        {
            if (data[i] == 0x7d && data[i + 1] == 0x01)
            {
                bb.put((byte)0x7d);
                i++;
            }
            else if (data[i] == 0x7d && data[i + 1] == 0x02)
            {
                bb.put((byte)0x7e);
                i++;
            }
            else
            {
                bb.put(data[i]);
            }
        }
        return bb.array();
    }

    /**
     * 验证校验和(JT808协议)
     * @param data
     * @return
     */
    private Boolean validateChecksum(byte[] data){
        // 1. 去掉分隔符之后，最后一位就是校验码
        int checkSumInPkg = data[data.length - 1 - 1];
        int calculatedCheckSum = calculateChecksum(data, 1, data.length - 1 - 1);
        if (checkSumInPkg != calculatedCheckSum)
        {
            return false;
        }
        return true;
    }

    /**
     * 计算校验和(JT808协议)
     *从开始标识符后一位到校验和前一位做异或运算
     * @param data
     * @param from
     * @param to
     * @return
     */
    private int calculateChecksum(byte[] data, int from, int to){
        int cs = 0;
        for (int i = from; i < to; i++)
        {
            cs ^= data[i];

        }
        return cs;
    }

    /**
     * 解析消息
     * @param data
     * @return
     */
    private Message decodeMessage(byte[] data){
        //设置消息头
        Header header = new Header();
        decodeHeader(data, header);
        Message message = new Message();
        message.setHeader(header);//设置消息头
        int msgBodyByteStartIndex = 12 + 1;
        // 3. 消息体
        // 有子包信息,消息体起始字节后移四个字节:消息包总数(word(16))+包序号(word(16))
        if (header.hasSubPackage())
        {
            msgBodyByteStartIndex = 16 + 1;
        }
        //设置消息体
        byte[] buffer = new byte[header.getMsgBodyLength()];
        System.arraycopy(data, msgBodyByteStartIndex, buffer, 0,header.getMsgBodyLength());
        message.setBody(buffer);//设置消息体
        message.setAll(StringUtil.toHexStringPadded(data));

        //设置应答消息
        setReplyBodyAndType(message);
        return message;
    }

    private void decodeHeader(byte[] data, Header header){
        int msgId = CommonUtil.cutBytesToInt(data, 1, 2);
        int msgBodyProps = CommonUtil.cutBytesToInt(data, 2 + 1, 2);
        boolean hasSubPackage = (((msgBodyProps & 0x2000) >> 13) == 1);
        int msgBodyLength = (CommonUtil.cutBytesToInt(data, 2 + 1, 2) & 0x3ff);
        String terminalPhone = StringUtil.toHexStringPadded(CommonUtil.subByteArr(data, 5, 6));
        int flowId = CommonUtil.cutBytesToInt(data, 11, 2);
        header.setMsgId(msgId);
        header.setMsgBodyProps(msgBodyProps);
        header.setHasSubPackage(hasSubPackage);
        header.setMsgBodyLength(msgBodyLength);
        header.setTerminalPhone(terminalPhone);
        header.setMsgLength(data.length);
        header.setProtocolType(StringUtil.toHexStringPadded(data, 0, 1));
        header.setFlowId(flowId);
    }

    /**
     * 设置应答消息
     * @param message
     */
    private void setReplyBodyAndType(Message message){
        int msgId = message.getHeader().getMsgId();
        String terminalPhone = message.getHeader().getTerminalPhone();
        int flowId = message.getHeader().getFlowId();
        byte[] body = message.getBody();
        String all = message.getAll();
        if (msgId == Command.MSG_ID_TERMINAL_REGISTER)
        {
            logger.info("【808】终端注册 ==> " + all);
            //1. 终端注册 ==> 终端注册应答
            byte[] sendMsg = newRegistryReplyMsg(0014, terminalPhone, flowId);
            message.setReplyBody(sendMsg);
            message.getHeader().setMsgType(DataType.Registry.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_AUTHENTICATION)
        {
            logger.info("【808】终端鉴权 ==> " + all);
            //2. 终端鉴权 ==> 平台通用应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            //查询终端属性
            byte[] sendBody = newQueryPropReplyMsg(0005, terminalPhone, flowId);
            message.setExtReplyBody(sendBody);
            message.getHeader().setMsgType(DataType.Authentication.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_HEART_BEAT)
        {
            //3. 终端心跳-消息体为空 ==> 平台通用应答
            logger.info("【808】终端心跳 ==> " + all);
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            String time = sdf.format(new Date());
            byte[] msgBody = ByteArrayUtil.hexStringToByteArray(time);//自定义心跳body为当前时间
            //客户端消息应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            //设置回复信息
            message.setBody(msgBody);
            message.setReplyBody(sendMsg);
            message.getHeader().setMsgType(DataType.HEARTBEAT.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_LOCATION_INFO_UPLOAD)
        {
            logger.info("【808】终端定位（单个） ==> " + all);
            //4. 位置信息汇报 ==> 平台通用应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            int alarmSign = CommonUtil.cutBytesToInt(body, 0, 4);
            message.getHeader().setMsgType(alarmSign <= 0?DataType.GPS.getValue():DataType.ALARM.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_LOCATION_INFO_BATCH_UPLOAD)
        {
            logger.info("【808】终端定位（批量） ==> " + all);
            //5.定位数据批量上传0x0704协议解析
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            byte[] mb = CommonUtil.subByteArr(body, 5, body.length - 5);
            int alarmSign = CommonUtil.cutBytesToInt(mb, 0, 4);
            message.getHeader().setMsgType(alarmSign <= 0?DataType.GPS.getValue():DataType.ALARM.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_PROP_QUERY_RESP)
        {
            logger.info("【808】终端属性查询应答 ==> " + all);
            //6.终端属性应答消息
            byte[] msgType = new byte[1];
            msgType[0] = 02;
            byte[] newBodyByte = CommonUtil.bytesMerge(msgType, body);
            message.setBody(newBodyByte);
            message.getHeader().setMsgType(DataType.Property.getValue());
        }else
        {
            logger.error("【808】未知消息类型，终端手机号 ==> "+terminalPhone);
        }
    }

    /**
     * 终端注册消息应答
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @return
     */
    public byte[] newRegistryReplyMsg(int msgBodyProps, String phone, int flowId)
    {
        //7E
        //8100            消息ID
        //0004            消息体属性
        //018512345678    手机号
        //0015            消息流水号
        //0015            应答流水号
        //04              结果(00成功, 01车辆已被注册, 02数据库中无该车辆, 03终端已被注册, 04数据库中无该终端)  无车辆与无终端有什么区别 ?
        //313C             鉴权码
        //7E
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = CommonUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = CommonUtil.integerTo2Bytes(Command.CMD_TERMINAL_REGISTER_RESP);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = CommonUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = CommonUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = CommonUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        // 6. 应答流水号
        byte[] bt6 = CommonUtil.integerTo2Bytes(flowId);
        len += bt6.length;
        // 7. 成功
        byte[] bt7 = CommonUtil.integerTo1Bytes(0);
        len += bt7.length;
        // 8. 鉴权码
        byte[] bt8 = new byte[0];
        try {
            bt8 = Command.REPLYTOKEN.getBytes(Command.STRING_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.error("replytoken parse error: "+e.getMessage());
        }
        len += bt8.length;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);
        buffer.put(bt6);
        buffer.put(bt7);
        buffer.put(bt8);
        // 校验码
        int checkSum = calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = CommonUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        //结束符
        buf.put(bt1);

        // 转义
        return doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

    /**
     * 发送消息时转义
     * @param data
     * @param start
     * @param end
     * @return
     */
    private static byte[] doSendEscape(byte[] data, int start, int end)
    {
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        for (int i = 0; i < start; i++)
        {
            buffer.put(data[i]);
        }
        for (int i = start; i < end; i++)
        {
            if (data[i] == 0x7e)
            {
                buffer.put((byte)0x7d);
                buffer.put((byte)0x02);
            }
            else
            {
                buffer.put(data[i]);
            }
        }
        for (int i = end; i < data.length; i++)
        {
            buffer.put(data[i]);
        }
        return buffer.array();
    }

    /**
     * 通用消息应答
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @param msgId
     * @return
     */
    public byte[] newCommonReplyMsg(int msgBodyProps, String phone, int flowId, int msgId)
    {
        //7E
        //8100            消息ID
        //0004            消息体属性
        //018512345678    手机号
        //0015            消息流水号
        //0015            应答流水号
        //04              结果(00成功, 01车辆已被注册, 02数据库中无该车辆, 03终端已被注册, 04数据库中无该终端)  无车辆与无终端有什么区别 ?
        //313C             鉴权码
        //7E
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = CommonUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = CommonUtil.integerTo2Bytes(Command.CMD_COMMON_RESP);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = CommonUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = CommonUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = CommonUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        // 6. 应答流水号
        byte[] bt6 = CommonUtil.integerTo2Bytes(flowId);
        len += bt6.length;
        // 7. 对应终端消息ID
        byte[] bt7 = CommonUtil.integerTo2Bytes(msgId);
        len += bt7.length;
        // 8. 成功
        byte[] bt8 = CommonUtil.integerTo1Bytes(0);
        len += bt8.length;

        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);
        buffer.put(bt6);
        buffer.put(bt7);
        buffer.put(bt8);
        // 校验码
        int checkSum = calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = CommonUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        //结束符
        buf.put(bt1);
        // 转义
        return doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

    /**
     * 终端鉴权 ==> 查询终端属性
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @return
     */
    public byte[] newQueryPropReplyMsg(int msgBodyProps, String phone, int flowId){
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = CommonUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = CommonUtil.integerTo2Bytes(Command.CMD_TERMINAL_PROP_QUERY);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = CommonUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = CommonUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = CommonUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);

        // 6.校验码
        int checkSum = calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = CommonUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        // 7. 0x7e
        buf.put(bt1);

        // 转义
        return doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

}
