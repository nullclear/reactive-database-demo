package dev.yxy.reactive.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import dev.yxy.reactive.property.RedisProperty;
import dev.yxy.reactive.util.CustomRedisSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.SpringSessionRedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.session.data.redis.config.annotation.web.server.RedisWebSessionConfiguration;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * spring data redis默认集成了Reactive Redis和Redis<br/>
 * 如果我们没有自己设置template的Bean，spring就会自动替我们生成<br/>
 * ----------------------------------------<br/>
 * 通过看spring session的配置类源码可以发现<br/>
 * spring session初始化连接工厂时先查找带有注解<b>@SpringSessionRedisConnectionFactory</b>的连接工厂，<br/>
 * 如果没有找到，才会使用通用的redis连接工厂，即这里的<b>LettuceConnectionFactory</b>。<br/>
 * spring session使用的RedisTemplate是调用以上初始化后的连接工厂额外生成的，<br/>
 * 序列化器如果没有注入就会默认生成一个JDK的序列化器。<br/>
 * <br/>
 * mvc的spring session配置见{@link RedisHttpSessionConfiguration}及其子类<b>RedisSessionConfiguration</b><br/>
 * webflux的spring session配置见{@link RedisWebSessionConfiguration}及其子类<b>RedisReactiveSessionConfiguration</b><br/>
 * <br/>
 * LettuceConnectionFactory属于通用配置, 适用于以下类<br/>
 * {@link RedisReactiveAutoConfiguration}<br/>
 * {@link RedisAutoConfiguration}<br/>
 * {@link CacheAutoConfiguration}<br/>
 * <br/>
 */
@SuppressWarnings("NullableProblems")
@Configuration
@EnableCaching//开启Redis Cache
@EnableRedisWebSession(maxInactiveIntervalInSeconds = RedisConfig.SESSION_TTL)//开启spring session
public class RedisConfig extends CachingConfigurerSupport {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    public static final int SESSION_TTL = 60 * 60 * 8;

    @Autowired
    private RedisProperty redis;

    //lettuce连接工厂
    private LettuceConnectionFactory createLettuceConnectionFactory(int database) {
        //redis Standalone配置
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redis.getHost(), redis.getPort());
        standaloneConfig.setDatabase(database);
        standaloneConfig.setPassword(redis.getPassword());

        //redis cluster配置
        /*RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(redis.getCluster().getNodes());
        clusterConfig.setMaxRedirects(redis.getCluster().getMaxRedirects());
        clusterConfig.setPassword(redis.getPassword());*/

        //连接池配置
        GenericObjectPoolConfig<RedisConfig> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(redis.getLettuce().getPool().getMaxIdle());
        poolConfig.setMinIdle(redis.getLettuce().getPool().getMinIdle());
        poolConfig.setMaxTotal(redis.getLettuce().getPool().getMaxActive());
        poolConfig.setMaxWaitMillis(redis.getLettuce().getPool().getMaxWait().toMillis());

        //lettuce客户端连接池配置
        LettucePoolingClientConfiguration lettuceConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(redis.getTimeout())
                .shutdownTimeout(redis.getLettuce().getShutdownTimeout())
                .clientName(redis.getClientName())
                .poolConfig(poolConfig)
                .build();

        /*非连接池配置
        LettuceClientConfiguration lettuceConfig = LettuceClientConfiguration.builder()
                .commandTimeout(redis.getTimeout())
                .shutdownTimeout(redis.getLettuce().getShutdownTimeout()).build();*/

        //lettuce连接工厂
        //lettuceConnectionFactory.afterPropertiesSet();
        return new LettuceConnectionFactory(standaloneConfig, lettuceConfig);
    }

    //redis和reactive redis使用的数据库
    @Bean(name = {"lettuceConnectionFactory"})
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return createLettuceConnectionFactory(redis.getDatabase());
    }

    @Bean("redisTemplate")
    @Primary
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(CustomRedisSerializer.string());//键
        template.setValueSerializer(CustomRedisSerializer.json());//值
        template.setHashKeySerializer(CustomRedisSerializer.string());//value中hash的key
        template.setHashValueSerializer(CustomRedisSerializer.json());//value中hash的value
        template.setStringSerializer(CustomRedisSerializer.string());//value是String
        return template;
    }

    @Bean("stringRedisTemplate")
    @Primary
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(CustomRedisSerializer.string());//键
        template.setValueSerializer(CustomRedisSerializer.string());//值
        template.setHashKeySerializer(CustomRedisSerializer.string());//value中hash的key
        template.setHashValueSerializer(CustomRedisSerializer.string());//value中hash的value
        template.setStringSerializer(CustomRedisSerializer.string());//value是String
        return template;
    }

    @Bean("reactiveRedisTemplate")
    @Primary
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext.
                <String, Object>newSerializationContext(CustomRedisSerializer.string())
                .value(CustomRedisSerializer.json())
                .hashValue(CustomRedisSerializer.json())
                .string(CustomRedisSerializer.string())
                .build();
        return new ReactiveRedisTemplate<>(lettuceConnectionFactory, serializationContext);
    }

    @Bean("reactiveStringRedisTemplate")
    @Primary
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext.
                <String, String>newSerializationContext(CustomRedisSerializer.string())
                .value(CustomRedisSerializer.string())
                .hashValue(CustomRedisSerializer.string())
                .string(CustomRedisSerializer.string())
                .build();
        return new ReactiveStringRedisTemplate(lettuceConnectionFactory, serializationContext);
    }

    //springSession使用的数据库连接工厂
    @SpringSessionRedisConnectionFactory
    @Bean(name = "springSessionRedisConnectionFactory")
    public LettuceConnectionFactory springSessionRedisConnectionFactory() {
        return createLettuceConnectionFactory(redis.getSessionDatabase());
    }

    //spring session序列化器
    //注意: 如果与spring security结合，请去除此Bean，会导致无法反序列化
    //可能有解决的办法，但是目前为止，不知道
    //@Bean(name = {"springSessionDefaultRedisSerializer"})
    public RedisSerializer<Object> crateRedisSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        //设置可见性
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        //设置激活默认类型
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
        //设置日期格式化
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //设置空值不报错
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //设置未知属性不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //设置序列时排除null
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //设置地区
        mapper.setLocale(Locale.CHINA);
        //设置时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        //设置允许单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //设置char数组转为json数组
        mapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    //Redis Cache Manager
    @Bean(name = "cacheManager")
    @Primary
    public CacheManager createCacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(lettuceConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(redis.getCacheTtl()))
                .transactionAware()//Enable synchronize cache put/evict
                .build();
    }

    //key生成器
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder builder = new StringBuilder();
            builder.append(target.getClass().getSimpleName()).append(".").append(method.getName()).append(":");
            for (int i = 0; i < params.length - 1; i++) {
                builder.append(params[i]).append("-");
            }
            builder.append(params[params.length - 1]);
            return builder.toString();
        };
    }

    //错误处理
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        //异常处理，当Redis发生异常时，打印日志，但是程序正常走
        logger.info("初始化 -> [{}]", "Redis CacheErrorHandler");
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logger.error("Redis occur handleCacheGetError：key -> [{}]", key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logger.error("Redis occur handleCachePutError：key -> [{}] value -> [{}]", key, value, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logger.error("Redis occur handleCacheEvictError：key -> [{}]", key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logger.error("Redis occur handleCacheClearError：", exception);
            }
        };
    }
}
