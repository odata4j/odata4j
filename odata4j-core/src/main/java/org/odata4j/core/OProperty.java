package org.odata4j.core;

import org.odata4j.edm.EdmType;

/**
 * An immutable OData property instance, consisting of a name, a strongly-typed value, and an edm-type.
 *
 * @param <T>  the java-type of the property
 */
public interface OProperty<T> extends NamedValue<T>{

    /**
     * Gets the edm-type for this property.
     * 
     * @return the edm-type
     */
    public abstract EdmType getType();
    
}
