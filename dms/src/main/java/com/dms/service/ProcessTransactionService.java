package com.dms.service;

import com.dms.jackson.model.TransactionEvent;
import com.dms.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.xml.bind.ValidationException;


/**
 * Created by VolodymyrD on 12/19/17.
 *
 * This class can be extended with more custom validation
 */
public class ProcessTransactionService {

    private Logger logger = LoggerFactory.getLogger(ProcessTransactionService.class);

    private EncryptionService encryptionService = new EncryptionService();

    public Observable<TransactionEvent> parseMessage(String message) {
        return JSONUtil.parseObservable(message, TransactionEvent.class);
    }

    public Observable<TransactionEvent> validateEvent(TransactionEvent transactionEvent){
        return Observable.create(subscriber -> {
            try{
                transactionEvent.validate();
            } catch (ValidationException e) {
                subscriber.onError(e);
            }
            subscriber.onNext(transactionEvent);
            subscriber.onCompleted();
        });
    }

    public Observable<TransactionEvent> decryptPayload(TransactionEvent transactionEvent) {
        return Observable.create(subscriber -> {
            if (transactionEvent.getExtraData() != null) {
                String payload = transactionEvent.getExtraData().getPayload();
                if (payload != null) {
                    try {
                        String decrypted = encryptionService.decrypt(payload, EncryptionService.DEFAULT_SECRET);
                        transactionEvent.getExtraData().setPayload(decrypted);
                    } catch (Exception e) {
                        logger.error("Failed to decrypt payload: " + payload, e);
                        subscriber.onError(new RuntimeException("Failed to decrypt payload", e));
                    }
                }
            }
            subscriber.onNext(transactionEvent);
            subscriber.onCompleted();
        });
    }
}
