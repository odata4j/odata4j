package org.odata4j.core;

import java.util.List;

import org.odata4j.edm.EdmEntitySet;

/**
 * An immutable OData entity instance, consisting of an identity (an entity-set and a unique entity-key within that set), 
 * properties (typed, named values), and links (references to other entities).
 * <p>The {@link OEntities} static factory class can be used to create <code>OEntity</code> instances.</p>
 * @see OEntities
 */
public interface OEntity extends OEntityId, OObject {

  /**
   * Get the entity-set of this instance.
   * 
   * @return the entity-set
   */
  EdmEntitySet getEntitySet();
  
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

  /**
   * Get all links of this instance.
   * 
   * @return the links
   */
  List<OLink> getLinks();

  /**
   * Get a link with a given name and link-type.
   * 
   * @param <T>  the link-type as a java-type
   * @param title  the link title
   * @param linkClass  the link-type as a java-type
   * @return the link strongly-typed as the java-type
   */
  <T extends OLink> T getLink(String title, Class<T> linkClass);
}
