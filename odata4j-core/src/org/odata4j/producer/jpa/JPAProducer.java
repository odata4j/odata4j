package org.odata4j.producer.jpa;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import org.core4j.CoreUtils;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.joda.time.LocalDateTime;
import org.odata4j.core.AtomInfo;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.NavPropertyResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.inmemory.ListUtils;
import org.odata4j.producer.Responses;
import org.odata4j.producer.resources.BaseResource;
import org.odata4j.producer.resources.OptionsQueryParser;

public class JPAProducer implements ODataProducer {

    private final static Client httpClient = Client.create(new DefaultClientConfig());
    private final EntityManagerFactory emf;
    private final EntityManager em;
    private final String namespace;
    private final EdmDataServices metadata;
    private final int maxResults;

    public JPAProducer(EntityManagerFactory emf, String namespace, int maxResults) {

        this.emf = emf;
        this.namespace = namespace;
        this.maxResults = maxResults;

        em = emf.createEntityManager(); // necessary for metamodel
        this.metadata = JPAEdmGenerator.buildEdm(emf, this.namespace);
    }

    @Override
    public void close() {
        em.close();
        emf.close();
    }

    @Override
    public EdmDataServices getMetadata() {
        return metadata;
    }

    @Override
    public EntityResponse getEntity(String entitySetName, Object entityKey) {

        return common(entitySetName, entityKey, null, new Func1<Context, EntityResponse>() {

            public EntityResponse apply(Context input) {
                return getEntity(input);
            }
        });

    }

    @Override
    public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {

        return common(entitySetName, null, queryInfo, new Func1<Context, EntitiesResponse>() {

            public EntitiesResponse apply(Context input) {
                return getEntities(input);
            }
        });

    }

    @Override
    public NavPropertyResponse getNavProperty(
            final String entitySetName,
            final Object entityKey,
            final String navProp,
            final QueryInfo queryInfo) {

        return common(
                entitySetName,
                entityKey,
                queryInfo,
                new Func1<Context, NavPropertyResponse>() {

                    @Override
                    public NavPropertyResponse apply(Context input) {
                        return getNavProperty(input, navProp);
                    }
                });
    }

    private class Context {

        EdmEntitySet ees;
        EntityType<?> jpaEntityType;
        String keyPropertyName;
        EntityManager em;
        Object entityKey;
        QueryInfo query;
    }

    private EntityResponse getEntity(final Context context) {

        Object jpaEntity = context.em.find(context.jpaEntityType.getJavaType(), context.entityKey);
        if (jpaEntity == null) {
            throw new EntityNotFoundException(context.jpaEntityType.getJavaType() + " not found with key " + context.entityKey);
        }

        final OEntity entity = makeEntity(context, jpaEntity);
        return new EntityResponse() {

            @Override
            public OEntity getEntity() {
                return entity;
            }

            @Override
            public EdmEntitySet getEntitySet() {
                return context.ees;
            }
        };

    }

    private OEntity makeEntity(Context context, final Object jpaEntity) {
        return makeEntity(context.ees, context.jpaEntityType, context.keyPropertyName, jpaEntity);
    }

    private OEntity makeEntity(final EdmEntitySet ees, final EntityType<?> jpaEntityType, final String keyPropertyName, final Object jpaEntity) {
        return OEntities.create(jpaEntityToOProperties(ees, jpaEntityType, jpaEntity), null);
    }

    private EntitiesResponse getEntities(final Context context) {

        final DynamicEntitiesResponse response = enumJpaEntities(context.em, context.jpaEntityType.getJavaType(), context.query, maxResults);
        final List<OEntity> entities = response.jpaEntities.select(new Func1<Object, OEntity>() {

            public OEntity apply(final Object input) {
                return makeEntity(context, input);
            }
        }).toList();

        String skipToken = null;
        if (response.useSkipToken) {
            skipToken = InternalUtil.keyString(Enumerable.create(entities).last().getProperty(context.keyPropertyName).getValue(), false);
        }

        return Responses.entities(entities, context.ees, response.inlineCount, skipToken);

    }

