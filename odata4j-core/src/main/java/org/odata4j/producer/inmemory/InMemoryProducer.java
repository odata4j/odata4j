package org.odata4j.producer.inmemory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Func1;
import org.core4j.Predicate1;
import org.odata4j.core.OAtomStreamEntity;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmDecorator;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.Expression;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.expression.OrderByExpression.Direction;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.edm.MetadataProducer;
import org.odata4j.producer.exceptions.NotFoundException;
import org.odata4j.producer.exceptions.NotImplementedException;

/**
 * An in-memory implementation of an ODATA Producer.  Uses the standard Java bean
 * and property model to access information within entities.
 */
public class InMemoryProducer implements ODataProducer {

  private static final String ID_PROPNAME = "EntityId";

  private final String namespace;
  private final int maxResults;
  private final Map<String, InMemoryEntityInfo<?, ?>> eis = new HashMap<String, InMemoryEntityInfo<?, ?>>();
  private EdmDataServices metadata;
  private final EdmDecorator decorator;
  private final MetadataProducer metadataProducer;
  private final InMemoryTypeMapping typeMapping;

  private static final int DEFAULT_MAX_RESULTS = 100;

  /**
   * Creates a new instance of an in-memory POJO producer.
   *
   * @param namespace  the namespace of the schema registrations
   */
  public InMemoryProducer(String namespace) {
    this(namespace, DEFAULT_MAX_RESULTS);
  }

  /**
   * Creates a new instance of an in-memory POJO producer.
   *
   * @param namespace  the namespace of the schema registrations
   * @param maxResults  the maximum number of entities to return in a single call
   */
  public InMemoryProducer(String namespace, int maxResults) {
    this(namespace, maxResults, null, null);
  }

  /**
   * Creates a new instance of an in-memory POJO producer.
   *
   * @param namespace  the namespace of the schema registrations
   * @param maxResults  the maximum number of entities to return in a single call
   * @param decorator  a decorator to use for edm customizations
   * @param typeMapping  optional mapping between java types and edm types, null for default
   */
  public InMemoryProducer(String namespace, int maxResults, EdmDecorator decorator, InMemoryTypeMapping typeMapping) {
    this.namespace = namespace;
    this.maxResults = maxResults;
    this.decorator = decorator;
    this.metadataProducer = new MetadataProducer(this, decorator);
    this.typeMapping = typeMapping == null ? InMemoryTypeMapping.DEFAULT : typeMapping;
  }

  @Override
  public EdmDataServices getMetadata() {
    if (metadata == null) {
      metadata = newEdmGenerator(namespace, typeMapping, ID_PROPNAME, eis).generateEdm(decorator).build();
    }
    return metadata;
  }

  protected InMemoryEdmGenerator newEdmGenerator(String namespace, InMemoryTypeMapping typeMapping, String idPropName, Map<String, InMemoryEntityInfo<?, ?>> eis) {
    return new InMemoryEdmGenerator(namespace, typeMapping, ID_PROPNAME, eis);
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    return metadataProducer;
  }

  @Override
  public void close() {

  }

  /**
   * Registers a new entity based on a POJO, with support for composite keys.
   *
   * @param entityClass  the class of the entities that are to be stored in the set
   * @param entitySetName  the alias the set will be known by; this is what is used in the OData url
   * @param get  a function to iterate over the elements in the set
   * @param keys  one or more keys for the entity
   */
  public <TEntity> void register(Class<TEntity> entityClass, String entitySetName, Func<Iterable<TEntity>> get, String... keys) {
    register(entityClass, entitySetName, entitySetName, get, keys);
  }

  /**
   * Registers a new entity based on a POJO, with support for composite keys.
   * 
   * @param entityClass  the class of the entities that are to be stored in the set
   * @param entitySetName  the alias the set will be known by; this is what is used in the OData url
   * @param entityTypeName  type name of the entity
   * @param get  a function to iterate over the elements in the set
   * @param keys  one or more keys for the entity
   */
  public <TEntity> void register(Class<TEntity> entityClass, String entitySetName, String entityTypeName, Func<Iterable<TEntity>> get, String... keys) {
    PropertyModel model = new BeanBasedPropertyModel(entityClass);
    model = new EnumsAsStringsPropertyModelDelegate(model);
    register(entityClass, model, entitySetName, entityTypeName, get, keys);
  }

