package dev.yxy.reactive;

import dev.yxy.reactive.handler.CacheHandler;
import dev.yxy.reactive.model.entity.Person;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.UUID;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheTest {
    private static final Logger logger = LoggerFactory.getLogger(CacheTest.class);

    @Autowired
    private CacheHandler cacheHandler;

    @RepeatedTest(2)
    @Order(1)
    void test_add() {
        HashSet<String> set = new HashSet<>();
        set.add("Hello");
        set.add("world");

        Person person = new Person();
        person.setId(UUID.randomUUID().toString());
        person.setName("robust");
        person.setAge(20);
        person.setRoles(set);

        cacheHandler.test_keyGenerator(100);
        cacheHandler.test_bean(person);
        cacheHandler.test_date();
        cacheHandler.test_boolean();
        cacheHandler.test_long();
        cacheHandler.test_double();
        cacheHandler.test_string();
        cacheHandler.test_map();
        cacheHandler.test_list();
        cacheHandler.test_null();
        cacheHandler.test_char();
        cacheHandler.test_char_array();
        logger.info("add successfully");
    }

    @RepeatedTest(6)
    @Order(2)
    void test_put() {
        cacheHandler.test_put();
        logger.info("put successfully");
    }

    @Test
    @Order(3)
    void test_delete() {
        cacheHandler.delete();
        cacheHandler.delete_x();
        logger.info("delete successfully");
    }

    @Test
    @Order(4)
    void test_delete_all() {
        cacheHandler.delete_all();
        logger.info("delete all successfully");
    }
}
