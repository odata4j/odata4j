
package org.odata4j.edm;

import java.util.List;
import org.odata4j.core.IAnnotated;
import org.odata4j.core.IAnnotation;

/**
 * Constructs in the CSDL that we model with the org.odata4j.edm classes all 
 * share some common funcionality:
 * - Documentation
 * - Annotation (attributes and elements)
 * 
 * 
 * @author Tony Rozga
 */
public class EdmItem implements IAnnotated {
  
  public EdmItem(EdmDocumentation documentation, List<EdmAnnotation> annotations) {
    this.documentation = documentation;
    this.annotations = annotations;
  }
  
  public EdmDocumentation getDocumentation() {
    return this.documentation;
  }

  public Iterable<? extends IAnnotation> getAnnotations() {
    return annotations;
  }
  
  public IAnnotation findAnnotation(IAnnotation a) {
    if (null == annotations) { return null; }
    for(IAnnotation check : annotations) {
      if (check.equals(a)) { 
        return check;
      }
    }
    return null;
  }
  
  private final EdmDocumentation documentation;
  private final List<? extends IAnnotation> annotations;
}
