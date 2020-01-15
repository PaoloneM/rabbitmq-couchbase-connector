package com.ff3d.rabbitmq_couchbase_connector;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.couchbase.client.core.env.NetworkResolution;
import com.couchbase.client.dcp.Client;
import com.couchbase.client.dcp.DefaultConnectionNameGenerator;
import com.couchbase.client.dcp.StreamFrom;
import com.couchbase.client.dcp.StreamTo;
import com.couchbase.client.dcp.config.CompressionMode;
import com.couchbase.client.dcp.config.DcpControl;
import com.couchbase.client.dcp.state.StateFormat;
import rx.CompletableSubscriber;
import rx.Subscription;


/**
 * This class handles DCP connection to Couchbase an attaches handlers to events
 * 
 * Author: P. Morgano
 */
public class DCPStream implements ConnectorDataEventHandler.ConnectorDataEventHandlerCallback,
		ConnectorControlEventHandler.ConnectorControlEventHandlerCallback {

	private Client client;
	private DcpStateHelper stateHelper;
	private long lastStateSaveTime = System.currentTimeMillis();
	private final int saveDelay = Integer.parseInt(System.getenv(Constants.STATE_SAVE_DELAY_SEC)) * 1000;

	public void init(List<String> clusters, 
			String bucket, 
			String bucketUsername, 
			String bucketPassword,
			long connectionTimeout, 
			NetworkResolution networkResolution, 
			CompressionMode compressionMode,
			long persistencePollingIntervalMillis, 
			int flowControlBufferBytes,
			String rabbitHost, 
			int rabbitPort,
			String rabbitUsername, 
			String rabbitPassword,
			String stateFilename)
			throws IOException, TimeoutException {

		System.out.println("Cb credentials: " + bucket + " - " + bucketUsername + " - " + bucketPassword);

		this.stateHelper = new DcpStateHelper(stateFilename + "DCP-" + bucket + "-status.json");
		
		this.client = Client.configure()
				.connectionNameGenerator(DefaultConnectionNameGenerator.forProduct("rabbit-connector", "0.0.1"))
				.connectTimeout(connectionTimeout)
				.hostnames(clusters)
				.networkResolution(networkResolution)
				.bucket(bucket)
				.username(bucketUsername)
				.password(bucketPassword)
				.controlParam(DcpControl.Names.ENABLE_NOOP, "true")
				.compression(compressionMode)
				.mitigateRollbacks(persistencePollingIntervalMillis, TimeUnit.MILLISECONDS)
				.flowControl(flowControlBufferBytes)
				.bufferAckWatermark(60)
				.sslEnabled(false)
				.build();
		;

		// Don't do anything with control events in this example
		this.client.controlEventHandler(new ConnectorControlEventHandler(this));

		// Print out Mutations and Deletions
		ConnectorDataEventHandler dataEventHandler = new ConnectorDataEventHandler(this);
		dataEventHandler.addEventListener(
				new DCP_RabbitMQ_Publisher(rabbitHost, rabbitPort, rabbitUsername, rabbitPassword);
		this.client.dataEventHandler(dataEventHandler);

	}

	public void start() throws Exception {
		// Connect the sockets
		this.client.connect().await();
	 
		 // if the persisted file exists recover, if not start from beginning
		 this.client.recoverOrInitializeState(StateFormat.JSON, stateHelper.loadState(), StreamFrom.NOW, StreamTo.INFINITY).await();
	 
		// Start streaming on all partitions
		this.client.startStreaming().await();

	}

	public void stop() throws IOException {
		System.out.println("Shutting down...");
		stateHelper.saveState(client);
		client.disconnect().await();
	}

	public void onDataEventHandled() {
		long now = System.currentTimeMillis();
		System.out.println("Event handled, checking if save state allowed: " + now + " - last saved: " + lastStateSaveTime);
		if (now > lastStateSaveTime + saveDelay){
			try {
				System.out.println("Going to save state " + now);
				stateHelper.saveState(client);
				lastStateSaveTime = now;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void onRollbackMessageReceived(final short partition, long seqNo) {
		client.rollbackAndRestartStream(partition, seqNo)
				.subscribe(new CompletableSubscriber() {
					public void onCompleted() {
						System.out.println("Rollback for partition " + partition + " complete!");
					}

					public void onError(Throwable e) {
						System.err.println("Rollback for partition " + partition + " failed!");
						e.printStackTrace();
						System.exit(2);
					}

					public void onSubscribe(Subscription d) {
					}
				});
	}

}
