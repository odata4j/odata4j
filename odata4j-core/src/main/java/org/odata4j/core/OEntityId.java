package org.odata4j.core;


/**
 * The identity of a single OData entity, consisting of an entity-set name and a unique entity-key within that set.
 */
public interface OEntityId {

  /**
   * Gets the entity-set name for this instance.
   * 
   * @return the entity-set name
   */
  String getEntitySetName();

  /**
   * Gets the entity-key for this instance.
   * 
   * @return the entity-key
   */
  OEntityKey getEntityKey();
}
