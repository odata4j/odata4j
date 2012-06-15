
package org.odata4j.core;

import java.util.Map;

public interface OServiceProvider {
 
  /**
   * find an extension service that implements the requested interface
   * @param clazz - the desired service type
   * @param params - parameters
   * @return 
   */
  Object findService(Class<?> clazz, Map<String, Object> params);

}
