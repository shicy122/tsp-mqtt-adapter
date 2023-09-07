package com.hycan.idn.adapter.biz.mqtt.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 系统消息事件
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:45
 */
@Getter
@Setter
public class SysMessageEvent extends ApplicationEvent {

    private String type;
    private String clientId;

    public SysMessageEvent(Object source, String type, String clientId) {
        super(source);

        this.type = type;
        this.clientId = clientId;
    }
}