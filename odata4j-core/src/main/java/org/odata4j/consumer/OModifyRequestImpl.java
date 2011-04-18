package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
import org.odata4j.internal.EntitySegment;

class OModifyRequestImpl<T> extends OConsumerRequestBase implements OModifyRequest<T> {

    private final T updateRoot;
    private final ODataClient client;
    
    private final List<EntitySegment> segments = new ArrayList<EntitySegment>();

    private EdmEntitySet entitySet;

    public OModifyRequestImpl(T updateRoot, ODataClient client, String serviceRootUri, EdmDataServices metadata, String entitySetName, OEntityKey key) {
    	super(entitySetName, serviceRootUri, metadata);
        this.updateRoot = updateRoot;
        this.client = client;
      
        segments.add(new EntitySegment(entitySetName, key));
        this.entitySet = metadata.getEdmEntitySet(entitySetName);
    }

    @Override
    public OModifyRequest<T> nav(String navProperty, OEntityKey key) {
        segments.add(new EntitySegment(navProperty, key));
        entitySet = metadata.getEdmEntitySet(entitySet.type.getNavigationProperty(navProperty).toRole.type);
        return this;
    }

    @Override
    public boolean execute() {

        List<OProperty<?>> requestProps = props;
        if (updateRoot != null) {
            OEntity updateRootEntity = (OEntity) updateRoot;
            requestProps = Enumerable.create(updateRootEntity.getProperties()).toList();
            for(final OProperty<?> prop : props) {
                OProperty<?> requestProp = Enumerable.create(requestProps).firstOrNull(new Predicate1<OProperty<?>>() {
                    public boolean apply(OProperty<?> input) {
                        return input.getName().equals(prop.getName());
                    }
                });
                requestProps.remove(requestProp);
                requestProps.add(prop);
            }
        }

        OEntityKey entityKey =  Enumerable.create(segments).last().key;
        Entry entry = client.createRequestEntry(entitySet, entityKey, requestProps, links);

        String path = Enumerable.create(segments).join("/");

        ODataClientRequest request = updateRoot != null ? ODataClientRequest.put(serviceRootUri + path, entry) : ODataClientRequest.merge(serviceRootUri + path, entry);
        boolean rt = client.updateEntity(request);
        return rt;
    }

    @Override
    public OModifyRequest<T> properties(OProperty<?>... props) {
       return super.properties(this, props);
    }
    
    @Override
    public OModifyRequest<T> properties(Iterable<OProperty<?>> props) {
       return super.properties(this, props);
    }
    
    @Override
    public OModifyRequest<T> link(String navProperty, OEntity target) {
    	return super.link(this,navProperty,target);
    }
    
    @Override
    public OModifyRequest<T> link(String navProperty, OEntityKey targetKey) {
    	return super.link(this,navProperty,targetKey);
    }

}
