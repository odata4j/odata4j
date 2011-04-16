package org.odata4j.consumer;

import java.util.HashMap;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityRequest;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OQueryRequest;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.format.FormatType;
import org.odata4j.format.xml.AtomFeedFormatParser.CollectionInfo;
import org.odata4j.internal.EdmDataServicesDecorator;
import org.odata4j.internal.FeedCustomizationMapping;

public class ODataConsumer {

    
    private static class ParsedHref{
        public String entitySetName;
        public OEntityKey entityKey;
        public String navProperty;
        
        private ParsedHref(){}
        public static ParsedHref parse(String href){
            // href: entityset(keyvalue[,keyvalue])/navprop[/navprop]
            // keyvalue: <literal> for one key value -or- <name=literal> for multiple key values
            
            int slashIndex = href.indexOf('/');
            String head = href.substring(0,slashIndex);
            String navProperty = href.substring(slashIndex+1);

            int pIndex = head.indexOf('(');
            String entitySetName = head.substring(0,pIndex);
            
            String keyString = head.substring(pIndex+1,head.length()-1);  // keyvalue[,keyvalue]
           
            ParsedHref rt = new ParsedHref();
            rt.entitySetName= entitySetName;
            rt.entityKey = OEntityKey.parse(keyString);
            rt.navProperty = navProperty;
            return rt;
        }        
    }
    

    public static enum Dump{
    	INSTANCE;
    	private boolean requestHeaders;
    	private boolean requestBody;
    	private boolean responseHeaders;
    	private boolean responseBody;
    	public void all(boolean dump){ requestHeaders(dump); requestBody(dump); responseHeaders(dump); responseBody(dump); }
    	public boolean requestHeaders() { return requestHeaders; }
    	public void requestHeaders(boolean dump){ this.requestHeaders = dump; }
    	public boolean requestBody() { return requestBody; }
    	public void requestBody(boolean dump){ this.requestBody = dump; }
    	public boolean responseHeaders() { return responseHeaders; }
    	public void responseHeaders(boolean dump){ this.responseHeaders = dump; }
    	public boolean responseBody() { return responseBody; }
    	public void responseBody(boolean dump){ this.responseBody = dump; }
    }
    
    public static final Dump dump = Dump.INSTANCE;
    
    private final Map<String,FeedCustomizationMapping> cachedMappings = new HashMap<String,FeedCustomizationMapping>();
    private final String serviceRootUri;
    private final ODataClient client;
    
    private EdmDataServices cachedMetadata;

	private ODataConsumer(FormatType type, String serviceRootUri, OClientBehavior... behaviors) {
        if (!serviceRootUri.endsWith("/"))
            serviceRootUri = serviceRootUri+"/";
        
        this.serviceRootUri = serviceRootUri;        
        this.client = new ODataClient(type, behaviors);
    }

    public String getServiceRootUri() {
        return serviceRootUri;
    }

    public static ODataConsumer create(String serviceRootUri) {
        return new ODataConsumer(FormatType.ATOM, serviceRootUri);
    }

    public static ODataConsumer create(String serviceRootUri, OClientBehavior... behaviors) {
        return new ODataConsumer(FormatType.ATOM, serviceRootUri, behaviors);
    }

    public static ODataConsumer create(FormatType preferredType, String serviceRootUri) {
        return new ODataConsumer(preferredType, serviceRootUri);
    }

    public static ODataConsumer create(FormatType preferredType, String serviceRootUri, OClientBehavior... behaviors) {
        return new ODataConsumer(preferredType, serviceRootUri, behaviors);
    }
    
	public Enumerable<String> getEntitySets() {
        ODataClientRequest request = ODataClientRequest.get(serviceRootUri);
        return Enumerable.create(client.getCollections(request)).select(new Func1<CollectionInfo, String>() {
            public String apply(CollectionInfo input) {
                return input.title;
            }
        });
    }
    
   
    
    public EdmDataServices getMetadata() {
        if (cachedMetadata==null)
        	cachedMetadata = new CachedEdmDataServices();
        return cachedMetadata;
    }
    
    
    
    public OQueryRequest<OEntity> getEntities(ORelatedEntitiesLink link) {
        ParsedHref parsed = ParsedHref.parse(link.getHref());
        return getEntities(parsed.entitySetName).nav(parsed.entityKey, parsed.navProperty);
    }
    
    public OQueryRequest<OEntity> getEntities(String entitySetName) {
        return getEntities(OEntity.class,entitySetName);
    }
    
	public <T> OQueryRequest<T> getEntities(Class<T> entityType, String entitySetName) {
        FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
		return new OQueryRequestImpl<T>(client, entityType, serviceRootUri, getMetadata(), entitySetName, mapping);
    }

	
	
