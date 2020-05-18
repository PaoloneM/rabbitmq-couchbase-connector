package com.ff3d.rabbitmq_couchbase_connector;

import com.couchbase.client.dcp.Client;
import com.couchbase.client.dcp.state.StateFormat;
import com.ff3d.rabbitmq_couchbase_connector.model.DcpStateHelperConfig;
import com.ff3d.rabbitmq_couchbase_connector.model.DcpStateHelperConfig.StateSaveStrategy;
import com.couchbase.client.java.*;
import com.couchbase.client.java.json.JsonObject;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper class to store and retrieve DCP stream state to/from file
 */
public class DcpStateHelper {

    private final DcpStateHelperConfig config;
    private Cluster cluster;
    private Bucket bucket;
    private Collection collection;

    private byte[] state;
    private String stringState;

    public DcpStateHelper(final DcpStateHelperConfig config) {
        this.config = config;
        if (config.getStrategy() == StateSaveStrategy.COUCHBASE){
            initCouchbaseClient(config);
        }
    }

    private void initCouchbaseClient(DcpStateHelperConfig config) {
        cluster = Cluster.connect(config.getCluster(), config.getUser(), config.getPassword());
        bucket = cluster.bucket(config.getBucket());
        collection = bucket.defaultCollection();
    }


    public byte[] loadState() throws IOException {
        switch (this.config.getStrategy()) {
            case FILE:
                return loadStateFromFile();
            case COUCHBASE:
                return loadStateFromCouchbase();
            default:
                throw new Error("Unknown state persistence strategy");
        }
    }

    public byte[] loadStateFromCouchbase() throws IOException {
        return null;
    }

    public byte[] loadStateFromFile() throws IOException {
        final File file = new File(this.config.getFilename());
        byte[] persisted = null;
        if (file.exists()) {
            final FileInputStream fis = new FileInputStream(this.config.getFilename());
            persisted = IOUtils.toByteArray(fis);
            fis.close();
        }
        return persisted;

    }

    public void saveState(final Client client) throws IOException {
        switch (this.config.getStrategy()) {
            case FILE:

            saveStateToFile(client);
                break;
            case COUCHBASE:
                saveStateToCouchbase(client);
                break;
            default:
                throw new Error("Unknown state persistence strategy");
        }
    }

    public void saveStateToFile(final Client client) throws IOException {

        // export the state as a JSON byte array
        final byte[] state = client.sessionState().export(StateFormat.JSON);

        final AsyncStateFileSaver saver = new AsyncStateFileSaver(state, this.config.getFilename());
        saver.run();

    }

    public void saveStateToCouchbase(final Client client) throws IOException {

        System.out.println("*** Saving state to Couchbase ***");
        // export the state as a JSON byte array
        state = client.sessionState().export(StateFormat.JSON);
        stringState = new String(state);
        collection.upsert(config.getStateDocKey(), JsonObject.fromJson(stringState));

    }
    public class AsyncStateFileSaver implements Runnable {

        public AsyncStateFileSaver(final byte[] state, final String stateFilename) throws IOException {
            // Write it to a file
            final FileOutputStream output = new FileOutputStream(new File(stateFilename));
            IOUtils.write(state, output);
            output.close();

            System.out.println(System.currentTimeMillis() + " - Persisted State! " + stateFilename);

        }

        public void run() {
        }
    }
}
