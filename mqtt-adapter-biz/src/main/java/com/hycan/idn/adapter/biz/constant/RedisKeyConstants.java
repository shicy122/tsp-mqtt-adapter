package com.hycan.idn.adapter.biz.constant;

/**
 * Redis Key前缀常量
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
public interface RedisKeyConstants {

    /** 指令下发key */
    String MQTT_COMMAND = "tsp:mqtt-adpt:command:";

    /** 下发到T-BOX会话序列ID的生成key，用于区分响应返回值为哪一条请求所发起(从0开始计数到65535后重新循环为0） */
    String T_BOX_SEQUENCE_ID = "tsp:mqtt-adpt:seq-id";

    /** T_box状态：0--断线、1--正常、2--低功耗 */
    String T_BOX_STATUS = "%s:state:vin";

    /** 车辆心跳周期（单位：秒） */
    String HEART_BEATER_PERIOD = "tsp:mqtt-adpt:heartbeat:period";

    /** 车辆心跳过期时间（单位：毫秒） */
    String HEART_BEATER_TIMEOUT = "tsp:mqtt-adpt:heartbeat:timeout";

    /** 车辆强制下线时间（单位：毫秒） */
    String FORCE_OFFLINE_TIME = "tsp:mqtt-adpt:force-offline:time";

}
