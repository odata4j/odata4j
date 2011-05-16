package org.odata4j.producer.inmemory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Func1;
import org.core4j.Predicate1;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationEnd;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmAssociationSetEnd;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmType;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.exceptions.NotFoundException;
import org.odata4j.producer.exceptions.NotImplementedException;



/** An in-memory implementation of an ODATA Producer.  Uses the standard Java bean
 * and property model to access information within entities. 
 */
public class InMemoryProducer implements ODataProducer {

    private static class EntityInfo<TEntity, TKey> {
    	String entitySetName;
        Class<TKey> keyClass;
        Class<TEntity> entityClass;
        Func<Iterable<TEntity>> get;
        Func1<Object, TKey> id;
        PropertyModel properties;

    }

    private static final String ID_PROPNAME = "EntityId";
    private static final String CONTAINER_NAME = "Container";
    private static final Map<Class<?>, EdmType> SUPPORTED_TYPES = new HashMap<Class<?>, EdmType>();
    static {
        SUPPORTED_TYPES.put(byte[].class, EdmType.BINARY);
        SUPPORTED_TYPES.put(Boolean.class, EdmType.BOOLEAN);
        SUPPORTED_TYPES.put(Boolean.TYPE, EdmType.BOOLEAN);
        SUPPORTED_TYPES.put(byte.class, EdmType.BYTE);
        SUPPORTED_TYPES.put(LocalDateTime.class, EdmType.DATETIME);
        SUPPORTED_TYPES.put(BigDecimal.class, EdmType.DECIMAL);
        SUPPORTED_TYPES.put(Double.class, EdmType.DOUBLE);
        SUPPORTED_TYPES.put(Double.TYPE, EdmType.DOUBLE);
        SUPPORTED_TYPES.put(Guid.class, EdmType.GUID);
        SUPPORTED_TYPES.put(Short.class, EdmType.INT16);
        SUPPORTED_TYPES.put(Short.TYPE, EdmType.INT16);
        SUPPORTED_TYPES.put(Integer.class, EdmType.INT32);
        SUPPORTED_TYPES.put(Integer.TYPE, EdmType.INT32);
        SUPPORTED_TYPES.put(Long.class, EdmType.INT64);
        SUPPORTED_TYPES.put(Long.TYPE, EdmType.INT64);
        SUPPORTED_TYPES.put(Float.class, EdmType.SINGLE);
        SUPPORTED_TYPES.put(Float.TYPE, EdmType.SINGLE);
        SUPPORTED_TYPES.put(String.class, EdmType.STRING);
        SUPPORTED_TYPES.put(LocalTime.class, EdmType.TIME);
        SUPPORTED_TYPES.put(DateTime.class, EdmType.DATETIMEOFFSET);
        SUPPORTED_TYPES.put(Date.class, EdmType.DATETIME);
        
        SUPPORTED_TYPES.put(Object.class, EdmType.STRING);
    }

    private final String namespace;
    private final int maxResults;
    private final Map<String, EntityInfo<?, ?>> eis = new HashMap<String, EntityInfo<?, ?>>();
    private EdmDataServices metadata;

    private static final int DEFAULT_MAX_RESULTS = 100;
    
    /** Create a new instance of an in-memory POJO/JPA producer
     * 
     * @param namespace - the namespace that the schema registrations will be in
     */
    public InMemoryProducer(String namespace){
        this(namespace,DEFAULT_MAX_RESULTS);
    }
    
    /** Create a new instance of an in-memory POJO/JPA producer
     * 
     * @param namespace - the names apce that the schema registrations will be in
     * @param maxResults - the maximum number of entities to return
     */
    public InMemoryProducer(String namespace, int maxResults) {
        this.namespace = namespace;
        this.maxResults = maxResults;
        this.metadata = buildMetadata();
    }

    @Override
    public EdmDataServices getMetadata() {
        return metadata;
    }

