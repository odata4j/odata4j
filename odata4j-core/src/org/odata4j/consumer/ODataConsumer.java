package org.odata4j.consumer;

import java.util.HashMap;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OCreate;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityRef;
import org.odata4j.core.OModify;
import org.odata4j.core.OQuery;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.format.xml.AtomFeedFormatParser.CollectionInfo;
import org.odata4j.internal.FeedCustomizationMapping;

public class ODataConsumer {

    public static boolean DUMP_REQUEST_HEADERS;
    public static boolean DUMP_REQUEST_BODY;
    public static boolean DUMP_RESPONSE_HEADERS;
    public static boolean DUMP_RESPONSE_BODY;
    
    private final String serviceRootUri;
    private final ODataClient client;

    private ODataConsumer(String serviceRootUri, OClientBehavior... behaviors) {
        if (!serviceRootUri.endsWith("/"))
            serviceRootUri = serviceRootUri+"/";
        
        this.serviceRootUri = serviceRootUri;
        this.client = new ODataClient(behaviors);
    }

    public String getServiceRootUri() {
        return serviceRootUri;
    }

    public static ODataConsumer create(String serviceRootUri) {
        return new ODataConsumer(serviceRootUri);
    }

    public static ODataConsumer create(String serviceRootUri, OClientBehavior... behaviors) {
        return new ODataConsumer(serviceRootUri, behaviors);
    }

    
    
    
    public Enumerable<String> getEntitySets() {
        ODataClientRequest request = ODataClientRequest.get(serviceRootUri);
        return Enumerable.create(client.getCollections(request)).select(new Func1<CollectionInfo, String>() {
            public String apply(CollectionInfo input) {
                return input.title;
            }
        });
    }
    
    private EdmDataServices cachedMetadata;
    private boolean gotMetadata;
    
    public EdmDataServices getMetadata() {
        if (!gotMetadata){
            ODataClientRequest request = ODataClientRequest.get(serviceRootUri + "$metadata");
            cachedMetadata = client.getMetadata(request);
            gotMetadata = true;
        }
        return cachedMetadata;
    }
    
    
    
    public OQuery<OEntity> getEntities(String entitySetName) {
        return getEntities(OEntity.class,entitySetName);
    }
    
    public <T> OQuery<T> getEntities(Class<T> entityType, String entitySetName) {
        FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
        return new OQueryImpl<T>(client, entityType, serviceRootUri, entitySetName, mapping);
    }

    
    
    public OEntityRef<OEntity> getEntity(String entitySetName, Object... key) {
        return getEntity(OEntity.class,entitySetName,key);
    }
    
    public <T> OEntityRef<T> getEntity(Class<T> entityType, String entitySetName, Object... key) {
        FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
        return new OEntityRefImpl<T>(false, client, entityType, serviceRootUri, entitySetName, key, mapping);
    }

    
    
    public OCreate<OEntity> createEntity(String entitySetName) {
        FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
        gotMetadata = false;
        return new OCreateImpl<OEntity>(client, serviceRootUri, entitySetName, mapping);
    }

    public OModify<OEntity> updateEntity(OEntity entity, String entitySetName, Object... key) {
        gotMetadata = false;
        return new OModifyImpl<OEntity>(entity, client, serviceRootUri, entitySetName, key);
    }

    public OModify<OEntity> mergeEntity(String entitySetName, Object... key) {
        gotMetadata = false;
        return new OModifyImpl<OEntity>(null, client, serviceRootUri, entitySetName, key);
    }

    public OEntityRef<Void> deleteEntity(String entitySetName, Object... key) {
        FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
        gotMetadata = false;
        return new OEntityRefImpl<Void>(true, client, null, serviceRootUri, entitySetName, key, mapping);
    }

    
    
    
    
    
    
    
    private final Map<String,FeedCustomizationMapping> cachedMappings = new HashMap<String,FeedCustomizationMapping>();
    
    private FeedCustomizationMapping getFeedCustomizationMapping(String entitySetName){
        
        if (!cachedMappings.containsKey(entitySetName)) {
           
            FeedCustomizationMapping rt = new FeedCustomizationMapping();
            
            EdmDataServices metadata = getMetadata();
            if (metadata != null) {
                EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
                EdmEntityType eet = ees.type;
                
                for(EdmProperty ep : eet.properties){
                    if ("SyndicationTitle".equals(ep.fcTargetPath) && "false".equals(ep.fcKeepInContent))
                        rt.titlePropName = ep.name;  
                    if ("SyndicationSummary".equals(ep.fcTargetPath) && "false".equals(ep.fcKeepInContent))
                        rt.summaryPropName = ep.name;  
                }
            }
            cachedMappings.put(entitySetName, rt);
        }
        return cachedMappings.get(entitySetName);
    }

   
    

}
