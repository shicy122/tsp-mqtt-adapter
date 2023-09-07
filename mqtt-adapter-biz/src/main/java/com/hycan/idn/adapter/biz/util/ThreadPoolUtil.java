package com.hycan.idn.adapter.biz.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具类
 *
 * @author shichongying
 * @datetime 2023年 02月 24日 15:48
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "adapter.thread")
public class ThreadPoolUtil {

    public static final int DEFAULT_CAPACITY = 2000;

    public static final String UP_MSG_THREAD = "adapter_mqtt_up_msg";
    public static final String DOWN_MSG_THREAD = "adapter_mqtt_down_msg";
    public static final String OTHER_MSG_THREAD = "adapter_mqtt_other_msg";
    public static final String SYS_MSG_THREAD = "adapter_mqtt_sys_msg";

    private static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = (t, e) ->
            log.error("thread {} encounter a uncaught exception: {}", t.getName(), e.toString());

    @Bean(name = UP_MSG_THREAD)
    public Executor mqttUpMsg() {
        return newThreadPoolExecutor(4, 8, UP_MSG_THREAD, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(name = DOWN_MSG_THREAD)
    public Executor mqttDownMsg() {
        return newThreadPoolExecutor(4, 8, DOWN_MSG_THREAD, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(name = OTHER_MSG_THREAD)
    public Executor mqttOtherMsg() {
        return newThreadPoolExecutor(2, 4, OTHER_MSG_THREAD, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(name = SYS_MSG_THREAD)
    public Executor mqttSysMsg() {
        return newThreadPoolExecutor(1, 2, SYS_MSG_THREAD, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ExecutorService newThreadPoolExecutor(int corePoolSize,
                                                        int maxPoolSize,
                                                        String threadName,
                                                        RejectedExecutionHandler handler) {
        return newThreadPoolExecutor(corePoolSize, maxPoolSize, DEFAULT_CAPACITY, threadName, handler);
    }

    public static ExecutorService newThreadPoolExecutor(int corePoolSize, int maxPoolSize, int capacity,
                                                        String threadName, RejectedExecutionHandler handler) {
        ThreadFactory factory = createThreadFactory(threadName);
        if (corePoolSize < 0) {
            corePoolSize = 1;
        }

        if (maxPoolSize < 0) {
            maxPoolSize = 1;
        }

        if (capacity < 0) {
            capacity = DEFAULT_CAPACITY;
        }

        if (Objects.isNull(handler)) {
            handler = new ThreadPoolExecutor.CallerRunsPolicy();
        }

        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(capacity), factory, handler);
    }

    public static ScheduledExecutorService newFixedScheduledExecutorService(int poolSize, String threadName) {
        ThreadFactory factory = Executors.defaultThreadFactory();
        if (!ObjectUtils.isEmpty(threadName)) {
            factory = createThreadFactory(threadName);
        }
        if (poolSize < 0) {
            poolSize = 1;
        }
        return new ScheduledThreadPoolExecutor(poolSize, factory);
    }

    /**
     * 创建一个线程工程，产生的线程格式为：Thread-threadName-1, Thread-threadName-2
     *
     * @param threadName 线程名
     * @return 线程工程，用于线程池创建可读的线程
     */
    private static ThreadFactory createThreadFactory(String threadName) {
        return new ThreadFactoryBuilder()
                .setNameFormat("Thread-" + threadName + "-%d")
                .setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER)
                .setDaemon(false)
                .build();
    }
}
