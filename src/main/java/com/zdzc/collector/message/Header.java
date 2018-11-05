package com.zdzc.collector.message;

import java.nio.ByteBuffer;

public class Header {

    private int msgLength; // The whole Message length includes header

    private byte msgType; // one byte for Message type

    public int getMsgLength() {
        // ByteBuffer b = ByteBuffer.wrap(msgLength);
        // return b.getInt();
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        // ByteBuffer b = ByteBuffer.allocate(4);
        // b.putInt(msgLength);
        // this.msgLength = b.array();
        this.msgLength = msgLength;
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public byte[] getBytes() {
        byte[] buffer = new byte[5];
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(msgLength);
        System.arraycopy(b.array(), 0, buffer, 0, 4);
        buffer[4] = msgType;
        return buffer;
    }
}
