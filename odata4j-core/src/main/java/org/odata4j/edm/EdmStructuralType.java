package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.core.OPredicates;

public abstract class EdmStructuralType extends EdmNonSimpleType {

  public final String namespace;
  public final String name;
  public final List<EdmProperty> properties;
  private EdmEntityType baseType;
  public final Boolean isAbstract;

  protected EdmStructuralType(EdmEntityType baseType, String namespace, String name, List<EdmProperty> properties) {
    this(baseType, namespace, name, properties, null, null, null);
  }    
  
  protected EdmStructuralType(EdmEntityType baseType, String namespace, String name, List<EdmProperty> properties,
          EdmDocumentation doc, List<EdmAnnotation> annotations) {
    this(baseType, namespace, name, properties, doc, annotations, null);
  }
  
  protected EdmStructuralType(EdmEntityType baseType, String namespace, String name, List<EdmProperty> properties,
          EdmDocumentation doc, List<EdmAnnotation> annotations, Boolean isAbstract) {
    super(namespace + "." + name, doc, annotations);
    this.baseType = baseType;
    this.namespace = namespace;
    this.name = name;
    this.isAbstract = isAbstract;
    this.properties = properties == null ? new ArrayList<EdmProperty>() : properties;
    
    for (EdmProperty prop : this.properties) {
      prop.setStructuralType(this);
    }
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
    return Enumerable.create(properties);
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
