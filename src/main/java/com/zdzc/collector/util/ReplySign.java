package com.zdzc.collector.util;

public class ReplySign {

    public static int pkg_delimiter = 0x7e;// 标识位
    public static String string_encoding = "GBK";//字符编码格式
    public static String replyToken = "1234567890Z";//鉴权码

    //msg是上行消息  cmd是下行指令
    public static int msg_id_terminal_common_resp = 0x0001;// 终端通用应答
    public static int msg_id_terminal_heart_beat = 0x0002;// 终端心跳
    public static int msg_id_terminal_register = 0x0100;// 终端注册
    public static int msg_id_terminal_log_out = 0x0003;// 终端注销
    public static int msg_id_terminal_authentication = 0x0102;// 终端鉴权
    public static int msg_id_terminal_location_info_upload = 0x0200;// 位置信息汇报
    public static int msg_id_terminal_transmission_tyre_pressure = 0x0600;// 胎压数据透传
    public static int msg_id_terminal_param_query_resp = 0x0104;// 查询终端参数应答
    public static int msg_id_terminal_location_info_batch_upload = 0x0704;// 定位数据批量上传
    public static int msg_id_terminal_prop_query_resp = 0x0107;//查询终端属性应答

    public static int cmd_common_resp = 0x8001;// 平台通用应答
    public static int cmd_terminal_register_resp = 0x8100;// 终端注册应答
    public static int cmd_terminal_param_settings = 0X8103;// 设置终端参数
    public static int cmd_terminal_param_query = 0x8104;// 查询终端参数
    public static int cmd_terminal_prop_query = 0x8107;//查询终端属性
}
