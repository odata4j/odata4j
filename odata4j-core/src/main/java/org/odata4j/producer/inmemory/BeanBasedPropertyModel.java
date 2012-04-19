package org.odata4j.producer.inmemory;

public class BeanBasedPropertyModel implements PropertyModel {

  private final BeanModel beanModel;
  
  public BeanBasedPropertyModel(Class<?> clazz) {
    this(clazz, true);
  }
  
  /**
   * construct
   * @param clazz - the POJO class that defines the model
   * @param flatten - if true, the model will flatten property names and
   *                  collection names.
   *                  
   *                  if false, the model will distinguish *declared* property/collection names
   *                  from *all* (i.e. include inherited) property/collection names.  This is
   *                  the same terminology used in the Edm classes and the Java Class class.
   *                  Note that all model methods that are value centric (getPropertyValue, etc)
   *                  will automatically work up the hierarchy if necessary.
   */
  public BeanBasedPropertyModel(Class<?> clazz, boolean flatten) {
    beanModel = new BeanModel(clazz, flatten);
  }
  
  @Override
  public Iterable<String> getPropertyNames() {
    return beanModel.getPropertyNames();
  }

  @Override
  public Class<?> getPropertyType(String propertyName) {
    return beanModel.getPropertyType(propertyName);
  }

  @Override
  public Object getPropertyValue(Object target, String propertyName) {
    return beanModel.getPropertyValue(target, propertyName);
  }

  @Override
  public Iterable<String> getCollectionNames() {
    return beanModel.getCollectionNames();
  }

  @Override
  public Iterable<?> getCollectionValue(Object target, String collectionName) {
    return beanModel.getCollectionValue(target, collectionName);
  }

  @Override
  public Class<?> getCollectionElementType(String collectionName) {
    return beanModel.getCollectionElementType(collectionName);
  }

  @Override
  public Iterable<String> getDeclaredPropertyNames() {
    return beanModel.getDeclaredPropertyNames();
  }

  @Override
  public Iterable<String> getDeclaredCollectionNames() {
    return beanModel.getDeclaredCollectionNames();
  }

}
