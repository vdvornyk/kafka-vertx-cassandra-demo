package com.dms.verticles;

import com.dms.entity.InvalidTransaction;
import com.dms.repository.InvalidTransactionRepository;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Subscriber;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Max on 14.12.2017.
 */
@RunWith(VertxUnitRunner.class)
public class InvalidTransactionTest extends AbstractIntegrationTest {

    @Test
    public void testSendingTransactions(TestContext context) throws InterruptedException {

        InvalidTransactionRepository invalidTransactionRepository = new InvalidTransactionRepository(sessionObs);

        final Async async = context.async();

        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configs.getKafkaBroker());
        config.put(ProducerConfig.ACKS_CONFIG, "1");

        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config, String.class, String.class);

        List<String> messages = generateInvalidTransactions();


        //sending data to storage
        for (String transaction : messages) {

            KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(configs.getKafkaTransactionTopic(), transaction);

            producer.write(record, res -> {
                if (res.succeeded()) {
                    RecordMetadata recordMetadata = res.result();
                    logger.info("Message '{}' sent to topic '{}', partition={}, offset={}",
                            record.value(), recordMetadata.getTopic(), recordMetadata.getPartition(), recordMetadata.getOffset());
                }
            });
        }

        //waiting till processd
        Thread.sleep(3000L);
        CountDownLatch latch = new CountDownLatch(messages.size());

        List<InvalidTransaction> transactionsSavedToDB = new ArrayList<>();

        invalidTransactionRepository.find(InvalidTransaction.GET_ALL_QUERY)
                .subscribe(new Subscriber<InvalidTransaction>() {
                    @Override
                    public void onCompleted() {
                        logger.info("INVALID Transactions found :{}", transactionsSavedToDB.size());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(InvalidTransaction invalidTransaction) {
                        latch.countDown();
                        transactionsSavedToDB.add(invalidTransaction);
                    }
                });

        latch.await();

        Assert.assertEquals(messages.size(), transactionsSavedToDB.size());
        transactionsSavedToDB.forEach(t -> logger.info(t.toString()));

        //complete verticles
        async.complete();

    }

    public List<String> generateInvalidTransactions() {

        JsonObject transaction1 = new JsonObject();
        transaction1.put("idd", "0x" + Integer.toHexString(22));

        JsonObject transaction2 = new JsonObject();
        transaction2.put("id", "0x" + Integer.toHexString(33));

        JsonObject payment = new JsonObject();

        payment.put("from", UUID.randomUUID().toString());
        payment.put("to", UUID.randomUUID().toString());
        payment.put("currency", "EUR");
        payment.put("createdAt", Instant.now().toString());

        transaction2.put("payment", payment);

        JsonObject transaction3 = new JsonObject();
        transaction3.put("id", "0x" + Integer.toHexString(44));
        JsonObject payment3 = payment.copy();
        payment3.put("amount", "100");
        transaction3.put("payment", payment3);

        JsonObject extraData = new JsonObject();
        extraData.put("payload", "invalid");
        transaction3.put("extraData", extraData);

        return Arrays.asList(
                "msg1",
                "not valid msg2",
                Json.encode(transaction1),
                Json.encode(transaction2),
                Json.encode(transaction3)
        );
    }

}
