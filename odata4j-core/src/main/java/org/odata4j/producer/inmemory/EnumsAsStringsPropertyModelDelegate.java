package org.odata4j.producer.inmemory;

public class EnumsAsStringsPropertyModelDelegate extends PropertyModelDelegate {

  private final PropertyModel propertyModel;

  public EnumsAsStringsPropertyModelDelegate(PropertyModel propertyModel) {
    this.propertyModel = propertyModel;
  }

  @Override
  public PropertyModel getDelegate() {
    return propertyModel;
  }

  @Override
  public Class<?> getPropertyType(String propertyName) {
    Class<?> rt = super.getPropertyType(propertyName);
    if (rt != null && rt.isEnum())
      return String.class;
    return rt;
  }

  @Override
  public Object getPropertyValue(Object target, String propertyName) {
    Class<?> baseType = super.getPropertyType(propertyName);
    Object rt = super.getPropertyValue(target, propertyName);
    if (baseType != null && baseType.isEnum() && rt != null)
      return ((Enum<?>) rt).name();
    return rt;
  }
  
  @Override
  public void setPropertyValue(Object target, String propertyName, Object value) {
    Class baseType = super.getPropertyType(propertyName);
    if (baseType != null && baseType.isEnum() && value instanceof String) {
      // convert string to enum value
      getDelegate().setPropertyValue(target, propertyName, Enum.valueOf(baseType, (String)value));
    } else {
      getDelegate().setPropertyValue(target, propertyName, value);
    }
  }

}
