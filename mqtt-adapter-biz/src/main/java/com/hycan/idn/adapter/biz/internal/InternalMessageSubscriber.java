package com.hycan.idn.adapter.biz.internal;

import com.hycan.idn.adapter.biz.pojo.dto.InternalMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 集群消息订阅分发处器
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 17:34
 */
@Slf4j
@Component
public class InternalMessageSubscriber extends AbstractInnerChannel {

    private final RedisTemplate<String, Object> redisTemplate;

    public InternalMessageSubscriber(List<Watcher> watchers, RedisTemplate<String, Object> redisTemplate) {
        super(watchers);
        this.redisTemplate = redisTemplate;
    }

    /**
     * 集群消息处理
     *
     * @param message 消息内容
     * @param channel 订阅频道
     */
    @SuppressWarnings("unchecked")
    public <T> void handleMessage(String message, String channel) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        Object obj = redisTemplate.getValueSerializer().deserialize(bytes);
        if (null == obj) {
            return;
        }

        dispatch((InternalMessageDTO<T>) obj, channel);
    }
}