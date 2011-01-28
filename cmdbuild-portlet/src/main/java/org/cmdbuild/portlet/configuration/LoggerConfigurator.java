package org.cmdbuild.portlet.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

public class LoggerConfigurator {

    public LoggerConfigurator() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j.properties");
        Properties props = new Properties();
        try {
            props.load(inputStream);
            PropertyConfigurator.configure(props);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
