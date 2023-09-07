package com.hycan.idn.adapter.biz.mqtt.event.listener;

import cn.hutool.core.text.StrPool;
import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import com.hycan.idn.adapter.biz.mqtt.event.UpMessageEvent;
import com.hycan.idn.adapter.biz.service.IMqttBizMessageService;
import com.hycan.idn.adapter.biz.util.ThreadPoolUtil;
import com.hycan.idn.common.core.util.BytesUtil;
import com.hycan.idn.tsp.engine.command.AcpMessageHeader;
import com.hycan.idn.tsp.engine.command.AcpRawMessage;
import com.hycan.idn.tsp.engine.command.acp.entity.VehicleDescription;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * TBOX上行消息监听器（除心跳、连接状态、国标登入/登出）
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:01
 */
@Slf4j
@Component
public class BizMessageListener {

    private final StringRedisTemplate redisTemplate;

    private final IMqttBizMessageService messageService;

    public BizMessageListener(StringRedisTemplate redisTemplate,
                              IMqttBizMessageService messageService) {
        this.redisTemplate = redisTemplate;
        this.messageService = messageService;
    }

    @Async(ThreadPoolUtil.UP_MSG_THREAD)
    @EventListener(condition = "T(com.hycan.idn.adapter.biz.constant.EventTypeConstants).BIZ_MSG.equals(#event.type)")
    public void handleEvent(UpMessageEvent event) {
        int appId = event.getAppId();

        String vin = event.getVin();
        byte[] payload = event.getPayload();

        VehicleDescription vehicleDescription = buildVehicleDescription(appId, vin, payload);
        if (Objects.isNull(vehicleDescription)) {
            return;
        }
        AcpRawMessage acpMessage = buildAcpRawMessage(appId, payload, vehicleDescription);
        messageService.sendUpMessage(vin, appId, acpMessage, false);
    }

    /**
     * 组装返回ack的VehicleDescription对象
     *
     * @param appId   协议标识
     * @param vin     VIN码
     * @param payload 协议数据
     * @return 组装完整的上行消息
     */
    public VehicleDescription buildVehicleDescription(int appId, String vin, byte[] payload) {
        VehicleDescription vehicleDescription = new VehicleDescription();
        vehicleDescription.setVin(vin);
        if (messageService.isAckMessage(appId)) {
            Long commandId = getCommandId(vin, appId, payload);
            if (null == commandId) {
                log.error("指令ID为空, VIN码=[{}], APP_ID=[{}], Payload=[{}]", vin, appId, BytesUtil.bytesToHexString(payload));
                return null;
            }
            vehicleDescription.setCommandId(commandId);
        }
        return vehicleDescription;
    }

    /**
     * 根据命理序号获取指令ID
     *
     * @param vin     VIN码
     * @param appId   协议标识
     * @param payload 协议数据
     * @return 指令序号SeqId
     */
    private Long getCommandId(String vin, int appId, byte[] payload) {
        int seqId = BytesUtil.parseBytesToShort(BytesUtil.getWord(13, payload));
        String key = RedisKeyConstants.MQTT_COMMAND + vin + StrPool.UNDERLINE + appId + StrPool.UNDERLINE + seqId;
        String commandId = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(commandId)) {
            return null;
        }

        return Long.parseLong(commandId);
    }

    /**
     * 组装的上行报文
     *
     * @param appId              协议标识
     * @param payload            协议数据
     * @param vehicleDescription
     * @return 上行报文
     */
    private AcpRawMessage buildAcpRawMessage(int appId, byte[] payload, VehicleDescription vehicleDescription) {
        AcpRawMessage acpMessage = new AcpRawMessage();
        acpMessage.setVehicleDescription(vehicleDescription);
        AcpMessageHeader acpMessageHeader = new AcpMessageHeader();
        acpMessageHeader.setApplicationID(appId);
        acpMessage.setHeader(acpMessageHeader);
        acpMessage.setPayload(payload);
        return acpMessage;
    }
}
