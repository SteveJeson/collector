package com.zdzc.collector;

import com.zdzc.collector.util.DataType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollectorApplicationTests {

    @Test
    public void contextLoads() {
        System.out.println(DataType.HEARTBEAT.getValue());
    }

}
