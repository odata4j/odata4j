package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OModify;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
import org.odata4j.internal.EntitySegment;

public class OModifyImpl<T> implements OModify<T> {

    private final T updateRoot;
    private final ODataClient client;
    private final String serviceRootUri;
    private final EdmDataServices metadata;
    private final List<EntitySegment> segments = new ArrayList<EntitySegment>();

    private final List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    private EdmEntitySet entitySet;

    public OModifyImpl(T updateRoot, ODataClient client, String serviceRootUri, EdmDataServices metadata,
    		String entitySetName, Object[] key) {
        this.updateRoot = updateRoot;
        this.client = client;
        this.serviceRootUri = serviceRootUri;
        this.metadata = metadata;

        segments.add(new EntitySegment(entitySetName, key));
        this.entitySet = metadata.getEdmEntitySet(entitySetName);
    }

    @Override
    public OModify<T> nav(String navProperty, Object... key) {
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

        List<OLink> links = Collections.emptyList();
        Entry entry = client.createRequestEntry(entitySet, requestProps, links);

        String path = Enumerable.create(segments).join("/");

        ODataClientRequest request = updateRoot != null ? ODataClientRequest.put(serviceRootUri + path, entry) : ODataClientRequest.merge(serviceRootUri + path, entry);
        boolean rt = client.updateEntity(request);
        return rt;
    }

    @Override
    public OModify<T> properties(OProperty<?>... props) {
        for(OProperty<?> prop : props)
            this.props.add(prop);
        return this;
    }

}
