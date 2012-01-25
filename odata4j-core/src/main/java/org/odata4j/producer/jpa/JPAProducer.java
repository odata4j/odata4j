package org.odata4j.producer.jpa;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.core4j.Predicate1;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmDecorator;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmPropertyBase;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.Expression;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.expression.OrderByExpression.Direction;
import org.odata4j.internal.TypeConverter;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.CountResponse;
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
import org.odata4j.producer.jpa.JPAProducer.Context.EntityAccessor;

public class JPAProducer implements ODataProducer {

  public enum CommandType {
    CreateEntity,
    GetEntities,
    GetEntity,
    CreateAndLink,
    DeleteEntity,
    MergeEntity,
    UpdateEntity,
    GetLinks,
    GetCount
  };

  public interface Command {
    public boolean execute(Context context);
  }

  public interface Filter extends Command {
    public boolean postProcess(Context context, Exception exception);
  }

  public interface JPAProducerBehavior {
    public List<Command> modify(CommandType type, List<Command> commands);
  }

  private final EntityManagerFactory emf;
  private final EdmDataServices metadata;
  private final int maxResults;
  private final MetadataProducer metadataProducer;
  private Command createEntityCommand;
  private Command createAndLinkCommand;
  private Command getEntitiesCommand;
  private Command getEntityCommand;
  private Command deleteEntityCommand;
  private Command mergeEntityCommand;
  private Command updateEntityCommand;
  private Command getLinksCommand;
  private Command getCountCommand;
  private JPAProducerBehavior producerBehavior;

  public JPAProducer(
      EntityManagerFactory emf,
      String namespace,
      int maxResults) {
    this(emf, new JPAEdmGenerator(emf, namespace).generateEdm(null).build(), maxResults, null, null);
  }

  public JPAProducer(
      EntityManagerFactory emf,
      EdmDataServices metadata,
      int maxResults) {
    this(emf, metadata, maxResults, null, null);
  }

  public JPAProducer(
      EntityManagerFactory emf,
      EdmDataServices metadata,
      int maxResults,
      EdmDecorator metadataDecorator) {
    this(emf, metadata, maxResults, metadataDecorator, null);
  }

  public JPAProducer(
      EntityManagerFactory emf,
      EdmDataServices metadata,
      int maxResults,
      EdmDecorator metadataDecorator,
      JPAProducerBehavior producerBehavior) {

    this.emf = emf;
    this.maxResults = maxResults;
    this.metadata = metadata;
    this.metadataProducer = new MetadataProducer(this, metadataDecorator);
    this.producerBehavior = producerBehavior;

    initCommandChains();
  }

  protected void initCommandChains() {
    List<Command> commands = new ArrayList<Command>();
    /* query processors */
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // parse generate the JPQL query
    commands.add(new GenerateJPQLCommand());
    // execute the JPQL query
    commands.add(new ExecuteJPQLQueryCommand(maxResults));
    // TODO ExecuteJPQLQueryProcessor should only execute the query
    // and set the result in the context
    // add a processor which converts the query result into a BaseResponse
    // and sets this into the response of the context. This allows for
    // inspecting and modifying the found enities.
    getEntitiesCommand = createChain(CommandType.GetEntities, commands);

    /* initialize the create processors */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // begin a transaction
    commands.add(new BeginTransactionCommand());
    // convert the given OEntity to a JPAEntity
    commands.add(new OEntityToJPAEntityCommand(true));
    // persist the JPAEntity
    commands.add(new PersistJPAEntityCommand());
    // commit the transaction
    commands.add(new CommitTransactionCommand());
    // reread the JPAEntity if necessary
    commands.add(new ReReadJPAEntityCommand());
    // convert the JPAEntity back to an OEntity
    commands.add(new JPAEntityToOEntityCommand());
    // set this OEntity as Response entity
    commands.add(new SetOEntityResponseCommand());
    createEntityCommand = createChain(CommandType.CreateEntity, commands);

    /* create and link processors */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // begin a transaction
    commands.add(new BeginTransactionCommand());
    // get the entity we want the new entity add to (parent entity)
    commands.add(new GetEntityCommand());
    // convert the given new OEntity to a new JPAEntity
    commands.add(new OEntityToJPAEntityCommand(Context.EntityAccessor.OTHER, true));
    // add the new JPAEntity to the parent entity
    commands.add(new CreateAndLinkCommand());
    // commit the transaction
    commands.add(new CommitTransactionCommand());
    // convert the new JPAEntity back to an OEntity
    commands.add(new JPAEntityToOEntityCommand(Context.EntityAccessor.OTHER));
    // set this new OEntity as Response entity
    commands.add(new SetOEntityResponseCommand(Context.EntityAccessor.OTHER));
    createAndLinkCommand = createChain(CommandType.CreateAndLink, commands);

    /* get entity processors */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // get the requested JPAEntity
    commands.add(new GetEntityCommand());
    // convert the JPAEntity to an OEntity
    commands.add(new JPAEntityToOEntityCommand());
    // set this OEntity as Response entity
    commands.add(new SetOEntityResponseCommand());
    getEntityCommand = createChain(CommandType.GetEntity, commands);

    /* delete entity processors */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // begin transaction
    commands.add(new BeginTransactionCommand());
    // get the JPAEntity to delete
    commands.add(new GetEntityCommand());
    // delete the JPAEntity
    commands.add(new DeleteEntityCommand());
    // commit the transaction
    commands.add(new CommitTransactionCommand());
    // the response stays empty
    deleteEntityCommand = createChain(CommandType.DeleteEntity, commands);

    /* merge entity processors */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // begin transaction
    commands.add(new BeginTransactionCommand());
    // get the JPAEntity to delete
    commands.add(new GetEntityCommand());
    // delete the JPAEntity
    commands.add(new MergeEntityCommand());
    // commit the transaction
    commands.add(new CommitTransactionCommand());
    // the response stays empty
    mergeEntityCommand = createChain(CommandType.MergeEntity, commands);

    /* update entity processors */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // begin transaction
    commands.add(new BeginTransactionCommand());
    // get the JPAEntity to delete
    commands.add(new OEntityToJPAEntityCommand(true));
    // delete the JPAEntity
    commands.add(new UpdateEntityCommand());
    // commit the transaction
    commands.add(new CommitTransactionCommand());
    // the response stays empty
    updateEntityCommand = createChain(CommandType.UpdateEntity, commands);

    /* get links command */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // parse generate the JPQL query
    commands.add(new GenerateJPQLCommand());
    // execute the JPQL query
    commands.add(new ExecuteJPQLQueryCommand(maxResults));
    getLinksCommand = createChain(CommandType.GetLinks, commands);

    /* get entities count processors */
    commands = new ArrayList<Command>();
    // create an EntityManager
    commands.add(new ValidateCountRequestProcessor());
    // create an EntityManager
    commands.add(new EntityManagerCommand(emf));
    // parse generate the JPQL query
    commands.add(new GenerateJPQLCommand(true));
    // execute the JPQL query
    commands.add(new ExecuteCountQueryCommand());
    getCountCommand = createChain(CommandType.GetCount, commands);
  }

