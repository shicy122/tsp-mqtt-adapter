package com.hycan.idn.adapter.biz.constant;

/**
 * Kafka Topic常量
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
public interface KafkaTopicConstants {

    /**
     * 接收协议引擎下发指令Topic
     */
    String MQTT_COMMAND_TOPIC = "mqtt-command";

    /**
     * 接收MQTT Broker桥接消息Topic
     */
    String MQTT_BRIDGE_TOPIC = "tbox-mqttx-bridge-biz";

    /**
     * 接收MQTT Broker客户端上下线消息Topic
     */
    String MQTT_SYS_EVENT_TOPIC = "tbox-mqttx-bridge-sys";

    /**
     * 推送车辆状态到车况Topic
     */
    String VEHICLE_STATUS_TOPIC = "acp-vehicle-status";
}
