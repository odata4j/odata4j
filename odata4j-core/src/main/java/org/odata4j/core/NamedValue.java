package org.odata4j.core;

/**
 * A strongly-typed named value.  
 * <p>No behavior or semantics are implied, this is simply a convenient reusable interface.</p>
 * <p>Use the {@link NamedValues} static factory class to create instances.</p>
 * 
 * @param <T>  the value's java-type
 * @see NamedValues
 */
public interface NamedValue<T> {
    
    /**
     * Gets the name.
     * 
     * @return the name
     */
    public abstract String getName();
    
    /**
     * Gets the value.
     * 
     * @return the value
     */
    public abstract T getValue();
    
}
