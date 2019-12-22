package com.ff3d.rabbitmq_couchbase_connector;

import com.couchbase.client.dcp.Client;
import com.couchbase.client.dcp.state.StateFormat;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper class to store and retrieve DCP stream state to/from file
 */
public class DcpStateHelper {

    private final String stateFilename;

    public DcpStateHelper(String stateFilename) {
        this.stateFilename = stateFilename;
    }

    public byte[] loadState() throws IOException {

        File file = new File(this.stateFilename);
        byte[] persisted = null;
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(this.stateFilename);
            persisted = IOUtils.toByteArray(fis);
            fis.close();
        }
        return persisted;

    }

    public void saveState(Client client) throws IOException {

        // export the state as a JSON byte array
        final byte[] state = client.sessionState().export(StateFormat.JSON);

        AsyncStateSaver saver = new AsyncStateSaver(state, this.stateFilename);
        saver.run();

    }

    public class AsyncStateSaver implements Runnable {

        public AsyncStateSaver(byte[] state, String stateFilename) throws IOException {
            // Write it to a file
            FileOutputStream output = new FileOutputStream(new File(stateFilename));
            IOUtils.write(state, output);
            output.close();

            System.out.println(System.currentTimeMillis() + " - Persisted State! " + stateFilename);

        }

        public void run() {
        }
    }
}