    private EdmDataServices buildMetadata() {

        List<EdmSchema> schemas = new ArrayList<EdmSchema>();
        List<EdmEntityContainer> containers = new ArrayList<EdmEntityContainer>();
        List<EdmEntitySet> entitySets = new ArrayList<EdmEntitySet>();
        List<EdmEntityType> entityTypes = new ArrayList<EdmEntityType>();
        List<EdmAssociation> associations = new ArrayList<EdmAssociation>();
        List<EdmAssociationSet> associationSets = new ArrayList<EdmAssociationSet>();

        for(String entitySetName : eis.keySet()) {
            EntityInfo<?, ?> ei = eis.get(entitySetName);

            List<EdmProperty> properties = new ArrayList<EdmProperty>();
            properties.add(new EdmProperty(ID_PROPNAME, getEdmType(ei.keyClass), false, null, null,null,null, null, null, null, null, null));

            properties.addAll(toEdmProperties(ei.properties));
            
            EdmEntityType eet = new EdmEntityType(namespace, null, entitySetName, null, Enumerable.create(ID_PROPNAME).toList(), properties, null);

            EdmEntitySet ees = new EdmEntitySet(entitySetName, eet);
            entitySets.add(ees);
            entityTypes.add(eet);
        }
        
        // TODO handle back references too
        
        Map<String, EdmEntityType> eetsByName = Enumerable.create(entityTypes).toMap(new Func1<EdmEntityType, String>() {
            public String apply(EdmEntityType input) {
                return input.name;
            }
        });
        Map<String, EdmEntitySet> eesByName = Enumerable.create(entitySets).toMap(new Func1<EdmEntitySet, String>() {
            public String apply(EdmEntitySet input) {
                return input.name;
            }
        });
        Map<Class<?>, String> eeNameByClass = new HashMap<Class<?>, String>();
        for(Entry<String, EntityInfo<?, ?>> e : eis.entrySet())
        	eeNameByClass.put(e.getValue().entityClass, e.getKey());

        for(String entitySetName : eis.keySet()) {
            EntityInfo<?, ?> ei = eis.get(entitySetName);
            
	        for(String assocProp : ei.properties.getCollectionNames()) {
	
                EdmEntityType eet1 = eetsByName.get(entitySetName);
                
                Class<?> clazz2 = ei.properties.getCollectionElementType(assocProp);
                String eetName2 = eeNameByClass.get(clazz2);
                EdmEntityType eet2 =  eetsByName.get(eetName2);

                EdmMultiplicity m1 = EdmMultiplicity.ZERO_TO_ONE;
                EdmMultiplicity m2 = EdmMultiplicity.MANY;

                String assocName = String.format("FK_%s_%s", eet1.name, eet2.name);
                EdmAssociationEnd assocEnd1 = new EdmAssociationEnd(eet1.name, eet1, m1);
                String assocEnd2Name = eet2.name;
                if (assocEnd2Name.equals(eet1.name))
                    assocEnd2Name = assocEnd2Name + "1";
                EdmAssociationEnd assocEnd2 = new EdmAssociationEnd(assocEnd2Name, eet2, m2);
                EdmAssociation assoc = new EdmAssociation(namespace, null, assocName, assocEnd1, assocEnd2);
                
                associations.add(assoc);

                EdmEntitySet ees1 = eesByName.get(eet1.name);
                EdmEntitySet ees2 = eesByName.get(eet2.name);
                EdmAssociationSet eas = new EdmAssociationSet(assocName, assoc, new EdmAssociationSetEnd(assocEnd1, ees1), new EdmAssociationSetEnd(assocEnd2, ees2));

                associationSets.add(eas);

                EdmNavigationProperty np = new EdmNavigationProperty(assocProp, assoc, assoc.end1, assoc.end2);

                eet1.navigationProperties.add(np);
	        }
        }
        
        EdmEntityContainer container = new EdmEntityContainer(CONTAINER_NAME, true, null, entitySets, associationSets, null);
        containers.add(container);

        EdmSchema schema = new EdmSchema(namespace, null, entityTypes, null, associations, containers);
        schemas.add(schema);
        EdmDataServices rt = new EdmDataServices(ODataConstants.DATA_SERVICE_VERSION,schemas);
        return rt;
    }

    @Override
    public void close() {

    }

    private static <T1, T2> Func1<Object, T2> widen(final Func1<T1, T2> fn) {
        return new Func1<Object, T2>() {

            @SuppressWarnings("unchecked")
            @Override
            public T2 apply(Object input) {
                return fn.apply((T1) input);
            }
        };
    }

    public <TEntity, TKey> void register(final Class<TEntity> entityClass, Class<TKey> keyClass, final String entitySetName, Func<Iterable<TEntity>> get, final String idPropertyName) {
        register(entityClass, keyClass, entitySetName, get, new Func1<TEntity, TKey>() {

            @SuppressWarnings("unchecked")
            @Override
            public TKey apply(TEntity input) {
                return (TKey) eis.get(entitySetName).properties.getPropertyValue(input, idPropertyName);
            }
        });
    }

    /** Register a new ODATA endpoint for an entity set.
     * 
     * @param entityClass the class of the entities that are to be stored in the set
     * @param keyClass the class of the key element of the set
     * @param entitySetName the alias the set will be known by; this is what is used in the ODATA URL
     * @param get a function to iterate over the elements in the set
     * @param id a function to extract the id from any given element in the set
     */
    public <TEntity, TKey> void register(Class<TEntity> entityClass, Class<TKey> keyClass, String entitySetName, Func<Iterable<TEntity>> get, final Func1<TEntity, TKey> id) {

        register(entityClass,new AugmentedBeanBasedPropertyModel(entityClass),keyClass,entitySetName,get,id);
    }
    
