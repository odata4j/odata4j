package org.odata4j.producer.inmemory;

import org.odata4j.core.Delegate;

public abstract class PropertyModelDelegate implements Delegate<PropertyModel>, PropertyModel {

  @Override
  public Object getPropertyValue(Object target, String propertyName) {
    return getDelegate().getPropertyValue(target, propertyName);
  }

  @Override
  public Iterable<String> getPropertyNames() {
    return getDelegate().getPropertyNames();
  }

  @Override
  public Class<?> getPropertyType(String propertyName) {
    return getDelegate().getPropertyType(propertyName);
  }

  @Override
  public Iterable<?> getCollectionValue(Object target, String collectionName) {
    return getDelegate().getCollectionValue(target, collectionName);
  }

  @Override
  public Iterable<String> getCollectionNames() {
    return getDelegate().getCollectionNames();
  }

  @Override
  public Class<?> getCollectionElementType(String collectionName) {
    return getDelegate().getCollectionElementType(collectionName);
  }

}
