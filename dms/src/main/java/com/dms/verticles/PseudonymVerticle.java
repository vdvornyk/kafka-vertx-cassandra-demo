package com.dms.verticles;

import com.dms.entity.PseudonymMapping;
import com.dms.repository.PseudonymMappingRepository;
import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by volodymyr on 04.08.17.
 */
public class PseudonymVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(PseudonymVerticle.class);

    public static final String ADDRESS = "pseudonym.consumer";

    private PseudonymMappingRepository pseudonymMappingRepository;

    public PseudonymVerticle(PseudonymMappingRepository pseudonymMappingRepository) {
        this.pseudonymMappingRepository = pseudonymMappingRepository;
    }

    @Override
    public void start(Future<Void> fut) {
        vertx.eventBus().consumer(ADDRESS).toObservable().map(Message::body).subscribe(e -> storeData((Pair<String, String>) e));
        fut.complete();
    }

    private void storeData(Pair<String, String> pair) {
        logger.info("Pseudonym CONSUMER | received message: {} ;", pair.toString());

        pseudonymMappingRepository
                .put(new PseudonymMapping(UUIDGen.getRandomTimeUUIDFromMicros(new Date().getTime()), pair.getLeft(), pair.getRight()))
                .subscribe();
    }


}
