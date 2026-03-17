package cn.guetzjb.onlineorderingjava.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "server-config")
public class ServerProperties {

    private String ip;
    private String username;
    private String password;
    private Integer port;
    private String dir;

    private static ServerProperties INSTANCE;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static ServerProperties getInstance() {
        return INSTANCE;
    }

}
