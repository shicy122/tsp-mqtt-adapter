package com.hycan.idn.adapter.biz.service.impl;

import cn.hutool.core.text.StrPool;
import cn.hutool.json.JSONUtil;
import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.CommonConstants;
import com.hycan.idn.adapter.biz.constant.ConnectStatusConstants;
import com.hycan.idn.adapter.biz.constant.KafkaTopicConstants;
import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import com.hycan.idn.adapter.biz.enums.InternalMessageEnum;
import com.hycan.idn.adapter.biz.pojo.dto.ConnectStatusDTO;
import com.hycan.idn.adapter.biz.pojo.dto.InternalMessageDTO;
import com.hycan.idn.adapter.biz.pojo.dto.VehicleStatusDTO;
import com.hycan.idn.adapter.biz.service.IConnectStatusService;
import com.hycan.idn.adapter.biz.service.IInternalMessageService;
import com.hycan.idn.adapter.biz.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 车辆状态 Service
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:12
 */
@Slf4j
@Service
public class ConnectStatusServiceImpl implements IConnectStatusService {

    private static final String HOST_ADDRESS = IpUtil.getHostIp();

    /** 缓存 VIN 对应的心跳周期 */
    private final ConcurrentHashMap<String, Integer> heartbeatPeriodMap = new ConcurrentHashMap<>(500);

    private final RedisTemplate<String, Object> redisTemplate;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final IInternalMessageService internalMsgService;

    private final boolean enableLog;

    public ConnectStatusServiceImpl(RedisTemplate<String, Object> redisTemplate,
                                    KafkaTemplate<String, String> kafkaTemplate,
                                    IInternalMessageService internalMsgService, AdapterConfig config) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.internalMsgService = internalMsgService;

        this.enableLog = config.getLog().getEnableConnectStatus();

        initHeartPeriodMap();
    }

    public void initHeartPeriodMap() {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(RedisKeyConstants.HEART_BEATER_PERIOD);
        if (!CollectionUtils.isEmpty(map)) {
            map.forEach((key, value) -> {
                String vin = key.toString();
                Integer heartbeatPeriod = Integer.parseInt(value.toString());
                heartbeatPeriodMap.put(vin, heartbeatPeriod);
            });
        }
    }

    /**
     * 设置车辆心跳周期
     * @param vin
     * @param heartbeatPeriod
     */
    @Override
    public void setConnectStatus(String vin, int heartbeatPeriod, boolean isClusterMessage) {
        heartbeatPeriodMap.put(vin, heartbeatPeriod);

        if (!isClusterMessage) {
            redisTemplate.opsForHash().put(RedisKeyConstants.HEART_BEATER_PERIOD, vin, heartbeatPeriod);

            // 发送集群消息
            internalMsgService.publish(
                    new InternalMessageDTO<>(
                            ConnectStatusDTO.of(vin, heartbeatPeriod), System.currentTimeMillis(), HOST_ADDRESS),
                    InternalMessageEnum.CONNECT_STATUS.getChannel());
        }
    }

    /**
     * 获取车辆心跳周期
     */
    @Override
    public int getConnectStatus(String vin, int tBoxStatus) {
        if (StringUtils.isEmpty(vin)) {
            return -1;
        }

        if (heartbeatPeriodMap.containsKey(vin)) {
            return heartbeatPeriodMap.get(vin);
        }

        Object obj = redisTemplate.opsForHash().get(RedisKeyConstants.HEART_BEATER_PERIOD, vin);
        if (Objects.isNull(obj)) {
            int period = getDefaultConnectStatus(tBoxStatus);
            setConnectStatus(vin, period, false);
            return period;
        }

        return Integer.parseInt(obj.toString());
    }

    /**
     * 向车控服务发送车辆在线状态
     *
     * @param vin
     * @param vinStatus 0--不在线 1--正常在线 2--睡眠
     */
    @Override
    public void sendVehicleStatus(String vin, int vinStatus, long sendTime) {
        String key = String.format(RedisKeyConstants.T_BOX_STATUS, vin.substring(vin.length() - 2));

        String vinStatusValue = vinStatus + "-" + sendTime;
        Object vinStatusObj = redisTemplate.opsForHash().get(key, vin);
        if (Objects.nonNull(vinStatusObj)) {
            String vinStatusCache = vinStatusObj.toString();
            String[] vehicleStatusArrays = vinStatusCache.split(StrPool.DASHED);
            if (vehicleStatusArrays.length > 1) {
                // Redis查询到的数据，解析得到的 上次消息发送时间 < 当前消息发送时间，更新车辆状态
                if (Long.parseLong(vehicleStatusArrays[1]) < sendTime) {
                    redisTemplate.opsForHash().put(key, vin, vinStatusValue);
                }
            } else {
                // Redis查询到的数据格式不正确时，更新车辆状态
                redisTemplate.opsForHash().put(key, vin, vinStatusValue);
            }
        } else {
            // Redis不存在指定车辆状态数据时，更新车辆状态
            redisTemplate.opsForHash().put(key, vin, vinStatusValue);
        }

        VehicleStatusDTO vehicleStatusDTO = VehicleStatusDTO.of(vin, vinStatus);

        if (enableLog) {
            log.info("发送车辆状态: VIN码=[{}], 状态=[{}](0:离线 1:在线 2:休眠)", vin, vinStatus);
        }

        kafkaTemplate.send(KafkaTopicConstants.VEHICLE_STATUS_TOPIC, JSONUtil.toJsonStr(vehicleStatusDTO));
    }

    /**
     * 获取默认心跳周期
     */
    private int getDefaultConnectStatus(int tBoxStatus) {
        int heartbeatPeriod;
        switch (tBoxStatus) {
            case ConnectStatusConstants.WORK_MODE :
                heartbeatPeriod = CommonConstants.WORK_MODEL_HEARTBEAT_PERIOD;
                break;
            case ConnectStatusConstants.HIBERNATE_MODE :
                heartbeatPeriod = CommonConstants.HIBERNATE_HEARTBEAT_PERIOD;
                break;
            default:
                return -1;
        }
        return heartbeatPeriod;
    }
}
