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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
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
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.IEdmDecorator;
import org.odata4j.edm.IEdmGenerator;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;
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
public class InMemoryProducer implements ODataProducer, IEdmGenerator {

  private final Logger log = Logger.getLogger(getClass().getName());

 

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
  private static final Map<Class<?>, EdmSimpleType> SUPPORTED_TYPES = new HashMap<Class<?>, EdmSimpleType>();
  static {
    SUPPORTED_TYPES.put(byte[].class, EdmSimpleType.BINARY);
    SUPPORTED_TYPES.put(Boolean.class, EdmSimpleType.BOOLEAN);
    SUPPORTED_TYPES.put(Boolean.TYPE, EdmSimpleType.BOOLEAN);
    SUPPORTED_TYPES.put(byte.class, EdmSimpleType.BYTE);
    SUPPORTED_TYPES.put(LocalDateTime.class, EdmSimpleType.DATETIME);
    SUPPORTED_TYPES.put(BigDecimal.class, EdmSimpleType.DECIMAL);
    SUPPORTED_TYPES.put(Double.class, EdmSimpleType.DOUBLE);
    SUPPORTED_TYPES.put(Double.TYPE, EdmSimpleType.DOUBLE);
    SUPPORTED_TYPES.put(Guid.class, EdmSimpleType.GUID);
    SUPPORTED_TYPES.put(Short.class, EdmSimpleType.INT16);
    SUPPORTED_TYPES.put(Short.TYPE, EdmSimpleType.INT16);
    SUPPORTED_TYPES.put(Integer.class, EdmSimpleType.INT32);
    SUPPORTED_TYPES.put(Integer.TYPE, EdmSimpleType.INT32);
    SUPPORTED_TYPES.put(Long.class, EdmSimpleType.INT64);
    SUPPORTED_TYPES.put(Long.TYPE, EdmSimpleType.INT64);
    SUPPORTED_TYPES.put(Float.class, EdmSimpleType.SINGLE);
    SUPPORTED_TYPES.put(Float.TYPE, EdmSimpleType.SINGLE);
    SUPPORTED_TYPES.put(String.class, EdmSimpleType.STRING);
    SUPPORTED_TYPES.put(LocalTime.class, EdmSimpleType.TIME);
    SUPPORTED_TYPES.put(DateTime.class, EdmSimpleType.DATETIMEOFFSET);
    SUPPORTED_TYPES.put(Date.class, EdmSimpleType.DATETIME);

    SUPPORTED_TYPES.put(Object.class, EdmSimpleType.STRING);
  }

  private final String namespace;
  private final int maxResults;
  private final Map<String, EntityInfo<?, ?>> eis = new HashMap<String, EntityInfo<?, ?>>();
  private EdmDataServices metadata;
  private final IEdmDecorator decorator;
  private final MetadataProducer metadataProducer;
  
  private static final int DEFAULT_MAX_RESULTS = 100;

  /** Create a new instance of an in-memory POJO/JPA producer
   * 
   * @param namespace - the namespace that the schema registrations will be in
   */
  public InMemoryProducer(String namespace) {
    this(namespace, DEFAULT_MAX_RESULTS);
  }

  /** Create a new instance of an in-memory POJO/JPA producer
   * 
   * @param namespace - the names apce that the schema registrations will be in
   * @param maxResults - the maximum number of entities to return
   */
  public InMemoryProducer(String namespace, int maxResults) {
    this(namespace, maxResults, null);
  }
  
  public InMemoryProducer(String namespace, int maxResults, IEdmDecorator decorator) {
    this.namespace = namespace;
    this.maxResults = maxResults;
    this.decorator = decorator;
    metadataProducer = new MetadataProducer(this, decorator);
  }

  @Override
  public EdmDataServices getMetadata() {
    if (metadata == null) {
      metadata = buildMetadata();
    }
    return metadata;
  }
  
  @Override
  public MetadataProducer getMetadataProducer() {
    return metadataProducer;
  }

  public IEdmDecorator getDecorator() {
    return this.decorator;
  }

  public EdmDataServices generateEdm() {
    return null == metadata ? getMetadata() : metadata;
  }
  
