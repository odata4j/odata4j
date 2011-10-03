package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.core.Named;
import org.odata4j.core.OPredicates;

public abstract class EdmStructuralType extends EdmNonSimpleType implements Named {

  private final String namespace;
  private final String name;
  private final List<EdmProperty> declaredProperties;
  private final Boolean isAbstract;
  private EdmEntityType baseType;

  protected EdmStructuralType(EdmEntityType baseType, String namespace, String name, List<EdmProperty.Builder> declaredProperties) {
    this(baseType, namespace, name, declaredProperties, null, null, null);
  }

  protected EdmStructuralType(EdmEntityType baseType, String namespace, String name, List<EdmProperty.Builder> declaredProperties,
      EdmDocumentation doc, List<EdmAnnotation<?>> annotations) {
    this(baseType, namespace, name, declaredProperties, doc, annotations, null);
  }

  protected EdmStructuralType(EdmEntityType baseType, String namespace, String name, List<EdmProperty.Builder> declaredProperties,
      EdmDocumentation doc, List<EdmAnnotation<?>> annotations, Boolean isAbstract) {
    super(namespace + "." + name, doc, annotations);
    this.baseType = baseType;
    this.namespace = namespace;
    this.name = name;
    this.isAbstract = isAbstract;
    this.declaredProperties = new ArrayList<EdmProperty>();
    if (declaredProperties != null) {
      for (EdmProperty.Builder declaredProperty : declaredProperties) {
        this.declaredProperties.add(declaredProperty.setDeclaringType(this).build());
      }
    }
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  public Boolean getIsAbstract() {
    return isAbstract;
  }

  public EdmEntityType getBaseType() {
    return baseType;
  }

  /**
   * Finds a property by name, searching up the type hierarchy if necessary.
   */
  public EdmProperty findProperty(String name) {
    return getProperties().firstOrNull(OPredicates.edmPropertyNameEquals(name));
  }

  /**
   * Gets the properties defined for this structural type <i>not including</i> inherited properties.
   */
  public Enumerable<EdmProperty> getDeclaredProperties() {
    return Enumerable.create(declaredProperties);
  }

  /**
   * Finds a property by name on this structural type <i>not including</i> inherited properties.
   */
  public EdmProperty findDeclaredProperty(String name) {
    return getDeclaredProperties().firstOrNull(OPredicates.edmPropertyNameEquals(name));
  }

  /**
   * Gets the properties defined for this structural type <i>including</i> inherited properties.
   */
  public Enumerable<EdmProperty> getProperties() {
    return isRootType()
        ? getDeclaredProperties()
        : baseType.getProperties().union(getDeclaredProperties());
  }

  public boolean isRootType() {
    return baseType == null;
  }

  // TODO remove!
  public void setBaseType(EdmEntityType baseType) {
    this.baseType = baseType;
  }

}
