package com.ff3d.rabbitmq_couchbase_connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

public interface DCPEventListener {
	
	public void onEvent(ByteBuf event) throws UnsupportedEncodingException, IOException, UnsupportedEncodingException, IOException;

}
