package dev.yxy.reactive.config;

import dev.yxy.reactive.property.R2dbcProperty;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * R2DBC的配置文件
 *
 * @see R2dbcDataAutoConfiguration
 * @see R2dbcTransactionManagerAutoConfiguration
 */
@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories(basePackages = "dev.yxy.reactive.dao.repository", excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {ReactiveMongoRepository.class})})
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Autowired
    private R2dbcProperty r2dbc;

    @Bean
    @Primary
    @NotNull
    @Override
    public ConnectionFactory connectionFactory() {
        //1.配置连接工厂
        ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, r2dbc.getDriver())//连接池
                .option(ConnectionFactoryOptions.PROTOCOL, r2dbc.getProtocol())//协议名字
                .option(ConnectionFactoryOptions.HOST, r2dbc.getHost())//主机名
                .option(ConnectionFactoryOptions.PORT, r2dbc.getPort())//端口
                .option(ConnectionFactoryOptions.DATABASE, r2dbc.getDatabase())//数据库名字
                .option(ConnectionFactoryOptions.USER, r2dbc.getUser())//用户名
                .option(ConnectionFactoryOptions.PASSWORD, r2dbc.getPassword())//密码
                .option(ConnectionFactoryOptions.CONNECT_TIMEOUT, r2dbc.getConnectTimeout())//连接超时时间
                .option(ConnectionFactoryOptions.SSL, r2dbc.isSsl())
                .build());

        //2.配置连接池
        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxSize(r2dbc.getPool().getMaxSize())//最大连接数
                .initialSize(r2dbc.getPool().getInitialSize())//初始连接数
                .maxIdleTime(r2dbc.getPool().getMaxIdleTime())//连接后的最大空闲时间
                .maxCreateConnectionTime(r2dbc.getPool().getMaxCreateConnectionTime())//最大创建连接时间
                .build();

        //3.返回连接池
        return new ConnectionPool(poolConfiguration);
    }

    @Bean
    @Primary
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        //配置事务管理器
        return new R2dbcTransactionManager(connectionFactory);
    }
}
