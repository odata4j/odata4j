
package org.odata4j.edm;

/**
 * An object that knows how to produce an {@link EdmDataServices} model in the
 * context of an {@link EdmDecorator}.
 */
public interface EdmGenerator {

  EdmDecorator getDecorator();

  EdmDataServices generateEdm();

}
