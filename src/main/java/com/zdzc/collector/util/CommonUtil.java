package com.zdzc.collector.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;

public class CommonUtil {

    public static String bytebufToHexstr(ByteBuf byteBuf){
        String hexstr = "";
        if (!byteBuf.hasArray()) {//false表示为这是直接缓冲
            int length = byteBuf.readableBytes();//得到可读字节数
            byte[] array = new byte[length];    //分配一个具有length大小的数组
            byteBuf.getBytes(byteBuf.readerIndex(), array); //将缓冲区中的数据拷贝到这个数组中
            for(int i = 0;i < array.length;i++){
                String str = StringUtil.byteToHexStringPadded(array[i]);
                hexstr += str;
            }
        }
        return hexstr;
    }
}
