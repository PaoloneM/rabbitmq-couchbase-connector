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

### Environment
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
export STATE_FILE_PATH="<path to file where DCP stream state will be saved>";
export STATE_SAVE_DELAY_SEC="10";
```

### Defining the RabbitMQ message key

You can define the message key by setting the `MESSAGE_KEY` env. If `MESSAGE_KEY` is not defined, the connector will try to build the key from `MESSAGE_KEY_TEMPLATE`, in wich you can store a string with a single variable substitution like `MY.%s.KEY`, by substituting the palceholder with the content of a property of message payload specified by `MESSAGE_KEY_FIELD`.
If none specified, `UNDEFINED` will be used as message key.

Example

Let's say you have the following environment set:

```
export MESSAGE_KEY_TEMPLATE=DOC.%s.CHANGE
export MESSAGE_KEY_FIELD=type
```

and you receive amutation event of this doc:
```
{
    "value1": "lore ipsum",
    "value2": "Aliquam amet erat"
    "type": "NOTE"
}
```

You should get the following RabbitMQ message key:
```
DOC.NOTE.CHANGE
```

### Run
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