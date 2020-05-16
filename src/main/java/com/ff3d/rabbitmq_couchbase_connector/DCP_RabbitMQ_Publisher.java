package com.ff3d.rabbitmq_couchbase_connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.couchbase.client.dcp.message.DcpDeletionMessage;
import com.couchbase.client.dcp.message.DcpMutationMessage;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.util.CharsetUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.BlockedListener;

import org.json.JSONObject;
import org.json.JSONTokener;

public class DCP_RabbitMQ_Publisher implements DCPEventListener {

	private Channel channel;
	private final String EXCHANGE_NAME = System.getenv(Constants.EXCHANGE_NAME);
	private String messageKey = System.getenv(Constants.MESSAGE_KEY);
	private final String messageKeyTemplate = System.getenv(Constants.MESSAGE_KEY_TEMPLATE);
	private final String messageKeyField = System.getenv(Constants.MESSAGE_KEY_FIELD);
	private Boolean renderKey = false;
	private int errorCounter = 0;
	final ConnectionFactory factory;
	private Connection connection;
	private BlockedListener blockedListener = new BlockedListener() {

		public void handleUnblocked() throws IOException {
			System.out.println("Rabbit connection unblocked");
		}

		public void handleBlocked(String reason) throws IOException {
			errorCounter++;
			System.err.println("Rabbit connection blocked");
			System.exit(5);
		}
	};
	private ShutdownListener shutdownListener = new ShutdownListener() {

		public void shutdownCompleted(ShutdownSignalException cause) {
			System.out.println("Rabbit connection closed, reconnect");
			connection.abort();
			connection.removeBlockedListener(blockedListener);
			connection.removeShutdownListener(this);
			errorCounter++;
			initConnection();
		}
	};
	private String docJson;
	private JSONTokener tokener;
	private JSONObject document;
	private String value;

	public DCP_RabbitMQ_Publisher(final String host, final int port, final String username, final String password) {
		factory = new ConnectionFactory();
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setHost(host);
		factory.setPort(port);
		factory.setRequestedHeartbeat(Constants.RMQ_HEARTBEAT);
		factory.setConnectionTimeout(Constants.RMQ_TIMEOUT);
		factory.setHandshakeTimeout(Constants.RMQ_TIMEOUT);
		HashMap<String, Object> clientProperties = new HashMap<String, Object>();
		clientProperties.put("connection_name", Constants.CLIENT_NAME + " - " + System.getenv("HOSTNAME"));
		factory.setClientProperties(clientProperties);
		System.out.println("username: " + username + " - password: " + password);
		initConnection();
		System.out.println("EXCHANGE_NAME: " + EXCHANGE_NAME);
		/**
		 * Check message key logic: if specified, MESSAGE_KEY is used as message key, if
		 * not, MESSAGE_KEY_TEMPLATE is rendered with the value of the message field
		 * specified by MESSAGE_KEY_FIELD. If none of previous is specified, a default
		 * value is set
		 */
		if (messageKey == null || messageKey.length() < 1) {
			System.out.println(
					"Message key must be rendered - template: " + messageKeyTemplate + " - field: " + messageKeyField);
			renderKey = true;
		}
	}

	private void initConnection() {
		System.out.println("Try connection to Rabbit, error count: " + errorCounter);
		if (errorCounter >= Constants.RMQ_CONN_MAX_RETRY) {
			System.out.println("Connection errors exceeded, abort");
			System.exit(6);
		}
		try {
			this.connection = factory.newConnection();
			this.connection.addBlockedListener(blockedListener);
			this.connection.addShutdownListener(shutdownListener);
			this.channel = connection.createChannel();
			errorCounter = 0;
		} catch (Exception e) {
			e.printStackTrace();
			errorCounter++;
			Timer timer = new Timer();
			TimerTask myTask = new TimerTask() {
				@Override
				public void run() {
					initConnection();
				}
			};
			timer.schedule(myTask, 5000, 5000);
		}
	}

	public void onEvent(final ByteBuf event) throws UnsupportedEncodingException, IOException {
		if (DcpMutationMessage.is(event)) {
			docJson = DcpMutationMessage.content(event).toString(CharsetUtil.UTF_8);
			System.out.println("Mutation: " + docJson);
			if (renderKey) {
				tokener = new JSONTokener(docJson);
				document = new JSONObject(tokener);
				value = "";
				try {
					value = document.getString(messageKeyField);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				if (value == null || value.equals("")) {
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
		} else {
			System.err.println("Unknown data event");
		}
		docJson = null;
		tokener = null;
		document = null;
		value = null;
	}

}
