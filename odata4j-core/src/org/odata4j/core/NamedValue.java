package org.odata4j.core;

public interface NamedValue<T> {
    
    public abstract String getName();
    public abstract T getValue();
    
}
