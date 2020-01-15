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

import org.json.JSONObject;
import org.json.JSONTokener;

public class DCP_RabbitMQ_Publisher implements DCPEventListener {

	private final Channel channel;
	private final String EXCHANGE_NAME = System.getenv(Constants.EXCHANGE_NAME);
	private String messageKey = System.getenv(Constants.MESSAGE_KEY);
	private final String messageKeyTemplate = System.getenv(Constants.MESSAGE_KEY_TEMPLATE);
	private final String messageKeyField = System.getenv(Constants.MESSAGE_KEY_FIELD);
	private Boolean renderKey = false;

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
		System.out.println("EXCHANGE_NAME: " + EXCHANGE_NAME);
        /**
         * Check message key logic: if specified, MESSAGE_KEY is used as message key,
         * if not, MESSAGE_KEY_TEMPLATE is rendered with the value of the message field
         * specified by MESSAGE_KEY_FIELD.
         * If none of previous is specified, a default value is set
         */
        if(messageKey == null || messageKey.length() < 1){
			System.out.println("Message key must be rendered - template: " + messageKeyTemplate + " - field: " + messageKeyField);
			renderKey = true;
        } 
	}

	public void onEvent(final ByteBuf event) throws UnsupportedEncodingException, IOException {
		if (DcpMutationMessage.is(event)) {
			String docJson  = DcpMutationMessage.content(event).toString(CharsetUtil.UTF_8);
			System.out.println("Mutation: " + docJson);
			if(renderKey){
				JSONTokener tokener =  new JSONTokener(docJson);
			    JSONObject document = new JSONObject(tokener);
				String value = "";
				try {
					value = document.getString(messageKeyField);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				if(value == null || value.equals("")){
					value = "UNDEFINED";
				}
				messageKey = String.format(messageKeyTemplate, value);
	
				System.out.println("Message key: " + messageKey);
			}

			final String message = "{" + "\"_id\": \"" + DcpMutationMessage.keyString(event) + "\", \"body\":"
					+ DcpMutationMessage.content(event).toString(CharsetUtil.UTF_8) + "}";

			channel.basicPublish(EXCHANGE_NAME, messageKey, null, message.getBytes("UTF-8"));

		} else if (DcpDeletionMessage.is(event)) {
			System.out.println("Deletion: " + DcpDeletionMessage.toString(event));
		}
	}
}
