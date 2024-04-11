package com.hycan.idn.adapter.biz.kafka;

import cn.hutool.core.text.StrPool;
import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.KafkaTopicConstants;
import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import com.hycan.idn.adapter.biz.exception.AdapterBusinessException;
import com.hycan.idn.adapter.biz.service.IMqttBizMessageService;
import com.hycan.idn.adapter.biz.util.EncodeUtil;
import com.hycan.idn.adapter.biz.util.ThreadPoolUtil;
import com.hycan.idn.common.core.util.BytesUtil;
import com.hycan.idn.common.core.util.JsonUtil;
import com.hycan.idn.tsp.common.core.util.ExceptionUtil;
import com.hycan.idn.tsp.engine.command.AcpMessageHeader;
import com.hycan.idn.tsp.engine.command.AcpRawMessage;
import com.hycan.idn.tsp.engine.command.VehicleCommand;
import com.hycan.idn.tsp.engine.command.acp.entity.VehicleDescription;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 协议引擎下行消息Listener
 *
 * <p>
 * 主要逻辑:
 * 1. 接收下发的原始消息并组装报文
 * 2. 通过http接口下发报文信息到指定的mqtt
 * 3. 返回应答消息到协议引擎
 * <p>
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 17:34
 */
@Slf4j
@Component
public class MqttCommandListener implements BatchAcknowledgingMessageListener<String, String> {

    private final EncodeUtil encodeUtil;

    private final StringRedisTemplate redisTemplate;

    private final IMqttBizMessageService messageService;

    private final boolean enableLog;

    public MqttCommandListener(EncodeUtil encodeUtil,
                               StringRedisTemplate redisTemplate,
                               IMqttBizMessageService messageService,
                               AdapterConfig config) {
        this.encodeUtil = encodeUtil;
        this.redisTemplate = redisTemplate;
        this.messageService = messageService;

        this.enableLog = config.getLog().getEnableDownMsg();
    }

    /**
     * 监听协议引擎发送到MQTT的下行指令消息
     */
    @Override
    @KafkaListener(topics = KafkaTopicConstants.MQTT_COMMAND_TOPIC, concurrency = "3")
    public void onMessage(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        try {
            for (final ConsumerRecord<String, String> record : records) {
                String vin = String.valueOf(record.key());
                String value = record.value();
                if (StringUtils.isBlank(vin) || Objects.isNull(value)) {
                    log.error("mqtt-command receive message is null");
                    continue;
                }
                sendMessage(vin, value);
            }
        } catch (Exception e) {
            log.error("处理Kafka消息异常={}", ExceptionUtil.getExceptionCause(e));
        } finally {
            ack.acknowledge();
        }
    }

    /**
     * 发送下行指令、缓存指令、应答协议引擎
     *
     * @param vin   VIN码
     * @param value 车控指令
     */
    @Async(ThreadPoolUtil.DOWN_MSG_THREAD)
    protected void sendMessage(String vin, String value) {
        if (enableLog) {
            log.info("接收远程指令: VIN=[{}], 指令数据=[{}]", vin, value);
        }
        VehicleCommand vehicleCommand = JsonUtil.readValue(value, VehicleCommand.class);
        int appId = vehicleCommand.getApplicationID();

        byte[] encodePayload = encodeUtil.encodePayload(appId, vehicleCommand.getData());
        // 下发指令消息到MQTTX
        messageService.sendDownMessage(appId, vehicleCommand.getCarSeries(), vin, encodePayload);
        // 缓存车辆和下发指令的映射关系
        cacheCommand(appId, vin, vehicleCommand.getCommandId(), encodePayload);
        // 将组装后的完整报文，发送到协议引擎
        AcpRawMessage ackRwqMessage = buildAckRawMessage(vin, vehicleCommand, encodePayload);
        messageService.sendUpMessage(vin, appId, ackRwqMessage, true);
    }

    /**
     * 构建AcpRawMessage用于返回ack
     *
     * @param vin            VIN码
     * @param vehicleCommand 车控指令
     * @param encodePayload  编码后的payload
     * @return 组装完整的应答消息
     */
    private AcpRawMessage buildAckRawMessage(String vin, VehicleCommand vehicleCommand, byte[] encodePayload) {
        AcpRawMessage acpMessage = new AcpRawMessage();
        AcpMessageHeader acpMessageHeader = new AcpMessageHeader();
        acpMessageHeader.setApplicationID(vehicleCommand.getApplicationID());
        acpMessage.setHeader(acpMessageHeader);
        VehicleDescription vehicleDescription = new VehicleDescription();
        vehicleDescription.setCommandId(Long.valueOf(vehicleCommand.getCommandId()));
        vehicleDescription.setVin(vin);
        acpMessage.setVehicleDescription(vehicleDescription);
        acpMessage.setDownPayload(encodePayload);
        return acpMessage;
    }

    /**
     * 缓存车辆和下发指令的映射关系
     * (用于TBox上行消息时候, 解析出SeqId用于返回给协议引擎)
     *
     * @param vin           VIN码
     * @param commandId     车控指令
     * @param encodePayload 编码后的payload
     */
    private void cacheCommand(int appId, String vin, String commandId, byte[] encodePayload) {
        int seqId = BytesUtil.parseBytesToShort(BytesUtil.getWord(13, encodePayload));
        String key = RedisKeyConstants.MQTT_COMMAND + vin + StrPool.UNDERLINE + appId + StrPool.UNDERLINE + seqId;
        redisTemplate.opsForValue().set(key, commandId, 10L, TimeUnit.MINUTES);
    }
}