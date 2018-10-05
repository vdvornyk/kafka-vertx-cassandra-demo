FROM openjdk:8u151

ENV ENV_CASSANDRA_KEYSPACE "dms"

#Cassandra service
ENV ENV_CASSANDRA_HOST "cassandra-cassandra"
ENV ENV_CASSANDRA_PORT "9042"

#Check this query ; not sure what configs we should provide here
ENV ENV_CASSANDRA_INIT_QUERY "CREATE KEYSPACE IF NOT EXISTS dms WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 };"

#Kafka service
ENV ENV_KAFKA_BROKER "kafka-kafka:9092"
ENV ENV_KAFKA_TRANSACTIONS_TOPIC  "com.dms.transactions"
ENV ENV_TRANSACTIONS_GROUP_ID "transactions"

RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY ./dms-0.0.1-SNAPSHOT-fat.jar /opt/app

ENTRYPOINT ["java","-jar","dms-0.0.1-SNAPSHOT-fat.jar"]
