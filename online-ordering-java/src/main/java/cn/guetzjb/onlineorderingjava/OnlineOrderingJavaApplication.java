package cn.guetzjb.onlineorderingjava;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableScheduling
public class OnlineOrderingJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineOrderingJavaApplication.class, args);
    }

}
