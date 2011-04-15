package org.odata4j.internal;

import java.util.List;

import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmPropertyBase;
import org.odata4j.edm.EdmSchema;

public abstract class EdmDataServicesDecorator extends EdmDataServices {

	protected abstract EdmDataServices getDelegate();
	
	public EdmDataServicesDecorator() {
		super(null,null);
	}
	
	@Override
	public String getVersion() {
		return getDelegate().getVersion();
	}
	@Override
	public List<EdmSchema> getSchemas() {
		return getDelegate().getSchemas();
	}
	@Override
	public EdmEntitySet getEdmEntitySet(String entitySetName) {
		return getDelegate().getEdmEntitySet(entitySetName);
	}
	@Override
	public EdmEntitySet getEdmEntitySet(final EdmEntityType type) {
		return getDelegate().getEdmEntitySet(type);
	}
	@Override
	public EdmEntitySet findEdmEntitySet(String entitySetName) {
		return getDelegate().findEdmEntitySet(entitySetName);
	}
	@Override
	public EdmComplexType findEdmComplexType(String complexTypeFQName) {
		return getDelegate().findEdmComplexType(complexTypeFQName);
	}
	@Override
	public EdmPropertyBase findEdmProperty(String propName) {
		return getDelegate().findEdmProperty(propName);
	}
	@Override
	public Iterable<EdmEntityType> getEntityTypes() {
		return getDelegate().getEntityTypes();
	}
	@Override
	public Iterable<EdmComplexType> getComplexTypes() {
		return getDelegate().getComplexTypes();
	}
	@Override
	public Iterable<EdmAssociation> getAssociations() {
		return getDelegate().getAssociations();
	}
	@Override
	public Iterable<EdmEntitySet> getEntitySets() {
		return getDelegate().getEntitySets();
	}

}
