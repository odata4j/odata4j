package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.OCreate;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

public class OCreateImpl<T> implements OCreate<T> {

    private final ODataClient client;
    private final EdmDataServices metadata;
    private final String serviceRootUri;
    private final String entitySetName;
    private OEntity parent;
    private String navProperty;

    private final List<OProperty<?>> props = new ArrayList<OProperty<?>>();

    private final FeedCustomizationMapping fcMapping;
    
    public OCreateImpl(ODataClient client, String serviceRootUri, EdmDataServices metadata, String entitySetName, FeedCustomizationMapping fcMapping) {
        this.client = client;
        this.serviceRootUri = serviceRootUri;
        this.metadata = metadata;
        this.entitySetName = entitySetName;
        this.fcMapping = fcMapping;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T execute() {

        DataServicesAtomEntry entry = new DataServicesAtomEntry();
        entry.contentType = MediaType.APPLICATION_XML;
        entry.properties = props;
        
        StringBuilder url = new StringBuilder(serviceRootUri);
        if (parent != null) {
        	url.append(InternalUtil.getEntityRelId(parent))
        	   .append("/")
        	   .append(navProperty);
        } else {
        	url.append(entitySetName);
        }
        
        ODataClientRequest request = ODataClientRequest.post(url.toString(), entry);

        DataServicesAtomEntry dsae = client.createEntity(request);
        OEntity rt = InternalUtil.toOEntity(metadata, 
        		metadata.getEdmEntitySet(entitySetName), dsae,fcMapping);
        return (T) rt;
    }

    @Override
    public OCreate<T> properties(OProperty<?>... props) {
        for(OProperty<?> prop : props)
            this.props.add(prop);
        return this;
    }

    @Override
    public OCreate<T> addToRelation(OEntity parent, String navProperty) {
    	if (parent == null || navProperty == null)
    		throw new IllegalArgumentException("please provide the parent and the navProperty");

    	this.parent = parent;
    	this.navProperty = navProperty;
    	return this;
    }
}
