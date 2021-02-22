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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 锁的粒度不够细
 */
@Component
public class LockUtil {
    private static final Logger logger = LoggerFactory.getLogger(LockUtil.class);

    //本机地址
    private static final String address;
    //默认加锁时间
    public static final long DEFAULT_SECOND = 90;
    //单次自旋时间，单位ms
    private static final long TIME_OUT = 500;
    //再尝试次数
    private static final long NUM = 3;

    //锁Key的集合
    public static final String LOCK_KEYS = "LOCK_KEYS";

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
                redisTemplate.opsForSet().add(LOCK_KEYS, key);//加入锁Key的集合
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
     * 分布式重入锁(阻塞，超时返回，多次尝试)
     *
     * @param key 互斥key
     * @return 是否获取成功
     */
    @Deprecated
    public boolean lockTimeOut(@NotNull String key) {
        return lockTimeOut(key, value.get(), DEFAULT_SECOND, TIME_OUT, NUM);
    }

    /**
     * 分布式重入锁(阻塞，超时返回，多次尝试)
     *
     * @param key     互斥key
     * @param timeOut 单次自旋时间(ms)
     * @param num     再尝试次数
     * @return 是否获取成功
     */
    @Deprecated
    public boolean lockTimeOut(@NotNull String key, long timeOut, long num) {
        return lockTimeOut(key, value.get(), DEFAULT_SECOND, timeOut, num);
    }

    /**
     * 分布式重入锁(阻塞，超时返回，多次尝试)
     *
     * @param key     互斥key
     * @param value   分布式唯一值
     * @param seconds 持有时间
     * @param timeOut 单次自旋时间(ms)
     * @param num     再尝试次数
     * @return 是否获取成功
     */
    @Deprecated
    private boolean lockTimeOut(@NotNull String key, @NotNull String value, long seconds, long timeOut, long num) {
        long times = 0L;
        if (Objects.equals(local.get(), key)) {//判断本线程是否已经持有此key
            count.set(count.get() + 1);//计数器加一
            redisTemplate.expire(key, Duration.ofSeconds(seconds)); //延长持有时间
            logger.trace("[{}]对锁[{}]计数器增加结果[{}]", value, key, count.get());
            return true;
        } else if (Objects.equals(local.get(), null)) {//如果是未持有的key，则需要去抢占
            for (; ; ) {
                // 尝试加锁
                if (Objects.equals(redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(seconds)), true)) {
                    local.set(key);//设置线程持有key
                    count.set(0);//重置计数器
                    redisTemplate.opsForSet().add(LOCK_KEYS, key);//加入锁Key的集合
                    logger.trace("[{}]加锁[{}]成功", value, key);
                    return true;
                }
                // 如果机会耗尽，则停止尝试
                if (times >= num) {
                    logger.trace("[{}]加锁[{}]失败", value, key);
                    return false;
                }
                // 自旋一会，不然反复执行循环会一直失败
                long threshold = System.currentTimeMillis() + timeOut;
                while (System.currentTimeMillis() < threshold) ;
                times++;
            }
        } else {
            logger.warn("raw key [{}] your key [{}]", local.get(), key);
            return false;
        }
    }

    /**
     * 分布式重入锁解锁
     *
     * @param key 互斥key
     */
    public void unlock(@NotNull String key) {
        //如果当前线程未持有此key则不需要解锁
        if (Objects.equals(local.get(), key)) {
            unlock();
        } else {
            logger.warn("[{}]未持有锁[{}]", value.get(), key);
        }
    }

    /**
     * 分布式重入锁解锁
     */
    public void unlock() {
        String key = local.get();
        // 解锁重入锁要先对计数器做减数操作
        if (count.get() > 0) {
            count.set(count.get() - 1);
            logger.trace("[{}]对锁[{}]计数器减少结果[{}]", value.get(), key, count.get());
        } else {
            // 检查并删除，原子操作
            boolean result = checkAndDelete(key, value.get());
            if (result) {
                logger.info("[{}]删除锁[{}]成功", value.get(), key);
            } else {
                // todo 如果删除失败了，应该怎么办呢？
                logger.warn("[{}]删除锁[{}]失败", value.get(), key);
            }
            // 不管redis是否能删除key，当前线程都不应该再持有key
            local.remove();
            logger.trace("[{}]解锁[{}]成功", value.get(), key);
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
        Boolean result = redisTemplate.execute(unlockScript, List.of(key, LOCK_KEYS), value);
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
