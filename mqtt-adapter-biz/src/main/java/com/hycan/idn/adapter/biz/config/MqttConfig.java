package com.hycan.idn.adapter.biz.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * mqtt属性配置类
 *
 * @author shichongying
 * @datetime 2023年 02月 27日 10:27
 */
@Data
@Slf4j
@Order(600)
@Configuration
@ConfigurationProperties(prefix = "adapter.mqttx")
public class MqttConfig {

    /** 出站通道 */
    public static final String OUTBOUND_CHANNEL = "mqttOutboundChannel";

    /** 输入通道 */
    public static final String INPUT_CHANNEL = "mqttInputChannel";

    /** Qos 报文的服务质量等级，默认1(最少一次) */
    public static final Integer DEFAULT_QOS = 1;

    /** 连接MQTT Broker集群的用户名 */
    private String username;

    /** 订阅主题列表 */
    private String[] topics;

    /** MQTT Broker集群地址 */
    private String[] url;

    /** 连接超时 */
    public Integer connectionTimeout = 60;

    /** 长连接最大时长为120秒 */
    public Integer keepAliveInterval = 120;

    /** 完成断开连接超时时间5秒 */
    public Integer completionTimeout = 5000;

    /** 重试间隔时间10秒 */
    public Integer recoveryInterval = 10000;

}