  /**
   * Registers a new entity set based on a POJO type using the default property model.
   */
  public <TEntity, TKey> void register(Class<TEntity> entityClass, Class<TKey> keyClass, String entitySetName, Func<Iterable<TEntity>> get, Func1<TEntity, TKey> id) {
    PropertyModel model = new BeanBasedPropertyModel(entityClass);
    model = new EnumsAsStringsPropertyModelDelegate(model);
    model = new EntityIdFunctionPropertyModelDelegate<TEntity, TKey>(model, ID_PROPNAME, keyClass, id);
    register(entityClass, model, entitySetName, get, ID_PROPNAME);
  }

  /**
   * Registers a new entity set based on a POJO type and a property model.
   *
   * @param entityClass  the class of the entities that are to be stored in the set
   * @param propertyModel a way to get/set properties on the POJO
   * @param entitySetName  the alias the set will be known by; this is what is used in the ODATA URL
   * @param get  a function to iterate over the elements in the set
   * @param keys  one or more keys for the entity
   */
  public <TEntity, TKey> void register(
      Class<TEntity> entityClass,
      PropertyModel propertyModel,
      String entitySetName,
      Func<Iterable<TEntity>> get,
      String... keys) {
    register(entityClass, propertyModel, entitySetName, entitySetName, get, keys);
  }

  public <TEntity, TKey> void register(
      final Class<TEntity> entityClass,
      final PropertyModel propertyModel,
      final String entitySetName,
      final String entityTypeName,
      final Func<Iterable<TEntity>> get,
      final String... keys) {

    InMemoryEntityInfo<TEntity, TKey> ei = new InMemoryEntityInfo<TEntity, TKey>();
    ei.entitySetName = entitySetName;
    ei.entityTypeName = entityTypeName;
    ei.properties = propertyModel;
    ei.get = get;
    ei.keys = keys;
    ei.entityClass = entityClass;
    ei.hasStream = OAtomStreamEntity.class.isAssignableFrom(entityClass);

    ei.id = new Func1<Object, HashMap<String, Object>>() {
      @Override
      public HashMap<String, Object> apply(Object input) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        for (String key : keys) {
          values.put(key, eis.get(entitySetName).properties.getPropertyValue(input, key));
        }
        return values;
      }
    };