    private NavPropertyResponse getNavProperty(final Context context, String navProp) {
        final Object jpaEntity = context.em.find(context.jpaEntityType.getJavaType(), context.entityKey);
        if (jpaEntity == null) {
            throw new EntityNotFoundException(
                    context.jpaEntityType.getJavaType() + " not found with key " + context.entityKey);
        }

        Object entityProp = jpaEntity;
        String propName = null;
        Object propInfo = null;
        EdmEntitySet ees = null;

        for (String pn : navProp.split("/")) {
            if (entityProp instanceof Iterable) {
                throw new UnsupportedOperationException(
                        String.format(
                        "The request URI is not valid. Since the segment '%s' refers to a collection, this must be the last segment in the request URI. All intermediate segments must refer to a single resource.",
                        entityProp.getClass().getSimpleName()));
            }

            String[] propSplit = pn.split("\\(");
            propName = propSplit[0];

            entityProp = CoreUtils.getFieldValue(
                    entityProp,
                    propName,
                    entityProp.getClass());

            if (propSplit.length > 1) {
                String id = (String) OptionsQueryParser.parseIdObject(
                        "(" + propSplit[1]);

                propInfo = this.metadata.findEdmProperty(propName);
                entityProp = this.findEntityById(
                        (Iterable) entityProp,
                        ((EdmNavigationProperty) propInfo).toRole.type.keys,
                        Integer.parseInt(id));
            }

            if (entityProp == null) {
                throw new EntityNotFoundException(
                        String.format(
                        "Resource not found for the segment '%s'.",
                        pn));
            }
        }

        propInfo = this.metadata.findEdmProperty(propName);
        List<OEntity> entities = new LinkedList<OEntity>();
        EdmMultiplicity mul = EdmMultiplicity.ONE;

        if (propInfo instanceof EdmNavigationProperty) {
            EntityType jpatype = findJPAEntityType(
                    context.em,
                    ((EdmNavigationProperty) propInfo).toRole.type.name);

            if (entityProp instanceof Iterable) {
                for (Object item : (Iterable) entityProp) {
                    if (ees == null) {
                        ees = this.metadata.findEdmEntitySet(item.getClass().getSimpleName());
                    }

                    final OEntity entity = OEntities.create(
                            jpaEntityToOProperties(
                            ees,
                            jpatype,
                            item),
                            null);

                    entities.add(entity);
                }

                mul = EdmMultiplicity.MANY;
            } else {
                ees = this.metadata.findEdmEntitySet(entityProp.getClass().getSimpleName());
                final OEntity entity = OEntities.create(
                        jpaEntityToOProperties(
                        ees,
                        jpatype,
                        entityProp),
                        null);

                entities.add(entity);
            }
        } else {
            ees = this.metadata.findEdmEntitySet(entityProp.getClass().getSimpleName());
            OProperty op = OProperties.simple(
                    ((EdmProperty) propInfo).name,
                    ((EdmProperty) propInfo).type,
                    entityProp);

            List<OProperty<?>> ls = new LinkedList<OProperty<?>>();
            ls.add(op);

            final OEntity entity = OEntities.create(ls, null);
            entities.add(entity);
        }

        entities = ListUtils.applyQuery(entities, context.query, this.maxResults);
        String skipToken = ListUtils.computeSkipToken(entities, context.query, this.maxResults);
        Integer inlineCount = ListUtils.computeInlineCount(entities, context.query);

        return Responses.navProperty(
                entities,
                ees,
                mul,
                inlineCount,
                skipToken);
    }

    private Object findEntityById(Iterable entities, List<String> keys, Integer id) {
        for (Object entity : entities) {
            for (String idkey : keys) {
                Object value = CoreUtils.getFieldValue(
                        entity,
                        idkey,
                        entity.getClass());

                if (value.equals(id)) {
                    return entity;
                }
            }
        }

        return null;
    }

    private <T> T common(final String entitySetName, Object entityKey, QueryInfo query, Func1<Context, T> fn) {
        Context context = new Context();

        context.em = emf.createEntityManager();
        try {
            context.ees = metadata.getEdmEntitySet(entitySetName);
            context.jpaEntityType = findJPAEntityType(context.em, context.ees.type.name);
            context.keyPropertyName = context.ees.type.keys.get(0);
            context.entityKey = entityKey;
            context.query = query;
            return fn.apply(context);

        } finally {
            context.em.close();
        }
    }

