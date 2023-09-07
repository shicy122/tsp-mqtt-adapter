package com.hycan.idn.adapter.biz.mqtt.event.publisher;

import com.hycan.idn.adapter.biz.mqtt.event.UpMessageEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 上行消息事件发布者
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:45
 */
@Component
public class UpMessageEventPublisher {

    private final ApplicationContext applicationContext;

    public UpMessageEventPublisher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 发布上行消息事件
     *
     * @param type    消息类型(对appId进行归类，方便监听器按条件执行)
     * @param appId   协议标识
     * @param serial  车系
     * @param vin     VIN码
     * @param payload 协议数据
     */
    public void publish(String type, int appId, String serial, String vin, byte[] payload) {
        applicationContext.publishEvent(new UpMessageEvent(this, type, appId, serial, vin, payload));
    }
}