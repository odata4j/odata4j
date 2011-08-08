package org.odata4j.core;

/**
 * An object representing an EDM simple type.  
 * 
 * V is the Java class used to represent the simple type (@see OProperties)
 */
public interface OSimpleObject<V> extends OObject {

   public V getValue();

}