  private EdmDataServices buildMetadata() {

    List<EdmSchema> schemas = new ArrayList<EdmSchema>();
    List<EdmEntityContainer> containers = new ArrayList<EdmEntityContainer>();
    List<EdmEntitySet> entitySets = new ArrayList<EdmEntitySet>();
    List<EdmEntityType> entityTypes = new ArrayList<EdmEntityType>();
    List<EdmAssociation> associations = new ArrayList<EdmAssociation>();
    List<EdmAssociationSet> associationSets = new ArrayList<EdmAssociationSet>();

    // creates id other basic SUPPORTED_TYPE properties(structural) entities
    createStructuralEntities(entitySets, entityTypes);

    // TODO handle back references too
    // create hashmaps from sets
    // --------------------------------------
    // create entityname:entityTypes
    Map<String, EdmEntityType> entityTypesByName = Enumerable.create(
            entityTypes).toMap(new Func1<EdmEntityType, String>() {
      public String apply(EdmEntityType input) {
        return input.name;
      }
    });

    // create entityname:entitySet
    Map<String, EdmEntitySet> entitySetByName = Enumerable.create(
            entitySets).toMap(new Func1<EdmEntitySet, String>() {
      public String apply(EdmEntitySet input) {
        return input.name;
      }
    });

    Map<Class<?>, String> entityNameByClass = new HashMap<Class<?>, String>();

    for (Entry<String, EntityInfo<?, ?>> e : eis.entrySet())
      entityNameByClass.put(e.getValue().entityClass, e.getKey());

    createNavigationProperties(associations, associationSets,
            entityTypesByName, entitySetByName, entityNameByClass);

    EdmEntityContainer container = new EdmEntityContainer(CONTAINER_NAME, true,
        null, entitySets, associationSets, null);

    containers.add(container);

    EdmSchema schema = new EdmSchema(namespace, null, entityTypes, null,
            associations, containers,
            null == decorator ? null : decorator.getDocumentationForSchema(namespace, namespace),
            null == decorator ? null : decorator.getAnnotationsForSchema(namespace, namespace));
    
    schemas.add(schema);
    EdmDataServices rt = new EdmDataServices(
            ODataConstants.DATA_SERVICE_VERSION, schemas,
            null == decorator ? null : decorator.getNamespaces());
    return rt;
  }

  private void createStructuralEntities(List<EdmEntitySet> entitySets,
      List<EdmEntityType> entityTypes) {

    for (String entitySetName : eis.keySet()) {
      EntityInfo<?, ?> entityInfo = eis.get(entitySetName);

      List<EdmProperty> properties = new ArrayList<EdmProperty>();

      properties.addAll(toEdmProperties(entityInfo.properties, entitySetName));

      EdmEntityType eet = new EdmEntityType(
              namespace, 
              null,       // alias
              entitySetName, 
              null,     // hasSream
              Enumerable.create(ID_PROPNAME).toList(),  // keys
              null,     // basetype
              properties, 
              null,     // nav props
              null == this.decorator ? null : this.decorator.getDocumentationForEntityType(namespace, entitySetName), 
              null == this.decorator ? null : this.decorator.getAnnotationsForEntityType(namespace, entitySetName));

      EdmEntitySet ees = new EdmEntitySet(entitySetName, eet);

      entitySets.add(ees);
      entityTypes.add(eet);
    }
  }

  private void createNavigationProperties(List<EdmAssociation> associations,
      List<EdmAssociationSet> associationSets,
      Map<String, EdmEntityType> entityTypesByName,
      Map<String, EdmEntitySet> entitySetByName,
      Map<Class<?>, String> entityNameByClass) {

    for (String entitySetName : eis.keySet()) {
      EntityInfo<?, ?> ei = eis.get(entitySetName);
      Class<?> clazz1 = ei.entityClass;

      generateToOneNavProperties(associations, associationSets,
              entityTypesByName, entitySetByName, entityNameByClass,
              entitySetName, ei);

      generateToManyNavProperties(associations, associationSets,
              entityTypesByName, entitySetByName, entityNameByClass,
              entitySetName, ei, clazz1);
    }
  }

