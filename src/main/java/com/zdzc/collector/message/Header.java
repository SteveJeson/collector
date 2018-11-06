package com.zdzc.collector.message;

public class Header {

    private int msgLength; // 整条消息的长度

    private String protocolType; // 协议类型

    private int msgId;  // 消息ID

    private int msgBodyLength;  // 消息体长度

    private String terminalPhone;   //终端手机号

    private int flowId; //消息流水号

    private int msgBodyProps;   //消息体属性

    private boolean hasSubPackage;  //是否有子包

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgBodyLength() {
        return msgBodyLength;
    }

    public void setMsgBodyLength(int msgBodyLength) {
        this.msgBodyLength = msgBodyLength;
    }

    public String getTerminalPhone() {
        return terminalPhone;
    }

    public void setTerminalPhone(String terminalPhone) {
        this.terminalPhone = terminalPhone;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public int getMsgBodyProps() {
        return msgBodyProps;
    }

    public void setMsgBodyProps(int msgBodyProps) {
        this.msgBodyProps = msgBodyProps;
    }

    public boolean isHasSubPackage() {
        return hasSubPackage;
    }

    public void setHasSubPackage(boolean hasSubPackage) {
        this.hasSubPackage = hasSubPackage;
    }

    //    public byte[] getBytes() {
//        byte[] buffer = new byte[5];
//        ByteBuffer b = ByteBuffer.allocate(4);
//        b.putInt(msgLength);
//        System.arraycopy(b.array(), 0, buffer, 0, 4);
//        buffer[4] = msgType;
//        return buffer;
//    }
}
