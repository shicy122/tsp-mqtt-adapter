package com.hycan.idn.adapter.biz.kafka;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.KafkaTopicConstants;
import com.hycan.idn.adapter.biz.mqtt.MqttMessageHandler;
import com.hycan.idn.adapter.biz.util.KryoSerializer;
import com.hycan.idn.common.core.util.BytesUtil;
import com.hycan.idn.mqttx.pojo.BridgeMsg;
import com.hycan.idn.tsp.common.core.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * MQTT桥接消息Listener
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 17:34
 */
@Slf4j
@Component
public class MqttBridgeMsgListener implements BatchAcknowledgingMessageListener<String, byte[]> {

    private final MqttMessageHandler mqttMessageHandler;

    private final boolean isEnableBizBridge;

    public MqttBridgeMsgListener(AdapterConfig adapterConfig, MqttMessageHandler mqttMessageHandler) {
        this.mqttMessageHandler = mqttMessageHandler;
        this.isEnableBizBridge = adapterConfig.getMqttx().getEnableBizBridge();
    }

    /**
     * 监听MQTT桥接的国标、企标消息
     */
    @Override
    @KafkaListener(topics = KafkaTopicConstants.MQTT_BRIDGE_TOPIC, concurrency = "3", containerFactory = "kafkaListenerContainerFactoryK2")
    public void onMessage(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        try {
            if (Boolean.FALSE.equals(isEnableBizBridge)) {
                return;
            }

            for (final ConsumerRecord<String, byte[]> record : records) {
                BridgeMsg bridgeMsg = KryoSerializer.deserialize(record.value(), BridgeMsg.class);
                String topic = bridgeMsg.getTopic();
                byte[] data = bridgeMsg.getPayload();

                if (!StringUtils.hasText(topic) || data.length <= 0) {
                    log.error("MQTT消息格式错误! Topic=[{}], Payload=[{}]", topic, data);
                    return;
                }
                mqttMessageHandler.handleBizMessage(topic, BytesUtil.bytesToHexString(data));
            }
        } catch (Exception e) {
            log.error("处理Kafka消息异常={}", ExceptionUtil.getExceptionCause(e));
        } finally {
            ack.acknowledge();
        }
    }
}