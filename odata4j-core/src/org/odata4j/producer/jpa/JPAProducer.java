package org.odata4j.producer.jpa;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;

import core4j.Enumerable;
import core4j.Func1;
import core4j.Predicate1;

public class JPAProducer implements ODataProducer {

    private final EntityManagerFactory emf;
    private final EntityManager em;
    private final String namespace;
    private final EdmDataServices metadata;

    public JPAProducer(EntityManagerFactory emf, String namespace) {

        this.emf = emf;
        this.namespace = namespace;

        em = emf.createEntityManager(); // necessary for metamodel
        this.metadata = EdmGenerator.buildEdm(emf, this.namespace);
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
        if (jpaEntity == null)
            throw new RuntimeException(context.jpaEntityType.getJavaType() + " not found with key " + context.entityKey);

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
        return new OEntity() {
            public List<OProperty<?>> getProperties() {
                return jpaEntityToOProperties(ees, jpaEntityType, jpaEntity);
            }

            @Override
            public List<OProperty<?>> getKeyProperties() {
                return Enumerable.create(getProperties()).where(new Predicate1<OProperty<?>>() {
                    public boolean apply(OProperty<?> input) {
                        return input.getName().equals(keyPropertyName);
                    }
                }).toList();
            }
        };
    }

    private EntitiesResponse getEntities(final Context context) {

        final DynamicEntitiesResponse response = enumJpaEntities(context.em, context.jpaEntityType.getJavaType(), context.query);
        final List<OEntity> entities = response.jpaEntities.select(new Func1<Object, OEntity>() {
            public OEntity apply(final Object input) {
                return makeEntity(context, input);
            }
        }).toList();

        return new EntitiesResponse() {
            public List<OEntity> getEntities() {
                return entities;
            }

            @Override
            public EdmEntitySet getEntitySet() {
                return context.ees;
            }

            @Override
            public Integer getInlineCount() {
                return response.inlineCount;
            }
        };
    }

    private <T> T common(final String entitySetName, Object entityKey, QueryInfo query, Func1<Context, T> fn) {
        Context context = new Context();

        context.em = emf.createEntityManager();
        try {
            context.ees = metadata.getEdmEntitySet(entitySetName);
            context.jpaEntityType = findJPAEntityType(context.em, context.ees.type.name);
            context.keyPropertyName = context.ees.type.key;
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
            for(EdmProperty ep : ees.type.properties) {

                Attribute<?, ?> att = entityType.getAttribute(ep.name);
                Member member = att.getJavaMember();

                if (!(member instanceof Field))
                    throw new UnsupportedOperationException("Implement member" + member);
                Field field = (Field) member;
                Object value = field.get(jpaEntity);
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
        for(EntityType<?> et : em.getMetamodel().getEntities()) {
            if (et.getName().equals(jpaEntityTypeName))
                return et;
        }
        throw new RuntimeException("JPA Entity type " + jpaEntityTypeName + " not found");
    }

    private static class DynamicEntitiesResponse {
        public final Integer inlineCount;
        public final Enumerable<Object> jpaEntities;

        public DynamicEntitiesResponse(Enumerable<Object> jpaEntities, Integer inlineCount) {
            this.jpaEntities = jpaEntities;
            this.inlineCount = inlineCount;
        }
    }

    public static DynamicEntitiesResponse enumJpaEntities(EntityManager em, Class<?> clazz, QueryInfo query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object> cq = cb.createQuery();

        Root<?> root = cq.from(em.getMetamodel().entity(clazz));
        cq = cq.select(root);

        // if (query.orderBy != null){
        // for(OrderBy orderBy : query.orderBy){
        // if (orderBy.ascending)
        // cq = cq.orderBy(cb.asc(root.get(orderBy.field)));
        // else
        // cq = cq.orderBy(cb.desc(root.get(orderBy.field)));
        // }
        //			
        // }

        TypedQuery<Object> tq = em.createQuery(cq);

        Integer inlineCount = query.inlineCount == InlineCount.ALLPAGES ? tq.getResultList().size() : null;

        if (query.top != null) {
            if (query.top.equals(0)) {
                return new DynamicEntitiesResponse(Enumerable.empty(Object.class), inlineCount);
            }
            tq = tq.setMaxResults(query.top);
        }
        if (query.skip != null) {
            tq = tq.setFirstResult(query.skip);
        }
        List<Object> results = tq.getResultList();
        return new DynamicEntitiesResponse(Enumerable.create(results), inlineCount);
    }

    private static Object createNewJPAEntity(EdmEntitySet ees, EntityType<?> jpaEntityType, List<OProperty<?>> properties) {
        try {
            Constructor<?> ctor = jpaEntityType.getJavaType().getDeclaredConstructor();
            ctor.setAccessible(true);
            Object jpaEntity = ctor.newInstance();

            applyOProperties(jpaEntityType, properties, jpaEntity);

            return jpaEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyOProperties(EntityType<?> jpaEntityType, List<OProperty<?>> properties, Object jpaEntity) {
        try {
            for(OProperty<?> prop : properties) {
                // EdmProperty ep = findProperty(ees,prop.getName());
                Attribute<?, ?> att = jpaEntityType.getAttribute(prop.getName());
                Member member = att.getJavaMember();

                if (!(member instanceof Field))
                    throw new UnsupportedOperationException("Implement member" + member);
                Field field = (Field) member;
                field.setAccessible(true);
                field.set(jpaEntity, prop.getValue());

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityResponse createEntity(String entitySetName, List<OProperty<?>> properties) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityType<?> jpaEntityType = findJPAEntityType(em, ees.type.name);
            Object jpaEntity = createNewJPAEntity(ees, jpaEntityType, properties);
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
    public void mergeEntity(String entitySetName, Object entityKey, List<OProperty<?>> properties) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityType<?> jpaEntityType = findJPAEntityType(em, ees.type.name);
            Object jpaEntity = em.find(jpaEntityType.getJavaType(), entityKey);

            applyOProperties(jpaEntityType, properties, jpaEntity);

            em.getTransaction().commit();

        } finally {
            em.close();
        }

    }

    @Override
    public void updateEntity(String entitySetName, Object entityKey, List<OProperty<?>> properties) {
        final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityType<?> jpaEntityType = findJPAEntityType(em, ees.type.name);
            Object jpaEntity = createNewJPAEntity(ees, jpaEntityType, properties);
            em.merge(jpaEntity);
            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

}
