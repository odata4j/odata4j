
package org.odata4j.edm;

/**
 * An object that knows how to produce an EdmDataServices model in the 
 * context of an IEdmDecorator.
 * 
 * @author Tony Rozga
 */
public interface IEdmGenerator {
  
  IEdmDecorator getDecorator();
  
  EdmDataServices generateEdm();
}
