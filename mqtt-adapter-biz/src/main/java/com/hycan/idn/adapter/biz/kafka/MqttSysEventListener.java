package com.hycan.idn.adapter.biz.kafka;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.CommonConstants;
import com.hycan.idn.adapter.biz.constant.KafkaTopicConstants;
import com.hycan.idn.adapter.biz.mqtt.event.publisher.SysMessageEventPublisher;
import com.hycan.idn.adapter.biz.util.JSON;
import com.hycan.idn.adapter.dto.ClientConnOrDiscMsgDTO;
import com.hycan.idn.tsp.common.core.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * MQTT客户端上下线消息Listener
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 17:34
 */
@Slf4j
@Component
public class MqttSysEventListener implements BatchAcknowledgingMessageListener<String, byte[]> {

    private final SysMessageEventPublisher sysMessageEventPublisher;

    private final boolean isEnableSysBridge;

    public MqttSysEventListener(SysMessageEventPublisher sysMessageEventPublisher, AdapterConfig adapterConfig) {
        this.sysMessageEventPublisher = sysMessageEventPublisher;
        this.isEnableSysBridge = adapterConfig.getMqttx().getEnableSysBridge();
    }

    /**
     * 监听MQTT客户端上下线消息
     */
    @Override
    @KafkaListener(topics = KafkaTopicConstants.MQTT_SYS_EVENT_TOPIC, concurrency = "3", containerFactory = "kafkaListenerContainerFactoryK2")
    public void onMessage(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        try {
            if (Boolean.FALSE.equals(isEnableSysBridge)) {
                return;
            }

            for (final ConsumerRecord<String, byte[]> record : records) {
                ClientConnOrDiscMsgDTO clientConnOrDiscMsgDTO = JSON.readValue(record.value(), ClientConnOrDiscMsgDTO.class);
                if (Objects.isNull(clientConnOrDiscMsgDTO)) {
                    continue;
                }

                String clientId = clientConnOrDiscMsgDTO.getClientId();
                if (!clientId.startsWith(CommonConstants.TBOX_CLIENT_ID_PREFIX)) {
                    continue;
                }

                sysMessageEventPublisher.publish(clientConnOrDiscMsgDTO.getType(), clientId);
            }
        } catch (Exception e) {
            log.error("处理Kafka消息异常={}", ExceptionUtil.getExceptionCause(e));
        } finally {
            ack.acknowledge();
        }
    }
}