package com.dms.repository;

import com.dms.entity.PseudonymMapping;
import com.dms.entitystore.AbstractEntityRepository;
import com.dms.rxcassandra.SessionObservable;

import java.util.UUID;

public class PseudonymMappingRepository extends AbstractEntityRepository<PseudonymMapping, UUID> {

    public PseudonymMappingRepository(SessionObservable sessionObservable) {
        super(sessionObservable);

        createColumnFamilyIfNotExists(PseudonymMapping.INIT_QUERY);
    }

    @Override
    public Class<PseudonymMapping> getEntityType() {
        return PseudonymMapping.class;
    }


}
