package org.talend.cveUtil.common;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties props;



    private ConfigManager() {
        props = new Properties();
        InputStream inputStream = null;

        try {


            final String dir = System.getProperty("user.dir");
            System.out.println(dir);
            final File configFile = new File(dir + "/config.properties");
            System.out.println(configFile);
            if (configFile.exists()) {
                inputStream = new FileInputStream(configFile);
            } else  {
                final String config_path = System.getProperty("config_path");
                System.out.println(config_path);
                if(config_path!=null && new File(config_path).exists()){
                    inputStream = new FileInputStream((config_path));
                }
            }
            if (inputStream == null) {
                throw new RuntimeException("config.properties not found in the classpath");
            }

            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static  ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getProperty(String key) {
        final String property = props.getProperty(key);
        if(property==null || "".equals(property)){
            throw new RuntimeException("config.properties <"+key +"> Not found in config.properties");
        }
        return property;
    }
}
