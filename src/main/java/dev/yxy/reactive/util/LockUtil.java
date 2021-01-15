package dev.yxy.reactive.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Component
public class LockUtil {
    private static final Logger logger = LoggerFactory.getLogger(LockUtil.class);

    //本机地址
    private static final String address;
    //默认加锁时间
    private static final long DEFAULT_SECOND = 60;
    //超时时间，单位ms
    private static final long TIME_OUT = 50;

    static {
        String addr;
        try {
            addr = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            addr = "unknown/" + UUID.randomUUID().hashCode();
        }
        address = addr;
        logger.info("本机地址: [ {} ]", address);
    }

    @NotNull
    private static String unique() {
        return address + "/" + Thread.currentThread().getId();
    }

    //线程持有锁的标志
    private static final ThreadLocal<String> local = new ThreadLocal<>();
    //唯一的value
    private static final ThreadLocal<String> value = ThreadLocal.withInitial(LockUtil::unique);
    //重入计数器
    private static final ThreadLocal<Integer> count = ThreadLocal.withInitial(() -> 0);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DefaultRedisScript<Boolean> unlockScript;

    /**
     * 分布式重入互斥锁
     *
     * @param key 互斥key
     * @return 是否获取成功
     */
    public boolean lock(@NotNull String key) {
        return lock(key, value.get(), DEFAULT_SECOND);
    }

    /**
     * 分布式重入互斥锁
     *
     * @param key     互斥key
     * @param seconds 持有时间
     * @return 是否获取成功
     */
    public boolean lock(@NotNull String key, long seconds) {
        return lock(key, value.get(), seconds);
    }

    /**
     * 分布式重入互斥锁
     *
     * @param key     互斥key
     * @param value   分布式唯一值
     * @param seconds 持有时间
     * @return 是否获取成功
     */
    private boolean lock(@NotNull String key, @NotNull String value, long seconds) {
        if (Objects.equals(local.get(), key)) {//判断本线程是否已经持有此key
            count.set(count.get() + 1);//计数器加一
            redisTemplate.expire(key, Duration.ofSeconds(seconds)); //延长持有时间
            logger.trace("[{}]对锁[{}]计数器增加结果[{}]", value, key, count.get());
            return true;
        } else if (Objects.equals(local.get(), null)) {//如果是未持有的key，则需要去抢占
            if (Objects.equals(redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(seconds)), true)) {
                local.set(key);//设置线程持有key
                count.set(0);//重置计数器
                logger.trace("[{}]加锁[{}]成功", value, key);
                return true;
            } else {
                logger.trace("[{}]加锁[{}]失败", value, key);
                return false;
            }
        } else {
            logger.warn("raw key [{}] your key [{}]", local.get(), key);
            return false;
        }
    }

    /**
     * 分布式重入互斥锁，加入多次尝试与超时功能
     *
     * @param key     互斥key
     * @param value   分布式唯一值
     * @param seconds 持有时间
     * @return 是否获取成功
     */
    @Deprecated
    private boolean lock_timeout(@NotNull String key, @NotNull String value, long seconds) {
        long start = System.currentTimeMillis();
        if (Objects.equals(local.get(), key)) {//判断本线程是否已经持有此key
            count.set(count.get() + 1);//计数器加一
            redisTemplate.expire(key, Duration.ofSeconds(seconds)); //延长持有时间
            logger.trace("[{}]对锁[{}]计数器增加结果[{}]", value, key, count.get());
            return true;
        } else if (Objects.equals(local.get(), null)) {//如果是未持有的key，则需要去抢占
            for (; ; ) {
                if (Objects.equals(redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(seconds)), true)) {
                    local.set(key);//设置线程持有key
                    count.set(0);//重置计数器
                    logger.trace("[{}]加锁[{}]成功", value, key);
                    return true;
                }
                if (System.currentTimeMillis() - start >= TIME_OUT) {
                    logger.trace("[{}]加锁[{}]失败", value, key);
                    return false;
                }
                try {
                    // 休眠一会，不然反复执行循环会一直失败
                    Thread.sleep(TIME_OUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.warn("raw key [{}] your key [{}]", local.get(), key);
            return false;
        }
    }

    /**
     * 分布式重入互斥锁
     *
     * @param key 互斥key
     */
    public void unlock(@NotNull String key) {
        if (Objects.equals(local.get(), key)) {//如果当前线程未持有此key则不需要解锁
            if (count.get() > 0) { //解锁重入锁要先对计数器做减数操作
                count.set(count.get() - 1);
                logger.trace("[{}]对锁[{}]计数器减少结果[{}]", value.get(), key, count.get());
            } else {
                //检查并删除，原子操作
                boolean result = checkAndDelete(key, value.get());
                if (result) {
                    logger.info("[{}]删除锁[{}]成功", value.get(), key);
                } else {
                    logger.warn("[{}]删除锁[{}]失败", value.get(), key);
                }
                local.remove();//不管redis是否能删除key，当前线程都不应该再持有key
                logger.trace("[{}]解锁[{}]成功", value.get(), key);
            }
        } else {
            logger.warn("[{}]未持有锁[{}]", value.get(), key);
        }
    }

    /**
     * 检查并删除，原子操作
     *
     * @param key   互斥key
     * @param value 分布式唯一值
     * @return 是否删除成功
     */
    private boolean checkAndDelete(String key, String value) {
        Boolean result = redisTemplate.execute(unlockScript, Collections.singletonList(key), value);
        return Objects.requireNonNullElse(result, false);
    }

    /*----------------------分布式锁变量区--------------------------------*/

    //锁变量
    public static final String DEVICE_LOCK = "DEVICE-LOCK";
    public static final String REMOTE_LOCK = "REMOTE-LOCK";

    @NotNull
    public static String deviceKey(@NotNull String id) {
        return DEVICE_LOCK + ":" + id;
    }

    @NotNull
    public static String remoteKey(@NotNull String id) {
        return REMOTE_LOCK + ":" + id;
    }
}
