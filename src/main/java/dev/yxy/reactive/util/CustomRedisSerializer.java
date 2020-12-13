package dev.yxy.reactive.util;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class CustomRedisSerializer<T> implements RedisSerializer<T> {
    //空数组
    private static final byte[] EMPTY_ARRAY = new byte[0];
    //默认字符编码
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    //泛型类型
    private final Class<T> TYPE;
    //JSON配置
    private static final JSONConfig JSON_CONFIG = JSONConfig.create().setIgnoreError(true).setOrder(true).setDateFormat("yyyy-MM-dd HH:mm:ss");

    @NotNull
    @Contract(" -> new")
    public static CustomRedisSerializer<Object> json() {
        return new CustomRedisSerializer<>(Object.class);
    }

    @NotNull
    @Contract(" -> new")
    public static CustomRedisSerializer<String> string() {
        return new CustomRedisSerializer<>(String.class);
    }

    public CustomRedisSerializer(@NotNull Class<T> TYPE) {
        this.TYPE = TYPE;
    }

    /**
     * null直接转为空
     * CharSequence、Character、Number、Boolean直接转为String
     * Date获取毫秒数后转为String
     * 其他全部转为Json，如果发生异常，转为String
     */
    @NotNull
    @Override
    public byte[] serialize(@Nullable Object source) throws SerializationException {
        if (null == source) {
            return EMPTY_ARRAY;
        } else if (source instanceof CharSequence || source instanceof Character || source instanceof Number || source instanceof Boolean) {
            return String.valueOf(source).getBytes(CHARSET);
        } else if (source instanceof Date) {
            return String.valueOf(((Date) source).getTime()).getBytes(CHARSET);
        } else {
            try {
                return JSONUtil.parse(source, JSON_CONFIG).toJSONString(0).getBytes(CHARSET);
            } catch (Exception e) {
                return String.valueOf(source).getBytes(CHARSET);
            }
        }
    }

    /**
     * 空数组或者null直接转为null，
     * 其他全部转为String
     */
    @Nullable
    @Override
    public T deserialize(@Nullable byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            return TYPE.cast(new String(bytes, CHARSET));
        }
    }
}
