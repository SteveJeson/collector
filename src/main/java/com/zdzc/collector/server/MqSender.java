package com.zdzc.collector.server;

import com.zdzc.collector.rabbitmq.MqInitializer;

public class MqSender extends MqInitializer {

    public void send(int no){
        System.out.println("gps channels num ==> "+getGpsChannels().size());
    }
}
