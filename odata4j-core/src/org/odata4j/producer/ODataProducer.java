package org.odata4j.producer;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmDataServices;

/** ODataProducer is an interface to be implemented by any data source which can return
 * data using OData.  The interface consists of two portions: the first portion consists
 * of the methods clients use to obtain and navigate the OData tree; the second half
 * reflects the methods that can be used to place new entities into the set.  Not all
 * ODATA producers will support this second half of the interface.
 */
public interface ODataProducer {

	/** Obtain the most up-to-date metadata for this producer.
	 * 
	 * @return an appropriate metadata object for encoding for return to the client
	 */
    public abstract EdmDataServices getMetadata();

    /** Get all the entities for a given set matching the query information
     * 
     * @param entitySetName the entity "set" or "type" to return
     * @param queryInfo the additional constraints to be applied to the entities
     * @return a packaged collection of entities to pass back to the client
     */
    public abstract EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo);

    /** Obtain a single entity based on its type and key
     * 
     * @param entitySetName the entity "set" or "type" to be examined
     * @param entityKey the unique ID within the set
     * @return the matching Entity if any
     * @throws NotFoundException if the requested entity could not be found.
     */
    public abstract EntityResponse getEntity(String entitySetName, Object entityKey);

    /** Given a specific entity, follow one of its navigation properties, applying constraints
     * as appropriate.
     * @param entitySetName the "set" or "type" of the entity to start with
     * @param entityKey the unique id of the starting entity
     * @param navProp the navigation property to follow
     * @param queryInfo additional constraints to apply to the property
     * @return
     */
    public abstract BaseResponse getNavProperty(
            String entitySetName,
            Object entityKey,
            String navProp,
            QueryInfo queryInfo);

    public abstract void close();

    public abstract EntityResponse createEntity(String entitySetName, OEntity entity);

    public abstract EntityResponse createEntity(String entitySetName, Object entityKey, String navProp, OEntity entity);

    public abstract void deleteEntity(String entitySetName, Object entityKey);

    public abstract void mergeEntity(String entitySetName, Object entityKey, OEntity entity);

    public abstract void updateEntity(String entitySetName, Object entityKey, OEntity entity);
}
