package org.odata4j.producer;

import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;

public class Responses {

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
	
	public static EntityResponse entity(final EdmEntitySet ees, final OEntity entity){
		return new EntityResponse(){

			@Override
			public EdmEntitySet getEntitySet() {
				return ees;
			}

			@Override
			public OEntity getEntity() {
				return entity;
			}};
	}

	public static PropertyResponse property(final OProperty<?> property) {
		return new PropertyResponse(){

			@Override
			public OProperty<?> getProperty() {
				return property;
			}};
	}
}
