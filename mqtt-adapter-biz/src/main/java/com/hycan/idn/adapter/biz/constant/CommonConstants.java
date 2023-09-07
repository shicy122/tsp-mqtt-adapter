package com.hycan.idn.adapter.biz.constant;

/**
 * Adapter 通用常量
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
public interface CommonConstants {

    /** mqtt-adapter作为MQTT订阅客户端的client_id前缀 */
    String TSP_ADPT_SUB_CLIENT_ID = "TSP_ADPT_SUB_";
    String TSP_ADPT_PUB_CLIENT_ID = "TSP_ADPT_PUB_";

    /** TBOX clientId(格式：tbox_${vin}_1) */
    String TBOX_CLIENT_ID = "tbox_%s_1";

    /**工作模式默认心跳周期(单位:秒)  */
    Integer WORK_MODEL_HEARTBEAT_PERIOD = 10;

    /** 低功耗模式默认心跳周期(单位:秒)  */
    Integer HIBERNATE_HEARTBEAT_PERIOD = 900;

    String MQTT_TOPIC_FORMAT_VIN = "vin";

    String MQTT_TOPIC_FORMAT_SERIAL = "serial";

    String TBOX_CLIENT_ID_PREFIX = "tbox_";

    String HEARTBEAT = "HEARTBEAT";
    String FORCE_OFFLINE = "FORCE_OFFLINE";
}
