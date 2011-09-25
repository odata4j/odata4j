package org.odata4j.core;

import java.util.List;

public interface OStructuralObject extends OObject {

  /**
   * Get all properties of this instance.
   *
   * @return the properties
   */
  List<OProperty<?>> getProperties();

  /**
   * Get a property by name.
   *
   * @param propName  the property name
   * @return the property
   */
  OProperty<?> getProperty(String propName);

  /**
   * Get a property by name as a strongly-typed OProperty.
   *
   * @param <T>  the java-type of the property
   * @param propName  the property name
   * @param propClass  the java-type of the property
   * @return the property
   */
  <T> OProperty<T> getProperty(String propName, Class<T> propClass);
}
