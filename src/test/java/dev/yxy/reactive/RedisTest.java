package dev.yxy.reactive;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import dev.yxy.reactive.model.entity.Person;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.io.IOException;
import java.util.*;

@SpringBootTest
public class RedisTest {
    private static final Logger logger = LoggerFactory.getLogger(RedisTest.class);

    public static final JSONConfig JSON_CONFIG = JSONConfig.create().setIgnoreError(true).setOrder(true).setDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Autowired
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void test_add() {
        HashSet<String> set = new HashSet<>();
        set.add("Hello");
        set.add("World");

        Person person = new Person();
        person.setId(UUID.randomUUID().toString());
        person.setName("robust");
        person.setAge(20);
        person.setRoles(set);

        String str1 = "aa\"bb\"ccc\"ddd";
        String str2 = "你好，666！";
        ArrayList<String> list = new ArrayList<>(Arrays.asList("1", "2", "3"));
        HashMap<Character, Double> map = new HashMap<>();
        map.put('a', 1.0);
        map.put('b', 2.2);
        map.put('c', 3.3);

        //reactive
        reactiveRedisTemplate.opsForValue().set("reactive:01", "").subscribe();//空
        reactiveRedisTemplate.opsForValue().set("reactive:02", "[]").subscribe();//[]
        reactiveRedisTemplate.opsForValue().set("reactive:03", "{}").subscribe();//{}
        reactiveRedisTemplate.opsForValue().set("reactive:04", null).subscribe();//空
        reactiveRedisTemplate.opsForValue().set("reactive:05", str1).subscribe();//aa"bb"ccc"ddd
        reactiveRedisTemplate.opsForValue().set("reactive:06", str2).subscribe();//你好，666！
        reactiveRedisTemplate.opsForValue().set("reactive:07", person).subscribe();//{"id":"c818dd67-17d2-47a3-a6ed-2f4387eb987e","name":"robust","age":20,"roles":["world","Hello"]}
        reactiveRedisTemplate.opsForValue().set("reactive:08", list).subscribe();//["1","2","3"]
        reactiveRedisTemplate.opsForValue().set("reactive:09", map).subscribe();//{"a":1,"b":2.2,"c":3.3}
        reactiveRedisTemplate.opsForValue().set("reactive:10", 123).subscribe();//123
        reactiveRedisTemplate.opsForValue().set("reactive:11", 88.88).subscribe();//88.88
        reactiveRedisTemplate.opsForValue().set("reactive:12", 66L).subscribe();//66
        reactiveRedisTemplate.opsForValue().set("reactive:13", 99D).subscribe();//99.0
        reactiveRedisTemplate.opsForValue().set("reactive:14", true).subscribe();//true
        reactiveRedisTemplate.opsForValue().set("reactive:15", 'a').subscribe();//a
        reactiveRedisTemplate.opsForValue().set("reactive:16", new Date()).subscribe();//1607846592660

        logger.info("add successfully");
    }

    @Test
    void test_add_string() {
        HashSet<String> set = new HashSet<>();
        set.add("Hello");
        set.add("World");

        Person person = new Person();
        person.setId(UUID.randomUUID().toString());
        person.setName("robust");
        person.setAge(20);
        person.setRoles(set);

        String str1 = "aa\"bb\"ccc\"ddd";
        String str2 = "你好，666！";
        ArrayList<String> list = new ArrayList<>(Arrays.asList("1", "2", "3"));
        HashMap<Character, Double> map = new HashMap<>();
        map.put('a', 1.0);
        map.put('b', 2.2);
        map.put('c', 3.3);

        //reactive string
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:01", "").subscribe();//空
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:02", "[]").subscribe();//[]
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:03", "{}").subscribe();//{}
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:04", null).subscribe();//空
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:05", str1).subscribe();//aa"bb"ccc"ddd
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:06", str2).subscribe();//你好，666！
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:07", person.toString()).subscribe();//{"id":"93b94f1c-2f1f-45a6-b5fe-b2fb7d46a933","name":"robust","age":20,"roles":[Hello, World]}
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:08", list.toString()).subscribe();//[1, 2, 3]
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:09", map.toString()).subscribe();//{a=1.0, b=2.2, c=3.3}
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:10", String.valueOf(123)).subscribe();//123
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:11", String.valueOf(88.88)).subscribe();//88.88
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:12", String.valueOf(66L)).subscribe();//66
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:13", String.valueOf(99D)).subscribe();//99.0
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:14", String.valueOf(true)).subscribe();//true
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:15", String.valueOf('a')).subscribe();//a
        reactiveStringRedisTemplate.opsForValue().set("reactive-string:16", String.valueOf(new Date())).subscribe();//Sun Dec 13 16:10:25 CST 2020

        logger.info("add string successfully");
    }

    @Test
    void test_get() throws InterruptedException {
        //看看null对reactive的影响
        reactiveStringRedisTemplate.opsForValue().get("reactive:01").map(s -> JSONUtil.parse(s).<Person>toBean(Person.class, true)).subscribe(x -> logger.info("reactive:01 = " + x.toString()));
        //没有输出

        //看看{}reactive的影响
        reactiveStringRedisTemplate.opsForValue().get("reactive:03").map(s -> JSONUtil.parse(s).<Person>toBean(Person.class, true)).subscribe(x -> logger.info("reactive:03 = " + x.toString()));
        //reactive:03 = {"id":"null","name":"null","age":null,"roles":null}

        //获取一个存在的实体类
        reactiveStringRedisTemplate.opsForValue().get("reactive:07").map(s -> JSONUtil.parse(s).<Person>toBean(Person.class, true)).subscribe(x -> logger.info("reactive:07 = " + x.toString()));
        //reactive:07 = {"id":"1b968e2d-7e98-4811-9cd0-39054e6a2459","name":"robust","age":20,"roles":[Hello, World]}

        Thread.sleep(2000);
    }

    //redisTemplate没有提供scan方法，自定义一个
    public Set<String> scan(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            try {
                cursor.close();
                connection.close();
            } catch (IOException | DataAccessException e) {
                e.printStackTrace();
            }
            return keys;
        });
    }
}
