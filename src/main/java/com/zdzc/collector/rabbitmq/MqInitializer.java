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
    public String hostname;

    @Value("${mq.server.username}")
    public String username;

    @Value("${mq.server.password}")
    public String password;

    @Value("${mq.server.port}")
    public int port;

    @Value("${gps.connection.count}")
    public int gpsConnCount;

    @Value("${gps.channel.count}")
    public int gpsChannelCount;

    @Value("${gps.queue.prex}")
    public String gpsQueuePrex;

    @Value("${gps.queue.count}")
    public int gpsQueueCount;

    @Value("${gps.queue.start}")
    public int gpsQueueStart;

    @Value("${alarm.connection.count}")
    public int alarmConnCount;

    @Value("${alarm.channel.count}")
    public int alarmChannelCount;

    @Value("${alarm.queue.prex}")
    public String alarmQueuePrex;

    @Value("${alarm.queue.count}")
    public int alarmQueueCount;

    @Value("${alarm.queue.start}")
    public int alarmQueueStart;

    @Value("${heartbeat.connection.count}")
    public int heartbeatConnCount;

    @Value("${heartbeat.channel.count}")
    public int heartbeatChannelCount;

    @Value("${heartbeat.queue.prex}")
    public String heartbeatQueuePrex;

    @Value("${heartbeat.queue.count}")
    public int heartbeatQueueCount;

    @Value("${heartbeat.queue.start}")
    public int heartbeatQueueStart;

    @Value("${business.connection.count}")
    public int businessConnCount;

    @Value("${business.channel.count}")
    public int businessChannelCount;

    @Value("${business.queue.prex}")
    public String businessQueuePrex;

    @Value("${business.queue.count}")
    public int businessQueueCount;

    @Value("${business.queue.start}")
    public int businessQueueStart;

    public ConnectionFactory factory;

    public static final Logger logger = LoggerFactory.getLogger(MqInitializer.class);

    public CopyOnWriteArrayList<Channel> gpsChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> alarmChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> heartbeatChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> businessChannels = new CopyOnWriteArrayList<>();

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

        //业务
        for(int i = 0;i < businessConnCount;i++){
            logger.info("create BUSINESS connection " + (i + 1));
            createQueues(this.factory.newConnection(), DataType.BUSINESS.getValue());
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
    public void createQueues(Connection connection, int type){
        if(DataType.GPS.getValue() == type){
            createQueues(connection, gpsQueuePrex, gpsChannelCount, gpsQueueCount, type);
        }else if(DataType.ALARM.getValue() == type){
            createQueues(connection, alarmQueuePrex, alarmChannelCount, alarmQueueCount, type);
        }else if (DataType.HEARTBEAT.getValue() == type) {
            createQueues(connection, heartbeatQueuePrex, heartbeatChannelCount, heartbeatQueueCount, type);
        }else if(DataType.BUSINESS.getValue() == type){
            createQueues(connection, businessQueuePrex, businessChannelCount, businessQueueCount, type);
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
    public void createQueues(Connection connection, String queuePrex, int channelCount, int queueCount, int type){
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
                }else if(DataType.BUSINESS.getValue() == type){
                    businessChannels.add(channel);
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }


}
