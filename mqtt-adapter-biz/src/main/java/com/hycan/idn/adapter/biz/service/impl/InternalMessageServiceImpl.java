package com.hycan.idn.adapter.biz.service.impl;

import com.hycan.idn.adapter.biz.pojo.dto.InternalMessageDTO;
import com.hycan.idn.adapter.biz.service.IInternalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于 Redis 的实现
 */
@Slf4j
@Service
public class InternalMessageServiceImpl implements IInternalMessageService {

    private final RedisTemplate<String, Object> redisTemplate;

    public InternalMessageServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> void publish(InternalMessageDTO<T> im, String channel) {
        redisTemplate.convertAndSend(channel, im);
    }
}
