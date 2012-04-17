package org.odata4j.producer.inmemory;

public interface PropertyModel {

  Object getPropertyValue(Object target, String propertyName);

  /**
   * gets the names of all properties defined by the model
   * @return - property names
   */
  Iterable<String> getPropertyNames();
  /**
   * gets the names of properties defined only at this inheritance level
   * in the model
   * @return - property names
   */
  Iterable<String> getDeclaredPropertyNames();

  Class<?> getPropertyType(String propertyName);

  Iterable<?> getCollectionValue(Object target, String collectionName);

  /**
   * gets the names of all collections defined by the model
   * @return - collection names
   */
  Iterable<String> getCollectionNames();
  /**
   * gets the names of collections defined only at this inheritance level
   * in the model
   * @return - collection names
   */
  Iterable<String> getDeclaredCollectionNames();

  Class<?> getCollectionElementType(String collectionName);
}
