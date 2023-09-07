package com.hycan.idn.adapter.biz.config;

import com.hycan.idn.adapter.biz.internal.InternalMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;

import static com.hycan.idn.adapter.biz.enums.InternalMessageEnum.CONNECT_STATUS;

/**
 * Redis 配置
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
@Configuration
public class RedisConfig {

    /**
     * 消息监听者容器
     *
     * @param factory         {@link RedisConnectionFactory}
     * @param listenerAdapter {@link MessageListenerAdapter}
     */
    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory,
                                                           MessageListenerAdapter listenerAdapter,
                                                           ThreadPoolTaskExecutor springSessionRedisTaskExecutor) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.setTaskExecutor(springSessionRedisTaskExecutor);

        List<ChannelTopic> channelTopics = new ArrayList<>();
        channelTopics.add(ChannelTopic.of(CONNECT_STATUS.getChannel()));
        container.addMessageListener(listenerAdapter, channelTopics);

        return container;
    }

    /**
     * 默认回调 {@link InternalMessageSubscriber#handleMessage(String, String)} 方法
     *
     * @param internalMessageSubscriber 集群消息订阅者
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(InternalMessageSubscriber internalMessageSubscriber) {
        return new MessageListenerAdapter(internalMessageSubscriber);
    }

    /**
     * spring session 监听线程池化，防止每次创建一个线程
     */
    @Bean
    public ThreadPoolTaskExecutor springSessionRedisTaskExecutor() {
        ThreadPoolTaskExecutor springSessionRedisTaskExecutor = new ThreadPoolTaskExecutor();
        springSessionRedisTaskExecutor.setCorePoolSize(4);
        springSessionRedisTaskExecutor.setMaxPoolSize(8);
        springSessionRedisTaskExecutor.setKeepAliveSeconds(10);
        springSessionRedisTaskExecutor.setQueueCapacity(2000);
        springSessionRedisTaskExecutor.setThreadNamePrefix("redis_internal_listener");
        return springSessionRedisTaskExecutor;
    }
}
