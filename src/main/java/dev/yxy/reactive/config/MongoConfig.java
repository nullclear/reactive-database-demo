package dev.yxy.reactive.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import dev.yxy.reactive.property.MongoProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.SpringDataMongoDB;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * 混合配置Reactive Mongo和Mongo<br/>
 * <br/>
 * spring自动配置的过程分为以下四步<br/>
 * <br/>
 * 一、{@link #configureClientSettings} -> {@link #mongoClientSettings}-> {@link #mongoClient()} + {@link #getDatabaseName()} -> {@link #mongoDbFactory}<br/>
 * 二、{@link #configureConverters} -> {@link #customConversions MongoCustomConversions} -> {@link #mongoMappingContext MongoMappingContext}<br/>
 * 三、{@link #mongoDbFactory} + {@link #mongoMappingContext MongoMappingContext} + {@link #customConversions MongoCustomConversions}-> {@link #mappingMongoConverter}<br/>
 * 四、{@link #mongoDbFactory} + {@link #mappingMongoConverter} -> MongoTemplate<br/>
 * <br/>
 * 其他细节参考以下的类<br/>
 * {@link MongoReactiveDataAutoConfiguration#reactiveMongoTemplate ReactiveMongoTemplate}<br/>
 * {@link MongoDataAutoConfiguration}中MongoDatabaseFactoryDependentConfiguration.class有MongoTemplate的来源<br/>
 * <br/>
 */
@Configuration
@EnableReactiveMongoRepositories(basePackages = {"dev.yxy.reactive.dao.repository"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {R2dbcRepository.class})})
public class MongoConfig extends MongoConfigurationSupport {

    @Autowired
    private MongoProperty mongo;

    //配置client参数
    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.credential(MongoCredential.createCredential(mongo.getUsername(), mongo.getAuthenticationDatabase(), mongo.getPassword()))
                .applyToClusterSettings(settings -> settings.hosts(mongo.obtainHosts())
                        .mode(mongo.getMode()).requiredClusterType(mongo.getClusterType()));
    }

    //要连接的数据库名称
    @NotNull
    @Override
    protected String getDatabaseName() {
        return mongo.getDatabase();
    }

    //配置是否自动创建索引
    @Override
    protected boolean autoIndexCreation() {
        return mongo.getAutoIndexCreation();
    }

    //mongo的配置，这两个要设置成Bean，Spring还需要，具体需要它干嘛不懂
    @Bean
    @Primary
    public com.mongodb.client.MongoClient mongoClient() {
        return com.mongodb.client.MongoClients.create(mongoClientSettings(), SpringDataMongoDB.driverInformation());
    }

    //Reactive mongo的配置，这两个要设置成Bean，Spring还需要，具体需要它干嘛不懂
    @Bean
    @Primary
    public com.mongodb.reactivestreams.client.MongoClient reactiveMongoClient() {
        return com.mongodb.reactivestreams.client.MongoClients.create(mongoClientSettings(), SpringDataMongoDB.driverInformation());
    }

    //mongo的数据库连接工厂
    @Bean
    @Primary
    public MongoDatabaseFactory mongoDbFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), getDatabaseName());
    }

    //Reactive mongo的数据库连接工厂
    @Bean
    @Primary
    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }

    //映射转换器
    @Bean
    @Primary
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory, MongoCustomConversions customConversions, MongoMappingContext mappingContext) {
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(databaseFactory), mappingContext);
        converter.setCustomConversions(customConversions);
        converter.setCodecRegistryProvider(databaseFactory);
        return converter;
    }
}