  private void generateToOneNavProperties(
      List<EdmAssociation> associations,
      List<EdmAssociationSet> associationSets,
      Map<String, EdmEntityType> entityTypesByName,
      Map<String, EdmEntitySet> entitySetByName,
      Map<Class<?>, String> entityNameByClass, String entitySetName,
      EntityInfo<?, ?> ei) {

    for (String assocProp : ei.properties.getPropertyNames()) {

      EdmEntityType eet1 = entityTypesByName.get(entitySetName);
      Class<?> clazz2 = ei.properties.getPropertyType(assocProp);
      String eetName2 = entityNameByClass.get(clazz2);

      if (eet1.findProperty(assocProp) != null || eetName2 == null) continue;

      EdmEntityType eet2 = entityTypesByName.get(eetName2);

      EdmMultiplicity m1 = EdmMultiplicity.MANY;
      EdmMultiplicity m2 = EdmMultiplicity.ONE;

      String assocName = String.format("FK_%s_%s", eet1.name, eet2.name);
      EdmAssociationEnd assocEnd1 = new EdmAssociationEnd(eet1.name,
              eet1, m1);
      String assocEnd2Name = eet2.name;
      if (assocEnd2Name.equals(eet1.name))
          assocEnd2Name = assocEnd2Name + "1";

      EdmAssociationEnd assocEnd2 = new EdmAssociationEnd(assocEnd2Name, eet2, m2);
      EdmAssociation assoc = new EdmAssociation(namespace, null, assocName, assocEnd1, assocEnd2);

      associations.add(assoc);

      EdmEntitySet ees1 = entitySetByName.get(eet1.name);
      EdmEntitySet ees2 = entitySetByName.get(eet2.name);
      EdmAssociationSet eas = new EdmAssociationSet(assocName, assoc,
              new EdmAssociationSetEnd(assocEnd1, ees1),
              new EdmAssociationSetEnd(assocEnd2, ees2));

      associationSets.add(eas);

      EdmNavigationProperty np = new EdmNavigationProperty(assocProp,
              assoc, assoc.end1, assoc.end2);

      eet1.addNavigationProperty(np);
    }
  }