    public OEntityRequest<OEntity> getEntity(ORelatedEntityLink link) {
        ParsedHref parsed = ParsedHref.parse(link.getHref());
        return getEntity(parsed.entitySetName,parsed.entityKey).nav(parsed.navProperty) ;
    }
    
    public OEntityRequest<OEntity> getEntity(String entitySetName, Object keyValue) {
    	return getEntity(entitySetName,OEntityKey.create(keyValue));
    }
    
    public OEntityRequest<OEntity> getEntity(String entitySetName, OEntityKey key) {
        return getEntity(OEntity.class,entitySetName,key);
    }
    
    public <T> OEntityRequest<T> getEntity(Class<T> entityType, String entitySetName, Object keyValue) {
    	return getEntity(entityType,entitySetName,OEntityKey.create(keyValue));
    }
    public <T> OEntityRequest<T> getEntity(Class<T> entityType, String entitySetName, OEntityKey key) {
        FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
		return new OEntityRequestImpl<T>(false,  client,
				entityType, serviceRootUri, getMetadata(),
				entitySetName, OEntityKey.create(key), mapping);
    }

	public OCreateRequest<OEntity> createEntity(String entitySetName) {
		FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
		return new OCreateRequestImpl<OEntity>(client, serviceRootUri, getMetadata(),
				entitySetName, mapping);
	}

	public OModifyRequest<OEntity> updateEntity(OEntity entity) {
        return new OModifyRequestImpl<OEntity>(entity, client, serviceRootUri, getMetadata(),
        		entity.getEntitySet().name,entity.getEntityKey());
    }

	public OModifyRequest<OEntity> mergeEntity(String entitySetName, Object keyValue) {
		return mergeEntity(entitySetName, OEntityKey.create(keyValue));
	}

	public OModifyRequest<OEntity> mergeEntity(String entitySetName, OEntityKey key) {
		return new OModifyRequestImpl<OEntity>(null, client, serviceRootUri,
				getMetadata(), entitySetName, key);
	}

	public OEntityRequest<Void> deleteEntity(OEntity entity) {
		return deleteEntity(entity.getEntitySet().name,entity.getEntityKey());
	}
	
    public OEntityRequest<Void> deleteEntity(String entitySetName, Object keyValue) {
    	return deleteEntity(entitySetName,OEntityKey.create(keyValue));
    }
    
	public OEntityRequest<Void> deleteEntity(String entitySetName, OEntityKey key) {
        FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
		return new OEntityRequestImpl<Void>(true, client,
				null, serviceRootUri, getMetadata(), entitySetName, key,
				mapping);
    }

	private FeedCustomizationMapping getFeedCustomizationMapping(String entitySetName){
        
        if (!cachedMappings.containsKey(entitySetName)) {
           
            FeedCustomizationMapping rt = new FeedCustomizationMapping();
            
            EdmDataServices metadata = getMetadata();
            if (metadata != null) {
                EdmEntitySet ees = metadata.findEdmEntitySet(entitySetName);
                if (ees==null){
                   rt = null;
                } else {
                    EdmEntityType eet = ees.type;
                    
                    for(EdmProperty ep : eet.properties){
                        if ("SyndicationTitle".equals(ep.fcTargetPath) && "false".equals(ep.fcKeepInContent))
                            rt.titlePropName = ep.name;  
                        if ("SyndicationSummary".equals(ep.fcTargetPath) && "false".equals(ep.fcKeepInContent))
                            rt.summaryPropName = ep.name;  
                    }
                }
            }
            cachedMappings.put(entitySetName, rt);
        }
        return cachedMappings.get(entitySetName);
    }

   

    private class CachedEdmDataServices extends EdmDataServicesDecorator {
    	private EdmDataServices delegate;
		public CachedEdmDataServices() {

		}
		@Override
		protected EdmDataServices getDelegate() {
			if (delegate==null)
				refreshDelegate();
			return delegate;
		}
		
		private void refreshDelegate() {
			ODataClientRequest request = ODataClientRequest.get(serviceRootUri + "$metadata");
			EdmDataServices metadata = client.getMetadata(request);
			 delegate = metadata==null?EdmDataServices.EMPTY:metadata;
		}
	
		@Override
		public EdmEntitySet findEdmEntitySet(String entitySetName) {
			EdmEntitySet rt = super.findEdmEntitySet(entitySetName);
			if (rt==null) {
				refreshDelegate();
				rt = super.findEdmEntitySet(entitySetName);
			}
			return rt;
		}
    }
  

   
    

}
