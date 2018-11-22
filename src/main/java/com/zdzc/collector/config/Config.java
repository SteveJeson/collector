package com.zdzc.collector.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    private static Properties config = null;

    public Config() {
        config = new Properties();
    }

    public Config(String filePath) {
        config = new Properties();
        try {
            ClassLoader CL = this.getClass().getClassLoader();
            InputStream in;
            if (CL != null) {
                in = CL.getResourceAsStream(filePath);
            }else {
                in = ClassLoader.getSystemResourceAsStream(filePath);
            }
            config.load(in);
            //    in.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public String getValue(String key) {
        if (config.containsKey(key)) {
            String value = config.getProperty(key);
            return value;
        }else {
            return "";
        }
    }

    public int getValueInt(String key) {
        String value = getValue(key);
        int valueInt = 0;
        try {
            valueInt = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return valueInt;
        }
        return valueInt;
    }
}
