package com.hycan.idn.adapter.biz.mqtt.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 上行消息事件
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:45
 */
@Getter
@Setter
public class UpMessageEvent extends ApplicationEvent {

    private String type;
    private int appId;
    private String serial;
    private String vin;
    private byte[] payload;

    public UpMessageEvent(Object source, String type, int appId, String serial, String vin, byte[] payload) {
        super(source);

        this.type = type;
        this.serial = serial;
        this.appId = appId;
        this.vin = vin;
        this.payload = payload;
    }
}