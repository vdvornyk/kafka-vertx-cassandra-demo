package com.dms;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.dms.configs.ConnectionConfig;
import com.dms.repository.IncommingMessageRepository;
import com.dms.jackson.model.TransactionEvent;
import com.dms.jackson.model.ExtraData;
import com.dms.jackson.model.Payment;
import com.dms.jackson.model.example.Command;
import com.dms.jackson.model.example.Transaction;
import com.dms.repository.InvalidTransactionRepository;
import com.dms.repository.PseudonymMappingRepository;
import com.dms.rxcassandra.SessionObservable;
import com.dms.service.IncommingMesageService;
import com.dms.service.ProcessTransactionService;
import com.dms.verticles.*;
import com.dms.codec.DefaultCodec;
import io.vertx.rxjava.core.Vertx;
import com.datastax.driver.core.Session;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Client {

    private static Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {


        ConnectionConfig connectionConfig = new ConnectionConfig();
        logger.info("DMS Application going to start with next configuration [{}]", connectionConfig.toString());

        //TODO: refactor with DI Framework(Guice)
        SessionObservable sessionObservable = new SessionObservable(getCassandraSession(connectionConfig));
        IncommingMessageRepository dao = new IncommingMessageRepository(sessionObservable);
        IncommingMesageService service = new IncommingMesageService(dao);

        InvalidTransactionRepository invalidTransactionRepository = new InvalidTransactionRepository(sessionObservable);
        PseudonymMappingRepository pseudonymMappingRepository = new PseudonymMappingRepository(sessionObservable);
        ProcessTransactionService processTransactionService = new ProcessTransactionService();
        //---------

        Vertx rxVertx = Vertx.vertx();


        io.vertx.core.Vertx vertx = rxVertx.getDelegate();
        setupDefaultCodecs(vertx);

        vertx.deployVerticle(new RestVerticle(service));

        vertx.deployVerticle(new KafkaConsumerVerticle(invalidTransactionRepository, processTransactionService, connectionConfig));

        vertx.deployVerticle(new PseudonymVerticle(pseudonymMappingRepository));
        vertx.deployVerticle(new StorageVerticle(service));

    }

    public static void setupDefaultCodecs(io.vertx.core.Vertx vertx) {

        vertx.eventBus().registerDefaultCodec(ArrayList.class, new DefaultCodec<>(ArrayList.class.getName()));
        vertx.eventBus().registerDefaultCodec(String.class, new DefaultCodec<>(String.class.getName()));
        vertx.eventBus().registerDefaultCodec(Long.class, new DefaultCodec<>(Long.class.getName()));
        vertx.eventBus().registerDefaultCodec(ImmutablePair.class, new DefaultCodec<>(Pair.class.getName()));

        //example
        vertx.eventBus().registerDefaultCodec(Command.class, new DefaultCodec<>(Command.class.getName()));
        vertx.eventBus().registerDefaultCodec(Transaction.class, new DefaultCodec<>(Transaction.class.getName()));

        //model:
        vertx.eventBus().registerDefaultCodec(TransactionEvent.class, new DefaultCodec<>(TransactionEvent.class.getName()));
        vertx.eventBus().registerDefaultCodec(ExtraData.class, new DefaultCodec<>(ExtraData.class.getName()));
        vertx.eventBus().registerDefaultCodec(Payment.class, new DefaultCodec<>(Payment.class.getName()));
    }

    /**
     * Get localhost session; this should be implemented later to read data from ZooKeeper configs
     *
     * @return
     */
    public static Session getCassandraSession(ConnectionConfig config) {
        Cluster cluster = Cluster.builder()
                .addContactPoint(config.getCassandraHost())
                .withPort(config.getCassandraPort())
                .build();

        Metadata metadata = cluster.getMetadata();
        logger.info("Connected to cluster: {}", metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            logger.info("Datacenter: {}; Host: {}; Rack: {}", host.getDatacenter(), host.getAddress(), host.getRack());
        }

        Session initial = cluster.connect();
        initial.execute(config.getInitKeyspaceQuery());
        initial.close();


        return cluster.connect(config.getCassandraKeyspace());
    }

}
