package com.ff3d.rabbitmq_couchbase_connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.couchbase.client.dcp.message.MessageUtil;
import com.couchbase.client.dcp.DataEventHandler;
import com.couchbase.client.dcp.transport.netty.ChannelFlowController;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

/**
 * Data event handler class, delegates event actions to registered listeners,
 * handles flowcontrol and buffer relase for the CB client
 *  
 * Author: P. Morgano
 */
public class ConnectorDataEventHandler implements DataEventHandler {

	private List<DCPEventListener> listeners = new ArrayList<DCPEventListener>();
	private boolean discardSyncEvents = true;
	private ConnectorDataEventHandlerCallback stream;

	public interface ConnectorDataEventHandlerCallback {
		void onDataEventHandled();
	}

	public ConnectorDataEventHandler(ConnectorDataEventHandlerCallback stream) {
		this(stream,true);
	}

	public ConnectorDataEventHandler(ConnectorDataEventHandlerCallback stream, boolean discardSyncEvents) {

		this.discardSyncEvents = discardSyncEvents;
		this.stream = stream;

	}

	public void addEventListener(DCPEventListener listener) {
		listeners.add(listener);
	}

	public void onEvent(ChannelFlowController flowController, ByteBuf event) {
		// By default ignore _sync mutations or deletions from CB. Can override in config
		if (!(discardSyncEvents && MessageUtil.getKeyAsString(event).contains("_sync"))) {
			Iterator<DCPEventListener> eventListerIterator = listeners.iterator();
			while (eventListerIterator.hasNext()) {
				try {
					eventListerIterator.next().onEvent(event);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		flowController.ack(event);
        event.release();
        stream.onDataEventHandled();
    }

}
