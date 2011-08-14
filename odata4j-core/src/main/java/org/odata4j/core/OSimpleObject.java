package org.odata4j.core;

import org.odata4j.edm.EdmSimpleType;

/**
 * An instance of an {@link EdmSimpleType}.
 * 
 * V is the Java class used to represent the simple type (@see OProperties)
 */
public interface OSimpleObject<V> extends OObject {

   V getValue();

}
