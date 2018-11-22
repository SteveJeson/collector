package com.zdzc.collector.client;

import com.zdzc.collector.config.Config;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ClientPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientPoolManager.class);

    public static FixedChannelPool connect() {
        Config config = new Config("application.properties");
        String host = config.getValue("remote.server.host");
        int port = config.getValueInt("remote.server.port");
        int maxChannel = config.getValueInt("client.channel.max");

        EventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();

        // 连接池每次初始化一个连接的时候都会根据这个值去连接服务器
        InetSocketAddress remoteaddress = InetSocketAddress.createUnresolved(host, port);// 连接地址
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .remoteAddress(remoteaddress);

        // 初始化连接池
        FixedChannelPool channelPool = new FixedChannelPool(bootstrap, new ClientChannelPoolHandler(), maxChannel);
        logger.info("Init client channel pool with max connections -> "+maxChannel);
        return channelPool;
    }
}
