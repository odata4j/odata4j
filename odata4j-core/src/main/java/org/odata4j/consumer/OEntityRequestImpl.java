package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.Settings;
import org.odata4j.internal.EntitySegment;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

import com.sun.jersey.api.client.ClientResponse;

class OEntityRequestImpl<T> implements OEntityRequest<T> {

    private final boolean isDelete;
    private final ODataClient client;
    private final Class<T> entityType;
    private final EdmDataServices metadata;
    private final String serviceRootUri;
    private final List<EntitySegment> segments = new ArrayList<EntitySegment>();

    private final FeedCustomizationMapping fcMapping;
   
    public OEntityRequestImpl(boolean isDelete, ODataClient client, Class<T> entityType, String serviceRootUri, EdmDataServices metadata, String entitySetName, OEntityKey key, FeedCustomizationMapping fcMapping) {
        this.isDelete = isDelete;
        this.client = client;
        this.entityType = entityType;
        this.serviceRootUri = serviceRootUri;
        this.metadata = metadata;
        
        segments.add(new EntitySegment(entitySetName, key));
        
        this.fcMapping = fcMapping;
    }

    @Override
    public OEntityRequest<T> nav(String navProperty, OEntityKey key) {
        segments.add(new EntitySegment(navProperty, key));
        return this;
    }
    
    @Override
    public OEntityRequest<T> nav(String navProperty) {
        segments.add(new EntitySegment(navProperty, null));
        return this;
    }

    @Override
    public T execute() {

        String path = Enumerable.create(segments).join("/");

        if (isDelete) {
            ODataClientRequest request = ODataClientRequest.delete(serviceRootUri + path);
            client.deleteEntity(request);
            return null;

        } else {

            ODataClientRequest request = ODataClientRequest.get(serviceRootUri + path);

            ClientResponse response = client.getEntity(request);
            if (response == null)
            	return null;

        	//	the first segment contains the entitySetName we start from
        	EdmEntitySet entitySet = metadata.getEdmEntitySet(segments.get(0).segment);
        	for(EntitySegment segment : segments.subList(1, segments.size()) ) {
        		EdmNavigationProperty navProperty = entitySet.type.getNavigationProperty(segment.segment);
        		entitySet = metadata.getEdmEntitySet(navProperty.toRole.type);
        	}

        	OEntityKey key = Enumerable.create(segments).last().key;
        	
        	// TODO determine the service version from header (and metadata?) 
    		final FormatParser<Feed> parser = FormatParserFactory
    			.getParser(Feed.class, client.type, 
    					new Settings(ODataConstants.DATA_SERVICE_VERSION, metadata, entitySet.name, key, fcMapping));
            
    		Entry entry  = Enumerable.create(parser.parse(client.getFeedReader(response))
    				.getEntries())
    				.firstOrNull();

            return (T) InternalUtil.toEntity(entityType, entry.getEntity());
        }
    }

}