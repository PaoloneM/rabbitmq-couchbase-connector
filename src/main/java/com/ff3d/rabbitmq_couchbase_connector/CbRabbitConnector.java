package com.ff3d.rabbitmq_couchbase_connector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.couchbase.client.core.env.NetworkResolution;
import com.couchbase.client.dcp.config.CompressionMode;

class CbRabbitConnector {
    public static void main(String[] args) throws IOException, TimeoutException {

        System.out.println("Welcome to FF3D.COM Couchbase Rabbit Connector");

        // Test name resolution if cluster headless server
        InetAddress address = InetAddress.getByName(Constants.COUCHBASE_CLUSTER_SERVICE); 
        System.out.println(address.getHostAddress()); 

        String list = System.getenv(Constants.COUCHBASE_CLUSTER);
        List<String> couchbaseCluster = Arrays.asList(list.split(","));
        String bucket = System.getenv(Constants.COUCHBASE_BUCKET);
        String bucketUser = System.getenv(Constants.COUCHBASE_BUCKET_USER);
        String bucketPassword = System.getenv(Constants.COUCHBASE_BUCKET_PASSWORD);
        long cbConnectionTimeout = Long.parseLong(System.getenv(Constants.COUCHBASE_CONN_TIMEOUT));
        long cbPersistencePollIntv = Long.parseLong(System.getenv(Constants.COUCHBASE_PERSISTENCE_POLL_INTV));
        int flowCtrlBuffBytes = Integer.parseInt(System.getenv(Constants.COUCHBASE_FLOWCTRL_BUFF_BYTES));
        String rabbitHost = System.getenv(Constants.RABBIT_HOST);
        int rabbitPort = Integer.parseInt(System.getenv(Constants.RABBIT_PORT));
        String rabbitUser = System.getenv(Constants.RABBIT_USER);
        String rabbitPassword = System.getenv(Constants.RABBIT_PASSWORD);
        String stateFilePath = System.getenv(Constants.STATE_FILE_PATH);

        final DCPStream stream = new DCPStream();
        
        System.out.println("Configuring stream");

        stream.init(couchbaseCluster, bucket, bucketUser, bucketPassword, cbConnectionTimeout, NetworkResolution.AUTO,
                CompressionMode.ENABLED, cbPersistencePollIntv, flowCtrlBuffBytes, rabbitHost, rabbitPort, rabbitUser,
                rabbitPassword, stateFilePath);
        
        System.out.println("Starting stream");
        try {       
            stream.start();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    try {
                        stream.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}