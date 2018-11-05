package com.zdzc.collector.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MqFactory {

    private static final Logger log = LoggerFactory.getLogger(MqFactory.class);

    protected CopyOnWriteArrayList<Channel> heartbeatChannels = new CopyOnWriteArrayList<>();

    protected CopyOnWriteArrayList<Channel> gpsChannels = new CopyOnWriteArrayList<>();

    protected CopyOnWriteArrayList<Channel> alarmChannels = new CopyOnWriteArrayList<>();

    protected CopyOnWriteArrayList<Channel> replyChannels = new CopyOnWriteArrayList<>();

    protected ConnectionFactory factory;

    public MqFactory(){}

    public MqFactory(String host, String username, String password) {
        //Create a connection factory
        this.factory = new ConnectionFactory();

        //hostname of your rabbitmq server
        this.factory.setHost(host);
        this.factory.setUsername(username);
        this.factory.setPassword(password);
        this.factory.setPort(5672);
    }



}
