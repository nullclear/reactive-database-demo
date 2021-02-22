package dev.yxy.reactive.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static dev.yxy.reactive.util.LockUtil.DEFAULT_SECOND;
import static dev.yxy.reactive.util.LockUtil.LOCK_KEYS;

/**
 * Created by Nuclear on 2021/1/19
 */
@Component
public class ExpireHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExpireHandler.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DefaultRedisScript<Boolean> expireScript;

    //给分布式锁续命
    @Scheduled(fixedDelay = 60 * 1000, initialDelay = 5000)
    void expire() {
        Boolean aBoolean = redisTemplate.execute(expireScript, Collections.singletonList(LOCK_KEYS), DEFAULT_SECOND);
        logger.trace("延长redis锁寿命结果：{}", aBoolean);
    }
}
