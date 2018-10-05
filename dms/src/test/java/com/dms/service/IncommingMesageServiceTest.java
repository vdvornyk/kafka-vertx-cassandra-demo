package com.dms.service;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.dms.repository.IncommingMessageRepository;
import com.dms.entity.IncommingMessage;
import com.dms.rxcassandra.SessionObservable;
import com.dms.verticles.RestVerticle;
import org.apache.cassandra.utils.UUIDGen;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by volodymyr on 15.12.17.
 */

public class IncommingMesageServiceTest {
    private Logger logger = LoggerFactory.getLogger(RestVerticle.class);

    private IncommingMesageService mesageService;
    private IncommingMessageRepository messageDao;

    private SessionObservable sessionObs;

    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("cql/init_dataset.cql", "dms"));

    @Before
    public void setUp() throws Exception {


        Cluster cluster = cassandraCQLUnit.getCluster();
        Session session = cluster.connect("dms");

        sessionObs = new SessionObservable(session);

        messageDao = new IncommingMessageRepository(sessionObs);
        mesageService = new IncommingMesageService(messageDao);

    }

    @Test
    public void shouldSaveAndReadLatestMessages() throws Exception {
        List<IncommingMessage> messageList = IntStream.range(1, 20)
                .mapToObj(i -> new IncommingMessage(UUIDGen.getRandomTimeUUIDFromMicros(new Date().getTime()), "Generated message : " + i))
                .collect(Collectors.toList());

        CountDownLatch writeLatch = new CountDownLatch(19);

        messageList.forEach(msg -> {
            mesageService.saveMessages(msg).subscribe(result -> {
                logger.info("Message id={} was saved with result {}", msg.getId(), result);
                writeLatch.countDown();
            });
        });

        writeLatch.await();

        CountDownLatch readLatch = new CountDownLatch(1);

        mesageService.getAllMessages().subscribe(new Subscriber<IncommingMessage>() {
            @Override
            public void onCompleted() {
                readLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.error("error happend", e);
            }

            @Override
            public void onNext(IncommingMessage message) {
                logger.info("Message was read from db: {}", message.toString());
            }
        });

        readLatch.await();

    }
}