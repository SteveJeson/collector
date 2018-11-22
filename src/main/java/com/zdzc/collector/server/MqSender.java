package com.zdzc.collector.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.zdzc.collector.client.ClientPoolManager;
import com.zdzc.collector.message.Message;
import io.netty.channel.pool.FixedChannelPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MqSender {

    private static final Logger logger = LoggerFactory.getLogger(MqSender.class);

    private FixedChannelPool channelPool;

    public MqSender(){
        this.channelPool = ClientPoolManager.connect();
    }

    public void send(Channel channel, byte[] sendBody, Message message, String qname){
        try {
            channel.basicPublish("", qname, MessageProperties.PERSISTENT_TEXT_PLAIN, sendBody);
            sendToRemote(message.getAll());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void sendToRemote(String message){
        io.netty.channel.Channel channel = null;
        try {
            channel = channelPool.acquire().sync().get();
            channel.write(ByteArrayUtil.hexStringToByteArray(message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            channelPool.release(channel);
        }

    }
}
