package org.odata4j.producer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.HttpHeaders;

/**
 * An implementation (prolly the only one ever) of ODataContext
 */
public class ODataContextImpl implements ODataContext {

  private ODataContextImpl() {}
  
  @Override
  public <T> T getContextAspect(Class<T> contextClass) {
    for (Entry<Class, Object> aspect : contexts.entrySet()) {
      if (contextClass.isAssignableFrom(aspect.getKey())) {
        return (T) aspect.getValue();
      }
    }
    return null;
  }

  @Override
  public ODataHeadersContext getRequestHeadersContext() {
    return getContextAspect(ODataHeadersContext.class);
  }
  
  public static ODataContextBuilder builder() {
    return new ODataContextBuilder();
  }
  
  public static class ODataContextBuilder {
    protected ODataContextBuilder() {
    }
    
    public ODataContextBuilder aspect(Object aspect) {
      if (HttpHeaders.class.isAssignableFrom(aspect.getClass())) {
        impl.addContextAspect(new ODataHeadersImpl((HttpHeaders)aspect));
      } else {
        impl.addContextAspect(aspect);
      }
      return this;
    }
    
    public ODataContextImpl build() {
      return impl;
    }
    
    private ODataContextImpl impl = new ODataContextImpl();
  }
  
  private void addContextAspect(Object aspect) {
    contexts.put(aspect.getClass(), aspect);
  }

  private Map<Class, Object> contexts = new HashMap<Class, Object>();
}
