package com.hycan.idn.adapter.biz.enums;

import lombok.Getter;

/**
 * MQTT系统主题-客户端上/下线连接类型
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
@Getter
public enum ClientConnectEnum {

    /** 客户端上线系统事件 */
    ONLINE("connected", 1),

    /** 客户端离线系统事件 */
    OFFLINE("disconnected", 0);

    private final String type;
    private final int value;

    ClientConnectEnum(String type, int value) {
        this.type = type;
        this.value = value;
    }
}
