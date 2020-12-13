package dev.yxy.reactive.util;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class JsonUtil {
    //配置
    public static final JSONConfig JSON_CONFIG = JSONConfig.create().setIgnoreError(true).setOrder(true).setDateFormat("yyyy-MM-dd HH:mm:ss");
    //list
    public static final TypeReference<ArrayList<String>> LIST_STRING = new TypeReference<>() {
    };
    //map
    public static final TypeReference<HashMap<String, Object>> MAP_STRING_OBJECT = new TypeReference<>() {
    };

    /**
     * 理论上原始数据只能传入JSON和CharSequence类型，但是传入别的类型与之对应的Class，可以实现克隆操作
     *
     * @param value 原始数据
     * @param clazz 目标类型
     * @param <T>   clazz
     * @return clazz
     */
    @Nullable
    public static <T> T toBean(@Nullable Object value, @NotNull Class<T> clazz) {
        try {
            return JSONUtil.toBean(JSONUtil.parse(value, JSON_CONFIG), clazz, true);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 理论上原始数据只能传入JSON和CharSequence类型，但是传入别的类型与它的Class，可以实现克隆操作
     *
     * @param value         原始数据
     * @param typeReference 目标类型
     * @param <T>           typeReference
     * @return typeReference
     */
    @Nullable
    public static <T> T toBean(@Nullable Object value, TypeReference<T> typeReference) {
        try {
            return JSONUtil.toBean(JSONUtil.parse(value, JSON_CONFIG), typeReference, true);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 可以传入任意字符串，但是不符合jsonArray格式只会返回空List
     *
     * @param jsonArray 任意字符串
     * @param clazz     List中类型
     * @param <T>       clazz
     * @return clazz (ArrayList)
     */
    @NotNull
    public static <T> List<T> toList(@Nullable String jsonArray, Class<T> clazz) {
        try {
            return JSONUtil.toList(jsonArray, clazz);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 可以将任意类型对象转为Json
     * 如果是无效的类型，将会转为{}
     *
     * @param obj 任意类型对象
     * @return json格式数据
     */
    @Nullable
    public static String toJson(@Nullable Object obj) {
        if (null == obj) {
            return null;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).toString();
        }
        try {
            return JSONUtil.parse(obj, JSON_CONFIG).toJSONString(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 可以将任意类型对象序列化
     * null直接转为""
     * CharSequence、Character、Number、Boolean直接转为String
     * Date获取毫秒数后转为String
     * 其他全部转为Json，如果发生异常，转为String
     *
     * @param source 任意类型对象
     * @return string或json
     */
    @NotNull
    public static String serialize(@Nullable Object source) {
        if (null == source) {
            return "";
        } else if (source instanceof CharSequence || source instanceof Character || source instanceof Number || source instanceof Boolean) {
            return String.valueOf(source);
        } else if (source instanceof Date) {
            return String.valueOf(((Date) source).getTime());
        } else {
            try {
                return JSONUtil.parse(source, JSON_CONFIG).toJSONString(0);
            } catch (Exception e) {
                return String.valueOf(source);
            }
        }
    }
}
