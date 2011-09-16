package org.odata4j.core;

/**
 * An annotated object.
 * 
 * @author rozan04
 */
public interface IAnnotated {
  
  /**
   * get all annotations associated with this
   * @return - all annotations
   */
  public Iterable<? extends IAnnotation> getAnnotations();
}