    eis.put(entitySetName, ei);
    metadata = null;
  }

  protected OEntity toOEntity(EdmEntitySet ees, Object obj, List<EntitySimpleProperty> expand) {
    InMemoryEntityInfo<?, ?> ei = eis.get(ees.getName());
    final List<OLink> links = new ArrayList<OLink>();
    final List<OProperty<?>> properties = new ArrayList<OProperty<?>>();

    Map<String, Object> keyKVPair = new HashMap<String, Object>();
    for (String key : ei.keys) {
      Object keyValue = ei.properties.getPropertyValue(obj, key);
      keyKVPair.put(key, keyValue);
    }

    for (String propName : ei.properties.getPropertyNames()) {
      EdmSimpleType<?> type;
      Object value = ei.properties.getPropertyValue(obj, propName);
      Class<?> propType = ei.properties.getPropertyType(propName);
      type = typeMapping.findEdmType(propType);
      if (type == null) continue;

      properties.add(OProperties.simple(propName, type, value));
    }

    if (expand != null && !expand.isEmpty()) {
      EdmEntityType edmEntityType = ees.getType();

      HashMap<String, List<EntitySimpleProperty>> expandedProps = new HashMap<String, List<EntitySimpleProperty>>();

      //process all the expanded properties and add them to map
      for (final EntitySimpleProperty propPath : expand) {
        String[] props = propPath.getPropertyName().split("/", 2);
        String prop = props[0];
        String remainingPropPath = props.length > 1 ? props[1] : null;
        //if link is already set to be expanded, add other remaining prop path to the list
        if (expandedProps.containsKey(prop)) {
          if (remainingPropPath != null) {
            List<EntitySimpleProperty> remainingPropPaths = expandedProps.get(prop);
            remainingPropPaths.add(Expression.simpleProperty(remainingPropPath));
          }
        } else {
          List<EntitySimpleProperty> remainingPropPaths = new ArrayList<EntitySimpleProperty>();
          if (remainingPropPath != null)
            remainingPropPaths.add(Expression.simpleProperty(remainingPropPath));
          expandedProps.put(prop, remainingPropPaths);
        }
      }

      for (final String prop : expandedProps.keySet()) {
        List<EntitySimpleProperty> remainingPropPath = expandedProps.get(prop);

        EdmNavigationProperty edmNavProperty = edmEntityType.findNavigationProperty(prop);

        if (edmNavProperty == null) continue;

        if (edmNavProperty.getToRole().getMultiplicity() == EdmMultiplicity.MANY) {
          List<OEntity> relatedEntities = new ArrayList<OEntity>();
          Iterable<?> values = ei.properties.getCollectionValue(obj, prop);
          if (values != null) {
            EdmEntitySet relEntitySet = null;

            for (final Object entity : values) {
              if (relEntitySet == null) {
                InMemoryEntityInfo<?, ?> oei = Enumerable.create(eis.values()).firstOrNull(new Predicate1<InMemoryEntityInfo<?, ?>>() {
                  @Override
                  public boolean apply(InMemoryEntityInfo<?, ?> input) {
                    return entity.getClass().equals(input.entityClass);
                  }
                });
                relEntitySet = getMetadata().getEdmEntitySet(oei.entitySetName);
              }

              relatedEntities.add(toOEntity(relEntitySet, entity, remainingPropPath));
            }
          }
          // relation and href will be filled in later for atom or json
          links.add(OLinks.relatedEntitiesInline(null, edmNavProperty.getName(), null, relatedEntities));
        } else {
          final Object entity = ei.properties.getPropertyValue(obj, prop);
          OEntity relatedEntity = null;

          if (entity != null) {
            InMemoryEntityInfo<?, ?> oei = Enumerable.create(eis.values()).firstOrNull(new Predicate1<InMemoryEntityInfo<?, ?>>() {
              @Override
              public boolean apply(InMemoryEntityInfo<?, ?> input) {
                return entity.getClass().equals(input.entityClass);
              }
            });

            EdmEntitySet relEntitySet = getMetadata().getEdmEntitySet(oei.entitySetName);

            relatedEntity = toOEntity(relEntitySet, entity, remainingPropPath);
          }
          links.add(OLinks.relatedEntityInline(null, edmNavProperty.getName(), null, relatedEntity));
        }
      }
    }

    // for every navigation propety that we didn' expand we must place an deferred
    // OLink if the nav prop is selected
    for (final EdmNavigationProperty ep : ees.getType().getNavigationProperties()) {
      // if $select is ever supported, check here and only include nave props
      // that are selected
      boolean expanded = null != Enumerable.create(links).firstOrNull(new Predicate1<OLink>() {

        @Override
        public boolean apply(OLink t) {
          return t.getTitle().equals(ep.getName());
        }
      });

      if (!expanded) {
        // defer
        if (ep.getToRole().getMultiplicity() == EdmMultiplicity.MANY) {
          links.add(OLinks.relatedEntities(null, ep.getName(), null));
        } else {
          links.add(OLinks.relatedEntity(null, ep.getName(), null));
        }
      }
    }

    return OEntities.create(ees, OEntityKey.create(keyKVPair), properties, links, obj);
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
    final EdmEntitySet ees = getMetadata().getEdmEntitySet(entitySetName);
    final InMemoryEntityInfo<?, ?> ei = eis.get(entitySetName);

    Enumerable<Object> objects = Enumerable.create(ei.get.apply()).cast(Object.class);

    // apply filter
    if (queryInfo != null && queryInfo.filter != null) {
      objects = objects.where(filterToPredicate(queryInfo.filter, ei.properties));
    }

    // compute inlineCount, must be done after applying filter
    Integer inlineCount = null;
    if (queryInfo != null && queryInfo.inlineCount == InlineCount.ALLPAGES) {
      objects = Enumerable.create(objects.toList()); // materialize up front, since we're about to count
      inlineCount = objects.count();
    }

    // apply ordering
    if (queryInfo != null && queryInfo.orderBy != null) {
      objects = orderBy(objects, queryInfo.orderBy, ei.properties);
    }

    // work with oentities
    Enumerable<OEntity> entities = objects.select(new Func1<Object, OEntity>() {
      public OEntity apply(Object input) {
        return toOEntity(ees, input, queryInfo != null ? queryInfo.expand : null);
      }
    });

    // skip records by $skipToken
    if (queryInfo != null && queryInfo.skipToken != null) {
      final Boolean[] skipping = new Boolean[] { true };
      entities = entities.skipWhile(new Predicate1<OEntity>() {
        public boolean apply(OEntity input) {
          if (skipping[0]) {
            String inputKey = input.getEntityKey().toKeyString();
            if (queryInfo.skipToken.equals(inputKey)) skipping[0] = false;
            return true;
          }
          return false;
        }
      });
    }

    // skip records by $skip amount
    if (queryInfo != null && queryInfo.skip != null) {
      entities = entities.skip(queryInfo.skip);
    }

    // apply limit
    int limit = this.maxResults;
    if (queryInfo != null && queryInfo.top != null && queryInfo.top < limit) {
      limit = queryInfo.top;
    }
    entities = entities.take(limit + 1);

    // materialize OEntities
    List<OEntity> entitiesList = entities.toList();

    // determine skipToken if necessary
    String skipToken = null;
    if (entitiesList.size() > limit) {
      entitiesList = Enumerable.create(entitiesList).take(limit).toList();
      skipToken = entitiesList.size() == 0 ? null : Enumerable.create(entitiesList).last().getEntityKey().toKeyString();
    }

    return Responses.entities(entitiesList, ees, inlineCount, skipToken);

  }

  private Enumerable<Object> orderBy(Enumerable<Object> iter, List<OrderByExpression> orderBys, final PropertyModel properties) {
    for (final OrderByExpression orderBy : Enumerable.create(orderBys).reverse())
      iter = iter.orderBy(new Comparator<Object>() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public int compare(Object o1, Object o2) {
          Comparable lhs = (Comparable) InMemoryEvaluation.evaluate(orderBy.getExpression(), o1, properties);
          Comparable rhs = (Comparable) InMemoryEvaluation.evaluate(orderBy.getExpression(), o2, properties);
          return (orderBy.getDirection() == Direction.ASCENDING ? 1 : -1) * lhs.compareTo(rhs);
        }
      });
    return iter;
  }

  @SuppressWarnings("unchecked")
  @Override
  public EntityResponse getEntity(String entitySetName, final OEntityKey entityKey, QueryInfo queryInfo) {
    final EdmEntitySet ees = getMetadata().getEdmEntitySet(entitySetName);
    final InMemoryEntityInfo<?, ?> ei = eis.get(entitySetName);

    final String[] keyList = ei.keys;

    Iterable<Object> iter = (Iterable<Object>) ei.get.apply();

    final Object rt = Enumerable.create(iter).firstOrNull(new Predicate1<Object>() {
      public boolean apply(Object input) {
        HashMap<String, Object> idObjectMap = ei.id.apply(input);

        if (keyList.length == 1) {
          Object idValue = entityKey.asSingleValue();
          return idObjectMap.get(keyList[0]).equals(idValue);
        } else if (keyList.length > 1) {
          for (String key : keyList) {
            Object curValue = null;
            Iterator<OProperty<?>> keyProps = entityKey.asComplexProperties().iterator();
            while (keyProps.hasNext()) {
              OProperty<?> keyProp = keyProps.next();
              if (keyProp.getName().equalsIgnoreCase(key)) {
                curValue = keyProp.getValue();
              }
            }
            if (curValue == null) {
              return false;
            } else if (!idObjectMap.get(key).equals(curValue)) {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }
      }
    });
    if (rt == null) throw new NotFoundException();

    OEntity oe = toOEntity(ees, rt, queryInfo.expand);

    return Responses.entity(oe);
  }

  @Override
  public void mergeEntity(String entitySetName, OEntity entity) {
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
  public BaseResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    throw new NotImplementedException();
  }

  @Override
  public EntityIdResponse getLinks(OEntityId sourceEntity, String targetNavProp) {
    throw new NotImplementedException();
  }

  @Override
  public void createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    throw new NotImplementedException();
  }

  @Override
  public void updateLink(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteLink(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
    throw new NotImplementedException();
  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport name, java.util.Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    throw new NotImplementedException();
  }

}
