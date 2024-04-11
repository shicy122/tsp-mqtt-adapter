package com.hycan.idn.adapter.biz.service.impl;

import cn.hutool.core.text.StrFormatter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.CommonConstants;
import com.hycan.idn.adapter.biz.pojo.dto.PubMsgDTO;
import com.hycan.idn.adapter.biz.pojo.entity.MqttTopicEntity;
import com.hycan.idn.adapter.biz.service.IMqttBizMessageService;
import com.hycan.idn.adapter.biz.service.IMqttxRemoteService;
import com.hycan.idn.common.core.util.BytesUtil;
import com.hycan.idn.common.core.util.JsonUtil;
import com.hycan.idn.tsp.common.core.util.ExceptionUtil;
import com.hycan.idn.tsp.engine.command.AcpRawMessage;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TBox数据上传相关接口Service
 *
 * @author: yaokun
 * @date: 2023/01/27
 */
@Slf4j
@Service
public class MqttBizMessageServiceImpl implements IMqttBizMessageService {

    private final LoadingCache<Integer, MqttTopicEntity> appId2MqttTopicCache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(12, TimeUnit.HOURS)
            .refreshAfterWrite(6, TimeUnit.HOURS)
            .build(new CacheLoader<Integer, MqttTopicEntity>() {
                @Override
                public @Nullable MqttTopicEntity load(@NonNull Integer appId) {
                    return selectMqttDownTopic(appId);
                }
            });

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final IMqttxRemoteService mqttxRemoteService;

    private final MongoTemplate mongoTemplate;

    private final boolean enableUpLog, enableDownLog, enableRawDataLog;

    private final Map<Integer, String> appId2KafkaTopicCache;

    private final List<Integer> ackAppId;

    public MqttBizMessageServiceImpl(AdapterConfig adapterConfig,
                                     KafkaTemplate<String, String> kafkaTemplate,
                                     IMqttxRemoteService mqttxRemoteService,
                                     MongoTemplate mongoTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.mqttxRemoteService = mqttxRemoteService;
        this.mongoTemplate = mongoTemplate;

        this.enableUpLog = adapterConfig.getLog().getEnableUpMsg();
        this.enableDownLog = adapterConfig.getLog().getEnableDownMsg();
        this.enableRawDataLog = adapterConfig.getLog().getEnableRawData();

        this.ackAppId = adapterConfig.getAckAppId();

        appId2KafkaTopicCache = adapterConfig.getKafkaTopic().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value ->
                        new AbstractMap.SimpleEntry<>(value, entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Override
    public void sendUpMessage(String vin, int appId, AcpRawMessage acpMessage, boolean isAckMsg) {
        try {
            String topic = appId2KafkaTopicCache.get(appId);
            if (Objects.isNull(topic)) {
                log.error("应答远程指令(APP_ID=[{}])对应的Kafka Topic为空!", appId);
                return;
            }

            acpMessage.setReceiveTime(System.currentTimeMillis());
            String acpMessageStr = JsonUtil.writeValueAsString(acpMessage);
            if (enableUpLog) {
                if (isAckMsg) {
                    log.info("应答远程指令: VIN码=[{}], 指令数据=[{}]", vin, acpMessageStr);
                } else if (isAckMessage(appId)) {
                    if (enableRawDataLog) {
                        log.info("发送上行消息: VIN码=[{}], APP_ID=[{}], CommandId=[{}], RawData=[{}]",
                                vin, appId, acpMessage.getVehicleDescription().getCommandId(),
                                BytesUtil.bytesToHexString(acpMessage.getPayload()));
                    } else {
                        log.info("发送上行消息: VIN码=[{}], APP_ID=[{}], CommandId=[{}]",
                                vin, appId, acpMessage.getVehicleDescription().getCommandId());
                    }
                } else {
                    if (enableRawDataLog) {
                        log.info("发送上行消息: VIN码=[{}], APP_ID=[{}], RawData=[{}]",
                                vin, appId, BytesUtil.bytesToHexString(acpMessage.getPayload()));
                    } else {
                        log.info("发送上行消息: VIN码=[{}], APP_ID=[{}]", vin, appId);
                    }
                }
            }
            kafkaTemplate.send(topic, vin, acpMessageStr);
        } catch (Exception e) {
            log.error("远程指令应答消息异常=[{}]", ExceptionUtil.getBriefStackTrace(e));
        }
    }

    @Override
    public void sendDownMessage(int appId, String serial, String vin, byte[] rawData) {
        if (enableDownLog) {
            if (enableRawDataLog) {
                log.info("发送下行消息: VIN码=[{}], APP_ID=[{}], RawData=[{}]", vin, appId, BytesUtil.bytesToHexString((rawData)));
            } else {
                log.info("发送下行消息: VIN码=[{}], APP_ID=[{}]", vin, appId);
            }
        }
        PubMsgDTO pubMsg = buildMqttPubMsgDto(appId, serial, vin, rawData);
        mqttxRemoteService.publishMessage(pubMsg, false);
    }

    /**
     * 构建mqttx消息发布PubMsg对象
     *
     * @param appId
     * @return
     */
    private PubMsgDTO buildMqttPubMsgDto(int appId, String serial, String vin, byte[] payload) {
        MqttTopicEntity mqttTopicEntity = selectMqttDownTopic(appId);
        if (Objects.isNull(mqttTopicEntity)) {
            return null;
        }
        PubMsgDTO pubMsgDTO = new PubMsgDTO();
        pubMsgDTO.setClientId(String.format(CommonConstants.TBOX_CLIENT_ID, vin));
        pubMsgDTO.setTopic(strFormatterTopic(vin, serial, mqttTopicEntity.getTopic()));
        pubMsgDTO.setQos(mqttTopicEntity.getQos());
        pubMsgDTO.setPayload(payload);
        return pubMsgDTO;
    }

    /**
     * 格式化系统Topic
     *
     * @param vin
     * @param tBoxSerial
     * @param topic
     * @return
     */
    private String strFormatterTopic(String vin, String tBoxSerial, String topic) {
        Map<String, String> hashMap = new HashMap<>(2);
        hashMap.put(CommonConstants.MQTT_TOPIC_FORMAT_VIN, vin);
        hashMap.put(CommonConstants.MQTT_TOPIC_FORMAT_SERIAL, tBoxSerial);
        return StrFormatter.format(topic, hashMap, true);
    }

    /**
     * 根据appId查询对应下行topic信息
     *
     * @param appId 协议标识
     * @return MQTT Topic 实体类
     */
    private MqttTopicEntity selectMqttDownTopic(int appId) {
        MqttTopicEntity mqttTopicEntity = appId2MqttTopicCache.getIfPresent(appId);
        if (Objects.nonNull(mqttTopicEntity)) {
            return mqttTopicEntity;
        }

        Query query = Query.query(
                Criteria.where(MqttTopicEntity.FIELD_APP_ID).is(appId)
                        .and(MqttTopicEntity.FIELD_DIRECTION).is(MqttTopicEntity.VALUE_DOWN_MSG));
        mqttTopicEntity = mongoTemplate.findOne(query, MqttTopicEntity.class);
        if (Objects.nonNull(mqttTopicEntity)) {
            appId2MqttTopicCache.put(appId, mqttTopicEntity);
            return mqttTopicEntity;
        }

        log.warn("配置中无APP_ID=[{}]对应的MQTT Topic信息!", appId);
        return null;
    }

    /**
     * 需要返回指令执行状态
     *
     * @param appId 协议标识
     * @return true/false
     */
    @Override
    public boolean isAckMessage(int appId) {
        return this.ackAppId.contains(appId);
    }
}
