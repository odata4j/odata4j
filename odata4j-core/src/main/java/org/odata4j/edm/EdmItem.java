
package org.odata4j.edm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odata4j.core.Annotated;
import org.odata4j.core.Annotation;
import org.odata4j.core.ImmutableList;

/**
 * Constructs in the CSDL that we model in the org.odata4j.edm package
 * share some common functionality:
 * <li>Documentation
 * <li>Annotation (attributes and elements)
 */
public class EdmItem implements Annotated {

  private final EdmDocumentation documentation;
  private final ImmutableList<? extends Annotation<?>> annotations;

  protected EdmItem(EdmDocumentation documentation, ImmutableList<EdmAnnotation<?>> annotations) {
    this.documentation = documentation;
    this.annotations = annotations;
  }

  public EdmDocumentation getDocumentation() {
    return documentation;
  }

  public Iterable<? extends Annotation<?>> getAnnotations() {
    return annotations;
  }

  public Annotation<?> findAnnotation(String namespaceUri, String localName) {
    if (annotations != null) {
      for(Annotation<?> annotation : annotations) {
        if (annotation.getNamespaceUri().equals(namespaceUri) && annotation.getLocalName().equals(localName))
          return annotation;
      }
    }
    return null;
  }

  static class BuilderContext {

    private final Map<Object, Builder<?, ?>> newBuilders = new HashMap<Object, Builder<?, ?>>();
    private final EdmDataServices.Builder dataServices;
    
    public BuilderContext(EdmDataServices.Builder ds) {
      this.dataServices = ds;
    }
    
    public EdmDataServices.Builder getDataServices() {
      return this.dataServices;
    }
    
    @SuppressWarnings("unchecked")
    public <T, TBuilder> TBuilder newBuilder(T item, Builder<T, TBuilder> builder) {
      if (!newBuilders.containsKey(item)) {
        newBuilders.put(item, (Builder<?, ?>) builder.newBuilder(item, this));
      }
      return (TBuilder) newBuilders.get(item);
    }

    public <T, TBuilder extends Builder<?, ?>> void register(T item, TBuilder builder) {
      newBuilders.put(item, builder);
    }

  }


  protected abstract static class Builder<T, TBuilder> {

    private EdmDocumentation documentation;
    private List<EdmAnnotation<?>> annotations;

    abstract TBuilder newBuilder(T item, BuilderContext context);

    public EdmDocumentation getDocumentation() {
      return documentation;
    }

    public List<EdmAnnotation<?>> getAnnotations() {
      return annotations;
    }

    @SuppressWarnings("unchecked")
    public TBuilder setDocumentation(EdmDocumentation documentation) {
      this.documentation = documentation;
      return (TBuilder) this;
    }

    @SuppressWarnings("unchecked")
    public TBuilder setAnnotations(List<EdmAnnotation<?>> annotations) {
      this.annotations = annotations;
      return (TBuilder) this;
    }

  }

}
