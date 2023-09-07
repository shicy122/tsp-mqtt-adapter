package com.hycan.idn.adapter.biz.mqtt;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.CommonConstants;
import com.hycan.idn.adapter.biz.constant.EventTypeConstants;
import com.hycan.idn.adapter.biz.mqtt.event.publisher.SysMessageEventPublisher;
import com.hycan.idn.adapter.biz.mqtt.event.publisher.UpMessageEventPublisher;
import com.hycan.idn.adapter.biz.pojo.dto.MessageHeadDTO;
import com.hycan.idn.adapter.biz.pojo.dto.SysNoticeDTO;
import com.hycan.idn.adapter.biz.pojo.dto.VehicleInfoDTO;
import com.hycan.idn.adapter.biz.util.Crc32Util;
import com.hycan.idn.adapter.biz.util.DecodeUtil;
import com.hycan.idn.adapter.biz.util.MqttTopicUtil;
import com.hycan.idn.common.core.util.BytesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 订阅MQTT Broker消息处理器
 *
 * @author shichongying
 * @datetime 2023年 02月 27日 10:14
 */
@Slf4j
@Component
public class MqttMessageHandler {

    /** 连接状态 */
    private static final int APP_ID_CONNECT_STATUS = 0;
    /** 心跳检测 */
    private static final int APP_ID_HEART_BEAT = 1;
    /** 车辆国标登入通知 */
    private static final int APP_ID_GB_LOGIN = 16;
    /** 车辆国标登出通知 */
    private static final int APP_ID_GB_LOGOUT = 17;

    private static final Pattern HEX_DECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");

    private final UpMessageEventPublisher upMessageEventPublisher;

    private final SysMessageEventPublisher sysMessageEventPublisher;

    private final boolean isEnableBizBridge, isEnableSysBridge;

    public MqttMessageHandler(AdapterConfig adapterConfig,
                              UpMessageEventPublisher upMessageEventPublisher,
                              SysMessageEventPublisher sysMessageEventPublisher) {
        this.upMessageEventPublisher = upMessageEventPublisher;
        this.sysMessageEventPublisher = sysMessageEventPublisher;
        this.isEnableBizBridge = adapterConfig.getMqttx().getEnableBizBridge();
        this.isEnableSysBridge = adapterConfig.getMqttx().getEnableSysBridge();
    }

    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        byte[] data = (byte[]) message.getPayload();

        if (!StringUtils.hasText(topic) || data.length <= 0) {
            log.error("MQTT消息格式错误! Topic=[{}], Payload=[{}]", topic, data);
            return;
        }

        // 通过MQTT Broker桥接过来的消息, 由Kafka消费者处理, 系统主题-上下线消息
        if (MqttTopicUtil.isSysNotice(topic)) {
            if (Boolean.FALSE.equals(isEnableSysBridge)) {
                handleSysMessage(topic);
            }
            // 通过MQTT Broker桥接过来的消息, 由Kafka消费者处理
        } else if (Boolean.FALSE.equals(isEnableBizBridge) || !MqttTopicUtil.isBridgeMsg(topic)) {
            // tBox上报数据转发到协议引擎
            handleBizMessage(topic, BytesUtil.bytesToHexString(data));
        }
    }

    /**
     * 系统主题-MQTT客户端上下线逻辑
     *
     * @param topic
     */
    private void handleSysMessage(String topic) {
        SysNoticeDTO sysNotice = MqttTopicUtil.getSysNotice(topic);
        if (Objects.isNull(sysNotice)) {
            return;
        }

        String clientId = sysNotice.getClientId();
        if (!clientId.startsWith(CommonConstants.TBOX_CLIENT_ID_PREFIX)) {
            return;
        }

        sysMessageEventPublisher.publish(sysNotice.getType(), clientId);
    }

    /**
     * 处理TBOX上报的业务数据，校验通过后，通过发布事件方式，由对应Listener监听指定APP_ID处理
     *
     * @param topic 上报业务数据的主题，以 up/tbox/... 开头
     * @param payload  16进制报文数据
     */
    public void handleBizMessage(String topic, String payload) {
        // 2 Topic校验, 从Topic中获取车系、车架号
        VehicleInfoDTO vehicle = MqttTopicUtil.vehicleInfo(topic);
        if (Objects.isNull(vehicle)) {
            log.error("Topic格式错误, Topic=[{}]", topic);
            return;
        }

        // 3 校验是否为16进制
        if (!HEX_DECIMAL_PATTERN.matcher(payload).matches()) {
            log.error("数据报文格式错误, Payload=[{}]", payload);
            return;
        }

        // 4 CRC32校验
        byte[] payloadHex = BytesUtil.toStringHex(payload);
        if (!Crc32Util.checkPayload(payloadHex)) {
            log.error("CRC32校验失败, 原始报文=[{}]", payload);
            return;
        }

        MessageHeadDTO header = DecodeUtil.header(payloadHex);

        // 5 发布上行消息事件
        String eventType = convertEventType(header.getAppId());
        int appId = header.getAppId();
        String serial = vehicle.getSerial();
        String vin = vehicle.getVin();
        upMessageEventPublisher.publish(eventType, appId, serial, vin, payloadHex);
    }

    private String convertEventType(int appId) {
        switch (appId) {
            case APP_ID_CONNECT_STATUS:
                return EventTypeConstants.CONNECT_STATUS;
            case APP_ID_HEART_BEAT:
                return EventTypeConstants.HEARTBEAT;
            case APP_ID_GB_LOGIN:
            case APP_ID_GB_LOGOUT:
                return EventTypeConstants.GB_LOG_IO;
            default:
                return EventTypeConstants.BIZ_MSG;
        }
    }
}
