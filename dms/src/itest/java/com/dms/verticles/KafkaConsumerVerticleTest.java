package com.dms.verticles;

import com.dms.jackson.model.TransactionEvent;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Max on 14.12.2017.
 */
@RunWith(VertxUnitRunner.class)
public class KafkaConsumerVerticleTest extends AbstractIntegrationTest {

    @Test
    public void testSendingTransactions(TestContext context) {
        final Async async = context.async();

        KafkaProducer<String, String> producer = createKafkaProducer();

        List<JsonObject> transactions = generateTransactions(5);
        Set<String> ids = transactions.stream().map(t -> t.getString("id")).collect(Collectors.toSet());

        final long startTime = System.currentTimeMillis();

        vertx.eventBus().consumer(StorageVerticle.ADDRESS, message -> {
            TransactionEvent value = (TransactionEvent) message.body();
            logger.info("STORAGE CONSUMER | received message: {} ;", value);

            context.assertTrue(ids.remove(value.getOriginalId()));

            if (ids.isEmpty()) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("All {} test transactions have been processed in {}ms", transactions.size(), duration);
                async.complete();
            }
        });

        for (JsonObject transaction : transactions) {
            String message = Json.encode(transaction);

            KafkaProducerRecord<String, String> record =
                    KafkaProducerRecord.create(configs.getKafkaBroker(), message);

            producer.write(record, res -> {
                if (res.succeeded()) {
                    RecordMetadata recordMetadata = res.result();
                    logger.info("Message '{}' sent to topic '{}', partition={}, offset={}",
                            record.value(), recordMetadata.getTopic(), recordMetadata.getPartition(), recordMetadata.getOffset());
                }
            });
        }
    }
}
