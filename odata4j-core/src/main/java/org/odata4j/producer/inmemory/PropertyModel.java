package org.odata4j.producer.inmemory;

public interface PropertyModel {

  Object getPropertyValue(Object target, String propertyName);

  Iterable<String> getPropertyNames();

  Class<?> getPropertyType(String propertyName);

  Iterable<?> getCollectionValue(Object target, String collectionName);

  Iterable<String> getCollectionNames();

  Class<?> getCollectionElementType(String collectionName);

}
