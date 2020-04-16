package com.ff3d.rabbitmq_couchbase_connector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.couchbase.client.core.env.NetworkResolution;
import com.couchbase.client.dcp.config.CompressionMode;

class CbRabbitConnector {
    public static void main(final String[] args) throws IOException, TimeoutException {

        System.out.println("Welcome to FF3D.COM Couchbase Rabbit Connector");

        System.out.println("******************************Environment Vars*****************************");
        final Map<String, String> env = System.getenv();
        for (final String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }

        // Test name resolution if cluster headless server
        List<String> dinamicHostList = new ArrayList<String>();
        try {
            System.out.println("Resolving cluster addresses");
            final InetAddress SW[] = InetAddress.getAllByName(System.getenv(Constants.COUCHBASE_CLUSTER_SERVICE));
            for (int i = 0; i < SW.length; i++){
                System.out.println(SW[i].getHostName());
                System.out.println(SW[i].getHostAddress());
                dinamicHostList.add(SW[i].getHostAddress());
            }
        } catch (final Exception e) {
            System.out.println("Error resolving hedless service: " + e.getMessage());
        }
        

        final String list = System.getenv(Constants.COUCHBASE_CLUSTER);
        final List<String> couchbaseCluster = Arrays.asList(list.split(","));
        final String bucket = System.getenv(Constants.COUCHBASE_BUCKET);
        final String bucketUser = System.getenv(Constants.COUCHBASE_BUCKET_USER);
        final String bucketPassword = System.getenv(Constants.COUCHBASE_BUCKET_PASSWORD);
        final long cbConnectionTimeout = Long.parseLong(System.getenv(Constants.COUCHBASE_CONN_TIMEOUT));
        final long cbPersistencePollIntv = Long.parseLong(System.getenv(Constants.COUCHBASE_PERSISTENCE_POLL_INTV));
        final int flowCtrlBuffBytes = Integer.parseInt(System.getenv(Constants.COUCHBASE_FLOWCTRL_BUFF_BYTES));
        final String rabbitHost = System.getenv(Constants.RABBIT_HOST);
        final int rabbitPort = Integer.parseInt(System.getenv(Constants.RABBIT_PORT));
        final String rabbitUser = System.getenv(Constants.RABBIT_USER);
        final String rabbitPassword = System.getenv(Constants.RABBIT_PASSWORD);
        final String stateFilePath = System.getenv(Constants.STATE_FILE_PATH);

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
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}