package com.dms.configs;

import java.util.Map;
import java.util.Optional;

/**
 * Created by volodymyr on 20.12.17.
 */
public class ConnectionConfig {
    //===== ENV VARIABLES =====
    private static final String ENV_CASSANDRA_KEYSPACE = "ENV_CASSANDRA_KEYSPACE";
    private static final String ENV_CASSANDRA_HOST = "ENV_CASSANDRA_HOST";
    private static final String ENV_CASSANDRA_PORT = "ENV_CASSANDRA_PORT";
    private static final String ENV_CASSANDRA_INIT_QUERY = "ENV_CASSANDRA_INIT_QUERY";

    private static final String ENV_KAFKA_BROKER = "ENV_KAFKA_BROKER";
    private static final String ENV_KAFKA_TRANSACTIONS_TOPIC = "ENV_KAFKA_TRANSACTIONS_TOPIC";
    private static final String ENV_TRANSACTIONS_GROUP_ID = "ENV_TRANSACTIONS_GROUP_ID";

    //===== DEFAULT VALUES VARIABLES =====
    public static final String DEFAULT_KEYSPACE = "dms";
    public static final String DEFAULT_CASSANDRA_HOST = "127.0.0.1";
    public static final String DEFAULT_CASSANDRA_PORT = "9042";

    public static final String DEFAULT_KAFKA_BROKER = "localhost:9092";
    public static final String DEFAULT_KAFKA_TRANSACTIONS_TOPIC = "com.dms.transactions";
    public static final String DEFAULT_KAFKA_TRANSACTIONS_GROUP_ID = "transactions";

    //===== ACTUAL VALUES
    private String cassandraKeyspace;

    private String cassandraHost;
    private Integer cassandraPort;

    private String kafkaBroker;
    private String kafkaTransactionTopic;
    private String kafkaTransactionGroupId;

    private String initKeyspaceQuery;

    public ConnectionConfig() {
        Map<String, String> envVariables = System.getenv();

        cassandraKeyspace = Optional.ofNullable(envVariables.get(ENV_CASSANDRA_KEYSPACE)).orElse(DEFAULT_KEYSPACE);
        cassandraHost = Optional.ofNullable(envVariables.get(ENV_CASSANDRA_HOST)).orElse(DEFAULT_CASSANDRA_HOST);

        String envCassandraPort = Optional.ofNullable(envVariables.get(ENV_CASSANDRA_PORT)).orElse(DEFAULT_CASSANDRA_PORT);

        try {
            cassandraPort = Integer.parseInt(envCassandraPort);
        } catch (Exception ex) {
            cassandraPort = Integer.parseInt(DEFAULT_CASSANDRA_HOST);
        }

        String defaultInitQuery = "CREATE KEYSPACE IF NOT EXISTS " + cassandraKeyspace + " WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 1 };";

        initKeyspaceQuery = Optional.ofNullable(envVariables.get(ENV_CASSANDRA_INIT_QUERY)).orElse(defaultInitQuery);


        kafkaBroker = Optional.ofNullable(envVariables.get(ENV_KAFKA_BROKER)).orElse(DEFAULT_KAFKA_BROKER);
        kafkaTransactionTopic = Optional.ofNullable(envVariables.get(ENV_KAFKA_TRANSACTIONS_TOPIC)).orElse(DEFAULT_KAFKA_TRANSACTIONS_TOPIC);
        kafkaTransactionGroupId = Optional.ofNullable(envVariables.get(ENV_TRANSACTIONS_GROUP_ID)).orElse(DEFAULT_KAFKA_TRANSACTIONS_GROUP_ID);

    }


    public String getCassandraKeyspace() {
        return cassandraKeyspace;
    }

    public String getCassandraHost() {
        return cassandraHost;
    }

    public Integer getCassandraPort() {
        return cassandraPort;
    }

    public String getKafkaBroker() {
        return kafkaBroker;
    }

    public String getKafkaTransactionTopic() {
        return kafkaTransactionTopic;
    }

    public String getKafkaTransactionGroupId() {
        return kafkaTransactionGroupId;
    }

    public String getInitKeyspaceQuery() {
        return initKeyspaceQuery;
    }

    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "cassandraKeyspace='" + cassandraKeyspace + '\'' +
                ", cassandraHost='" + cassandraHost + '\'' +
                ", cassandraPort='" + cassandraPort + '\'' +
                ", kafkaBroker='" + kafkaBroker + '\'' +
                ", kafkaTransactionTopic='" + kafkaTransactionTopic + '\'' +
                ", kafkaTransactionGroupId='" + kafkaTransactionGroupId + '\'' +
                ", initKeyspaceQuery='" + initKeyspaceQuery + '\'' +
                '}';
    }
}
