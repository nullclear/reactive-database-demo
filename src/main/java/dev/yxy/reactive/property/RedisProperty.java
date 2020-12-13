package dev.yxy.reactive.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "reactive.redis")
//可以省略此注解
//@PropertySource(value = "classpath:application.yml", encoding = "UTF-8")
public class RedisProperty {

    /**
     * redis的主机名
     */
    private String host = "localhost";

    /**
     * redis的端口号
     */
    private int port = 6379;

    /**
     * redis连接的数据库
     */
    private int database = 0;

    /**
     * redis的登录密码
     */
    private String password;

    /**
     * 连接超时的时间
     */
    private Duration timeout = Duration.ofSeconds(10);

    /**
     * Client name to be set on connections with CLIENT SETNAME.
     */
    private String clientName;

    /**
     * spring session独享的数据库
     */
    private int sessionDatabase = 1;

    /**
     * spring session的过期时间
     */
    private Duration sessionTimeout = Duration.ofMinutes(30);

    /**
     * 注解缓存的存活时间 @EnableCaching
     */
    private Duration cacheTtl = Duration.ofMinutes(30);

    /**
     * 集群配置
     */
    private Cluster cluster;

    /**
     * 哨兵配置
     */
    private Sentinel sentinel;

    /**
     * lettuce 客户端配置
     */
    @NestedConfigurationProperty
    private final Lettuce lettuce = new Lettuce();

    /**
     * jedis 客户端配置
     */
    @NestedConfigurationProperty
    private final Jedis jedis = new Jedis();

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

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getSessionDatabase() {
        return sessionDatabase;
    }

    public void setSessionDatabase(int sessionDatabase) {
        this.sessionDatabase = sessionDatabase;
    }

    public Duration getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Duration sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Sentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    public Lettuce getLettuce() {
        return lettuce;
    }

    public Jedis getJedis() {
        return jedis;
    }

    /**
     * Cluster 模式配置
     */
    public static class Cluster {

        /**
         * 以逗号分割的host:port列表，至少要有一个节点
         */
        private List<String> nodes;

        /**
         * 当在集群间穿梭执行命令是的最大重定向吃屎
         */
        private Integer maxRedirects;

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public Integer getMaxRedirects() {
            return this.maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }
    }

    /**
     * Sentinel 模式配置
     */
    public static class Sentinel {

        /**
         * redis服务器的名称
         */
        private String master;

        /**
         * 以逗号分割的host:port列表，至少要有一个节点
         */
        private List<String> nodes;

        /**
         * 哨兵的认证密码
         */
        private String password;

        public String getMaster() {
            return this.master;
        }

        public void setMaster(String master) {
            this.master = master;
        }

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * lettuce 客户端配置
     */
    public static class Lettuce {

        /**
         * 关闭时的超时时间
         */
        private Duration shutdownTimeout = Duration.ofMillis(100);

        /**
         * 连接池配置
         */
        private Pool pool;

        /**
         * 集群刷新
         */
        private final Cluster cluster = new Cluster();

        public Duration getShutdownTimeout() {
            return shutdownTimeout;
        }

        public void setShutdownTimeout(Duration shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
        }

        public Pool getPool() {
            return pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }

        public Cluster getCluster() {
            return cluster;
        }

        public static class Cluster {

            private final Refresh refresh = new Refresh();

            public Refresh getRefresh() {
                return this.refresh;
            }

            public static class Refresh {

                /**
                 * Cluster topology refresh period.
                 */
                private Duration period;

                /**
                 * Whether adaptive topology refreshing using all available refresh
                 * triggers should be used.
                 */
                private boolean adaptive;

                public Duration getPeriod() {
                    return this.period;
                }

                public void setPeriod(Duration period) {
                    this.period = period;
                }

                public boolean isAdaptive() {
                    return this.adaptive;
                }

                public void setAdaptive(boolean adaptive) {
                    this.adaptive = adaptive;
                }

            }
        }
    }

    /**
     * Jedis 客户端配置
     */
    public static class Jedis {

        /**
         * Jedis pool configuration.
         */
        private Pool pool;

        public Pool getPool() {
            return this.pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }
    }

    /**
     * 连接池配置
     */
    public static class Pool {
        /**
         * 最大连接数
         */
        private int maxActive = 8;

        /**
         * 最大空闲数
         */
        private int maxIdle = 8;

        /**
         * 最小空闲数
         */
        private int minIdle = 0;

        /**
         * 连接的最大等待的时间
         */
        private Duration maxWait = Duration.ofMillis(-1);

        /**
         * Time between runs of the idle object evictor thread. When positive, the idle
         * object evictor thread starts, otherwise no idle object eviction is performed.
         */
        private Duration timeBetweenEvictionRuns;

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public Duration getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(Duration maxWait) {
            this.maxWait = maxWait;
        }

        public Duration getTimeBetweenEvictionRuns() {
            return timeBetweenEvictionRuns;
        }

        public void setTimeBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        }
    }
}
