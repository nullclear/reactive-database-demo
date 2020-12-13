package dev.yxy.reactive.handler;

import dev.yxy.reactive.model.entity.Person;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@CacheConfig(cacheNames = "cache")//作用在类上，可以设置整个类的cacheNames，但是会被方法上的替换
public class CacheHandler {

    //----add------------------------------------------------

    @Cacheable(keyGenerator = "keyGenerator")
    public long test_keyGenerator(long id) {
        return id;
    }

    @Cacheable(key = "'bean'")
    public Person test_bean(Person bean) {
        return bean;
    }

    @Cacheable(key = "'date'")
    public Date test_date() {
        return new Date();
    }

    @Cacheable(key = "'boolean'")
    public boolean test_boolean() {
        return true;
    }

    @Cacheable(key = "'long'")
    public long test_long() {
        return 1;
    }

    @Cacheable(key = "'double'")
    public double test_double() {
        return 6.66;
    }

    @Cacheable(key = "'char'")
    public char test_char() {
        return 'a';
    }

    @Cacheable(key = "'string'")
    public String test_string() {
        return "Hello 你好！@666";
    }

    @Cacheable(key = "'null'")
    public String test_null() {
        return null;
    }

    @Cacheable(key = "'map'")
    public Map<String, Object> test_map() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("one", 'a');
        map.put("two", 1);
        map.put("three", 3.33);
        return map;
    }

    @Cacheable(key = "'list'")
    public List<Object> test_list() {
        return new ArrayList<>(Arrays.asList('a', 1, 3.33));
    }

    @Cacheable(key = "'char_array'")
    public char[] test_char_array() {
        return new char[]{'a', 'b', 'c'};
    }

    //----put--------------------------------------------------------------------

    @CachePut(key = "'time'")
    public long test_put() {
        return System.currentTimeMillis();
    }

    //----delete-----------------------------------------------------------------

    @CacheEvict(key = "'long'", beforeInvocation = true)
    public void delete() {
    }

    @CacheEvict(cacheNames = "x", allEntries = true, beforeInvocation = true)
    public void delete_x() {
    }

    //只能删除cacheNames一样的
    @CacheEvict(allEntries = true, beforeInvocation = true)
    public void delete_all() {
    }
}
