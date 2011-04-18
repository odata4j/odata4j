package org.odata4j.producer;

import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;

/**
 * A static factory to create immutable {@link EntitiesResponse}, {@link EntityResponse}, or {@link PropertyResponse} instances.
 */
public class Responses {

	private Responses(){}
	
	/**
	 * Create a new <code>EntitiesResponse</code> instance.
	 * 
	 * @param entities  the OData entities, if any
	 * @param entitySet  the entity-set
	 * @param inlineCount  the inline-count value, if necessary
	 * @param skipToken  the continuation-token, if necessary
	 * @return a new <code>EntitiesResponse</code> instance
	 */
	public static EntitiesResponse entities(
			final List<OEntity> entities,
			final EdmEntitySet entitySet, 
			final Integer inlineCount,
			final String skipToken) {
		return new EntitiesResponse() {

			@Override
			public List<OEntity> getEntities() {
				return entities;
			}

			@Override
			public EdmEntitySet getEntitySet() {
				return entitySet;
			}

			@Override
			public Integer getInlineCount() {
				return inlineCount;
			}

			@Override
			public String getSkipToken() {
				return skipToken;
			}
		};
	}
	
	/**
	 * Create a new <code>EntityResponse</code> instance.
	 * 
	 * @param entity  the OData entity
	 * @return a new <code>EntityResponse</code> instance
	 */
	public static EntityResponse entity(final OEntity entity){
		return new EntityResponse(){
			@Override
			public OEntity getEntity() {
				return entity;
			}};
	}

	/**
	 * Create a new <code>PropertyResponse</code> instance.
	 * 
	 * @param property  the property value
	 * @return a new <code>PropertyResponse</code> instance
	 */
	public static PropertyResponse property(final OProperty<?> property) {
		return new PropertyResponse(){

			@Override
			public OProperty<?> getProperty() {
				return property;
			}};
	}
}
