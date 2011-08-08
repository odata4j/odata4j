package org.odata4j.edm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.core.OPredicates;

public class EdmEntityType extends EdmBaseType {

  public final String namespace;
  public final String alias;
  public final String name;
  public final Boolean hasStream;
  private final List<String> keys;
  private final List<EdmProperty> properties;
  private final List<EdmNavigationProperty> navigationProperties;
  private EdmEntityType baseType;
  private String baseTypeNameFQ;

  public EdmEntityType(String namespace, String alias, String name, Boolean hasStream, List<String> keys, List<EdmProperty> properties, List<EdmNavigationProperty> navigationProperties) {
    this(namespace, alias, name, hasStream, keys, null, properties, navigationProperties);
  }

  public EdmEntityType(String namespace, String alias, String name, Boolean hasStream, List<String> keys, List<EdmProperty> properties, List<EdmNavigationProperty> navigationProperties, String baseTypeNameFQ) {
    this(namespace, alias, name, hasStream, keys, null, properties, navigationProperties);
    // during schema parsing we may not have the base type object yet...
    this.baseTypeNameFQ = baseTypeNameFQ;
  }

  public EdmEntityType(String namespace, String alias, String name, Boolean hasStream, List<String> keys, EdmEntityType baseType, List<EdmProperty> properties, List<EdmNavigationProperty> navigationProperties) {
    super(namespace + "." + name);
    this.namespace = namespace;
    this.alias = alias;
    this.name = name;
    this.hasStream = hasStream;

    this.keys = keys;
    this.baseType = baseType;
    
    if (baseType == null && keys == null)
      throw new IllegalArgumentException("Root types must have keys");
    if (baseType != null && keys != null)
      throw new IllegalArgumentException("Keys on root types only");
    
    this.properties = properties == null ? new ArrayList<EdmProperty>() : properties;
    this.navigationProperties = navigationProperties == null ? new ArrayList<EdmNavigationProperty>() : navigationProperties;
  }

  @Deprecated
  /**
   * use EdmBaseType.toTypeString()
   */
  public String getFQNamespaceName() {
    return namespace + "." + name;
  }

  public String getFQAliasName() {
    return alias == null ? null : (alias + "." + name);
  }

  @Override
  public String toString() {
    return String.format("EdmEntityType[%s.%s,alias=%s]", namespace, name, alias);
  }

  /**
   * Finds a navigation property by name, searching up the type hierarchy if necessary.
   */
  public EdmNavigationProperty findNavigationProperty(String name) {
    return getNavigationProperties().firstOrNull(OPredicates.edmNavigationPropertyNameEquals(name));
  }

  /**
   * Finds a property by name, searching up the type hierarchy if necessary.
   */
  public EdmProperty findProperty(String name) {
    return getProperties().firstOrNull(OPredicates.edmPropertyNameEquals(name));
  }

  /**
   * Gets the properties defined for this entity type <i>not including</i> inherited properties.
   */
  public Enumerable<EdmProperty> getDeclaredProperties() {
    return Enumerable.create(properties);
  }

  /**
   * Finds a property by name on this entity type <i>not including</i> inherited properties.
   */
  public EdmProperty findDeclaredProperty(String name) {
    return getDeclaredProperties().firstOrNull(OPredicates.edmPropertyNameEquals(name));
  }

  /**
   * Gets the properties defined for this entity type <i>including</i> inherited properties.
   */
  public Enumerable<EdmProperty> getProperties() {
    return isRootType()
        ? getDeclaredProperties()
        : baseType.getProperties().union(getDeclaredProperties());
  }

  /**
   * Gets the navigation properties defined for this entity type <i>not including</i> inherited properties.
   */
  public Enumerable<EdmNavigationProperty> getDeclaredNavigationProperties() {
    return Enumerable.create(navigationProperties);
  }
  
  /**
   * Finds a navigation property by name on this entity type <i>not including</i> inherited properties.
   */
  public EdmNavigationProperty findDeclaredNavigationProperty(String name) {
    return getDeclaredNavigationProperties().firstOrNull(OPredicates.edmNavigationPropertyNameEquals(name));
  }
  
  /**
   * Gets the navigation properties defined for this entity type <i>including</i> inherited properties.
   */
  public Enumerable<EdmNavigationProperty> getNavigationProperties() {
    return isRootType()
        ? getDeclaredNavigationProperties()
        : baseType.getNavigationProperties().union(getDeclaredNavigationProperties());
  }

  public EdmEntityType getBaseType() {
    return baseType;
  }

  public String getFQBaseTypeName() {
    return baseTypeNameFQ != null ? baseTypeNameFQ :
        (getBaseType() != null ? getBaseType().getFQNamespaceName() : null);
  }

  public boolean isRootType() {
    return baseType == null;
  }

  /**
   * Gets the keys for this EdmEntityType.  Keys are defined only in a root types.
   */
  public List<String> getKeys() {
    return isRootType() ? keys : baseType.getKeys();
  }

  
  //TODO remove!
  public void addNavigationProperty(EdmNavigationProperty np) {
    this.navigationProperties.add(np);
  }
  
  // TODO remove!
  public void setDeclaredNavigationProperties(Collection<EdmNavigationProperty> navProperties) {
    this.navigationProperties.clear();
    this.navigationProperties.addAll(navProperties);
  }
  
  // TODO remove!
  public void setBaseType(EdmEntityType baseType) {
    this.baseType = baseType;
  }

}
