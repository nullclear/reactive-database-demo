package dev.yxy.reactive;

import dev.yxy.reactive.property.MongoProperty;
import dev.yxy.reactive.property.R2dbcProperty;
import dev.yxy.reactive.property.RedisProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties(value = {R2dbcProperty.class, MongoProperty.class, RedisProperty.class})
@SpringBootApplication
public class ReactiveDatabaseDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveDatabaseDemoApplication.class, args);
    }

}