    public <TEntity, TKey> void register(Class<TEntity> entityClass, PropertyModel propertyModel, Class<TKey> keyClass, String entitySetName, Func<Iterable<TEntity>> get, final Func1<TEntity, TKey> id) {

        EntityInfo<TEntity, TKey> ei = new EntityInfo<TEntity, TKey>();
        ei.entitySetName = entitySetName;
        ei.properties = propertyModel;
        ei.get = get;
        ei.id = widen(id);
        ei.keyClass = keyClass;
        ei.entityClass = entityClass;
        
        eis.put(entitySetName, ei);
        this.metadata = buildMetadata();
    }

    /** A simple extention of the BeanBasedPropertyModel that treats Enums as their
     * corresponding strings
     */
    private static class AugmentedBeanBasedPropertyModel extends BeanBasedPropertyModel {

        public AugmentedBeanBasedPropertyModel(Class<?> clazz) {
            super(clazz);
        }

        @Override
        public Class<?> getPropertyType(String propertyName) {
            Class<?> rt = super.getPropertyType(propertyName);
            if (rt.isEnum())
                return String.class;
            return rt;
        }

        @Override
        public Object getPropertyValue(Object target, String propertyName) {
            Class<?> baseType = super.getPropertyType(propertyName);
            Object rt = super.getPropertyValue(target, propertyName);
            if (baseType.isEnum())
                return ((Enum<?>) rt).name();
            return rt;
        }

    }

    private OEntity toOEntity(EdmEntitySet ees, Object obj, List<EntitySimpleProperty> expand) {
    	EntityInfo<?, ?> ei = eis.get(ees.name);
    	final List<OLink> links = new ArrayList<OLink>();
        final List<OProperty<?>> properties = new ArrayList<OProperty<?>>();

        Object keyValue = ei.id.apply(obj);
        properties.add(OProperties.simple(ID_PROPNAME, getEdmType(ei.keyClass), keyValue));

        for(String propName : ei.properties.getPropertyNames()) {
            EdmType type;
            Object value = ei.properties.getPropertyValue(obj, propName);
            Class<?> propType = ei.properties.getPropertyType(propName);
            type = findEdmType(propType);
            if (type == null)
                continue;

            properties.add(OProperties.simple(propName, type, value));
        }
        
        if (expand != null && !expand.isEmpty()) {
        	EdmEntityType edmEntityType = ees.type;
        	
            for (final EntitySimpleProperty propPath : expand) {
            	
            	String[] props = propPath.getPropertyName().split("/", 2);
				String prop = props[0];
				List<EntitySimpleProperty> remainingPropPath = props.length > 1
						? Arrays.asList(org.odata4j.expression.Expression
								.simpleProperty(props[1])) : null;
						
            	EdmNavigationProperty edmNavProperty = edmEntityType
            		.getNavigationProperty(prop);
            	if (edmNavProperty.toRole.multiplicity == EdmMultiplicity.MANY) {
	            	List<OEntity> relatedEntities = new ArrayList<OEntity>();
	            	Iterable<?> values = ei.properties.getCollectionValue(obj, prop);
	            	if (values != null) {
	            		EdmEntitySet relEntitySet = null;
	            		
	            		for(final Object entity : values) {
	            			if (relEntitySet == null) {
	            				EntityInfo<?, ?> oei = Enumerable.create(eis.values())
	            								.firstOrNull(new Predicate1<InMemoryProducer.EntityInfo<?,?>>() {
	    				        							@Override
	    				        							public boolean apply(EntityInfo<?, ?> input) {
	    				        								return entity.getClass().equals(input.entityClass);
	    				        							}});
	            				relEntitySet = metadata.getEdmEntitySet(oei.entitySetName);
	            			}        	
	            			
	            			relatedEntities.add(toOEntity(relEntitySet, entity, remainingPropPath)); 
	            		}
	            	}
	        		// relation and href will be filled in later for atom or json
	        		links.add(OLinks.relatedEntitiesInline(null, edmNavProperty.name, null, relatedEntities));
	            } else {
	            	//	TODO handle the toOne or toZero navigation properties as well
	            }
            }
        }
               
        return OEntities.create(ees, OEntityKey.create(keyValue), properties, links); 
    }

    private static Predicate1<Object> filterToPredicate(final BoolCommonExpression filter, final PropertyModel properties) {
        return new Predicate1<Object>() {
            public boolean apply(Object input) {
                return InMemoryEvaluation.evaluate(filter, input, properties);
            }
        };
    }

    @Override
    public EntitiesResponse getEntities(String entitySetName, final QueryInfo queryInfo) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
        final EntityInfo<?, ?> ei = eis.get(entitySetName);

        Enumerable<Object> objects = Enumerable.create(ei.get.apply()).cast(Object.class);

