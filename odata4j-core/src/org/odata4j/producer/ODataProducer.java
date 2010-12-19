package org.odata4j.producer;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmDataServices;

public interface ODataProducer {

    public abstract EdmDataServices getMetadata();

    public abstract EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo);

    public abstract EntityResponse getEntity(String entitySetName, Object entityKey);

    public abstract EntitiesResponse getNavProperty(
            String entitySetName,
            Object entityKey,
            String navProp,
            QueryInfo queryInfo);

    public abstract void close();

    public abstract EntityResponse createEntity(String entitySetName, OEntity entity);

    public abstract void deleteEntity(String entitySetName, Object entityKey);

    public abstract void mergeEntity(String entitySetName, Object entityKey, OEntity entity);

    public abstract void updateEntity(String entitySetName, Object entityKey, OEntity entity);
}
