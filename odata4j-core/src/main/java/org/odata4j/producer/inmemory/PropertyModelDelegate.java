package org.odata4j.producer.inmemory;

public abstract class PropertyModelDelegate implements PropertyModel {

  public abstract PropertyModel getDelegate();

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
