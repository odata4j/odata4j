
package org.odata4j.edm;

import java.util.List;

import org.odata4j.core.Annotated;
import org.odata4j.core.Annotation;

/**
 * Constructs in the CSDL that we model in the org.odata4j.edm package
 * share some common functionality:
 * <li>Documentation
 * <li>Annotation (attributes and elements)
 */
public class EdmItem implements Annotated {

  private final EdmDocumentation documentation;
  private final List<? extends Annotation<?>> annotations;

  public EdmItem(EdmDocumentation documentation, List<EdmAnnotation<?>> annotations) {
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

  public static class Builder<T> {

    private EdmDocumentation documentation;
    private List<EdmAnnotation<?>> annotations;

    Builder() {

    }

    public EdmDocumentation getDocumentation() {
      return documentation;
    }

    public List<EdmAnnotation<?>> getAnnotations() {
      return annotations;
    }

    @SuppressWarnings("unchecked")
    public T setDocumentation(EdmDocumentation documentation) {
      this.documentation = documentation;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setAnnotations(List<EdmAnnotation<?>> annotations) {
      this.annotations = annotations;
      return (T) this;
    }

  }

}
