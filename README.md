# RabbitMQ Couchbase Connector

## Introduction

This repo contains a Java-based connector to stream Couchbase server's DCP events to [RabbitMQ](https://www.rabbitmq.com/) message broker.

### Main features

- Streaming Couchbase's DCP data events to RabbitMQ exchange
- filtering `_sync*` documents events
- DCP status persistence

## How to build

To build the project should be as simple as

```mvn package```

If everything goes as expected you should find in `taget/` directory the following packages:

```
target
├── CbRabbitConnector-jar-with-dependencies.jar
├── CbRabbitConnector.jar
```

The `CbRabbitConnector-jar-with-dependencies.jar` is a full-packaged jar with all dependencies. Just move to your target machine and run.

## Running the project
Before running the connector you must provide some env vars:

```shell script
export COUCHBASE_CLUSTER="<couchbase cluster domain or IP>";
export COUCHBASE_BUCKET="<bucket name>";
export COUCHBASE_BUCKET_USER="<bucket user>";
export COUCHBASE_BUCKET_PASSWORD="<bucket password>";
export COUCHBASE_CONN_TIMEOUT="10000";  
export COUCHBASE_PERSISTENCE_POLL_INTV="10000";
export COUCHBASE_FLOWCTRL_BUFF_BYTES="128000000";
export RABBIT_HOST="<rabbit cluster domain or IP>";
export RABBIT_PORT="<rabbit cluster port>";
export RABBIT_USER="<rabbit cluster user>";
export RABBIT_PASSWORD="<rabbit cluster user's password>";
export EXCHANGE_NAME="exchange name";
export QUEUE_NAME="queue name";
export STATE_FILE_PATH="<path to file where DCP stream state will be saved>";
export STATE_SAVE_DELAY_SEC="10";
```

Once all vars are set up, run package with:

```shell script
java -jar CbRabbitConnector-jar-with-dependencies.jar
```

## TODO

- Improve exception handling
- Add DCP events filtering capabilities
- Add Logging
- Testing

## Disclaimer

This project is not yet tested, use your own risk.

## License
This projects is released under the [MIT license](LICENSE.txt).

## Credits
This work is inspired by [Christian Käslin's](https://github.com/ckaeslin) [couchbase-rabbitmq-connector](https://github.com/etops/couchbase-rabbitmq-connector) and from [Couchbase](couchbase) [DCP Client](https://github.com/couchbase/java-dcp-client) and [Kafka connector](https://github.com/couchbase/kafka-connect-couchbase) 

## Contributing

Any help appreciated, please contact me if you are interested in contributing to the project