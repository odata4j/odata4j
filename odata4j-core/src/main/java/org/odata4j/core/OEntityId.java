package org.odata4j.core;

import org.odata4j.edm.EdmEntitySet;

/**
 * The identity of a single OData entity, consisting of an entity-set and a unique entity-key within that set.
 */
public interface OEntityId {

  /**
   * Gets the entity-set for this instance.
   * 
   * @return the entity-set
   */
  EdmEntitySet getEntitySet();

  /**
   * Gets the entity-key for this instance.
   * 
   * @return the entity-key
   */
  OEntityKey getEntityKey();
}
