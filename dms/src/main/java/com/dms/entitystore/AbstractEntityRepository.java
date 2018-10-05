package com.dms.entitystore;

import com.dms.entity.AbstractEntity;
import com.dms.rxcassandra.SessionObservable;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;


/**
 * Created by volodymyr on 15.12.17.
 */
public abstract class AbstractEntityRepository<T extends AbstractEntity, K> implements EntityManager<T, K> {

    private Logger logger = LoggerFactory.getLogger(AbstractEntityRepository.class);


    protected SessionObservable sessionObservable;

    public AbstractEntityRepository(SessionObservable sessionObservable) {
        this.sessionObservable = sessionObservable;
    }

    public abstract Class<T> getEntityType();

    @Override
    public Observable<Boolean> put(T t) {
        logger.info("Will put data using string: {}", t.saveQuery());
        return sessionObservable.execute(t.saveQuery())
                .map(rs -> rs.isFullyFetched());

    }

    @Override
    public Observable<T> get(K k) {
        return Observable.error(new NotImplementedException("Not Implemented Yet"));

    }

    @Override
    public void delete(K k) {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public void remove(T t) {
        throw new NotImplementedException("Not implemented yet, please implement");
    }


    @Override
    public Observable<T> getAll() {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public Observable<T> get(Collection<K> collection) {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public void delete(Collection<K> collection) {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public void remove(Collection<T> collection) {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public void put(Collection<T> collection) {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public Observable<T> find(String s) {

        return sessionObservable.execute(s)
                .flatMap(r -> Observable.from(r::iterator))
                .flatMap(r -> {
                    try {
                        return Observable.just((T) getEntityType().newInstance().withRow(r));
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                });
    }



    public void createStorage(String query) {
        //execute sync
        sessionObservable.getSession().execute(query);
    }

    public void createColumnFamilyIfNotExists(String query) {
        this.createStorage(query);
    }

    @Override
    public void deleteStorage() {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public void truncate() {
        throw new NotImplementedException("Not implemented yet, please implement");
    }

    @Override
    public void commit() {
        throw new NotImplementedException("Not implemented yet, please implement");
    }
}