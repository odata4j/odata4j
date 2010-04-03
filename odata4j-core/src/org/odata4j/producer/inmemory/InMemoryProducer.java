package org.odata4j.producer.inmemory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;


import com.sun.jersey.api.NotFoundException;

import core4j.Enumerable;
import core4j.Func;
import core4j.Func1;
import core4j.Predicate1;

public class InMemoryProducer implements ODataProducer {

	
	private static class EntityInfo<TEntity,TKey>{
		Class<TEntity> entityClass;
		Class<TKey> keyClass;
		Func<Iterable<TEntity>> get;
		Func1<Object,TKey> id;
		Map<String,Method> getters;
		
		public Object invokeGetter( Object target, String propertyName){
			try {
				Method m = getters.get(propertyName);
				if (!m.isAccessible())
					m.setAccessible(true);
				return m.invoke(target);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static final String ID_PROPNAME = "EntityId";
	private static final String CONTAINER_NAME = "Container";
	private static final Map<Class<?>,EdmType> SUPPORTED_TYPES = new HashMap<Class<?>,EdmType>();
	static 
	{
		SUPPORTED_TYPES.put(String.class, EdmType.STRING);
		SUPPORTED_TYPES.put(Long.class, EdmType.INT64);
		SUPPORTED_TYPES.put(Long.TYPE, EdmType.INT64);
		SUPPORTED_TYPES.put(Integer.class, EdmType.INT32);
		SUPPORTED_TYPES.put(Integer.TYPE, EdmType.INT32);
		SUPPORTED_TYPES.put(Boolean.class, EdmType.BOOLEAN);
		SUPPORTED_TYPES.put(Boolean.TYPE, EdmType.BOOLEAN);
		SUPPORTED_TYPES.put(Object.class, EdmType.STRING);
		
	}
	
	
	
	private final String namespace;
	private final Map<String,EntityInfo<?,?>> eis = new  HashMap<String,EntityInfo<?,?>>();
	private EdmDataServices metadata;
	
	public InMemoryProducer(String namespace){
		this.namespace = namespace;
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

		

		for (String entitySetName : eis.keySet()) {
			EntityInfo<?,?> ei = eis.get(entitySetName);
			
			List<EdmProperty> properties = new ArrayList<EdmProperty>();
			properties.add(new EdmProperty(ID_PROPNAME, getEdmType(ei.keyClass), false, null));
			
			properties.addAll(toEdmProperties(ei.entityClass));
			
			
			EdmEntityType eet = new EdmEntityType(namespace, entitySetName, ID_PROPNAME,properties, null);
			EdmEntitySet ees = new EdmEntitySet(entitySetName, eet);
			entitySets.add(ees);
			entityTypes.add(eet);
		}

		EdmEntityContainer container = new EdmEntityContainer(CONTAINER_NAME,true, entitySets, null);
		containers.add(container);

		EdmSchema schema = new EdmSchema(namespace, entityTypes, null,containers);
		schemas.add(schema);
		EdmDataServices rt = new EdmDataServices(schemas);
		return rt;
	}
	
	
	@Override
	public void close() {
		 
	}

	
	
	
	private static <T1,T2> Func1<Object,T2> widen(final Func1<T1,T2> fn){
		return new Func1<Object,T2>(){

			@SuppressWarnings("unchecked")
			@Override
			public T2 apply(Object input) {
				return fn.apply((T1)input);
			}};
	}

	public <TEntity,TKey> void register(final Class<TEntity> entityClass, Class<TKey> keyClass, final String entitySetName, 
			Func<Iterable<TEntity>> get, final String idPropertyName){
		register(entityClass,keyClass,entitySetName,get,new Func1<TEntity,TKey>(){

			@SuppressWarnings("unchecked")
			@Override
			public TKey apply(TEntity input) {
				return (TKey)eis.get(entitySetName).invokeGetter(input, idPropertyName);
			}});
	}
	public <TEntity,TKey> void register(Class<TEntity> entityClass, Class<TKey> keyClass, String entitySetName, 
			Func<Iterable<TEntity>> get, final Func1<TEntity,TKey> id){
		
		EntityInfo<TEntity,TKey> ei = new EntityInfo<TEntity, TKey>();
		ei.getters = getBeanGetters(entityClass);
		ei.entityClass = entityClass;
		ei.get = get;
		ei.id = widen(id);
		ei.keyClass = keyClass;
		eis.put(entitySetName, ei);
		this.metadata = buildMetadata();
	}
	

	
	
	private OEntity toOEntity(EntityInfo<?,?> ei, Object obj){
		final List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
		
		Object key = ei.id.apply(obj);
		properties.add(OProperties.simple(ID_PROPNAME, getEdmType(ei.keyClass), key));
		
		
		for(String getterName : ei.getters.keySet()){
			Method getter = ei.getters.get(getterName);
			EdmType type;
			Object value = ei.invokeGetter(obj, getterName);
			
			if (getter.getReturnType().isEnum()){
				type = EdmType.STRING;
			} else {
				type = findEdmType(getter.getReturnType());
				if (type==null)
					continue;
			}
			if (type==EdmType.STRING)
				value = value.toString();
			
			properties.add(OProperties.simple(getterName, type, value));
		}
		
		
		final List<OProperty<?>> keyProperties = properties.subList(0,1);
		OEntity oe = new OEntity(){

			@Override
			public List<OProperty<?>> getKeyProperties() {
				return keyProperties;
			}

			@Override
			public List<OProperty<?>> getProperties() {
				return properties;
			}};
			
			return oe;
	}
	
	
	@Override
	public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
		final EntityInfo<?,?> ei = eis.get(entitySetName);
		
		Enumerable<Object> iter = Enumerable.create(ei.get.apply()).cast(Object.class);
		
		if (queryInfo.skip != null)
			iter = iter.skip(queryInfo.skip);
		if (queryInfo.top != null)
			iter = iter.take(queryInfo.top);
		
		final List<OEntity> entities = iter.select(new Func1<Object,OEntity>(){
			public OEntity apply(Object input) {
				return toOEntity(ei,input);
			}}).toList();
		
		final Integer inlineCount = queryInfo.inlineCount==InlineCount.ALLPAGES?iter.count():null;
		
		return new EntitiesResponse(){

			@Override
			public List<OEntity> getEntities() {
				return entities;
			}

			@Override
			public EdmEntitySet getEntitySet() {
				return ees;
			}

			@Override
			public Integer getInlineCount() {
				return inlineCount;
			}};
	}

	@SuppressWarnings("unchecked")
	@Override
	public EntityResponse getEntity(String entitySetName, Object entityKey) {
		final EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
		final EntityInfo<?,?> ei = eis.get(entitySetName);
		
		// TODO make more generic
		if (ei.keyClass.equals(Long.class) && entityKey instanceof Integer){
			entityKey = ((Integer)entityKey).longValue();
		}
		Iterable<Object> iter = (Iterable<Object>)ei.get.apply();
		
		final Object finalKey = entityKey;
		final Object rt = Enumerable.create(iter).firstOrNull(new Predicate1<Object>(){
			public boolean apply(Object input) {
				Object id = ei.id.apply(input);
				
				return finalKey.equals(id);
			}});
		if (rt==null)
			throw new NotFoundException();
		
		
		final OEntity oe = toOEntity(ei,rt);
		
		return new EntityResponse(){

			@Override
			public OEntity getEntity() {
				return oe;
			}

			@Override
			public EdmEntitySet getEntitySet() {
				return ees;
			}};
	}

	
	
	private Collection<EdmProperty> toEdmProperties(Class<?> entityClass){
		List<EdmProperty> rt = new ArrayList<EdmProperty>();
		
		Map<String,Method> getters = getBeanGetters(entityClass);
		for(String name : getters.keySet()){
			Class<?> returnType = getters.get(name).getReturnType();
			EdmType type;
			if (returnType.isEnum()){
				type = EdmType.STRING;
			} else {
				type = findEdmType(returnType);
				if (type==null)
					continue;
			
			}
			rt.add(new EdmProperty(name,type,true,null));
		}
		
		return rt;
	}
	
	
	private EdmType getEdmType(Class<?> clazz){
		EdmType type = findEdmType(clazz);
		if (type != null)
			return type;
		throw new UnsupportedOperationException(clazz.getName());
	}
	private EdmType findEdmType(Class<?> clazz){
		EdmType type = SUPPORTED_TYPES.get(clazz);
		if (type != null)
			return type;
		return null;
	}
	
	
	private static Map<String,Method> getBeanGetters(Class<?> clazz){
		
		 Map<String,Method> rt = new HashMap<String,Method>();
		 for(Method method : clazz.getMethods()){
			String methodName = method.getName();
			if (
					methodName.startsWith("get") 
					&& methodName.length()>3 
					&& Character.isUpperCase(methodName.charAt(3))
					&& method.getParameterTypes().length==0
					&& !method.getReturnType().equals(Void.TYPE)
					&& !Modifier.isStatic(method.getModifiers())
					
					){
				String name = methodName.substring(3);
				rt.put(name, method);
				
			}
			if (
					methodName.startsWith("is") 
					&& methodName.length()>2
					&& Character.isUpperCase(methodName.charAt(2))
					&& method.getParameterTypes().length==0
					&& (method.getReturnType().equals(Boolean.class) || method.getReturnType().equals(Boolean.TYPE))
					&& !Modifier.isStatic(method.getModifiers())
					){
				String name = methodName.substring(2);
				rt.put(name, method);
				
			}
		 }
		 return rt;
	}
	
	
	
	
	
	
	
	
	@Override
	public void mergeEntity(String entitySetName, Object entityKey, List<OProperty<?>> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateEntity(String entitySetName, Object entityKey, List<OProperty<?>> properties) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void deleteEntity(String entitySetName, Object entityKey) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public EntityResponse createEntity(String entitySetName, List<OProperty<?>> properties) {
		throw new UnsupportedOperationException();
	}

}
