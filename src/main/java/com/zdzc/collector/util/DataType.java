package com.zdzc.collector.util;

public enum DataType {
    GPS(1, "终端定位"), ALARM(2, "终端报警"), HEARTBEAT(3, "终端心跳"),
    Registry(4, "终端注册"), Authentication(5, "终端鉴权"), Property(6, "终端属性"), BUSINESS(7, "报警业务");

    private int value;
    private String desc;

    DataType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    /**
     * 根据值返回名称
     * @param value
     * @return
     */
    public static DataType getStatusByValue(int value){
        for(DataType status : DataType.values()){
            if(status.getValue() == value){
                return status;
            }
        }
        return null;
    }


    /**
     * 根据值返回描述
     * @param value
     * @return
     */
    public static String getDescByValue(int value){
        return getStatusByValue(value).getDesc();
    }

}
