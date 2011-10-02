package org.odata4j.core;

/**
 * An object that maintains a set of {@link Annotation}s.
 */
public interface Annotated {

  Iterable<? extends Annotation<?>> getAnnotations();

}
