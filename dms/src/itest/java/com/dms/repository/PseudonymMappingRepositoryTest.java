package com.dms.repository;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.dms.entity.PseudonymMapping;
import com.dms.rxcassandra.SessionObservable;
import com.dms.verticles.RestVerticle;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

public class PseudonymMappingRepositoryTest {

    private Logger logger = LoggerFactory.getLogger(RestVerticle.class);

    private PseudonymMappingRepository pseudonymMappingRepository;

    private SessionObservable sessionObs;

    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("cql/init_dataset.cql", "dms"));

    @Before
    public void setUp() {
        Cluster cluster = cassandraCQLUnit.getCluster();
        Session session = cluster.connect("dms");
        sessionObs = new SessionObservable(session);

        pseudonymMappingRepository = new PseudonymMappingRepository(sessionObs);
    }

    @Test
    public void testSavingMappings() throws Exception {
        int count = 50;
        final Set<PseudonymMapping> mappings = IntStream.range(1, count)
                .mapToObj(i -> new PseudonymMapping(UUID.randomUUID(), String.valueOf(i), UUID.randomUUID().toString()))
                .collect(Collectors.toSet());

        CountDownLatch writeLatch = new CountDownLatch(count - 1);

        mappings.forEach(mapping -> {
            pseudonymMappingRepository.put(mapping).subscribe(res -> {
                logger.info("Mapping {} was saved with result {}", mapping, res);
                writeLatch.countDown();
            });
        });

        writeLatch.await();

        CountDownLatch readLatch = new CountDownLatch(1);

        pseudonymMappingRepository.find(PseudonymMapping.GET_ALL_QUERY).subscribe(new Subscriber<PseudonymMapping>() {
            @Override
            public void onCompleted() {
                readLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.error("error happend", e);
            }

            @Override
            public void onNext(PseudonymMapping mapping) {
                logger.info("Mapping was read from db: {}", mapping);
                assertTrue(mappings.remove(mapping));
            }
        });

        readLatch.await();
        assertTrue(mappings.isEmpty());
    }
}
