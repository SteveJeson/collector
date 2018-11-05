package com.zdzc.collector.util;

public enum DataType {
    GPS(1, "定位"), ALARM(2, "报警"), HEARTBEAT(3, "心跳");

    private int value;
    private String desc;

    private DataType(int value, String desc) {
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
        DataType defaultStatus = DataType.GPS;
        for(DataType status : DataType.values()){
            if(status.getValue() == value){
                return status;
            }
        }
        return defaultStatus;
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
