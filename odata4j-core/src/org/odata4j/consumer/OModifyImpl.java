package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.OEntity;
import org.odata4j.core.OModify;
import org.odata4j.core.OProperty;
import org.odata4j.internal.EntitySegment;
import org.odata4j.xml.AtomFeedParser.DataServicesAtomEntry;

import org.core4j.Enumerable;
import org.core4j.Predicate1;

public class OModifyImpl<T> implements OModify<T> {

    private final T updateRoot;
    private final ODataClient client;
    private final String serviceRootUri;
    private final List<EntitySegment> segments = new ArrayList<EntitySegment>();

    private final List<OProperty<?>> props = new ArrayList<OProperty<?>>();

    public OModifyImpl(T updateRoot, ODataClient client, String serviceRootUri, String entitySetName, Object[] key) {
        this.updateRoot = updateRoot;
        this.client = client;
        this.serviceRootUri = serviceRootUri;

        segments.add(new EntitySegment(entitySetName, key));
    }

    @Override
    public OModify<T> nav(String navProperty, Object... key) {
        segments.add(new EntitySegment(navProperty, key));
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

        DataServicesAtomEntry entry = new DataServicesAtomEntry();
        entry.contentType = MediaType.APPLICATION_XML;
        entry.properties = requestProps;

        String path = Enumerable.create(segments).join("/");

        ODataClientRequest request = updateRoot != null ? ODataClientRequest.put(serviceRootUri + path, entry) : ODataClientRequest.merge(serviceRootUri + path, entry);
        request = request.header("Content-Type", MediaType.APPLICATION_XML);
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
