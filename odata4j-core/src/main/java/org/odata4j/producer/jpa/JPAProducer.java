package org.odata4j.producer.jpa;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmPropertyBase;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.internal.TypeConverter;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.PropertyResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;

public class JPAProducer implements ODataProducer {

	private final EntityManagerFactory emf;
	private final EntityManager em;
	private final EdmDataServices metadata;
	private final int maxResults;

	public JPAProducer(
			EntityManagerFactory emf,
			EdmDataServices metadata,
			int maxResults) {

		this.emf = emf;
		this.maxResults = maxResults;
		this.metadata = metadata;
		em = emf.createEntityManager(); // necessary for metamodel
	}

	public JPAProducer(
			EntityManagerFactory emf,
			String namespace,
			int maxResults) {
		this(emf, new JPAEdmGenerator().buildEdm(emf, namespace), maxResults);
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
	public EntityResponse getEntity(String entitySetName, OEntityKey entityKey,
			QueryInfo queryInfo) {
		return common(entitySetName, entityKey, queryInfo,
				new Func1<Context, EntityResponse>() {
					public EntityResponse apply(Context input) {
						return getEntity(input);
					}
				});
	}

	@Override
	public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
		return common(entitySetName, null, queryInfo,
				new Func1<Context, EntitiesResponse>() {
					public EntitiesResponse apply(Context input) {
						return getEntities(input);
					}
				});
	}

	@Override
	public BaseResponse getNavProperty(
			final String entitySetName,
			final OEntityKey entityKey,
			final String navProp,
			final QueryInfo queryInfo) {
		
		return common(
				entitySetName,
				entityKey,
				queryInfo,
				new Func1<Context, BaseResponse>() {
					public BaseResponse apply(Context input) {
						return getNavProperty(input, navProp);
					}
				});
	}

	private static class Context {

		EntityManager em;
		EdmEntitySet ees;
		EntityType<?> jpaEntityType;
		String keyAttributeName;
		Object typeSafeEntityKey;
		QueryInfo query;
		EdmPropertyBase edmPropertyBase;
	}

	private EntityResponse getEntity(final Context context) {
		Object jpaEntity = context.em.find(
				context.jpaEntityType.getJavaType(),
				context.typeSafeEntityKey);

		if (jpaEntity == null) {
			throw new EntityNotFoundException(
					context.jpaEntityType.getJavaType()
							+ " not found with key "
							+ context.typeSafeEntityKey);
		}

		OEntity entity = makeEntity(context, jpaEntity);
		return Responses.entity(entity);
	}

	private OEntity makeEntity(Context context, Object jpaEntity) {

		return jpaEntityToOEntity(
				context.ees,
				context.jpaEntityType,
				jpaEntity,
				context.query == null ? null : context.query.expand,
				context.query == null ? null : context.query.select);
	}

	private EntitiesResponse getEntities(final Context context) {

		DynamicEntitiesResponse response = getEntitiesResponse(context,null);

		return Responses.entities(
				response.entities,
				context.ees,
				response.inlineCount,
				response.skipToken);
	}

	private BaseResponse getNavProperty(final Context context,String navProp) {

		DynamicEntitiesResponse response = getEntitiesResponse(context,navProp);
		if (response.responseType.equals(PropertyResponse.class))
			return Responses.property(response.property);
		if (response.responseType.equals(EntityResponse.class))
			return Responses.entity(response.entity);
		if (response.responseType.equals(EntitiesResponse.class))
			return Responses.entities(
					response.entities,
					context.ees,
					response.inlineCount,
					response.skipToken);
		
		throw new UnsupportedOperationException("Unknown responseType: " + response.responseType.getName());
	}

