package com.hycan.idn.adapter.biz.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 错误码
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
public enum ErrorCodeEnum {

    // 0-99 系统错误码
    TBOX_TOPIC_CONFIG_INFO_NOT_EXIST(100, "mqttx与tBox的协议主题映射关系缺失,请检查相关配置"),
    TBOX_TOPIC_PARAMETER_FORMAT_INCORRECT(101, "tBox上报Topic参数格式有误"),
    MQTT_BROKER_INFO_INCORRECT(102, "mqttx-broker参数信息格式有误");


    private final String message;
    private final int code;

    ErrorCodeEnum(int code, String message) {
        this.message = message;
        this.code = code;
    }

    @JsonValue
    public String getMessage() {
        return this.message;
    }

    public int getCode() {
        return this.code;
    }


    static {
        // check that the codes are unique.
        if (Arrays.stream(ErrorCodeEnum.values()).map(ErrorCodeEnum::getCode).distinct()
                .count() != ErrorCodeEnum.values().length) {
            throw new IllegalStateException("Response Codes aren't unique.");
        }
    }
}
