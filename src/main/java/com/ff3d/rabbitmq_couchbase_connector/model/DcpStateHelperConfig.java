package com.ff3d.rabbitmq_couchbase_connector.model;

public class DcpStateHelperConfig {

    public static enum StateSaveStrategy {
        FILE,
        COUCHBASE 
    }

    private String filename;
    private String bucket;
    private String user;
    private String password;
    private StateSaveStrategy strategy;

    public DcpStateHelperConfig(String stateFilename, String bucket, String user, String password, StateSaveStrategy strategy) {
        this.filename = stateFilename + "DCP-" + bucket + "-status.json";
        this.bucket = bucket;
        this.user = user;
        this.password = password;
        this.strategy = strategy;
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

    
}