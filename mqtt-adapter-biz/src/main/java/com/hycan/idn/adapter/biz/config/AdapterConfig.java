package com.hycan.idn.adapter.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

/**
 * Adapter服务配置
 *
 * @author shichongying
 * @datetime 2023年 02月 27日 10:27
 */
@Data
@Order(700)
@Configuration
@ConfigurationProperties(prefix = "adapter")
public class AdapterConfig {

    /** Kafka topic 与 appId 对照关系 */
    private Map<String, List<Integer>> kafkaTopic;

    /** 需要返回 commandId 的业务 */
    private List<Integer> ackAppId;

    /** 是否开启连续在线72小时强制下线 */
    private Boolean enableForceOffline = false;

    Log log = new Log();

    Mqttx mqttx = new Mqttx();

    @Data
    public static class Log {

        /** 是否开启心跳日志打印 */
        private Boolean enableHeartbeat = false;

        /** 是否开启连接状态日志打印 */
        private Boolean enableConnectStatus = false;

        /** 是否开启国标登入登出日志打印 */
        private Boolean enableGb = false;

        /** 是否开启上行消息日志打印 */
        private Boolean enableUpMsg = false;

        /** 是否开启下行消息日志打印 */
        private Boolean enableDownMsg = false;

        /** 是否开启客户端上线日志打印 */
        private Boolean enableOnline = false;

        /** 是否开启客户端下线日志打印 */
        private Boolean enableOffline = false;

        /** 是否开启数据报文日志打印 */
        private Boolean enableRawData = false;
    }

    @Data
    public static class Mqttx {

        /** 是否开启业务消息桥接 */
        private Boolean enableBizBridge = false;

        /** 是否开启系统消息桥接 */
        private Boolean enableSysBridge = false;

        /** MQTTX服务的k8s域名 */
        private String endpoint;
    }
}
