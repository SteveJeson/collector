package com.zdzc.collector.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.collector.util.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

@Configuration
@Component
public class MqInitializer {

    @Value("${mq.server.hostname}")
    private String hostname;

    @Value("${mq.server.username}")
    private String username;

    @Value("${mq.server.password}")
    private String password;

    @Value("${mq.server.port}")
    private int port;

    @Value("${gps.connection.count}")
    private int gpsConnCount;

    @Value("${gps.channel.count}")
    private int gpsChannelCount;

    @Value("${gps.queue.prex}")
    private String gpsQueuePrex;

    @Value("${gps.queue.count}")
    private int gpsQueueCount;

    @Value("${gps.queue.start}")
    private int gpsQueueStart;

    @Value("${alarm.connection.count}")
    private int alarmConnCount;

    @Value("${alarm.channel.count}")
    private int alarmChannelCount;

    @Value("${alarm.queue.prex}")
    private String alarmQueuePrex;

    @Value("${alarm.queue.count}")
    private int alarmQueueCount;

    @Value("${alarm.queue.start}")
    private int alarmQueueStart;

    @Value("${heartbeat.connection.count}")
    private int heartbeatConnCount;

    @Value("${heartbeat.channel.count}")
    private int heartbeatChannelCount;

    @Value("${heartbeat.queue.prex}")
    private String heartbeatQueuePrex;

    @Value("${heartbeat.queue.count}")
    private int heartbeatQueueCount;

    @Value("${heartbeat.queue.start}")
    private int heartbeatQueueStart;

    @Value("${business.connection.count}")
    private int businessConnCount;

    @Value("${business.channel.count}")
    private int businessChannelCount;

    @Value("${business.queue.prex}")
    private String businessQueuePrex;

    @Value("${business.queue.count}")
    private int businessQueueCount;

    @Value("${business.queue.start}")
    private int businessQueueStart;

    private ConnectionFactory factory;

    private static final Logger logger = LoggerFactory.getLogger(MqInitializer.class);

    public CopyOnWriteArrayList<Channel> gpsChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> alarmChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> heartbeatChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> replyChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> getGpsChannels(){
        return this.gpsChannels;
    }

    /**
     * 配置MQ
     * @throws IOException
     * @throws TimeoutException
     */
    @Bean
    public void configMq() throws IOException, TimeoutException {
        setFactory();
        //位置
        logger.info("Mq Server ==> " + hostname);
        for(int i = 0;i < gpsConnCount;i++){
            logger.info("create GPS connection " + (i + 1));
            createQueues(this.factory.newConnection(), DataType.GPS.getValue());
        }

        //报警
        for(int i = 0;i < alarmConnCount;i++){
            logger.info("create ALARM connection " + (i + 1));
            createQueues(this.factory.newConnection(), DataType.ALARM.getValue());
        }

        //心跳
        for(int i = 0;i < heartbeatConnCount;i++){
            logger.info("create HEARTBEAT connection " + (i + 1));
            createQueues(this.factory.newConnection(), DataType.HEARTBEAT.getValue());
        }

    }

    /**
     * Rabbitmq 工厂配置
     */
    private void setFactory(){
        this.factory = new ConnectionFactory();
        this.factory.setHost(hostname);
        this.factory.setUsername(username);
        this.factory.setPassword(password);
        this.factory.setPort(port);
    }

    /**
     * 创建消息队列
     * @param connection
     * @param type
     */
    private void createQueues(Connection connection, int type){
        if(DataType.GPS.getValue() == type){
            createQueues(connection, gpsQueuePrex, gpsChannelCount, gpsQueueCount, type);
        }else if(DataType.ALARM.getValue() == type){
            createQueues(connection, alarmQueuePrex, alarmChannelCount, alarmQueueCount, type);
        }else if (DataType.HEARTBEAT.getValue() == type) {
            createQueues(connection, heartbeatQueuePrex, heartbeatChannelCount, heartbeatQueueCount, type);
        }
    }

    /**
     * 创建消息队列
     * @param connection
     * @param queuePrex
     * @param channelCount
     * @param queueCount
     * @param type
     */
    private void createQueues(Connection connection, String queuePrex, int channelCount, int queueCount, int type){
        for(int i = 0;i < channelCount;i++){
            try {
                Channel channel = connection.createChannel();
                for(int j = 1;j <= queueCount;j++){
                    channel.queueDeclare(queuePrex + j,true,false,false,null);
                }
                if(DataType.GPS.getValue() == type){
                    gpsChannels.add(channel);
                }else if(DataType.ALARM.getValue() == type){
                    alarmChannels.add(channel);
                }else if(DataType.HEARTBEAT.getValue() == type){
                    heartbeatChannels.add(channel);
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }


}
