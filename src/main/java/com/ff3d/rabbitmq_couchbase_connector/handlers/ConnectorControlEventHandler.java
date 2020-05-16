package com.ff3d.rabbitmq_couchbase_connector.handlers;

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
            System.out.println("DcpSnapshotMarkerRequest event: " + DcpSnapshotMarkerRequest.toString(event));
            flowController.ack(event);
        } else if (RollbackMessage.is(event)) {
            System.out.println("RollbackMessage event");
            final short partition = RollbackMessage.vbucket(event);
            stream.onRollbackMessageReceived(partition, RollbackMessage.seqno(event));
        } else {
            System.out.println("Other event: " + event.toString()); 
            flowController.ack(event);
        }
        event.release();
    }

}
