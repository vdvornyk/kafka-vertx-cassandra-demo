package com.dms.repository;

import com.dms.entity.IncommingMessage;
import com.dms.entitystore.AbstractEntityRepository;
import com.dms.rxcassandra.SessionObservable;

import java.util.UUID;


/**
 * Created by volodymyr on 15.12.17.
 */
public class IncommingMessageRepository extends AbstractEntityRepository<IncommingMessage, UUID> {

    public IncommingMessageRepository(SessionObservable sessionObservable) {
        super(sessionObservable);

        createColumnFamilyIfNotExists(IncommingMessage.INIT_QUERY);
    }

    @Override
    public Class<IncommingMessage> getEntityType() {
        return IncommingMessage.class;
    }


}