	private <T> T common(
			String entitySetName,
			OEntityKey entityKey,
			QueryInfo query,
			Func1<Context, T> fn) {
		Context context = new Context();

		context.em = emf.createEntityManager();
		try {
			context.ees = metadata.getEdmEntitySet(entitySetName);
			context.jpaEntityType = getJPAEntityType(
					context.em,
					context.ees.type.name);

			context.keyAttributeName = JPAEdmGenerator.getIdAttribute(context.jpaEntityType).getName();
			context.typeSafeEntityKey = typeSafeEntityKey(
					context.em,
					context.jpaEntityType,
					entityKey);

			context.query = query;
			return fn.apply(context);

		} finally {
			context.em.close();
		}
	}

	private OEntity jpaEntityToOEntity(
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
			for (EdmProperty ep : ees.type.properties) {

				if (!isSelected(ep.name, select)) {
					continue;
				}

				// we have a embedded composite key and we want a property from
				// that key
				if (hasEmbeddedCompositeKey && ees.type.keys.contains(ep.name)) {
					Object value = getIdValue(jpaEntity, idAtt, ep.name);

					properties.add(OProperties.simple(
							ep.name,
							ep.type,
							value,
							true));

				} else {
					// get the simple attribute
					Attribute<?, ?> att = entityType.getAttribute(ep.name);
					JPAMember member = JPAMember.create(att,jpaEntity);
					Object value = member.get();

					properties.add(OProperties.simple(
							ep.name,
							ep.type,
							value,
							true));
				}
			}

			for (final EdmNavigationProperty ep : ees.type.navigationProperties) {
				ep.selected = isSelected(ep.name, select);
			}

			// get the collections if necessary
			if (expand != null && !expand.isEmpty()) {
				for (final EntitySimpleProperty propPath : expand) {

					// split the property path into the first and remaining
					// parts
					String[] props = propPath.getPropertyName().split("/", 2);
					String prop = props[0];
					List<EntitySimpleProperty> remainingPropPath = props.length > 1
							? Arrays.asList(org.odata4j.expression.Expression
									.simpleProperty(props[1])) : null;

					Attribute<?, ?> att = entityType.getAttribute(prop);
					if (att.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_MANY
							|| att.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_MANY) {

						Collection<?> value = JPAMember.create(att,jpaEntity).get();
		
						List<OEntity> relatedEntities = new ArrayList<OEntity>();
						for (Object relatedEntity : value) {
							EntityType<?> elementEntityType = (EntityType<?>) ((PluralAttribute<?, ?, ?>) att)
											.getElementType();
							EdmEntitySet elementEntitySet = metadata
									.getEdmEntitySet(JPAEdmGenerator.getEntitySetName(elementEntityType));

							relatedEntities.add(jpaEntityToOEntity(
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

						Object relatedEntity = JPAMember.create(att,jpaEntity).get();
				
						if( relatedEntity == null )
						{
							links.add(OLinks.relatedEntityInline(
									null,
									prop,
									null,
									null));		
							
						}else
						{
							links.add(OLinks.relatedEntityInline(
									null,
									prop,
									null,
									jpaEntityToOEntity(
											relatedEntitySet,
											relatedEntityType,
											relatedEntity,
											remainingPropPath,
											null)));
						}
	
					}

				}
			}


			return OEntities.create(ees, toOEntityKey(jpaEntity,idAtt), properties, links);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private OEntityKey toOEntityKey(Object jpaEntity, SingularAttribute<?, ?> idAtt ){
		boolean hasEmbeddedCompositeKey =
			idAtt.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED;
		if (!hasEmbeddedCompositeKey){
			Object id = getIdValue(jpaEntity, idAtt, null);
			return OEntityKey.create(id);
		}
		ManagedType<?> keyType = (ManagedType<?>) idAtt.getType();
		
		Map<String,Object> nameValues = new HashMap<String,Object>();
		for(Attribute<?,?> att : keyType.getAttributes())
			nameValues.put(att.getName(), getIdValue(jpaEntity, idAtt, att.getName()));
		return OEntityKey.create(nameValues);
	}
	
	

	private static boolean isSelected(String name, List<EntitySimpleProperty> select) {

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

	private static Object getIdValue(
			Object jpaEntity,
			SingularAttribute<?, ?> idAtt,
			String propName) {
		try {
			// get the composite id
			Object keyValue = JPAMember.create(idAtt,jpaEntity).get();

			if (propName == null) 
				return keyValue;

			// get the property from the key
			ManagedType<?> keyType = (ManagedType<?>) idAtt.getType();
			Attribute<?, ?> att = keyType.getAttribute(propName);
			return JPAMember.create(att,keyValue).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	
	private static EntityType<?> getJPAEntityType(EntityManager em, String jpaEntityTypeName) {

		for (EntityType<?> et : em.getMetamodel().getEntities()) {
			if (JPAEdmGenerator.getEntitySetName(et).equals(jpaEntityTypeName)) {
				return et;
			}
		}

		throw new RuntimeException("JPA Entity type " + jpaEntityTypeName + " not found");
	}

	private static class DynamicEntitiesResponse {

		public final Class<?> responseType;
		public final OProperty<?> property;
		public final OEntity entity;
		public final List<OEntity> entities;
		public final Integer inlineCount;
		public final String skipToken;
		
		
		public static DynamicEntitiesResponse property(OProperty<?> property){
			return new DynamicEntitiesResponse(PropertyResponse.class,property,null,null,null,null);
		}
		
		public static DynamicEntitiesResponse entity(OEntity entity){
			return new DynamicEntitiesResponse(EntityResponse.class,null,entity,null,null,null);
		}
		public static DynamicEntitiesResponse entities(List<OEntity> entityList,Integer inlineCount,String skipToken){
			return new DynamicEntitiesResponse(EntitiesResponse.class,null,null,entityList,inlineCount,skipToken);
		}
		private DynamicEntitiesResponse(
				Class<?> responseType,
				OProperty<?> property,
				OEntity entity,
				List<OEntity> entityList,
				Integer inlineCount,
				String skipToken) {
			this.responseType = responseType;
			this.property = property;
			this.entity = entity;
			this.entities = entityList;
			this.inlineCount = inlineCount;
			this.skipToken = skipToken;
		}
	}

	private String whereKeyEquals(Context context, String alias){
		SingularAttribute<?,?> idAtt = context.jpaEntityType.getSingularAttribute(context.keyAttributeName);
		if (idAtt.getPersistentAttributeType()==PersistentAttributeType.EMBEDDED){
			List<String> predicates = new ArrayList<String>();
			EmbeddableType<?> et = (EmbeddableType<?>)idAtt.getType();
			for(Attribute<?,?> subAtt : et.getAttributes()){
				Object subAttValue = JPAMember.create(subAtt, context.typeSafeEntityKey).get();
				String jpqlLiteral = JPQLGenerator.toJpqlLiteral(subAttValue);
				String predicate = String.format(
						"(%s.%s.%s = %s)",
						alias,
						context.keyAttributeName,
						subAtt.getName(),
						jpqlLiteral);
				predicates.add(predicate);
			}
			
			return "("+Enumerable.create(predicates).join(" AND ")+")";
		}
		
		String jpqlLiteral = JPQLGenerator.toJpqlLiteral(context.typeSafeEntityKey);
		return String.format(
				"(%s.%s = %s)",
				alias,
				context.keyAttributeName,
				jpqlLiteral);
	}
	
	@SuppressWarnings("unchecked")
	private DynamicEntitiesResponse getEntitiesResponse(final Context context, String navProp) {

		// generate jpql
		String jpql = generateJPQL(context, navProp);

		// jpql -> jpa query
		Query tq = context.em.createQuery(jpql);

		Integer inlineCount = context.query.inlineCount == InlineCount.ALLPAGES
				? tq.getResultList().size()
				: null;

		int queryMaxResults = maxResults;
		if (context.query.top != null) {
			
			// top=0: don't even hit jpa, return a response with zero entities
			if (context.query.top.equals(0)) 
				return DynamicEntitiesResponse.entities(null,inlineCount,null);
			
			if (context.query.top < maxResults) 
				queryMaxResults = context.query.top;
		}

		// jpa query for one more than specified to determine whether or not to return a skip token
		tq = tq.setMaxResults(queryMaxResults + 1);

		if (context.query.skip != null) 
			tq = tq.setFirstResult(context.query.skip);
		
		// execute jpa query
		List<Object> results = tq.getResultList();
		
		// property response
		if (context.edmPropertyBase instanceof EdmProperty) {
			EdmProperty propInfo = (EdmProperty) context.edmPropertyBase;

			if (results.size() != 1)
				throw new RuntimeException("Expected one and only one result for property, found " + results.size());
			
			Object value = results.get(0);
			OProperty<?> op = OProperties.simple(
					((EdmProperty) propInfo).name,
					((EdmProperty) propInfo).type,
					value);
			return DynamicEntitiesResponse.property(op);
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
		boolean hasMoreResults = context.query.top != null
				? context.query.top > maxResults && results.size() > queryMaxResults
				: results.size() > queryMaxResults;
				
		if (hasMoreResults) 
			skipToken = JPASkipToken.create(context.query.orderBy, Enumerable.create(entities).last());
		
		if (context.edmPropertyBase instanceof EdmNavigationProperty){
			EdmNavigationProperty edmNavProp = (EdmNavigationProperty)context.edmPropertyBase;
			if (edmNavProp.toRole.multiplicity == EdmMultiplicity.ONE || edmNavProp.toRole.multiplicity == EdmMultiplicity.ZERO_TO_ONE){
				if (entities.size()!=1)
					throw new RuntimeException("Expected only one entity, found " + entities.size());
				return DynamicEntitiesResponse.entity(entities.get(0));
			}
		}
		
		return DynamicEntitiesResponse.entities(entities, inlineCount, skipToken);
	}
	
	private String generateJPQL(Context context, String navProp){
		String alias = "t0";
		String from = context.jpaEntityType.getName() + " " + alias;
		String where = null;

		if (navProp != null) {
			where = whereKeyEquals(context,alias);

			String prop = null;
			int propCount = 0;

			for (String pn : navProp.split("/")) {
				String[] propSplit = pn.split("\\(");
				prop = propSplit[0];
				propCount++;

				if (context.edmPropertyBase instanceof EdmProperty) {
					throw new UnsupportedOperationException(
							String.format(
									"The request URI is not valid. Since the segment '%s' "
											+ "refers to a collection, this must be the last segment "
											+ "in the request URI. All intermediate segments must refer "
											+ "to a single resource.",
									alias));
				}

				context.edmPropertyBase = metadata.findEdmProperty(prop);
				
				if (context.edmPropertyBase instanceof EdmNavigationProperty) {
					EdmNavigationProperty propInfo = (EdmNavigationProperty) context.edmPropertyBase;
					
					context.jpaEntityType = getJPAEntityType(
							context.em,
							propInfo.toRole.type.name);

					context.ees = metadata.findEdmEntitySet(JPAEdmGenerator.getEntitySetName(context.jpaEntityType));

					prop = alias + "." + prop;
					alias = "t" + Integer.toString(propCount);
					from = String.format("%s JOIN %s %s", from, prop, alias);

					if (propSplit.length > 1) {
						OEntityKey entityKey = OEntityKey.parse("(" + propSplit[1]);
							
						context.keyAttributeName = JPAEdmGenerator.getIdAttribute(context.jpaEntityType).getName();

						context.typeSafeEntityKey = typeSafeEntityKey(
								em,
								context.jpaEntityType,
								entityKey);

						where = whereKeyEquals(context,alias);
					}
				} else if (context.edmPropertyBase instanceof EdmProperty) {
					EdmProperty propInfo = (EdmProperty) context.edmPropertyBase;

					alias = alias + "." + propInfo.name;
					// TODO
					context.ees = null;
				}

				if (context.edmPropertyBase == null) {
					throw new EntityNotFoundException(
							String.format(
									"Resource not found for the segment '%s'.",
									pn));
				}
			}
		}

		String jpql = String.format("SELECT %s FROM %s", alias, from);

		JPQLGenerator jpqlGen = new JPQLGenerator(context.keyAttributeName, alias);

		if (context.query.filter != null) {
			String filterPredicate = jpqlGen.toJpql(context.query.filter);
			where = addWhereExpression(where, filterPredicate, "AND");
		}

		if (context.query.skipToken != null) {
			BoolCommonExpression skipTokenPredicateExpr = JPASkipToken.parse(jpqlGen.getPrimaryKeyName(), context.query.orderBy, context.query.skipToken);
			String skipTokenPredicate = jpqlGen.toJpql(skipTokenPredicateExpr);
			where = addWhereExpression(where, skipTokenPredicate, "AND");
		}

		if (where != null) 
			jpql = String.format("%s WHERE %s", jpql, where);
		
		if (context.query.orderBy != null && !context.query.orderBy.isEmpty()) {
			List<String> orderBys = new ArrayList<String>();
			for (OrderByExpression orderBy : context.query.orderBy) {
				String field = jpqlGen.toJpql(orderBy.getExpression());
				orderBys.add(field + (orderBy.isAscending()?"":" DESC"));				
			}
			jpql = jpql + " ORDER BY " + Enumerable.create(orderBys).join(",");
		}
		
		return jpql;
	}

	private static String addWhereExpression(String expression, String nextExpression, String condition) {

		return expression == null
				? nextExpression
				: String.format(
						"%s %s %s",
						expression,
						condition,
						nextExpression);
	}

	
	private Object createNewJPAEntity(
			EntityManager em,
			EntityType<?> jpaEntityType,
			OEntity oEntity,
			boolean withLinks) {
		
		
		Object jpaEntity = newInstance(jpaEntityType.getJavaType());

		if (jpaEntityType.getIdType().getPersistenceType()==PersistenceType.EMBEDDABLE){
			EmbeddableType<?> et =(EmbeddableType<?>)jpaEntityType.getIdType();
			
			JPAMember idMember = JPAMember.create(jpaEntityType.getId(et.getJavaType()),jpaEntity);
			Object idValue = newInstance(et.getJavaType());
			idMember.set(idValue);
		}
		
		applyOProperties(em, jpaEntityType, oEntity.getProperties(), jpaEntity);
		if (withLinks) 
			applyOLinks(em, jpaEntityType, oEntity.getLinks(), jpaEntity);
		

		return jpaEntity;
	}

	@SuppressWarnings("unchecked")
	private static <T> T newInstance(Class<?> javaType){
		try {
			if (javaType.equals(Collection.class))
				javaType = HashSet.class;
			Constructor<?> ctor = javaType.getDeclaredConstructor();
			ctor.setAccessible(true);
			return (T)ctor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void applyOLinks(EntityManager em, EntityType<?> jpaEntityType, List<OLink> links, Object jpaEntity) {
		if (links==null)
			return;
		
		try {
			for (final OLink link : links) {
				String[] propNameSplit = link.getRelation().split("/");
				String propName = propNameSplit[propNameSplit.length - 1];

				if (link instanceof ORelatedEntitiesLinkInline) {
					PluralAttribute<?, ?, ?> att = (PluralAttribute<?, ?, ?>)jpaEntityType.getAttribute(propName);
					JPAMember member = JPAMember.create(att,jpaEntity);
					
					EntityType<?> collJpaEntityType = (EntityType<?>)att.getElementType();

					OneToMany oneToMany = member.getAnnotation(OneToMany.class);
					boolean hasSingularBackRef = oneToMany != null
						&& oneToMany.mappedBy() != null
						&& !oneToMany.mappedBy().isEmpty();
					
					ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);
					
					Collection<Object> coll = member.get();
					if (coll==null){
						coll = (Collection<Object>)newInstance(member.getJavaType());
						member.set(coll);
					}
					for (OEntity oentity : ((ORelatedEntitiesLinkInline)link).getRelatedEntities()) {
						Object collJpaEntity = createNewJPAEntity(em, collJpaEntityType, oentity, true);
						if (hasSingularBackRef) {
							JPAMember backRef = JPAMember.create(collJpaEntityType.getAttribute(oneToMany.mappedBy()),collJpaEntity);
							backRef.set(jpaEntity);
						}
						if (manyToMany!=null) {
							Attribute<?,?> other = null;
							if (manyToMany.mappedBy()!=null&&!manyToMany.mappedBy().isEmpty())
								other = collJpaEntityType.getAttribute(manyToMany.mappedBy());
							else {
								for(Attribute<?,?> att2 : collJpaEntityType.getAttributes()){
									if (att2.isCollection() && JPAMember.create(att2, null).getAnnotation(ManyToMany.class)!=null){
										CollectionAttribute<?, ?> ca = (CollectionAttribute<?, ?>)att2;
										if (ca.getElementType().equals(jpaEntityType)){
											other = ca;
											break;
										}
									}
								}
							}
							
							if (other==null)
								throw new RuntimeException("Could not find other side of many-to-many relationship");
							
							JPAMember backRef = JPAMember.create(other,collJpaEntity);
							Collection<Object> coll2 = backRef.get();
							if (coll2==null){
								coll2 = newInstance(backRef.getJavaType());
								backRef.set(coll2);
							}
							coll2.add(jpaEntity);
						}
						
						em.persist(collJpaEntity);
						coll.add(collJpaEntity);
					}
					
				} else if (link instanceof ORelatedEntityLinkInline ) {
					SingularAttribute<?, ?> att = jpaEntityType.getSingularAttribute(propName);
					JPAMember member = JPAMember.create(att,jpaEntity);
					
					EntityType<?> relJpaEntityType = (EntityType<?>)att.getType();
					Object relJpaEntity = createNewJPAEntity(em, relJpaEntityType, 
							((ORelatedEntityLinkInline)link).getRelatedEntity(), true);
					em.persist(relJpaEntity);

					member.set(relJpaEntity);
				} else if (link instanceof ORelatedEntityLink ) {
					
					// look up the linked entity, and set the member value
					SingularAttribute<?, ?> att = jpaEntityType.getSingularAttribute(propName);
					JPAMember member = JPAMember.create(att,jpaEntity);
					
					EntityType<?> relJpaEntityType = (EntityType<?>)att.getType();
					Object key = typeSafeEntityKey(em, relJpaEntityType, OEntityKey.parse(link.getHref().substring(link.getHref().indexOf('('))));
					Object relEntity = em.find(relJpaEntityType.getJavaType(), key);

					member.set(relEntity);
					
					// set corresponding property (if there is one)
					JoinColumn joinColumn = member.getAnnotation(JoinColumn.class);
					ManyToOne manyToOne =  member.getAnnotation(ManyToOne.class);
					if (joinColumn!=null && manyToOne !=null){
						String columnName = joinColumn.name();
						JPAMember m = JPAMember.findByColumn(jpaEntityType, columnName, jpaEntity);
						if (m!=null)
							m.set(key);
						
					}

				} else {
					throw new UnsupportedOperationException("binding the new entity to many entities is not supported");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void applyOProperties(EntityManager em, ManagedType<?> jpaManagedType, Collection<OProperty<?>> properties, Object jpaEntity) {

		for (OProperty<?> prop : properties) {
			boolean found = false;
			if (jpaManagedType instanceof EntityType) {
			EntityType<?> jpaEntityType  = (EntityType<?>)jpaManagedType;
				if (jpaEntityType.getIdType().getPersistenceType()==PersistenceType.EMBEDDABLE){
					EmbeddableType<?> et =(EmbeddableType<?>)jpaEntityType.getIdType();
					
					for(Attribute<?, ?> idAtt : et.getAttributes()){
						
						if (idAtt.getName().equals(prop.getName())){
	
							Object idValue = JPAMember.create(jpaEntityType.getId(et.getJavaType()),jpaEntity).get();
							
							setAttribute(idAtt,prop,idValue);
							found = true;
							break;
						}
					}
				}
			}
			if (found)
				continue;
			Attribute<?, ?> att = jpaManagedType.getAttribute(prop.getName());
			setAttribute(att,prop,jpaEntity);
		}
	}
	
	private static void setAttribute(Attribute<?, ?> att, OProperty<?> prop, Object target){
		JPAMember attMember = JPAMember.create(att,target);
		Object value = coercePropertyValue(prop, attMember.getJavaType());
		attMember.set(value);
	}
	
	
	@Override
	public EntityResponse createEntity(String entitySetName, OEntity entity) {
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			EntityType<?> jpaEntityType = getJPAEntityType(em, ees.type.name);
			Object jpaEntity = createNewJPAEntity(
					em,
					jpaEntityType,
					entity,
					true);

			em.persist(jpaEntity);
			em.getTransaction().commit();
			
			//	reread the entity in case we had links. This should insure
			//	we get the implicitly set foreign keys. E.g in the Northwind model 
			//	creating a new Product with a link to the Category should return
			//	the CategoryID.
			if (entity.getLinks() != null
				&& !entity.getLinks().isEmpty()) {
				em.getTransaction().begin();
				try {
					em.refresh(jpaEntity);
					em.getTransaction().commit();
				} finally {
					if (em.getTransaction().isActive())
						em.getTransaction().rollback();
				}
			}

			final OEntity responseEntity = jpaEntityToOEntity(
					ees,
					jpaEntityType,
					jpaEntity,
					null,
					null);

			return Responses.entity(responseEntity);

		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, final String navProp, OEntity entity) {
		//	get the EdmEntitySet for the parent (fromRole) entity
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

		//	get the navigation property
		EdmNavigationProperty edmNavProperty = ees.type.getNavigationProperty(navProp);

		//	check whether the navProperty is valid
		if (edmNavProperty == null
			|| edmNavProperty.toRole.multiplicity != EdmMultiplicity.MANY) {
			throw new IllegalArgumentException("unknown navigation property "
					+ navProp + " or navigation property toRole Multiplicity is not '*'" );
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			
			//	get the entity we want the new entity add to
			EntityType<?> jpaEntityType = getJPAEntityType(em, ees.type.name);
			Object typeSafeEntityKey = typeSafeEntityKey(em, jpaEntityType, entityKey);
			Object jpaEntity = em.find(jpaEntityType.getJavaType(), typeSafeEntityKey);

			//	create the new entity
			EntityType<?> newJpaEntityType = getJPAEntityType(em, edmNavProperty.toRole.type.name);
			Object newJpaEntity = createNewJPAEntity(em, newJpaEntityType, entity, true);
			
			//	get the collection attribute and add the new entity to the parent entity
			PluralAttribute attr = Enumerable.create(
					jpaEntityType.getPluralAttributes())
					.firstOrNull(new Predicate1() {
						public boolean apply(Object input) {
							PluralAttribute<?, ?, ?> pa = (PluralAttribute<?, ?, ?>)input;
							return pa.getName().equals(navProp);
						}
					});
			JPAMember member = JPAMember.create(attr,jpaEntity);
			Collection<Object> collection = member.get();
			collection.add(newJpaEntity);
			
			//	TODO handle ManyToMany relationships
			// set the backreference in bidirectional relationships
			OneToMany oneToMany = member.getAnnotation(OneToMany.class);
			if (oneToMany != null
					&& oneToMany.mappedBy() != null
					&& !oneToMany.mappedBy().isEmpty()) {
				JPAMember.create(newJpaEntityType.getAttribute(oneToMany.mappedBy()),newJpaEntity)
					.set(jpaEntity);
			}
			
			//	check whether the EntityManager will persist the
			//	new entity or should we do it
			if (oneToMany != null
					&& oneToMany.cascade() != null) {
				List<CascadeType> cascadeTypes = Arrays.asList(oneToMany.cascade());
				if (!cascadeTypes.contains(CascadeType.ALL)
					&& !cascadeTypes.contains(CascadeType.PERSIST)) {
					em.persist(newJpaEntity);
				}
			}
			
			em.getTransaction().commit();

			//	prepare the response
			final EdmEntitySet toRoleees = getMetadata()
					.getEdmEntitySet(edmNavProperty.toRole.type);
			final OEntity responseEntity = jpaEntityToOEntity(toRoleees,
					newJpaEntityType, newJpaEntity, null, null);

			return Responses.entity(responseEntity);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			em.close();
		}
	}
	
	

	@Override
	public void deleteEntity(String entitySetName, OEntityKey entityKey) {
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			EntityType<?> jpaEntityType = getJPAEntityType(em, ees.type.name);
			Object typeSafeEntityKey = typeSafeEntityKey(
					em,
					jpaEntityType,
					entityKey);

			Object jpaEntity = em.find(
					jpaEntityType.getJavaType(),
					typeSafeEntityKey);

			em.remove(jpaEntity);
			em.getTransaction().commit();

		} finally {
			em.close();
		}

	}

	@Override
	public void mergeEntity(String entitySetName, OEntity entity) {
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			EntityType<?> jpaEntityType = getJPAEntityType(em, ees.type.name);
			Object typeSafeEntityKey = typeSafeEntityKey(
					em,
					jpaEntityType,
					entity.getEntityKey());

			Object jpaEntity = em.find(
					jpaEntityType.getJavaType(),
					typeSafeEntityKey);

			applyOProperties(em, jpaEntityType, entity.getProperties(), jpaEntity);
			applyOLinks(em, jpaEntityType, entity.getLinks(), jpaEntity);
            	
			em.getTransaction().commit();

		} finally {
			em.close();
		}

	}

	
	@Override
	public void updateEntity(String entitySetName, OEntity entity) {
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			EntityType<?> jpaEntityType = getJPAEntityType(em, ees.type.name);
			Object jpaEntity = createNewJPAEntity(
					em,
					jpaEntityType,
					entity,
					true);

			em.merge(jpaEntity);
			applyOLinks(em, jpaEntityType, entity.getLinks(), jpaEntity);
			
			em.getTransaction().commit();

		} finally {
			em.close();
		}
	}

	private static Object typeSafeEntityKey(
			EntityManager em,
			EntityType<?> jpaEntityType,
			OEntityKey entityKey) {
		
		if (entityKey!=null&&jpaEntityType.getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE){
			Object id = newInstance(jpaEntityType.getIdType().getJavaType());
			applyOProperties(em, em.getMetamodel().embeddable(jpaEntityType.getIdType().getJavaType()), entityKey.asComplexProperties(), id);
			return id;
		}
		
		Class<?> javaType = jpaEntityType.getIdType().getJavaType();
		
		return TypeConverter.convert(entityKey==null?null:entityKey.asSingleValue(),javaType);
	}
	
	protected static Object coercePropertyValue(OProperty<?> prop, Class<?> javaType) {
		Object value = prop.getValue();
		try {
			return TypeConverter.convert(value, javaType);
		} catch (UnsupportedOperationException ex) {
			// let java complain
			return value;
		}
	}


}
