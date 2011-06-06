package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;

public class EdmEntityType {

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
    this.namespace = namespace;
    this.alias = alias;
    this.name = name;
    this.hasStream = hasStream;

    this.keys = keys;
    this.baseType = baseType;
    assert (null == baseType && null != keys) ||
           (null != baseType && null == keys) : "keys on basetype only";
    
    this.properties = properties == null ? new ArrayList<EdmProperty>() : properties;
    this.navigationProperties = navigationProperties == null ? new ArrayList<EdmNavigationProperty>() : navigationProperties;
  }

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
   * get a navigation property by name, searches up the type hierarchy if necessary
   * @param name
   * @return EdmNavigationProperty object
   */
  public EdmNavigationProperty getNavigationProperty(final String name) {
    return this.getAllNavigationProperties()
        .firstOrNull(new Predicate1<EdmNavigationProperty>() {
          @Override
          public boolean apply(EdmNavigationProperty input) {
            return input.name.equals(name);
          }
        });
  }

  /**
   * get a property by name, searches up the type hierarchy if necessary
   * @param name
   * @return EdmProperty object
   */
  public EdmProperty getProperty(final String name) {
    return this.getAllProperties()
        .firstOrNull(new Predicate1<EdmProperty>() {
          @Override
          public boolean apply(EdmProperty input) {
            return input.name.equals(name);
          }
        });
  }

  /**
   * get the properties defined for this entity type
   * *not including* inherited properties
   * @return
   */
  public List<EdmProperty> getScopedProperties() {
    return properties;
  }

  /**
   * get an Enumerable over the properties defined for this entity type
   * *not including* inherited properties
   * @return
   */
  public Enumerable<EdmProperty> getScopedPropertiesEnumerable() {
    return Enumerable.create(properties);
  }

  public EdmProperty getScopedProperty(final String name) {
    return this.getScopedPropertiesEnumerable()
        .firstOrNull(new Predicate1<EdmProperty>() {
          @Override
          public boolean apply(EdmProperty input) {
            return input.name.equals(name);
          }
        });
  }

  /**
   * get the properties defined for this entity type
   * *including* inherited properties
   * @return
   */
  public Enumerable<EdmProperty> getAllProperties() {
    Enumerable<EdmProperty> e = (null == this.baseType ? null : this.baseType.getAllProperties());
    e = (null == e ? getScopedPropertiesEnumerable() : e.union(getScopedPropertiesEnumerable()));
    return e;
  }

  /**
   * get the navigation properties defined for this entity type
   * *not including* inherited properties
   * @return
   */
  public List<EdmNavigationProperty> getScopedNavigationProperties() {
    return this.navigationProperties;
  }

   /**
   * get an Enumerable over the navigation properties defined for this entity type
   * *not including* inherited properties
   * @return
   */
  public Enumerable<EdmNavigationProperty> getScopedNavigationPropertiesEnumerable() {
    return Enumerable.create(this.navigationProperties);
  }

  public EdmNavigationProperty getScopedNavigationProperty(final String name) {
    return this.getScopedNavigationPropertiesEnumerable()
        .firstOrNull(new Predicate1<EdmNavigationProperty>() {
          @Override
          public boolean apply(EdmNavigationProperty input) {
            return input.name.equals(name);
          }
        });
  }

  /**
   * get an Enumerable over the navigation properties defined for this entity type
   * *including* inherited properties
   * @return
   */
  public Enumerable<EdmNavigationProperty> getAllNavigationProperties() {
    Enumerable<EdmNavigationProperty> e = (null == this.baseType ? null : this.baseType.getAllNavigationProperties());
    e = (null == e ? getScopedNavigationPropertiesEnumerable() : e.union(getScopedNavigationPropertiesEnumerable()));
    return e;
  }

  public EdmEntityType getBaseType() {
    return this.baseType;
  }

  public void setBaseType(EdmEntityType baseType) {
    this.baseType = baseType;
  }

  public String getFQBaseTypeName() {
    return null != this.baseTypeNameFQ ? this.baseTypeNameFQ :
        (null != this.getBaseType() ? this.getBaseType().getFQNamespaceName() : null);
  }

  public boolean isRootType() {
    return null == this.baseType;
  }

  /**
   * get keys for this EdmEntityType.  Keys are defined only in a root types
   * @return
   */
  public List<String> getKeys() {
    return null == this.baseType ? this.keys : this.baseType.getKeys();
  }

  public void addNavigationProperty(EdmNavigationProperty np) {
    this.navigationProperties.add(np);
  }


}
