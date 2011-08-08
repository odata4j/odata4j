package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.core4j.Enumerable;
import org.odata4j.core.OPredicates;

/**
 * Metadata about an EDM Complex Type
 * 
 * I'm sure there are synergies between EdmComplexType and EdmEntityType that
 * aren't being exploited ...(that was fun to type :)
 * 
 * Warts:
 * - complex type inheritence not supported yet
 * 
 */
public class EdmComplexType extends EdmType {

  public final String namespace;
  public final String name;

  public final List<EdmProperty> properties;

  public EdmComplexType(String namespace, String name, List<EdmProperty> properties) {
    super(namespace + "." + name);
    this.namespace = namespace;
    this.name = name;
    this.properties = properties == null ? new ArrayList<EdmProperty>() : properties;
  }

  public EdmComplexType(String typeString) {
    super(typeString);
    int lastDot = typeString.lastIndexOf('.');
    this.namespace = typeString.substring(0, lastDot);
    this.name = typeString.substring(lastDot);
    this.properties = new ArrayList<EdmProperty>();
  }

  @Deprecated
  /**
   * use EdmBaseType.toTypeString();
   */
  public String getFQName() {
    return namespace + "." + name;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public Set<Class<?>> getJavaTypes() {
    return null;
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
    return getDeclaredProperties();
  }
}
