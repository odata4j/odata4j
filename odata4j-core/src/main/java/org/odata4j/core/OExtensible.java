package org.odata4j.core;

import java.util.Map;

/**
 * Basic extension mechanism.
 *
 * @param <T>  the type being extended
 *
 * @see OExtension
 */
public interface OExtensible<T> {

  /**
   * Finds an extension instance given an interface, if one exists.
   *
   * @param clazz  the extension interface
   * @param <TExtension>  type of extension
   * @param params optional additional parameters to assist in finding/creating the extension
   * @return the extension instance or null
   *
   * @see OAtomStreamEntity
   * @see OAtomEntity
   */
  <TExtension extends OExtension<T>> TExtension findExtension(Class<TExtension> clazz,
      Map<String, Object> params);

}
