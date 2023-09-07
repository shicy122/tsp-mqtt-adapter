package com.hycan.idn.mqttx.pojo;

import lombok.Data;

/**
 * 客户端上/下线集群消息
 *
 * @author shichongying
 * @datetime 2023年 03月 09日 10:57
 */
@Data
public class ClientConnOrDiscMsg {

    /** 上/下线客户端ID */
    private String clientId;

    /** 当前实例ID */
    private String instanceId;

    /** 上/下线类型(connected/disconnected) */
    private String type;

    /** 时间戳 */
    private long timestamp;
}
