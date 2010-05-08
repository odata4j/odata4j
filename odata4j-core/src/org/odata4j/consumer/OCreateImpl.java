package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.OCreate;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

public class OCreateImpl<T> implements OCreate<T> {

    private final ODataClient client;
    private final String serviceRootUri;
    private final String entitySetName;

    private final List<OProperty<?>> props = new ArrayList<OProperty<?>>();

    private final FeedCustomizationMapping fcMapping;
    
    public OCreateImpl(ODataClient client, String serviceRootUri, String entitySetName, FeedCustomizationMapping fcMapping) {
        this.client = client;
        this.serviceRootUri = serviceRootUri;
        this.entitySetName = entitySetName;
        this.fcMapping = fcMapping;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T execute() {

        DataServicesAtomEntry entry = new DataServicesAtomEntry();
        entry.contentType = MediaType.APPLICATION_XML;
        entry.properties = props;
        ODataClientRequest request = ODataClientRequest.post(serviceRootUri + entitySetName, entry);

        DataServicesAtomEntry dsae = client.createEntity(request);
        OEntity rt = InternalUtil.toOEntity(dsae,fcMapping);
        return (T) rt;
    }

    @Override
    public OCreate<T> properties(OProperty<?>... props) {
        for(OProperty<?> prop : props)
            this.props.add(prop);
        return this;
    }


}
