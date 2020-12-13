package dev.yxy.reactive.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@ConfigurationProperties(prefix = "reactive.r2dbc")
public class R2dbcProperty {

    /**
     * 驱动
     */
    private String driver = "pool";

    /**
     * 协议
     */
    private String protocol = "mysql";

    /**
     * 主机
     */
    private String host = "localhost";

    /**
     * 端口
     */
    private int port = 3306;

    /**
     * 数据库
     */
    private String database;

    /**
     * 用户
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接超时时间
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * 是否需要ssl
     */
    private boolean ssl = false;

    /**
     * 连接池配置
     */
    @NestedConfigurationProperty
    private Pool pool = new Pool();

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    /**
     * 连接池配置
     */
    public static class Pool {
        /**
         * 最大连接数
         */
        private int maxSize = 10;

        /**
         * 初始化连接数
         */
        private Integer initialSize = 5;

        /**
         * 连接后的最大空闲时间
         */
        private Duration maxIdleTime = Duration.ofMinutes(30);

        /**
         * 最大创建连接的时间
         */
        private Duration maxCreateConnectionTime = Duration.ZERO;

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public Integer getInitialSize() {
            return initialSize;
        }

        public void setInitialSize(Integer initialSize) {
            this.initialSize = initialSize;
        }

        public Duration getMaxIdleTime() {
            return maxIdleTime;
        }

        public void setMaxIdleTime(Duration maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }

        public Duration getMaxCreateConnectionTime() {
            return maxCreateConnectionTime;
        }

        public void setMaxCreateConnectionTime(Duration maxCreateConnectionTime) {
            this.maxCreateConnectionTime = maxCreateConnectionTime;
        }
    }
}
