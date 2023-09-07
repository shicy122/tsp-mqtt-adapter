package com.hycan.idn.adapter.biz;

import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MqttAdapterApplication.class)
public class RedisLuaTest {

    private static final String TEST_LUA_SCRIPT =
            "local result = redis.call('ZRANGEBYSCORE', KEYS[1], 0, ARGV[1]) \n" +
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1]) \n" +
            "return result";

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testLua() {
        String key = RedisKeyConstants.HEART_BEATER_TIMEOUT;

        redisTemplate.opsForZSet().add(key, "VIN123", 100);
        redisTemplate.opsForZSet().add(key, "VIN456", 200);
        redisTemplate.opsForZSet().add(key, "VIN789", 300);

        RedisScript<List> script =  RedisScript.of(TEST_LUA_SCRIPT, List.class);
        long max = Math.addExact(System.currentTimeMillis(), 500);
        List offlineVinSet = (List) redisTemplate.execute(script, Collections.singletonList(key), max);
        System.out.println(offlineVinSet);
    }
}