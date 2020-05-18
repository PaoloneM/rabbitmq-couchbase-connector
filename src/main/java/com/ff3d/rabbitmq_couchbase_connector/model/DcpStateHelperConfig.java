package com.ff3d.rabbitmq_couchbase_connector.model;

public class DcpStateHelperConfig {

    public static enum StateSaveStrategy {
        FILE,
        COUCHBASE 
    }

    private String filename;
    private String cluster;
    private String bucket;
    private String user;
    private String password;
    private StateSaveStrategy strategy;
    private String stateDocKey;

    public DcpStateHelperConfig(String stateFilename, String cluster, String bucket, String user, String password, StateSaveStrategy strategy, String stateDocPrefix) {
        this.filename = stateFilename + "DCP-" + bucket + "-status.json";
        this.cluster = cluster;
        this.bucket = bucket;
        this.user = user;
        this.password = password;
        this.strategy = strategy;
        this.stateDocKey = stateDocPrefix;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * @param bucket the bucket to set
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the strategy
     */
    public StateSaveStrategy getStrategy() {
        return strategy;
    }

    /**
     * @param strategy the strategy to set
     */
    public void setStrategy(StateSaveStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * @return the cluster
     */
    public String getCluster() {
        return cluster;
    }

    /**
     * @param cluster the cluster to set
     */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    /**
     * @return the stateDocPrefix
     */
    public String getStateDocKey() {
        return stateDocKey;
    }

    /**
     * @param stateDocKey the stateDocPrefix to set
     */
    public void setStateDocKey(String stateDocKey) {
        this.stateDocKey = stateDocKey;
    }

    
}