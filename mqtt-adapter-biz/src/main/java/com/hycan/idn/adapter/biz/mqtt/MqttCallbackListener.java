package com.hycan.idn.adapter.biz.mqtt;

import com.hycan.idn.adapter.biz.service.IMqttxRemoteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.integration.mqtt.event.MqttProtocolErrorEvent;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MQTT Broker回调监听器
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:45
 */
@Slf4j
@Component
@AllArgsConstructor
public class MqttCallbackListener {

    private final IMqttxRemoteService mqttxRemoteService;

    /**
     * mqtt连接失败或者订阅失败时, 触发MqttConnectionFailedEvent事件
     */
    @EventListener(MqttConnectionFailedEvent.class)
    public void mqttConnectionFailedEvent(MqttConnectionFailedEvent event) {
        log.error("MQTT连接错误: 发生时间=[{}], 错误源=[{}], 错误详情={}", LocalDateTime.now(), event.getSource(), event.getCause().getMessage());
        if (event.getSource() instanceof MqttPahoMessageHandler) {
            MqttPahoMessageHandler handler = (MqttPahoMessageHandler) event.getSource();
            char[] pubPwd = mqttxRemoteService.searchEncryptPwd(MqttOutboundAdapter.PUB_CLIENT_ID);
            handler.getConnectionInfo().setPassword(pubPwd);
        } else if (event.getSource() instanceof MqttPahoMessageDrivenChannelAdapter) {
            MqttPahoMessageDrivenChannelAdapter adapter = (MqttPahoMessageDrivenChannelAdapter) event.getSource();
            char[] subPwd = mqttxRemoteService.searchEncryptPwd(MqttInboundAdapter.SUB_CLIENT_ID);
            adapter.getConnectionInfo().setPassword(subPwd);
        }
    }

    /**
     * 客户端交互期间发生 MQTT 错误
     */
    @EventListener(MqttProtocolErrorEvent.class)
    public void mqttProtocolErrorEvent(MqttProtocolErrorEvent event) {
        log.error("MQTT交互错误: 发生时间=[{}], 错误源=[{}], 错误详情=[{}]", LocalDateTime.now(), event.getSource(), event.getCause().getMessage());
    }
}
