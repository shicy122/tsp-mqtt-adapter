package com.hycan.idn.adapter.biz.mqtt.event.publisher;

import com.hycan.idn.adapter.biz.mqtt.event.SysMessageEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 系统消息事件发布者
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:45
 */
@Component
public class SysMessageEventPublisher {

    private final ApplicationContext applicationContext;

    public SysMessageEventPublisher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 发布系统消息事件
     *
     * @param type       协议类型
     * @param clientId   TBOX客户端ID
     */
    public void publish(String type, String clientId) {
        applicationContext.publishEvent(new SysMessageEvent(this, type, clientId));
    }
}