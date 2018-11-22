package com.zdzc.collector;

import com.zdzc.collector.util.DataType;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class CollectorApplicationTests {

    @Test
    public void contextLoads() {
//        System.out.println(DataType.HEARTBEAT.getValue());
//        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
//        String time = sdf.format(new Date());
//        System.out.println(time);
        int a = 1;
        System.out.println(Integer.parseInt("20000000"));
//        System.out.println(Integer.parseInt();
        System.out.println(Integer.parseInt("4194304"));
        String hexAddr = "20000000";
        String binAddr = Integer.toBinaryString(Integer.parseInt(hexAddr, 16));
        System.out.println(binAddr);
        String r = StringUtils.leftPad(binAddr, 32, '0');
        System.out.println(r);
        System.out.println(10 % 10);
    }

}