  private void generateToManyNavProperties(List<EdmAssociation> associations,
      List<EdmAssociationSet> associationSets,
      Map<String, EdmEntityType> entityTypesByName,
      Map<String, EdmEntitySet> entitySetByName,
      Map<Class<?>, String> entityNameByClass, String entitySetName,
      EntityInfo<?, ?> ei, Class<?> clazz1) {

    for (String assocProp : ei.properties.getCollectionNames()) {

      final EdmEntityType eet1 = entityTypesByName.get(entitySetName);

      Class<?> clazz2 = ei.properties.getCollectionElementType(assocProp);
      String eetName2 = entityNameByClass.get(clazz2);
      if (eetName2 == null)
        continue;
      
      final EdmEntityType eet2 = entityTypesByName.get(eetName2);

      try {
        EdmAssociation assoc = Enumerable.create(associations).firstOrNull(new Predicate1<EdmAssociation>() {

          public boolean apply(EdmAssociation input) {
            return input.end1.type.equals(eet2) && input.end2.type.equals(eet1);
          }
        });

        EdmAssociationEnd fromRole, toRole;

        if (assoc == null) {
          //no association already exists
          EdmMultiplicity m1 = EdmMultiplicity.ZERO_TO_ONE;
          EdmMultiplicity m2 = EdmMultiplicity.MANY;

          //find ei info of class2
          EntityInfo<?, ?> class2eiInfo = eis.get(eetName2);
          for (String tmp : class2eiInfo.properties.getCollectionNames()) {
            //class2 has a ref to class1
            //Class<?> tmpc = class2eiInfo.properties.getCollectionElementType(tmp);
            if (clazz1 == class2eiInfo.properties.getCollectionElementType(tmp)) {
              m1 = EdmMultiplicity.MANY;
              m2 = EdmMultiplicity.MANY;
              break;
            }
          }

          String assocName = String.format("FK_%s_%s", eet1.name, eet2.name);
          EdmAssociationEnd assocEnd1 = new EdmAssociationEnd(eet1.name, eet1, m1);
          String assocEnd2Name = eet2.name;
          if (assocEnd2Name.equals(eet1.name))
              assocEnd2Name = assocEnd2Name + "1";
          EdmAssociationEnd assocEnd2 = new EdmAssociationEnd(assocEnd2Name, eet2, m2);
          assoc = new EdmAssociation(namespace, null, assocName, assocEnd1, assocEnd2);

          associations.add(assoc);

          EdmEntitySet ees1 = entitySetByName.get(eet1.name);
          EdmEntitySet ees2 = entitySetByName.get(eet2.name);
          EdmAssociationSet eas = new EdmAssociationSet(assocName, assoc, new EdmAssociationSetEnd(assocEnd1, ees1), new EdmAssociationSetEnd(assocEnd2, ees2));
          associationSets.add(eas);

          fromRole = assoc.end1;
          toRole = assoc.end2;
        } else {
          fromRole = assoc.end2;
          toRole = assoc.end1;
        }

        EdmNavigationProperty np = new EdmNavigationProperty(assocProp, assoc, fromRole, toRole);

        eet1.addNavigationProperty(np);
      } catch (Exception e) {
        log.log(Level.WARNING, "Exception building Edm associations: " + e.getMessage(), e);
      }
    }
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
  public <TEntity, TKey> void register(Class<TEntity> entityClass, Class<TKey> keyClass, String entitySetName, Func<Iterable<TEntity>> get, Func1<TEntity, TKey> id) {
    PropertyModel model = new BeanBasedPropertyModel(entityClass);
    model = new EnumsAsStringsPropertyModelDelegate(model);
    model = new EntityIdFunctionPropertyModelDelegate<TEntity, TKey>(model, ID_PROPNAME, keyClass, id);
    register(entityClass, model, keyClass, entitySetName, get, id);
  }

  public <TEntity, TKey> void register(
      Class<TEntity> entityClass,
      PropertyModel propertyModel,
      Class<TKey> keyClass,
      String entitySetName,
      Func<Iterable<TEntity>> get,
      Func1<TEntity, TKey> id) {

    EntityInfo<TEntity, TKey> ei = new EntityInfo<TEntity, TKey>();
    ei.entitySetName = entitySetName;
    ei.properties = propertyModel;
    ei.get = get;
    ei.id = widen(id);
    ei.keyClass = keyClass;
    ei.entityClass = entityClass;

    eis.put(entitySetName, ei);
    metadata = null;
  }

  protected OEntity toOEntity(EdmEntitySet ees, Object obj, List<EntitySimpleProperty> expand) {
    EntityInfo<?, ?> ei = eis.get(ees.name);
    final List<OLink> links = new ArrayList<OLink>();
    final List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
    
    Object keyValue = ei.properties.getPropertyValue(obj, ID_PROPNAME);

    for (String propName : ei.properties.getPropertyNames()) {
      EdmSimpleType type;
      Object value = ei.properties.getPropertyValue(obj, propName);
      Class<?> propType = ei.properties.getPropertyType(propName);
      type = findEdmType(propType);
      if (type == null) continue;

      properties.add(OProperties.simple(propName, type, value));
    }

    if (expand != null && !expand.isEmpty()) {
      EdmEntityType edmEntityType = ees.type;

      for (final EntitySimpleProperty propPath : expand) {

        String[] props = propPath.getPropertyName().split("/", 2);
        String prop = props[0];
        List<EntitySimpleProperty> remainingPropPath = props.length > 1 ? Arrays.asList(org.odata4j.expression.Expression.simpleProperty(props[1])) : null;

        EdmNavigationProperty edmNavProperty = edmEntityType.findNavigationProperty(prop);

        if (edmNavProperty == null) continue;

        if (edmNavProperty.toRole.multiplicity == EdmMultiplicity.MANY) {
          List<OEntity> relatedEntities = new ArrayList<OEntity>();
          Iterable<?> values = ei.properties.getCollectionValue(obj, prop);
          if (values != null) {
            EdmEntitySet relEntitySet = null;

            for (final Object entity : values) {
              if (relEntitySet == null) {
                EntityInfo<?, ?> oei = Enumerable.create(eis.values()).firstOrNull(new Predicate1<InMemoryProducer.EntityInfo<?, ?>>() {
                  @Override
                  public boolean apply(EntityInfo<?, ?> input) {
                    return entity.getClass().equals(input.entityClass);
                  }
                });
                relEntitySet = getMetadata().getEdmEntitySet(oei.entitySetName);
              }

              relatedEntities.add(toOEntity(relEntitySet, entity, remainingPropPath));
            }
          }
          // relation and href will be filled in later for atom or json
          links.add(OLinks.relatedEntitiesInline(null, edmNavProperty.name, null, relatedEntities));
        } else {
          final Object entity = ei.properties.getPropertyValue(obj, prop);
          OEntity relatedEntity = null;

          if (entity != null) {
            EntityInfo<?, ?> oei = Enumerable.create(eis.values()).firstOrNull(new Predicate1<InMemoryProducer.EntityInfo<?, ?>>() {
              @Override
              public boolean apply(EntityInfo<?, ?> input) {
                return entity.getClass().equals(input.entityClass);
              }
            });

            EdmEntitySet relEntitySet = getMetadata().getEdmEntitySet(oei.entitySetName);

            relatedEntity = toOEntity(relEntitySet, entity, remainingPropPath);
          }
          links.add(OLinks.relatedEntityInline(null, edmNavProperty.name, null, relatedEntity));
        }
      }
    }

    // for every navigation propety that we didn' expand we must place an deferred
    // OLink if the nav prop is selected
    for (final EdmNavigationProperty ep : ees.type.getNavigationProperties()) {
      // if $select is ever supported, check here and only include nave props
      // that are selected
      boolean expanded = null != Enumerable.create(links).firstOrNull(new Predicate1<OLink>() {

        @Override
        public boolean apply(OLink t) {
          return t.getTitle().equals(ep.name);
          }
      });

      if (!expanded) {
        // defer
        if (ep.toRole.multiplicity == EdmMultiplicity.MANY) {
          links.add(OLinks.relatedEntities(null, ep.name, null));
        } else {
          links.add(OLinks.relatedEntity(null, ep.name, null));
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
    final EdmEntitySet ees = getMetadata().getEdmEntitySet(entitySetName);
    final EntityInfo<?, ?> ei = eis.get(entitySetName);

    Enumerable<Object> objects = Enumerable.create(ei.get.apply()).cast(Object.class);

    // apply filter
    if (queryInfo.filter != null) objects = objects.where(filterToPredicate(queryInfo.filter, ei.properties));

    // compute inlineCount
    Integer inlineCount = queryInfo.inlineCount == InlineCount.ALLPAGES ? objects.count() : null;

    // apply ordering
    if (queryInfo.orderBy != null) objects = orderBy(objects, queryInfo.orderBy, ei.properties);

    // work with oentities
    Enumerable<OEntity> entities = objects.select(new Func1<Object, OEntity>() {
      public OEntity apply(Object input) {
        return toOEntity(ees, input, queryInfo.expand);
      }
    });

    // skip records by $skipToken
    if (queryInfo.skipToken != null) {
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
    if (queryInfo.skip != null) entities = entities.skip(queryInfo.skip);

    // apply limit
    int limit = this.maxResults;
    if (queryInfo.top != null && queryInfo.top < limit) limit = queryInfo.top;
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
          return (orderBy.isAscending() ? 1 : -1) * lhs.compareTo(rhs);
        }
      });
    return iter;
  }

  @SuppressWarnings("unchecked")
  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, QueryInfo queryInfo) {
    final EdmEntitySet ees = getMetadata().getEdmEntitySet(entitySetName);
    final EntityInfo<?, ?> ei = eis.get(entitySetName);

    final Object idValue = InMemoryEvaluation.cast(entityKey.asSingleValue(), ei.keyClass);

    Iterable<Object> iter = (Iterable<Object>) ei.get.apply();

    final Object rt = Enumerable.create(iter).firstOrNull(new Predicate1<Object>() {
      public boolean apply(Object input) {
        Object id = ei.id.apply(input);
        return idValue.equals(id);
      }
    });
    if (rt == null) throw new NotFoundException();

    OEntity oe = toOEntity(ees, rt, queryInfo.expand);

    return Responses.entity(oe);
  }

  private Collection<EdmProperty> toEdmProperties(PropertyModel model, String structuralTypename) {
    List<EdmProperty> rt = new ArrayList<EdmProperty>();

    for (String propName : model.getPropertyNames()) {
      Class<?> propType = model.getPropertyType(propName);
      EdmSimpleType type = findEdmType(propType);
      if (type == null) continue;
      rt.add(new EdmProperty(propName, type, true, null, null, null, null, null, null, null, null, null, CollectionKind.None, 
              null == this.decorator ? null : this.decorator.getDocumentationForProperty(namespace, structuralTypename, propName), 
              null == this.decorator ? null : this.decorator.getAnnotationsForProperty(namespace, structuralTypename, propName)));
    }

    return rt;
  }

  @SuppressWarnings("unused")
  private EdmSimpleType getEdmType(Class<?> clazz) {
    EdmSimpleType type = findEdmType(clazz);
    if (type != null) return type;
    throw new UnsupportedOperationException(clazz.getName());
  }

  private EdmSimpleType findEdmType(Class<?> clazz) {
    EdmSimpleType type = SUPPORTED_TYPES.get(clazz);
    if (type != null) return type;
    return null;
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
  public EntitiesResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
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
