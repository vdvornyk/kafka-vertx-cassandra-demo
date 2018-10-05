package com.dms.repository;

import com.dms.entity.InvalidTransaction;
import com.dms.entitystore.AbstractEntityRepository;
import com.dms.rxcassandra.SessionObservable;

import java.util.UUID;

/**
 * Created by VolodymyrD on 12/19/17.
 */
public class InvalidTransactionRepository extends AbstractEntityRepository<InvalidTransaction, UUID> {

    public InvalidTransactionRepository(SessionObservable sessionObservable) {
        super(sessionObservable);

        createColumnFamilyIfNotExists(InvalidTransaction.INIT_QUERY);
    }

    @Override
    public Class<InvalidTransaction> getEntityType() {
        return InvalidTransaction.class;
    }
}
