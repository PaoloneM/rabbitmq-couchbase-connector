package com.ff3d.rabbitmq_couchbase_connector.handlers;

import com.couchbase.client.core.event.CouchbaseEvent;
import com.couchbase.client.dcp.SystemEventHandler;

public class ConnectorSystemEventHandler implements SystemEventHandler {

    private ConnectorSystemEventHandlerCallback stream;

    public ConnectorSystemEventHandler(ConnectorSystemEventHandlerCallback stream) {
        this.stream = stream;
    }

    public interface ConnectorSystemEventHandlerCallback {
        void onCbSystemEventReceived(CouchbaseEvent event);
    }

    public void onEvent(CouchbaseEvent event) {
        stream.onCbSystemEventReceived(event);
    }
    
}