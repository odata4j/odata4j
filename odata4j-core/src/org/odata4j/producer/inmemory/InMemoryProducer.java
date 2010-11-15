package org.odata4j.producer.inmemory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmType;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.NavPropertyResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;

import com.sun.jersey.api.NotFoundException;

public class InMemoryProducer implements ODataProducer {

    private static class EntityInfo<TEntity, TKey> {
        Class<TKey> keyClass;
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
        
        SUPPORTED_TYPES.put(Object.class, EdmType.STRING);
    }

    private final String namespace;
    private final int maxResults;
    private final Map<String, EntityInfo<?, ?>> eis = new HashMap<String, EntityInfo<?, ?>>();
    private EdmDataServices metadata;

    private static final int DEFAULT_MAX_RESULTS = 100;
    
    public InMemoryProducer(String namespace){
        this(namespace,DEFAULT_MAX_RESULTS);
    }
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

        EdmEntityContainer container = new EdmEntityContainer(CONTAINER_NAME, true, null, entitySets, null,null);
        containers.add(container);

        EdmSchema schema = new EdmSchema(namespace, null, entityTypes, null, null, containers);
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

    public <TEntity, TKey> void register(Class<TEntity> entityClass, Class<TKey> keyClass, String entitySetName, Func<Iterable<TEntity>> get, final Func1<TEntity, TKey> id) {

        register(entityClass,new AugmentedBeanBasedPropertyModel(entityClass),keyClass,entitySetName,get,id);
    }
    
    public <TEntity, TKey> void register(Class<TEntity> entityClass, PropertyModel propertyModel, Class<TKey> keyClass, String entitySetName, Func<Iterable<TEntity>> get, final Func1<TEntity, TKey> id) {

        EntityInfo<TEntity, TKey> ei = new EntityInfo<TEntity, TKey>();
        ei.properties = propertyModel;
        ei.get = get;
        ei.id = widen(id);
        ei.keyClass = keyClass;
        
        eis.put(entitySetName, ei);
        this.metadata = buildMetadata();
    }

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

    private OEntity toOEntity(EntityInfo<?, ?> ei, Object obj) {
        final List<OProperty<?>> properties = new ArrayList<OProperty<?>>();

        Object key = ei.id.apply(obj);
        properties.add(OProperties.simple(ID_PROPNAME, getEdmType(ei.keyClass), key));

        for(String propName : ei.properties.getPropertyNames()) {
            EdmType type;
            Object value = ei.properties.getPropertyValue(obj, propName);
            Class<?> propType = ei.properties.getPropertyType(propName);
            type = findEdmType(propType);
            if (type == null)
                continue;

            properties.add(OProperties.simple(propName, type, value));
        }
        
        return OEntities.create(properties, null); 
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
                return toOEntity(ei, input);
            }
        });
        
        // skip records by $skipToken
        if (queryInfo.skipToken != null){
            final Boolean[] skipping = new Boolean[]{true};
            entities = entities.skipWhile(new Predicate1<OEntity>(){
                public boolean apply(OEntity input) {
                    if (skipping[0]) {
                        String inputKey = keyString(input);
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
            skipToken = entitiesList.size()==0?null:InternalUtil.keyString(Enumerable.create(entitiesList).last().getProperty(ID_PROPNAME).getValue(),false);
        }
        
        return Responses.entities(entitiesList, ees, inlineCount, skipToken);
      
    }
    
    private static String keyString(OEntity entity){
        return InternalUtil.keyString(entity.getProperty(ID_PROPNAME).getValue(),false);
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
    public EntityResponse getEntity(String entitySetName, Object entityKey) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
        final EntityInfo<?, ?> ei = eis.get(entitySetName);

        entityKey = InMemoryEvaluation.cast(entityKey, ei.keyClass);

        Iterable<Object> iter = (Iterable<Object>) ei.get.apply();

        final Object finalKey = entityKey;
        final Object rt = Enumerable.create(iter).firstOrNull(new Predicate1<Object>() {
            public boolean apply(Object input) {
                Object id = ei.id.apply(input);

                return finalKey.equals(id);
            }
        });
        if (rt == null)
            throw new NotFoundException();

        final OEntity oe = toOEntity(ei, rt);

        return new EntityResponse() {

            @Override
            public OEntity getEntity() {
                return oe;
            }

            @Override
            public EdmEntitySet getEntitySet() {
                return ees;
            }
        };
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
    public void mergeEntity(String entitySetName, Object entityKey, OEntity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEntity(String entitySetName, Object entityKey, OEntity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEntity(String entitySetName, Object entityKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityResponse createEntity(String entitySetName, OEntity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavPropertyResponse getNavProperty(String entitySetName, Object entityKey, String navProp, QueryInfo queryInfo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
