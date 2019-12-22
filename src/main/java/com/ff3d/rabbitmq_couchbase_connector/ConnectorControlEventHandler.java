package com.ff3d.rabbitmq_couchbase_connector;

import com.couchbase.client.dcp.ControlEventHandler;
import com.couchbase.client.dcp.message.DcpSnapshotMarkerRequest;
import com.couchbase.client.dcp.message.RollbackMessage;
import com.couchbase.client.dcp.transport.netty.ChannelFlowController;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

/**
 * Control event handler class, delegates event actions to registered listeners,
 * handles flowcontrol and buffer relase for the CB client
 *  
 * Author: P. Morgano
 */
public class ConnectorControlEventHandler implements ControlEventHandler {

    private ConnectorControlEventHandlerCallback stream;
    public ConnectorControlEventHandler(ConnectorControlEventHandlerCallback stream) {
        this.stream = stream;
    }

    public interface ConnectorControlEventHandlerCallback {
        void onRollbackMessageReceived(short partition, long seqNo);
    }

    public void onEvent(ChannelFlowController flowController, ByteBuf event) {

        if (DcpSnapshotMarkerRequest.is(event)) {
            flowController.ack(event);
        }

        if (RollbackMessage.is(event)) {
            final short partition = RollbackMessage.vbucket(event);
            stream.onRollbackMessageReceived(partition, RollbackMessage.seqno(event));
        }

        event.release();
    }

}
