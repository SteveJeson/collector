package com.zdzc.collector;

import com.zdzc.collector.server.NettyMqServer;
import com.zdzc.collector.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CollectorApplication {

    public static void main(String[] args) {
        ApplicationContext context =  SpringApplication.run(CollectorApplication.class, args);
        SpringContextUtil.setApplicationContext(context);
        NettyMqServer server = new NettyMqServer();
        server.doStart();
    }
}
