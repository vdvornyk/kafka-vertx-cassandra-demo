package com.dms.verticles;

import com.dms.configs.ConnectionConfig;
import com.dms.entity.InvalidTransaction;
import com.dms.jackson.model.TransactionEvent;
import com.dms.repository.InvalidTransactionRepository;
import com.dms.service.ProcessTransactionService;
import io.vertx.core.Future;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.rxjava.core.AbstractVerticle;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by volodymyr on 04.08.17.
 */
public class KafkaConsumerVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(KafkaConsumerVerticle.class);

    private InvalidTransactionRepository invalidTransactionRepository;

    private ProcessTransactionService processTransactionService;

    private KafkaConsumer<String, String> consumer;

    private ConnectionConfig configs;

    public KafkaConsumerVerticle(InvalidTransactionRepository invalidTransactionRepository, ProcessTransactionService processTransactionService, ConnectionConfig configs ) {
        this.invalidTransactionRepository = invalidTransactionRepository;
        this.processTransactionService = processTransactionService;
        this.configs = configs;
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configs.getKafkaBroker());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, configs.getKafkaTransactionGroupId());

        consumer = KafkaConsumer.create(vertx.getDelegate(), config, String.class, String.class);
        consumer.handler(record -> {
            logger.debug("Received record [key={}, value={}, partition={}, offset={}] from Kafka topic '{}'",
                    record.key(), record.value(), record.partition(), record.offset(), configs.getKafkaTransactionTopic());
            consumeKafkaMsg(record.value());
        });

        consumer.subscribe(configs.getKafkaTransactionTopic(), res -> {
            if (res.succeeded()) {
                logger.info("Subscribed to Kafka topic '{}'", configs.getKafkaTransactionTopic());
            } else {
                logger.error("Failed to subscribe to Kafka topic", res.cause());
                fut.fail(res.cause());
            }
        });
        consumer.partitionsAssignedHandler(topicPartitions -> {
            logger.info("Kafka partitions are assigned: " + Arrays.toString(topicPartitions.toArray()));
            fut.complete();
        });
    }

    private void consumeKafkaMsg(String message) {
        processTransactionService
                .parseMessage(message)
                .flatMap(processTransactionService::validateEvent)
                .flatMap(processTransactionService::decryptPayload)
                .subscribe(new Subscriber<TransactionEvent>() {
                    @Override
                    public void onCompleted() {
                        logger.info("KAFKA CONSUMER | Message processs SUCCESSFULLY {}", message);
                    }

                    @Override
                    public void onError(Throwable e) {
                        String errorMsg = "KAFKA CONSUMER | Exception happened during processing incoming message | Reason: " + e.getMessage();
                        errorMsg = errorMsg.replaceAll("'", "\"");

                        logger.error(errorMsg);
                        invalidTransactionRepository
                                .put(new InvalidTransaction(UUIDGen.getRandomTimeUUIDFromMicros(new Date().getTime()), message, errorMsg))
                                .subscribe();
                    }

                    @Override
                    public void onNext(TransactionEvent transactionEvent) {
                        String pseudonym = generatePseudonym();
                        Pair mapping = Pair.of(transactionEvent.getId(), pseudonym);

                        //sending to 1 msg
                        vertx.eventBus().send(PseudonymVerticle.ADDRESS, mapping);

                        //sending to 2 msg
                        transactionEvent.overrideId(pseudonym);
                        vertx.eventBus().send(StorageVerticle.ADDRESS, transactionEvent);
                    }
                });

    }

    private String generatePseudonym() {
        return UUID.randomUUID().toString();
    }
}
