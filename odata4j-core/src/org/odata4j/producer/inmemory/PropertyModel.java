package org.odata4j.producer.inmemory;

public interface PropertyModel {

	public abstract Object getPropertyValue(Object target, String propertyName);
	public abstract Iterable<String> getPropertyNames();
	public abstract Class<?> getPropertyType(String propertyName);
}
