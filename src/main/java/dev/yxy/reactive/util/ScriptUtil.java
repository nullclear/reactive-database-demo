package dev.yxy.reactive.util;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * Created by Nuclear on 2021/1/15
 */
@Component
public class ScriptUtil {

    @Bean(name = "unlockScript")
    public DefaultRedisScript<Boolean> unlockScript() {
        DefaultRedisScript<Boolean> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Boolean.class);
        defaultRedisScript.setLocation(new ClassPathResource("redis/unlock.lua"));
        return defaultRedisScript;
    }

    @Bean(name = "expireScript")
    public DefaultRedisScript<Boolean> expireScript() {
        DefaultRedisScript<Boolean> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Boolean.class);
        defaultRedisScript.setLocation(new ClassPathResource("redis/expire.lua"));
        return defaultRedisScript;
    }

    @Bean(name = "captchaScript")
    public DefaultRedisScript<String> captchaScript() {
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(String.class);
        defaultRedisScript.setLocation(new ClassPathResource("redis/captcha.lua"));
        return defaultRedisScript;
    }

    @Bean(name = "counterScript")
    public DefaultRedisScript<Long> counterScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setLocation(new ClassPathResource("redis/counter.lua"));
        return script;
    }
}
