package org.odata4j.consumer;

import java.util.HashMap;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.NamedValue;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OCreate;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityRef;
import org.odata4j.core.OModify;
import org.odata4j.core.OQuery;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.Expression;
import org.odata4j.expression.ExpressionParser;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.format.xml.AtomFeedFormatParser.CollectionInfo;
import org.odata4j.internal.FeedCustomizationMapping;

public class ODataConsumer {

    
    private static class ParsedHref{
        public String entitySetName;
        public Object[] key;
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
            String[] keyValues = keyString.split(",");
            Object[] key;
            if (keyValues.length==1){
                // segments with one key value should not include the name
                key = new Object[]{parseKeyValue(keyValues[0],false)};
            } else {
                // segments with multiple key values should include the name (ie they should be NamedValue)
                key = Enumerable.create(keyValues).select(new Func1<String,Object>(){
                    public Object apply(String keyValue) {
                       return parseKeyValue(keyValue,true);
                    }}).toArray(Object.class);
            }
      
            ParsedHref rt = new ParsedHref();
            rt.entitySetName= entitySetName;
            rt.key = key;
            rt.navProperty = navProperty;
            return rt;
        }
        
        private static Object parseKeyValue(String keyValue, boolean expectName){
           
            String name = null;
            if (expectName){
                int equalsIndex = keyValue.indexOf('=');
                if (equalsIndex<0)
                    throw new IllegalArgumentException("Expected name for key value: " + keyValue);
                name = keyValue.substring(0,equalsIndex);
                keyValue = keyValue.substring(equalsIndex+1);
            }
            CommonExpression expr = ExpressionParser.parse(keyValue);
            LiteralExpression literal = (LiteralExpression)expr;
            Object rt = Expression.literalValue(literal);
            return expectName?new NamedValueImpl<Object>(name,rt):rt;
        }
        
        private static class NamedValueImpl<T> implements NamedValue<T> {

            private final String name;
            private final T value;
            
            public NamedValueImpl(String name, T value){
                this.name = name;
                this.value = value;
            }
            @Override
            public String getName() {
               return name;
            }

            @Override
            public T getValue() {
               return value;
            }
            
        }
    }
    
    
    public static boolean DUMP_REQUEST_HEADERS;
    public static boolean DUMP_REQUEST_BODY;
    public static boolean DUMP_RESPONSE_HEADERS;
    public static boolean DUMP_RESPONSE_BODY;
    
    private final Map<String,FeedCustomizationMapping> cachedMappings = new HashMap<String,FeedCustomizationMapping>();
    private final String serviceRootUri;
    private final ODataClient client;
    
    private EdmDataServices cachedMetadata;
    private boolean gotMetadata;
    
    

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
    
   
    
    public EdmDataServices getMetadata() {
        if (!gotMetadata){
            ODataClientRequest request = ODataClientRequest.get(serviceRootUri + "$metadata");
            cachedMetadata = client.getMetadata(request);
            gotMetadata = true;
        }
        return cachedMetadata;
    }
    
    
    
    public OQuery<OEntity> getEntities(ORelatedEntitiesLink link) {
        ParsedHref parsed = ParsedHref.parse(link.getHref());
        return getEntities(parsed.entitySetName).nav(parsed.key, parsed.navProperty);
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
    public OEntityRef<OEntity> getEntity(ORelatedEntityLink link) {
        ParsedHref parsed = ParsedHref.parse(link.getHref());
        return getEntity(parsed.entitySetName,parsed.key).nav(parsed.navProperty) ;
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

   

  

   
    

}
