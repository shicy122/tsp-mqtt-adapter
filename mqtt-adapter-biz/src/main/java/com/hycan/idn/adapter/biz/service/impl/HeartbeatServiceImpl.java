package com.hycan.idn.adapter.biz.service.impl;

import com.hycan.idn.adapter.biz.constant.CommonConstants;
import com.hycan.idn.adapter.biz.constant.ConnectStatusConstants;
import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import com.hycan.idn.adapter.biz.service.IHeartbeatService;
import com.hycan.idn.adapter.biz.service.IMqttxRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author shichongying
 * @datetime 2023年 02月 26日 14:23
 */
@Slf4j
@Service
public class HeartbeatServiceImpl implements IHeartbeatService {

    private static final int MAX_DISCONNECT_CLIENT_SIZE = 50;

    private final RedisTemplate<String, Object> redisTemplate;

    private final IMqttxRemoteService mqttxRemoteService;

    private static final String SEARCH_OFFLINE_VIN_LUA_SCRIPT =
            "local result = redis.call('ZRANGEBYSCORE', KEYS[1], 0, ARGV[1])\n" +
                    "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])\n" +
                    "return result";

    public HeartbeatServiceImpl(RedisTemplate<String, Object> redisTemplate, IMqttxRemoteService mqttxRemoteService) {
        this.redisTemplate = redisTemplate;
        this.mqttxRemoteService = mqttxRemoteService;
    }

    @Scheduled(initialDelay = 5, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void batchOfflineClient() {
        RedisScript<List> script = RedisScript.of(SEARCH_OFFLINE_VIN_LUA_SCRIPT, List.class);
        String key = RedisKeyConstants.HEART_BEATER_TIMEOUT;
        long currentScore = Math.addExact(System.currentTimeMillis(), 500);
        List offlineVinSet = redisTemplate.execute(script, Collections.singletonList(key), currentScore);
        if (CollectionUtils.isEmpty(offlineVinSet)) {
            return;
        }

        List<String> clientIds = new ArrayList<>();
        for (Object vin : offlineVinSet) {
            String clientId = String.format(CommonConstants.TBOX_CLIENT_ID, vin.toString());

            clientIds.add(clientId);
            if (clientIds.size() >= MAX_DISCONNECT_CLIENT_SIZE) {
                log.info("心跳超时客户端强制下线, 数量=[{}], 列表={}", clientIds.size(), clientIds);
                mqttxRemoteService.sendOfflineEvent(clientIds, CommonConstants.HEARTBEAT);
                clientIds.clear();
            }
        }
        log.info("心跳超时客户端强制下线, 数量=[{}], 列表={}", clientIds.size(), clientIds);
        mqttxRemoteService.sendOfflineEvent(clientIds, CommonConstants.HEARTBEAT);
    }

    /**
     * 更新心跳过期时间
     */
    @Override
    public void setHeartbeatExpire(String vin, int tBoxStatus, int heartbeatPeriod) {
        long heartbeatExpire = calculateHeartbeat(tBoxStatus, heartbeatPeriod);
        redisTemplate.opsForZSet().add(RedisKeyConstants.HEART_BEATER_TIMEOUT, vin, heartbeatExpire);
    }

    /**
     * 设置默认心跳过期时间
     *
     * @param vin        VIN码
     * @param bBoxStatus TBOX状态
     */
    @Override
    public void setDefaultHeartbeatExpire(String vin, int bBoxStatus) {
        int heartbeatPeriod;
        switch (bBoxStatus) {
            case ConnectStatusConstants.WORK_MODE:
                heartbeatPeriod = CommonConstants.WORK_MODEL_HEARTBEAT_PERIOD;
                break;
            case ConnectStatusConstants.HIBERNATE_MODE:
                heartbeatPeriod = CommonConstants.HIBERNATE_HEARTBEAT_PERIOD;
                break;
            default:
                heartbeatPeriod = 0;
        }
        setHeartbeatExpire(vin, bBoxStatus, heartbeatPeriod);
    }

    /**
     * 删除心跳
     *
     * @param vin VIN码
     */
    @Override
    public void removeHeartbeatExpire(String vin) {
        redisTemplate.opsForZSet().remove(RedisKeyConstants.HEART_BEATER_TIMEOUT, vin);
    }

    /**
     * 根据TBOX状态计算心跳过期时间
     *
     * @param tBoxStatus
     * @param heartbeatPeriod
     * @return
     */
    private long calculateHeartbeat(int tBoxStatus, long heartbeatPeriod) {
        long heartbeatExpire;
        switch (tBoxStatus) {
            case ConnectStatusConstants.WORK_MODE:
                heartbeatExpire = heartbeatPeriod * 3000L;
                break;
            case ConnectStatusConstants.HIBERNATE_MODE:
                heartbeatExpire = heartbeatPeriod * 2000L;
                break;
            default:
                return -1L;
        }

        return Math.addExact(heartbeatExpire, System.currentTimeMillis());
    }
}
