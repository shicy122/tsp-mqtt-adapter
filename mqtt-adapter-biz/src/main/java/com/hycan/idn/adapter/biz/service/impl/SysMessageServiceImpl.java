package com.hycan.idn.adapter.biz.service.impl;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.CommonConstants;
import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import com.hycan.idn.adapter.biz.service.IMqttxRemoteService;
import com.hycan.idn.adapter.biz.service.ISysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shichongying
 * @datetime 2023年 02月 26日 14:23
 */
@Slf4j
@Service
public class SysMessageServiceImpl implements ISysMessageService {

    private static final int MAX_DISCONNECT_CLIENT_SIZE = 50;

    private static final int DEFAULT_OFFLINE_EXPIRE = 72 * 60 * 60 * 1000;

    private final RedisTemplate<String, Object> redisTemplate;

    private final IMqttxRemoteService mqttxRemoteService;

    private final boolean enableForceOffline;

    private static final String SEARCH_OFFLINE_VIN_LUA_SCRIPT =
            "local result = redis.call('ZRANGEBYSCORE', KEYS[1], 0, ARGV[1])\n" +
                    "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])\n" +
                    "return result";

    public SysMessageServiceImpl(RedisTemplate<String, Object> redisTemplate,
                                 IMqttxRemoteService mqttxRemoteService, AdapterConfig config) {
        this.redisTemplate = redisTemplate;
        this.mqttxRemoteService = mqttxRemoteService;

        this.enableForceOffline = config.getEnableForceOffline();
        batchOfflineClient();
    }

    @Scheduled(cron = "0 30 4 * * ?")
    public void batchOfflineClient() {
        RedisScript<List> script = RedisScript.of(SEARCH_OFFLINE_VIN_LUA_SCRIPT, List.class);
        String key = RedisKeyConstants.FORCE_OFFLINE_TIME;
        List offlineVinSet = redisTemplate.execute(script, Collections.singletonList(key), System.currentTimeMillis());
        if (CollectionUtils.isEmpty(offlineVinSet)) {
            return;
        }

        List<String> clientIds = new ArrayList<>();
        for (Object vin : offlineVinSet) {
            String clientId = String.format(CommonConstants.TBOX_CLIENT_ID, vin.toString());
            clientIds.add(clientId);
            if (clientIds.size() >= MAX_DISCONNECT_CLIENT_SIZE) {
                log.info("连续在线超过72小时客户端强制下线, 数量=[{}], 列表={}", clientIds.size(), clientIds);
                mqttxRemoteService.sendOfflineEvent(clientIds, CommonConstants.FORCE_OFFLINE);
                clientIds.clear();
            }
        }
        log.info("连续在线超过72小时客户端强制下线, 数量=[{}], 列表={}", clientIds.size(), clientIds);
        mqttxRemoteService.sendOfflineEvent(clientIds, CommonConstants.FORCE_OFFLINE);
    }

    /**
     * 设置默认下线时间，连续在线24小时需要强制下线
     */
    @Override
    public void setOfflineExpire(String vin) {
        if (enableForceOffline) {
            long offlineTime = System.currentTimeMillis() + DEFAULT_OFFLINE_EXPIRE;
            redisTemplate.opsForZSet().add(RedisKeyConstants.FORCE_OFFLINE_TIME, vin, offlineTime);
        }
    }

    @Override
    public void removeOfflineExpire(String vin) {
        redisTemplate.opsForZSet().remove(RedisKeyConstants.FORCE_OFFLINE_TIME, vin);
    }
}