        // apply filter
        if (queryInfo.filter != null)
            objects = objects.where(filterToPredicate(queryInfo.filter, ei.properties));

        // compute inlineCount
        Integer inlineCount = queryInfo.inlineCount == InlineCount.ALLPAGES ? objects.count() : null;

        // apply ordering
        if (queryInfo.orderBy != null)
            objects = orderBy(objects, queryInfo.orderBy, ei.properties);

        // work with oentities
        Enumerable<OEntity> entities = objects.select(new Func1<Object, OEntity>() {
            public OEntity apply(Object input) {
                return toOEntity(ees, input, queryInfo.expand);
            }
        });
        
        // skip records by $skipToken
        if (queryInfo.skipToken != null){
            final Boolean[] skipping = new Boolean[]{true};
            entities = entities.skipWhile(new Predicate1<OEntity>(){
                public boolean apply(OEntity input) {
                    if (skipping[0]) {
                        String inputKey = input.getEntityKey().toKeyString();
                        if (queryInfo.skipToken.equals(inputKey))
                            skipping[0] = false;
                        return true;
                    }
                    return false;
                }});
        }
        
        // skip records by $skip amount
        if (queryInfo.skip != null)
            entities = entities.skip(queryInfo.skip);
        
        // apply limit
        int limit = this.maxResults;
        if (queryInfo.top != null && queryInfo.top < limit)
            limit = queryInfo.top;        
        entities = entities.take(limit+1);

        // materialize OEntities
        List<OEntity> entitiesList = entities.toList();

        // determine skipToken if necessary
        String skipToken = null;
        if (entitiesList.size() > limit){
            entitiesList = Enumerable.create(entitiesList).take(limit).toList();
            skipToken = entitiesList.size()==0?null:Enumerable.create(entitiesList).last().getEntityKey().toKeyString();
        }
        
        return Responses.entities(entitiesList, ees, inlineCount, skipToken);
      
    }
  
    private Enumerable<Object> orderBy(Enumerable<Object> iter, List<OrderByExpression> orderBys, final PropertyModel properties) {
        for(final OrderByExpression orderBy : Enumerable.create(orderBys).reverse())
            iter = iter.orderBy(new Comparator<Object>() {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                public int compare(Object o1, Object o2) {
                    Comparable lhs = (Comparable) InMemoryEvaluation.evaluate(orderBy.getExpression(), o1, properties);
                    Comparable rhs = (Comparable) InMemoryEvaluation.evaluate(orderBy.getExpression(), o2, properties);
                    return (orderBy.isAscending() ? 1 : -1) * lhs.compareTo(rhs);
                }
            });
        return iter;
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public EntityResponse getEntity(String entitySetName, OEntityKey entityKey,
			QueryInfo queryInfo) {
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
        final EntityInfo<?, ?> ei = eis.get(entitySetName);

        final Object idValue = InMemoryEvaluation.cast(entityKey.asSingleValue(), ei.keyClass);

        Iterable<Object> iter = (Iterable<Object>) ei.get.apply();

        final Object rt = Enumerable.create(iter).firstOrNull(new Predicate1<Object>() {
            public boolean apply(Object input) {
                Object id = ei.id.apply(input);
                return idValue.equals(id);
            }
        });
        if (rt == null)
            throw new NotFoundException();

        OEntity oe = toOEntity(ees, rt, queryInfo.expand);

        return Responses.entity(oe);
    }

    private Collection<EdmProperty> toEdmProperties(PropertyModel model) {
        List<EdmProperty> rt = new ArrayList<EdmProperty>();

        for(String propName : model.getPropertyNames()) {
            Class<?> propType = model.getPropertyType(propName);
            EdmType type = findEdmType(propType);
            if (type == null)
                continue;
            rt.add(new EdmProperty(propName, type, true, null, null, null, null, null, null, null, null, null));
        }

        return rt;
    }

    private EdmType getEdmType(Class<?> clazz) {
        EdmType type = findEdmType(clazz);
        if (type != null)
            return type;
        throw new UnsupportedOperationException(clazz.getName());
    }

    private EdmType findEdmType(Class<?> clazz) {
        EdmType type = SUPPORTED_TYPES.get(clazz);
        if (type != null)
            return type;
        return null;
    }

    @Override
    public void mergeEntity(String entitySetName, OEntity entity){
        throw new NotImplementedException();
    }

    @Override
    public void updateEntity(String entitySetName, OEntity entity) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteEntity(String entitySetName, OEntityKey entityKey) {
        throw new NotImplementedException();
    }

    @Override
    public EntityResponse createEntity(String entitySetName, OEntity entity) {
        throw new NotImplementedException();
    }
    
	@Override
    public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
		throw new NotImplementedException();
	}

	@Override
	public EntitiesResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo){
        throw new NotImplementedException();
	}
}
