package dev.yxy.reactive.property;

import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "reactive.mongo")
public class MongoProperty {

    /**
     * cluster types.
     */
    private ClusterType clusterType = ClusterType.UNKNOWN;

    /**
     * The cluster connection mode.
     */
    private ClusterConnectionMode mode = ClusterConnectionMode.SINGLE;

    /**
     * 以逗号分割的host:port列表，至少要有一个节点
     */
    private List<String> nodes = Collections.singletonList("localhost:27017");

    /**
     * Authentication database name.
     */
    private String authenticationDatabase = "admin";

    /**
     * Login user of the mongo server. Cannot be set with URI.
     */
    private String username = "root";

    /**
     * Login password of the mongo server. Cannot be set with URI.
     */
    private char[] password = "root".toCharArray();

    /**
     * Database name.
     */
    private String database = "mongo";

    /**
     * Whether to enable auto-index creation.
     */
    private Boolean autoIndexCreation = true;

    /**
     * 连接的客户端名称
     */
    private String applicationName = "localhost";

    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public ClusterConnectionMode getMode() {
        return mode;
    }

    public void setMode(ClusterConnectionMode mode) {
        this.mode = mode;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getAuthenticationDatabase() {
        return authenticationDatabase;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Boolean getAutoIndexCreation() {
        return autoIndexCreation;
    }

    public void setAutoIndexCreation(Boolean autoIndexCreation) {
        this.autoIndexCreation = autoIndexCreation;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public List<ServerAddress> obtainHosts() {
        List<ServerAddress> hosts = new ArrayList<>();
        for (String node : getNodes()) {
            String[] s = node.split(":");
            hosts.add(new ServerAddress(s[0], Integer.parseInt(s[1])));
        }
        return hosts;
    }
}