  private Command createChain(CommandType type, List<Command> commands) {
    if (producerBehavior != null) {
      return new Chain(producerBehavior.modify(type, commands));
    } else {
      return new Chain(commands);
    }
  }

  @Override
  public EdmDataServices getMetadata() {
    return metadata;
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    return this.metadataProducer;
  }

  @Override
  public EntitiesResponse getEntities(String entitySetName,
      QueryInfo queryInfo) {
    Context context = new Context(metadata, entitySetName, queryInfo);
    getEntitiesCommand.execute(context);
    return (EntitiesResponse) context.getResponse();
  }

  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey,
      QueryInfo queryInfo) {
    Context context = new Context(metadata, entitySetName, entityKey, null,
        queryInfo);
    getEntityCommand.execute(context);
    return (EntityResponse) context.getResponse();
  }

  @Override
  public BaseResponse getNavProperty(String entitySetName,
      OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    Context context = new Context(metadata, entitySetName, entityKey,
        navProp, queryInfo);
    getEntitiesCommand.execute(context);
    return context.getResponse();
  }

  @Override
  public void close() {}

  @Override
  public EntityResponse createEntity(String entitySetName, OEntity entity) {
    Context context = new Context(metadata, entitySetName, null, entity);
    try {
      createEntityCommand.execute(context);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return (EntityResponse) context.getResponse();
  }

  @Override
  public EntityResponse createEntity(String entitySetName,
      OEntityKey entityKey, String navProp, OEntity entity) {
    Context context = new Context(metadata, entitySetName, entityKey,
        navProp, entity);
    createAndLinkCommand.execute(context);
    return (EntityResponse) context.getResponse();
  }

  @Override
  public void deleteEntity(String entitySetName, OEntityKey entityKey) {
    Context context = new Context(metadata, entitySetName, entityKey, null);
    deleteEntityCommand.execute(context);
  }

  @Override
  public void mergeEntity(String entitySetName, OEntity entity) {
    Context context = new Context(metadata, entitySetName,
        entity.getEntityKey(), entity);
    mergeEntityCommand.execute(context);
  }

  @Override
  public void updateEntity(String entitySetName, OEntity entity) {
    Context context = new Context(metadata, entitySetName,
        entity.getEntityKey(), entity);
    updateEntityCommand.execute(context);
  }

  @Override
  public EntityIdResponse getLinks(OEntityId sourceEntity,
      String targetNavProp) {
    Context context = new Context(metadata,
        sourceEntity.getEntitySetName(),
        sourceEntity.getEntityKey(), targetNavProp, (QueryInfo) null);
    getLinksCommand.execute(context);

    BaseResponse r = context.getResponse();
    if (r instanceof EntitiesResponse) {
      EntitiesResponse er = (EntitiesResponse) r;
      return Responses.multipleIds(er.getEntities());
    }
    if (r instanceof EntityResponse) {
      EntityResponse er = (EntityResponse) r;
      return Responses.singleId(er.getEntity());
    }
    if (r instanceof EntitiesResponse) {
      EntitiesResponse er = (EntitiesResponse) r;
      return Responses.multipleIds(er.getEntities());
    }
    if (r instanceof EntityResponse) {
      EntityResponse er = (EntityResponse) r;
      return Responses.singleId(er.getEntity());
    }
    throw new NotImplementedException(sourceEntity + " " + targetNavProp);
  }

  @Override
  public void createLink(OEntityId sourceEntity, String targetNavProp,
      OEntityId targetEntity) {
    throw new NotImplementedException();
  }

  @Override
  public void updateLink(OEntityId sourceEntity, String targetNavProp,
      OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteLink(OEntityId sourceEntity, String targetNavProp,
      OEntityKey targetEntityKey) {
    throw new NotImplementedException();
  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport name,
      Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public CountResponse getEntitiesCount(String entitySetName,
      QueryInfo queryInfo) {
    Context context = new Context(metadata, entitySetName, queryInfo);
    getCountCommand.execute(context);
    return (CountResponse) context.getResponse();
  }

  @Override
  public CountResponse getNavPropertyCount(String entitySetName,
      OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    Context context = new Context(metadata, entitySetName, entityKey,
        navProp, queryInfo);
    getCountCommand.execute(context);
    return (CountResponse) context.getResponse();
  }

  public static class Context {

    private EdmDataServices metadata;
    private EntityManager em;
    private EntityTransaction tx;

    private ContextEntity entity;
    private ContextEntity otherEntity;

    private String navProperty;

    private QueryInfo queryInfo;

    private String jpqlQuery;
    private EdmPropertyBase edmPropertyBase;

    private BaseResponse response;
    
    // update, merge, delete
    protected Context(EdmDataServices metadata, String entitySetName,
        OEntityKey oEntityKey, OEntity oEntity) {
      this.metadata = metadata;
      this.entity = new ContextEntity(entitySetName, oEntityKey, oEntity);
    }

    // create
    public Context(EdmDataServices metadata, String entitySetName,
        OEntityKey oEntityKey, String navProperty, OEntity oEntity) {
      this.metadata = metadata;
      this.entity = new ContextEntity(entitySetName, oEntityKey, null);
      this.navProperty = navProperty;
      this.otherEntity = new ContextEntity(oEntity.getEntitySetName(), oEntity.getEntityKey(), oEntity);
    }

    // query
    public Context(EdmDataServices metadata, String entitySetName,
        QueryInfo queryInfo) {
      this.metadata = metadata;
      this.entity = new ContextEntity(entitySetName, null, null);
      this.queryInfo = queryInfo;
    }

    // get entity / with nav property (count?)
    public Context(EdmDataServices metadata, String entitySetName,
        OEntityKey oEntityKey, String navProperty, QueryInfo queryInfo) {
      this.metadata = metadata;
      this.entity = new ContextEntity(entitySetName, oEntityKey, null);
      this.navProperty = navProperty;
      this.queryInfo = queryInfo;
    }

    public EdmDataServices getMetadata() {
      return metadata;
    }

    public EntityManager getEntityManager() {
      return em;
    }

    public void setEntityManager(EntityManager em) {
      this.em = em;
    }

    public EntityTransaction getEntityTransaction() {
      return tx;
    }

    public void setEntityTransaction(EntityTransaction tx) {
      this.tx = tx;
    }
    
    public ContextEntity getEntity() {
      return entity;
    }

    public ContextEntity getOtherEntity() {
      return otherEntity;
    }
    
    public String getNavProperty() {
      return navProperty;
    }

    public QueryInfo getQueryInfo() {
      return queryInfo;
    }

    public String getJPQLQuery() {
      return jpqlQuery;
    }

    public void setJPQLQuery(String jpqlQuery) {
      this.jpqlQuery = jpqlQuery;
    }

    public EdmPropertyBase getEdmPropertyBase() {
      return edmPropertyBase;
    }

    public void setEdmPropertyBase(EdmPropertyBase edmPropertyBase) {
      this.edmPropertyBase = edmPropertyBase;
    }
    
    public BaseResponse getResponse() {
      return response;
    }

    public void setResponse(BaseResponse response) {
      this.response = response;
    }

    public class ContextEntity {
      private String entitySetName;
      private OEntityKey oEntityKey;
      private OEntity oEntity;

      private EdmEntitySet ees;
      private EntityType<?> jpaEntityType;
      private String keyAttributeName;
      private Object jpaEntity;

      public ContextEntity(String entitySetName, OEntityKey oEntityKey,
          OEntity oEntity) {
        this.entitySetName = entitySetName;
        this.oEntityKey = oEntityKey;
        this.oEntity = oEntity;
      }

      public String getEntitySetName() {
        return entitySetName;
      }

      public void setEntitySetName(String entitySetName) {
        this.entitySetName = entitySetName;
        this.jpaEntityType = null;
        this.ees = null;
        this.keyAttributeName = null;
        this.oEntityKey = null;
      }

      public void setOEntityKey(OEntityKey oEntityKey) {
        this.oEntityKey = oEntityKey;
      }

      public EntityType<?> getJPAEntityType() {
        if (jpaEntityType == null) {

          jpaEntityType = JPAProducer.getJPAEntityType(em,
              getEdmEntitySet()
                  .getType().getName());
        }
        return jpaEntityType;
      }

      public EdmEntitySet getEdmEntitySet() {
        if (ees == null) {
          ees = getMetadata().getEdmEntitySet(getEntitySetName());
        }
        return ees;
      }

      public String getKeyAttributeName() {
        if (keyAttributeName == null) {
          keyAttributeName = JPAEdmGenerator
            .getIdAttribute(getJPAEntityType()).getName();
        }
        return keyAttributeName;
      }

      public Object getTypeSafeEntityKey() {
        return typeSafeEntityKey(
              getEntityManager(),
              getJPAEntityType(),
              oEntityKey);
      }

      public Object getJpaEntity() {
        return jpaEntity;
      }

      public void setJpaEntity(Object jpaEntity) {
        this.jpaEntity = jpaEntity;
      }

      public OEntity getOEntity() {
        return oEntity;
      }

      public void setOEntity(OEntity oEntity) {
        this.oEntity = oEntity;
        setEntitySetName(oEntity.getEntitySetName());
        this.oEntityKey = oEntity != null ? oEntity.getEntityKey() : null;
      }
    }

    public static abstract class EntityAccessor {
      public abstract ContextEntity getEntity(Context context);

      public abstract void setJPAEntity(Context context, Object jpaEntity);

      public static final EntityAccessor ENTITY = new EntityAccessor() {

        @Override
        public ContextEntity getEntity(Context context) {
          return context.getEntity();
        }

        @Override
        public void setJPAEntity(Context context, Object jpaEntity) {
          context.getEntity().setJpaEntity(jpaEntity);
        }
      };

      public static final EntityAccessor OTHER = new EntityAccessor() {

        @Override
        public ContextEntity getEntity(Context context) {
          return context.getOtherEntity();
        }

        @Override
        public void setJPAEntity(Context context, Object jpaEntity) {
          context.getOtherEntity().setJpaEntity(jpaEntity);
        }
      };
    }
  }

  class Chain implements Command {

    List<Command> commands;

    Chain(List<Command> commands) {
      this.commands = Collections.unmodifiableList(commands);
    }

    /**
     * copied from http://commons.apache.org/chain. 
     */
    @Override
    public boolean execute(Context context) {
      boolean saveResult = false;
      RuntimeException saveException = null;
      int i = 0;
      int n = commands.size();
      for (i = 0; i < n; i++) {
        try {
          saveResult = commands.get(i).execute(context);
          if (saveResult) {
            break;
          }
        } catch (RuntimeException e) {
          saveException = e;
          break;
        }
      }

      // Call postprocess methods on Filters in reverse order
      if (i >= n) { // Fell off the end of the chain
        i--;
      }
      boolean handled = false;
      boolean result = false;
      for (int j = i; j >= 0; j--) {
        if (commands.get(j) instanceof Filter) {
          try {
            result =
                            ((Filter) commands.get(j)).postProcess(context,
                                                               saveException);
            if (result) {
              handled = true;
            }
          } catch (Exception e) {
            // Silently ignore
          }
        }
      }

      // Return the exception or result state from the last execute()
      if ((saveException != null) && !handled) {
        throw saveException;
      } else {
        return saveResult;
      }
    }
  }

  public class CreateAndLinkCommand implements Command {

    @Override
    public boolean execute(Context context) {
      // get the navigation property
      EdmNavigationProperty edmNavProperty = context.getEntity()
          .getEdmEntitySet().getType()
          .findNavigationProperty(context.getNavProperty());

      // check whether the navProperty is valid
      if (edmNavProperty == null
          || edmNavProperty.getToRole().getMultiplicity() != EdmMultiplicity.MANY) {
        throw new IllegalArgumentException(
            "unknown navigation property "
                + context.getNavProperty()
                + " or navigation property toRole Multiplicity is not '*'");
      }

      EntityType<?> newJpaEntityType = context.getOtherEntity()
          .getJPAEntityType();
      Object newJpaEntity = context.getOtherEntity().getJpaEntity();

      // get the collection attribute and add the new entity to the parent
      // entity
      final String navProperty = context.getNavProperty();
      @SuppressWarnings("unchecked")
      PluralAttribute<?, ?, ?> attr = Enumerable.create(
          context.getEntity().getJPAEntityType()
              .getPluralAttributes())
          .firstOrNull(new Predicate1() {
            public boolean apply(Object input) {
              PluralAttribute<?, ?, ?> pa = (PluralAttribute<?, ?, ?>) input;
              return pa.getName().equals(navProperty);
            }
          });
      JPAMember member = JPAMember.create(attr, context.getEntity()
          .getJpaEntity());
      Collection<Object> collection = member.get();
      collection.add(newJpaEntity);

      // TODO handle ManyToMany relationships
      // set the backreference in bidirectional relationships
      OneToMany oneToMany = member.getAnnotation(OneToMany.class);
      if (oneToMany != null
          && oneToMany.mappedBy() != null
          && !oneToMany.mappedBy().isEmpty()) {
        JPAMember.create(
            newJpaEntityType.getAttribute(oneToMany.mappedBy()),
            newJpaEntity)
            .set(context.getEntity().getJpaEntity());
      }

      // check whether the EntityManager will persist the
      // new entity or should we do it
      if (oneToMany != null
          && oneToMany.cascade() != null) {
        List<CascadeType> cascadeTypes = Arrays.asList(oneToMany
            .cascade());
        if (!cascadeTypes.contains(CascadeType.ALL)
            && !cascadeTypes.contains(CascadeType.PERSIST)) {
          context.getEntityManager().persist(newJpaEntity);
        }
      }

      return false;
    }
  }

  public class DeleteEntityCommand implements Command {

    @Override
    public boolean execute(Context context) {
      context.getEntityManager().remove(context.getEntity().getJpaEntity());

      return false;
    }
  }

  public class EntityManagerCommand implements Filter {

    private final EntityManagerFactory emf;

    public EntityManagerCommand(EntityManagerFactory emf) {
      this.emf = emf;
    }

    @Override
    public boolean execute(Context context) {
      EntityManager em = this.emf.createEntityManager();
      context.setEntityManager(em);

      return false;
    }

    @Override
    public boolean postProcess(Context context, Exception exception) {
      context.getEntityManager().close();
      context.setEntityManager(null);

      return false;
    }
  }

  public class ExecuteCountQueryCommand implements Command {

    @Override
    public boolean execute(Context context) {
      // get the jpql
      String jpql = context.getJPQLQuery();

      // jpql -> jpa query
      Query tq = context.getEntityManager().createQuery(jpql);

      // execute jpa query
      Long count = (Long) tq.getSingleResult();

      QueryInfo query = context.getQueryInfo();
      // apply $skip.
      // example: http://odata.netflix.com/Catalog/Titles/$count?$skip=100
      if (query != null && query.skip != null)
        count = Math.max(0, count - query.skip);

      // apply $top.
      // example: http://odata.netflix.com/Catalog/Titles/$count?$top=10
      if (query != null && query.top != null)
        count = Math.min(count, query.top);

      context.setResponse(Responses.count(count));

      return false;
    }
  }

  public class ExecuteJPQLQueryCommand implements Command {

    private int maxResults;

    public ExecuteJPQLQueryCommand(int maxResults) {
      this.maxResults = maxResults;
    }

    @Override
    public boolean execute(Context context) {
      context.setResponse(getEntitiesResponse(context));

      return false;
    }

    private BaseResponse getEntitiesResponse(final Context context) {

      // get the jpql
      String jpql = context.getJPQLQuery();

      // jpql -> jpa query
      Query tq = context.getEntityManager().createQuery(jpql);

      Integer inlineCount = context.getQueryInfo() != null
          && context.getQueryInfo().inlineCount == InlineCount.ALLPAGES
          ? tq.getResultList().size()
          : null;

      int queryMaxResults = maxResults;
      if (context.getQueryInfo() != null
          && context.getQueryInfo().top != null) {

        // top=0: don't even hit jpa, return a response with zero
        // entities
        if (context.getQueryInfo().top.equals(0)) {
          // returning null from this function would cause the
          // FormatWriters to throw
          // a null reference exception as the entities collection is
          // expected to be empty and
          // not null. This prevents us from being able to
          // successfully
          // respond to $top=0 contexts.
          List<OEntity> emptyList = Collections.emptyList();
          return Responses.entities(
              emptyList,
              context.getEntity().getEdmEntitySet(),
              inlineCount,
              null);
        }

        if (context.getQueryInfo().top < maxResults)
          queryMaxResults = context.getQueryInfo().top;
      }

      // jpa query for one more than specified to determine whether or not
      // to
      // return a skip token
      tq = tq.setMaxResults(queryMaxResults + 1);

      if (context.getQueryInfo() != null
          && context.getQueryInfo().skip != null)
        tq = tq.setFirstResult(context.getQueryInfo().skip);

      // execute jpa query
      @SuppressWarnings("unchecked")
      List<Object> results = tq.getResultList();

      // property response
      if (context.getEdmPropertyBase() instanceof EdmProperty) {
        EdmProperty propInfo = (EdmProperty) context
            .getEdmPropertyBase();

        if (results.size() != 1)
          throw new RuntimeException(
              "Expected one and only one result for property, found "
                  + results.size());

        Object value = results.get(0);
        OProperty<?> op = OProperties.simple(
            ((EdmProperty) propInfo).getName(),
            (EdmSimpleType<?>) ((EdmProperty) propInfo).getType(),
            value);
        return Responses.property(op);
      }

      // entities response
      List<OEntity> entities = Enumerable.create(results)
          .take(queryMaxResults)
          .select(new Func1<Object, OEntity>() {
            public OEntity apply(final Object jpaEntity) {
              return makeEntity(context, jpaEntity);
            }
          }).toList();

      // compute skip token if necessary
      String skipToken = null;
      boolean hasMoreResults = context.getQueryInfo() != null
          && context.getQueryInfo().top != null
          ? context.getQueryInfo().top > maxResults
              && results.size() > queryMaxResults
          : results.size() > queryMaxResults;

      if (hasMoreResults)
        skipToken = JPASkipToken.create(context.getQueryInfo() == null
            ? null
            : context.getQueryInfo().orderBy,
            Enumerable.create(entities)
                .last());

      if (context.getEdmPropertyBase() instanceof EdmNavigationProperty) {
        EdmNavigationProperty edmNavProp = (EdmNavigationProperty) context
            .getEdmPropertyBase();
        if (edmNavProp.getToRole().getMultiplicity() == EdmMultiplicity.ONE
            || edmNavProp.getToRole().getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE) {
          if (entities.size() != 1)
            throw new RuntimeException(
                "Expected only one entity, found "
                    + entities.size());
          return Responses.entity(entities.get(0));
        }
      }

      return Responses.entities(entities, context.getEntity()
          .getEdmEntitySet(),
          inlineCount, skipToken);
    }

    private OEntity makeEntity(Context context, Object jpaEntity) {

      return jpaEntityToOEntity(
          context.getMetadata(),
          context.getEntity().getEdmEntitySet(),
          context.getEntity().getJPAEntityType(),
          jpaEntity,
          context.getQueryInfo() == null ? null : context
              .getQueryInfo().expand,
          context.getQueryInfo() == null ? null : context
              .getQueryInfo().select);
    }
  }

  public class GenerateJPQLCommand implements Command {

    private boolean isCount;

    public GenerateJPQLCommand() {
      this(false);
    }

    public GenerateJPQLCommand(boolean isCount) {
      this.isCount = isCount;
    }

    @Override
    public boolean execute(Context context) {
      context.setJPQLQuery(generateJPQL(context));

      return false;
    }

    private String generateJPQL(Context context) {
      String alias = "t0";
      String from = context.getEntity().getJPAEntityType().getName()
          + " " + alias;
      String where = null;

      if (context.getNavProperty() != null) {
        where = whereKeyEquals(context.getEntity().getJPAEntityType(),
            context.getEntity().getKeyAttributeName(),
            context.getEntity().getTypeSafeEntityKey(), alias);

        String prop = null;
        int propCount = 0;

        for (String pn : context.getNavProperty().split("/")) {
          String[] propSplit = pn.split("\\(");
          prop = propSplit[0];
          propCount++;

          if (context.getEdmPropertyBase() instanceof EdmProperty) {
            throw new UnsupportedOperationException(
                String.format(
                    "The request URI is not valid. Since the segment '%s' "
                        + "refers to a collection, this must be the last segment "
                        + "in the request URI. All intermediate segments must refer "
                        + "to a single resource.",
                    alias));
          }

          context.setEdmPropertyBase(context.getMetadata()
              .findEdmProperty(prop));

          if (context.getEdmPropertyBase() instanceof EdmNavigationProperty) {
            EdmNavigationProperty propInfo = (EdmNavigationProperty) context
                .getEdmPropertyBase();

            context.getEntity().setEntitySetName(
                propInfo.getToRole().getType().getName());

            prop = alias + "." + prop;
            alias = "t" + Integer.toString(propCount);
            from = String
                .format("%s JOIN %s %s", from, prop, alias);

            if (propSplit.length > 1) {
              OEntityKey entityKey = OEntityKey.parse("("
                  + propSplit[1]);
              context.getEntity().setOEntityKey(entityKey);

              where = whereKeyEquals(context.getEntity()
                  .getJPAEntityType(),
                  context.getEntity().getKeyAttributeName(),
                  context.getEntity().getTypeSafeEntityKey(),
                  alias);
            }
          } else if (context.getEdmPropertyBase() instanceof EdmProperty) {
            EdmProperty propInfo = (EdmProperty) context
                .getEdmPropertyBase();

            alias = alias + "." + propInfo.getName();
            // TODO?
          }

          if (context.getEdmPropertyBase() == null) {
            throw new EntityNotFoundException(
                String.format(
                    "Resource not found for the segment '%s'.",
                    pn));
          }
        }
      }

      String select = isCount ? "COUNT(" + alias + ")" : alias;

      String jpql = String.format("SELECT %s FROM %s", select, from);

      JPQLGenerator jpqlGen = new JPQLGenerator(context.getEntity()
          .getKeyAttributeName(), alias);

      if (context.getQueryInfo() != null
          && context.getQueryInfo().filter != null) {
        String filterPredicate = jpqlGen
            .toJpql(context.getQueryInfo().filter);
        where = addWhereExpression(where, filterPredicate, "AND");
      }

      if (context.getQueryInfo() != null
          && context.getQueryInfo().skipToken != null) {
        BoolCommonExpression skipTokenPredicateExpr = JPASkipToken
            .parse(jpqlGen.getPrimaryKeyName(),
                context.getQueryInfo().orderBy,
                context.getQueryInfo().skipToken);
        String skipTokenPredicate = jpqlGen
            .toJpql(skipTokenPredicateExpr);
        where = addWhereExpression(where, skipTokenPredicate, "AND");
      }

      if (where != null)
        jpql = String.format("%s WHERE %s", jpql, where);

      if (!isCount && context.getQueryInfo() != null
          && context.getQueryInfo().orderBy != null
          && !context.getQueryInfo().orderBy.isEmpty()) {
        List<String> orderBys = new ArrayList<String>();
        for (OrderByExpression orderBy : context.getQueryInfo().orderBy) {
          String field = jpqlGen.toJpql(orderBy.getExpression());
          orderBys.add(field
              + (orderBy.getDirection() == Direction.ASCENDING
                  ? ""
                  : " DESC"));
        }
        jpql = jpql + " ORDER BY "
            + Enumerable.create(orderBys).join(",");
      }

      return jpql;
    }

    private String addWhereExpression(String expression,
        String nextExpression, String condition) {

      return expression == null
          ? nextExpression
          : String.format(
              "%s %s %s",
              expression,
              condition,
              nextExpression);
    }

    private String whereKeyEquals(EntityType<?> jpsEntityType,
        String keyAttributeName, Object typeSafeEntityKey, String alias) {
      SingularAttribute<?, ?> idAtt = jpsEntityType
          .getSingularAttribute(keyAttributeName);
      if (idAtt.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
        List<String> predicates = new ArrayList<String>();
        EmbeddableType<?> et = (EmbeddableType<?>) idAtt.getType();
        for (Attribute<?, ?> subAtt : et.getAttributes()) {
          Object subAttValue = JPAMember
              .create(subAtt, typeSafeEntityKey).get();
          String jpqlLiteral = JPQLGenerator
              .toJpqlLiteral(subAttValue);
          String predicate = String.format(
              "(%s.%s.%s = %s)",
              alias,
              keyAttributeName,
              subAtt.getName(),
              jpqlLiteral);
          predicates.add(predicate);
        }

        return "(" + Enumerable.create(predicates).join(" AND ") + ")";
      }

      String jpqlLiteral = JPQLGenerator.toJpqlLiteral(typeSafeEntityKey);
      return String.format(
          "(%s.%s = %s)",
          alias,
          keyAttributeName,
          jpqlLiteral);
    }
  }

  public class GetEntityCommand implements Command {

    private EntityAccessor accessor;

    public GetEntityCommand() {
      this(Context.EntityAccessor.ENTITY);
    }

    public GetEntityCommand(EntityAccessor accessor) {
      this.accessor = accessor;
    }

    @Override
    public boolean execute(Context context) {

      EntityType<?> jpaEntityType = accessor.getEntity(context)
          .getJPAEntityType();
      Object typeSafeEntityKey = accessor.getEntity(context)
          .getTypeSafeEntityKey();
      Object jpaEntity = context.getEntityManager().find(
          jpaEntityType.getJavaType(), typeSafeEntityKey);

      if (jpaEntity == null) {
        throw new NotFoundException(jpaEntityType
            .getJavaType()
            + " not found with key "
            + typeSafeEntityKey);
      }

      accessor.getEntity(context).setJpaEntity(jpaEntity);

      return false;
    }
  }

  public class JPAEntityToOEntityCommand implements Command {

    private Context.EntityAccessor accessor;

    public JPAEntityToOEntityCommand() {
      this(Context.EntityAccessor.ENTITY);
    }

    public JPAEntityToOEntityCommand(Context.EntityAccessor accessor) {
      this.accessor = accessor;
    }

    @Override
    public boolean execute(Context context) {

      OEntity oentity = jpaEntityToOEntity(
          context.getMetadata(),
          accessor.getEntity(context).getEdmEntitySet(),
          accessor.getEntity(context).getJPAEntityType(),
          accessor.getEntity(context).getJpaEntity(),
          context.getQueryInfo() == null
              ? null
              : context.getQueryInfo().expand,
          context.getQueryInfo() == null
              ? null
              : context.getQueryInfo().select);

      accessor.getEntity(context).setOEntity(oentity);

      return false;
    }
  }

  public class MergeEntityCommand implements Command {

    @Override
    public boolean execute(Context context) {
      EntityManager em = context.getEntityManager();
      EntityType<?> jpaEntityType = context.getEntity()
          .getJPAEntityType();
      Object jpaEntity = context.getEntity().getJpaEntity();
      OEntity entity = context.getEntity().getOEntity();

      applyOProperties(em, jpaEntityType,
          entity.getProperties(),
          jpaEntity);
      applyOLinks(em, jpaEntityType, entity.getLinks(),
          jpaEntity);

      return false;
    }
  }

  public class OEntityToJPAEntityCommand implements Command {

    private boolean withLinks;
    private Context.EntityAccessor accessor;

    public OEntityToJPAEntityCommand(boolean withLinks) {
      this(Context.EntityAccessor.ENTITY, withLinks);
    }

    public OEntityToJPAEntityCommand(Context.EntityAccessor accessor,
        boolean withLinks) {
      this.accessor = accessor;
      this.withLinks = withLinks;
    }

    @Override
    public boolean execute(Context context) {

      Object jpaEntity = createNewJPAEntity(
          context.getEntityManager(),
            accessor.getEntity(context).getJPAEntityType(),
            accessor.getEntity(context).getOEntity(),
            withLinks);
      accessor.getEntity(context).setJpaEntity(jpaEntity);

      return false;
    }
  }

  public class PersistJPAEntityCommand implements Command {

    @Override
    public boolean execute(Context context) {
      context.getEntityManager().persist(
          context.getEntity().getJpaEntity());

      return false;
    }
  }

  public class ReReadJPAEntityCommand implements Command {

    @Override
    public boolean execute(Context context) {

      // reread the entity in case we had links. This should insure
      // we get the implicitly set foreign keys. E.g in the Northwind model
      // creating a new Product with a link to the Category should return
      // the CategoryID.
      EntityManager em = context.getEntityManager();
      if (context.getEntity().getOEntity().getLinks() != null
                && !context.getEntity().getOEntity().getLinks().isEmpty()) {

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
          context.getEntityManager().refresh(
                    context.getEntity().getJpaEntity());
          tx.commit();
        } finally {
          if (tx.isActive()) {
            tx.rollback();
          }
        }
      }

      return false;
    }
  }

  public class SetOEntityResponseCommand implements Command {

    private Context.EntityAccessor accessor;

    public SetOEntityResponseCommand() {
      this(Context.EntityAccessor.ENTITY);
    }

    public SetOEntityResponseCommand(Context.EntityAccessor accessor) {
      this.accessor = accessor;
    }

    @Override
    public boolean execute(Context context) {

      OEntity oentity = accessor.getEntity(context).getOEntity();
      context.setResponse(Responses.entity(oentity));

      return false;
    }
  }

  public class BeginTransactionCommand implements Filter {

    @Override
    public boolean execute(Context context) {
      EntityManager em = context.getEntityManager();
      EntityTransaction tx = em.getTransaction();
      tx.begin();
      context.setEntityTransaction(tx);

      return false;
    }

    public boolean postProcess(Context context, Exception ex) {
      EntityTransaction tx = context.getEntityTransaction();
      if (tx != null) {
        if (tx.isActive()) {
          tx.rollback();
        } else {
          context.setEntityTransaction(null);
        }
      }
      return false;
    }
  }

  public class CommitTransactionCommand implements Command {

    @Override
    public boolean execute(Context context) {
      EntityManager em = context.getEntityManager();
      EntityTransaction tx = em.getTransaction();
      tx.commit();
      context.setEntityTransaction(null);

      return false;
    }
  }

  public class UpdateEntityCommand implements Command {

    @Override
    public boolean execute(Context context) {
      EntityManager em = context.getEntityManager();
      EntityType<?> jpaEntityType = context.getEntity()
          .getJPAEntityType();
      Object jpaEntity = context.getEntity().getJpaEntity();
      OEntity entity = context.getEntity().getOEntity();

      em.merge(jpaEntity);
      applyOLinks(em, jpaEntityType, entity.getLinks(),
          jpaEntity);

      return false;
    }
  }

  public class ValidateCountRequestProcessor implements Command {

    @Override
    public boolean execute(Context context) {
      // inlineCount is not applicable to $count queries
      QueryInfo query = context.getQueryInfo();
      if (query != null && query.inlineCount == InlineCount.ALLPAGES) {
        throw new UnsupportedOperationException(
            "$inlinecount cannot be applied to the resource segment '$count'");
      }

      // sktiptoken is not applicable to $count queries
      if (query != null && query.skipToken != null) {
        throw new UnsupportedOperationException(
            "Skip tokens can only be provided for contexts that return collections of entities.");
      }

      return false;
    }
  }

  /**** utility functions ***/

  static void applyOProperties(EntityManager em,
      ManagedType<?> jpaManagedType, Collection<OProperty<?>> properties,
      Object jpaEntity) {

    for (OProperty<?> prop : properties) {
      boolean found = false;
      if (jpaManagedType instanceof EntityType) {
        EntityType<?> jpaEntityType = (EntityType<?>) jpaManagedType;
        if (jpaEntityType.getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE) {
          EmbeddableType<?> et = (EmbeddableType<?>) jpaEntityType
              .getIdType();

          for (Attribute<?, ?> idAtt : et.getAttributes()) {

            if (idAtt.getName().equals(prop.getName())) {

              Object idValue = JPAMember.create(
                  jpaEntityType.getId(et.getJavaType()),
                  jpaEntity).get();

              setAttribute(idAtt, prop, idValue);
              found = true;
              break;
            }
          }
        }
      }
      if (found)
        continue;
      Attribute<?, ?> att = jpaManagedType.getAttribute(prop.getName());
      setAttribute(att, prop, jpaEntity);
    }
  }

  static Object coercePropertyValue(OProperty<?> prop, Class<?> javaType) {
    Object value = prop.getValue();
    try {
      return TypeConverter.convert(value, javaType);
    } catch (UnsupportedOperationException ex) {
      // let java complain
      return value;
    }
  }

  static EntityType<?> getJPAEntityType(EntityManager em,
      String jpaEntityTypeName) {

    for (EntityType<?> et : em.getMetamodel().getEntities()) {
      if (JPAEdmGenerator.getEntitySetName(et).equals(jpaEntityTypeName)) {
        return et;
      }
    }

    throw new RuntimeException("JPA Entity type " + jpaEntityTypeName
        + " not found");
  }

  @SuppressWarnings("unchecked")
  static <T> T newInstance(Class<?> javaType) {
    try {
      if (javaType.equals(Collection.class))
        javaType = HashSet.class;
      Constructor<?> ctor = javaType.getDeclaredConstructor();
      ctor.setAccessible(true);
      return (T) ctor.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static void setAttribute(Attribute<?, ?> att, OProperty<?> prop,
      Object target) {
    JPAMember attMember = JPAMember.create(att, target);
    Object value = coercePropertyValue(prop, attMember.getJavaType());
    attMember.set(value);
  }

  static Object typeSafeEntityKey(
      EntityManager em,
      EntityType<?> jpaEntityType,
      OEntityKey entityKey) {

    if (entityKey != null
        && jpaEntityType.getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE) {
      Object id = newInstance(jpaEntityType.getIdType().getJavaType());
      applyOProperties(
          em,
          em.getMetamodel().embeddable(
              jpaEntityType.getIdType().getJavaType()),
          entityKey.asComplexProperties(), id);
      return id;
    }

    Class<?> javaType = jpaEntityType.getIdType().getJavaType();

    return TypeConverter.convert(
        entityKey == null ? null : entityKey.asSingleValue(), javaType);
  }

  @SuppressWarnings("unchecked")
  static void applyOLinks(EntityManager em, EntityType<?> jpaEntityType,
      List<OLink> links, Object jpaEntity) {
    if (links == null)
      return;

    for (final OLink link : links) {
      String[] propNameSplit = link.getRelation().split("/");
      String propName = propNameSplit[propNameSplit.length - 1];

      if (link instanceof ORelatedEntitiesLinkInline) {
        PluralAttribute<?, ?, ?> att = (PluralAttribute<?, ?, ?>) jpaEntityType
            .getAttribute(propName);
        JPAMember member = JPAMember.create(att, jpaEntity);

        EntityType<?> collJpaEntityType = (EntityType<?>) att
            .getElementType();

        OneToMany oneToMany = member.getAnnotation(OneToMany.class);
        boolean hasSingularBackRef = oneToMany != null
            && oneToMany.mappedBy() != null
            && !oneToMany.mappedBy().isEmpty();
        boolean cascade = oneToMany != null && oneToMany.cascade() != null
            ? Enumerable.create(oneToMany.cascade()).any(new Predicate1<CascadeType>() {
              @Override
              public boolean apply(CascadeType input) {
                return input == CascadeType.ALL || input == CascadeType.PERSIST;
              }
            })
            : false;

        ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);

        Collection<Object> coll = member.get();
        if (coll == null) {
          coll = (Collection<Object>) newInstance(member
              .getJavaType());
          member.set(coll);
        }
        for (OEntity oentity : ((ORelatedEntitiesLinkInline) link)
            .getRelatedEntities()) {
          Object collJpaEntity = createNewJPAEntity(em,
              collJpaEntityType, oentity, true);
          if (hasSingularBackRef) {
            JPAMember backRef = JPAMember.create(collJpaEntityType
                .getAttribute(oneToMany.mappedBy()),
                collJpaEntity);
            backRef.set(jpaEntity);
          }
          if (manyToMany != null) {
            Attribute<?, ?> other = null;
            if (manyToMany.mappedBy() != null
                && !manyToMany.mappedBy().isEmpty())
              other = collJpaEntityType.getAttribute(manyToMany
                  .mappedBy());
            else {
              for (Attribute<?, ?> att2 : collJpaEntityType
                  .getAttributes()) {
                if (att2.isCollection()
                    && JPAMember
                        .create(att2, null)
                        .getAnnotation(ManyToMany.class) != null) {
                  CollectionAttribute<?, ?> ca = (CollectionAttribute<?, ?>) att2;
                  if (ca.getElementType().equals(
                      jpaEntityType)) {
                    other = ca;
                    break;
                  }
                }
              }
            }

            if (other == null)
              throw new RuntimeException(
                  "Could not find other side of many-to-many relationship");

            JPAMember backRef = JPAMember.create(other,
                collJpaEntity);
            Collection<Object> coll2 = backRef.get();
            if (coll2 == null) {
              coll2 = newInstance(backRef.getJavaType());
              backRef.set(coll2);
            }
            coll2.add(jpaEntity);
          }

          if (!cascade) {
            em.persist(collJpaEntity);
          }
          coll.add(collJpaEntity);
        }

      } else if (link instanceof ORelatedEntityLinkInline) {
        SingularAttribute<?, ?> att = jpaEntityType
            .getSingularAttribute(propName);
        JPAMember member = JPAMember.create(att, jpaEntity);

        OneToOne oneToOne = member.getAnnotation(OneToOne.class);
        boolean cascade = oneToOne != null && oneToOne.cascade() != null
            ? Enumerable.create(oneToOne.cascade()).any(new Predicate1<CascadeType>() {
              @Override
              public boolean apply(CascadeType input) {
                return input == CascadeType.ALL || input == CascadeType.PERSIST;
              }
            })
            : false;

        EntityType<?> relJpaEntityType = (EntityType<?>) att.getType();
        Object relJpaEntity = createNewJPAEntity(em, relJpaEntityType,
            ((ORelatedEntityLinkInline) link).getRelatedEntity(),
            true);
        
        if (!cascade) {
          em.persist(relJpaEntity);
        }

        member.set(relJpaEntity);
      } else if (link instanceof ORelatedEntityLink) {

        // look up the linked entity, and set the member value
        SingularAttribute<?, ?> att = jpaEntityType
            .getSingularAttribute(propName);
        JPAMember member = JPAMember.create(att, jpaEntity);

        EntityType<?> relJpaEntityType = (EntityType<?>) att.getType();
        Object key = typeSafeEntityKey(
            em,
            relJpaEntityType,
            OEntityKey.parse(link.getHref().substring(
                link.getHref().indexOf('('))));
        Object relEntity = em.find(relJpaEntityType.getJavaType(), key);

        member.set(relEntity);

        // set corresponding property (if there is one)
        JoinColumn joinColumn = member.getAnnotation(JoinColumn.class);
        ManyToOne manyToOne = member.getAnnotation(ManyToOne.class);
        if (joinColumn != null && manyToOne != null) {
          String columnName = joinColumn.name();
          JPAMember m = JPAMember.findByColumn(jpaEntityType,
              columnName, jpaEntity);
          if (m != null)
            m.set(key);

        }

      } else {
        throw new UnsupportedOperationException(
            "binding the new entity to many entities is not supported");
      }
    }
  }

  static Object createNewJPAEntity(
      EntityManager em,
      EntityType<?> jpaEntityType,
      OEntity oEntity,
      boolean withLinks) {

    Object jpaEntity = newInstance(jpaEntityType.getJavaType());

    if (jpaEntityType.getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE) {
      EmbeddableType<?> et = (EmbeddableType<?>) jpaEntityType
          .getIdType();

      JPAMember idMember = JPAMember.create(
          jpaEntityType.getId(et.getJavaType()), jpaEntity);
      Object idValue = newInstance(et.getJavaType());
      idMember.set(idValue);
    }

    applyOProperties(em, jpaEntityType, oEntity.getProperties(), jpaEntity);
    if (withLinks)
      applyOLinks(em, jpaEntityType, oEntity.getLinks(), jpaEntity);

    return jpaEntity;
  }

  static Object getIdValue(
        Object jpaEntity,
        SingularAttribute<?, ?> idAtt,
        String propName) {
    try {
      // get the composite id
      Object keyValue = JPAMember.create(idAtt, jpaEntity).get();

      if (propName == null)
          return keyValue;

      // get the property from the key
      ManagedType<?> keyType = (ManagedType<?>) idAtt.getType();
      Attribute<?, ?> att = keyType.getAttribute(propName);
      return JPAMember.create(att, keyValue).get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static boolean isSelected(String name, List<EntitySimpleProperty> select) {

    if (select != null && !select.isEmpty()) {
      for (EntitySimpleProperty prop : select) {
        String sname = prop.getPropertyName();
        if (name.equals(sname)) {
          return true;
        }
      }

      return false;
    }

    return true;
  }

  static OEntity jpaEntityToOEntity(
      EdmDataServices metadata,
        EdmEntitySet ees,
        EntityType<?> entityType,
        Object jpaEntity,
        List<EntitySimpleProperty> expand,
        List<EntitySimpleProperty> select) {

    List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
    List<OLink> links = new ArrayList<OLink>();

    try {
      SingularAttribute<?, ?> idAtt = JPAEdmGenerator.getIdAttribute(entityType);
      boolean hasEmbeddedCompositeKey =
            idAtt.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED;

      // get properties
      for (EdmProperty ep : ees.getType().getProperties()) {

        if (!isSelected(ep.getName(), select)) {
          continue;
        }

        // we have a embedded composite key and we want a property from
        // that key
        if (hasEmbeddedCompositeKey && ees.getType().getKeys().contains(ep.getName())) {
          Object value = getIdValue(jpaEntity, idAtt, ep.getName());

          properties.add(OProperties.simple(
                ep.getName(),
                (EdmSimpleType<?>) ep.getType(),
                value));

        } else {
          // get the simple attribute
          Attribute<?, ?> att = entityType.getAttribute(ep.getName());
          JPAMember member = JPAMember.create(att, jpaEntity);
          Object value = member.get();

          if (ep.getType().isSimple()) {
            properties.add(OProperties.simple(
                  ep.getName(),
                  (EdmSimpleType<?>) ep.getType(),
                  value));
          } else {
            // TODO handle embedded entities
          }
        }
      }

      // get the collections if necessary
      if (expand != null && !expand.isEmpty()) {

        HashMap<String, List<EntitySimpleProperty>> expandedProps = new HashMap<String, List<EntitySimpleProperty>>();

        //process all the expanded properties and add them to map
        for (final EntitySimpleProperty propPath : expand) {
          // split the property path into the first and remaining
          // parts
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

          Attribute<?, ?> att = entityType.getAttribute(prop);
          if (att.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_MANY
                || att.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_MANY) {

            Collection<?> value = JPAMember.create(att, jpaEntity).get();

            List<OEntity> relatedEntities = new ArrayList<OEntity>();
            for (Object relatedEntity : value) {
              EntityType<?> elementEntityType = (EntityType<?>) ((PluralAttribute<?, ?, ?>) att)
                    .getElementType();
              EdmEntitySet elementEntitySet = metadata
                    .getEdmEntitySet(JPAEdmGenerator.getEntitySetName(elementEntityType));

              relatedEntities.add(jpaEntityToOEntity(
                  metadata,
                    elementEntitySet,
                    elementEntityType,
                    relatedEntity,
                    remainingPropPath,
                    null));
            }

            links.add(OLinks.relatedEntitiesInline(
                  null,
                  prop,
                  null,
                  relatedEntities));

          } else if (att.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE
                || att.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE) {
            EntityType<?> relatedEntityType =
                  (EntityType<?>) ((SingularAttribute<?, ?>) att)
                      .getType();

            EdmEntitySet relatedEntitySet =
                  metadata.getEdmEntitySet(JPAEdmGenerator
                      .getEntitySetName(relatedEntityType));

            Object relatedEntity = JPAMember.create(att, jpaEntity).get();

            if (relatedEntity == null) {
              links.add(OLinks.relatedEntityInline(
                    null,
                    prop,
                    null,
                    null));

            } else {
              links.add(OLinks.relatedEntityInline(
                    null,
                    prop,
                    null,
                    jpaEntityToOEntity(
                        metadata,
                        relatedEntitySet,
                        relatedEntityType,
                        relatedEntity,
                        remainingPropPath,
                        null)));
            }

          }

        }
      }

      // for every navigation propety that we didn' expand we must place an deferred
      // OLink if the nav prop is selected
      for (final EdmNavigationProperty ep : ees.getType().getNavigationProperties()) {
        if (isSelected(ep.getName(), select)) {
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
      }

      return OEntities.create(ees, toOEntityKey(jpaEntity, idAtt), properties, links);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static OEntityKey toOEntityKey(Object jpaEntity, SingularAttribute<?, ?> idAtt) {
    boolean hasEmbeddedCompositeKey =
          idAtt.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED;
    if (!hasEmbeddedCompositeKey) {
      Object id = getIdValue(jpaEntity, idAtt, null);
      return OEntityKey.create(id);
    }
    ManagedType<?> keyType = (ManagedType<?>) idAtt.getType();

    Map<String, Object> nameValues = new HashMap<String, Object>();
    for (Attribute<?, ?> att : keyType.getAttributes())
      nameValues.put(att.getName(), getIdValue(jpaEntity, idAtt, att.getName()));
    return OEntityKey.create(nameValues);
  }

}
