package com.ff3d.rabbitmq_couchbase_connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

import com.couchbase.client.dcp.message.DcpDeletionMessage;
import com.couchbase.client.dcp.message.DcpMutationMessage;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.util.CharsetUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class DCP_RabbitMQ_Publisher implements DCPEventListener {

	private final Channel channel;
	private final String EXCHANGE_NAME = System.getenv(Constants.EXCHANGE_NAME);
	private final String QUEUE_NAME = System.getenv(Constants.QUEUE_NAME);

	public DCP_RabbitMQ_Publisher(final String host, final int port, final String username, final String password)
			throws IOException, TimeoutException {
		final ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setHost(host);
		factory.setPort(port);
		System.out.println("username: " + username + " - password: " + password);
		final Connection connection = factory.newConnection();
		this.channel = connection.createChannel();
		System.out.println("EXCHANGE_NAME: " + EXCHANGE_NAME + " - QUEUE_NAME: " + QUEUE_NAME);
		this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		this.channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		this.channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);

	}

	public void onEvent(final ByteBuf event) throws UnsupportedEncodingException, IOException {
		if (DcpMutationMessage.is(event)) {
			System.out.println("Mutation: " + DcpMutationMessage.toString(event));
			final String message = "{" + "\"_id\": \"" + DcpMutationMessage.keyString(event) + "\", \"body\":"
					+ DcpMutationMessage.content(event).toString(CharsetUtil.UTF_8) + "}";
			channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null, message.getBytes("UTF-8"));

		} else if (DcpDeletionMessage.is(event)) {
			System.out.println("Deletion: " + DcpDeletionMessage.toString(event));
		}
	}
}
