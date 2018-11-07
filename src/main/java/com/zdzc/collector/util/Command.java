package com.zdzc.collector.util;

public class Command {

    public static final int PKG_DELIMITER = 0x7e;// 标识位
    public static final String STRING_ENCODING = "GBK";//字符编码格式
    public static final String REPLYTOKEN = "1234567890Z";//鉴权码

    //msg是上行指令  cmd是下行指令
    public static final int MSG_ID_TERMINAL_HEART_BEAT = 0x0002;// 终端心跳
    public static final int MSG_ID_TERMINAL_REGISTER = 0x0100;// 终端注册
    public static final int MSG_ID_TERMINAL_AUTHENTICATION = 0x0102;// 终端鉴权
    public static final int MSG_ID_TERMINAL_LOCATION_INFO_UPLOAD = 0x0200;// 位置信息汇报
    public static final int MSG_ID_TERMINAL_LOCATION_INFO_BATCH_UPLOAD = 0x0704;// 定位数据批量上传
    public static final int MSG_ID_TERMINAL_PROP_QUERY_RESP = 0x0107;//查询终端属性应答

    public static final int CMD_COMMON_RESP = 0x8001;// 平台通用应答
    public static final int CMD_TERMINAL_REGISTER_RESP = 0x8100;// 终端注册应答
    public static final int CMD_TERMINAL_PROP_QUERY = 0x8107;//查询终端属性
}
