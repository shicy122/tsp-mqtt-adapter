package com.hycan.idn.adapter.biz;

import com.hycan.idn.tsp.common.feign.annotation.EnableTspFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MQTT适配项目启动类
 */
@EnableAsync
@EnableScheduling
@EnableTspFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MqttAdapterApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MqttAdapterApplication.class);
        application.setAllowBeanDefinitionOverriding(true);

        application.run(args);
    }
}