    private static List<OProperty<?>> jpaEntityToOProperties(EdmEntitySet ees, EntityType<?> entityType, Object jpaEntity) {
        List<OProperty<?>> rt = new ArrayList<OProperty<?>>();

        try {
            for (EdmProperty ep : ees.type.properties) {

                Attribute<?, ?> att = entityType.getAttribute(ep.name);
                Member member = att.getJavaMember();

                Object value;
                if (member instanceof Method) {
                    Method method = (Method) member;
                    value = method.invoke(jpaEntity);
                } else if (member instanceof Field) {
                    Field field = (Field) member;
                    value = field.get(jpaEntity);
                } else {
                    throw new UnsupportedOperationException("Implement member" + member);
                }

                if (ep.type == EdmType.STRING) {
                    String sValue = (String) value;
                    rt.add(OProperties.string(ep.name, sValue));
                } else if (ep.type == EdmType.INT32) {
                    Integer iValue = (Integer) value;
                    rt.add(OProperties.int32(ep.name, iValue));
                } else if (ep.type == EdmType.BOOLEAN) {
                    Boolean bValue = (Boolean) value;
                    rt.add(OProperties.boolean_(ep.name, bValue));
                } else if (ep.type == EdmType.INT16) {
                    Short sValue = (Short) value;
                    rt.add(OProperties.short_(ep.name, sValue));
                } else if (ep.type == EdmType.INT64) {
                    Long iValue = (Long) value;
                    rt.add(OProperties.int64(ep.name, iValue));
                } else if (ep.type == EdmType.DECIMAL) {
                    BigDecimal dValue = (BigDecimal) value;
                    rt.add(OProperties.decimal(ep.name, dValue));
                } else if (ep.type == EdmType.DATETIME) {
                    Date dValue = (Date) value;
                    rt.add(OProperties.datetime(ep.name, dValue));
                } else if (ep.type == EdmType.BINARY) {
                    byte[] bValue = (byte[]) value;
                    rt.add(OProperties.binary(ep.name, bValue));
                } else {
                    throw new UnsupportedOperationException("Implement " + ep.type);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return rt;
    }

    private static EntityType<?> findJPAEntityType(EntityManager em, String jpaEntityTypeName) {
        for (EntityType<?> et : em.getMetamodel().getEntities()) {
            if (et.getName().equals(jpaEntityTypeName)) {
                return et;
            }
        }
        throw new RuntimeException("JPA Entity type " + jpaEntityTypeName + " not found");
    }

    private static class DynamicEntitiesResponse {

        public final Integer inlineCount;
        public final boolean useSkipToken;
        public final Enumerable<Object> jpaEntities;

        public DynamicEntitiesResponse(Enumerable<Object> jpaEntities, Integer inlineCount, boolean useSkipToken) {
            this.jpaEntities = jpaEntities;
            this.inlineCount = inlineCount;
            this.useSkipToken = useSkipToken;
        }
    }

    public static DynamicEntitiesResponse enumJpaEntities(
            EntityManager em, Class<?> clazz,
            QueryInfo query,
            int maxResults) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object> cq = cb.createQuery();

        Root<?> root = cq.from(em.getMetamodel().entity(clazz));
        cq = cq.select(root);

        if (query.filter != null) {
            cq = cq.where(
                    InJPAEvaluation.evaluate(
                    query.filter,
                    cb,
                    root));
        }

        if (query.orderBy != null) {
            for (OrderByExpression orderBy : query.orderBy) {
                String field = (String) InJPAEvaluation.evaluate(
                        orderBy.getExpression(),
                        cb,
                        root);

                if (orderBy.isAscending()) {
                    cq = cq.orderBy(cb.asc(root.get(field)));
                } else {
                    cq = cq.orderBy(cb.desc(root.get(field)));
                }
            }
        }

        TypedQuery<Object> tq = em.createQuery(cq);
        Integer inlineCount = query.inlineCount == InlineCount.ALLPAGES
                ? tq.getResultList().size()
                : null;

        if (query.top != null) {
            if (query.top.equals(0)) {
                return new DynamicEntitiesResponse(
                        Enumerable.empty(Object.class),
                        inlineCount,
                        false);
            }

            if (query.top < maxResults) {
                maxResults = query.top;
            }
        }

        tq = tq.setMaxResults(maxResults + 1);
        if (query.skip != null) {
            tq = tq.setFirstResult(query.skip);
        }

        List<Object> results = tq.getResultList();
        boolean useSkipToken = results.size() > maxResults;

        Enumerable<Object> jpaEntities = Enumerable.create(results).take(maxResults);
        return new DynamicEntitiesResponse(jpaEntities, inlineCount, useSkipToken);
    }

    private Object createNewJPAEntity(
            EntityManager em,
            EntityType<?> jpaEntityType,
            OEntity oentity,
            boolean withLinks) {
        try {
            Constructor<?> ctor = jpaEntityType.getJavaType().getDeclaredConstructor();
            ctor.setAccessible(true);
            Object jpaEntity = ctor.newInstance();

            applyOProperties(jpaEntityType, oentity.getProperties(), jpaEntity);
            if (withLinks) {
                applyOLinks(em, jpaEntityType, oentity.getLinks(), jpaEntity);
            }

            return jpaEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void applyOLinks(EntityManager em, EntityType<?> jpaEntityType, List<OLink> links, Object jpaEntity) {
        try {
            for (final OLink link : links) {
                String[] propNameSplit = link.getRelation().split("/");
                String propName = propNameSplit[propNameSplit.length - 1];

                Attribute<?, ?> att = jpaEntityType.getAttribute(propName);
                Member member = att.getJavaMember();

                if (!(member instanceof Field)) {
                    throw new UnsupportedOperationException("Implement member" + member);
                }

                WebResource webResource = httpClient.resource(link.getHref());
                String requestEntity = webResource.get(String.class);

                OEntity relOEntity = BaseResource.ConvertFromString(requestEntity);
                String term = ((AtomInfo) relOEntity).getCategoryTerm();
                EdmEntitySet ees = metadata.getEdmEntitySet(term.split("\\.")[1]);
                EntityType<?> jpaRelType = findJPAEntityType(em, ees.type.name);
                Object relEntity = createNewJPAEntity(em, jpaRelType, relOEntity, false);

                Field field = (Field) member;
                field.setAccessible(true);
                field.set(jpaEntity, relEntity);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyOProperties(EntityType<?> jpaEntityType, List<OProperty<?>> properties, Object jpaEntity) {
        try {
            for (OProperty<?> prop : properties) {
                // EdmProperty ep = findProperty(ees,prop.getName());
                Attribute<?, ?> att = jpaEntityType.getAttribute(prop.getName());
                Member member = att.getJavaMember();

                if (!(member instanceof Field)) {
                    throw new UnsupportedOperationException("Implement member" + member);
                }
                Field field = (Field) member;
                field.setAccessible(true);

                Object value = prop.getValue();
                if (value instanceof LocalDateTime && field.getType() == Date.class) {
                    value = ((LocalDateTime) value).toDateTime().toDate();
                }

                field.set(jpaEntity, value);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityResponse createEntity(String entitySetName, OEntity entity) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityType<?> jpaEntityType = findJPAEntityType(em, ees.type.name);
            Object jpaEntity = createNewJPAEntity(em, jpaEntityType, entity, true);
            em.persist(jpaEntity);
            em.getTransaction().commit();

            final OEntity responseEntity = makeEntity(ees, jpaEntityType, null, jpaEntity);

            return new EntityResponse() {

                @Override
                public OEntity getEntity() {
                    return responseEntity;
                }

                @Override
                public EdmEntitySet getEntitySet() {
                    return ees;
                }
            };

        } finally {
            em.close();
        }
    }

    @Override
    public void deleteEntity(String entitySetName, Object entityKey) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityType<?> jpaEntityType = findJPAEntityType(em, ees.type.name);
            Object jpaEntity = em.find(jpaEntityType.getJavaType(), entityKey);
            em.remove(jpaEntity);
            em.getTransaction().commit();

        } finally {
            em.close();
        }

    }

    @Override
    public void mergeEntity(String entitySetName, Object entityKey, OEntity entity) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityType<?> jpaEntityType = findJPAEntityType(em, ees.type.name);
            Object jpaEntity = em.find(jpaEntityType.getJavaType(), entityKey);

            applyOProperties(jpaEntityType, entity.getProperties(), jpaEntity);

            em.getTransaction().commit();

        } finally {
            em.close();
        }

    }

    @Override
    public void updateEntity(String entitySetName, Object entityKey, OEntity entity) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityType<?> jpaEntityType = findJPAEntityType(em, ees.type.name);
            Object jpaEntity = createNewJPAEntity(em, jpaEntityType, entity, true);
            em.merge(jpaEntity);
            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }
}
