package org.odata4j.core;

/**
 * Basic extension mechanism.
 *
 * @param <T>  the type being extended
 *
 * @see OExtension
 */
public interface OExtensible<T> {

  /**
   * Find extension instance given an interface, if one exists.
   *
   * @param clazz  the extension interface
   * @param <TExtension>  type of extension
   * @return the extension instance or null
   *
   * @see OAtomStreamEntity
   * @see OAtomEntity
   */
  <TExtension extends OExtension<T>> TExtension findExtension(Class<TExtension> clazz);

}
