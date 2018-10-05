package com.dms.verticles;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.dms.Client;
import com.dms.configs.ConnectionConfig;
import com.dms.repository.InvalidTransactionRepository;
import com.dms.rxcassandra.SessionObservable;
import com.dms.service.ProcessTransactionService;
import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.kafka.client.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by VolodymyrD on 12/19/17.
 */
public class AbstractIntegrationTest {

    protected Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    protected Vertx vertx;
    protected KafkaCluster kafkaCluster;

    protected SessionObservable sessionObs;

    protected ConnectionConfig configs;
    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("cql/init_dataset.cql", "dms"));


    @Before
    public void setUp(TestContext context) throws InterruptedException {
        vertx = Vertx.vertx();
        configs = new ConnectionConfig();
        Client.setupDefaultCodecs(vertx);

        //Setup Cassandra
        Cluster cluster = cassandraCQLUnit.getCluster();
        Session session = cluster.connect("dms");

        sessionObs = new SessionObservable(session);
        InvalidTransactionRepository invalidTransactionRepository = new InvalidTransactionRepository(sessionObs);
        ProcessTransactionService processTransactionService = new ProcessTransactionService();


        File dataDir = Testing.Files.createTestingDirectory("cluster");
        dataDir.deleteOnExit();
        try {
            kafkaCluster = new KafkaCluster()
                    .usingDirectory(dataDir)
                    .withPorts(2181, 9092)
                    .addBrokers(1)
                    .deleteDataPriorToStartup(true)
                    .deleteDataUponShutdown(true)
                    .startup();
        } catch (IOException e) {
            context.fail(e.getCause());
        }

        vertx.deployVerticle(new KafkaConsumerVerticle(invalidTransactionRepository, processTransactionService, configs), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
        kafkaCluster.shutdown();
    }


    protected List<JsonObject> generateTransactions(int count) {
        String[] testPayloads = {
                "U2FsdGVkX1+NbcIEB29R99mFy7DrcglNBPxHwvCBq2v4qZVue+82bezi/+bie5O6KQezOda6dw0HtzaCkkJk0w==",
                "U2FsdGVkX1/mC7Hd9idBVWy8NZe5P1DjP2arrWaLo9Uw1a6SvIFE+n8MRTjwCUVS0JAEZIlKbl2SHwYAgSOtiF7tBFuAa3p+pT0sjauybHd/iXP+xWhLqVQePSMevE7fCZxxVAlOWMwVh45mVT+oCcsQcPT3LaVWOzqjC0A45qXmX2oWs3QHcrzZm4+zBn8tVHEtPBRZNl3OSh+H9GvSfA==",
                "U2FsdGVkX1+g15tEFs5M9cuiPnMGJ8WVzLZUvNI0Psphmt0fOeUsqZ6V8EPBpkci1/QLEsoKoLGfq+iUQpvmk6I6+QE7Zl/h6pC2lGTWdZ2ro5Yh25ySFWP8KA5FgnXZwZ05wrHxH6EhthHO1Ex6Fw=="
        };

        List<JsonObject> transactions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            JsonObject transaction = new JsonObject();
            transaction.put("id", "0x" + Integer.toHexString(i));

            JsonObject payment = new JsonObject();
            payment.put("from", UUID.randomUUID().toString());
            payment.put("to", UUID.randomUUID().toString());
            payment.put("amount", Math.random() * 100);
            payment.put("currency", "EUR");
            payment.put("createdAt", Instant.now().toString());
            transaction.put("payment", payment);

            JsonObject extraData = new JsonObject();
            extraData.put("payload", testPayloads[i % testPayloads.length]);
            transaction.put("extraData", extraData);

            transactions.add(transaction);
        }

        return transactions;
    }

    protected KafkaProducer<String, String> createKafkaProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configs.getKafkaBroker());
        config.put(ProducerConfig.ACKS_CONFIG, "1");

        return KafkaProducer.create(vertx, config, String.class, String.class);
    }
}
