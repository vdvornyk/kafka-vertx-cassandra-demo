package com.dms.verticles;

import com.dms.entity.IncommingMessage;
import com.dms.jackson.model.TransactionEvent;
import com.dms.service.IncommingMesageService;
import com.dms.util.JSONUtil;
import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.utils.UUIDGen;

import java.util.Date;

/**
 * Created by volodymyr on 04.08.17.
 */
public class StorageVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(StorageVerticle.class);

    public static final String ADDRESS = "storage.consumer";

    public IncommingMesageService mesageService;

    public StorageVerticle(IncommingMesageService mesageService) {
        this.mesageService = mesageService;
    }

    @Override
    public void start(Future<Void> fut) throws Exception {

        vertx.eventBus().consumer(ADDRESS).toObservable().map(Message::body).subscribe(e -> storeData((TransactionEvent) e));
        fut.complete();

    }

    private void storeData(TransactionEvent e) {
        logger.info("STORAGE CONSUMER | received message: {} ;", e);
        mesageService
                .saveMessages(new IncommingMessage(UUIDGen.getRandomTimeUUIDFromMicros(new Date().getTime()), JSONUtil.toJSONString(e).orElse(e.toString())))
                .subscribe();
    }


}
