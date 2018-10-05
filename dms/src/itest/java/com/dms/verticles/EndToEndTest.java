package com.dms.verticles;

import com.dms.entity.PseudonymMapping;
import com.dms.jackson.model.TransactionEvent;
import com.dms.repository.IncommingMessageRepository;
import com.dms.repository.PseudonymMappingRepository;
import com.dms.service.IncommingMesageService;
import com.dms.util.JSONUtil;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(VertxUnitRunner.class)
public class EndToEndTest extends AbstractIntegrationTest {

    private IncommingMesageService incommingMesageService;
    private PseudonymMappingRepository pseudonymMappingRepository;

    @Override
    public void setUp(TestContext context) throws InterruptedException {
        super.setUp(context);
        incommingMesageService = new IncommingMesageService(new IncommingMessageRepository(sessionObs));
        vertx.deployVerticle(new StorageVerticle(incommingMesageService), context.asyncAssertSuccess());

        pseudonymMappingRepository = new PseudonymMappingRepository(sessionObs);
        vertx.deployVerticle(new PseudonymVerticle(pseudonymMappingRepository), context.asyncAssertSuccess());
    }

    @Test
    public void testHappyPath() throws InterruptedException {
        JsonObject transaction = generateTransactions(1).get(0);

        KafkaProducer<String, String> producer = createKafkaProducer();

        KafkaProducerRecord<String, String> record =
                KafkaProducerRecord.create(configs.getKafkaTransactionTopic(), Json.encode(transaction));

        producer.write(record, res -> {
            if (res.succeeded()) {
                RecordMetadata recordMetadata = res.result();
                logger.info("Message '{}' sent to topic '{}', partition={}, offset={}",
                        record.value(), recordMetadata.getTopic(), recordMetadata.getPartition(), recordMetadata.getOffset());
            }
        });

        Thread.sleep(1000);

        // find incomingMessage
        incommingMesageService.getAllMessages().subscribe(incomingMessage -> {
                logger.info("Message was read from db: {}", incomingMessage.toString());
                JSONUtil.parseObservable(incomingMessage.getMessage(), TransactionEvent.class)
                        .subscribe(event -> {

                            // find pseudonym mapping
                            pseudonymMappingRepository.find(PseudonymMapping.GET_ALL_QUERY).subscribe(mapping -> {
                                logger.info("Mapping was read from db: {}", mapping);

                                // check id->pseudonym mapping logic
                                assertEquals(transaction.getString("id"), mapping.getTransactionId());
                                assertEquals(mapping.getPseudonym(), event.getId());
                                assertNull(event.getOriginalId());
                            });
                        });
        });
    }
}
