package com.dms.entitystore;

import rx.Observable;

import java.util.Collection;
import java.util.Map;

/**
 * @param <T> entity type 
 * @param <K> rowKey type
 */
public interface EntityManager<T, K> {

	/**
	 * write entity to cassandra with mapped rowId and columns
	 * @param entity entity object
	 */
	public Observable<Boolean> put(T entity);
	
	/**
	 * fetch whole row and construct entity object mapping from columns
	 * @param id row key
	 * @return entity object. null if not exist
	 */
	public Observable<T> get(K id);


    /**
     * delete the whole row by id
     * @param id row key
     */
    public void delete(K id);
    
    /**
     * remove an entire entity
     * @param id row key
     */
    public void remove(T entity);
    
	/**
	 * @return Return all entities.  
	 *
	 */
	public Observable<T> getAll();
	
	/**
	 * @return Get a set of entities
	 * @param ids
	 */
	public Observable<T> get(Collection<K> ids);

	/**
	 * Delete a set of entities by their id
	 * @param ids
	 */
	public void delete(Collection<K> ids);
	
    /**
     * Delete a set of entities 
     * @param ids
     */
	public void remove(Collection<T> entities);
	
	/**
	 * Store a set of entities.
	 * @param entites
	 */
	public void put(Collection<T> entities);
	

	/**
	 * Execute a CQL query and return the found entites
	 * @param cql
	 */
	public Observable<T> find(String cql);

	/**
	 * Create the underlying storage for this entity.  This should only be called
	 * once when first creating store and not part of the normal startup sequence.
	 */
	public void createStorage(String query);
    
    /**
     * Delete the underlying storage for this entity.  
     * @param options
     */
    public void deleteStorage();
    /**
     * Truncate all data in the underlying
     * @param options
     */
    public void truncate();
    
    /**
     * Commit the internal batch after multiple operations.  Note that an entity
     * manager implementation may autocommit after each operation.
     */
    public void commit();
}
