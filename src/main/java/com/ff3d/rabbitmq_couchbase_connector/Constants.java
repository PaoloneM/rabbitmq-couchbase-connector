package com.ff3d.rabbitmq_couchbase_connector;

class Constants {
    public static final String COUCHBASE_CLUSTER = "CB_URL";
    public static final String COUCHBASE_BUCKET = "CB_BUCKET";
    public static final String COUCHBASE_BUCKET_USER= "CB_USERNAME";
    public static final String COUCHBASE_BUCKET_PASSWORD = "CB_PWD";
    public static final String COUCHBASE_CONN_TIMEOUT = "CB_CONN_TIMEOUT";
    public static final String COUCHBASE_PERSISTENCE_POLL_INTV = "CB_PERSISTENCE_POLL_INTV";
    public static final String COUCHBASE_FLOWCTRL_BUFF_BYTES = "CB_FLOWCTRL_BUFF_BYTES";
    public static final String RABBIT_HOST = "RMQ_IP";
    public static final String RABBIT_PORT = "RMQ_PORT";
    public static final String RABBIT_USER = "RMQ_USERNAME";
    public static final String RABBIT_PASSWORD = "RMQ_PWD";
	public static final String EXCHANGE_NAME = "RMQ_CO_EXCHANGE";
    public static final String STATE_FILE_PATH = "STATE_FILE_PATH";
    public static final String STATE_SAVE_DELAY_SEC = "STATE_SAVE_DELAY_SEC";
    public static final String MESSAGE_KEY="MESSAGE_KEY";
    public static final String MESSAGE_KEY_TEMPLATE="MESSAGE_KEY_TEMPLATE";
    public static final String MESSAGE_KEY_FIELD="MESSAGE_KEY_FIELD";

}